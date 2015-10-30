/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.info;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;


/**
 * Controller for the info window.
 * This is only used to add or remove key bindings to/from the info view
 * because this view doesn't support more interaction.
 * 
 * @author Jan Trukenmüller
 */
public class InfoController implements WindowListener {
	
	private InfoView view = null;
	
	/**
	 * Creates a new instance of the controller for the given info view.
	 * This is called during the initialization of the info view.
	 * 
	 * @param view  info view to which the controller is connected.
	 */
	public InfoController( InfoView view ) {
		this.view = view;
	}
	
	/**
	 * Adds info view specific key bindings.
	 */
	public void windowActivated( WindowEvent e ) {
		view.addKeyBindings();
	}
	
	/**
	 * Removes info view specific key bindings.
	 */
	public void windowClosed( WindowEvent e ) {
		view.removeKeyBindings();
	}
	
	/**
	 * Removes info view specific key bindings.
	 */
	public void windowClosing( WindowEvent e ) {
		view.removeKeyBindings();
	}
	
	/**
	 * Removes info view specific key bindings.
	 */
	public void windowDeactivated( WindowEvent e ) {
		view.removeKeyBindings();
	}
	
	@Override
	public void windowDeiconified( WindowEvent e ) {
	}
	
	@Override
	public void windowIconified( WindowEvent e ) {
	}
	
	@Override
	public void windowOpened( WindowEvent e ) {
	}
	
}
