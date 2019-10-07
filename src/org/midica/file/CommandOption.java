/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file;

/**
 * Objects of this class represent a MidicaPL command option.
 * It can be:
 * 
 * - a channel option
 * - an option for n CALL command
 * - an option for an opening or closing brace of a nestable block
 * 
 * @author Jan Trukenmüller
 */
public class CommandOption {
	
	private String name  = null;
	private Object value = null;
	
	/**
	 * Initializes the option with a name and a value.
	 * 
	 * @param name     Option name.
	 * @param value    Option value.
	 * @throws ParseException if the given name is invalid.
	 */
	public void set(String name, Object value) throws ParseException {
		this.name = name;
		if (MidicaPLParser.OPT_MULTIPLE.equals(name)) {
			this.value = (Boolean) value;
		}
		else if (MidicaPLParser.OPT_VELOCITY.equals(name)) {
			this.value = (Integer) value;
		}
		else if (MidicaPLParser.OPT_QUANTITY.equals(name)) {
			this.value = (Integer) value;
		}
		else if (MidicaPLParser.OPT_DURATION.equals(name)) {
			this.value = (Float) value;
		}
		else if (MidicaPLParser.OPT_LYRICS.equals(name)) {
			this.value = (String) value;
		}
		else if (MidicaPLParser.OPT_TUPLET.equals(name)) {
			this.value = (String) value;
		}
		else if (MidicaPLParser.OPT_TREMOLO.equals(name)) {
			this.value = (Integer) value;
		}
		else if (MidicaPLParser.OPT_SHIFT.equals(name)) {
			this.value = (Integer) value;
		}
		else {
			// should never happen
			throw new ParseException("Invalid option name: " + name + ". Please report.");
		}
	}
	
	/**
	 * Returns the option name.
	 * @return option name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the quantity.
	 * @return quantity.
	 */
	public Integer getQuantity() {
		return (Integer) value;
	}
	
	/**
	 * Returns the velocity.
	 * @return velocity.
	 */
	public Integer getVelocity() {
		return (Integer) value;
	}
	
	/**
	 * Returns the duration.
	 * @return duration.
	 */
	public Float getDuration() {
		return (Float) value;
	}
	
	/**
	 * Returns a lyrics syllable.
	 * @return lyrics syllable.
	 */
	public String getLyrics() {
		return (String) value;
	}
	
	/**
	 * Returns a tuplet modifier.
	 * @return tuplet modifier.
	 */
	public String getTuplet() {
		return (String) value;
	}
	
	/**
	 * Returns a tremolo modifier.
	 * @return tremolo modifier.
	 */
	public int getTremolo() {
		return (Integer) value;
	}
	
	/**
	 * Returns a shift modifier.
	 * @return shift modifier.
	 */
	public int getShift() {
		return (Integer) value;
	}
}
