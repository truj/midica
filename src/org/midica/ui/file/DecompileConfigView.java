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
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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
	private static final int TEXT_AREA_HEIGHT  =  80;
	
	private DecompileConfigController controller;
	private DecompileConfigIcon       icon;
	
	// widgets that change dc config values
	JTextField              fldDurationTickTolerance;
	JTextField              fldDurationRatioTolerance;
	JTextField              fldMinDurToKeep;
	JTextField              fldNextNoteOnTolerance;
	JTextField              fldMaxTargetTicksOn;
	JTextField              fldChordNoteOnTolerance;
	JTextField              fldChordNoteOffTolerance;
	JTextField              fldChordVelocityTolerance;
	JComboBox<NamedInteger> cbxLengthStrategy;
	JComboBox<NamedInteger> cbxOrphanedSyllables;
	JCheckBox               cbxAddTickComments;
	JCheckBox               cbxAddScore;
	JCheckBox               cbxAddStatistics;
	JCheckBox               cbxAddConfig;
	JCheckBox               cbxKarOneChannel;
	JCheckBox               cbxPredefinedChords;
	JTextField              fldAddGlobalAtTick;
	JTextField              fldAddGlobalsStartTick;
	JTextField              fldAddGlobalsEachTick;
	JTextField              fldAddGlobalsStopTick;
	JTextArea               areaGlobalsStr;
	MidicaButton            btnAddGlobalAtTick;
	MidicaButton            btnAddGlobalTicks;
	MidicaButton            btnAllTicks;
	MidicaButton            btnRestoreDefaults; // use hard-coded default
	MidicaButton            btnRestore;         // use config from file
	MidicaButton            btnSave;            // copy session config to config file
	
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
		fldDurationTickTolerance  = new JTextField();
		fldDurationRatioTolerance = new JTextField();
		fldMinDurToKeep           = new JTextField();
		fldNextNoteOnTolerance    = new JTextField();
		fldMaxTargetTicksOn       = new JTextField();
		fldChordNoteOnTolerance   = new JTextField();
		fldChordNoteOffTolerance  = new JTextField();
		fldChordVelocityTolerance = new JTextField();
		cbxLengthStrategy         = new JComboBox<>();
		cbxOrphanedSyllables      = new JComboBox<>();
		cbxAddTickComments        = new JCheckBox( Dict.get(Dict.DC_ADD_TICK_COMMENT) );
		cbxAddScore               = new JCheckBox( Dict.get(Dict.DC_ADD_SCORE) );
		cbxAddStatistics          = new JCheckBox( Dict.get(Dict.DC_ADD_STATISTICS) );
		cbxAddConfig              = new JCheckBox( Dict.get(Dict.DC_ADD_CONFIG) );
		cbxKarOneChannel          = new JCheckBox();
		cbxPredefinedChords       = new JCheckBox();
		fldAddGlobalAtTick        = new JTextField();
		fldAddGlobalsStartTick    = new JTextField();
		fldAddGlobalsEachTick     = new JTextField();
		fldAddGlobalsStopTick     = new JTextField();
		areaGlobalsStr            = new JTextArea();
		btnAddGlobalAtTick        = new MidicaButton( Dict.get(Dict.BTN_ADD_TICK) );
		btnAddGlobalTicks         = new MidicaButton( Dict.get(Dict.BTN_ADD_TICKS) );
		btnAllTicks               = new MidicaButton( Dict.get(Dict.BTN_UPDATE_TICKS) );
		btnRestoreDefaults        = new MidicaButton( Dict.get(Dict.DC_RESTORE_DEFAULTS) );
		btnRestore                = new MidicaButton( Dict.get(Dict.DC_RESTORE) );
		btnSave                   = new MidicaButton( Dict.get(Dict.DC_SAVE) );
		cbxLengthStrategy.setModel( DecompileConfigController.getComboboxModel(Config.DC_LENGTH_STRATEGY) );
		cbxOrphanedSyllables.setModel( DecompileConfigController.getComboboxModel(Config.DC_ORPHANED_SYLLABLES) );
		
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
		Container content = getContentPane();
		
		// layout
		content.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.HORIZONTAL;
		constraints.insets     = Laf.INSETS_NWE;
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 0;
		constraints.weighty    = 0;
		
		// debug
		content.add(createDebugArea(), constraints);
		
		// note length calculation
		constraints.insets = Laf.INSETS_WE;
		constraints.gridy++;
		content.add(createNoteLengthArea(), constraints);
		
		// chords
		constraints.insets = Laf.INSETS_WE;
		constraints.gridy++;
		content.add(createChordArea(), constraints);
		
		// karaoke
		constraints.insets = Laf.INSETS_WE;
		constraints.gridy++;
		content.add(createKaraokeArea(), constraints);
		
		// extra slices
		constraints.gridy++;
		content.add(createSliceArea(), constraints);
		
		// buttons
		constraints.insets = Laf.INSETS_SWE;
		constraints.gridy++;
		content.add(createButtonArea(), constraints);
	}
	
	/**
	 * Creates the area for debug settings.
	 * 
	 * @return the created area
	 */
	private Container createDebugArea() {
		JPanel area = new JPanel();
		area.setBorder( Laf.createTitledBorder(Dict.get(Dict.DC_CAT_DEBUG)) );
		
		// layout
		area.setLayout(new GridBagLayout());
		GridBagConstraints[] constaints = createConstraintsForArea();
		GridBagConstraints constrLeft   = constaints[0];
		GridBagConstraints constrCenter = constaints[1];
		GridBagConstraints constrRight  = constaints[2];
		
		// tick comments
		cbxAddTickComments.addActionListener(controller);
		area.add(cbxAddTickComments, constrLeft);
		
		// config
		cbxAddConfig.addActionListener(controller);
		area.add(cbxAddConfig, constrCenter);
		
		// score
		cbxAddScore.addActionListener(controller);
		area.add(cbxAddScore, constrRight);
		
		// statistics
		constrLeft.gridy++;
		cbxAddStatistics.addActionListener(controller);
		area.add(cbxAddStatistics, constrLeft);
		
		return area;
	}
	
	/**
	 * Creates the area for note length calculation settings.
	 * 
	 * @return the created area
	 */
	private Container createNoteLengthArea() {
		JPanel area = new JPanel();
		area.setBorder( Laf.createTitledBorder(Dict.get(Dict.DC_CAT_NOTE_LENGTH)) );
		
		// layout
		area.setLayout(new GridBagLayout());
		GridBagConstraints[] constaints = createConstraintsForArea();
		GridBagConstraints constrLeft   = constaints[0];
		GridBagConstraints constrCenter = constaints[1];
		GridBagConstraints constrRight  = constaints[2];
		
		// length strategy
		// label
		JLabel lblStrategy = new JLabel( Dict.get(Dict.NOTE_LENGTH_STRATEGY) );
		Laf.makeBold(lblStrategy);
		area.add(lblStrategy, constrLeft);
		
		// combobox
		cbxLengthStrategy.setPreferredSize(new Dimension(TEXT_FIELD_WIDTH, TEXT_FIELD_HEIGHT));
		cbxLengthStrategy.addActionListener(controller);
		area.add(cbxLengthStrategy, constrCenter);
		
		// description
		JLabel descStrategy = new JLabel( Dict.get(Dict.NOTE_LENGTH_STRATEGY_D) );
		area.add(descStrategy, constrRight);
		
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
		
		// max target ticks for next note on
		// label
		constrLeft.gridy++;
		JLabel lblMaxTargetOn = new JLabel( Dict.get(Dict.MAX_TARGET_TICKS_NEXT_ON) );
		Laf.makeBold(lblMaxTargetOn);
		area.add(lblMaxTargetOn, constrLeft);
		
		// field
		constrCenter.gridy++;
		fldMaxTargetTicksOn.getDocument().addDocumentListener(controller);
		fldMaxTargetTicksOn.setPreferredSize(new Dimension(TEXT_FIELD_WIDTH, TEXT_FIELD_HEIGHT));
		area.add(fldMaxTargetTicksOn, constrCenter);
		
		// description
		constrRight.gridy++;
		JLabel descMaxTargetOn = new JLabel( Dict.get(Dict.MAX_TARGET_TICKS_NEXT_ON_D) );
		area.add(descMaxTargetOn, constrRight);
		
		return area;
	}
	
	/**
	 * Creates the area for chord settings.
	 * 
	 * @return the created area
	 */
	private Container createChordArea() {
		JPanel area = new JPanel();
		area.setBorder( Laf.createTitledBorder(Dict.get(Dict.DC_CAT_CHORDS)) );
		
		// layout
		area.setLayout(new GridBagLayout());
		GridBagConstraints[] constaints = createConstraintsForArea();
		GridBagConstraints constrLeft   = constaints[0];
		GridBagConstraints constrCenter = constaints[1];
		GridBagConstraints constrRight  = constaints[2];
		
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
		
		return area;
	}
	
	/**
	 * Creates the area for karaoke settings.
	 * 
	 * @return the created area
	 */
	private Container createKaraokeArea() {
		JPanel area = new JPanel();
		area.setBorder( Laf.createTitledBorder(Dict.get(Dict.DC_CAT_KARAOKE)) );
		
		// layout
		area.setLayout(new GridBagLayout());
		GridBagConstraints[] constaints = createConstraintsForArea();
		GridBagConstraints constrLeft   = constaints[0];
		GridBagConstraints constrCenter = constaints[1];
		GridBagConstraints constrRight  = constaints[2];
		
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
		
		return area;
	}
	
	/**
	 * Creates the area for adding additional global commands.
	 * 
	 * @return the created area
	 */
	private Container createSliceArea() {
		JPanel area = new JPanel();
		area.setBorder( Laf.createTitledBorder(Dict.get(Dict.DC_CAT_SLICE)) );
		
		// layout
		area.setLayout(new GridBagLayout());
		GridBagConstraints[] constaints = createConstraintsForArea();
		GridBagConstraints constrLeft   = constaints[0];
		GridBagConstraints constrCenter = constaints[1];
		GridBagConstraints constrRight  = constaints[2];
		GridBagConstraints constrFull   = constaints[3];
		constrRight.fill = GridBagConstraints.NONE;
		
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
		
		return area;
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
		constrLeft.gridy      = 0;
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
		constrFull.gridwidth = 3;
		constrFull.weightx   = 1.0;
		constrFull.insets    = Laf.INSETS_WE;
		constrFull.fill      = GridBagConstraints.HORIZONTAL;
		constrFull.anchor    = GridBagConstraints.CENTER;
		
		return new GridBagConstraints[] {
			constrLeft,
			constrCenter,
			constrRight,
			constrFull,
		};
	}
	
	/**
	 * Adds key bindings to the info window.
	 */
	private void addKeyBindings() {
		
		// reset everything
		KeyBindingManager keyBindingManager = new KeyBindingManager(this, this.getRootPane());
		
		// close bindings
		keyBindingManager.addBindingsForClose( Dict.KEY_DC_CONFIG_CLOSE );
		
		// text fields
		keyBindingManager.addBindingsForFocus( fldDurationTickTolerance,  Dict.KEY_DC_TOL_DUR_TICK        );
		keyBindingManager.addBindingsForFocus( fldDurationRatioTolerance, Dict.KEY_DC_TOL_DUR_RATIO       );
		keyBindingManager.addBindingsForFocus( fldMinDurToKeep,           Dict.KEY_DC_MIN_DUR_TO_KEEP     );
		keyBindingManager.addBindingsForFocus( fldNextNoteOnTolerance,    Dict.KEY_DC_TOL_NEXT_ON         );
		keyBindingManager.addBindingsForFocus( fldMaxTargetTicksOn,       Dict.KEY_DC_MAX_TARGET_TICKS_ON );
		keyBindingManager.addBindingsForFocus( fldChordNoteOnTolerance,   Dict.KEY_DC_CRD_NOTE_ON         );
		keyBindingManager.addBindingsForFocus( fldChordNoteOffTolerance,  Dict.KEY_DC_CRD_NOTE_OFF        );
		keyBindingManager.addBindingsForFocus( fldChordVelocityTolerance, Dict.KEY_DC_CRD_VELOCITY        );
		keyBindingManager.addBindingsForFocus( fldAddGlobalAtTick,        Dict.KEY_DC_FLD_GLOB_SINGLE     );
		keyBindingManager.addBindingsForFocus( fldAddGlobalsEachTick,     Dict.KEY_DC_FLD_GLOB_EACH       );
		keyBindingManager.addBindingsForFocus( fldAddGlobalsStartTick,    Dict.KEY_DC_FLD_GLOB_FROM       );
		keyBindingManager.addBindingsForFocus( fldAddGlobalsStopTick,     Dict.KEY_DC_FLD_GLOB_TO         );
		
		// text area
		keyBindingManager.addBindingsForFocus( areaGlobalsStr, Dict.KEY_DC_AREA_GLOB_ALL );
		
		// combobox
		keyBindingManager.addBindingsForComboboxOpen( cbxLengthStrategy,    Dict.KEY_DC_NOTE_LENGTH_STRATEGY );
		keyBindingManager.addBindingsForComboboxOpen( cbxOrphanedSyllables, Dict.KEY_DC_KAR_ORPHANED         );
		
		// checkbox
		keyBindingManager.addBindingsForCheckbox( cbxAddTickComments,  Dict.KEY_DC_ADD_TICK_COMMENTS );
		keyBindingManager.addBindingsForCheckbox( cbxAddConfig,        Dict.KEY_DC_ADD_CONFIG        );
		keyBindingManager.addBindingsForCheckbox( cbxAddScore,         Dict.KEY_DC_ADD_SCORE         );
		keyBindingManager.addBindingsForCheckbox( cbxAddStatistics,    Dict.KEY_DC_ADD_STATISTICS    );
		keyBindingManager.addBindingsForCheckbox( cbxKarOneChannel,    Dict.KEY_DC_KAR_ONE_CH        );
		keyBindingManager.addBindingsForCheckbox( cbxPredefinedChords, Dict.KEY_DC_CRD_PREDEFINED    );
		
		// buttons
		keyBindingManager.addBindingsForButton( btnAddGlobalAtTick, Dict.KEY_DC_BTN_GLOB_SINGLE );
		keyBindingManager.addBindingsForButton( btnAddGlobalTicks,  Dict.KEY_DC_BTN_GLOB_RANGE  );
		keyBindingManager.addBindingsForButton( btnAllTicks,        Dict.KEY_DC_BTN_GLOB_ALL    );
		keyBindingManager.addBindingsForButton( btnSave,            Dict.KEY_DC_SAVE            );
		keyBindingManager.addBindingsForButton( btnRestore,         Dict.KEY_DC_RESTORE_SAVED   );
		keyBindingManager.addBindingsForButton( btnRestoreDefaults, Dict.KEY_DC_RESTORE_DEFAULT );
		
		// set input and action maps
		keyBindingManager.postprocess();
	}
}
