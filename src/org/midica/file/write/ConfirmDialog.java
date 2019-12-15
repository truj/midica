/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file.write;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.midica.config.Dict;
import org.midica.ui.widget.MidicaButton;

/**
 * This class provides a confirm dialog implementing the same look and feel as the other components.
 * 
 * It uses {@link JOptionPane} to create the dialog.
 * 
 * @author Jan Trukenmüller
 */
public class ConfirmDialog {
	
	private static int result = JOptionPane.CANCEL_OPTION;
	
	/**
	 * Creates a new confirm dialog with a YES button and a NO button.
	 * 
	 * Returns a result value, according to the user's action:
	 * 
	 * - If the YES button was pressed, returns {@link JOptionPane#YES_OPTION}.
	 * - If the NO button was pressed, returns {@link JOptionPane#NO_OPTION}.
	 * - If no button was pressed (dialog closed by pressing ESC or otherwise), returns {@link JOptionPane#CANCEL_OPTION}.
	 * 
	 * @param parent     the parent frame
	 * @param message    the question to be asked
	 * @param title      the window title
	 * @return a value according to the clicked button, like described above.
	 */
	public static int confirm(JFrame parent, String message, String title) {
		
		// reset the result
		result = JOptionPane.CANCEL_OPTION;
		
		// create buttons
		MidicaButton   yesButton = new MidicaButton( Dict.get(Dict.CONFIRM_DIALOG_YES), true  );
		MidicaButton   noButton  = new MidicaButton( Dict.get(Dict.CONFIRM_DIALOG_NO),  false );
		MidicaButton[] buttons   = {yesButton, noButton};
		
		// listener for the YES button
		yesButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				result = JOptionPane.YES_OPTION;
				closeDialog(e);
			}
		});
		
		// listener for the NO button
		noButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				result = JOptionPane.NO_OPTION;
				closeDialog(e);
			}
		});
		
		// show the dialog
		JOptionPane.showOptionDialog(
			parent,
			message,
			title,
			JOptionPane.YES_NO_OPTION,
			JOptionPane.QUESTION_MESSAGE,
			null,
			buttons,
			null
		);
		
		return result;
    }
	
	/**
	 * Close the dialog after a button has been pressed.
	 * 
	 * @param e  action event created by pressing the button
	 */
	private static void closeDialog(ActionEvent e) {
		Window dialog = SwingUtilities.getWindowAncestor( (Component) e.getSource() );
		if (dialog != null)
			dialog.setVisible(false);
	}
}
