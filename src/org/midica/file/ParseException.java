/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file;

/**
 * Exceptions of this class can be thrown if an error occurs while parsing a file.
 * It can be used for different file types.
 * 
 * @author Jan Trukenmüller
 */
public class ParseException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	private int    lineNumber = 0;
	private String fileName   = null;
	
	/**
	 * Throws a generic parse exception without a detail message.
	 */
	public ParseException() {
	}
	
	/**
	 * Throws an exception including a detail message.
	 * 
	 * @param message
	 */
	public ParseException( String message ) {
		super( message );
	}
	
	/**
	 * Returns the line number of the parsed file where the exception occured.
	 * 
	 * @return    Line number of the exception.
	 */
	public int getLineNumber() {
		return lineNumber;
	}
	
	/**
	 * Sets the line number of the parsed file where the exception occured.
	 * 
	 * @param lineNumber        Line number of the exception.
	 */
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}
	
	/**
	 * Returns the name of the parsed file.
	 * 
	 * @return  file name
	 */
	public String getFileName() {
		return fileName;
	}
	
	/**
	 * Sets the name of the parsed file.
	 * 
	 * @param fileName    Name of the parsed file.
	 */
	public void setFileName( String fileName ) {
		this.fileName = fileName;
	}
}
