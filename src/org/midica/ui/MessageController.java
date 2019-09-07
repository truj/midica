/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This class is used as an {@link ActionListener} for a {@link MessageView} window.
 * 
 * @author Jan Trukenmüller
 */
public class MessageController implements ActionListener {
	
	private MessageView view = null;
	
	/**
	 * Creates a new object of this class.
	 * 
	 * @param view    Message window.
	 */
	public MessageController( MessageView view ) {
		this.view = view;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		
		if (MessageView.CMD_CLOSE.equals(cmd)) {
			view.close();
		}
	}
}
