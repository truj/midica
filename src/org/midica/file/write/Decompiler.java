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
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Track;

import org.midica.config.Cli;
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
import org.midica.ui.file.ExportResult;
import org.midica.ui.file.config.DecompileConfigController;
import org.midica.ui.file.config.DecompileConfigView;
import org.midica.ui.model.ComboboxStringOption;
import org.midica.ui.model.ConfigComboboxModel;
import org.midica.ui.model.MidicaTreeModel;

/**
 * This is the base class of all decompiling exporters, translating MIDI into something else.
 * 
 * @author Jan Trukenmüller
 */
public abstract class Decompiler extends Exporter {
	
	/* *****************
	 * constants
	 *******************/
	
	// formats
	protected static final byte MIDICA = 1;
	protected static final byte ALDA   = 2;
	
	// event types
	protected static final byte ET_INSTR      = 1; // instrument change
	protected static final byte ET_NOTES      = 2; // notes or chords (or an inline rest with a syllable)
	protected static final byte ET_INLINE_BLK = 3; // start ticks of inline blocks
	
	// note properties
	protected static final byte NP_VELOCITY = 1; // velocity option
	protected static final byte NP_OFF_TICK = 2; // note-off tick
	protected static final byte NP_END_TICK = 3; // tick at end of note
	protected static final byte NP_LENGTH   = 4; // length column as MidicaPL length
	protected static final byte NP_DURATION = 5; // duration option as float number
	protected static final byte NP_MULTIPLE = 6; // multiple option (value ignored)
	protected static final byte NP_LYRICS   = 7; // lyrics option
	protected static final byte NP_NOTE_NUM = 8; // MIDI note number
	
	// constants for statistics about the decompilation quality
	protected static final byte STAT_TOTAL           = 17;
	protected static final byte STAT_RESTS           = 21;
	protected static final byte STAT_REST_SKIPPED    = 22;
	protected static final byte STAT_REST_TRIPLETS   = 23;
	protected static final byte STAT_REST_SUMMANDS   = 24;
	protected static final byte STAT_NOTES           = 31;
	protected static final byte STAT_NOTE_VELOCITIES = 32;
	protected static final byte STAT_NOTE_DURATIONS  = 33;
	protected static final byte STAT_NOTE_TRIPLETS   = 34;
	protected static final byte STAT_NOTE_SUMMANDS   = 35;
	protected static final byte STAT_NOTE_MULTIPLE   = 36;
	
	// constants for the used note length strategy
	private static final byte STAT_STRATEGY_NEXT_ON  = 41;
	private static final byte STAT_STRATEGY_DURATION = 42;
	private static final byte STAT_STRATEGY_PRESS    = 43;
	
	protected static final String NEW_LINE = System.getProperty("line.separator");
	
	// decompile constants
	public static final byte INLINE_BLOCK                 = 1;
	public static final byte SLICE_BEGIN_BLOCK            = 2;
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
	public static final boolean DEFAULT_MUST_ADD_STRATEGY_STAT   = true;
	public static final byte    DEFAULT_LENGTH_STRATEGY          = STRATEGY_NEXT_DURATION_PRESS;
	public static final long    DEFAULT_MIN_TARGET_TICKS_ON      = 60;    // /32 (32th note)
	public static final long    DEFAULT_MAX_TARGET_TICKS_ON      = 3840;  // *2 (2 full notes)
	public static final float   DEFAULT_MIN_DURATION_TO_KEEP     = 0.2f;  // 20%
	public static final float   DEFAULT_MAX_DURATION_TO_KEEP     = 1.1f;  // 110%
	public static final long    DEFAULT_LENGTH_TICK_TOLERANCE    = 5;
	public static final float   DEFAULT_DURATION_RATIO_TOLERANCE = 0.15f;
	public static final boolean DEFAULT_USE_PRE_DEFINED_CHORDS   = true;
	public static final long    DEFAULT_CHORD_NOTE_ON_TOLERANCE  = 3;
	public static final long    DEFAULT_CHORD_NOTE_OFF_TOLERANCE = 20;
	public static final long    DEFAULT_CHORD_VELOCITY_TOLERANCE = 5;
	public static final boolean DEFAULT_USE_DOTTED_NOTES         = true;
	public static final boolean DEFAULT_USE_DOTTED_RESTS         = true;
	public static final boolean DEFAULT_USE_TRIPLETTED_NOTES     = true;
	public static final boolean DEFAULT_USE_TRIPLETTED_RESTS     = true;
	public static final boolean DEFAULT_USE_KARAOKE              = true;
	public static final boolean DEFAULT_ALL_SYLLABLES_ORPHANED   = false;
	public static final byte    DEFAULT_ORPHANED_SYLLABLES       = INLINE_BLOCK;
	public static final boolean DEFAULT_KARAOKE_ONE_CHANNEL      = false;
	public static final byte    DEFAULT_CTRL_CHANGE_MODE         = INLINE_BLOCK;
	public static final String  DEFAULT_EXTRA_GLOBALS_STR        = "";
	
	/* *****************
	 * class fields
	 *******************/
	
	protected static int format = -1;
	
	// decompile configuration
	protected static boolean       MUST_ADD_TICK_COMMENTS   = DEFAULT_MUST_ADD_TICK_COMMENTS;
	protected static boolean       MUST_ADD_CONFIG          = DEFAULT_MUST_ADD_CONFIG;
	protected static boolean       MUST_ADD_QUALITY_SCORE   = DEFAULT_MUST_ADD_QUALITY_SCORE;
	protected static boolean       MUST_ADD_STATISTICS      = DEFAULT_MUST_ADD_STATISTICS;
	protected static boolean       MUST_ADD_STRATEGY_STAT   = DEFAULT_MUST_ADD_STRATEGY_STAT;
	protected static byte          LENGTH_STRATEGY          = DEFAULT_LENGTH_STRATEGY;
	protected static long          MAX_TARGET_TICKS_ON      = DEFAULT_MAX_TARGET_TICKS_ON;
	protected static long          MIN_TARGET_TICKS_ON      = DEFAULT_MIN_TARGET_TICKS_ON;
	protected static long          MIN_SOURCE_TICKS_ON      = 0L;
	protected static long          MAX_SOURCE_TICKS_ON      = 0L;
	protected static float         MIN_DURATION_TO_KEEP     = DEFAULT_MIN_DURATION_TO_KEEP;
	protected static float         MAX_DURATION_TO_KEEP     = DEFAULT_MAX_DURATION_TO_KEEP;
	protected static long          LENGTH_TICK_TOLERANCE    = DEFAULT_LENGTH_TICK_TOLERANCE;
	protected static float         DURATION_RATIO_TOLERANCE = DEFAULT_DURATION_RATIO_TOLERANCE;
	protected static boolean       USE_PRE_DEFINED_CHORDS   = DEFAULT_USE_PRE_DEFINED_CHORDS;
	protected static long          CHORD_NOTE_ON_TOLERANCE  = DEFAULT_CHORD_NOTE_ON_TOLERANCE;
	protected static long          CHORD_NOTE_OFF_TOLERANCE = DEFAULT_CHORD_NOTE_OFF_TOLERANCE;
	protected static long          CHORD_VELOCITY_TOLERANCE = DEFAULT_CHORD_VELOCITY_TOLERANCE;
	protected static boolean       USE_DOTTED_NOTES         = DEFAULT_USE_DOTTED_NOTES;
	protected static boolean       USE_DOTTED_RESTS         = DEFAULT_USE_DOTTED_RESTS;
	protected static boolean       USE_TRIPLETTED_NOTES     = DEFAULT_USE_TRIPLETTED_NOTES;
	protected static boolean       USE_TRIPLETTED_RESTS     = DEFAULT_USE_TRIPLETTED_RESTS;
	protected static boolean       USE_KARAOKE              = DEFAULT_USE_KARAOKE;
	protected static boolean       ALL_SYLLABLES_ORPHANED   = DEFAULT_ALL_SYLLABLES_ORPHANED;
	protected static byte          ORPHANED_SYLLABLES       = DEFAULT_ORPHANED_SYLLABLES;
	protected static boolean       KARAOKE_ONE_CHANNEL      = DEFAULT_KARAOKE_ONE_CHANNEL;
	protected static byte          CTRL_CHANGE_MODE         = DEFAULT_CTRL_CHANGE_MODE;
	protected static TreeSet<Long> EXTRA_GLOBALS            = null;
	
	protected static int          sourceResolution = 0;
	protected static int          targetResolution = SequenceCreator.DEFAULT_RESOLUTION;
	protected static ExportResult exportResult     = null;
	protected static boolean      isSoftKaraoke    = false;
	
	/** stores the current state of each channel */
	protected static ArrayList<Instrument>       instrumentsByChannel = null;
	protected static TreeMap<String, Instrument> instrumentsByName    = null;
	
	protected static TreeMap<Long, String> noteLength = null;
	protected static TreeMap<Long, String> restLength = null;
	
	/** comma-separated note bytes  --  chord name */
	protected static TreeMap<String, String> chords = null;
	
	/** lowest note  --  chord count */
	protected static TreeMap<String, Integer> chordCount = null;
	
	/** lowest note  --  comma-separated note bytes (This structure is only needed for the sorting: lowest note first, then chord name) */
	protected static TreeMap<String, ArrayList<String>> chordsByBaseNote = null;
	
	/* ******************
	 * instance fields
	 ********************/
	
	/** channels that can be used for lyrics, sorted by priority */
	protected ArrayList<Byte> lyricsChannels = null;
	
	// structures built by SequenceAnalyzer and KaraokeAnalyzer
	protected TreeMap<Byte, TreeMap<Long, Byte[]>>                 instrumentHistory = null;
	protected TreeMap<Byte, TreeMap<Long, String>>                 commentHistory    = null;
	protected TreeMap<Byte, TreeMap<Long, TreeMap<Byte, Byte>>>    noteHistory       = null;
	protected TreeMap<Byte, TreeMap<Byte, TreeMap<Long, Boolean>>> noteOnOff         = null;
	protected TreeMap<Long, String>                                lyricsSyllables   = null;
	
	/**
	 * Stores statistics to estimate the decompilation quality and to count the used strategies.
	 * Parts of this structure:
	 * 
	 * - channel number (or a higher number for the total statistic)
	 * - type number (value of the STAT_... variable)
	 * - appearance count
	 */
	private TreeMap<Byte, TreeMap<Byte, Integer>> statistics = null;
	
	/**
	 * Stores each **slice** of the sequence.
	 * 
	 * A slice begins either with (one or more) global commands or at tick 0.
	 * 
	 * It ends either one tick before a global command or at the end of the sequence.
	 * 
	 * index -- slice
	 */
	protected static ArrayList<Slice> slices = null;
	
	/**
	 * Initializes format-specific data structures.
	 */
	protected abstract void init();
	
	/**
	 * Creates the format-specific string to be written to the exported file.
	 * 
	 * @return the output file content.
	 */
	protected abstract String createOutput();
	
	/**
	 * Creates a rest.
	 * 
	 * @param channel    MIDI channel
	 * @param ticks      tick length of the rest to create
	 * @param beginTick  used for the tick comment (negative value: don't include a tick comment)
	 * @param syllable   a lyrics syllable or (in most cases): **null**
	 * @return the channel command containing the rest.
	 */
	protected abstract String createRest(byte channel, long ticks, long beginTick, String syllable);
	
	/**
	 * Calculates which tick length corresponds to which note or rest length.
	 * That depends on the resolution of the current MIDI sequence.
	 * 
	 * @param rest    **true** to initialize REST lengths, **false** for NOTE lengths
	 * @return Mapping between tick length and note length for the syntax.
	 */
	protected abstract TreeMap<Long, String> initLengths(boolean rest);
	
	/**
	 * Decompiles a MIDI sequence and writes the result either into the given file
	 * or to the standard output.
	 * 
	 * @param  file    target file to be written.
	 * @return warnings that occured during the export.
	 * @throws ExportException if the file can not be exported correctly.
	 */
	public ExportResult export(File file) throws ExportException {
		
		// initialize format specific structures, if necessary
		init();
		
		// prepare export result (including the warning table)
		// The message tree must be postprocessed to make sure that the details column
		// of message-based warnings is filled properly.
		// Otherwise it only works if the InfoView has been opened before the decompilation.
		exportResult = new ExportResult(true);
		MidicaTreeModel model = (MidicaTreeModel) SequenceAnalyzer.getSequenceInfo().get("msg_tree_model");
		model.postprocess();
		
		// charset
		String targetCharset = "UTF-8";
		try {
			// TODO: ALDA charset
			targetCharset = ((ComboboxStringOption) ConfigComboboxModel.getModel(Config.CHARSET_EXPORT_MPL).getSelectedItem()).getIdentifier();
		}
		catch (NullPointerException e) {
			e.printStackTrace(); // bug #72
		}
		
		try {
			
			// file or STDOUT?
			OutputStreamWriter osw;
			if (null == file && Cli.exportToStdout) {
				osw = new OutputStreamWriter(System.out, targetCharset);
			}
			else {
				
				// user doesn't want to overwrite the file?
				if (! createFile(file))
					return new ExportResult(false);
				
				// open file for writing
				FileOutputStream fos = new FileOutputStream(file);
				osw = new OutputStreamWriter(fos, targetCharset);
			}
			BufferedWriter writer = new BufferedWriter(osw);
			
			// get pre-parsed data structures
			instrumentHistory = SequenceAnalyzer.getInstrumentHistory();
			commentHistory    = SequenceAnalyzer.getCommentHistory();
			noteHistory       = SequenceAnalyzer.getNoteHistory();
			noteOnOff         = SequenceAnalyzer.getOnOffHistory();
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
			
			// calculate which tick length corresponds to which note length
			noteLength = initLengths(false);
			restLength = initLengths(true);
			postprocessLengths(noteLength);
			postprocessLengths(restLength);
			
			// fill slices
			addInstrumentsToSlices();
			groupNotes();
			addNotesToSlices();
			addLyricsToSlices();
			
			// create MidicaPL string from the data structures and write it into the file
			writer.write( createOutput() );
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
		HashMap<String, String> sessionConfig = DecompileConfigController.getInstance(
				new DecompileConfigView(), null
		).getSessionConfig();
		MUST_ADD_TICK_COMMENTS   = Boolean.parseBoolean( sessionConfig.get(Config.DC_MUST_ADD_TICK_COMMENTS)   );
		MUST_ADD_CONFIG          = Boolean.parseBoolean( sessionConfig.get(Config.DC_MUST_ADD_CONFIG)          );
		MUST_ADD_QUALITY_SCORE   = Boolean.parseBoolean( sessionConfig.get(Config.DC_MUST_ADD_QUALITY_SCORE)   );
		MUST_ADD_STATISTICS      = Boolean.parseBoolean( sessionConfig.get(Config.DC_MUST_ADD_STATISTICS)      );
		MUST_ADD_STRATEGY_STAT   = Boolean.parseBoolean( sessionConfig.get(Config.DC_MUST_ADD_STRATEGY_STAT)   );
		LENGTH_STRATEGY          = Byte.parseByte(       sessionConfig.get(Config.DC_LENGTH_STRATEGY)          );
		MIN_TARGET_TICKS_ON      = Long.parseLong(       sessionConfig.get(Config.DC_MIN_TARGET_TICKS_ON)      );
		MAX_TARGET_TICKS_ON      = Long.parseLong(       sessionConfig.get(Config.DC_MAX_TARGET_TICKS_ON)      );
		MIN_DURATION_TO_KEEP     = Float.parseFloat(     sessionConfig.get(Config.DC_MIN_DURATION_TO_KEEP)     );
		MAX_DURATION_TO_KEEP     = Float.parseFloat(     sessionConfig.get(Config.DC_MAX_DURATION_TO_KEEP)     );
		LENGTH_TICK_TOLERANCE    = Long.parseLong(       sessionConfig.get(Config.DC_LENGTH_TICK_TOLERANCE)    );
		DURATION_RATIO_TOLERANCE = Float.parseFloat(     sessionConfig.get(Config.DC_DURATION_RATIO_TOLERANCE) );
		USE_PRE_DEFINED_CHORDS   = Boolean.parseBoolean( sessionConfig.get(Config.DC_USE_PRE_DEFINED_CHORDS)   );
		CHORD_NOTE_ON_TOLERANCE  = Long.parseLong(       sessionConfig.get(Config.DC_CHORD_NOTE_ON_TOLERANCE)  );
		CHORD_NOTE_OFF_TOLERANCE = Long.parseLong(       sessionConfig.get(Config.DC_CHORD_NOTE_OFF_TOLERANCE) );
		CHORD_VELOCITY_TOLERANCE = Long.parseLong(       sessionConfig.get(Config.DC_CHORD_VELOCITY_TOLERANCE) );
		USE_DOTTED_NOTES         = Boolean.parseBoolean( sessionConfig.get(Config.DC_USE_DOTTED_NOTES)         );
		USE_DOTTED_RESTS         = Boolean.parseBoolean( sessionConfig.get(Config.DC_USE_DOTTED_RESTS)         );
		USE_TRIPLETTED_NOTES     = Boolean.parseBoolean( sessionConfig.get(Config.DC_USE_TRIPLETTED_NOTES)     );
		USE_TRIPLETTED_RESTS     = Boolean.parseBoolean( sessionConfig.get(Config.DC_USE_TRIPLETTED_RESTS)     );
		USE_KARAOKE              = Boolean.parseBoolean( sessionConfig.get(Config.DC_USE_KARAOKE)              );
		ALL_SYLLABLES_ORPHANED   = Boolean.parseBoolean( sessionConfig.get(Config.DC_ALL_SYLLABLES_ORPHANED)   );
		ORPHANED_SYLLABLES       = Byte.parseByte(       sessionConfig.get(Config.DC_ORPHANED_SYLLABLES)       );
		KARAOKE_ONE_CHANNEL      = Boolean.parseBoolean( sessionConfig.get(Config.DC_KARAOKE_ONE_CHANNEL)      );
		CTRL_CHANGE_MODE         = Byte.parseByte(       sessionConfig.get(Config.DC_CTRL_CHANGE_MODE)         );
		EXTRA_GLOBALS            = DecompileConfigController.getExtraGlobalTicks();
		
		// apply indirect configuration
		MIN_SOURCE_TICKS_ON = (MIN_TARGET_TICKS_ON * sourceResolution * 10 + 5) / (targetResolution * 10);
		MAX_SOURCE_TICKS_ON = (MAX_TARGET_TICKS_ON * sourceResolution * 10 + 5) / (targetResolution * 10);
	}
	
	/**
	 * Initializes data structures for statistics.
	 * This is used for the following statistics:
	 * 
	 * - quality statistics (to estimate the decompilation quality)
	 * - strategy statistics (counts which strategy is used how often)
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
			
			// quality statistics
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
			
			// strategy statistics
			channelStats.put( STAT_STRATEGY_NEXT_ON,  0 );
			channelStats.put( STAT_STRATEGY_DURATION, 0 );
			channelStats.put( STAT_STRATEGY_PRESS,    0 );
		}
	}
	
	/**
	 * Initializes and resets the instruments structures so that
	 * their configurations can be tracked.
	 */
	private void initInstruments() {
		instrumentsByName    = new TreeMap<>();
		instrumentsByChannel = new ArrayList<>();
		
		// make a deep copy (clone) of the instrument history
		TreeMap<Byte, TreeMap<Long, Byte[]>> originalInstrumentHistory = instrumentHistory;
		instrumentHistory = new TreeMap<>();
		for (Entry<Byte, TreeMap<Long, Byte[]>> channelEntry : originalInstrumentHistory.entrySet()) {
			byte channel = channelEntry.getKey();
			TreeMap<Long, Byte[]> originalInstrChanges = channelEntry.getValue();
			
			TreeMap<Long, Byte[]> instrChanges = new TreeMap<Long, Byte[]>();
			instrumentHistory.put(channel, instrChanges);
			
			for (Entry<Long, Byte[]> originalInstrChange : originalInstrChanges.entrySet()) {
				long tick    = originalInstrChange.getKey();
				Byte[] value = originalInstrChange.getValue();
				instrChanges.put(tick, value);
			}
		}
		
		// remove unnecessary instrument changes from the instrument history
		for (Entry<Byte, TreeMap<Long, Byte[]>> channelEntry : originalInstrumentHistory.entrySet()) {
			byte channel = channelEntry.getKey();
			TreeMap<Long, Byte[]> instrChanges = channelEntry.getValue();
			for (Entry<Long, Byte[]> instrChangeEntry : instrChanges.entrySet()) {
				long tick = instrChangeEntry.getKey();
				
				// get next change (if any)
				long nextChangeTick;
				Entry<Long, Byte[]> nextEntry = instrChanges.ceilingEntry(tick + 1);
				if (null == nextEntry)
					nextChangeTick = Long.MAX_VALUE;
				else
					nextChangeTick = nextEntry.getKey();
				
				// get next note-ON tick after the instrument change
				long noteTick;
				Entry<Long, TreeMap<Byte, Byte>> nextNoteEntry = noteHistory.get(channel).ceilingEntry(tick);
				if (null == nextNoteEntry)
					// no note at all - unnecessary
					noteTick = -1;
				else
					noteTick = nextNoteEntry.getKey();
				
				// is the instrument change necessary?
				// (Is the next note between this instrument change and the next one?)
				if (noteTick >= 0 && tick <= noteTick && noteTick < nextChangeTick) {
					continue;
				}
				
				// no, the instrument change is unnecessary - remove it
				instrumentHistory.get(channel).remove(tick);
			}
			instrChanges.size();
		}
		
		// move the first instrument change of each channel to tick zero, if it contains
		// only positive ticks so far.
		// This ensures that all used instruments are initialized as soon as possible.
		// (In MidicaPL: in the first INSTRUMENTS block)
		for (Entry<Byte, TreeMap<Long, Byte[]>> channelEntry : instrumentHistory.entrySet()) {
			TreeMap<Long, Byte[]> instrChanges = channelEntry.getValue();
			
			// get earliest instrument change
			Entry<Long, Byte[]> firstChange = instrChanges.firstEntry();
			if (null == firstChange)
				continue;
			long   tick       = firstChange.getKey();
			Byte[] firstInstr = firstChange.getValue();
			
			// move to tick 0, if necessary
			if (tick > 0) {
				instrChanges.put(0L, firstInstr);
				instrChanges.remove(tick);
			}
		}
		
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
			instrumentsByChannel.add(instr);
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
			if (instrumentsByChannel.get(channel).autoChannel)
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
	 * Removes lengths that are too small to make sense according to the source resolution.
	 * 
	 * The following lengths are removed:
	 * 
	 * - lengths with zero ticks
	 * - lengths with less than half ticks of the next longer length
	 * 
	 * The last step is necessary to avoid length sums like /4+256+256.
	 * Can happen due to rounding errors of very small lengths.
	 * 
	 * @param lengths  note or rest lengths
	 */
	private void postprocessLengths(TreeMap<Long, String> lengths) {
		
		// remove zero-tick lengths
		while (lengths.floorKey(0L) != null) {
			lengths.remove(lengths.floorKey(0L));
		}
		
		// Remove the smallest length, if the second smallest length is more than double of its size.
		
		// Step 1: collect the lengths to be removed
		ArrayList<Long> toRemove = new ArrayList<>();
		Long lastLength = null;
		for (long length : lengths.keySet()) {
			
			// first loop run?
			if (null == lastLength) {
				lastLength = length;
				continue;
			}
			
			// must remove?
			boolean mustRemove = false;
			if (2 * lastLength < length)
				mustRemove = true;
			
			// remove
			if (mustRemove)
				toRemove.add(lastLength);
			
			// stop, if possible
			lastLength = length;
			if (! mustRemove)
				break;
		}
		
		// Step 2: remove the lengths
		for (long length : toRemove) {
			lengths.remove(length);
		}
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
		// TRACK:
		for (Track track : MidiDevices.getSequence().getTracks()) {
			
			// EVENT:
			for (int i=0; i < track.size(); i++) {
				MidiEvent   event = track.get(i);
				long        tick  = event.getTick();
				MidiMessage msg   = event.getMessage();
				
				// short message
				if (msg instanceof ShortMessage) {
					ShortMessage shortMsg = (ShortMessage) msg;
					int  cmd     = shortMsg.getCommand();
					byte channel = (byte) shortMsg.getChannel();
					
					// ignore events that are handled otherwise
					if (ShortMessage.PROGRAM_CHANGE == cmd
					  || ShortMessage.NOTE_ON       == cmd
					  || ShortMessage.NOTE_OFF      == cmd) {
						// ignore
					}
					
					// something else? - add warning
					else {
						exportResult.addWarning(trackNum, tick, channel, Dict.get(Dict.WARNING_IGNORED_SHORT_MESSAGE));
						exportResult.setDetailsOfLastWarning(SequenceAnalyzer.getSingleMsgByMidiMsg(msg));
					}
				}
				
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
						if (ALDA == format)
							value = sharpsOrFlats + "/" + tonality;
					}
					else if (MidiListener.META_TIME_SIGNATURE == type) {
						int numerator   = data[0];
						int exp         = data[1];
						int denominator = (int) Math.pow(2, exp);
						cmdId           = "time";
						value           = numerator + MidicaPLParser.TIME_SIG_SLASH + denominator;
						if (ALDA == format) // not supported?
							continue;
					}
					else if (MidiListener.META_INSTRUMENT_NAME == type) {
						// channel name?
						// TODO: implement or delete
					}
					else {
						// something else
						
						// marker, created by Midica? - ignore
						// end of track?              - ignore
						boolean showWarning = true;
						if (MidiListener.META_MARKER == type || MidiListener.META_END_OF_SEQUENCE == type) {
							showWarning = null != SequenceAnalyzer.getSingleMsgByMidiMsg(msg);
						}
						
						if (showWarning) {
							exportResult.addWarning(trackNum, tick, null, Dict.get(Dict.WARNING_IGNORED_META_MESSAGE));
							exportResult.setDetailsOfLastWarning(SequenceAnalyzer.getSingleMsgByMidiMsg(msg));
						}
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
				
				// add warnings for ignored sysex messages
				if (msg instanceof SysexMessage) {
					exportResult.addWarning(trackNum, tick, null, Dict.get(Dict.WARNING_IGNORED_SYSEX_MESSAGE));
					exportResult.setDetailsOfLastWarning(SequenceAnalyzer.getSingleMsgByMidiMsg(msg));
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
		
		// deep-clone the on/off structure
		TreeMap<Byte, TreeMap<Byte, TreeMap<Long, Boolean>>> noteOnOffClone = new TreeMap<>();
		for (Entry<Byte, TreeMap<Byte, TreeMap<Long, Boolean>>> channelEntry : noteOnOff.entrySet()) {
			byte channel = channelEntry.getKey();
			TreeMap<Byte, TreeMap<Long, Boolean>> notes      = channelEntry.getValue();
			TreeMap<Byte, TreeMap<Long, Boolean>> notesClone = new TreeMap<>();
			noteOnOffClone.put(channel, notesClone);
			for (Entry<Byte, TreeMap<Long, Boolean>> noteEntry : notes.entrySet()) {
				byte note = noteEntry.getKey();
				TreeMap<Long, Boolean> onOff      = noteEntry.getValue();
				TreeMap<Long, Boolean> onOffClone = new TreeMap<>();
				notesClone.put(note, onOffClone);
				for (Entry<Long, Boolean> tickEntry : onOff.entrySet()) {
					long    tick    = tickEntry.getKey();
					boolean onOrOff = tickEntry.getValue();
					onOffClone.put(tick, onOrOff);
				}
			}
		}
		
		// note history: deep clone with grouping
		TreeMap<Byte, TreeMap<Long, TreeMap<Byte, Byte>>> noteHistoryClone = new TreeMap<>();
		
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
				
				// adjust OFF TICK and velocity
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
								adjustOffTick(channelOnOffClone, channel, note, offTick, crdOffTick);
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
	 * Adjusts the Note-OFF tick of a note so that it can join a chord.
	 * 
	 * Checks if there is another ON/OFF between source and target OFF tick.
	 * 
	 * In this case: Doesn't adjust the OFF tick and creates a warning instead.
	 * 
	 * @param channelOnOff  noteOnOff structure for the channel in question (note, offTick, onOff)
	 * @param channel       MIDI channel
	 * @param note          note number
	 * @param offTick       current Note-OFF tick
	 * @param crdOffTick    target Note-OFF tick (Note-OFF tick of the chord)
	 */
	private void adjustOffTick(TreeMap<Byte, TreeMap<Long, Boolean>> channelOnOff, byte channel, byte note, long offTick, long crdOffTick) {
		
		// check if there are ON/OFF events for the same note and channel
		// between original and target tick (including the target tick itself)
		Long conflictTick = null;
		long buffer       = offTick < crdOffTick ? 1 : -1;
		long tick         = offTick + buffer;
		while (tick != crdOffTick + buffer) {
			
			// remove and remember the note event, if available
			Boolean onOff = channelOnOff.get(note).get(tick);
			if (onOff != null) {
				conflictTick = tick;
				break;
			}
			
			// move on
			if (offTick < crdOffTick)
				tick++;
			else
				tick--;
		}
		
		// conflict found?
		if (conflictTick != null) {
			String details = String.format(
				Dict.get(Dict.WARNING_OFF_MOVING_CONFLICT),
				Dict.getNoteOrPercussionName(note, 9 == channel),
				offTick, crdOffTick
			);
			exportResult.addWarning(null, conflictTick, channel, Dict.get(Dict.WARNING_CHORD_GROUPING_FAILED));
			exportResult.setDetailsOfLastWarning(details);
			
			return;
		}
		
		// move the OFF tick
		channelOnOff.get(note).remove(offTick);
		channelOnOff.get(note).put(crdOffTick, false);
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
						
						// TODO: test this
						// handle the case that there is no offTick at all
						// can happen if the MIDI is corrupt or uses all-notes-off / all-sounds-off
						// instead of note-off or note-on with velocity=0
						if (null == offTick) {
							exportResult.addWarning(null, tick, channel, Dict.get(Dict.WARNING_OFF_NOT_FOUND));
							exportResult.setDetailsOfLastWarning(Dict.get(Dict.ERROR_NOTE) + ": " + note);
						}
						
						// create structure for this note
						TreeMap<Byte, String> noteStruct = new TreeMap<>();
						noteStruct.put( NP_VELOCITY, velocity + "" );
						noteStruct.put( NP_OFF_TICK, offTick  + "" );
						noteStruct.put( NP_NOTE_NUM, note     + "" );
						
						// add to the tick notes
						String noteName = Dict.getNoteOrPercussionName(note, 9 == channel);
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
		
		boolean useInlineChords = ! USE_PRE_DEFINED_CHORDS;
		if (ALDA == format) {
			useInlineChords = true;
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
					String noteName = Dict.getNoteOrPercussionName(note, isPercussion);
					if (isPercussion || useInlineChords) {
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
				if (isPercussion || useInlineChords) {
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
	 * Adds syllables to the slices' timelines.
	 * 
	 * If there are any notes or chords played in the same tick as the syllable,
	 * the syllable is added to one of them (according to the channel's priority).
	 * 
	 * If there are no notes or chords played in the syllable's tick,
	 * the syllable is added to a special timeline as an option to a rest inside a nestable block.
	 */
	private void addLyricsToSlices() {
		
		if (! USE_KARAOKE || ALDA == format)
			return;
		
		TICK:
		for (long tick : lyricsSyllables.keySet()) {
			Slice  slice    = Slice.getSliceByTick(slices, tick);
			String syllable = lyricsSyllables.get(tick);
			
			CHANNEL:
			for (byte channel : lyricsChannels) {
				
				if (ALL_SYLLABLES_ORPHANED)
					break CHANNEL;
				
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
			// Add the syllable to a special timeline (inline block or slice begin block)
			byte channel = lyricsChannels.get(0);
			slice.addSyllableRest(tick, syllable, channel, ORPHANED_SYLLABLES, sourceResolution);
		}
	}
	
	/**
	 * Calculates the tick length of a note, based on the current MIDI
	 * sequence's resolution and in relation to a quarter note.
	 * The given factor and divisor influences the resulting note length.
	 * If factor and divisor are the same, the resulting length is exactly
	 * one quarter note.
	 * 
	 * @param factor     the factor to multiply the quarter note with
	 * @param divisor    the divisor to divide the quarter note with
	 * @return mathematically rounded result of resolution * factor / divisor
	 */
	protected int calculateTicks(int factor, int divisor) {
		return (sourceResolution * factor * 10 + 5) / (divisor * 10);
	}
	
	/**
	 * Splits a note or rest length into several lengths that can be used for a length sum.
	 * 
	 * @param ticks   total tick length of the note or rest length
	 * @param isRest  **true** for a rest, **false** for a note.
	 * @return the single lengths in ticks.
	 */
	protected ArrayList<Long> getLengthsForSum(long ticks, boolean isRest) {
		ArrayList<Long> elements = new ArrayList<>();
		
		// init
		TreeMap<Long, String> structure = isRest ? restLength : noteLength;
		long ticksLeft = ticks;
		
		// fill elements
		while (true) {
			Long restTicks = structure.floorKey(ticksLeft);
			
			// continuing makes no sense?
			if (null == restTicks || 0 == ticksLeft || restTicks <= 0)
				break;
			
			// add summand
			ticksLeft -= restTicks;
			elements.add(restTicks);
		}
		
		return elements;
	}
	
	/**
	 * Calculates the note length string and duration percentage of a note (or chord).
	 * 
	 * - the **note length string** is the 3rd column of a MidicaPL channel command or the
	 *   number following the note in ALDA.
	 * - the **duration percentage** is the duration option of a MidicaPL channel command or
	 *   the quantization attribute in ALDA
	 * 
	 * For the calculation, one of the following strategies is used:
	 * 
	 * - The length between note-ON and note-ON of the next note, if this is reasonable.
	 * - The length according to the channel's current duration ratio.
	 * - The lowest possible of the predefined values, that is higher than the note press length.
	 * 
	 * The priority of the actually chosen strategy to be used is controlled by the configuration.
	 * 
	 * @param onTick   Note-ON tick of the note.
	 * @param offTick  Note-OFF tick of the note.
	 * @return the following values:
	 * 
	 * - note length in ticks (according to the source resolution)
	 * - duration in percent (rounded mathematically)
	 */
	protected long[] getNoteLengthProperties(long onTick, long offTick, byte channel) {
		
		long  pressTicks = offTick - onTick;
		long  noteTicks;
		float durationRatio;
		
		// TODO: Handle the case of different durations in the same tick,
		// TODO: because than instruments.get(channel).getDurationRatio() will already be outdated.
		
		// DURATION strategy: calculate note length according to the current duration ratio
		float oldDuration    = instrumentsByChannel.get(channel).getDurationRatio();
		long  noteTicksByDur = (long) ((pressTicks / (double) oldDuration)) - LENGTH_TICK_TOLERANCE;
		noteTicksByDur       = getNoteLengthByPressTicks(noteTicksByDur);
		float durationByDur  = calculateDuration(noteTicksByDur, pressTicks);
		float durationDiff   = oldDuration > durationByDur ? oldDuration - durationByDur : durationByDur - oldDuration;
		boolean canUseByDur  = durationDiff < DURATION_RATIO_TOLERANCE;
		durationByDur        = oldDuration;
		if (durationByDur < MIN_DURATION_TO_KEEP || durationByDur > MAX_DURATION_TO_KEEP) {
			canUseByDur = false;
		}
		
		// next-ON strategy: calculate note length according to the next note-ON tick
		long  noteTicksByOn = -1;
		float durationByOn  = -1;
		Long nextNoteOnTick = noteHistory.get(channel).ceilingKey(onTick + 1);
		if (nextNoteOnTick != null) {
			noteTicksByOn = nextNoteOnTick - onTick - LENGTH_TICK_TOLERANCE;
			noteTicksByOn = getNoteLengthByPressTicks(noteTicksByOn);
			durationByOn  = calculateDuration(noteTicksByOn, pressTicks);
		}
		boolean canUseNextOn = noteTicksByOn > 0 && durationByOn > 0;
		if (noteTicksByOn < MIN_SOURCE_TICKS_ON || noteTicksByOn > MAX_SOURCE_TICKS_ON) {
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
			incrementStats(STAT_STRATEGY_NEXT_ON, channel);
		}
		else if (canUseByDur) {
			noteTicks     = noteTicksByDur;
			durationRatio = durationByDur;
			incrementStats(STAT_STRATEGY_DURATION, channel);
		}
		else {
			// fallback strategy: calculate note length only by press length
			pressTicks   -= LENGTH_TICK_TOLERANCE;
			pressTicks    = pressTicks < 1 ? 1 : pressTicks;
			noteTicks     = getNoteLengthByPressTicks(pressTicks);
			durationRatio = calculateDuration(noteTicks, pressTicks);
			incrementStats(STAT_STRATEGY_PRESS, channel);
		}
		
		// ignore small duration changes
		durationDiff = oldDuration > durationRatio ? oldDuration - durationRatio : durationRatio - oldDuration;
		if (durationDiff < DURATION_RATIO_TOLERANCE) {
			durationRatio = oldDuration;
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
			Long length = noteLength.ceilingKey(ticksLeft); // next highest length
			if (null == length)
				length = noteLength.lastKey(); // highest possible element
			
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
	protected String createRestBeforeSlice(Slice slice) {
		StringBuilder restStr = new StringBuilder("");
		
		// choose a channel
		Instrument chosenInstr = getFurthestInstrument();
		
		// no notes? - default to the percussion channel
		if (null == chosenInstr)
			chosenInstr = instrumentsByChannel.get(9);
		
		// get missing ticks
		long missingTicks = slice.getBeginTick() - chosenInstr.getCurrentTicks();
		if (missingTicks > 0) {
			byte channel = (byte) chosenInstr.channel;
			restStr.append( createRest(channel, missingTicks, -1, null) );
		}
		
		return restStr.toString();
	}
	
	/**
	 * Calculates and returns the most advanced instrument.
	 * (Advanced in terms of "current ticks").
	 * 
	 * Returns **null**, if no instrument is used at all.
	 * 
	 * @return the most advanced instrument, or **null**
	 */
	protected Instrument getFurthestInstrument() {
		long maxTick = Instrument.getMaxCurrentTicks(instrumentsByChannel);
		Instrument chosenInstr = null;
		for (Instrument instr : instrumentsByChannel) {
			
			// ignore automatic channels
			if (instr.autoChannel)
				continue;
			
			// furthest channel found?
			if (maxTick == instr.getCurrentTicks()) {
				chosenInstr = instr;
				break;
			}
		}
		
		return chosenInstr;
	}
	
	/**
	 * Increments the statistics for the channel and total.
	 * 
	 * @param type     statistics type
	 * @param channel  MIDI channel
	 */
	protected void incrementStats(Byte type, Byte channel) {
		int channelValue = statistics.get(channel).get(type);
		int totalValue   = statistics.get(STAT_TOTAL).get(type);
		
		statistics.get(channel).put(type, channelValue + 1);
		statistics.get(STAT_TOTAL).put(type, totalValue + 1);
	}
	
	/**
	 * Creates a warning caused by a skipped rest.
	 * 
	 * @param tick     the tick where the rest should have occurred.
	 * @param ticks    length of the skipped rest in ticks
	 * @param channel  MIDI channel
	 */
	protected void addWarningRestSkipped(Long tick, Long ticks, Byte channel) {
		exportResult.addWarning(null, tick, channel, Dict.get(Dict.WARNING_REST_SKIPPED));
		exportResult.setDetailsOfLastWarning(ticks + " " + Dict.get(Dict.WARNING_REST_SKIPPED_TICKS));
	}
	
	/**
	 * Creates the quality statistics to be printed at the end of the produced file.
	 * 
	 * @return quality statistics block
	 */
	protected String createQualityStats() {
		if (! MUST_ADD_STATISTICS && ! MUST_ADD_QUALITY_SCORE)
			return "";
		
		StringBuilder statLines = new StringBuilder("");
		String comment = getCommentSymbol();
		statLines.append(comment + " " + "QUALITY STATISTICS:" + NEW_LINE);
		statLines.append(comment + NEW_LINE);
		
		// channels
		for (byte channel = 0; channel < 16; channel++) {
			TreeMap<Byte, Integer> channelStats = statistics.get(channel);
			
			// nothing to do?
			if (0 == channelStats.get(STAT_NOTES) && 0 == channelStats.get(STAT_RESTS))
				continue;
			
			statLines.append(comment + " Channel " + channel + ":" + NEW_LINE);
			statLines.append( createQualityStatsPart(channelStats) );
		}
		
		// total
		statLines.append(comment + " TOTAL:" + NEW_LINE);
		statLines.append(createQualityStatsPart(statistics.get(STAT_TOTAL)));
		statLines.append(NEW_LINE);
		
		return statLines.toString();
	}
	
	/**
	 * Creates the quality statistics for one part (either a channel or total).
	 * 
	 * @param subStat    statistic structure for the part (channel or total)
	 * @return the created statistics.
	 */
	private String createQualityStatsPart(TreeMap<Byte,Integer> subStat) {
		StringBuilder stats = new StringBuilder("");
		String comment = getCommentSymbol();
		
		// markers for the quality score
		int    markerCount = 0;
		double markerSum   = 0;
		double subScore;
		
		// rests
		{
			int rests = subStat.get(STAT_RESTS);
			if (MUST_ADD_STATISTICS)
				stats.append(comment + "     " + "Rests: " + rests + NEW_LINE);
			
			// rests / notes
			int notes = subStat.get(STAT_NOTES);
			if (notes > 0) {
				double restsPercent = ((double) rests) / ((double) (notes));
				restsPercent *= 100;
				subScore = 100.0D - restsPercent;
				addQualityDetailsLine(stats, "Rests/Notes:", rests + "/" + notes, restsPercent, subScore);
				markerCount++;
				markerSum += subScore;
			}
			
			if (rests > 0) {
				
				// rests skipped - only for information, no sub score
				double restsSkipped = ((double) subStat.get(STAT_REST_SKIPPED)) / ((double) rests);
				restsSkipped *= 100;
				addQualityDetailsLine(stats, "Skipped:", subStat.get(STAT_REST_SKIPPED) + "", restsSkipped, null);
				
				// rest summands
				int    summands        = subStat.get(STAT_REST_SUMMANDS);
				double summandsPercent = ((double) summands) / ((double) rests);
				summandsPercent *= 100;
				subScore = 200.0D - summandsPercent;
				addQualityDetailsLine(stats, "Summands:", summands + "", summandsPercent, subScore);
				markerCount++;
				markerSum += subScore;
				
				// rest triplets
				if (summands > 0) {
					int triplets = subStat.get(STAT_REST_TRIPLETS);
					double tripletsPercent = ((double) triplets) / ((double) summands);
					tripletsPercent *= 100;
					subScore = 100.0D - tripletsPercent;
					addQualityDetailsLine(stats, "Triplets:", triplets + "", tripletsPercent, subScore);
					markerCount++;
					markerSum += subScore;
				}
			}
		}
		
		// notes
		{
			int notes = subStat.get(STAT_NOTES);
			if (MUST_ADD_STATISTICS)
				stats.append(comment + "     " + "Notes: " + notes + NEW_LINE);
			if (notes > 0) {
				
				// note summands
				int    summands    = subStat.get(STAT_NOTE_SUMMANDS);
				double summandsPercent = ((double) summands) / ((double) notes);
				summandsPercent *= 100;
				subScore = 200.0D - summandsPercent;
				addQualityDetailsLine(stats, "Summands:", summands + "", summandsPercent, subScore);
				markerCount++;
				markerSum += subScore;
				
				// note triplets
				if (summands > 0) {
					int triplets = subStat.get(STAT_NOTE_TRIPLETS);
					double tripletsPercent = ((double) triplets) / ((double) summands);
					tripletsPercent *= 100;
					subScore = 100.0D - tripletsPercent;
					addQualityDetailsLine(stats, "Triplets:", triplets + "", tripletsPercent, subScore);
					markerCount++;
					markerSum += subScore;
				}
				
				// velocity changes
				int    velocities    = subStat.get(STAT_NOTE_VELOCITIES);
				double velocitiesPercent = ((double) velocities) / ((double) notes);
				velocitiesPercent *= 100;
				subScore = 100.0D - velocitiesPercent;
				addQualityDetailsLine(stats, "Velocity changes:", velocities + "", velocitiesPercent, subScore);
				markerCount++;
				markerSum += subScore;
				
				// duration changes
				int    durations   = subStat.get(STAT_NOTE_DURATIONS);
				double durationPercent = ((double) durations) / ((double) notes);
				durationPercent *= 100;
				subScore = 100.0D - durationPercent;
				addQualityDetailsLine(stats, "Duration changes:", durations + "", durationPercent, subScore);
				markerCount++;
				markerSum += subScore;
				
				// multiple option
				if (MIDICA == format) {
					int    multiple        = subStat.get(STAT_NOTE_MULTIPLE);
					double multiplePercent = ((double) multiple) / ((double) notes);
					multiplePercent *= 100;
					subScore = 100.0D - multiplePercent;
					addQualityDetailsLine(stats, "Multiple option:", multiple + "", multiplePercent, subScore);
					markerCount++;
					markerSum += subScore;
				}
			}
		}
		
		// quality score
		if (MUST_ADD_QUALITY_SCORE) {
			double totalScore    = ((double) markerSum) / markerCount;
			String totalScoreStr = String.format("%.2f", totalScore);
			stats.append(comment + "     Quality Score: " + totalScoreStr + NEW_LINE);
		}
		
		// empty line
		if (MUST_ADD_STATISTICS) {
			stats.append(comment + NEW_LINE);
		}
		
		return stats.toString();
	}
	
	/**
	 * Adds a line to the quality statistics, if configured.
	 * 
	 * @param stats         the statistic line
	 * @param name          which kind of sub score (e.g. Summands, Triplets, ...)
	 * @param count         number of occurrences
	 * @param percentage    percentage of occurrences
	 * @param subScore      sub score to be added (or **null**, if no sub score is used)
	 */
	private void addQualityDetailsLine(StringBuilder stats, String name, String count, double percentage, Double subScore) {
		if (! MUST_ADD_STATISTICS)
			return;
		
		String comment = getCommentSymbol();
		stats.append(
			comment + "         " + String.format("%-20s", name)
			+ String.format("%1$10s", count) + " "
			+ String.format(
				"%-9s",
				"(" + String.format("%.2f", percentage) + "%)"
			)
		);
		
		if (MUST_ADD_QUALITY_SCORE && subScore != null) {
			stats.append(" Sub Score: "
				+ String.format(
					"%1$10s",
					String.format("%.2f", subScore)
				)
				+ NEW_LINE
			);
			return;
		}
		
		stats.append(NEW_LINE);
	}
	
	/**
	 * Creates the strategy statistics to be printed at the end of the produced file.
	 * 
	 * @return strategy statistics block
	 */
	protected String createStrategyStats() {
		if (! MUST_ADD_STRATEGY_STAT)
			return "";
		
		StringBuilder statLines = new StringBuilder("");
		String comment = getCommentSymbol();
		
		statLines.append(comment + " " + "STRATEGY STATISTICS:" + NEW_LINE);
		statLines.append(comment + NEW_LINE);
		
		// channels
		for (byte channel = 0; channel < 16; channel++) {
			TreeMap<Byte, Integer> channelStats = statistics.get(channel);
			
			int sum = channelStats.get(STAT_STRATEGY_NEXT_ON)
			        + channelStats.get(STAT_STRATEGY_DURATION)
			        + channelStats.get(STAT_STRATEGY_PRESS);
			
			// nothing to do?
			if (0 == sum)
				continue;
			
			statLines.append(comment + " Channel " + channel + ":" + NEW_LINE);
			statLines.append(createStrategyStatsPart(channelStats));
		}
		
		statLines.append(comment + " TOTAL:" + NEW_LINE);
		statLines.append(createStrategyStatsPart(statistics.get(STAT_TOTAL)));
		statLines.append(NEW_LINE);
		
		return statLines.toString();
	}
	
	/**
	 * Creates the strategy statistics for one part (either a channel or total).
	 * 
	 * @param subStat    statistic structure for the part (channel or total)
	 * @return the created statistics.
	 */
	private String createStrategyStatsPart(TreeMap<Byte,Integer> subStat) {
		StringBuilder stats = new StringBuilder("");
		String comment = getCommentSymbol();
		
		// get counts
		int countNextOn   = subStat.get(STAT_STRATEGY_NEXT_ON);
		int countDuration = subStat.get(STAT_STRATEGY_DURATION);
		int countPress    = subStat.get(STAT_STRATEGY_PRESS);
		int countAll      = countNextOn + countDuration + countPress;
		
		// format counts
		String strCountNextOn   = String.format("%6d", countNextOn);
		String strCountDuration = String.format("%6d", countDuration);
		String strCountPress    = String.format("%6d", countPress);
		String strCountAll      = String.format("%6d", countAll);
		
		// calculate percentages
		String percentNextOn   = String.format("%.2f", ((double) 100) * ((double) countNextOn)   / ((double) (countAll)));
		String percentDuration = String.format("%.2f", ((double) 100) * ((double) countDuration) / ((double) (countAll)));
		String percentPress    = String.format("%.2f", ((double) 100) * ((double) countPress)    / ((double) (countAll)));
		
		// add the lines
		stats.append(comment + "     " + "Sum:      " + strCountAll + NEW_LINE);
		stats.append(comment + "     " + "Next ON:  " + strCountNextOn   + " (" + percentNextOn   + "%)" + NEW_LINE);
		stats.append(comment + "     " + "Duration: " + strCountDuration + " (" + percentDuration + "%)" + NEW_LINE);
		stats.append(comment + "     " + "Press:    " + strCountPress    + " (" + percentPress    + "%)" + NEW_LINE);
		
		return stats.toString();
	}
	
	/**
	 * Creates the block with configuration variables that has been used for decompilation.
	 * 
	 * @return configuration block
	 */
	protected String createConfig() {
		if (! MUST_ADD_CONFIG)
			return "";
		
		StringBuilder statLines = new StringBuilder("");
		String comment = getCommentSymbol();
		
		// headline
		statLines.append(comment + " " + "CONFIGURATION:" + NEW_LINE);
		statLines.append(comment + NEW_LINE);
		
		// config values
		HashMap<String, String> sessionConfig = DecompileConfigController.getInstance(
			new DecompileConfigView(), null
		).getSessionConfig();
		ArrayList<String> configKeys = new ArrayList<String>(sessionConfig.keySet());
		Collections.sort(configKeys);
		for (String key : configKeys) {
			String value = sessionConfig.get(key);
			statLines.append(
				comment + " "
				+ String.format("%-30s", key) + " " + value + NEW_LINE
			);
		}
		statLines.append(NEW_LINE);
		
		return statLines.toString();
	}
	
	/**
	 * Creates a tick description that can be used in tick comments.
	 * The description contains the given tick in source and target resolution.
	 * 
	 * @param tick                 MIDI tickstamp (in source resolution).
	 * @param withCommentSymbol    prefixes a comment symbol, if **true**
	 * @return the comment string.
	 */
	protected String createTickDescription(long tick, boolean withCommentSymbol) {
		
		// convert source tick to target tick
		long   targetTick  = (tick * targetResolution * 10 + 5) / (sourceResolution * 10);
		String description = Dict.get(Dict.EXPORTER_TICK) + " " + tick + " ==> ~" + targetTick;
		
		if (withCommentSymbol)
			return getCommentSymbol() + " " + description;
		else
			return description;
	}
	
	/**
	 * Returns the comment symbol of the current format.
	 * 
	 * @return comment symbol.
	 */
	protected String getCommentSymbol() {
		if (ALDA == format)
			return "#";
		return MidicaPLParser.COMMENT;
	}
}
