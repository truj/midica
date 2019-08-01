/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.player;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JTable;

import org.midica.config.Dict;
import org.midica.config.Laf;
import org.midica.ui.renderer.MidicaTableCellRenderer;

/**
 * Cell renderer for the note history table for each channel.
 * 
 * Past and future notes are rendered with a different background color.
 * 
 * Cells containing a long percussion ID get a tooltip with the corresponding short ID.
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
		row = table.convertRowIndexToModel(row);
		
		// set background color
		if ( model.isFuture(row) ) {
			cell.setBackground( Laf.COLOR_TABLE_CELL_FUTURE );
		}
		else {
			cell.setBackground( Laf.COLOR_TABLE_CELL_PAST );
		}
		
		// set tooltip for percussion IDs
		if (9 == model.getChannel() && 1 == col && cell instanceof JComponent) {
			try {
				Object     element = table.getValueAt(row, col);
				JComponent jCell   = (JComponent) cell;
				String     longId  = (String) element;
				int        percNum = Dict.getPercussion(longId);
				String     shortId = Dict.getPercussionShortId(percNum);
				jCell.setToolTipText(shortId);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return cell;
	}
}
