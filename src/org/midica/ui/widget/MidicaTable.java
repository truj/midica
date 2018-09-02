/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.widget;

import javax.swing.JTable;
import javax.swing.table.JTableHeader;

import org.midica.ui.model.MidicaTableModel;

/**
 * Class for Midica tables.
 * 
 * These tables use {@link MidicaTableHeader}s supporting tooltips.
 * 
 * @author Jan Trukenmüller
 */
public class MidicaTable extends JTable {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Creates a new table supporting tooltips in the table header.
	 */
	public MidicaTable() {
		super();
	}
	
	/**
	 * Creates a new table supporting tooltips in the table header.
	 * 
	 * @param model  The table model
	 */
	public MidicaTable( MidicaTableModel model ) {
		super( model );
	}
	
	/**
	 * Sets the given model as table model.
	 * 
	 * @param model  The table model
	 */
	public void setModel( MidicaTableModel model ) {
		super.setModel( model );
	}
	
	/**
	 * Creates a {@link MidicaTableHeader} supporting tooltips.
	 * 
	 * Enables that header to synchronize the column header sizes with the
	 * column sizes.
	 * 
	 * @return the created table header.
	 */
	@Override
	protected JTableHeader createDefaultTableHeader() {
		
		// create the header - that is a listener for column resizes
		MidicaTableHeader header = new MidicaTableHeader( columnModel );
		
		// let the header listen to column size changes
		columnModel.addColumnModelListener( header );
		
		return header;
	}
}
