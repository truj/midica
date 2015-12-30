/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import org.midica.Midica;
import org.midica.file.NamedInteger;
import org.midica.ui.info.InstrumentElement;
import org.midica.ui.info.SyntaxElement;
import org.midica.ui.model.ConfigComboboxModel;

/**
 * This class contains and processes all kinds of translations.
 * 
 * This includes:
 * 
 * - Language specific translations
 * - Note system specific symbols (including octave and half tone symbols)
 * - MidicaPL syntax keywords
 * - Percussion shortcut naming
 * - Instrument shortcut naming
 * 
 * It defines different types of translation keys as public member variables.
 * It also contains methods to set and return values of a certain translation type.
 * These methods are called with the corresponding member variables in order to identify the
 * right element to be set or returned.
 * 
 * The member variables' beginning depends on the type of translation they are used for:
 * 
 * - **SYNTAX_** - Keys beginning with this string identify keys for MidicaPL syntax keywords.
 * - **PERCUSSION_** - Those keys identify shortcuts for percussion instruments.
 * - **INSTR_** - Those keys identify instrument shortcuts.
 * - Most member variables without a special beginning identify language-specific translations.
 * 
 * This class contains only static methods.
 * 
 * @author Jan Trukenmüller
 */
public class Dict {
	
	// code to be returned if the note or percussion string is unknown
	public static final int UNKNOWN_CODE = -1;
	
	// the different dictionaries
	private static HashMap<String, String>      dictionary          = null;
	private static HashMap<String, Integer>     noteNameToInt       = null;
	private static HashMap<Integer, String>     noteIntToName       = null;
	private static HashMap<String, Integer>     percussionNameToInt = null;
	private static HashMap<Integer, String>     percussionIntToName = null;
	private static HashMap<String, Integer>     drumkitNameToInt    = null;
	private static HashMap<Integer, String>     drumkitIntToName    = null;
	private static HashMap<String, String>      syntax              = null;
	private static HashMap<String, Integer>     instrNameToInt      = null;
	private static HashMap<Integer, String>     instrIntToName      = null;
	private static ArrayList<SyntaxElement>     syntaxList          = null;
	private static ArrayList<InstrumentElement> instrumentList      = null;
	
	// needed to build up the note dictionaries (noteNameToInt and noteIntToName)
	private static String[] notes      = new String[12];
	private static byte[]   isHalfTone = null;
	
	// syntax
	public static final String SYNTAX_DEFINE        = "DEFINE";
	public static final String SYNTAX_COMMENT       = "COMMENT";
	public static final String SYNTAX_GLOBAL        = "GLOBAL";
	public static final String SYNTAX_P             = "PERCUSSION_CHANNEL";
	public static final String SYNTAX_END           = "END";
	public static final String SYNTAX_MACRO         = "MACRO";
	public static final String SYNTAX_INCLUDE       = "INCLUDE";
	public static final String SYNTAX_INSTRUMENTS   = "INSTRUMENTS";
	public static final String SYNTAX_BPM           = "BPM";
	public static final String SYNTAX_OPT_SEPARATOR = "OPT_SEPARATOR";
	public static final String SYNTAX_OPT_ASSIGNER  = "OPT_ASSIGNER";
	public static final String SYNTAX_PROG_BANK_SEP = "PROGRAM_BANK_SEPARATOR";
	public static final String SYNTAX_BANK_SEP      = "BANK_SEPARATOR";
	public static final String SYNTAX_VOLUME        = "VOLUME";
	public static final String SYNTAX_V             = "VOLUME_SHORT";
	public static final String SYNTAX_STACCATO      = "STACCATO";
	public static final String SYNTAX_S             = "STACCATO_SHORT";
	public static final String SYNTAX_MULTIPLE      = "MULTIPLE";
	public static final String SYNTAX_M             = "MULTIPLE_SHORT";
	public static final String SYNTAX_QUANTITY      = "QUANTITY";
	public static final String SYNTAX_Q             = "QUANTITY_SHORT";
	public static final String SYNTAX_PAUSE         = "PAUSE";
	public static final String SYNTAX_CHORD         = "CHORD";
	public static final String SYNTAX_INCLUDE_FILE  = "INCLUDE_FILE";
	public static final String SYNTAX_32            = "LENGTH_32";
	public static final String SYNTAX_16            = "LENGTH_16";
	public static final String SYNTAX_8             = "LENGTH_8";
	public static final String SYNTAX_4             = "LENGTH_4";
	public static final String SYNTAX_2             = "LENGTH_2";
	public static final String SYNTAX_1             = "LENGTH_1";
	public static final String SYNTAX_M1            = "LENGTH_M1";
	public static final String SYNTAX_M2            = "LENGTH_M2";
	public static final String SYNTAX_M4            = "LENGTH_M4";
	public static final String SYNTAX_M8            = "LENGTH_M8";
	public static final String SYNTAX_M16           = "LENGTH_M16";
	public static final String SYNTAX_M32           = "LENGTH_M32";
	public static final String SYNTAX_DOT           = "DOT";
	public static final String SYNTAX_TRIPLET       = "TRIPLET";
	public static final String SYNTAX_TUPLET        = "TUPLET";
	public static final String SYNTAX_TUPLET_FOR    = "TUPLET_FOR";
	
	// drumkit identifiers
	public static final String DRUMKIT_STANDARD   = "drumkit_0";
	public static final String DRUMKIT_ROOM       = "drumkit_8";
	public static final String DRUMKIT_POWER      = "drumkit_16";
	public static final String DRUMKIT_ELECTRONIC = "drumkit_24";
	public static final String DRUMKIT_TR808      = "drumkit_25";
	public static final String DRUMKIT_JAZZ       = "drumkit_32";
	public static final String DRUMKIT_BRUSH      = "drumkit_40";
	public static final String DRUMKIT_ORCHESTRA  = "drumkit_48";
	public static final String DRUMKIT_SOUND_FX   = "drumkit_56";
	public static final String DRUMKIT_CM64_CM32  = "drumkit_127";
	
	// percussion identifiers
	public static final String PERCUSSION_HIGH_Q          = "percussion_27";
	public static final String PERCUSSION_SLAP            = "percussion_28";
	public static final String PERCUSSION_SCRATCH_PUSH    = "percussion_29";
	public static final String PERCUSSION_SCRATCH_PULL    = "percussion_30";
	public static final String PERCUSSION_STICKS          = "percussion_31";
	public static final String PERCUSSION_SQUARE_CLICK    = "percussion_32";
	public static final String PERCUSSION_METRONOME_CLICK = "percussion_33";
	public static final String PERCUSSION_METRONOME_BELL  = "percussion_34";
	public static final String PERCUSSION_BASS_DRUM_2     = "percussion_35";
	public static final String PERCUSSION_BASS_DRUM_1     = "percussion_36";
	public static final String PERCUSSION_RIM_SHOT        = "percussion_37";
	public static final String PERCUSSION_SNARE_DRUM_1    = "percussion_38";
	public static final String PERCUSSION_HAND_CLAP       = "percussion_39";
	public static final String PERCUSSION_SNARE_DRUM_2    = "percussion_40";
	public static final String PERCUSSION_TOM_1           = "percussion_41";
	public static final String PERCUSSION_HI_HAT_CLOSED   = "percussion_42";
	public static final String PERCUSSION_TOM_2           = "percussion_43";
	public static final String PERCUSSION_HI_HAT_PEDAL    = "percussion_44";
	public static final String PERCUSSION_TOM_3           = "percussion_45";
	public static final String PERCUSSION_HI_HAT_OPEN     = "percussion_46";
	public static final String PERCUSSION_TOM_4           = "percussion_47";
	public static final String PERCUSSION_TOM_5           = "percussion_48";
	public static final String PERCUSSION_CRASH_CYMBAL_1  = "percussion_49";
	public static final String PERCUSSION_TOM_6           = "percussion_50";
	public static final String PERCUSSION_RIDE_CYMBAL_1   = "percussion_51";
	public static final String PERCUSSION_CHINESE_CYMBAL  = "percussion_52";
	public static final String PERCUSSION_RIDE_BELL       = "percussion_53";
	public static final String PERCUSSION_TAMBOURINE      = "percussion_54";
	public static final String PERCUSSION_SPLASH_CYMBAL   = "percussion_55";
	public static final String PERCUSSION_COWBELL         = "percussion_56";
	public static final String PERCUSSION_CRASH_CYMBAL_2  = "percussion_57";
	public static final String PERCUSSION_VIBRA_SLAP      = "percussion_58";
	public static final String PERCUSSION_RIDE_CYMBAL_2   = "percussion_59";
	public static final String PERCUSSION_BONGO_HIGH      = "percussion_60";
	public static final String PERCUSSION_BONGO_LOW       = "percussion_61";
	public static final String PERCUSSION_CONGA_MUTE      = "percussion_62";
	public static final String PERCUSSION_CONGA_OPEN      = "percussion_63";
	public static final String PERCUSSION_CONGA_LOW       = "percussion_64";
	public static final String PERCUSSION_TIMBALES_HIGH   = "percussion_65";
	public static final String PERCUSSION_TIMBALES_LOW    = "percussion_66";
	public static final String PERCUSSION_AGOGO_HIGH      = "percussion_67";
	public static final String PERCUSSION_AGOGO_LOW       = "percussion_68";
	public static final String PERCUSSION_CABASA          = "percussion_69";
	public static final String PERCUSSION_MARACAS         = "percussion_70";
	public static final String PERCUSSION_WHISTLE_SHORT   = "percussion_71";
	public static final String PERCUSSION_WHISTLE_LONG    = "percussion_72";
	public static final String PERCUSSION_GUIRO_SHORT     = "percussion_73";
	public static final String PERCUSSION_GUIRO_LONG      = "percussion_74";
	public static final String PERCUSSION_CLAVE           = "percussion_75";
	public static final String PERCUSSION_WOOD_BLOCK_HIGH = "percussion_76";
	public static final String PERCUSSION_WOOD_BLOCK_LOW  = "percussion_77";
	public static final String PERCUSSION_CUICA_MUTE      = "percussion_78";
	public static final String PERCUSSION_CUICA_OPEN      = "percussion_79";
	public static final String PERCUSSION_TRIANGLE_MUTE   = "percussion_80";
	public static final String PERCUSSION_TRIANGLE_OPEN   = "percussion_81";
	public static final String PERCUSSION_SHAKER          = "percussion_82";
	public static final String PERCUSSION_JINGLE_BELL     = "percussion_83";
	public static final String PERCUSSION_BELLTREE        = "percussion_84";
	public static final String PERCUSSION_CASTANETS       = "percussion_85";
	public static final String PERCUSSION_SURDO_MUTE      = "percussion_86";
	public static final String PERCUSSION_SURDO_OPEN      = "percussion_87";
	
	// TODO: use for the syntax or delete
	// instrument identifiers
	public static final String INSTR_ACOUSTIC_GRAND_PIANO  = "instr_0";   // 0-7: Piano
	public static final String INSTR_BRIGHT_ACOUSTIC_PIANO = "instr_1";
	public static final String INSTR_ELECTRIC_GRAND_PIANO  = "instr_2";
	public static final String INSTR_HONKY_TONK_PIANO      = "instr_3";
	public static final String INSTR_ELECTRIC_PIANO_1      = "instr_4";
	public static final String INSTR_ELECTRIC_PIANO_2      = "instr_5";
	public static final String INSTR_HARPSICHORD           = "instr_6";
	public static final String INSTR_CLAVINET              = "instr_7";
	public static final String INSTR_CELESTA               = "instr_8";   // 8-15: Chromatic Percussion
	public static final String INSTR_GLOCKENSPIEL          = "instr_9";
	public static final String INSTR_MUSIC_BOX             = "instr_10";
	public static final String INSTR_VIBRAPHONE            = "instr_11";
	public static final String INSTR_MARIMBA               = "instr_12";
	public static final String INSTR_XYLOPHONE             = "instr_13";
	public static final String INSTR_TUBULAR_BELL          = "instr_14";
	public static final String INSTR_DULCIMER              = "instr_15";
	public static final String INSTR_DRAWBAR_ORGAN         = "instr_16";  // 16-23: Organ
	public static final String INSTR_PERCUSSIVE_ORGAN      = "instr_17";
	public static final String INSTR_ROCK_ORGAN            = "instr_18";
	public static final String INSTR_CHURCH_ORGAN          = "instr_19";
	public static final String INSTR_REED_ORGAN            = "instr_20";
	public static final String INSTR_ACCORDION             = "instr_21";
	public static final String INSTR_HARMONICA             = "instr_22";
	public static final String INSTR_TANGO_ACCORDION       = "instr_23";
	public static final String INSTR_NYLON_GUITAR          = "instr_24";  // 24-31: Guitar
	public static final String INSTR_STEEL_GUITAR          = "instr_25";
	public static final String INSTR_E_GUITAR_JAZZ         = "instr_26";
	public static final String INSTR_E_GUITAR_CLEAN        = "instr_27";
	public static final String INSTR_E_GUITAR_MUTED        = "instr_28";
	public static final String INSTR_OVERDRIVEN_GUITAR     = "instr_29";
	public static final String INSTR_DISTORTION_GUITAR     = "instr_30";
	public static final String INSTR_GUITAR_HARMONICS      = "instr_31";
	public static final String INSTR_ACOUSTIC_BASS         = "instr_32";  // 32-39: Bass
	public static final String INSTR_E_BASS_FINGER         = "instr_33";
	public static final String INSTR_E_BASS_PICK           = "instr_34";
	public static final String INSTR_FRETLESS_BASS         = "instr_35";
	public static final String INSTR_SLAP_BASS_1           = "instr_36";
	public static final String INSTR_SLAP_BASS_2           = "instr_37";
	public static final String INSTR_SYNTH_BASS_1          = "instr_38";
	public static final String INSTR_SYNTH_BASS_2          = "instr_39";
	public static final String INSTR_VIOLIN                = "instr_40";  // 40-47: Strings
	public static final String INSTR_VIOLA                 = "instr_41";
	public static final String INSTR_CELLO                 = "instr_42";
	public static final String INSTR_CONTRABASS            = "instr_43";
	public static final String INSTR_TREMOLO_STRINGS       = "instr_44";
	public static final String INSTR_PIZZACATO_STRINGS     = "instr_45";
	public static final String INSTR_ORCHESTRAL_HARP       = "instr_46";
	public static final String INSTR_TIMPANI               = "instr_47";
	public static final String INSTR_STRING_ENSEMBLE_1     = "instr_48";  // 48-55: Ensemble
	public static final String INSTR_STRING_ENSEMBLE_2     = "instr_49";
	public static final String INSTR_SYNTH_STRINGS_1       = "instr_50";
	public static final String INSTR_SYNTH_STRINGS_2       = "instr_51";
	public static final String INSTR_CHOIR_AAHS            = "instr_52";
	public static final String INSTR_VOICE_OOHS            = "instr_53";
	public static final String INSTR_SYNTH_CHOIR           = "instr_54";
	public static final String INSTR_ORCHESTRA_HIT         = "instr_55";
	public static final String INSTR_TRUMPET               = "instr_56";  // 56-63: Brass
	public static final String INSTR_TROMBONE              = "instr_57";
	public static final String INSTR_TUBA                  = "instr_58";
	public static final String INSTR_MUTED_TRUMPET         = "instr_59";
	public static final String INSTR_FRENCH_HORN           = "instr_60";
	public static final String INSTR_BRASS_SECTION         = "instr_61";
	public static final String INSTR_SYNTH_BRASS_1         = "instr_62";
	public static final String INSTR_SYNTH_BRASS_2         = "instr_63";
	public static final String INSTR_SOPRANO_SAX           = "instr_64";  // 64-71 :Reed
	public static final String INSTR_ALTO_SAX              = "instr_65";
	public static final String INSTR_TENOR_SAX             = "instr_66";
	public static final String INSTR_BARITONE_SAX          = "instr_67";
	public static final String INSTR_OBOE                  = "instr_68";
	public static final String INSTR_ENGLISH_HORN          = "instr_69";
	public static final String INSTR_BASSOON               = "instr_70";
	public static final String INSTR_CLARINET              = "instr_71";
	public static final String INSTR_PICCOLO               = "instr_72";  // 72-79: Pipe
	public static final String INSTR_FLUTE                 = "instr_73";
	public static final String INSTR_RECORDER              = "instr_74";
	public static final String INSTR_PAN_FLUTE             = "instr_75";
	public static final String INSTR_BLOWN_BOTTLE          = "instr_76";
	public static final String INSTR_SHAKUHACHI            = "instr_77";
	public static final String INSTR_WHISTLE               = "instr_78";
	public static final String INSTR_OCARINA               = "instr_79";
	public static final String INSTR_LEAD_SQUARE           = "instr_80";  // 80-87: Synth Lead
	public static final String INSTR_LEAD_SAWTOOTH         = "instr_81";
	public static final String INSTR_LEAD_CALLIOPE         = "instr_82";
	public static final String INSTR_LEAD_CHIFF            = "instr_83";
	public static final String INSTR_LEAD_CHARANGO         = "instr_84";
	public static final String INSTR_LEAD_VOICE            = "instr_85";
	public static final String INSTR_LEAD_FIFTHS           = "instr_86";
	public static final String INSTR_LEAD_BASS_LEAD        = "instr_87";
	public static final String INSTR_PAD_NEW_AGE           = "instr_88";  // 88-95: Synth Pad
	public static final String INSTR_PAD_WARM              = "instr_89";
	public static final String INSTR_PAD_POLYSYNTH         = "instr_90";
	public static final String INSTR_PAD_CHOIR             = "instr_91";
	public static final String INSTR_PAD_POWED             = "instr_92";
	public static final String INSTR_PAD_METALLIC          = "instr_93";
	public static final String INSTR_PAD_HALO              = "instr_94";
	public static final String INSTR_PAD_SWEEP             = "instr_95";
	public static final String INSTR_FX_RAIN               = "instr_96";  // 69-103: Synth Effects
	public static final String INSTR_FX_SOUNDTRACK         = "instr_97";
	public static final String INSTR_FX_CRYSTAL            = "instr_98";
	public static final String INSTR_FX_ATMOSPHERE         = "instr_99";
	public static final String INSTR_FX_BRIGHTNESS         = "instr_100";
	public static final String INSTR_FX_GOBLINS            = "instr_101";
	public static final String INSTR_FX_ECHOES             = "instr_102";
	public static final String INSTR_FX_SCI_FI             = "instr_103";
	public static final String INSTR_SITAR                 = "instr_104"; // 104-111: Ethnic
	public static final String INSTR_BANJO                 = "instr_105";
	public static final String INSTR_SHAMISEN              = "instr_106";
	public static final String INSTR_KOTO                  = "instr_107";
	public static final String INSTR_KALIMBA               = "instr_108";
	public static final String INSTR_BAG_PIPE              = "instr_109";
	public static final String INSTR_FIDDLE                = "instr_110";
	public static final String INSTR_SHANAI                = "instr_111";
	public static final String INSTR_TINKLE_BELL           = "instr_112"; // 112.119: Percussive
	public static final String INSTR_AGOGO                 = "instr_113";
	public static final String INSTR_STEEL_DRUMS           = "instr_114";
	public static final String INSTR_WOODBLOCK             = "instr_115";
	public static final String INSTR_TAIKO_DRUM            = "instr_116";
	public static final String INSTR_MELODIC_TOM           = "instr_117";
	public static final String INSTR_SYNTH_DRUM            = "instr_118";
	public static final String INSTR_REVERSE_CYMBAL        = "instr_119";
	public static final String INSTR_GUITAR_FRET_NOISE     = "instr_120"; // 120-127: Sound Effects
	public static final String INSTR_BREATH_NOISE          = "instr_121";
	public static final String INSTR_SEASHORE              = "instr_122";
	public static final String INSTR_BIRD_TWEET            = "instr_123";
	public static final String INSTR_TELEPHONE_RING        = "instr_124";
	public static final String INSTR_HELICOPTER            = "instr_125";
	public static final String INSTR_APPLAUSE              = "instr_126";
	public static final String INSTR_GUNSHOT               = "instr_127";
	
	// UiView
	public static final String TITLE_MAIN_WINDOW           = "title_main";
	public static final String UNKNOWN_NOTE_NAME           = "unknown_note_name";
	public static final String UNKNOWN_PERCUSSION_NAME     = "unknown_percussion_name";
	public static final String UNKNOWN_DRUMKIT_NAME        = "unknown_drumkit_name";
	public static final String UNKNOWN_SYNTAX              = "unknown_syntax";
	public static final String UNKNOWN_INSTRUMENT          = "unknown_instrument";
	public static final String CONFIGURATION               = "configuration";
	public static final String LANGUAGE                    = "language";
	public static final String NOTE_SYSTEM                 = "note_system";
	public static final String HALF_TONE_SYMBOL            = "half_tone_symbol";
	public static final String OCTAVE_NAMING               = "octave_naming";
	public static final String SYNTAX                      = "syntax";
	public static final String PERCUSSION                  = "percussion";
	public static final String INSTRUMENT                  = "instrument";
	public static final String SHOW_INFO                   = "show_info";
	public static final String SHOW_INFO_FROM_PLAYER       = "show_info_from_player";
	public static final String IMPORT                      = "import";
	public static final String EXPORT                      = "export";
	public static final String TRANSPOSE_LEVEL             = "transpose_level";
	public static final String PLAYER                      = "player";
	public static final String MIDICAPL_FILE               = "midicapl_file";
	public static final String MIDI_FILE                   = "midi_file";
	public static final String SOUNDFONT                   = "soundfont";
	public static final String PLAYER_BUTTON               = "player_button";
	public static final String UNCHOSEN_FILE               = "unchosen_file";
	public static final String CHOOSE_FILE                 = "choose_file";
	public static final String CHOOSE_FILE_EXPORT          = "choose_file_export";
	public static final String MIDI_EXPORT                 = "midi_export";
	public static final String MIDICAPL_EXPORT             = "midicapl_export";
	public static final String CONF_ERROR_OK               = "conf_error_ok";
	public static final String CONF_ERROR_ERROR            = "conf_error_error";
	public static final String CONF_ERROR_HALFTONE_SYNTAX  = "conf_Error_halftone_syntax";
	public static final String ERROR_NOT_YET_IMPLEMENTED   = "error_not_yet_implemented";
	
	// InfoView
	public static final String TITLE_INFO_VIEW             = "title_info_view";
	public static final String TAB_CONFIG                  = "tab_config";
	public static final String TAB_SOUNDFONT               = "tab_soundfont";
	public static final String TAB_MIDI_STREAM             = "tab_midi_stream";
	public static final String TAB_MIDICA                  = "tab_midica";
	public static final String NOTE_DETAILS                = "note_details";
	public static final String PERCUSSION_DETAILS          = "percussion_details";
	public static final String SYNTAX_DETAILS              = "syntax_details";
	public static final String SOUNDFONT_INFO              = "soundfont_info";
	public static final String SOUNDFONT_INSTRUMENTS       = "soundfont_instruments";
	public static final String SOUNDFONT_DRUMKITS          = "soundfont_drumkits";
	public static final String SOUNDFONT_RESOURCES         = "soundfont_resources";
	public static final String SOUNDFONT_VENDOR            = "soundfont_vendor";
	public static final String SOUNDFONT_CREA_DATE         = "soundfont_crea_date";
	public static final String SOUNDFONT_CREA_TOOLS        = "soundfont_crea_tools";
	public static final String SOUNDFONT_PRODUCT           = "soundfont_product";
	public static final String SOUNDFONT_TARGET_ENGINE     = "soundfont_target_engine";
	public static final String MIDI_STREAM_INFO            = "midi_stream_info";
	public static final String USED_BANKS                  = "used_banks";
	public static final String RESOLUTION                  = "resolution";
	public static final String RESOLUTION_UNIT             = "resolution_unit";
	public static final String TEMPO_BPM                   = "tempo_bpm";
	public static final String TEMPO_MPQ                   = "tempo_mpq";
	public static final String AVERAGE                     = "average";
	public static final String MIN                         = "min";
	public static final String MAX                         = "max";
	public static final String TICK_LENGTH                 = "tick_length";
	public static final String TIME_LENGTH                 = "time_length";
	public static final String SAMPLES_TOTAL               = "samples_total";
	public static final String SAMPLES_AVERAGE             = "samples_average";
	public static final String FRAMES                      = "frames";
	public static final String BYTES                       = "bytes";
	public static final String SEC                         = "sec";
	public static final String SF_INSTR_CAT_CHROMATIC      = "sf_instr_cat_chromatic";
	public static final String SF_INSTR_CAT_DRUMKIT_SINGLE = "sf_instr_cat_drumkit_single";
	public static final String SF_INSTR_CAT_DRUMKIT_MULTI  = "sf_instr_cat_drumkit_multi";
	public static final String SF_INSTR_CAT_UNKNOWN        = "sf_instr_cat_unknown";
	public static final String SF_RESOURCE_CAT_SAMPLE      = "sf_resource_cat_sample";
	public static final String SF_RESOURCE_CAT_LAYER       = "sf_resource_cat_layer";
	public static final String SF_RESOURCE_CAT_UNKNOWN     = "sf_resource_cat_unknown";
	public static final String SINGLE_CHANNEL              = "single_channel";
	public static final String MULTI_CHANNEL               = "multi_channel";
	public static final String UNKNOWN                     = "unknown";
	public static final String DATE                        = "date";
	public static final String AUTHOR                      = "author";
	public static final String SOURCE_URL                  = "source_url";
	public static final String WEBSITE                     = "website";
	public static final String TIMESTAMP_FORMAT            = "timestamp_format";
	public static final String NAME                        = "name";
	public static final String FILE                        = "file";
	public static final String VERSION                     = "version";
	public static final String DESCRIPTION                 = "description";
	public static final String INFO_COL_NOTE_NUM           = "info_col_note_num";
	public static final String INFO_COL_NOTE_NAME          = "info_col_note_name";
	public static final String INFO_COL_SYNTAX_NAME        = "info_col_syntax_name";
	public static final String INFO_COL_SYNTAX_DESC        = "info_col_syntax_desc";
	public static final String INFO_COL_SYNTAX_SHORTCUT    = "info_col_syntax_shortcut";
	public static final String INFO_COL_PERC_NUM           = "info_col_perc_num";
	public static final String INFO_COL_PERC_NAME          = "info_col_perc_name";
	public static final String INFO_COL_INSTR_NUM          = "info_col_instr_num";
	public static final String INFO_COL_INSTR_NAME         = "info_col_instr_name";
	public static final String INFO_COL_SF_INSTR_PROGRAM   = "info_col_sf_instr_program";
	public static final String INFO_COL_SF_INSTR_BANK      = "info_col_sf_instr_bank";
	public static final String INFO_COL_SF_INSTR_NAME      = "info_col_sf_instr_name";
	public static final String INFO_COL_SF_INSTR_CHANNELS  = "info_col_sf_instr_channels";
	public static final String INFO_COL_SF_INSTR_KEYS      = "info_col_sf_instr_keys";
	public static final String INFO_COL_SF_RES_INDEX       = "info_col_sf_res_index";
	public static final String INFO_COL_SF_RES_TYPE        = "info_col_sf_res_type";
	public static final String INFO_COL_SF_RES_NAME        = "info_col_sf_res_name";
	public static final String INFO_COL_SF_RES_FRAMELENGTH = "info_col_sf_res_framelength";
	public static final String INFO_COL_SF_RES_FORMAT      = "info_col_sf_res_format";
	public static final String INFO_COL_SF_RES_CLASS       = "info_col_sf_res_class";
	public static final String TOOLTIP_BANK_MSB            = "tooltip_bank_msb";
	public static final String TOOLTIP_BANK_LSB            = "tooltip_bank_lsb";
	public static final String TOOLTIP_BANK_FULL           = "tooltip_bank_full";
	public static final String SYNTAX_CAT_DEFINITION       = "syntax_cat_definition";
	public static final String SYNTAX_CAT_CALL             = "syntax_cat_call";
	public static final String SYNTAX_CAT_OPTION           = "syntax_cat_option";
	public static final String SYNTAX_CAT_OTHER            = "syntax_cat_other";
	public static final String SYNTAX_CAT_NOTE_LENGTH      = "syntax_cat_note_length";
	public static final String INSTR_CAT_PIANO             = "instr_cat_piano";
	public static final String INSTR_CAT_CHROM_PERC        = "instr_cat_chrom_perc";
	public static final String INSTR_CAT_ORGAN             = "instr_cat_organ";
	public static final String INSTR_CAT_GUITAR            = "instr_cat_guitar";
	public static final String INSTR_CAT_BASS              = "instr_cat_bass";
	public static final String INSTR_CAT_STRINGS           = "instr_cat_strings";
	public static final String INSTR_CAT_ENSEMBLE          = "instr_cat_ensemble";
	public static final String INSTR_CAT_BRASS             = "instr_cat_brass";
	public static final String INSTR_CAT_REED              = "instr_cat_reed";
	public static final String INSTR_CAT_PIPE              = "instr_cat_pipe";
	public static final String INSTR_CAT_SYNTH_LEAD        = "instr_cat_synth_lead";
	public static final String INSTR_CAT_SYNTH_PAD         = "instr_cat_synth_pad";
	public static final String INSTR_CAT_SYNTH_EFFECTS     = "instr_cat_synth_effects";
	public static final String INSTR_CAT_ETHNIC            = "instr_cat_ethnic";
	public static final String INSTR_CAT_PERCUSSIVE        = "instr_cat_percussive";
	public static final String INSTR_CAT_SOUND_EFFECTS     = "instr_cat_sound_effects";
	
	// UiControler + PlayerControler
	public static final String ERROR_IN_LINE               = "parsing_error_in_line";
	
	// MidiParser
	public static final String ERROR_ONLY_PPQ_SUPPORTED    = "error_only_ppq_supported";
	
	// MidicaPLParser
	public static final String ERROR_0_NOT_ALLOWED              = "error_0_not_allowed";
	public static final String ERROR_NEGATIVE_NOT_ALLOWED       = "error_negative_not_allowed: ";
	public static final String ERROR_NOT_AN_INTEGER             = "error_not_an_integer: ";
	public static final String ERROR_INSTRUMENTS_NOT_DEFINED    = "error_instruments_not_defined";
	public static final String ERROR_GLOBALS_IN_INSTR_DEF       = "error_globals_in_instr_def";
	public static final String ERROR_UNKNOWN_CMD                = "error_unknown_cmd";
	public static final String ERROR_CMD_END_NUM_OF_ARGS        = "error_cmd_end_num_of_args";
	public static final String ERROR_CMD_END_WITHOUT_BEGIN      = "error_cmd_end_without_begin";
	public static final String ERROR_MACRO_ALREADY_DEFINED      = "error_macro_already_defined";
	public static final String ERROR_MACRO_NOT_ALLOWED_HERE     = "error_macro_not_allowed_here";
	public static final String ERROR_CHORD_ALREADY_DEFINED      = "error_chord_already_defined";
	public static final String ERROR_CHORD_EQUALS_NOTE          = "error_chord_equals_note";
	public static final String ERROR_CHORD_EQUALS_PERCUSSION    = "error_chord_equals_percussion";
	public static final String ERROR_CHORD_CONTAINS_ALREADY     = "error_chord_contains_already";
	public static final String ERROR_CHORD_DEF_NOT_ALLOWED_HERE = "error_chord_def_not_allowed_here";
	public static final String ERROR_CHORD_NUM_OF_ARGS          = "error_chord_num_of_args";
	public static final String ERROR_DEFINE_NUM_OF_ARGS         = "error_define_num_of_args";
	public static final String ERROR_FILE_NUM_OF_ARGS           = "error_file_num_of_args";
	public static final String ERROR_FILE_EXISTS                = "error_file_exists";
	public static final String ERROR_FILE_NORMAL                = "error_file_normal";
	public static final String ERROR_FILE_READABLE              = "error_file_readable";
	public static final String ERROR_FILE_IO                    = "error_file_io";
	public static final String ERROR_MACRO_NUM_OF_ARGS          = "error_macronum_of_arts";
	public static final String ERROR_MACRO_RECURSION            = "error_macro_recursion";
	public static final String ERROR_MACRO_UNDEFINED            = "error_macro_undefined";
	public static final String ERROR_INCLUDE_NUM_OF_ARGS        = "error_include_num_of_args";
	public static final String ERROR_MODE_INSTR_NUM_OF_ARGS     = "error_mode_instr_num_of_args";
	public static final String ERROR_NOTE_TOO_BIG               = "error_note_too_big";
	public static final String ERROR_NOTE_TOO_SMALL             = "error_note_too_small";
	public static final String ERROR_NOTE_LENGTH_INVALID        = "error_note_length_invalid";
	public static final String ERROR_UNKNOWN_MACRO_CMD          = "error_unknown_macro_cmd";
	public static final String ERROR_INSTR_NUM_OF_ARGS          = "error_num_of_args";
	public static final String ERROR_INSTR_BANK                 = "error_instr_bank";
	public static final String ERROR_GLOBAL_NUM_OF_ARGS         = "error_global_num_of_args";
	public static final String ERROR_UNKNOWN_GLOBAL_CMD         = "error_unknown_global_cmd: ";
	public static final String ERROR_MIDI_PROBLEM               = "error_midi_problem";
	public static final String ERROR_CH_CMD_NUM_OF_ARGS         = "error_ch_num_of_args";
	public static final String ERROR_CANT_PARSE_OPTIONS         = "error_cant_parse_options";
	public static final String ERROR_VOL_NOT_MORE_THAN_127      = "error_vol_not_more_than_127";
	public static final String ERROR_UNKNOWN_OPTION             = "error_unknown_option: ";
	public static final String ERROR_UNKNOWN_NOTE               = "error_unknown_note";
	public static final String ERROR_UNKNOWN_PERCUSSION         = "error_unknown_percussion";
	public static final String ERROR_CHANNEL_UNDEFINED          = "error_channel_undefined";
	public static final String ERROR_INVALID_CHANNEL_NUMBER     = "error_invalid_channel_number";
	
	// SequenceParser
	public static final String ERROR_ANALYZE_POSTPROCESS        = "error_analyze_postprocess";
	
	// Exporter
	public static final String ERROR_EXPORT                     = "error_export";
	public static final String ERROR_FILE_NOT_WRITABLE          = "error_file_not_writable";
	public static final String OVERWRITE_FILE                   = "overwrite_file";
	
	// ExportResultView
	public static final String TITLE_EXPORT_RESULT              = "title_export_result";
	public static final String EXPORT_SUCCESS                   = "export_success";
	public static final String NUMBER_OF_WARNINGS               = "number_of_warnings";
	public static final String WARNING_COL_TICK                 = "warning_col_tick";
	public static final String WARNING_COL_CHANNEL              = "warning_col_channel";
	public static final String WARNING_COL_NOTE                 = "warning_col_note";
	public static final String WARNING_COL_MESSAGE              = "warning_col_message";
	
	// ExportException
	public static final String ERROR_TICK                       = "error_tick";
	public static final String ERROR_CHANNEL                    = "error_channel";
	public static final String ERROR_NOTE                       = "error_note";
	
	// MidicaPLExporter
	public static final String WARNING_SAME_NOTE_IN_SAME_TICK   = "warning_same_note_in_same_tick";
	public static final String WARNING_UNPRESSED_NOTE_RELEASED  = "warning_unpressed_note_released";
	public static final String WARNING_UNRELEASED_NOTE_PRESSED  = "warning_unreleased_note_pressed";
	public static final String WARNING_IGNORED_SHORT_MESSAGE    = "warning_ignored_short_message";
	
	// MidiDevices
	public static final String DEFAULT_CHANNEL_COMMENT          = "default_channel_comment";
	public static final String DEFAULT_INSTRUMENT_NAME          = "default_instrument_name";
	public static final String DEFAULT_PROGRAM_NUMBER           = "default_program_number";
	public static final String PERCUSSION_CHANNEL               = "percussion_channel";
	public static final String NORMAL_CHANNEL                   = "normal_channel";
	public static final String ERROR_SOUNDFONT_NOT_SUPPORTED    = "error_soundfont_not_supported";
	public static final String ERROR_SOUNDFONT_LOADING_FAILED   = "error_soundfont_loading_failed";
	
	// SequenceNotSetException
	public static final String ERROR_SEQUENCE_NOT_SET           = "error_sequence_not_set";
	
	// ErrorMessage
	public static final String CLOSE                            = "close";
	public static final String TITLE_ERROR                      = "title_error";
	public static final String TITLE_CONFIRMATION               = "title_confirmation";
	
	// FileSelector
	public static final String TITLE_FILE_SELECTOR              = "title_file_selector";
	
	// PlayerView
	public static final String TITLE_PLAYER                     = "title_player";
	public static final String REPARSE                          = "reparse";
	public static final String SOUNDCHECK                       = "soundcheck";
	public static final String MEMORIZE                         = "memorize";
	public static final String JUMP                             = "jump";
	public static final String TIME_INFO_UNAVAILABLE            = "time_info_unavailable";
	public static final String SLIDER_VOL                       = "slider_vol";
	public static final String SLIDER_TEMPO                     = "slider_tempo";
	public static final String SLIDER_TRANSPOSE                 = "slider_transpose";
	public static final String ACTIVITY_ACTIVE                  = "activity_active";
	public static final String ACTIVITY_INACTIVE                = "activity_inactive";
	public static final String CTRL_BTN_STOP                    = "stop";
	public static final String CTRL_BTN_FAST_REW                = "ctrl_btn_fast_rew";
	public static final String CTRL_BTN_REW                     = "ctrl_btn_rew";
	public static final String CTRL_BTN_FAST_FWD                = "ctrl_btn_fast_fwd";
	public static final String CTRL_BTN_FWD                     = "ctrl_btn_fwd";
	public static final String CTRL_BTN_PLAY                    = "ctrl_btn_play";
	public static final String CTRL_BTN_PAUSE                   = "ctrl_btn_pause";
	public static final String ABBR_CH_NUM                      = "abbr_ch_num";
	public static final String TIP_CH_NUM                       = "tip_ch_num";
	public static final String ABBR_MUTE                        = "abbr_mute";
	public static final String TIP_MUTE                         = "tip_mute";
	public static final String ABBR_SOLO                        = "abbr_solo";
	public static final String TIP_SOLO                         = "tip_solo";
	public static final String ABBR_ACTIVITY                    = "abbr_activity";
	public static final String TIP_ACTIVITY                     = "tip_activity";
	public static final String ABBR_PROG_NUM                    = "abbr_prog_num";
	public static final String TIP_PROG_NUM                     = "tip_prog_num";
	public static final String ABBR_BANK_NUM                    = "abbr_bank_num";
	public static final String TIP_BANK_NUM                     = "tip_bank_num";
	public static final String CH_HEAD_INSTRUMENT               = "ch_head_instrument";
	public static final String CH_HEAD_COMMENT                  = "ch_head_comment";
	public static final String CH_DETAILS_VOLUME                = "ch_details_volume";
	public static final String LBL_NOTE_HISTORY                 = "lbl_note_history";
	public static final String COLUMN_VOLUME                    = "column_volume";
	public static final String COLUMN_NUMBER                    = "column_number";
	public static final String COLUMN_NAME                      = "column_name";
	public static final String COLUMN_TICK                      = "column_tick";
	
	// SoundcheckView
	public static final String TITLE_SOUNDCHECK                 = "title_soundcheck";
	public static final String SNDCHK_CHANNEL                   = "sndchk_channel";
	public static final String SNDCHK_INSTRUMENT                = "sndchk_instrument";
	public static final String SNDCHK_NOTE                      = "sndchk_note";
	public static final String SNDCHK_VOLUME                    = "sndchk_volume";
	public static final String SNDCHK_DURATION                  = "sndchk_duration";
	public static final String SNDCHK_PLAY                      = "sndchk_play";
	public static final String SNDCHK_COL_PROGRAM               = "sndchk_col_program";
	public static final String SNDCHK_COL_BANK                  = "sndchk_col_bank";
	public static final String SNDCHK_COL_NAME_SF               = "sndchk_col_name_sf";
	public static final String SNDCHK_COL_NAME_SYNTAX           = "sndchk_col_name_syntax";
	
	/**
	 * This class is only used statically so a public constructor is not needed.
	 */
	private Dict() {
	}
	
	/**
	 * Initializes the internal data structures used for the translations.
	 * This includes:
	 * 
	 * - Initializing language specific translations using {@link #initLanguage()}
	 * - Initializing the note system (including half tone and octave symbols)
	 *   using {@link #initNoteSystem()}
	 * - Initializing percussion shortcuts using {@link #initPercussion()}
	 * - Initializing MidicaPL syntax keywords using {@link #initSyntax()}
	 * - Initializing instrument shortcuts using {@link #initInstruments()}
	 * 
	 * This method is called initially on program startup and then always if
	 * the user changes a configuration using the GUI.
	 */
	public static void init() {
		initLanguage();
		initNoteSystem(); // calls also initHalfTones() which calls initOctaves()
		initPercussion();
		initSyntax();
		initInstruments();
	}
	
	/**
	 * Initializes the internal data structure for the chosen language translation.
	 */
	private static void initLanguage() {
		
		// get language
		String language = Config.get( Config.LANGUAGE );
		
		// init the default language
		dictionary = new HashMap<String, String>();
		initLanguageEnglish();
		
		// init another language if chosen
		if ( Config.CBX_LANG_GERMAN.equals(language) )
			initLanguageGerman();
	}
	
	/**
	 * Initializes the internal data structure for english language translations.
	 */
	private static void initLanguageEnglish() {
		
		// Config
		set( Config.CBX_HALFTONE_ID_SHARP,           "#: c#, d#, f#..."                      );
		set( Config.CBX_HALFTONE_ID_FLAT,            "b: db, eb, gb..."                      );
		set( Config.CBX_HALFTONE_ID_CIS,             "-is: cis, dis, fis..."                 );
		set( Config.CBX_HALFTONE_ID_DES,             "-es: des, es, ges..."                  );
		set( Config.CBX_HALFTONE_ID_DIESIS,          "-diesis: do-diesis, re-diesis..."      );
		set( Config.CBX_HALFTONE_ID_BEMOLLE,         "-bemolle: re-bemolle, mi-bemolle..."   );
		
		set( Config.CBX_NOTE_ID_INTERNATIONAL_LC,    "International: c, d, e, f, g, a, b"    );
		set( Config.CBX_NOTE_ID_INTERNATIONAL_UC,    "International: C, D, E, F, G, A, B"    );
		set( Config.CBX_NOTE_ID_GERMAN_LC,           "German (lower): c, d, e, f, g, a, h"   );
		set( Config.CBX_NOTE_ID_GERMAN_UC,           "German (upper): C, D, E, F, G, A, H"   );
		set( Config.CBX_NOTE_ID_ITALIAN_LC,          "Italian (lower): do, re, mi, fa..."    );
		set( Config.CBX_NOTE_ID_ITALIAN_UC,          "Italian (upper): Do, Re, Mi, Fa..."    );
		
		set( Config.CBX_OCTAVE_INTERNATIONAL,        "International: c0, c1, c2..."          );
		set( Config.CBX_OCTAVE_GERMAN,               "German: C', C, c, c', c'', c'''..."    );
		set( Config.CBX_OCTAVE_PLUS_MINUS,           "+/-: c--, c-, c, c+, c++..."           );
		
		set( Config.CBX_SYNTAX_MUSICIAN_1,           "Musician Syntax"                       );
		set( Config.CBX_SYNTAX_PROG_1,               "Programmer Syntax I"                   );
		set( Config.CBX_SYNTAX_PROG_2,               "Programmer Syntax II"                  );
		
		set( Config.CBX_PERC_EN_1,                   "English"                               );
		set( Config.CBX_PERC_DE_1,                   "German"                                );
		
		set( Config.CBX_INSTR_EN_1,                  "English"                               );
		set( Config.CBX_INSTR_DE_1,                  "German"                                );
		
		// UiView
		set( CONFIGURATION,                "Configuration"                 );
		set( LANGUAGE,                     "Language"                      );
		set( NOTE_SYSTEM,                  "Note System"                   );
		set( HALF_TONE_SYMBOL,             "Half Tone Symbol"              );
		set( OCTAVE_NAMING,                "Octave Naming"                 );
		set( SYNTAX,                       "Syntax"                        );
		set( PERCUSSION,                   "Percussion Shortcuts"          );
		set( INSTRUMENT,                   "Instrument Shortcuts"          );
		set( TITLE_MAIN_WINDOW,            "Midica " + Midica.VERSION      );
		set( SHOW_INFO,                    "Info & Configuration Details"  );
		set( SHOW_INFO_FROM_PLAYER,        "Info & Configuration"          );
		set( IMPORT,                       "Import"                        );
		set( PLAYER,                       "Player"                        );
		set( EXPORT,                       "Export"                        );
		set( TRANSPOSE_LEVEL,              "Transpose Level"               );
		set( MIDICAPL_FILE,                "MidicaPL file"                 );
		set( UNKNOWN_NOTE_NAME,            "unknown"                       );
		set( UNKNOWN_PERCUSSION_NAME,      "unknown"                       );
		set( UNKNOWN_DRUMKIT_NAME,         "unknown"                       );
		set( UNKNOWN_SYNTAX,               "?"                             );
		set( UNKNOWN_INSTRUMENT,           "unknown"                       );
		set( MIDI_FILE,                    "Midi file"                     );
		set( SOUNDFONT,                    "Soundfont"                     );
		set( PLAYER_BUTTON,                "Start Player"                  );
		set( UNCHOSEN_FILE,                "no file loaded"                );
		set( CHOOSE_FILE,                  "select file"                   );
		set( CHOOSE_FILE_EXPORT,           "select file"                   );
		set( MIDI_EXPORT,                  "export as midi"                );
		set( MIDICAPL_EXPORT,              "export as score"               );
		set( CONF_ERROR_OK,                "Configuration OK"              );
		set( CONF_ERROR_HALFTONE_SYNTAX,   "Chosen half tone symbol incompatible with chosen syntax" );
		set( ERROR_NOT_YET_IMPLEMENTED,    "This functionality is not yet implemented" );
		
		
		// InfoView
		set( TITLE_INFO_VIEW,                        "Midica Info"                   );
		set( TAB_CONFIG,                             "Configuration"                 );
		set( TAB_SOUNDFONT,                          "Soundfont"                     );
		set( TAB_MIDI_STREAM,                        "MIDI Stream"                   );
		set( TAB_MIDICA,                             "Midica"                        );
		set( NOTE_DETAILS,                           "Note Table"                    );
		set( PERCUSSION_DETAILS,                     "Percussion Shortcuts"          );
		set( SYNTAX_DETAILS,                         "Commands"                      );
		set( SOUNDFONT_INFO,                         "General Info"                  );
		set( SOUNDFONT_INSTRUMENTS,                  "Instruments & Drum Kits"       );
		set( SOUNDFONT_RESOURCES,                    "Resources"                     );
		set( SOUNDFONT_VENDOR,                       "Vendor"                        );
		set( SOUNDFONT_CREA_DATE,                    "Creation Date"                 );
		set( SOUNDFONT_CREA_TOOLS,                   "Creation Tools"                );
		set( SOUNDFONT_PRODUCT,                      "Product"                       );
		set( SOUNDFONT_TARGET_ENGINE,                "Target Sound Engine"           );
		set( MIDI_STREAM_INFO,                       "General Info"                  );
		set( USED_BANKS,                             "Used Banks"                    );
		set( RESOLUTION,                             "Resolution"                    );
		set( RESOLUTION_UNIT,                        "Ticks / Quarter Note"          );
		set( TEMPO_BPM,                              "Tempo in BPM (Beats per Minute or Quarter Notes per Minute)" );
		set( TEMPO_MPQ,                              "Tempo in MPQ (Microseconds per Quarter Note)"                );
		set( AVERAGE,                                "Average"                       );
		set( MIN,                                    "Minimum"                       );
		set( MAX,                                    "Maximum"                       );
		set( TICK_LENGTH,                            "Length (Ticks)"                );
		set( TIME_LENGTH,                            "Length (Time)"                 );
		set( SAMPLES_TOTAL,                          "Samples (Total)"               );
		set( SAMPLES_AVERAGE,                        "Samples (Average)"             );
		set( FRAMES,                                 "Frames"                        );
		set( BYTES,                                  "Bytes"                         );
		set( SEC,                                    "Sec"                           );
		set( SF_INSTR_CAT_CHROMATIC,                 "Chromatic Instruments"         );
		set( SF_INSTR_CAT_DRUMKIT_SINGLE,            "Drum Kits (single channel)"    );
		set( SF_INSTR_CAT_DRUMKIT_MULTI,             "Drum Kits (multi channel)"     );
		set( SF_INSTR_CAT_UNKNOWN,                   "Instruments with unknown Type" );
		set( SF_RESOURCE_CAT_SAMPLE,                 "Samples"                       );
		set( SF_RESOURCE_CAT_LAYER,                  "Layers"                        );
		set( SF_RESOURCE_CAT_UNKNOWN,                "Resources with unknown Type"   );
		set( SOUNDFONT_DRUMKITS,                     "Drum Kits"                     );
		set( SINGLE_CHANNEL,                         "Single Channel"                );
		set( MULTI_CHANNEL,                          "Multi Channel"                 );
		set( UNKNOWN,                                "Unknown"                       );
		set( DATE,                                   "Date (UTC)"                    );
		set( AUTHOR,                                 "Author"                        );
		set( SOURCE_URL,                             "Source"                        );
		set( WEBSITE,                                "Website"                       );
		set( TIMESTAMP_FORMAT,                       "yyyy-MM-dd HH:mm:ss"           );
		set( NAME,                                   "Name"                          );
		set( FILE,                                   "File"                          );
		set( VERSION,                                "Version"                       );
		set( DESCRIPTION,                            "Description"                   );
		set( INFO_COL_NOTE_NUM,                      "Number"                        );
		set( INFO_COL_NOTE_NAME,                     "Name"                          );
		set( INFO_COL_SYNTAX_NAME,                   "Name"                          );
		set( INFO_COL_SYNTAX_DESC,                   "Description"                   );
		set( INFO_COL_SYNTAX_SHORTCUT,               "Keyword"                       );
		set( INFO_COL_PERC_NUM,                      "Number"                        );
		set( INFO_COL_PERC_NAME,                     "Name"                          );
		set( INFO_COL_INSTR_NUM,                     "Number"                        );
		set( INFO_COL_INSTR_NAME,                    "Name"                          );
		set( INFO_COL_SF_INSTR_PROGRAM,              "Number"                        );
		set( INFO_COL_SF_INSTR_BANK,                 "Bank"                          );
		set( INFO_COL_SF_INSTR_NAME,                 "Name"                          );
		set( INFO_COL_SF_INSTR_CHANNELS,             "Channels"                      );
		set( INFO_COL_SF_INSTR_KEYS,                 "Keys"                          );
		set( INFO_COL_SF_RES_INDEX,                  "Index"                         );
		set( INFO_COL_SF_RES_TYPE,                   "Type"                          );
		set( INFO_COL_SF_RES_NAME,                   "Name"                          );
		set( INFO_COL_SF_RES_FRAMELENGTH,            "Frames"                        );
		set( INFO_COL_SF_RES_FORMAT,                 "Format"                        );
		set( INFO_COL_SF_RES_CLASS,                  "Class"                         );
		set( TOOLTIP_BANK_MSB,                       "MSB"                           );
		set( TOOLTIP_BANK_LSB,                       "LSB"                           );
		set( TOOLTIP_BANK_FULL,                      "Bank Number"                   );
		set( SYNTAX_CAT_DEFINITION,                  "Definition Commands"           );
		set( SYNTAX_CAT_CALL,                        "Execution Commands"            );
		set( SYNTAX_CAT_OPTION,                      "Option Syntax"                 );
		set( SYNTAX_CAT_OTHER,                       "Other Commands"                );
		set( SYNTAX_CAT_NOTE_LENGTH,                 "Note Length Definitions"       );
		set( INSTR_CAT_PIANO,                        "Piano"                         );
		set( INSTR_CAT_CHROM_PERC,                   "Chromatic Percussion"          );
		set( INSTR_CAT_ORGAN,                        "Organ"                         );
		set( INSTR_CAT_GUITAR,                       "Guitar"                        );
		set( INSTR_CAT_BASS,                         "Bass"                          );
		set( INSTR_CAT_STRINGS,                      "Strings"                       );
		set( INSTR_CAT_ENSEMBLE,                     "Ensemble"                      );
		set( INSTR_CAT_BRASS,                        "Brass"                         );
		set( INSTR_CAT_REED,                         "Reed"                          );
		set( INSTR_CAT_PIPE,                         "Pipe"                          );
		set( INSTR_CAT_SYNTH_LEAD,                   "Synth Lead"                    );
		set( INSTR_CAT_SYNTH_PAD,                    "Synth Pad"                     );
		set( INSTR_CAT_SYNTH_EFFECTS,                "Synth Effects"                 );
		set( INSTR_CAT_ETHNIC,                       "Ethnic"                        );
		set( INSTR_CAT_PERCUSSIVE,                   "Percussive"                    );
		set( INSTR_CAT_SOUND_EFFECTS,                "Sound Effects"                 );
		
		// syntax for InfoView
		set( SYNTAX_DEFINE,          "syntax element definition"                        );
		set( SYNTAX_COMMENT,         "comment character"                                );
		set( SYNTAX_GLOBAL,          "global command (all channels)"                    );
		set( SYNTAX_P,               "percussion channel"                               );
		set( SYNTAX_END,             "end of a definition block"                        );
		set( SYNTAX_MACRO,           "macro definition"                                 );
		set( SYNTAX_INCLUDE,         "macro execution"                                  );
		set( SYNTAX_INSTRUMENTS,     "definition of instruments"                        );
		set( SYNTAX_BPM,             "tempo definition in BPM"                          );
		set( SYNTAX_OPT_SEPARATOR,   "option separating character"                      );
		set( SYNTAX_OPT_ASSIGNER,    "option assignment character"                      );
		set( SYNTAX_PROG_BANK_SEP,   "Separator between program number and bank select" );
		set( SYNTAX_BANK_SEP,        "MSB / LSB separator for bank select"              );
		set( SYNTAX_VOLUME,          "volume option (long)"                             );
		set( SYNTAX_V,               "volume option (short)"                            );
		set( SYNTAX_STACCATO,        "staccato option (long)"                           );
		set( SYNTAX_S,               "staccato option (short)"                          );
		set( SYNTAX_MULTIPLE,        "multiple notes option (long)"                     );
		set( SYNTAX_M,               "multiple notes option (short)"                    );
		set( SYNTAX_QUANTITY,        "quantity: how often to play the note"             );
		set( SYNTAX_Q,               "quantity option (short)"                          );
		set( SYNTAX_PAUSE,           "pause character"                                  );
		set( SYNTAX_CHORD,           "chord definition"                                 );
		set( SYNTAX_INCLUDE_FILE,    "including another file"                           );
		set( SYNTAX_32,              "32nd"                                             );
		set( SYNTAX_16,              "16th"                                             );
		set( SYNTAX_8,               "8th"                                              );
		set( SYNTAX_4,               "quarter"                                          );
		set( SYNTAX_2,               "half"                                             );
		set( SYNTAX_1,               "full"                                             );
		set( SYNTAX_M1,              "full"                                             );
		set( SYNTAX_M2,              "2 full notes"                                     );
		set( SYNTAX_M4,              "4 full notes"                                     );
		set( SYNTAX_M8,              "8 full notes"                                     );
		set( SYNTAX_M16,             "16 full notes"                                    );
		set( SYNTAX_M32,             "32 full notes"                                    );
		set( SYNTAX_DOT,             "dot (note length multiplied by 1.5)"              );
		set( SYNTAX_TRIPLET,         "triplet (note length devided by 1.5)"             );
		set( SYNTAX_TUPLET,          "tuplet"                                           );
		set( SYNTAX_TUPLET_FOR,      "tuplet definition separator"                      );
		
		// UiControler + PlayerControler
		set( ERROR_IN_LINE,                       "<html>parsing error in file:<br>%s<br>line: %s<br>" );
		
		// Parser
		set( ERROR_0_NOT_ALLOWED,                 "0 not allowed"                 );
		set( ERROR_NEGATIVE_NOT_ALLOWED,          "negative number not allowed: " );
		set( ERROR_NOT_AN_INTEGER,                "not an integer: "              );
		
		// MidiParser
		set( ERROR_ONLY_PPQ_SUPPORTED,            "Only MIDI files with division type PPQ are supported." );
		
		// MidicaPLParser
		set( ERROR_INSTRUMENTS_NOT_DEFINED,       "no instruments have been defined yet"                              );
		set( ERROR_GLOBALS_IN_INSTR_DEF,          "global commands are not allowed inside an instrument definition"   );
		set( ERROR_UNKNOWN_CMD,                   "unknown command: "                                                 );
		set( ERROR_CMD_END_NUM_OF_ARGS,           "wrong number of arguments in mode command 'END'"                   );
		set( ERROR_CMD_END_WITHOUT_BEGIN,         "there is no open block to be closed"                               );
		set( ERROR_CHORD_ALREADY_DEFINED,         "chord name has been already defined: "                             );
		set( ERROR_CHORD_EQUALS_NOTE,             "illegal chord name (equals a note name): "                         );
		set( ERROR_CHORD_EQUALS_PERCUSSION,       "illegal chord name (equals a percussion shortcut): "               );
		set( ERROR_CHORD_CONTAINS_ALREADY,        "Note cannot be defined more than once in the same chord: "         );
		set( ERROR_CHORD_DEF_NOT_ALLOWED_HERE,    "a chord definition is not allowed inside a block<br>maybe you forgot to close the block." );
		set( ERROR_CHORD_NUM_OF_ARGS,             "wrong number of arguments in CHORD command"                        );
		set( ERROR_DEFINE_NUM_OF_ARGS,            "wrong number of arguments in DEFINE command"                       );
		set( ERROR_FILE_NUM_OF_ARGS,              "wrong number of arguments in INCLUDE_FILE command"                 );
		set( ERROR_FILE_EXISTS,                   "file does not exist:<br>"                                          );
		set( ERROR_FILE_NORMAL,                   "not a normal file:<br>"                                            );
		set( ERROR_FILE_READABLE,                 "file not readable:<br>"                                            );
		set( ERROR_FILE_IO,                       "file cannot be parsed:<br>"                                        );
		set( ERROR_MACRO_NUM_OF_ARGS,             "wrong number of arguments in macro command 'MACRO'"                );
		set( ERROR_MACRO_RECURSION,               "Can't include the current macro in itself. Recursion not allowed." );
		set( ERROR_MACRO_UNDEFINED,               "include failed. macro not yet defined."                            );
		set( ERROR_MACRO_ALREADY_DEFINED,         "macro name has been already defined: "                             );
		set( ERROR_MACRO_NOT_ALLOWED_HERE,        "a macro definition is not allowed inside a block<br>maybe you forgot to close the block." );
		set( ERROR_INCLUDE_NUM_OF_ARGS,           "wrong number of arguments in macro command 'INCLUDE'"              );
		set( ERROR_MODE_INSTR_NUM_OF_ARGS,        "wrong number of arguments in mode command 'INSTRUMENTS'"           );
		set( ERROR_NOTE_TOO_BIG,                  "note number too big: "                                             );
		set( ERROR_NOTE_TOO_SMALL,                "note number too small: "                                           );
		set( ERROR_NOTE_LENGTH_INVALID,           "invalid note length expression: "                                  );
		set( ERROR_UNKNOWN_MACRO_CMD,             "unknown macro command: "                                           );
		set( ERROR_INSTR_NUM_OF_ARGS,             "wrong number of arguments in instrument command"                   );
		set( ERROR_INSTR_BANK,                    "Instrument and/or Bank definition erroneous"                       );
		set( ERROR_GLOBAL_NUM_OF_ARGS,            "wrong number of arguments in global command"                       );
		set( ERROR_UNKNOWN_GLOBAL_CMD,            "unknown global command: "                                          );
		set( ERROR_MIDI_PROBLEM,                  "<html>Midi Problem!<br>"                                           );
		set( ERROR_CH_CMD_NUM_OF_ARGS,            "wrong number of arguments in channel command"                      );
		set( ERROR_CANT_PARSE_OPTIONS,            "cannot parse options"                                              );
		set( ERROR_VOL_NOT_MORE_THAN_127,         "volume cannot be set to more than 127"                             );
		set( ERROR_UNKNOWN_OPTION,                "unknown option: "                                                  );
		set( ERROR_UNKNOWN_NOTE,                  "unknown note: "                                                    );
		set( ERROR_UNKNOWN_PERCUSSION,            "unknown percussion shortcut: "                                     );
		set( ERROR_CHANNEL_UNDEFINED,             "channel %s has not been defined"                                   );
		set( ERROR_INVALID_CHANNEL_NUMBER,        "Invalid channel number (must be between 0 and 15): "               );
		
		// Exporter
		set( ERROR_EXPORT,                        "Export Error in the file "                                         );
		set( ERROR_FILE_NOT_WRITABLE,             "File not writable"                                                 );
		set( OVERWRITE_FILE,                      "Overwrite the file?"                                               );
		
		// ExportResultView
		set( TITLE_EXPORT_RESULT,                 "Export Result"                                                     );
		set( EXPORT_SUCCESS,                      "The file has been exported!"                                       );
		set( NUMBER_OF_WARNINGS,                  "Number of warnings:"                                               );
		set( WARNING_COL_TICK,                    "Tick"                                                              );
		set( WARNING_COL_CHANNEL,                 "Channel"                                                           );
		set( WARNING_COL_NOTE,                    "Note"                                                              );
		set( WARNING_COL_MESSAGE,                 "Warning"                                                           );
		
		// ExportException
		set( ERROR_TICK,                          "Tick"                                                              );
		set( ERROR_CHANNEL,                       "Channel"                                                           );
		set( ERROR_NOTE,                          "Note"                                                              );
		
		// MidicaPLExporter
		set( WARNING_SAME_NOTE_IN_SAME_TICK,      "The same note has been addressed more than once"
		                                        + " at the same time and channel (current/old volume: %s/%s)"         );
		set( WARNING_UNPRESSED_NOTE_RELEASED,     "Note released without having been pressed since tick %s"           );
		set( WARNING_UNRELEASED_NOTE_PRESSED,     "Note pressed without having been released since tick %s"           );
		set( WARNING_IGNORED_SHORT_MESSAGE,       "Ignored ShortMessage - command: %s, data1: %s, data2: %s"          );
		
		// MidiDevices
		set( PERCUSSION_CHANNEL,                  "Percussion Channel"         );
		set( NORMAL_CHANNEL,                      "Normal Channel"             );
		set( DEFAULT_CHANNEL_COMMENT,             "undefined"                  );
		set( DEFAULT_INSTRUMENT_NAME,             "Fake Instrument"            );
		set( DEFAULT_PROGRAM_NUMBER,              "-"                          );
		set( ERROR_SOUNDFONT_NOT_SUPPORTED,       "Soundfont not supported"    );
		set( ERROR_SOUNDFONT_LOADING_FAILED,      "Soundfont failed to load"   );
		
		// SequenceNotSetException
		set( ERROR_SEQUENCE_NOT_SET,              "<html>A Sequence is not yet set<br>"
		                                        + "You have to open either a MIDI file (.mid)<br>"
		                                        + "or a score file (.midica) from the import area<br>"
		                                        + "before you can use the player or export a file." );
		
		// ErrorMessage
		set( CLOSE,                               "Close"                      );
		set( TITLE_ERROR,                         "Midica Error"               );
		set( TITLE_CONFIRMATION,                  "Midica Confirmation"        );
		
		// FileSelector
		set( TITLE_FILE_SELECTOR,                 "Midica Choose File"         );
		
		// PlayerView
		set( TITLE_PLAYER,                        "Midica Player"              );
		set( REPARSE,                             "Reparse"                    );
		set( SOUNDCHECK,                          "Soundcheck"                 );
		set( MEMORIZE,                            "Memorize"                   );
		set( JUMP,                                "Go"                         );
		set( TIME_INFO_UNAVAILABLE,               "-"                          );
		set( SLIDER_VOL,                          "Vol"                        );
		set( SLIDER_TEMPO,                        "Tempo"                      );
		set( SLIDER_TRANSPOSE,                    "Transpose"                  );
		set( CTRL_BTN_STOP,                       "Stop"                       );
		set( CTRL_BTN_FAST_REW,                   "<<"                         );
		set( CTRL_BTN_REW,                        "<"                          );
		set( CTRL_BTN_FAST_FWD,                   ">>"                         );
		set( CTRL_BTN_FWD,                        ">"                          );
		set( CTRL_BTN_PLAY,                       "Play"                       );
		set( CTRL_BTN_PAUSE,                      "||"                         );
		set( ABBR_CH_NUM,                         "C"                          );
		set( TIP_CH_NUM,                          "channel number"             );
		set( ABBR_MUTE,                           "M"                          );
		set( TIP_MUTE,                            "mute"                       );
		set( ABBR_SOLO,                           "S"                          );
		set( TIP_SOLO,                            "solo"                       );
		set( ABBR_ACTIVITY,                       "A"                          );
		set( TIP_ACTIVITY,                        "activity"                   );
		set( ABBR_PROG_NUM,                       "P"                          );
		set( TIP_PROG_NUM,                        "program number"             );
		set( ABBR_BANK_NUM,                       "B"                          );
		set( TIP_BANK_NUM,                        "bank number"                );
		set( CH_HEAD_INSTRUMENT,                  "Instrument"                 );
		set( CH_HEAD_COMMENT,                     "Comment"                    );
		set( ACTIVITY_ACTIVE,                     "active"                     );
		set( ACTIVITY_INACTIVE,                   "inactive"                   );
		set( CH_DETAILS_VOLUME,                   "Volume"                     );
		set( LBL_NOTE_HISTORY,                    "<html>Note<br>History"      );
		set( COLUMN_VOLUME,                       "Volume"                     );
		set( COLUMN_NUMBER,                       "Number"                     );
		set( COLUMN_NAME,                         "Name"                       );
		set( COLUMN_TICK,                         "Tick"                       );
		
		// SoundcheckView
		set( TITLE_SOUNDCHECK,               "Midica Soundcheck"          );
		set( SNDCHK_CHANNEL,                 "Channel"                    );
		set( SNDCHK_INSTRUMENT,              "Instrument"                 );
		set( SNDCHK_NOTE,                    "Note"                       );
		set( SNDCHK_VOLUME,                  "Volume"                     );
		set( SNDCHK_DURATION,                "Duration (ms)"              );
		set( SNDCHK_PLAY,                    "Play"                       );
		set( SNDCHK_COL_PROGRAM,             "Prog"                       );
		set( SNDCHK_COL_BANK,                "Bank"                       );
		set( SNDCHK_COL_NAME_SF,             "Soundfont Name"             );
		set( SNDCHK_COL_NAME_SYNTAX,         "Syntax"                     );
	}
	
	/**
	 * Initializes the internal data structure for german language translations.
	 */
	private static void initLanguageGerman() {
		
		// Config
		set( Config.CBX_HALFTONE_ID_SHARP,           "#: c#, d#, f#..."                      );
		set( Config.CBX_HALFTONE_ID_FLAT,            "b: db, eb, gb..."                      );
		set( Config.CBX_HALFTONE_ID_CIS,             "-is: cis, dis, fis..."                 );
		set( Config.CBX_HALFTONE_ID_DES,             "-es: des, es, ges..."                  );
		set( Config.CBX_HALFTONE_ID_DIESIS,          "-diesis: do-diesis, re-diesis..."      );
		set( Config.CBX_HALFTONE_ID_BEMOLLE,         "-bemolle: re-bemolle, mi-bemolle..."   );
		
		set( Config.CBX_NOTE_ID_INTERNATIONAL_LC,    "International: c, d, e, f, g, a, b"    );
		set( Config.CBX_NOTE_ID_INTERNATIONAL_UC,    "International: C, D, E, F, G, A, B"    );
		set( Config.CBX_NOTE_ID_GERMAN_LC,           "Deutsch (klein): c, d, e, f, g, a, h"  );
		set( Config.CBX_NOTE_ID_GERMAN_UC,           "Deutsch (groß): C, D, E, F, G, A, H"   );
		set( Config.CBX_NOTE_ID_ITALIAN_LC,          "Italienisch (klein): do, re, mi..."    );
		set( Config.CBX_NOTE_ID_ITALIAN_UC,          "Italienisch (groß): Do, Re, Mi..."     );
		
		set( Config.CBX_OCTAVE_INTERNATIONAL,        "International: c0, c1, c2..."          );
		set( Config.CBX_OCTAVE_GERMAN,               "Deutsch: C', C, c, c', c'', c'''..."   );
		set( Config.CBX_OCTAVE_PLUS_MINUS,           "+/-: c--, c-, c, c+, c++..."           );
		
		set( Config.CBX_SYNTAX_MUSICIAN_1,           "Musiker-Syntax"                        );
		set( Config.CBX_SYNTAX_PROG_1,               "Programmierer-Syntax I"                );
		set( Config.CBX_SYNTAX_PROG_2,               "Programmierer-Syntax II"               );
		
		set( Config.CBX_PERC_EN_1,                   "Englisch"                              );
		set( Config.CBX_PERC_DE_1,                   "Deutsch"                               );
		
		set( Config.CBX_INSTR_EN_1,                  "Englisch"                              );
		set( Config.CBX_INSTR_DE_1,                  "Deutsch"                               );
		
		// UiView
		set( CONFIGURATION,                          "Konfiguration"                         );
		set( LANGUAGE,                               "Sprache"                               );
		set( NOTE_SYSTEM,                            "Notensystem"                           );
		set( HALF_TONE_SYMBOL,                       "Halbton-Symbol"                        );
		set( OCTAVE_NAMING,                          "Oktavenbezeichner"                     );
		set( SYNTAX,                                 "Syntax"                                );
		set( PERCUSSION,                             "Percussion-Bezeichner"                 );
		set( INSTRUMENT,                             "Instrumenten-Bezeichner"               );
		set( TITLE_MAIN_WINDOW,                      "Midica " + Midica.VERSION              );
		set( TIMESTAMP_FORMAT,                       "dd.MM.yyyy HH:mm:ss"                   );
		set( SHOW_INFO,                              "Info & Konfigurationsdetails"          );
		set( SHOW_INFO_FROM_PLAYER,                  "Info & Konfiguration"                  );
		set( IMPORT,                                 "Laden"                                 );
		set( PLAYER,                                 "Abspielen"                             );
		set( EXPORT,                                 "Speichern"                             );
		set( TRANSPOSE_LEVEL,                        "Transposition:"                        );
		set( MIDICAPL_FILE,                          "MidicaPL-Datei"                        );
		set( UNKNOWN_NOTE_NAME,                      "unbekannt"                             );
		set( UNKNOWN_PERCUSSION_NAME,                "unbekannt"                             );
		set( UNKNOWN_DRUMKIT_NAME,                   "unbekannt"                             );
		set( UNKNOWN_SYNTAX,                         "?"                                     );
		set( UNKNOWN_INSTRUMENT,                     "unbekannt"                             );
		set( MIDI_FILE,                              "Midi-Datei"                            );
		set( SOUNDFONT,                              "Soundfont-Datei"                       );
		set( PLAYER_BUTTON,                          "Abspielen"                             );
		set( UNCHOSEN_FILE,                          "keine Datei geladen"                   );
		set( CHOOSE_FILE,                            "Öffnen"                                );
		set( CHOOSE_FILE_EXPORT,                     "Speichern"                             );
		set( MIDI_EXPORT,                            "Als MIDI exportieren"                  );
		set( MIDICAPL_EXPORT,                        "Als .midica exportieren"               );
		set( ERROR_NOT_YET_IMPLEMENTED,              "Diese Funktionalität ist noch nicht fertig." );
	}
	
	/**
	 * Adds the currently configured half tone and octave symbols
	 * to the currently configured note symbols.
	 * 
	 * The octave symbols are added using the method {@link #initOctaves()}.
	 */
	public static void initHalfTones() {
		
		// refresh combobox language
		ConfigComboboxModel.refill( Config.HALF_TONE );
		
		// get configuration
		String configuredHalfTone = Config.get( Config.HALF_TONE );
		
		// init half tone
		String  suffix = null;
		boolean sharp  = true;
		if ( Config.CBX_HALFTONE_ID_SHARP.equals(configuredHalfTone) ) {
			suffix = "#";
			sharp  = true;
		}
		else if ( Config.CBX_HALFTONE_ID_FLAT.equals(configuredHalfTone) ) {
			suffix = "b";
			sharp  = false;
		}
		else if ( Config.CBX_HALFTONE_ID_CIS.equals(configuredHalfTone) ) {
			suffix = "is";
			sharp  = true;
		}
		else if ( Config.CBX_HALFTONE_ID_DES.equals(configuredHalfTone) ) {
			suffix = "es";
			sharp  = false;
		}
		else if ( Config.CBX_HALFTONE_ID_DIESIS.equals(configuredHalfTone) ) {
			suffix = "-diesis";
			sharp  = true;
		}
		else if ( Config.CBX_HALFTONE_ID_BEMOLLE.equals(configuredHalfTone) ) {
			suffix = "-bemolle";
			sharp  = false;
		}
		else {
			// default
			suffix = "#";
			sharp  = true;
		}
		
		// find out what to add to the current index
		// to get the index of the base note
		byte baseIncrementation;
		if (sharp)
			baseIncrementation = -1;
		else
			baseIncrementation =  1;
		
		// initialize half tones
		for ( byte i : isHalfTone ) {
			// get base note
			int baseIndex = i + baseIncrementation;
			baseIndex %= 12;
			String baseNote = notes[ baseIndex ];
			
			// construct current half tone
			notes[ i ] = baseNote + suffix;
			
			// handle exceptions
			if ( "Hb".equals(notes[i]) ) {
				notes[ i ] = "B";
			}
			else if ( "hb".equals(notes[i]) ) {
				notes[ i ] = "b";
			}
			else if ( "Ees".equals(notes[i]) ) {
				notes[ i ] = "Es";
			}
			else if ( "ees".equals(notes[i]) ) {
				notes[ i ] = "es";
			}
			else if ( "Aes".equals(notes[i]) ) {
				notes[ i ] = "As";
			}
			else if ( "aes".equals(notes[i]) ) {
				notes[ i ] = "as";
			}
		}
		
		// the half tones have changed so the octave naming has to be refreshed as well
		initOctaves();
	}
	
	/**
	 * Initializes the internal data structures for translations between note values
	 * and the configured note symbols.
	 * This includes:
	 * 
	 * - initializing the raw note name translations
	 * - adding the currently configured half tone and octave symbols to the note names
	 *   using the method {@link #initHalfTones()}
	 */
	public static void initNoteSystem() {
		
		// refresh combobox language
		ConfigComboboxModel.refill( Config.NOTE );
		
		// get note system and half tone
		String configuredNoteSystem = Config.get( Config.NOTE );
		
		// define what is a half tone (in german note systems this will be overridden)
		isHalfTone = new byte[ 5 ];
		isHalfTone[ 0 ] =  1; // C#, Db
		isHalfTone[ 1 ] =  3; // D#, Eb
		isHalfTone[ 2 ] =  6; // F#, Gb
		isHalfTone[ 3 ] =  8; // G#, Ab
		isHalfTone[ 4 ] = 10; // A#, Bb
		
		// initialize the configuration specific note system
		if ( Config.CBX_NOTE_ID_INTERNATIONAL_UC.equals(configuredNoteSystem) ) {
			initNotesInternational( true );
		}
		else if ( Config.CBX_NOTE_ID_INTERNATIONAL_LC.equals(configuredNoteSystem) ) {
			initNotesInternational( false );
		}
		else if ( Config.CBX_NOTE_ID_GERMAN_UC.equals(configuredNoteSystem) ) {
			initNotesGerman( true );
		}
		else if ( Config.CBX_NOTE_ID_GERMAN_LC.equals(configuredNoteSystem) ) {
			initNotesGerman( false );
		}
		else if ( Config.CBX_NOTE_ID_ITALIAN_UC.equals(configuredNoteSystem) ) {
			initNotesItalian( true );
		}
		else if ( Config.CBX_NOTE_ID_ITALIAN_LC.equals(configuredNoteSystem) ) {
			initNotesItalian( false );
		}
		else {
			initNotesInternational( true );
		}
		
		// the note system has changed so the half tones have to be refreshed as well
		initHalfTones();
	}
	
	/**
	 * Creates the translation structures between note values and their configured
	 * symbols.
	 * 
	 * Reads the currently configured note names and half tone symbols which have
	 * been created before. Creates full translation structures including the
	 * configured octave symbols for each possible note value.
	 */
	public static void initOctaves() {
		
		// refresh combobox language
		ConfigComboboxModel.refill( Config.OCTAVE );
		
		// get note system and half tone
		String configuredOctave = Config.get( Config.OCTAVE );
		
		noteNameToInt = new HashMap<String, Integer>();
		noteIntToName = new HashMap<Integer, String>();
		
		// initialize the octave according to the configuration
		if ( Config.CBX_OCTAVE_INTERNATIONAL.equals(configuredOctave) ) {
			initOctavesInternational();
		}
		else if ( Config.CBX_OCTAVE_GERMAN.equals(configuredOctave) ) {
			initOctavesGerman();
		}
		else if ( Config.CBX_OCTAVE_PLUS_MINUS.equals(configuredOctave) ) {
			initOctavesPlusMinus();
		}
		else {
			initOctavesInternational();
		}
		
		// init integers to names
		for ( String key : noteNameToInt.keySet() ) {
			noteIntToName.put( noteNameToInt.get(key), key );
		}
	}
	
	/**
	 * Creates translations between configured keywords or symbols and their
	 * meaning for MidicaPL.
	 * 
	 * Strictly speaking this translation is only the default because
	 * these symbols can still be redefined inside the MidicaPL source files themselves.
	 */
	public static void initSyntax() {
		
		// refresh combobox language
		ConfigComboboxModel.refill( Config.SYNTAX );
		
		// get syntax
		String configuredSyntax = Config.get( Config.SYNTAX );
		
		// init configured syntax
		syntax = new HashMap<String, String>();
		
		if ( Config.CBX_SYNTAX_MUSICIAN_1.equals(configuredSyntax) ) {
			initSyntaxMusician1();
		}
		else if ( Config.CBX_SYNTAX_PROG_2.equals(configuredSyntax) ) {
			initSyntaxProgrammer2();
		}
		else {
			initSyntaxProgrammer1();
		}
		
		// init syntax for the config feedback
		syntaxList = new ArrayList<SyntaxElement>();
		
		addSyntaxCategory( get(SYNTAX_CAT_DEFINITION) );
		addSyntaxForInfoView( SYNTAX_DEFINE       );
		addSyntaxForInfoView( SYNTAX_INSTRUMENTS  );
		addSyntaxForInfoView( SYNTAX_CHORD        );
		addSyntaxForInfoView( SYNTAX_MACRO        );
		addSyntaxForInfoView( SYNTAX_END          );
		
		addSyntaxCategory( get(SYNTAX_CAT_CALL) );
		addSyntaxForInfoView( SYNTAX_INCLUDE      );
		addSyntaxForInfoView( SYNTAX_INCLUDE_FILE );
		
		addSyntaxCategory( get(SYNTAX_CAT_OTHER) );
		addSyntaxForInfoView( SYNTAX_COMMENT      );
		addSyntaxForInfoView( SYNTAX_GLOBAL       );
		addSyntaxForInfoView( SYNTAX_BPM          );
		addSyntaxForInfoView( SYNTAX_PAUSE        );
		addSyntaxForInfoView( SYNTAX_P            );

		addSyntaxCategory( get(SYNTAX_CAT_OPTION) );
		addSyntaxForInfoView( SYNTAX_OPT_SEPARATOR );
		addSyntaxForInfoView( SYNTAX_OPT_ASSIGNER  );
		addSyntaxForInfoView( SYNTAX_PROG_BANK_SEP );
		addSyntaxForInfoView( SYNTAX_BANK_SEP      );
		addSyntaxForInfoView( SYNTAX_VOLUME        );
		addSyntaxForInfoView( SYNTAX_V             );
		addSyntaxForInfoView( SYNTAX_STACCATO      );
		addSyntaxForInfoView( SYNTAX_S             );
		addSyntaxForInfoView( SYNTAX_MULTIPLE      );
		addSyntaxForInfoView( SYNTAX_M             );
		addSyntaxForInfoView( SYNTAX_QUANTITY      );
		addSyntaxForInfoView( SYNTAX_Q             );
		
		addSyntaxCategory( get(SYNTAX_CAT_NOTE_LENGTH) );
		addSyntaxForInfoView( SYNTAX_32            );
		addSyntaxForInfoView( SYNTAX_16            );
		addSyntaxForInfoView( SYNTAX_8             );
		addSyntaxForInfoView( SYNTAX_4             );
		addSyntaxForInfoView( SYNTAX_2             );
		addSyntaxForInfoView( SYNTAX_1             );
		addSyntaxForInfoView( SYNTAX_M1            );
		addSyntaxForInfoView( SYNTAX_M2            );
		addSyntaxForInfoView( SYNTAX_M4            );
		addSyntaxForInfoView( SYNTAX_M8            );
		addSyntaxForInfoView( SYNTAX_M16           );
		addSyntaxForInfoView( SYNTAX_M32           );
		addSyntaxForInfoView( SYNTAX_DOT           );
		addSyntaxForInfoView( SYNTAX_TRIPLET       );
		addSyntaxForInfoView( SYNTAX_TUPLET        );
		addSyntaxForInfoView( SYNTAX_TUPLET_FOR    );
	}
	
	/**
	 * Creates drumkit and percussion instrument translations.
	 * 
	 * - Creates translations between note values and the corresponding configured
	 *   percussion instrument shortcuts.
	 * - Creates translations between program numbers and the corresponding configured
	 *   drumkit shortcuts.
	 */
	public static void initPercussion() {
		
		// refresh combobox language
		ConfigComboboxModel.refill( Config.PERCUSSION );
		
		// get language
		String percSet = Config.get( Config.PERCUSSION );
		
		// init percussion translations
		initPercussionSetEnglish1();
		if ( Config.CBX_PERC_DE_1.equals( percSet ) ) {
			initPercussionSetGerman1();
		}
		
		// init names to integers (for percussion instruments)
		percussionNameToInt = new HashMap<String, Integer>();
		
		percussionNameToInt.put( get(PERCUSSION_HIGH_Q),          27 );
		percussionNameToInt.put( get(PERCUSSION_SLAP),            28 );
		percussionNameToInt.put( get(PERCUSSION_SCRATCH_PUSH),    29 );
		percussionNameToInt.put( get(PERCUSSION_SCRATCH_PULL),    30 );
		percussionNameToInt.put( get(PERCUSSION_STICKS),          31 );
		percussionNameToInt.put( get(PERCUSSION_SQUARE_CLICK),    32 );
		percussionNameToInt.put( get(PERCUSSION_METRONOME_CLICK), 33 );
		percussionNameToInt.put( get(PERCUSSION_METRONOME_BELL),  34 );
		percussionNameToInt.put( get(PERCUSSION_BASS_DRUM_2),     35 );
		percussionNameToInt.put( get(PERCUSSION_BASS_DRUM_1),     36 );
		percussionNameToInt.put( get(PERCUSSION_RIM_SHOT),        37 );
		percussionNameToInt.put( get(PERCUSSION_SNARE_DRUM_1),    38 );
		percussionNameToInt.put( get(PERCUSSION_HAND_CLAP),       39 );
		percussionNameToInt.put( get(PERCUSSION_SNARE_DRUM_2),    40 );
		percussionNameToInt.put( get(PERCUSSION_TOM_1),           41 );
		percussionNameToInt.put( get(PERCUSSION_HI_HAT_CLOSED),   42 );
		percussionNameToInt.put( get(PERCUSSION_TOM_2),           43 );
		percussionNameToInt.put( get(PERCUSSION_HI_HAT_PEDAL),    44 );
		percussionNameToInt.put( get(PERCUSSION_TOM_3),           45 );
		percussionNameToInt.put( get(PERCUSSION_HI_HAT_OPEN),     46 );
		percussionNameToInt.put( get(PERCUSSION_TOM_4),           47 );
		percussionNameToInt.put( get(PERCUSSION_TOM_5),           48 );
		percussionNameToInt.put( get(PERCUSSION_CRASH_CYMBAL_1),  49 );
		percussionNameToInt.put( get(PERCUSSION_TOM_6),           50 );
		percussionNameToInt.put( get(PERCUSSION_RIDE_CYMBAL_1),   51 );
		percussionNameToInt.put( get(PERCUSSION_CHINESE_CYMBAL),  52 );
		percussionNameToInt.put( get(PERCUSSION_RIDE_BELL),       53 );
		percussionNameToInt.put( get(PERCUSSION_TAMBOURINE),      54 );
		percussionNameToInt.put( get(PERCUSSION_SPLASH_CYMBAL),   55 );
		percussionNameToInt.put( get(PERCUSSION_COWBELL),         56 );
		percussionNameToInt.put( get(PERCUSSION_CRASH_CYMBAL_2),  57 );
		percussionNameToInt.put( get(PERCUSSION_VIBRA_SLAP),      58 );
		percussionNameToInt.put( get(PERCUSSION_RIDE_CYMBAL_2),   59 );
		percussionNameToInt.put( get(PERCUSSION_BONGO_HIGH),      60 );
		percussionNameToInt.put( get(PERCUSSION_BONGO_LOW),       61 );
		percussionNameToInt.put( get(PERCUSSION_CONGA_MUTE),      62 );
		percussionNameToInt.put( get(PERCUSSION_CONGA_OPEN),      63 );
		percussionNameToInt.put( get(PERCUSSION_CONGA_LOW),       64 );
		percussionNameToInt.put( get(PERCUSSION_TIMBALES_HIGH),   65 );
		percussionNameToInt.put( get(PERCUSSION_TIMBALES_LOW),    66 );
		percussionNameToInt.put( get(PERCUSSION_AGOGO_HIGH),      67 );
		percussionNameToInt.put( get(PERCUSSION_AGOGO_LOW),       68 );
		percussionNameToInt.put( get(PERCUSSION_CABASA),          69 );
		percussionNameToInt.put( get(PERCUSSION_MARACAS),         70 );
		percussionNameToInt.put( get(PERCUSSION_WHISTLE_SHORT),   71 );
		percussionNameToInt.put( get(PERCUSSION_WHISTLE_LONG),    72 );
		percussionNameToInt.put( get(PERCUSSION_GUIRO_SHORT),     73 );
		percussionNameToInt.put( get(PERCUSSION_GUIRO_LONG),      74 );
		percussionNameToInt.put( get(PERCUSSION_CLAVE),           75 );
		percussionNameToInt.put( get(PERCUSSION_WOOD_BLOCK_HIGH), 76 );
		percussionNameToInt.put( get(PERCUSSION_WOOD_BLOCK_LOW),  77 );
		percussionNameToInt.put( get(PERCUSSION_CUICA_MUTE),      78 );
		percussionNameToInt.put( get(PERCUSSION_CUICA_OPEN),      79 );
		percussionNameToInt.put( get(PERCUSSION_TRIANGLE_MUTE),   80 );
		percussionNameToInt.put( get(PERCUSSION_TRIANGLE_OPEN),   81 );
		percussionNameToInt.put( get(PERCUSSION_SHAKER),          82 );
		percussionNameToInt.put( get(PERCUSSION_JINGLE_BELL),     83 );
		percussionNameToInt.put( get(PERCUSSION_BELLTREE),        84 );
		percussionNameToInt.put( get(PERCUSSION_CASTANETS),       85 );
		percussionNameToInt.put( get(PERCUSSION_SURDO_MUTE),      86 );
		percussionNameToInt.put( get(PERCUSSION_SURDO_OPEN),      87 );
		
		// init integers to names (for percussion instruments)
		percussionIntToName = new HashMap<Integer, String>();
		for ( String key : percussionNameToInt.keySet() ) {
			percussionIntToName.put( percussionNameToInt.get(key), key );
		}
		
		// init names to integers (for drum kits)
		drumkitNameToInt = new HashMap<String, Integer>();
		
		drumkitNameToInt.put( get(DRUMKIT_STANDARD),     0 );
		drumkitNameToInt.put( get(DRUMKIT_ROOM),         8 );
		drumkitNameToInt.put( get(DRUMKIT_POWER),       16 );
		drumkitNameToInt.put( get(DRUMKIT_ELECTRONIC),  24 );
		drumkitNameToInt.put( get(DRUMKIT_TR808),       25 );
		drumkitNameToInt.put( get(DRUMKIT_JAZZ),        32 );
		drumkitNameToInt.put( get(DRUMKIT_BRUSH),       40 );
		drumkitNameToInt.put( get(DRUMKIT_ORCHESTRA),   48 );
		drumkitNameToInt.put( get(DRUMKIT_SOUND_FX),    56 );
		drumkitNameToInt.put( get(DRUMKIT_CM64_CM32),  127 );
		
		// init integers to names (for drum kits)
		drumkitIntToName = new HashMap<Integer, String>();
		for ( String key : drumkitNameToInt.keySet() ) {
			drumkitIntToName.put( drumkitNameToInt.get(key), key );
		}
	}
	
	/**
	 * Creates translations between note values and the corresponding configured
	 * instrument shortcuts.
	 */
	public static void initInstruments() {
		
		// refresh combobox language
		ConfigComboboxModel.refill( Config.INSTRUMENT );

		// get language
		String instrSet = Config.get( Config.INSTRUMENT );
		
		// init instrument translations
		instrIntToName = new HashMap<Integer, String>();
		instrNameToInt = new HashMap<String, Integer>();
		instrumentList = new ArrayList<InstrumentElement>();
		if ( Config.CBX_PERC_DE_1.equals( instrSet ) ) {
			initInstrumentsGerman1();
		}
		else {
			initInstrumentsEnglish1();
		}
	}
	
	/**
	 * Initializes note translations for international note names.
	 * 
	 * @param upperCase true if the resulting note names shall be capitalized
	 *                  -- otherwise: false
	 */
	private static void initNotesInternational( boolean upperCase ) {
		// initialize full notes
		if (upperCase) {
			notes[  0 ] = "C";
			notes[  2 ] = "D";
			notes[  4 ] = "E";
			notes[  5 ] = "F";
			notes[  7 ] = "G";
			notes[  9 ] = "A";
			notes[ 11 ] = "B";
		}
		else {
			notes[  0 ] = "c";
			notes[  2 ] = "d";
			notes[  4 ] = "e";
			notes[  5 ] = "f";
			notes[  7 ] = "g";
			notes[  9 ] = "a";
			notes[ 11 ] = "b";
		}
	}
	
	/**
	 * Initializes note translations for german note names.
	 * 
	 * @param upperCase true if the resulting note names shall be capitalized
	 *                  -- otherwise: false
	 */
	private static void initNotesGerman( boolean upperCase ) {
		// initialize full notes
		if (upperCase) {
			notes[  0 ] = "C";
			notes[  2 ] = "D";
			notes[  4 ] = "E";
			notes[  5 ] = "F";
			notes[  7 ] = "G";
			notes[  9 ] = "A";
			notes[ 10 ] = "B";
			notes[ 11 ] = "H";
		}
		else {
			notes[  0 ] = "c";
			notes[  2 ] = "d";
			notes[  4 ] = "e";
			notes[  5 ] = "f";
			notes[  7 ] = "g";
			notes[  9 ] = "a";
			notes[ 10 ] = "b";
			notes[ 11 ] = "h";
		}
		
		// redefine what is a half tone (the german system is different)
		isHalfTone = new byte[ 4 ];
		isHalfTone[ 0 ] =  1; // C#, Db
		isHalfTone[ 1 ] =  3; // D#, Eb
		isHalfTone[ 2 ] =  6; // F#, Gb
		isHalfTone[ 3 ] =  8; // G#, Ab
	}
	
	/**
	 * Initializes note translations for italien note names.
	 * 
	 * @param upperCase true if the resulting note names shall have an capitalized
	 *                  first character -- otherwise: false
	 */
	private static void initNotesItalian( boolean upperCase ) {
		// initialize full notes
		if (upperCase) {
			notes[  0 ] = "Do";
			notes[  2 ] = "Re";
			notes[  4 ] = "Mi";
			notes[  5 ] = "Fa";
			notes[  7 ] = "Sol";
			notes[  9 ] = "La";
			notes[ 11 ] = "Si";
		}
		else {
			notes[  0 ] = "do";
			notes[  2 ] = "re";
			notes[  4 ] = "mi";
			notes[  5 ] = "fa";
			notes[  7 ] = "sol";
			notes[  9 ] = "la";
			notes[ 11 ] = "si";
		}
	}
	
	/**
	 * Sets up note names using the international octave naming system  
	 * C-1, C0, C1, C2, C3, C4...
	 */
	private static void initOctavesInternational() {
		// define unmodified note names
		ArrayList<NamedInteger> noteNames = new ArrayList<NamedInteger>();
		byte i = 0; // C-1
		for ( String name : notes ) {
			NamedInteger note = new NamedInteger( name, i );
			noteNames.add( note );
			i++;
		}
		
		// initialize octaves
		byte postfix = -1;
		OCTAVE:
		for ( int octave = 0; ; octave++ ) {
			int increment = octave * 12;
			NAME:
			for ( NamedInteger name : noteNames ) {
				String newName  = name.name  + postfix;
				int    newValue = name.value + increment;
				if ( newValue > 127 )
					break OCTAVE;
				noteNameToInt.put( newName, newValue );
			}
			postfix++;
		}
	}
	
	/**
	 * Sets up note names using the traditional german octave naming system  
	 * lower case c, c', c'', c'''... for higher octaves and  
	 * upper case C, C', C'', C'''... for lower octaves
	 */
	private static void initOctavesGerman() {
		
		// define unmodified note names for higher notes (lower case)
		ArrayList<NamedInteger> noteNames = new ArrayList<NamedInteger>();
		byte i = 48; // middle C
		for ( String name : notes ) {
			// use lower case for higher octaves
			name = name.toLowerCase();
			NamedInteger note = new NamedInteger( name, i );
			noteNames.add( note );
			i++;
		}
		
		// initialize unmodified and higher notes
		StringBuffer postfix = new StringBuffer( "" );
		OCTAVE:
		for ( int octave = 0; ; octave++ ) {
			if ( octave > 0 )
				postfix.append( "'" );
			int increment = octave * 12;
			NAME:
			for ( NamedInteger name : noteNames ) {
				String newName  = name.name  + postfix;
				int    newValue = name.value + increment;
				if ( newValue > 127 )
					break OCTAVE;
				noteNameToInt.put( newName, newValue );
			}
		}
		
		// define unmodified note names for lower notes (upper case)
		for ( NamedInteger note : noteNames ) {
			note.name = note.name.substring( 0, 1 ).toUpperCase()
			          + note.name.substring( 1 )
			          ;
		}
		Collections.reverse( noteNames );
		postfix = new StringBuffer( "" );
		
		// initialize lower notes
		OCTAVE:
		for ( int octave = 1; ; octave++ ) {
			if ( octave > 1 )
				postfix.append( "'" );
			int decrement = octave * 12;
			NAME:
			for ( NamedInteger name : noteNames ) {
				String newName  = name.name  + postfix;
				int    newValue = name.value - decrement;
				if ( newValue < 0 )
					break OCTAVE;
				noteNameToInt.put( newName, newValue );
			}
		}
	}
	
	/**
	 * Sets up note names using  
	 * `+`, `++`, `+++`, ... for higher octaves and  
	 * `-`, `--`, `---`, ... for lower octaves.
	 */
	private static void initOctavesPlusMinus() {
		
		// define unmodified note names
		ArrayList<NamedInteger> noteNames = new ArrayList<NamedInteger>();
		byte i = 48; // middle C
		for ( String name : notes ) {
			NamedInteger note = new NamedInteger( name, i );
			noteNames.add( note );
			i++;
		}
		
		// initialize unmodified and higher notes
		StringBuffer postfix = new StringBuffer( "" );
		OCTAVE:
		for ( int octave = 0; ; octave++ ) {
			if ( octave > 0 )
				postfix.append( "+" );
			int increment = octave * 12;
			NAME:
			for ( NamedInteger name : noteNames ) {
				String newName  = name.name  + postfix;
				int    newValue = name.value + increment;
				if ( newValue > 127 )
					break OCTAVE;
				noteNameToInt.put( newName, newValue );
			}
		}
		
		// initialize lower notes
		Collections.reverse( noteNames );
		postfix = new StringBuffer( "" );
		OCTAVE:
		for ( int octave = 1; ; octave++ ) {
			postfix.append( "-" );
			int decrement = octave * 12;
			NAME:
			for ( NamedInteger name : noteNames ) {
				String newName  = name.name  + postfix;
				int    newValue = name.value - decrement;
				if ( newValue < 0 )
					break OCTAVE;
				noteNameToInt.put( newName, newValue );
			}
		}
	}
	
	/**
	 * Sets up english drumkit and percussion instrument translation shortcuts.
	 */
	private static void initPercussionSetEnglish1() {
		// percussion instruments
		set( PERCUSSION_HIGH_Q,          "HIGH_Q"          );
		set( PERCUSSION_SLAP,            "SLAP"            );
		set( PERCUSSION_SCRATCH_PUSH,    "SCRATCH_PUSH"    );
		set( PERCUSSION_SCRATCH_PULL,    "SCRATCH_PULL"    );
		set( PERCUSSION_STICKS,          "STICKS"          );
		set( PERCUSSION_SQUARE_CLICK,    "SQUARE_CLICK"    );
		set( PERCUSSION_METRONOME_CLICK, "METRONOME_CLICK" );
		set( PERCUSSION_METRONOME_BELL,  "METRONOME_BELL"  );
		set( PERCUSSION_BASS_DRUM_2,     "BASS_DRUM_2"     );
		set( PERCUSSION_BASS_DRUM_1,     "BASS_DRUM_1"     );
		set( PERCUSSION_RIM_SHOT,        "RIM_SHOT"        );
		set( PERCUSSION_SNARE_DRUM_1,    "SNARE_DRUM_1"    );
		set( PERCUSSION_HAND_CLAP,       "HAND_CLAP"       );
		set( PERCUSSION_SNARE_DRUM_2,    "SNARE_DRUM_2"    );
		set( PERCUSSION_TOM_1,           "TOM_1"           );
		set( PERCUSSION_HI_HAT_CLOSED,   "HI_HAT_CLOSED"   );
		set( PERCUSSION_TOM_2,           "TOM_2"           );
		set( PERCUSSION_HI_HAT_PEDAL,    "HI_HAT_PEDAL"    );
		set( PERCUSSION_TOM_3,           "TOM_3"           );
		set( PERCUSSION_HI_HAT_OPEN,     "HI_HAT_OPEN"     );
		set( PERCUSSION_TOM_4,           "TOM_4"           );
		set( PERCUSSION_TOM_5,           "TOM_5"           );
		set( PERCUSSION_CRASH_CYMBAL_1,  "CRASH_CYMBAL_1"  );
		set( PERCUSSION_TOM_6,           "TOM_6"           );
		set( PERCUSSION_RIDE_CYMBAL_1,   "RIDE_CYMBAL_1"   );
		set( PERCUSSION_CHINESE_CYMBAL,  "CHINESE_CYMBAL"  );
		set( PERCUSSION_RIDE_BELL,       "RIDE_BELL"       );
		set( PERCUSSION_TAMBOURINE,      "TAMBOURINE"      );
		set( PERCUSSION_SPLASH_CYMBAL,   "SPLASH_CYMBAL"   );
		set( PERCUSSION_COWBELL,         "COWBELL"         );
		set( PERCUSSION_CRASH_CYMBAL_2,  "CRASH_CYMBAL_2"  );
		set( PERCUSSION_VIBRA_SLAP,      "VIBRA_SLAP"      );
		set( PERCUSSION_RIDE_CYMBAL_2,   "RIDE_CYMBAL_2"   );
		set( PERCUSSION_BONGO_HIGH,      "BONGO_HIGH"      );
		set( PERCUSSION_BONGO_LOW,       "BONGO_LOW"       );
		set( PERCUSSION_CONGA_MUTE,      "CONGA_MUTE"      );
		set( PERCUSSION_CONGA_OPEN,      "CONGA_OPEN"      );
		set( PERCUSSION_CONGA_LOW,       "CONGA_LOW"       );
		set( PERCUSSION_TIMBALES_HIGH,   "TIMBALES_HIGH"   );
		set( PERCUSSION_TIMBALES_LOW,    "TIMBALES_LOW"    );
		set( PERCUSSION_AGOGO_HIGH,      "AGOGO_HIGH"      );
		set( PERCUSSION_AGOGO_LOW,       "AGOGO_LOW"       );
		set( PERCUSSION_CABASA,          "CABASA"          );
		set( PERCUSSION_MARACAS,         "MARACAS"         );
		set( PERCUSSION_WHISTLE_SHORT,   "WHISTLE_SHORT"   );
		set( PERCUSSION_WHISTLE_LONG,    "WHISTLE_LONG"    );
		set( PERCUSSION_GUIRO_SHORT,     "GUIRO_SHORT"     );
		set( PERCUSSION_GUIRO_LONG,      "GUIRO_LONG"      );
		set( PERCUSSION_CLAVE,           "CLAVE"           );
		set( PERCUSSION_WOOD_BLOCK_HIGH, "WOOD_BLOCK_HIGH" );
		set( PERCUSSION_WOOD_BLOCK_LOW,  "WOOD_BLOCK_LOW"  );
		set( PERCUSSION_CUICA_MUTE,      "CUICA_MUTE"      );
		set( PERCUSSION_CUICA_OPEN,      "CUICA_OPEN"      );
		set( PERCUSSION_TRIANGLE_MUTE,   "TRIANGLE_MUTE"   );
		set( PERCUSSION_TRIANGLE_OPEN,   "TRIANGLE_OPEN"   );
		set( PERCUSSION_SHAKER,          "SHAKER"          );
		set( PERCUSSION_JINGLE_BELL,     "JINGLE_BELL"     );
		set( PERCUSSION_BELLTREE,        "BELLTREE"        );
		set( PERCUSSION_CASTANETS,       "CASTANETS"       );
		set( PERCUSSION_SURDO_MUTE,      "SURDO_MUTE"      );
		set( PERCUSSION_SURDO_OPEN,      "SURDO_OPEN"      );
		
		// drumkits
		set( DRUMKIT_STANDARD,   "STANDARD"   );
		set( DRUMKIT_ROOM,       "ROOM"       );
		set( DRUMKIT_POWER,      "POWER"      );
		set( DRUMKIT_ELECTRONIC, "ELECTRONIC" );
		set( DRUMKIT_TR808,      "TR808"      );
		set( DRUMKIT_JAZZ,       "JAZZ"       );
		set( DRUMKIT_BRUSH,      "BRUSH"      );
		set( DRUMKIT_ORCHESTRA,  "ORCHESTRA"  );
		set( DRUMKIT_SOUND_FX,   "SOUND_FX"   );
		set( DRUMKIT_CM64_CM32,  "CM64_CM32"  );
	}
	
	/**
	 * Sets up german drumkit and percussion instrument translation shortcuts.
	 */
	private static void initPercussionSetGerman1() {
		// percussion instruments
		set( PERCUSSION_HIGH_Q,          "HIGH_Q"              );
		set( PERCUSSION_SLAP,            "SCHLAG"              );
		set( PERCUSSION_SCRATCH_PUSH,    "SCRATCH_PUSH"        );
		set( PERCUSSION_SCRATCH_PULL,    "SCRATCH_PULL"        );
		set( PERCUSSION_STICKS,          "STÖCKE"              );
		set( PERCUSSION_SQUARE_CLICK,    "SQUARE_CLICK"        );
		set( PERCUSSION_METRONOME_CLICK, "METRONOM_KLICK"      );
		set( PERCUSSION_METRONOME_BELL,  "METRONOM_KLINGEL"    );
		set( PERCUSSION_BASS_DRUM_2,     "BASSTROMMEL_2"       );
		set( PERCUSSION_BASS_DRUM_1,     "BASSTROMMEL_1"       );
		set( PERCUSSION_RIM_SHOT,        "RIMSHOT"             );
		set( PERCUSSION_SNARE_DRUM_1,    "KLEINE_TROMMEL_1"    );
		set( PERCUSSION_HAND_CLAP,       "KLATSCHEN"           );
		set( PERCUSSION_SNARE_DRUM_2,    "KLEINE_TROMMEL_2"    );
		set( PERCUSSION_TOM_1,           "TOMTOM_1"            );
		set( PERCUSSION_HI_HAT_CLOSED,   "HI_HAT_GESCHLOSSEN"  );
		set( PERCUSSION_TOM_2,           "TOMTOM_2"            );
		set( PERCUSSION_HI_HAT_PEDAL,    "HI_HAT_PEDAL"        );
		set( PERCUSSION_TOM_3,           "TOMTOM_3"            );
		set( PERCUSSION_HI_HAT_OPEN,     "HI_HAT_OFFEN"        );
		set( PERCUSSION_TOM_4,           "TOMTOM_4"            );
		set( PERCUSSION_TOM_5,           "TOMTOM_5"            );
		set( PERCUSSION_CRASH_CYMBAL_1,  "CRASH_BECKEN_1"      );
		set( PERCUSSION_TOM_6,           "TOMTOM_6"            );
		set( PERCUSSION_RIDE_CYMBAL_1,   "RIDE_BECKEN_1"       );
		set( PERCUSSION_CHINESE_CYMBAL,  "CHINESISCHES_BECKEN" );
		set( PERCUSSION_RIDE_BELL,       "RIDE_BELL"           );
		set( PERCUSSION_TAMBOURINE,      "TAMBURIN"            );
		set( PERCUSSION_SPLASH_CYMBAL,   "SPLASH_BECKEN"       );
		set( PERCUSSION_COWBELL,         "KUHGLOCKE"           );
		set( PERCUSSION_CRASH_CYMBAL_2,  "CRASH_BECKEN_2"      );
		set( PERCUSSION_VIBRA_SLAP,      "VIBRASLAP"           );
		set( PERCUSSION_RIDE_CYMBAL_2,   "RIDE_BECKEN_2"       );
		set( PERCUSSION_BONGO_HIGH,      "BONGO_HOCH"          );
		set( PERCUSSION_BONGO_LOW,       "BONGO_TIEF"          );
		set( PERCUSSION_CONGA_MUTE,      "CONGA_GEDÄMPFT"      );
		set( PERCUSSION_CONGA_OPEN,      "CONGA_OFFEN"         );
		set( PERCUSSION_CONGA_LOW,       "CONGA_TIEF"          );
		set( PERCUSSION_TIMBALES_HIGH,   "TIMBALES_HOCH"       );
		set( PERCUSSION_TIMBALES_LOW,    "TIMBALES_TIEF"       );
		set( PERCUSSION_AGOGO_HIGH,      "AGOGO_HOCH"          );
		set( PERCUSSION_AGOGO_LOW,       "AGOGO_TIEF"          );
		set( PERCUSSION_CABASA,          "CABASA"              );
		set( PERCUSSION_MARACAS,         "MARACAS"             );
		set( PERCUSSION_WHISTLE_SHORT,   "PFEIFE_KURZ"         );
		set( PERCUSSION_WHISTLE_LONG,    "PFEIFE_LANG"         );
		set( PERCUSSION_GUIRO_SHORT,     "GUIRO_KURZ"          );
		set( PERCUSSION_GUIRO_LONG,      "GUIRO_LANG"          );
		set( PERCUSSION_CLAVE,           "CLAVE"               );
		set( PERCUSSION_WOOD_BLOCK_HIGH, "HOLZBLOCK_HOCH"      );
		set( PERCUSSION_WOOD_BLOCK_LOW,  "HOLZBLOCK_TIEF"      );
		set( PERCUSSION_CUICA_MUTE,      "CUICA_GEDÄMPFT"      );
		set( PERCUSSION_CUICA_OPEN,      "CUICA_OFFEN"         );
		set( PERCUSSION_TRIANGLE_MUTE,   "TRIANGEL_GEDÄMPFT"   );
		set( PERCUSSION_TRIANGLE_OPEN,   "TRIANGEL_OFFEN"      );
		set( PERCUSSION_SHAKER,          "SCHÜTTLER"           );
		set( PERCUSSION_JINGLE_BELL,     "SCHELLE"             );
		set( PERCUSSION_BELLTREE,        "GLOCKENBAUM"         );
		set( PERCUSSION_CASTANETS,       "KASTAGNETTEN"        );
		set( PERCUSSION_SURDO_MUTE,      "SURDO_GEDÄMPFT"      );
		set( PERCUSSION_SURDO_OPEN,      "SURDO_OFFEN"         );
		
		// drumkits
		set( DRUMKIT_STANDARD,   "STANDARD"      );
		set( DRUMKIT_ROOM,       "RAUM"          );
		set( DRUMKIT_POWER,      "POWER"         );
		set( DRUMKIT_ELECTRONIC, "ELEKTRO"       );
		set( DRUMKIT_TR808,      "TR808"         );
		set( DRUMKIT_JAZZ,       "JAZZ"          );
		set( DRUMKIT_BRUSH,      "BÜRSTE"        );
		set( DRUMKIT_ORCHESTRA,  "ORCHESTER"     );
		set( DRUMKIT_SOUND_FX,   "SOUND_EFFEKTE" );
		set( DRUMKIT_CM64_CM32,  "CM64_CM32"     );
	}
	
	/**
	 * Sets up programming keywords and syntax elements for musicians.
	 */
	private static void initSyntaxMusician1() {
		setSyntax( SYNTAX_DEFINE,          "DEFINE"         );
		setSyntax( SYNTAX_COMMENT,         ";"              );
		setSyntax( SYNTAX_GLOBAL,          "*"              );
		setSyntax( SYNTAX_P,               "P"              );
		setSyntax( SYNTAX_END,             "END"            );
		setSyntax( SYNTAX_MACRO,           "RIFF"           );
		setSyntax( SYNTAX_INCLUDE,         "PLAY"           );
		setSyntax( SYNTAX_INSTRUMENTS,     "INSTRUMENTS"    );
		setSyntax( SYNTAX_BPM,             "bpm"            );
		setSyntax( SYNTAX_OPT_SEPARATOR,   ","              );
		setSyntax( SYNTAX_OPT_ASSIGNER,    ":"              );
		setSyntax( SYNTAX_PROG_BANK_SEP,   "-"              );
		setSyntax( SYNTAX_BANK_SEP,        "."              );
		setSyntax( SYNTAX_VOLUME,          "volume"         );
		setSyntax( SYNTAX_V,               "vol"            );
		setSyntax( SYNTAX_STACCATO,        "staccato"       );
		setSyntax( SYNTAX_S,               "stac"           );
		setSyntax( SYNTAX_MULTIPLE,        "polyphonic"     );
		setSyntax( SYNTAX_M,               "poly"           );
		setSyntax( SYNTAX_QUANTITY,        "quantity"       );
		setSyntax( SYNTAX_Q,               "q"              );
		setSyntax( SYNTAX_PAUSE,           "-"              );
		setSyntax( SYNTAX_CHORD,           "CHORD"          );
		setSyntax( SYNTAX_INCLUDE_FILE,    "PLAY_FILE"      );
		setSyntax( SYNTAX_32,              "TS"             );
		setSyntax( SYNTAX_16,              "S"              );
		setSyntax( SYNTAX_8,               "E"              );
		setSyntax( SYNTAX_4,               "Q"              );
		setSyntax( SYNTAX_2,               "H"              );
		setSyntax( SYNTAX_1,               "F"              );
		setSyntax( SYNTAX_M1,              "*1"             );
		setSyntax( SYNTAX_M2,              "*2"             );
		setSyntax( SYNTAX_M4,              "*4"             );
		setSyntax( SYNTAX_M8,              "*8"             );
		setSyntax( SYNTAX_M16,             "*16"            );
		setSyntax( SYNTAX_M32,             "*32"            );
		setSyntax( SYNTAX_DOT,             "."              );
		setSyntax( SYNTAX_TRIPLET,         ")3"             );
		setSyntax( SYNTAX_TUPLET,          ")"              );
		setSyntax( SYNTAX_TUPLET_FOR,      ":"              );
	}
	
	/**
	 * Sets up programming keywords and syntax elements for programmers (syntax set 1).
	 */
	private static void initSyntaxProgrammer1() {
		setSyntax( SYNTAX_DEFINE,          "DEFINE"         );
		setSyntax( SYNTAX_COMMENT,         ";"              );
		setSyntax( SYNTAX_GLOBAL,          "*"              );
		setSyntax( SYNTAX_P,               "P"              );
		setSyntax( SYNTAX_END,             "END"            );
		setSyntax( SYNTAX_MACRO,           "MACRO"          );
		setSyntax( SYNTAX_INCLUDE,         "INCLUDE"        );
		setSyntax( SYNTAX_INSTRUMENTS,     "INSTRUMENTS"    );
		setSyntax( SYNTAX_BPM,             "bpm"            );
		setSyntax( SYNTAX_OPT_SEPARATOR,   ","              );
		setSyntax( SYNTAX_OPT_ASSIGNER,    "="              );
		setSyntax( SYNTAX_PROG_BANK_SEP,   ","              );
		setSyntax( SYNTAX_BANK_SEP,        "/"              );
		setSyntax( SYNTAX_VOLUME,          "volume"         );
		setSyntax( SYNTAX_V,               "v"              );
		setSyntax( SYNTAX_STACCATO,        "staccato"       );
		setSyntax( SYNTAX_S,               "s"              );
		setSyntax( SYNTAX_MULTIPLE,        "multiple"       );
		setSyntax( SYNTAX_M,               "m"              );
		setSyntax( SYNTAX_QUANTITY,        "quantity"       );
		setSyntax( SYNTAX_Q,               "q"              );
		setSyntax( SYNTAX_PAUSE,           "-"              );
		setSyntax( SYNTAX_CHORD,           "CHORD"          );
		setSyntax( SYNTAX_INCLUDE_FILE,    "INCLUDE_FILE"   );
		setSyntax( SYNTAX_32,              "/32"            );
		setSyntax( SYNTAX_16,              "/16"            );
		setSyntax( SYNTAX_8,               "/8"             );
		setSyntax( SYNTAX_4,               "/4"             );
		setSyntax( SYNTAX_2,               "/2"             );
		setSyntax( SYNTAX_1,               "/1"             );
		setSyntax( SYNTAX_M1,              "*1"             );
		setSyntax( SYNTAX_M2,              "*2"             );
		setSyntax( SYNTAX_M4,              "*4"             );
		setSyntax( SYNTAX_M8,              "*8"             );
		setSyntax( SYNTAX_M16,             "*16"            );
		setSyntax( SYNTAX_M32,             "*32"            );
		setSyntax( SYNTAX_DOT,             "."              );
		setSyntax( SYNTAX_TRIPLET,         "T"              );
		setSyntax( SYNTAX_TUPLET,          "T"              );
		setSyntax( SYNTAX_TUPLET_FOR,      ":"              );
	}
	
	/**
	 * Sets up programming keywords and syntax elements for programmers (syntax set 2).
	 */
	private static void initSyntaxProgrammer2() {
		setSyntax( SYNTAX_DEFINE,          "DEFINE"       );
		setSyntax( SYNTAX_COMMENT,         "#"            );
		setSyntax( SYNTAX_GLOBAL,          "*"            );
		setSyntax( SYNTAX_P,               "P"            );
		setSyntax( SYNTAX_END,             "END"          );
		setSyntax( SYNTAX_MACRO,           "MACRO"        );
		setSyntax( SYNTAX_INCLUDE,         "INCLUDE"      );
		setSyntax( SYNTAX_INSTRUMENTS,     "INSTRUMENTS"  );
		setSyntax( SYNTAX_BPM,             "bpm"          );
		setSyntax( SYNTAX_OPT_SEPARATOR,   ","            );
		setSyntax( SYNTAX_OPT_ASSIGNER,    "="            );
		setSyntax( SYNTAX_PROG_BANK_SEP,   ","            );
		setSyntax( SYNTAX_BANK_SEP,        ":"            );
		setSyntax( SYNTAX_VOLUME,          "vol"          );
		setSyntax( SYNTAX_V,               "v"            );
		setSyntax( SYNTAX_STACCATO,        "stac"         );
		setSyntax( SYNTAX_S,               "s"            );
		setSyntax( SYNTAX_MULTIPLE,        "m"            );
		setSyntax( SYNTAX_M,               "&"            );
		setSyntax( SYNTAX_QUANTITY,        "q"            );
		setSyntax( SYNTAX_Q,               "x"            );
		setSyntax( SYNTAX_PAUSE,           "-"            );
		setSyntax( SYNTAX_CHORD,           "CHORD"        );
		setSyntax( SYNTAX_INCLUDE_FILE,    "INCLUDE_FILE" );
		setSyntax( SYNTAX_32,              "/32"          );
		setSyntax( SYNTAX_16,              "/16"          );
		setSyntax( SYNTAX_8,               "/8"           );
		setSyntax( SYNTAX_4,               "/4"           );
		setSyntax( SYNTAX_2,               "/2"           );
		setSyntax( SYNTAX_1,               "/1"           );
		setSyntax( SYNTAX_M1,              "*1"           );
		setSyntax( SYNTAX_M2,              "*2"           );
		setSyntax( SYNTAX_M4,              "*4"           );
		setSyntax( SYNTAX_M8,              "*8"           );
		setSyntax( SYNTAX_M16,             "*16"          );
		setSyntax( SYNTAX_M32,             "*32"          );
		setSyntax( SYNTAX_DOT,             "."            );
		setSyntax( SYNTAX_TRIPLET,         "T"            );
		setSyntax( SYNTAX_TUPLET,          "T"            );
		setSyntax( SYNTAX_TUPLET_FOR,      ":"            );
	}
	
	/**
	 * Sets one specific syntax keyword.
	 * This is called indirectly by {@link #initSyntax()} while initializing the
	 * currently configured MidicaPL syntax.
	 * 
	 * @param id        identifier defining the meaning of the keyword to specify
	 * @param keyword   the keyword to be configured
	 */
	private static void setSyntax( String id, String keyword ) {
		syntax.put( id, keyword );
	}
	
	/**
	 * Creates a plain syntax element and adds it to the internal data structure of
	 * syntax elements.
	 * This is used later in order to display the syntax categories and elements
	 * in the info view of the GUI.
	 * 
	 * @param id identifier of the syntax element to add
	 */
	private static void addSyntaxForInfoView( String id ) {
		String description = get( id );
		String keyword     = getSyntax( id );
		SyntaxElement elem = new SyntaxElement( id, description, keyword, false );
		syntaxList.add( elem );
	}
	
	/**
	 * Creates a syntax category and adds it to the internal data structure of
	 * syntax elements.
	 * This is used later in order to display the syntax categories and elements
	 * in the info view of the GUI.
	 * 
	 * @param categoryName name of the syntax category
	 */
	private static void addSyntaxCategory( String categoryName ) {
		SyntaxElement elem = new SyntaxElement( categoryName, "", "", true );
		syntaxList.add( elem );
	}
	
	/**
	 * Creates english translations between note values and the corresponding
	 * instrument shortcuts.
	 */
	private static void initInstrumentsEnglish1() {
		// piano
		addInstrCategory( INSTR_CAT_PIANO );
		setInstrument( 0,   "ACOUSTIC_GRAND_PIANO"  );
		setInstrument( 1,   "BRIGHT_ACOUSTIC_PIANO" );
		setInstrument( 2,   "ELECTRIC_GRAND_PIANO"  );
		setInstrument( 3,   "HONKY_TONK_PIANO"      );
		setInstrument( 4,   "ELECTRIC_PIANO_1"      );
		setInstrument( 5,   "ELECTRIC_PIANO_2"      );
		setInstrument( 6,   "HARPSICHORD"           );
		setInstrument( 7,   "CLAVINET"              );
		
		// chromatic percussion
		addInstrCategory( INSTR_CAT_CHROM_PERC );
		setInstrument( 8,   "CELESTA"               );
		setInstrument( 9,   "GLOCKENSPIEL"          );
		setInstrument( 10,  "MUSIC_BOX"             );
		setInstrument( 11,  "VIBRAPHONE"            );
		setInstrument( 12,  "MARIMBA"               );
		setInstrument( 13,  "XYLOPHONE"             );
		setInstrument( 14,  "TUBULAR_BELL"          );
		setInstrument( 15,  "DULCIMER"              );
		
		// organ
		addInstrCategory( INSTR_CAT_ORGAN );
		setInstrument( 16,  "DRAWBAR_ORGAN"         );
		setInstrument( 17,  "PERCUSSIVE_ORGAN"      );
		setInstrument( 18,  "ROCK_ORGAN"            );
		setInstrument( 19,  "CHURCH_ORGAN"          );
		setInstrument( 20,  "REED_ORGAN"            );
		setInstrument( 21,  "ACCORDION"             );
		setInstrument( 22,  "HARMONICA"             );
		setInstrument( 23,  "TANGO_ACCORDION"       );
		
		// guitar
		addInstrCategory( INSTR_CAT_GUITAR );
		setInstrument( 24,  "NYLON_GUITAR"          );
		setInstrument( 25,  "STEEL_GUITAR"          );
		setInstrument( 26,  "E_GUITAR_JAZZ"         );
		setInstrument( 27,  "E_GUITAR_CLEAN"        );
		setInstrument( 28,  "E_GUITAR_MUTED"        );
		setInstrument( 29,  "OVERDRIVEN_GUITAR"     );
		setInstrument( 30,  "DISTORTION_GUITAR"     );
		setInstrument( 31,  "GUITAR_HARMONICS"      );
		
		// bass
		addInstrCategory( INSTR_CAT_BASS );
		setInstrument( 32,  "ACOUSTIC_BASS"         );
		setInstrument( 33,  "E_BASS_FINGER"         );
		setInstrument( 34,  "E_BASS_PICK"           );
		setInstrument( 35,  "FRETLESS_BASS"         );
		setInstrument( 36,  "SLAP_BASS_1"           );
		setInstrument( 37,  "SLAP_BASS_2"           );
		setInstrument( 38,  "SYNTH_BASS_1"          );
		setInstrument( 39,  "SYNTH_BASS_2"          );
		
		// strings
		addInstrCategory( INSTR_CAT_STRINGS );
		setInstrument( 40,  "VIOLIN"                );
		setInstrument( 41,  "VIOLA"                 );
		setInstrument( 42,  "CELLO"                 );
		setInstrument( 43,  "CONTRABASS"            );
		setInstrument( 44,  "TREMOLO_STRINGS"       );
		setInstrument( 45,  "PIZZACATO_STRINGS"     );
		setInstrument( 46,  "ORCHESTRAL_HARP"       );
		setInstrument( 47,  "TIMPANI"               );
		
		// ensemble
		addInstrCategory( INSTR_CAT_ENSEMBLE );
		setInstrument( 48,  "STRING_ENSEMBLE_1"     );
		setInstrument( 49,  "STRING_ENSEMBLE_2"     );
		setInstrument( 50,  "SYNTH_STRINGS_1"       );
		setInstrument( 51,  "SYNTH_STRINGS_2"       );
		setInstrument( 52,  "CHOIR_AAHS"            );
		setInstrument( 53,  "VOICE_OOHS"            );
		setInstrument( 54,  "SYNTH_CHOIR"           );
		setInstrument( 55,  "ORCHESTRA_HIT"         );
		
		// brass
		addInstrCategory( INSTR_CAT_BRASS );
		setInstrument( 56,  "TRUMPET"               );
		setInstrument( 57,  "TROMBONE"              );
		setInstrument( 58,  "TUBA"                  );
		setInstrument( 59,  "MUTED_TRUMPET"         );
		setInstrument( 60,  "FRENCH_HORN"           );
		setInstrument( 61,  "BRASS_SECTION"         );
		setInstrument( 62,  "SYNTH_BRASS_1"         );
		setInstrument( 63,  "SYNTH_BRASS_2"         );
		
		// reed
		addInstrCategory( INSTR_CAT_REED );
		setInstrument( 64,  "SOPRANO_SAX"           );
		setInstrument( 65,  "ALTO_SAX"              );
		setInstrument( 66,  "TENOR_SAX"             );
		setInstrument( 67,  "BARITONE_SAX"          );
		setInstrument( 68,  "OBOE"                  );
		setInstrument( 69,  "ENGLISH_HORN"          );
		setInstrument( 70,  "BASSOON"               );
		setInstrument( 71,  "CLARINET"              );
		
		// pipe
		addInstrCategory( INSTR_CAT_PIPE );
		setInstrument( 72,  "PICCOLO"               );
		setInstrument( 73,  "FLUTE"                 );
		setInstrument( 74,  "RECORDER"              );
		setInstrument( 75,  "PAN_FLUTE"             );
		setInstrument( 76,  "BLOWN_BOTTLE"          );
		setInstrument( 77,  "SHAKUHACHI"            );
		setInstrument( 78,  "WHISTLE"               );
		setInstrument( 79,  "OCARINA"               );
		
		// synth lead
		addInstrCategory( INSTR_CAT_SYNTH_LEAD );
		setInstrument( 80,  "LEAD_SQUARE"          );
		setInstrument( 81,  "LEAD_SAWTOOTH"        );
		setInstrument( 82,  "LEAD_CALLIOPE"        );
		setInstrument( 83,  "LEAD_CHIFF"           );
		setInstrument( 84,  "LEAD_CHARANGO"        );
		setInstrument( 85,  "LEAD_VOICE"           );
		setInstrument( 86,  "LEAD_FIFTHS"          );
		setInstrument( 87,  "LEAD_BASS_LEAD"       );
		
		// synth pad
		addInstrCategory( INSTR_CAT_SYNTH_PAD );
		setInstrument( 88,  "PAD_NEW_AGE"           );
		setInstrument( 89,  "PAD_WARM"              );
		setInstrument( 90,  "PAD_POLYSYNTH"         );
		setInstrument( 91,  "PAD_CHOIR"             );
		setInstrument( 92,  "PAD_POWED"             );
		setInstrument( 93,  "PAD_METALLIC"          );
		setInstrument( 94,  "PAD_HALO"              );
		setInstrument( 95,  "PAD_SWEEP"             );
		
		// synth effects
		addInstrCategory( INSTR_CAT_SYNTH_EFFECTS );
		setInstrument( 96,  "FX_RAIN"               );
		setInstrument( 97,  "FX_SOUNDTRACK"         );
		setInstrument( 98,  "FX_CRYSTAL"            );
		setInstrument( 99,  "FX_ATMOSPHERE"         );
		setInstrument( 100, "FX_BRIGHTNESS"         );
		setInstrument( 101, "FX_GOBLINS"            );
		setInstrument( 102, "FX_ECHOES"             );
		setInstrument( 103, "FX_SCI_FI"             );
		
		// ethnic
		addInstrCategory( INSTR_CAT_ETHNIC );
		setInstrument( 104, "SITAR"                 );
		setInstrument( 105, "BANJO"                 );
		setInstrument( 106, "SHAMISEN"              );
		setInstrument( 107, "KOTO"                  );
		setInstrument( 108, "KALIMBA"               );
		setInstrument( 109, "BAG_PIPE"              );
		setInstrument( 110, "FIDDLE"                );
		setInstrument( 111, "SHANAI"                );
		
		// percussive
		addInstrCategory( INSTR_CAT_PERCUSSIVE );
		setInstrument( 112, "TINKLE_BELL"           );
		setInstrument( 113, "AGOGO"                 );
		setInstrument( 114, "STEEL_DRUMS"           );
		setInstrument( 115, "WOODBLOCK"             );
		setInstrument( 116, "TAIKO_DRUM"            );
		setInstrument( 117, "MELODIC_TOM"           );
		setInstrument( 118, "SYNTH_DRUM"            );
		setInstrument( 119, "REVERSE_CYMBAL"        );
		
		// sound effects
		addInstrCategory( INSTR_CAT_SOUND_EFFECTS );
		setInstrument( 120, "GUITAR_FRET_NOISE"     );
		setInstrument( 121, "BREATH_NOISE"          );
		setInstrument( 122, "SEASHORE"              );
		setInstrument( 123, "BIRD_TWEET"            );
		setInstrument( 124, "TELEPHONE_RING"        );
		setInstrument( 125, "HELICOPTER"            );
		setInstrument( 126, "APPLAUSE"              );
		setInstrument( 127, "GUNSHOT"               );
	}
	
	/**
	 * Creates german translations between note values and the corresponding
	 * instrument shortcuts.
	 */
	private static void initInstrumentsGerman1() {
		// TODO: translate
		// piano
		addInstrCategory( INSTR_CAT_PIANO );
		setInstrument( 0,   "ACOUSTIC_GRAND_PIANO"  );
		setInstrument( 1,   "BRIGHT_ACOUSTIC_PIANO" );
		setInstrument( 2,   "ELECTRIC_GRAND_PIANO"  );
		setInstrument( 3,   "HONKY_TONK_PIANO"      );
		setInstrument( 4,   "ELECTRIC_PIANO_1"      );
		setInstrument( 5,   "ELECTRIC_PIANO_2"      );
		setInstrument( 6,   "HARPSICHORD"           );
		setInstrument( 7,   "CLAVINET"              );
		
		// chromatic percussion
		addInstrCategory( INSTR_CAT_CHROM_PERC );
		setInstrument( 8,   "CELESTA"               );
		setInstrument( 9,   "GLOCKENSPIEL"          );
		setInstrument( 10,  "MUSIC_BOX"             );
		setInstrument( 11,  "VIBRAPHONE"            );
		setInstrument( 12,  "MARIMBA"               );
		setInstrument( 13,  "XYLOPHONE"             );
		setInstrument( 14,  "TUBULAR_BELL"          );
		setInstrument( 15,  "DULCIMER"              );
		
		// organ
		addInstrCategory( INSTR_CAT_ORGAN );
		setInstrument( 16,  "DRAWBAR_ORGAN"         );
		setInstrument( 17,  "PERCUSSIVE_ORGAN"      );
		setInstrument( 18,  "ROCK_ORGAN"            );
		setInstrument( 19,  "CHURCH_ORGAN"          );
		setInstrument( 20,  "REED_ORGAN"            );
		setInstrument( 21,  "ACCORDION"             );
		setInstrument( 22,  "HARMONICA"             );
		setInstrument( 23,  "TANGO_ACCORDION"       );
		
		// guitar
		addInstrCategory( INSTR_CAT_GUITAR );
		setInstrument( 24,  "NYLON_GUITAR"          );
		setInstrument( 25,  "STEEL_GUITAR"          );
		setInstrument( 26,  "E_GUITAR_JAZZ"         );
		setInstrument( 27,  "E_GUITAR_CLEAN"        );
		setInstrument( 28,  "E_GUITAR_MUTED"        );
		setInstrument( 29,  "OVERDRIVEN_GUITAR"     );
		setInstrument( 30,  "DISTORTION_GUITAR"     );
		setInstrument( 31,  "GUITAR_HARMONICS"      );
		
		// bass
		addInstrCategory( INSTR_CAT_BASS );
		setInstrument( 32,  "ACOUSTIC_BASS"         );
		setInstrument( 33,  "E_BASS_FINGER"         );
		setInstrument( 34,  "E_BASS_PICK"           );
		setInstrument( 35,  "FRETLESS_BASS"         );
		setInstrument( 36,  "SLAP_BASS_1"           );
		setInstrument( 37,  "SLAP_BASS_2"           );
		setInstrument( 38,  "SYNTH_BASS_1"          );
		setInstrument( 39,  "SYNTH_BASS_2"          );
		
		// strings
		addInstrCategory( INSTR_CAT_STRINGS );
		setInstrument( 40,  "VIOLIN"                );
		setInstrument( 41,  "VIOLA"                 );
		setInstrument( 42,  "CELLO"                 );
		setInstrument( 43,  "CONTRABASS"            );
		setInstrument( 44,  "TREMOLO_STRINGS"       );
		setInstrument( 45,  "PIZZACATO_STRINGS"     );
		setInstrument( 46,  "ORCHESTRAL_HARP"       );
		setInstrument( 47,  "TIMPANI"               );
		
		// ensemble
		addInstrCategory( INSTR_CAT_ENSEMBLE );
		setInstrument( 48,  "STRING_ENSEMBLE_1"     );
		setInstrument( 49,  "STRING_ENSEMBLE_2"     );
		setInstrument( 50,  "SYNTH_STRINGS_1"       );
		setInstrument( 51,  "SYNTH_STRINGS_2"       );
		setInstrument( 52,  "CHOIR_AAHS"            );
		setInstrument( 53,  "VOICE_OOHS"            );
		setInstrument( 54,  "SYNTH_CHOIR"           );
		setInstrument( 55,  "ORCHESTRA_HIT"         );
		
		// brass
		addInstrCategory( INSTR_CAT_BRASS );
		setInstrument( 56,  "TRUMPET"               );
		setInstrument( 57,  "TROMBONE"              );
		setInstrument( 58,  "TUBA"                  );
		setInstrument( 59,  "MUTED_TRUMPET"         );
		setInstrument( 60,  "FRENCH_HORN"           );
		setInstrument( 61,  "BRASS_SECTION"         );
		setInstrument( 62,  "SYNTH_BRASS_1"         );
		setInstrument( 63,  "SYNTH_BRASS_2"         );
		
		// reed
		addInstrCategory( INSTR_CAT_REED );
		setInstrument( 64,  "SOPRANO_SAX"           );
		setInstrument( 65,  "ALTO_SAX"              );
		setInstrument( 66,  "TENOR_SAX"             );
		setInstrument( 67,  "BARITONE_SAX"          );
		setInstrument( 68,  "OBOE"                  );
		setInstrument( 69,  "ENGLISH_HORN"          );
		setInstrument( 70,  "BASSOON"               );
		setInstrument( 71,  "CLARINET"              );
		
		// pipe
		addInstrCategory( INSTR_CAT_PIPE );
		setInstrument( 72,  "PICCOLO"               );
		setInstrument( 73,  "FLUTE"                 );
		setInstrument( 74,  "RECORDER"              );
		setInstrument( 75,  "PAN_FLUTE"             );
		setInstrument( 76,  "BLOWN_BOTTLE"          );
		setInstrument( 77,  "SHAKUHACHI"            );
		setInstrument( 78,  "WHISTLE"               );
		setInstrument( 79,  "OCARINA"               );
		
		// synth lead
		addInstrCategory( INSTR_CAT_SYNTH_LEAD );
		setInstrument( 80,  "LEAD_SQUARE"          );
		setInstrument( 81,  "LEAD_SAWTOOTH"        );
		setInstrument( 82,  "LEAD_CALLIOPE"        );
		setInstrument( 83,  "LEAD_CHIFF"           );
		setInstrument( 84,  "LEAD_CHARANGO"        );
		setInstrument( 85,  "LEAD_VOICE"           );
		setInstrument( 86,  "LEAD_FIFTHS"          );
		setInstrument( 87,  "LEAD_BASS_LEAD"       );
		
		// synth pad
		addInstrCategory( INSTR_CAT_SYNTH_PAD );
		setInstrument( 88,  "PAD_NEW_AGE"           );
		setInstrument( 89,  "PAD_WARM"              );
		setInstrument( 90,  "PAD_POLYSYNTH"         );
		setInstrument( 91,  "PAD_CHOIR"             );
		setInstrument( 92,  "PAD_POWED"             );
		setInstrument( 93,  "PAD_METALLIC"          );
		setInstrument( 94,  "PAD_HALO"              );
		setInstrument( 95,  "PAD_SWEEP"             );
		
		// synth effects
		addInstrCategory( INSTR_CAT_SYNTH_EFFECTS );
		setInstrument( 96,  "FX_RAIN"               );
		setInstrument( 97,  "FX_SOUNDTRACK"         );
		setInstrument( 98,  "FX_CRYSTAL"            );
		setInstrument( 99,  "FX_ATMOSPHERE"         );
		setInstrument( 100, "FX_BRIGHTNESS"         );
		setInstrument( 101, "FX_GOBLINS"            );
		setInstrument( 102, "FX_ECHOES"             );
		setInstrument( 103, "FX_SCI_FI"             );
		
		// ethnic
		addInstrCategory( INSTR_CAT_ETHNIC );
		setInstrument( 104, "SITAR"                 );
		setInstrument( 105, "BANJO"                 );
		setInstrument( 106, "SHAMISEN"              );
		setInstrument( 107, "KOTO"                  );
		setInstrument( 108, "KALIMBA"               );
		setInstrument( 109, "BAG_PIPE"              );
		setInstrument( 110, "FIDDLE"                );
		setInstrument( 111, "SHANAI"                );
		
		// percussive
		addInstrCategory( INSTR_CAT_PERCUSSIVE );
		setInstrument( 112, "TINKLE_BELL"           );
		setInstrument( 113, "AGOGO"                 );
		setInstrument( 114, "STEEL_DRUMS"           );
		setInstrument( 115, "WOODBLOCK"             );
		setInstrument( 116, "TAIKO_DRUM"            );
		setInstrument( 117, "MELODIC_TOM"           );
		setInstrument( 118, "SYNTH_DRUM"            );
		setInstrument( 119, "REVERSE_CYMBAL"        );
		
		// sound effects
		addInstrCategory( INSTR_CAT_SOUND_EFFECTS );
		setInstrument( 120, "GUITAR_FRET_NOISE"     );
		setInstrument( 121, "BREATH_NOISE"          );
		setInstrument( 122, "SEASHORE"              );
		setInstrument( 123, "BIRD_TWEET"            );
		setInstrument( 124, "TELEPHONE_RING"        );
		setInstrument( 125, "HELICOPTER"            );
		setInstrument( 126, "APPLAUSE"              );
		setInstrument( 127, "GUNSHOT"               );
	}
	
	/**
	 * Sets the translation for one specific instrument.
	 * Also makes the instrument name available for the info view and for the
	 * soundcheck.
	 * 
	 * @param number  instrument number as it is defined by MIDI
	 * @param name    name of the instrument as it is used by the Midica user
	 */
	private static void setInstrument( int number, String name ) {
		instrIntToName.put( number, name ); // TODO: check if this is needed
		instrNameToInt.put( name, number );
		
		// prepare for the info view
		InstrumentElement elem = new InstrumentElement( number, name, false );
		instrumentList.add( elem );
	}
	
	/**
	 * Adds an instrument category to the list of instruments used by
	 * info view and soundcheck.
	 * 
	 * @param categoryId  identifier of the category to be added
	 */
	private static void addInstrCategory( String categoryId ) {
		String categoryName = get( categoryId );
		InstrumentElement elem = new InstrumentElement( -1, categoryName, true );
		instrumentList.add( elem );
	}
	
	/**
	 * Creates an association between a translation identifier and the
	 * corresponding language translation.
	 * 
	 * @param id           identifier of the translation
	 * @param translation  translated text corresponding to the identifier
	 */
	private static void set( String id, String translation ) {
		dictionary.put( id, translation );
	}
	
	/**
	 * Returns the language translation of the given identifier.
	 * If there is no corresponding translation: returns the given identifier, surrounded
	 * by `[[` and `]]`.
	 * This makes it easier to identify translation problems or typos.
	 * 
	 * @param key  identifier of the requested translation
	 * @return     language translation corresponding to the given identifier
	 */
	public static String get( String key ) {
		if ( dictionary.containsKey(key) )
			return dictionary.get(key);
		else
			return "[[" + key + "]]";
	}
	
	/**
	 * Returns a note value by note name.
	 * 
	 * @param name  note name to identify the requested note
	 * @return      value of the requested note like it is defined in the MIDI specification
	 */
	public static int getNote( String name ) {
		if ( noteNameToInt.containsKey(name) )
			return noteNameToInt.get( name );
		else
			return UNKNOWN_CODE;
	}
	
	/**
	 * Returns a note name by note value.
	 * 
	 * @param i  value of the requested note like it is defined in the MIDI specification
	 * @return   currently configured name for the requested note
	 */
	public static String getNote( int i ) {
		if ( noteIntToName.containsKey(i) )
			return noteIntToName.get( i );
		else
			return get( UNKNOWN_NOTE_NAME );
	}
	
	/**
	 * Returns an instrument value by name.
	 * 
	 * @param name  currently configured name for the instrument
	 * @return      value for the instrument as defined by the MIDI specification
	 */
	public static int getInstrument( String name ) {
		if ( instrNameToInt.containsKey(name) )
			return instrNameToInt.get( name );
		else
			return UNKNOWN_CODE;
	}
	
	/**
	 * Returns an instrument name by value.
	 * 
	 * @param i  instrument value as defined by the MIDI specification
	 * @return   currently configured name for the requested instrument
	 */
	public static String getInstrument( int i ) {
		if ( instrIntToName.containsKey(i) )
			return instrIntToName.get( i );
		else
			return get( UNKNOWN_INSTRUMENT );
	}
	
	/**
	 * Indicates if the given name is a currently configured note name.
	 * 
	 * @param name  note name to be checked
	 * @return      true, if the note name is configured -- otherwise: false
	 */
	public static boolean noteExists( String name ) {
		if ( noteNameToInt.containsKey(name) )
			return true;
		return false;
	}
	
	/**
	 * Returns a percussion instrument value by name.
	 * 
	 * @param name  shortcut of the percussion instrument
	 * @return      instrument value as defined by the MIDI specification
	 */
	public static int getPercussion( String name ) {
		if ( percussionNameToInt.containsKey(name) )
			return percussionNameToInt.get( name );
		else
			return UNKNOWN_CODE;
	}
	
	/**
	 * Returns a percussion instrument shortcut by value.
	 * 
	 * @param i  instrument value as defined by the MIDI specification
	 * @return   currently configured percussion shortcut for the requested
	 *           percussion instrument
	 */
	public static String getPercussion( int i ) {
		if ( percussionIntToName.containsKey(i) )
			return percussionIntToName.get( i );
		else
			return get( UNKNOWN_PERCUSSION_NAME );
	}
	
	/**
	 * Returns a drumkit number by name.
	 * 
	 * @param name  drumkit shortcut
	 * @return      drumkit number as defined by the General Standard specification
	 */
	public static int getDrumkit( String name ) {
		if ( drumkitNameToInt.containsKey(name) )
			return drumkitNameToInt.get( name );
		else
			return UNKNOWN_CODE;
	}
	
	/**
	 * Returns a drumkit shortcut by value.
	 * 
	 * @param i  drumkit value as defined by the General Standard specification
	 * @return
	 */
	public static String getDrumkit( int i ) {
		if ( drumkitIntToName.containsKey(i) )
			return drumkitIntToName.get( i );
		else
			return get( UNKNOWN_DRUMKIT_NAME );
	}
	
	/**
	 * Indicates if the given name is a currently configured percussion shortcut.
	 * 
	 * @param name  percussion name to be checked
	 * @return      true, if the percussion name is configured -- otherwise: false
	 */
	public static boolean percussionExists( String name ) {
		if ( percussionNameToInt.containsKey(name) )
			return true;
		return false;
	}
	
	/**
	 * Returns a specific MidicaPL keyword or symbol.
	 * 
	 * @param id  identifier for the syntax element
	 * @return    currently configured syntax keyword
	 */
	public static String getSyntax( String id ) {
		if ( syntax.containsKey(id) )
			return syntax.get( id );
		else
			return UNKNOWN_SYNTAX;
	}
	
	/**
	 * Counts the currently configured note names.
	 * 
	 * @return number of currently configured note names
	 */
	public static int countNotes() {
		return noteIntToName.size();
	}
	
	/**
	 * Counts the currently configured percussion shortcuts.
	 * 
	 * @return number of currently configured percussion shortcuts
	 */
	public static int countPercussion() {
		return percussionIntToName.size();
	}
	
	/**
	 * Counts the currently configured instrument names.
	 * 
	 * @return number of currently configured instrument names
	 */
	public static int countInstruments() {
		return instrIntToName.size();
	}
	
	/**
	 * Returns all currently configured percussion shortcuts.
	 * 
	 * @return all currently configured percussion shortcuts
	 */
	public static Set<Integer> getPercussionNotes() {
		return percussionIntToName.keySet();
	}
	
	/**
	 * Returns all currently configured syntax elements including category entries.
	 * 
	 * @return all currently configured syntax elements
	 */
	public static ArrayList<SyntaxElement> getSyntaxList() {
		return syntaxList;
	}
	
	/**
	 * Returns all currently configured instruments including category entries.
	 * 
	 * @return all currently configured instruments
	 */
	public static ArrayList<InstrumentElement> getInstrumentList() {
		return instrumentList;
	}
	
	/**
	 * Returns all configured drumkit numbers.
	 * 
	 * @return all configured drumkit numbers
	 */
	public static Set<Integer> getDrumkitList() {
		return drumkitIntToName.keySet();
	}
	
	/**
	 * Returns two strings indicating if the chosen configuration is OK.
	 * 
	 * The first string is language independent and indicates if an error occurred at all.
	 * The second string is the language-dependent error or success message.
	 * 
	 * @return strings describing the configuration error
	 */
	public static String[] getConfigFeedback() {
		String[] feedback = new String[ 2 ];
		feedback[ 0 ] = CONF_ERROR_OK;
		feedback[ 1 ] = get( CONF_ERROR_OK );
		
		if ( Config.CBX_HALFTONE_ID_SHARP.equals(Config.get(Config.HALF_TONE)) ) {
			if ( Config.CBX_SYNTAX_PROG_2.equals(Config.get(Config.SYNTAX)) ) {
				feedback[ 0 ] = CONF_ERROR_ERROR;
				feedback[ 1 ] = get( CONF_ERROR_HALFTONE_SYNTAX );
			}
		}
		
		return feedback;
	}
}
