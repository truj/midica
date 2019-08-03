/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.tablesorter;

/**
 * This class provides table row sorting for columns that normally contain a number but
 * may also contain a string sometimes.
 * 
 * Strings that cannot be cast to a number will be sorted as if they had a negative value.
 * 
 * @author Jan Trukenmüller
 */
public class OptionalNumber implements Comparable<OptionalNumber> {
	
	public  Long   value;
	private String display;
	
	/**
	 * Creates a comparable (sortable) wrapper object for a table cell with an optional number.
	 * 
	 * @param value  The value to be displayed.
	 */
	public OptionalNumber(Object value) {
		if (value instanceof Integer) {
			this.value   = (long) (int) value;
			this.display = String.valueOf(value);
		}
		else if (value instanceof String) {
			try {
				this.display = (String) value;
				this.value   = Long.parseLong(display);
			}
			catch(NumberFormatException e) {
				this.value = -1L;
			}
		}
		else if (value instanceof Long) {
			this.value   = (Long) value;
			this.display = String.valueOf(value);
		}
		else {
			this.value   = -1L;
			this.display = "";
		}
	}
	
	@Override
	public int compareTo(OptionalNumber o) {
		return value.compareTo( o.value );
	}
	
	/**
	 * Returns the value to displayed in the table cell.
	 */
	@Override
	public String toString() {
		return display;
	}
}
