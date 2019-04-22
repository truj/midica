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
import java.awt.Insets;
import java.awt.KeyEventPostProcessor;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.swing.JButton;
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
import javax.swing.tree.DefaultTreeCellRenderer;

import org.midica.Midica;
import org.midica.config.Config;
import org.midica.config.Dict;
import org.midica.file.SequenceParser;
import org.midica.file.SoundfontParser;
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
import org.midica.ui.model.SoundfontInstrumentsTableModel;
import org.midica.ui.model.SoundfontResourceTableModel;
import org.midica.ui.model.SyntaxTableModel;
import org.midica.ui.renderer.InstrumentTableCellRenderer;
import org.midica.ui.renderer.MessageTableCellRenderer;
import org.midica.ui.renderer.MidicaTableCellRenderer;
import org.midica.ui.renderer.SoundfontInstrumentTableCellRenderer;
import org.midica.ui.renderer.SoundfontResourceTableCellRenderer;
import org.midica.ui.renderer.SyntaxTableCellRenderer;
import org.midica.ui.widget.FlowLabel;
import org.midica.ui.widget.MidicaSplitPane;
import org.midica.ui.widget.MidicaTable;
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
 * - Information about the currently loaded soundfont
 *     - General information
 *     - Drum kits and Instruments
 *     - Resources
 * - Information about the currently loaded MIDI sequence.
 *     - General information
 *     - Banks, Instruments and Notes
 *     - MIDI Messages
 * - General Midica version and build information.
 * 
 * @author Jan Trukenmüller
 */
public class InfoView extends JDialog {
	
	private static final long serialVersionUID = 1L;
	
	// widths and heights, used for dimensions
	private static final int COL_WIDTH_NOTE_NUM          =  60;
	private static final int COL_WIDTH_NOTE_NAME         = 240;
	private static final int COL_WIDTH_PERC_NUM          =  60;
	private static final int COL_WIDTH_PERC_ID_SHORT     =  80;
	private static final int COL_WIDTH_PERC_ID_LONG      = 250;
	private static final int COL_WIDTH_SYNTAX_NAME       = 180;
	private static final int COL_WIDTH_SYNTAX_DESC       = 230;
	private static final int COL_WIDTH_SYNTAX_KEYWORD    = 130;
	private static final int COL_WIDTH_INSTR_NUM         =  60;
	private static final int COL_WIDTH_INSTR_NAME        = 300;
	private static final int COL_WIDTH_DRUMKIT_NUM       =  60;
	private static final int COL_WIDTH_DRUMKIT_NAME      = 200;
	private static final int COL_WIDTH_SF_INSTR_PROGRAM  =  60;
	private static final int COL_WIDTH_SF_INSTR_BANK     =  80;
	private static final int COL_WIDTH_SF_INSTR_NAME     = 300;
	private static final int COL_WIDTH_SF_INSTR_CHANNELS = 100;
	private static final int COL_WIDTH_SF_INSTR_KEYS     = 120;
	private static final int COL_WIDTH_SF_RES_INDEX      =  45;
	private static final int COL_WIDTH_SF_RES_TYPE       =  55;
	private static final int COL_WIDTH_SF_RES_NAME       = 130;
	private static final int COL_WIDTH_SF_RES_FRAMES     =  60;
	private static final int COL_WIDTH_SF_RES_FORMAT     = 260;
	private static final int COL_WIDTH_SF_RES_CLASS      = 130;
	private static final int COL_WIDTH_MSG_TICK          =  80;
	private static final int COL_WIDTH_MSG_STATUS        =  40;
	private static final int COL_WIDTH_MSG_TRACK         =  25;
	private static final int COL_WIDTH_MSG_CHANNEL       =  25;
	private static final int COL_WIDTH_MSG_LENGTH        =  35;
	private static final int COL_WIDTH_MSG_TYPE          = 500;
	private static final int TABLE_HEIGHT                = 400;
	private static final int MSG_TABLE_PREF_HEIGHT       = 150;
	private static final int COLLAPSE_EXPAND_WIDTH       =  25;
	private static final int COLLAPSE_EXPAND_HEIGHT      =  25;
	private static final int TICK_RANGE_FILTER_WIDTH     =  70; // tick text fields in the message filter
	private static final int TRACK_RANGE_FILTER_WIDTH    =  90; // track text fields in the message filter
	private static final int FILTER_BUTTON_HEIGHT        =  15;
	
	// Fake widths and heights, used for dimensions. The real sizes are
	// determined by the layout manager. But for some reasons,
	// setPreferredSize() is anyway necessary for correct display.
	private static final int MSG_TREE_PREF_WIDTH     = 1;
	private static final int MSG_TREE_PREF_HEIGHT    = 1;
	private static final int MSG_DETAILS_PREF_WIDTH  = 1;
	private static final int MSG_DETAILS_PREF_HEIGHT = 1;
	private static final int MAX_HEIGHT_LYRICS       = 1; // max height
	
	// width/height constants for FlowLabels
	private static final int CPL_MIDI_INFO             =  73; // CPL: characters per line
	private static final int CPL_SOUNDFONT_INFO        =  73;
	private static final int CPL_MSG_DETAILS           =  28;
	private static final int PWIDTH_GENERAL_INFO_VALUE = 500; // PWIDTH: preferred width
	private static final int PWIDTH_MSG_DETAIL_CONTENT = 170;
	private static final int MAX_HEIGHT_SOUNDFONT_DESC = 155; // max height
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
	
	// dimensions (defined later)
	private static Dimension noteTableDim       = null;
	private static Dimension percTableDim       = null;
	private static Dimension syntaxTableDim     = null;
	private static Dimension instrTableDim      = null;
	private static Dimension drumkitTableDim    = null;
	private static Dimension sfInstrTableDim    = null;
	private static Dimension sfResourceTableDim = null;
	private static Dimension msgTreeDim         = null;
	private static Dimension msgDetailsDim      = null;
	private static Dimension msgTableDim        = null;
	private static Dimension collapseExpandDim  = null;
	
	private static int collapseExpandHeadlineHeight = 0; // will be filled dynamically
	
	private static InfoView infoView = null;
	
	private InfoController        controller       = null;
	private KeyEventPostProcessor keyProcessor     = null;
	private JTabbedPane           content          = null;
	private JTabbedPane           contentConfig    = null;
	private JTabbedPane           contentSoundfont = null;
	
	// widgets
	private MidicaTree                  msgTree       = null;
	private JPanel                      msgDetails    = null;
	private MidicaTable                 msgTable      = null;
	private HashMap<String, JComponent> filterWidgets = null;
	
	/**
	 * Creates a new info view window and sets the given owner Dialog.
	 * 
	 * @param owner  window to be set as the info view's owner
	 */
	private InfoView(Window owner) {
		super(owner);
		setTitle( Dict.get(Dict.TITLE_INFO_VIEW) );
		
		// initialize table dimensions
		int noteWidth       = COL_WIDTH_NOTE_NUM + COL_WIDTH_NOTE_NAME;
		int percWidth       = COL_WIDTH_PERC_NUM + COL_WIDTH_PERC_ID_SHORT
							+ COL_WIDTH_PERC_ID_LONG;
		int syntaxWidth     = COL_WIDTH_SYNTAX_NAME + COL_WIDTH_SYNTAX_KEYWORD
		                    + COL_WIDTH_SYNTAX_DESC;
		int instrWidth      = COL_WIDTH_INSTR_NUM        + COL_WIDTH_INSTR_NAME;
		int drumkitWidth    = COL_WIDTH_DRUMKIT_NUM      + COL_WIDTH_DRUMKIT_NAME;
		int sfInstrWidth    = COL_WIDTH_SF_INSTR_PROGRAM + COL_WIDTH_SF_INSTR_BANK
		                    + COL_WIDTH_SF_INSTR_NAME    + COL_WIDTH_SF_INSTR_CHANNELS
		                    + COL_WIDTH_SF_INSTR_KEYS;
		int sfResourceWidth = COL_WIDTH_SF_RES_INDEX  + COL_WIDTH_SF_RES_TYPE
		                    + COL_WIDTH_SF_RES_NAME   + COL_WIDTH_SF_RES_FRAMES
		                    + COL_WIDTH_SF_RES_FORMAT + COL_WIDTH_SF_RES_CLASS;
		int msgTableWidth   = COL_WIDTH_MSG_TICK    + COL_WIDTH_MSG_STATUS
		                    + COL_WIDTH_MSG_LENGTH  + COL_WIDTH_MSG_TRACK
		                    + COL_WIDTH_MSG_CHANNEL + COL_WIDTH_MSG_TYPE;
		noteTableDim       = new Dimension( noteWidth,       TABLE_HEIGHT          );
		percTableDim       = new Dimension( percWidth,       TABLE_HEIGHT          );
		syntaxTableDim     = new Dimension( syntaxWidth,     TABLE_HEIGHT          );
		instrTableDim      = new Dimension( instrWidth,      TABLE_HEIGHT          );
		drumkitTableDim    = new Dimension( drumkitWidth,    TABLE_HEIGHT          );
		sfInstrTableDim    = new Dimension( sfInstrWidth,    TABLE_HEIGHT          );
		sfResourceTableDim = new Dimension( sfResourceWidth, TABLE_HEIGHT          );
		msgTableDim        = new Dimension( msgTableWidth,   MSG_TABLE_PREF_HEIGHT );
		
		// initialize dimensions for trees, collapse-all/expand-all buttons and message details
		msgTreeDim        = new Dimension( MSG_TREE_PREF_WIDTH,    MSG_TREE_PREF_HEIGHT    );
		msgDetailsDim     = new Dimension( MSG_DETAILS_PREF_WIDTH, MSG_DETAILS_PREF_HEIGHT );
		collapseExpandDim = new Dimension( COLLAPSE_EXPAND_WIDTH,  COLLAPSE_EXPAND_HEIGHT  );
		
		// create content
		init();
		
		// show everything
		pack();
		setVisible( true );
	}
	
	/**
	 * Initializes the content of all the tabs inside the info view.
	 */
	private void init() {
		// content
		content = new JTabbedPane( JTabbedPane.LEFT );
		getContentPane().add( content );
		
		// enable key bindings
		this.controller = new InfoController( this );
		addWindowListener( this.controller );
		
		// add tabs
		content.addTab( Dict.get(Dict.TAB_CONFIG),        createConfigArea()       );
		content.addTab( Dict.get(Dict.TAB_SOUNDFONT),     createSoundfontArea()    );
		content.addTab( Dict.get(Dict.TAB_MIDI_SEQUENCE), createMidiSequenceArea() );
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
		contentConfig = new JTabbedPane( JTabbedPane.TOP );
		
		// add tabs
		contentConfig.addTab( Dict.get(Dict.TAB_NOTE_DETAILS),       createNoteArea()       );
		contentConfig.addTab( Dict.get(Dict.TAB_PERCUSSION_DETAILS), createPercussionArea() );
		contentConfig.addTab( Dict.get(Dict.SYNTAX),                 createSyntaxArea()     );
		contentConfig.addTab( Dict.get(Dict.INSTRUMENT_IDS),         createInstrumentArea() );
		contentConfig.addTab( Dict.get(Dict.DRUMKIT_IDS),            createDrumkitArea()    );
		
		return contentConfig;
	}
	
	/**
	 * Creates the soundfont tab.
	 * This contains the following sub tabs:
	 * 
	 * - General soundfont info
	 * - Instruments and drum kits
	 * - Resources
	 * 
	 * @return the created soundfont area.
	 */
	private Container createSoundfontArea() {
		// content
		contentSoundfont = new JTabbedPane( JTabbedPane.TOP );
		
		// add tabs
		contentSoundfont.addTab( Dict.get(Dict.TAB_SOUNDFONT_INFO),        createSoundfontInfoArea()       );
		contentSoundfont.addTab( Dict.get(Dict.TAB_SOUNDFONT_INSTRUMENTS), createSoundfontInstrumentArea() );
		contentSoundfont.addTab( Dict.get(Dict.TAB_SOUNDFONT_RESOURCES),   createSoundfontResourceArea()   );
		
		return contentSoundfont;
	}
	
	/**
	 * Creates the MIDI sequence tab.
	 * This contains the following sub tabs:
	 * 
	 * - General sequence info
	 * - Used banks, instruments and played notes
	 * - MIDI Messages (containing tree, details and table)
	 * 
	 * @return the created soundfont area.
	 */
	private Container createMidiSequenceArea() {
		// content
		contentSoundfont = new JTabbedPane( JTabbedPane.TOP );
		
		// add tabs
		contentSoundfont.addTab( Dict.get(Dict.TAB_MIDI_SEQUENCE_INFO), createMidiSequenceInfoArea() );
		contentSoundfont.addTab( Dict.get(Dict.TAB_MIDI_KARAOKE),       createKaraokeArea()          );
		contentSoundfont.addTab( Dict.get(Dict.TAB_BANK_INSTR_NOTE),    createBankInstrNoteArea()    );
		contentSoundfont.addTab( Dict.get(Dict.TAB_MESSAGES),           createMsgArea()              );
		
		return contentSoundfont;
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
		area.setLayout( layout );
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill   = GridBagConstraints.NONE;
		constraints.insets = new Insets( 2, 2, 2, 2 );
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 1;
		constraints.weighty    = 0;
		
		// label
		JLabel label = new JLabel( Dict.get(Dict.TAB_NOTE_DETAILS) );
		area.add( label, constraints );
		
		// table
		constraints.fill    = GridBagConstraints.VERTICAL;
		constraints.weighty = 1;
		constraints.gridy++;
		MidicaTable table = new MidicaTable();
		table.setModel( new NoteTableModel() );
		table.setDefaultRenderer( Object.class, new MidicaTableCellRenderer() );
		JScrollPane scroll = new JScrollPane( table );
		scroll.setPreferredSize( noteTableDim );
		area.add( scroll, constraints );
		
		// set column sizes and colors
		table.getColumnModel().getColumn( 0 ).setPreferredWidth( COL_WIDTH_NOTE_NUM  );
		table.getColumnModel().getColumn( 1 ).setPreferredWidth( COL_WIDTH_NOTE_NAME );
		
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
		area.setLayout( layout );
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill   = GridBagConstraints.NONE;
		constraints.insets = new Insets( 2, 2, 2, 2 );
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 1;
		constraints.weighty    = 0;
		
		// label
		JLabel label = new JLabel( Dict.get(Dict.TAB_PERCUSSION_DETAILS) );
		area.add( label, constraints );
		
		// table
		constraints.fill    = GridBagConstraints.VERTICAL;
		constraints.weighty = 1;
		constraints.gridy++;
		MidicaTable table = new MidicaTable();
		table.setModel( new PercussionTableModel() );
		table.setDefaultRenderer( Object.class, new MidicaTableCellRenderer() );
		JScrollPane scroll = new JScrollPane( table );
		scroll.setPreferredSize( percTableDim );
		area.add( scroll, constraints );
		
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
		area.setLayout( layout );
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill   = GridBagConstraints.NONE;
		constraints.insets = new Insets( 2, 2, 2, 2 );
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 1;
		constraints.weighty    = 0;
		
		// label
		JLabel label = new JLabel( Dict.get(Dict.SYNTAX) );
		area.add( label, constraints );
		
		// table
		constraints.fill    = GridBagConstraints.BOTH;
		constraints.weighty = 1;
		constraints.gridy++;
		MidicaTable table = new MidicaTable();
		table.setModel( new SyntaxTableModel() );
		table.setDefaultRenderer( Object.class, new SyntaxTableCellRenderer() );
		JScrollPane scroll = new JScrollPane( table );
		scroll.setPreferredSize( syntaxTableDim );
		area.add( scroll, constraints );
		
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
		area.setLayout( layout );
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill   = GridBagConstraints.NONE;
		constraints.insets = new Insets( 2, 2, 2, 2 );
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 1;
		constraints.weighty    = 0;
		
		// label
		JLabel label = new JLabel( Dict.get(Dict.INSTRUMENT_IDS) );
		area.add( label, constraints );
		
		// table
		constraints.fill    = GridBagConstraints.VERTICAL;
		constraints.weighty = 1;
		constraints.gridy++;
		MidicaTable table = new MidicaTable();
		table.setModel( new InstrumentTableModel() );
		table.setDefaultRenderer( Object.class, new InstrumentTableCellRenderer() );
		JScrollPane scroll = new JScrollPane( table );
		scroll.setPreferredSize( instrTableDim );
		area.add( scroll, constraints );
		
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
		area.setLayout( layout );
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill   = GridBagConstraints.NONE;
		constraints.insets = new Insets( 2, 2, 2, 2 );
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 1;
		constraints.weighty    = 0;
		
		// label
		JLabel label = new JLabel( Dict.get(Dict.DRUMKIT_IDS) );
		area.add( label, constraints );
		
		// table
		constraints.fill    = GridBagConstraints.VERTICAL;
		constraints.weighty = 1;
		constraints.gridy++;
		MidicaTable table = new MidicaTable();
		table.setModel( new DrumkitTableModel() );
		table.setDefaultRenderer( Object.class, new MidicaTableCellRenderer() );
		JScrollPane scroll = new JScrollPane( table );
		scroll.setPreferredSize( drumkitTableDim );
		area.add( scroll, constraints );
		
		// set column sizes
		table.getColumnModel().getColumn( 0 ).setPreferredWidth( COL_WIDTH_DRUMKIT_NUM  );
		table.getColumnModel().getColumn( 1 ).setPreferredWidth( COL_WIDTH_DRUMKIT_NAME );
		
		return area;
	}
	
	/**
	 * Creates the area for general soundfont information.
	 * 
	 * @return the created soundfont info area.
	 */
	private Container createSoundfontInfoArea() {
		// content
		JPanel area = new JPanel();
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout( layout );
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.NONE;
		constraints.insets     = new Insets( 2, 2, 2, 2 );
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 0;
		constraints.weighty    = 0;
		
		// get general soundfont info
		HashMap<String, String> soundfontInfo = SoundfontParser.getSoundfontInfo();
		
		// file translation
		constraints.anchor = GridBagConstraints.NORTHEAST;
		JLabel lblFile     = new JLabel( Dict.get(Dict.FILE) + ": " );
		area.add( lblFile, constraints );
		
		// file name
		constraints.gridx++;
		constraints.anchor  = GridBagConstraints.NORTHWEST;
		constraints.weightx = 1;
		String fileName     = SoundfontParser.getFileName();
		String filePath     = SoundfontParser.getFilePath();
		if ( null == fileName || null == filePath ) {
			fileName = Dict.get( Dict.UNCHOSEN_FILE );
			filePath = Dict.get( Dict.UNCHOSEN_FILE );
		}
		FlowLabel lblFileContent = new FlowLabel( fileName, CPL_SOUNDFONT_INFO, PWIDTH_GENERAL_INFO_VALUE );
		lblFileContent.setToolTipText( filePath );
		area.add( lblFileContent, constraints );
		
		// name translation
		constraints.gridx = 0;
		constraints.gridy++;
		constraints.weightx = 0;
		constraints.anchor  = GridBagConstraints.NORTHEAST;
		JLabel lblname      = new JLabel( Dict.get(Dict.NAME) + ": " );
		area.add( lblname, constraints );
		
		// name content
		constraints.gridx++;
		constraints.anchor       = GridBagConstraints.NORTHWEST;
		FlowLabel lblNameContent = new FlowLabel( soundfontInfo.get("name"), CPL_SOUNDFONT_INFO, PWIDTH_GENERAL_INFO_VALUE );
		area.add( lblNameContent, constraints );
		
		// version translation
		constraints.gridx = 0;
		constraints.gridy++;
		constraints.anchor = GridBagConstraints.NORTHEAST;
		JLabel lblVersion  = new JLabel( Dict.get(Dict.VERSION) + ": " );
		area.add( lblVersion, constraints );
		
		// version content
		constraints.gridx++;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		FlowLabel lblVersionContent = new FlowLabel( soundfontInfo.get("version"), CPL_SOUNDFONT_INFO, PWIDTH_GENERAL_INFO_VALUE );
		area.add( lblVersionContent, constraints );
		
		// vendor translation
		constraints.gridx = 0;
		constraints.gridy++;
		constraints.anchor = GridBagConstraints.NORTHEAST;
		JLabel lblVendor  = new JLabel( Dict.get(Dict.SOUNDFONT_VENDOR) + ": " );
		area.add( lblVendor, constraints );
		
		// vendor content
		constraints.gridx++;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		FlowLabel lblVendorContent = new FlowLabel( soundfontInfo.get("vendor"), CPL_SOUNDFONT_INFO, PWIDTH_GENERAL_INFO_VALUE );
		area.add( lblVendorContent, constraints );
		
		// creation date translation
		constraints.gridx = 0;
		constraints.gridy++;
		constraints.anchor     = GridBagConstraints.NORTHEAST;
		JLabel lblCreationDate = new JLabel( Dict.get(Dict.SOUNDFONT_CREA_DATE) + ": " );
		area.add( lblCreationDate, constraints );
		
		// creation date content
		constraints.gridx++;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		FlowLabel lblCreationDateContent = new FlowLabel( soundfontInfo.get("creation_date"), CPL_SOUNDFONT_INFO, PWIDTH_GENERAL_INFO_VALUE );
		area.add( lblCreationDateContent, constraints );
		
		// creation tools translation
		constraints.gridx = 0;
		constraints.gridy++;
		constraints.anchor = GridBagConstraints.NORTHEAST;
		JLabel lblTools    = new JLabel( Dict.get(Dict.SOUNDFONT_CREA_TOOLS) + ": " );
		area.add( lblTools, constraints );
		
		// version content
		constraints.gridx++;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		FlowLabel lblToolsContent = new FlowLabel( soundfontInfo.get("tools"), CPL_SOUNDFONT_INFO, PWIDTH_GENERAL_INFO_VALUE );
		area.add( lblToolsContent, constraints );
		
		// product translation
		constraints.gridx = 0;
		constraints.gridy++;
		constraints.anchor = GridBagConstraints.NORTHEAST;
		JLabel lblProduct  = new JLabel( Dict.get(Dict.SOUNDFONT_PRODUCT) + ": " );
		area.add( lblProduct, constraints );
		
		// version content
		constraints.gridx++;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		FlowLabel lblProductContent = new FlowLabel( soundfontInfo.get("product"), CPL_SOUNDFONT_INFO, PWIDTH_GENERAL_INFO_VALUE );
		area.add( lblProductContent, constraints );
		
		// target engine translation
		constraints.gridx = 0;
		constraints.gridy++;
		constraints.anchor     = GridBagConstraints.NORTHEAST;
		JLabel lblTargetEngine = new JLabel( Dict.get(Dict.SOUNDFONT_TARGET_ENGINE) + ": " );
		area.add( lblTargetEngine, constraints );
		
		// version content
		constraints.gridx++;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		FlowLabel lblTargetEngineContent = new FlowLabel( soundfontInfo.get("target_engine"), CPL_SOUNDFONT_INFO, PWIDTH_GENERAL_INFO_VALUE );
		area.add( lblTargetEngineContent, constraints );
		
		// chromatic instruments translation
		constraints.gridx = 0;
		constraints.gridy++;
		constraints.anchor = GridBagConstraints.NORTHEAST;
		JLabel lblChromatic  = new JLabel( Dict.get(Dict.SF_INSTR_CAT_CHROMATIC) + ": " );
		area.add( lblChromatic, constraints );
		
		// chromatic instruments content
		constraints.gridx++;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		FlowLabel lblChromaticContent = new FlowLabel( soundfontInfo.get("chromatic_count"), CPL_SOUNDFONT_INFO, PWIDTH_GENERAL_INFO_VALUE );
		area.add( lblChromaticContent, constraints );
		
		// drum kits translation
		constraints.gridx = 0;
		constraints.gridy++;
		constraints.anchor       = GridBagConstraints.NORTHEAST;
		JLabel lblDrumkitsSingle = new JLabel( Dict.get(Dict.SOUNDFONT_DRUMKITS) + ": " );
		area.add( lblDrumkitsSingle, constraints );
		
		// drum kits content
		int drumSingle = Integer.parseInt( soundfontInfo.get("drumkit_single_count") );
		int drumMulti  = Integer.parseInt( soundfontInfo.get("drumkit_multi_count")  );
		int drumTotal  = drumSingle + drumMulti;
		String drumkitsContent = drumTotal  + " ("
		                       + drumSingle + " " + Dict.get( Dict.SINGLE_CHANNEL ) + ", "
		                       + drumMulti  + " " + Dict.get( Dict.MULTI_CHANNEL  ) + ")";
		constraints.gridx++;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		FlowLabel lblDrumkitsSingleContent = new FlowLabel( drumkitsContent, CPL_SOUNDFONT_INFO, PWIDTH_GENERAL_INFO_VALUE );
		area.add( lblDrumkitsSingleContent, constraints );
		
		// resources translation
		constraints.gridx = 0;
		constraints.gridy++;
		constraints.anchor = GridBagConstraints.NORTHEAST;
		JLabel lblLayers  = new JLabel( Dict.get(Dict.TAB_SOUNDFONT_RESOURCES) + ": " );
		area.add( lblLayers, constraints );
		
		// resources content
		int resLayer   = Integer.parseInt( soundfontInfo.get("layer_count")            );
		int resSample  = Integer.parseInt( soundfontInfo.get("sample_count")           );
		int resUnknown = Integer.parseInt( soundfontInfo.get("unknown_resource_count") );
		int resourcesTotal = resLayer + resSample + resUnknown;
		String resourcesContent = resourcesTotal + " ("
		             + resLayer   + " " + Dict.get( Dict.SF_RESOURCE_CAT_LAYER  ) + ", "
		             + resSample  + " " + Dict.get( Dict.SF_RESOURCE_CAT_SAMPLE ) + ")";
		constraints.gridx++;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		FlowLabel lblLayersContent = new FlowLabel( resourcesContent, CPL_SOUNDFONT_INFO, PWIDTH_GENERAL_INFO_VALUE );
		area.add( lblLayersContent, constraints );
		
		// total length translation
		constraints.gridx = 0;
		constraints.gridy++;
		constraints.anchor = GridBagConstraints.NORTHEAST;
		JLabel lblTotal    = new JLabel( Dict.get(Dict.SAMPLES_TOTAL) + ": " );
		area.add( lblTotal, constraints );
		
		// total length content
		constraints.gridx++;
		constraints.anchor  = GridBagConstraints.NORTHWEST;
		String totalContent = soundfontInfo.get( "frames_count" )  + " " + Dict.get( Dict.FRAMES ) + ", "
		                    + soundfontInfo.get( "seconds_count" ) + " " + Dict.get( Dict.SEC    ) + ", "
		                    + soundfontInfo.get( "bytes_count" )   + " " + Dict.get( Dict.BYTES  );
		FlowLabel lblTotalContent = new FlowLabel( totalContent, CPL_SOUNDFONT_INFO, PWIDTH_GENERAL_INFO_VALUE );
		area.add( lblTotalContent, constraints );
		
		// average length translation
		constraints.gridx = 0;
		constraints.gridy++;
		constraints.anchor = GridBagConstraints.NORTHEAST;
		JLabel lblAverage  = new JLabel( Dict.get(Dict.SAMPLES_AVERAGE) + ": " );
		area.add( lblAverage, constraints );
		
		// average length content
		constraints.gridx++;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		String avgContent  = soundfontInfo.get( "frames_avg"  ) + " " + Dict.get( Dict.FRAMES ) + ", "
		                   + soundfontInfo.get( "seconds_avg" ) + " " + Dict.get( Dict.SEC    ) + ", "
                           + soundfontInfo.get( "bytes_avg"   ) + " " + Dict.get( Dict.BYTES  );
		FlowLabel lblAverageContent = new FlowLabel( avgContent, CPL_SOUNDFONT_INFO, PWIDTH_GENERAL_INFO_VALUE );
		area.add( lblAverageContent, constraints );
		
		// description translation
		constraints.gridx = 0;
		constraints.gridy++;
		constraints.anchor    = GridBagConstraints.NORTHEAST;
		JLabel lblDescription = new JLabel( Dict.get(Dict.DESCRIPTION) + ": " );
		area.add( lblDescription, constraints );
		
		// description content
		constraints.gridx++;
		constraints.anchor  = GridBagConstraints.NORTHWEST;
		constraints.weighty = 1;
		FlowLabel lblDescriptionContent = new FlowLabel( soundfontInfo.get("description"), CPL_SOUNDFONT_INFO, PWIDTH_GENERAL_INFO_VALUE );
		lblDescriptionContent.setHeightLimit( MAX_HEIGHT_SOUNDFONT_DESC );
		area.add( lblDescriptionContent, constraints );
		
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
		area.setLayout( layout );
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.NONE;
		constraints.insets     = new Insets( 2, 2, 2, 2 );
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 0;
		constraints.weighty    = 0;
		
		// get general sequence info
		HashMap<String, Object> sequenceInfo = SequenceAnalyzer.getSequenceInfo();
		
		// file translation
		constraints.anchor = GridBagConstraints.NORTHEAST;
		JLabel lblFile     = new JLabel( Dict.get(Dict.FILE) + ": " );
		area.add( lblFile, constraints );
		
		// file name
		constraints.gridx++;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		String filename    = SequenceParser.getFileName();
		if ( null == filename ) {
			filename = "-";
		}
		FlowLabel lblFileContent = new FlowLabel( filename, CPL_MIDI_INFO, PWIDTH_GENERAL_INFO_VALUE );
		area.add( lblFileContent, constraints );
		
		// copyright translation
		constraints.gridy++;
		constraints.gridx  = 0;
		constraints.anchor = GridBagConstraints.NORTHEAST;
		JLabel lblCopy     = new JLabel( Dict.get(Dict.COPYRIGHT) + ": " );
		area.add( lblCopy, constraints );
		
		// copyright content
		constraints.gridx++;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		String copyright                 = "-";
		Object metaObj                   = sequenceInfo.get("meta_info");
		HashMap<String, String> metaInfo = new HashMap<String, String>();
		if ( metaObj != null )
			metaInfo = (HashMap<String, String>) metaObj;
		if ( metaInfo.containsKey("copyright") )
			copyright = metaInfo.get( "copyright" );
		FlowLabel lblCopyContent = new FlowLabel( copyright, CPL_MIDI_INFO, PWIDTH_GENERAL_INFO_VALUE );
		area.add( lblCopyContent, constraints );
		
		// software translation
		constraints.gridy++;
		constraints.gridx  = 0;
		constraints.anchor = GridBagConstraints.NORTHEAST;
		JLabel lblSoftware = new JLabel( Dict.get(Dict.SOFTWARE_VERSION) + ": " );
		area.add( lblSoftware, constraints );
		
		// software content
		constraints.gridx++;
		constraints.anchor  = GridBagConstraints.NORTHWEST;
		String software     = "-";
		if ( metaInfo.containsKey("software") ) {
			software = metaInfo.get( "software" );
		}
		if ( metaInfo.containsKey("software_date") ) {
			String softwareDate = metaInfo.get( "software_date" );
			software = software + " - " + Dict.get( Dict.SOFTWARE_DATE ) + " " + softwareDate;
		}
		FlowLabel lblSoftwareContent = new FlowLabel( software, CPL_MIDI_INFO, PWIDTH_GENERAL_INFO_VALUE );
		area.add( lblSoftwareContent, constraints );
		
		// ticks translation
		constraints.gridy++;
		constraints.gridx  = 0;
		constraints.anchor = GridBagConstraints.NORTHEAST;
		JLabel lblTicks    = new JLabel( Dict.get(Dict.TICK_LENGTH) + ": " );
		area.add( lblTicks, constraints );
		
		// ticks content
		constraints.gridx++;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		String lenghStr = "-";
		Object ticksObj = sequenceInfo.get("ticks");
		if ( ticksObj != null )
			lenghStr = Long.toString( (Long) ticksObj );
		FlowLabel lblTicksContent = new FlowLabel( lenghStr, CPL_MIDI_INFO, PWIDTH_GENERAL_INFO_VALUE );
		area.add( lblTicksContent, constraints );
		
		// time translation
		constraints.gridy++;
		constraints.gridx  = 0;
		constraints.anchor = GridBagConstraints.NORTHEAST;
		JLabel lblTime     = new JLabel( Dict.get(Dict.TIME_LENGTH) + ": " );
		area.add( lblTime, constraints );
		
		// time content
		constraints.gridx++;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		lenghStr = "-";
		Object timeObj = sequenceInfo.get("time_length");
		if ( timeObj != null )
			lenghStr = (String) timeObj;
		FlowLabel lblTimeContent = new FlowLabel( (String) lenghStr, CPL_MIDI_INFO, PWIDTH_GENERAL_INFO_VALUE );
		area.add( lblTimeContent, constraints );
		
		// resolution translation
		constraints.gridy++;
		constraints.gridx    = 0;
		constraints.anchor   = GridBagConstraints.NORTHEAST;
		JLabel lblResolution = new JLabel( Dict.get(Dict.RESOLUTION) + ": " );
		area.add( lblResolution, constraints );
		
		// resolution content
		constraints.gridx++;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		String resolution  = "-";
		if ( null != sequenceInfo.get("resolution") )
			resolution = sequenceInfo.get("resolution") + " " + Dict.get( Dict.RESOLUTION_UNIT );
		FlowLabel lblResolutionContent = new FlowLabel( resolution, CPL_MIDI_INFO, PWIDTH_GENERAL_INFO_VALUE );
		area.add( lblResolutionContent, constraints );
		
		// number of tracks translation
		constraints.gridy++;
		constraints.gridx    = 0;
		constraints.anchor   = GridBagConstraints.NORTHEAST;
		JLabel lblNumTracks  = new JLabel( Dict.get(Dict.NUMBER_OF_TRACKS) + ": " );
		area.add( lblNumTracks, constraints );
		
		// number of tracks content
		constraints.gridx++;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		String numTracks   = "-";
		if ( null != sequenceInfo.get("num_tracks") ) {
			Object tracksObj = sequenceInfo.get("num_tracks");
			numTracks        = Integer.toString( (int) tracksObj );
		}
		FlowLabel lblNumTracksContent = new FlowLabel( numTracks, CPL_MIDI_INFO, PWIDTH_GENERAL_INFO_VALUE );
		area.add( lblNumTracksContent, constraints );
		
		// spacer
		constraints.gridy++;
		constraints.gridx  = 0;
		JLabel spacerLine1 = new JLabel( " " );
		area.add( spacerLine1, constraints );
		
		// tempo BPM headline
		constraints.gridwidth = 2; // colspan
		constraints.gridy++;
		constraints.gridx    = 0;
		constraints.anchor   = GridBagConstraints.NORTHWEST;
		JLabel lblTempoBpmHeadline = new JLabel( Dict.get(Dict.TEMPO_BPM) );
		area.add( lblTempoBpmHeadline, constraints );
		constraints.gridwidth = 1; // end of colspan
		
		// BPM average translation
		constraints.gridy++;
		constraints.gridx    = 0;
		constraints.anchor   = GridBagConstraints.NORTHEAST;
		JLabel lblBpmAvg = new JLabel( Dict.get(Dict.AVERAGE) + ": " );
		area.add( lblBpmAvg, constraints );
		
		// BPM average content
		constraints.gridx++;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		String tempoStr = "-";
		Object tempoObj = sequenceInfo.get("tempo_bpm_avg");
		if ( null != tempoObj )
			tempoStr = (String) tempoObj;
		FlowLabel lblBpmAvgContent = new FlowLabel( tempoStr, CPL_MIDI_INFO, PWIDTH_GENERAL_INFO_VALUE );
		area.add( lblBpmAvgContent, constraints );
		
		// BPM min translation
		constraints.gridy++;
		constraints.gridx    = 0;
		constraints.anchor   = GridBagConstraints.NORTHEAST;
		JLabel lblBpmMin = new JLabel( Dict.get(Dict.MIN) + ": " );
		area.add( lblBpmMin, constraints );
		
		// BPM min content
		constraints.gridx++;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		tempoObj           = sequenceInfo.get("tempo_bpm_min");
		if ( null != tempoObj )
			tempoStr = (String) tempoObj;
		FlowLabel lblBpmMinContent = new FlowLabel( tempoStr, CPL_MIDI_INFO, PWIDTH_GENERAL_INFO_VALUE );
		area.add( lblBpmMinContent, constraints );
		
		// BPM max translation
		constraints.gridy++;
		constraints.gridx    = 0;
		constraints.anchor   = GridBagConstraints.NORTHEAST;
		JLabel lblBpmMax = new JLabel( Dict.get(Dict.MAX) + ": " );
		area.add( lblBpmMax, constraints );
		
		// BPM max content
		constraints.gridx++;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		tempoObj           = sequenceInfo.get("tempo_bpm_max");
		if ( null != tempoObj )
			tempoStr = (String) tempoObj;
		FlowLabel lblBpmMaxContent = new FlowLabel( tempoStr, CPL_MIDI_INFO, PWIDTH_GENERAL_INFO_VALUE );
		area.add( lblBpmMaxContent, constraints );
		
		// spacer
		constraints.gridy++;
		constraints.gridx  = 0;
		JLabel spacerLine2 = new JLabel( " " );
		area.add( spacerLine2, constraints );
		
		// tempo MPQ headline
		constraints.gridwidth = 2; // colspan
		constraints.gridy++;
		constraints.gridx    = 0;
		constraints.anchor   = GridBagConstraints.NORTHWEST;
		JLabel lblTempoMpqHeadline = new JLabel( Dict.get(Dict.TEMPO_MPQ) );
		area.add( lblTempoMpqHeadline, constraints );
		constraints.gridwidth = 1; // end of colspan
		
		// MPQ average translation
		constraints.gridy++;
		constraints.gridx    = 0;
		constraints.anchor   = GridBagConstraints.NORTHEAST;
		JLabel lblMpqAvg = new JLabel( Dict.get(Dict.AVERAGE) + ": " );
		area.add( lblMpqAvg, constraints );
		
		// MPQ average content
		constraints.gridx++;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		tempoObj           = sequenceInfo.get("tempo_mpq_avg");
		if ( null != tempoObj )
			tempoStr = (String) tempoObj;
		FlowLabel lblMpqAvgContent = new FlowLabel( tempoStr, CPL_MIDI_INFO, PWIDTH_GENERAL_INFO_VALUE );
		area.add( lblMpqAvgContent, constraints );
		
		// MPQ min translation
		constraints.gridy++;
		constraints.gridx    = 0;
		constraints.anchor   = GridBagConstraints.NORTHEAST;
		JLabel lblMpqMin = new JLabel( Dict.get(Dict.MIN) + ": " );
		area.add( lblMpqMin, constraints );
		
		// MPQ min content
		constraints.gridx++;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		tempoObj           = sequenceInfo.get("tempo_mpq_min");
		if ( null != tempoObj )
			tempoStr = (String) tempoObj;
		FlowLabel lblMpqMinContent = new FlowLabel( tempoStr, CPL_MIDI_INFO, PWIDTH_GENERAL_INFO_VALUE );
		area.add( lblMpqMinContent, constraints );
		
		// MPQ max translation
		constraints.gridy++;
		constraints.gridx    = 0;
		constraints.anchor   = GridBagConstraints.NORTHEAST;
		JLabel lblMpqMax = new JLabel( Dict.get(Dict.MAX) + ": " );
		area.add( lblMpqMax, constraints );
		
		// MPQ max content
		constraints.gridx++;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		tempoObj           = sequenceInfo.get("tempo_mpq_max");
		if ( null != tempoObj )
			tempoStr = (String) tempoObj;
		FlowLabel lblMpqMaxContent = new FlowLabel( tempoStr, CPL_MIDI_INFO, PWIDTH_GENERAL_INFO_VALUE );
		area.add( lblMpqMaxContent, constraints );
		
		// spacer
		constraints.gridy++;
		constraints.gridx   = 0;
		constraints.weighty = 1;
		JLabel spacer = new JLabel( " " );
		area.add( spacer, constraints );
		
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
		area.setLayout( layout );
		GridBagConstraints constrLeft = new GridBagConstraints();
		constrLeft.fill       = GridBagConstraints.NONE;
		constrLeft.insets     = new Insets( 2, 2, 2, 2 );
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
		
		// get karaoke info
		HashMap<String, Object> sequenceInfo = SequenceAnalyzer.getSequenceInfo();
		HashMap<String, Object> karaokeInfo  = (HashMap<String, Object>) sequenceInfo.get( "karaoke" );
		if ( null == karaokeInfo )
			karaokeInfo = new HashMap<String, Object>();
		
		// karaoke type
		JLabel lblKarType = new JLabel( Dict.get(Dict.KARAOKE_TYPE) + ": " );
		area.add( lblKarType, constrLeft );
		
		// karaoke type content
		String karType = "-";
		if ( null != karaokeInfo.get("type") )
			karType = (String) karaokeInfo.get("type");
		FlowLabel lblKarTypeContent = new FlowLabel( karType, CPL_MIDI_INFO, PWIDTH_GENERAL_INFO_VALUE );
		area.add( lblKarTypeContent, constrRight );
		
		// version
		constrLeft.gridy++;
		JLabel lblVersion  = new JLabel( Dict.get(Dict.VERSION) + ": " );
		area.add( lblVersion, constrLeft );
		
		// version content
		constrRight.gridy++;
		String version = "-";
		if ( null != karaokeInfo.get("version") )
			version = (String) karaokeInfo.get("version");
		FlowLabel lblVersionContent = new FlowLabel( version, CPL_MIDI_INFO, PWIDTH_GENERAL_INFO_VALUE );
		area.add( lblVersionContent, constrRight );
		
		// title
		constrLeft.gridy++;
		JLabel lblTitle = new JLabel( Dict.get(Dict.SONG_TITLE) + ": " );
		area.add( lblTitle, constrLeft );
		
		// title content
		constrRight.gridy++;
		String title = "-";
		if ( null != karaokeInfo.get("title") )
			title = (String) karaokeInfo.get("title");
		FlowLabel lblTitleContent = new FlowLabel( title, CPL_MIDI_INFO, PWIDTH_GENERAL_INFO_VALUE );
		area.add( lblTitleContent, constrRight );
		
		// author
		constrLeft.gridy++;
		JLabel lblAuthor = new JLabel( Dict.get(Dict.AUTHOR) + ": " );
		area.add( lblAuthor, constrLeft );
		
		// author content
		constrRight.gridy++;
		String author = "-";
		if ( null != karaokeInfo.get("author") )
			author = (String) karaokeInfo.get("author");
		FlowLabel lblAuthorContent = new FlowLabel( author, CPL_MIDI_INFO, PWIDTH_GENERAL_INFO_VALUE );
		area.add( lblAuthorContent, constrRight );
		
		// composer
		constrLeft.gridy++;
		JLabel lblComposer = new JLabel( Dict.get(Dict.COMPOSER) + ": " );
		area.add( lblComposer, constrLeft );
		
		// composer content
		constrRight.gridy++;
		String composer = "-";
		if ( null != karaokeInfo.get("composer") )
			composer = (String) karaokeInfo.get("composer");
		FlowLabel lblComposerContent = new FlowLabel( composer, CPL_MIDI_INFO, PWIDTH_GENERAL_INFO_VALUE );
		area.add( lblComposerContent, constrRight );
		
		// lyricist
		constrLeft.gridy++;
		JLabel lblLyricist = new JLabel( Dict.get(Dict.LYRICIST) + ": " );
		area.add( lblLyricist, constrLeft );
		
		// lyricist content
		constrRight.gridy++;
		String lyricist = "-";
		if ( null != karaokeInfo.get("lyricist") )
			lyricist = (String) karaokeInfo.get("lyricist");
		FlowLabel lblLyricistContent = new FlowLabel( lyricist, CPL_MIDI_INFO, PWIDTH_GENERAL_INFO_VALUE );
		area.add( lblLyricistContent, constrRight );
		
		// artist
		constrLeft.gridy++;
		JLabel lblArtist = new JLabel( Dict.get(Dict.ARTIST) + ": " );
		area.add( lblArtist, constrLeft );
		
		// artist content
		constrRight.gridy++;
		String artist = "-";
		if ( null != karaokeInfo.get("artist") )
			artist = (String) karaokeInfo.get("artist");
		FlowLabel lblArtistContent = new FlowLabel( artist, CPL_MIDI_INFO, PWIDTH_GENERAL_INFO_VALUE );
		area.add( lblArtistContent, constrRight );
		
		// copyright
		constrLeft.gridy++;
		JLabel lblCopyright = new JLabel( Dict.get(Dict.KARAOKE_COPYRIGHT) + ": " );
		area.add( lblCopyright, constrLeft );
		
		// copyright content
		constrRight.gridy++;
		String copyright = "-";
		if ( null != karaokeInfo.get("copyright") )
			copyright = (String) karaokeInfo.get("copyright");
		FlowLabel lblCopyrightContent = new FlowLabel( copyright, CPL_MIDI_INFO, PWIDTH_GENERAL_INFO_VALUE );
		area.add( lblCopyrightContent, constrRight );
		
		// language
		constrLeft.gridy++;
		JLabel lblLanguage = new JLabel( Dict.get(Dict.LANGUAGE) + ": " );
		area.add( lblLanguage, constrLeft );
		
		// language content
		constrRight.gridy++;
		String language = "-";
		if ( null != karaokeInfo.get("language") )
			language = (String) karaokeInfo.get("language");
		FlowLabel lblLanguageContent = new FlowLabel( language, CPL_MIDI_INFO, PWIDTH_GENERAL_INFO_VALUE );
		area.add( lblLanguageContent, constrRight );
		
		// info
		constrLeft.gridy++;
		JLabel lblInfo = new JLabel( Dict.get(Dict.KARAOKE_INFO) + ": " );
		area.add( lblInfo, constrLeft );
		
		// info content
		constrRight.gridy++;
		String info = "-";
		if ( null != karaokeInfo.get("info") )
			info = (String) karaokeInfo.get("info");
		FlowLabel lblInfoContent = new FlowLabel( info, CPL_MIDI_INFO, PWIDTH_GENERAL_INFO_VALUE );
		lblInfoContent.setHeightLimit( MAX_HEIGHT_KARAOKE_INFO );
		area.add( lblInfoContent, constrRight );
		
		// lyrics translation
		constrLeft.gridy++;
		JLabel lblLyrics = new JLabel( Dict.get(Dict.LYRICS) + ": " );
		area.add( lblLyrics, constrLeft );
		
		// lyrics content
		constrRight.gridy++;
		constrRight.weighty = 1;
		constrRight.fill    = GridBagConstraints.BOTH;
		String lyrics       = "";
		if ( null != karaokeInfo.get("lyrics_full") )
			lyrics = (String) karaokeInfo.get( "lyrics_full"  );
		FlowLabel lblLyricsContent = new FlowLabel( lyrics, CPL_MIDI_INFO, PWIDTH_GENERAL_INFO_VALUE );
		lblLyricsContent.setHeightLimit( MAX_HEIGHT_LYRICS );
		area.add( lblLyricsContent, constrRight );
		
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
		MidicaSplitPane area = new MidicaSplitPane( JSplitPane.HORIZONTAL_SPLIT );
		area.setResizeWeight( 0.5 );
		area.setDividerLocation( 0.5 );
		
		// fill the sub areas
		area.add( createBankInstrNoteAreaHalf(true) );
		area.add( createBankInstrNoteAreaHalf(false) );
		
		return area;
	}
	
	/**
	 * Creates the area for banks, instruments and notes used by the loaded
	 * MIDI sequence, either **in total** or **per channel**.
	 * 
	 * @param inTotal  **true** for creating the total usage tree area.
	 *                 **false** for the per channel area.
	 * @return the created area.
	 */
	private Container createBankInstrNoteAreaHalf( boolean inTotal ) {
		// content
		JPanel area = new JPanel();
		
		// initialize area-dependant constants
		String keyName  = "banks_per_channel";
		String headline = Dict.get( Dict.PER_CHANNEL );
		String btnName  = InfoController.NAME_TREE_BANKS_PER_CHANNEL;
		if (inTotal) {
			keyName  = "banks_total";
			headline = Dict.get( Dict.TOTAL );
			btnName  = InfoController.NAME_TREE_BANKS_TOTAL;
		}
		
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
		constraints.weighty    = 0;
		constraints.anchor     = GridBagConstraints.NORTH;
		
		// get tree model and inform the controller
		HashMap<String, Object> sequenceInfo = SequenceAnalyzer.getSequenceInfo();
		Object          contentObj = sequenceInfo.get( keyName );
		MidicaTreeModel model;
		if ( contentObj != null && contentObj instanceof MidicaTreeModel ) {
			model = (MidicaTreeModel) contentObj;
			model.postprocess();
		}
		else {
			model = new MidicaTreeModel( Dict.get(Dict.TOTAL) );
		}
		controller.setTreeModel( model, btnName );
		
		// headline (translation, expand, collapse)
		Container head = createTreeHeadline( headline, btnName );
		area.add( head, constraints );
		
		// tree
		constraints.gridy++;
		constraints.weighty = 1;
		MidicaTree  tree    = new MidicaTree( model );
		JScrollPane scroll  = new JScrollPane( tree );
		model.setTree( tree );
		area.add( scroll, constraints );
		
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
	private Container createTreeHeadline( String headline, String btnName ) {
		// content
		JPanel area = new JPanel();
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout( layout );
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.NONE;
		constraints.insets     = new Insets( 2, 2, 2, 2 );
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 0;
		constraints.weighty    = 0;
		constraints.anchor     = GridBagConstraints.CENTER;
		
		// headline translation
		JLabel lblHeadline = new JLabel( headline );
		area.add( lblHeadline, constraints );
		
		// spacer
		constraints.gridx++;
		JLabel spacer1 = new JLabel( "<html>&nbsp;" );
		area.add( spacer1, constraints );
		
		// collapse button
		constraints.gridx++;
		Insets buttonInsets = new Insets( 0, 0, 0, 0 );
		JButton btnCollapse = new JButton( Dict.get(Dict.COLLAPSE_BUTTON) );
		btnCollapse.setToolTipText( Dict.get(Dict.COLLAPSE_TOOLTIP) );
		btnCollapse.setPreferredSize( collapseExpandDim );
		btnCollapse.setMargin( buttonInsets );
		btnCollapse.setName( btnName );
		btnCollapse.setActionCommand( InfoController.CMD_COLLAPSE );
		btnCollapse.addActionListener( controller );
		area.add( btnCollapse, constraints );
		
		// spacer
		constraints.gridx++;
		JLabel spacer2 = new JLabel( "" );
		area.add( spacer2, constraints );
		
		// expand button
		constraints.gridx++;
		JButton btnExpand = new JButton( Dict.get(Dict.EXPAND_BUTTON) );
		btnExpand.setToolTipText( Dict.get(Dict.EXPAND_TOOLTIP) );
		btnExpand.setPreferredSize( collapseExpandDim );
		btnExpand.setMargin( buttonInsets );
		btnExpand.setName( btnName );
		btnExpand.setActionCommand( InfoController.CMD_EXPAND );
		btnExpand.addActionListener( controller );
		area.add( btnExpand, constraints );
		
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
		MidicaSplitPane area = new MidicaSplitPane( JSplitPane.VERTICAL_SPLIT );
		area.setResizeWeight( 0.45 );
		area.setDividerLocation( 0.45 );
		
		// tree and details
		MidicaSplitPane topArea = new MidicaSplitPane( JSplitPane.HORIZONTAL_SPLIT );
		topArea.add( createMsgTreeArea()    );
		topArea.add( createMsgDetailsArea() );
		topArea.setResizeWeight( 0.5 );
		topArea.setDividerLocation( 0.5 );
		area.add( topArea );
		
		// list
		area.add( createMsgTableArea() );
		
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
		area.setLayout( layout );
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.BOTH;
		constraints.insets     = new Insets( 2, 2, 2, 2 );
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 0;
		constraints.weighty    = 0;
		constraints.anchor     = GridBagConstraints.CENTER;
		
		// get tree model and inform the controller
		HashMap<String, Object> sequenceInfo = SequenceAnalyzer.getSequenceInfo();
		Object          modelObj = sequenceInfo.get( "msg_tree_model" );
		MidicaTreeModel model    = null;
		if ( modelObj != null && modelObj instanceof MidicaTreeModel ) {
			model = (MidicaTreeModel) modelObj;
			model.postprocess();
		}
		else {
			try {
				model = new MidicaTreeModel( Dict.get(Dict.TAB_MESSAGES), MessageTreeNode.class );
			}
			catch ( ReflectiveOperationException e ) {
				e.printStackTrace();
			}
		}
		controller.setTreeModel( model, InfoController.NAME_TREE_MESSAGES );
		
		// tree headline (translation, expand, collapse)
		String headline              = Dict.get( Dict.TAB_MESSAGES );
		String btnName               = InfoController.NAME_TREE_MESSAGES;
		Container head               = createTreeHeadline( headline, btnName );
		collapseExpandHeadlineHeight = head.getPreferredSize().height;
		area.add( head, constraints );
		
		// message tree
		constraints.gridy++;
		constraints.weightx = 1;
		constraints.weighty = 1;
		msgTree             = new MidicaTree( model );
		msgTree.setName( InfoController.NAME_TREE_MESSAGES );
		msgTree.addTreeSelectionListener( controller );
		msgTree.addFocusListener( controller );
		msgTree.setBackground( Config.MSG_TREE_COLOR );
		DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) msgTree.getCellRenderer();
		renderer.setBackgroundNonSelectionColor( null ); // use the tree's color also for the nodes
		JScrollPane msgScroll = new JScrollPane( msgTree );
		model.setTree( msgTree );
		msgScroll.setPreferredSize( msgTreeDim );
		area.add( msgScroll, constraints );
		
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
		area.setLayout( layout );
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.NONE;
		constraints.insets     = new Insets( 2, 2, 2, 2 );
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 0;
		constraints.weighty    = 0;
		constraints.anchor     = GridBagConstraints.CENTER;
		
		// details headline
		JLabel lblDetails = new JLabel( Dict.get(Dict.DETAILS) );
		area.add( lblDetails, constraints );
		
		// synchronize with the tree headline from the other
		// half of the split pane
		int       width       = lblDetails.getPreferredSize().width;
		Dimension headlineDim = new Dimension( width, collapseExpandHeadlineHeight );
		lblDetails.setPreferredSize( headlineDim );
		
		// details content
		constraints.gridy++;
		constraints.weightx = 1;
		constraints.weighty = 1;
		constraints.fill    = GridBagConstraints.BOTH;
		msgDetails = new JPanel();
		msgDetails.setLayout( layout );
		msgDetails.setBackground( Config.MSG_DEFAULT_COLOR );
		JScrollPane detailsScroll = new JScrollPane( msgDetails );
		detailsScroll.setPreferredSize( msgDetailsDim );
		area.add( detailsScroll, constraints );
		
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
		area.setLayout( layout );
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.BOTH;
		constraints.insets     = new Insets( 0, 0, 0, 0 );
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 0;
		constraints.weighty    = 0;
		constraints.anchor     = GridBagConstraints.NORTHWEST;
		
		// get messages
		HashMap<String, Object>  sequenceInfo = SequenceAnalyzer.getSequenceInfo();
		Object                   msgObj       = sequenceInfo.get( "messages" );
		ArrayList<SingleMessage> messages     = (ArrayList<SingleMessage>) msgObj;
		
		// filter
		long minTick = 0;
		long maxTick = 0;
		if (null != messages) {
			int last = messages.size() - 1;
			if ( last > 0 ) {
				minTick = (long) messages.get( 0    ).getOption( IMessageType.OPT_TICK );
				maxTick = (long) messages.get( last ).getOption( IMessageType.OPT_TICK );
			}
		}
		Container filter = createMsgFilterArea( minTick, maxTick );
		area.add( filter, constraints );
		
		// message table
		constraints.gridy++;
		constraints.weightx     = 1;
		constraints.weighty     = 1;
		constraints.fill        = GridBagConstraints.BOTH;
		MessageTableModel model = new MessageTableModel(messages);
		msgTable                = new MidicaTable(model);
		msgTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
		msgTable.getSelectionModel().addListSelectionListener( controller );
		msgTable.addFocusListener( controller );
		msgTable.getColumnModel().getColumn( 0 ).setPreferredWidth( COL_WIDTH_MSG_TICK    );
		msgTable.getColumnModel().getColumn( 1 ).setPreferredWidth( COL_WIDTH_MSG_STATUS  );
		msgTable.getColumnModel().getColumn( 2 ).setPreferredWidth( COL_WIDTH_MSG_TRACK   );
		msgTable.getColumnModel().getColumn( 3 ).setPreferredWidth( COL_WIDTH_MSG_CHANNEL );
		msgTable.getColumnModel().getColumn( 4 ).setPreferredWidth( COL_WIDTH_MSG_LENGTH  );
		msgTable.getColumnModel().getColumn( 5 ).setPreferredWidth( COL_WIDTH_MSG_TYPE    );
		MessageTableCellRenderer renderer = new MessageTableCellRenderer( model, Config.MSG_TABLE_COLOR );
		msgTable.setDefaultRenderer( Object.class, renderer );
		
		JScrollPane scroll = new JScrollPane( msgTable );
		scroll.setPreferredSize( msgTableDim );
		area.add( scroll, constraints );
		
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
	private Container createMsgFilterArea( long minTick, long maxTick ) {
		// content
		JPanel area = new JPanel();
		
		// prepare filter widget storing
		filterWidgets = new HashMap<String, JComponent>();
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout( layout );
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor     = GridBagConstraints.WEST;
		constraints.fill       = GridBagConstraints.NONE;
		constraints.insets     = new Insets( 0, 0, 0, 0 );
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 0;
		constraints.weighty    = 0;
		
		// line 1
		
		// checkbox to filter channel-independent messages
		JCheckBox cbxChannelIndep = new JCheckBox( Dict.get(Dict.MSG_FILTER_CHANNEL_INDEP) );
		cbxChannelIndep.setToolTipText( Dict.get(Dict.MSG_FLTR_TT_CHANNEL_INDEP) );
		cbxChannelIndep.addItemListener( controller );
		cbxChannelIndep.setSelected( true );
		cbxChannelIndep.setName( FILTER_CBX_CHAN_INDEP );
		area.add( cbxChannelIndep, constraints );
		filterWidgets.put( FILTER_CBX_CHAN_INDEP, cbxChannelIndep );
		
		// checkbox to filter selected tree nodes
		constraints.gridx++;
		JCheckBox cbxFilterNodes = new JCheckBox( Dict.get(Dict.MSG_FILTER_SELECTED_NODES) );
		cbxFilterNodes.setToolTipText( Dict.get(Dict.MSG_FLTR_TT_SELECTED_NODES) );
		cbxFilterNodes.addItemListener( controller );
		cbxFilterNodes.setName( FILTER_CBX_NODE );
		area.add( cbxFilterNodes, constraints );
		filterWidgets.put( FILTER_CBX_NODE, cbxFilterNodes );
		
		// checkbox to limit ticks
		constraints.gridx++;
		JCheckBox cbxLimitTicks = new JCheckBox( Dict.get(Dict.MSG_FILTER_LIMIT_TICKS) );
		cbxLimitTicks.setToolTipText( Dict.get(Dict.MSG_FLTR_TT_LIMIT_TICKS) );
		cbxLimitTicks.addItemListener( controller );
		cbxLimitTicks.setName( FILTER_CBX_LIMIT_TICKS );
		area.add( cbxLimitTicks, constraints );
		filterWidgets.put( FILTER_CBX_LIMIT_TICKS, cbxLimitTicks );
		
		// spacer
		constraints.gridx++;
		constraints.fill       = GridBagConstraints.BOTH;
		constraints.weightx    = 1;
		JLabel spacerTickRange = new JLabel( "<html>&nbsp;&nbsp;&nbsp;" );
		area.add( spacerTickRange, constraints );
		
		// ticks: "from" label
		constraints.gridx++;
		constraints.fill    = GridBagConstraints.NONE;
		constraints.weightx = 0;
		JLabel lblFromTicks = new JLabel( Dict.get(Dict.MSG_FILTER_TICK_FROM) + ": " );
		area.add( lblFromTicks, constraints );
		
		// dimension for text fields
		int defaultHeight      = ( new JTextField() ).getHeight();
		Dimension dimTicksFld  = new Dimension( TICK_RANGE_FILTER_WIDTH,  defaultHeight );
		Dimension dimTracksFld = new Dimension( TRACK_RANGE_FILTER_WIDTH, defaultHeight );
		
		// ticks: "from" textfield
		constraints.gridx++;
		constraints.fill        = GridBagConstraints.VERTICAL;
		JTextField txtFromTicks = new JTextField( minTick + "" );
		txtFromTicks.setPreferredSize( dimTicksFld );
		txtFromTicks.setName( FILTER_TXT_FROM_TICKS );
		txtFromTicks.setEnabled( false );
		txtFromTicks.getDocument().putProperty( "name", FILTER_TXT_FROM_TICKS );
		txtFromTicks.getDocument().addDocumentListener( controller );
		txtFromTicks.setBackground( Config.COLOR_NORMAL );
		area.add( txtFromTicks, constraints );
		filterWidgets.put( FILTER_TXT_FROM_TICKS, txtFromTicks );
		
		// spacer
		constraints.gridx++;
		constraints.fill    = GridBagConstraints.NONE;
		JLabel spacerFromTo = new JLabel( " " );
		area.add( spacerFromTo, constraints );
		
		// ticks: "to" label
		constraints.gridx++;
		JLabel lblToTicks = new JLabel( Dict.get(Dict.MSG_FILTER_TICK_TO) + ": " );
		area.add( lblToTicks, constraints );
		
		// ticks: "to" textfield
		constraints.gridx++;
		constraints.fill      = GridBagConstraints.VERTICAL;
		JTextField txtToTicks = new JTextField( maxTick + "" );
		txtToTicks.setPreferredSize( dimTicksFld );
		txtToTicks.setName( FILTER_TXT_TO_TICKS );
		txtToTicks.setEnabled( false );
		txtToTicks.getDocument().putProperty( "name", FILTER_TXT_TO_TICKS );
		txtToTicks.getDocument().addDocumentListener( controller );
		txtToTicks.setBackground( Config.COLOR_NORMAL );
		area.add( txtToTicks, constraints );
		filterWidgets.put( FILTER_TXT_TO_TICKS, txtToTicks );
		
		// spacer
		constraints.gridx++;
		constraints.weightx    = 1;
		constraints.fill       = GridBagConstraints.BOTH;
		JLabel spacerTickTrack = new JLabel( "<html>&nbsp;&nbsp;&nbsp;" );
		area.add( spacerTickTrack, constraints );
		
		// checkbox to limit tracks
		constraints.gridx++;
		constraints.weightx = 0;
		constraints.fill    = GridBagConstraints.NONE;
		JCheckBox cbxTracks = new JCheckBox( Dict.get(Dict.MSG_FILTER_LIMIT_TRACKS) );
		cbxTracks.setToolTipText( Dict.get(Dict.MSG_FLTR_TT_LIMIT_TRACKS) );
		cbxTracks.addItemListener( controller );
		cbxTracks.setName( FILTER_CBX_LIMIT_TRACKS );
		area.add( cbxTracks, constraints );
		filterWidgets.put( FILTER_CBX_LIMIT_TRACKS, cbxTracks );
		
		// spacer
		constraints.gridx++;
		JLabel spacerTrack = new JLabel( "<html>&nbsp;" );
		area.add( spacerTrack, constraints );
		
		// get max track number
		int maxTrack = 0;
		HashMap<String, Object> sequenceInfo = SequenceAnalyzer.getSequenceInfo();
		Object trackNumObj                   = sequenceInfo.get( "num_tracks" );
		if ( trackNumObj != null ) {
			maxTrack = (int) trackNumObj;
			maxTrack--; // track numbers start from 0
		}
		
		// limit tracks text field
		constraints.gridx++;
		constraints.fill      = GridBagConstraints.VERTICAL;
		JTextField txtTracks = new JTextField( "0-" + maxTrack );
		txtTracks.setPreferredSize( dimTracksFld );
		txtTracks.setName( FILTER_TXT_TRACKS );
		txtTracks.setEnabled( false );
		txtTracks.getDocument().putProperty( "name", FILTER_TXT_TRACKS );
		txtTracks.getDocument().addDocumentListener( controller );
		txtTracks.setBackground( Config.COLOR_NORMAL );
		txtTracks.setToolTipText( Dict.get(Dict.MSG_FLTR_TT_TRACKS) );
		area.add( txtTracks, constraints );
		filterWidgets.put( FILTER_TXT_TRACKS, txtTracks );
		
		// line 2
		
		// checkbox to filter channel messages
		constraints.gridy++;
		constraints.gridx       = 0;
		constraints.fill        = GridBagConstraints.NONE;
		JCheckBox cbxChannelDep = new JCheckBox( Dict.get(Dict.MSG_FILTER_CHANNEL_DEP) );
		cbxChannelDep.setToolTipText( Dict.get(Dict.MSG_FLTR_TT_CHANNEL_DEP) );
		cbxChannelDep.addItemListener( controller );
		cbxChannelDep.setName( FILTER_CBX_CHAN_DEP );
		area.add( cbxChannelDep, constraints );
		cbxChannelDep.setSelected( true );
		filterWidgets.put( FILTER_CBX_CHAN_DEP, cbxChannelDep );
		
		// checkboxes for all channels
		constraints.gridx++;
		constraints.fill      = GridBagConstraints.HORIZONTAL;
		constraints.weightx   = 1;
		constraints.gridwidth = 12;
		area.add( createMsgFilterChannelCheckboxes(), constraints );
		
		// line 3
		
		// button, auto-show checkbox, visible/total count
		constraints.gridy++;
		constraints.gridx     = 0;
		constraints.weightx   = 0;
		constraints.gridwidth = 13;
		area.add( createMsgFilterLine3(), constraints );
		
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
		area.setLayout( layout );
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.HORIZONTAL;
		constraints.insets     = new Insets( 0, 0, 0, 0 );
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 0;
		constraints.weighty    = 0;
		constraints.anchor     = GridBagConstraints.WEST;
		
		// one checkbox for each channel
		for ( byte channel = 0; channel < 16; channel++ ) {
			constraints.gridx++;
			JCheckBox cbxChannel = new JCheckBox( channel + "" );
			cbxChannel.setToolTipText( Dict.get(Dict.MSG_FLTR_TT_CHANNEL_SINGLE) + " " + channel );
			cbxChannel.addItemListener( controller );
			cbxChannel.setName( FILTER_CBX_CHAN_PREFIX + channel );
			cbxChannel.setSelected( true );
			area.add( cbxChannel, constraints );
			filterWidgets.put( FILTER_CBX_CHAN_PREFIX + channel, cbxChannel );
		}
		
		// spacer
		constraints.gridx++;
		constraints.weightx    = 1;
		constraints.fill       = GridBagConstraints.HORIZONTAL;
		JLabel spacerTickRange = new JLabel( "" );
		area.add( spacerTickRange, constraints );
		
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
		area.setLayout( layout );
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor     = GridBagConstraints.WEST;
		constraints.fill       = GridBagConstraints.NONE;
		constraints.insets     = new Insets( 0, 0, 0, 0 );
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 0;
		constraints.weighty    = 0;
		
		// button: show in tree
		JButton btnShowInTree = new JButton( Dict.get(Dict.MSG_FILTER_SHOW_IN_TREE) );
		btnShowInTree.setToolTipText( Dict.get(Dict.MSG_FLTR_TT_SHOW_IN_TREE) );
		btnShowInTree.setActionCommand( FILTER_BTN_SHOW_TREE );
		btnShowInTree.setName( FILTER_BTN_SHOW_TREE );
		btnShowInTree.addActionListener( controller );
		btnShowInTree.setEnabled( false );
		area.add( btnShowInTree, constraints );
		filterWidgets.put( FILTER_BTN_SHOW_TREE, btnShowInTree );
		// adjust dimension
		int width = btnShowInTree.getPreferredSize().width;
		btnShowInTree.setPreferredSize( new Dimension(width, FILTER_BUTTON_HEIGHT) );
		
		// checkbox: auto-show-in-tree
		constraints.gridx++;
		JCheckBox cbxAutoShow = new JCheckBox( Dict.get(Dict.MSG_FILTER_AUTO_SHOW) );
		cbxAutoShow.setToolTipText( Dict.get(Dict.MSG_FLTR_TT_AUTO_SHOW) );
		cbxAutoShow.addItemListener( controller );
		cbxAutoShow.setName( FILTER_CBX_AUTO_SHOW );
		area.add( cbxAutoShow, constraints );
		cbxAutoShow.setSelected( false );
		filterWidgets.put( FILTER_CBX_AUTO_SHOW, cbxAutoShow );
		
		// spacer
		constraints.gridx++;
		constraints.fill    = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1;
		JLabel spacerRight  = new JLabel( "" );
		area.add( spacerRight, constraints );
		
		// label: visible messages
		constraints.gridx++;
		constraints.fill     = GridBagConstraints.NONE;
		constraints.weightx  = 0;
		JLabel lblVisibleMsg = new JLabel( "0" );
		lblVisibleMsg.setToolTipText( Dict.get(Dict.MSG_FLTR_TT_VISIBLE) );
		area.add( lblVisibleMsg, constraints );
		filterWidgets.put( FILTER_LBL_VISIBLE, lblVisibleMsg );
		
		// label: /
		constraints.gridx++;
		JLabel lblSlash = new JLabel( "<html>&nbsp;&nbsp;/&nbsp;&nbsp;" );
		area.add( lblSlash, constraints );
		
		// label: total messages
		constraints.gridx++;
		JLabel lblTotalMsg = new JLabel( "0" );
		lblTotalMsg.setToolTipText( Dict.get(Dict.MSG_FLTR_TT_TOTAL) );
		area.add( lblTotalMsg, constraints );
		filterWidgets.put( FILTER_LBL_TOTAL, lblTotalMsg );
		
		return area;
	}
	
	/**
	 * Creates the area for instruments and drumkits of the currently loaded soundfont.
	 * 
	 * @return the created soundfont instruments area.
	 */
	private Container createSoundfontInstrumentArea() {
		// content
		JPanel area = new JPanel();
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout( layout );
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.NONE;
		constraints.insets     = new Insets( 2, 2, 2, 2 );
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 1;
		constraints.weighty    = 0;
		constraints.anchor     = GridBagConstraints.NORTH;
		
		// label
		JLabel label = new JLabel( Dict.get(Dict.TAB_SOUNDFONT_INSTRUMENTS) );
		area.add( label, constraints );

		// table
		constraints.fill    = GridBagConstraints.VERTICAL;
		constraints.weighty = 1;
		constraints.gridy++;
		MidicaTable table = new MidicaTable();
		table.setModel( new SoundfontInstrumentsTableModel() );
		table.setDefaultRenderer( Object.class, new SoundfontInstrumentTableCellRenderer() );
		JScrollPane scroll = new JScrollPane( table );
		scroll.setPreferredSize( sfInstrTableDim );
		area.add( scroll, constraints );
		
		// set column sizes and colors
		table.getColumnModel().getColumn( 0 ).setPreferredWidth( COL_WIDTH_SF_INSTR_PROGRAM  );
		table.getColumnModel().getColumn( 1 ).setPreferredWidth( COL_WIDTH_SF_INSTR_BANK     );
		table.getColumnModel().getColumn( 2 ).setPreferredWidth( COL_WIDTH_SF_INSTR_NAME     );
		table.getColumnModel().getColumn( 3 ).setPreferredWidth( COL_WIDTH_SF_INSTR_CHANNELS );
		table.getColumnModel().getColumn( 4 ).setPreferredWidth( COL_WIDTH_SF_INSTR_KEYS     );
		
		return area;
	}
	
	/**
	 * Creates the area for resources of the currently loaded soundfont.
	 * 
	 * @return the created soundfont resource area.
	 */
	private Container createSoundfontResourceArea() {
		// content
		JPanel area = new JPanel();
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout( layout );
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill   = GridBagConstraints.NONE;
		constraints.insets = new Insets( 2, 2, 2, 2 );
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 1;
		constraints.weighty    = 0;
		
		// label
		JLabel label = new JLabel( Dict.get(Dict.TAB_SOUNDFONT_RESOURCES) );
		area.add( label, constraints );
		
		// table
		constraints.fill    = GridBagConstraints.BOTH;
		constraints.weighty = 1;
		constraints.gridy++;
		MidicaTable table = new MidicaTable();
		table.setModel( new SoundfontResourceTableModel() );
		table.setDefaultRenderer( Object.class, new SoundfontResourceTableCellRenderer() );
		JScrollPane scroll = new JScrollPane( table );
		scroll.setPreferredSize( sfResourceTableDim );
		area.add( scroll, constraints );
		
		// set column sizes and colors
		table.getColumnModel().getColumn( 0 ).setPreferredWidth( COL_WIDTH_SF_RES_INDEX  );
		table.getColumnModel().getColumn( 1 ).setPreferredWidth( COL_WIDTH_SF_RES_TYPE   );
		table.getColumnModel().getColumn( 2 ).setPreferredWidth( COL_WIDTH_SF_RES_NAME   );
		table.getColumnModel().getColumn( 3 ).setPreferredWidth( COL_WIDTH_SF_RES_FRAMES );
		table.getColumnModel().getColumn( 4 ).setPreferredWidth( COL_WIDTH_SF_RES_FORMAT );
		table.getColumnModel().getColumn( 5 ).setPreferredWidth( COL_WIDTH_SF_RES_CLASS  );
		
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
		area.setLayout( layout );
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.NONE;
		constraints.insets     = new Insets( 2, 2, 2, 2 );
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 0;
		constraints.weighty    = 0;
		
		// spacer
		JLabel spacerTop = new JLabel( "<html><br><br><br>" );
		area.add( spacerTop, constraints );
		
		// version translation
		constraints.gridy++;
		constraints.gridx  = 0;
		constraints.anchor = GridBagConstraints.EAST;
		JLabel lblVersion  = new JLabel( Dict.get(Dict.VERSION) + ": " );
		area.add( lblVersion, constraints );
		
		// version content
		constraints.gridx++;
		constraints.anchor = GridBagConstraints.WEST;
		JLabel lblVersionContent = new JLabel( Midica.VERSION );
		area.add( lblVersionContent, constraints );
		
		// timestamp translation
		constraints.gridx = 0;
		constraints.gridy++;
		constraints.anchor = GridBagConstraints.EAST;
		JLabel lblTimestamp = new JLabel( Dict.get(Dict.DATE) + ": " );
		area.add( lblTimestamp, constraints );
		
		// timestamp content
		constraints.gridx++;
		constraints.anchor = GridBagConstraints.WEST;
		Date             timestamp = new Date( Midica.VERSION_MINOR * 1000L );
		SimpleDateFormat formatter = new SimpleDateFormat( Dict.get(Dict.TIMESTAMP_FORMAT) );
		JLabel lblTimestampContent = new JLabel( formatter.format(timestamp) );
		area.add( lblTimestampContent, constraints );
		
		// author translation
		constraints.gridx = 0;
		constraints.gridy++;
		constraints.anchor = GridBagConstraints.EAST;
		JLabel lblAuthor   = new JLabel( Dict.get(Dict.AUTHOR) + ": " );
		area.add( lblAuthor, constraints );
		
		// author content
		constraints.gridx++;
		constraints.anchor      = GridBagConstraints.WEST;
		JLabel lblAuthorContent = new JLabel( Midica.AUTHOR );
		area.add( lblAuthorContent, constraints );
		
		// source URL translation
		constraints.gridx = 0;
		constraints.gridy++;
		constraints.anchor = GridBagConstraints.EAST;
		JLabel lblSource   = new JLabel( Dict.get(Dict.SOURCE_URL) + ": " );
		area.add( lblSource, constraints );
		
		// source URL content
		constraints.gridx++;
		constraints.anchor      = GridBagConstraints.WEST;
		JLabel lblSourceContent = new JLabel( Midica.SOURCE_URL );
		area.add( lblSourceContent, constraints );
		
		// website translation
		constraints.gridx = 0;
		constraints.gridy++;
		constraints.anchor = GridBagConstraints.EAST;
		JLabel lblWebsite  = new JLabel( Dict.get(Dict.WEBSITE) + ": " );
		area.add( lblWebsite, constraints );
		
		// website content
		constraints.gridx++;
		constraints.anchor       = GridBagConstraints.WEST;
		JLabel lblWebsiteContent = new JLabel( Midica.URL );
		area.add( lblWebsiteContent, constraints );
		
		// spacer
		constraints.gridy++;
		constraints.gridx   = 0;
		constraints.weighty = 1;
		JLabel spacer = new JLabel( " " );
		area.add( spacer, constraints );
		
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
		if ( null != infoView )
			infoView.close();
		infoView = new InfoView( owner );
	}
	
	/**
	 * Removes all contents from the message details area.
	 */
	public void cleanMsgDetails() {
		msgDetails.removeAll();
		msgDetails.setBackground( Config.MSG_DEFAULT_COLOR );
		msgDetails.revalidate();
		msgDetails.repaint();
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
		Color bgColor = Config.MSG_TREE_COLOR;
		if (fromTable) {
			bgColor = Config.MSG_TABLE_COLOR;
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
		constrLeft.insets     = new Insets( 2, 2, 2, 2 );
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
		String tickRange = messageSource.getRange( IMessageType.OPT_TICK );
		if ( tickRange != null ) {
			
			// label
			JLabel lblTicks = new JLabel( translTick );
			msgDetails.add( lblTicks, constrLeft );
			
			// content
			FlowLabel ticks = new FlowLabel( tickRange, CPL_MSG_DETAILS, PWIDTH_MSG_DETAIL_CONTENT );
			ticks.setBackground( bgColor );
			msgDetails.add( ticks, constrRight );
		}
		
		// message length
		String lengthRange = messageSource.getRange( IMessageType.OPT_LENGTH );
		if ( lengthRange != null ) {
			
			// label
			constrLeft.gridy++;
			JLabel lblLength = new JLabel( Dict.get(Dict.MSG_DETAILS_LENGTH) );
			msgDetails.add( lblLength, constrLeft );
			
			// content
			constrRight.gridy++;
			FlowLabel length = new FlowLabel( lengthRange, CPL_MSG_DETAILS, PWIDTH_MSG_DETAIL_CONTENT );
			length.setBackground( bgColor );
			msgDetails.add( length, constrRight );
		}
		
		// status byte
		Object statusObj = messageSource.getOption( IMessageType.OPT_STATUS_BYTE );
		if ( statusObj != null ) {
			
			// label
			constrLeft.gridy++;
			JLabel lblStatus = new JLabel( Dict.get(Dict.MSG_DETAILS_STATUS_BYTE) );
			msgDetails.add( lblStatus, constrLeft );
			
			// content
			constrRight.gridy++;
			String statusStr = "";
			if ( statusObj instanceof String ) {
				statusStr = (String) statusObj;
			}
			else {
				byte statusByte = (Byte) statusObj;
				statusStr       = String.format( "%02X", statusByte );
			}
			FlowLabel status = new FlowLabel( "0x" + statusStr, CPL_MSG_DETAILS, PWIDTH_MSG_DETAIL_CONTENT );
			status.setBackground( bgColor );
			msgDetails.add( status, constrRight );
		}
		
		// tracks
		String tracksStr = messageSource.getDistinctOptions( IMessageType.OPT_TRACK );
		if ( tracksStr != null ) {
			
			// label
			constrLeft.gridy++;
			JLabel lblTracks = new JLabel( translTrack );
			msgDetails.add( lblTracks, constrLeft );
			
			// content
			constrRight.gridy++;
			FlowLabel tracks = new FlowLabel( tracksStr, CPL_MSG_DETAILS, PWIDTH_MSG_DETAIL_CONTENT );
			tracks.setBackground( bgColor );
			msgDetails.add( tracks, constrRight );
		}
		
		// channels
		String channelsStr = messageSource.getDistinctOptions( IMessageType.OPT_CHANNEL );
		if ( channelsStr != null ) {
			
			// label
			constrLeft.gridy++;
			JLabel lblChannels = new JLabel( translChannel );
			msgDetails.add( lblChannels, constrLeft );
			
			// content
			constrRight.gridy++;
			FlowLabel channels = new FlowLabel( channelsStr, CPL_MSG_DETAILS, PWIDTH_MSG_DETAIL_CONTENT );
			channels.setBackground( bgColor );
			msgDetails.add( channels, constrRight );
		}
		
		// meta type
		Object metaTypeObj = messageSource.getOption( IMessageType.OPT_META_TYPE );
		if ( metaTypeObj instanceof Integer ) {
			
			// label
			constrLeft.gridy++;
			JLabel lblMetaType = new JLabel( Dict.get(Dict.MSG_DETAILS_META_TYPE) );
			msgDetails.add( lblMetaType, constrLeft );
			
			// content
			constrRight.gridy++;
			int       metaTypeByte = (Integer) metaTypeObj;
			String    metaTypeStr  = String.format( "%02X", metaTypeByte );
			FlowLabel metaType     = new FlowLabel( "0x" + metaTypeStr, CPL_MSG_DETAILS, PWIDTH_MSG_DETAIL_CONTENT );
			metaType.setBackground( bgColor );
			msgDetails.add( metaType, constrRight );
		}
		
		// vendor - byte(s) and name
		Object vendorObj     = messageSource.getOption( IMessageType.OPT_VENDOR_ID   );
		Object vendorNameObj = messageSource.getOption( IMessageType.OPT_VENDOR_NAME );
		if ( vendorObj instanceof String ) {
			
			// label
			constrLeft.gridy++;
			JLabel lblVendor = new JLabel( Dict.get(Dict.MSG_DETAILS_VENDOR) );
			msgDetails.add( lblVendor, constrLeft );
			
			// constrct vendor string
			String vendorStr = "0x" + (String) vendorObj; // ID
			if ( vendorNameObj instanceof String ) {
				vendorStr = vendorStr + " (" + ( (String) vendorNameObj ) + ")";
			}
			
			// content
			constrRight.gridy++;
			FlowLabel vendor = new FlowLabel( vendorStr, CPL_MSG_DETAILS, PWIDTH_MSG_DETAIL_CONTENT );
			vendor.setBackground( bgColor );
			msgDetails.add( vendor, constrRight );
		}
		
		// sysex channel
		Object sysexChannelObj = messageSource.getDistinctOptions( IMessageType.OPT_SYSEX_CHANNEL );
		if ( sysexChannelObj instanceof String ) {
			
			// label
			constrLeft.gridy++;
			JLabel lblSysExChannel = new JLabel( translDevId );
			msgDetails.add( lblSysExChannel, constrLeft );
			
			// contentnode
			constrRight.gridy++;
			String sysexChannelStr = (String) sysexChannelObj;
			FlowLabel sysExChannel = new FlowLabel( sysexChannelStr, CPL_MSG_DETAILS, PWIDTH_MSG_DETAIL_CONTENT );
			sysExChannel.setBackground( bgColor );
			msgDetails.add( sysExChannel, constrRight );
		}
		
		// sub ID 1 byte
		Object subId1Obj = messageSource.getOption( IMessageType.OPT_SUB_ID_1 );
		if ( subId1Obj instanceof String ) {
			
			// label
			constrLeft.gridy++;
			JLabel lblSubId = new JLabel( Dict.get(Dict.MSG_DETAILS_SUB_ID_1) );
			msgDetails.add( lblSubId, constrLeft );
			
			// content
			constrRight.gridy++;
			String subIdStr = (String) subId1Obj;
			FlowLabel subId = new FlowLabel( subIdStr, CPL_MSG_DETAILS, PWIDTH_MSG_DETAIL_CONTENT );
			subId.setBackground( bgColor );
			msgDetails.add( subId, constrRight );
		}
		
		// sub ID 2 byte
		Object subId2Obj = messageSource.getOption( IMessageType.OPT_SUB_ID_2 );
		if ( subId2Obj instanceof String ) {
			
			// label
			constrLeft.gridy++;
			JLabel lblSubId = new JLabel( Dict.get(Dict.MSG_DETAILS_SUB_ID_2) );
			msgDetails.add( lblSubId, constrLeft );
			
			// content
			constrRight.gridy++;
			String subIdStr = (String) subId2Obj;
			FlowLabel subId = new FlowLabel( subIdStr, CPL_MSG_DETAILS, PWIDTH_MSG_DETAIL_CONTENT );
			subId.setBackground( bgColor );
			msgDetails.add( subId, constrRight );
		}
		
		// controller byte
		Object ctrlObj = messageSource.getOption( IMessageType.OPT_CONTROLLER );
		if ( ctrlObj instanceof Byte ) {
			
			// label
			constrLeft.gridy++;
			JLabel lblCtrl = new JLabel( Dict.get(Dict.MSG_DETAILS_CTRL_BYTE) );
			msgDetails.add( lblCtrl, constrLeft );
			
			// content
			constrRight.gridy++;
			byte   ctrlByte = (Byte) ctrlObj;
			String ctrlStr  = String.format( "%02X", ctrlByte );
			FlowLabel ctrl  = new FlowLabel( "0x" + ctrlStr, CPL_MSG_DETAILS, PWIDTH_MSG_DETAIL_CONTENT );
			ctrl.setBackground( bgColor );
			msgDetails.add( ctrl, constrRight );
		}
		
		// RPN - parameter number(s)
		Object rpnObj = messageSource.getDistinctOptions( IMessageType.OPT_RPN );
		if ( rpnObj != null ) {
			
			// label
			constrLeft.gridy++;
			JLabel lblRpn = new JLabel( translRpnByte );
			msgDetails.add( lblRpn, constrLeft );
			
			// content
			constrRight.gridy++;
			String rpnStr = (String) rpnObj;
			FlowLabel rpn = new FlowLabel( "0x" + rpnStr, CPL_MSG_DETAILS, PWIDTH_MSG_DETAIL_CONTENT );
			rpn.setBackground( bgColor );
			msgDetails.add( rpn, constrRight );
		}
		
		// NRPN - parameter number(s)
		Object nrpnObj = messageSource.getDistinctOptions( IMessageType.OPT_NRPN );
		if ( nrpnObj != null ) {
			
			// label
			constrLeft.gridy++;
			JLabel lblNrpn = new JLabel( translNrpnByte );
			msgDetails.add( lblNrpn, constrLeft );
			
			// content
			constrRight.gridy++;
			String nrpnStr = (String) nrpnObj;
			FlowLabel nrpn = new FlowLabel( "0x" + nrpnStr, CPL_MSG_DETAILS, PWIDTH_MSG_DETAIL_CONTENT );
			nrpn.setBackground( bgColor );
			msgDetails.add( nrpn, constrRight );
		}
		
		// text content
		Object textObj = messageSource.getDistinctOptions( IMessageType.OPT_TEXT, "\n============\n" );
		if ( textObj instanceof String ) {
			
			// label
			constrLeft.gridy++;
			JLabel lblText = new JLabel( translText );
			msgDetails.add( lblText, constrLeft );
			
			// content
			constrRight.gridy++;
			String textStr = (String) textObj;
			FlowLabel text = new FlowLabel( textStr, CPL_MSG_DETAILS, PWIDTH_MSG_DETAIL_CONTENT );
			text.setBackground( bgColor );
			msgDetails.add( text, constrRight );
		}
		
		// detail/meaning
		Object meaningObj = messageSource.getOption( IMessageType.OPT_MEANING );
		if ( meaningObj != null ) {

			// label
			constrLeft.gridy++;
			JLabel lblMsg = new JLabel( Dict.get(Dict.MSG_DETAILS_MEANING) );
			msgDetails.add( lblMsg, constrLeft );

			// content
			constrRight.gridy++;
			String meaning = (String) meaningObj;
			
			FlowLabel msg = new FlowLabel( meaning, CPL_MSG_DETAILS, PWIDTH_MSG_DETAIL_CONTENT );
			msg.setBackground( bgColor );
			msgDetails.add( msg, constrRight );
		}
		
		// complete message
		Object msgObj = messageSource.getOption( IMessageType.OPT_MESSAGE );
		if ( msgObj != null ) {

			// label
			constrLeft.gridy++;
			JLabel lblMsg = new JLabel( Dict.get(Dict.MSG_DETAILS_MESSAGE) );
			msgDetails.add( lblMsg, constrLeft );

			// content
			constrRight.gridy++;
			byte[]        msgBytes = (byte[]) msgObj;
			StringBuilder msgStr   = new StringBuilder( "" );
			for ( int i = 0; i < msgBytes.length; i++ ) {
				String hex = String.format( "%02X", msgBytes[i] );
				if ( 0 == i )
					msgStr.append( hex );
				else
					msgStr.append( " " + hex );
			}
			FlowLabel msg = new FlowLabel( msgStr.toString(), CPL_MSG_DETAILS, PWIDTH_MSG_DETAIL_CONTENT );
			msg.setBackground( bgColor );
			msgDetails.add( msg, constrRight );
		}
		
		// spacer
		constrLeft.gridy++;
		constrLeft.weighty = 1;
		JLabel spacer = new JLabel( " " );
		msgDetails.add( spacer, constrLeft );
		
		// make the changes visible
		msgDetails.revalidate();
		msgDetails.repaint();
	}
	
	/**
	 * Closes and destroys the info window.
	 */
	public void close() {
		setVisible( false );
		dispose();
		infoView = null;
	}
	
	// TODO: add key bindings for nested tabs
	/**
	 * Creates key bindings for the info view and adds them
	 * to the {@link java.awt.KeyboardFocusManager}.
	 * The following key bindings are created:
	 * 
	 * - **ESC** -- close the window
	 * - **N**   -- switch to the note names translation tab
	 * - **P**   -- switch to the percussion shortcut translation tab
	 * - **S**   -- switch to the syntax keyword definition tab
	 * - **I**   -- switch to the instrument shortcut translation tab
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
							close();
							return true;
						}
						
						else if ( KeyEvent.VK_N == e.getKeyCode() ) {
							content.setSelectedIndex( 0 );
							contentConfig.setSelectedIndex( 0 );
						}
						
						else if ( KeyEvent.VK_P == e.getKeyCode() ) {
							content.setSelectedIndex( 0 );
							contentConfig.setSelectedIndex( 1 );
						}
						
						else if ( KeyEvent.VK_S == e.getKeyCode() ) {
							content.setSelectedIndex( 0 );
							contentConfig.setSelectedIndex( 2 );
						}
						
						else if ( KeyEvent.VK_I == e.getKeyCode() ) {
							content.setSelectedIndex( 0 );
							contentConfig.setSelectedIndex( 3 );
						}
					}
					return e.isConsumed();
				}
			};
		}
		
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventPostProcessor( keyProcessor );
	}
	
	/**
	 * Removes the key bindings for the info view
	 * from the {@link java.awt.KeyboardFocusManager}.
	 */
	public void removeKeyBindings() {
		KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventPostProcessor( keyProcessor );
	}
}
