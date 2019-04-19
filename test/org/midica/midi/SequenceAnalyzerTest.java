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
import org.midica.ui.model.MessageTreeNode;
import org.midica.ui.model.MidicaTreeModel;

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
	 * Tests for analyzing (N)RPN and Data Entry / Increment / Decrement messages.
	 * 
	 * @throws InvalidMidiDataException if something went wrong.
	 * @throws ParseException if something went wrong.
	 * @throws IOException if something went wrong.
	 */
	@Test
	void testRpnNrpnData() throws InvalidMidiDataException, IOException, ParseException {
		ArrayList<List<Number>> events = new ArrayList<>();
		int  track = 0;
		long tick  = 10;
		
		// RPN / Data Entry
		String filename = "rpn-data-entry";
		
		// data entry without setting RPN
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xB0, 0x06, 0x02) ); // Data Entry MSB
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xB0, 0x26, 0x01) ); // Data Entry LSB
		
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
		
		// Data Increment (on a valid RPN)
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xB1, 0x60, 0x00) ); // Data Increment
		
		// Disable RPN
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xB0, 0x65, 0x7F) ); // RPN MSB
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xB0, 0x64, 0x7F) ); // RPN LSB
		
		// Data Increment (on an unset RPN)
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xB0, 0x60, 0x00) ); // Data Increment
		
		// Data Increment (on an unknown RPN)
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xB0, 0x65, 0x12) ); // RPN MSB
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xB0, 0x64, 0x34) ); // RPN LSB
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xB0, 0x60, 0x00) ); // Data Increment
		
		// Data Increment (on an unknown NRPN)
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xB0, 0x63, 0x0A) ); // NRPN MSB
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xB0, 0x62, 0x0B) ); // NRPN LSB
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xB0, 0x60, 0x00) ); // Data Increment
		
		// Data Decrement (on an unknown NRPN)
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xB0, 0x61, 0x00) ); // Data Decrement
		
		ArrayList<MessageDetail> messages = parseMidiFile(filename, events);
		int i = 0;
		
		// data entry without setting RPN
		assertEquals( "[127,127] Not Set",                   getNodeText(messages,   i, 1) );
		assertEquals( "[6] MSB (Data Entry)",                getNodeText(messages,   i, 0) );
		assertEquals( "[127,127] Not Set",                   getNodeText(messages, ++i, 1) );
		assertEquals( "[38] LSB (Data Entry)",               getNodeText(messages,   i, 0) );
		
		// pitch bend sensitivity
		assertEquals( "RPN (Registered Parameter)",          getNodeText(messages, ++i, 2) );
		assertEquals( "[101] MSB (RPN)",                     getNodeText(messages,   i, 1) );
		assertEquals( "[0] 0x00",                            getNodeText(messages,   i, 0) );
		assertEquals( "RPN (Registered Parameter)",          getNodeText(messages, ++i, 2) );
		assertEquals( "[100] LSB (RPN)",                     getNodeText(messages,   i, 1) );
		assertEquals( "[0] 0x00",                            getNodeText(messages,   i, 0) );
		assertEquals( "[0,0] Pitch Bend Sensitivity",        getNodeText(messages, ++i, 1) );
		assertEquals( "[6] MSB (Data Entry)",                getNodeText(messages,   i, 0) );
		assertEquals( "[0,0] Pitch Bend Sensitivity",        getNodeText(messages, ++i, 1) );
		assertEquals( "[38] LSB (Data Entry)",               getNodeText(messages,   i, 0) );
		
		// master fine tuning
		assertEquals( "RPN (Registered Parameter)",          getNodeText(messages, ++i, 2) );
		assertEquals( "[101] MSB (RPN)",                     getNodeText(messages,   i, 1) );
		assertEquals( "[0] 0x00",                            getNodeText(messages,   i, 0) );
		assertEquals( "RPN (Registered Parameter)",          getNodeText(messages, ++i, 2) );
		assertEquals( "[100] LSB (RPN)",                     getNodeText(messages,   i, 1) );
		assertEquals( "[1] 0x01",                            getNodeText(messages,   i, 0) );
		assertEquals( "[0,1] Master Fine Tuning (in Cents)", getNodeText(messages, ++i, 1) );
		assertEquals( "[6] MSB (Data Entry)",                getNodeText(messages,   i, 0) );
		assertEquals( "[0,1] Master Fine Tuning (in Cents)", getNodeText(messages, ++i, 1) );
		assertEquals( "[38] LSB (Data Entry)",               getNodeText(messages,   i, 0) );
		
		// Data Increment (on a valid RPN)
		assertEquals( "[96] Data Button Increment",          getNodeText(messages, ++i, 1) );
		assertEquals( "[0,1] Master Fine Tuning (in Cents)", getNodeText(messages,   i, 0) );
		
		// Disable RPN
		assertEquals( "RPN (Registered Parameter)",          getNodeText(messages, ++i, 2) );
		assertEquals( "[101] MSB (RPN)",                     getNodeText(messages,   i, 1) );
		assertEquals( "[127] 0x7F",                          getNodeText(messages,   i, 0) );
		assertEquals( "RPN (Registered Parameter)",          getNodeText(messages, ++i, 2) );
		assertEquals( "[100] LSB (RPN)",                     getNodeText(messages,   i, 1) );
		assertEquals( "[127] 0x7F",                          getNodeText(messages,   i, 0) );
		
		// Data Increment (on an unset RPN)
		assertEquals( "[96] Data Button Increment",          getNodeText(messages, ++i, 1) );
		assertEquals( "[127,127] Not Set",                   getNodeText(messages,   i, 0) );
		
		// Data Increment (on an unknown RPN)
		assertEquals( "RPN (Registered Parameter)",          getNodeText(messages, ++i, 2) );
		assertEquals( "[101] MSB (RPN)",                     getNodeText(messages,   i, 1) );
		assertEquals( "[18] 0x12",                           getNodeText(messages,   i, 0) );
		assertEquals( "RPN (Registered Parameter)",          getNodeText(messages, ++i, 2) );
		assertEquals( "[100] LSB (RPN)",                     getNodeText(messages,   i, 1) );
		assertEquals( "[52] 0x34",                           getNodeText(messages,   i, 0) );
		assertEquals( "[96] Data Button Increment",          getNodeText(messages, ++i, 1) );
		assertEquals( "[18,52] Unknown",                     getNodeText(messages,   i, 0) );
		
		// Data Increment (on an unknown NRPN)
		assertEquals( "NRPN (Non-Registered Parameter)",     getNodeText(messages, ++i, 2) );
		assertEquals( "[99] MSB (NRPN)",                     getNodeText(messages,   i, 1) );
		assertEquals( "[10] 0x0A",                           getNodeText(messages,   i, 0) );
		assertEquals( "NRPN (Non-Registered Parameter)",     getNodeText(messages, ++i, 2) );
		assertEquals( "[98] LSB (NRPN)",                     getNodeText(messages,   i, 1) );
		assertEquals( "[11] 0x0B",                           getNodeText(messages,   i, 0) );
		assertEquals( "[96] Data Button Increment",          getNodeText(messages, ++i, 1) );
		assertEquals( "[10,11] Unknown",                     getNodeText(messages,   i, 0) );
		
		// Data Decrement (on an unknown NRPN)
		assertEquals( "[97] Data Button Decrement",          getNodeText(messages, ++i, 1) );
		assertEquals( "[10,11] Unknown",                     getNodeText(messages,   i, 0) );
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
		
		// postprocess tree model and connect nodes with each other
		MidicaTreeModel model = (MidicaTreeModel) SequenceAnalyzer.getSequenceInfo().get("msg_tree_model");
		model.postprocess();
		
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
		ArrayList<Track> tracks = new ArrayList<>();
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
	 * Searches a tree node and returns it's text.
	 * 
	 * @param messages    MIDI message list as created by the SequenceAnalyzer.
	 * @param index       The index of the message we are interested in.
	 * @param stepsUp     The number of steps to go up from the leaf node towards the root node.
	 * @return tree node text.
	 */
	private static String getNodeText(ArrayList<MessageDetail> messages, int index, int stepsUp) {
		
		MessageTreeNode currentNode = (MessageTreeNode) messages.get( index ).getOption("leaf_node");
		for (int i = 0; i < stepsUp; i++) {
			currentNode = (MessageTreeNode) currentNode.getParent();
		}
		
		String text;
		if (null == currentNode.getNumber())
			text = currentNode.getName();
		else
			text = "[" + currentNode.getNumber() + "] " + currentNode.getName();
		
		return text;
	}
}
