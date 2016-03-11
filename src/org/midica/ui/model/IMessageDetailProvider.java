/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.model;

/**
 * This interface can be implemented by classes representing either a single
 * MIDI message or a certain type of messages.
 * 
 * It's implemented by the following classes:
 * 
 * - {@link MidicaTreeNode} -- represents a type of MIDI messages
 * - {@link MessageDetail}  -- represents a certain MIDI message
 * 
 * The implementing classes provide different kinds of options. Each option
 * has a certain (arbitrary) name and a value. Depending on the kind of
 * option, the value can be a single value, a list of values or a range
 * of values.
 * 
 * If the class represents a certain MIDI message, the value is always
 * a single value and never a list or range.
 * 
 * Jan Trukenmüller
 */
public interface IMessageDetailProvider {
	
	/**
	 * Returns the value of a single option.
	 * 
	 * @param name  The option name.
	 * @return the option value or **null** if not available.
	 */
	public Object getOption( String name );
	
	/**
	 * Returns the given option's value, range or **null**.
	 * 
	 * - Returns **null**, if an option with this name is not available.
	 * - Returns a single value, if the class represents only one message,
	 *   or if the minimum and maximum values are identical.
	 * - Otherwise, returns a value range.
	 * 
	 * @param name  The option name.
	 * @return value, range or **null**, as described above.
	 */
	public String getRange( String name );
	
	/**
	 * Returns the given option's value, list or **null**.
	 * 
	 * - Returns **null**, if an option with this name is not available.
	 * - Returns a single value, if the class represents only one message,
	 *   or if only one value with this name exists.
	 * - Otherwise, returns a value list.
	 * 
	 * Same as {@link #getDistinctOptions(String, String)} using "**, **" as
	 * the separator.
	 * 
	 * @param name  The option name.
	 * @return value, list or **null**, as described above.
	 */
	public String getDistinctOptions( String name );
	
	/**
	 * Returns the given option's value, list or **null**.
	 * 
	 * - Returns **null**, if an option with this name is not available.
	 * - Returns a single value, if the class represents only one message,
	 *   or if only one value with this name exists.
	 * - Otherwise, returns a value list.
	 * 
	 * @param name       The option name.
	 * @param separator  The string to be used in order to separate the values.
	 * @return value, list or **null**, as described above.
	 */
	public String getDistinctOptions( String name, String separator );
}
