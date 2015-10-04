/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 * Provides a filter for a {@link JFileChooser} to filter files by file extension.
 * This class is used together with the {@link JFileChooser} by the class
 * {@link FileExtensionFilter}.
 * 
 * @author Jan Trukenmüller
 */
public class FileExtensionFilter extends FileFilter {
	
	private String extension;
	
	/**
	 * Creates a new file extension filter for a file chooser filtering files
	 * by the given extension.
	 * 
	 * @param extension    File extension to be filtered.
	 */
	public FileExtensionFilter( String extension ) {
		this.extension = extension;
	}
	
	@Override
	public boolean accept( File file ) {
		String filename = file.getName();
		if ( file.isDirectory() )
			return true;
		if ( filename.endsWith("." + getExtension()) )
			return true;
		return false;
	}
	
	@Override
	public String getDescription() {
		return "*." + getExtension();
	}
	
	/**
	 * Returns the file extension bound to this filter.
	 * 
	 * @return    File extension.
	 */
	public String getExtension() {
		return extension;
	}
}
