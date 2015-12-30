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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.TreeMap;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JList;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.midica.midi.MidiDevices;
import org.midica.ui.player.PlayerView;


/**
 * This class provides the controller and event listener for the soundcheck
 * window. It's implemented as a singleton.
 * 
 * @author Jan Trukenmüller
 */
public class SoundcheckController implements ActionListener, ListSelectionListener, ItemListener, ChangeListener, MouseWheelListener, DocumentListener, WindowListener {
	
	public static final int DEFAULT_NOTE_INDEX       = 48; // MIDI num 48 (chromatic instrumends start at 0)
	public static final int DEFAULT_PERCUSSION_INDEX = 11; // MIDI num 38 (percussion starts at 27)
	
	private static SoundcheckController      controller            = null;
	private static SoundcheckView            view                  = null;
	private static SoundcheckNoteModel       noteModel             = null;
	private static SoundcheckInstrumentModel instrumentModel       = null;
	private static TreeMap<Integer, Integer> selectedInstrumentRow = null;
	private static TreeMap<Boolean, Integer> selectedNoteIndex     = null;
	
	/**
	 * The actual constructor - private in order to enforce singleton behaviour.
	 */
	private SoundcheckController() {
	}
	
	/**
	 * Creates a {@link SoundcheckController} object by calling the private
	 * constructor, if not yet done. Returns the object.
	 * 
	 * This method is called from outside instead of the constructor itself
	 * in order to ensure the singleton behaviour.
	 * 
	 * @param scView       Soundcheck window.
	 * @param scNoteModel  Note combobox model.
	 * @param scInstrModel Instruments table model.
	 * @return a singleton {@link SoundcheckController} object.
	 */
	public static SoundcheckController getController( SoundcheckView scView, SoundcheckNoteModel scNoteModel, SoundcheckInstrumentModel scInstrModel ) {
		view            = scView;
		noteModel       = scNoteModel;
		instrumentModel = scInstrModel;
		
		// initialize instrument row selection for each channel
		selectedInstrumentRow = new TreeMap<Integer, Integer>();
		for ( int channel = 0; channel < 16; channel++ )
			selectedInstrumentRow.put( channel, -1 );
		
		// initialize note row selection for chromatic and percussive instruments
		selectedNoteIndex = new TreeMap<Boolean, Integer>();
		selectedNoteIndex.put( false, DEFAULT_NOTE_INDEX       );
		selectedNoteIndex.put( true,  DEFAULT_PERCUSSION_INDEX );
		
		if ( null == controller )
			controller = new SoundcheckController();
		return controller;
	}
	
	/**
	 * Returns the singleton {@link SoundcheckController} object.
	 * 
	 * @return {@link SoundcheckController} object.
	 */
	public static SoundcheckController getController() {
		return controller;
	}
	
	/**
	 * Combobox selection listener for the channel combobox.
	 * Refills the instruments table based on the new channel and pre-selects
	 * the last selected row for this channel.
	 * 
	 * @param event selection change event
	 */
	@Override
	public void itemStateChanged( ItemEvent event ) {
		
		// Only listen to selection events and ignore deselection events.
		// Otherwise this would be evaluated twice for each selection.
		if ( ItemEvent.DESELECTED == event.getStateChange() )
			return;
		
		// refill instrument/drumkit table and note/percussion list
		fillInstrumentsTable();
		fillNoteList();
		
		// play the note
		view.pressPlayButton();
	}
	
	/**
	 * Action listener for the soundcheck window.
	 * 
	 * Handles:
	 * 
	 * - clicks on the play button
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
			
			int   channel  = view.getChannel();
			int   note     = view.getNote();
			int   volume   = view.getVolume();
			int   duration = view.getDurationFromField(); // throws NumberFormatException
			int[] instr    = view.getInstrument();
			int   instrNum = instr[ 0 ];
			
			// category selected or no note selected?
			if ( instrNum < 0 || note < 0 )
				return;
			
			// play the note
			MidiDevices.doSoundcheck( channel, instr, note, volume, duration );
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
	 * 
	 * @param e event
	 */
	@Override
	public void windowActivated( WindowEvent e ) {
		view.addKeyBindings();
	}
	
	/**
	 * Removes key bindings from the soundcheck window.
	 * 
	 * @param e event
	 */
	@Override
	public void windowClosed( WindowEvent e ) {
		view.removeKeyBindings();
	}
	
	/**
	 * Removes key bindings from the soundcheck window.
	 * 
	 * @param e event
	 */
	@Override
	public void windowClosing( WindowEvent e ) {
		view.removeKeyBindings();
	}
	
	/**
	 * Removes key bindings from the soundcheck window.
	 * 
	 * @param e event
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
	
	/**
	 * Fills the instruments table and the notes list (in the right order)
	 * and ensures that in both of them one element is selected.
	 * 
	 * @param e event
	 */
	@Override
	public void windowOpened( WindowEvent e ) {
		fillInstrumentsTable();
		fillNoteList();
	}
	
	/**
	 * Handles selections of instruments or notes.
	 * 
	 * Instruments or drumkits are selected from a {@link JTable}.
	 * Notes or percussion instruments are selected from a {@link JList}.
	 * 
	 * @param event Table row or list item selection event.
	 */
	@Override
	public void valueChanged( ListSelectionEvent event ) {
		
		// prevent to play the note twice after a selection by mouse click
		if ( event.getValueIsAdjusting() )
			return;
		
		// instrument or drumkit selected?
		Object source = event.getSource();
		if ( source instanceof DefaultListSelectionModel ) {
			
			// remember the selected row for the current channel
			int channel  = view.getChannel();
			int instrRow = view.getSelectedInstrumentRow();
			selectedInstrumentRow.put( channel, instrRow );
			
			// refill the note/percussion list
			fillNoteList();
		}
		
		// note or percussion instrument selected?
		else if ( source instanceof JList ) {
			
			// remember selected note/percussion
			boolean isPercussion = noteModel.getPercussion();
			selectedNoteIndex.put( isPercussion, view.getSelectedNoteIndex() );
		}
		
		// play the note
		view.pressPlayButton();
	}
	
	/**
	 * Returns the currently selected channel.
	 * 
	 * @return the currently selected channel
	 */
	public static int getChannel() {
		if ( null == view )
			return 0;
		return view.getChannel();
	}
	
	/**
	 * (Re)fills the instruments/drumkits table according to the
	 * currently selected channel.
	 */
	private void fillInstrumentsTable() {
		
		// get last selected note/percussion of the channel
		int lastSelectedRow = selectedInstrumentRow.get( view.getChannel() );
		
		// refill the table
		view.toggleInstrumentSelectionListener( false );  // remove listener
		instrumentModel.initList();                       // rebuild options
		view.setSelectedInstrumentRow( lastSelectedRow ); // select row
		view.toggleInstrumentSelectionListener( true );   // add listener
		view.scrollInstrumentTable( lastSelectedRow );    // scroll to selection
	}
	
	/**
	 * (Re)fills the note/percussion list according to the currently
	 * selected instrument type.
	 */
	private void fillNoteList() {
		
		// adjust mode (chromatic/percussive)
		boolean isPercussion = view.isDrumSelected();
		if (isPercussion)
			noteModel.setPercussion( true );
		else
			noteModel.setPercussion( false );
		
		// get last selected note/percussion of the new mode
		int lastSelNoteIdx = selectedNoteIndex.get( isPercussion );
		
		// refill note list
		view.toggleNoteListener( false );            // remove listener
		noteModel.init();                            // rebuild options
		view.setSelectedNoteIndex( lastSelNoteIdx ); // restore note selection
		view.toggleNoteListener( true );             // add listener
		view.scrollNoteList( lastSelNoteIdx );       // scroll to selection
	}
}
