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

/**
 * This class is used to import an ABC file using the abc2midi executable (belongs to abcMIDI).
 * https://ifdo.ca/~seymour/runabc/top.html
 * This works only if abc2midi is available.
 * 
 * The process contains the following steps:
 * 
 * - Convert ABC to a MIDI tempfile, using abc2midi
 * - Parse the MIDI file using the parent class
 * - Delete the MIDI file
 * 
 * @author Jan Trukenmüller
 */
public class AbcImporter extends MidiParser {
	
	// foreign program description for error messages
	private static String programName = Dict.get(Dict.FOREIGN_PROG_ABCMIDI);
	
	/**
	 * Returns the absolute path of the successfully parsed ABC file.
	 * Returns **null**, if no file has been successfully parsed or the successfully parsed file
	 * is not an ABC file.
	 * 
	 * @return file path or **null**.
	 */
	public static String getFilePath() {
		return getFilePath(FORMAT_ABC);
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
		chosenCharset   = "US-ASCII"; // TODO: test this - maybe we need to use an actual file chooser
		
		try {
			String execPath = Config.get(Config.EXEC_PATH_IMP_ABC);
			
			// create temp midi file
			File tempfile = Foreign.createTempMidiFile();
			
			// convert from the ABC file to the tempfile
			String[] abc2midi = {execPath, file.getAbsolutePath(), "-o", tempfile.getAbsolutePath()};
			Foreign.execute(abc2midi, programName, false);
			
			// get MIDI from tempfile
			Sequence sequence = MidiSystem.getSequence(tempfile);
			
			// delete tempfile
			Foreign.deleteTempFile(tempfile);
			
			// transform and analyze the sequence
			createSequence(sequence);
			postprocessSequence(sequence, FORMAT_ABC, chosenCharset); // analyze the original sequence
		}
		catch (ForeignException | InvalidMidiDataException | IOException e) {
			throw new ParseException(e.getMessage());
		}
	}
}
