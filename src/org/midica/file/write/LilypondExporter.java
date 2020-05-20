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
 * This class is used to export a LilyPond file using the midi2ly executable (belongs to LilyPond).
 * This works only if midi2ly is available.
 * 
 * Steps:
 * 
 * - Export the currently loaded MIDI sequence as a temporary MIDI file
 * - Convert the MIDI file to LilyPond, using midi2ly
 * - Delete the MIDI file
 * 
 * @author Jan Trukenmüller
 */
public class LilypondExporter extends MidiExporter {
	
	// foreign program description for error messages
	private static String programName = Dict.get(Dict.FOREIGN_PROG_MIDI2LY);
	
	/**
	 * Exports an LilyPond file.
	 * 
	 * @param file  LilyPond file to be exported.
	 * @return                   Empty data structure (warnings are not used for LilyPond exports).
	 * @throws  ExportException  If the file can not be exported correctly.
	 */
	public ExportResult export(File file) throws ExportException {
		
		// user doesn't want to overwrite the file?
		if (! createFile(file)) {
			return new ExportResult(false);
		}
		
		// charset // TODO: optimize
		String sourceFileType = SequenceCreator.getFileType();
		sourceCharset = "mid".equals(sourceFileType) ? Config.get(Config.CHARSET_MID) : Config.get(Config.CHARSET_MPL);
		targetCharset = sourceCharset;
		
		try {
			// create temp midi file
			File tempfile = Foreign.createTempMidiFile();
			
			// export the MIDI file
			Sequence seq = cloneSequence();
			int[] supportedFileTypes = MidiSystem.getMidiFileTypes(seq);
			MidiSystem.write(seq, supportedFileTypes[0], tempfile);
			
			// create the convert command
			String execPath = Config.get(Config.EXEC_PATH_EXP_LY);
			
			// convert from the MIDI tempfile to LilyPond
			String[] midi2ly = {execPath, "-o", file.getAbsolutePath(), tempfile.getAbsolutePath()};
			Foreign.execute(midi2ly, programName, false);
			
			// delete tempfile
			Foreign.deleteTempFile(tempfile);
			
			return new ExportResult(true);
		}
		catch (ForeignException | InvalidMidiDataException | IOException e) {
			throw new ExportException(e.getMessage());
		}
	}
}
