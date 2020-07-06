/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.tablesorter;

import java.util.ArrayList;

import javax.swing.RowFilter;

import org.midica.config.Dict;
import org.midica.ui.model.ExportResultTableModel;

/**
 * This class provides table filtering (and sorting) for the warnings in the export result table.
 * 
 * The sorting functionality is implemented by the parent class.
 * This class adds table specific filtering.
 * 
 * @author Jan Trukenmüller
 * @param <M> type of the model (MidicaTableModel)
 */
public class ExportResultTableSorter<M> extends MidicaSorter<M> {
	
	private boolean showShortMsg    = true;
	private boolean showMetaMsg     = true;
	private boolean showSysexMsg    = true;
	private boolean showRestSkipped = true;
	private boolean showOffNotFound = true;
	private boolean showOther       = true;
	
	/**
	 * Sets a new set of warning filters.
	 * 
	 * The only filter type which is **not** set here is the string filter.
	 * This is done by setStringFilter() in the parent class.
	 * 
	 * @param shortMsg      **true** to show Ignored Short Message warnings, otherwise **false**
	 * @param metaMsg       **true** to show Ignored Meta Message warnings, otherwise **false**
	 * @param sysexMsg      **true** to show Ignored SysEx Message warnings, otherwise **false**
	 * @param restSkipped   **true** to show Skipped Rest warnings, otherwise **false**
	 * @param offNotFound   **true** to show Note-OFF Not Found warnings, otherwise **false**
	 * @param other         **true** to show other warnings, otherwise **false**
	 */
	public void setWarningFilters(boolean shortMsg, boolean metaMsg, boolean sysexMsg, boolean restSkipped, boolean offNotFound, boolean other) {
		showShortMsg    = shortMsg;
		showMetaMsg     = metaMsg;
		showSysexMsg    = sysexMsg;
		showRestSkipped = restSkipped;
		showOffNotFound = offNotFound;
		showOther       = other;
	}
	
	/**
	 * Applies all filters.
	 * This includes the filters set by setMessageFilters() as well as
	 * the string filter set by setStringFilter() in the parent class.
	 */
	public void filter() {
		
		// top-level filter
		ArrayList<RowFilter<ExportResultTableModel, Integer>> andFilters = new ArrayList<>();
		
		// apply show filters
		ArrayList<RowFilter<ExportResultTableModel, Integer>> showFilters = new ArrayList<>();
		showFilters.add(new RowFilter<ExportResultTableModel, Integer>() {
			@Override
			public boolean include(Entry<? extends ExportResultTableModel, ? extends Integer> entry) {
				
				ExportResultTableModel tableModel = entry.getModel();
				Integer row = entry.getIdentifier();
				String warning = (String) tableModel.getValueAt(row, 3);
				if (showShortMsg && warning.equals(Dict.get(Dict.WARNING_IGNORED_SHORT_MESSAGE)))
					return true;
				if (showMetaMsg && warning.equals(Dict.get(Dict.WARNING_IGNORED_META_MESSAGE)))
					return true;
				if (showSysexMsg && warning.equals(Dict.get(Dict.WARNING_IGNORED_SYSEX_MESSAGE)))
					return true;
				if (showRestSkipped && warning.equals(Dict.get(Dict.WARNING_REST_SKIPPED)))
					return true;
				if (showOffNotFound && warning.equals(Dict.get(Dict.WARNING_OFF_NOT_FOUND)))
					return true;
				if (showOther && ! warning.equals(Dict.get(Dict.WARNING_IGNORED_SHORT_MESSAGE))
					&& ! warning.equals(Dict.get(Dict.WARNING_IGNORED_META_MESSAGE))
					&& ! warning.equals(Dict.get(Dict.WARNING_IGNORED_SYSEX_MESSAGE))
					&& ! warning.equals(Dict.get(Dict.WARNING_REST_SKIPPED))
					&& ! warning.equals(Dict.get(Dict.WARNING_OFF_NOT_FOUND)))
					return true;
				return false;
			}
		});
		andFilters.add(RowFilter.orFilter(showFilters));
		
		// apply string filter
		if (! filterStr.contentEquals("")) {
			andFilters.add(RowFilter.regexFilter(filterStr));
		}
		
		// set resulting filter
		setRowFilter(RowFilter.andFilter(andFilters));
	}
}
