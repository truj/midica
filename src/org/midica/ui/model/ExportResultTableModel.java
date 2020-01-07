/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.model;

import java.util.ArrayList;
import java.util.HashMap;

import org.midica.config.Dict;
import org.midica.ui.file.ExportResult;

/**
 * This class represents the data model of export result tables.
 * 
 * When exporting a MIDI stream to a MidicaPL file, this table shows all warnings.
 * 
 * Each warning (row) consists of the following fields (columns):
 * 
 * - tickstamp where the warning occurred
 * - channel
 * - note number
 * - warning text
 * 
 * @author Jan Trukenmüller
 */
public class ExportResultTableModel extends MidicaTableModel {

	private static final long serialVersionUID = 1L;
	
	private ArrayList<HashMap<String, Object>> warnings = null;
	
	/**
	 * Creates a new instance of the export result table data model.
	 * Initializes the table header names according to the currently configured language.
	 * 
	 * @param result
	 */
	public ExportResultTableModel( ExportResult result ) {
		
		this.warnings = result.getWarnings();
		
		// table header
		columnNames = new String[ 5 ];
		columnNames[ 0 ] = Dict.get( Dict.WARNING_COL_TRACK   );
		columnNames[ 1 ] = Dict.get( Dict.WARNING_COL_TICK    );
		columnNames[ 2 ] = Dict.get( Dict.WARNING_COL_CHANNEL );
		columnNames[ 3 ] = Dict.get( Dict.WARNING_COL_NOTE    );
		columnNames[ 4 ] = Dict.get( Dict.WARNING_COL_MESSAGE );
		
		// column classes, used for sorting
		columnClasses = new Class[ 5 ];
		columnClasses[ 0 ] = Integer.class;
		columnClasses[ 1 ] = Long.class;
		columnClasses[ 2 ] = Byte.class;
		columnClasses[ 3 ] = String.class;
		columnClasses[ 4 ] = String.class;
	}
	
	
	/**
	 * Returns the number of rows in the table - same as the number of warnings.
	 * 
	 * @return    Number of rows (warnings).
	 */
	@Override
	public int getRowCount() {
		
		if ( null == warnings )
			return 0;
		
		return warnings.size();
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
		
		// track
		if (0 == colIndex) {
			return warnings.get(rowIndex).get("track");
		}
		
		// tick
		else if (1 == colIndex) {
			return warnings.get(rowIndex).get("tick");
		}
		
		// channel
		else if (2 == colIndex) {
			Integer channel = (Integer) warnings.get(rowIndex).get("channel");
			if (-1 == channel) {
				return "-";
			}
			return channel;
		}
		
		// note
		else if (3 == colIndex) {
			Integer noteNum = (Integer) warnings.get(rowIndex).get("note");
			if (-1 == noteNum) {
				return "-";
			}
			Integer channel = (Integer) warnings.get(rowIndex).get("channel");
			String noteName;
			if (9 == channel)
				noteName = Dict.getPercussionLongId(noteNum);
			else
				noteName = Dict.getNote(noteNum);
			return noteNum + ": " + noteName;
		}
		
		// message
		else if (4 == colIndex) {
			return warnings.get(rowIndex).get("msg");
		}
		
		// default
		return "";
	}
}
