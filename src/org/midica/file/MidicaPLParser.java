/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.midi.InvalidMidiDataException;

import org.midica.config.Dict;
import org.midica.midi.MidiDevices;
import org.midica.midi.SequenceCreator;

/**
 * This class is used to parse a MidicaPL source file.
 * 
 * @author Jan Trukenmüller
 */
public class MidicaPLParser extends SequenceParser {
	
	/* *****************
	 * constants
	 *******************/
	
	// identifiers for options
	private static final String OPT_VOLUME   = "volume";
	private static final String OPT_MULTIPLE = "multiple";
	private static final String OPT_STACCATO = "staccato";
	private static final String OPT_QUANTITY = "quantity";
	
	private static final int MODE_INSTRUMENTS = 0;
	private static final int MODE_MACRO       = 1;
	private static final int MODE_DEFAULT     = 2;
	
	private static final int TICK_BANK_BEFORE_PROGRAM = 10; // how many ticks a bank select will be made before the program change
	
	private static final int  PAUSE_VALUE = -1;
	
	/* *****************
	 * class fields
	 *******************/
	
	public static String DEFINE        = null;
	public static String COMMENT       = null;
	public static String GLOBAL        = null;
	public static String P             = null;
	public static String END           = null;
	public static String MACRO         = null;
	public static String INCLUDE       = null;
	public static String INSTRUMENTS   = null;
	public static String CHORD         = null;
	public static String BPM           = null;
	public static String OPT_SEPARATOR = null;
	public static String OPT_ASSIGNER  = null;
	public static String VOLUME        = null;
	public static String V             = null;
	public static String STACCATO      = null;
	public static String S             = null;
	public static String MULTIPLE      = null;
	public static String M             = null;
	public static String QUANTITY      = null;
	public static String Q             = null;
	public static String PAUSE         = null;
	public static String INCLUDE_FILE  = null;
	public static String LENGTH_32     = null;
	public static String LENGTH_16     = null;
	public static String LENGTH_8      = null;
	public static String LENGTH_4      = null;
	public static String LENGTH_2      = null;
	public static String LENGTH_1      = null;
	public static String LENGTH_M1     = null;
	public static String LENGTH_M2     = null;
	public static String LENGTH_M4     = null;
	public static String LENGTH_M8     = null;
	public static String LENGTH_M16    = null;
	public static String LENGTH_M32    = null;
	public static String DOT           = null;
	public static String TRIPLET       = null;
	public static String TUPLET        = null;
	public static String TUPLET_FOR    = null;
	
	private static ArrayList<Instrument>                instruments       = null;
	private static HashMap<String, ArrayList<String[]>> macros            = null;
	private static HashMap<String, HashSet<Integer>>    chords            = null;
	private static boolean                              instrumentsParsed = false;
	
	/* *******************
	 * instance fields
	 *********************/
	
	private File                file             = null;
	private int                 currentMode      = MODE_DEFAULT;
	private String              currentMacroName = null;
	private ArrayList<String[]> currentMacro     = null;
	
	/**
	 * Indicates if the current parser object parses the file chosen by the user.  
	 * Otherwise it parses an included file.
	 */
	private boolean isRootParser = false;
	
	/**
	 * Creates a new MidicaPL parser.
	 * 
	 * @param isRootParser  **true**, if the parser object is created in order to parse a
	 *                      user-defined file.
	 *                      **false**, if the parser object is created in order to parse a
	 *                      file which has been included by another MidicaPL file.
	 */
	public MidicaPLParser( boolean isRootParser ) {
		this.isRootParser = isRootParser;
		if ( isRootParser )
			refreshSyntax();
	}
	
	/**
	 * Restores the configured MidicaPL keywords and symbols.
	 */
	public static void refreshSyntax() {
		DEFINE         = Dict.getSyntax( Dict.SYNTAX_DEFINE        );
		COMMENT        = Dict.getSyntax( Dict.SYNTAX_COMMENT       );
		GLOBAL         = Dict.getSyntax( Dict.SYNTAX_GLOBAL        );
		P              = Dict.getSyntax( Dict.SYNTAX_P             );
		END            = Dict.getSyntax( Dict.SYNTAX_END           );
		MACRO          = Dict.getSyntax( Dict.SYNTAX_MACRO         );
		INCLUDE        = Dict.getSyntax( Dict.SYNTAX_INCLUDE       );
		INSTRUMENTS    = Dict.getSyntax( Dict.SYNTAX_INSTRUMENTS   );
		BPM            = Dict.getSyntax( Dict.SYNTAX_BPM           );
		OPT_SEPARATOR  = Dict.getSyntax( Dict.SYNTAX_OPT_SEPARATOR );
		OPT_ASSIGNER   = Dict.getSyntax( Dict.SYNTAX_OPT_ASSIGNER  );
		VOLUME         = Dict.getSyntax( Dict.SYNTAX_VOLUME        );
		V              = Dict.getSyntax( Dict.SYNTAX_V             );
		STACCATO       = Dict.getSyntax( Dict.SYNTAX_STACCATO      );
		S              = Dict.getSyntax( Dict.SYNTAX_S             );
		MULTIPLE       = Dict.getSyntax( Dict.SYNTAX_MULTIPLE      );
		M              = Dict.getSyntax( Dict.SYNTAX_M             );
		QUANTITY       = Dict.getSyntax( Dict.SYNTAX_QUANTITY      );
		Q              = Dict.getSyntax( Dict.SYNTAX_Q             );
		PAUSE          = Dict.getSyntax( Dict.SYNTAX_PAUSE         );
		CHORD          = Dict.getSyntax( Dict.SYNTAX_CHORD         );
		INCLUDE_FILE   = Dict.getSyntax( Dict.SYNTAX_INCLUDE_FILE  );
		LENGTH_32      = Dict.getSyntax( Dict.SYNTAX_32            );
		LENGTH_16      = Dict.getSyntax( Dict.SYNTAX_16            );
		LENGTH_8       = Dict.getSyntax( Dict.SYNTAX_8             );
		LENGTH_4       = Dict.getSyntax( Dict.SYNTAX_4             );
		LENGTH_2       = Dict.getSyntax( Dict.SYNTAX_2             );
		LENGTH_1       = Dict.getSyntax( Dict.SYNTAX_1             );
		LENGTH_M1      = Dict.getSyntax( Dict.SYNTAX_M1            );
		LENGTH_M2      = Dict.getSyntax( Dict.SYNTAX_M2            );
		LENGTH_M4      = Dict.getSyntax( Dict.SYNTAX_M4            );
		LENGTH_M8      = Dict.getSyntax( Dict.SYNTAX_M8            );
		LENGTH_M16     = Dict.getSyntax( Dict.SYNTAX_M16           );
		LENGTH_M32     = Dict.getSyntax( Dict.SYNTAX_M32           );
		DOT            = Dict.getSyntax( Dict.SYNTAX_DOT           );
		TRIPLET        = Dict.getSyntax( Dict.SYNTAX_TRIPLET       );
		TUPLET         = Dict.getSyntax( Dict.SYNTAX_TUPLET        );
		TUPLET_FOR     = Dict.getSyntax( Dict.SYNTAX_TUPLET_FOR    );
	}
	
	/**
	 * Parses a MidicaPL source file. Creates a MIDI sequence from that file.
	 * 
	 * @param file  MidicaPL source file.
	 */
	public void parse( File file ) throws ParseException {
		this.file = file;
		// clean up and make parser ready for parsing
		reset();
		
		try {
			// open file for reading
			FileReader     fr         = new FileReader( file );
			BufferedReader br         = new BufferedReader( fr );
			int            lineNumber = 0;
			String         line;
			
			// parse line by line
			while ( null != (line = br.readLine()) ) {
				lineNumber++;
				try {
					parseLine( line );
				}
				catch ( ParseException e ) {
					// Add file name and line number to exception and throw it again
					// but only if this is not yet done.
					// If this information is already available than it comes from
					// another parser instance. In this case we must not overwrite it.
					if ( 0 == e.getLineNumber() )
						e.setLineNumber( lineNumber );
					if ( null == e.getFileName() )
						e.setFileName( file.getCanonicalPath() );
					br.close();
					throw e;
				}
			}
			br.close();
		}
		catch ( FileNotFoundException e ) {
			e.printStackTrace();
		}
		catch ( IOException e ) {
			e.printStackTrace();
		}
		
		// EOF has been reached
		postprocessSequence( SequenceCreator.getSequence(), "midica" );
	}
	
	/**
	 * Parses one single line of a MidicaPL source file.
	 * 
	 * @param line             The line to be parsed.
	 * @throws ParseException  If the line cannot be parsed.
	 */
	private void parseLine( String line ) throws ParseException {
		
		// cut away comments
		String cleanedLine = line.split( Pattern.quote(COMMENT) + "|$", 2 )[ 0 ];
		cleanedLine = clean( cleanedLine ); // eliminate leading and trailing whitespaces
//		System.out.println( cleanedLine ); // TODO: delete
		String[] tokens = getTokens( cleanedLine );
		
		if ( "".equals(tokens[0]) )
			// empty line or only comments
			return;
		
		parseTokens( tokens );
	}
	
	/**
	 * Extracts the tokens of one MidicaPL line.
	 * 
	 * @param line  The line containing the tokens.
	 * @return      The extracted tokens.
	 */
	private String[] getTokens( String line ) {
		String[] tokens = line.split( "\\s+", 3 );
		return tokens;
	}
	
	/**
	 * Parses the tokens of one MidicaPL command.
	 * 
	 * @param  tokens          The tokens from one MidicaPL line.
	 * @throws ParseException  If the tokens cannot be parsed.
	 */
	private void parseTokens( String[] tokens ) throws ParseException {
		
		// replace note, instrument, percussion and drumkit names
		if ( tokens.length > 2  )
			replaceShortcuts( tokens );
		
		// mode command (instruments, macro, end)
		// or definition (define, chord)
		// or call (include, include_file)
		if ( tokens[0].matches("^[A-Za-z_]\\w*$") ) {
			parseModeCmd( tokens );
			return;
		}
		
		// instruments have been parsed already?
		else if ( 0 == instruments.size() && MODE_INSTRUMENTS != currentMode ) {
			throw new ParseException( Dict.get(Dict.ERROR_INSTRUMENTS_NOT_DEFINED) );
		}
		
		// global command?
		if ( tokens[0].matches("^" + Pattern.quote(GLOBAL) + "$") ) {
			if ( MODE_INSTRUMENTS == currentMode )
				// we are inside an instruments definition
				throw new ParseException( Dict.get(Dict.ERROR_GLOBALS_IN_INSTR_DEF) );
			else if ( MODE_MACRO == currentMode )
				// we are inside a macro definition
				currentMacro.add( tokens );
			else
				// we are outside of any special block
				parseGlobalCmd( tokens );
		}
		
		// channel or instruments command
		else if ( tokens[0].matches("^\\d+$") ) {
			
			// instruments command
			if ( MODE_INSTRUMENTS == currentMode ) {
				// we are inside an instruments definition
				parseInstrumentCmd( tokens );
				return;
			}
			
			// channel command with a chord
			if ( chords.containsKey(tokens[1]) ) {
				HashSet<Integer> chordElements = chords.get( tokens[1] );
				int i = 1;
				CHORD_ELEMENT:
				for ( int note : chordElements ) {
					// create and process a new token array for each note of the chord
					String[] subTokens = new String[ 3 ];
					subTokens[ 0 ] = tokens[ 0 ]; // same channel
					subTokens[ 1 ] = Integer.toString( note );
					if ( chordElements.size() == i ) {
						// last note of the chord: prevent volume
						subTokens[ 2 ] = tokens[ 2 ];
					}
					else {
						// add MULTIPLE to the options string
						// so that the next note of the same chord starts at the same time
						subTokens[ 2 ] = addMultiple( tokens[2] );
					}
					parseTokens( subTokens );
					i++;
				}
				return;
			}
			
			// channel command with a single note
			if ( MODE_MACRO == currentMode )
				// we are inside a macro definition
				currentMacro.add( tokens );
			else
				parseChannelCmd( tokens );
		}
		
		else
			throw new ParseException( Dict.get(Dict.ERROR_UNKNOWN_CMD) + tokens[0] );
	}
	
	/**
	 * Replaces shortcuts in normal channel commands.
	 * 
	 * The following shortcuts are replaced:
	 * 
	 * - percussion channel shortcut (this is also replaced for instrument commands)
	 * - note names
	 * - percussion shortcuts
	 * 
	 * @param tokens Channel command token array.
	 */
	private void replaceShortcuts( String[] tokens ) {
		
		// percussion channel shortcut?
		if ( P.equals(tokens[0]) ) {
			tokens[ 0 ] = "9";
		}
		
		// ignore the instruments block - it will be handled
		// separately by replaceInstrument()
		if ( MODE_INSTRUMENTS == currentMode ) {
			return;
		}
		
		// only care about normal channel commands
		if ( ! tokens[0].matches("^\\d{1,2}$") ) {
			return;
		}
		
		// percussion instrument name --> number
		if ( Dict.UNKNOWN_CODE != Dict.getPercussion(tokens[1]) ) {
			tokens[ 1 ] = Integer.toString( Dict.getPercussion(tokens[1]) );
		}
		
		// note name --> number
		else if ( ! "9".equals(tokens[0]) ) {
			if ( Dict.UNKNOWN_CODE != Dict.getNote(tokens[1]) ) {
				tokens[ 1 ] = Integer.toString( Dict.getNote(tokens[1]) );
			}
		}
	}
	
	/**
	 * Transforms an instrument or drumkit name or number string into the
	 * corresponding MIDI number.
	 * 
	 * This is only called while a command inside an INSTRUMENTS block
	 * is parsed.
	 * 
	 * @param shortcut String containing the instrument/drumkit name or number
	 * @param channel  MIDI channel number
	 * @throws ParseException if the given shortcut is neither a valid shortcut nor a valid number.
	 */
	private int replaceInstrument( String shortcut, int channel ) throws ParseException {
		
		// drumkit name --> number
		if ( Dict.UNKNOWN_CODE != Dict.getDrumkit(shortcut) ) {
			shortcut = Integer.toString( Dict.getDrumkit(shortcut) );
		}
		
		// instrument name --> number
		else if ( channel != 9 ) {
			if ( Dict.UNKNOWN_CODE != Dict.getInstrument( shortcut) ) {
				shortcut = Integer.toString( Dict.getInstrument(shortcut) );
			}
		}
		
		int number = toInt( shortcut );
		
		// check range
		if ( number > 127 )
			throw new ParseException( Dict.get(Dict.ERROR_INSTR_BANK) );
		
		return number;
	}
	
	/**
	 * Parses the duration string from a channel command and calculates the
	 * duration in ticks.
	 * 
	 * @param s  The duration string, extracted from the MidicaPL line.
	 * @return   The duration of the note in ticks.
	 * @throws ParseException  If the duration string cannot be parsed.
	 */
	private int parseDuration( String s ) throws ParseException {
		Pattern pattern = Pattern.compile(
			  "^(\\d+|.+?)"                // basic divisor (basic note length)
			+ "(("                         // open capturing group for modifiers
			+ Pattern.quote(TUPLET)        //   customized tuplet
			+ "\\d+"                       //     first number of the customized tuplet
			+ Pattern.quote(TUPLET_FOR)    //     separator between the 2 numbers
			+ "\\d+"                       //     second number of the customized tuplet
			+ "|" + Pattern.quote(TRIPLET) //   ordinary triplet
			+ "|" + Pattern.quote(DOT)     //   dotted
			+ ")*)"                        // close capturing group
			+ "$"
		);
		Matcher matcher = pattern.matcher( s );
		if ( matcher.matches() ) {
			String prefix  = matcher.group( 1 );
			String postfix = matcher.group( 2 );
			int    factor  = 4; // the resolution is for a quarter note but we are based on a full note
			int    divisor = 1;
			
			// parse unmodified note length
			if ( prefix.matches("^\\d+$") )
				divisor = toInt( prefix );
			else if ( LENGTH_32.equals(prefix) )
				divisor = 32;
			else if ( LENGTH_16.equals(prefix) )
				divisor = 16;
			else if ( LENGTH_8.equals(prefix) )
				divisor = 8;
			else if ( LENGTH_4.equals(prefix) )
				divisor = 4;
			else if ( LENGTH_2.equals(prefix) )
				divisor = 2;
			else if ( LENGTH_1.equals(prefix) )
				divisor = 1;
			else if ( LENGTH_M1.equals(prefix) )
				factor = 1;
			else if ( LENGTH_M2.equals(prefix) )
				factor = 2;
			else if ( LENGTH_M4.equals(prefix) )
				factor = 4;
			else if ( LENGTH_M8.equals(prefix) )
				factor = 8;
			else if ( LENGTH_M16.equals(prefix) )
				factor = 16;
			else if ( LENGTH_M32.equals(prefix) )
				factor = 32;
			else
				throw new ParseException( Dict.get(Dict.ERROR_NOTE_LENGTH_INVALID) + s );
			
			// parse modifications by dots
			int dot_count = 0;
			DOT:
			while ( postfix.matches(".*" + Pattern.quote(DOT) + ".*") ) {
				dot_count++;
				postfix = postfix.replaceFirst( Pattern.quote(DOT), "" );
			}
			if ( dot_count > 0 ) {
				// dots modify the note length like this:
				// .: 3/2; ..: 7/4; ...: 15/8, ....: 31/16 and so on
				// in other words: length = 2 * length - ( length / 2 ^ dot_count )
				int power = (int) Math.pow( 2, dot_count );
				factor    = factor  * (2 * power - 1);
				divisor   = divisor * power;
			}
			
			// parse modifications by (nested) triplets
			// this must be before the triplet parsing to support the same symbol
			// for tuplets and triplets
			Pattern tupletPattern = Pattern.compile(
				".*" + Pattern.quote(TUPLET) + "(\\d+)" + Pattern.quote(TUPLET_FOR) + "(\\d+)" + ".*"
			);
			TUPLET:
			while ( postfix.matches(tupletPattern.toString()) ) {
				Matcher tupletMatcher = tupletPattern.matcher( postfix );
				if ( tupletMatcher.matches() ) {
					int count    = toInt( tupletMatcher.group(1), true );
					int countFor = toInt( tupletMatcher.group(2), true );
					// cut away the matched tuplet
					postfix = tupletMatcher.replaceFirst( "" );
					// a tuplet a:b (a for b) modifies the note length by the factor b/a
					factor  *= countFor;
					divisor *= count;
				}
			}
			
			// parse modifications by (nested) triplets
			TRIPLET:
			while ( postfix.matches(".*" + Pattern.quote(TRIPLET) + ".*") ) {
				// cut away the matched triplet
				postfix = postfix.replaceFirst( Pattern.quote(TRIPLET), "" );
				// nested triplets modify the note length by 2/3 for each nesting
				factor  *= 2;
				divisor *= 3;
			}
			
			int resolution = SequenceCreator.getResolution();
			
			// Theoretically: duration = resolution * factor / divisor
			// But integer divisions are always rounded down and we want to round mathematically
			int duration = ( resolution * factor * 10 + 5 ) / ( divisor * 10 );
			return duration;
		}
		else
			// pattern doesn't match
			throw new ParseException( Dict.get(Dict.ERROR_NOTE_LENGTH_INVALID) + s );
	}
	
	/**
	 * Parses a mode command.
	 * That's a command that is neither global nor channel-based.
	 * 
	 * Determines the type of mode command and calls the appropriate method to
	 * parse that command type.
	 * 
	 * @param tokens            Mode command token array.
	 * @throws ParseException   If the mode command cannot be parsed.
	 */
	private void parseModeCmd( String[] tokens ) throws ParseException {
		
		if ( END.equals(tokens[0]) )
			parseEND( tokens );
		
		else if ( MACRO.equals(tokens[0]) )
			parseMACRO( tokens );
		
		else if ( CHORD.equals(tokens[0]) )
			parseCHORD( tokens );
		
		else if ( INCLUDE.equals(tokens[0]) )
			parseINCLUDE( tokens );
		
		else if ( INCLUDE_FILE.equals(tokens[0]) )
			parseINCLUDE_FILE( tokens );
		
		else if ( DEFINE.equals(tokens[0]) )
			parseDEFINE( tokens );
		
		else if ( INSTRUMENTS.equals( tokens[0] ) )
			if ( 1 == tokens.length )
				currentMode = MODE_INSTRUMENTS;
			else
				throw new ParseException( Dict.get(Dict.ERROR_MODE_INSTR_NUM_OF_ARGS) );
		
		// other
		else
			throw new ParseException( Dict.get(Dict.ERROR_UNKNOWN_CMD) + tokens[0] );
	}
	
	/**
	 * Parses an END command.
	 * An END command is a mode command that finishes a previously opened mode.
	 * 
	 * @param tokens             Token array.
	 * @throws ParseException    If the command cannot be parsed.
	 */
	private void parseEND( String[] tokens ) throws ParseException {
		if ( 1 == tokens.length ) {
			if ( MODE_DEFAULT == currentMode )
				throw new ParseException( Dict.get(Dict.ERROR_CMD_END_WITHOUT_BEGIN) );
			if ( MODE_INSTRUMENTS == currentMode )
				// create defined and undefined instruments for all channels
				// but only if not yet done
				if ( ! instrumentsParsed )
					postprocessInstruments();
			currentMode      = MODE_DEFAULT;
			currentMacroName = null;
		}
		else
			throw new ParseException( Dict.get(Dict.ERROR_CMD_END_NUM_OF_ARGS) );
	}
	
	/**
	 * Parses a MACRO command.
	 * A MACRO command is a mode command that opens a MACRO definition.
	 * 
	 * @param tokens             Token array.
	 * @throws ParseException    If the command cannot be parsed.
	 */
	private void parseMACRO( String[] tokens ) throws ParseException {
		if ( MODE_DEFAULT != currentMode )
			throw new ParseException( Dict.get(Dict.ERROR_MACRO_NOT_ALLOWED_HERE) );
		if ( 2 == tokens.length ) {
			currentMode      = MODE_MACRO;
			currentMacroName = tokens[1];
			currentMacro     = new ArrayList<String[]>();
			if ( macros.containsKey(currentMacroName) )
				throw new ParseException( Dict.get(Dict.ERROR_MACRO_ALREADY_DEFINED) + currentMacroName );
			macros.put( currentMacroName, currentMacro );
		}
		else
			throw new ParseException( Dict.get(Dict.ERROR_MACRO_NUM_OF_ARGS) );
	}
	
	/**
	 * Parses a CHORD command.
	 * A CHORD command defines which a new chord name and describes the included notes.
	 * 
	 * @param tokens             Token array.
	 * @throws ParseException    If the command cannot be parsed.
	 */
	private void parseCHORD( String[] tokens ) throws ParseException {
		if ( MODE_DEFAULT != currentMode )
			throw new ParseException( Dict.get(Dict.ERROR_CHORD_DEF_NOT_ALLOWED_HERE) );
		String chordDef;
		if ( 2 == tokens.length )
			// e.g. CHORD crd=c,d,e
			chordDef = tokens[ 1 ];
		else if ( 3 == tokens.length )
			// e.g. CHORD crd = c, d, e
			// or   CHORD crd c d e
			chordDef = tokens[ 1 ] + " " + tokens[ 2 ];
		else
			throw new ParseException( Dict.get(Dict.ERROR_CHORD_NUM_OF_ARGS) );
		
		// get and process chord name
		String[] chordParts = chordDef.split( "[" + Pattern.quote(OPT_ASSIGNER) + "\\s]+", 2 ); // chord name and chords can be separated by OPT_ASSIGNER (e,g, "=") and/or whitespace(s)
		if ( chordParts.length < 2 )
			throw new ParseException( Dict.get(Dict.ERROR_CHORD_NUM_OF_ARGS) );
		String chordName  = chordParts[ 0 ];
		String chordValue = chordParts[ 1 ];
		if ( chords.containsKey(chordName) )
			throw new ParseException( Dict.get(Dict.ERROR_CHORD_ALREADY_DEFINED) + chordName );
		else if ( Dict.noteExists(chordName) )
			throw new ParseException( Dict.get(Dict.ERROR_CHORD_EQUALS_NOTE) + chordName );
		else if ( Dict.percussionExists(chordName) )
			throw new ParseException( Dict.get(Dict.ERROR_CHORD_EQUALS_PERCUSSION) + chordName );
		
		// get and process chord elements
		HashSet<Integer> chord = new HashSet<Integer>();
		String[] notes = chordValue.split( "[" + OPT_SEPARATOR + "\\s]+" ); // notes of the chord can be separated by OPT_ASSIGNER (e,g, "=") and/or whitespace(s)
		
		for ( String note : notes ) {
			int noteVal = parseNote( note );
			if ( chord.contains(noteVal) )
				throw new ParseException( Dict.get(Dict.ERROR_CHORD_CONTAINS_ALREADY) + note );
			chord.add( noteVal );
		}
		chords.put( chordName, chord );
	}
	
	/**
	 * Parses an INCLUDE command.
	 * An INCLUDE command calls a previously defined MACRO.
	 * 
	 * @param tokens             Token array.
	 * @throws ParseException    If the command cannot be parsed.
	 */
	private void parseINCLUDE( String[] tokens ) throws ParseException {
		if ( 2 == tokens.length ) {
			String includeName = tokens[1];
			if ( includeName.equals(currentMacroName) )
				throw new ParseException( Dict.get(Dict.ERROR_MACRO_RECURSION) );
			if ( ! macros.containsKey(includeName) )
				throw new ParseException( Dict.get(Dict.ERROR_MACRO_UNDEFINED) );
			// fetch the right macro
			ArrayList<String[]> macro = macros.get( includeName );
			// apply all commands of the called macro
			for ( int i=0; i<macro.size(); i++ ) {
				String[] macroTokens = macro.get( i );
				parseTokens( macroTokens );
			}
		}
		else
			throw new ParseException( Dict.get(Dict.ERROR_INCLUDE_NUM_OF_ARGS) );
	}
	
	/**
	 * Parses an INCLUDE_FILE command.
	 * An INCLUDE_FILE command parses another MidicaPL source file. Therefore
	 * another parser instance of this class is created.
	 * 
	 * @param tokens             Token array.
	 * @throws ParseException    If the command cannot be parsed.
	 */
	private void parseINCLUDE_FILE( String[] tokens ) throws ParseException {
		if ( 2 == tokens.length ) {
			try {
				// create File object with absolute path for include file
				String inclPath = tokens[ 1 ];
				File inclFile   = new File( inclPath );
				if ( ! inclFile.isAbsolute() ) {
					File currentDir = file.getParentFile();
					inclFile = new File(
						currentDir.getCanonicalPath(), // parent
						inclPath                       // child
					);
				}
				
				// check if the file can be parsed
				if ( ! inclFile.exists() )
					throw new ParseException( Dict.get(Dict.ERROR_FILE_EXISTS) + inclFile.getCanonicalPath() );
				if ( ! inclFile.isFile() )
					throw new ParseException( Dict.get(Dict.ERROR_FILE_NORMAL) + inclFile.getCanonicalPath() );
				if ( ! inclFile.canRead() )
					throw new ParseException( Dict.get(Dict.ERROR_FILE_READABLE) + inclFile.getCanonicalPath() );
				
				// parse it
				MidicaPLParser parser = new MidicaPLParser( false );
				parser.parse( inclFile );
			}
			catch( IOException e ) {
				throw new ParseException( Dict.get(Dict.ERROR_FILE_IO) + e.getMessage() );
			}
		}
		else
			throw new ParseException( Dict.get(Dict.ERROR_FILE_NUM_OF_ARGS) );
	}
	
	/**
	 * Parses a DEFINE command.
	 * A DEFINE command is used to re-define a mode command name.
	 * 
	 * @param tokens             Token array.
	 * @throws ParseException    If the command cannot be parsed.
	 */
	private void parseDEFINE( String[] tokens ) throws ParseException {
		String def;
		if ( 2 == tokens.length )
			// e.g. DEFINE DEFINE=def
			def = tokens[ 1 ];
		else if ( 3 == tokens.length )
			// e.g. DEFINE DEFINE = def
			// or   DEFINE DEFINE def
			def = tokens[ 1 ] + " " + tokens[ 2 ];
		else
			throw new ParseException( Dict.get(Dict.ERROR_DEFINE_NUM_OF_ARGS) );
		
		// split definition string by OPT_ASSIGNER (e,g, "=") and/or whitespace(s)
		String[] defParts = def.split( "[" + Pattern.quote(OPT_ASSIGNER) + "\\s]+", 2 );
		if ( defParts.length < 2 )
			throw new ParseException( Dict.get(Dict.ERROR_DEFINE_NUM_OF_ARGS) );
		String cmdId      = clean( defParts[0] );
		String cmdName    = clean( defParts[1] );
		if ( cmdName.matches("\\s") )
			throw new ParseException( Dict.get(Dict.ERROR_DEFINE_NUM_OF_ARGS) );
		
		if ( DEFINE.equals(cmdId) )
			DEFINE = cmdName;
		else if ( COMMENT.equals(cmdId) )
			COMMENT = cmdName;
		else if ( GLOBAL.equals(cmdId) )
			GLOBAL = cmdName;
		else if ( P.equals(cmdId) )
			P = cmdName;
		else if ( END.equals(cmdId) )
			END = cmdName;
		else if ( MACRO.equals(cmdId) )
			MACRO = cmdName;
		else if ( INCLUDE.equals(cmdId) )
			INCLUDE = cmdName;
		else if ( INSTRUMENTS.equals(cmdId) )
			INSTRUMENTS = cmdName;
		else if ( BPM.equals(cmdId) )
			BPM = cmdName;
		else if ( OPT_SEPARATOR.equals(cmdId) )
			OPT_SEPARATOR = cmdName;
		else if ( OPT_ASSIGNER.equals(cmdId) )
			OPT_ASSIGNER = cmdName;
		else if ( VOLUME.equals(cmdId) )
			VOLUME = cmdName;
		else if ( V.equals(cmdId) )
			V = cmdName;
		else if ( STACCATO.equals(cmdId) )
			STACCATO = cmdName;
		else if ( S.equals(cmdId) )
			S = cmdName;
		else if ( MULTIPLE.equals(cmdId) )
			MULTIPLE = cmdName;
		else if ( M.equals(cmdId) )
			M = cmdName;
		else if ( QUANTITY.equals(cmdId) )
			QUANTITY = cmdName;
		else if ( Q.equals(cmdId) )
			Q = cmdName;
		else if ( PAUSE.equals(cmdId) )
			PAUSE = cmdName;
		else if ( CHORD.equals(cmdId) )
			CHORD = cmdName;
		else if ( INCLUDE_FILE.equals(cmdId) )
			INCLUDE_FILE = cmdName;
		else if ( LENGTH_32.equals(cmdId) )
			LENGTH_32 = cmdName;
		else if ( LENGTH_16.equals(cmdId) )
			LENGTH_16 = cmdName;
		else if ( LENGTH_8.equals(cmdId) )
			LENGTH_8 = cmdName;
		else if ( LENGTH_4.equals(cmdId) )
			LENGTH_4 = cmdName;
		else if ( LENGTH_2.equals(cmdId) )
			LENGTH_2 = cmdName;
		else if ( LENGTH_1.equals(cmdId) )
			LENGTH_1 = cmdName;
		else if ( LENGTH_M1.equals(cmdId) )
			LENGTH_M1 = cmdName;
		else if ( LENGTH_M2.equals(cmdId) )
			LENGTH_M2 = cmdName;
		else if ( LENGTH_M4.equals(cmdId) )
			LENGTH_M4 = cmdName;
		else if ( LENGTH_M8.equals(cmdId) )
			LENGTH_M8 = cmdName;
		else if ( LENGTH_M16.equals(cmdId) )
			LENGTH_M16 = cmdName;
		else if ( LENGTH_M32.equals(cmdId) )
			LENGTH_M32 = cmdName;
		else if ( DOT.equals(cmdId) )
			DOT = cmdName;
		else if ( TRIPLET.equals(cmdId) )
			TRIPLET = cmdName;
		else if ( TUPLET.equals(cmdId) )
			TUPLET = cmdName;
		else if ( TUPLET_FOR.equals(cmdId) )
			TUPLET_FOR = cmdName;
	}
	
	/**
	 * Parses an instrument command inside an INSTRUMENTS block.
	 * This command assigns a certain instrument to a certain channel.
	 * A bank number can also be assigned for the channel.
	 * 
	 * TODO: test soundfonts with bankLSB > 0
	 * 
	 * @param tokens             Token array.
	 * @throws ParseException    If the command cannot be parsed.
	 */
	private void parseInstrumentCmd( String[] tokens ) throws ParseException{
		if ( 3 != tokens.length )
			throw new ParseException( Dict.get(Dict.ERROR_INSTR_NUM_OF_ARGS) );
		
		int    channel   = toChannel( tokens[0] );
		String instrStr  = tokens[ 1 ];
		String instrName = tokens[ 2 ];
		int    bankMSB   = 0;
		int    bankLSB   = 0;
		
		// program number and banks
		String [] instrBank = instrStr.split( Dict.getSyntax(Dict.SYNTAX_PROG_BANK_SEP), 2 );
		int       instrNum  = replaceInstrument( instrBank[0], channel );
		if ( 1 == instrBank.length ) {
			// only program number, no bank defined - nothing more to do
		}
		else if ( 2 == instrBank.length ) {
			// program number and bank are both defined
			
			// bank MSB / LSB / full number
			String[] msbLsb = instrBank[ 1 ].split( Dict.getSyntax(Dict.SYNTAX_BANK_SEP), 2 );
			bankMSB         = toInt( msbLsb[0] );
			if ( 1 == msbLsb.length ) {
				if ( bankMSB > 127 * 127 ) {
					// too big
					throw new ParseException( Dict.get(Dict.ERROR_INSTR_BANK) );
				}
				else if ( bankMSB > 127 ) {
					// full number
					bankLSB   = bankMSB &  0b00000000_01111111;
					bankMSB >>= 7;
				}
				else {
					// only MSB - nothing more to do
				}
			}
			else {
				// MSB and LSB
				bankLSB = toInt( msbLsb[1] );
			}
		}
		
		// wrong syntax?
		if ( bankMSB > 127 || bankLSB > 127 ) {
			throw new ParseException( Dict.get(Dict.ERROR_INSTR_BANK) );
		}
		
		// TODO: delete
//		System.out.println( "instrNum: " + instrNum + ", MSB: " + bankMSB + ", LSB: " + bankLSB + ", full: " + ((bankMSB << 7) | bankLSB) );
		
		if (instrumentsParsed) {
			// instruments are already parsed
			// this is an instrument change command for the channel
			Instrument instr       = instruments.get( channel );
			instr.autoChannel      = false;
			instr.instrumentNumber = instrNum;
			instr.instrumentName   = instrName;
			long tick              = instr.getCurrentTicks();
			
			boolean[] isChanged = instr.setBank( bankMSB, bankLSB );
			
			try {
				// calculate tick for bank select
				long bankTick = tick - TICK_BANK_BEFORE_PROGRAM;
				if ( bankTick < 0 )
					bankTick = 0;
				
				// bank select, if necessary
				if ( isChanged[0] )
					SequenceCreator.setBank( channel, bankTick, bankMSB, false );
				if ( isChanged[1] )
					SequenceCreator.setBank( channel, bankTick, bankLSB, true );
				
				// program change and instrument name
				SequenceCreator.initChannel( channel, instrNum, instrName, tick );
			}
			catch ( InvalidMidiDataException e ) {
				throw new ParseException( Dict.get(Dict.ERROR_MIDI_PROBLEM) + e.getMessage() );
			}
		}
		else {
			// create and add instrument
			Instrument instr = new Instrument(
				channel,
				instrNum,
				instrName,
				false      // no auto channel
			);
			instr.setBank( bankMSB, bankLSB );
			instruments.add( instr );
		}
	}
	
	/**
	 * Parses a global command.
	 * Global commands apply to every channel. So they always contain a syncronization
	 * of the current ticks of each channel.
	 * 
	 * @param tokens             Token array.
	 * @throws ParseException    If the command cannot be parsed.
	 */
	private void parseGlobalCmd( String[] tokens ) throws ParseException {
		synchronize();
		long currentTicks = instruments.get( 0 ).getCurrentTicks();
		
		int tokenCount = tokens.length;
		if ( 1 == tokenCount )
			return;
		else if ( 3 != tokenCount )
			throw new ParseException( Dict.get(Dict.ERROR_GLOBAL_NUM_OF_ARGS) );
		
		String cmd   = tokens[1];
		String value = tokens[2];
		
		try {
			// set
			if ( cmd.equals(BPM) ) {
				int bpm = toInt( value, true );
				SequenceCreator.addMessageBpm( bpm, currentTicks );
			}
			else
				throw new ParseException( Dict.get(Dict.ERROR_UNKNOWN_GLOBAL_CMD) + cmd );
		}
		catch ( InvalidMidiDataException e ) {
			throw new ParseException( Dict.get(Dict.ERROR_MIDI_PROBLEM) + e.getMessage() );
		}
	}
	
	/**
	 * Parses a channel command.
	 * A channel command is a channel-based command like a note or a pause.
	 * 
	 * @param tokens             Token array.
	 * @throws ParseException    If the command cannot be parsed.
	 */
	private void parseChannelCmd( String[] tokens ) throws ParseException {
		
		int tokenCount = tokens.length;
		if ( tokenCount < 3 )
			throw new ParseException( Dict.get(Dict.ERROR_CH_CMD_NUM_OF_ARGS) );
		
		int channel  = toInt( tokens[0] );
		int note     = parseNote( tokens[1], channel );
		
		// separate the duration from further arguments
		String[] subTokens = tokens[ 2 ].split( "\\s+", 2 );
		if ( 0 == subTokens.length )
			throw new ParseException( Dict.get(Dict.ERROR_CH_CMD_NUM_OF_ARGS) );
		
		// process duration
		String durationStr = subTokens[ 0 ];
		int duration = parseDuration( durationStr );
		
		// process options
		HashMap<String, Integer> options = new HashMap<String, Integer>();
		if ( 2 == subTokens.length )
			options = parseOptions( subTokens[1] );
		
		// apply volume option
		if ( options.containsKey(OPT_VOLUME) ) {
			int volume = options.get( OPT_VOLUME );
			instruments.get( channel ).setVolume( volume );
		}
		// apply staccato option
		if ( options.containsKey(OPT_STACCATO) ) {
			int staccato = options.get( OPT_STACCATO );
			instruments.get( channel ).setStaccato( staccato );
		}
		
		// determine if more notes for this channel are expected at the same time
		boolean multiple = false;
		if ( options.containsKey(OPT_MULTIPLE) )
			multiple = true;
		
		// determine how often to play the note(s)
		int quantity = 1;
		if ( options.containsKey(OPT_QUANTITY) )
			quantity = options.get( OPT_QUANTITY );
		
		// get instrument
		Instrument instr = instruments.get( channel );
		
		if ( instr.autoChannel )
			throw new ParseException(
				String.format( Dict.get(Dict.ERROR_CHANNEL_UNDEFINED), channel )
			);
		
		// get start ticks of the first note and volume
		long absoluteStartTicks = instr.getCurrentTicks();
		int  volume             = instr.getVolume();
		
		NOTE_QUANTITY:
		for ( int i = 0; i < quantity; i++ ) {
			if ( PAUSE_VALUE == note ) {
				instr.incrementTicks( duration );
				return;
			}
			
			// calculate beginning and end ticks of the current note
			long startTicks = instr.getCurrentTicks();
			long endTicks   = instr.addNote( duration );
			
			// create and add messages
			try {
				int newNote = transpose( note, channel );
				SequenceCreator.addMessageKeystroke( channel, newNote, startTicks, endTicks, volume );
			}
			catch ( InvalidMidiDataException e ) {
				throw new ParseException( Dict.get(Dict.ERROR_MIDI_PROBLEM) + e.getMessage() );
			}
		}
		
		if ( multiple )
			instr.setCurrentTicks( absoluteStartTicks );
	}
	
	/**
	 * Parses the options part of a channel command.
	 * 
	 * @param optString    The options string of the channel command to be parsed.
	 * @return             All options and their values that have been found in the
	 *                     provided options string.
	 * @throws ParseException    If the command cannot be parsed.
	 */
	private HashMap<String, Integer> parseOptions( String optString ) throws ParseException {
		HashMap<String, Integer> options = new HashMap<String, Integer>();
		
		String[] optTokens = optString.split( OPT_SEPARATOR );
		for ( String opt : optTokens ) {
			opt = clean( opt );
			String[] optParts = opt.split( "[" + Pattern.quote(OPT_ASSIGNER) + "\\s]+" ); // name and value can be separated by OPT_ASSIGNER (e,g, "=") and/or whitespace(s)
			if ( optParts.length > 2 )
				throw new ParseException( Dict.get(Dict.ERROR_CANT_PARSE_OPTIONS) );
			// construct name and value
			String optName  = optParts[ 0 ];
			int    optValue = 0;
			if ( V.equals(optName) || VOLUME.equals(optName) ) {
				optName = OPT_VOLUME;
				optValue = toInt( optParts[1] );
				if ( optValue > 127 )
					throw new ParseException( Dict.get(Dict.ERROR_VOL_NOT_MORE_THAN_127) );
			}
			else if ( S.equals(optName) || STACCATO.equals(optName) ) {
				optName = OPT_STACCATO;
				optValue = toInt( optParts[1], false );
			}
			else if ( Q.equals(optName) || QUANTITY.equals(optName) ) {
				optName = OPT_QUANTITY;
				optValue = toInt( optParts[1], false );
			}
			else if ( M.equals(optName) || MULTIPLE.equals(optName) ) {
				optName = OPT_MULTIPLE;
			}
			else {
				throw new ParseException( Dict.get(Dict.ERROR_UNKNOWN_OPTION) + optName );
			}
			options.put( optName, optValue );
		}
		
		return options;
	}
	
	/**
	 * Returns the note value of a given string in the context of the given channel.
	 * (Used to parse a note from a channel command)
	 * 
	 * The string can be:
	 * 
	 * - the pause character (in this case PAUSE_VALUE is returned)
	 * - the note value in numeric characters
	 * - the note name as defined by the chosen configuration (if the channel is not the percussion channel)
	 * - the percussion name as defined by the chosen configutation (if channel is 9)
	 * 
	 * @param note       The note string to be parsed.
	 * @param channel    The channel number.
	 * @return MIDI note value of the given note
	 * @throws ParseExceptionif the note or percussion name is unknown or the note value is out of the legal range for MIDI notes
	 */
	private int parseNote( String note, int channel ) throws ParseException {
		if ( note.equals(PAUSE) )
			return PAUSE_VALUE;
		else if ( note.matches("^\\d+$") )
			return toInt( note );
		else {
			if ( 9 == channel )
				throw new ParseException( Dict.get(Dict.ERROR_UNKNOWN_PERCUSSION) + note );
			throw new ParseException( Dict.get(Dict.ERROR_UNKNOWN_NOTE) + note );
		}
	}
	
	/**
	 * Returns the note value of a given string.
	 * (Used for parsing a note from a chord definition)
	 * 
	 * The string can be:
	 * 
	 * - the note value in numeric characters
	 * - the note name as defined by the chosen configuration
	 * 
	 * @param noteName string defining the note
	 * @return MIDI note value of the given note
	 * @throws ParseException if the note name is unknown or the note value is out of the legal range for MIDI notes
	 */
	private int parseNote( String noteName ) throws ParseException {
		int noteVal;
		if ( noteName.matches("^\\d+$") )
			noteVal = toInt( noteName );
		else {
			noteVal = Dict.getNote( noteName );
			if ( Dict.UNKNOWN_CODE == noteVal )
				throw new ParseException( Dict.get(Dict.ERROR_UNKNOWN_NOTE) + noteName );
		}
		if ( noteVal > 127 )
			throw new ParseException( Dict.get(Dict.ERROR_NOTE_TOO_BIG) + noteName );
		
		return noteVal;
	}
	
	/**
	 * Adds the option MULTIPLE to an option string.
	 * This is used if a note of a predefined chord is produced - so that the next note
	 * of the same chord starts at the same tick.
	 * 
	 * @param original token containing the volume and the option string: token[2]
	 * @return same token but with the MULTIPLE option
	 */
	private String addMultiple( String original ) {
		
		String[] subTokens = original.split( "\\s+", 2 );
		String optStr;
		if ( subTokens.length > 1 )
			optStr = subTokens[ 1 ];
		else
			return subTokens[ 0 ] + " " + MULTIPLE;
		
		// cut away trailing whitespaces and option separators (e.g. ',')
		String cleanedOptString = optStr.replaceFirst( "[" + Pattern.quote(OPT_SEPARATOR) + "\\s]+$", "" );
		
		// append the option MULTIPLE
		subTokens[ 1 ] = cleanedOptString + OPT_SEPARATOR + MULTIPLE;
		
		return subTokens[ 0 ] + " " + subTokens[ 1 ];
	}

	/**
	 * Synchronizes all instruments.
	 * Sets the current ticks of each channel to the value of the channel with the maximum
	 * current tick value.
	 */
	private void synchronize() {
		long maxTicks = Instrument.getMaxCurrentTicks( instruments );
		for ( int i=0; i<instruments.size(); i++ ) {
			instruments.get( i ).setCurrentTicks( maxTicks );
		}
	}
	
	/**
	 * Postprocesses a finished INSTRUMENTS block.
	 * Initializes the percussion channel.
	 * Initializes all necessary data structures for all channels. Thereby all undefined
	 * channels will be initialized with a fake instrument so that the data structures work.
	 * 
	 * @throws ParseException    If the command cannot be parsed.
	 */
	private void postprocessInstruments() throws ParseException {
		
		if ( 0 == instruments.size() )
			throw new ParseException( Dict.get(Dict.ERROR_INSTRUMENTS_NOT_DEFINED) );
		
		// sort instruments ascending
		Collections.sort( instruments );
		
		// find out which channels are missing
		HashSet<Integer> missing = new HashSet<Integer>();
		for ( int i=0; i<MidiDevices.NUMBER_OF_CHANNELS; i++ )
			missing.add( i );
		for ( Instrument instr : instruments )
			missing.remove( instr.channel );
		
		// add the missing channels
		for ( int i : missing ) {
			Instrument fakeInstr;
			if ( 9 == i )
				fakeInstr = new Instrument( i, 0, Dict.get(Dict.PERCUSSION_CHANNEL), false );
			else
				fakeInstr = new Instrument( i, 0, Dict.get(Dict.DEFAULT_CHANNEL_COMMENT), true );
			instruments.add( fakeInstr );
		}
		
		// sort again
		Collections.sort( instruments );
		
		// initialize sequence
		try {
			SequenceCreator.reset();
			for ( Instrument instr : instruments  ) {
				int    channel      = instr.channel;
				int    instrNum     = instr.instrumentNumber;
				String instrComment = instr.instrumentName;
				int    bankMSB      = instr.getBankMSB();
				int    bankLSB      = instr.getBankLSB();
				// reset instrument
				instr.resetCurrentTicks();
				if ( ! instr.autoChannel ) {
					if ( bankMSB != 0 )
						SequenceCreator.setBank( channel, 0L, bankMSB, false );
					if ( bankLSB != 0 )
						SequenceCreator.setBank( channel, 0L, bankLSB, true );
					SequenceCreator.initChannel( channel, instrNum, instrComment, SequenceCreator.NOW );
				}
			}
		}
		catch ( InvalidMidiDataException e ) {
			throw new ParseException( Dict.get(Dict.ERROR_MIDI_PROBLEM) + e.getMessage() );
		}
		
		instrumentsParsed = true;
	}
	
	/**
	 * Parses a String that is expected to be an integer.
	 * 
	 * - If greaterZero is true: Accepts only values > 0
	 * - If greaterZero is false: Accepts everything (including negative values)
	 * 
	 * @param s              The string to be parsed.
	 * @param greaterZero    **true**, if only positive integers are allowed.
	 *                       **false**, if negative values are allowed.
	 * @return               The parsed value.
	 * @throws ParseException    If the string cannot be parsed.
	 */
	private int toInt( String s, boolean greaterZero ) throws ParseException {
		int i;
		if ( greaterZero ) {
			i = toInt( s );
			if ( 0 == i )
				throw new ParseException( Dict.get(Dict.ERROR_0_NOT_ALLOWED) );
		}
		else {
			i = Integer.parseInt( s );
		}
		return i;
	}
	
	/**
	 * Parses a String that is expected to be an integer, accepting only values >= 0.
	 * 
	 * @param s              The string to be parsed.
	 * @return               The parsed value.
	 * @throws ParseException    If the string cannot be parsed.
	 */
	private int toInt( String s ) throws ParseException {
		try {
			int i = Integer.parseInt( s );
			if ( i < 0 ) {
				throw new ParseException( Dict.get(Dict.ERROR_NEGATIVE_NOT_ALLOWED) + s );
			}
			return i;
		}
		catch ( NumberFormatException e ) {
			throw new ParseException( Dict.get(Dict.ERROR_NOT_AN_INTEGER) + s );
		}
	}
	
	/**
	 * Parses a channel number.
	 * 
	 * @param s    Channel number string.
	 * @return     Parsed channel number.
	 * @throws ParseException    If the command cannot be parsed.
	 */
	private int toChannel( String s ) throws ParseException {
		int channel = toInt( s );
		if ( channel > 15 )
			throw new ParseException( Dict.get(Dict.ERROR_INVALID_CHANNEL_NUMBER) + s );
		return channel;
	}
	
	/**
	 * Cuts away any leading and trailing whitespaces from an input string
	 * and returns the resulting string.
	 * 
	 * @param input    The string to be cleaned.
	 * @return         Resulting string without leading or trailing whitespaces.
	 */
	private String clean( String input ) {
		input = input.replaceFirst( "^\\s+", "" ); // eliminate leading whitespaces
		input = input.replaceFirst( "\\s+$", "" ); // eliminate trailing whitespaces
		return input;
	}
	
	/**
	 * Initializes the parser for a new parsing run.
	 * This is called at the beginning of parse().
	 */
	private void reset() {
		// reset fields
		currentMode      = MODE_DEFAULT;
		currentMacroName = null;
		
		if (isRootParser) {
			instrumentsParsed = false;
			instruments       = new ArrayList<Instrument>();
			macros            = new HashMap<String, ArrayList<String[]>>();
			chords            = new HashMap<String, HashSet<Integer>>();
			refreshSyntax();
		}
	}
}
