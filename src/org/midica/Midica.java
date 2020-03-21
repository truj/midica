/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica;

import javax.swing.SwingUtilities;

import org.midica.config.Cli;
import org.midica.config.Config;
import org.midica.config.Dict;
import org.midica.config.Laf;
import org.midica.ui.UiController;

/**
 * This is the main class to be defined by the MANIFEST.MF in the jar archive.
 * 
 * @author Jan Trukenmüller
 */
public class Midica {
	
	/** Major version number. This has to be incremented manually. */
	public static final int VERSION_MAJOR = 0;
	
	/** Minor version number. This is intended to be incremented automatically by precommit.pl. */
	public static final int VERSION_MINOR = 1584827931;
	
	/** Full version string. */
	public static final String VERSION = VERSION_MAJOR + "." + VERSION_MINOR;
	
	/** Author name */
	public static final String AUTHOR = "Jan Trukenmüller";
	
	/** Repository URL */
	public static final String SOURCE_URL = "https://github.com/truj/midica";
	
	/** Website URL */
	public static final String URL = "http://midica.org/";
	
	/** Controller of the main window */
	public static UiController uiController;
	
	/**
	 * The entry method which is launched on program startup.
	 * 
	 * @param args No specific arguments are supported so far.
	 */
	public static void main(String[] args) {
		
		// command line arguments
		Cli.parseArguments(args);
		
		// init config
		Config.init();
		
		// init look and feel
		if (! Cli.isCliMode) {
			Laf.init();
		}
		
		// initialize dictionaries
		Dict.init();
		
		// start the GUI (inside of the event dispatching thread)
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				synchronized(UiController.class) {
					uiController = new UiController();
				}
			}
		});
		
		// import/export, if requested
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (Cli.useSoundfont) {
					Cli.loadSoundfont(uiController);
				}
				if (Cli.isImport) {
					Cli.importFile(uiController);
					if (Cli.isExport) {
						Cli.exportFile(uiController);
					}
				}
			}
		});
		
		// finish
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (!Cli.keepAlive) {
					System.exit(0);
				}
			}
		});
	}
}
