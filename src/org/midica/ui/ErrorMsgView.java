/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

import org.midica.config.Dict;


/**
 * This class provides a window for error messages.
 * 
 * @author Jan Trukenmüller
 */
public class ErrorMsgView extends MessageView {
	
	private ActionListener controller  = null;
	private Container      content     = null;
	
	/**
	 * Creates a new error message window.
	 * The window is not visible by default and must be initialized and set visible via init().
	 * 
	 * @param view          The parent window (owner).
	 * @param controller    The controller that acts as an {@link ActionListener} for this
	 *                      window.
	 */
	public ErrorMsgView( JDialog view, ActionListener controller ) {
		super( view, Dict.get(Dict.TITLE_ERROR) );
		this.controller = controller;
	}
	
	/**
	 * Initializes the error message window, writes the error message and shows the window.
	 * 
	 * @param msg    Error message.
	 */
	public void init( String msg ) {
		// content
		content = getContentPane();
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		content.setLayout( layout );
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill   = GridBagConstraints.BOTH;
		constraints.insets = new Insets( 2, 2, 2, 2 );
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 0;
		constraints.weighty    = 0;
		
		// error message
		JLabel label = new JLabel( msg );
		content.add( label, constraints );
		
		// close button
		constraints.gridy++;
		closeButton = new JButton( Dict.get(Dict.CLOSE) );
		closeButton.setActionCommand( MessageView.CMD_CLOSE );
		closeButton.addActionListener( controller );
		closeButton.requestFocusInWindow();
		content.add( closeButton, constraints );
		
		pack();
		setVisible( true );
	}
}
