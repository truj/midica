/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.widget;

import org.midica.ui.model.MessageTableModel;
import org.midica.ui.tablesorter.MessageTableSorter;

/**
 * Class for the message table in the info view.
 * 
 * Supports special sorting and filtering.
 * 
 * @author Jan Trukenmüller
 */
public class MessageTable extends MidicaTable {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Creates a new message table.
	 * 
	 * @param model  The table model
	 */
	public MessageTable(MessageTableModel model) {
		super(model);
		enableSorting(model);
	}
	
	/**
	 * Creates and adds a sorter for the table so that it can be sorted by column.
	 * Applies the sortability of each column that is defined in the derived model.
	 * 
	 * @param model  the table model.
	 */
	private void enableSorting(MessageTableModel model) {
		
		// create sorter and connect it with the model
		MessageTableSorter<MessageTableModel> sorter = new MessageTableSorter<>();
		sorter.setModel(model);
		
		// apply sortability
		for (int i = 0; i < model.getColumnCount(); i++) {
			sorter.setSortable( i, model.isSortable(i) );
		}
		
		// connect sorter with table
		super.setRowSorter(sorter);
	}
}
