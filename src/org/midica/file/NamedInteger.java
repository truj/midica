/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file;

import java.util.Collection;

/**
 * This class is used to provide integers, associated with a name string.
 * This can be used for example for combobox entries.
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
	 * Returns the name.
	 * This is needed if this class is used for combobox entries.
	 */
	public String toString() {
		return value + " : " + name;
	}
	
	/**
	 * Returns the {@link NamedInteger} element of the given list with the given value.
	 * 
	 * @param number    The value to be found.
	 * @param list      The list of elements to be searched for the given value.
	 * @return          The requested element from the given list.
	 */
	public static NamedInteger getElementByNumber( int number, Collection<NamedInteger> list ) {
		for ( NamedInteger element : list ) {
			if ( number == element.value ) {
				return element;
			}
		}
		return null;
	}
}
