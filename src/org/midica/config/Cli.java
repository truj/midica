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

import javax.swing.SwingUtilities;

import org.midica.midi.MidiDevices;
import org.midica.ui.UiController;
import org.midica.ui.file.FileSelector;

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
	public  static boolean isImport       = false;
	public  static boolean isExport       = false;
	public  static boolean exportToStdout = false;
	private static String  importPathMpl  = null;
	private static String  importPathMidi = null;
	private static String  exportPathMpl  = null;
	private static String  exportPathMidi = null;
	private static String  exportPathAlda = null;
	
	/**
	 * This class is only used statically so a public constructor is not needed.
	 */
	private Cli() {
	}
	
	/**
	 * Processes command line arguments.
	 * 
	 * @param args command line arguments.
	 */
	public static void processArguments(String[] args) {
		Pattern patternImport = Pattern.compile("^\\-\\-(import|import\\-.+?)=(.+)$");
		Pattern patternExport = Pattern.compile("^\\-\\-(export|export\\-.+?)=(.+)$");
		
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
			else if (patternImport.matcher(arg).matches()) {
				if (isImport) {
					help(false, "More than one import file is not allowed!");
				}
				else {
					isImport = true;
					Matcher m = patternImport.matcher(arg);
					m.matches();
					if ("import".equals(m.group(1))) {
						importPathMpl = m.group(2);
					}
					else if ("import-midi".equals(m.group(1))) {
						importPathMidi = m.group(2);
					}
					else {
						help(false, "Unknown import format: --" + m.group(1));
					}
				}
			}
			else if (patternExport.matcher(arg).matches()) {
				if (isExport) {
					help(false, "More than one export file is not allowed!");
				}
				else {
					Matcher m = patternExport.matcher(arg);
					m.matches();
					isExport = true;
					if ("export".equals(m.group(1))) {
						exportPathMpl = m.group(2);
					}
					else if ("export-midi".equals(m.group(1))) {
						exportPathMidi = m.group(2);
					}
					else if ("export-alda".equals(m.group(1))) {
						exportPathAlda = m.group(2);
					}
					else {
						help(false, "Unknown export format: --" + m.group(1));
					}
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
	 * Otherwise the parameters are regarded as erroneous and the exit code is 1.
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
		msg.append("Options:\n");
		msg.append("--help                : Print this message.\n");
		msg.append("--cli                 : Run in CLI mode (command line interface) without GUI.\n");
		msg.append("                        Exits after all CLI related work is done.\n");
		msg.append("--keep-alive          : Don't exit, even if --cli has been used.\n");
		msg.append("                        Mainly used for unit tests.\n");
		msg.append("--ignore-local-config : Doesn't use local config file. Use default config.\n");
		msg.append("--import=PATH         : Import from the specified MidicaPL file.\n");
		msg.append("--import-midi=PATH    : Import from the specified MIDI file.\n");
		msg.append("--export-midi=PATH    : Export to the specified MIDI file.\n");
		msg.append("--export=PATH         : Export to the specified MidicaPL file. (*)\n");
//		msg.append("--export-alda=PATH    : Export to the specified ALDA file. (*)\n");
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
	 * Imports a file due to a command line argument.
	 * 
	 * @param uiController    the UI controller
	 */
	public static void importFile(UiController uiController) {
		final String importPath;
		final String importType;
		if (importPathMpl != null) {
			importPath = importPathMpl;
			importType = FileSelector.FILE_TYPE_MPL;
		}
		else if (importPathMidi != null) {
			importPath = importPathMidi;
			importType = FileSelector.FILE_TYPE_MIDI;
		}
		else {
			importPath = null;
			importType = null;
		}
		if (importPath != null && importType != null) {
			File importFile = new File(importPath);
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					uiController.parseChosenFile(importType, importFile);
					
					// import failed?
					if ( ! MidiDevices.isSequenceSet() ) {
						help(false, "Import failed!");
					}
				}
			});
		}
	}
	
	/**
	 * Imports a file due to a command line argument.
	 * 
	 * @param uiController    the UI controller
	 */
	public static void exportFile(UiController uiController) {
		// TODO: implement
	}
}
