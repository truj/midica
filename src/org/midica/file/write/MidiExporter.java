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
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import org.midica.config.Config;
import org.midica.config.Dict;
import org.midica.file.CharsetUtils;
import org.midica.midi.MidiDevices;
import org.midica.midi.MidiListener;
import org.midica.midi.SequenceCreator;
import org.midica.ui.file.ExportResult;
import org.midica.ui.model.ComboboxStringOption;
import org.midica.ui.model.ConfigComboboxModel;

/**
 * This class is used to export the currently loaded MIDI sequence as a MIDI file.
 * 
 * @author Jan Trukenmüller
 */
public class MidiExporter extends Exporter {
	
	/** (default) charset used to read the currently loaded file (or its text-based messages) */
	protected String sourceCharset = null;
	
	/** Target charset for text-based messages. */
	protected String targetCharset = null;
	
	/** Source charset, specified in the sequence. */
	private String fileCharset = null;
	
	private ExportResult exportResult = null;
	
	/**
	 * Creates a new MIDI exporter.
	 */
	public MidiExporter() {
	}
	
	/**
	 * Exports a MIDI file.
	 * 
	 * @param   file             MIDI file.
	 * @return                   Empty data structure (warnings are not used for MIDI exports).
	 * @throws  ExportException  If the file can not be exported correctly.
	 */
	public ExportResult export(File file) throws ExportException {
		
		exportResult = new ExportResult(true);
		
		// charset-related initializations
		targetCharset = getTargetCharset();
		sourceCharset = SequenceCreator.getCharset();
		
		try {
			
			// user doesn't want to overwrite the file?
			if (! createFile(file))
				return new ExportResult(false);
			
			// export the MIDI file
			Sequence seq = cloneSequence();
			int[] supportedFileTypes = MidiSystem.getMidiFileTypes(seq);
			MidiSystem.write(seq, supportedFileTypes[0], file);
			
		}
		catch (IOException | InvalidMidiDataException e) {
			throw new ExportException(e.getMessage());
		}
		
		return exportResult;
	}
	
	/**
	 * Determins and returns the target charset from the according charset combobox.
	 * 
	 * Can be overridden to return a charset from a different combobox or a hard-coded
	 * value, depending on the format.
	 * 
	 * @return the target charset.
	 */
	protected String getTargetCharset() {
		return ((ComboboxStringOption) ConfigComboboxModel.getModel(Config.CHARSET_EXPORT_MID).getSelectedItem()).getIdentifier();
	}
	
	/**
	 * Creates a modified copy of the loaded sequence.
	 * Adds a meta event for the target charset.
	 * Removes meta events for all other charset switches.
	 * Removes meta events for key presses and key releases.
	 * 
	 * @return copied and modified MIDI Sequence.
	 * @throws InvalidMidiDataException if the new MIDI (copied) sequence cannot be created.
	 */
	protected Sequence cloneSequence() throws InvalidMidiDataException {
		
		// needed for derived classes
		targetCharset = getTargetCharset();
		sourceCharset = SequenceCreator.getCharset();
		
		Sequence oldSeq = MidiDevices.getSequence();
		Sequence newSeq = new Sequence(oldSeq.getDivisionType(), oldSeq.getResolution());
		
		int trackNum = 0;
		TRACK:
		for (Track oldTrack : oldSeq.getTracks()) {
			
			Track newTrack = newSeq.createTrack();
			
			// add a charset event
			if (mustIncludeRp026Tags()) {
				if (0 == trackNum) {
					String csChange = "{@" + targetCharset + "}";
					byte[] data     = CharsetUtils.getBytesFromText(csChange, "US-ASCII");
					MetaMessage msg = new MetaMessage(MidiListener.META_LYRICS, data, data.length);
					newTrack.add(new MidiEvent(msg, 0));
				}
			}
			
			EVENT:
			for (int i=0; i < oldTrack.size(); i++) {
				MidiEvent   event = oldTrack.get(i);
				MidiMessage msg   = event.getMessage();
				
				// illegal short message? - warn and ignore
				if (msg instanceof ShortMessage) {
					int data1 = ((ShortMessage) msg).getData1();
					int data2 = ((ShortMessage) msg).getData2();
					if (data1 > 0x7F || data2 > 0x7F) {
						int channel  = ((ShortMessage) msg).getChannel();
						byte[] bytes = ((ShortMessage) msg).getMessage();
						String msgStr = "";
						for (byte b : bytes)
							msgStr += String.format(" %02X", b);
						exportResult.addWarning(trackNum, event.getTick(), (byte) channel, Dict.get(Dict.WARNING_ILLEGAL_SHORT_MESSAGE));
						exportResult.setDetailsOfLastWarning(msgStr);
						continue EVENT;
					}
				}
				
				// manipulate some meta messages
				if (msg instanceof MetaMessage) {
					int    type = ((MetaMessage) msg).getType();
					byte[] data = ((MetaMessage) msg).getData();
					
					// ignore marker messages created by the SequenceCreator
					if (MidiListener.META_MARKER == type
					  && data.length > 0
					  && data.length <= 16) {
						continue EVENT;
					}
					
					// convert charset of text-based messages
					else if (type >= 0x01 && type <= 0x0F) {
						String text = CharsetUtils.getTextFromBytes(data, sourceCharset, fileCharset);
						
						// remove meta info, if needed
						if (MidiListener.META_LYRICS == type && ! mustIncludeRp026Tags()) {
							if (0 == trackNum && 0 == event.getTick() && text.startsWith("{#"))
								continue EVENT;
						}
						
						// handle charset switches in the sequence
						event = convertCharset(event, text, fileCharset, type, event.getTick(), trackNum);
						if (MidiListener.META_TEXT == type || MidiListener.META_LYRICS == type) {
							String newCharset = CharsetUtils.findCharsetSwitch(text);
							if (newCharset != null) {
								
								// remember the new charset
								fileCharset = newCharset;
								
								// remove charset switch from the message
								event = removeCharsetSwitch(event, text, type, event.getTick(), trackNum);
								if (null == event) {
									continue EVENT;
								}
							}
						}
					}
				}
				
				// copy the event
				newTrack.add(event);
			}
			trackNum++;
		}
		
		return newSeq;
	}
	
	/**
	 * Determins if RP-026 tags (charset switch or meta info) shall be included into
	 * the exported MIDI file or not.
	 * 
	 * Returns **true** per default but can be overridden by a derived class.
	 * 
	 * @return **true** to indicate that RP-026 tags should be used.
	 */
	protected boolean mustIncludeRp026Tags() {
		return true;
	}
	
	/**
	 * Converts the text of a META message into the target charset.
	 * 
	 * @param oldEvent     The original MIDI event.
	 * @param text         The text of the meta message.
	 * @param fileCharset  Last charset, specified in the MIDI stream.
	 * @param type         META message type.
	 * @param tick         Tickstamp.
	 * @param trackNum     Track number.
	 * @return the new meta event with the converted text or the original
	 *         event, if the text cannot be converted.
	 */
	private MidiEvent convertCharset(MidiEvent oldEvent, String text, String fileCharset, int type, long tick, int trackNum) {
		
		// convert text
		byte[] data = CharsetUtils.getBytesFromText(text, targetCharset);
		try {
			MetaMessage newMsg = new MetaMessage(type, data, data.length);
			return new MidiEvent(newMsg, tick);
		}
		catch (InvalidMidiDataException e) {
			exportResult.addWarning(trackNum, tick, null, e.getMessage());
			
			return oldEvent;
		}
	}
	
	/**
	 * Removes all charset switch tags from the event's text.
	 * 
	 * @param oldEvent     The original MIDI event.
	 * @param text         The text of the meta message.
	 * @param type         META message type.
	 * @param tick         Tickstamp.
	 * @param trackNum     Track number.
	 * @return the new event with the changed text or **null** if the text is
	 *         empty after the changes.
	 */
	private MidiEvent removeCharsetSwitch(MidiEvent oldEvent, String text, int type, long tick, int trackNum) {
		
		// replace text recursively (don't allow nested tags to evaluate to new tags)
		// e.g. @{UT@{UTF-16}F-8} would otherwise evaluate to @{UTF-16}.
		int length     = text.length();
		int lastLength = -1;
		while (length != lastLength) {
			lastLength = length;
			text       = text.replaceAll("\\{@[\\w\\-]+\\}", "");
			length     = text.length();
		}
		
		// text empty after the replacement? - than remove it completely.
		if ("".equals(text)) {
			return null;
		}
		
		// replace the event with a new event using the new text
		byte[] data = CharsetUtils.getBytesFromText(text, targetCharset);
		try {
			MetaMessage newMsg = new MetaMessage(type, data, data.length);
			return new MidiEvent(newMsg, tick);
		}
		catch (InvalidMidiDataException e) {
			exportResult.addWarning(trackNum, tick, null, e.getMessage());
			
			return oldEvent;
		}
	}
}
