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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.Border;

/**
 * This class provides a replacement for a {@link JLabel} that enables smoother
 * word wrapping of long texts and scrollbars when needed.
 * 
 * It's actually a {@link JScrollPane} containing a {@link JTextArea} that's
 * changed modified in order to look more or less like a {@link JLabel}.
 * 
 * The height of the label is automatically adjusted to the containing text.
 * However it can still be limited to a certain value by calling
 * {@link #setHeightLimit(int)}.
 * 
 * @author Jan Trukenmüller
 */
public class FlowLabel extends JScrollPane {
	
	private static final long serialVersionUID = 1L;
	
	private static final int HEIGHT_PER_LINE = 15;
	
	private int       charsPerLine    = 0;
	private int       preferredWidth  = 0;
	private int       preferredHeight = HEIGHT_PER_LINE;
	private int       maxHeight       = -1;
	private String    text            = null;
	private JTextArea label           = null;

	/**
	 * Creates a new, empty {@link FlowLabel}.
	 * 
	 * @param content         The text to be displayed.
	 * @param charsPerLine    The estimated number of characters fitting in one line.
	 * @param prefWidth       The designated width of the label.
	 */
	public FlowLabel( String content, int charsPerLine, int prefWidth ) {
		
		// Create the label inside the scroll pane.
		this.text           = content;
		this.charsPerLine   = charsPerLine;
		this.preferredWidth = prefWidth;
		this.label          = new JTextArea( content );
		
		// Make it look like a label.
		label.setOpaque( false );
		
		// Make it behave like a label.
		label.setEditable( false );
		
		// Break lines if needed.
		label.setLineWrap( true );
		
		// Don't break the lines inside of a word but between words, if possible.
		label.setWrapStyleWord( true );
		
		// Connect the label with the scroll pane.
		this.setViewportView( label );
		
		// Enable scrollbars if needed.
		this.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
		this.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED );
		
		// calculate the preferred height
		adjustHeight();
	}
	
	/**
	 * Makes the font look like in a JLabel.
	 */
	public void makeFontLookLikeLabel() {
		label.setFont( UIManager.getFont("Label.font") );
	}
	
	/**
	 * Calculates and remembers the preferred height of this component.
	 * This calculation is based on the number of line breaks in the text,
	 * and also on the lines that are longer than the {@link #charsPerLine}
	 * and their lengths.
	 */
	private void adjustHeight() {
		
		// get lines specified by line breaks
		String[] lines = text.split( "\n", -1 ); // -1: don't ignore trailing line breaks
		int lineCount  = lines.length;
		
		// increment the line count for oversized lines
		int extraLines = 0;
		for ( String line : lines ) {
			
			// estimate the needed sub lines for this line
			int chars        = line.length();
			int subLineCount = chars / charsPerLine;
			
			// add to the total extra lines
			extraLines += subLineCount;
		}
		lineCount += extraLines;
		
		// get the needed height
		int neededHeight = lineCount * HEIGHT_PER_LINE;
		
		// adjust the resulting height
		preferredHeight = neededHeight;
	}
	
	/**
	 * Overrides the parent's method in order to adjust the dimension
	 * to the text content. If {@link #setHeightLimit(int)} has been called
	 * before with a positive value, the preferred height will never be larger
	 * than that value.
	 * 
	 * In other words: This method tries to make the content big enough so that
	 * scrollbars are not necessary. But the height can still be limited to a
	 * smaller value.
	 */
	@Override
	public Dimension getPreferredSize() {
		int height = preferredHeight;
		if ( maxHeight > -1 && preferredHeight > maxHeight )
			height = maxHeight;
		return new Dimension( preferredWidth, height );
	}
	
	/**
	 * Sets the maximum height of the label. Calling this method with a
	 * positive height makes sure that {@link #getPreferredSize()} will never
	 * return a dimension with a height greater than **height**.
	 * 
	 * This should be used if the text can be so large that the layout is
	 * in danger.
	 * 
	 * @param height  Height limit for the preferred size of the label.
	 */
	public void setHeightLimit( int height ) {
		maxHeight = height;
	}
	
	/**
	 * Sets the background color.
	 */
	@Override
	public void setBackground( Color color ) {
		
		if ( null == label )
			return;
		
		label.setBackground( color );
		label.setOpaque( true );
	}
	
	/**
	 * This method is overridden in order to show no border at all.
	 */
	@Override
	public void setBorder( Border border ) {
		// nothing to do
	}
}
