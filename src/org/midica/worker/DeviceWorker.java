/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.worker;

import org.midica.midi.MidiDevices;
import org.midica.ui.player.PlayerController;


/**
 * This class is used to setup the MIDI devices in the background while a
 * {@link WaitView} is shown.
 * 
 * Setting up the MIDI devices may be time consuming because it includes
 * loading a soundbank file and that file may be large.
 * 
 * The worker is executed in the background before the (blocking)
 * setVisible() method of the (modal) waiting dialog is called.
 * That causes the execution of {@link #doInBackground()} that sets up the
 * devices.
 * 
 * After the work is finished, {@link MidicaWorker#done()} is called and
 * closes the waiting dialog.
 * 
 * @author Jan Trukenmüller
 */
public class DeviceWorker extends MidicaWorker {
	
	private PlayerController playerController = null;
	
	/**
	 * Creates a worker that sets up MIDI devices in the background while
	 * a waiting dialog is shown.
	 * 
	 * @param view              The waiting dialog.
	 * @param playerController  The controller class for the player.
	 */
	public DeviceWorker(WaitView view, PlayerController playerController) {
		super(view);
		this.playerController = playerController;
	}
	
	/**
	 * Sets up the MIDI devices in the background.
	 * This method is executed after calling {@link #execute()}.
	 * 
	 * @return any caught exception or **null** if no exception is caught.
	 */
	@Override
	protected Exception doInBackground() {
		// parse
		Exception deviceException = null;
		try {
			MidiDevices.setupDevices(playerController);
		}
		catch (Exception e) {
			deviceException = e;
		}
		
		return deviceException;
	}
}
