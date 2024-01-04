/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file.read;

/**
 * Objects of this class represent a MidicaPL command option.
 * It can be:
 * 
 * - a channel option
 * - an option for a CALL command
 * - an outer option for a pattern call command
 * - an inner option for a pattern line
 * - an option for an opening or closing brace of a nestable block
 * - an option in a compact syntax string
 * 
 * @author Jan Trukenmüller
 */
public class CommandOption {
	
	private String name     = null; // unified name
	private Object value    = null;
	private String rawName  = null; // name according to the currently configured syntax
	private String rawValue = null; // value as a MidicaPL string
	
	/**
	 * Initializes the option with a name and a value.
	 * 
	 * @param name      Option name in a canonized form, independent from the current syntax configuration.
	 * @param value     Option value.
	 * @param rawName   Option name according to the current syntax configuration.
	 * @param rawValue  Option value as a string, according to the current MidicaPL configuration.
	 * @throws ParseException if the given name is invalid.
	 */
	public void set(String name, Object value, String rawName, String rawValue) throws ParseException {
		this.name     = name;
		this.rawName  = rawName;
		this.rawValue = rawValue;
		if (MidicaPLParser.OPT_MULTIPLE.equals(name)) {
			this.value = (Boolean) value;
		}
		else if (MidicaPLParser.OPT_LENGTH.equals(name)) {
			this.value = (String) value;
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
		else if (MidicaPLParser.OPT_IF.equals(name)) {
			this.value = (String) value;
		}
		else if (MidicaPLParser.OPT_ELSIF.equals(name)) {
			this.value = (String) value;
		}
		else if (MidicaPLParser.OPT_ELSE.equals(name)) {
			this.value = (Boolean) value;
		}
		else {
			// should never happen
			throw new FatalParseException("Invalid option name: " + name + ".");
		}
	}
	
	/**
	 * Returns the raw name, like it is used in MidicaPL syntax.
	 * @return the raw name.
	 */
	public String getRawName() {
		return rawName;
	}
	
	/**
	 * Returns the (unified) option name.
	 * @return option name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the string representation of the value, if avaliable, or **null** otherwise.
	 * 
	 * @return the string representation of the value or **null**.
	 */
	public String getRawValue() {
		return rawValue;
	}
	
	/**
	 * Returns the quantity.
	 * @return quantity.
	 */
	public Integer getQuantity() {
		return (Integer) value;
	}
	
	/**
	 * Returns the note length.
	 * @return note length.
	 */
	public String getLength() {
		return (String) value;
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
	
	/**
	 * Returns a condition for an if or elsif option.
	 * @return condition.
	 */
	public String getCondition() {
		return (String) value;
	}
}
