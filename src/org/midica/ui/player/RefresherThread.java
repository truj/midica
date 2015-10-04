/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.player;

/**
 * The thread defined by this class is responsible to for refreshing the progress bar while
 * a midi stream is being played.
 * 
 * @author Jan Trukenmüller
 */
public class RefresherThread extends Thread {
	
	/** Waiting time in milli seconds between the refreshes of the progress bar. */
	public static final int WAITING_TIME = 50;
	
	private PlayerController controller = null;
	private volatile boolean isAlive    = true;
	
	/**
	 * Creates the refresher thread when the player window is opened.
	 * 
	 * @param controller  Event listener object for the player window.
	 */
	public RefresherThread( PlayerController controller ) {
		this.controller = controller;
	}
	
	/**
	 * Refreshes the player window's progress bar in an endless loop every 50 milliseconds.
	 * 
	 * Stops refreshing if {@link #die()} is called.
	 */
	@Override
	public void run() {
		while (isAlive) {
			controller.refreshProgressBar();
			try {
				sleep( WAITING_TIME );
			}
			catch ( InterruptedException e ) {
			}
		}
		try {
			finalize();
		}
		catch ( Throwable e ) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Sets a flag to make the {@link #run()} method stop running.
	 */
	public void die() {
		isAlive = false;
	}
}
