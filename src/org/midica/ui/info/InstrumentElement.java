/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.info;


/**
 * Objects of this class represent either an instrument or a category of instruments.
 * 
 * @author Jan Trukenmüller
 */
public class InstrumentElement extends CategorizedElement {
	
	/** Instrument number as defined by the MIDI specification (or -1 if it's a category) */
	public int instrNum;
	/** configured instrument name */
	public String name;
	
	/**
	 * Creates a new instrument element or instrument category with the given parameters
	 * as it's properties.
	 * 
	 * @param instrNum  instrument number as defined by the MIDI specification (or -1 if it's a category)
	 * @param name      configured instrument name
	 * @param category  true if a category shall be created -- false if it's a normal instrument
	 */
	public InstrumentElement( int instrNum, String name, boolean category ) {
		this.instrNum  = instrNum;
		this.name      = name;
		super.category = category;
	}
	
	/**
	 * Used for combobox options
	 */
	public String toString() {
		if ( category )
			return name;
		return instrNum + " : " + name;
	}
}
