/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.midi;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

import org.midica.config.Laf;
import org.midica.file.CharsetUtils;
import org.midica.ui.player.PlayerView;

/**
 * This class analyzes the karaoke-related messages of a MIDI sequence.
 * It collects information from it and builds up the karaoke structures used by the
 * player and the info window.
 * 
 * @author Jan Trukenmüller
 */
public class KaraokeAnalyzer {
	
	// constants
	public  static final int    KARAOKE_TEXT       =  1; // meta message type: text
	public  static final int    KARAOKE_LYRICS     =  2; // meta message type: lyrics
	private static final float  PRE_ALERT_QUARTERS =  1; // pre-alert so many quarter notes before a lyric change
	private static final float  MAX_SYL_SPACE_RATE =  8; // max syllables-per-whitespace - add spaces, if there are not enough
	
	private static String    chosenCharset = null;
	private static LyricUtil lyricUtil     = null;
	
	// fields
	private static String  midiFileCharset  = null;
	private static String  karaokeMode      = null;
	private static long    karLineTick      = -1;
	private static boolean karLineEnded     = false;
	private static long    karPreAlertTicks = 0;
	private static Pattern karPattSecond    = Pattern.compile( Pattern.quote("<span class='second'>") );
	
	// fields to determine META type and track to use for lyrics
	private static TreeMap<Integer, Integer> textTrackCounter   = null;
	private static TreeMap<Integer, Integer> lyricsTrackCounter = null;
	private static int     lyricsTrack = 0;
	private static boolean useLyrics   = true;
	
	private static HashMap<String, Object> karaokeInfo = null;
	
	/** ticks of either lyrics events or pre-alerts */
	private static TreeSet<Long> lyricsEventTicks = null;
	
	/**                    tick */
	private static TreeSet<Long> markerTicks = null;
	
	/**        line begin tick -- syllable tick -- syllable */
	private static TreeMap<Long, TreeMap<Long, String>> lyrics = null;
	
	/** Flat lyrics structure: tick -- syllable */
	private static TreeMap<Long, String> lyricsFlat = null;
	
	/**
	 * This class is only used statically so a public constructor is not needed.
	 */
	private KaraokeAnalyzer() {
	}
	
	/**
	 * Resets all information that can be queried in public getter methods.
	 * This is called before a MIDI or MidicaPL file is parsed so that the
	 * getters don't return the information of a former parsing after a later
	 * parsing failed.
	 */
	public static void reset() {
		lyrics     = null;
		lyricsFlat = null;
	}
	
	/**
	 * Returns the lyrics as tick/syllable pairs.
	 * 
	 * - key: syllable tick
	 * - value: syllable
	 * 
	 * @return lyrics structure as described above.
	 */
	public static TreeMap<Long, String> getLyricsFlat() {
		return lyricsFlat;
	}
	
	/**
	 * Initializes the internal data structures so that they are ready to
	 * be filled with karaoke and lyrics information.
	 * 
	 * @param resolution           The sequence's resolution in ticks per quarter note.
	 * @param charset              The charset that has been chosen in the file chooser.
	 * @param analyzerMarkerTicks  Marker ticks from the {@link SequenceAnalyzer}.
	 */
	public static void init(int resolution, String charset, TreeSet<Long> analyzerMarkerTicks) {
		chosenCharset   = charset;
		markerTicks     = analyzerMarkerTicks;
		lyricUtil       = LyricUtil.getInstance();
		midiFileCharset = null;
		
		// initialize data structures
		karPreAlertTicks   = (long) (resolution / PRE_ALERT_QUARTERS);
		textTrackCounter   = new TreeMap<>();
		lyricsTrackCounter = new TreeMap<>();
		lyricsTrack        = 0;
		useLyrics          = true;
		karaokeMode        = null;
		karLineTick        = -1;
		karLineEnded       = false;
		karaokeInfo        = new HashMap<>();
		lyrics             = new TreeMap<>();
		lyricsFlat         = new TreeMap<>();
		lyricsEventTicks   = new TreeSet<>();
		karaokeInfo.put("lyrics", lyrics);
	}
	
	/**
	 * Resets the character set detected in the sequence.
	 * Called from the SequenceAnalyzer between several parsing runs of the same sequence.
	 */
	public static void resetFileCharset() {
		midiFileCharset = null;
	}
	
	/**
	 * Returns the last detected charset defined by a lyrics event in the sequence.
	 * 
	 * @return the last detected charset.
	 */
	public static String getFileCharset() {
		return midiFileCharset;
	}
	
	/**
	 * Decides which meta event type and track number to use for the lyrics.
	 */
	public static void chooseLyricsTypeAndTrack() {
		
		// choose the best track for text messages
		int maxTextCount = 0;
		int maxTextTrack = 0;
		for (Entry<Integer, Integer> entry : textTrackCounter.entrySet()) {
			int track = entry.getKey();
			int count = entry.getValue();
			if (count > maxTextCount) {
				maxTextCount = count;
				maxTextTrack = track;
			}
		}
		
		// choose the best track for lyrics messages
		int maxLyricsCount = 0;
		int maxLyricsTrack = 0;
		for (Entry<Integer, Integer> entry : lyricsTrackCounter.entrySet()) {
			int track = entry.getKey();
			int count = entry.getValue();
			if (count > maxLyricsCount) {
				maxLyricsCount = count;
				maxLyricsTrack = track;
			}
		}
		
		// use lyrics or text events?
		if (maxTextCount > maxLyricsCount) {
			useLyrics   = false;
			lyricsTrack = maxTextTrack;
		}
		else {
			useLyrics   = true;
			lyricsTrack = maxLyricsTrack;
		}
	}
	
	/**
	 * Returns karaoke-related information.
	 * 
	 * Lyrics related fields:
	 * 
	 * - **lyrics**: lyrics structure containing:
	 *     - line begin tick
	 *     - syllable tick
	 *     - syllable
	 * - **lyrics_full**:
	 *     - the lyrics as one single string
	 * 
	 * Fields from RP-026 tags:
	 * 
	 * - **title**: title tag
	 * - **composer**: composer tag
	 * - **lyricist**: lyrics tag
	 * - **artist**: artist tag
	 * - **software**: software tag
	 * 
	 * Simple Soft Karaoke fields:
	 * 
	 * - **sk_type**: content of the **@K** tag
	 * - **sk_version**: content of the **@V** tag
	 * - **sk_language**: content of the **@L** tag
	 * - **sk_title**: content of first **@T** tag
	 * - **sk_author**: content of second **@T** tag
	 * - **sk_copyright**: content of third **@T** tag
	 * 
	 * Special Soft Karaoke fields:
	 * 
	 * - **sk_infos**: `ArrayList<String>` of all contents of **@I** tags
	 * - **info**: all **@I** tags, joined with \n
	 * 
	 * @return the information as described above.
	 */
	public static HashMap<String, Object> getKaraokeInfo() {
		if (karaokeInfo != null)
			return karaokeInfo;
		return new HashMap<>();
	}
	
	/**
	 * Counts the LYRICS or TEXT event by type and track for later determination about what to use for lyrics.
	 * Also retrieves song and karaoke related fields.
	 * 
	 * @param type     META message type: {@link #KARAOKE_TEXT} or {@link #KARAOKE_LYRICS}.
	 * @param track    track number of the META message
	 * @param content  text content of the META message.
	 */
	public static void countEventAndGetSongInfo(int type, int track, byte[] content) {
		
		if (KARAOKE_TEXT == type) {
			
			// count
			Integer count = textTrackCounter.get(track);
			if (null == count)
				count = 0;
			count++;
			textTrackCounter.put(track, count);
		}
		else if (KARAOKE_LYRICS == type) {
			
			// count
			Integer count = lyricsTrackCounter.get(track);
			if (null == count)
				count = 0;
			count++;
			lyricsTrackCounter.put(track, count);
			
			// charset switch?
			String text       = CharsetUtils.getTextFromBytes(content, chosenCharset, midiFileCharset);
			String newCharset = CharsetUtils.findCharsetSwitch(text);
			if (newCharset != null) {
				midiFileCharset = newCharset;
			}
			
			// {#...=...} song info according to recommended practice RP-026
			HashMap<String, String> info = lyricUtil.getSongInfo(text);
			if (info != null) {
				for (String key : info.keySet()) {
					String value = info.get(key);
					if (LyricUtil.SOFTWARE.equals(key)) {
						SequenceAnalyzer.retrieveSoftwareVersion(value);
					}
					else if (karaokeInfo.containsKey(key)) {
						String oldValue = (String) karaokeInfo.get(key);
						karaokeInfo.put(key, oldValue + "\n" + value);
					}
					else {
						karaokeInfo.put(key, value);
					}
				}
			}
		}
	}
	
	/**
	 * Collects karaoke specific information from meta messages and adds the event to
	 * the lyrics, if necessary.
	 * 
	 * @param type     META message type: {@link #KARAOKE_TEXT} or {@link #KARAOKE_LYRICS}.
	 * @param track    Track number of the META message.
	 * @param tick     Tickstamp of the META message.
	 * @param content  Text content of the META message.
	 */
	public static void addEvent(int type, int track, long tick, byte[] content) {
		
		if (KARAOKE_LYRICS == type) {
			String text = CharsetUtils.getTextFromBytes(content, chosenCharset, midiFileCharset);
			
			// charset switch?
			String newCharset = CharsetUtils.findCharsetSwitch(text);
			if (newCharset != null) {
				midiFileCharset = newCharset;
			}
			text = lyricUtil.removeTags(text);
			processKaraoke(text, KARAOKE_LYRICS, tick, track);
		}
		else if (KARAOKE_TEXT == type) {
			String text = CharsetUtils.getTextFromBytes(content, chosenCharset, midiFileCharset);
			
			// karaoke meta message according to .kar files?
			if (text.startsWith("@") && text.length() > 1) {
				String prefix = text.substring( 0, 2 );
				text          = text.substring( 2 );
				
				// karaoke type definition?
				if ("@K".equals(prefix)) {
					karaokeMode = text;
					karaokeInfo.put("sk_type", karaokeMode);
				}
				
				// version
				else if ("@V".equals(prefix)) {
					if (null == karaokeInfo.get("sk_version")) {
						karaokeInfo.put("sk_version", text);
					}
				}
				
				// language
				else if ("@L".equals(prefix)) {
					if (null == karaokeInfo.get("sk_language")) {
						karaokeInfo.put("sk_language", text);
					}
				}
				
				// title, author or copyright
				else if ("@T".equals(prefix)) {
					if (null == karaokeInfo.get("sk_title")) {
						karaokeInfo.put("sk_title", text);
					}
					else if (null == karaokeInfo.get("sk_author")) {
						karaokeInfo.put("sk_author", text);
					}
					else if (null == karaokeInfo.get("sk_copyright")) {
						karaokeInfo.put("sk_copyright", text);
					}
				}
				
				// further information
				else if ("@I".equals(prefix)) {
					ArrayList<String> infos = (ArrayList<String>) karaokeInfo.get("sk_infos");
					if (null == infos) {
						infos = new ArrayList<>();
						karaokeInfo.put("sk_infos", infos);
					}
					infos.add(text);
				}
				
				// ignore all other messages beginning with "@"
				return;
			}
			
			// Normal text, maybe lyrics.
			// Unfortunately some MIDI files contain lyrics as type TEXT
			// instead of LYRICS without providing an @K header. So we must
			// consider all text as possibly lyrics.
			processKaraoke(text, KARAOKE_TEXT, tick, track);
		}
	}
	
	/**
	 * Processes the given text as a karaoke meta or lyrics command.
	 * 
	 * @param text   The text from the text or lyrics message.
	 * @param type   The message type (1: text, 2: lyrics).
	 * @param tick   Tickstamp of the event.
	 * @param track  Track number of the event.
	 */
	private static void processKaraoke(String text, int type, long tick, int track) {
		
		// Process as syllable-based lyrics?
		// Some @K songs contain duplicate lyrics (text and lyrics messages).
		// In this case show only one part of them.
		if (useLyrics && KARAOKE_TEXT == type)
			return;
		if (! useLyrics && KARAOKE_LYRICS == type)
			return;
		if (lyricsTrack != track)
			return;
		if ("".equals(text))
			return;
		
		// add it to the flat structure (used only by the decompiler)
		if (null == lyricsFlat.get(tick))
			lyricsFlat.put(tick, text);
		
		// UNIFY WHITESPACES FROM DIFFERENT SOURCES
		if (KARAOKE_LYRICS == type) {
			// unescape \r, \n, \t
			text = lyricUtil.unescapeSpecialWhitespaces(text);
		}
		else {
			// unescape /, \
			if (text.startsWith("/"))
				text = "\r" + text.substring(1);
			else if (text.startsWith("\\"))
				text = "\n" + text.substring(1);
		}
		
		// get last or current line
		TreeMap<Long, String> line = lyrics.get(karLineTick);
		
		// Did we already find another lyrics/text event at this tick?
		if (line != null && line.containsKey(tick)) {
			// Assume that the first one was the right one and ignore the rest.
			return;
		}
		
		// do we need (a) new line(s)?
		boolean needNewLineNow         = false;
		boolean beginsWithNewLine      = false;
		boolean beginsWithNewParagraph = false;
		boolean endsWithNewLine        = false;
		boolean endsWithNewParagraph   = false;
		if (-1 == karLineTick || karLineEnded) {
			needNewLineNow = true;
			karLineEnded   = false;
		}
		if (text.length() > 1) { // regard "\r" as belonging only to the LAST line
			if (text.startsWith("\r")) {
				needNewLineNow    = true;
				beginsWithNewLine = true;
			}
			else if (text.startsWith("\n")) {
				needNewLineNow         = true;
				beginsWithNewLine      = true;
				beginsWithNewParagraph = true;
			}
		}
		if (text.endsWith("\r")) {
			endsWithNewLine = true;
			karLineEnded    = true;
		}
		else if (text.endsWith("\n")) {
			endsWithNewLine      = true;
			endsWithNewParagraph = true;
			karLineEnded         = true;
		}
		
		// BEGINNING WITH LINEBREAK(S)?
		// move CR/LF from the beginning to the end of the last syllable
		if (beginsWithNewLine) {
			text = text.substring(1); // remove CR/LF
			if (line != null) {
				Entry<Long, String> lastSylEntry = line.lastEntry();
				long lastSylTick    = lastSylEntry.getKey();
				String lastSyllable = lastSylEntry.getValue();
				
				// add 1st line break (for the new line)
				lastSyllable = lastSyllable + "\n";
				
				// add 2nd line break (for the new paragraph)
				if (beginsWithNewParagraph) {
					lastSyllable = lastSyllable + "\n";
				}
				
				// commit the changes
				line.put( lastSylTick, lastSyllable );
			}
		}
		
		// ADD NEW LINE
		if (needNewLineNow) {
			karLineTick = tick;
			line        = new TreeMap<>();
			lyrics.put(tick, line);
		}
		
		// ENDING WITH LINEBREAK(S)?
		if (endsWithNewLine) {
			text = text.substring(0, text.length() - 1) + "\n"; // replace \r with \n
			if (endsWithNewParagraph) {
				text += "\n"; // add a second line break
			}
		}
		
		// add the syllable
		line.put(tick, text);
		
		// PREPARE MARKER EVENTS
		
		// the lyrics event itself
		lyricsEventTicks.add(tick);
		markerTicks.add(tick);
		
		// pre-alert before the lyrics event
		tick -= karPreAlertTicks;
		if (tick < 0) {
			tick = 0;
		}
		lyricsEventTicks.add(tick);
		markerTicks.add(tick);
	}
	
	/**
	 * Postprocesses data structures.
	 * 
	 * - Joins all info headers (beginning with "@I") to a single string.
	 * - Deletes non printable characters.
	 * - Adds spaces if necessary.
	 * - Creates a full lyrics string for the info view.
	 * - Re-organizes lines, if necessary.
	 * - Replacements for the HTML view.
	 * - HTML formatting for the second voice.
	 */
	public static void postprocess() {
		
		// join all info headers (@I)
		ArrayList<String> infoHeaders = (ArrayList<String>) karaokeInfo.get("sk_infos");
		if (infoHeaders != null) {
			karaokeInfo.put("info", String.join("\n", infoHeaders));
		}
		
		// delete carriage returns from the middle of syllables
		Pattern patCR = Pattern.compile( Pattern.quote("\r") );
		for (TreeMap<Long, String> line : lyrics.values()) {
			for (Entry<Long, String> sylEntry : line.entrySet()) {
				long   tick     = sylEntry.getKey();
				String syllable = sylEntry.getValue();
				syllable        = patCR.matcher(syllable).replaceAll("");
				line.put(tick, syllable);
			}
		}
		
		// Check if there are enough spaces between the syllables.
		int     totalSyllables = 0;
		int     totalSpaces    = 0;
		Pattern pattEnd        = Pattern.compile(".*\\s$", Pattern.MULTILINE); // ends with whitespace
		Pattern pattBegin      = Pattern.compile("^\\s",   Pattern.MULTILINE); // begins with whitespace
		for (TreeMap<Long, String> line : lyrics.values()) {
			
			// put all syllables into an array with indexes instead of ticks
			ArrayList<String> sylsInLine = new ArrayList<>();
			for (String syllable : line.values()) {
				sylsInLine.add( syllable );
			}
			
			// count syllables and spaces
			for (int i = 0; i < sylsInLine.size(); i++) {
				totalSyllables++;
				// space at the end of THIS line?
				boolean hasSpace = pattEnd.matcher( sylsInLine.get(i) ).lookingAt();
				if (! hasSpace && i < sylsInLine.size() - 1) {
					// space at the beginning of the NEXT line?
					hasSpace = pattBegin.matcher( sylsInLine.get(i+1) ).lookingAt();
				}
				if (hasSpace) {
					totalSpaces++;
				}
			}
		}
		boolean needMoreSpaces = true;
		if (totalSpaces != 0) {
			needMoreSpaces = (float) totalSyllables / (float) totalSpaces > MAX_SYL_SPACE_RATE;
		}
		
		// add spaces if necessary
		if (needMoreSpaces) {
			for (TreeMap<Long, String> line : lyrics.values()) {
				for (Entry<Long, String> sylEntry : line.entrySet()) {
					long   tick     = sylEntry.getKey();
					String syllable = sylEntry.getValue();
					
					// word ends with "-". That usually means: The word is not yet over.
					if (syllable.endsWith("-")) {
						// just delete the trailing "-" but don't add a space
						syllable = syllable.replaceFirst( "\\-$", "" );
					}
					else if (! syllable.endsWith("\n")) {
						// add a space
						syllable += " ";
					}
					line.put( tick, syllable );
				}
			}
		}
		
		// create full lyrics string (for the info view)
		StringBuilder lyricsFull = new StringBuilder("");
		for (TreeMap<Long, String> line : lyrics.values()) {
			for (String syllable : line.values()) {
				lyricsFull.append(syllable);
			}
		}
		karaokeInfo.put("lyrics_full", lyricsFull.toString());
		
		// get all ticks where a word ends
		// (needed for syllable hyphenation later)
		Pattern pattEndPunct = Pattern.compile(".*[.,?!\"'\\]\\[;]$", Pattern.MULTILINE); // ends with punctuation character
		TreeSet<Long> wordEndTicks = new TreeSet<>();
		for (TreeMap<Long, String> line : lyrics.values()) {
			long lastSylTick = -1;
			for (Entry<Long, String> sylEntry : line.entrySet()) {
				long   tick     = sylEntry.getKey();
				String syllable = sylEntry.getValue();
				
				// Word ends AFTER this syllable?
				if (pattEnd.matcher(syllable).lookingAt() || pattEndPunct.matcher(syllable).lookingAt()) {
					wordEndTicks.add(tick);
				}
				
				// Word ends BEFORE this syllable?
				if (pattBegin.matcher(syllable).lookingAt()) {
					wordEndTicks.add(lastSylTick);
				}
				lastSylTick = tick;
			}
		}
		
		// reorganize lines if necessary
		TreeMap<Long, TreeMap<Long, String>> newLyrics = new TreeMap<>();
		for (Entry<Long, TreeMap<Long, String>> lineEntry : lyrics.entrySet()) {
			long                  lineTick = lineEntry.getKey();
			TreeMap<Long, String> line     = lineEntry.getValue();
			
			int lineLength = 0; // number of characters in the line, INCLUDING the current syllable
			int lastLength = 0; // number of characters in the line, WITHOUT the current syllable
			
			// create one or more new line(s) from one original line
			TreeMap<Long, String> newLine = new TreeMap<>();
			for (Entry<Long, String> sylEntry : line.entrySet()) {
				long    sylTick  = sylEntry.getKey();
				String  syllable = sylEntry.getValue();
				lineLength      += syllable.length();
				
				// line too long?
				if (lineLength >= PlayerView.KAR_MAX_CHARS_PER_LINE && lastLength > 0) {
					
					// add linebreak to the LAST syllable
					Entry<Long, String> lastSylEntry = newLine.lastEntry();
					long                lastSylTick  = lastSylEntry.getKey();
					String              lastSyllable = lastSylEntry.getValue();
					if (! wordEndTicks.contains(lastSylTick)) {
						lastSyllable += "-";
					}
					lastSyllable += "\n";
					newLine.put(lastSylTick, lastSyllable);
					
					// close the line and open a new one
					newLyrics.put(lineTick, newLine);
					newLine    = new TreeMap<>();
					lineTick   = sylTick;
					lineLength = syllable.length();
				}
				
				// add current syllable and prepare the next one
				newLine.put( sylTick, syllable );
				lastLength = lineLength;
			}
			
			// record the rest of the original line and prepare the next one
			newLyrics.put(lineTick, newLine);
		}
		lyrics = newLyrics;
		
		// HTML replacements and entities
		Pattern pattAMP = Pattern.compile("&");
		Pattern pattLT  = Pattern.compile("<");
		Pattern pattGT  = Pattern.compile(">");
		Pattern pattLF  = Pattern.compile( Pattern.quote("\n") );
		for (TreeMap<Long, String> line : lyrics.values()) {
			for (Entry<Long, String> sylEntry : line.entrySet()) {
				long   tick     = sylEntry.getKey();
				String syllable = sylEntry.getValue();
				syllable        = pattAMP.matcher(syllable).replaceAll("&amp;");
				syllable        = pattLT.matcher(syllable).replaceAll("&lt;");
				syllable        = pattGT.matcher(syllable).replaceAll("&gt;");
				syllable        = pattLF.matcher(syllable).replaceAll(Matcher.quoteReplacement("<br>\n"));
				line.put( tick, syllable );
			}
		}
		
		// HTML formatting for the second voice:
		// - Replace [ and ] inside of syllables by HTML tags.
		// - Replace begin/end of syllables starting/ending in
		//   second-voice-mode by HTML tags. (Necessary if the [ or ] is in a
		//   former/later syllable or line)
		String  htmlStart     = "<span class='second'>";
		String  htmlStop      = "</span>";
		Pattern pattBracket   = Pattern.compile( "(\\]|\\[)", Pattern.MULTILINE ); // '[' or ']'
		boolean isSecondVoice = false;
		for (TreeMap<Long, String> line : lyrics.values()) {
			
			for (Entry<Long, String> sylEntry : line.entrySet()) {
				long         tick        = sylEntry.getKey();
				String       syllable    = sylEntry.getValue();
				boolean      mustModify  = false;
				StringBuffer modifiedSyl = new StringBuffer();
				
				// '[' is in a former syllable? - start in second voice mode
				if (isSecondVoice) {
					mustModify = true;
					modifiedSyl.append( htmlStart );
				}
				
				// replace [ and ] - but ignore nested structures
				Matcher matcher = pattBracket.matcher( syllable );
				while (matcher.find()) {
					String bracket     = matcher.group();
					String htmlReplStr = null;
					if ("[".equals(bracket) && ! isSecondVoice) {
						isSecondVoice = true;
						htmlReplStr   = htmlStart;
					}
					else if ("]".equals(bracket) && isSecondVoice) {
						isSecondVoice = false;
						htmlReplStr   = htmlStop;
					}
					
					// replacement necessary?
					if (htmlReplStr != null) {
						mustModify = true;
						
						// append everything until (including) the replacement
						// to the modified syllable
						matcher.appendReplacement( modifiedSyl, Matcher.quoteReplacement(htmlReplStr) );
					}
				}
				
				// save the modified syllable
				if (mustModify) {
					
					// append the rest of the syllable
					matcher.appendTail( modifiedSyl );
					
					// ']' is in a later syllable? - close second voice wrapping
					if (isSecondVoice) {
						modifiedSyl.append( htmlStop );
					}
					
					// replace the syllable
					line.put( tick, modifiedSyl.toString() );
				}
			}
		}
	}
	
	/**
	 * Returns the tick/line/syllable structure of the lyrics.
	 * 
	 * - first level: line begin tick
	 * - second level: syllable begin tick
	 * - third level: syllable
	 * 
	 * @return the lyrics structure as described above.
	 */
	public static TreeMap<Long, TreeMap<Long, String>> getLyrics() {
		return lyrics;
	}
	
	/**
	 * Returns all ticks where a lyrics event happens (either the lyrics itself for the pre-alert).
	 * 
	 * @return the ticks.
	 */
	public static TreeSet<Long> getLyricsEventTicks() {
		return lyricsEventTicks;
	}
	
	/**
	 * Returns the lyrics including formatting to be displayed
	 * at the given tick.
	 * 
	 * @param tick  Tickstamp in the MIDI sequence.
	 * @return  the lyrics to be displayed.
	 */
	public static String getLyricsForPlayer(long tick) {
		
		if (null == lyrics) {
			return "";
		}
		
		// prepare text
		StringBuilder text = new StringBuilder(
			  "<html><head><style>"
			+ "body {"
			+     "width: "     + PlayerView.KAR_WIDTH     + "; " // "width: 100%" doesn't work
			+     "font-size: " + PlayerView.KAR_FONT_SIZE + "; "
			+     "color: #"    + Laf.COLOR_KAR_1_PAST  + "; "
			+ "}"
			+ ".future { color: #"        + Laf.COLOR_KAR_1_FUTURE + "; } "
			+ ".second { color: #"        + Laf.COLOR_KAR_2_PAST   + "; } "
			+ ".future_second { color: #" + Laf.COLOR_KAR_2_FUTURE + "; } "
			+ "</style></head><body>"
		);
		
		// collect past lines to be shown
		TreeSet<Long> lineTicks = new TreeSet<>();
		long          loopTick  = tick;
		PAST_LINE:
		for (int i = 0; i < PlayerView.KAR_PAST_LINES; i++) {
			Long pastTick = lyrics.floorKey(loopTick);
			if (null == pastTick) {
				break PAST_LINE;
			}
			lineTicks.add(pastTick);
			loopTick = pastTick - 1;
		}
		
		// collect future lines to be shown
		loopTick = tick;
		FUTURE_LINE:
		while (lineTicks.size() < PlayerView.KAR_TOTAL_LINES) {
			Long futureTick = lyrics.ceilingKey( loopTick );
			if (null == futureTick) {
				break FUTURE_LINE;
			}
			lineTicks.add(futureTick);
			loopTick = futureTick + 1;
		}
		
		// process lines
		boolean isPast = true;
		for (long lineTick : lineTicks) {
			TreeMap<Long, String> line = lyrics.get(lineTick);
			
			// process syllables
			for (Entry<Long, String> sylEntry : line.entrySet()) {
				long   sylTick  = sylEntry.getKey();
				String syllable = sylEntry.getValue();
				
				// switch from past to future?
				if (isPast && sylTick > tick) {
					isPast = false;
					text.append("<span class='future'>");
				}
				
				// Adjust second voice CSS class for future syllables. That's
				// needed because CSS class nesting doesn't work in swing.
				// So this is not supported:
				// '<style>.future .second { color: ...; }</style>'
				if (! isPast) {
					// <span class='second'> --> <span class='future_second'>
					// necessary because nesting CSS classes doesn't work in swing
					syllable = karPattSecond.matcher(syllable).replaceAll("<span class='future_second'>");
				}
				
				// must alert?
				if (! isPast && sylTick - tick <= karPreAlertTicks) {
					text.append("<i>" + syllable + "</i>");
				}
				else {
					text.append(syllable);
				}
			}
		}
		text.append("</span></body></html>");
		
		return text.toString();
	}
}
