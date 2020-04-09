/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.file;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.midica.config.Config;
import org.midica.config.Dict;
import org.midica.config.KeyBindingManager;
import org.midica.config.Laf;
import org.midica.config.NamedInteger;
import org.midica.ui.widget.DecompileConfigIcon;
import org.midica.ui.widget.MidicaButton;

/**
 * This class provides the configuration window for decompile options.
 * 
 * @author Jan Trukenmüller
 */
public class DecompileConfigView extends JDialog {
	
	private static final long serialVersionUID = 1L;
	
	// identifier property name for text documents
	public static final String DOC_ID = "doc_id";
	
	// identifier property values for text documents
	public static final Integer DOC_ID_ADD_GLOBAL_AT_TICK = 1;
	public static final Integer DOC_ID_ADD_GLOBAL_EACH    = 2;
	public static final Integer DOC_ID_ADD_GLOBAL_START   = 3;
	public static final Integer DOC_ID_ADD_GLOBAL_STOP    = 4;
	public static final Integer DOC_ID_UPDATE_GLOBAL_ALL  = 5;
	
	private static final int TEXT_FIELD_WIDTH  = 150;
	private static final int TEXT_FIELD_HEIGHT =  30;
	private static final int TEXT_AREA_HEIGHT  = 150;
	
	private DecompileConfigController controller;
	private DecompileConfigIcon       icon;
	
	// widgets that change dc config values
	JCheckBox               cbxAddTickComments;
	JCheckBox               cbxAddConfig;
	JCheckBox               cbxAddScore;
	JCheckBox               cbxAddStatistics;
	JComboBox<NamedInteger> cbxLengthStrategy;
	JComboBox<NamedInteger> cbxMaxTargetTicksOn;
	JTextField              fldNextNoteOnTolerance;
	JTextField              fldMinDurToKeep;
	JTextField              fldDurationTickTolerance;
	JTextField              fldDurationRatioTolerance;
	JCheckBox               cbxPredefinedChords;
	JTextField              fldChordNoteOnTolerance;
	JTextField              fldChordNoteOffTolerance;
	JTextField              fldChordVelocityTolerance;
	JCheckBox               cbxUseDottedNote;
	JCheckBox               cbxUseDottedRest;
	JCheckBox               cbxUseTriplettedNote;
	JCheckBox               cbxUseTriplettedRest;
	JComboBox<NamedInteger> cbxOrphanedSyllables;
	JCheckBox               cbxKarOneChannel;
	JTextField              fldAddGlobalAtTick;
	MidicaButton            btnAddGlobalAtTick;
	JTextField              fldAddGlobalsEachTick;
	JTextField              fldAddGlobalsStartTick;
	JTextField              fldAddGlobalsStopTick;
	MidicaButton            btnAddGlobalTicks;
	JTextArea               areaGlobalsStr;
	MidicaButton            btnAllTicks;
	
	// other widgets or elements
	JTabbedPane  tabs;
	MidicaButton btnRestoreDefaults; // use hard-coded default
	MidicaButton btnRestore;         // use config from file
	MidicaButton btnSave;            // copy session config to config file
	
	/**
	 * Creates the window for the decompile configuration.
	 * 
	 * @param dcIcon  the icon to open this window
	 * @param owner   the file selection window
	 */
	public DecompileConfigView(DecompileConfigIcon dcIcon, JDialog owner) {
		super(owner, Dict.get(Dict.TITLE_DC_CONFIG), true);
		icon = dcIcon;
		
		// init widgets
		cbxAddTickComments        = new JCheckBox(Dict.get(Dict.DC_ADD_TICK_COMMENT));
		cbxAddConfig              = new JCheckBox(Dict.get(Dict.DC_ADD_CONFIG));
		cbxAddScore               = new JCheckBox(Dict.get(Dict.DC_ADD_SCORE));
		cbxAddStatistics          = new JCheckBox(Dict.get(Dict.DC_ADD_STATISTICS));
		cbxLengthStrategy         = new JComboBox<>();
		cbxMaxTargetTicksOn       = new JComboBox<>();
		fldNextNoteOnTolerance    = new JTextField();
		fldMinDurToKeep           = new JTextField();
		fldDurationTickTolerance  = new JTextField();
		fldDurationRatioTolerance = new JTextField();
		cbxPredefinedChords       = new JCheckBox();
		fldChordNoteOnTolerance   = new JTextField();
		fldChordNoteOffTolerance  = new JTextField();
		fldChordVelocityTolerance = new JTextField();
		cbxUseDottedNote          = new JCheckBox(Dict.get(Dict.USE_DOTTED_NOTES));
		cbxUseDottedRest          = new JCheckBox(Dict.get(Dict.USE_DOTTED_RESTS));
		cbxUseTriplettedNote      = new JCheckBox(Dict.get(Dict.USE_TRIPLETTED_NOTES));
		cbxUseTriplettedRest      = new JCheckBox(Dict.get(Dict.USE_TRIPLETTED_RESTS));
		cbxOrphanedSyllables      = new JComboBox<>();
		cbxKarOneChannel          = new JCheckBox();
		fldAddGlobalAtTick        = new JTextField();
		btnAddGlobalAtTick        = new MidicaButton(Dict.get(Dict.BTN_ADD_TICK));
		fldAddGlobalsEachTick     = new JTextField();
		fldAddGlobalsStartTick    = new JTextField();
		fldAddGlobalsStopTick     = new JTextField();
		btnAddGlobalTicks         = new MidicaButton(Dict.get(Dict.BTN_ADD_TICKS));
		areaGlobalsStr            = new JTextArea();
		btnAllTicks               = new MidicaButton(Dict.get(Dict.BTN_UPDATE_TICKS));
		btnRestoreDefaults        = new MidicaButton(Dict.get(Dict.DC_RESTORE_DEFAULTS));
		btnRestore                = new MidicaButton(Dict.get(Dict.DC_RESTORE));
		btnSave                   = new MidicaButton(Dict.get(Dict.DC_SAVE));
		cbxLengthStrategy.setModel(DecompileConfigController.getComboboxModel(Config.DC_LENGTH_STRATEGY));
		cbxMaxTargetTicksOn.setModel(DecompileConfigController.getComboboxModel(Config.DC_MAX_TARGET_TICKS_ON));
		cbxOrphanedSyllables.setModel(DecompileConfigController.getComboboxModel(Config.DC_ORPHANED_SYLLABLES));
		
		// setup controller
		controller = new DecompileConfigController(this, icon);
		
		// fill the content
		init();
		addKeyBindings();
		pack();
		addWindowListener(controller);
	}
	
	/**
	 * Opens the window.
	 */
	public void open() {
		setLocationRelativeTo(icon);
		setVisible(true);
	}
	
	/**
	 * Initializes the content of the window.
	 */
	private void init() {
		
		// create top-level container
		JPanel content = new JPanel();
		GridBagConstraints constraints = new GridBagConstraints();
		content.setLayout(new GridBagLayout());
		constraints.fill       = GridBagConstraints.BOTH;
		constraints.insets     = Laf.INSETS_NWE;
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 1;
		constraints.weighty    = 1;
		
		// create and add tabs
		tabs = new JTabbedPane(JTabbedPane.LEFT);
		tabs.add( Dict.get(Dict.DC_TAB_DEBUG),       createDebugArea(Dict.DC_TAB_DEBUG)            );
		tabs.add( Dict.get(Dict.DC_TAB_NOTE_LENGTH), createNoteLengthArea(Dict.DC_TAB_NOTE_LENGTH) );
		tabs.add( Dict.get(Dict.DC_TAB_CHORDS),      createChordArea(Dict.DC_TAB_CHORDS)           );
		tabs.add( Dict.get(Dict.DC_TAB_NOTE_REST),   createNoteRestArea(Dict.DC_TAB_NOTE_REST)        );
		tabs.add( Dict.get(Dict.DC_TAB_KARAOKE),     createKaraokeArea(Dict.DC_TAB_KARAOKE)        );
		tabs.add( Dict.get(Dict.DC_TAB_SLICE),       createSliceArea(Dict.DC_TAB_SLICE)            );
		content.add(tabs, constraints);
		
		// separator
		constraints.gridy++;
		constraints.weighty = 0;
		constraints.insets  = Laf.INSETS_ZERO;
		content.add(Laf.createSeparator(), constraints);
		
		// create and add buttons
		constraints.gridy++;
		constraints.weightx = 0;
		constraints.insets  = Laf.INSETS_SWE;
		Container buttonArea = createButtonArea();
		content.add(buttonArea, constraints);
		
		add(content);
	}
	
	/**
	 * Wraps the given content of a tab inside another container.
	 * This is used to position the tab content correctly inside the tab.
	 * 
	 * @param area    the area to be wrapped
	 * @return the wrapped area.
	 */
	private Container wrapTabContent(Container area) {
		
		// outer container and layout
		JPanel content = new JPanel();
		GridBagConstraints constraints = new GridBagConstraints();
		content.setLayout(new GridBagLayout());
		constraints.fill       = GridBagConstraints.NONE;
		constraints.insets     = Laf.INSETS_ALL;
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 1;
		constraints.weighty    = 1;
		constraints.anchor     = GridBagConstraints.NORTHWEST;
		
		// wrap it
		content.add(area, constraints);
		
		return content;
	}
	
	/**
	 * Creates the area for debug settings.
	 * 
	 * @param tabKey  language key for the tab name
	 * @return the created area
	 */
	private Container createDebugArea(String tabKey) {
		
		// layout
		JPanel area = new JPanel();
		area.setLayout(new GridBagLayout());
		GridBagConstraints[] constaints = createConstraintsForArea();
		GridBagConstraints constrFull   = constaints[0];
		GridBagConstraints constrLeft   = constaints[1];
		
		// tab info box
		area.add(createTabInfo(tabKey, Dict.DC_TABINFO_DEBUG), constrFull);
		
		// tick comments
		cbxAddTickComments.addActionListener(controller);
		area.add(cbxAddTickComments, constrLeft);
		
		// config
		constrLeft.gridy++;
		cbxAddConfig.addActionListener(controller);
		area.add(cbxAddConfig, constrLeft);
		
		// score
		constrLeft.gridy++;
		cbxAddScore.addActionListener(controller);
		area.add(cbxAddScore, constrLeft);
		
		// statistics
		constrLeft.gridy++;
		cbxAddStatistics.addActionListener(controller);
		area.add(cbxAddStatistics, constrLeft);
		
		return wrapTabContent(area);
	}
	
	/**
	 * Creates the area for note length calculation settings.
	 * 
	 * @param tabKey  language key for the tab name
	 * @return the created area
	 */
	private Container createNoteLengthArea(String tabKey) {
		
		// layout
		JPanel area = new JPanel();
		area.setLayout(new GridBagLayout());
		GridBagConstraints[] constaints = createConstraintsForArea();
		GridBagConstraints constrFull   = constaints[0];
		GridBagConstraints constrLeft   = constaints[1];
		GridBagConstraints constrCenter = constaints[2];
		GridBagConstraints constrRight  = constaints[3];
		
		// tab info box
		area.add(createTabInfo(tabKey, Dict.DC_TABINFO_NOTE_LENGTH), constrFull);
		
		// length strategy
		// label
		JLabel lblStrategy = new JLabel( Dict.get(Dict.NOTE_LENGTH_STRATEGY) );
		Laf.makeBold(lblStrategy);
		area.add(lblStrategy, constrLeft);
		
		// combobox
		cbxLengthStrategy.addActionListener(controller);
		constrCenter.gridwidth = 2;
		area.add(cbxLengthStrategy, constrCenter);
		constrCenter.gridwidth = 1;
		
		// separator
		constrLeft.gridy++;
		constrCenter.gridy++;
		constrRight.gridy++;
		constrFull.gridy = constrRight.gridy;
		area.add(Laf.createSeparator(), constrFull);
		
		// max note length (max target ticks) for next note on
		// label
		constrLeft.gridy++;
		JLabel lblMaxTargetOn = new JLabel( Dict.get(Dict.MAX_TARGET_TICKS_NEXT_ON) );
		Laf.makeBold(lblMaxTargetOn);
		area.add(lblMaxTargetOn, constrLeft);
		
		// combobox
		constrCenter.gridy++;
		constrCenter.gridwidth = 2;
		constrRight.gridy++;
		cbxMaxTargetTicksOn.addActionListener(controller);
		area.add(cbxMaxTargetTicksOn, constrCenter);
		constrCenter.gridwidth = 1;
		
		// next note-on tolerance
		// label
		constrLeft.gridy++;
		JLabel lblNextOnTol = new JLabel( Dict.get(Dict.NEXT_NOTE_ON_TOLERANCE) );
		Laf.makeBold(lblNextOnTol);
		area.add(lblNextOnTol, constrLeft);
		
		// field
		constrCenter.gridy++;
		fldNextNoteOnTolerance.getDocument().addDocumentListener(controller);
		fldNextNoteOnTolerance.setPreferredSize(new Dimension(TEXT_FIELD_WIDTH, TEXT_FIELD_HEIGHT));
		area.add(fldNextNoteOnTolerance, constrCenter);
		
		// description
		constrRight.gridy++;
		JLabel descNextOnTol = new JLabel( Dict.get(Dict.NEXT_NOTE_ON_TOLERANCE_D) );
		area.add(descNextOnTol, constrRight);
		
		// separator
		constrLeft.gridy++;
		constrCenter.gridy++;
		constrRight.gridy++;
		constrFull.gridy = constrRight.gridy;
		area.add(Laf.createSeparator(), constrFull);
		
		// duration ratio tolerance
		// label
		constrLeft.gridy++;
		JLabel lblMinDuration = new JLabel( Dict.get(Dict.MIN_DURATION_TO_KEEP) );
		Laf.makeBold(lblMinDuration);
		area.add(lblMinDuration, constrLeft);
		
		// field
		constrCenter.gridy++;
		fldMinDurToKeep.getDocument().addDocumentListener(controller);
		fldMinDurToKeep.setPreferredSize(new Dimension(TEXT_FIELD_WIDTH, TEXT_FIELD_HEIGHT));
		area.add(fldMinDurToKeep, constrCenter);
		
		// description
		constrRight.gridy++;
		JLabel descMinDuration = new JLabel( Dict.get(Dict.MIN_DURATION_TO_KEEP_D) );
		area.add(descMinDuration, constrRight);
		
		// duration tick tolerance
		// label
		constrLeft.gridy++;
		JLabel lblTickTol = new JLabel( Dict.get(Dict.DURATION_TICK_TOLERANCE) );
		Laf.makeBold(lblTickTol);
		area.add(lblTickTol, constrLeft);
		
		// field
		constrCenter.gridy++;
		fldDurationTickTolerance.getDocument().addDocumentListener(controller);
		fldDurationTickTolerance.setPreferredSize(new Dimension(TEXT_FIELD_WIDTH, TEXT_FIELD_HEIGHT));
		area.add(fldDurationTickTolerance, constrCenter);
		
		// description
		constrRight.gridy++;
		JLabel descTickTol = new JLabel( Dict.get(Dict.DURATION_TICK_TOLERANCE_D) );
		area.add(descTickTol, constrRight);
		
		// duration ratio tolerance
		// label
		constrLeft.gridy++;
		JLabel lblDurRatioTol = new JLabel( Dict.get(Dict.DURATION_RATIO_TOLERANCE) );
		Laf.makeBold(lblDurRatioTol);
		area.add(lblDurRatioTol, constrLeft);
		
		// field
		constrCenter.gridy++;
		fldDurationRatioTolerance.getDocument().addDocumentListener(controller);
		fldDurationRatioTolerance.setPreferredSize(new Dimension(TEXT_FIELD_WIDTH, TEXT_FIELD_HEIGHT));
		area.add(fldDurationRatioTolerance, constrCenter);
		
		// description
		constrRight.gridy++;
		JLabel descDurRatioTol = new JLabel( Dict.get(Dict.DURATION_RATIO_TOLERANCE_D) );
		area.add(descDurRatioTol, constrRight);
		
		return wrapTabContent(area);
	}
	
	/**
	 * Creates the area for chord settings.
	 * 
	 * @param tabKey  language key for the tab name
	 * @return the created area
	 */
	private Container createChordArea(String tabKey) {
		
		// layout
		JPanel area = new JPanel();
		area.setLayout(new GridBagLayout());
		GridBagConstraints[] constaints = createConstraintsForArea();
		GridBagConstraints constrFull   = constaints[0];
		GridBagConstraints constrLeft   = constaints[1];
		GridBagConstraints constrCenter = constaints[2];
		GridBagConstraints constrRight  = constaints[3];
		
		// tab info box
		area.add(createTabInfo(tabKey, Dict.DC_TABINFO_CHORDS), constrFull);
		
		// pre-defined chords
		// label
		JLabel lblPredefined = new JLabel( Dict.get(Dict.USE_PRE_DEFINED_CHORDS) );
		Laf.makeBold(lblPredefined);
		area.add(lblPredefined, constrLeft);
		
		// checkbox
		cbxPredefinedChords.setPreferredSize(new Dimension(TEXT_FIELD_WIDTH, TEXT_FIELD_HEIGHT));
		cbxPredefinedChords.addActionListener(controller);
		area.add(cbxPredefinedChords, constrCenter);
		
		// description
		JLabel descTickTol = new JLabel( Dict.get(Dict.USE_PRE_DEFINED_CHORDS_D) );
		area.add(descTickTol, constrRight);
		
		// note-on tolerance
		// label
		constrLeft.gridy++;
		JLabel lblNoteOnTol = new JLabel( Dict.get(Dict.CHORD_NOTE_ON_TOLERANCE) );
		Laf.makeBold(lblNoteOnTol);
		area.add(lblNoteOnTol, constrLeft);
		
		// field
		constrCenter.gridy++;
		fldChordNoteOnTolerance.getDocument().addDocumentListener(controller);
		fldChordNoteOnTolerance.setPreferredSize(new Dimension(TEXT_FIELD_WIDTH, TEXT_FIELD_HEIGHT));
		area.add(fldChordNoteOnTolerance, constrCenter);
		
		// description
		constrRight.gridy++;
		JLabel descOnTol = new JLabel( Dict.get(Dict.CHORD_NOTE_ON_TOLERANCE_D) );
		area.add(descOnTol, constrRight);
		
		// note-off tolerance
		// label
		constrLeft.gridy++;
		JLabel lblNoteOffTol = new JLabel( Dict.get(Dict.CHORD_NOTE_OFF_TOLERANCE) );
		Laf.makeBold(lblNoteOffTol);
		area.add(lblNoteOffTol, constrLeft);
		
		// field
		constrCenter.gridy++;
		fldChordNoteOffTolerance.getDocument().addDocumentListener(controller);
		fldChordNoteOffTolerance.setPreferredSize(new Dimension(TEXT_FIELD_WIDTH, TEXT_FIELD_HEIGHT));
		area.add(fldChordNoteOffTolerance, constrCenter);
		
		// description
		constrRight.gridy++;
		JLabel descOffTol = new JLabel( Dict.get(Dict.CHORD_NOTE_OFF_TOLERANCE_D) );
		area.add(descOffTol, constrRight);
		
		// velocity tolerance
		// label
		constrLeft.gridy++;
		JLabel lblVeloTol = new JLabel( Dict.get(Dict.CHORD_VELOCITY_TOLERANCE) );
		Laf.makeBold(lblVeloTol);
		area.add(lblVeloTol, constrLeft);
		
		// field
		constrCenter.gridy++;
		fldChordVelocityTolerance.setPreferredSize(new Dimension(TEXT_FIELD_WIDTH, TEXT_FIELD_HEIGHT));
		fldChordVelocityTolerance.addActionListener(controller);
		area.add(fldChordVelocityTolerance, constrCenter);
		
		// description
		constrRight.gridy++;
		JLabel descVeloTol = new JLabel( Dict.get(Dict.CHORD_VELOCITY_TOLERANCE_D) );
		area.add(descVeloTol, constrRight);
		
		return wrapTabContent(area);
	}
	
	/**
	 * Creates the area for notes/rests settings.
	 * 
	 * @param tabKey  language key for the tab name
	 * @return the created area
	 */
	private Container createNoteRestArea(String tabKey) {
		
		// layout
		JPanel area = new JPanel();
		area.setLayout(new GridBagLayout());
		GridBagConstraints[] constaints = createConstraintsForArea();
		GridBagConstraints constrFull   = constaints[0];
		GridBagConstraints constrLeft   = constaints[1];
		GridBagConstraints constrCenter = constaints[2];
		GridBagConstraints constrRight  = constaints[3];
		
		// tab info box
		area.add(createTabInfo(tabKey, Dict.DC_TABINFO_NOTE_REST), constrFull);
		
		// dotted notes
		cbxUseDottedNote.addActionListener(controller);
		area.add(cbxUseDottedNote, constrLeft);
		
		// dotted rests
		constrLeft.gridy++;
		cbxUseDottedRest.addActionListener(controller);
		area.add(cbxUseDottedRest, constrLeft);
		
		// tripletted notes
		constrLeft.gridy++;
		cbxUseTriplettedNote.addActionListener(controller);
		area.add(cbxUseTriplettedNote, constrLeft);
		
		// tripletted rests
		constrLeft.gridy++;
		cbxUseTriplettedRest.addActionListener(controller);
		area.add(cbxUseTriplettedRest, constrLeft);
		
		return wrapTabContent(area);
	}
	
	/**
	 * Creates the area for karaoke settings.
	 * 
	 * @param tabKey  language key for the tab name
	 * @return the created area
	 */
	private Container createKaraokeArea(String tabKey) {
		
		// layout
		JPanel area = new JPanel();
		area.setLayout(new GridBagLayout());
		GridBagConstraints[] constaints = createConstraintsForArea();
		GridBagConstraints constrFull   = constaints[0];
		GridBagConstraints constrLeft   = constaints[1];
		GridBagConstraints constrCenter = constaints[2];
		GridBagConstraints constrRight  = constaints[3];
		
		// tab info box
		area.add(createTabInfo(tabKey, Dict.DC_TABINFO_KARAOKE), constrFull);
		
		// orphaned syllables
		// label
		JLabel lblOrpSyl = new JLabel( Dict.get(Dict.ORPHANED_SYLLABLES) );
		Laf.makeBold(lblOrpSyl);
		area.add(lblOrpSyl, constrLeft);
		
		// combobox
		cbxOrphanedSyllables.setPreferredSize(new Dimension(TEXT_FIELD_WIDTH, TEXT_FIELD_HEIGHT));
		cbxOrphanedSyllables.addActionListener(controller);
		area.add(cbxOrphanedSyllables, constrCenter);
		
		// description
		JLabel descOrpSyl = new JLabel( Dict.get(Dict.ORPHANED_SYLLABLES_D) );
		area.add(descOrpSyl, constrRight);
		
		// one karaoke channel
		// label
		constrLeft.gridy++;
		JLabel lblOneKarCh = new JLabel( Dict.get(Dict.KAR_ONE_CHANNEL) );
		Laf.makeBold(lblOneKarCh);
		area.add(lblOneKarCh, constrLeft);
		
		// checkbox
		constrCenter.gridy++;
		cbxKarOneChannel.setPreferredSize(new Dimension(TEXT_FIELD_WIDTH, TEXT_FIELD_HEIGHT));
		cbxKarOneChannel.addActionListener(controller);
		area.add(cbxKarOneChannel, constrCenter);
		
		// description
		constrRight.gridy++;
		JLabel descNextOnTol = new JLabel( Dict.get(Dict.KAR_ONE_CHANNEL_D) );
		area.add(descNextOnTol, constrRight);
		
		return wrapTabContent(area);
	}
	
	/**
	 * Creates the area for adding additional global commands.
	 * 
	 * @param tabKey  language key for the tab name
	 * @return the created area
	 */
	private Container createSliceArea(String tabKey) {
		
		// layout
		JPanel area = new JPanel();
		area.setLayout(new GridBagLayout());
		GridBagConstraints[] constaints = createConstraintsForArea();
		GridBagConstraints constrFull   = constaints[0];
		GridBagConstraints constrLeft   = constaints[1];
		GridBagConstraints constrCenter = constaints[2];
		GridBagConstraints constrRight  = constaints[3];
		constrRight.fill = GridBagConstraints.NONE;
		
		// tab info box
		area.add(createTabInfo(tabKey, Dict.DC_TABINFO_SLICE), constrFull);
		
		// add global at a single tick
		// label
		JLabel lblTickGlob = new JLabel( Dict.get(Dict.ADD_GLOBAL_AT_TICK) );
		Laf.makeBold(lblTickGlob);
		area.add(lblTickGlob, constrLeft);
		
		// field
		fldAddGlobalAtTick.getDocument().putProperty(DOC_ID, DOC_ID_ADD_GLOBAL_AT_TICK);
		fldAddGlobalAtTick.getDocument().addDocumentListener(controller);
		fldAddGlobalAtTick.addActionListener(controller);
		fldAddGlobalAtTick.addFocusListener(controller);
		fldAddGlobalAtTick.setPreferredSize(new Dimension(TEXT_FIELD_WIDTH, TEXT_FIELD_HEIGHT));
		area.add(fldAddGlobalAtTick, constrCenter);
		
		// button
		btnAddGlobalAtTick.addActionListener(controller);
		area.add(btnAddGlobalAtTick, constrRight);
		
		// separator
		constrLeft.gridy++;
		constrCenter.gridy++;
		constrRight.gridy++;
		constrFull.gridy = constrLeft.gridy;
		area.add(Laf.createSeparator(), constrFull);
		
		// add many global commands starting in an area (from, each, to)
		// each label
		constrLeft.gridy++;
		JLabel lblGlobEach = new JLabel( Dict.get(Dict.ADD_GLOBAL_EACH) );
		Laf.makeBold(lblGlobEach);
		area.add(lblGlobEach, constrLeft);
		
		// each field
		constrCenter.gridy++;
		fldAddGlobalsEachTick.getDocument().putProperty(DOC_ID, DOC_ID_ADD_GLOBAL_EACH);
		fldAddGlobalsEachTick.getDocument().addDocumentListener(controller);
		fldAddGlobalsEachTick.addActionListener(controller);
		fldAddGlobalsEachTick.addFocusListener(controller);
		fldAddGlobalsEachTick.setPreferredSize(new Dimension(TEXT_FIELD_WIDTH, TEXT_FIELD_HEIGHT));
		area.add(fldAddGlobalsEachTick, constrCenter);
		
		// from label
		constrLeft.gridy++;
		JLabel lblGlobFrom = new JLabel( Dict.get(Dict.ADD_GLOBAL_FROM) );
		Laf.makeBold(lblGlobFrom);
		area.add(lblGlobFrom, constrLeft);
		
		// from field
		constrCenter.gridy++;
		fldAddGlobalsStartTick.getDocument().putProperty(DOC_ID, DOC_ID_ADD_GLOBAL_START);
		fldAddGlobalsStartTick.getDocument().addDocumentListener(controller);
		fldAddGlobalsStartTick.addActionListener(controller);
		fldAddGlobalsStartTick.addFocusListener(controller);
		fldAddGlobalsStartTick.setPreferredSize(new Dimension(TEXT_FIELD_WIDTH, TEXT_FIELD_HEIGHT));
		area.add(fldAddGlobalsStartTick, constrCenter);
		
		// to label
		constrLeft.gridy++;
		JLabel lblGlobTo = new JLabel( Dict.get(Dict.ADD_GLOBAL_TO) );
		Laf.makeBold(lblGlobTo);
		area.add(lblGlobTo, constrLeft);
		
		// to field
		constrCenter.gridy++;
		fldAddGlobalsStopTick.getDocument().putProperty(DOC_ID, DOC_ID_ADD_GLOBAL_STOP);
		fldAddGlobalsStopTick.getDocument().addDocumentListener(controller);
		fldAddGlobalsStopTick.addActionListener(controller);
		fldAddGlobalsStopTick.addFocusListener(controller);
		fldAddGlobalsStopTick.setPreferredSize(new Dimension(TEXT_FIELD_WIDTH, TEXT_FIELD_HEIGHT));
		area.add(fldAddGlobalsStopTick, constrCenter);
		
		// from/each/to button
		constrRight.gridy = constrLeft.gridy;
		btnAddGlobalTicks.addActionListener(controller);
		area.add(btnAddGlobalTicks, constrRight);
		
		// separator
		constrLeft.gridy++;
		constrCenter.gridy++;
		constrRight.gridy++;
		constrFull.gridy = constrLeft.gridy;
		area.add(Laf.createSeparator(), constrFull);
		
		// label all ticks
		constrLeft.gridy++;
		JLabel lblAllTicks = new JLabel( Dict.get(Dict.DC_ALL_TICKS) );
		Laf.makeBold(lblAllTicks);
		area.add(lblAllTicks, constrLeft);
		
		// all ticks text area
		constrCenter.gridy++;
		areaGlobalsStr.getDocument().putProperty(DOC_ID, DOC_ID_UPDATE_GLOBAL_ALL);
		areaGlobalsStr.getDocument().addDocumentListener(controller);
		areaGlobalsStr.addFocusListener(controller);
		areaGlobalsStr.setLineWrap(true);
		JScrollPane scroll = new JScrollPane(areaGlobalsStr);
		scroll.setPreferredSize(new Dimension(TEXT_FIELD_WIDTH, TEXT_AREA_HEIGHT));
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		area.add(scroll, constrCenter);
		
		// all ticks button
		constrRight.gridy = constrLeft.gridy;
		btnAllTicks.addActionListener(controller);
		area.add(btnAllTicks, constrRight);
		
		return wrapTabContent(area);
	}
	
	/**
	 * Creates the area for buttons.
	 * 
	 * @return the created area
	 */
	private Container createButtonArea() {
		JPanel area = new JPanel();
		
		// layout
		area.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.NONE;
		constraints.insets     = Laf.INSETS_IN;
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 0;
		constraints.weighty    = 0;
		
		// restore button
		btnRestore.addActionListener(controller);
		area.add(btnRestore, constraints);
		
		// restore defaults button
		constraints.gridx++;
		btnRestoreDefaults.addActionListener(controller);
		area.add(btnRestoreDefaults, constraints);
		
		// save button
		constraints.gridx++;
		btnSave.addActionListener(controller);
		area.add(btnSave, constraints);
		
		return area;
	}
	
	/**
	 * Creates {@link GridBagConstraints} that can be used for the sub areas of the config file.
	 * 
	 * Returns the following elements:
	 * 
	 * - left column constraints
	 * - center column constraints
	 * - right column constraints
	 * - full width constraints (for elements using all 3 columns)
	 * 
	 * @return the created constraints like described above.
	 */
	private GridBagConstraints[] createConstraintsForArea() {
		
		GridBagConstraints constrLeft = new GridBagConstraints();
		constrLeft.fill       = GridBagConstraints.NONE;
		constrLeft.anchor     = GridBagConstraints.WEST;
		constrLeft.insets     = Laf.INSETS_W;
		constrLeft.gridx      = 0;
		constrLeft.gridy      = 1;
		constrLeft.gridheight = 1;
		constrLeft.gridwidth  = 1;
		constrLeft.weightx    = 0;
		constrLeft.weighty    = 0;
		GridBagConstraints constrCenter = (GridBagConstraints) constrLeft.clone();
		constrCenter.gridx = 1;
		constrLeft.insets  = Laf.INSETS_IN;
		GridBagConstraints constrRight = (GridBagConstraints) constrLeft.clone();
		constrRight.gridx   = 2;
		constrLeft.insets   = Laf.INSETS_E;
		constrRight.weightx = 1.0;
		constrRight.fill    = GridBagConstraints.HORIZONTAL;
		GridBagConstraints constrFull = (GridBagConstraints) constrCenter.clone();
		constrFull.gridx     = 0;
		constrFull.gridy     = 0;
		constrFull.gridwidth = 3;
		constrFull.weightx   = 1.0;
		constrFull.insets    = Laf.INSETS_ZERO;
		constrFull.fill      = GridBagConstraints.HORIZONTAL;
		constrFull.anchor    = GridBagConstraints.CENTER;
		
		return new GridBagConstraints[] {
			constrFull,
			constrLeft,
			constrCenter,
			constrRight,
		};
	}
	
	/**
	 * Creates the info area for a tab.
	 * 
	 * @param tabKey     language key for the titled border (same as the tab name)
	 * @param infoKey    language key for the info text
	 * @return the component containing the tab info
	 */
	private JComponent createTabInfo(String tabKey, String infoKey) {
		JPanel area = new JPanel();
		
		// border
		area.setBorder( Laf.createTitledBorder(Dict.get(tabKey)) );
		
		// layout
		area.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.BOTH;
		constraints.insets     = Laf.INSETS_IN;
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 1;
		constraints.weighty    = 1;
		constraints.anchor     = GridBagConstraints.NORTHWEST;
		
		// info text
		JLabel lbl = new JLabel(Dict.get(infoKey));
		area.add(lbl, constraints);
		
		return area;
	}
	
	/**
	 * Adds key bindings to the info window.
	 */
	private void addKeyBindings() {
		
		// reset everything
		KeyBindingManager keyBindingManager = new KeyBindingManager(this, this.getRootPane());
		
		// close bindings
		keyBindingManager.addBindingsForClose( Dict.KEY_DC_CONFIG_CLOSE );
		
		// tab bindings
		keyBindingManager.addBindingsForTabLevel1( tabs, Dict.KEY_DC_TAB_DEBUG,        0 );
		keyBindingManager.addBindingsForTabLevel1( tabs, Dict.KEY_DC_TAB_NOTE_LENGTH,  1 );
		keyBindingManager.addBindingsForTabLevel1( tabs, Dict.KEY_DC_TAB_CHORDS,       2 );
		keyBindingManager.addBindingsForTabLevel1( tabs, Dict.KEY_DC_TAB_NOTE_REST,    3 );
		keyBindingManager.addBindingsForTabLevel1( tabs, Dict.KEY_DC_TAB_KARAOKE,      4 );
		keyBindingManager.addBindingsForTabLevel1( tabs, Dict.KEY_DC_TAB_SLICES,       5 );
		
		// debug tab
		keyBindingManager.addBindingsForTabLevel3( cbxAddTickComments, Dict.KEY_DC_ADD_TICK_COMMENTS );
		keyBindingManager.addBindingsForTabLevel3( cbxAddConfig,       Dict.KEY_DC_ADD_CONFIG        );
		keyBindingManager.addBindingsForTabLevel3( cbxAddScore,        Dict.KEY_DC_ADD_SCORE         );
		keyBindingManager.addBindingsForTabLevel3( cbxAddStatistics,   Dict.KEY_DC_ADD_STATISTICS    );
		
		// note length tab
		keyBindingManager.addBindingsForTabLevel3( cbxLengthStrategy,         Dict.KEY_DC_NOTE_LENGTH_STRATEGY );
		keyBindingManager.addBindingsForTabLevel3( cbxMaxTargetTicksOn,       Dict.KEY_DC_MAX_TARGET_TICKS_ON  );
		keyBindingManager.addBindingsForTabLevel3( fldNextNoteOnTolerance,    Dict.KEY_DC_TOL_NEXT_ON          );
		keyBindingManager.addBindingsForTabLevel3( fldMinDurToKeep,           Dict.KEY_DC_MIN_DUR_TO_KEEP      );
		keyBindingManager.addBindingsForTabLevel3( fldDurationTickTolerance,  Dict.KEY_DC_TOL_DUR_TICK         );
		keyBindingManager.addBindingsForTabLevel3( fldDurationRatioTolerance, Dict.KEY_DC_TOL_DUR_RATIO        );
		
		// chords tab
		keyBindingManager.addBindingsForTabLevel3( cbxPredefinedChords,       Dict.KEY_DC_CRD_PREDEFINED );
		keyBindingManager.addBindingsForTabLevel3( fldChordNoteOnTolerance,   Dict.KEY_DC_CRD_NOTE_ON    );
		keyBindingManager.addBindingsForTabLevel3( fldChordNoteOffTolerance,  Dict.KEY_DC_CRD_NOTE_OFF   );
		keyBindingManager.addBindingsForTabLevel3( fldChordVelocityTolerance, Dict.KEY_DC_CRD_VELOCITY   );
		
		// notes/rests tab
		keyBindingManager.addBindingsForTabLevel3( cbxUseDottedNote,     Dict.KEY_DC_USE_DOT_NOTES  );
		keyBindingManager.addBindingsForTabLevel3( cbxUseDottedRest,     Dict.KEY_DC_USE_DOT_RESTS  );
		keyBindingManager.addBindingsForTabLevel3( cbxUseTriplettedNote, Dict.KEY_DC_USE_TRIP_NOTES );
		keyBindingManager.addBindingsForTabLevel3( cbxUseTriplettedRest, Dict.KEY_DC_USE_TRIP_RESTS );
		
		// karaoke tab
		keyBindingManager.addBindingsForTabLevel3( cbxOrphanedSyllables, Dict.KEY_DC_KAR_ORPHANED );
		keyBindingManager.addBindingsForTabLevel3( cbxKarOneChannel,     Dict.KEY_DC_KAR_ONE_CH   );
		
		// slices
		keyBindingManager.addBindingsForTabLevel3( fldAddGlobalAtTick,     Dict.KEY_DC_FLD_GLOB_SINGLE );
		keyBindingManager.addBindingsForTabLevel3( btnAddGlobalAtTick,     Dict.KEY_DC_BTN_GLOB_SINGLE );
		keyBindingManager.addBindingsForTabLevel3( fldAddGlobalsEachTick,  Dict.KEY_DC_FLD_GLOB_EACH   );
		keyBindingManager.addBindingsForTabLevel3( fldAddGlobalsStartTick, Dict.KEY_DC_FLD_GLOB_FROM   );
		keyBindingManager.addBindingsForTabLevel3( fldAddGlobalsStopTick,  Dict.KEY_DC_FLD_GLOB_TO     );
		keyBindingManager.addBindingsForTabLevel3( btnAddGlobalTicks,      Dict.KEY_DC_BTN_GLOB_RANGE  );
		keyBindingManager.addBindingsForTabLevel3( areaGlobalsStr,         Dict.KEY_DC_AREA_GLOB_ALL   );
		keyBindingManager.addBindingsForTabLevel3( btnAllTicks,            Dict.KEY_DC_BTN_GLOB_ALL    );
		
		// restore/save buttons
		keyBindingManager.addBindingsForButton( btnSave,            Dict.KEY_DC_SAVE            );
		keyBindingManager.addBindingsForButton( btnRestore,         Dict.KEY_DC_RESTORE_SAVED   );
		keyBindingManager.addBindingsForButton( btnRestoreDefaults, Dict.KEY_DC_RESTORE_DEFAULT );
		
		// set input and action maps
		keyBindingManager.postprocess();
	}
}
