/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file.read;

import javax.sound.midi.InvalidMidiDataException;

/**
 * Exceptions of this class can be thrown if a really unexpected error occurs while parsing a file.
 * 
 * This is used for exceptions caused by problems in the Midica source code itself.
 * 
 * @author Jan Trukenmüller
 */
public class FatalParseException extends ParseException {
	
	private static final long serialVersionUID = 1L;
	
	private static final String suffix = "<br>This should never happen. Please file a bug report.";
	
	/**
	 * Throws an exception including a detail message.
	 * 
	 * @param message
	 */
	public FatalParseException(String message) {
		super(message + suffix);
	}
	
	/**
	 * Throws an exception caused by an InvalidMidiDataException.
	 * @param e
	 */
	public FatalParseException(InvalidMidiDataException e) {
		super("Invalid MIDI data: " + e.getMessage() + suffix);
	}
}
