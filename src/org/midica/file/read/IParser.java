/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file.read;

import java.io.File;

/**
 * This interface can be implemented by parser classes which can
 * parse an input file.
 * 
 * It's implemented by the following classes:
 * 
 * - {@link SoundfontParser} -- loads a soundfont file (.sf2)
 * - {@link SequenceParser} -- parses a MIDI stream using one of the following sub classes:
 *     - {@link MidiParser} -- parses the stream from a MIDI file (.mid)
 *     - {@link MidicaPLParser} -- parses the stream from a MidicaPL file (.midica)
 * 
 * @author Jan Trukenmüller
 */
public interface IParser {
	
	/**
	 * Parses an input file.
	 * 
	 * @param file             Input file written in a format that the derived
	 *                         parser class can parse.
	 * @throws ParseException  If the file can not be parsed correctly.
	 */
	public void parse( File file ) throws ParseException;
	
}
