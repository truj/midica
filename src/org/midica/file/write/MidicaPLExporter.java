/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file.write;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.midica.config.Dict;
import org.midica.file.Instrument;
import org.midica.file.read.MidicaPLParser;
import org.midica.midi.KaraokeAnalyzer;
import org.midica.midi.SequenceAnalyzer;

/**
 * This class is used to export the currently loaded MIDI sequence as a MidicaPL source file.
 * 
 * @author Jan Trukenmüller
 */
public class MidicaPLExporter extends Decompiler {
	
	// string formats for channel commands
	private String FORMAT_CH_CMD_CHANNEL = "%-2s";   // channel:          2 left-aligned characters, filled with spaces
	private String FORMAT_CH_CMD_CRD     = "%-10s";  // chord/note/rest: 10 left-aligned characters, filled with spaces
	private String FORMAT_CH_CMD_LENGTH  = "%1$10s"; // length:          10 right-aligned characters, filled with spaces
	
	/**
	 * Creates a new MidicaPL exporter.
	 */
	public MidicaPLExporter() {
		format = MIDICA;
	}
	
	/**
	 * Initializes MidicaPL specific data structures.
	 */
	public void init() {
	}
	
	/**
	 * Creates the MidicaPL string to be written into the export file.
	 * 
	 * @return MidicaPL string to be written into the export file
	 */
	public String createOutput() {
		StringBuilder output = new StringBuilder();
		
		// META block
		output.append( createMetaBlock() );
		
		// initial INSTRUMENTS block (tick 0)
		output.append( createInitialInstrumentsBlock() );
		
		// add chord definitions
		output.append( createChordDefinitions() );
		
		// SLICE:
		for (Slice slice : slices) {
			
			// if necessary: add rest from current tick to the slice's begin tick
			output.append( createRestBeforeSlice(slice) );
			
			// global commands
			output.append( createGlobalCommands(slice) );
			
			// channel commands and instrument changes
			for (byte channel = 0; channel < 16; channel++) {
				
				// Add nestable block at the slice begin, if needed.
				// This may contain orphaned syllables and/or (in the future) control changes.
				if (slice.hasSliceBeginBlock(channel)) {
					output.append( createSliceBeginBlock(slice, channel) );
				}
				
				// normal commands
				output.append( createCommandsFromTimeline(slice, channel) );
			}
		}
		
		// config
		output.append(createConfig());
		
		// quality statistics
		output.append(createQualityStats());
		
		// strategy statistics
		output.append(createStrategyStats());
		
		return output.toString();
	}
	
	/**
	 * Creates channel commands and instrument changes from a slice's timeline.
	 * 
	 * Steps:
	 * 
	 * - Adds the following missing properties and elements to the notes and chords of the timeline:
	 *     - properties:
	 *         - length property for each note/chord
	 *         - multiple property, if neccessary
	 *     - elements:
	 *         - rests, if necessary
	 * - Creates the commands
	 * 
	 * @param slice    the sequence slice
	 * @param channel  MIDI channel
	 * @return the created commands (or an empty string, if the slice's timeline doesn't contain anything in the given channel)
	 */
	private String createCommandsFromTimeline(Slice slice, byte channel) {
		StringBuilder lines = new StringBuilder();
		TreeMap<Long, TreeMap<Byte, TreeMap<String, TreeMap<Byte, String>>>> timeline = slice.getTimeline(channel);
		
		// TICK:
		for (Entry<Long, TreeMap<Byte, TreeMap<String, TreeMap<Byte, String>>>> timelineSet : timeline.entrySet()) {
			long tick = timelineSet.getKey();
			TreeMap<Byte, TreeMap<String, TreeMap<Byte, String>>> events = timelineSet.getValue();
			
			// instrument change
			if (events.containsKey(ET_INSTR)) {
				lines.append( createInstrumentChange(channel, tick) );
			}
			
			// inline block
			if (events.containsKey(ET_INLINE_BLK)) {
				lines.append( createInlineBlock(channel, tick, slice) );
			}
			
			// notes/chords
			if (events.containsKey(ET_NOTES)) {
				
				// all notes/chords with the same note-ON tick
				TreeMap<String, TreeMap<Byte, String>> notes = events.get(ET_NOTES);
				for (Entry<String, TreeMap<Byte, String>> entry : notes.entrySet()) {
					TreeMap<Byte, String> params = entry.getValue();
					long offTick = Long.parseLong( params.get(NP_OFF_TICK) );
					
					// calculate note length / duration
					long[] lengthProps  = getNoteLengthProperties(tick, offTick, channel);
					long   length       = lengthProps[0];
					long   endTick      = tick + length;
					String durationPerc = lengthProps[1] + "";
					ArrayList<Long> summands = getLengthsForSum(length, false);
					ArrayList<String> summandStrings = new ArrayList<>();
					for (Long summand : summands) {
						String summandStr = noteLength.get(summand);
						summandStrings.add(summandStr);
						incrementStats(STAT_NOTE_SUMMANDS, channel);
						if (summandStr.endsWith(MidicaPLParser.TRIPLET)) {
							incrementStats(STAT_NOTE_TRIPLETS, channel);
						}
					}
					String lengthStr = String.join(MidicaPLParser.LENGTH_PLUS, summandStrings);
					
					// add note length / duration to timeline
					params.put( NP_LENGTH,   lengthStr    );
					params.put( NP_END_TICK, endTick + "" );
					params.put( NP_DURATION, durationPerc );
					
					incrementStats(STAT_NOTES, channel);
				}
				
				// write MidicaPL
				lines.append( createNoteLines(slice, channel, tick, events.get(ET_NOTES)) );
			}
		}
		
		// add one empty line between channels
		if (! timeline.isEmpty()) {
			lines.append(NEW_LINE);
		}
		
		return lines.toString();
	}
	
	/**
	 * Creates the META block, if the sequence contains any META information.
	 * 
	 * @return the META block, or an empty string if the sequence doesn't contain any meta information.
	 */
	private String createMetaBlock() {
		StringBuilder     block = new StringBuilder("");
		ArrayList<String> lines = new ArrayList<>();
		
		// get data structures
		HashMap<String, Object> sequenceInfo = (HashMap<String, Object>) SequenceAnalyzer.getSequenceInfo();
		HashMap<String, String> metaInfo     = (HashMap<String, String>) sequenceInfo.get("meta_info");
		HashMap<String, Object> karaokeInfo  = KaraokeAnalyzer.getKaraokeInfo();
		String copyright = (String) metaInfo.get("copyright");
		String[] fields = {"copyright", "title", "composer", "lyricist", "artist"};
		String[] values = new String[5];
		String[] mplIds = {
			MidicaPLParser.META_COPYRIGHT,
			MidicaPLParser.META_TITLE,
			MidicaPLParser.META_COMPOSER,
			MidicaPLParser.META_LYRICIST,
			MidicaPLParser.META_ARTIST,
		};
		values[0] = copyright;
		
		// process fields
		for (int i = 0; i < fields.length; i++) {
			
			// read value (skip copyright as we have it already)
			if (i > 0)
				values[i] = (String) karaokeInfo.get(fields[i]);
			
			// value not set
			if (null == values[i])
				continue;
			
			// split the line, if necessary
			String[] multiLines = values[i].split("\n");
			
			// LINE of this field
			for (String singleLine : multiLines) {
				if (! "".equals(singleLine))
					lines.add("\t" + String.format("%-12s", mplIds[i]) + " " + singleLine + NEW_LINE);
			}
		}
		
		// add soft karaoke block, if necessary
		String skType = (String) karaokeInfo.get("sk_type");
		if (skType != null && "MIDI KARAOKE FILE".equals(skType.toUpperCase())) {
			isSoftKaraoke = true;
			lines.add(createSoftKaraokeBlock(karaokeInfo));
		}
		
		// no meta data found?
		if (lines.isEmpty())
			return "";
		
		// add block
		block.append(MidicaPLParser.META + NEW_LINE);
		for (String line : lines) {
			block.append(line);
		}
		block.append(MidicaPLParser.END + NEW_LINE + NEW_LINE);
		
		return block.toString();
	}
	
	/**
	 * Creates the SOFT_KARAOKE block inside of the META block.
	 * This is called only if the sequence uses SOFT KARAOKE.
	 * 
	 * @param karaokeInfo  Karaoke information extracted from the sequence.
	 * @return the created block.
	 */
	private String createSoftKaraokeBlock(HashMap<String, Object> karaokeInfo) {
		StringBuilder block = new StringBuilder("");
		
		// open the block
		block.append("\t" + MidicaPLParser.META_SOFT_KARAOKE + NEW_LINE);
		
		// read single-line fields
		String[] fields = {"sk_version", "sk_language", "sk_title", "sk_author", "sk_copyright"};
		String[] mplIds = {
			MidicaPLParser.META_SK_VERSION,
			MidicaPLParser.META_SK_LANG,
			MidicaPLParser.META_SK_TITLE,
			MidicaPLParser.META_SK_AUTHOR,
			MidicaPLParser.META_SK_COPYRIGHT,
		};
		
		// process single-line fields
		for (int i = 0; i < fields.length; i++) {
			
			// read value
			String value = (String) karaokeInfo.get(fields[i]);
			if (null == value)
				continue;
			
			// append the line
			block.append("\t\t" + String.format("%-12s", mplIds[i]) + " " + value + NEW_LINE);
		}
		
		// process info fields
		ArrayList<String> infos = (ArrayList<String>) karaokeInfo.get("sk_infos");
		if (infos != null) {
			for (String info : infos) {
				
				// append info line
				if (! "".equals(info))
					block.append(
						  "\t\t" + String.format("%-12s", MidicaPLParser.META_SK_INFO) + " "
						+ info + NEW_LINE
					);
			}
		}
		
		// close the block
		block.append("\t" + MidicaPLParser.END + NEW_LINE);
		
		return block.toString();
	}
	
	/**
	 * Creates the initial INSTRUMENTS block.
	 * 
	 * @return the created block.
	 */
	private String createInitialInstrumentsBlock() {
		
		// open block
		StringBuilder block = new StringBuilder("");
		block.append(MidicaPLParser.INSTRUMENTS + NEW_LINE);
		
		// add instruments
		for (byte channel = 0; channel < 16; channel++) {
			String instrLine = createInstrLine(0, channel);
			block.append(instrLine);
		}
		
		// close block
		block.append(MidicaPLParser.END + NEW_LINE + NEW_LINE);
		
		return block.toString();
	}
	
	/**
	 * Creates one INSTRUMENT line for an instrument change in the given channel and tick.
	 * 
	 * @param channel  MIDI channel
	 * @param tick     MIDI tick
	 * @return the created lines.
	 */
	private String createInstrumentChange(byte channel, long tick) {
		
		// prepare
		StringBuilder lines = new StringBuilder("");
		
		// add instruments
		Set<Long> changeTicks = instrumentHistory.get(channel).keySet();
		if (changeTicks.contains(tick)) {
			String instrLine = createInstrLine(tick, channel);
			if (! "".equals(instrLine)) {
				lines.append(instrLine);
			}
		}
		
		return lines.toString();
	}
	
	/**
	 * Creates one line inside an INSTRUMENTS block **or** one single instrument change line.
	 * 
	 * If tick is 0, a line inside a block is created. Otherwise it's an instrument change line.
	 * 
	 * Returns an empty string, if no instruments must be defined or changed in the given channel and tick.
	 * 
	 * At the beginning this method is called for each channel (0-15).
	 * This considers:
	 * 
	 * - bank selects at tick 0
	 * - program changes at tick 0
	 * - channels without a program change that are used anyway
	 * 
	 * Afterwards this method is called for every tick and channel that contains one or more
	 * program changes at a tick higher than 0.
	 * 
	 * @param tick     The tickstamp of the program change event; or **0** during initialization.
	 * @param channel  The channel number.
	 * @return the instrument line or an empty string.
	 */
	private String createInstrLine(long tick, byte channel) {
		
		// channel used?
		if (0 == noteHistory.get(channel).size()) {
			return "";
		}
		
		// get the channel's history
		TreeMap<Long, Byte[]> chInstrHist = instrumentHistory.get(channel);
		Byte[]  instrConfig;
		boolean isAutoChannel = false;
		
		String cmd = "\t";
		if (0 == tick) {
			// initialization - either a program change at tick 0 or the default at a negative tick
			Entry<Long, Byte[]> initialInstr   = chInstrHist.floorEntry(tick);
			long                progChangeTick = initialInstr.getKey();
			instrConfig                        = initialInstr.getValue();
			if (progChangeTick < 0) {
				isAutoChannel = true;
			}
		}
		else {
			// program change at a tick > 0
			cmd         = MidicaPLParser.INSTRUMENT;
			instrConfig = chInstrHist.get(tick);
			
			// no program change at this tick?
			if (null == instrConfig) {
				return "";
			}
		}
		
		// get program and bank
		byte msb  = instrConfig[ 0 ];
		byte lsb  = instrConfig[ 1 ];
		byte prog = instrConfig[ 2 ];
		
		// initialize instrument
		Instrument instr = new Instrument(channel, prog, null, isAutoChannel);
		
		// get the strings to write into the instrument line
		String channelStr = 9 == channel ? MidicaPLParser.P : channel + "";
		String programStr = instr.instrumentName;
		if (Dict.get(Dict.UNKNOWN_DRUMKIT_NAME).equals(programStr)) {
			programStr = prog + "";
		}
		if (msb != 0 || lsb != 0) {
			programStr += MidicaPLParser.PROG_BANK_SEP + msb;
			if (lsb != 0) {
				programStr += MidicaPLParser.BANK_SEP + lsb;
			}
		}
		String commentStr    = instr.instrumentName;
		Long   instrNameTick = commentHistory.get(channel).floorKey(tick);
		if (instrNameTick != null) {
			commentStr = commentHistory.get(channel).get(instrNameTick);
		}
		
		// put everything together
		
		// instruments block
		if (0 == tick) {
			return (
				  cmd
				+ String.format("%-4s", channelStr)
				+ " "
				+ String.format("%-22s", programStr)
				+ " "
				+ commentStr
				+ NEW_LINE
			);
		}
		
		// single instrument change
		return appendTickComment(cmd + "  " + channelStr + "  " + programStr, tick) + NEW_LINE;
	}
	
	/**
	 * Creates the CHORD definitions.
	 * 
	 * @return the CHORD commands.
	 */
	private String createChordDefinitions() {
		
		// no chords available?
		if (chords.isEmpty()) {
			return "";
		}
		
		// initialize
		StringBuilder chordBlock = new StringBuilder("");
		
		// get base notes in the right order, beginning with A
		ArrayList<String> orderedNotes = new ArrayList<>();
		for (int i=0; i<12; i++) {
			String baseName = Dict.getBaseNoteName(i);
			orderedNotes.add(baseName);
		}
		
		// note name that may be the base of several chords
		BASE_NAME:
		for (String baseName : orderedNotes) {
			
			// chords with the current baseName as the lowest note
			ArrayList<String> noteChords = chordsByBaseNote.get(baseName);
			
			// no chords with this base name?
			if (null == noteChords) {
				continue BASE_NAME;
			}
			
			// chords
			for (String notesStr : noteChords) {
				String chordName = chords.get(notesStr);
				chordBlock.append(MidicaPLParser.CHORD + " " + String.format("%-12s", chordName)  + " ");
				
				// notes
				String[]          noteNumbers = notesStr.split("\\,");
				ArrayList<String> noteNames   = new ArrayList<>();
				for (String noteNumber : noteNumbers) {
					String noteName = Dict.getNote(Integer.parseInt(noteNumber));
					noteNames.add(noteName);
				}
				chordBlock.append( String.join(MidicaPLParser.CHORD_SEPARATOR, noteNames) );
				chordBlock.append(NEW_LINE);
			}
		}
		chordBlock.append(NEW_LINE);
		
		return chordBlock.toString();
	}
	
	/**
	 * Creates a string with global commands for the given slice.
	 * 
	 * @param slice  the sequence slice
	 * @return the created string (or an empty string, if the slice doesn't contain any global commands)
	 */
	private String createGlobalCommands(Slice slice) {
		StringBuilder result = new StringBuilder("");
		
		// synchronize: set all channels to the highest tick
		long maxTick = Instrument.getMaxCurrentTicks(instrumentsByChannel);
		for (Instrument instr : instrumentsByChannel) {
			instr.setCurrentTicks(maxTick);
		}
		
		// tick comment
		if (MUST_ADD_TICK_COMMENTS)
			result.append( createTickDescription(slice.getBeginTick(), true) + NEW_LINE );
		
		// create global commands
		TreeMap<String, String> globalCmds = slice.getGlobalCommands();
		if (0 == globalCmds.size()) {
			if (slice.getBeginTick() > 0) {
				result.append(MidicaPLParser.GLOBAL + NEW_LINE + NEW_LINE);
			}
		}
		else {
			for (String cmdId : globalCmds.keySet()) {
				String value = globalCmds.get(cmdId);
				
				// get global command
				String globalCmd = MidicaPLParser.TEMPO;
				if ("time".equals(cmdId))
					globalCmd = MidicaPLParser.TIME_SIG;
				else if ("key".equals(cmdId))
					globalCmd = MidicaPLParser.KEY_SIG;
				
				// append command
				result.append(
					  MidicaPLParser.GLOBAL + " "
					+ String.format("%-7s", globalCmd) + " "
					+ value + NEW_LINE
				);
			}
			result.append(NEW_LINE);
		}
		
		return result.toString();
	}
	
	/**
	 * Creates a nestable block with the 'multiple' option at the beginning of a slice.
	 * 
	 * This block doesn't contain any notes.
	 * It may contain only:
	 * 
	 * - rests
	 * - rests with (orphaned) syllables
	 * - (in the future) control changes
	 * 
	 * @param slice      the sequence slice
	 * @param channel    MIDI channel
	 * @return the created block.
	 */
	private String createSliceBeginBlock(Slice slice, byte channel) {
		StringBuilder lines = new StringBuilder();
		TreeMap<Long, String> timeline = slice.getSliceBeginBlockTimeline(channel);
		
		// open the block
		lines.append(MidicaPLParser.BLOCK_OPEN + " " + MidicaPLParser.M);
		lines.append(NEW_LINE);
		
		// get channel and tickstamp
		long currentTicks = instrumentsByChannel.get(channel).getCurrentTicks();
		
		// TICK:
		for (Entry<Long, String> entry : timeline.entrySet()) {
			long   restTick = entry.getKey();
			String syllable = entry.getValue();
			
			// need a normal rest before the syllable?
			if (restTick > currentTicks) {
				long missingTicks = restTick - currentTicks;
				lines.append( "\t" + createRest(channel, missingTicks, currentTicks, null) );
				currentTicks = restTick;
			}
			
			// get tick distance until the next syllable
			Long nextTick = timeline.ceilingKey(currentTicks + 1);
			if (null == nextTick) {
				// last syllable in this slice
				nextTick = currentTicks + sourceResolution; // use a quarter note
			}
			
			long restTicks = nextTick - currentTicks;
			
			// add the rest with the syllable
			lines.append( "\t" + createRest(channel, restTicks, currentTicks, syllable) );
			currentTicks = nextTick;
		}
		
		// close the block
		lines.append(MidicaPLParser.BLOCK_CLOSE);
		lines.append(NEW_LINE);
		
		return lines.toString();
	}
	
	/**
	 * Creates an inline block with a multiple option.
	 * 
	 * Adds a rest before the block, if necessary.
	 * 
	 * @param channel  MIDI channel
	 * @param tick     MIDI tick
	 * @param slice    the sequence slice
	 * @return the created block.
	 */
	private String createInlineBlock(byte channel, long tick, Slice slice) {
		StringBuilder lines = new StringBuilder("");
		
		// add rest, if necessary
		Instrument instr  = instrumentsByChannel.get(channel);
		long currentTicks = instr.getCurrentTicks();
		long missingTicks = tick - currentTicks;
		if (missingTicks > 0) {
			lines.append( createRest(channel, missingTicks, currentTicks, null) );
			instr.setCurrentTicks(tick);
			currentTicks = tick;
		}
		
		// open the block
		String lineOpen = MidicaPLParser.BLOCK_OPEN + " " + MidicaPLParser.M;
		lines.append(appendTickComment(lineOpen, tick));
		lines.append(NEW_LINE);
		
		// add the inline block
		TreeMap<Long, String> content = slice.getInlineBlockTimeline(channel, tick);
		for (Entry<Long, String> entry : content.entrySet()) {
			long   eventTick = entry.getKey();
			String syllable  = entry.getValue();
			
			// add rest before event
			missingTicks = eventTick - currentTicks;
			if (missingTicks > 0) {
				lines.append("\t" + createRest(channel, missingTicks, currentTicks, null));
				currentTicks = eventTick;
			}
			
			// calculate event rest length
			Long nextEventTick = content.ceilingKey(eventTick + 1);
			if (null == nextEventTick) {
				nextEventTick = eventTick + 1;
			}
			long restTicks = nextEventTick - eventTick;
			
			// make sure that the rest is not skipped if it's too short
			if (restTicks < restLength.firstKey()) {
				restTicks = restLength.firstKey();
			}
			
			// add rest with syllable
			lines.append("\t" + createRest(channel, restTicks, currentTicks, syllable));
			currentTicks += restTicks;
		}
		
		// close the block
		lines.append(MidicaPLParser.BLOCK_CLOSE);
		lines.append(NEW_LINE);
		
		return lines.toString();
	}
	
	/**
	 * Creates lines for all notes or chords that are played in a certain
	 * channel and begin at a certain tick.
	 * 
	 * Steps:
	 * 
	 * # If necessary, adds a REST so that the current tick is reached.
	 * # Chooses the LAST note/chord command to be printed.
	 * # Prints all lines apart from the last one, and adds the MULTIPLE option.
	 * # Prints the last line, and adds the MULTIPLE option only if necessary.
	 * # Increments current channel ticks (if the last element has no MULTIPLE option).
	 * 
	 * Strategy to choose the LAST note/chord command:
	 * 
	 * # Choose a note/chord ending in the same tick when the next note/chord starts, if available and in the same slice.
	 *     - no MULTIPLE option needed for the last note/chord
	 *     - no rests are necessary
	 * # Choose a note/chord ending at the end of the slice, if possible, and not later than the next ON-tick
	 *     - no MULTIPLE option needed for the last note/chord
	 *     - rests must be added LATER but not now
	 * # Choose the longest note/chord ending BEFORE the NEXT note/chord starts, if available.
	 *     - no MULTIPLE option needed for the last note/chord
	 *     - rest(s) must be added
	 * # Choose any other note/chord.
	 *     - all chords/notes need the MULTIPLE option, even the last one.
	 *     - rest(s) must be added
	 * 
	 * @param slice    the sequence slice
	 * @param channel  MIDI channel
	 * @param tick     MIDI tick
	 * @param events   All notes/chords with the same note-ON tick in the same channel (comes from the slice's timeline)
	 * @return the created note lines.
	 */
	// TODO: change docu about the strategy
	private String createNoteLines(Slice slice, byte channel, long tick, TreeMap<String, TreeMap<Byte, String>> events) {
		StringBuilder lines = new StringBuilder("");
		
		// add rest, if necessary
		Instrument instr  = instrumentsByChannel.get(channel);
		long currentTicks = instr.getCurrentTicks();
		if (tick > currentTicks) {
			long restTicks = tick - currentTicks;
			lines.append( createRest(channel, restTicks, tick, null) );
			instr.setCurrentTicks(tick);
		}
		
		// get the LAST note/chord to be printed.
		Long   nextOnTick            = noteHistory.get(channel).ceilingKey(tick + 1);
		long   sliceEndTick          = slice.getEndTick();
		String lastNoteOrCrdName     = null;
		long   highestFittingEndTick = -1;
		for (Entry<String, TreeMap<Byte, String>> noteSet : events.entrySet()) {
			String name = noteSet.getKey();
			TreeMap<Byte, String> note = noteSet.getValue();
			
			long endTick = Long.parseLong(note.get(NP_END_TICK));
			
			// next note-ON exists?
			if (nextOnTick != null) {
				
				// next note-ON is in the same slice?
				if (nextOnTick <= sliceEndTick) {
					
					// note/chord fits before next note-ON?
					if (nextOnTick >= endTick) {
						
						// no better candidate found yet?
						if (endTick > highestFittingEndTick) {
							highestFittingEndTick = endTick;
							lastNoteOrCrdName     = name;
						}
					}
				}
			}
			// no next note-ON but note/chord fits into the slice?
			else if (endTick <= sliceEndTick) {
				
				// no better candidate found yet?
				if (endTick > highestFittingEndTick) {
					highestFittingEndTick = endTick;
					lastNoteOrCrdName     = name;
				}
			}
		}
		
		// get notes/chords in the right order
		ArrayList<String> noteOrCrdNames = new ArrayList<>();
		for (Entry<String, TreeMap<Byte, String>> noteSet : events.entrySet()) {
			String name = noteSet.getKey();
			
			// skip the line to be printed last
			if (lastNoteOrCrdName != null && name.equals(lastNoteOrCrdName))
				continue;
			
			noteOrCrdNames.add(name);
		}
		if (lastNoteOrCrdName != null) {
			noteOrCrdNames.add(lastNoteOrCrdName);
		}
		
		// create the lines
		int i = 0;
		for (String name : noteOrCrdNames) {
			i++;
			TreeMap<Byte, String> note = events.get(name);
			
			// add multiple option, if necessary
			if (-1 == highestFittingEndTick || i < noteOrCrdNames.size()) {
				note.put(NP_MULTIPLE, null);
			}
			lines.append( createSingleNoteLine(channel, name, note, tick) );
		}
		
		// increment ticks, if necessary
		if (highestFittingEndTick > 0) {
			instr.setCurrentTicks(highestFittingEndTick);
		}
		
		return lines.toString();
	}
	
	/**
	 * Prints a single channel command for a note or chord.
	 * 
	 * @param channel    MIDI channel
	 * @param noteName   note or chord name
	 * @param noteOrCrd  note properties (from the slice's timeline)
	 * @param tick       MIDI tickstamp.
	 * @return the created line.
	 */
	private String createSingleNoteLine(byte channel, String noteName, TreeMap<Byte, String> noteOrCrd, long tick) {
		StringBuilder line = new StringBuilder("");
		
		Instrument instr = instrumentsByChannel.get(channel);
		
		// main part of the command
		line.append(
			  String.format(FORMAT_CH_CMD_CHANNEL, channel)  + " "
			+ String.format(FORMAT_CH_CMD_CRD,     noteName) + " "
			+ String.format(FORMAT_CH_CMD_LENGTH,  noteOrCrd.get(NP_LENGTH))
		);
		
		// get options that must be appended
		ArrayList<String> options = new ArrayList<>();
		{
			// multiple
			if (noteOrCrd.containsKey(NP_MULTIPLE)) {
				options.add(MidicaPLParser.M);
				incrementStats(STAT_NOTE_MULTIPLE, channel);
			}
			
			// duration
			float duration           = Float.parseFloat( noteOrCrd.get(NP_DURATION) ) / 100;
			float oldDuration        = instr.getDurationRatio();
			int   durationPercent    = (int) ((duration    * 1000 + 0.5f) / 10);
			int   oldDurationPercent = (int) ((oldDuration * 1000 + 0.5f) / 10);
			if (durationPercent != oldDurationPercent) {
				// don't allow 0%
				String durationPercentStr = durationPercent + "";
				if (durationPercent < 1) {
					durationPercentStr = "0.5";
					duration = 0.005f;
				}
				options.add(MidicaPLParser.D + MidicaPLParser.OPT_ASSIGNER + durationPercentStr + MidicaPLParser.DURATION_PERCENT);
				instr.setDurationRatio(duration);
				incrementStats(STAT_NOTE_DURATIONS, channel);
			}
			
			// velocity
			int velocity    = Integer.parseInt( noteOrCrd.get(NP_VELOCITY) );
			int oldVelocity = instr.getVelocity();
			if (velocity != oldVelocity) {
				options.add(MidicaPLParser.V + MidicaPLParser.OPT_ASSIGNER + velocity);
				instr.setVelocity(velocity);
				incrementStats(STAT_NOTE_VELOCITIES, channel);
			}
			
			// add syllable, if needed
			if (noteOrCrd.containsKey(NP_LYRICS)) {
				String syllable = noteOrCrd.get(NP_LYRICS);
				syllable = escapeSyllable(syllable);
				options.add(MidicaPLParser.L + MidicaPLParser.OPT_ASSIGNER + syllable);
			}
		}
		
		// append options
		if (options.size() > 0) {
			String optionsStr = String.join(MidicaPLParser.OPT_SEPARATOR + " ", options);
			line.append(" " + optionsStr);
		}
		
		// finish the line
		return appendTickComment(line.toString(), tick) + NEW_LINE;
	}
	
	/**
	 * Creates a channel command with a rest.
	 * 
	 * @param channel    MIDI channel
	 * @param ticks      tick length of the rest to create
	 * @param beginTick  used for the tick comment (negative value: don't include a tick comment)
	 * @param syllable   a lyrics syllable or (in most cases): **null**
	 * @return the channel command containing the rest.
	 */
	protected String createRest(byte channel, long ticks, long beginTick, String syllable) {
		StringBuilder line = new StringBuilder("");
		
		// split length into elements
		ArrayList<Long> lengthElements = getLengthsForSum(ticks, true);
		
		// transform to strings
		ArrayList<String> lengthSummands = new ArrayList<>();
		for (Long length : lengthElements) {
			String summandStr = restLength.get(length);
			lengthSummands.add(summandStr);
			incrementStats(STAT_REST_SUMMANDS, channel);
			if (summandStr.endsWith(MidicaPLParser.TRIPLET)) {
				incrementStats(STAT_REST_TRIPLETS, channel);
			}
		}
		
		// add line
		if (lengthSummands.size() > 0) {
			String length = String.join(MidicaPLParser.LENGTH_PLUS, lengthSummands);
			line.append(
				  String.format(FORMAT_CH_CMD_CHANNEL, channel) + " "
				+ String.format(FORMAT_CH_CMD_CRD, MidicaPLParser.REST) + " "
				+ String.format(FORMAT_CH_CMD_LENGTH, length)
			);
			
			incrementStats(STAT_RESTS, channel);
		}
		else {
			addWarningRestSkipped(beginTick, ticks, channel);
			incrementStats(STAT_REST_SKIPPED, channel);
			if (MUST_ADD_TICK_COMMENTS)
				line.append(getCommentSymbol() + " " + Dict.get(Dict.WARNING_REST_SKIPPED));
			else
				return ""; // no line needed
		}
		
		// add lyrics option, if needed
		if (syllable != null) {
			syllable = escapeSyllable(syllable);
			line.append(" " + MidicaPLParser.L + MidicaPLParser.OPT_ASSIGNER + syllable);
		}
		
		// finish the line
		if (beginTick < 0)
			return line.toString() + NEW_LINE;
		else
			return appendTickComment(line.toString(), beginTick) + NEW_LINE;
	}
	
	/**
	 * Escapes special characters in syllables.
	 * 
	 * @param syllable  The syllable to be escaped.
	 * @return the replaced syllable.
	 */
	private String escapeSyllable(String syllable) {
		
		// escape \r and \n
		if (isSoftKaraoke) {
			syllable = syllable.replaceAll("\n", "_").replaceAll("\r", "_");
		}
		else {
			syllable = syllable.replaceAll("\n", "\\\\n").replaceAll("\r", "\\\\r");
		}
		
		// escape space and comma
		return syllable.replaceAll(" ", "_").replaceAll(",",  "\\\\c");
	}
	
	/**
	 * Calculates which tick length corresponds to which note or rest length.
	 * That depends on the resolution of the current MIDI sequence.
	 * 
	 * The created rest lengths will contain a view more very short lengths.
	 * This is needed because rests should be less tolerant than notes.
	 * 
	 * This enables us to use more common lengths for notes but let the
	 * exported sequence be still as close as possible to the original one.
	 * 
	 * @param rest    **true** to initialize REST lengths, **false** for NOTE lengths
	 * @return Mapping between tick length and note length for the syntax.
	 */
	public TreeMap<Long, String> initLengths(boolean rest) {
		
		boolean useDots     = rest ? USE_DOTTED_RESTS     : USE_DOTTED_NOTES;
		boolean useTriplets = rest ? USE_TRIPLETTED_RESTS : USE_TRIPLETTED_NOTES;
		
		String triplet = MidicaPLParser.TRIPLET;
		String dot     = MidicaPLParser.DOT;
		String d1      = MidicaPLParser.LENGTH_1;
		String d2      = MidicaPLParser.LENGTH_2;
		String d4      = MidicaPLParser.LENGTH_4;
		String d8      = MidicaPLParser.LENGTH_8;
		String d16     = MidicaPLParser.LENGTH_16;
		String d32     = MidicaPLParser.LENGTH_32;
		String m2      = MidicaPLParser.LENGTH_M2;
		String m4      = MidicaPLParser.LENGTH_M4;
		String m8      = MidicaPLParser.LENGTH_M8;
		String m16     = MidicaPLParser.LENGTH_M16;
		String m32     = MidicaPLParser.LENGTH_M32;
		
		TreeMap<Long, String> lengthToSymbol = new TreeMap<>();
		
		// use very small lengths only for rests
		if (rest) {
			// 1/128
			long length128 = calculateTicks(1, 32);
			lengthToSymbol.put(length128, 128 + "");
			
			// 1/64
			long length64 = calculateTicks(1, 16);
			lengthToSymbol.put(length64, 64 + "");
		}
		
		// 32th
		long length32t = calculateTicks( 2, 8 * 3 ); // inside a triplet
		long length32  = calculateTicks( 1, 8     ); // normal length
		long length32d = calculateTicks( 3, 8 * 2 ); // dotted length
		if (useTriplets) lengthToSymbol.put( length32t, d32 + triplet ); // triplet
		                 lengthToSymbol.put( length32,  d32           ); // normal
		if (useDots)     lengthToSymbol.put( length32d, d32 + dot     ); // dotted
		
		// 16th
		long length16t = calculateTicks( 2, 4 * 3 );
		long length16  = calculateTicks( 1, 4     );
		long length16d = calculateTicks( 3, 4 * 2 );
		if (useTriplets) lengthToSymbol.put( length16t, d16 + triplet );
		                 lengthToSymbol.put( length16,  d16           );
		if (useDots)     lengthToSymbol.put( length16d, d16 + dot     );
		
		// 8th
		long length8t = calculateTicks( 2, 2 * 3 );
		long length8  = calculateTicks( 1, 2     );
		long length8d = calculateTicks( 3, 2 * 2 );
		if (useTriplets) lengthToSymbol.put( length8t, d8 + triplet );
		                 lengthToSymbol.put( length8,  d8           );
		if (useDots)     lengthToSymbol.put( length8d, d8 + dot     );
		
		// quarter
		long length4t = calculateTicks( 2, 3 );
		long length4  = calculateTicks( 1, 1 );
		long length4d = calculateTicks( 3, 2 );
		if (useTriplets) lengthToSymbol.put( length4t, d4 + triplet );
		                 lengthToSymbol.put( length4,  d4           );
		if (useDots)     lengthToSymbol.put( length4d, d4 + dot     );
		
		// half
		long length2t = calculateTicks( 2 * 2, 3 );
		long length2  = calculateTicks( 2,     1 );
		long length2d = calculateTicks( 2 * 3, 2 );
		if (useTriplets) lengthToSymbol.put( length2t, d2 + triplet );
		                 lengthToSymbol.put( length2,  d2           );
		if (useDots)     lengthToSymbol.put( length2d, d2 + dot     );
		
		// full
		long length1t = calculateTicks( 4 * 2, 3 );
		long length1  = calculateTicks( 4,     1 );
		if (useTriplets) lengthToSymbol.put( length1t, d1 + triplet );
		                 lengthToSymbol.put( length1,  d1           );
		
		// allow longer lengths only for rests
		if (rest) {
			// 2 full notes
			long length_m2 = calculateTicks(8, 1);
			lengthToSymbol.put(length_m2,  m2);
			
			// 4 full notes
			long length_m4 = calculateTicks(16, 1);
			lengthToSymbol.put(length_m4, m4);
			
			// 8 full notes
			long length_m8 = calculateTicks(32, 1);
			lengthToSymbol.put(length_m8, m8);
			
			// 16 full notes
			long length_m16 = calculateTicks(64, 1);
			lengthToSymbol.put(length_m16, m16);
			
			// 32 full notes
			long length_m32 = calculateTicks(128, 1);
			lengthToSymbol.put(length_m32, m32);
		}
		
		return lengthToSymbol;
	}
	
	/**
	 * Appends a tick comment to the given line, if tick comments are configured.
	 * Otherwise the given line is returned unchanged.
	 * 
	 * @param line    the line to be completed by a comment
	 * @param tick    MIDI tick
	 * @return the changed or unchanged line
	 */
	private String appendTickComment(String line, long tick) {
		if (MUST_ADD_TICK_COMMENTS) {
			line = String.format("%-45s", line) + " " + createTickDescription(tick, true);
		}
		return line;
	}
}
