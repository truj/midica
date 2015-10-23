/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui;

import java.io.File;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JFileChooser;

import org.midica.config.Config;
import org.midica.config.Dict;

/**
 * Provides a file selector window for choosing files with a certain extension.
 * 
 * @author Jan Trukenmüller
 */
public class FileSelector extends JDialog {

	private static final long serialVersionUID = 1L;
	
	// file extensions
	public static final String FILE_EXTENSION_MPL       = "midica";
	public static final String FILE_EXTENSION_MIDI      = "mid";
	public static final String FILE_EXTENSION_SOUNDFONT = "sf2";
	public static final byte   READ                     = 1;
	public static final byte   WRITE                    = 2;
	
	private String suffix;
	private byte   filePurpose;
	
	JFileChooser fileChooser = null;
	UiController controller  = null;
	
	/**
	 * Creates a new file selector window.
	 * 
	 * @param v    Parent window.
	 * @param c    Listener for file choosing events.
	 */
	public FileSelector( UiView v, UiController c ) {
		super( v, Dict.get(Dict.TITLE_FILE_SELECTOR), true );
		controller = c;
	}
	
	/**
	 * Initializes and shows the file selector window including the {@link JFileChooser}.
	 * Sets a {@link FileExtensionFilter} for selecting the files by extension.
	 * 
	 * @param suffix         File extension by which the files are filtered.
	 * @param filePurpose    Purpose for which the file is going to be opened.
	 *                       Possible values are **READ** or **WRITE**.
	 */
	public void init( String suffix, byte filePurpose ) {
		this.suffix      = suffix;
		this.filePurpose = filePurpose;
		
		if ( READ == this.filePurpose ) {
    		if ( suffix.equals(FILE_EXTENSION_MIDI) )
    			fileChooser = new JFileChooser( Config.get(Config.DIRECTORY_MID) );
    		else if ( suffix.equals(FILE_EXTENSION_SOUNDFONT) )
    			fileChooser = new JFileChooser( Config.get(Config.DIRECTORY_SF2) );
    		else
    			fileChooser = new JFileChooser( Config.get(Config.DIRECTORY_MPL) );
    		}
		else {
			if ( suffix.equals(FILE_EXTENSION_MIDI) )
    			fileChooser = new JFileChooser( Config.get(Config.DIRECTORY_EXPORT_MID) );
    		else
    			fileChooser = new JFileChooser( Config.get(Config.DIRECTORY_EXPORT_MPL) );
		}
		
		fileChooser.setFileFilter( new FileExtensionFilter(suffix) );
		fileChooser.setAcceptAllFileFilterUsed( false );
		fileChooser.addActionListener( controller );
		add( fileChooser );
		pack();
		setVisible( false );
	}
	
	/**
	 * Returns the chosen file.
	 * 
	 * Remembers the directory of that file so that next time the selector defaults to the same
	 * directory if the same file type for the same purpose is going to be opened.
	 * 
	 * @return    Chosen file.
	 */
	public File getFile() {
		// get directory and store it into the config
		File file = fileChooser.getSelectedFile();
		try {
			String directory = file.getParentFile().getCanonicalPath();
			if ( READ == this.filePurpose ) {
				if ( FILE_EXTENSION_MIDI.equals(suffix) )
					Config.set( Config.DIRECTORY_MID, directory );
    			else if ( FILE_EXTENSION_SOUNDFONT.equals(suffix) )
    				Config.set( Config.DIRECTORY_SF2, directory );
    			else
    				Config.set( Config.DIRECTORY_MPL, directory );
    		}
			else {
				if ( FILE_EXTENSION_MIDI.equals(suffix) )
					Config.set( Config.DIRECTORY_EXPORT_MID, directory );
				else
    				Config.set( Config.DIRECTORY_EXPORT_MPL, directory );
			}
		}
		catch ( IOException e ) {
		}
		
		return file;
	}
}
