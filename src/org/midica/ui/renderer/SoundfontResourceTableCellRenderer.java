/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.renderer;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JTable;

import org.midica.config.Laf;
import org.midica.file.SoundfontParser;

/**
 * Cell renderer for the resources table in the
 * **Soundfont** > **Resources** tab of the info window.
 * 
 * The categories (samples, layers) are displayed in another color than the
 * normal data rows.
 * 
 * For the format and class columns the tooltips have to show more information
 * than the cell content.
 * 
 * @author Jan Trukenmüller
 */
public class SoundfontResourceTableCellRenderer extends MidicaTableCellRenderer {
	
	private static final long serialVersionUID = 1L;
	
	/** List containing all elements including category entries */
	private ArrayList<HashMap<String, Object>> resources;
	
	/**
	 * Creates a cell renderer for the soundfont resources table.
	 */
	public SoundfontResourceTableCellRenderer() {
		this.resources = SoundfontParser.getSoundfontResources();
	}
	
	@Override
	public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col ) {
		Component cell = super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, col );
		row = table.convertRowIndexToModel(row);
		
		// category entries have an element with key=category and value=category
		HashMap<String, Object> resource = resources.get( row );
		boolean isCategory = resource.get("category") != null;
		if (isCategory) {
			if (isSelected)
				cell.setBackground( Laf.COLOR_TABLE_CELL_CAT_SELECTED );
			else
				cell.setBackground( Laf.COLOR_TABLE_CELL_CATEGORY );
		}
		else {
			if (isSelected)
				cell.setBackground( Laf.COLOR_TABLE_CELL_SELECTED );
			else
				cell.setBackground( null );
		}
		
		// change the frames tooltip
		if ( 3 == col && ! isCategory && cell instanceof JComponent ) {
			JComponent jCell = (JComponent) cell;
			jCell.setToolTipText( (String) resource.get("length_detail") );
		}
		
		// change the format tooltip
		if ( 4 == col && ! isCategory && cell instanceof JComponent ) {
			JComponent jCell = (JComponent) cell;
			jCell.setToolTipText( (String) resource.get("format_detail") );
		}
		
		// change the class tooltip
		if ( 5 == col && ! isCategory && cell instanceof JComponent ) {
			JComponent jCell = (JComponent) cell;
			jCell.setToolTipText( (String) resource.get("class_detail") );
		}
		
		return cell;
	}
}
