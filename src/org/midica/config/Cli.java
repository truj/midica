/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.config;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.midica.file.read.SoundfontParser;
import org.midica.file.write.AudioExporter;
import org.midica.midi.MidiDevices;
import org.midica.ui.UiController;
import org.midica.ui.file.FileSelector;
import org.midica.ui.file.SoundUrlHelper;

/**
 * This class handles command line options.
 * 
 * @author Jan Trukenmüller
 */
public class Cli {
	
	/** CLI mode (command line interface) without GUI - e.g. for unit tests */
	public static boolean isCliMode = false;
	
	/** Determins if the local config file shall be used or not. */
	public static boolean useLocalConfig = true;
	
	/** Determins if the process shall stay alive (or exit) after all cli-related work is done. */
	public static boolean keepAlive = true;
	
	// import/export related fields
	public  static boolean useSoundbank     = false;
	public  static boolean isImport         = false;
	public  static boolean isExport         = false;
	public  static boolean exportToStdout   = false;
	public  static String  exportErrorMsg   = null;
	private static String  soundbankPath    = null;
	private static String  soundbankUrl     = null;
	private static String  soundbankFormat  = null;
	private static String  importPathMpl    = null;
	private static String  importPathMidi   = null;
	private static String  importPathAlda   = null;
	private static String  importPathAbc    = null;
	private static String  importPathLy     = null;
	private static String  importPathMscore = null;
	private static String  exportPathMpl    = null;
	private static String  exportPathMidi   = null;
	private static String  exportPathAlda   = null;
	private static String  exportPathAudio  = null;
	private static String  exportPathAbc    = null;
	private static String  exportPathLy     = null;
	private static String  exportPathMscore = null;
	
	/**
	 * This class is only used statically so a public constructor is not needed.
	 */
	private Cli() {
	}
	
	/**
	 * Parses command line arguments.
	 * 
	 * @param args command line arguments.
	 */
	public static void parseArguments(String[] args) {
		Pattern patImport      = Pattern.compile("^\\-\\-(import|import\\-.+?)=(.+)$");
		Pattern patExport      = Pattern.compile("^\\-\\-(export|export\\-.+?)=(.+)$");
		Pattern patSoundbank   = Pattern.compile("^\\-\\-(sound\\-path)=(.+)$");
		Pattern patSoundUrl    = Pattern.compile("^\\-\\-(sound\\-url\\-.+?)=(.+)$");
		Pattern patInvalidPath = Pattern.compile("^\\-\\-((im|ex)port(\\-[\\w-]+?)|sound[\\w\\-]+?)(=|$)$");
		
		for (String arg : args) {
			if ("--cli".equals(arg)) {
				isCliMode = true;
				keepAlive = false;
			}
			else if ("--keep-alive".equals(arg)) {
				keepAlive = true;
			}
			else if ("--ignore-local-config".equals(arg)) {
				useLocalConfig = false;
			}
			else if ("--help".equals(arg)) {
				help(true, null);
			}
			else if (patSoundbank.matcher(arg).matches()) {
				if (useSoundbank) {
					help(false, "More than one soundbank is not allowed!");
				}
				else {
					useSoundbank = true;
					Matcher m = patSoundbank.matcher(arg);
					m.matches();
					soundbankPath = m.group(2);
					if ("-".equals(soundbankPath)) {
						help(false, arg + " not possible. Use a real PATH.");
					}
				}
			}
			else if (patSoundUrl.matcher(arg).matches()) {
				if (useSoundbank) {
					help(false, "More than one soundbank is not allowed!");
				}
				else {
					useSoundbank = true;
					Matcher m = patSoundUrl.matcher(arg);
					m.matches();
					String format = m.group(1);
					soundbankUrl  = m.group(2);
					
					if ("sound-url-sf2".equals(format))
						soundbankFormat = SoundfontParser.SOUND_FORMAT_SF2;
					else if ("sound-url-dls".equals(format))
						soundbankFormat = SoundfontParser.SOUND_FORMAT_DLS;
					else
						help(false, "Unknown soundbank format: --" + format);
				}
			}
			else if (patImport.matcher(arg).matches()) {
				if (isImport) {
					help(false, "More than one import file is not allowed!");
				}
				else {
					isImport = true;
					Matcher m = patImport.matcher(arg);
					m.matches();
					String option = m.group(1);
					String path   = m.group(2);
					if ("-".equals(path)) {
						help(false, arg + " not possible. Use a real PATH.");
					}
					if ("import".equals(option)) {
						importPathMpl = path;
					}
					else if ("import-midi".equals(option)) {
						importPathMidi = path;
					}
					else if ("import-alda".equals(option)) {
						importPathAlda = path;
					}
					else if ("import-abc".equals(option)) {
						importPathAbc = path;
					}
					else if ("import-ly".equals(option)) {
						importPathLy = path;
					}
					else if ("import-mscore".equals(option)) {
						importPathMscore = path;
					}
					else {
						help(false, "Unknown import format: --" + option);
					}
				}
			}
			else if (patExport.matcher(arg).matches()) {
				if (isExport) {
					help(false, "More than one export file is not allowed!");
				}
				else {
					Matcher m = patExport.matcher(arg);
					m.matches();
					isExport = true;
					String  option   = m.group(1);
					String  path     = m.group(2);
					boolean stdoutOk = false;
					if ("export".equals(option)) {
						exportPathMpl = path;
						stdoutOk      = true;
					}
					else if ("export-midi".equals(option)) {
						exportPathMidi = path;
					}
					else if ("export-alda".equals(option)) {
						exportPathAlda = path;
						stdoutOk       = true;
					}
					else if ("export-audio".equals(option)) {
						exportPathAudio = path;
					}
					else if ("export-abc".equals(option)) {
						exportPathAbc = path;
						stdoutOk      = true;
					}
					else if ("export-ly".equals(option)) {
						exportPathLy = path;
						stdoutOk     = true;
					}
					else if ("export-mscore".equals(option)) {
						exportPathMscore = path;
						stdoutOk         = true;
					}
					else {
						help(false, "Unknown export format: --" + option);
					}
					if ("-".equals(path) && ! stdoutOk) {
						help(false, arg + " not possible. Use a real PATH.");
					}
				}
			}
			else if (patInvalidPath.matcher(arg).matches()) {
				Matcher m = patInvalidPath.matcher(arg);
				m.matches();
				String option = m.group(1);
				String rest   = m.group(4);
				if ("".equals(rest)) {
					help(false, "'=' missing. Try: --" + option + "=PATH");
				}
				else if ("=".equals(rest)) {
					help(false, "Path missing. Try: --" + option + "=PATH");
				}
				else {
					help(false, "Wrong usage of parameter --" + option);
				}
			}
			else {
				help(false, "Unknown argument: " + arg);
			}
		}
		
		if (isCliMode && ! isImport && ! keepAlive) {
			help(true, "Nothing to do.");
		}
		
		// export without import is forbidden
		if (isExport && !isImport) {
			help(false, "Export without import is not possible!");
		}
	}
	
	/**
	 * Prints a hepl message and exits.
	 * The exit code is 0, if called with `--help`.
	 * Otherwise the parameters are regarded as erroneous and the exit code is 64.
	 * 
	 * @param isHelpRequested    **true**, if called with `--help` on the command line
	 * @param message            error message
	 */
	private static void help(boolean isHelpRequested, String message) {
		
		StringBuilder msg = new StringBuilder("");
		if (message != null) {
			msg.append("==============================================================================\n");
			if (!isHelpRequested) {
				msg.append("Error: \n");
			}
			msg.append(message + "\n");
			msg.append("==============================================================================\n");
		}
		msg.append("ARGUMENTS:\n");
		msg.append("--help                : Print this message.\n");
		msg.append("--cli                 : Run in CLI mode (command line interface) without GUI.\n");
		msg.append("                        Exits after all CLI related work is done.\n");
		msg.append("--keep-alive          : Don't exit, even if --cli has been used.\n");
		msg.append("                        Mainly used for unit tests.\n");
		msg.append("--ignore-local-config : Doesn't use local config file. Use default config.\n");
		msg.append("                        Without this argument the config is read from and\n");
		msg.append("                        written into the file '.midica.conf' in the current\n");
		msg.append("                        user's home directory.\n");
		msg.append("--sound-path=PATH     : Use the specified soundbank file (.sf2 or .dls).\n");
		msg.append("--sound-url-sf2=URL   : Use the specified SF2 soundfont URL.\n");
		msg.append("--sound-url-dls=URL   : Use the specified DLS soundbank URL.\n");
		msg.append("--import=PATH         : Import from the specified MidicaPL file.\n");
		msg.append("--import-midi=PATH    : Import from the specified MIDI file.\n");
		msg.append("--import-alda=PATH    : Import from the specified ALDA file by calling the\n");
		msg.append("                        alda program. (ALDA needs to be installed.)\n");
		msg.append("--import-abc=PATH     : Import from the specified ABC file by calling\n");
		msg.append("                        abc2midi. (abcMIDI needs to be installed.)\n");
		msg.append("--import-ly=PATH      : Import from the specified LilyPond file by calling\n");
		msg.append("                        lilypond. (LilyPond needs to be installed.)\n");
		msg.append("--import-mscore=PATH  : Import from the specified file using MuseScore\n");
		msg.append("                        (MuseScore needs to be installed.)\n");
		msg.append("--export-midi=PATH    : Export to the specified MIDI file.\n");
		msg.append("--export=PATH         : Export to the specified MidicaPL file. (*)\n");
		msg.append("--export-alda=PATH    : Export to the specified ALDA file. (*)\n");
		msg.append("--export-audio=PATH   : Export to the specified audio file.\n");
		msg.append("                        (Supported file Extensions: "
			+ AudioExporter.getSupportedFileExtensions() + ")\n");
		msg.append("--export-abc=PATH     : Export to the specified ABC file by calling\n");
		msg.append("                        midi2abc. (abcMIDI needs to be installed.)\n");
		msg.append("--export-ly=PATH      : Export to the specified Lilypond file by calling\n");
		msg.append("                        midi2ly. (Lilypond needs to be installed.)\n");
		msg.append("--export-mscore=PATH  : Export to the specified file using MuseScore.\n");
		msg.append("                        (MuseScore needs to be installed.)\n");
		msg.append("\n");
		msg.append("(*) A file is exported to STDOUT if the export PATH is a dash (-).\n");
		msg.append("    E.g. --export=-");
		msg.append("\n");
		
		if (isHelpRequested) {
			System.out.println(msg);
			System.exit(0);
		}
		else {
			System.err.println(msg);
			System.exit(64);
		}
	}
	
	/**
	 * Loads a soundbank file or URL due to a command line argument.
	 * 
	 * @param uiController    the UI controller
	 */
	public static void loadSoundbank(UiController uiController) {
		Object fileOrUrl        = null;
		String fileSelectorType = null;
		
		// file?
		if (soundbankPath != null) {
			fileOrUrl        = new File(soundbankPath);
			fileSelectorType = FileSelector.FILE_TYPE_SOUND_FILE;
		}
		else {
			// url
			fileOrUrl        = soundbankUrl;
			fileSelectorType = FileSelector.FILE_TYPE_SOUND_URL;
			SoundUrlHelper.setSoundUrlFormat(soundbankFormat);
		}
		uiController.parseChosenFile(fileSelectorType, fileOrUrl);
		
		// loading failed?
		if (null == SoundfontParser.getFullPath()) {
			String pathOrUrl = soundbankPath != null ? soundbankPath : soundbankUrl;
			help(false, "Failed to load Soundbank: " + pathOrUrl);
		}
	}
	
	/**
	 * Imports a file due to a command line argument.
	 * 
	 * @param uiController    the UI controller
	 */
	public static void importFile(UiController uiController) {
		String importPath;
		String importType;
		if (importPathMpl != null) {
			importPath = importPathMpl;
			importType = FileSelector.FILE_TYPE_MPL;
		}
		else if (importPathMidi != null) {
			importPath = importPathMidi;
			importType = FileSelector.FILE_TYPE_MIDI;
		}
		else if (importPathAlda != null) {
			importPath = importPathAlda;
			importType = FileSelector.FILE_TYPE_ALDA;
		}
		else if (importPathAbc != null) {
			importPath = importPathAbc;
			importType = FileSelector.FILE_TYPE_ABC;
		}
		else if (importPathLy != null) {
			importPath = importPathLy;
			importType = FileSelector.FILE_TYPE_LY;
		}
		else if (importPathMscore != null) {
			importPath = importPathMscore;
			importType = FileSelector.FILE_TYPE_MSCORE_IMP;
		}
		else {
			importPath = null;
			importType = null;
		}
		if (importPath != null && importType != null) {
			
			// import
			File importFile = new File(importPath);
			uiController.parseChosenFile(importType, importFile);
			
			// import failed?
			if ( ! MidiDevices.isSequenceSet() ) {
				help(false, "Failed to import from: " + importPath);
			}
		}
	}
	
	/**
	 * Imports a file due to a command line argument.
	 * 
	 * @param uiController    the UI controller
	 */
	public static void exportFile(UiController uiController) {
		String exportPath;
		String exportType;
		if (exportPathMpl != null) {
			exportPath     = exportPathMpl;
			exportType     = FileSelector.FILE_TYPE_MPL;
			exportToStdout = "-".equals(exportPath);
		}
		else if (exportPathMidi != null) {
			exportPath = exportPathMidi;
			exportType = FileSelector.FILE_TYPE_MIDI;
		}
		else if (exportPathAlda != null) {
			exportPath     = exportPathAlda;
			exportType     = FileSelector.FILE_TYPE_ALDA;
			exportToStdout = "-".equals(exportPath);
		}
		else if (exportPathAudio != null) {
			exportPath = exportPathAudio;
			exportType = FileSelector.FILE_TYPE_AUDIO;
		}
		else if (exportPathAbc != null) {
			exportPath = exportPathAbc;
			exportType = FileSelector.FILE_TYPE_ABC;
		}
		else if (exportPathLy != null) {
			exportPath = exportPathLy;
			exportType = FileSelector.FILE_TYPE_LY;
		}
		else if (exportPathMscore != null) {
			exportPath = exportPathMscore;
			exportType = FileSelector.FILE_TYPE_MSCORE_EXP;
		}
		else {
			exportPath = null;
			exportType = null;
		}
		if (exportPath != null && exportType != null) {
			File exportFile;
			if (exportToStdout)
				exportFile = null;
			else
				exportFile = new File(exportPath);
			
			// export
			uiController.exportChosenFile(exportType, exportFile);
			
			// export failed?
			if (exportErrorMsg != null) {
				help(false, "Failed to export to: " + exportPath + "\n" + exportErrorMsg);
			}
		}
	}
}
