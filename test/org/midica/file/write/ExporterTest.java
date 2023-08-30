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
import java.util.Arrays;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.midica.TestUtil;
import org.midica.file.Foreign;
import org.midica.file.ForeignException;
import org.midica.file.read.AldaImporter;
import org.midica.file.read.MidiParser;
import org.midica.file.read.MidicaPLParser;
import org.midica.file.read.ParseException;
import org.midica.file.read.SequenceParser;
import org.midica.file.write.MidicaPLExporter;

/**
 * This is the test class for file exporters.
 * 
 * @author Jan Trukenmüller
 */
public class ExporterTest {
	
	// The following variables control which file types are tested in which part
	// of the testing run.
	// In manual testing runs, some formats may be commented out in order to save time.
	// (Especially ALDA imports can take quite some time to be processed.)
	// In automatic runs via commit hook, all types should be tested.
	private static final String[] importTypes = new String[] {
		"mpl",
		"mid",
		"alda"
	};
	private static final String[] exportTypes = new String[] {
		"mpl",
		"mid",
		"alda"
	};
	private static final String[] reImportTypes = new String[] {
		"mpl",
		"mid",
		"alda"
	};
	
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
	 * Tests MidicaPL files, if they can be imported, exported, and then imported again
	 * without exception.
	 * 
	 * The directories to be tested are:
	 * 
	 * - the example files
	 * - the working unit test files for the {@link MidicaPLParser}
	 * - the MidicaPL test files for the decompiler
	 * - real-world midi files that are not committed because of copyright
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
	void testImportExportReimportDirectories() throws ParseException, ForeignException, ExportException {
		
		// get directories to be searched
		ArrayList<String> directories = new ArrayList<>();
		directories.add(System.getProperty("user.dir")  + File.separator + "examples");
		directories.add(TestUtil.getTestfileDirectory() + File.separator + "working");
		directories.add(TestUtil.getTestfileDirectory() + File.separator + "exporter");
		directories.add(TestUtil.getTestfileDirectory() + File.separator + "midi-real-world");
		
		for (String dirStr : directories) {
			File dir = new File(dirStr);
			
			for (File file : dir.listFiles()) {
				SequenceParser importer;
				
				// choose importer
				String sourceType;
				if (!file.isFile())
					continue;
				if (file.getName().endsWith(".midica") || file.getName().endsWith(".midicapl") || file.getName().endsWith(".mpl"))
					sourceType = "mpl";
				else if (file.getName().endsWith(".alda"))
					sourceType = "alda";
				else if (file.getName().endsWith(".mid") || file.getName().endsWith(".midi") || file.getName().endsWith(".kar"))
					sourceType = "mid";
				else
					continue;
				importer = getImporter(sourceType);
				
				// skip import?
				if (!Arrays.asList(importTypes).contains(sourceType))
					continue;
				
				for (String exportType : exportTypes) {
					
					// import source file
					try {
						importer.parse(file);
					}
					catch (Exception e) {
						System.err.println("failed to import source file: " + file.getAbsolutePath());
						throw e;
					}
					
					// export to target format
					String   extension  = exportType;
					Exporter exporter   = getExporter(exportType);
					File decompiledFile = Foreign.createTempFile(extension, null);
					try {
						exporter.export(decompiledFile);
					}
					catch (Exception e) {
						System.err.println("failed to export file."
							+ " Source: " + file.getAbsolutePath()
							+ " Target: " + decompiledFile.getAbsolutePath());
						throw e;
					}
					
					// skip re-import?
					if (!Arrays.asList(reImportTypes).contains(exportType)) {
						Foreign.deleteTempFile(decompiledFile);
						continue;
					}
					
					// re-import
					SequenceParser reImporter = getImporter(exportType);
					try {
						reImporter.parse(decompiledFile);
					}
					catch (Exception e) {
						System.err.println("failed to re-import exported file."
							+ " Source: " + file.getAbsolutePath()
							+ " Target: " + decompiledFile.getAbsolutePath());
						throw e;
					}
					
					// clean up
					Foreign.deleteTempFile(decompiledFile);
				}
			}
		}
	}
	
	/**
	 * Creates and returns an exporter for the given file type.
	 * 
	 * @param type    mpl, mid or alda (for MidicaPL, MIDI or ALDA)
	 * @return the exporter.
	 * @throws IllegalArgumentException if an unknown file type is given.
	 */
	private Exporter getExporter(String type) throws IllegalArgumentException {
		
		if ("mid".equals(type))
			return new MidiExporter();
		if ("mpl".equals(type))
			return new MidicaPLExporter();
		if ("alda".equals(type))
			return new AldaExporter();
		
		throw new IllegalArgumentException("unknown exporter type: " + type);
	}
	
	/**
	 * Creates and returns an importer for the given file type.
	 * 
	 * @param type    mpl, mid or alda (for MidicaPL, MIDI or ALDA)
	 * @return the importer.
	 * @throws IllegalArgumentException if an unknown file type is given.
	 */
	private SequenceParser getImporter(String type) throws IllegalArgumentException {
		
		if ("mid".equals(type))
			return new MidiParser();
		if ("mpl".equals(type))
			return new MidicaPLParser(true);
		if ("alda".equals(type))
			return new AldaImporter();
		
		throw new IllegalArgumentException("unknown importer type: " + type);
	}
}
