/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.tablesorter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.RowFilter;
import javax.swing.SortOrder;
import javax.swing.table.TableRowSorter;

import org.midica.ui.model.MidicaTableModel;

/**
 * This class provides a table row sorter and filter for tables used in Midica.
 * 
 * The following sorting cycle is used:
 * 
 * MODEL > ASCENDING > DESCENDING > MODEL > ...
 * 
 * Filtering is possible based on:
 * 
 * - categories
 * - strings
 * - a combination of both
 * 
 * @author Jan Trukenmüller
 * @param <M> type of the model (MidicaTableModel)
 */
public class MidicaSorter<M> extends TableRowSorter {
	
	private static final HideCategoryFilter     hideCatFilter     = new HideCategoryFilter<>();
	private static final ShowOnlyCategoryFilter showOnlyCatFilter = new ShowOnlyCategoryFilter<>();
	
	private   boolean showCategories = true;
	protected String  filterStr      = "";
	
	private RowFilter<MidicaTableModel, Integer>            stringFilter = null;
	private ArrayList<RowFilter<MidicaTableModel, Integer>> filters      = new ArrayList<>();
	
	@Override
	public void toggleSortOrder(int column) {
		List<? extends SortKey> sortKeys = getSortKeys();
		showCategories = false;
		
		// already sorted?
		if (sortKeys.size() > 0) {
			
			// new and old sorting has the same column?
			int oldColumn = sortKeys.get(0).getColumn();
			if (column == oldColumn) {
				
				// change sorting to natural order: DESCENDING > MODEL
				if (SortOrder.DESCENDING == sortKeys.get(0).getSortOrder()) {
					setSortKeys(null);
					showCategories = true;
					filter();
					
					return;
				}
			}
		}
		filter();
		
		// use default toggling
		super.toggleSortOrder(column);
	}
	
	/**
	 * Applies the given filter criterion.
	 * 
	 * @param criterion  Filter criteron for filtering table rows.
	 */
	public void setStringFilter(String criterion) {
		
		// (?i) = insensitive
		if (criterion.length() > 0)
			filterStr = "(?i)" + Pattern.quote(criterion);
		else
			filterStr = "";
		
		filter();
	}
	
	/**
	 * Apply all active filters - category filter and/or string filter.
	 */
	private void filter() {
		filters.clear();
		
		// filter categories, if needed
		if ( ! showCategories )
			filters.add(hideCatFilter);
		
		// filter strings, if needed
		if ( ! "".equals(filterStr) ) {
			stringFilter = RowFilter.regexFilter(filterStr);
			filters.add(stringFilter);
			
			if (showCategories)
				filters.add(showOnlyCatFilter);
		}
		
		// apply filter(s)
		if (0 == filters.size())
			setRowFilter(null);
		else if (showCategories)
			setRowFilter(RowFilter.orFilter(filters));
		else
			setRowFilter(RowFilter.andFilter(filters));
	}
}
