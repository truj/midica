/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file;

import java.util.HashMap;

import org.midica.config.Dict;
import org.midica.midi.MidiDevices;
import org.midica.midi.SequenceCreator;
import org.midica.ui.info.InfoView;

/**
 * This class can be extended by specialized parser classes
 * which parse an input file and create a MIDI stream.
 * 
 * Derived classes are:
 * 
 * - {@link MidiParser} (parses a MIDI file; extension: .mid)
 * - {@link MidicaPLParser} (parses a MidicaPL file; extension: .midica)
 * 
 * @author Jan Trukenmüller
 */
public abstract class SequenceParser implements IParser {
	
	/**
	 * Contains information about the currently loaded MIDI stream - no matter where
	 * it has been loaded from.
	 */
	public static HashMap<String, Object> streamInfo = new HashMap<String, Object>();
	
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
	 * Postprocesses the loaded MIDI stream.
	 * 
	 * The following steps are included:
	 * 
	 * - Collecting some last informations to be shown in the {@link InfoView}.
	 * - Making the stream available for the player.
	 * 
	 * 
	 * @param type "mid" or "midica", depending on the parser class.
	 */
	protected void postprocessMidiStream( String type ) {
		
		// collect some last informations of the created stream
		SequenceCreator.postprocess( type );
		
		// publish the stream
		MidiDevices.setSequence( SequenceCreator.getSequence() );
	}

}
