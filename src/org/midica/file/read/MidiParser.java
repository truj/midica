/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file.read;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Track;

import org.midica.config.Config;
import org.midica.config.Dict;
import org.midica.file.CharsetUtils;
import org.midica.midi.LyricUtil;
import org.midica.midi.MidiListener;
import org.midica.midi.SequenceCreator;
import org.midica.ui.model.ComboboxStringOption;
import org.midica.ui.model.ConfigComboboxModel;

/**
 * An object of this class can be used in order to parse a MIDI file.
 * 
 * It uses the {@link SequenceCreator} in order to create a MIDI sequence from the parsed input
 * and includes meta events whenever a key is pressed or released.
 * This is used later for the channel activity analyzer in the player while playing the
 * MIDI sequence.
 * 
 * @author Jan Trukenmüller
 */
public class MidiParser extends SequenceParser {
	
	// Midi control messages
	public static final int CTRL_CHANGE_BANK_SELECT      =  0;
	public static final int CTRL_CHANGE_MODULATION_WHEEL =  1;
	public static final int CTRL_CHANGE_FOOT_CONTROLLER  =  4;
	public static final int CTRL_CHANGE_VOLUME           =  7;
	public static final int CTRL_CHANGE_BALANCE          =  8;
	public static final int CTRL_CHANGE_PAN              = 10;
	
	private boolean isProducedByMidica = false;
	
	protected String midiFileCharset = null;
	protected String chosenCharset   = null;
	
	private static LyricUtil lyricUtil = LyricUtil.getInstance();
	
	/**
	 * Parses a MIDI file.
	 * 
	 * @param fileAsObj  MIDI file to be parsed.
	 */
	public void parse(Object fileAsObj) throws ParseException {
		File file = (File) fileAsObj;
		
		// reset file name and file type
		preprocess(file);
		
		isProducedByMidica = false;
		midiFileCharset    = null;
		
		// get chosen charset
		ConfigComboboxModel  charsetModel = ConfigComboboxModel.getModel(Config.CHARSET_MID);
		ComboboxStringOption chosenOption = (ComboboxStringOption) charsetModel.getSelectedItem();
		chosenCharset = chosenOption.getIdentifier();
		
		try {
			Sequence sequence = MidiSystem.getSequence(file);
			createSequence(sequence);
			postprocessSequence(sequence, chosenCharset); // we want to analyze the loaded sequence - not the created one
			
			// Many MIDI files out there contain channel volume messages.
			// Transform them into expression messages.
			replaceChannelVolume();
		}
		catch (InvalidMidiDataException e) {
			throw new ParseException(e.getMessage());
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new ParseException(e.getMessage());
		}
	}
	
	/**
	 * Returns the import format defined in {@link SequenceCreator}
	 * as IMPORT_FORMAT_*.
	 * 
	 * Returns {@link SequenceCreator#IMPORT_FORMAT_MIDI} by default.
	 * Can be overridden by foreign format importers like ALDA, ABC, and so on.
	 * 
	 * @return the import format.
	 */
	protected int getImportFormat() {
		return SequenceCreator.IMPORT_FORMAT_MIDI;
	}
	
	/**
	 * Transforms the parsed MIDI sequence. Passes all events to {@link SequenceCreator}
	 * in order to create a new sequence while adding meta events for each key-press
	 * and key-release event.
	 * 
	 * @param  sequence                  The original MIDI sequence.
	 * @throws ParseException            If the input file can not be parsed correctly.
	 * @throws InvalidMidiDataException  If the created sequence is invalid.
	 */
	protected void createSequence(Sequence sequence) throws ParseException, InvalidMidiDataException {
		
		// process global parameters and initialize the sequence to create
		float divisionType = sequence.getDivisionType();
		if (Sequence.PPQ != divisionType)
			throw new ParseException(Dict.get(Dict.ERROR_ONLY_PPQ_SUPPORTED));
		int resolution = sequence.getResolution();
		try {
			SequenceCreator.reset(resolution, chosenCharset, getImportFormat());
			// init percussion channel comment
			SequenceCreator.initChannel(9, 0, Dict.get(Dict.PERCUSSION_CHANNEL), SequenceCreator.NOW);
		}
		catch (InvalidMidiDataException e) {
			throw new ParseException(e.getMessage());
		}
		
		int trackNum = 0;
		for (Track t : sequence.getTracks()) {
			for (int i=0; i < t.size(); i++) {
				MidiEvent   event = t.get(i);
				long        tick  = event.getTick();
				MidiMessage msg   = event.getMessage();
				
				if (msg instanceof MetaMessage) {
					processMetaMessage((MetaMessage) msg, tick, trackNum);
				}
				else if (msg instanceof ShortMessage) {
					processShortMessage((ShortMessage) msg, tick);
				}
				else if (msg instanceof SysexMessage) {
					processSysexMessage((SysexMessage) msg, tick);
				}
				else {
				}
			}
			trackNum++;
		}
	}
	
	/**
	 * Processes a meta message from the input sequence.
	 * 
	 * Meta messages are just forwarded into the target sequence without being
	 * changed. Karaoke-related meta messages are written into track 1 or 2.
	 * All others will be written into track 0.
	 * 
	 * @param msg        Meta message from the input sequence.
	 * @param tick       Tickstamp of the message's occurrence.
	 * @param origTrack  Original track number.
	 * @throws InvalidMidiDataException
	 */
	private void processMetaMessage(MetaMessage msg, long tick, int origTrack) throws InvalidMidiDataException {
		int track = 0; // default track for generic messages
		int type  = msg.getType();
		
		// lyrics: track 2
		if (MidiListener.META_LYRICS == type) {
			track = 2;
			
			// produced by midica?
			String text = CharsetUtils.getTextFromBytes(msg.getData(), chosenCharset, midiFileCharset);
			HashMap<String, String> info = lyricUtil.getSongInfo(text);
			if (info != null && info.containsKey(LyricUtil.SOFTWARE)) {
				String value = info.get(LyricUtil.SOFTWARE);
				if (value.startsWith("Midica ")) {
					isProducedByMidica = true;
				}
			}
		}
		
		// text: further checks needed
		else if (MidiListener.META_TEXT == type) {
			String text = CharsetUtils.getTextFromBytes(msg.getData(), chosenCharset, midiFileCharset);
			
			// karaoke meta information for track 1
			if ( text.startsWith("@K")
			  || text.startsWith("@V")
			  || text.startsWith("@I") ) {
				track = 1;
			}
			
			// either lyrics or karaoke meta information for track 2 ("@L" or "@T")
			else {
				track = 2;
			}
		}
		
		// instrument comment - keep original track number because that
		// determins the channel number
		else if (MidiListener.META_INSTRUMENT_NAME == type && isProducedByMidica) {
			track = origTrack;
		}
		
		// add the message to the right track
		SequenceCreator.addMessageToTrack(msg, track, tick);
		
		// charset switch in a TEXT or LYRICS event?
		if (MidiListener.META_LYRICS == type || MidiListener.META_TEXT == type) {

			// charset definition?
			String text       = CharsetUtils.getTextFromBytes(msg.getData(), chosenCharset, midiFileCharset);
			String newCharset = CharsetUtils.findCharsetSwitch(text);
			if (newCharset != null) {
				midiFileCharset = newCharset;
			}
		}
	}
	
	/**
	 * Processes a SysEx message from the input sequence. SysEx messages are just
	 * forwarded into the target sequence without being changed.
	 * 
	 * @param msg   SysEx message from the input sequence.
	 * @param tick  Tickstamp of the message's occurrence.
	 */
	private void processSysexMessage(SysexMessage msg, long tick) {
		SequenceCreator.addMessageGeneric(msg, tick);
	}
	
	/**
	 * Processes a short message from the input sequence.
	 * 
	 * If it is a **note-on** or **note-off** message than the note will be transposed
	 * according to the configured transpose level.
	 * 
	 * If it is a **program change**, **note-on** or **note-off** message than an according
	 * meta message is added to the target sequence together with the message itself.
	 * 
	 * @param msg   The short message from the input sequence.
	 * @param tick  Tickstamp of the message's occurrence.
	 * @throws InvalidMidiDataException
	 * @throws ParseException
	 */
	private void processShortMessage(ShortMessage msg, long tick) throws InvalidMidiDataException, ParseException {
		int cmd      = msg.getCommand();
		int channel  = msg.getChannel();
		int note     = msg.getData1();
		int velocity = msg.getData2();
		if (channel < 0 || channel > 15) {
			// not a channel command
			SequenceCreator.addMessageGeneric(msg, tick);
			return;
		}
		
		if (ShortMessage.NOTE_ON == cmd && velocity > 0) {
			
			// note on
			note = transpose(note, channel);
			SequenceCreator.addMessageNoteON(channel, note, tick, velocity);
		}
		else if (ShortMessage.NOTE_OFF == cmd || (ShortMessage.NOTE_ON == cmd && 0 == velocity)) {
			
			// note off
			note = transpose(note, channel);
			SequenceCreator.addMessageNoteOFF(channel, note, tick);
		}
		
		else {
			// another channel command
			SequenceCreator.addMessageGeneric(msg, channel, tick);
		}
	}
	
	/**
	 * Replaces all channel volume events in the successfully parsed sequence
	 * by an according expression message.
	 * 
	 * Problem:
	 * 
	 * Many MIDI files contain channel volume messages (MSB Controller 0x07, LSB Controller 0x27).
	 * These interfere with setting the channel volume in the player.
	 * 
	 * Solution:
	 * 
	 * - Replace controller 0x07 (channel volume MSB) with controller 0x0B (expression MSB)
	 * - Replace controller 0x27 (channel volume MSB) with controller 0x2B (expression MSB)
	 * 
	 * The messages are replaced only in the CREATED sequence (the one that's used by the player).
	 * The original sequence (used by the analyzer) stays untouched.
	 * 
	 * @throws InvalidMidiDataException if the transformation to expression fails
	 */
	protected void replaceChannelVolume() throws InvalidMidiDataException {
		
		Sequence seq = SequenceCreator.getSequence();
		for (Track track : seq.getTracks()) {
			for (int i=0; i < track.size(); i++) {
				MidiEvent   event        = track.get(i);
				MidiMessage msg          = event.getMessage();
				int         status       = msg.getStatus();
				boolean     isCtrlChange = ShortMessage.CONTROL_CHANGE == (status & 0b1111_0000);
				
				// control change message?
				if (isCtrlChange) {
					ShortMessage sMsg = (ShortMessage) msg;
					int controller = sMsg.getData1();
					
					// channel volume MSB or LSB?
					if (0x07 == controller || 0x27 == controller) {
						controller += 4;
						sMsg.setMessage(status, controller, sMsg.getData2());
					}
				}
			}
		}
	}
}
