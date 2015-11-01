/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.renderer;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Cell renderer for Midica tables.
 * 
 * Shows cell contents also as a tooltip text.
 * 
 * @author Jan Trukenmüller
 */
public class MidicaTableCellRenderer extends DefaultTableCellRenderer {
	
	private static final long serialVersionUID = 1L;
	
	@Override
	public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col ) {
		Component cell = super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, col );
		
		// add tooltip if possible
		if ( cell instanceof JComponent ) {
			JComponent jCell   = (JComponent) cell;
			Object     element = table.getValueAt( row, col );
			if ( element != null )
				jCell.setToolTipText( element.toString() );
		}
		
		return cell;
	}
	
}
