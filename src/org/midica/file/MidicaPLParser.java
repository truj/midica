/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.midi.InvalidMidiDataException;

import org.midica.Midica;
import org.midica.config.Config;
import org.midica.config.Dict;
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
	
	private static final int MODE_DEFAULT     = 0;
	private static final int MODE_INSTRUMENTS = 1;
	private static final int MODE_MACRO       = 2;
	private static final int MODE_META        = 3;
	
	private static final int TICK_BANK_BEFORE_PROGRAM = 10; // how many ticks a bank select will be made before the program change
	
	private static final int REST_VALUE = -1;
	
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
	public static String INLINE_CHORD_SEP   = null;
	public static String COMMENT            = null;
	public static String DEFINE             = null;
	public static String DOT                = null;
	public static String END                = null;
	public static String BLOCK_OPEN         = null;
	public static String BLOCK_CLOSE        = null;
	public static String GLOBAL             = null;
	public static String INCLUDE            = null;
	public static String INCLUDE_FILE       = null;
	public static String SOUNDFONT          = null;
	public static String INSTRUMENTS        = null;
	public static String META               = null;
	public static String META_COPYRIGHT     = null;
	public static String META_TITLE         = null;
	public static String META_COMPOSER      = null;
	public static String META_LYRICIST      = null;
	public static String META_ARTIST        = null;
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
	public static String MACRO              = null;
	public static String M                  = null;
	public static String MULTIPLE           = null;
	public static String OPT_ASSIGNER       = null;
	public static String OPT_SEPARATOR      = null;
	public static String P                  = null;
	public static String REST               = null;
	public static String PROG_BANK_SEP      = null;
	public static String Q                  = null;
	public static String QUANTITY           = null;
	public static String D                  = null;
	public static String DURATION           = null;
	public static String DURATION_PERCENT   = null;
	public static String T                  = null;
	public static String TUPLET             = null;
	public static String V                  = null;
	public static String VELOCITY           = null;
	public static String TRIPLET            = null;
	public static String TUPLET_INTRO       = null;
	public static String TUPLET_FOR         = null;
	public static String DURATION_PLUS      = null;
	
	public static String ORIGINAL_DEFINE       = null;
	public static String ORIGINAL_INCLUDE_FILE = null;
	
	protected static ArrayList<Instrument> instruments = null;
	
	private static LyricUtil lyricUtil = LyricUtil.getInstance();
	
	private static HashMap<String, ArrayList<String>> macros            = null;
	private static HashMap<String, HashSet<Integer>>  chords            = null;
	private static boolean                            instrumentsParsed = false;
	private static HashMap<String, StringBuilder>     metaInfo          = null;
	private static boolean                            frstInstrBlkOver  = false;
	private static String                             chosenCharset     = null;
	private static HashSet<String>                    definedMacroNames = null;
	private static int                                nestableBlkDepth  = 0;
	private static Deque<NestableBlock>               nestableBlkStack  = null;
	private static HashSet<String>                    redefinitions     = null;
	private static boolean                            soundfontParsed   = false;
	
	private static boolean isDefineParsRun     = false; // parsing run for define commands
	private static boolean isChInstMetaParsRun = false; // parsing run for chords, meta, instruments and block nesting
	private static boolean isMacrNameParsRun   = false; // parsing run for defined macro names
	private static boolean isMacroParsRun      = false; // parsing run for macros
	private static boolean isDefaultParsRun    = false; // final parsing run
	
	/* *******************
	 * instance fields
	 *********************/
	
	private File              file              = null;
	private int               currentMode       = MODE_DEFAULT;
	private String            currentMacroName  = null;
	private ArrayList<String> currentMacro      = null;
	
	/**
	 * Returns the absolute path of the successfully parsed MidicaPL file.
	 * Returns **null**, if no file has been successfully parsed or the successfully parsed file
	 * is not a MidicaPL file.
	 * 
	 * @return file path or **null**.
	 */
	public static String getFilePath() {
		return getFilePath("midica");
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
		INLINE_CHORD_SEP   = Dict.getSyntax( Dict.SYNTAX_INLINE_CHORD_SEP   );
		COMMENT            = Dict.getSyntax( Dict.SYNTAX_COMMENT            );
		DEFINE             = Dict.getSyntax( Dict.SYNTAX_DEFINE             );
		DOT                = Dict.getSyntax( Dict.SYNTAX_DOT                );
		END                = Dict.getSyntax( Dict.SYNTAX_END                );
		BLOCK_OPEN         = Dict.getSyntax( Dict.SYNTAX_BLOCK_OPEN         );
		BLOCK_CLOSE        = Dict.getSyntax( Dict.SYNTAX_BLOCK_CLOSE        );
		GLOBAL             = Dict.getSyntax( Dict.SYNTAX_GLOBAL             );
		INCLUDE            = Dict.getSyntax( Dict.SYNTAX_INCLUDE            );
		INCLUDE_FILE       = Dict.getSyntax( Dict.SYNTAX_INCLUDE_FILE       );
		SOUNDFONT          = Dict.getSyntax( Dict.SYNTAX_SOUNDFONT          );
		INSTRUMENTS        = Dict.getSyntax( Dict.SYNTAX_INSTRUMENTS        );
		META               = Dict.getSyntax( Dict.SYNTAX_META               );
		META_COPYRIGHT     = Dict.getSyntax( Dict.SYNTAX_META_COPYRIGHT     );
		META_TITLE         = Dict.getSyntax( Dict.SYNTAX_META_TITLE         );
		META_COMPOSER      = Dict.getSyntax( Dict.SYNTAX_META_COMPOSER      );
		META_LYRICIST      = Dict.getSyntax( Dict.SYNTAX_META_LYRICIST      );
		META_ARTIST        = Dict.getSyntax( Dict.SYNTAX_META_ARTIST        );
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
		MACRO              = Dict.getSyntax( Dict.SYNTAX_MACRO              );
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
		T                  = Dict.getSyntax( Dict.SYNTAX_T                  );
		TUPLET             = Dict.getSyntax( Dict.SYNTAX_TUPLET             );
		V                  = Dict.getSyntax( Dict.SYNTAX_V                  );
		VELOCITY           = Dict.getSyntax( Dict.SYNTAX_VELOCITY           );
		TRIPLET            = Dict.getSyntax( Dict.SYNTAX_TRIPLET            );
		TUPLET_INTRO       = Dict.getSyntax( Dict.SYNTAX_TUPLET_INTRO       );
		TUPLET_FOR         = Dict.getSyntax( Dict.SYNTAX_TUPLET_FOR         );
		DURATION_PLUS      = Dict.getSyntax( Dict.SYNTAX_DURATION_PLUS      );
		
		// Remember the original names of some commands.
		// Needed to redefine them without getting an error in the following parsing runs.
		ORIGINAL_DEFINE       = DEFINE;
		ORIGINAL_INCLUDE_FILE = INCLUDE_FILE;
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
			// open file for reading
			FileInputStream   fis = new FileInputStream(file);
			InputStreamReader ir  = new InputStreamReader(fis, charset);
			BufferedReader    br  = new BufferedReader(ir);
			String            rawLine;
			
			// get lines from file
			ArrayList<String> lines = new ArrayList<String>();
			while (null != (rawLine = br.readLine())) {
				
				// cut away comments
				String cleanedLine = rawLine.split( Pattern.quote(COMMENT) + "|$", 2 )[ 0 ];
				
				// eliminate leading and trailing whitespaces
				cleanedLine = clean(cleanedLine);
				
				lines.add(cleanedLine);
			}
			br.close();
			
			if (isRootParser) {
				// look for define commands
				isDefineParsRun = true;
				parsingRun(lines);
				isDefineParsRun = false;
				
				// look for chords, the very first instruments block,
				// meta definitions, and checks block nesting
				isChInstMetaParsRun = true;
				parsingRun(lines);
				postprocessMeta(); // apply all collected meta info
				isChInstMetaParsRun = false;
				
				// collect all macro names that are defined somewhere
				isMacrNameParsRun = true;
				parsingRun(lines);
				isMacrNameParsRun = false;
				
				// look for macros
				isMacroParsRun = true;
				parsingRun(lines);
				isMacroParsRun = false;
				
				// look for everything else
				// final parsing run, building up the sequence
				isDefaultParsRun = true;
				parsingRun(lines);
				isDefaultParsRun = false;
			}
			else {
				// Not the root parser.
				// This is called for each INCLUDE_FILE command and parsing run.
				// So the parsing run has to be executed only once here.
				// For which run it's called is obvious because the according fields are static.
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
			postprocessSequence( SequenceCreator.getSequence(), "midica", chosenCharset );
		}
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
		String[] tokens = line.split("\\s+", 3);
		
		if ("".equals(tokens[0])) {
			// empty line or only comments
			return;
		}
		
		// only 2 tokens for meta commands
		if (isChInstMetaParsRun && isMetaCmd(tokens[0])) {
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
		return false;
	}
	
	/**
	 * Creates and returns a snapshot of the tickstamps of all channels.
	 * The indexes of the returned structure are the channels.
	 * The values are the tickstamps.
	 * 
	 * @return snapshot of tickstamps
	 */
	public ArrayList<Long> rememberTickstamps() {
		ArrayList<Long> tickstampByChannel = new ArrayList<Long>();
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
	 * E.g. find definition commands, chords, macros, and so on.
	 * 
	 * @param lines           All pre-cleaned lines of the MidicaPL source.
	 * @throws ParseException if one of the lines cannot be parsed.
	 * @throws IOException    if the file path cannot be calculated.
	 */
	private void parsingRun(ArrayList<String> lines) throws ParseException, IOException {
		int lineNumber = 0;
		try {
			for (String line : lines) {
				lineNumber++;
				parseLine(line);
			}
			
			// find open blocks at the end of the file
			lineNumber++;
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
				e.setLineNumber(lineNumber);
			}
			if (null == e.getFileName()) {
				e.setFileName( file.getCanonicalPath() );
			}
			throw e;
		}
		catch (Exception e) {
			// any other exception? - wrap it into a parsing exception with file and line
			ParseException pe = new ParseException(e.toString());
			e.printStackTrace();
			pe.setLineNumber(lineNumber);
			pe.setFileName( file.getCanonicalPath() );
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
		
		checkIfCmdExists(tokens[0]);
		
		// continue or not - decide depending on parsing run and current mode
		boolean mustIgnore = mustIgnore(tokens[0]);
		if (mustIgnore) {
			return;
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
		boolean isMacro = false;
		boolean isBlock = false;
		if (MODE_MACRO == currentMode) {
			isFake  = true;
			isMacro = true;
		}
		else if (nestableBlkDepth > 0) {
			isFake  = true;
			isBlock = true;
		}
		
		// global command?
		if (tokens[0].matches("^" + Pattern.quote(GLOBAL) + "$")) {
			if (MODE_INSTRUMENTS == currentMode) {
				// we are inside an instruments definition
				throw new ParseException( Dict.get(Dict.ERROR_GLOBALS_IN_INSTR_DEF) );
			}
			else if (isMacro) {
				currentMacro.add(String.join(" ", tokens)); // add to macro
			}
			else if (isBlock) {
				nestableBlkStack.peek().add(tokens); // add to block
			}
			parseGlobalCmd(tokens, isFake);
		}
		
		// channel or instruments command
		else if (tokens[0].matches("^\\d+$")) {
			if (!isMacro) {
				checkInstrumentsParsed();
			}
			
			// instruments command
			if (MODE_INSTRUMENTS == currentMode) {
				// we are inside an instruments definition block
				parseInstrumentCmd(tokens);
				return;
			}
			
			// more than one note or chord? --> call this method (parseTokens()) once for each chord element
			if (parseChordNotes(tokens)) {
				return;
			}
			
			// channel command with a single note
			if (isMacro)
				currentMacro.add(String.join(" ", tokens)); // add to macro
			else if (isBlock)
				nestableBlkStack.peek().add(tokens); // add to block
			
			// apply or fake command
			parseChannelCmd(tokens, isFake);
		}
		
		// include a macro
		else if (INCLUDE.equals(tokens[0])) {
			
			// only remember the line?
			if (isMacro)
				currentMacro.add(String.join(" ", tokens)); // add to macro
			else if (isBlock)
				nestableBlkStack.peek().add(tokens); // add to block
			
			parseINCLUDE(tokens, isFake);
		}
		
		// nestable block commands
		// "(", ")", instruments, macro, end
		else if (BLOCK_OPEN.equals(tokens[0]) || BLOCK_CLOSE.equals(tokens[0])) {
			
			// only remember the line?
			if (isMacro)
				currentMacro.add(String.join(" ", tokens));
			
			parseBLOCK(tokens, isMacro);
		}
		
		// mode command (instruments, macro, meta, end)
		else if (INSTRUMENTS.equals(tokens[0]) || MACRO.equals(tokens[0]) || META.equals(tokens[0]) || END.equals(tokens[0])) {
			parseModeCmd(tokens);
			return;
		}
		
		// meta definition
		else if (MODE_META == currentMode) {
			parseMetaCmd(tokens);
		}
		
		// root-level commands (that are not allowed to appear inside of a block)
		// define, chord, include_file, soundfont
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
		else if ( MACRO.equals(cmd)                 ) {}
		else if ( CHORD.equals(cmd)                 ) {}
		else if ( INCLUDE.equals(cmd)               ) {}
		else if ( INCLUDE_FILE.equals(cmd)          ) {}
		else if ( SOUNDFONT.equals(cmd)             ) {}
		else if ( DEFINE.equals(cmd)                ) {}
		else if ( INSTRUMENTS.equals(cmd)           ) {}
		else if ( META.equals(cmd)                  ) {}
		else if ( META_COPYRIGHT.equals(cmd)        ) {}
		else if ( META_TITLE.equals(cmd)            ) {}
		else if ( META_COMPOSER.equals(cmd)         ) {}
		else if ( META_LYRICIST.equals(cmd)         ) {}
		else if ( META_ARTIST.equals(cmd)           ) {}
		else if ( GLOBAL.equals(cmd)                ) {}
		else if ( P.equals(cmd)                     ) {}
		else if ( ORIGINAL_DEFINE.equals(cmd)       ) {}
		else if ( ORIGINAL_INCLUDE_FILE.equals(cmd) ) {}
		else if ( cmd.matches("^\\d+$")             ) {}
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
	 *  - get all needed macro names from include commands
	 *  - parse macros
	 *  - parse everything else
	 * 
	 * @param cmd             The first token in the line
	 * @return                **true**, if the current line must be ignored.
	 * @throws ParseException if this method is called without configuring the parsing run before.
	 */
	private boolean mustIgnore(String cmd) throws ParseException {
		
		// always follow files
		if (INCLUDE_FILE.equals(cmd) || ORIGINAL_INCLUDE_FILE.equals(cmd)) {
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
			if (END.equals(cmd) && (MODE_INSTRUMENTS == currentMode || MODE_META == currentMode)) {
				// handle later
			}
			else if (BLOCK_OPEN.equals(cmd) || BLOCK_CLOSE.equals(cmd) || MACRO.equals(cmd) || END.equals(cmd)) {
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
		if (META.equals(cmd) || MODE_META == currentMode) {
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
		
		// look for defined macro names
		if (isMacrNameParsRun) {
			if (MACRO.equals(cmd)) {
				return false;
			}
			
			// handle macro-END
			if (END.equals(cmd)) {
				currentMode = MODE_DEFAULT;
			}
			
			return true;
		}
		
		// parse macros
		if (isMacroParsRun) {
			if (MACRO.equals(cmd)) {
				return false;
			}
			if (MODE_MACRO == currentMode) {
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
			
			if (MACRO.equals(cmd)) {
				currentMode = MODE_MACRO;
				return true;
			}
			else if (MODE_MACRO == currentMode) {
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
		else if (MACRO.equals(cmd) && MODE_DEFAULT == currentMode)
			currentMode = MODE_MACRO;
		else if (META.equals(cmd) && MODE_DEFAULT == currentMode)
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
		if (INSTRUMENTS.equals(cmd) || MACRO.equals(cmd) || META.equals(cmd) || END.equals(cmd)) {
			if (nestableBlkDepth > 0) {
				throw new ParseException( Dict.get(Dict.ERROR_BLOCK_UNMATCHED_OPEN) );
			}
		}
		else if (BLOCK_OPEN.equals(cmd) || BLOCK_CLOSE.equals(cmd)) {
			if (MODE_INSTRUMENTS == currentMode) {
				throw new ParseException( Dict.get(Dict.ERROR_NOT_ALLOWED_IN_INSTR_BLK) + cmd );
			}
			if (MODE_META == currentMode) {
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
		String[] summands = s.split(Pattern.quote(DURATION_PLUS), -1);
		int      duration = 0;
		for (String summand : summands) {
			if ("".equals(summand))
				throw new ParseException( Dict.get(Dict.ERROR_EMPTY_DURATION_SUMMAND) + s );
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
	 * - instruments
	 * - macro
	 * - meta
	 * - end
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
		else if (MACRO.equals(cmd)) {
			parseMACRO(tokens);
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
	 * - define
	 * - chord
	 * - include_file
	 * - soundfont
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
		else if (INCLUDE_FILE.equals(cmd) || ORIGINAL_INCLUDE_FILE.equals(cmd)) {
			parseINCLUDE_FILE(tokens);
		}
		else if (SOUNDFONT.equals(cmd)) {
			parseSOUNDFONT(tokens);
		}
		else if (DEFINE.equals(cmd) || ORIGINAL_DEFINE.equals(cmd)) {
			parseDEFINE(tokens);
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
			currentMode      = MODE_DEFAULT;
			currentMacroName = null;
		}
		else
			throw new ParseException( Dict.get(Dict.ERROR_ARGS_NOT_ALLOWED) );
	}
	
	/**
	 * Parses a MACRO command.
	 * A MACRO command is a mode command that opens a MACRO definition.
	 * 
	 * @param tokens             Token array.
	 * @throws ParseException    If the command cannot be parsed.
	 */
	private void parseMACRO(String[] tokens) throws ParseException {
		if (MODE_DEFAULT != currentMode) {
			throw new ParseException( Dict.get(Dict.ERROR_MACRO_NOT_ALLOWED_HERE) );
		}
		if (2 == tokens.length) {
			currentMode      = MODE_MACRO;
			currentMacroName = tokens[1];
			if (macros.containsKey(currentMacroName)) {
				throw new ParseException( Dict.get(Dict.ERROR_MACRO_ALREADY_DEFINED) + currentMacroName );
			}
			
			// only collect the macro name?
			if (isMacrNameParsRun) {
				definedMacroNames.add(currentMacroName);
				return;
			}
			
			// real macro parsing run
			currentMacro = new ArrayList<String>();
			macros.put(currentMacroName, currentMacro);
		}
		else
			throw new ParseException( Dict.get(Dict.ERROR_MACRO_NUM_OF_ARGS) );
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
	 * Parses opening or closing a nestable block.
	 * By default, that is **(** or **)**.
	 * 
	 * @param tokens             Token array.
	 * @param isFake             **true**, if this is called inside a macro definition or block.
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
		int     quantity = -1;
		boolean multiple = false;
		String  tuplet   = null;
		if (optionsStr.length() > 0) {
			ArrayList<CommandOption> options = parseOptions(optionsStr);
			for (CommandOption opt : options) {
				String optName = opt.getName();
				if (OPT_QUANTITY.equals(optName)) {
					quantity = opt.getQuantity();
				}
				else if (OPT_MULTIPLE.equals(optName)) {
					multiple = true;
				}
				else if (OPT_TUPLET.equals(optName)) {
					tuplet = opt.getTuplet();
				}
				else {
					throw new ParseException( Dict.get(Dict.ERROR_BLOCK_INVALID_ARG) + optName );
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
		if (quantity != -1)
			block.setQuantity(quantity, OPT_QUANTITY);
		if (tuplet != null)
			block.setTuplet(tuplet, OPT_TUPLET);
		
		// apply root block
		if (BLOCK_CLOSE.equals(cmd) && nestableBlkDepth == 0) {
			block.applyTuplets(null);
			block.play();
		}
	}
	
	/**
	 * Parses a CHORD command.
	 * A CHORD command defines which a new chord name and describes the included notes.
	 * 
	 * @param tokens             Token array.
	 * @throws ParseException    If the command cannot be parsed.
	 */
	private void parseCHORD( String[] tokens ) throws ParseException {
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
		String[] chordParts = chordDef.split( "[" + Pattern.quote(OPT_ASSIGNER) + "\\s]+", 2 ); // chord name and chords can be separated by OPT_ASSIGNER (e,g, "=") and/or whitespace(s)
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
		HashSet<Integer> chord = new HashSet<Integer>();
		String[] notes = chordValue.split( "[" + OPT_SEPARATOR + "\\s]+" ); // notes of the chord can be separated by OPT_ASSIGNER (e,g, "=") and/or whitespace(s)
		
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
	 * Parses an INCLUDE command.
	 * An INCLUDE command calls a previously defined MACRO.
	 * 
	 * @param tokens             Token array.
	 * @param isFake             **true**, if this is called inside a macro definition or block
	 * @throws ParseException    If the command cannot be parsed.
	 */
	private void parseINCLUDE(String[] tokens, boolean isFake) throws ParseException {
		
		// get options (quantity / multiple)
		int     quantity = 1;
		boolean multiple = false;
		if (3 == tokens.length) {
			ArrayList<CommandOption> options = parseOptions(tokens[2]);
			for (CommandOption opt : options) {
				String optName = opt.getName();
				if (OPT_QUANTITY.equals(optName)) {
					quantity = opt.getQuantity();
				}
				else if (OPT_MULTIPLE.equals(optName)) {
					multiple = true;
				}
				else {
					throw new ParseException( Dict.get(Dict.ERROR_INCLUDE_UNKNOWN_ARG) + optName );
				}
			}
		}
		
		if (tokens.length > 1) {
			String includeName = tokens[1];
			if (includeName.equals(currentMacroName)) {
				throw new ParseException( Dict.get(Dict.ERROR_MACRO_RECURSION) );
			}
			if (! definedMacroNames.contains(includeName)) {
				throw new ParseException( Dict.get(Dict.ERROR_MACRO_UNDEFINED) );
			}
			
			// only fake?
			if (isFake) {
				return;
			}
			
			// remember current tickstamps if needed (included from outside of any macro)
			ArrayList<Long> tickstamps = rememberTickstamps();
			
			// fetch the right macro
			ArrayList<String> macro = macros.get(includeName);
			
			// apply all lines of the called macro
			for (int m=0; m<quantity; m++) {
				for (int i=0; i<macro.size(); i++) {
					String macroLine = macro.get(i);
					parseLine(macroLine);
				}
			}
			
			// restore tickstamps, if needed (included from outside of any macro)
			if (multiple) {
				restoreTickstamps(tickstamps);
			}
		}
		else {
			throw new ParseException( Dict.get(Dict.ERROR_INCLUDE_NUM_OF_ARGS) );
		}
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
				// create File object with absolute path for include file
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
		
		// split definition string by OPT_ASSIGNER (e,g, "=") and/or whitespace(s)
		String[] defParts = def.split( "[" + Pattern.quote(OPT_ASSIGNER) + "\\s]+", 2 );
		if (defParts.length < 2)
			throw new ParseException( Dict.get(Dict.ERROR_DEFINE_NUM_OF_ARGS) );
		String cmdId      = clean( defParts[0] );
		String cmdName    = clean( defParts[1] );
		if (cmdName.matches("\\s"))
			throw new ParseException( Dict.get(Dict.ERROR_DEFINE_NUM_OF_ARGS) );
		
		if (isDefaultParsRun) {
			return;
		}
		
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
		else if ( Dict.SYNTAX_INLINE_CHORD_SEP.equals(cmdId)   ) INLINE_CHORD_SEP   = cmdName;
		else if ( Dict.SYNTAX_COMMENT.equals(cmdId)            ) COMMENT            = cmdName;
		else if ( Dict.SYNTAX_DEFINE.equals(cmdId)             ) DEFINE             = cmdName;
		else if ( Dict.SYNTAX_END.equals(cmdId)                ) END                = cmdName;
		else if ( Dict.SYNTAX_BLOCK_OPEN.equals(cmdId)         ) BLOCK_OPEN         = cmdName;
		else if ( Dict.SYNTAX_BLOCK_CLOSE.equals(cmdId)        ) BLOCK_CLOSE        = cmdName;
		else if ( Dict.SYNTAX_GLOBAL.equals(cmdId)             ) GLOBAL             = cmdName;
		else if ( Dict.SYNTAX_DOT.equals(cmdId)                ) DOT                = cmdName;
		else if ( Dict.SYNTAX_INCLUDE.equals(cmdId)            ) INCLUDE            = cmdName;
		else if ( Dict.SYNTAX_INCLUDE_FILE.equals(cmdId)       ) INCLUDE_FILE       = cmdName;
		else if ( Dict.SYNTAX_SOUNDFONT.equals(cmdId)          ) SOUNDFONT          = cmdName;
		else if ( Dict.SYNTAX_INSTRUMENTS.equals(cmdId)        ) INSTRUMENTS        = cmdName;
		else if ( Dict.SYNTAX_META.equals(cmdId)               ) META               = cmdName;
		else if ( Dict.SYNTAX_META_COPYRIGHT.equals(cmdId)     ) META_COPYRIGHT     = cmdName;
		else if ( Dict.SYNTAX_META_TITLE.equals(cmdId)         ) META_TITLE         = cmdName;
		else if ( Dict.SYNTAX_META_COMPOSER.equals(cmdId)      ) META_COMPOSER      = cmdName;
		else if ( Dict.SYNTAX_META_LYRICIST.equals(cmdId)      ) META_LYRICIST      = cmdName;
		else if ( Dict.SYNTAX_META_ARTIST.equals(cmdId)        ) META_ARTIST        = cmdName;
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
		else if ( Dict.SYNTAX_MACRO.equals(cmdId)              ) MACRO              = cmdName;
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
		else if ( Dict.SYNTAX_T.equals(cmdId)                  ) T                  = cmdName;
		else if ( Dict.SYNTAX_TUPLET.equals(cmdId)             ) TUPLET             = cmdName;
		else if ( Dict.SYNTAX_V.equals(cmdId)                  ) V                  = cmdName;
		else if ( Dict.SYNTAX_VELOCITY.equals(cmdId)           ) VELOCITY           = cmdName;
		else if ( Dict.SYNTAX_TRIPLET.equals(cmdId)            ) TRIPLET            = cmdName;
		else if ( Dict.SYNTAX_TUPLET_INTRO.equals(cmdId)       ) TUPLET_INTRO       = cmdName;
		else if ( Dict.SYNTAX_TUPLET_FOR.equals(cmdId)         ) TUPLET_FOR         = cmdName;
		else if ( Dict.SYNTAX_DURATION_PLUS.equals(cmdId)      ) DURATION_PLUS      = cmdName;
		else {
			throw new ParseException( Dict.get(Dict.ERROR_UNKNOWN_COMMAND_ID) + cmdId );
		}
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
	private void parseInstrumentCmd(String[] tokens) throws ParseException {
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
	 * Parses a global command.
	 * Global commands apply to every channel. So they always contain a syncronization
	 * of the current ticks of each channel.
	 * 
	 * @param tokens             Token array.
	 * @param isFake             **true**, if this is called inside a macro definition or block
	 * @throws ParseException    If the command cannot be parsed.
	 */
	private void parseGlobalCmd(String[] tokens, boolean isFake) throws ParseException {
		
		// allow global commands in drum-only sequences without an INSTRUMENTS block
		if (!instrumentsParsed) {
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
	 * A channel command is a channel-based command like a note or a pause.
	 * 
	 * @param tokens             Token array.
	 * @param isFake             **true**, if this is called inside a macro definition or block
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
		int duration = parseDuration(durationStr);
		
		// process options
		boolean multiple = false;
		int     quantity = 1;
		String  syllable = null;
		if (2 == subTokens.length) {
			ArrayList<CommandOption> options = parseOptions(subTokens[1]);
			
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
				}
			}
		}
		
		// allow drum-only sequences without an INSTRUMENTS block
		if (!instrumentsParsed) {
			postprocessInstruments();
		}
		
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
			NOTE_QUANTITY:
			for (int i = 0; i < quantity; i++) {
				long startTicks = instr.getCurrentTicks();
				if (! isFake && syllable != null) {
					syllable = syllable.replaceAll(Pattern.quote(LYRICS_SPACE), " ");
					syllable = syllable.replaceAll(Pattern.quote(LYRICS_CR), "\r");
					syllable = syllable.replaceAll(Pattern.quote(LYRICS_LF), "\n");
					SequenceCreator.addMessageLyrics(syllable, startTicks);
				}
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
					// get end ticks of the current note
					long endTicks = instr.addNote( duration );
					
					// create and add messages
					int newNote = transpose( note, channel );
					SequenceCreator.addMessageKeystroke( channel, newNote, startTicks, endTicks, velocity );
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
	 * Parses the options part of a channel command.
	 * 
	 * @param optString    The options string of the channel command to be parsed.
	 * @return             All options and their values that have been found in the
	 *                     provided options string.
	 * @throws ParseException    If the command cannot be parsed.
	 */
	private ArrayList<CommandOption> parseOptions(String optString) throws ParseException {
		ArrayList<CommandOption> options = new ArrayList<CommandOption>();
		
		String[] optTokens = optString.split( OPT_SEPARATOR );
		for (String opt : optTokens) {
			opt = clean( opt );
			String[] optParts = opt.split("[" + Pattern.quote(OPT_ASSIGNER) + "\\s]+"); // name and value can be separated by OPT_ASSIGNER (e,g, "=") and/or whitespace(s)
			if (optParts.length > 2)
				throw new ParseException( Dict.get(Dict.ERROR_CANT_PARSE_OPTIONS) );
			// construct name and value
			String        optName  = optParts[0];
			CommandOption optValue = new CommandOption();
			if (V.equals(optName) || VELOCITY.equals(optName)) {
				if (optParts.length < 2) {
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
				if (optParts.length < 2) {
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
				optName = OPT_QUANTITY;
				optValue.set( optName, toInt(optParts[1], false) );
			}
			else if (M.equals(optName) || MULTIPLE.equals(optName)) {
				optName = OPT_MULTIPLE;
				optValue.set(optName, true);
			}
			else if (L.equals(optName) || LYRICS.equals(optName)) {
				optName = OPT_LYRICS;
				optValue.set( optName, optParts[1] );
			}
			else if (T.equals(optName) || TUPLET.equals(optName)) {
				optName = OPT_TUPLET;
				Pattern pattern = Pattern.compile(
					  "^(?:"                       // open non-capturing group for all tuplets and triplets
					+ Pattern.quote(TUPLET_INTRO)  //   customized tuplet
					+ "\\d+"                       //     first number of the customized tuplet
					+ Pattern.quote(TUPLET_FOR)    //     separator between the 2 numbers
					+ "\\d+"                       //     second number of the customized tuplet
					+ "|" + Pattern.quote(TRIPLET) //   ordinary triplet
					+ ")+"                         // close non-capturing group
					+ "$"
				);
				if (pattern.matcher(optParts[1]).matches()) {
					optValue.set( optName, optParts[1] );
				}
				else {
					throw new ParseException( Dict.get(Dict.ERROR_TUPLET_INVALID) + optParts[1] );
				}
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
	private int parseNote( String note, int channel ) throws ParseException {
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
	 * @return a collection of all included notes, of **null**, if the token contains only a single note.
	 */
	private HashSet<String> parseChord(String token) {
		if (token.matches(".*" + Pattern.quote(INLINE_CHORD_SEP) + ".*") || chords.containsKey(token)) {
			HashSet<String> chordElements = new HashSet<String>();
			
			// collect comma-separated inline chord parts
			String[] inlineElements = token.split( Pattern.quote(INLINE_CHORD_SEP) );
			for (String inlineElement : inlineElements) {
				
				// collect predefined chord elements
				if (chords.containsKey(inlineElement)) {
					for (int note : chords.get(inlineElement)) {
						chordElements.add(Integer.toString(note));
					}
				}
				else {
					// collect simple note
					chordElements.add(inlineElement);
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
		HashSet<String> chordElements = parseChord(tokens[1]);
		if (null == chordElements) {
			
			// not a chord
			return false;
		}
		
		// call this method again with each chord, adding the multiple option
		int i = 1;
		for (String chordElement : chordElements) {
			// create and process a new token array for each note of the chord
			String[] subTokens = new String[3];
			subTokens[0] = tokens[0];    // same channel
			subTokens[1] = chordElement; // note name or number
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
		HashSet<Integer> channels = new HashSet<Integer>();
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
		ArrayList<Instrument> partialInstruments = new ArrayList<Instrument>();
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
	 * @throws ParseException    If something went wrong.
	 */
	private void postprocessInstruments() throws ParseException {
		
		// sort instruments ascending
		Collections.sort( instruments );
		
		// find out which channels are missing
		HashSet<Integer> missing = new HashSet<Integer>();
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
			SequenceCreator.reset( chosenCharset );
			for (Instrument instr : instruments) {
				int    channel      = instr.channel;
				int    instrNum     = instr.instrumentNumber;
				String instrComment = instr.instrumentName;
				int    bankMSB      = instr.getBankMSB();
				int    bankLSB      = instr.getBankLSB();
				// reset instrument
				instr.resetCurrentTicks();
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
	 * @throws ParseException    If something went wrong.
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
			SequenceCreator.addMessageLyrics(rp26.toString(), 0);
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
			instrumentsParsed   = false;
			metaInfo            = new HashMap<String, StringBuilder>();
			frstInstrBlkOver    = false;
			isDefineParsRun     = false;
			isChInstMetaParsRun = false;
			isMacrNameParsRun   = false;
			isMacroParsRun      = false;
			isDefaultParsRun    = false;
			instruments         = new ArrayList<Instrument>();
			definedMacroNames   = new HashSet<String>();
			macros              = new HashMap<String, ArrayList<String>>();
			chords              = new HashMap<String, HashSet<Integer>>();
			nestableBlkDepth    = 0;
			nestableBlkStack    = new ArrayDeque<NestableBlock>();
			redefinitions       = new HashSet<String>();
			soundfontParsed     = false;
			refreshSyntax();
		}
	}
}
