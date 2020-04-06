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

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.midica.config.Dict;
import org.midica.config.Laf;

/**
 * This class provides a window for error messages.
 * 
 * @author Jan Trukenmüller
 */
public class ErrorMsgView extends MessageView {
	
	private static final long serialVersionUID = 1L;
	
	private Container content = null;
	
	/**
	 * Creates a new error message window.
	 * The window is not visible by default and must be initialized and set visible via init().
	 * 
	 * @param owner    The parent window (Player).
	 */
	public ErrorMsgView(JDialog owner) {
		super(owner, Dict.get(Dict.TITLE_ERROR));
	}
	
	/**
	 * Creates a new error message window.
	 * The window is not visible by default and must be initialized and set visible via init().
	 * 
	 * @param owner    The parent window (main window).
	 */
	public ErrorMsgView(JFrame owner) {
		super(owner, Dict.get(Dict.TITLE_ERROR));
	}
	
	/**
	 * Initializes the error message window, writes the error message and shows the window.
	 * 
	 * @param msg    Error message.
	 */
	public void init(String msg) {
		// content
		content = getContentPane();
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		content.setLayout(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.BOTH;
		constraints.insets     = Laf.INSETS_NWE;
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 1;
		constraints.weighty    = 1;
		constraints.anchor     = GridBagConstraints.NORTHWEST;
		
		// error message
		Container scrollableMsg = createScrollableMsg(msg);
		content.add(scrollableMsg, constraints);
		
		// close button
		constraints.gridy++;
		constraints.fill    = GridBagConstraints.NONE;
		constraints.anchor  = GridBagConstraints.CENTER;
		constraints.insets  = Laf.INSETS_SWE;
		constraints.weightx = 0;
		constraints.weighty = 0;
		content.add(createCloseButton(), constraints);
		super.addKeyBindings();
		
		pack();
		setVisible(true);
	}
	
	/**
	 * Creates a scrollable area containing the message.
	 * 
	 * @param msg  the message to be displayed
	 * @return the created area.
	 */
	private Container createScrollableMsg(String msg) {
		
		// make the area scrollable
		JPanel area = new JPanel();
		area.setLayout(new GridBagLayout());
		JScrollPane scrollPane = new JScrollPane(area);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		
		// layout
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.NONE;
		constraints.insets     = Laf.INSETS_IN;
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 1;
		constraints.weighty    = 1;
		constraints.anchor     = GridBagConstraints.NORTHWEST;
		
		// add message
		JLabel lblMsg = new JLabel(msg);
		lblMsg.setLayout(new GridBagLayout());
		area.add(lblMsg, constraints);
		
		return scrollPane;
	}
}
