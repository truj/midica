/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.player.soundcheck;

import java.util.ArrayList;
import java.util.Collections;

import javax.swing.DefaultComboBoxModel;

import org.midica.config.Dict;
import org.midica.file.NamedInteger;

/**
 * This class provides the model for the note/percussion combobox in the
 * soundcheck window.
 * 
 * @author Jan Trukenmüller
 */
public class SoundcheckNoteModel extends DefaultComboBoxModel {
	
	private static final long serialVersionUID = 1L;
    
	public static final int DEFAULT_NOTE       = 48;
	public static final int DEFAULT_PERCUSSION = 38;
	
	/** determins if a note or percussion list has to be displayed */
	private boolean percussion           = false;
	private int     lastChosenNote       = DEFAULT_NOTE;
	private int     lastChosenPercussion = DEFAULT_PERCUSSION;
	
	private ArrayList<NamedInteger> list = null;
	
	/**
	 * Creates a model for the note/percussion combobox.
	 */
	public SoundcheckNoteModel() {
		init();
	}
	
	/**
	 * Switches the display mode to display either **notes** or
	 * **percussion instruments**.
	 * 
	 * In other words: sets the {@link #percussion} field.
	 * 
	 * @param newPercussion  **true** to display percussion instruments, **false** to display notes.
	 */
	public void setPercussion( boolean newPercussion ) {
		
		// remember the old selection depending on the old percussion flag
		int oldValue = ( (NamedInteger) getSelectedItem() ).value;
		if ( percussion )
			lastChosenPercussion = oldValue;
		else
			lastChosenNote = oldValue;
		
		// set the new percussion value
		percussion = newPercussion;
	}
	
	/**
	 * (Re)fills the note/percussion combobox with either notes or
	 * percussion instruments - depending on the {@link #percussion} field.
	 */
	public void init() {
		removeAllElements();
		list = new ArrayList<NamedInteger>();
		
		if (percussion) {
			// construct percussion options
			
			// get all keys in a sorted list
			ArrayList<Integer> sortedPercussion = new ArrayList<Integer>();
			for ( int key : Dict.getPercussionNotes() ) {
				sortedPercussion.add( key );
			}
			Collections.sort( sortedPercussion );
			
			// construct and add each option
			for ( int key : sortedPercussion ) {
				String name = Dict.getPercussion( key );
				NamedInteger option = new NamedInteger( name, key );
				list.add( option );
				addElement( option );
			}
			
			NamedInteger chosenOption = NamedInteger.getElementByNumber( lastChosenPercussion, list );
			super.setSelectedItem( chosenOption );
		}
		else {
			// construct note options
			for ( int i = 0; i < 128; i++ ) {
				String name = Dict.getNote( i );
				NamedInteger option = new NamedInteger( name, i );
				list.add( option );
				addElement( option );
			}
			
			NamedInteger chosenOption = NamedInteger.getElementByNumber( lastChosenNote, list );
			super.setSelectedItem( chosenOption );
		}
	}
}
