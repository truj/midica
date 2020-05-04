/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.model;

import org.midica.config.Dict;

/**
 * This class represents the data model of the notes table in the
 * configuration details view of the info window.
 * 
 * Each row represents a note.
 * 
 * @author Jan Trukenmüller
 */
public class NoteTableModel extends MidicaTableModel {

	private static final long serialVersionUID = 1L;
    
	/**
	 * Creates a new instance of the notes table data model.
	 * Initializes the table header names according to the currently configured language.
	 */
	public NoteTableModel() {
		
		// table header
		columnNames = new String[ 3 ];
		columnNames[ 0 ] = Dict.get(Dict.INFO_COL_NOTE_NUM);
		columnNames[ 1 ] = Dict.get(Dict.INFO_COL_NOTE_NAME);
		columnNames[ 2 ] = Dict.get(Dict.INFO_COL_NOTE_ALT);
		
		// column classes, used for sorting
		columnClasses = new Class[ 3 ];
		columnClasses[ 0 ] = Integer.class;
		columnClasses[ 1 ] = String.class;
		columnClasses[ 2 ] = String.class;
	}
	
	/**
	 * Returns the number of rows in the table - same as the sum of notes (128).
	 * 
	 * @return    Number of rows (notes).
	 */
	@Override
	public int getRowCount() {
		return Dict.countNotes();
	}
	
	/**
	 * Returns the String value to be written into the specified table cell.
	 * 
	 * @param rowIndex    Queried table row index.
	 * @param colIndex    Queried table column index.
	 * @return    Table cell text.
	 */
	@Override
	public Object getValueAt(int rowIndex, int colIndex) {
		
		// number
		if (0 == colIndex)
			return rowIndex;
		
		// name
		else if (1 == colIndex)
			return Dict.getNote(rowIndex);
		
		// alternative names
		else if (2 == colIndex)
			return Dict.getNoteAlternatives(rowIndex);
		
		// default
		return "";
	}
}


