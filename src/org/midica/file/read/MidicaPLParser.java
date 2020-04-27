/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file.read;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.midi.InvalidMidiDataException;

import org.midica.Midica;
import org.midica.config.Config;
import org.midica.config.Dict;
import org.midica.file.Instrument;
import org.midica.midi.LyricUtil;
import org.midica.midi.MidiDevices;
import org.midica.midi.SequenceCreator;
import org.midica.ui.model.ComboboxStringOption;
import org.midica.ui.model.ConfigComboboxModel;

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
	public static final String OPT_VELOCITY = "velocity";
	public static final String OPT_MULTIPLE = "multiple";
	public static final String OPT_DURATION = "duration";
	public static final String OPT_QUANTITY = "quantity";
	public static final String OPT_LYRICS   = "lyrics";
	public static final String OPT_TUPLET   = "tuplet";
	public static final String OPT_TREMOLO  = "tremolo";
	public static final String OPT_SHIFT    = "shift";
	public static final String OPT_IF       = "if";
	public static final String OPT_ELSIF    = "elsif";
	public static final String OPT_ELSE     = "else";
	
	private static final int MODE_DEFAULT      = 0;
	private static final int MODE_INSTRUMENTS  = 1;
	private static final int MODE_FUNCTION     = 2;
	private static final int MODE_PATTERN      = 3;
	private static final int MODE_META         = 4;
	private static final int MODE_SOFT_KARAOKE = 5;
	
	public static final  int COND_TYPE_NONE   = 0;
	public static final  int COND_TYPE_IF     = 1;
	public static final  int COND_TYPE_ELSIF  = 2;
	public static final  int COND_TYPE_ELSE   = 3;
	
	private static final int TICK_BANK_BEFORE_PROGRAM = 10; // how many ticks a bank select will be made before the program change
	
	private static final int REST_VALUE = -1;
	
	private static final int MAX_RECURSION_DEPTH_FUNCTION =   30;
	private static final int MAX_RECURSION_DEPTH_PATTERN  =    5;
	private static final int MAX_RECURSION_DEPTH_CONST    = 1000;
	private static final int MAX_RECURSION_DEPTH_VAR      = 1000;
	
	/* *****************
	 * class fields
	 *******************/
	
	public static String BANK_SEP           = null;
	public static String TEMPO              = null;
	public static String TIME_SIG           = null;
	public static String TIME_SIG_SLASH     = null;
	public static String KEY_SIG            = null;
	public static String KEY_SEPARATOR      = null;
	public static String KEY_MAJ            = null;
	public static String KEY_MIN            = null;
	public static String PARTIAL_SYNC_RANGE = null;
	public static String PARTIAL_SYNC_SEP   = null;
	public static String CHORD              = null;
	public static String CHORD_ASSIGNER     = null;
	public static String CHORD_SEPARATOR    = null;
	public static String COMMENT            = null;
	public static String CONST              = null;
	public static String VAR                = null;
	public static String VAR_ASSIGNER       = null;
	public static String VAR_SYMBOL         = null;
	public static String DEFINE             = null;
	public static String DOT                = null;
	public static String END                = null;
	public static String BLOCK_OPEN         = null;
	public static String BLOCK_CLOSE        = null;
	public static String GLOBAL             = null;
	public static String CALL               = null;
	public static String INCLUDE            = null;
	public static String SOUNDFONT          = null;
	public static String INSTRUMENT         = null;
	public static String INSTRUMENTS        = null;
	public static String META               = null;
	public static String META_COPYRIGHT     = null;
	public static String META_TITLE         = null;
	public static String META_COMPOSER      = null;
	public static String META_LYRICIST      = null;
	public static String META_ARTIST        = null;
	public static String META_SOFT_KARAOKE  = null;
	public static String META_SK_VERSION    = null;
	public static String META_SK_LANG       = null;
	public static String META_SK_TITLE      = null;
	public static String META_SK_AUTHOR     = null;
	public static String META_SK_COPYRIGHT  = null;
	public static String META_SK_INFO       = null;
	public static String LENGTH_32          = null;
	public static String LENGTH_16          = null;
	public static String LENGTH_8           = null;
	public static String LENGTH_4           = null;
	public static String LENGTH_2           = null;
	public static String LENGTH_1           = null;
	public static String LENGTH_M1          = null;
	public static String LENGTH_M2          = null;
	public static String LENGTH_M4          = null;
	public static String LENGTH_M8          = null;
	public static String LENGTH_M16         = null;
	public static String LENGTH_M32         = null;
	public static String L                  = null;
	public static String LYRICS             = null;
	public static String LYRICS_SPACE       = null;
	public static String LYRICS_CR          = null;
	public static String LYRICS_LF          = null;
	public static String LYRICS_COMMA       = null;
	public static String FUNCTION           = null;
	public static String PATTERN            = null;
	public static String PATTERN_INDEX_SEP  = null;
	public static String PARAM_OPEN         = null;
	public static String PARAM_CLOSE        = null;
	public static String PARAM_SEPARATOR    = null;
	public static String PARAM_ASSIGNER     = null;
	public static String PARAM_NAMED_OPEN   = null;
	public static String PARAM_NAMED_CLOSE  = null;
	public static String PARAM_INDEX_OPEN   = null;
	public static String PARAM_INDEX_CLOSE  = null;
	public static String M                  = null;
	public static String MULTIPLE           = null;
	public static String OPT_ASSIGNER       = null;
	public static String OPT_SEPARATOR      = null;
	public static String P                  = null;
	public static String REST               = null;
	public static String S                  = null;
	public static String SHIFT              = null;
	public static String IF                 = null;
	public static String ELSIF              = null;
	public static String ELSE               = null;
	public static String COND_EQ            = null;
	public static String COND_NEQ           = null;
	public static String COND_NDEF          = null;
	public static String COND_LT            = null;
	public static String COND_LE            = null;
	public static String COND_GT            = null;
	public static String COND_GE            = null;
	public static String COND_IN            = null;
	public static String COND_IN_SEP        = null;
	public static String PROG_BANK_SEP      = null;
	public static String Q                  = null;
	public static String QUANTITY           = null;
	public static String D                  = null;
	public static String DURATION           = null;
	public static String DURATION_PERCENT   = null;
	public static String TR                 = null;
	public static String TREMOLO            = null;
	public static String T                  = null;
	public static String TUPLET             = null;
	public static String V                  = null;
	public static String VELOCITY           = null;
	public static String TRIPLET            = null;
	public static String TUPLET_INTRO       = null;
	public static String TUPLET_FOR         = null;
	public static String LENGTH_PLUS        = null;
	
	public static String ORIGINAL_DEFINE  = null;
	public static String ORIGINAL_INCLUDE = null;
	public static String ORIGINAL_COMMENT = null;
	
	protected static ArrayList<Instrument> instruments = null;
	
	private static LyricUtil lyricUtil = LyricUtil.getInstance();
	
	private static Pattern whitespace = Pattern.compile("\\s+");
	
	private   static HashMap<String, ArrayList<String>> fileCache            = null;
	private   static HashMap<String, ArrayList<String>> functions            = null;
	private   static HashMap<String, File>              functionToFile       = null;
	private   static HashMap<String, Integer>           functionToLineOffset = null;
	public    static HashMap<String, ArrayList<String>> patterns             = null;
	private   static HashMap<String, File>              patternToFile        = null;
	private   static HashMap<String, Integer>           patternToLineOffset  = null;
	private   static TreeMap<String, TreeSet<Integer>>  chords               = null;
	private   static boolean                            instrumentsParsed    = false;
	private   static HashMap<String, StringBuilder>     metaInfo             = null;
	private   static HashMap<String, ArrayList<String>> softKaraokeInfo      = null;
	private   static boolean                            frstInstrBlkOver     = false;
	private   static String                             chosenCharset        = null;
	private   static HashSet<String>                    definedFunctionNames = null;
	private   static HashSet<String>                    definedPatternNames  = null;
	private   static int                                nestableBlkDepth     = 0;
	private   static Deque<NestableBlock>               nestableBlkStack     = null;
	private   static Deque<StackTraceElement>           stackTrace           = null;
	private   static Deque<String>                      functionNameStack    = null;
	private   static Deque<Integer>                     functionLineStack    = null;
	private   static Deque<File>                        functionFileStack    = null;
	private   static Deque<HashMap<String, String>>     paramStackNamed      = null;
	private   static Deque<ArrayList<String>>           paramStackIndexed    = null;
	private   static Deque<String>                      patternNameStack     = null;
	private   static Deque<Integer>                     patternLineStack     = null;
	private   static Deque<File>                        patternFileStack     = null;
	private   static HashSet<String>                    redefinitions        = null;
	private   static boolean                            soundfontParsed      = false;
	protected static HashMap<String, String>            constants            = null;
	protected static HashMap<String, String>            variables            = null;
	private   static Pattern                            varPattern           = null;
	private   static Pattern                            callPattern          = null;
	private   static Pattern                            condPattern          = null;
	private   static Pattern                            condInPattern        = null;
	private   static Pattern                            crlfSkPattern        = null;
	private   static boolean                            isSoftKaraoke        = false;
	private   static boolean                            isPatternSubChord    = false;
	
	private static boolean isDefineParsRun     = false; // parsing run for define commands
	private static boolean isConstParsRun      = false; // parsing run for constant definitions
	private static boolean isChInstMetaParsRun = false; // parsing run for chords, meta, instruments and block nesting
	private static boolean isFuncNameParsRun   = false; // parsing run for defined function and pattern names
	private static boolean isFuncParsRun       = false; // parsing run for functions and patterns
	private static boolean isCondCheckParsRun  = false; // parsing run for pre-checks of if/elsif conditions
	private static boolean isDefaultParsRun    = false; // final parsing run
	
	/* *******************
	 * instance fields
	 *********************/
	
	private File              file                = null;
	private int               currentLineNumber   = 0;
	private String            currentLineContent  = null;
	private int               currentMode         = MODE_DEFAULT;
	private String            currentFunctionName = null;
	private ArrayList<String> currentFunction     = null;
	private String            currentPatternName  = null;
	private ArrayList<String> currentPattern      = null;
	private boolean           condChainOpened     = false; // current / last block had an if or elsif
	private boolean           condChainHit        = false; // if / elsif chain had a hit
	
	/**
	 * Returns the absolute path of the successfully parsed MidicaPL file.
	 * Returns **null**, if no file has been successfully parsed or the successfully parsed file
	 * is not a MidicaPL file.
	 * 
	 * @return file path or **null**.
	 */
	public static String getFilePath() {
		return getFilePath(FORMAT_MIDICAPL);
	}
	
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
		if (isRootParser) {
			refreshSyntax();
		}
	}
	
	/**
	 * Restores the configured MidicaPL keywords and symbols.
	 */
	public static void refreshSyntax() {
		BANK_SEP           = Dict.getSyntax( Dict.SYNTAX_BANK_SEP           );
		TEMPO              = Dict.getSyntax( Dict.SYNTAX_TEMPO              );
		TIME_SIG           = Dict.getSyntax( Dict.SYNTAX_TIME_SIG           );
		TIME_SIG_SLASH     = Dict.getSyntax( Dict.SYNTAX_TIME_SIG_SLASH     );
		KEY_SIG            = Dict.getSyntax( Dict.SYNTAX_KEY_SIG            );
		KEY_SEPARATOR      = Dict.getSyntax( Dict.SYNTAX_KEY_SEPARATOR      );
		KEY_MAJ            = Dict.getSyntax( Dict.SYNTAX_KEY_MAJ            );
		KEY_MIN            = Dict.getSyntax( Dict.SYNTAX_KEY_MIN            );
		PARTIAL_SYNC_RANGE = Dict.getSyntax( Dict.SYNTAX_PARTIAL_SYNC_RANGE );
		PARTIAL_SYNC_SEP   = Dict.getSyntax( Dict.SYNTAX_PARTIAL_SYNC_SEP   );
		CHORD              = Dict.getSyntax( Dict.SYNTAX_CHORD              );
		CHORD_ASSIGNER     = Dict.getSyntax( Dict.SYNTAX_CHORD_ASSIGNER     );
		CHORD_SEPARATOR    = Dict.getSyntax( Dict.SYNTAX_CHORD_SEPARATOR    );
		COMMENT            = Dict.getSyntax( Dict.SYNTAX_COMMENT            );
		CONST              = Dict.getSyntax( Dict.SYNTAX_CONST              );
		VAR                = Dict.getSyntax( Dict.SYNTAX_VAR                );
		VAR_ASSIGNER       = Dict.getSyntax( Dict.SYNTAX_VAR_ASSIGNER       );
		VAR_SYMBOL         = Dict.getSyntax( Dict.SYNTAX_VAR_SYMBOL         );
		DEFINE             = Dict.getSyntax( Dict.SYNTAX_DEFINE             );
		DOT                = Dict.getSyntax( Dict.SYNTAX_DOT                );
		END                = Dict.getSyntax( Dict.SYNTAX_END                );
		BLOCK_OPEN         = Dict.getSyntax( Dict.SYNTAX_BLOCK_OPEN         );
		BLOCK_CLOSE        = Dict.getSyntax( Dict.SYNTAX_BLOCK_CLOSE        );
		GLOBAL             = Dict.getSyntax( Dict.SYNTAX_GLOBAL             );
		CALL               = Dict.getSyntax( Dict.SYNTAX_CALL               );
		INCLUDE            = Dict.getSyntax( Dict.SYNTAX_INCLUDE            );
		SOUNDFONT          = Dict.getSyntax( Dict.SYNTAX_SOUNDFONT          );
		INSTRUMENT         = Dict.getSyntax( Dict.SYNTAX_INSTRUMENT         );
		INSTRUMENTS        = Dict.getSyntax( Dict.SYNTAX_INSTRUMENTS        );
		META               = Dict.getSyntax( Dict.SYNTAX_META               );
		META_COPYRIGHT     = Dict.getSyntax( Dict.SYNTAX_META_COPYRIGHT     );
		META_TITLE         = Dict.getSyntax( Dict.SYNTAX_META_TITLE         );
		META_COMPOSER      = Dict.getSyntax( Dict.SYNTAX_META_COMPOSER      );
		META_LYRICIST      = Dict.getSyntax( Dict.SYNTAX_META_LYRICIST      );
		META_ARTIST        = Dict.getSyntax( Dict.SYNTAX_META_ARTIST        );
		META_SOFT_KARAOKE  = Dict.getSyntax( Dict.SYNTAX_META_SOFT_KARAOKE  );
		META_SK_VERSION    = Dict.getSyntax( Dict.SYNTAX_META_SK_VERSION    );
		META_SK_LANG       = Dict.getSyntax( Dict.SYNTAX_META_SK_LANG       );
		META_SK_TITLE      = Dict.getSyntax( Dict.SYNTAX_META_SK_TITLE      );
		META_SK_AUTHOR     = Dict.getSyntax( Dict.SYNTAX_META_SK_AUTHOR     );
		META_SK_COPYRIGHT  = Dict.getSyntax( Dict.SYNTAX_META_SK_COPYRIGHT  );
		META_SK_INFO       = Dict.getSyntax( Dict.SYNTAX_META_SK_INFO       );
		LENGTH_32          = Dict.getSyntax( Dict.SYNTAX_32                 );
		LENGTH_16          = Dict.getSyntax( Dict.SYNTAX_16                 );
		LENGTH_8           = Dict.getSyntax( Dict.SYNTAX_8                  );
		LENGTH_4           = Dict.getSyntax( Dict.SYNTAX_4                  );
		LENGTH_2           = Dict.getSyntax( Dict.SYNTAX_2                  );
		LENGTH_1           = Dict.getSyntax( Dict.SYNTAX_1                  );
		LENGTH_M1          = Dict.getSyntax( Dict.SYNTAX_M1                 );
		LENGTH_M2          = Dict.getSyntax( Dict.SYNTAX_M2                 );
		LENGTH_M4          = Dict.getSyntax( Dict.SYNTAX_M4                 );
		LENGTH_M8          = Dict.getSyntax( Dict.SYNTAX_M8                 );
		LENGTH_M16         = Dict.getSyntax( Dict.SYNTAX_M16                );
		LENGTH_M32         = Dict.getSyntax( Dict.SYNTAX_M32                );
		L                  = Dict.getSyntax( Dict.SYNTAX_L                  );
		LYRICS             = Dict.getSyntax( Dict.SYNTAX_LYRICS             );
		LYRICS_SPACE       = Dict.getSyntax( Dict.SYNTAX_LYRICS_SPACE       );
		LYRICS_CR          = Dict.getSyntax( Dict.SYNTAX_LYRICS_CR          );
		LYRICS_LF          = Dict.getSyntax( Dict.SYNTAX_LYRICS_LF          );
		LYRICS_COMMA       = Dict.getSyntax( Dict.SYNTAX_LYRICS_COMMA       );
		FUNCTION           = Dict.getSyntax( Dict.SYNTAX_FUNCTION           );
		PATTERN            = Dict.getSyntax( Dict.SYNTAX_PATTERN            );
		PATTERN_INDEX_SEP  = Dict.getSyntax( Dict.SYNTAX_PATTERN_INDEX_SEP  );
		PARAM_OPEN         = Dict.getSyntax( Dict.SYNTAX_PARAM_OPEN         );
		PARAM_CLOSE        = Dict.getSyntax( Dict.SYNTAX_PARAM_CLOSE        );
		PARAM_SEPARATOR    = Dict.getSyntax( Dict.SYNTAX_PARAM_SEPARATOR    );
		PARAM_ASSIGNER     = Dict.getSyntax( Dict.SYNTAX_PARAM_ASSIGNER     );
		PARAM_NAMED_OPEN   = Dict.getSyntax( Dict.SYNTAX_PARAM_NAMED_OPEN   );
		PARAM_NAMED_CLOSE  = Dict.getSyntax( Dict.SYNTAX_PARAM_NAMED_CLOSE  );
		PARAM_INDEX_OPEN   = Dict.getSyntax( Dict.SYNTAX_PARAM_INDEX_OPEN   );
		PARAM_INDEX_CLOSE  = Dict.getSyntax( Dict.SYNTAX_PARAM_INDEX_CLOSE  );
		M                  = Dict.getSyntax( Dict.SYNTAX_M                  );
		MULTIPLE           = Dict.getSyntax( Dict.SYNTAX_MULTIPLE           );
		OPT_ASSIGNER       = Dict.getSyntax( Dict.SYNTAX_OPT_ASSIGNER       );
		OPT_SEPARATOR      = Dict.getSyntax( Dict.SYNTAX_OPT_SEPARATOR      );
		P                  = Dict.getSyntax( Dict.SYNTAX_P                  );
		REST               = Dict.getSyntax( Dict.SYNTAX_REST               );
		PROG_BANK_SEP      = Dict.getSyntax( Dict.SYNTAX_PROG_BANK_SEP      );
		Q                  = Dict.getSyntax( Dict.SYNTAX_Q                  );
		QUANTITY           = Dict.getSyntax( Dict.SYNTAX_QUANTITY           );
		D                  = Dict.getSyntax( Dict.SYNTAX_D                  );
		DURATION           = Dict.getSyntax( Dict.SYNTAX_DURATION           );
		DURATION_PERCENT   = Dict.getSyntax( Dict.SYNTAX_DURATION_PERCENT   );
		TR                 = Dict.getSyntax( Dict.SYNTAX_TR                 );
		TREMOLO            = Dict.getSyntax( Dict.SYNTAX_TREMOLO            );
		S                  = Dict.getSyntax( Dict.SYNTAX_S                  );
		SHIFT              = Dict.getSyntax( Dict.SYNTAX_SHIFT              );
		T                  = Dict.getSyntax( Dict.SYNTAX_T                  );
		TUPLET             = Dict.getSyntax( Dict.SYNTAX_TUPLET             );
		V                  = Dict.getSyntax( Dict.SYNTAX_V                  );
		VELOCITY           = Dict.getSyntax( Dict.SYNTAX_VELOCITY           );
		TRIPLET            = Dict.getSyntax( Dict.SYNTAX_TRIPLET            );
		TUPLET_INTRO       = Dict.getSyntax( Dict.SYNTAX_TUPLET_INTRO       );
		TUPLET_FOR         = Dict.getSyntax( Dict.SYNTAX_TUPLET_FOR         );
		LENGTH_PLUS        = Dict.getSyntax( Dict.SYNTAX_LENGTH_PLUS        );
		IF                 = Dict.getSyntax( Dict.SYNTAX_IF                 );
		ELSIF              = Dict.getSyntax( Dict.SYNTAX_ELSIF              );
		ELSE               = Dict.getSyntax( Dict.SYNTAX_ELSE               );
		COND_EQ            = Dict.getSyntax( Dict.SYNTAX_COND_EQ            );
		COND_NEQ           = Dict.getSyntax( Dict.SYNTAX_COND_NEQ           );
		COND_NDEF          = Dict.getSyntax( Dict.SYNTAX_COND_NDEF          );
		COND_LT            = Dict.getSyntax( Dict.SYNTAX_COND_LT            );
		COND_LE            = Dict.getSyntax( Dict.SYNTAX_COND_LE            );
		COND_GT            = Dict.getSyntax( Dict.SYNTAX_COND_GT            );
		COND_GE            = Dict.getSyntax( Dict.SYNTAX_COND_GE            );
		COND_IN            = Dict.getSyntax( Dict.SYNTAX_COND_IN            );
		COND_IN_SEP        = Dict.getSyntax( Dict.SYNTAX_COND_IN_SEP        );
		
		// Remember the original names of some commands.
		// Needed to redefine them without getting an error in the following parsing runs.
		ORIGINAL_DEFINE  = DEFINE;
		ORIGINAL_INCLUDE = INCLUDE;
		ORIGINAL_COMMENT = COMMENT;
	}
	
	/**
	 * Parses a MidicaPL source file. Creates a MIDI sequence from that file.
	 * 
	 * @param file  MidicaPL source file.
	 */
	public void parse(File file) throws ParseException {
		this.file = file;
		
		// clean up and make parser ready for parsing
		if (isRootParser) {
			preprocess(file);
			reset();
		}
		
		// get charset
		chosenCharset   = ((ComboboxStringOption) ConfigComboboxModel.getModel( Config.CHARSET_MPL ).getSelectedItem() ).getIdentifier();
		Charset charset = Charset.forName( chosenCharset );
		
		try {
			String            filePath = file.getCanonicalPath();
			ArrayList<String> lines    = fileCache.get(filePath);
			
			// file not yet cached?
			if (null == lines) {
				
				// open file for reading
				FileInputStream   fis = new FileInputStream(file);
				InputStreamReader ir  = new InputStreamReader(fis, charset);
				BufferedReader    br  = new BufferedReader(ir);
				String            line;
				
				// get lines from file
				lines = new ArrayList<>();
				while (null != (line = br.readLine())) {
					lines.add(line);
				}
				br.close();
				
				// cache file (not neccessary for the root parser)
				if ( ! isRootParser ) {
					fileCache.put(filePath, lines);
				}
			}
			
			if (isRootParser) {
				// look for define commands
				isDefineParsRun = true;
				parsingRun(lines);
				isDefineParsRun = false;
				
				// now the comment symbol cannot change any more.
				cleanLines(lines);
				
				// compile regex patterns that are needed after the
				// define run is finished.
				compilePatterns();
				
				// look for constant definitions
				isConstParsRun = true;
				parsingRun(lines);
				isConstParsRun = false;
				
				// look for chords, the very first instruments block,
				// meta definitions, and checks block nesting
				isChInstMetaParsRun = true;
				parsingRun(lines);
				postprocessMeta(); // apply all collected meta info
				isChInstMetaParsRun = false;
				
				// collect all function names that are defined somewhere
				isFuncNameParsRun = true;
				parsingRun(lines);
				isFuncNameParsRun = false;
				
				// look for functions
				isFuncParsRun = true;
				parsingRun(lines);
				isFuncParsRun = false;
				
				// pre-check if/elsif conditions
				isCondCheckParsRun = true;
				parsingRun(lines);
				isCondCheckParsRun = false;
				
				// prepare if-elsif-else for root-level blocks
				condChainOpened = false;
				condChainHit    = false;
				
				// look for everything else
				// final parsing run, building up the sequence
				isDefaultParsRun = true;
				parsingRun(lines);
				isDefaultParsRun = false;
			}
			else {
				// Not the root parser.
				// This is called for each INCLUDE command and parsing run.
				// So the parsing run has to be executed only once here.
				// For which run it's called is obvious because the according fields are static.
				
				if (isConstParsRun) {
					// The comment symbol cannot change any more.
					// Clean the lines for all following parsing runs.
					cleanLines(lines);
				}
				
				parsingRun(lines);
			}
		}
		catch ( FileNotFoundException e ) {
			throw new ParseException(e.toString());
		}
		catch ( IOException e ) {
			e.printStackTrace();
			throw new ParseException(e.toString());
		}
		
		// allow an empty sequence?
		if (isRootParser && ! instrumentsParsed) {
			postprocessInstruments();
		}
		
		// EOF has been reached
		if (isRootParser) {
			postprocessSequence(SequenceCreator.getSequence(), FORMAT_MIDICAPL, chosenCharset);
		}
	}
	
	/**
	 * Create regex patterns.
	 * This is called after the define run so that the syntax keywords cannot change any more.
	 */
	private void compilePatterns() {
		
		// match constants/variables/parameters
		varPattern = Pattern.compile(
			  "("       // group 1: the whole construct (constant, variable or parameter)
			+ "("       // group 2: $
			+ Pattern.quote(VAR_SYMBOL)
			+ ")"
			+ "("       // group 3: everything except $
			+ "("
			+ "\\w+"    // group 4: normal variable
			+ ")"
			+ "|"
			+ Pattern.quote(PARAM_INDEX_OPEN)
			+ "("
			+ "\\w+"    // group 5: indexed parameter
			+ ")"
			+ Pattern.quote(PARAM_INDEX_CLOSE)
			+ "|"
			+ Pattern.quote(PARAM_NAMED_OPEN)
			+ "("
			+ "\\w+"    // group 6: named parameter
			+ ")"
			+ Pattern.quote(PARAM_NAMED_CLOSE)
			+ ")"
			+ ")"
		);
		
		// match function or pattern calls
		callPattern = Pattern.compile(
			  "^([^"                      // function/pattern name, consisting of all characters except:
			+ "\\s"                       // - whitespace
			+ Pattern.quote(PARAM_OPEN)   // - (
			+ Pattern.quote(PARAM_CLOSE)  // - )
			+ "]+)"                       // ... with one or more characters
			+ "\\s*"                      // optional whitespace(s)
			+ "("                         // open capturing group for an optional parameter list
			+ "\\s*"                          // optional whitespace(s)
			+ Pattern.quote(PARAM_OPEN)       // (
			+ "\\s*"                              // optional whitespace(s)
			+ "(.*?)"                                 // parameters
			+ "\\s*"                              // optional whitespace(s)
			+ Pattern.quote(PARAM_CLOSE)      // )
			+ "\\s*"                          // optional whitespace(s)
			+ ")?"                         // close optional parameter list
			+ "\\s*"                       // optional whitespace(s)
			+ "(.+?)?"                     // optional options
			+ "\\s*"                       // optional whitespace(s)
			+ "$"
		);
		
		// split on condition for if/elsif
		condPattern = Pattern.compile(
			"("                           // group 1: get the operator
			+ Pattern.quote(COND_EQ)      // ==
			+ "|"
			+ Pattern.quote(COND_NEQ)     // !=
			+ "|"
			+ Pattern.quote(COND_NDEF)    // !
			+ "|"
			+ Pattern.quote(COND_LE)      // <=
			+ "|"
			+ Pattern.quote(COND_LT)      // <
			+ "|"
			+ Pattern.quote(COND_GE)      // >=
			+ "|"
			+ Pattern.quote(COND_GT)      // >
			+ "|"
			+ "\\b"
			+ Pattern.quote(COND_IN)      // IN-clause, e.g. $x in 0;1;2;5
			+ "\\b"
			+ ")"
		);
		
		// split list in "in" conditions for if/elsif
		condInPattern = Pattern.compile("\\s*" + Pattern.quote(COND_IN_SEP) + "\\s*");
		
		// find forbidden \r or \n in a soft karaoke field or syllable
		crlfSkPattern = Pattern.compile(Pattern.quote(LYRICS_CR) + "|" + Pattern.quote(LYRICS_LF));
	}
	
	/**
	 * Parses one single line of a MidicaPL source file.
	 * The line is expected to be cleaned already from comments and leading/trailing whitespaces.
	 * Can also be called from a {@link NestableBlock}.
	 * 
	 * @param line             The line to be parsed.
	 * @throws ParseException  If the line cannot be parsed.
	 */
	public void parseLine(String line) throws ParseException {
		
		// replace constants
		if ( ! isDefineParsRun && ! isConstParsRun ) {
			line = replaceConstants(line);
		}
		
		// replace variables?
		boolean mustReplaceVars = isDefaultParsRun             // only in the default run
		                       && MODE_DEFAULT == currentMode  // not inside of functions
		                       && 0 == nestableBlkDepth;       // not inside of a block
		if (mustReplaceVars) {
			String[] tokens = line.split("\\s+", 2);
			
			// inside of VAR definitions the replacement is done later
			if ( ! VAR.equals(tokens[0]) ) {
				line = replaceVariables(line);
			}
		}
		currentLineContent = line;
		
		String[] tokens = line.split("\\s+", 3);
		
		if ("".equals(tokens[0])) {
			// empty line or only comments
			parseTokens(tokens);
			return;
		}
		
		// only 2 tokens for meta or call commands
		if (isChInstMetaParsRun && isMetaCmd(tokens[0])) {
			tokens = line.split("\\s+", 2);
		}
		else if (CALL.equals(tokens[0])) {
			tokens = line.split("\\s+", 2);
		}
		
		// only 2 tokens for constant or variable definitions
		else if (isConstParsRun && CONST.equals(tokens[0])) {
			tokens = line.split("\\s+", 2);
		}
		else if ((isDefaultParsRun || isFuncParsRun) && VAR.equals(tokens[0])) {
			tokens = line.split("\\s+", 2);
		}
		
		parseTokens(tokens);
	}
	
	/**
	 * Determins if the given command is a META command.
	 * 
	 * @param cmd    The command to be judged.
	 * @return **true**, if cmd is a meta command, otherwise: **false**.
	 */
	private boolean isMetaCmd(String cmd) {
		if (META_COPYRIGHT.equals(cmd))
			return true;
		if (META_TITLE.equals(cmd))
			return true;
		if (META_COMPOSER.equals(cmd))
			return true;
		if (META_LYRICIST.equals(cmd))
			return true;
		if (META_ARTIST.equals(cmd))
			return true;
		if (META_SOFT_KARAOKE.equals(cmd))
			return true;
		if (META_SK_VERSION.equals(cmd))
			return true;
		if (META_SK_LANG.equals(cmd))
			return true;
		if (META_SK_TITLE.equals(cmd))
			return true;
		if (META_SK_AUTHOR.equals(cmd))
			return true;
		if (META_SK_COPYRIGHT.equals(cmd))
			return true;
		if (META_SK_INFO.equals(cmd))
			return true;
		return false;
	}
	
	/**
	 * Creates and returns a snapshot of the tickstamps of all channels.
	 * The indexes of the returned structure are the channels.
	 * The values are the tickstamps.
	 * 
	 * @return snapshot of tickstamps
	 * @throws ParseException    if something went wrong.
	 */
	public ArrayList<Long> rememberTickstamps() throws ParseException {
		
		// allow drum-only sequences beginning with an empty multiple block or empty function call
		if (! instrumentsParsed) {
			postprocessInstruments();
		}
		
		ArrayList<Long> tickstampByChannel = new ArrayList<>();
		for (int channel = 0; channel < 16; channel++) {
			long ticks = instruments.get(channel).getCurrentTicks();
			tickstampByChannel.add(ticks);
		}
		return tickstampByChannel;
	}
	
	/**
	 * Restores a snapshot of tickstamps for each channel.
	 * 
	 * @param tickstamps the tickstamps to be restored - indexes are channels, values are tickstamps.
	 */
	public void restoreTickstamps(ArrayList<Long> tickstamps) {
		for (int channel = 0; channel < 16; channel++) {
			long ticks = tickstamps.get(channel);
			instruments.get(channel).setCurrentTicks(ticks);
		}
	}
	
	/**
	 * Parses a channel number.
	 * 
	 * @param s    Channel number string.
	 * @return     Parsed channel number.
	 * @throws ParseException    If the command cannot be parsed.
	 */
	public int toChannel(String s) throws ParseException {
		if (s.equals(P)) {
			return 9;
		}
		int channel = toInt( s );
		if (channel > 15)
			throw new ParseException( Dict.get(Dict.ERROR_INVALID_CHANNEL_NUMBER) + s );
		return channel;
	}
	
	/**
	 * Parses all MidicaPL source lines for a specific purpose.
	 * E.g. find definition commands, chords, functions, and so on.
	 * 
	 * @param lines           All lines of the MidicaPL source.
	 * @throws ParseException if one of the lines cannot be parsed.
	 * @throws IOException    if the file path cannot be calculated.
	 */
	private void parsingRun(ArrayList<String> lines) throws ParseException, IOException {
		currentLineNumber = 0;
		try {
			for (String line : lines) {
				currentLineNumber++;
				
				// In the define parsing run, the lines are not yet cleaned because
				// the comment symbol may change any time. So we need to do that here.
				if (isDefineParsRun) {
					line = cleanLine(line);
				}
				currentLineContent = line;
				
				parseLine(line);
			}
			
			// find open blocks at the end of the file
			currentLineNumber++;
			checkNestingAtEOF();
			
			// In case of wrong block nesting make sure that the correct
			// error message and line number is shown
			nestableBlkDepth = 0;
		}
		catch (ParseException e) {
			// Add file name and line number to exception and throw it again
			// but only if this is not yet done.
			// If this information is already available than it comes from
			// another parser instance. In this case we must not overwrite it.
			if (0 == e.getLineNumber()) {
				e.setLineNumber(currentLineNumber);
			}
			if (null == e.getFile()) {
				e.setFile(file);
			}
			e.setStackTrace(stackTrace);
			e.setLineContentIfNotYetDone(currentLineContent);
			throw e;
		}
		catch (Exception e) {
			// any other exception? - wrap it into a parsing exception with file and line
			ParseException pe = new ParseException(e.toString());
			e.printStackTrace();
			pe.setLineNumber(currentLineNumber);
			pe.setFile(file);
			pe.setStackTrace(stackTrace);
			pe.setLineContentIfNotYetDone(currentLineContent);
			throw pe;
		}
	}
	
	/**
	 * Parses the tokens of one MidicaPL command.
	 * 
	 * @param  tokens          The tokens from one MidicaPL line.
	 * @throws ParseException  If the tokens cannot be parsed.
	 */
	private void parseTokens(String[] tokens) throws ParseException {
		
		// reset if/elsif/else conditions in blocks
		if (isDefaultParsRun
			&& 0 == nestableBlkDepth
			&& ! "".equals(tokens[0])
			&& ! BLOCK_OPEN.equals(tokens[0]) ) {
			condChainOpened = false;
			condChainHit    = false;
		}
		
		// continue or not - decide depending on parsing run and current mode
		boolean mustIgnore = mustIgnore(tokens[0]);
		if (mustIgnore) {
			return;
		}
		
		if (MODE_PATTERN == currentMode && ! END.equals(tokens[0])) {
			// line inside of a pattern definition
			
			parsePatternLine(tokens);
			return;
		}
		else {
			checkIfCmdExists(tokens[0]); // check for command or channel
		}
		
		// replace note, instrument, percussion and drumkit names
		if (tokens.length > 2) {
			replaceShortcuts(tokens);
		}
		
		// Some lines must be parsed directly, others must be stored and executed later.
		// However we have to fake an execution in order to detect syntax errors as soon as
		// possible, so that the error message contains the right line number.
		// Here we find out if and why an execution has to be faked.
		boolean isFake  = false;
		boolean isFunct = false;
		boolean isBlock = false;
		if (MODE_FUNCTION == currentMode) {
			isFake  = true;
			isFunct = true;
		}
		else if (nestableBlkDepth > 0) {
			isFake  = true;
			isBlock = true;
		}
		
		// empty line?
		if ("".equals(tokens[0])) {
			if (isFunct)
				currentFunction.add(String.join(" ", tokens)); // add to function
			else if (isBlock)
				nestableBlkStack.peek().add(tokens); // add to block
		}
		
		// global command?
		else if (tokens[0].matches("^" + Pattern.quote(GLOBAL) + "$")) {
			if (MODE_INSTRUMENTS == currentMode) {
				// we are inside an instruments definition
				throw new ParseException( Dict.get(Dict.ERROR_GLOBALS_IN_INSTR_DEF) );
			}
			else if (isFunct) {
				currentFunction.add(String.join(" ", tokens)); // add to function
			}
			else if (isBlock) {
				nestableBlkStack.peek().add(tokens); // add to block
			}
			parseGlobalCmd(tokens, isFake);
		}
		
		// channel or instruments command
		else if (tokens[0].matches("^\\d+$")) {
			if (!isFunct) {
				checkInstrumentsParsed();
			}
			
			// instruments command
			if (MODE_INSTRUMENTS == currentMode) {
				// we are inside an instruments definition block
				parseInstrumentCmd(tokens, isFake);
				return;
			}
			
			// pattern call --> call this method (parseTokens()) once for each pattern line
			if (tokens.length > 2) {
				Matcher patCallMatcher = callPattern.matcher(tokens[2]);
				String  patternName    = null;
				if (patCallMatcher.matches()) {
					patternName = patCallMatcher.group(1);
				}
				if (patternName != null && definedPatternNames.contains(patternName)) {
					if (isFunct)
						currentFunction.add(String.join(" ", tokens)); // add to function
					else if (isBlock)
						nestableBlkStack.peek().add(tokens); // add to block
					parsePatternCall(tokens, isFake);
					return;
				}
			}
			
			// more than one note or chord? --> call this method (parseTokens()) once for each chord element
			if (! isBlock && parseChordNotes(tokens)) {
				return;
			}
			
			// channel command with a single note
			if (isFunct)
				currentFunction.add(String.join(" ", tokens)); // add to function
			else if (isBlock) {
				nestableBlkStack.peek().add(tokens); // add to block
				
				// Ensure that a chord in a block in a pattern is not added more than once.
				// Otherwise, the stacktrace line numbers would be wrong.
				if (isPatternSubChord)
					return;
			}
			
			// apply or fake command
			parseChannelCmd(tokens, isFake);
		}
		
		// variable definition / assignment
		else if (VAR.equals(tokens[0])) {
			if (isFunct)
				currentFunction.add(String.join(" ", tokens)); // add to function
			else if (isBlock)
				nestableBlkStack.peek().add(tokens); // add to block
			parseVAR(tokens, isFake);
		}
		
		// line begins with variable?
		// (Don't check this in the define parsing run, when varPattern is not yet initialized.)
		else if (varPattern != null && varPattern.matcher(tokens[0]).matches()) {
			if (isFunct)
				currentFunction.add(String.join(" ", tokens)); // add to function
			else if (isBlock)
				nestableBlkStack.peek().add(tokens); // add to block
			else
				throw new ParseException( Dict.get(Dict.ERROR_VAR_NOT_ALLOWED) + tokens[0] );
		}
		
		// (single line) instrument switch
		else if (INSTRUMENT.equals(tokens[0])) {
			if (!isFake)
				checkInstrumentsParsed();
			if (MODE_INSTRUMENTS == currentMode)
				throw new ParseException( Dict.get(Dict.ERROR_SINGLE_INSTR_IN_INSTR_DEF) );
			
			// only remember the line?
			if (isFunct)
				currentFunction.add(String.join(" ", tokens)); // add to function
			else if (isBlock)
				nestableBlkStack.peek().add(tokens); // add to block
			
			parseSingleLineInstrumentSwitch(tokens, isFake);
		}
		
		// call a function
		else if (CALL.equals(tokens[0])) {
			
			// only remember the line?
			if ( ! isCondCheckParsRun ) {
				if (isFunct)
					currentFunction.add(String.join(" ", tokens)); // add to function
				else if (isBlock)
					nestableBlkStack.peek().add(tokens); // add to block
			}
			
			parseCALL(tokens, isFake);
		}
		
		// nestable block commands
		// "{", "}", instruments, function, end
		else if (BLOCK_OPEN.equals(tokens[0]) || BLOCK_CLOSE.equals(tokens[0])) {
			
			// only remember the line?
			if (isFunct)
				currentFunction.add(String.join(" ", tokens));
			
			parseBLOCK(tokens, isFunct);
		}
		
		// mode command (instruments, function, meta, soft karaoke, end)
		else if (INSTRUMENTS.equals(tokens[0])
			|| FUNCTION.equals(tokens[0])
			|| PATTERN.equals(tokens[0])
			|| META.equals(tokens[0])
			|| META_SOFT_KARAOKE.equals(tokens[0])
			|| END.equals(tokens[0])) {
			parseModeCmd(tokens);
			return;
		}
		
		// meta definition
		else if (MODE_META == currentMode) {
			parseMetaCmd(tokens);
		}
		
		// soft karaoke definition
		else if (MODE_SOFT_KARAOKE == currentMode) {
			parseSoftKaraokeCmd(tokens);
		}
		
		// root-level commands (that are not allowed to appear inside of a block)
		// define, chord, const, include, soundfont
		else {
			parseRootLevelCmd(tokens);
		}
	}
	
	/**
	 * Throws an error if the given command does not exist.
	 * 
	 * Each command is also checked at the place it's parsed. But sometimes this is too late.
	 * Different parsing runs ignore different lines, so that the real error is not shown but
	 * a consecutive error with a much higher line number.
	 * 
	 * Example:
	 * 
	 *     DEFINE CHORD CRD
	 *     // ...
	 *     CHORD cmay c,e,g
	 *     // ...
	 *     0 cmay /4
	 * 
	 * This would say: `unknown note: cmay`
	 * 
	 * But correct is: `unknown command: CHORD`
	 * 
	 * We need this check early or otherwise the error messages could be confusing.
	 * 
	 * @param cmd    The command to be checked
	 * @throws ParseException if the command does not exist.
	 */
	private void checkIfCmdExists(String cmd) throws ParseException {
		
		// The define parsing run itself is too early for checkings.
		// Otherwise, a DEFINE would not work.
		if (isDefineParsRun) {
			return;
		}
		
		if      ( END.equals(cmd)                   ) {}
		else if ( BLOCK_OPEN.equals(cmd)            ) {}
		else if ( BLOCK_CLOSE.equals(cmd)           ) {}
		else if ( FUNCTION.equals(cmd)              ) {}
		else if ( PATTERN.equals(cmd)               ) {}
		else if ( CHORD.equals(cmd)                 ) {}
		else if ( CALL.equals(cmd)                  ) {}
		else if ( INCLUDE.equals(cmd)               ) {}
		else if ( SOUNDFONT.equals(cmd)             ) {}
		else if ( DEFINE.equals(cmd)                ) {}
		else if ( CONST.equals(cmd)                 ) {}
		else if ( VAR.equals(cmd)                   ) {}
		else if ( INSTRUMENT.equals(cmd)            ) {}
		else if ( INSTRUMENTS.equals(cmd)           ) {}
		else if ( META.equals(cmd)                  ) {}
		else if ( META_COPYRIGHT.equals(cmd)        ) {}
		else if ( META_TITLE.equals(cmd)            ) {}
		else if ( META_COMPOSER.equals(cmd)         ) {}
		else if ( META_LYRICIST.equals(cmd)         ) {}
		else if ( META_ARTIST.equals(cmd)           ) {}
		else if ( META_SOFT_KARAOKE.equals(cmd)     ) {}
		else if ( META_SK_VERSION.equals(cmd)       ) {}
		else if ( META_SK_LANG.equals(cmd)          ) {}
		else if ( META_SK_TITLE.equals(cmd)         ) {}
		else if ( META_SK_AUTHOR.equals(cmd)        ) {}
		else if ( META_SK_COPYRIGHT.equals(cmd)     ) {}
		else if ( META_SK_INFO.equals(cmd)          ) {}
		else if ( GLOBAL.equals(cmd)                ) {}
		else if ( P.equals(cmd)                     ) {}
		else if ( ORIGINAL_DEFINE.equals(cmd)       ) {}
		else if ( ORIGINAL_INCLUDE.equals(cmd)      ) {}
		else if ( cmd.matches("^\\d+$")             ) {}
		else if ( "".equals(cmd)                    ) {} // empty line. needed to get the right line numbers in stack traces
		else if ( varPattern.matcher(cmd).matches() ) {
			// Line begins with a variable - check only in the default run.
			// And not inside of a block.
			// However this should never happen because in the default run, either:
			// - the variable should have been replaced already; or
			// - an exception for an undefined variable should have been thrown already.
			if (isDefaultParsRun && 0 == nestableBlkDepth && currentMode == MODE_DEFAULT) {
				throw new ParseException(
					Dict.get(Dict.ERROR_UNKNOWN_CMD) + cmd
					+ "\n This should not happen. Please report."
				);
			}
		}
		else {
			throw new ParseException( Dict.get(Dict.ERROR_UNKNOWN_CMD) + cmd );
		}
	}
	
	/**
	 * Determins if a command must be ignored in the current parsing run and mode.
	 * 
	 * Also sets the current mode or nesting depth, if this is required but no further
	 * parsing is needed.
	 * 
	 * This is called for each line in each parsing run.
	 * 
	 * There are different parsing runs, in the following order:
	 * 
	 *  - parse define commands
	 *  - parse chords and the first instruments block
	 *  - get all needed function names from call commands
	 *  - parse functions
	 *  - parse everything else
	 * 
	 * @param cmd             The first token in the line
	 * @return                **true**, if the current line must be ignored.
	 * @throws ParseException if this method is called without configuring the parsing run before.
	 */
	private boolean mustIgnore(String cmd) throws ParseException {
		
		// always follow files
		if (INCLUDE.equals(cmd) || ORIGINAL_INCLUDE.equals(cmd)) {
			return false;
		}
		
		// look for DEFINE
		if (isDefineParsRun || isDefaultParsRun) {
			
			// define run: parse; default run: check nesting
			if (DEFINE.equals(cmd) || ORIGINAL_DEFINE.equals(cmd))
				return false;
			
			// define run: ignore everything else
			if (isDefineParsRun)
				return true;
		}
		
		// ignore DEFINE in all other runs
		if (DEFINE.equals(cmd) || ORIGINAL_DEFINE.equals(cmd)) {
			return true;
		}
		
		// look for CONST
		if (isConstParsRun || isDefaultParsRun) {
			
			// const run: parse; default run: check nesting
			if (CONST.equals(cmd))
				return false;
			
			// const run: ignore everything else
			if (isConstParsRun)
				return true;
		}
		
		// ignore CONST in later runs
		if (CONST.equals(cmd)) {
			return true;
		}
		
		// handle variables only in the default and function parsing run
		if ( ! isDefaultParsRun && ! isFuncParsRun ) {
			
			// VAR - default run: parse, function run: add, other run: ignore
			if (VAR.equals(cmd))
				return true;
			
			// lines beginning with a variable - default run: parse, function run: add, other run: ignore
			if ( varPattern.matcher(cmd).matches() )
				return true;
		}
		
		// from now on, always track INSTRUMENTS
		if (INSTRUMENTS.equals(cmd)) {
			checkNesting(cmd);
			return false; // set mode correctly
		}
		
		// chords, first instruments block, and block nesting
		if (isChInstMetaParsRun) {
			
			// don't allow overlappings between nestable and named blocks
			checkNesting(cmd);
			
			// ignore blocks other than instruments or meta
			if (END.equals(cmd) && (MODE_INSTRUMENTS == currentMode || MODE_META == currentMode || MODE_SOFT_KARAOKE == currentMode)) {
				// handle later
			}
			else if (BLOCK_OPEN.equals(cmd) || BLOCK_CLOSE.equals(cmd) || FUNCTION.equals(cmd) || PATTERN.equals(cmd) || END.equals(cmd)) {
				trackNesting(cmd);
				return true;
			}
			
			// chord
			if (CHORD.equals(cmd)) {
				return false;
			}
			
			// parse meta
			if (META.equals(cmd) || MODE_META == currentMode) {
				return false;
			}
			
			// parse soft karaoke
			if (META_SOFT_KARAOKE.equals(cmd) || MODE_SOFT_KARAOKE == currentMode) {
				return false;
			}
			
			// only care about the very first instruments block
			if (MODE_INSTRUMENTS == currentMode) {
				
				// NOT the first instruments block? --> Don't parse. Just care about END.
				if (instrumentsParsed) {
					if (END.equals(cmd)) {
						currentMode = MODE_DEFAULT;
					}
					return true;
				}
				
				// First instruments block --> parse everything.
				else {
					return false;
				}
			}
			
			// ignore everything else here
			else {
				return true;
			}
		}
		
		// from now on: ignore chord commands and meta blocks
		if (CHORD.equals(cmd)) {
			return true;
		}
		if (META.equals(cmd) || MODE_META == currentMode || META_SOFT_KARAOKE.equals(cmd) || MODE_SOFT_KARAOKE == currentMode) {
			trackNesting(cmd);
			return true;
		}
		
		// care about instruments only in the default run
		if ( ! isDefaultParsRun && MODE_INSTRUMENTS == currentMode ) {
			if (END.equals(cmd)) {
				// reset mode manually
				currentMode = MODE_DEFAULT;
			}
			return true;
		}
		
		// look for defined function/pattern names
		if (isFuncNameParsRun) {
			if (FUNCTION.equals(cmd) || PATTERN.equals(cmd)) {
				return false;
			}
			
			// handle function-END
			if (END.equals(cmd)) {
				currentMode = MODE_DEFAULT;
			}
			
			return true;
		}
		
		// parse functions/patterns
		if (isFuncParsRun) {
			if (FUNCTION.equals(cmd) || PATTERN.equals(cmd)) {
				return false;
			}
			if (MODE_FUNCTION == currentMode || MODE_PATTERN == currentMode) {
				return false;
			}
			return true;
		}
		
		// pre-checks if/elsif conditions
		if (isCondCheckParsRun) {
			
			// function call or nestable block
			if (BLOCK_OPEN.equals(cmd) || BLOCK_CLOSE.equals(cmd) || CALL.equals(cmd)) {
				return false;
			}
			
			return true;
		}
		
		// main parsing run
		if (isDefaultParsRun) {
			
			// ignore the first instruments block but parse the rest
			if (MODE_INSTRUMENTS == currentMode) {
				
				// NOT the first instruments block? --> Parse everything.
				if (frstInstrBlkOver) {
					return false;
				}
				
				// First instruments block --> Don't parse. Just care about END.
				else {
					if (END.equals(cmd)) {
						currentMode      = MODE_DEFAULT;
						frstInstrBlkOver = true;
					}
					return true;
				}
			}
			
			if (FUNCTION.equals(cmd)) {
				currentMode = MODE_FUNCTION;
				return true;
			}
			else if (PATTERN.equals(cmd)) {
				currentMode = MODE_PATTERN;
				return true;
			}
			else if (MODE_FUNCTION == currentMode || MODE_PATTERN == currentMode) {
				if (END.equals(cmd)) {
					currentMode = MODE_DEFAULT;
				}
				return true;
			}
		}
		
		// should never happen
		else {
			throw new ParseException("Undefined parsing run called");
		}
		
		return false;
	}
	
	/**
	 * Tracks nesting and mode of nestable and named blocks.
	 * Used only if nothing more is needed.
	 * Otherwise the nesting will be tracked by the according parse method.
	 * 
	 * @param cmd command to be tracked
	 */
	private void trackNesting(String cmd) {
		if (BLOCK_OPEN.equals(cmd))
			nestableBlkDepth++;
		else if (BLOCK_CLOSE.equals(cmd))
			nestableBlkDepth--;
		else if (FUNCTION.equals(cmd) && MODE_DEFAULT == currentMode)
			currentMode = MODE_FUNCTION;
		else if (PATTERN.equals(cmd) && MODE_DEFAULT == currentMode)
			currentMode = MODE_PATTERN;
		else if (META.equals(cmd) && MODE_DEFAULT == currentMode)
			currentMode = MODE_META;
		else if (META_SOFT_KARAOKE.equals(cmd) && MODE_META == currentMode)
			currentMode = MODE_SOFT_KARAOKE;
		else if (END.equals(cmd) && MODE_SOFT_KARAOKE == currentMode)
			currentMode = MODE_META;
		else if (END.equals(cmd) && currentMode != MODE_INSTRUMENTS)
			currentMode = MODE_DEFAULT;
	}
	
	/**
	 * Checks nesting of named or nestable blocks.
	 * 
	 * @param cmd    The command to be checked
	 * @throws ParseException if the check fails.
	 */
	private void checkNesting(String cmd) throws ParseException {
		if (INSTRUMENTS.equals(cmd) || FUNCTION.equals(cmd) || PATTERN.equals(cmd) || META.equals(cmd) || END.equals(cmd)) {
			if (nestableBlkDepth > 0) {
				throw new ParseException( Dict.get(Dict.ERROR_BLOCK_UNMATCHED_OPEN) );
			}
			if (MODE_DEFAULT != currentMode && ! END.equals(cmd)) {
				throw new ParseException( Dict.get(Dict.ERROR_NOT_ALLOWED_IN_BLK) + cmd );
			}
		}
		else if (BLOCK_OPEN.equals(cmd) || BLOCK_CLOSE.equals(cmd)) {
			if (MODE_INSTRUMENTS == currentMode) {
				throw new ParseException( Dict.get(Dict.ERROR_NOT_ALLOWED_IN_INSTR_BLK) + cmd );
			}
			if (MODE_META == currentMode || MODE_SOFT_KARAOKE == currentMode) {
				throw new ParseException( Dict.get(Dict.ERROR_NOT_ALLOWED_IN_META_BLK) + cmd );
			}
		}
	}
	
	/**
	 * Checks nesting of named or nestable blocks at end of file.
	 * 
	 * @throws ParseException if the check fails.
	 */
	private void checkNestingAtEOF() throws ParseException {
		if (nestableBlkDepth > 0) {
			throw new ParseException( Dict.get(Dict.ERROR_NESTABLE_BLOCK_OPEN_AT_EOF) );
		}
		if (currentMode != MODE_DEFAULT) {
			throw new ParseException( Dict.get(Dict.ERROR_NAMED_BLOCK_OPEN_AT_EOF) );
		}
	}
	
	/**
	 * Checks if the first instruments block is expected to be parsed already and have not yet been parsed.
	 * In this case an exception is thrown.
	 * 
	 * - Does nothing if called in the wrong parsing run.
	 * - Does nothing if the instruments are already parsed.
	 * - Does nothing instruments are currently being parsed.
	 * 
	 * @throws ParseException the first instruments block has not yet been parsed.
	 */
	private void checkInstrumentsParsed() throws ParseException {
		
		// wrong parsing run?
		if (!isChInstMetaParsRun) {
			return;
		}
		
		// are we currently parsing the first instruments block?
		if (MODE_INSTRUMENTS == currentMode) {
			return;
		}
		
		// instruments not yet parsed?
		if (0 == instruments.size()) {
			throw new ParseException( Dict.get(Dict.ERROR_INSTRUMENTS_NOT_DEFINED) );
		}
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
		if (P.equals(tokens[0])) {
			tokens[ 0 ] = "9";
		}
		
		// ignore the instruments block - it will be handled
		// separately by replaceInstrument()
		if (MODE_INSTRUMENTS == currentMode) {
			return;
		}
		
		// only care about normal channel commands
		if (! tokens[0].matches("^\\d{1,2}$")) {
			return;
		}
		
		// percussion instrument name --> number
		if (Dict.UNKNOWN_CODE != Dict.getPercussion(tokens[1])) {
			tokens[ 1 ] = Integer.toString( Dict.getPercussion(tokens[1]) );
		}
		
		// note name --> number
		else if (! "9".equals(tokens[0])) {
			if (Dict.UNKNOWN_CODE != Dict.getNote(tokens[1])) {
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
		if (Dict.UNKNOWN_CODE != Dict.getDrumkit(shortcut)) {
			shortcut = Integer.toString( Dict.getDrumkit(shortcut) );
		}
		
		// instrument name --> number
		else if (channel != 9) {
			if (Dict.UNKNOWN_CODE != Dict.getInstrument(shortcut)) {
				shortcut = Integer.toString( Dict.getInstrument(shortcut) );
			}
		}
		
		int number = toInt( shortcut );
		
		// check range
		if (number > 127) {
			throw new ParseException( Dict.get(Dict.ERROR_INSTR_BANK) );
		}
		
		return number;
	}
	
	/**
	 * Parses the duration string from a channel command and calculates the
	 * duration in ticks.
	 * 
	 * The duration can be a sum of durations, separated by **+** characters.
	 * 
	 * @param s  The duration string, extracted from the MidicaPL line.
	 * @return   The duration of the note in ticks.
	 * @throws ParseException  If the duration string cannot be parsed.
	 */
	protected int parseDuration(String s) throws ParseException {
		String[] summands = s.split(Pattern.quote(LENGTH_PLUS), -1);
		int      duration = 0;
		for (String summand : summands) {
			if ("".equals(summand))
				throw new ParseException( Dict.get(Dict.ERROR_EMPTY_LENGTH_SUMMAND) + s );
			duration += parseDurationSummand(summand);
		}
		
		return duration;
	}
	
	/**
	 * Parses the duration string from one duration summand channel command
	 * and calculates the duration in ticks.
	 * 
	 * @param s  One duration summand, extracted from the MidicaPL line.
	 * @return   The duration of the summand in ticks.
	 * @throws ParseException  If the duration summand cannot be parsed.
	 */
	private int parseDurationSummand(String s) throws ParseException {
		Pattern pattern = Pattern.compile(
			  "^(\\d+|.+?)"                // basic divisor (basic note length)
			+ "(("                         // open capturing group for modifiers
			+ Pattern.quote(TUPLET_INTRO)  //   customized tuplet
			+ "\\d+"                       //     first number of the customized tuplet
			+ Pattern.quote(TUPLET_FOR)    //     separator between the 2 numbers
			+ "\\d+"                       //     second number of the customized tuplet
			+ "|" + Pattern.quote(TRIPLET) //   ordinary triplet
			+ "|" + Pattern.quote(DOT)     //   dotted
			+ ")*)"                        // close capturing group
			+ "$"
		);
		Matcher matcher = pattern.matcher( s );
		if (matcher.matches()) {
			String prefix  = matcher.group( 1 );
			String postfix = matcher.group( 2 );
			int    factor  = 4; // the resolution is for a quarter note but we are based on a full note
			int    divisor = 1;
			
			// parse unmodified summand length
			if (prefix.matches("^\\d+$"))
				divisor = toInt( prefix );
			else if (LENGTH_32.equals(prefix))
				divisor = 32;
			else if (LENGTH_16.equals(prefix))
				divisor = 16;
			else if (LENGTH_8.equals(prefix))
				divisor = 8;
			else if (LENGTH_4.equals(prefix))
				divisor = 4;
			else if (LENGTH_2.equals(prefix))
				divisor = 2;
			else if (LENGTH_1.equals(prefix))
				divisor = 1;
			else if (LENGTH_M1.equals(prefix))
				factor *= 1;
			else if (LENGTH_M2.equals(prefix))
				factor *= 2;
			else if (LENGTH_M4.equals(prefix))
				factor *= 4;
			else if (LENGTH_M8.equals(prefix))
				factor *= 8;
			else if (LENGTH_M16.equals(prefix))
				factor *= 16;
			else if (LENGTH_M32.equals(prefix))
				factor *= 32;
			else
				throw new ParseException( Dict.get(Dict.ERROR_NOTE_LENGTH_INVALID) + s );
			
			// parse modifications by dots
			int dot_count = 0;
			DOT:
			while (postfix.matches(".*" + Pattern.quote(DOT) + ".*")) {
				dot_count++;
				postfix = postfix.replaceFirst( Pattern.quote(DOT), "" );
			}
			if (dot_count > 0) {
				// dots modify the note length like this:
				// .: 3/2; ..: 7/4; ...: 15/8, ....: 31/16 and so on
				// in other words: length = 2 * length - ( length / 2 ^ dot_count )
				int power = (int) Math.pow( 2, dot_count );
				factor    = factor  * (2 * power - 1);
				divisor   = divisor * power;
			}
			
			// parse modifications by (nested) tuplets
			// this must be before the triplet parsing to support the same symbol
			// for tuplets and triplets
			Pattern tupletPattern = Pattern.compile(
				".*" + Pattern.quote(TUPLET_INTRO) + "(\\d+)" + Pattern.quote(TUPLET_FOR) + "(\\d+)" + ".*"
			);
			TUPLET:
			while (postfix.matches(tupletPattern.toString())) {
				Matcher tupletMatcher = tupletPattern.matcher( postfix );
				if (tupletMatcher.matches()) {
					int count    = toInt( tupletMatcher.group(1), true );
					int countFor = toInt( tupletMatcher.group(2), true );
					// cut away the matched tuplet
					postfix = postfix.replaceFirst(
						Pattern.quote(TUPLET_INTRO) + count + Pattern.quote(TUPLET_FOR) + countFor,
						""
					);
					// a tuplet a:b (a for b) modifies the note length by the factor b/a
					factor  *= countFor;
					divisor *= count;
				}
			}
			
			// parse modifications by (nested) triplets
			TRIPLET:
			while (postfix.matches(".*" + Pattern.quote(TRIPLET) + ".*")) {
				// cut away the matched triplet
				postfix = postfix.replaceFirst( Pattern.quote(TRIPLET), "" );
				// nested triplets modify the note length by 2/3 for each nesting
				factor  *= 2;
				divisor *= 3;
			}
			
			int resolution = SequenceCreator.getResolution();
			
			// Theoretically: duration = resolution * factor / divisor
			// But integer divisions are always rounded down and we want to round mathematically
			int duration = ( resolution * factor * 10 + divisor * 5 ) / ( divisor * 10 );
			return duration;
		}
		else
			// pattern doesn't match
			throw new ParseException( Dict.get(Dict.ERROR_NOTE_LENGTH_INVALID) + s );
	}
	
	/**
	 * Parses a mode command.
	 * 
	 * This is one of the following commands:
	 * 
	 * - INSTRUMENTS
	 * - FUNCTION
	 * - PATTERN
	 * - META
	 * - SOFT_KARAOKE
	 * - END
	 * 
	 * Determines the type of mode command and calls the appropriate method to
	 * parse that command type.
	 * 
	 * @param tokens            Mode command token array.
	 * @throws ParseException   If the mode command cannot be parsed.
	 */
	private void parseModeCmd(String[] tokens) throws ParseException {
		String cmd = tokens[0];
		
		if (END.equals(cmd)) {
			parseEND(tokens);
		}
		else if (FUNCTION.equals(cmd)) {
			parseFUNCTION(tokens);
		}
		else if (PATTERN.equals(cmd)) {
			parsePATTERN(tokens);
		}
		else if (INSTRUMENTS.equals(cmd)) {
			if (1 == tokens.length ) {
				currentMode = MODE_INSTRUMENTS;
			}
			else {
				throw new ParseException( Dict.get(Dict.ERROR_MODE_INSTR_NUM_OF_ARGS) );
			}
		}
		else if (META.equals(cmd)) {
			parseMETA(tokens);
		}
		else if (META_SOFT_KARAOKE.equals(cmd)) {
			parseSOFT_KARAOKE(tokens);
		}
		
		// other - may never happen
		else {
			throw new ParseException(
				"Invalid Command " + cmd + " found."
				+ "This should not happen. Please report."
			);
		}
	}
	
	/**
	 * Parses a command that is not allowed inside of any block and has nothing to do with blocks.
	 * 
	 * This is one of the following commands:
	 * 
	 * - DEFINE
	 * - CHORD
	 * - INCLUDE
	 * - SOUNDFONT
	 * 
	 * Checks if we are in the root level.
	 * 
	 * Determines the type of root-level command and calls the appropriate method to
	 * parse that command.
	 * 
	 * @param tokens            Mode command token array.
	 * @throws ParseException   If the command cannot be parsed or is inside of a block.
	 */
	private void parseRootLevelCmd(String[] tokens) throws ParseException {
		String cmd = tokens[0];
		
		// check nesting
		if (currentMode != MODE_DEFAULT || nestableBlkDepth > 0) {
			throw new ParseException( Dict.get(Dict.ERROR_NOT_ALLOWED_IN_BLK) + cmd );
		}
		
		// parse command
		if (CHORD.equals(cmd)) {
			parseCHORD(tokens);
		}
		else if (INCLUDE.equals(cmd) || ORIGINAL_INCLUDE.equals(cmd)) {
			parseINCLUDE(tokens);
		}
		else if (SOUNDFONT.equals(cmd)) {
			parseSOUNDFONT(tokens);
		}
		else if (DEFINE.equals(cmd) || ORIGINAL_DEFINE.equals(cmd)) {
			parseDEFINE(tokens);
		}
		else if (CONST.equals(cmd)) {
			if (isConstParsRun) {
				parseCONST(tokens);
			}
		}
		else {
			throw new ParseException( Dict.get(Dict.ERROR_UNKNOWN_CMD) + cmd );
		}
	}
	
	/**
	 * Parses an END command.
	 * An END command is a mode command that finishes a previously opened mode.
	 * 
	 * @param tokens             Token array.
	 * @throws ParseException    If the command cannot be parsed.
	 */
	private void parseEND( String[] tokens ) throws ParseException {
		if (1 == tokens.length) {
			if (MODE_DEFAULT == currentMode) {
				throw new ParseException( Dict.get(Dict.ERROR_CMD_END_WITHOUT_BEGIN) );
			}
			if (MODE_INSTRUMENTS == currentMode) {
				// create defined and undefined instruments for all channels
				// but only if not yet done
				if (! instrumentsParsed) {
					postprocessInstruments();
				}
			}
			if (MODE_SOFT_KARAOKE == currentMode) {
				currentMode         = MODE_META;
				currentFunctionName = null;
				return;
			}
			currentMode         = MODE_DEFAULT;
			currentFunctionName = null;
		}
		else
			throw new ParseException( Dict.get(Dict.ERROR_ARGS_NOT_ALLOWED) );
	}
	
	/**
	 * Parses a FUNCTION command.
	 * A FUNCTION command is a mode command that opens a FUNCTION definition.
	 * 
	 * @param tokens             Token array.
	 * @throws ParseException    If the command cannot be parsed.
	 */
	private void parseFUNCTION(String[] tokens) throws ParseException {
		if (MODE_DEFAULT != currentMode) {
			throw new ParseException( Dict.get(Dict.ERROR_FUNCTION_NOT_ALLOWED_HERE) );
		}
		if (2 == tokens.length) {
			currentMode         = MODE_FUNCTION;
			currentFunctionName = tokens[1];
			if (functions.containsKey(currentFunctionName)) {
				throw new ParseException( Dict.get(Dict.ERROR_FUNCTION_ALREADY_DEFINED) + currentFunctionName );
			}
			
			// only collect the function name?
			if (isFuncNameParsRun) {
				definedFunctionNames.add(currentFunctionName);
				return;
			}
			
			// real function parsing run
			currentFunction = new ArrayList<>();
			functions.put(currentFunctionName, currentFunction);
			
			// add to stack
			functionToFile.put(currentFunctionName, file);
			functionToLineOffset.put(currentFunctionName, currentLineNumber);
		}
		else
			throw new ParseException( Dict.get(Dict.ERROR_FUNCTION_NUM_OF_ARGS) );
	}
	
	/**
	 * Parses a PATTERN command.
	 * A PATTERN command is a mode command that opens a PATTERN definition.
	 * 
	 * @param tokens             Token array.
	 * @throws ParseException    If the command cannot be parsed.
	 */
	private void parsePATTERN(String[] tokens) throws ParseException {
		if (MODE_DEFAULT != currentMode) {
			throw new ParseException( Dict.get(Dict.ERROR_PATTERN_NOT_ALLOWED_HERE) );
		}
		if (2 == tokens.length) {
			currentMode        = MODE_PATTERN;
			currentPatternName = tokens[1];
			if (patterns.containsKey(currentPatternName)) {
				throw new ParseException( Dict.get(Dict.ERROR_PATTERN_ALREADY_DEFINED) + currentPatternName );
			}
			
			// only collect the function name?
			if (isFuncNameParsRun) {
				definedPatternNames.add(currentPatternName);
				return;
			}
			
			// real function parsing run
			currentPattern = new ArrayList<>();
			patterns.put(currentPatternName, currentPattern);
			
			// add to stack
			patternToFile.put(currentPatternName, file);
			patternToLineOffset.put(currentPatternName, currentLineNumber);
		}
		else
			throw new ParseException( Dict.get(Dict.ERROR_PATTERN_NUM_OF_ARGS) );
	}
	
	/**
	 * Parses a META command.
	 * A META command is a mode command that opens a META definition block.
	 * 
	 * @param tokens             Token array.
	 * @throws ParseException    If the command cannot be parsed.
	 */
	private void parseMETA(String[] tokens) throws ParseException {
		if (MODE_DEFAULT != currentMode) {
			throw new ParseException( Dict.get(Dict.ERROR_META_NOT_ALLOWED_HERE) );
		}
		if (1 == tokens.length) {
			currentMode = MODE_META;
		}
		else
			throw new ParseException( Dict.get(Dict.ERROR_META_NUM_OF_ARGS) );
	}
	
	/**
	 * Parses a SOFT_KARAOKE command.
	 * A SOFT_KARAOKE command is a mode command that opens a SOFT_KARAOKE definition block
	 * inside of a META block.
	 * 
	 * @param tokens             Token array.
	 * @throws ParseException    If the command cannot be parsed.
	 */
	private void parseSOFT_KARAOKE(String[] tokens) throws ParseException {
		if (MODE_META != currentMode) {
			throw new ParseException( Dict.get(Dict.ERROR_SOFT_KARAOKE_NOT_ALLOWED_HERE) );
		}
		if (1 == tokens.length) {
			if (isSoftKaraoke) {
				throw new ParseException( Dict.get(Dict.ERROR_SOFT_KARAOKE_ALREADY_SET) );
			}
			currentMode   = MODE_SOFT_KARAOKE;
			isSoftKaraoke = true;
		}
		else
			throw new ParseException( Dict.get(Dict.ERROR_SOFT_KARAOKE_NUM_OF_ARGS) );
	}
	
	/**
	 * Parses opening or closing a nestable block.
	 * By default, that is **{** or **}**.
	 * 
	 * @param tokens             Token array.
	 * @param isFake             **true**, if this is called inside a function definition or block.
	 * @throws ParseException    If the command cannot be parsed.
	 */
	private void parseBLOCK(String[] tokens, boolean isFake) throws ParseException {
		String cmd = tokens[0];
		
		// check nesting
		if (BLOCK_OPEN.equals(cmd)) {
			nestableBlkDepth++;
		}
		else if (BLOCK_CLOSE.equals(cmd)) {
			nestableBlkDepth--;
		}
		if (nestableBlkDepth < 0) {
			throw new ParseException( Dict.get(Dict.ERROR_BLOCK_UNMATCHED_CLOSE) );
		}
		
		// construct options string from tokens
		StringBuilder optionsStrBuf = new StringBuilder("");
		String        optionsStr    = "";
		if (tokens.length > 1) {
			for (int i = 1; i < tokens.length; i++) {
				optionsStrBuf.append(tokens[i] + " ");
			}
			optionsStr = clean(optionsStrBuf.toString());
		}
		
		// get options (quantity / multiple)
		int     quantity       = -1;
		int     shift          =  0;
		boolean multiple       = false;
		String  condIf         = null;
		String  condElsif      = null;
		boolean isElse         = false;
		String  tuplet         = null;
		boolean mustCheckChain = isDefaultParsRun && nestableBlkDepth == 0;
		if (optionsStr.length() > 0) {
			ArrayList<CommandOption> options = parseOptions(optionsStr, isFake);
			for (CommandOption opt : options) {
				String optName = opt.getName();
				if (OPT_QUANTITY.equals(optName))
					quantity = opt.getQuantity();
				else if (OPT_MULTIPLE.equals(optName))
					multiple = true;
				else if (OPT_TUPLET.equals(optName))
					tuplet = opt.getTuplet();
				else if (OPT_SHIFT.equals(optName))
					shift += opt.getShift();
				else if (OPT_IF.equals(optName)) {
					if (condIf != null || condElsif != null || isElse) {
						throw new ParseException( Dict.get(Dict.ERROR_BLOCK_IF_MUST_BE_ALONE) );
					}
					condIf = opt.getCondition();
				}
				else if (OPT_ELSIF.equals(optName)) {
					if (condIf != null || condElsif != null || isElse) {
						throw new ParseException( Dict.get(Dict.ERROR_BLOCK_ELSIF_MUST_BE_ALONE) );
					}
					if (mustCheckChain && ! condChainOpened) {
						throw new ParseException( Dict.get(Dict.ERROR_BLOCK_NO_IF_FOUND) + ": " + optName );
					}
					condElsif = opt.getCondition();
				}
				else if (OPT_ELSE.equals(optName)) {
					if (condIf != null || condElsif != null || isElse) {
						throw new ParseException( Dict.get(Dict.ERROR_BLOCK_ELSE_MUST_BE_ALONE) );
					}
					if (mustCheckChain && ! condChainOpened) {
						throw new ParseException( Dict.get(Dict.ERROR_BLOCK_NO_IF_FOUND) + ": " + optName );
					}
					isElse = true;
				}
				else
					throw new ParseException( Dict.get(Dict.ERROR_BLOCK_INVALID_OPT) + optName );
				
				// only check?
				if (isCondCheckParsRun) {
					if (OPT_IF.equals(optName) || OPT_ELSIF.equals(optName)) {
						if (OPT_IF.equals(optName))
							evalCondition(condIf);
						else if (OPT_ELSIF.equals(optName))
							evalCondition(condElsif);
					}
				}
			}
		}
		
		// only check?
		if (isFake || ! isDefaultParsRun) {
			return;
		}
		
		// parse open and close
		NestableBlock block;
		if (BLOCK_OPEN.equals(cmd)) {
			block = new NestableBlock(this);
			
			// nested?
			if (nestableBlkDepth > 1) {
				NestableBlock parentBlock = nestableBlkStack.peek();
				parentBlock.add(block);
			}
			
			// add to stack
			nestableBlkStack.push(block);
		}
		else {
			// BLOCK_CLOSE
			block = nestableBlkStack.pop();
		}
		
		// apply options
		if (multiple)
			block.setMultiple(OPT_MULTIPLE);
		if (shift != 0)
			block.setShift(shift, OPT_SHIFT);
		if (quantity != -1)
			block.setQuantity(quantity, OPT_QUANTITY);
		if (tuplet != null) {
			if (TRIPLET.equals(tuplet))
				block.setTuplet(TRIPLET, OPT_TUPLET);
			else
				block.setTuplet(TUPLET_INTRO + tuplet, OPT_TUPLET);
		}
		if (condIf != null)
			block.setIf(condIf);
		if (condElsif != null)
			block.setElsif(condElsif);
		if (isElse)
			block.setElse();
		
		// apply root block
		if (BLOCK_CLOSE.equals(cmd) && nestableBlkDepth == 0) {
			int  line = currentLineNumber;
			File file = this.file;
			
			// get condition from the WHOLE block (not only the closing brace)
			String condition     = block.getCondition();
			int    conditionType = block.getConditionType();
			
			if ( ! functionNameStack.isEmpty() ) {
				String functionName = functionNameStack.peek();
				int offset = functionToLineOffset.get(functionName);
				line = offset + functionLineStack.peek();
				file = functionFileStack.peek();
			}
			if ( ! patternNameStack.isEmpty() ) {
				String patternName = patternNameStack.peek();
				int offset = patternToLineOffset.get(patternName);
				line = offset + patternLineStack.peek();
				file = patternFileStack.peek();
			}
			block.applyTupletsAndShifts(null, 0);
			boolean mustPlay = true;
			try {
				if (COND_TYPE_NONE == conditionType) {
					condChainHit    = false;
					condChainOpened = false;
				}
				else {
					if (COND_TYPE_IF == conditionType) {
						condChainHit = false;
					}
					
					// check if-elsif-else
					if (COND_TYPE_ELSIF == conditionType || COND_TYPE_ELSE == conditionType) {
						if ( ! condChainOpened )
							throw new ParseException( Dict.get(Dict.ERROR_BLOCK_NO_IF_FOUND) );
					}
					if (COND_TYPE_ELSE == conditionType)
						mustPlay = ! condChainHit;
					else if (condChainHit)
						mustPlay = false;
					else
						mustPlay = evalCondition(condition);
				}
			}
			catch (ParseException e) {
				e.setCausedByBlockConditions();
				throw e;
			}
			
			if (mustPlay) {
				block.play(stackTrace, true, file, line);
			}
			
			// postprocess conditions
			if (COND_TYPE_ELSE == conditionType) {
				condChainOpened = false;
				condChainHit    = false;
			}
			else if (conditionType != COND_TYPE_NONE) {
				condChainOpened = true;
				condChainHit    = condChainHit || mustPlay;
			}
		}
	}
	
	/**
	 * Parses a CHORD command.
	 * A CHORD command defines which a new chord name and describes the included notes.
	 * 
	 * @param tokens             Token array.
	 * @throws ParseException    If the command cannot be parsed.
	 */
	private void parseCHORD(String[] tokens) throws ParseException {
		if (MODE_DEFAULT != currentMode) {
			throw new ParseException( Dict.get(Dict.ERROR_CHORD_DEF_NOT_ALLOWED_HERE) );
		}
		String chordDef;
		if (2 == tokens.length) {
			// e.g. CHORD crd=c,d,e
			chordDef = tokens[ 1 ];
		}
		else if (3 == tokens.length) {
			// e.g. CHORD crd = c, d, e
			// or   CHORD crd c d e
			chordDef = tokens[ 1 ] + " " + tokens[ 2 ];
		}
		else
			throw new ParseException( Dict.get(Dict.ERROR_CHORD_NUM_OF_ARGS) );
		
		// get and process chord name
		String[] chordParts = chordDef.split( "[" + Pattern.quote(CHORD_ASSIGNER) + "\\s]+", 2 ); // chord name and chords can be separated by CHORD_ASSIGNER (e,g, "=") and/or whitespace(s)
		if (chordParts.length < 2) {
			throw new ParseException( Dict.get(Dict.ERROR_CHORD_NUM_OF_ARGS) );
		}
		String chordName  = chordParts[ 0 ];
		String chordValue = chordParts[ 1 ];
		if (chords.containsKey(chordName)) {
			throw new ParseException( Dict.get(Dict.ERROR_CHORD_ALREADY_DEFINED) + chordName );
		}
		else if (Dict.noteExists(chordName)) {
			throw new ParseException( Dict.get(Dict.ERROR_CHORD_EQUALS_NOTE) + chordName );
		}
		else if (Dict.percussionExists(chordName)) {
			throw new ParseException( Dict.get(Dict.ERROR_CHORD_EQUALS_PERCUSSION) + chordName );
		}
		
		// get and process chord elements
		TreeSet<Integer> chord = new TreeSet<>();
		String[] notes = chordValue.split( "[" + CHORD_SEPARATOR + "\\s]+" ); // notes of the chord can be separated by CHORD_SEPARATOR (e,g, "=") and/or whitespace(s)
		
		for (String note : notes) {
			int noteVal = parseNote( note );
			if (chord.contains(noteVal)) {
				throw new ParseException( Dict.get(Dict.ERROR_CHORD_CONTAINS_ALREADY) + note );
			}
			chord.add( noteVal );
		}
		chords.put( chordName, chord );
	}
	
	/**
	 * Parses a CALL command.
	 * A CALL command calls a previously defined FUNCTION.
	 * 
	 * @param tokens             Token array.
	 * @param isFake             **true**, if this is called inside a function definition or block
	 * @throws ParseException    If the command cannot be parsed.
	 */
	private void parseCALL(String[] tokens, boolean isFake) throws ParseException {
		
		if (tokens.length < 2)
			throw new ParseException( Dict.get(Dict.ERROR_CALL_NUM_OF_ARGS) );
		
		// parse call
		String functionName = null;
		String paramString  = null;
		String optionString = null;
		Matcher callMatcher = callPattern.matcher(tokens[1]);
		if (callMatcher.matches()) {
			functionName = callMatcher.group( 1 );
			paramString  = callMatcher.group( 3 );
			optionString = callMatcher.group( 4 );
		}
		else {
			throw new ParseException( Dict.get(Dict.ERROR_CALL_SYNTAX) );
		}
		
		// parse options
		int     quantity = 1;
		int     shift    = 0;
		boolean multiple = false;
		String  condIf   = null;
		if (optionString != null && ! "".equals(optionString)) {
			ArrayList<CommandOption> options = parseOptions(optionString, isFake);
			for (CommandOption opt : options) {
				String optName = opt.getName();
				if (OPT_QUANTITY.equals(optName))
					quantity = opt.getQuantity();
				else if (OPT_SHIFT.equals(optName))
					shift += opt.getShift();
				else if (OPT_MULTIPLE.equals(optName))
					multiple = true;
				else if (OPT_IF.equals(optName)) {
					if (condIf != null) {
						throw new ParseException( Dict.get(Dict.ERROR_CALL_IF_MUST_BE_ALONE) + optName );
					}
					condIf = opt.getCondition();
					
					// only check?
					if (isCondCheckParsRun) {
						evalCondition(condIf);
					}
				}
				else
					throw new ParseException( Dict.get(Dict.ERROR_CALL_UNKNOWN_OPT) + optName );
			}
		}
		
		if (isCondCheckParsRun)
			return;
		
		// check function name
		if (functionName.equals(currentFunctionName)) {
			throw new ParseException( Dict.get(Dict.ERROR_FUNCTION_RECURSION) );
		}
		if (! definedFunctionNames.contains(functionName)) {
			throw new ParseException( Dict.get(Dict.ERROR_FUNCTION_UNDEFINED) );
		}
		
		// parse parameters
		Object[] parsedParams = parseParameters(paramString);
		ArrayList<String>       paramsIndexed = (ArrayList<String>)       parsedParams[0];
		HashMap<String, String> paramsNamed   = (HashMap<String, String>) parsedParams[1];
		
		// only fake?
		if (isFake) {
			return;
		}
		
		// check if condition
		if (condIf != null && ! evalCondition(condIf))
			return;
		
		// remember current tickstamps
		ArrayList<Long> tickstamps = rememberTickstamps();
		
		// fetch the right function
		ArrayList<String> function = functions.get(functionName);
		
		// add params to call stack
		File              file       = functionToFile.get(functionName);
		int               lineOffset = functionToLineOffset.get(functionName);
		StackTraceElement traceElem  = new StackTraceElement(file, lineOffset);
		traceElem.setFunctionName(functionName);
		traceElem.setParams(paramString);
		traceElem.setOptions(optionString);
		stackTrace.push(traceElem);
		functionNameStack.push(functionName);
		functionLineStack.push(0);
		functionFileStack.push(file);
		paramStackIndexed.push(paramsIndexed);
		paramStackNamed.push(paramsNamed);
		
		// check recursion
		if (functionNameStack.size() > MAX_RECURSION_DEPTH_FUNCTION) {
			throw new ParseException( Dict.get(Dict.ERROR_FUNCTION_RECURSION_DEPTH) );
		}
		
		// apply all lines of the called function
		for (int i = 0; i < quantity; i++) {
			
			// reset line in stacks
			traceElem.resetLine();
			functionLineStack.pop();
			functionLineStack.push(0);
			
			for (String functionLine : function) {
				
				// increment line in stacks
				traceElem.incrementLine();
				int lineNum = functionLineStack.pop();
				functionLineStack.push(lineNum + 1);
				
				// apply shift, if needed
				if (shift != 0) {
					if ( ! functionLine.startsWith(VAR) ) {
						functionLine = replaceVariables(functionLine);
						String[] functionTokens = functionLine.split("\\s+", 3);
						functionTokens = addShiftToOptions(functionTokens, shift);
						functionLine   = String.join(" ", functionTokens);
					}
				}
				
				parseLine(functionLine);
			}
		}
		
		// restore tickstamps, if needed
		if (multiple) {
			restoreTickstamps(tickstamps);
		}
		
		// remove params from call stack
		stackTrace.pop();
		functionNameStack.pop();
		functionLineStack.pop();
		functionFileStack.pop();
		paramStackIndexed.pop();
		paramStackNamed.pop();
	}
	
	/**
	 * Parses a channel call with a pattern instead of a note length.
	 * 
	 * @param tokens    Token array.
	 * @param isFake    **true**, if this is called inside a function definition or block
	 * @throws ParseException if something is wrong.
	 */
	private void parsePatternCall(String[] tokens, boolean isFake) throws ParseException {
		
		// care about whitespaces in pattern parameters
		tokens = reorganizePatternCallTokens(tokens, 2);
		
		if (tokens.length < 3) {
			throw new ParseException( Dict.get(Dict.ERROR_CH_CMD_NUM_OF_ARGS) );
		}
		
		// parse call
		String  patternName    = null;
		String  paramString    = null;
		String  outerOptStr    = null;
		Matcher patCallMatcher = callPattern.matcher(tokens[2]);
		if (patCallMatcher.matches()) {
			patternName  = patCallMatcher.group( 1 );
			paramString  = patCallMatcher.group( 3 );
			outerOptStr  = patCallMatcher.group( 4 );
		}
		else {
			throw new ParseException( "Pattern Call Error\nThis should not happen. Please report." );
		}
		
		// get channel and pattern content
		int               channel      = toChannel(tokens[0]);
		ArrayList<String> patternLines = patterns.get(patternName);
		
		// init instruments if not yet done
		if (! instrumentsParsed) {
			postprocessInstruments();
		}
		
		// remember channel state
		Instrument instr = instruments.get(channel);
		long  outerStartTicks = instr.getCurrentTicks();
		int   outerVelocity   = instr.getVelocity();
		float outerDuration   = instr.getDurationRatio();
		
		// process pattern call options (outer options)
		boolean outerMultiple = false;
		int     outerQuantity = 1;
		int     outerShift    = 0;
		String  outerSyllable = null;
		if (outerOptStr != null) {
			ArrayList<CommandOption> callOptions = parseOptions(outerOptStr, false);
			
			for (CommandOption opt : callOptions) {
				String optName = opt.getName();
				
				if (OPT_VELOCITY.equals(optName)) {
					int velocity = opt.getVelocity();
					if (! isFake)
						instr.setVelocity(velocity);
					outerVelocity = velocity;
				}
				else if (OPT_DURATION.equals(optName)) {
					float durationRatio = opt.getDuration();
					if (! isFake)
						instr.setDurationRatio(durationRatio);
					outerDuration = durationRatio;
				}
				else if (OPT_MULTIPLE.equals(optName)) {
					outerMultiple = true;
				}
				else if (OPT_QUANTITY.equals(optName)) {
					outerQuantity = opt.getQuantity();
				}
				else if (OPT_LYRICS.equals(optName)) {
					outerSyllable = opt.getLyrics();
					if (isSoftKaraoke && crlfSkPattern.matcher(outerSyllable).find()) {
						throw new ParseException( Dict.get(Dict.ERROR_SK_SYLLABLE_CRLF_NOT_ALLOWED) );
					}
				}
				else if (OPT_SHIFT.equals(optName)) {
					outerShift += opt.getShift();
				}
				else
					throw new ParseException( Dict.get(Dict.ERROR_PATTERN_INVALID_OUTER_OPT) + optName );
			}
		}
		
		// get notes belonging to the chord
		ArrayList<Integer> notes = parseChord(tokens[1]);
		if (null == notes) {
			
			// only one single note
			notes = new ArrayList<>();
			int note = parseNote(tokens[1]);
			notes.add(note);
		}
		Integer[] noteNumbers = notes.toArray(new Integer[0]);
		
		// parse parameters
		Object[] parsedParams = parseParameters(paramString);
		ArrayList<String>       paramsIndexed = (ArrayList<String>)       parsedParams[0];
		HashMap<String, String> paramsNamed   = (HashMap<String, String>) parsedParams[1];
		
		// only fake?
		if (isFake) {
			return;
		}
		
		// add params to call stack
		File              file       = patternToFile.get(patternName);
		int               lineOffset = patternToLineOffset.get(patternName);
		StackTraceElement traceElem  = new StackTraceElement(file, lineOffset);
		traceElem.setPatternName(patternName);
		traceElem.setParams(paramString);
		traceElem.setOptions(outerOptStr);
		stackTrace.push(traceElem);
		patternNameStack.push(patternName);
		patternLineStack.push(0);
		patternFileStack.push(file);
		paramStackIndexed.push(paramsIndexed);
		paramStackNamed.push(paramsNamed);
		
		// check recursion
		if (patternNameStack.size() > MAX_RECURSION_DEPTH_PATTERN) {
			throw new ParseException( Dict.get(Dict.ERROR_PATTERN_RECURSION_DEPTH) );
		}
		
		// apply pattern lines
		// OUTER_QUANTITY:
		for (int i = 0; i < outerQuantity; i++) {
			
			// reset line in stacks
			traceElem.resetLine();
			patternLineStack.pop();
			patternLineStack.push(0);
			
			if (! isFake && outerSyllable != null) {
				applySyllable(outerSyllable, outerStartTicks);
			}
			
			PATTERN_LINE:
			for (String patternLine : patternLines) {
				
				// increment line in stacks
				traceElem.incrementLine();
				int lineNum = patternLineStack.pop();
				patternLineStack.push(lineNum + 1);
				
				// replace variables
				currentLineContent = patternLine;
				patternLine        = replaceVariables(patternLine);
				currentLineContent = patternLine;
				
				// special line inside the pattern?
				String[] patLineTokens = whitespace.split(patternLine);
				if (patLineTokens.length > 0) {
					
					// empty line?
					if ("".equals(patLineTokens[0])) {
						parseTokens(patLineTokens);
						continue PATTERN_LINE;
					}
					
					// block?
					if (BLOCK_OPEN.equals(patLineTokens[0]) || BLOCK_CLOSE.equals(patLineTokens[0])) {
						parseTokens(patLineTokens);
						continue PATTERN_LINE;
					}
				}
				
				// from now on assume a normal pattern line, beginning with indices
				String[] patternTokens         = patternLine.split("\\s+", 3);
				patternTokens                  = reorganizePatternCallTokens(patternTokens, 1); // for nested pattern calls
				String[]          indexStrings = patternTokens[0].split(Pattern.quote(PATTERN_INDEX_SEP), -1);
				ArrayList<String> lineNotes    = new ArrayList<String>();
				
				// process pattern line options (inner options)
				boolean innerMultiple = false;
				Integer innerQuantity = null;
				Integer innerVelocity = null;
				Float   innerDuration = null;
				String  innerTremolo  = null;
				if (patternTokens.length > 2) {
					ArrayList<CommandOption> patternOptions = parseOptions(patternTokens[2], false);
					
					for (CommandOption opt : patternOptions) {
						String optName = opt.getName();
						
						if (OPT_VELOCITY.equals(optName)) {
							innerVelocity = opt.getVelocity();
						}
						else if (OPT_DURATION.equals(optName)) {
							innerDuration = opt.getDuration();
						}
						else if (OPT_MULTIPLE.equals(optName)) {
							innerMultiple = true;
						}
						else if (OPT_QUANTITY.equals(optName)) {
							innerQuantity = opt.getQuantity();
						}
						else if (OPT_TREMOLO.equals(optName)) {
							innerTremolo = opt.getValueString();
						}
						else
							throw new ParseException( Dict.get(Dict.ERROR_PATTERN_INVALID_INNER_OPT) + optName );
					}
				}
				
				// rest?
				if (REST.equals(patLineTokens[0])) {
					lineNotes.add(REST);
				}
				else {
					// INDEX:
					for (String indexStr : indexStrings) {
						try {
							int index = Integer.parseInt(indexStr);
							int note  = noteNumbers[index];
							lineNotes.add(note + "");
						}
						catch (NumberFormatException e) {
							throw new ParseException( Dict.get(Dict.ERROR_PATTERN_INDEX_INVALID) + indexStr );
						}
						catch (IndexOutOfBoundsException e) {
							throw new ParseException( Dict.get(Dict.ERROR_PATTERN_INDEX_TOO_HIGH) + indexStr );
						}
					}
				}
				
				// construct resulting pattern line options
				ArrayList<String> lineOptions = new ArrayList<String>();
				if (innerMultiple) {
					lineOptions.add(M);
				}
				if (innerQuantity != null) {
					lineOptions.add(Q + OPT_ASSIGNER + innerQuantity);
				}
				if (innerVelocity != null) {
					lineOptions.add(V + OPT_ASSIGNER + innerVelocity);
				}
				if (innerDuration != null) {
					lineOptions.add(D + OPT_ASSIGNER + innerDuration);
				}
				if (innerTremolo != null) {
					lineOptions.add(TR + OPT_ASSIGNER + innerTremolo);
				}
				if (outerShift != 0) {
					lineOptions.add(S + OPT_ASSIGNER + outerShift);
				}
				
				// construct resulting tokens for the current pattern line
				ArrayList<String> lineTokens = new ArrayList<String>();
				lineTokens.add(channel + "");
				lineTokens.add(String.join(CHORD_SEPARATOR, lineNotes));
				lineTokens.add(patternTokens[1]);
				if (lineOptions.size() > 0) {
					String patternAndOptions = lineTokens.get(2);
					patternAndOptions += " " + String.join(OPT_SEPARATOR, lineOptions);
					lineTokens.set(2, patternAndOptions);
				}
				
				// parse the resulting line
				isPatternSubChord = lineNotes.size() > 1;
				parseTokens(lineTokens.toArray(new String[0]));
				isPatternSubChord = false;
			}
			
			// reset channel state (velocity + duration)
			if (! isFake) {
				instr.setVelocity(outerVelocity);
				instr.setDurationRatio(outerDuration);
			}
		}
		
		// reset ticks, if needed
		if (! isFake) {
			if (outerMultiple)
				instr.setCurrentTicks(outerStartTicks);
		}
		
		// remove params from call stack
		stackTrace.pop();
		patternNameStack.pop();
		patternLineStack.pop();
		patternFileStack.pop();
		paramStackIndexed.pop();
		paramStackNamed.pop();
	}
	
	/**
	 * Parses (indexed and named) parameters of a function call or a pattern usage.
	 * 
	 * Returns the indexed parameters in index 0 and the named parameters in index 1
	 * of the returned array.
	 * 
	 * - Indexed parameters: {@link ArrayList<String>}
	 * - Named parameters: {@link HashMap<String, String>}
	 * 
	 * @param paramStr    the parameter string
	 * @return indexed and named parameters as described above.
	 * @throws ParseException if something is wrong with the parameter string.
	 */
	private Object[] parseParameters(String paramStr) throws ParseException {
		ArrayList<String>       paramsIndexed = new ArrayList<>();
		HashMap<String, String> paramsNamed   = new HashMap<>();
		
		if (paramStr != null && ! "".equals(paramStr)) {
			paramStr = clean(paramStr);
			String[] params = paramStr.split("\\s*" + Pattern.quote(PARAM_SEPARATOR) + "\\s*", -1);
			for (String rawParam : params) {
				if ("".equals(rawParam)) {
					throw new ParseException( Dict.get(Dict.ERROR_CALL_EMPTY_PARAM) + paramStr );
				}
				
				// save as indexed param?
				if (whitespace.matcher(rawParam).find()) {
					paramsIndexed.add(null);
				}
				else {
					paramsIndexed.add(rawParam);
				}
				
				// parameter contains assigner? (e.g. ...,a=b,...)
				String[] paramParts = rawParam.split("\\s*" + Pattern.quote(PARAM_ASSIGNER) + "\\s*", -1);
				if (1 == paramParts.length) {
					// indexed
					// nothing more to do here
				}
				else if (2 == paramParts.length) {
					// named
					String name  = paramParts[0];
					String value = paramParts[1];
					if ("".equals(name)) {
						throw new ParseException( Dict.get(Dict.ERROR_CALL_PARAM_NAME_EMPTY) + paramStr );
					}
					else if ("".equals(value)) {
						throw new ParseException( Dict.get(Dict.ERROR_CALL_PARAM_VALUE_EMPTY) + name );
					}
					
					// don't allow special characters in parameter names
					Pattern paramNamePatt    = Pattern.compile("[^\\w]");
					Matcher paramNameMatcher = paramNamePatt.matcher(name);
					if (paramNameMatcher.find()) {
						throw new ParseException( Dict.get(Dict.ERROR_CALL_PARAM_NAME_WITH_SPEC) + name );
					}
					
					// duplicate param name?
					if (paramsNamed.containsKey(name)) {
						throw new ParseException( Dict.get(Dict.ERROR_CALL_DUPLICATE_PARAM_NAME) + name );
					}
					
					// add named param
					paramsNamed.put(name, value);
				}
				else {
					throw new ParseException( Dict.get(Dict.ERROR_CALL_PARAM_MORE_ASSIGNERS) + rawParam );
				}
			}
		}
		
		return new Object[]{paramsIndexed, paramsNamed};
	}
	
	/**
	 * Parses a line inside of a pattern definition.
	 * 
	 * @param tokens  command tokens
	 * @throws ParseException    if something is wrong.
	 */
	private void parsePatternLine(String[] tokens) throws ParseException {
		
		// nested pattern? - care about whitespaces in nested pattern call parameters
		tokens = reorganizePatternCallTokens(tokens, 1);
		
		// empty line?
		if (tokens.length > 0 && "".equals(tokens[0])) {
			// ok - must be added so that the line number in the stack trace is correct
		}
		else if (tokens.length > 0) {
			
			// block?
			if (BLOCK_OPEN.equals(tokens[0]) || BLOCK_CLOSE.equals(tokens[0])) {
				// ok
			}
			else if (REST.equals(tokens[0])) {
				// ok
			}
			else {
				if (tokens.length < 2) {
					throw new ParseException( Dict.get(Dict.ERROR_PATTERN_NUM_OF_ARGS) );
				}
				
				// check if indices are numbers
				String[] indexStrings = tokens[0].split(Pattern.quote(PATTERN_INDEX_SEP), -1);
				for (String indexStr : indexStrings) {
					try {
						Integer.parseInt(indexStr);
					}
					catch (NumberFormatException e) {
						throw new ParseException( Dict.get(Dict.ERROR_PATTERN_INDEX_INVALID) + indexStr );
					}
				}
				
				// check options
				if (tokens.length > 2) {
					ArrayList<CommandOption> patternOptions = parseOptions(tokens[2], true);
					
					for (CommandOption opt : patternOptions) {
						String optName = opt.getName();
						
						if (OPT_VELOCITY.equals(optName) || OPT_DURATION.equals(optName) || OPT_MULTIPLE.equals(optName)
						 || OPT_QUANTITY.equals(optName) || OPT_TREMOLO.equals(optName)) {
							// ok
						}
						else
							throw new ParseException( Dict.get(Dict.ERROR_PATTERN_INVALID_INNER_OPT) + optName );
					}
				}
			}
		}
		else if (0 == tokens.length) {
			throw new ParseException( "Pattern Error\nThis should not happen. Please report." );
		}
		
		// add line to pattern
		currentPattern.add(String.join(" ", tokens));
	}
	
	/**
	 * Ensured that (nested or real) pattern call tokens contain the pattern call
	 * and the options token in the right position.
	 * 
	 * For real pattern calls:
	 * 
	 * - index 0: channel
	 * - index 1: chord
	 * - index 2: pattern call
	 * - index 3: options
	 * 
	 * For nested pattern calls:
	 * 
	 * - index 0: chord
	 * - index 1: pattern call
	 * - index 2: options
	 * 
	 * @param tokens         tokens to be checked or changed
	 * @param indexOffset    pattern call index (**2** for real calls, **1** for nested calls)
	 * @return the corrected tokens, if changed, or the original tokens, if no change was necessary.
	 */
	private String[] reorganizePatternCallTokens(String[] tokens, int indexOffset) {
		
		int iPat = indexOffset;      // index containing the pattern call
		int iOpt = indexOffset + 1;  // index containing the options
		
		// no whitespaces to be handled?
		if (tokens.length <= indexOffset + 1) {
			return tokens;
		}
		
		String  last2columns   = tokens[iPat] + " " + tokens[iOpt];
		Matcher patCallMatcher = callPattern.matcher(last2columns);
		if (patCallMatcher.matches()) {
			String patternName = patCallMatcher.group( 1 );
			String paramString = patCallMatcher.group( 3 );
			String options     = patCallMatcher.group( 4 );
			
			// unknown pattern name (probably a duration) - don't change anything
			if (! definedPatternNames.contains(patternName)) {
				return tokens;
			}
			
			// apply pattern corrections
			if (null == options) {
				if (iPat == 1)
					tokens = new String[] {tokens[0], last2columns};
				else if (iPat == 2)
					tokens = new String[] {tokens[0], tokens[1], last2columns};
			}
			else {
				if (null == paramString)
					paramString = "";
				tokens[iPat] = patternName + PARAM_OPEN + paramString + PARAM_CLOSE;
				tokens[iOpt] = options;
			}
		}
		
		return tokens;
	}
	
	/**
	 * Unescapes the given syllable and puts it into the MIDI sequence.
	 * 
	 * Takes care about SoftKaraoke.
	 * 
	 * @param syllable  the syllable to be unescaped.
	 * @param tick      the tick when the syllable occurs.
	 * @throws ParseException if a MIDI problem occurs.
	 */
	private void applySyllable(String syllable, long tick) throws ParseException {
		syllable = syllable.replaceAll( Pattern.quote(LYRICS_SPACE), " "  );
		syllable = syllable.replaceAll( Pattern.quote(LYRICS_CR),    "\r" );
		syllable = syllable.replaceAll( Pattern.quote(LYRICS_LF),    "\n" );
		syllable = syllable.replaceAll( Pattern.quote(LYRICS_COMMA), ","  );
		
		try {
			if (isSoftKaraoke)
				SequenceCreator.addMessageText(syllable, tick, 2);
			else
				SequenceCreator.addMessageLyrics(syllable, tick, false);
		}
		catch (InvalidMidiDataException e) {
			throw new ParseException( Dict.get(Dict.ERROR_MIDI_PROBLEM) + e.getMessage() );
		}
	}
	
	/**
	 * Evaluates the given if/elsif condition.
	 * 
	 * @param condition  the condition to be evaluated.
	 * @return **true**, if the condition evaluates to **true**, otherwise: **false**.
	 * @throws ParseException if the condition is invalid
	 */
	public boolean evalCondition(String condition) throws ParseException {
		condition = clean(condition);
		
		String first    = null;
		String second   = null;
		String operator = null;
		
		// get condition parts
		String[] parts = condPattern.split(condition, -1);
		if (parts.length > 2)
			throw new ParseException( Dict.get(Dict.ERROR_TOO_MANY_OPERATORS_IN_COND) + condition );
		else if (parts.length == 1) {
			
			// defined
			Matcher wsMatcher = whitespace.matcher(parts[0]);
			if (wsMatcher.find())
				throw new ParseException( Dict.get(Dict.ERROR_COND_DEFINED_HAS_WHITESPACE) + condition );
			
			if ("".equals(parts[0]))
				return false;
			else
				return true;
		}
		else {
			// one operator, two operands
			first  = clean(parts[0]);
			second = clean(parts[1]);
			Matcher condMatcher = condPattern.matcher(condition);
			if (condMatcher.find())
				operator = condMatcher.group(1);
			else
				throw new ParseException( "invalid operator\nThis should not happen. Please report." );
			
			// check for forbidden whitespaces
			Matcher wsFirstMatcher = whitespace.matcher(first);
			if (wsFirstMatcher.find())
				throw new ParseException( Dict.get(Dict.ERROR_COND_WHITESPACE_IN_FIRST_OP) + first );
			if ( ! COND_IN.equals(operator) ) {
				Matcher wsSecondMatcher = whitespace.matcher(second);
				if (wsSecondMatcher.find())
					throw new ParseException( Dict.get(Dict.ERROR_COND_WHITESPACE_IN_SEC_OP) + second );
			}
			
			// evaluate binary operation
			if (COND_NDEF.equals(operator)) {
				if ( isCondCheckParsRun && "".equals(second) )
					throw new ParseException( Dict.get(Dict.ERROR_COND_UNDEF_EMPTY) + condition );
				if ( ! "".equals(first) )
					throw new ParseException( Dict.get(Dict.ERROR_COND_UNDEF_IN_CENTER) + condition );
				
				if ("".equals(second))
					return true;
				else
					return false;
			}
			if (COND_EQ.equals(operator)) {
				// ==
				if (first.equals(second))
					return true;
				else
					return false;
			}
			else if (COND_NEQ.equals(operator)) {
				// !=
				if (first.equals(second))
					return false;
				else
					return true;
			}
			else if (COND_LT.equals(operator) || COND_LE.equals(operator) || COND_GT.equals(operator) || COND_GE.equals(operator)) {
				int firstInt  = isCondCheckParsRun ? 0 : toInt(first, false);
				int secondInt = isCondCheckParsRun ? 0 : toInt(second, false);
				if (COND_LT.equals(operator))
					return firstInt < secondInt;
				else if (COND_LE.equals(operator))
					return firstInt <= secondInt;
				else if (COND_GT.equals(operator))
					return firstInt > secondInt;
				else
					return firstInt >= secondInt;
			}
			else if (COND_IN.equals(operator)) {
				String[]     candidates    = condInPattern.split(second);
				List<String> candidateList = Arrays.asList(candidates);
				
				// check for forbidden whitespaces
				for (String candidate : candidateList) {
					Matcher wsMatcher = whitespace.matcher(candidate);
					if (wsMatcher.find())
						throw new ParseException( Dict.get(Dict.ERROR_COND_WHITESPACE_IN_IN_ELEM) + candidate );
					else if (isCondCheckParsRun && "".equals(candidate))
						throw new ParseException( Dict.get(Dict.ERROR_COND_EMPTY_ELEM_IN_IN_LIST) + second );
				}
				
				return candidateList.contains(first);
			}
			else
				throw new ParseException( "invalid operator (" + operator + ")\nThis should not happen. Please report." );
		}
	}
	
	/**
	 * Adds the shift option to the given command, if it's a note, chord or call command.
	 * 
	 * @param tokens  command tokens
	 * @return the resulting command tokens.
	 */
	public String[] addShiftToOptions(String[] tokens, int shift) {
		
		String shiftOptionStr = MidicaPLParser.SHIFT + MidicaPLParser.OPT_ASSIGNER + shift;
		String line           = String.join(" ", (String[]) tokens);
		
		// call command?
		if (MidicaPLParser.CALL.equals(tokens[0])) {
			if (tokens.length < 3)
				line += " " + shiftOptionStr;
			else
				line += MidicaPLParser.OPT_SEPARATOR + shiftOptionStr;
			return line.split("\\s+", 3);
		}
		
		// channel command?
		try {
			toChannel(tokens[0]);
			
			// no exception --> channel command
			
			// rest? - ignore
			if ( MidicaPLParser.REST.equals(tokens[1]) ) {
				return tokens;
			}
			
			// note or chord
			if (whitespace.matcher(tokens[2]).find())
				line += MidicaPLParser.OPT_SEPARATOR + shiftOptionStr;
			else
				line += " " + shiftOptionStr;
			return line.split("\\s+", 3);
				
		}
		catch (ParseException e) {
			// not a channel command
		}
		
		return tokens;
	}
	
	/**
	 * Parses an INCLUDE command.
	 * An INCLUDE command parses another MidicaPL source file. Therefore
	 * another parser instance of this class is created.
	 * 
	 * @param tokens             Token array.
	 * @throws ParseException    If the command cannot be parsed.
	 */
	private void parseINCLUDE( String[] tokens ) throws ParseException {
		
		if (2 == tokens.length) {
			try {
				// create File object with absolute path for include file
				String inclPath = tokens[ 1 ];
				File   inclFile = new File(inclPath);
				if (! inclFile.isAbsolute()) {
					File currentDir = file.getParentFile();
					inclFile = new File(
						currentDir.getCanonicalPath(), // parent
						inclPath                       // child
					);
				}

				// make it canonical
				inclFile = inclFile.getCanonicalFile();
				
				// check if the file can be parsed
				if (! inclFile.exists())
					throw new ParseException( Dict.get(Dict.ERROR_FILE_EXISTS) + inclFile.getCanonicalPath() );
				if (! inclFile.isFile())
					throw new ParseException( Dict.get(Dict.ERROR_FILE_NORMAL) + inclFile.getCanonicalPath() );
				if (! inclFile.canRead())
					throw new ParseException( Dict.get(Dict.ERROR_FILE_READABLE) + inclFile.getCanonicalPath() );
				
				// parse it
				MidicaPLParser parser = new MidicaPLParser(false);
				parser.parse(inclFile);
			}
			catch (IOException e) {
				throw new ParseException( Dict.get(Dict.ERROR_FILE_IO) + e.getMessage() );
			}
		}
		else
			throw new ParseException( Dict.get(Dict.ERROR_FILE_NUM_OF_ARGS) );
	}
	
	/**
	 * Parses a SOUNDFONT command.
	 * A SOUNDFONT command includes a soundfont file.
	 * 
	 * @param tokens             Token array.
	 * @throws ParseException    If the soundfont cannot be loaded.
	 */
	private void parseSOUNDFONT(String[] tokens) throws ParseException {
		
		// prevent more than one soundfont include
		if (soundfontParsed) {
			throw new ParseException( Dict.get(Dict.ERROR_SOUNDFONT_ALREADY_PARSED) );
		}
		soundfontParsed = true;
		
		if (2 == tokens.length) {
			try {
				// create File object with absolute path for soundfont file
				String inclPath = tokens[1];
				File   inclFile = new File( inclPath );
				if (! inclFile.isAbsolute()) {
					File currentDir = file.getParentFile();
					inclFile = new File(
						currentDir.getCanonicalPath(), // parent
						inclPath                       // child
					);
				}
				
				// make it canonical
				inclFile = inclFile.getCanonicalFile();
				
				// check if this file is already loaded
				String oldPath = SoundfontParser.getFilePath();
				String newPath = inclFile.getCanonicalPath();
				if (oldPath != null && oldPath.equals(newPath)) {
					return;
				}
				
				// check if the file can be parsed
				if (! inclFile.exists())
					throw new ParseException( Dict.get(Dict.ERROR_FILE_EXISTS) + inclFile.getCanonicalPath() );
				if (! inclFile.isFile())
					throw new ParseException( Dict.get(Dict.ERROR_FILE_NORMAL) + inclFile.getCanonicalPath() );
				if (! inclFile.canRead())
					throw new ParseException( Dict.get(Dict.ERROR_FILE_READABLE) + inclFile.getCanonicalPath() );
				
				// parse it
				SoundfontParser parser = new SoundfontParser();
				parser.parse( inclFile );
				
				// set the file name label in the main window
				Midica.uiController.soundfontLoadedBySourceCode();
			}
			catch (IOException e) {
				throw new ParseException( Dict.get(Dict.ERROR_SOUNDFONT_IO) + e.getMessage() );
			}
		}
		else
			throw new ParseException( Dict.get(Dict.ERROR_SOUNDFONT_NUM_OF_ARGS) );
	}
	
	/**
	 * Parses a DEFINE command.
	 * A DEFINE command is used to re-define a mode command name.
	 * 
	 * @param tokens             Token array.
	 * @throws ParseException    If the command cannot be parsed.
	 */
	private void parseDEFINE(String[] tokens) throws ParseException {
		String def;
		if (2 == tokens.length) {
			// e.g. DEFINE DEFINE=def
			def = tokens[ 1 ];
		}
		else if (3 == tokens.length) {
			// e.g. DEFINE DEFINE = def
			// or   DEFINE DEFINE def
			def = tokens[ 1 ] + " " + tokens[ 2 ];
		}
		else {
			throw new ParseException( Dict.get(Dict.ERROR_DEFINE_NUM_OF_ARGS) );
		}
		
		// Further checks would fail in the default run, if the COMMENT command has
		// been redefined.
		if (isDefaultParsRun) {
			return;
		}
		
		// split definition string by OPT_ASSIGNER (e,g, "=") and/or whitespace(s)
		String[] defParts = def.split( "[" + Pattern.quote(OPT_ASSIGNER) + "\\s]+", 2 );
		if (defParts.length < 2)
			throw new ParseException( Dict.get(Dict.ERROR_DEFINE_NUM_OF_ARGS) );
		String cmdId      = clean( defParts[0] );
		String cmdName    = clean( defParts[1] );
		if ( ! cmdName.matches("^\\S+$") )
			throw new ParseException( Dict.get(Dict.ERROR_DEFINE_NUM_OF_ARGS) );
		
		// only one redefinition allowed per command
		if (redefinitions.contains(cmdId)) {
			throw new ParseException( Dict.get(Dict.ERROR_ALREADY_REDEFINED) + cmdId );
		}
		redefinitions.add(cmdId);
		
		if      ( Dict.SYNTAX_BANK_SEP.equals(cmdId)           ) BANK_SEP           = cmdName;
		else if ( Dict.SYNTAX_TEMPO.equals(cmdId)              ) TEMPO              = cmdName;
		else if ( Dict.SYNTAX_TIME_SIG.equals(cmdId)           ) TIME_SIG           = cmdName;
		else if ( Dict.SYNTAX_TIME_SIG_SLASH.equals(cmdId)     ) TIME_SIG_SLASH     = cmdName;
		else if ( Dict.SYNTAX_KEY_SIG.equals(cmdId)            ) KEY_SIG            = cmdName;
		else if ( Dict.SYNTAX_KEY_SEPARATOR.equals(cmdId)      ) KEY_SEPARATOR      = cmdName;
		else if ( Dict.SYNTAX_KEY_MAJ.equals(cmdId)            ) KEY_MAJ            = cmdName;
		else if ( Dict.SYNTAX_KEY_MIN.equals(cmdId)            ) KEY_MIN            = cmdName;
		else if ( Dict.SYNTAX_PARTIAL_SYNC_RANGE.equals(cmdId) ) PARTIAL_SYNC_RANGE = cmdName;
		else if ( Dict.SYNTAX_PARTIAL_SYNC_SEP.equals(cmdId)   ) PARTIAL_SYNC_SEP   = cmdName;
		else if ( Dict.SYNTAX_CHORD.equals(cmdId)              ) CHORD              = cmdName;
		else if ( Dict.SYNTAX_CHORD_SEPARATOR.equals(cmdId)    ) CHORD_SEPARATOR    = cmdName;
		else if ( Dict.SYNTAX_CHORD_ASSIGNER.equals(cmdId)     ) CHORD_ASSIGNER     = cmdName;
		else if ( Dict.SYNTAX_COMMENT.equals(cmdId)            ) COMMENT            = cmdName;
		else if ( Dict.SYNTAX_CONST.equals(cmdId)              ) CONST              = cmdName;
		else if ( Dict.SYNTAX_VAR.equals(cmdId)                ) VAR                = cmdName;
		else if ( Dict.SYNTAX_VAR_ASSIGNER.equals(cmdId)       ) VAR_ASSIGNER       = cmdName;
		else if ( Dict.SYNTAX_VAR_SYMBOL.equals(cmdId)         ) VAR_SYMBOL         = cmdName;
		else if ( Dict.SYNTAX_DEFINE.equals(cmdId)             ) DEFINE             = cmdName;
		else if ( Dict.SYNTAX_END.equals(cmdId)                ) END                = cmdName;
		else if ( Dict.SYNTAX_BLOCK_OPEN.equals(cmdId)         ) BLOCK_OPEN         = cmdName;
		else if ( Dict.SYNTAX_BLOCK_CLOSE.equals(cmdId)        ) BLOCK_CLOSE        = cmdName;
		else if ( Dict.SYNTAX_GLOBAL.equals(cmdId)             ) GLOBAL             = cmdName;
		else if ( Dict.SYNTAX_DOT.equals(cmdId)                ) DOT                = cmdName;
		else if ( Dict.SYNTAX_CALL.equals(cmdId)               ) CALL               = cmdName;
		else if ( Dict.SYNTAX_INCLUDE.equals(cmdId)            ) INCLUDE            = cmdName;
		else if ( Dict.SYNTAX_SOUNDFONT.equals(cmdId)          ) SOUNDFONT          = cmdName;
		else if ( Dict.SYNTAX_INSTRUMENT.equals(cmdId)         ) INSTRUMENT         = cmdName;
		else if ( Dict.SYNTAX_INSTRUMENTS.equals(cmdId)        ) INSTRUMENTS        = cmdName;
		else if ( Dict.SYNTAX_META.equals(cmdId)               ) META               = cmdName;
		else if ( Dict.SYNTAX_META_COPYRIGHT.equals(cmdId)     ) META_COPYRIGHT     = cmdName;
		else if ( Dict.SYNTAX_META_TITLE.equals(cmdId)         ) META_TITLE         = cmdName;
		else if ( Dict.SYNTAX_META_COMPOSER.equals(cmdId)      ) META_COMPOSER      = cmdName;
		else if ( Dict.SYNTAX_META_LYRICIST.equals(cmdId)      ) META_LYRICIST      = cmdName;
		else if ( Dict.SYNTAX_META_ARTIST.equals(cmdId)        ) META_ARTIST        = cmdName;
		else if ( Dict.SYNTAX_META_SOFT_KARAOKE.equals(cmdId)  ) META_SOFT_KARAOKE  = cmdName;
		else if ( Dict.SYNTAX_META_SK_VERSION.equals(cmdId)    ) META_SK_VERSION    = cmdName;
		else if ( Dict.SYNTAX_META_SK_LANG.equals(cmdId)       ) META_SK_LANG       = cmdName;
		else if ( Dict.SYNTAX_META_SK_TITLE.equals(cmdId)      ) META_SK_TITLE      = cmdName;
		else if ( Dict.SYNTAX_META_SK_AUTHOR.equals(cmdId)     ) META_SK_AUTHOR     = cmdName;
		else if ( Dict.SYNTAX_META_SK_COPYRIGHT.equals(cmdId)  ) META_SK_COPYRIGHT  = cmdName;
		else if ( Dict.SYNTAX_META_SK_INFO.equals(cmdId)       ) META_SK_INFO       = cmdName;
		else if ( Dict.SYNTAX_32.equals(cmdId)                 ) LENGTH_32          = cmdName;
		else if ( Dict.SYNTAX_16.equals(cmdId)                 ) LENGTH_16          = cmdName;
		else if ( Dict.SYNTAX_8.equals(cmdId)                  ) LENGTH_8           = cmdName;
		else if ( Dict.SYNTAX_4.equals(cmdId)                  ) LENGTH_4           = cmdName;
		else if ( Dict.SYNTAX_2.equals(cmdId)                  ) LENGTH_2           = cmdName;
		else if ( Dict.SYNTAX_1.equals(cmdId)                  ) LENGTH_1           = cmdName;
		else if ( Dict.SYNTAX_M1.equals(cmdId)                 ) LENGTH_M1          = cmdName;
		else if ( Dict.SYNTAX_M2.equals(cmdId)                 ) LENGTH_M2          = cmdName;
		else if ( Dict.SYNTAX_M4.equals(cmdId)                 ) LENGTH_M4          = cmdName;
		else if ( Dict.SYNTAX_M8.equals(cmdId)                 ) LENGTH_M8          = cmdName;
		else if ( Dict.SYNTAX_M16.equals(cmdId)                ) LENGTH_M16         = cmdName;
		else if ( Dict.SYNTAX_M32.equals(cmdId)                ) LENGTH_M32         = cmdName;
		else if ( Dict.SYNTAX_L.equals(cmdId)                  ) L                  = cmdName;
		else if ( Dict.SYNTAX_LYRICS.equals(cmdId)             ) LYRICS             = cmdName;
		else if ( Dict.SYNTAX_LYRICS_SPACE.equals(cmdId)       ) LYRICS_SPACE       = cmdName;
		else if ( Dict.SYNTAX_LYRICS_CR.equals(cmdId)          ) LYRICS_CR          = cmdName;
		else if ( Dict.SYNTAX_LYRICS_LF.equals(cmdId)          ) LYRICS_LF          = cmdName;
		else if ( Dict.SYNTAX_LYRICS_COMMA.equals(cmdId)       ) LYRICS_COMMA       = cmdName;
		else if ( Dict.SYNTAX_FUNCTION.equals(cmdId)           ) FUNCTION           = cmdName;
		else if ( Dict.SYNTAX_PATTERN.equals(cmdId)            ) PATTERN            = cmdName;
		else if ( Dict.SYNTAX_PATTERN_INDEX_SEP.equals(cmdId)  ) PATTERN_INDEX_SEP  = cmdName;
		else if ( Dict.SYNTAX_PARAM_OPEN.equals(cmdId)         ) PARAM_OPEN         = cmdName;
		else if ( Dict.SYNTAX_PARAM_CLOSE.equals(cmdId)        ) PARAM_CLOSE        = cmdName;
		else if ( Dict.SYNTAX_PARAM_SEPARATOR.equals(cmdId)    ) PARAM_SEPARATOR    = cmdName;
		else if ( Dict.SYNTAX_PARAM_ASSIGNER.equals(cmdId)     ) PARAM_ASSIGNER     = cmdName;
		else if ( Dict.SYNTAX_PARAM_NAMED_OPEN.equals(cmdId)   ) PARAM_NAMED_OPEN   = cmdName;
		else if ( Dict.SYNTAX_PARAM_NAMED_CLOSE.equals(cmdId)  ) PARAM_NAMED_CLOSE  = cmdName;
		else if ( Dict.SYNTAX_PARAM_INDEX_OPEN.equals(cmdId)   ) PARAM_INDEX_OPEN   = cmdName;
		else if ( Dict.SYNTAX_PARAM_INDEX_CLOSE.equals(cmdId)  ) PARAM_INDEX_CLOSE  = cmdName;
		else if ( Dict.SYNTAX_M.equals(cmdId)                  ) M                  = cmdName;
		else if ( Dict.SYNTAX_MULTIPLE.equals(cmdId)           ) MULTIPLE           = cmdName;
		else if ( Dict.SYNTAX_OPT_ASSIGNER.equals(cmdId)       ) OPT_ASSIGNER       = cmdName;
		else if ( Dict.SYNTAX_OPT_SEPARATOR.equals(cmdId)      ) OPT_SEPARATOR      = cmdName;
		else if ( Dict.SYNTAX_P.equals(cmdId)                  ) P                  = cmdName;
		else if ( Dict.SYNTAX_REST.equals(cmdId)               ) REST               = cmdName;
		else if ( Dict.SYNTAX_PROG_BANK_SEP.equals(cmdId)      ) PROG_BANK_SEP      = cmdName;
		else if ( Dict.SYNTAX_Q.equals(cmdId)                  ) Q                  = cmdName;
		else if ( Dict.SYNTAX_QUANTITY.equals(cmdId)           ) QUANTITY           = cmdName;
		else if ( Dict.SYNTAX_D.equals(cmdId)                  ) D                  = cmdName;
		else if ( Dict.SYNTAX_DURATION.equals(cmdId)           ) DURATION           = cmdName;
		else if ( Dict.SYNTAX_DURATION_PERCENT.equals(cmdId)   ) DURATION_PERCENT   = cmdName;
		else if ( Dict.SYNTAX_TR.equals(cmdId)                 ) TR                 = cmdName;
		else if ( Dict.SYNTAX_TREMOLO.equals(cmdId)            ) TREMOLO            = cmdName;
		else if ( Dict.SYNTAX_T.equals(cmdId)                  ) T                  = cmdName;
		else if ( Dict.SYNTAX_SHIFT.equals(cmdId)              ) SHIFT              = cmdName;
		else if ( Dict.SYNTAX_S.equals(cmdId)                  ) S                  = cmdName;
		else if ( Dict.SYNTAX_TUPLET.equals(cmdId)             ) TUPLET             = cmdName;
		else if ( Dict.SYNTAX_V.equals(cmdId)                  ) V                  = cmdName;
		else if ( Dict.SYNTAX_VELOCITY.equals(cmdId)           ) VELOCITY           = cmdName;
		else if ( Dict.SYNTAX_TRIPLET.equals(cmdId)            ) TRIPLET            = cmdName;
		else if ( Dict.SYNTAX_TUPLET_INTRO.equals(cmdId)       ) TUPLET_INTRO       = cmdName;
		else if ( Dict.SYNTAX_TUPLET_FOR.equals(cmdId)         ) TUPLET_FOR         = cmdName;
		else if ( Dict.SYNTAX_LENGTH_PLUS.equals(cmdId)        ) LENGTH_PLUS        = cmdName;
		else if ( Dict.SYNTAX_IF.equals(cmdId)                 ) IF                 = cmdName;
		else if ( Dict.SYNTAX_ELSIF.equals(cmdId)              ) ELSIF              = cmdName;
		else if ( Dict.SYNTAX_ELSE.equals(cmdId)               ) ELSE               = cmdName;
		else if ( Dict.SYNTAX_COND_EQ.equals(cmdId)            ) COND_EQ            = cmdName;
		else if ( Dict.SYNTAX_COND_NEQ.equals(cmdId)           ) COND_NEQ           = cmdName;
		else if ( Dict.SYNTAX_COND_NDEF.equals(cmdId)          ) COND_NDEF          = cmdName;
		else if ( Dict.SYNTAX_COND_LT.equals(cmdId)            ) COND_LT            = cmdName;
		else if ( Dict.SYNTAX_COND_LE.equals(cmdId)            ) COND_LE            = cmdName;
		else if ( Dict.SYNTAX_COND_GT.equals(cmdId)            ) COND_GT            = cmdName;
		else if ( Dict.SYNTAX_COND_GE.equals(cmdId)            ) COND_GE            = cmdName;
		else if ( Dict.SYNTAX_COND_IN.equals(cmdId)            ) COND_IN            = cmdName;
		else if ( Dict.SYNTAX_COND_IN_SEP.equals(cmdId)        ) COND_IN_SEP        = cmdName;
		else {
			throw new ParseException( Dict.get(Dict.ERROR_UNKNOWN_COMMAND_ID) + cmdId );
		}
	}
	
	/**
	 * Parses a CONST command.
	 * A CONST command is used to define a constant.
	 * 
	 * @param tokens             Token array.
	 * @throws ParseException    If the command cannot be parsed.
	 */
	private void parseCONST(String[] tokens) throws ParseException {
		
		// CONST without name?
		if (tokens.length < 2) {
			throw new ParseException( Dict.get(Dict.ERROR_CONST_NUM_OF_ARGS) );
		}
		
		// CONST with name but without value?
		String[] assignParts = tokens[ 1 ].split("[" + Pattern.quote(VAR_ASSIGNER) + "\\s]+", 2); // const name and value can be separated by "=" and/or whitespace(s)
		if (assignParts.length < 2) {
			throw new ParseException( Dict.get(Dict.ERROR_CONST_NUM_OF_ARGS) );
		}
		
		// constant name already defined?
		String name  = assignParts[ 0 ];
		String value = assignParts[ 1 ];
		
		// recursion not allowed
		if (name.equals(value)) {
			throw new ParseException( Dict.get(Dict.ERROR_CONST_NAME_EQ_VALUE) + name );
		}
		
		// the value could contain other constants as well
		value = replaceConstants(value);
		
		if (constants.containsKey(name)) {
			throw new ParseException( Dict.get(Dict.ERROR_CONST_ALREADY_DEFINED) + name );
		}
		
		// store it
		constants.put(name, value);
	}
	
	/**
	 * Parses a VAR command.
	 * A VAR command is used to define and/or (re)assign a variable.
	 * 
	 * @param tokens             Token array.
	 * @param isFake             **true**, if this is called inside a function definition or block
	 * @throws ParseException    If the command cannot be parsed.
	 */
	private void parseVAR(String[] tokens, boolean isFake) throws ParseException {
		
		// VAR without name?
		if (tokens.length < 2) {
			throw new ParseException( Dict.get(Dict.ERROR_VAR_NUM_OF_ARGS) );
		}
		
		// VAR with name but without value?
		String[] assignParts = tokens[ 1 ].split("[" + Pattern.quote(VAR_ASSIGNER) + "\\s]+", 2); // var name and value can be separated by "=" and/or whitespace(s)
		if (assignParts.length < 2) {
			throw new ParseException( Dict.get(Dict.ERROR_VAR_NUM_OF_ARGS) );
		}
		
		// name already defined as a constant?
		String name  = assignParts[ 0 ];
		String value = assignParts[ 1 ];
		if (constants.containsKey(name)) {
			throw new ParseException( Dict.get(Dict.ERROR_VAR_ALREADY_DEF_AS_CONST) + name );
		}
		
		// recursion not allowed
		if (name.equals(value)) {
			throw new ParseException( Dict.get(Dict.ERROR_VAR_NAME_EQ_VALUE) + name );
		}
		
		// no whitespace in variable values allowed!
		Matcher matcher = whitespace.matcher(value);
		if (matcher.find()) {
			throw new ParseException( Dict.get(Dict.ERROR_VAR_VAL_HAS_WHITESPACE) + value );
		}
		
		if (isFake)
			return;
		
		// variable or param?
		boolean isNamedParam   = false;
		boolean isIndexedParam = false;
		Matcher varMatcher     = varPattern.matcher(name);
		if (varMatcher.matches()) {
			isIndexedParam = varMatcher.group(5) != null;
			isNamedParam   = varMatcher.group(6) != null;
		}
		else {
			throw new ParseException( Dict.get(Dict.ERROR_VAR_NAME_INVALID) + name );
		}
		
		// the value could contain other variables as well
		value = replaceVariables(value);
		
		// store it
		if (isNamedParam) {
			// named parameter
			String key = varMatcher.group(6);
			HashMap<String, String> params = paramStackNamed.peek();
			if (null == params) {
				throw new ParseException( Dict.get(Dict.ERROR_PARAM_OUTSIDE_FUNCTION) + name );
			}
			if ( ! params.containsKey(key) ) {
				throw new ParseException( Dict.get(Dict.ERROR_PARAM_NAMED_UNKNOWN) + name );
			}
			params.put(key, value);
		}
		else if (isIndexedParam) {
			// indexed parameter
			int index = toInt( varMatcher.group(5) );
			ArrayList<String> params = paramStackIndexed.peek();
			if (null == params) {
				throw new ParseException( Dict.get(Dict.ERROR_PARAM_OUTSIDE_FUNCTION) + name );
			}
			if (index >= params.size()) {
				throw new ParseException( Dict.get(Dict.ERROR_PARAM_INDEX_TOO_HIGH) + name );
			}
			if (null == params.get(index)) {
				throw new ParseException( Dict.get(Dict.ERROR_PARAM_INDEX_UNDEFINED) + name );
			}
			params.set(index, value);
		}
		else {
			// normal variable
			variables.put(name, value);
		}
	}
	
	/**
	 * Replaces all constants in the given string by their values.
	 * 
	 * @param str  The string to be replaced.
	 * @return     The resulting string.
	 * @throws ParseException if the recursion depth is too high
	 */
	private String replaceConstants(String str) throws ParseException {
		
		// no constant/variable found?
		if ( ! varPattern.matcher(str).find() ) {
			return str;
		}
		
		int     recursionCount  = 0;
		boolean mustSearchAgain = true;
		while (mustSearchAgain) {
			recursionCount++;
			mustSearchAgain = false;
			Matcher constMatcher = varPattern.matcher(str);
			StringBuffer resultingLine = new StringBuffer();
			
			// find and replace the next constant
			while (constMatcher.find()) {
				String constName  = constMatcher.group( 1 );
				String constValue = constants.get(constName);
				
				// constant (not a variable)? - replace it
				if (constValue != null) {
					mustSearchAgain = true;
					constMatcher.appendReplacement(resultingLine, Matcher.quoteReplacement(constValue));
				}
			}
			
			// replace, if necessary
			if (mustSearchAgain) {
				constMatcher.appendTail(resultingLine);
				str = resultingLine.toString();
			}
			
			// recursion depth too high?
			if (recursionCount > MAX_RECURSION_DEPTH_CONST) {
				throw new ParseException( Dict.get(Dict.ERROR_CONST_RECURSION) );
			}
		}
		
		return str;
	}
	
	/**
	 * Replaces all variables (or parameters) in the given string by their values.
	 * 
	 * @param str  The string to be replaced.
	 * @return     The resulting string.
	 * @throws ParseException    if an undefined variable is found or the recursion depth is too high.
	 */
	public String replaceVariables(String str) throws ParseException {
		
		// no variable found?
		if ( ! varPattern.matcher(str).find() ) {
			return str;
		}
		
		int     recursionCount  = 0;
		boolean mustSearchAgain = true;
		while (mustSearchAgain) {
			recursionCount++;
			mustSearchAgain = false;
			Matcher varMatcher = varPattern.matcher(str);
			StringBuffer resultingLine = new StringBuffer();
			
			// find and replace the next variable
			while (varMatcher.find()) {
				mustSearchAgain        = true;
				String varName         = varMatcher.group(1);
				boolean isIndexedParam = varMatcher.group(5) != null;
				boolean isNamedParam   = varMatcher.group(6) != null;
				
				String varValue = null;
				try {
					if (isIndexedParam) {
						int index = toInt( varMatcher.group(5) );
						ArrayList<String> params = paramStackIndexed.peek();
						if (null == params) {
							throw new ParseException( Dict.get(Dict.ERROR_PARAM_OUTSIDE_FUNCTION) + varName );
						}
						if (index < params.size()) {
							varValue = params.get(index);
						}
					}
					else if (isNamedParam) {
						String name = varMatcher.group(6);
						HashMap<String, String> params = paramStackNamed.peek();
						if (null == params) {
							throw new ParseException( Dict.get(Dict.ERROR_PARAM_OUTSIDE_FUNCTION) + varName );
						}
						varValue = params.get(name);
					}
					else {
						varValue = variables.get(varName);
					}
				}
				catch (ParseException e) {
					e.setCausedByInvalidVar(varName);
					throw e;
				}
				
				// variable undefined?
				if (null == varValue) {
					if (isIndexedParam || isNamedParam)
						varValue = ""; // allow undefined parameters
					else
						throw new ParseException( Dict.get(Dict.ERROR_VAR_NOT_DEFINED) + varName );
				}
				
				// replace
				varMatcher.appendReplacement(resultingLine, Matcher.quoteReplacement(varValue));
			}
			
			// replace, if necessary
			if (mustSearchAgain) {
				varMatcher.appendTail(resultingLine);
				str = resultingLine.toString();
			}
			
			// recursion depth too high?
			if (recursionCount > MAX_RECURSION_DEPTH_VAR) {
				throw new ParseException( Dict.get(Dict.ERROR_VAR_RECURSION) );
			}
		}
		
		return str;
	}
	
	/**
	 * Parses a single line instrument switch command (outside of an instruments bock).
	 * 
	 * @param tokens             Token array.
	 * @param isFake             **true**, if this is called inside a function definition or block
	 * @throws ParseException    If the command cannot be parsed.
	 */
	private void parseSingleLineInstrumentSwitch(String[] tokens, boolean isFake) throws ParseException {
		
		// not enough arguments?
		if (tokens.length < 3) {
			throw new ParseException( Dict.get(Dict.ERROR_INSTR_NUM_OF_ARGS_SINGLE) );
		}
		
		// add channel to new command
		StringBuilder command = new StringBuilder( tokens[1] + " " );
		int channel = toChannel(tokens[1]);
		
		// add instrument number
		String[] instrAndDesc = tokens[2].split("\\s", 2);
		command.append(instrAndDesc[0] + " ");
		
		// add description
		if (1 == instrAndDesc.length) {
			String description = instruments.get(channel).instrumentName;
			command.append(description);
		}
		else {
			command.append(instrAndDesc[1]);
		}
		
		// parse the resulting command
		String[] newTokens = command.toString().split("\\s+", 3);
		parseInstrumentCmd(newTokens, isFake);
	}
	
	/**
	 * Parses an instrument command inside an INSTRUMENTS block or a single line INSTRUMENT change.
	 * This command assigns a certain instrument to a certain channel.
	 * A bank number can also be assigned for the channel.
	 * 
	 * TODO: test soundfonts with bankLSB > 0
	 * 
	 * @param tokens             Token array.
	 * @param isFake             **true**, if this is called inside a function definition or block
	 * @throws ParseException    If the command cannot be parsed.
	 */
	private void parseInstrumentCmd(String[] tokens, boolean isFake) throws ParseException {
		if (3 != tokens.length) {
			throw new ParseException( Dict.get(Dict.ERROR_INSTR_NUM_OF_ARGS) );
		}
		
		int    channel   = toChannel( tokens[0] );
		String instrStr  = tokens[ 1 ];
		String instrName = tokens[ 2 ];
		int    bankMSB   = 0;
		int    bankLSB   = 0;
		
		// program number and banks
		String [] instrBank = instrStr.split( PROG_BANK_SEP, 2 );
		int       instrNum  = replaceInstrument( instrBank[0], channel );
		if (1 == instrBank.length) {
			// only program number, no bank defined - nothing more to do
		}
		else if (2 == instrBank.length) {
			// program number and bank are both defined
			
			// bank MSB / LSB / full number
			String[] msbLsb = instrBank[ 1 ].split( BANK_SEP, 2 );
			bankMSB         = toInt( msbLsb[0] );
			if (1 == msbLsb.length) {
				if (bankMSB > 127 * 127) {
					// too big
					throw new ParseException( Dict.get(Dict.ERROR_INSTR_BANK) );
				}
				else if (bankMSB > 127) {
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
		if (bankMSB > 127 || bankLSB > 127) {
			throw new ParseException( Dict.get(Dict.ERROR_INSTR_BANK) );
		}
		
		if (instrumentsParsed) {
			// instruments are already parsed
			// this is an instrument change command for the channel
			Instrument instr       = instruments.get( channel );
			instr.autoChannel      = false;
			instr.instrumentNumber = instrNum;
			instr.instrumentName   = instrName;
			long tick              = instr.getCurrentTicks();
			
			boolean[] isChanged = instr.setBank( bankMSB, bankLSB );
			
			if (isFake)
				return;
			
			try {
				// calculate tick for bank select
				long bankTick = tick - TICK_BANK_BEFORE_PROGRAM;
				if (bankTick < 0)
					bankTick = 0;
				
				// bank select, if necessary
				if (isChanged[0])
					SequenceCreator.setBank( channel, bankTick, bankMSB, false );
				if (isChanged[1])
					SequenceCreator.setBank( channel, bankTick, bankLSB, true );
				
				// program change and instrument name
				SequenceCreator.initChannel( channel, instrNum, instrName, tick );
			}
			catch (InvalidMidiDataException e) {
				throw new ParseException( Dict.get(Dict.ERROR_MIDI_PROBLEM) + e.getMessage() );
			}
		}
		else {
			// first instruments block
			
			// check if the channel has already been defined
			for (Instrument instr : instruments) {
				if (channel == instr.channel) {
					throw new ParseException(
						String.format( Dict.get(Dict.ERROR_CHANNEL_REDEFINED), channel )
					);
				}
			}
			
			if (isFake)
				return;
			
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
	 * Parses one command inside of a META block.
	 * 
	 * @param tokens             Token array.
	 * @throws ParseException    If the command cannot be parsed.
	 */
	private void parseMetaCmd(String[] tokens) throws ParseException {
		String key;
		String crlf = "\\r\\n"; // according to RP-026
		if (META_COPYRIGHT.equals(tokens[0])) {
			key  = "copyright";
			crlf = "\r\n"; // not RP-026
		}
		else if (META_TITLE.equals(tokens[0]))
			key = "title";
		else if (META_COMPOSER.equals(tokens[0]))
			key = "composer";
		else if (META_LYRICIST.equals(tokens[0]))
			key = "lyrics";
		else if (META_ARTIST.equals(tokens[0]))
			key = "artist";
		else if (META_SOFT_KARAOKE.equals(tokens[0])) {
			currentMode = MODE_SOFT_KARAOKE;
			return;
		}
		else
			throw new ParseException( Dict.get(Dict.ERROR_META_UNKNOWN_CMD) + tokens[0] );
		
		// prepare the new meta line
		String line = tokens[1];
		if ("copyright".equals(key)) {
			// nothing more to do
		}
		else {
			// replace characters according to RP-026
			line = lyricUtil.escape(line);
		}
		
		// create or append to the meta message
		StringBuilder val = metaInfo.get(key);
		if (null == val) {
			val = new StringBuilder();
			metaInfo.put(key, val);
		}
		else {
			val.append(crlf);
		}
		val.append(line);
	}
	
	/**
	 * Parses one command inside of a SOFT_KARAOKE block.
	 * 
	 * @param tokens             Token array.
	 * @throws ParseException    If the command cannot be parsed.
	 */
	private void parseSoftKaraokeCmd(String[] tokens) throws ParseException {
		String key;
		if (META_SK_VERSION.equals(tokens[0]))
			key = "sk_version";
		else if (META_SK_LANG.equals(tokens[0]))
			key = "sk_lang";
		else if (META_SK_TITLE.equals(tokens[0]))
			key = "sk_title";
		else if (META_SK_AUTHOR.equals(tokens[0]))
			key = "sk_author";
		else if (META_SK_COPYRIGHT.equals(tokens[0]))
			key = "sk_copyright";
		else if (META_SK_INFO.equals(tokens[0]))
			key = "sk_info";
		else
			throw new ParseException( Dict.get(Dict.ERROR_SOFT_KARAOKE_UNKNOWN_CMD) + tokens[0] );
		
		// create a new value, if necessary
		ArrayList<String> values = softKaraokeInfo.get(key);
		if (null == values) {
			values = new ArrayList<>();
		}
		else {
			// value exists already
			
			// no limit for info events
			// other values may exist only once
			if ( ! META_SK_INFO.equals(tokens[0]) ) {
				throw new ParseException( Dict.get(Dict.ERROR_SK_VALUE_ALREADY_SET) + tokens[0] );
			}
		}
		
		// don't allow line breaks (\r or \n)
		if (crlfSkPattern.matcher(tokens[1]).find()) {
			throw new ParseException( Dict.get(Dict.ERROR_SK_FIELD_CRLF_NOT_ALLOWED) );
		}
		
		// add content
		values.add(tokens[1]);
		softKaraokeInfo.put(key, values);
	}
	
	/**
	 * Parses a global command.
	 * Global commands apply to every channel. So they always contain a syncronization
	 * of the current ticks of each channel.
	 * 
	 * @param tokens             Token array.
	 * @param isFake             **true**, if this is called inside a function definition or block
	 * @throws ParseException    If the command cannot be parsed.
	 */
	private void parseGlobalCmd(String[] tokens, boolean isFake) throws ParseException {
		
		// allow global commands in drum-only sequences without an INSTRUMENTS block
		if (! instrumentsParsed) {
			postprocessInstruments();
		}
		
		String channelDesc = "0" + PARTIAL_SYNC_RANGE + "15";
		int    tokenCount  = tokens.length;
		
		if (tokenCount > 3)
			throw new ParseException( Dict.get(Dict.ERROR_GLOBAL_NUM_OF_ARGS) );
		if (2 == tokenCount)
			channelDesc = tokens[1]; // partial sync
		
		long currentTicks = 0;
		if (! isFake) {
			synchronize(channelDesc);
			currentTicks = instruments.get( 0 ).getCurrentTicks();
		}
		if (tokenCount < 3)
			return;
		
		String cmd   = tokens[1];
		String value = tokens[2];
		
		try {
			// set tempo
			if (cmd.equals(TEMPO)) {
				int bpm = toInt( value, true );
				if (! isFake) {
					SequenceCreator.addMessageTempo(bpm, currentTicks);
				}
			}
			
			// set time signature
			else if (cmd.equals(TIME_SIG)) {
				Pattern pattern = Pattern.compile("^(\\d+)" + Pattern.quote(TIME_SIG_SLASH) + "(\\d+)$");
				Matcher matcher = pattern.matcher(value);
				if (matcher.matches()) {
					int numerator   = toInt(matcher.group(1));
					int denominator = toInt(matcher.group(2));
					
					// set the time signature message
					if (! isFake)
						SequenceCreator.addMessageTimeSignature(numerator, denominator, currentTicks);
				}
				else {
					throw new ParseException( Dict.get(Dict.ERROR_INVALID_TIME_SIG) + value);
				}
			}
			
			// set key signature
			else if (cmd.equals(KEY_SIG)) {
				Pattern pattern = Pattern.compile("^(\\S+)" + Pattern.quote(KEY_SEPARATOR) + "(\\S+)$");
				Matcher matcher = pattern.matcher(value);
				if (matcher.matches()) {
					String noteName = matcher.group(1);
					String tonality = matcher.group(2);
					
					// check and process note and tonality
					int     note = parseNote(noteName);
					boolean isMajor;
					if (tonality.equals(KEY_MAJ)) {
						isMajor = true;
					}
					else if (tonality.equals(KEY_MIN)) {
						isMajor = false;
					}
					else {
						throw new ParseException( Dict.get(Dict.ERROR_INVALID_TONALITY) + tonality );
					}
					// set the key signature message
					if (! isFake)
						SequenceCreator.addMessageKeySignature(note, isMajor, currentTicks);
				}
				else {
					throw new ParseException( Dict.get(Dict.ERROR_INVALID_KEY_SIG) + value);
				}
			}
			
			else {
				throw new ParseException( Dict.get(Dict.ERROR_UNKNOWN_GLOBAL_CMD) + cmd );
			}
		}
		catch (InvalidMidiDataException e) {
			throw new ParseException( Dict.get(Dict.ERROR_MIDI_PROBLEM) + e.getMessage() );
		}
	}
	
	/**
	 * Parses a channel command.
	 * A channel command is a channel-based command like a note or a rest.
	 * 
	 * @param tokens             Token array.
	 * @param isFake             **true**, if this is called inside a function definition or block
	 * @throws ParseException    If the command cannot be parsed.
	 */
	private void parseChannelCmd(String[] tokens, boolean isFake) throws ParseException {
		
		int tokenCount = tokens.length;
		if (tokenCount < 3)
			throw new ParseException( Dict.get(Dict.ERROR_CH_CMD_NUM_OF_ARGS) );
		
		int channel = toChannel(tokens[0]);
		int note    = parseNote(tokens[1], channel);
		
		// separate the duration from further arguments
		String[] subTokens = tokens[2].split( "\\s+", 2 );
		if (0 == subTokens.length)
			throw new ParseException( Dict.get(Dict.ERROR_CH_CMD_NUM_OF_ARGS) );
		
		// process duration
		String durationStr = subTokens[0];
		int duration;
		if (isFake) {
			if (varPattern.matcher(durationStr).find()) {
				duration = 0; // variable: parse only in default mode
			}
			else if (patterns.containsKey(durationStr)) {
				duration = 0;
			}
			else {
				duration = parseDuration(durationStr);
			}
		}
		else if (patterns.containsKey(durationStr)) {
			return;
		}
		else {
			duration = parseDuration(durationStr);
		}
		
		// allow drum-only sequences without an INSTRUMENTS block
		if (! instrumentsParsed) {
			postprocessInstruments();
		}
		
		// process options
		boolean multiple = false;
		int     tremolo  = duration;
		int     quantity = 1;
		int     shift    = 0;
		String  syllable = null;
		if (2 == subTokens.length) {
			ArrayList<CommandOption> options = parseOptions(subTokens[1], isFake);
			
			for (CommandOption opt : options) {
				String optName = opt.getName();
				
				if (OPT_VELOCITY.equals(optName)) {
					int velocity = opt.getVelocity();
					if (! isFake)
						instruments.get( channel ).setVelocity( velocity );
				}
				else if (OPT_DURATION.equals(optName)) {
					float durationRatio = opt.getDuration();
					if (! isFake)
						instruments.get(channel).setDurationRatio(durationRatio);
				}
				else if (OPT_MULTIPLE.equals(optName)) {
					multiple = true;
				}
				else if (OPT_QUANTITY.equals(optName)) {
					quantity = opt.getQuantity();
				}
				else if (OPT_LYRICS.equals(optName)) {
					syllable = opt.getLyrics();
					if (isSoftKaraoke && crlfSkPattern.matcher(syllable).find()) {
						throw new ParseException( Dict.get(Dict.ERROR_SK_SYLLABLE_CRLF_NOT_ALLOWED) );
					}
				}
				else if (OPT_TREMOLO.equals(optName)) {
					tremolo = opt.getTremolo();
				}
				else if (OPT_SHIFT.equals(optName)) {
					shift += opt.getShift();
				}
				else
					throw new ParseException( Dict.get(Dict.ERROR_CHANNEL_INVALID_OPT) + optName );
			}
		}
		
		// transpose by source code
		if (note != REST_VALUE)
			note += shift;
		
		// get instrument
		Instrument instr = instruments.get( channel );
		
		if (instr.autoChannel)
			throw new ParseException(
				String.format( Dict.get(Dict.ERROR_CHANNEL_UNDEFINED), channel )
			);
		
		// get start ticks of the first note and velocity
		long absoluteStartTicks = instr.getCurrentTicks();
		int  velocity           = instr.getVelocity();
		
		try {
			if (! isFake && syllable != null) {
				applySyllable(syllable, instr.getCurrentTicks());
			}
			
			NOTE_QUANTITY:
			for (int i = 0; i < quantity; i++) {
				int  currentDuration = duration;
				int  currentTremolo  = tremolo;
				long startTicks      = instr.getCurrentTicks();
				
				if (REST_VALUE == note) {
					if (! isFake)
						instr.incrementTicks( duration );
					continue;
				}
				
				if (isFake) {
					transpose(note, channel);
					return;
				}
				else {
					
					TREMOLO_PART:
					while (currentDuration > 0) {
						startTicks = instr.getCurrentTicks();
						
						// handle tremolo
						if (tremolo == currentDuration) {
							currentDuration = 0; // normal note (or last, dividable tremolo note)
						}
						else if (currentDuration > tremolo) {
							currentDuration -= tremolo; // tremolo note
						}
						else {
							// last rest of a tremolo note
							currentTremolo  = currentDuration;
							currentDuration = 0;
						}
						
						// get end ticks of the current note
						long endTicks = instr.addNote(note, currentTremolo);
						int  newNote  = transpose(note, channel);
						
						// correction of legato overlappings needed?
						Long tickToCorrect = instr.getStopTickToCorrect();
						if (tickToCorrect != null) {
							long targetTick = startTicks - 1;
							try {
								SequenceCreator.moveNoteOffMessage(channel, newNote, tickToCorrect, targetTick);
							}
							catch (Exception e) {
								throw new ParseException( e.getMessage() );
							}
						}
						
						// create and add messages
						SequenceCreator.addMessageKeystroke(channel, newNote, startTicks, endTicks, velocity);
					}
				}
			}
		}
		catch (InvalidMidiDataException e) {
			throw new ParseException( Dict.get(Dict.ERROR_MIDI_PROBLEM) + e.getMessage() );
		}
		
		if (multiple) {
			instr.setCurrentTicks( absoluteStartTicks );
		}
	}
	
	/**
	 * Parses the options part of a command.
	 * 
	 * @param optString    The options string of the channel command to be parsed.
	 * @param isFake       **true**, if this is called inside a function definition, pattern definition or block.
	 * @return             All options and their values that have been found in the
	 *                     provided options string.
	 * @throws ParseException    If the command cannot be parsed.
	 */
	private ArrayList<CommandOption> parseOptions(String optString, boolean isFake) throws ParseException {
		ArrayList<CommandOption> options = new ArrayList<>();
		
		String[] optTokens = optString.split(OPT_SEPARATOR, -1);
		for (String opt : optTokens) {
			opt = clean( opt );
			String[] optParts = opt.split("[" + Pattern.quote(OPT_ASSIGNER) + "\\s]+", 2); // name and value can be separated by OPT_ASSIGNER (e,g, "=") and/or whitespace(s)
			if (optParts.length > 2)
				throw new ParseException( Dict.get(Dict.ERROR_CANT_PARSE_OPTIONS) + opt );
			
			// value is a variable? - don't check if this is fake
			if (isFake && optParts.length > 1 && varPattern.matcher(optParts[1]).find())
				return options;
			
			// construct name and value
			String        optName  = optParts[0];
			CommandOption optValue = new CommandOption();
			
			if (isCondCheckParsRun && ! IF.equals(optName) && ! ELSIF.equals(optName) && ! ELSE.equals(optName))
				continue;
			
			if (V.equals(optName) || VELOCITY.equals(optName)) {
				if (optParts.length < 2 || "".equals(optParts[1])) {
					throw new ParseException( Dict.get(Dict.ERROR_OPTION_NEEDS_VAL) + optName );
				}
				optName = OPT_VELOCITY;
				int val = toInt(optParts[1]);
				if (val > 127)
					throw new ParseException( Dict.get(Dict.ERROR_VEL_NOT_MORE_THAN_127) );
				if (val < 1)
					throw new ParseException( Dict.get(Dict.ERROR_VEL_NOT_LESS_THAN_1) );
				optValue.set(optName, val);
			}
			else if (D.equals(optName) || DURATION.equals(optName)) {
				if (optParts.length < 2 || "".equals(optParts[1])) {
					throw new ParseException( Dict.get(Dict.ERROR_OPTION_NEEDS_VAL) + optName );
				}
				optName = OPT_DURATION;
				String[] valueParts = optParts[1].split(Pattern.quote(DURATION_PERCENT), -1);
				float    val        = toFloat(valueParts[0]);
				if (valueParts.length > 1)
					val /= 100; // percentage --> numeric
				if (val <= 0.0)
					throw new ParseException( Dict.get(Dict.ERROR_DURATION_MORE_THAN_0) );
				optValue.set(optName, val);
			}
			else if (Q.equals(optName) || QUANTITY.equals(optName)) {
				if (optParts.length < 2 || "".equals(optParts[1])) {
					throw new ParseException( Dict.get(Dict.ERROR_OPTION_NEEDS_VAL) + optName );
				}
				optName = OPT_QUANTITY;
				optValue.set( optName, toInt(optParts[1], true) );
			}
			else if (M.equals(optName) || MULTIPLE.equals(optName)) {
				optName = OPT_MULTIPLE;
				optValue.set(optName, true);
				if (optParts.length > 1)
					throw new ParseException( Dict.get(Dict.ERROR_OPTION_VAL_NOT_ALLOWED) + optName );
			}
			else if (L.equals(optName) || LYRICS.equals(optName)) {
				if (optParts.length < 2 || "".equals(optParts[1])) {
					throw new ParseException( Dict.get(Dict.ERROR_OPTION_NEEDS_VAL) + optName );
				}
				optName = OPT_LYRICS;
				optValue.set( optName, optParts[1] );
			}
			else if (T.equals(optName) || TUPLET.equals(optName)) {
				optName = OPT_TUPLET;
				if (1 == optParts.length) {
					optValue.set(optName, TRIPLET);
				}
				else {
					Pattern pattern = Pattern.compile("^(\\d+)" + Pattern.quote(TUPLET_FOR) + "(\\d+)$");
					Matcher matcher = pattern.matcher(optParts[1]);
					if (matcher.matches()) {
						String num1 = matcher.group(1);
						String num2 = matcher.group(2);
						if ("0".equals(num1) || "0".equals(num2))
							throw new ParseException( Dict.get(Dict.ERROR_TUPLET_INVALID) + optParts[1] );
						optValue.set( optName, optParts[1] );
					}
					else {
						throw new ParseException( Dict.get(Dict.ERROR_TUPLET_INVALID) + optParts[1] );
					}
				}
			}
			else if (TR.equals(optName) || TREMOLO.equals(optName)) {
				if (optParts.length < 2 || "".equals(optParts[1])) {
					throw new ParseException( Dict.get(Dict.ERROR_OPTION_NEEDS_VAL) + optName );
				}
				optName = OPT_TREMOLO;
				optValue.setValueString(optParts[1]);
				optValue.set( optName, parseDuration(optParts[1]) );
			}
			else if (S.equals(optName) || SHIFT.equals(optName)) {
				if (optParts.length < 2 || "".equals(optParts[1])) {
					throw new ParseException( Dict.get(Dict.ERROR_OPTION_NEEDS_VAL) + optName );
				}
				optName = OPT_SHIFT;
				optValue.set( optName, toInt(optParts[1], false) );
			}
			else if (IF.equals(optName)) {
				if (isCondCheckParsRun && (optParts.length < 2 || "".equals(optParts[1]))) {
					throw new ParseException( Dict.get(Dict.ERROR_OPTION_NEEDS_VAL) + optName );
				}
				String val = optParts.length >= 2 ? optParts[1] : "";
				optName = OPT_IF;
				optValue.set( optName, val );
			}
			else if (ELSIF.equals(optName)) {
				if (isCondCheckParsRun && (optParts.length < 2 || "".equals(optParts[1]))) {
					throw new ParseException( Dict.get(Dict.ERROR_OPTION_NEEDS_VAL) + optName );
				}
				String val = optParts.length >= 2 ? optParts[1] : "";
				optName = OPT_ELSIF;
				optValue.set( optName, val );
			}
			else if (ELSE.equals(optName)) {
				optName = OPT_ELSE;
				optValue.set(optName, true);
				if (optParts.length > 1)
					throw new ParseException( Dict.get(Dict.ERROR_OPTION_VAL_NOT_ALLOWED) + optName );
			}
			else {
				throw new ParseException( Dict.get(Dict.ERROR_UNKNOWN_OPTION) + optName );
			}
			options.add(optValue);
		}
		
		return options;
	}
	
	/**
	 * Returns the note value of a given string in the context of the given channel.
	 * (Used to parse a note from a channel command)
	 * 
	 * The string can be:
	 * 
	 * - the pause character (in this case REST_VALUE is returned)
	 * - the note value in numeric characters
	 * - the note name as defined by the chosen configuration (if the channel is not the percussion channel)
	 * - the percussion name as defined by the chosen configutation (if channel is 9)
	 * 
	 * @param note       The note string to be parsed.
	 * @param channel    The channel number.
	 * @return MIDI note value of the given note
	 * @throws ParseExceptionif the note or percussion name is unknown or the note value is out of the legal range for MIDI notes
	 */
	private int parseNote(String note, int channel) throws ParseException {
		if (note.equals(REST))
			return REST_VALUE;
		else if (note.matches("^\\d+$"))
			return toInt( note );
		else {
			if (9 == channel)
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
		if (noteName.matches("^\\d+$")) {
			noteVal = toInt( noteName );
		}
		else {
			noteVal = Dict.getNote( noteName );
			if (Dict.UNKNOWN_CODE == noteVal) {
				throw new ParseException( Dict.get(Dict.ERROR_UNKNOWN_NOTE) + noteName );
			}
		}
		if (noteVal > 127) {
			throw new ParseException( Dict.get(Dict.ERROR_NOTE_TOO_BIG) + noteName );
		}
		
		return noteVal;
	}
	
	/**
	 * Analyzes the note/chord string of a channel command.
	 * This token can be a note string, a note number, a predefined chord or an inline chord,
	 * consisting of any combination of the former.
	 * 
	 * @param token   The note/chord part of a channel command (second column).
	 * @return a collection of all included notes, or **null**, if the token contains only a single note.
	 * @throws ParseException if one of the notes cannot be parsed.
	 */
	private ArrayList<Integer> parseChord(String token) throws ParseException {
		if (token.matches(".*" + Pattern.quote(CHORD_SEPARATOR) + ".*") || chords.containsKey(token)) {
			ArrayList<Integer> chordElements = new ArrayList<>();
			
			// collect comma-separated inline chord parts
			String[] inlineElements = token.split( Pattern.quote(CHORD_SEPARATOR) );
			for (String inlineElement : inlineElements) {
				
				// collect predefined chord elements
				if (chords.containsKey(inlineElement)) {
					for (int note : chords.get(inlineElement)) {
						chordElements.add(note);
					}
				}
				else {
					// collect simple note (or percussion instrument)
					int note = Dict.getPercussion(inlineElement);
					if (Dict.UNKNOWN_CODE == note) {
						note = parseNote(inlineElement);
					}
					chordElements.add(note);
				}
			}
			return chordElements;
		}
		
		// no chord found
		return null;
	}
	
	/**
	 * Splits a channel command with a chord into the single notes and applies one channel command for each note.
	 * 
	 * @param tokens             Token array.
	 * @return                   **true**, if the channel command contains a chord, otherwise **false**.
	 * @throws ParseException    if one of the notes cannot be parsed.
	 */
	private boolean parseChordNotes(String[] tokens) throws ParseException {
		
		if (tokens.length < 3) {
			throw new ParseException( Dict.get(Dict.ERROR_CH_CMD_NUM_OF_ARGS) );
		}
		
		// chord or not?
		ArrayList<Integer> chordElements = parseChord(tokens[1]);
		if (null == chordElements) {
			
			// not a chord
			return false;
		}
		
		// call this method again with each chord, adding the multiple option
		int i = 1;
		for (Integer chordElement : chordElements) {
			// create and process a new token array for each note of the chord
			String[] subTokens = new String[3];
			subTokens[0] = tokens[0];         // same channel
			subTokens[1] = chordElement + ""; // note name or number
			if (chordElements.size() == i) {
				// last note of the chord: use original options
				subTokens[2] = tokens[2];
			}
			else {
				// add MULTIPLE to the options string
				// so that the next note of the same chord starts at the same time
				subTokens[2] = addMultiple( tokens[2] );
			}
			parseTokens(subTokens);
			i++;
		}
		
		return true;
	}
	
	/**
	 * Adds the option MULTIPLE to an option string.
	 * This is used if a note of a predefined chord is produced - so that the next note
	 * of the same chord starts at the same tick.
	 * 
	 * @param original token containing the velocity and the option string: token[2]
	 * @return same token but with the MULTIPLE option
	 */
	private String addMultiple( String original ) {
		
		String[] subTokens = original.split( "\\s+", 2 );
		String optStr;
		if (subTokens.length > 1) {
			optStr = subTokens[ 1 ];
		}
		else {
			return subTokens[ 0 ] + " " + MULTIPLE;
		}
		
		// cut away trailing whitespaces and option separators (e.g. ',')
		String cleanedOptString = optStr.replaceFirst( "[" + Pattern.quote(OPT_SEPARATOR) + "\\s]+$", "" );
		
		// append the option MULTIPLE
		subTokens[ 1 ] = cleanedOptString + OPT_SEPARATOR + MULTIPLE;
		
		return subTokens[ 0 ] + " " + subTokens[ 1 ];
	}

	/**
	 * Synchronizes all channels according to the given channel description.
	 * Sets the current ticks of each of these channels to the value of the
	 * channel with the maximum current tick value.
	 * 
	 * @param channelDesc    channel description (e.g. "1-3,5,7-p")
	 * @throws ParseException    If the channel description cannot be parsed.
	 */
	private void synchronize(String channelDesc) throws ParseException {
		
		// find out which channels to sync
		HashSet<Integer> channels = new HashSet<>();
		String[] ranges = channelDesc.split(Pattern.quote(PARTIAL_SYNC_SEP), -1);
		for (String range : ranges) {
			String[] limits = range.split(Pattern.quote(PARTIAL_SYNC_RANGE), -1);
			if (1 == limits.length) {
				if ("".equals(range))
					throw new ParseException( Dict.get(Dict.ERROR_PARTIAL_RANGE_EMPTY) );
				channels.add( toChannel(range) );
			}
			else if (2 == limits.length) {
				int fromCh = toChannel( limits[0] );
				int toCh   = toChannel( limits[1] );
				if (toCh <= fromCh) {
					throw new ParseException( Dict.get(Dict.ERROR_PARTIAL_RANGE_ORDER) + range );
				}
				for (int channel = fromCh; channel <= toCh; channel++) {
					channels.add(channel);
				}
			}
			else {
				throw new ParseException( Dict.get(Dict.ERROR_PARTIAL_RANGE) + range );
			}
		}
		
		// collect all relevant instruments
		ArrayList<Instrument> partialInstruments = new ArrayList<>();
		for (int channel : channels) {
			partialInstruments.add( instruments.get(channel) );
		}
		
		// sync the channels
		long maxTicks = Instrument.getMaxCurrentTicks(partialInstruments);
		for (int channel : channels) {
			instruments.get( channel ).setCurrentTicks( maxTicks );
		}
	}
	
	/**
	 * Postprocesses a finished INSTRUMENTS block.
	 * Initializes the percussion channel.
	 * Initializes all necessary data structures for all channels. Thereby all undefined
	 * channels will be initialized with a fake instrument so that the data structures work.
	 * 
	 * @throws ParseException    if something went wrong.
	 */
	private void postprocessInstruments() throws ParseException {
		
		// sort instruments ascending
		Collections.sort( instruments );
		
		// find out which channels are missing
		HashSet<Integer> missing = new HashSet<>();
		for (int i=0; i<MidiDevices.NUMBER_OF_CHANNELS; i++) {
			missing.add( i );
		}
		for (Instrument instr : instruments) {
			missing.remove( instr.channel );
		}
		
		// add the missing channels
		for (int i : missing) {
			Instrument fakeInstr;
			if (9 == i) {
				fakeInstr = new Instrument( i, 0, Dict.get(Dict.PERCUSSION_CHANNEL), false );
			}
			else {
				fakeInstr = new Instrument( i, 0, Dict.get(Dict.DEFAULT_CHANNEL_COMMENT), true );
			}
			instruments.add( fakeInstr );
		}
		
		// sort again
		Collections.sort( instruments );
		
		// initialize sequence
		try {
			for (Instrument instr : instruments) {
				int    channel      = instr.channel;
				int    instrNum     = instr.instrumentNumber;
				String instrComment = instr.instrumentName;
				int    bankMSB      = instr.getBankMSB();
				int    bankLSB      = instr.getBankLSB();
				// reset instrument
				instr.reset();
				if (! instr.autoChannel) {
					if (bankMSB != 0) {
						SequenceCreator.setBank( channel, 0L, bankMSB, false );
					}
					if (bankLSB != 0) {
						SequenceCreator.setBank( channel, 0L, bankLSB, true );
					}
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
	 * Postprocesses a finished META block.
	 * 
	 * Sets all defined meta events in the MIDI sequence.
	 * 
	 * @throws ParseException    if something went wrong.
	 */
	private void postprocessMeta() throws ParseException {
		try {
			// copyright
			StringBuilder copyright = metaInfo.get("copyright");
			if (copyright != null) {
				SequenceCreator.addMessageCopyright(copyright.toString());
			}
			
			// RP-026 messages
			StringBuilder rp26 = new StringBuilder("");
			String[]      keys = {"title", "composer", "lyrics", "artist"};
			for (String key : keys) {
				StringBuilder value = metaInfo.get(key);
				if (value != null) {
					rp26.append("{#" + key + "=" + value + "}");
				}
			}

			// add midica version
			rp26.append("{#" + LyricUtil.SOFTWARE + "=" + "Midica " + Midica.VERSION + "}");
			
			// add end tag and write RP-026 tags
			rp26.append("{#}");
			SequenceCreator.addMessageLyrics(rp26.toString(), 0, true);
			
			// postprocess soft karaoke
			if (isSoftKaraoke) {
				SequenceCreator.addMessageText("@KMIDI KARAOKE FILE", 0, 1);
				if (softKaraokeInfo.containsKey("sk_version")) {
					String version = softKaraokeInfo.get("sk_version").get(0);
					SequenceCreator.addMessageText("@V" + version, 0, 1);
				}
				if (softKaraokeInfo.containsKey("sk_info")) {
					ArrayList<String> infos = softKaraokeInfo.get("sk_info");
					for (String info : infos) {
						SequenceCreator.addMessageText("@I" + info, 0, 1);
					}
				}
				if (softKaraokeInfo.containsKey("sk_lang")) {
					String language = softKaraokeInfo.get("sk_lang").get(0);
					SequenceCreator.addMessageText("@L" + language, 0, 2);
				}
				int tFieldCount  = 0;
				String[] tFields = {"", "", ""};
				if (softKaraokeInfo.containsKey("sk_title")) {
					tFieldCount = 1;
					tFields[0]  = softKaraokeInfo.get("sk_title").get(0);
				}
				if (softKaraokeInfo.containsKey("sk_author")) {
					tFieldCount = tFieldCount < 2 ? 2 : tFieldCount;
					tFields[1]  = softKaraokeInfo.get("sk_author").get(0);
				}
				if (softKaraokeInfo.containsKey("sk_copyright")) {
					tFieldCount = tFieldCount < 3 ? 3 : tFieldCount;
					tFields[2]  = softKaraokeInfo.get("sk_copyright").get(0);
				}
				for (int i = 0; i < tFieldCount; i++) {
					SequenceCreator.addMessageText("@T" + tFields[i], 0, 2);
				}
			}
		}
		catch (InvalidMidiDataException e) {
			throw new ParseException( Dict.get(Dict.ERROR_MIDI_PROBLEM) + e.getMessage() );
		}
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
		if (greaterZero) {
			i = toInt( s );
			if (0 == i)
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
			if (i < 0) {
				throw new ParseException( Dict.get(Dict.ERROR_NEGATIVE_NOT_ALLOWED) + s );
			}
			return i;
		}
		catch ( NumberFormatException e ) {
			throw new ParseException(Dict.get(Dict.ERROR_NOT_AN_INTEGER) + s);
		}
	}
	
	/**
	 * Parses a String that is expected to be a float.
	 * 
	 * @param s              The string to be parsed.
	 * @return               The parsed value.
	 * @throws ParseException    If the string cannot be parsed.
	 */
	private float toFloat(String s) throws ParseException {
		try {
			float f = Float.parseFloat(s);
			return f;
		}
		catch ( NumberFormatException e ) {
			throw new ParseException(Dict.get(Dict.ERROR_NOT_A_FLOAT) + s);
		}
	}
	
	/**
	 * Cuts away any leading and trailing whitespaces from an input string
	 * and returns the resulting string.
	 * 
	 * @param input    The string to be cleaned.
	 * @return         Resulting string without leading or trailing whitespaces.
	 */
	private String clean(String input) {
		input = input.replaceFirst( "^\\s+", "" ); // eliminate leading whitespaces
		input = input.replaceFirst( "\\s+$", "" ); // eliminate trailing whitespaces
		return input;
	}
	
	/**
	 * Removes comments and leding and trailing whitespaces from a source code line.
	 * 
	 * @param line  The source code line.
	 * @return  the cleaned line.
	 */
	private String cleanLine(String line) {
		
		// cut away comments
		String cleanedLine = line.split( Pattern.quote(COMMENT) + "|" + Pattern.quote(ORIGINAL_COMMENT) + "|$", 2 )[ 0 ];
		
		// eliminate leading and trailing whitespaces
		cleanedLine = clean(cleanedLine);
		
		return cleanedLine;
	}
	
	/**
	 * Removes comments and leding and trailing whitespaces from a whole list of
	 * source code lines.
	 * 
	 * @param lines  The source code lines to be cleaned.
	 */
	private void cleanLines(ArrayList<String> lines) {
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			line = cleanLine(line);
			lines.set(i, line);
		}
	}
	
	/**
	 * Initializes the parser for a new parsing run.
	 * This is called at the beginning of parse().
	 * 
	 * @throws ParseException if sequence creation fails.
	 */
	private void reset() throws ParseException {
		// reset fields
		currentMode         = MODE_DEFAULT;
		currentFunctionName = null;
		currentPatternName  = null;
		condChainOpened     = false;
		condChainHit        = false;
		
		if (isRootParser) {
			try {
				SequenceCreator.reset( chosenCharset );
			}
			catch (InvalidMidiDataException e) {
				throw new ParseException(e.toString());
			}
			instrumentsParsed    = false;
			metaInfo             = new HashMap<>();
			softKaraokeInfo      = new HashMap<>();
			frstInstrBlkOver     = false;
			isDefineParsRun      = false;
			isConstParsRun       = false;
			isChInstMetaParsRun  = false;
			isFuncNameParsRun    = false;
			isFuncParsRun        = false;
			isCondCheckParsRun   = false;
			isDefaultParsRun     = false;
			instruments          = new ArrayList<>();
			definedFunctionNames = new HashSet<>();
			definedPatternNames  = new HashSet<>();
			fileCache            = new HashMap<>();
			functions            = new HashMap<>();
			functionToFile       = new HashMap<>();
			functionToLineOffset = new HashMap<>();
			patterns             = new HashMap<>();
			patternToFile        = new HashMap<>();
			patternToLineOffset  = new HashMap<>();
			chords               = new TreeMap<>();
			nestableBlkDepth     = 0;
			nestableBlkStack     = new ArrayDeque<>();
			stackTrace           = new ArrayDeque<>();
			functionNameStack    = new ArrayDeque<>();
			functionLineStack    = new ArrayDeque<>();
			functionFileStack    = new ArrayDeque<>();
			paramStackNamed      = new ArrayDeque<>();
			paramStackIndexed    = new ArrayDeque<>();
			patternNameStack     = new ArrayDeque<>();
			patternLineStack     = new ArrayDeque<>();
			patternFileStack     = new ArrayDeque<>();
			redefinitions        = new HashSet<>();
			soundfontParsed      = false;
			isSoftKaraoke        = false;
			isPatternSubChord    = false;
			constants            = new HashMap<>();
			variables            = new HashMap<>();
			varPattern           = null;
			callPattern          = null;
			condPattern          = null;
			condInPattern        = null;
			crlfSkPattern        = null;
			refreshSyntax();
			NestableBlock.reset();
		}
	}
}
