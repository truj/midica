/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.midica.Midica;
import org.midica.config.Config;
import org.midica.config.Dict;
import org.midica.config.KeyBindingManager;
import org.midica.config.Laf;
import org.midica.file.read.SequenceParser;
import org.midica.file.read.SoundfontParser;
import org.midica.ui.model.ComboboxStringOption;
import org.midica.ui.model.ConfigComboboxModel;
import org.midica.ui.widget.MidicaButton;

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
	public static final String CMD_EXPORT_DECOMPILE    = "cmd_export_decompile";
	
	// application icons
	private static final String    APP_ICON_PATH = "org/midica/resources/app-icon.png";
	private static       ImageIcon appIcon       = null;
	
	// make file names in the import section as small as possible
	private static final Dimension MAX_FILE_NAME_DIM = new Dimension(0, new JLabel(" ").getPreferredSize().height);
	
	private KeyBindingManager               keyBindingManager      = null;
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
	private MidicaButton                    btnInfo                = null;
	private MidicaButton                    btnPlayer              = null;
	private MidicaButton                    btnSelectMidicaPL      = null;
	private MidicaButton                    btnSelectMidi          = null;
	private MidicaButton                    btnSelectSoundfont     = null;
	private MidicaButton                    btnExportMidi          = null;
	private MidicaButton                    btnExportDecompile     = null;
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
	public UiView(UiController controller) {
		super(Dict.get(Dict.TITLE_MAIN_WINDOW));
		this.controller = controller;
		addWindowListener(controller);
		
		if (!Midica.isCliMode)
			appIcon = new ImageIcon(ClassLoader.getSystemResource(APP_ICON_PATH));
		
		init();
		addKeyBindings();
		
		// don't show the window in CLI mode
		if (Midica.isCliMode) {
			return;
		}
		
		// set app icon
		setIconImage(appIcon.getImage());
		
		pack();
		setVisible(true);
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
		content.setLayout(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.NONE;
		constraints.insets     = Laf.INSETS_WNS;
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		
		// left area (config and player)
		content.add(createLeftArea(), constraints);
		
		// right area (import and export)
		constraints.insets = Laf.INSETS_ENS;
		constraints.gridx++;
		content.add(createRightArea(), constraints);
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
		area.setLayout(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.BOTH;
		constraints.insets     = Laf.INSETS_IN;
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		
		// config area
		area.add(createConfigArea(), constraints);
		
		// player area
		constraints.gridy++;
		area.add(createPlayerArea(), constraints);
		
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
		area.setLayout(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.BOTH;
		constraints.insets     = Laf.INSETS_IN;
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		
		// import area
		area.add(createImportArea(), constraints);
		
		// export area
		constraints.gridy++;
		area.add(createExportArea(), constraints);
		
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
		area.setBorder( Laf.createTitledBorder(Dict.get(Dict.CONFIGURATION)) );
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.BOTH;
		constraints.insets     = Laf.INSETS_IN;
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		
		// gui language label
		JLabel lblLanguage = new JLabel(Dict.get(Dict.LANGUAGE));
		Laf.makeBold(lblLanguage);
		area.add(lblLanguage, constraints);
		
		// gui language selection
		constraints.gridx++;
		cbxGuiLang = new JComboBox<ComboboxStringOption>();
		cbxGuiLang.setName(NAME_SELECT_LANGUAGE);
		cbxGuiLang.setModel(ConfigComboboxModel.getModel(Config.LANGUAGE));
		cbxGuiLang.addActionListener(controller);
		area.add(cbxGuiLang, constraints);
		
		// note system label
		constraints.gridx = 0;
		constraints.gridy++;
		JLabel lblNoteSys = new JLabel(Dict.get(Dict.NOTE_SYSTEM));
		Laf.makeBold(lblNoteSys);
		area.add(lblNoteSys, constraints);
		
		// note system selection
		constraints.gridx++;
		cbxNoteSys = new JComboBox<ComboboxStringOption> ();
		cbxNoteSys.setName(NAME_SELECT_SYSTEM);
		cbxNoteSys.setModel(ConfigComboboxModel.getModel(Config.NOTE));
		cbxNoteSys.addActionListener(controller);
		
		area.add(cbxNoteSys, constraints);
		
		// half tone label
		constraints.gridx = 0;
		constraints.gridy++;
		JLabel lblHalfTone = new JLabel(Dict.get(Dict.HALF_TONE_SYMBOL));
		Laf.makeBold(lblHalfTone);
		area.add(lblHalfTone, constraints);
		
		// half tone selection
		constraints.gridx++;
		cbxHalfTone = new JComboBox<ComboboxStringOption>();
		cbxHalfTone.setName(NAME_SELECT_HALF_TONE);
		cbxHalfTone.setModel(ConfigComboboxModel.getModel(Config.HALF_TONE));
		cbxHalfTone.addActionListener(controller);
		
		area.add(cbxHalfTone, constraints);
		
		// octave naming label
		constraints.gridx = 0;
		constraints.gridy++;
		JLabel lblOctave = new JLabel(Dict.get(Dict.OCTAVE_NAMING));
		Laf.makeBold(lblOctave);
		area.add(lblOctave, constraints);
		
		// octave selection
		constraints.gridx++;
		cbxOctave = new JComboBox<ComboboxStringOption>();
		cbxOctave.setName(NAME_SELECT_OCTAVE);
		cbxOctave.setModel(ConfigComboboxModel.getModel(Config.OCTAVE));
		cbxOctave.addActionListener(controller);
		
		area.add(cbxOctave, constraints);
		
		// syntax label
		constraints.gridx = 0;
		constraints.gridy++;
		JLabel lblSyntax = new JLabel(Dict.get(Dict.SYNTAX));
		Laf.makeBold(lblSyntax);
		area.add(lblSyntax, constraints);
		
		// syntax selection
		constraints.gridx++;
		cbxSyntax = new JComboBox<ComboboxStringOption>();
		cbxSyntax.setName(NAME_SELECT_SYNTAX);
		cbxSyntax.setModel(ConfigComboboxModel.getModel(Config.SYNTAX));
		cbxSyntax.addActionListener(controller);
		
		area.add(cbxSyntax, constraints);
		
		// percussion label
		constraints.gridx = 0;
		constraints.gridy++;
		JLabel lblPercussion = new JLabel(Dict.get(Dict.PERCUSSION));
		Laf.makeBold(lblPercussion);
		area.add(lblPercussion, constraints);
		
		// percussion selection
		constraints.gridx++;
		cbxPercussion = new JComboBox<ComboboxStringOption>();
		cbxPercussion.setName(NAME_SELECT_PERCUSSION);
		cbxPercussion.setModel(ConfigComboboxModel.getModel(Config.PERCUSSION));
		cbxPercussion.addActionListener(controller);
		
		area.add(cbxPercussion, constraints);
		
		// instrument naming label
		constraints.gridx = 0;
		constraints.gridy++;
		JLabel lblInstrument = new JLabel(Dict.get(Dict.INSTRUMENT_IDS));
		Laf.makeBold(lblInstrument);
		area.add(lblInstrument, constraints);
		
		// instrument naming selection
		constraints.gridx++;
		cbxInstrument = new JComboBox<ComboboxStringOption>();
		cbxInstrument.setName(NAME_SELECT_INSTRUMENT);
		cbxInstrument.setModel(ConfigComboboxModel.getModel(Config.INSTRUMENT));
		cbxInstrument.addActionListener(controller);
		
		area.add(cbxInstrument, constraints);
		
		// info button
		constraints.gridx = 1;
		constraints.gridy++;
		constraints.gridwidth = 1;
		btnInfo = new MidicaButton(Dict.get(Dict.SHOW_INFO));
		btnInfo.setActionCommand(CMD_SHOW_INFO_WINDOW);
		btnInfo.addActionListener(controller);
		
		area.add(btnInfo, constraints);
		
		return area;
	}
	
	/**
	 * Creates the player area containing the button to open the player.
	 * 
	 * @return    The created area.
	 */
	private Container createPlayerArea() {
		JPanel area = new JPanel();
		area.setBorder( Laf.createTitledBorder(Dict.get(Dict.PLAYER)) );
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.BOTH;
		constraints.insets     = Laf.INSETS_IN;
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		
		// player button
		constraints.gridy++;
		constraints.gridx   = 0;
		constraints.weightx = 1;
		btnPlayer = new MidicaButton(Dict.get(Dict.PLAYER_BUTTON), true);
		btnPlayer.setActionCommand(CMD_START_PLAYER);
		btnPlayer.addActionListener(controller);
		area.add(btnPlayer, constraints);
		
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
		area.setBorder( Laf.createTitledBorder(Dict.get(Dict.IMPORT)) );
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.BOTH;
		constraints.insets     = Laf.INSETS_IN;
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 0.5;
		
		// MidicaPL label
		constraints.insets = Laf.INSETS_LBL_IMPORT_EXPORT;
		JLabel lblMidicaPL = new JLabel(Dict.get(Dict.MIDICAPL_FILE));
		Laf.makeBold(lblMidicaPL);
		area.add(lblMidicaPL, constraints);
		constraints.insets = Laf.INSETS_IN;
		
		// file selector button
		constraints.gridx++;
		constraints.gridheight = 2;
		btnSelectMidicaPL = new MidicaButton(Dict.get(Dict.CHOOSE_FILE));
		btnSelectMidicaPL.setActionCommand(CMD_OPEN_MIDICAPL_FILE);
		btnSelectMidicaPL.addActionListener(controller);
		area.add(btnSelectMidicaPL, constraints);
		
		// remember midicapl checkbox
		constraints.insets = Laf.INSETS_LBL_IMPORT_EXPORT;
		constraints.gridx  = 0;
		constraints.gridy++;
		constraints.gridheight = 1;
		cbxRememberMidicapl = new JCheckBox(Dict.get(Dict.REMEMBER_MPL));
		cbxRememberMidicapl.setToolTipText(Dict.get(Dict.REMEMBER_MPL_TT));
		cbxRememberMidicapl.addItemListener(controller);
		String remember = Config.get(Config.REMEMBER_MIDICAPL);
		if ("true".equals(remember)) {
			cbxRememberMidicapl.setSelected(true);
		}
		cbxRememberMidicapl.setName(NAME_REMEMBER_MIDICAPL);
		area.add(cbxRememberMidicapl, constraints);
		constraints.insets = Laf.INSETS_IN;
		
		// chosen MidicaPL file name
		constraints.gridy++;
		constraints.gridwidth = 2;
		lblChosenMidicaPLFile = new JLabel(Dict.get(Dict.UNCHOSEN_FILE));
		lblChosenMidicaPLFile.setPreferredSize(MAX_FILE_NAME_DIM);
		area.add(lblChosenMidicaPLFile, constraints);
		String fileType = SequenceParser.getFileType();
		String fileName = SequenceParser.getFileName();
		if ("midica".equals(fileType)) {
			lblChosenMidicaPLFile.setText(fileName);
		}
		
		// line
		constraints.gridy++;
		area.add(Laf.createSeparator(), constraints);
		
		// midi label
		constraints.insets = Laf.INSETS_LBL_IMPORT_EXPORT;
		constraints.gridy++;
		constraints.gridwidth = 1;
		JLabel lblMidi = new JLabel(Dict.get(Dict.MIDI_FILE));
		Laf.makeBold(lblMidi);
		area.add(lblMidi, constraints);
		constraints.insets = Laf.INSETS_IN;
		
		// file selector button
		constraints.gridx++;
		constraints.gridheight = 2;
		btnSelectMidi = new MidicaButton(Dict.get(Dict.CHOOSE_FILE));
		btnSelectMidi.setActionCommand(CMD_OPEN_MIDI_FILE);
		btnSelectMidi.addActionListener(controller);
		area.add(btnSelectMidi, constraints);
		
		// remember midicapl checkbox
		constraints.insets = Laf.INSETS_LBL_IMPORT_EXPORT;
		constraints.gridx  = 0;
		constraints.gridy++;
		constraints.gridheight = 1;
		cbxRememberMidi = new JCheckBox(Dict.get(Dict.REMEMBER_MID));
		cbxRememberMidi.setToolTipText(Dict.get(Dict.REMEMBER_MID_TT));
		cbxRememberMidi.addItemListener(controller);
		remember = Config.get(Config.REMEMBER_MIDI);
		if ("true".equals(remember)) {
			cbxRememberMidi.setSelected(true);
		}
		cbxRememberMidi.setName(NAME_REMEMBER_MIDI);
		area.add(cbxRememberMidi, constraints);
		constraints.insets = Laf.INSETS_IN;
		
		// chosen midi file name
		constraints.gridx = 0;
		constraints.gridy++;
		constraints.gridwidth = 2;
		lblChosenMidiFile = new JLabel(Dict.get(Dict.UNCHOSEN_FILE));
		lblChosenMidiFile.setPreferredSize(MAX_FILE_NAME_DIM);
		area.add(lblChosenMidiFile, constraints);
		if ("mid".equals(fileType)) {
			lblChosenMidiFile.setText(fileName);
		}
		
		// line
		constraints.gridy++;
		area.add(Laf.createSeparator(), constraints);
		
		// soundfont label
		constraints.insets = Laf.INSETS_LBL_IMPORT_EXPORT;
		constraints.gridy++;
		constraints.gridwidth = 1;
		JLabel lblSndFont = new JLabel(Dict.get(Dict.SOUNDFONT));
		Laf.makeBold(lblSndFont);
		area.add(lblSndFont, constraints);
		constraints.insets = Laf.INSETS_IN;
		
		// file selector button
		constraints.gridx++;
		constraints.gridheight = 2;
		btnSelectSoundfont = new MidicaButton(Dict.get(Dict.CHOOSE_FILE));
		btnSelectSoundfont.setActionCommand(CMD_OPEN_SNDFNT_FILE);
		btnSelectSoundfont.addActionListener(controller);
		area.add(btnSelectSoundfont, constraints);
		
		// remember soundfont checkbox
		constraints.insets = Laf.INSETS_LBL_IMPORT_EXPORT;
		constraints.gridx  = 0;
		constraints.gridy++;
		constraints.gridheight = 1;
		cbxRememberSf = new JCheckBox(Dict.get(Dict.REMEMBER_SF));
		cbxRememberSf.setToolTipText(Dict.get(Dict.REMEMBER_SF_TT));
		cbxRememberSf.addItemListener(controller);
		remember = Config.get(Config.REMEMBER_SF2);
		if ("true".equals(remember)) {
			cbxRememberSf.setSelected(true);
		}
		cbxRememberSf.setName(NAME_REMEMBER_SF);
		area.add(cbxRememberSf, constraints);
		constraints.insets = Laf.INSETS_IN;
		
		// chosen soundfont file name
		constraints.gridy++;
		constraints.gridwidth = 2;
		lblChosenSoundfontFile = new JLabel(Dict.get(Dict.UNCHOSEN_FILE));
		lblChosenSoundfontFile.setPreferredSize(MAX_FILE_NAME_DIM);
		area.add(lblChosenSoundfontFile, constraints);
		String soundfontFileName = SoundfontParser.getFileName();
		if (soundfontFileName != null) {
			lblChosenSoundfontFile.setText(soundfontFileName);
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
		area.setBorder( Laf.createTitledBorder(Dict.get(Dict.EXPORT)) );
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.BOTH;
		constraints.insets     = Laf.INSETS_IN;
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 1;
		
		// transpose level label
		area.add(new JLabel(Dict.get(Dict.TRANSPOSE_LEVEL)), constraints);
		
		// transpose level
		constraints.gridx++;
		lblTranspose = new JLabel();
		Laf.makeBold(lblTranspose);
		setTransposeLevel(0);
		area.add(lblTranspose, constraints);
		
		// line
		constraints.gridx = 0;
		constraints.gridy++;
		constraints.gridwidth = 2;
		area.add(Laf.createSeparator(), constraints);
		
		// midi label
		constraints.gridx = 0;
		constraints.gridy++;
		JLabel lblMidi = new JLabel(Dict.get(Dict.MIDI_EXPORT));
		Laf.makeBold(lblMidi);
		area.add(lblMidi, constraints);
		
		// file selector button
		constraints.gridx++;
		btnExportMidi = new MidicaButton(Dict.get(Dict.CHOOSE_FILE_EXPORT));
		btnExportMidi.setActionCommand(CMD_EXPORT_MIDI);
		btnExportMidi.addActionListener(controller);
		area.add(btnExportMidi, constraints);
		
		// line
		constraints.gridx = 0;
		constraints.gridy++;
		constraints.gridwidth = 2;
		area.add(Laf.createSeparator(), constraints);
		
		// decompile label
		constraints.gridwidth = 1;
		constraints.gridy++;
		JLabel lblDecompile = new JLabel(Dict.get(Dict.DECOMPILE_EXPORT));
		Laf.makeBold(lblDecompile);
		area.add(lblDecompile, constraints);
		
		// file selector button
		constraints.gridx++;
		btnExportDecompile = new MidicaButton(Dict.get(Dict.CHOOSE_FILE_EXPORT));
		btnExportDecompile.setActionCommand(CMD_EXPORT_DECOMPILE);
		btnExportDecompile.addActionListener(controller);
		area.add(btnExportDecompile, constraints);
		
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
	 * Closes the window.
	 * This is used after changing the language, followed by re-initializing the window.
	 */
	public void close() {
		dispose();
		setVisible(false);
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
	public void setTransposeLevel(int level) {
		lblTranspose.setText(Integer.toString(level));
		if (0 == level)
			lblTranspose.setForeground(Laf.COLOR_TRANSPOSE_DEFAULT);
		else
			lblTranspose.setForeground(Laf.COLOR_TRANSPOSE_CHANGED);
	}
	
	/**
	 * Adds key bindings to the window.
	 */
	private void addKeyBindings() {
		
		// reset everything
		keyBindingManager = new KeyBindingManager(this, this.getRootPane());
		
		// add key bindings to buttons
		keyBindingManager.addBindingsForButton( this.btnInfo,            Dict.KEY_MAIN_INFO             );
		keyBindingManager.addBindingsForButton( this.btnPlayer,          Dict.KEY_MAIN_PLAYER           );
		keyBindingManager.addBindingsForButton( this.btnSelectMidicaPL,  Dict.KEY_MAIN_IMPORT_MPL       );
		keyBindingManager.addBindingsForButton( this.btnSelectMidi,      Dict.KEY_MAIN_IMPORT_MID       );
		keyBindingManager.addBindingsForButton( this.btnSelectSoundfont, Dict.KEY_MAIN_IMPORT_SF        );
		keyBindingManager.addBindingsForButton( this.btnExportMidi,      Dict.KEY_MAIN_EXPORT_MID       );
		keyBindingManager.addBindingsForButton( this.btnExportDecompile, Dict.KEY_MAIN_EXPORT_DECOMPILE );
		
		// add key bindings to focus comboboxes
		keyBindingManager.addBindingsForComboboxOpen( this.cbxGuiLang,    Dict.KEY_MAIN_CBX_LANGUAGE   );
		keyBindingManager.addBindingsForComboboxOpen( this.cbxNoteSys,    Dict.KEY_MAIN_CBX_NOTE       );
		keyBindingManager.addBindingsForComboboxOpen( this.cbxHalfTone,   Dict.KEY_MAIN_CBX_HALFTONE   );
		keyBindingManager.addBindingsForComboboxOpen( this.cbxOctave,     Dict.KEY_MAIN_CBX_OCTAVE     );
		keyBindingManager.addBindingsForComboboxOpen( this.cbxSyntax,     Dict.KEY_MAIN_CBX_SYNTAX     );
		keyBindingManager.addBindingsForComboboxOpen( this.cbxPercussion, Dict.KEY_MAIN_CBX_PERCUSSION );
		keyBindingManager.addBindingsForComboboxOpen( this.cbxInstrument, Dict.KEY_MAIN_CBX_INSTRUMENT );
		
		// postprocess key bindings
		keyBindingManager.postprocess();
	}
}

