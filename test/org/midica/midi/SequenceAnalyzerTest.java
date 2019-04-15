/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.midi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.midica.TestUtil;
import org.midica.file.MidiParser;
import org.midica.file.ParseException;
import org.midica.ui.model.MessageDetail;

/**
 * This is the test class for {@link org.midica.midi.SequenceAnalyzer}.
 * 
 * @author Jan Trukenmüller
 */
public class SequenceAnalyzerTest {
	
	// set to true temporarily if MIDI test files must be created or changed
	private static final boolean RECREATE_MIDI_FILES = false;
	
	private static final int SHORT_MSG = 0;
	private static final int META_MSG  = 1;
	private static final int SYSEX_MSG = 2;
	
	private static final MidiParser parser = new MidiParser();
	
	/**
	 * Initializes midica in test mode.
	 */
	@BeforeAll
	static void setUpBeforeClass() {
		TestUtil.initMidica();
	}
	
	/**
	 * Tests for analyzing (and creating) full MIDI files.
	 * 
	 * @throws InvalidMidiDataException if something went wrong.
	 * @throws ParseException if something went wrong.
	 * @throws IOException if something went wrong.
	 */
	@Test
	void testAnalyze() throws InvalidMidiDataException, IOException, ParseException {
		ArrayList<List<Number>> events = new ArrayList<List<Number>>();
		int  track = 0;
		long tick  = 10;
		
		// RPN / Data Entry
		if (RECREATE_MIDI_FILES) {
			// pitch bend sensitivity --> 0x02 0x01
			events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xB0, 0x65, 0x00) ); // RPN MSB
			events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xB0, 0x64, 0x00) ); // RPN LSB
			events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xB0, 0x06, 0x02) ); // Data Entry MSB
			events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xB0, 0x26, 0x01) ); // Data Entry LSB
			
			// master fine tuning --> 0x02 0x01
			events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xB1, 0x65, 0x00) ); // RPN MSB
			events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xB1, 0x64, 0x01) ); // RPN LSB
			events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xB1, 0x06, 0x02) ); // Data Entry MSB
			events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xB1, 0x26, 0x01) ); // Data Entry LSB
		}
		ArrayList<MessageDetail> messages = parseMidiFile("rpn_data_entry", events);
		assertEquals( "[101] MSB (RPN) (2)",       getNodeText(messages, 0) );
		assertEquals( "[100] LSB (RPN) (2)",       getNodeText(messages, 1) );
		assertEquals( "[6] MSB (Data Entry) (1)",  getNodeText(messages, 2) );
		assertEquals( "[38] LSB (Data Entry) (1)", getNodeText(messages, 3) );
		assertEquals( "[101] MSB (RPN) (2)",       getNodeText(messages, 4) );
		assertEquals( "[100] LSB (RPN) (2)",       getNodeText(messages, 5) );
		assertEquals( "[6] MSB (Data Entry) (1)",  getNodeText(messages, 6) );
		assertEquals( "[38] LSB (Data Entry) (1)", getNodeText(messages, 7) );
	}
	
	/**
	 * Parses the file, or creates and parses it, if requested.
	 * 
	 * @param name      File name without directory and extension.
	 * @param events    Data to be transformed into MIDI events for file creation.
	 * @return the message list, created by the {@link SequenceAnalyzer}.
	 * @throws IOException if something goes wrong.
	 * @throws InvalidMidiDataException if something goes wrong.
	 * @throws ParseException if something goes wrong.
	 */
	private ArrayList<MessageDetail> parseMidiFile(String name, ArrayList<List<Number>> events) throws IOException, InvalidMidiDataException, ParseException {
		
		File file = new File(TestUtil.getTestfileDirectory() + "midi" + File.separator + name + ".mid");
		
		// write file, if requested
		if (RECREATE_MIDI_FILES) {
			createMidiFile(file, events);
		}
		
		// parse
		parser.parse(file);
		
		return (ArrayList<MessageDetail>) SequenceAnalyzer.getSequenceInfo().get("messages");
	}
	
	/**
	 * Creates a MIDI file from the given event list.
	 * 
	 * @param file      File to be created.
	 * @param events    List of event data.
	 * @throws IOException if something goes wrong.
	 * @throws InvalidMidiDataException if something goes wrong.
	 * @throws ParseException if something goes wrong.
	 */
	private void createMidiFile(File file, ArrayList<List<Number>> events) throws IOException, InvalidMidiDataException, ParseException {
		
		// create sequence
		Sequence seq = new Sequence( Sequence.PPQ, 480 );
		ArrayList<Track> tracks = new ArrayList<Track>();
		for (List<Number> event : events) {
			
			// create message
			int  type  = (int)  event.get( 0 );
			int  track = (int)  event.get( 1 );
			long tick  = (long) event.get( 2 );
			byte[] content = new byte[ event.size() - 3 ];
			for (int i = 3; i < event.size(); i++) {
				int byteAsInt = (Integer) event.get( i );
				content[ i - 3 ] = (byte) byteAsInt;
			}
			
			// create new track(s), if necessary
			while (tracks.size() <= track) {
				tracks.add(seq.createTrack());
			}
			
			// process message
			MidiMessage msg;
			if (SHORT_MSG == type) {
				msg = new ShortMessage();
				int statusByte = content[0] & 0b1111_0000;
				int channel    = content[0] & 0b0000_1111;
				((ShortMessage) msg).setMessage(statusByte, channel, content[1], content[2]);
			}
			else {
				throw new ParseException("unknown message type");
			}
			MidiEvent e = new MidiEvent(msg, tick);
			tracks.get(track).add(e);
		}
		
		// write file
		MidiSystem.write( seq, 1, file );
	}
	
	/**
	 * Searches the tree node message/index combination and returns it's text.
	 * 
	 * @param messages    MIDI message list as created by the SequenceAnalyzer.
	 * @param index       The index of the message we are interested in.
	 * @return tree node text.
	 */
	private static String getNodeText(ArrayList<MessageDetail> messages, int index) {
		return messages.get( index ).getOption("leaf_node").toString();
	}
}
