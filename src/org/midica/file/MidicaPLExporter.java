/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import org.midica.config.Dict;
import org.midica.midi.MidiDevices;
import org.midica.midi.MidiListener;

import com.sun.media.sound.MidiUtils;

/**
 * This class is used to export the currently loaded MIDI sequence as a MidicaPL source file.
 * 
 * @author Jan Trukenmüller
 */
public class MidicaPLExporter extends Exporter {
	
	/* *****************
	 * constants
	 *******************/
	
	/* *****************
	 * class fields
	 *******************/
	
	String newline = System.getProperty( "line.separator" );
	
	private static int                                                 resolution       = 0;
	private static int                                                 targetResolution = 480;
	private static ExportResult                                        exportResult     = null;
	
	private static HashMap<Integer, Instrument>                        instruments      = null;
	private static ArrayList<ArrayList<Long>>                          instrumentChange = null;
	private static TreeMap<Long, HashMap<String, String>>              globalEvents     = null;
	private static boolean[]                                           isChannelUsed    = null;
	private static TreeMap<Long, ArrayList<HashMap<String, String>>>[] channelEvents    = null;
	private static TreeMap<Long, Integer>                              globalActivity   = null;
	private static TreeMap<Long, Integer>[]                            channelActivity  = null;
	private static TreeMap<Long, HashMap<String, String>>[][]          noteEvent        = null;
	
	private static TreeMap<Integer, String>                            noteLength       = null;
	
	/* *******************
	 * instance fields
	 *********************/
	
	/**
	 * Creates a new MidicaPL exporter.
	 */
	public MidicaPLExporter() {
	}
	
	/**
	 * Exports a MidicaPL source file.
	 * 
	 * @param   file             MidicaPL source file.
	 * @return                   Warnings that occured during the export.
	 * @throws  ExportException  If the file can not be exported correctly.
	 */
    public ExportResult export( File file ) throws ExportException {
		
    	exportResult = new ExportResult( true );
    	
    	try {
			
			// create file writer and store it in this.writer
			if ( ! createFile(file) )
				return new ExportResult( false );
			
			// open file for writing
    		BufferedWriter writer = new BufferedWriter( new FileWriter( file ) );
			
    		// initialize data structures
    		instruments      = new HashMap<Integer, Instrument>();
    		instrumentChange = new ArrayList<ArrayList<Long>>();
    		globalEvents     = new TreeMap<Long, HashMap<String,String>>();
    		globalActivity   = new TreeMap<Long, Integer>();
    		isChannelUsed    = new boolean[ 16 ];
    		channelEvents    = new TreeMap[ 16 ];
    		channelActivity  = new TreeMap[ 16 ];
    		noteEvent        = new TreeMap[ 16 ][ 128 ];
    		for ( byte channel = 0; channel < 16; channel++ ) {
    			isChannelUsed[ channel ]   = false;
    			channelEvents[ channel ]   = new TreeMap<Long, ArrayList<HashMap<String,String>>>();
    			channelActivity[ channel ] = new TreeMap<Long, Integer>();
    			
    			TreeMap<Long, HashMap<String, String>>[] channelNoteActivity = new TreeMap[ 128 ];
    			for ( int note = 0; note < 128; note++ ) {
    				channelNoteActivity[ note ] = new TreeMap<Long, HashMap<String, String>>();
    			}
    			noteEvent[ channel ] = channelNoteActivity;
    		}
    			
    		
    		// make sure that the syntax configuration is up to date
    		MidicaPLParser.refreshSyntax();
    		
    		
    		
    		
    		
    		// fill data structures
    		resolution = readSequence();
    		
    		// calculate what tick length corresponds to what note length
        	noteLength = initNoteLengths();
    		
        	
    		preprocessEvents();
    		
    		// create MidicaPL string from the data structures and write it into the file
    		writer.write( createMidicaPL() );
			
    		
			// parse line by line
//			while ( null != (line = bw.readLine()) ) {
//				lineNumber++;
//				try {
//					parseLine( line );
//				}
//				catch ( ExportException e ) {
//					// Add file name and line number to exception and throw it again
//					// but only if this is not yet done.
//					// If this information is already available than it comes from
//					// another parser instance. In this case we must not overwrite it.
//					if ( 0 == e.getLineNumber() )
//						e.setLineNumber( lineNumber );
//					if ( null == e.getFileName() )
//						e.setFileName( file.getCanonicalPath() );
//					throw e;
//				}
//			}
			
			writer.close();
		}
		catch ( FileNotFoundException e ) {
			e.printStackTrace();
		}
		catch ( IOException e ) {
			e.printStackTrace();
		}
		
		return exportResult;
	}
	
	/**
	 * Reads the currently loaded MIDI sequence and stores the contained events into
	 * several data structures.
	 * The resulting data structures are:
	 * 
	 * TODO: fix documentation
	 * 
	 * 
	 * The resulting data structure is ordered using the following priority:
	 * 1. tickstamp
	 * 1. command (global commands first)
	 * 1. channel number
	 * 
	 * @return resolution of the loaded MIDI stream (ticks per quarter note)
	 * @throws ExportException 
	 */
	private int readSequence() throws ExportException {
		
		// parse the midi sequence
		Sequence seq = MidiDevices.getSequence();
		
		TRACK:
		for ( Track track : seq.getTracks() ) {
			
			EVENT:
			for ( int i=0; i < track.size(); i++ ) {
				MidiEvent   event = track.get( i );
				long        tick  = event.getTick();
				MidiMessage msg   = event.getMessage();
				
				// channel name?
				if ( msg instanceof MetaMessage ) {
					
				}
				
				// short message
				if ( msg instanceof ShortMessage ) {
					ShortMessage shortMsg = (ShortMessage) msg;
					int cmd     = shortMsg.getCommand();
					int channel = shortMsg.getChannel();
					int note    = shortMsg.getData1();
					int volume  = shortMsg.getData2();
					
					isChannelUsed[ channel ] = true; // mark channel as used
					
					// instrument selection?
					if ( ShortMessage.PROGRAM_CHANGE == cmd ) {
						
						// ignore instrument changes in the percussion channel
						if ( 9 == channel )
							break;
						
						Instrument instr    = instruments.get( channel );
						int        instrNum = shortMsg.getData1();
						
						// first instrument selection?
						if ( null == instr ) {
							instr = new Instrument( channel, instrNum, null, false );
							instruments.put( channel, instr );
						}
						else {
							// later instrument change
							instrumentChange.get( channel ).add( tick ); // TODO: reicht noch nicht
						}
					}
					
					// note on?
					else if ( ShortMessage.NOTE_ON == cmd && volume > 0 ) {
						addNoteEvent( tick, channel, note, true, volume );
					}
					
					// note off?
					else if ( ShortMessage.NOTE_OFF == cmd || (ShortMessage.NOTE_ON == cmd && 0 == volume) ) {
						addNoteEvent( tick, channel, note, false, 0 );
					}
					
					// something else?
					else {
						String warning = String.format( Dict.get(Dict.WARNING_IGNORED_SHORT_MESSAGE), cmd, note, volume );
						exportResult.addWarning( tick, channel, -1, warning );
					}
				}
				
				// meta message
				if ( msg instanceof MetaMessage ) {
					MetaMessage metaMsg = (MetaMessage) msg;
					int    type   = metaMsg.getType();
					int    status = metaMsg.getStatus();
					byte[] data   = metaMsg.getData();
					
					if ( MidiListener.META_SET_TEMPO == type ) {
						
						int    mpq    = MidiUtils.getTempoMPQ( metaMsg );
						int    bpm    = (int) MidiUtils.convertTempo( mpq );
						String bpmStr = Integer.toString( bpm );
						
						addGlobalEvent( tick, Dict.getSyntax(Dict.SYNTAX_BPM), bpmStr );
					}
					
				}
			}
		}
		
		return seq.getResolution();
	}
	
	/**
	 * Checks the sequence's events for consistency and adds some more information.
	 * This method is called after the events of the sequence have been read into the
	 * data structures.
	 * 
	 * TODO: fix documentation
	 * 
	 */
	private void preprocessEvents() {
		
		// check noteEvent[][] for integrity
		for ( byte channel = 0; channel < 16; channel++ ) {
			for ( int note = 0; note < 128; note++ ) {
				if ( ! noteEvent[channel][note].isEmpty() ) {
					boolean previousActivity = false;
					long    previousTick     = -1;
					
					for ( long tick : noteEvent[channel][note].keySet() ) {
						
						HashMap<String, String> event = noteEvent[ channel ][ note ].get( tick );
						int volume = Integer.parseInt( event.get("volume") );
						
						// KEY_OFF
						if ( 0 == volume ) {
							if (previousActivity) {
								// Add length information to the corresponding NOTE_ON event.
								HashMap<String, String> previousEvent = noteEvent[channel][note].get( previousTick );
								String length = Long.toString( tick - previousTick );
								previousEvent.put( "tick_length", length );
								String[] cmdLength = convertLength( tick - previousTick );
								previousEvent.put( "cmd_length", cmdLength[0] );
								previousEvent.put( "staccato",   cmdLength[1] );
								
								previousTick = tick;
							}
							else {
								String warning = String.format( Dict.get(Dict.WARNING_UNPRESSED_NOTE_RELEASED), previousTick );
								exportResult.addWarning( tick, channel, note, warning );
							}
							previousActivity = false;
						}
						else {
							if (previousActivity) {
								String warning = String.format( Dict.get(Dict.WARNING_UNRELEASED_NOTE_PRESSED), previousTick );
								exportResult.addWarning( tick, channel, note, warning );
							}
							previousActivity = true;
							previousTick     = tick;
						}
					}
				}
			}
		}
		
	}
	
	/**
	 * Creates the MidicaPL string to be written into the export file.
	 * 
	 * @return MidicaPL string to be written into the export file
	 */
	private String createMidicaPL() {
		StringBuffer output = new StringBuffer();
		
		output.append( createInstrInitBlock() );
		
		
//		for ( HashMap<String, String> cmd : globalEvents. )
//			output
		
		
		// TODO: delete
		output.append( newline );
		
		return output.toString();
	}
	
	/**
	 * Creates the initial INSTRUMENTS block.
	 * This block contains channels used in the MIDI sequence apart from the percussion channel.
	 * 
	 * @return initial INSTRUMENTS block
	 */
	private String createInstrInitBlock() {
		
		// open block
		StringBuffer block = new StringBuffer( MidicaPLParser.INSTRUMENTS + newline );
		
		// TODO: delete
		System.out.println( "Number of tracks: " + MidiDevices.getSequence().getTracks().length );
		
		// content
		for ( int channel = 0; channel < isChannelUsed.length; channel++ ) {
			
			// ignore unused channels and the percussion channel
			if ( ! isChannelUsed[channel] || 9 == channel )
				continue;
			
			System.out.println( channel );
			
			Instrument instr = instruments.get( channel );
			block.append(
				  "\t" + channel
				+ "\t" + instr.instrumentNumber
				+ "\t" + instr.instrumentName
				+ newline
			);
		}
		
		// close block
		block.append( MidicaPLParser.END + newline + newline );
		
		return block.toString();
	}
	
	/**
	 * Adds a global event to the event data structures.
	 * In the moment the only known global event is a tempo change to e new BPM value.
	 * 
	 * TODO: fix documentation
	 * 
	 * @param tick     Tickstamp of the event.
	 * @param cmd      Command
	 * @param options  Further options of the event.
	 */
	private void addGlobalEvent( long tick, String cmd, String options ) {
		
		HashMap<String, String> commands;
		
		// are there already other global command at this tickstamp?
		if ( globalEvents.containsKey(tick) )
			commands = globalEvents.get( tick );
		
		// it's the first global command at this tickstamp
		else {
			commands = new HashMap<String, String>();
			globalEvents.put( tick, commands );
		}
		
		// add the command
		commands.put( cmd, options );
	}
	
	/**
	 * Adds a NOTE_ON or NOTE_OFF event to the channel's event data structure.
	 * 
	 * TODO: add documentation for ExportException
	 * 
	 * @param tick     Tickstamp of the event
	 * @param channel  Channel number
	 * @param note     Note number
	 * @param onOff    true: NOTE_ON, false: NOTE_OFF
	 * @param volume   velocity of the note event in case of NOTE_ON (or 0 in case of NOTE_OFF)
	 * @throws ExportException
	 */
	private void addNoteEvent( long tick, int channel, int note, boolean onOff, int volume ) throws ExportException {
		
		if ( false == onOff )
			volume = 0;
		
		// has the note (already) been pressed or released in the same tick and channel?
		boolean hasOtherEvent = noteEvent[ channel ][ note ].containsKey( tick );
		if (hasOtherEvent) {
			int otherVol = Integer.parseInt( noteEvent[channel][note].get(tick).get("volume") );
			String warning = String.format( Dict.get(Dict.WARNING_SAME_NOTE_IN_SAME_TICK), volume, otherVol );
			exportResult.addWarning( tick, channel, note, warning );
			
			// keep the louder note
			if ( volume <= otherVol )
				return;
		}
		
		// add the note event
		HashMap<String, String> event = new HashMap<String, String>();
		event.put( "volume", Integer.toString(volume) );
		noteEvent[ channel ][ note ].put( tick, event );
		
		
		
//		ArrayList<HashMap<String, String>> events;
//		
//		// are there already other events at this channel and tickstamp?
//		if ( channelEvents[channel].containsKey(tick) )
//			events = channelEvents[ channel ].get( tick );
//		
//		// it's the first event at this channel/tickstamp
//		else {
//			events = new ArrayList<HashMap<String, String>>();
//			channelEvents[ channel ].put( tick, events );
//		}
		
		// add the command
		// TODO: ...
//		HashMap<String, String> options;
//		events.put( cmd, options );
	}
	
	/**
	 * Converts the given number of ticks into the MidicaPL note length information.
	 * 
	 * @param ticks note length in ticks
	 * @return array containing 2 elements:
	 *         - note length information in MidicaPL format
	 *         - staccato value
	 */
	private String[] convertLength( long ticks ) {
		
		// note too long?
		if ( ticks > noteLength.lastKey() ) {
			// TODO: handle this case. Create a warning and set tick to maximum.
		}
		
		// calculate
		int fullTicks = noteLength.ceilingKey( (int)ticks );
		String cmd    = noteLength.get( fullTicks );
		
		// pack the result
		String[] result = new String[ 2 ];
		result[ 0 ] = cmd;
		result[ 1 ] = Integer.toString( fullTicks - (int)ticks );
		
		return result;
	}
	
	/**
	 * Calculates what tick length corresponds to what note length.
	 * That depends on the resolution of the current MIDI stream.
	 * 
	 * @return Mapping between tick length and note length.
	 */
	private TreeMap<Integer, String> initNoteLengths() {
		
		String triplet = Dict.getSyntax( Dict.SYNTAX_TRIPLET );
		String dot     = Dict.getSyntax( Dict.SYNTAX_DOT     );
		String m2      = Dict.getSyntax( Dict.SYNTAX_M2      );
		String m4      = Dict.getSyntax( Dict.SYNTAX_M4      );
		String m8      = Dict.getSyntax( Dict.SYNTAX_M8      );
		String m16     = Dict.getSyntax( Dict.SYNTAX_M16     );
		String m32     = Dict.getSyntax( Dict.SYNTAX_M32     );
		
		TreeMap<Integer, String> noteLength = new TreeMap<Integer, String>();
		
		// 32th
		int length32t = calculateTicks( 2, 8 * 3 ); // inside a triplet
		int length32  = calculateTicks( 1, 8     ); // normal length
		int length32d = calculateTicks( 3, 8 * 2 ); // dotted length
		noteLength.put( length32t, Integer.toString(32) + triplet ); // triplet
		noteLength.put( length32,  Integer.toString(32)           ); // normal
		noteLength.put( length32d, Integer.toString(32) + dot     ); // dotted
		
		// 16th
		int length16t = calculateTicks( 2, 4 * 3 );
		int length16  = calculateTicks( 1, 4     );
		int length16d = calculateTicks( 3, 4 * 2 );
		noteLength.put( length16t, Integer.toString(16) + triplet );
		noteLength.put( length16,  Integer.toString(16)           );
		noteLength.put( length16d, Integer.toString(16) + dot     );
		
		// 8th
		int length8t = calculateTicks( 2, 2 * 3 );
		int length8  = calculateTicks( 1, 2     );
		int length8d = calculateTicks( 3, 2 * 2 );
		noteLength.put( length8t, Integer.toString(8) + triplet );
		noteLength.put( length8,  Integer.toString(8)           );
		noteLength.put( length8d, Integer.toString(8) + dot     );
		
		// quarter
		int length4t = calculateTicks( 2, 3 );
		int length4  = calculateTicks( 1, 1 );
		int length4d = calculateTicks( 3, 2 );
		noteLength.put( length4t, Integer.toString(4) + triplet );
		noteLength.put( length4,  Integer.toString(4)           );
		noteLength.put( length4d, Integer.toString(4) + dot     );
		
		// half
		int length2t = calculateTicks( 2 * 2, 3 );
		int length2  = calculateTicks( 2,     1 );
		int length2d = calculateTicks( 2 * 3, 2 );
		noteLength.put( length2t, Integer.toString(2) + triplet );
		noteLength.put( length2,  Integer.toString(2)           );
		noteLength.put( length2d, Integer.toString(2) + dot     );
		
		// full
		int length1t = calculateTicks( 4 * 2, 3 );
		int length1  = calculateTicks( 4,     1 );
		int length1d = calculateTicks( 4 * 3, 2 );
		noteLength.put( length1t, Integer.toString(1) + triplet );
		noteLength.put( length1,  Integer.toString(1)           );
		noteLength.put( length1d, Integer.toString(1) + dot     );
		
		// 2 full notes
		int length_m2t = calculateTicks( 8 * 2, 3 );
		int length_m2  = calculateTicks( 8,     1 );
		int length_m2d = calculateTicks( 8 * 3, 2 );
		noteLength.put( length_m2t, m2 + triplet );
		noteLength.put( length_m2,  m2           );
		noteLength.put( length_m2d, m2  + dot    );
		
		// 4 full notes
		int length_m4t = calculateTicks( 16 * 2, 3 );
		int length_m4  = calculateTicks( 16,     1 );
		int length_m4d = calculateTicks( 16 * 3, 2 );
		noteLength.put( length_m4t, m4 + triplet );
		noteLength.put( length_m4,  m4           );
		noteLength.put( length_m4d, m4  + dot    );
		
		// 8 full notes
		int length_m8t = calculateTicks( 32 * 2, 3 );
		int length_m8  = calculateTicks( 32,     1 );
		int length_m8d = calculateTicks( 32 * 3, 2 );
		noteLength.put( length_m8t, m8 + triplet );
		noteLength.put( length_m8,  m8           );
		noteLength.put( length_m8d, m8  + dot    );
		
		// 16 full notes
		int length_m16t = calculateTicks( 64 * 2, 3 );
		int length_m16  = calculateTicks( 64,     1 );
		int length_m16d = calculateTicks( 64 * 3, 2 );
		noteLength.put( length_m16t, m16 + triplet );
		noteLength.put( length_m16,  m16           );
		noteLength.put( length_m16d, m16  + dot    );
		
		// 32 full notes
		int length_m32t = calculateTicks( 128 * 2, 3 );
		int length_m32  = calculateTicks( 128,     1 );
		int length_m32d = calculateTicks( 128 * 3, 2 );
		noteLength.put( length_m32t, m32 + triplet );
		noteLength.put( length_m32,  m32           );
		noteLength.put( length_m32d, m32  + dot    );
		
		return noteLength;
	}
	
	/**
	 * Calculates the tick length of a note, based on the current MIDI stream's resolution and
	 * in relation to a quarter note.
	 * The given factor and divisor influences the resulting note length.
	 * If factor and divisor are the same, the resulting length is exactly one quarter note.
	 * 
	 * @param factor
	 * @param divisor
	 * @return mathematically rounded result of resolution * factor / divisor
	 */
	private int calculateTicks( int factor, int divisor ) {
		return ( resolution * factor * 10 + 5 ) / ( divisor * 10 );
	}
	
}


