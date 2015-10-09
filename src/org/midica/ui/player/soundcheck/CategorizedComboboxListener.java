/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.player.soundcheck;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;

import org.midica.config.CategorizedElement;
import org.midica.config.InstrumentElement;


/**
 * This Class defines a listener for comboboxes with categorized entries.
 * It makes sure that category entries can not be selected.
 * 
 * If the user tries to select a category entry than the previous or next
 * entry is selected instead. This enables the user to step through the
 * combobox with the arrow keys.
 */
public class CategorizedComboboxListener implements ActionListener {
	
	private JComboBox<InstrumentElement> combobox;
	private int                          lastIndex;
	
	/**
	 * Creates a combobox listener preventing that categories cannot be selected.
	 * 
	 * @param combobox  The combobox containing categorized entries.
	 */
	public CategorizedComboboxListener( JComboBox<InstrumentElement> combobox ) {
		this.combobox = combobox;
		
		// make sure that we don't start with a selected category
		actionPerformed( null );
	}
	
	/**
	 * Checks if the selected entry is a category; in this case selects one entry higher
	 * or lower.
	 */
	@Override
	public void actionPerformed( ActionEvent e ) {
		int                index   = combobox.getSelectedIndex();
		CategorizedElement element = (CategorizedElement) combobox.getSelectedItem();
		
		if ( element.isCategory() ) {
			// It's a category. Find out if we have to select one element up or down.
			int targetIndex = index;
			if ( index < lastIndex ) {
				// upwards selection
				targetIndex--;
				if ( targetIndex < 0 )
					targetIndex = index + 1;
			}
			else
				// downwards selection
				targetIndex++;
			
			combobox.setSelectedIndex( targetIndex );
			lastIndex = targetIndex;
		}
		else {
			// It's a normal element. Store the index.
			lastIndex = index;
		}
	}
}
