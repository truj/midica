/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.config;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyEventPostProcessor;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

import org.midica.Midica;
import org.midica.ui.model.InstrumentTableModel;
import org.midica.ui.model.NoteTableModel;
import org.midica.ui.model.PercussionTableModel;
import org.midica.ui.model.SyntaxTableModel;

/**
 * This class defines the GUI view for the configuration overview window.
 * The window shows the currently configured values for different configurable elements.
 * It contains areas and tables for:
 * 
 * - Note names
 * - Percussion instrument shortcuts
 * - Syntax keywords for MidicaPL
 * - Instrument shortcuts for non-percussion instruments
 * 
 * @author Jan Trukenmüller
 */
public class ConfigView extends JDialog {
	
	private static final long serialVersionUID = 1L;
	
	// column widths
	private static final int COL_WIDTH_NOTE_NUM       =  60;
	private static final int COL_WIDTH_NOTE_NAME      = 140;
	private static final int COL_WIDTH_PERC_NUM       =  60;
	private static final int COL_WIDTH_PERC_NAME      = 140;
	private static final int COL_WIDTH_SYNTAX_NAME    = 180;
	private static final int COL_WIDTH_SYNTAX_DESC    = 230;
	private static final int COL_WIDTH_SYNTAX_KEYWORD = 130;
	private static final int COL_WIDTH_INSTR_NUM      =  60;
	private static final int COL_WIDTH_INSTR_NAME     = 180;
	private static final int TABLE_HEIGHT             = 400;
	
	private static Dimension noteTableDim   = null;
	private static Dimension percTableDim   = null;
	private static Dimension syntaxTableDim = null;
	private static Dimension instrTableDim  = null;
	
	private static ConfigView configView = null;
	
	private KeyEventPostProcessor keyProcessor = null;
	private JTabbedPane           content      = null;
	
	/**
	 * Creates a new config view window with owner = null.
	 */
	private ConfigView() {
		this( (JDialog) null );
	}
	
	/**
	 * Creates a new config view window and sets the given owner Dialog.
	 * 
	 * @param owner  window to be set as the config view's owner
	 */
	private ConfigView( JDialog owner ) {
		super( owner );
		setTitle( Dict.get(Dict.TITLE_CONFIG_VIEW) );
		
		int noteWidth   = COL_WIDTH_NOTE_NUM    + COL_WIDTH_NOTE_NAME;
		int percWidth   = COL_WIDTH_PERC_NUM    + COL_WIDTH_PERC_NAME;
		int syntaxWidth = COL_WIDTH_SYNTAX_NAME + COL_WIDTH_SYNTAX_KEYWORD + COL_WIDTH_SYNTAX_DESC;
		int instrWidth  = COL_WIDTH_INSTR_NUM   + COL_WIDTH_INSTR_NAME;
		noteTableDim   = new Dimension( noteWidth,   TABLE_HEIGHT );
		percTableDim   = new Dimension( percWidth,   TABLE_HEIGHT );
		syntaxTableDim = new Dimension( syntaxWidth, TABLE_HEIGHT );
		instrTableDim  = new Dimension( instrWidth,  TABLE_HEIGHT );
		
		init();
		
		pack();
		setVisible( true );
	}
	
	/**
	 * Initializes the content of all the tabs inside the config view.
	 */
	private void init() {
		// content
		content = new JTabbedPane( JTabbedPane.TOP );
		getContentPane().add( content );
		
		// add tabs
		content.addTab( Dict.get(Dict.NOTE_DETAILS),       createNoteArea()       );
		content.addTab( Dict.get(Dict.PERCUSSION_DETAILS), createPercussionArea() );
		content.addTab( Dict.get(Dict.SYNTAX),             createSyntaxArea()     );
		content.addTab( Dict.get(Dict.INSTRUMENT),         createInstrumentArea() );
		content.addTab( Dict.get(Dict.VERSION),            createVersionArea()    );
		
		// enable key bindings
		ConfigController controller = new ConfigController( this );
		addWindowListener( controller );
	}
	
	/**
	 * Creates the note area containing the translation table for note names.
	 * The table translates between MIDI note values and their configured names.
	 * 
	 * @return the created note area
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
		constraints.weighty    = 1;
		
		// label
		JLabel label = new JLabel( Dict.get(Dict.NOTE_DETAILS) );
		area.add( label, constraints );
		
		// table
		constraints.gridy++;
		JTable table = new JTable();
		table.setModel( new NoteTableModel() );
		JScrollPane scroll = new JScrollPane( table );
		scroll.setPreferredSize( noteTableDim );
		scroll.setMinimumSize( noteTableDim );
		area.add( scroll, constraints );
		
		// set column sizes and colors
		table.getColumnModel().getColumn( 0 ).setPreferredWidth( COL_WIDTH_NOTE_NUM  );
		table.getColumnModel().getColumn( 1 ).setPreferredWidth( COL_WIDTH_NOTE_NAME );
		table.getTableHeader().setBackground( Config.TABLE_HEADER_COLOR );
		
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
		constraints.weighty    = 1;
		
		// label
		JLabel label = new JLabel( Dict.get(Dict.PERCUSSION_DETAILS) );
		area.add( label, constraints );
		
		// table
		constraints.gridy++;
		JTable table = new JTable();
		table.setModel( new PercussionTableModel() );
		JScrollPane scroll = new JScrollPane( table );
		scroll.setPreferredSize( percTableDim );
		scroll.setMinimumSize( percTableDim );
		area.add( scroll, constraints );
		
		// set column sizes
		table.getColumnModel().getColumn( 0 ).setPreferredWidth( COL_WIDTH_PERC_NUM  );
		table.getColumnModel().getColumn( 1 ).setPreferredWidth( COL_WIDTH_PERC_NAME );
		table.getTableHeader().setBackground( Config.TABLE_HEADER_COLOR );
		
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
		constraints.weighty    = 1;
		
		// label
		JLabel label = new JLabel( Dict.get(Dict.SYNTAX) );
		area.add( label, constraints );
		
		// table
		constraints.gridy++;
		JTable table = new JTable();
		table.setModel( new SyntaxTableModel() );
		table.setDefaultRenderer( Object.class, new SyntaxTableCellRenderer() );
		JScrollPane scroll = new JScrollPane( table );
		scroll.setPreferredSize( syntaxTableDim );
		scroll.setMinimumSize( syntaxTableDim );
		area.add( scroll, constraints );
		
		// set column sizes
		table.getColumnModel().getColumn( 0 ).setPreferredWidth( COL_WIDTH_SYNTAX_NAME    );
		table.getColumnModel().getColumn( 1 ).setPreferredWidth( COL_WIDTH_SYNTAX_KEYWORD );
		table.getColumnModel().getColumn( 2 ).setPreferredWidth( COL_WIDTH_SYNTAX_DESC    );
		table.getTableHeader().setBackground( Config.TABLE_HEADER_COLOR );
		
		return area;
	}
	
	/**
	 * Creates the instrument area containing the translation table for instrument names.
	 * The table translates between MIDI instrument values and their configured names.
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
		constraints.weighty    = 1;
		
		// label
		JLabel label = new JLabel( Dict.get(Dict.INSTRUMENT) );
		area.add( label, constraints );
		
		// table
		constraints.gridy++;
		JTable table = new JTable();
		table.setModel( new InstrumentTableModel() );
		table.setDefaultRenderer( Object.class, new InstrumentTableCellRenderer() );
		JScrollPane scroll = new JScrollPane( table );
		scroll.setPreferredSize( instrTableDim );
		scroll.setMinimumSize( instrTableDim );
		area.add( scroll, constraints );
		
		// set column sizes
		table.getColumnModel().getColumn( 0 ).setPreferredWidth( COL_WIDTH_INSTR_NUM  );
		table.getColumnModel().getColumn( 1 ).setPreferredWidth( COL_WIDTH_INSTR_NAME );
		table.getTableHeader().setBackground( Config.TABLE_HEADER_COLOR );
		
		return area;
	}
	
	// TODO: define and document
	private Container createVersionArea() {
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
		
		// version translation
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
		DateTimeFormatter formatter  = DateTimeFormatter.ofPattern( Dict.get(Dict.TIMESTAMP_FORMAT) );
		ZoneId            zoneId     = ZoneId.of( "GMT+1" );
		String            timestamp  = Instant.ofEpochSecond( Midica.VERSION_MINOR ).atZone( zoneId ).format( formatter );
		JLabel lblTimestampContent   = new JLabel( timestamp );
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
	 * Creates and shows the config view passing **owner = null** to the constructor.
	 * This is done by calling {@link #showConfig(JDialog)} with parameter **null**.
	 */
	public static void showConfig() {
		showConfig( null );
	}
	
	/**
	 * Creates and shows the config view passing the specified owner window to the constructor.
	 * 
	 * If a config view already exists it will be destroyed before.
	 * This is done in order to make sure that the newly created config view:
	 * 
	 * - exists only once
	 * - will be in the foreground
	 * - will be focused
	 * 
	 * @param owner  The GUI window owning the config window.
	 */
	public static void showConfig( JDialog owner ) {
		
		// it cannot be guaranteed that the config view can be focused
		// so we have to destroy and rebuild it
		if ( null != configView )
			configView.close();
		configView = new ConfigView( owner );
	}
	
	/**
	 * Closes and destroys the configuration window.
	 */
	public void close() {
		setVisible( false );
		dispose();
		configView = null;
	}
	
	/**
	 * Creates key bindings for the config view and adds them
	 * to the {@link java.awt.KeyboardFocusManager}.
	 * The following key bindings are created:
	 * 
	 * - **ESC** - close the window
	 * - **N** - switch to the note names translation tab
	 * - **P** - switch to the percussion shortcut translation tab
	 * - **S** - switch to the syntax keyword definition tab
	 * - **I** - switch to the instrument shortcut translation tab
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
						}
						
						else if ( KeyEvent.VK_P == e.getKeyCode() ) {
							content.setSelectedIndex( 1 );
						}
						
						else if ( KeyEvent.VK_S == e.getKeyCode() ) {
							content.setSelectedIndex( 2 );
						}
						
						else if ( KeyEvent.VK_I == e.getKeyCode() ) {
							content.setSelectedIndex( 3 );
						}
					}
					return e.isConsumed();
				}
			};
		}
		
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventPostProcessor( keyProcessor );
	}
	
	/**
	 * Removes the key bindings for the config view
	 * from the {@link java.awt.KeyboardFocusManager}.
	 */
	public void removeKeyBindings() {
		KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventPostProcessor( keyProcessor );
	}
}
