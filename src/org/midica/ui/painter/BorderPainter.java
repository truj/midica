/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.painter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.Painter;

/**
 * Custom painter for rectangle-shaped borders around components.
 * Used for inactive text fields.
 */
public class BorderPainter implements Painter<JComponent> {
	
	private Color color = null;
	
	/**
	 * Creates a border painter.
	 * 
	 * @param color  border color
	 */
	public BorderPainter(Color color) {
		this.color = color;
	}
	
	@Override
	public void paint(Graphics2D g, JComponent component, int width, int height) {
		if (component instanceof JTextField) {
			g.setColor(color);
			Insets insets = component.getInsets();
			int xStart    = insets.left   - 3;
			int yStart    = insets.top    - 4;
			int xMargin   = insets.right  - 3;
			int yMargin   = insets.bottom - 3;
			g.drawRect(
				xStart,
				yStart,
				width  - (xStart + xMargin),
				height - (yStart + yMargin)
			);
		}
	}
}
