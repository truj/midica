/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.info;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.KeyListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeSet;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.midica.Midica;
import org.midica.config.Config;
import org.midica.config.Dict;
import org.midica.config.KeyBinding;
import org.midica.config.KeyBindingManager;
import org.midica.config.Laf;
import org.midica.file.read.SequenceParser;
import org.midica.file.read.SoundbankParser;
import org.midica.midi.KaraokeAnalyzer;
import org.midica.midi.MessageClassifier;
import org.midica.midi.SequenceAnalyzer;
import org.midica.ui.model.DrumkitTableModel;
import org.midica.ui.model.IMessageType;
import org.midica.ui.model.InstrumentTableModel;
import org.midica.ui.model.MessageTableModel;
import org.midica.ui.model.SingleMessage;
import org.midica.ui.model.MessageTreeNode;
import org.midica.ui.model.MidicaTreeModel;
import org.midica.ui.model.NoteTableModel;
import org.midica.ui.model.PercussionTableModel;
import org.midica.ui.model.SoundbankInstrumentsTableModel;
import org.midica.ui.model.SoundbankResourceTableModel;
import org.midica.ui.model.SyntaxTableModel;
import org.midica.ui.renderer.InstrumentTableCellRenderer;
import org.midica.ui.renderer.MessageTableCellRenderer;
import org.midica.ui.renderer.MidicaTableCellRenderer;
import org.midica.ui.renderer.SoundbankInstrumentTableCellRenderer;
import org.midica.ui.renderer.SoundbankResourceTableCellRenderer;
import org.midica.ui.renderer.SyntaxTableCellRenderer;
import org.midica.ui.tablefilter.FilterIcon;
import org.midica.ui.tablefilter.FilterIconWithLabel;
import org.midica.ui.widget.FlowLabel;
import org.midica.ui.widget.LinkLabel;
import org.midica.ui.widget.MessageTable;
import org.midica.ui.widget.MidicaButton;
import org.midica.ui.widget.MidicaSplitPane;
import org.midica.ui.widget.MidicaTable;
import org.midica.ui.widget.MidicaTableHeader;
import org.midica.ui.widget.MidicaTree;

/**
 * This class defines the GUI view for the information about the current state
 * of the program instance. It contains the following types of information:
 * 
 * - Configuration (currently configured values for different configurable elements)
 *     - Note names
 *     - Percussion instrument shortcuts
 *     - Syntax keywords for MidicaPL
 *     - Instrument shortcuts for non-percussion instruments
 * - Information about the currently loaded soundbank
 *     - General information
 *     - Drum kits and Instruments
 *     - Resources
 * - Information about the currently loaded MIDI sequence.
 *     - General information
 *     - Banks, Instruments and Notes
 *     - MIDI Messages
 * - Key Bindings
 * - General Midica version and build information.
 * 
 * @author Jan Trukenmüller
 */
public class InfoView extends JDialog {
	
	private static final long serialVersionUID = 1L;
	
	// widths and heights, used for dimensions
	private static final int COL_WIDTH_NOTE_NUM          =  80;
	private static final int COL_WIDTH_NOTE_NAME         = 120;
	private static final int COL_WIDTH_NOTE_ALT          = 630;
	private static final int COL_WIDTH_PERC_NUM          =  80;
	private static final int COL_WIDTH_PERC_ID_SHORT     =  80;
	private static final int COL_WIDTH_PERC_ID_LONG      = 250;
	private static final int COL_WIDTH_SYNTAX_NAME       = 180;
	private static final int COL_WIDTH_SYNTAX_DESC       = 360;
	private static final int COL_WIDTH_SYNTAX_KEYWORD    = 100;
	private static final int COL_WIDTH_INSTR_NUM         =  80;
	private static final int COL_WIDTH_INSTR_NAME        = 300;
	private static final int COL_WIDTH_DRUMKIT_NUM       =  80;
	private static final int COL_WIDTH_DRUMKIT_NAME      = 300;
	private static final int COL_WIDTH_SB_INSTR_PROGRAM  =  80;
	private static final int COL_WIDTH_SB_INSTR_BANK     =  80;
	private static final int COL_WIDTH_SB_INSTR_NAME     = 300;
	private static final int COL_WIDTH_SB_INSTR_CHANNELS = 100;
	private static final int COL_WIDTH_SB_INSTR_KEYS     = 120;
	private static final int COL_WIDTH_SB_RES_INDEX      =  45;
	private static final int COL_WIDTH_SB_RES_TYPE       =  55;
	private static final int COL_WIDTH_SB_RES_NAME       = 130;
	private static final int COL_WIDTH_SB_RES_FRAMES     =  60;
	private static final int COL_WIDTH_SB_RES_FORMAT     = 260;
	private static final int COL_WIDTH_SB_RES_CLASS      = 130;
	private static final int COL_WIDTH_MSG_TICK          =  80;
	private static final int COL_WIDTH_MSG_STATUS        =  45;
	private static final int COL_WIDTH_MSG_TRACK         =  30;
	private static final int COL_WIDTH_MSG_CHANNEL       =  30;
	private static final int COL_WIDTH_MSG_LENGTH        =  40;
	private static final int COL_WIDTH_MSG_SUMMARY       = 120;
	private static final int COL_WIDTH_MSG_TYPE          = 500;
	private static final int TABLE_HEIGHT                = 400;
	private static final int MSG_TABLE_PREF_HEIGHT       = 150;
	private static final int COLLAPSE_EXPAND_WIDTH       =  25;
	private static final int COLLAPSE_EXPAND_HEIGHT      =  25;
	private static final int TICK_RANGE_FILTER_WIDTH     =  70; // tick text fields in the message filter
	private static final int TRACK_RANGE_FILTER_WIDTH    =  90; // track text fields in the message filter
	private static final int FILTER_BUTTON_HEIGHT        = Laf.isNimbus ? 19 : 15;
	
	// Fake widths and heights, used for dimensions. The real sizes are
	// determined by the layout manager. But for some reasons,
	// setPreferredSize() is anyway necessary for correct display.
	private static final int MSG_TREE_PREF_WIDTH         = 1;
	private static final int MSG_TREE_PREF_HEIGHT        = 1;
	private static final int MSG_DETAILS_PREF_WIDTH      = 1;
	private static final int MSG_DETAILS_PREF_HEIGHT     = 1;
	private static final int KEYBINDING_TREE_PREF_WIDTH  = 1;
	private static final int KEYBINDING_TREE_PREF_HEIGHT = 1;
	
	private static final int MAX_HEIGHT_LYRICS       = 1; // max height
	
	// width/height constants for FlowLabels
	private static final int CPL_MIDI_INFO             =  73; // CPL: characters per line
	private static final int CPL_SOUNDBANK_INFO        =  73;
	private static final int CPL_MSG_DETAILS           =  28;
	private static final int CPL_ABOUT                 =  35;
	private static final int CPL_KEYBINDING_DESC       =  45;
	private static final int PWIDTH_GENERAL_INFO_VALUE = 500; // PWIDTH: preferred width
	private static final int PWIDTH_MSG_DETAIL_CONTENT = 170;
	private static final int PWIDTH_ABOUT              = 200;
	private static final int PWIDTH_KEYBINDING_DESC    =   1;
	private static final int MAX_HEIGHT_SOUNDBANK_DESC = 155; // max height
	private static final int MAX_HEIGHT_KARAOKE_INFO   =  45; // max height
	
	// filter widget names (used as hashmap key in filterWidgets and as name property)
	public static final String FILTER_CBX_CHAN_INDEP   = "filter_cbx_chan_indep";
	public static final String FILTER_CBX_CHAN_DEP     = "filter_cbx_chan_dep";
	public static final String FILTER_CBX_CHAN_PREFIX  = "filter_cbx_single_";
	public static final String FILTER_CBX_NODE         = "filter_cbx_node";
	public static final String FILTER_CBX_LIMIT_TICKS  = "filter_cbx_limit_ticks";
	public static final String FILTER_CBX_LIMIT_TRACKS = "filter_cbx_limit_tracks";
	public static final String FILTER_CBX_AUTO_SHOW    = "filter_cbx_auto_show";
	public static final String FILTER_TXT_FROM_TICKS   = "filter_txt_from_ticks";
	public static final String FILTER_TXT_TO_TICKS     = "filter_txt_to_ticks";
	public static final String FILTER_TXT_TRACKS       = "filter_txt_tracks";
	public static final String FILTER_BTN_SHOW_TREE    = "filter_btn_show_tree";
	public static final String FILTER_LBL_VISIBLE      = "filter_lbl_visible";
	public static final String FILTER_LBL_TOTAL        = "filter_lbl_total";
	public static final String FILTER_ICON             = "filter_icon";
	
	// dimensions (defined later)
	private static Dimension noteTableDim       = null;
	private static Dimension percTableDim       = null;
	private static Dimension syntaxTableDim     = null;
	private static Dimension instrTableDim      = null;
	private static Dimension drumkitTableDim    = null;
	private static Dimension sbInstrTableDim    = null;
	private static Dimension sbResourceTableDim = null;
	private static Dimension msgTreeDim         = null;
	private static Dimension keyBindingTreeDim  = null;
	private static Dimension msgDetailsDim      = null;
	private static Dimension msgTableDim        = null;
	private static Dimension collapseExpandDim  = null;
	
	private static int collapseExpandHeadlineHeight = 0; // will be filled dynamically
	
	private static InfoView infoView = null;
	
	private InfoController        controller         = null;
	private KeyBindingManager     keyBindingManager  = null;
	private JTabbedPane           content            = null;
	private JTabbedPane           contentConfig      = null;
	private JTabbedPane           contentSoundbank   = null;
	private JTabbedPane           contentMidi        = null;
	private JSplitPane            contentKeybindings = null;
	
	// widgets
	private MidicaTree                  bankTotalTree          = null;
	private MidicaTree                  bankChannelTree        = null;
	private MidicaTree                  keyBindingTree         = null;
	private JTextField                  keybindingTreeFilter   = null;
	private MidicaButton                keybindingAddBtn       = null;
	private JCheckBox                   keybindingResetIdCbx   = null;
	private JCheckBox                   keybindingResetGlobCbx = null;
	private MidicaButton                keybindingResetIdBtn   = null;
	private MidicaButton                keybindingResetGlobBtn = null;
	private MidicaTree                  msgTree                = null;
	private JPanel                      msgDetails             = null;
	private JPanel                      keyBindingDetails      = null;
	private MidicaTable                 msgTable               = null;
	private HashMap<String, JComponent> filterWidgets          = null;
	private JTextField                  addKeyBindingFld       = null;
	private ArrayList<MidicaButton>     expandCollapseButtons  = null;
	
	private HashMap<String, JComponent> tableStringFilterIcons = null;
	
	private static final String logoPath = "org/midica/resources/logo.png";
	
	/**
	 * Creates a new info view window and sets the given owner Dialog.
	 * 
	 * @param owner  window to be set as the info view's owner
	 */
	private InfoView(Window owner) {
		super(owner);
		setTitle(Dict.get(Dict.TITLE_INFO_VIEW));
		
		// initialize table dimensions
		int noteWidth       = COL_WIDTH_NOTE_NUM + COL_WIDTH_NOTE_NAME + COL_WIDTH_NOTE_ALT;
		int percWidth       = COL_WIDTH_PERC_NUM + COL_WIDTH_PERC_ID_SHORT
							+ COL_WIDTH_PERC_ID_LONG;
		int syntaxWidth     = COL_WIDTH_SYNTAX_NAME + COL_WIDTH_SYNTAX_KEYWORD
		                    + COL_WIDTH_SYNTAX_DESC;
		int instrWidth      = COL_WIDTH_INSTR_NUM        + COL_WIDTH_INSTR_NAME;
		int drumkitWidth    = COL_WIDTH_DRUMKIT_NUM      + COL_WIDTH_DRUMKIT_NAME;
		int sbInstrWidth    = COL_WIDTH_SB_INSTR_PROGRAM + COL_WIDTH_SB_INSTR_BANK
		                    + COL_WIDTH_SB_INSTR_NAME    + COL_WIDTH_SB_INSTR_CHANNELS
		                    + COL_WIDTH_SB_INSTR_KEYS;
		int sbResourceWidth = COL_WIDTH_SB_RES_INDEX  + COL_WIDTH_SB_RES_TYPE
		                    + COL_WIDTH_SB_RES_NAME   + COL_WIDTH_SB_RES_FRAMES
		                    + COL_WIDTH_SB_RES_FORMAT + COL_WIDTH_SB_RES_CLASS;
		int msgTableWidth   = COL_WIDTH_MSG_TICK   + COL_WIDTH_MSG_STATUS
		                    + COL_WIDTH_MSG_TRACK  + COL_WIDTH_MSG_CHANNEL
		                    + COL_WIDTH_MSG_LENGTH + COL_WIDTH_MSG_SUMMARY
		                    + COL_WIDTH_MSG_TYPE;
		noteTableDim       = new Dimension( noteWidth,       TABLE_HEIGHT          );
		percTableDim       = new Dimension( percWidth,       TABLE_HEIGHT          );
		syntaxTableDim     = new Dimension( syntaxWidth,     TABLE_HEIGHT          );
		instrTableDim      = new Dimension( instrWidth,      TABLE_HEIGHT          );
		drumkitTableDim    = new Dimension( drumkitWidth,    TABLE_HEIGHT          );
		sbInstrTableDim    = new Dimension( sbInstrWidth,    TABLE_HEIGHT          );
		sbResourceTableDim = new Dimension( sbResourceWidth, TABLE_HEIGHT          );
		msgTableDim        = new Dimension( msgTableWidth,   MSG_TABLE_PREF_HEIGHT );
		
		// initialize dimensions for trees, collapse-all/expand-all buttons and message details
		msgTreeDim        = new Dimension( MSG_TREE_PREF_WIDTH,        MSG_TREE_PREF_HEIGHT        );
		keyBindingTreeDim = new Dimension( KEYBINDING_TREE_PREF_WIDTH, KEYBINDING_TREE_PREF_HEIGHT );
		msgDetailsDim     = new Dimension( MSG_DETAILS_PREF_WIDTH,     MSG_DETAILS_PREF_HEIGHT     );
		collapseExpandDim = new Dimension( COLLAPSE_EXPAND_WIDTH,      COLLAPSE_EXPAND_HEIGHT      );
		
		expandCollapseButtons = new ArrayList<>();
		
		// create content
		init();
		addKeyBindings();
		
		// show everything
		pack();
		setVisible(true);
	}
	
	/**
	 * Initializes the content of all the tabs inside the info view.
	 */
	private void init() {
		
		// content
		content = new JTabbedPane(JTabbedPane.LEFT);
		getContentPane().add(content);
		
		// enable key bindings
		this.controller = new InfoController(this);
		addWindowListener(this.controller);
		
		// add tabs
		tableStringFilterIcons = new HashMap<>();
		content.addTab( Dict.get(Dict.TAB_CONFIG),        createConfigArea()       );
		content.addTab( Dict.get(Dict.TAB_SOUNDBANK),     createSoundbankArea()    );
		content.addTab( Dict.get(Dict.TAB_MIDI_SEQUENCE), createMidiSequenceArea() );
		content.addTab( Dict.get(Dict.TAB_KEYBINDINGS),   createKeyBindingArea()   );
		content.addTab( Dict.get(Dict.TAB_ABOUT),         createAboutArea()        );
	}
	
	/**
	 * Creates the configuration tab.
	 * This contains the following sub tabs:
	 * 
	 * - Note details
	 * - Percussion details
	 * - Syntax details
	 * - Instrument details
	 * 
	 * @return the created configuration area.
	 */
	private Container createConfigArea() {
		// content
		contentConfig = new JTabbedPane(JTabbedPane.TOP);
		
		// add tabs
		contentConfig.addTab( Dict.get(Dict.TAB_NOTE_DETAILS),       createNoteArea()       );
		contentConfig.addTab( Dict.get(Dict.TAB_PERCUSSION_DETAILS), createPercussionArea() );
		contentConfig.addTab( Dict.get(Dict.SYNTAX),                 createSyntaxArea()     );
		contentConfig.addTab( Dict.get(Dict.INSTRUMENT_IDS),         createInstrumentArea() );
		contentConfig.addTab( Dict.get(Dict.DRUMKIT_IDS),            createDrumkitArea()    );
		
		return contentConfig;
	}
	
	/**
	 * Creates the soundbank tab.
	 * This contains the following sub tabs:
	 * 
	 * - General soundbank info
	 * - Instruments and drum kits
	 * - Resources
	 * 
	 * @return the created soundbank area.
	 */
	private Container createSoundbankArea() {
		// content
		contentSoundbank = new JTabbedPane(JTabbedPane.TOP);
		
		// add tabs
		contentSoundbank.addTab( Dict.get(Dict.TAB_SOUNDBANK_INFO),        createSoundbankInfoArea()       );
		contentSoundbank.addTab( Dict.get(Dict.TAB_SOUNDBANK_INSTRUMENTS), createSoundbankInstrumentArea() );
		contentSoundbank.addTab( Dict.get(Dict.TAB_SOUNDBANK_RESOURCES),   createSoundbankResourceArea()   );
		
		return contentSoundbank;
	}
	
	/**
	 * Creates the MIDI sequence tab.
	 * This contains the following sub tabs:
	 * 
	 * - General sequence info
	 * - Used banks, instruments and played notes
	 * - MIDI Messages (containing tree, details and table)
	 * 
	 * @return the created sequence area.
	 */
	private Container createMidiSequenceArea() {
		// content
		contentMidi = new JTabbedPane(JTabbedPane.TOP);
		
		// add tabs
		contentMidi.addTab( Dict.get(Dict.TAB_MIDI_SEQUENCE_INFO), createMidiSequenceInfoArea() );
		contentMidi.addTab( Dict.get(Dict.TAB_MIDI_KARAOKE),       createKaraokeArea()          );
		contentMidi.addTab( Dict.get(Dict.TAB_BANK_INSTR_NOTE),    createBankInstrNoteArea()    );
		contentMidi.addTab( Dict.get(Dict.TAB_MESSAGES),           createMsgArea()              );
		
		return contentMidi;
	}
	
	/**
	 * Creates the key binding area.
	 * 
	 * @return the created area
	 */
	private Container createKeyBindingArea() {
		
		// create split pane
		contentKeybindings = new MidicaSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		contentKeybindings.setResizeWeight(0.5);
		contentKeybindings.setDividerLocation(0.5);
		
		// fill the sub areas
		contentKeybindings.add( createKeyBindingLeft()  );
		contentKeybindings.add( createKeyBindingRight() );
		
		return contentKeybindings;
	}
	
	/**
	 * Creates the note area containing the translation table for note names.
	 * The table translates between MIDI note values and their configured names.
	 * 
	 * @return the created note area.
	 */
	private Container createNoteArea() {
		// content
		JPanel area = new JPanel();
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.NONE;
		constraints.insets     = Laf.INSETS_NWE;
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 1;
		constraints.weighty    = 0;
		
		// label
		FilterIconWithLabel labelWithFilter = new FilterIconWithLabel(Dict.get(Dict.TAB_NOTE_DETAILS), this);
		area.add(labelWithFilter, constraints);
		tableStringFilterIcons.put(Dict.KEY_INFO_CONF_NOTE_FILTER, labelWithFilter);
		
		// table
		constraints.insets  = Laf.INSETS_SWE;
		constraints.fill    = GridBagConstraints.VERTICAL;
		constraints.weighty = 1;
		constraints.gridy++;
		MidicaTable table = new MidicaTable();
		table.setModel(new NoteTableModel());
		table.setDefaultRenderer(Object.class, new MidicaTableCellRenderer());
		JScrollPane scroll = new JScrollPane(table);
		scroll.setPreferredSize(noteTableDim);
		labelWithFilter.setTable(table);
		area.add(scroll, constraints);
		
		// set column sizes and colors
		table.getColumnModel().getColumn( 0 ).setPreferredWidth( COL_WIDTH_NOTE_NUM  );
		table.getColumnModel().getColumn( 1 ).setPreferredWidth( COL_WIDTH_NOTE_NAME );
		table.getColumnModel().getColumn( 2 ).setPreferredWidth( COL_WIDTH_NOTE_ALT  );
		
		return area;
	}
	
	/**
	 * Creates the percussion area containing the translation table for percussion shortcuts.
	 * The table translates between MIDI note values for the percussion channel and their
	 * configured shortcuts.
	 * 
	 * @return the created percussion area
	 */
	private Container createPercussionArea() {
		// content
		JPanel area = new JPanel();
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.NONE;
		constraints.insets     = Laf.INSETS_NWE;
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 1;
		constraints.weighty    = 0;
		
		// label
		FilterIconWithLabel labelWithFilter = new FilterIconWithLabel(Dict.get(Dict.TAB_PERCUSSION_DETAILS), this);
		area.add(labelWithFilter, constraints);
		tableStringFilterIcons.put(Dict.KEY_INFO_CONF_PERC_FILTER, labelWithFilter);
		
		// table
		constraints.insets  = Laf.INSETS_SWE;
		constraints.fill    = GridBagConstraints.VERTICAL;
		constraints.weighty = 1;
		constraints.gridy++;
		MidicaTable table = new MidicaTable();
		table.setModel(new PercussionTableModel());
		table.setDefaultRenderer(Object.class, new MidicaTableCellRenderer());
		JScrollPane scroll = new JScrollPane(table);
		scroll.setPreferredSize(percTableDim);
		labelWithFilter.setTable(table);
		area.add(scroll, constraints);
		
		// set column sizes
		table.getColumnModel().getColumn( 0 ).setPreferredWidth( COL_WIDTH_PERC_NUM      );
		table.getColumnModel().getColumn( 1 ).setPreferredWidth( COL_WIDTH_PERC_ID_SHORT );
		table.getColumnModel().getColumn( 2 ).setPreferredWidth( COL_WIDTH_PERC_ID_LONG  );
		
		return area;
	}
	
	/**
	 * Creates the syntax area containing the translation table for syntax keywords.
	 * 
	 * @return the created syntax area
	 */
	private Container createSyntaxArea() {
		// content
		JPanel area = new JPanel();
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.NONE;
		constraints.insets     = Laf.INSETS_NWE;
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 1;
		constraints.weighty    = 0;
		
		// label
		FilterIconWithLabel labelWithFilter = new FilterIconWithLabel(Dict.get(Dict.SYNTAX), this);
		area.add(labelWithFilter, constraints);
		tableStringFilterIcons.put(Dict.KEY_INFO_CONF_SYNTAX_FILTER, labelWithFilter);
		
		// table
		constraints.insets  = Laf.INSETS_SWE;
		constraints.fill    = GridBagConstraints.BOTH;
		constraints.weighty = 1;
		constraints.gridy++;
		MidicaTable table = new MidicaTable();
		table.setModel(new SyntaxTableModel());
		table.setDefaultRenderer(Object.class, new SyntaxTableCellRenderer());
		JScrollPane scroll = new JScrollPane(table);
		scroll.setPreferredSize(syntaxTableDim);
		labelWithFilter.setTable(table);
		area.add(scroll, constraints);
		
		// set column sizes
		table.getColumnModel().getColumn( 0 ).setPreferredWidth( COL_WIDTH_SYNTAX_NAME    );
		table.getColumnModel().getColumn( 1 ).setPreferredWidth( COL_WIDTH_SYNTAX_KEYWORD );
		table.getColumnModel().getColumn( 2 ).setPreferredWidth( COL_WIDTH_SYNTAX_DESC    );
		
		return area;
	}
	
	/**
	 * Creates the instrument area containing the translation table for instrument IDs.
	 * The table translates between MIDI program numbers and their configured instrument names.
	 * 
	 * @return the created instrument area
	 */
	private Container createInstrumentArea() {
		// content
		JPanel area = new JPanel();
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill   = GridBagConstraints.NONE;
		constraints.insets = Laf.INSETS_NWE;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 1;
		constraints.weighty    = 0;
		
		// label
		FilterIconWithLabel labelWithFilter = new FilterIconWithLabel(Dict.get(Dict.INSTRUMENT_IDS), this);
		area.add(labelWithFilter, constraints);
		tableStringFilterIcons.put(Dict.KEY_INFO_CONF_INSTR_FILTER, labelWithFilter);
		
		// table
		constraints.insets  = Laf.INSETS_SWE;
		constraints.fill    = GridBagConstraints.VERTICAL;
		constraints.weighty = 1;
		constraints.gridy++;
		MidicaTable table = new MidicaTable();
		table.setModel(new InstrumentTableModel());
		table.setDefaultRenderer(Object.class, new InstrumentTableCellRenderer());
		JScrollPane scroll = new JScrollPane(table);
		scroll.setPreferredSize(instrTableDim);
		labelWithFilter.setTable(table);
		area.add(scroll, constraints);
		
		// set column sizes
		table.getColumnModel().getColumn( 0 ).setPreferredWidth( COL_WIDTH_INSTR_NUM  );
		table.getColumnModel().getColumn( 1 ).setPreferredWidth( COL_WIDTH_INSTR_NAME );
		
		return area;
	}
	
	/**
	 * Creates the drumkit area containing the translation table for drumkit IDs.
	 * The table translates between MIDI program numbers and their configured drumkit names.
	 * 
	 * @return the created drumkit area
	 */
	private Container createDrumkitArea() {
		// content
		JPanel area = new JPanel();
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill   = GridBagConstraints.NONE;
		constraints.insets = Laf.INSETS_NWE;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 1;
		constraints.weighty    = 0;
		
		// label
		FilterIconWithLabel labelWithFilter = new FilterIconWithLabel(Dict.get(Dict.DRUMKIT_IDS), this);
		area.add(labelWithFilter, constraints);
		tableStringFilterIcons.put(Dict.KEY_INFO_CONF_DRUMKIT_FILTER, labelWithFilter);
		
		// table
		constraints.insets  = Laf.INSETS_SWE;
		constraints.fill    = GridBagConstraints.VERTICAL;
		constraints.weighty = 1;
		constraints.gridy++;
		MidicaTable table = new MidicaTable();
		table.setModel(new DrumkitTableModel());
		table.setDefaultRenderer(Object.class, new MidicaTableCellRenderer());
		JScrollPane scroll = new JScrollPane(table);
		scroll.setPreferredSize(drumkitTableDim);
		labelWithFilter.setTable(table);
		area.add(scroll, constraints);
		
		// set column sizes
		table.getColumnModel().getColumn( 0 ).setPreferredWidth( COL_WIDTH_DRUMKIT_NUM  );
		table.getColumnModel().getColumn( 1 ).setPreferredWidth( COL_WIDTH_DRUMKIT_NAME );
		
		return area;
	}
	
	/**
	 * Creates the area for general soundbank information.
	 * 
	 * @return the created soundbank info area.
	 */
	private Container createSoundbankInfoArea() {
		// content
		JPanel area = new JPanel();
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.NONE;
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 0;
		constraints.weighty    = 0;
		
		// get general soundbank info
		HashMap<String, String> soundbankInfo = SoundbankParser.getSoundbankInfo();
		
		// sound source translation
		constraints.insets  = Laf.INSETS_NW;
		constraints.anchor  = GridBagConstraints.NORTHEAST;
		JLabel lblSndSource = new JLabel(Dict.get(Dict.SOUND_SOURCE) + ": ");
		Laf.makeBold(lblSndSource);
		area.add(lblSndSource, constraints);
		
		// sound source
		constraints.insets = Laf.INSETS_NE;
		constraints.gridx++;
		constraints.anchor  = GridBagConstraints.NORTHWEST;
		constraints.weightx = 1;
		String shortSoundName = SoundbankParser.getShortName();
		String fullSoundName  = SoundbankParser.getFullPath();
		if (null == shortSoundName || null == fullSoundName) {
			shortSoundName = Dict.get(Dict.UNCHOSEN_FILE);
			fullSoundName  = Dict.get(Dict.UNCHOSEN_FILE);
		}
		FlowLabel lblSndSourceContent = new FlowLabel(shortSoundName, CPL_SOUNDBANK_INFO, PWIDTH_GENERAL_INFO_VALUE);
		lblSndSourceContent.setToolTipText(fullSoundName);
		area.add(lblSndSourceContent, constraints);
		
		// name translation
		constraints.insets = Laf.INSETS_W;
		constraints.gridx  = 0;
		constraints.gridy++;
		constraints.weightx = 0;
		constraints.anchor  = GridBagConstraints.NORTHEAST;
		JLabel lblName      = new JLabel(Dict.get(Dict.NAME) + ": ");
		Laf.makeBold(lblName);
		area.add(lblName, constraints);
		
		// name content
		constraints.insets = Laf.INSETS_E;
		constraints.gridx++;
		constraints.anchor       = GridBagConstraints.NORTHWEST;
		FlowLabel lblNameContent = new FlowLabel(soundbankInfo.get("name"), CPL_SOUNDBANK_INFO, PWIDTH_GENERAL_INFO_VALUE);
		area.add(lblNameContent, constraints);
		
		// version translation
		constraints.insets = Laf.INSETS_W;
		constraints.gridx  = 0;
		constraints.gridy++;
		constraints.anchor = GridBagConstraints.NORTHEAST;
		JLabel lblVersion  = new JLabel(Dict.get(Dict.VERSION) + ": ");
		Laf.makeBold(lblVersion);
		area.add(lblVersion, constraints);
		
		// version content
		constraints.insets = Laf.INSETS_E;
		constraints.gridx++;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		FlowLabel lblVersionContent = new FlowLabel(soundbankInfo.get("version"), CPL_SOUNDBANK_INFO, PWIDTH_GENERAL_INFO_VALUE);
		area.add(lblVersionContent, constraints);
		
		// vendor translation
		constraints.insets = Laf.INSETS_W;
		constraints.gridx  = 0;
		constraints.gridy++;
		constraints.anchor = GridBagConstraints.NORTHEAST;
		JLabel lblVendor  = new JLabel(Dict.get(Dict.SOUNDBANK_VENDOR) + ": ");
		Laf.makeBold(lblVendor);
		area.add(lblVendor, constraints);
		
		// vendor content
		constraints.insets = Laf.INSETS_E;
		constraints.gridx++;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		FlowLabel lblVendorContent = new FlowLabel(soundbankInfo.get("vendor"), CPL_SOUNDBANK_INFO, PWIDTH_GENERAL_INFO_VALUE);
		area.add(lblVendorContent, constraints);
		
		// creation date translation
		constraints.insets = Laf.INSETS_W;
		constraints.gridx  = 0;
		constraints.gridy++;
		constraints.anchor     = GridBagConstraints.NORTHEAST;
		JLabel lblCreationDate = new JLabel(Dict.get(Dict.SOUNDBANK_CREA_DATE) + ": ");
		Laf.makeBold(lblCreationDate);
		area.add(lblCreationDate, constraints);
		
		// creation date content
		constraints.insets = Laf.INSETS_E;
		constraints.gridx++;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		FlowLabel lblCreationDateContent = new FlowLabel(soundbankInfo.get("creation_date"), CPL_SOUNDBANK_INFO, PWIDTH_GENERAL_INFO_VALUE);
		area.add(lblCreationDateContent, constraints);
		
		// creation tools translation
		constraints.insets = Laf.INSETS_W;
		constraints.gridx  = 0;
		constraints.gridy++;
		constraints.anchor = GridBagConstraints.NORTHEAST;
		JLabel lblTools    = new JLabel(Dict.get(Dict.SOUNDBANK_CREA_TOOLS) + ": ");
		Laf.makeBold(lblTools);
		area.add(lblTools, constraints);
		
		// version content
		constraints.insets = Laf.INSETS_E;
		constraints.gridx++;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		FlowLabel lblToolsContent = new FlowLabel(soundbankInfo.get("tools"), CPL_SOUNDBANK_INFO, PWIDTH_GENERAL_INFO_VALUE);
		area.add(lblToolsContent, constraints);
		
		// product translation
		constraints.insets = Laf.INSETS_W;
		constraints.gridx  = 0;
		constraints.gridy++;
		constraints.anchor = GridBagConstraints.NORTHEAST;
		JLabel lblProduct  = new JLabel(Dict.get(Dict.SOUNDBANK_PRODUCT) + ": ");
		Laf.makeBold(lblProduct);
		area.add(lblProduct, constraints);
		
		// version content
		constraints.insets = Laf.INSETS_E;
		constraints.gridx++;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		FlowLabel lblProductContent = new FlowLabel(soundbankInfo.get("product"), CPL_SOUNDBANK_INFO, PWIDTH_GENERAL_INFO_VALUE);
		area.add(lblProductContent, constraints);
		
		// target engine translation
		constraints.insets = Laf.INSETS_W;
		constraints.gridx  = 0;
		constraints.gridy++;
		constraints.anchor     = GridBagConstraints.NORTHEAST;
		JLabel lblTargetEngine = new JLabel(Dict.get(Dict.SOUNDBANK_TARGET_ENGINE) + ": ");
		Laf.makeBold(lblTargetEngine);
		area.add(lblTargetEngine, constraints);
		
		// version content
		constraints.insets = Laf.INSETS_E;
		constraints.gridx++;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		FlowLabel lblTargetEngineContent = new FlowLabel(soundbankInfo.get("target_engine"), CPL_SOUNDBANK_INFO, PWIDTH_GENERAL_INFO_VALUE);
		area.add(lblTargetEngineContent, constraints);
		
		// chromatic instruments translation
		constraints.insets = Laf.INSETS_W;
		constraints.gridx  = 0;
		constraints.gridy++;
		constraints.anchor = GridBagConstraints.NORTHEAST;
		JLabel lblChromatic  = new JLabel(Dict.get(Dict.SB_INSTR_CAT_CHROMATIC) + ": ");
		Laf.makeBold(lblChromatic);
		area.add(lblChromatic, constraints);
		
		// chromatic instruments content
		constraints.insets = Laf.INSETS_E;
		constraints.gridx++;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		FlowLabel lblChromaticContent = new FlowLabel(soundbankInfo.get("chromatic_count"), CPL_SOUNDBANK_INFO, PWIDTH_GENERAL_INFO_VALUE);
		area.add(lblChromaticContent, constraints);
		
		// drum kits translation
		constraints.insets = Laf.INSETS_W;
		constraints.gridx  = 0;
		constraints.gridy++;
		constraints.anchor       = GridBagConstraints.NORTHEAST;
		JLabel lblDrumkitsSingle = new JLabel(Dict.get(Dict.SOUNDBANK_DRUMKITS) + ": ");
		Laf.makeBold(lblDrumkitsSingle);
		area.add(lblDrumkitsSingle, constraints);
		
		// drum kits content
		int drumSingle = Integer.parseInt(soundbankInfo.get("drumkit_single_count"));
		int drumMulti  = Integer.parseInt(soundbankInfo.get("drumkit_multi_count"));
		int drumTotal  = drumSingle + drumMulti;
		String drumkitsContent = drumTotal  + " ("
		                       + drumSingle + " " + Dict.get(Dict.SINGLE_CHANNEL) + ", "
		                       + drumMulti  + " " + Dict.get(Dict.MULTI_CHANNEL)  + ")";
		constraints.insets = Laf.INSETS_E;
		constraints.gridx++;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		FlowLabel lblDrumkitsSingleContent = new FlowLabel(drumkitsContent, CPL_SOUNDBANK_INFO, PWIDTH_GENERAL_INFO_VALUE);
		area.add(lblDrumkitsSingleContent, constraints);
		
		// resources translation
		constraints.insets = Laf.INSETS_W;
		constraints.gridx  = 0;
		constraints.gridy++;
		constraints.anchor  = GridBagConstraints.NORTHEAST;
		JLabel lblResources = new JLabel(Dict.get(Dict.TAB_SOUNDBANK_RESOURCES) + ": ");
		Laf.makeBold(lblResources);
		area.add(lblResources, constraints);
		
		// resources content
		int resLayer   = Integer.parseInt( soundbankInfo.get("layer_count")            );
		int resSample  = Integer.parseInt( soundbankInfo.get("sample_count")           );
		int resUnknown = Integer.parseInt( soundbankInfo.get("unknown_resource_count") );
		int resourcesTotal = resLayer + resSample + resUnknown;
		String resourcesContent = resourcesTotal + " ("
		             + resLayer   + " " + Dict.get(Dict.SB_RESOURCE_CAT_LAYER)  + ", "
		             + resSample  + " " + Dict.get(Dict.SB_RESOURCE_CAT_SAMPLE) + ")";
		constraints.insets = Laf.INSETS_E;
		constraints.gridx++;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		FlowLabel lblLayersContent = new FlowLabel(resourcesContent, CPL_SOUNDBANK_INFO, PWIDTH_GENERAL_INFO_VALUE);
		area.add(lblLayersContent, constraints);
		
		// total length translation
		constraints.insets = Laf.INSETS_W;
		constraints.gridx  = 0;
		constraints.gridy++;
		constraints.anchor = GridBagConstraints.NORTHEAST;
		JLabel lblTotal    = new JLabel(Dict.get(Dict.SAMPLES_TOTAL) + ": ");
		Laf.makeBold(lblTotal);
		area.add(lblTotal, constraints);
		
		// total length content
		constraints.insets = Laf.INSETS_E;
		constraints.gridx++;
		constraints.anchor  = GridBagConstraints.NORTHWEST;
		String totalContent = soundbankInfo.get("frames_count")  + " " + Dict.get(Dict.FRAMES) + ", "
		                    + soundbankInfo.get("seconds_count") + " " + Dict.get(Dict.SEC)    + ", "
		                    + soundbankInfo.get("bytes_count")   + " " + Dict.get(Dict.BYTES);
		FlowLabel lblTotalContent = new FlowLabel(totalContent, CPL_SOUNDBANK_INFO, PWIDTH_GENERAL_INFO_VALUE);
		area.add(lblTotalContent, constraints);
		
		// average length translation
		constraints.insets = Laf.INSETS_W;
		constraints.gridx  = 0;
		constraints.gridy++;
		constraints.anchor = GridBagConstraints.NORTHEAST;
		JLabel lblAverage  = new JLabel(Dict.get(Dict.SAMPLES_AVERAGE) + ": ");
		Laf.makeBold(lblAverage);
		area.add(lblAverage, constraints);
		
		// average length content
		constraints.insets = Laf.INSETS_W;
		constraints.gridx++;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		String avgContent  = soundbankInfo.get("frames_avg")  + " " + Dict.get(Dict.FRAMES) + ", "
		                   + soundbankInfo.get("seconds_avg") + " " + Dict.get(Dict.SEC)    + ", "
                           + soundbankInfo.get("bytes_avg")   + " " + Dict.get(Dict.BYTES);
		FlowLabel lblAverageContent = new FlowLabel(avgContent, CPL_SOUNDBANK_INFO, PWIDTH_GENERAL_INFO_VALUE);
		area.add(lblAverageContent, constraints);
		
		// description translation
		constraints.insets = Laf.INSETS_SW;
		constraints.gridx  = 0;
		constraints.gridy++;
		constraints.anchor    = GridBagConstraints.NORTHEAST;
		JLabel lblDescription = new JLabel(Dict.get(Dict.DESCRIPTION) + ": ");
		Laf.makeBold(lblDescription);
		area.add(lblDescription, constraints);
		
		// description content
		constraints.insets = Laf.INSETS_SE;
		constraints.gridx++;
		constraints.anchor  = GridBagConstraints.NORTHWEST;
		constraints.weighty = 1;
		FlowLabel lblDescriptionContent = new FlowLabel(soundbankInfo.get("description"), CPL_SOUNDBANK_INFO, PWIDTH_GENERAL_INFO_VALUE);
		lblDescriptionContent.setHeightLimit(MAX_HEIGHT_SOUNDBANK_DESC);
		area.add(lblDescriptionContent, constraints);
		
		return area;
	}
	
	/**
	 * Creates the area for general MIDI sequence information.
	 * 
	 * @return the created MIDI sequence info area.
	 */
	private Container createMidiSequenceInfoArea() {
		// content
		JPanel area = new JPanel();
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.NONE;
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 0;
		constraints.weighty    = 0;
		
		// get general sequence info
		HashMap<String, Object> sequenceInfo = SequenceAnalyzer.getSequenceInfo();
		
		// file translation
		constraints.insets = Laf.INSETS_NW;
		constraints.anchor = GridBagConstraints.NORTHEAST;
		JLabel lblFile     = new JLabel(Dict.get(Dict.FILE) + ": ");
		Laf.makeBold(lblFile);
		area.add(lblFile, constraints);
		
		// file name
		constraints.insets = Laf.INSETS_NE;
		constraints.gridx++;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		String filename    = SequenceParser.getFileName();
		if (null == filename) {
			filename = "-";
		}
		FlowLabel lblFileContent = new FlowLabel(filename, CPL_MIDI_INFO, PWIDTH_GENERAL_INFO_VALUE);
		area.add(lblFileContent, constraints);
		
		// copyright translation
		constraints.insets = Laf.INSETS_W;
		constraints.gridy++;
		constraints.gridx  = 0;
		constraints.anchor = GridBagConstraints.NORTHEAST;
		JLabel lblCopy     = new JLabel(Dict.get(Dict.COPYRIGHT) + ": ");
		Laf.makeBold(lblCopy);
		area.add(lblCopy, constraints);
		
		// copyright content
		constraints.insets = Laf.INSETS_E;
		constraints.gridx++;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		String copyright                 = "-";
		Object metaObj                   = sequenceInfo.get("meta_info");
		HashMap<String, String> metaInfo = new HashMap<String, String>();
		if (metaObj != null)
			metaInfo = (HashMap<String, String>) metaObj;
		if (metaInfo.containsKey("copyright"))
			copyright = metaInfo.get("copyright");
		FlowLabel lblCopyContent = new FlowLabel(copyright, CPL_MIDI_INFO, PWIDTH_GENERAL_INFO_VALUE);
		area.add(lblCopyContent, constraints);
		
		// software translation
		constraints.insets = Laf.INSETS_W;
		constraints.gridy++;
		constraints.gridx  = 0;
		constraints.anchor = GridBagConstraints.NORTHEAST;
		JLabel lblSoftware = new JLabel(Dict.get(Dict.SOFTWARE_VERSION) + ": ");
		Laf.makeBold(lblSoftware);
		area.add(lblSoftware, constraints);
		
		// software content
		constraints.insets = Laf.INSETS_E;
		constraints.gridx++;
		constraints.anchor  = GridBagConstraints.NORTHWEST;
		String software     = "-";
		if (metaInfo.containsKey("software")) {
			software = metaInfo.get("software");
		}
		if (metaInfo.containsKey("software_date")) {
			String softwareDate = metaInfo.get("software_date");
			software = software + " - " + Dict.get(Dict.SOFTWARE_DATE) + " " + softwareDate;
		}
		FlowLabel lblSoftwareContent = new FlowLabel(software, CPL_MIDI_INFO, PWIDTH_GENERAL_INFO_VALUE);
		area.add(lblSoftwareContent, constraints);
		
		// ticks translation
		constraints.insets = Laf.INSETS_W;
		constraints.gridy++;
		constraints.gridx  = 0;
		constraints.anchor = GridBagConstraints.NORTHEAST;
		JLabel lblTicks    = new JLabel(Dict.get(Dict.TICK_LENGTH) + ": ");
		Laf.makeBold(lblTicks);
		area.add(lblTicks, constraints);
		
		// ticks content
		constraints.insets = Laf.INSETS_E;
		constraints.gridx++;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		String lenghStr = "-";
		Object ticksObj = sequenceInfo.get("ticks");
		if (ticksObj != null)
			lenghStr = Long.toString((Long) ticksObj);
		FlowLabel lblTicksContent = new FlowLabel(lenghStr, CPL_MIDI_INFO, PWIDTH_GENERAL_INFO_VALUE);
		area.add(lblTicksContent, constraints);
		
		// time translation
		constraints.insets = Laf.INSETS_W;
		constraints.gridy++;
		constraints.gridx  = 0;
		constraints.anchor = GridBagConstraints.NORTHEAST;
		JLabel lblTime     = new JLabel(Dict.get(Dict.TIME_LENGTH) + ": ");
		Laf.makeBold(lblTime);
		area.add(lblTime, constraints);
		
		// time content
		constraints.insets = Laf.INSETS_E;
		constraints.gridx++;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		lenghStr = "-";
		Object timeObj = sequenceInfo.get("time_length");
		if (timeObj != null)
			lenghStr = (String) timeObj;
		FlowLabel lblTimeContent = new FlowLabel((String) lenghStr, CPL_MIDI_INFO, PWIDTH_GENERAL_INFO_VALUE);
		area.add(lblTimeContent, constraints);
		
		// resolution translation
		constraints.insets = Laf.INSETS_W;
		constraints.gridy++;
		constraints.gridx    = 0;
		constraints.anchor   = GridBagConstraints.NORTHEAST;
		JLabel lblResolution = new JLabel(Dict.get(Dict.RESOLUTION) + ": ");
		Laf.makeBold(lblResolution);
		area.add(lblResolution, constraints);
		
		// resolution content
		constraints.insets = Laf.INSETS_E;
		constraints.gridx++;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		String resolution  = "-";
		if (null != sequenceInfo.get("resolution"))
			resolution = sequenceInfo.get("resolution") + " " + Dict.get(Dict.RESOLUTION_UNIT);
		FlowLabel lblResolutionContent = new FlowLabel(resolution, CPL_MIDI_INFO, PWIDTH_GENERAL_INFO_VALUE);
		area.add(lblResolutionContent, constraints);
		
		// number of tracks translation
		constraints.insets = Laf.INSETS_W;
		constraints.gridy++;
		constraints.gridx   = 0;
		constraints.anchor  = GridBagConstraints.NORTHEAST;
		JLabel lblNumTracks = new JLabel(Dict.get(Dict.NUMBER_OF_TRACKS) + ": ");
		Laf.makeBold(lblNumTracks);
		area.add(lblNumTracks, constraints);
		
		// number of tracks content
		constraints.insets = Laf.INSETS_E;
		constraints.gridx++;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		String numTracks   = "-";
		if (null != sequenceInfo.get("num_tracks")) {
			Object tracksObj = sequenceInfo.get("num_tracks");
			numTracks        = Integer.toString((int) tracksObj);
		}
		FlowLabel lblNumTracksContent = new FlowLabel(numTracks, CPL_MIDI_INFO, PWIDTH_GENERAL_INFO_VALUE);
		area.add(lblNumTracksContent, constraints);
		
		// spacer
		constraints.gridy++;
		constraints.gridx  = 0;
		JLabel spacerLine1 = new JLabel(" ");
		area.add(spacerLine1, constraints);
		
		// tempo BPM headline
		constraints.insets = Laf.INSETS_WE;
		constraints.gridwidth = 2; // colspan
		constraints.gridy++;
		constraints.gridx    = 0;
		constraints.anchor   = GridBagConstraints.NORTHWEST;
		JLabel lblTempoBpmHeadline = new JLabel(Dict.get(Dict.TEMPO_BPM));
		Laf.makeBold(lblTempoBpmHeadline);
		area.add(lblTempoBpmHeadline, constraints);
		constraints.gridwidth = 1; // end of colspan
		
		// BPM average translation
		constraints.insets = Laf.INSETS_W;
		constraints.gridy++;
		constraints.gridx    = 0;
		constraints.anchor   = GridBagConstraints.NORTHEAST;
		JLabel lblBpmAvg = new JLabel(Dict.get(Dict.AVERAGE) + ": ");
		Laf.makeBold(lblBpmAvg);
		area.add(lblBpmAvg, constraints);
		
		// BPM average content
		constraints.insets = Laf.INSETS_E;
		constraints.gridx++;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		String tempoStr = "-";
		Object tempoObj = sequenceInfo.get("tempo_bpm_avg");
		if (null != tempoObj)
			tempoStr = (String) tempoObj;
		FlowLabel lblBpmAvgContent = new FlowLabel(tempoStr, CPL_MIDI_INFO, PWIDTH_GENERAL_INFO_VALUE);
		area.add(lblBpmAvgContent, constraints);
		
		// BPM min translation
		constraints.insets = Laf.INSETS_W;
		constraints.gridy++;
		constraints.gridx    = 0;
		constraints.anchor   = GridBagConstraints.NORTHEAST;
		JLabel lblBpmMin = new JLabel(Dict.get(Dict.MIN) + ": ");
		Laf.makeBold(lblBpmMin);
		area.add(lblBpmMin, constraints);
		
		// BPM min content
		constraints.insets = Laf.INSETS_E;
		constraints.gridx++;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		tempoObj           = sequenceInfo.get("tempo_bpm_min");
		if (null != tempoObj)
			tempoStr = (String) tempoObj;
		FlowLabel lblBpmMinContent = new FlowLabel(tempoStr, CPL_MIDI_INFO, PWIDTH_GENERAL_INFO_VALUE);
		area.add(lblBpmMinContent, constraints);
		
		// BPM max translation
		constraints.insets = Laf.INSETS_W;
		constraints.gridy++;
		constraints.gridx    = 0;
		constraints.anchor   = GridBagConstraints.NORTHEAST;
		JLabel lblBpmMax = new JLabel(Dict.get(Dict.MAX) + ": ");
		Laf.makeBold(lblBpmMax);
		area.add(lblBpmMax, constraints);
		
		// BPM max content
		constraints.insets = Laf.INSETS_E;
		constraints.gridx++;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		tempoObj           = sequenceInfo.get("tempo_bpm_max");
		if (null != tempoObj)
			tempoStr = (String) tempoObj;
		FlowLabel lblBpmMaxContent = new FlowLabel(tempoStr, CPL_MIDI_INFO, PWIDTH_GENERAL_INFO_VALUE);
		area.add(lblBpmMaxContent, constraints);
		
		// spacer
		constraints.gridy++;
		constraints.gridx  = 0;
		JLabel spacerLine2 = new JLabel(" ");
		area.add(spacerLine2, constraints);
		
		// tempo MPQ headline
		constraints.insets = Laf.INSETS_WE;
		constraints.gridwidth = 2; // colspan
		constraints.gridy++;
		constraints.gridx    = 0;
		constraints.anchor   = GridBagConstraints.NORTHWEST;
		JLabel lblTempoMpqHeadline = new JLabel(Dict.get(Dict.TEMPO_MPQ));
		Laf.makeBold(lblTempoMpqHeadline);
		area.add(lblTempoMpqHeadline, constraints);
		constraints.gridwidth = 1; // end of colspan
		
		// MPQ average translation
		constraints.insets = Laf.INSETS_W;
		constraints.gridy++;
		constraints.gridx    = 0;
		constraints.anchor   = GridBagConstraints.NORTHEAST;
		JLabel lblMpqAvg = new JLabel(Dict.get(Dict.AVERAGE) + ": ");
		Laf.makeBold(lblMpqAvg);
		area.add(lblMpqAvg, constraints);
		
		// MPQ average content
		constraints.insets = Laf.INSETS_E;
		constraints.gridx++;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		tempoObj           = sequenceInfo.get("tempo_mpq_avg");
		if (null != tempoObj)
			tempoStr = (String) tempoObj;
		FlowLabel lblMpqAvgContent = new FlowLabel(tempoStr, CPL_MIDI_INFO, PWIDTH_GENERAL_INFO_VALUE);
		area.add(lblMpqAvgContent, constraints);
		
		// MPQ min translation
		constraints.insets = Laf.INSETS_W;
		constraints.gridy++;
		constraints.gridx    = 0;
		constraints.anchor   = GridBagConstraints.NORTHEAST;
		JLabel lblMpqMin = new JLabel(Dict.get(Dict.MIN) + ": ");
		Laf.makeBold(lblMpqMin);
		area.add(lblMpqMin, constraints);
		
		// MPQ min content
		constraints.insets = Laf.INSETS_E;
		constraints.gridx++;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		tempoObj           = sequenceInfo.get("tempo_mpq_min");
		if (null != tempoObj)
			tempoStr = (String) tempoObj;
		FlowLabel lblMpqMinContent = new FlowLabel(tempoStr, CPL_MIDI_INFO, PWIDTH_GENERAL_INFO_VALUE);
		area.add(lblMpqMinContent, constraints);
		
		// MPQ max translation
		constraints.insets = Laf.INSETS_SW;
		constraints.gridy++;
		constraints.gridx    = 0;
		constraints.anchor   = GridBagConstraints.NORTHEAST;
		JLabel lblMpqMax = new JLabel(Dict.get(Dict.MAX) + ": ");
		Laf.makeBold(lblMpqMax);
		area.add(lblMpqMax, constraints);
		
		// MPQ max content
		constraints.insets  = Laf.INSETS_SE;
		constraints.weighty = 1;
		constraints.gridx++;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		tempoObj           = sequenceInfo.get("tempo_mpq_max");
		if (null != tempoObj)
			tempoStr = (String) tempoObj;
		FlowLabel lblMpqMaxContent = new FlowLabel(tempoStr, CPL_MIDI_INFO, PWIDTH_GENERAL_INFO_VALUE);
		area.add(lblMpqMaxContent, constraints);
		
		return area;
	}
	
	/**
	 * Creates the area for karaoke and lyrics information from the
	 * loaded MIDI sequence.
	 * 
	 * @return the created area.
	 */
	private Container createKaraokeArea() {
		
		// content
		JPanel area = new JPanel();
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.HORIZONTAL;
		constraints.insets     = Laf.INSETS_NWE;
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 1;
		constraints.weighty    = 0;
		
		// get karaoke info
		HashMap<String, Object> karaokeInfo = KaraokeAnalyzer.getKaraokeInfo();
		
		// general
		area.add(createKaraokeGeneralArea(karaokeInfo), constraints);
		
		// soft karaoke
		constraints.gridy++;
		constraints.insets = Laf.INSETS_WE;
		area.add(createKaraokeSkArea(karaokeInfo), constraints);
		
		// lyrics
		constraints.gridy++;
		constraints.weighty = 1;
		constraints.fill    = GridBagConstraints.BOTH;
		constraints.insets  = Laf.INSETS_SWE;
		area.add(createKaraokeLyricsArea(karaokeInfo), constraints);
		
		return area;
	}
	
	/**
	 * Creates the area for general karaoke information inside of the karaoke and lyrics area.
	 * 
	 * @param karaokeInfo  karaoke information, extracted from the MIDI sequence
	 * @return the created area.
	 */
	private Container createKaraokeGeneralArea(HashMap<String, Object> karaokeInfo) {
		
		// content
		JPanel area = new JPanel();
		area.setBorder(Laf.createTitledBorder(Dict.get(Dict.KARAOKE_GENERAL)));
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout(layout);
		GridBagConstraints constrLeft = new GridBagConstraints();
		constrLeft.fill       = GridBagConstraints.NONE;
		constrLeft.insets     = Laf.INSETS_W;
		constrLeft.gridx      = 0;
		constrLeft.gridy      = 0;
		constrLeft.gridheight = 1;
		constrLeft.gridwidth  = 1;
		constrLeft.weightx    = 0;
		constrLeft.weighty    = 0;
		constrLeft.anchor     = GridBagConstraints.NORTHEAST;
		GridBagConstraints constrRight = (GridBagConstraints) constrLeft.clone();
		constrRight.gridx++;
		constrRight.insets  = Laf.INSETS_E;
		constrRight.weightx = 1;
		constrRight.fill    = GridBagConstraints.HORIZONTAL;
		constrRight.anchor  = GridBagConstraints.NORTHWEST;
		
		// title
		JLabel lblTitle = new JLabel(Dict.get(Dict.SONG_TITLE) + ": ");
		Laf.makeBold(lblTitle);
		area.add(lblTitle, constrLeft);
		
		// title content
		String title = (String) karaokeInfo.get("title");
		if (null == title)
			title = "-";
		FlowLabel lblTitleContent = new FlowLabel(title, CPL_MIDI_INFO, PWIDTH_GENERAL_INFO_VALUE);
		area.add(lblTitleContent, constrRight);
		
		// composer
		constrLeft.gridy++;
		JLabel lblComposer = new JLabel(Dict.get(Dict.COMPOSER) + ": ");
		Laf.makeBold(lblComposer);
		area.add(lblComposer, constrLeft);
		
		// composer content
		constrRight.gridy++;
		String composer = "-";
		if (null != karaokeInfo.get("composer"))
			composer = (String) karaokeInfo.get("composer");
		FlowLabel lblComposerContent = new FlowLabel(composer, CPL_MIDI_INFO, PWIDTH_GENERAL_INFO_VALUE);
		area.add(lblComposerContent, constrRight);
		
		// lyricist
		constrLeft.gridy++;
		JLabel lblLyricist = new JLabel(Dict.get(Dict.LYRICIST) + ": ");
		Laf.makeBold(lblLyricist);
		area.add(lblLyricist, constrLeft);
		
		// lyricist content
		constrRight.gridy++;
		String lyricist = "-";
		if (null != karaokeInfo.get("lyricist"))
			lyricist = (String) karaokeInfo.get("lyricist");
		FlowLabel lblLyricistContent = new FlowLabel(lyricist, CPL_MIDI_INFO, PWIDTH_GENERAL_INFO_VALUE);
		area.add(lblLyricistContent, constrRight);
		
		// artist
		constrLeft.gridy++;
		JLabel lblArtist = new JLabel(Dict.get(Dict.ARTIST) + ": ");
		Laf.makeBold(lblArtist);
		area.add(lblArtist, constrLeft);
		
		// artist content
		constrRight.gridy++;
		String artist = "-";
		if (null != karaokeInfo.get("artist"))
			artist = (String) karaokeInfo.get("artist");
		FlowLabel lblArtistContent = new FlowLabel(artist, CPL_MIDI_INFO, PWIDTH_GENERAL_INFO_VALUE);
		area.add(lblArtistContent, constrRight);
		
		return area;
	}
	
	/**
	 * Creates the area for SOFT KARAOKE inside of the karaoke and lyrics area.
	 * 
	 * @param karaokeInfo  karaoke information, extracted from the MIDI sequence
	 * @return the created area.
	 */
	private Container createKaraokeSkArea(HashMap<String, Object> karaokeInfo) {
		
		// content
		JPanel area = new JPanel();
		area.setBorder(Laf.createTitledBorder(Dict.get(Dict.KARAOKE_SOFT_KARAOKE)));
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout(layout);
		GridBagConstraints constrLeft = new GridBagConstraints();
		constrLeft.fill       = GridBagConstraints.NONE;
		constrLeft.insets     = Laf.INSETS_W;
		constrLeft.gridx      = 0;
		constrLeft.gridy      = 0;
		constrLeft.gridheight = 1;
		constrLeft.gridwidth  = 1;
		constrLeft.weightx    = 0;
		constrLeft.weighty    = 0;
		constrLeft.anchor     = GridBagConstraints.NORTHEAST;
		GridBagConstraints constrRight = (GridBagConstraints) constrLeft.clone();
		constrRight.gridx++;
		constrRight.insets  = Laf.INSETS_E;
		constrRight.weightx = 1;
		constrRight.fill    = GridBagConstraints.HORIZONTAL;
		constrRight.anchor  = GridBagConstraints.NORTHWEST;
		
		// karaoke type
		JLabel lblKarType = new JLabel(Dict.get(Dict.KARAOKE_TYPE) + ": ");
		Laf.makeBold(lblKarType);
		area.add(lblKarType, constrLeft);
		
		// karaoke type content
		String karType = "-";
		if (null != karaokeInfo.get("sk_type"))
			karType = (String) karaokeInfo.get("sk_type");
		FlowLabel lblKarTypeContent = new FlowLabel(karType, CPL_MIDI_INFO, PWIDTH_GENERAL_INFO_VALUE);
		area.add(lblKarTypeContent, constrRight);
		
		// version
		constrLeft.gridy++;
		JLabel lblVersion  = new JLabel(Dict.get(Dict.VERSION) + ": ");
		Laf.makeBold(lblVersion);
		area.add(lblVersion, constrLeft);
		
		// version content
		constrRight.gridy++;
		String version = "-";
		if (null != karaokeInfo.get("sk_version"))
			version = (String) karaokeInfo.get("sk_version");
		FlowLabel lblVersionContent = new FlowLabel(version, CPL_MIDI_INFO, PWIDTH_GENERAL_INFO_VALUE);
		area.add(lblVersionContent, constrRight);
		
		// info
		constrLeft.gridy++;
		JLabel lblInfo = new JLabel(Dict.get(Dict.KARAOKE_INFO) + ": ");
		Laf.makeBold(lblInfo);
		area.add(lblInfo, constrLeft);
		
		// info content
		constrRight.gridy++;
		String info = "-";
		if (null != karaokeInfo.get("info"))
			info = (String) karaokeInfo.get("info");
		FlowLabel lblInfoContent = new FlowLabel(info, CPL_MIDI_INFO, PWIDTH_GENERAL_INFO_VALUE);
		lblInfoContent.setHeightLimit(MAX_HEIGHT_KARAOKE_INFO);
		area.add(lblInfoContent, constrRight);
		
		// language
		constrLeft.gridy++;
		JLabel lblLanguage = new JLabel(Dict.get(Dict.LANGUAGE) + ": ");
		Laf.makeBold(lblLanguage);
		area.add(lblLanguage, constrLeft);
		
		// language content
		constrRight.gridy++;
		String language = "-";
		if (null != karaokeInfo.get("sk_language"))
			language = (String) karaokeInfo.get("sk_language");
		FlowLabel lblLanguageContent = new FlowLabel(language, CPL_MIDI_INFO, PWIDTH_GENERAL_INFO_VALUE);
		area.add(lblLanguageContent, constrRight);
		
		// title
		constrLeft.gridy++;
		JLabel lblTitle = new JLabel(Dict.get(Dict.SONG_TITLE) + ": ");
		Laf.makeBold(lblTitle);
		area.add(lblTitle, constrLeft);
		
		// title content
		constrRight.gridy++;
		String title = (String) karaokeInfo.get("sk_title");
		if (null == title || "".equals(title)) {
			title = "-";
		}
		FlowLabel lblTitleContent = new FlowLabel(title, CPL_MIDI_INFO, PWIDTH_GENERAL_INFO_VALUE);
		area.add(lblTitleContent, constrRight);
		
		// author
		constrLeft.gridy++;
		JLabel lblAuthor = new JLabel(Dict.get(Dict.AUTHOR) + ": ");
		Laf.makeBold(lblAuthor);
		area.add(lblAuthor, constrLeft);
		
		// author content
		constrRight.gridy++;
		String author = "";
		if (null != karaokeInfo.get("sk_author"))
			author = (String) karaokeInfo.get("sk_author");
		if ("".equals(author))
			author = "-";
		FlowLabel lblAuthorContent = new FlowLabel(author, CPL_MIDI_INFO, PWIDTH_GENERAL_INFO_VALUE);
		area.add(lblAuthorContent, constrRight);
		
		// copyright
		constrLeft.gridy++;
		JLabel lblCopyright = new JLabel(Dict.get(Dict.KARAOKE_COPYRIGHT) + ": ");
		Laf.makeBold(lblCopyright);
		area.add(lblCopyright, constrLeft);
		
		// copyright content
		constrRight.gridy++;
		String copyright = "-";
		if (null != karaokeInfo.get("sk_copyright"))
			copyright = (String) karaokeInfo.get("sk_copyright");
		FlowLabel lblCopyrightContent = new FlowLabel(copyright, CPL_MIDI_INFO, PWIDTH_GENERAL_INFO_VALUE);
		area.add(lblCopyrightContent, constrRight);
		
		return area;
	}
	
	/**
	 * Creates the area for lyrics inside of the karaoke and lyrics area.
	 * 
	 * @param karaokeInfo  karaoke information, extracted from the MIDI sequence
	 * @return the created area.
	 */
	private Container createKaraokeLyricsArea(HashMap<String, Object> karaokeInfo) {
		// content
		JPanel area = new JPanel();
		area.setBorder(Laf.createTitledBorder(Dict.get(Dict.LYRICS)));
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.BOTH;
		constraints.insets     = Laf.INSETS_WE;
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 1;
		constraints.weighty    = 1;
		constraints.anchor     = GridBagConstraints.NORTHWEST;
		
		String lyrics = "";
		if (null != karaokeInfo.get("lyrics_full"))
			lyrics = (String) karaokeInfo.get("lyrics_full");
		FlowLabel lblLyricsContent = new FlowLabel(lyrics, CPL_MIDI_INFO, PWIDTH_GENERAL_INFO_VALUE);
		lblLyricsContent.setHeightLimit(MAX_HEIGHT_LYRICS);
		area.add(lblLyricsContent, constraints);
		
		return area;
	}
	
	/**
	 * Creates the area for banks, instruments and notes
	 * used by the loaded MIDI sequence.
	 * 
	 * @return the created area.
	 */
	private Container createBankInstrNoteArea() {
		
		// create split pane
		MidicaSplitPane area = new MidicaSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		area.setResizeWeight(0.5);
		area.setDividerLocation(0.5);
		
		// fill the sub areas
		area.add(createBankInstrNoteAreaHalf(true));
		area.add(createBankInstrNoteAreaHalf(false));
		
		return area;
	}
	
	/**
	 * Creates the area for banks, instruments and notes used by the loaded
	 * MIDI sequence, either **in total** or **per channel**.
	 * 
	 * @param isTotal  **true** for creating the total usage tree area.
	 *                 **false** for the per channel area.
	 * @return the created area.
	 */
	private Container createBankInstrNoteAreaHalf(boolean isTotal) {
		// content
		JPanel area = new JPanel();
		
		// initialize area-dependant constants
		String keyName  = "banks_per_channel";
		String headline = Dict.get(Dict.PER_CHANNEL);
		String btnName  = InfoController.NAME_TREE_BANKS_PER_CHANNEL;
		if (isTotal) {
			keyName  = "banks_total";
			headline = Dict.get(Dict.TOTAL);
			btnName  = InfoController.NAME_TREE_BANKS_TOTAL;
		}
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.BOTH;
		constraints.insets     = isTotal ? Laf.INSETS_NW : Laf.INSETS_NE;
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 1;
		constraints.weighty    = 0;
		constraints.anchor     = GridBagConstraints.NORTH;
		
		// get tree model and inform the controller
		HashMap<String, Object> sequenceInfo = SequenceAnalyzer.getSequenceInfo();
		Object          contentObj = sequenceInfo.get(keyName);
		MidicaTreeModel model;
		if (contentObj != null && contentObj instanceof MidicaTreeModel) {
			model = (MidicaTreeModel) contentObj;
			model.postprocess();
		}
		else {
			model = new MidicaTreeModel(Dict.get(Dict.TOTAL));
		}
		controller.setTreeModel(model, btnName);
		
		// headline (translation, expand, collapse)
		Container head = createTreeHeadline(headline, btnName);
		area.add(head, constraints);
		
		// tree
		constraints.insets = isTotal ? Laf.INSETS_SW : Laf.INSETS_SE;
		constraints.gridy++;
		constraints.weighty = 1;
		MidicaTree  tree   = new MidicaTree(model);
		JScrollPane scroll = new JScrollPane(tree);
		model.setTree(tree);
		area.add(scroll, constraints);
		if (isTotal)
			bankTotalTree = tree;
		else
			bankChannelTree = tree;
		
		return area;
	}
	
	/**
	 * Creates the left side of the key binding area, containing
	 * the key binding tree and the collapse/expand buttons.
	 * 
	 * @return the created area
	 */
	private Container createKeyBindingLeft() {
		// content
		JPanel area = new JPanel();
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.BOTH;
		constraints.insets     = Laf.INSETS_NW;
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 1;
		constraints.weighty    = 0;
		constraints.anchor     = GridBagConstraints.NORTH;
		
		// get tree model and inform the controller
		MidicaTreeModel model = controller.getKeyBindingTreeModel();
		
		// headline (translation, expand, collapse)
		Container head = createTreeHeadline(Dict.get(Dict.TAB_KEYBINDINGS), InfoController.NAME_TREE_KEYBINDINGS);
		area.add(head, constraints);
		
		// tree filter
		constraints.gridy++;
		constraints.insets = Laf.INSETS_W;
		Container filter = createTreeFilter();
		area.add(filter, constraints);
		
		// tree
		constraints.insets = Laf.INSETS_W;
		constraints.gridy++;
		constraints.weighty = 1;
		keyBindingTree      = new MidicaTree(model);
		JScrollPane scroll  = new JScrollPane(keyBindingTree);
		scroll.setPreferredSize(keyBindingTreeDim);
		keyBindingTree.setName(InfoController.NAME_TREE_KEYBINDINGS);
		keyBindingTree.addTreeSelectionListener(controller);
		keyBindingTree.addFocusListener(controller);
		model.setTree(keyBindingTree);
		area.add(scroll, constraints);
		
		// prepare reset checkbox and reset button
		keybindingResetGlobCbx = new JCheckBox(Dict.get(Dict.KB_RESET_GLOB_CBX));
		keybindingResetGlobBtn = new MidicaButton(Dict.get(Dict.KB_RESET_GLOB_BTN));
		keybindingResetGlobBtn.setActionCommand(InfoController.CMD_RESET_KEY_BINDING_GLOB);
		keybindingResetGlobBtn.addActionListener(controller);
		
		// reset checkbox
		constraints.gridy++;
		constraints.weighty = 0;
		area.add(keybindingResetGlobCbx, constraints);
		keybindingResetGlobCbx.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (keybindingResetGlobCbx.isSelected())
					keybindingResetGlobBtn.setEnabled(true);
				else
					keybindingResetGlobBtn.setEnabled(false);
			}
		});
		
		// reset button
		constraints.insets = Laf.INSETS_SW;
		constraints.gridy++;
		keybindingResetGlobBtn.setEnabled(false);
		area.add(keybindingResetGlobBtn, constraints);
		
		return area;
	}
	
	/**
	 * Creates the area containing the key binding tree filter (label and text field).
	 * 
	 * @return the created area.
	 */
	private Container createTreeFilter() {
		// content
		JPanel area = new JPanel();
		keybindingTreeFilter = new JTextField("");
		keybindingTreeFilter.setName(InfoController.NAME_KB_FILTER);
		keybindingTreeFilter.getDocument().putProperty("name", InfoController.NAME_KB_FILTER);
		keybindingTreeFilter.getDocument().addDocumentListener(controller);
		
		// layout
		BoxLayout layout = new BoxLayout(area, BoxLayout.X_AXIS);
		area.setLayout(layout);
		
		// spacer
		area.add(Box.createHorizontalStrut(3));
		
		// label
		JLabel lbl = new JLabel(Dict.get(Dict.KB_FILTER));
		area.add(lbl);
		
		// spacer
		area.add(Box.createHorizontalStrut(10));
		
		// text field
		area.add(keybindingTreeFilter);
		
		return area;
	}
	
	/**
	 * Returns the key binding tree filter text field.
	 * 
	 * @return the key binding tree filter text field
	 */
	public JTextField getKeybindingTreeFilter() {
		return keybindingTreeFilter;
	}
	
	/**
	 * Creates the right side of the key binding area, where the selected
	 * key binding ID can be configured.
	 * 
	 * @return the created area
	 */
	private Container createKeyBindingRight() {
		// content
		JPanel area = new JPanel();
		
		// create objects that need to be available even if they are not visible
		addKeyBindingFld = new JTextField("");
		keybindingAddBtn = new MidicaButton(Dict.get(Dict.KB_ADD_BTN));
		keybindingAddBtn.setActionCommand(InfoController.CMD_ADD_KEY_BINDING);
		keybindingAddBtn.addActionListener(controller);
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.NONE;
		constraints.insets     = Laf.INSETS_NE;
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 0;
		constraints.weighty    = 0;
		constraints.anchor     = GridBagConstraints.CENTER;
		
		// details headline
		JLabel lblDetails = new JLabel(Dict.get(Dict.DETAILS));
		Laf.makeBold(lblDetails);
		area.add(lblDetails, constraints);
		
		// synchronize with the tree headline from the other
		// half of the split pane
		int       width       = lblDetails.getPreferredSize().width;
		Dimension headlineDim = new Dimension(width, collapseExpandHeadlineHeight);
		lblDetails.setPreferredSize(headlineDim);
		
		// details content
		constraints.insets = Laf.INSETS_E;
		constraints.gridy++;
		constraints.weightx = 1;
		constraints.weighty = 1;
		constraints.fill    = GridBagConstraints.BOTH;
		keyBindingDetails   = new JPanel();
		keyBindingDetails.setLayout(layout);
		keyBindingDetails.setBackground(Laf.COLOR_KEYBINDING_DEFAULT);
		JScrollPane kbScroll = new JScrollPane(keyBindingDetails);
		area.add(kbScroll, constraints);
		
		// add reset checkbox
		constraints.gridy++;
		constraints.weighty = 0;
		keybindingResetIdCbx = new JCheckBox(Dict.get(Dict.KB_RESET_ID_CBX));
		area.add(keybindingResetIdCbx, constraints);
		
		// add reset button
		constraints.gridy++;
		constraints.insets = Laf.INSETS_SE;
		keybindingResetIdBtn = new MidicaButton(Dict.get(Dict.KB_RESET_ID_BTN));
		keybindingResetIdBtn.setActionCommand(InfoController.CMD_RESET_KEY_BINDING_ID);
		keybindingResetIdBtn.addActionListener(controller);
		keybindingResetIdBtn.setEnabled(false);
		area.add(keybindingResetIdBtn, constraints);
		
		// connect button and checkbox with each other
		keybindingResetIdCbx.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (keybindingResetIdCbx.isSelected())
					keybindingResetIdBtn.setEnabled(true);
				else
					keybindingResetIdBtn.setEnabled(false);
			}
		});
		
		// disable the reset widgets by default
		resetResetWidgetsForSelectedKeyBindingAction();
		
		return area;
	}
	
	/**
	 * Creates the headline area for a {@link MidicaTree}, containing a translation
	 * and collapse-all / expand-all buttons.
	 * 
	 * @param headline  Text to be displayed above the tree.
	 * @param btnName   Name for the buttons.
	 * @return the created area.
	 */
	private Container createTreeHeadline(String headline, String btnName) {
		// content
		JPanel area = new JPanel();
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.NONE;
		constraints.insets     = Laf.INSETS_IN;
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 0;
		constraints.weighty    = 0;
		constraints.anchor     = GridBagConstraints.CENTER;
		
		// headline translation
		JLabel lblHeadline = new JLabel(headline);
		Laf.makeBold(lblHeadline);
		area.add(lblHeadline, constraints);
		
		// spacer
		constraints.gridx++;
		JLabel spacer1 = new JLabel(" ");
		area.add(spacer1, constraints);
		
		// collapse button
		constraints.gridx++;
		MidicaButton btnCollapse = new MidicaButton(Dict.get(Dict.COLLAPSE_BUTTON));
		btnCollapse.setToolTipText(Dict.get(Dict.COLLAPSE_TOOLTIP));
		btnCollapse.setPreferredSize(collapseExpandDim);
		btnCollapse.setMargin(Laf.INSETS_BTN_EXPAND_COLLAPSE);
		btnCollapse.setName(btnName);
		btnCollapse.setActionCommand(InfoController.CMD_COLLAPSE);
		btnCollapse.addActionListener(controller);
		area.add(btnCollapse, constraints);
		expandCollapseButtons.add(btnCollapse);
		
		// spacer
		constraints.gridx++;
		JLabel spacer2 = new JLabel("");
		area.add(spacer2, constraints);
		
		// expand button
		constraints.gridx++;
		MidicaButton btnExpand = new MidicaButton(Dict.get(Dict.EXPAND_BUTTON));
		btnExpand.setToolTipText(Dict.get(Dict.EXPAND_TOOLTIP));
		btnExpand.setPreferredSize(collapseExpandDim);
		btnExpand.setMargin(Laf.INSETS_BTN_EXPAND_COLLAPSE);
		btnExpand.setName(btnName);
		btnExpand.setActionCommand(InfoController.CMD_EXPAND);
		btnExpand.addActionListener(controller);
		area.add(btnExpand, constraints);
		expandCollapseButtons.add(btnExpand);
		
		return area;
	}
	
	/**
	 * Creates the MIDI message area for the message tree, details area and
	 * message table.
	 * 
	 * @return the created area.
	 */
	private Container createMsgArea() {
		
		// total content
		MidicaSplitPane area = new MidicaSplitPane(JSplitPane.VERTICAL_SPLIT);
		area.setResizeWeight(0.45);
		area.setDividerLocation(0.45);
		
		// tree and details
		MidicaSplitPane topArea = new MidicaSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		topArea.add( createMsgTreeArea()    );
		topArea.add( createMsgDetailsArea() );
		topArea.setResizeWeight(0.5);
		topArea.setDividerLocation(0.5);
		area.add(topArea);
		
		// list
		area.add(createMsgTableArea());
		
		return area;
	}
	
	/**
	 * Creates the area for the message tree including the
	 * collapse-all / expand-all buttons.
	 * 
	 * @return the created area.
	 */
	private Container createMsgTreeArea() {
		// content
		JPanel area = new JPanel();
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.BOTH;
		constraints.insets     = Laf.INSETS_NW;
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 0;
		constraints.weighty    = 0;
		constraints.anchor     = GridBagConstraints.CENTER;
		
		// get tree model and inform the controller
		HashMap<String, Object> sequenceInfo = SequenceAnalyzer.getSequenceInfo();
		Object          modelObj = sequenceInfo.get("msg_tree_model");
		MidicaTreeModel model    = null;
		if (modelObj != null && modelObj instanceof MidicaTreeModel) {
			model = (MidicaTreeModel) modelObj;
			model.postprocess();
		}
		else {
			try {
				model = new MidicaTreeModel(Dict.get(Dict.TAB_MESSAGES), MessageTreeNode.class);
			}
			catch (ReflectiveOperationException e) {
				e.printStackTrace();
			}
		}
		controller.setTreeModel(model, InfoController.NAME_TREE_MESSAGES);
		
		// tree headline (translation, expand, collapse)
		String headline              = Dict.get(Dict.TAB_MESSAGES);
		String btnName               = InfoController.NAME_TREE_MESSAGES;
		Container head               = createTreeHeadline(headline, btnName);
		collapseExpandHeadlineHeight = head.getPreferredSize().height;
		area.add(head, constraints);
		
		// message tree
		constraints.insets = Laf.INSETS_W;
		constraints.gridy++;
		constraints.weightx = 1;
		constraints.weighty = 1;
		msgTree             = new MidicaTree(model);
		msgTree.setName(InfoController.NAME_TREE_MESSAGES);
		msgTree.addTreeSelectionListener(controller);
		msgTree.addFocusListener(controller);
		msgTree.setBackground(Laf.COLOR_MSG_TREE);
		msgTree.setMsgTreeFlag();
		
		// use the tree's background color also for the nodes - important for metal look and feel
		DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) msgTree.getCellRenderer();
		renderer.setBackgroundNonSelectionColor(null);
		
		// add tree to area
		JScrollPane msgScroll = new JScrollPane(msgTree);
		model.setTree(msgTree);
		msgScroll.setPreferredSize(msgTreeDim);
		area.add(msgScroll, constraints);
		
		return area;
	}
	
	/**
	 * Creates the MIDI message details area.
	 * 
	 * @return the created area.
	 */
	private Container createMsgDetailsArea() {
		// content
		JPanel area = new JPanel();
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.NONE;
		constraints.insets     = Laf.INSETS_NE;
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 0;
		constraints.weighty    = 0;
		constraints.anchor     = GridBagConstraints.CENTER;
		
		// details headline
		JLabel lblDetails = new JLabel(Dict.get(Dict.DETAILS));
		Laf.makeBold(lblDetails);
		area.add(lblDetails, constraints);
		
		// synchronize with the tree headline from the other
		// half of the split pane
		int       width       = lblDetails.getPreferredSize().width;
		Dimension headlineDim = new Dimension(width, collapseExpandHeadlineHeight);
		lblDetails.setPreferredSize(headlineDim);
		
		// details content
		constraints.insets = Laf.INSETS_E;
		constraints.gridy++;
		constraints.weightx = 1;
		constraints.weighty = 1;
		constraints.fill    = GridBagConstraints.BOTH;
		msgDetails = new JPanel();
		msgDetails.setLayout(layout);
		msgDetails.setBackground(Laf.COLOR_MSG_DEFAULT);
		JScrollPane detailsScroll = new JScrollPane(msgDetails);
		detailsScroll.setPreferredSize(msgDetailsDim);
		area.add(detailsScroll, constraints);
		
		return area;
	}
	
	/**
	 * Creates the MIDI message table area.
	 * 
	 * @return the created area.
	 */
	private Container createMsgTableArea() {
		// content
		JPanel area = new JPanel();
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.BOTH;
		constraints.insets     = Laf.INSETS_WE;
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 0;
		constraints.weighty    = 0;
		constraints.anchor     = GridBagConstraints.NORTHWEST;
		
		// get messages
		ArrayList<SingleMessage> messages = SequenceAnalyzer.getMessages();
		
		// filter
		long minTick = 0;
		long maxTick = 0;
		if (null == messages) {
			messages = new ArrayList<>();
		}
		int last = messages.size() - 1;
		if (last > 0) {
			minTick = (long) messages.get( 0    ).getOption(IMessageType.OPT_TICK);
			maxTick = (long) messages.get( last ).getOption(IMessageType.OPT_TICK);
		}
		Container filter = createMsgFilterArea(minTick, maxTick);
		area.add(filter, constraints);
		
		// message table
		constraints.insets = Laf.INSETS_SWE;
		constraints.gridy++;
		constraints.weightx     = 1;
		constraints.weighty     = 1;
		constraints.fill        = GridBagConstraints.BOTH;
		MessageTableModel model = new MessageTableModel(messages);
		msgTable                = new MessageTable(model);
		msgTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		msgTable.getSelectionModel().addListSelectionListener(controller);
		msgTable.addFocusListener(controller);
		msgTable.getColumnModel().getColumn( 0 ).setPreferredWidth( COL_WIDTH_MSG_TICK    );
		msgTable.getColumnModel().getColumn( 1 ).setPreferredWidth( COL_WIDTH_MSG_STATUS  );
		msgTable.getColumnModel().getColumn( 2 ).setPreferredWidth( COL_WIDTH_MSG_TRACK   );
		msgTable.getColumnModel().getColumn( 3 ).setPreferredWidth( COL_WIDTH_MSG_CHANNEL );
		msgTable.getColumnModel().getColumn( 4 ).setPreferredWidth( COL_WIDTH_MSG_LENGTH  );
		msgTable.getColumnModel().getColumn( 5 ).setPreferredWidth( COL_WIDTH_MSG_SUMMARY );
		msgTable.getColumnModel().getColumn( 6 ).setPreferredWidth( COL_WIDTH_MSG_TYPE    );
		MessageTableCellRenderer renderer = new MessageTableCellRenderer(model);
		msgTable.setDefaultRenderer(Object.class, renderer);
		MidicaTableHeader header = (MidicaTableHeader) msgTable.getTableHeader();
		header.setBackground(Laf.COLOR_MSG_TABLE_HEADER_BG);
		header.setForeground(Laf.COLOR_MSG_TABLE_HEADER_TXT);
		msgTable.setGridColor(Laf.COLOR_MSG_TABLE_GRID);
		((FilterIcon) filterWidgets.get(FILTER_ICON)).setTable(msgTable);
		((FilterIcon) filterWidgets.get(FILTER_ICON)).addRowSorterListener(controller);
		
		JScrollPane scroll = new JScrollPane(msgTable);
		scroll.setPreferredSize(msgTableDim);
		area.add(scroll, constraints);
		
		return area;
	}
	
	/**
	 * Creates the message filter area.
	 * 
	 * The given parameters are used to prefill the tick fields.
	 * 
	 * @param minTick  Lowest tick from the sequence or **0** if no sequence is loaded.
	 * @param maxTick  Highest tick from the sequence or **0** if no sequence is loaded.
	 * @return the created area.
	 */
	private Container createMsgFilterArea(long minTick, long maxTick) {
		// content
		JPanel area = new JPanel();
		
		// prepare filter widget storing
		filterWidgets = new HashMap<String, JComponent>();
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor     = GridBagConstraints.WEST;
		constraints.fill       = GridBagConstraints.NONE;
		constraints.insets     = Laf.INSETS_ZERO;
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 0;
		constraints.weighty    = 0;
		
		// line 1
		
		// checkbox to filter channel-independent messages
		JCheckBox cbxChannelIndep = new JCheckBox(Dict.get(Dict.MSG_FILTER_CHANNEL_INDEP));
		cbxChannelIndep.setToolTipText(Dict.get(Dict.MSG_FLTR_TT_CHANNEL_INDEP));
		cbxChannelIndep.addItemListener(controller);
		cbxChannelIndep.setSelected(true);
		cbxChannelIndep.setName(FILTER_CBX_CHAN_INDEP);
		area.add(cbxChannelIndep, constraints);
		filterWidgets.put(FILTER_CBX_CHAN_INDEP, cbxChannelIndep);
		
		// checkbox to filter selected tree nodes
		constraints.insets = Laf.INSETS_MSG_FILTER_CBX_LBL;
		constraints.gridx++;
		JCheckBox cbxFilterNodes = new JCheckBox(Dict.get(Dict.MSG_FILTER_SELECTED_NODES));
		cbxFilterNodes.setToolTipText(Dict.get(Dict.MSG_FLTR_TT_SELECTED_NODES));
		cbxFilterNodes.addItemListener(controller);
		cbxFilterNodes.setName(FILTER_CBX_NODE);
		area.add(cbxFilterNodes, constraints);
		filterWidgets.put(FILTER_CBX_NODE, cbxFilterNodes);
		
		// spacer
		constraints.gridx++;
		constraints.fill       = GridBagConstraints.BOTH;
		constraints.weightx    = 1;
		JLabel spacerNodesTicks = new JLabel("");
		area.add(spacerNodesTicks, constraints);
		
		// checkbox to limit ticks
		constraints.gridx++;
		constraints.fill    = GridBagConstraints.NONE;
		constraints.weightx = 0;
		JCheckBox cbxLimitTicks = new JCheckBox(Dict.get(Dict.MSG_FILTER_LIMIT_TICKS));
		cbxLimitTicks.setToolTipText(Dict.get(Dict.MSG_FLTR_TT_LIMIT_TICKS));
		cbxLimitTicks.addItemListener(controller);
		cbxLimitTicks.setName(FILTER_CBX_LIMIT_TICKS);
		area.add(cbxLimitTicks, constraints);
		filterWidgets.put(FILTER_CBX_LIMIT_TICKS, cbxLimitTicks);
		
		// spacer
		constraints.gridx++;
		JLabel spacerTickRange = new JLabel(" ");
		area.add(spacerTickRange, constraints);
		
		// ticks: "from" label
		constraints.gridx++;
		constraints.insets  = Laf.INSETS_MSG_FILTER_FROM_TO_LBL;
		JLabel lblFromTicks = new JLabel(Dict.get(Dict.MSG_FILTER_TICK_FROM) + ": ");
		area.add(lblFromTicks, constraints);
		
		// dimension for text fields
		Dimension dimTicksFld  = new Dimension(TICK_RANGE_FILTER_WIDTH,  Laf.textFieldHeight);
		Dimension dimTracksFld = new Dimension(TRACK_RANGE_FILTER_WIDTH, Laf.textFieldHeight);
		
		// ticks: "from" textfield
		constraints.insets = Laf.INSETS_MSG_FILTER_CBX_LBL;
		constraints.gridx++;
		JTextField txtFromTicks = new JTextField(minTick + "");
		txtFromTicks.setPreferredSize(dimTicksFld);
		txtFromTicks.setName(FILTER_TXT_FROM_TICKS);
		txtFromTicks.setEnabled(false);
		txtFromTicks.getDocument().putProperty("name", FILTER_TXT_FROM_TICKS);
		txtFromTicks.getDocument().addDocumentListener(controller);
		txtFromTicks.setBackground(Laf.COLOR_NORMAL);
		area.add(txtFromTicks, constraints);
		filterWidgets.put(FILTER_TXT_FROM_TICKS, txtFromTicks);
		
		// spacer
		if (Laf.isMetal) {
			constraints.gridx++;
			constraints.fill    = GridBagConstraints.NONE;
			JLabel spacerFromTo = new JLabel(" ");
			area.add(spacerFromTo, constraints);
		}
		
		// ticks: "to" label
		constraints.gridx++;
		constraints.insets = Laf.INSETS_MSG_FILTER_FROM_TO_LBL;
		JLabel lblToTicks  = new JLabel(Dict.get(Dict.MSG_FILTER_TICK_TO) + ": ");
		area.add(lblToTicks, constraints);
		
		// ticks: "to" textfield
		constraints.insets = Laf.INSETS_MSG_FILTER_CBX_LBL;
		constraints.gridx++;
		JTextField txtToTicks = new JTextField(maxTick + "");
		txtToTicks.setPreferredSize(dimTicksFld);
		txtToTicks.setName(FILTER_TXT_TO_TICKS);
		txtToTicks.setEnabled(false);
		txtToTicks.getDocument().putProperty("name", FILTER_TXT_TO_TICKS);
		txtToTicks.getDocument().addDocumentListener(controller);
		txtToTicks.setBackground(Laf.COLOR_NORMAL);
		area.add(txtToTicks, constraints);
		filterWidgets.put(FILTER_TXT_TO_TICKS, txtToTicks);
		
		// spacer
		constraints.gridx++;
		constraints.weightx    = 1;
		constraints.fill       = GridBagConstraints.BOTH;
		JLabel spacerTickTrack = new JLabel("");
		area.add(spacerTickTrack, constraints);
		
		// checkbox to limit tracks
		constraints.gridx++;
		constraints.weightx = 0;
		constraints.fill    = GridBagConstraints.NONE;
		JCheckBox cbxTracks = new JCheckBox(Dict.get(Dict.MSG_FILTER_LIMIT_TRACKS));
		cbxTracks.setToolTipText(Dict.get(Dict.MSG_FLTR_TT_LIMIT_TRACKS));
		cbxTracks.addItemListener(controller);
		cbxTracks.setName(FILTER_CBX_LIMIT_TRACKS);
		area.add(cbxTracks, constraints);
		filterWidgets.put(FILTER_CBX_LIMIT_TRACKS, cbxTracks);
		
		// get max track number
		int maxTrack = 0;
		HashMap<String, Object> sequenceInfo = SequenceAnalyzer.getSequenceInfo();
		Object trackNumObj                   = sequenceInfo.get("num_tracks");
		if (trackNumObj != null) {
			maxTrack = (int) trackNumObj;
			maxTrack--; // track numbers start from 0
		}
		
		// limit tracks text field
		constraints.gridx++;
		constraints.fill      = GridBagConstraints.VERTICAL;
		JTextField txtTracks = new JTextField("0-" + maxTrack);
		txtTracks.setPreferredSize(dimTracksFld);
		txtTracks.setName(FILTER_TXT_TRACKS);
		txtTracks.setEnabled(false);
		txtTracks.getDocument().putProperty("name", FILTER_TXT_TRACKS);
		txtTracks.getDocument().addDocumentListener(controller);
		txtTracks.setBackground(Laf.COLOR_NORMAL);
		txtTracks.setToolTipText(Dict.get(Dict.MSG_FLTR_TT_TRACKS));
		area.add(txtTracks, constraints);
		filterWidgets.put(FILTER_TXT_TRACKS, txtTracks);
		
		// line 2
		
		// checkbox to filter channel messages
		constraints.insets = Laf.INSETS_ZERO;
		constraints.gridy++;
		constraints.gridx       = 0;
		constraints.fill        = GridBagConstraints.NONE;
		JCheckBox cbxChannelDep = new JCheckBox(Dict.get(Dict.MSG_FILTER_CHANNEL_DEP));
		cbxChannelDep.setToolTipText(Dict.get(Dict.MSG_FLTR_TT_CHANNEL_DEP));
		cbxChannelDep.addItemListener(controller);
		cbxChannelDep.setName(FILTER_CBX_CHAN_DEP);
		area.add(cbxChannelDep, constraints);
		cbxChannelDep.setSelected(true);
		filterWidgets.put(FILTER_CBX_CHAN_DEP, cbxChannelDep);
		
		// checkboxes for all channels
		constraints.gridx++;
		constraints.fill      = GridBagConstraints.HORIZONTAL;
		constraints.weightx   = 1;
		constraints.gridwidth = 10;
		area.add(createMsgFilterChannelCheckboxes(), constraints);
		
		// string filter
		constraints.gridx++;
		constraints.weightx   = 0;
		constraints.fill      = GridBagConstraints.NONE;
		constraints.anchor    = GridBagConstraints.EAST;
		FilterIcon filterIcon = new FilterIcon(this);
		area.add(filterIcon, constraints);
		filterWidgets.put(FILTER_ICON, filterIcon);
		
		// line 3
		
		// button, auto-show checkbox, visible/total count
		constraints.gridy++;
		constraints.gridx     = 0;
		constraints.weightx   = 0;
		constraints.gridwidth = 13;
		constraints.fill      = GridBagConstraints.HORIZONTAL;
		area.add(createMsgFilterLine3(), constraints);
		
		return area;
	}
	
	/**
	 * Creates the channel checkboxes for the message filter area.
	 * 
	 * @return the created area.
	 */
	private Container createMsgFilterChannelCheckboxes() {
		// content
		JPanel area = new JPanel();
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.NONE;
		constraints.insets     = Laf.INSETS_MSG_FILTER_CBX_LBL;
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 0;
		constraints.weighty    = 0;
		constraints.anchor     = GridBagConstraints.WEST;
		
		// one checkbox for each channel
		for (byte channel = 0; channel < 16; channel++) {
			constraints.gridx++;
			JCheckBox cbxChannel = new JCheckBox(channel + "");
			cbxChannel.setToolTipText(Dict.get(Dict.MSG_FLTR_TT_CHANNEL_SINGLE) + " " + channel);
			cbxChannel.addItemListener(controller);
			cbxChannel.setName(FILTER_CBX_CHAN_PREFIX + channel);
			cbxChannel.setSelected(true);
			area.add(cbxChannel, constraints);
			filterWidgets.put(FILTER_CBX_CHAN_PREFIX + channel, cbxChannel);
		}
		
		// spacer
		constraints.insets = Laf.INSETS_ZERO;
		constraints.gridx++;
		constraints.weightx    = 1;
		constraints.fill       = GridBagConstraints.NONE;
		JLabel spacerTickRange = new JLabel("");
		area.add(spacerTickRange, constraints);
		
		return area;
	}
	
	/**
	 * Creates third line of the message filter area.
	 * 
	 * This line contains the show-in-tree button, the auto-show checkbox
	 * and the counts of visible and total messages.
	 * 
	 * @return the created area.
	 */
	private Container createMsgFilterLine3() {
		// content
		JPanel area = new JPanel();
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor     = GridBagConstraints.WEST;
		constraints.fill       = GridBagConstraints.NONE;
		constraints.insets     = Laf.INSETS_ZERO;
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 0;
		constraints.weighty    = 0;
		
		// button: show in tree
		MidicaButton btnShowInTree = new MidicaButton(Dict.get(Dict.MSG_FILTER_SHOW_IN_TREE));
		btnShowInTree.setToolTipText(Dict.get(Dict.MSG_FLTR_TT_SHOW_IN_TREE));
		btnShowInTree.setActionCommand(FILTER_BTN_SHOW_TREE);
		btnShowInTree.setName(FILTER_BTN_SHOW_TREE);
		btnShowInTree.addActionListener(controller);
		btnShowInTree.setEnabled(false);
		area.add(btnShowInTree, constraints);
		filterWidgets.put(FILTER_BTN_SHOW_TREE, btnShowInTree);
		// adjust dimension
		int width = btnShowInTree.getPreferredSize().width;
		btnShowInTree.setPreferredSize(new Dimension(width, FILTER_BUTTON_HEIGHT));
		
		// checkbox: auto-show-in-tree
		constraints.gridx++;
		JCheckBox cbxAutoShow = new JCheckBox(Dict.get(Dict.MSG_FILTER_AUTO_SHOW));
		cbxAutoShow.setToolTipText(Dict.get(Dict.MSG_FLTR_TT_AUTO_SHOW));
		cbxAutoShow.addItemListener(controller);
		cbxAutoShow.setName(FILTER_CBX_AUTO_SHOW);
		area.add(cbxAutoShow, constraints);
		cbxAutoShow.setSelected(false);
		filterWidgets.put(FILTER_CBX_AUTO_SHOW, cbxAutoShow);
		
		// spacer
		constraints.gridx++;
		constraints.fill    = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1;
		JLabel spacerRight  = new JLabel("");
		area.add(spacerRight, constraints);
		
		// label: visible messages
		constraints.anchor   = GridBagConstraints.EAST;
		constraints.gridx++;
		constraints.fill     = GridBagConstraints.NONE;
		constraints.weightx  = 0;
		JLabel lblVisibleMsg = new JLabel("0");
		lblVisibleMsg.setToolTipText(Dict.get(Dict.MSG_FLTR_TT_VISIBLE));
		area.add(lblVisibleMsg, constraints);
		filterWidgets.put(FILTER_LBL_VISIBLE, lblVisibleMsg);
		
		// label: /
		constraints.gridx++;
		JLabel lblSlash = new JLabel("  /  ");
		area.add(lblSlash, constraints);
		
		// label: total messages
		constraints.gridx++;
		JLabel lblTotalMsg = new JLabel("0");
		lblTotalMsg.setToolTipText(Dict.get(Dict.MSG_FLTR_TT_TOTAL));
		area.add(lblTotalMsg, constraints);
		filterWidgets.put(FILTER_LBL_TOTAL, lblTotalMsg);
		
		return area;
	}
	
	/**
	 * Creates the area for instruments and drumkits of the currently loaded soundbank.
	 * 
	 * @return the created soundbank instruments area.
	 */
	private Container createSoundbankInstrumentArea() {
		// content
		JPanel area = new JPanel();
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.NONE;
		constraints.insets     = Laf.INSETS_NWE;
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 1;
		constraints.weighty    = 0;
		constraints.anchor     = GridBagConstraints.NORTH;
		
		// label
		FilterIconWithLabel labelWithFilter = new FilterIconWithLabel(Dict.get(Dict.TAB_SOUNDBANK_INSTRUMENTS), this);
		area.add(labelWithFilter, constraints);
		tableStringFilterIcons.put(Dict.KEY_INFO_SB_INSTR_FILTER, labelWithFilter);
		
		// table
		constraints.insets  = Laf.INSETS_SWE;
		constraints.fill    = GridBagConstraints.VERTICAL;
		constraints.weighty = 1;
		constraints.gridy++;
		MidicaTable table = new MidicaTable();
		table.setModel(new SoundbankInstrumentsTableModel());
		table.setDefaultRenderer(Object.class, new SoundbankInstrumentTableCellRenderer());
		JScrollPane scroll = new JScrollPane(table);
		scroll.setPreferredSize(sbInstrTableDim);
		labelWithFilter.setTable(table);
		area.add(scroll, constraints);
		
		// set column sizes and colors
		table.getColumnModel().getColumn( 0 ).setPreferredWidth( COL_WIDTH_SB_INSTR_PROGRAM  );
		table.getColumnModel().getColumn( 1 ).setPreferredWidth( COL_WIDTH_SB_INSTR_BANK     );
		table.getColumnModel().getColumn( 2 ).setPreferredWidth( COL_WIDTH_SB_INSTR_NAME     );
		table.getColumnModel().getColumn( 3 ).setPreferredWidth( COL_WIDTH_SB_INSTR_CHANNELS );
		table.getColumnModel().getColumn( 4 ).setPreferredWidth( COL_WIDTH_SB_INSTR_KEYS     );
		
		return area;
	}
	
	/**
	 * Creates the area for resources of the currently loaded soundbank.
	 * 
	 * @return the created soundbank resource area.
	 */
	private Container createSoundbankResourceArea() {
		// content
		JPanel area = new JPanel();
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.NONE;
		constraints.insets     = Laf.INSETS_NWE;
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 1;
		constraints.weighty    = 0;
		
		// label
		FilterIconWithLabel labelWithFilter = new FilterIconWithLabel(Dict.get(Dict.TAB_SOUNDBANK_RESOURCES), this);
		area.add(labelWithFilter, constraints);
		tableStringFilterIcons.put(Dict.KEY_INFO_SB_RES_FILTER, labelWithFilter);
		
		// table
		constraints.insets  = Laf.INSETS_SWE;
		constraints.fill    = GridBagConstraints.BOTH;
		constraints.weighty = 1;
		constraints.gridy++;
		MidicaTable table = new MidicaTable();
		table.setModel(new SoundbankResourceTableModel());
		table.setDefaultRenderer(Object.class, new SoundbankResourceTableCellRenderer());
		JScrollPane scroll = new JScrollPane(table);
		scroll.setPreferredSize(sbResourceTableDim);
		labelWithFilter.setTable(table);
		area.add(scroll, constraints);
		
		// set column sizes and colors
		table.getColumnModel().getColumn( 0 ).setPreferredWidth( COL_WIDTH_SB_RES_INDEX  );
		table.getColumnModel().getColumn( 1 ).setPreferredWidth( COL_WIDTH_SB_RES_TYPE   );
		table.getColumnModel().getColumn( 2 ).setPreferredWidth( COL_WIDTH_SB_RES_NAME   );
		table.getColumnModel().getColumn( 3 ).setPreferredWidth( COL_WIDTH_SB_RES_FRAMES );
		table.getColumnModel().getColumn( 4 ).setPreferredWidth( COL_WIDTH_SB_RES_FORMAT );
		table.getColumnModel().getColumn( 5 ).setPreferredWidth( COL_WIDTH_SB_RES_CLASS  );
		
		return area;
	}
	
	/**
	 * Creates the about area containing version, author and general information.
	 * 
	 * @return the created area
	 */
	private Container createAboutArea() {
		// content
		JPanel area = new JPanel();
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.NONE;
		constraints.insets     = Laf.INSETS_NWE;
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 2;
		constraints.weightx    = 0;
		constraints.weighty    = 0;
		
		// spacer
		JLabel spacerTop = new JLabel("<html><br><br><br>");
		area.add(spacerTop, constraints);
		
		// logo
		ImageIcon logoIcon = new ImageIcon(ClassLoader.getSystemResource(logoPath));
		JLabel    logoLbl  = new JLabel(logoIcon);
		constraints.insets = Laf.INSETS_WE;
		constraints.gridy++;
		area.add(logoLbl, constraints);
		
		// add spacer
		constraints.gridwidth = 1;
		constraints.gridy++;
		JLabel spacerLogo = new JLabel(" ");
		area.add(spacerLogo, constraints);
		
		// version translation
		constraints.insets = Laf.INSETS_W;
		constraints.gridy++;
		constraints.anchor = GridBagConstraints.NORTHEAST;
		JLabel lblVersion  = new JLabel(Dict.get(Dict.VERSION) + ": ");
		Laf.makeBold(lblVersion);
		area.add(lblVersion, constraints);
		
		// version content
		constraints.insets = Laf.INSETS_E;
		constraints.gridx++;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		FlowLabel lblVersionContent = new FlowLabel(Midica.VERSION, CPL_ABOUT, PWIDTH_ABOUT);
		area.add(lblVersionContent, constraints);
		
		// timestamp translation
		constraints.insets = Laf.INSETS_W;
		constraints.gridx  = 0;
		constraints.gridy++;
		constraints.anchor = GridBagConstraints.NORTHEAST;
		JLabel lblTimestamp = new JLabel(Dict.get(Dict.DATE) + ": ");
		Laf.makeBold(lblTimestamp);
		area.add(lblTimestamp, constraints);
		
		// timestamp content
		constraints.insets = Laf.INSETS_E;
		constraints.gridx++;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		Date             timestamp = new Date(Midica.COMMIT_TIME * 1000L);
		SimpleDateFormat formatter = new SimpleDateFormat(Dict.get(Dict.TIMESTAMP_FORMAT));
		FlowLabel lblTimestampContent = new FlowLabel(formatter.format(timestamp), CPL_ABOUT, PWIDTH_ABOUT);
		area.add(lblTimestampContent, constraints);
		
		// author translation
		constraints.insets = Laf.INSETS_W;
		constraints.gridx  = 0;
		constraints.gridy++;
		constraints.anchor = GridBagConstraints.NORTHEAST;
		JLabel lblAuthor   = new JLabel(Dict.get(Dict.AUTHOR) + ": ");
		Laf.makeBold(lblAuthor);
		area.add(lblAuthor, constraints);
		
		// author content
		constraints.insets = Laf.INSETS_E;
		constraints.gridx++;
		constraints.anchor      = GridBagConstraints.NORTHWEST;
		FlowLabel lblAuthorContent = new FlowLabel(Midica.AUTHOR, CPL_ABOUT, PWIDTH_ABOUT);
		area.add(lblAuthorContent, constraints);
		
		// source URL translation
		constraints.insets = Laf.INSETS_W;
		constraints.gridx  = 0;
		constraints.gridy++;
		constraints.anchor = GridBagConstraints.NORTHEAST;
		JLabel lblSource   = new JLabel(Dict.get(Dict.SOURCE_URL) + ": ");
		Laf.makeBold(lblSource);
		area.add(lblSource, constraints);
		
		// source URL content
		constraints.insets = Laf.INSETS_E;
		constraints.gridx++;
		constraints.anchor      = GridBagConstraints.NORTHWEST;
		LinkLabel lblSourceContent = new LinkLabel(Midica.SOURCE_URL);
		area.add(lblSourceContent, constraints);
		
		// website translation
		constraints.insets = Laf.INSETS_SW;
		constraints.gridx  = 0;
		constraints.gridy++;
		constraints.anchor = GridBagConstraints.NORTHEAST;
		JLabel lblWebsite  = new JLabel(Dict.get(Dict.WEBSITE) + ": ");
		Laf.makeBold(lblWebsite);
		area.add(lblWebsite, constraints);
		
		// website content
		constraints.insets  = Laf.INSETS_SE;
		constraints.weighty = 1;
		constraints.gridx++;
		constraints.anchor       = GridBagConstraints.NORTHWEST;
		LinkLabel lblWebsiteContent = new LinkLabel(Midica.URL);
		area.add(lblWebsiteContent, constraints);
		
		return area;
	}
	
	/**
	 * Returns the widgets for the message filter.
	 * 
	 * @return widgets for the filter.
	 */
	public HashMap<String, JComponent> getMsgFilterWidgets() {
		return filterWidgets;
	}
	
	/**
	 * Returns the message table.
	 * 
	 * @return the message table.
	 */
	public MidicaTable getMsgTable() {
		return msgTable;
	}
	
	/**
	 * Returns the message tree.
	 * 
	 * @return the message tree.
	 */
	public MidicaTree getMsgTree() {
		return msgTree;
	}
	
	/**
	 * Creates and shows the info view passing the specified owner window to the constructor.
	 * 
	 * If an info view already exists it will be destroyed before.
	 * This is done in order to make sure that the newly created info view:
	 * 
	 * - exists only once
	 * - will be in the foreground
	 * - will be focused
	 * 
	 * @param owner  The GUI window owning the info window.
	 */
	public static void showInfoWindow(Window owner) {
		
		// it cannot be guaranteed that the info view can be focused
		// so we have to destroy and rebuild it
		if (null != infoView)
			infoView.close();
		infoView = new InfoView(owner);
	}
	
	/**
	 * Removes all contents from the message details area.
	 */
	public void cleanMsgDetails() {
		msgDetails.removeAll();
		msgDetails.setBackground(Laf.COLOR_MSG_DEFAULT);
		msgDetails.revalidate();
		msgDetails.repaint();
	}
	
	/**
	 * Removes all contents from the key binding details area.
	 */
	public void cleanKeyBindingDetails() {
		keyBindingDetails.removeAll();
		keyBindingDetails.setBackground(Laf.COLOR_KEYBINDING_DEFAULT);
		keyBindingDetails.revalidate();
		keyBindingDetails.repaint();
	}
	
	/**
	 * Fills the details area with the given content.
	 * Before calling this method, {@link #cleanMsgDetails()} should
	 * be called first.
	 * 
	 * Adjusts the background color according to the click source
	 * (message table or message tree)
	 *
	 * @param messageSource  The selected leaf node or table row object.
	 */
	public void fillMsgDetails(IMessageType messageSource) {
		
		// single message from the table or multiple messages from a table node?
		boolean fromTable = false;
		if (messageSource instanceof SingleMessage) {
			fromTable = true;
		}
		
		// adjust background color
		Color bgColor = Laf.COLOR_MSG_TREE;
		if (fromTable) {
			bgColor = Laf.COLOR_MSG_TABLE;
		}
		msgDetails.setBackground(bgColor);
		
		// adjust translations (singular or plural)
		String translTick     = Dict.get( Dict.MSG_DETAILS_TICK_PL      );
		String translTrack    = Dict.get( Dict.MSG_DETAILS_TRACK_PL     );
		String translChannel  = Dict.get( Dict.MSG_DETAILS_CHANNEL_PL   );
		String translDevId    = Dict.get( Dict.MSG_DETAILS_DEVICE_ID_PL );
		String translRpnByte  = Dict.get( Dict.MSG_DETAILS_RPN_BYTE_PL  );
		String translNrpnByte = Dict.get( Dict.MSG_DETAILS_NRPN_BYTE_PL );
		String translText     = Dict.get( Dict.MSG_DETAILS_TEXT_PL      );
		if (fromTable) {
			translTick     = Dict.get( Dict.MSG_DETAILS_TICK_SG      );
			translChannel  = Dict.get( Dict.MSG_DETAILS_CHANNEL_SG   );
			translTrack    = Dict.get( Dict.MSG_DETAILS_TRACK_SG     );
			translDevId    = Dict.get( Dict.MSG_DETAILS_DEVICE_ID_SG );
			translRpnByte  = Dict.get( Dict.MSG_DETAILS_RPN_BYTE_SG  );
			translNrpnByte = Dict.get( Dict.MSG_DETAILS_NRPN_BYTE_SG );
			translText     = Dict.get( Dict.MSG_DETAILS_TEXT_SG      );
		}
		
		// layout
		GridBagConstraints constrLeft = new GridBagConstraints();
		constrLeft.fill       = GridBagConstraints.NONE;
		constrLeft.insets     = Laf.INSETS_IN;
		constrLeft.gridx      = 0;
		constrLeft.gridy      = 0;
		constrLeft.gridheight = 1;
		constrLeft.gridwidth  = 1;
		constrLeft.weightx    = 0;
		constrLeft.weighty    = 0;
		constrLeft.anchor     = GridBagConstraints.NORTHEAST;
		GridBagConstraints constrRight = (GridBagConstraints) constrLeft.clone();
		constrRight.gridx++;
		constrRight.weightx = 1;
		constrRight.fill    = GridBagConstraints.HORIZONTAL;
		constrRight.anchor  = GridBagConstraints.NORTHWEST;
		
		// tick range
		String tickRange = messageSource.getRange(IMessageType.OPT_TICK);
		if (tickRange != null) {
			
			// label
			JLabel lblTicks = new JLabel(translTick);
			msgDetails.add(lblTicks, constrLeft);
			
			// content
			FlowLabel ticks = new FlowLabel(tickRange, CPL_MSG_DETAILS, PWIDTH_MSG_DETAIL_CONTENT);
			ticks.setBackground(bgColor);
			msgDetails.add(ticks, constrRight);
		}
		
		// message length
		String lengthRange = messageSource.getRange(IMessageType.OPT_LENGTH);
		if (lengthRange != null) {
			
			// label
			constrLeft.gridy++;
			JLabel lblLength = new JLabel(Dict.get(Dict.MSG_DETAILS_LENGTH));
			msgDetails.add(lblLength, constrLeft);
			
			// content
			constrRight.gridy++;
			FlowLabel length = new FlowLabel(lengthRange, CPL_MSG_DETAILS, PWIDTH_MSG_DETAIL_CONTENT);
			length.setBackground(bgColor);
			msgDetails.add(length, constrRight);
		}
		
		// status byte
		Object statusObj = messageSource.getOption(IMessageType.OPT_STATUS_BYTE);
		if (statusObj != null) {
			
			// label
			constrLeft.gridy++;
			JLabel lblStatus = new JLabel(Dict.get(Dict.MSG_DETAILS_STATUS_BYTE));
			msgDetails.add(lblStatus, constrLeft);
			
			// content
			constrRight.gridy++;
			String statusStr = "";
			if (statusObj instanceof String) {
				statusStr = (String) statusObj;
			}
			else {
				byte statusByte = (Byte) statusObj;
				statusStr       = String.format("%02X", statusByte);
			}
			FlowLabel status = new FlowLabel("0x" + statusStr, CPL_MSG_DETAILS, PWIDTH_MSG_DETAIL_CONTENT);
			status.setBackground(bgColor);
			msgDetails.add(status, constrRight);
		}
		
		// tracks
		String tracksStr = messageSource.getDistinctOptions(IMessageType.OPT_TRACK);
		if (tracksStr != null) {
			
			// label
			constrLeft.gridy++;
			JLabel lblTracks = new JLabel(translTrack);
			msgDetails.add(lblTracks, constrLeft);
			
			// content
			constrRight.gridy++;
			FlowLabel tracks = new FlowLabel(tracksStr, CPL_MSG_DETAILS, PWIDTH_MSG_DETAIL_CONTENT);
			tracks.setBackground(bgColor);
			msgDetails.add(tracks, constrRight);
		}
		
		// channels
		String channelsStr = messageSource.getDistinctOptions(IMessageType.OPT_CHANNEL);
		if (channelsStr != null) {
			
			// label
			constrLeft.gridy++;
			JLabel lblChannels = new JLabel(translChannel);
			msgDetails.add(lblChannels, constrLeft);
			
			// content
			constrRight.gridy++;
			FlowLabel channels = new FlowLabel(channelsStr, CPL_MSG_DETAILS, PWIDTH_MSG_DETAIL_CONTENT);
			channels.setBackground(bgColor);
			msgDetails.add(channels, constrRight);
		}
		
		// meta type
		Object metaTypeObj = messageSource.getOption(IMessageType.OPT_META_TYPE);
		if (metaTypeObj instanceof Integer) {
			
			// label
			constrLeft.gridy++;
			JLabel lblMetaType = new JLabel(Dict.get(Dict.MSG_DETAILS_META_TYPE));
			msgDetails.add(lblMetaType, constrLeft);
			
			// content
			constrRight.gridy++;
			int       metaTypeByte = (Integer) metaTypeObj;
			String    metaTypeStr  = String.format("%02X", metaTypeByte);
			FlowLabel metaType     = new FlowLabel("0x" + metaTypeStr, CPL_MSG_DETAILS, PWIDTH_MSG_DETAIL_CONTENT);
			metaType.setBackground(bgColor);
			msgDetails.add(metaType, constrRight);
		}
		
		// vendor - byte(s) and name
		Object vendorObj     = messageSource.getOption( IMessageType.OPT_VENDOR_ID   );
		Object vendorNameObj = messageSource.getOption( IMessageType.OPT_VENDOR_NAME );
		if (vendorObj instanceof String) {
			
			// label
			constrLeft.gridy++;
			JLabel lblVendor = new JLabel(Dict.get(Dict.MSG_DETAILS_VENDOR));
			msgDetails.add(lblVendor, constrLeft);
			
			// constrct vendor string
			String vendorStr = "0x" + (String) vendorObj; // ID
			if (vendorNameObj instanceof String) {
				vendorStr = vendorStr + " (" + ((String) vendorNameObj) + ")";
			}
			
			// content
			constrRight.gridy++;
			FlowLabel vendor = new FlowLabel(vendorStr, CPL_MSG_DETAILS, PWIDTH_MSG_DETAIL_CONTENT);
			vendor.setBackground(bgColor);
			msgDetails.add(vendor, constrRight);
		}
		
		// sysex channel
		Object sysexChannelObj = messageSource.getDistinctOptions(IMessageType.OPT_SYSEX_CHANNEL);
		if (sysexChannelObj instanceof String) {
			
			// label
			constrLeft.gridy++;
			JLabel lblSysExChannel = new JLabel(translDevId);
			msgDetails.add(lblSysExChannel, constrLeft);
			
			// contentnode
			constrRight.gridy++;
			String sysexChannelStr = (String) sysexChannelObj;
			FlowLabel sysExChannel = new FlowLabel(sysexChannelStr, CPL_MSG_DETAILS, PWIDTH_MSG_DETAIL_CONTENT);
			sysExChannel.setBackground(bgColor);
			msgDetails.add(sysExChannel, constrRight);
		}
		
		// sub ID 1 byte
		Object subId1Obj = messageSource.getOption(IMessageType.OPT_SUB_ID_1);
		if (subId1Obj instanceof String) {
			
			// label
			constrLeft.gridy++;
			JLabel lblSubId = new JLabel(Dict.get(Dict.MSG_DETAILS_SUB_ID_1));
			msgDetails.add(lblSubId, constrLeft);
			
			// content
			constrRight.gridy++;
			String subIdStr = (String) subId1Obj;
			FlowLabel subId = new FlowLabel(subIdStr, CPL_MSG_DETAILS, PWIDTH_MSG_DETAIL_CONTENT);
			subId.setBackground(bgColor);
			msgDetails.add(subId, constrRight);
		}
		
		// sub ID 2 byte
		Object subId2Obj = messageSource.getOption(IMessageType.OPT_SUB_ID_2);
		if (subId2Obj instanceof String) {
			
			// label
			constrLeft.gridy++;
			JLabel lblSubId = new JLabel(Dict.get(Dict.MSG_DETAILS_SUB_ID_2));
			msgDetails.add(lblSubId, constrLeft);
			
			// content
			constrRight.gridy++;
			String subIdStr = (String) subId2Obj;
			FlowLabel subId = new FlowLabel(subIdStr, CPL_MSG_DETAILS, PWIDTH_MSG_DETAIL_CONTENT);
			subId.setBackground(bgColor);
			msgDetails.add(subId, constrRight);
		}
		
		// controller byte
		Object ctrlObj = messageSource.getOption(IMessageType.OPT_CONTROLLER);
		if (ctrlObj instanceof Byte) {
			
			// label
			constrLeft.gridy++;
			JLabel lblCtrl = new JLabel(Dict.get(Dict.MSG_DETAILS_CTRL_BYTE));
			msgDetails.add(lblCtrl, constrLeft);
			
			// content
			constrRight.gridy++;
			byte   ctrlByte = (Byte) ctrlObj;
			String ctrlStr  = String.format("%02X", ctrlByte);
			FlowLabel ctrl  = new FlowLabel("0x" + ctrlStr, CPL_MSG_DETAILS, PWIDTH_MSG_DETAIL_CONTENT);
			ctrl.setBackground(bgColor);
			msgDetails.add(ctrl, constrRight);
		}
		
		// RPN - parameter number(s)
		Object rpnObj = messageSource.getDistinctOptions(IMessageType.OPT_RPN);
		if (rpnObj != null) {
			
			// label
			constrLeft.gridy++;
			JLabel lblRpn = new JLabel(translRpnByte);
			msgDetails.add(lblRpn, constrLeft);
			
			// content
			constrRight.gridy++;
			String rpnStr = (String) rpnObj;
			FlowLabel rpn = new FlowLabel("0x" + rpnStr, CPL_MSG_DETAILS, PWIDTH_MSG_DETAIL_CONTENT);
			rpn.setBackground(bgColor);
			msgDetails.add(rpn, constrRight);
		}
		
		// NRPN - parameter number(s)
		Object nrpnObj = messageSource.getDistinctOptions(IMessageType.OPT_NRPN);
		if (nrpnObj != null) {
			
			// label
			constrLeft.gridy++;
			JLabel lblNrpn = new JLabel(translNrpnByte);
			msgDetails.add(lblNrpn, constrLeft);
			
			// content
			constrRight.gridy++;
			String nrpnStr = (String) nrpnObj;
			FlowLabel nrpn = new FlowLabel("0x" + nrpnStr, CPL_MSG_DETAILS, PWIDTH_MSG_DETAIL_CONTENT);
			nrpn.setBackground(bgColor);
			msgDetails.add(nrpn, constrRight);
		}
		
		// text content
		Object textObj = messageSource.getDistinctOptions(IMessageType.OPT_TEXT, "\n============\n");
		if (textObj instanceof String) {
			
			// label
			constrLeft.gridy++;
			JLabel lblText = new JLabel(translText);
			msgDetails.add(lblText, constrLeft);
			
			// content
			constrRight.gridy++;
			String textStr = (String) textObj;
			FlowLabel text = new FlowLabel(textStr, CPL_MSG_DETAILS, PWIDTH_MSG_DETAIL_CONTENT);
			text.setBackground(bgColor);
			msgDetails.add(text, constrRight);
		}
		
		// complete message
		Object msgObj = messageSource.getOption(IMessageType.OPT_MESSAGE);
		if (msgObj != null) {

			// label
			constrLeft.gridy++;
			JLabel lblMsg = new JLabel(Dict.get(Dict.MSG_DETAILS_MESSAGE));
			msgDetails.add(lblMsg, constrLeft);

			// content
			constrRight.gridy++;
			byte[]        msgBytes = (byte[]) msgObj;
			StringBuilder msgStr   = new StringBuilder("");
			for (int i = 0; i < msgBytes.length; i++) {
				String hex = String.format("%02X", msgBytes[i]);
				if (0 == i)
					msgStr.append(hex);
				else
					msgStr.append(" " + hex);
			}
			FlowLabel msg = new FlowLabel(msgStr.toString(), CPL_MSG_DETAILS, PWIDTH_MSG_DETAIL_CONTENT);
			msg.setBackground(bgColor);
			msgDetails.add(msg, constrRight);
		}
		
		// message description
		String description = MessageClassifier.getDescription(messageSource)[0];
		if (description != null) {

			// label
			constrLeft.gridy++;
			JLabel lblMsg = new JLabel(Dict.get(Dict.MSG_DETAILS_DESCRIPTION));
			msgDetails.add(lblMsg, constrLeft);

			// content
			constrRight.gridy++;
			FlowLabel msg = new FlowLabel(description, CPL_MSG_DETAILS, PWIDTH_MSG_DETAIL_CONTENT);
			msg.setBackground(bgColor);
			msgDetails.add(msg, constrRight);
		}
		
		// spacer
		constrLeft.gridy++;
		constrLeft.weighty = 1;
		JLabel spacer = new JLabel(" ");
		msgDetails.add(spacer, constrLeft);
		
		// make the changes visible
		msgDetails.revalidate();
		msgDetails.repaint();
	}
	
	/**
	 * Fills the details area for a key binding action.
	 * This area contains:
	 * 
	 * - category and action description
	 * - a form to add a new key binding
	 * - the already configured key bindings for the action
	 * - a checkbox and button to reset the key bindings for this action
	 * 
	 * @param id        the ID of the key binding action
	 * @param category  the category description (the window containing the action)
	 */
	public void fillKeyBindingDetails(String id, String category) {
		
		// set background color
		keyBindingDetails.setBackground(Laf.COLOR_KEYBINDING_SELECTED);
		
		// reset the reset checkbox and the reset button
		resetResetWidgetsForSelectedKeyBindingAction();
		keybindingResetIdCbx.setEnabled(true);
		
		// layout
		GridBagConstraints constrLeft = new GridBagConstraints();
		constrLeft.fill       = GridBagConstraints.NONE;
		constrLeft.insets     = Laf.INSETS_IN;
		constrLeft.gridx      = 0;
		constrLeft.gridy      = 0;
		constrLeft.gridheight = 1;
		constrLeft.gridwidth  = 1;
		constrLeft.weightx    = 0;
		constrLeft.weighty    = 0;
		constrLeft.anchor     = GridBagConstraints.NORTHEAST;
		GridBagConstraints constrRight = (GridBagConstraints) constrLeft.clone();
		constrRight.gridx++;
		constrRight.weightx = 1;
		constrRight.fill    = GridBagConstraints.HORIZONTAL;
		constrRight.anchor  = GridBagConstraints.NORTHWEST;
		GridBagConstraints constrFull = (GridBagConstraints) constrLeft.clone();
		constrFull.gridwidth = 2;
		constrFull.fill      = GridBagConstraints.HORIZONTAL;
		constrFull.anchor    = GridBagConstraints.CENTER;
		
		// category
		JLabel catLbl = new JLabel(Dict.get(Dict.KB_CATEGORY));
		Laf.makeBold(catLbl);
		keyBindingDetails.add(catLbl, constrLeft);
		JLabel catValueLbl = new JLabel(category);
		keyBindingDetails.add(catValueLbl, constrRight);
		
		// description
		constrLeft.gridy++;
		JLabel descLbl = new JLabel(Dict.get(Dict.KB_ACTION));
		Laf.makeBold(descLbl);
		keyBindingDetails.add(descLbl, constrLeft);
		constrRight.gridy++;
		FlowLabel descValueLbl = new FlowLabel(Dict.get(id), CPL_KEYBINDING_DESC, PWIDTH_KEYBINDING_DESC);
		keyBindingDetails.add(descValueLbl, constrRight);
		
		// hint
		constrLeft.gridy++;
		JLabel hintLbl = new JLabel(Dict.get(Dict.KB_HINT));
		Laf.makeBold(hintLbl);
		hintLbl.setForeground(Laf.COLOR_HINT);
		keyBindingDetails.add(hintLbl, constrLeft);
		constrRight.gridy++;
		FlowLabel hintValueLbl = new FlowLabel(Dict.get(Dict.KB_HINT_TXT), CPL_KEYBINDING_DESC, PWIDTH_KEYBINDING_DESC);
		hintValueLbl.setForeground(Laf.COLOR_HINT);
		keyBindingDetails.add(hintValueLbl, constrRight);
		
		// add-new-key-binding area
		constrLeft.gridy++;
		constrRight.gridy++;
		constrFull.gridy  = constrLeft.gridy;
		constrLeft.anchor = GridBagConstraints.EAST;
		constrRight.fill  = GridBagConstraints.NONE;
		Container configForm = createKeyBindingForm();
		keyBindingDetails.add(configForm, constrFull);
		
		// configured bindings
		constrFull.gridy++;
		Container configuredBindings = createConfiguredBindingsArea(id);
		keyBindingDetails.add(configuredBindings, constrFull);
		
		// add spacer
		constrFull.gridy++;
		constrFull.weighty = 1;
		JLabel spacer = new JLabel(" ");
		keyBindingDetails.add(spacer, constrFull);
	}
	
	/**
	 * Resets the checkbox and button to reset the key bindings on the right side.
	 */
	public void resetResetWidgetsForSelectedKeyBindingAction() {
		keybindingResetIdCbx.setSelected(false);
		keybindingResetIdCbx.setEnabled(false);
		keybindingResetIdBtn.setEnabled(false);
	}
	
	/**
	 * Creates the form to add a new key binding.
	 * 
	 * @return the created form.
	 */
	private Container createKeyBindingForm() {
		JPanel area = new JPanel();
		area.setBackground(Laf.COLOR_KEYBINDING_SELECTED);
		area.setBorder(Laf.createTitledBorder(Dict.get(Dict.KB_ADD)));
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout(layout);
		GridBagConstraints constrLeft = new GridBagConstraints();
		constrLeft.fill       = GridBagConstraints.NONE;
		constrLeft.insets     = Laf.INSETS_ZERO;
		constrLeft.gridx      = 0;
		constrLeft.gridy      = 0;
		constrLeft.gridheight = 1;
		constrLeft.gridwidth  = 1;
		constrLeft.weightx    = 0;
		constrLeft.weighty    = 0;
		constrLeft.anchor     = GridBagConstraints.EAST;
		GridBagConstraints constrRight = (GridBagConstraints) constrLeft.clone();
		constrRight.gridx++;
		constrRight.weightx = 1;
		constrRight.fill    = GridBagConstraints.HORIZONTAL;
		constrRight.anchor  = GridBagConstraints.NORTHWEST;
		
		// label for text field
		JLabel fieldLbl = new JLabel(Dict.get(Dict.KB_ENTER));
		area.add(fieldLbl, constrLeft);
		
		// text field
		addKeyBindingFld.setText("");
		area.add(addKeyBindingFld, constrRight);
		
		// bindings for the text field
		for (KeyListener listener : addKeyBindingFld.getKeyListeners()) {
			addKeyBindingFld.removeKeyListener(listener);
		}
		addKeyBindingFld.addKeyListener(controller.createKeyListener(addKeyBindingFld));
		
		// add button
		constrRight.gridy++;
		area.add(keybindingAddBtn, constrRight);
		
		return area;
	}
	
	/**
	 * Creates the area with all configured key bindings for a certain action.
	 * Each key binding consists of a description of the key combination and a button
	 * to delete this key binding.
	 * 
	 * @param id  the ID of the key binding action
	 * @return the created area.
	 */
	private Container createConfiguredBindingsArea(String id) {
		JPanel area = new JPanel();
		area.setBackground(Laf.COLOR_KEYBINDING_SELECTED);
		area.setBorder(Laf.createTitledBorder(Dict.get(Dict.KB_CONFIGURED)));
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout(layout);
		GridBagConstraints constrLeft = new GridBagConstraints();
		constrLeft.fill       = GridBagConstraints.NONE;
		constrLeft.insets     = Laf.INSETS_IN;
		constrLeft.gridx      = 0;
		constrLeft.gridy      = 0;
		constrLeft.gridheight = 1;
		constrLeft.gridwidth  = 1;
		constrLeft.weightx    = 0;
		constrLeft.weighty    = 0;
		constrLeft.anchor     = GridBagConstraints.EAST;
		GridBagConstraints constrRight = (GridBagConstraints) constrLeft.clone();
		constrRight.gridx++;
		constrRight.weightx = 1;
		constrRight.fill    = GridBagConstraints.NONE;
		constrRight.anchor  = GridBagConstraints.WEST;
		GridBagConstraints constrFull = (GridBagConstraints) constrLeft.clone();
		constrFull.gridwidth = 2;
		constrFull.fill      = GridBagConstraints.HORIZONTAL;
		constrFull.anchor    = GridBagConstraints.CENTER;
		
		// add all configured bindings
		int i = 0;
		TreeSet<KeyBinding> bindings = Config.getKeyBindings(id);
		for (KeyBinding binding : bindings) {
			
			// add separator
			if (i > 0) {
				constrRight.gridy++;
				constrLeft.gridy++;
				constrFull.gridy = constrLeft.gridy;
				area.add(Laf.createSeparator(), constrFull);
			}
			
			// add binding
			constrLeft.gridy++;
			JLabel bindingLbl = new JLabel(binding.getDescription());
			area.add(bindingLbl, constrLeft);
			
			// button to remove the old binding
			constrRight.gridy++;
			MidicaButton removeBtn = new MidicaButton(Dict.get(Dict.KB_REMOVE));
			area.add(removeBtn, constrRight);
			removeBtn.setActionCommand(InfoController.CMD_REMOVE_KEY_BINDING);
			removeBtn.setName(binding.toString());
			removeBtn.addActionListener(controller);
			
			i++;
		}
		
		return area;
	}
	
	/**
	 * Closes and destroys the info window.
	 */
	public void close() {
		setVisible(false);
		dispose();
		infoView = null;
	}
	
	/**
	 * Adds key bindings to the info window.
	 */
	public void addKeyBindings() {
		
		// reset everything
		keyBindingManager = new KeyBindingManager(this, this.getRootPane());
		
		// close bindings
		keyBindingManager.addBindingsForClose(Dict.KEY_INFO_CLOSE);
		
		// level-1 tabs
		keyBindingManager.addBindingsForTabLevel1( content, Dict.KEY_INFO_CONF,        0 );
		keyBindingManager.addBindingsForTabLevel1( content, Dict.KEY_INFO_SB,          1 );
		keyBindingManager.addBindingsForTabLevel1( content, Dict.KEY_INFO_MIDI,        2 );
		keyBindingManager.addBindingsForTabLevel1( content, Dict.KEY_INFO_KEYBINDINGS, 3 );
		keyBindingManager.addBindingsForTabLevel1( content, Dict.KEY_INFO_ABOUT,       4 );
		
		// level-2 tabs (config)
		keyBindingManager.addBindingsForTabLevel2( contentConfig, Dict.KEY_INFO_CONF_NOTE,    0, 0 );
		keyBindingManager.addBindingsForTabLevel2( contentConfig, Dict.KEY_INFO_CONF_PERC,    0, 1 );
		keyBindingManager.addBindingsForTabLevel2( contentConfig, Dict.KEY_INFO_CONF_SYNTAX,  0, 2 );
		keyBindingManager.addBindingsForTabLevel2( contentConfig, Dict.KEY_INFO_CONF_INSTR,   0, 3 );
		keyBindingManager.addBindingsForTabLevel2( contentConfig, Dict.KEY_INFO_CONF_DRUMKIT, 0, 4 );
		
		// level-2 tabs (soundbank)
		keyBindingManager.addBindingsForTabLevel2( contentSoundbank, Dict.KEY_INFO_SB_GENERAL, 1, 0 );
		keyBindingManager.addBindingsForTabLevel2( contentSoundbank, Dict.KEY_INFO_SB_INSTR,   1, 1 );
		keyBindingManager.addBindingsForTabLevel2( contentSoundbank, Dict.KEY_INFO_SB_RES,     1, 2 );
		
		// level-2 tabs (midi)
		keyBindingManager.addBindingsForTabLevel2( contentMidi, Dict.KEY_INFO_MIDI_GENERAL, 2, 0 );
		keyBindingManager.addBindingsForTabLevel2( contentMidi, Dict.KEY_INFO_MIDI_KARAOKE, 2, 1 );
		keyBindingManager.addBindingsForTabLevel2( contentMidi, Dict.KEY_INFO_MIDI_BANKS,   2, 2 );
		keyBindingManager.addBindingsForTabLevel2( contentMidi, Dict.KEY_INFO_MIDI_MSG,     2, 3 );
		
		// level-3: config tables / string filters
		keyBindingManager.addBindingsForTabLevel3( tableStringFilterIcons.get(Dict.KEY_INFO_CONF_NOTE_FILTER),    Dict.KEY_INFO_CONF_NOTE_FILTER    );
		keyBindingManager.addBindingsForTabLevel3( tableStringFilterIcons.get(Dict.KEY_INFO_CONF_PERC_FILTER),    Dict.KEY_INFO_CONF_PERC_FILTER    );
		keyBindingManager.addBindingsForTabLevel3( tableStringFilterIcons.get(Dict.KEY_INFO_CONF_SYNTAX_FILTER),  Dict.KEY_INFO_CONF_SYNTAX_FILTER  );
		keyBindingManager.addBindingsForTabLevel3( tableStringFilterIcons.get(Dict.KEY_INFO_CONF_INSTR_FILTER),   Dict.KEY_INFO_CONF_INSTR_FILTER   );
		keyBindingManager.addBindingsForTabLevel3( tableStringFilterIcons.get(Dict.KEY_INFO_CONF_DRUMKIT_FILTER), Dict.KEY_INFO_CONF_DRUMKIT_FILTER );
		
		// level-3: soundbank tables / string filters
		keyBindingManager.addBindingsForTabLevel3( tableStringFilterIcons.get(Dict.KEY_INFO_SB_INSTR_FILTER), Dict.KEY_INFO_SB_INSTR_FILTER );
		keyBindingManager.addBindingsForTabLevel3( tableStringFilterIcons.get(Dict.KEY_INFO_SB_RES_FILTER),   Dict.KEY_INFO_SB_RES_FILTER   );
		
		// level-3: midi / banks
		keyBindingManager.addBindingsForTabLevel3( expandCollapseButtons.get(0), Dict.KEY_INFO_MIDI_BANKS_TOT_MIN  );
		keyBindingManager.addBindingsForTabLevel3( expandCollapseButtons.get(1), Dict.KEY_INFO_MIDI_BANKS_TOT_PL   );
		keyBindingManager.addBindingsForTabLevel3( bankTotalTree,                Dict.KEY_INFO_MIDI_BANKS_TOT_TREE );
		keyBindingManager.addBindingsForTabLevel3( expandCollapseButtons.get(2), Dict.KEY_INFO_MIDI_BANKS_CH_MIN   );
		keyBindingManager.addBindingsForTabLevel3( expandCollapseButtons.get(3), Dict.KEY_INFO_MIDI_BANKS_CH_PL    );
		keyBindingManager.addBindingsForTabLevel3( bankChannelTree,              Dict.KEY_INFO_MIDI_BANKS_CH_TREE  );
		
		// level-3: midi / messages
		keyBindingManager.addBindingsForTabLevel3( filterWidgets.get(FILTER_ICON),                 Dict.KEY_INFO_MIDI_MSG_FILTER     );
		keyBindingManager.addBindingsForTabLevel3( expandCollapseButtons.get(4),                   Dict.KEY_INFO_MIDI_MSG_MIN        );
		keyBindingManager.addBindingsForTabLevel3( expandCollapseButtons.get(5),                   Dict.KEY_INFO_MIDI_MSG_PL         );
		keyBindingManager.addBindingsForTabLevel3( msgTree,                                        Dict.KEY_INFO_MIDI_MSG_TREE       );
		keyBindingManager.addBindingsForTabLevel3( msgTable,                                       Dict.KEY_INFO_MIDI_MSG_TABLE      );
		keyBindingManager.addBindingsForTabLevel3( filterWidgets.get(FILTER_CBX_CHAN_INDEP),       Dict.KEY_INFO_MIDI_MSG_CH_INDEP   );
		keyBindingManager.addBindingsForTabLevel3( filterWidgets.get(FILTER_CBX_CHAN_DEP),         Dict.KEY_INFO_MIDI_MSG_CH_DEP     );
		keyBindingManager.addBindingsForTabLevel3( filterWidgets.get(FILTER_CBX_NODE),             Dict.KEY_INFO_MIDI_MSG_SEL_NOD    );
		keyBindingManager.addBindingsForTabLevel3( filterWidgets.get(FILTER_CBX_LIMIT_TICKS),      Dict.KEY_INFO_MIDI_MSG_LIM_TCK    );
		keyBindingManager.addBindingsForTabLevel3( filterWidgets.get(FILTER_TXT_FROM_TICKS),       Dict.KEY_INFO_MIDI_MSG_TICK_FROM  );
		keyBindingManager.addBindingsForTabLevel3( filterWidgets.get(FILTER_TXT_TO_TICKS),         Dict.KEY_INFO_MIDI_MSG_TICK_TO    );
		keyBindingManager.addBindingsForTabLevel3( filterWidgets.get(FILTER_CBX_LIMIT_TRACKS),     Dict.KEY_INFO_MIDI_MSG_LIM_TRK    );
		keyBindingManager.addBindingsForTabLevel3( filterWidgets.get(FILTER_TXT_TRACKS),           Dict.KEY_INFO_MIDI_MSG_TRACKS_TXT );
		keyBindingManager.addBindingsForTabLevel3( filterWidgets.get(FILTER_BTN_SHOW_TREE),        Dict.KEY_INFO_MIDI_MSG_SHOW_IN_TR );
		keyBindingManager.addBindingsForTabLevel3( filterWidgets.get(FILTER_CBX_AUTO_SHOW),        Dict.KEY_INFO_MIDI_MSG_SHOW_AUTO  );
		keyBindingManager.addBindingsForTabLevel3( filterWidgets.get(FILTER_CBX_CHAN_PREFIX +  0), Dict.KEY_INFO_MIDI_MSG_CH_00      );
		keyBindingManager.addBindingsForTabLevel3( filterWidgets.get(FILTER_CBX_CHAN_PREFIX +  1), Dict.KEY_INFO_MIDI_MSG_CH_01      );
		keyBindingManager.addBindingsForTabLevel3( filterWidgets.get(FILTER_CBX_CHAN_PREFIX +  2), Dict.KEY_INFO_MIDI_MSG_CH_02      );
		keyBindingManager.addBindingsForTabLevel3( filterWidgets.get(FILTER_CBX_CHAN_PREFIX +  3), Dict.KEY_INFO_MIDI_MSG_CH_03      );
		keyBindingManager.addBindingsForTabLevel3( filterWidgets.get(FILTER_CBX_CHAN_PREFIX +  4), Dict.KEY_INFO_MIDI_MSG_CH_04      );
		keyBindingManager.addBindingsForTabLevel3( filterWidgets.get(FILTER_CBX_CHAN_PREFIX +  5), Dict.KEY_INFO_MIDI_MSG_CH_05      );
		keyBindingManager.addBindingsForTabLevel3( filterWidgets.get(FILTER_CBX_CHAN_PREFIX +  6), Dict.KEY_INFO_MIDI_MSG_CH_06      );
		keyBindingManager.addBindingsForTabLevel3( filterWidgets.get(FILTER_CBX_CHAN_PREFIX +  7), Dict.KEY_INFO_MIDI_MSG_CH_07      );
		keyBindingManager.addBindingsForTabLevel3( filterWidgets.get(FILTER_CBX_CHAN_PREFIX +  8), Dict.KEY_INFO_MIDI_MSG_CH_08      );
		keyBindingManager.addBindingsForTabLevel3( filterWidgets.get(FILTER_CBX_CHAN_PREFIX +  9), Dict.KEY_INFO_MIDI_MSG_CH_09      );
		keyBindingManager.addBindingsForTabLevel3( filterWidgets.get(FILTER_CBX_CHAN_PREFIX + 10), Dict.KEY_INFO_MIDI_MSG_CH_10      );
		keyBindingManager.addBindingsForTabLevel3( filterWidgets.get(FILTER_CBX_CHAN_PREFIX + 11), Dict.KEY_INFO_MIDI_MSG_CH_11      );
		keyBindingManager.addBindingsForTabLevel3( filterWidgets.get(FILTER_CBX_CHAN_PREFIX + 12), Dict.KEY_INFO_MIDI_MSG_CH_12      );
		keyBindingManager.addBindingsForTabLevel3( filterWidgets.get(FILTER_CBX_CHAN_PREFIX + 13), Dict.KEY_INFO_MIDI_MSG_CH_13      );
		keyBindingManager.addBindingsForTabLevel3( filterWidgets.get(FILTER_CBX_CHAN_PREFIX + 14), Dict.KEY_INFO_MIDI_MSG_CH_14      );
		keyBindingManager.addBindingsForTabLevel3( filterWidgets.get(FILTER_CBX_CHAN_PREFIX + 15), Dict.KEY_INFO_MIDI_MSG_CH_15      );
		
		// level-3 (keybindings)
		keyBindingManager.addBindingsForTabLevel3( keyBindingTree,               Dict.KEY_INFO_KEY_TREE           );
		keyBindingManager.addBindingsForTabLevel3( expandCollapseButtons.get(7), Dict.KEY_INFO_KEY_PL             );
		keyBindingManager.addBindingsForTabLevel3( expandCollapseButtons.get(6), Dict.KEY_INFO_KEY_MIN            );
		keyBindingManager.addBindingsForTabLevel3( addKeyBindingFld,             Dict.KEY_INFO_KEY_FLD            );
		keyBindingManager.addBindingsForTabLevel3( keybindingTreeFilter,         Dict.KEY_INFO_KEY_FILTER         );
		keyBindingManager.addBindingsForTabLevel3( keybindingAddBtn,             Dict.KEY_INFO_KEY_ADD_BTN        );
		keyBindingManager.addBindingsForTabLevel3( keybindingResetIdCbx,         Dict.KEY_INFO_KEY_RESET_ID_CBX   );
		keyBindingManager.addBindingsForTabLevel3( keybindingResetIdBtn,         Dict.KEY_INFO_KEY_RESET_ID_BTN   );
		keyBindingManager.addBindingsForTabLevel3( keybindingResetGlobCbx,       Dict.KEY_INFO_KEY_RESET_GLOB_CBX );
		keyBindingManager.addBindingsForTabLevel3( keybindingResetGlobBtn,       Dict.KEY_INFO_KEY_RESET_GLOB_BTN );
		
		// set input and action maps
		keyBindingManager.postprocess();
	}
}
