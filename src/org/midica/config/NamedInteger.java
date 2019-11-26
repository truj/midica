/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.config;

/**
 * This class is used to provide integers, associated with a name string.
 * This can be used for combobox or list entries.
 * 
 * @author Jan Trukenmüller
 */
public class NamedInteger {
	
	/** Name of the integer */
	public String name;
	
	/** Value of the named integer */
	public int    value;
	
	/**
	 * Creates a new object without initializing name or integer.
	 * So the initialization has to be done from outside.
	 */
	public NamedInteger() {
	}
	
	/**
	 * Creates a new object and initializes name and value of the integer.
	 * 
	 * @param name     Name of the integer.
	 * @param value    Value of the integer.
	 */
	public NamedInteger( String name, int value ) {
		this.name  = name;
		this.value = value;
	}
	
	/**
	 * Returns a descriptive string containing number and name.
	 * This is needed if this class is used for combobox or list entries.
	 */
	public String toString() {
		
		// \u00a0 is a non-breaking space.
		// reason: avoid key binding conflicts in the soundcheck view
		// (list is focused and number is pressed)
		return "\u00a0" + value + " : " + name;
	}
}
