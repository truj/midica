/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.model;

import javax.swing.table.DefaultTableModel;

/**
 * This class represents the data model of most tables used in this project.
 * 
 * @author Jan Trukenmüller
 */
public abstract class MidicaTableModel extends DefaultTableModel {
	
	// table header
	protected String[] columnNames = new String[ 0 ];
	
	/**
	 * Creates a new table model.
	 */
	protected MidicaTableModel() {
	}
	
	/**
	 * Returns the column name according to the given index.
	 * All column names should be initialized in the constructor of the derived class.
	 * 
	 * @param index    Table column index.
	 * @return    Column name.
	 */
	@Override
	public String getColumnName( int index ) {
		return columnNames[ index ];
	}
	
	/**
	 * Returns the number of columns in the table.
	 * Same as the column names initialized by the constructor of the derived class.
	 * 
	 * @return    Number of columns.
	 */
	@Override
	public int getColumnCount() {
		if ( null == columnNames )
			return 0;
		
		return columnNames.length;
	}
	
	/**
	 * Returns always false because none of the table cells is editable.
	 * 
	 * @param row    Queried row index.
	 * @param col    Queried column index.
	 * @return **false** - always.
	 */
	@Override
	public boolean isCellEditable( int row, int col ) {
		return false;
	}
	
	/**
	 * Returns the number of table rows.
	 * 
	 * @return    Number of table rows.
	 */
	@Override
	public abstract int getRowCount();
	
	/**
	 * Returns the value of the queried table cell.
	 * 
	 * @param rowIndex    Row index.
	 * @param colIndex    Column index.
	 * @return    Table cell value.
	 */
	@Override
	public abstract Object getValueAt( int rowIndex, int colIndex );
}


