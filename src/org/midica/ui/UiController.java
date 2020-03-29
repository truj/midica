/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.concurrent.ExecutionException;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.midica.config.Cli;
import org.midica.config.Config;
import org.midica.config.Dict;
import org.midica.file.read.AbcImporter;
import org.midica.file.read.AldaImporter;
import org.midica.file.read.IParser;
import org.midica.file.read.LilypondImporter;
import org.midica.file.read.MidiParser;
import org.midica.file.read.MidicaPLParser;
import org.midica.file.read.ParseException;
import org.midica.file.read.SequenceParser;
import org.midica.file.read.SoundfontParser;
import org.midica.file.write.AldaExporter;
import org.midica.file.write.ExportException;
import org.midica.file.write.Exporter;
import org.midica.file.write.MidiExporter;
import org.midica.file.write.MidicaPLExporter;
import org.midica.midi.MidiDevices;
import org.midica.ui.file.ExportResult;
import org.midica.ui.file.ExportResultView;
import org.midica.ui.file.FileExtensionFilter;
import org.midica.ui.file.FileSelector;
import org.midica.ui.info.InfoView;
import org.midica.ui.model.ComboboxStringOption;
import org.midica.ui.model.ConfigComboboxModel;
import org.midica.ui.player.PlayerView;
import org.midica.worker.ParsingWorker;
import org.midica.worker.WaitView;

/**
 * This class provides the controller of the main window.
 * 
 * It listens and reacts to events in the {@link UiView}.
 * 
 * @author Jan Trukenmüller
 */
public class UiController implements ActionListener, WindowListener, ItemListener {
	
	public static final String CMD_COMBOBOX_CHANGED = "comboBoxChanged";
	public static final String CMD_FILE_CHOSEN      = "ApproveSelection";
	public static final String CMD_CANCELED         = "CancelSelection";
	public static final String FILE_PURPOSE_PARSE   = "parse";
	public static final String FILE_PURPOSE_EXPORT  = "export";
	
	private UiView           view                    = null;
	private FileSelector     importSelector          = null;
	private FileSelector     soundfontSelector       = null;
	private FileSelector     exportSelector          = null;
	private MidicaPLParser   mplParser               = null;
	private MidiParser       midiParser              = null;
	private AldaImporter     aldaImporter            = null;
	private AbcImporter      abcImporter             = null;
	private LilypondImporter lyImporter              = null;
	private SoundfontParser  soundfontParser         = null;
	private PlayerView       player                  = null;
	private File             currentFile             = null;
	private String           currentFileType         = null;
	private String           currentFilePurpose      = FILE_PURPOSE_PARSE;
	
	/**
	 * Sets up the UI of the main window by initializing the {@link UiView}
	 * and the parser classes.
	 */
	public UiController() {
		mplParser       = new MidicaPLParser(true);
		midiParser      = new MidiParser();
		aldaImporter    = new AldaImporter();
		abcImporter     = new AbcImporter();
		lyImporter      = new LilypondImporter();
		soundfontParser = new SoundfontParser();
		
		// initView() must be called after the parsers are created.
		// Otherwise a null parser may be passed to the ParsingWorker's
		// constructor. This error would appear on startup with remembered
		// soundfont files in about 1 of 5 cases.
		initView();
	}
	
	/**
	 * Initializes the main window by creating the {@link UiView} object and all possible types
	 * of {@link FileSelector}s.
	 * 
	 * This method is also called after changing the language in order to re-draw everything.
	 */
	private void initView() {
		view = new UiView(this);
		initSelectorsIfNotYetDone();
	}
	
	/**
	 * Initializes all possible types of {@link FileSelector}s.
	 */
	private void initSelectorsIfNotYetDone() {
		if (null == importSelector) {
			importSelector = new FileSelector(view, this);
			importSelector.init(FileSelector.FILE_TYPE_MPL, FileSelector.READ);
		}
		if (null == soundfontSelector) {
			soundfontSelector = new FileSelector(view, this);
			soundfontSelector.init(FileSelector.FILE_TYPE_SOUNDFONT, FileSelector.READ);
		}
		if (null == exportSelector) {
			exportSelector = new FileSelector(view, this);
			exportSelector.init(FileSelector.FILE_TYPE_MIDI, FileSelector.WRITE);
		}
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
	 * Sets the soundfont file name label after loading a soundfont by MidicaPL source code.
	 */
	public void soundfontLoadedBySourceCode() {
		String text = Dict.get(Dict.SF_LOADED_BY_SOURCE);
		view.getChosenSoundfontFileLbl().setText(text);
	}
	
	/**
	 * This method processes all action events in the {@link UiView}.
	 * 
	 * @param e The event to be handled.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		
		// combobox changes
		if (CMD_COMBOBOX_CHANGED.equals(cmd)) {
			JComboBox<?> cbx                    = (JComboBox<?>) e.getSource();
			String name                         = cbx.getName();
			DefaultComboBoxModel<?> model       = (DefaultComboBoxModel<?>) cbx.getModel();
			ComboboxStringOption selectedOption = (ComboboxStringOption) model.getSelectedItem();
			String selectedId                   = selectedOption.getIdentifier();
			
			// apply the changes
			configurationChanged(name, selectedId);
		}
		
		// button pressed: open MidicaPL file
		else if (UiView.CMD_IMPORT.equals(cmd)) {
			currentFilePurpose = FILE_PURPOSE_PARSE;
			importSelector.setVisible(true);
		}
		
		// button pressed: open soundfont file
		else if (UiView.CMD_OPEN_SNDFNT_FILE.equals(cmd)) {
			currentFilePurpose = FILE_PURPOSE_PARSE;
			soundfontSelector.setVisible(true);
		}
		
		// button pressed: export file
		else if (UiView.CMD_EXPORT.equals(cmd)) {
			openExportFileSelector();
		}
		
		// file chosen with the file selector
		else if (CMD_FILE_CHOSEN.equals(cmd)) {
			
			JFileChooser chooser = (JFileChooser) e.getSource();
			String       type    = ((FileExtensionFilter) (chooser).getFileFilter()).getType();
			
			if (currentFilePurpose.equals(FILE_PURPOSE_EXPORT))
				exportChosenFile(type, chooser.getSelectedFile());
			else
				parseChosenFile(type, chooser.getSelectedFile());
		}
		
		// cancel or ESC in FileChooser pressed
		else if (CMD_CANCELED.equals(cmd)) {
			importSelector.setVisible(false);
			soundfontSelector.setVisible(false);
			exportSelector.setVisible(false);
		}
		
		// button pressed: start player
		else if (UiView.CMD_START_PLAYER.equals(cmd)) {
			if (MidiDevices.isSequenceSet()) {
				if (FileSelector.FILE_TYPE_MPL.equals(currentFileType))
					player = new PlayerView(view, mplParser, currentFile);
				else if (FileSelector.FILE_TYPE_MIDI.equals(currentFileType))
					player = new PlayerView(view, midiParser, currentFile);
				else if (FileSelector.FILE_TYPE_ALDA.equals(currentFileType))
					player = new PlayerView(view, aldaImporter, currentFile);
				else if (FileSelector.FILE_TYPE_ABC.equals(currentFileType))
					player = new PlayerView(view, abcImporter, currentFile);
				else if (FileSelector.FILE_TYPE_LY.equals(currentFileType))
					player = new PlayerView(view, lyImporter, currentFile);
			}
			else {
				showErrorMessage(Dict.get(Dict.ERROR_SEQUENCE_NOT_SET));
			}
		}
		
		// button pressed: show info view
		else if (UiView.CMD_SHOW_INFO_WINDOW.equals(cmd)) {
			InfoView.showInfoWindow(view);
		}
	}
	
	/**
	 * Handles selecting/deselecting of one of the remember checkboxes.
	 * 
	 * @param e The event to be handled.
	 */
	@Override
	public void itemStateChanged(ItemEvent e) {
		
		// get name, component and checked/unchecked
		String    name      = ((Component) e.getSource()).getName();
		JCheckBox cbx       = (JCheckBox) e.getSource();
		boolean   isChecked = cbx.isSelected();
		
		if (null == name) {
			return;
		}
		
		// find out the config variables to change
		String rememberConfigKey = "";
		String typeConfigVal     = "";
		String pathConfigKey     = "";
		String path              = "";
		
		// find out which checkbox has been changed
		if (UiView.NAME_REMEMBER_SF.equals(name)) {
			rememberConfigKey = Config.REMEMBER_SF2;
			pathConfigKey     = Config.PATH_SF2;
			path              = SoundfontParser.getFilePath();
		}
		else if (UiView.NAME_REMEMBER_IMPORT.equals(name)) {
			rememberConfigKey = Config.REMEMBER_IMPORT;
			if (FileSelector.FILE_TYPE_MPL.equals(currentFileType)) {
				pathConfigKey = Config.PATH_MIDICAPL;
				path          = MidicaPLParser.getFilePath();
				typeConfigVal = FileSelector.FILE_TYPE_MPL;
			}
			else if (FileSelector.FILE_TYPE_MIDI.equals(currentFileType)) {
				pathConfigKey = Config.PATH_MIDI;
				path          = MidiParser.getFilePath();
				typeConfigVal = FileSelector.FILE_TYPE_MIDI;
			}
			else if (FileSelector.FILE_TYPE_ALDA.equals(currentFileType)) {
				pathConfigKey = Config.PATH_ALDA;
				path          = AldaImporter.getFilePath();
				typeConfigVal = FileSelector.FILE_TYPE_ALDA;
			}
			else if (FileSelector.FILE_TYPE_ABC.equals(currentFileType)) {
				pathConfigKey = Config.PATH_ABC;
				path          = AbcImporter.getFilePath();
				typeConfigVal = FileSelector.FILE_TYPE_ABC;
			}
			else if (FileSelector.FILE_TYPE_LY.equals(currentFileType)) {
				pathConfigKey = Config.PATH_LY;
				path          = LilypondImporter.getFilePath();
				typeConfigVal = FileSelector.FILE_TYPE_LY;
			}
		}
		
		// save in config
		if (isChecked) {
			// remember checkbox state in the config
			Config.set(rememberConfigKey, "true");
			
			// remember file path and type, if a file is loaded!
			if (path != null) {
				Config.set(pathConfigKey, path);
			}
			if (!typeConfigVal.equals("")) {
				Config.set(Config.IMPORT_TYPE, typeConfigVal);
			}
		}
		else {
			Config.set( rememberConfigKey, "false" );
			Config.set( pathConfigKey,     ""      );
		}
	}
	
	/**
	 * Checks if a MIDI sequence is set and opens the file selector to choose
	 * the file to be exported.
	 * 
	 * If no MIDI sequence is set, shows an error message instead of opening the file selector.
	 */
	private void openExportFileSelector() {
		
		if ( ! MidiDevices.isSequenceSet() ) {
			showErrorMessage(Dict.get(Dict.ERROR_SEQUENCE_NOT_SET));
			return;
		}
		currentFilePurpose = FILE_PURPOSE_EXPORT;
		exportSelector.setVisible(true);
	}
	
	/**
	 * Parses the chosen file using the right parser for the right file type.
	 * A wait dialog is shown during the parsing.
	 * The parsing itself is executed by a {@link SwingWorker}.
	 * 
	 * @param type  File type.
	 * @param file  Selected file.
	 */
	public void parseChosenFile(String type, File file) {
		
		initSelectorsIfNotYetDone();
		
		// initialize variables based on the file type to be parsed
		WaitView     waitView = new WaitView(view);
		String       waitMsg;
		IParser      parser;
		FileSelector selector;
		String       charsetKey = null;
		String       pathKey    = null;
		if (FileSelector.FILE_TYPE_MPL.equals(type)) {
			parser     = mplParser;
			selector   = importSelector;
			waitMsg    = Dict.get(Dict.WAIT_PARSE_MPL);
			charsetKey = Config.CHARSET_MPL;
			pathKey    = Config.PATH_MIDICAPL;
		}
		else if (FileSelector.FILE_TYPE_MIDI.equals(type)) {
			parser     = midiParser;
			selector   = importSelector;
			waitMsg    = Dict.get(Dict.WAIT_PARSE_MID);
			charsetKey = Config.CHARSET_MID;
			pathKey    = Config.PATH_MIDI;
		}
		else if (FileSelector.FILE_TYPE_ALDA.equals(type)) {
			parser     = aldaImporter;
			selector   = importSelector;
			waitMsg    = String.format(Dict.get(Dict.WAIT_PARSE_FOREIGN), Dict.get(Dict.FOREIGN_PROG_ALDA));
			pathKey    = Config.PATH_ALDA;
		}
		else if (FileSelector.FILE_TYPE_ABC.equals(type)) {
			parser     = abcImporter;
			selector   = importSelector;
			waitMsg    = String.format(Dict.get(Dict.WAIT_PARSE_FOREIGN), Dict.get(Dict.FOREIGN_PROG_ABCMIDI));
			pathKey    = Config.PATH_ABC;
		}
		else if (FileSelector.FILE_TYPE_LY.equals(type)) {
			parser     = lyImporter;
			selector   = importSelector;
			waitMsg    = String.format(Dict.get(Dict.WAIT_PARSE_FOREIGN), Dict.get(Dict.FOREIGN_PROG_LY));
			pathKey    = Config.PATH_LY;
		}
		else if (FileSelector.FILE_TYPE_SOUNDFONT.equals(type)) {
			parser   = soundfontParser;
			selector = soundfontSelector;
			waitMsg  = Dict.get(Dict.WAIT_PARSE_SF2);
		}
		else {
			return;
		}
		
		// Reset the filename for the case that it cannot be parsed.
		// But don't do that for soundfonts because the last successfully
		// parsed soundfont file will stay valid.
		if (! FileSelector.FILE_TYPE_SOUNDFONT.equals(type)) {
			displayFilename(type, Dict.get(Dict.UNCHOSEN_FILE));
		}
		
		// close file selector
		selector.setVisible(false);
		
		// start file parsing in the background and show the wait window
		ParsingWorker worker = new ParsingWorker(waitView, parser, file);
		worker.execute();
		waitView.init(waitMsg);
		
		// wait until the file is parsed and than evaluate the parsing result
		try {
			try {
				ParseException parseException = (ParseException) worker.get();
				if (parseException != null) {
					throw parseException;
				}
			}
			catch (InterruptedException | ExecutionException workerException) {
				workerException.printStackTrace();
				throw new ParseException(workerException.getMessage());
			}
			
			// show the filename of the successfully parsed file
			displayFilename(type, file.getName());
			if (FileSelector.FILE_TYPE_SOUNDFONT.equals(type)) {
				String remember = Config.get(Config.REMEMBER_SF2);
				if ("true".equals(remember)) {
					Config.set(Config.PATH_SF2, file.getAbsolutePath());
				}
			}
			else {
				// store the file for later reparsing
				currentFile     = file;
				currentFileType = type;
				
				// set chosen charset in the config
				if (charsetKey != null) {
					ComboboxStringOption o = (ComboboxStringOption) ConfigComboboxModel.getModel(charsetKey).getSelectedItem();
					Config.set(charsetKey, o.getIdentifier());
				}
				
				// remember file path in the config, if necessary
				String mustRemember = Config.get(Config.REMEMBER_IMPORT);
				if (mustRemember.equals("true")) {
					Config.set(pathKey, file.getAbsolutePath());
					Config.set(Config.IMPORT_TYPE, type);
				}
			}
			
			// set import directory in the config
			selector.rememberDirectory();
		}
		catch (ParseException ex) {
			showErrorMessage(ex.getFullMessage());
			ex.printStackTrace();
		}
		
		// re-draw everything because the file name to be displayed can be longer or shorter
		// and therefore the container sizes need to be adjusted
		view.pack();
	}
	
	/**
	 * Updates the parsed file label after the player has been closed.
	 * This is necessary because a reparse from the player could have failed.
	 * Then the label must not display the file name any more.
	 */
	public void updateAfterPlayerClosed() {
		if (null == SequenceParser.getFileName()) {
			displayFilename(currentFileType, Dict.get(Dict.UNCHOSEN_FILE));
		}
	}
	
	/**
	 * Exports to the chosen file using the right exporter for this file type.
	 * 
	 * @param type  File type.
	 * @param file  Selected file.
	 */
	public void exportChosenFile(String type, File file) {
		
		String   charsetKey = null;
		Exporter exporter   = null;
		if (FileSelector.FILE_TYPE_MPL.equals(type)) {
			charsetKey = Config.CHARSET_EXPORT_MPL;
			exporter   = new MidicaPLExporter();
		}
		else if (FileSelector.FILE_TYPE_ALDA.equals(type)) {
			exporter = new AldaExporter();
		}
		else if (FileSelector.FILE_TYPE_MIDI.equals(type)) {
			charsetKey = Config.CHARSET_EXPORT_MID;
			exporter   = new MidiExporter();
		}
		else {
			return;
		}
		exportSelector.setVisible(false);
		try {
			ExportResult result = exporter.export(file);
			if (result.isSuccessful() && ! Cli.isCliMode) {
				showExportResult(result);
				
				// set chosen charset in the config
				if (charsetKey != null) {
					ComboboxStringOption o = (ComboboxStringOption) ConfigComboboxModel.getModel(charsetKey).getSelectedItem();
					Config.set(charsetKey, o.getIdentifier());
				}
				
				// set export directory in the config
				exportSelector.rememberDirectory();
				
				// directly import the exported file, if needed
				if (exportSelector.mustDirectlyImport()) {
					parseChosenFile(type, file);
				}
			}
		}
		catch (ExportException ex) {
			if (Cli.isCliMode)
				Cli.exportErrorMsg = ex.getErrorMessage();
			else
				showErrorMessage(ex.getErrorMessage());
		}
	}
	
	/**
	 * Opens an error message window showing the given message, if in GUI mode.
	 * In CLI mode, prints the message to STDERR.
	 * 
	 * @param message    Error message.
	 */
	private void showErrorMessage(String message) {
		
		if (Cli.isCliMode) {
			message = message.replaceAll( "<br>",     "\n" );
			message = message.replaceAll( "</?b>",    ""   );
			message = message.replaceAll( "</?html>", ""   );
			message = message.replaceAll( "&nbsp;",   " "  );
			System.err.println(message);
			return;
		}
		
		// errorMsg.init cannot be invoked by 'new ErrorMsgView()' because it's modal.
		// That means setVisible() is blocking. So 'new' would not return until the
		// error message window is closed.
		// errorMsg.close() would cause a NullPointerException.
		// The solution is to call setVisible() from 'errorMsg.init(..)' and
		// to call 'errorMsg.init(..)' separately after 'new' has returned and therefore
		// errorMsg is not null any more.
		ErrorMsgView errorMsg = new ErrorMsgView(view);
		errorMsg.init(message);
	}
	
	/**
	 * Opens an export result window showing the given export result.
	 * 
	 * @param result    Export result containing warnings and errors of a file export.
	 */
	private void showExportResult(ExportResult result) {
		
		if (Cli.isCliMode) {
			return;
		}
		
		// same problem like in showErrorMessage()
		ExportResultView warningView = new ExportResultView(view);
		warningView.init(result);
	}
	
	/**
	 * Update the labels for the imported sequence file name and type, if any sequence is loaded.
	 * 
	 * This is needed only in case of language switches to make sure that the correct files are still displayed.
	 */
	public void updateImportedFileTypeAndName() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				String filename = SequenceParser.getFileName();
				if (filename != null) {
					view.getImportedFileLbl().setText(filename);
					if (FileSelector.FILE_TYPE_MPL.equals(currentFileType))
						view.getImportedFileTypeLbl().setText(Dict.get(Dict.IMPORTED_TYPE_MPL));
					else if (FileSelector.FILE_TYPE_MIDI.equals(currentFileType))
						view.getImportedFileTypeLbl().setText(Dict.get(Dict.IMPORTED_TYPE_MPL));
					else if (FileSelector.FILE_TYPE_ALDA.equals(currentFileType))
						view.getImportedFileTypeLbl().setText(Dict.get(Dict.IMPORTED_TYPE_ALDA));
					else if (FileSelector.FILE_TYPE_ABC.equals(currentFileType))
						view.getImportedFileTypeLbl().setText(Dict.get(Dict.IMPORTED_TYPE_ABC));
					else if (FileSelector.FILE_TYPE_LY.equals(currentFileType))
						view.getImportedFileTypeLbl().setText(Dict.get(Dict.IMPORTED_TYPE_LY));
				}
			}
		});
	}
	
	/**
	 * Displays the parsed sequence or soundfont file name.
	 * In case of a sequence, also displays the file type.
	 * 
	 * @param type        File type.
	 * @param filename    File name.
	 */
	private void displayFilename(String type, String filename) {
		boolean notImported = ! FileSelector.FILE_TYPE_SOUNDFONT.equals(type)
			&& filename.equals(Dict.get(Dict.UNCHOSEN_FILE));
		
		if (FileSelector.FILE_TYPE_MPL.equals(type)) {
			// show imported file
			view.getImportedFileLbl().setText(filename);
			view.getImportedFileTypeLbl().setText(Dict.get(Dict.IMPORTED_TYPE_MPL));
		}
		else if (FileSelector.FILE_TYPE_MIDI.equals(type)) {
			view.getImportedFileLbl().setText(filename);
			view.getImportedFileTypeLbl().setText(Dict.get(Dict.IMPORTED_TYPE_MIDI));
		}
		else if (FileSelector.FILE_TYPE_ALDA.equals(type)) {
			view.getImportedFileLbl().setText(filename);
			view.getImportedFileTypeLbl().setText(Dict.get(Dict.IMPORTED_TYPE_ALDA));
		}
		else if (FileSelector.FILE_TYPE_ABC.equals(type)) {
			view.getImportedFileLbl().setText(filename);
			view.getImportedFileTypeLbl().setText(Dict.get(Dict.IMPORTED_TYPE_ABC));
		}
		else if (FileSelector.FILE_TYPE_LY.equals(type)) {
			view.getImportedFileLbl().setText(filename);
			view.getImportedFileTypeLbl().setText(Dict.get(Dict.IMPORTED_TYPE_LY));
		}
		else if (FileSelector.FILE_TYPE_SOUNDFONT.equals(type))
			// show soundfont file
			view.getChosenSoundfontFileLbl().setText(filename);
		
		// don't display the file type if no file is imported
		if (notImported)
			view.getImportedFileTypeLbl().setText(Dict.get(Dict.UNCHOSEN_FILE));
	}
	
	/**
	 * Initializes key bindings and refreshes the current transpose level that has been set
	 * in the player (or 0, if the transpose level has not yet been changed).
	 * 
	 * @param e    Window event.
	 */
	public void windowActivated(WindowEvent e) {
		view.setTransposeLevel(SequenceParser.getTransposeLevel());
	}
	
	/**
	 * Does nothing.
	 * 
	 * @param e    Window event.
	 */
	public void windowClosed(WindowEvent e) {
	}
	
	/**
	 * Writes the current config to the config file, and exits.
	 * 
	 * @param e    Window event.
	 */
	public void windowClosing(WindowEvent e) {
		Config.writeConfigFile();
		System.exit(0);
	}
	
	/**
	 * Does nothing.
	 * 
	 * @param e    Window event.
	 */
	public void windowDeactivated(WindowEvent e) {
	}
	
	/**
	 * Does nothing.
	 * 
	 * @param e    Window closed event.
	 */
	public void windowDeiconified(WindowEvent e) {
	}
	
	/**
	 * Does nothing.
	 * 
	 * @param e    Window event.
	 */
	public void windowIconified(WindowEvent e) {
	}
	
	/**
	 * Checks if files must be loaded automatically, and loads them, if
	 * the check succeeds.
	 * 
	 * @param e    Window event.
	 */
	public void windowOpened(WindowEvent e) {
		
		// files already parsed?
		if ( SoundfontParser.getFileName()  != null
			|| MidicaPLParser.getFileName() != null
			|| MidiParser.getFileName()     != null ) {
			
			// Don't parse them again. This method has only been called because
			// of a language change.
			return;
		}
		
		// check config
		String rememberSf     = Config.get( Config.REMEMBER_SF2    );
		String soundfontPath  = Config.get( Config.PATH_SF2        );
		String rememberImport = Config.get( Config.REMEMBER_IMPORT );
		String midicaplPath   = Config.get( Config.PATH_MIDICAPL   );
		String midiPath       = Config.get( Config.PATH_MIDI       );
		String aldaPath       = Config.get( Config.PATH_ALDA       );
		String abcPath        = Config.get( Config.PATH_ABC        );
		String lyPath         = Config.get( Config.PATH_LY         );
		String importType     = Config.get( Config.IMPORT_TYPE     );
		
		// Wait until Midica.uiController is not null any more.
		// Otherwise a remembered file could be parsed faster (race condition) and
		// we would get a null pointer exception in the SequenceParser.
		synchronized(UiController.class) {
			
			// load soundfont, if needed
			if (!Cli.useSoundfont) {
				if ("true".equals(rememberSf) && ! soundfontPath.equals("")) {
					parseChosenFile(FileSelector.FILE_TYPE_SOUNDFONT, new File(soundfontPath));
				}
			}
			
			if (Cli.isImport) {
				// file already imported - don't import again
			}
			else {
				
				// load sequence, if needed
				if ("true".equals(rememberImport)) {
					
					if (FileSelector.FILE_TYPE_MPL.equals(importType) && ! midicaplPath.equals(""))
						parseChosenFile(FileSelector.FILE_TYPE_MPL, new File(midicaplPath));
					else if (FileSelector.FILE_TYPE_MIDI.equals(importType) && ! midiPath.equals(""))
						parseChosenFile(FileSelector.FILE_TYPE_MIDI, new File(midiPath));
					else if (FileSelector.FILE_TYPE_ALDA.equals(importType) && ! aldaPath.equals(""))
						parseChosenFile(FileSelector.FILE_TYPE_ALDA, new File(aldaPath));
					else if (FileSelector.FILE_TYPE_ABC.equals(importType) && ! abcPath.equals(""))
						parseChosenFile(FileSelector.FILE_TYPE_ABC, new File(abcPath));
					else if (FileSelector.FILE_TYPE_LY.equals(importType) && ! lyPath.equals(""))
						parseChosenFile(FileSelector.FILE_TYPE_LY, new File(lyPath));
				}
			}
		}
	}
	
	/**
	 * Applies configuration changes.
	 * 
	 * This method is called whenever one of the configuration comboboxes is changed.
	 * 
	 * @param name          Name of the combobox that has been changed.
	 * @param selectedId    ID of the selected item in the combobox.
	 */
	private void configurationChanged(String name, String selectedId) {
		String type         = null;
		String configuredId = null;
		
		// language selected
		if (UiView.NAME_SELECT_LANGUAGE.equals(name)) {
			
			// store new configuration
			type         = Config.LANGUAGE;
			configuredId = Config.get(type);
			Config.set(type, selectedId);
			
			// check if there has been a change we have to care about
			if ( ! selectedId.equals(configuredId) ) {
				
				// update gui
				view.close();
				Dict.init();
				Config.initLocale();
				initView();
			}
		}
		
		// note system selected
		else if (UiView.NAME_SELECT_SYSTEM.equals(name)) {
			
			// store new configuration
			type         = Config.NOTE;
			configuredId = Config.get(type);
			Config.set(type, selectedId);
			
			// check if there has been a change we have to care about
			if ( ! selectedId.equals(configuredId) ) {
				
				// update note system
				Dict.initNoteSystem();
			}
		}
		
		// half tones selected
		else if (UiView.NAME_SELECT_HALF_TONE.equals(name)) {
			
			// store new configuration
			type         = Config.HALF_TONE;
			configuredId = Config.get(type);
			Config.set(type, selectedId);
			
			// check if there has been a change we have to care about
			if ( ! selectedId.equals(configuredId) ) {
				
				// update half tone
				Dict.initHalfTones();
			}
		}
		
		// octave naming selected
		else if (UiView.NAME_SELECT_OCTAVE.equals(name)) {
			
			// store new configuration
			type         = Config.OCTAVE;
			configuredId = Config.get(type);
			Config.set(type, selectedId);
			
			// check if there has been a change we have to care about
			if ( ! selectedId.equals(configuredId) ) {
				
				// update note system
				Dict.initOctaves();
			}
		}
		
		// syntax selected
		else if (UiView.NAME_SELECT_SYNTAX.equals(name)) {
			
			// store new configuration
			type         = Config.SYNTAX;
			configuredId = Config.get(type);
			Config.set(type, selectedId);
			
			// check if there has been a change we have to care about
			if ( ! selectedId.equals(configuredId) ) {
				
				// update syntax
				Dict.initSyntax();
			}
		}
		
		// percussion selected
		else if (UiView.NAME_SELECT_PERCUSSION.equals(name)) {
			
			// store new configuration
			type         = Config.PERCUSSION;
			configuredId = Config.get(type);
			Config.set(type, selectedId);
			
			// check if there has been a change we have to care about
			if ( ! selectedId.equals(configuredId) ) {
				
				// update percussion
				Dict.initPercussion();
			}
		}
		
		// instruments selected
		else if (UiView.NAME_SELECT_INSTRUMENT.equals(name)) {
			
			// store new configuration
			type         = Config.INSTRUMENT;
			configuredId = Config.get(type);
			Config.set(type, selectedId);
			
			// check if there has been a change we have to care about
			if ( ! selectedId.equals(configuredId) ) {
				
				// update instruments
				Dict.initInstruments();
			}
		}
		
		// refresh syntax in the parser
		MidicaPLParser.refreshSyntax();
	}
}
