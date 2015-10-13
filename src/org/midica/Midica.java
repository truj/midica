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

import com.sun.corba.se.impl.util.Version;

/**
 * This is the main class to be defined by the MANIFEST.MF in the jar archive.
 * 
 * @author Jan Trukenmüller
 */
public class Midica {
	
	/** Major version number. This has to be incremented manually. */
	public static final int VERSION_MAJOR = 0;
	
	/** Minor version number. This is intended to be incremented automatically by precommit.pl. */
	public static final int VERSION_MINOR = 1444756101;
	
	/** Full version string. */
	public static final String VERSION = VERSION_MAJOR + "." + VERSION_MINOR;
	
	/**
	 * The entry method which is launched on program startup.
	 * 
	 * @param args No specific arguments are supported so far.
	 */
	public static void main(String[] args) {
		
		// init config
		Config.init();
		
		// initialize dictionaries
		Dict.init();
		
		// start the GUI
		new UiController();
	}
}
