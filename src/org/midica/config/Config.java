/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.config;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.TreeMap;
import java.util.TreeSet;

import org.midica.Midica;
import org.midica.ui.model.ComboboxStringOption;
import org.midica.ui.model.ConfigComboboxModel;

/**
 * This class handles configuration issues.
 * It contains functionality to get and set config values and to read and write the config file.
 * 
 * It is not meant to instantiate any objects and contains only static methods.
 * 
 * @author Jan Trukenmüller
 */
public class Config {
	
	public static final String DEFAULT_CHARSET_MPL        = Charset.defaultCharset().name();
	public static final String DEFAULT_CHARSET_MID        = "ISO-8859-1";
	public static final String DEFAULT_CHARSET_EXPORT_MPL = Charset.defaultCharset().name();
	public static final String DEFAULT_CHARSET_EXPORT_MID = "ISO-8859-1";
	
	// keys for the config dropdown boxes
	public static final String LANGUAGE   = "language";
	public static final String HALF_TONE  = "half_tone";
	public static final String NOTE       = "note";
	public static final String OCTAVE     = "octave";
	public static final String SYNTAX     = "syntax";
	public static final String PERCUSSION = "percussion";
	public static final String INSTRUMENT = "instruments";
	
	// keys for directories and paths
	public static final String DIRECTORY_MPL        = "directory_mpl";
	public static final String DIRECTORY_MID        = "directory_mid";
	public static final String DIRECTORY_SF2        = "directory_sf2";
	public static final String DIRECTORY_EXPORT_MPL = "directory_export_mpl";
	public static final String DIRECTORY_EXPORT_MID = "directory_export_mid";
	public static final String PATH_SF2             = "path_sf2";
	public static final String PATH_MIDICAPL        = "path_midicapl";
	public static final String PATH_MIDI            = "path_midi";
	public static final String REMEMBER_SF2         = "remember_sf2";
	public static final String REMEMBER_MIDICAPL    = "remember_midicapl";
	public static final String REMEMBER_MIDI        = "remember_midi";
	
	// charsets
	public static final String CHARSET_MPL        = "charset_mpl";
	public static final String CHARSET_MID        = "charset_mid";
	public static final String CHARSET_EXPORT_MPL = "charset_export_mpl";
	public static final String CHARSET_EXPORT_MID = "charset_export_mid";
	
	// language combobox
	public static final String CBX_LANG_ENGLISH = "English";
	public static final String CBX_LANG_GERMAN  = "Deutsch";
	public static final String[] CBX_LANGUAGES = {
		CBX_LANG_ENGLISH,
		CBX_LANG_GERMAN,
	};
	public static ArrayList<ComboboxStringOption> CBX_LANGUAGE_OPTIONS = null;
	
	// note system combobox
	public  static final String   CBX_NOTE_ID_INTERNATIONAL_LC = "cbx_note_id_international_lc";
	public  static final String   CBX_NOTE_ID_INTERNATIONAL_UC = "cbx_note_id_international_uc";
	public  static final String   CBX_NOTE_ID_ITALIAN_LC       = "cbx_note_id_italian_lc";
	public  static final String   CBX_NOTE_ID_ITALIAN_UC       = "cbx_note_id_italian_uc";
	public  static final String   CBX_NOTE_ID_GERMAN_LC        = "cbx_note_id_german_lc";
	public  static final String   CBX_NOTE_ID_GERMAN_UC        = "cbx_note_id_german_uc";
	private static final String[] CBX_NOTE_IDENTIFIERS = {
		CBX_NOTE_ID_INTERNATIONAL_LC,
		CBX_NOTE_ID_INTERNATIONAL_UC,
		CBX_NOTE_ID_ITALIAN_LC,
		CBX_NOTE_ID_ITALIAN_UC,
		CBX_NOTE_ID_GERMAN_LC,
		CBX_NOTE_ID_GERMAN_UC,
	};
	private static ArrayList<ComboboxStringOption> CBX_NOTE_OPTIONS = null;
	
	// octave naming combobox
	public  static final String   CBX_OCTAVE_PLUS_MINUS_N  = "cbx_octave_plus_minus_n";
	public  static final String   CBX_OCTAVE_PLUS_MINUS    = "cbx_octave_plus_minus";
	public  static final String   CBX_OCTAVE_INTERNATIONAL = "cbx_octave_international";
	public  static final String   CBX_OCTAVE_GERMAN        = "cbx_octave_german";
	private static final String[] CBX_OCTAVE_IDENTIFIERS = {
		CBX_OCTAVE_PLUS_MINUS_N,
		CBX_OCTAVE_PLUS_MINUS,
		CBX_OCTAVE_INTERNATIONAL,
		CBX_OCTAVE_GERMAN,
	};
	private static ArrayList<ComboboxStringOption> CBX_OCTAVE_OPTIONS = null;
	
	// half tone combobox
	public  static final String   CBX_HALFTONE_ID_SHARP   = "cbx_halftone_id_sharp";
	public  static final String   CBX_HALFTONE_ID_FLAT    = "cbx_halftone_id_flat";
	public  static final String   CBX_HALFTONE_ID_DIESIS  = "cbx_halftone_id_diesis";
	public  static final String   CBX_HALFTONE_ID_BEMOLLE = "cbx_halftone_id_bemolle";
	public  static final String   CBX_HALFTONE_ID_CIS     = "cbx_halftone_id_cis";
	public  static final String   CBX_HALFTONE_ID_DES     = "cbx_halftone_id_des";
	private static final String[] CBX_HALFTONE_IDENTIFIERS = {
		CBX_HALFTONE_ID_SHARP,
		CBX_HALFTONE_ID_FLAT,
		CBX_HALFTONE_ID_DIESIS,
		CBX_HALFTONE_ID_BEMOLLE,
		CBX_HALFTONE_ID_CIS,
		CBX_HALFTONE_ID_DES,
	};
	private static ArrayList<ComboboxStringOption> CBX_HALFTONE_OPTIONS = null;
	
	// syntax combobox
	public  static final String   CBX_SYNTAX_MIXED       = "cbx_syntax_mixed";
	public  static final String   CBX_SYNTAX_LOWER       = "cbx_syntax_lower";
	public  static final String   CBX_SYNTAX_UPPER       = "cbx_syntax_upper";
	private static final String[] CBX_SYNTAX_IDENTIFIERS = {
		CBX_SYNTAX_MIXED,
		CBX_SYNTAX_LOWER,
		CBX_SYNTAX_UPPER,
	};
	private static ArrayList<ComboboxStringOption> CBX_SYNTAX_OPTIONS = null;
	
	// percussion combobox
	public  static final String   CBX_PERC_EN_1 = "cbx_perc_en_1";
	public  static final String   CBX_PERC_DE_1 = "cbx_perc_de_1";
	private static final String[] CBX_PERCUSSION_IDENTIFIERS = {
		CBX_PERC_EN_1,
		CBX_PERC_DE_1,
	};
	private static ArrayList<ComboboxStringOption> CBX_PERCUSSION_OPTIONS = null;
	
	// instrument naming combobox
	public  static final String   CBX_INSTR_EN_1 = "cbx_instr_en_1";
	public  static final String   CBX_INSTR_DE_1 = "cbx_instr_de_1";
	private static final String[] CBX_INSTRUMENT_IDENTIFIERS = {
		CBX_INSTR_EN_1,
		CBX_INSTR_DE_1,
	};
	private static ArrayList<ComboboxStringOption> CBX_INSTRUMENT_OPTIONS = null;
	
	// private constants
	private static File configFile;
	private static HashMap<String, String>              defaults        = null;
	private static TreeMap<String, TreeSet<KeyBinding>> defaultBindings = null;
	private static TreeMap<String, String>              config          = null;
	private static TreeMap<String, TreeSet<KeyBinding>> keyBindings     = null;
	
	/**
	 * This class is only used statically so a public constructor is not needed.
	 */
	private Config() {
	}
	
	/**
	 * Initializes the configuration on startup. This includes:
	 * 
	 * - Setting default configurations
	 * - Reading the config file and overwriting the defaults with values configured
	 *   in that file
	 * - Initializing the config comboboxes in the GUI
	 */
	public static void init() {
		
		String homeDir  = System.getProperty( "user.home" );
		String fileName = ".midica.conf";
		if (Midica.useLocalConfig) {
			configFile = new File( homeDir + File.separator + fileName );
		}
		
		restoreDefaults( homeDir );
		readConfigFile();
		
		initComboBoxes();
		initLocale();
	}
	
	/**
	 * Sets the current configuration to the default configuration.
	 * Must be public because we must be able to call it from test classes as well.
	 * 
	 * @param homeDir Home directory of the current user
	 */
	public static void restoreDefaults( String homeDir ) {
		
		// define default config
		defaults = new HashMap<>();
		defaults.put( LANGUAGE,    CBX_LANG_ENGLISH             );
		defaults.put( HALF_TONE,   CBX_HALFTONE_ID_SHARP        );
		defaults.put( NOTE,        CBX_NOTE_ID_INTERNATIONAL_LC );
		defaults.put( OCTAVE,      CBX_OCTAVE_PLUS_MINUS_N      );
		defaults.put( SYNTAX,      CBX_SYNTAX_MIXED             );
		defaults.put( PERCUSSION,  CBX_PERC_EN_1                );
		defaults.put( INSTRUMENT,  CBX_INSTR_EN_1               );
		
		defaults.put( DIRECTORY_MPL,        homeDir );
		defaults.put( DIRECTORY_MID,        homeDir );
		defaults.put( DIRECTORY_SF2,        homeDir );
		defaults.put( DIRECTORY_EXPORT_MPL, homeDir );
		defaults.put( DIRECTORY_EXPORT_MID, homeDir );
		defaults.put( REMEMBER_MIDICAPL,    "false" );
		defaults.put( REMEMBER_MIDI,        "false" );
		defaults.put( REMEMBER_SF2,         "false" );
		defaults.put( PATH_SF2,             ""      );
		defaults.put( PATH_MIDICAPL,        ""      );
		defaults.put( PATH_MIDI,            ""      );
		
		defaults.put( CHARSET_MPL,        DEFAULT_CHARSET_MPL        );
		defaults.put( CHARSET_MID,        DEFAULT_CHARSET_MID        );
		defaults.put( CHARSET_EXPORT_MPL, DEFAULT_CHARSET_EXPORT_MPL );
		defaults.put( CHARSET_EXPORT_MID, DEFAULT_CHARSET_EXPORT_MID );
		
		// init config with defaults
		config = new TreeMap<>();
		for ( String key : defaults.keySet() ) {
			set( key, defaults.get(key) );
		}
		
		// define default key bindings
		defaultBindings = new TreeMap<>();
		addDefaultKeyBinding( Dict.KEY_MAIN_INFO,                KeyEvent.VK_I,        0                          );
		addDefaultKeyBinding( Dict.KEY_MAIN_PLAYER,              KeyEvent.VK_P,        0                          );
		addDefaultKeyBinding( Dict.KEY_MAIN_IMPORT_MPL,          KeyEvent.VK_O,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_MAIN_IMPORT_MID,          KeyEvent.VK_M,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_MAIN_IMPORT_SF,           KeyEvent.VK_S,        InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_MAIN_EXPORT_MID,          KeyEvent.VK_S,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_MAIN_EXPORT_MPL,          KeyEvent.VK_E,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_MAIN_CBX_LANGUAGE,        KeyEvent.VK_L,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_MAIN_CBX_NOTE,            KeyEvent.VK_N,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_MAIN_CBX_HALFTONE,        KeyEvent.VK_H,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_MAIN_CBX_OCTAVE,          KeyEvent.VK_O,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_MAIN_CBX_SYNTAX,          KeyEvent.VK_S,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_MAIN_CBX_PERCUSSION,      KeyEvent.VK_P,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_MAIN_CBX_INSTRUMENT,      KeyEvent.VK_I,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CLOSE,             KeyEvent.VK_ESCAPE,   InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_PLAYER_PLAY,              KeyEvent.VK_P,        0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_PLAY,              KeyEvent.VK_SPACE,    0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_REPARSE,           KeyEvent.VK_F5,       0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_INFO,              KeyEvent.VK_I,        0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_SOUNDCHECK,        KeyEvent.VK_S,        0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_MEMORIZE,          KeyEvent.VK_M,        0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_JUMP_FIELD,        KeyEvent.VK_F,        0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_GO,                KeyEvent.VK_G,        0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_LYRICS,            KeyEvent.VK_L,        0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_STOP,              KeyEvent.VK_ESCAPE,   0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_FAST_REWIND,       KeyEvent.VK_UP,       0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_REWIND,            KeyEvent.VK_LEFT,     0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_FORWARD,           KeyEvent.VK_RIGHT,    0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_FAST_FORWARD,      KeyEvent.VK_DOWN,     0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_BEGIN,             KeyEvent.VK_HOME,     0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_END,               KeyEvent.VK_END,      0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_VOL_FLD,           KeyEvent.VK_V,        0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_VOL_SLD,           KeyEvent.VK_V,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_TEMPO_FLD,         KeyEvent.VK_T,        0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_TEMPO_SLD,         KeyEvent.VK_T,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_TRANSPOSE_FLD,     KeyEvent.VK_T,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_PLAYER_TRANSPOSE_SLD,     KeyEvent.VK_T,        InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_VOL_FLD,        KeyEvent.VK_V,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_VOL_SLD,        KeyEvent.VK_V,        InputEvent.ALT_DOWN_MASK  | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_0,              KeyEvent.VK_0,        0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_1,              KeyEvent.VK_1,        0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_2,              KeyEvent.VK_2,        0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_3,              KeyEvent.VK_3,        0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_4,              KeyEvent.VK_4,        0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_5,              KeyEvent.VK_5,        0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_6,              KeyEvent.VK_6,        0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_7,              KeyEvent.VK_7,        0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_8,              KeyEvent.VK_8,        0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_9,              KeyEvent.VK_P,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_9,              KeyEvent.VK_9,        0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_10,             KeyEvent.VK_0,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_11,             KeyEvent.VK_1,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_12,             KeyEvent.VK_2,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_13,             KeyEvent.VK_3,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_14,             KeyEvent.VK_4,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_15,             KeyEvent.VK_5,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_0,              KeyEvent.VK_NUMPAD0,  0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_1,              KeyEvent.VK_NUMPAD1,  0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_2,              KeyEvent.VK_NUMPAD2,  0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_3,              KeyEvent.VK_NUMPAD3,  0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_4,              KeyEvent.VK_NUMPAD4,  0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_5,              KeyEvent.VK_NUMPAD5,  0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_6,              KeyEvent.VK_NUMPAD6,  0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_7,              KeyEvent.VK_NUMPAD7,  0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_8,              KeyEvent.VK_NUMPAD8,  0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_9,              KeyEvent.VK_NUMPAD9,  0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_10,             KeyEvent.VK_NUMPAD0,  InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_11,             KeyEvent.VK_NUMPAD1,  InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_12,             KeyEvent.VK_NUMPAD2,  InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_13,             KeyEvent.VK_NUMPAD3,  InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_14,             KeyEvent.VK_NUMPAD4,  InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_15,             KeyEvent.VK_NUMPAD5,  InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_0_M,            KeyEvent.VK_0,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_1_M,            KeyEvent.VK_1,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_2_M,            KeyEvent.VK_2,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_3_M,            KeyEvent.VK_3,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_4_M,            KeyEvent.VK_4,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_5_M,            KeyEvent.VK_5,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_6_M,            KeyEvent.VK_6,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_7_M,            KeyEvent.VK_7,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_8_M,            KeyEvent.VK_8,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_9_M,            KeyEvent.VK_9,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_10_M,           KeyEvent.VK_0,        InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_11_M,           KeyEvent.VK_1,        InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_12_M,           KeyEvent.VK_2,        InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_13_M,           KeyEvent.VK_3,        InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_14_M,           KeyEvent.VK_4,        InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_15_M,           KeyEvent.VK_5,        InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_0_M,            KeyEvent.VK_NUMPAD0,  InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_1_M,            KeyEvent.VK_NUMPAD1,  InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_2_M,            KeyEvent.VK_NUMPAD2,  InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_3_M,            KeyEvent.VK_NUMPAD3,  InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_4_M,            KeyEvent.VK_NUMPAD4,  InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_5_M,            KeyEvent.VK_NUMPAD5,  InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_6_M,            KeyEvent.VK_NUMPAD6,  InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_7_M,            KeyEvent.VK_NUMPAD7,  InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_8_M,            KeyEvent.VK_NUMPAD8,  InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_9_M,            KeyEvent.VK_NUMPAD9,  InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_10_M,           KeyEvent.VK_NUMPAD0,  InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_11_M,           KeyEvent.VK_NUMPAD1,  InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_12_M,           KeyEvent.VK_NUMPAD2,  InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_13_M,           KeyEvent.VK_NUMPAD3,  InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_14_M,           KeyEvent.VK_NUMPAD4,  InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_15_M,           KeyEvent.VK_NUMPAD5,  InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_0_S,            KeyEvent.VK_0,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_1_S,            KeyEvent.VK_1,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_2_S,            KeyEvent.VK_2,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_3_S,            KeyEvent.VK_3,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_4_S,            KeyEvent.VK_4,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_5_S,            KeyEvent.VK_5,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_6_S,            KeyEvent.VK_6,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_7_S,            KeyEvent.VK_7,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_8_S,            KeyEvent.VK_8,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_9_S,            KeyEvent.VK_9,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_10_S,           KeyEvent.VK_0,        InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_11_S,           KeyEvent.VK_1,        InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_12_S,           KeyEvent.VK_2,        InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_13_S,           KeyEvent.VK_3,        InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_14_S,           KeyEvent.VK_4,        InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_15_S,           KeyEvent.VK_5,        InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_0_S,            KeyEvent.VK_NUMPAD0,  InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_1_S,            KeyEvent.VK_NUMPAD1,  InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_2_S,            KeyEvent.VK_NUMPAD2,  InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_3_S,            KeyEvent.VK_NUMPAD3,  InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_4_S,            KeyEvent.VK_NUMPAD4,  InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_5_S,            KeyEvent.VK_NUMPAD5,  InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_6_S,            KeyEvent.VK_NUMPAD6,  InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_7_S,            KeyEvent.VK_NUMPAD7,  InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_8_S,            KeyEvent.VK_NUMPAD8,  InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_9_S,            KeyEvent.VK_NUMPAD9,  InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_10_S,           KeyEvent.VK_NUMPAD0,  InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_11_S,           KeyEvent.VK_NUMPAD1,  InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_12_S,           KeyEvent.VK_NUMPAD2,  InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_13_S,           KeyEvent.VK_NUMPAD3,  InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_14_S,           KeyEvent.VK_NUMPAD4,  InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_15_S,           KeyEvent.VK_NUMPAD5,  InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CLOSE,         KeyEvent.VK_ESCAPE,   0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_PLAY,          KeyEvent.VK_P,        0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_FILTER_INSTR,  KeyEvent.VK_F,        0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_FILTER_NOTE,   KeyEvent.VK_F,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_INSTR,         KeyEvent.VK_I,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_NOTE,          KeyEvent.VK_N,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_VOL_FLD,       KeyEvent.VK_V,        0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_VOL_SLD,       KeyEvent.VK_V,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_VEL_FLD,       KeyEvent.VK_V,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_VEL_SLD,       KeyEvent.VK_V,        InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_DURATION,      KeyEvent.VK_D,        0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_KEEP,          KeyEvent.VK_K,        0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_0,          KeyEvent.VK_NUMPAD0,  0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_1,          KeyEvent.VK_NUMPAD1,  0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_2,          KeyEvent.VK_NUMPAD2,  0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_3,          KeyEvent.VK_NUMPAD3,  0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_4,          KeyEvent.VK_NUMPAD4,  0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_5,          KeyEvent.VK_NUMPAD5,  0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_6,          KeyEvent.VK_NUMPAD6,  0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_7,          KeyEvent.VK_NUMPAD7,  0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_8,          KeyEvent.VK_NUMPAD8,  0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_9,          KeyEvent.VK_NUMPAD9,  0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_10,         KeyEvent.VK_NUMPAD0,  InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_11,         KeyEvent.VK_NUMPAD1,  InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_12,         KeyEvent.VK_NUMPAD2,  InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_13,         KeyEvent.VK_NUMPAD3,  InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_14,         KeyEvent.VK_NUMPAD4,  InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_15,         KeyEvent.VK_NUMPAD5,  InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_0,          KeyEvent.VK_0,        0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_1,          KeyEvent.VK_1,        0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_2,          KeyEvent.VK_2,        0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_3,          KeyEvent.VK_3,        0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_4,          KeyEvent.VK_4,        0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_5,          KeyEvent.VK_5,        0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_6,          KeyEvent.VK_6,        0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_7,          KeyEvent.VK_7,        0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_8,          KeyEvent.VK_8,        0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_9,          KeyEvent.VK_P,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_9,          KeyEvent.VK_9,        0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_10,         KeyEvent.VK_0,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_11,         KeyEvent.VK_1,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_12,         KeyEvent.VK_2,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_13,         KeyEvent.VK_3,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_14,         KeyEvent.VK_4,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_15,         KeyEvent.VK_5,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_0,          KeyEvent.VK_NUMPAD0,  0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_1,          KeyEvent.VK_NUMPAD1,  0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_2,          KeyEvent.VK_NUMPAD2,  0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_3,          KeyEvent.VK_NUMPAD3,  0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_4,          KeyEvent.VK_NUMPAD4,  0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_5,          KeyEvent.VK_NUMPAD5,  0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_6,          KeyEvent.VK_NUMPAD6,  0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_7,          KeyEvent.VK_NUMPAD7,  0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_8,          KeyEvent.VK_NUMPAD8,  0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_9,          KeyEvent.VK_NUMPAD9,  0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_10,         KeyEvent.VK_NUMPAD0,  InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_11,         KeyEvent.VK_NUMPAD1,  InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_12,         KeyEvent.VK_NUMPAD2,  InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_13,         KeyEvent.VK_NUMPAD3,  InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_14,         KeyEvent.VK_NUMPAD4,  InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_15,         KeyEvent.VK_NUMPAD5,  InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_INFO_CLOSE,               KeyEvent.VK_ESCAPE,   0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_CONF,                KeyEvent.VK_C,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_CONF_NOTE,           KeyEvent.VK_N,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_CONF_PERC,           KeyEvent.VK_P,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_CONF_SYNTAX,         KeyEvent.VK_S,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_CONF_INSTR,          KeyEvent.VK_I,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_CONF_DRUMKIT,        KeyEvent.VK_D,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_SF,                  KeyEvent.VK_S,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_SF_GENERAL,          KeyEvent.VK_G,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_SF_INSTR,            KeyEvent.VK_I,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_SF_RES,              KeyEvent.VK_R,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI,                KeyEvent.VK_M,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_GENERAL,        KeyEvent.VK_G,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_KARAOKE,        KeyEvent.VK_K,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_BANKS,          KeyEvent.VK_B,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG,            KeyEvent.VK_M,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_ABOUT,               KeyEvent.VK_A,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_CONF_NOTE_FILTER,    KeyEvent.VK_F,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_CONF_NOTE_FILTER,    KeyEvent.VK_F,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_INFO_CONF_PERC_FILTER,    KeyEvent.VK_F,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_CONF_PERC_FILTER,    KeyEvent.VK_F,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_INFO_CONF_SYNTAX_FILTER,  KeyEvent.VK_F,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_CONF_SYNTAX_FILTER,  KeyEvent.VK_F,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_INFO_CONF_INSTR_FILTER,   KeyEvent.VK_F,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_CONF_INSTR_FILTER,   KeyEvent.VK_F,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_INFO_CONF_DRUMKIT_FILTER, KeyEvent.VK_F,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_CONF_DRUMKIT_FILTER, KeyEvent.VK_F,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_INFO_SF_INSTR_FILTER,     KeyEvent.VK_F,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_SF_INSTR_FILTER,     KeyEvent.VK_F,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_INFO_SF_RES_FILTER,       KeyEvent.VK_F,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_SF_RES_FILTER,       KeyEvent.VK_F,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_FILTER,     KeyEvent.VK_F,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_FILTER,     KeyEvent.VK_F,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_BANKS_TOT_PL,   KeyEvent.VK_PLUS,     0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_BANKS_TOT_PL,   KeyEvent.VK_ADD,      0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_BANKS_TOT_MIN,  KeyEvent.VK_MINUS,    0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_BANKS_TOT_MIN,  KeyEvent.VK_SUBTRACT, 0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_BANKS_TOT_TREE, KeyEvent.VK_T,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_BANKS_CH_PL,    KeyEvent.VK_PLUS,     InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_BANKS_CH_PL,    KeyEvent.VK_ADD,      InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_BANKS_CH_MIN,   KeyEvent.VK_MINUS,    InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_BANKS_CH_MIN,   KeyEvent.VK_SUBTRACT, InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_BANKS_CH_TREE,  KeyEvent.VK_T,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_PL,         KeyEvent.VK_PLUS,     0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_PL,         KeyEvent.VK_ADD,      0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_MIN,        KeyEvent.VK_MINUS,    0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_MIN,        KeyEvent.VK_SUBTRACT, 0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_TREE,       KeyEvent.VK_T,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_TABLE,      KeyEvent.VK_T,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_INDEP,   KeyEvent.VK_I,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_DEP,     KeyEvent.VK_D,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_SEL_NOD,    KeyEvent.VK_N,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_LIM_TCK,    KeyEvent.VK_L,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_TICK_FROM,  KeyEvent.VK_F,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_TICK_FROM,  KeyEvent.VK_F,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_TICK_TO,    KeyEvent.VK_T,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_LIM_TRK,    KeyEvent.VK_L,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_TRACKS_TXT, KeyEvent.VK_T,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_SHOW_IN_TR, KeyEvent.VK_S,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_SHOW_AUTO,  KeyEvent.VK_A,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_0,       KeyEvent.VK_0,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_1,       KeyEvent.VK_1,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_2,       KeyEvent.VK_2,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_3,       KeyEvent.VK_3,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_4,       KeyEvent.VK_4,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_5,       KeyEvent.VK_5,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_6,       KeyEvent.VK_6,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_7,       KeyEvent.VK_7,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_8,       KeyEvent.VK_8,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_9,       KeyEvent.VK_9,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_10,      KeyEvent.VK_0,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_11,      KeyEvent.VK_1,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_12,      KeyEvent.VK_2,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_13,      KeyEvent.VK_3,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_14,      KeyEvent.VK_4,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_15,      KeyEvent.VK_5,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_0,       KeyEvent.VK_NUMPAD0,  0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_1,       KeyEvent.VK_NUMPAD1,  0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_2,       KeyEvent.VK_NUMPAD2,  0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_3,       KeyEvent.VK_NUMPAD3,  0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_4,       KeyEvent.VK_NUMPAD4,  0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_5,       KeyEvent.VK_NUMPAD5,  0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_6,       KeyEvent.VK_NUMPAD6,  0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_7,       KeyEvent.VK_NUMPAD7,  0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_8,       KeyEvent.VK_NUMPAD8,  0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_9,       KeyEvent.VK_NUMPAD9,  0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_10,      KeyEvent.VK_NUMPAD0,  InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_11,      KeyEvent.VK_NUMPAD1,  InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_12,      KeyEvent.VK_NUMPAD2,  InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_13,      KeyEvent.VK_NUMPAD3,  InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_14,      KeyEvent.VK_NUMPAD4,  InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_15,      KeyEvent.VK_NUMPAD5,  InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_MSG_CLOSE,                KeyEvent.VK_ESCAPE,   0                          );
		addDefaultKeyBinding( Dict.KEY_MSG_CLOSE,                KeyEvent.VK_ENTER,    0                          );
		addDefaultKeyBinding( Dict.KEY_MSG_CLOSE,                KeyEvent.VK_SPACE,    0                          );
		addDefaultKeyBinding( Dict.KEY_STRING_FILTER_CLOSE,      KeyEvent.VK_ESCAPE,   0                          );
		addDefaultKeyBinding( Dict.KEY_STRING_FILTER_CLOSE,      KeyEvent.VK_ENTER,    0                          );
		addDefaultKeyBinding( Dict.KEY_STRING_FILTER_CLEAR,      KeyEvent.VK_C,        InputEvent.ALT_DOWN_MASK   );
		
		// init key bindings with defaults
		keyBindings  = new TreeMap<>();
		for ( String id : defaultBindings.keySet() ) {
			keyBindings.put( id, defaultBindings.get(id) );
		}
	}
	
	/**
	 * Adds a key binding to the default structures.
	 * 
	 * @param id         Key binding identifier.
	 * @param keyCode    Key code to be added.
	 * @param mods       Modifiers to be added.
	 */
	private static void addDefaultKeyBinding(String id, int keyCode, int mods) {
		TreeSet<KeyBinding> bindings = defaultBindings.get(id);
		
		// not yet initialized?
		if (null == bindings) {
			bindings = new TreeSet<>();
			defaultBindings.put(id, bindings);
		}
		
		bindings.add( new KeyBinding(keyCode, mods) );
	}
	
	/**
	 * Reads the config file and sets the current configuration accordingly.
	 * Does not do anything, if the command line option --ignore-local-config has been used.
	 */
	private static void readConfigFile() {
		
		if (! Midica.useLocalConfig) {
			return;
		}
		
		if ( configFile.canRead() ) {
			try {
				// open file for reading
				FileReader     fr = new FileReader( configFile );
				BufferedReader br = new BufferedReader( fr );
				String line;
				
				// parse line by line
				while ( null != (line = br.readLine()) ) {
					parseConfig( line );
				}
				
				br.close();
			}
			catch ( FileNotFoundException e ) {
				e.printStackTrace();
			}
			catch ( IOException e ) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Saves the current configuration to the config file.
	 * Does not do anything, if the command line option --ignore-local-config has been used.
	 * 
	 * This is called if the main GUI window is closed.
	 */
	public static void writeConfigFile() {
		
		if (! Midica.useLocalConfig) {
			return;
		}
		
		try {
			// create file if not yet done
			if ( ! configFile.canWrite() ) {
				configFile.createNewFile();
			}
			
			// write config if possible
			if ( configFile.canWrite() ) {
				FileWriter     fw = new FileWriter( configFile );
				BufferedWriter bw = new BufferedWriter( fw );
				
				for ( String key : defaults.keySet() ) {
					String value = config.get( key );
					bw.write( key + " " + value );
					bw.newLine();
				}
				bw.close();
			}
		}
		catch ( FileNotFoundException e ) {
			e.printStackTrace();
		}
		catch ( IOException e ) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Reads a line of the config file.
	 * Sets the according configuration element like it is configured in that line.
	 * 
	 * @param line The line of the config file to be parsed
	 */
	private static void parseConfig( String line ) {
		line = line.replaceFirst( "\\s+$", "" ); // eliminate trailing whitespaces
		String[] splitted = line.split( " ", 2 );
		try {
			set( splitted[0], splitted[1] );
		}
		catch ( ArrayIndexOutOfBoundsException e ) {
			// nothing to set
			return;
		}
	}
	
	/**
	 * Sets a configuration element.
	 * 
	 * @param key    config key
	 * @param value  config value
	 */
	public static void set(String key, String value) {
		config.put(key, value);
	}
	
	/**
	 * Reads and returns the config value according to the given key.
	 * 
	 * @param key  Config key
	 * @return     Config value according to the given key
	 */
	public static String get(String key) {
		return config.get(key);
	}
	
	/**
	 * Reads and returns the configured key bindings according to the given identifier.
	 * The key binding is the combination of a key code and a value for the used modifiers
	 * like SHIFT, CTRL, and so on.
	 * 
	 * @param id  key binding identifier
	 * @return    configured key bindings
	 */
	public static TreeSet<KeyBinding> getKeyBindings(String id) {
		return keyBindings.get(id);
	}
	
	/**
	 * Sets the localization according to the configured language.
	 */
	public static void initLocale() {
		String lang = get(LANGUAGE);
		if (lang.equals(CBX_LANG_ENGLISH)) {
			Locale.setDefault(Locale.ENGLISH);
		}
		else if (lang.equals(CBX_LANG_GERMAN)) {
			Locale.setDefault(Locale.GERMAN);
		}
	}
	
	/**
	 * Fills the configuration comboboxes in the GUI.
	 * This is done by initializing the data models ({@link ConfigComboboxModel}) of the
	 * corresponding comboboxes.
	 */
	private static void initComboBoxes() {
		
		// language combobox
		CBX_LANGUAGE_OPTIONS = new ArrayList<>();
		for ( String id : CBX_LANGUAGES ) {
			CBX_LANGUAGE_OPTIONS.add( new ComboboxStringOption(id, id) );
		}
		ConfigComboboxModel.initModel( CBX_LANGUAGE_OPTIONS, Config.LANGUAGE );
		
		// half tone symbol
		CBX_HALFTONE_OPTIONS = new ArrayList<>();
		for ( String id : CBX_HALFTONE_IDENTIFIERS ) {
			CBX_HALFTONE_OPTIONS.add( new ComboboxStringOption(id, get(id)) );
		}
		ConfigComboboxModel.initModel( CBX_HALFTONE_OPTIONS, Config.HALF_TONE );
		
		// note system
		CBX_NOTE_OPTIONS = new ArrayList<>();
		for ( String id : CBX_NOTE_IDENTIFIERS ) {
			CBX_NOTE_OPTIONS.add( new ComboboxStringOption(id, get(id)) );
		}
		ConfigComboboxModel.initModel( CBX_NOTE_OPTIONS, Config.NOTE );
		
		// octave naming
		CBX_OCTAVE_OPTIONS = new ArrayList<>();
		for ( String id : CBX_OCTAVE_IDENTIFIERS ) {
			CBX_OCTAVE_OPTIONS.add( new ComboboxStringOption(id, get(id)) );
		}
		ConfigComboboxModel.initModel( CBX_OCTAVE_OPTIONS, Config.OCTAVE );
		
		// syntax
		CBX_SYNTAX_OPTIONS = new ArrayList<>();
		for ( String id : CBX_SYNTAX_IDENTIFIERS ) {
			CBX_SYNTAX_OPTIONS.add( new ComboboxStringOption(id, get(id)) );
		}
		ConfigComboboxModel.initModel( CBX_SYNTAX_OPTIONS, Config.SYNTAX );
		
		// percussion shortcuts
		CBX_PERCUSSION_OPTIONS = new ArrayList<>();
		for ( String id : CBX_PERCUSSION_IDENTIFIERS ) {
			CBX_PERCUSSION_OPTIONS.add( new ComboboxStringOption(id, get(id)) );
		}
		ConfigComboboxModel.initModel( CBX_PERCUSSION_OPTIONS, Config.PERCUSSION );
		
		// instrument naming
		CBX_INSTRUMENT_OPTIONS = new ArrayList<>();
		for ( String id : CBX_INSTRUMENT_IDENTIFIERS ) {
			CBX_INSTRUMENT_OPTIONS.add( new ComboboxStringOption(id, get(id)) );
		}
		ConfigComboboxModel.initModel( CBX_INSTRUMENT_OPTIONS, Config.INSTRUMENT );
	}
}
