/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.worker;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.JLabel;

import org.midica.config.Cli;
import org.midica.config.Dict;
import org.midica.config.Laf;


/**
 * This class provides a 'please wait' window for time-consuming processes.
 * The window cannot be closed by the user. It's closed after the
 * time-consuming process is finished by calling {@link #close()};
 * 
 * @author Jan Trukenmüller
 */
public class WaitView extends JDialog {

	private static final long serialVersionUID = 1L;
	
	public static final int MIN_WIDTH  = 250;
	public static final int MIN_HEIGHT = 150;
	
	private Container content = null;
	
	/**
	 * Creates a new 'please wait' window.
	 * The window is not visible by default and must be initialized and set visible via init().
	 * 
	 * @param owner    The parent window (owner).
	 */
	public WaitView(Window owner) {
		super(owner, Dict.get(Dict.TITLE_WAIT));
		setPreferredSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		
		// don't let the user close this window
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		
		// show the window in the center of the parent window
		pack();
		setLocationRelativeTo(owner);
	}
	
	/**
	 * Initializes the window, writes the message and shows the window.
	 * 
	 * @param msg    Message to be displayed.
	 */
	public void init(String msg) {
		// content
		content = getContentPane();
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		content.setLayout(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.BOTH;
		constraints.insets     = Laf.INSETS_ALL;
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 0;
		constraints.weighty    = 0;
		
		// message
		JLabel label = new JLabel(msg);
		content.add(label, constraints);
		
		if (Cli.isCliMode) {
			return;
		}
		
		// show the window
		pack();
		setModal(true);
		setVisible(true); // this method is blocking
	}
	
	/**
	 * Closes the window.
	 */
	public void close() {
		setVisible(false);
		dispose();
	}
}
