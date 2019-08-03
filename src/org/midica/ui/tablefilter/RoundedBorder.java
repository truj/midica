/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.tablefilter;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.border.AbstractBorder;

/**
 * This class represents a rounded border around a component.
 * It's used to draw a border around a string filter layer for a table.
 * 
 * @author Jan Trukenmüller
 */
class RoundedBorder extends AbstractBorder {
	
	private static final long serialVersionUID = 1L;
	
	private int   arcWidth;
	private int   arcHeight;
	private int   borderWidth;
	private Color color;
	
	/**
	 * Creates a rounded border.
	 * 
	 * @param arcWidth     the horizontal diameter of the arc at the four corners
	 * @param arcHeight    the vertical diameter of the arc at the four corners
	 * @param borderWidth  the border stroke width
	 * @param color        the border color
	 */
	public RoundedBorder(int arcWidth, int arcHeight, int borderWidth, Color color) {
		this.arcWidth    = arcWidth;
		this.arcHeight   = arcHeight;
		this.borderWidth = borderWidth;
		this.color       = color;
	}
	
	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
		
		// enable antialiasing
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		// adjust border width
		g2.setStroke(new BasicStroke(borderWidth));
		
	    int w = width;
	    int h = height;
	    
	    g.translate(x, y);
	    g.setColor(color);
	    g.drawRoundRect( 0, 0, w-2, h-2, arcWidth, arcHeight );
	    g.translate(-x, -y);
	}
}
