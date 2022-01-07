/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.worker;

import org.midica.file.read.IParser;
import org.midica.file.read.ParseException;


/**
 * This class is used to parse a file (or URL) in the background while a
 * {@link WaitView} is shown.
 * 
 * This worker is executed in the background before the (blocking)
 * setVisible() method of the (modal) waiting dialog is called.
 * That causes the execution of {@link #doInBackground()} that parses
 * the file or URL.
 * 
 * After the parsing work is finished, {@link MidicaWorker#done()} is called
 * and closes the waiting dialog.
 * 
 * @author Jan Trukenmüller
 */
public class ParsingWorker extends MidicaWorker {
	
	private IParser parser    = null;
	private Object  fileOrUrl = null;
	
	/**
	 * Creates a parsing worker that parses a file in the background while
	 * a waiting dialog is shown.
	 * 
	 * @param view         The waiting dialog.
	 * @param parser       The parser do be executed in the background.
	 * @param fileOrUrl    The file or url to be parsed.
	 */
	public ParsingWorker(WaitView view, IParser parser, Object fileOrUrl) {
		super(view);
		this.parser    = parser;
		this.fileOrUrl = fileOrUrl;
	}
	
	/**
	 * Parses the file or URL in the background.
	 * This method is executed after calling {@link #execute()}.
	 * 
	 * @return the parse exception or **null** if no exception is caught.
	 */
	@Override
	protected ParseException doInBackground() {
		// parse
		ParseException parseException = null;
		try {
			parser.parse(fileOrUrl);
		}
		catch (ParseException e) {
			parseException = e;
		}
		
		return parseException;
	}
}
