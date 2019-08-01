/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.sorter;

import java.util.List;

import javax.swing.SortOrder;
import javax.swing.table.TableRowSorter;

/**
 * This class provides a table row sorter for tables used in Midica.
 * 
 * Using the following sorting cycle:
 * 
 * MODEL > ASCENDING > DESCENDING > MODEL > ...
 * 
 * @author Jan Trukenmüller
 * @param <M> type of the model (MidicaTableModel)
 */
public class MidicaSorter<M> extends TableRowSorter {
	
	private static final HideCategoryFilter hideCatFilter = new HideCategoryFilter<>();
	
	@Override
	public void toggleSortOrder(int column) {
		List<? extends SortKey> sortKeys = getSortKeys();
		
		// show everything
		setRowFilter(null);
		
		// already sorted?
		if (sortKeys.size() > 0) {
			
			// new and old sorting has the same column?
			int oldColumn = sortKeys.get(0).getColumn();
			if (column == oldColumn) {
				
				// change sorting to natural order: DESCENDING > MODEL
				if (SortOrder.DESCENDING == sortKeys.get(0).getSortOrder()) {
					setSortKeys(null);
					return;
				}
			}
		}
		
		// use default toggling
		setRowFilter(hideCatFilter);
		super.toggleSortOrder(column);
	}
	
}
