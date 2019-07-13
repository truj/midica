/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui;

import java.awt.KeyEventPostProcessor;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.WindowListener;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.midica.ui.widget.MidicaButton;

/**
 * This class provides a modal window belonging to a parent window.
 * 
 * It provides the following key bindings that can be used to close the window:
 * 
 * - Enter
 * - ESC
 * - Space
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
	
	protected MidicaButton          closeButton  = null;
	private   KeyEventPostProcessor keyProcessor = null;
	
	/**
	 * Creates a new modal message window.
	 * Creates a new {@link MessageController} object and adds it as a {@link WindowListener}.
	 * 
	 * @param view     Parent window (Player).
	 * @param title    Window title.
	 */
	public MessageView(JDialog view, String title) {
		super(view, title, true);
		addWindowListener( new MessageController(this) );
	}
	
	/**
	 * Creates a new modal message window.
	 * Creates a new {@link MessageController} object and adds it as a {@link WindowListener}.
	 * 
	 * @param view     Parent window (main window).
	 * @param title    Window title.
	 */
	public MessageView(JFrame view, String title) {
		super(view, title, true);
		addWindowListener( new MessageController(this) );
	}
	
	/**
	 * Closes the window.
	 */
	public void close() {
		setVisible( false );
		dispose();
	}
	
	/**
	 * Adds the key bindings that can be used to close the window:
	 * 
	 * - Enter
	 * - ESC
	 * - Space
	 * 
	 * This is called when the window is activated.
	 */
	public void addKeyBindings() {
		
		if ( null == keyProcessor ) {
			keyProcessor = new KeyEventPostProcessor() {
				public boolean postProcessKeyEvent( KeyEvent e ) {
					if ( KeyEvent.KEY_PRESSED == e.getID() ) {
						switch ( e.getKeyCode() ) {
						
						case KeyEvent.VK_ENTER:
							closeButton.doClick();
							break;
							
						case KeyEvent.VK_ESCAPE:
							closeButton.doClick();
							break;
							
						case KeyEvent.VK_SPACE:
							closeButton.doClick();
							break;
							
						default:
							break;
						}
					}
					return e.isConsumed();
				}
			};
		}
		
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventPostProcessor( keyProcessor );
	}
	
	/**
	 * Removes all key bindings.
	 * 
	 * This is called when the window is closed.
	 */
	public void removeKeyBindings() {
		KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventPostProcessor( keyProcessor );
	}
}
