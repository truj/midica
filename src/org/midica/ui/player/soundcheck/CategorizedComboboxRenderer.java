/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.player.soundcheck;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.midica.config.CategorizedElement;

/**
 * Renders the items of a categorized combobox. Makes sure that categories
 * are colored different from normal items.
 * 
 * @author Jan Trukenmüller
 */
public class CategorizedComboboxRenderer extends JLabel implements ListCellRenderer {

	private static final long serialVersionUID = 1L;
	
	public static final Color COLOR_CATEGORY_FOREGROUND = new Color(  50, 100, 255 );
	public static final Color COLOR_CATEGORY_BACKGROUND = new Color( 200, 255, 200 );
	
	/**
	 * Creates an object of a cell renderer for categorized comboboxes.
	 */
	public CategorizedComboboxRenderer() {
		setOpaque( true );
	}
	
	/**
	 * Renders and returns a combobox item for a categorized combobox.
	 * 
	 * Categories are colored different from normal items.
	 * 
	 * @param list        A list of all items, including the categories.
	 * @param value       The {@link CategorizedElement} to be displayed.
	 * @param index       The index number inside the list.
	 * @param isSelected  **true** if the specified cell was selected; otherwise **false**
	 * @param hasFocus    **true** if the specified cell is focused; otherwise **false**
	 * 
	 * @return the rendered combobox item.
	 */
	@Override
	public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected, boolean hasFocus ) {
		
		if (isSelected) {
			setForeground( list.getSelectionForeground() );
			setBackground( list.getSelectionBackground() );
		}
		else {
			if ( ((CategorizedElement) value).isCategory() ) {
				setForeground( COLOR_CATEGORY_FOREGROUND );
				setBackground( COLOR_CATEGORY_BACKGROUND );
			}
			else {
				setForeground( list.getForeground() );
				setBackground( list.getBackground() );
			}
		}
		
		setText( (value == null) ? "" : value.toString() );
		return this;
	}
}
