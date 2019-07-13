/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.widget;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;

import org.midica.config.Laf;

/**
 * This class provides a replacement for a {@link JLabel} with a fixed size.
 * The purpose is to avoid the label to become too big so that the layout stays healthy.
 * If the text is longer than the label, only the beginning of the text is shown.
 * The full text is set automatically as a tool tip.
 * 
 * It's actually a {@link JTextField} that's modified in order to look more or less
 * like a {@link JLabel}.
 * 
 * @author Jan Trukenmüller
 */
public class FixedLabel extends JTextField {
	
	private static final long serialVersionUID = 1L;
	
	private int preferredWidth  = 0;
	private int preferredHeight = 15;
	
	/**
	 * Creates a new {@link FixedLabel}.
	 * 
	 * @param content      The text to be displayed.
	 * @param prefWidth    The designated width of the label.
	 */
	public FixedLabel(String content, int prefWidth) {
		
		super(content);
		this.preferredWidth = prefWidth;
		
		// background color for nimbus
		if (Laf.isNimbus) {
			setMargin( Laf.INSETS_FLOW_LBL_NIMBUS );
			setBackground( Laf.COLOR_PANEL );
		}
		
		// Make it look like a label.
		setOpaque(false);
		
		// Make it behave like a label.
		setEditable(false);
		
		// Show the beginning of the string (not the end),
		// if the string is longer than the label.
		setCaretPosition(0);
	}
	
	/**
	 * Makes the font look like in a JLabel.
	 */
	public void makeFontLookLikeLabel() {
		setFont( UIManager.getFont("Label.font") );
	}
	
	/**
	 * Overrides the parent's method in order to adjust the dimension
	 * to the text content.
	 */
	@Override
	public Dimension getPreferredSize() {
		return new Dimension( preferredWidth, preferredHeight );
	}
	
	/**
	 * Sets the background color.
	 */
	@Override
	public void setBackground( Color color ) {
		super.setBackground(color);
		setOpaque(true);
	}
	
	/**
	 * Sets the given text in the label.
	 * 
	 * @param text The text to be displayed.
	 */
	public void setText(String text) {
		super.setText(text);
		if (null == text || "".equals(text))
			setToolTipText(null);
		else
			setToolTipText(text);
		setCaretPosition(0);
	}
	
	/**
	 * This method is overridden in order to show no border at all.
	 */
	@Override
	public void setBorder(Border border) {
		// nothing to do
	}
}
