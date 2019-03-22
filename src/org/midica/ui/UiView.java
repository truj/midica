/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.KeyEventPostProcessor;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.midica.Midica;
import org.midica.config.Config;
import org.midica.config.Dict;
import org.midica.file.SequenceParser;
import org.midica.file.SoundfontParser;
import org.midica.ui.model.ComboboxStringOption;
import org.midica.ui.model.ConfigComboboxModel;


/**
 * This class provides the main window containing configuration area, player button
 * and file import/export buttons. It uses the {@link UiController} as {@link WindowListener}
 * and {@link ActionListener} for all buttons and dropdown boxes.
 * 
 * @author Jan Trukenmüller
 */
public class UiView extends JFrame {
	
	private static final long serialVersionUID = 1L;
    
	public static final String NAME_SELECT_LANGUAGE   = "name_select_language";
	public static final String NAME_SELECT_SYSTEM     = "name_select_system";
	public static final String NAME_SELECT_HALF_TONE  = "name_select_half_tone";
	public static final String NAME_SELECT_OCTAVE     = "name_select_octave";
	public static final String NAME_SELECT_SYNTAX     = "name_select_syntax";
	public static final String NAME_SELECT_PERCUSSION = "name_select_percussion";
	public static final String NAME_SELECT_INSTRUMENT = "name_select_instrument";
	public static final String NAME_REMEMBER_SF       = "name_remember_sf";
	public static final String NAME_REMEMBER_MIDI     = "name_remember_midi";
	public static final String NAME_REMEMBER_MIDICAPL = "name_remember_midicapl";
	
	public static final String CMD_SELECT_LANGUAGE     = "select_language";
	public static final String CMD_START_PLAYER        = "start_player";
	public static final String CMD_OPEN_MIDICAPL_FILE  = "cmd_open_midicapl_file";
	public static final String CMD_OPEN_MIDI_FILE      = "cmd_open_midi_file";
	public static final String CMD_OPEN_SNDFNT_FILE    = "cmd_open_soundfont_file";
	public static final String CMD_SHOW_INFO_WINDOW    = "cmd_show_info_window";
	public static final String CMD_EXPORT_MIDI         = "cmd_export_midi";
	public static final String CMD_EXPORT_MIDICAPL     = "cmd_export_midicapl";
	public static final String CMD_OPEN_FCT_NOT_READY  = "cmd_open_fct_not_ready";
	
	public static final Color  COLOR_TRANSPOSE_DEFAULT = new Color(  50, 100, 255 );
	public static final Color  COLOR_TRANSPOSE_CHANGED = new Color( 255,   0,   0 );
	
	// application icons
	private static final String inactiveAppIconPath = "org/midica/resources/app-icon-inactive.png";
	private static final String activeAppIconPath   = "org/midica/resources/app-icon-active.png";
	private static Image inactiveIcon = null;
	private static Image activeIcon   = null;
	
	// make file names in the import section as small as possible
	private static final Dimension MAX_FILE_NAME_DIM = new Dimension(0, new JLabel(" ").getPreferredSize().height);
	
	private KeyEventPostProcessor           keyProcessor           = null;
	private Container                       content                = null;
	private UiController                    controller             = null;
	private JLabel                          lblChosenMidicaPLFile  = null;
	private JLabel                          lblChosenMidiFile      = null;
	private JLabel                          lblChosenSoundfontFile = null;
	private JComboBox<ComboboxStringOption> cbxGuiLang             = null;
	private JComboBox<ComboboxStringOption> cbxNoteSys             = null;
	private JComboBox<ComboboxStringOption> cbxHalfTone            = null;
	private JComboBox<ComboboxStringOption> cbxOctave              = null;
	private JComboBox<ComboboxStringOption> cbxSyntax              = null;
	private JComboBox<ComboboxStringOption> cbxPercussion          = null;
	private JComboBox<ComboboxStringOption> cbxInstrument          = null;
	private JLabel                          lblTranspose           = null;
	private JButton                         btnInfo                = null;
	private JButton                         btnPlayer              = null;
	private JButton                         btnSelectMidicaPL      = null;
	private JButton                         btnSelectMidi          = null;
	private JButton                         btnSelectSoundfont     = null;
	private JButton                         btnExportMidi          = null;
	private JButton                         btnExportMidicaPL      = null;
	private JCheckBox                       cbxRememberSf          = null;
	private JCheckBox                       cbxRememberMidicapl    = null;
	private JCheckBox                       cbxRememberMidi        = null;
	
	/**
	 * Creates the main window of the program.
	 * 
	 * Fills the contents via calling {@link #init()}.
	 * 
	 * @param controller    Used as listener for all events.
	 */
	public UiView( UiController controller ) {
		super( Dict.get(Dict.TITLE_MAIN_WINDOW) );
		this.controller = controller;
		addWindowListener( controller );
		init();
		
		// don't show the window in CLI mode
		if (Midica.isCliMode) {
			return;
		}
		
		// initialize icons
		ImageIcon icon = new ImageIcon( ClassLoader.getSystemResource(inactiveAppIconPath) );
		inactiveIcon   = icon.getImage();
		icon           = new ImageIcon( ClassLoader.getSystemResource(activeAppIconPath) );
		activeIcon     = icon.getImage();
		setAppIcon(false);
		
		pack();
		setVisible(true);
	}
	
	/**
	 * Sets the application icon indicating if a sequence has been loaded.
	 * 
	 * @param active    **true** to set the active icon, **false** for setting the inactive icon
	 */
	public void setAppIcon(boolean active) {
		if (active)
			setIconImage(activeIcon);
		else
			setIconImage(inactiveIcon);
	}
	
	/**
	 * Separates the main window by the left part (configuration area and player button)
	 * and the right part (import/export area).
	 * Those areas are filled by calling {@link #createLeftArea()} and
	 * {@link #createRightArea()}.
	 */
	private void init() {
		// content
		content = getContentPane();
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		content.setLayout( layout );
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.NONE;
		constraints.insets     = new Insets( 2, 2, 2, 2 );
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		
		// left area (config and player)
		content.add( createLeftArea(), constraints );
		
		// right area (import and export)
		constraints.gridx++;
		content.add( createRightArea(), constraints );
	}
	
	/**
	 * Creates the left area consisting of the (upper) configuration area and the
	 * (lower) area for the player button.
	 * Fills those areas by calling {@link #createConfigArea()} and
	 * {@link #createPlayerArea()}.
	 * 
	 * @return    The created area.
	 */
	private Container createLeftArea() {
		JPanel area = new JPanel();
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout( layout );
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.BOTH;
		constraints.insets     = new Insets( 2, 2, 2, 2 );
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		
		// config area
		area.add( createConfigArea(), constraints );
		
		// player area
		constraints.gridy++;
		area.add( createPlayerArea(), constraints );
		
		return area;
	}
	
	/**
	 * Creates the right area containing the (upper) import area and the (lower) export area.
	 * Fills those areas by calling {@link #createImportArea()} and {@link #createExportArea()}.
	 * 
	 * @return    The created area.
	 */
	private Container createRightArea() {
		JPanel area = new JPanel();
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout( layout );
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.BOTH;
		constraints.insets     = new Insets( 2, 2, 2, 2 );
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		
		// import area
		area.add( createImportArea(), constraints );
		
		// export area
		constraints.gridy++;
		area.add( createExportArea(), constraints );
		
		return area;
	}
	
	/**
	 * Creates the configuration area containing the dropdown boxes with their labels
	 * and the button to open the info and config view.
	 * 
	 * @return    The created area.
	 */
	private Container createConfigArea() {
		JPanel area = new JPanel();
		area.setBorder( createTitledBorder(Dict.get(Dict.CONFIGURATION)) );
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout( layout );
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.BOTH;
		constraints.insets     = new Insets( 2, 2, 2, 2 );
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		
		// gui language label
		JLabel lblLanguage = new JLabel( Dict.get(Dict.LANGUAGE) );
		area.add( lblLanguage, constraints );
		
		// gui language selection
		constraints.gridx++;
		cbxGuiLang = new JComboBox<ComboboxStringOption>();
		cbxGuiLang.setName( NAME_SELECT_LANGUAGE );
		cbxGuiLang.setModel( ConfigComboboxModel.getModel(Config.LANGUAGE) );
		cbxGuiLang.addActionListener( controller );
		area.add( cbxGuiLang, constraints );
		
		// note system label
		constraints.gridx = 0;
		constraints.gridy++;
		JLabel lblNoteSys = new JLabel( Dict.get(Dict.NOTE_SYSTEM) );
		area.add( lblNoteSys, constraints );
		
		// note system selection
		constraints.gridx++;
		cbxNoteSys = new JComboBox<ComboboxStringOption> ();
		cbxNoteSys.setName( NAME_SELECT_SYSTEM );
		cbxNoteSys.setModel( ConfigComboboxModel.getModel(Config.NOTE) );
		cbxNoteSys.addActionListener( controller );
		
		area.add( cbxNoteSys, constraints );
		
		// half tone label
		constraints.gridx = 0;
		constraints.gridy++;
		JLabel lblHalfTone = new JLabel( Dict.get(Dict.HALF_TONE_SYMBOL) );
		area.add( lblHalfTone, constraints );
		
		// half tone selection
		constraints.gridx++;
		cbxHalfTone = new JComboBox<ComboboxStringOption>();
		cbxHalfTone.setName( NAME_SELECT_HALF_TONE );
		cbxHalfTone.setModel( ConfigComboboxModel.getModel(Config.HALF_TONE) );
		cbxHalfTone.addActionListener( controller );
		
		area.add( cbxHalfTone, constraints );
		
		// octave naming label
		constraints.gridx = 0;
		constraints.gridy++;
		JLabel lblOctave = new JLabel( Dict.get(Dict.OCTAVE_NAMING) );
		area.add( lblOctave, constraints );
		
		// octave selection
		constraints.gridx++;
		cbxOctave = new JComboBox<ComboboxStringOption>();
		cbxOctave.setName( NAME_SELECT_OCTAVE );
		cbxOctave.setModel( ConfigComboboxModel.getModel(Config.OCTAVE) );
		cbxOctave.addActionListener( controller );
		
		area.add( cbxOctave, constraints );
		
		// syntax label
		constraints.gridx = 0;
		constraints.gridy++;
		JLabel lblSyntax = new JLabel( Dict.get(Dict.SYNTAX) );
		area.add( lblSyntax, constraints );
		
		// syntax selection
		constraints.gridx++;
		cbxSyntax = new JComboBox<ComboboxStringOption>();
		cbxSyntax.setName( NAME_SELECT_SYNTAX );
		cbxSyntax.setModel( ConfigComboboxModel.getModel(Config.SYNTAX) );
		cbxSyntax.addActionListener( controller );
		
		area.add( cbxSyntax, constraints );
		
		// percussion label
		constraints.gridx = 0;
		constraints.gridy++;
		JLabel lblPercussion = new JLabel( Dict.get(Dict.PERCUSSION) );
		area.add( lblPercussion, constraints );
		
		// percussion selection
		constraints.gridx++;
		cbxPercussion = new JComboBox<ComboboxStringOption>();
		cbxPercussion.setName( NAME_SELECT_PERCUSSION );
		cbxPercussion.setModel( ConfigComboboxModel.getModel(Config.PERCUSSION) );
		cbxPercussion.addActionListener( controller );
		
		area.add( cbxPercussion, constraints );
		
		// instrument naming label
		constraints.gridx = 0;
		constraints.gridy++;
		JLabel lblInstrument = new JLabel( Dict.get(Dict.INSTRUMENT_IDS) );
		area.add( lblInstrument, constraints );
		
		// instrument naming selection
		constraints.gridx++;
		cbxInstrument = new JComboBox<ComboboxStringOption>();
		cbxInstrument.setName( NAME_SELECT_INSTRUMENT );
		cbxInstrument.setModel( ConfigComboboxModel.getModel(Config.INSTRUMENT) );
		cbxInstrument.addActionListener( controller );
		
		area.add( cbxInstrument, constraints );
		
		// info button
		constraints.gridx = 1;
		constraints.gridy++;
		constraints.gridwidth = 1;
		btnInfo = new JButton( Dict.get(Dict.SHOW_INFO) );
		btnInfo.setActionCommand( CMD_SHOW_INFO_WINDOW );
		btnInfo.addActionListener( controller );
		
		area.add( btnInfo, constraints );
		
		return area;
	}
	
	/**
	 * Creates the player area containing the button to open the player.
	 * 
	 * @return    The created area.
	 */
	private Container createPlayerArea() {
		JPanel area = new JPanel();
		area.setBorder( createTitledBorder(Dict.get(Dict.PLAYER)) );
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout( layout );
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.BOTH;
		constraints.insets     = new Insets( 2, 2, 2, 2 );
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		
		// player button
		constraints.gridy++;
		constraints.gridx   = 0;
		constraints.weightx = 1;
		btnPlayer = new JButton( Dict.get(Dict.PLAYER_BUTTON) );
		btnPlayer.setActionCommand( CMD_START_PLAYER );
		btnPlayer.addActionListener( controller );
		area.add( btnPlayer, constraints );
		
		return area;
	}
	
	/**
	 * Creates the import area containing the sub areas for all importable file type.
	 * For each file type the following objects are created:
	 * 
	 * - a label describing the file type
	 * - a button to select the file
	 * - a label containing the successfully loaded file
	 * 
	 * @return    The created area.
	 */
	private Container createImportArea() {
		JPanel area = new JPanel();
		area.setBorder( createTitledBorder(Dict.get(Dict.IMPORT)) );
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout( layout );
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.BOTH;
		constraints.insets     = new Insets( 2, 2, 2, 2 );
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 0.5;
		
		// MidicaPL label
		constraints.insets = new Insets( 0, 2, 0, 2 );
		JLabel lblMidicaPL = new JLabel( Dict.get(Dict.MIDICAPL_FILE) );
		area.add( lblMidicaPL, constraints );
		constraints.insets = new Insets( 2, 2, 2, 2 );
		
		// file selector button
		constraints.gridx++;
		constraints.gridheight = 2;
		btnSelectMidicaPL = new JButton( Dict.get(Dict.CHOOSE_FILE) );
		btnSelectMidicaPL.setActionCommand( CMD_OPEN_MIDICAPL_FILE );
		btnSelectMidicaPL.addActionListener( controller );
		area.add( btnSelectMidicaPL, constraints );
		
		// remember midicapl checkbox
		constraints.insets = new Insets( 0, 2, 0, 2 );
		constraints.gridx = 0;
		constraints.gridy++;
		constraints.gridheight = 1;
		cbxRememberMidicapl = new JCheckBox( Dict.get(Dict.REMEMBER_MPL) );
		cbxRememberMidicapl.setToolTipText( Dict.get(Dict.REMEMBER_MPL_TT) );
		cbxRememberMidicapl.addItemListener( controller );
		String remember = Config.get( Config.REMEMBER_MIDICAPL );
		if ( "true".equals(remember) ) {
			cbxRememberMidicapl.setSelected( true );
		}
		cbxRememberMidicapl.setName( NAME_REMEMBER_MIDICAPL );
		area.add( cbxRememberMidicapl, constraints );
		constraints.insets = new Insets( 2, 2, 2, 2 );
		
		// chosen MidicaPL file name
		constraints.gridy++;
		constraints.gridwidth = 2;
		lblChosenMidicaPLFile = new JLabel( Dict.get(Dict.UNCHOSEN_FILE) );
		lblChosenMidicaPLFile.setPreferredSize(MAX_FILE_NAME_DIM);
		area.add( lblChosenMidicaPLFile, constraints );
		String fileType = SequenceParser.getFileType();
		String fileName = SequenceParser.getFileName();
		if ( "midica".equals(fileType) ) {
			lblChosenMidicaPLFile.setText( fileName );
		}
		
		// line
		constraints.gridy++;
		area.add( new JSeparator(JSeparator.HORIZONTAL), constraints );
		
		
		// midi label
		constraints.insets = new Insets( 0, 2, 0, 2 );
		constraints.gridy++;
		constraints.gridwidth = 1;
		JLabel lblMidi = new JLabel( Dict.get(Dict.MIDI_FILE) );
		area.add( lblMidi, constraints );
		constraints.insets = new Insets( 2, 2, 2, 2 );
		
		// file selector button
		constraints.gridx++;
		constraints.gridheight = 2;
		btnSelectMidi = new JButton( Dict.get(Dict.CHOOSE_FILE) );
		btnSelectMidi.setActionCommand( CMD_OPEN_MIDI_FILE );
		btnSelectMidi.addActionListener( controller );
		area.add( btnSelectMidi, constraints );
		
		// remember midicapl checkbox
		constraints.insets = new Insets( 0, 2, 0, 2 );
		constraints.gridx = 0;
		constraints.gridy++;
		constraints.gridheight = 1;
		cbxRememberMidi = new JCheckBox( Dict.get(Dict.REMEMBER_MID) );
		cbxRememberMidi.setToolTipText( Dict.get(Dict.REMEMBER_MID_TT) );
		cbxRememberMidi.addItemListener( controller );
		remember = Config.get( Config.REMEMBER_MIDI );
		if ( "true".equals(remember) ) {
			cbxRememberMidi.setSelected( true );
		}
		cbxRememberMidi.setName( NAME_REMEMBER_MIDI );
		area.add( cbxRememberMidi, constraints );
		constraints.insets = new Insets( 2, 2, 2, 2 );
		
		// chosen midi file name
		constraints.gridx = 0;
		constraints.gridy++;
		constraints.gridwidth = 2;
		lblChosenMidiFile = new JLabel( Dict.get(Dict.UNCHOSEN_FILE) );
		lblChosenMidiFile.setPreferredSize(MAX_FILE_NAME_DIM);
		area.add( lblChosenMidiFile, constraints );
		if ( "mid".equals(fileType) ) {
			lblChosenMidiFile.setText( fileName );
		}
		
		// line
		constraints.gridy++;
		area.add( new JSeparator(JSeparator.HORIZONTAL), constraints );
		
		
		// soundfont label
		constraints.insets = new Insets( 0, 2, 0, 2 );
		constraints.gridy++;
		constraints.gridwidth = 1;
		JLabel lblSndBnk = new JLabel( Dict.get(Dict.SOUNDFONT) );
		area.add( lblSndBnk, constraints );
		constraints.insets = new Insets( 2, 2, 2, 2 );
		
		// file selector button
		constraints.gridx++;
		constraints.gridheight = 2;
		btnSelectSoundfont = new JButton( Dict.get(Dict.CHOOSE_FILE) );
		btnSelectSoundfont.setActionCommand( CMD_OPEN_SNDFNT_FILE );
		btnSelectSoundfont.addActionListener( controller );
		area.add( btnSelectSoundfont, constraints );
		
		// remember soundfont checkbox
		constraints.insets = new Insets( 0, 2, 0, 2 );
		constraints.gridx = 0;
		constraints.gridy++;
		constraints.gridheight = 1;
		cbxRememberSf = new JCheckBox( Dict.get(Dict.REMEMBER_SF) );
		cbxRememberSf.setToolTipText( Dict.get(Dict.REMEMBER_SF_TT) );
		cbxRememberSf.addItemListener( controller );
		remember = Config.get( Config.REMEMBER_SF2 );
		if ( "true".equals(remember) ) {
			cbxRememberSf.setSelected( true );
		}
		cbxRememberSf.setName( NAME_REMEMBER_SF );
		area.add( cbxRememberSf, constraints );
		constraints.insets = new Insets( 2, 2, 2, 2 );
		
		// chosen soundfont file name
		constraints.gridy++;
		constraints.gridwidth = 2;
		lblChosenSoundfontFile = new JLabel( Dict.get(Dict.UNCHOSEN_FILE) );
		lblChosenSoundfontFile.setPreferredSize(MAX_FILE_NAME_DIM);
		area.add( lblChosenSoundfontFile, constraints );
		String soundfontFileName = SoundfontParser.getFileName();
		if ( soundfontFileName != null ) {
			lblChosenSoundfontFile.setText( soundfontFileName );
		}
		
		return area;
	}
	
	/**
	 * Creates the export area containing the sub areas for all exportable file types.
	 * 
	 * @return    The created area.
	 */
	private Container createExportArea() {
		JPanel area = new JPanel();
		area.setBorder( createTitledBorder(Dict.get(Dict.EXPORT)) );
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout( layout );
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.BOTH;
		constraints.insets     = new Insets( 2, 2, 2, 2 );
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 1;
		
		// transpose level label
		area.add( new JLabel(Dict.get(Dict.TRANSPOSE_LEVEL)), constraints );
		
		// transpose level
		constraints.gridx++;
		lblTranspose = new JLabel();
		setTransposeLevel( 0 );
		area.add( lblTranspose, constraints );
		
		// line
		constraints.gridx = 0;
		constraints.gridy++;
		constraints.gridwidth = 2;
		area.add( new JSeparator(JSeparator.HORIZONTAL), constraints );
		
		// midi label
		constraints.gridx = 0;
		constraints.gridy++;
		JLabel lblMidi = new JLabel( Dict.get(Dict.MIDI_EXPORT) );
		area.add( lblMidi, constraints );
		
		// file selector button
		constraints.gridx++;
		btnExportMidi = new JButton( Dict.get(Dict.CHOOSE_FILE_EXPORT) );
		btnExportMidi.setActionCommand( CMD_EXPORT_MIDI );
		btnExportMidi.addActionListener( controller );
		area.add( btnExportMidi, constraints );
		
		// line
		constraints.gridx = 0;
		constraints.gridy++;
		constraints.gridwidth = 2;
		area.add( new JSeparator(JSeparator.HORIZONTAL), constraints );
		
		// MidicaPL label
		constraints.gridwidth = 1;
		constraints.gridy++;
		JLabel lblMidicaPL = new JLabel( Dict.get(Dict.MIDICAPL_EXPORT) );
		area.add( lblMidicaPL, constraints );
		
		// file selector button
		constraints.gridx++;
		btnExportMidicaPL = new JButton( Dict.get(Dict.CHOOSE_FILE_EXPORT) );
		btnExportMidicaPL.setActionCommand( CMD_EXPORT_MIDICAPL );
		btnExportMidicaPL.setActionCommand( CMD_OPEN_FCT_NOT_READY ); // TODO: delete when implemented
		btnExportMidicaPL.addActionListener( controller );
		area.add( btnExportMidicaPL, constraints );
		
		return area;
	}
	
	/**
	 * Returns the label displaying the successfully imported MidicaPL file.
	 * 
	 * @return    Label displaying the file name.
	 */
	public JLabel getChosenMidicaPLFileLbl() {
		return lblChosenMidicaPLFile;
	}
	
	/**
	 * Returns the label displaying the successfully imported MIDI file.
	 * 
	 * @return    Label displaying the file name.
	 */
	public JLabel getChosenMidiFileLbl() {
		return lblChosenMidiFile;
	}
	
	/**
	 * Returns the label displaying the successfully imported soundfont file.
	 * 
	 * @return    Label displaying the file name.
	 */
	public JLabel getChosenSoundfontFileLbl() {
		return lblChosenSoundfontFile;
	}
	
	/**
	 * Creates a new titled and etched border for a named object grouping widget.
	 * This border is used to group and label:
	 * 
	 * - the configuration area
	 * - the player area
	 * - the import area
	 * - the export area
	 * 
	 * @param title    Label for the border.
	 * @return         Titled and etched border.
	 */
	private Border createTitledBorder( String title ) {
		Border etchedBorder = BorderFactory.createEtchedBorder();
		Border titledBorder = BorderFactory.createTitledBorder(
			etchedBorder,
			title,
			TitledBorder.RIGHT,
			TitledBorder.TOP
		);
		return titledBorder;
	}
	
	/**
	 * Closes the window.
	 * This is used after changing the language, followed by re-initializing the window.
	 */
	public void close() {
		dispose();
		setVisible( false );
	}
	
	/**
	 * Called if a remember-file checkbox has been checked.
	 * Makes sure that the checkboxes remember-midi and remember-midicapl
	 * are never checked both at the same time.
	 * 
	 * @param cbxName name of the checked checkbox
	 */
	public void rememberCheckboxChecked(String cbxName) {
		if (UiView.NAME_REMEMBER_MIDICAPL.equals(cbxName)) {
			cbxRememberMidi.setSelected(false);
		}
		else if (UiView.NAME_REMEMBER_MIDI.equals(cbxName)) {
			cbxRememberMidicapl.setSelected(false);
		}
	}
	
	/**
	 * Writes the current transpose level into the according label.
	 * Adjusts the font color of that label according to the transpose level.
	 * 
	 * @param level    Transpose level.
	 */
	public void setTransposeLevel( int level ) {
		lblTranspose.setText( Integer.toString(level) );
		if ( 0 == level )
			lblTranspose.setForeground( COLOR_TRANSPOSE_DEFAULT );
		else
			lblTranspose.setForeground( COLOR_TRANSPOSE_CHANGED );
	}
	
	/**
	 * Adds key bindings to the window.
	 * 
	 * The following bindings are added:
	 * 
	 * - I: Open info window.
	 * - P: Open the player.
	 */
	public void addKeyBindings() {
		
		if ( null == keyProcessor ) {
			keyProcessor = new KeyEventPostProcessor() {
				public boolean postProcessKeyEvent( KeyEvent e ) {
					
					if ( KeyEvent.KEY_PRESSED == e.getID() ) {
						
						// don't handle already consumed shortcuts any more
						if ( e.isConsumed() )
							return true;
						
						if ( KeyEvent.VK_ESCAPE == e.getKeyCode() ) {
							setVisible( false );
							return true;
						}
						
						switch ( e.getKeyCode() ) {
							case KeyEvent.VK_I:
								btnInfo.doClick();
								break;
							case KeyEvent.VK_P:
								btnPlayer.doClick();
								break;
							default:
								break;
						}
					}
					return e.isConsumed();
				}
			};
		}
		
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventPostProcessor( keyProcessor );
	}
	
	/**
	 * Removes all key bindings from the window.
	 */
	public void removeKeyBindings() {
		KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventPostProcessor( keyProcessor );
	}
}

