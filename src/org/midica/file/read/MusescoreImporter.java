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
 * This class is used to import a file using the MuseScore.
 * This works only if MuseScore is installed.
 * 
 * The process contains the following steps:
 * 
 * - Convert the file to a MIDI tempfile, using the MuseScore
 * - Parse the MIDI file using the parent class
 * - Delete the MIDI file
 * 
 * @author Jan Trukenmüller
 */
public class MusescoreImporter extends MidiParser {
	
	// foreign program description for error messages
	private static String programName = Dict.get(Dict.FOREIGN_PROG_MSCORE);
	
	@Override
	protected int getImportFormat() {
		return SequenceCreator.IMPORT_FORMAT_ABC;
	}
	
	/**
	 * Imports a file using MuseScore.
	 * 
	 * @param fileAsObj  file to be imported.
	 */
	@Override
	public void parse(Object fileAsObj) throws ParseException {
		File file = (File) fileAsObj;
		
		// reset file name and file type
		preprocess(file);
		midiFileCharset = null;
		chosenCharset   = "US-ASCII";
		
		try {
			String execPath = Config.get(Config.EXEC_PATH_IMP_MSCORE);
			
			// create a temp file
			File tempfile = Foreign.createTempMidiFile();
			
			// convert file to the MIDI tempfile
			String[] convertCmd = {execPath, "-o", tempfile.getAbsolutePath(), file.getAbsolutePath()};
			Foreign.execute(convertCmd, programName, false);
			
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
