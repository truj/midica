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
	 * Obtains and returns information from the currently loaded soundfont.
	 * 
	 * @return soundfont information.
	 */
	public HashMap<String, String> getSoundfontInfo() {
		HashMap<String, String> info = new HashMap<String, String>();
		
		info.put( "file", Midica.uiController.getView().getChosenSoundfontFileLbl().getText() );
		
		Soundbank soundfont = MidiDevices.getSoundfont();
		if ( soundfont != null ) {
			// soundfont available
			info.put( "name",        soundfont.getName()        );
			info.put( "version",     soundfont.getVersion()     );
			info.put( "vendor",      soundfont.getVendor()      );
			info.put( "description", soundfont.getDescription() );
		}
		else {
			// no soundfont loaded
			info.put( "name",        "-" );
			info.put( "version",     "-" );
			info.put( "vendor",      "-" );
			info.put( "description", "-" );
		}
		
		// TODO: delete
//		SoundbankResource[] resources = soundfont.getResources();
//		for ( int i=0; i < resources.length; i++ ) {
//			Dumper.print( i + " : " + resources[ i ] );
//		}
//		Dumper.print( resources.length );
//		
//		Instrument[] instruments = soundfont.getInstruments();
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
