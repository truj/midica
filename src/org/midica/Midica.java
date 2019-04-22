/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica;

import org.midica.config.Config;
import org.midica.config.Dict;
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
	public static final int VERSION_MINOR = 1555950713;
	
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
	
	/** CLI mode (command line interface) without GUI - e.g. for unit tests */
	public static boolean isCliMode = false;
	
	/** Determins if the local config file shall be used or not. */
	public static boolean useLocalConfig = true;
	
	/**
	 * The entry method which is launched on program startup.
	 * 
	 * @param args No specific arguments are supported so far.
	 */
	public static void main(String[] args) {
		
		// command line arguments
		processCmdLineArgs(args);
		
		// init config
		Config.init();
		
		// initialize dictionaries
		Dict.init();
		
		// start the GUI
		synchronized(UiController.class) {
			uiController = new UiController();
		}
	}
	
	/**
	 * Processes command line arguments.
	 * 
	 * @param args command line arguments.
	 */
	private static void processCmdLineArgs(String[] args) {
		for (String arg : args) {
			if ("--cli".equals(arg)) {
				isCliMode = true;
			}
			else if ("--ignore-local-config".equals(arg)) {
				useLocalConfig = false;
			}
			else if ("--help".equals(arg)) {
				help(true);
			}
			else {
				help(false);
			}
		}
	}
	
	/**
	 * Prints a hepl message and exits.
	 * The exit code is 0, if called with `--help`.
	 * Otherwise the parameters are regarded as erroneous and the exit code is 1.
	 * 
	 * @param isHelpRequested **true**, if called with `--help` on the command line
	 */
	private static void help(boolean isHelpRequested) {
		
		StringBuilder msg = new StringBuilder("Options:\n");
		msg.append("--help                : Print this message.\n");
		msg.append("--cli                 : Run in CLI mode (command line interface) without GUI.\n");
		msg.append("--ignore-local-config : Doesn't use local config file. Use default config.\n");
		
		if (isHelpRequested) {
			System.out.println(msg);
			System.exit(0);
		}
		else {
			System.err.println(msg);
			System.exit(64);
		}
	}
}
