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
import java.lang.reflect.InvocationTargetException;
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
import org.midica.file.read.MidiParser;
import org.midica.file.read.ParseException;
import org.midica.ui.model.IMessageType;
import org.midica.ui.model.SingleMessage;
import org.midica.ui.model.MessageTreeNode;
import org.midica.ui.model.MidicaTreeModel;
import org.midica.ui.model.MidicaTreeNode;

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
	 * 
	 * @throws InterruptedException       on interruptions while waiting for the event dispatching thread.
	 * @throws InvocationTargetException  on exceptions.
	 */
	@BeforeAll
	static void setUpBeforeClass() throws InvocationTargetException, InterruptedException {
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
		String                  filename = "rpn-data-entry";
		ArrayList<List<Number>> events   = new ArrayList<>();
		int  track = 0;
		long tick  = 10;
		
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
		
		ArrayList<SingleMessage> messages = parseMidiFile(filename, events);
		int i = 0;
		
		// data entry without setting RPN
		assertEquals( "[127,127] Not Set",                   getMsgNodeText(messages,   i, 1) );
		assertEquals( "[6] MSB (Data Entry)",                getMsgNodeText(messages,   i, 0) );
		assertEquals( "[127,127] Not Set",                   getMsgNodeText(messages, ++i, 1) );
		assertEquals( "[38] LSB (Data Entry)",               getMsgNodeText(messages,   i, 0) );
		
		// pitch bend sensitivity
		assertEquals( "RPN (Registered Parameter)",          getMsgNodeText(messages, ++i, 2) );
		assertEquals( "[101] MSB (RPN)",                     getMsgNodeText(messages,   i, 1) );
		assertEquals( "[0] 0x00",                            getMsgNodeText(messages,   i, 0) );
		assertEquals( "RPN (Registered Parameter)",          getMsgNodeText(messages, ++i, 2) );
		assertEquals( "[100] LSB (RPN)",                     getMsgNodeText(messages,   i, 1) );
		assertEquals( "[0] 0x00",                            getMsgNodeText(messages,   i, 0) );
		assertEquals( "[0,0] Pitch Bend Sensitivity",        getMsgNodeText(messages, ++i, 1) );
		assertEquals( "[6] MSB (Data Entry)",                getMsgNodeText(messages,   i, 0) );
		assertEquals( "[0,0] Pitch Bend Sensitivity",        getMsgNodeText(messages, ++i, 1) );
		assertEquals( "[38] LSB (Data Entry)",               getMsgNodeText(messages,   i, 0) );
		
		// master fine tuning
		assertEquals( "RPN (Registered Parameter)",          getMsgNodeText(messages, ++i, 2) );
		assertEquals( "[101] MSB (RPN)",                     getMsgNodeText(messages,   i, 1) );
		assertEquals( "[0] 0x00",                            getMsgNodeText(messages,   i, 0) );
		assertEquals( "RPN (Registered Parameter)",          getMsgNodeText(messages, ++i, 2) );
		assertEquals( "[100] LSB (RPN)",                     getMsgNodeText(messages,   i, 1) );
		assertEquals( "[1] 0x01",                            getMsgNodeText(messages,   i, 0) );
		assertEquals( "[0,1] Master Fine Tuning (in Cents)", getMsgNodeText(messages, ++i, 1) );
		assertEquals( "[6] MSB (Data Entry)",                getMsgNodeText(messages,   i, 0) );
		assertEquals( "[0,1] Master Fine Tuning (in Cents)", getMsgNodeText(messages, ++i, 1) );
		assertEquals( "[38] LSB (Data Entry)",               getMsgNodeText(messages,   i, 0) );
		
		// Data Increment (on a valid RPN)
		assertEquals( "[96] Data Button Increment",          getMsgNodeText(messages, ++i, 1) );
		assertEquals( "[0,1] Master Fine Tuning (in Cents)", getMsgNodeText(messages,   i, 0) );
		
		// Disable RPN
		assertEquals( "RPN (Registered Parameter)",          getMsgNodeText(messages, ++i, 2) );
		assertEquals( "[101] MSB (RPN)",                     getMsgNodeText(messages,   i, 1) );
		assertEquals( "[127] 0x7F",                          getMsgNodeText(messages,   i, 0) );
		assertEquals( "RPN (Registered Parameter)",          getMsgNodeText(messages, ++i, 2) );
		assertEquals( "[100] LSB (RPN)",                     getMsgNodeText(messages,   i, 1) );
		assertEquals( "[127] 0x7F",                          getMsgNodeText(messages,   i, 0) );
		
		// Data Increment (on an unset RPN)
		assertEquals( "[96] Data Button Increment",          getMsgNodeText(messages, ++i, 1) );
		assertEquals( "[127,127] Not Set",                   getMsgNodeText(messages,   i, 0) );
		
		// Data Increment (on an unknown RPN)
		assertEquals( "RPN (Registered Parameter)",          getMsgNodeText(messages, ++i, 2) );
		assertEquals( "[101] MSB (RPN)",                     getMsgNodeText(messages,   i, 1) );
		assertEquals( "[18] 0x12",                           getMsgNodeText(messages,   i, 0) );
		assertEquals( "RPN (Registered Parameter)",          getMsgNodeText(messages, ++i, 2) );
		assertEquals( "[100] LSB (RPN)",                     getMsgNodeText(messages,   i, 1) );
		assertEquals( "[52] 0x34",                           getMsgNodeText(messages,   i, 0) );
		assertEquals( "[96] Data Button Increment",          getMsgNodeText(messages, ++i, 1) );
		assertEquals( "[18,52] Unknown",                     getMsgNodeText(messages,   i, 0) );
		
		// Data Increment (on an unknown NRPN)
		assertEquals( "NRPN (Non-Registered Parameter)",     getMsgNodeText(messages, ++i, 2) );
		assertEquals( "[99] MSB (NRPN)",                     getMsgNodeText(messages,   i, 1) );
		assertEquals( "[10] 0x0A",                           getMsgNodeText(messages,   i, 0) );
		assertEquals( "NRPN (Non-Registered Parameter)",     getMsgNodeText(messages, ++i, 2) );
		assertEquals( "[98] LSB (NRPN)",                     getMsgNodeText(messages,   i, 1) );
		assertEquals( "[11] 0x0B",                           getMsgNodeText(messages,   i, 0) );
		assertEquals( "[96] Data Button Increment",          getMsgNodeText(messages, ++i, 1) );
		assertEquals( "[10,11] Unknown",                     getMsgNodeText(messages,   i, 0) );
		
		// Data Decrement (on an unknown NRPN)
		assertEquals( "[97] Data Button Decrement",          getMsgNodeText(messages, ++i, 1) );
		assertEquals( "[10,11] Unknown",                     getMsgNodeText(messages,   i, 0) );
	}
	
	/**
	 * Tests for analyzing bank-select, program-change and note-on messages.
	 * 
	 * @throws InvalidMidiDataException if something went wrong.
	 * @throws ParseException if something went wrong.
	 * @throws IOException if something went wrong.
	 */
	@Test
	void testBankProgramNote() throws InvalidMidiDataException, IOException, ParseException {
		String                  filename = "bank-program-note";
		ArrayList<List<Number>> events   = new ArrayList<>();
		int  track = 0;
		long tick  = 10;
		
		// c in channel 0, clave in channel 9
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0x90, 0x3C, 0x7F) ); // Note-On
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0x80, 0x3C, 0x00) ); // Note-Off
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0x99, 0x4B, 0x7F) ); // Note-On
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0x89, 0x4B, 0x00) ); // Note-Off
		
		// VIOLIN, c in channel 0; ROOM, clave in channel 9
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xC0, 0x28, 0x00) ); // Prog Change: VIOLIN
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0x90, 0x3C, 0x7F) ); // Note-On
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0x80, 0x3C, 0x00) ); // Note-Off
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xC9, 0x08, 0x00) ); // Prog Change: ROOM
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0x99, 0x4B, 0x7F) ); // Note-On
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0x89, 0x4B, 0x00) ); // Note-Off
		
		// Ch0: Bank Select: MSB=10, LSB=20, Bank=1300
		// Program: CELESTA=8
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xB0, 0x00, 0x0A) ); // Bank Select MSB
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xB0, 0x20, 0x14) ); // Bank Select LSB
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xC0, 0x08, 0x00) ); // Prog Change: CELESTA
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0x90, 0x3C, 0x7F) ); // Note-On
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0x80, 0x3C, 0x00) ); // Note-Off
		
		// Ch9: Bank Select: MSB=120, LSB=0, Bank=1300
		// Program: ELECTRONIC=24
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xB9, 0x00, 0x78) ); // Bank Select MSB
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xB9, 0x20, 0x00) ); // Bank Select LSB
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xC9, 0x18, 0x00) ); // Prog Change: ELECTRONIC
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0x99, 0x4B, 0x7F) ); // Note-On
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0x89, 0x4B, 0x00) ); // Note-Off
		
		// parse and get tree
		parseMidiFile(filename, events);
		MidicaTreeModel model = (MidicaTreeModel) SequenceAnalyzer.getSequenceInfo().get("banks_per_channel");
		model.postprocess();
		MidicaTreeNode  rootNode = (MidicaTreeNode) model.getRoot();
		assertEquals( "Per Channel", rootNode.getName() );
		
		// channel 0
		MidicaTreeNode nodeCh0 = (MidicaTreeNode) rootNode.getChildAt( 0 );
		assertEquals( "Channel 0", nodeCh0.getName() );
		
		// channel 0, bank 0
		MidicaTreeNode nodeCh0bank0 = (MidicaTreeNode) nodeCh0.getChildAt( 0 );
		assertEquals( "Bank 0, MSB: 0, LSB: 0", nodeCh0bank0.getName() );
		
		// channel 0, bank 0, program 0
		MidicaTreeNode nodeCh0bank0prog0 = (MidicaTreeNode) nodeCh0bank0.getChildAt( 0 );
		assertEquals( "ACOUSTIC_GRAND_PIANO", nodeCh0bank0prog0.getName() );
		
		// channel 0, bank 0, program 0, note c
		MidicaTreeNode nodeCh0bank0prog0c = (MidicaTreeNode) nodeCh0bank0prog0.getChildAt( 0 );
		assertEquals( "c", nodeCh0bank0prog0c.getName() );
		
		// channel 0, bank 0, program 40
		MidicaTreeNode nodeCh0bank0prog40 = (MidicaTreeNode) nodeCh0bank0.getChildAt( 1 );
		assertEquals( "VIOLIN", nodeCh0bank0prog40.getName() );
		
		// channel 0, bank 0, program 0, note c
		MidicaTreeNode nodeCh0bank0prog40c = (MidicaTreeNode) nodeCh0bank0prog40.getChildAt( 0 );
		assertEquals( "c", nodeCh0bank0prog40c.getName() );
		
		// channel 0, bank 1300
		MidicaTreeNode nodeCh0bank1300 = (MidicaTreeNode) nodeCh0.getChildAt( 1 );
		assertEquals( "Bank 1300, MSB: 10, LSB: 20", nodeCh0bank1300.getName() );
		
		// channel 0, bank 1300, program 8
		MidicaTreeNode nodeCh0bank1300prog8 = (MidicaTreeNode) nodeCh0bank1300.getChildAt( 0 );
		assertEquals( "CELESTA", nodeCh0bank1300prog8.getName() );
		
		// channel 0, bank 1300, program 8, note c
		MidicaTreeNode nodeCh0bank1300prog8c = (MidicaTreeNode) nodeCh0bank1300prog8.getChildAt( 0 );
		assertEquals( "c", nodeCh0bank1300prog8c.getName() );
		
		// channel 9
		MidicaTreeNode nodeCh9 = (MidicaTreeNode) rootNode.getChildAt( 1 );
		assertEquals( "Channel 9", nodeCh9.getName() );
		
		// channel 9, bank 0
		MidicaTreeNode nodeCh9bank0 = (MidicaTreeNode) nodeCh9.getChildAt( 0 );
		assertEquals( "Bank 0, MSB: 0, LSB: 0", nodeCh9bank0.getName() );
		
		// channel 9, bank 0, program 0
		MidicaTreeNode nodeCh9bank0prog0 = (MidicaTreeNode) nodeCh9bank0.getChildAt( 0 );
		assertEquals( "STANDARD", nodeCh9bank0prog0.getName() );
		
		// channel 9, bank 0, program 0, note clave
		MidicaTreeNode nodeCh9bank0prog0cla = (MidicaTreeNode) nodeCh9bank0prog0.getChildAt( 0 );
		assertEquals( "clave", nodeCh9bank0prog0cla.getName() );
		
		// channel 9, bank 0, program 8
		MidicaTreeNode nodeCh9bank0prog8 = (MidicaTreeNode) nodeCh9bank0.getChildAt( 1 );
		assertEquals( "ROOM", nodeCh9bank0prog8.getName() );
		
		// channel 9, bank 0, program 8, note clave
		MidicaTreeNode nodeCh9bank0prog8cla = (MidicaTreeNode) nodeCh9bank0prog8.getChildAt( 0 );
		assertEquals( "clave", nodeCh9bank0prog8cla.getName() );
		
		// channel 9, bank 15360
		MidicaTreeNode nodeCh9bank15360 = (MidicaTreeNode) nodeCh9.getChildAt( 1 );
		assertEquals( "Bank 15360, MSB: 120, LSB: 0", nodeCh9bank15360.getName() );
		
		// channel 9, bank 15360, program 24
		MidicaTreeNode nodeCh9bank15360prog24 = (MidicaTreeNode) nodeCh9bank15360.getChildAt( 0 );
		assertEquals( "ELECTRONIC", nodeCh9bank15360prog24.getName() );
		
		// channel 9, bank 15360, program 24, note clave
		MidicaTreeNode nodeCh9bank15360prog24cla = (MidicaTreeNode) nodeCh9bank15360prog24.getChildAt( 0 );
		assertEquals( "clave", nodeCh9bank15360prog24cla.getName() );
	}
	
	/**
	 * Tests for analyzing pitch bend related messages.
	 * This includes:
	 * 
	 * - pitch bend sensitivity (RPN, Data Entry / Increment / Decrement)
	 * - pitch bend change
	 * 
	 * @throws InvalidMidiDataException if something went wrong.
	 * @throws ParseException if something went wrong.
	 * @throws IOException if something went wrong.
	 */
	@Test
	void testPitchBend() throws InvalidMidiDataException, IOException, ParseException {
		String                  filename = "pitch-bend";
		ArrayList<List<Number>> events   = new ArrayList<>();
		int  track = 0;
		long tick  = 10;

		// pitch bend
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xE0, 0x00, 0x00) ); // minimum
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xE0, 0x00, 0x40) ); // neutral
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xE0, 0x7F, 0x7F) ); // maximum
		
		// pitch bend sensitivity --> 0x03 0x7F
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xB0, 0x65, 0x00) ); // RPN MSB
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xB0, 0x64, 0x00) ); // RPN LSB
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xB0, 0x06, 0x03) ); // Data Entry MSB
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xB0, 0x26, 0x7F) ); // Data Entry LSB
		
		// pitch bend
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xE0, 0x00, 0x00) ); // minimum
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xE0, 0x00, 0x40) ); // neutral
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xE0, 0x7F, 0x7F) ); // maximum
		
		// sensitivity increment
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xB0, 0x60, 0x00) );
		
		// pitch bend
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xE0, 0x00, 0x00) ); // minimum
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xE0, 0x00, 0x40) ); // neutral
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xE0, 0x7F, 0x7F) ); // maximum
		
		// sensitivity increment
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xB0, 0x60, 0x00) );
		
		// pitch bend
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xE0, 0x00, 0x00) ); // minimum
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xE0, 0x00, 0x40) ); // neutral
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xE0, 0x7F, 0x7F) ); // maximum
		
		// sensitivity decrement
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xB0, 0x61, 0x00) );
		
		// pitch bend
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xE0, 0x00, 0x00) ); // minimum
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xE0, 0x00, 0x40) ); // neutral
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xE0, 0x7F, 0x7F) ); // maximum
		
		// sensitivity decrement
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xB0, 0x61, 0x00) );
		
		// pitch bend
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xE0, 0x00, 0x00) ); // minimum
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xE0, 0x00, 0x40) ); // neutral
		events.add( Arrays.asList(SHORT_MSG, track, tick++, 0xE0, 0x7F, 0x7F) ); // maximum
		
		
		ArrayList<SingleMessage> messages = parseMidiFile(filename, events);
		int i = 0;
		
		// pitch bend (using a default range of +/- 2.0)
		assertEquals( "-2.0", getMsgSummary(messages, i++) ); // 0x0000
		assertEquals( "0.0",  getMsgSummary(messages, i++) ); // 0x4000
		assertEquals( "+2.0", getMsgSummary(messages, i++) ); // 0x7F7F
		
		// pitch bend sensitivity
		assertEquals(   "0", getMsgSummary(messages, i++) ); // RPN MSB
		assertEquals(   "0", getMsgSummary(messages, i++) ); // RPN LSB
		assertEquals(   "3", getMsgSummary(messages, i++) ); // Data Entry MSB
		assertEquals( "127", getMsgSummary(messages, i++) ); // Data Entry LSB
		
		// pitch bend (using a range of +/- 4.0 minus 1 cent)
		assertEquals( "-3.9921875", getMsgSummary(messages, i++) ); // 0x0000
		assertEquals( "0.0",        getMsgSummary(messages, i++) ); // 0x4000
		assertEquals( "+3.9921875", getMsgSummary(messages, i++) ); // 0x7F7F
		
		// sensitivity increment
		assertEquals( null, getMsgSummary(messages, i++) );
		
		// pitch bend (using a range of +/- 4.0)
		assertEquals( "-4.0", getMsgSummary(messages, i++) ); // 0x0000
		assertEquals( "0.0",  getMsgSummary(messages, i++) ); // 0x4000
		assertEquals( "+4.0", getMsgSummary(messages, i++) ); // 0x7F7F
		
		// sensitivity increment
		assertEquals( null, getMsgSummary(messages, i++) );
		
		// pitch bend (using a range of +/- 4.0 plus 1 cent)
		assertEquals( "-4.0078125", getMsgSummary(messages, i++) ); // 0x0000
		assertEquals( "0.0",        getMsgSummary(messages, i++) ); // 0x4000
		assertEquals( "+4.0078125", getMsgSummary(messages, i++) ); // 0x7F7F
		
		// sensitivity decrement
		assertEquals( null, getMsgSummary(messages, i++) );
		
		// pitch bend (using a range of +/- 4.0)
		assertEquals( "-4.0", getMsgSummary(messages, i++) ); // 0x0000
		assertEquals( "0.0",  getMsgSummary(messages, i++) ); // 0x4000
		assertEquals( "+4.0", getMsgSummary(messages, i++) ); // 0x7F7F
		
		// sensitivity decrement
		assertEquals( null, getMsgSummary(messages, i++) );
		
		// pitch bend (using a range of +/- 4.0 minus 1 cent)
		assertEquals( "-3.9921875", getMsgSummary(messages, i++) ); // 0x0000
		assertEquals( "0.0",        getMsgSummary(messages, i++) ); // 0x4000
		assertEquals( "+3.9921875", getMsgSummary(messages, i++) ); // 0x7F7F
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
	private ArrayList<SingleMessage> parseMidiFile(String name, ArrayList<List<Number>> events) throws IOException, InvalidMidiDataException, ParseException {
		
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
		
		return (ArrayList<SingleMessage>) SequenceAnalyzer.getSequenceInfo().get("messages");
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
			int   type    = (int)  event.get( 0 );
			int   track   = (int)  event.get( 1 );
			long  tick    = (long) event.get( 2 );
			int[] content = new int[ event.size() - 3 ];
			for (int i = 3; i < event.size(); i++) {
				int byteAsInt = (Integer) event.get( i );
				content[ i - 3 ] = byteAsInt;
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
	 * Searches a message tree node and returns it's text.
	 * 
	 * @param messages    MIDI message list as created by the SequenceAnalyzer.
	 * @param index       The index of the message we are interested in.
	 * @param stepsUp     The number of steps to go up from the leaf node towards the root node.
	 * @return tree node text.
	 */
	private static String getMsgNodeText(ArrayList<SingleMessage> messages, int index, int stepsUp) {
		
		MessageTreeNode currentNode = (MessageTreeNode) messages.get( index ).getOption( IMessageType.OPT_LEAF_NODE );
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
	
	/**
	 * Searches and returns a message summary.
	 * 
	 * @param messages    MIDI message list as created by the SequenceAnalyzer.
	 * @param index       The index of the message we are interested in.
	 * @return summary text.
	 */
	private static String getMsgSummary(ArrayList<SingleMessage> messages, int index) {
		return (String) messages.get( index ).getOption( IMessageType.OPT_SUMMARY );
	}
}
