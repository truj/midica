/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file.read;

import java.io.File;

import javax.sound.midi.Sequence;

import org.midica.config.Dict;
import org.midica.midi.MidiDevices;
import org.midica.midi.SequenceAnalyzer;
import org.midica.midi.SequenceCreator;
import org.midica.ui.info.InfoView;

/**
 * This class can be extended by specialized parser classes
 * which parse an input file and create a MIDI sequence.
 * 
 * Derived classes are:
 * 
 * - {@link MidicaPLParser} (parses a MidicaPL file)
 * - {@link MidiParser} (parses a MIDI file)
 * - Other parsers, derived by {@link MidiParser} (use an external tool to create a temporary midi file and parse this file)
 * 
 * @author Jan Trukenmüller
 */
public abstract class SequenceParser implements IParser {
	
	private static final int FORMAT_NONE      = -1;
	public  static final int FORMAT_MIDICAPL  =  1;
	public  static final int FORMAT_MIDI      =  2;
	public  static final int FORMAT_ALDA      =  3;
	public  static final int FORMAT_MUSESCORE =  4;
	
	/**
	 * Defines how much the parsed input has to be transposed.
	 * A positive transpose level causes a transposition into higher pitches.
	 * A negative level transposes into lower pitches.
	 */
	protected static byte transposeLevel = 0;
	
	/** Type of the last successfully parsed file, according to one of the FORMAT_... fields. */
	private static int fileFormat = FORMAT_NONE;
	
	/** Last successfully parsed file. */
	private static File sequenceFile = null;
	
	/** The file to be parsed. */
	private static File currentFile = null;
	
	/**
	 * Returns the transpose level. This is the value which defines how much the
	 * input has to be transposed before creating the MIDI stream.
	 * 
	 * @return transpose level
	 */
	public static byte getTransposeLevel() {
		return transposeLevel;
	}
	
	/**
	 * Sets the transpose level. That defines how much the parsed input is
	 * transposed.
	 * 
	 * - Positive level: transposition into a higher pitch
	 * - Negative level: transposition into a lower pitch
	 * 
	 * @param level Transpose level
	 */
	public static void setTransposeLevel(byte level) {
		transposeLevel = level;
	}
	
	/**
	 * Transposes the given note into another pitch according to the current
	 * {@link #transposeLevel}.
	 * Does not transpose anything on channel 9 because that is the percussion channel and
	 * transposing percussion instrument transposition is not supported.
	 * 
	 * @param note             Input note value as defined by the MIDI specification (0-127)
	 * @param channel          Channel number as defined by the MIDI specification (0-15)
	 * @return                 Target note after the transposition.
	 * @throws ParseException  If the transposition would cause an invalid target note with
	 *                         a value lower than 0 or higher than 127.
	 */
	protected int transpose(int note, int channel) throws ParseException {
		
		// don't transpose percussions
		if (9 == channel)
			return note;
		
		note += transposeLevel;
		if (note < 0)
			throw new ParseException(Dict.get(Dict.ERROR_NOTE_TOO_SMALL) + note);
		if (note > 127)
			throw new ParseException(Dict.get(Dict.ERROR_NOTE_TOO_BIG) + note);
		
		return note;
	}
	
	/**
	 * Resets the file name and format.
	 * Remembers the file to be parsed so that it can be marked as successful
	 * later.
	 * 
	 * This method is called before parsing.
	 * 
	 * @param file  The file to be parsed.
	 */
	protected void preprocess(File file) {
		fileFormat   = FORMAT_NONE;
		sequenceFile = null;
		currentFile  = file;
		SequenceAnalyzer.reset();
		MidiDevices.setSequence(null);
	}
	
	/**
	 * Postprocesses the loaded MIDI sequence.
	 * 
	 * Retrieves information from the given sequence and makes them available
	 * for the {@link InfoView}.
	 * 
	 * Makes the created sequence available for the player.
	 * 
	 * The sequence to be analyzed is not necessarily the same
	 * that will be published. After parsing a MidicaPL file it's
	 * the same. But after parsing a MIDI file the original sequence
	 * will be analyzed while the sequence created by the
	 * {@link SequenceCreator} will be published.
	 * 
	 * @param seq        The MIDI sequence to be analyzed.
	 * @param format     one of the FORMAT_... fields, depending on the derived parser class.
	 * @param charset    The charset that has been chosen in the file chooser.
	 * @throws ParseException if a marker event cannot be created during
	 *                        the postprocessing of the analyzing process
	 */
	protected void postprocessSequence(Sequence seq, int format, String charset) throws ParseException {
		
		// analyze sequence and add marker events
		SequenceAnalyzer.analyze(seq, charset);
		
		// publich successfully parsed file
		sequenceFile = currentFile;
		fileFormat   = format;
		MidiDevices.setSequence(SequenceCreator.getSequence());
	}
	
	/**
	 * Returns the format of the successfully parsed file.
	 * 
	 * @return the file format.
	 */
	public static int getFileFormat() {
		return fileFormat;
	}
	
	/**
	 * Returns the absolute path of the successfully parsed file, if it matches the requested type.
	 * Returns **null**, if the requested type doesn't match or no file has been parsed successfully.
	 * 
	 * @param  format    format type (according to one of the FORMAT_... fields)
	 * @return file path or **null**.
	 */
	public static String getFilePath(int format) {
		if (null == sequenceFile)
			return null;
		if (FORMAT_NONE == fileFormat)
			return null;
		if (format == fileFormat)
			return sequenceFile.getAbsolutePath();
		return null;
	}
	
	/**
	 * Returns the base name of the successfully parsed file.
	 * 
	 * @return the parsed file name if available, or **null** otherwise.
	 */
	public static String getFileName() {
		if (null == sequenceFile) {
			return null;
		}
		return sequenceFile.getName();
	}
}
