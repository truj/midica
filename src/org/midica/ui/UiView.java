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

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.midica.config.Cli;
import org.midica.config.Config;
import org.midica.config.Dict;
import org.midica.config.KeyBindingManager;
import org.midica.config.Laf;
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
	public static final String NAME_SELECT_SHARP_FLAT = "name_select_sharp_flat";
	public static final String NAME_SELECT_OCTAVE     = "name_select_octave";
	public static final String NAME_SELECT_SYNTAX     = "name_select_syntax";
	public static final String NAME_SELECT_PERCUSSION = "name_select_percussion";
	public static final String NAME_SELECT_INSTRUMENT = "name_select_instrument";
	public static final String NAME_REMEMBER_SOUND    = "name_remember_sound";
	public static final String NAME_REMEMBER_IMPORT   = "name_remember_import";
	
	public static final String CMD_SELECT_LANGUAGE     = "select_language";
	public static final String CMD_START_PLAYER        = "start_player";
	public static final String CMD_IMPORT              = "cmd_import";
	public static final String CMD_OPEN_SNDFNT_FILE    = "cmd_open_soundfont_file";
	public static final String CMD_SHOW_INFO_WINDOW    = "cmd_show_info_window";
	public static final String CMD_EXPORT              = "cmd_export";
	
	// application icons
	private static final String    APP_ICON_PATH = "org/midica/resources/app-icon.png";
	private static       ImageIcon appIcon       = null;
	
	// make file names in the import section as small as possible
	private static final Dimension MAX_FILE_NAME_DIM = new Dimension(0, new JLabel(" ").getPreferredSize().height);
	
	private KeyBindingManager               keyBindingManager      = null;
	private Container                       content                = null;
	private UiController                    controller             = null;
	private JLabel                          lblChosenImportedFile  = null;
	private JLabel                          lblChosenImportedType  = null;
	private JLabel                          lblChosenSoundfontFile = null;
	private JComboBox<ComboboxStringOption> cbxGuiLang             = null;
	private JComboBox<ComboboxStringOption> cbxNoteSys             = null;
	private JComboBox<ComboboxStringOption> cbxHalfTone            = null;
	private JComboBox<ComboboxStringOption> cbxSharpFlat           = null;
	private JComboBox<ComboboxStringOption> cbxOctave              = null;
	private JComboBox<ComboboxStringOption> cbxSyntax              = null;
	private JComboBox<ComboboxStringOption> cbxPercussion          = null;
	private JComboBox<ComboboxStringOption> cbxInstrument          = null;
	private JLabel                          lblTranspose           = null;
	private MidicaButton                    btnInfo                = null;
	private MidicaButton                    btnPlayer              = null;
	private MidicaButton                    btnSelectImport        = null;
	private MidicaButton                    btnSelectSoundfont     = null;
	private MidicaButton                    btnExport              = null;
	private JCheckBox                       cbxRememberSound       = null;
	private JCheckBox                       cbxRememberImport      = null;
	
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
		
		if (!Cli.isCliMode)
			appIcon = new ImageIcon(ClassLoader.getSystemResource(APP_ICON_PATH));
		
		init();
		addKeyBindings();
		
		// don't show the window in CLI mode
		if (Cli.isCliMode) {
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
		constraints.fill       = GridBagConstraints.BOTH;
		constraints.insets     = Laf.INSETS_WNS;
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		
		// left area (config and player)
		content.add(createLeftArea(), constraints);
		
		// right area (import and export)
		constraints.gridx++;
		constraints.insets = Laf.INSETS_ENS;
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
		constraints.fill       = GridBagConstraints.HORIZONTAL;
		constraints.insets     = Laf.INSETS_IN;
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 1;
		constraints.weighty    = 0;
		
		// config area
		area.add(createConfigArea(), constraints);
		
		// spacer
		constraints.gridy++;
		constraints.insets  = Laf.INSETS_ZERO;
		constraints.weighty = 1;
		area.add(Box.createGlue(), constraints);
		
		// player area
		constraints.gridy++;
		constraints.insets  = Laf.INSETS_IN;
		constraints.weighty = 0;
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
		constraints.fill       = GridBagConstraints.HORIZONTAL;
		constraints.insets     = Laf.INSETS_IN;
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 1;
		constraints.weighty    = 0;
		
		// import area
		area.add(createImportArea(), constraints);
		
		// spacer
		constraints.gridy++;
		constraints.insets  = Laf.INSETS_ZERO;
		constraints.weighty = 1;
		area.add(Box.createGlue(), constraints);
		
		// soundfont area
		constraints.gridy++;
		constraints.insets  = Laf.INSETS_IN;
		constraints.weighty = 0;
		area.add(createSoundfontArea(), constraints);
		
		// spacer
		constraints.gridy++;
		constraints.insets  = Laf.INSETS_ZERO;
		constraints.weighty = 1;
		area.add(Box.createGlue(), constraints);
		
		// export area
		constraints.gridy++;
		constraints.insets  = Laf.INSETS_IN;
		constraints.weighty = 0;
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
		
		// sharp/flat label
		constraints.gridx = 0;
		constraints.gridy++;
		JLabel lblSharpFlat = new JLabel(Dict.get(Dict.SHARP_FLAT_DEFAULT));
		Laf.makeBold(lblSharpFlat);
		area.add(lblSharpFlat, constraints);
		
		// sharp/flat selection
		constraints.gridx++;
		cbxSharpFlat = new JComboBox<ComboboxStringOption>();
		cbxSharpFlat.setName(NAME_SELECT_SHARP_FLAT);
		cbxSharpFlat.setModel(ConfigComboboxModel.getModel(Config.SHARP_FLAT));
		cbxSharpFlat.addActionListener(controller);
		
		area.add(cbxSharpFlat, constraints);
		
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
	 * Creates the import area with the import selection button, the remember checkbox and
	 * labels for the imported file name and type.
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
		
		// import label
		constraints.insets = Laf.INSETS_LBL_IMPORT_EXPORT;
		JLabel lblMidicaPL = new JLabel(Dict.get(Dict.IMPORT_FILE));
		Laf.makeBold(lblMidicaPL);
		area.add(lblMidicaPL, constraints);
		constraints.insets = Laf.INSETS_IN;
		
		// file selector button
		constraints.gridx++;
		constraints.gridheight = 2;
		btnSelectImport = new MidicaButton(Dict.get(Dict.CHOOSE_FILE));
		btnSelectImport.setActionCommand(CMD_IMPORT);
		btnSelectImport.addActionListener(controller);
		area.add(btnSelectImport, constraints);
		
		// remember import file checkbox
		constraints.insets = Laf.INSETS_LBL_IMPORT_EXPORT;
		constraints.gridx  = 0;
		constraints.gridy++;
		constraints.gridheight = 1;
		cbxRememberImport = new JCheckBox(Dict.get(Dict.REMEMBER_IMPORT));
		cbxRememberImport.setToolTipText(Dict.get(Dict.REMEMBER_IMPORT_TT));
		cbxRememberImport.addItemListener(controller);
		String remember = Config.get(Config.REMEMBER_IMPORT);
		if ("true".equals(remember)) {
			cbxRememberImport.setSelected(true);
		}
		cbxRememberImport.setName(NAME_REMEMBER_IMPORT);
		area.add(cbxRememberImport, constraints);
		constraints.insets = Laf.INSETS_IN;
		
		// line
		constraints.gridy++;
		constraints.gridwidth = 2;
		area.add(Laf.createSeparator(), constraints);
		
		// file name label
		constraints.gridy++;
		JLabel lblImportedFile = new JLabel(Dict.get(Dict.IMPORTED_FILE));
		Laf.makeBold(lblImportedFile);
		area.add(lblImportedFile, constraints);
		constraints.insets = Laf.INSETS_IN;
		
		// chosen file name
		constraints.gridy++;
		constraints.gridwidth = 2;
		lblChosenImportedFile = new JLabel(Dict.get(Dict.UNCHOSEN_FILE));
		lblChosenImportedFile.setPreferredSize(MAX_FILE_NAME_DIM);
		area.add(lblChosenImportedFile, constraints);
		
		// line
		constraints.gridy++;
		constraints.gridwidth = 2;
		area.add(Laf.createSeparator(), constraints);
		
		// file type label
		constraints.gridy++;
		JLabel lblImportedType = new JLabel(Dict.get(Dict.IMPORTED_TYPE));
		Laf.makeBold(lblImportedType);
		area.add(lblImportedType, constraints);
		constraints.insets = Laf.INSETS_IN;
		
		// chosen file type
		constraints.gridy++;
		constraints.gridwidth = 2;
		lblChosenImportedType = new JLabel(Dict.get(Dict.UNCHOSEN_FILE));
		area.add(lblChosenImportedType, constraints);
		
		// make sure that the file type/name labels are correct, even after a language change
		controller.updateImportedFileTypeAndName();
		
		return area;
	}
	
	/**
	 * Creates the soundfont area with the selection button, the remember checkbox and
	 * a label for the loaded soundfont file name.
	 * 
	 * @return    The created area.
	 */
	private Container createSoundfontArea() {
		JPanel area = new JPanel();
		area.setBorder( Laf.createTitledBorder(Dict.get(Dict.SOUNDBANK)) );
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.BOTH;
		constraints.insets     = Laf.INSETS_LBL_IMPORT_EXPORT;
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 0.5;
		
		// soundbank label
		JLabel lblSndBnk = new JLabel(Dict.get(Dict.SOUNDBANK));
		Laf.makeBold(lblSndBnk);
		area.add(lblSndBnk, constraints);
		constraints.insets = Laf.INSETS_IN;
		
		// file selector button
		constraints.gridx++;
		constraints.gridheight = 2;
		btnSelectSoundfont = new MidicaButton(Dict.get(Dict.CHOOSE_FILE));
		btnSelectSoundfont.setActionCommand(CMD_OPEN_SNDFNT_FILE);
		btnSelectSoundfont.addActionListener(controller);
		area.add(btnSelectSoundfont, constraints);
		
		// remember sound checkbox
		constraints.insets = Laf.INSETS_LBL_IMPORT_EXPORT;
		constraints.gridx  = 0;
		constraints.gridy++;
		constraints.gridheight = 1;
		cbxRememberSound = new JCheckBox(Dict.get(Dict.REMEMBER_SOUND));
		cbxRememberSound.setToolTipText(Dict.get(Dict.REMEMBER_SOUND_TT));
		cbxRememberSound.addItemListener(controller);
		String remember = Config.get(Config.REMEMBER_SOUND);
		if ("true".equals(remember)) {
			cbxRememberSound.setSelected(true);
		}
		cbxRememberSound.setName(NAME_REMEMBER_SOUND);
		area.add(cbxRememberSound, constraints);
		constraints.insets = Laf.INSETS_IN;
		
		// line
		constraints.gridy++;
		constraints.gridwidth = 2;
		area.add(Laf.createSeparator(), constraints);
		
		// file name label
		constraints.gridy++;
		JLabel lblImportedFile = new JLabel(Dict.get(Dict.CURRENT_SOUNDBANK));
		Laf.makeBold(lblImportedFile);
		area.add(lblImportedFile, constraints);
		constraints.insets = Laf.INSETS_IN;
		
		// chosen soundfont file name
		constraints.gridy++;
		lblChosenSoundfontFile = new JLabel(Dict.get(Dict.UNCHOSEN_FILE));
		lblChosenSoundfontFile.setPreferredSize(MAX_FILE_NAME_DIM);
		area.add(lblChosenSoundfontFile, constraints);
		String soundShortName = SoundfontParser.getShortName();
		String soundFullPath  = SoundfontParser.getFullPath();
		if (soundShortName != null) { // may be != null after switching the language
			lblChosenSoundfontFile.setText(soundShortName);
			lblChosenSoundfontFile.setToolTipText(soundFullPath);
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
		
		// export label
		constraints.gridx = 0;
		constraints.gridy++;
		JLabel lblMidi = new JLabel(Dict.get(Dict.EXPORT_FILE));
		Laf.makeBold(lblMidi);
		area.add(lblMidi, constraints);
		
		// file selector button
		constraints.gridx++;
		btnExport = new MidicaButton(Dict.get(Dict.CHOOSE_FILE_EXPORT));
		btnExport.setActionCommand(CMD_EXPORT);
		btnExport.addActionListener(controller);
		area.add(btnExport, constraints);
		
		return area;
	}
	
	/**
	 * Returns the label displaying the successfully imported sequence file.
	 * 
	 * @return    Label displaying the file name.
	 */
	public JLabel getImportedFileLbl() {
		return lblChosenImportedFile;
	}
	
	/**
	 * Returns the label displaying the successfully imported file type.
	 * 
	 * @return    Label displaying the file name.
	 */
	public JLabel getImportedFileTypeLbl() {
		return lblChosenImportedType;
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
	 * Returns the config comboboxes.
	 * Used for unit-testing.
	 * 
	 * @return the config comboboxes.
	 */
	public JComboBox<?>[] getConfigComboboxes() {
		return new JComboBox<?>[] {
			cbxGuiLang,
			cbxNoteSys,
			cbxHalfTone,
			cbxSharpFlat,
			cbxOctave,
			cbxSyntax,
			cbxPercussion,
			cbxInstrument,
		};
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
		keyBindingManager.addBindingsForButton( this.btnSelectImport,    Dict.KEY_MAIN_IMPORT           );
		keyBindingManager.addBindingsForButton( this.btnSelectSoundfont, Dict.KEY_MAIN_IMPORT_SF        );
		keyBindingManager.addBindingsForButton( this.btnExport,          Dict.KEY_MAIN_EXPORT           );
		
		// add key bindings to focus comboboxes
		keyBindingManager.addBindingsForComboboxOpen( this.cbxGuiLang,    Dict.KEY_MAIN_CBX_LANGUAGE   );
		keyBindingManager.addBindingsForComboboxOpen( this.cbxNoteSys,    Dict.KEY_MAIN_CBX_NOTE       );
		keyBindingManager.addBindingsForComboboxOpen( this.cbxHalfTone,   Dict.KEY_MAIN_CBX_HALFTONE   );
		keyBindingManager.addBindingsForComboboxOpen( this.cbxSharpFlat,  Dict.KEY_MAIN_CBX_SHARPFLAT  );
		keyBindingManager.addBindingsForComboboxOpen( this.cbxOctave,     Dict.KEY_MAIN_CBX_OCTAVE     );
		keyBindingManager.addBindingsForComboboxOpen( this.cbxSyntax,     Dict.KEY_MAIN_CBX_SYNTAX     );
		keyBindingManager.addBindingsForComboboxOpen( this.cbxPercussion, Dict.KEY_MAIN_CBX_PERCUSSION );
		keyBindingManager.addBindingsForComboboxOpen( this.cbxInstrument, Dict.KEY_MAIN_CBX_INSTRUMENT );
		
		// postprocess key bindings
		keyBindingManager.postprocess();
	}
}
