/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.sorter;

/**
 * This class provides table row sorting for the bank column in the soundcheck's instrument table.
 * 
 * The cell content is a string consisting of MSB, "/", and LSB.
 * But the sort criterion shall be the full bank number.
 * 
 * The row may also contain an empty string, if it belongs to a category or sub category.
 * 
 * @author Jan Trukenmüller
 */
public class BankNumber implements Comparable<BankNumber> {
	
	public  Integer bankNumber;
	private String  display;
	
	/**
	 * Creates a comparable (sortable) wrapper object for a table cell with a bank number or category.
	 * 
	 * @param bankNumber  The full bank number, or (in case of a category) just any number.
	 * @param display     The cell content to be displayed.
	 */
	public BankNumber(Integer bankNumber, String display) {
		this.bankNumber = bankNumber;
		this.display    = display;
	}
	
	@Override
	public int compareTo(BankNumber o) {
		return bankNumber.compareTo( o.bankNumber );
	}
	
	/**
	 * Returns the value to displayed in the table cell.
	 */
	@Override
	public String toString() {
		return display;
	}
}
