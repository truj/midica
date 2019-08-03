/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.tablefilter;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.midica.config.Laf;
import org.midica.ui.widget.MidicaTable;

/**
 * This class provides a combination between a text label and a string filter icon for a table.
 * 
 * @author Jan Trukenmüller
 */
public class FilterIconWithLabel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	private FilterIcon filterIcon;
	
	/**
	 * Creates a new panel, consisting of a text label and a filter icon.
	 * Text and icon are placed horizontally (in a row).
	 * 
	 * @param labelStr   The content of the text label.
	 * @param owner      The window containing the text, the icon and the table.
	 */
	public FilterIconWithLabel(String labelStr, Window owner) {
		filterIcon = new FilterIcon(owner);
		init(labelStr, false);
	}
	
	/**
	 * Creates a new panel, consisting of a text label and a filter icon.
	 * Text and icon are placed either horizontally (in a row) or vertically (in a column).
	 * 
	 * @param labelStr   The content of the text label.
	 * @param owner      The window containing the text, the icon and the table.
	 * @param vertical   **true** for placing text and icon in a column, **false** for placing them in a row.
	 */
	public FilterIconWithLabel(String labelStr, Window owner, boolean vertical) {
		filterIcon = new FilterIcon(owner);
		init(labelStr, vertical);
	}
	
	/**
	 * Creates the panel and places text and icon inside.
	 * 
	 * @param labelStr   The content of the text label.
	 * @param vertical   **true** for placing text and icon in a column, **false** for placing them in a row.
	 */
	private void init(String labelStr, boolean vertical) {
		
		// layout
		setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.NONE;
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 0;
		constraints.weighty    = 0;
		if (vertical)
			constraints.insets = Laf.INSETS_FILTER_ICON_W_LBL_V;
		else
			constraints.insets = Laf.INSETS_FILTER_ICON_W_LBL_H;
		
		// label
		JLabel label = new JLabel(labelStr);
		add(label, constraints);
		Laf.makeBold(label);
		
		// filter icon
		constraints.insets = Laf.INSETS_ZERO;
		if (vertical) {
			constraints.gridy++;
			constraints.anchor = GridBagConstraints.EAST;
		}
		else {
			constraints.gridx++;
		}
		add(filterIcon, constraints);
	}
	
	/**
	 * Sets the table that's filtered by the filter.
	 * 
	 * @param table  Table to be filtered.
	 */
	public void setTable(MidicaTable table) {
		filterIcon.setTable(table);
	}
}
