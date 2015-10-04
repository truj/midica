/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.player.soundcheck;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyEventPostProcessor;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JTextField;

import org.midica.config.Dict;
import org.midica.config.InstrumentElement;
import org.midica.file.NamedInteger;
import org.midica.midi.MidiDevices;
import org.midica.ui.SliderHelper;
import org.midica.ui.player.PlayerView;

/**
 * This class defines the UI for the soundcheck window.
 * 
 * @author Jan Trukenmüller
 */
public class SoundcheckView extends JDialog {
	
	public static final Dimension DIM_TEXT_FIELD   = new Dimension( 60, 20 );
	public static final int       VOL_LABEL        =    30;
	public static final int       DEFAULT_DURATION =   300;
	public static final int       MIN_DURATION     =     0;
	public static final int       MAX_DURATION     = 10000;
	
	public static final String CMD_PLAY      = "cmd_play";
	public static final String CMD_CHANNEL   = "cmd_channel";
	public static final String CMD_INSTR     = "cmd_instr";
	public static final String CMD_NOTE      = "cmd_note";
	public static final String NAME_VOLUME   = "name_volume";
	public static final String NAME_DURATION = "name_duration";
	
	private JComboBox<NamedInteger>      cbxChannel    = null;
	private JComboBox<InstrumentElement> cbxInstrument = null;
	private JComboBox<NamedInteger>      cbxNote       = null;
	private JTextField fldVolume     = null;
	private JSlider    sldVolume     = null;
	private JTextField fldDuration   = null;
	private JButton    btnPlay       = null;
	
	private        KeyEventPostProcessor keyProcessor   = null;
	private static SoundcheckView        soundcheckView = null;
	private        Container             content        = null;
	private        SoundcheckNoteModel   noteModel      = null;
	
	/**
	 * Creates the soundcheck window.
	 * 
	 * This constructor is private to ensure a singleton behaviour.
	 * 
	 * @param owner The player window (a {@link PlayerView} object).
	 */
	private SoundcheckView( JDialog owner ) {
		super( owner );
		setTitle( Dict.get(Dict.TITLE_SOUNDCHECK) );
		noteModel = new SoundcheckNoteModel();
		
		init();
		
		pack();
		setVisible( true );
	}
	
	/**
	 * Initializes the content of the soundcheck window.
	 * This is called by the constructor.
	 */
	private void init() {
		// content
		content = getContentPane();
		SoundcheckControler controller = SoundcheckControler.getControler( this, noteModel );
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		content.setLayout( layout );
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill   = GridBagConstraints.BOTH;
		constraints.insets = new Insets( 2, 2, 2, 2 );
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 0;
		constraints.weighty    = 0;
		
		// channel label
		JLabel lblChannel = new JLabel( Dict.get(Dict.SNDCHK_CHANNEL) );
		content.add( lblChannel, constraints );
		
		// channel checkbox
		constraints.gridx++;
		constraints.gridwidth = 2;
		cbxChannel = new JComboBox<NamedInteger>();
		cbxChannel.setModel( new SoundcheckChannelModel() );
		cbxChannel.setActionCommand( CMD_CHANNEL );
		cbxChannel.addActionListener( controller );
		content.add( cbxChannel, constraints );
		
		// instrument label
		constraints.gridy++;
		constraints.gridx     = 0;
		constraints.gridwidth = 1;
		JLabel lblInstr = new JLabel( Dict.get(Dict.SNDCHK_INSTRUMENT) );
		content.add( lblInstr, constraints );
		
		// instrument checkbox
		constraints.gridx++;
		constraints.gridwidth = 2;
		cbxInstrument = new JComboBox<InstrumentElement>();
		cbxInstrument.setModel( new SoundcheckInstrumentModel() );
		cbxInstrument.setRenderer( new CategorizedComboboxRenderer() );
		cbxInstrument.setActionCommand( CMD_INSTR );
		cbxInstrument.addActionListener( new CategorizedComboboxListener(cbxInstrument) );
		cbxInstrument.addActionListener( controller );
		content.add( cbxInstrument, constraints );
		
		// note label
		constraints.gridy++;
		constraints.gridx     = 0;
		constraints.gridwidth = 1;
		JLabel lblNote = new JLabel( Dict.get(Dict.SNDCHK_NOTE) );
		content.add( lblNote, constraints );
		
		// note checkbox
		constraints.gridx++;
		constraints.gridwidth = 2;
		cbxNote = new JComboBox<NamedInteger>();
		cbxNote.setModel( noteModel );
		cbxNote.setActionCommand( CMD_NOTE );
		cbxNote.addActionListener( controller );
		content.add( cbxNote, constraints );
		
		// volume label
		constraints.gridy++;
		constraints.gridx     = 0;
		constraints.gridwidth = 1;
		JLabel lblVolume = new JLabel( Dict.get(Dict.SNDCHK_VOLUME) );
		content.add( lblVolume, constraints );
		
		// volume text field
		constraints.gridx++;
		fldVolume = new JTextField( Integer.toString(MidiDevices.DEFAULT_VOLUME) );
		fldVolume.setName( NAME_VOLUME );
		fldVolume.getDocument().putProperty( "name", NAME_VOLUME );
		fldVolume.getDocument().addDocumentListener( controller );
		fldVolume.addActionListener( controller );
		fldVolume.setPreferredSize( DIM_TEXT_FIELD );
		fldVolume.setMinimumSize( DIM_TEXT_FIELD );
		fldVolume.setMaximumSize( DIM_TEXT_FIELD );
		content.add( fldVolume, constraints );
		
		// volume slider
		constraints.gridy++;
		constraints.gridwidth = 2;
		content.add( createVolumeSlider(controller), constraints );
		
		// duration label
		constraints.gridy++;
		constraints.gridx     = 0;
		constraints.gridwidth = 1;
		JLabel lblDuration = new JLabel( Dict.get(Dict.SNDCHK_DURATION) );
		content.add( lblDuration, constraints );
		
		// duration text field
		constraints.gridx++;
		fldDuration = new JTextField( Integer.toString(DEFAULT_DURATION) );
		fldDuration.setName( NAME_DURATION );
		fldDuration.getDocument().putProperty( "name", NAME_DURATION );
		fldDuration.getDocument().addDocumentListener( controller );
		fldDuration.addActionListener( controller );
		fldDuration.setPreferredSize( DIM_TEXT_FIELD );
		fldDuration.setMinimumSize( DIM_TEXT_FIELD );
		fldDuration.setMaximumSize( DIM_TEXT_FIELD );
		content.add( fldDuration, constraints );
		
		// play button
		constraints.gridy++;
		constraints.gridx     = 0;
		constraints.gridwidth = 3;
		btnPlay = new JButton( Dict.get(Dict.SNDCHK_PLAY) );
		btnPlay.setActionCommand( CMD_PLAY );
		btnPlay.addActionListener( SoundcheckControler.getControler() );
		content.add( btnPlay, constraints );
		
		// make sure that the key bindings work
		addWindowListener( controller );
	}
	
	/**
	 * Creates the volume slider for the soundcheck window.
	 * 
	 * @param controller The event listener object for the soundcheck.
	 * @return the volume slider.
	 */
	private JSlider createVolumeSlider( SoundcheckControler controller ) {
		sldVolume = new JSlider( JSlider.HORIZONTAL );
		sldVolume.setName( NAME_VOLUME );
		sldVolume.addChangeListener( controller );
		sldVolume.addMouseWheelListener( controller );
		sldVolume.setUI( SliderHelper.createSliderUi() );
		sldVolume.setValue( MidiDevices.DEFAULT_VOLUME );
		sldVolume.setPaintTicks( true );
		sldVolume.setPaintLabels( true );
		sldVolume.setPaintTrack( true );
		// labels
		sldVolume.setMinimum( PlayerView.VOL_MIN );
		sldVolume.setMaximum( PlayerView.VOL_MAX );
		sldVolume.setMajorTickSpacing( PlayerView.VOL_MAJOR );
		sldVolume.setMinorTickSpacing( PlayerView.VOL_MINOR );
		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		for ( int i = PlayerView.VOL_MIN; i <= PlayerView.VOL_MAX; i += VOL_LABEL ) {
			int display = i;
			labelTable.put( i, new JLabel(Integer.toString(display)) );
		}
		sldVolume.setLabelTable( labelTable );
		return sldVolume;
	}
	
	/**
	 * This method is used instead of the (private) constructor to create the
	 * soundcheck window if not yet done.
	 * 
	 * @param owner The player window (a {@link PlayerView} object).
	 */
	public static void showSoundcheck( JDialog owner ) {
		
		if ( null == soundcheckView )
			soundcheckView = new SoundcheckView( owner );
		soundcheckView.makeVisible();
	}
	
	/**
	 * Destroys the {@link SoundcheckView} object.
	 * 
	 * This is called if the player window is closed.
	 */
	public static void close() {
		soundcheckView = null;
	}
	
	/**
	 * Shows the soundcheck window and brings it to the front.
	 * 
	 * This is called whenever the **soundcheck** button in the player window
	 * is pushed.
	 */
	public void makeVisible() {
		setVisible( true );
		soundcheckView.toFront();
	}
	
	/**
	 * Sets the volume slider and the volume text field to the given value.
	 * 
	 * @param volume  The volume value to set.
	 */
	public void setVolume( byte volume ) {
		
		// set slider
		sldVolume.setValue( volume );
		
		// set text field
		SoundcheckControler controller = SoundcheckControler.getControler();
		fldVolume.getDocument().removeDocumentListener( controller );
		fldVolume.setText( Byte.toString(volume) );
		fldVolume.getDocument().addDocumentListener( controller );
		setTextFieldColor( fldVolume.getName(), PlayerView.COLOR_NORMAL );
	}
	
	/**
	 * Sets the given text field's background to the given color.
	 * 
	 * @param name   Text field name.
	 * @param color  Background color.
	 */
	public void setTextFieldColor( String name, Color color ) {
		if ( NAME_VOLUME.equals(name) )
			fldVolume.setBackground( color );
		else if ( NAME_DURATION.equals(name) )
			fldDuration.setBackground( color );
	}
	
	/**
	 * Shows if the volume slider is being changed via mouse click in the moment.
	 * 
	 * Returns true, if a mouse click on the slider has been started (mouse down) but
	 * is not yet finished (mouse up).
	 * 
	 * @return **true**, if the slider is being changed. Otherwise: **false**.
	 */
	public boolean isVolumeSliderAdjusting() {
		return sldVolume.getValueIsAdjusting();
	}
	
	/**
	 * Returns the content of the volume text field.
	 * 
	 * @return Volume from the text field.
	 * @throws NumberFormatException if the text cannot be parsed to a byte value.
	 */
	public byte getVolumeFromField() throws NumberFormatException {
		return Byte.parseByte( fldVolume.getText() );
	}
	
	/**
	 * Returns the content of the duration text field.
	 * 
	 * @return Duration from the text field.
	 * @throws NumberFormatException if the text cannot be parsed to an integer value.
	 */
	public int getDurationFromField() throws NumberFormatException {
		return Integer.parseInt( fldDuration.getText() );
	}
	
	/**
	 * Enables or disables the instruments combobox, depending on the given
	 * value.
	 * 
	 * The combobox has to be disabled if the percussion channel is selected
	 * and enabled if another channel is selected.
	 * 
	 * @param active  **true** to activate the instruments combobox, **false** to disable it.
	 */
	public void toggleInstruments( boolean active ) {
		cbxInstrument.setEnabled( active );
	}
	
	/**
	 * Returns the currently selected channel from the channel combobox.
	 * 
	 * @return the selected MIDI channel number.
	 */
	public int getChannel() {
		NamedInteger option = (NamedInteger) cbxChannel.getSelectedItem();
		return option.value;
	}
	
	/**
	 * Returns the currently selected instrument number from the instruments
	 * combobox.
	 * 
	 * @return the selected instrument number.
	 */
	public int getInstrument() {
		InstrumentElement option = (InstrumentElement) cbxInstrument.getSelectedItem();
		return option.instrNum;
	}
	
	/**
	 * Returns the currently selected note (or percussion instrument) from
	 * the note/percussion combobox.
	 * 
	 * If the percussion channel is selected, a percussion number is returned.
	 * Otherwise it's a note number.
	 * 
	 * @return the currently selected note or percussion number.
	 */
	public int getNote() {
		NamedInteger option = (NamedInteger) cbxNote.getSelectedItem();
		return option.value;
	}
	
	/**
	 * Enables or disables the action listener for the note/percussion
	 * combobox, depending on the given value.
	 * 
	 * The listener must be disabled temporarily while another channel is
	 * selected because that causes a rebuild of the note/percussion combobox.
	 * 
	 * @param on  **true** to activate the listener, **false** to deactivate it.
	 */
	public void toggleNoteListener( boolean on ) {
		SoundcheckControler controller = SoundcheckControler.getControler();
		if (on)
			cbxNote.addActionListener( controller );
		else
			cbxNote.removeActionListener( controller );
	}
	
	/**
	 * Returns the current volume from the volume slider.
	 * 
	 * @return Volume value from the slider.
	 */
	public int getVolume() {
		return sldVolume.getValue();
	}
	
	/**
	 * Causes a click on the **play** button.
	 * 
	 * This is called if an item from one of the comboboxes has been selected
	 * or if **Enter** has been pressed while one of the text fields was
	 * focused.
	 */
	public void pressPlayButton() {
		btnPlay.doClick();
	}
	
	/**
	 * Adds key bindings for the soundcheck window.
	 * 
	 * This is done by creating a new {@link KeyEventPostProcessor} object (if
	 * not yet done) and adding it to the keyboard focus manager.
	 * 
	 * This is called if the soundcheck window is opened.
	 */
	public void addKeyBindings() {
		
		if ( null == keyProcessor ) {
			keyProcessor = new KeyEventPostProcessor() {
				public boolean postProcessKeyEvent( KeyEvent e ) {
					
					if ( KeyEvent.KEY_PRESSED == e.getID() ) {
						
						// handle slider adjustments via keyboard
						SliderHelper.handleSliderAdjustmentViaKey( e );
						
						// don't handle already consumed shortcuts any more
						if ( e.isConsumed() )
							return true;
						
						if ( KeyEvent.VK_ESCAPE == e.getKeyCode() ) {
							setVisible( false );
							return true;
						}
						
						// Unfortunately the postprocessing takes place before
						// any text field input. So we have to check if a textfield is focused.
						if ( isTextfieldFocussed() ) {
							return false;
						}
						
						if (   KeyEvent.VK_P     == e.getKeyCode()
							|| KeyEvent.VK_ENTER == e.getKeyCode()
							|| KeyEvent.VK_SPACE == e.getKeyCode() ) {
								btnPlay.doClick();
								e.consume();
						}
					}
					return e.isConsumed();
				}
			};
		}
		
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventPostProcessor( keyProcessor );
	}
	
	/**
	 * Removes the key bindings from the soundcheck window.
	 * 
	 * This is called if the soundcheck window is closed.
	 */
	public void removeKeyBindings() {
		KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventPostProcessor( keyProcessor );
	}
	
	/**
	 * Determines if one of the text fields in the soundcheck window has
	 * the focus.
	 * 
	 * @return **true** if a text field has the focus. Otherwise: returns **false**.
	 */
	private boolean isTextfieldFocussed() {
		if ( fldDuration.hasFocus() || fldVolume.hasFocus() )
			return true;
		
		return false;
	}
}
