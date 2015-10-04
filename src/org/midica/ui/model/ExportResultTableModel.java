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
import org.midica.file.ExportResult;

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
	
	private ArrayList<HashMap<String, String>> warnings = null;
	
	/**
	 * Creates a new instance of the export result table data model.
	 * Initializes the table header names according to the currently configured language.
	 * 
	 * @param result
	 */
	public ExportResultTableModel( ExportResult result ) {
		
		this.warnings = result.getWarnings();
		
		// table header
		columnNames = new String[ 4 ];
		columnNames[ 0 ] = Dict.get( Dict.WARNING_COL_TICK    );
		columnNames[ 1 ] = Dict.get( Dict.WARNING_COL_CHANNEL );
		columnNames[ 2 ] = Dict.get( Dict.WARNING_COL_NOTE    );
		columnNames[ 3 ] = Dict.get( Dict.WARNING_COL_MESSAGE );
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
	public Object getValueAt( int rowIndex, int colIndex ) {
		
		// tick
		if ( 0 == colIndex ) {
			return warnings.get( rowIndex ).get( "tick" );
		}
		
		// channel
		else if ( 1 == colIndex ) {
			return warnings.get( rowIndex ).get( "channel" );
		}
		
		// note
		else if ( 2 == colIndex ) {
			String noteNum = warnings.get( rowIndex ).get( "note" );
			String channel = warnings.get( rowIndex ).get( "channel" );
			String noteName;
			if ( "9".equals(channel) )
				noteName = Dict.getPercussion( Integer.parseInt(noteNum) );
			else
				noteName = Dict.getNote( Integer.parseInt(noteNum) );
			return noteNum + ": " + noteName;
		}
		
		// message
		else if ( 3 == colIndex ) {
			return warnings.get( rowIndex ).get( "msg" );
		}
		
		// default
		return "";
	}
}


