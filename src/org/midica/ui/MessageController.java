/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * This class is used as a {@link WindowListener} for a {@link MessageView} window.
 * 
 * @author Jan Trukenmüller
 */
public class MessageController implements WindowListener {
	
	private MessageView view = null;
	
	/**
	 * Creates a new object of this class.
	 * 
	 * @param view    Message window.
	 */
	public MessageController( MessageView view ) {
		this.view = view;
	}
	
	/**
	 * Adds key bindings in the window.
	 */
	public void windowActivated( WindowEvent e ) {
		view.addKeyBindings();
	}
	
	/**
	 * Removes key bindings in the window.
	 */
	public void windowClosed( WindowEvent e ) {
		view.removeKeyBindings();
	}
	
	/**
	 * Removes key bindings in the window.
	 */
	public void windowClosing( WindowEvent e ) {
		view.removeKeyBindings();
	}
	
	/**
	 * Removes key bindings in the window.
	 */
	public void windowDeactivated( WindowEvent e ) {
		view.removeKeyBindings();
	}
	
	/**
	 * Does nothing.
	 */
	public void windowDeiconified( WindowEvent e ) {
	}
	
	/**
	 * Does nothing.
	 */
	public void windowIconified( WindowEvent e ) {
	}
	
	/**
	 * Does nothing.
	 */
	public void windowOpened( WindowEvent e ) {
	}
}
