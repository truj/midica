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
 * - **PERCUSSION_** - Those keys identify IDs for percussion instruments.
 * - **INSTR_** - Those keys identify instrument IDs.
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
	
	// needed to build up the note dictionaries (noteNameToInt and noteIntToName)
	private static String[] notes      = new String[12];
	private static byte[]   isHalfTone = null;
	
	// syntax
	public static final String SYNTAX_DEFINE             = "DEFINE";
	public static final String SYNTAX_COMMENT            = "COMMENT";
	public static final String SYNTAX_GLOBAL             = "GLOBAL";
	public static final String SYNTAX_P                  = "PERCUSSION_CHANNEL";
	public static final String SYNTAX_END                = "END";
	public static final String SYNTAX_BLOCK_OPEN         = "BLOCK_OPEN";
	public static final String SYNTAX_BLOCK_CLOSE        = "BLOCK_CLOSE";
	public static final String SYNTAX_MACRO              = "MACRO";
	public static final String SYNTAX_INCLUDE            = "INCLUDE";
	public static final String SYNTAX_INSTRUMENTS        = "INSTRUMENTS";
	public static final String SYNTAX_META               = "META";
	public static final String SYNTAX_META_COPYRIGHT     = "META_COPYRIGHT";
	public static final String SYNTAX_META_TITLE         = "META_TITLE";
	public static final String SYNTAX_META_COMPOSER      = "META_COMPOSER";
	public static final String SYNTAX_META_LYRICIST      = "META_LYRICIST";
	public static final String SYNTAX_META_ARTIST        = "META_ARTIST";
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
	public static final String SYNTAX_TUPLET             = "TUPLET";
	public static final String SYNTAX_T                  = "TUPLET_SHORT";
	public static final String SYNTAX_TREMOLO            = "TREMOLO";
	public static final String SYNTAX_TR                 = "TREMOLO_SHORT";
	public static final String SYNTAX_REST               = "REST";
	public static final String SYNTAX_CHORD              = "CHORD";
	public static final String SYNTAX_INLINE_CHORD_SEP   = "INLINE_CHORD_SEP";
	public static final String SYNTAX_INCLUDE_FILE       = "INCLUDE_FILE";
	public static final String SYNTAX_SOUNDFONT          = "SOUNDFONT";
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
	public static final String SYNTAX_DURATION_PLUS      = "DURATION_PLUS";
	
	// TODO: use for the syntax or delete
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
	
	// translation keys used by many classes
	public static final String TICK                        = "tick";
	
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
	public static final String INSTRUMENT_IDS              = "instrument_ids";
	public static final String DRUMKIT_IDS                 = "drumkit_ids";
	public static final String SHOW_INFO                   = "show_info";
	public static final String SHOW_INFO_FROM_PLAYER       = "show_info_from_player";
	public static final String IMPORT                      = "import";
	public static final String EXPORT                      = "export";
	public static final String TRANSPOSE_LEVEL             = "transpose_level";
	public static final String PLAYER                      = "player";
	public static final String MIDICAPL_FILE               = "midicapl_file";
	public static final String MIDI_FILE                   = "midi_file";
	public static final String SOUNDFONT                   = "soundfont";
	public static final String REMEMBER_SF                 = "remember_sf";
	public static final String REMEMBER_SF_TT              = "remember_sf_tt";
	public static final String REMEMBER_MPL                = "remember_mpl";
	public static final String REMEMBER_MPL_TT             = "remember_mpl_tt";
	public static final String REMEMBER_MID                = "remember_mid";
	public static final String REMEMBER_MID_TT             = "remember_mid_tt";
	public static final String PLAYER_BUTTON               = "player_button";
	public static final String UNCHOSEN_FILE               = "unchosen_file";
	public static final String SF_LOADED_BY_SOURCE         = "sf_loaded_by_source";
	public static final String CHOOSE_FILE                 = "choose_file";
	public static final String CHOOSE_FILE_EXPORT          = "choose_file_export";
	public static final String MIDI_EXPORT                 = "midi_export";
	public static final String MIDICAPL_EXPORT             = "midicapl_export";
	public static final String CONF_ERROR_OK               = "conf_error_ok";
	public static final String CONF_ERROR_ERROR            = "conf_error_error";
	public static final String CONF_ERROR_HALFTONE_SYNTAX  = "conf_Error_halftone_syntax";
	public static final String ERROR_NOT_YET_IMPLEMENTED   = "error_not_yet_implemented";
	
	// MidicaFileChooser
	public static final String CHARSET                     = "charset";
	public static final String CHARSET_DESC_MPL_READ       = "charset_desc_mpl_read";
	public static final String CHARSET_DESC_MID_READ       = "charset_desc_mid_read";
	public static final String CHARSET_DESC_MPL_WRITE      = "charset_desc_mpl_write";
	public static final String CHARSET_DESC_MID_WRITE      = "charset_desc_mid_write";
	
	// InfoView
	public static final String TITLE_INFO_VIEW             = "title_info_view";
	public static final String TAB_CONFIG                  = "tab_config";
	public static final String TAB_SOUNDFONT               = "tab_soundfont";
	public static final String TAB_MIDI_SEQUENCE           = "tab_midi_sequence";
	public static final String TAB_ABOUT                   = "tab_about";
	public static final String TAB_NOTE_DETAILS            = "tab_note_details";
	public static final String TAB_PERCUSSION_DETAILS      = "tab_percussion_details";
	public static final String TAB_SYNTAX_DETAILS          = "tab_syntax_details";
	public static final String TAB_SOUNDFONT_INFO          = "tab_soundfont_info";
	public static final String TAB_SOUNDFONT_INSTRUMENTS   = "tab_soundfont_instruments";
	public static final String TAB_SOUNDFONT_RESOURCES     = "tab_soundfont_resources";
	public static final String TAB_MIDI_SEQUENCE_INFO      = "tab_midi_sequence_info";
	public static final String TAB_MIDI_KARAOKE            = "tab_midi_karaoke";
	public static final String TAB_BANK_INSTR_NOTE         = "tab_bank_instr_note";
	public static final String TAB_MESSAGES                = "tab_messages";
	public static final String SOUNDFONT_DRUMKITS          = "soundfont_drumkits";
	public static final String SOUNDFONT_VENDOR            = "soundfont_vendor";
	public static final String SOUNDFONT_CREA_DATE         = "soundfont_crea_date";
	public static final String SOUNDFONT_CREA_TOOLS        = "soundfont_crea_tools";
	public static final String SOUNDFONT_PRODUCT           = "soundfont_product";
	public static final String SOUNDFONT_TARGET_ENGINE     = "soundfont_target_engine";
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
	public static final String UNSET                       = "unset";
	public static final String DATE                        = "date";
	public static final String AUTHOR                      = "author";
	public static final String SOURCE_URL                  = "source_url";
	public static final String WEBSITE                     = "website";
	public static final String LINK_TOOLTIP                = "link_tooltip";
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
	public static final String INFO_COL_PERC_ID_LONG       = "info_col_perc_id_long";
	public static final String INFO_COL_PERC_ID_SHORT      = "info_col_perc_id_short";
	public static final String INFO_COL_INSTR_NUM          = "info_col_instr_num";
	public static final String INFO_COL_INSTR_NAME         = "info_col_instr_name";
	public static final String INFO_COL_DRUMKIT_NUM        = "info_col_drumkit_num";
	public static final String INFO_COL_DRUMKIT_NAME       = "info_col_drumkit_name";
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
	public static final String SYNTAX_CAT_CALL             = "syntax_cat_call";
	public static final String SYNTAX_CAT_OPTION           = "syntax_cat_option";
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
	
	// MidiParser
	public static final String ERROR_ONLY_PPQ_SUPPORTED    = "error_only_ppq_supported";
	
	// MidicaPLParser
	public static final String ERROR_0_NOT_ALLOWED              = "error_0_not_allowed";
	public static final String ERROR_NEGATIVE_NOT_ALLOWED       = "error_negative_not_allowed";
	public static final String ERROR_NOT_AN_INTEGER             = "error_not_an_integer";
	public static final String ERROR_NOT_A_FLOAT                = "error_not_a_float";
	public static final String ERROR_INSTRUMENTS_NOT_DEFINED    = "error_instruments_not_defined";
	public static final String ERROR_GLOBALS_IN_INSTR_DEF       = "error_globals_in_instr_def";
	public static final String ERROR_UNKNOWN_CMD                = "error_unknown_cmd";
	public static final String ERROR_CMD_END_WITHOUT_BEGIN      = "error_cmd_end_without_begin";
	public static final String ERROR_BLOCK_INVALID_ARG          = "error_block_invalid_arg";
	public static final String ERROR_BLOCK_UNMATCHED_CLOSE      = "error_block_unmatched_close";
	public static final String ERROR_BLOCK_UNMATCHED_OPEN       = "error_block_unmatched_open";
	public static final String ERROR_NESTABLE_BLOCK_OPEN_AT_EOF = "error_nestable_block_open_at_eof";
	public static final String ERROR_NAMED_BLOCK_OPEN_AT_EOF    = "error_named_block_open_at_eof";
	public static final String ERROR_ARGS_NOT_ALLOWED           = "error_args_not_allowed";
	public static final String ERROR_MACRO_ALREADY_DEFINED      = "error_macro_already_defined";
	public static final String ERROR_MACRO_NOT_ALLOWED_HERE     = "error_macro_not_allowed_here";
	public static final String ERROR_META_NOT_ALLOWED_HERE      = "error_meta_not_allowed_here";
	public static final String ERROR_CHORD_ALREADY_DEFINED      = "error_chord_already_defined";
	public static final String ERROR_CHORD_EQUALS_NOTE          = "error_chord_equals_note";
	public static final String ERROR_CHORD_EQUALS_PERCUSSION    = "error_chord_equals_percussion";
	public static final String ERROR_CHORD_CONTAINS_ALREADY     = "error_chord_contains_already";
	public static final String ERROR_CHORD_DEF_NOT_ALLOWED_HERE = "error_chord_def_not_allowed_here";
	public static final String ERROR_CHORD_NUM_OF_ARGS          = "error_chord_num_of_args";
	public static final String ERROR_DEFINE_NUM_OF_ARGS         = "error_define_num_of_args";
	public static final String ERROR_ALREADY_REDEFINED          = "error_already_redefined";
	public static final String ERROR_FILE_NUM_OF_ARGS           = "error_file_num_of_args";
	public static final String ERROR_SOUNDFONT_NUM_OF_ARGS      = "error_soundfont_num_of_args";
	public static final String ERROR_FILE_EXISTS                = "error_file_exists";
	public static final String ERROR_FILE_NORMAL                = "error_file_normal";
	public static final String ERROR_FILE_READABLE              = "error_file_readable";
	public static final String ERROR_FILE_IO                    = "error_file_io";
	public static final String ERROR_SOUNDFONT_IO               = "error_soundfont_io";
	public static final String ERROR_SOUNDFONT_ALREADY_PARSED   = "error_soundfont_already_parsed";
	public static final String ERROR_MACRO_NUM_OF_ARGS          = "error_macro_num_of_arts";
	public static final String ERROR_META_NUM_OF_ARGS           = "error_meta_num_of_arts";
	public static final String ERROR_META_UNKNOWN_CMD           = "error_meta_unknown_cmd";
	public static final String ERROR_MACRO_RECURSION            = "error_macro_recursion";
	public static final String ERROR_MACRO_UNDEFINED            = "error_macro_undefined";
	public static final String ERROR_INCLUDE_NUM_OF_ARGS        = "error_include_num_of_args";
	public static final String ERROR_INCLUDE_UNKNOWN_ARG        = "error_include_unknown_arg";
	public static final String ERROR_INVALID_TIME_DENOM         = "error_invalid_time_denom";
	public static final String ERROR_INVALID_TIME_SIG           = "error_invalid_time_sig";
	public static final String ERROR_INVALID_KEY_SIG            = "error_invalid_key_sig";
	public static final String ERROR_INVALID_TONALITY           = "error_invalid_tonality";
	public static final String ERROR_PARTIAL_RANGE              = "error_partial_range";
	public static final String ERROR_PARTIAL_RANGE_ORDER        = "error_partial_range_order";
	public static final String ERROR_PARTIAL_RANGE_EMPTY        = "error_partial_range_empty";
	public static final String ERROR_MODE_INSTR_NUM_OF_ARGS     = "error_mode_instr_num_of_args";
	public static final String ERROR_NOTE_TOO_BIG               = "error_note_too_big";
	public static final String ERROR_NOTE_TOO_SMALL             = "error_note_too_small";
	public static final String ERROR_NOTE_LENGTH_INVALID        = "error_note_length_invalid";
	public static final String ERROR_EMPTY_DURATION_SUMMAND     = "error_empty_duration_summand";
	public static final String ERROR_UNKNOWN_MACRO_CMD          = "error_unknown_macro_cmd";
	public static final String ERROR_INSTR_NUM_OF_ARGS          = "error_num_of_args";
	public static final String ERROR_INSTR_BANK                 = "error_instr_bank";
	public static final String ERROR_GLOBAL_NUM_OF_ARGS         = "error_global_num_of_args";
	public static final String ERROR_UNKNOWN_GLOBAL_CMD         = "error_unknown_global_cmd: ";
	public static final String ERROR_UNKNOWN_COMMAND_ID         = "error_unknown_command_id";
	public static final String ERROR_MIDI_PROBLEM               = "error_midi_problem";
	public static final String ERROR_CH_CMD_NUM_OF_ARGS         = "error_ch_num_of_args";
	public static final String ERROR_CANT_PARSE_OPTIONS         = "error_cant_parse_options";
	public static final String ERROR_OPTION_NEEDS_VAL           = "error_option_needs_val";
	public static final String ERROR_OPTION_VAL_NOT_ALLOWED     = "error_option_val_not_allowed";
	public static final String ERROR_VEL_NOT_MORE_THAN_127      = "error_vel_not_more_than_127";
	public static final String ERROR_VEL_NOT_LESS_THAN_1        = "error_vel_not_less_than_1";
	public static final String ERROR_TUPLET_INVALID             = "error_tuplet_invalid";
	public static final String ERROR_DURATION_MORE_THAN_0       = "error_duration_more_than_0";
	public static final String ERROR_UNKNOWN_OPTION             = "error_unknown_option: ";
	public static final String ERROR_UNKNOWN_NOTE               = "error_unknown_note";
	public static final String ERROR_UNKNOWN_PERCUSSION         = "error_unknown_percussion";
	public static final String ERROR_CHANNEL_UNDEFINED          = "error_channel_undefined";
	public static final String ERROR_CHANNEL_REDEFINED          = "error_channel_redefined";
	public static final String ERROR_INVALID_CHANNEL_NUMBER     = "error_invalid_channel_number";
	public static final String ERROR_NOT_ALLOWED_IN_INSTR_BLK   = "error_not_allowed_in_instr_blk";
	public static final String ERROR_NOT_ALLOWED_IN_META_BLK    = "error_not_allowed_in_meta_blk";
	public static final String ERROR_NOT_ALLOWED_IN_BLK         = "error_not_allowed_in_blk";
	
	// NestableBlock
	public static final String ERROR_BLOCK_ARG_ALREADY_SET      = "error_block_arg_already_set";
	
	// SequenceParser
	public static final String ERROR_ANALYZE_POSTPROCESS        = "error_analyze_postprocess";
	
	// WaitView
	public static final String TITLE_WAIT                       = "title_wait";
	public static final String WAIT_PARSE_MPL                   = "wait_parse_mpl";
	public static final String WAIT_PARSE_MID                   = "wait_parse_mid";
	public static final String WAIT_PARSE_SF2                   = "wait_parse_sf2";
	public static final String WAIT_REPARSE                     = "wait_reparse";
	public static final String WAIT_SETUP_DEVICES               = "wait_setup_devices";
	
	// Exporter
	public static final String ERROR_EXPORT                     = "error_export";
	public static final String ERROR_FILE_NOT_WRITABLE          = "error_file_not_writable";
	public static final String OVERWRITE_FILE                   = "overwrite_file";
	
	// ExportResultView
	public static final String TITLE_EXPORT_RESULT              = "title_export_result";
	public static final String EXPORT_SUCCESS                   = "export_success";
	public static final String NUMBER_OF_WARNINGS               = "number_of_warnings";
	public static final String WARNING_COL_TRACK                = "warning_col_track";
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
	 * - Initializing percussion IDs using {@link #initPercussion()}
	 * - Initializing MidicaPL syntax keywords using {@link #initSyntax()}
	 * - Initializing instrument IDs using {@link #initInstruments()}
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
		set( Config.CBX_HALFTONE_ID_DIESIS,          "-diesis: do-diesis, re-diesis..."      );
		set( Config.CBX_HALFTONE_ID_BEMOLLE,         "-bemolle: re-bemolle, mi-bemolle..."   );
		set( Config.CBX_HALFTONE_ID_CIS,             "-is: cis, dis, fis..."                 );
		set( Config.CBX_HALFTONE_ID_DES,             "-es: des, es, ges..."                  );
		
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
		
		// translation keys used by many classes
		set( TICK,                         "Tick"                          );
		
		// UiView
		set( CONFIGURATION,                "Configuration"                 );
		set( LANGUAGE,                     "Language"                      );
		set( NOTE_SYSTEM,                  "Note System"                   );
		set( HALF_TONE_SYMBOL,             "Half Tone Symbol"              );
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
		set( MIDICAPL_FILE,                "MidicaPL file"                 );
		set( UNKNOWN_NOTE_NAME,            "unknown"                       );
		set( UNKNOWN_PERCUSSION_NAME,      "unknown"                       );
		set( UNKNOWN_DRUMKIT_NAME,         "unknown"                       );
		set( UNKNOWN_SYNTAX,               "?"                             );
		set( UNKNOWN_INSTRUMENT,           "unknown"                       );
		set( MIDI_FILE,                    "Midi file"                     );
		set( SOUNDFONT,                    "Soundfont"                     );
		set( REMEMBER_SF,                  "Remember"                      );
		set( REMEMBER_SF_TT,               "Load the chosen soundfont automatically at the next startup" );
		set( REMEMBER_MPL,                 "Remember"                      );
		set( REMEMBER_MPL_TT,              "Load the chosen MidicaPL file automatically at the next startup" );
		set( REMEMBER_MID,                 "Remember"                      );
		set( REMEMBER_MID_TT,              "Load the chosen MIDI file automatically at the next startup" );
		set( PLAYER_BUTTON,                "Start Player"                  );
		set( UNCHOSEN_FILE,                "no file loaded"                );
		set( SF_LOADED_BY_SOURCE,          "[loaded by MidicaPL file]"     );
		set( CHOOSE_FILE,                  "select file"                   );
		set( CHOOSE_FILE_EXPORT,           "select file"                   );
		set( MIDI_EXPORT,                  "Midi file"                     );
		set( MIDICAPL_EXPORT,              "MidicaPL file"                 );
		set( CONF_ERROR_OK,                "Configuration OK"              );
		set( CONF_ERROR_HALFTONE_SYNTAX,   "Chosen half tone symbol incompatible with chosen syntax" );
		set( ERROR_NOT_YET_IMPLEMENTED,    "This functionality is not yet implemented" );
		
		// MidicaFileChooser
		set( CHARSET,                  "Charset"                                              );
		set( CHARSET_DESC_MPL_READ,    "Encoding of the source file."                         );
		set( CHARSET_DESC_MID_READ,    "Default encoding of text-based messages in the source file. Used if neither a BOM nor a {@...} tag is found." );
		set( CHARSET_DESC_MPL_WRITE,   "Encoding of the file to be saved."                    );
		set( CHARSET_DESC_MID_WRITE,   "Encoding for text-based messages in the target file." );
		
		// InfoView
		set( TITLE_INFO_VIEW,                        "Midica Info"                   );
		set( TAB_CONFIG,                             "Configuration"                 );
		set( TAB_SOUNDFONT,                          "Soundfont"                     );
		set( TAB_MIDI_SEQUENCE,                      "MIDI Sequence"                 );
		set( TAB_ABOUT,                              "About"                         );
		set( TAB_NOTE_DETAILS,                       "Note Table"                    );
		set( TAB_PERCUSSION_DETAILS,                 "Percussion IDs"                );
		set( TAB_SYNTAX_DETAILS,                     "Commands"                      );
		set( TAB_SOUNDFONT_INFO,                     "General Info"                  );
		set( TAB_SOUNDFONT_INSTRUMENTS,              "Instruments & Drum Kits"       );
		set( TAB_SOUNDFONT_RESOURCES,                "Resources"                     );
		set( TAB_MIDI_SEQUENCE_INFO,                 "General Info"                  );
		set( TAB_MIDI_KARAOKE,                       "Karaoke Info"                  );
		set( TAB_BANK_INSTR_NOTE,                    "Banks, Instruments, Notes"     );
		set( TAB_MESSAGES,                           "MIDI Messages"                 );
		set( SOUNDFONT_VENDOR,                       "Vendor"                        );
		set( SOUNDFONT_CREA_DATE,                    "Creation Date"                 );
		set( SOUNDFONT_CREA_TOOLS,                   "Creation Tools"                );
		set( SOUNDFONT_PRODUCT,                      "Product"                       );
		set( SOUNDFONT_TARGET_ENGINE,                "Target Sound Engine"           );
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
		set( LYRICS,                                 "Lyrics"                        );
		set( TOTAL,                                  "Total"                         );
		set( PER_CHANNEL,                            "Per Channel"                   );
		set( BANK,                                   "Bank"                          );
		set( COLLAPSE_BUTTON,                        "-"                             );
		set( COLLAPSE_TOOLTIP,                       "Collapse"                      );
		set( EXPAND_BUTTON,                          "+"                             );
		set( EXPAND_TOOLTIP,                         "Expand"                        );
		set( DETAILS,                                "Details"                       );
		set( SAMPLES_TOTAL,                          "Samples (Total)"               );
		set( SAMPLES_AVERAGE,                        "Samples (Average)"             );
		set( FRAMES,                                 "Frames"                        );
		set( BYTES,                                  "Bytes"                         );
		set( SEC,                                    "Sec"                           );
		set( BROADCAST_MSG,                          "Broadcast"                     );
		set( INVALID_MSG,                            "Invalid Message"               );
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
		set( UNSET,                                  "Not Set"                       );
		set( DATE,                                   "Date (UTC)"                    );
		set( AUTHOR,                                 "Author"                        );
		set( SOURCE_URL,                             "Source"                        );
		set( WEBSITE,                                "Website"                       );
		set( LINK_TOOLTIP,                           "(Click to open in Browser)"    );
		set( TIMESTAMP_FORMAT,                       "yyyy-MM-dd HH:mm:ss"           );
		set( NAME,                                   "Name"                          );
		set( FILE,                                   "File"                          );
		set( VERSION,                                "Version"                       );
		set( DESCRIPTION,                            "Description"                   );
		set( INFO_COL_NOTE_NUM,                      "Number"                        );
		set( INFO_COL_NOTE_NAME,                     "Name"                          );
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
		set( SYNTAX_CAT_CALL,                        "Execution Commands"            );
		set( SYNTAX_CAT_OPTION,                      "Option Syntax"                 );
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
		set( MSG_DETAILS_TICK_SG,                    "Tick:"            );
		set( MSG_DETAILS_TICK_PL,                    "Ticks:"           );
		set( MSG_DETAILS_LENGTH,                     "Length:"          );
		set( MSG_DETAILS_STATUS_BYTE,                "Status Byte:"     );
		set( MSG_DETAILS_TRACK_SG,                   "Track:"           );
		set( MSG_DETAILS_TRACK_PL,                   "Tracks:"          );
		set( MSG_DETAILS_CHANNEL_SG,                 "Channel:"         );
		set( MSG_DETAILS_CHANNEL_PL,                 "Channels:"        );
		set( MSG_DETAILS_META_TYPE,                  "Meta Type:"       );
		set( MSG_DETAILS_VENDOR,                     "Manufacturer:"    );
		set( MSG_DETAILS_DEVICE_ID_SG,               "Device ID:"       );
		set( MSG_DETAILS_DEVICE_ID_PL,               "Device IDs:"      );
		set( MSG_DETAILS_SUB_ID_1,                   "Sub ID 1"         );
		set( MSG_DETAILS_SUB_ID_2,                   "Sub ID 2"         );
		set( MSG_DETAILS_CTRL_BYTE,                  "Controller Byte:" );
		set( MSG_DETAILS_RPN_BYTE_SG,                "RPN Byte:"        );
		set( MSG_DETAILS_RPN_BYTE_PL,                "RPN Bytes:"       );
		set( MSG_DETAILS_NRPN_BYTE_SG,               "NRPN Byte:"       );
		set( MSG_DETAILS_NRPN_BYTE_PL,               "NRPN Bytes:"      );
		set( MSG_DETAILS_TEXT_SG,                    "Text:"            );
		set( MSG_DETAILS_TEXT_PL,                    "Texts:"           );
		set( MSG_DETAILS_MESSAGE,                    "Message (Hex):"   );
		set( MSG_DETAILS_DESCRIPTION,                "Description"      );
		
		// syntax for InfoView
		set( SYNTAX_DEFINE,             "syntax element definition"                        );
		set( SYNTAX_COMMENT,            "comment"                                          );
		set( SYNTAX_GLOBAL,             "global command (all channels)"                    );
		set( SYNTAX_P,                  "percussion channel"                               );
		set( SYNTAX_END,                "end of a definition block"                        );
		set( SYNTAX_BLOCK_OPEN,         "opens a nestable block"                           );
		set( SYNTAX_BLOCK_CLOSE,        "closes a nestable block"                          );
		set( SYNTAX_MACRO,              "macro definition"                                 );
		set( SYNTAX_INCLUDE,            "macro execution"                                  );
		set( SYNTAX_INSTRUMENTS,        "definition of instruments"                        );
		set( SYNTAX_META,               "meta information block definition"                );
		set( SYNTAX_META_COPYRIGHT,     "copyright information"                            );
		set( SYNTAX_META_TITLE,         "song title"                                       );
		set( SYNTAX_META_COMPOSER,      "coposer"                                          );
		set( SYNTAX_META_LYRICIST,      "lyricist"                                         );
		set( SYNTAX_META_ARTIST,        "artist"                                           );
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
		set( SYNTAX_TUPLET,             "tuplet option (long)"                             );
		set( SYNTAX_T,                  "tuplet option (short)"                            );
		set( SYNTAX_TREMOLO,            "tremolo option (long)"                            );
		set( SYNTAX_TR,                 "tremolo option (short)"                           );
		set( SYNTAX_REST,               "rest character"                                   );
		set( SYNTAX_CHORD,              "chord definition"                                 );
		set( SYNTAX_INLINE_CHORD_SEP,   "inline chord separator"                           );
		set( SYNTAX_INCLUDE_FILE,       "including another file"                           );
		set( SYNTAX_SOUNDFONT,          "including a soundfont file"                       );
		
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
		set( SYNTAX_DURATION_PLUS,    "duration addition symbol"                         );
		
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
		
		// Parser
		set( ERROR_0_NOT_ALLOWED,                 "0 not allowed"                 );
		set( ERROR_NEGATIVE_NOT_ALLOWED,          "negative number not allowed: " );
		set( ERROR_NOT_AN_INTEGER,                "not an integer: "              );
		set( ERROR_NOT_A_FLOAT,                   "not a valid number: "          );
		
		// MidiParser
		set( ERROR_ONLY_PPQ_SUPPORTED,            "Only MIDI files with division type PPQ are supported." );
		
		// MidicaPLParser
		set( ERROR_INSTRUMENTS_NOT_DEFINED,       "no instruments have been defined yet"                              );
		set( ERROR_GLOBALS_IN_INSTR_DEF,          "global commands are not allowed inside an instrument definition"   );
		set( ERROR_UNKNOWN_CMD,                   "unknown command: "                                                 );
		set( ERROR_CMD_END_WITHOUT_BEGIN,         "there is no open block to be closed"                               );
		set( ERROR_BLOCK_INVALID_ARG,             "invalid block argument: "                                          );
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
		set( ERROR_DEFINE_NUM_OF_ARGS,            "wrong number of arguments in DEFINE command"                       );
		set( ERROR_ALREADY_REDEFINED,             "Command ID already redefined. Cannot be redefined again: "         );
		set( ERROR_FILE_NUM_OF_ARGS,              "wrong number of arguments in INCLUDE_FILE command"                 );
		set( ERROR_SOUNDFONT_NUM_OF_ARGS,         "wrong number of arguments in SOUNDFONT command"                    );
		set( ERROR_FILE_EXISTS,                   "file does not exist:<br>"                                          );
		set( ERROR_FILE_NORMAL,                   "not a normal file:<br>"                                            );
		set( ERROR_FILE_READABLE,                 "file not readable:<br>"                                            );
		set( ERROR_FILE_IO,                       "file cannot be parsed:<br>"                                        );
		set( ERROR_SOUNDFONT_IO,                  "soundfont cannot be parsed:<br>"                                   );
		set( ERROR_SOUNDFONT_ALREADY_PARSED,      "a soundfont can be included only once"                             );
		set( ERROR_MACRO_NUM_OF_ARGS,             "wrong number of arguments in macro command"                        );
		set( ERROR_META_NUM_OF_ARGS,              "no arguments allowed in meta command"                              );
		set( ERROR_META_UNKNOWN_CMD,              "unknown meta command: "                                            );
		set( ERROR_MACRO_RECURSION,               "Can't include the current macro in itself. Recursion not allowed." );
		set( ERROR_MACRO_UNDEFINED,               "include failed. macro not yet defined."                            );
		set( ERROR_MACRO_ALREADY_DEFINED,         "macro name has been already defined: "                             );
		set( ERROR_MACRO_NOT_ALLOWED_HERE,        "a macro definition is not allowed inside a block<br>maybe you forgot to close the block." );
		set( ERROR_META_NOT_ALLOWED_HERE,         "a meta definition is not allowed inside a block<br>maybe you forgot to close the block." );
		set( ERROR_INCLUDE_NUM_OF_ARGS,           "wrong number of arguments in macro command 'INCLUDE'"              );
		set( ERROR_INCLUDE_UNKNOWN_ARG,           "unknown argument for 'INCLUDE': "                                  );
		set( ERROR_INVALID_TIME_DENOM,            "invalid denominator in time signature: "                           );
		set( ERROR_INVALID_TIME_SIG,              "invalid time signature argument: "                                 );
		set( ERROR_INVALID_KEY_SIG,               "invalid key signature argument: "                                  );
		set( ERROR_INVALID_TONALITY,              "invalid tonality: "                                                );
		set( ERROR_PARTIAL_RANGE,                 "invalid range definition: "                                        );
		set( ERROR_PARTIAL_RANGE_ORDER,           "invalid range definition: ranges must be ascending: "              );
		set( ERROR_PARTIAL_RANGE_EMPTY,           "empty range in channel definition"                                 );
		set( ERROR_MODE_INSTR_NUM_OF_ARGS,        "wrong number of arguments in mode command 'INSTRUMENTS'"           );
		set( ERROR_NOTE_TOO_BIG,                  "note number too big: "                                             );
		set( ERROR_NOTE_TOO_SMALL,                "note number too small: "                                           );
		set( ERROR_NOTE_LENGTH_INVALID,           "invalid note length expression: "                                  );
		set( ERROR_EMPTY_DURATION_SUMMAND,        "empty summand in duration string: "                                );
		set( ERROR_UNKNOWN_MACRO_CMD,             "unknown macro command: "                                           );
		set( ERROR_INSTR_NUM_OF_ARGS,             "wrong number of arguments in instrument command"                   );
		set( ERROR_INSTR_BANK,                    "Instrument and/or Bank definition erroneous"                       );
		set( ERROR_GLOBAL_NUM_OF_ARGS,            "wrong number of arguments in global command"                       );
		set( ERROR_UNKNOWN_GLOBAL_CMD,            "unknown global command: "                                          );
		set( ERROR_UNKNOWN_COMMAND_ID,            "Unknown command ID: "                                              );
		set( ERROR_MIDI_PROBLEM,                  "<html>Midi Problem!<br>"                                           );
		set( ERROR_CH_CMD_NUM_OF_ARGS,            "wrong number of arguments in channel command"                      );
		set( ERROR_CANT_PARSE_OPTIONS,            "cannot parse options"                                              );
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
		
		// NestableBlock
		set( ERROR_BLOCK_ARG_ALREADY_SET,         "Block argument has already been set before: "                      );
		
		// SequenceParser
		set( ERROR_ANALYZE_POSTPROCESS,           "Error while postprocessing the sequence"                           );
		
		// WaitView
		set( TITLE_WAIT,                          "Please Wait"                                                       );
		set( WAIT_PARSE_MPL,                      "Parsing the MidicaPL file..."                                      );
		set( WAIT_PARSE_MID,                      "Parsing the MIDI file..."                                          );
		set( WAIT_PARSE_SF2,                      "Parsing the Soundfont"                                             );
		set( WAIT_REPARSE,                        "Reloading the File"                                                );
		set( WAIT_SETUP_DEVICES,                  "Setting up MIDI devices and loading Soundfont"                     );
		
		// Exporter
		set( ERROR_EXPORT,                        "Export Error in the file "                                         );
		set( ERROR_FILE_NOT_WRITABLE,             "File not writable"                                                 );
		set( OVERWRITE_FILE,                      "Overwrite the file?"                                               );
		
		// ExportResultView
		set( TITLE_EXPORT_RESULT,                 "Export Result"                                                     );
		set( EXPORT_SUCCESS,                      "The file has been exported!"                                       );
		set( NUMBER_OF_WARNINGS,                  "Number of warnings:"                                               );
		set( WARNING_COL_TRACK,                   "Track"                                                             );
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
		                                        + " at the same time and channel (current/old velocity: %s/%s)"       );
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
		set( SNDCHK_COL_NAME_SF,             "Soundfont Name"                  );
		set( SNDCHK_COL_NAME_SYNTAX,         "Syntax"                          );
	}
	
	/**
	 * Initializes the internal data structure for german language translations.
	 */
	private static void initLanguageGerman() {
		
		// Config
		set( Config.CBX_HALFTONE_ID_SHARP,           "#: c#, d#, f#..."                      );
		set( Config.CBX_HALFTONE_ID_FLAT,            "b: db, eb, gb..."                      );
		set( Config.CBX_HALFTONE_ID_DIESIS,          "-diesis: do-diesis, re-diesis..."      );
		set( Config.CBX_HALFTONE_ID_BEMOLLE,         "-bemolle: re-bemolle, mi-bemolle..."   );
		set( Config.CBX_HALFTONE_ID_CIS,             "-is: cis, dis, fis..."                 );
		set( Config.CBX_HALFTONE_ID_DES,             "-es: des, es, ges..."                  );
		
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
		set( HALF_TONE_SYMBOL,                       "Halbton-Symbol"                        );
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
		set( SF_LOADED_BY_SOURCE,                    "[durch MidicaPL-Datei geladen]"        );
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
		else if ( Config.CBX_HALFTONE_ID_DIESIS.equals(configuredHalfTone) ) {
			suffix = "-diesis";
			sharp  = true;
		}
		else if ( Config.CBX_HALFTONE_ID_BEMOLLE.equals(configuredHalfTone) ) {
			suffix = "-bemolle";
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
		if ( Config.CBX_NOTE_ID_INTERNATIONAL_LC.equals(configuredNoteSystem) ) {
			initNotesInternational( false );
		}
		else if ( Config.CBX_NOTE_ID_INTERNATIONAL_UC.equals(configuredNoteSystem) ) {
			initNotesInternational( true );
		}
		else if ( Config.CBX_NOTE_ID_ITALIAN_LC.equals(configuredNoteSystem) ) {
			initNotesItalian( false );
		}
		else if ( Config.CBX_NOTE_ID_ITALIAN_UC.equals(configuredNoteSystem) ) {
			initNotesItalian( true );
		}
		else if ( Config.CBX_NOTE_ID_GERMAN_LC.equals(configuredNoteSystem) ) {
			initNotesGerman( false );
		}
		else if ( Config.CBX_NOTE_ID_GERMAN_UC.equals(configuredNoteSystem) ) {
			initNotesGerman( true );
		}
		else {
			initNotesInternational( false );
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
		if ( Config.CBX_OCTAVE_PLUS_MINUS_N.equals(configuredOctave) ) {
			initOctavesPlusMinusN();
		}
		else if ( Config.CBX_OCTAVE_PLUS_MINUS.equals(configuredOctave) ) {
			initOctavesPlusMinus();
		}
		else if ( Config.CBX_OCTAVE_INTERNATIONAL.equals(configuredOctave) ) {
			initOctavesInternational();
		}
		else if ( Config.CBX_OCTAVE_GERMAN.equals(configuredOctave) ) {
			initOctavesGerman();
		}
		else {
			initOctavesPlusMinusN();
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
		
		// set up default syntax
		setSyntax( SYNTAX_DEFINE,            "DEFINE"        );
		setSyntax( SYNTAX_COMMENT,           "//"            );
		setSyntax( SYNTAX_GLOBAL,            "*"             );
		setSyntax( SYNTAX_P,                 "p"             );
		setSyntax( SYNTAX_END,               "END"           );
		setSyntax( SYNTAX_BLOCK_OPEN,        "("             );
		setSyntax( SYNTAX_BLOCK_CLOSE,       ")"             );
		setSyntax( SYNTAX_MACRO,             "MACRO"         );
		setSyntax( SYNTAX_INCLUDE,           "INCLUDE"       );
		setSyntax( SYNTAX_INSTRUMENTS,       "INSTRUMENTS"   );
		setSyntax( SYNTAX_META,              "META"          );
		setSyntax( SYNTAX_META_COPYRIGHT,    "copyright"     );
		setSyntax( SYNTAX_META_TITLE,        "title"         );
		setSyntax( SYNTAX_META_COMPOSER,     "composer"      );
		setSyntax( SYNTAX_META_LYRICIST,     "lyrics"        );
		setSyntax( SYNTAX_META_ARTIST,       "artist"        );
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
		setSyntax( SYNTAX_TUPLET,             "tuplet"       );
		setSyntax( SYNTAX_T,                  "t"            );
		setSyntax( SYNTAX_TREMOLO,            "tremolo"      );
		setSyntax( SYNTAX_TR,                 "tr"           );
		setSyntax( SYNTAX_REST,               "-"            );
		setSyntax( SYNTAX_CHORD,              "CHORD"        );
		setSyntax( SYNTAX_INLINE_CHORD_SEP,   ","            );
		setSyntax( SYNTAX_INCLUDE_FILE,       "INCLUDE_FILE" );
		setSyntax( SYNTAX_SOUNDFONT,          "SOUNDFONT"    );
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
		setSyntax( SYNTAX_DURATION_PLUS,      "+"            );
		
		// switch to lower/upper, if needed
		if ( Config.CBX_SYNTAX_LOWER.equals(configuredSyntax) ) {
			for (String id : syntax.keySet()) {
				String keyword = syntax.get(id).toLowerCase();
				setSyntax(id, keyword);
			}
		}
		else if ( Config.CBX_SYNTAX_UPPER.equals(configuredSyntax) ) {
			for (String id : syntax.keySet()) {
				String keyword = syntax.get(id).toUpperCase();
				setSyntax(id, keyword);
			}
		}
		
		// init syntax for the syntax tab in the info view
		syntaxList = new ArrayList<SyntaxElement>();
		
		addSyntaxCategory( get(SYNTAX_CAT_DEFINITION) );
		addSyntaxForInfoView( SYNTAX_DEFINE      );
		addSyntaxForInfoView( SYNTAX_INSTRUMENTS );
		addSyntaxForInfoView( SYNTAX_META        );
		addSyntaxForInfoView( SYNTAX_CHORD       );
		addSyntaxForInfoView( SYNTAX_MACRO       );
		addSyntaxForInfoView( SYNTAX_END         );
		addSyntaxForInfoView( SYNTAX_BLOCK_OPEN  );
		addSyntaxForInfoView( SYNTAX_BLOCK_CLOSE );
		
		addSyntaxCategory( get(SYNTAX_CAT_CALL) );
		addSyntaxForInfoView( SYNTAX_INCLUDE      );
		addSyntaxForInfoView( SYNTAX_INCLUDE_FILE );
		addSyntaxForInfoView( SYNTAX_SOUNDFONT    );
		
		addSyntaxCategory( get(SYNTAX_CAT_GLOBAL) );
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
		
		addSyntaxCategory( get(SYNTAX_CAT_OTHER) );
		addSyntaxForInfoView( SYNTAX_COMMENT          );
		addSyntaxForInfoView( SYNTAX_REST             );
		addSyntaxForInfoView( SYNTAX_P                );
		addSyntaxForInfoView( SYNTAX_INLINE_CHORD_SEP );
		
		addSyntaxCategory( get(SYNTAX_CAT_META) );
		addSyntaxForInfoView( SYNTAX_META_COPYRIGHT );
		addSyntaxForInfoView( SYNTAX_META_TITLE     );
		addSyntaxForInfoView( SYNTAX_META_COMPOSER  );
		addSyntaxForInfoView( SYNTAX_META_LYRICIST  );
		addSyntaxForInfoView( SYNTAX_META_ARTIST    );
		
		addSyntaxCategory( get(SYNTAX_CAT_OPTION) );
		addSyntaxForInfoView( SYNTAX_OPT_SEPARATOR    );
		addSyntaxForInfoView( SYNTAX_OPT_ASSIGNER     );
		addSyntaxForInfoView( SYNTAX_PROG_BANK_SEP    );
		addSyntaxForInfoView( SYNTAX_BANK_SEP         );
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
		addSyntaxForInfoView( SYNTAX_TUPLET           );
		addSyntaxForInfoView( SYNTAX_T                );
		addSyntaxForInfoView( SYNTAX_TREMOLO          );
		addSyntaxForInfoView( SYNTAX_TR               );
		
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
		addSyntaxForInfoView( SYNTAX_TUPLET_INTRO  );
		addSyntaxForInfoView( SYNTAX_TUPLET_FOR    );
		addSyntaxForInfoView( SYNTAX_DURATION_PLUS );
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
		ConfigComboboxModel.refill( Config.PERCUSSION );
		
		// get language
		String percSet = Config.get( Config.PERCUSSION );
		
		// init percussion translations
		initPercussionEnglish();
		if ( Config.CBX_PERC_DE_1.equals( percSet ) ) {
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
		for ( String key : percussionIdToInt.keySet() ) {
			percussionIntToLongId.put( percussionIdToInt.get(key), key );
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
			percussionIntToShortId.put( percussionIdToInt.get(key), key );
		}
	}
	
	/**
	 * Creates translations between note values and the corresponding configured
	 * instrument IDs.
	 */
	public static void initInstruments() {
		
		// refresh combobox language
		ConfigComboboxModel.refill( Config.INSTRUMENT );

		// get language
		String instrSet = Config.get( Config.INSTRUMENT );
		
		// init instrument translations
		instrIntToName   = new HashMap<Integer, String>();
		instrNameToInt   = new HashMap<String, Integer>();
		instrumentList   = new ArrayList<InstrumentElement>();
		drumkitIntToName = new HashMap<Integer, String>();
		drumkitNameToInt = new HashMap<String, Integer>();
		drumkitList      = new ArrayList<InstrumentElement>();
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
	 * Sets up note names using:
	 * 
	 * - `+`, `+2`, `+3`, ... for higher octaves
	 * - `-`, `-2`, `-3`, ... for lower octaves
	 */
	private static void initOctavesPlusMinusN() {
		
		// define unmodified note names
		ArrayList<NamedInteger> noteNames = new ArrayList<NamedInteger>();
		byte i = 60; // middle C
		for (String name : notes) {
			NamedInteger note = new NamedInteger(name, i);
			noteNames.add(note);
			i++;
		}
		
		// initialize unmodified and higher notes
		String postfix = "";
		OCTAVE:
		for (int octave = 0; ; octave++) {
			if (1 == octave)
				postfix = "+";
			if (octave > 1)
				postfix = "+" + octave;
			int increment = octave * 12;
			for (NamedInteger name : noteNames) {
				String newName  = name.name  + postfix;
				int    newValue = name.value + increment;
				if (newValue > 127)
					break OCTAVE;
				noteNameToInt.put(newName, newValue);
			}
		}
		
		// initialize lower notes
		Collections.reverse( noteNames );
		postfix = "";
		OCTAVE:
		for (int octave = 1; ; octave++) {
			if (1 == octave)
				postfix = "-";
			if (octave > 1)
				postfix = "-" + octave;
			int decrement = octave * 12;
			for (NamedInteger name : noteNames) {
				String newName  = name.name  + postfix;
				int    newValue = name.value - decrement;
				if (newValue < 0)
					break OCTAVE;
				noteNameToInt.put(newName, newValue);
			}
		}
	}
	
	/**
	 * Sets up note names using:
	 * 
	 * - `+`, `++`, `+++`, ... for higher octaves
	 * - `-`, `--`, `---`, ... for lower octaves
	 */
	private static void initOctavesPlusMinus() {
		
		// define unmodified note names
		ArrayList<NamedInteger> noteNames = new ArrayList<NamedInteger>();
		byte i = 60; // middle C
		for (String name : notes) {
			NamedInteger note = new NamedInteger(name, i);
			noteNames.add(note);
			i++;
		}
		
		// initialize unmodified and higher notes
		StringBuilder postfix = new StringBuilder("");
		OCTAVE:
		for (int octave = 0; ; octave++) {
			if (octave > 0)
				postfix.append("+");
			int increment = octave * 12;
			NAME:
			for (NamedInteger name : noteNames) {
				String newName  = name.name  + postfix;
				int    newValue = name.value + increment;
				if (newValue > 127)
					break OCTAVE;
				noteNameToInt.put(newName, newValue);
			}
		}
		
		// initialize lower notes
		Collections.reverse(noteNames);
		postfix = new StringBuilder("");
		OCTAVE:
		for ( int octave = 1; ; octave++ ) {
			postfix.append("-");
			int decrement = octave * 12;
			NAME:
			for (NamedInteger name : noteNames) {
				String newName  = name.name  + postfix;
				int    newValue = name.value - decrement;
				if (newValue < 0)
					break OCTAVE;
				noteNameToInt.put(newName, newValue);
			}
		}
	}
	
	/**
	 * Sets up note names using the international octave naming system:
	 * 
	 * - C-1, C0, C1, C2, C3, C4...
	 */
	private static void initOctavesInternational() {
		// define unmodified note names
		ArrayList<NamedInteger> noteNames = new ArrayList<NamedInteger>();
		byte i = 0; // C-1
		for (String name : notes) {
			NamedInteger note = new NamedInteger(name, i);
			noteNames.add(note);
			i++;
		}
		
		// initialize octaves
		byte postfix = -1;
		OCTAVE:
		for (int octave = 0; ; octave++) {
			int increment = octave * 12;
			NAME:
			for (NamedInteger name : noteNames) {
				String newName  = name.name  + postfix;
				int    newValue = name.value + increment;
				if (newValue > 127)
					break OCTAVE;
				noteNameToInt.put(newName, newValue);
			}
			postfix++;
		}
	}
	
	/**
	 * Sets up note names using the traditional german octave naming system:
	 * 
	 * - lower case c, c', c'', c'''... for higher octaves and
	 * - upper case C, C', C'', C'''... for lower octaves
	 */
	private static void initOctavesGerman() {
		
		// define unmodified note names for higher notes (lower case)
		ArrayList<NamedInteger> noteNames = new ArrayList<NamedInteger>();
		byte i = 48; // capizalized C without modifiers
		for (String name : notes) {
			// use lower case for higher octaves
			name = name.toLowerCase();
			NamedInteger note = new NamedInteger(name, i);
			noteNames.add(note);
			i++;
		}
		
		// initialize unmodified and higher notes
		StringBuilder postfix = new StringBuilder("");
		OCTAVE:
		for (int octave = 0; ; octave++) {
			if ( octave > 0 )
				postfix.append("'");
			int increment = octave * 12;
			NAME:
			for (NamedInteger name : noteNames) {
				String newName  = name.name  + postfix;
				int    newValue = name.value + increment;
				if ( newValue > 127 )
					break OCTAVE;
				noteNameToInt.put(newName, newValue);
			}
		}
		
		// define unmodified note names for lower notes (upper case)
		for (NamedInteger note : noteNames) {
			note.name = note.name.substring( 0, 1 ).toUpperCase()
			          + note.name.substring( 1 )
			          ;
		}
		Collections.reverse( noteNames );
		postfix = new StringBuilder("");
		
		// initialize lower notes
		OCTAVE:
		for (int octave = 1; ; octave++) {
			if (octave > 1)
				postfix.append("'");
			int decrement = octave * 12;
			NAME:
			for (NamedInteger name : noteNames) {
				String newName  = name.name  + postfix;
				int    newValue = name.value - decrement;
				if (newValue < 0)
					break OCTAVE;
				noteNameToInt.put(newName, newValue);
			}
		}
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
	 * instrument IDs.
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
	 * Returns the base note name for the given note number.
	 * This name contains half tone steps but no octave information.
	 * 
	 * @param noteNum  The note number.
	 * @return  The note's base name.
	 */
	public static String getBaseNoteName( int noteNum ) {
		int noteIndex = noteNum % 12;
		return notes[ noteIndex ];
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
	public static String getInstrument(int i) {
		if ( instrIntToName.containsKey(i) )
			return instrIntToName.get(i);
		else
			return get( UNKNOWN_INSTRUMENT );
	}
	
	/**
	 * Indicates if the given name is a currently configured note name.
	 * 
	 * @param name  note name to be checked
	 * @return      true, if the note name is configured -- otherwise: false
	 */
	public static boolean noteExists(String name) {
		if ( noteNameToInt.containsKey(name) )
			return true;
		return false;
	}
	
	/**
	 * Returns a percussion instrument number by percussion ID.
	 * 
	 * @param id    long or short ID of the percussion instrument
	 * @return      instrument value as defined by the MIDI specification
	 */
	public static int getPercussion(String name) {
		if ( percussionIdToInt.containsKey(name) )
			return percussionIdToInt.get(name);
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
		if ( percussionIntToLongId.containsKey(i) )
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
		if ( percussionIntToShortId.containsKey(i) )
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
		if ( drumkitNameToInt.containsKey(name) )
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
		if ( drumkitIntToName.containsKey(i) )
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
		if ( percussionIdToInt.containsKey(name) )
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
		if ( syntax.containsKey(id) )
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
	 * Checks if the configured half tone symbol is sharp or flat.
	 * Only needed for some special cases when parsing the key signature.
	 * 
	 * @return **true**, if flat is configured, otherwise false.
	 */
	public static boolean isFlatConfigured() {
		String configuredHalfTone = Config.get( Config.HALF_TONE );
		if ( Config.CBX_HALFTONE_ID_FLAT.equals(configuredHalfTone)    ) { return true; }
		if ( Config.CBX_HALFTONE_ID_BEMOLLE.equals(configuredHalfTone) ) { return true; }
		if ( Config.CBX_HALFTONE_ID_DES.equals(configuredHalfTone)     ) { return true; }
		return false;
	}
}
