/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file.read;

import java.io.File;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;

import org.midica.config.Config;
import org.midica.config.Dict;
import org.midica.file.Foreign;
import org.midica.file.ForeignException;
import org.midica.midi.SequenceCreator;

/**
 * This class is used to import an ALDA file using the alda executable.
 * This works only if ALDA is installed.
 * 
 * The process contains the following steps:
 * 
 * - Checks which ALDA version is used
 * - If ALDA 1 is used, starts the ALDA server, if not yet done
 * - Convert ALDA to a MIDI tempfile, using the alda executable
 * - Parse the MIDI file using the parent class
 * - Delete the MIDI file
 * 
 * @author Jan Trukenmüller
 */
public class AldaImporter extends MidiParser {
	
	// how often shall we sleep to wait for ALDA to create the MIDI file?
	private static final int MAX_SLEEP_COUNT = 100;
	
	// how long shall we sleep each time (in milliseconds)
	private static final int SLEEP_TIME = 200;
	
	// foreign program description for error messages
	private static String programName = Dict.get(Dict.FOREIGN_PROG_ALDA);
	
	@Override
	protected int getImportFormat() {
		return SequenceCreator.IMPORT_FORMAT_ALDA;
	}
	
	/**
	 * Parses an ALDA file.
	 * 
	 * @param fileAsObj  ALDA file to be parsed.
	 */
	@Override
	public void parse(Object fileAsObj) throws ParseException {
		File file = (File) fileAsObj;
		
		// reset file name and file type
		preprocess(file);
		midiFileCharset = null;
		chosenCharset   = "US-ASCII";
		
		try {
			String execPath = Config.get(Config.EXEC_PATH_IMP_ALDA);
			
			// get alda version
			int version = -1;
			String[] aldaVersion = {execPath, "version"};
			Foreign.execute(aldaVersion, execPath, true);
			String versionStr = Foreign.getLastOutput();
			if (versionStr != null) {
				versionStr = versionStr.toLowerCase();
				if (versionStr.startsWith("alda 2")) {
					version = 2;
				}
				else if (versionStr.startsWith("client version: 1")) {
					version = 1;
				}
			}
			
			// alda up
			if (1 == version) {
				String[] aldaUp = {execPath, "up"};
				Foreign.execute(aldaUp, programName, true);
			}
			
			// get a temp file path
			File tempfile = Foreign.createTempMidiFile();
			Foreign.deleteTempFile(tempfile);
			
			// convert from the ALDA file to the tempfile
			String[] aldaConvert = {execPath, "export", "-f", file.getAbsolutePath(), "-o", tempfile.getAbsolutePath()};
			Foreign.execute(aldaConvert, programName, false);
			
			// Due to a bug in alda 1, sometimes the exit code was successul even if
			// no MIDI file was created.
			// I don't know if that bug still exists in alda 2 but this workaround doesn't
			// hurt either.
			if (!tempfile.exists()) {
				throw new ParseException(Dict.get(Dict.ERROR_ALDA_NO_MIDI_FILE));
			}
			
			// sometimes alda 2 needs a little more time to fill the MIDI file
			if (0L == tempfile.length()) {
				for (int i = 0; i < MAX_SLEEP_COUNT; i++) {
					Thread.sleep(SLEEP_TIME);
					
					// file not empty any more?
					if (tempfile.length() > 0L)
						break;
				}
				
				// file still empty?
				if (0L == tempfile.length())
					throw new ParseException(Dict.get(Dict.ERROR_ALDA_MIDI_FILE_EMPTY));
			}
			
			// get MIDI from tempfile
			Sequence sequence = MidiSystem.getSequence(tempfile);
			
			// delete tempfile
			Foreign.deleteTempFile(tempfile);
			
			// transform and analyze the sequence
			createSequence(sequence);
			postprocessSequence(sequence, chosenCharset); // analyze the original sequence
		}
		catch (ForeignException | InvalidMidiDataException | IOException | InterruptedException e) {
			throw new ParseException(e.getMessage());
		}
	}
}
