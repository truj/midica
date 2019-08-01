/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.model;

import java.util.ArrayList;

import org.midica.config.Dict;
import org.midica.ui.info.InstrumentElement;


/**
 * This class represents the data model of the drumkits table in the
 * configuration details view of the info window.
 * 
 * @author Jan Trukenmüller
 */
public class DrumkitTableModel extends MidicaTableModel {

	private static final long serialVersionUID = 1L;
	
	private ArrayList<InstrumentElement> drumkitList = null;
	
	/**
	 * Creates a new instance of the drumkit table data model.
	 * Initializes the table header names according to the currently configured language.
	 */
	public DrumkitTableModel() {
		
		// table header
		columnNames = new String[ 2 ];
		columnNames[ 0 ] = Dict.get( Dict.INFO_COL_DRUMKIT_NUM  );
		columnNames[ 1 ] = Dict.get( Dict.INFO_COL_DRUMKIT_NAME );
		
		// column classes, used for sorting
		columnClasses = new Class[ 2 ];
		columnClasses[ 0 ] = Integer.class;
		columnClasses[ 1 ] = String.class;
		
		drumkitList = Dict.getDrumkitList();
	}
	
	/**
	 * Returns the number of rows in the table - same as the sum of drumkits.
	 * 
	 * @return    Number of rows (drumkits).
	 */
	@Override
	public int getRowCount() {
		if ( null == drumkitList )
			return 0;
		return drumkitList.size();
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
			return drumkitList.get( rowIndex ).instrNum;
		
		// name
		else if (1 == colIndex) {
			return drumkitList.get(rowIndex).name;
		}
		
		// default
		return Dict.get( Dict.UNKNOWN_DRUMKIT_NAME );
	}
}


