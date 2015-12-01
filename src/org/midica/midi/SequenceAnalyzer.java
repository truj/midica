/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.midi;

import java.util.HashMap;
import java.util.Map.Entry;
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
import org.midica.file.ParseException;

import com.sun.media.sound.MidiUtils;

/**
 * This class analyzes a MIDI sequence and collects information from it.
 * These informations can be displayed later by the {@link InfoView}
 * 
 * It also adds marker events to the sequence at each tick where the
 * channel activity changes for at least one channel.
 * 
 * @author Jan Trukenmüller
 */
public class SequenceAnalyzer {
	
	private static Sequence sequence = null;
	private static String   fileType = null;
	private static HashMap<String, Object> sequenceInfo = null;
	
	/**                    channel     --   note  -- usage count */
	private static TreeMap<Integer, TreeMap<Integer, Integer>> keysByChannel = null;
	
	/**                    channel    --    program number */
	private static TreeMap<Integer, TreeSet<Integer>> programsByChannel = null;
	
	/**                   channel    --     note      --     tick -- on/off */
	private static TreeMap<Integer, TreeMap<Integer, TreeMap<Long, Boolean>>> noteOnOffByChannel = null;
	
	/**                    channel  --   tick -- number of keys pressed at this time */
	private static TreeMap<Byte, TreeMap<Long, Integer>> activityByChannel = null;
	
	/**                    tick     --   channel */
	private static TreeMap<Long, TreeSet<Byte>> markers = null;
	
	/**                    tick */
	private static TreeSet<Long> markerTicks = null;
	
	/**
	 * This class is only used statically so a public constructor is not needed.
	 */
	private SequenceAnalyzer() {
	}
	
	/**
	 * Analyzes the given MIDI stream and collects informations about it.
	 * Adds marker events for channel activity changes.
	 * 
	 * @param seq  The MIDI sequence to be analyzed.
	 * @param type The file type where the stream originally comes from
	 *             -- **mid** for MIDI or **midica** for MidicaPL.
	 * @throws ParseException if a marker event cannot be created during the postprocessing
	 */
	public static void analyze ( Sequence seq, String type ) throws ParseException {
		sequence = seq;
		fileType = type;
		
		// initialize data structures
		init();
		
		// fill data structures
		parse();
		
		// add statistic information to the data structures
		postprocess();
	}
	
	/**
	 * Returns the informations that have been collected while
	 * analyzing the MIDI sequence.
	 * 
	 * If no MIDI stream has been loaded: returns an empty data structure.
	 * 
	 * @return MIDI stream info.
	 */
	public static HashMap<String, Object> getStreamInfo() {
		if ( null == sequenceInfo )
			return new HashMap<String, Object>();
		return sequenceInfo;
	}
	
	/**
	 * Initializes the internal data structures so that they are ready to
	 * be filled with sequence informations during the parsing process.
	 */
	private static void init() {
		
		// initialize data structures for the sequence info
		sequenceInfo = new HashMap<String, Object>();
		sequenceInfo.put( "resolution",       sequence.getResolution()        );
		sequenceInfo.put( "used_channels",    new TreeSet<Integer>()          );
		sequenceInfo.put( "used_banks",       new TreeSet<String>()           );
		sequenceInfo.put( "banks_by_channel", new TreeMap<Integer, String>()  );
		sequenceInfo.put( "tempo_mpq",        new TreeMap<Long, Integer>()    );
		sequenceInfo.put( "tempo_bpm",        new TreeMap<Long, Integer>()    );
		sequenceInfo.put( "parser_type",      fileType                        );
		sequenceInfo.put( "ticks",            sequence.getTickLength()        );
		keysByChannel      = new TreeMap<Integer, TreeMap<Integer, Integer>>();
		programsByChannel  = new TreeMap<Integer, TreeSet<Integer>>();
		sequenceInfo.put( "keys_by_channel",     keysByChannel     );
		sequenceInfo.put( "programs_by_channel", programsByChannel );
		long microseconds = sequence.getMicrosecondLength();
		String time       = MidiDevices.microsecondsToTimeString( microseconds );
		sequenceInfo.put( "time_length", time );
		
		// init data structures for the channel activity
		activityByChannel  = new TreeMap<Byte, TreeMap<Long, Integer>>();
		noteOnOffByChannel = new TreeMap<Integer, TreeMap<Integer, TreeMap<Long, Boolean>>>();
		markerTicks        = new TreeSet<Long>();
		markers            = new TreeMap<Long, TreeSet<Byte>>();
	}
	
	/**
	 * Parses the MIDI sequence track by track and event by event and
	 * collects informations.
	 */
	private static void parse() {
		TRACK:
		for ( Track t : sequence.getTracks() ) {
			EVENT:
			for ( int i=0; i < t.size(); i++ ) {
				MidiEvent   event = t.get( i );
				long        tick  = event.getTick();
				MidiMessage msg   = event.getMessage();
				
				if ( msg instanceof MetaMessage ) {
					processMetaMessage( (MetaMessage) msg, tick );
				}
				else if ( msg instanceof ShortMessage ) {
					processShortMessage( (ShortMessage) msg, tick );
				}
				else if ( msg instanceof SysexMessage ) {
					processSysexMessage( (SysexMessage) msg, tick );
				}
				else {
				}
			}
		}
	}
	
	/**
	 * Retrieves informations from short messages.
	 * 
	 * @param msg   Short message
	 * @param tick  Tickstamp
	 */
	private static void processShortMessage( ShortMessage msg, long tick ) {
		int cmd     = msg.getCommand();
		int channel = msg.getChannel();
		
		// PROGRAM CHANGE
		if ( ShortMessage.PROGRAM_CHANGE == cmd ) {
			int instrNum = msg.getData1();
			TreeSet<Integer> programs = programsByChannel.get( channel );
			if ( null == programs ) {
				programs = new TreeSet<Integer>();
				programsByChannel.put( channel, programs );
			}
			programs.add( instrNum );
		}
		
		// NOTE ON or OFF
		else if ( ShortMessage.NOTE_ON == cmd ) {
			int note    = msg.getData1();
			int volume  = msg.getData2();
			
			// ON
			if ( volume > 0 )
				addNoteOn( tick, channel, note );
			else
				addNoteOff( tick, channel, note );
		}
		
		// NOTE OFF
		else if ( ShortMessage.NOTE_OFF == cmd ) {
			int note = msg.getData1();
			addNoteOff( tick, channel, note );
		}
		
		// another channel command
		else {
			
		}
		
		// TODO: collect event information
	}
	
	/**
	 * Retrieves informations from meta messages.
	 * 
	 * @param msg   Meta message
	 * @param tick  Tickstamp
	 */
	private static void processMetaMessage( MetaMessage msg, long tick ) {
		int    type    = msg.getType();
		int    length  = msg.getLength();
		byte[] message = msg.getMessage();
		
		// TEMPO
		if ( MidiListener.META_SET_TEMPO == type ) {
			int mpq = MidiUtils.getTempoMPQ( msg );
			int bpm = (int) MidiUtils.convertTempo( mpq );
			TreeMap<Long, Integer> tempoMpq = (TreeMap<Long, Integer>) sequenceInfo.get( "tempo_mpq" );
			tempoMpq.put( tick, mpq );
			TreeMap<Long, Integer> tempoBpm = (TreeMap<Long, Integer>) sequenceInfo.get( "tempo_bpm" );
			tempoBpm.put( tick, bpm );
		}
		
		// TODO: collect event information
	}
	
	/**
	 * Retrieves informations from SysEx messages.
	 * 
	 * @param msg   SysEx message
	 * @param tick  Tickstamp
	 */
	private static void processSysexMessage(SysexMessage msg, long tick) {
		// TODO: collect event information
	}
	
	/**
	 * Adds a detected **note-on** event to the data structures.
	 * 
	 * @param tick     The tickstamp when this event occurred.
	 * @param channel  The MIDI channel number.
	 * @param note     The note number.
	 */
	private static void addNoteOn( long tick, int channel, int note ) {
		
		// totally used keys: keysByChannel
		TreeMap<Integer, Integer> totalKeys = keysByChannel.get( channel );
		if ( null == totalKeys ) {
			totalKeys = new TreeMap<Integer, Integer>();
			keysByChannel.put( channel, totalKeys );
		}
		Integer totalKeyCount = totalKeys.get( note );
		if ( null == totalKeyCount )
			totalKeyCount = 1;
		else
			totalKeyCount++;
		totalKeys.put( note, totalKeyCount );
		
		// note history
		TreeMap<Integer, TreeMap<Long, Boolean>> noteTickOnOff = noteOnOffByChannel.get( channel );
		if ( null == noteTickOnOff ) {
			noteTickOnOff = new TreeMap<Integer, TreeMap<Long, Boolean>>();
			noteOnOffByChannel.put( channel, noteTickOnOff );
		}
		TreeMap<Long, Boolean> pressedAtTick = noteTickOnOff.get( note );
		if ( null == pressedAtTick ) {
			pressedAtTick = new TreeMap<Long, Boolean>();
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
		TreeMap<Long, Integer> activityAtTick = activityByChannel.get( (byte) channel );
		if ( activityAtTick == null ) {
			activityAtTick = new TreeMap<Long, Integer>();
			activityByChannel.put( (byte) channel, activityAtTick );
		}
		else {
			Entry<Long, Integer> lastActivity = activityAtTick.floorEntry( tick );
			if ( lastActivity != null )
				lastChannelActivity = lastActivity.getValue();
		}
		activityAtTick.put( tick, lastChannelActivity + 1 );
		
		// prepare marker event
		markerTicks.add( tick );
	}
	
	/**
	 * Adds a detected **note-off** event to the data structures.
	 * 
	 * @param tick     The tickstamp when this event occurred.
	 * @param channel  The MIDI channel number.
	 * @param note     The note number.
	 */
	private static void addNoteOff( long tick, int channel, int note ) {
		
		// check if the released key has been pressed before
		TreeMap<Integer, TreeMap<Long, Boolean>> noteTickOnOff = noteOnOffByChannel.get( channel );
		if ( null == noteTickOnOff ) {
			noteTickOnOff = new TreeMap<Integer, TreeMap<Long, Boolean>>();
			noteOnOffByChannel.put( channel, noteTickOnOff );
		}
		TreeMap<Long, Boolean> pressedAtTick = noteTickOnOff.get( note );
		if ( null == pressedAtTick ) {
			pressedAtTick = new TreeMap<Long, Boolean>();
			noteTickOnOff.put( note, pressedAtTick );
		}
		boolean wasPressedBefore = false;
		Entry<Long, Boolean> wasPressed = pressedAtTick.floorEntry( tick );
		if ( null != wasPressed ) {
			wasPressedBefore = wasPressed.getValue();
		}
		if ( ! wasPressedBefore )
			return;
		
		// mark as released
		pressedAtTick.put( tick, false );
		
		// channel activity
		TreeMap<Long, Integer> activityAtTick = activityByChannel.get( (byte) channel );
		if ( null == activityAtTick ) {
			activityAtTick = new TreeMap<Long, Integer>();
			activityByChannel.put( (byte) channel, activityAtTick );
		}
		Entry<Long, Integer> lastActivity = activityAtTick.floorEntry( tick );
		if ( null == lastActivity ) {
			// A key was released before it has been pressed for the very first time.
			return;
		}
		
		// decrement activity
		Integer lastActivityCount = lastActivity.getValue();
		if ( lastActivityCount < 1 ) {
			// should never happen
			return;
		}
		activityAtTick.put( tick, lastActivityCount - 1 );
		
		// prepare marker event
		markerTicks.add( tick );
	}
	
	/**
	 * Adds last informations to the info data structure about the MIDI stream.
	 * Adds marker events to the stream.
	 * 
	 * @param type "mid" or "midica", depending on the parser class.
	 * @throws ParseException if the marker events cannot be added to the MIDI sequence.
	 */
	private static void postprocess() throws ParseException {
		
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
		for ( long tick : tempoBpm.keySet() ) {
			
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
			if ( tick > 0 && 0 == minBpm && 0 == maxBpm ) {
				minBpm = MidiDevices.DEFAULT_TEMPO_BPM;
				maxBpm = MidiDevices.DEFAULT_TEMPO_BPM;
				minMpq = MidiDevices.DEFAULT_TEMPO_MPQ;
				maxMpq = MidiDevices.DEFAULT_TEMPO_MPQ;
			}
			if ( 0 == minBpm || minBpm > newBpm )
				minBpm = newBpm;
			if ( 0 == maxBpm || maxBpm < newBpm )
				maxBpm = newBpm;
			if ( 0 == minMpq || minMpq > newMpq )
				minMpq = newMpq;
			if ( 0 == maxMpq || maxMpq < newMpq )
				maxMpq = newMpq;
		}
		long tickLength = (Long) sequenceInfo.get( "ticks" );
		long tickDiff = tickLength - lastTick;
		bpmProduct   += tickDiff * lastBpm;
		mpqProduct   += tickDiff * lastMpq;
		if ( 0 == minBpm )
			minBpm = lastBpm;
		if ( 0 == maxBpm )
			maxBpm = lastBpm;
		if ( 0 == minMpq )
			minMpq = lastMpq;
		if ( 0 == maxMpq )
			maxMpq = lastMpq;
		double avgBpm = (double) bpmProduct / tickLength;
		double avgMpq = (double) mpqProduct / tickLength;
		sequenceInfo.put( "tempo_bpm_avg", String.format("%.2f", avgBpm) );
		sequenceInfo.put( "tempo_bpm_min", Integer.toString(minBpm) );
		sequenceInfo.put( "tempo_bpm_max", Integer.toString(maxBpm) );
		sequenceInfo.put( "tempo_mpq_avg", String.format("%.1f", avgMpq) );
		sequenceInfo.put( "tempo_mpq_min", Integer.toString(minMpq) );
		sequenceInfo.put( "tempo_mpq_max", Integer.toString(maxMpq) );
		
		// markers
		for ( long tick : markerTicks ) {
			for ( byte channel : activityByChannel.keySet() ) {
				if ( activityByChannel.get(channel).containsKey(tick) ) {
					TreeSet<Byte> channelsAtTick = markers.get( tick );
					if ( null == channelsAtTick ) {
						channelsAtTick = new TreeSet<Byte>();
						markers.put( tick, channelsAtTick );
					}
					channelsAtTick.add( channel );
				}
			}
		}
		try {
			SequenceCreator.addMarkers( markers );
		}
		catch ( InvalidMidiDataException e ) {
			throw new ParseException( Dict.get(Dict.ERROR_ANALYZE_POSTPROCESS) + e.getMessage() );
		}
		
		// TODO: delete
		System.out.println(keysByChannel);
		System.out.println(activityByChannel);
		System.out.println(noteOnOffByChannel);
		System.out.println(markerTicks);
		System.out.println(markers);
	}
	
	/**
	 * Calculates the channel activity for the given channel in the given tick.
	 * 
	 * @param channel  MIDI channel
	 * @param tick     tickstamp of the sequence
	 * @return   the channel activity
	 */
	public static boolean getChannelActivity( byte channel, long tick ) {
		
		// get ticks of this channel
		TreeMap<Long, Integer> ticksInChannel = activityByChannel.get( channel );
		if ( null == ticksInChannel )
			// channel not used at all
			return false;
		
		// get the last activity
		Entry<Long, Integer> activityState = ticksInChannel.floorEntry( tick );
		if ( null == activityState )
			// nothing happened in the channel so far
			return false;
		
		// inactive?
		if ( 0 == activityState.getValue() )
			return false;
		
		// active
		return true;
	}
}
