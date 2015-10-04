/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file;

import java.io.File;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Soundbank;

import org.midica.midi.MidiDevices;

/**
 * This class is used in order to load a user-defined soundbank file.
 * Users who want to stick with the standard soundfonts of the JVM do not need it.
 * 
 * @author Jan Trukenmüller
 */
public class SoundbankParser extends Parser {
	
	/** The currently loaded user-defined soundbank. */
	private Soundbank soundbank = null;
	
	/**
	 * Parses a soundbank file.
	 * 
	 * @param file             Soundbank file chosen by the user.
	 * @throws ParseException  If the file can not be loaded correctly.
	 */
	public void parse( File file ) throws ParseException{
		try {
			soundbank = MidiSystem.getSoundbank( file );
			MidiDevices.setSoundbank( soundbank );
		}
		catch ( InvalidMidiDataException e ) {
			throw new ParseException( e.getMessage() );
		}
		catch ( IOException e ) {
			throw new ParseException( e.getMessage() );
		}
	}
}
