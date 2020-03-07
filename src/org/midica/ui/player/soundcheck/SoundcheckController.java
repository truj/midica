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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.midica.config.Laf;
import org.midica.midi.MidiDevices;
import org.midica.ui.player.PlayerView;
import org.midica.ui.widget.MidicaSlider;

/**
 * This class provides the controller and event listener for the soundcheck
 * window. It's implemented as a singleton.
 * 
 * @author Jan Trukenmüller
 */
public class SoundcheckController implements ActionListener, ListSelectionListener, ItemListener, ChangeListener,
		MouseWheelListener, DocumentListener, WindowListener, RowSorterListener {
	
	private static final int DEFAULT_NOTE_NUM       = 60;
	private static final int DEFAULT_PERCUSSION_NUM = 11; // MIDI num 38 (percussion starts at 27)
	
	private static SoundcheckController                       controller            = null;
	private static SoundcheckView                             view                  = null;
	private static SoundcheckNoteModel                        noteModel             = null;
	private static SoundcheckInstrumentModel                  instrumentModel       = null;
	private static TreeMap<Integer, Integer>                  selectedInstrumentRow = null; // maps channel to instrument table model index
	private static TreeMap<Integer, TreeMap<Boolean,Integer>> selectedNoteRow       = null;
	
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
	public static SoundcheckController getController(SoundcheckView scView, SoundcheckNoteModel scNoteModel, SoundcheckInstrumentModel scInstrModel) {
		view            = scView;
		noteModel       = scNoteModel;
		instrumentModel = scInstrModel;
		
		// initialize instrument and note row selection for each channel
		selectedInstrumentRow = new TreeMap<>();
		selectedNoteRow       = new TreeMap<>();
		for (int channel = 0; channel < 16; channel++) {
			selectedInstrumentRow.put(channel, -1);
			TreeMap<Boolean, Integer> noteRow = new TreeMap<>();
			noteRow.put(false, DEFAULT_NOTE_NUM);
			noteRow.put(true,  DEFAULT_PERCUSSION_NUM);
			selectedNoteRow.put(channel, noteRow);
		}
		
		if (null == controller)
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
	public void itemStateChanged(ItemEvent event) {
		
		// Only listen to selection events and ignore deselection events.
		// Otherwise this would be evaluated twice for each selection.
		if (ItemEvent.DESELECTED == event.getStateChange())
			return;
		
		// refill instrument/drumkit table and note/percussion table
		fillInstrumentsTable();
		fillNoteTable();
		
		// play the note
		view.pressPlayButton();
	}
	
	/**
	 * Action listener for the soundcheck window.
	 * 
	 * Handles:
	 * 
	 * - clicks on the play button
	 * - changes of the 'keep settings' checkbox
	 * - pressing **Enter** in one of the text fields
	 * 
	 * @param e The event to be handled.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		String    cmd       = e.getActionCommand();
		Component component = (Component) e.getSource();
		
		// button clicked
		if (SoundcheckView.CMD_PLAY.equals(cmd)) {
			play();
		}
		
		// keep checkbox changed
		else if (SoundcheckView.CMD_KEEP.equals(cmd)) {
			
			// checkbox was un-checked
			if ( ! view.mustKeepSettings() ) {
				MidiDevices.restoreChannelAfterSoundcheck(view.getChannel());
			}
		}
		
		// text field entered (volume/velocity/duration field)
		else if (component instanceof JTextField) {
			view.pressPlayButton();
		}
	}
	
	/**
	 * Scrolls to the currently selected instruments table row, if possible.
	 * Called when the table string filter or sorter has been changed.
	 * 
	 * @param e   the row sorter event
	 */
	@Override
	public void sorterChanged(RowSorterEvent e) {
		view.scrollInstrumentTable();
	}
	
	/**
	 * Returns the last (remembered) selected table model index of the given channel.
	 * 
	 * @param channel  The requested channel.
	 * @return last selected table (model) row.
	 */
	public static int getLastSelectedInstrumentTableRow(int channel) {
		return selectedInstrumentRow.get(channel);
	}
	
	/**
	 * Returns the currently (or last) selected instrument coordinates from the instruments
	 * combobox.
	 * 
	 * The returned value consists of the following parts:
	 * 
	 * - program number
	 * - bank MSB
	 * - bank LSB
	 * 
	 * If no instrument is selected, all these values are **-1**.
	 * 
	 * @return program number, bank MSB and bank LSB of the selected instrument.
	 */
	public int[] getInstrument() {
		
		int[] result = { -1, -1, -1 };
		
		// get channel
		int channel = view.getChannel();
		
		// get last selected table row
		int row = selectedInstrumentRow.get(channel);
		
		if (-1 == row)
			return result;
		ArrayList<HashMap<String, String>> instruments = instrumentModel.getInstruments();
		HashMap<String, String> instr = instruments.get(row);
		
		// real instrument/drumkit selected?
		if (instr.containsKey("program")) {
			result[ 0 ] = Integer.parseInt( instr.get("program")  );
			result[ 1 ] = Integer.parseInt( instr.get("bank_msb") );
			result[ 2 ] = Integer.parseInt( instr.get("bank_lsb") );
		}
		
		return result;
	}
	
	/**
	 * Returns the currently selected note (or percussion instrument) from
	 * the note/percussion table.
	 * 
	 * In case of an error, **-1** is returned.
	 * 
	 * @return the currently selected note or percussion number or **-1** on error.
	 */
	public int getNote() {
		
		// get last selected note row
		int row = selectedNoteRow.get(view.getChannel()).get(view.isDrumSelected());
		if (row < 0)
			return -1;
		
		// get note/percussion list
		ArrayList<Integer> list = noteModel.getList();
		if (null == list)
			return -1;
		
		// get MIDI number
		Integer num = list.get(row);
		if (null == num)
			return -1;
		
		return num;
	}
	
	/**
	 * Plays the customized sound.
	 */
	private void play() {
		try {
			// check volume/velocity/duration fields, set field color, and set the volume/velocity slider
			checkVolOrVelField("volume");   // throws NumberFormatException
			checkDurationField();           // throws NumberFormatException
			checkVolOrVelField("velocity"); // throws NumberFormatException
			
			int     channel  = view.getChannel();
			int     note     = getNote();
			byte    volume   = view.getVolume();
			int     velocity = view.getVelocity();
			int     duration = view.getDurationFromField(); // throws NumberFormatException
			int[]   instr    = getInstrument();
			int     instrNum = instr[ 0 ];
			boolean mustKeep = view.mustKeepSettings();
			
			// category selected or no note selected?
			if (instrNum < 0 || note < 0)
				return;
			
			// play the note
			Thread playThread = new Thread() {
				@Override
				public void run() {
					MidiDevices.doSoundcheck(channel, instr, note, volume, velocity, duration, mustKeep);
				}
			};
			playThread.start();
		}
		catch(NumberFormatException e) {
		}
	}
	
	/**
	 * Checks if the volume or velocity textfield contains a usable string.
	 * 
	 * If the check succeeds: sets the volume/velocity slider according to the
	 * value from the text field.
	 * 
	 * @param  type  **volume** or **velocity**, depending on the field to be checked.
	 * @throws NumberFormatException if the field cannot be used as a volume/velocity value.
	 */
	private void checkVolOrVelField(String type) throws NumberFormatException {
		
		// check field
		byte value;
		if ("volume".equals(type))
			value = view.getVolumeFromField(); // throws NumberFormatException
		else
			value = view.getVeloctiyFromField(); // throws NumberFormatException
		if (value < PlayerView.CH_VOL_MIN || value > PlayerView.CH_VOL_MAX)
			throw new NumberFormatException();
		
		// set slider
		if ("volume".equals(type))
			view.setVolume(value);
		else
			view.setVelocity(value);
	}
	
	/**
	 * Checks if the duration textfield contains a usable string.
	 * 
	 * @throws NumberFormatException if the field cannot be used as a duration value.
	 */
	private void checkDurationField() throws NumberFormatException {
		int duration = view.getDurationFromField(); // throws NumberFormatException
		if (duration < SoundcheckView.MIN_DURATION || duration > SoundcheckView.MAX_DURATION)
			throw new NumberFormatException();
		view.setTextFieldColor(SoundcheckView.NAME_DURATION, Laf.COLOR_NORMAL);
	}
	
	/**
	 * Handles volume or velocity slider changes via mouse clicks.
	 * Sets the volume or velocity text field's content to the new value.
	 * 
	 * @param e The event to be handled.
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		String name = ((Component) e.getSource()).getName();
		
		// volume slider?
		if (SoundcheckView.NAME_VOLUME.equals(name)) {
			
			// only react if it's moved manually
			if ( ! view.isVolumeSliderAdjusting() )
				return;
			byte volume = (byte) ((MidicaSlider) e.getSource()).getValue();
			view.setVolume(volume);
		}
		
		// velocity slider
		else if (SoundcheckView.NAME_VELOCITY.equals(name)) {
			
			// only react if it's moved manually
			if ( ! view.isVelocitySliderAdjusting() )
				return;
			byte velocity = (byte) ((MidicaSlider) e.getSource()).getValue();
			view.setVelocity(velocity);
		}
	}
	
	/**
	 * Handles volume or velocity slider scrolls with the mouse wheel.
	 * Sets the volume or velocity text field's content to the new value.
	 * 
	 * @param e The event to be handled.
	 */
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		String name     = ((Component) e.getSource()).getName();
		int amount      = e.getWheelRotation();
		int sliderTicks = ((MidicaSlider) e.getSource()).getValue();
		sliderTicks    -= amount * PlayerView.CH_VOL_SCROLL;
		
		// check new slider state
		if (sliderTicks < PlayerView.CH_VOL_MIN)
			sliderTicks = PlayerView.CH_VOL_MIN;
		else if (sliderTicks > PlayerView.CH_VOL_MAX)
			sliderTicks = PlayerView.CH_VOL_MAX;
		
		// set new slider state and update text field
		if (SoundcheckView.NAME_VOLUME.equals(name))
			view.setVolume((byte) sliderTicks);
		else if (SoundcheckView.NAME_VELOCITY.equals(name))
			view.setVelocity((byte) sliderTicks);
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
	public void changedUpdate(DocumentEvent e) {
		handleTextFieldChanges(e);
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
	public void insertUpdate(DocumentEvent e) {
		handleTextFieldChanges(e);
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
	public void removeUpdate(DocumentEvent e) {
		handleTextFieldChanges(e);
	}
	
	/**
	 * Called if a text field value changes by one of the according
	 * event handlers.
	 * Checks the content of the changed field and adjusts the background
	 * color according to the check result.
	 * 
	 * @param e The event to be handled.
	 */
	private void handleTextFieldChanges(DocumentEvent e) {
		Document doc  = e.getDocument();
		String   name = doc.getProperty("name").toString();
		
		try {
			String text = doc.getText(0, doc.getLength());
			
			// volume or velocity field changed
			if (SoundcheckView.NAME_VOLUME.equals(name) || SoundcheckView.NAME_VELOCITY.equals(name)) {
				byte value = Byte.parseByte(text);
				if (value < PlayerView.CH_VOL_MIN || value > PlayerView.CH_VOL_MAX)
					throw new NumberFormatException();
			}
			else if (SoundcheckView.NAME_DURATION.equals(name)) {
				int duration = Integer.parseInt(text);
				if (duration < SoundcheckView.MIN_DURATION || duration > SoundcheckView.MAX_DURATION)
					throw new NumberFormatException();
			}
			
			// no exception yet, so the field input is ok
			view.setTextFieldColor(name, Laf.COLOR_OK);
		}
		catch (NumberFormatException ex) {
			view.setTextFieldColor(name, Laf.COLOR_ERROR);
		}
		catch (BadLocationException ex) {
		}
	}
	
	@Override
	public void windowActivated(WindowEvent e) {
	}
	
	@Override
	public void windowClosed(WindowEvent e) {
	}
	
	@Override
	public void windowClosing(WindowEvent e) {
	}
	
	@Override
	public void windowDeactivated(WindowEvent e) {
	}
	
	@Override
	public void windowDeiconified(WindowEvent e) {
	}
	
	@Override
	public void windowIconified(WindowEvent e) {
	}
	
	/**
	 * Fills the instruments table and the notes list (in the right order)
	 * and ensures that in both of them one element is selected.
	 * 
	 * @param e event
	 */
	@Override
	public void windowOpened(WindowEvent e) {
		fillInstrumentsTable();
		fillNoteTable();
	}
	
	/**
	 * Handles selections of instruments or notes.
	 * 
	 * Instruments or drumkits are selected from a {@link org.midica.ui.widget.MidicaTable}.
	 * Notes or percussion instruments are selected from a {@link JList}.
	 * 
	 * @param event Table row or list item selection event.
	 */
	@Override
	public void valueChanged(ListSelectionEvent event) {
		
		// prevent to play the note twice after a selection by mouse click
		if (event.getValueIsAdjusting())
			return;
		
		// prevent to play the note, while the string table filter is shown
		// otherwise the play button would be pushed for each keyboard press
		if (view.isFilterLayerOpen())
			return;
		
		// instrument or drumkit selected?
		Object source = event.getSource();
		if (source == view.getListSelectionModelFromTable(true)) {
			
			// remember the (last) selected row for the current channel
			int channel  = view.getChannel();
			int instrRow = view.getSelectedInstrumentRow();
			if (instrRow >= 0)
				selectedInstrumentRow.put(channel, instrRow);
			
			// refill the note/percussion list
			fillNoteTable();
		}
		
		// note or percussion instrument selected?
		else if (source == view.getListSelectionModelFromTable(false)) {
			
			// remember selected note or percussion for the current channel
			int channel = view.getChannel();
			int row     = view.getSelectedNoteRow();
			TreeMap<Boolean, Integer> noteRowByType = selectedNoteRow.get(channel);
			noteRowByType.put(view.isDrumSelected(), row);
			selectedNoteRow.put(channel, noteRowByType);
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
		if (null == view)
			return 0;
		return view.getChannel();
	}
	
	/**
	 * (Re)fills the instruments/drumkits table according to the
	 * currently selected channel.
	 */
	private void fillInstrumentsTable() {
		
		// get last selected note/percussion of the channel
		int lastSelectedRow = selectedInstrumentRow.get(view.getChannel());
		
		// refill the table
		view.toggleInstrumentSelectionListener(false);  // remove listener
		instrumentModel.initList();                     // rebuild options
		view.setSelectedInstrumentRow(lastSelectedRow); // select row
		view.toggleInstrumentSelectionListener(true);   // add listener
		view.scrollInstrumentTable();                   // scroll to selection
	}
	
	/**
	 * (Re)fills the note/percussion table according to the currently
	 * selected instrument type.
	 */
	private void fillNoteTable() {
		
		// adjust mode (chromatic/percussive)
		boolean isPercussion = view.isDrumSelected();
		if (isPercussion)
			noteModel.setPercussion(true);
		else
			noteModel.setPercussion(false);
		
		// get last selected note/percussion of the new mode
		int lastSelRow = selectedNoteRow.get(view.getChannel()).get(isPercussion);
		
		// refill note list
		view.toggleNoteListener(false);      // remove listener
		noteModel.init();                    // rebuild options
		view.setSelectedNoteRow(lastSelRow); // restore note selection
		view.toggleNoteListener(true);       // add listener
		view.scrollNoteTable();              // scroll to selection
	}
}
