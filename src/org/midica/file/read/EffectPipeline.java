/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file.read;

/**
 * This class represents a sound effect pipeline.
 * It is mainly responsible to track the state of the pipeline.
 * 
 * @author Jan Trukenmüller
 */
public class EffectPipeline {
	
	public final int channel;
	
	private long tick;
	private long ticksPerAction;
	
	/**
	 * Creates a new sound effect pipeline.
	 * 
	 * @param channel    MIDI channel
	 * @param lengthStr  initial length string
	 * @throws ParseException
	 */
	public EffectPipeline(int channel, String lengthStr) throws ParseException {
		this.channel        = channel;
		this.tick           = MidicaPLParser.instruments.get(channel).getCurrentTicks();
		this.ticksPerAction = MidicaPLParser.parseDuration(lengthStr);
	}
	
	/**
	 * Applies a **length(...)** function call in the pipeline.
	 * 
	 * @param lengthStr  The (note length) parameter of the length() call.
	 * @throws ParseException if the length string is an invalid note lengh.
	 */
	public void applyLength(String lengthStr) throws ParseException {
		ticksPerAction = MidicaPLParser.parseDuration(lengthStr);
	}
	
	/**
	 * Applies a **wait()** function call in the pipeline.
	 */
	public void applyWait() {
		tick += ticksPerAction;
	}
}
