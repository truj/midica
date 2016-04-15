/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.config;

import java.awt.Color;
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
import java.util.TreeMap;

import javax.swing.UIManager;

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
	public static final String REMEMBER_SF2         = "remember_sf2";
	
	// charsets
	public static final String CHARSET_MPL        = "charset_mpl";
	public static final String CHARSET_MID        = "charset_mid";
	public static final String CHARSET_EXPORT_MPL = "charset_export_mpl";
	public static final String CHARSET_EXPORT_MID = "charset_export_mid";
	
	
	// table header and column colors
	public static final Color  TABLE_HEADER_COLOR        = new Color( 200, 230, 255 );
	public static final Color  TABLE_CELL_DEFAULT_COLOR  = new Color( 255, 255, 255 );
	public static final Color  TABLE_CELL_CATEGORY_COLOR = new Color( 200, 255, 200 );
	public static final Color  TABLE_CELL_FUTURE_COLOR   = new Color( 255, 255, 150 ); // future notes for the note history
	public static final Color  TABLE_CELL_SELECTED_COLOR = new Color( 200, 200, 255 ); // table row is currently selected
	public static final Color  MSG_TABLE_COLOR           = new Color( 255, 255, 200 );
	public static final Color  MSG_TREE_COLOR            = new Color( 240, 220, 255 );
	public static final Color  MSG_DEFAULT_COLOR         = UIManager.getColor( "Panel.background" );
	
	// text field background colors
	public static final Color  COLOR_NORMAL = new Color( 255, 255, 255 );
	public static final Color  COLOR_OK     = new Color( 200, 255, 200 );
	public static final Color  COLOR_ERROR  = new Color( 255, 150, 150 );
	
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
	public  static final String   CBX_NOTE_ID_GERMAN_LC        = "cbx_note_id_german_lc";
	public  static final String   CBX_NOTE_ID_GERMAN_UC        = "cbx_note_id_german_uc";
	public  static final String   CBX_NOTE_ID_ITALIAN_LC       = "cbx_note_id_italian_lc";
	public  static final String   CBX_NOTE_ID_ITALIAN_UC       = "cbx_note_id_italian_uc";
	private static final String[] CBX_NOTE_IDENTIFIERS = {
		CBX_NOTE_ID_INTERNATIONAL_UC,
		CBX_NOTE_ID_INTERNATIONAL_LC,
		CBX_NOTE_ID_GERMAN_UC,
		CBX_NOTE_ID_GERMAN_LC,
		CBX_NOTE_ID_ITALIAN_LC,
		CBX_NOTE_ID_ITALIAN_UC,
	};
	private static ArrayList<ComboboxStringOption> CBX_NOTE_OPTIONS = null;
	
	// octave naming combobox
	public  static final String   CBX_OCTAVE_INTERNATIONAL = "cbx_octave_international";
	public  static final String   CBX_OCTAVE_GERMAN        = "cbx_octave_german";
	public  static final String   CBX_OCTAVE_PLUS_MINUS    = "cbx_octave_plus_minus";
	private static final String[] CBX_OCTAVE_IDENTIFIERS = {
		CBX_OCTAVE_INTERNATIONAL,
		CBX_OCTAVE_GERMAN,
		CBX_OCTAVE_PLUS_MINUS,
	};
	private static ArrayList<ComboboxStringOption> CBX_OCTAVE_OPTIONS = null;
	
	// half tone combobox
	public  static final String   CBX_HALFTONE_ID_SHARP   = "cbx_halftone_id_sharp";
	public  static final String   CBX_HALFTONE_ID_FLAT    = "cbx_halftone_id_flat";
	public  static final String   CBX_HALFTONE_ID_CIS     = "cbx_halftone_id_cis";
	public  static final String   CBX_HALFTONE_ID_DES     = "cbx_halftone_id_des";
	public  static final String   CBX_HALFTONE_ID_DIESIS  = "cbx_halftone_id_diesis";
	public  static final String   CBX_HALFTONE_ID_BEMOLLE = "cbx_halftone_id_bemolle";
	private static final String[] CBX_HALFTONE_IDENTIFIERS = {
		CBX_HALFTONE_ID_SHARP,
		CBX_HALFTONE_ID_FLAT,
		CBX_HALFTONE_ID_CIS,
		CBX_HALFTONE_ID_DES,
		CBX_HALFTONE_ID_DIESIS,
		CBX_HALFTONE_ID_BEMOLLE,
	};
	private static ArrayList<ComboboxStringOption> CBX_HALFTONE_OPTIONS = null;
	
	// syntax combobox
	public  static final String   CBX_SYNTAX_PROG_1     = "cbx_syntax_prog_1";
	public  static final String   CBX_SYNTAX_PROG_2     = "cbx_syntax_prog_2";
	public  static final String   CBX_SYNTAX_MUSICIAN_1 = "cbx_syntax_musician_1";
	private static final String[] CBX_SYNTAX_IDENTIFIERS = {
		CBX_SYNTAX_MUSICIAN_1,
		CBX_SYNTAX_PROG_1,
		CBX_SYNTAX_PROG_2,
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
	private static TreeMap<String, String> defaults = null;
	private static HashMap<String, String> config   = null;
	
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
		configFile = new File( homeDir + File.separator + fileName );
		
		restoreDefaults( homeDir );
		readConfigFile();
		
		initComboBoxes();
	}
	
	/**
	 * Sets the current configuration to the default configuration.
	 * 
	 * @param homeDir Home directory of the current user
	 */
	private static void restoreDefaults( String homeDir ) {
		
		// init defaults
		defaults = new TreeMap<String, String>();
		defaults.put( LANGUAGE,    CBX_LANG_ENGLISH             );
		defaults.put( HALF_TONE,   CBX_HALFTONE_ID_SHARP        );
		defaults.put( NOTE,        CBX_NOTE_ID_INTERNATIONAL_LC );
		defaults.put( OCTAVE,      CBX_OCTAVE_INTERNATIONAL     );
		defaults.put( SYNTAX,      CBX_SYNTAX_MUSICIAN_1        );
		defaults.put( PERCUSSION,  CBX_PERC_EN_1                );
		defaults.put( INSTRUMENT,  CBX_INSTR_EN_1               );
		
		defaults.put( DIRECTORY_MPL,        homeDir );
		defaults.put( DIRECTORY_MID,        homeDir );
		defaults.put( DIRECTORY_EXPORT_MPL, homeDir );
		defaults.put( DIRECTORY_EXPORT_MID, homeDir );
		defaults.put( DIRECTORY_SF2,        homeDir );
		defaults.put( PATH_SF2,             ""      );
		defaults.put( REMEMBER_SF2,         "false" );
		
		defaults.put( CHARSET_MPL,        DEFAULT_CHARSET_MPL        );
		defaults.put( CHARSET_MID,        DEFAULT_CHARSET_MID        );
		defaults.put( CHARSET_EXPORT_MPL, DEFAULT_CHARSET_EXPORT_MPL );
		defaults.put( CHARSET_EXPORT_MID, DEFAULT_CHARSET_EXPORT_MID );
		
		// init config with defaults
		config = new HashMap<String, String>();
		for ( String key : defaults.keySet() ) {
			set( key, defaults.get(key) );
		}
	}
	
	/**
	 * Reads the config file and sets the current configuration accordingly.
	 */
	private static void readConfigFile() {
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
	 * 
	 * This is called if the main GUI window is closed.
	 */
	public static void writeConfigFile() {
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
	public static void set( String key, String value ) {
		config.put( key, value );
	}
	
	/**
	 * Reads and returns the config value according to the given key.
	 * 
	 * @param key  Config key
	 * @return     Config value according to the given key
	 */
	public static String get( String key ) {
		return config.get( key );
	}
	
	/**
	 * Fills the configuration comboboxes in the GUI.
	 * This is done by initializing the data models ({@link ConfigComboboxModel}) of the
	 * corresponding comboboxes.
	 */
	private static void initComboBoxes() {
		
		// language combobox
		CBX_LANGUAGE_OPTIONS = new ArrayList<ComboboxStringOption>();
		for ( String id : CBX_LANGUAGES ) {
			CBX_LANGUAGE_OPTIONS.add( new ComboboxStringOption(id, id) );
		}
		ConfigComboboxModel.initModel( CBX_LANGUAGE_OPTIONS, Config.LANGUAGE );
		
		// half tone symbol
		CBX_HALFTONE_OPTIONS = new ArrayList<ComboboxStringOption>();
		for ( String id : CBX_HALFTONE_IDENTIFIERS ) {
			CBX_HALFTONE_OPTIONS.add( new ComboboxStringOption(id, get(id)) );
		}
		ConfigComboboxModel.initModel( CBX_HALFTONE_OPTIONS, Config.HALF_TONE );
		
		// note system
		CBX_NOTE_OPTIONS = new ArrayList<ComboboxStringOption>();
		for ( String id : CBX_NOTE_IDENTIFIERS ) {
			CBX_NOTE_OPTIONS.add( new ComboboxStringOption(id, get(id)) );
		}
		ConfigComboboxModel.initModel( CBX_NOTE_OPTIONS, Config.NOTE );
		
		// octave naming
		CBX_OCTAVE_OPTIONS = new ArrayList<ComboboxStringOption>();
		for ( String id : CBX_OCTAVE_IDENTIFIERS ) {
			CBX_OCTAVE_OPTIONS.add( new ComboboxStringOption(id, get(id)) );
		}
		ConfigComboboxModel.initModel( CBX_OCTAVE_OPTIONS, Config.OCTAVE );
		
		// syntax
		CBX_SYNTAX_OPTIONS = new ArrayList<ComboboxStringOption>();
		for ( String id : CBX_SYNTAX_IDENTIFIERS ) {
			CBX_SYNTAX_OPTIONS.add( new ComboboxStringOption(id, get(id)) );
		}
		ConfigComboboxModel.initModel( CBX_SYNTAX_OPTIONS, Config.SYNTAX );
		
		// percussion shortcuts
		CBX_PERCUSSION_OPTIONS = new ArrayList<ComboboxStringOption>();
		for ( String id : CBX_PERCUSSION_IDENTIFIERS ) {
			CBX_PERCUSSION_OPTIONS.add( new ComboboxStringOption(id, get(id)) );
		}
		ConfigComboboxModel.initModel( CBX_PERCUSSION_OPTIONS, Config.PERCUSSION );
		
		// instrument naming
		CBX_INSTRUMENT_OPTIONS = new ArrayList<ComboboxStringOption>();
		for ( String id : CBX_INSTRUMENT_IDENTIFIERS ) {
			CBX_INSTRUMENT_OPTIONS.add( new ComboboxStringOption(id, get(id)) );
		}
		ConfigComboboxModel.initModel( CBX_INSTRUMENT_OPTIONS, Config.INSTRUMENT );
	}
}
