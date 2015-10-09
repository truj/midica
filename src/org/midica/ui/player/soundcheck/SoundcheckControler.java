/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.player.soundcheck;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JComboBox;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.midica.file.NamedInteger;
import org.midica.midi.MidiDevices;
import org.midica.ui.player.PlayerView;


/**
 * This class provides the controller and event listener for the soundcheck
 * window. It's implemented as a singleton.
 * 
 * @author Jan Trukenmüller
 */
public class SoundcheckControler implements ActionListener, ChangeListener, MouseWheelListener, DocumentListener, WindowListener {
	
	private static SoundcheckControler controller = null;
	private static SoundcheckView      view        = null;
	private static SoundcheckNoteModel noteModel   = null;
	
	/**
	 * The actual constructor - private in order to enforce singleton behaviour.
	 */
	private SoundcheckControler() {
	}
	
	/**
	 * Creates a {@link SoundcheckControler} object by calling the private
	 * constructor, if not yet done. Returns the object.
	 * 
	 * This method is called from outside instead of the constructor itself
	 * in order to ensure the singleton behaviour.
	 * 
	 * @param scView       Soundcheck window.
	 * @param scNoteModel  Note model.
	 * @return a singleton {@link SoundcheckControler} object.
	 */
	public static SoundcheckControler getControler( SoundcheckView scView, SoundcheckNoteModel scNoteModel ) {
		view      = scView;
		noteModel = scNoteModel;
		if ( null == controller )
			controller = new SoundcheckControler();
		return controller;
	}
	
	/**
	 * Returns the singleton {@link SoundcheckControler} object.
	 * 
	 * @return {@link SoundcheckControler} object.
	 */
	public static SoundcheckControler getControler() {
		return controller;
	}
	
	/**
	 * Action listener for the soundcheck window.
	 * 
	 * Handles:
	 * 
	 * - clicks on the play button
	 * - selecting a checkbox item
	 * - pressing **Enter** in one of the text fields
	 * 
	 * @param e The event to be handled.
	 */
	@Override
	public void actionPerformed( ActionEvent e ) {
		String    cmd       = e.getActionCommand();
		Component component = (Component) e.getSource();
		
		// button clicked
		if ( SoundcheckView.CMD_PLAY.equals(cmd) ) {
			play();
		}
		
		// channel selected
		else if ( SoundcheckView.CMD_CHANNEL.equals(cmd) ) {
			NamedInteger option = (NamedInteger) ( (JComboBox<?>) component ).getSelectedItem();
			int channel = option.value;
			if ( 9 == channel ) {
				noteModel.setPercussion( true );
				view.toggleInstruments( false );
			}
			else {
				noteModel.setPercussion( false );
				view.toggleInstruments( true );
			}
			
			// Remove the action listener from the note combobox.
			view.toggleNoteListener( false );
			// Rebuild the note combobox's options.
			noteModel.init();
			// Add the action listener again.
			view.toggleNoteListener( true );
			
			view.pressPlayButton();
		}
		
		// instrument selected
		else if ( SoundcheckView.CMD_INSTR.equals(cmd) ) {
			view.pressPlayButton();
		}
		
		// note or percussion selected
		else if ( SoundcheckView.CMD_NOTE.equals(cmd) ) {
			view.pressPlayButton();
		}
		
		// text field entered (volume or duration field)
		else if ( component instanceof JTextField ) {
			view.pressPlayButton();
		}
	}
	
	/**
	 * Plays the customized sound.
	 */
	private void play() {
		try {
			// check volume and duration field, set field color, and set the volume slider
			checkVolumeField();   // throws NumberFormatException
			checkDurationField(); // throws NumberFormatException
			
			int channel  = view.getChannel();
			int instrNum = view.getInstrument();
			int note     = view.getNote();
			int volume   = view.getVolume();
			int duration = view.getDurationFromField(); // throws NumberFormatException
			
			MidiDevices.doSoundcheck( instrNum, channel, note, volume, duration );
		}
		catch( NumberFormatException e ) {
		}
	}
	
	/**
	 * Checks if the volume textfield contains a usable string.
	 * 
	 * If the check succeeds: sets the volume slider according to the
	 * volume from the text field.
	 * 
	 * @throws NumberFormatException if the field cannot be used as a volume value.
	 */
	private void checkVolumeField() throws NumberFormatException {
		byte volume = view.getVolumeFromField(); // throws NumberFormatException
		if ( volume < PlayerView.VOL_MIN || volume > PlayerView.VOL_MAX )
			throw new NumberFormatException();
		view.setVolume( volume );
	}
	
	/**
	 * Checks if the duration textfield contains a usable string.
	 * 
	 * @throws NumberFormatException if the field cannot be used as a duration value.
	 */
	private void checkDurationField() throws NumberFormatException {
		int duration = view.getDurationFromField(); // throws NumberFormatException
		if ( duration < SoundcheckView.MIN_DURATION || duration > SoundcheckView.MAX_DURATION )
			throw new NumberFormatException();
		view.setTextFieldColor( SoundcheckView.NAME_DURATION, PlayerView.COLOR_NORMAL );
	}
	
	/**
	 * Handles volume slider changes via mouse clicks.
	 * Sets the volume text field's content to the new value.
	 * 
	 * @param e The event to be handled.
	 */
	@Override
	public void stateChanged( ChangeEvent e ) {
		String name = ((Component) e.getSource()).getName();
		
		// handle volume slider changes
		if ( SoundcheckView.NAME_VOLUME.equals(name) ) {
			
			// only react if it's moved manually
			if ( ! view.isVolumeSliderAdjusting() )
				return;
			byte volume = (byte) ( (JSlider) e.getSource() ).getValue();
			view.setVolume( volume );
		}
	}
	
	/**
	 * Handles volume slider scrolls with the mouse wheel.
	 * Sets the volume text field's content to the new value.
	 * 
	 * @param e The event to be handled.
	 */
	@Override
	public void mouseWheelMoved( MouseWheelEvent e ) {
		String name     = ( (Component) e.getSource() ).getName();
		int amount      = e.getWheelRotation();
		int sliderTicks = ( (JSlider) e.getSource() ).getValue();
		
		// handle volume slider scrolls
		if ( SoundcheckView.NAME_VOLUME.equals(name) ) {
			// get and check new slider state
			sliderTicks -= amount * PlayerView.VOL_SCROLL;
			if ( sliderTicks < PlayerView.VOL_MIN )
				sliderTicks = PlayerView.VOL_MIN;
			else if ( sliderTicks > PlayerView.VOL_MAX )
				sliderTicks = PlayerView.VOL_MAX;
			
			// set new slider state and update text field
			view.setVolume( (byte) sliderTicks );
		}
	}
	
	/**
	 * Called if one of the text fields has been changed.
	 * 
	 * Passes the event to {@link #handleTextFieldChanges(DocumentEvent)}
	 * in order to check the content and adjust the background color.
	 * 
	 * @param e The event to be handled.
	 */
	@Override
	public void changedUpdate( DocumentEvent e ) {
		handleTextFieldChanges( e );
	}
	
	/**
	 * Called if one of the text fields has been changed.
	 * 
	 * Passes the event to {@link #handleTextFieldChanges(DocumentEvent)}
	 * in order to check the content and adjust the background color.
	 * 
	 * @param e The event to be handled.
	 */
	@Override
	public void insertUpdate( DocumentEvent e ) {
		handleTextFieldChanges( e );
	}
	
	/**
	 * Called if one of the text fields has been changed.
	 * 
	 * Passes the event to {@link #handleTextFieldChanges(DocumentEvent)}
	 * in order to check the content and adjust the background color.
	 * 
	 * @param e The event to be handled.
	 */
	@Override
	public void removeUpdate( DocumentEvent e ) {
		handleTextFieldChanges( e );
	}
	
	/**
	 * Called if a text field value changes by one of the according
	 * event handlers.
	 * Checks the content of the changed field and adjusts the background
	 * color according to the check result.
	 * 
	 * @param e The event to be handled.
	 */
	private void handleTextFieldChanges( DocumentEvent e ) {
		Document doc  = e.getDocument();
		String   name = doc.getProperty( "name" ).toString();
		
		try {
			String text = doc.getText( 0, doc.getLength() );
			
			// volume field changed
			if ( SoundcheckView.NAME_VOLUME.equals(name) ) {
				byte volume = Byte.parseByte( text );
				if ( volume < PlayerView.VOL_MIN || volume > PlayerView.VOL_MAX )
					throw new NumberFormatException();
			}
			else if ( SoundcheckView.NAME_DURATION.equals(name) ) {
				int duration = Integer.parseInt( text );
				if ( duration < SoundcheckView.MIN_DURATION || duration > SoundcheckView.MAX_DURATION )
					throw new NumberFormatException();
			}
			
			// no exception yet, so the field input is ok
			view.setTextFieldColor( name, PlayerView.COLOR_OK );
		}
		catch ( NumberFormatException ex ) {
			view.setTextFieldColor( name, PlayerView.COLOR_ERROR );
		}
		catch ( BadLocationException ex ) {
		}
	}
	
	/**
	 * Adds key bindings to the soundcheck window.
	 */
	@Override
	public void windowActivated( WindowEvent e ) {
		view.addKeyBindings();
	}
	
	/**
	 * Removes key bindings from the soundcheck window.
	 */
	@Override
	public void windowClosed( WindowEvent e ) {
		view.removeKeyBindings();
	}
	
	/**
	 * Removes key bindings from the soundcheck window.
	 */
	@Override
	public void windowClosing( WindowEvent e ) {
		view.removeKeyBindings();
	}
	
	/**
	 * Removes key bindings from the soundcheck window.
	 */
	@Override
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
