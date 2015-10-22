/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.config;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashMap;

import javax.sound.midi.Soundbank;

import org.midica.Midica;
import org.midica.midi.MidiDevices;

/**
 * Controller for the config overview window.
 * This is only used to add or remove key bindings to/from the config view
 * because this view doesn't support more interaction.
 * 
 * @author Jan Trukenmüller
 */
public class ConfigController implements WindowListener {
	
	private ConfigView view = null;
	
	/**
	 * Creates a new instance of the controller for the given config view.
	 * This is called during the initialization of the config view.
	 * 
	 * @param view  config view to which the controller is connected.
	 */
	public ConfigController( ConfigView view ) {
		this.view = view;
	}
	
	/**
	 * Adds config view specific key bindings.
	 */
	public void windowActivated( WindowEvent e ) {
		view.addKeyBindings();
	}
	
	/**
	 * Removes config view specific key bindings.
	 */
	public void windowClosed( WindowEvent e ) {
		view.removeKeyBindings();
	}
	
	/**
	 * Removes config view specific key bindings.
	 */
	public void windowClosing( WindowEvent e ) {
		view.removeKeyBindings();
	}
	
	/**
	 * Removes config view specific key bindings.
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
	
	/**
	 * Obtains and returns information from the currently loaded soundbank.
	 * 
	 * @return soundbank information.
	 */
	public HashMap<String, String> getSoundbankInfo() {
		HashMap<String, String> info = new HashMap<String, String>();
		
		info.put( "file", Midica.uiController.getView().getChosenSoundbankFileLbl().getText() );
		
		Soundbank soundbank = MidiDevices.getSoundbank();
		if ( soundbank != null ) {
			// soundbank available
			info.put( "name",        soundbank.getName()        );
			info.put( "version",     soundbank.getVersion()     );
			info.put( "vendor",      soundbank.getVendor()      );
			info.put( "description", soundbank.getDescription() );
		}
		else {
			// no soundbank loaded
			info.put( "name",        "-" );
			info.put( "version",     "-" );
			info.put( "vendor",      "-" );
			info.put( "description", "-" );
		}
		
		// TODO: delete
//		SoundbankResource[] resources = soundbank.getResources();
//		for ( int i=0; i < resources.length; i++ ) {
//			Dumper.print( i + " : " + resources[ i ] );
//		}
//		Dumper.print( resources.length );
//		
//		Instrument[] instruments = soundbank.getInstruments();
//		for ( int i=0; i < instruments.length; i++ ) {
//			Instrument instr = instruments[ i ];
//			Patch      patch = instr.getPatch();
//			System.out.println( i + " : " + patch.getProgram() + "/" + patch.getBank() + " / " + instr.getName() );
//		}
//		Dumper.print( instruments.length );
//		
		
		return info;
	}
}
