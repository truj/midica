/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;

import org.midica.config.Config;
import org.midica.config.ConfigView;
import org.midica.config.Dict;
import org.midica.file.ExportException;
import org.midica.file.ExportResult;
import org.midica.file.MidiExporter;
import org.midica.file.MidiParser;
import org.midica.file.MidicaPLExporter;
import org.midica.file.ParseException;
import org.midica.file.Parser;
import org.midica.file.MidicaPLParser;
import org.midica.file.SoundbankParser;
import org.midica.midi.MidiDevices;
import org.midica.ui.model.ComboboxStringOption;
import org.midica.ui.player.PlayerView;

/**
 * This class provides the controller of the main window.
 * 
 * It listens and reacts to events in the {@link UiView}.
 * 
 * @author Jan Trukenmüller
 */
public class UiController implements ActionListener, WindowListener {
	
	public static final String CMD_COMBOBOX_CHANGED = "comboBoxChanged";
	public static final String CMD_FILE_CHOSEN      = "ApproveSelection";
	public static final String CMD_CANCELED         = "CancelSelection";
	public static final String FILE_PURPOSE_PARSE   = "parse";
	public static final String FILE_PURPOSE_EXPORT  = "export";
	
	private UiView           view               = null;
	private FileSelector     mplSelector        = null;
	private FileSelector     midiSelector       = null;
	private FileSelector     soundbankSelector  = null;
	private FileSelector     midiExportSelector = null;
	private FileSelector     mplExportSelector  = null;
	private MidicaPLParser   mplParser          = null;
	private MidiParser       midiParser         = null;
	private SoundbankParser  soundbankParser    = null;
	private ErrorMsgView     errorMsg           = null;
	private ExportResultView warningView        = null;
	private PlayerView       player             = null;
	private File             currentFile        = null;
	private String           currentFileType    = null;
	private String           currentFilePurpose = FILE_PURPOSE_PARSE;
	
	/**
	 * Sets up the UI of the main window by initializing the {@link UiView}
	 * and the parser classes.
	 */
	public UiController() {
		initView();
		
		mplParser       = new MidicaPLParser( true );
		midiParser      = new MidiParser();
		soundbankParser = new SoundbankParser();
	}
	
	/**
	 * Initializes the main window by creating the {@link UiView} object and all possible types
	 * of {@link FileSelector}s.
	 * 
	 * This method is also called after changing the language in order to re-draw everything.
	 */
	private void initView() {
		view = new UiView( this );
		mplSelector = new FileSelector( view, this );
		mplSelector.init( FileSelector.FILE_EXTENSION_MPL, FileSelector.READ );
		midiSelector = new FileSelector( view, this );
		midiSelector.init( FileSelector.FILE_EXTENSION_MIDI, FileSelector.READ );
		soundbankSelector = new FileSelector( view, this );
		soundbankSelector.init( FileSelector.FILE_EXTENSION_SOUNDBANK, FileSelector.READ );
		midiExportSelector = new FileSelector( view, this );
		midiExportSelector.init( FileSelector.FILE_EXTENSION_MIDI, FileSelector.WRITE );
		mplExportSelector = new FileSelector( view, this );
		mplExportSelector.init( FileSelector.FILE_EXTENSION_MPL, FileSelector.WRITE );
	}
	
	/**
	 * Returns the view of the main window.
	 * 
	 * @return main window view.
	 */
	public UiView getView() {
		return this.view;
	}
	
	/**
	 * This method processes all action events in the {@link UiView}.
	 */
	public void actionPerformed( ActionEvent e ) {
		String cmd = e.getActionCommand();
		
		// combobox changes
		if ( CMD_COMBOBOX_CHANGED.equals(cmd) ) {
			JComboBox<String> cbx               = (JComboBox<String>) e.getSource();
			String name                         = cbx.getName();
			DefaultComboBoxModel<String> model  = (DefaultComboBoxModel<String>) cbx.getModel();
			ComboboxStringOption selectedOption = (ComboboxStringOption) model.getSelectedItem();
			String selectedId                   = selectedOption.getIdentifier();
			
			// apply the changes
			configurationChanged( name, selectedId );
		}
		
		// button pressed: open MidicaPL file
		else if ( UiView.CMD_OPEN_MIDICAPL_FILE.equals(cmd) ) {
			currentFilePurpose = FILE_PURPOSE_PARSE;
			mplSelector.setVisible( true );
		}
		
		// button pressed: open MIDI file
		else if ( UiView.CMD_OPEN_MIDI_FILE.equals(cmd) ) {
			currentFilePurpose = FILE_PURPOSE_PARSE;
			midiSelector.setVisible( true );
		}
		
		// button pressed: open soundbank file
		else if ( UiView.CMD_OPEN_SNDBNK_FILE.equals(cmd) ) {
			currentFilePurpose = FILE_PURPOSE_PARSE;
			soundbankSelector.setVisible( true );
		}
		
		// button pressed: export MIDI file
		else if ( UiView.CMD_EXPORT_MIDI.equals(cmd) ) {
			openExportFileSelector( cmd );
		}
		
		// button pressed: export MidicaPL file
		else if ( UiView.CMD_EXPORT_MIDICAPL.equals(cmd) ) {
			openExportFileSelector( cmd );
		}
		
		// file chosen with the file selector
		else if ( CMD_FILE_CHOSEN.equals(cmd) ) {
			if ( currentFilePurpose.equals(FILE_PURPOSE_EXPORT) )
				exportChosenFile( e );
			else
				parseChosenFile( e );
		}
		
		// cancel or ESC in FileChooser pressed
		else if ( CMD_CANCELED.equals(cmd) ) {
			mplSelector.setVisible( false );
			midiSelector.setVisible( false );
			soundbankSelector.setVisible( false );
			midiExportSelector.setVisible( false );
			mplExportSelector.setVisible( false );
		}
		
		// close button pressed in the the error or warning message window
		// or ESC/Enter/Space pressed to close the window
		else if ( MessageView.CMD_CLOSE.equals(cmd) ) {
			if ( null != errorMsg )
				errorMsg.close();
			if ( null != warningView )
				warningView.close();
		}
		
		// button pressed: start player
		else if ( UiView.CMD_START_PLAYER.equals(cmd) ) {
			if ( MidiDevices.isSequenceSet() ) {
				if ( FileSelector.FILE_EXTENSION_MIDI.equals(currentFileType) )
					player = new PlayerView( view, midiParser, currentFile );
				else
					player = new PlayerView( view, mplParser, currentFile );
			}
			else {
				showErrorMessage( Dict.get(Dict.ERROR_SEQUENCE_NOT_SET) );
			}
		}
		
		// button pressed: show config details
		else if ( UiView.CMD_SHOW_CONFIG_DETAILS.equals(cmd) ) {
			ConfigView.showConfig();
		}
		
		// not yet implemented function used
		else if ( UiView.CMD_OPEN_FCT_NOT_READY.equals(cmd) ) {
			showErrorMessage( Dict.get(Dict.ERROR_NOT_YET_IMPLEMENTED) );
		}
	}
	
	/**
	 * Checks if a MIDI sequence is set and opens the file selector to choose
	 * the file to be exported.
	 * 
	 * If no MIDI sequence is set, shows an error message instead of opening the file selector.
	 * 
	 * @param cmd The action command of the pressed button.
	 */
	private void openExportFileSelector ( String cmd ) {
		
		if ( ! MidiDevices.isSequenceSet() ) {
			showErrorMessage( Dict.get(Dict.ERROR_SEQUENCE_NOT_SET) );
			return;
		}
		
		currentFilePurpose = FILE_PURPOSE_EXPORT;
		if ( UiView.CMD_EXPORT_MIDI.equals(cmd) )
			midiExportSelector.setVisible( true );
		else if ( UiView.CMD_EXPORT_MIDICAPL.equals(cmd) )
			mplExportSelector.setVisible( true );
	}
	
	/**
	 * Parses the chosen file using the right parser for the right file type.
	 * 
	 * @param e    Event caused by choosing a file.
	 */
	private void parseChosenFile( ActionEvent e ) {
		String type = ((FileExtensionFilter)((JFileChooser)e.getSource()).getFileFilter()).getExtension();
		File         file;
		Parser       parser;
		FileSelector selector;
		if ( FileSelector.FILE_EXTENSION_MPL.equals(type) ) {
			file     = mplSelector.getFile();
			parser   = mplParser;
			selector = mplSelector;
		}
		else if ( FileSelector.FILE_EXTENSION_MIDI.equals(type) ) {
			file     = midiSelector.getFile();
			parser   = midiParser;
			selector = midiSelector;
		}
		else if ( FileSelector.FILE_EXTENSION_SOUNDBANK.equals(type) ) {
			file     = soundbankSelector.getFile();
			parser   = soundbankParser;
			selector = soundbankSelector;
		}
		else {
			return;
		}
		selector.setVisible( false );
		try {
			displayFilename( type, Dict.get(Dict.UNCHOSEN_FILE) );
			parser.parse( file );
			// show the filename of the successfully parsed file
			displayFilename( type, file.getName() );
			if ( FileSelector.FILE_EXTENSION_MPL.equals(type)
			  || FileSelector.FILE_EXTENSION_MIDI.equals(type) ) {
				// store the file for later reparsing
				currentFile     = file;
				currentFileType = type;
			}
		}
		catch ( ParseException ex ) {
			int    lineNumber = ex.getLineNumber();
			String fileName   = ex.getFileName();
			String msg = ex.getMessage();
			if ( lineNumber > 0 && null != fileName ) {
				msg = String.format(
					Dict.get( Dict.ERROR_IN_LINE ),
					fileName,
					lineNumber
				) + msg;
			}
			showErrorMessage( msg );
			// reset sequence
			MidiDevices.setSequence( null );
		}
	}
	
	/**
	 * Exports to the chosen file using the right exporter for this file type.
	 * 
	 * @param e    Event caused by choosing a file.
	 */
	private void exportChosenFile( ActionEvent e ) {
		String type = ((FileExtensionFilter)((JFileChooser)e.getSource()).getFileFilter()).getExtension();
		File         file;
		FileSelector selector;
		boolean      mustExportMidi;
		if ( FileSelector.FILE_EXTENSION_MPL.equals(type) ) {
			file           = mplExportSelector.getFile();
			selector       = mplExportSelector;
			mustExportMidi = false;
		}
		else if ( FileSelector.FILE_EXTENSION_MIDI.equals(type) ) {
			file           = midiExportSelector.getFile();
			selector       = midiExportSelector;
			mustExportMidi = true;
		}
		else {
			return;
		}
		selector.setVisible( false );
		try {
			ExportResult result;
			if (mustExportMidi) {
				MidiExporter exporter = new MidiExporter();
				result = exporter.export( file );
			}
			else {
				MidicaPLExporter exporter = new MidicaPLExporter();
				result = exporter.export( file );
			}
			if ( result.isSuccessful() )
				showExportResult( result );
		}
		catch ( ExportException ex ) {
			showErrorMessage( ex.getErrorMessage() );
		}
	}
	
	/**
	 * Opens an error message window showing the given message.
	 * 
	 * @param message    Error message.
	 */
	private void showErrorMessage( String message ) {
		// errorMsg.init cannot be invoked by 'new ErrorMsgView()' because it's modal.
		// That means setVisible() is blocking. So 'new' would not return until the
		// error message window is closed.
		// errorMsg.close() would cause a NullPointerException.
		// The solution is to call setVisible() from 'errorMsg.init(..)' and
		// to call 'errorMsg.init(..)' separately after 'new' has returned and therefore
		// errorMsg is not null any more.
		errorMsg = new ErrorMsgView( view, this );
		errorMsg.init( message );
	}
	
	/**
	 * Opens an export result window showing the given export result.
	 * 
	 * @param result    Export result containing warnings and errors of a file export.
	 */
	private void showExportResult ( ExportResult result ) {
		// same problem like in showErrorMessage()
		warningView = new ExportResultView( view, this );
		warningView.init( result );
	}
	
	/**
	 * Displays the imported file name in the right field (according to the file type).
	 * 
	 * @param type        File extension.
	 * @param filename    File name.
	 */
	private void displayFilename( String type, String filename ) {
		if ( FileSelector.FILE_EXTENSION_MPL.equals(type) ) {
			// show MidicaPL file
			view.getChosenMidicaPLFileLbl().setText( filename );
			// hide Midi file
			view.getChosenMidiFileLbl().setText( Dict.get(Dict.UNCHOSEN_FILE) );
		}
		else if ( FileSelector.FILE_EXTENSION_MIDI.equals(type) ) {
			// show Midi file
			view.getChosenMidiFileLbl().setText( filename );
			// hide MidicaPL file
			view.getChosenMidicaPLFileLbl().setText( Dict.get(Dict.UNCHOSEN_FILE) );
		}
		else if ( FileSelector.FILE_EXTENSION_SOUNDBANK.equals(type) )
			// show soundbank file
			view.getChosenSoundbankFileLbl().setText( filename );
	}
	
	/**
	 * Initializes key bindings and refreshes the current transpose level that has been set
	 * in the player (or 0, if the transpose level has not yet been changed).
	 * 
	 * @param e    Window activation event.
	 */
	public void windowActivated( WindowEvent e ) {
		view.setTransposeLevel( Parser.getTransposeLevel() );
		view.addKeyBindings();
	}
	
	/**
	 * Removes all key bindings.
	 * 
	 * @param e    Window closed event.
	 */
	public void windowClosed( WindowEvent e ) {
		view.removeKeyBindings();
	}
	
	/**
	 * Removes all key bindings, writes the current config to the config file, and exits.
	 * 
	 * @param e    Window closing event.
	 */
	public void windowClosing( WindowEvent e ) {
		view.removeKeyBindings();
		Config.writeConfigFile();
		System.exit( 0 );
	}
	
	/**
	 * Removes all key bindings.
	 * 
	 * @param e    Window closed event.
	 */
	public void windowDeactivated( WindowEvent e ) {
		view.removeKeyBindings();
	}
	
	/**
	 * Does nothing.
	 * 
	 * @param e    Window closed event.
	 */
	public void windowDeiconified( WindowEvent e ) {
	}
	
	/**
	 * Does nothing.
	 * 
	 * @param e    Window closed event.
	 */
	public void windowIconified( WindowEvent e ) {
	}
	
	/**
	 * Does nothing.
	 * 
	 * @param e    Window closed event.
	 */
	public void windowOpened( WindowEvent e ) {
	}
	
	/**
	 * Applies configuration changes.
	 * 
	 * This method is called whenever one of the configuration comboboxes is changed.
	 * 
	 * @param name          Name of the combobox that has been changed.
	 * @param selectedId    ID of the selected item in the combobox.
	 */
	private void configurationChanged( String name, String selectedId ) {
		String type         = null;
		String configuredId = null;
		
		// language selected
		if ( UiView.NAME_SELECT_LANGUAGE.equals(name) ) {
			
			// store new configuration
			type         = Config.LANGUAGE;
			configuredId = Config.get( type );
			Config.set( type, selectedId );
			
			// check if there has been a change we have to care about
			if ( ! selectedId.equals(configuredId) ) {
				
				// update gui
				view.close();
				Dict.init();
				initView();
			}
		}
		
		// note system selected
		else if ( UiView.NAME_SELECT_SYSTEM.equals(name) ) {
			
			// store new configuration
			type         = Config.NOTE;
			configuredId = Config.get( type );
			Config.set( type, selectedId );
			
			// check if there has been a change we have to care about
			if ( ! selectedId.equals(configuredId) ) {
				
				// update note system
				Dict.initNoteSystem();
			}
		}
		
		// half tones selected
		else if ( UiView.NAME_SELECT_HALF_TONE.equals(name) ) {
			
			// store new configuration
			type         = Config.HALF_TONE;
			configuredId = Config.get( type );
			Config.set( type, selectedId );
			
			// check if there has been a change we have to care about
			if ( ! selectedId.equals(configuredId) ) {
				
				// update half tone
				Dict.initHalfTones();
			}
		}
		
		// octave naming selected
		else if ( UiView.NAME_SELECT_OCTAVE.equals(name) ) {
			
			// store new configuration
			type         = Config.OCTAVE;
			configuredId = Config.get( type );
			Config.set( type, selectedId );
			
			// check if there has been a change we have to care about
			if ( ! selectedId.equals(configuredId) ) {
				
				// update note system
				Dict.initOctaves();
			}
		}
		
		// syntax selected
		else if ( UiView.NAME_SELECT_SYNTAX.equals(name) ) {
			
			// store new configuration
			type         = Config.SYNTAX;
			configuredId = Config.get( type );
			Config.set( type, selectedId );
			
			// check if there has been a change we have to care about
			if ( ! selectedId.equals(configuredId) ) {
				
				// update syntax
				Dict.initSyntax();
			}
		}
		
		// percussion selected
		else if ( UiView.NAME_SELECT_PERCUSSION.equals(name) ) {
			
			// store new configuration
			type         = Config.PERCUSSION;
			configuredId = Config.get( type );
			Config.set( type, selectedId );
			
			// check if there has been a change we have to care about
			if ( ! selectedId.equals(configuredId) ) {
				
				// update percussion
				Dict.initPercussion();
			}
		}
		
		// instruments selected
		else if ( UiView.NAME_SELECT_INSTRUMENT.equals(name) ) {
			
			// store new configuration
			type         = Config.INSTRUMENT;
			configuredId = Config.get( type );
			Config.set( type, selectedId );
			
			// check if there has been a change we have to care about
			if ( ! selectedId.equals(configuredId) ) {
				
				// update instruments
				Dict.initInstruments();
			}
		}
		
		// show new feedback
		view.refreshConfigFeedback();
		
		// refresh syntax in the parser
		MidicaPLParser.refreshSyntax();
	}
}
