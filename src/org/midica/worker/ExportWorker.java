/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.worker;

import java.io.File;

import org.midica.file.write.ExportException;
import org.midica.file.write.Exporter;
import org.midica.ui.file.ExportResult;


/**
 * This class is used to export a file in the background while a
 * {@link WaitView} is shown.
 * 
 * This worker is executed in the background before the (blocking)
 * setVisible() method of the (modal) waiting dialog is called.
 * That causes the execution of {@link #doInBackground()} that exports the file.
 * 
 * After the export work is finished, {@link MidicaWorker#done()} is called and
 * closes the waiting dialog.
 * 
 * @author Jan Trukenmüller
 */
public class ExportWorker extends MidicaWorker {
	
	private Exporter     exporter = null;
	private File         file     = null;
	private ExportResult result   = null;
	
	/**
	 * Creates an export worker that exports a file in the background while
	 * a waiting dialog is shown.
	 * 
	 * @param view      The waiting dialog.
	 * @param exporter  The exporter do be executed in the background.
	 * @param file      The file to be exported.
	 */
	public ExportWorker(WaitView view, Exporter exporter, File file) {
		super(view);
		this.exporter = exporter;
		this.file     = file;
	}
	
	/**
	 * Exports the file in the background.
	 * This method is executed after calling {@link #execute()}.
	 * 
	 * @return the export exception or **null** if no exception is caught.
	 */
	@Override
	protected ExportException doInBackground() {
		
		// export
		ExportException exportException = null;
		result = null;
		try {
			result = exporter.export(file);
		}
		catch (ExportException e) {
			exportException = e;
		}
		
		return exportException;
	}
	
	/**
	 * Returns the export result, if available, or **null** if an ExportException
	 * has occurred.
	 * 
	 * @return the export result or **null**.
	 */
	public ExportResult getResult() {
		return result;
	}
}
