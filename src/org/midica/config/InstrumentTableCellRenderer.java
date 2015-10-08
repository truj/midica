/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.config;

import java.awt.Component;
import java.util.ArrayList;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Cell renderer for the instrument table in the configuration overview.
 * 
 * The categories are displayed in another color than the plain instruments.
 * 
 * @author Jan Trukenmüller
 */
public class InstrumentTableCellRenderer extends DefaultTableCellRenderer {
	
	private static final long serialVersionUID = 1L;
	
	/** List containing all instruments including instrument category entries */
	private ArrayList<InstrumentElement> instrumentList;
	
	/**
	 * Creates a cell renderer for the instruments table in the configuration overview.
	 */
	public InstrumentTableCellRenderer() {
		instrumentList = Dict.getInstrumentList();
	}
	
	@Override
	public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col ) {
		Component cell = super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, col );
		if ( instrumentList.get(row).category ) {
			cell.setBackground( Config.TABLE_CELL_CATEGORY_COLOR );
		}
		else {
			cell.setBackground( Config.TABLE_CELL_DEFAULT_COLOR );
		}
		return cell;
	}
	
}
