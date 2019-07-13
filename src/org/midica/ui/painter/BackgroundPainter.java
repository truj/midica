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
import java.awt.RenderingHints;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.Painter;

import org.midica.ui.widget.MidicaSlider;
import org.midica.ui.widget.MidicaTree;

/**
 * Custom background painter for Nimbus components without color gradients.
 * 
 * Used for painting:
 * 
 * - the background of inactive text fields
 * - background and border of inactive buttons
 * - the background of selected tree cells
 * - the background of slider tracks
 */
public class BackgroundPainter implements Painter<JComponent> {
	
	private Color bgColor     = null;
	private Color secondColor = null;
	
	/**
	 * Creates a background painter using two colors.
	 * Used for selected tree cells, slider tracks or inactive (disabled) buttons.
	 * 
	 * @param bgColor      background color
	 * @param secondColor  border color (for inactive buttons and slider tracks) or background color (for message trees)
	 */
	public BackgroundPainter(Color bgColor, Color secondColor) {
		this.bgColor     = bgColor;
		this.secondColor = secondColor;
	}
	
	/**
	 * Creates a background painter using one color.
	 * Used for inactive (disabled) text fields.
	 * 
	 * @param bgColor  background color
	 */
	public BackgroundPainter(Color bgColor) {
		this.bgColor = bgColor;
	}
	
	@Override
	public void paint(Graphics2D g, JComponent component, int width, int height) {
		
		// enable antialiasing
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		// text field
		if (component instanceof JTextField) {
			Insets insets = component.getInsets();
			int xStart    = insets.left   - 3;
			int yStart    = insets.top    - 4;
			int xMargin   = insets.right  - 3;
			int yMargin   = insets.bottom - 3;
			
			// draw background
			g.setColor(bgColor);
			g.fillRect(
				xStart,
				yStart,
				width  - (xStart + xMargin),
				height - (yStart + yMargin)
			);
		}
		
		// button
		else if (component instanceof JButton) {
			
			Insets insets = component.getInsets();
			int xStart    = insets.left   - 12;
			int yStart    = insets.top    -  4;
			int xMargin   = insets.right  - 11;
			int yMargin   = insets.bottom -  3;
			int arcSize   = 7;
			
			// draw background
			g.setColor(bgColor);
			g.fillRoundRect(
				xStart,
				yStart,
				width  - (xStart + xMargin),
				height - (yStart + yMargin),
				arcSize,
				arcSize
			);
			
			// draw border
			g.setColor(secondColor);
			g.drawRoundRect(
				xStart,
				yStart,
				width  - (xStart + xMargin),
				height - (yStart + yMargin),
				arcSize,
				arcSize
			);
		}
		
		// tree
		else if (component instanceof MidicaTree) {
			
			// message trees get a different background color of selected cells
			MidicaTree tree = (MidicaTree) component;
			Color bgColor   = this.bgColor;
			if (tree.isMsgTree()) {
				bgColor = this.secondColor;
			}
			
			Insets insets = component.getInsets();
			int xStart    = insets.left;
			int yStart    = insets.top;
			int xMargin   = insets.right;
			int yMargin   = insets.bottom;
			
			// draw background
			g.setColor(bgColor);
			g.fillRect(
				xStart,
				yStart,
				width  - (xStart + xMargin),
				height - (yStart + yMargin)
			);
		}
		
		// slider track
		else if (component instanceof MidicaSlider) {
			
			Insets insets = component.getInsets();
			int xStart    = insets.left   + 1;
			int yStart    = insets.top    + 7;
			int xMargin   = insets.right  + 1;
			int yMargin   = insets.bottom + 7;
			int arcSize1  = 4;
			int arcSize2  = 2;
			
			// draw background
			g.setColor(bgColor);
			g.fillRoundRect(
				xStart,
				yStart,
				width  - (xStart + xMargin),
				height - (yStart + yMargin),
				arcSize1,
				arcSize2
			);
			
			// draw border
			g.setColor(secondColor);
			g.drawRoundRect(
				xStart,
				yStart,
				width  - (xStart + xMargin),
				height - (yStart + yMargin),
				arcSize1,
				arcSize2
			);
		}
	}
}
