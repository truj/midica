/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.tablefilter;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.event.MouseInputAdapter;

/**
 * This class provides drag functionality for a component.
 * It's used to make table string filter layers draggable.
 * 
 * @author Jan Trukenmüller
 */
public class DragListener extends MouseInputAdapter {
	Point      location;
	MouseEvent dragStart;
	
	/**
	 * Remembers the starting point of the dragging.
	 * 
	 * @param e  the mouse event
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		dragStart = e;
	}
	
	/**
	 * Moves the component to the new location.
	 * 
	 * @param e  the mouse event
	 */
	@Override
	public void mouseDragged(MouseEvent e) {
		Component component = e.getComponent();
		location = component.getLocation(location);
		int newX = location.x - dragStart.getX() + e.getX();
		int newY = location.y - dragStart.getY() + e.getY();
		component.setLocation(newX, newY);
	}
}