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
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.midica.config.Dict;
import org.midica.config.KeyBindingManager;
import org.midica.config.Laf;
import org.midica.config.NamedInteger;
import org.midica.midi.MidiDevices;
import org.midica.ui.player.PlayerView;
import org.midica.ui.tablefilter.FilterIconWithLabel;
import org.midica.ui.widget.MidicaButton;
import org.midica.ui.widget.MidicaSlider;
import org.midica.ui.widget.MidicaTable;

/**
 * This class defines the UI for the soundcheck window.
 * 
 * @author Jan Trukenmüller
 */
public class SoundcheckView extends JDialog {
	
	private static final long serialVersionUID = 1L;
    
	public static final int    DEFAULT_VELOCITY =   100;
	public static final int    MIN_DURATION     =     0;
	public static final int    MAX_DURATION     = 10000;
	public static final String CMD_PLAY         = "cmd_play";
	public static final String CMD_KEEP         = "cmd_keep";
	public static final String NAME_INSTR       = "name_instr";
	public static final String NAME_NOTE        = "name_note";
	public static final String NAME_VOLUME      = "name_volume";
	public static final String NAME_VELOCITY    = "name_velocity";
	public static final String NAME_DURATION    = "name_duration";
	
	private static final int    TEXT_FIELD_WIDTH      =  60;
	private static final int    WIDTH_COL_PROG        =  40;
	private static final int    WIDTH_COL_BANK        =  50;
	private static final int    WIDTH_COL_NAME_SF     = 200;
	private static final int    WIDTH_COL_NAME_SYNTAX = 200;
	private static final int    HEIGHT_TABLE_INSTR    = 200;
	private static final int    HEIGHT_LIST_NOTE      = 150;
	private static final int    VOL_OR_VEL_LABEL_SKIP =  20;
	private static final int    DEFAULT_DURATION      = 300;
	private static final int    LIST_FONT_SIZE        =  12;
	private static final String LIST_FONT_NAME        = "monospaced";
	private static final int    LIST_FONT_STYLE       = Font.BOLD;
	
	private Dimension dimTblInstr  = null;
	private Dimension dimListNote  = null;
	private Dimension dimTextField = null;
	
	private JComboBox<NamedInteger> cbxChannel      = null;
	private MidicaTable             tblInstrument   = null;
	private FilterIconWithLabel     labelWithFilter = null;
	private JList<NamedInteger>     lstNote         = null;
	private JTextField              fldVolume       = null;
	private JTextField              fldVelocity     = null;
	private MidicaSlider            sldVolume       = null;
	private MidicaSlider            sldVelocity     = null;
	private JTextField              fldDuration     = null;
	private JCheckBox               cbxKeep         = null;
	private MidicaButton            btnPlay         = null;
	
	private        KeyBindingManager         keyBindingManager = null;
	private static SoundcheckView            soundcheckView    = null;
	private        Container                 content           = null;
	private        SoundcheckNoteModel       noteModel         = null;
	private        SoundcheckInstrumentModel instrModel        = null;
	
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
		noteModel    = new SoundcheckNoteModel();
		instrModel   = new SoundcheckInstrumentModel();
		dimTextField = new Dimension(TEXT_FIELD_WIDTH, Laf.textFieldHeight);
		
		// size of instruments table and notes list
		int widthInstr = WIDTH_COL_PROG + WIDTH_COL_BANK + WIDTH_COL_NAME_SF + WIDTH_COL_NAME_SYNTAX;
		dimTblInstr    = new Dimension( widthInstr, HEIGHT_TABLE_INSTR );
		dimListNote    = new Dimension( widthInstr, HEIGHT_LIST_NOTE   );
		
		init();
		addKeyBindings();
		
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
		SoundcheckController controller = SoundcheckController.getController( this, noteModel, instrModel );
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		content.setLayout( layout );
		GridBagConstraints constrLeft = new GridBagConstraints();
		constrLeft.anchor     = GridBagConstraints.WEST;
		constrLeft.fill       = GridBagConstraints.NONE;
		constrLeft.insets     = Laf.INSETS_NW;
		constrLeft.gridx      = 0;
		constrLeft.gridy      = 0;
		constrLeft.gridheight = 1;
		constrLeft.gridwidth  = 1;
		constrLeft.weightx    = 0;
		constrLeft.weighty    = 0;
		GridBagConstraints constrRight = (GridBagConstraints) constrLeft.clone();
		constrRight.fill      = GridBagConstraints.HORIZONTAL;
		constrRight.insets    = Laf.INSETS_NE;
		constrRight.weightx   = 1;
		constrRight.gridwidth = 2;
		constrRight.gridx++;
		
		// channel label
		JLabel lblChannel = new JLabel( Dict.get(Dict.SNDCHK_CHANNEL) );
		Laf.makeBold(lblChannel);
		content.add( lblChannel, constrLeft );
		
		// channel checkbox
		cbxChannel = new JComboBox<NamedInteger>();
		cbxChannel.setModel( new SoundcheckChannelModel() );
		cbxChannel.addItemListener( controller );
		content.add( cbxChannel, constrRight );
		
		// instrument label
		constrLeft.insets = Laf.INSETS_W;
		constrLeft.gridy++;
		labelWithFilter = new FilterIconWithLabel( Dict.get(Dict.SNDCHK_INSTRUMENT), this, true );
		content.add(labelWithFilter, constrLeft);
		
		// instrument list
		constrRight.gridy++;
		constrRight.insets  = Laf.INSETS_E;
		constrRight.fill    = GridBagConstraints.BOTH;
		constrRight.weighty = 1;
		SoundcheckInstrumentTableCellRenderer instrRenderer = new SoundcheckInstrumentTableCellRenderer( instrModel );
		tblInstrument = new MidicaTable( instrModel );
		tblInstrument.setName( NAME_INSTR );
		tblInstrument.setDefaultRenderer( Object.class, instrRenderer );
		tblInstrument.getColumnModel().getColumn( 0 ).setPreferredWidth( WIDTH_COL_PROG        );
		tblInstrument.getColumnModel().getColumn( 1 ).setPreferredWidth( WIDTH_COL_BANK        );
		tblInstrument.getColumnModel().getColumn( 2 ).setPreferredWidth( WIDTH_COL_NAME_SF     );
		tblInstrument.getColumnModel().getColumn( 3 ).setPreferredWidth( WIDTH_COL_NAME_SYNTAX );
		tblInstrument.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
		tblInstrument.getSelectionModel().addListSelectionListener( controller );
		JScrollPane scrollInstr = new JScrollPane( tblInstrument );
		scrollInstr.setPreferredSize( dimTblInstr );
		labelWithFilter.setTable(tblInstrument);
		labelWithFilter.addRowSorterListener(controller);
		content.add( scrollInstr, constrRight );
		
		// note label
		constrLeft.gridy++;
		JLabel lblNote = new JLabel( Dict.get(Dict.SNDCHK_NOTE) );
		Laf.makeBold(lblNote);
		content.add( lblNote, constrLeft );
		
		// note list
		constrRight.gridy++;
		lstNote = new JList<NamedInteger>();
		Font listFont = new Font(LIST_FONT_NAME, LIST_FONT_STYLE, LIST_FONT_SIZE);
		lstNote.setFont(listFont);
		lstNote.setName( NAME_NOTE );
		lstNote.setModel( noteModel );
		lstNote.addListSelectionListener( controller );
		JScrollPane scrollNote = new JScrollPane( lstNote );
		scrollNote.setPreferredSize( dimListNote );
		content.add( scrollNote, constrRight );
		
		// volume label
		constrLeft.gridy++;
		constrLeft.gridheight = 2;
		constrLeft.anchor     = GridBagConstraints.NORTHWEST;
		JLabel lblVolume = new JLabel( Dict.get(Dict.SNDCHK_VOLUME) );
		Laf.makeBold(lblVolume);
		lblVolume.setVerticalAlignment( SwingConstants.TOP );
		content.add( lblVolume, constrLeft );
		
		// volume text field
		constrRight.gridy++;
		constrRight.gridwidth = 1;
		constrRight.fill      = GridBagConstraints.NONE;
		constrRight.weighty   = 0;
		fldVolume = new JTextField( Integer.toString(MidiDevices.DEFAULT_CHANNEL_VOL_MSB) );
		fldVolume.setName( NAME_VOLUME );
		fldVolume.getDocument().putProperty( "name", NAME_VOLUME );
		fldVolume.getDocument().addDocumentListener( controller );
		fldVolume.addActionListener( controller );
		fldVolume.setPreferredSize(dimTextField);
		content.add( fldVolume, constrRight );
		
		// volume slider
		constrRight.gridy++;
		constrRight.gridwidth = 2;
		constrRight.fill      = GridBagConstraints.HORIZONTAL;
		sldVolume = createVolOrVelSlider( controller );
		sldVolume.setName( NAME_VOLUME );
		sldVolume.setValue( MidiDevices.DEFAULT_CHANNEL_VOL_MSB );
		content.add( sldVolume, constrRight );
		
		// velocity label
		constrLeft.gridy  += 2;
		JLabel lblVelocity = new JLabel( Dict.get(Dict.SNDCHK_VELOCITY) );
		Laf.makeBold(lblVelocity);
		lblVelocity.setVerticalAlignment( SwingConstants.TOP );
		content.add( lblVelocity, constrLeft );
		
		// velocity text field
		constrRight.gridy++;
		constrRight.gridwidth = 1;
		constrRight.weightx   = 0;
		constrRight.fill      = GridBagConstraints.NONE;
		fldVelocity = new JTextField( Integer.toString(DEFAULT_VELOCITY) );
		fldVelocity.setName( NAME_VELOCITY );
		fldVelocity.getDocument().putProperty( "name", NAME_VELOCITY );
		fldVelocity.getDocument().addDocumentListener( controller );
		fldVelocity.addActionListener( controller );
		fldVelocity.setPreferredSize(dimTextField);
		content.add( fldVelocity, constrRight );
		
		// velocity slider
		constrRight.gridy++;
		constrRight.gridwidth = 2;
		constrRight.weightx   = 1;
		constrRight.fill      = GridBagConstraints.HORIZONTAL;
		sldVelocity = createVolOrVelSlider( controller );
		sldVelocity.setName( NAME_VELOCITY );
		sldVelocity.setValue( DEFAULT_VELOCITY );
		content.add( sldVelocity, constrRight );
		
		// duration label
		constrLeft.gridy     += 2;
		constrLeft.anchor     = GridBagConstraints.WEST;
		constrLeft.gridheight = 1;
		JLabel lblDuration    = new JLabel( Dict.get(Dict.SNDCHK_DURATION) );
		Laf.makeBold(lblDuration);
		content.add( lblDuration, constrLeft );
		
		// duration text field
		constrRight.gridy++;
		constrRight.gridwidth = 1;
		constrRight.weightx   = 0;
		constrRight.fill      = GridBagConstraints.NONE;
		fldDuration = new JTextField( Integer.toString(DEFAULT_DURATION) );
		fldDuration.setName( NAME_DURATION );
		fldDuration.getDocument().putProperty( "name", NAME_DURATION );
		fldDuration.getDocument().addDocumentListener( controller );
		fldDuration.addActionListener( controller );
		fldDuration.setPreferredSize(dimTextField);
		content.add( fldDuration, constrRight );
		
		// keep settings label
		constrLeft.gridy++;
		JLabel lblKeep = new JLabel( Dict.get(Dict.SNDCHK_KEEP_SETTINGS) );
		Laf.makeBold(lblKeep);
		content.add( lblKeep, constrLeft );
		
		// keep settings checkbox
		constrRight.gridy++;
		cbxKeep = new JCheckBox();
		cbxKeep.setActionCommand( CMD_KEEP );
		cbxKeep.addActionListener( controller );
		content.add( cbxKeep, constrRight );
		
		// play button
		constrLeft.gridy++;
		constrLeft.gridwidth = 3;
		constrLeft.insets    = Laf.INSETS_SWE;
		constrLeft.fill      = GridBagConstraints.HORIZONTAL;
		btnPlay = new MidicaButton( Dict.get(Dict.SNDCHK_PLAY), true );
		btnPlay.setActionCommand( CMD_PLAY );
		btnPlay.addActionListener( SoundcheckController.getController() );
		content.add( btnPlay, constrLeft );
		
		// make sure that the key bindings work
		addWindowListener( controller );
	}
	
	/**
	 * Creates the volume or velocity slider for the soundcheck window.
	 * 
	 * @param controller The event listener object for the soundcheck.
	 * @return the volume or velocity slider.
	 */
	private MidicaSlider createVolOrVelSlider( SoundcheckController controller ) {
		MidicaSlider slider = new MidicaSlider( MidicaSlider.HORIZONTAL );
		slider.addChangeListener( controller );
		slider.addMouseWheelListener( controller );
		// labels
		slider.setMinimum( PlayerView.CH_VOL_MIN );
		slider.setMaximum( PlayerView.CH_VOL_MAX );
		slider.setMajorTickSpacing( PlayerView.CH_VOL_MAJOR );
		slider.setMinorTickSpacing( PlayerView.CH_VOL_MINOR );
		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		for ( int i = PlayerView.CH_VOL_MIN; i <= PlayerView.CH_VOL_MAX; i += VOL_OR_VEL_LABEL_SKIP ) {
			int display = i;
			labelTable.put( i, new JLabel(Integer.toString(display)) );
		}
		slider.setLabelTable( labelTable );
		return slider;
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
	public void setVolume(byte volume) {
		
		// set slider
		sldVolume.setValue( volume );
		
		// set text field
		SoundcheckController controller = SoundcheckController.getController();
		fldVolume.getDocument().removeDocumentListener( controller );
		fldVolume.setText( Byte.toString(volume) );
		fldVolume.getDocument().addDocumentListener( controller );
		setTextFieldColor( fldVolume.getName(), Laf.COLOR_NORMAL );
	}
	
	/**
	 * Sets the velocity slider and the velocity text field to the given value.
	 * 
	 * @param velocity  The velocity value to set.
	 */
	public void setVelocity( byte velocity ) {
		
		// set slider
		sldVelocity.setValue( velocity );
		
		// set text field
		SoundcheckController controller = SoundcheckController.getController();
		fldVelocity.getDocument().removeDocumentListener( controller );
		fldVelocity.setText( Byte.toString(velocity) );
		fldVelocity.getDocument().addDocumentListener( controller );
		setTextFieldColor( fldVelocity.getName(), Laf.COLOR_NORMAL );
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
		else if ( NAME_VELOCITY.equals(name) )
			fldVelocity.setBackground( color );
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
	 * Shows if the velocity slider is being changed via mouse click in the moment.
	 * 
	 * Returns true, if a mouse click on the slider has been started (mouse down) but
	 * is not yet finished (mouse up).
	 * 
	 * @return **true**, if the slider is being changed. Otherwise: **false**.
	 */
	public boolean isVelocitySliderAdjusting() {
		return sldVelocity.getValueIsAdjusting();
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
	 * Returns the content of the velocity text field.
	 * 
	 * @return Velocity from the text field.
	 * @throws NumberFormatException if the text cannot be parsed to a byte value.
	 */
	public byte getVeloctiyFromField() throws NumberFormatException {
		return Byte.parseByte( fldVelocity.getText() );
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
	 * Returns the currently selected channel from the channel combobox.
	 * 
	 * @return the selected MIDI channel number.
	 */
	public int getChannel() {
		NamedInteger option = (NamedInteger) cbxChannel.getSelectedItem();
		return option.value;
	}
	
	/**
	 * Indicates, if the string filter layer is currently open or not.
	 * 
	 * @return  **true**, if the layer is open, otherwise **false**;
	 */
	public boolean isFilterLayerOpen() {
		return labelWithFilter.isFilterLayerOpen();
	}
	
	/**
	 * Determines the currently selected row index of the instrument table (model).
	 * 
	 * @return selected row or **-1** if no row is selected.
	 */
	public int getSelectedInstrumentRow() {
		int row = tblInstrument.getSelectedRow();
		if ( row < 0 )
			return -1;
		return tblInstrument.convertRowIndexToModel(row);
	}
	
	/**
	 * Selects the given row of the instruments table, if possible.
	 * 
	 * @param i  row index (model index) to be selected
	 */
	public void setSelectedInstrumentRow( int i ) {
		
		if (i < 0)
			return;
		
		// convert row (model --> view)
		i = tblInstrument.convertRowIndexToView(i);
		if (i < 0)
			return;
		
		int rowCount = tblInstrument.getRowCount();
		if ( i > rowCount - 1 )
			return;
		
		// select the row (in the view)
		tblInstrument.setRowSelectionInterval( i, i );
	}
	
	/**
	 * Determines the currently selected index of the note list.
	 * 
	 * @return selected index or **-1** if nothing is selected.
	 */
	public int getSelectedNoteIndex() {
		int index = lstNote.getSelectedIndex();
		if ( index < 0 )
			return -1;
		return index;
	}
	
	/**
	 * Selects the given index of the note list.
	 * 
	 * @param i note index to be selected
	 */
	public void setSelectedNoteIndex( int i ) {
		lstNote.setSelectedIndex( i );
	}
	
	/**
	 * Scrolls the instrument table so that the selected row is visible.
	 */
	public void scrollInstrumentTable() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				
				// get selected (view) row
				int row = tblInstrument.getSelectedRow();
				if (row < 0)
					return;
				
				// scroll
				Rectangle cell = tblInstrument.getCellRect( row, 0, true );
				tblInstrument.scrollRectToVisible( cell );
			}
		});
	}
	
	/**
	 * Scrolls the note/percussion list so that the note with the given
	 * (selected) index is visible.
	 * 
	 * @param i selected index to be made visible
	 */
	public void scrollNoteList( int i ) {
		Rectangle cell = lstNote.getCellBounds( i, i );
		lstNote.scrollRectToVisible( cell );
	}
	
	/**
	 * Returns the currently selected instrument coordinates from the instruments
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
		
		// get last selected table row
		int row = SoundcheckController.getLastSelectedTableRow( getChannel() );
		
		if ( -1 == row )
			return result;
		ArrayList<HashMap<String, String>> instruments = instrModel.getInstruments();
		HashMap<String, String> instr = instruments.get( row );
		
		// real instrument/drumkit selected?
		if ( instr.containsKey("program") ) {
			result[ 0 ] = Integer.parseInt( instr.get("program")  );
			result[ 1 ] = Integer.parseInt( instr.get("bank_msb") );
			result[ 2 ] = Integer.parseInt( instr.get("bank_lsb") );
		}
		
		return result;
	}
	
	/**
	 * Determins if the category or instrument currently selected in the instruments
	 * table is a drumkit or not.
	 * 
	 * @return **true**, if a drumkit or drum category is selected. Otherwise: returns **false**
	 */
	public boolean isDrumSelected() {
		
		// get table row
		int row = tblInstrument.getSelectedRow();
		if ( row < 0 )
			return false;
		ArrayList<HashMap<String, String>> instruments = instrModel.getInstruments();
		HashMap<String, String> instr = instruments.get( tblInstrument.convertRowIndexToModel(row) );
		
		// main or sub category
		if ( instr.containsKey("category") ) {
			String cat = instr.get("category");
			
			// (chromatic) sub category
			if ( "sub".equals(cat) ) {
				return false;
			}
			
			// main category
			String type = instr.get("type");
			if ( "category_chromatic".equals(type) )
				return false;
			else
				return true;
		}
		
		// not a category
		String type = instr.get("type");
		if ( "chromatic".equals(type) )
			return false;
		else
			return true;
	}
	
	/**
	 * Returns the currently selected note (or percussion instrument) from
	 * the note/percussion combobox.
	 * 
	 * If no note or percussion instrument is selected, **-1** is returned.
	 * 
	 * @return the currently selected note or percussion number or **-1** if nothing is selected.
	 */
	public int getNote() {
		NamedInteger option = lstNote.getSelectedValue();
		if ( null == option )
			return -1;
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
		SoundcheckController controller = SoundcheckController.getController();
		if (on)
			lstNote.addListSelectionListener( controller );
		else
			lstNote.removeListSelectionListener( controller );
	}
	
	/**
	 * Enables or disables the selection listener for the instruments table.
	 * During channel selection the listener must be disabled temporarily
	 * while the instruments table is refilled and the last selection is
	 * restored.
	 * 
	 * @param on  **true** to activate the listener, **false** to deactivate it.
	 */
	public void toggleInstrumentSelectionListener( boolean on ) {
		SoundcheckController controller = SoundcheckController.getController();
		if (on)
			tblInstrument.getSelectionModel().addListSelectionListener( controller );
		else
			tblInstrument.getSelectionModel().removeListSelectionListener( controller );
	}
	
	/**
	 * Returns the current volume from the volume slider.
	 * 
	 * @return Volume value from the slider.
	 */
	public byte getVolume() {
		return (byte) sldVolume.getValue();
	}
	
	/**
	 * Returns the current velocity from the velocity slider.
	 * 
	 * @return Velocity value from the slider.
	 */
	public int getVelocity() {
		return sldVelocity.getValue();
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
	 * Determins if the 'keep settings' checkbox is checked or not.
	 * 
	 * @return **true** if the checkbox is checked. Otherwise: returns **false**.
	 */
	public boolean mustKeepSettings() {
		return cbxKeep.isSelected();
	}
	
	/**
	 * Adds key bindings to the soundcheck window using the {@link KeyBindingManager}.
	 */
	private void addKeyBindings() {
		
		// reset everything
		keyBindingManager = new KeyBindingManager( this, this.getRootPane() );
		
		// allow to use SPACE as a window-wide key binding for the play button
		keyBindingManager.globalizeSpace();
		
		// add key bindings for closing the window
		keyBindingManager.addBindingsForClose( Dict.KEY_SOUNDCHECK_CLOSE );
		
		// add key bindings to buttons
		keyBindingManager.addBindingsForButton( this.btnPlay, Dict.KEY_SOUNDCHECK_PLAY );
		
		// add bindings for table filters
		keyBindingManager.addBindingsForIconLabel( this.labelWithFilter, Dict.KEY_SOUNDCHECK_FILTER_INSTR );
		
		// add focus bindings
		keyBindingManager.addBindingsForFocus( this.tblInstrument, Dict.KEY_SOUNDCHECK_INSTR    );
		keyBindingManager.addBindingsForFocus( this.lstNote,       Dict.KEY_SOUNDCHECK_NOTE     );
		keyBindingManager.addBindingsForFocus( this.fldVolume,     Dict.KEY_SOUNDCHECK_VOL_FLD  );
		keyBindingManager.addBindingsForFocus( this.sldVolume,     Dict.KEY_SOUNDCHECK_VOL_SLD  );
		keyBindingManager.addBindingsForFocus( this.fldVelocity,   Dict.KEY_SOUNDCHECK_VEL_FLD  );
		keyBindingManager.addBindingsForFocus( this.sldVelocity,   Dict.KEY_SOUNDCHECK_VEL_SLD  );
		keyBindingManager.addBindingsForFocus( this.fldDuration,   Dict.KEY_SOUNDCHECK_DURATION );
		
		// add checkbox bindings
		keyBindingManager.addBindingsForCheckbox( this.cbxKeep, Dict.KEY_SOUNDCHECK_KEEP );
		
		// add key bindings for switching the channel
		keyBindingManager.addBindingsForComboboxSelect( this.cbxChannel, Dict.KEY_SOUNDCHECK_CH_00, 0  );
		keyBindingManager.addBindingsForComboboxSelect( this.cbxChannel, Dict.KEY_SOUNDCHECK_CH_01, 1  );
		keyBindingManager.addBindingsForComboboxSelect( this.cbxChannel, Dict.KEY_SOUNDCHECK_CH_02, 2  );
		keyBindingManager.addBindingsForComboboxSelect( this.cbxChannel, Dict.KEY_SOUNDCHECK_CH_03, 3  );
		keyBindingManager.addBindingsForComboboxSelect( this.cbxChannel, Dict.KEY_SOUNDCHECK_CH_04, 4  );
		keyBindingManager.addBindingsForComboboxSelect( this.cbxChannel, Dict.KEY_SOUNDCHECK_CH_05, 5  );
		keyBindingManager.addBindingsForComboboxSelect( this.cbxChannel, Dict.KEY_SOUNDCHECK_CH_06, 6  );
		keyBindingManager.addBindingsForComboboxSelect( this.cbxChannel, Dict.KEY_SOUNDCHECK_CH_07, 7  );
		keyBindingManager.addBindingsForComboboxSelect( this.cbxChannel, Dict.KEY_SOUNDCHECK_CH_08, 8  );
		keyBindingManager.addBindingsForComboboxSelect( this.cbxChannel, Dict.KEY_SOUNDCHECK_CH_09, 9  );
		keyBindingManager.addBindingsForComboboxSelect( this.cbxChannel, Dict.KEY_SOUNDCHECK_CH_10, 10 );
		keyBindingManager.addBindingsForComboboxSelect( this.cbxChannel, Dict.KEY_SOUNDCHECK_CH_11, 11 );
		keyBindingManager.addBindingsForComboboxSelect( this.cbxChannel, Dict.KEY_SOUNDCHECK_CH_12, 12 );
		keyBindingManager.addBindingsForComboboxSelect( this.cbxChannel, Dict.KEY_SOUNDCHECK_CH_13, 13 );
		keyBindingManager.addBindingsForComboboxSelect( this.cbxChannel, Dict.KEY_SOUNDCHECK_CH_14, 14 );
		keyBindingManager.addBindingsForComboboxSelect( this.cbxChannel, Dict.KEY_SOUNDCHECK_CH_15, 15 );
		
		// commit key bindings
		keyBindingManager.postprocess();
	}
}
