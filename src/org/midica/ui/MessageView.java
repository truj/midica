/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui;

import java.awt.event.ActionListener;
import java.awt.event.WindowListener;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.midica.config.Dict;
import org.midica.config.KeyBindingManager;
import org.midica.ui.file.ExportResultView;
import org.midica.ui.widget.MidicaButton;

/**
 * This class provides a modal window belonging to a parent window.
 * 
 * It provides a close button and key bindings that can be used to close the window.
 * 
 * Sub classes are:
 * 
 * - {@link ErrorMsgView}: for showing error messages
 * - {@link ExportResultView}: for showing export results
 * 
 * @author Jan Trukenmüller
 */
public abstract class MessageView extends JDialog {

	private static final long serialVersionUID = 1L;
	
	public static final String CMD_CLOSE = "close_message";
	
	protected ActionListener    controller        = null;
	protected MidicaButton      closeButton       = null;
	private   KeyBindingManager keyBindingManager = null;
	
	/**
	 * Creates a new modal message window.
	 * Creates a new {@link MessageController} object and adds it as a {@link WindowListener}.
	 * 
	 * @param view     Parent window (Player).
	 * @param title    Window title.
	 */
	public MessageView(JDialog view, String title) {
		super(view, title, true);
		controller = new MessageController(this);
	}
	
	/**
	 * Creates a new modal message window.
	 * 
	 * @param view     Parent window (main window).
	 * @param title    Window title.
	 */
	public MessageView(JFrame view, String title) {
		super(view, title, true);
		controller = new MessageController(this);
	}
	
	/**
	 * Closes the window.
	 */
	public void close() {
		setVisible(false);
		dispose();
	}
	
	/**
	 * Adds the key bindings that can be used to close the window.
	 */
	public void addKeyBindings() {
		
		// reset everything
		keyBindingManager = new KeyBindingManager(this, this.getRootPane());
		
		// add key bindings to normal buttons
		keyBindingManager.addBindingsForButton(this.closeButton, Dict.KEY_MSG_CLOSE);
		
		// set input and action maps
		keyBindingManager.postprocess();
	}
	
	/**
	 * Creates and returns a close button for the view and connects it with the action controller.
	 * 
	 * @return the created close button.
	 */
	protected MidicaButton createCloseButton() {
		closeButton = new MidicaButton(Dict.get(Dict.CLOSE));
		closeButton.setActionCommand(MessageView.CMD_CLOSE);
		closeButton.addActionListener(controller);
		closeButton.requestFocusInWindow();
		
		return closeButton;
	}
}
