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
import java.util.Iterator;
import java.util.Locale;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.midica.file.write.AudioExporter;
import org.midica.file.write.Decompiler;
import org.midica.ui.file.FileSelector;
import org.midica.ui.model.ComboboxStringOption;
import org.midica.ui.model.ConfigComboboxModel;
import org.midica.ui.model.MidicaTreeModel;

/**
 * This class handles configuration issues.
 * It contains functionality to get and set config values and to read and write the config file.
 * 
 * It is not meant to instantiate any objects and contains only static methods.
 * 
 * @author Jan Trukenmüller
 */
public class Config {
	
	public static final String DEFAULT_CHARSET_MPL         = Charset.defaultCharset().name();
	public static final String DEFAULT_CHARSET_MID         = "ISO-8859-1";
	public static final String DEFAULT_CHARSET_EXPORT_MPL  = Charset.defaultCharset().name();
	public static final String DEFAULT_CHARSET_EXPORT_MID  = "ISO-8859-1";
	
	// keys for the config dropdown boxes
	public static final String LANGUAGE   = "language";
	public static final String HALF_TONE  = "half_tone";
	public static final String SHARP_FLAT = "sharp_flat";
	public static final String NOTE       = "note";
	public static final String OCTAVE     = "octave";
	public static final String SYNTAX     = "syntax";
	public static final String PERCUSSION = "percussion";
	public static final String INSTRUMENT = "instruments";
	
	// keys for directories, paths and file choose tabs
	public static final String DIRECTORY_MPL           = "directory_mpl";
	public static final String DIRECTORY_MID           = "directory_mid";
	public static final String DIRECTORY_ALDA          = "directory_alda";
	public static final String DIRECTORY_ABC           = "directory_abc";
	public static final String DIRECTORY_LY            = "directory_ly";
	public static final String DIRECTORY_MSCORE        = "directory_mscore";
	public static final String DIRECTORY_SF2           = "directory_sf2";
	public static final String DIRECTORY_EXPORT_MPL    = "directory_export_mpl";
	public static final String DIRECTORY_EXPORT_MID    = "directory_export_mid";
	public static final String DIRECTORY_EXPORT_ALDA   = "directory_export_alda";
	public static final String DIRECTORY_EXPORT_AUDIO  = "directory_export_audio";
	public static final String DIRECTORY_EXPORT_ABC    = "directory_export_abc";
	public static final String DIRECTORY_EXPORT_LY     = "directory_export_ly";
	public static final String DIRECTORY_EXPORT_MSCORE = "directory_export_mscore";
	public static final String PATH_SOUND              = "path_sound";
	public static final String SOUND_URL               = "sound_url";
	public static final String PATH_MIDICAPL           = "path_midicapl";
	public static final String PATH_MIDI               = "path_midi";
	public static final String PATH_ALDA               = "path_alda";
	public static final String PATH_ABC                = "path_abc";
	public static final String PATH_LY                 = "path_ly";
	public static final String PATH_MSCORE             = "path_mscore";
	public static final String EXEC_PATH_IMP_ALDA      = "exec_path_imp_alda";
	public static final String EXEC_PATH_IMP_ABC       = "exec_path_imp_abc";
	public static final String EXEC_PATH_IMP_LY        = "exec_path_imp_ly";
	public static final String EXEC_PATH_IMP_MSCORE    = "exec_path_imp_mscore";
	public static final String EXEC_PATH_EXP_ABC       = "exec_path_exp_abc";
	public static final String EXEC_PATH_EXP_LY        = "exec_path_exp_ly";
	public static final String EXEC_PATH_EXP_MSCORE    = "exec_path_exp_mscore";
	public static final String REMEMBER_SOUND          = "remember_sound";
	public static final String REMEMBER_IMPORT         = "remember_import";
	public static final String IMPORT_TYPE             = "import_type";
	public static final String SOUND_SOURCE            = "sound_source";
	public static final String TAB_FILE_IMPORT         = "tab_file_import";
	public static final String TAB_FILE_SOUND          = "tab_file_sound";
	public static final String TAB_FILE_EXPORT         = "tab_file_export";
	
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
	public  static final String   CBX_HALFTONE_ID_SHARP  = "cbx_halftone_id_sharp";
	public  static final String   CBX_HALFTONE_ID_DIESIS = "cbx_halftone_id_diesis";
	public  static final String   CBX_HALFTONE_ID_CIS    = "cbx_halftone_id_cis";
	private static final String[] CBX_HALFTONE_IDENTIFIERS = {
		CBX_HALFTONE_ID_SHARP,
		CBX_HALFTONE_ID_DIESIS,
		CBX_HALFTONE_ID_CIS,
	};
	private static ArrayList<ComboboxStringOption> CBX_HALFTONE_OPTIONS = null;
	
	// sharp or flat
	public  static final String   CBX_SHARPFLAT_SHARP = "cbx_sharp_flat_sharp";
	public  static final String   CBX_SHARPFLAT_FLAT  = "cbx_sharp_flat_flat";
	private static final String[] CBX_SHARPFLAT_IDENTIFIERS = {
		CBX_SHARPFLAT_SHARP,
		CBX_SHARPFLAT_FLAT,
	};
	private static ArrayList<ComboboxStringOption> CBX_SHARPFLAT_OPTIONS = null;
	
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
	
	// decompile options
	public static final String DC_MUST_ADD_TICK_COMMENTS   = "dc_must_add_tick_comments";
	public static final String DC_MUST_ADD_CONFIG          = "dc_must_add_config";
	public static final String DC_MUST_ADD_QUALITY_SCORE   = "dc_must_add_quality_score";
	public static final String DC_MUST_ADD_STATISTICS      = "dc_must_add_statistics";
	public static final String DC_MUST_ADD_STRATEGY_STAT   = "dc_must_add_strategy_stat";
	public static final String DC_LENGTH_STRATEGY          = "dc_length_strategy";
	public static final String DC_MIN_TARGET_TICKS_ON      = "dc_min_target_ticks_on";
	public static final String DC_MAX_TARGET_TICKS_ON      = "dc_max_target_ticks_on";
	public static final String DC_MIN_DURATION_TO_KEEP     = "dc_min_duration_to_keep";
	public static final String DC_MAX_DURATION_TO_KEEP     = "dc_max_duration_to_keep";
	public static final String DC_LENGTH_TICK_TOLERANCE    = "dc_length_tick_tolerance";
	public static final String DC_DURATION_RATIO_TOLERANCE = "dc_duration_ratio_tolerance";
	public static final String DC_USE_PRE_DEFINED_CHORDS   = "dc_use_pre_defined_chords";
	public static final String DC_CHORD_NOTE_ON_TOLERANCE  = "dc_chord_note_on_tolerance";
	public static final String DC_CHORD_NOTE_OFF_TOLERANCE = "dc_chord_note_off_tolerance";
	public static final String DC_CHORD_VELOCITY_TOLERANCE = "dc_chord_velocity_tolerance";
	public static final String DC_USE_DOTTED_NOTES         = "dc_use_dotted_notes";
	public static final String DC_USE_DOTTED_RESTS         = "dc_use_dotted_rests";
	public static final String DC_USE_TRIPLETTED_NOTES     = "dc_use_tripletted_notes";
	public static final String DC_USE_TRIPLETTED_RESTS     = "dc_use_tripletted_rests";
	public static final String DC_USE_KARAOKE              = "dc_use_karaoke";
	public static final String DC_ALL_SYLLABLES_ORPHANED   = "dc_all_syllables_orphaned";
	public static final String DC_ORPHANED_SYLLABLES       = "dc_orphaned_syllables";
	public static final String DC_KARAOKE_ONE_CHANNEL      = "dc_karaoke_one_channel";
	public static final String DC_CTRL_CHANGE_MODE         = "dc_ctrl_change_mode";
	public static final String DC_EXTRA_GLOBALS_STR        = "dc_extra_globals_str";
	
	// audio options
	public static final String AU_ENCODING         = "au_encoding";
	public static final String AU_SAMPLE_SIZE_BITS = "au_sample_size_bits";
	public static final String AU_SAMPLE_RATE      = "au_sample_rate";
	public static final String AU_CHANNELS         = "au_channels";
	public static final String AU_IS_BIG_ENDIAN    = "au_is_big_endian";
	
	// private constants
	private static File configFile;
	private static HashMap<String, String>              defaults        = null;
	private static TreeMap<String, TreeSet<KeyBinding>> defaultBindings = null;
	private static HashMap<String, String>              dcDefaults      = null;
	private static HashMap<String, String>              auDefaults      = null;
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
		
		String homeDir  = System.getProperty("user.home");
		String fileName = ".midica.conf";
		if (Cli.useLocalConfig) {
			configFile = new File(homeDir + File.separator + fileName);
		}
		
		restoreDefaults(homeDir);
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
	public static void restoreDefaults(String homeDir) {
		
		// define default config
		defaults = new HashMap<>();
		defaults.put( LANGUAGE,    CBX_LANG_ENGLISH             );
		defaults.put( HALF_TONE,   CBX_HALFTONE_ID_SHARP        );
		defaults.put( SHARP_FLAT,  CBX_SHARPFLAT_SHARP          );
		defaults.put( NOTE,        CBX_NOTE_ID_INTERNATIONAL_LC );
		defaults.put( OCTAVE,      CBX_OCTAVE_PLUS_MINUS_N      );
		defaults.put( SYNTAX,      CBX_SYNTAX_MIXED             );
		defaults.put( PERCUSSION,  CBX_PERC_EN_1                );
		defaults.put( INSTRUMENT,  CBX_INSTR_EN_1               );
		
		defaults.put( DIRECTORY_MPL,           homeDir      );
		defaults.put( DIRECTORY_MID,           homeDir      );
		defaults.put( DIRECTORY_ALDA,          homeDir      );
		defaults.put( DIRECTORY_ABC,           homeDir      );
		defaults.put( DIRECTORY_LY,            homeDir      );
		defaults.put( DIRECTORY_MSCORE,        homeDir      );
		defaults.put( DIRECTORY_SF2,           homeDir      );
		defaults.put( DIRECTORY_EXPORT_MPL,    homeDir      );
		defaults.put( DIRECTORY_EXPORT_MID,    homeDir      );
		defaults.put( DIRECTORY_EXPORT_ALDA,   homeDir      );
		defaults.put( DIRECTORY_EXPORT_AUDIO,  homeDir      );
		defaults.put( DIRECTORY_EXPORT_ABC,    homeDir      );
		defaults.put( DIRECTORY_EXPORT_LY,     homeDir      );
		defaults.put( DIRECTORY_EXPORT_MSCORE, homeDir      );
		defaults.put( REMEMBER_IMPORT,         "false"      );
		defaults.put( REMEMBER_SOUND,          "false"      );
		defaults.put( PATH_SOUND,              ""           );
		defaults.put( SOUND_URL,               ""           );
		defaults.put( PATH_MIDICAPL,           ""           );
		defaults.put( PATH_MIDI,               ""           );
		defaults.put( PATH_ALDA,               ""           );
		defaults.put( PATH_ABC,                ""           );
		defaults.put( PATH_LY,                 ""           );
		defaults.put( PATH_MSCORE,             ""           );
		defaults.put( EXEC_PATH_IMP_ALDA,      "alda"       );
		defaults.put( EXEC_PATH_IMP_ABC,       "abc2midi"   );
		defaults.put( EXEC_PATH_IMP_LY,        "lilypond"   );
		defaults.put( EXEC_PATH_IMP_MSCORE,    "musescore3" );
		defaults.put( EXEC_PATH_EXP_ABC,       "midi2abc"   );
		defaults.put( EXEC_PATH_EXP_LY,        "midi2ly"    );
		defaults.put( EXEC_PATH_EXP_MSCORE,    "musescore3" );
		defaults.put( IMPORT_TYPE,             FileSelector.FILE_TYPE_MPL );
		defaults.put( SOUND_SOURCE,            FileSelector.FILE_TYPE_SOUND_FILE );
		defaults.put( TAB_FILE_IMPORT,         "0"          );
		defaults.put( TAB_FILE_SOUND,          "0"          );
		defaults.put( TAB_FILE_EXPORT,         "0"          );
		
		defaults.put( CHARSET_MPL,        DEFAULT_CHARSET_MPL        );
		defaults.put( CHARSET_MID,        DEFAULT_CHARSET_MID        );
		defaults.put( CHARSET_EXPORT_MPL, DEFAULT_CHARSET_EXPORT_MPL );
		defaults.put( CHARSET_EXPORT_MID, DEFAULT_CHARSET_EXPORT_MID );
		
		// init config with defaults
		config = new TreeMap<>();
		for (String key : defaults.keySet()) {
			set(key, defaults.get(key));
		}
		
		// do the same thing with file-based config settings
		dcDefaults = getDefaultDecompileConfig();
		for (String key : dcDefaults.keySet()) {
			set(key, dcDefaults.get(key));
		}
		auDefaults = getDefaultAudioExportConfig();
		for (String key : auDefaults.keySet()) {
			set(key, auDefaults.get(key));
		}
		
		// do the same thing with key bindings
		restoreDefaultKeyBindings();
	}
	
	/**
	 * Initializes all default decompile configuration values and copies them to the current config.
	 */
	public static HashMap<String, String> getDefaultDecompileConfig() {
		HashMap<String, String> dcDefaults = new HashMap<>();
		
		dcDefaults.put( DC_MUST_ADD_TICK_COMMENTS,   "" + Decompiler.DEFAULT_MUST_ADD_TICK_COMMENTS   );
		dcDefaults.put( DC_MUST_ADD_CONFIG,          "" + Decompiler.DEFAULT_MUST_ADD_CONFIG          );
		dcDefaults.put( DC_MUST_ADD_QUALITY_SCORE,   "" + Decompiler.DEFAULT_MUST_ADD_QUALITY_SCORE   );
		dcDefaults.put( DC_MUST_ADD_STATISTICS,      "" + Decompiler.DEFAULT_MUST_ADD_STATISTICS      );
		dcDefaults.put( DC_MUST_ADD_STRATEGY_STAT,   "" + Decompiler.DEFAULT_MUST_ADD_STRATEGY_STAT   );
		dcDefaults.put( DC_LENGTH_STRATEGY,          "" + Decompiler.DEFAULT_LENGTH_STRATEGY          );
		dcDefaults.put( DC_MIN_TARGET_TICKS_ON,      "" + Decompiler.DEFAULT_MIN_TARGET_TICKS_ON      );
		dcDefaults.put( DC_MAX_TARGET_TICKS_ON,      "" + Decompiler.DEFAULT_MAX_TARGET_TICKS_ON      );
		dcDefaults.put( DC_MIN_DURATION_TO_KEEP,     "" + Decompiler.DEFAULT_MIN_DURATION_TO_KEEP     );
		dcDefaults.put( DC_MAX_DURATION_TO_KEEP,     "" + Decompiler.DEFAULT_MAX_DURATION_TO_KEEP     );
		dcDefaults.put( DC_LENGTH_TICK_TOLERANCE,    "" + Decompiler.DEFAULT_LENGTH_TICK_TOLERANCE    );
		dcDefaults.put( DC_DURATION_RATIO_TOLERANCE, "" + Decompiler.DEFAULT_DURATION_RATIO_TOLERANCE );
		dcDefaults.put( DC_USE_PRE_DEFINED_CHORDS,   "" + Decompiler.DEFAULT_USE_PRE_DEFINED_CHORDS   );
		dcDefaults.put( DC_CHORD_NOTE_ON_TOLERANCE,  "" + Decompiler.DEFAULT_CHORD_NOTE_ON_TOLERANCE  );
		dcDefaults.put( DC_CHORD_NOTE_OFF_TOLERANCE, "" + Decompiler.DEFAULT_CHORD_NOTE_OFF_TOLERANCE );
		dcDefaults.put( DC_CHORD_VELOCITY_TOLERANCE, "" + Decompiler.DEFAULT_CHORD_VELOCITY_TOLERANCE );
		dcDefaults.put( DC_USE_DOTTED_NOTES,         "" + Decompiler.DEFAULT_USE_DOTTED_NOTES         );
		dcDefaults.put( DC_USE_DOTTED_RESTS,         "" + Decompiler.DEFAULT_USE_DOTTED_RESTS         );
		dcDefaults.put( DC_USE_TRIPLETTED_NOTES,     "" + Decompiler.DEFAULT_USE_TRIPLETTED_NOTES     );
		dcDefaults.put( DC_USE_TRIPLETTED_RESTS,     "" + Decompiler.DEFAULT_USE_TRIPLETTED_RESTS     );
		dcDefaults.put( DC_USE_KARAOKE,              "" + Decompiler.DEFAULT_USE_KARAOKE              );
		dcDefaults.put( DC_ALL_SYLLABLES_ORPHANED,   "" + Decompiler.DEFAULT_ALL_SYLLABLES_ORPHANED   );
		dcDefaults.put( DC_ORPHANED_SYLLABLES,       "" + Decompiler.DEFAULT_ORPHANED_SYLLABLES       );
		dcDefaults.put( DC_KARAOKE_ONE_CHANNEL,      "" + Decompiler.DEFAULT_KARAOKE_ONE_CHANNEL      );
		dcDefaults.put( DC_CTRL_CHANGE_MODE,         "" + Decompiler.DEFAULT_CTRL_CHANGE_MODE         );
		dcDefaults.put( DC_EXTRA_GLOBALS_STR,             Decompiler.DEFAULT_EXTRA_GLOBALS_STR        );
		
		return dcDefaults;
	}
	
	/**
	 * Initializes all default audio export configuration values and copies
	 * them to the current config.
	 */
	public static HashMap<String, String> getDefaultAudioExportConfig() {
		HashMap<String, String> auDefaults = new HashMap<>();
		
		auDefaults.put( AU_ENCODING,              AudioExporter.DEFAULT_ENCODING         );
		auDefaults.put( AU_SAMPLE_SIZE_BITS, "" + AudioExporter.DEFAULT_SAMPLE_SIZE_BITS );
		auDefaults.put( AU_SAMPLE_RATE,      "" + AudioExporter.DEFAULT_SAMPLE_RATE      );
		auDefaults.put( AU_CHANNELS,         "" + AudioExporter.DEFAULT_CHANNELS         );
		auDefaults.put( AU_IS_BIG_ENDIAN,    "" + AudioExporter.DEFAULT_IS_BIG_ENDIAN    );
		
		return auDefaults;
	}
	
	/**
	 * Initializes all key default key bindings and uses copies of them for the real key bindings
	 */
	private static void restoreDefaultKeyBindings() {
		
		// define default key bindings
		defaultBindings = new TreeMap<>();
		addDefaultKeyBinding( Dict.KEY_MAIN_INFO,                    KeyEvent.VK_I,        0                          );
		addDefaultKeyBinding( Dict.KEY_MAIN_PLAYER,                  KeyEvent.VK_P,        0                          );
		addDefaultKeyBinding( Dict.KEY_MAIN_IMPORT,                  KeyEvent.VK_O,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_MAIN_IMPORT_SF,               KeyEvent.VK_S,        InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_MAIN_EXPORT,                  KeyEvent.VK_S,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_MAIN_CBX_LANGUAGE,            KeyEvent.VK_L,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_MAIN_CBX_NOTE,                KeyEvent.VK_N,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_MAIN_CBX_HALFTONE,            KeyEvent.VK_H,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_MAIN_CBX_SHARPFLAT,           KeyEvent.VK_D,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_MAIN_CBX_OCTAVE,              KeyEvent.VK_O,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_MAIN_CBX_SYNTAX,              KeyEvent.VK_S,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_MAIN_CBX_PERCUSSION,          KeyEvent.VK_P,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_MAIN_CBX_INSTRUMENT,          KeyEvent.VK_I,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CLOSE,                 KeyEvent.VK_ESCAPE,   InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_PLAYER_PLAY,                  KeyEvent.VK_P,        0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_PLAY,                  KeyEvent.VK_SPACE,    0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_REPARSE,               KeyEvent.VK_F5,       0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_INFO,                  KeyEvent.VK_I,        0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_SOUNDCHECK,            KeyEvent.VK_S,        0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_MEMORIZE,              KeyEvent.VK_M,        0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_JUMP_FIELD,            KeyEvent.VK_F,        0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_GO,                    KeyEvent.VK_G,        0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_LYRICS,                KeyEvent.VK_L,        0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_STOP,                  KeyEvent.VK_ESCAPE,   0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_FAST_REWIND,           KeyEvent.VK_UP,       0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_REWIND,                KeyEvent.VK_LEFT,     0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_FORWARD,               KeyEvent.VK_RIGHT,    0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_FAST_FORWARD,          KeyEvent.VK_DOWN,     0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_BEGIN,                 KeyEvent.VK_HOME,     0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_END,                   KeyEvent.VK_END,      0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_VOL_FLD,               KeyEvent.VK_V,        0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_VOL_SLD,               KeyEvent.VK_V,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_TEMPO_FLD,             KeyEvent.VK_T,        0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_TEMPO_SLD,             KeyEvent.VK_T,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_TRANSPOSE_FLD,         KeyEvent.VK_T,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_PLAYER_TRANSPOSE_SLD,         KeyEvent.VK_T,        InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_VOL_FLD,            KeyEvent.VK_V,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_VOL_SLD,            KeyEvent.VK_V,        InputEvent.ALT_DOWN_MASK  | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_VOL_BTN,            KeyEvent.VK_A,        InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_00,                 KeyEvent.VK_0,        0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_01,                 KeyEvent.VK_1,        0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_02,                 KeyEvent.VK_2,        0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_03,                 KeyEvent.VK_3,        0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_04,                 KeyEvent.VK_4,        0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_05,                 KeyEvent.VK_5,        0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_06,                 KeyEvent.VK_6,        0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_07,                 KeyEvent.VK_7,        0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_08,                 KeyEvent.VK_8,        0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_09,                 KeyEvent.VK_P,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_09,                 KeyEvent.VK_9,        0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_10,                 KeyEvent.VK_0,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_11,                 KeyEvent.VK_1,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_12,                 KeyEvent.VK_2,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_13,                 KeyEvent.VK_3,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_14,                 KeyEvent.VK_4,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_15,                 KeyEvent.VK_5,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_00,                 KeyEvent.VK_NUMPAD0,  0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_01,                 KeyEvent.VK_NUMPAD1,  0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_02,                 KeyEvent.VK_NUMPAD2,  0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_03,                 KeyEvent.VK_NUMPAD3,  0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_04,                 KeyEvent.VK_NUMPAD4,  0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_05,                 KeyEvent.VK_NUMPAD5,  0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_06,                 KeyEvent.VK_NUMPAD6,  0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_07,                 KeyEvent.VK_NUMPAD7,  0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_08,                 KeyEvent.VK_NUMPAD8,  0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_09,                 KeyEvent.VK_NUMPAD9,  0                          );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_10,                 KeyEvent.VK_NUMPAD0,  InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_11,                 KeyEvent.VK_NUMPAD1,  InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_12,                 KeyEvent.VK_NUMPAD2,  InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_13,                 KeyEvent.VK_NUMPAD3,  InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_14,                 KeyEvent.VK_NUMPAD4,  InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_15,                 KeyEvent.VK_NUMPAD5,  InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_00_M,               KeyEvent.VK_0,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_01_M,               KeyEvent.VK_1,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_02_M,               KeyEvent.VK_2,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_03_M,               KeyEvent.VK_3,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_04_M,               KeyEvent.VK_4,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_05_M,               KeyEvent.VK_5,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_06_M,               KeyEvent.VK_6,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_07_M,               KeyEvent.VK_7,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_08_M,               KeyEvent.VK_8,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_09_M,               KeyEvent.VK_9,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_10_M,               KeyEvent.VK_0,        InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_11_M,               KeyEvent.VK_1,        InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_12_M,               KeyEvent.VK_2,        InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_13_M,               KeyEvent.VK_3,        InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_14_M,               KeyEvent.VK_4,        InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_15_M,               KeyEvent.VK_5,        InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_00_M,               KeyEvent.VK_NUMPAD0,  InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_01_M,               KeyEvent.VK_NUMPAD1,  InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_02_M,               KeyEvent.VK_NUMPAD2,  InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_03_M,               KeyEvent.VK_NUMPAD3,  InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_04_M,               KeyEvent.VK_NUMPAD4,  InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_05_M,               KeyEvent.VK_NUMPAD5,  InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_06_M,               KeyEvent.VK_NUMPAD6,  InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_07_M,               KeyEvent.VK_NUMPAD7,  InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_08_M,               KeyEvent.VK_NUMPAD8,  InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_09_M,               KeyEvent.VK_NUMPAD9,  InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_10_M,               KeyEvent.VK_NUMPAD0,  InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_11_M,               KeyEvent.VK_NUMPAD1,  InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_12_M,               KeyEvent.VK_NUMPAD2,  InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_13_M,               KeyEvent.VK_NUMPAD3,  InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_14_M,               KeyEvent.VK_NUMPAD4,  InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_15_M,               KeyEvent.VK_NUMPAD5,  InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_00_S,               KeyEvent.VK_0,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_01_S,               KeyEvent.VK_1,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_02_S,               KeyEvent.VK_2,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_03_S,               KeyEvent.VK_3,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_04_S,               KeyEvent.VK_4,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_05_S,               KeyEvent.VK_5,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_06_S,               KeyEvent.VK_6,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_07_S,               KeyEvent.VK_7,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_08_S,               KeyEvent.VK_8,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_09_S,               KeyEvent.VK_9,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_10_S,               KeyEvent.VK_0,        InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_11_S,               KeyEvent.VK_1,        InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_12_S,               KeyEvent.VK_2,        InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_13_S,               KeyEvent.VK_3,        InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_14_S,               KeyEvent.VK_4,        InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_15_S,               KeyEvent.VK_5,        InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_00_S,               KeyEvent.VK_NUMPAD0,  InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_01_S,               KeyEvent.VK_NUMPAD1,  InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_02_S,               KeyEvent.VK_NUMPAD2,  InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_03_S,               KeyEvent.VK_NUMPAD3,  InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_04_S,               KeyEvent.VK_NUMPAD4,  InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_05_S,               KeyEvent.VK_NUMPAD5,  InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_06_S,               KeyEvent.VK_NUMPAD6,  InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_07_S,               KeyEvent.VK_NUMPAD7,  InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_08_S,               KeyEvent.VK_NUMPAD8,  InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_09_S,               KeyEvent.VK_NUMPAD9,  InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_10_S,               KeyEvent.VK_NUMPAD0,  InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_11_S,               KeyEvent.VK_NUMPAD1,  InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_12_S,               KeyEvent.VK_NUMPAD2,  InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_13_S,               KeyEvent.VK_NUMPAD3,  InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_14_S,               KeyEvent.VK_NUMPAD4,  InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_PLAYER_CH_15_S,               KeyEvent.VK_NUMPAD5,  InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CLOSE,             KeyEvent.VK_ESCAPE,   0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_PLAY,              KeyEvent.VK_P,        0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_FILTER_INSTR,      KeyEvent.VK_F,        0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_FILTER_NOTE,       KeyEvent.VK_F,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_INSTR,             KeyEvent.VK_I,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_NOTE,              KeyEvent.VK_N,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_VOL_FLD,           KeyEvent.VK_V,        0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_VOL_SLD,           KeyEvent.VK_V,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_VEL_FLD,           KeyEvent.VK_V,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_VEL_SLD,           KeyEvent.VK_V,        InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_DURATION,          KeyEvent.VK_D,        0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_KEEP,              KeyEvent.VK_K,        0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_00,             KeyEvent.VK_NUMPAD0,  0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_01,             KeyEvent.VK_NUMPAD1,  0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_02,             KeyEvent.VK_NUMPAD2,  0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_03,             KeyEvent.VK_NUMPAD3,  0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_04,             KeyEvent.VK_NUMPAD4,  0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_05,             KeyEvent.VK_NUMPAD5,  0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_06,             KeyEvent.VK_NUMPAD6,  0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_07,             KeyEvent.VK_NUMPAD7,  0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_08,             KeyEvent.VK_NUMPAD8,  0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_09,             KeyEvent.VK_NUMPAD9,  0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_10,             KeyEvent.VK_NUMPAD0,  InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_11,             KeyEvent.VK_NUMPAD1,  InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_12,             KeyEvent.VK_NUMPAD2,  InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_13,             KeyEvent.VK_NUMPAD3,  InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_14,             KeyEvent.VK_NUMPAD4,  InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_15,             KeyEvent.VK_NUMPAD5,  InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_00,             KeyEvent.VK_0,        0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_01,             KeyEvent.VK_1,        0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_02,             KeyEvent.VK_2,        0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_03,             KeyEvent.VK_3,        0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_04,             KeyEvent.VK_4,        0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_05,             KeyEvent.VK_5,        0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_06,             KeyEvent.VK_6,        0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_07,             KeyEvent.VK_7,        0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_08,             KeyEvent.VK_8,        0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_09,             KeyEvent.VK_P,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_09,             KeyEvent.VK_9,        0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_10,             KeyEvent.VK_0,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_11,             KeyEvent.VK_1,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_12,             KeyEvent.VK_2,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_13,             KeyEvent.VK_3,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_14,             KeyEvent.VK_4,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_15,             KeyEvent.VK_5,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_00,             KeyEvent.VK_NUMPAD0,  0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_01,             KeyEvent.VK_NUMPAD1,  0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_02,             KeyEvent.VK_NUMPAD2,  0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_03,             KeyEvent.VK_NUMPAD3,  0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_04,             KeyEvent.VK_NUMPAD4,  0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_05,             KeyEvent.VK_NUMPAD5,  0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_06,             KeyEvent.VK_NUMPAD6,  0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_07,             KeyEvent.VK_NUMPAD7,  0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_08,             KeyEvent.VK_NUMPAD8,  0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_09,             KeyEvent.VK_NUMPAD9,  0                          );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_10,             KeyEvent.VK_NUMPAD0,  InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_11,             KeyEvent.VK_NUMPAD1,  InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_12,             KeyEvent.VK_NUMPAD2,  InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_13,             KeyEvent.VK_NUMPAD3,  InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_14,             KeyEvent.VK_NUMPAD4,  InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_SOUNDCHECK_CH_15,             KeyEvent.VK_NUMPAD5,  InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_INFO_CLOSE,                   KeyEvent.VK_ESCAPE,   0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_CONF,                    KeyEvent.VK_C,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_CONF_NOTE,               KeyEvent.VK_N,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_CONF_PERC,               KeyEvent.VK_P,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_CONF_SYNTAX,             KeyEvent.VK_S,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_CONF_INSTR,              KeyEvent.VK_I,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_CONF_DRUMKIT,            KeyEvent.VK_D,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_SF,                      KeyEvent.VK_S,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_SF_GENERAL,              KeyEvent.VK_G,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_SF_INSTR,                KeyEvent.VK_I,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_SF_RES,                  KeyEvent.VK_R,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI,                    KeyEvent.VK_M,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_GENERAL,            KeyEvent.VK_G,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_KARAOKE,            KeyEvent.VK_K,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_BANKS,              KeyEvent.VK_B,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG,                KeyEvent.VK_M,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_ABOUT,                   KeyEvent.VK_A,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_KEYBINDINGS,             KeyEvent.VK_K,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_CONF_NOTE_FILTER,        KeyEvent.VK_F,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_CONF_NOTE_FILTER,        KeyEvent.VK_F,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_INFO_CONF_PERC_FILTER,        KeyEvent.VK_F,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_CONF_PERC_FILTER,        KeyEvent.VK_F,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_INFO_CONF_SYNTAX_FILTER,      KeyEvent.VK_F,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_CONF_SYNTAX_FILTER,      KeyEvent.VK_F,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_INFO_CONF_INSTR_FILTER,       KeyEvent.VK_F,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_CONF_INSTR_FILTER,       KeyEvent.VK_F,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_INFO_CONF_DRUMKIT_FILTER,     KeyEvent.VK_F,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_CONF_DRUMKIT_FILTER,     KeyEvent.VK_F,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_INFO_SF_INSTR_FILTER,         KeyEvent.VK_F,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_SF_INSTR_FILTER,         KeyEvent.VK_F,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_INFO_SF_RES_FILTER,           KeyEvent.VK_F,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_SF_RES_FILTER,           KeyEvent.VK_F,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_FILTER,         KeyEvent.VK_F,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_FILTER,         KeyEvent.VK_F,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_BANKS_TOT_PL,       KeyEvent.VK_PLUS,     0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_BANKS_TOT_PL,       KeyEvent.VK_ADD,      0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_BANKS_TOT_MIN,      KeyEvent.VK_MINUS,    0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_BANKS_TOT_MIN,      KeyEvent.VK_SUBTRACT, 0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_BANKS_TOT_TREE,     KeyEvent.VK_T,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_BANKS_CH_PL,        KeyEvent.VK_PLUS,     InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_BANKS_CH_PL,        KeyEvent.VK_ADD,      InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_BANKS_CH_MIN,       KeyEvent.VK_MINUS,    InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_BANKS_CH_MIN,       KeyEvent.VK_SUBTRACT, InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_BANKS_CH_TREE,      KeyEvent.VK_T,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_PL,             KeyEvent.VK_PLUS,     0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_PL,             KeyEvent.VK_ADD,      0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_MIN,            KeyEvent.VK_MINUS,    0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_MIN,            KeyEvent.VK_SUBTRACT, 0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_TREE,           KeyEvent.VK_T,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_TABLE,          KeyEvent.VK_T,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_INDEP,       KeyEvent.VK_I,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_DEP,         KeyEvent.VK_D,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_SEL_NOD,        KeyEvent.VK_N,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_LIM_TCK,        KeyEvent.VK_L,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_TICK_FROM,      KeyEvent.VK_F,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_TICK_FROM,      KeyEvent.VK_F,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_TICK_TO,        KeyEvent.VK_T,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_LIM_TRK,        KeyEvent.VK_L,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_TRACKS_TXT,     KeyEvent.VK_T,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_SHOW_IN_TR,     KeyEvent.VK_S,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_SHOW_AUTO,      KeyEvent.VK_A,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_00,          KeyEvent.VK_0,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_01,          KeyEvent.VK_1,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_02,          KeyEvent.VK_2,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_03,          KeyEvent.VK_3,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_04,          KeyEvent.VK_4,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_05,          KeyEvent.VK_5,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_06,          KeyEvent.VK_6,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_07,          KeyEvent.VK_7,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_08,          KeyEvent.VK_8,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_09,          KeyEvent.VK_9,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_10,          KeyEvent.VK_0,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_11,          KeyEvent.VK_1,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_12,          KeyEvent.VK_2,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_13,          KeyEvent.VK_3,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_14,          KeyEvent.VK_4,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_15,          KeyEvent.VK_5,        InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_00,          KeyEvent.VK_NUMPAD0,  0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_01,          KeyEvent.VK_NUMPAD1,  0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_02,          KeyEvent.VK_NUMPAD2,  0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_03,          KeyEvent.VK_NUMPAD3,  0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_04,          KeyEvent.VK_NUMPAD4,  0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_05,          KeyEvent.VK_NUMPAD5,  0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_06,          KeyEvent.VK_NUMPAD6,  0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_07,          KeyEvent.VK_NUMPAD7,  0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_08,          KeyEvent.VK_NUMPAD8,  0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_09,          KeyEvent.VK_NUMPAD9,  0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_10,          KeyEvent.VK_NUMPAD0,  InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_11,          KeyEvent.VK_NUMPAD1,  InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_12,          KeyEvent.VK_NUMPAD2,  InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_13,          KeyEvent.VK_NUMPAD3,  InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_14,          KeyEvent.VK_NUMPAD4,  InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_INFO_MIDI_MSG_CH_15,          KeyEvent.VK_NUMPAD5,  InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_INFO_KEY_TREE,                KeyEvent.VK_T,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_KEY_PL,                  KeyEvent.VK_PLUS,     0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_KEY_PL,                  KeyEvent.VK_ADD,      0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_KEY_MIN,                 KeyEvent.VK_MINUS,    0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_KEY_MIN,                 KeyEvent.VK_SUBTRACT, 0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_KEY_FLD,                 KeyEvent.VK_K,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_INFO_KEY_FILTER,              KeyEvent.VK_F,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_KEY_ADD_BTN,             KeyEvent.VK_A,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_INFO_KEY_RESET_ID_CBX,        KeyEvent.VK_R,        0                          );
		addDefaultKeyBinding( Dict.KEY_INFO_KEY_RESET_ID_BTN,        KeyEvent.VK_R,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_INFO_KEY_RESET_GLOB_CBX,      KeyEvent.VK_R,        InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_INFO_KEY_RESET_GLOB_BTN,      KeyEvent.VK_R,        InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK | InputEvent.ALT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_MSG_CLOSE,                    KeyEvent.VK_ESCAPE,   0                          );
		addDefaultKeyBinding( Dict.KEY_MSG_CLOSE,                    KeyEvent.VK_ENTER,    0                          );
		addDefaultKeyBinding( Dict.KEY_MSG_CLOSE,                    KeyEvent.VK_SPACE,    0                          );
		addDefaultKeyBinding( Dict.KEY_STRING_FILTER_CLOSE,          KeyEvent.VK_ESCAPE,   0                          );
		addDefaultKeyBinding( Dict.KEY_STRING_FILTER_CLOSE,          KeyEvent.VK_ENTER,    0                          );
		addDefaultKeyBinding( Dict.KEY_STRING_FILTER_CLEAR,          KeyEvent.VK_C,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECT_CLOSE,            KeyEvent.VK_ESCAPE,   0                          );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECT_CHARSET_CBX,      KeyEvent.VK_C,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECT_FOREIGN_EXE,      KeyEvent.VK_P,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECT_CONFIG_OPEN,      KeyEvent.VK_O,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_IMP_MPL,        KeyEvent.VK_1,        0                          );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_IMP_MPL,        KeyEvent.VK_NUMPAD1,  0                          );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_IMP_MPL,        KeyEvent.VK_1,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_IMP_MPL,        KeyEvent.VK_NUMPAD1,  InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_IMP_MID,        KeyEvent.VK_2,        0                          );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_IMP_MID,        KeyEvent.VK_NUMPAD2,  0                          );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_IMP_MID,        KeyEvent.VK_2,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_IMP_MID,        KeyEvent.VK_NUMPAD2,  InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_IMP_ALDA,       KeyEvent.VK_3,        0                          );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_IMP_ALDA,       KeyEvent.VK_NUMPAD3,  0                          );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_IMP_ALDA,       KeyEvent.VK_3,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_IMP_ALDA,       KeyEvent.VK_NUMPAD3,  InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_IMP_ABC,        KeyEvent.VK_4,        0                          );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_IMP_ABC,        KeyEvent.VK_NUMPAD4,  0                          );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_IMP_ABC,        KeyEvent.VK_4,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_IMP_ABC,        KeyEvent.VK_NUMPAD4,  InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_IMP_LY,         KeyEvent.VK_5,        0                          );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_IMP_LY,         KeyEvent.VK_NUMPAD5,  0                          );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_IMP_LY,         KeyEvent.VK_5,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_IMP_LY,         KeyEvent.VK_NUMPAD5,  InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_IMP_MSCORE,     KeyEvent.VK_6,        0                          );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_IMP_MSCORE,     KeyEvent.VK_NUMPAD6,  0                          );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_IMP_MSCORE,     KeyEvent.VK_6,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_IMP_MSCORE,     KeyEvent.VK_NUMPAD6,  InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_SND_FILE,       KeyEvent.VK_1,        0                          );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_SND_FILE,       KeyEvent.VK_NUMPAD1,  0                          );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_SND_FILE,       KeyEvent.VK_1,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_SND_FILE,       KeyEvent.VK_NUMPAD1,  InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_SND_URL,        KeyEvent.VK_2,        0                          );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_SND_URL,        KeyEvent.VK_NUMPAD2,  0                          );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_SND_URL,        KeyEvent.VK_2,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_SND_URL,        KeyEvent.VK_NUMPAD2,  InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_SND_URL_FLD,    KeyEvent.VK_U,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_SND_DOWNLOAD,   KeyEvent.VK_D,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_EXP_MID,        KeyEvent.VK_1,        0                          );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_EXP_MID,        KeyEvent.VK_NUMPAD1,  0                          );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_EXP_MID,        KeyEvent.VK_1,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_EXP_MID,        KeyEvent.VK_NUMPAD1,  InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_EXP_MPL,        KeyEvent.VK_2,        0                          );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_EXP_MPL,        KeyEvent.VK_NUMPAD2,  0                          );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_EXP_MPL,        KeyEvent.VK_2,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_EXP_MPL,        KeyEvent.VK_NUMPAD2,  InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_EXP_ALDA,       KeyEvent.VK_3,        0                          );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_EXP_ALDA,       KeyEvent.VK_NUMPAD3,  0                          );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_EXP_ALDA,       KeyEvent.VK_3,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_EXP_ALDA,       KeyEvent.VK_NUMPAD3,  InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_EXP_AUDIO,      KeyEvent.VK_4,        0                          );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_EXP_AUDIO,      KeyEvent.VK_NUMPAD4,  0                          );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_EXP_AUDIO,      KeyEvent.VK_4,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_EXP_AUDIO,      KeyEvent.VK_NUMPAD4,  InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_EXP_ABC,        KeyEvent.VK_5,        0                          );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_EXP_ABC,        KeyEvent.VK_NUMPAD5,  0                          );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_EXP_ABC,        KeyEvent.VK_5,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_EXP_ABC,        KeyEvent.VK_NUMPAD5,  InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_EXP_LY,         KeyEvent.VK_6,        0                          );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_EXP_LY,         KeyEvent.VK_NUMPAD6,  0                          );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_EXP_LY,         KeyEvent.VK_6,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_EXP_LY,         KeyEvent.VK_NUMPAD6,  InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_EXP_MSCORE,     KeyEvent.VK_7,        0                          );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_EXP_MSCORE,     KeyEvent.VK_NUMPAD7,  0                          );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_EXP_MSCORE,     KeyEvent.VK_7,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_FILE_SELECTOR_EXP_MSCORE,     KeyEvent.VK_NUMPAD7,  InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_FILE_CONF_CLOSE,              KeyEvent.VK_ESCAPE,   0                          );
		addDefaultKeyBinding( Dict.KEY_FILE_CONF_SAVE,               KeyEvent.VK_S,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_FILE_CONF_RESTORE_SAVED,      KeyEvent.VK_R,        InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_FILE_CONF_RESTORE_DEFAULT,    KeyEvent.VK_D,        InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_TAB_DEBUG,            KeyEvent.VK_1,        0                          );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_TAB_DEBUG,            KeyEvent.VK_NUMPAD1,  0                          );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_TAB_DEBUG,            KeyEvent.VK_1,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_TAB_DEBUG,            KeyEvent.VK_NUMPAD1,  InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_TAB_NOTE_LENGTH,      KeyEvent.VK_2,        0                          );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_TAB_NOTE_LENGTH,      KeyEvent.VK_NUMPAD2,  0                          );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_TAB_NOTE_LENGTH,      KeyEvent.VK_2,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_TAB_NOTE_LENGTH,      KeyEvent.VK_NUMPAD2,  InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_TAB_CHORDS,           KeyEvent.VK_3,        0                          );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_TAB_CHORDS,           KeyEvent.VK_NUMPAD3,  0                          );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_TAB_CHORDS,           KeyEvent.VK_3,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_TAB_CHORDS,           KeyEvent.VK_NUMPAD3,  InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_TAB_NOTE_REST,        KeyEvent.VK_4,        0                          );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_TAB_NOTE_REST,        KeyEvent.VK_NUMPAD4,  0                          );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_TAB_NOTE_REST,        KeyEvent.VK_4,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_TAB_NOTE_REST,        KeyEvent.VK_NUMPAD4,  InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_TAB_KARAOKE,          KeyEvent.VK_5,        0                          );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_TAB_KARAOKE,          KeyEvent.VK_NUMPAD5,  0                          );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_TAB_KARAOKE,          KeyEvent.VK_5,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_TAB_KARAOKE,          KeyEvent.VK_NUMPAD5,  InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_TAB_CTRL_CHANGE,      KeyEvent.VK_6,        0                          );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_TAB_CTRL_CHANGE,      KeyEvent.VK_NUMPAD6,  0                          );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_TAB_CTRL_CHANGE,      KeyEvent.VK_6,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_TAB_CTRL_CHANGE,      KeyEvent.VK_NUMPAD6,  InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_TAB_SLICES,           KeyEvent.VK_7,        0                          );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_TAB_SLICES,           KeyEvent.VK_NUMPAD7,  0                          );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_TAB_SLICES,           KeyEvent.VK_7,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_TAB_SLICES,           KeyEvent.VK_NUMPAD7,  InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_ADD_TICK_COMMENTS,    KeyEvent.VK_T,        0                          );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_ADD_CONFIG,           KeyEvent.VK_C,        0                          );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_ADD_SCORE,            KeyEvent.VK_Q,        0                          );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_ADD_STATISTICS,       KeyEvent.VK_S,        0                          );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_ADD_STRATEGY_STAT,    KeyEvent.VK_R,        0                          );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_NOTE_LENGTH_STRATEGY, KeyEvent.VK_P,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_MIN_TARGET_TICKS_ON,  KeyEvent.VK_L,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_MAX_TARGET_TICKS_ON,  KeyEvent.VK_L,        InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_MIN_DUR_TO_KEEP,      KeyEvent.VK_K,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_MAX_DUR_TO_KEEP,      KeyEvent.VK_K,        InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_TOL_TICK_LEN,         KeyEvent.VK_N,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_TOL_DUR_RATIO,        KeyEvent.VK_R,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_CRD_PREDEFINED,       KeyEvent.VK_P,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_CRD_NOTE_ON,          KeyEvent.VK_N,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_CRD_NOTE_OFF,         KeyEvent.VK_F,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_CRD_VELOCITY,         KeyEvent.VK_V,        InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_USE_DOT_NOTES,        KeyEvent.VK_D,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_USE_DOT_RESTS,        KeyEvent.VK_D,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_USE_TRIP_NOTES,       KeyEvent.VK_T,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_USE_TRIP_RESTS,       KeyEvent.VK_T,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_USE_KARAOKE,          KeyEvent.VK_U,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_ALL_SYL_ORP,          KeyEvent.VK_A,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_KAR_ORPHANED,         KeyEvent.VK_O,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_KAR_ONE_CH,           KeyEvent.VK_A,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_CTRL_CHANGE_MODE,     KeyEvent.VK_M,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_FLD_GLOB_SINGLE,      KeyEvent.VK_O,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_BTN_GLOB_SINGLE,      KeyEvent.VK_O,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_FLD_GLOB_EACH,        KeyEvent.VK_D,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_FLD_GLOB_FROM,        KeyEvent.VK_F,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_FLD_GLOB_TO,          KeyEvent.VK_N,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_BTN_GLOB_RANGE,       KeyEvent.VK_R,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_AREA_GLOB_ALL,        KeyEvent.VK_E,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_DC_CONF_BTN_GLOB_ALL,         KeyEvent.VK_U,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_AU_CONF_ENCODING,             KeyEvent.VK_E,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_AU_CONF_FLD_SAMPLE_SIZE_BITS, KeyEvent.VK_S,        InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK );
		addDefaultKeyBinding( Dict.KEY_AU_CONF_FLD_SAMPLE_RATE,      KeyEvent.VK_S,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_AU_CONF_CHANNELS,             KeyEvent.VK_C,        InputEvent.ALT_DOWN_MASK   );
		addDefaultKeyBinding( Dict.KEY_AU_CONF_IS_BIG_ENDIAN,        KeyEvent.VK_B,        InputEvent.CTRL_DOWN_MASK  );
		addDefaultKeyBinding( Dict.KEY_EXPORT_RESULT_CLOSE,          KeyEvent.VK_ESCAPE,   0                          );
		addDefaultKeyBinding( Dict.KEY_EXPORT_RESULT_SHORT,          KeyEvent.VK_S,        0                          );
		addDefaultKeyBinding( Dict.KEY_EXPORT_RESULT_META,           KeyEvent.VK_M,        0                          );
		addDefaultKeyBinding( Dict.KEY_EXPORT_RESULT_SYSEX,          KeyEvent.VK_X,        0                          );
		addDefaultKeyBinding( Dict.KEY_EXPORT_RESULT_SKIPPED_RESTS,  KeyEvent.VK_R,        0                          );
		addDefaultKeyBinding( Dict.KEY_EXPORT_RESULT_OFF_NOT_FOUND,  KeyEvent.VK_N,        0                          );
		addDefaultKeyBinding( Dict.KEY_EXPORT_RESULT_CRD_GRP_FAILED, KeyEvent.VK_C,        0                          );
		addDefaultKeyBinding( Dict.KEY_EXPORT_RESULT_OTHER,          KeyEvent.VK_O,        0                          );
		addDefaultKeyBinding( Dict.KEY_EXPORT_RESULT_FILTER,         KeyEvent.VK_F,        0                          );
		
		// init key bindings with defaults
		keyBindings = new TreeMap<>();
		for (String id : defaultBindings.keySet()) {
			TreeSet<KeyBinding> bindings = (TreeSet<KeyBinding>) defaultBindings.get(id).clone();
			keyBindings.put(id, bindings);
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
		
		bindings.add(new KeyBinding(keyCode, mods));
	}
	
	/**
	 * Reads the config file and sets the current configuration accordingly.
	 * Does not do anything, if the command line option --ignore-local-config has been used.
	 */
	private static void readConfigFile() {
		
		if (! Cli.useLocalConfig) {
			return;
		}
		
		if (configFile.canRead()) {
			try {
				// open file for reading
				FileReader     fr = new FileReader(configFile);
				BufferedReader br = new BufferedReader(fr);
				String line;
				
				// parse line by line
				while (null != (line = br.readLine())) {
					parseConfig(line);
				}
				
				br.close();
			}
			catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			catch (IOException e) {
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
		
		if (! Cli.useLocalConfig) {
			return;
		}
		
		try {
			// create file if not yet done
			if (! configFile.canWrite()) {
				configFile.createNewFile();
			}
			
			// write config if possible
			if (configFile.canWrite()) {
				FileWriter     fw = new FileWriter(configFile);
				BufferedWriter bw = new BufferedWriter(fw);
				
				// normal config
				for (String key : defaults.keySet()) {
					String value = config.get(key);
					bw.write(key + " " + value);
					bw.newLine();
				}
				
				// import/export based config
				for (String key : dcDefaults.keySet()) {
					String value = config.get(key);
					bw.write(key + " " + value);
					bw.newLine();
				}
				for (String key : auDefaults.keySet()) {
					String value = config.get(key);
					bw.write(key + " " + value);
					bw.newLine();
				}
				
				// key bindings
				for (String key : keyBindings.keySet()) {
					String value = config.get(key);
					if (null == value) {
						continue;
					}
					bw.write(key + " " + value);
					bw.newLine();
				}
				
				bw.close();
			}
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Reads a line of the config file.
	 * Sets the according configuration element like it is configured in that line.
	 * 
	 * @param line The line of the config file to be parsed
	 */
	private static void parseConfig(String line) {
		line = line.replaceFirst("\\s+$", ""); // eliminate trailing whitespaces
		String[] splitted = line.split(" ", 2);
		try {
			String name = splitted[0];
			String value;
			
			// null values are allowed for key bindings...
			if (defaultBindings.containsKey(name)) {
				try {
					value = splitted[1];
				}
				catch (ArrayIndexOutOfBoundsException|NumberFormatException e) {
					value = "";
				}
			}
			else {
				// ... but for other config values they are forbidden
				value = splitted[1];
			}
			
			if (defaultBindings.containsKey(name)) {
				TreeSet<KeyBinding> bindings = new TreeSet<>();
				String[] bindingStr = value.split(",");
				
				if (! "".equals(value)) {
					for (String keycodeAndMods : bindingStr) {
						String[]   elements  = keycodeAndMods.split("-", -1);
						int        keyCode   = Integer.parseInt(elements[0]);
						int        modifiers = Integer.parseInt(elements[1]);
						KeyBinding binding   = new KeyBinding(keyCode, modifiers);
						bindings.add(binding);
					}
				}
				keyBindings.put(name, bindings);
			}
			set(name, value);
		}
		catch (ArrayIndexOutOfBoundsException|NumberFormatException e) {
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
	 * Creates and returns a tree model for the currently configured key bindings.
	 * 
	 * @return tree model for the key bindings.
	 */
	public static MidicaTreeModel getKeyBindingTreeModel() {
		MidicaTreeModel model = new MidicaTreeModel(Dict.get(Dict.KB_CATEGORIES));
		
		Pattern chPattern = Pattern.compile("(\\d{1})");
		
		// category == window
		int catNum = 0;
		for (String category : Dict.getKeyBindingCategories()) {
			String catName = Dict.get(category);
			
			// key binding action == widget
			for (String id : keyBindings.keySet()) {
				if (id.startsWith(category)) {
					
					// we want the action to be displayed in the tree, even if it has no
					// configured key bindings.
					boolean mustFake = false;
					if (0 == keyBindings.get(id).size()) {
						mustFake = true;
						keyBindings.get(id).add(new KeyBinding(-1, -1));
					}
					
					// number of bindings
					Iterator<KeyBinding> iterator = keyBindings.get(id).iterator();
					while (iterator.hasNext()) {
						KeyBinding binding = iterator.next();
						ArrayList<String[]> treePath = new ArrayList<>();
						
						// Ensure correct sorting for channel-based key bindings.
						// E.g. 1, 9, 10 instead of 1, 10, 9
						String sortString = Dict.get(id);
						if (id.contains("_0")) {
							Matcher chMatcher = chPattern.matcher(sortString);
							if (chMatcher.find()) {
								sortString = chMatcher.replaceAll("0$1");
							}
						}
						
						// add path to tree model
						String[] cat         = { catNum + "",  catName,      null             };
						String[] bindingNode = { id,           Dict.get(id), null, sortString };
						treePath.add(cat);
						treePath.add(bindingNode);
						
						try {
							if (mustFake) {
								model.addWithoutIncrementing(treePath);
							}
							else {
								keyBindings.get(id);
								String kbToolTip = "<br>- " + binding.getDescription();
								model.add(treePath, kbToolTip);
							}
						}
						catch (ReflectiveOperationException e) {
						}
					}
					
					// remove faked element again
					if (mustFake) {
						keyBindings.get(id).clear();
					}
				}
			}
			catNum++;
		}
		model.postprocess();
		
		return model;
	}
	
	/**
	 * Adds a new key binding.
	 * This is called when the user manually adds a new key binding.
	 * 
	 * @param id       the ID describing the action for the key binding
	 * @param binding  the key binding to be added to the action
	 */
	public static void addKeyBinding(String id, KeyBinding binding) {
		
		// make it ready to be used now
		TreeSet<KeyBinding> bindings = keyBindings.get(id);
		bindings.add(binding);
		
		// mark it to be written into the config file
		setKeyBindingIdInConfig(id, bindings);
	}
	
	/**
	 * Removes the given key binding.
	 * This is called when the user manually removes a key binding.
	 * 
	 * @param id           the ID describing the action for the key binding
	 * @param bindingDesc  short string describing the key binding uniquely
	 */
	public static void removeKeyBinding(String id, String bindingDesc) {
		
		// find the binding to be removed
		KeyBinding targetBinding = null;
		TreeSet<KeyBinding> bindings = keyBindings.get(id);
		for (KeyBinding b : bindings) {
			if (b.toString().equals(bindingDesc)) {
				targetBinding = b;
				break;
			}
		}
		
		// remove it from the currently used structure
		bindings.remove(targetBinding);
		
		// mark it to be written into the config file
		setKeyBindingIdInConfig(id, bindings);
	}
	
	/**
	 * Resets the key bindings belonging to the specified action to the default.
	 * 
	 * @param id  the ID describing the action for the key binding
	 */
	public static void resetKeyBindingsToDefault(String id) {
		
		// copy from default
		TreeSet<KeyBinding> bindings = (TreeSet<KeyBinding>) defaultBindings.get(id).clone();
		keyBindings.put(id, bindings);
		
		// mark it to be removed from the config file
		config.remove(id);
	}
	
	/**
	 * Resets all key bindings to the default value.
	 */
	public static void resetAllKeyBindingsToDefault() {
		
		// copy from default
		keyBindings.clear();
		for (String id : defaultBindings.keySet()) {
			TreeSet<KeyBinding> bindings = (TreeSet<KeyBinding>) defaultBindings.get(id).clone();
			keyBindings.put(id, bindings);
		}
		
		// mark them to be removed from the config file
		for (String id : keyBindings.keySet()) {
			config.remove(id);
		}
	}
	
	/**
	 * Creates and sets a config value for key bindings to be written into the config file later.
	 * 
	 * @param id        the ID describing the action for the key binding
	 * @param bindings  key bindings of the action to be set
	 */
	private static void setKeyBindingIdInConfig(String id, TreeSet<KeyBinding> bindings) {
		
		// create a value for the config file
		ArrayList<String> values = new ArrayList<>();
		for (KeyBinding b : bindings) {
			values.add(b.toString());
		}
		String configValue = String.join(",", values);
		
		// mark it to be written into the config file
		set(id, configValue);
	}
	
	/**
	 * Fills the configuration comboboxes in the GUI.
	 * This is done by initializing the data models ({@link ConfigComboboxModel}) of the
	 * corresponding comboboxes.
	 */
	private static void initComboBoxes() {
		
		// language combobox
		CBX_LANGUAGE_OPTIONS = new ArrayList<>();
		for (String id : CBX_LANGUAGES) {
			CBX_LANGUAGE_OPTIONS.add(new ComboboxStringOption(id, id));
		}
		ConfigComboboxModel.initModel(CBX_LANGUAGE_OPTIONS, Config.LANGUAGE);
		
		// half tone symbol
		CBX_HALFTONE_OPTIONS = new ArrayList<>();
		for (String id : CBX_HALFTONE_IDENTIFIERS) {
			CBX_HALFTONE_OPTIONS.add(new ComboboxStringOption(id, get(id)));
		}
		ConfigComboboxModel.initModel(CBX_HALFTONE_OPTIONS, Config.HALF_TONE);
		
		// half tone symbol
		CBX_SHARPFLAT_OPTIONS = new ArrayList<>();
		for (String id : CBX_SHARPFLAT_IDENTIFIERS) {
			CBX_SHARPFLAT_OPTIONS.add(new ComboboxStringOption(id, get(id)));
		}
		ConfigComboboxModel.initModel(CBX_SHARPFLAT_OPTIONS, Config.SHARP_FLAT);
		
		// note system
		CBX_NOTE_OPTIONS = new ArrayList<>();
		for (String id : CBX_NOTE_IDENTIFIERS) {
			CBX_NOTE_OPTIONS.add(new ComboboxStringOption(id, get(id)));
		}
		ConfigComboboxModel.initModel(CBX_NOTE_OPTIONS, Config.NOTE);
		
		// octave naming
		CBX_OCTAVE_OPTIONS = new ArrayList<>();
		for (String id : CBX_OCTAVE_IDENTIFIERS) {
			CBX_OCTAVE_OPTIONS.add(new ComboboxStringOption(id, get(id)));
		}
		ConfigComboboxModel.initModel(CBX_OCTAVE_OPTIONS, Config.OCTAVE);
		
		// syntax
		CBX_SYNTAX_OPTIONS = new ArrayList<>();
		for (String id : CBX_SYNTAX_IDENTIFIERS) {
			CBX_SYNTAX_OPTIONS.add(new ComboboxStringOption(id, get(id)));
		}
		ConfigComboboxModel.initModel(CBX_SYNTAX_OPTIONS, Config.SYNTAX);
		
		// percussion shortcuts
		CBX_PERCUSSION_OPTIONS = new ArrayList<>();
		for (String id : CBX_PERCUSSION_IDENTIFIERS) {
			CBX_PERCUSSION_OPTIONS.add(new ComboboxStringOption(id, get(id)));
		}
		ConfigComboboxModel.initModel(CBX_PERCUSSION_OPTIONS, Config.PERCUSSION);
		
		// instrument naming
		CBX_INSTRUMENT_OPTIONS = new ArrayList<>();
		for (String id : CBX_INSTRUMENT_IDENTIFIERS) {
			CBX_INSTRUMENT_OPTIONS.add(new ComboboxStringOption(id, get(id)));
		}
		ConfigComboboxModel.initModel(CBX_INSTRUMENT_OPTIONS, Config.INSTRUMENT);
	}
	
	/**
	 * Returns the currently configured sharp or flat symbol.
	 * 
	 * @param sharp  **true** for the sharp symbol, **false** for the flat symbol
	 * @return the requested symbol
	 */
	public static String getConfiguredSharpOrFlat(boolean sharp) {
		String halfTone = get(Config.HALF_TONE);
		if (CBX_HALFTONE_ID_DIESIS.equals(halfTone))
			return sharp ? "-diesis" : "-bemolle";
		if (CBX_HALFTONE_ID_CIS.equals(halfTone))
			return sharp ? "is" : "es";
		return sharp ? "#" : "b";
	}
	
	/**
	 * Checks if the configured half tone symbol is sharp or flat.
	 * Only needed for some special cases when parsing the key signature.
	 * 
	 * @return **true**, if flat is configured, otherwise false.
	 */
	public static boolean isFlatConfigured() {
		return Config.CBX_SHARPFLAT_FLAT.equals(Config.get(Config.SHARP_FLAT));
	}
}
