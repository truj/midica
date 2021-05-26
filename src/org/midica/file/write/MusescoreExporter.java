/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file.write;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

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
 * This class is used to export a file from MIDI to something else using MuseScore.
 * This works only if MuseScore is installed.
 * 
 * Steps:
 * 
 * - Export the currently loaded MIDI sequence as a temporary MIDI file
 * - Convert the MIDI file to something else, using MuseScore, into a temp directory
 * - Move the file(s) from the temp directory to the final destination
 * - Delete the MIDI file and the temporary directory
 * 
 * @author Jan Trukenmüller
 */
public class MusescoreExporter extends MidiExporter {
	
	// foreign program description for error messages
	private static String programName = Dict.get(Dict.FOREIGN_PROG_MSCORE);
	
	/**
	 * Exports a file.
	 * 
	 * @param file    file to be exported
	 * @return                     Empty data structure (warnings are not used for MuseScore exports).
	 * @throws  ExportException    If the file can not be exported correctly..
	 */
	public ExportResult export(File file) throws ExportException {
		
		// user doesn't want to overwrite the file?
		if (! createFile(file))
			return new ExportResult(false);
		
		try {
			// create temp midi file
			File tempMidiFile = Foreign.createTempMidiFile();
			
			// export the MIDI file
			Sequence seq = cloneSequence();
			int[] supportedFileTypes = MidiSystem.getMidiFileTypes(seq);
			MidiSystem.write(seq, supportedFileTypes[0], tempMidiFile);
			
			// create the convert command
			String execPath = Config.get(Config.EXEC_PATH_EXP_MSCORE);
			
			// get target file extension
			String extension = null;
			int extIndex = file.getName().lastIndexOf('.');
			if (extIndex > 0) {
				extension = file.getName().substring(extIndex + 1);
			}
			
			// TODO: check extension
			
			// create temp dir and temp file path
			Path tempDir        = Foreign.createTempDirectory();
			File tempTargetFile = Foreign.createTempFile(extension, tempDir);
			String tempTargetPath = tempTargetFile.getAbsolutePath();
			
			// convert from the MIDI tempfile to ABC
			String[] cmd = {execPath, "-i", tempMidiFile.getAbsolutePath(), "-o", tempTargetPath};
			Foreign.execute(cmd, programName, false);
			
			// get all MIDI files, created by lilypond
			File[] files = Foreign.getFiles(tempDir);
			
			// move all created file(s) to the final destination
			if (0 == files.length) {
				throw new ExportException(Dict.get(Dict.ERROR_MSCORE_NO_OUTPUT_FILE));
			}
			else if (1 == files.length) {
				String from = files[0].getAbsolutePath();
				boolean ok  = files[0].renameTo(file);
				if (! ok) {
					throw new ExportException(String.format(
						Dict.get(Dict.ERROR_MSCORE_MOVE_FAILED),
						from,
						file.getAbsolutePath()
					));
				}
			}
			else {
				String targetDir = file.getParent();
				for (File f : files) {
					String name = f.getName();
					f.renameTo(new File(targetDir + File.separator + name));
				}
			}
			
			// delete tempfile and temp dir
			Foreign.deleteTempFile(tempMidiFile);
			Foreign.deleteTempDir(tempDir);
			
			return new ExportResult(true);
		}
		catch (ForeignException | InvalidMidiDataException | IOException e) {
			throw new ExportException(e.getMessage());
		}
	}
	
	@Override
	protected String getTargetCharset() {
		
		// use the same charset for source and target
		// TODO: check if other target charsets should be selectable or not
		
		return SequenceCreator.getCharset();
	}
}
