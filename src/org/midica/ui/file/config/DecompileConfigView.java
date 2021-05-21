/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.file.config;

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
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.midica.config.Config;
import org.midica.config.Dict;
import org.midica.config.Laf;
import org.midica.config.NamedInteger;
import org.midica.ui.widget.ConfigIcon;
import org.midica.ui.widget.MidicaButton;

/**
 * This class provides the configuration window for decompile options.
 * 
 * @author Jan Trukenmüller
 */
public class DecompileConfigView extends FileConfigView {
	
	private static final long serialVersionUID = 1L;
	
	// identifier property name for text documents
	static final String DOC_ID = "doc_id";
	
	// identifier property values for text documents
	static final Integer DOC_ID_ADD_GLOBAL_AT_TICK = 1;
	static final Integer DOC_ID_ADD_GLOBAL_EACH    = 2;
	static final Integer DOC_ID_ADD_GLOBAL_START   = 3;
	static final Integer DOC_ID_ADD_GLOBAL_STOP    = 4;
	static final Integer DOC_ID_UPDATE_GLOBAL_ALL  = 5;
	
	// sizes
	private static final int TEXT_FIELD_WIDTH  = 150;
	private static final int TEXT_FIELD_HEIGHT =  30;
	private static final int TEXT_AREA_HEIGHT  = 150;
	
	// widgets that change dc config values
	JCheckBox               cbxAddTickComments;
	JCheckBox               cbxAddConfig;
	JCheckBox               cbxAddScore;
	JCheckBox               cbxAddStatistics;
	JCheckBox               cbxAddStrategyStat;
	JComboBox<NamedInteger> cbxLengthStrategy;
	JComboBox<NamedInteger> cbxMinTargetTicksOn;
	JComboBox<NamedInteger> cbxMaxTargetTicksOn;
	JTextField              fldMinDurToKeep;
	JTextField              fldMaxDurToKeep;
	JTextField              fldLengthTickTolerance;
	JTextField              fldDurationRatioTolerance;
	JCheckBox               cbxPredefinedChords;
	JTextField              fldChordNoteOnTolerance;
	JTextField              fldChordNoteOffTolerance;
	JTextField              fldChordVelocityTolerance;
	JCheckBox               cbxUseDottedNote;
	JCheckBox               cbxUseDottedRest;
	JCheckBox               cbxUseTriplettedNote;
	JCheckBox               cbxUseTriplettedRest;
	JCheckBox               cbxUseKaraoke;
	JCheckBox               cbxAllSyllablesOrphaned;
	JComboBox<NamedInteger> cbxOrphanedSyllables;
	JCheckBox               cbxKarOneChannel;
	JComboBox<NamedInteger> cbxCtrlChangeMode;
	JTextField              fldAddGlobalAtTick;
	MidicaButton            btnAddGlobalAtTick;
	JTextField              fldAddGlobalsEachTick;
	JTextField              fldAddGlobalsStartTick;
	JTextField              fldAddGlobalsStopTick;
	MidicaButton            btnAddGlobalTicks;
	JTextArea               areaGlobalsStr;
	MidicaButton            btnAllTicks;
	
	// other widgets or elements
	JTabbedPane tabs;
	
	/**
	 * Creates the window for the decompile configuration.
	 * 
	 * @param owner  the file selection window
	 * @param icon   the icon to open this window
	 */
	public DecompileConfigView(JDialog owner, ConfigIcon icon) {
		super(owner, icon, Dict.get(Dict.TITLE_DC_CONFIG));
	}
	
	/**
	 * Creates a default window that is not intended to be used as a real window.
	 * 
	 * See {@link FileConfigView#FileConfigView()}.
	 */
	public DecompileConfigView() {
		super();
	}
	
	@Override
	protected FileConfigController initStructures() {
		
		// init widgets
		cbxAddTickComments        = new JCheckBox(Dict.get(Dict.DC_ADD_TICK_COMMENT));
		cbxAddConfig              = new JCheckBox(Dict.get(Dict.DC_ADD_CONFIG));
		cbxAddScore               = new JCheckBox(Dict.get(Dict.DC_ADD_SCORE));
		cbxAddStatistics          = new JCheckBox(Dict.get(Dict.DC_ADD_STATISTICS));
		cbxAddStrategyStat        = new JCheckBox(Dict.get(Dict.DC_ADD_STRATEGY_STAT));
		cbxLengthStrategy         = new JComboBox<>();
		cbxMinTargetTicksOn       = new JComboBox<>();
		cbxMaxTargetTicksOn       = new JComboBox<>();
		fldMinDurToKeep           = new JTextField();
		fldMaxDurToKeep           = new JTextField();
		fldLengthTickTolerance    = new JTextField();
		fldDurationRatioTolerance = new JTextField();
		cbxPredefinedChords       = new JCheckBox();
		fldChordNoteOnTolerance   = new JTextField();
		fldChordNoteOffTolerance  = new JTextField();
		fldChordVelocityTolerance = new JTextField();
		cbxUseDottedNote          = new JCheckBox(Dict.get(Dict.USE_DOTTED_NOTES));
		cbxUseDottedRest          = new JCheckBox(Dict.get(Dict.USE_DOTTED_RESTS));
		cbxUseTriplettedNote      = new JCheckBox(Dict.get(Dict.USE_TRIPLETTED_NOTES));
		cbxUseTriplettedRest      = new JCheckBox(Dict.get(Dict.USE_TRIPLETTED_RESTS));
		cbxUseKaraoke             = new JCheckBox();
		cbxAllSyllablesOrphaned   = new JCheckBox();
		cbxOrphanedSyllables      = new JComboBox<>();
		cbxKarOneChannel          = new JCheckBox();
		cbxCtrlChangeMode         = new JComboBox<>();
		fldAddGlobalAtTick        = new JTextField();
		btnAddGlobalAtTick        = new MidicaButton(Dict.get(Dict.BTN_ADD_TICK));
		fldAddGlobalsEachTick     = new JTextField();
		fldAddGlobalsStartTick    = new JTextField();
		fldAddGlobalsStopTick     = new JTextField();
		btnAddGlobalTicks         = new MidicaButton(Dict.get(Dict.BTN_ADD_TICKS));
		areaGlobalsStr            = new JTextArea();
		btnAllTicks               = new MidicaButton(Dict.get(Dict.BTN_UPDATE_TICKS));
		cbxLengthStrategy.setModel(DecompileConfigController.getComboboxModel(Config.DC_LENGTH_STRATEGY));
		cbxMinTargetTicksOn.setModel(DecompileConfigController.getComboboxModel(Config.DC_MIN_TARGET_TICKS_ON));
		cbxMaxTargetTicksOn.setModel(DecompileConfigController.getComboboxModel(Config.DC_MAX_TARGET_TICKS_ON));
		cbxOrphanedSyllables.setModel(DecompileConfigController.getComboboxModel(Config.DC_ORPHANED_SYLLABLES));
		cbxCtrlChangeMode.setModel(DecompileConfigController.getComboboxModel(Config.DC_CTRL_CHANGE_MODE));
		
		// create controller
		return DecompileConfigController.getInstance(this, icon);
	}
	
	@Override
	protected void initUi() {
		
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
		tabs.add( Dict.get(Dict.DC_TAB_NOTE_REST),   createNoteRestArea(Dict.DC_TAB_NOTE_REST)     );
		tabs.add( Dict.get(Dict.DC_TAB_KARAOKE),     createKaraokeArea(Dict.DC_TAB_KARAOKE)        );
		tabs.add( Dict.get(Dict.DC_TAB_CTRL_CHANGE), createCtrlChangeArea(Dict.DC_TAB_CTRL_CHANGE) );
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
		
		// quality statistics
		constrLeft.gridy++;
		cbxAddStatistics.addActionListener(controller);
		area.add(cbxAddStatistics, constrLeft);
		
		// note length stragegy statistics
		constrLeft.gridy++;
		cbxAddStrategyStat.addActionListener(controller);
		area.add(cbxAddStrategyStat, constrLeft);
		
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
		
		// min note length (min target ticks) for next note on
		// label
		constrLeft.gridy++;
		JLabel lblMinTargetOn = new JLabel( Dict.get(Dict.MIN_TARGET_TICKS_NEXT_ON) );
		Laf.makeBold(lblMinTargetOn);
		area.add(lblMinTargetOn, constrLeft);
		
		// combobox
		constrCenter.gridy++;
		constrCenter.gridwidth = 2;
		constrRight.gridy++;
		cbxMinTargetTicksOn.addActionListener(controller);
		area.add(cbxMinTargetTicksOn, constrCenter);
		constrCenter.gridwidth = 1;
		
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
		
		// separator
		constrLeft.gridy++;
		constrCenter.gridy++;
		constrRight.gridy++;
		constrFull.gridy = constrRight.gridy;
		area.add(Laf.createSeparator(), constrFull);
		
		// min duration to keep
		// label
		constrLeft.gridy++;
		JLabel lblMinDuration = new JLabel( Dict.get(Dict.MIN_DURATION_TO_KEEP) );
		Laf.makeBold(lblMinDuration);
		area.add(lblMinDuration, constrLeft);
		
		// field
		constrCenter.gridy++;
		constrCenter.gridwidth = 1;
		fldMinDurToKeep.getDocument().addDocumentListener(controller);
		fldMinDurToKeep.setPreferredSize(new Dimension(TEXT_FIELD_WIDTH, TEXT_FIELD_HEIGHT));
		area.add(fldMinDurToKeep, constrCenter);
		
		// description
		constrRight.gridy++;
		JLabel descMinDuration = new JLabel( Dict.get(Dict.MIN_DURATION_TO_KEEP_D) );
		area.add(descMinDuration, constrRight);
		
		// max duration to keep
		// label
		constrLeft.gridy++;
		JLabel lblMaxDuration = new JLabel( Dict.get(Dict.MAX_DURATION_TO_KEEP) );
		Laf.makeBold(lblMaxDuration);
		area.add(lblMaxDuration, constrLeft);
		
		// field
		constrCenter.gridy++;
		fldMaxDurToKeep.getDocument().addDocumentListener(controller);
		fldMaxDurToKeep.setPreferredSize(new Dimension(TEXT_FIELD_WIDTH, TEXT_FIELD_HEIGHT));
		area.add(fldMaxDurToKeep, constrCenter);
		
		// description
		constrRight.gridy++;
		JLabel descMaxDuration = new JLabel( Dict.get(Dict.MAX_DURATION_TO_KEEP_D) );
		area.add(descMaxDuration, constrRight);
		
		// separator
		constrLeft.gridy++;
		constrCenter.gridy++;
		constrRight.gridy++;
		constrFull.gridy = constrRight.gridy;
		area.add(Laf.createSeparator(), constrFull);
		
		// note length tick tolerance
		// label
		constrLeft.gridy++;
		JLabel lblLenTickTol = new JLabel( Dict.get(Dict.LENGTH_TICK_TOLERANCE) );
		Laf.makeBold(lblLenTickTol);
		area.add(lblLenTickTol, constrLeft);
		
		// field
		constrCenter.gridy++;
		fldLengthTickTolerance.getDocument().addDocumentListener(controller);
		fldLengthTickTolerance.setPreferredSize(new Dimension(TEXT_FIELD_WIDTH, TEXT_FIELD_HEIGHT));
		area.add(fldLengthTickTolerance, constrCenter);
		
		// description
		constrRight.gridy++;
		JLabel descLenTickTol = new JLabel( Dict.get(Dict.LENGTH_TICK_TOLERANCE_D) );
		area.add(descLenTickTol, constrRight);
		
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
		
		// use karaoke
		JLabel lblUseKaraoke = new JLabel( Dict.get(Dict.USE_KARAOKE) );
		Laf.makeBold(lblUseKaraoke);
		area.add(lblUseKaraoke, constrLeft);
		
		// checkbox
		cbxUseKaraoke.setPreferredSize(new Dimension(TEXT_FIELD_WIDTH, TEXT_FIELD_HEIGHT));
		cbxUseKaraoke.addActionListener(controller);
		area.add(cbxUseKaraoke, constrCenter);
		
		// description
		JLabel descUseKar = new JLabel( Dict.get(Dict.USE_KARAOKE_D) );
		area.add(descUseKar, constrRight);
		
		// regard all syllables as orphaned
		constrLeft.gridy++;
		JLabel lblAllSylOrp = new JLabel( Dict.get(Dict.ALL_SYLLABLES_ORPHANED) );
		Laf.makeBold(lblAllSylOrp);
		area.add(lblAllSylOrp, constrLeft);
		
		// checkbox
		constrCenter.gridy++;
		cbxAllSyllablesOrphaned.setPreferredSize(new Dimension(TEXT_FIELD_WIDTH, TEXT_FIELD_HEIGHT));
		cbxAllSyllablesOrphaned.addActionListener(controller);
		area.add(cbxAllSyllablesOrphaned, constrCenter);
		
		// description
		constrRight.gridy++;
		JLabel descAllSylOrp = new JLabel( Dict.get(Dict.ALL_SYLLABLES_ORPHANED_D) );
		area.add(descAllSylOrp, constrRight);
		
		// orphaned syllables (how to treat them)
		// label
		constrLeft.gridy++;
		JLabel lblOrpSyl = new JLabel( Dict.get(Dict.ORPHANED_SYLLABLES) );
		Laf.makeBold(lblOrpSyl);
		area.add(lblOrpSyl, constrLeft);
		
		// combobox
		constrCenter.gridy++;
		cbxOrphanedSyllables.setPreferredSize(new Dimension(TEXT_FIELD_WIDTH, TEXT_FIELD_HEIGHT));
		cbxOrphanedSyllables.addActionListener(controller);
		area.add(cbxOrphanedSyllables, constrCenter);
		
		// description
		constrRight.gridy++;
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
	 * Creates the area for control change settings.
	 * 
	 * @param tabKey  language key for the tab name
	 * @return the created area
	 */
	private Container createCtrlChangeArea(String tabKey) {
		
		// layout
		JPanel area = new JPanel();
		area.setLayout(new GridBagLayout());
		GridBagConstraints[] constaints = createConstraintsForArea();
		GridBagConstraints constrFull   = constaints[0];
		GridBagConstraints constrLeft   = constaints[1];
		GridBagConstraints constrCenter = constaints[2];
		GridBagConstraints constrRight  = constaints[3];
		
		// tab info box
		area.add(createTabInfo(tabKey, Dict.DC_TABINFO_CTRL_CHANGE), constrFull);
		
		// orphaned syllables
		// label
		JLabel lblOrpSyl = new JLabel( Dict.get(Dict.CTRL_CHANGE_MODE) );
		Laf.makeBold(lblOrpSyl);
		area.add(lblOrpSyl, constrLeft);
		
		// combobox
		cbxCtrlChangeMode.setPreferredSize(new Dimension(TEXT_FIELD_WIDTH, TEXT_FIELD_HEIGHT));
		cbxCtrlChangeMode.addActionListener(controller);
		area.add(cbxCtrlChangeMode, constrCenter);
		
		// description
		JLabel descOrpSyl = new JLabel( Dict.get(Dict.CTRL_CHANGE_MODE_D) );
		area.add(descOrpSyl, constrRight);

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
	
	@Override
	protected void addSpecificKeyBindings() {
		
		// tab bindings
		keyBindingManager.addBindingsForTabLevel1( tabs, Dict.KEY_DC_CONF_TAB_DEBUG,       0 );
		keyBindingManager.addBindingsForTabLevel1( tabs, Dict.KEY_DC_CONF_TAB_NOTE_LENGTH, 1 );
		keyBindingManager.addBindingsForTabLevel1( tabs, Dict.KEY_DC_CONF_TAB_CHORDS,      2 );
		keyBindingManager.addBindingsForTabLevel1( tabs, Dict.KEY_DC_CONF_TAB_NOTE_REST,   3 );
		keyBindingManager.addBindingsForTabLevel1( tabs, Dict.KEY_DC_CONF_TAB_KARAOKE,     4 );
		keyBindingManager.addBindingsForTabLevel1( tabs, Dict.KEY_DC_CONF_TAB_CTRL_CHANGE, 5 );
		keyBindingManager.addBindingsForTabLevel1( tabs, Dict.KEY_DC_CONF_TAB_SLICES,      6 );
		
		// debug tab
		keyBindingManager.addBindingsForTabLevel3( cbxAddTickComments, Dict.KEY_DC_CONF_ADD_TICK_COMMENTS );
		keyBindingManager.addBindingsForTabLevel3( cbxAddConfig,       Dict.KEY_DC_CONF_ADD_CONFIG        );
		keyBindingManager.addBindingsForTabLevel3( cbxAddScore,        Dict.KEY_DC_CONF_ADD_SCORE         );
		keyBindingManager.addBindingsForTabLevel3( cbxAddStatistics,   Dict.KEY_DC_CONF_ADD_STATISTICS    );
		keyBindingManager.addBindingsForTabLevel3( cbxAddStrategyStat, Dict.KEY_DC_CONF_ADD_STRATEGY_STAT );
		
		// note length tab
		keyBindingManager.addBindingsForTabLevel3( cbxLengthStrategy,         Dict.KEY_DC_CONF_NOTE_LENGTH_STRATEGY );
		keyBindingManager.addBindingsForTabLevel3( cbxMinTargetTicksOn,       Dict.KEY_DC_CONF_MIN_TARGET_TICKS_ON  );
		keyBindingManager.addBindingsForTabLevel3( cbxMaxTargetTicksOn,       Dict.KEY_DC_CONF_MAX_TARGET_TICKS_ON  );
		keyBindingManager.addBindingsForTabLevel3( fldMinDurToKeep,           Dict.KEY_DC_CONF_MIN_DUR_TO_KEEP      );
		keyBindingManager.addBindingsForTabLevel3( fldMaxDurToKeep,           Dict.KEY_DC_CONF_MAX_DUR_TO_KEEP      );
		keyBindingManager.addBindingsForTabLevel3( fldLengthTickTolerance,    Dict.KEY_DC_CONF_TOL_TICK_LEN         );
		keyBindingManager.addBindingsForTabLevel3( fldDurationRatioTolerance, Dict.KEY_DC_CONF_TOL_DUR_RATIO        );
		
		// chords tab
		keyBindingManager.addBindingsForTabLevel3( cbxPredefinedChords,       Dict.KEY_DC_CONF_CRD_PREDEFINED );
		keyBindingManager.addBindingsForTabLevel3( fldChordNoteOnTolerance,   Dict.KEY_DC_CONF_CRD_NOTE_ON    );
		keyBindingManager.addBindingsForTabLevel3( fldChordNoteOffTolerance,  Dict.KEY_DC_CONF_CRD_NOTE_OFF   );
		keyBindingManager.addBindingsForTabLevel3( fldChordVelocityTolerance, Dict.KEY_DC_CONF_CRD_VELOCITY   );
		
		// notes/rests tab
		keyBindingManager.addBindingsForTabLevel3( cbxUseDottedNote,     Dict.KEY_DC_CONF_USE_DOT_NOTES  );
		keyBindingManager.addBindingsForTabLevel3( cbxUseDottedRest,     Dict.KEY_DC_CONF_USE_DOT_RESTS  );
		keyBindingManager.addBindingsForTabLevel3( cbxUseTriplettedNote, Dict.KEY_DC_CONF_USE_TRIP_NOTES );
		keyBindingManager.addBindingsForTabLevel3( cbxUseTriplettedRest, Dict.KEY_DC_CONF_USE_TRIP_RESTS );
		
		// karaoke tab
		keyBindingManager.addBindingsForTabLevel3( cbxUseKaraoke,           Dict.KEY_DC_CONF_USE_KARAOKE  );
		keyBindingManager.addBindingsForTabLevel3( cbxAllSyllablesOrphaned, Dict.KEY_DC_CONF_ALL_SYL_ORP  );
		keyBindingManager.addBindingsForTabLevel3( cbxOrphanedSyllables,    Dict.KEY_DC_CONF_KAR_ORPHANED );
		keyBindingManager.addBindingsForTabLevel3( cbxKarOneChannel,        Dict.KEY_DC_CONF_KAR_ONE_CH   );
		
		// control change tab
		keyBindingManager.addBindingsForTabLevel3( cbxCtrlChangeMode, Dict.KEY_DC_CONF_CTRL_CHANGE_MODE );
		
		// slices
		keyBindingManager.addBindingsForTabLevel3( fldAddGlobalAtTick,     Dict.KEY_DC_CONF_FLD_GLOB_SINGLE );
		keyBindingManager.addBindingsForTabLevel3( btnAddGlobalAtTick,     Dict.KEY_DC_CONF_BTN_GLOB_SINGLE );
		keyBindingManager.addBindingsForTabLevel3( fldAddGlobalsEachTick,  Dict.KEY_DC_CONF_FLD_GLOB_EACH   );
		keyBindingManager.addBindingsForTabLevel3( fldAddGlobalsStartTick, Dict.KEY_DC_CONF_FLD_GLOB_FROM   );
		keyBindingManager.addBindingsForTabLevel3( fldAddGlobalsStopTick,  Dict.KEY_DC_CONF_FLD_GLOB_TO     );
		keyBindingManager.addBindingsForTabLevel3( btnAddGlobalTicks,      Dict.KEY_DC_CONF_BTN_GLOB_RANGE  );
		keyBindingManager.addBindingsForTabLevel3( areaGlobalsStr,         Dict.KEY_DC_CONF_AREA_GLOB_ALL   );
		keyBindingManager.addBindingsForTabLevel3( btnAllTicks,            Dict.KEY_DC_CONF_BTN_GLOB_ALL    );
	}
}
