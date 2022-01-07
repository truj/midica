/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file.read;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;

import org.midica.config.Config;
import org.midica.config.Dict;
import org.midica.file.Foreign;
import org.midica.file.ForeignException;
import org.midica.midi.SequenceCreator;

/**
 * This class is used to import a LilyPond file using the lilypond executable.
 * This works only if LilyPond is installed.
 * 
 * The process contains the following steps:
 * 
 * - Convert the lilypond file to a MIDI tempfile, using the lilypond executable
 * - Parse the MIDI file using the parent class
 * - Delete the MIDI file
 * 
 * @author Jan Trukenmüller
 */
public class LilypondImporter extends MidiParser {
	
	// foreign program description for error messages
	private static String programName = Dict.get(Dict.FOREIGN_PROG_LY);
	
	@Override
	protected int getImportFormat() {
		return SequenceCreator.IMPORT_FORMAT_LY;
	}
	
	/**
	 * Parses a LilyPond file.
	 * 
	 * @param fileAsObj  LilyPond file to be parsed.
	 */
	@Override
	public void parse(Object fileAsObj) throws ParseException {
		File file = (File) fileAsObj;
		
		// reset file name and file type
		preprocess(file);
		midiFileCharset = null;
		chosenCharset   = "UTF-8";
		
		try {
			String execPath = Config.get(Config.EXEC_PATH_IMP_LY);
			
			// create temp file name (without extension) in a temp directory
			// this is needed because lilypond may create more than one MIDI file
			Path   dir      = Foreign.createTempDirectory();
			File   tempfile = Foreign.createTempFile("", dir);
			String tempName = tempfile.getAbsolutePath();
			Foreign.deleteTempFile(tempfile);
			
			// convert from LilyPond file to the tempfile(s)
			String[] lyConvert = {execPath, "-dbackend=null", "-dmidi-extension=mid", "-o", tempName, file.getAbsolutePath()};
			Foreign.execute(lyConvert, programName, false);
			
			// get all MIDI files, created by lilypond
			File[] files = Foreign.getFiles(dir);
			if (0 == files.length)
				throw new ParseException(Dict.get(Dict.ERROR_LILYPOND_NO_MIDI_FILE));
			
			// create one sequence for each created MIDI file
			ArrayList<Sequence> sequences = new ArrayList<>();
			Integer resolution = null;
			for (File f : files) {
				Sequence s = MidiSystem.getSequence(f);
				
				// check sequence (only PPQ with the same resolution)
				if (s.getDivisionType() != Sequence.PPQ)
					throw new ParseException(Dict.get(Dict.ERROR_WRONG_DIVISION_TYPE));
				int res = s.getResolution();
				if (resolution != null && res != resolution)
					throw new ParseException(Dict.get(Dict.ERROR_DIFFERENT_RESOLUTION));
				resolution = res;
				
				sequences.add(s);
				f.delete();
			}
			
			// merge all sequences
			Sequence mergedSequence = new Sequence(Sequence.PPQ, resolution);
			for (Sequence seq : sequences) {
				for (Track track : seq.getTracks()) {
					Track targetTrack = mergedSequence.createTrack();
					for (int i=0; i < track.size(); i++) {
						MidiEvent event = track.get(i);
						targetTrack.add(event);
					}
				}
			}
			
			// delete tempfile
			Foreign.deleteTempDir(dir);
			
			// transform and analyze the sequence
			createSequence(mergedSequence);
			postprocessSequence(mergedSequence, chosenCharset); // analyze the original sequence
			
			// unfortunately LilyPond includes channel volume messages
			// so we have to treat them by expression
			replaceChannelVolume();
		}
		catch (ForeignException | InvalidMidiDataException | IOException e) {
			throw new ParseException(e.getMessage());
		}
	}
}
