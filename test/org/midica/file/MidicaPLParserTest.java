/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import javax.sound.midi.MidiEvent;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.midica.config.Config;
import org.midica.config.Dict;
import org.midica.midi.SequenceCreator;
import org.midica.ui.widget.MidicaFileChooser;

/**
 * This is the test class for {@link org.midica.file.MidicaPLParser}.
 * 
 * @author Jan Trukenmüller
 */
class MidicaPLParserTest extends MidicaPLParser {
	
	public MidicaPLParserTest() {
		super(true);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		Config.init();
		Config.restoreDefaults(""); // ignore config file
		Dict.init();
		
		// Indirectly make sure that the static ConfigComboboxModel for MidicaPL file charsets
		// will be initialized. Otherwise parse() would fail later.
		new MidicaFileChooser("", 1, null, true);
	}
	
	/**
	 * Test method for {@link org.midica.file.MidicaPLParser}.
	 */
	@Test
	void testParseDuration() {
		try {
			assertEquals(   480, parseDuration("/4"),    "/4"    );
			assertEquals(  1920, parseDuration("/1"),    "/1"    );
			assertEquals(  1920, parseDuration("*1"),    "*1"    );
			assertEquals(  7680, parseDuration("*4"),    "*4"    );
			assertEquals( 11520, parseDuration("*4."),   "*4."   );
			assertEquals( 13440, parseDuration("*4.."),  "*4.."  );
			assertEquals( 13440, parseDuration("*4.."),  "*4.."  );
			assertEquals( 14400, parseDuration("*4..."), "*4..." );
			assertEquals(  5120, parseDuration("*4t"),   "*4t"   );
			assertEquals(  3413, parseDuration("*4tt"),  "*4tt"  );
			assertEquals(  2276, parseDuration("*4ttt"), "*4ttt" );
		}
		catch (ParseException e) {
			fail("Exception not expected for tested durations");
		}
	}
	
	/**
	 * Tests for parsing full source files.
	 * 
	 * The files are located in test/midicapl
	 * @throws ParseException if something went wrong.
	 */
	@Test
	void testParseFiles() throws ParseException {
		
		// expected tickstamps after parsing
		parse(getSourceFile("globals"));
		assertEquals(  960, instruments.get(0).getCurrentTicks() );
		assertEquals( 1440, instruments.get(1).getCurrentTicks() );
	}
	
	/**
	 * Returns a source file for testing the parse() method.
	 * 
	 * @param name file name without the extension .midicapl
	 * @return file object of the file to be tested
	 */
	private static File getSourceFile(String name) {
		String sourceDir = System.getProperty("user.dir") + File.separator
			+ "test" + File.separator + "org" + File.separator + "midica" + File.separator
			+ "testfiles" + File.separator;
		File file = new File(sourceDir + name + ".midica");
		
		return file;
	}
}
