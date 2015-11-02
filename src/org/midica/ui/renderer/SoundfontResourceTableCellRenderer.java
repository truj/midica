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

import org.midica.config.Config;
import org.midica.file.SoundfontParser;

/**
 * Cell renderer for the soundfont resource table in the
 * configuration overview of the info window.
 * 
 * The categories are displayed in another color than the plain syntax
 * elements.
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
	 * Creates a cell renderer for the soundfont resource table
	 * in the configuration overview of the info window.
	 */
	public SoundfontResourceTableCellRenderer() {
		this.resources = SoundfontParser.getSoundfontResources();
	}
	
	@Override
	public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col ) {
		Component cell = super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, col );
		
		// category entries have an element with key=category and value=category
		HashMap<String, Object> resource = resources.get( row );
		boolean isCategory = resource.get("category") != null;
		if (isCategory) {
			cell.setBackground( Config.TABLE_CELL_CATEGORY_COLOR );
		}
		else {
			cell.setBackground( Config.TABLE_CELL_DEFAULT_COLOR );
		}
		
		// change the format tooltip
		if ( 4 == col && ! isCategory && cell instanceof JComponent ) {
			JComponent jCell   = (JComponent) cell;
			jCell.setToolTipText( (String) resource.get("formatDetail") );
		}
		
		// change the class tooltip
		if ( 5 == col && ! isCategory && cell instanceof JComponent ) {
			JComponent jCell   = (JComponent) cell;
			jCell.setToolTipText( (String) resource.get("classDetail") );
		}
		
		return cell;
	}
}
