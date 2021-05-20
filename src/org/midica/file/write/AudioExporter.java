/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file.write;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.sound.midi.Sequence;
import javax.sound.midi.Soundbank;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;

import org.midica.config.Config;
import org.midica.file.read.SoundfontParser;
import org.midica.midi.MidiDevices;
import org.midica.ui.file.ExportResult;
import org.midica.ui.file.config.AudioConfigController;
import org.midica.ui.file.config.AudioConfigView;

import com.sun.gervill.SF2Soundbank;
import com.sun.kh.MidiToAudioRenderer;

/**
 * This class is used to export the currently loaded MIDI sequence as an audio file.
 * Different audio formats are supported.
 * 
 * @author Jan Trukenmüller
 */
public class AudioExporter extends Exporter {
	
	// define default values
	public static final String  DEFAULT_ENCODING         = AudioFormat.Encoding.PCM_SIGNED.toString();
	public static final int     DEFAULT_SAMPLE_SIZE_BITS = 16;
	public static final float   DEFAULT_SAMPLE_RATE      = 44100;
	public static final int     DEFAULT_CHANNELS         = 2;
	public static final boolean DEFAULT_IS_BIG_ENDIAN    = false;
	
	// audio configuration
	public static String  ENCODING         = DEFAULT_ENCODING;
	public static int     SAMPLE_SIZE_BITS = DEFAULT_SAMPLE_SIZE_BITS;
	public static float   SAMPLE_RATE      = DEFAULT_SAMPLE_RATE;
	public static int     CHANNELS         = DEFAULT_CHANNELS;
	public static boolean IS_BIG_ENDIAN    = DEFAULT_IS_BIG_ENDIAN;
	
	/**
	 * Creates a new audio exporter.
	 */
	public AudioExporter() {
	}
	
	/**
	 * Exports an audio file.
	 * 
	 * @param  file             target audio file
	 * @return an empty data structure (warnings are not used for audio exports).
	 * @throws ExportException   if the file can not be exported correctly.
	 */
	public ExportResult export(File file) throws ExportException {
		try {
			
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
			
			// get and apply the current session config
			refreshConfig();
			AudioFormat.Encoding encoding = getEncoding(ENCODING);
			AudioFileFormat.Type fileType = getFileType(file);
			
			// create format
			int   frameSize = ((SAMPLE_SIZE_BITS + 7) / 8) * CHANNELS;
			float frameRate = SAMPLE_RATE; // TODO: change for non-pcm, if necessary
			AudioFormat format = new AudioFormat(
				encoding,
				SAMPLE_RATE,
				SAMPLE_SIZE_BITS,
				CHANNELS,
				frameSize,
				frameRate,
				IS_BIG_ENDIAN
			);
			
			// create file
			MidiToAudioRenderer.render(soundfont, seq, file, format, fileType);
		}
		catch (Exception e) {
			throw new ExportException(e.getMessage());
		}
		
		return new ExportResult(true);
	}
	
	/**
	 * Returns a list with all possible encoding formats.
	 */
	public static ArrayList<AudioFormat.Encoding> getEncodings() {
		ArrayList<AudioFormat.Encoding> encodings = new ArrayList<>();
		
		encodings.add(AudioFormat.Encoding.PCM_SIGNED);
		encodings.add(AudioFormat.Encoding.PCM_UNSIGNED);
		encodings.add(AudioFormat.Encoding.PCM_FLOAT);
		encodings.add(AudioFormat.Encoding.ALAW);
		encodings.add(AudioFormat.Encoding.ULAW);
		
		return encodings;
	}
	
	/**
	 * Returns an encoding by a string.
	 * 
	 * @param encodingStr  encoding string like it is used in the config file
	 * @return the according encoding, or PCM_SIGNED, if no matching encoding is found.
	 */
	private static AudioFormat.Encoding getEncoding(String encodingStr) {
		
		// init with default
		AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
		
		// select from possible encodings
		for (AudioFormat.Encoding enc : getEncodings()) {
			if (encodingStr.equals(enc.toString())) {
				encoding = enc;
			}
		}
		
		return encoding;
	}
	
	/**
	 * Returns a file type according to the given file's extension,
	 * 
	 * @param file  the target file
	 * @return the file type
	 */
	private static AudioFileFormat.Type getFileType(File file) {
		
		// get file extension
		String ext      = null;
		String fileName = file.getName();
		int i = fileName.lastIndexOf('.');
		if (i > 0)
			ext = fileName.substring(i+1);
		
		// select file type
		if (ext != null) {
			if ("wav".equals(ext))
				return AudioFileFormat.Type.WAVE;
			if ("au".equals(ext))
				return AudioFileFormat.Type.AU;
			if ("snd".equals(ext))
				return AudioFileFormat.Type.SND;
			if ("aif".equals(ext))
				return AudioFileFormat.Type.AIFF;
			if ("aiff".equals(ext))
				return AudioFileFormat.Type.AIFF;
			if ("aifc".equals(ext))
				return AudioFileFormat.Type.AIFC;
		}
		
		// invalid type
		return null;
	}
	
	/**
	 * Re-reads all config variables that are relevant for audio export.
	 */
	private void refreshConfig() {
		HashMap<String, String> sessionConfig = AudioConfigController.getInstance(
			new AudioConfigView(), null
		).getSessionConfig();
		ENCODING         =                       sessionConfig.get(Config.AU_ENCODING);
		SAMPLE_SIZE_BITS = Integer.parseInt(     sessionConfig.get(Config.AU_SAMPLE_SIZE_BITS) );
		SAMPLE_RATE      = Float.parseFloat(     sessionConfig.get(Config.AU_SAMPLE_RATE)      );
		CHANNELS         = Integer.parseInt(     sessionConfig.get(Config.AU_CHANNELS)         );
		IS_BIG_ENDIAN    = Boolean.parseBoolean( sessionConfig.get(Config.AU_IS_BIG_ENDIAN)    );
	}
}
