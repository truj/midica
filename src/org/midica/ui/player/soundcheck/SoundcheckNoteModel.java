/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.player.soundcheck;

import java.util.ArrayList;

import org.midica.config.Dict;
import org.midica.ui.model.MidicaTableModel;

/**
 * This class provides the model for the note/percussion table in the
 * soundcheck window.
 * 
 * @author Jan Trukenmüller
 */
public class SoundcheckNoteModel extends MidicaTableModel {
	
	private static final long serialVersionUID = 1L;
    
	/** determins if a note or percussion list has to be displayed */
	private boolean percussion = false;
	
	private ArrayList<Integer> list = null;
	
	/**
	 * Creates a model for the note/percussion list.
	 */
	public SoundcheckNoteModel() {

		// table header
		columnNames = new String[ 3 ];
		columnNames[ 0 ] = Dict.get( Dict.SNDCHK_COL_NOTE_NUM   );
		columnNames[ 1 ] = Dict.get( Dict.SNDCHK_COL_NOTE_NAME  );
		columnNames[ 2 ] = Dict.get( Dict.SNDCHK_COL_NOTE_SHORT );
		
		columnClasses = new Class[ 3 ];
		columnClasses[ 0 ] = Integer.class;
		columnClasses[ 1 ] = String.class;
		columnClasses[ 2 ] = String.class;
		
		list = new ArrayList<Integer>();
		
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
	public void setPercussion(boolean newPercussion) {
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
	 * Returns the note/percussion number list.
	 * 
	 * @return the list.
	 */
	public ArrayList<Integer> getList() {
		return list;
	}
	
	/**
	 * (Re)fills the note/percussion table with either notes or
	 * percussion instruments - depending on the {@link #percussion} field.
	 */
	public void init() {
		list.clear();
		
		if (percussion) {
			// add all known percussion ids
			for (int num : Dict.getPercussionNotes()) {
				list.add(num);
			}
		}
		else {
			// add all possible note numbers
			for (int i = 0; i < 128; i++) {
				list.add(i);
			}
		}
		
		// tell the table that it's data has changed.
		super.fireTableDataChanged();
	}
	
	@Override
	public int getRowCount() {
		if (null == list)
			return 0;
		return list.size();
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		if (0 == col) {
			return list.get(row);
		}
		else if (1 == col) {
			if (percussion)
				return Dict.getPercussionShortId(list.get(row));
			else
				return Dict.getNote(list.get(row));
		}
		else if (2 == col) {
			if (percussion)
				return Dict.getPercussionLongId(list.get(row));
			else
				return "";
		}
		
		return "-";
	}
}
