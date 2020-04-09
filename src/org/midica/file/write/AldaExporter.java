/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file.write;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.TreeMap;
import java.util.TreeSet;

import org.midica.file.Instrument;

/**
 * This class is used to export the currently loaded MIDI sequence as an ALDA source file.
 * 
 * @author Jan Trukenmüller
 */
public class AldaExporter extends Decompiler {
	
	public static ArrayList<String> instrumentNames = new ArrayList<>();
	public static ArrayList<String> noteNames       = new ArrayList<>();
	public static ArrayList<Byte>   noteOctaves     = new ArrayList<>();
	
	private int                 currentSliceNumber = 0;
	private Instrument          currentInstrument  = null;
	private TreeSet<Instrument> usedInSlice        = null;
	private String              currentKeySig      = null;
	
	private static Pattern tripletPattern = Pattern.compile("(\\d+)$");
	
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
		currentSliceNumber = 0;
		currentKeySig      = "0/0";
		initInstrumentNames();
		initNoteNames();
	}
	
	/**
	 * Creates the MidicaPL string to be written into the export file.
	 * 
	 * @return MidicaPL string to be written into the export file
	 */
	public String createOutput() {
		StringBuilder output = new StringBuilder();
		
		// SLICE:
		for (Slice slice : slices) {
			
			usedInSlice = new TreeSet<>();
			
			// if necessary: add rest from current tick to the slice's begin tick
			output.append( createRestBeforeSlice(slice) );
			if (currentSliceNumber > 0) {
				output.append( createMarker() );
			}
			
			// global attributes
			output.append( createGlobalAttributes(slice) );
			
			// channel commands and instrument changes
			for (byte channel = 0; channel < 16; channel++) {
				output.append( createCommandsFromTimeline(slice, channel) );
			}
			
			currentSliceNumber++;
		}
		
		// config
		if (MUST_ADD_CONFIG) {
			output.append(NEW_LINE + NEW_LINE + createConfig());
		}
		
		// statistics
		if (MUST_ADD_STATISTICS) {
			output.append(createStatistics());
		}
		
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
		
		// add one empty line between channels
		if (! timeline.isEmpty()) {
			lines.append(NEW_LINE);
		}
		
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
						
						// statistics
						incrementStats(STAT_NOTE_SUMMANDS, channel);
						Matcher tripletMatcher = tripletPattern.matcher(summandStr);
						if (tripletMatcher.matches()) {
							String lengthStr = tripletMatcher.group(1);
							int summandLength = Integer.parseInt(lengthStr);
							if (0 == summandLength % 3) {
								incrementStats(STAT_NOTE_TRIPLETS, channel);
							}
						}
					}
					String lengthStr = String.join("~", summandStrings);
					
					// add note length / duration to timeline
					params.put( NP_LENGTH,   lengthStr    );
					params.put( NP_END_TICK, endTick + "" );
					params.put( NP_DURATION, durationPerc );
					incrementStats(STAT_NOTES, channel);
				}
				
				// write ALDA
				lines.append( createChordNotes(slice, channel, tick, events.get(ET_NOTES)) );
			}
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
		Byte[] instrConfig = instrumentHistory.get(channel).floorEntry(tick).getValue();
		if (instrConfig != null) {
			program = instrConfig[2];
		}
		
		// get instrument name and alias
		String instrName = 9 == channel ? "percussion" : instrumentNames.get(program);
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
		instrumentsByChannel.set(channel, currentInstrument);
		
		return content.toString();
	}
	
	/**
	 * Creates a string with global attributes for the given slice.
	 * 
	 * Global attributes are:
	 * 
	 * - tempo changes
	 * - key signature
	 * 
	 * @param slice  the sequence slice
	 * @return the created string (or an empty string, if the slice doesn't contain any global attributes)
	 */
	private String createGlobalAttributes(Slice slice) {
		StringBuilder result = new StringBuilder("");
		
		if (MUST_ADD_TICK_COMMENTS) {
			result.append(NEW_LINE);
			result.append("# SLICE " + currentSliceNumber + " (tick: " + slice.getBeginTick() + ")");
		}
		result.append(NEW_LINE + NEW_LINE);
		
		// create global commands
		TreeMap<String, String> globalCmds = slice.getGlobalCommands();
		if (0 == globalCmds.size()) {
			if (slice.getBeginTick() > 0) {
				// nothing more to do
			}
		}
		else {
			for (String cmdId : globalCmds.keySet()) {
				String value = globalCmds.get(cmdId);
				
				// get global command
				String globalCmd;
				if ("tempo".equals(cmdId)) {
					globalCmd = "tempo";
				}
				else if ("key".equals(cmdId)) {
					globalCmd     = "key-sig";
					currentKeySig = value;
					value         = getKeySignature();
					initNoteNames();
				}
				else {
					continue;
				}
				
				// append command
				result.append("(" + globalCmd + "! " + value + ")");
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
	 * # If necessary, creates an instrument change
	 * # If necessary, jumps to the marker at the begin of the current slice
	 * # If necessary, adds a REST to reach the current tick.
	 * # Checks the note-END tick of the shortest note
	 * # Adds a (smaller) rest to the chord, if the lowest END tick is further than the next ON tick or the slice's end
	 * # Prints all notes and (if necessary) the rest
	 * 
	 * @param slice    the sequence slice
	 * @param channel  MIDI channel
	 * @param tick     MIDI tick
	 * @param events   All notes/chords with the same note-ON tick in the same channel (comes from the slice's timeline)
	 * @return the created note lines.
	 */
	private String createChordNotes(Slice slice, byte channel, long tick, TreeMap<String, TreeMap<Byte, String>> events) {
		StringBuilder content = new StringBuilder("");
		boolean needSpace = true;
		
		// first usage in the current slice?
		Instrument instr = instrumentsByChannel.get(channel);
		if (!usedInSlice.contains(instr)) {
			
			// switch instrument, if necessary
			if (currentInstrument != instr) {
				content.append( createInstrumentChange(channel, tick) );
				instr = currentInstrument;
				needSpace = false;
			}
			
			usedInSlice.add(instr);
		}
		
		// jump to marker, if necessary
		Slice currentSlice   = slices.get(currentSliceNumber);
		long  sliceBeginTick = currentSlice.getBeginTick();
		if (instr.getCurrentTicks() < sliceBeginTick) {
			if (needSpace) {
				content.append(" ");
				needSpace = false;
			}
			content.append("@slice-" + currentSliceNumber);
			needSpace = true;
			instr.setCurrentTicks(sliceBeginTick);
		}
		
		// add rest, if necessary
		long currentTicks = instr.getCurrentTicks();
		if (tick > currentTicks) {
			long restTicks = tick - currentTicks;
			content.append( createRest(channel, restTicks, tick, null) );
			needSpace = true;
			instr.setCurrentTicks(tick);
		}
		
		// add space, if needed
		if (needSpace) {
			content.append(" ");
		}
		
		// get the note length END tick of the shortest note
		Long chordEndTick = null;
		for (Entry<String, TreeMap<Byte, String>> noteSet : events.entrySet()) {
			TreeMap<Byte, String> note = noteSet.getValue();
			long endTick = Long.parseLong(note.get(NP_END_TICK));
			if (null == chordEndTick || endTick < chordEndTick) {
				chordEndTick = endTick;
			}
		}
		
		// need a rest inside the cord?
		Long restEndTick  = null;
		Long nextOnTick   = noteHistory.get(channel).ceilingKey(tick + 1);
		long sliceEndTick = slice.getEndTick();
		if (nextOnTick != null && chordEndTick > nextOnTick) {
			restEndTick = nextOnTick;
		}
		if (chordEndTick > sliceEndTick) {
			if (restEndTick != null && restEndTick > sliceEndTick) {
				restEndTick = sliceEndTick;
			}
		}
		
		// collect the notes
		ArrayList<String> notes = new ArrayList<>();
		for (Entry<String, TreeMap<Byte, String>> noteSet : events.entrySet()) {
			String name = noteSet.getKey();
			TreeMap<Byte, String> properties = events.get(name);
			notes.add( createNote(channel, properties) );
		}
		content.append(String.join("/", notes));
		
		// create rest inside the chord
		if (restEndTick != null) {
			String rest = createRest(channel, restEndTick - tick, tick, null)
				.replaceFirst(" ", ""); // don't need the leading space here
			
			// rest created? - doesn't work if we don't have enough rest ticks
			if (! "".equals(rest)) {
				content.append("/" + rest);
			}
			chordEndTick = restEndTick;
		}
		
		// increment ticks, if necessary
		instr.setCurrentTicks(chordEndTick);
		
		return content.toString();
	}
	
	/**
	 * Prints a single note.
	 * 
	 * The note may be a part of a chord or a single note.
	 * 
	 * Parts of the note are:
	 * 
	 * - attributes, if needed (quant, vol)
	 * - octave switch, if needed
	 * - name
	 * - length, if needed
	 * 
	 * @param channel       MIDI channel
	 * @param properties    note properties (from the slice's timeline)
	 * @return the created note.
	 */
	private String createNote(byte channel, TreeMap<Byte, String> properties) {
		StringBuilder content = new StringBuilder("");
		
		Instrument instr = instrumentsByChannel.get(channel);
		
		// TODO: add the following attributes:
		// pan       == panning
		// track-vol == track-volume
		
		// get attributes to be changed
		ArrayList<String> attributes = new ArrayList<>();
		{
			// quantization
			float duration           = Float.parseFloat(properties.get(NP_DURATION)) / 100;
			float oldDuration        = instr.getDurationRatio();
			int   durationPercent    = (int) ((duration    * 1000 + 0.5f) / 10);
			int   oldDurationPercent = (int) ((oldDuration * 1000 + 0.5f) / 10);
			if (durationPercent != oldDurationPercent) {
				// don't allow 0%
				String durationPercentStr = durationPercent + "";
				if (durationPercent < 1) {
					durationPercentStr = "1";
					duration = 0.01f;
				}
				attributes.add("(quant " + durationPercentStr + ")");
				instr.setDurationRatio(duration);
				incrementStats(STAT_NOTE_DURATIONS, channel);
			}
			
			// velocity
			int velocity    = Integer.parseInt(properties.get(NP_VELOCITY));
			int oldVelocity = instr.getVelocity();
			if (velocity != oldVelocity) {
				instr.setVelocity(velocity);
				velocity = (velocity * 1000 + 5) / 1270;
				attributes.add("(vol " + velocity + ")");
				incrementStats(STAT_NOTE_VELOCITIES, channel);
			}
		}
		
		// add attributes
		if (attributes.size() > 0) {
			String attributesStr = String.join(" ", attributes);
			content.append(attributesStr + " ");
		}
		
		// switch octave, if needed
		int  noteNum   = Integer.parseInt(properties.get(NP_NOTE_NUM));
		byte oldOctave = instr.getOctave();
		byte newOctave = noteOctaves.get(noteNum);
		if (instr.getOctave() != newOctave) {
			String changer = newOctave < oldOctave ? "<" : ">";
			int    diff    = Math.abs(newOctave - oldOctave);
			for (int i = 0; i < diff; i++) {
				content.append(changer);
			}
			instr.setOctave(newOctave);
		}
		
		// note name
		String noteName = noteNames.get(noteNum);
		content.append(noteName);
		
		// switch note length, if needed
		String oldLength = instr.getNoteLength();
		String newLength = properties.get(NP_LENGTH);
		if (! oldLength.equals(newLength)) {
			content.append(newLength);
			instr.setNoteLength(newLength);
		}
		
		return content.toString();
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
			content.append( createInstrumentChange(channel, beginTick + ticks - 1) );
		else
			content.append(" ");
		
		// split length into elements
		ArrayList<Long> lengthElements = getLengthsForSum(ticks, true);
		
		// transform to strings
		ArrayList<String> lengthSummands = new ArrayList<>();
		for (Long length : lengthElements) {
			String summandStr = restLength.get(length);
			lengthSummands.add(summandStr);
			
			// statistics
			incrementStats(STAT_REST_SUMMANDS, channel);
			Matcher tripletMatcher = tripletPattern.matcher(summandStr);
			if (tripletMatcher.matches()) {
				String lengthStr = tripletMatcher.group(1);
				int summandLength = Integer.parseInt(lengthStr);
				if (0 == summandLength % 3) {
					incrementStats(STAT_REST_TRIPLETS, channel);
				}
			}
		}
		
		// add rest
		if (lengthSummands.size() > 0) {
			String length = String.join("~", lengthSummands);
			content.append("r" + length);
			currentInstrument.setNoteLength(length);
			incrementStats(STAT_RESTS, channel);
		}
		else {
			// TODO: Dict
			// TODO: add warning
			System.err.println("rest too small to be handled: " + ticks + " ticks");
			incrementStats(STAT_REST_SKIPPED, channel);
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
		
		// switch instrument if necessary
		long maxTick       = Instrument.getMaxCurrentTicks(instrumentsByChannel);
		long curInstrTicks = currentInstrument.getCurrentTicks();
		if (maxTick > curInstrTicks) {
			Instrument furthestInstr = getFurthestInstrument();
			byte channel = (byte) furthestInstr.channel;
			content.append( createInstrumentChange(channel, maxTick - 1) );
		}
		else {
			content.append(" ");
		}
		
		// create the marker
		content.append("%slice-" + currentSliceNumber + NEW_LINE);
		return content.toString();
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
		
		TreeMap<Long, String> lengthToSymbol = new TreeMap<>();
		
		// use very small lengths only for rests
		if (rest) {
			// 1/512
			long length512 = calculateTicks(1, 128);
			lengthToSymbol.put(length512, 512 + "");
			
			// 1/256
			long length256 = calculateTicks(1, 64);
			lengthToSymbol.put(length256, 256 + "");
			
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
		long base      = 32;
		if (useTriplets) lengthToSymbol.put( length32t, getTriplet(base) ); // triplet
		                 lengthToSymbol.put( length32,  base + ""        ); // normal
		if (useDots)     lengthToSymbol.put( length32d, base + "."       ); // dotted
		
		// 16th
		base = 16;
		long length16t = calculateTicks( 2, 4 * 3 );
		long length16  = calculateTicks( 1, 4     );
		long length16d = calculateTicks( 3, 4 * 2 );
		if (useTriplets) lengthToSymbol.put( length16t, getTriplet(base) );
		                 lengthToSymbol.put( length16,  base + ""        );
		if (useDots)     lengthToSymbol.put( length16d, base + "."       );
		
		// 8th
		base = 8;
		long length8t = calculateTicks( 2, 2 * 3 );
		long length8  = calculateTicks( 1, 2     );
		long length8d = calculateTicks( 3, 2 * 2 );
		if (useTriplets) lengthToSymbol.put( length8t, getTriplet(base) );
		                 lengthToSymbol.put( length8,  base + ""        );
		if (useDots)     lengthToSymbol.put( length8d, base + "."       );
		
		// quarter
		base = 4;
		long length4t = calculateTicks( 2, 3 );
		long length4  = calculateTicks( 1, 1 );
		long length4d = calculateTicks( 3, 2 );
		if (useTriplets) lengthToSymbol.put( length4t, getTriplet(base) );
		                 lengthToSymbol.put( length4,  base + ""        );
		if (useDots)     lengthToSymbol.put( length4d, base + "."       );
		
		// half
		base = 2;
		long length2t = calculateTicks( 2 * 2, 3 );
		long length2  = calculateTicks( 2,     1 );
		long length2d = calculateTicks( 2 * 3, 2 );
		if (useTriplets) lengthToSymbol.put( length2t, getTriplet(base) );
		                 lengthToSymbol.put( length2,  base + ""        );
		if (useDots)     lengthToSymbol.put( length2d, base + "."       );
		
		// full
		long length1  = calculateTicks( 4,     1 );
		long length1d = calculateTicks( 4 * 3, 2 );
		             lengthToSymbol.put( length1,  "1"  );
		if (useDots) lengthToSymbol.put( length1d, "1." );
		
		return lengthToSymbol;
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
	 * Initializes all possible MIDI instrument numbers with an ALDA instrument name.
	 */
	private void initInstrumentNames() {
		instrumentNames.clear();
		
		instrumentNames.add("piano");                           //   0
		instrumentNames.add("midi-bright-acoustic-piano");      //   1
		instrumentNames.add("midi-electric-grand-piano");       //   2
		instrumentNames.add("midi-honky-tonk-piano");           //   3
		instrumentNames.add("midi-electric-piano-1");           //   4
		instrumentNames.add("midi-electric-piano-2");           //   5
		instrumentNames.add("harpsichord");                     //   6
		instrumentNames.add("clavinet");                        //   7
		instrumentNames.add("celesta");                         //   8
		instrumentNames.add("glockenspiel");                    //   9
		instrumentNames.add("music-box");                       //  10
		instrumentNames.add("vibraphone");                      //  11
		instrumentNames.add("marimba");                         //  12
		instrumentNames.add("xylophone");                       //  13
		instrumentNames.add("tubular-bells");                   //  14
		instrumentNames.add("dulcimer");                        //  15
		instrumentNames.add("midi-drawbar-organ");              //  16
		instrumentNames.add("midi-percussive-organ");           //  17
		instrumentNames.add("midi-rock-organ");                 //  18
		instrumentNames.add("organ");                           //  19
		instrumentNames.add("midi-reed-organ");                 //  20
		instrumentNames.add("accordion");                       //  21
		instrumentNames.add("harmonica");                       //  22
		instrumentNames.add("midi-tango-accordion");            //  23
		instrumentNames.add("guitar");                          //  24
		instrumentNames.add("midi-acoustic-guitar-steel");      //  25
		instrumentNames.add("midi-electric-guitar-jazz");       //  26
		instrumentNames.add("electric-guitar-clean");           //  27
		instrumentNames.add("midi-electric-guitar-palm-muted"); //  28
		instrumentNames.add("electric-guitar-overdrive");       //  29
		instrumentNames.add("electric-guitar-distorted");       //  30
		instrumentNames.add("electric-guitar-harmonics");       //  31
		instrumentNames.add("acoustic-bass");                   //  32
		instrumentNames.add("electric-bass");                   //  33
		instrumentNames.add("electric-bass-pick");              //  34
		instrumentNames.add("fretless-bass");                   //  35
		instrumentNames.add("midi-bass-slap");                  //  36
		instrumentNames.add("midi-bass-pop");                   //  37
		instrumentNames.add("midi-synth-bass-1");               //  38
		instrumentNames.add("midi-synth-bass-2");               //  39
		instrumentNames.add("violin");                          //  40
		instrumentNames.add("viola");                           //  41
		instrumentNames.add("cello");                           //  42
		instrumentNames.add("contrabass");                      //  43
		instrumentNames.add("midi-tremolo-strings");            //  44
		instrumentNames.add("midi-pizzicato-strings");          //  45
		instrumentNames.add("harp");                            //  46
		instrumentNames.add("timpani");                         //  47
		instrumentNames.add("midi-string-ensemble-1");          //  48
		instrumentNames.add("midi-string-ensemble-2");          //  49
		instrumentNames.add("midi-synth-strings-1");            //  50
		instrumentNames.add("midi-synth-strings-2");            //  51
		instrumentNames.add("midi-choir-aahs");                 //  52
		instrumentNames.add("midi-voice-oohs");                 //  53
		instrumentNames.add("midi-synth-voice");                //  54
		instrumentNames.add("midi-orchestra-hit");              //  55
		instrumentNames.add("trumpet");                         //  56
		instrumentNames.add("trombone");                        //  57
		instrumentNames.add("tuba");                            //  58
		instrumentNames.add("midi-muted-trumpet");              //  59
		instrumentNames.add("french-horn");                     //  60
		instrumentNames.add("midi-brass-section");              //  61
		instrumentNames.add("midi-synth-brass-1");              //  62
		instrumentNames.add("midi-synth-brass-2");              //  63
		instrumentNames.add("soprano-sax");                     //  64
		instrumentNames.add("alto-sax");                        //  65
		instrumentNames.add("tenor-sax");                       //  66
		instrumentNames.add("bari-sax");                        //  67
		instrumentNames.add("oboe");                            //  68
		instrumentNames.add("english-horn");                    //  69
		instrumentNames.add("bassoon");                         //  70
		instrumentNames.add("clarinet");                        //  71
		instrumentNames.add("piccolo");                         //  72
		instrumentNames.add("flute");                           //  73
		instrumentNames.add("recorder");                        //  74
		instrumentNames.add("pan-flute");                       //  75
		instrumentNames.add("bottle");                          //  76
		instrumentNames.add("shakuhachi");                      //  77
		instrumentNames.add("whistle");                         //  78
		instrumentNames.add("ocarina");                         //  79
		instrumentNames.add("square");                          //  80
		instrumentNames.add("sawtooth");                        //  81
		instrumentNames.add("calliope");                        //  82
		instrumentNames.add("chiff");                           //  83
		instrumentNames.add("charang");                         //  84
		instrumentNames.add("midi-solo-vox");                   //  85
		instrumentNames.add("midi-fifths");                     //  86
		instrumentNames.add("midi-bass-and-lead");              //  87
		instrumentNames.add("midi-pad-new-age");                //  88
		instrumentNames.add("midi-pad-warm");                   //  89
		instrumentNames.add("midi-pad-polysynth");              //  90
		instrumentNames.add("midi-pad-choir");                  //  91
		instrumentNames.add("midi-pad-bowed");                  //  92
		instrumentNames.add("midi-pad-metallic");               //  93
		instrumentNames.add("midi-pad-halo");                   //  94
		instrumentNames.add("midi-pad-sweep");                  //  95
		instrumentNames.add("midi-fx-ice-rain");                //  96
		instrumentNames.add("midi-soundtrack");                 //  97
		instrumentNames.add("midi-crystal");                    //  98
		instrumentNames.add("midi-atmosphere");                 //  99
		instrumentNames.add("midi-brightness");                 // 100
		instrumentNames.add("midi-goblins");                    // 101
		instrumentNames.add("midi-echoes");                     // 102
		instrumentNames.add("midi-sci-fi");                     // 103
		instrumentNames.add("sitar");                           // 104
		instrumentNames.add("banjo");                           // 105
		instrumentNames.add("shamisen");                        // 106
		instrumentNames.add("koto");                            // 107
		instrumentNames.add("kalimba");                         // 108
		instrumentNames.add("bagpipes");                        // 109
		instrumentNames.add("midi-fiddle");                     // 110
		instrumentNames.add("shanai");                          // 111
		instrumentNames.add("midi-tinkle-bell");                // 112
		instrumentNames.add("midi-agogo");                      // 113
		instrumentNames.add("steel-drums");                     // 114
		instrumentNames.add("midi-woodblock");                  // 115
		instrumentNames.add("midi-taiko-drum");                 // 116
		instrumentNames.add("midi-melodic-tom");                // 117
		instrumentNames.add("midi-synth-drum");                 // 118
		instrumentNames.add("midi-reverse-cymbal");             // 119
		instrumentNames.add("midi-guitar-fret-noise");          // 120
		instrumentNames.add("midi-breath-noise");               // 121
		instrumentNames.add("midi-seashore");                   // 122
		instrumentNames.add("midi-bird-tweet");                 // 123
		instrumentNames.add("midi-telephone-ring");             // 124
		instrumentNames.add("midi-helicopter");                 // 125
		instrumentNames.add("midi-applause");                   // 126
		instrumentNames.add("midi-gunshot");                    // 127
	}
	
	/**
	 * Initializes all possible MIDI note numbers with an ALDA instrument and octave name.
	 * Takes the current key signature into account.
	 */
	private void initNoteNames() {
		noteNames.clear();
		noteOctaves.clear();
		
		// get sharps/flats
		String[] shaFlaTon     = currentKeySig.split("/", 2);
		int      sharpsOrFlats = Integer.parseInt(shaFlaTon[0]);
		
		// create base names (without octaves)
		// begin with the default: no sharps or flats (C maj / A min)
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
		
		// sharps
		if (sharpsOrFlats > 0) {
			
			// G maj / E min
			baseNotes.set(5, "f_");
			baseNotes.set(6, "f");
			if (sharpsOrFlats > 1) { // D maj / B min
				baseNotes.set(0, "c_");
				baseNotes.set(1, "c");
			}
			if (sharpsOrFlats > 2) { // A maj / F# min
				baseNotes.set(7, "g_");
				baseNotes.set(8, "g");
			}
			if (sharpsOrFlats > 3) { // E maj / C# min
				baseNotes.set(2, "d_");
				baseNotes.set(3, "d");
			}
			if (sharpsOrFlats > 4) { // B maj / G# min
				baseNotes.set(9, "a_");
				baseNotes.set(10, "a");
			}
			if (sharpsOrFlats > 5) { // F# maj / D# min
				baseNotes.set(4, "e_");
				baseNotes.set(5, "e");
			}
			if (sharpsOrFlats > 6) { // C# maj / A# min
				baseNotes.set(11, "b_");
			}
		}
		
		// flats
		else if (sharpsOrFlats < 0) {
			// F maj / D min
			baseNotes.set(10, "b");
			baseNotes.set(11, "b_");
			if (sharpsOrFlats < -1) { // Bb maj / G min
				baseNotes.set(3, "e");
				baseNotes.set(4, "e_");
			}
			if (sharpsOrFlats < -2) { // Eb maj / C min
				baseNotes.set(8, "a");
				baseNotes.set(9, "a_");
			}
			if (sharpsOrFlats < -3) { // Ab maj / F min
				baseNotes.set(1, "d");
				baseNotes.set(2, "d_");
			}
			if (sharpsOrFlats < -4) { // Db maj / Bb min
				baseNotes.set(6, "g");
				baseNotes.set(7, "g_");
			}
			if (sharpsOrFlats < -5) { // Gb maj / Eb min
				baseNotes.set(0, "c_");
			}
			if (sharpsOrFlats < -6) { // Cb maj / Ab min
				baseNotes.set(4, "f");
				baseNotes.set(5, "f_");
			}
		}
		
		// build up the resulting structures: add octaves
		byte num = 0;
		byte oct = -1;
		NOTES:
		while (true) {
			for (String baseName : baseNotes) {
				
				noteNames.add(baseName);
				noteOctaves.add(oct);
				
				if (127 == num)
					break NOTES;
				num++;
			}
			oct++;
		}
	}
	
	/**
	 * Calculates and returns the value for the key signature attribute.
	 * 
	 * @return the key signature
	 */
	private String getKeySignature() {
		
		String[] parts = currentKeySig.split("/", 2);
		
		int sharpsOrFlats = Integer.parseInt(parts[0]);
		int tonality      = Integer.parseInt(parts[1]);
		
		// tonality
		boolean isMajor     = 0 == tonality;
		boolean isMinor     = 1 == tonality;
		String  noteStr     = null;
		String  tonalityStr = null;
		
		if (isMajor) {
			tonalityStr = "major";
			if      (0  == sharpsOrFlats) noteStr = "c";        // C maj
			else if (1  == sharpsOrFlats) noteStr = "g";        // G maj
			else if (2  == sharpsOrFlats) noteStr = "d";        // D maj
			else if (3  == sharpsOrFlats) noteStr = "a";        // A maj
			else if (4  == sharpsOrFlats) noteStr = "e";        // E maj
			else if (5  == sharpsOrFlats) noteStr = "b";        // B maj
			else if (6  == sharpsOrFlats) noteStr = "f :sharp"; // F# maj
			else if (7  == sharpsOrFlats) noteStr = "c :sharp"; // C# maj
			else if (-1 == sharpsOrFlats) noteStr = "f";        // F maj
			else if (-2 == sharpsOrFlats) noteStr = "b :flat";  // Bb maj
			else if (-3 == sharpsOrFlats) noteStr = "e :flat";  // Eb maj
			else if (-4 == sharpsOrFlats) noteStr = "a :flat";  // Ab maj
			else if (-5 == sharpsOrFlats) noteStr = "d :flat";  // Db maj
			else if (-6 == sharpsOrFlats) noteStr = "g :flat";  // Gb maj
			else if (-7 == sharpsOrFlats) noteStr = "c :flat";  // Cb maj
		}
		else if (isMinor) {
			tonalityStr = "minor";
			if      (0  == sharpsOrFlats) noteStr = "a";        // A min
			else if (1  == sharpsOrFlats) noteStr = "e";        // E min
			else if (2  == sharpsOrFlats) noteStr = "b";        // B min
			else if (3  == sharpsOrFlats) noteStr = "f :sharp"; // F# min
			else if (4  == sharpsOrFlats) noteStr = "c :sharp"; // C# min
			else if (5  == sharpsOrFlats) noteStr = "g :sharp"; // G# min
			else if (6  == sharpsOrFlats) noteStr = "d :sharp"; // D# min
			else if (7  == sharpsOrFlats) noteStr = "a :sharp"; // A# min
			else if (-1 == sharpsOrFlats) noteStr = "d";        // D min
			else if (-2 == sharpsOrFlats) noteStr = "g";        // G min
			else if (-3 == sharpsOrFlats) noteStr = "c";        // C min
			else if (-4 == sharpsOrFlats) noteStr = "f";        // F min
			else if (-5 == sharpsOrFlats) noteStr = "b :flat";  // Bb min
			else if (-6 == sharpsOrFlats) noteStr = "e :flat";  // Eb min
			else if (-7 == sharpsOrFlats) noteStr = "a :flat";  // Ab min
		}
		
		// invalid?
		if (null == noteStr || null == tonalityStr)
			return null;
		
		return "[:" + noteStr + " :" + tonalityStr + "]";
	}
}
