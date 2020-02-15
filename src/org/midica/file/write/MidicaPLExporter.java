/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file.write;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Track;

import org.midica.config.Config;
import org.midica.config.Dict;
import org.midica.file.Instrument;
import org.midica.file.read.MidicaPLParser;
import org.midica.midi.KaraokeAnalyzer;
import org.midica.midi.MessageClassifier;
import org.midica.midi.MidiDevices;
import org.midica.midi.MidiListener;
import org.midica.midi.SequenceAnalyzer;
import org.midica.midi.SequenceCreator;
import org.midica.midi.Tempo;
import org.midica.ui.file.DecompileConfigController;
import org.midica.ui.file.ExportResult;
import org.midica.ui.model.ComboboxStringOption;
import org.midica.ui.model.ConfigComboboxModel;

/**
 * This class is used to export the currently loaded MIDI sequence as a MidicaPL source file.
 * 
 * @author Jan Trukenmüller
 */
public class MidicaPLExporter extends Exporter {
	
	/* *****************
	 * constants
	 *******************/
	
	// event types
	public static final byte ET_INSTR = 1; // instrument change
	public static final byte ET_NOTES = 2; // notes or chords (or an inline rest with syllable)
	
	// note properties
	public static final byte NP_VELOCITY = 1; // velocity option
	public static final byte NP_OFF_TICK = 2; // note-off tick
	public static final byte NP_END_TICK = 3; // tick at end of note
	public static final byte NP_LENGTH   = 4; // length column as MidicaPL length
	public static final byte NP_DURATION = 5; // duration option as float number
	public static final byte NP_MULTIPLE = 6; // multiple option (value ignored)
	public static final byte NP_LYRICS   = 7; // lyrics option
	
	// constants for statistics about the decompilation quality
	private static final byte STAT_TOTAL           = 17;
	private static final byte STAT_RESTS           = 21;
	private static final byte STAT_REST_SKIPPED    = 22;
	private static final byte STAT_REST_TRIPLETS   = 23;
	private static final byte STAT_REST_SUMMANDS   = 24;
	private static final byte STAT_NOTES           = 31;
	private static final byte STAT_NOTE_VELOCITIES = 32;
	private static final byte STAT_NOTE_DURATIONS  = 33;
	private static final byte STAT_NOTE_TRIPLETS   = 34;
	private static final byte STAT_NOTE_SUMMANDS   = 35;
	private static final byte STAT_NOTE_MULTIPLE   = 36;
	
	private static final String NEW_LINE = System.getProperty("line.separator");
	
	// decompile constants
	public static final byte INLINE                       = 1;
	public static final byte BLOCK                        = 2;
	public static final byte STRATEGY_NEXT_DURATION_PRESS = 1;
	public static final byte STRATEGY_DURATION_NEXT_PRESS = 2;
	public static final byte STRATEGY_NEXT_PRESS          = 3;
	public static final byte STRATEGY_DURATION_PRESS      = 4;
	public static final byte STRATEGY_PRESS               = 5;
	
	// decompile configuration defaults
	public static final boolean DEFAULT_MUST_ADD_TICK_COMMENTS   = true;
	public static final boolean DEFAULT_MUST_ADD_CONFIG          = true;
	public static final boolean DEFAULT_MUST_ADD_QUALITY_SCORE   = true;
	public static final boolean DEFAULT_MUST_ADD_STATISTICS      = true;
	public static final byte    DEFAULT_LENGTH_STRATEGY          = STRATEGY_NEXT_DURATION_PRESS;
	public static final long    DEFAULT_DURATION_TICK_TOLERANCE  = 2;
	public static final float   DEFAULT_DURATION_RATIO_TOLERANCE = 0.014f;
	public static final float   DEFAULT_MIN_DURATION_TO_KEEP     = 0.05f;
	public static final long    DEFAULT_NEXT_NOTE_ON_TOLERANCE   = 3;
	public static final long    DEFAULT_MAX_TARGET_TICKS_ON      = 3840; // 2 full notes
	public static final boolean DEFAULT_USE_PRE_DEFINED_CHORDS   = true;
	public static final long    DEFAULT_CHORD_NOTE_ON_TOLERANCE  = 0;
	public static final long    DEFAULT_CHORD_NOTE_OFF_TOLERANCE = 0;
	public static final long    DEFAULT_CHORD_VELOCITY_TOLERANCE = 0;
	public static final byte    DEFAULT_ORPHANED_SYLLABLES       = INLINE;
	public static final boolean DEFAULT_KARAOKE_ONE_CHANNEL      = false;
	public static final String  DEFAULT_EXTRA_GLOBALS_STR        = "";
	
	// decompile configuration
	private static boolean       MUST_ADD_TICK_COMMENTS   = DEFAULT_MUST_ADD_TICK_COMMENTS;
	private static boolean       MUST_ADD_CONFIG          = DEFAULT_MUST_ADD_CONFIG;
	private static boolean       MUST_ADD_QUALITY_SCORE   = DEFAULT_MUST_ADD_QUALITY_SCORE;
	private static boolean       MUST_ADD_STATISTICS      = DEFAULT_MUST_ADD_STATISTICS;
	private static byte          LENGTH_STRATEGY          = DEFAULT_LENGTH_STRATEGY;
	private static long          DURATION_TICK_TOLERANCE  = DEFAULT_DURATION_TICK_TOLERANCE;
	private static float         DURATION_RATIO_TOLERANCE = DEFAULT_DURATION_RATIO_TOLERANCE;
	private static float         MIN_DURATION_TO_KEEP     = DEFAULT_MIN_DURATION_TO_KEEP;
	private static long          NEXT_NOTE_ON_TOLERANCE   = DEFAULT_NEXT_NOTE_ON_TOLERANCE;
	private static long          MAX_TARGET_TICKS_ON      = DEFAULT_MAX_TARGET_TICKS_ON;
	private static long          MAX_SOURCE_TICKS_ON      = 0L;
	private static boolean       USE_PRE_DEFINED_CHORDS   = DEFAULT_USE_PRE_DEFINED_CHORDS;
	private static long          CHORD_NOTE_ON_TOLERANCE  = DEFAULT_CHORD_NOTE_ON_TOLERANCE;
	private static long          CHORD_NOTE_OFF_TOLERANCE = DEFAULT_CHORD_NOTE_OFF_TOLERANCE;
	private static long          CHORD_VELOCITY_TOLERANCE = DEFAULT_CHORD_VELOCITY_TOLERANCE;
	private static byte          ORPHANED_SYLLABLES       = DEFAULT_ORPHANED_SYLLABLES;
	private static boolean       KARAOKE_ONE_CHANNEL      = DEFAULT_KARAOKE_ONE_CHANNEL;
	private static TreeSet<Long> EXTRA_GLOBALS            = null;
	
	/* *****************
	 * class fields
	 *******************/
	
	private static int          sourceResolution = 0;
	private static int          targetResolution = SequenceCreator.DEFAULT_RESOLUTION;
	private static ExportResult exportResult     = null;
	private static boolean      isSoftKaraoke    = false;
	
	/** stores the current state of each channel */
	private static ArrayList<Instrument> instruments = null;
	
	/**
	 * Stores each **slice** of the sequence.
	 * 
	 * A slice begins either with (one or more) global commands or at tick 0.
	 * 
	 * It ends either one tick before a global command or at the end of the sequence.
	 * 
	 * index -- slice
	 */
	private static ArrayList<Slice> slices = null;
	
	private static TreeMap<Long, String> noteLength = null;
	private static TreeMap<Long, String> restLength = null;
	
	/** comma-separated note bytes  --  chord name */
	private static TreeMap<String, String> chords = null;
	
	/** lowest note  --  chord count */
	private static TreeMap<String, Integer> chordCount = null;
	
	/** lowest note  --  comma-separated note bytes (This structure is only needed for the sorting: lowest note first, then chord name) */
	private static TreeMap<String, ArrayList<String>> chordsByBaseNote = null;
	
	/* ******************
	 * instance fields
	 ********************/
	
	/** channels that can be used for lyrics, sorted by priority */
	private ArrayList<Byte> lyricsChannels = null;
	
	/** Priority which channel to choose for a lyrics events, if we have different possibilities. */
	private ArrayList<Byte> karaokePriority = null;
	
	// structures built by SequenceAnalyzer and KaraokeAnalyzer
	private TreeMap<Byte, TreeMap<Long, Byte[]>>                 instrumentHistory = null;
	private TreeMap<Byte, TreeMap<Long, String>>                 commentHistory    = null;
	private TreeMap<Byte, TreeMap<Long, TreeMap<Byte, Byte>>>    noteHistory       = null;
	private TreeMap<Byte, TreeMap<Byte, TreeMap<Long, Boolean>>> noteOnOff         = null;
	private TreeMap<Long, String>                                lyricsSyllables   = null;
	
	/** stores statistics to estimate the decompilation quality */
	private TreeMap<Byte, TreeMap<Byte, Integer>> statistics = null;
	
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
	public ExportResult export(File file) throws ExportException {
		
		exportResult         = new ExportResult(true);
		String targetCharset = ((ComboboxStringOption) ConfigComboboxModel.getModel(Config.CHARSET_EXPORT_MPL).getSelectedItem()).getIdentifier();
		
		try {
			
			// create file writer and store it in this.writer
			if ( ! createFile(file) )
				return new ExportResult(false);
			
			// open file for writing
			FileOutputStream   fos    = new FileOutputStream(file);
			OutputStreamWriter osw    = new OutputStreamWriter(fos, targetCharset);
			BufferedWriter     writer = new BufferedWriter(osw);
			
			// get pre-parsed data structures
			HashMap<String, Object> histories = SequenceAnalyzer.getHistories();
			instrumentHistory = (TreeMap<Byte, TreeMap<Long, Byte[]>>)                 histories.get( "instrument_history" );
			commentHistory    = (TreeMap<Byte, TreeMap<Long, String>>)                 histories.get( "comment_history" );
			noteHistory       = (TreeMap<Byte, TreeMap<Long, TreeMap<Byte, Byte>>>)    histories.get( "note_history" );
			noteOnOff         = (TreeMap<Byte, TreeMap<Byte, TreeMap<Long, Boolean>>>) histories.get( "note_on_off" );
			lyricsSyllables   = KaraokeAnalyzer.getLyricsFlat();
			
			// init data structures
			chords           = new TreeMap<>();
			chordCount       = new TreeMap<>();
			chordsByBaseNote = new TreeMap<>();
			
			// get resolution
			sourceResolution = MidiDevices.getSequence().getResolution();
			
			// refresh decompile config
			refreshConfig();
			
			// initialize statistics
			initStatistics();
			
			// initialize instruments (to track the channel configuration)
			initInstruments();
			
			// Prioritize channels to be used for karaoke.
			lyricsChannels = prioritizeChannelsForLyrics();
			
			// make sure that the syntax configuration is up to date
			MidicaPLParser.refreshSyntax();
			
			// fill the timeline with instrument changes and note events
			slices = new ArrayList<>();
			
			// detect global commands and split the sequence into slices accordingly
			splitSequence();
			
			// calculate what tick length corresponds to what note length
			noteLength = initNoteLengths();
			restLength = initRestLengths();
			
			// fill slices
			addInstrumentsToSlices();
			groupNotes();
			addNotesToSlices();
			addLyricsToSlices();
			
			// create MidicaPL string from the data structures and write it into the file
			writer.write( createMidicaPL() );
			writer.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		return exportResult;
	}
	
	/**
	 * Re-reads all config variables that are relevant for decompilation.
	 */
	private void refreshConfig() {
		
		// apply direct configuration
		HashMap<String, String> sessionConfig = DecompileConfigController.getSessionConfig();
		MUST_ADD_TICK_COMMENTS   = Boolean.parseBoolean( sessionConfig.get(Config.DC_MUST_ADD_TICK_COMMENTS)   );
		MUST_ADD_CONFIG          = Boolean.parseBoolean( sessionConfig.get(Config.DC_MUST_ADD_CONFIG)          );
		MUST_ADD_QUALITY_SCORE   = Boolean.parseBoolean( sessionConfig.get(Config.DC_MUST_ADD_QUALITY_SCORE)   );
		MUST_ADD_STATISTICS      = Boolean.parseBoolean( sessionConfig.get(Config.DC_MUST_ADD_STATISTICS)      );
		LENGTH_STRATEGY          = Byte.parseByte(       sessionConfig.get(Config.DC_LENGTH_STRATEGY)          );
		DURATION_TICK_TOLERANCE  = Long.parseLong(       sessionConfig.get(Config.DC_DURATION_TICK_TOLERANCE)  );
		DURATION_RATIO_TOLERANCE = Float.parseFloat(     sessionConfig.get(Config.DC_DURATION_RATIO_TOLERANCE) );
		MIN_DURATION_TO_KEEP     = Float.parseFloat(     sessionConfig.get(Config.DC_MIN_DURATION_TO_KEEP)     );
		NEXT_NOTE_ON_TOLERANCE   = Long.parseLong(       sessionConfig.get(Config.DC_NEXT_NOTE_ON_TOLERANCE)   );
		MAX_TARGET_TICKS_ON      = Long.parseLong(       sessionConfig.get(Config.DC_MAX_TARGET_TICKS_ON)      );
		USE_PRE_DEFINED_CHORDS   = Boolean.parseBoolean( sessionConfig.get(Config.DC_USE_PRE_DEFINED_CHORDS)   );
		CHORD_NOTE_ON_TOLERANCE  = Long.parseLong(       sessionConfig.get(Config.DC_CHORD_NOTE_ON_TOLERANCE)  );
		CHORD_NOTE_OFF_TOLERANCE = Long.parseLong(       sessionConfig.get(Config.DC_CHORD_NOTE_OFF_TOLERANCE) );
		CHORD_VELOCITY_TOLERANCE = Long.parseLong(       sessionConfig.get(Config.DC_CHORD_VELOCITY_TOLERANCE) );
		ORPHANED_SYLLABLES       = Byte.parseByte(       sessionConfig.get(Config.DC_ORPHANED_SYLLABLES)       );
		KARAOKE_ONE_CHANNEL      = Boolean.parseBoolean( sessionConfig.get(Config.DC_KARAOKE_ONE_CHANNEL)      );
		EXTRA_GLOBALS            = DecompileConfigController.getExtraGlobalTicks();
		
		// apply indirect configuration
		MAX_SOURCE_TICKS_ON = (MAX_TARGET_TICKS_ON * sourceResolution * 10 + 5) / (targetResolution * 10);
	}
	
	/**
	 * Initializes and resets the instruments structure so that the channel configurations can be tracked.
	 */
	private void initInstruments() {
		instruments = new ArrayList<>();
		// CHANNEL:
		for (byte channel = 0; channel < 16; channel++) {
			// regard only the lowest tick >= 0
			Entry<Long, Byte[]> entry = instrumentHistory.get(channel).ceilingEntry(0L);
			boolean isAutomatic = null == entry;
			isAutomatic         = isAutomatic || 0 == noteHistory.get(channel).size();
			int     instrNumber = 0;
			if (entry != null) {
				Byte[] channelConfig = entry.getValue();
				instrNumber = channelConfig[2];
			}
			Instrument instr = new Instrument(channel, instrNumber, null, isAutomatic);
			instruments.add(instr);
		}
	}
	
	/**
	 * Prioritizes the channels for karaoke usage.
	 * 
	 * Calculates the following values for each channel:
	 * 
	 * - **notes**: number of ticks with a note or chord
	 * - **matches**: number of ticks with a note or chords that can be used for a syllable
	 * - **relevance**: matches divided by notes
	 * - **coverage**: matches divided by the total number of syllables
	 * - **priority**: relevance plus coverage
	 * 
	 * Sorts the channels by priority and returns them.
	 * 
	 * Reasons for this approach:
	 * 
	 * Often we have the following facts:
	 * 
	 * - One channel is mainly used for the lead vocals.
	 * - Another channel is mainly used for background vocals, e.g. only for the refrain,
	 *   together with the lead vocals channel.
	 * - Another channel (e.g. the percussion channel) has also a lot of matches because
	 *   it contains a lot of notes.
	 * 
	 * In this case the lead vocal channel is the best choice.
	 * 
	 * But the background vocals could have a better relevance, and the percussion channel
	 * could have the best coverage.
	 * 
	 * However the lead vocal channel has probably the highest sum (relevance + coverage).
	 * That's why we choose this approach.
	 * 
	 * @return channels usable for lyrics, sorted by priority.
	 */
	private ArrayList<Byte> prioritizeChannelsForLyrics() {
		TreeMap<Byte, Integer> channelMatches = new TreeMap<>(); // number of events matching a lyrics tick
		TreeMap<Byte, Integer> channelNotes   = new TreeMap<>(); // total number of different ticks with Note-ON
		
		// collect all channels that can be used at all
		for (byte channel = 0; channel < 16; channel++) {
			
			// no notes?
			if (instruments.get(channel).autoChannel)
				continue;
			
			channelMatches.put(channel, 0);
			channelNotes.put(channel, noteHistory.get(channel).size());
		}
		
		// Fallback: no notes at all, but only lyrics.
		// In this case: use the percussion channel because this is the only channel that doesn't need to
		// appear in the INSTRUMENTS block.
		if (channelMatches.isEmpty()) {
			ArrayList<Byte> prioritizedChannels = new ArrayList<>();
			prioritizedChannels.add((byte) 9);
			
			return prioritizedChannels;
		}
		
		// count the lyrics with notes in the same ticks (for each channel)
		// TICK:
		for (long tick: lyricsSyllables.keySet()) {
			
			// CHANNEL:
			for (byte channel : channelMatches.keySet()) {
				
				// channel contains a note at this tick? - increment
				if (noteHistory.get(channel).containsKey(tick)) {
					int count = channelMatches.get(channel);
					channelMatches.put(channel, count + 1);
				}
			}
		}
		
		// calculate priority for each channel
		TreeMap<Byte, Float> channelPriorities = new TreeMap<>();
		int allSyllables = lyricsSyllables.size();
		for (byte channel : channelMatches.keySet()) {
			int matches = channelMatches.get(channel);
			int notes   = channelNotes.get(channel);
			
			// 1st priority part: relevance = matches / events
			float relevance = -1;
			if (matches > 0 && notes > 0) {
				relevance = ((float) matches) / ((float) notes);
			}
			
			// 2nd priority part: coverage = matches / total lyrics
			float coverage = -1;
			if (matches > 0 && allSyllables > 0) {
				coverage = ((float) matches) / ((float) allSyllables);
			}
			
			// put it together
			float priority = coverage + relevance;
			channelPriorities.put(channel, priority);
		}
		
		// collect and sort all priorities
		TreeSet<Float> priorities = new TreeSet<>();
		for (byte channel : channelPriorities.keySet()) {
			float priority = channelPriorities.get(channel);
			priorities.add(priority);
		}
		
		// prioritize by number of counts
		ArrayList<Byte> prioritizedChannels = new ArrayList<>();
		PRIORITY:
		for (float priority : priorities.descendingSet()) {
			for (byte channel : channelPriorities.keySet()) {
				float channelPriority = channelPriorities.get(channel);
				if (priority == channelPriority) {
					prioritizedChannels.add(channel);
					if (KARAOKE_ONE_CHANNEL)
						break PRIORITY;
				}
			}
		}
		
		return prioritizedChannels;
	}
	
	/**
	 * Initializes data structures for the statistics to estimate the decompilation quality.
	 */
	private void initStatistics() {
		statistics = new TreeMap<>();
		for (byte channel = 0; channel < 16; channel++) {
			TreeMap<Byte, Integer> channelStats = new TreeMap<>();
			statistics.put(channel, channelStats);
		}
		TreeMap<Byte, Integer> totalStats = new TreeMap<>();
		statistics.put(STAT_TOTAL, totalStats);
		
		// init sub statistics for all channels and total
		for (Byte channelOrTotal : statistics.keySet()) {
			TreeMap<Byte, Integer> channelStats = statistics.get(channelOrTotal);
			channelStats.put( STAT_RESTS,           0 );
			channelStats.put( STAT_REST_SKIPPED,    0 );
			channelStats.put( STAT_REST_TRIPLETS,   0 );
			channelStats.put( STAT_REST_SUMMANDS,   0 );
			channelStats.put( STAT_NOTES,           0 );
			channelStats.put( STAT_NOTE_VELOCITIES, 0 );
			channelStats.put( STAT_NOTE_DURATIONS,  0 );
			channelStats.put( STAT_NOTE_TRIPLETS,   0 );
			channelStats.put( STAT_NOTE_SUMMANDS,   0 );
			channelStats.put( STAT_NOTE_MULTIPLE,   0 );
		}
	}
	
	/**
	 * Increments the statistics for the channel and total.
	 * 
	 * @param type     statistics type
	 * @param channel  MIDI channel
	 */
	private void incrementStats(Byte type, Byte channel) {
		int channelValue = statistics.get(channel).get(type);
		int totalValue   = statistics.get(STAT_TOTAL).get(type);
		
		statistics.get(channel).put(type, channelValue + 1);
		statistics.get(STAT_TOTAL).put(type, totalValue + 1);
	}
	
	/**
	 * Splits the sequence into slices between global commands.
	 * Adds the global commands to the according slices.
	 * 
	 * The following messages are regarded as global commands:
	 * 
	 * - tempo changes
	 * - key signature
	 * - time signature
	 * 
	 * Creates warnings for ignored short messages.
	 */
	private void splitSequence() {
		
		// structure for all global commands ticks and their command strings
		TreeMap <Long, ArrayList<String[]>> allGlobals = new TreeMap<>();
		
		// make sure that the first slice always begins in tick 0
		EXTRA_GLOBALS.add(0L);
		
		// add extra global ticks from configuration
		Iterator<Long> iterator = EXTRA_GLOBALS.iterator();
		while (iterator.hasNext()) {
			long tick = iterator.next();
			ArrayList<String[]> commands = new ArrayList<String[]>();
			allGlobals.put(tick, commands);
		}
		
		// add global ticks from META events
		int trackNum = 0;
		TRACK:
		for (Track track : MidiDevices.getSequence().getTracks()) {
			
			EVENT:
			for (int i=0; i < track.size(); i++) {
				MidiEvent   event = track.get(i);
				long        tick  = event.getTick();
				MidiMessage msg   = event.getMessage();
				
				// channel name?
				if (msg instanceof MetaMessage) {
					// TODO: implement or delete
				}
				
				// TODO: implement or delete
				// short message
//				if (msg instanceof ShortMessage) {
//					ShortMessage shortMsg = (ShortMessage) msg;
//					int cmd      = shortMsg.getCommand();
//					int channel  = shortMsg.getChannel();
//					int note     = shortMsg.getData1();
//					int velocity = shortMsg.getData2();
//					
//					// ignore events that are handled otherwise
//					if ( ShortMessage.PROGRAM_CHANGE == cmd
//					  || ShortMessage.NOTE_ON        == cmd
//					  || ShortMessage.NOTE_OFF       == cmd ) {
//						// ignore
//					}
//					
//					// something else?
//					else {
//						String warning = String.format( Dict.get(Dict.WARNING_IGNORED_SHORT_MESSAGE), cmd, note, velocity );
//						exportResult.addWarning(trackNum, tick, channel, -1, warning);
//					}
//				}
				
				// meta message
				if (msg instanceof MetaMessage) {
					MetaMessage metaMsg = (MetaMessage) msg;
					int     type       = metaMsg.getType();
					int     status     = metaMsg.getStatus();
					byte[]  data       = metaMsg.getData();
					String  cmdId      = null;
					String  value      = null;
					if (MidiListener.META_SET_TEMPO == type) {
						int bpm = Tempo.getBpm(metaMsg);
						cmdId   = "tempo";
						value   = Integer.toString(bpm);
					}
					else if (MidiListener.META_KEY_SIGNATURE == type) {
						byte sharpsOrFlats       = data[0];
						byte tonality            = data[1];
						String[] noteAndTonality = MessageClassifier.getKeySignature(sharpsOrFlats, tonality);
						cmdId = "key";
						value = noteAndTonality[0] + MidicaPLParser.KEY_SEPARATOR + noteAndTonality[1];
					}
					else if (MidiListener.META_TIME_SIGNATURE == type) {
						int numerator   = data[0];
						int exp         = data[1];
						int denominator = (int) Math.pow(2, exp);
						cmdId           = "time";
						value           = numerator + MidicaPLParser.TIME_SIG_SLASH + denominator;
					}
					
					// global command found?
					if (cmdId != null) {
						ArrayList<String[]> commands = allGlobals.get(tick);
						if (null == commands) {
							commands = new ArrayList<String[]>();
							allGlobals.put(tick, commands);
						}
						commands.add(new String[]{cmdId, value});
					}
				}
			}
			trackNum++;
		}
		
		// add slices
		Slice currentSlice = null;
		for (long tick : allGlobals.keySet()) {
			
			// close last slice
			if (currentSlice != null) {
				currentSlice.setEndTick(tick);
			}
			
			// add new slice
			currentSlice = new Slice(tick);
			slices.add(currentSlice);
			
			// add global commands to slice
			ArrayList<String[]> commands = allGlobals.get(tick);
			for (String[] cmd : commands) {
				String cmdId = cmd[ 0 ];
				String value = cmd[ 1 ];
				currentSlice.addGlobalCmd(cmdId, value);
			}
		}
		
		// set end tick of the last slice.
		currentSlice.setEndTick(Long.MAX_VALUE);
	}
	
	/**
	 * Check if we need a new slice according to a detected global command at the given tick.
	 * 
	 * If a new slice is needed, it is added to **slices**.
	 * 
	 * @param tick   MIDI tickstamp.
	 * @param key    ID of the global command
	 * @param value  the global command's value
	 * @return the newly created slice, if created, or otherwise the current (last) slice.
	 */
	private Slice addSliceIfNecessary(long tick, String key, String value) {
		
		// get current slice
		Slice currentSlice = slices.get(slices.size() - 1);
		
		// do we need a new slice or not?
		if (0 == tick || tick == currentSlice.getBeginTick()) {
			
			// no new slice needed
			currentSlice.addGlobalCmd(key, value);
			
			return currentSlice;
		}
		else {
			currentSlice.setEndTick(tick);
			Slice newSlice = new Slice(tick);
			newSlice.addGlobalCmd(key, value);
			slices.add(newSlice);
			return newSlice;
		}
	}
	
	/**
	 * Adds all ticks to the timeline with at least one instrument change.
	 */
	private void addInstrumentsToSlices() {
		
		// add instrument change ticks to the timeline
		for (byte channel = 0; channel < 16; channel++) {
			for (long tick : instrumentHistory.get(channel).keySet()) {
				if (tick > 0) {
					Slice slice = Slice.getSliceByTick(slices, tick);
					slice.addInstrChange(tick, channel);
				}
			}
		}
	}
	
	/**
	 * Groups notes with note-on/note-off/velocity differences that are smaller
	 * than the according tolerances.
	 */
	private void groupNotes() {
		
		// nothing to do?
		if (0 == CHORD_NOTE_ON_TOLERANCE
			&& 0 == CHORD_NOTE_OFF_TOLERANCE
			&& 0 == CHORD_VELOCITY_TOLERANCE)
			return;
		
		// clone the local copy of the structures to be modified
		TreeMap<Byte, TreeMap<Long, TreeMap<Byte, Byte>>>    noteHistoryClone = new TreeMap<>();
		TreeMap<Byte, TreeMap<Byte, TreeMap<Long, Boolean>>> noteOnOffClone   = (TreeMap<Byte, TreeMap<Byte, TreeMap<Long, Boolean>>>) noteOnOff.clone();
		
		// CHANNEL:
		for (byte channel : noteHistory.keySet()) {
			TreeMap<Long, TreeMap<Byte, Byte>>    channelHistoryOriginal = noteHistory.get(channel);
			TreeMap<Long, TreeMap<Byte, Byte>>    channelHistoryClone    = new TreeMap<>();
			TreeMap<Byte, TreeMap<Long, Boolean>> channelOnOffClone      = noteOnOffClone.get(channel);
			
			long skipUntil = -1;
			
			TICK:
			for (Long tick : channelHistoryOriginal.keySet()) {
				
				if (tick <= skipUntil)
					continue TICK;
				
				TreeMap<Byte, Byte> tickStructOriginal = channelHistoryOriginal.get(tick);
				TreeMap<Byte, Byte> tickStructClone    = new TreeMap<>();
				
				// NOTE:
				for (Entry<Byte, Byte> noteEntry : tickStructOriginal.entrySet()) {
					byte note     = noteEntry.getKey();
					byte velocity = noteEntry.getValue();
					
					// copy note to tick
					tickStructClone.put(note, velocity);
				}
				
				FUTURE_TICK:
				for (Long futureTick = tick + 1; futureTick <= tick + CHORD_NOTE_ON_TOLERANCE; futureTick++) {
					TreeMap<Byte, Byte> futureTickStruct = channelHistoryOriginal.get(futureTick);
					if (null == futureTickStruct)
						continue FUTURE_TICK;
					
					// copy notes to the first notes' tick
					for (Entry<Byte, Byte> futureTickEntry : futureTickStruct.entrySet()) {
						byte note     = futureTickEntry.getKey();
						byte velocity = futureTickEntry.getValue();
						tickStructClone.put(note, velocity);
						
						// update ON tick in the ON/OFF structure
						Boolean onOff = channelOnOffClone.get(note).get(futureTick);
						if (onOff != null && onOff) {
							channelOnOffClone.get(note).remove(futureTick);
							channelOnOffClone.get(note).put(tick, true);
						}
					}
					
					// don't process this tick again
					skipUntil = futureTick;
				}
				
				// TODO: adjust OFF TICK and velocity
				TreeMap<String, Long[]> chordIds = new TreeMap<>();
				NOTE:
				for (Entry<Byte, Byte> tickEntry: tickStructClone.entrySet()) {
					byte note     = tickEntry.getKey();
					byte velocity = tickEntry.getValue();
					long offTick  = channelOnOffClone.get(note).ceilingKey(tick + 1);
					
					String chordId = offTick + "/" + velocity;
					
					// already part of a chord?
					if (chordIds.containsKey(chordId))
						continue NOTE;
					
					// possible to become part of a chord?
					for (Entry<String, Long[]> candidate : chordIds.entrySet()) {
						Long[] values       = candidate.getValue();
						long   crdOffTick   = values[0];
						byte   crdVelocity  = (byte) (long) values[1];
						long   diffOff      = Math.abs(crdOffTick  - offTick);
						long   diffVelocity = Math.abs(crdVelocity - velocity);
						if (diffOff <= CHORD_NOTE_OFF_TOLERANCE && diffVelocity <= CHORD_VELOCITY_TOLERANCE) {
							if (diffOff != 0) {
								channelOnOffClone.get(note).remove(offTick);
								channelOnOffClone.get(note).put(crdOffTick, false);
							}
							if (diffVelocity != 0) {
								tickEntry.setValue(crdVelocity);
							}
							continue NOTE;
						}
					}
					
					// create a new chord-ID
					chordIds.put(chordId, new Long[]{offTick, (long) velocity});
				}
				
				// copy tick to channel
				channelHistoryClone.put(tick, tickStructClone);
			}
			
			// copy channel to history
			noteHistoryClone.put(channel, channelHistoryClone);
		}
		
		// replace the local copy with the adjusted clone
		noteHistory = noteHistoryClone;
		noteOnOff   = noteOnOffClone;
	}
	
	/**
	 * Fills the timeline structures of the slices with note-on events regarding
	 * chords and single notes.
	 */
	private void addNotesToSlices() {
		
		// process notes slice by slice
		for (Slice slice : slices) {
			
			// filter notes by slice
			TreeMap<Byte, TreeMap<Long, TreeMap<Byte, Byte>>>    sliceNoteHistory = slice.filterNotes(noteHistory);
			TreeMap<Byte, TreeMap<Byte, TreeMap<Long, Boolean>>> sliceOnOff       = slice.filterOnOff(noteOnOff);
			
			// CHANNEL:
			for (Entry<Byte, TreeMap<Long, TreeMap<Byte, Byte>>> channelSet : sliceNoteHistory.entrySet()) {
				byte                               channel        = channelSet.getKey();
				TreeMap<Long, TreeMap<Byte, Byte>> channelHistory = channelSet.getValue();
				
				// TICK:
				for (Entry<Long, TreeMap<Byte, Byte>> tickSet : channelHistory.entrySet()) {
					long tick                      = tickSet.getKey();
					TreeMap<Byte, Byte> tickStruct = tickSet.getValue();
					
					// create notes structure for this tick
					TreeMap<String, TreeMap<Byte, String>> notesStruct = new TreeMap<>();
					
					// NOTE:
					for (Entry<Byte, Byte> noteSet : tickStruct.entrySet()) {
						byte note     = noteSet.getKey();
						byte velocity = noteSet.getValue();
						Long offTick  = sliceOnOff.get(channel).get(note).ceilingKey(tick + 1);
						
						// TODO: handle the case that there is no offTick at all
						// can happen if the MIDI is corrupt or uses all-notes-off / all-sounds-off instead of note-off
						if (null == offTick) {
							System.err.println("note-off not found for channel " + channel + ", note: " + note + ", tick: " + tick);
						}
						
						// create structure for this note
						TreeMap<Byte, String> noteStruct = new TreeMap<>();
						noteStruct.put( NP_VELOCITY, velocity + "" );
						noteStruct.put( NP_OFF_TICK, offTick  + "" );
						
						// add to the tick notes
						String noteName = Dict.getNote((int) note);
						if (9 == channel) {
							noteName = Dict.getPercussionShortId((int) note);
							if ( noteName.equals(Dict.get(Dict.UNKNOWN_PERCUSSION_NAME)) ) {
								noteName = note + ""; // name unknown - use number instead
							}
						}
						notesStruct.put(noteName, noteStruct);
					}
					
					// transform notes into chords, if possible
					if (notesStruct.keySet().size() > 1) {
						organizeChords(notesStruct, 9 == channel);
					}
					
					// add all notes/chords of this tick/channel to the timeline of the slice/channel
					slice.addNotesToTimeline(tick, channel, notesStruct);
				}
			}
		}
	}
	
	/**
	 * Adds syllables to the slices' timelines.
	 * 
	 * If there are any notes or chords played in the same tick as the syllable, the syllable is added to
	 * one of them (according to the channel's priority).
	 * 
	 * If there are no notes or chords played in the syllable's tick, the syllable is added to the timeline
	 * as an option to a rest.
	 */
	private void addLyricsToSlices() {
		
		TICK:
		for (long tick : lyricsSyllables.keySet()) {
			Slice  slice    = Slice.getSliceByTick(slices, tick);
			String syllable = lyricsSyllables.get(tick);
			
			CHANNEL:
			for (byte channel : lyricsChannels) {
				TreeMap<Byte, TreeMap<String, TreeMap<Byte, String>>> events = slice.getTimeline(channel).get(tick);
				
				// no event for this channel/tick
				if (null == events)
					continue CHANNEL;
				
				// no notes for this channel/tick?
				TreeMap<String, TreeMap<Byte, String>> notes = events.get(ET_NOTES);
				if (null == notes)
					continue CHANNEL;
				
				// add the syllable to the first available note/chord
				String noteOrChord = notes.firstKey();
				TreeMap<Byte, String> params = notes.get(noteOrChord);
				params.put(NP_LYRICS, syllable);
				
				continue TICK;
			}
			
			// If we reach this point, there is no matching note/chord.
			// Add the syllable to the slice using a rest.
			byte channel = lyricsChannels.get(0);
			slice.addSyllableRest(tick, syllable, channel, ORPHANED_SYLLABLES, sourceResolution);
		}
	}
	
	/**
	 * Calculates the note length (according to the 3rd column of a channel command)
	 * and duration percentage (duration option of a channel command).
	 * 
	 * Uses one of the following values:
	 * 
	 * - The length between note-ON and note-ON of the next note, if this is reasonable.
	 * - The length according to the channel's current duration ratio.
	 * - The lowest possible of the predefined values, that is higher than the note press length.
	 * 
	 * The priority of the actually chosen value to be used is controlled by the configuration.
	 * 
	 * @param onTick   Note-ON tick of the note.
	 * @param offTick  Note-OFF tick of the note.
	 * @return the following values:
	 * 
	 * - note length in ticks (according to the source resolution)
	 * - duration in percent (rounded mathematically)
	 */
	private long[] getNoteLengthProperties(long onTick, long offTick, byte channel) {
		
		long  pressTicks = offTick - onTick;
		long  noteTicks;
		float durationRatio;
		
		// TODO: Handle the case of different durations in the same tick,
		// TODO: because than instruments.get(channel).getDurationRatio() will already be outdated.
		
		// 1st strategy: calculate note length according to the current duration ratio
		float oldDuration    = instruments.get(channel).getDurationRatio();
		long  noteTicksByDur = (long) ((pressTicks / (double) oldDuration)) - DURATION_TICK_TOLERANCE;
		noteTicksByDur       = getNoteLengthByPressTicks(noteTicksByDur);
		float durationByDur  = calculateDuration(noteTicksByDur, pressTicks);
		float durationDiff   = oldDuration > durationByDur ? oldDuration - durationByDur : durationByDur - oldDuration;
		boolean canUseByDur  = durationDiff < DURATION_RATIO_TOLERANCE;
		if (oldDuration < MIN_DURATION_TO_KEEP) {
			canUseByDur = false;
		}
		
		// 2nd strategy: calculate note length according to the next note-ON
		long  noteTicksByOn = -1;
		float durationByOn  = -1;
		Long nextNoteOnTick = noteHistory.get(channel).ceilingKey(onTick + 1);
		if (nextNoteOnTick != null) {
			noteTicksByOn   = nextNoteOnTick - onTick - NEXT_NOTE_ON_TOLERANCE;
			noteTicksByOn   = getNoteLengthByPressTicks(noteTicksByOn);
			durationByOn    = calculateDuration(noteTicksByOn, pressTicks);
		}
		boolean canUseNextOn = noteTicksByOn > 0 && durationByOn > 0;
		if (noteTicksByOn > MAX_SOURCE_TICKS_ON) {
			canUseNextOn = false;
		}
		
		// apply strategy configuration
		if (STRATEGY_NEXT_PRESS == LENGTH_STRATEGY || STRATEGY_PRESS == LENGTH_STRATEGY) {
			canUseByDur = false;
		}
		if (STRATEGY_DURATION_PRESS == LENGTH_STRATEGY || STRATEGY_PRESS == LENGTH_STRATEGY) {
			canUseNextOn = false;
		}
		if (canUseByDur && canUseNextOn) {
			if (STRATEGY_NEXT_DURATION_PRESS == LENGTH_STRATEGY) {
				canUseByDur = false;
			}
			else if (STRATEGY_DURATION_NEXT_PRESS == LENGTH_STRATEGY) {
				canUseNextOn = false;
			}
		}
		
		// apply strategy
		if (canUseNextOn) {
			noteTicks     = noteTicksByOn;
			durationRatio = durationByOn;
		}
		else if (canUseByDur) {
			noteTicks     = noteTicksByDur;
			durationRatio = durationByDur;
		}
		else {
			// fallback strategy: calculate note length only by press length
			noteTicks     = getNoteLengthByPressTicks(pressTicks);
			durationRatio = calculateDuration(noteTicks, pressTicks);
		}
		
		// calculate duration ratio
		long durationPercent = ((long) ((durationRatio * 100  + 0.5f) * 10) / 10);
		
		// pack result
		long[] result = {
			noteTicks,
			durationPercent,
		};
		return result;
	}
	
	/**
	 * Guesses the theoretical note length just by choosing the next possible pre-defined length.
	 * 
	 * @param ticks  tick difference between note-ON and note-OFF.
	 * @return the guessed note length.
	 */
	private long getNoteLengthByPressTicks(long ticks) {
		
		// init
		long ticksLeft   = ticks;
		long totalLength = 0L;
		
		// sum up
		while (true) {
			Long length = noteLength.ceilingKey(ticks); // next highest length
			if (null == length)
				length = noteLength.floorKey(ticks); // highest possible element
			
			totalLength += length;
			ticksLeft   -= length;
			
			if (ticksLeft <= 0)
				break;
		}
		
		return totalLength;
	}
	
	/**
	 * Calculates the duration ratio.
	 * 
	 * @param noteTicks   full (theoretical) note length in ticks
	 * @param pressTicks  number of ticks that the note is pressed (from note-ON to note-OFF)
	 * @return the duration ratio
	 */
	private static float calculateDuration(long noteTicks, long pressTicks) {
		double duration = ((double) pressTicks / (double) noteTicks);
		return (float) duration;
	}
	
	/**
	 * Receives the notes that are pressed at one tick and channel.
	 * Puts them together as chords, if possible.
	 * 
	 * In case of percussion instruments, inline chords are used.
	 * Otherwise predefined chords are used.
	 * 
	 * @param notes         Notes, pressed at a certain tick in a certain channel.
	 * @param isPercussion  **true** for channel 9, otherwise **false**.
	 */
	private void organizeChords(TreeMap<String, TreeMap<Byte, String>> notes, boolean isPercussion) {
		
		// velocity,off_tick -- notes
		TreeMap<String, TreeSet<Byte>> chordCandidates = new TreeMap<>();
		
		// sort all notes by groups of the same velocity and off_tick
		for (Entry<String, TreeMap<Byte, String>> noteSet : notes.entrySet()) {
			
			String                noteName = noteSet.getKey();
			TreeMap<Byte, String> noteOpts = noteSet.getValue();
			String                velocity = noteOpts.get(NP_VELOCITY);
			String                offTick  = noteOpts.get(NP_OFF_TICK);
			byte                  note     = (byte) Dict.getNote(noteName);
			if (isPercussion) {
				note = (byte) Dict.getPercussion(noteName);
				if (Dict.UNKNOWN_CODE == note) {
					note = Byte.parseByte(noteName);
				}
			}
			
			// add structure for this velocity/off_tick, if not yet done
			String        chordKey   = velocity + "," + offTick;
			TreeSet<Byte> chordNotes = chordCandidates.get(chordKey);
			if (null == chordNotes) {
				chordNotes = new TreeSet<>();
				chordCandidates.put(chordKey, chordNotes);
			}
			
			// add current note
			chordNotes.add(note);
		}
		
		// check if there are notes that we can combine to chords
		for (Entry<String, TreeSet<Byte>> noteSet : chordCandidates.entrySet()) {
			TreeSet<Byte> chordNotes = noteSet.getValue();
			
			// more than 1 note of the same velocity and end tick?
			if (chordNotes.size() > 1) {
				
				// structure for the chord (to replace the candidates' structures
				TreeMap<Byte, String> chordStruct = null;
				
				// create the global chord key
				StringBuilder  chordKey = new StringBuilder();
				Iterator<Byte> it       = chordNotes.iterator();
				boolean        isfirst  = true;
				ArrayList<String> inlineChord = new ArrayList<>();
				while (it.hasNext()) {
					byte   note     = it.next();
					String noteName = Dict.getNote(note);
					if (isPercussion) {
						noteName = Dict.getPercussionShortId(note);
						if ( noteName.equals(Dict.get(Dict.UNKNOWN_PERCUSSION_NAME)) ) {
							noteName = note + "";
						}
					}
					if (isPercussion || ! USE_PRE_DEFINED_CHORDS) {
						inlineChord.add(noteName);
					}
					
					TreeMap<Byte, String> noteStruct = notes.get(noteName);
					if (isfirst) {
						isfirst = false;
						chordKey.append(note);
						chordStruct = noteStruct; // copy note options to the chord
					}
					else {
						chordKey.append("," + note);
					}
					
					// remove the note
					notes.remove(noteName);
				}
				
				// chord not yet available?
				String chordName = chords.get(chordKey.toString());
				if (isPercussion || ! USE_PRE_DEFINED_CHORDS) {
					chordName = String.join(MidicaPLParser.CHORD_SEPARATOR, inlineChord);
				}
				else {
					if (null == chordName) {
						chordName = makeChordName(chordNotes, chordKey.toString());
						chords.put(chordKey.toString(), chordName);
					}
				}
				
				// add the chord
				// predefined chord (for chromatic channels) or inline chord (for percussion channels)
				notes.put(chordName, chordStruct);
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
	private String makeChordName(TreeSet<Byte> notes, String csvNotes) {
		
		byte lowestNote = notes.first();

		// get base name of the chord
		String baseName = Dict.getBaseNoteName(lowestNote);
		
		// get number of chords with this lowest note so far
		Integer count = chordCount.get(baseName);
		if (null == count) {
			count = 0;
		}
		
		// increment chord count
		count++;
		chordCount.put(baseName, count);
		
		// create name
		String chordName = "crd_" + baseName + "_" + count;
		
		// store in an ordered form
		ArrayList<String> noteChords = chordsByBaseNote.get(baseName);
		if (null == noteChords) {
			noteChords = new ArrayList<>();
			chordsByBaseNote.put(baseName, noteChords);
		}
		noteChords.add(csvNotes);
		
		return chordName;
	}
	
	/**
	 * Creates the MidicaPL string to be written into the export file.
	 * 
	 * @return MidicaPL string to be written into the export file
	 */
	private String createMidicaPL() {
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
				
				// block with rests that are only used for syllables that don't have a note
				if (slice.hasSyllableRests() && channel == lyricsChannels.get(0)) {
					output.append( createSyllableRestsBlock(slice) );
				}
				
				// normal commands
				output.append( createCommandsFromTimeline(slice, channel) );
			}
		}
		
		// config
		output.append( createConfig() );
		
		// statistics
		output.append( createStatistics() );
		
		return output.toString();
	}
	
	/**
	 * Creates a nestable block containing all rests with syllables that
	 * have no corresponting Note-ON event.
	 * 
	 * @param slice  the sequence slice
	 * @return the created block.
	 */
	private String createSyllableRestsBlock(Slice slice) {
		StringBuilder lines = new StringBuilder();
		TreeMap<Long, String> timeline = slice.getSyllableRestTimeline();
		
		// open the block
		lines.append(MidicaPLParser.BLOCK_OPEN + " " + MidicaPLParser.M);
		lines.append(NEW_LINE);
		
		// get channel and tickstamp
		byte channel      = lyricsChannels.get(0);
		long currentTicks = instruments.get(channel).getCurrentTicks();
		
		// TICK:
		for (Entry<Long, String> entry : timeline.entrySet()) {
			long   restTick = entry.getKey();
			String syllable = entry.getValue();
			
			// need a normal rest before the syllable?
			if (restTick < currentTicks) {
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
			
			long restLength = nextTick - currentTicks;
			
			// add the rest with the syllable
			lines.append( "\t" + createRest(channel, restLength, currentTicks, syllable) );
			currentTicks = nextTick;
		}
		
		// close the block
		lines.append(MidicaPLParser.BLOCK_CLOSE);
		lines.append(NEW_LINE);
		
		return lines.toString();
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
		if ( ! timeline.isEmpty() ) {
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
				if ( ! "".equals(singleLine) )
					lines.add("\t" + mplIds[i] + "\t" + singleLine + NEW_LINE);
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
			block.append("\t\t" + mplIds[i] + "\t" + value + NEW_LINE);
		}
		
		// process info fields
		ArrayList<String> infos = (ArrayList<String>) karaokeInfo.get("sk_infos");
		if (infos != null) {
			for (String info : infos) {
				
				// append info line
				if ( ! "".equals(info) )
					block.append("\t\t" + MidicaPLParser.META_SK_INFO + "\t" + info + NEW_LINE);
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
	 * Creates one INSTRUMENT line for each instrument change in the given channel and tick.
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
			if ( ! "".equals(instrLine) ) {
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
		
		String cmd = "";
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
		
		// tick comment (only for instrument changes
		String lineEnd = NEW_LINE;
		if (tick > 0) {
			lineEnd = createTickComment(tick, true) + NEW_LINE;
		}
		
		// put everything together
		return (
			  cmd
			+ "\t"   + channelStr
			+ "\t"   + programStr
			+ "\t\t" + commentStr
			+ lineEnd
		);
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
				chordBlock.append(MidicaPLParser.CHORD + "\t" + chordName + MidicaPLParser.CHORD_ASSIGNER);
				
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
	 * Creates the block with configuration variables that has been used for decompilation.
	 * 
	 * @return configuration block
	 */
	private String createConfig() {
		StringBuilder statLines = new StringBuilder("");
		
		if ( ! MUST_ADD_CONFIG )
			return statLines.toString();
		
		// headline
		statLines.append(MidicaPLParser.COMMENT + " " + "CONFIGURATION:" + NEW_LINE);
		statLines.append(MidicaPLParser.COMMENT + NEW_LINE);
		
		// config values
		HashMap<String, String> sessionConfig = DecompileConfigController.getSessionConfig();
		ArrayList<String> configKeys = new ArrayList<String>(sessionConfig.keySet());
		Collections.sort(configKeys);
		for (String key : configKeys) {
			String value = sessionConfig.get(key);
			statLines.append(MidicaPLParser.COMMENT + " " + key + "\t" + value + NEW_LINE);
		}
		statLines.append(NEW_LINE + NEW_LINE);
		
		return statLines.toString();
	}
	
	/**
	 * Creates the statistics to be printed at the end of the produced file.
	 * 
	 * @return statistics block
	 */
	private String createStatistics() {
		StringBuilder statLines = new StringBuilder("");
		
		if (MUST_ADD_STATISTICS)
			statLines.append(MidicaPLParser.COMMENT + " " + "STATISTICS:" + NEW_LINE);
		
		// channels
		for (byte channel = 0; channel < 16; channel++) {
			TreeMap<Byte, Integer> channelStats = statistics.get(channel);
			
			// nothing to do?
			if (0 == channelStats.get(STAT_NOTES) && 0 == channelStats.get(STAT_RESTS))
				continue;
			
			if (MUST_ADD_STATISTICS)
				statLines.append(MidicaPLParser.COMMENT + " Channel " + channel + ":" + NEW_LINE);
			statLines.append( createStatisticPart(channelStats, false) );
		}
		
		// total
		if (MUST_ADD_STATISTICS)
			statLines.append(MidicaPLParser.COMMENT + " TOTAL:" + NEW_LINE);
		statLines.append( createStatisticPart(statistics.get(STAT_TOTAL), true) );
		
		return statLines.toString();
	}
	
	/**
	 * Creates the statistics for one part (either a channel or total).
	 * 
	 * @param subStat  statistic structure for the part (channel or total)
	 * @param isTotal  **true**, if this is for the total statistics, **false** for channel statistics
	 * @return the created statistics.
	 */
	private String createStatisticPart(TreeMap<Byte,Integer> subStat, boolean isTotal) {
		StringBuilder stats = new StringBuilder("");
		
		// markers for the quality score
		int    markerCount = 0;
		double markerSum   = 0;
		
		// rests
		{
			int rests = subStat.get(STAT_RESTS);
			if (MUST_ADD_STATISTICS)
				stats.append(MidicaPLParser.COMMENT + "\t" + "Rests: " + rests + NEW_LINE);
			
			// rests / notes
			int notes = subStat.get(STAT_NOTES);
			if (notes > 0) {
				double restsPercent = ((double) rests) / ((double) (notes));
				restsPercent *= 100;
				String restsPercentStr = String.format("%.2f", restsPercent);
				if (MUST_ADD_STATISTICS)
					stats.append(MidicaPLParser.COMMENT + "\t\t" + "Rests/Total: " + subStat.get(STAT_REST_SKIPPED) + " (" + restsPercentStr + "%)" + NEW_LINE);
				markerCount++;
				markerSum += (100.0D - restsPercent);
			}
			
			if (rests > 0) {
				
				// rests skipped
				double restsSkipped = ((double) subStat.get(STAT_REST_SKIPPED)) / ((double) rests);
				restsSkipped *= 100;
				String restsSkippedStr = String.format("%.2f", restsSkipped);
				if (MUST_ADD_STATISTICS)
					stats.append(MidicaPLParser.COMMENT + "\t\t" + "Skipped: " + subStat.get(STAT_REST_SKIPPED) + " (" + restsSkippedStr + "%)" + NEW_LINE);
				markerCount++;
				markerSum += (100.0D - restsSkipped);
				
				// rest summands
				int    summands        = subStat.get(STAT_REST_SUMMANDS);
				double summandsPercent = ((double) summands) / ((double) rests);
				summandsPercent *= 100;
				String summandsPercentStr = String.format("%.2f", summandsPercent);
				if (MUST_ADD_STATISTICS)
					stats.append(MidicaPLParser.COMMENT + "\t\t" + "Summands: " + summands + " (" + summandsPercentStr + "%)" + NEW_LINE);
				markerCount++;
				markerSum += 100.0D - (summandsPercent - 100.0D);
				
				// rest triplets
				if (summands > 0) {
					int triplets = subStat.get(STAT_REST_TRIPLETS);
					double tripletsPercent = ((double) triplets) / ((double) summands);
					tripletsPercent *= 100;
					String tripletsStr = String.format("%.2f", tripletsPercent);
					if (MUST_ADD_STATISTICS)
						stats.append(MidicaPLParser.COMMENT + "\t\t" + "Triplets: " + triplets + " (" + tripletsStr + "%)" + NEW_LINE);
					markerCount++;
					markerSum += (100.0D - tripletsPercent);
				}
			}
		}
		
		// notes
		{
			int notes = subStat.get(STAT_NOTES);
			if (MUST_ADD_STATISTICS)
				stats.append(MidicaPLParser.COMMENT + "\t" + "Notes: " + notes + NEW_LINE);
			if (notes > 0) {
				
				// note summands
				int    summands    = subStat.get(STAT_NOTE_SUMMANDS);
				double summandsPercent = ((double) summands) / ((double) notes);
				summandsPercent *= 100;
				String summandsPercentStr = String.format("%.2f", summandsPercent);
				if (MUST_ADD_STATISTICS)
					stats.append(MidicaPLParser.COMMENT + "\t\t" + "Summands: " + summands + " (" + summandsPercentStr + "%)" + NEW_LINE);
				markerCount++;
				markerSum += 100.0D - (summandsPercent - 100.0D);
				
				// note triplets
				if (summands > 0) {
					int triplets = subStat.get(STAT_NOTE_TRIPLETS);
					double tripletsPercent = ((double) triplets) / ((double) summands);
					tripletsPercent *= 100;
					String tripletsStr = String.format("%.2f", tripletsPercent);
					if (MUST_ADD_STATISTICS)
						stats.append(MidicaPLParser.COMMENT + "\t\t" + "Triplets: " + triplets + " (" + tripletsStr + "%)" + NEW_LINE);
					markerCount++;
					markerSum += (100.0D - tripletsPercent);
				}
				
				// velocity changes
				int    velocities    = subStat.get(STAT_NOTE_VELOCITIES);
				double velocitiesPercent = ((double) velocities) / ((double) notes);
				velocitiesPercent *= 100;
				String velocitiesPercentStr = String.format("%.2f", velocitiesPercent);
				if (MUST_ADD_STATISTICS)
					stats.append(MidicaPLParser.COMMENT + "\t\t" + "Velocity changes: " + velocities + " (" + velocitiesPercentStr + "%)" + NEW_LINE);
				markerCount++;
				markerSum += (100.0D - velocitiesPercent);
				
				// duration changes
				int    durations   = subStat.get(STAT_NOTE_DURATIONS);
				double durationPercent = ((double) durations) / ((double) notes);
				durationPercent *= 100;
				String durationPercentStr = String.format("%.2f", durationPercent);
				if (MUST_ADD_STATISTICS)
					stats.append(MidicaPLParser.COMMENT + "\t\t" + "Duration changes: " + durations + " (" + durationPercentStr + "%)" + NEW_LINE);
				markerCount++;
				markerSum += (100.0D - durationPercent);
				
				// multiple option
				int    multiple        = subStat.get(STAT_NOTE_MULTIPLE);
				double multiplePercent = ((double) multiple) / ((double) notes);
				multiplePercent *= 100;
				String multiplePercentStr = String.format("%.2f", multiplePercent);
				if (MUST_ADD_STATISTICS)
					stats.append(MidicaPLParser.COMMENT + "\t\t" + "Multiple option: " + multiple + " (" + multiplePercentStr + "%)" + NEW_LINE);
				markerCount++;
				markerSum += (100.0D - multiplePercent);
			}
		}
		
		// quality score
		if (isTotal) {
			if (MUST_ADD_STATISTICS && MUST_ADD_QUALITY_SCORE)
				stats.append(MidicaPLParser.COMMENT + NEW_LINE);
			double totalScore = ((double) markerSum) / markerCount;
			String totalScoreStr = String.format("%.2f", totalScore);
			if (MUST_ADD_QUALITY_SCORE)
				stats.append(MidicaPLParser.COMMENT + " QUALITY SCORE: " + totalScoreStr + NEW_LINE);
		}
		
		// empty line
		if (MUST_ADD_STATISTICS || MUST_ADD_QUALITY_SCORE) {
			if (isTotal)
				stats.append(NEW_LINE);
			else if (MUST_ADD_STATISTICS)
				stats.append(MidicaPLParser.COMMENT + NEW_LINE);
		}
		
		return stats.toString();
	}
	
	/**
	 * Creates a rest before a new slice begins, if necessary.
	 * 
	 * The rest is only necessary if no channel has reached the slice's begin tick yet.
	 * 
	 * With the slice's beginning, all channels are synchronized. Therefore the rest is
	 * only necessary in one channel.
	 * 
	 * Steps:
	 * 
	 * - calculation of the furthest tick
	 * - choosing the according channel
	 * - adding the rest in this channel
	 * 
	 * @param slice  the sequence slice
	 * @return the created rest, or an empty string if no rest is created.
	 */
	private String createRestBeforeSlice(Slice slice) {
		StringBuilder restStr = new StringBuilder("");
		
		// choose a channel
		long maxTick = Instrument.getMaxCurrentTicks(instruments);
		Instrument chosenInstr = null;
		for (Instrument instr : instruments) {
			
			// ignore automatic channels
			if (instr.autoChannel)
				continue;
			
			// furthest channel found?
			if (maxTick == instr.getCurrentTicks()) {
				chosenInstr = instr;
				break;
			}
		}
		
		// no notes? - default to the percussion channel
		if (null == chosenInstr)
			chosenInstr = instruments.get(9);
		
		// get missing ticks
		long missingTicks = slice.getBeginTick() - chosenInstr.getCurrentTicks();
		if (missingTicks > 0) {
			byte channel = (byte) chosenInstr.channel;
			restStr.append( createRest(channel, missingTicks, -1, null) );
		}
		
		return restStr.toString();
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
		long maxTick = Instrument.getMaxCurrentTicks(instruments);
		for (Instrument instr : instruments) {
			instr.setCurrentTicks(maxTick);
		}
		
		// tick comment
		result.append( createTickComment(slice.getBeginTick(), false) );
		
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
		Instrument instr  = instruments.get(channel);
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
	 * (Or a rest with a syllable, if orphaned syllables are configured as INLINE.)
	 * 
	 * @param channel    MIDI channel
	 * @param noteName   note or chord name
	 * @param noteOrCrd  note properties (from the slice's timeline)
	 * @param tick       MIDI tickstamp.
	 * @return the created line.
	 */
	private String createSingleNoteLine(byte channel, String noteName, TreeMap<Byte, String> noteOrCrd, long tick) {
		StringBuilder line = new StringBuilder("");
		
		Instrument instr = instruments.get(channel);
		
		// main part of the command
		line.append(channel + "\t" + noteName + "\t" + noteOrCrd.get(NP_LENGTH));
		
		// get options that must be appended
		ArrayList<String> options = new ArrayList<>();
		{
			// multiple
			if (noteOrCrd.containsKey(NP_MULTIPLE)) {
				options.add(MidicaPLParser.M);
				incrementStats(STAT_NOTE_MULTIPLE, channel);
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
			line.append("\t" + optionsStr);
		}
		
		// finish the line
		line.append( createTickComment(tick, true) );
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
	private String createRest(byte channel, long ticks, long beginTick, String syllable) {
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
			line.append(channel + "\t" + MidicaPLParser.REST + "\t" + length);
			incrementStats(STAT_RESTS, channel);
		}
		else {
			// TODO: Dict
			// TODO: add warning
			System.err.println("rest too small to be handled: " + ticks + " ticks");
			line.append("// rest too small to be handled: " + ticks + " ticks");
			incrementStats(STAT_REST_SKIPPED, channel);
		}
		
		// add lyrics option, if needed
		if (syllable != null) {
			syllable = escapeSyllable(syllable);
			line.append("\t" + MidicaPLParser.L + MidicaPLParser.OPT_ASSIGNER + syllable);
		}
		
		// finish the line
		if (beginTick >= 0) {
			line.append( createTickComment(beginTick, true) );
		}
		line.append(NEW_LINE);
		
		return line.toString();
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
	 * Splits a note or rest length into several lengths that can be used for a length sum.
	 * 
	 * @param ticks   total tick length of the note or rest length
	 * @param isRest  **true** for a rest, **false** for a note.
	 * @return the single lengths in ticks.
	 */
	private ArrayList<Long> getLengthsForSum(long ticks, boolean isRest) {
		ArrayList<Long> elements = new ArrayList<>();
		
		// init
		TreeMap<Long, String> structure = isRest ? restLength : noteLength;
		long ticksLeft = ticks;
		
		// fill elements
		while (true) {
			Long restTicks = structure.floorKey(ticksLeft);
			
			// continuing makes no sence?
			if (null == restTicks || 0 == ticksLeft || restTicks <= 0)
				break;
			
			// add summand
			ticksLeft -= restTicks;
			elements.add(restTicks);
		}
		
		return elements;
	}
	
	/**
	 * Calculates which tick length corresponds to which note length.
	 * That depends on the resolution of the current MIDI sequence.
	 * 
	 * @return Mapping between tick length and note length for the syntax.
	 */
	private TreeMap<Long, String> initNoteLengths() {
		
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
		
		TreeMap<Long, String> noteLength = new TreeMap<>();
		
		// 32th
		long length32t = calculateTicks( 2, 8 * 3 ); // inside a triplet
		long length32  = calculateTicks( 1, 8     ); // normal length
		long length32d = calculateTicks( 3, 8 * 2 ); // dotted length
		noteLength.put( length32t, d32 + triplet ); // triplet
		noteLength.put( length32,  d32           ); // normal
		noteLength.put( length32d, d32 + dot     ); // dotted
		
		// 16th
		long length16t = calculateTicks( 2, 4 * 3 );
		long length16  = calculateTicks( 1, 4     );
		long length16d = calculateTicks( 3, 4 * 2 );
		noteLength.put( length16t, d16 + triplet );
		noteLength.put( length16,  d16           );
		noteLength.put( length16d, d16 + dot     );
		
		// 8th
		long length8t = calculateTicks( 2, 2 * 3 );
		long length8  = calculateTicks( 1, 2     );
		long length8d = calculateTicks( 3, 2 * 2 );
		noteLength.put( length8t, d8 + triplet );
		noteLength.put( length8,  d8           );
		noteLength.put( length8d, d8 + dot     );
		
		// quarter
		long length4t = calculateTicks( 2, 3 );
		long length4  = calculateTicks( 1, 1 );
		long length4d = calculateTicks( 3, 2 );
		noteLength.put( length4t, d4 + triplet );
		noteLength.put( length4,  d4           );
		noteLength.put( length4d, d4 + dot     );
		
		// half
		long length2t = calculateTicks( 2 * 2, 3 );
		long length2  = calculateTicks( 2,     1 );
		long length2d = calculateTicks( 2 * 3, 2 );
		noteLength.put( length2t, d2 + triplet );
		noteLength.put( length2,  d2           );
		noteLength.put( length2d, d2 + dot     );
		
		// full
		long length1t = calculateTicks( 4 * 2, 3 );
		long length1  = calculateTicks( 4,     1 );
		long length1d = calculateTicks( 4 * 3, 2 );
		noteLength.put( length1t, d1 + triplet );
		noteLength.put( length1,  d1           );
		noteLength.put( length1d, d1 + dot     );
		
		// 2 full notes
		long length_m2  = calculateTicks( 8,     1 );
		long length_m2d = calculateTicks( 8 * 3, 2 );
		noteLength.put( length_m2,  m2        );
		noteLength.put( length_m2d, m2  + dot );
		
		// 4 full notes
		long length_m4  = calculateTicks( 16,     1 );
		long length_m4d = calculateTicks( 16 * 3, 2 );
		noteLength.put( length_m4,  m4        );
		noteLength.put( length_m4d, m4  + dot );
		
		// 8 full notes
		long length_m8  = calculateTicks( 32,     1 );
		long length_m8d = calculateTicks( 32 * 3, 2 );
		noteLength.put( length_m8,  m8        );
		noteLength.put( length_m8d, m8  + dot );
		
		// 16 full notes
		long length_m16  = calculateTicks( 64,     1 );
		long length_m16d = calculateTicks( 64 * 3, 2 );
		noteLength.put( length_m16,  m16        );
		noteLength.put( length_m16d, m16  + dot );
		
		// 32 full notes
		long length_m32  = calculateTicks( 128,     1 );
		long length_m32d = calculateTicks( 128 * 3, 2 );
		noteLength.put( length_m32,  m32        );
		noteLength.put( length_m32d, m32  + dot );
		
		return noteLength;
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
	private TreeMap<Long, String> initRestLengths() {
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
	private int calculateTicks(int factor, int divisor) {
		return (sourceResolution * factor * 10 + 5) / (divisor * 10);
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
	private String createTickComment(long tick, boolean mustAppend) {
		
		// convert source tick to target tick
		long targetTick = (tick * targetResolution * 10 + 5) / (sourceResolution * 10);
		
		String comment = "";
		if (MUST_ADD_TICK_COMMENTS) {
			if (mustAppend)
				comment = "\t\t\t\t";
			
			comment += MidicaPLParser.COMMENT + " "
				+ Dict.get(Dict.EXPORTER_TICK)  + " "
				+ tick
				+ " ==> "
				+ targetTick;
		}
		
		if (mustAppend)
			return comment;
		return comment + NEW_LINE;
	}
}
