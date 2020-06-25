/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.model;

import java.util.ArrayList;
import java.util.HashMap;

import org.midica.config.Dict;
import org.midica.ui.file.ExportResult;
import org.midica.ui.tablesorter.OptionalNumber;

/**
 * This class represents the data model of export result tables.
 * 
 * When decompiling a MIDI sequence, this table shows all warnings.
 * 
 * Each warning (row) consists of the following fields (columns):
 * 
 * - track number (or '?', if unknown)
 * - tickstamp where the warning occurred
 * - channel (or '-', if unknown)
 * - warning text
 * - details
 * 
 * @author Jan Trukenmüller
 */
public class ExportResultTableModel extends MidicaTableModel {

	private static final long serialVersionUID = 1L;
	
	private ArrayList<HashMap<String, Object>> warnings = null;
	
	/**
	 * Creates a new instance of the export result table data model.
	 * Initializes the table header names according to the currently configured language.
	 * 
	 * @param result
	 */
	public ExportResultTableModel(ExportResult result) {
		
		this.warnings = result.getWarnings();
		
		// table header
		columnNames = new String[5];
		columnNames[0] = Dict.get(Dict.WARNING_COL_TRACK);
		columnNames[1] = Dict.get(Dict.WARNING_COL_TICK);
		columnNames[2] = Dict.get(Dict.WARNING_COL_CHANNEL);
		columnNames[3] = Dict.get(Dict.WARNING_COL_MESSAGE);
		columnNames[4] = Dict.get(Dict.WARNING_COL_DETAILS);
		
		// column classes, used for sorting
		columnClasses = new Class[5];
		columnClasses[0] = OptionalNumber.class;
		columnClasses[1] = Long.class;
		columnClasses[2] = OptionalNumber.class;
		columnClasses[3] = String.class;
		columnClasses[4] = String.class;
	}
	
	/**
	 * Returns the number of rows in the table - same as the number of warnings.
	 * 
	 * @return    Number of rows (warnings).
	 */
	@Override
	public int getRowCount() {
		
		if (null == warnings)
			return 0;
		
		return warnings.size();
	}
	
	/**
	 * Returns the String value to be written into the specified table cell.
	 * 
	 * @param rowIndex    Queried table row index.
	 * @param colIndex    Queried table column index.
	 * @return    Table cell text.
	 */
	@Override
	public Object getValueAt(int rowIndex, int colIndex) {
		
		// track
		if (0 == colIndex) {
			Object trackObj = warnings.get(rowIndex).get("track");
			if (null == trackObj) {
				trackObj = "?";
			}
			trackObj = new OptionalNumber(trackObj);
			return trackObj;
		}
		
		// tick
		else if (1 == colIndex) {
			return warnings.get(rowIndex).get("tick");
		}
		
		// channel
		else if (2 == colIndex) {
			Object channelObj = warnings.get(rowIndex).get("channel");
			if (null == channelObj) {
				channelObj = "-";
			}
			channelObj = new OptionalNumber(channelObj);
			return channelObj;
		}
		
		// message
		else if (3 == colIndex) {
			return warnings.get(rowIndex).get("msg");
		}
		
		// details
		else if (4 == colIndex) {
			String details = (String) warnings.get(rowIndex).get("details");
			return null == details ? "" : details;
		}
		
		// default
		return "";
	}
}
