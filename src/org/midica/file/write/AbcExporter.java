/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file.write;

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
import org.midica.ui.file.ExportResult;

/**
 * This class is used to export an ABC file using the midi2abc executable (belongs to abcMIDI).
 * https://ifdo.ca/~seymour/runabc/top.html
 * This works only if midi2abc is available.
 * 
 * Steps:
 * 
 * - Export the currently loaded MIDI sequence as a temporary MIDI file
 * - Convert the MIDI file to ABC, using midi2abc
 * - Delete the MIDI file
 * 
 * @author Jan Trukenmüller
 */
public class AbcExporter extends MidiExporter {
	
	// foreign program description for error messages
	private static String programName = Dict.get(Dict.FOREIGN_PROG_MIDI2ABC);
	
	/**
	 * Exports an ABC file.
	 * 
	 * @param file  ABC file to be exported
	 * @return                     Empty data structure (warnings are not used for ABC exports).
	 * @throws  ExportException    If the file can not be exported correctly..
	 */
	public ExportResult export(File file) throws ExportException {
		
		// user doesn't want to overwrite the file?
		if (! createFile(file))
			return new ExportResult(false);
		
		try {
			// create temp midi file
			File tempfile = Foreign.createTempMidiFile();
			
			// export the MIDI file
			Sequence seq = cloneSequence();
			int[] supportedFileTypes = MidiSystem.getMidiFileTypes(seq);
			MidiSystem.write(seq, supportedFileTypes[0], tempfile);
			
			// create the convert command
			String execPath = Config.get(Config.EXEC_PATH_EXP_ABC);
			
			// convert from the MIDI tempfile to ABC
			String[] midi2abc = {execPath, "-i", tempfile.getAbsolutePath(), "-o", file.getAbsolutePath()};
			Foreign.execute(midi2abc, programName, false);
			
			// delete tempfile
			Foreign.deleteTempFile(tempfile);
			
			return new ExportResult(true);
		}
		catch (ForeignException | InvalidMidiDataException | IOException e) {
			throw new ExportException(e.getMessage());
		}
	}
	
	@Override
	protected String getTargetCharset() {
		
		// use the same charset for source and target
		// TODO: check if ABC needs or supports one or more special charsets
		
		return SequenceCreator.getCharset();
	}
}
