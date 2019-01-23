/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.player.soundcheck;

import java.util.ArrayList;
import java.util.Collections;

import javax.swing.DefaultListModel;

import org.midica.config.Dict;
import org.midica.file.NamedInteger;

/**
 * This class provides the model for the note/percussion list in the
 * soundcheck window.
 * 
 * @author Jan Trukenmüller
 */
public class SoundcheckNoteModel extends DefaultListModel<NamedInteger> {
	
	private static final long serialVersionUID = 1L;
    
	/** determins if a note or percussion list has to be displayed */
	private boolean percussion = false;
	
	private ArrayList<NamedInteger> list = null;
	
	/**
	 * Creates a model for the note/percussion list.
	 */
	public SoundcheckNoteModel() {
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
		percussion = newPercussion;
	}
	
	/**
	 * Determines if the list is currently loaded with chromatic notes or
	 * percussive instruments.
	 * 
	 * @return **true**, if the list contains percussion instruments; otherwise: **false**
	 */
	public boolean getPercussion() {
		return percussion;
	}
	
	/**
	 * (Re)fills the note/percussion list with either notes or
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
			
			// get longest possible amount of characters for a short option
			int shortOptChars = Dict.getPercussionShortIdLength();
			
			// construct and add each option
			for ( int key : sortedPercussion ) {
				StringBuffer name = new StringBuffer(Dict.getPercussionShortId(key));
				int numSpaces     = shortOptChars - name.length();
				for (int i = 0; i < numSpaces; i++)
					name.append(" ");
				name.append(" / ");
				name.append( Dict.getPercussionLongId(key) );
				NamedInteger option = new NamedInteger( name.toString(), key );
				list.add( option );
				addElement( option );
			}
		}
		else {
			// construct note options
			for ( int i = 0; i < 128; i++ ) {
				String name = Dict.getNote( i );
				NamedInteger option = new NamedInteger( name, i );
				list.add( option );
				addElement( option );
			}
		}
	}
}
