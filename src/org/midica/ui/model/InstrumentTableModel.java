/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.model;

import java.util.ArrayList;

import org.midica.config.Dict;
import org.midica.ui.info.CategorizedElement;
import org.midica.ui.info.InstrumentElement;
import org.midica.ui.tablesorter.OptionalNumber;


/**
 * This class represents the data model of the instruments table in the
 * configuration details view of the info window.
 * 
 * Each row represents either an instrument category or an instrument.
 * 
 * @author Jan Trukenmüller
 */
public class InstrumentTableModel extends MidicaTableModel {

	private static final long serialVersionUID = 1L;
	
	private ArrayList<InstrumentElement> instrumentList = null;
	
	/**
	 * Creates a new instance of the instruments table data model.
	 * Initializes the table header names according to the currently configured language.
	 */
	public InstrumentTableModel() {
		
		// table header
		columnNames = new String[ 2 ];
		columnNames[ 0 ] = Dict.get( Dict.INFO_COL_INSTR_NUM  );
		columnNames[ 1 ] = Dict.get( Dict.INFO_COL_INSTR_NAME );
		
		// column classes, used for sorting
		columnClasses = new Class[ 2 ];
		columnClasses[ 0 ] = OptionalNumber.class;
		columnClasses[ 1 ] = String.class;
		
		instrumentList = Dict.getInstrumentList();
	}
	
	/**
	 * Returns the number of rows in the table - same as the sum of instruments and categories.
	 * 
	 * @return    Number of rows (instruments + categories).
	 */
	@Override
	public int getRowCount() {
		if ( null == instrumentList )
			return 0;
		return instrumentList.size();
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
		
		// number
		if ( 0 == colIndex )
			if ( instrumentList.get(rowIndex).isCategory() )
				return new OptionalNumber("");
			else
				return new OptionalNumber( instrumentList.get( rowIndex ).instrNum );
		
		// name
		else if ( 1 == colIndex ) {
			return instrumentList.get( rowIndex ).name;
		}
		
		// default
		return Dict.get( Dict.UNKNOWN_INSTRUMENT );
	}
	
	@Override
	public ArrayList<CategorizedElement> getCategorizedRows() {
		return new ArrayList<CategorizedElement>(instrumentList);
	}
}


