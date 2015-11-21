/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.midi;

import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Track;

import com.sun.media.sound.MidiUtils;

/**
 * This class analyzes a MIDI sequence and collects informations from it.
 * These informations can be displayed later by the {@link InfoView}
 * 
 * @author Jan Trukenmüller
 */
public class SequenceAnalyzer {
	
	private static Sequence sequence = null;
	private static String   fileType = null;
	private static HashMap<String, Object> streamInfo = null;
	
	private static TreeMap<Integer, TreeMap<Integer, Integer>> keysByChannel     = null;
	private static TreeMap<Integer, TreeSet<Integer>>          programsByChannel = null;
	
	/**
	 * This class is only used statically so a public constructor is not needed.
	 */
	private SequenceAnalyzer() {
	}
	
	/**
	 * Analyzes the given MIDI stream and collects informations about it.
	 * 
	 * @param seq  The MIDI sequence to be analyzed.
	 * @param type The file type where the stream originally comes from
	 *             -- **mid** for MIDI or **midica** for MidicaPL.
	 */
	public static void analyze ( Sequence seq, String type ) {
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
		if ( null == streamInfo )
			return new HashMap<String, Object>();
		return streamInfo;
	}
	
	/**
	 * Initializes the internal data structures so that they are ready to
	 * be filled with sequence informations during the parsing process.
	 */
	private static void init () {
		
		// initialize simpler data structures
		streamInfo = new HashMap<String, Object>();
		streamInfo.put( "resolution",       sequence.getResolution()        );
		streamInfo.put( "used_channels",    new TreeSet<Integer>()          );
		streamInfo.put( "used_banks",       new TreeSet<String>()           );
		streamInfo.put( "banks_by_channel", new TreeMap<Integer, String>()  );
		streamInfo.put( "tempo_mpq",        new TreeMap<Long, Integer>()    );
		streamInfo.put( "tempo_bpm",        new TreeMap<Long, Integer>()    );
		streamInfo.put( "parser_type",      fileType                        );
		streamInfo.put( "ticks",            sequence.getTickLength()        );
		
		// initialize more complicated data structures
		keysByChannel     = new TreeMap<Integer, TreeMap<Integer, Integer>>();
		programsByChannel = new TreeMap<Integer, TreeSet<Integer>>();
		streamInfo.put( "keys_by_channel",     keysByChannel     );
		streamInfo.put( "programs_by_channel", programsByChannel );
		
		// stream length in a human-readable time string
		long microseconds = sequence.getMicrosecondLength();
		String time       = MidiDevices.microsecondsToTimeString( microseconds );
		streamInfo.put( "time_length", time );
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
			if ( volume > 0 ) {
				TreeMap<Integer, Integer> keys = keysByChannel.get( channel );
				if ( null == keys ) {
					keys = new TreeMap<Integer, Integer>();
					keysByChannel.put( channel, keys );
				}
				Integer keyCount = keys.get( note );
				if ( null == keyCount )
					keyCount = 1;
				else
					keyCount++;
				keys.put( note, keyCount );
			}
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
			TreeMap<Long, Integer> tempoMpq = (TreeMap<Long, Integer>) streamInfo.get( "tempo_mpq" );
			tempoMpq.put( tick, mpq );
			TreeMap<Long, Integer> tempoBpm = (TreeMap<Long, Integer>) streamInfo.get( "tempo_bpm" );
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
	 * Adds last informations to the info data structure about the MIDI stream.
	 * 
	 * @param type "mid" or "midica", depending on the parser class.
	 */
	private static void postprocess() {
		
		// average, min and max tempo
		TreeMap<Long, Integer> tempoMpq = (TreeMap<Long, Integer>) streamInfo.get( "tempo_mpq" );
		TreeMap<Long, Integer> tempoBpm = (TreeMap<Long, Integer>) streamInfo.get( "tempo_bpm" );
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
		long tickLength = (Long) streamInfo.get( "ticks" );
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
		streamInfo.put( "tempo_bpm_avg", String.format("%.2f", avgBpm) );
		streamInfo.put( "tempo_bpm_min", Integer.toString(minBpm) );
		streamInfo.put( "tempo_bpm_max", Integer.toString(maxBpm) );
		streamInfo.put( "tempo_mpq_avg", String.format("%.1f", avgMpq) );
		streamInfo.put( "tempo_mpq_min", Integer.toString(minMpq) );
		streamInfo.put( "tempo_mpq_max", Integer.toString(maxMpq) );
	}
}
