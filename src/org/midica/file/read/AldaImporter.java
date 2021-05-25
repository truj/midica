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
 * - Start the ALDA server, if not yet done
 * - Convert ALDA to a MIDI tempfile, using the alda executable
 * - Parse the MIDI file using the parent class
 * - Delete the MIDI file
 * 
 * @author Jan Trukenmüller
 */
public class AldaImporter extends MidiParser {
	
	// foreign program description for error messages
	private static String programName = Dict.get(Dict.FOREIGN_PROG_ALDA);
	
	@Override
	protected int getImportFormat() {
		return SequenceCreator.IMPORT_FORMAT_ALDA;
	}
	
	/**
	 * Parses an ALDA file.
	 * 
	 * @param file  ALDA file to be parsed.
	 */
	public void parse(File file) throws ParseException {
		
		// reset file name and file type
		preprocess(file);
		midiFileCharset = null;
		chosenCharset   = "US-ASCII";
		
		try {
			String execPath = Config.get(Config.EXEC_PATH_IMP_ALDA);
			
			// alda up
			String[] aldaUp = {execPath, "up"};
			Foreign.execute(aldaUp, programName, true);
			
			// get a temp file path
			File tempfile = Foreign.createTempMidiFile();
			Foreign.deleteTempFile(tempfile);
			
			// convert from the ALDA file to the tempfile
			String[] aldaConvert = {execPath, "export", "-f", file.getAbsolutePath(), "-o", tempfile.getAbsolutePath()};
			Foreign.execute(aldaConvert, programName, false);
			
			// due to an ALDA bug sometimes the exit code is successul even if no MIDI file was created
			if (!tempfile.exists()) {
				throw new ParseException(Dict.get(Dict.ERROR_ALDA_NO_MIDI_FILE));
			}
			
			// get MIDI from tempfile
			Sequence sequence = MidiSystem.getSequence(tempfile);
			
			// delete tempfile
			Foreign.deleteTempFile(tempfile);
			
			// transform and analyze the sequence
			createSequence(sequence);
			postprocessSequence(sequence, chosenCharset); // analyze the original sequence
		}
		catch (ForeignException | InvalidMidiDataException | IOException e) {
			throw new ParseException(e.getMessage());
		}
	}
}
