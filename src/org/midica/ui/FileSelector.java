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
	 * Sets a {@link FileExtensionFilter} for selecting the files by type.
	 * 
	 * @param type           File type by which the files are filtered.
	 * @param filePurpose    Purpose for which the file is going to be opened.
	 *                       Possible values are **READ** or **WRITE**.
	 */
	public void init( String type, byte filePurpose ) {
		this.fileType    = type;
		this.filePurpose = filePurpose;
		
		if ( READ == this.filePurpose ) {
    		if ( type.equals(FILE_TYPE_MIDI) )
    			fileChooser = new JFileChooser( Config.get(Config.DIRECTORY_MID) );
    		else if ( type.equals(FILE_TYPE_SOUNDFONT) )
    			fileChooser = new JFileChooser( Config.get(Config.DIRECTORY_SF2) );
    		else
    			fileChooser = new JFileChooser( Config.get(Config.DIRECTORY_MPL) );
    		}
		else {
			if ( type.equals(FILE_TYPE_MIDI) )
    			fileChooser = new JFileChooser( Config.get(Config.DIRECTORY_EXPORT_MID) );
    		else
    			fileChooser = new JFileChooser( Config.get(Config.DIRECTORY_EXPORT_MPL) );
		}
		
		fileChooser.setFileFilter( new FileExtensionFilter(type) );
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
				if ( FILE_TYPE_MIDI.equals(fileType) )
					Config.set( Config.DIRECTORY_MID, directory );
    			else if ( FILE_TYPE_SOUNDFONT.equals(fileType) )
    				Config.set( Config.DIRECTORY_SF2, directory );
    			else
    				Config.set( Config.DIRECTORY_MPL, directory );
    		}
			else {
				if ( FILE_TYPE_MIDI.equals(fileType) )
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
