/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.midica.Midica;
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
 * - Percussion IDs
 * - Instrument IDs
 * 
 * It defines different types of translation keys as public member variables.
 * It also contains methods to set and return values of a certain translation type.
 * These methods are called with the corresponding member variables in order to identify the
 * right element to be set or returned.
 * 
 * The member variables' beginning depends on the type of translation they are used for:
 * 
 * - **SYNTAX_** - Keys beginning with this string identify keys for MidicaPL syntax keywords.
 * - **PERCUSSION_** - These keys identify IDs for percussion instruments.
 * - **INSTR_** - These keys identify instrument IDs.
 * - **TT_KEY_** - These keys identify key binding types.
 * - **KEY_** - These keys identify key bindings for a certain widget or target.
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
	private static HashMap<String, String>      dictionary             = null;
	private static HashMap<String, Integer>     noteNameToInt          = null;
	private static HashMap<Integer, String>     noteIntToName          = null;
	private static HashMap<String, Integer>     percussionIdToInt      = null;
	private static HashMap<Integer, String>     percussionIntToLongId  = null;
	private static HashMap<Integer, String>     percussionIntToShortId = null;
	private static HashMap<String, Integer>     drumkitNameToInt       = null;
	private static HashMap<Integer, String>     drumkitIntToName       = null;
	private static HashMap<String, String>      syntax                 = null;
	private static HashMap<String, Integer>     instrNameToInt         = null;
	private static HashMap<Integer, String>     instrIntToName         = null;
	private static ArrayList<SyntaxElement>     syntaxList             = null;
	private static ArrayList<InstrumentElement> instrumentList         = null;
	private static ArrayList<InstrumentElement> drumkitList            = null;
	private static ArrayList<String>            keyBindingCategories   = null;
	
	// needed to build up the note dictionaries (noteNameToInt and noteIntToName)
	private static String[]                            notes            = new String[12];
	private static byte[]                              halfTones        = null;
	private static boolean[]                           isHalfTone       = null;
	private static HashMap<String, Integer>            moreNotes        = null;
	private static TreeMap<Integer, ArrayList<String>> moreBaseNotes    = null;
	private static TreeMap<Integer, ArrayList<String>> moreBaseNotesPos = null;
	private static ArrayList<TreeSet<String>>          moreNotesByNum   = null;
	
	// syntax
	public static final String SYNTAX_DEFINE             = "DEFINE";
	public static final String SYNTAX_COMMENT            = "COMMENT";
	public static final String SYNTAX_CONST              = "CONST";
	public static final String SYNTAX_VAR                = "VAR";
	public static final String SYNTAX_VAR_SYMBOL         = "VAR_SYMBOL";
	public static final String SYNTAX_VAR_ASSIGNER       = "VAR_ASSIGNER";
	public static final String SYNTAX_PARAM_NAMED_OPEN   = "PARAM_NAMED_OPEN";
	public static final String SYNTAX_PARAM_NAMED_CLOSE  = "PARAM_NAMED_CLOSE";
	public static final String SYNTAX_PARAM_INDEX_OPEN   = "PARAM_INDEX_OPEN";
	public static final String SYNTAX_PARAM_INDEX_CLOSE  = "PARAM_INDEX_CLOSE";
	public static final String SYNTAX_GLOBAL             = "GLOBAL";
	public static final String SYNTAX_P                  = "PERCUSSION_CHANNEL";
	public static final String SYNTAX_END                = "END";
	public static final String SYNTAX_BLOCK_OPEN         = "BLOCK_OPEN";
	public static final String SYNTAX_BLOCK_CLOSE        = "BLOCK_CLOSE";
	public static final String SYNTAX_FUNCTION           = "FUNCTION";
	public static final String SYNTAX_PATTERN            = "PATTERN";
	public static final String SYNTAX_PATTERN_INDEX_SEP  = "SYNTAX_PATTERN_INDEX_SEP";
	public static final String SYNTAX_PARAM_OPEN         = "PARAM_OPEN";
	public static final String SYNTAX_PARAM_CLOSE        = "PARAM_CLOSE";
	public static final String SYNTAX_PARAM_SEPARATOR    = "PARAM_SEPARATOR";
	public static final String SYNTAX_PARAM_ASSIGNER     = "PARAM_ASSIGNER";
	public static final String SYNTAX_CALL               = "CALL";
	public static final String SYNTAX_INSTRUMENT         = "INSTRUMENT";
	public static final String SYNTAX_INSTRUMENTS        = "INSTRUMENTS";
	public static final String SYNTAX_META               = "META";
	public static final String SYNTAX_META_COPYRIGHT     = "META_COPYRIGHT";
	public static final String SYNTAX_META_TITLE         = "META_TITLE";
	public static final String SYNTAX_META_COMPOSER      = "META_COMPOSER";
	public static final String SYNTAX_META_LYRICIST      = "META_LYRICIST";
	public static final String SYNTAX_META_ARTIST        = "META_ARTIST";
	public static final String SYNTAX_META_SOFT_KARAOKE  = "META_SOFT_KARAOKE";
	public static final String SYNTAX_META_SK_VERSION    = "META_SK_VERSION";
	public static final String SYNTAX_META_SK_LANG       = "META_SK_LANG";
	public static final String SYNTAX_META_SK_TITLE      = "META_SK_TITLE";
	public static final String SYNTAX_META_SK_AUTHOR     = "META_SK_AUTHOR";
	public static final String SYNTAX_META_SK_COPYRIGHT  = "META_SK_COPYRIGHT";
	public static final String SYNTAX_META_SK_INFO       = "META_SK_INFO";
	public static final String SYNTAX_TEMPO              = "TEMPO";
	public static final String SYNTAX_TIME_SIG           = "TIME_SIG";
	public static final String SYNTAX_TIME_SIG_SLASH     = "TIME_SIG_SLASH";
	public static final String SYNTAX_KEY_SIG            = "KEY_SIG_SIG";
	public static final String SYNTAX_KEY_SEPARATOR      = "KEY_SIG_SEPARATOR";
	public static final String SYNTAX_KEY_MAJ            = "KEY_SIG_MAJ";
	public static final String SYNTAX_KEY_MIN            = "KEY_SIG_MIN";
	public static final String SYNTAX_PARTIAL_SYNC_RANGE = "PARTIAL_SYNC_RANGE";
	public static final String SYNTAX_PARTIAL_SYNC_SEP   = "PARTIAL_SYNC_SEP";
	public static final String SYNTAX_OPT_SEPARATOR      = "OPT_SEPARATOR";
	public static final String SYNTAX_OPT_ASSIGNER       = "OPT_ASSIGNER";
	public static final String SYNTAX_PROG_BANK_SEP      = "PROGRAM_BANK_SEPARATOR";
	public static final String SYNTAX_BANK_SEP           = "BANK_SEPARATOR";
	public static final String SYNTAX_VELOCITY           = "VELOCITY";
	public static final String SYNTAX_V                  = "VELOCITY_SHORT";
	public static final String SYNTAX_DURATION           = "DURATION";
	public static final String SYNTAX_D                  = "DURATION_SHORT";
	public static final String SYNTAX_DURATION_PERCENT   = "DURATION_PERCENT";
	public static final String SYNTAX_MULTIPLE           = "MULTIPLE";
	public static final String SYNTAX_M                  = "MULTIPLE_SHORT";
	public static final String SYNTAX_QUANTITY           = "QUANTITY";
	public static final String SYNTAX_Q                  = "QUANTITY_SHORT";
	public static final String SYNTAX_LYRICS             = "LYRICS";
	public static final String SYNTAX_L                  = "LYRICS_SHORT";
	public static final String SYNTAX_LYRICS_SPACE       = "LYRICS_SPACE";
	public static final String SYNTAX_LYRICS_CR          = "LYRICS_CR";
	public static final String SYNTAX_LYRICS_LF          = "LYRICS_LF";
	public static final String SYNTAX_LYRICS_COMMA       = "LYRICS_COMMA";
	public static final String SYNTAX_TUPLET             = "TUPLET";
	public static final String SYNTAX_T                  = "TUPLET_SHORT";
	public static final String SYNTAX_TREMOLO            = "TREMOLO";
	public static final String SYNTAX_TR                 = "TREMOLO_SHORT";
	public static final String SYNTAX_SHIFT              = "SHIFT";
	public static final String SYNTAX_S                  = "SHIFT_SHORT";
	public static final String SYNTAX_IF                 = "IF";
	public static final String SYNTAX_ELSIF              = "ELSIF";
	public static final String SYNTAX_ELSE               = "ELSE";
	public static final String SYNTAX_COND_EQ            = "COND_EQ";
	public static final String SYNTAX_COND_NEQ           = "COND_NEQ";
	public static final String SYNTAX_COND_NDEF          = "COND_NDEF";
	public static final String SYNTAX_COND_LT            = "COND_LT";
	public static final String SYNTAX_COND_LE            = "COND_LE";
	public static final String SYNTAX_COND_GT            = "COND_GT";
	public static final String SYNTAX_COND_GE            = "COND_GE";
	public static final String SYNTAX_COND_IN            = "COND_IN";
	public static final String SYNTAX_COND_IN_SEP        = "COND_IN_SEP";
	public static final String SYNTAX_REST               = "REST";
	public static final String SYNTAX_CHORD              = "CHORD";
	public static final String SYNTAX_CHORD_ASSIGNER     = "CHORD_ASSIGNER";
	public static final String SYNTAX_CHORD_SEPARATOR    = "CHORD_SEPARATOR";
	public static final String SYNTAX_INCLUDE            = "INCLUDE";
	public static final String SYNTAX_SOUNDBANK          = "SOUNDBANK";
	public static final String SYNTAX_SOUNDFONT          = "SOUNDFONT";
	public static final String SYNTAX_ZEROLENGTH         = "LENGTH_ZERO";
	public static final String SYNTAX_32                 = "LENGTH_32";
	public static final String SYNTAX_16                 = "LENGTH_16";
	public static final String SYNTAX_8                  = "LENGTH_8";
	public static final String SYNTAX_4                  = "LENGTH_4";
	public static final String SYNTAX_2                  = "LENGTH_2";
	public static final String SYNTAX_1                  = "LENGTH_1";
	public static final String SYNTAX_M1                 = "LENGTH_M1";
	public static final String SYNTAX_M2                 = "LENGTH_M2";
	public static final String SYNTAX_M4                 = "LENGTH_M4";
	public static final String SYNTAX_M8                 = "LENGTH_M8";
	public static final String SYNTAX_M16                = "LENGTH_M16";
	public static final String SYNTAX_M32                = "LENGTH_M32";
	public static final String SYNTAX_DOT                = "DOT";
	public static final String SYNTAX_TRIPLET            = "TRIPLET";
	public static final String SYNTAX_TUPLET_INTRO       = "TUPLET_INTRO";
	public static final String SYNTAX_TUPLET_FOR         = "TUPLET_FOR";
	public static final String SYNTAX_LENGTH_PLUS        = "LENGTH_PLUS";
	
	// TODO: use for the syntax or delete
	// drumkit identifiers
	public static final String DRUMKIT_STANDARD   = "drumkit_0";
	public static final String DRUMKIT_ROOM       = "drumkit_8";
	public static final String DRUMKIT_POWER      = "drumkit_16";
	public static final String DRUMKIT_ELECTRONIC = "drumkit_24";
	public static final String DRUMKIT_TR808      = "drumkit_25"; // TODO: rename to ANALOG (see gm, page 33)
	public static final String DRUMKIT_JAZZ       = "drumkit_32";
	public static final String DRUMKIT_BRUSH      = "drumkit_40";
	public static final String DRUMKIT_ORCHESTRA  = "drumkit_48";
	public static final String DRUMKIT_SOUND_FX   = "drumkit_56";
	public static final String DRUMKIT_CM64_CM32  = "drumkit_127";
	
	// percussion identifiers (long)
	private static final String PERCUSSION_HIGH_Q          = "percussion_27";
	private static final String PERCUSSION_SLAP            = "percussion_28";
	private static final String PERCUSSION_SCRATCH_PUSH    = "percussion_29";
	private static final String PERCUSSION_SCRATCH_PULL    = "percussion_30";
	private static final String PERCUSSION_STICKS          = "percussion_31";
	private static final String PERCUSSION_SQUARE_CLICK    = "percussion_32";
	private static final String PERCUSSION_METRONOME_CLICK = "percussion_33";
	private static final String PERCUSSION_METRONOME_BELL  = "percussion_34";
	private static final String PERCUSSION_BASS_DRUM_2     = "percussion_35";
	private static final String PERCUSSION_BASS_DRUM_1     = "percussion_36";
	private static final String PERCUSSION_RIM_SHOT        = "percussion_37";
	private static final String PERCUSSION_SNARE_DRUM_1    = "percussion_38";
	private static final String PERCUSSION_HAND_CLAP       = "percussion_39";
	private static final String PERCUSSION_SNARE_DRUM_2    = "percussion_40";
	private static final String PERCUSSION_TOM_1           = "percussion_41";
	private static final String PERCUSSION_HI_HAT_CLOSED   = "percussion_42";
	private static final String PERCUSSION_TOM_2           = "percussion_43";
	private static final String PERCUSSION_HI_HAT_PEDAL    = "percussion_44";
	private static final String PERCUSSION_TOM_3           = "percussion_45";
	private static final String PERCUSSION_HI_HAT_OPEN     = "percussion_46";
	private static final String PERCUSSION_TOM_4           = "percussion_47";
	private static final String PERCUSSION_TOM_5           = "percussion_48";
	private static final String PERCUSSION_CRASH_CYMBAL_1  = "percussion_49";
	private static final String PERCUSSION_TOM_6           = "percussion_50";
	private static final String PERCUSSION_RIDE_CYMBAL_1   = "percussion_51";
	private static final String PERCUSSION_CHINESE_CYMBAL  = "percussion_52";
	private static final String PERCUSSION_RIDE_BELL       = "percussion_53";
	private static final String PERCUSSION_TAMBOURINE      = "percussion_54";
	private static final String PERCUSSION_SPLASH_CYMBAL   = "percussion_55";
	private static final String PERCUSSION_COWBELL         = "percussion_56";
	private static final String PERCUSSION_CRASH_CYMBAL_2  = "percussion_57";
	private static final String PERCUSSION_VIBRA_SLAP      = "percussion_58";
	private static final String PERCUSSION_RIDE_CYMBAL_2   = "percussion_59";
	private static final String PERCUSSION_BONGO_HIGH      = "percussion_60";
	private static final String PERCUSSION_BONGO_LOW       = "percussion_61";
	private static final String PERCUSSION_CONGA_MUTE      = "percussion_62";
	private static final String PERCUSSION_CONGA_OPEN      = "percussion_63";
	private static final String PERCUSSION_CONGA_LOW       = "percussion_64";
	private static final String PERCUSSION_TIMBALES_HIGH   = "percussion_65";
	private static final String PERCUSSION_TIMBALES_LOW    = "percussion_66";
	private static final String PERCUSSION_AGOGO_HIGH      = "percussion_67";
	private static final String PERCUSSION_AGOGO_LOW       = "percussion_68";
	private static final String PERCUSSION_CABASA          = "percussion_69";
	private static final String PERCUSSION_MARACAS         = "percussion_70";
	private static final String PERCUSSION_WHISTLE_SHORT   = "percussion_71";
	private static final String PERCUSSION_WHISTLE_LONG    = "percussion_72";
	private static final String PERCUSSION_GUIRO_SHORT     = "percussion_73";
	private static final String PERCUSSION_GUIRO_LONG      = "percussion_74";
	private static final String PERCUSSION_CLAVE           = "percussion_75";
	private static final String PERCUSSION_WOOD_BLOCK_HIGH = "percussion_76";
	private static final String PERCUSSION_WOOD_BLOCK_LOW  = "percussion_77";
	private static final String PERCUSSION_CUICA_MUTE      = "percussion_78";
	private static final String PERCUSSION_CUICA_OPEN      = "percussion_79";
	private static final String PERCUSSION_TRIANGLE_MUTE   = "percussion_80";
	private static final String PERCUSSION_TRIANGLE_OPEN   = "percussion_81";
	private static final String PERCUSSION_SHAKER          = "percussion_82";
	private static final String PERCUSSION_JINGLE_BELL     = "percussion_83";
	private static final String PERCUSSION_BELLTREE        = "percussion_84";
	private static final String PERCUSSION_CASTANETS       = "percussion_85";
	private static final String PERCUSSION_SURDO_MUTE      = "percussion_86";
	private static final String PERCUSSION_SURDO_OPEN      = "percussion_87";
	
	// percussion identifiers (short)
	private static final String PERCUSSION_SHORT_HIGH_Q          = "percussion_short_27";
	private static final String PERCUSSION_SHORT_SLAP            = "percussion_short_28";
	private static final String PERCUSSION_SHORT_SCRATCH_PUSH    = "percussion_short_29";
	private static final String PERCUSSION_SHORT_SCRATCH_PULL    = "percussion_short_30";
	private static final String PERCUSSION_SHORT_STICKS          = "percussion_short_31";
	private static final String PERCUSSION_SHORT_SQUARE_CLICK    = "percussion_short_32";
	private static final String PERCUSSION_SHORT_METRONOME_CLICK = "percussion_short_33";
	private static final String PERCUSSION_SHORT_METRONOME_BELL  = "percussion_short_34";
	private static final String PERCUSSION_SHORT_BASS_DRUM_2     = "percussion_short_35";
	private static final String PERCUSSION_SHORT_BASS_DRUM_1     = "percussion_short_36";
	private static final String PERCUSSION_SHORT_RIM_SHOT        = "percussion_short_37";
	private static final String PERCUSSION_SHORT_SNARE_DRUM_1    = "percussion_short_38";
	private static final String PERCUSSION_SHORT_HAND_CLAP       = "percussion_short_39";
	private static final String PERCUSSION_SHORT_SNARE_DRUM_2    = "percussion_short_40";
	private static final String PERCUSSION_SHORT_TOM_1           = "percussion_short_41";
	private static final String PERCUSSION_SHORT_HI_HAT_CLOSED   = "percussion_short_42";
	private static final String PERCUSSION_SHORT_TOM_2           = "percussion_short_43";
	private static final String PERCUSSION_SHORT_HI_HAT_PEDAL    = "percussion_short_44";
	private static final String PERCUSSION_SHORT_TOM_3           = "percussion_short_45";
	private static final String PERCUSSION_SHORT_HI_HAT_OPEN     = "percussion_short_46";
	private static final String PERCUSSION_SHORT_TOM_4           = "percussion_short_47";
	private static final String PERCUSSION_SHORT_TOM_5           = "percussion_short_48";
	private static final String PERCUSSION_SHORT_CRASH_CYMBAL_1  = "percussion_short_49";
	private static final String PERCUSSION_SHORT_TOM_6           = "percussion_short_50";
	private static final String PERCUSSION_SHORT_RIDE_CYMBAL_1   = "percussion_short_51";
	private static final String PERCUSSION_SHORT_CHINESE_CYMBAL  = "percussion_short_52";
	private static final String PERCUSSION_SHORT_RIDE_BELL       = "percussion_short_53";
	private static final String PERCUSSION_SHORT_TAMBOURINE      = "percussion_short_54";
	private static final String PERCUSSION_SHORT_SPLASH_CYMBAL   = "percussion_short_55";
	private static final String PERCUSSION_SHORT_COWBELL         = "percussion_short_56";
	private static final String PERCUSSION_SHORT_CRASH_CYMBAL_2  = "percussion_short_57";
	private static final String PERCUSSION_SHORT_VIBRA_SLAP      = "percussion_short_58";
	private static final String PERCUSSION_SHORT_RIDE_CYMBAL_2   = "percussion_short_59";
	private static final String PERCUSSION_SHORT_BONGO_HIGH      = "percussion_short_60";
	private static final String PERCUSSION_SHORT_BONGO_LOW       = "percussion_short_61";
	private static final String PERCUSSION_SHORT_CONGA_MUTE      = "percussion_short_62";
	private static final String PERCUSSION_SHORT_CONGA_OPEN      = "percussion_short_63";
	private static final String PERCUSSION_SHORT_CONGA_LOW       = "percussion_short_64";
	private static final String PERCUSSION_SHORT_TIMBALES_HIGH   = "percussion_short_65";
	private static final String PERCUSSION_SHORT_TIMBALES_LOW    = "percussion_short_66";
	private static final String PERCUSSION_SHORT_AGOGO_HIGH      = "percussion_short_67";
	private static final String PERCUSSION_SHORT_AGOGO_LOW       = "percussion_short_68";
	private static final String PERCUSSION_SHORT_CABASA          = "percussion_short_69";
	private static final String PERCUSSION_SHORT_MARACAS         = "percussion_short_70";
	private static final String PERCUSSION_SHORT_WHISTLE_SHORT   = "percussion_short_71";
	private static final String PERCUSSION_SHORT_WHISTLE_LONG    = "percussion_short_72";
	private static final String PERCUSSION_SHORT_GUIRO_SHORT     = "percussion_short_73";
	private static final String PERCUSSION_SHORT_GUIRO_LONG      = "percussion_short_74";
	private static final String PERCUSSION_SHORT_CLAVE           = "percussion_short_75";
	private static final String PERCUSSION_SHORT_WOOD_BLOCK_HIGH = "percussion_short_76";
	private static final String PERCUSSION_SHORT_WOOD_BLOCK_LOW  = "percussion_short_77";
	private static final String PERCUSSION_SHORT_CUICA_MUTE      = "percussion_short_78";
	private static final String PERCUSSION_SHORT_CUICA_OPEN      = "percussion_short_79";
	private static final String PERCUSSION_SHORT_TRIANGLE_MUTE   = "percussion_short_80";
	private static final String PERCUSSION_SHORT_TRIANGLE_OPEN   = "percussion_short_81";
	private static final String PERCUSSION_SHORT_SHAKER          = "percussion_short_82";
	private static final String PERCUSSION_SHORT_JINGLE_BELL     = "percussion_short_83";
	private static final String PERCUSSION_SHORT_BELLTREE        = "percussion_short_84";
	private static final String PERCUSSION_SHORT_CASTANETS       = "percussion_short_85";
	private static final String PERCUSSION_SHORT_SURDO_MUTE      = "percussion_short_86";
	private static final String PERCUSSION_SHORT_SURDO_OPEN      = "percussion_short_87";
	
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
	public static final String INSTR_PIZZICATO_STRINGS     = "instr_45";
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
	
	// key bindings
	public static final String WARNING_KEY_BINDING_CONFLICT = "warning_key_binding_conflict";
	public static final String TT_KEY_NOT_CONFIGURED        = "tt_key_not_configured";
	public static final String TT_KEY_BUTTON_PRESS          = "tt_key_button_press";
	public static final String TT_KEY_TXT_FLD_FOCUS         = "tt_key_txt_fld_focus";
	public static final String TT_KEY_TXT_AREA_FOCUS        = "tt_key_txt_area_focus";
	public static final String TT_KEY_SLD_FOCUS             = "tt_key_sld_focus";
	public static final String TT_KEY_TREE_FOCUS            = "tt_key_tree_focus";
	public static final String TT_KEY_TABLE_FOCUS           = "tt_key_table_focus";
	public static final String TT_KEY_LIST_FOCUS            = "tt_key_list_focus";
	public static final String TT_KEY_CBX_TOGGLE            = "tt_key_cbx_toggle";
	public static final String TT_KEY_CBX_OPEN              = "tt_key_cbx_open";
	public static final String TT_KEY_CBX_CHANNEL_SELECT    = "tt_key_cbx_channel_select";
	public static final String TT_KEY_CBX_SELECT_CHANNEL_N  = "tt_key_cbx_select_channel_n";
	public static final String TT_KEY_TAB_SELECT            = "tt_key_tab_select";
	public static final String TT_KEY_OPEN_ICON             = "tt_key_open_icon";
	
	// key binding categories (windows)
	public static final String KEYCAT_MAIN                  = "key_main";
	public static final String KEYCAT_PLAYER                = "key_player";
	public static final String KEYCAT_SOUNDCHECK            = "key_soundcheck";
	public static final String KEYCAT_INFO                  = "key_info";
	public static final String KEYCAT_MSG                   = "key_msg";
	public static final String KEYCAT_FILE_SELECT           = "key_file_select";
	public static final String KEYCAT_FILE_CONF             = "key_file_conf";
	public static final String KEYCAT_DC_CONF               = "key_dc_conf";
	public static final String KEYCAT_AU_CONF               = "key_au_conf";
	public static final String KEYCAT_EXPORT_RESULT         = "key_export_result";
	public static final String KEYCAT_STRING_FILTER         = "key_string_filter";
	
	// key binding IDs
	public static final String KEY_MAIN_INFO                    = "key_main_info";
	public static final String KEY_MAIN_PLAYER                  = "key_main_player";
	public static final String KEY_MAIN_IMPORT                  = "key_main_import";
	public static final String KEY_MAIN_IMPORT_SB               = "key_main_import_sb";
	public static final String KEY_MAIN_EXPORT                  = "key_main_export";
	public static final String KEY_MAIN_CBX_LANGUAGE            = "key_main_cbx_language";
	public static final String KEY_MAIN_CBX_NOTE                = "key_main_cbx_note";
	public static final String KEY_MAIN_CBX_HALFTONE            = "key_main_cbx_halftone";
	public static final String KEY_MAIN_CBX_SHARPFLAT           = "key_main_cbx_sharpflat";
	public static final String KEY_MAIN_CBX_OCTAVE              = "key_main_cbx_octave";
	public static final String KEY_MAIN_CBX_SYNTAX              = "key_main_cbx_syntax";
	public static final String KEY_MAIN_CBX_PERCUSSION          = "key_main_cbx_percussion";
	public static final String KEY_MAIN_CBX_INSTRUMENT          = "key_main_cbx_instrument";
	public static final String KEY_PLAYER_CLOSE                 = "key_player_close";
	public static final String KEY_PLAYER_PLAY                  = "key_player_play";
	public static final String KEY_PLAYER_REPARSE               = "key_player_reparse";
	public static final String KEY_PLAYER_INFO                  = "key_player_info";
	public static final String KEY_PLAYER_SOUNDCHECK            = "key_player_soundcheck";
	public static final String KEY_PLAYER_MEMORIZE              = "key_player_memorize";
	public static final String KEY_PLAYER_JUMP_FIELD            = "key_player_jump_field";
	public static final String KEY_PLAYER_GO                    = "key_player_go";
	public static final String KEY_PLAYER_LYRICS                = "key_player_lyrics";
	public static final String KEY_PLAYER_STOP                  = "key_player_stop";
	public static final String KEY_PLAYER_FAST_REWIND           = "key_player_fast_rewind";
	public static final String KEY_PLAYER_REWIND                = "key_player_rewind";
	public static final String KEY_PLAYER_FORWARD               = "key_player_forward";
	public static final String KEY_PLAYER_FAST_FORWARD          = "key_player_fast_forward";
	public static final String KEY_PLAYER_BEGIN                 = "key_player_begin";
	public static final String KEY_PLAYER_END                   = "key_player_end";
	public static final String KEY_PLAYER_VOL_FLD               = "key_player_vol_fld";
	public static final String KEY_PLAYER_VOL_SLD               = "key_player_vol_sld";
	public static final String KEY_PLAYER_TEMPO_FLD             = "key_player_tempo_fld";
	public static final String KEY_PLAYER_TEMPO_SLD             = "key_player_tempo_sld";
	public static final String KEY_PLAYER_TRANSPOSE_FLD         = "key_player_transpose_fld";
	public static final String KEY_PLAYER_TRANSPOSE_SLD         = "key_player_transpose_sld";
	public static final String KEY_PLAYER_CH_VOL_FLD            = "key_player_ch_vol_fld";
	public static final String KEY_PLAYER_CH_VOL_SLD            = "key_player_ch_vol_sld";
	public static final String KEY_PLAYER_CH_VOL_BTN            = "key_player_ch_vol_btn";
	public static final String KEY_PLAYER_CH_00                 = "key_player_ch_00";
	public static final String KEY_PLAYER_CH_01                 = "key_player_ch_01";
	public static final String KEY_PLAYER_CH_02                 = "key_player_ch_02";
	public static final String KEY_PLAYER_CH_03                 = "key_player_ch_03";
	public static final String KEY_PLAYER_CH_04                 = "key_player_ch_04";
	public static final String KEY_PLAYER_CH_05                 = "key_player_ch_05";
	public static final String KEY_PLAYER_CH_06                 = "key_player_ch_06";
	public static final String KEY_PLAYER_CH_07                 = "key_player_ch_07";
	public static final String KEY_PLAYER_CH_08                 = "key_player_ch_08";
	public static final String KEY_PLAYER_CH_09                 = "key_player_ch_09";
	public static final String KEY_PLAYER_CH_10                 = "key_player_ch_10";
	public static final String KEY_PLAYER_CH_11                 = "key_player_ch_11";
	public static final String KEY_PLAYER_CH_12                 = "key_player_ch_12";
	public static final String KEY_PLAYER_CH_13                 = "key_player_ch_13";
	public static final String KEY_PLAYER_CH_14                 = "key_player_ch_14";
	public static final String KEY_PLAYER_CH_15                 = "key_player_ch_15";
	public static final String KEY_PLAYER_CH_00_M               = "key_player_ch_00_m";
	public static final String KEY_PLAYER_CH_01_M               = "key_player_ch_01_m";
	public static final String KEY_PLAYER_CH_02_M               = "key_player_ch_02_m";
	public static final String KEY_PLAYER_CH_03_M               = "key_player_ch_03_m";
	public static final String KEY_PLAYER_CH_04_M               = "key_player_ch_04_m";
	public static final String KEY_PLAYER_CH_05_M               = "key_player_ch_05_m";
	public static final String KEY_PLAYER_CH_06_M               = "key_player_ch_06_m";
	public static final String KEY_PLAYER_CH_07_M               = "key_player_ch_07_m";
	public static final String KEY_PLAYER_CH_08_M               = "key_player_ch_08_m";
	public static final String KEY_PLAYER_CH_09_M               = "key_player_ch_09_m";
	public static final String KEY_PLAYER_CH_10_M               = "key_player_ch_10_m";
	public static final String KEY_PLAYER_CH_11_M               = "key_player_ch_11_m";
	public static final String KEY_PLAYER_CH_12_M               = "key_player_ch_12_m";
	public static final String KEY_PLAYER_CH_13_M               = "key_player_ch_13_m";
	public static final String KEY_PLAYER_CH_14_M               = "key_player_ch_14_m";
	public static final String KEY_PLAYER_CH_15_M               = "key_player_ch_15_m";
	public static final String KEY_PLAYER_CH_00_S               = "key_player_ch_00_s";
	public static final String KEY_PLAYER_CH_01_S               = "key_player_ch_01_s";
	public static final String KEY_PLAYER_CH_02_S               = "key_player_ch_02_s";
	public static final String KEY_PLAYER_CH_03_S               = "key_player_ch_03_s";
	public static final String KEY_PLAYER_CH_04_S               = "key_player_ch_04_s";
	public static final String KEY_PLAYER_CH_05_S               = "key_player_ch_05_s";
	public static final String KEY_PLAYER_CH_06_S               = "key_player_ch_06_s";
	public static final String KEY_PLAYER_CH_07_S               = "key_player_ch_07_s";
	public static final String KEY_PLAYER_CH_08_S               = "key_player_ch_08_s";
	public static final String KEY_PLAYER_CH_09_S               = "key_player_ch_09_s";
	public static final String KEY_PLAYER_CH_10_S               = "key_player_ch_10_s";
	public static final String KEY_PLAYER_CH_11_S               = "key_player_ch_11_s";
	public static final String KEY_PLAYER_CH_12_S               = "key_player_ch_12_s";
	public static final String KEY_PLAYER_CH_13_S               = "key_player_ch_13_s";
	public static final String KEY_PLAYER_CH_14_S               = "key_player_ch_14_s";
	public static final String KEY_PLAYER_CH_15_S               = "key_player_ch_15_s";
	public static final String KEY_SOUNDCHECK_CLOSE             = "key_soundcheck_close";
	public static final String KEY_SOUNDCHECK_PLAY              = "key_soundcheck_play";
	public static final String KEY_SOUNDCHECK_FILTER_INSTR      = "key_soundcheck_filter_instr";
	public static final String KEY_SOUNDCHECK_FILTER_NOTE       = "key_soundcheck_filter_note";
	public static final String KEY_SOUNDCHECK_INSTR             = "key_soundcheck_instr";
	public static final String KEY_SOUNDCHECK_NOTE              = "key_soundcheck_note";
	public static final String KEY_SOUNDCHECK_VOL_FLD           = "key_soundcheck_vol_fld";
	public static final String KEY_SOUNDCHECK_VOL_SLD           = "key_soundcheck_vol_sld";
	public static final String KEY_SOUNDCHECK_VEL_FLD           = "key_soundcheck_vel_fld";
	public static final String KEY_SOUNDCHECK_VEL_SLD           = "key_soundcheck_vel_sld";
	public static final String KEY_SOUNDCHECK_DURATION          = "key_soundcheck_duration";
	public static final String KEY_SOUNDCHECK_KEEP              = "key_soundcheck_keep";
	public static final String KEY_SOUNDCHECK_CH_00             = "key_soundcheck_ch_00";
	public static final String KEY_SOUNDCHECK_CH_01             = "key_soundcheck_ch_01";
	public static final String KEY_SOUNDCHECK_CH_02             = "key_soundcheck_ch_02";
	public static final String KEY_SOUNDCHECK_CH_03             = "key_soundcheck_ch_03";
	public static final String KEY_SOUNDCHECK_CH_04             = "key_soundcheck_ch_04";
	public static final String KEY_SOUNDCHECK_CH_05             = "key_soundcheck_ch_05";
	public static final String KEY_SOUNDCHECK_CH_06             = "key_soundcheck_ch_06";
	public static final String KEY_SOUNDCHECK_CH_07             = "key_soundcheck_ch_07";
	public static final String KEY_SOUNDCHECK_CH_08             = "key_soundcheck_ch_08";
	public static final String KEY_SOUNDCHECK_CH_09             = "key_soundcheck_ch_09";
	public static final String KEY_SOUNDCHECK_CH_10             = "key_soundcheck_ch_10";
	public static final String KEY_SOUNDCHECK_CH_11             = "key_soundcheck_ch_11";
	public static final String KEY_SOUNDCHECK_CH_12             = "key_soundcheck_ch_12";
	public static final String KEY_SOUNDCHECK_CH_13             = "key_soundcheck_ch_13";
	public static final String KEY_SOUNDCHECK_CH_14             = "key_soundcheck_ch_14";
	public static final String KEY_SOUNDCHECK_CH_15             = "key_soundcheck_ch_15";
	public static final String KEY_INFO_CLOSE                   = "key_info_close";
	public static final String KEY_INFO_CONF                    = "key_info_conf";
	public static final String KEY_INFO_CONF_NOTE               = "key_info_conf_note";
	public static final String KEY_INFO_CONF_PERC               = "key_info_conf_perc";
	public static final String KEY_INFO_CONF_SYNTAX             = "key_info_conf_syntax";
	public static final String KEY_INFO_CONF_INSTR              = "key_info_conf_instr";
	public static final String KEY_INFO_CONF_DRUMKIT            = "key_info_conf_drumkit";
	public static final String KEY_INFO_SB                      = "key_info_sb";
	public static final String KEY_INFO_SB_GENERAL              = "key_info_sb_general";
	public static final String KEY_INFO_SB_INSTR                = "key_info_sb_instr";
	public static final String KEY_INFO_SB_RES                  = "key_info_sb_res";
	public static final String KEY_INFO_MIDI                    = "key_info_midi";
	public static final String KEY_INFO_MIDI_GENERAL            = "key_info_midi_general";
	public static final String KEY_INFO_MIDI_KARAOKE            = "key_info_midi_karaoke";
	public static final String KEY_INFO_MIDI_BANKS              = "key_info_midi_banks";
	public static final String KEY_INFO_MIDI_MSG                = "key_info_midi_msg";
	public static final String KEY_INFO_ABOUT                   = "key_info_about";
	public static final String KEY_INFO_KEYBINDINGS             = "key_info_keybindings";
	public static final String KEY_INFO_CONF_NOTE_FILTER        = "key_info_conf_note_filter";
	public static final String KEY_INFO_CONF_PERC_FILTER        = "key_info_conf_perc_filter";
	public static final String KEY_INFO_CONF_SYNTAX_FILTER      = "key_info_conf_syntax_filter";
	public static final String KEY_INFO_CONF_INSTR_FILTER       = "key_info_conf_instr_filter";
	public static final String KEY_INFO_CONF_DRUMKIT_FILTER     = "key_info_conf_drumkit_filter";
	public static final String KEY_INFO_SB_INSTR_FILTER         = "key_info_sb_instr_filter";
	public static final String KEY_INFO_SB_RES_FILTER           = "key_info_sb_res_filter";
	public static final String KEY_INFO_MIDI_MSG_FILTER         = "key_info_midi_msg_filter";
	public static final String KEY_INFO_MIDI_BANKS_TOT_PL       = "key_info_midi_banks_tot_pl";
	public static final String KEY_INFO_MIDI_BANKS_TOT_MIN      = "key_info_midi_banks_tot_min";
	public static final String KEY_INFO_MIDI_BANKS_TOT_TREE     = "key_info_midi_banks_tot_tree";
	public static final String KEY_INFO_MIDI_BANKS_CH_PL        = "key_info_midi_banks_ch_pl";
	public static final String KEY_INFO_MIDI_BANKS_CH_MIN       = "key_info_midi_banks_ch_min";
	public static final String KEY_INFO_MIDI_BANKS_CH_TREE      = "key_info_midi_banks_ch_tree";
	public static final String KEY_INFO_MIDI_MSG_PL             = "key_info_midi_msg_pl";
	public static final String KEY_INFO_MIDI_MSG_MIN            = "key_info_midi_msg_min";
	public static final String KEY_INFO_MIDI_MSG_TREE           = "key_info_midi_msg_tree";
	public static final String KEY_INFO_MIDI_MSG_TABLE          = "key_info_midi_msg_table";
	public static final String KEY_INFO_MIDI_MSG_CH_INDEP       = "key_info_midi_msg_ch_indep";
	public static final String KEY_INFO_MIDI_MSG_CH_DEP         = "key_info_midi_msg_ch_dep";
	public static final String KEY_INFO_MIDI_MSG_SEL_NOD        = "key_info_midi_msg_sel_nod";
	public static final String KEY_INFO_MIDI_MSG_LIM_TCK        = "key_info_midi_msg_lim_tck";
	public static final String KEY_INFO_MIDI_MSG_TICK_FROM      = "key_info_midi_msg_tick_from";
	public static final String KEY_INFO_MIDI_MSG_TICK_TO        = "key_info_midi_msg_tick_to";
	public static final String KEY_INFO_MIDI_MSG_LIM_TRK        = "key_info_midi_msg_lim_trk";
	public static final String KEY_INFO_MIDI_MSG_TRACKS_TXT     = "key_info_midi_msg_tracks_txt";
	public static final String KEY_INFO_MIDI_MSG_SHOW_IN_TR     = "key_info_midi_msg_show_in_tr";
	public static final String KEY_INFO_MIDI_MSG_SHOW_AUTO      = "key_info_midi_msg_show_auto";
	public static final String KEY_INFO_MIDI_MSG_CH_00          = "key_info_midi_msg_ch_00";
	public static final String KEY_INFO_MIDI_MSG_CH_01          = "key_info_midi_msg_ch_01";
	public static final String KEY_INFO_MIDI_MSG_CH_02          = "key_info_midi_msg_ch_02";
	public static final String KEY_INFO_MIDI_MSG_CH_03          = "key_info_midi_msg_ch_03";
	public static final String KEY_INFO_MIDI_MSG_CH_04          = "key_info_midi_msg_ch_04";
	public static final String KEY_INFO_MIDI_MSG_CH_05          = "key_info_midi_msg_ch_05";
	public static final String KEY_INFO_MIDI_MSG_CH_06          = "key_info_midi_msg_ch_06";
	public static final String KEY_INFO_MIDI_MSG_CH_07          = "key_info_midi_msg_ch_07";
	public static final String KEY_INFO_MIDI_MSG_CH_08          = "key_info_midi_msg_ch_08";
	public static final String KEY_INFO_MIDI_MSG_CH_09          = "key_info_midi_msg_ch_09";
	public static final String KEY_INFO_MIDI_MSG_CH_10          = "key_info_midi_msg_ch_10";
	public static final String KEY_INFO_MIDI_MSG_CH_11          = "key_info_midi_msg_ch_11";
	public static final String KEY_INFO_MIDI_MSG_CH_12          = "key_info_midi_msg_ch_12";
	public static final String KEY_INFO_MIDI_MSG_CH_13          = "key_info_midi_msg_ch_13";
	public static final String KEY_INFO_MIDI_MSG_CH_14          = "key_info_midi_msg_ch_14";
	public static final String KEY_INFO_MIDI_MSG_CH_15          = "key_info_midi_msg_ch_15";
	public static final String KEY_INFO_KEY_TREE                = "key_info_key_tree";
	public static final String KEY_INFO_KEY_PL                  = "key_info_key_pl";
	public static final String KEY_INFO_KEY_MIN                 = "key_info_key_min";
	public static final String KEY_INFO_KEY_FLD                 = "key_info_key_fld";
	public static final String KEY_INFO_KEY_FILTER              = "key_info_key_filter";
	public static final String KEY_INFO_KEY_ADD_BTN             = "key_info_key_add_btn";
	public static final String KEY_INFO_KEY_RESET_ID_CBX        = "key_info_key_reset_id_cbx";
	public static final String KEY_INFO_KEY_RESET_ID_BTN        = "key_info_key_reset_id_btn";
	public static final String KEY_INFO_KEY_RESET_GLOB_CBX      = "key_info_key_reset_glob_cbx";
	public static final String KEY_INFO_KEY_RESET_GLOB_BTN      = "key_info_key_reset_glob_btn";
	public static final String KEY_MSG_CLOSE                    = "key_msg_close";
	public static final String KEY_STRING_FILTER_CLOSE          = "key_string_filter_close";
	public static final String KEY_STRING_FILTER_CLEAR          = "key_string_filter_clear";
	public static final String KEY_FILE_SELECT_CLOSE            = "key_file_select_close";
	public static final String KEY_FILE_SELECT_CHARSET_CBX      = "key_file_select_charset_cbx";
	public static final String KEY_FILE_SELECT_FOREIGN_EXE      = "key_file_select_foreign_exe";
	public static final String KEY_FILE_SELECT_CONFIG_OPEN      = "key_file_select_config_open";
	public static final String KEY_FILE_SELECTOR_IMP_MPL        = "key_file_selector_imp_mpl";
	public static final String KEY_FILE_SELECTOR_IMP_MID        = "key_file_selector_imp_mid";
	public static final String KEY_FILE_SELECTOR_IMP_ALDA       = "key_file_selector_imp_alda";
	public static final String KEY_FILE_SELECTOR_IMP_ABC        = "key_file_selector_imp_abc";
	public static final String KEY_FILE_SELECTOR_IMP_LY         = "key_file_selector_imp_ly";
	public static final String KEY_FILE_SELECTOR_IMP_MSCORE     = "key_file_selector_imp_mscore";
	public static final String KEY_FILE_SELECTOR_SND_FILE       = "key_file_selector_snd_file";
	public static final String KEY_FILE_SELECTOR_SND_URL        = "key_file_selector_snd_url";
	public static final String KEY_FILE_SELECTOR_SND_URL_FLD    = "key_file_selector_url_fld";
	public static final String KEY_FILE_SELECTOR_SND_DOWNLOAD   = "key_file_selector_url_download";
	public static final String KEY_FILE_SELECTOR_EXP_MID        = "key_file_selector_exp_mid";
	public static final String KEY_FILE_SELECTOR_EXP_MPL        = "key_file_selector_exp_mpl";
	public static final String KEY_FILE_SELECTOR_EXP_ALDA       = "key_file_selector_exp_alda";
	public static final String KEY_FILE_SELECTOR_EXP_AUDIO      = "key_file_selector_exp_audio";
	public static final String KEY_FILE_SELECTOR_EXP_ABC        = "key_file_selector_exp_abc";
	public static final String KEY_FILE_SELECTOR_EXP_LY         = "key_file_selector_exp_ly";
	public static final String KEY_FILE_SELECTOR_EXP_MSCORE     = "key_file_selector_exp_mscore";
	public static final String KEY_FILE_CONF_CLOSE              = "key_file_conf_close";
	public static final String KEY_FILE_CONF_SAVE               = "key_file_conf_save";
	public static final String KEY_FILE_CONF_RESTORE_SAVED      = "key_file_conf_restore_saved";
	public static final String KEY_FILE_CONF_RESTORE_DEFAULT    = "key_file_conf_restore_default";
	public static final String KEY_DC_CONF_TAB_DEBUG            = "key_dc_conf_tab_debug";
	public static final String KEY_DC_CONF_TAB_NOTE_LENGTH      = "key_dc_conf_tab_note_length";
	public static final String KEY_DC_CONF_TAB_CHORDS           = "key_dc_conf_tab_chords";
	public static final String KEY_DC_CONF_TAB_NOTE_REST        = "key_dc_conf_tab_note_rest";
	public static final String KEY_DC_CONF_TAB_KARAOKE          = "key_dc_conf_tab_karaoke";
	public static final String KEY_DC_CONF_TAB_CTRL_CHANGE      = "key_dc_conf_tab_ctrl_change";
	public static final String KEY_DC_CONF_TAB_SLICES           = "key_dc_conf_tab_slices";
	public static final String KEY_DC_CONF_ADD_TICK_COMMENTS    = "key_dc_conf_add_tick_comments";
	public static final String KEY_DC_CONF_ADD_CONFIG           = "key_dc_conf_config";
	public static final String KEY_DC_CONF_ADD_SCORE            = "key_dc_conf_add_score";
	public static final String KEY_DC_CONF_ADD_STATISTICS       = "key_dc_conf_add_statistics";
	public static final String KEY_DC_CONF_ADD_STRATEGY_STAT    = "key_dc_conf_add_strategy_stat";
	public static final String KEY_DC_CONF_NOTE_LENGTH_STRATEGY = "key_dc_conf_note_length_strategy";
	public static final String KEY_DC_CONF_MIN_TARGET_TICKS_ON  = "key_dc_conf_min_target_ticks_on";
	public static final String KEY_DC_CONF_MAX_TARGET_TICKS_ON  = "key_dc_conf_max_target_ticks_on";
	public static final String KEY_DC_CONF_MIN_DUR_TO_KEEP      = "key_dc_conf_min_dur_to_keep";
	public static final String KEY_DC_CONF_MAX_DUR_TO_KEEP      = "key_dc_conf_max_dur_to_keep";
	public static final String KEY_DC_CONF_TOL_TICK_LEN         = "key_dc_conf_tol_tick_len";
	public static final String KEY_DC_CONF_TOL_DUR_RATIO        = "key_dc_conf_tol_dur_ratio";
	public static final String KEY_DC_CONF_CRD_PREDEFINED       = "key_dc_conf_crd_predefined";
	public static final String KEY_DC_CONF_CRD_NOTE_ON          = "key_dc_conf_crd_note_on";
	public static final String KEY_DC_CONF_CRD_NOTE_OFF         = "key_dc_conf_crd_note_off";
	public static final String KEY_DC_CONF_CRD_VELOCITY         = "key_dc_conf_crd_velocity";
	public static final String KEY_DC_CONF_USE_DOT_NOTES        = "key_dc_conf_use_dot_notes";
	public static final String KEY_DC_CONF_USE_DOT_RESTS        = "key_dc_conf_use_dot_rests";
	public static final String KEY_DC_CONF_USE_TRIP_NOTES       = "key_dc_conf_use_trip_notes";
	public static final String KEY_DC_CONF_USE_TRIP_RESTS       = "key_dc_conf_use_trip_rests";
	public static final String KEY_DC_CONF_ALL_SYL_ORP          = "key_dc_conf_all_syl_orp";
	public static final String KEY_DC_CONF_USE_KARAOKE          = "key_dc_conf_use_karaoke";
	public static final String KEY_DC_CONF_KAR_ORPHANED         = "key_dc_conf_kar_orphaned";
	public static final String KEY_DC_CONF_KAR_ONE_CH           = "key_dc_conf_kar_one_ch";
	public static final String KEY_DC_CONF_CTRL_CHANGE_MODE     = "key_dc_conf_ctrl_change_mode";
	public static final String KEY_DC_CONF_FLD_GLOB_SINGLE      = "key_dc_conf_fld_glob_single";
	public static final String KEY_DC_CONF_BTN_GLOB_SINGLE      = "key_dc_conf_btn_glob_single";
	public static final String KEY_DC_CONF_FLD_GLOB_EACH        = "key_dc_conf_fld_glob_each";
	public static final String KEY_DC_CONF_FLD_GLOB_FROM        = "key_dc_conf_fld_glob_from";
	public static final String KEY_DC_CONF_FLD_GLOB_TO          = "key_dc_conf_fld_glob_to";
	public static final String KEY_DC_CONF_BTN_GLOB_RANGE       = "key_dc_conf_btn_glob_range";
	public static final String KEY_DC_CONF_AREA_GLOB_ALL        = "key_dc_conf_area_glob_all";
	public static final String KEY_DC_CONF_BTN_GLOB_ALL         = "key_dc_conf_btn_glob_all";
	public static final String KEY_AU_CONF_ENCODING             = "key_au_conf_encoding";
	public static final String KEY_AU_CONF_FLD_SAMPLE_SIZE_BITS = "key_au_conf_fld_sample_size_bits";
	public static final String KEY_AU_CONF_FLD_SAMPLE_RATE      = "key_au_conf_fld_sample_rate";
	public static final String KEY_AU_CONF_CHANNELS             = "key_au_conf_channels";
	public static final String KEY_AU_CONF_IS_BIG_ENDIAN        = "key_au_conf_is_big_endian";
	public static final String KEY_EXPORT_RESULT_CLOSE          = "key_export_result_close";
	public static final String KEY_EXPORT_RESULT_SHORT          = "key_export_result_short";
	public static final String KEY_EXPORT_RESULT_META           = "key_export_result_meta";
	public static final String KEY_EXPORT_RESULT_SYSEX          = "key_export_result_sysex";
	public static final String KEY_EXPORT_RESULT_SKIPPED_RESTS  = "key_export_result_skipped_rests";
	public static final String KEY_EXPORT_RESULT_OFF_NOT_FOUND  = "key_export_result_off_not_found";
	public static final String KEY_EXPORT_RESULT_CRD_GRP_FAILED = "key_export_result_crd_grp_failed";
	public static final String KEY_EXPORT_RESULT_OTHER          = "key_export_result_other";
	public static final String KEY_EXPORT_RESULT_FILTER         = "key_export_result_filter";
	
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
	public static final String SHARP_FLAT_DEFAULT          = "sharp_flat_default";
	public static final String OCTAVE_NAMING               = "octave_naming";
	public static final String SYNTAX                      = "syntax";
	public static final String PERCUSSION                  = "percussion";
	public static final String INSTRUMENT_IDS              = "instrument_ids";
	public static final String DRUMKIT_IDS                 = "drumkit_ids";
	public static final String SHOW_INFO                   = "show_info";
	public static final String SHOW_INFO_FROM_PLAYER       = "show_info_from_player";
	public static final String IMPORT                      = "import";
	public static final String EXPORT                      = "export";
	public static final String TRANSPOSE_LEVEL             = "transpose_level";
	public static final String PLAYER                      = "player";
	public static final String IMPORT_FILE                 = "import_file";
	public static final String IMPORTED_FILE               = "imported_file";
	public static final String IMPORTED_TYPE               = "imported_type";
	public static final String IMPORTED_TYPE_MIDI          = "imported_type_midi";
	public static final String IMPORTED_TYPE_MPL           = "imported_type_mpl";
	public static final String IMPORTED_TYPE_ALDA          = "imported_type_alda";
	public static final String IMPORTED_TYPE_ABC           = "imported_type_abc";
	public static final String IMPORTED_TYPE_LY            = "imported_type_ly";
	public static final String IMPORTED_TYPE_MSCORE        = "imported_type_mscore";
	public static final String SOUNDBANK                   = "soundbank";
	public static final String CURRENT_SOUNDBANK           = "current_soundbank";
	public static final String REMEMBER_SOUND              = "remember_sound";
	public static final String REMEMBER_SOUND_TT           = "remember_sound_tt";
	public static final String REMEMBER_IMPORT             = "remember_import";
	public static final String REMEMBER_IMPORT_TT          = "remember_import_tt";
	public static final String PLAYER_BUTTON               = "player_button";
	public static final String UNCHOSEN_FILE               = "unchosen_file";
	public static final String SB_LOADED_BY_SOURCE         = "sb_loaded_by_source";
	public static final String CHOOSE_FILE                 = "choose_file";
	public static final String CHOOSE_FILE_EXPORT          = "choose_file_export";
	public static final String EXPORT_FILE                 = "export_file";
	public static final String CONF_ERROR_OK               = "conf_error_ok";
	public static final String CONF_ERROR_ERROR            = "conf_error_error";
	public static final String ERROR_NOT_YET_IMPLEMENTED   = "error_not_yet_implemented";
	
	// Foreign
	public static final String FOREIGN_CREATE_TMPDIR       = "foreign_create_tmpdir";
	public static final String FOREIGN_READ_TMPDIR         = "foreign_read_tmpdir";
	public static final String FOREIGN_CREATE_TMPFILE      = "foreign_create_tmpfile";
	public static final String FOREIGN_EX_CODE             = "foreign_ex_code";
	public static final String FOREIGN_EX_INTERRUPTED      = "foreign_ex_interrupted";
	public static final String FOREIGN_EX_EXECUTE          = "foreign_ex_execute";
	public static final String FOREIGN_EX_NO_EXE           = "foreign_ex_no_exe";
	
	// DecompileConfigView
	public static final String TITLE_DC_CONFIG              = "title_dc_config";
	public static final String DC_TAB_DEBUG                 = "dc_tab_debug";
	public static final String DC_TAB_NOTE_LENGTH           = "dc_tab_note_length";
	public static final String DC_TAB_CHORDS                = "dc_tab_chords";
	public static final String DC_TAB_NOTE_REST             = "dc_tab_note_rest";
	public static final String DC_TAB_KARAOKE               = "dc_tab_karaoke";
	public static final String DC_TAB_CTRL_CHANGE           = "dc_tab_ctrl_change";
	public static final String DC_TAB_SLICE                 = "dc_tab_slice";
	public static final String DC_TABINFO_DEBUG             = "dc_tabinfo_debug";
	public static final String DC_TABINFO_NOTE_LENGTH       = "dc_tabinfo_note_length";
	public static final String DC_TABINFO_CHORDS            = "dc_tabinfo_chords";
	public static final String DC_TABINFO_NOTE_REST         = "dc_tabinfo_note_rest";
	public static final String DC_TABINFO_KARAOKE           = "dc_tabinfo_karaoke";
	public static final String DC_TABINFO_CTRL_CHANGE       = "dc_tabinfo_ctrl_change";
	public static final String DC_TABINFO_SLICE             = "dc_tabinfo_slice";
	public static final String DC_ADD_TICK_COMMENT          = "dc_add_tick_comment";
	public static final String DC_ADD_CONFIG                = "dc_add_config";
	public static final String DC_ADD_SCORE                 = "dc_add_score";
	public static final String DC_ADD_STATISTICS            = "dc_add_statistics";
	public static final String DC_ADD_STRATEGY_STAT         = "dc_add_strategy_stat";
	public static final String NOTE_LENGTH_STRATEGY         = "note_length_strategy";
	public static final String MIN_TARGET_TICKS_NEXT_ON     = "min_target_ticks_next_on";
	public static final String MAX_TARGET_TICKS_NEXT_ON     = "max_target_ticks_next_on";
	public static final String MIN_DURATION_TO_KEEP         = "min_duration_to_keep";
	public static final String MIN_DURATION_TO_KEEP_D       = "min_duration_to_keep_d";
	public static final String MAX_DURATION_TO_KEEP         = "max_duration_to_keep";
	public static final String MAX_DURATION_TO_KEEP_D       = "max_duration_to_keep_d";
	public static final String LENGTH_TICK_TOLERANCE        = "length_tick_tolerance";
	public static final String LENGTH_TICK_TOLERANCE_D      = "length_tick_tolerance_d";
	public static final String DURATION_RATIO_TOLERANCE     = "duration_ratio_tolerance";
	public static final String DURATION_RATIO_TOLERANCE_D   = "duration_ratio_tolerance_d";
	public static final String USE_PRE_DEFINED_CHORDS       = "use_pre_defined_chords";
	public static final String USE_PRE_DEFINED_CHORDS_D     = "use_pre_defined_chords_d";
	public static final String CHORD_NOTE_ON_TOLERANCE      = "chord_note_on_tolerance";
	public static final String CHORD_NOTE_ON_TOLERANCE_D    = "chord_note_on_tolerance_d";
	public static final String CHORD_NOTE_OFF_TOLERANCE     = "chord_note_off_tolerance";
	public static final String CHORD_NOTE_OFF_TOLERANCE_D   = "chord_note_off_tolerance_d";
	public static final String CHORD_VELOCITY_TOLERANCE     = "chord_velocity_tolerance";
	public static final String CHORD_VELOCITY_TOLERANCE_D   = "chord_velocity_tolerance_d";
	public static final String USE_DOTTED_NOTES             = "use_dotted_notes";
	public static final String USE_DOTTED_RESTS             = "use_dotted_rests";
	public static final String USE_TRIPLETTED_NOTES         = "use_tripletted_notes";
	public static final String USE_TRIPLETTED_RESTS         = "use_tripletted_rests";
	public static final String USE_KARAOKE                  = "use_karaoke";
	public static final String USE_KARAOKE_D                = "use_karaoke_d";
	public static final String ALL_SYLLABLES_ORPHANED       = "all_syllables_orphaned";
	public static final String ALL_SYLLABLES_ORPHANED_D     = "all_syllables_orphaned_d";
	public static final String ORPHANED_SYLLABLES           = "orphaned_syllables";
	public static final String ORPHANED_SYLLABLES_D         = "orphaned_syllables_d";
	public static final String CTRL_CHANGE_MODE             = "ctrl_change_mode";
	public static final String CTRL_CHANGE_MODE_D           = "ctrl_change_mode_d";
	public static final String DC_INLINE_BLOCK              = "dc_inline_block";
	public static final String DC_SLICE_BEGIN_BLOCK         = "dc_slice_begin_block";
	public static final String DC_STRAT_NEXT_DURATION_PRESS = "dc_strat_next_duration_press";
	public static final String DC_STRAT_DURATION_NEXT_PRESS = "dc_strat_duration_next_press";
	public static final String DC_STRAT_NEXT_PRESS          = "dc_strat_next_press";
	public static final String DC_STRAT_DURATION_PRESS      = "dc_strat_duration_press";
	public static final String DC_STRAT_PRESS               = "dc_strat_press";
	public static final String KAR_ONE_CHANNEL              = "kar_one_channel";
	public static final String KAR_ONE_CHANNEL_D            = "kar_one_channel_d";
	public static final String ADD_GLOBAL_AT_TICK           = "add_global_at_tick";
	public static final String ADD_GLOBAL_EACH              = "add_global_each";
	public static final String ADD_GLOBAL_FROM              = "add_global_from";
	public static final String ADD_GLOBAL_TO                = "add_global_to";
	public static final String DC_ALL_TICKS                 = "dc_all_ticks";
	public static final String BTN_ADD_TICK                 = "btn_add_tick";
	public static final String BTN_ADD_TICKS                = "btn_add_ticks";
	public static final String BTN_UPDATE_TICKS             = "btn_update_ticks";
	public static final String DC_RESTORE                   = "dc_restore";
	public static final String DC_RESTORE_DEFAULTS          = "dc_restore_defaults";
	public static final String DC_SAVE                      = "dc_save";
	public static final String CHANGED_IN_CONF_FILE         = "changed_in_conf_file";
	public static final String TICKS_FOR_TARGET_PPQ         = "ticks_for_target_ppq";
	
	// AudioExportView / AudioExportController
	public static final String TITLE_AU_CONFIG              = "title_au_config";
	public static final String AUDIO_ENCODING               = "audio_encoding";
	public static final String AUDIO_SAMPLE_SIZE_BITS       = "audio_sample_size_bits";
	public static final String AUDIO_SAMPLE_SIZE_BITS_D     = "audio_sample_size_bits_d";
	public static final String AUDIO_SAMPLE_RATE            = "audio_sample_rate";
	public static final String AUDIO_SAMPLE_RATE_D          = "audio_sample_rate_d";
	public static final String AUDIO_CHANNELS               = "audio_channels";
	public static final String AUDIO_IS_BIG_ENDIAN          = "audio_is_big_endian";
	public static final String AUDIO_IS_BIG_ENDIAN_D        = "audio_is_big_endian_d";
	public static final String AUDIO_FILE_TYPE              = "audio_file_type";
	public static final String AUDIO_FILE_TYPE_D            = "audio_file_type_d";
	public static final String AU_MONO                      = "au_mono";
	public static final String AU_STEREO                    = "au_stereo";
	
	// InfoView
	public static final String TITLE_INFO_VIEW             = "title_info_view";
	public static final String TAB_CONFIG                  = "tab_config";
	public static final String TAB_SOUNDBANK               = "tab_soundbank";
	public static final String TAB_MIDI_SEQUENCE           = "tab_midi_sequence";
	public static final String TAB_KEYBINDINGS             = "tab_keybindings";
	public static final String TAB_ABOUT                   = "tab_about";
	public static final String TAB_NOTE_DETAILS            = "tab_note_details";
	public static final String TAB_PERCUSSION_DETAILS      = "tab_percussion_details";
	public static final String TAB_SYNTAX_DETAILS          = "tab_syntax_details";
	public static final String TAB_SOUNDBANK_INFO          = "tab_soundbank_info";
	public static final String TAB_SOUNDBANK_INSTRUMENTS   = "tab_soundbank_instruments";
	public static final String TAB_SOUNDBANK_RESOURCES     = "tab_soundbank_resources";
	public static final String TAB_MIDI_SEQUENCE_INFO      = "tab_midi_sequence_info";
	public static final String TAB_MIDI_KARAOKE            = "tab_midi_karaoke";
	public static final String TAB_BANK_INSTR_NOTE         = "tab_bank_instr_note";
	public static final String TAB_MESSAGES                = "tab_messages";
	public static final String SOUNDBANK_DRUMKITS          = "soundbank_drumkits";
	public static final String SOUNDBANK_VENDOR            = "soundbank_vendor";
	public static final String SOUNDBANK_CREA_DATE         = "soundbank_crea_date";
	public static final String SOUNDBANK_CREA_TOOLS        = "soundbank_crea_tools";
	public static final String SOUNDBANK_PRODUCT           = "soundbank_product";
	public static final String SOUNDBANK_TARGET_ENGINE     = "soundbank_target_engine";
	public static final String COPYRIGHT                   = "copyright";
	public static final String SOFTWARE_VERSION            = "software_version";
	public static final String SOFTWARE_DATE               = "software_date";
	public static final String TICK_LENGTH                 = "tick_length";
	public static final String TIME_LENGTH                 = "time_length";
	public static final String RESOLUTION                  = "resolution";
	public static final String RESOLUTION_UNIT             = "resolution_unit";
	public static final String NUMBER_OF_TRACKS            = "number_of_tracks";
	public static final String TEMPO_BPM                   = "tempo_bpm";
	public static final String TEMPO_MPQ                   = "tempo_mpq";
	public static final String AVERAGE                     = "average";
	public static final String MIN                         = "min";
	public static final String MAX                         = "max";
	public static final String CHANNEL                     = "channel";
	public static final String KARAOKE_TYPE                = "karaoke_type";
	public static final String SONG_TITLE                  = "song_title";
	public static final String COMPOSER                    = "composer";
	public static final String LYRICIST                    = "lyricist";
	public static final String ARTIST                      = "artist";
	public static final String KARAOKE_COPYRIGHT           = "karaoke_copyright";
	public static final String KARAOKE_INFO                = "karaoke_info";
	public static final String KARAOKE_GENERAL             = "karaoke_general";
	public static final String KARAOKE_SOFT_KARAOKE        = "karaoke_soft_karaoke";
	public static final String LYRICS                      = "lyrics";
	public static final String TOTAL                       = "total";
	public static final String PER_CHANNEL                 = "per_channel";
	public static final String BANK                        = "bank";
	public static final String COLLAPSE_BUTTON             = "collapse_button";
	public static final String COLLAPSE_TOOLTIP            = "collapse_tooltip";
	public static final String EXPAND_BUTTON               = "expand_button";
	public static final String EXPAND_TOOLTIP              = "expand_tooltip";
	public static final String DETAILS                     = "details";
	public static final String SAMPLES_TOTAL               = "samples_total";
	public static final String SAMPLES_AVERAGE             = "samples_average";
	public static final String FRAMES                      = "frames";
	public static final String BYTES                       = "bytes";
	public static final String SEC                         = "sec";
	public static final String BROADCAST_MSG               = "broadcast_msg";
	public static final String INVALID_MSG                 = "invalid_msg";
	public static final String SB_INSTR_CAT_CHROMATIC      = "sb_instr_cat_chromatic";
	public static final String SB_INSTR_CAT_DRUMKIT_SINGLE = "sb_instr_cat_drumkit_single";
	public static final String SB_INSTR_CAT_DRUMKIT_MULTI  = "sb_instr_cat_drumkit_multi";
	public static final String SB_INSTR_CAT_UNKNOWN        = "sb_instr_cat_unknown";
	public static final String SB_RESOURCE_CAT_SAMPLE      = "sb_resource_cat_sample";
	public static final String SB_RESOURCE_CAT_LAYER       = "sb_resource_cat_layer";
	public static final String SB_RESOURCE_CAT_UNKNOWN     = "sb_resource_cat_unknown";
	public static final String SINGLE_CHANNEL              = "single_channel";
	public static final String MULTI_CHANNEL               = "multi_channel";
	public static final String UNKNOWN                     = "unknown";
	public static final String UNSET                       = "unset";
	public static final String DATE                        = "date";
	public static final String AUTHOR                      = "author";
	public static final String SOURCE_URL                  = "source_url";
	public static final String WEBSITE                     = "website";
	public static final String LINK_TOOLTIP                = "link_tooltip";
	public static final String TIMESTAMP_FORMAT            = "timestamp_format";
	public static final String NAME                        = "name";
	public static final String FILE                        = "file";
	public static final String SOUND_SOURCE                = "sound_source";
	public static final String VERSION                     = "version";
	public static final String DESCRIPTION                 = "description";
	public static final String INFO_COL_NOTE_NUM           = "info_col_note_num";
	public static final String INFO_COL_NOTE_NAME          = "info_col_note_name";
	public static final String INFO_COL_NOTE_ALT           = "info_col_note_alt";
	public static final String INFO_COL_SYNTAX_NAME        = "info_col_syntax_name";
	public static final String INFO_COL_SYNTAX_DESC        = "info_col_syntax_desc";
	public static final String INFO_COL_SYNTAX_SHORTCUT    = "info_col_syntax_shortcut";
	public static final String INFO_COL_PERC_NUM           = "info_col_perc_num";
	public static final String INFO_COL_PERC_ID_LONG       = "info_col_perc_id_long";
	public static final String INFO_COL_PERC_ID_SHORT      = "info_col_perc_id_short";
	public static final String INFO_COL_INSTR_NUM          = "info_col_instr_num";
	public static final String INFO_COL_INSTR_NAME         = "info_col_instr_name";
	public static final String INFO_COL_DRUMKIT_NUM        = "info_col_drumkit_num";
	public static final String INFO_COL_DRUMKIT_NAME       = "info_col_drumkit_name";
	public static final String INFO_COL_SB_INSTR_PROGRAM   = "info_col_sb_instr_program";
	public static final String INFO_COL_SB_INSTR_BANK      = "info_col_sb_instr_bank";
	public static final String INFO_COL_SB_INSTR_NAME      = "info_col_sb_instr_name";
	public static final String INFO_COL_SB_INSTR_CHANNELS  = "info_col_sb_instr_channels";
	public static final String INFO_COL_SB_INSTR_KEYS      = "info_col_sb_instr_keys";
	public static final String INFO_COL_SB_RES_INDEX       = "info_col_sb_res_index";
	public static final String INFO_COL_SB_RES_TYPE        = "info_col_sb_res_type";
	public static final String INFO_COL_SB_RES_NAME        = "info_col_sb_res_name";
	public static final String INFO_COL_SB_RES_FRAMELENGTH = "info_col_sb_res_framelength";
	public static final String INFO_COL_SB_RES_FORMAT      = "info_col_sb_res_format";
	public static final String INFO_COL_SB_RES_CLASS       = "info_col_sb_res_class";
	public static final String INFO_COL_MSG_TICK           = "info_col_msg_tick";
	public static final String INFO_COL_MSG_STATUS_BYTE    = "info_col_msg_status_byte";
	public static final String INFO_COL_MSG_TRACK          = "info_col_msg_track";
	public static final String INFO_COL_MSG_CHANNEL        = "info_col_msg_channel";
	public static final String INFO_COL_MSG_LENGTH         = "info_col_msg_length";
	public static final String INFO_COL_MSG_SUMMARY        = "info_col_msg_summary";
	public static final String INFO_COL_MSG_TYPE           = "info_col_msg_type";
	public static final String INFO_COL_MSG_TT_STATUS      = "info_col_msg_tt_status";
	public static final String INFO_COL_MSG_TT_TRACK       = "info_col_msg_tt_track";
	public static final String INFO_COL_MSG_TT_CHANNEL     = "info_col_msg_tt_channel";
	public static final String INFO_COL_MSG_TT_LENGTH      = "info_col_msg_tt_length";
	public static final String TOOLTIP_BANK_MSB            = "tooltip_bank_msb";
	public static final String TOOLTIP_BANK_LSB            = "tooltip_bank_lsb";
	public static final String TOOLTIP_BANK_FULL           = "tooltip_bank_full";
	public static final String SYNTAX_CAT_DEFINITION       = "syntax_cat_definition";
	public static final String SYNTAX_CAT_EXECUTE          = "syntax_cat_execute";
	public static final String SYNTAX_CAT_VAR_AND_CONST    = "syntax_cat_var_and_const";
	public static final String SYNTAX_CAT_OPTION           = "syntax_cat_option";
	public static final String SYNTAX_CAT_CONDITON         = "syntax_cat_condition";
	public static final String SYNTAX_CAT_GLOBAL           = "syntax_cat_global";
	public static final String SYNTAX_CAT_OTHER            = "syntax_cat_other";
	public static final String SYNTAX_CAT_META             = "syntax_cat_meta";
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
	public static final String MSG_FILTER_CHANNEL_INDEP    = "msg_filter_channel_indep";
	public static final String MSG_FILTER_CHANNEL_DEP      = "msg_filter_channel_dep";
	public static final String MSG_FILTER_SELECTED_NODES   = "msg_filter_selected_nodes";
	public static final String MSG_FILTER_LIMIT_TICKS      = "msg_filter_limit_ticks";
	public static final String MSG_FILTER_TICK_FROM        = "msg_filter_tick_from";
	public static final String MSG_FILTER_TICK_TO          = "msg_filter_tick_to";
	public static final String MSG_FILTER_LIMIT_TRACKS     = "msg_filter_limit_tracks";
	public static final String MSG_FILTER_SHOW_IN_TREE     = "msg_filter_show_in_tree";
	public static final String MSG_FILTER_AUTO_SHOW        = "msg_filter_auto_show";
	public static final String MSG_FLTR_TT_CHANNEL_INDEP   = "msg_fltr_tt_channel_indep";
	public static final String MSG_FLTR_TT_CHANNEL_DEP     = "msg_fltr_tt_channel_dep";
	public static final String MSG_FLTR_TT_CHANNEL_SINGLE  = "msg_fltr_tt_channel_single";
	public static final String MSG_FLTR_TT_SELECTED_NODES  = "msg_fltr_tt_selected_nodes";
	public static final String MSG_FLTR_TT_LIMIT_TICKS     = "msg_fltr_tt_limit_ticks";
	public static final String MSG_FLTR_TT_LIMIT_TRACKS    = "msg_fltr_tt_limit_tracks";
	public static final String MSG_FLTR_TT_TRACKS          = "msg_fltr_tt_tracks";
	public static final String MSG_FLTR_TT_SHOW_IN_TREE    = "msg_fltr_tt_show_in_tree";
	public static final String MSG_FLTR_TT_AUTO_SHOW       = "msg_fltr_tt_auto_show";
	public static final String MSG_FLTR_TT_VISIBLE         = "msg_fltr_tt_visible";
	public static final String MSG_FLTR_TT_TOTAL           = "msg_fltr_tt_total";
	public static final String MSG_DETAILS_TICK_SG         = "msg_details_tick_sg";
	public static final String MSG_DETAILS_TICK_PL         = "msg_details_tick_pl";
	public static final String MSG_DETAILS_LENGTH          = "msg_details_length";
	public static final String MSG_DETAILS_STATUS_BYTE     = "msg_details_status_byte";
	public static final String MSG_DETAILS_TRACK_SG        = "msg_details_track_sg";
	public static final String MSG_DETAILS_TRACK_PL        = "msg_details_track_pl";
	public static final String MSG_DETAILS_CHANNEL_SG      = "msg_details_channel_sg";
	public static final String MSG_DETAILS_CHANNEL_PL      = "msg_details_channel_pl";
	public static final String MSG_DETAILS_META_TYPE       = "msg_details_meta_type";
	public static final String MSG_DETAILS_VENDOR          = "msg_details_vendor";
	public static final String MSG_DETAILS_DEVICE_ID_SG    = "msg_details_device_id_sg";
	public static final String MSG_DETAILS_DEVICE_ID_PL    = "msg_details_device_id_pl";
	public static final String MSG_DETAILS_SUB_ID_1        = "msg_details_sub_id_1";
	public static final String MSG_DETAILS_SUB_ID_2        = "msg_details_sub_id_2";
	public static final String MSG_DETAILS_CTRL_BYTE       = "msg_details_ctrl_byte";
	public static final String MSG_DETAILS_RPN_BYTE_SG     = "msg_details_rpn_byte_sg";
	public static final String MSG_DETAILS_RPN_BYTE_PL     = "msg_details_rpn_byte_pl";
	public static final String MSG_DETAILS_NRPN_BYTE_SG    = "msg_details_nrpn_byte_sg";
	public static final String MSG_DETAILS_NRPN_BYTE_PL    = "msg_details_nrpn_byte_pl";
	public static final String MSG_DETAILS_TEXT_SG         = "msg_details_text_sg";
	public static final String MSG_DETAILS_TEXT_PL         = "msg_details_text_pl";
	public static final String MSG_DETAILS_MESSAGE         = "msg_details_message";
	public static final String MSG_DETAILS_DESCRIPTION     = "msg_details_description";
	public static final String KB_CATEGORIES               = "kb_categories";
	public static final String KB_CATEGORY                 = "kb_category";
	public static final String KB_FILTER                   = "kb_filter";
	public static final String KB_ACTION                   = "kb_action";
	public static final String KB_HINT                     = "kb_hint";
	public static final String KB_HINT_TXT                 = "kb_hint_txt";
	public static final String KB_CONFIGURED               = "kb_configured";
	public static final String KB_ENTER                    = "kb_enter";
	public static final String KB_ADD                      = "kb_add";
	public static final String KB_ADD_BTN                  = "kb_add_btn";
	public static final String KB_REMOVE                   = "kb_remove";
	public static final String KB_RESET_ID_CBX             = "kb_reset_id_cbx";
	public static final String KB_RESET_ID_BTN             = "kb_reset_id_btn";
	public static final String KB_RESET_GLOB_CBX           = "kb_reset_glob_cbx";
	public static final String KB_RESET_GLOB_BTN           = "kb_reset_glob_btn";
	public static final String KB_ERROR_NO_BINDING_PRESSED = "kb_error_no_binding_pressed";
	
	// MIDI messages for InfoView - level 1
	public static final String MSG1_CH_VOICE            = "msg1_ch_voice";          // MSG2_CV_*
	public static final String MSG1_CH_MODE             = "msg1_ch_mode";           // MSG2_CM_*
	public static final String MSG1_SYSTEM_COMMON       = "msg1_system_common";     // MSG2_SC_*
	public static final String MSG1_SYSTEM_REALTIME     = "msg1_system_realtime";   // MSG2_SR_*
	public static final String MSG1_META                = "msg1_meta";              // MSG2_M_*
	// MIDI messages for InfoView - level 2
	public static final String MSG2_CV_NOTE_OFF         = "msg2_cv_note_off";
	public static final String MSG2_CV_NOTE_ON          = "msg2_cv_note_on";
	public static final String MSG2_CV_POLY_PRESSURE    = "msg2_cv_poly_pressure";
	public static final String MSG2_CV_CONTROL_CHANGE   = "msg2_cv_control_change";  // MSG3_C_*
	public static final String MSG2_CV_PROGRAM_CHANGE   = "msg2_cv_program_change";
	public static final String MSG2_CV_CHANNEL_PRESSURE = "msg2_cv_channel_pressure";
	public static final String MSG2_CV_PITCH_BEND       = "msg2_cv_pitch_band";
	public static final String MSG2_CM_ALL_SOUND_OFF    = "msg3_cm_all_sound_off";
	public static final String MSG2_CM_ALL_CTRLS_OFF    = "msg3_cm_all_ctrls_off";
	public static final String MSG2_CM_LOCAL_CTRL       = "msg3_cm_local_ctrl";
	public static final String MSG2_CM_ALL_NOTES_OFF    = "msg3_cm_all_notes_off";
	public static final String MSG2_CM_OMNI_MODE_OFF    = "msg3_cm_omni_mode_on";
	public static final String MSG2_CM_OMNI_MODE_ON     = "msg3_cm_omni_mode_off";
	public static final String MSG2_CM_MONO_NOTES_OFF   = "msg3_cm_mono_notes_off";
	public static final String MSG2_CM_POLY_NOTES_OFF   = "msg3_cm_poly_notes_off";
	public static final String MSG2_SC_SYSEX            = "msg2_sc_sysex";          // MSG3_SX_*
	public static final String MSG2_SC_MIDI_TIME_CODE   = "msg2_sc_midi_time_code";
	public static final String MSG2_SC_SONG_POS_POINTER = "msg2_sc_song_pos_pointer";
	public static final String MSG2_SC_SONG_SELECT      = "msg2_sc_song_select";
	public static final String MSG2_SC_TUNE_REQUEST     = "msg2_sc_tune_request";
	public static final String MSG2_SC_END_OF_SYSEX     = "msg2_sc_end_of_sysex";
	public static final String MSG2_SR_TIMING_CLOCK     = "msg2_sr_timing_clock";
	public static final String MSG2_SR_START            = "msg2_sr_start";
	public static final String MSG2_SR_CONTINUE         = "msg2_sr_continue";
	public static final String MSG2_SR_STOP             = "msg2_sr_stop";
	public static final String MSG2_SR_ACTIVE_SENSING   = "msg2_sr_active_sensing";
	public static final String MSG2_SR_SYSTEM_RESET     = "msg2_sr_system_reset";
	public static final String MSG2_M_SEQUENCE_NUMBER   = "msg2_m_sequence_number";
	public static final String MSG2_M_TEXT              = "msg2_m_text";
	public static final String MSG2_M_COPYRIGHT         = "msg2_m_copyright";
	public static final String MSG2_M_TRACK_NAME        = "msg2_m_track_name";
	public static final String MSG2_M_INSTRUMENT_NAME   = "msg2_m_instrument_name";
	public static final String MSG2_M_LYRICS            = "msg2_m_lyrics";
	public static final String MSG2_M_MARKER            = "msg2_m_marker";
	public static final String MSG2_M_CUE_POINT         = "msg2_m_cue_point";
	public static final String MSG2_M_CHANNEL_PREFIX    = "msg2_m_channel_prefix";
	public static final String MSG2_M_MIDI_PORT         = "msg2_m_midi_port";
	public static final String MSG2_M_END_OF_SEQUENCE   = "msg2_m_end_of_sequence";
	public static final String MSG2_M_SET_TEMPO         = "msg2_m_set_tempo";
	public static final String MSG2_M_SMPTE_OFFSET      = "msg2_m_smpte_offset";
	public static final String MSG2_M_TIME_SIGNATURE    = "msg2_m_time_signature";
	public static final String MSG2_M_KEY_SIGNATURE     = "msg2_m_key_signature";
	public static final String MSG2_M_SEQUENCER_SPEC    = "msg2_m_sequencer_spec";
	// MIDI messages for InfoView - level 3
	public static final String MSG3_C_BANK_SELECT        = "msg3_c_bank_select";      // ..._[MSB|LSB]
	public static final String MSG3_C_MODULATION_WHEEL   = "msg3_c_modulation_wheel"; // ..._[MSB|LSB]
	public static final String MSG3_C_BREATH_CTRL        = "msg3_c_breath_ctrl";      // ..._[MSB|LSB]
	public static final String MSG3_C_FOOT_CTRL          = "msg3_c_foot_ctrl";        // ..._[MSB|LSB]
	public static final String MSG3_C_PORTAMENTO_TIME    = "msg3_c_portamento_time";  // ..._[MSB|LSB]
	public static final String MSG3_C_DATA_ENTRY         = "msg3_c_data_entry";       // MSG4_(N)RPN_*
	public static final String MSG3_C_CHANNEL_VOL        = "msg3_c_channel_vol";      // ..._[MSB|LSB]
	public static final String MSG3_C_BALANCE            = "msg3_c_balance";          // ..._[MSB|LSB]
	public static final String MSG3_C_PAN                = "msg3_c_pan";              // ..._[MSB|LSB]
	public static final String MSG3_C_EXPRESSION         = "msg3_c_expression";       // ..._[MSB|LSB]
	public static final String MSG3_C_EFFECT_CTRL_1      = "msg3_c_effect_ctrl_1";    // ..._[MSB|LSB]
	public static final String MSG3_C_EFFECT_CTRL_2      = "msg3_c_effect_ctrl_2";    // ..._[MSB|LSB]
	public static final String MSG3_C_GEN_PURP_CTRL_1    = "msg3_c_gen_purp_ctrl_1";  // ..._[MSB|LSB]
	public static final String MSG3_C_GEN_PURP_CTRL_2    = "msg3_c_gen_purp_ctrl_2";  // ..._[MSB|LSB]
	public static final String MSG3_C_GEN_PURP_CTRL_3    = "msg3_c_gen_purp_ctrl_3";  // ..._[MSB|LSB]
	public static final String MSG3_C_GEN_PURP_CTRL_4    = "msg3_c_gen_purp_ctrl_4";  // ..._[MSB|LSB]
	public static final String MSG3_C_HOLD_PEDAL_1       = "msg3_c_hold_pedal_1";
	public static final String MSG3_C_PORTAMENTO_PEDAL   = "msg3_c_portamento_pedal";
	public static final String MSG3_C_SOSTENUTO_PEDAL    = "msg3_c_sostenuto_pedal";
	public static final String MSG3_C_SOFT_PEDAL         = "msg3_c_soft_pedal";
	public static final String MSG3_C_LEGATO_PEDAL       = "msg3_c_legato_pedal";
	public static final String MSG3_C_HOLD_PEDAL_2       = "msg3_c_hold_pedal_2";
	public static final String MSG3_C_SOUND_CTRL_1       = "msg3_c_sound_ctrl_1";
	public static final String MSG3_C_SOUND_CTRL_2       = "msg3_c_sound_ctrl_2";
	public static final String MSG3_C_SOUND_CTRL_3       = "msg3_c_sound_ctrl_3";
	public static final String MSG3_C_SOUND_CTRL_4       = "msg3_c_sound_ctrl_4";
	public static final String MSG3_C_SOUND_CTRL_5       = "msg3_c_sound_ctrl_5";
	public static final String MSG3_C_SOUND_CTRL_6       = "msg3_c_sound_ctrl_6";
	public static final String MSG3_C_SOUND_CTRL_7       = "msg3_c_sound_ctrl_7";
	public static final String MSG3_C_SOUND_CTRL_8       = "msg3_c_sound_ctrl_8";
	public static final String MSG3_C_SOUND_CTRL_9       = "msg3_c_sound_ctrl_9";
	public static final String MSG3_C_SOUND_CTRL_10      = "msg3_c_sound_ctrl_10";
	public static final String MSG3_C_GEN_PURP_CTRL_5    = "msg3_c_gen_purp_5";
	public static final String MSG3_C_GEN_PURP_CTRL_6    = "msg3_c_gen_purp_6";
	public static final String MSG3_C_GEN_PURP_CTRL_7    = "msg3_c_gen_purp_7";
	public static final String MSG3_C_GEN_PURP_CTRL_8    = "msg3_c_gen_purp_8";
	public static final String MSG3_C_PORTAMENTO_CTRL    = "msg3_c_portamento_ctrl";
	public static final String MSG3_C_HI_RES_VELO_PRFX   = "msg3_c_hi_res_velo_prfx";
	public static final String MSG3_C_EFFECT_1_DEPTH     = "msg3_c_effect_1_depth";
	public static final String MSG3_C_EFFECT_2_DEPTH     = "msg3_c_effect_2_depth";
	public static final String MSG3_C_EFFECT_3_DEPTH     = "msg3_c_effect_3_depth";
	public static final String MSG3_C_EFFECT_4_DEPTH     = "msg3_c_effect_4_depth";
	public static final String MSG3_C_EFFECT_5_DEPTH     = "msg3_c_effect_5_depth";
	public static final String MSG3_C_DATA_BUTTON_INCR   = "msg3_c_data_button_incr"; // MSG4_(N)RPN_*
	public static final String MSG3_C_DATA_BUTTON_DECR   = "msg3_c_data_button_decr"; // MSG4_(N)RPN_*
	public static final String MSG3_C_NRPN               = "msg3_c_nrpn";             // ..._[MSB|LSB]
	public static final String MSG3_C_RPN                = "msg3_c_rpn";              // ..._[MSB|LSB]
	public static final String MSG3_SX_NON_RT_UNIVERSAL  = "msg3_sx_non_rt_universal"; // MSG4_SX_NU_*
	public static final String MSG3_SX_RT_UNIVERSAL      = "msg3_sx_rt_universal";     // MSG4_SX_RU_*
	public static final String MSG3_SX_EDUCATIONAL       = "msg3_sx_educational";
	public static final String MSG3_SX_VENDOR            = "msg3_sx_vendor";           // MSG4_SX_V_*
	// MIDI messages for InfoView - level 4
	public static final String MSG4_C_BANK_SELECT_MSB      = "msg4_c_bank_select_msb";
	public static final String MSG4_C_MODULATION_WHEEL_MSB = "msg4_c_modulation_wheel_msb";
	public static final String MSG4_C_BREATH_CTRL_MSB      = "msg4_c_breath_ctrl_msb";
	public static final String MSG4_C_FOOT_CTRL_MSB        = "msg4_c_foot_ctrl_msb";
	public static final String MSG4_C_PORTAMENTO_TIME_MSB  = "msg4_c_portamento_time_msb";
	public static final String MSG4_C_DATA_ENTRY_MSB       = "msg4_c_data_entry_msb"; // actually level 5
	public static final String MSG4_C_CHANNEL_VOL_MSB      = "msg4_c_channel_vol_msb";
	public static final String MSG4_C_BALANCE_MSB          = "msg4_c_balance_msb";
	public static final String MSG4_C_PAN_MSB              = "msg4_c_pan_msb";
	public static final String MSG4_C_EXPRESSION_MSB       = "msg4_c_expression_msb";
	public static final String MSG4_C_EFFECT_CTRL_1_MSB    = "msg4_c_effect_ctrl_1_msb";
	public static final String MSG4_C_EFFECT_CTRL_2_MSB    = "msg4_c_effect_ctrl_2_msb";
	public static final String MSG4_C_GEN_PURP_CTRL_1_MSB  = "msg4_c_gen_purp_1_msb";
	public static final String MSG4_C_GEN_PURP_CTRL_2_MSB  = "msg4_c_gen_purp_2_msb";
	public static final String MSG4_C_GEN_PURP_CTRL_3_MSB  = "msg4_c_gen_purp_3_msb";
	public static final String MSG4_C_GEN_PURP_CTRL_4_MSB  = "msg4_c_gen_purp_4_msb";
	public static final String MSG4_C_BANK_SELECT_LSB      = "msg4_c_bank_select_lsb";
	public static final String MSG4_C_MODULATION_WHEEL_LSB = "msg4_c_modulation_wheel_lsb";
	public static final String MSG4_C_BREATH_CTRL_LSB      = "msg4_c_breath_ctrl_lsb";
	public static final String MSG4_C_FOOT_CTRL_LSB        = "msg4_c_foot_ctrl_lsb";
	public static final String MSG4_C_PORTAMENTO_TIME_LSB  = "msg4_c_portamento_time_lsb";
	public static final String MSG4_C_DATA_ENTRY_LSB       = "msg4_c_data_entry_lsb";  // actually level 5
	public static final String MSG4_C_CHANNEL_VOL_LSB      = "msg4_c_channel_vol_lsb";
	public static final String MSG4_C_BALANCE_LSB          = "msg4_c_balance_lsb";
	public static final String MSG4_C_PAN_LSB              = "msg4_c_pan_lsb";
	public static final String MSG4_C_EXPRESSION_LSB       = "msg4_c_expression_lsb";
	public static final String MSG4_C_EFFECT_CTRL_1_LSB    = "msg4_c_effect_ctrl_1_lsb";
	public static final String MSG4_C_EFFECT_CTRL_2_LSB    = "msg4_c_effect_ctrl_2_lsb";
	public static final String MSG4_C_GEN_PURP_CTRL_1_LSB  = "msg4_c_gen_purp_1_lsb";
	public static final String MSG4_C_GEN_PURP_CTRL_2_LSB  = "msg4_c_gen_purp_2_lsb";
	public static final String MSG4_C_GEN_PURP_CTRL_3_LSB  = "msg4_c_gen_purp_3_lsb";
	public static final String MSG4_C_GEN_PURP_CTRL_4_LSB  = "msg4_c_gen_purp_4_lsb";
	public static final String MSG4_RPN_PITCH_BEND_SENS    = "msg4_rpn_pitch_band_sens";       // MSG5_C_RPN_[MSB|LSB]
	public static final String MSG4_RPN_MASTER_FINE_TUN    = "msg4_rpn_master_fine_tuning";    // MSG5_C_RPN_[MSB|LSB]
	public static final String MSG4_RPN_MASTER_COARSE_TUN  = "msg4_rpn_master_coarse_tuning";  // MSG5_C_RPN_[MSB|LSB]
	public static final String MSG4_RPN_TUN_PROG_CHANGE    = "msg4_rpn_tuning_program_change"; // MSG5_C_RPN_[MSB|LSB]
	public static final String MSG4_RPN_TUN_BANK_SELECT    = "msg4_rpn_tuning_bank_select";    // MSG5_C_RPN_[MSB|LSB]
	public static final String MSG4_RPN_MOD_DEPTH_RANGE    = "msg4_rpn_mod_depth_range";       // MSG5_C_RPN_[MSB|LSB]
	public static final String MSG4_RPN_MPE_CONFIG         = "msg4_rpn_mpe_config";            // MSG5_C_RPN_[MSB|LSB]
	public static final String MSG4_RPN_AZIMUTH_ANGLE      = "msg4_rpn_azimuth_angle";         // MSG5_C_RPN_[MSB|LSB]
	public static final String MSG4_RPN_ELEVATION_ANGLE    = "msg4_rpn_elevation_angle";       // MSG5_C_RPN_[MSB|LSB]
	public static final String MSG4_RPN_GAIN               = "msg4_rpn_gain";                  // MSG5_C_RPN_[MSB|LSB]
	public static final String MSG4_RPN_DISTANCE_RATIO     = "msg4_rpn_distance_ratio";        // MSG5_C_RPN_[MSB|LSB]
	public static final String MSG4_RPN_MAXIMUM_DISTANCE   = "msg4_rpn_maximum_distance";      // MSG5_C_RPN_[MSB|LSB]
	public static final String MSG4_RPN_GAIN_AT_MAX_DIST   = "msg4_rpn_gain_at_max_dist";      // MSG5_C_RPN_[MSB|LSB]
	public static final String MSG4_RPN_REF_DISTANCE_RATIO = "msg4_rpn_ref_distance_ratio";    // MSG5_C_RPN_[MSB|LSB]
	public static final String MSG4_RPN_PAN_SPREAD_ANGLE   = "msg4_rpn_pan_spread_angle";      // MSG5_C_RPN_[MSB|LSB]
	public static final String MSG4_RPN_ROLL_ANGLE         = "msg4_rpn_roll_angle";            // MSG5_C_RPN_[MSB|LSB]
	public static final String MSG4_SX_NU_SMPL_DUMP_HDR    = "msg4_sx_nu_smpl_dump_hdr";
	public static final String MSG4_SX_NU_SMPL_DATA_PKT    = "msg4_sx_nu_smpl_data_pkt";
	public static final String MSG4_SX_NU_SMPL_DUMP_REQ    = "msg4_sx_nu_smpl_dump_req";
	public static final String MSG4_SX_NU_MIDI_TIME_CODE   = "msg4_sx_nu_midi_time_code";      // MSG5_SXN4_*
	public static final String MSG4_SX_NU_SAMPLE_DUMP_EXT  = "msg4_sx_nu_sample_dump_ext";     // MSG5_SXN5_*
	public static final String MSG4_SX_NU_GENERAL_INFO     = "msg4_sx_nu_general_info";        // MSG5_SXN6_*
	public static final String MSG4_SX_NU_FILE_DUMP        = "msg4_sx_nu_file_dump";           // MSG5_SXN7_*
	public static final String MSG4_SX_NU_TUNING_STANDARD  = "msg4_sx_nu_tuning_standard";     // MSG5_SXN8_*
	public static final String MSG4_SX_NU_GENERA_MIDI      = "msg4_sx_nu_general_midi";        // MSG5_SXN9_*
	public static final String MSG4_SX_NU_DOWNLOADABLE_SND = "msg4_sx_nu_downloadable_snd";    // MSG5_SXNA_*
	public static final String MSG4_SX_NU_FILE_REF_MSG     = "msg4_sx_nu_file_ref_msg";        // MSG5_SXNB_*
	public static final String MSG4_SX_NU_MIDI_VISUAL_CTRL = "msg4_sx_nu_midi_visual_ctrl";    // MSG5_SXNC_*
	public static final String MSG4_SX_NU_END_OF_FILE      = "msg4_sx_nu_end_of_file";
	public static final String MSG4_SX_NU_WAIT             = "msg4_sx_nu_wait";
	public static final String MSG4_SX_NU_CANCEL           = "msg4_sx_nu_cancel";
	public static final String MSG4_SX_NU_NAK              = "msg4_sx_nu_nak";
	public static final String MSG4_SX_NU_ACK              = "msg4_sx_nu_ack";
	public static final String MSG4_SX_NU_MIDI_CI          = "msg5_sx_nu_midi_ci";             // MSG5_SXND_*  MSG6_SXND??_*
	public static final String MSG4_SX_RU_MIDI_TIME_CODE   = "msg4_sx_ru_midi_time_code";      // MSG5_SXR1_*
	public static final String MSG4_SX_RU_MIDI_SHOW_CTRL   = "msg4_sx_ru_midi_show_ctrl";      // MSG5_SXR2_*
	public static final String MSG4_SX_RU_NOTATION_INFO    = "msg4_sx_ru_nonation_info";       // MSG5_SXR3_*
	public static final String MSG4_SX_RU_DEVICE_CTRL      = "msg4_sx_ru_device_ctrl";         // MSG5_SXR4_*
	public static final String MSG4_SX_RU_RT_MTC_CUEING    = "msg4_sx_ru_rt_mtc_cueing";       // MSG5_SXR5_*
	public static final String MSG4_SX_RU_MACH_CTRL_CMD    = "msg4_sx_ru_mach_ctrl_cmd";       // MSG5_SXR6_*
	public static final String MSG4_SX_RU_MACH_CTRL_RES    = "msg4_sx_ru_mach_ctrl_res";       // MSG5_SXR7_*
	public static final String MSG4_SX_RU_TUNING_STANDARD  = "msg4_sx_ru_tuning_standard";     // MSG5_SXR8_*
	public static final String MSG4_SX_RU_CTRL_DEST_SET    = "msg4_sx_ru_ctrl_dest_set";       // MSG5_SXR9_*
	public static final String MSG4_SX_RU_KEY_B_INSTR_CTRL = "msg4_sx_ru_key_b_instr_ctrl";
	public static final String MSG4_SX_RU_SCAL_POLY_MIP    = "msg4_sx_ru_scal_poly_mip";
	public static final String MSG4_SX_RU_MOB_PHONE_CTRL   = "msg4_sx_ru_mob_phone_ctrl";
	// MIDI messages for InfoView - level 5
	public static final String MSG5_C_NRPN_LSB             = "msg5_c_nrpm_lsb";
	public static final String MSG5_C_NRPN_MSB             = "msg5_c_nrpm_msb";
	public static final String MSG5_C_RPN_LSB              = "msg5_c_rpm_lsb";
	public static final String MSG5_C_RPN_MSB              = "msg5_c_rpm_msb";
	
	public static final String MSG5_SXN4_SPECIAL           = "msg5_sxn4_special";
	public static final String MSG5_SXN4_PUNCH_IN_PTS      = "msg5_sxn4_punch_in_pts";
	public static final String MSG5_SXN4_PUNCH_OUT_PTS     = "msg5_sxn4_punch_out_pts";
	public static final String MSG5_SXN4_DEL_PUNCH_IN_PTS  = "msg5_sxn4_del_punch_in_pts";
	public static final String MSG5_SXN4_DEL_PUNCH_OUT_PTS = "msg5_sxn4_del_punch_out_pts";
	public static final String MSG5_SXN4_EVT_START_PT      = "msg5_sxn4_evt_start_pt";
	public static final String MSG5_SXN4_EVT_STOP_PT       = "msg5_sxn4_evt_stop_pt";
	public static final String MSG5_SXN4_EVT_START_PTS_ADD = "msg5_sxn4_evt_start_pts_add";
	public static final String MSG5_SXN4_EVT_STOP_PTS_ADD  = "msg5_sxn4_evt_stop_pts_add";
	public static final String MSG5_SXN4_DEL_EVT_START_PT  = "msg5_sxn4_del_ev_start_pt";
	public static final String MSG5_SXN4_DEL_EVT_STOP_PT   = "msg5_sxn4_del_ev_stop_pt";
	public static final String MSG5_SXN4_CUE_PTS           = "msg5_sxn4_cue_pts";
	public static final String MSG5_SXN4_CUE_PTS_ADD       = "msg5_sxn4_cue_pts_add";
	public static final String MSG5_SXN4_DEL_CUE_PT        = "msg5_sxn4_del_cue_pt";
	public static final String MSG5_SXN4_EVT_NAME_IN_ADD   = "msg5_sxn4_evt_name_in_add";
	public static final String MSG5_SXN5_LOOP_PTS_TRANSM   = "msg5_sxn5_loop_pts_transm";
	public static final String MSG5_SXN5_LOOP_PTS_REQ      = "msg5_sxn5_loop_pts_req";
	public static final String MSG5_SXN5_SMPL_NAME_TRANSM  = "msg5_sxn5_smpl_name_transm";
	public static final String MSG5_SXN5_SMPL_NAME_REQ     = "msg5_sxn5_smpl_name_req";
	public static final String MSG5_SXN5_EXT_DUMP_HDR      = "msg5_sxn5_ext_dump_hdr";
	public static final String MSG5_SXN5_EXT_LOOP_PTS_TR   = "msg5_sxn5_ext_loop_pts_tr";
	public static final String MSG5_SXN5_EXT_LOOP_PTS_REQ  = "msg5_sxn5_ext_loop_pts_req";
	public static final String MSG5_SXN6_IDENTITY_REQ      = "msg5_sxn6_identity_req";
	public static final String MSG5_SXN6_IDENTITY_REPL     = "msg5_sxn6_identity_repl";
	public static final String MSG5_SXN7_HEADER            = "msg5_sxn7_header";
	public static final String MSG5_SXN7_DATA_PACKET       = "msg5_sxn7_data_packet";
	public static final String MSG5_SXN7_REQUEST           = "msg5_sxn7_request";
	public static final String MSG5_SXN8_BLK_DUMP_REQ      = "msg5_sxn8_blk_dump_req";
	public static final String MSG5_SXN8_BLK_DUMP_REPL     = "msg5_sxn8_blk_dump_repl";
	public static final String MSG5_SXN8_TUNING_DUMP_REQ   = "msg5_sxn8_tunin_dump_req";
	public static final String MSG5_SXN8_KEY_B_TUNING_DMP  = "msg5_sxn8_key_b_tuning_dmp";
	public static final String MSG5_SXN8_SO_TUN_DMP_1      = "msg5_sxn8_so_tun_dmp_1";
	public static final String MSG5_SXN8_SO_TUN_DMP_2      = "msg5_sxn8_so_tun_dmp_2";
	public static final String MSG5_SXN8_SG_TUN_CH_BNK_SEL = "msg5_sxn8_sg_tun_ch_bnk_sel";
	public static final String MSG5_SXN8_SO_TUN_1          = "msg5_sxn8_so_tun_1";
	public static final String MSG5_SXN8_SO_TUN_2          = "msg5_sxn8_so_tun_2";
	public static final String MSG5_SXN9_GM_DISABLE        = "msg5_sxn9_gm_disable";
	public static final String MSG5_SXN9_GM_1_ON           = "msg5_sxn9_gm_1_on";
	public static final String MSG5_SXN9_GM_OFF            = "msg5_sxn9_gm_off";
	public static final String MSG5_SXN9_GM_2_ON           = "msg5_sxn9_gm_2_on";
	public static final String MSG5_SXNA_DLS_ON            = "msg5_sxna_dls_on";
	public static final String MSG5_SXNA_DLS_OFF           = "msg5_sxna_dls_off";
	public static final String MSG5_SXNA_DLS_VA_OFF        = "msg5_sxna_dls_va_off";
	public static final String MSG5_SXNA_DLS_VA_ON         = "msg5_sxna_dls_va_on";
	public static final String MSG5_SXNB_OPEN_FILE         = "msg5_sxnb_open_file";
	public static final String MSG5_SXNB_SEL_RESEL_CONT    = "msg5_sxnb_sel_resel_cont";
	public static final String MSG5_SXNB_OPEN_SEL_CONT     = "msg5_sxnb_open_sel_cont";
	public static final String MSG5_SXNB_CLOSE_FILE        = "msg5_sxnb_close_file";
	public static final String MSG5_SXNC_MVC_CMD           = "msg5_sxnc_mvc_cmd";
	public static final String MSG5_SXND_CI_RESERVED       = "msg5_sxnd_ci_reserved";
	public static final String MSG5_SXND_CI_PROTO_NEGO     = "msg5_sxnd_ci_proto_nego";        // MSG6_SXND1?_*
	public static final String MSG5_SXND_CI_PROF_CONF      = "msg5_sxnd_ci_prof_conf";         // MSG6_SXND2?_*
	public static final String MSG5_SXND_CI_PROP_EXCH      = "msg5_sxnd_ci_prop_exch";         // MSG6_SXND3?_*
	public static final String MSG5_SXND_CI_MGMT_MSG       = "msg5_sxnd_ci_mgmt_msg";          // MSG6_SXND7?_*
	
	public static final String MSG5_SXR1_FULL_MSG          = "msg5_sxr1_full_msg";
	public static final String MSG5_SXR1_USER_BITS         = "msg5_sxr1_user_bits";
	public static final String MSG5_SXR2_MSC_EXT           = "msg5_sxr2_msc_ext";
	public static final String MSG5_SXR2_MSC_CMD           = "msg5_sxr2_msc_cmd";
	public static final String MSG5_SXR3_BAR_NUMBER        = "msg5_sxr3_bar_number";
	public static final String MSG5_SXR3_TIME_SIG_IMMED    = "msg5_sxr3_time_sig_immed";
	public static final String MSG5_SXR3_TIME_SIG_DELAYED  = "msg5_sxr3_time_sig_delayed";
	public static final String MSG5_SXR4_MASTER_VOLUME     = "msg5_sxr4_master_volume";
	public static final String MSG5_SXR4_MASTER_BALANCE    = "msg5_sxr4_master_balance";
	public static final String MSG5_SXR4_MASTER_FINE_TUN   = "msg5_sxr4_master_fine_tun";
	public static final String MSG5_SXR4_MASTER_COARSE_TUN = "msg5_sxr4_master_coarse_tun";
	public static final String MSG5_SXR4_GLOBAL_PARAM_CTRL = "msg5_sxr4_global_param_ctrl";
	public static final String MSG5_SXR5_SPECIAL           = "msg5_sxr5_special";
	public static final String MSG5_SXR5_PUNCH_IN_PTS      = "msg5_sxr5_punch_in_pts";
	public static final String MSG5_SXR5_PUNCH_OUT_PTS     = "msg5_sxr5_punch_out_pts";
	public static final String MSG5_SXR5_EVT_START_PT      = "msg5_sxr5_evt_start_pt";
	public static final String MSG5_SXR5_EVT_STOP_PT       = "msg5_sxr5_evt_stop_pt";
	public static final String MSG5_SXR5_EVT_START_PTS_ADD = "msg5_sxr5_evt_start_pts_add";
	public static final String MSG5_SXR5_EVT_STOP_PTS_ADD  = "msg5_sxr5_evt_stop_pts_add";
	public static final String MSG5_SXR5_CUE_PTS           = "msg5_sxr5_cue_pts";
	public static final String MSG5_SXR5_CUE_PTS_ADD       = "msg5_sxr5_cue_pts_add";
	public static final String MSG5_SXR5_EVT_NAME_IN_ADD   = "msg5_sxr5_evt_name_in_add";
	public static final String MSG5_SXR6_STOP              = "msg5_sxr6_stop";
	public static final String MSG5_SXR6_PLAY              = "msg5_sxr6_play";
	public static final String MSG5_SXR6_DEF_PLAY          = "msg5_sxr6_def_play";
	public static final String MSG5_SXR6_FAST_FW           = "msg5_sxr6_fast_fw";
	public static final String MSG5_SXR6_REWIND            = "msg5_sxr6_rewind";
	public static final String MSG5_SXR6_REC_STROBE        = "msg5_sxr6_rec_strobe";
	public static final String MSG5_SXR6_REC_EXIT          = "msg5_sxr6_rec_exit";
	public static final String MSG5_SXR6_REC_PAUSE         = "msg5_sxr6_rec_pause";
	public static final String MSG5_SXR6_PAUSE             = "msg5_sxr6_pause";
	public static final String MSG5_SXR6_EJECT             = "msg5_sxr6_eject";
	public static final String MSG5_SXR6_CHASE             = "msg5_sxr6_chase";
	public static final String MSG5_SXR6_CMD_ERR_RESET     = "msg5_sxr6_cmd_err_reset";
	public static final String MSG5_SXR6_MMC_RESET         = "msg5_sxr6_mmc_reset";
	public static final String MSG5_SXR6_WRITE             = "msg5_sxr6_write";
	public static final String MSG5_SXR6_GOTO              = "msg5_sxr6_goto";
	public static final String MSG5_SXR6_SHUTTLE           = "msg5_sxr6_shuttle";
	public static final String MSG5_SXR7_MMC_RES           = "msg5_sxr7_mmc_res";
	public static final String MSG5_SXR8_SG_TUN_CH         = "msg5_sxr8_sg_tun_ch";
	public static final String MSG5_SXR8_SG_TUN_CH_BNK_SEL = "msg5_sxr8_sg_tun_ch_bnk_sel";
	public static final String MSG5_SXR8_SO_TUN_1          = "msg5_sxr8_so_tun_1";
	public static final String MSG5_SXR8_SO_TUN_2          = "msg5_sxr8_so_tun_2";
	public static final String MSG5_SXR9_CHANNEL_PRESSURE  = "msg5_sxr9_channel_pressure";
	public static final String MSG5_SXR9_POLY_KEY_PRESSURE = "msg5_sxr9_poly_key_pressure";
	public static final String MSG5_SXR9_CTRL              = "msg5_sxr9_ctrl";
	
	// MIDI messages for InfoView - level 6
	public static final String MSG6_SXND10_INIT_PROTO_NEGO   = "msg6_sxnd10_init_proto_nego";
	public static final String MSG6_SXND11_INIT_PROTO_REPL   = "msg6_sxnd11_init_proto_repl";
	public static final String MSG6_SXND12_SET_NEW_PROTO     = "msg6_sxnd12_set_new_proto";
	public static final String MSG6_SXND13_TEST_NEW_PROT_ITR = "msg6_sxnd13_test_new_proto_itr";
	public static final String MSG6_SXND14_TEST_NEW_PROT_RTI = "msg6_sxnd14_test_new_proto_rti";
	public static final String MSG6_SXND15_CONF_NEW_PROT_EST = "msg6_sxnd15_conf_new_proto_est";
	public static final String MSG6_SXND20_PROF_INQ          = "msg6_sxnd20_prof_inq";
	public static final String MSG6_SXND21_PROF_INQ_REPL     = "msg6_sxnd21_prof_inq_repl";
	public static final String MSG6_SXND22_SET_PROF_ON       = "msg6_sxnd22_prof_on";
	public static final String MSG6_SXND23_SET_PROF_OFF      = "msg6_sxnd23_prof_off";
	public static final String MSG6_SXND24_PROF_ENABL_RPRT   = "msg6_sxnd24_enabl_rprt";
	public static final String MSG6_SXND25_PROF_DISABL_RPRT  = "msg6_sxnd25_disabl_rprt";
	public static final String MSG6_SXND2F_PROF_SPEC_DATA    = "msg6_sxnd2f_spec_data";
	public static final String MSG6_SXND30_PROP_EXCH_CAP_INQ  = "msg6_sxnd30_prop_exch_cap_inq";
	public static final String MSG6_SXND31_PROP_EXCH_CAP_REPL = "msg6_sxnd31_prop_exch_cap_repl";
	public static final String MSG6_SXND32_HAS_PROP_DATA_INQ  = "msg6_sxnd32_has_prop_data_inq";
	public static final String MSG6_SXND33_HAS_PROP_DATA_REPL = "msg6_sxnd33_has_prop_data_repl";
	public static final String MSG6_SXND34_GET_PROP_DATA_INQ  = "msg6_sxnd34_get_prop_data_inq";
	public static final String MSG6_SXND35_GET_PROP_DATA_REPL = "msg6_sxnd35_get_prop_data_repl";
	public static final String MSG6_SXND36_SET_PROP_DATA_INQ  = "msg6_sxnd36_set_prop_data_inq";
	public static final String MSG6_SXND37_SET_PROP_DATA_REPL = "msg6_sxnd37_set_prop_data_repl";
	public static final String MSG6_SXND38_SUBSCRIPTION       = "msg6_sxnd38_subscription";
	public static final String MSG6_SXND39_SUBSCRIPTION_REPL  = "msg6_sxnd39_subscription_repl";
	public static final String MSG6_SXND3F_NOTIFY             = "msg6_sxnd3f_notify";
	public static final String MSG6_SXND70_DISCOVERY          = "msg6_sxnd70_discovery";
	public static final String MSG6_SXND71_DISCOVERY_RESP     = "msg6_sxnd71_discovery_repl";
	public static final String MSG6_SXND7E_INVAL_MUID         = "msg6_sxnd7e_inval_muid";
	public static final String MSG6_SXND7F_NAK                = "msg6_sxnd7f_nak";
	
	// MessageClassifier: message description
	public static final String MSG_DESC_MEANING            = "msg_desc_meaning";
	public static final String MSG_DESC_VALUE              = "msg_desc_value";
	public static final String MSG_DESC_ON                 = "msg_desc_on";
	public static final String MSG_DESC_OFF                = "msg_desc_off";
	public static final String MSG_DESC_89A_NOTE           = "msg_desc_89a_note";
	public static final String MSG_DESC_89A_VELOCITY       = "msg_desc_89a_velocity";
	public static final String MSG_DESC_89AD_PRESSURE      = "msg_desc_89ad_pressure";
	public static final String MSG_DESC_B_POS_MEANINGS     = "msg_desc_b_pos_meanings";
	public static final String MSG_DESC_B_FOR_PARAM        = "msg_desc_b_for_param";
	public static final String MSG_DESC_B_RPN_MSB_00       = "msg_desc_b_rpm_msb_00";
	public static final String MSG_DESC_B_RPN_MSB_3D       = "msg_desc_b_rpm_msb_3d";
	public static final String MSG_DESC_B_RPN_LSB_00       = "msg_desc_b_rpm_lsb_00";
	public static final String MSG_DESC_B_RPN_LSB_01       = "msg_desc_b_rpm_lsb_01";
	public static final String MSG_DESC_B_RPN_LSB_02       = "msg_desc_b_rpm_lsb_02";
	public static final String MSG_DESC_B_RPN_LSB_03       = "msg_desc_b_rpm_lsb_03";
	public static final String MSG_DESC_B_RPN_LSB_04       = "msg_desc_b_rpm_lsb_04";
	public static final String MSG_DESC_B_RPN_LSB_05       = "msg_desc_b_rpm_lsb_05";
	public static final String MSG_DESC_B_RPN_LSB_06       = "msg_desc_b_rpm_lsb_06";
	public static final String MSG_DESC_B_RPN_LSB_07       = "msg_desc_b_rpm_lsb_07";
	public static final String MSG_DESC_B_RPN_LSB_08       = "msg_desc_b_rpm_lsb_08";
	public static final String MSG_DESC_B_RPN_NRPN_7F      = "msg_desc_b_rpm_nrpn_7f";
	public static final String MSG_DESC_B_START_NOTE       = "msg_desc_b_start_note";
	public static final String MSG_DESC_C_PROGRAM          = "msg_desc_c_program";
	public static final String MSG_DESC_C_INSTRUMENT       = "msg_desc_c_instrument";
	public static final String MSG_DESC_C_DRUMKIT          = "msg_desc_c_drumkit";
	public static final String MSG_DESC_E_GENERAL_DESC     = "msg_desc_e_general_desc";
	public static final String MSG_DESC_E_CURRENT_SENS     = "msg_desc_e_current_sens";
	public static final String MSG_DESC_E_HALF_TONE        = "msg_desc_e_half_tone";
	public static final String MSG_DESC_F_TEMPO_MPQ        = "msg_desc_f_tempo_mpq";
	public static final String MSG_DESC_F_TEMPO_BPM        = "msg_desc_f_tempo_bpm";
	public static final String MSG_DESC_F_BPM              = "msg_desc_f_bpm";
	public static final String MSG_DESC_F_KEY_SIG_SHARPS   = "msg_desc_f_key_sig_sharps";
	public static final String MSG_DESC_F_KEY_SIG_FLATS    = "msg_desc_f_key_sig_flats";
	public static final String MSG_DESC_F_KEY_SIG_NONE     = "msg_desc_f_key_sig_none";
	public static final String MSG_DESC_F_UNKNOWN_TONALITY = "msg_desc_f_unknown_tonality";
	
	// UiControler + PlayerControler
	public static final String ERROR_IN_LINE               = "parsing_error_in_line";
	
	// SoundbankParser // TODO: rename from SoundfontParser
	public static final String UNKNOWN_SOUND_EXT           = "unknown_sound_ext";
	public static final String INVALID_URL                 = "invalid_url";
	public static final String UNKNOWN_HOST                = "unknown_host";
	public static final String SOUND_FORMAT_FAILED         = "sound_format_failed";
	public static final String INVALID_RIFF                = "invalid_riff";
	public static final String CANNOT_OPEN_SOUND           = "cannot_open_sound";
	public static final String DOWNLOAD_PROBLEM            = "download_problem";
	public static final String COULDNT_CREATE_CACHE_DIR    = "couldnt_create_cache_dir";
	public static final String SOUND_FROM_FILE             = "sound_from_file";
	public static final String SOUND_FROM_URL              = "sound_from_url";
	
	// SequenceParser
	public static final String ERROR_NOTE_TOO_BIG          = "error_note_too_big";
	public static final String ERROR_NOTE_TOO_SMALL        = "error_note_too_small";
	public static final String ERROR_ANALYZE_POSTPROCESS   = "error_analyze_postprocess";
	
	// MidiParser
	public static final String ERROR_ONLY_PPQ_SUPPORTED    = "error_only_ppq_supported";
	
	// MidicaPLParser
	public static final String ERROR_0_NOT_ALLOWED               = "error_0_not_allowed";
	public static final String ERROR_NEGATIVE_NOT_ALLOWED        = "error_negative_not_allowed";
	public static final String ERROR_NOT_AN_INTEGER              = "error_not_an_integer";
	public static final String ERROR_NOT_A_FLOAT                 = "error_not_a_float";
	public static final String ERROR_INSTRUMENTS_NOT_DEFINED     = "error_instruments_not_defined";
	public static final String ERROR_GLOBALS_IN_INSTR_DEF        = "error_globals_in_instr_def";
	public static final String ERROR_SINGLE_INSTR_IN_INSTR_DEF   = "error_single_instr_in_instr_def";
	public static final String ERROR_UNKNOWN_CMD                 = "error_unknown_cmd";
	public static final String ERROR_CMD_END_WITHOUT_BEGIN       = "error_cmd_end_without_begin";
	public static final String ERROR_CHANNEL_INVALID_OPT         = "error_channel_invalid_opt";
	public static final String ERROR_BLOCK_INVALID_OPT           = "error_block_invalid_opt";
	public static final String ERROR_BLOCK_UNMATCHED_CLOSE       = "error_block_unmatched_close";
	public static final String ERROR_BLOCK_UNMATCHED_OPEN        = "error_block_unmatched_open";
	public static final String ERROR_NESTABLE_BLOCK_OPEN_AT_EOF  = "error_nestable_block_open_at_eof";
	public static final String ERROR_NAMED_BLOCK_OPEN_AT_EOF     = "error_named_block_open_at_eof";
	public static final String ERROR_ARGS_NOT_ALLOWED            = "error_args_not_allowed";
	public static final String ERROR_FUNCTION_ALREADY_DEFINED    = "error_function_already_defined";
	public static final String ERROR_FUNCTION_NOT_ALLOWED_HERE   = "error_function_not_allowed_here";
	public static final String ERROR_META_NOT_ALLOWED_HERE       = "error_meta_not_allowed_here";
	public static final String ERROR_CHORD_ALREADY_DEFINED       = "error_chord_already_defined";
	public static final String ERROR_CHORD_EQUALS_NOTE           = "error_chord_equals_note";
	public static final String ERROR_CHORD_EQUALS_PERCUSSION     = "error_chord_equals_percussion";
	public static final String ERROR_CHORD_CONTAINS_ALREADY      = "error_chord_contains_already";
	public static final String ERROR_CHORD_DEF_NOT_ALLOWED_HERE  = "error_chord_def_not_allowed_here";
	public static final String ERROR_CHORD_NUM_OF_ARGS           = "error_chord_num_of_args";
	public static final String ERROR_CHORD_REDUNDANT_SEP         = "error_chord_redundant_sep";
	public static final String ERROR_CONST_NUM_OF_ARGS           = "error_const_num_of_args";
	public static final String ERROR_CONST_ALREADY_DEFINED       = "error_const_already_defined";
	public static final String ERROR_CONST_NAME_EQ_VALUE         = "error_const_name_eq_value";
	public static final String ERROR_CONST_RECURSION             = "error_const_recursion";
	public static final String ERROR_VAR_NUM_OF_ARGS             = "error_var_num_of_args";
	public static final String ERROR_VAR_ALREADY_DEF_AS_CONST    = "error_var_already_def_as_const";
	public static final String ERROR_VAR_VAL_HAS_WHITESPACE      = "error_var_val_has_whitespace";
	public static final String ERROR_VAR_NOT_DEFINED             = "error_var_not_defined";
	public static final String ERROR_VAR_NOT_ALLOWED             = "error_var_not_allowed";
	public static final String ERROR_VAR_NAME_INVALID            = "error_var_name_invalid";
	public static final String ERROR_VAR_NAME_EQ_VALUE           = "error_var_name_eq_value";
	public static final String ERROR_VAR_RECURSION               = "error_var_recursion";
	public static final String ERROR_PARAM_OUTSIDE_FUNCTION      = "error_param_outside_function";
	public static final String ERROR_PARAM_NAMED_UNKNOWN         = "error_param_named_unknown";
	public static final String ERROR_PARAM_INDEX_TOO_HIGH        = "error_param_index_too_high";
	public static final String ERROR_PARAM_INDEX_UNDEFINED       = "error_param_index_undefined";
	public static final String ERROR_DEFINE_NUM_OF_ARGS          = "error_define_num_of_args";
	public static final String ERROR_ALREADY_REDEFINED           = "error_already_redefined";
	public static final String ERROR_FILE_NUM_OF_ARGS            = "error_file_num_of_args";
	public static final String ERROR_SOUNDBANK_NUM_OF_ARGS       = "error_soundbank_num_of_args";
	public static final String ERROR_FILE_EXISTS                 = "error_file_exists";
	public static final String ERROR_FILE_NORMAL                 = "error_file_normal";
	public static final String ERROR_FILE_READABLE               = "error_file_readable";
	public static final String ERROR_FILE_IO                     = "error_file_io";
	public static final String ERROR_SOUNDBANK_IO                = "error_soundbank_io";
	public static final String ERROR_SOUNDBANK_ALREADY_PARSED    = "error_soundbank_already_parsed";
	public static final String ERROR_FUNCTION_NUM_OF_ARGS        = "error_function_num_of_args";
	public static final String ERROR_PATTERN_NOT_ALLOWED_HERE    = "error_pattern_not_allowed_here";
	public static final String ERROR_PATTERN_ALREADY_DEFINED     = "error_pattern_already_defined";
	public static final String ERROR_PATTERN_NUM_OF_ARGS         = "error_pattern_num_of_args";
	public static final String ERROR_PATTERN_INVALID_OUTER_OPT   = "error_pattern_invalid_outer_opt";
	public static final String ERROR_PATTERN_INVALID_INNER_OPT   = "error_pattern_invalid_inner_opt";
	public static final String ERROR_PATTERN_INDEX_INVALID       = "error_pattern_index_invalid";
	public static final String ERROR_PATTERN_INDEX_TOO_HIGH      = "error_pattern_index_too_high";
	public static final String ERROR_PATTERN_RECURSION_DEPTH     = "error_pattern_recursion_depth";
	public static final String ERROR_PATTERN_UNDEFINED           = "error_pattern_undefined";
	public static final String ERROR_META_NUM_OF_ARGS            = "error_meta_num_of_arts";
	public static final String ERROR_META_UNKNOWN_CMD            = "error_meta_unknown_cmd";
	public static final String ERROR_SOFT_KARAOKE_UNKNOWN_CMD    = "error_soft_karaoke_unknown_cmd";
	public static final String ERROR_SOFT_KARAOKE_NOT_ALLOWED_HERE = "error_soft_karaoke_not_allowed_here";
	public static final String ERROR_SOFT_KARAOKE_ALREADY_SET    = "error_soft_karaoke_already_set";
	public static final String ERROR_SOFT_KARAOKE_NUM_OF_ARGS    = "error_soft_karaoke_num_of_args";
	public static final String ERROR_SK_VALUE_ALREADY_SET        = "error_sk_value_already_set";
	public static final String ERROR_SK_FIELD_CRLF_NOT_ALLOWED   = "error_sk_field_crlf_not_allowed";
	public static final String ERROR_SK_SYLLABLE_CRLF_NOT_ALLOWED = "error_sk_syllable_crlf_not_allowed";
	public static final String ERROR_FUNCTION_RECURSION          = "error_function_recursion";
	public static final String ERROR_FUNCTION_RECURSION_DEPTH    = "error_function_recursion_depth";
	public static final String ERROR_FUNCTION_UNDEFINED          = "error_function_undefined";
	public static final String ERROR_CALL_NUM_OF_ARGS            = "error_call_num_of_args";
	public static final String ERROR_CALL_UNKNOWN_OPT            = "error_call_unknown_opt";
	public static final String ERROR_CALL_SYNTAX                 = "error_call_syntax";
	public static final String ERROR_CALL_EMPTY_PARAM            = "error_call_empty_param";
	public static final String ERROR_CALL_PARAM_NAME_EMPTY       = "error_call_param_name_empty";
	public static final String ERROR_CALL_PARAM_VALUE_EMPTY      = "error_call_param_value_empty";
	public static final String ERROR_CALL_PARAM_NAME_WITH_SPEC   = "error_call_param_name_with_spec";
	public static final String ERROR_CALL_DUPLICATE_PARAM_NAME   = "error_call_duplicate_param_name";
	public static final String ERROR_CALL_PARAM_MORE_ASSIGNERS   = "error_call_param_more_assigners";
	public static final String ERROR_INVALID_TIME_DENOM          = "error_invalid_time_denom";
	public static final String ERROR_INVALID_TIME_SIG            = "error_invalid_time_sig";
	public static final String ERROR_INVALID_KEY_SIG             = "error_invalid_key_sig";
	public static final String ERROR_INVALID_TONALITY            = "error_invalid_tonality";
	public static final String ERROR_PARTIAL_RANGE               = "error_partial_range";
	public static final String ERROR_PARTIAL_RANGE_ORDER         = "error_partial_range_order";
	public static final String ERROR_PARTIAL_RANGE_EMPTY         = "error_partial_range_empty";
	public static final String ERROR_MODE_INSTR_NUM_OF_ARGS      = "error_mode_instr_num_of_args";
	public static final String ERROR_NOTE_LENGTH_INVALID         = "error_note_length_invalid";
	public static final String ERROR_ZEROLENGTH_NOT_ALLOWED      = "error_zerolength_not_allowed";
	public static final String ERROR_ZEROLENGTH_IN_SUM           = "error_zerolength_in_sum";
	public static final String ERROR_ZEROLENGTH_INVALID_OPTION   = "error_zerolength_invalid_option";
	public static final String ERROR_EMPTY_LENGTH_SUMMAND        = "error_empty_length_summand";
	public static final String ERROR_UNKNOWN_FUNCTION_CMD        = "error_unknown_function_cmd";
	public static final String ERROR_INSTR_NUM_OF_ARGS           = "error_num_of_args";
	public static final String ERROR_INSTR_NUM_OF_ARGS_SINGLE    = "error_num_of_args_single";
	public static final String ERROR_INSTR_BANK                  = "error_instr_bank";
	public static final String ERROR_GLOBAL_NUM_OF_ARGS          = "error_global_num_of_args";
	public static final String ERROR_UNKNOWN_GLOBAL_CMD          = "error_unknown_global_cmd: ";
	public static final String ERROR_UNKNOWN_COMMAND_ID          = "error_unknown_command_id";
	public static final String ERROR_MIDI_PROBLEM                = "error_midi_problem";
	public static final String ERROR_CH_CMD_NUM_OF_ARGS          = "error_ch_num_of_args";
	public static final String ERROR_CANT_PARSE_OPTIONS          = "error_cant_parse_options";
	public static final String ERROR_OPTION_NEEDS_VAL            = "error_option_needs_val";
	public static final String ERROR_OPTION_VAL_NOT_ALLOWED      = "error_option_val_not_allowed";
	public static final String ERROR_VEL_NOT_MORE_THAN_127       = "error_vel_not_more_than_127";
	public static final String ERROR_VEL_NOT_LESS_THAN_1         = "error_vel_not_less_than_1";
	public static final String ERROR_TUPLET_INVALID              = "error_tuplet_invalid";
	public static final String ERROR_DURATION_MORE_THAN_0        = "error_duration_more_than_0";
	public static final String ERROR_UNKNOWN_OPTION              = "error_unknown_option: ";
	public static final String ERROR_UNKNOWN_NOTE                = "error_unknown_note";
	public static final String ERROR_UNKNOWN_PERCUSSION          = "error_unknown_percussion";
	public static final String ERROR_CHANNEL_UNDEFINED           = "error_channel_undefined";
	public static final String ERROR_CHANNEL_REDEFINED           = "error_channel_redefined";
	public static final String ERROR_INVALID_CHANNEL_NUMBER      = "error_invalid_channel_number";
	public static final String ERROR_NOT_ALLOWED_IN_INSTR_BLK    = "error_not_allowed_in_instr_blk";
	public static final String ERROR_NOT_ALLOWED_IN_META_BLK     = "error_not_allowed_in_meta_blk";
	public static final String ERROR_NOT_ALLOWED_IN_BLK          = "error_not_allowed_in_blk";
	public static final String ERROR_BLOCK_IF_MUST_BE_ALONE      = "error_block_if_must_be_alone";
	public static final String ERROR_BLOCK_ELSIF_MUST_BE_ALONE   = "error_block_elsif_must_be_alone";
	public static final String ERROR_BLOCK_ELSE_MUST_BE_ALONE    = "error_block_else_must_be_alone";
	public static final String ERROR_BLOCK_NO_IF_FOUND           = "error_block_no_if_found";
	public static final String ERROR_TOO_MANY_OPERATORS_IN_COND  = "error_too_many_operators_in_cond";
	public static final String ERROR_COND_DEFINED_HAS_WHITESPACE = "error_cond_defined_has_whitespace";
	public static final String ERROR_COND_WHITESPACE_IN_FIRST_OP = "error_cond_whitespace_in_first_op";
	public static final String ERROR_COND_WHITESPACE_IN_SEC_OP   = "error_cond_whitespace_in_sec_op";
	public static final String ERROR_COND_UNDEF_EMPTY            = "error_cond_undef_empty";
	public static final String ERROR_COND_UNDEF_IN_CENTER        = "error_cond_undef_in_center";
	public static final String ERROR_COND_WHITESPACE_IN_IN_ELEM  = "error_cond_whitespace_in_in_elem";
	public static final String ERROR_COND_EMPTY_ELEM_IN_IN_LIST  = "error_cond_empty_elem_in_in_list";
	public static final String ERROR_CALL_IF_MUST_BE_ALONE       = "error_call_if_must_be_alone";
	
	// NestableBlock
	public static final String ERROR_BLOCK_ARG_ALREADY_SET      = "error_block_arg_already_set";
	
	// ParseException and StackTraceElement
	public static final String EXCEPTION_CAUSED_BY_LINE         = "exception_caused_by_line";
	public static final String EXCEPTION_CAUSED_BY_BLK_COND     = "exception_caused_by_blk_cond";
	public static final String EXCEPTION_CAUSED_BY_INVALID_VAR  = "exception_caused_by_invalid_var";
	public static final String STACK_TRACE_HEADER               = "stack_trace_header";
	public static final String STACK_TRACE_INDENTATION          = "stack_trace_indentation";
	public static final String STACK_TRACE_BLOCK                = "stack_trace_block";
	public static final String STACK_TRACE_OPTIONS              = "stack_trace_options";
	public static final String STACK_TRACE_PARAMS               = "stack_trace_params";
	public static final String STACK_TRACE_LINE                 = "stack_trace_line";
	public static final String STACK_TRACE_IN                   = "stack_trace_in";
	public static final String STACK_TRACE_EXEC                 = "stack_trace_exec";
	public static final String STACK_TRACE_FUNCTION             = "stack_trace_function";
	public static final String STACK_TRACE_PATTERN              = "stack_trace_pattern";
	
	// AldaImporter
	public static final String ERROR_ALDA_NO_MIDI_FILE           = "error_alda_no_midi_file";
	
	// LilypondImporter
	public static final String ERROR_LILYPOND_NO_MIDI_FILE       = "error_lilypond_no_midi_file";
	public static final String ERROR_WRONG_DIVISION_TYPE         = "error_wrong_division_type";
	public static final String ERROR_DIFFERENT_RESOLUTION        = "error_different_resolution";
	
	// Exporter
	public static final String ERROR_EXPORT                     = "error_export";
	public static final String ERROR_FILE_NOT_WRITABLE          = "error_file_not_writable";
	public static final String OVERWRITE_FILE                   = "overwrite_file";
	public static final String EXPORTER_TICK                    = "exporter_tick";
	public static final String CONFIRM_DIALOG_YES               = "confirm_dialog_yes";
	public static final String CONFIRM_DIALOG_NO                = "confirm_dialog_no";
	
	// AudioExporter
	public static final String ERROR_AU_SAMPLE_RATE_NOT_POS          = "error_au_sample_rate_not_pos";
	public static final String ERROR_AU_SAMPLE_SIZE_NOT_POS          = "error_au_sample_size_not_pos";
	public static final String ERROR_AU_SAMPLE_SIZE_NOT_DIV_8        = "error_au_sample_size_not_div_8";
	public static final String ERROR_AU_FLOAT_NOT_32_OR_64           = "error_au_float_not_32_or_64";
	public static final String ERROR_AU_FILETYPE_UNKNOWN             = "error_au_filetype_unknown";
	public static final String ERROR_AU_FILETYPE_NOT_SUPP            = "error_au_filetype_not_supp";
	public static final String ERROR_AU_FILETYPE_NOT_SUPP_F_STREAM   = "error_au_filetype_not_supp_f_stream";
	
	// MusescoreExporter
	public static final String ERROR_MSCORE_NO_OUTPUT_FILE      = "error_mscore_no_output_file";
	public static final String ERROR_MSCORE_MOVE_FAILED         = "error_mscore_move_failed";
	public static final String ERROR_MSCORE_EXT_NOT_ALLOWED     = "error_mscore_ext_not_allowed";
	
	// WaitView
	public static final String TITLE_WAIT                       = "title_wait";
	public static final String WAIT_PARSE_MPL                   = "wait_parse_mpl";
	public static final String WAIT_PARSE_MID                   = "wait_parse_mid";
	public static final String WAIT_PARSE_SB                    = "wait_parse_sb";
	public static final String WAIT_PARSE_URL                   = "wait_parse_url";
	public static final String WAIT_PARSE_FOREIGN               = "wait_parse_foreign";
	public static final String WAIT_REPARSE                     = "wait_reparse";
	public static final String WAIT_SETUP_DEVICES               = "wait_setup_devices";
	public static final String WAIT_EXPORT                      = "wait_export";
	
	// ExportResultView
	public static final String TITLE_EXPORT_RESULT              = "title_export_result";
	public static final String EXPORT_SUCCESS                   = "export_success";
	public static final String NUMBER_OF_WARNINGS               = "number_of_warnings";
	public static final String WARNING_COL_TRACK                = "warning_col_track";
	public static final String WARNING_COL_TICK                 = "warning_col_tick";
	public static final String WARNING_COL_CHANNEL              = "warning_col_channel";
	public static final String WARNING_COL_MESSAGE              = "warning_col_message";
	public static final String WARNING_COL_DETAILS              = "warning_col_details";
	public static final String SHOW_IGN_SHORT_MSG               = "show_ign_short_msg";
	public static final String SHOW_IGN_META_MSG                = "show_ign_meta_msg";
	public static final String SHOW_IGN_SYSEX_MSG               = "show_ign_sysex_msg";
	public static final String SHOW_SKIPPED_RESTS               = "show_skipped_rests";
	public static final String SHOW_OFF_NOT_FOUND               = "show_off_not_found";
	public static final String SHOW_CRD_GRP_FAILED              = "show_crd_grp_failed";
	public static final String SHOW_OTHER_WARNINGS              = "show_other_warnings";
	
	// ExportException
	public static final String ERROR_TICK                       = "error_tick";
	public static final String ERROR_CHANNEL                    = "error_channel";
	public static final String ERROR_NOTE                       = "error_note";
	
	// MidicaPLExporter
	public static final String WARNING_IGNORED_SHORT_MESSAGE    = "warning_ignored_short_message";
	public static final String WARNING_IGNORED_META_MESSAGE     = "warning_ignored_meta_message";
	public static final String WARNING_IGNORED_SYSEX_MESSAGE    = "warning_ignored_sysex_message";
	public static final String WARNING_REST_SKIPPED             = "warning_rest_skipped";
	public static final String WARNING_REST_SKIPPED_TICKS       = "warning_rest_skipped_ticks";
	public static final String WARNING_OFF_NOT_FOUND            = "warning_off_not_found";
	public static final String WARNING_CHORD_GROUPING_FAILED    = "warning_chord_grouping_conflict";
	public static final String WARNING_OFF_MOVING_CONFLICT      = "warning_off_moving_conflict";
	
	// MidiDevices
	public static final String DEFAULT_CHANNEL_COMMENT          = "default_channel_comment";
	public static final String DEFAULT_INSTRUMENT_NAME          = "default_instrument_name";
	public static final String DEFAULT_PROGRAM_NUMBER           = "default_program_number";
	public static final String PERCUSSION_CHANNEL               = "percussion_channel";
	public static final String NORMAL_CHANNEL                   = "normal_channel";
	public static final String ERROR_SOUNDBANK_NOT_SUPPORTED    = "error_soundbank_not_supported";
	public static final String ERROR_SOUNDBANK_LOADING_FAILED   = "error_soundbank_loading_failed";
	
	// SequenceNotSetException
	public static final String ERROR_SEQUENCE_NOT_SET           = "error_sequence_not_set";
	
	// ErrorMessage
	public static final String CLOSE                            = "close";
	public static final String TITLE_ERROR                      = "title_error";
	public static final String TITLE_CONFIRMATION               = "title_confirmation";
	
	// FileSelector / MidicaFileChooser
	public static final String TITLE_FILE_SELECTOR              = "title_file_selector";
	public static final String CONFIG_ICON_TOOLTIP              = "config_icon_tooltip";
	public static final String CONFIG_ICON_TOOLTIP_WRONG        = "config_icon_tooltip_wrong";
	public static final String CONFIG_ICON_TOOLTIP_OK           = "config_icon_tooltip_ok";
	public static final String TAB_SOUND_FILE                   = "tab_sound_file";
	public static final String TAB_SOUND_URL                    = "tab_sound_url";
	public static final String TAB_MIDI                    = "tab_midi";
	public static final String TAB_MIDICAPL                = "tab_midicapl";
	public static final String TAB_ALDA                    = "tab_alda";
	public static final String TAB_AUDIO                   = "tab_audio";
	public static final String TAB_ABC                     = "tab_abc";
	public static final String TAB_LY                      = "tab_ly";
	public static final String TAB_MSCORE                  = "tab_mscore";
	public static final String CHARSET                     = "charset";
	public static final String CHARSET_DESC_MPL_READ       = "charset_desc_mpl_read";
	public static final String CHARSET_DESC_MID_READ       = "charset_desc_mid_read";
	public static final String CHARSET_DESC_MPL_WRITE      = "charset_desc_mpl_write";
	public static final String CHARSET_DESC_MID_WRITE      = "charset_desc_mid_write";
	public static final String FOREIGN_URL                 = "foreign_url";
	public static final String FOREIGN_PROG                = "foreign_prog";
	public static final String FOREIGN_PROG_DESC           = "foreign_prog_desc";
	public static final String FOREIGN_PROG_ALDA           = "foreign_prog_alda";
	public static final String FOREIGN_PROG_ABCMIDI        = "foreign_prog_abcmidi";
	public static final String FOREIGN_PROG_LY             = "foreign_prog_ly";
	public static final String FOREIGN_PROG_MIDI2ABC       = "foreign_prog_midi2abc";
	public static final String FOREIGN_PROG_MIDI2LY        = "foreign_prog_midi2ly";
	public static final String FOREIGN_PROG_MSCORE         = "foreign_prog_mscore";
	public static final String DIRECT_IMPORT               = "direct_import";
	public static final String FILE_OPTIONS                = "file_options";
	public static final String SOUND_URL                   = "sound_url";
	public static final String SOUND_DOWNLOAD              = "sound_download";
	
	// PlayerView
	public static final String TITLE_PLAYER                     = "title_player";
	public static final String REPARSE                          = "reparse";
	public static final String SOUNDCHECK                       = "soundcheck";
	public static final String MEMORIZE                         = "memorize";
	public static final String JUMP                             = "jump";
	public static final String SHOW_LYRICS                      = "show_lyrics";
	public static final String TIME_INFO_UNAVAILABLE            = "time_info_unavailable";
	public static final String SLIDER_MASTER_VOL                = "slider_master_vol";
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
	public static final String APPLY_TO_ALL_CHANNELS            = "apply_to_all_channels";
	public static final String LBL_NOTE_HISTORY                 = "lbl_note_history";
	public static final String COLUMN_VELOCITY                  = "column_velocity";
	public static final String COLUMN_NUMBER                    = "column_number";
	public static final String COLUMN_NAME                      = "column_name";
	public static final String COLUMN_TICK                      = "column_tick";
	public static final String TIP_PARSE_SUCCESS                = "tip_parse_success";
	public static final String TIP_PARSE_FAILED                 = "tip_parse_failed";
	
	// SoundcheckView
	public static final String TITLE_SOUNDCHECK                 = "title_soundcheck";
	public static final String SNDCHK_CHANNEL                   = "sndchk_channel";
	public static final String SNDCHK_INSTRUMENT                = "sndchk_instrument";
	public static final String SNDCHK_NOTE                      = "sndchk_note";
	public static final String SNDCHK_VOLUME                    = "sndchk_volume";
	public static final String SNDCHK_VELOCITY                  = "sndchk_velocity";
	public static final String SNDCHK_DURATION                  = "sndchk_duration";
	public static final String SNDCHK_KEEP_SETTINGS             = "sndchk_keep_settings";
	public static final String SNDCHK_PLAY                      = "sndchk_play";
	public static final String SNDCHK_COL_PROGRAM               = "sndchk_col_program";
	public static final String SNDCHK_COL_BANK                  = "sndchk_col_bank";
	public static final String SNDCHK_COL_NAME_SB               = "sndchk_col_name_sb";
	public static final String SNDCHK_COL_NAME_SYNTAX           = "sndchk_col_name_syntax";
	public static final String SNDCHK_COL_NOTE_NUM              = "sndchk_col_note_num";
	public static final String SNDCHK_COL_NOTE_NAME             = "sndchk_col_note_name";
	public static final String SNDCHK_COL_NOTE_SHORT            = "sndchk_col_note_short";
	
	// string filter for tables
	public static final String FILTER_ICON_TOOLTIP              = "filter_icon_tooltip";
	public static final String FILTER_ICON_TOOLTIP_ACTIVE       = "filter_icon_tooltip_active";
	public static final String FILTER_ICON_TOOLTIP_EMPTY        = "filter_icon_tooltip_empty";
	public static final String FILTER_LAYER_LABEL               = "filter_layer_label";
	public static final String FILTER_LAYER_CLEAR               = "filter_layer_clear";
	
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
	 * - Initializing percussion IDs using {@link #initPercussion()}
	 * - Initializing MidicaPL syntax keywords using {@link #initSyntax()}
	 * - Initializing instrument IDs using {@link #initInstruments()}
	 * 
	 * This method is called initially on program startup and then always if
	 * the user changes a configuration using the GUI.
	 */
	public static void init() {
		syntax = new HashMap<String, String>();
		
		initLanguage();
		initNoteSystem(); // calls also initHalfTones() which calls initOctaves()
		initPercussion();
		initInstruments();
		initSyntax();
		
		// init key binding categories
		keyBindingCategories = new ArrayList<>();
		keyBindingCategories.add(KEYCAT_MAIN);
		keyBindingCategories.add(KEYCAT_PLAYER);
		keyBindingCategories.add(KEYCAT_SOUNDCHECK);
		keyBindingCategories.add(KEYCAT_INFO);
		keyBindingCategories.add(KEYCAT_MSG);
		keyBindingCategories.add(KEYCAT_FILE_SELECT);
		keyBindingCategories.add(KEYCAT_FILE_CONF);
		keyBindingCategories.add(KEYCAT_DC_CONF);
		keyBindingCategories.add(KEYCAT_AU_CONF);
		keyBindingCategories.add(KEYCAT_EXPORT_RESULT);
		keyBindingCategories.add(KEYCAT_STRING_FILTER);
	}
	
	/**
	 * Initializes the internal data structure for the chosen language translation.
	 */
	private static void initLanguage() {
		
		// get language
		String language = Config.get(Config.LANGUAGE);
		
		// init the default language
		dictionary = new HashMap<String, String>();
		initLanguageEnglish();
		
		// init another language if chosen
		if (Config.CBX_LANG_GERMAN.equals(language))
			initLanguageGerman();
	}
	
	/**
	 * Initializes the internal data structure for english language translations.
	 */
	private static void initLanguageEnglish() {
		
		// Config
		set( Config.CBX_HALFTONE_ID_SHARP,           "# / b (c#, cb, d#, db, ...)"           );
		set( Config.CBX_HALFTONE_ID_DIESIS,          "-diesis / -bemolle (do-diesis, ...)"   );
		set( Config.CBX_HALFTONE_ID_CIS,             "-is / -es (cis, ces, dis, des,...)"    );
		
		set( Config.CBX_SHARPFLAT_SHARP,             "sharp (#, -is, -dieses)"               );
		set( Config.CBX_SHARPFLAT_FLAT,              "flat (b, -es, -bemolle)"               );
		
		set( Config.CBX_NOTE_ID_INTERNATIONAL_LC,    "International: c, d, e, f, g, a, b"    );
		set( Config.CBX_NOTE_ID_INTERNATIONAL_UC,    "International: C, D, E, F, G, A, B"    );
		set( Config.CBX_NOTE_ID_ITALIAN_LC,          "Italian (lower): do, re, mi, fa..."    );
		set( Config.CBX_NOTE_ID_ITALIAN_UC,          "Italian (upper): Do, Re, Mi, Fa..."    );
		set( Config.CBX_NOTE_ID_GERMAN_LC,           "German (lower): c, d, e, f, g, a, h"   );
		set( Config.CBX_NOTE_ID_GERMAN_UC,           "German (upper): C, D, E, F, G, A, H"   );
		
		set( Config.CBX_OCTAVE_PLUS_MINUS_N,         "+n/-n: c-2, c-, c, c+, c+2, c+3..."    );
		set( Config.CBX_OCTAVE_PLUS_MINUS,           "+/-: c--, c-, c, c+, c++..."           );
		set( Config.CBX_OCTAVE_INTERNATIONAL,        "International: c0, c1, c2..."          );
		set( Config.CBX_OCTAVE_GERMAN,               "German: C', C, c, c', c'', c'''..."    );
		
		set( Config.CBX_SYNTAX_MIXED,                "Mixed Case Syntax"                     );
		set( Config.CBX_SYNTAX_LOWER,                "Lower Case Syntax"                     );
		set( Config.CBX_SYNTAX_UPPER,                "Upper Case Syntax"                     );
		
		set( Config.CBX_PERC_EN_1,                   "English"                               );
		set( Config.CBX_PERC_DE_1,                   "German"                                );
		
		set( Config.CBX_INSTR_EN_1,                  "English"                               );
		set( Config.CBX_INSTR_DE_1,                  "German"                                );
		
		// translations for key-bindings
		set( WARNING_KEY_BINDING_CONFLICT, "Conflicting Key bindings in the same scope" );
		set( TT_KEY_NOT_CONFIGURED,        "No key binding(s) configured"               );
		set( TT_KEY_BUTTON_PRESS,          "Key binding(s) to press"                    );
		set( TT_KEY_TXT_FLD_FOCUS,         "Key binding(s) to focus"                    );
		set( TT_KEY_TXT_AREA_FOCUS,        "Key binding(s) to focus"                    );
		set( TT_KEY_SLD_FOCUS,             "Key binding(s) to focus"                    );
		set( TT_KEY_TREE_FOCUS,            "Key binding(s) to focus the tree"           );
		set( TT_KEY_TABLE_FOCUS,           "Key binding(s) to focus the table"          );
		set( TT_KEY_LIST_FOCUS,            "Key binding(s) to focus the list"           );
		set( TT_KEY_CBX_TOGGLE,            "Key binding(s) to toggle"                   );
		set( TT_KEY_CBX_OPEN,              "Key binding(s) to open"                     );
		set( TT_KEY_CBX_CHANNEL_SELECT,    "Key binding(s) to select the channel"       );
		set( TT_KEY_CBX_SELECT_CHANNEL_N,  "Select Channel "                            );
		set( TT_KEY_TAB_SELECT,            "Key binding(s) to select"                   );
		set( TT_KEY_OPEN_ICON,             "Key binding(s) to open"                     );
		
		// key binding categories (windows)
		set( KEYCAT_MAIN,                  "Main Window"                                    );
		set( KEYCAT_PLAYER,                "Player"                                         );
		set( KEYCAT_SOUNDCHECK,            "Soundcheck"                                     );
		set( KEYCAT_INFO,                  "Info & Configuration"                           );
		set( KEYCAT_MSG,                   "Message Window"                                 );
		set( KEYCAT_FILE_SELECT,           "File Selector"                                  );
		set( KEYCAT_FILE_CONF,             "File-based Config Windows (general settings)"   );
		set( KEYCAT_DC_CONF,               "Decompile Config Window (specific settings)"    );
		set( KEYCAT_AU_CONF,               "Audio Export Config Window (specific settings)" );
		set( KEYCAT_EXPORT_RESULT,         "Export Result Window"                           );
		set( KEYCAT_STRING_FILTER,         "Table Filter"                                   );
		
		// key binding IDs
		set( KEY_MAIN_INFO,                    "Open Info & Config Details"                                                  );
		set( KEY_MAIN_PLAYER,                  "Open Player Window"                                                          );
		set( KEY_MAIN_IMPORT,                  "Import File"                                                                 );
		set( KEY_MAIN_IMPORT_SB,               "Load Soundbank file"                                                         );
		set( KEY_MAIN_EXPORT,                  "Export file"                                                                 );
		set( KEY_MAIN_CBX_LANGUAGE,            "Open Language Selection"                                                     );
		set( KEY_MAIN_CBX_NOTE,                "Open Note System Selection"                                                  );
		set( KEY_MAIN_CBX_HALFTONE,            "Open Half Tone Symbol Selection"                                             );
		set( KEY_MAIN_CBX_SHARPFLAT,           "Open Default Half Tone Selection"                                            );
		set( KEY_MAIN_CBX_OCTAVE,              "Open Octave Naming Selection"                                                );
		set( KEY_MAIN_CBX_SYNTAX,              "Open Syntax Selection"                                                       );
		set( KEY_MAIN_CBX_PERCUSSION,          "Open Percussion ID Selection"                                                );
		set( KEY_MAIN_CBX_INSTRUMENT,          "Open Instrument ID Selection"                                                );
		set( KEY_PLAYER_CLOSE,                 "Close the player"                                                            );
		set( KEY_PLAYER_PLAY,                  "Click the Play/Pause button"                                                 );
		set( KEY_PLAYER_REPARSE,               "Reparse the current sequence"                                                );
		set( KEY_PLAYER_INFO,                  "Open Info & Config Details"                                                  );
		set( KEY_PLAYER_SOUNDCHECK,            "Open the Soundcheck window"                                                  );
		set( KEY_PLAYER_MEMORIZE,              "Memorize the current tick"                                                   );
		set( KEY_PLAYER_JUMP_FIELD,            "Focus the Jump field"                                                        );
		set( KEY_PLAYER_GO,                    "Go to the tick in the Jump field"                                            );
		set( KEY_PLAYER_LYRICS,                "Toggle between Channel and Lyrics view"                                      );
		set( KEY_PLAYER_STOP,                  "Click the Stop button"                                                       );
		set( KEY_PLAYER_FAST_REWIND,           "Click the Fast Rewind button (<<)"                                           );
		set( KEY_PLAYER_REWIND,                "Click the Rewind button (<)"                                                 );
		set( KEY_PLAYER_FORWARD,               "Click the Forward button (>)"                                                );
		set( KEY_PLAYER_FAST_FORWARD,          "Click the Fast Forward button (>>)"                                          );
		set( KEY_PLAYER_BEGIN,                 "Go to the start point of the sequence"                                       );
		set( KEY_PLAYER_END,                   "Go to the end point of the sequence"                                         );
		set( KEY_PLAYER_VOL_FLD,               "Focus the volume text field"                                                 );
		set( KEY_PLAYER_VOL_SLD,               "Focus the volume slider"                                                     );
		set( KEY_PLAYER_TEMPO_FLD,             "Focus the tempo text field"                                                  );
		set( KEY_PLAYER_TEMPO_SLD,             "Focus the tempo slider"                                                      );
		set( KEY_PLAYER_TRANSPOSE_FLD,         "Focus the transpose text field"                                              );
		set( KEY_PLAYER_TRANSPOSE_SLD,         "Focus the transpose slider"                                                  );
		set( KEY_PLAYER_CH_VOL_FLD,            "Focus the channel volume text field"                                         );
		set( KEY_PLAYER_CH_VOL_SLD,            "Focus the channel volume slider"                                             );
		set( KEY_PLAYER_CH_VOL_BTN,            "Press the button to apply the channel volume to all channels"                );
		set( KEY_PLAYER_CH_00,                 "Toggle channel 0"                                                            );
		set( KEY_PLAYER_CH_01,                 "Toggle channel 1"                                                            );
		set( KEY_PLAYER_CH_02,                 "Toggle channel 2"                                                            );
		set( KEY_PLAYER_CH_03,                 "Toggle channel 3"                                                            );
		set( KEY_PLAYER_CH_04,                 "Toggle channel 4"                                                            );
		set( KEY_PLAYER_CH_05,                 "Toggle channel 5"                                                            );
		set( KEY_PLAYER_CH_06,                 "Toggle channel 6"                                                            );
		set( KEY_PLAYER_CH_07,                 "Toggle channel 7"                                                            );
		set( KEY_PLAYER_CH_08,                 "Toggle channel 8"                                                            );
		set( KEY_PLAYER_CH_09,                 "Toggle channel 9"                                                            );
		set( KEY_PLAYER_CH_10,                 "Toggle channel 10"                                                           );
		set( KEY_PLAYER_CH_11,                 "Toggle channel 11"                                                           );
		set( KEY_PLAYER_CH_12,                 "Toggle channel 12"                                                           );
		set( KEY_PLAYER_CH_13,                 "Toggle channel 13"                                                           );
		set( KEY_PLAYER_CH_14,                 "Toggle channel 14"                                                           );
		set( KEY_PLAYER_CH_15,                 "Toggle channel 15"                                                           );
		set( KEY_PLAYER_CH_00_M,               "Mute or Unmute channel 0"                                                    );
		set( KEY_PLAYER_CH_01_M,               "Mute or Unmute channel 1"                                                    );
		set( KEY_PLAYER_CH_02_M,               "Mute or Unmute channel 2"                                                    );
		set( KEY_PLAYER_CH_03_M,               "Mute or Unmute channel 3"                                                    );
		set( KEY_PLAYER_CH_04_M,               "Mute or Unmute channel 4"                                                    );
		set( KEY_PLAYER_CH_05_M,               "Mute or Unmute channel 5"                                                    );
		set( KEY_PLAYER_CH_06_M,               "Mute or Unmute channel 6"                                                    );
		set( KEY_PLAYER_CH_07_M,               "Mute or Unmute channel 7"                                                    );
		set( KEY_PLAYER_CH_08_M,               "Mute or Unmute channel 8"                                                    );
		set( KEY_PLAYER_CH_09_M,               "Mute or Unmute channel 9"                                                    );
		set( KEY_PLAYER_CH_10_M,               "Mute or Unmute channel 10"                                                   );
		set( KEY_PLAYER_CH_11_M,               "Mute or Unmute channel 11"                                                   );
		set( KEY_PLAYER_CH_12_M,               "Mute or Unmute channel 12"                                                   );
		set( KEY_PLAYER_CH_13_M,               "Mute or Unmute channel 13"                                                   );
		set( KEY_PLAYER_CH_14_M,               "Mute or Unmute channel 14"                                                   );
		set( KEY_PLAYER_CH_15_M,               "Mute or Unmute channel 15"                                                   );
		set( KEY_PLAYER_CH_00_S,               "Solo or Unsolo channel 0"                                                    );
		set( KEY_PLAYER_CH_01_S,               "Solo or Unsolo channel 1"                                                    );
		set( KEY_PLAYER_CH_02_S,               "Solo or Unsolo channel 2"                                                    );
		set( KEY_PLAYER_CH_03_S,               "Solo or Unsolo channel 3"                                                    );
		set( KEY_PLAYER_CH_04_S,               "Solo or Unsolo channel 4"                                                    );
		set( KEY_PLAYER_CH_05_S,               "Solo or Unsolo channel 5"                                                    );
		set( KEY_PLAYER_CH_06_S,               "Solo or Unsolo channel 6"                                                    );
		set( KEY_PLAYER_CH_07_S,               "Solo or Unsolo channel 7"                                                    );
		set( KEY_PLAYER_CH_08_S,               "Solo or Unsolo channel 8"                                                    );
		set( KEY_PLAYER_CH_09_S,               "Solo or Unsolo channel 9"                                                    );
		set( KEY_PLAYER_CH_10_S,               "Solo or Unsolo channel 10"                                                   );
		set( KEY_PLAYER_CH_11_S,               "Solo or Unsolo channel 11"                                                   );
		set( KEY_PLAYER_CH_12_S,               "Solo or Unsolo channel 12"                                                   );
		set( KEY_PLAYER_CH_13_S,               "Solo or Unsolo channel 13"                                                   );
		set( KEY_PLAYER_CH_14_S,               "Solo or Unsolo channel 14"                                                   );
		set( KEY_PLAYER_CH_15_S,               "Solo or Unsolo channel 15"                                                   );
		set( KEY_SOUNDCHECK_CLOSE,             "Close the Soundcheck window"                                                 );
		set( KEY_SOUNDCHECK_PLAY,              "Click the Play button"                                                       );
		set( KEY_SOUNDCHECK_FILTER_INSTR,      "Open the Instruments/Drumkits Filter"                                        );
		set( KEY_SOUNDCHECK_FILTER_NOTE,       "Open the Note/Percussion Filter"                                             );
		set( KEY_SOUNDCHECK_INSTR,             "Focus the Instrument table"                                                  );
		set( KEY_SOUNDCHECK_NOTE,              "Focus the Note/Percussion table"                                             );
		set( KEY_SOUNDCHECK_VOL_FLD,           "Focus the volume text field"                                                 );
		set( KEY_SOUNDCHECK_VOL_SLD,           "Focus the volume slider"                                                     );
		set( KEY_SOUNDCHECK_VEL_FLD,           "Focus the velocity text field"                                               );
		set( KEY_SOUNDCHECK_VEL_SLD,           "Focus the velocity slider"                                                   );
		set( KEY_SOUNDCHECK_DURATION,          "Focus the duration text field"                                               );
		set( KEY_SOUNDCHECK_KEEP,              "Toggle the \"Keep Settings\" checkbox"                                       );
		set( KEY_SOUNDCHECK_CH_00,             "Select channel 0"                                                            );
		set( KEY_SOUNDCHECK_CH_01,             "Select channel 1"                                                            );
		set( KEY_SOUNDCHECK_CH_02,             "Select channel 2"                                                            );
		set( KEY_SOUNDCHECK_CH_03,             "Select channel 3"                                                            );
		set( KEY_SOUNDCHECK_CH_04,             "Select channel 4"                                                            );
		set( KEY_SOUNDCHECK_CH_05,             "Select channel 5"                                                            );
		set( KEY_SOUNDCHECK_CH_06,             "Select channel 6"                                                            );
		set( KEY_SOUNDCHECK_CH_07,             "Select channel 7"                                                            );
		set( KEY_SOUNDCHECK_CH_08,             "Select channel 8"                                                            );
		set( KEY_SOUNDCHECK_CH_09,             "Select channel 9"                                                            );
		set( KEY_SOUNDCHECK_CH_10,             "Select channel 10"                                                           );
		set( KEY_SOUNDCHECK_CH_11,             "Select channel 11"                                                           );
		set( KEY_SOUNDCHECK_CH_12,             "Select channel 12"                                                           );
		set( KEY_SOUNDCHECK_CH_13,             "Select channel 13"                                                           );
		set( KEY_SOUNDCHECK_CH_14,             "Select channel 14"                                                           );
		set( KEY_SOUNDCHECK_CH_15,             "Select channel 15"                                                           );
		set( KEY_INFO_CLOSE,                   "Close the Info window"                                                       );
		set( KEY_INFO_CONF,                    "Switch to the Configuration tab"                                             );
		set( KEY_INFO_CONF_NOTE,               "Switch to the Configuration / Note Table tab"                                );
		set( KEY_INFO_CONF_PERC,               "Switch to the Configuration / Percussion IDs tab"                            );
		set( KEY_INFO_CONF_SYNTAX,             "Switch to the Configuration / Syntax tab"                                    );
		set( KEY_INFO_CONF_INSTR,              "Switch to the Configuration / Instrument IDs tab"                            );
		set( KEY_INFO_CONF_DRUMKIT,            "Switch to the Configuration / Drumkit IDs tab"                               );
		set( KEY_INFO_SB,                      "Switch to the Soundbank tab"                                                 );
		set( KEY_INFO_SB_GENERAL,              "Switch to the Soundbank / General Info tab"                                  );
		set( KEY_INFO_SB_INSTR,                "Switch to the Soundbank / Instruments & Drum Kits tab"                       );
		set( KEY_INFO_SB_RES,                  "Switch to the Soundbank / Resources tab"                                     );
		set( KEY_INFO_MIDI,                    "Switch to the MIDI Sequence tab"                                             );
		set( KEY_INFO_MIDI_GENERAL,            "Switch to the MIDI Sequence / General Info tab"                              );
		set( KEY_INFO_MIDI_KARAOKE,            "Switch to the MIDI Sequence / Karaoke Info tab"                              );
		set( KEY_INFO_MIDI_BANKS,              "Switch to the MIDI Sequence / Banks, Instruments, Notes tab"                 );
		set( KEY_INFO_MIDI_MSG,                "Switch to the MIDI Sequence / MIDI Messages tab"                             );
		set( KEY_INFO_ABOUT,                   "Switch to the About tab"                                                     );
		set( KEY_INFO_KEYBINDINGS,             "Switch to the Key Bindings Tab"                                              );
		set( KEY_INFO_CONF_NOTE_FILTER,        "Open the Note table filter"                                                  );
		set( KEY_INFO_CONF_PERC_FILTER,        "Open the Percussion ID table filter"                                         );
		set( KEY_INFO_CONF_SYNTAX_FILTER,      "Open the Syntax table filter"                                                );
		set( KEY_INFO_CONF_INSTR_FILTER,       "Open the Instrument ID table filter"                                         );
		set( KEY_INFO_CONF_DRUMKIT_FILTER,     "Open the Drumkit ID table filter"                                            );
		set( KEY_INFO_SB_INSTR_FILTER,         "Open the Instruments & Drumkits table filter"                                );
		set( KEY_INFO_SB_RES_FILTER,           "Open the Resources table filter"                                             );
		set( KEY_INFO_MIDI_MSG_FILTER,         "Open the string filter for the MIDI Messages table"                          );
		set( KEY_INFO_MIDI_BANKS_TOT_PL,       "Expand selected nodes (or the whole tree) from MIDI / Banks / Total"         );
		set( KEY_INFO_MIDI_BANKS_TOT_MIN,      "Collapse selected nodes (or the whole tree) from MIDI / Banks / Total"       );
		set( KEY_INFO_MIDI_BANKS_TOT_TREE,     "Focus the MIDI / Banks / Total tree"                                         );
		set( KEY_INFO_MIDI_BANKS_CH_PL,        "Expand selected nodes (or the whole tree) from MIDI / Banks / Per Channel"   );
		set( KEY_INFO_MIDI_BANKS_CH_MIN,       "Collapse selected nodes (or the whole tree) from MIDI / Banks / Per Channel" );
		set( KEY_INFO_MIDI_BANKS_CH_TREE,      "Focus the MIDI / Banks / Per Channel tree"                                   );
		set( KEY_INFO_MIDI_MSG_PL,             "Expand selected nodes (or the whole tree) from MIDI / Messages"              );
		set( KEY_INFO_MIDI_MSG_MIN,            "Collapse selected nodes (or the whole tree) from MIDI / Messages"            );
		set( KEY_INFO_MIDI_MSG_TREE,           "Focus the MIDI Sequence / MIDI Messages tree"                                );
		set( KEY_INFO_MIDI_MSG_TABLE,          "Focus the MIDI Messages table"                                               );
		set( KEY_INFO_MIDI_MSG_CH_INDEP,       "Toggle \"Channel-Independent\" checkbox"                                     );
		set( KEY_INFO_MIDI_MSG_CH_DEP,         "Toggle \"Channel-Dependent\" checkbox"                                       );
		set( KEY_INFO_MIDI_MSG_SEL_NOD,        "Toggle \"Selected Nodes\" checkbox"                                          );
		set( KEY_INFO_MIDI_MSG_LIM_TCK,        "Toggle \"Limit Ticks\" checkbox"                                             );
		set( KEY_INFO_MIDI_MSG_TICK_FROM,      "Focus \"From\" tick text field"                                              );
		set( KEY_INFO_MIDI_MSG_TICK_TO,        "Focus \"To\" tick text field"                                                );
		set( KEY_INFO_MIDI_MSG_LIM_TRK,        "Toggle \"Limit Tracks\" checkbox"                                            );
		set( KEY_INFO_MIDI_MSG_TRACKS_TXT,     "Focus Tracks text field"                                                     );
		set( KEY_INFO_MIDI_MSG_SHOW_IN_TR,     "Click \"Show in Tree\" button"                                               );
		set( KEY_INFO_MIDI_MSG_SHOW_AUTO,      "Toggle \"Automatically\" checkbox"                                           );
		set( KEY_INFO_MIDI_MSG_CH_00,          "Toggle checkbox for channel 0"                                               );
		set( KEY_INFO_MIDI_MSG_CH_01,          "Toggle checkbox for channel 1"                                               );
		set( KEY_INFO_MIDI_MSG_CH_02,          "Toggle checkbox for channel 2"                                               );
		set( KEY_INFO_MIDI_MSG_CH_03,          "Toggle checkbox for channel 3"                                               );
		set( KEY_INFO_MIDI_MSG_CH_04,          "Toggle checkbox for channel 4"                                               );
		set( KEY_INFO_MIDI_MSG_CH_05,          "Toggle checkbox for channel 5"                                               );
		set( KEY_INFO_MIDI_MSG_CH_06,          "Toggle checkbox for channel 6"                                               );
		set( KEY_INFO_MIDI_MSG_CH_07,          "Toggle checkbox for channel 7"                                               );
		set( KEY_INFO_MIDI_MSG_CH_08,          "Toggle checkbox for channel 8"                                               );
		set( KEY_INFO_MIDI_MSG_CH_09,          "Toggle checkbox for channel 9"                                               );
		set( KEY_INFO_MIDI_MSG_CH_10,          "Toggle checkbox for channel 10"                                              );
		set( KEY_INFO_MIDI_MSG_CH_11,          "Toggle checkbox for channel 11"                                              );
		set( KEY_INFO_MIDI_MSG_CH_12,          "Toggle checkbox for channel 12"                                              );
		set( KEY_INFO_MIDI_MSG_CH_13,          "Toggle checkbox for channel 13"                                              );
		set( KEY_INFO_MIDI_MSG_CH_14,          "Toggle checkbox for channel 14"                                              );
		set( KEY_INFO_MIDI_MSG_CH_15,          "Toggle checkbox for channel 15"                                              );
		set( KEY_INFO_KEY_TREE,                "Focus key binding Tree"                                                      );
		set( KEY_INFO_KEY_PL,                  "Expand selected nodes (or the whole tree) from Key Bindings"                 );
		set( KEY_INFO_KEY_MIN,                 "Collapse selected nodes (or the whole tree) from Key Bindings"               );
		set( KEY_INFO_KEY_FLD,                 "Focus the field to enter a new key binding"                                  );
		set( KEY_INFO_KEY_FILTER,              "Focus the key binding tree filter field"                                     );
		set( KEY_INFO_KEY_ADD_BTN,             "Press the button to add a key binding"                                       );
		set( KEY_INFO_KEY_RESET_ID_CBX,        "Toggle the checkbox to reset the key bindings of the selected action"        );
		set( KEY_INFO_KEY_RESET_ID_BTN,        "Press the button to reset the key bindings of the selected action"           );
		set( KEY_INFO_KEY_RESET_GLOB_CBX,      "Toggle the checkbox to reset all key bindings globally"                      );
		set( KEY_INFO_KEY_RESET_GLOB_BTN,      "Press the button to reset all key bindings globally"                         );
		set( KEY_MSG_CLOSE,                    "Close the message window"                                                    );
		set( KEY_STRING_FILTER_CLOSE,          "Close the table filter"                                                      );
		set( KEY_STRING_FILTER_CLEAR,          "Clear the table filter"                                                      );
		set( KEY_FILE_SELECT_CLOSE,            "Close the File Selector"                                                     );
		set( KEY_FILE_SELECT_CHARSET_CBX,      "Open the combobox to select the charset"                                     );
		set( KEY_FILE_SELECT_FOREIGN_EXE,      "Focus the Text field for the Command or full Path of the foreign program"    );
		set( KEY_FILE_SELECT_CONFIG_OPEN,      "Open the File-based Configuration Window"                                    );
		set( KEY_FILE_SELECTOR_IMP_MPL,        "In the IMPORT selector, select the MidicaPL Tab"                             );
		set( KEY_FILE_SELECTOR_IMP_MID,        "In the IMPORT selector, select the MIDI Tab"                                 );
		set( KEY_FILE_SELECTOR_IMP_ALDA,       "In the IMPORT selector, select the ALDA Tab"                                 );
		set( KEY_FILE_SELECTOR_IMP_ABC,        "In the IMPORT selector, select the ABC Tab"                                  );
		set( KEY_FILE_SELECTOR_IMP_LY,         "In the IMPORT selector, select the LilyPond Tab"                             );
		set( KEY_FILE_SELECTOR_IMP_MSCORE,     "In the IMPORT selector, select the MuscScore Tab"                            );
		set( KEY_FILE_SELECTOR_SND_FILE,       "In the SOUNDBANK selector, select the File Tab"                              );
		set( KEY_FILE_SELECTOR_SND_URL,        "In the SOUNDBANK selector, select the URL Tab"                               );
		set( KEY_FILE_SELECTOR_SND_URL_FLD,    "In the SOUNDBANK selector, focus the URL text field"                         );
		set( KEY_FILE_SELECTOR_SND_DOWNLOAD,   "In the SOUNDBANK selector, press the DOWNLOAD button"                        );
		set( KEY_FILE_SELECTOR_EXP_MID,        "In the EXPORT selector, select the MIDI Tab"                                 );
		set( KEY_FILE_SELECTOR_EXP_MPL,        "In the EXPORT selector, select the MidicaPL Tab"                             );
		set( KEY_FILE_SELECTOR_EXP_ALDA,       "In the EXPORT selector, select the ALDA Tab"                                 );
		set( KEY_FILE_SELECTOR_EXP_AUDIO,      "In the EXPORT selector, select the Audio Tab"                                );
		set( KEY_FILE_SELECTOR_EXP_ABC,        "In the EXPORT selector, select the ABC Tab"                                  );
		set( KEY_FILE_SELECTOR_EXP_LY,         "In the EXPORT selector, select the LilyPond Tab"                             );
		set( KEY_FILE_SELECTOR_EXP_MSCORE,     "In the EXPORT selector, select the MuscScore Tab"                            );
		set( KEY_FILE_CONF_CLOSE,              "Close the file-based configuration window"                                   );
		set( KEY_FILE_CONF_SAVE,               "Press the button to save the file config"                                    );
		set( KEY_FILE_CONF_RESTORE_SAVED,      "Press the button to restore saved file config"                               );
		set( KEY_FILE_CONF_RESTORE_DEFAULT,    "Press the button to restore default file config"                             );
		set( KEY_DC_CONF_TAB_DEBUG,            "Select the Debug tab in the decompile config window"                         );
		set( KEY_DC_CONF_TAB_NOTE_LENGTH,      "Select the Note Length tab in the decompile config window"                   );
		set( KEY_DC_CONF_TAB_CHORDS,           "Select the Chords tab in the decompile config window"                        );
		set( KEY_DC_CONF_TAB_NOTE_REST,        "Select the Notes/Rests tab in the decompile config window"                   );
		set( KEY_DC_CONF_TAB_KARAOKE,          "Select the Karaoke tab in the decompile config window"                       );
		set( KEY_DC_CONF_TAB_CTRL_CHANGE,      "Select the Control Change tab in the decompile config window"                );
		set( KEY_DC_CONF_TAB_SLICES,           "Select the Extra Slices tab in the decompile config window"                  );
		set( KEY_DC_CONF_ADD_TICK_COMMENTS,    "Toggle Checkbox: Add Tick Comments"                                          );
		set( KEY_DC_CONF_ADD_CONFIG,           "Toggle Checkbox: Add Configuration"                                          );
		set( KEY_DC_CONF_ADD_SCORE,            "Toggle Checkbox: Add Quality Score"                                          );
		set( KEY_DC_CONF_ADD_STATISTICS,       "Toggle Checkbox: Add Quality Statistics"                                     );
		set( KEY_DC_CONF_ADD_STRATEGY_STAT,    "Toggle Checkbox: Add Stragegy Statistics"                                    );
		set( KEY_DC_CONF_NOTE_LENGTH_STRATEGY, "Open Selection: Note Length Strategy"                                        );
		set( KEY_DC_CONF_MIN_TARGET_TICKS_ON,  "Focus the text field for the Min target ticks"                               );
		set( KEY_DC_CONF_MAX_TARGET_TICKS_ON,  "Focus the text field for the Max target ticks"                               );
		set( KEY_DC_CONF_MIN_DUR_TO_KEEP,      "Focus the text field for the Min Duration to keep"                           );
		set( KEY_DC_CONF_MAX_DUR_TO_KEEP,      "Focus the text field for the Max Duration to keep"                           );
		set( KEY_DC_CONF_TOL_TICK_LEN,         "Focus the text field for the Note Length Tick Tolerance"                     );
		set( KEY_DC_CONF_TOL_DUR_RATIO,        "Focus the text field for the Duration Ratio Tolerance"                       );
		set( KEY_DC_CONF_CRD_PREDEFINED,       "Toggle Checkbox: Use Predefined Chords"                                      );
		set( KEY_DC_CONF_CRD_NOTE_ON,          "Focus the text field for the chord Note-ON tick tolerance"                   );
		set( KEY_DC_CONF_CRD_NOTE_OFF,         "Focus the text field for the chord Note-OFF tick tolerance"                  );
		set( KEY_DC_CONF_CRD_VELOCITY,         "Focus the text field for the chord velocity tolerance"                       );
		set( KEY_DC_CONF_USE_DOT_NOTES,        "Toggle Checkbox: Use dotted notes"                                           );
		set( KEY_DC_CONF_USE_DOT_RESTS,        "Toggle Checkbox: Use dotted rests"                                           );
		set( KEY_DC_CONF_USE_TRIP_NOTES,       "Toggle Checkbox: Use tripletted notes"                                       );
		set( KEY_DC_CONF_USE_TRIP_RESTS,       "Toggle Checkbox: Use tripletted rests"                                       );
		set( KEY_DC_CONF_USE_KARAOKE,          "Toggle Checkbox: Use Karaoke"                                                );
		set( KEY_DC_CONF_ALL_SYL_ORP,          "Toggle Checkbox: Regard all syllables as orphaned"                           );
		set( KEY_DC_CONF_KAR_ORPHANED,         "Open Orphaned Syllables Selection"                                           );
		set( KEY_DC_CONF_KAR_ONE_CH,           "Toggle Checkbox: All Lyrics in One Channel"                                  );
		set( KEY_DC_CONF_CTRL_CHANGE_MODE,     "Open Control Change Mode Selection"                                          );
		set( KEY_DC_CONF_FLD_GLOB_SINGLE,      "Focus text field: Add one split at tick..."                                  );
		set( KEY_DC_CONF_BTN_GLOB_SINGLE,      "Press Button: Add Single Tick"                                               );
		set( KEY_DC_CONF_FLD_GLOB_EACH,        "Focus text field: Add many splits (distance)"                                );
		set( KEY_DC_CONF_FLD_GLOB_FROM,        "Focus text field: Add many splits (from)"                                    );
		set( KEY_DC_CONF_FLD_GLOB_TO,          "Focus text field: Add many splits (to)"                                      );
		set( KEY_DC_CONF_BTN_GLOB_RANGE,       "Press Button: Add many splits"                                               );
		set( KEY_DC_CONF_AREA_GLOB_ALL,        "Focus text area: Edit Extra Split Ticks Directly"                            );
		set( KEY_DC_CONF_BTN_GLOB_ALL,         "Press Button: Update Ticks"                                                  );
		set( KEY_AU_CONF_ENCODING,             "Open Selection: Encoding"                                                    );
		set( KEY_AU_CONF_FLD_SAMPLE_SIZE_BITS, "Focus field: Sample Size in Bits"                                            );
		set( KEY_AU_CONF_FLD_SAMPLE_RATE,      "Focus field: Sample Rate"                                                    );
		set( KEY_AU_CONF_CHANNELS,             "Open Selection: Channels"                                                    );
		set( KEY_AU_CONF_IS_BIG_ENDIAN,        "Toggle Checkbox: Big Endian"                                                 );
		set( KEY_EXPORT_RESULT_CLOSE,          "Close the Export Result Window"                                              );
		set( KEY_EXPORT_RESULT_SHORT,          "Toggle Checkbox: Show Ignored Short Message"                                 );
		set( KEY_EXPORT_RESULT_META,           "Toggle Checkbox: Show Ignored Meta Message"                                  );
		set( KEY_EXPORT_RESULT_SYSEX,          "Toggle Checkbox: Show Ignored SysEx Message"                                 );
		set( KEY_EXPORT_RESULT_SKIPPED_RESTS,  "Toggle Checkbox: Show Skipped Rests"                                         );
		set( KEY_EXPORT_RESULT_OFF_NOT_FOUND,  "Toggle Checkbox: Show Note-OFF not found"                                    );
		set( KEY_EXPORT_RESULT_CRD_GRP_FAILED, "Toggle Checkbox: Show Chord grouping failed"                                 );
		set( KEY_EXPORT_RESULT_OTHER,          "Toggle Checkbox: Show other warnings"                                        );
		set( KEY_EXPORT_RESULT_FILTER,         "Open the Table Filter"                                                       );
		
		// UiView
		set( CONFIGURATION,                "Configuration"                 );
		set( LANGUAGE,                     "Language"                      );
		set( NOTE_SYSTEM,                  "Note System"                   );
		set( HALF_TONE_SYMBOL,             "Half Tone Symbols"             );
		set( SHARP_FLAT_DEFAULT,           "Default Half Tone"             );
		set( OCTAVE_NAMING,                "Octave Naming"                 );
		set( SYNTAX,                       "Syntax"                        );
		set( PERCUSSION,                   "Percussion IDs"                );
		set( INSTRUMENT_IDS,               "Instrument IDs"                );
		set( DRUMKIT_IDS,                  "Drumkit IDs"                   );
		set( TITLE_MAIN_WINDOW,            "Midica " + Midica.VERSION      );
		set( SHOW_INFO,                    "Info & Configuration Details"  );
		set( SHOW_INFO_FROM_PLAYER,        "Info & Configuration"          );
		set( IMPORT,                       "Import"                        );
		set( PLAYER,                       "Player"                        );
		set( EXPORT,                       "Export"                        );
		set( TRANSPOSE_LEVEL,              "Transpose Level"               );
		set( IMPORT_FILE,                  "Import file"                   );
		set( IMPORTED_FILE,                "Imported file"                 );
		set( IMPORTED_TYPE,                "File Type"                     );
		set( IMPORTED_TYPE_MIDI,           "MIDI"                          );
		set( IMPORTED_TYPE_MPL,            "MidicaPL"                      );
		set( IMPORTED_TYPE_ALDA,           "ALDA"                          );
		set( IMPORTED_TYPE_ABC,            "ABC"                           );
		set( IMPORTED_TYPE_LY,             "LilyPond"                      );
		set( IMPORTED_TYPE_MSCORE,         "Imported by MuseScore"         );
		set( UNKNOWN_NOTE_NAME,            "unknown"                       );
		set( UNKNOWN_PERCUSSION_NAME,      "unknown"                       );
		set( UNKNOWN_DRUMKIT_NAME,         "unknown"                       );
		set( UNKNOWN_SYNTAX,               "?"                             );
		set( UNKNOWN_INSTRUMENT,           "unknown"                       );
		set( SOUNDBANK,                    "Soundbank"                     );
		set( CURRENT_SOUNDBANK,            "Current Soundbank"             );
		set( REMEMBER_SOUND,               "Remember"                      );
		set( REMEMBER_SOUND_TT,            "Load the chosen soundbank automatically at the next startup" );
		set( REMEMBER_IMPORT,              "Remember"                      );
		set( REMEMBER_IMPORT_TT,           "Load the chosen file automatically at the next startup" );
		set( PLAYER_BUTTON,                "Start Player"                  );
		set( UNCHOSEN_FILE,                "no file loaded"                );
		set( SB_LOADED_BY_SOURCE,          "[loaded by MidicaPL file]"     );
		set( CHOOSE_FILE,                  "select file"                   );
		set( CHOOSE_FILE_EXPORT,           "select file"                   );
		set( EXPORT_FILE,                  "Export file"                   );
		set( CONF_ERROR_OK,                "Configuration OK"              );
		set( ERROR_NOT_YET_IMPLEMENTED,    "This functionality is not yet implemented" );
		
		// FileSelector / MidicaFileChooser
		set( TITLE_FILE_SELECTOR,       "Midica Choose File"                                   );
		set( CONFIG_ICON_TOOLTIP,       "Config Options"                                       );
		set( CONFIG_ICON_TOOLTIP_WRONG, "Config contains errors"                               );
		set( CONFIG_ICON_TOOLTIP_OK,    "Config formally correct"                              );
		set( TAB_SOUND_FILE,            "File"                                                 );
		set( TAB_SOUND_URL,             "URL"                                                  );
		set( TAB_MIDI,                  "MIDI"                                                 );
		set( TAB_MIDICAPL,              "MidicaPL"                                             );
		set( TAB_ALDA,                  "ALDA"                                                 );
		set( TAB_AUDIO,                 "Audio"                                                );
		set( TAB_ABC,                   "ABC"                                                  );
		set( TAB_LY,                    "LilyPond"                                             );
		set( TAB_MSCORE,                "MuseScore"                                            );
		set( CHARSET,                   "Charset"                                              );
		set( CHARSET_DESC_MPL_READ,     "Encoding of the source file:"                         );
		set( CHARSET_DESC_MID_READ,     "Default encoding of text-based messages in the source file. Used if neither a BOM nor a {@...} tag is found:" );
		set( CHARSET_DESC_MPL_WRITE,    "Encoding of the file to be saved:"                    );
		set( CHARSET_DESC_MID_WRITE,    "Encoding for text-based messages in the target file:" );
		set( FOREIGN_URL,               "URL"                                                  );
		set( FOREIGN_PROG,              "Program"                                              );
		set( FOREIGN_PROG_DESC,         "Command or full path to the program %s:"              );
		set( FOREIGN_PROG_ALDA,         "ALDA"                                                 );
		set( FOREIGN_PROG_ABCMIDI,      "abcMIDI"                                              );
		set( FOREIGN_PROG_LY,           "LilyPond"                                             );
		set( FOREIGN_PROG_MIDI2ABC,     "midi2abc"                                             );
		set( FOREIGN_PROG_MIDI2LY,      "midi2ly"                                              );
		set( FOREIGN_PROG_MSCORE,       "MuseScore"                                            );
		set( DIRECT_IMPORT,             "Directly import the exported file"                    );
		set( FILE_OPTIONS,              "Options:"                                             );
		set( SOUND_URL,                 "Soundbank URL"                                        );
		set( SOUND_DOWNLOAD,            "Download Sound"                                       );
		
		// Foreign
		set( FOREIGN_CREATE_TMPDIR,  "Failed to create temporary directory. Error Message: "  );
		set( FOREIGN_READ_TMPDIR,    "Failed to read temporary directory. Error Message: "    );
		set( FOREIGN_CREATE_TMPFILE, "Failed to create temporary file. Error Message: "       );
		set( FOREIGN_EX_CODE,        "<html><b>The following command failed:</b><br>%s<br><br><b>Exit Code:</b><br>%s<br><br><b>Standard error:</b><br>%s<br><br><b>Standard output:</b><br>%s<br>" );
		set( FOREIGN_EX_INTERRUPTED, "%s has been interrupted"                                );
		set( FOREIGN_EX_EXECUTE,     "<html>Failed to execute %s.<br>Make sure that %s is installed.<br>Make sure that the command or path is correct.<br>The failing command or path was:<br><b>%s</b>" );
		set( FOREIGN_EX_NO_EXE,      "<html>Cannot start %s without a command or path.<br>Please enter a command or path in the file chooser." );
		
		// DecompileConfigView
		set( TITLE_DC_CONFIG,              "Midica - Decompilation Settings"                  );
		set( DC_TAB_DEBUG,                 "Debugging"                                        );
		set( DC_TAB_NOTE_LENGTH,           "Note Length Calculation"                          );
		set( DC_TAB_CHORDS,                "Chords"                                           );
		set( DC_TAB_NOTE_REST,             "Notes / Rests"                                    );
		set( DC_TAB_KARAOKE,               "Karaoke Settings"                                 );
		set( DC_TAB_CTRL_CHANGE,           "Control Change"                                   );
		set( DC_TAB_SLICE,                 "Extra Slices"                                     );
		set( DC_TABINFO_DEBUG,             "<html>Settings to control additional debugging information that's added as code comments in the target file."
		                                 + "<br>The resulting MIDI sequence is not affected by these settings." );
		set( DC_TABINFO_NOTE_LENGTH,       "<html>Settings to control how a note length is calculated. Therefore the following strategies can be used:"
		                                 + "<br><b>&mdash; Next ON:</b> Adjusts the note length so that it ends as late as possible before the following note begins."
		                                 + "<br><b>&mdash; Duration*:</b> Tries to avoid a duration change, if possible. The note length is adjusted as close as possible to the previous note's duration."
		                                 + "<br><b>&mdash; Press length:</b> Uses the press length (Note-ON to Note-OFF) and chooses the lowest possible length with at least this number of ticks."
		                                 + "<br>The priority combobox configures which strategy to prefer if more than one is possible."
		                                 + "<br>The other fields can be used to fine-tune the strategies."
		                                 + "<br><b>*</b> In ALDA the duration is called 'quantization'" );
		set( DC_TABINFO_CHORDS,            "<html>Settings to control chords."
		                                 + "<br>Pre-defined chords are only used for MidicaPL, otherwise ignored."
		                                 + "<br>The other settings control how different the properties of two notes can be to be still regarded as a part of the same chord." );
		set( DC_TABINFO_NOTE_REST,         "<html>Settings to control which note or rest lengths to use" );
		set( DC_TABINFO_KARAOKE,           "<html>Karaoke-related settings. Only used by the MidicaPL decompiler. Otherwise ignored."
		                                 + "<br><br>A syllable is <b>normal</b>, if there's a Note-ON beginning in the same tick. Otherwise it is <b>orphaned</b>."
		                                 + "<br>Normal syllables are implemented as an option to a note or chord."
		                                 + "<br>Orphaned Syllables are implemented as an option of a rest inside of a nestable block."
		                                 + "<br>This block can either be an <b>Inline Block</b> starting together with the previous Note-ON,"
		                                 + "<br>or a <b>Slice Begin Block</b>, starting at the beginning of a slice."
		                                 + "<br><b>Inline Blocks</b> are more accurate and keep the lyrics closer to the right tick."
		                                 + "<br>They are smaller and occur more often."
		                                 + "<br><b>Slice Begin Blocks</b> produce cleaner code regarding the notes. But the placement of the lyrics in the timeline is less precise."
		                                 + "<br>They are larger and occur less often."
		                                 + "<br><br>Using only one channel for all the lyrics keeps the lyrics closer together, making the karaoke-related code more readable."
		                                 + "<br>But it could result in more orphaned syllables requiring more nestable blocks and rests." );
		set( DC_TABINFO_CTRL_CHANGE,       "<html>Settings related to Control Change Events. Not yet implemented. Feature for the future." );
		set( DC_TABINFO_SLICE,             "<html>Settings to add extra slices."
		                                 + "<br>By default the sequence is split into slices in each tick that contains certain META messages."
		                                 + "<br>Here you can add extra splitting for certain ticks." );
		set( DC_ADD_TICK_COMMENT,          "Add Tick Comments"                                );
		set( DC_ADD_CONFIG,                "Add Configuration"                                );
		set( DC_ADD_SCORE,                 "Add Quality Score"                                );
		set( DC_ADD_STATISTICS,            "Add Quality Statistics"                           );
		set( DC_ADD_STRATEGY_STAT,         "Add Strategy Statistics"                          );
		set( NOTE_LENGTH_STRATEGY,         "Priority of strategies"                           );
		set( MIN_TARGET_TICKS_NEXT_ON,     "<html>Minimum note length for<br>the 'Next ON' strategy" );
		set( MAX_TARGET_TICKS_NEXT_ON,     "<html>Maximum note length for<br>the 'Next ON' strategy" );
		set( MIN_DURATION_TO_KEEP,         "Minimum Duration to keep" );
		set( MIN_DURATION_TO_KEEP_D,       "<html>Minimum Duration for using the duration strategy."
		                                 + "<br>1.0 = 100%; &nbsp; 0.1 = 10%; &nbsp; 0.01 = 1%" );
		set( MAX_DURATION_TO_KEEP,         "Maximum Duration to keep" );
		set( MAX_DURATION_TO_KEEP_D,       "<html>Maximum Duration for using the duration strategy."
		                                 + "<br>1.0 = 100%; &nbsp; 0.1 = 10%; &nbsp; 0.01 = 1%" );
		set( LENGTH_TICK_TOLERANCE,        "<html>Note Length Tick Tolerance"
		                                 + "<br>(used for all strategies)" );
		set( LENGTH_TICK_TOLERANCE_D,      "<html>If the calculated note length is so many ticks longer than a note length <b>L</b>,<br>"
		                                 + "then <b>L</b> is still used instead of the next longer note length." );
		set( DURATION_RATIO_TOLERANCE,     "<html>Duration Ratio Tolerance"
		                                 + "<br>(used for all strategies)" );
		set( DURATION_RATIO_TOLERANCE_D,   "<html>The duration is not changed, if the difference between the old duration and the"
		                                 + "<br>new (calculated) duration is smaller than this value."
		                                 + "<br>0.1 = 10%; &nbsp; 0.01 = 1%; &nbsp; 0.001 = 0.1%" );
		set( USE_PRE_DEFINED_CHORDS,       "Pre-defined Chords"                                                        );
		set( USE_PRE_DEFINED_CHORDS_D,     "Use Pre-defined Chords (instead of inline chords)"                         );
		set( CHORD_NOTE_ON_TOLERANCE,      "Note-ON tolerance"                                                         );
		set( CHORD_NOTE_ON_TOLERANCE_D,    "Maximum Note-ON tick difference (still regarded as the same chord)"        );
		set( CHORD_NOTE_OFF_TOLERANCE,     "Note-OFF tolerance"                                                        );
		set( CHORD_NOTE_OFF_TOLERANCE_D,   "Maximum Note-OFF tick difference (still regarded as the same chord)"       );
		set( CHORD_VELOCITY_TOLERANCE,     "Velocity tolerance"                                                        );
		set( CHORD_VELOCITY_TOLERANCE_D,   "Maximum Velocity difference (still regarded as the same chord)"            );
		set( USE_DOTTED_NOTES,             "Use dotted notes"                                                          );
		set( USE_DOTTED_RESTS,             "Use dotted rests"                                                          );
		set( USE_TRIPLETTED_NOTES,         "Use tripletted notes"                                                      );
		set( USE_TRIPLETTED_RESTS,         "Use tripletted rests"                                                      );
		set( USE_KARAOKE,                  "Use Karaoke"                                                               );
		set( USE_KARAOKE_D,                "Includes lyrics, if checked. Otherwise, ignores all other Karaoke Settings." );
		set( ALL_SYLLABLES_ORPHANED,       "Regard all syllables as orphaned"                                          );
		set( ALL_SYLLABLES_ORPHANED_D,     "If checked, no syllable is assigned to any note."                          );
		set( ORPHANED_SYLLABLES,           "Orphaned Syllables"                                                        );
		set( ORPHANED_SYLLABLES_D,         "How to treat syllables that appear in a tick without any Note-ON"          );
		set( CTRL_CHANGE_MODE,             "Control Change Mode"                                                       );
		set( CTRL_CHANGE_MODE_D,           "Where to put control changes"                                              );
		set( DC_INLINE_BLOCK,              "Inline Block"                                                              );
		set( DC_SLICE_BEGIN_BLOCK,         "Slice Begin Block"                                                         );
		set( DC_STRAT_NEXT_DURATION_PRESS, "<html><b>1.</b> Next ON, &nbsp;&nbsp;<b>2.</b> Duration,&nbsp;&nbsp;<b>3.</b> Press length" );
		set( DC_STRAT_DURATION_NEXT_PRESS, "<html><b>1.</b> Duration,&nbsp;&nbsp;<b>2.</b> Next ON, &nbsp;&nbsp;<b>3.</b> Press length" );
		set( DC_STRAT_NEXT_PRESS,          "<html><b>1.</b> Next ON, &nbsp;&nbsp;<b>2.</b> Press length"               );
		set( DC_STRAT_DURATION_PRESS,      "<html><b>1.</b> Duration,&nbsp;&nbsp;<b>2.</b> Press length"               );
		set( DC_STRAT_PRESS,               "Press length only"                                                         );
		set( KAR_ONE_CHANNEL,              "All Lyrics in one channel"                                                 );
		set( KAR_ONE_CHANNEL_D,            "If checked, all lyrics are assigned to the same channel"                   );
		set( ADD_GLOBAL_AT_TICK,           "Add one split at tick:"                                                    );
		set( ADD_GLOBAL_EACH,              "Add many splits. Tick distance:"                                           );
		set( ADD_GLOBAL_FROM,              "...starting from tick:"                                                    );
		set( ADD_GLOBAL_TO,                "...ending not later than tick:"                                            );
		set( DC_ALL_TICKS,                 "Edit directly"                                                             );
		set( BTN_ADD_TICK,                 "Add Single Tick"                                                           );
		set( BTN_ADD_TICKS,                "Add Tick Range"                                                            );
		set( BTN_UPDATE_TICKS,             "Update Ticks"                                                              );
		set( DC_RESTORE,                   "Restore saved settings"                                                    );
		set( DC_RESTORE_DEFAULTS,          "Restore default settings"                                                  );
		set( DC_SAVE,                      "Save settings"                                                             );
		set( CHANGED_IN_CONF_FILE,         "Manually changed in Config file"                                           );
		set( TICKS_FOR_TARGET_PPQ,         "ticks @ 480 PPQ"                                                           );
		
		// AudioExportView / AudioExportController
		set( TITLE_AU_CONFIG,                  "Midica - Audio Export Settings"                            );
		set( AUDIO_ENCODING,                   "Encoding"                                                  );
		set( AUDIO_SAMPLE_SIZE_BITS,           "Sample Size in Bits"                                       );
		set( AUDIO_SAMPLE_SIZE_BITS_D,         "<html>Must be divisible by 8.<br>"
		                                     + "For PCM_FLOAT, only <b>32</b> and <b>64</b> are allowed."   );
		set( AUDIO_SAMPLE_RATE,                "Sample Rate"                                               );
		set( AUDIO_SAMPLE_RATE_D,              "Samples per second (Hz)"                                   );
		set( AUDIO_CHANNELS,                   "Channels"                                                  );
		set( AUDIO_IS_BIG_ENDIAN,              "Big Endian"                                                );
		set( AUDIO_IS_BIG_ENDIAN_D,            "<html><b>Big endian</b> (if checked) or <b>Little endian</b> (otherwise)" );
		set( AUDIO_FILE_TYPE,                  "File Type"                                                 );
		set( AUDIO_FILE_TYPE_D,                "The file type will be chosen based on the file extension.<br>"
		                                     + "The operating system supports the following file types:"   );
		set( AU_MONO,                          "Mono"                                                      );
		set( AU_STEREO,                        "Stereo"                                                    );
		
		// InfoView
		set( TITLE_INFO_VIEW,                        "Midica Info"                   );
		set( TAB_CONFIG,                             "Configuration"                 );
		set( TAB_SOUNDBANK,                          "Soundbank"                     );
		set( TAB_MIDI_SEQUENCE,                      "MIDI Sequence"                 );
		set( TAB_KEYBINDINGS,                        "Key Bindings"                  );
		set( TAB_ABOUT,                              "About"                         );
		set( TAB_NOTE_DETAILS,                       "Note Table"                    );
		set( TAB_PERCUSSION_DETAILS,                 "Percussion IDs"                );
		set( TAB_SYNTAX_DETAILS,                     "Commands"                      );
		set( TAB_SOUNDBANK_INFO,                     "General Info"                  );
		set( TAB_SOUNDBANK_INSTRUMENTS,              "Instruments & Drum Kits"       );
		set( TAB_SOUNDBANK_RESOURCES,                "Resources"                     );
		set( TAB_MIDI_SEQUENCE_INFO,                 "General Info"                  );
		set( TAB_MIDI_KARAOKE,                       "Karaoke Info"                  );
		set( TAB_BANK_INSTR_NOTE,                    "Banks, Instruments, Notes"     );
		set( TAB_MESSAGES,                           "MIDI Messages"                 );
		set( SOUNDBANK_VENDOR,                       "Vendor"                        );
		set( SOUNDBANK_CREA_DATE,                    "Creation Date"                 );
		set( SOUNDBANK_CREA_TOOLS,                   "Creation Tools"                );
		set( SOUNDBANK_PRODUCT,                      "Product"                       );
		set( SOUNDBANK_TARGET_ENGINE,                "Target Sound Engine"           );
		set( COPYRIGHT,                              "Copyright"                     );
		set( SOFTWARE_VERSION,                       "Produced with"                 );
		set( SOFTWARE_DATE,                          "released"                      );
		set( TICK_LENGTH,                            "Length (Ticks)"                );
		set( TIME_LENGTH,                            "Length (Time)"                 );
		set( RESOLUTION,                             "Resolution"                    );
		set( RESOLUTION_UNIT,                        "Ticks / Quarter Note"          );
		set( NUMBER_OF_TRACKS,                       "Number of tracks"              );
		set( TEMPO_BPM,                              "Tempo in BPM (Beats per Minute or Quarter Notes per Minute)" );
		set( TEMPO_MPQ,                              "Tempo in MPQ (Microseconds per Quarter Note)"                );
		set( AVERAGE,                                "Average"                       );
		set( MIN,                                    "Minimum"                       );
		set( MAX,                                    "Maximum"                       );
		set( CHANNEL,                                "Channel"                       );
		set( KARAOKE_TYPE,                           "Type"                          );
		set( SONG_TITLE,                             "Title"                         );
		set( COMPOSER,                               "Composer"                      );
		set( LYRICIST,                               "Lyricist"                      );
		set( ARTIST,                                 "Artist"                        );
		set( KARAOKE_COPYRIGHT,                      "Copyright, etc."               );
		set( KARAOKE_INFO,                           "Other Info"                    );
		set( KARAOKE_GENERAL,                        "General"                       );
		set( KARAOKE_SOFT_KARAOKE,                   "Soft Karaoke"                  );
		set( LYRICS,                                 "Lyrics"                        );
		set( TOTAL,                                  "Total"                         );
		set( PER_CHANNEL,                            "Per Channel"                   );
		set( BANK,                                   "Bank"                          );
		set( COLLAPSE_BUTTON,                        "-"                             );
		set( COLLAPSE_TOOLTIP,                       "Collapse selected nodes (or the whole tree)" );
		set( EXPAND_BUTTON,                          "+"                             );
		set( EXPAND_TOOLTIP,                         "Expand selected nodes (or the whole tree)" );
		set( DETAILS,                                "Details"                       );
		set( SAMPLES_TOTAL,                          "Samples (Total)"               );
		set( SAMPLES_AVERAGE,                        "Samples (Average)"             );
		set( FRAMES,                                 "Frames"                        );
		set( BYTES,                                  "Bytes"                         );
		set( SEC,                                    "Sec"                           );
		set( BROADCAST_MSG,                          "Broadcast"                     );
		set( INVALID_MSG,                            "Invalid Message"               );
		set( SB_INSTR_CAT_CHROMATIC,                 "Chromatic Instruments"         );
		set( SB_INSTR_CAT_DRUMKIT_SINGLE,            "Drum Kits (single channel)"    );
		set( SB_INSTR_CAT_DRUMKIT_MULTI,             "Drum Kits (multi channel)"     );
		set( SB_INSTR_CAT_UNKNOWN,                   "Instruments with unknown Type" );
		set( SB_RESOURCE_CAT_SAMPLE,                 "Samples"                       );
		set( SB_RESOURCE_CAT_LAYER,                  "Layers"                        );
		set( SB_RESOURCE_CAT_UNKNOWN,                "Resources with unknown Type"   );
		set( SOUNDBANK_DRUMKITS,                     "Drum Kits"                     );
		set( SINGLE_CHANNEL,                         "Single Channel"                );
		set( MULTI_CHANNEL,                          "Multi Channel"                 );
		set( UNKNOWN,                                "Unknown"                       );
		set( UNSET,                                  "Not Set"                       );
		set( DATE,                                   "Date (UTC)"                    );
		set( AUTHOR,                                 "Author"                        );
		set( SOURCE_URL,                             "Source"                        );
		set( WEBSITE,                                "Website"                       );
		set( LINK_TOOLTIP,                           "(Click to open in Browser)"    );
		set( TIMESTAMP_FORMAT,                       "yyyy-MM-dd HH:mm:ss"           );
		set( NAME,                                   "Name"                          );
		set( FILE,                                   "File"                          );
		set( SOUND_SOURCE,                           "Source"                        );
		set( VERSION,                                "Version"                       );
		set( DESCRIPTION,                            "Description"                   );
		set( INFO_COL_NOTE_NUM,                      "Number"                        );
		set( INFO_COL_NOTE_NAME,                     "Name"                          );
		set( INFO_COL_NOTE_ALT,                      "Alternative Names"             );
		set( INFO_COL_SYNTAX_NAME,                   "ID"                            );
		set( INFO_COL_SYNTAX_DESC,                   "Description"                   );
		set( INFO_COL_SYNTAX_SHORTCUT,               "Keyword"                       );
		set( INFO_COL_PERC_NUM,                      "Number"                        );
		set( INFO_COL_PERC_ID_LONG,                  "Long ID"                       );
		set( INFO_COL_PERC_ID_SHORT,                 "Short ID"                      );
		set( INFO_COL_INSTR_NUM,                     "Number"                        );
		set( INFO_COL_INSTR_NAME,                    "Instrument ID"                 );
		set( INFO_COL_DRUMKIT_NUM,                   "Number"                        );
		set( INFO_COL_DRUMKIT_NAME,                  "Drumkit ID"                    );
		set( INFO_COL_SB_INSTR_PROGRAM,              "Number"                        );
		set( INFO_COL_SB_INSTR_BANK,                 "Bank"                          );
		set( INFO_COL_SB_INSTR_NAME,                 "Name"                          );
		set( INFO_COL_SB_INSTR_CHANNELS,             "Channels"                      );
		set( INFO_COL_SB_INSTR_KEYS,                 "Keys"                          );
		set( INFO_COL_SB_RES_INDEX,                  "Index"                         );
		set( INFO_COL_SB_RES_TYPE,                   "Type"                          );
		set( INFO_COL_SB_RES_NAME,                   "Name"                          );
		set( INFO_COL_SB_RES_FRAMELENGTH,            "Frames"                        );
		set( INFO_COL_SB_RES_FORMAT,                 "Format"                        );
		set( INFO_COL_SB_RES_CLASS,                  "Class"                         );
		set( INFO_COL_MSG_TICK,                      "Tick"                          );
		set( INFO_COL_MSG_STATUS_BYTE,               "St"                            );
		set( INFO_COL_MSG_TRACK,                     "Tr"                            );
		set( INFO_COL_MSG_CHANNEL,                   "Ch"                            );
		set( INFO_COL_MSG_LENGTH,                    "Len"                           );
		set( INFO_COL_MSG_SUMMARY,                   "Summary"                       );
		set( INFO_COL_MSG_TYPE,                      "Type"                          );
		set( INFO_COL_MSG_TT_STATUS,                 "Status Byte"                   );
		set( INFO_COL_MSG_TT_TRACK,                  "Track Number"                  );
		set( INFO_COL_MSG_TT_CHANNEL,                "Channel"                       );
		set( INFO_COL_MSG_TT_LENGTH,                 "Message Length in Bytes"       );
		set( TOOLTIP_BANK_MSB,                       "MSB"                           );
		set( TOOLTIP_BANK_LSB,                       "LSB"                           );
		set( TOOLTIP_BANK_FULL,                      "Bank Number"                   );
		set( SYNTAX_CAT_DEFINITION,                  "Definition Commands"           );
		set( SYNTAX_CAT_EXECUTE,                     "Execution Commands"            );
		set( SYNTAX_CAT_VAR_AND_CONST,               "Constants/Variables/Parameters" );
		set( SYNTAX_CAT_OPTION,                      "Option Syntax"                 );
		set( SYNTAX_CAT_CONDITON,                    "Conditions"                    );
		set( SYNTAX_CAT_GLOBAL,                      "Global Commands"               );
		set( SYNTAX_CAT_OTHER,                       "Other Commands"                );
		set( SYNTAX_CAT_META,                        "Meta Commands"                 );
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
		set( MSG_FILTER_CHANNEL_INDEP,               "Ch-Indep."                     );
		set( MSG_FILTER_CHANNEL_DEP,                 "Ch-Dep."                       );
		set( MSG_FILTER_SELECTED_NODES,              "Sel. Nodes"                    );
		set( MSG_FILTER_LIMIT_TICKS,                 "Limit Ticks"                   );
		set( MSG_FILTER_TICK_FROM,                   "From"                          );
		set( MSG_FILTER_TICK_TO,                     "To"                            );
		set( MSG_FILTER_LIMIT_TRACKS,                "Limit Tracks"                  );
		set( MSG_FILTER_SHOW_IN_TREE,                "Show in Tree"                  );
		set( MSG_FILTER_AUTO_SHOW,                   "Automatically"                 );
		set( MSG_FLTR_TT_CHANNEL_INDEP,              "Show Channel-Independent Messages"                  );
		set( MSG_FLTR_TT_CHANNEL_DEP,                "Show Channel-Dependent Messages"                    );
		set( MSG_FLTR_TT_CHANNEL_SINGLE,             "Show Messages of channel"                           );
		set( MSG_FLTR_TT_SELECTED_NODES,             "Show ONLY message types under selected tree nodes." );
		set( MSG_FLTR_TT_LIMIT_TICKS,                "Show only messages in a certain tick range."        );
		set( MSG_FLTR_TT_LIMIT_TRACKS,               "Show only messages from certain track numbers"      );
		set( MSG_FLTR_TT_TRACKS,                     "<html>Track numbers begin with 0.<br>" +
		                                             "Several track numbers can be separated by a comma or<br>" +
		                                             "given as a range (separated by a minus).<br>" +
		                                             "Several ranges can also be given, separated by a comma.<br>" +
		                                             "Example:<br>" +
		                                             "<b>0-3,6,8-11</b>" );
		set( MSG_FLTR_TT_SHOW_IN_TREE,               "Show selected messsage in Tree"                     );
		set( MSG_FLTR_TT_AUTO_SHOW,                  "Show Selected Message in the tree automatically without pushing the button" );
		set( MSG_FLTR_TT_VISIBLE,                    "Currently shown Messages (matching the filter)" );
		set( MSG_FLTR_TT_TOTAL,                      "All messages"                                   );
		set( MSG_DETAILS_TICK_SG,                    "Tick:"                               );
		set( MSG_DETAILS_TICK_PL,                    "Ticks:"                              );
		set( MSG_DETAILS_LENGTH,                     "Length:"                             );
		set( MSG_DETAILS_STATUS_BYTE,                "Status Byte:"                        );
		set( MSG_DETAILS_TRACK_SG,                   "Track:"                              );
		set( MSG_DETAILS_TRACK_PL,                   "Tracks:"                             );
		set( MSG_DETAILS_CHANNEL_SG,                 "Channel:"                            );
		set( MSG_DETAILS_CHANNEL_PL,                 "Channels:"                           );
		set( MSG_DETAILS_META_TYPE,                  "Meta Type:"                          );
		set( MSG_DETAILS_VENDOR,                     "Manufacturer:"                       );
		set( MSG_DETAILS_DEVICE_ID_SG,               "Device ID:"                          );
		set( MSG_DETAILS_DEVICE_ID_PL,               "Device IDs:"                         );
		set( MSG_DETAILS_SUB_ID_1,                   "Sub ID 1"                            );
		set( MSG_DETAILS_SUB_ID_2,                   "Sub ID 2"                            );
		set( MSG_DETAILS_CTRL_BYTE,                  "Controller Byte:"                    );
		set( MSG_DETAILS_RPN_BYTE_SG,                "RPN Byte:"                           );
		set( MSG_DETAILS_RPN_BYTE_PL,                "RPN Bytes:"                          );
		set( MSG_DETAILS_NRPN_BYTE_SG,               "NRPN Byte:"                          );
		set( MSG_DETAILS_NRPN_BYTE_PL,               "NRPN Bytes:"                         );
		set( MSG_DETAILS_TEXT_SG,                    "Text:"                               );
		set( MSG_DETAILS_TEXT_PL,                    "Texts:"                              );
		set( MSG_DETAILS_MESSAGE,                    "Message (Hex):"                      );
		set( MSG_DETAILS_DESCRIPTION,                "Description"                         );
		set( KB_CATEGORIES,                          "Category or Window"                  );
		set( KB_CATEGORY,                            "Category: "                          );
		set( KB_FILTER,                              "Filter:"                             );
		set( KB_ACTION,                              "Action: "                            );
		set( KB_HINT,                                "Hint: "                              );
		set( KB_HINT_TXT,                            "Key binding changes will be active after next opening of the according window(s)." );
		set( KB_CONFIGURED,                          "Currently configured key bindings: " );
		set( KB_ENTER,                               "Press Key Combination: "             );
		set( KB_ADD,                                 "Add Key Binding"                     );
		set( KB_ADD_BTN,                             "Add Key Binding"                     );
		set( KB_REMOVE,                              "Remove"                              );
		set( KB_RESET_ID_CBX,                        "<html>Reset Key Binding(s) for this Action<br>(Must be checked before clicking the reset button)" );
		set( KB_RESET_ID_BTN,                        "Reset Key Binding(s) for this Action" );
		set( KB_RESET_GLOB_CBX,                      "<html>Reset ALL Key Bindings GLOBALLY<br>(Must be checked before clicking the reset button)" );
		set( KB_RESET_GLOB_BTN,                      "Reset ALL Key Bindings GLOBALLY"     );
		set( KB_ERROR_NO_BINDING_PRESSED,            "<html>No key binding entered.<br>Focus the text field and press a key combination first." );
		
		// syntax for InfoView
		set( SYNTAX_DEFINE,             "syntax element definition"                        );
		set( SYNTAX_COMMENT,            "comment"                                          );
		set( SYNTAX_CONST,              "constant definition"                              );
		set( SYNTAX_VAR,                "variable definition"                              );
		set( SYNTAX_VAR_SYMBOL,         "first character of a variable or constant"        );
		set( SYNTAX_VAR_ASSIGNER,       "assign symbol between variable/constant and value" );
		set( SYNTAX_PARAM_NAMED_OPEN,   "opens a parameter name"                           );
		set( SYNTAX_PARAM_NAMED_CLOSE,  "closes a parameter name"                          );
		set( SYNTAX_PARAM_INDEX_OPEN,   "opens a parameter index"                          );
		set( SYNTAX_PARAM_INDEX_CLOSE,  "opens a parameter index"                          );
		set( SYNTAX_GLOBAL,             "global command (all channels)"                    );
		set( SYNTAX_P,                  "percussion channel"                               );
		set( SYNTAX_END,                "end of a definition block"                        );
		set( SYNTAX_BLOCK_OPEN,         "opens a nestable block"                           );
		set( SYNTAX_BLOCK_CLOSE,        "closes a nestable block"                          );
		set( SYNTAX_FUNCTION,           "function definition"                              );
		set( SYNTAX_PATTERN,            "pattern definition"                               );
		set( SYNTAX_PATTERN_INDEX_SEP,  "index separator inside of a pattern definition"   );
		set( SYNTAX_PARAM_OPEN,         "opens a parameter list (in a function or pattern call)"  );
		set( SYNTAX_PARAM_CLOSE,        "closes a parameter list (in a function or pattern call)" );
		set( SYNTAX_PARAM_SEPARATOR,    "separates parameters in a function or pattern call"      );
		set( SYNTAX_PARAM_ASSIGNER,     "assignes named parameters in a function or pattern call" );
		set( SYNTAX_CALL,               "function execution"                               );
		set( SYNTAX_INSTRUMENT,         "instrument switch for one single channel"         );
		set( SYNTAX_INSTRUMENTS,        "definition of instruments"                        );
		set( SYNTAX_META,               "meta information block definition"                );
		set( SYNTAX_META_COPYRIGHT,     "copyright information"                            );
		set( SYNTAX_META_TITLE,         "song title"                                       );
		set( SYNTAX_META_COMPOSER,      "coposer"                                          );
		set( SYNTAX_META_LYRICIST,      "lyricist"                                         );
		set( SYNTAX_META_ARTIST,        "artist"                                           );
		set( SYNTAX_META_SOFT_KARAOKE,  "opens a SOFT KARAOKE block"                       );
		set( SYNTAX_META_SK_VERSION,    "(soft karaoke) version"                           );
		set( SYNTAX_META_SK_LANG,       "(soft karaoke) language of the lyrics"            );
		set( SYNTAX_META_SK_TITLE,      "(soft karaoke) song title"                        );
		set( SYNTAX_META_SK_AUTHOR,     "(soft karaoke) author"                            );
		set( SYNTAX_META_SK_COPYRIGHT,  "(soft karaoke) copyright information"             );
		set( SYNTAX_META_SK_INFO,       "(soft karaoke) further information"               );
		set( SYNTAX_TEMPO,              "tempo definition in quarter notes per minute"     );
		set( SYNTAX_TIME_SIG,           "time signature definition"                        );
		set( SYNTAX_TIME_SIG_SLASH,     "fraction bar in the time signature definition"    );
		set( SYNTAX_KEY_SIG,            "key signature definition"                         );
		set( SYNTAX_KEY_SEPARATOR,      "key/tonality separator in the key signature definition" );
		set( SYNTAX_KEY_MAJ,            "major (tonality) in the key signature definition" );
		set( SYNTAX_KEY_MIN,            "minor (tonality) in the key signature definition" );
		set( SYNTAX_PARTIAL_SYNC_RANGE, "Channel range operator in a partial sync command" );
		set( SYNTAX_PARTIAL_SYNC_SEP,   "Channel separator in a partial sync command"      );
		set( SYNTAX_OPT_SEPARATOR,      "option separating character"                      );
		set( SYNTAX_OPT_ASSIGNER,       "option assignment character"                      );
		set( SYNTAX_PROG_BANK_SEP,      "Separator between program number and bank select" );
		set( SYNTAX_BANK_SEP,           "MSB / LSB separator for bank select"              );
		set( SYNTAX_VELOCITY,           "velocity option (long)"                           );
		set( SYNTAX_V,                  "velocity option (short)"                          );
		set( SYNTAX_DURATION,           "duration option (long)"                           );
		set( SYNTAX_D,                  "duration option (short)"                          );
		set( SYNTAX_DURATION_PERCENT,   "percent indicator of the duration option"         );
		set( SYNTAX_MULTIPLE,           "multiple notes option (long)"                     );
		set( SYNTAX_M,                  "multiple notes option (short)"                    );
		set( SYNTAX_QUANTITY,           "quantity: how often to play the note"             );
		set( SYNTAX_Q,                  "quantity option (short)"                          );
		set( SYNTAX_LYRICS,             "lyrics option (long)"                             );
		set( SYNTAX_L,                  "lyrics option (short)"                            );
		set( SYNTAX_LYRICS_SPACE,       "placeholder for a space inside of a syllable"     );
		set( SYNTAX_LYRICS_CR,          "placeholder for a new line inside of a syllable"  );
		set( SYNTAX_LYRICS_LF,          "placeholder for a new paragraph inside of a syllable" );
		set( SYNTAX_LYRICS_COMMA,       "placeholder for a comma inside of a syllable"     );
		set( SYNTAX_TUPLET,             "tuplet option (long)"                             );
		set( SYNTAX_T,                  "tuplet option (short)"                            );
		set( SYNTAX_TREMOLO,            "tremolo option (long)"                            );
		set( SYNTAX_TR,                 "tremolo option (short)"                           );
		set( SYNTAX_SHIFT,              "shift option (long)"                              );
		set( SYNTAX_S,                  "shift option (short)"                             );
		set( SYNTAX_IF,                 "IF option"                                        );
		set( SYNTAX_ELSIF,              "ELSIF option"                                     );
		set( SYNTAX_ELSE,               "ELSE option"                                      );
		set( SYNTAX_COND_EQ,            "Condition: equal"                                 );
		set( SYNTAX_COND_NEQ,           "Condition: not equal"                             );
		set( SYNTAX_COND_NDEF,          "Condition: not defined"                           );
		set( SYNTAX_COND_LT,            "Condition: lower than..."                         );
		set( SYNTAX_COND_LE,            "Condition: lower or equal than..."                );
		set( SYNTAX_COND_GT,            "Condition: greater than..."                       );
		set( SYNTAX_COND_GE,            "Condition: greater or equal than..."              );
		set( SYNTAX_COND_IN,            "Condition: in, e.g. $x in 0;1;5"                  );
		set( SYNTAX_COND_IN_SEP,        "Separator for in-condition"                       );
		set( SYNTAX_REST,               "rest character"                                   );
		set( SYNTAX_CHORD,              "chord definition"                                 );
		set( SYNTAX_CHORD_ASSIGNER,     "assign symbol between chord name and notes"       );
		set( SYNTAX_CHORD_SEPARATOR,    "separator for chord notes"                        );
		set( SYNTAX_INCLUDE,            "including another file"                           );
		set( SYNTAX_SOUNDBANK,          "including a soundbank file or URL (SF2 or DLS)"   );
		set( SYNTAX_SOUNDFONT,          "Deprecated. Will be removed in a future Version"  );
		
		set( SYNTAX_ZEROLENGTH,       "Zero-Length"                                      );
		set( SYNTAX_32,               "32nd"                                             );
		set( SYNTAX_16,               "16th"                                             );
		set( SYNTAX_8,                "8th"                                              );
		set( SYNTAX_4,                "quarter"                                          );
		set( SYNTAX_2,                "half"                                             );
		set( SYNTAX_1,                "full"                                             );
		set( SYNTAX_M1,               "full"                                             );
		set( SYNTAX_M2,               "2 full notes"                                     );
		set( SYNTAX_M4,               "4 full notes"                                     );
		set( SYNTAX_M8,               "8 full notes"                                     );
		set( SYNTAX_M16,              "16 full notes"                                    );
		set( SYNTAX_M32,              "32 full notes"                                    );
		set( SYNTAX_DOT,              "dot (note length multiplied by 1.5)"              );
		set( SYNTAX_TRIPLET,          "triplet (note length devided by 1.5)"             );
		set( SYNTAX_TUPLET_INTRO,     "tuplet opener"                                    );
		set( SYNTAX_TUPLET_FOR,       "tuplet definition separator"                      );
		set( SYNTAX_LENGTH_PLUS,      "note length addition symbol"                      );
		
		// messages for InfoView
		set( MSG1_CH_VOICE,               "Channel Voice Messages"                         );
		set( MSG1_CH_MODE,                "Channel Mode Messages"                          );
		set( MSG1_SYSTEM_COMMON,          "System Common Messages"                         );
		set( MSG1_SYSTEM_REALTIME,        "System Realtime Messages"                       );
		set( MSG1_META,                   "Meta Messages"                                  );
		set( MSG2_CV_NOTE_OFF,            "Note Off"                                       );
		set( MSG2_CV_NOTE_ON,             "Note On"                                        );
		set( MSG2_CV_POLY_PRESSURE,       "Polyphonic Key Pressure (Aftertouch)"           );
		set( MSG2_CV_CONTROL_CHANGE,      "Control Change"                                 );
		set( MSG2_CV_PROGRAM_CHANGE,      "Program Change"                                 );
		set( MSG2_CV_CHANNEL_PRESSURE,    "Channel Pressure (Aftertouch)"                  );
		set( MSG2_CV_PITCH_BEND,          "Pitch Bend"                                     );
		set( MSG2_CM_ALL_SOUND_OFF,       "All Sound Off"                                  );
		set( MSG2_CM_ALL_CTRLS_OFF,       "All Controllers Off"                            );
		set( MSG2_CM_LOCAL_CTRL,          "Local Control"                                  );
		set( MSG2_CM_ALL_NOTES_OFF,       "All Notes Off"                                  );
		set( MSG2_CM_OMNI_MODE_OFF,       "Omni Mode Off"                                  );
		set( MSG2_CM_OMNI_MODE_ON,        "Omni Mode On"                                   );
		set( MSG2_CM_MONO_NOTES_OFF,      "Mono Operation & All Notes Off"                 );
		set( MSG2_CM_POLY_NOTES_OFF,      "Poly Operation & All Notes Off"                 );
		set( MSG2_SC_SYSEX,               "SysEx (System Exclusive)"                       );
		set( MSG2_SC_MIDI_TIME_CODE,      "MIDI Time Code (Quarter Frame)"                 );
		set( MSG2_SC_SONG_POS_POINTER,    "Song Position Pointer"                          );
		set( MSG2_SC_SONG_SELECT,         "Song Select"                                    );
		set( MSG2_SC_TUNE_REQUEST,        "Tune Request"                                   );
		set( MSG2_SC_END_OF_SYSEX,        "End of SysEx"                                   );
		set( MSG2_SR_TIMING_CLOCK,        "Timing Clock"                                   );
		set( MSG2_SR_START,               "Start"                                          );
		set( MSG2_SR_CONTINUE,            "Continue"                                       );
		set( MSG2_SR_STOP,                "Stop"                                           );
		set( MSG2_SR_ACTIVE_SENSING,      "Active Sensing"                                 );
		set( MSG2_SR_SYSTEM_RESET,        "System Reset"                                   );
		set( MSG2_M_SEQUENCE_NUMBER,      "Sequence Number"                                );
		set( MSG2_M_TEXT,                 "Text"                                           );
		set( MSG2_M_COPYRIGHT,            "Copyright"                                      );
		set( MSG2_M_TRACK_NAME,           "Track Name"                                     );
		set( MSG2_M_INSTRUMENT_NAME,      "Instrument Name"                                );
		set( MSG2_M_LYRICS,               "Lyrics"                                         );
		set( MSG2_M_MARKER,               "Marker"                                         );
		set( MSG2_M_CUE_POINT,            "Cue Point"                                      );
		set( MSG2_M_CHANNEL_PREFIX,       "Channel Prefix"                                 );
		set( MSG2_M_MIDI_PORT,            "MIDI Port"                                      );
		set( MSG2_M_END_OF_SEQUENCE,      "End of Sequence"                                );
		set( MSG2_M_SET_TEMPO,            "Set Tempo"                                      );
		set( MSG2_M_SMPTE_OFFSET,         "SMPTE Offset"                                   );
		set( MSG2_M_TIME_SIGNATURE,       "Time Signature"                                 );
		set( MSG2_M_KEY_SIGNATURE,        "Key Signature"                                  );
		set( MSG2_M_SEQUENCER_SPEC,       "Sequencer Specific"                             );
		set( MSG3_C_BANK_SELECT,          "Bank Select"                                    );
		set( MSG3_C_MODULATION_WHEEL,     "Modulation Wheel"                               );
		set( MSG3_C_BREATH_CTRL,          "Breath Controller"                              );
		set( MSG3_C_FOOT_CTRL,            "Foot Controller"                                );
		set( MSG3_C_PORTAMENTO_TIME,      "Portamento Time"                                );
		set( MSG3_C_DATA_ENTRY,           "Data Entry"                                     );
		set( MSG3_C_CHANNEL_VOL,          "Channel Volume"                                 );
		set( MSG3_C_BALANCE,              "Balance"                                        );
		set( MSG3_C_PAN,                  "Pan"                                            );
		set( MSG3_C_EXPRESSION,           "Expression"                                     );
		set( MSG3_C_EFFECT_CTRL_1,        "Effect Controller 1"                            );
		set( MSG3_C_EFFECT_CTRL_2,        "Effect Controller 2"                            );
		set( MSG3_C_GEN_PURP_CTRL_1,      "General Purpose Controller 1"                   );
		set( MSG3_C_GEN_PURP_CTRL_2,      "General Purpose Controller 2"                   );
		set( MSG3_C_GEN_PURP_CTRL_3,      "General Purpose Controller 3"                   );
		set( MSG3_C_GEN_PURP_CTRL_4,      "General Purpose Controller 4"                   );
		set( MSG3_C_HOLD_PEDAL_1,         "Hold Pedal 1"                                   );
		set( MSG3_C_PORTAMENTO_PEDAL,     "Portamento Pedal"                               );
		set( MSG3_C_SOSTENUTO_PEDAL,      "Sostenuto Pedal"                                );
		set( MSG3_C_SOFT_PEDAL,           "Soft Pedal"                                     );
		set( MSG3_C_LEGATO_PEDAL,         "Legato Pedal"                                   );
		set( MSG3_C_HOLD_PEDAL_2,         "Hold Pedal 2"                                   );
		set( MSG3_C_SOUND_CTRL_1,         "Sound Controller 1 (Sound Variation)"           );
		set( MSG3_C_SOUND_CTRL_2,         "Sound Controller 2 (Timbre/Harmonic/Filter)"    );
		set( MSG3_C_SOUND_CTRL_3,         "Sound Controller 3 (Release Time)"              );
		set( MSG3_C_SOUND_CTRL_4,         "Sound Controller 4 (Attack Time)"               );
		set( MSG3_C_SOUND_CTRL_5,         "Sound Controller 5 (Brightness)"                );
		set( MSG3_C_SOUND_CTRL_6,         "Sound Controller 6 (Decay Time)"                );
		set( MSG3_C_SOUND_CTRL_7,         "Sound Controller 7 (Vibrato Rate)"              );
		set( MSG3_C_SOUND_CTRL_8,         "Sound Controller 8 (Vibrato Depth)"             );
		set( MSG3_C_SOUND_CTRL_9,         "Sound Controller 9 (Vibrato Delay)"             );
		set( MSG3_C_SOUND_CTRL_10,        "Sound Controller 10"                            );
		set( MSG3_C_GEN_PURP_CTRL_5,      "General Purpose Controller 5"                   );
		set( MSG3_C_GEN_PURP_CTRL_6,      "General Purpose Controller 6"                   );
		set( MSG3_C_GEN_PURP_CTRL_7,      "General Purpose Controller 7"                   );
		set( MSG3_C_GEN_PURP_CTRL_8,      "General Purpose Controller 8"                   );
		set( MSG3_C_PORTAMENTO_CTRL,      "Portamento Control"                             );
		set( MSG3_C_HI_RES_VELO_PRFX,     "High Resolution Velocity Prefix"                );
		set( MSG3_C_EFFECT_1_DEPTH,       "Effect 1 Depth (Reverb Send Level)"             );
		set( MSG3_C_EFFECT_2_DEPTH,       "Effect 2 Depth (Tremolo Depth)"                 );
		set( MSG3_C_EFFECT_3_DEPTH,       "Effect 3 Depth (Chorus Send Level)"             );
		set( MSG3_C_EFFECT_4_DEPTH,       "Effect 4 Depth (Celeste Depth)"                 );
		set( MSG3_C_EFFECT_5_DEPTH,       "Effect 5 Depth (Phaser Level)"                  );
		set( MSG3_C_DATA_BUTTON_INCR,     "Data Button Increment"                          );
		set( MSG3_C_DATA_BUTTON_DECR,     "Data Button Decrement"                          );
		set( MSG3_C_NRPN,                 "NRPN (Non-Registered Parameter)"                );
		set( MSG3_C_RPN,                  "RPN (Registered Parameter)"                     );
		set( MSG3_SX_NON_RT_UNIVERSAL,    "Universal, Non Real Time"                       );
		set( MSG3_SX_RT_UNIVERSAL,        "Universal, Real Time"                           );
		set( MSG3_SX_VENDOR,              "Manufacturer Specific"                          );
		set( MSG3_SX_EDUCATIONAL,         "Educational"                                    );
		set( MSG4_C_BANK_SELECT_MSB,      "MSB (Bank Select)"                              );
		set( MSG4_C_MODULATION_WHEEL_MSB, "MSB (Modulation Wheel)"                         );
		set( MSG4_C_BREATH_CTRL_MSB,      "MSB (Breath Controller)"                        );
		set( MSG4_C_FOOT_CTRL_MSB,        "MSB (Foot Controller)"                          );
		set( MSG4_C_PORTAMENTO_TIME_MSB,  "MSB (Portamento Time)"                          );
		set( MSG4_C_DATA_ENTRY_MSB,       "MSB (Data Entry)"                               );
		set( MSG4_C_CHANNEL_VOL_MSB,      "MSB (Channel Volume)"                           );
		set( MSG4_C_BALANCE_MSB,          "MSB (Balance)"                                  );
		set( MSG4_C_PAN_MSB,              "MSB (Pan)"                                      );
		set( MSG4_C_EXPRESSION_MSB,       "MSB (Expression)"                               );
		set( MSG4_C_EFFECT_CTRL_1_MSB,    "MSB (Effect Controller 1)"                      );
		set( MSG4_C_EFFECT_CTRL_2_MSB,    "MSB (Effect Controller 2)"                      );
		set( MSG4_C_GEN_PURP_CTRL_1_MSB,  "MSB (General Purpose Controller 1)"             );
		set( MSG4_C_GEN_PURP_CTRL_2_MSB,  "MSB (General Purpose Controller 2)"             );
		set( MSG4_C_GEN_PURP_CTRL_3_MSB,  "MSB (General Purpose Controller 3)"             );
		set( MSG4_C_GEN_PURP_CTRL_4_MSB,  "MSB (General Purpose Controller 4)"             );
		set( MSG4_C_BANK_SELECT_LSB,      "LSB (Bank Select)"                              );
		set( MSG4_C_MODULATION_WHEEL_LSB, "LSB (Modulation Wheel)"                         );
		set( MSG4_C_BREATH_CTRL_LSB,      "LSB (Breath Controller)"                        );
		set( MSG4_C_FOOT_CTRL_LSB,        "LSB (Foot Controller)"                          );
		set( MSG4_C_PORTAMENTO_TIME_LSB,  "LSB (Portamento Time)"                          );
		set( MSG4_C_DATA_ENTRY_LSB,       "LSB (Data Entry)"                               );
		set( MSG4_C_CHANNEL_VOL_LSB,      "LSB (Channel Volume)"                           );
		set( MSG4_C_BALANCE_LSB,          "LSB (Balance)"                                  );
		set( MSG4_C_PAN_LSB,              "LSB (Pan)"                                      );
		set( MSG4_C_EXPRESSION_LSB,       "LSB (Expression)"                               );
		set( MSG4_C_EFFECT_CTRL_1_LSB,    "LSB (Effect Controller 1)"                      );
		set( MSG4_C_EFFECT_CTRL_2_LSB,    "LSB (Effect Controller 2)"                      );
		set( MSG4_C_GEN_PURP_CTRL_1_LSB,  "LSB (General Purpose Controller 1)"             );
		set( MSG4_C_GEN_PURP_CTRL_2_LSB,  "LSB (General Purpose Controller 2)"             );
		set( MSG4_C_GEN_PURP_CTRL_3_LSB,  "LSB (General Purpose Controller 3)"             );
		set( MSG4_C_GEN_PURP_CTRL_4_LSB,  "LSB (General Purpose Controller 4)"             );
		set( MSG4_RPN_PITCH_BEND_SENS,    "Pitch Bend Sensitivity"                         );
		set( MSG4_RPN_MASTER_FINE_TUN,    "Master Fine Tuning (in Cents)"                  );
		set( MSG4_RPN_MASTER_COARSE_TUN,  "Master Coarse Tuning (in Half Steps)"           );
		set( MSG4_RPN_TUN_PROG_CHANGE,    "Tuning Program Change"                          );
		set( MSG4_RPN_TUN_BANK_SELECT,    "Tuning Bank Select"                             );
		set( MSG4_RPN_MOD_DEPTH_RANGE,    "Modulation Depth Range"                         );
		set( MSG4_RPN_MPE_CONFIG,         "MPE Config Msg (MCM)"                           );
		set( MSG4_RPN_AZIMUTH_ANGLE,      "Azimuth Angle"                                  );
		set( MSG4_RPN_ELEVATION_ANGLE,    "Elevation Angle"                                );
		set( MSG4_RPN_GAIN,               "Gain"                                           );
		set( MSG4_RPN_DISTANCE_RATIO,     "Distance Ratio"                                 );
		set( MSG4_RPN_MAXIMUM_DISTANCE,   "Maximum Distance"                               );
		set( MSG4_RPN_GAIN_AT_MAX_DIST,   "Gain at Maximum Distance"                       );
		set( MSG4_RPN_REF_DISTANCE_RATIO, "Reference Distance Ratio"                       );
		set( MSG4_RPN_PAN_SPREAD_ANGLE,   "Pan Spread Angle"                               );
		set( MSG4_RPN_ROLL_ANGLE,         "Roll Angle"                                     );
		set( MSG5_C_NRPN_LSB,             "LSB (NRPN)"                                     );
		set( MSG5_C_NRPN_MSB,             "MSB (NRPN)"                                     );
		set( MSG5_C_RPN_LSB,              "LSB (RPN)"                                      );
		set( MSG5_C_RPN_MSB,              "MSB (RPN)"                                      );
		set( MSG4_SX_NU_SMPL_DUMP_HDR,    "Sample Dump Header"                             );
		set( MSG4_SX_NU_SMPL_DATA_PKT,    "Sample Data Packet"                             );
		set( MSG4_SX_NU_SMPL_DUMP_REQ,    "Sample Dump Request"                            );
		set( MSG4_SX_NU_MIDI_TIME_CODE,   "MIDI Time Code"                                 );
		set( MSG4_SX_NU_SAMPLE_DUMP_EXT,  "Sample Dump Extensions"                         );
		set( MSG4_SX_NU_GENERAL_INFO,     "General Information"                            );
		set( MSG4_SX_NU_FILE_DUMP,        "File Dump"                                      );
		set( MSG4_SX_NU_TUNING_STANDARD,  "MIDI Tuning Standard (Non-Real Time)"           );
		set( MSG4_SX_NU_GENERA_MIDI,      "General MIDI"                                   );
		set( MSG4_SX_NU_DOWNLOADABLE_SND, "Downloadable Sounds"                            );
		set( MSG4_SX_NU_FILE_REF_MSG,     "File Reference Message"                         );
		set( MSG4_SX_NU_MIDI_VISUAL_CTRL, "MIDI Visual Control"                            );
		set( MSG4_SX_NU_END_OF_FILE,      "End of File"                                    );
		set( MSG4_SX_NU_WAIT,             "Wait"                                           );
		set( MSG4_SX_NU_CANCEL,           "Cancel"                                         );
		set( MSG4_SX_NU_NAK,              "NAK"                                            );
		set( MSG4_SX_NU_ACK,              "ACK"                                            );
		set( MSG4_SX_NU_MIDI_CI,          "MIDI 2.0 Capability Inquiry"                    );
		set( MSG4_SX_RU_MIDI_TIME_CODE,   "MIDI Time Code"                                 );
		set( MSG4_SX_RU_MIDI_SHOW_CTRL,   "MIDI Show Control"                              );
		set( MSG4_SX_RU_NOTATION_INFO,    "Notation Information"                           );
		set( MSG4_SX_RU_DEVICE_CTRL,      "Device Control"                                 );
		set( MSG4_SX_RU_RT_MTC_CUEING,    "Real Time MTC Cueing"                           );
		set( MSG4_SX_RU_MACH_CTRL_CMD,    "MIDI Machine Control Commands"                  );
		set( MSG4_SX_RU_MACH_CTRL_RES,    "MIDI Machine Control Responses"                 );
		set( MSG4_SX_RU_TUNING_STANDARD,  "MIDI Tuning Standard (Real Time)"               );
		set( MSG4_SX_RU_CTRL_DEST_SET,    "Controller Destination Setting"                 );
		set( MSG4_SX_RU_KEY_B_INSTR_CTRL, "Key-based Instrument Control"                   );
		set( MSG4_SX_RU_SCAL_POLY_MIP,    "Scalable Polyphony MIDI MIP Message"            );
		set( MSG4_SX_RU_MOB_PHONE_CTRL,   "Mobile Phone Control Message"                   );
		set( MSG5_SXN4_SPECIAL,           "Special"                                        );
		set( MSG5_SXN4_PUNCH_IN_PTS,      "Punch In Points"                                );
		set( MSG5_SXN4_PUNCH_OUT_PTS,     "Punch Out Points"                               );
		set( MSG5_SXN4_DEL_PUNCH_IN_PTS,  "Delete Punch In Point"                          );
		set( MSG5_SXN4_DEL_PUNCH_OUT_PTS, "Delete Punch Out Point"                         );
		set( MSG5_SXN4_EVT_START_PT,      "Event Start Point"                              );
		set( MSG5_SXN4_EVT_STOP_PT,       "Event Stop Point"                               );
		set( MSG5_SXN4_EVT_START_PTS_ADD, "Event Start Points with additional info"        );
		set( MSG5_SXN4_EVT_STOP_PTS_ADD,  "Event Stop Points with additional info"         );
		set( MSG5_SXN4_DEL_EVT_START_PT,  "Delete Event Start Point"                       );
		set( MSG5_SXN4_DEL_EVT_STOP_PT,   "Delete Event Stop Point"                        );
		set( MSG5_SXN4_CUE_PTS,           "Cue Points"                                     );
		set( MSG5_SXN4_CUE_PTS_ADD,       "Cue Points with additional info"                );
		set( MSG5_SXN4_DEL_CUE_PT,        "Delete Cue Point"                               );
		set( MSG5_SXN4_EVT_NAME_IN_ADD,   "Event Name in additional info"                  );
		set( MSG5_SXN5_LOOP_PTS_TRANSM,   "Loop Points Transmission"                       );
		set( MSG5_SXN5_LOOP_PTS_REQ,      "Loop Points Request"                            );
		set( MSG5_SXN5_SMPL_NAME_TRANSM,  "Sample Name Transmission"                       );
		set( MSG5_SXN5_SMPL_NAME_REQ,     "Sample Name Request"                            );
		set( MSG5_SXN5_EXT_DUMP_HDR,      "Extended Dump Header"                           );
		set( MSG5_SXN5_EXT_LOOP_PTS_TR,   "Extended Loop Points Transmission"              );
		set( MSG5_SXN5_EXT_LOOP_PTS_REQ,  "Extended Loop Points Request"                   );
		set( MSG5_SXN6_IDENTITY_REQ,      "Identity Request"                               );
		set( MSG5_SXN6_IDENTITY_REPL,     "Identity Reply"                                 );
		set( MSG5_SXN7_HEADER,            "Header"                                         );
		set( MSG5_SXN7_DATA_PACKET,       "Data Packet"                                    );
		set( MSG5_SXN7_REQUEST,           "Request"                                        );
		set( MSG5_SXN8_BLK_DUMP_REQ,      "Bulk Dump Request"                              );
		set( MSG5_SXN8_BLK_DUMP_REPL,     "Bulk Dump Reply"                                );
		set( MSG5_SXN8_TUNING_DUMP_REQ,   "Tuning Dump Request"                            );
		set( MSG5_SXN8_KEY_B_TUNING_DMP,  "Key-Based Tuning Dump"                          );
		set( MSG5_SXN8_SO_TUN_DMP_1,      "Scale/Octave Tuning Dump, 1 byte format"        );
		set( MSG5_SXN8_SO_TUN_DMP_2,      "Scale/Octave Tuning Dump, 2 byte format"        );
		set( MSG5_SXN8_SG_TUN_CH_BNK_SEL, "Single Note Tuning Change with Bank Select"     );
		set( MSG5_SXN8_SO_TUN_1,          "Scale/Octave Tuning, 1 byte format"             );
		set( MSG5_SXN8_SO_TUN_2,          "Scale/Octave Tuning, 2 byte format"             );
		set( MSG5_SXN9_GM_DISABLE,        "GM Disable"                                     );
		set( MSG5_SXN9_GM_1_ON,           "General MIDI 1 System On"                       );
		set( MSG5_SXN9_GM_OFF,            "General MIDI System Off"                        );
		set( MSG5_SXN9_GM_2_ON,           "General MIDI 2 System On"                       );
		set( MSG5_SXNA_DLS_ON,            "Turn DLS On"                                    );
		set( MSG5_SXNA_DLS_OFF,           "Turn DLS Off"                                   );
		set( MSG5_SXNA_DLS_VA_OFF,        "Turn DLS Voice Allocation Off"                  );
		set( MSG5_SXNA_DLS_VA_ON,         "Turn DLS Voice Allocation On"                   );
		set( MSG5_SXNB_OPEN_FILE,         "Open File"                                      );
		set( MSG5_SXNB_SEL_RESEL_CONT,    "Select or Reselect Contents"                    );
		set( MSG5_SXNB_OPEN_SEL_CONT,     "Open File and Select Contents"                  );
		set( MSG5_SXNB_CLOSE_FILE,        "Close File"                                     );
		set( MSG5_SXNC_MVC_CMD,           "MVC Command"                                    );
		set( MSG5_SXND_CI_RESERVED,       "Reserved"                                       );
		set( MSG5_SXND_CI_PROTO_NEGO,     "Protocol Negotiation"                           );
		set( MSG5_SXND_CI_PROF_CONF,      "Profile Configuration"                          );
		set( MSG5_SXND_CI_PROP_EXCH,      "Property Exchange"                              );
		set( MSG5_SXND_CI_MGMT_MSG,       "Management"                                     );
		set( MSG5_SXR1_FULL_MSG,          "Full Message (Full Frame)"                      );
		set( MSG5_SXR1_USER_BITS,         "User Bits"                                      );
		set( MSG5_SXR2_MSC_EXT,           "MSC Extensions"                                 );
		set( MSG5_SXR2_MSC_CMD,           "MSC Command"                                    );
		set( MSG5_SXR3_BAR_NUMBER,        "Bar Number"                                     );
		set( MSG5_SXR3_TIME_SIG_IMMED,    "Time Signature (Immediate)"                     );
		set( MSG5_SXR3_TIME_SIG_DELAYED,  "Time Signature (Delayed)"                       );
		set( MSG5_SXR4_MASTER_VOLUME,     "Master Volume"                                  );
		set( MSG5_SXR4_MASTER_BALANCE,    "Master Balance"                                 );
		set( MSG5_SXR4_MASTER_FINE_TUN,   "Master Fine Tuning"                             );
		set( MSG5_SXR4_MASTER_COARSE_TUN, "Master Course Tuning"                           );
		set( MSG5_SXR4_GLOBAL_PARAM_CTRL, "Global Parameter Control"                       );
		set( MSG5_SXR5_SPECIAL,           "Special"                                        );
		set( MSG5_SXR5_PUNCH_IN_PTS,      "Punch In Points"                                );
		set( MSG5_SXR5_PUNCH_OUT_PTS,     "Punch Out Points"                               );
		set( MSG5_SXR5_EVT_START_PT,      "Event Start points"                             );
		set( MSG5_SXR5_EVT_STOP_PT,       "Event Stop points"                              );
		set( MSG5_SXR5_EVT_START_PTS_ADD, "Event Start points with additional info"        );
		set( MSG5_SXR5_EVT_STOP_PTS_ADD,  "Event Stop points with additional info"         );
		set( MSG5_SXR5_CUE_PTS,           "Cue points"                                     );
		set( MSG5_SXR5_CUE_PTS_ADD,       "Cue points with additional info"                );
		set( MSG5_SXR5_EVT_NAME_IN_ADD,   "Event Name in additional info"                  );
		set( MSG5_SXR6_STOP,              "Stop"                                           );
		set( MSG5_SXR6_PLAY,              "Play"                                           );
		set( MSG5_SXR6_DEF_PLAY,          "Deferred Play (play after no longer busy)"      );
		set( MSG5_SXR6_FAST_FW,           "Fast Forward"                                   );
		set( MSG5_SXR6_REWIND,            "Rewind"                                         );
		set( MSG5_SXR6_REC_STROBE,        "Record Strobe (Punch In)"                       );
		set( MSG5_SXR6_REC_EXIT,          "Record Exit (Punch out)"                        );
		set( MSG5_SXR6_REC_PAUSE,         "Record Pause"                                   );
		set( MSG5_SXR6_PAUSE,             "Pause (pause playback)"                         );
		set( MSG5_SXR6_EJECT,             "Eject (disengage media container from MMC device)" );
		set( MSG5_SXR6_CHASE,             "Chase"                                          );
		set( MSG5_SXR6_CMD_ERR_RESET,     "Command Error Reset"                            );
		set( MSG5_SXR6_MMC_RESET,         "MMC Reset (to default/startup state)"           );
		set( MSG5_SXR6_WRITE,             "Write (Record Ready/Arm Tracks)"                );
		set( MSG5_SXR6_GOTO,              "Goto (Locate)"                                  );
		set( MSG5_SXR6_SHUTTLE,           "Shuttle"                                        );
		set( MSG5_SXR7_MMC_RES,           "MMC Response"                                   );
		set( MSG5_SXR8_SG_TUN_CH,         "Single Note Tuning Change"                      );
		set( MSG5_SXR8_SG_TUN_CH_BNK_SEL, "Single Note Tuning Change with Bank Select"     );
		set( MSG5_SXR8_SO_TUN_1,          "Scale/Octave Tuning, 1 byte format"             );
		set( MSG5_SXR8_SO_TUN_2,          "Scale/Octave Tuning, 2 byte format"             );
		set( MSG5_SXR9_CHANNEL_PRESSURE,  "Channel Pressure (Aftertouch)"                  );
		set( MSG5_SXR9_POLY_KEY_PRESSURE, "Polyphonic Key Pressure (Aftertouch)"           );
		set( MSG5_SXR9_CTRL,              "Controller (Control Change)"                    );
		set( MSG6_SXND10_INIT_PROTO_NEGO,   "Initiate Protocol Negotiation"                );
		set( MSG6_SXND11_INIT_PROTO_REPL,   "Reply to Initiate Protocol Negotiation"       );
		set( MSG6_SXND12_SET_NEW_PROTO,     "Set New Select Protocol"                      );
		set( MSG6_SXND13_TEST_NEW_PROT_ITR, "Test New Protocol Initiator to Responder"     );
		set( MSG6_SXND14_TEST_NEW_PROT_RTI, "Test New Protocol Responder to Initiator"     );
		set( MSG6_SXND15_CONF_NEW_PROT_EST, "Confirmation Protocol Established"            );
		set( MSG6_SXND20_PROF_INQ,          "Profile Inquiry"                              );
		set( MSG6_SXND21_PROF_INQ_REPL,     "Reply to Profile Inquiry"                     );
		set( MSG6_SXND22_SET_PROF_ON,       "Set Profile On"                               );
		set( MSG6_SXND23_SET_PROF_OFF,      "Set Profile Off"                              );
		set( MSG6_SXND24_PROF_ENABL_RPRT,   "Profile Enabled Report"                       );
		set( MSG6_SXND25_PROF_DISABL_RPRT,  "Profile Disabled Report"                      );
		set( MSG6_SXND2F_PROF_SPEC_DATA,    "Profile Specific Data"                        );
		set( MSG6_SXND30_PROP_EXCH_CAP_INQ,  "Inquiry: Property Exchange Capabilities"     );
		set( MSG6_SXND31_PROP_EXCH_CAP_REPL, "Reply to Property Exchange Capabilities"     );
		set( MSG6_SXND32_HAS_PROP_DATA_INQ,  "Inquiry: Has Property Data (Reserved)"       );
		set( MSG6_SXND33_HAS_PROP_DATA_REPL, "Reply to Has Property Data (Reserved)"       );
		set( MSG6_SXND34_GET_PROP_DATA_INQ,  "Inquiry: Get Property Data"                  );
		set( MSG6_SXND35_GET_PROP_DATA_REPL, "Reply to Get Property Data"                  );
		set( MSG6_SXND36_SET_PROP_DATA_INQ,  "Inquiry: Set Property Data"                  );
		set( MSG6_SXND37_SET_PROP_DATA_REPL, "Reply to Set Property Data"                  );
		set( MSG6_SXND38_SUBSCRIPTION,       "Subscription"                                );
		set( MSG6_SXND39_SUBSCRIPTION_REPL,  "Reply to Subscription"                       );
		set( MSG6_SXND3F_NOTIFY,             "Notify"                                      );
		set( MSG6_SXND70_DISCOVERY,          "Discovery"                                   );
		set( MSG6_SXND71_DISCOVERY_RESP,     "Reply to Discovery"                          );
		set( MSG6_SXND7E_INVAL_MUID,         "Invalidate MUID"                             );
		set( MSG6_SXND7F_NAK,                "NAK"                                         );
		
		// MessageClassifier: message description
		set( MSG_DESC_MEANING,            "Meaning: "                                      );
		set( MSG_DESC_VALUE,              "Value: "                                        );
		set( MSG_DESC_ON,                 "ON"                                             );
		set( MSG_DESC_OFF,                "OFF"                                            );
		set( MSG_DESC_89A_NOTE,           "Note: "                                         );
		set( MSG_DESC_89A_VELOCITY,       "Velocity: "                                     );
		set( MSG_DESC_89AD_PRESSURE,      "Pressure: "                                     );
		set( MSG_DESC_B_POS_MEANINGS,     "Possible Meanings: "                            );
		set( MSG_DESC_B_FOR_PARAM,        "For Parameter: "                                );
		set( MSG_DESC_B_RPN_MSB_00,       "Any of the standard controllers"                );
		set( MSG_DESC_B_RPN_MSB_3D,       "Any of the three-dimensional sound controllers" );
		set( MSG_DESC_B_RPN_LSB_00,       "Standard: Pitch Bend Sensitivity / 3D: Azimuth Angle"                     );
		set( MSG_DESC_B_RPN_LSB_01,       "Standard: Channel Fine Tuning  / 3D: Elevation Angle"                     );
		set( MSG_DESC_B_RPN_LSB_02,       "Standard: Channel Coarse Tuning / 3D: Gain"                               );
		set( MSG_DESC_B_RPN_LSB_03,       "Standard: Tuning Program Change / 3D: Distance Ratio"                     );
		set( MSG_DESC_B_RPN_LSB_04,       "Standard: Tuning Bank Select / 3D: Maximum Distance"                      );
		set( MSG_DESC_B_RPN_LSB_05,       "Standard: Modulation Depth Range / 3D: Gain at maximum distance"          );
		set( MSG_DESC_B_RPN_LSB_06,       "Standard: MPE Configurarion Message (MCM) / 3D: Reference distance ratio" );
		set( MSG_DESC_B_RPN_LSB_07,       "3D: Pan spread angle"                                                     );
		set( MSG_DESC_B_RPN_LSB_08,       "3D: Roll angle"                                                           );
		set( MSG_DESC_B_RPN_NRPN_7F,      "Null Function Number (Reset)"                   );
		set( MSG_DESC_B_START_NOTE,       "Start Note: "                                   );
		set( MSG_DESC_C_PROGRAM,          "Program: "                                      );
		set( MSG_DESC_C_INSTRUMENT,       "Instrument: "                                   );
		set( MSG_DESC_C_DRUMKIT,          "Drumkit: "                                      );
		set( MSG_DESC_E_GENERAL_DESC,     "Possible values: -8192 to 8191"                 );
		set( MSG_DESC_E_CURRENT_SENS,     "Current pitch bend sensitivity (+/- half tones): " );
		set( MSG_DESC_E_HALF_TONE,        "Resulting half tones: "                         );
		set( MSG_DESC_F_TEMPO_MPQ,        "MPQ (milliseconds per quarter note)"            );
		set( MSG_DESC_F_TEMPO_BPM,        "BPM (beats/quarter notes per minute)"           );
		set( MSG_DESC_F_BPM,              "bpm"                                            );
		set( MSG_DESC_F_KEY_SIG_SHARPS,   "Sharps (♯)"                                     );
		set( MSG_DESC_F_KEY_SIG_FLATS,    "Flats (♭)"                                      );
		set( MSG_DESC_F_KEY_SIG_NONE,     "Sharps (♯) or Flats (♭)"                        );
		set( MSG_DESC_F_UNKNOWN_TONALITY, "Unknown tonality"                               );
		
		// UiControler + PlayerControler
		set( ERROR_IN_LINE,                       "<html>parsing error in file:<br>%s<br>line: %s<br>" );
		
		// SoundbankParser  // TODO: rename from SoundfontParser
		set( UNKNOWN_SOUND_EXT,        "<html>Allowed file extensions: *.sf2 or *.dls"
		                             + "<br>Invalid file extension in file:<br>"   );
		set( INVALID_URL,              "Invalid url: "                             );
		set( UNKNOWN_HOST,             "Unknown Host: "                            );
		set( SOUND_FORMAT_FAILED,      "<b>Tried %s format</b><br><b>Failed:</b> " );
		set( INVALID_RIFF,             "Invalid RIFF format"                       );
		set( CANNOT_OPEN_SOUND,        "Unable to load soundbank"                  );
		set( DOWNLOAD_PROBLEM,         "Unable to download from "                  );
		set( COULDNT_CREATE_CACHE_DIR, "Unable to create cache directory: "        );
		set( SOUND_FROM_FILE,          "[File] "                                   );
		set( SOUND_FROM_URL,           "[URL] "                                    );
		
		// SequenceParser
		set( ERROR_NOTE_TOO_BIG,                  "note number too big: "                                             );
		set( ERROR_NOTE_TOO_SMALL,                "note number too small: "                                           );
		set( ERROR_ANALYZE_POSTPROCESS,           "Error while postprocessing the sequence"                           );
		
		// MidiParser
		set( ERROR_ONLY_PPQ_SUPPORTED,            "Only MIDI files with division type PPQ are supported." );
		
		// MidicaPLParser
		set( ERROR_0_NOT_ALLOWED,                 "0 not allowed"                 );
		set( ERROR_NEGATIVE_NOT_ALLOWED,          "negative number not allowed: " );
		set( ERROR_NOT_AN_INTEGER,                "not an integer: "              );
		set( ERROR_NOT_A_FLOAT,                   "not a valid number: "          );
		set( ERROR_INSTRUMENTS_NOT_DEFINED,       "no instruments have been defined yet"                              );
		set( ERROR_GLOBALS_IN_INSTR_DEF,          "global commands are not allowed inside an instrument definition"   );
		set( ERROR_SINGLE_INSTR_IN_INSTR_DEF,     "instrument commands are not allowed inside an instrument definition block" );
		set( ERROR_UNKNOWN_CMD,                   "unknown command: "                                                 );
		set( ERROR_CMD_END_WITHOUT_BEGIN,         "there is no open block to be closed"                               );
		set( ERROR_CHANNEL_INVALID_OPT,           "invalid channel option: "                                          );
		set( ERROR_BLOCK_INVALID_OPT,             "invalid block option: "                                            );
		set( ERROR_BLOCK_UNMATCHED_CLOSE,         "there is no open block to be closed"                               );
		set( ERROR_BLOCK_UNMATCHED_OPEN,          "nestable block not closed"                                         );
		set( ERROR_NESTABLE_BLOCK_OPEN_AT_EOF,    "nestable block not closed until end of file"                       );
		set( ERROR_NAMED_BLOCK_OPEN_AT_EOF,       "named block not closed until end of file"                          );
		set( ERROR_ARGS_NOT_ALLOWED,              "arguments not allowed here"                                        );
		set( ERROR_CHORD_ALREADY_DEFINED,         "chord name has been already defined: "                             );
		set( ERROR_CHORD_EQUALS_NOTE,             "illegal chord name (equals a note name): "                         );
		set( ERROR_CHORD_EQUALS_PERCUSSION,       "illegal chord name (equals a percussion ID): "                     );
		set( ERROR_CHORD_CONTAINS_ALREADY,        "Note cannot be defined more than once in the same chord: "         );
		set( ERROR_CHORD_DEF_NOT_ALLOWED_HERE,    "a chord definition is not allowed inside a block<br>maybe you forgot to close the block." );
		set( ERROR_CHORD_NUM_OF_ARGS,             "wrong number of arguments in CHORD command"                        );
		set( ERROR_CHORD_REDUNDANT_SEP,           "redundant separator in chord definition"                           );
		set( ERROR_CONST_NUM_OF_ARGS,             "wrong number of arguments in CONSTANT definition"                  );
		set( ERROR_CONST_ALREADY_DEFINED,         "constant already defined: "                                        );
		set( ERROR_CONST_NAME_EQ_VALUE,           "constant name must be different from it's value: "                 );
		set( ERROR_CONST_RECURSION,               "recursion depth in interpolation of a constant too high"           );
		set( ERROR_VAR_NUM_OF_ARGS,               "wrong number of arguments in VAR definition or assignment"         );
		set( ERROR_VAR_ALREADY_DEF_AS_CONST,      "variable name already used for a constant: "                       );
		set( ERROR_VAR_VAL_HAS_WHITESPACE,        "Variables must not contain a whitespace in the value: "            );
		set( ERROR_VAR_NOT_DEFINED,               "variable undefined: "                                              );
		set( ERROR_VAR_NOT_ALLOWED,               "variable not allowed here: "                                       );
		set( ERROR_VAR_NAME_INVALID,              "invalid variable or parameter name: "                              );
		set( ERROR_VAR_NAME_EQ_VALUE,             "variable name must be different from it's value: "                 );
		set( ERROR_VAR_RECURSION,                 "recursion depth in interpolation of a variable or parameter too high" );
		set( ERROR_PARAM_OUTSIDE_FUNCTION,        "parameter cannot be used outside of a function: "                  );
		set( ERROR_PARAM_NAMED_UNKNOWN,           "unknown parameter name: "                                          );
		set( ERROR_PARAM_INDEX_TOO_HIGH,          "parameter index too high: "                                        );
		set( ERROR_PARAM_INDEX_UNDEFINED,         "parameter contains undefined index: "                              );
		set( ERROR_DEFINE_NUM_OF_ARGS,            "wrong number of arguments in DEFINE command"                       );
		set( ERROR_ALREADY_REDEFINED,             "Command ID already redefined. Cannot be redefined again: "         );
		set( ERROR_FILE_NUM_OF_ARGS,              "wrong number of arguments in INCLUDE command"                      );
		set( ERROR_SOUNDBANK_NUM_OF_ARGS,         "wrong number of arguments in SOUNDBANK command"                    );
		set( ERROR_FILE_EXISTS,                   "file does not exist:<br>"                                          );
		set( ERROR_FILE_NORMAL,                   "not a normal file:<br>"                                            );
		set( ERROR_FILE_READABLE,                 "file not readable:<br>"                                            );
		set( ERROR_FILE_IO,                       "file cannot be parsed:<br>"                                        );
		set( ERROR_SOUNDBANK_IO,                  "soundbank cannot be parsed:<br>"                                   );
		set( ERROR_SOUNDBANK_ALREADY_PARSED,      "a soundbank can be included only once"                             );
		set( ERROR_FUNCTION_NUM_OF_ARGS,          "wrong number of arguments in function command"                     );
		set( ERROR_PATTERN_NOT_ALLOWED_HERE,      "a pattern definition is not allowed inside a block<br>maybe you forgot to close the block." );
		set( ERROR_PATTERN_ALREADY_DEFINED,       "pattern name has been already defined: "                           );
		set( ERROR_PATTERN_NUM_OF_ARGS,           "wrong number of arguments in pattern command"                      );
		set( ERROR_PATTERN_INVALID_OUTER_OPT,     "in a channel commands with a pattern this option is not allowed: " );
		set( ERROR_PATTERN_INVALID_INNER_OPT,     "invalid pattern option: "                                          );
		set( ERROR_PATTERN_INDEX_INVALID,         "pattern index not a number: "                                      );
		set( ERROR_PATTERN_INDEX_TOO_HIGH,        "pattern index too high: "                                          );
		set( ERROR_PATTERN_RECURSION_DEPTH,       "Recursion depth in pattern too big."                               );
		set( ERROR_PATTERN_UNDEFINED,             "Pattern not defined: "                                             );
		set( ERROR_META_NUM_OF_ARGS,              "no arguments allowed in meta command"                              );
		set( ERROR_META_UNKNOWN_CMD,              "unknown meta command: "                                            );
		set( ERROR_SOFT_KARAOKE_UNKNOWN_CMD,      "unknown soft karaoke command: "                                    );
		set( ERROR_SOFT_KARAOKE_NOT_ALLOWED_HERE, "a soft karaoke definition is not allowed outside of a META block." );
		set( ERROR_SOFT_KARAOKE_ALREADY_SET,      "only one SOFT KARAOKE block is allowed"                            );
		set( ERROR_SOFT_KARAOKE_NUM_OF_ARGS,      "no arguments allowed when opening a soft karaoke block"            );
		set( ERROR_SK_VALUE_ALREADY_SET,          "soft karaoke field is already set: "                               );
		set( ERROR_SK_FIELD_CRLF_NOT_ALLOWED,     "carriage return or line feed not allowed in soft karaoke commands" );
		set( ERROR_SK_SYLLABLE_CRLF_NOT_ALLOWED,  "carriage return or line feed not allowed in lyrics when using SOFT KARAOKE" );
		set( ERROR_FUNCTION_RECURSION,            "Can't call the current function in itself. Recursion not allowed." );
		set( ERROR_FUNCTION_RECURSION_DEPTH,      "Recursion depth in function call too big."                         );
		set( ERROR_FUNCTION_UNDEFINED,            "Call failed. Function not defined."                                );
		set( ERROR_FUNCTION_ALREADY_DEFINED,      "function name has been already defined: "                          );
		set( ERROR_FUNCTION_NOT_ALLOWED_HERE,     "a function definition is not allowed inside a block<br>maybe you forgot to close the block." );
		set( ERROR_META_NOT_ALLOWED_HERE,         "a meta definition is not allowed inside a block<br>maybe you forgot to close the block." );
		set( ERROR_CALL_NUM_OF_ARGS,              "wrong number of arguments in function call command"                );
		set( ERROR_CALL_UNKNOWN_OPT,              "unknown option for function call command: "                        );
		set( ERROR_CALL_SYNTAX,                   "invalid syntax in call command"                                    );
		set( ERROR_CALL_EMPTY_PARAM,              "empty parameter in parameter list: "                               );
		set( ERROR_CALL_PARAM_NAME_EMPTY,         "empty named parameter in parameter list: "                         );
		set( ERROR_CALL_PARAM_VALUE_EMPTY,        "value of named parameter is empty: "                               );
		set( ERROR_CALL_PARAM_NAME_WITH_SPEC,     "parameter name contains special characters: "                      );
		set( ERROR_CALL_DUPLICATE_PARAM_NAME,     "duplicate parameter name: "                                        );
		set( ERROR_CALL_PARAM_MORE_ASSIGNERS,     "named parameter must not contain more than one assign symbol: "    );
		set( ERROR_INVALID_TIME_DENOM,            "invalid denominator in time signature: "                           );
		set( ERROR_INVALID_TIME_SIG,              "invalid time signature argument: "                                 );
		set( ERROR_INVALID_KEY_SIG,               "invalid key signature argument: "                                  );
		set( ERROR_INVALID_TONALITY,              "invalid tonality: "                                                );
		set( ERROR_PARTIAL_RANGE,                 "invalid range definition: "                                        );
		set( ERROR_PARTIAL_RANGE_ORDER,           "invalid range definition: range must be ascending: "               );
		set( ERROR_PARTIAL_RANGE_EMPTY,           "empty range in channel definition"                                 );
		set( ERROR_MODE_INSTR_NUM_OF_ARGS,        "wrong number of arguments in mode command 'INSTRUMENTS'"           );
		set( ERROR_NOTE_LENGTH_INVALID,           "invalid note length expression or undefined pattern: "             );
		set( ERROR_ZEROLENGTH_NOT_ALLOWED,        "zero-length not allowed for notes or chords"                       );
		set( ERROR_ZEROLENGTH_IN_SUM,             "zero-length not allowed in length sum"                             );
		set( ERROR_ZEROLENGTH_INVALID_OPTION,     "channel option invalid for zero-length commands: "                 );
		set( ERROR_EMPTY_LENGTH_SUMMAND,          "empty summand in length string: "                                  );
		set( ERROR_UNKNOWN_FUNCTION_CMD,          "unknown function command: "                                        ); // TODO: check
		set( ERROR_INSTR_NUM_OF_ARGS,             "wrong number of arguments in instrument command"                   );
		set( ERROR_INSTR_NUM_OF_ARGS_SINGLE,      "wrong number of arguments in single-line instrument command"       );
		set( ERROR_INSTR_BANK,                    "Instrument and/or Bank definition erroneous"                       );
		set( ERROR_GLOBAL_NUM_OF_ARGS,            "wrong number of arguments in global command"                       );
		set( ERROR_UNKNOWN_GLOBAL_CMD,            "unknown global command: "                                          );
		set( ERROR_UNKNOWN_COMMAND_ID,            "Unknown command ID: "                                              );
		set( ERROR_MIDI_PROBLEM,                  "<html>Midi Problem!<br>"                                           );
		set( ERROR_CH_CMD_NUM_OF_ARGS,            "wrong number of arguments in channel command"                      );
		set( ERROR_CANT_PARSE_OPTIONS,            "cannot parse options: "                                            );
		set( ERROR_OPTION_NEEDS_VAL,              "option needs value: "                                              );
		set( ERROR_OPTION_VAL_NOT_ALLOWED,        "no value allowed for option: "                                     );
		set( ERROR_VEL_NOT_MORE_THAN_127,         "velocity cannot be set to more than 127"                           );
		set( ERROR_VEL_NOT_LESS_THAN_1,           "velocity must be more than 0"                                      );
		set( ERROR_TUPLET_INVALID,                "Invalid tuplet definition: "                                       );
		set( ERROR_DURATION_MORE_THAN_0,          "duration must be more than 0.0 (or 0%)"                            );
		set( ERROR_UNKNOWN_OPTION,                "unknown option: "                                                  );
		set( ERROR_UNKNOWN_NOTE,                  "unknown note: "                                                    );
		set( ERROR_UNKNOWN_PERCUSSION,            "unknown percussion ID: "                                           );
		set( ERROR_CHANNEL_UNDEFINED,             "channel %s has not been defined"                                   );
		set( ERROR_CHANNEL_REDEFINED,             "channel %s has been defined already"                               );
		set( ERROR_INVALID_CHANNEL_NUMBER,        "Invalid channel number (must be between 0 and 15): "               );
		set( ERROR_NOT_ALLOWED_IN_INSTR_BLK,      "Command not allowed inside of an instruments definition block: "   );
		set( ERROR_NOT_ALLOWED_IN_META_BLK,       "Command not allowed inside of a meta block: "                      );
		set( ERROR_NOT_ALLOWED_IN_BLK,            "Command not allowed inside of a block: "                           );
		set( ERROR_BLOCK_IF_MUST_BE_ALONE,        "block option 'if' cannot be combined with another if/elsif/else"   );
		set( ERROR_BLOCK_ELSIF_MUST_BE_ALONE,     "block option 'elsif' cannot be combined with another if/elsif/else" );
		set( ERROR_BLOCK_ELSE_MUST_BE_ALONE,      "block option 'else' cannot be combined with another if/elsif/else" );
		set( ERROR_BLOCK_NO_IF_FOUND,             "no 'if' block was found before this block"                         );
		set( ERROR_TOO_MANY_OPERATORS_IN_COND,    "Too many operators in condition: "                                 );
		set( ERROR_COND_DEFINED_HAS_WHITESPACE,   "Invalid whitespace in 'defined' condition: "                       );
		set( ERROR_COND_WHITESPACE_IN_FIRST_OP,   "Invalid whitespace in a condition's first operand: "               );
		set( ERROR_COND_WHITESPACE_IN_SEC_OP,     "Invalid whitespace in a condition's second operand: "              );
		set( ERROR_COND_UNDEF_EMPTY,              "Empty 'undefined' condition: "                                     );
		set( ERROR_COND_UNDEF_IN_CENTER,          "'!' must be at the beginning of the condition. : "                 );
		set( ERROR_COND_WHITESPACE_IN_IN_ELEM,    "Invalid whitespace in list element: "                              );
		set( ERROR_COND_EMPTY_ELEM_IN_IN_LIST,    "Empty element in 'in' list: "                                      );
		set( ERROR_CALL_IF_MUST_BE_ALONE,         "block option 'if' cannot be combined with another if"              );
		
		// NestableBlock
		set( ERROR_BLOCK_ARG_ALREADY_SET,         "Block argument has already been set before: "                      );
		
		// ParseException and StackTraceElement
		set( EXCEPTION_CAUSED_BY_LINE,            "Caused by this line:"                                              );
		set( EXCEPTION_CAUSED_BY_BLK_COND,        "Caused by erroneous if/elsif/else chain or conditions in nestable block" );
		set( EXCEPTION_CAUSED_BY_INVALID_VAR,     "Caused by invalid variable or parameter name: "                    );
		set( STACK_TRACE_HEADER,                  "Stack Trace:"                                                      );
		set( STACK_TRACE_INDENTATION,             "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"                  );
		set( STACK_TRACE_BLOCK,                   "Nestable block"                                                    );
		set( STACK_TRACE_OPTIONS,                 "options: "                                                         );
		set( STACK_TRACE_PARAMS,                  "params: "                                                          );
		set( STACK_TRACE_LINE,                    "line:"                                                             );
		set( STACK_TRACE_IN,                      "in"                                                                );
		set( STACK_TRACE_EXEC,                    "Executed:"                                                         );
		set( STACK_TRACE_FUNCTION,                "Function "                                                         );
		set( STACK_TRACE_PATTERN,                 "Pattern "                                                          );
		
		// AldaImporter
		set( ERROR_ALDA_NO_MIDI_FILE,             "<html>ALDA failed to create a MIDI file.<br>In most cases that means:<br>Too many Instruments." );
		
		// LilypondImporter
		set( ERROR_LILYPOND_NO_MIDI_FILE,         "<html>LilyPond didn't create any MIDI file.<br>Did you forget to include a \\midi block?" );
		set( ERROR_WRONG_DIVISION_TYPE,           "Wrong division type. Only PPQ allowed."                            );
		set( ERROR_DIFFERENT_RESOLUTION,          "Temporary MIDI files have different resolutions."                  );
		
		// Exporter
		set( ERROR_EXPORT,                        "Export Error in the file "                                         );
		set( ERROR_FILE_NOT_WRITABLE,             "File not writable"                                                 );
		set( OVERWRITE_FILE,                      "Overwrite the file?"                                               );
		set( EXPORTER_TICK,                       "Tick"                                                              );
		set( CONFIRM_DIALOG_YES,                  "Yes"                                                               );
		set( CONFIRM_DIALOG_NO,                   "No"                                                                );
		
		// AudioExporter
		set( ERROR_AU_SAMPLE_RATE_NOT_POS,        "<html>Config Error: Sample Rate must be positive.<br>Current Value: ");
		set( ERROR_AU_SAMPLE_SIZE_NOT_POS,        "<html>Config Error: Sample Size must be positive.<br>Current Value: ");
		set( ERROR_AU_SAMPLE_SIZE_NOT_DIV_8,      "<html>Config Error: Sample Size must be divisible by 8.<br>Current Value: " );
		set( ERROR_AU_FLOAT_NOT_32_OR_64,         "<html>Config Error: Sample Size must be 32 or 64 when using PCM_FLOAT<br>Current Value: " );
		set( ERROR_AU_FILETYPE_UNKNOWN,           "File extension unknown. Cannot determine the right file type."     );
		set( ERROR_AU_FILETYPE_NOT_SUPP,          "File type not supported by the system: "                           );
		set( ERROR_AU_FILETYPE_NOT_SUPP_F_STREAM, "<html>File type not supported for the target audio format:<br>"    );
		
		// MusescoreExporter
		set( ERROR_MSCORE_NO_OUTPUT_FILE,         "<html>MuseScore didn't create any file."                           );
		set( ERROR_MSCORE_MOVE_FAILED,            "Failed to move temporary file '%s' to '%s'"                        );
		set( ERROR_MSCORE_EXT_NOT_ALLOWED,        "File Extension not support: '%s'"                                  );
		
		// WaitView
		set( TITLE_WAIT,                          "Please Wait"                                                       );
		set( WAIT_PARSE_MPL,                      "Parsing the MidicaPL file..."                                      );
		set( WAIT_PARSE_MID,                      "Parsing the MIDI file..."                                          );
		set( WAIT_PARSE_SB,                      "Parsing the Soundbank"                                             );
		set( WAIT_PARSE_URL,                      "Downloading the Soundbank"                                         );
		set( WAIT_PARSE_FOREIGN,                  "Importing the file using %s"                                       );
		set( WAIT_REPARSE,                        "Reloading the File"                                                );
		set( WAIT_SETUP_DEVICES,                  "Setting up MIDI devices and loading Soundbank"                     );
		set( WAIT_EXPORT,                         "Exporting the file..."                                             );
		
		// ExportResultView
		set( TITLE_EXPORT_RESULT,                 "Export Result"                                                     );
		set( EXPORT_SUCCESS,                      "The file has been exported!"                                       );
		set( NUMBER_OF_WARNINGS,                  "Number of warnings:"                                               );
		set( WARNING_COL_TRACK,                   "Track"                                                             );
		set( WARNING_COL_TICK,                    "Tick"                                                              );
		set( WARNING_COL_CHANNEL,                 "Channel"                                                           );
		set( WARNING_COL_MESSAGE,                 "Warning"                                                           );
		set( WARNING_COL_DETAILS,                 "Details"                                                           );
		set( SHOW_IGN_SHORT_MSG,                  "Show Ignored Short Messages"                                       );
		set( SHOW_IGN_META_MSG,                   "Show Ignored Meta Messages"                                        );
		set( SHOW_IGN_SYSEX_MSG,                  "Show Ignored SysEx Messages"                                       );
		set( SHOW_SKIPPED_RESTS,                  "Show Skipped Rests"                                                );
		set( SHOW_OFF_NOT_FOUND,                  "Show Note-OFF not found"                                           );
		set( SHOW_CRD_GRP_FAILED,                 "Show Chord grouping failed"                                        );
		set( SHOW_OTHER_WARNINGS,                 "Show other warnings"                                               );
		
		// ExportException
		set( ERROR_TICK,                          "Tick"                                                              );
		set( ERROR_CHANNEL,                       "Channel"                                                           );
		set( ERROR_NOTE,                          "Note"                                                              );
		
		// MidicaPLExporter
		set( WARNING_IGNORED_SHORT_MESSAGE,       "Ignored Short Message"                                             );
		set( WARNING_IGNORED_META_MESSAGE,        "Ignored Meta Message"                                              );
		set( WARNING_IGNORED_SYSEX_MESSAGE,       "Ignored SysEx Message"                                             );
		set( WARNING_REST_SKIPPED,                "Rest skipped (too small)"                                          );
		set( WARNING_REST_SKIPPED_TICKS,          "ticks"                                                             );
		set( WARNING_OFF_NOT_FOUND,               "Note-Off not found"                                                );
		set( WARNING_CHORD_GROUPING_FAILED,       "Chord grouping failed"                                             );
		set( WARNING_OFF_MOVING_CONFLICT,         "<html>Cannot move Note-OFF for <b>%s</b> from tick %d to %d. Conflict detected." );
		
		// MidiDevices
		set( PERCUSSION_CHANNEL,                  "Percussion Channel"         );
		set( NORMAL_CHANNEL,                      "Normal Channel"             );
		set( DEFAULT_CHANNEL_COMMENT,             "undefined"                  );
		set( DEFAULT_INSTRUMENT_NAME,             "Fake Instrument"            );
		set( DEFAULT_PROGRAM_NUMBER,              "-"                          );
		set( ERROR_SOUNDBANK_NOT_SUPPORTED,       "Soundbank not supported"    );
		set( ERROR_SOUNDBANK_LOADING_FAILED,      "Soundbank failed to load"   );
		
		// SequenceNotSetException
		set( ERROR_SEQUENCE_NOT_SET,              "<html>A MIDI Sequence is not yet loaded.<br>"
		                                        + "You have to import a file (no matter which format)<br>"
		                                        + "before you can use the player or export a file." );
		
		// ErrorMessage
		set( CLOSE,                               "Close"                      );
		set( TITLE_ERROR,                         "Midica Error"               );
		set( TITLE_CONFIRMATION,                  "Midica Confirmation"        );
		
		// PlayerView
		set( TITLE_PLAYER,                        "Midica Player"              );
		set( REPARSE,                             "Reparse"                    );
		set( SOUNDCHECK,                          "Soundcheck"                 );
		set( MEMORIZE,                            "Memorize"                   );
		set( JUMP,                                "Go"                         );
		set( SHOW_LYRICS,                         "Show Lyrics"                );
		set( TIME_INFO_UNAVAILABLE,               "-"                          );
		set( SLIDER_MASTER_VOL,                   "Vol"                        );
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
		set( APPLY_TO_ALL_CHANNELS,               "apply to all channels"      );
		set( LBL_NOTE_HISTORY,                    "<html>Note<br>History"      );
		set( COLUMN_VELOCITY,                     "Velocity"                   );
		set( COLUMN_NUMBER,                       "Number"                     );
		set( COLUMN_NAME,                         "Name"                       );
		set( COLUMN_TICK,                         "Tick"                       );
		set( TIP_PARSE_SUCCESS,                   "Parsing successful"         );
		set( TIP_PARSE_FAILED,                    "Parsing failed"             );
		
		// SoundcheckView
		set( TITLE_SOUNDCHECK,               "Midica Soundcheck"               );
		set( SNDCHK_CHANNEL,                 "Channel"                         );
		set( SNDCHK_INSTRUMENT,              "Instrument"                      );
		set( SNDCHK_NOTE,                    "Note"                            );
		set( SNDCHK_VOLUME,                  "<html>Channel<br>Volume"         );
		set( SNDCHK_VELOCITY,                "<html>Note<br>Velocity<br>"      );
		set( SNDCHK_DURATION,                "Duration (ms)"                   );
		set( SNDCHK_KEEP_SETTINGS,           "Keep Settings"                   );
		set( SNDCHK_PLAY,                    "Play"                            );
		set( SNDCHK_COL_PROGRAM,             "Prog"                            );
		set( SNDCHK_COL_BANK,                "Bank"                            );
		set( SNDCHK_COL_NAME_SB,             "Soundbank Name"                  );
		set( SNDCHK_COL_NAME_SYNTAX,         "Syntax"                          );
		set( SNDCHK_COL_NOTE_NUM,            "Number"                          );
		set( SNDCHK_COL_NOTE_NAME,           "Name"                            );
		set( SNDCHK_COL_NOTE_SHORT,          "Short ID"                        );
		set( FILTER_ICON_TOOLTIP,            "Table Filter"                    );
		set( FILTER_ICON_TOOLTIP_ACTIVE,     "Currently filtering"             );
		set( FILTER_ICON_TOOLTIP_EMPTY,      "Currently empty"                 );
		set( FILTER_LAYER_LABEL,             "Table Filter"                    );
		set( FILTER_LAYER_CLEAR,             "Clear Filter"                    );
	}
	
	/**
	 * Initializes the internal data structure for german language translations.
	 */
	private static void initLanguageGerman() {
		
		// Config
		set( Config.CBX_HALFTONE_ID_SHARP,           "# / b (c#, cb, d#, db, ...)"           );
		set( Config.CBX_HALFTONE_ID_DIESIS,          "-diesis / -bemolle (do-diesis, ...)"   );
		set( Config.CBX_HALFTONE_ID_CIS,             "-is / -es (cis, ces, dis, des,...)"    );
		
		set( Config.CBX_SHARPFLAT_SHARP,             "Kreuz (#, -is, -diesis)"               );
		set( Config.CBX_SHARPFLAT_FLAT,              "B (b, -es, -bemolle)"                  );
		
		set( Config.CBX_NOTE_ID_INTERNATIONAL_LC,    "International: c, d, e, f, g, a, b"    );
		set( Config.CBX_NOTE_ID_INTERNATIONAL_UC,    "International: C, D, E, F, G, A, B"    );
		set( Config.CBX_NOTE_ID_ITALIAN_LC,          "Italienisch (klein): do, re, mi..."    );
		set( Config.CBX_NOTE_ID_ITALIAN_UC,          "Italienisch (groß): Do, Re, Mi..."     );
		set( Config.CBX_NOTE_ID_GERMAN_LC,           "Deutsch (klein): c, d, e, f, g, a, h"  );
		set( Config.CBX_NOTE_ID_GERMAN_UC,           "Deutsch (groß): C, D, E, F, G, A, H"   );
		
		set( Config.CBX_OCTAVE_PLUS_MINUS_N,         "+n/-n: c-2, c-, c, c+, c+2, c+3..."    );
		set( Config.CBX_OCTAVE_PLUS_MINUS,           "+/-: c--, c-, c, c+, c++..."           );
		set( Config.CBX_OCTAVE_INTERNATIONAL,        "International: c0, c1, c2..."          );
		set( Config.CBX_OCTAVE_GERMAN,               "Deutsch: C', C, c, c', c'', c'''..."   );
		
		set( Config.CBX_SYNTAX_MIXED,                "Gemischte Syntax"                      );
		set( Config.CBX_SYNTAX_LOWER,                "Syntax mit Kleinbuchstaben"            );
		set( Config.CBX_SYNTAX_UPPER,                "Syntax mit Großbuchstaben"             );
		
		set( Config.CBX_PERC_EN_1,                   "Englisch"                              );
		set( Config.CBX_PERC_DE_1,                   "Deutsch"                               );
		
		set( Config.CBX_INSTR_EN_1,                  "Englisch"                              );
		set( Config.CBX_INSTR_DE_1,                  "Deutsch"                               );
		
		// UiView
		set( CONFIGURATION,                          "Konfiguration"                         );
		set( LANGUAGE,                               "Sprache"                               );
		set( NOTE_SYSTEM,                            "Notensystem"                           );
		set( HALF_TONE_SYMBOL,                       "Versetzungszeichen"                    );
		set( SHARP_FLAT_DEFAULT,                     "Default-Versetzungszeichen"            );
		set( OCTAVE_NAMING,                          "Oktavenbezeichner"                     );
		set( SYNTAX,                                 "Syntax"                                );
		set( PERCUSSION,                             "Percussion-Bezeichner"                 );
		set( INSTRUMENT_IDS,                         "Instrumenten-Bezeichner"               );
		set( DRUMKIT_IDS,                            "Drumkit-Bezeichner"                    );
		set( TITLE_MAIN_WINDOW,                      "Midica " + Midica.VERSION              );
		set( TIMESTAMP_FORMAT,                       "dd.MM.yyyy HH:mm:ss"                   );
		set( SHOW_INFO,                              "Info & Konfigurationsdetails"          );
		set( SHOW_INFO_FROM_PLAYER,                  "Info & Konfiguration"                  );
		set( IMPORT,                                 "Laden"                                 );
		set( PLAYER,                                 "Abspielen"                             );
		set( EXPORT,                                 "Speichern"                             );
		set( TRANSPOSE_LEVEL,                        "Transposition:"                        );
		set( IMPORT_FILE,                            "Datei importieren"                     );
		set( IMPORTED_FILE,                          "Name"                                  );
		set( IMPORTED_TYPE,                          "Typ"                                   );
		set( IMPORTED_TYPE_MIDI,                     "MIDI"                                  );
		set( IMPORTED_TYPE_MPL,                      "MidicaPL"                              );
		set( IMPORTED_TYPE_ALDA,                     "ALDA"                                  );
		set( IMPORTED_TYPE_ABC,                      "ABC"                                   );
		set( IMPORTED_TYPE_LY,                       "LilyPond"                              );
		set( IMPORTED_TYPE_MSCORE,                   "Von MuseScore importiert"              );
		set( UNKNOWN_NOTE_NAME,                      "unbekannt"                             );
		set( UNKNOWN_PERCUSSION_NAME,                "unbekannt"                             );
		set( UNKNOWN_DRUMKIT_NAME,                   "unbekannt"                             );
		set( UNKNOWN_SYNTAX,                         "?"                                     );
		set( UNKNOWN_INSTRUMENT,                     "unbekannt"                             );
		set( SOUNDBANK,                              "Soundbank-Datei"                       );
		set( CURRENT_SOUNDBANK,                      "Momentane Soundbank"                   );
		set( PLAYER_BUTTON,                          "Abspielen"                             );
		set( UNCHOSEN_FILE,                          "keine Datei geladen"                   );
		set( SB_LOADED_BY_SOURCE,                    "[durch MidicaPL-Datei geladen]"        );
		set( CHOOSE_FILE,                            "Öffnen"                                );
		set( CHOOSE_FILE_EXPORT,                     "Speichern"                             );
		set( EXPORT_FILE,                            "Exportieren"                           );
		set( ERROR_NOT_YET_IMPLEMENTED,              "Diese Funktionalität ist noch nicht fertig." );
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
		ConfigComboboxModel.refill(Config.NOTE);
		
		// get note system and half tone
		String configuredNoteSystem = Config.get(Config.NOTE);
		
		// define what is a half tone (in german note systems this will be overridden)
		halfTones = new byte[5];
		halfTones[0] =  1; // C#, Db
		halfTones[1] =  3; // D#, Eb
		halfTones[2] =  6; // F#, Gb
		halfTones[3] =  8; // G#, Ab
		halfTones[4] = 10; // A#, Bb
		
		// initialize the configuration specific note system
		if (Config.CBX_NOTE_ID_INTERNATIONAL_LC.equals(configuredNoteSystem)) {
			initNotesInternational(false);
		}
		else if (Config.CBX_NOTE_ID_INTERNATIONAL_UC.equals(configuredNoteSystem)) {
			initNotesInternational(true);
		}
		else if (Config.CBX_NOTE_ID_ITALIAN_LC.equals(configuredNoteSystem)) {
			initNotesItalian(false);
		}
		else if (Config.CBX_NOTE_ID_ITALIAN_UC.equals(configuredNoteSystem)) {
			initNotesItalian(true);
		}
		else if (Config.CBX_NOTE_ID_GERMAN_LC.equals(configuredNoteSystem)) {
			initNotesGerman(false);
		}
		else if (Config.CBX_NOTE_ID_GERMAN_UC.equals(configuredNoteSystem)) {
			initNotesGerman(true);
		}
		else {
			initNotesInternational(false);
		}
		
		// the note system has changed so the half tones have to be refreshed as well
		initHalfTones();
	}
	
	/**
	 * Adds the currently configured half tone and octave symbols
	 * to the currently configured note symbols.
	 * 
	 * The octave symbols are added using the method {@link #initOctaves()}.
	 */
	public static void initHalfTones() {
		
		// refresh combobox language
		ConfigComboboxModel.refill(Config.HALF_TONE);
		ConfigComboboxModel.refill(Config.SHARP_FLAT);
		
		// get configuration
		String  configuredHalfToneSymbols = Config.get(Config.HALF_TONE);
		boolean isFlat                    = Config.isFlatConfigured();
		
		// init half tone symbols
		String  suffixFlat  = null;
		String  suffixSharp = null;
		if (Config.CBX_HALFTONE_ID_SHARP.equals(configuredHalfToneSymbols)) {
			suffixSharp = "#";
			suffixFlat  = "b";
		}
		else if (Config.CBX_HALFTONE_ID_DIESIS.equals(configuredHalfToneSymbols)) {
			suffixSharp = "-diesis";
			suffixFlat  = "-bemolle";
		}
		else if (Config.CBX_HALFTONE_ID_CIS.equals(configuredHalfToneSymbols)) {
			suffixSharp = "is";
			suffixFlat  = "es";
		}
		else {
			// default
			suffixSharp = "#";
			suffixFlat  = "b";
		}
		
		// init default half tone symbol
		String suffixDefault = isFlat ? suffixFlat : suffixSharp;
		
		// find out what to add to the current index
		// to get the index of the base note
		byte baseIncrementation;
		if (isFlat)
			baseIncrementation =  1;
		else
			baseIncrementation = -1;
		
		// initialize half tones
		isHalfTone = new boolean[12];
		for (int i = 0; i < 12; i++) {
			isHalfTone[i] = false;
		}
		for (byte i : halfTones) {
			isHalfTone[i] = true;
			
			// get base note
			int baseIndex = i + baseIncrementation;
			baseIndex %= 12;
			String baseNote = notes[baseIndex];
			
			// construct current half tone
			notes[i] = baseNote + suffixDefault;
			
			// handle exceptions
			notes[i] = replaceNoteNameExceptions(notes[i]);
		}
		
		// create additional sharps and flats
		// flat  / double-flat  / triple-flat
		// sharp / double-sharp / triple-sharp
		moreNotes     = new HashMap<>();
		moreBaseNotes = new TreeMap<>();
		boolean[] sharpOrFlat = {true, false};
		for (boolean sharp : sharpOrFlat) {
			byte increment = (byte) (sharp ? 1 : -1);
			for (byte i = 0; i < 12; i++) {
				
				// don't allow mixes like c#b, cb#, etc.
				if (isHalfTone[i] && sharp == isFlat)
					continue;
				
				String baseName = notes[i];
				int    baseNum  = i;
				for (byte step = 1; step < 4; step++) {
					if (sharp)
						baseName += suffixSharp;
					else
						baseName += suffixFlat;
					baseName = replaceNoteNameExceptions(baseName);
					baseNum += increment;
					moreNotes.put(baseName, baseNum);
					
					ArrayList<String> alternatives = moreBaseNotes.get(baseNum);
					if (null == alternatives) {
						alternatives = new ArrayList<>();
						moreBaseNotes.put(baseNum, alternatives);
					}
					alternatives.add(baseName);
				}
			}
		}
		
		// needed for getNoteAsSharpOrFlat()
		moreBaseNotesPos = new TreeMap<>();
		for (Entry<Integer, ArrayList<String>> entry : moreBaseNotes.entrySet()) {
			int num = entry.getKey();
			ArrayList<String> names = entry.getValue();
			num = num < 0 ? num + 12 : num % 12;
			ArrayList<String> notes = moreBaseNotesPos.get(num);
			if (null == notes) {
				notes = new ArrayList<>();
				moreBaseNotesPos.put(num, notes);
			}
			for (String name : names) {
				notes.add(name);
			}
		}
		
		// the half tones have changed so the octave naming has to be refreshed as well
		initOctaves();
	}
	
	/**
	 * Maps a calculated note name to the actual name.
	 * Most of the time, the note name will stay the same. But there are some
	 * exceptions in the german note naming system.
	 * 
	 * @param name  the calculated note name
	 * @return the actual name
	 */
	private static String replaceNoteNameExceptions(String name) {
		
		if ("Hb".equals(name))
			return "B";
		if ("hb".equals(name))
			return "b";
		if ("Ees".equals(name))
			return "Es";
		if ("ees".equals(name))
			return "es";
		if ("Aes".equals(name))
			return "As";
		if ("aes".equals(name))
			return "as";
		if ("Hes".equals(name))
			return "B";
		if ("hes".equals(name))
			return "b";
		
		return name;
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
		boolean isGermanOctave = Config.CBX_OCTAVE_GERMAN.equals(Config.get(Config.OCTAVE));
		
		// refresh combobox language
		ConfigComboboxModel.refill(Config.OCTAVE);
		
		// create number-to-name and name-to-number translations
		noteIntToName = new HashMap<>();
		noteNameToInt = new HashMap<>();
		int octave = -5;
		int num    =  0;
		OCTAVE:
		while (true) {
			if (0 == num % 12 && num > 0)
				octave++;
			
			// notes
			for (String name : notes) {
				if (num > 127)
					break OCTAVE;
				if (isGermanOctave)
					name = octave < -1 ? ucFirst(name) : lcFirst(name);
				String fullName = name + getOctavePostfix(octave);
				noteNameToInt.put(fullName, num);
				noteIntToName.put(num, fullName);
				num++;
			}
		}
		
		// calculate alternative note names
		moreNotesByNum = new ArrayList<>();
		for (int i = 0; i < 128; i++) {
			moreNotesByNum.add(new TreeSet<>());
		}
		int middleC = 60;
		for (Entry<Integer, ArrayList<String>> entry : moreBaseNotes.entrySet()) {
			
			int baseNum = entry.getKey();
			ArrayList<String> baseNames = entry.getValue();
			
			for (octave = -6; octave < 6; octave++) {
				num = octave * 12 + middleC + baseNum;
				if (num < 0 || num > 127)
					continue;
				
				String octavePostfix = getOctavePostfix(octave);
				for (String baseName : baseNames) {
					String name = baseName + octavePostfix;
					if (isGermanOctave)
						name = octave < -1 ? ucFirst(name) : lcFirst(name);
					
					// ignore duplicates
					if (noteNameToInt.get(name) != null)
						continue;
					
					moreNotesByNum.get(num).add(name);
					noteNameToInt.put(name, num);
				}
			}
		}
	}
	
	/**
	 * Calculates and returns the postfix of an octave.
	 * 
	 * @param octaveNumber  0 for the middle octave, positive for higher octaves, negative for lower octaves
	 * @return the octave postfix
	 */
	private static String getOctavePostfix(int octaveNumber) {
		String configuredOctave = Config.get(Config.OCTAVE);
		
		if (Config.CBX_OCTAVE_PLUS_MINUS_N.equals(configuredOctave)) {
			if (-6 == octaveNumber) return "-6";
			if (-5 == octaveNumber) return "-5";
			if (-4 == octaveNumber) return "-4";
			if (-3 == octaveNumber) return "-3";
			if (-2 == octaveNumber) return "-2";
			if (-1 == octaveNumber) return "-";
			if ( 0 == octaveNumber) return "";
			if ( 1 == octaveNumber) return "+";
			if ( 2 == octaveNumber) return "+2";
			if ( 3 == octaveNumber) return "+3";
			if ( 4 == octaveNumber) return "+4";
			if ( 5 == octaveNumber) return "+5";
			if ( 6 == octaveNumber) return "+6";
		}
		if (Config.CBX_OCTAVE_PLUS_MINUS.equals(configuredOctave)) {
			if (-6 == octaveNumber) return "------";
			if (-5 == octaveNumber) return "-----";
			if (-4 == octaveNumber) return "----";
			if (-3 == octaveNumber) return "---";
			if (-2 == octaveNumber) return "--";
			if (-1 == octaveNumber) return "-";
			if ( 0 == octaveNumber) return "";
			if ( 1 == octaveNumber) return "+";
			if ( 2 == octaveNumber) return "++";
			if ( 3 == octaveNumber) return "+++";
			if ( 4 == octaveNumber) return "++++";
			if ( 5 == octaveNumber) return "+++++";
			if ( 6 == octaveNumber) return "++++++";
		}
		if (Config.CBX_OCTAVE_INTERNATIONAL.equals(configuredOctave)) {
			if (-6 == octaveNumber) return "-2";
			if (-5 == octaveNumber) return "-1";
			if (-4 == octaveNumber) return "0";
			if (-3 == octaveNumber) return "1";
			if (-2 == octaveNumber) return "2";
			if (-1 == octaveNumber) return "3";
			if ( 0 == octaveNumber) return "4";
			if ( 1 == octaveNumber) return "5";
			if ( 2 == octaveNumber) return "6";
			if ( 3 == octaveNumber) return "7";
			if ( 4 == octaveNumber) return "8";
			if ( 5 == octaveNumber) return "9";
			if ( 6 == octaveNumber) return "10";
		}
		if (Config.CBX_OCTAVE_GERMAN.equals(configuredOctave)) {
			if (-6 == octaveNumber) return "''''";
			if (-5 == octaveNumber) return "'''";
			if (-4 == octaveNumber) return "''";
			if (-3 == octaveNumber) return "'";
			if (-2 == octaveNumber) return "";
			if (-1 == octaveNumber) return "";
			if ( 0 == octaveNumber) return "'";
			if ( 1 == octaveNumber) return "''";
			if ( 2 == octaveNumber) return "'''";
			if ( 3 == octaveNumber) return "''''";
			if ( 4 == octaveNumber) return "'''''";
			if ( 5 == octaveNumber) return "''''''";
			if ( 6 == octaveNumber) return "'''''''";
		}
		
		return "invalid octave";
	}
	
	/**
	 * Transforms the first character of the given string to lower-case.
	 * This is used for the german octave naming system.
	 * 
	 * @param str    the string to be transformed
	 * @return the transformed string.
	 */
	private static String lcFirst(String str) {
		char characters[] = str.toCharArray();
		characters[0] = Character.toLowerCase(characters[0]);
		return new String(characters);
	}
	
	/**
	 * Transforms the first character of the given string to upper-case.
	 * This is used for the german octave naming system.
	 * 
	 * @param str    the string to be transformed
	 * @return the transformed string.
	 */
	private static String ucFirst(String str) {
		char characters[] = str.toCharArray();
		characters[0] = Character.toUpperCase(characters[0]);
		return new String(characters);
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
		ConfigComboboxModel.refill(Config.SYNTAX);
		
		// get syntax
		String configuredSyntax = Config.get(Config.SYNTAX);
		
		// set up default syntax
		setSyntax( SYNTAX_DEFINE,            "DEFINE"        );
		setSyntax( SYNTAX_COMMENT,           "//"            );
		setSyntax( SYNTAX_CONST,             "CONST"         );
		setSyntax( SYNTAX_VAR,               "VAR"           );
		setSyntax( SYNTAX_VAR_SYMBOL,        "$"             );
		setSyntax( SYNTAX_VAR_ASSIGNER,      "="             );
		setSyntax( SYNTAX_PARAM_NAMED_OPEN,  "{"             );
		setSyntax( SYNTAX_PARAM_NAMED_CLOSE, "}"             );
		setSyntax( SYNTAX_PARAM_INDEX_OPEN,  "["             );
		setSyntax( SYNTAX_PARAM_INDEX_CLOSE, "]"             );
		setSyntax( SYNTAX_GLOBAL,            "*"             );
		setSyntax( SYNTAX_P,                 "p"             );
		setSyntax( SYNTAX_END,               "END"           );
		setSyntax( SYNTAX_BLOCK_OPEN,        "{"             );
		setSyntax( SYNTAX_BLOCK_CLOSE,       "}"             );
		setSyntax( SYNTAX_FUNCTION,          "FUNCTION"      );
		setSyntax( SYNTAX_PATTERN,           "PATTERN"       );
		setSyntax( SYNTAX_PATTERN_INDEX_SEP, ","             );
		setSyntax( SYNTAX_PARAM_OPEN,        "("             );
		setSyntax( SYNTAX_PARAM_CLOSE,       ")"             );
		setSyntax( SYNTAX_PARAM_SEPARATOR,   ","             );
		setSyntax( SYNTAX_PARAM_ASSIGNER,    "="             );
		setSyntax( SYNTAX_CALL,              "CALL"          );
		setSyntax( SYNTAX_INSTRUMENT,        "INSTRUMENT"    );
		setSyntax( SYNTAX_INSTRUMENTS,       "INSTRUMENTS"   );
		setSyntax( SYNTAX_META,              "META"          );
		setSyntax( SYNTAX_META_COPYRIGHT,    "copyright"     );
		setSyntax( SYNTAX_META_TITLE,        "title"         );
		setSyntax( SYNTAX_META_COMPOSER,     "composer"      );
		setSyntax( SYNTAX_META_LYRICIST,     "lyrics"        );
		setSyntax( SYNTAX_META_ARTIST,       "artist"        );
		setSyntax( SYNTAX_META_SOFT_KARAOKE, "SOFT_KARAOKE"  );
		setSyntax( SYNTAX_META_SK_VERSION,   "version"       );
		setSyntax( SYNTAX_META_SK_LANG,      "language"      );
		setSyntax( SYNTAX_META_SK_TITLE,     "title"         );
		setSyntax( SYNTAX_META_SK_AUTHOR,    "author"        );
		setSyntax( SYNTAX_META_SK_COPYRIGHT, "copyright"     );
		setSyntax( SYNTAX_META_SK_INFO,      "info"          );
		setSyntax( SYNTAX_TEMPO,             "tempo"         );
		setSyntax( SYNTAX_TIME_SIG,          "time"          );
		setSyntax( SYNTAX_TIME_SIG_SLASH,    "/"             );
		setSyntax( SYNTAX_KEY_SIG,           "key"           );
		setSyntax( SYNTAX_KEY_SEPARATOR,     "/"             );
		setSyntax( SYNTAX_KEY_MAJ,           "maj"           );
		setSyntax( SYNTAX_KEY_MIN,           "min"           );
		setSyntax( SYNTAX_PARTIAL_SYNC_RANGE, "-"            );
		setSyntax( SYNTAX_PARTIAL_SYNC_SEP,   ","            );
		setSyntax( SYNTAX_OPT_SEPARATOR,      ","            );
		setSyntax( SYNTAX_OPT_ASSIGNER,       "="            );
		setSyntax( SYNTAX_PROG_BANK_SEP,      ","            );
		setSyntax( SYNTAX_BANK_SEP,           "/"            );
		setSyntax( SYNTAX_VELOCITY,           "velocity"     );
		setSyntax( SYNTAX_V,                  "v"            );
		setSyntax( SYNTAX_DURATION,           "duration"     );
		setSyntax( SYNTAX_D,                  "d"            );
		setSyntax( SYNTAX_DURATION_PERCENT,   "%"            );
		setSyntax( SYNTAX_MULTIPLE,           "multiple"     );
		setSyntax( SYNTAX_M,                  "m"            );
		setSyntax( SYNTAX_QUANTITY,           "quantity"     );
		setSyntax( SYNTAX_Q,                  "q"            );
		setSyntax( SYNTAX_LYRICS,             "lyrics"       );
		setSyntax( SYNTAX_L,                  "l"            );
		setSyntax( SYNTAX_LYRICS_SPACE,       "_"            );
		setSyntax( SYNTAX_LYRICS_CR,          "\\r"          );
		setSyntax( SYNTAX_LYRICS_LF,          "\\n"          );
		setSyntax( SYNTAX_LYRICS_COMMA,       "\\c"          );
		setSyntax( SYNTAX_TUPLET,             "tuplet"       );
		setSyntax( SYNTAX_T,                  "t"            );
		setSyntax( SYNTAX_TREMOLO,            "tremolo"      );
		setSyntax( SYNTAX_TR,                 "tr"           );
		setSyntax( SYNTAX_SHIFT,              "shift"        );
		setSyntax( SYNTAX_S,                  "s"            );
		setSyntax( SYNTAX_IF,                 "if"           );
		setSyntax( SYNTAX_ELSIF,              "elsif"        );
		setSyntax( SYNTAX_ELSE,               "else"         );
		setSyntax( SYNTAX_COND_EQ,            "=="           );
		setSyntax( SYNTAX_COND_NEQ,           "!="           );
		setSyntax( SYNTAX_COND_NDEF,          "!"            );
		setSyntax( SYNTAX_COND_LT,            "<"            );
		setSyntax( SYNTAX_COND_LE,            "<="           );
		setSyntax( SYNTAX_COND_GT,            ">"            );
		setSyntax( SYNTAX_COND_GE,            ">="           );
		setSyntax( SYNTAX_COND_IN,            "in"           );
		setSyntax( SYNTAX_COND_IN_SEP,        ";"            );
		setSyntax( SYNTAX_REST,               "-"            );
		setSyntax( SYNTAX_CHORD,              "CHORD"        );
		setSyntax( SYNTAX_CHORD_ASSIGNER,     "="            );
		setSyntax( SYNTAX_CHORD_SEPARATOR,    ","            );
		setSyntax( SYNTAX_INCLUDE,            "INCLUDE"      );
		setSyntax( SYNTAX_SOUNDBANK,          "SOUNDBANK"    );
		setSyntax( SYNTAX_SOUNDFONT,          "SOUNDFONT"    );
		setSyntax( SYNTAX_ZEROLENGTH,         "-"            );
		setSyntax( SYNTAX_32,                 "/32"          );
		setSyntax( SYNTAX_16,                 "/16"          );
		setSyntax( SYNTAX_8,                  "/8"           );
		setSyntax( SYNTAX_4,                  "/4"           );
		setSyntax( SYNTAX_2,                  "/2"           );
		setSyntax( SYNTAX_1,                  "/1"           );
		setSyntax( SYNTAX_M1,                 "*1"           );
		setSyntax( SYNTAX_M2,                 "*2"           );
		setSyntax( SYNTAX_M4,                 "*4"           );
		setSyntax( SYNTAX_M8,                 "*8"           );
		setSyntax( SYNTAX_M16,                "*16"          );
		setSyntax( SYNTAX_M32,                "*32"          );
		setSyntax( SYNTAX_DOT,                "."            );
		setSyntax( SYNTAX_TRIPLET,            "t"            );
		setSyntax( SYNTAX_TUPLET_INTRO,       "t"            );
		setSyntax( SYNTAX_TUPLET_FOR,         ":"            );
		setSyntax( SYNTAX_LENGTH_PLUS,        "+"            );
		
		// switch to lower/upper, if needed
		if (Config.CBX_SYNTAX_LOWER.equals(configuredSyntax)) {
			for (String id : syntax.keySet()) {
				String keyword = syntax.get(id).toLowerCase();
				setSyntax(id, keyword);
			}
		}
		else if (Config.CBX_SYNTAX_UPPER.equals(configuredSyntax)) {
			for (String id : syntax.keySet()) {
				String keyword = syntax.get(id).toUpperCase();
				setSyntax(id, keyword);
			}
		}
		
		// init syntax for the syntax tab in the info view
		syntaxList = new ArrayList<SyntaxElement>();
		
		addSyntaxCategory(get(SYNTAX_CAT_DEFINITION));
		addSyntaxForInfoView( SYNTAX_DEFINE            );
		addSyntaxForInfoView( SYNTAX_META              );
		addSyntaxForInfoView( SYNTAX_FUNCTION          );
		addSyntaxForInfoView( SYNTAX_END               );
		addSyntaxForInfoView( SYNTAX_PATTERN           );
		addSyntaxForInfoView( SYNTAX_PATTERN_INDEX_SEP );
		addSyntaxForInfoView( SYNTAX_CHORD             );
		addSyntaxForInfoView( SYNTAX_CHORD_ASSIGNER    );
		addSyntaxForInfoView( SYNTAX_CHORD_SEPARATOR   );
		addSyntaxForInfoView( SYNTAX_INSTRUMENT        );
		addSyntaxForInfoView( SYNTAX_INSTRUMENTS       );
		addSyntaxForInfoView( SYNTAX_PROG_BANK_SEP     );
		addSyntaxForInfoView( SYNTAX_BANK_SEP          );
		addSyntaxForInfoView( SYNTAX_BLOCK_OPEN        );
		addSyntaxForInfoView( SYNTAX_BLOCK_CLOSE       );
		
		addSyntaxCategory(get(SYNTAX_CAT_EXECUTE));
		addSyntaxForInfoView( SYNTAX_CALL            );
		addSyntaxForInfoView( SYNTAX_PARAM_OPEN      );
		addSyntaxForInfoView( SYNTAX_PARAM_CLOSE     );
		addSyntaxForInfoView( SYNTAX_PARAM_SEPARATOR );
		addSyntaxForInfoView( SYNTAX_PARAM_ASSIGNER  );
		addSyntaxForInfoView( SYNTAX_INCLUDE         );
		addSyntaxForInfoView( SYNTAX_SOUNDBANK       );
		addSyntaxForInfoView( SYNTAX_SOUNDFONT       );
		
		addSyntaxCategory(get(SYNTAX_CAT_GLOBAL));
		addSyntaxForInfoView( SYNTAX_GLOBAL             );
		addSyntaxForInfoView( SYNTAX_TEMPO              );
		addSyntaxForInfoView( SYNTAX_TIME_SIG           );
		addSyntaxForInfoView( SYNTAX_TIME_SIG_SLASH     );
		addSyntaxForInfoView( SYNTAX_KEY_SIG            );
		addSyntaxForInfoView( SYNTAX_KEY_SEPARATOR      );
		addSyntaxForInfoView( SYNTAX_KEY_MAJ            );
		addSyntaxForInfoView( SYNTAX_KEY_MIN            );
		addSyntaxForInfoView( SYNTAX_PARTIAL_SYNC_RANGE );
		addSyntaxForInfoView( SYNTAX_PARTIAL_SYNC_SEP   );
		
		addSyntaxCategory(get(SYNTAX_CAT_OTHER));
		addSyntaxForInfoView( SYNTAX_COMMENT );
		addSyntaxForInfoView( SYNTAX_REST    );
		addSyntaxForInfoView( SYNTAX_P       );
		
		addSyntaxCategory(get(SYNTAX_CAT_META));
		addSyntaxForInfoView( SYNTAX_META_COPYRIGHT    );
		addSyntaxForInfoView( SYNTAX_META_TITLE        );
		addSyntaxForInfoView( SYNTAX_META_COMPOSER     );
		addSyntaxForInfoView( SYNTAX_META_LYRICIST     );
		addSyntaxForInfoView( SYNTAX_META_ARTIST       );
		addSyntaxForInfoView( SYNTAX_META_SOFT_KARAOKE );
		addSyntaxForInfoView( SYNTAX_META_SK_VERSION   );
		addSyntaxForInfoView( SYNTAX_META_SK_LANG      );
		addSyntaxForInfoView( SYNTAX_META_SK_TITLE     );
		addSyntaxForInfoView( SYNTAX_META_SK_AUTHOR    );
		addSyntaxForInfoView( SYNTAX_META_SK_COPYRIGHT );
		addSyntaxForInfoView( SYNTAX_META_SK_INFO      );
		
		addSyntaxCategory(get(SYNTAX_CAT_VAR_AND_CONST));
		addSyntaxForInfoView( SYNTAX_CONST             );
		addSyntaxForInfoView( SYNTAX_VAR               );
		addSyntaxForInfoView( SYNTAX_VAR_SYMBOL        );
		addSyntaxForInfoView( SYNTAX_VAR_ASSIGNER      );
		addSyntaxForInfoView( SYNTAX_PARAM_NAMED_OPEN  );
		addSyntaxForInfoView( SYNTAX_PARAM_NAMED_CLOSE );
		addSyntaxForInfoView( SYNTAX_PARAM_INDEX_OPEN  );
		addSyntaxForInfoView( SYNTAX_PARAM_INDEX_CLOSE );
		
		addSyntaxCategory(get(SYNTAX_CAT_OPTION));
		addSyntaxForInfoView( SYNTAX_OPT_SEPARATOR    );
		addSyntaxForInfoView( SYNTAX_OPT_ASSIGNER     );
		addSyntaxForInfoView( SYNTAX_VELOCITY         );
		addSyntaxForInfoView( SYNTAX_V                );
		addSyntaxForInfoView( SYNTAX_DURATION         );
		addSyntaxForInfoView( SYNTAX_D                );
		addSyntaxForInfoView( SYNTAX_DURATION_PERCENT );
		addSyntaxForInfoView( SYNTAX_MULTIPLE         );
		addSyntaxForInfoView( SYNTAX_M                );
		addSyntaxForInfoView( SYNTAX_QUANTITY         );
		addSyntaxForInfoView( SYNTAX_Q                );
		addSyntaxForInfoView( SYNTAX_LYRICS           );
		addSyntaxForInfoView( SYNTAX_L                );
		addSyntaxForInfoView( SYNTAX_LYRICS_SPACE     );
		addSyntaxForInfoView( SYNTAX_LYRICS_CR        );
		addSyntaxForInfoView( SYNTAX_LYRICS_LF        );
		addSyntaxForInfoView( SYNTAX_LYRICS_COMMA     );
		addSyntaxForInfoView( SYNTAX_TUPLET           );
		addSyntaxForInfoView( SYNTAX_T                );
		addSyntaxForInfoView( SYNTAX_TREMOLO          );
		addSyntaxForInfoView( SYNTAX_TR               );
		addSyntaxForInfoView( SYNTAX_SHIFT            );
		addSyntaxForInfoView( SYNTAX_S                );
		addSyntaxForInfoView( SYNTAX_IF               );
		addSyntaxForInfoView( SYNTAX_ELSIF            );
		addSyntaxForInfoView( SYNTAX_ELSE             );
		
		addSyntaxCategory(get(SYNTAX_CAT_CONDITON));
		addSyntaxForInfoView( SYNTAX_COND_EQ          );
		addSyntaxForInfoView( SYNTAX_COND_NEQ         );
		addSyntaxForInfoView( SYNTAX_COND_NDEF        );
		addSyntaxForInfoView( SYNTAX_COND_LT          );
		addSyntaxForInfoView( SYNTAX_COND_LE          );
		addSyntaxForInfoView( SYNTAX_COND_GT          );
		addSyntaxForInfoView( SYNTAX_COND_GE          );
		addSyntaxForInfoView( SYNTAX_COND_IN          );
		addSyntaxForInfoView( SYNTAX_COND_IN_SEP      );
		
		addSyntaxCategory(get(SYNTAX_CAT_NOTE_LENGTH));
		addSyntaxForInfoView( SYNTAX_ZEROLENGTH    );
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
		addSyntaxForInfoView( SYNTAX_TUPLET_INTRO  );
		addSyntaxForInfoView( SYNTAX_TUPLET_FOR    );
		addSyntaxForInfoView( SYNTAX_LENGTH_PLUS   );
	}
	
	/**
	 * Creates drumkit and percussion instrument translations.
	 * 
	 * - Creates translations between note values and the corresponding configured
	 *   percussion ID.
	 * - Creates translations between program numbers and the corresponding configured
	 *   drumkit IDs.
	 */
	public static void initPercussion() {
		
		// refresh combobox language
		ConfigComboboxModel.refill(Config.PERCUSSION);
		
		// get language
		String percSet = Config.get(Config.PERCUSSION);
		
		// init percussion translations
		initPercussionEnglish();
		if (Config.CBX_PERC_DE_1.equals(percSet)) {
			initPercussionGerman();
		}
		
		// init names to integers (for percussion instruments)
		percussionIdToInt = new HashMap<String, Integer>();
		
		// long identifiers
		percussionIdToInt.put( get(PERCUSSION_HIGH_Q),          27 );
		percussionIdToInt.put( get(PERCUSSION_SLAP),            28 );
		percussionIdToInt.put( get(PERCUSSION_SCRATCH_PUSH),    29 );
		percussionIdToInt.put( get(PERCUSSION_SCRATCH_PULL),    30 );
		percussionIdToInt.put( get(PERCUSSION_STICKS),          31 );
		percussionIdToInt.put( get(PERCUSSION_SQUARE_CLICK),    32 );
		percussionIdToInt.put( get(PERCUSSION_METRONOME_CLICK), 33 );
		percussionIdToInt.put( get(PERCUSSION_METRONOME_BELL),  34 );
		percussionIdToInt.put( get(PERCUSSION_BASS_DRUM_2),     35 );
		percussionIdToInt.put( get(PERCUSSION_BASS_DRUM_1),     36 );
		percussionIdToInt.put( get(PERCUSSION_RIM_SHOT),        37 );
		percussionIdToInt.put( get(PERCUSSION_SNARE_DRUM_1),    38 );
		percussionIdToInt.put( get(PERCUSSION_HAND_CLAP),       39 );
		percussionIdToInt.put( get(PERCUSSION_SNARE_DRUM_2),    40 );
		percussionIdToInt.put( get(PERCUSSION_TOM_1),           41 );
		percussionIdToInt.put( get(PERCUSSION_HI_HAT_CLOSED),   42 );
		percussionIdToInt.put( get(PERCUSSION_TOM_2),           43 );
		percussionIdToInt.put( get(PERCUSSION_HI_HAT_PEDAL),    44 );
		percussionIdToInt.put( get(PERCUSSION_TOM_3),           45 );
		percussionIdToInt.put( get(PERCUSSION_HI_HAT_OPEN),     46 );
		percussionIdToInt.put( get(PERCUSSION_TOM_4),           47 );
		percussionIdToInt.put( get(PERCUSSION_TOM_5),           48 );
		percussionIdToInt.put( get(PERCUSSION_CRASH_CYMBAL_1),  49 );
		percussionIdToInt.put( get(PERCUSSION_TOM_6),           50 );
		percussionIdToInt.put( get(PERCUSSION_RIDE_CYMBAL_1),   51 );
		percussionIdToInt.put( get(PERCUSSION_CHINESE_CYMBAL),  52 );
		percussionIdToInt.put( get(PERCUSSION_RIDE_BELL),       53 );
		percussionIdToInt.put( get(PERCUSSION_TAMBOURINE),      54 );
		percussionIdToInt.put( get(PERCUSSION_SPLASH_CYMBAL),   55 );
		percussionIdToInt.put( get(PERCUSSION_COWBELL),         56 );
		percussionIdToInt.put( get(PERCUSSION_CRASH_CYMBAL_2),  57 );
		percussionIdToInt.put( get(PERCUSSION_VIBRA_SLAP),      58 );
		percussionIdToInt.put( get(PERCUSSION_RIDE_CYMBAL_2),   59 );
		percussionIdToInt.put( get(PERCUSSION_BONGO_HIGH),      60 );
		percussionIdToInt.put( get(PERCUSSION_BONGO_LOW),       61 );
		percussionIdToInt.put( get(PERCUSSION_CONGA_MUTE),      62 );
		percussionIdToInt.put( get(PERCUSSION_CONGA_OPEN),      63 );
		percussionIdToInt.put( get(PERCUSSION_CONGA_LOW),       64 );
		percussionIdToInt.put( get(PERCUSSION_TIMBALES_HIGH),   65 );
		percussionIdToInt.put( get(PERCUSSION_TIMBALES_LOW),    66 );
		percussionIdToInt.put( get(PERCUSSION_AGOGO_HIGH),      67 );
		percussionIdToInt.put( get(PERCUSSION_AGOGO_LOW),       68 );
		percussionIdToInt.put( get(PERCUSSION_CABASA),          69 );
		percussionIdToInt.put( get(PERCUSSION_MARACAS),         70 );
		percussionIdToInt.put( get(PERCUSSION_WHISTLE_SHORT),   71 );
		percussionIdToInt.put( get(PERCUSSION_WHISTLE_LONG),    72 );
		percussionIdToInt.put( get(PERCUSSION_GUIRO_SHORT),     73 );
		percussionIdToInt.put( get(PERCUSSION_GUIRO_LONG),      74 );
		percussionIdToInt.put( get(PERCUSSION_CLAVE),           75 );
		percussionIdToInt.put( get(PERCUSSION_WOOD_BLOCK_HIGH), 76 );
		percussionIdToInt.put( get(PERCUSSION_WOOD_BLOCK_LOW),  77 );
		percussionIdToInt.put( get(PERCUSSION_CUICA_MUTE),      78 );
		percussionIdToInt.put( get(PERCUSSION_CUICA_OPEN),      79 );
		percussionIdToInt.put( get(PERCUSSION_TRIANGLE_MUTE),   80 );
		percussionIdToInt.put( get(PERCUSSION_TRIANGLE_OPEN),   81 );
		percussionIdToInt.put( get(PERCUSSION_SHAKER),          82 );
		percussionIdToInt.put( get(PERCUSSION_JINGLE_BELL),     83 );
		percussionIdToInt.put( get(PERCUSSION_BELLTREE),        84 );
		percussionIdToInt.put( get(PERCUSSION_CASTANETS),       85 );
		percussionIdToInt.put( get(PERCUSSION_SURDO_MUTE),      86 );
		percussionIdToInt.put( get(PERCUSSION_SURDO_OPEN),      87 );
		
		// init translation integer --> long ID
		percussionIntToLongId = new HashMap<Integer, String>();
		for (String key : percussionIdToInt.keySet()) {
			percussionIntToLongId.put(percussionIdToInt.get(key), key);
		}
		
		// short identifiers
		percussionIdToInt.put( get(PERCUSSION_SHORT_HIGH_Q),          27 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_SLAP),            28 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_SCRATCH_PUSH),    29 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_SCRATCH_PULL),    30 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_STICKS),          31 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_SQUARE_CLICK),    32 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_METRONOME_CLICK), 33 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_METRONOME_BELL),  34 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_BASS_DRUM_2),     35 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_BASS_DRUM_1),     36 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_RIM_SHOT),        37 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_SNARE_DRUM_1),    38 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_HAND_CLAP),       39 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_SNARE_DRUM_2),    40 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_TOM_1),           41 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_HI_HAT_CLOSED),   42 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_TOM_2),           43 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_HI_HAT_PEDAL),    44 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_TOM_3),           45 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_HI_HAT_OPEN),     46 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_TOM_4),           47 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_TOM_5),           48 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_CRASH_CYMBAL_1),  49 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_TOM_6),           50 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_RIDE_CYMBAL_1),   51 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_CHINESE_CYMBAL),  52 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_RIDE_BELL),       53 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_TAMBOURINE),      54 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_SPLASH_CYMBAL),   55 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_COWBELL),         56 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_CRASH_CYMBAL_2),  57 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_VIBRA_SLAP),      58 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_RIDE_CYMBAL_2),   59 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_BONGO_HIGH),      60 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_BONGO_LOW),       61 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_CONGA_MUTE),      62 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_CONGA_OPEN),      63 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_CONGA_LOW),       64 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_TIMBALES_HIGH),   65 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_TIMBALES_LOW),    66 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_AGOGO_HIGH),      67 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_AGOGO_LOW),       68 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_CABASA),          69 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_MARACAS),         70 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_WHISTLE_SHORT),   71 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_WHISTLE_LONG),    72 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_GUIRO_SHORT),     73 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_GUIRO_LONG),      74 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_CLAVE),           75 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_WOOD_BLOCK_HIGH), 76 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_WOOD_BLOCK_LOW),  77 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_CUICA_MUTE),      78 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_CUICA_OPEN),      79 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_TRIANGLE_MUTE),   80 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_TRIANGLE_OPEN),   81 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_SHAKER),          82 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_JINGLE_BELL),     83 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_BELLTREE),        84 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_CASTANETS),       85 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_SURDO_MUTE),      86 );
		percussionIdToInt.put( get(PERCUSSION_SHORT_SURDO_OPEN),      87 );
		
		// init translation integer --> short ID
		percussionIntToShortId = new HashMap<Integer, String>();
		for (String key : percussionIdToInt.keySet()) {
			int    number = percussionIdToInt.get(key);
			String longId = percussionIntToLongId.get(number);
			if (key.equals(longId))
				continue;
			percussionIntToShortId.put(percussionIdToInt.get(key), key);
		}
	}
	
	/**
	 * Creates translations between note values and the corresponding configured
	 * instrument IDs.
	 */
	public static void initInstruments() {
		
		// refresh combobox language
		ConfigComboboxModel.refill(Config.INSTRUMENT);

		// get language
		String instrSet = Config.get(Config.INSTRUMENT);
		
		// init instrument translations
		instrIntToName   = new HashMap<Integer, String>();
		instrNameToInt   = new HashMap<String, Integer>();
		instrumentList   = new ArrayList<InstrumentElement>();
		drumkitIntToName = new HashMap<Integer, String>();
		drumkitNameToInt = new HashMap<String, Integer>();
		drumkitList      = new ArrayList<InstrumentElement>();
		if (Config.CBX_PERC_DE_1.equals(instrSet)) {
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
	private static void initNotesInternational(boolean upperCase) {
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
	 * Initializes note translations for italien note names.
	 * 
	 * @param upperCase true if the resulting note names shall have an capitalized
	 *                  first character -- otherwise: false
	 */
	private static void initNotesItalian(boolean upperCase) {
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
	 * Initializes note translations for german note names.
	 * 
	 * @param upperCase true if the resulting note names shall be capitalized
	 *                  -- otherwise: false
	 */
	private static void initNotesGerman(boolean upperCase) {
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
		halfTones = new byte[4];
		halfTones[0] =  1; // C#, Db
		halfTones[1] =  3; // D#, Eb
		halfTones[2] =  6; // F#, Gb
		halfTones[3] =  8; // G#, Ab
	}
	
	/**
	 * Sets up english percussion IDs.
	 */
	private static void initPercussionEnglish() {
		// percussion instruments (long)
		set( PERCUSSION_HIGH_Q,          "high_q"          );
		set( PERCUSSION_SLAP,            "slap"            );
		set( PERCUSSION_SCRATCH_PUSH,    "scratch_push"    );
		set( PERCUSSION_SCRATCH_PULL,    "scratch_pull"    );
		set( PERCUSSION_STICKS,          "sticks"          );
		set( PERCUSSION_SQUARE_CLICK,    "square_click"    );
		set( PERCUSSION_METRONOME_CLICK, "metronome_click" );
		set( PERCUSSION_METRONOME_BELL,  "metronome_bell"  );
		set( PERCUSSION_BASS_DRUM_2,     "bass_drum_2"     );
		set( PERCUSSION_BASS_DRUM_1,     "bass_drum_1"     );
		set( PERCUSSION_RIM_SHOT,        "rim_shot"        );
		set( PERCUSSION_SNARE_DRUM_1,    "snare_drum_1"    );
		set( PERCUSSION_HAND_CLAP,       "hand_clap"       );
		set( PERCUSSION_SNARE_DRUM_2,    "snare_drum_2"    );
		set( PERCUSSION_TOM_1,           "tom_1"           );
		set( PERCUSSION_HI_HAT_CLOSED,   "hi_hat_closed"   );
		set( PERCUSSION_TOM_2,           "tom_2"           );
		set( PERCUSSION_HI_HAT_PEDAL,    "hi_hat_pedal"    );
		set( PERCUSSION_TOM_3,           "tom_3"           );
		set( PERCUSSION_HI_HAT_OPEN,     "hi_hat_open"     );
		set( PERCUSSION_TOM_4,           "tom_4"           );
		set( PERCUSSION_TOM_5,           "tom_5"           );
		set( PERCUSSION_CRASH_CYMBAL_1,  "crash_cymbal_1"  );
		set( PERCUSSION_TOM_6,           "tom_6"           );
		set( PERCUSSION_RIDE_CYMBAL_1,   "ride_cymbal_1"   );
		set( PERCUSSION_CHINESE_CYMBAL,  "chinese_cymbal"  );
		set( PERCUSSION_RIDE_BELL,       "ride_bell"       );
		set( PERCUSSION_TAMBOURINE,      "tambourine"      );
		set( PERCUSSION_SPLASH_CYMBAL,   "splash_cymbal"   );
		set( PERCUSSION_COWBELL,         "cowbell"         );
		set( PERCUSSION_CRASH_CYMBAL_2,  "crash_cymbal_2"  );
		set( PERCUSSION_VIBRA_SLAP,      "vibra_slap"      );
		set( PERCUSSION_RIDE_CYMBAL_2,   "ride_cymbal_2"   );
		set( PERCUSSION_BONGO_HIGH,      "bongo_high"      );
		set( PERCUSSION_BONGO_LOW,       "bongo_low"       );
		set( PERCUSSION_CONGA_MUTE,      "conga_mute"      );
		set( PERCUSSION_CONGA_OPEN,      "conga_open"      );
		set( PERCUSSION_CONGA_LOW,       "conga_low"       );
		set( PERCUSSION_TIMBALES_HIGH,   "timbales_high"   );
		set( PERCUSSION_TIMBALES_LOW,    "timbales_low"    );
		set( PERCUSSION_AGOGO_HIGH,      "agogo_high"      );
		set( PERCUSSION_AGOGO_LOW,       "agogo_low"       );
		set( PERCUSSION_CABASA,          "cabasa"          );
		set( PERCUSSION_MARACAS,         "maracas"         );
		set( PERCUSSION_WHISTLE_SHORT,   "whistle_short"   );
		set( PERCUSSION_WHISTLE_LONG,    "whistle_long"    );
		set( PERCUSSION_GUIRO_SHORT,     "guiro_short"     );
		set( PERCUSSION_GUIRO_LONG,      "guiro_long"      );
		set( PERCUSSION_CLAVE,           "clave"           );
		set( PERCUSSION_WOOD_BLOCK_HIGH, "wood_block_high" );
		set( PERCUSSION_WOOD_BLOCK_LOW,  "wood_block_low"  );
		set( PERCUSSION_CUICA_MUTE,      "cuica_mute"      );
		set( PERCUSSION_CUICA_OPEN,      "cuica_open"      );
		set( PERCUSSION_TRIANGLE_MUTE,   "triangle_mute"   );
		set( PERCUSSION_TRIANGLE_OPEN,   "triangle_open"   );
		set( PERCUSSION_SHAKER,          "shaker"          );
		set( PERCUSSION_JINGLE_BELL,     "jingle_bell"     );
		set( PERCUSSION_BELLTREE,        "belltree"        );
		set( PERCUSSION_CASTANETS,       "castanets"       );
		set( PERCUSSION_SURDO_MUTE,      "surdo_mute"      );
		set( PERCUSSION_SURDO_OPEN,      "surdo_open"      );
		
		// percussion instruments (short)
		set( PERCUSSION_SHORT_HIGH_Q,          "hq"  );
		set( PERCUSSION_SHORT_SLAP,            "sl"  );
		set( PERCUSSION_SHORT_SCRATCH_PUSH,    "sph" );
		set( PERCUSSION_SHORT_SCRATCH_PULL,    "spl" );
		set( PERCUSSION_SHORT_STICKS,          "st"  );
		set( PERCUSSION_SHORT_SQUARE_CLICK,    "sqc" );
		set( PERCUSSION_SHORT_METRONOME_CLICK, "mc"  );
		set( PERCUSSION_SHORT_METRONOME_BELL,  "mb"  );
		set( PERCUSSION_SHORT_BASS_DRUM_2,     "bd2" );
		set( PERCUSSION_SHORT_BASS_DRUM_1,     "bd1" );
		set( PERCUSSION_SHORT_RIM_SHOT,        "rs"  );
		set( PERCUSSION_SHORT_SNARE_DRUM_1,    "sd1" );
		set( PERCUSSION_SHORT_HAND_CLAP,       "hc"  );
		set( PERCUSSION_SHORT_SNARE_DRUM_2,    "sd2" );
		set( PERCUSSION_SHORT_TOM_1,           "t1"  );
		set( PERCUSSION_SHORT_HI_HAT_CLOSED,   "hhc" );
		set( PERCUSSION_SHORT_TOM_2,           "t2"  );
		set( PERCUSSION_SHORT_HI_HAT_PEDAL,    "hhp" );
		set( PERCUSSION_SHORT_TOM_3,           "t3"  );
		set( PERCUSSION_SHORT_HI_HAT_OPEN,     "hho" );
		set( PERCUSSION_SHORT_TOM_4,           "t4"  );
		set( PERCUSSION_SHORT_TOM_5,           "t5"  );
		set( PERCUSSION_SHORT_CRASH_CYMBAL_1,  "cc1" );
		set( PERCUSSION_SHORT_TOM_6,           "t6"  );
		set( PERCUSSION_SHORT_RIDE_CYMBAL_1,   "rc1" );
		set( PERCUSSION_SHORT_CHINESE_CYMBAL,  "chc" );
		set( PERCUSSION_SHORT_RIDE_BELL,       "rb"  );
		set( PERCUSSION_SHORT_TAMBOURINE,      "ta"  );
		set( PERCUSSION_SHORT_SPLASH_CYMBAL,   "sc"  );
		set( PERCUSSION_SHORT_COWBELL,         "cwb" );
		set( PERCUSSION_SHORT_CRASH_CYMBAL_2,  "cc2" );
		set( PERCUSSION_SHORT_VIBRA_SLAP,      "vs"  );
		set( PERCUSSION_SHORT_RIDE_CYMBAL_2,   "rc2" );
		set( PERCUSSION_SHORT_BONGO_HIGH,      "bh"  );
		set( PERCUSSION_SHORT_BONGO_LOW,       "bl"  );
		set( PERCUSSION_SHORT_CONGA_MUTE,      "cm"  );
		set( PERCUSSION_SHORT_CONGA_OPEN,      "co"  );
		set( PERCUSSION_SHORT_CONGA_LOW,       "cl"  );
		set( PERCUSSION_SHORT_TIMBALES_HIGH,   "th"  );
		set( PERCUSSION_SHORT_TIMBALES_LOW,    "tl"  );
		set( PERCUSSION_SHORT_AGOGO_HIGH,      "ah"  );
		set( PERCUSSION_SHORT_AGOGO_LOW,       "al"  );
		set( PERCUSSION_SHORT_CABASA,          "cab" );
		set( PERCUSSION_SHORT_MARACAS,         "ma"  );
		set( PERCUSSION_SHORT_WHISTLE_SHORT,   "ws"  );
		set( PERCUSSION_SHORT_WHISTLE_LONG,    "wl"  );
		set( PERCUSSION_SHORT_GUIRO_SHORT,     "gs"  );
		set( PERCUSSION_SHORT_GUIRO_LONG,      "gl"  );
		set( PERCUSSION_SHORT_CLAVE,           "cla" );
		set( PERCUSSION_SHORT_WOOD_BLOCK_HIGH, "wbh" );
		set( PERCUSSION_SHORT_WOOD_BLOCK_LOW,  "wbl" );
		set( PERCUSSION_SHORT_CUICA_MUTE,      "cum" );
		set( PERCUSSION_SHORT_CUICA_OPEN,      "cuo" );
		set( PERCUSSION_SHORT_TRIANGLE_MUTE,   "tm"  );
		set( PERCUSSION_SHORT_TRIANGLE_OPEN,   "to"  );
		set( PERCUSSION_SHORT_SHAKER,          "sh"  );
		set( PERCUSSION_SHORT_JINGLE_BELL,     "jb"  );
		set( PERCUSSION_SHORT_BELLTREE,        "bt"  );
		set( PERCUSSION_SHORT_CASTANETS,       "cas" );
		set( PERCUSSION_SHORT_SURDO_MUTE,      "sm"  );
		set( PERCUSSION_SHORT_SURDO_OPEN,      "so"  );
	}
	
	/**
	 * Sets up german percussion IDs.
	 */
	private static void initPercussionGerman() {
		// percussion instruments (long)
		set( PERCUSSION_HIGH_Q,          "high_q"              );
		set( PERCUSSION_SLAP,            "schlag"              );
		set( PERCUSSION_SCRATCH_PUSH,    "scratch_push"        );
		set( PERCUSSION_SCRATCH_PULL,    "scratch_pull"        );
		set( PERCUSSION_STICKS,          "stöcke"              );
		set( PERCUSSION_SQUARE_CLICK,    "square_klick"        );
		set( PERCUSSION_METRONOME_CLICK, "metronom_klick"      );
		set( PERCUSSION_METRONOME_BELL,  "metronom_glocke"     );
		set( PERCUSSION_BASS_DRUM_2,     "basstrommel_2"       );
		set( PERCUSSION_BASS_DRUM_1,     "basstrommel_1"       );
		set( PERCUSSION_RIM_SHOT,        "rimshot"             );
		set( PERCUSSION_SNARE_DRUM_1,    "kleine_trommel_1"    );
		set( PERCUSSION_HAND_CLAP,       "klatschen"           );
		set( PERCUSSION_SNARE_DRUM_2,    "kleine_trommel_2"    );
		set( PERCUSSION_TOM_1,           "tomtom_1"            );
		set( PERCUSSION_HI_HAT_CLOSED,   "hi_hat_geschlossen"  );
		set( PERCUSSION_TOM_2,           "tomtom_2"            );
		set( PERCUSSION_HI_HAT_PEDAL,    "hi_hat_pedal"        );
		set( PERCUSSION_TOM_3,           "tomtom_3"            );
		set( PERCUSSION_HI_HAT_OPEN,     "hi_hat_offen"        );
		set( PERCUSSION_TOM_4,           "tomtom_4"            );
		set( PERCUSSION_TOM_5,           "tomtom_5"            );
		set( PERCUSSION_CRASH_CYMBAL_1,  "crash_becken_1"      );
		set( PERCUSSION_TOM_6,           "tomtom_6"            );
		set( PERCUSSION_RIDE_CYMBAL_1,   "ride_becken_1"       );
		set( PERCUSSION_CHINESE_CYMBAL,  "chinesisches_becken" );
		set( PERCUSSION_RIDE_BELL,       "ride_bell"           );
		set( PERCUSSION_TAMBOURINE,      "tamburin"            );
		set( PERCUSSION_SPLASH_CYMBAL,   "splash_becken"       );
		set( PERCUSSION_COWBELL,         "kuhglocke"           );
		set( PERCUSSION_CRASH_CYMBAL_2,  "crash_becken_2"      );
		set( PERCUSSION_VIBRA_SLAP,      "vibraslap"           );
		set( PERCUSSION_RIDE_CYMBAL_2,   "ride_becken_2"       );
		set( PERCUSSION_BONGO_HIGH,      "bongo_hoch"          );
		set( PERCUSSION_BONGO_LOW,       "bongo_tief"          );
		set( PERCUSSION_CONGA_MUTE,      "conga_gedämpft"      );
		set( PERCUSSION_CONGA_OPEN,      "conga_offen"         );
		set( PERCUSSION_CONGA_LOW,       "conga_tief"          );
		set( PERCUSSION_TIMBALES_HIGH,   "timbales_hoch"       );
		set( PERCUSSION_TIMBALES_LOW,    "timbales_tief"       );
		set( PERCUSSION_AGOGO_HIGH,      "agogo_hoch"          );
		set( PERCUSSION_AGOGO_LOW,       "agogo_tief"          );
		set( PERCUSSION_CABASA,          "cabasa"              );
		set( PERCUSSION_MARACAS,         "maracas"             );
		set( PERCUSSION_WHISTLE_SHORT,   "pfeife_kurz"         );
		set( PERCUSSION_WHISTLE_LONG,    "pfeife_lang"         );
		set( PERCUSSION_GUIRO_SHORT,     "guiro_kurz"          );
		set( PERCUSSION_GUIRO_LONG,      "guiro_lang"          );
		set( PERCUSSION_CLAVE,           "clave"               );
		set( PERCUSSION_WOOD_BLOCK_HIGH, "holzblock_hoch"      );
		set( PERCUSSION_WOOD_BLOCK_LOW,  "holzblock_tief"      );
		set( PERCUSSION_CUICA_MUTE,      "cuica_gedämpft"      );
		set( PERCUSSION_CUICA_OPEN,      "cuica_offen"         );
		set( PERCUSSION_TRIANGLE_MUTE,   "triangel_gedämpft"   );
		set( PERCUSSION_TRIANGLE_OPEN,   "triangel_offen"      );
		set( PERCUSSION_SHAKER,          "schüttler"           );
		set( PERCUSSION_JINGLE_BELL,     "schelle"             );
		set( PERCUSSION_BELLTREE,        "glockenbaum"         );
		set( PERCUSSION_CASTANETS,       "kastagnetten"        );
		set( PERCUSSION_SURDO_MUTE,      "surdo_gedämpft"      );
		set( PERCUSSION_SURDO_OPEN,      "surdo_offen"         );
		
		// percussion instruments (short)
		set( PERCUSSION_SHORT_HIGH_Q,          "hq"  );
		set( PERCUSSION_SHORT_SLAP,            "sg"  );
		set( PERCUSSION_SHORT_SCRATCH_PUSH,    "sph" );
		set( PERCUSSION_SHORT_SCRATCH_PULL,    "spl" );
		set( PERCUSSION_SHORT_STICKS,          "st"  );
		set( PERCUSSION_SHORT_SQUARE_CLICK,    "sk"  );
		set( PERCUSSION_SHORT_METRONOME_CLICK, "mk"  );
		set( PERCUSSION_SHORT_METRONOME_BELL,  "mg"  );
		set( PERCUSSION_SHORT_BASS_DRUM_2,     "bt2" );
		set( PERCUSSION_SHORT_BASS_DRUM_1,     "bt1" );
		set( PERCUSSION_SHORT_RIM_SHOT,        "rs"  );
		set( PERCUSSION_SHORT_SNARE_DRUM_1,    "kt1" );
		set( PERCUSSION_SHORT_HAND_CLAP,       "kl"  );
		set( PERCUSSION_SHORT_SNARE_DRUM_2,    "kt2" );
		set( PERCUSSION_SHORT_TOM_1,           "t1"  );
		set( PERCUSSION_SHORT_HI_HAT_CLOSED,   "hhg" );
		set( PERCUSSION_SHORT_TOM_2,           "t2"  );
		set( PERCUSSION_SHORT_HI_HAT_PEDAL,    "hhp" );
		set( PERCUSSION_SHORT_TOM_3,           "t3"  );
		set( PERCUSSION_SHORT_HI_HAT_OPEN,     "hho" );
		set( PERCUSSION_SHORT_TOM_4,           "t4"  );
		set( PERCUSSION_SHORT_TOM_5,           "t5"  );
		set( PERCUSSION_SHORT_CRASH_CYMBAL_1,  "cb1" );
		set( PERCUSSION_SHORT_TOM_6,           "t6"  );
		set( PERCUSSION_SHORT_RIDE_CYMBAL_1,   "rb1" );
		set( PERCUSSION_SHORT_CHINESE_CYMBAL,  "cbk" );
		set( PERCUSSION_SHORT_RIDE_BELL,       "rb"  );
		set( PERCUSSION_SHORT_TAMBOURINE,      "ta"  );
		set( PERCUSSION_SHORT_SPLASH_CYMBAL,   "spk" );
		set( PERCUSSION_SHORT_COWBELL,         "kg"  );
		set( PERCUSSION_SHORT_CRASH_CYMBAL_2,  "cb2" );
		set( PERCUSSION_SHORT_VIBRA_SLAP,      "vs"  );
		set( PERCUSSION_SHORT_RIDE_CYMBAL_2,   "rb2" );
		set( PERCUSSION_SHORT_BONGO_HIGH,      "bh"  );
		set( PERCUSSION_SHORT_BONGO_LOW,       "bt"  );
		set( PERCUSSION_SHORT_CONGA_MUTE,      "cg"  );
		set( PERCUSSION_SHORT_CONGA_OPEN,      "co"  );
		set( PERCUSSION_SHORT_CONGA_LOW,       "ct"  );
		set( PERCUSSION_SHORT_TIMBALES_HIGH,   "th"  );
		set( PERCUSSION_SHORT_TIMBALES_LOW,    "tt"  );
		set( PERCUSSION_SHORT_AGOGO_HIGH,      "ah"  );
		set( PERCUSSION_SHORT_AGOGO_LOW,       "at"  );
		set( PERCUSSION_SHORT_CABASA,          "ca"  );
		set( PERCUSSION_SHORT_MARACAS,         "ma"  );
		set( PERCUSSION_SHORT_WHISTLE_SHORT,   "pk"  );
		set( PERCUSSION_SHORT_WHISTLE_LONG,    "pl"  );
		set( PERCUSSION_SHORT_GUIRO_SHORT,     "gk"  );
		set( PERCUSSION_SHORT_GUIRO_LONG,      "gl"  );
		set( PERCUSSION_SHORT_CLAVE,           "cl"  );
		set( PERCUSSION_SHORT_WOOD_BLOCK_HIGH, "hbh" );
		set( PERCUSSION_SHORT_WOOD_BLOCK_LOW,  "hbt" );
		set( PERCUSSION_SHORT_CUICA_MUTE,      "cug" );
		set( PERCUSSION_SHORT_CUICA_OPEN,      "cuo" );
		set( PERCUSSION_SHORT_TRIANGLE_MUTE,   "tg"  );
		set( PERCUSSION_SHORT_TRIANGLE_OPEN,   "to"  );
		set( PERCUSSION_SHORT_SHAKER,          "sr"  );
		set( PERCUSSION_SHORT_JINGLE_BELL,     "sc"  );
		set( PERCUSSION_SHORT_BELLTREE,        "glb" );
		set( PERCUSSION_SHORT_CASTANETS,       "ka"  );
		set( PERCUSSION_SHORT_SURDO_MUTE,      "sug" );
		set( PERCUSSION_SHORT_SURDO_OPEN,      "suo" );
	}
	
	/**
	 * Sets one specific syntax keyword.
	 * This is called indirectly by {@link #initSyntax()} while initializing the
	 * currently configured MidicaPL syntax.
	 * 
	 * @param id        identifier defining the meaning of the keyword to specify
	 * @param keyword   the keyword to be configured
	 */
	private static void setSyntax(String id, String keyword) {
		syntax.put(id, keyword);
	}
	
	/**
	 * Creates a plain syntax element and adds it to the internal data structure of
	 * syntax elements.
	 * This is used later in order to display the syntax categories and elements
	 * in the info view of the GUI.
	 * 
	 * @param id identifier of the syntax element to add
	 */
	private static void addSyntaxForInfoView(String id) {
		String description = get(id);
		String keyword     = getSyntax(id);
		SyntaxElement elem = new SyntaxElement(id, description, keyword, false);
		syntaxList.add(elem);
	}
	
	/**
	 * Creates a syntax category and adds it to the internal data structure of
	 * syntax elements.
	 * This is used later in order to display the syntax categories and elements
	 * in the info view of the GUI.
	 * 
	 * @param categoryName name of the syntax category
	 */
	private static void addSyntaxCategory(String categoryName) {
		SyntaxElement elem = new SyntaxElement(categoryName, "", "", true);
		syntaxList.add(elem);
	}
	
	/**
	 * Creates english translations between note values and the corresponding
	 * instrument IDs.
	 */
	private static void initInstrumentsEnglish1() {
		// piano
		addInstrCategory(INSTR_CAT_PIANO);
		setInstrument( 0,   "ACOUSTIC_GRAND_PIANO"  );
		setInstrument( 1,   "BRIGHT_ACOUSTIC_PIANO" );
		setInstrument( 2,   "ELECTRIC_GRAND_PIANO"  );
		setInstrument( 3,   "HONKY_TONK_PIANO"      );
		setInstrument( 4,   "ELECTRIC_PIANO_1"      );
		setInstrument( 5,   "ELECTRIC_PIANO_2"      );
		setInstrument( 6,   "HARPSICHORD"           );
		setInstrument( 7,   "CLAVINET"              );
		
		// chromatic percussion
		addInstrCategory(INSTR_CAT_CHROM_PERC);
		setInstrument( 8,   "CELESTA"               );
		setInstrument( 9,   "GLOCKENSPIEL"          );
		setInstrument( 10,  "MUSIC_BOX"             );
		setInstrument( 11,  "VIBRAPHONE"            );
		setInstrument( 12,  "MARIMBA"               );
		setInstrument( 13,  "XYLOPHONE"             );
		setInstrument( 14,  "TUBULAR_BELL"          );
		setInstrument( 15,  "DULCIMER"              );
		
		// organ
		addInstrCategory(INSTR_CAT_ORGAN);
		setInstrument( 16,  "DRAWBAR_ORGAN"         );
		setInstrument( 17,  "PERCUSSIVE_ORGAN"      );
		setInstrument( 18,  "ROCK_ORGAN"            );
		setInstrument( 19,  "CHURCH_ORGAN"          );
		setInstrument( 20,  "REED_ORGAN"            );
		setInstrument( 21,  "ACCORDION"             );
		setInstrument( 22,  "HARMONICA"             );
		setInstrument( 23,  "TANGO_ACCORDION"       );
		
		// guitar
		addInstrCategory(INSTR_CAT_GUITAR);
		setInstrument( 24,  "NYLON_GUITAR"          );
		setInstrument( 25,  "STEEL_GUITAR"          );
		setInstrument( 26,  "E_GUITAR_JAZZ"         );
		setInstrument( 27,  "E_GUITAR_CLEAN"        );
		setInstrument( 28,  "E_GUITAR_MUTED"        );
		setInstrument( 29,  "OVERDRIVEN_GUITAR"     );
		setInstrument( 30,  "DISTORTION_GUITAR"     );
		setInstrument( 31,  "GUITAR_HARMONICS"      );
		
		// bass
		addInstrCategory(INSTR_CAT_BASS);
		setInstrument( 32,  "ACOUSTIC_BASS"         );
		setInstrument( 33,  "E_BASS_FINGER"         );
		setInstrument( 34,  "E_BASS_PICK"           );
		setInstrument( 35,  "FRETLESS_BASS"         );
		setInstrument( 36,  "SLAP_BASS_1"           );
		setInstrument( 37,  "SLAP_BASS_2"           );
		setInstrument( 38,  "SYNTH_BASS_1"          );
		setInstrument( 39,  "SYNTH_BASS_2"          );
		
		// strings
		addInstrCategory(INSTR_CAT_STRINGS);
		setInstrument( 40,  "VIOLIN"                );
		setInstrument( 41,  "VIOLA"                 );
		setInstrument( 42,  "CELLO"                 );
		setInstrument( 43,  "CONTRABASS"            );
		setInstrument( 44,  "TREMOLO_STRINGS"       );
		setInstrument( 45,  "PIZZICATO_STRINGS"     );
		setInstrument( 46,  "ORCHESTRAL_HARP"       );
		setInstrument( 47,  "TIMPANI"               );
		
		// ensemble
		addInstrCategory(INSTR_CAT_ENSEMBLE);
		setInstrument( 48,  "STRING_ENSEMBLE_1"     );
		setInstrument( 49,  "STRING_ENSEMBLE_2"     );
		setInstrument( 50,  "SYNTH_STRINGS_1"       );
		setInstrument( 51,  "SYNTH_STRINGS_2"       );
		setInstrument( 52,  "CHOIR_AAHS"            );
		setInstrument( 53,  "VOICE_OOHS"            );
		setInstrument( 54,  "SYNTH_CHOIR"           );
		setInstrument( 55,  "ORCHESTRA_HIT"         );
		
		// brass
		addInstrCategory(INSTR_CAT_BRASS);
		setInstrument( 56,  "TRUMPET"               );
		setInstrument( 57,  "TROMBONE"              );
		setInstrument( 58,  "TUBA"                  );
		setInstrument( 59,  "MUTED_TRUMPET"         );
		setInstrument( 60,  "FRENCH_HORN"           );
		setInstrument( 61,  "BRASS_SECTION"         );
		setInstrument( 62,  "SYNTH_BRASS_1"         );
		setInstrument( 63,  "SYNTH_BRASS_2"         );
		
		// reed
		addInstrCategory(INSTR_CAT_REED);
		setInstrument( 64,  "SOPRANO_SAX"           );
		setInstrument( 65,  "ALTO_SAX"              );
		setInstrument( 66,  "TENOR_SAX"             );
		setInstrument( 67,  "BARITONE_SAX"          );
		setInstrument( 68,  "OBOE"                  );
		setInstrument( 69,  "ENGLISH_HORN"          );
		setInstrument( 70,  "BASSOON"               );
		setInstrument( 71,  "CLARINET"              );
		
		// pipe
		addInstrCategory(INSTR_CAT_PIPE);
		setInstrument( 72,  "PICCOLO"               );
		setInstrument( 73,  "FLUTE"                 );
		setInstrument( 74,  "RECORDER"              );
		setInstrument( 75,  "PAN_FLUTE"             );
		setInstrument( 76,  "BLOWN_BOTTLE"          );
		setInstrument( 77,  "SHAKUHACHI"            );
		setInstrument( 78,  "WHISTLE"               );
		setInstrument( 79,  "OCARINA"               );
		
		// synth lead
		addInstrCategory(INSTR_CAT_SYNTH_LEAD);
		setInstrument( 80,  "LEAD_SQUARE"          );
		setInstrument( 81,  "LEAD_SAWTOOTH"        );
		setInstrument( 82,  "LEAD_CALLIOPE"        );
		setInstrument( 83,  "LEAD_CHIFF"           );
		setInstrument( 84,  "LEAD_CHARANGO"        );
		setInstrument( 85,  "LEAD_VOICE"           );
		setInstrument( 86,  "LEAD_FIFTHS"          );
		setInstrument( 87,  "LEAD_BASS_LEAD"       );
		
		// synth pad
		addInstrCategory(INSTR_CAT_SYNTH_PAD);
		setInstrument( 88,  "PAD_NEW_AGE"           );
		setInstrument( 89,  "PAD_WARM"              );
		setInstrument( 90,  "PAD_POLYSYNTH"         );
		setInstrument( 91,  "PAD_CHOIR"             );
		setInstrument( 92,  "PAD_POWED"             );
		setInstrument( 93,  "PAD_METALLIC"          );
		setInstrument( 94,  "PAD_HALO"              );
		setInstrument( 95,  "PAD_SWEEP"             );
		
		// synth effects
		addInstrCategory(INSTR_CAT_SYNTH_EFFECTS);
		setInstrument( 96,  "FX_RAIN"               );
		setInstrument( 97,  "FX_SOUNDTRACK"         );
		setInstrument( 98,  "FX_CRYSTAL"            );
		setInstrument( 99,  "FX_ATMOSPHERE"         );
		setInstrument( 100, "FX_BRIGHTNESS"         );
		setInstrument( 101, "FX_GOBLINS"            );
		setInstrument( 102, "FX_ECHOES"             );
		setInstrument( 103, "FX_SCI_FI"             );
		
		// ethnic
		addInstrCategory(INSTR_CAT_ETHNIC);
		setInstrument( 104, "SITAR"                 );
		setInstrument( 105, "BANJO"                 );
		setInstrument( 106, "SHAMISEN"              );
		setInstrument( 107, "KOTO"                  );
		setInstrument( 108, "KALIMBA"               );
		setInstrument( 109, "BAG_PIPE"              );
		setInstrument( 110, "FIDDLE"                );
		setInstrument( 111, "SHANAI"                );
		
		// percussive
		addInstrCategory(INSTR_CAT_PERCUSSIVE);
		setInstrument( 112, "TINKLE_BELL"           );
		setInstrument( 113, "AGOGO"                 );
		setInstrument( 114, "STEEL_DRUMS"           );
		setInstrument( 115, "WOODBLOCK"             );
		setInstrument( 116, "TAIKO_DRUM"            );
		setInstrument( 117, "MELODIC_TOM"           );
		setInstrument( 118, "SYNTH_DRUM"            );
		setInstrument( 119, "REVERSE_CYMBAL"        );
		
		// sound effects
		addInstrCategory(INSTR_CAT_SOUND_EFFECTS);
		setInstrument( 120, "GUITAR_FRET_NOISE"     );
		setInstrument( 121, "BREATH_NOISE"          );
		setInstrument( 122, "SEASHORE"              );
		setInstrument( 123, "BIRD_TWEET"            );
		setInstrument( 124, "TELEPHONE_RING"        );
		setInstrument( 125, "HELICOPTER"            );
		setInstrument( 126, "APPLAUSE"              );
		setInstrument( 127, "GUNSHOT"               );
		
		// drumkits
		setDrumkit(   0, "STANDARD"   );
		setDrumkit(   8, "ROOM"       );
		setDrumkit(  16, "POWER"      );
		setDrumkit(  24, "ELECTRONIC" );
		setDrumkit(  25, "TR808"      );
		setDrumkit(  32, "JAZZ"       );
		setDrumkit(  40, "BRUSH"      );
		setDrumkit(  48, "ORCHESTRA"  );
		setDrumkit(  56, "SOUND_FX"   );
		setDrumkit( 127, "CM64_CM32"  );
	}
	
	/**
	 * Creates german translations between note values and the corresponding
	 * instrument IDs.
	 */
	private static void initInstrumentsGerman1() {
		// TODO: translate
		// piano
		addInstrCategory(INSTR_CAT_PIANO);
		setInstrument( 0,   "ACOUSTIC_GRAND_PIANO"  );
		setInstrument( 1,   "BRIGHT_ACOUSTIC_PIANO" );
		setInstrument( 2,   "ELECTRIC_GRAND_PIANO"  );
		setInstrument( 3,   "HONKY_TONK_PIANO"      );
		setInstrument( 4,   "ELECTRIC_PIANO_1"      );
		setInstrument( 5,   "ELECTRIC_PIANO_2"      );
		setInstrument( 6,   "HARPSICHORD"           );
		setInstrument( 7,   "CLAVINET"              );
		
		// chromatic percussion
		addInstrCategory(INSTR_CAT_CHROM_PERC);
		setInstrument( 8,   "CELESTA"               );
		setInstrument( 9,   "GLOCKENSPIEL"          );
		setInstrument( 10,  "MUSIC_BOX"             );
		setInstrument( 11,  "VIBRAPHONE"            );
		setInstrument( 12,  "MARIMBA"               );
		setInstrument( 13,  "XYLOPHONE"             );
		setInstrument( 14,  "TUBULAR_BELL"          );
		setInstrument( 15,  "DULCIMER"              );
		
		// organ
		addInstrCategory(INSTR_CAT_ORGAN);
		setInstrument( 16,  "DRAWBAR_ORGAN"         );
		setInstrument( 17,  "PERCUSSIVE_ORGAN"      );
		setInstrument( 18,  "ROCK_ORGAN"            );
		setInstrument( 19,  "CHURCH_ORGAN"          );
		setInstrument( 20,  "REED_ORGAN"            );
		setInstrument( 21,  "ACCORDION"             );
		setInstrument( 22,  "HARMONICA"             );
		setInstrument( 23,  "TANGO_ACCORDION"       );
		
		// guitar
		addInstrCategory(INSTR_CAT_GUITAR);
		setInstrument( 24,  "NYLON_GUITAR"          );
		setInstrument( 25,  "STEEL_GUITAR"          );
		setInstrument( 26,  "E_GUITAR_JAZZ"         );
		setInstrument( 27,  "E_GUITAR_CLEAN"        );
		setInstrument( 28,  "E_GUITAR_MUTED"        );
		setInstrument( 29,  "OVERDRIVEN_GUITAR"     );
		setInstrument( 30,  "DISTORTION_GUITAR"     );
		setInstrument( 31,  "GUITAR_HARMONICS"      );
		
		// bass
		addInstrCategory(INSTR_CAT_BASS);
		setInstrument( 32,  "ACOUSTIC_BASS"         );
		setInstrument( 33,  "E_BASS_FINGER"         );
		setInstrument( 34,  "E_BASS_PICK"           );
		setInstrument( 35,  "FRETLESS_BASS"         );
		setInstrument( 36,  "SLAP_BASS_1"           );
		setInstrument( 37,  "SLAP_BASS_2"           );
		setInstrument( 38,  "SYNTH_BASS_1"          );
		setInstrument( 39,  "SYNTH_BASS_2"          );
		
		// strings
		addInstrCategory(INSTR_CAT_STRINGS);
		setInstrument( 40,  "VIOLIN"                );
		setInstrument( 41,  "VIOLA"                 );
		setInstrument( 42,  "CELLO"                 );
		setInstrument( 43,  "CONTRABASS"            );
		setInstrument( 44,  "TREMOLO_STRINGS"       );
		setInstrument( 45,  "PIZZICATO_STRINGS"     );
		setInstrument( 46,  "ORCHESTRAL_HARP"       );
		setInstrument( 47,  "TIMPANI"               );
		
		// ensemble
		addInstrCategory(INSTR_CAT_ENSEMBLE);
		setInstrument( 48,  "STRING_ENSEMBLE_1"     );
		setInstrument( 49,  "STRING_ENSEMBLE_2"     );
		setInstrument( 50,  "SYNTH_STRINGS_1"       );
		setInstrument( 51,  "SYNTH_STRINGS_2"       );
		setInstrument( 52,  "CHOIR_AAHS"            );
		setInstrument( 53,  "VOICE_OOHS"            );
		setInstrument( 54,  "SYNTH_CHOIR"           );
		setInstrument( 55,  "ORCHESTRA_HIT"         );
		
		// brass
		addInstrCategory(INSTR_CAT_BRASS);
		setInstrument( 56,  "TRUMPET"               );
		setInstrument( 57,  "TROMBONE"              );
		setInstrument( 58,  "TUBA"                  );
		setInstrument( 59,  "MUTED_TRUMPET"         );
		setInstrument( 60,  "FRENCH_HORN"           );
		setInstrument( 61,  "BRASS_SECTION"         );
		setInstrument( 62,  "SYNTH_BRASS_1"         );
		setInstrument( 63,  "SYNTH_BRASS_2"         );
		
		// reed
		addInstrCategory(INSTR_CAT_REED);
		setInstrument( 64,  "SOPRANO_SAX"           );
		setInstrument( 65,  "ALTO_SAX"              );
		setInstrument( 66,  "TENOR_SAX"             );
		setInstrument( 67,  "BARITONE_SAX"          );
		setInstrument( 68,  "OBOE"                  );
		setInstrument( 69,  "ENGLISH_HORN"          );
		setInstrument( 70,  "BASSOON"               );
		setInstrument( 71,  "CLARINET"              );
		
		// pipe
		addInstrCategory(INSTR_CAT_PIPE);
		setInstrument( 72,  "PICCOLO"               );
		setInstrument( 73,  "FLUTE"                 );
		setInstrument( 74,  "RECORDER"              );
		setInstrument( 75,  "PAN_FLUTE"             );
		setInstrument( 76,  "BLOWN_BOTTLE"          );
		setInstrument( 77,  "SHAKUHACHI"            );
		setInstrument( 78,  "WHISTLE"               );
		setInstrument( 79,  "OCARINA"               );
		
		// synth lead
		addInstrCategory(INSTR_CAT_SYNTH_LEAD);
		setInstrument( 80,  "LEAD_SQUARE"          );
		setInstrument( 81,  "LEAD_SAWTOOTH"        );
		setInstrument( 82,  "LEAD_CALLIOPE"        );
		setInstrument( 83,  "LEAD_CHIFF"           );
		setInstrument( 84,  "LEAD_CHARANGO"        );
		setInstrument( 85,  "LEAD_VOICE"           );
		setInstrument( 86,  "LEAD_FIFTHS"          );
		setInstrument( 87,  "LEAD_BASS_LEAD"       );
		
		// synth pad
		addInstrCategory(INSTR_CAT_SYNTH_PAD);
		setInstrument( 88,  "PAD_NEW_AGE"           );
		setInstrument( 89,  "PAD_WARM"              );
		setInstrument( 90,  "PAD_POLYSYNTH"         );
		setInstrument( 91,  "PAD_CHOIR"             );
		setInstrument( 92,  "PAD_POWED"             );
		setInstrument( 93,  "PAD_METALLIC"          );
		setInstrument( 94,  "PAD_HALO"              );
		setInstrument( 95,  "PAD_SWEEP"             );
		
		// synth effects
		addInstrCategory(INSTR_CAT_SYNTH_EFFECTS);
		setInstrument( 96,  "FX_RAIN"               );
		setInstrument( 97,  "FX_SOUNDTRACK"         );
		setInstrument( 98,  "FX_CRYSTAL"            );
		setInstrument( 99,  "FX_ATMOSPHERE"         );
		setInstrument( 100, "FX_BRIGHTNESS"         );
		setInstrument( 101, "FX_GOBLINS"            );
		setInstrument( 102, "FX_ECHOES"             );
		setInstrument( 103, "FX_SCI_FI"             );
		
		// ethnic
		addInstrCategory(INSTR_CAT_ETHNIC);
		setInstrument( 104, "SITAR"                 );
		setInstrument( 105, "BANJO"                 );
		setInstrument( 106, "SHAMISEN"              );
		setInstrument( 107, "KOTO"                  );
		setInstrument( 108, "KALIMBA"               );
		setInstrument( 109, "BAG_PIPE"              );
		setInstrument( 110, "FIDDLE"                );
		setInstrument( 111, "SHANAI"                );
		
		// percussive
		addInstrCategory(INSTR_CAT_PERCUSSIVE);
		setInstrument( 112, "TINKLE_BELL"           );
		setInstrument( 113, "AGOGO"                 );
		setInstrument( 114, "STEEL_DRUMS"           );
		setInstrument( 115, "WOODBLOCK"             );
		setInstrument( 116, "TAIKO_DRUM"            );
		setInstrument( 117, "MELODIC_TOM"           );
		setInstrument( 118, "SYNTH_DRUM"            );
		setInstrument( 119, "REVERSE_CYMBAL"        );
		
		// sound effects
		addInstrCategory(INSTR_CAT_SOUND_EFFECTS);
		setInstrument( 120, "GUITAR_FRET_NOISE"     );
		setInstrument( 121, "BREATH_NOISE"          );
		setInstrument( 122, "SEASHORE"              );
		setInstrument( 123, "BIRD_TWEET"            );
		setInstrument( 124, "TELEPHONE_RING"        );
		setInstrument( 125, "HELICOPTER"            );
		setInstrument( 126, "APPLAUSE"              );
		setInstrument( 127, "GUNSHOT"               );
		
		// drumkits
		setDrumkit(   0, "STANDARD"      );
		setDrumkit(   8, "RAUM"          );
		setDrumkit(  16, "POWER"         );
		setDrumkit(  24, "ELEKTRO"       );
		setDrumkit(  25, "TR808"         );
		setDrumkit(  32, "JAZZ"          );
		setDrumkit(  40, "BÜRSTE"        );
		setDrumkit(  48, "ORCHESTER"     );
		setDrumkit(  56, "SOUND_EFFEKTE" );
		setDrumkit( 127, "CM64_CM32"     );
	}
	
	/**
	 * Sets the translation for one specific instrument.
	 * Also makes the instrument name available for the info view and for the
	 * soundcheck.
	 * 
	 * @param number  instrument number as it is defined by MIDI
	 * @param name    name of the instrument as it is used by the Midica user
	 */
	private static void setInstrument(int number, String name) {
		instrIntToName.put(number, name);
		instrNameToInt.put(name, number);
		
		// prepare for the info view
		InstrumentElement elem = new InstrumentElement(number, name, false);
		instrumentList.add(elem);
	}
	
	/**
	 * Sets the translation for one specific drumkit.
	 * Also makes the drumkit name available for the info view.
	 * 
	 * @param number  drumkit program number as it is defined by MIDI
	 * @param name    name of the drumkit as it is used by the Midica user
	 */
	private static void setDrumkit(int number, String name) {
		drumkitIntToName.put(number, name);
		drumkitNameToInt.put(name, number);
		
		// prepare for the info view
		InstrumentElement elem = new InstrumentElement(number, name, false);
		drumkitList.add(elem);
	}
	
	/**
	 * Adds an instrument category to the list of instruments used by
	 * info view and soundcheck.
	 * 
	 * @param categoryId  identifier of the category to be added
	 */
	private static void addInstrCategory(String categoryId) {
		String categoryName = get(categoryId);
		InstrumentElement elem = new InstrumentElement(-1, categoryName, true);
		instrumentList.add(elem);
	}
	
	/**
	 * Creates an association between a translation identifier and the
	 * corresponding language translation.
	 * 
	 * @param id           identifier of the translation
	 * @param translation  translated text corresponding to the identifier
	 */
	private static void set(String id, String translation) {
		dictionary.put(id, translation);
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
	public static String get(String key) {
		if (dictionary.containsKey(key))
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
	public static int getNote(String name) {
		if (noteNameToInt.containsKey(name))
			return noteNameToInt.get(name);
		else
			return UNKNOWN_CODE;
	}
	
	/**
	 * Returns a note name by note value.
	 * 
	 * @param i  value of the requested note like it is defined in the MIDI specification
	 * @return   currently configured name for the requested note
	 */
	public static String getNote(int i) {
		if (noteIntToName.containsKey(i))
			return noteIntToName.get(i);
		else
			return get(UNKNOWN_NOTE_NAME);
	}
	
	/**
	 * Returns a note or percussion name by number.
	 * 
	 * If the percussion name is requested and not found, the number is returned as a string instead.
	 * 
	 * @param number        number
	 * @param isPercussion  **true** for a percussion name, **false** for a note name
	 * @return the name
	 */
	public static String getNoteOrPercussionName(int number, boolean isPercussion) {
		String name = Dict.getNote(number);
		if (isPercussion) {
			name = Dict.getPercussionShortId(number);
			if (name.equals(Dict.get(Dict.UNKNOWN_PERCUSSION_NAME))) {
				name = number + ""; // name unknown - use number instead
			}
		}
		return name;
	}
	
	/**
	 * Returns a note name by note value, preferring sharps or flats instead of normal notes.
	 * 
	 * @param noteNum        value of the requested note like it is defined in the MIDI specification
	 * @param preferSharp    **true** to prefer the "sharp" name, **false** for the "flat" name
	 * @return the requested note name
	 */
	public static String getNoteAsSharpOrFlat(int noteNum, boolean preferSharp) {
		noteNum %= 12;
		
		// not the configured default symbol - check alternative note names
		String symbol = Config.getConfiguredSharpOrFlat(preferSharp);
		Pattern patternSingle = Pattern.compile("^.+" + Pattern.quote(symbol) + ".*");
		Pattern patternMore   = Pattern.compile("^.+" + Pattern.quote(symbol) + "{2,}.*");
		ArrayList<String> noteNames = moreBaseNotesPos.get(noteNum);
		for (String name : noteNames) {
			if (patternSingle.matcher(name).find() && ! patternMore.matcher(name).find()) {
				return name;
			}
		}
		
		// no candidate found
		return notes[noteNum];
	}
	
	/**
	 * Returns a String with alternative note names by note value.
	 * 
	 * @param i  value of the requested note like it is defined in the MIDI specification
	 * @return   currently configured alternative names for the requested note
	 */
	public static String getNoteAlternatives(int i) {
		return String.join(", ", moreNotesByNum.get(i));
	}
	
	/**
	 * Returns the base note name for the given note number.
	 * This name contains half tone steps but no octave information.
	 * 
	 * @param noteNum  The note number.
	 * @return  The note's base name.
	 */
	public static String getBaseNoteName(int noteNum) {
		int noteIndex = noteNum % 12;
		return notes[noteIndex];
	}
	
	/**
	 * Returns an instrument value by name.
	 * 
	 * @param name  currently configured name for the instrument
	 * @return      value for the instrument as defined by the MIDI specification
	 */
	public static int getInstrument(String name) {
		if (instrNameToInt.containsKey(name))
			return instrNameToInt.get(name);
		else
			return UNKNOWN_CODE;
	}
	
	/**
	 * Returns an instrument name by value.
	 * 
	 * @param i  instrument value as defined by the MIDI specification
	 * @return   currently configured name for the requested instrument
	 */
	public static String getInstrument(int i) {
		if (instrIntToName.containsKey(i))
			return instrIntToName.get(i);
		else
			return get(UNKNOWN_INSTRUMENT);
	}
	
	/**
	 * Indicates if the given name is a currently configured note name.
	 * 
	 * @param name  note name to be checked
	 * @return      true, if the note name is configured -- otherwise: false
	 */
	public static boolean noteExists(String name) {
		if (noteNameToInt.containsKey(name))
			return true;
		return false;
	}
	
	/**
	 * Returns a percussion instrument number by percussion ID.
	 * 
	 * @param id  long or short ID of the percussion instrument
	 * @return    instrument value as defined by the MIDI specification
	 */
	public static int getPercussion(String id) {
		if (percussionIdToInt.containsKey(id))
			return percussionIdToInt.get(id);
		else
			return UNKNOWN_CODE;
	}
	
	/**
	 * Returns the long form of a percussion instrument id by MIDI number.
	 * 
	 * @param i  percussion instrument number as defined by the MIDI specification
	 * @return   currently configured percussion ID (long form) for the requested
	 *           percussion instrument
	 */
	public static String getPercussionLongId(int i) {
		if (percussionIntToLongId.containsKey(i))
			return percussionIntToLongId.get(i);
		else
			return get(UNKNOWN_PERCUSSION_NAME);
	}
	
	/**
	 * Returns the short form of a percussion instrument id by MIDI number.
	 * 
	 * @param i  percussion instrument number as defined by the MIDI specification
	 * @return   currently configured percussion ID (short form) for the requested
	 *           percussion instrument
	 */
	public static String getPercussionShortId(int i) {
		if (percussionIntToShortId.containsKey(i))
			return percussionIntToShortId.get(i);
		else
			return get(UNKNOWN_PERCUSSION_NAME);
	}
	
	/**
	 * Returns the longest number of characters that a (short) percussion ID can have.
	 * 
	 * @return longest possible percussion short ID
	 */
	public static int getPercussionShortIdLength() {
		int longest = 0;
		for (int number : percussionIntToShortId.keySet()) {
			String id = percussionIntToShortId.get(number);
			if (id.length() > longest) {
				longest = id.length();
			}
		}
		
		return longest;
	}
	
	/**
	 * Returns a drumkit number by name.
	 * 
	 * @param name  drumkit ID
	 * @return      drumkit number as defined by the General Standard specification
	 */
	public static int getDrumkit(String name) {
		if (drumkitNameToInt.containsKey(name))
			return drumkitNameToInt.get(name);
		else
			return UNKNOWN_CODE;
	}
	
	/**
	 * Returns a drumkit ID by value.
	 * 
	 * @param i  drumkit value as defined by the General Standard specification
	 * @return drumkit ID.
	 */
	public static String getDrumkit(int i) {
		if (drumkitIntToName.containsKey(i))
			return drumkitIntToName.get(i);
		else
			return get(UNKNOWN_DRUMKIT_NAME);
	}
	
	/**
	 * Indicates if the given name is a currently configured percussion ID.
	 * 
	 * @param name  percussion name to be checked
	 * @return      true, if the percussion name is configured -- otherwise: false
	 */
	public static boolean percussionExists(String name) {
		if (percussionIdToInt.containsKey(name))
			return true;
		return false;
	}
	
	/**
	 * Returns a specific MidicaPL keyword or symbol.
	 * 
	 * @param id  identifier for the syntax element
	 * @return    currently configured syntax keyword
	 */
	public static String getSyntax(String id) {
		if (syntax.containsKey(id))
			return syntax.get(id);
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
	 * Counts the currently configured percussion IDs.
	 * 
	 * @return number of currently configured percussion IDs
	 */
	public static int countPercussion() {
		return percussionIntToLongId.size();
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
	 * Returns all currently configured percussion IDs (long form).
	 * 
	 * @return all currently configured (long) percussion IDs
	 */
	public static Set<Integer> getPercussionNotes() {
		return percussionIntToLongId.keySet();
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
	 * Returns all currently configured drumkits.
	 * 
	 * @return all currently configured drumkits
	 */
	public static ArrayList<InstrumentElement> getDrumkitList() {
		return drumkitList;
	}
	
	/**
	 * Returns the list of key binding category identifiers.
	 * 
	 * @return key binding category IDs
	 */
	public static ArrayList<String> getKeyBindingCategories() {
		return keyBindingCategories;
	}
	
	/**
	 * Returns the URL to a foreign program if there is any URL available.
	 * Otherwise, returns **null**.
	 * 
	 * @param id    the configuration ID of the executable command, like defined in {@link Config}.
	 * @return the URL or **null**.
	 */
	public static String getForeignProgramUrl(String id) {
		if (Config.EXEC_PATH_IMP_ALDA.equals(id))
			return "https://github.com/alda-lang/alda";
		if (Config.EXEC_PATH_IMP_ABC.equals(id))
			return "https://ifdo.ca/~seymour/runabc/top.html";
		if (Config.EXEC_PATH_IMP_LY.equals(id))
			return "https://lilypond.org/";
		if (Config.EXEC_PATH_IMP_MSCORE.equals(id))
			return "https://musescore.org/";
		if (Config.EXEC_PATH_EXP_ABC.equals(id))
			return "https://ifdo.ca/~seymour/runabc/top.html";
		if (Config.EXEC_PATH_EXP_LY.equals(id))
			return "https://lilypond.org/";
		if (Config.EXEC_PATH_EXP_MSCORE.equals(id))
			return "https://musescore.org/";
		else
			return null;
	}
}
