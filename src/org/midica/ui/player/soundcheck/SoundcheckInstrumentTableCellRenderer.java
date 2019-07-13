/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.player.soundcheck;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JTable;

import org.midica.config.Dict;
import org.midica.config.Laf;
import org.midica.ui.renderer.MidicaTableCellRenderer;

/**
 * Cell renderer for the instruments/drumkits table in the soundcheck window.
 * 
 * Categories and sub categories are colored differently from normal entries.
 * 
 * @author Jan Trukenmüller
 */
public class SoundcheckInstrumentTableCellRenderer extends MidicaTableCellRenderer {
	
	private static final long serialVersionUID = 1L;
	
	private SoundcheckInstrumentModel          model       = null;
	private ArrayList<HashMap<String, String>> instruments = null;
	
	/**
	 * Creates a cell renderer for the instruments/drumkits table.
	 */
	public SoundcheckInstrumentTableCellRenderer( SoundcheckInstrumentModel model ) {
		this.model = model;
		refreshInstruments();
	}
	
	@Override
	public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col ) {
		Component cell = super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, col );
		HashMap<String, String> entry = instruments.get( row );
		
		// category
		if ( entry.containsKey("category") ) {
			
			// sub category: colorize only the cell containing the name
			if ( "sub".equals(entry.get("category")) ) {
				if ( 3 == col ) {
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
			}
			
			// main category: same background in the whole row
			else {
				cell.setBackground( Laf.COLOR_TABLE_CELL_CATEGORY );
				if ( isSelected )
					cell.setBackground( Laf.COLOR_TABLE_CELL_CAT_SELECTED );
			}
		}
		else {
			// instrument or drumkit
			
			// background color
			if (isSelected)
				cell.setBackground( Laf.COLOR_TABLE_CELL_SELECTED );
			else
				cell.setBackground( null );
			
			// bank tooltip
			if ( 1 == col && cell instanceof JComponent ) {
				JComponent jCell = (JComponent) cell;
				String text = Dict.get(Dict.TOOLTIP_BANK_MSB)  + ": " + entry.get("bank_msb") + ", "
				            + Dict.get(Dict.TOOLTIP_BANK_LSB)  + ": " + entry.get("bank_lsb") + ", "
				            + Dict.get(Dict.TOOLTIP_BANK_FULL) + ": " + entry.get("bank");
				jCell.setToolTipText( text );
			}
		}
		
		return cell;
	}
	
	/**
	 * Initializes or reloads the list of categories, instruments and drumkits
	 * that support the currently selected channel.
	 */
	public void refreshInstruments() {
		instruments = model.getInstruments();
	}
}
