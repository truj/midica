/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.model;

import java.util.ArrayList;

/**
* This class represents String options of a combobox.
* 
* Each option is a combination of:
* 
* - a language-dependent display text string; and
* - an according language-independent identifier string that is the actual value
*   that is chosen by the combobox option.
* 
* So far this class is used only for comboboxes containing configuration options.
* 
* @author Jan Trukenmüller
*/
public class ComboboxStringOption {
	
	private String identifier = null;
	private String text       = null;
	
	/**
	 * Creates a new combobox option.
	 * 
	 * @param identifier    The value that is actually chosen by selecting this combobox option.
	 * @param text          The displayed language-dependent text.
	 */
	public ComboboxStringOption( String identifier, String text ) {
		this.identifier = identifier;
		setText( text );
	}
	
	/**
	 * Returns the identifier of the combobox.
	 * 
	 * @return    Identifier.
	 */
	public String getIdentifier() {
		return identifier;
	}
	
	/**
	 * Sets the displayed, language-dependent text.
	 * 
	 * @param text    Displayed text.
	 */
	public void setText( String text ) {
		this.text = text;
	}
	
	/**
	 * Returns the displayed text.
	 * 
	 * @return    The displayed text.
	 */
	public String toString() {
		return text;
	}
	
	/**
	 * Finds and returns the element of the given **options** with the given **identifier**.
	 * 
	 * @param identifier    Combobox value.
	 * @param options       List of all available combobox options.
	 * @return    The member of the given options with the given identifier.
	 */
	public static ComboboxStringOption getOptionById( String identifier, ArrayList<ComboboxStringOption> options ) {
		for ( ComboboxStringOption option : options ) {
			if ( identifier.equals(option.getIdentifier()) ) {
				return option;
			}
		}
		return null;
	}
}
