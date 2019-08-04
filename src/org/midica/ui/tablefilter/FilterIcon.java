/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.tablefilter;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.RowSorterListener;

import org.midica.config.Dict;
import org.midica.config.Laf;
import org.midica.ui.widget.MidicaTable;

/**
 * This class provides a filter icon for filtering a table.
 * When clicked, it opens a layer where a string filter can be entered.
 * 
 * @author Jan Trukenmüller
 */
public class FilterIcon extends JLabel {
	
	private static final long serialVersionUID = 1L;
	
	private boolean isActive = false;
	
	private static final String pathFilterActive   = "org/midica/resources/filter-active.png";
	private static final String pathFilterInactive = "org/midica/resources/filter-inactive.png";
	
	private ImageIcon iconActive   = null;
	private ImageIcon iconInactive = null;
	
	private static final Border borderActive   = new LineBorder(Laf.COLOR_TBL_FILTER_ICON_BORDER_ACTIVE,   1);
	private static final Border borderInactive = new LineBorder(Laf.COLOR_TBL_FILTER_ICON_BORDER_INACTIVE, 1);
	
	private StringFilterLayer layer;
	
	/**
	 * Creates a new icon for showing a table filter on click.
	 * 
	 * @param owner  The window that contains the icon and the table.
	 */
	public FilterIcon(Window owner) {
		iconActive   = new ImageIcon( ClassLoader.getSystemResource(pathFilterActive)   );
		iconInactive = new ImageIcon( ClassLoader.getSystemResource(pathFilterInactive) );
		layer        = new StringFilterLayer(this, owner);
		
		setIcon(iconInactive);
		setOpaque(true);
		setActive(false);
		setBackground(null);
		
		
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				layer.open();
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				if (isActive)
					setBackground(Laf.COLOR_TBL_FILTER_ICON_HOVER_BG_ACTIVE);
				else
					setBackground(Laf.COLOR_TBL_FILTER_ICON_HOVER_BG_INACTIVE);
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				setBackground(null);
			}
		});
	}
	
	/**
	 * Sets the table that's filtered by the filter.
	 * 
	 * @param table  Table to be filtered.
	 */
	public void setTable(MidicaTable table) {
		layer.setTable(table);
	}
	
	/**
	 * Changes icon and tooltip according to the filter state.
	 * 
	 * An empty filter icon looks different from an active filter.
	 * 
	 * @param active    **false**, if the filter is empty, otherwise **true**.
	 */
	public void setActive(boolean active) {
		isActive = active;
		setBackground(null);
		
		// icon and tooltip
		String tooltip = "<html>" + Dict.get(Dict.FILTER_ICON_TOOLTIP) + "<br>\n";
		if (active) {
			setIcon(iconActive);
			tooltip += Dict.get(Dict.FILTER_ICON_TOOLTIP_ACTIVE);
			setBorder(borderActive);
		}
		else {
			setIcon(iconInactive);
			tooltip += Dict.get(Dict.FILTER_ICON_TOOLTIP_EMPTY);
			setBorder(borderInactive);
		}
		setToolTipText(tooltip);
	}
	
	/**
	 * Adds a {@link RowSorterListener} to the row sorter.
	 * 
	 * @param listener  The listener to be added.
	 */
	public void addRowSorterListener(RowSorterListener listener) {
		layer.addRowSorterListener(listener);
	}
}
