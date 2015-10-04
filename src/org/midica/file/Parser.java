/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file;

import java.io.File;

import org.midica.config.Dict;

/**
 * This class can be extended by parser classes which parse an input file.
 * 
 * The derived classes {@link MidiParser} and {@link MidicaPLParser} create a MIDI string
 * from the parsed file(s).
 * 
 * @author Jan Trukenmüller
 */
public abstract class Parser {
	
	/**
	 * Defines how much the parsed input has to be transposed.
	 * A positive transpose level causes a transposition into higher pitches.
	 * A negative level transposes into lower pitches.
	 */
	protected static byte transposeLevel = 0;
	
	/**
	 * Returns the transpose level. This is the value which defines how much the
	 * input has to be transposed before creating the MIDI stream.
	 * 
	 * @return transpose level
	 */
	public static byte getTransposeLevel() {
		return transposeLevel;
	}
	
	/**
	 * Sets the transpose level. That defines how much the parsed input is
	 * transposed.
	 * 
	 * - Positive level: transposition into a higher pitch
	 * - Negative level: transposition into a lower pitch
	 * 
	 * @param level Transpose level
	 */
	public static void setTransposeLevel( byte level ) {
		transposeLevel = level;
	}
	
	/**
	 * Transposes the given note into another pitch according to the current
	 * {@link #transposeLevel}.
	 * Does not transpose anything on channel 9 because that is the percussion channel and
	 * transposing percussion instrument transposition is not supported.
	 * 
	 * @param note             Input note value as defined by the MIDI specification (0-127)
	 * @param channel          Channel number as defined by the MIDI specification (0-15)
	 * @return                 Target note after the transposition.
	 * @throws ParseException  If the transposition would cause an invalid target note with
	 *                         a value lower than 0 or higher than 127.
	 */
	protected int transpose( int note, int channel ) throws ParseException {
		
		// don't transpose percussions
		if ( 9 == channel )
			return note;
		
		note += transposeLevel;
		if ( note < 0 )
			throw new ParseException( Dict.get(Dict.ERROR_NOTE_TOO_SMALL) + note );
		if ( note > 127 )
			throw new ParseException( Dict.get(Dict.ERROR_NOTE_TOO_BIG) + note );
		
		return note;
	}

	/**
	 * Parses an input file.
	 * 
	 * @param file             Input file written in a format that the derived parser class
	 *                         can parse.
	 * @throws ParseException  If the file can not be parsed correctly.
	 */
	public abstract void parse( File file ) throws ParseException;
}
