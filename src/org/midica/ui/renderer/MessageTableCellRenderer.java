/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.renderer;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JTable;

import org.midica.ui.model.SingleMessage;
import org.midica.ui.model.MessageTableModel;

/**
 * Cell renderer for the message table in the **MIDI Sequence** > **MIDI Message**
 * tab of the info window.
 * 
 * The background color can be defined in the constructor.
 * 
 * The tooltip for cells in the **type** column is different from their
 * content.
 * 
 * @author Jan Trukenmüller
 */
public class MessageTableCellRenderer extends MidicaTableCellRenderer {
	
	private static final long serialVersionUID = 1L;
	
	private MessageTableModel model = null;
	
	/**
	 * Creates a cell renderer for the MIDI messages table.
	 */
	public MessageTableCellRenderer(MessageTableModel model, Color bgColor) {
		this.model = model;
		setBackground(bgColor);
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
		Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
		
		if ( col != 6 )
			return cell;
		
		// set the tooltip for the type column
		SingleMessage msg = model.getMsg(row);
		JComponent jCell = (JComponent) cell;
		jCell.setToolTipText( msg.getTypeTooltip() );
		
		return cell;
	}
}
