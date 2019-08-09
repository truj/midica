/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

/**
 * This class provides static methods that can be used by other test classes.
 * 
 * @author Jan Trukenmüller
 */
public class TestUtil {
	
	private static boolean isMidicaInitialized = false;
	
	/**
	 * Initializes and stores midica in test mode, if not yet done.
	 * 
	 * @throws InterruptedException       on interruptions while waiting for the event dispatching thread.
	 * @throws InvocationTargetException  on exceptions.
	 */
	public static void initMidica() throws InvocationTargetException, InterruptedException {
		
		if (isMidicaInitialized)
			return;
		
		// init (mostly static) dependencies
		String[] args = {
			"--cli",
			"--ignore-local-config",
		};
		Midica.main(args);
		
		// wait until the swing-based components are ready
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				// nothing more to be done here
			}
		});
		
		isMidicaInitialized = true;
	}
	
	/**
	 * Returns the directory with testfiles.
	 * 
	 * @return testfile directory.
	 */
	public static String getTestfileDirectory() {
		return System.getProperty("user.dir") + File.separator + "test" + File.separator
			+ "org" + File.separator + "midica" + File.separator + "testfiles" + File.separator;
	}
}
