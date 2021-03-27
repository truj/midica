/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file.write;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.midica.TestUtil;
import org.midica.file.Foreign;
import org.midica.file.ForeignException;
import org.midica.file.read.MidicaPLParser;
import org.midica.file.read.ParseException;
import org.midica.file.write.MidicaPLExporter;

/**
 * This is the test class for {@link MidicaPLExporter}.
 * 
 * @author Jan Trukenmüller
 */
public class MidicaPLExporterTest extends MidicaPLExporter {
	
	/**
	 * Initializes midica in test mode.
	 * 
	 * @throws InterruptedException       on interruptions while waiting for the event dispatching thread.
	 * @throws InvocationTargetException  on exceptions.
	 */
	@BeforeAll
	static void setUpBeforeClass() throws InvocationTargetException, InterruptedException {
		TestUtil.initMidica();
	}
	
	/**
	 * Tests if the files in the examples directory can be parsed and
	 * then decompiled without exception.
	 * 
	 * @throws ParseException if something went wrong.
	 */
	@Test
	void testExampleFiles() throws ParseException, ForeignException, ExportException {
		MidicaPLParser parser = new MidicaPLParser(true);
		String dirStr = System.getProperty("user.dir") + File.separator + "examples";
		File dir = new File(dirStr);
		for (File file : dir.listFiles()) {
			
			// parse
			if (!file.isFile())
				continue;
			if (!file.getName().endsWith(".midica") && !file.getName().endsWith(".mpl"))
				continue;
			parser.parse(file);
			
			// decompile
			File decompiledFile = Foreign.createTempFile("mpl", null);
			export(decompiledFile);
			
			// clean up
			Foreign.deleteTempFile(decompiledFile);
		}
	}
}
