/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.sorter;

import javax.swing.RowFilter;

import org.midica.ui.model.MidicaTableModel;

/**
 * Row filter for categorized tables that hides the categories.
 * This is used, if the user changes the sorting.
 * In this case, the categories are cannot be placed correctly any more, so they need to be hidden.
 * 
 * @author Jan Trukenmüller
 * @param <M> type of the model (MidicaTableModel)
 */
public class HideCategoryFilter<M> extends RowFilter<M, Integer> {
	
	@Override
	public boolean include(Entry<? extends M, ? extends Integer> entry) {
		MidicaTableModel model = (MidicaTableModel) entry.getModel();
		
		// categorized using our own class?
		if ( model.getCategorizedRows() != null ) {
			int row = entry.getIdentifier();
			
			// category
			if ( model.getCategorizedRows().get(row).isCategory() )
				return false;
			
			// normal entry
			return true;
			
		}
		
		// categorized with a HashMap?
		else if ( model.getCategorizedHashMapRows() != null ) {
			int row = entry.getIdentifier();
			
			// category
			if ( model.getCategorizedHashMapRows().get(row).get("category") != null )
				return false;
			
			// normal entry
			return true;
		}
		
		// not categorized
		return true;
	}
}
