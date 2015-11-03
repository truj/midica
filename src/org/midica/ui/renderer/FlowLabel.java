/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.renderer;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;

/**
 * This class provides a replacement for a {@link JLabel} that enables smoother
 * word wrapping of long texts and scrollbars when needed.
 * 
 * It's actually a {@link JScrollPane} containing a {@link JTextField} that's
 * changed modified in order to look more or less like a {@link JLabel}.
 * 
 * @author Jan Trukenmüller
 */
public class FlowLabel extends JScrollPane {
	
    private static final long serialVersionUID = 1L;

	/**
	 * Creates a new, empty {@link FlowLabel}.
	 */
	public FlowLabel( String content ) {
		
		// Create the label inside the scroll pane.
		JTextArea label = new JTextArea( content );
		
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
	}
	
	/**
	 * This method is overridden in order to show no border at all.
	 */
	@Override
	public void setBorder( Border border ) {
		// nothing to do
	}
}
