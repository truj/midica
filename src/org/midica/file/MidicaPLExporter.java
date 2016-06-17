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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import org.midica.config.Config;
import org.midica.config.Dict;
import org.midica.midi.MidiDevices;
import org.midica.midi.MidiListener;
import org.midica.midi.SequenceAnalyzer;
import org.midica.midi.SequenceCreator;
import org.midica.ui.model.ComboboxStringOption;
import org.midica.ui.model.ConfigComboboxModel;

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
	
	private static final String NEW_LINE    = System.getProperty( "line.separator" );
	private static final int    PRIO_INSTR  = -5;
	private static final int    PRIO_GLOBAL = -3;
	private static final int    PRIO_LYRICS = 20;
	
	/* *****************
	 * class fields
	 *******************/
	
	
	private static int          sourceResolution = 0;
	private static int          targetResolution = SequenceCreator.DEFAULT_RESOLUTION;
	private static ExportResult exportResult     = null;
	
	/** stores the current state of each channel */
	private static TreeMap<Long, ArrayList<HashMap<String, String>>>[] channelEvents = null;
	private static TreeMap<Long, HashMap<String, String>>[][]          noteEvent     = null;
	
	/**
	 * tick -- priority (-5: instr; -3: global; channel) -- key (cmd, end_tick, ...) -- value
	 * 
	 * The value type depends on the key:
	 * 
	 * - instr: null
	 * - 
	 * 
	 * TODO: complete docu
	 */
	private static TreeMap<Long, TreeMap<Integer, HashMap<String, Object>>> timeline = null;
	
	private static TreeMap<Integer, String> noteLength = null;
	
	/** comma-separated note bytes  --  chord name */
	private static TreeMap<String, String> chords = null;
	
	/** lowest note  --  chord count */
	private static TreeMap<String, Integer> chordCount = null;
	
	/** lowest note  --  comma-separated note bytes (This structure is only needed for the sorting: lowest note first, then chord name) */
	private static TreeMap<String, ArrayList<String>> chordsByBaseNote = null;
	
	// structures built by the SequenceAnalyzer
	private static TreeMap<Byte, TreeMap<Long, Byte[]>>                 instrumentHistory = null;
	private static TreeMap<Byte, TreeMap<Long, String>>                 commentHistory    = null;
	private static TreeMap<Byte, TreeMap<Long, TreeMap<Byte, Byte>>>    noteHistory       = null;
	private static TreeMap<Byte, TreeMap<Byte, TreeMap<Long, Boolean>>> noteOnOff         = null;
	private static TreeMap<Long, TreeMap<Long, String>>                 lyrics            = null;
	
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
	 * @param  file  MidicaPL source file.
	 * @return warnings that occured during the export.
	 * @throws ExportException if the file can not be exported correctly.
	 */
	public ExportResult export( File file ) throws ExportException {
		
		exportResult         = new ExportResult( true );
		String targetCharset = ((ComboboxStringOption) ConfigComboboxModel.getModel( Config.CHARSET_EXPORT_MPL ).getSelectedItem() ).getIdentifier();
		
		try {
			
			// create file writer and store it in this.writer
			if ( ! createFile(file) )
				return new ExportResult( false );
			
			// open file for writing
			FileOutputStream   fos    = new FileOutputStream( file );
			OutputStreamWriter osw    = new OutputStreamWriter( fos, targetCharset );
			BufferedWriter     writer = new BufferedWriter( osw );
			
			// get pre-parsed data structures
			HashMap<String, Object> histories = SequenceAnalyzer.getHistories();
			instrumentHistory = (TreeMap<Byte, TreeMap<Long, Byte[]>>)                 histories.get( "instrument_history" );
			commentHistory    = (TreeMap<Byte, TreeMap<Long, String>>)                 histories.get( "comment_history" );
			noteHistory       = (TreeMap<Byte, TreeMap<Long, TreeMap<Byte, Byte>>>)    histories.get( "note_history" );
			noteOnOff         = (TreeMap<Byte, TreeMap<Byte, TreeMap<Long, Boolean>>>) histories.get( "note_on_off" );
			lyrics            = (TreeMap<Long, TreeMap<Long, String>>)                 histories.get( "lyrics" );
			
			chords           = new TreeMap<String, String>();
			chordCount       = new TreeMap<String, Integer>();
			chordsByBaseNote = new TreeMap<String, ArrayList<String>>();
			
			// TODO: delete
			System.out.println( "comment history: " + commentHistory.get((byte)0).keySet() );
			
			// initialize data structures
			channelEvents = new TreeMap[ 16 ];
			noteEvent     = new TreeMap[ 16 ][ 128 ];
			for ( byte channel = 0; channel < 16; channel++ ) {
				channelEvents[ channel ] = new TreeMap<Long, ArrayList<HashMap<String,String>>>();
				
				TreeMap<Long, HashMap<String, String>>[] channelNoteActivity = new TreeMap[ 128 ];
				for ( int note = 0; note < 128; note++ ) {
					channelNoteActivity[ note ] = new TreeMap<Long, HashMap<String, String>>();
				}
				noteEvent[ channel ] = channelNoteActivity;
			}
			
			
			// make sure that the syntax configuration is up to date
			MidicaPLParser.refreshSyntax();
			
			// fill the timeline with instrument changes and note events
			timeline = new TreeMap<Long, TreeMap<Integer, HashMap<String, Object>>>();
			
			// fill data structures
			sourceResolution = readSequence();
			
			// calculate what tick length corresponds to what note length
			noteLength = initNoteLengths();
			prefillTimeline();
			
			
//			preprocessEvents();
			
			// create MidicaPL string from the data structures and write it into the file
			writer.write( createMidicaPL() );
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
	 * Fills the timeline structure with the following information:
	 * 
	 * - instrument changes
	 * - note-on events
	 */
	private void prefillTimeline() {
		
		// add instrument change ticks to the timeline
		for ( byte channel = 0; channel < 16; channel++ ) {
			for ( long tick : instrumentHistory.get( channel ).keySet() ) {
				if ( tick > 0 ) {
					addToTimeline( tick, PRIO_INSTR, "instr", null );
				}
			}
		}
		
		// collect notes by channel
		// channel tick note/velo/length/...
		TreeMap<Byte, TreeMap<Long, HashMap<String, String>>> notesByChannel;
		for ( Entry<Byte, TreeMap<Long, TreeMap<Byte, Byte>>> channelSet : noteHistory.entrySet() ) {
			byte channel                                     = channelSet.getKey();
			TreeMap<Long, TreeMap<Byte, Byte>> channelStruct = channelSet.getValue();
			for ( Entry<Long, TreeMap<Byte, Byte>> tickSet : channelStruct.entrySet() ) {
				long tick                      = tickSet.getKey();
				TreeMap<Byte, Byte> tickStruct = tickSet.getValue();
				
				// create notes structure for this tick
				TreeMap<String, TreeMap<String, String>> notesStruct = new TreeMap<String, TreeMap<String, String>>();
				for ( Entry<Byte, Byte> noteSet : tickStruct.entrySet() ) {
					byte   note     = noteSet.getKey();
					byte   velocity = noteSet.getValue();
					Long   endTick  = noteOnOff.get( channel ).get( note ).ceilingKey( tick + 1 );
					
					// create structure for this note
					TreeMap<String, String> noteStruct = new TreeMap<String, String>();
					String[] cmdLength = convertLength( endTick - tick );
					noteStruct.put( "velocity", velocity + "" );
					noteStruct.put( "end_tick", endTick  + "" );
					noteStruct.put( "length",   cmdLength[0]  );
					noteStruct.put( "staccato", cmdLength[1]  );
					
					// TODO: delete
					if ( 0 == channel ) {
						System.out.println( "note: " + note + ", channel: " + channel + ", velo: " + velocity + ", tick: " + tick + "-" + endTick + ", length: " + cmdLength[0] + ", staccato: " + cmdLength[1] );
					}
					
					// add to the tick notes
					String noteName = Dict.getNote( (int) note );
					if ( 9 == channel ) {
						noteName = Dict.getPercussion( (int) note );
					}
					notesStruct.put( noteName + "", noteStruct );
					
					// TODO: delete
//					TreeMap<Long, HashMap<String, String>> tickNoteOpt = notesByChannel.get( channel );
//					if ( null == tickNoteOpt ) {
//						tickNoteOpt = new TreeMap<Long, HashMap<String,String>>();
//						notesByChannel.put( channel, tickNoteOpt );
//					}
//					notesByChannel.put( , value)
				}
				
				// transform notes into chords, if possible
				if ( channel != 9 ) {
					organizeChords( notesStruct );
				}
				
				System.out.println( notesStruct ); // TODO: delete
				
				// add all notes/chords of this tick to the timeline
				addToTimeline( tick, channel, "notes", notesStruct );
			}
		}
	}
	
	/**
	 * Receives the notes that are pressed at one tick and channel.
	 * Puts them together as chords, if possible.
	 * 
	 * @param notes Notes, pressed at a certain tick in a certain channel.
	 */
	private void organizeChords( TreeMap<String, TreeMap<String, String>> notes ) {
		
		// velocity,end_tick -- notes
		TreeMap<String, TreeSet<Byte>> chordCandidates = new TreeMap<String, TreeSet<Byte>>();
		
		// sort all notes by groups of the same velocity and end_tick
		for ( Entry<String, TreeMap<String, String>> noteSet : notes.entrySet() ) {
			
			String                  noteName = noteSet.getKey();
			TreeMap<String, String> noteOpts = noteSet.getValue();
			byte                    note     = (byte) Dict.getNote( noteName );
			String                  velocity = noteOpts.get( "velocity" );
			String                  endTick  = noteOpts.get( "end_tick" );
			
			// add structure for this velocity/end_tick, if not yet done
			String        chordKey   = velocity + "," + endTick;
			TreeSet<Byte> chordNotes = chordCandidates.get( chordKey );
			if ( null == chordNotes ) {
				chordNotes = new TreeSet<Byte>();
				chordCandidates.put( chordKey, chordNotes );
			}
			
			// add current note
			chordNotes.add( note );
		}
		
		// check if there are notes that we can combine to chords
		for ( Entry<String, TreeSet<Byte>> noteSet : chordCandidates.entrySet() ) {
			TreeSet<Byte> chordNotes = noteSet.getValue();
			
			// more than 1 note of the same velocity and end tick?
			if ( chordNotes.size() > 1 ) {
				
				// structure for the chord (to replace the candidates' structures
				TreeMap<String, String> chordStruct = null;
				
				// create the global chord key
				StringBuilder  chordKey = new StringBuilder();
				Iterator<Byte> it       = chordNotes.iterator();
				boolean        isfirst  = true;
				while ( it.hasNext() ) {
					byte                    note       = it.next();
					String                  noteName   = Dict.getNote( note );
					TreeMap<String, String> noteStruct = notes.get( noteName );
					if (isfirst) {
						isfirst = false;
						chordKey.append( note );
						chordStruct = noteStruct; // copy note options to the chord
					}
					else {
						chordKey.append( "," + note );
					}
					
					// remove the note
					notes.remove( noteName );
				}
				
				// chord not yet available?
				String chordName = chords.get( chordKey.toString() );
				if ( null == chordName ) {
					chordName = makeChordName( chordNotes, chordKey.toString() );
					chords.put( chordKey.toString(), chordName );
				}
				
				// add the chord
				notes.put( chordName, chordStruct );
			}
		}
	}
	
	/**
	 * Creates a new unique name for the chord consisting of the given notes.
	 * 
	 * @param notes     The notes of the chord.
	 * @param csvNotes  Comma-separated note bytes.
	 * @return the new chord name.
	 */
	private String makeChordName( TreeSet<Byte> notes, String csvNotes ) {
		
		byte lowestNote = notes.first();

		// get base name of the chord
		String baseName = Dict.getBaseNoteName( lowestNote );
		
		// get number of chords with this lowest note so far
		Integer count = chordCount.get( baseName );
		if ( null == count ) {
			count = 0;
		}
		
		// increment chord count
		count++;
		chordCount.put( baseName, count );
		
		// create name
		String chordName = "crd_" + baseName + "_" + count;
		
		// store in an ordered form
		ArrayList<String> noteChords = chordsByBaseNote.get( baseName );
		if ( null == noteChords ) {
			noteChords = new ArrayList<String>();
			chordsByBaseNote.put( baseName, noteChords );
		}
		noteChords.add( csvNotes );
		
		return chordName;
	}
	
	/**
	 * Adds the given values to the timeline structure.
	 * 
	 * TODO: complete docu
	 * 
	 * @param tick
	 * @param priority
	 * @param key
	 * @param value
	 */
	private void addToTimeline( long tick, int priority, String key, Object value ) {
		
		// get or create sub structure for the tick
		TreeMap<Integer, HashMap<String, Object>> tickStruct = timeline.get( tick );
		if ( null == tickStruct ) {
			tickStruct = new TreeMap<Integer, HashMap<String, Object>>();
			timeline.put( tick, tickStruct );
		}
		
		// get or create sub structure for the priority
		HashMap<String, Object> prioStruct = tickStruct.get( priority );
		if ( null == prioStruct ) {
			prioStruct = new HashMap<String, Object>();
			tickStruct.put( priority, prioStruct );
		}
		
		// TODO: get or create sub structure for the notes
		if ( "notes".equals(key) ) {
			
		}
		else {
			
		}
		
		// fill
		prioStruct.put( key, value );
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
	 * @return resolution of the loaded MIDI sequence (ticks per quarter note)
	 * @throws ExportException 
	 */
	private int readSequence() throws ExportException {
		
		// parse the midi sequence
		Sequence seq = MidiDevices.getSequence();
		
		int trackNum = 0;
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
					
					// TODO: delete
					// instrument selection?
					if ( ShortMessage.PROGRAM_CHANGE == cmd ) {
						// ignore
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
						exportResult.addWarning( trackNum, tick, channel, -1, warning );
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
						
						addToTimeline( tick, PRIO_GLOBAL, "bpm", bpmStr );
					}
					
				}
			}
			trackNum++;
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
								int    track   = channel + SequenceCreator.NUM_META_TRACKS;
								exportResult.addWarning( track, tick, channel, note, warning );
							}
							previousActivity = false;
						}
						else {
							if (previousActivity) {
								String warning = String.format( Dict.get(Dict.WARNING_UNRELEASED_NOTE_PRESSED), previousTick );
								int    track   = channel + SequenceCreator.NUM_META_TRACKS;
								exportResult.addWarning( track, tick, channel, note, warning );
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
		StringBuilder output = new StringBuilder();
		
		// initial INSTRUMENTS block (tick 0)
		output.append( createInstrBlock(0) );
		
		// add chord definitions
		output.append( createChordDefinitions() );
		
		// apply timeline
		for ( long tick : timeline.keySet() ) {
			TreeMap<Integer, HashMap<String, Object>> prioStruct = timeline.get( tick );
			for ( int prio : prioStruct.keySet() ) {
				HashMap<String, Object> contentStruct = prioStruct.get( prio );
				
				// instrument change
				if ( contentStruct.containsKey("instr") ) {
					output.append( createInstrBlock(tick) );
				}
				
				// tempo change
				else if ( contentStruct.containsKey("bpm") ) {
					String bpm = (String) contentStruct.get( "bpm" );
					output.append( createTickComment(tick) );
					output.append( MidicaPLParser.GLOBAL + "\t" + MidicaPLParser.BPM + "\t" + bpm + NEW_LINE + NEW_LINE );
				}
				
				// notes
				else if ( contentStruct.containsKey("notes") ) {
					System.out.println( prio ); // TODO: delete
					output.append( createNoteLines(prio, contentStruct) );
				}
			}
		}
		
		// TODO: delete
		output.append( NEW_LINE );
		
		return output.toString();
	}
	
	/**
	 * Creates an INSTRUMENTS block.
	 * 
	 * If this is the initial INSTRUMENTS block (tick == 0),
	 * the block contains all channels used in the MIDI sequence.
	 * 
	 * Otherwise (tick > 0) it includes only the instruments that
	 * have a program change at the given tick.
	 * 
	 * @param tick **0** for the initial block; higher values for later instrument changes
	 * @return the initial INSTRUMENTS block.
	 */
	private String createInstrBlock( long tick ) {
		
		// open block
		StringBuilder block = new StringBuilder( "" );
		block.append( NEW_LINE );
		block.append( createTickComment(tick) );
		block.append( MidicaPLParser.INSTRUMENTS + NEW_LINE );
		
		// content
		if ( 0 == tick ) {
			// initial INSTRUMENTS block -- include all channels
			for ( byte channel = 0; channel < 16; channel++ ) {
				String instrLine = createInstrLine( tick, channel );
				block.append( instrLine );
			}
		}
		else {
			// instrument change -- include only instrument with
			// a program change at the given tick.
			for ( byte channel = 0; channel < 16; channel++ ) {
				Set<Long> changeTicks = instrumentHistory.get( channel ).keySet();
				if ( changeTicks.contains(tick) ) {
					String instrLine = createInstrLine( tick, channel );
					block.append( instrLine );
				}
			}
		}
		
		// close block
		block.append( MidicaPLParser.END + NEW_LINE + NEW_LINE );
		
		return block.toString();
	}
	
	/**
	 * Creates one line inside an INSTRUMENTS block.
	 * 
	 * Returns an empty string, if no notes are played in the given channel.
	 * 
	 * At the beginning this method is called for each channel (0-15).
	 * This considers:
	 * 
	 * - bank selects at tick 0
	 * - program changes at tick 0
	 * - channels without a program change that are used anyway
	 * 
	 * Afterwards this method is called for every tick that contains a program
	 * change at a tick higher than 0.
	 * 
	 * @param tick     The tickstamp of the program change event; or **0** during the initialization.
	 * @param channel  The channel number.
	 * @return the instrument line or an empty string, if the channel is not used.
	 */
	private String createInstrLine( long tick, byte channel ) {
		
		// channel used?
		if ( 0 == noteHistory.get(channel).size() ) {
			return "";
		}
		
		// get the channel's history
		TreeMap<Long, Byte[]> chInstrHist = instrumentHistory.get( channel );
		Byte[]  instrConfig;
		boolean isAutoChannel = false;
		
		if ( 0 == tick ) {
			// initialization - either a program change at tick 0 or the default at a negative tick
			Entry<Long, Byte[]> initialInstr   = chInstrHist.floorEntry( tick );
			long                progChangeTick = initialInstr.getKey();
			instrConfig                        = initialInstr.getValue();
			if ( progChangeTick < 0 ) {
				isAutoChannel = true;
			}
		}
		else {
			// program change at a tick > 0
			instrConfig = chInstrHist.get( tick );
			
			// no program change at this tick?
			if ( null == instrConfig ) {
				return "";
			}
		}
		
		// get program and bank
		byte msb  = instrConfig[ 0 ];
		byte lsb  = instrConfig[ 1 ];
		byte prog = instrConfig[ 2 ];
		
		// initialize instrument
		Instrument instr = new Instrument( channel, prog, null, isAutoChannel );
		
		// get the strings to write into the instrument line
		String channelStr = 9 == channel ? MidicaPLParser.P : channel + "";
		String programStr = instr.instrumentName;
		if ( msb != 0 || lsb != 0 ) {
			programStr += MidicaPLParser.PROG_BANK_SEP + msb;
			if ( lsb != 0 ) {
				programStr += MidicaPLParser.BANK_SEP + lsb;
			}
		}
		String commentStr    = instr.instrumentName;
		Long   instrNameTick = commentHistory.get( channel ).floorKey( tick );
		if ( instrNameTick != null ) {
			commentStr = commentHistory.get( channel ).get( instrNameTick );
		}
		
		// put everything together
		return (
			  "\t"   + channelStr
			+ "\t"   + programStr
			+ "\t\t" + commentStr
			+ NEW_LINE
		);
	}
	
	/**
	 * Creates the CHORD definitions.
	 * 
	 * @return the CHORD commands.
	 */
	private String createChordDefinitions() {
		
		// no chords available?
		if ( chords.isEmpty() ) {
			return "";
		}
		
		// initialize
		StringBuilder chordBlock = new StringBuilder( "" );
		
		// chords
//		for ( Entry<String, String> chordSet : chords.entrySet() ) {
//			String notesStr  = chordSet.getKey();
//			String chordName = chordSet.getValue();
//			chordBlock.append( MidicaPLParser.CHORD + " " + chordName + "\t" );
//			
//			// notes
//			boolean isFirst = true;
//			String[] notes  = notesStr.split( "\\," );
//			for ( String noteStr : notes ) {
//				String noteName = Dict.getNote( Integer.parseInt(noteStr) );
//				if (isFirst) {
//					chordBlock.append( noteName );
//					isFirst = false;
//				}
//				else {
//					chordBlock.append( " " + noteName );
//				}
//			}
//			chordBlock.append( NEW_LINE );
//		}
//		chordBlock.append( NEW_LINE );
		
		// get base notes in the right order, beginning with A
		ArrayList<String> orderedNotes = new ArrayList<String>();
		for ( int i=0; i<12; i++ ) {
			String baseName = Dict.getBaseNoteName( i );
			orderedNotes.add( baseName );
		}
		
		// all chords
		BASE_NAME:
		for ( String baseName : orderedNotes ) {
			
			// chords with the current baseName as the lowest note
			ArrayList<String> noteChords = chordsByBaseNote.get( baseName );
			
			// no chords with this base name?
			if ( null == noteChords ) {
				continue BASE_NAME;
			}
			
			for ( String notesStr : noteChords ) {
				String chordName = chords.get( notesStr );
				chordBlock.append( MidicaPLParser.CHORD + " " + chordName + "\t" );
				
				// notes
				boolean isFirst = true;
				String[] notes  = notesStr.split( "\\," );
				for ( String noteStr : notes ) {
					String noteName = Dict.getNote( Integer.parseInt(noteStr) );
					if (isFirst) {
						chordBlock.append( noteName );
						isFirst = false;
					}
					else {
						chordBlock.append( " " + noteName );
					}
				}
				chordBlock.append( NEW_LINE );
			}
		}
		chordBlock.append( NEW_LINE );
		
		return chordBlock.toString();
	}
	
	/**
	 * Crates lines for all notes or chords that are played in a certain
	 * channel at a certain tick.
	 * 
	 * @param channel  MIDI channel.
	 * @param content  notes structure for this tick and channel.
	 * @return the created line(s).
	 */
	private String createNoteLines( int channel, HashMap<String, Object> content ) {
		
		// initialize
		StringBuilder lines = new StringBuilder( "" );
		
		for ( Object notesObj : content.values() ) {
			TreeMap<String, TreeMap<String, String>> notes = (TreeMap<String, TreeMap<String, String>>) notesObj;
			
			for ( Entry<String, TreeMap<String, String>> noteSet : notes.entrySet() ) {
				String                  noteName = noteSet.getKey();
				TreeMap<String, String> options  = noteSet.getValue();
				lines.append( channel + "\t" + noteName + "\t" + options.get("length") );
				// TODO: add staccato
				// TODO: add multi
				lines.append("\n");
			}
		}
		
		
		return lines.toString();
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
			int    track   = channel + SequenceCreator.NUM_META_TRACKS;
			exportResult.addWarning( track, tick, channel, note, warning );
			
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
			System.err.println( "note too long. ticks: " + ticks ); // TODO: delete
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
	 * Calculates which tick length corresponds to which note length.
	 * That depends on the resolution of the current MIDI sequence.
	 * 
	 * @return Mapping between tick length and note length for the syntax.
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
	 * Calculates the tick length of a note, based on the current MIDI
	 * sequence's resolution and in relation to a quarter note.
	 * The given factor and divisor influences the resulting note length.
	 * If factor and divisor are the same, the resulting length is exactly
	 * one quarter note.
	 * 
	 * @param factor
	 * @param divisor
	 * @return mathematically rounded result of resolution * factor / divisor
	 */
	private int calculateTicks( int factor, int divisor ) {
		return ( sourceResolution * factor * 10 + 5 ) / ( divisor * 10 );
	}
	
	/**
	 * Creates a comment line giving the current tick.
	 * 
	 * @param tick  MIDI tickstamp.
	 * @return the comment string.
	 */
	private String createTickComment( long tick ) {
		return (
			  MidicaPLParser.COMMENT + " "
			+ Dict.get( Dict.TICK )  + " "
			+ tick + NEW_LINE
		);
	}
}


