/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file;

import java.io.File;

import javax.swing.SwingWorker;

import org.midica.ui.WaitView;

/**
 * This class is used to parse a file in the background while a
 * {@link WaitView} is shown.
 * 
 * This worker is executed in the background before the (blocking)
 * setVisible() method of the (modal) waiting dialog is called.
 * That causes the execution of {@link #doInBackground()} that parses the file.
 * 
 * After the parsing work is done, {@link #done()} is called and closes the
 * waiting dialog.
 * 
 * @author Jan Trukenmüller
 */
public class ParsingWorker extends SwingWorker<ParseException, Void> {
	
	private WaitView view   = null;
	private IParser  parser = null;
	private File     file   = null;
	
	/**
	 * Creates a parsing worker that parses a file in the background while
	 * a waiting dialog is shown.
	 * 
	 * @param view    The waiting dialog.
	 * @param parser  The parser do be executed in the background.
	 * @param file    The file to be parsed.
	 */
	public ParsingWorker( WaitView view, IParser parser, File file ) {
		this.view   = view;
		this.parser = parser;
		this.file   = file;
	}
	
	/**
	 * Parses the file in the background.
	 * This method is executed after calling {@link #execute()}.
	 * 
	 * @return the parse exception or **null** if no exception occurrs.
	 * @throws ParseException if the file parsing fails.
	 */
	@Override
	protected ParseException doInBackground() {
		// parse
		ParseException parseException = null;
		try {
			parser.parse( file );
		}
		catch (ParseException e) {
			parseException = e;
		}
		
		return parseException;
	}
	
	/**
	 * Closes the waiting dialog.
	 * This method is executed if the parsing is finished.
	 */
	@Override
	protected void done() {
		view.close();
	}
	
}
