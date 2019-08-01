/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.widget;

import javax.swing.JTable;

import org.midica.ui.model.MidicaTableModel;
import org.midica.ui.sorter.MidicaSorter;

/**
 * Class for Midica tables.
 * 
 * These tables use {@link MidicaTableHeader}s supporting tooltips.
 * 
 * @author Jan Trukenmüller
 */
public class MidicaTable extends JTable {
	
	private static final long serialVersionUID = 1L;
	private MidicaTableHeader tableHeader = null;
	
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
	public MidicaTable(MidicaTableModel model) {
		super(model);
		enableSorting(model);
	}
	
	/**
	 * Sets the given model as table model.
	 * 
	 * @param model  The table model
	 */
	public void setModel(MidicaTableModel model) {
		super.setModel(model);
		enableSorting(model);
	}
	
	/**
	 * Creates and adds a sorter for the table so that it can be sorted by column.
	 * Applies the sortability of each column that is defined in the derived model.
	 * 
	 * @param model  the table model.
	 */
	private void enableSorting(MidicaTableModel model) {
		
		// create sorter and connect it with the model
		MidicaSorter<MidicaTableModel> sorter = new MidicaSorter<>();
		sorter.setModel(model);
		
		// apply sortability
		for (int i = 0; i < model.getColumnCount(); i++) {
			sorter.setSortable( i, model.isSortable(i) );
		}
		
		// connect sorter with table
		super.setRowSorter(sorter);
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
	public MidicaTableHeader createDefaultTableHeader() {
		
		// create the header - that is a listener for column resizes
		tableHeader = new MidicaTableHeader( columnModel );
		
		// let the header listen to column size changes
		columnModel.addColumnModelListener( tableHeader );
		
		return tableHeader;
	}
}
