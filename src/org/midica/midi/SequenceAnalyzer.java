/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.midi;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Track;

import org.midica.config.Dict;
import org.midica.file.CharsetUtils;
import org.midica.file.read.ParseException;
import org.midica.file.write.MidicaPLExporter;
import org.midica.ui.model.SingleMessage;
import org.midica.ui.model.MessageTreeNode;
import org.midica.ui.model.MidicaTreeModel;

import com.sun.media.sound.MidiUtils;

/**
 * This class analyzes a MIDI sequence and collects information from it.
 * This information can be displayed later by the {@link org.midica.ui.info.InfoView}
 * 
 * It also adds marker events to the sequence at each tick where the
 * channel activity changes for at least one channel.
 * 
 * @author Jan Trukenmüller
 */
public class SequenceAnalyzer {
	
	public static final byte NOTE_HISTORY_BUFFER_SIZE_PAST   = 5;
	public static final byte NOTE_HISTORY_BUFFER_SIZE_FUTURE = 3;
	
	private static final long DEFAULT_CHANNEL_CONFIG_TICK = -100;
	
	private static Sequence  sequence      = null;
	private static String    chosenCharset = null;
	
	private static HashMap<String, Object> sequenceInfo = null;
	
	private static MidicaTreeModel banksAndInstrPerChannel = null;
	private static MidicaTreeModel banksAndInstrTotal      = null;
	private static MidicaTreeModel msgTreeModel            = null;
	
	private static ArrayList<SingleMessage> messages = null;
	
	/**                   channel    --     note      --     tick -- on/off */
	private static TreeMap<Byte, TreeMap<Byte, TreeMap<Long, Boolean>>> noteOnOffByChannel = null;
	
	/**                    channel  --   tick    --    note -- velocity */
	private static TreeMap<Byte, TreeMap<Long, TreeMap<Byte, Byte>>> noteHistory = null;
	
	/**                    channel  --   tick -- number of keys pressed at this time */
	private static TreeMap<Byte, TreeMap<Long, Integer>> activityByChannel = null;
	
	/**                    tick     --   channel */
	private static TreeMap<Long, TreeSet<Byte>> markers = null;
	
	/**                    tick */
	private static TreeSet<Long> markerTicks = null;
	
	/**
	 * History of the instrument configuration for each channel.
	 * 
	 * - channel
	 * - tick
	 * - index:
	 *     - 0: bankMSB
	 *     - 1: bankLSB
	 *     - 2: program number
	 */
	private static TreeMap<Byte, TreeMap<Long, Byte[]>> instrumentHistory = null;
	
	/**                    channel   --  tick -- comment   */
	private static TreeMap<Byte, TreeMap<Long, String>> commentHistory = null;
	
	/**
	 * Describes which parameter (RPN/NRPN and MSB/LSB) will be changed in case of a
	 * data entry / data increment / data decrement.
	 * 
	 * - channel
	 * - index
	 *     - 0: RPN MSB
	 *     - 1: RPN LSB
	 *     - 2: NRPN MSB
	 *     - 3: NRPN LSB
	 *     - 4: 1=RPN, 0=NRPN, -1=unknown
	 */
	private static TreeMap<Byte, Byte[]> channelParamConfig = null;
	
	/**
	 * History of {@link #channelParamConfig} by tick.
	 * 
	 * - channel
	 * - tick
	 * - index
	 *     - 0: RPN MSB
	 *     - 1: RPN LSB
	 *     - 2: NRPN MSB
	 *     - 3: NRPN LSB
	 *     - 4: 1=RPN, 0=NRPN, -1=unknown
	 */
	private static TreeMap<Byte, TreeMap<Long, Byte[]>> channelParamHistory = null;
	
	/**                  channel  --  controller  --  tick -- value */
	private static TreeMap<Byte, TreeMap<Byte, TreeMap<Long, Byte>>> controllerHistory = null;
	
	/**
	 * RPN history by tick.
	 * 
	 * - channel
	 * - RPN: (MSB*128+LSB)
	 * - tick
	 * - value: (MSB*128+LSB)
	 */
	private static TreeMap<Byte, TreeMap<Integer, TreeMap<Long, Integer>>> rpnHistory = null;
	
	/**
	 * This class is only used statically so a public constructor is not needed.
	 */
	private SequenceAnalyzer() {
	}
	
	/**
	 * Resets all information that can be queried in public getter methods.
	 * This is called before a MIDI or MidicaPL file is parsed so that the
	 * getters don't return the information of a former parsing after a later
	 * parsing failed.
	 */
	public static void reset() {
		sequenceInfo = null;
		noteHistory  = null;
	}
	
	/**
	 * Analyzes the given MIDI sequence and collects information about it.
	 * Adds marker events for channel activity changes.
	 * 
	 * @param seq      The MIDI sequence to be analyzed.
	 * @param charset  The charset that has been chosen in the file chooser.
	 * @throws ParseException if something went wrong.
	 */
	public static void analyze(Sequence seq, String charset) throws ParseException {
		sequence      = seq;
		chosenCharset = charset;
		
		try {
			// initialize data structures
			init();
			KaraokeAnalyzer.init(sequence.getResolution(), charset, markerTicks);
			
			// fill data structures
			parse();
		}
		catch (Exception e) {
			if (e instanceof ParseException) {
				throw (ParseException) e;
			}
			else {
				e.printStackTrace();
				throw new ParseException( e.getMessage() );
			}
		}
		
		// add statistic information to the data structures
		postprocess();
	}
	
	/**
	 * Returns the information that have been collected while
	 * analyzing the MIDI sequence.
	 * 
	 * If no MIDI sequence has been loaded: returns an empty data structure.
	 * 
	 * @return MIDI sequence info.
	 */
	public static HashMap<String, Object> getSequenceInfo() {
		if (null == sequenceInfo) {
			return new HashMap<>();
		}
		
		return sequenceInfo;
	}
	
	/**
	 * Returns internal data structures from the parsed sequence.
	 * This is needed for the {@link MidicaPLExporter}.
	 * 
	 * The following structures are returned:
	 * 
	 * - **instrument_history**: TreeMap<Byte, TreeMap<Long, Byte[]>>
	 *     - channel
	 *     - tick
	 *     - 0=bankMSB, 1=bankLSB, 3=program
	 * - **comment_history**: TreeMap<Byte, TreeMap<Long, String>>
	 *     - channel
	 *     - tick
	 *     - comment
	 * - **note_history**: TreeMap<Byte, TreeMap<Long, TreeMap<Byte, Byte>>>
	 *     - channel
	 *     - tick
	 *     - note
	 *     - velocity
	 * - **note_on_off**: TreeMap<Byte, TreeMap<Byte, TreeMap<Long, Boolean>>>
	 *     - channel
	 *     - note
	 *     - tick
	 *     - on/off
	 * @return internal data structures as described above.
	 */
	public static HashMap<String, Object> getHistories() {
		HashMap<String, Object> histories = new HashMap<>();
		histories.put( "instrument_history", instrumentHistory  );
		histories.put( "comment_history",    commentHistory     );
		histories.put( "note_history",       noteHistory        );
		histories.put( "note_on_off",        noteOnOffByChannel );
		
		return histories;
	}
	
	/**
	 * Initializes the internal data structures so that they are ready to
	 * be filled with sequence information during the parsing process.
	 * @throws ReflectiveOperationException if the root node of the message
	 *         tree cannot be created.
	 */
	private static void init() throws ReflectiveOperationException {
		
		// initialize data structures for the sequence info
		sequenceInfo   = new HashMap<>();
		int resolution = sequence.getResolution();
		sequenceInfo.put( "resolution",    resolution               );
		sequenceInfo.put( "meta_info",     new HashMap<>()          );
		sequenceInfo.put( "used_channels", new TreeSet<>()          );
		sequenceInfo.put( "tempo_mpq",     new TreeMap<>()          );
		sequenceInfo.put( "tempo_bpm",     new TreeMap<>()          );
		sequenceInfo.put( "ticks",         sequence.getTickLength() );
		banksAndInstrTotal      = new MidicaTreeModel( Dict.get(Dict.TOTAL)        );
		banksAndInstrPerChannel = new MidicaTreeModel( Dict.get(Dict.PER_CHANNEL)  );
		msgTreeModel            = new MidicaTreeModel( Dict.get(Dict.TAB_MESSAGES), MessageTreeNode.class );
		messages                = new ArrayList<>();
		sequenceInfo.put( "banks_total",         banksAndInstrTotal      );
		sequenceInfo.put( "banks_per_channel",   banksAndInstrPerChannel );
		sequenceInfo.put( "msg_tree_model",      msgTreeModel            );
		sequenceInfo.put( "messages",            messages                );
		long   microseconds = sequence.getMicrosecondLength();
		String time         = MidiDevices.microsecondsToTimeString( microseconds );
		sequenceInfo.put( "time_length", time );
		channelParamConfig = new TreeMap<>();
		for (byte channel = 0; channel < 16; channel++) {
			// default (N)RPN config: MSB=LSB=127 (no parameter set), -1: neither RPN nor NRPN is active
			Byte[] conf = { 127, 127, 127, 127, -1 };
			channelParamConfig.put(channel, conf);
		}
		channelParamHistory = new TreeMap<>();
		for (byte channel = 0; channel < 16; channel++) {
			TreeMap<Long, Byte[]> paramHistory = new TreeMap<>();
			channelParamHistory.put( channel, paramHistory );
			
			// default config (same as the default in channelParamConfig)
			Byte[] conf0 = { 127, 127, 127, 127, -1 }; // MSB=LSB=127 (no parameter set), -1: neither RPN nor NRPN is active
			paramHistory.put( 0L, conf0 );
		}
		controllerHistory = new TreeMap<>();
		for (byte channel = 0; channel < 16; channel++) {
			resetAllControllers(channel, 0L);
		}
		rpnHistory = new TreeMap<>();
		for (byte channel = 0; channel < 16; channel++) {
			resetAllRPNs(channel, 0L);
		}
		
		// init data structures for the channel activity
		activityByChannel  = new TreeMap<>();
		noteOnOffByChannel = new TreeMap<>();
		markerTicks        = new TreeSet<>();
		markers            = new TreeMap<>();
		
		// init data structures for the note history
		noteHistory = new TreeMap<>();
		for (byte channel = 0; channel < 16; channel++) {
			noteHistory.put( channel, new TreeMap<>() );
		}
		
		instrumentHistory = new TreeMap<>();
		for (byte channel = 0; channel < 16; channel++) {
			TreeMap<Long, Byte[]> channelHistory = new TreeMap<>();
			instrumentHistory.put( channel, channelHistory );
			
			// default config
			Byte[] conf0 = { 0, 0, 0 }; // default values: bankMSB=0, bankLSB=0, program=0
			channelHistory.put( DEFAULT_CHANNEL_CONFIG_TICK, conf0 ); // this must be configured before the sequence starts
		}
		commentHistory = new TreeMap<>();
		for (byte channel = 0; channel < 16; channel++) {
			TreeMap<Long, String> channelCommentHistory = new TreeMap<>();
			commentHistory.put( channel, channelCommentHistory );
		}
	}
	
	/**
	 * Parses the MIDI sequence track by track and event by event and
	 * collects information.
	 * @throws ReflectiveOperationException if a tree node cannot be created.
	 */
	private static void parse() throws ReflectiveOperationException {
		
		// Analyze for channel activity, note history, banks, instruments, controllers and (N)RPN,
		// and channel names (instrument names).
		// Therefore the CREATED sequence is used.
		// In this sequence tracks match channels. So we know that we will
		// process the note-related events in the right order.
		int trackNum = 0;
		for (Track t : SequenceCreator.getSequence().getTracks()) {
			for (int i=0; i < t.size(); i++) {
				MidiEvent   event = t.get(i);
				long        tick  = event.getTick();
				MidiMessage msg   = event.getMessage();
				
				if (msg instanceof ShortMessage) {
					processShortMessageByChannel((ShortMessage) msg, tick);
				}
				else if (msg instanceof MetaMessage) {
					processMetaMessageByChannel((MetaMessage) msg, tick, trackNum);
				}
			}
			trackNum++;
		}
		
		// Analyze for general statistics.
		// Therefore the ORIGINAL sequence is used.
		KaraokeAnalyzer.resetFileCharset();
		trackNum = 0;
		for (Track t : sequence.getTracks()) {
			int msgNum = 0;
			for (int i=0; i < t.size(); i++) {
				MidiEvent   event = t.get(i);
				long        tick  = event.getTick();
				MidiMessage msg   = event.getMessage();
				
				if (msg instanceof MetaMessage) {
					processMetaMessage((MetaMessage) msg, tick, trackNum, msgNum);
				}
				else if (msg instanceof ShortMessage) {
					MessageClassifier.processShortMessage(
						(ShortMessage) msg, tick, trackNum, msgNum,
						messages,     // add details and leaf node to messages
						msgTreeModel  // add leaf node
					);
				}
				else if (msg instanceof SysexMessage) {
					MessageClassifier.processSysexMessage(
						(SysexMessage) msg, tick, trackNum, msgNum,
						messages,     // add details and leaf node to messages
						msgTreeModel  // add leaf node
					);
				}
				else {
				}
				msgNum++;
			}
			trackNum++;
		}
		sequenceInfo.put( "num_tracks", trackNum );
		
		// decide which track number and META type to use for the lyrics
		KaraokeAnalyzer.chooseLyricsTypeAndTrack();
		
		// collect syllables for lyrics using the ORIGINAL sequence
		KaraokeAnalyzer.resetFileCharset();
		trackNum = 0;
		for (Track t : sequence.getTracks()) {
			for (int i=0; i < t.size(); i++) {
				MidiEvent   event = t.get(i);
				long        tick  = event.getTick();
				MidiMessage msg   = event.getMessage();
				
				if (msg instanceof MetaMessage) {
					MetaMessage metaMsg = (MetaMessage) msg;
					int type    = metaMsg.getType();
					byte[] data = metaMsg.getData();
					if (MidiListener.META_LYRICS == type) {
						KaraokeAnalyzer.addEvent(KaraokeAnalyzer.KARAOKE_LYRICS, trackNum, tick, data);
					}
					else if (MidiListener.META_TEXT == type) {
						KaraokeAnalyzer.addEvent(KaraokeAnalyzer.KARAOKE_TEXT, trackNum, tick, data);
					}
				}
			}
			trackNum++;
		}
	}
	
	/**
	 * Retrieves some channel-specific information from short messages that can affect following messages.
	 * 
	 * The regarded messages are:
	 * 
	 * - Bank Select
	 * - Program Change
	 * - Note On/Off
	 * - RPN
	 * - NRPN
	 * 
	 * Stores these information in order to lookup their values later, when other
	 * (affected) messages are found.
	 * 
	 * Fills the trees for the info view tab "Banks, Instruments, Notes".
	 * 
	 * Does **not** fill the tree in the tab "MIDI Messages".
	 * 
	 * @param msg   Short message
	 * @param tick  Tickstamp
	 * @throws ReflectiveOperationException if the note-on event cannot be
	 *         added to one of the tree models.
	 */
	private static void processShortMessageByChannel(ShortMessage msg, long tick) throws ReflectiveOperationException {
		int  cmd     = msg.getCommand();
		byte channel = (byte) msg.getChannel();
		
		// Program Change?
		if (ShortMessage.PROGRAM_CHANGE == cmd) {
			
			// get current config
			Entry<Long, Byte> bankMsbEntry = controllerHistory.get(channel).get( (byte) 0x00 ).floorEntry(tick);
			Entry<Long, Byte> bankLsbEntry = controllerHistory.get(channel).get( (byte) 0x20 ).floorEntry(tick);
			byte bankMsb       = bankMsbEntry.getValue();
			byte bankLsb       = bankLsbEntry.getValue();
			byte programNumber = (byte) msg.getData1();
			Byte[] currentConf = { bankMsb, bankLsb, programNumber };
			
			// add to history
			instrumentHistory.get(channel).put(tick, currentConf);
			
			// prepare marker event
			markerTicks.add(tick);
			
			return;
		}
		
		// CONTROL CHANGE
		if (ShortMessage.CONTROL_CHANGE == cmd) {
			byte controller = (byte) msg.getData1();
			byte value      = (byte) msg.getData2();
			
			//    0-31: high resolution MSB
			//   32-63: high resolution LSB
			//   64-69: single byte (switches)
			//   70-95: single byte (low resolution)
			//   96-97: data increment/decrement
			//   98-99: NRPN LSB/MSB
			// 100-101: RPN LSB/MSB
			// 102-119: single byte (undefined)
			// 120-127: mode
			controllerHistory.get(channel).get(controller).put(tick, value);
			
			// (N)RPN MSB/LSB
			if (controller >= 0x62 && controller <= 0x65) {
				
				// adjust current channel param config
				int index = 0x62 == controller ? 3  // NRPN LSB
				          : 0x63 == controller ? 2  // NRPN MSB
				          : 0x64 == controller ? 1  // RPN LSB
				          : 0x65 == controller ? 0  // RPN MSB
				          : -1;
				if (-1 == index) { // should never happen
					return;
				}
				channelParamConfig.get(channel)[index] = value;
				
				// calculate and adjust active parameter type: RPN(1), NRPN(0) or none(-1)?
				byte rpnNrpnReset;
				byte currentMsb;
				byte currentLsb;
				if (controller >= 0x64) {
					rpnNrpnReset = 1;  // RPN
					currentMsb = channelParamConfig.get(channel)[ 0 ];
					currentLsb = channelParamConfig.get(channel)[ 1 ];
				}
				else {
					rpnNrpnReset = 0;  // NRPN
					currentMsb = channelParamConfig.get(channel)[ 2 ];
					currentLsb = channelParamConfig.get(channel)[ 3 ];
				}
				if (((byte) 0x7F) == currentMsb && ((byte) 0x7F) == currentLsb) {
					// reset
					rpnNrpnReset = -1;
				}
				channelParamConfig.get(channel)[ 4 ] = rpnNrpnReset;
				
				// add history entry
				Byte[] confAtTick = channelParamConfig.get(channel).clone();
				channelParamHistory.get(channel).put(tick, confAtTick);
			}
			
			// Data Entry/Increment/Decrement
			else if ( 0x06 == controller       // data entry MSB
			       || 0x26 == controller       // data entry LSB
			       || 0x60 == controller       // data button increment
			       || 0x61 == controller ) {   // data button decrement
				
				// find out what parameter has to be changed
				Byte[] paramMsbLsb = getChannelParamMsbLsbType(channel, tick);
				byte msb  = paramMsbLsb[ 0 ];
				byte lsb  = paramMsbLsb[ 1 ];
				byte type = paramMsbLsb[ 2 ];
				
				// change RPN
				if (1 == type) {
					
					// get current value
					int rpn = (128 * (int) msb) + lsb;
					TreeMap<Long, Integer> paramValueHistory = rpnHistory.get(channel).get(rpn);
					if (null == paramValueHistory) {
						
						// not yet set - get the default
						byte[] defaultMsbLsb = getRpnDefault(msb, lsb);
						int    defaultValue  = (defaultMsbLsb[0] << 8) + defaultMsbLsb[1];
						paramValueHistory = new TreeMap<>();
						paramValueHistory.put(0L, defaultValue);
						rpnHistory.get(channel).put(rpn, paramValueHistory);
					}
					Entry<Long, Integer> paramValueEntry = paramValueHistory.floorEntry(tick);
					int  currentValue = paramValueEntry.getValue();
					byte msbVal       = (byte) (currentValue >> 8 & 0xFF);
					byte lsbVal       = (byte) (currentValue      & 0xFF);
					
					// change MSB or LSB or both
					if (0x06 == controller)         // data entry MSB
						msbVal = value;
					else if (0x26 == controller)    // data entry LSB
						lsbVal = value;
					else if (0x60 == controller) {  // data button increment
						if (lsbVal < 0x7F) {
							lsbVal++;
						}
						else if (msbVal < 0x7F) {
							lsbVal = 0;
							msbVal++;
						}
					}
					else if (0x61 == controller) {  // data button decrement
						if (lsbVal > 0x00) {
							lsbVal--;
						}
						else if (msbVal > 0x00) {
							lsbVal = 0x7F;
							msbVal--;
						}
					}
					
					// calculate and set the new value
					int newValue = (msbVal << 8) + lsbVal;
					rpnHistory.get(channel).get(rpn).put(tick, newValue);
				}
			}
		}
		
		// NOTE ON or OFF
		else if (ShortMessage.NOTE_ON == cmd) {
			byte note     = (byte) msg.getData1();
			byte velocity = (byte) msg.getData2();
			
			// on or off?
			if (velocity > 0) {
				addNoteOn(tick, channel, note, velocity);
			}
			else {
				addNoteOff(tick, channel, note);
			}
		}
		
		// NOTE OFF
		else if (ShortMessage.NOTE_OFF == cmd) {
			byte note = (byte) msg.getData1();
			addNoteOff(tick, channel, note);
		}
	}
	
	/**
	 * Retrieves general information from meta messages.
	 * 
	 * @param msg       Meta message
	 * @param tick      Tickstamp
	 * @param trackNum  Track number (beginning with 0).
	 * @param msgNum    Number of the message inside the track.
	 * @throws ReflectiveOperationException if the message cannot be added to
	 *         the tree model.
	 */
	private static void processMetaMessage(MetaMessage msg, long tick, int trackNum, int msgNum) throws ReflectiveOperationException {
		
		// prepare data structures for the message tree
		int    type    = msg.getType();
		byte[] content = msg.getData();
		
		// TEMPO
		if (MidiListener.META_SET_TEMPO == type) {
			int mpq = MidiUtils.getTempoMPQ(msg);
			int bpm = (int) MidiUtils.convertTempo(mpq);
			TreeMap<Long, Integer> tempoMpq = (TreeMap<Long, Integer>) sequenceInfo.get("tempo_mpq");
			tempoMpq.put(tick, mpq);
			TreeMap<Long, Integer> tempoBpm = (TreeMap<Long, Integer>) sequenceInfo.get("tempo_bpm");
			tempoBpm.put(tick, bpm);
		}
		
		// TEXT
		else if (MidiListener.META_TEXT == type) {
			KaraokeAnalyzer.countEventAndGetSongInfo(KaraokeAnalyzer.KARAOKE_TEXT, trackNum, content);
		}
		
		// LYRICS
		else if (MidiListener.META_LYRICS == type) {
			KaraokeAnalyzer.countEventAndGetSongInfo(KaraokeAnalyzer.KARAOKE_LYRICS, trackNum, content);
		}
		
		// COPYRIGHT
		else if (MidiListener.META_COPYRIGHT == type) {
			String                  copyright = CharsetUtils.getTextFromBytes(content, chosenCharset, KaraokeAnalyzer.getFileCharset());
			HashMap<String, String> metaInfo  = (HashMap<String, String>) sequenceInfo.get("meta_info");
			metaInfo.put( "copyright", copyright );
		}
		
		// fetch tree and detailed message information
		MessageClassifier.processMetaMessage(
			(MetaMessage) msg, tick, trackNum, msgNum,
			messages,                        // add details and leaf node to messages
			msgTreeModel,                    // add leaf node
			chosenCharset,                   // charset from the file chooser
			KaraokeAnalyzer.getFileCharset() // charset from last charset switch in a meta message
		);
	}
	
	/**
	 * Retrieves instrument specific information from meta messages.
	 * 
	 * @param msg       Meta message.
	 * @param tick      Tickstamp.
	 * @param trackNum  Track number of the created string.
	 */
	private static void processMetaMessageByChannel(MetaMessage msg, long tick, int trackNum) {
		int    type = msg.getType();
		byte[] data = msg.getData();
		String text = null;
		
		// INSTRUMENT NAME
		if (MidiListener.META_INSTRUMENT_NAME == type) {
			
			// get channel number
			byte channel = (byte) (trackNum - SequenceCreator.NUM_META_TRACKS);
			
			// invalid channel - not produced by Midica?
			if (channel < 0 || channel >= MidiDevices.NUMBER_OF_CHANNELS) {
				return;
			}
			
			// remember the channel comment
			text = CharsetUtils.getTextFromBytes(data, chosenCharset, KaraokeAnalyzer.getFileCharset());
			commentHistory.get(channel).put(tick, text);
		}
	}
	
	/**
	 * Adds a detected **note-on** event to the data structures.
	 * 
	 * - tracks note events for the note history
	 * - tracks the channel activity
	 * - prepares markers
	 * - builds up the tree model for the bank/instrument/note trees
	 * 
	 * @param tick      The tickstamp when this event occurred.
	 * @param channel   The MIDI channel number.
	 * @param note      The note number.
	 * @param velocity  The note's velocity.
	 * @throws ReflectiveOperationException if the note cannot be added to
	 *         one of the tree models.
	 */
	private static void addNoteOn(long tick, byte channel, byte note, byte velocity) throws ReflectiveOperationException {
		
		// note on/off tracking
		TreeMap<Byte, TreeMap<Long, Boolean>> noteTickOnOff = noteOnOffByChannel.get( channel );
		if (null == noteTickOnOff) {
			noteTickOnOff = new TreeMap<>();
			noteOnOffByChannel.put( channel, noteTickOnOff );
		}
		TreeMap<Long, Boolean> pressedAtTick = noteTickOnOff.get( note );
		if (null == pressedAtTick) {
			pressedAtTick = new TreeMap<>();
			noteTickOnOff.put( note, pressedAtTick );
		}
		boolean wasPressedBefore = pressedAtTick.containsKey( tick );
		if (wasPressedBefore)
			// Key press and/or release conflict.
			// There was a(nother) ON or OFF event for the same key in the
			// same tick. It's probably depending on the sequencer or
			// synthesizer implementation what happens.
			// Here we just assume that these events will be processed in
			// the same order as we found them.
			wasPressedBefore = pressedAtTick.get( tick );
		if (wasPressedBefore)
			return;
		pressedAtTick.put( tick, true );
		
		// get last channel activity
		int lastChannelActivity = 0;
		TreeMap<Long, Integer> activityAtTick = activityByChannel.get( channel );
		if (null == activityAtTick) {
			activityAtTick = new TreeMap<>();
			activityByChannel.put( channel, activityAtTick );
		}
		else {
			Entry<Long, Integer> lastActivity = activityAtTick.floorEntry( tick );
			if (lastActivity != null) {
				lastChannelActivity = lastActivity.getValue();
			}
		}
		activityAtTick.put( tick, lastChannelActivity + 1 );
		
		// note history by channel
		TreeMap<Long, TreeMap<Byte, Byte>> noteHistoryForChannel = noteHistory.get( channel );
		TreeMap<Byte, Byte> noteHistoryAtTick = noteHistoryForChannel.get( tick );
		if (null == noteHistoryAtTick) {
			noteHistoryAtTick = new TreeMap<>();
			noteHistoryForChannel.put( tick, noteHistoryAtTick );
		}
		noteHistoryAtTick.put( note, velocity );
		
		// prepare marker event
		markerTicks.add( tick );
		
		
		// bank/instrument/note info for the tree
		String channelTxt = Dict.get(Dict.CHANNEL) + " " + channel;
		String channelID  = String.format( "%02X", channel );
		Entry<Long, Byte[]> instrEntry = instrumentHistory.get(channel).floorEntry(tick);
		Byte[] config     = instrEntry.getValue();
		int    bankNum    = (config[0] << 7) | config[1]; // bankMSB * 2^7 + bankLSB
		String bankSyntax = config[0] + ""; // MSB as a string
		if (config[1] > 0) {  // MSB/LSB
			bankSyntax    = bankSyntax + Dict.getSyntax( Dict.SYNTAX_PROG_BANK_SEP ) + config[ 1 ];
		}
		String bankTxt    = Dict.get(Dict.BANK)             + " "  + bankNum     + ", "
		                  + Dict.get(Dict.TOOLTIP_BANK_MSB) + ": " + config[ 0 ] + ", "
				          + Dict.get(Dict.TOOLTIP_BANK_LSB) + ": " + config[ 1 ];
		String bankID     = String.format( "%02X%02X", config[0], config[1] );
		String programStr = config[ 2 ] + "";
		String programID  = String.format( "%02X", config[2] );
		String instrTxt   = 9 == channel ? Dict.getDrumkit( config[2] ) : Dict.getInstrument( config[2] );
		String noteStr    = note + "";
		String noteTxt    = 9 == channel ? Dict.getPercussionLongId( note ) : Dict.getNote( note );
		String noteID     = String.format( "%02X", note );
		if (9 == channel) {
			noteID = "Z" + noteID; // give percussion notes have a different (higher) ID
		}
		
		// per channel           id         name        number
		String[] channelOpts = { channelID, channelTxt, null       };
		String[] bankOpts    = { bankID,    bankTxt,    bankSyntax };
		String[] programOpts = { programID, instrTxt,   programStr };
		String[] noteOpts    = { noteID,    noteTxt,    noteStr    };
		ArrayList<String[]> perChannel = new ArrayList<>();
		perChannel.add( channelOpts );
		perChannel.add( bankOpts    );
		perChannel.add( programOpts );
		perChannel.add( noteOpts    );
		banksAndInstrPerChannel.add(perChannel, null);
		
		// total
		ArrayList<String[]> total = new ArrayList<>();
		total.add( bankOpts    );
		total.add( programOpts );
		total.add( noteOpts    );
		banksAndInstrTotal.add(total, null);
	}
	
	/**
	 * Adds a detected **note-off** event to the data structures.
	 * 
	 * - tracks note events for the note history
	 * - tracks the channel activity
	 * - prepares markers
	 * 
	 * @param tick     The tickstamp when this event occurred.
	 * @param channel  The MIDI channel number.
	 * @param note     The note number.
	 */
	private static void addNoteOff(long tick, byte channel, byte note) {
		
		// check if the released key has been pressed before
		TreeMap<Byte, TreeMap<Long, Boolean>> noteTickOnOff = noteOnOffByChannel.get( channel );
		if (null == noteTickOnOff) {
			noteTickOnOff = new TreeMap<>();
			noteOnOffByChannel.put( channel, noteTickOnOff );
		}
		TreeMap<Long, Boolean> pressedAtTick = noteTickOnOff.get( note );
		if (null == pressedAtTick) {
			pressedAtTick = new TreeMap<>();
			noteTickOnOff.put( note, pressedAtTick );
		}
		boolean wasPressedBefore = false;
		Entry<Long, Boolean> wasPressed = pressedAtTick.floorEntry( tick );
		if (null != wasPressed) {
			wasPressedBefore = wasPressed.getValue();
		}
		if (!wasPressedBefore) {
			return;
		}
		
		// mark as released
		pressedAtTick.put( tick, false );
		
		// channel activity
		TreeMap<Long, Integer> activityAtTick = activityByChannel.get( channel );
		if (null == activityAtTick) {
			activityAtTick = new TreeMap<>();
			activityByChannel.put( channel, activityAtTick );
		}
		Entry<Long, Integer> lastActivity = activityAtTick.floorEntry( tick );
		if (null == lastActivity) {
			// A key was released before it has been pressed for the very first time.
			return;
		}
		
		// decrement activity
		Integer lastActivityCount = lastActivity.getValue();
		if (lastActivityCount < 1) {
			// should never happen
			return;
		}
		activityAtTick.put( tick, lastActivityCount - 1 );
		
		// prepare marker event
		markerTicks.add( tick );
	}
	
	/**
	 * Adds last information to the info data structure about the MIDI sequence.
	 * Adds marker events to the sequence.
	 * 
	 * @throws ParseException if the marker events cannot be added to the MIDI sequence.
	 */
	private static void postprocess() throws ParseException {
		
		// sort messages for the message table
		Collections.sort(messages);
		
		// average, min and max tempo
		TreeMap<Long, Integer> tempoMpq = (TreeMap<Long, Integer>) sequenceInfo.get("tempo_mpq");
		TreeMap<Long, Integer> tempoBpm = (TreeMap<Long, Integer>) sequenceInfo.get("tempo_bpm");
		long lastTick   = 0;
		int  lastBpm    = MidiDevices.DEFAULT_TEMPO_BPM;
		int  lastMpq    = MidiDevices.DEFAULT_TEMPO_MPQ;
		int  minBpm     = 0;
		int  maxBpm     = 0;
		int  minMpq     = 0;
		int  maxMpq     = 0;
		long bpmProduct = 0;
		long mpqProduct = 0;
		for (long tick : tempoBpm.keySet()) {
			
			// average
			int  newBpm   = tempoBpm.get( tick );
			int  newMpq   = tempoMpq.get( tick );
			long tickDiff = tick - lastTick;
			bpmProduct   += tickDiff * lastBpm;
			mpqProduct   += tickDiff * lastMpq;
			lastBpm       = newBpm;
			lastMpq       = newMpq;
			lastTick      = tick;
			
			// min, max
			if (tick > 0 && 0 == minBpm && 0 == maxBpm) {
				minBpm = MidiDevices.DEFAULT_TEMPO_BPM;
				maxBpm = MidiDevices.DEFAULT_TEMPO_BPM;
				minMpq = MidiDevices.DEFAULT_TEMPO_MPQ;
				maxMpq = MidiDevices.DEFAULT_TEMPO_MPQ;
			}
			if (0 == minBpm || minBpm > newBpm) {
				minBpm = newBpm;
			}
			if (0 == maxBpm || maxBpm < newBpm) {
				maxBpm = newBpm;
			}
			if (0 == minMpq || minMpq > newMpq) {
				minMpq = newMpq;
			}
			if (0 == maxMpq || maxMpq < newMpq) {
				maxMpq = newMpq;
			}
		}
		long tickLength = (Long) sequenceInfo.get( "ticks" );
		long tickDiff = tickLength - lastTick;
		bpmProduct   += tickDiff * lastBpm;
		mpqProduct   += tickDiff * lastMpq;
		if (0 == minBpm ) {
			minBpm = lastBpm;
		}
		if (0 == maxBpm ) {
			maxBpm = lastBpm;
		}
		if (0 == minMpq ) {
			minMpq = lastMpq;
		}
		if (0 == maxMpq ) {
			maxMpq = lastMpq;
		}
		double avgBpm = (double) bpmProduct / tickLength;
		double avgMpq = (double) mpqProduct / tickLength;
		sequenceInfo.put( "tempo_bpm_avg", String.format("%.2f", avgBpm) );
		sequenceInfo.put( "tempo_bpm_min", Integer.toString(minBpm) );
		sequenceInfo.put( "tempo_bpm_max", Integer.toString(maxBpm) );
		sequenceInfo.put( "tempo_mpq_avg", String.format("%.1f", avgMpq) );
		sequenceInfo.put( "tempo_mpq_min", Integer.toString(minMpq) );
		sequenceInfo.put( "tempo_mpq_max", Integer.toString(maxMpq) );
		
		// reset default channel config for unused channels (to avoid confusion in the player UI)
		for (byte channel = 0; channel < 16; channel++) {
			
			// channel unused?
			TreeMap<Long, TreeMap<Byte, Byte>> channelNoteHistory = noteHistory.get(channel);
			if (channelNoteHistory.isEmpty()) {
				TreeMap<Long, Byte[]> channelInstrumentHistory = instrumentHistory.get(channel);
				
				Byte[] conf0 = { -1, -1, -1 };
				channelInstrumentHistory.put(-1L, conf0);
			}
		}
		
		// Decide which channel to use for the channel part of the lyrics marker bytes.
		Set<Byte> activeChannels = activityByChannel.keySet();
		byte      lyricsChannel  = -1; // channel part for the lyrics marker events
		if (KaraokeAnalyzer.getLyrics().size() > 0) {
			Iterator<Byte> it = activeChannels.iterator();
			if (it.hasNext()) {
				// use one of the active channels for the lyrics
				lyricsChannel = it.next();
			}
			else {
				// No channel activity at all, only lyrics.
				// Use channel 0 for lyrics.
				lyricsChannel = 0;
				activityByChannel.put(lyricsChannel, new TreeMap<>());
			}
		}
		
		// markers
		for (long tick : markerTicks) {
			
			// initiate structures for that tick
			TreeSet<Byte> channelsAtTick = new TreeSet<>();
			boolean       mustAddMarker  = false;
			
			// walk through all channels that have any activity IN ANY TICK (or lyrics)
			for (byte channel : activeChannels) {
				
				boolean lyricsChanged     = false;
				boolean activityChanged   = false;
				boolean historyChanged    = false;
				boolean instrumentChanged = false;
				
				// is there a lyrics event at the current tick?
				if (lyricsChannel == channel && KaraokeAnalyzer.getLyricsEventTicks().contains(tick)) {
					lyricsChanged = true;
				}
				
				// is there an instrument change at the current tick?
				Byte[] instrChange = instrumentHistory.get(channel).get(tick);
				if (instrChange != null) {
					instrumentChanged = true;
				}
				
				// is there any channel activity at the current tick?
				if (activityByChannel.get(channel).containsKey(tick)) {
					activityChanged = true;
					
					// is at least one of the channel events a NOTE-ON?
					TreeMap<Byte, TreeMap<Long, Boolean>>    noteTickOnOff    = noteOnOffByChannel.get( channel );
					Set<Entry<Byte, TreeMap<Long, Boolean>>> noteTickOnOffSet = noteTickOnOff.entrySet();
					for (Entry<Byte, TreeMap<Long, Boolean>> noteTickOnOffEntry : noteTickOnOffSet) {
						TreeMap<Long, Boolean> tickOnOff = noteTickOnOffEntry.getValue();
						if (null == tickOnOff) {
							continue;
						}
						Boolean onOff = tickOnOff.get( tick );
						if (null == onOff) {
							continue;
						}
						if (onOff) {
							historyChanged = true;
							break;
						}
					}
				}
				
				// apply bitmasks to the channel byte
				if (lyricsChanged)
					channel |= MidiListener.MARKER_BITMASK_LYRICS;
				if (activityChanged)
					channel |= MidiListener.MARKER_BITMASK_ACTIVITY;
				if (historyChanged)
					channel |= MidiListener.MARKER_BITMASK_HISTORY;
				if (instrumentChanged)
					channel |= MidiListener.MARKER_BITMASK_INSTRUMENT;
				
				// add the channel to the marker
				if (lyricsChanged || activityChanged || historyChanged || instrumentChanged) {
					channelsAtTick.add( channel );
					mustAddMarker = true;
				}
			}
			
			// add the marker
			if (mustAddMarker) {
				markers.put( tick, channelsAtTick );
			}
		}
		try {
			SequenceCreator.addMarkers( markers );
		}
		catch (InvalidMidiDataException e) {
			throw new ParseException( Dict.get(Dict.ERROR_ANALYZE_POSTPROCESS) + e.getMessage() );
		}
		
		// postprocess the lyrics for karaoke
		KaraokeAnalyzer.postprocess();
	}
	
	/**
	 * Calculates the channel activity for the given channel at the given tick.
	 * 
	 * @param channel  MIDI channel
	 * @param tick     tickstamp of the sequence
	 * @return   the channel activity
	 */
	public static boolean getChannelActivity(byte channel, long tick) {
		
		// get ticks of this channel
		TreeMap<Long, Integer> ticksInChannel = activityByChannel.get( channel );
		if (null == ticksInChannel) {
			// channel not used at all
			return false;
		}
		
		// get the last activity
		Entry<Long, Integer> activityState = ticksInChannel.floorEntry( tick );
		if (null == activityState) {
			// nothing happened in the channel so far
			return false;
		}
		
		// inactive?
		if (0 == activityState.getValue()) {
			return false;
		}
		
		// active
		return true;
	}
	
	/**
	 * Calculates the note history for the given channel at the given tick.
	 * 
	 * The returned history consists of past and future notes, ordered by the tickstamp
	 * of their occurrence. Each entry contains the following parts:
	 * 
	 * - **index 0**: note number
	 * - **index 1**: velocity
	 * - **index 2**: tickstamp
	 * - **index 3**: past/future marker (**0** = presence or past, **1** = future)
	 * 
	 * @param channel  MIDI channel
	 * @param tick     tickstamp of the sequence
	 * @return   the note history
	 */
	public static ArrayList<Long[]> getNoteHistory(byte channel, long tick) {
		
		ArrayList<Long[]> result = new ArrayList<>();
		if (null == noteHistory) {
			return result;
		}
		TreeMap<Long, TreeMap<Byte, Byte>> channelHistory = noteHistory.get( channel );
		
		// get past notes
		long lastTick = tick;
		int i = 0;
		PAST:
		while (i < NOTE_HISTORY_BUFFER_SIZE_PAST) {
			
			// get all notes from the last tick
			Entry<Long, TreeMap<Byte, Byte>> notesAtTickEntry = channelHistory.floorEntry( lastTick );
			if (null == notesAtTickEntry) {
				break PAST;
			}
			lastTick = notesAtTickEntry.getKey();
			NavigableMap<Byte, Byte> notesAtTick = notesAtTickEntry.getValue().descendingMap(); // reverse order
			
			// each note at lastTick
			Set<Entry<Byte, Byte>> noteEntrySet = notesAtTick.entrySet();
			for (Entry<Byte, Byte> noteEntry : noteEntrySet) {
				byte note     = noteEntry.getKey();
				byte velocity = noteEntry.getValue();
				
				Long[] row = {
					(long) note,     // note number
					(long) velocity, // 0 - 127
					lastTick,        // tick
					0L,              // 0 = past; 1 = future
				};
				result.add( row );
				
				i++;
				if (i >= NOTE_HISTORY_BUFFER_SIZE_PAST) {
					break PAST;
				}
			}
			
			// go further into the past
			lastTick--;
		}
		
		// reverse the order of the past notes
		Collections.reverse( result );
		
		// get future notes
		long nextTick = tick + 1;
		i = 0;
		FUTURE:
		while (i < NOTE_HISTORY_BUFFER_SIZE_FUTURE) {
			
			// get all notes from the next tick
			Entry<Long, TreeMap<Byte, Byte>> notesAtTickEntry = channelHistory.ceilingEntry( nextTick );
			if (null == notesAtTickEntry) {
				break FUTURE;
			}
			nextTick = notesAtTickEntry.getKey();
			TreeMap<Byte, Byte> notesAtTick = notesAtTickEntry.getValue();
			
			// each note at nextTick
			Set<Entry<Byte, Byte>> noteEntrySet = notesAtTick.entrySet();
			for (Entry<Byte, Byte> noteEntry : noteEntrySet) {
				byte note     = noteEntry.getKey();
				byte velocity = noteEntry.getValue();
				
				Long[] row = {
					(long) note,     // note number
					(long) velocity, // 0 - 127
					nextTick,        // tick
					1L,              // 0 = past; 1 = future
				};
				result.add( row );
				
				i++;
				if (i >= NOTE_HISTORY_BUFFER_SIZE_FUTURE) {
					break FUTURE;
				}
			}
			
			// go further into the future
			nextTick++;
		}
		
		return result;
	}
	
	/**
	 * Calculates and returns bank and instrument information for the given channel at the given tick.
	 * 
	 * The returned value consists of 3 bytes with the following meaning:
	 * 
	 * - 1st byte: bank MSB
	 * - 2nd byte: bank LSB
	 * - 3rd byte: program number
	 * 
	 * @param channel  MIDI channel
	 * @param tick     tickstamp of the sequence
	 * @return         instrument description as described above
	 */
	public static Byte[] getInstrument(byte channel, long tick) {
		Entry<Long, Byte[]> entry = instrumentHistory.get(channel).floorEntry(tick);
		return entry.getValue();
	}
	
	/**
	 * Returns the channel comment for the given channel at the given tick.
	 * 
	 * @param channel  MIDI channel
	 * @param tick     tickstamp of the sequence
	 * @return         channel comment
	 */
	public static String getChannelComment(byte channel, long tick) {
		Entry<Long, String> entry = commentHistory.get( channel ).floorEntry( tick );
		
		if (null == entry) {
			return "";
		}
		
		return entry.getValue();
	}
	
	/**
	 * Calculates and returns RPN/NRPN info for the given channel at
	 * the given tick.
	 * Called from {@link MessageClassifier} while processing a
	 * data entry/increment/decrement message.
	 * 
	 * The returned value consists of 3 bytes with the following meaning:
	 * 
	 * - 1st byte: (N)RPN MSB
	 * - 2nd byte: (N)RPN LSB
	 * - 3rd byte: type (0=NRPN, 1=RPN, -1=none)
	 * 
	 * @param channel  MIDI channel
	 * @param tick     tickstamp of the sequence
	 * @return         MSB/LSB/type as described above
	 */
	public static Byte[] getChannelParamMsbLsbType(byte channel, long tick) {
		
		// get all params at the given tick
		Entry<Long, Byte[]> entry = channelParamHistory.get(channel).floorEntry(tick);
		Byte[] confAtTick = entry.getValue();
		
		// get current type (0=RPN, 1=NRPN or -1=none)
		byte type = confAtTick[ 4 ];
		byte msb  = 127; // none
		byte lsb  = 127; // none
		// get MSB and LSB
		if (1 == type) {
			// RPN
			msb = confAtTick[ 0 ];
			lsb = confAtTick[ 1 ];
		}
		else if (0 == type) {
			// NRPN
			msb = confAtTick[ 2 ];
			lsb = confAtTick[ 3 ];
		}
		
		// return the result
		Byte[] result = { msb, lsb, type };
		return result;
	}
	
	/**
	 * Returns the pitch bend sensitivity for the given channel at the given tick.
	 * 
	 * @param channel  MIDI channel
	 * @param tick     tickstamp of the sequence
	 * @return         pitch bend sensitivity
	 */
	public static final float getPitchBendSensitivity(byte channel, long tick) {
		Entry<Long, Integer> entry = rpnHistory.get(channel).get(0).floorEntry(tick);
		int  paramValue = entry.getValue();
		byte msb        = (byte) (paramValue >> 8 & 0xFF);
		byte lsb        = (byte) (paramValue      & 0xFF);
		
		return ((float) msb) + (((float) lsb) / 128f);
	}
	
	/**
	 * Analyzes the software version string from the MIDI sequence.
	 * Called from the {@link KaraokeAnalyzer}.
	 * 
	 * @param text    software version string.
	 */
	public static void retrieveSoftwareVersion(String text) {
		
		HashMap<String, String> metaInfo = (HashMap<String, String>) sequenceInfo.get( "meta_info" );
		metaInfo.put("software", text);
		
		// minor version?
		Pattern patternMV = Pattern.compile(".+\\.(\\d{10})$");
		Matcher matcherMV = patternMV.matcher(text);
		if (matcherMV.matches()) {
			int  minor     = Integer.parseInt(matcherMV.group(1));
			Date timestamp = new Date(minor * 1000L);
			SimpleDateFormat formatter    = new SimpleDateFormat( Dict.get(Dict.TIMESTAMP_FORMAT) );
			String           softwareDate = formatter.format(timestamp);
			metaInfo.put("software_date", softwareDate);
		}
	}
	
	/**
	 * Resets all controllers of the given channel to their default values.
	 * 
	 * @param channel  MIDI channel
	 * @param tick     tickstamp of the sequence
	 */
	private static final void resetAllControllers(byte channel, long tick) {
		
		TreeMap<Byte, TreeMap<Long, Byte>> channelCtrlHistory = new TreeMap<>();
		if (0L == tick) {
			// initialize ALL controllers by their default values
			for (int ctrl = 0; ctrl < 128; ctrl++) {
				TreeMap<Long, Byte> ctrlHistoryEntry = new TreeMap<>();
				byte defaultValue = getControllerDefault(ctrl);
				ctrlHistoryEntry.put(tick, defaultValue);
				channelCtrlHistory.put((byte) ctrl, ctrlHistoryEntry);
			}
		}
		else {
			// TODO: only reset the controllers mentioned in gm2, page 14
		}
		controllerHistory.put(channel, channelCtrlHistory);
		
		// TODO: reset modes:
		// omni on / poly (page 11)
		
		// TODO: mode messages:
		// 120: all sounds off
		// 121: reset all controllers
		// 122: local control
		// 123: all notes off
		// 124: omni off
		// 125: omni on
		// 126: mono on, poly off
		// 127: poly on, mono off
		
		// TODO: modes
		// mode 1: == omni on, poly
		// mode 2: == omni on, mono
		// mode 3: == omni off, poly
		// mode 4: == omni off, mono
		
		// TODO:
		// controllers 123-127: also imply 123 (all notes off) (page 52)
		
		// TODO: system defaul: General MIDI mode: on
		// see (page 332)
		
		// TODO: GM system on message:
		// implies: everything from "reset all controllers"
		// implies: volume (7) == 100
		// implies: expression (11) == 127
		// implies: pan (64) == 64
		// implies: bank-msb == 0
		// implies: bank-lsb == 0
		// implies: program-change == 0
		
		// TODO: all controller defaults should be 0 or 64 except:
		// #7 (volume)
		// #11 (expression)
		// see (page 323)
		
		// TODO: all sound off (#120)
		// 
		
		// TODO: MPE Config Message (MCM)
		// implies: reset all controllers
		// implies: stop all ongoing notes
		// see: MPE docu, page 5
		
	}
	
	/**
	 * Returns the default value for the given controller number.
	 * 
	 * @param ctrl Controller number
	 * @return default value.
	 */
	private static final byte getControllerDefault(int ctrl) {
		
		// 0-31 (0x00-0x1F): high resolution MSB
		if (0x00 == ctrl) return 0x00; // msb bank                     (0x00 == 0)  // TODO: adjust ???
		if (0x01 == ctrl) return 0x00; // msb modulation wheel         (0x00, see gm2, page 6)
		if (0x02 == ctrl) return 0x00; // msb breath ctrl              
		if (0x03 == ctrl) return 0x00; // msb (undefined)
		if (0x04 == ctrl) return 0x00; // msb foot ctrl                
		if (0x05 == ctrl) return 0x00; // msb portamento time          (0x00, see gm2, page 6)
		if (0x06 == ctrl) return 0x7F; // msb DATA ENTRY               (0x00 == 0, see gm2, page 12)
		if (0x07 == ctrl) return 0x64; // msb channel volume           (0x64 == 100)
		if (0x08 == ctrl) return 0x00; // msb balance                  (0x40 == 64, see MIDI 1.0, page 45)
		if (0x09 == ctrl) return 0x00; // msb (undefined)
		if (0x0A == ctrl) return 0x40; // msb pan                      (0x40 == 64, see gm2, page 8 and RP-036)
		if (0x0B == ctrl) return 0x7F; // msb expression               (0x7F == 127, see gm2, page 8)
		if (0x0C == ctrl) return 0x00; // msb effect ctrl 1
		if (0x0D == ctrl) return 0x00; // msb effect ctrl 2
		if (0x0E == ctrl) return 0x00; // msb (undefined)
		if (0x0F == ctrl) return 0x00; // msb (undefined)
		if (0x10 == ctrl) return 0x00; // msb general purpose ctrl 1
		if (0x11 == ctrl) return 0x00; // msb general purpose ctrl 2
		if (0x12 == ctrl) return 0x00; // msb general purpose ctrl 3
		if (0x13 == ctrl) return 0x00; // msb general purpose ctrl 4
		if (0x14 == ctrl) return 0x00; // msb (undefined)
		if (0x15 == ctrl) return 0x00; // msb (undefined)
		if (0x16 == ctrl) return 0x00; // msb (undefined)
		if (0x17 == ctrl) return 0x00; // msb (undefined)
		if (0x18 == ctrl) return 0x00; // msb (undefined)
		if (0x19 == ctrl) return 0x00; // msb (undefined)
		if (0x1A == ctrl) return 0x00; // msb (undefined)
		if (0x1B == ctrl) return 0x00; // msb (undefined)
		if (0x1C == ctrl) return 0x00; // msb (undefined)
		if (0x1D == ctrl) return 0x00; // msb (undefined)
		if (0x1E == ctrl) return 0x00; // msb (undefined)
		if (0x1F == ctrl) return 0x00; // msb (undefined)
		
		// 32-63 (0x20-0x3F): high resolution LSB
		if (0x20 == ctrl) return 0x00; // lsb bank (0x00 == 0)
		if (0x21 == ctrl) return 0x00; // lsb modulation wheel
		if (0x22 == ctrl) return 0x00; // lsb breath ctrl
		if (0x23 == ctrl) return 0x00; // lsb (undefined)
		if (0x24 == ctrl) return 0x00; // lsb foot ctrl
		if (0x25 == ctrl) return 0x00; // lsb portamento time
		if (0x26 == ctrl) return 0x7F; // lsb DATA ENTRY             (0x00 == 0, see gm2, page 12)
		if (0x27 == ctrl) return 0x00; // lsb channel volume         (0x00 == 0)
		if (0x28 == ctrl) return 0x00; // lsb balance                (0x00 == 0, not mentioned in MIDI 1.0 page 45 but 0x00 makes sense)
		if (0x29 == ctrl) return 0x00; // lsb (undefined)
		if (0x2A == ctrl) return 0x00; // lsb pan                    (0x00 == 0, not mentioned in RP-036 but 0x00 makes sense)
		if (0x2B == ctrl) return 0x00; // lsb expression
		if (0x2C == ctrl) return 0x00; // lsb effect ctrl 1
		if (0x2D == ctrl) return 0x00; // lsb effect ctrl 2
		if (0x2E == ctrl) return 0x00; // lsb (undefined)
		if (0x2F == ctrl) return 0x00; // lsb (undefined)
		if (0x30 == ctrl) return 0x00; // lsb general purpose ctrl 1
		if (0x31 == ctrl) return 0x00; // lsb general purpose ctrl 2
		if (0x32 == ctrl) return 0x00; // lsb general purpose ctrl 3
		if (0x33 == ctrl) return 0x00; // lsb general purpose ctrl 4
		if (0x34 == ctrl) return 0x00; // lsb (undefined)
		if (0x35 == ctrl) return 0x00; // lsb (undefined)
		if (0x36 == ctrl) return 0x00; // lsb (undefined)
		if (0x37 == ctrl) return 0x00; // lsb (undefined)
		if (0x38 == ctrl) return 0x00; // lsb (undefined)
		if (0x39 == ctrl) return 0x00; // lsb (undefined)
		if (0x3A == ctrl) return 0x00; // lsb (undefined)
		if (0x3B == ctrl) return 0x00; // lsb (undefined)
		if (0x3C == ctrl) return 0x00; // lsb (undefined)
		if (0x3D == ctrl) return 0x00; // lsb (undefined)
		if (0x3E == ctrl) return 0x00; // lsb (undefined)
		if (0x3F == ctrl) return 0x00; // lsb (undefined)
		
		// 64-69 (0x40-0x45): single byte (switches)
		if (0x40 == ctrl) return 0x00; // hold pedal 1        (0x00, see gm2, page 8)
		if (0x41 == ctrl) return 0x00; // portamento pedal    (0x00, see gm2, page 9)
		if (0x42 == ctrl) return 0x00; // sostenuto pedal     (0x00, see gm2, page 9)
		if (0x43 == ctrl) return 0x00; // soft pedal          (0x00, see gm2, page 9)
		if (0x44 == ctrl) return 0x00; // legato pedal
		if (0x45 == ctrl) return 0x00; // hold pedal 2
		
		// 70-95 (0x46-0x5F): single byte (low resolution)
		if (0x46 == ctrl) return 0x00; // sound ctrl 1 (sound variation)
		if (0x47 == ctrl) return 0x40; // sound ctrl 2 (timbre/harmonic/filter)    (0x40 == 64, see gm2, page 9)
		if (0x48 == ctrl) return 0x40; // sound ctrl 3 (release time)              (0x40 == 64, see gm2, page 10)
		if (0x49 == ctrl) return 0x40; // sound ctrl 4 (attack time)               (0x40 == 64, see gm2, page 10)
		if (0x4A == ctrl) return 0x40; // sound ctrl 5 (brightness)                (0x40 == 64, see gm2, page 10)
		if (0x4B == ctrl) return 0x40; // sound ctrl 6 (decay time)                (0x40 == 64, see gm2, page 10)
		if (0x4C == ctrl) return 0x40; // sound ctrl 7 (vibrato rate)              (0x40 == 64, see gm2, page 11)
		if (0x4D == ctrl) return 0x40; // sound ctrl 8 (vibrato depth)             (0x40 == 64, see gm2, page 11)
		if (0x4E == ctrl) return 0x40; // sound ctrl 9 (vibrato delay)             (0x40 == 64, see gm2, page 11)
		if (0x4F == ctrl) return 0x00; // sound ctrl 10
		if (0x50 == ctrl) return 0x00; // general purpose ctrl 5
		if (0x51 == ctrl) return 0x00; // general purpose ctrl 6
		if (0x52 == ctrl) return 0x00; // general purpose ctrl 7
		if (0x53 == ctrl) return 0x00; // general purpose ctrl 8
		if (0x54 == ctrl) return 0x00; // portamento ctrl
		if (0x55 == ctrl) return 0x00; // (undefined)
		if (0x56 == ctrl) return 0x00; // (undefined)
		if (0x57 == ctrl) return 0x00; // (undefined)
		if (0x58 == ctrl) return 0x00; // high resolution velocity prefix
		if (0x59 == ctrl) return 0x00; // (undefined)
		if (0x5A == ctrl) return 0x00; // (undefined)
		if (0x5B == ctrl) return 0x28; // effect 1 depth (reverb send level)       (0x28 == 40, see gm2, page 11)
		if (0x5C == ctrl) return 0x00; // effect 2 depth (tremolo depth)
		if (0x5D == ctrl) return 0x00; // effect 3 depth (chorus send level)       (0x00 == 0, see gm2, page 11)
		if (0x5E == ctrl) return 0x00; // effect 4 depth (celeste depth)
		if (0x5F == ctrl) return 0x00; // effect 5 depth (phaser level)
		
		// 96-97 (0x60-0x61): data increment/decrement
		if (0x60 == ctrl) return 0x00; // DATA INCREMENT       (not used anyway)
		if (0x61 == ctrl) return 0x00; // DATA DECREMENT       (not used anyway)
		
		// 98-99 (0x62-0x63): NRPN LSB/MSB
		if (0x62 == ctrl) return 0x00; // NRPN LSB             (0x7F = 127, not mentioned but makes sense)
		if (0x63 == ctrl) return 0x00; // NRPN MSB             (0x7F = 127, not mentioned but makes sense)
		
		// 100-101 (0x64-0x65): RPN LSB/MSB
		if (0x64 == ctrl) return 0x00; // RPN LSB              (0x7F = 127, see gm2, page 12)
		if (0x65 == ctrl) return 0x00; // RPN MSB              (0x7F = 127, see gm2, page 12)
		
		// 102-119 (0x66-0x77): single byte (undefined)
		if (0x66 == ctrl) return 0x00; // (undefined)
		if (0x67 == ctrl) return 0x00; // (undefined)
		if (0x68 == ctrl) return 0x00; // (undefined)
		if (0x69 == ctrl) return 0x00; // (undefined)
		if (0x6A == ctrl) return 0x00; // (undefined)
		if (0x6B == ctrl) return 0x00; // (undefined)
		if (0x6C == ctrl) return 0x00; // (undefined)
		if (0x6D == ctrl) return 0x00; // (undefined)
		if (0x6E == ctrl) return 0x00; // (undefined)
		if (0x6F == ctrl) return 0x00; // (undefined)
		if (0x70 == ctrl) return 0x00; // (undefined)
		if (0x71 == ctrl) return 0x00; // (undefined)
		if (0x72 == ctrl) return 0x00; // (undefined)
		if (0x73 == ctrl) return 0x00; // (undefined)
		if (0x74 == ctrl) return 0x00; // (undefined)
		if (0x75 == ctrl) return 0x00; // (undefined)
		if (0x76 == ctrl) return 0x00; // (undefined)
		if (0x77 == ctrl) return 0x00; // (undefined)
		
		// 120-127 (0x78-0x7F): mode
		if (0x78 == ctrl) return 0x00; // all sounds off                                (see gm2, page 13)
		if (0x79 == ctrl) return 0x00; // all controllers off (reset all controllers)   (see gm2, page 14)
		if (0x7A == ctrl) return 0x7F; // local control (0=ON, 7F=OFF, rest=invalid)    (see MIDI 1.0, page 26)
		if (0x7B == ctrl) return 0x00; // all notes off                                 (see gm2, page 14)
		if (0x7C == ctrl) return 0x00; // omni mode: off (+all notes off)               (see gm2, page 14)
		if (0x7D == ctrl) return 0x00; // omni mode: on  (+all notes off)               (see gm2, page 14)
		if (0x7E == ctrl) return 0x01; // mono mode (+poly off, +all notes off)         (see gm2, page 14)
		if (0x7F == ctrl) return 0x00; // poly mode (+mono off, +all notes off)         (see gm2, page 15)
		
		// default (should never happen)
		return 0x00;
	}
	
	/**
	 * Resets all known RPNs to their default values.
	 * 
	 * @param channel  MIDI channel
	 * @param tick     tickstamp of the sequence
	 */
	private static final void resetAllRPNs(byte channel, long tick) {
		TreeMap<Integer, TreeMap<Long, Integer>> channelRpnHistory = new TreeMap<>();
		
		byte msb = 0x00;
		byte lsb = 0x00;
		
		// normal RPNs
		while (lsb <= 0x06) {
			int rpn = msb * 128 + lsb;
			TreeMap<Long, Integer> rpnHistoryEntry = new TreeMap<>();
			byte[] defaultMsbLsb = getRpnDefault(msb, lsb);
			int    defaultValue  = (defaultMsbLsb[0] << 8) + defaultMsbLsb[1];
			rpnHistoryEntry.put(tick, defaultValue);
			channelRpnHistory.put(rpn, rpnHistoryEntry);
			rpnHistory.put(channel, channelRpnHistory);
			lsb++;
		}
		
		// 3D
		msb = 0x3D;
		lsb = 0x00;
		while (lsb <= 0x08) {
			int rpn = msb * 128 + lsb;
			TreeMap<Long, Integer> rpnHistoryEntry = new TreeMap<>();
			byte[] defaultMsbLsb = getRpnDefault(msb, lsb);
			int    defaultValue  = (defaultMsbLsb[0] << 8) + defaultMsbLsb[1];
			rpnHistoryEntry.put(tick, defaultValue);
			channelRpnHistory.put(rpn, rpnHistoryEntry);
			rpnHistory.put(channel, channelRpnHistory);
			lsb++;
		}
	}
	
	/**
	 * Returns the default value for the given RPN.
	 * 
	 * @param msb  RPN MSB
	 * @param lsb  RPN LSB
	 * @return default MSB (index 0) and default LSB (index 1)
	 */
	private static final byte[] getRpnDefault(int msb, int lsb) {
		byte[] result = {0, 0};
		if (0x00 == msb) {
			if (0x00 == lsb) {
				// pitch bend sensitivity    (see gm2, page 12)
				result[0] = 0x02;
				result[1] = 0x00;
			}
			else if (0x01 == lsb) {
				// master fine tune    (see gm2, page 12)
				result[0] = 0x40;
				result[1] = 0x00;
			}
			else if (0x02 == lsb) {
				// master coarse tune    (see gm2, page 13)
				result[0] = 0x40;
				result[1] = 0x00;
			}
			else if (0x03 == lsb) {
				// tuning program change
				// TODO: default
			}
			else if (0x04 == lsb) {
				// tuning bank select
				// TODO: default
			}
			else if (0x05 == lsb) {
				// modulation depth range    (see gm2, page 13)
				result[0] = 0x00;
				result[1] = 0x40;
			}
			else if (0x06 == lsb) {
				// MPE config message (MCM)    (see RP-053)
				result[0] = 0x00;
				result[1] = 0x00;
			}
		}
		else if (0x3D == msb) {
			// 3D defaults can be found in RP-049
			
			if (0x00 == lsb) {
				// azimuth angle
				result[0] = 0x40;
				result[1] = 0x00;
			}
			else if (0x01 == lsb) {
				// elevation angle
				result[0] = 0x40;
				result[1] = 0x00;
			}
			else if (0x02 == lsb) {
				// gain
				result[0] = 0x7F;
				result[1] = 0x7F;
			}
			else if (0x03 == lsb) {
				// distance ratio
				result[0] = 0x00;
				result[1] = 0x10;
			}
			else if (0x04 == lsb) {
				// maximum distance
				result[0] = 0x7F;
				result[1] = 0x7F;
			}
			else if (0x05 == lsb) {
				// gain at maximum distance
				result[0] = 0x51;
				result[1] = 0x0F;
			}
			else if (0x06 == lsb) {
				// reference distance ratio
				result[0] = 0x00;
				result[1] = 0x10;
			}
			else if (0x07 == lsb) {
				// pan spread angle
				result[0] = 0x4A;
				result[1] = 0x55;
			}
			else if (0x08 == lsb) {
				// roll angle
				result[0] = 0x40;
				result[1] = 0x00;
			}
		}
		
		return result;
	}
}
