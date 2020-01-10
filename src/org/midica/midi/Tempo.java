/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.midi;

import javax.sound.midi.MetaMessage;

/**
 * Some tempo related static methods.
 * 
 * @author Jan Trukenmüller
 */
public final class Tempo {
	
	/** microseconds per minute */
	private static final int msPerMin = 60 * 1000 * 1000;
	
	/**
	 * Allow only static methods.
	 */
	private Tempo() {
	}
	
	/**
	 * Parses a tempo change message and returns the value in BPM.
	 * 
	 * @param msg  tempo change message
	 * @return tempo in BPM
	 */
	public static final int getBpm(MetaMessage msg) {
		byte[] content = msg.getData();
		
		// message long enough?
		if (content.length < 3) {
			return -1;
		}
		
		int mpq = (content[2] & 0xFF) | ((content[1] & 0xFF) << 8) | ((content[0] & 0xFF) << 16);
		if (mpq > 0)
			return msPerMin / mpq;
		
		// fallback
		return -1;
	}
	
	/**
	 * Converts BPM (beats per minute) into MPQ (microseconds per quarter note).
	 * 
	 * @param tempo  the tempo in BPM
	 * @return tempo in MPQ
	 */
	public static final int bpmToMpq(int bpm) {
		if (bpm > 0)
			return msPerMin / bpm;
		
		// fallback
		return -1;
	}
}
