/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.tablesorter;

import javax.swing.RowFilter;

import org.midica.ui.model.MidicaTableModel;

/**
 * Row filter for categorized tables that shows the categories.
 * This is used, if the user uses a string filter but keeps the default sorting.
 * In this case, the categories are still shown, even if they don't match the string filter.
 * 
 * @author Jan Trukenmüller
 * @param <M> type of the model (MidicaTableModel)
 */
public class ShowOnlyCategoryFilter<M> extends RowFilter<M, Integer> {
	
	@Override
	public boolean include(Entry<? extends M, ? extends Integer> entry) {
		MidicaTableModel model = (MidicaTableModel) entry.getModel();
		
		// categorized using our own class?
		if ( model.getCategorizedRows() != null ) {
			int row = entry.getIdentifier();
			
			// category
			if ( model.getCategorizedRows().get(row).isCategory() )
				return true;
			
			// normal entry
			return false;
			
		}
		
		// categorized with a HashMap?
		else if ( model.getCategorizedHashMapRows() != null ) {
			int row = entry.getIdentifier();
			
			// category
			if ( model.getCategorizedHashMapRows().get(row).get("category") != null )
				return true;
			
			// normal entry
			return false;
		}
		
		// not categorized
		return false;
	}
}
