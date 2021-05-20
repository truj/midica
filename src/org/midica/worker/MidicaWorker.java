/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.worker;

import javax.swing.SwingWorker;

/**
 * This class is the base class for workers that perform time-consuming
 * actions in the background while a {@link WaitView} is shown.
 * 
 * The derived worker is executed in the background before the (blocking)
 * setVisible() method of the (modal) waiting dialog is called.
 * That causes the execution of {@link #doInBackground()} which does the actual
 * work.
 * 
 * After the background work is finished, {@link #done()} is called and closes
 * the waiting dialog.
 * 
 * @author Jan Trukenmüller
 */
public abstract class MidicaWorker extends SwingWorker<Exception, Void> {
	
	private WaitView view = null;
	
	/**
	 * Creates a worker for time-consuming background work while
	 * a waiting dialog is shown.
	 * 
	 * @param view  The waiting dialog
	 */
	public MidicaWorker(WaitView view) {
		this.view = view;
	}
	
	/**
	 * Does the background work.
	 * 
	 * @return the exception caught during the work or **null** if no exception
	 *         is caught
	 */
	@Override
	protected abstract Exception doInBackground();
	
	/**
	 * Closes the waiting dialog.
	 * This method is executed if the background work is finished.
	 */
	@Override
	protected void done() {
		view.close();
	}
}
