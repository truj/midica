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
import java.util.TreeMap;

import org.midica.file.Instrument;
import org.midica.file.read.MidicaPLParser;

/**
 * This class is used to export the currently loaded MIDI sequence as an ALDA source file.
 * 
 * @author Jan Trukenmüller
 */
public class AldaExporter extends Decompiler {
	
	public static ArrayList<String> instrumentNames = new ArrayList<>();
	public static ArrayList<String> noteNames       = new ArrayList<>();
	public static ArrayList<Byte>   noteOctaves     = new ArrayList<>();
	
	private int                          currentSliceNumber = 0;
	private Instrument                   currentInstrument  = null;
	private TreeMap<Instrument, Integer> usedInSlice        = new TreeMap<>();
//	private HashSet<Byte>               usedChannels    = new HashSet<>();
//	private HashMap<String, Instrument> usedInstruments = new HashMap<>();
	
	/**
	 * Creates a new MidicaPL exporter.
	 */
	public AldaExporter() {
		format = ALDA;
	}
	
	/**
	 * Initializes MidicaPL specific data structures.
	 */
	public void init() {
		initInstrumentNames();
		initNoteNames();
	}
	
//	/**
//	 * Increments the statistics for the channel and total.
//	 * 
//	 * @param type     statistics type
//	 * @param channel  MIDI channel
//	 */
//	private void incrementStats(Byte type, Byte channel) {
//		int channelValue = statistics.get(channel).get(type);
//		int totalValue   = statistics.get(STAT_TOTAL).get(type);
//		
//		statistics.get(channel).put(type, channelValue + 1);
//		statistics.get(STAT_TOTAL).put(type, totalValue + 1);
//	}
	
	/**
	 * Creates the MidicaPL string to be written into the export file.
	 * 
	 * @return MidicaPL string to be written into the export file
	 */
	public String createOutput() {
		StringBuilder output = new StringBuilder();
		
		// META block
//		output.append( createMetaBlock() );
		
		// initial INSTRUMENTS block (tick 0)
//		output.append( createInitialInstrumentsBlock() );
		
//		// add chord definitions
//		output.append( createChordDefinitions() );
		
		// SLICE:
		for (Slice slice : slices) {
			currentSliceNumber++;
			
			// if necessary: add rest from current tick to the slice's begin tick
			output.append( createRestBeforeSlice(slice) );
			if (currentSliceNumber > 0) {
				output.append( createMarker() );
			}
			
			// global commands
			output.append( createGlobalCommands(slice) );
			
			// channel commands and instrument changes
			for (byte channel = 0; channel < 16; channel++) {
				
//				// block with rests that are only used for syllables that don't have a note
//				if (slice.hasSyllableRests() && channel == lyricsChannels.get(0)) {
//					output.append( createSyllableRestsBlock(slice) );
//				}
				
				// normal commands
				output.append( createCommandsFromTimeline(slice, channel) );
			}
		}
		
		// config
//		output.append( createConfig() );
		
		// statistics
//		output.append( createStatistics() );
		
		return output.toString();
	}
	
	/**
	 * Creates notes and instrument changes from a slice's timeline.
	 * 
	 * Steps:
	 * 
	 * - Adds the following missing properties and elements to the notes of the timeline:
	 *     - properties:
	 *         - length property for each note/chord
	 *         - multiple property, if neccessary
	 *     - elements:
	 *         - rests, if necessary
	 *     - instrument changes:
	 *         - if a program change is found
	 *         - if a note is played for the first time in a channel
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
//						incrementStats(STAT_NOTE_SUMMANDS, channel);
						if (summandStr.endsWith(MidicaPLParser.TRIPLET)) {
//							incrementStats(STAT_NOTE_TRIPLETS, channel);
						}
					}
					String lengthStr = String.join(MidicaPLParser.LENGTH_PLUS, summandStrings);
					
					// add note length / duration to timeline
					params.put( NP_LENGTH,   lengthStr    );
					params.put( NP_END_TICK, endTick + "" );
					params.put( NP_DURATION, durationPerc );
					
//					incrementStats(STAT_NOTES, channel);
				}
				
				// write MidicaPL
				lines.append( createChordNotes(slice, channel, tick, events.get(ET_NOTES)) );
			}
		}
		
		// add one empty line between channels
		if ( ! timeline.isEmpty() ) {
			lines.append(NEW_LINE);
		}
		
		return lines.toString();
	}
	
	/**
	 * Creates one instrument line for an instrument change in the given channel and tick.
	 * If there is no instrument change event found in the MIDI sequence, the default instrument
	 * (piano) is used.
	 * 
	 * @param channel  MIDI channel
	 * @param tick     MIDI tick
	 * @return the created lines.
	 */
	private String createInstrumentChange(byte channel, long tick) {
		
		// prepare
		StringBuilder content = new StringBuilder(NEW_LINE);
		
		// get program number
		byte program = 0; // default = piano
		Byte[] instrConfig = instrumentHistory.get(channel).get(tick);
		if (instrConfig != null) {
			program = instrConfig[2];
		}
		
		// get instrument name and alias
		String instrName = instrumentNames.get(program);
		String alias     = instrName + "-ch" + channel;
		
		// alias has been used before?
		if (instrumentsByName.containsKey(alias)) {
			currentInstrument = instrumentsByName.get(alias);
			content.append(alias);
		}
		else {
			// create new alias
			content.append(instrName + " \"" + alias + "\"");
			
			// create new channel
			currentInstrument = new Instrument(channel, program, instrName, false);
			currentInstrument.setDurationRatio(0.9f); // alda-default: 90%
			instrumentsByName.put(alias, currentInstrument);
		}
		content.append(":" + NEW_LINE + "\t");
		
		// synchronize data structures
		instrumentsByChannel.add(channel, currentInstrument);
		
		return content.toString();
	}
	
	/**
	 * Creates the block with configuration variables that has been used for decompilation.
	 * 
	 * @return configuration block
	 */
//	private String createConfig() {
//		StringBuilder statLines = new StringBuilder("");
//		
//		if ( ! MUST_ADD_CONFIG )
//			return statLines.toString();
//		
//		// headline
//		statLines.append(MidicaPLParser.COMMENT + " " + "CONFIGURATION:" + NEW_LINE);
//		statLines.append(MidicaPLParser.COMMENT + NEW_LINE);
//		
//		// config values
//		HashMap<String, String> sessionConfig = DecompileConfigController.getSessionConfig();
//		ArrayList<String> configKeys = new ArrayList<String>(sessionConfig.keySet());
//		Collections.sort(configKeys);
//		for (String key : configKeys) {
//			String value = sessionConfig.get(key);
//			statLines.append(MidicaPLParser.COMMENT + " " + key + "\t" + value + NEW_LINE);
//		}
//		statLines.append(NEW_LINE + NEW_LINE);
//		
//		return statLines.toString();
//	}
	
	/**
	 * Creates the statistics to be printed at the end of the produced file.
	 * 
	 * @return statistics block
	 */
//	private String createStatistics() {
//		StringBuilder statLines = new StringBuilder("");
//		
//		if (MUST_ADD_STATISTICS)
//			statLines.append(MidicaPLParser.COMMENT + " " + "STATISTICS:" + NEW_LINE);
//		
//		// channels
//		for (byte channel = 0; channel < 16; channel++) {
//			TreeMap<Byte, Integer> channelStats = statistics.get(channel);
//			
//			// nothing to do?
//			if (0 == channelStats.get(STAT_NOTES) && 0 == channelStats.get(STAT_RESTS))
//				continue;
//			
//			if (MUST_ADD_STATISTICS)
//				statLines.append(MidicaPLParser.COMMENT + " Channel " + channel + ":" + NEW_LINE);
//			statLines.append( createStatisticPart(channelStats, false) );
//		}
//		
//		// total
//		if (MUST_ADD_STATISTICS)
//			statLines.append(MidicaPLParser.COMMENT + " TOTAL:" + NEW_LINE);
//		statLines.append( createStatisticPart(statistics.get(STAT_TOTAL), true) );
//		
//		return statLines.toString();
//	}
	
	/**
	 * Creates the statistics for one part (either a channel or total).
	 * 
	 * @param subStat  statistic structure for the part (channel or total)
	 * @param isTotal  **true**, if this is for the total statistics, **false** for channel statistics
	 * @return the created statistics.
	 */
//	private String createStatisticPart(TreeMap<Byte,Integer> subStat, boolean isTotal) {
//		StringBuilder stats = new StringBuilder("");
//		
//		// markers for the quality score
//		int    markerCount = 0;
//		double markerSum   = 0;
//		
//		// rests
//		{
//			int rests = subStat.get(STAT_RESTS);
//			if (MUST_ADD_STATISTICS)
//				stats.append(MidicaPLParser.COMMENT + "\t" + "Rests: " + rests + NEW_LINE);
//			
//			// rests / notes
//			int notes = subStat.get(STAT_NOTES);
//			if (notes > 0) {
//				double restsPercent = ((double) rests) / ((double) (notes));
//				restsPercent *= 100;
//				String restsPercentStr = String.format("%.2f", restsPercent);
//				if (MUST_ADD_STATISTICS)
//					stats.append(MidicaPLParser.COMMENT + "\t\t" + "Rests/Notes: " + rests + "/" + notes + " (" + restsPercentStr + "%)" + NEW_LINE);
//				markerCount++;
//				markerSum += (100.0D - restsPercent);
//			}
//			
//			if (rests > 0) {
//				
//				// rests skipped
//				double restsSkipped = ((double) subStat.get(STAT_REST_SKIPPED)) / ((double) rests);
//				restsSkipped *= 100;
//				String restsSkippedStr = String.format("%.2f", restsSkipped);
//				if (MUST_ADD_STATISTICS)
//					stats.append(MidicaPLParser.COMMENT + "\t\t" + "Skipped: " + subStat.get(STAT_REST_SKIPPED) + " (" + restsSkippedStr + "%)" + NEW_LINE);
//				markerCount++;
//				markerSum += (100.0D - restsSkipped);
//				
//				// rest summands
//				int    summands        = subStat.get(STAT_REST_SUMMANDS);
//				double summandsPercent = ((double) summands) / ((double) rests);
//				summandsPercent *= 100;
//				String summandsPercentStr = String.format("%.2f", summandsPercent);
//				if (MUST_ADD_STATISTICS)
//					stats.append(MidicaPLParser.COMMENT + "\t\t" + "Summands: " + summands + " (" + summandsPercentStr + "%)" + NEW_LINE);
//				markerCount++;
//				markerSum += 100.0D - (summandsPercent - 100.0D);
//				
//				// rest triplets
//				if (summands > 0) {
//					int triplets = subStat.get(STAT_REST_TRIPLETS);
//					double tripletsPercent = ((double) triplets) / ((double) summands);
//					tripletsPercent *= 100;
//					String tripletsStr = String.format("%.2f", tripletsPercent);
//					if (MUST_ADD_STATISTICS)
//						stats.append(MidicaPLParser.COMMENT + "\t\t" + "Triplets: " + triplets + " (" + tripletsStr + "%)" + NEW_LINE);
//					markerCount++;
//					markerSum += (100.0D - tripletsPercent);
//				}
//			}
//		}
//		
//		// notes
//		{
//			int notes = subStat.get(STAT_NOTES);
//			if (MUST_ADD_STATISTICS)
//				stats.append(MidicaPLParser.COMMENT + "\t" + "Notes: " + notes + NEW_LINE);
//			if (notes > 0) {
//				
//				// note summands
//				int    summands    = subStat.get(STAT_NOTE_SUMMANDS);
//				double summandsPercent = ((double) summands) / ((double) notes);
//				summandsPercent *= 100;
//				String summandsPercentStr = String.format("%.2f", summandsPercent);
//				if (MUST_ADD_STATISTICS)
//					stats.append(MidicaPLParser.COMMENT + "\t\t" + "Summands: " + summands + " (" + summandsPercentStr + "%)" + NEW_LINE);
//				markerCount++;
//				markerSum += 100.0D - (summandsPercent - 100.0D);
//				
//				// note triplets
//				if (summands > 0) {
//					int triplets = subStat.get(STAT_NOTE_TRIPLETS);
//					double tripletsPercent = ((double) triplets) / ((double) summands);
//					tripletsPercent *= 100;
//					String tripletsStr = String.format("%.2f", tripletsPercent);
//					if (MUST_ADD_STATISTICS)
//						stats.append(MidicaPLParser.COMMENT + "\t\t" + "Triplets: " + triplets + " (" + tripletsStr + "%)" + NEW_LINE);
//					markerCount++;
//					markerSum += (100.0D - tripletsPercent);
//				}
//				
//				// velocity changes
//				int    velocities    = subStat.get(STAT_NOTE_VELOCITIES);
//				double velocitiesPercent = ((double) velocities) / ((double) notes);
//				velocitiesPercent *= 100;
//				String velocitiesPercentStr = String.format("%.2f", velocitiesPercent);
//				if (MUST_ADD_STATISTICS)
//					stats.append(MidicaPLParser.COMMENT + "\t\t" + "Velocity changes: " + velocities + " (" + velocitiesPercentStr + "%)" + NEW_LINE);
//				markerCount++;
//				markerSum += (100.0D - velocitiesPercent);
//				
//				// duration changes
//				int    durations   = subStat.get(STAT_NOTE_DURATIONS);
//				double durationPercent = ((double) durations) / ((double) notes);
//				durationPercent *= 100;
//				String durationPercentStr = String.format("%.2f", durationPercent);
//				if (MUST_ADD_STATISTICS)
//					stats.append(MidicaPLParser.COMMENT + "\t\t" + "Duration changes: " + durations + " (" + durationPercentStr + "%)" + NEW_LINE);
//				markerCount++;
//				markerSum += (100.0D - durationPercent);
//				
//				// multiple option
//				int    multiple        = subStat.get(STAT_NOTE_MULTIPLE);
//				double multiplePercent = ((double) multiple) / ((double) notes);
//				multiplePercent *= 100;
//				String multiplePercentStr = String.format("%.2f", multiplePercent);
//				if (MUST_ADD_STATISTICS)
//					stats.append(MidicaPLParser.COMMENT + "\t\t" + "Multiple option: " + multiple + " (" + multiplePercentStr + "%)" + NEW_LINE);
//				markerCount++;
//				markerSum += (100.0D - multiplePercent);
//			}
//		}
//		
//		// quality score
//		if (isTotal) {
//			if (MUST_ADD_STATISTICS && MUST_ADD_QUALITY_SCORE)
//				stats.append(MidicaPLParser.COMMENT + NEW_LINE);
//			double totalScore = ((double) markerSum) / markerCount;
//			String totalScoreStr = String.format("%.2f", totalScore);
//			if (MUST_ADD_QUALITY_SCORE)
//				stats.append(MidicaPLParser.COMMENT + " QUALITY SCORE: " + totalScoreStr + NEW_LINE);
//		}
//		
//		// empty line
//		if (MUST_ADD_STATISTICS || MUST_ADD_QUALITY_SCORE) {
//			if (isTotal)
//				stats.append(NEW_LINE);
//			else if (MUST_ADD_STATISTICS)
//				stats.append(MidicaPLParser.COMMENT + NEW_LINE);
//		}
//		
//		return stats.toString();
//	}
	
	/**
	 * Synchronizes all instruments and creates a string with global commands for the given slice.
	 * 
	 * Global commands are:
	 * 
	 * - tempo changes
	 * - time signature
	 * - key signature
	 * 
	 * @param slice  the sequence slice
	 * @return the created string (or an empty string, if the slice doesn't contain any global commands)
	 */
	private String createGlobalCommands(Slice slice) {
		StringBuilder result = new StringBuilder("");
		
		// synchronize: set all channels to the highest tick
		// TODO: use instrumentsByName instead
		// TODO: move down
		long maxTick = Instrument.getMaxCurrentTicks(instrumentsByChannel);
		for (Instrument instr : instrumentsByChannel) {
			instr.setCurrentTicks(maxTick);
		}
		
		// tick comment
//		result.append( createTickComment(slice.getBeginTick(), false) );
		
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
				result.append(MidicaPLParser.GLOBAL + "\t" + globalCmd + "\t" + value + NEW_LINE);
			}
			result.append(NEW_LINE);
		}
		
		return result.toString();
	}
	
	/**
	 * Creates a chord - all notes beginning in the same tick in the same channel.
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
	// TODO: adjust docu to ALDA
	// TODO: change docu about the strategy
	private String createChordNotes(Slice slice, byte channel, long tick, TreeMap<String, TreeMap<Byte, String>> events) {
		StringBuilder lines = new StringBuilder("");
		
		// add rest, if necessary
		Instrument instr  = instrumentsByChannel.get(channel);
		long currentTicks = instr.getCurrentTicks();
		if (tick > currentTicks) {
			long restTicks = tick - currentTicks;
//			lines.append( createRest(channel, restTicks, tick, null) );
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
			lines.append( createSingleNote(channel, name, note, tick) );
		}
		
		// increment ticks, if necessary
		if (highestFittingEndTick > 0) {
			instr.setCurrentTicks(highestFittingEndTick);
		}
		
		return lines.toString();
	}
	
	/**
	 * Prints a single channel command for a note or chord.
	 * (Or a rest with a syllable, if orphaned syllables are configured as INLINE.)
	 * 
	 * @param channel    MIDI channel
	 * @param noteName   note or chord name
	 * @param noteOrCrd  note properties (from the slice's timeline)
	 * @param tick       MIDI tickstamp.
	 * @return the created line.
	 */
	// TODO: adjust docu to ALDA
	private String createSingleNote(byte channel, String noteName, TreeMap<Byte, String> noteOrCrd, long tick) {
		StringBuilder line = new StringBuilder("");
		
		Instrument instr = instrumentsByChannel.get(channel);
		
		// main part of the command
		line.append(channel + "\t" + noteName + "\t" + noteOrCrd.get(NP_LENGTH));
		
		// get options that must be appended
		ArrayList<String> options = new ArrayList<>();
		{
			// multiple
			if (noteOrCrd.containsKey(NP_MULTIPLE)) {
				options.add(MidicaPLParser.M);
//				incrementStats(STAT_NOTE_MULTIPLE, channel);
			}
			
			// duration and velocity
			if ( ! noteName.equals(MidicaPLParser.REST) ) {
				
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
//					incrementStats(STAT_NOTE_DURATIONS, channel);
				}
				
				// velocity
				int velocity    = Integer.parseInt( noteOrCrd.get(NP_VELOCITY) );
				int oldVelocity = instr.getVelocity();
				if (velocity != oldVelocity) {
					options.add(MidicaPLParser.V + MidicaPLParser.OPT_ASSIGNER + velocity);
					instr.setVelocity(velocity);
//					incrementStats(STAT_NOTE_VELOCITIES, channel);
				}
			}
			
			// add syllable, if needed
//			if (noteOrCrd.containsKey(NP_LYRICS)) {
//				String syllable = noteOrCrd.get(NP_LYRICS);
//				syllable = escapeSyllable(syllable);
//				options.add(MidicaPLParser.L + MidicaPLParser.OPT_ASSIGNER + syllable);
//			}
		}
		
		// append options
		if (options.size() > 0) {
			String optionsStr = String.join(MidicaPLParser.OPT_SEPARATOR + " ", options);
			line.append("\t" + optionsStr);
		}
		
		// finish the line
//		line.append( createTickComment(tick, true) );
		line.append(NEW_LINE);
		
		return line.toString();
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
		StringBuilder content = new StringBuilder("");
		
		// switch instrument, if necessary
		if (currentInstrument != instrumentsByChannel.get(channel))
			createInstrumentChange(channel, beginTick);
		else
			content.append(" ");
		
		// split length into elements
		ArrayList<Long> lengthElements = getLengthsForSum(ticks, true);
		
		// transform to strings
		ArrayList<String> lengthSummands = new ArrayList<>();
		for (Long length : lengthElements) {
			String summandStr = restLength.get(length);
			lengthSummands.add(summandStr);
//			incrementStats(STAT_REST_SUMMANDS, channel);
//			if (summandStr.endsWith(MidicaPLParser.TRIPLET)) {
//				incrementStats(STAT_REST_TRIPLETS, channel);
//			}
		}
		
		// add rest
		if (lengthSummands.size() > 0) {
			String length = String.join("~", lengthSummands);
//			incrementStats(STAT_RESTS, channel);
			content.append("r" + length);
		}
		else {
			// TODO: Dict
			// TODO: add warning
			System.err.println("rest too small to be handled: " + ticks + " ticks");
//			incrementStats(STAT_REST_SKIPPED, channel);
		}
		
		return content.toString();
	}
	
	/**
	 * Creates a marker in the current instrument to mark the end of a slice.
	 * 
	 * @return the created marker
	 */
	protected String createMarker() {
		StringBuilder content = new StringBuilder("");
		content.append(" %slice-" + currentSliceNumber + NEW_LINE);
		return content.toString();
	}
	
	/**
	 * Calculates which tick length corresponds to which note length.
	 * That depends on the resolution of the current MIDI sequence.
	 * 
	 * @return Mapping between tick length and note length for the syntax.
	 */
	public TreeMap<Long, String> initNoteLengths() {
		
		TreeMap<Long, String> noteLength = new TreeMap<>();
		
		// 32th
		long length32t = calculateTicks( 2, 8 * 3 ); // inside a triplet
		long length32  = calculateTicks( 1, 8     ); // normal length
		long length32d = calculateTicks( 3, 8 * 2 ); // dotted length
		long base      = 32;
		noteLength.put( length32t, getTriplet(base) ); // triplet
		noteLength.put( length32,  base + ""        ); // normal
		noteLength.put( length32d, base + "."       ); // dotted
		
		// 16th
		base = 16;
		long length16t = calculateTicks( 2, 4 * 3 );
		long length16  = calculateTicks( 1, 4     );
		long length16d = calculateTicks( 3, 4 * 2 );
		noteLength.put( length16t, getTriplet(base) );
		noteLength.put( length16,  base + ""        );
		noteLength.put( length16d, base + "."       );
		
		// 8th
		base = 8;
		long length8t = calculateTicks( 2, 2 * 3 );
		long length8  = calculateTicks( 1, 2     );
		long length8d = calculateTicks( 3, 2 * 2 );
		noteLength.put( length8t, getTriplet(base) );
		noteLength.put( length8,  base + ""        );
		noteLength.put( length8d, base + "."       );
		
		// quarter
		base = 4;
		long length4t = calculateTicks( 2, 3 );
		long length4  = calculateTicks( 1, 1 );
		long length4d = calculateTicks( 3, 2 );
		noteLength.put( length4t, getTriplet(base) );
		noteLength.put( length4,  base + ""        );
		noteLength.put( length4d, base + "."       );
		
		// half
		base = 2;
		long length2t = calculateTicks( 2 * 2, 3 );
		long length2  = calculateTicks( 2,     1 );
		long length2d = calculateTicks( 2 * 3, 2 );
		noteLength.put( length2t, getTriplet(base) );
		noteLength.put( length2,  base + ""        );
		noteLength.put( length2d, base + "."       );
		
		// full
		long length1  = calculateTicks( 4,     1 );
		long length1d = calculateTicks( 4 * 3, 2 );
		noteLength.put( length1,  "1"  );
		noteLength.put( length1d, "1." );
		
		// 2 full notes
		long length_m2  = calculateTicks( 8,     1 );
		long length_m2d = calculateTicks( 8 * 3, 2 );
		noteLength.put( length_m2,  "0.5"  );
		noteLength.put( length_m2d, "0.5." );
		
		// 4 full notes
		long length_m4  = calculateTicks( 16,     1 );
		long length_m4d = calculateTicks( 16 * 3, 2 );
		noteLength.put( length_m4,  "0.25"  );
		noteLength.put( length_m4d, "0.25." );
		
		// 8 full notes
		long length_m8  = calculateTicks( 32,     1 );
		long length_m8d = calculateTicks( 32 * 3, 2 );
		noteLength.put( length_m8,  "0.125"  );
		noteLength.put( length_m8d, "0.125." );
		
		return noteLength;
	}
	
	/**
	 * Calculates and returns a (rounded) triplet note length.
	 * 
	 * @param base the base note length to be tripleted
	 * @return the triplet symbol
	 */
	private String getTriplet(long base) {
		base = (base * 3) / 2;
		
		return base + "";
	}
	
	/**
	 * Calculates which tick length corresponds to which rest length.
	 * 
	 * Creates the same structure as {@link #initNoteLengths()} but adds
	 * a few shorter lengths as well.
	 * 
	 * This is needed because rests should be less tolerant than notes.
	 * 
	 * This enables us to use more common lengths for notes but let the
	 * exported sequence be still as close as possible to the original one.
	 * 
	 * @return Mapping between tick length and rest length for the syntax.
	 */
	public TreeMap<Long, String> initRestLengths() {
		TreeMap<Long, String> restLength = (TreeMap<Long, String>) noteLength.clone();
		
		// 1/64
		long length64 = calculateTicks(1, 16);
		restLength.put(length64, 64 + "");
		
		// 1/128
		long length128 = calculateTicks(1, 32);
		restLength.put(length128, 128 + "");
		
		// 1/256
		long length256 = calculateTicks(1, 64);
		restLength.put(length256, 256 + "");
		
		// 1/512
		long length512 = calculateTicks(1, 128);
		restLength.put(length512, 512 + "");
		
		return restLength;
	}
	
	/**
	 * Creates a comment giving the current tick - if configured accordingly.
	 * 
	 * Adds a line break, if **must_append** is **true**.
	 * 
	 * @param tick        MIDI tickstamp.
	 * @param mustAppend  **true** for a comment to be appended to a line; **false** for a full-line comment.
	 * @return the comment string.
	 */
//	private String createTickComment(long tick, boolean mustAppend) {
//		
//		// convert source tick to target tick
//		long targetTick = (tick * targetResolution * 10 + 5) / (sourceResolution * 10);
//		
//		String comment = "";
//		if (MUST_ADD_TICK_COMMENTS) {
//			if (mustAppend)
//				comment = "\t\t\t\t";
//			
//			comment += MidicaPLParser.COMMENT + " "
//				+ Dict.get(Dict.EXPORTER_TICK)  + " "
//				+ tick
//				+ " ==> "
//				+ targetTick;
//		}
//		
//		if (mustAppend)
//			return comment;
//		return comment + NEW_LINE;
//	}
	
	/**
	 * Initializes all possible MIDI instrument numbers with an ALDA instrument name.
	 */
	private void initInstrumentNames() {
		instrumentNames.clear();
		
		instrumentNames.add(   0, "piano"                           );
		instrumentNames.add(   1, "midi-bright-acoustic-piano"      );
		instrumentNames.add(   2, "midi-electric-grand-piano"       );
		instrumentNames.add(   3, "midi-honky-tonk-piano"           );
		instrumentNames.add(   4, "midi-electric-piano-1"           );
		instrumentNames.add(   5, "midi-electric-piano-2"           );
		instrumentNames.add(   6, "harpsichord"                     );
		instrumentNames.add(   7, "clavinet"                        );
		instrumentNames.add(   8, "celesta"                         );
		instrumentNames.add(   9, "glockenspiel"                    );
		instrumentNames.add(  10, "music-box"                       );
		instrumentNames.add(  11, "vibraphone"                      );
		instrumentNames.add(  12, "marimba"                         );
		instrumentNames.add(  13, "xylophone"                       );
		instrumentNames.add(  14, "tubular-bells"                   );
		instrumentNames.add(  15, "dulcimer"                        );
		instrumentNames.add(  16, "midi-drawbar-organ"              );
		instrumentNames.add(  17, "midi-percussive-organ"           );
		instrumentNames.add(  18, "midi-rock-organ"                 );
		instrumentNames.add(  19, "organ"                           );
		instrumentNames.add(  20, "midi-reed-organ"                 );
		instrumentNames.add(  21, "accordion"                       );
		instrumentNames.add(  22, "harmonica"                       );
		instrumentNames.add(  23, "midi-tango-accordion"            );
		instrumentNames.add(  24, "guitar"                          );
		instrumentNames.add(  25, "midi-acoustic-guitar-steel"      );
		instrumentNames.add(  26, "midi-electric-guitar-jazz"       );
		instrumentNames.add(  27, "electric-guitar-clean"           );
		instrumentNames.add(  28, "midi-electric-guitar-palm-muted" );
		instrumentNames.add(  29, "electric-guitar-overdrive"       );
		instrumentNames.add(  30, "electric-guitar-distorted"       );
		instrumentNames.add(  31, "electric-guitar-harmonics"       );
		instrumentNames.add(  32, "acoustic-bass"                   );
		instrumentNames.add(  33, "electric-bass"                   );
		instrumentNames.add(  34, "electric-bass-pick"              );
		instrumentNames.add(  35, "fretless-bass"                   );
		instrumentNames.add(  36, "midi-bass-slap"                  );
		instrumentNames.add(  37, "midi-bass-pop"                   );
		instrumentNames.add(  38, "midi-synth-bass-1"               );
		instrumentNames.add(  39, "midi-synth-bass-2"               );
		instrumentNames.add(  40, "violin"                          );
		instrumentNames.add(  41, "viola"                           );
		instrumentNames.add(  42, "cello"                           );
		instrumentNames.add(  43, "contrabass"                      );
		instrumentNames.add(  44, "midi-tremolo-strings"            );
		instrumentNames.add(  45, "midi-pizzicato-strings"          );
		instrumentNames.add(  46, "harp"                            );
		instrumentNames.add(  47, "timpani"                         );
		instrumentNames.add(  48, "midi-string-ensemble-1"          );
		instrumentNames.add(  49, "midi-string-ensemble-2"          );
		instrumentNames.add(  50, "midi-synth-strings-1"            );
		instrumentNames.add(  51, "midi-synth-strings-2"            );
		instrumentNames.add(  52, "midi-choir-aahs"                 );
		instrumentNames.add(  53, "midi-voice-oohs"                 );
		instrumentNames.add(  54, "midi-synth-voice"                );
		instrumentNames.add(  55, "midi-orchestra-hit"              );
		instrumentNames.add(  56, "trumpet"                         );
		instrumentNames.add(  57, "trombone"                        );
		instrumentNames.add(  58, "tuba"                            );
		instrumentNames.add(  59, "midi-muted-trumpet"              );
		instrumentNames.add(  60, "french-horn"                     );
		instrumentNames.add(  61, "midi-brass-section"              );
		instrumentNames.add(  62, "midi-synth-brass-1"              );
		instrumentNames.add(  63, "midi-synth-brass-2"              );
		instrumentNames.add(  64, "soprano-sax"                     );
		instrumentNames.add(  65, "alto-sax"                        );
		instrumentNames.add(  66, "tenor-sax"                       );
		instrumentNames.add(  67, "bari-sax"                        );
		instrumentNames.add(  68, "oboe"                            );
		instrumentNames.add(  69, "english-horn"                    );
		instrumentNames.add(  70, "bassoon"                         );
		instrumentNames.add(  71, "clarinet"                        );
		instrumentNames.add(  72, "piccolo"                         );
		instrumentNames.add(  73, "flute"                           );
		instrumentNames.add(  74, "recorder"                        );
		instrumentNames.add(  75, "pan-flute"                       );
		instrumentNames.add(  76, "bottle"                          );
		instrumentNames.add(  77, "shakuhachi"                      );
		instrumentNames.add(  78, "whistle"                         );
		instrumentNames.add(  79, "ocarina"                         );
		instrumentNames.add(  80, "square"                          );
		instrumentNames.add(  81, "sawtooth"                        );
		instrumentNames.add(  82, "calliope"                        );
		instrumentNames.add(  83, "chiff"                           );
		instrumentNames.add(  84, "charang"                         );
		instrumentNames.add(  85, "midi-solo-vox"                   );
		instrumentNames.add(  86, "midi-fifths"                     );
		instrumentNames.add(  87, "midi-bass-and-lead"              );
		instrumentNames.add(  88, "midi-pad-new-age"                );
		instrumentNames.add(  89, "midi-pad-warm"                   );
		instrumentNames.add(  90, "midi-pad-polysynth"              );
		instrumentNames.add(  91, "midi-pad-choir"                  );
		instrumentNames.add(  92, "midi-pad-bowed"                  );
		instrumentNames.add(  93, "midi-pad-metallic"               );
		instrumentNames.add(  94, "midi-pad-halo"                   );
		instrumentNames.add(  95, "midi-pad-sweep"                  );
		instrumentNames.add(  96, "midi-fx-ice-rain"                );
		instrumentNames.add(  97, "midi-soundtrack"                 );
		instrumentNames.add(  98, "midi-crystal"                    );
		instrumentNames.add(  99, "midi-atmosphere"                 );
		instrumentNames.add( 100, "midi-brightness"                 );
		instrumentNames.add( 101, "midi-goblins"                    );
		instrumentNames.add( 102, "midi-echoes"                     );
		instrumentNames.add( 103, "midi-sci-fi"                     );
		instrumentNames.add( 104, "sitar"                           );
		instrumentNames.add( 105, "banjo"                           );
		instrumentNames.add( 106, "shamisen"                        );
		instrumentNames.add( 107, "koto"                            );
		instrumentNames.add( 108, "kalimba"                         );
		instrumentNames.add( 109, "bagpipes"                        );
		instrumentNames.add( 110, "midi-fiddle"                     );
		instrumentNames.add( 111, "shanai"                          );
		instrumentNames.add( 112, "midi-tinkle-bell"                );
		instrumentNames.add( 113, "midi-agogo"                      );
		instrumentNames.add( 114, "steel-drums"                     );
		instrumentNames.add( 115, "midi-woodblock"                  );
		instrumentNames.add( 116, "midi-taiko-drum"                 );
		instrumentNames.add( 117, "midi-melodic-tom"                );
		instrumentNames.add( 118, "midi-synth-drum"                 );
		instrumentNames.add( 119, "midi-reverse-cymbal"             );
		instrumentNames.add( 120, "midi-guitar-fret-noise"          );
		instrumentNames.add( 121, "midi-breath-noise"               );
		instrumentNames.add( 122, "midi-seashore"                   );
		instrumentNames.add( 123, "midi-bird-tweet"                 );
		instrumentNames.add( 124, "midi-telephone-ring"             );
		instrumentNames.add( 125, "midi-helicopter"                 );
		instrumentNames.add( 126, "midi-applause"                   );
		instrumentNames.add( 127, "midi-gunshot"                    );
	}
	
	/**
	 * Initializes all possible MIDI note numbers with an ALDA instrument and octave name.
	 */
	private void initNoteNames() {
		noteNames.clear();
		noteOctaves.clear();
		
		ArrayList<String> baseNotes = new ArrayList<>();
		baseNotes.add("c");
		baseNotes.add("c+");
		baseNotes.add("d");
		baseNotes.add("d+");
		baseNotes.add("e");
		baseNotes.add("f");
		baseNotes.add("f+");
		baseNotes.add("g");
		baseNotes.add("g+");
		baseNotes.add("a");
		baseNotes.add("a+");
		baseNotes.add("b");
		
		byte num = 0;
		byte oct = -1;
		NOTES:
		while (true) {
			for (String baseName : baseNotes) {
				
				noteNames.add(num, baseName);
				noteOctaves.add(num, oct);
				
				if (127 == num)
					break NOTES;
				num++;
			}
			oct++;
		}
	}
}
