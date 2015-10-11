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
	
	public static final int VERSION_MAJOR = 0;
	public static final int VERSION_MINOR = 1444601776;
	
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
