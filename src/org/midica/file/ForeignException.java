/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file;

/**
 * Exceptions of this class indicate an error while interacting with a foreign
 * process.
 * 
 * @author Jan Trukenmüller
 */
public class ForeignException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	private boolean isPathChecked = false;
	
	/**
	 * Creates an exception with the given message
	 * 
	 * @param message    Error message
	 */
	public ForeignException(String message) {
		super(message);
	}
	
	/**
	 * Sets the path of the executable to be be checked.
	 * That means that the foreign program was executable.
	 */
	public void setPathChecked() {
		isPathChecked = true;
	}
	
	/**
	 * Indicates if the command path was generally executable.
	 * 
	 * @return **true** if the command was executable, otherwise **false**.
	 */
	public boolean isPathChecked() {
		return isPathChecked;
	}
}
