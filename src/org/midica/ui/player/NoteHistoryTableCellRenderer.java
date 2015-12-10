/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.player;

import java.awt.Component;

import javax.swing.JTable;

import org.midica.config.Config;
import org.midica.ui.renderer.MidicaTableCellRenderer;

/**
 * Cell renderer for the note history table for each channel.
 * 
 * Past and future notes are rendered with a different background color.
 * 
 * Each row represents a played note.
 * 
 * @author Jan Trukenmüller
 */
public class NoteHistoryTableCellRenderer extends MidicaTableCellRenderer {
	
	private static final long serialVersionUID = 1L;
	
	private NoteHistoryTableModel model = null;
	
	/**
	 * Creates a new instance of a note history table cell renderer.
	 * 
	 * @param model
	 */
	public NoteHistoryTableCellRenderer( NoteHistoryTableModel model ) {
		this.model = model;
	}
	
	@Override
	public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col ) {
		Component cell = super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, col );
		
		if ( model.isFuture(row) ) {
			cell.setBackground( Config.TABLE_CELL_FUTURE_COLOR );
		}
		else {
			cell.setBackground( Config.TABLE_CELL_DEFAULT_COLOR );
		}
		return cell;
	}
}
