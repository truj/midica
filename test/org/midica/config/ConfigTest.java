/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.config;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.InvocationTargetException;

import javax.swing.JComboBox;

import org.junit.Test;
import org.midica.Midica;
import org.midica.TestUtil;
import org.midica.ui.UiView;

/**
 * This is the test class for {@link Config}.
 * 
 * @author Jan Trukenmüller
 */
public class ConfigTest {
	
	private static UiView         uiView;
	private static JComboBox<?>[] cbxs;
	
	/**
	 * Initializes midica in test mode, if not yet done.
	 * 
	 * @throws InvocationTargetException  on interruptions while initializing Midica.
	 * @throws InterruptedException       on exceptions while initializing Midica.
	 */
	public ConfigTest() throws InvocationTargetException, InterruptedException  {
		TestUtil.initMidica();
		
		uiView = Midica.uiController.getView();
		cbxs   = uiView.getConfigComboboxes();
	}
	
	/**
	 * Tests if changing the config checkboxes updates the configured values correctly.
	 */
	@Test
	public void testConfigChanges() {
		
		// default config
		assertEquals( Config.CBX_LANG_ENGLISH,             Config.get(Config.LANGUAGE)   );
		assertEquals( Config.CBX_NOTE_ID_INTERNATIONAL_LC, Config.get(Config.NOTE)       );
		assertEquals( Config.CBX_HALFTONE_ID_SHARP,        Config.get(Config.HALF_TONE)  );
		assertEquals( Config.CBX_OCTAVE_PLUS_MINUS_N,      Config.get(Config.OCTAVE)     );
		assertEquals( Config.CBX_SYNTAX_MIXED,             Config.get(Config.SYNTAX)     );
		assertEquals( Config.CBX_PERC_EN_1,                Config.get(Config.PERCUSSION) );
		assertEquals( Config.CBX_INSTR_EN_1,               Config.get(Config.INSTRUMENT) );
		
		// change language
		cbxs[0].setSelectedIndex(1);
		assertEquals(Config.CBX_LANG_GERMAN, Config.get(Config.LANGUAGE));
		cbxs[0].setSelectedIndex(0);
		assertEquals(Config.CBX_LANG_ENGLISH, Config.get(Config.LANGUAGE));
		
		// change note system
		cbxs[1].setSelectedIndex(1);
		assertEquals(Config.CBX_NOTE_ID_INTERNATIONAL_UC, Config.get(Config.NOTE));
		cbxs[1].setSelectedIndex(2);
		assertEquals(Config.CBX_NOTE_ID_ITALIAN_LC, Config.get(Config.NOTE));
		cbxs[1].setSelectedIndex(3);
		assertEquals(Config.CBX_NOTE_ID_ITALIAN_UC, Config.get(Config.NOTE));
		cbxs[1].setSelectedIndex(4);
		assertEquals(Config.CBX_NOTE_ID_GERMAN_LC, Config.get(Config.NOTE));
		cbxs[1].setSelectedIndex(5);
		assertEquals(Config.CBX_NOTE_ID_GERMAN_UC, Config.get(Config.NOTE));
		cbxs[1].setSelectedIndex(0);
		assertEquals(Config.CBX_NOTE_ID_INTERNATIONAL_LC, Config.get(Config.NOTE));
		
		// change half tone system
		cbxs[2].setSelectedIndex(1);
		assertEquals(Config.CBX_HALFTONE_ID_DIESIS, Config.get(Config.HALF_TONE));
		cbxs[2].setSelectedIndex(2);
		assertEquals(Config.CBX_HALFTONE_ID_CIS, Config.get(Config.HALF_TONE));
		cbxs[2].setSelectedIndex(0);
		assertEquals(Config.CBX_HALFTONE_ID_SHARP, Config.get(Config.HALF_TONE));
		
		// change sharp/flat
		cbxs[3].setSelectedIndex(1);
		assertEquals(Config.CBX_SHARPFLAT_FLAT, Config.get(Config.SHARP_FLAT));
		cbxs[3].setSelectedIndex(0);
		assertEquals(Config.CBX_SHARPFLAT_SHARP, Config.get(Config.SHARP_FLAT));
		
		// change octave naming
		cbxs[4].setSelectedIndex(1);
		assertEquals(Config.CBX_OCTAVE_PLUS_MINUS, Config.get(Config.OCTAVE));
		cbxs[4].setSelectedIndex(2);
		assertEquals(Config.CBX_OCTAVE_INTERNATIONAL, Config.get(Config.OCTAVE));
		cbxs[4].setSelectedIndex(3);
		assertEquals(Config.CBX_OCTAVE_GERMAN, Config.get(Config.OCTAVE));
		cbxs[4].setSelectedIndex(0);
		assertEquals(Config.CBX_OCTAVE_PLUS_MINUS_N, Config.get(Config.OCTAVE));
		
		// change syntax
		cbxs[5].setSelectedIndex(1);
		assertEquals(Config.CBX_SYNTAX_LOWER, Config.get(Config.SYNTAX));
		cbxs[5].setSelectedIndex(2);
		assertEquals(Config.CBX_SYNTAX_UPPER, Config.get(Config.SYNTAX));
		cbxs[5].setSelectedIndex(0);
		assertEquals(Config.CBX_SYNTAX_MIXED, Config.get(Config.SYNTAX));
		
		// change percussion language
		cbxs[6].setSelectedIndex(1);
		assertEquals(Config.CBX_PERC_DE_1, Config.get(Config.PERCUSSION));
		cbxs[6].setSelectedIndex(0);
		assertEquals(Config.CBX_PERC_EN_1, Config.get(Config.PERCUSSION));
		
		// change percussion language
		cbxs[7].setSelectedIndex(1);
		assertEquals(Config.CBX_INSTR_DE_1, Config.get(Config.INSTRUMENT));
		cbxs[7].setSelectedIndex(0);
		assertEquals(Config.CBX_INSTR_EN_1, Config.get(Config.INSTRUMENT));
	}
}
