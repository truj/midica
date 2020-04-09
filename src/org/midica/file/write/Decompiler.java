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
import org.midica.ui.file.DecompileConfigController;
import org.midica.ui.file.ExportResult;
import org.midica.ui.model.ComboboxStringOption;
import org.midica.ui.model.ConfigComboboxModel;

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
	protected static final byte ET_INSTR = 1; // instrument change
	protected static final byte ET_NOTES = 2; // notes or chords (or an inline rest with syllable)
	
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
	
	protected static final String NEW_LINE = System.getProperty("line.separator");
	
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
	public static final long    DEFAULT_MAX_TARGET_TICKS_ON      = 3840; // 2 full notes
	public static final long    DEFAULT_NEXT_NOTE_ON_TOLERANCE   = 3;
	public static final float   DEFAULT_MIN_DURATION_TO_KEEP     = 0.05f;
	public static final long    DEFAULT_DURATION_TICK_TOLERANCE  = 2;
	public static final float   DEFAULT_DURATION_RATIO_TOLERANCE = 0.014f;
	public static final boolean DEFAULT_USE_PRE_DEFINED_CHORDS   = true;
	public static final long    DEFAULT_CHORD_NOTE_ON_TOLERANCE  = 0;
	public static final long    DEFAULT_CHORD_NOTE_OFF_TOLERANCE = 0;
	public static final long    DEFAULT_CHORD_VELOCITY_TOLERANCE = 0;
	public static final boolean DEFAULT_USE_DOTTED_NOTES         = true;
	public static final boolean DEFAULT_USE_DOTTED_RESTS         = true;
	public static final boolean DEFAULT_USE_TRIPLETTED_NOTES     = true;
	public static final boolean DEFAULT_USE_TRIPLETTED_RESTS     = true;
	public static final byte    DEFAULT_ORPHANED_SYLLABLES       = INLINE;
	public static final boolean DEFAULT_KARAOKE_ONE_CHANNEL      = false;
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
	protected static byte          LENGTH_STRATEGY          = DEFAULT_LENGTH_STRATEGY;
	protected static long          MAX_TARGET_TICKS_ON      = DEFAULT_MAX_TARGET_TICKS_ON;
	protected static long          MAX_SOURCE_TICKS_ON      = 0L;
	protected static long          NEXT_NOTE_ON_TOLERANCE   = DEFAULT_NEXT_NOTE_ON_TOLERANCE;
	protected static float         MIN_DURATION_TO_KEEP     = DEFAULT_MIN_DURATION_TO_KEEP;
	protected static long          DURATION_TICK_TOLERANCE  = DEFAULT_DURATION_TICK_TOLERANCE;
	protected static float         DURATION_RATIO_TOLERANCE = DEFAULT_DURATION_RATIO_TOLERANCE;
	protected static boolean       USE_PRE_DEFINED_CHORDS   = DEFAULT_USE_PRE_DEFINED_CHORDS;
	protected static long          CHORD_NOTE_ON_TOLERANCE  = DEFAULT_CHORD_NOTE_ON_TOLERANCE;
	protected static long          CHORD_NOTE_OFF_TOLERANCE = DEFAULT_CHORD_NOTE_OFF_TOLERANCE;
	protected static long          CHORD_VELOCITY_TOLERANCE = DEFAULT_CHORD_VELOCITY_TOLERANCE;
	protected static boolean       USE_DOTTED_NOTES         = DEFAULT_USE_DOTTED_NOTES;
	protected static boolean       USE_DOTTED_RESTS         = DEFAULT_USE_DOTTED_RESTS;
	protected static boolean       USE_TRIPLETTED_NOTES     = DEFAULT_USE_TRIPLETTED_NOTES;
	protected static boolean       USE_TRIPLETTED_RESTS     = DEFAULT_USE_TRIPLETTED_RESTS;
	protected static byte          ORPHANED_SYLLABLES       = DEFAULT_ORPHANED_SYLLABLES;
	protected static boolean       KARAOKE_ONE_CHANNEL      = DEFAULT_KARAOKE_ONE_CHANNEL;
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
	
	/** stores statistics to estimate the decompilation quality */
	protected TreeMap<Byte, TreeMap<Byte, Integer>> statistics = null;
	
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
		
		exportResult         = new ExportResult(true);
		String targetCharset = ((ComboboxStringOption) ConfigComboboxModel.getModel(Config.CHARSET_EXPORT_MPL).getSelectedItem()).getIdentifier();
		
		try {
			
			// file or STDOUT?
			OutputStreamWriter osw;
			if (null == file && Cli.exportToStdout) {
				osw = new OutputStreamWriter(System.out, targetCharset);
			}
			else {
				// create file writer and store it in this.writer
				if ( ! createFile(file) )
					return new ExportResult(false);
				
				// open file for writing
				FileOutputStream fos = new FileOutputStream(file);
				osw = new OutputStreamWriter(fos, targetCharset);
			}
			BufferedWriter writer = new BufferedWriter(osw);
			
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
			noteLength = initLengths(false);
			restLength = initLengths(true);
			
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
		HashMap<String, String> sessionConfig = DecompileConfigController.getSessionConfig();
		MUST_ADD_TICK_COMMENTS   = Boolean.parseBoolean( sessionConfig.get(Config.DC_MUST_ADD_TICK_COMMENTS)   );
		MUST_ADD_CONFIG          = Boolean.parseBoolean( sessionConfig.get(Config.DC_MUST_ADD_CONFIG)          );
		MUST_ADD_QUALITY_SCORE   = Boolean.parseBoolean( sessionConfig.get(Config.DC_MUST_ADD_QUALITY_SCORE)   );
		MUST_ADD_STATISTICS      = Boolean.parseBoolean( sessionConfig.get(Config.DC_MUST_ADD_STATISTICS)      );
		LENGTH_STRATEGY          = Byte.parseByte(       sessionConfig.get(Config.DC_LENGTH_STRATEGY)          );
		MAX_TARGET_TICKS_ON      = Long.parseLong(       sessionConfig.get(Config.DC_MAX_TARGET_TICKS_ON)      );
		NEXT_NOTE_ON_TOLERANCE   = Long.parseLong(       sessionConfig.get(Config.DC_NEXT_NOTE_ON_TOLERANCE)   );
		MIN_DURATION_TO_KEEP     = Float.parseFloat(     sessionConfig.get(Config.DC_MIN_DURATION_TO_KEEP)     );
		DURATION_TICK_TOLERANCE  = Long.parseLong(       sessionConfig.get(Config.DC_DURATION_TICK_TOLERANCE)  );
		DURATION_RATIO_TOLERANCE = Float.parseFloat(     sessionConfig.get(Config.DC_DURATION_RATIO_TOLERANCE) );
		USE_PRE_DEFINED_CHORDS   = Boolean.parseBoolean( sessionConfig.get(Config.DC_USE_PRE_DEFINED_CHORDS)   );
		CHORD_NOTE_ON_TOLERANCE  = Long.parseLong(       sessionConfig.get(Config.DC_CHORD_NOTE_ON_TOLERANCE)  );
		CHORD_NOTE_OFF_TOLERANCE = Long.parseLong(       sessionConfig.get(Config.DC_CHORD_NOTE_OFF_TOLERANCE) );
		CHORD_VELOCITY_TOLERANCE = Long.parseLong(       sessionConfig.get(Config.DC_CHORD_VELOCITY_TOLERANCE) );
		USE_DOTTED_NOTES         = Boolean.parseBoolean( sessionConfig.get(Config.DC_USE_DOTTED_NOTES)         );
		USE_DOTTED_RESTS         = Boolean.parseBoolean( sessionConfig.get(Config.DC_USE_DOTTED_RESTS)         );
		USE_TRIPLETTED_NOTES     = Boolean.parseBoolean( sessionConfig.get(Config.DC_USE_TRIPLETTED_NOTES)     );
		USE_TRIPLETTED_RESTS     = Boolean.parseBoolean( sessionConfig.get(Config.DC_USE_TRIPLETTED_RESTS)     );
		ORPHANED_SYLLABLES       = Byte.parseByte(       sessionConfig.get(Config.DC_ORPHANED_SYLLABLES)       );
		KARAOKE_ONE_CHANNEL      = Boolean.parseBoolean( sessionConfig.get(Config.DC_KARAOKE_ONE_CHANNEL)      );
		EXTRA_GLOBALS            = DecompileConfigController.getExtraGlobalTicks();
		
		// apply indirect configuration
		MAX_SOURCE_TICKS_ON = (MAX_TARGET_TICKS_ON * sourceResolution * 10 + 5) / (targetResolution * 10);
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
	 * Initializes and resets the instruments structures so that
	 * their configurations can be tracked.
	 */
	private void initInstruments() {
		instrumentsByName    = new TreeMap<>();
		instrumentsByChannel = new ArrayList<>();
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
						noteStruct.put( NP_NOTE_NUM, note     + "" );
						
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
					String noteName = Dict.getNote(note);
					if (isPercussion) {
						noteName = Dict.getPercussionShortId(note);
						if ( noteName.equals(Dict.get(Dict.UNKNOWN_PERCUSSION_NAME)) ) {
							noteName = note + "";
						}
					}
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
			byte orphanedSyllables = ORPHANED_SYLLABLES;
			if (ALDA == format) {
				orphanedSyllables = BLOCK;
			}
			slice.addSyllableRest(tick, syllable, channel, orphanedSyllables, sourceResolution);
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
	 * Calculates the note length string and duration percentage of a note (or chord).
	 * 
	 * - the **note length string** is the 3rd column of a MidicaPL channel command or the
	 *   number following the note in ALDA.
	 * - the **duration percentage** is the duration option of a MidicaPL channel command or
	 *   the quantization attribute in ALDA
	 * 
	 * For the calculation, one one of the following values is used:
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
	protected long[] getNoteLengthProperties(long onTick, long offTick, byte channel) {
		
		long  pressTicks = offTick - onTick;
		long  noteTicks;
		float durationRatio;
		
		// TODO: Handle the case of different durations in the same tick,
		// TODO: because than instruments.get(channel).getDurationRatio() will already be outdated.
		
		// 1st strategy: calculate note length according to the current duration ratio
		float oldDuration    = instrumentsByChannel.get(channel).getDurationRatio();
		long  noteTicksByDur = (long) ((pressTicks / (double) oldDuration)) - DURATION_TICK_TOLERANCE;
		noteTicksByDur       = getNoteLengthByPressTicks(noteTicksByDur);
		float durationByDur  = calculateDuration(noteTicksByDur, pressTicks);
		float durationDiff   = oldDuration > durationByDur ? oldDuration - durationByDur : durationByDur - oldDuration;
		boolean canUseByDur  = durationDiff < DURATION_RATIO_TOLERANCE;
		durationByDur        = oldDuration;
		if (durationByDur < MIN_DURATION_TO_KEEP) {
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
	 * Creates the statistics to be printed at the end of the produced file.
	 * 
	 * @return statistics block
	 */
	protected String createStatistics() {
		StringBuilder statLines = new StringBuilder("");
		String comment = getCommentSymbol();
		
		if (MUST_ADD_STATISTICS)
			statLines.append(comment + " " + "STATISTICS:" + NEW_LINE);
		
		// channels
		for (byte channel = 0; channel < 16; channel++) {
			TreeMap<Byte, Integer> channelStats = statistics.get(channel);
			
			// nothing to do?
			if (0 == channelStats.get(STAT_NOTES) && 0 == channelStats.get(STAT_RESTS))
				continue;
			
			if (MUST_ADD_STATISTICS)
				statLines.append(comment + " Channel " + channel + ":" + NEW_LINE);
			statLines.append( createStatisticPart(channelStats, false) );
		}
		
		// total
		if (MUST_ADD_STATISTICS)
			statLines.append(comment + " TOTAL:" + NEW_LINE);
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
		String comment = getCommentSymbol();
		
		// markers for the quality score
		int    markerCount = 0;
		double markerSum   = 0;
		
		// rests
		{
			int rests = subStat.get(STAT_RESTS);
			if (MUST_ADD_STATISTICS)
				stats.append(comment + "\t" + "Rests: " + rests + NEW_LINE);
			
			// rests / notes
			int notes = subStat.get(STAT_NOTES);
			if (notes > 0) {
				double restsPercent = ((double) rests) / ((double) (notes));
				restsPercent *= 100;
				String restsPercentStr = String.format("%.2f", restsPercent);
				if (MUST_ADD_STATISTICS)
					stats.append(comment + "\t\t" + "Rests/Notes: " + rests + "/" + notes + " (" + restsPercentStr + "%)" + NEW_LINE);
				markerCount++;
				markerSum += (100.0D - restsPercent);
			}
			
			if (rests > 0) {
				
				// rests skipped
				double restsSkipped = ((double) subStat.get(STAT_REST_SKIPPED)) / ((double) rests);
				restsSkipped *= 100;
				String restsSkippedStr = String.format("%.2f", restsSkipped);
				if (MUST_ADD_STATISTICS)
					stats.append(comment + "\t\t" + "Skipped: " + subStat.get(STAT_REST_SKIPPED) + " (" + restsSkippedStr + "%)" + NEW_LINE);
				markerCount++;
				markerSum += (100.0D - restsSkipped);
				
				// rest summands
				int    summands        = subStat.get(STAT_REST_SUMMANDS);
				double summandsPercent = ((double) summands) / ((double) rests);
				summandsPercent *= 100;
				String summandsPercentStr = String.format("%.2f", summandsPercent);
				if (MUST_ADD_STATISTICS)
					stats.append(comment + "\t\t" + "Summands: " + summands + " (" + summandsPercentStr + "%)" + NEW_LINE);
				markerCount++;
				markerSum += 100.0D - (summandsPercent - 100.0D);
				
				// rest triplets
				if (summands > 0) {
					int triplets = subStat.get(STAT_REST_TRIPLETS);
					double tripletsPercent = ((double) triplets) / ((double) summands);
					tripletsPercent *= 100;
					String tripletsStr = String.format("%.2f", tripletsPercent);
					if (MUST_ADD_STATISTICS)
						stats.append(comment + "\t\t" + "Triplets: " + triplets + " (" + tripletsStr + "%)" + NEW_LINE);
					markerCount++;
					markerSum += (100.0D - tripletsPercent);
				}
			}
		}
		
		// notes
		{
			int notes = subStat.get(STAT_NOTES);
			if (MUST_ADD_STATISTICS)
				stats.append(comment + "\t" + "Notes: " + notes + NEW_LINE);
			if (notes > 0) {
				
				// note summands
				int    summands    = subStat.get(STAT_NOTE_SUMMANDS);
				double summandsPercent = ((double) summands) / ((double) notes);
				summandsPercent *= 100;
				String summandsPercentStr = String.format("%.2f", summandsPercent);
				if (MUST_ADD_STATISTICS)
					stats.append(comment + "\t\t" + "Summands: " + summands + " (" + summandsPercentStr + "%)" + NEW_LINE);
				markerCount++;
				markerSum += 100.0D - (summandsPercent - 100.0D);
				
				// note triplets
				if (summands > 0) {
					int triplets = subStat.get(STAT_NOTE_TRIPLETS);
					double tripletsPercent = ((double) triplets) / ((double) summands);
					tripletsPercent *= 100;
					String tripletsStr = String.format("%.2f", tripletsPercent);
					if (MUST_ADD_STATISTICS)
						stats.append(comment + "\t\t" + "Triplets: " + triplets + " (" + tripletsStr + "%)" + NEW_LINE);
					markerCount++;
					markerSum += (100.0D - tripletsPercent);
				}
				
				// velocity changes
				int    velocities    = subStat.get(STAT_NOTE_VELOCITIES);
				double velocitiesPercent = ((double) velocities) / ((double) notes);
				velocitiesPercent *= 100;
				String velocitiesPercentStr = String.format("%.2f", velocitiesPercent);
				if (MUST_ADD_STATISTICS)
					stats.append(comment + "\t\t" + "Velocity changes: " + velocities + " (" + velocitiesPercentStr + "%)" + NEW_LINE);
				markerCount++;
				markerSum += (100.0D - velocitiesPercent);
				
				// duration changes
				int    durations   = subStat.get(STAT_NOTE_DURATIONS);
				double durationPercent = ((double) durations) / ((double) notes);
				durationPercent *= 100;
				String durationPercentStr = String.format("%.2f", durationPercent);
				if (MUST_ADD_STATISTICS)
					stats.append(comment + "\t\t" + "Duration changes: " + durations + " (" + durationPercentStr + "%)" + NEW_LINE);
				markerCount++;
				markerSum += (100.0D - durationPercent);
				
				// multiple option
				int    multiple        = subStat.get(STAT_NOTE_MULTIPLE);
				double multiplePercent = ((double) multiple) / ((double) notes);
				multiplePercent *= 100;
				String multiplePercentStr = String.format("%.2f", multiplePercent);
				if (MUST_ADD_STATISTICS)
					stats.append(comment + "\t\t" + "Multiple option: " + multiple + " (" + multiplePercentStr + "%)" + NEW_LINE);
				markerCount++;
				markerSum += (100.0D - multiplePercent);
			}
		}
		
		// quality score
		if (isTotal) {
			if (MUST_ADD_STATISTICS && MUST_ADD_QUALITY_SCORE)
				stats.append(comment + NEW_LINE);
			double totalScore = ((double) markerSum) / markerCount;
			String totalScoreStr = String.format("%.2f", totalScore);
			if (MUST_ADD_QUALITY_SCORE)
				stats.append(comment + " QUALITY SCORE: " + totalScoreStr + NEW_LINE);
		}
		
		// empty line
		if (MUST_ADD_STATISTICS || MUST_ADD_QUALITY_SCORE) {
			if (isTotal)
				stats.append(NEW_LINE);
			else if (MUST_ADD_STATISTICS)
				stats.append(comment + NEW_LINE);
		}
		
		return stats.toString();
	}
	
	/**
	 * Creates the block with configuration variables that has been used for decompilation.
	 * 
	 * @return configuration block
	 */
	protected String createConfig() {
		StringBuilder statLines = new StringBuilder("");
		String comment = getCommentSymbol();
		
		if ( ! MUST_ADD_CONFIG )
			return statLines.toString();
		
		// headline
		statLines.append(comment + " " + "CONFIGURATION:" + NEW_LINE);
		statLines.append(comment + NEW_LINE);
		
		// config values
		HashMap<String, String> sessionConfig = DecompileConfigController.getSessionConfig();
		ArrayList<String> configKeys = new ArrayList<String>(sessionConfig.keySet());
		Collections.sort(configKeys);
		for (String key : configKeys) {
			String value = sessionConfig.get(key);
			statLines.append(comment + " " + key + "\t" + value + NEW_LINE);
		}
		statLines.append(NEW_LINE + NEW_LINE);
		
		return statLines.toString();
	}
	
	/**
	 * Returns the comment symbol of the current format.
	 * 
	 * @return comment symbol.
	 */
	private String getCommentSymbol() {
		if (ALDA == format)
			return "#";
		return MidicaPLParser.COMMENT;
	}
}
