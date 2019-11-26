/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.midi;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
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
import org.midica.config.Laf;
import org.midica.file.CharsetUtils;
import org.midica.file.read.ParseException;
import org.midica.file.write.MidicaPLExporter;
import org.midica.ui.model.SingleMessage;
import org.midica.ui.model.MessageTreeNode;
import org.midica.ui.model.MidicaTreeModel;
import org.midica.ui.player.PlayerView;

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
	
	// message debugging
	private static final boolean DEBUG_MODE = false;
	
	// karaoke-related constants
	private static final int    KARAOKE_TEXT           =  1; // meta message type: text
	private static final int    KARAOKE_LYRICS         =  2; // meta message type: lyrics
	private static final float  KAR_PRE_ALERT_QUARTERS =  1; // pre-alert so many quarter notes before a lyric change
	private static final float  KAR_MAX_SYL_SPACE_RATE =  8; // max syllables-per-whitespace - add spaces, if there are not enough
	
	public static final byte NOTE_HISTORY_BUFFER_SIZE_PAST   = 5;
	public static final byte NOTE_HISTORY_BUFFER_SIZE_FUTURE = 3;
	
	private static final long DEFAULT_CHANNEL_CONFIG_TICK = -100;
	
	private static Sequence  sequence      = null;
	private static String    chosenCharset = null;
	private static LyricUtil lyricUtil     = null;
	
	// karaoke-related fields
	private static String  midiFileCharset  = null;
	private static String  karaokeMode      = null;
	private static long    karLineTick      = -1;
	private static boolean karLineEnded     = false;
	private static long    karPreAlertTicks = 0;
	private static int     karTextCounter   = 0;
	private static int     karLyricsCounter = 0;
	private static boolean karUseLyrics     = true;
	private static Pattern karPattSecond    = Pattern.compile( Pattern.quote("<span class='second'>") );
	
	private static HashMap<String, Object> sequenceInfo = null;
	private static HashMap<String, Object> karaokeInfo  = null;
	
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
	
	/** ticks of either lyrics events or pre-alerts */
	private static TreeSet<Long> lyricsEvent = null;
	
	/**                    tick     --   channel */
	private static TreeMap<Long, TreeSet<Byte>> markers = null;
	
	/**                    tick */
	private static TreeSet<Long> markerTicks = null;
	
	/**                    channel   --  0=bankMSB, 1=bankLSB, 2=program  */
	private static TreeMap<Byte, Byte[]> channelConfig = null;
	
	/**                    channel   --  tick -- 0=bankMSB, 1=bankLSB, 3=program   */
	private static TreeMap<Byte, TreeMap<Long, Byte[]>> instrumentHistory = null;
	
	/**                    channel   --  tick -- comment   */
	private static TreeMap<Byte, TreeMap<Long, String>> commentHistory = null;
	
	/**                    channel   --  0=RPN MSB, 1=RPN LSB, 2=NRPN MSB, 3=NRPN LSB, 4=1(RPN)|0(NRPN)|-1(unknown) */
	private static TreeMap<Byte, Byte[]> channelParamConfig = null;
	
	/**                    channel   --  tick -- 0=RPN MSB, 1=RPN LSB, 2=NRPN MSB, 3=NRPN LSB, 4=1(RPN)|0(NRPN)|-1(unknown) */
	private static TreeMap<Byte, TreeMap<Long, Byte[]>> channelParamHistory = null;
	
	/**                    channel   --  tick -- MSB */
	private static TreeMap<Byte, TreeMap<Long, Byte>> pitchBendSensMsbHistory = null;
	
	/**                    channel   --  tick -- LSB */
	private static TreeMap<Byte, TreeMap<Long, Byte>> pitchBendSensLsbHistory = null;
	
	/**        line begin tick -- syllable tick -- syllable */
	private static TreeMap<Long, TreeMap<Long, String>> lyrics = null;
	
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
		lyrics       = null;
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
			
			// fill data structures
			parse();
		}
		catch (Exception e) {
			if (DEBUG_MODE) {
				e.printStackTrace();
			}
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
	 * - **lyrics**: TreeMap<Long, TreeMap<Long, String>>
	 *     - line begin tick
	 *     - syllable tick
	 *     - syllable
	 * 
	 * @return internal data structures as described above.
	 */
	public static HashMap<String, Object> getHistories() {
		HashMap<String, Object> histories = new HashMap<>();
		histories.put( "instrument_history", instrumentHistory  );
		histories.put( "comment_history",    commentHistory     );
		histories.put( "note_history",       noteHistory        );
		histories.put( "note_on_off",        noteOnOffByChannel );
		histories.put( "lyrics",             lyrics             );
		
		return histories;
	}
	
	/**
	 * Initializes the internal data structures so that they are ready to
	 * be filled with sequence information during the parsing process.
	 * @throws ReflectiveOperationException if the root node of the message
	 *         tree cannot be created.
	 */
	private static void init() throws ReflectiveOperationException {
		lyricUtil = LyricUtil.getInstance();
		
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
		pitchBendSensMsbHistory = new TreeMap<>();
		pitchBendSensLsbHistory = new TreeMap<>();
		for (byte channel = 0; channel < 16; channel++) {
			TreeMap<Long, Byte> msbHistoryEntry = new TreeMap<>();
			TreeMap<Long, Byte> lsbHistoryEntry = new TreeMap<>();
			msbHistoryEntry.put(0L, (byte) 2); // default: 2 half tone steps
			lsbHistoryEntry.put(0L, (byte) 0);
			pitchBendSensMsbHistory.put(channel, msbHistoryEntry);
			pitchBendSensLsbHistory.put(channel, lsbHistoryEntry);
		}
		
		// initialize data structures for karaoke
		karPreAlertTicks = (long) (resolution / KAR_PRE_ALERT_QUARTERS);
		karTextCounter   = 0;
		karLyricsCounter = 0;
		karUseLyrics     = true;
		karaokeMode      = null;
		karLineTick      = -1;
		karLineEnded     = false;
		karaokeInfo      = new HashMap<>();
		lyrics           = new TreeMap<>();
		lyricsEvent      = new TreeSet<>();
		karaokeInfo.put( "lyrics", lyrics );
		sequenceInfo.put( "karaoke", karaokeInfo );
		
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
		
		// init data structures for the bank and instrument history
		channelConfig = new TreeMap<>();
		for (byte channel = 0; channel < 16; channel++) {
			Byte[] conf = { 0, 0, 0 }; // default values: bankMSB=0, bankLSB=0, program=0
			channelConfig.put( channel, conf );
		}
		instrumentHistory = new TreeMap<>();
		for (byte channel = 0; channel < 16; channel++) {
			TreeMap<Long, Byte[]> channelHistory = new TreeMap<>();
			instrumentHistory.put( channel, channelHistory );
			
			// default config (same as the default in channelConfig)
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
		
		// Analyze for channel activity, note history, banks, instruments and (N)RPN.
		// Therefore the CREATED sequence is used.
		// In this sequence tracks match channels. So we know that we will
		// process the note-related events in the right order.
		int trackNum = 0;
		for (Track t : SequenceCreator.getSequence().getTracks()) {
			for (int i=0; i < t.size(); i++) {
				MidiEvent   event = t.get( i );
				long        tick  = event.getTick();
				MidiMessage msg   = event.getMessage();
				
				if (msg instanceof ShortMessage) {
					processShortMessageByChannel( (ShortMessage) msg, tick );
				}
			}
		}
		
		// Analyze for general statistics.
		// Therefore the ORIGINAL sequence is used.
		midiFileCharset = null;
		trackNum        = 0;
		for (Track t : sequence.getTracks()) {
			int msgNum = 0;
			for (int i=0; i < t.size(); i++) {
				MidiEvent   event = t.get( i );
				long        tick  = event.getTick();
				MidiMessage msg   = event.getMessage();
				
				// replace some messages
				if (DEBUG_MODE) {
					msg = replaceMessage( msg, tick );
				}
				
				if (msg instanceof MetaMessage) {
					processMetaMessage( (MetaMessage) msg, tick, trackNum, msgNum );
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
		
		// use lyrics or text events for karaoke?
		if (karTextCounter > karLyricsCounter) {
			karUseLyrics = false;
		}
		
		// Analyze for karaoke events and channel names (instrument names).
		// Therefore the CREATED sequence is used.
		midiFileCharset = null;
		trackNum        = 0;
		for (Track t : SequenceCreator.getSequence().getTracks()) {
			for (int i=0; i < t.size(); i++) {
				MidiEvent   event = t.get( i );
				long        tick  = event.getTick();
				MidiMessage msg   = event.getMessage();
				
				if (msg instanceof MetaMessage) {
					processMetaMessageByChannel( (MetaMessage) msg, tick, trackNum );
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
			// PROGRAM index = 2
			channelConfig.get( channel )[ 2 ] = (byte) msg.getData1();
			Byte[] confAtTick = channelConfig.get( channel ).clone();
			instrumentHistory.get( channel ).put( tick, confAtTick );
			markerTicks.add( tick ); // prepare marker event
			
			return;
		}
		
		// CONTROL CHANGE
		if (ShortMessage.CONTROL_CHANGE == cmd) {
			int controller = msg.getData1();
			int value      = msg.getData2();
			
			// BANK MSB
			if (0x00 == controller) {
				// MSB index = 0
				channelConfig.get( channel )[ 0 ] = (byte) value;
			}
			
			// BANK LSB
			else if (0x20 == controller) {
				// LSB index = 1
				channelConfig.get( channel )[ 1 ] = (byte) value;
			}
			
			// (N)RPN MSB/LSB
			else if (controller >= 0x62 && controller <= 0x65) {
				
				// adjust current channel param config
				int index = 0x62 == controller ? 3  // NRPN LSB
				          : 0x63 == controller ? 2  // NRPN MSB
				          : 0x64 == controller ? 1  // RPN LSB
				          : 0x65 == controller ? 0  // RPN MSB
				          : -1;
				if (-1 == index) { // should never happen
					return;
				}
				channelParamConfig.get( channel )[ index ] = (byte) value;
				
				// calculate and adjust active parameter type: RPN(1), NRPN(0) or none(-1)?
				byte rpnNrpnReset;
				byte currentMsb;
				byte currentLsb;
				if (controller >= 0x64) {
					rpnNrpnReset = 1;  // RPN
					currentMsb = channelParamConfig.get( channel )[ 0 ];
					currentLsb = channelParamConfig.get( channel )[ 1 ];
				}
				else {
					rpnNrpnReset = 0;  // NRPN
					currentMsb = channelParamConfig.get( channel )[ 2 ];
					currentLsb = channelParamConfig.get( channel )[ 3 ];
				}
				if (((byte) 0x7F) == currentMsb && ((byte) 0x7F) == currentLsb) {
					// reset
					rpnNrpnReset = -1;
				}
				channelParamConfig.get( channel )[ 4 ] = rpnNrpnReset;
				
				// add history entry
				Byte[] confAtTick = channelParamConfig.get( channel ).clone();
				channelParamHistory.get( channel ).put( tick, confAtTick );
			}
			
			// Data Entry/Increment/Decrement
			else if ( 0x06 == controller       // data entry MSB
			       || 0x26 == controller       // data entry LSB
			       || 0x60 == controller       // data button increment
			       || 0x61 == controller ) {   // data button decrement
				
				Byte[] paramMsbLsb = getChannelParamMsbLsbType(channel, tick);
				byte msb  = paramMsbLsb[ 0 ];
				byte lsb  = paramMsbLsb[ 1 ];
				byte type = paramMsbLsb[ 2 ];
				
				// data change for pitch bend sensitivity?
				if (0 == msb && 0 == lsb && 1 == type) {
					boolean mustChangeBoth = false;
					float   currentValue = getPitchBendSensitivity(channel, tick);
					if (0x06 == controller)         // data entry MSB
						pitchBendSensMsbHistory.get( channel ).put(tick, (byte) value);
					else if (0x26 == controller)    // data entry LSB (ignore)
						pitchBendSensLsbHistory.get( channel ).put(tick, (byte) value);
					else if (0x60 == controller) {  // data button increment
						mustChangeBoth = true;
						currentValue += ( 1f / 128f );
					}
					else if (0x61 == controller) {  // data button decrement
						mustChangeBoth = true;
						currentValue -= ( 1f / 128f );
					}
					if (mustChangeBoth) {
						byte newMsb = (byte) currentValue;
						byte newLsb = (byte) Math.round(128 * (currentValue - (float) newMsb));
						pitchBendSensMsbHistory.get( channel ).put(tick, (byte) newMsb);
						pitchBendSensLsbHistory.get( channel ).put(tick, (byte) newLsb);
					}
				}
			}
		}
		
		// NOTE ON or OFF
		else if (ShortMessage.NOTE_ON == cmd) {
			byte note     = (byte) msg.getData1();
			byte velocity = (byte) msg.getData2();
			
			// on or off?
			if (velocity > 0) {
				addNoteOn( tick, channel, note, velocity );
			}
			else {
				addNoteOff( tick, channel, note );
			}
		}
		
		// NOTE OFF
		else if (ShortMessage.NOTE_OFF == cmd) {
			byte note = (byte) msg.getData1();
			addNoteOff( tick, channel, note );
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
		String text    = null;
		
		// TEMPO
		if (MidiListener.META_SET_TEMPO == type) {
			int mpq = MidiUtils.getTempoMPQ( msg );
			int bpm = (int) MidiUtils.convertTempo( mpq );
			TreeMap<Long, Integer> tempoMpq = (TreeMap<Long, Integer>) sequenceInfo.get( "tempo_mpq" );
			tempoMpq.put( tick, mpq );
			TreeMap<Long, Integer> tempoBpm = (TreeMap<Long, Integer>) sequenceInfo.get( "tempo_bpm" );
			tempoBpm.put( tick, bpm );
		}
		
		// TEXT
		else if (MidiListener.META_TEXT == type) {
			karTextCounter++;
		}
		
		// {#...=...} song info according to recommended practice RP-026
		else if (MidiListener.META_LYRICS == type) {
			karLyricsCounter++;
			text = CharsetUtils.getTextFromBytes(content, chosenCharset, midiFileCharset);
			
			// charset switch?
			String newCharset = CharsetUtils.findCharsetSwitch(text);
			if (newCharset != null) {
				midiFileCharset = newCharset;
			}
			
			HashMap<String, String> info = lyricUtil.getSongInfo(text);
			if (info != null) {
				for (String key : info.keySet()) {
					String value = info.get(key);
					if (LyricUtil.SOFTWARE.equals(key)) {
						retrieveSoftwareVersion(value);
					}
					else if (karaokeInfo.containsKey(key)) {
						String oldValue = (String) karaokeInfo.get( key );
						karaokeInfo.put( key, oldValue + "\n" + value );
					}
					else {
						karaokeInfo.put( key, value );
					}
				}
			}
		}
		
		// COPYRIGHT
		else if (MidiListener.META_COPYRIGHT == type) {
			String                  copyright = CharsetUtils.getTextFromBytes( content, chosenCharset, midiFileCharset );
			HashMap<String, String> metaInfo  = (HashMap<String, String>) sequenceInfo.get( "meta_info" );
			metaInfo.put( "copyright", copyright );
		}
		
		// fetch tree and detailed message information
		MessageClassifier.processMetaMessage(
			(MetaMessage) msg, tick, trackNum, msgNum,
			messages,       // add details and leaf node to messages
			msgTreeModel,   // add leaf node
			chosenCharset,  // charset from the file chooser
			midiFileCharset // charset from last charset switch in a meta message
		);
	}
	
	/**
	 * Retrieves instrument or karaoke specific information from meta messages.
	 * 
	 * @param msg       Meta message.
	 * @param tick      Tickstamp.
	 * @param trackNum  Track number of the created string.
	 */
	private static void processMetaMessageByChannel(MetaMessage msg, long tick, int trackNum) {
		int    type = msg.getType();
		byte[] data = msg.getData();
		String text = null;
		
		// LYRICS
		if (MidiListener.META_LYRICS == type) {
			text = CharsetUtils.getTextFromBytes( data, chosenCharset, midiFileCharset );
			
			// charset switch?
			String newCharset = CharsetUtils.findCharsetSwitch(text);
			if (newCharset != null) {
				midiFileCharset = newCharset;
			}
			text = lyricUtil.removeTags(text);
			processKaraoke( text, KARAOKE_LYRICS, tick );
		}
		
		// INSTRUMENT NAME
		if (MidiListener.META_INSTRUMENT_NAME == type) {
			
			// get channel number
			byte channel = (byte) ( trackNum - SequenceCreator.NUM_META_TRACKS );
			
			// invalid channel - not produced by Midica?
			if (channel < 0 || channel >= MidiDevices.NUMBER_OF_CHANNELS) {
				return;
			}
			
			// remember the channel comment
			text = CharsetUtils.getTextFromBytes( data, chosenCharset, midiFileCharset );
			commentHistory.get( channel ).put( tick, text );
		}
		
		// TEXT
		else if (MidiListener.META_TEXT == type) {
			text = CharsetUtils.getTextFromBytes( data, chosenCharset, midiFileCharset );
			
			// karaoke meta message according to .kar files?
			if (text.startsWith("@") && text.length() > 1) {
				String prefix = text.substring( 0, 2 );
				text          = text.substring( 2 );
				
				// karaoke type definition?
				if ("@K".equals(prefix)) {
					karaokeMode = text;
					karaokeInfo.put( "type", karaokeMode );
				}
				
				// version
				else if ("@V".equals(prefix)) {
					if (null == karaokeInfo.get("version")) {
						karaokeInfo.put( "version", text );
					}
				}
				
				// language
				else if ("@L".equals(prefix)) {
					if (null == karaokeInfo.get("language")) {
						karaokeInfo.put( "language", text );
					}
				}
				
				// title, author or copyright
				else if ("@T".equals(prefix)) {
					if (null == karaokeInfo.get("title")) {
						karaokeInfo.put( "title", text );
					}
					else if (null == karaokeInfo.get("author")) {
						karaokeInfo.put( "author", text );
					}
					else if (null == karaokeInfo.get("copyright")) {
						karaokeInfo.put( "copyright", text );
					}
				}
				
				// further information
				else if ("@I".equals(prefix)) {
					ArrayList<String> infos = (ArrayList<String>) karaokeInfo.get( "infos" );
					if (null == infos) {
						infos = new ArrayList<>();
						karaokeInfo.put( "infos", infos );
					}
					infos.add( text );
				}
				
				// ignore all other messages beginning with "@"
				return;
			}
			
			// Normal text, maybe lyrics.
			// Unfortunately some MIDI files contain lyrics as type TEXT
			// instead of LYRICS without providing an @K header. So we must
			// consider all text as possibly lyrics.
			processKaraoke( text, KARAOKE_TEXT, tick );
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
		Byte[] config     = channelConfig.get( channel );
		int    bankNum    = ( config[0] << 7 ) | config[ 1 ]; // bankMSB * 2^7 + bankLSB
		String bankSyntax = config[ 0 ] + ""; // MSB as a string
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
	 * Processes the given text as a karaoke meta or lyrics command.
	 * 
	 * @param text  The text from the text or lyrics message.
	 * @param type  The message type (1: text, 2: lyrics).
	 * @param tick  Tickstamp of the event.
	 */
	private static void processKaraoke(String text, int type, long tick) {
		
		// Process as syllable-based lyrics?
		// Some @K songs contain duplicate lyrics (text and lyrics messages).
		// In this case show only one part of them.
		if (karUseLyrics && KARAOKE_TEXT == type)
			return;
		if (! karUseLyrics && KARAOKE_LYRICS == type)
			return;
		if ( "".equals(text) )
			return;
		
		// UNIFY WHITESPACES FROM DIFFERENT SOURCES
		if (KARAOKE_LYRICS == type) {
			// unescape \r, \n, \t
			text = lyricUtil.unescapeSpecialWhitespaces(text);
		}
		else {
			// unescape /, \
			if (text.startsWith("/"))
				text = "\r" + text.substring(1);
			else if (text.startsWith("\\"))
				text = "\n" + text.substring(1);
		}
		
		// get last or current line
		TreeMap<Long, String> line = lyrics.get( karLineTick );
		
		// Did we already find another lyrics/text event at this tick?
		if (line != null && line.containsKey(tick)) {
			// Assume that the first one was the right one and ignore the rest.
			return;
		}
		
		// do we need (a) new line(s)?
		boolean needNewLineNow         = false;
		boolean beginsWithNewLine      = false;
		boolean beginsWithNewParagraph = false;
		boolean endsWithNewLine        = false;
		boolean endsWithNewParagraph   = false;
		if (-1 == karLineTick || karLineEnded) {
			needNewLineNow = true;
			karLineEnded   = false;
		}
		if (text.length() > 1) { // regard "\r" as belonging only to the LAST line
			if (text.startsWith("\r")) {
				needNewLineNow    = true;
				beginsWithNewLine = true;
			}
			else if (text.startsWith("\n")) {
				needNewLineNow         = true;
				beginsWithNewLine      = true;
				beginsWithNewParagraph = true;
			}
		}
		if (text.endsWith("\r")) {
			endsWithNewLine = true;
			karLineEnded    = true;
		}
		else if (text.endsWith("\n")) {
			endsWithNewLine      = true;
			endsWithNewParagraph = true;
			karLineEnded         = true;
		}
		
		// BEGINNING WITH LINEBREAK(S)?
		// move CR/LF from the beginning to the end of the last syllable
		if (beginsWithNewLine) {
			text = text.substring(1); // remove CR/LF
			if (line != null) {
				Entry<Long, String> lastSylEntry = line.lastEntry();
				long lastSylTick    = lastSylEntry.getKey();
				String lastSyllable = lastSylEntry.getValue();
				
				// add 1st line break (for the new line)
				lastSyllable = lastSyllable + "\n";
				
				// add 2nd line break (for the new paragraph)
				if (beginsWithNewParagraph) {
					lastSyllable = lastSyllable + "\n";
				}
				
				// commit the changes
				line.put( lastSylTick, lastSyllable );
			}
		}
		
		// ADD NEW LINE
		if (needNewLineNow) {
			karLineTick = tick;
			line        = new TreeMap<>();
			lyrics.put(tick, line);
		}
		
		// ENDING WITH LINEBREAK(S)?
		if (endsWithNewLine) {
			text = text.substring(0, text.length() - 1) + "\n"; // replace \r with \n
			if (endsWithNewParagraph) {
				text += "\n"; // add a second line break
			}
		}
		
		// add the syllable
		line.put(tick, text);
		
		// PREPARE MARKER EVENTS
		
		// the lyrics event itself
		lyricsEvent.add( tick );
		markerTicks.add( tick );
		
		// pre-alert before the lyrics event
		tick -= karPreAlertTicks;
		if (tick < 0) {
			tick = 0;
		}
		lyricsEvent.add( tick );
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
		Collections.sort( messages );
		
		// average, min and max tempo
		TreeMap<Long, Integer> tempoMpq = (TreeMap<Long, Integer>) sequenceInfo.get( "tempo_mpq" );
		TreeMap<Long, Integer> tempoBpm = (TreeMap<Long, Integer>) sequenceInfo.get( "tempo_bpm" );
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
			TreeMap<Long, TreeMap<Byte, Byte>> channelNoteHistory = noteHistory.get( channel );
			if (channelNoteHistory.isEmpty()) {
				TreeMap<Long, Byte[]> channelInstrumentHistory = instrumentHistory.get( channel );
				
				Byte[] conf0 = { -1, -1, -1 };
				channelInstrumentHistory.put( -1L, conf0 );
			}
		}
		
		// Decide which channel to use for the channel part of the lyrics marker bytes.
		Set<Byte> activeChannels = activityByChannel.keySet();
		byte      lyricsChannel  = -1; // channel part for the lyrics marker events
		if (lyrics.size() > 0) {
			Iterator<Byte> it = activeChannels.iterator();
			if (it.hasNext()) {
				// use one of the active channels for the lyrics
				lyricsChannel = it.next();
			}
			else {
				// No channel activity at all, only lyrics.
				// Use channel 0 for lyrics and avoid a later null pointer exception.
				lyricsChannel = 0;
				activeChannels.add( lyricsChannel );
				activityByChannel.put( lyricsChannel, new TreeMap<>() );
			}
		}
		
		// markers
		for (long tick : markerTicks) {
			
			// initiate structures for that tick
			TreeSet<Byte> channelsAtTick  = new TreeSet<>();
			boolean       must_add_marker = false;
			
			// walk through all channels that have any activity IN ANY TICK (or lyrics)
			for (byte channel : activeChannels) {
				
				boolean lyricsChanged     = false;
				boolean activityChanged   = false;
				boolean historyChanged    = false;
				boolean instrumentChanged = false;
				
				// is there a lyrics event at the current tick?
				if (lyricsChannel == channel && lyricsEvent.contains(tick)) {
					lyricsChanged = true;
				}
				
				// is there an instrument change at the current tick?
				Byte[] instrChange = instrumentHistory.get( channel ).get( tick );
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
					must_add_marker = true;
				}
			}
			
			// add the marker
			if (must_add_marker) {
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
		postprocessKaraoke();
	}
	
	/**
	 * Postprocesses data structures for karaoke.
	 * 
	 * - Joins all info headers (beginning with "@I") to a single string.
	 * - Deletes non printable characters.
	 * - Adds spaces if necessary.
	 * - Creates a full lyrics string for the info view.
	 * - Re-organizes lines, if necessary.
	 * - Replacements for the HTML view.
	 * - HTML formatting for the second voice.
	 */
	private static void postprocessKaraoke() {
		
		// join all info headers (@I)
		ArrayList<String> infoHeaders = (ArrayList<String>) karaokeInfo.get( "infos" );
		if (infoHeaders != null) {
			karaokeInfo.put( "info", String.join("\n", infoHeaders) );
		}
		
		// delete carriage returns from the middle of syllables
		Pattern patCR = Pattern.compile( Pattern.quote("\r") );
		for (TreeMap<Long, String> line : lyrics.values()) {
			for (Entry<Long, String> sylEntry : line.entrySet()) {
				long   tick     = sylEntry.getKey();
				String syllable = sylEntry.getValue();
				syllable        = patCR.matcher(syllable).replaceAll("");
				line.put( tick, syllable );
			}
		}
		
		// Check if there are enough spaces between the syllables.
		int     totalSyllables = 0;
		int     totalSpaces    = 0;
		Pattern pattEnd        = Pattern.compile( ".*\\s$", Pattern.MULTILINE ); // ends with whitespace
		Pattern pattBegin      = Pattern.compile( "^\\s",   Pattern.MULTILINE ); // begins with whitespace
		for (TreeMap<Long, String> line : lyrics.values()) {
			
			// put all syllables into an array with indexes instead of ticks
			ArrayList<String> sylsInLine = new ArrayList<>();
			for (String syllable : line.values()) {
				sylsInLine.add( syllable );
			}
			
			// count syllables and spaces
			for (int i = 0; i < sylsInLine.size(); i++) {
				totalSyllables++;
				// space at the end of THIS line?
				boolean hasSpace = pattEnd.matcher( sylsInLine.get(i) ).lookingAt();
				if (! hasSpace && i < sylsInLine.size() - 1) {
					// space at the beginning of the NEXT line?
					hasSpace = pattBegin.matcher( sylsInLine.get(i+1) ).lookingAt();
				}
				if (hasSpace) {
					totalSpaces++;
				}
			}
		}
		boolean needMoreSpaces = true;
		if (totalSpaces != 0) {
			needMoreSpaces = (float) totalSyllables / (float) totalSpaces > KAR_MAX_SYL_SPACE_RATE;
		}
		
		// add spaces if necessary
		if (needMoreSpaces) {
			for (TreeMap<Long, String> line : lyrics.values()) {
				for (Entry<Long, String> sylEntry : line.entrySet()) {
					long   tick     = sylEntry.getKey();
					String syllable = sylEntry.getValue();
					
					// word ends with "-". That usually means: The word is not yet over.
					if (syllable.endsWith("-")) {
						// just delete the trailing "-" but don't add a space
						syllable = syllable.replaceFirst( "\\-$", "" );
					}
					else if (! syllable.endsWith("\n")) {
						// add a space
						syllable += " ";
					}
					line.put( tick, syllable );
				}
			}
		}
		
		// create full lyrics string (for the info view)
		StringBuilder lyricsFull = new StringBuilder("");
		for (TreeMap<Long, String> line : lyrics.values()) {
			for (String syllable : line.values()) {
				lyricsFull.append( syllable );
			}
		}
		karaokeInfo.put( "lyrics_full", lyricsFull.toString() );
		
		// get all ticks where a word ends
		// (needed for syllable hyphenation later)
		Pattern pattEndPunct = Pattern.compile( ".*[.,?!\"'\\]\\[;]$", Pattern.MULTILINE ); // ends with punctuation character
		TreeSet<Long> wordEndTicks = new TreeSet<>();
		for (TreeMap<Long, String> line : lyrics.values()) {
			long lastSylTick = -1;
			for (Entry<Long, String> sylEntry : line.entrySet()) {
				long   tick     = sylEntry.getKey();
				String syllable = sylEntry.getValue();
				
				// Word ends AFTER this syllable?
				if (pattEnd.matcher(syllable).lookingAt() || pattEndPunct.matcher(syllable).lookingAt()) {
					wordEndTicks.add(tick);
				}
				
				// Word ends BEFORE this syllable?
				if (pattBegin.matcher(syllable).lookingAt()) {
					wordEndTicks.add(lastSylTick);
				}
				lastSylTick = tick;
			}
		}
		
		// reorganize lines if necessary
		TreeMap<Long, TreeMap<Long, String>> newLyrics = new TreeMap<>();
		for (Entry<Long, TreeMap<Long, String>> lineEntry : lyrics.entrySet()) {
			long                  lineTick = lineEntry.getKey();
			TreeMap<Long, String> line     = lineEntry.getValue();
			
			int lineLength = 0; // number of characters in the line, INCLUDING the current syllable
			int lastLength = 0; // number of characters in the line, WITHOUT the current syllable
			
			// create one or more new line(s) from one original line
			TreeMap<Long, String> newLine = new TreeMap<>();
			for (Entry<Long, String> sylEntry : line.entrySet()) {
				long    sylTick  = sylEntry.getKey();
				String  syllable = sylEntry.getValue();
				lineLength      += syllable.length();
				
				// line too long?
				if (lineLength >= PlayerView.KAR_MAX_CHARS_PER_LINE && lastLength > 0) {
					
					// add linebreak to the LAST syllable
					Entry<Long, String> lastSylEntry = newLine.lastEntry();
					long                lastSylTick  = lastSylEntry.getKey();
					String              lastSyllable = lastSylEntry.getValue();
					if (! wordEndTicks.contains(lastSylTick)) {
						lastSyllable += "-";
					}
					lastSyllable += "\n";
					newLine.put( lastSylTick, lastSyllable );
					
					// close the line and open a new one
					newLyrics.put( lineTick, newLine );
					newLine    = new TreeMap<>();
					lineTick   = sylTick;
					lineLength = syllable.length();
				}
				
				// add current syllable and prepare the next one
				newLine.put( sylTick, syllable );
				lastLength = lineLength;
			}
			
			// record the rest of the original line and prepare the next one
			newLyrics.put( lineTick, newLine );
		}
		lyrics = newLyrics;
		
		// HTML replacements and entities
		Pattern pattAMP = Pattern.compile("&");
		Pattern pattLT  = Pattern.compile("<");
		Pattern pattGT  = Pattern.compile(">");
		Pattern pattLF  = Pattern.compile( Pattern.quote("\n") );
		for (TreeMap<Long, String> line : lyrics.values()) {
			for (Entry<Long, String> sylEntry : line.entrySet()) {
				long   tick     = sylEntry.getKey();
				String syllable = sylEntry.getValue();
				syllable        = pattAMP.matcher(syllable).replaceAll("&amp;");
				syllable        = pattLT.matcher(syllable).replaceAll("&lt;");
				syllable        = pattGT.matcher(syllable).replaceAll("&gt;");
				syllable        = pattLF.matcher(syllable).replaceAll( Matcher.quoteReplacement("<br>\n") );
				line.put( tick, syllable );
			}
		}
		
		// HTML formatting for the second voice:
		// - Replace [ and ] inside of syllables by HTML tags.
		// - Replace begin/end of syllables starting/ending in
		//   second-voice-mode by HTML tags. (Necessary if the [ or ] is in a
		//   former/later syllable or line)
		String  htmlStart     = "<span class='second'>";
		String  htmlStop      = "</span>";
		Pattern pattBracket   = Pattern.compile( "(\\]|\\[)", Pattern.MULTILINE ); // '[' or ']'
		boolean isSecondVoice = false;
		for (TreeMap<Long, String> line : lyrics.values()) {
			
			for (Entry<Long, String> sylEntry : line.entrySet()) {
				long         tick        = sylEntry.getKey();
				String       syllable    = sylEntry.getValue();
				boolean      mustModify  = false;
				StringBuffer modifiedSyl = new StringBuffer();
				
				// '[' is in a former syllable? - start in second voice mode
				if (isSecondVoice) {
					mustModify = true;
					modifiedSyl.append( htmlStart );
				}
				
				// replace [ and ] - but ignore nested structures
				Matcher matcher = pattBracket.matcher( syllable );
				while (matcher.find()) {
					String bracket     = matcher.group();
					String htmlReplStr = null;
					if ("[".equals(bracket) && ! isSecondVoice) {
						isSecondVoice = true;
						htmlReplStr   = htmlStart;
					}
					else if ("]".equals(bracket) && isSecondVoice) {
						isSecondVoice = false;
						htmlReplStr   = htmlStop;
					}
					
					// replacement necessary?
					if (htmlReplStr != null) {
						mustModify = true;
						
						// append everything until (including) the replacement
						// to the modified syllable
						matcher.appendReplacement( modifiedSyl, Matcher.quoteReplacement(htmlReplStr) );
					}
				}
				
				// save the modified syllable
				if (mustModify) {
					
					// append the rest of the syllable
					matcher.appendTail( modifiedSyl );
					
					// ']' is in a later syllable? - close second voice wrapping
					if (isSecondVoice) {
						modifiedSyl.append( htmlStop );
					}
					
					// replace the syllable
					line.put( tick, modifiedSyl.toString() );
				}
			}
		}
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
	 * Returns the lyrics including formatting to be displayed
	 * at the given tick.
	 * 
	 * @param tick  Tickstamp in the MIDI sequence.
	 * @return  the lyrics to be displayed.
	 */
	public static String getLyrics(long tick) {
		
		if (null == lyrics) {
			return "";
		}
		
		// prepare text
		StringBuilder text = new StringBuilder(
			  "<html><head><style>"
			+ "body {"
			+     "width: "     + PlayerView.KAR_WIDTH     + "; " // "width: 100%" doesn't work
			+     "font-size: " + PlayerView.KAR_FONT_SIZE + "; "
			+     "color: #"    + Laf.COLOR_KAR_1_PAST  + "; "
			+ "}"
			+ ".future { color: #"        + Laf.COLOR_KAR_1_FUTURE + "; } "
			+ ".second { color: #"        + Laf.COLOR_KAR_2_PAST   + "; } "
			+ ".future_second { color: #" + Laf.COLOR_KAR_2_FUTURE + "; } "
			+ "</style></head><body>"
		);
		
		// collect past lines to be shown
		TreeSet<Long> lineTicks = new TreeSet<>();
		long          loopTick  = tick;
		PAST_LINE:
		for (int i = 0; i < PlayerView.KAR_PAST_LINES; i++) {
			Long pastTick = lyrics.floorKey( loopTick );
			if (null == pastTick) {
				break PAST_LINE;
			}
			lineTicks.add( pastTick );
			loopTick = pastTick - 1;
		}
		
		// collect future lines to be shown
		loopTick = tick;
		FUTURE_LINE:
		while (lineTicks.size() < PlayerView.KAR_TOTAL_LINES) {
			Long futureTick = lyrics.ceilingKey( loopTick );
			if (null == futureTick) {
				break FUTURE_LINE;
			}
			lineTicks.add( futureTick );
			loopTick = futureTick + 1;
		}
		
		// process lines
		boolean isPast = true;
		for (long lineTick : lineTicks) {
			TreeMap<Long, String> line = lyrics.get( lineTick );
			
			// process syllables
			for (Entry<Long, String> sylEntry : line.entrySet()) {
				long   sylTick  = sylEntry.getKey();
				String syllable = sylEntry.getValue();
				
				// switch from past to future?
				if (isPast && sylTick > tick) {
					isPast = false;
					text.append( "<span class='future'>" );
				}
				
				// Adjust second voice CSS class for future syllables. That's
				// needed because CSS class nesting doesn't work in swing.
				// So this is not supported:
				// '<style>.future .second { color: ...; }</style>'
				if (! isPast) {
					// <span class='second'> --> <span class='future_second'>
					// necessary because nesting CSS classes doesn't work in swing
					syllable = karPattSecond.matcher(syllable).replaceAll( "<span class='future_second'>" );
				}
				
				// must alert?
				if (! isPast && sylTick - tick <= karPreAlertTicks) {
					text.append( "<i>" + syllable + "</i>" );
				}
				else {
					text.append( syllable );
				}
			}
		}
		text.append( "</span></body></html>" );
		
		return text.toString();
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
		Entry<Long, Byte[]> entry = instrumentHistory.get( channel ).floorEntry( tick );
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
		Entry<Long, Byte[]> entry = channelParamHistory.get( channel ).floorEntry( tick );
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
		Entry<Long, Byte> msbEntry = pitchBendSensMsbHistory.get( channel ).floorEntry( tick );
		Entry<Long, Byte> lsbEntry = pitchBendSensLsbHistory.get( channel ).floorEntry( tick );
		float msb = msbEntry.getValue();
		float lsb = lsbEntry.getValue();
		return msb + (lsb / 128f);
	}
	
	/**
	 * Analyzes the software version string from the MIDI sequence.
	 * 
	 * @param text    software version string.
	 */
	private static void retrieveSoftwareVersion(String text) {
		
		HashMap<String, String> metaInfo = (HashMap<String, String>) sequenceInfo.get( "meta_info" );
		metaInfo.put( "software", text );
		
		// minor version?
		Pattern patternMV = Pattern.compile( ".+\\.(\\d{10})$" );
		Matcher matcherMV = patternMV.matcher( text );
		if (matcherMV.matches()) {
			int  minor     = Integer.parseInt( matcherMV.group(1) );
			Date timestamp = new Date( minor * 1000L );
			SimpleDateFormat formatter    = new SimpleDateFormat( Dict.get(Dict.TIMESTAMP_FORMAT) );
			String           softwareDate = formatter.format( timestamp );
			metaInfo.put( "software_date", softwareDate );
		}
	}
	
	/**
	 * Can be used for replacing MIDI messages for debugging reasons.
	 * 
	 * This method is only used if {@link #DEBUG_MODE} is **true**.
	 * 
	 * @param msg    The original message.
	 * @param tick   The tickstamp when the message occurs.
	 * @return the replaced message.
	 */
	private static final MidiMessage replaceMessage(MidiMessage msg, long tick) {
		
		// only replace messages in a certain tick range
		try {
			
			// fake short messages
			if (tick > 10000 && tick < 20000) {
				if (tick < 12000)
					msg = new ShortMessage( ShortMessage.ACTIVE_SENSING );
				else if (tick < 14000)
					msg = new ShortMessage( 0xFB ); // system realtime: continue
				else if (tick < 16000)
					msg = new ShortMessage( 0xF7 ); // system common: end of sysex
				else if (tick < 18000)
					msg = new ShortMessage( 0xB0, 0x65, 0x00 ); // rpn MSB
				else
					msg = new ShortMessage( 0xB1, 0x65, 0x01 ); // rpn MSB
			}
			
			// fake sysex messages
			if (tick > 20000 && tick < 30000) {
				byte[] content = {
					(byte) 0xF0,	// status
					0x41,			// vendor
					0x7F,			// channel
					0x08,			// sub 1
					0x00,			// sub 2
					0x66,			// data
					(byte) 0xF7,	// end of sysex
				};
				if (tick < 22000) {
					// nothing more to do
				}
				else if (tick < 24000) {
					content[ 1 ] = 0x7E;
				}
				else if (tick < 26000) {
					content[ 1 ] = 0x7F;
				}
				else {
					content[ 1 ] = 0x7D;
				}
				
				msg = new SysexMessage( content, content.length );
			}
			
			// fake meta messages
			if (tick > 30000 && tick < 40000) {
				int    type = 0x05; // lyrics
				byte[] content;
				try {
					if (tick < 32000) {
						String text = "faked DEFAULT lyrics for testing äöü ÄÖÜ § 中中中中";
						content     = text.getBytes();
					}
					else if (tick < 34000) {
						String text = "faked ISO-8859-1 lyrics for testing äöü ÄÖÜ § 中中中中";
						content     = text.getBytes( "ISO-8859-1" );
					}
					else {
						String text = "faked UTF8 lyrics for testing äöü ÄÖÜ § 中中中中";
						content     = text.getBytes( Charset.forName("UTF-8") );
					}
				}
				catch (UnsupportedEncodingException e) {
					String text = "faked DEFAULT lyrics after UnsupportedEncodingException for testing äöü ÄÖÜ § 中中中中";
					content     = text.getBytes();
				}
				if (tick > 36000) {
					type = 0x7F;     // sequencer specific
					content = new byte[ 3 ];
					if (tick < 38000) {
						content[ 0 ] = 0x41; // roland
						content[ 1 ] = 0x01;
						content[ 2 ] = 0x02;
					}
					else if (tick < 39000) {
						content[ 0 ] = 0x42; // korg
						content[ 1 ] = 0x03;
						content[ 2 ] = 0x04;
					}
					else {
						content = new byte[ 6 ];
						content[ 0 ] = 0x00;
						content[ 1 ] = 0x00;
						content[ 2 ] = 0x41; // microsoft
						content[ 3 ] = 0x06;
						content[ 4 ] = 0x07;
						content[ 5 ] = 0x08;
					}
				}
				 
				msg = new MetaMessage( type, content, content.length );
			}
		}
		catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
		
		return msg;
	}
}
