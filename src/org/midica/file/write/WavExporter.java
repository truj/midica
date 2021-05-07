/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file.write;

import java.io.File;

import javax.sound.midi.Sequence;
import javax.sound.midi.Soundbank;

import org.midica.midi.MidiDevices;
import org.midica.ui.file.ExportResult;

import com.sun.karlhelgason.MidiToAudioRenderer;

/**
 * This class is used to export the currently loaded MIDI sequence as a MIDI file.
 * 
 * @author Jan Trukenmüller
 */
public class WavExporter extends Exporter {
	
	private ExportResult exportResult = null;
	
	/**
	 * Creates a new MIDI exporter.
	 */
	public WavExporter() {
	}
	
	/**
	 * Exports a WAV file.
	 * 
	 * @param   file             WAV file.
	 * @return                   Empty data structure (warnings are not used for WAV exports).
	 * @throws  ExportException  If the file can not be exported correctly.
	 */
	public ExportResult export(File file) throws ExportException {
		
		exportResult = new ExportResult(true);
		
		try {
			
			// user doesn't want to overwrite the file?
			if (! createFile(file))
				return new ExportResult(false);
			
			// get loaded soundfont and sequence
			Soundbank soundfont = MidiDevices.getSoundfont();
			Sequence  seq       = MidiDevices.getSequence();
			
			// create file
			MidiToAudioRenderer.render(soundfont, seq, file);
		}
		catch (Exception e) {
			throw new ExportException(e.getMessage());
		}
		
		return exportResult;
	}
}
