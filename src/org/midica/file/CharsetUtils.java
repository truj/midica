/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.SortedMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provides charset-dependant methods.
 * 
 * @author Jan Trukenmüller
 */
public final class CharsetUtils {
	
	private static final SortedMap<String, Charset> availableCharsets = Charset.availableCharsets();
	
	private static final Pattern pattSwitchCharset = Pattern.compile( "\\{@(.+?)\\}" );
	
	// special charsets, defined in SMF Recommended Practice RP-026
	private static final String CHARSET_LATIN = "ISO-8859-1";
	private static final String CHARSET_JP    = "Shift_JIS";
	
	/**
	 * Converts a byte array from a text-based MIDI message into a string.
	 * 
	 * For the conversion the following charsets are tried:
	 * 
	 * - 1st priority: The charset found in the byte order mark, if available.
	 * - 2nd priority: The charset defined in a previous MIDI message, if available.
	 * - 3rd priority: The charset chosen in the file chooser.
	 * - 4th priority: The platform default, if none of the above worked.
	 * 
	 * @param bytes          Byte array from the MIDI message.
	 * @param chosenCharset  Charset chosen in the file selector.
	 * @param fileCharset    Last charset declared in the sequence.
	 * @return the text.
	 */
	public static final String getTextFromBytes( byte[] bytes, String chosenCharset, String fileCharset ) {
		
		// First look for a BOM (byte order mark).
		String bomCharset = null;
		if ( bytes.length >= 4 ) {
			if ( bytes[0] == (byte) 0x00
			  && bytes[1] == (byte) 0x00
			  && bytes[2] == (byte) 0xFE
			  && bytes[3] == (byte) 0xFF ) {
				bomCharset = "UTF-32BE";
			}
			else if ( bytes[0] == (byte) 0xFF
			       && bytes[1] == (byte) 0xFE
			       && bytes[2] == (byte) 0x00
			       && bytes[3] == (byte) 0x00 ) {
				bomCharset = "UTF-32LE";
			}
			else if ( bytes[0] == (byte) 0x84
			       && bytes[1] == (byte) 0x31
			       && bytes[2] == (byte) 0x95
			       && bytes[3] == (byte) 0x33 ) {
				bomCharset = "GB18030";
			}
		}
		if ( null == bomCharset && bytes.length >= 3 ) {
			if ( bytes[0] == (byte) 0xFE
			  && bytes[1] == (byte) 0xBB
			  && bytes[2] == (byte) 0xBF ) {
				bomCharset = "UTF-8";
			}
		}
		if ( null == bomCharset && bytes.length >= 2 ) {
			if ( bytes[0] == (byte) 0xFE
			  && bytes[1] == (byte) 0xFF ) {
				bomCharset = "UTF-16BE";
			}
			else if ( bytes[0] == (byte) 0xFF
			       && bytes[1] == (byte) 0xFE ) {
				bomCharset = "UTF-16LE";
			}
		}
		
		// BOM found?
		if ( bomCharset != null ) {
			try {
				return new String( bytes, bomCharset );
			}
			catch ( UnsupportedEncodingException e ) {
			}
		}
		
		// Found a charset definition in the MIDI file itself?
		if ( fileCharset != null ) {
			try {
				return new String( bytes, fileCharset );
			}
			catch ( UnsupportedEncodingException e ) {
			}
		}
		
		// Then try the charset that has been chosen in the file selector.
		try {
			return new String( bytes, chosenCharset );
		}
		catch ( UnsupportedEncodingException e ) {
		}
		
		// Fall back to the platform standard.
		return new String( bytes );
	}
	
	/**
	 * Converts a text into a byte array using the given charset, if possible.
	 * 
	 * @param text     The text to be converted.
	 * @param charset  The charset to be used for converting.
	 * @return
	 */
	public static final byte[] getBytesFromText( String text, String charset ) {
		
		try {
			return text.getBytes( charset );
		}
		catch ( UnsupportedEncodingException e ) {
		}
		
		return text.getBytes();
	}
	
	/**
	 * Checks the given string for a new charset marker.
	 * If one or more such markers are found, returns the charset from the
	 * last tag. Otherwise: returns **null*.
	 * 
	 * 
	 * @param text  The text to be examinated.
	 * @return The new charset, if a charset change is found. Otherwise: **null**.
	 */
	public static final String findCharsetSwitch( String text ) {
		
		// pre-check for performance reasons
		if ( ! text.contains("{@" ) ) {
			return null;
		}
		
		// find charset definitions
		String  newCharset = null;
		Matcher m          = pattSwitchCharset.matcher( text );
		while ( m.find() ) {
			String cs = m.group( 1 );
			
			// special charsets, defined in SMF Recommended Practice RP-026
			if ( "LATIN".equals(cs.toUpperCase()) ) {
				cs = CHARSET_LATIN;
			}
			else if ( "JP".equals(cs.toUpperCase()) ) {
				cs = CHARSET_JP;
			}
			
			// charset valid?
			if ( availableCharsets.containsKey(cs) ) {
				newCharset = cs;
			}
		}
		
		return newCharset;
	}
}
