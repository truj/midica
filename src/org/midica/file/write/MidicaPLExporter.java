/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file.write;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;
import java.util.TreeMap;

import org.midica.config.Dict;
import org.midica.file.Instrument;
import org.midica.file.read.MidicaPLParser;
import org.midica.file.read.ParseException;
import org.midica.midi.KaraokeAnalyzer;
import org.midica.midi.SequenceAnalyzer;

/**
 * This class is used to export the currently loaded MIDI sequence as a MidicaPL source file.
 * 
 * @author Jan Trukenmüller
 */
public class MidicaPLExporter extends Decompiler {
	
	// string formats for lowlevel commands
	private String FORMAT_CH_CMD_CHANNEL = "%-2s";   // channel:          2 left-aligned characters, filled with spaces
	private String FORMAT_CH_CMD_CRD     = "%-10s";  // chord/note/rest: 10 left-aligned characters, filled with spaces
	private String FORMAT_CH_CMD_LENGTH  = "%1$10s"; // length:          10 right-aligned characters, filled with spaces
	
	private boolean       isCompactSyntax       = true;
	private boolean       isLowlevelSyntax      = false;
	private int           elementsInCurrentLine = 0;
	private HashSet<Byte> usedInSlice           = null;
	private Long          lineBeginTickSrc      = null;
	private Long          lineBeginTickTgt      = null;
	private boolean       isInBlock             = false;
	
	/**
	 * Creates a new MidicaPL exporter.
	 */
	public MidicaPLExporter() {
		format = MIDICA;
	}
	
	@Override
	public void init() {
		isCompactSyntax       = SYNTAX_COMPACT  == SYNTAX_TYPE;
		isLowlevelSyntax      = SYNTAX_LOWLEVEL == SYNTAX_TYPE;
		elementsInCurrentLine = 0;
		isInBlock             = false;
	}
	
	@Override
	public String createOutput() {
		
		// in MPL we calculate measure lengths based on the TARGET sequence
		// so we need to overwrite the structure from the parent class
		measureLengthHistory.clear();
		measureLengthHistory.put(0L, 4L * sourceResolution); // MIDI default is 4/4
		
		// META block
		createMetaBlock();
		
		// initial INSTRUMENTS block (tick 0)
		createInitialInstrumentsBlock();
		
		// add chord definitions
		createChordDefinitions();
		
		// SLICE:
		for (Slice slice : slices) {
			usedInSlice = new HashSet<>();
			
			// if necessary: add rest from current tick to the slice's begin tick
			createRestBeforeSlice(slice);
			
			// global commands
			createGlobalCommands(slice);
			
			// channel commands and instrument changes
			for (byte channel = 0; channel < 16; channel++) {
				resetTickCommentLineLength();
				
				// Add nestable block at the slice begin, if needed.
				// This may contain orphaned syllables and/or (in the future) control changes.
				if (slice.hasSliceBeginBlock(channel)) {
					createSliceBeginBlock(slice, channel);
				}
				
				// normal commands
				createCommandsFromTimeline(slice, channel);
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
	 * Creates nothing, if the slice's timeline doesn't contain anything in the given channel.
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
	 */
	private void createCommandsFromTimeline(Slice slice, byte channel) {
		TreeMap<Long, TreeMap<Byte, TreeMap<String, TreeMap<Byte, String>>>> timeline = slice.getTimeline(channel);
		
		// TICK:
		for (Entry<Long, TreeMap<Byte, TreeMap<String, TreeMap<Byte, String>>>> timelineSet : timeline.entrySet()) {
			long tick = timelineSet.getKey();
			TreeMap<Byte, TreeMap<String, TreeMap<Byte, String>>> events = timelineSet.getValue();
			
			// instrument change
			if (events.containsKey(ET_INSTR)) {
				createInstrumentChange(channel, tick);
			}
			
			// inline block
			if (events.containsKey(ET_INLINE_BLK)) {
				createInlineBlock(channel, tick, slice);
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
				createNotesAtTick(slice, channel, tick, events.get(ET_NOTES));
			}
		}
		
		// add one empty line between channels
		if (! timeline.isEmpty()) {
			if (isCompactSyntax)
				createCompactLineCloseIfPossible(channel);
			output.append(NEW_LINE);
		}
	}
	
	/**
	 * Creates the META block, if the sequence contains any META information.
	 * Creates nothing, if the sequence doesn't contain any meta information.
	 */
	private void createMetaBlock() {
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
					lines.add(BLOCK_INDENT + String.format("%-12s", mplIds[i]) + " " + singleLine + NEW_LINE);
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
			return;
		
		// add block
		output.append(MidicaPLParser.META + NEW_LINE);
		for (String line : lines) {
			output.append(line);
		}
		output.append(MidicaPLParser.END + NEW_LINE + NEW_LINE);
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
		block.append(BLOCK_INDENT + MidicaPLParser.META_SOFT_KARAOKE + NEW_LINE);
		
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
		block.append(BLOCK_INDENT + MidicaPLParser.END + NEW_LINE);
		
		return block.toString();
	}
	
	/**
	 * Creates the initial INSTRUMENTS block.
	 */
	private void createInitialInstrumentsBlock() {
		
		// open block
		output.append(MidicaPLParser.INSTRUMENTS + NEW_LINE);
		
		// add instruments
		for (byte channel = 0; channel < 16; channel++) {
			createInstrLine(0, channel);
		}
		
		// close block
		output.append(MidicaPLParser.END + NEW_LINE + NEW_LINE);
	}
	
	/**
	 * Creates one INSTRUMENT line for an instrument change in the given channel and tick.
	 * 
	 * @param channel  MIDI channel
	 * @param tick     MIDI tick
	 */
	private void createInstrumentChange(byte channel, long tick) {
		
		// add rest if needed
		long restBeginTick = srcInstrByChannel.get(channel).getCurrentTicks();
		long restTicks     = tick - restBeginTick;
		if (restTicks > 0) {
			createRest(channel, restTicks, null);
		}
		
		// close compact line, if necessary
		createCompactLineCloseIfPossible(channel);
		
		// add instruments
		Set<Long> changeTicks = instrumentHistory.get(channel).keySet();
		if (changeTicks.contains(tick)) {
			createInstrLine(tick, channel);
		}
	}
	
	/**
	 * Creates one line inside an INSTRUMENTS block **or** one single instrument change line.
	 * 
	 * If tick is 0, a line inside a block is created. Otherwise it's an instrument change line.
	 * 
	 * Creates nothing, if no instruments must be defined or changed in the given channel and tick.
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
	 */
	private void createInstrLine(long tick, byte channel) {
		
		// channel used?
		if (0 == noteHistory.get(channel).size()) {
			return;
		}
		
		// get the channel's history
		TreeMap<Long, Byte[]> chInstrHist = instrumentHistory.get(channel);
		Byte[]  instrConfig;
		boolean isAutoChannel = false;
		
		String cmd = BLOCK_INDENT;
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
				return;
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
			output.append(
				  cmd
				+ String.format("%-4s", channelStr)
				+ " "
				+ String.format("%-22s", programStr)
				+ " "
				+ commentStr
				+ NEW_LINE
			);
			return;
		}
		
		// single instrument change
		output.append(cmd + "  " + channelStr + "  " + programStr);
		createTickComment(tick, tgtInstrByChannel.get(channel).getCurrentTicks());
		output.append(NEW_LINE);
	}
	
	/**
	 * Creates the CHORD definitions.
	 */
	private void createChordDefinitions() {
		
		// no chords available?
		if (chords.isEmpty()) {
			return;
		}
		
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
				output.append(MidicaPLParser.CHORD + " " + String.format("%-12s", chordName)  + " ");
				
				// notes
				String[]          noteNumbers = notesStr.split("\\,");
				ArrayList<String> noteNames   = new ArrayList<>();
				for (String noteNumber : noteNumbers) {
					String noteName = Dict.getNote(Integer.parseInt(noteNumber));
					noteNames.add(noteName);
				}
				output.append( String.join(MidicaPLParser.CHORD_SEPARATOR, noteNames) );
				output.append(NEW_LINE);
			}
		}
		output.append(NEW_LINE);
	}
	
	/**
	 * Creates the global commands for the given slice.
	 * 
	 * @param slice  the sequence slice
	 */
	private void createGlobalCommands(Slice slice) {
		
		// synchronize: set all channels to the highest tick
		// and: close compact line, if necessary
		long maxSrcTick = Instrument.getMaxCurrentTicks(srcInstrByChannel);
		long maxTgtTick = Instrument.getMaxCurrentTicks(tgtInstrByChannel);
		for (byte channel = 0; channel < srcInstrByChannel.size(); channel++) {
			createCompactLineCloseIfPossible(channel);
			srcInstrByChannel.get(channel).setCurrentTicks(maxSrcTick);
			tgtInstrByChannel.get(channel).setCurrentTicks(maxTgtTick);
		}
		
		// tick comment
		if (MUST_ADD_TICK_COMMENTS) {
			createTickDescription(slice.getBeginTick(), maxTgtTick, true);
			output.append(NEW_LINE);
		}
		
		// create global commands
		TreeMap<String, String> globalCmds = slice.getGlobalCommands();
		if (0 == globalCmds.size()) {
			if (slice.getBeginTick() > 0) {
				output.append(MidicaPLParser.GLOBAL + NEW_LINE + NEW_LINE);
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
				output.append(
					  MidicaPLParser.GLOBAL + " "
					+ String.format("%-7s", globalCmd) + " "
					+ value + NEW_LINE
				);
				
				// update measure length, if needed
				if ("time".equals(cmdId)) {
					Pattern pattern = Pattern.compile("^(\\d+)" + Pattern.quote(MidicaPLParser.TIME_SIG_SLASH) + "(\\d+)$");
					Matcher matcher = pattern.matcher(value);
					if (matcher.matches()) {
						int numerator      = Integer.parseInt(matcher.group(1));
						int denominator    = Integer.parseInt(matcher.group(2));
						long measureLength = numerator * 4 * targetResolution / denominator;
						measureLengthHistory.put(maxTgtTick, measureLength);
					}
				}
			}
			output.append(NEW_LINE);
		}
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
	 */
	private void createSliceBeginBlock(Slice slice, byte channel) {
		TreeMap<Long, String> timeline = slice.getSliceBeginBlockTimeline(channel);
		
		// remember current ticks
		long beginSrcTicks = srcInstrByChannel.get(channel).getCurrentTicks();
		long beginTgtTicks = tgtInstrByChannel.get(channel).getCurrentTicks();
		
		// open the block
		createCompactLineCloseIfPossible(channel);
		resetTickCommentLineLength();
		output.append(MidicaPLParser.BLOCK_OPEN + " " + MidicaPLParser.M);
		output.append(NEW_LINE);
		isInBlock = true;
		
		// get channel and tickstamp
		long currentTicks = beginSrcTicks;
		
		// TICK:
		for (Entry<Long, String> entry : timeline.entrySet()) {
			long   restTick = entry.getKey();
			String syllable = entry.getValue();
			
			// need a normal rest before the syllable?
			if (restTick > currentTicks) {
				long missingTicks = restTick - currentTicks;
				if (isLowlevelSyntax)
					output.append(BLOCK_INDENT);
				createRest(channel, missingTicks, null);
				currentTicks = restTick;
			}
			
			// get tick distance until the next syllable
			Long nextTick = timeline.ceilingKey(currentTicks + 1);
			if (null == nextTick) {
				// last syllable in this slice
				nextTick = currentTicks; // use a zero-length rest
			}
			
			long restTicks = nextTick - currentTicks;
			
			// add the rest with the syllable
			if (isLowlevelSyntax)
				output.append(BLOCK_INDENT);
			createRest(channel, restTicks, syllable);
			currentTicks = nextTick;
		}
		
		// close the block
		isInBlock = false;
		createCompactLineCloseIfPossible(channel);
		resetTickCommentLineLength();
		output.append(MidicaPLParser.BLOCK_CLOSE);
		output.append(NEW_LINE);
		
		// restore current ticks
		srcInstrByChannel.get(channel).setCurrentTicks(beginSrcTicks);
		tgtInstrByChannel.get(channel).setCurrentTicks(beginTgtTicks);
	}
	
	/**
	 * Creates an inline block with a multiple option.
	 * 
	 * Adds a rest before the block, if necessary.
	 * 
	 * @param channel  MIDI channel
	 * @param tick     MIDI tick
	 * @param slice    the sequence slice
	 */
	private void createInlineBlock(byte channel, long tick, Slice slice) {
		
		// add rest, if necessary
		Instrument instr  = srcInstrByChannel.get(channel);
		long currentTicks = instr.getCurrentTicks();
		long missingTicks = tick - currentTicks;
		if (missingTicks > 0) {
			createRest(channel, missingTicks, null);
			instr.setCurrentTicks(tick);
			currentTicks = tick;
		}
		
		// remember current ticks
		long beginSrcTicks = srcInstrByChannel.get(channel).getCurrentTicks();
		long beginTgtTicks = tgtInstrByChannel.get(channel).getCurrentTicks();
		
		// open the block
		createCompactLineCloseIfPossible(channel);
		resetTickCommentLineLength();
		String lineOpen = MidicaPLParser.BLOCK_OPEN + " " + MidicaPLParser.M;
		output.append(lineOpen);
		createTickComment(tick, beginTgtTicks);
		output.append(NEW_LINE);
		isInBlock = true;
		
		// add the inline block
		TreeMap<Long, String> content = slice.getInlineBlockTimeline(channel, tick);
		for (Entry<Long, String> entry : content.entrySet()) {
			long   eventTick = entry.getKey();
			String syllable  = entry.getValue();
			
			// add rest before event
			missingTicks = eventTick - currentTicks;
			if (missingTicks > 0) {
				if (isLowlevelSyntax)
					output.append(BLOCK_INDENT);
				createRest(channel, missingTicks, null);
				currentTicks = eventTick;
			}
			
			// calculate event rest length
			boolean isLast = false;
			Long nextEventTick = content.ceilingKey(eventTick + 1);
			if (null == nextEventTick) {
				// last syllable in this inline block
				isLast        = true;
				nextEventTick = eventTick; // use a zero-length rest
			}
			long restTicks = nextEventTick - eventTick;
			
			// make sure to use zero-length rests only for the last rest of the block
			if (! isLast && restTicks < restLength.firstKey()) {
				restTicks = restLength.firstKey();
			}
			
			// add rest with syllable
			if (isLowlevelSyntax)
				output.append(BLOCK_INDENT);
			createRest(channel, restTicks, syllable);
			currentTicks += restTicks;
		}
		
		// close the block
		isInBlock = false;
		createCompactLineCloseIfPossible(channel);
		resetTickCommentLineLength();
		output.append(MidicaPLParser.BLOCK_CLOSE);
		output.append(NEW_LINE);
		
		// restore current ticks
		srcInstrByChannel.get(channel).setCurrentTicks(beginSrcTicks);
		tgtInstrByChannel.get(channel).setCurrentTicks(beginTgtTicks);
	}
	
	/**
	 * Creates commands for all notes or chords that are played in a certain
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
	 */
	// TODO: change docu about the strategy
	private void createNotesAtTick(Slice slice, byte channel, long tick, TreeMap<String, TreeMap<Byte, String>> events) {
		
		// close and/or open compact line, if necessary
		createCompactLineChangeIfNeeded(channel);
		
		// add rest, if necessary
		Instrument instr  = srcInstrByChannel.get(channel);
		long currentTicks = instr.getCurrentTicks();
		if (tick > currentTicks) {
			long restTicks = tick - currentTicks;
			createRest(channel, restTicks, null);
			createCompactLineChangeIfNeeded(channel);
		}
		
		// get the LAST note/chord to be printed.
		Long nextOnTick    = noteHistory.get(channel).ceilingKey(tick + 1);
		Long nextInstrTick = instrumentHistory.get(channel).ceilingKey(tick + 1);
		Long nextOnOrInstrTick = null == nextOnTick ? nextInstrTick : nextOnTick;
		if (nextOnOrInstrTick != null && nextInstrTick != null) {
			nextOnOrInstrTick = nextOnTick < nextInstrTick ? nextOnTick : nextInstrTick;
		}
		long    sliceEndTick          = slice.getEndTick();
		String  lastNoteOrCrdName     = null;
		long    highestFittingEndTick = -1;
		boolean isCandidate           = false;
		for (Entry<String, TreeMap<Byte, String>> noteSet : events.entrySet()) {
			String name = noteSet.getKey();
			TreeMap<Byte, String> note = noteSet.getValue();
			
			long endTick = Long.parseLong(note.get(NP_END_TICK));
			
			// next note-ON exists?
			if (nextOnOrInstrTick != null) {
				
				// next note-ON is in the same slice?
				if (nextOnOrInstrTick <= sliceEndTick) {
					
					// note/chord fits before next note-ON?
					if (nextOnOrInstrTick >= endTick) {
						isCandidate = true;
					}
				}
				else if (endTick <= sliceEndTick) {
					isCandidate = true;
				}
			}
			// no next note-ON but note/chord fits into the slice?
			else if (endTick <= sliceEndTick) {
				isCandidate = true;
			}
			
			// is this the best candidate?
			if (isCandidate && endTick > highestFittingEndTick) {
				highestFittingEndTick = endTick;
				lastNoteOrCrdName     = name;
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
			createSingleNoteOrChord(channel, name, note, tick);
		}
		
		// bar line
		createBarlineIfNeeded(channel);
	}
	
	/**
	 * Creates a single channel command for a note or chord.
	 * (Lowlevel command or compact element plus options.)
	 * 
	 * @param channel    MIDI channel
	 * @param noteName   note or chord name
	 * @param noteOrCrd  note properties (from the slice's timeline)
	 * @param tick       MIDI tickstamp.
	 */
	private void createSingleNoteOrChord(byte channel, String noteName, TreeMap<Byte, String> noteOrCrd, long tick) {
		
		Instrument instr     = srcInstrByChannel.get(channel);
		long targetBeginTick = tgtInstrByChannel.get(channel).getCurrentTicks();
		
		// lowlevel: main part of the command
		if (isLowlevelSyntax) {
			output.append(
				  String.format(FORMAT_CH_CMD_CHANNEL, channel)  + " "
				+ String.format(FORMAT_CH_CMD_CRD,     noteName) + " "
				+ String.format(FORMAT_CH_CMD_LENGTH,  noteOrCrd.get(NP_LENGTH))
			);
		}
		
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
			int   durationPercent    = (int) ((duration    * 1000 + 5f) / 10);
			int   oldDurationPercent = (int) ((oldDuration * 1000 + 5f) / 10);
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
		
		// options
		if (options.size() > 0) {
			if (isLowlevelSyntax) {
				String optionsStr = String.join(MidicaPLParser.OPT_SEPARATOR + " ", options);
				output.append(" " + optionsStr);
			}
			else {
				String optionsStr = String.join(MidicaPLParser.OPT_SEPARATOR, options);
				output.append(" " + MidicaPLParser.COMPACT_OPT_OPEN);
				output.append(optionsStr);
				output.append(MidicaPLParser.COMPACT_OPT_CLOSE);
			}
		}
		
		// increment current ticks
		if (!noteOrCrd.containsKey(NP_MULTIPLE)) {
			long srcEndTick = Long.parseLong(noteOrCrd.get(NP_END_TICK));
			srcInstrByChannel.get(channel).setCurrentTicks(srcEndTick);
			incrementTargetTicks(channel, noteOrCrd.get(NP_LENGTH));
		}
		
		// add compact element
		if (isCompactSyntax) {
			createCompactElement(noteName, channel, noteOrCrd.get(NP_LENGTH));
			return;
		}
		
		// finish the line
		createTickComment(tick, targetBeginTick);
		output.append(NEW_LINE);
	}
	
	/**
	 * In compact syntax: returns the opening, closing or switching of a line for
	 * the given channel, if necessary.
	 * Returns an empty string if none is needed or in lowlevel mode.
	 * 
	 * @param channel  MIDI channel
	 */
	private void createCompactLineChangeIfNeeded(byte channel) {
		
		if (isLowlevelSyntax)
			return;
		
		// close line, if needed
		if (usedInSlice.contains(channel)) {
			if (elementsInCurrentLine >= ELEMENTS_PER_LINE) {
				createCompactLineClose(channel);
			}
		}
		
		// open line, if needed
		if (!usedInSlice.contains(channel)) {
			String channelStr = 9 == channel ? MidicaPLParser.P : channel + "";
			usedInSlice.add(channel);
			if (isInBlock)
				output.append(BLOCK_INDENT);
			output.append(channelStr + MidicaPLParser.COMPACT_CHANNEL);
			lineBeginTickSrc = srcInstrByChannel.get(channel).getCurrentTicks();
			lineBeginTickTgt = tgtInstrByChannel.get(channel).getCurrentTicks();
			createBarlineIfNeeded(channel);
		}
	}
	
	/**
	 * Closes a compact line, if one is open and compact syntax is used.
	 * 
	 * @param channel  MIDI channel
	 */
	private void createCompactLineCloseIfPossible(byte channel) {
		
		if (isCompactSyntax && usedInSlice.contains(channel)) {
			createCompactLineClose(channel);
		}
	}
	
	/**
	 * Closes a compact, if compact syntax is used.
	 * 
	 * @param channel  MIDI channel
	 */
	private void createCompactLineClose(byte channel) {
		
		if (isLowlevelSyntax)
			return;
		
		usedInSlice.remove(channel);
		elementsInCurrentLine = 0;
		if (MUST_ADD_TICK_COMMENTS) {
			long currentSrcTicks = srcInstrByChannel.get(channel).getCurrentTicks();
			long currentTgtTicks = tgtInstrByChannel.get(channel).getCurrentTicks();
			createTickLineComment(lineBeginTickSrc, currentSrcTicks, lineBeginTickTgt, currentTgtTicks);
		}
		output.append(NEW_LINE);
	}
	
	/**
	 * Creates a barline for the given channel, if needed.
	 * 
	 * @param channel  MIDI channel
	 */
	private void createBarlineIfNeeded(byte channel) {
		
		// barlines not supported?
		if (isLowlevelSyntax || !USE_BARLINES) {
			return;
		}
		
		// get current tolerance and ticks
		Instrument tgtInstr  = tgtInstrByChannel.get(channel);
		int  currentTgtTol   = tgtInstr.getBarLineTolerance();
		long currentTgtTicks = tgtInstr.getCurrentTicks();
		
		// get measure length and ticks since last time signature
		Entry<Long, Long> entry = measureLengthHistory.floorEntry(tgtInstr.getCurrentTicks());
		long lastTimeSigTick = entry.getKey();
		long measureLength   = entry.getValue();
		long totalTicks      = currentTgtTicks - lastTimeSigTick;
		
		// get delta
		long srcDelta  = totalTicks % measureLength;
		long srcDelta2 = measureLength - srcDelta;
		if (srcDelta2 < srcDelta)
			srcDelta = srcDelta2;
		long tgtDelta = (srcDelta * targetResolution * 10 + sourceResolution * 5) / (sourceResolution * 10);
		
		// no bar line at all?
		if (tgtDelta > MAX_BARLINE_TOL) {
			return;
		}
		
		// create bar line
		String barline = " " + MidicaPLParser.BAR_LINE;
		
		// need to increase tolerance?
		if (tgtDelta > currentTgtTol) {
			barline += tgtDelta;
			tgtInstr.setBarLineTolerance((int) tgtDelta);
		}
		
		output.append(barline);
	}
	
	/**
	 * Creates a single compact element.
	 * 
	 * @param noteName  note, chord or rest
	 * @param channel   MIDI channel
	 * @param length    note length
	 */
	private void createCompactElement(String noteName, byte channel, String length) {
		
		output.append(" " + noteName);
		
		// switch note length, if needed
		Instrument instr = srcInstrByChannel.get(channel);
		String oldLength = instr.getNoteLength();
		if (! oldLength.equals(length)) {
			output.append(MidicaPLParser.COMPACT_NOTE_SEP + length);
			instr.setNoteLength(length);
		}
		elementsInCurrentLine++;
	}
	
	@Override
	protected void createRest(byte channel, long ticks, String syllable) {
		long srcBeginTick = srcInstrByChannel.get(channel).getCurrentTicks();
		long tgtBeginTick = tgtInstrByChannel.get(channel).getCurrentTicks();
		
		// compact line opening needed, if called from createRestBeforeSlice()
		createCompactLineChangeIfNeeded(channel);
		
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
		
		// prepare lyrics
		String optionsStr = "";
		if (syllable != null) {
			syllable = escapeSyllable(syllable);
			if (isLowlevelSyntax) {
				optionsStr = " " + MidicaPLParser.L + MidicaPLParser.OPT_ASSIGNER + syllable;
			}
			else {
				optionsStr = " " + MidicaPLParser.COMPACT_OPT_OPEN
					+ MidicaPLParser.L + MidicaPLParser.OPT_ASSIGNER + syllable
					+ MidicaPLParser.COMPACT_OPT_CLOSE;
				output.append(optionsStr);
			}
		}
		
		// zero-length?
		if (0 == ticks && 0 == lengthSummands.size()) {
			lengthSummands.add(MidicaPLParser.LENGTH_ZERO);
		}
		
		// add line
		if (lengthSummands.size() > 0) {
			String length = String.join(MidicaPLParser.LENGTH_PLUS, lengthSummands);
			if (isLowlevelSyntax) {
				output.append(
					  String.format(FORMAT_CH_CMD_CHANNEL, channel) + " "
					+ String.format(FORMAT_CH_CMD_CRD, MidicaPLParser.REST) + " "
					+ String.format(FORMAT_CH_CMD_LENGTH, length)
				);
			}
			else {
				createCompactElement(MidicaPLParser.REST, channel, length);
			}
			
			srcInstrByChannel.get(channel).setCurrentTicks(srcBeginTick + ticks);
			incrementTargetTicks(channel, length);
			incrementStats(STAT_RESTS, channel);
		}
		else {
			addWarningRestSkipped(srcBeginTick, ticks, channel);
			incrementStats(STAT_REST_SKIPPED, channel);
			if (MUST_ADD_TICK_COMMENTS && isLowlevelSyntax)
				output.append(getCommentSymbol() + " " + Dict.get(Dict.WARNING_REST_SKIPPED));
			else
				return; // no output needed
		}
		
		if (isCompactSyntax) {
			createBarlineIfNeeded(channel);
			return;
		}
		else {
			// add lyrics option, if needed
			output.append(optionsStr);
			
			// finish the line
			createTickComment(srcBeginTick, tgtBeginTick);
			output.append(NEW_LINE);
		}
	}
	
	/**
	 * Increments the target channel by the amount of ticks of the given length value.
	 * 
	 * @param channel  MIDI channel
	 * @param length   note length in MPL syntax
	 */
	private void incrementTargetTicks(byte channel, String length) {
		
		// get current ticks
		try {
			Instrument targetInstr = tgtInstrByChannel.get(channel);
			long currentTicks = targetInstr.getCurrentTicks();
			long lengthTicks  = MidicaPLParser.parseDuration(length);
			targetInstr.setCurrentTicks(currentTicks + lengthTicks);
		}
		catch (ParseException e) {
			System.err.println("Unable to calculate target note length. This should not happen. Please report.");
		}
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
	
	@Override
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
		if (isCompactSyntax) {
			d1  = "1";
			d2  = "2";
			d4  = "4";
			d8  = "8";
			d16 = "16";
			d32 = "32";
		}
		
		TreeMap<Long, String> lengthToSymbol = new TreeMap<>();
		
		// use very small lengths only for rests
		if (rest) {
			// 1/128
			long length128 = calculateTicks(1, 32, LM_NONE, false);
			lengthToSymbol.put(length128, 128 + "");
			
			// 1/64
			long length64 = calculateTicks(1, 16, LM_NONE, false);
			lengthToSymbol.put(length64, 64 + "");
		}
		
		// 32th
		long length32t = calculateTicks(1, 8, LM_TRIPLET, false);
		long length32  = calculateTicks(1, 8, LM_NONE, false);
		long length32d = calculateTicks(1, 8, LM_DOT, false);
		if (useTriplets) lengthToSymbol.put( length32t, d32 + triplet );
		                 lengthToSymbol.put( length32,  d32           );
		if (useDots)     lengthToSymbol.put( length32d, d32 + dot     );
		
		// 16th
		long length16t = calculateTicks(1, 4, LM_TRIPLET, false);
		long length16  = calculateTicks(1, 4, LM_NONE, false);
		long length16d = calculateTicks(1, 4, LM_DOT, false);
		if (useTriplets) lengthToSymbol.put( length16t, d16 + triplet );
		                 lengthToSymbol.put( length16,  d16           );
		if (useDots)     lengthToSymbol.put( length16d, d16 + dot     );
		
		// 8th
		long length8t = calculateTicks(1, 2, LM_TRIPLET, false);
		long length8  = calculateTicks(1, 2, LM_NONE, false);
		long length8d = calculateTicks(1, 2, LM_DOT, false);
		if (useTriplets) lengthToSymbol.put( length8t, d8 + triplet );
		                 lengthToSymbol.put( length8,  d8           );
		if (useDots)     lengthToSymbol.put( length8d, d8 + dot     );
		
		// quarter
		long length4t = calculateTicks(1, 1, LM_TRIPLET, false);
		long length4  = calculateTicks(1, 1, LM_NONE, false);
		long length4d = calculateTicks(1, 1, LM_DOT, false);
		if (useTriplets) lengthToSymbol.put( length4t, d4 + triplet );
		                 lengthToSymbol.put( length4,  d4           );
		if (useDots)     lengthToSymbol.put( length4d, d4 + dot     );
		
		// half
		long length2t = calculateTicks(2, 1, LM_TRIPLET, false);
		long length2  = calculateTicks(2, 1, LM_NONE, false);
		long length2d = calculateTicks(2, 1, LM_DOT, false);
		if (useTriplets) lengthToSymbol.put( length2t, d2 + triplet );
		                 lengthToSymbol.put( length2,  d2           );
		if (useDots)     lengthToSymbol.put( length2d, d2 + dot     );
		
		// full
		long length1t = calculateTicks(4, 1, LM_TRIPLET, false);
		long length1  = calculateTicks(4, 1, LM_NONE, false);
		if (useTriplets) lengthToSymbol.put( length1t, d1 + triplet );
		                 lengthToSymbol.put( length1,  d1           );
		
		// allow longer lengths only for rests
		if (rest) {
			// 2 full notes
			long length_m2 = calculateTicks(8, 1, LM_NONE, false);
			lengthToSymbol.put(length_m2,  m2);
			
			// 4 full notes
			long length_m4 = calculateTicks(16, 1, LM_NONE, false);
			lengthToSymbol.put(length_m4, m4);
			
			// 8 full notes
			long length_m8 = calculateTicks(32, 1, LM_NONE, false);
			lengthToSymbol.put(length_m8, m8);
			
			// 16 full notes
			long length_m16 = calculateTicks(64, 1, LM_NONE, false);
			lengthToSymbol.put(length_m16, m16);
			
			// 32 full notes
			long length_m32 = calculateTicks(128, 1, LM_NONE, false);
			lengthToSymbol.put(length_m32, m32);
		}
		
		return lengthToSymbol;
	}
	
	/**
	 * Creates a tick comment, if tick comments are configured.
	 * 
	 * @param srcTick    MIDI tick in source resolution
	 * @param tgtTick    MIDI tick in target resolution
	 */
	private void createTickComment(long srcTick, long tgtTick) {
		
		if (!MUST_ADD_TICK_COMMENTS)
			return;
		
		// create spaces
		if (isLowlevelSyntax) {
			int lastLineLength = output.length() - output.lastIndexOf(NEW_LINE);
			int spaces         = 44 - lastLineLength;
			output.append(" "); // minimum 1 space
			for (int i = 0; i < spaces; i++)
				output.append(" ");
		}
		else {
			createSpacesBeforeComment();
		}
		
		// create the comment
		createTickDescription(srcTick, tgtTick, true);
	}
}
