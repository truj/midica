/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.widget;

import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.event.ChangeEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

import org.midica.config.Config;
import org.midica.ui.model.MidicaTableModel;

/**
 * Table header for {@link MidicaTable}s.
 * 
 * Sets the table header's background color.
 * 
 * Shows tooltips for table column headers. The tooltips are provided by
 * the {@link MidicaTableModel}.
 * 
 * @author Jan Trukenmüller
 */
public class MidicaTableHeader extends JTableHeader implements TableColumnModelListener {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Creates a new table header supporting tooltips.
	 * Sets the background color.
	 * 
	 * @param colModel   The column model of the table.
	 */
	public MidicaTableHeader( TableColumnModel colModel ) {
		columnModel = colModel;
		setBackground( Config.TABLE_HEADER_COLOR );
	}
	
	/**
	 * Returns the tooltip text according to the table header cell hovered
	 * by the mouse pointer.
	 * 
	 * @param e  The mouseover event.
	 * @return   The tooltip text for the column header hovered by the
	 *           mouse pointer.
	 */
	@Override
	public String getToolTipText( MouseEvent e ) {
		
		// get column
		Point mousePoint = e.getPoint();
		int   index      = columnModel.getColumnIndexAtX( mousePoint.x );
		int   col        = columnModel.getColumn( index ).getModelIndex();
		
		// get model
		MidicaTableModel model = (MidicaTableModel) this.getTable().getModel();
		
		// get tooltip
		return model.getHeaderTooltip( col );
	}
	
	/**
	 * Listens to column resize events and adjusts the header columns to the
	 * data columns.
	 * 
	 * @param e   The column resize event.
	 */
	@Override
	public void columnMarginChanged( ChangeEvent e ) {
		
		// It's really strange that it's necessary to override this method in
		// this way. But otherwise the header columns just stay the same.
		super.columnMarginChanged( e );
	}
}
