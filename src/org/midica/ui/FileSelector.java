/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JFileChooser;

import org.midica.config.Config;
import org.midica.config.Dict;
import org.midica.ui.widget.MidicaFileChooser;

/**
 * Provides a file selector window for choosing files with a certain
 * set of extensions.
 * 
 * @author Jan Trukenmüller
 */
public class FileSelector extends JDialog {

	private static final long serialVersionUID = 1L;
	
	// file types (NOT the same as file extensions)
	public static final String FILE_TYPE_MPL       = "mpl";
	public static final String FILE_TYPE_MIDI      = "midi";
	public static final String FILE_TYPE_SOUNDFONT = "sf2";
	public static final byte   READ                = 1;
	public static final byte   WRITE               = 2;
	
	private String fileType;
	private byte   filePurpose;
	
	MidicaFileChooser fileChooser = null;
	UiController      controller  = null;
	
	/**
	 * Creates a new file selector window.
	 * 
	 * @param v    Parent view.
	 * @param c    Listener for file choosing events.
	 */
	public FileSelector(UiView v, UiController c) {
		super(v, Dict.get(Dict.TITLE_FILE_SELECTOR), true);
		controller = c;
	}
	
	/**
	 * Initializes and shows the file selector window including the {@link JFileChooser}.
	 * Sets a {@link FileExtensionFilter} for selecting the files by type.
	 * 
	 * @param type           File type by which the files are filtered.
	 * @param filePurpose    Purpose for which the file is going to be opened.
	 *                       Possible values are **READ** or **WRITE**.
	 */
	public void init(String type, byte filePurpose) {
		this.fileType    = type;
		this.filePurpose = filePurpose;
		
		String  directory     = "";
		boolean charSetSelect = false;
		if (READ == this.filePurpose) {
    		if ( type.equals(FILE_TYPE_MIDI) ) {
    			directory     = Config.get(Config.DIRECTORY_MID);
    			charSetSelect = true;
    		}
    		else if ( type.equals(FILE_TYPE_SOUNDFONT) ) {
    			directory     = Config.get(Config.DIRECTORY_SF2);
    			charSetSelect = false;
    		}
    		else {
    			directory     = Config.get(Config.DIRECTORY_MPL);
    			charSetSelect = true;
    		}
    	}
		else {
			if ( type.equals(FILE_TYPE_MIDI) ) {
    			directory     = Config.get(Config.DIRECTORY_EXPORT_MID);
    			charSetSelect = true;
			}
    		else {
    			directory     = Config.get(Config.DIRECTORY_EXPORT_MPL);
    			charSetSelect = true;
    		}
		}
		fileChooser = new MidicaFileChooser(type, filePurpose, directory, charSetSelect);
		
		fileChooser.setFileFilter( new FileExtensionFilter(type) );
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.addActionListener(controller);
		add(fileChooser);
		pack();
		setVisible(false);
		
		// refresh directory when (re)activated
		this.addWindowListener(new WindowListener() {
			
			@Override
			public void windowActivated(WindowEvent e) {
				fileChooser.rescanCurrentDirectory();
			}
			
			@Override
			public void windowOpened(WindowEvent e) {
			}
			@Override
			public void windowIconified(WindowEvent e) {
			}
			@Override
			public void windowDeiconified(WindowEvent e) {
			}
			@Override
			public void windowDeactivated(WindowEvent e) {
			}
			@Override
			public void windowClosing(WindowEvent e) {
			}
			@Override
			public void windowClosed(WindowEvent e) {
			}
		});
	}
	
	/**
	 * Sets the directory of the chosen file in the config.
	 * 
	 * Remembers the directory of that file so that next time the selector defaults to the same
	 * directory if the same file type for the same purpose is going to be opened.
	 */
	public void rememberDirectory() {
		File file = fileChooser.getSelectedFile();
		
		// no file selected? - probably this is during startup after parsing an automatically
		// parsed, remembered file
		if (null == file) {
			return;
		}
		
		// get directory and store it in the config
		try {
			String directory = file.getParentFile().getCanonicalPath();
			if (READ == this.filePurpose) {
				if ( FILE_TYPE_MIDI.equals(fileType) )
					Config.set(Config.DIRECTORY_MID, directory);
    			else if ( FILE_TYPE_SOUNDFONT.equals(fileType) )
    				Config.set(Config.DIRECTORY_SF2, directory);
    			else
    				Config.set(Config.DIRECTORY_MPL, directory);
    		}
			else {
				if ( FILE_TYPE_MIDI.equals(fileType) )
					Config.set(Config.DIRECTORY_EXPORT_MID, directory);
				else
    				Config.set(Config.DIRECTORY_EXPORT_MPL, directory);
			}
		}
		catch (IOException e) {
		}
	}
}
