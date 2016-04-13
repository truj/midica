/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 * Provides a filter for a {@link JFileChooser} to filter files by file extensions.
 * This class is used together with the {@link JFileChooser} by the class
 * {@link FileSelector}.
 * 
 * @author Jan Trukenmüller
 */
public class FileExtensionFilter extends FileFilter {
	
	private String             type;
	private ArrayList<Pattern> patterns          = null;
	private ArrayList<String>  allowedExtensions = null;
	
	/**
	 * Creates a new file extension filter for a file chooser filtering files
	 * by the given file type.
	 * 
	 * @param extension    File type to be filtered.
	 */
	public FileExtensionFilter( String type ) {
		this.type = type;
		init();  // initialize extensions and patterns
	}
	
	/**
	 * Initializes the allowed file extensions and regex patterns according to
	 * the file type.
	 */
	private void init() {
		
		// init file extensions
		allowedExtensions = new ArrayList<String>();
		if ( FileSelector.FILE_TYPE_MPL.equals(type) ) {
			allowedExtensions.add( "midica"   );
			allowedExtensions.add( "midicapl" );
			allowedExtensions.add( "mpl"      );
		}
		else if ( FileSelector.FILE_TYPE_MIDI.equals(type) ) {
			allowedExtensions.add( "mid"  );
			allowedExtensions.add( "midi" );
			allowedExtensions.add( "kar"  );
		}
		else if ( FileSelector.FILE_TYPE_SOUNDFONT.equals(type) ) {
			allowedExtensions.add( "sf2" );
		}
		
		// init filter patterns
		patterns  = new ArrayList<Pattern>();
		int flags = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
		for ( String extension : allowedExtensions ) {
			Pattern patt = Pattern.compile( ".+\\." + extension + "$" ,flags  );
			patterns.add( patt );
		}
	}
	
	@Override
	public boolean accept( File file ) {
		String filename = file.getName();
		
		// show all directories
		if ( file.isDirectory() )
			return true;
		
		// check if the file name has one of the allowed extensions
		for ( Pattern patt : patterns ) {
			boolean isAllowed = patt.matcher( filename ).matches();
			if (isAllowed)
				return true;
		}
		
		return false;
	}
	
	/**
	 * Returns a list of comma-separated, allowed file extensions.
	 * Every extension is preceeded by '*.'
	 * 
	 * Example:
	 * - *.mid, *.midi, *.kar
	 * 
	 * @return    a list of allowed file extensions.
	 */
	@Override
	public String getDescription() {
		
		boolean       needComma = false;
		StringBuilder result    = new StringBuilder();
		for ( String extension : allowedExtensions ) {
			if (needComma)
				result.append( ", " );
			
			result.append( "*." + extension );
			needComma = true;
		}
		
		return result.toString();
	}
	
	/**
	 * Returns the file extension bound to this filter.
	 * 
	 * @return    File extension.
	 */
	public String getType() {
		return type;
	}
}
