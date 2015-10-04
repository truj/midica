/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.midi;

import org.midica.config.Dict;

/**
 * This exception is thrown if a MIDI sequence is not yet set but needs to be set.
 * 
 * @author Jan Trukenmüller
 */
public class SequenceNotSetException extends Exception {
	
	/**
	 * Creates a new exception because a MIDI sequence is not yet set.
	 */
	public SequenceNotSetException() {
		super( Dict.get(Dict.ERROR_SEQUENCE_NOT_SET) );
	}
}
