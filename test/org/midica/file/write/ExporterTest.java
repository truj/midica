/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file.write;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.midica.TestUtil;
import org.midica.file.Foreign;
import org.midica.file.ForeignException;
import org.midica.file.read.MidicaPLParser;
import org.midica.file.read.ParseException;
import org.midica.file.write.MidicaPLExporter;

/**
 * This is the test class for file exporters.
 * 
 * @author Jan Trukenmüller
 */
public class ExporterTest {
	
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
	 * Tests MidicaPL files, if they can be parsed and then exported without exception.
	 * 
	 * The directories to be tested are:
	 * 
	 * - the example files
	 * - the working unit test files for the {@link MidicaPLParser}
	 * - the MidicaPL test files for the decompiler
	 * 
	 * The exporters to be tested with each file are:
	 * 
	 * - {@link MidicaPLExporter}
	 * - {@link AldaExporter}
	 * - {@link MidiExporter}
	 * 
	 * @throws ParseException if something went wrong.
	 */
	@Test
	void testParseDecompileMplDirectories() throws ParseException, ForeignException, ExportException {
		MidicaPLParser parser = new MidicaPLParser(true);
		
		// get directories to be searched
		ArrayList<String> directories = new ArrayList<>();
		directories.add(System.getProperty("user.dir")  + File.separator + "examples");
		directories.add(TestUtil.getTestfileDirectory() + File.separator + "working");
		directories.add(TestUtil.getTestfileDirectory() + File.separator + "exporter");
		
		// get exporters
		HashMap<String, Exporter> exporters = new HashMap<>();
		exporters.put("mid",  new MidiExporter());
		exporters.put("mpl",  new MidicaPLExporter());
		exporters.put("alda", new AldaExporter());
		
		for (String dirStr : directories) {
			File dir = new File(dirStr);
			
			for (File file : dir.listFiles()) {
				
				// parse
				if (!file.isFile())
					continue;
				if (!file.getName().endsWith(".midica") && !file.getName().endsWith(".midicapl") && !file.getName().endsWith(".mpl"))
					continue;
				parser.parse(file);
				
				// export
				for (Entry<String, Exporter> entry : exporters.entrySet()) {
					String   extension = entry.getKey();
					Exporter exporter  = entry.getValue();
					File decompiledFile = Foreign.createTempFile(extension, null);
					exporter.export(decompiledFile);
					
					// clean up
					Foreign.deleteTempFile(decompiledFile);
				}
			}
		}
	}
}
