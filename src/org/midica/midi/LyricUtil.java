/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.midi;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.midica.file.write.MidicaPLExporter;
import org.midica.ui.widget.FlowLabel;

/**
 * This class provides methods to deal with Lyrics events, especially according to RP-026.
 * 
 * @author Jan Trukenmüller
 */
public final class LyricUtil {

	/** used for the software and version meta info */
	public static final String SOFTWARE = "software";
	
	/** Singleton object of this class */
	private static LyricUtil instance = null;
	
	// patterns for replacements
	// forward: escape sequence --> replacement
	// reverse: replacement --> escape sequence
	private static final ArrayList<Pattern> forwardPatterns     = new ArrayList<Pattern>();
	private static final ArrayList<String>  forwardReplacements = new ArrayList<String>();
	private static final ArrayList<Pattern> reversePatterns     = new ArrayList<Pattern>();
	private static final ArrayList<String>  reverseReplacements = new ArrayList<String>();
	private static final ArrayList<String>  reverseSpecialChars = new ArrayList<String>();
	
	// find all tags like {@...} or {#...=...}
	private static final Pattern tagPattern = Pattern.compile( "\\{[#@].*?\\}" );
	
	// find song info tags like {#...=...}
	private static final Pattern pattSongInfo = Pattern.compile( "\\{#\\s*(.+?)\\s*=\\s*(.+?)\\s*\\}" );
	
	// escape \, [, ], {, }, tab, cr, lf
	private static final ArrayList<Pattern> escapePatterns     = new ArrayList<Pattern>();
	private static final ArrayList<String>  escapeReplacements = new ArrayList<String>();
	
	// unescape \r, \n, \t
	private static final Pattern pattCR  = Pattern.compile( Pattern.quote("\\r")  );
	private static final Pattern pattLF  = Pattern.compile( Pattern.quote("\\n")  );
	private static final Pattern pattTab = Pattern.compile( Pattern.quote("\\t")  );
	
	/**
	 * Private constructor, precompiling needed regex patterns.
	 */
	private LyricUtil() {
		
		// The escaped strings we need to replace and then restore again later.
		String[] escapedStrings = {
			"\\\\", // \\
			"\\{",  // \{
			"\\}",  // \}
			"\\[",  // \[
			"\\]",  // \]
		};
		
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			for (String escaped : escapedStrings) {
				BigInteger bigInt      = new BigInteger( 1, md.digest(escaped.getBytes("ISO-8859-1")) );
				String     placeholder = bigInt.toString(16);
				Pattern    pattern     = Pattern.compile( Pattern.quote(escaped) );
				forwardReplacements.add(placeholder);
				forwardPatterns.add(pattern);
				pattern = Pattern.compile(placeholder);
				reversePatterns.add(pattern);
				reverseReplacements.add( Matcher.quoteReplacement(escaped) );
				String specialChar = escaped.substring(1);
				reverseSpecialChars.add( Matcher.quoteReplacement(specialChar) );
			}
			
			// \, [, ], {, }, tab, cr, lf
			escapePatterns.add( Pattern.compile(Pattern.quote("\\")) );
			escapePatterns.add( Pattern.compile(Pattern.quote("["))  );
			escapePatterns.add( Pattern.compile(Pattern.quote("]"))  );
			escapePatterns.add( Pattern.compile(Pattern.quote("{"))  );
			escapePatterns.add( Pattern.compile(Pattern.quote("}"))  );
			escapePatterns.add( Pattern.compile(Pattern.quote("\t")) );
			escapePatterns.add( Pattern.compile(Pattern.quote("\r")) );
			escapePatterns.add( Pattern.compile(Pattern.quote("\n")) );
			escapeReplacements.add( Matcher.quoteReplacement("\\\\") );
			escapeReplacements.add( Matcher.quoteReplacement("\\[") );
			escapeReplacements.add( Matcher.quoteReplacement("\\]") );
			escapeReplacements.add( Matcher.quoteReplacement("\\{") );
			escapeReplacements.add( Matcher.quoteReplacement("\\}") );
			escapeReplacements.add( Matcher.quoteReplacement("\\t") );
			escapeReplacements.add( Matcher.quoteReplacement("\\r") );
			escapeReplacements.add( Matcher.quoteReplacement("\\n") );
		}
		catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates and returns a singleton object of this class.
	 * 
	 * @return a singleton object of this class.
	 */
	public static final LyricUtil getInstance() {
		if (null == instance) {
			instance = new LyricUtil();
		}
		return instance;
	}
	
	/**
	 * Searches for song info tags in the given text.
	 * The text is assumed to be the text part of a lyrics meta event.
	 * Such tags look like {#...=...}.
	 * 
	 * @param text    The text to search.
	 * @return a list of found info keys and their according values.
	 */
	public final HashMap<String, String> getSongInfo(String text) {
		
		// no tag at all?
		if (-1 == text.indexOf('{')) {
			return null;
		}
		
		text = encodeHashes(text);
		
		HashMap<String, String> info = new HashMap<String, String>();
		Matcher m = pattSongInfo.matcher(text);
		while (m.find()) {
			String key   = m.group( 1 ).toLowerCase();
			String value = m.group( 2 );
			if ("lyrics".equals(key)) {
				key = "lyricist";
			}
			
			if ( "title".equals(key)
			  || "composer".equals(key)
			  || "lyricist".equals(key)
			  || "artist".equals(key)
			  || SOFTWARE.equals(key) ) {
				
				value = unescapeSpecialWhitespaces(value);
				value = hashesToSpecial(value);
				
				if (info.containsKey(key)) {
					String oldValue = info.get(key);
					info.put(key, oldValue + "\\n" + value);
				}
				else {
					info.put(key, value);
				}
			}
		}
		
		return info;
	}
	
	/**
	 * Escapes special characters of the given string according to RP-026.
	 * 
	 * @param text    The text to be escaped.
	 * @return the escaped text.
	 */
	public final String escape(String text) {
		for (int i = 0; i < escapePatterns.size(); i++) {
			text = escapePatterns.get(i).matcher(text).replaceAll( escapeReplacements.get(i) );
		}
		return text;
	}
	
	/**
	 * Unescapes escaped special whitespace characters in lyrics events.
	 * Replaces \r, \n and \t.
	 * 
	 * @param text    The text to unescape.
	 * @return the unescaped text.
	 */
	public final String unescapeSpecialWhitespaces(String text) {
		
		text = pattCR.matcher(text).replaceAll("\r");
		text = pattLF.matcher(text).replaceAll("\n");
		text = pattTab.matcher(text).replaceAll("\t");
		
		return text;
	}
	
	/**
	 * Removes RP-026 tags from the given lyrics text.
	 * 
	 * @param text    The lyrics text.
	 * @return the cleaned text.
	 */
	public final String removeTags(String text) {
		
		// no tag at all?
		if (-1 == text.indexOf('{')) {
			return text;
		}
		
		text = encodeHashes(text);
		text = tagPattern.matcher(text).replaceAll("");
		text = decodeHashes(text);
		
		return text;
	}
	
	/**
	 * Unifies different kinds of newlines in the given text.
	 * Transforms them all to \n.
	 * 
	 * - CRLF ==> LF
	 * - LF ==> LF
	 * - CR ==> LF
	 * 
	 * This is needed by the {@link FlowLabel} in order to display text from MIDI files.
	 * It's also needed by the {@link MidicaPLExporter} to correctly interpret Meta messages.
	 * 
	 * @param text  the original text
	 * @return the unified text
	 */
	public final String unifyNewlinesToLf(String text) {
		
		if (null == text)
			return text;
		
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			
			// create placeholders
			String cr   = new BigInteger(1, md.digest("\r".getBytes("ISO-8859-1"))).toString(16);
			String lf   = new BigInteger(1, md.digest("\n".getBytes("ISO-8859-1"))).toString(16);
			String crlf = new BigInteger(1, md.digest("\r\n".getBytes("ISO-8859-1"))).toString(16);
			
			// replace every possible cr-lf combination
			text = text
				.replace("\r\n", crlf)
				.replace("\n",   lf)
				.replace("\r",   cr);
			
			// convert \r\n to \n
			text = text
				.replace(crlf, lf)
				.replace(cr, lf);
			
			// convert remaining placeholder back to \n
			return text.replace(lf, "\n");
		}
		catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return text;
	}
	
	/**
	 * Replaces escaped characters with their hash values
	 * so that they don't disturb regexes.
	 * 
	 * @param text    The text to be encoded.
	 * @return the encoded text.
	 */
	private final String encodeHashes(String text) {
		for (int i = 0; i < forwardPatterns.size(); i++) {
			text = forwardPatterns.get(i).matcher(text).replaceAll( forwardReplacements.get(i) );
		}
		return text;
	}
	
	/**
	 * Replaces hash values by their original escaped characters.
	 * 
	 * @param text    The text to be decoded.
	 * @return the decoded text.
	 */
	private final String decodeHashes(String text) {
		for (int i = 0; i < reversePatterns.size(); i++) {
			text = reversePatterns.get(i).matcher(text).replaceAll( reverseReplacements.get(i) );
		}
		return text;
	}
	
	/**
	 * Replaces hash values of escaped characters by the according special characters.
	 * 
	 * @param text    The text to be decoded.
	 * @return the decoded text.
	 */
	private final String hashesToSpecial(String text) {
		for (int i = 0; i < reversePatterns.size(); i++) {
			text = reversePatterns.get(i).matcher(text).replaceAll( reverseSpecialChars.get(i) );
		}
		return text;
	}
}
