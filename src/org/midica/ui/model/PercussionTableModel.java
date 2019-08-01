/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.model;

import java.util.ArrayList;
import java.util.Collections;

import org.midica.config.Dict;


/**
 * This class represents the data model of the percussion table in the
 * configuration details view of the info window.
 * 
 * Each row represents a percussion instrument.
 * 
 * @author Jan Trukenmüller
 */
public class PercussionTableModel extends MidicaTableModel {

	private static final long serialVersionUID = 1L;
    
	private ArrayList<Integer> instrumentNumbers = new ArrayList<Integer>();
	
	/**
	 * Creates a new instance of the percussion table data model.
	 * Initializes the table header names according to the currently configured language.
	 */
	public PercussionTableModel() {
		
		// table header
		columnNames = new String[ 3 ];
		columnNames[ 0 ] = Dict.get( Dict.INFO_COL_PERC_NUM      );
		columnNames[ 1 ] = Dict.get( Dict.INFO_COL_PERC_ID_SHORT );
		columnNames[ 2 ] = Dict.get( Dict.INFO_COL_PERC_ID_LONG  );
		
		// column classes, used for sorting
		columnClasses = new Class[ 3 ];
		columnClasses[ 0 ] = Integer.class;
		columnClasses[ 1 ] = String.class;
		columnClasses[ 2 ] = String.class;
		
		for ( int instrNum : Dict.getPercussionNotes() ) {
			instrumentNumbers.add( instrNum );
		}
		Collections.sort( instrumentNumbers );
	}
	
	/**
	 * Returns the number of rows in the table - same as the configured
	 * percussion instruments.
	 * 
	 * @return    Number of rows (percussion instruments).
	 */
	@Override
	public int getRowCount() {
		return Dict.countPercussion();
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
		int instrNum = instrumentNumbers.get( rowIndex );
		
		// number
		if (0 == colIndex)
			return instrNum;
		
		// short id
		else if (1 == colIndex)
			return Dict.getPercussionShortId( instrNum );
		
		// long id
		else if (2 == colIndex)
			return Dict.getPercussionLongId( instrNum );
		
		// default
		return "";
	}
}


