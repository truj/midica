/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.model;

/**
 * This interface represents a certain type of MIDI message.
 * It can be implemented by classes representing either a single
 * MIDI message or a certain type of messages.
 * 
 * It's implemented by the following classes:
 * 
 * - {@link MessageTreeNode} -- represents a type of MIDI messages
 * - {@link SingleMessage}   -- represents a certain MIDI message
 * 
 * The implementing classes provide different kinds of options. Each option
 * has a certain (arbitrary) ID and a value. Depending on the kind of
 * option, the value can be a single value, a list of values or a range
 * of values.
 * 
 * If the class represents a certain MIDI message, the value is always
 * a single value and never a list or range.
 * 
 * Jan Trukenmüller
 */
public interface IMessageType {
	
	public static final int OPT_TICK          = 1;
	public static final int OPT_TRACK         = 2;
	public static final int OPT_MSG_NUM       = 3;
	public static final int OPT_LEAF_NODE     = 4;
	public static final int OPT_STATUS_BYTE   = 5;
	public static final int OPT_META_TYPE     = 6;
	public static final int OPT_VENDOR_ID     = 7;
	public static final int OPT_VENDOR_NAME   = 8;
	public static final int OPT_SUB_ID_1      = 9;
	public static final int OPT_SUB_ID_2      = 10;
	public static final int OPT_CONTROLLER    = 11;
	public static final int OPT_MESSAGE       = 12;
	public static final int OPT_CHANNEL       = 13;
	public static final int OPT_LENGTH        = 14;
	public static final int OPT_SYSEX_CHANNEL = 15;
	public static final int OPT_RPN           = 16;
	public static final int OPT_NRPN          = 17;
	public static final int OPT_TEXT          = 18;
	public static final int OPT_TEMPO_MPQ     = 19;
	public static final int OPT_TEMPO_BPM     = 20;
	
	/** Contains all option IDs in the right order. */
	public static final int[] OPTIONS = {
		OPT_TICK,
		OPT_TRACK,
	};
	
	/**
	 * Returns the value of a single option.
	 * 
	 * @param id  The option ID.
	 * @return the option value or **null** if not available.
	 */
	public Object getOption( int id );
	
	/**
	 * Returns the given option's value, range or **null**.
	 * 
	 * - Returns **null**, if an option with this ID is not available.
	 * - Returns a single value, if the class represents only one message,
	 *   or if the minimum and maximum values are identical.
	 * - Otherwise, returns a value range.
	 * 
	 * @param id  The option ID.
	 * @return value, range or **null**, as described above.
	 */
	public String getRange( int id );
	
	/**
	 * Returns the given option's value, list or **null**.
	 * 
	 * - Returns **null**, if an option with this ID is not available.
	 * - Returns a single value, if the class represents only one message,
	 *   or if only one value with this ID exists.
	 * - Otherwise, returns a value list.
	 * 
	 * Same as {@link #getDistinctOptions(int, String)} using "**, **" as
	 * the separator.
	 * 
	 * @param id  The option ID.
	 * @return value, list or **null**, as described above.
	 */
	public String getDistinctOptions( int id );
	
	/**
	 * Returns the given option's value, list or **null**.
	 * 
	 * - Returns **null**, if an option with this ID is not available.
	 * - Returns a single value, if the class represents only one message,
	 *   or if only one value with this ID exists.
	 * - Otherwise, returns a value list.
	 * 
	 * @param id         The option ID.
	 * @param separator  The string to be used in order to separate the values.
	 * @return value, list or **null**, as described above.
	 */
	public String getDistinctOptions( int id, String separator );
}
