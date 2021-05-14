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
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;

import org.midica.file.read.SoundfontParser;
import org.midica.midi.MidiDevices;
import org.midica.ui.file.ExportResult;

import com.sun.gervill.SF2Soundbank;
import com.sun.kh.MidiToAudioRenderer;

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
			
			// PCM_SIGNED:   8, 16, 24, 32, ...
			// PCM_UNSIGNED: 8, 16, 24, 32, ...
			// PCM_FLOAT:    32 or 64
			// File Types supported by my system: WAVE, AU, AIFF
			
			// TODO: make configurable
			AudioFileFormat.Type type     = AudioFileFormat.Type.WAVE;
			AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_UNSIGNED;
			int     sampleSizeInBits =    16;
			float   sampleRate       = 44100;
			int     channels         =     2;
			boolean isBigEndian      =  true;
			
			// TODO: delete
//			encoding = AudioFormat.Encoding.ALAW;
//			type = AudioFileFormat.Type.AU;
			
			// user doesn't want to overwrite the file?
			if (! createFile(file))
				return new ExportResult(false);
			
			// get loaded soundfont and sequence
			Soundbank soundfont = MidiDevices.getSoundfont();
			Sequence  seq       = MidiDevices.getSequence();
			
			// load the soundfont in the right format, if not yet done
			if (soundfont.getClass() != SF2Soundbank.class) {
				File   sf2File = null;
				String sf2Path = SoundfontParser.getFilePath();
				if (sf2Path != null)
					sf2File = new File(sf2Path);
				soundfont = new SF2Soundbank(sf2File);
			}
			
			// create format
			int   frameSize = ((sampleSizeInBits + 7) / 8) * channels;
			float frameRate = sampleRate;
			AudioFormat format = new AudioFormat(
				encoding,
				sampleRate,
				sampleSizeInBits,
				channels,
				frameSize,
				frameRate,
				isBigEndian
			);
			
			// create file
			MidiToAudioRenderer.render(soundfont, seq, file, format, type);
		}
		catch (Exception e) {
			throw new ExportException(e.getMessage());
		}
		
		return exportResult;
	}
}
