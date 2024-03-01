/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file.read;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.TreeSet;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.swing.JComboBox;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.midica.Midica;
import org.midica.TestUtil;
import org.midica.config.Dict;
import org.midica.file.CharsetUtils;
import org.midica.file.read.MidicaPLParser;
import org.midica.file.read.ParseException;
import org.midica.file.read.StackTraceElement;
import org.midica.midi.KaraokeAnalyzer;
import org.midica.midi.SequenceAnalyzer;
import org.midica.midi.SequenceCreator;
import org.midica.ui.model.IMessageType;
import org.midica.ui.model.MidicaTreeModel;
import org.midica.ui.model.SingleMessage;

/**
 * This is the test class for {@link MidicaPLParser}.
 * 
 * @author Jan Trukenmüller
 */
class MidicaPLParserTest extends MidicaPLParser {
	
	private static JComboBox<?>[] cbxs;
	
	public MidicaPLParserTest() {
		super(true);
	}

	/**
	 * Initializes midica in test mode.
	 * 
	 * @throws InterruptedException       on interruptions while waiting for the event dispatching thread.
	 * @throws InvocationTargetException  on exceptions.
	 */
	@BeforeAll
	static void setUpBeforeClass() throws InvocationTargetException, InterruptedException {
		TestUtil.initMidica();
		
		cbxs = Midica.uiController.getView().getConfigComboboxes();
	}
	
	/**
	 * Test method parseDuration().
	 */
	@Test
	void testParseDuration() throws ParseException {
		
		// tests that are supposed to return something
		assertEquals(              60, parseDuration("/32")                );
		assertEquals(             120, parseDuration("/16")                );
		assertEquals(             240, parseDuration("/8")                 );
		assertEquals(             480, parseDuration("/4")                 );
		assertEquals(             960, parseDuration("/2")                 );
		assertEquals(            1920, parseDuration("/1")                 );
		assertEquals(            1920, parseDuration("*1")                 );
		assertEquals(            3840, parseDuration("*2")                 );
		assertEquals(            7680, parseDuration("*4")                 );
		assertEquals(           15360, parseDuration("*8")                 );
		assertEquals(           30720, parseDuration("*16")                );
		assertEquals(           61440, parseDuration("*32")                );
		assertEquals(             480, parseDuration("4")                  );
		assertEquals(             384, parseDuration("5")                  );
		assertEquals(           11520, parseDuration("*4.")                );
		assertEquals(           13440, parseDuration("*4..")               );
		assertEquals(           13440, parseDuration("*4..")               );
		assertEquals(           14400, parseDuration("*4...")              );
		assertEquals(            5120, parseDuration("*4t")                );
		assertEquals(            3413, parseDuration("*4tt")               );
		assertEquals(            2276, parseDuration("*4ttt")              );
		assertEquals(            4389, parseDuration("*4t7:4")             );
		assertEquals(            2508, parseDuration("*4t7:4t7:4")         );
		assertEquals(            2006, parseDuration("*4t7:4t7:4t5:4")     );
		assertEquals(             480, parseDuration("/4t7:4t4:7t7:4t4:7") );
		assertEquals(     2508 + 3413, parseDuration("*4t7:4t7:4+*4tt")    );
		assertEquals( 480 + 60 + 1920, parseDuration("4+32+1")             );
		
		// tests that are supposed to throw a parsing exception
		assertThrows( ParseException.class, () -> parseDuration("/64") );
		assertThrows( ParseException.class, () -> parseDuration("*64") );
		assertThrows( ParseException.class, () -> parseDuration("xyz") );
		assertThrows( ParseException.class, () -> parseDuration("/4+") );
	}
	
	/**
	 * Tests for parsing full source files that are expected to work.
	 * 
	 * The files are located in test/org/midica/testfiles/working
	 * @throws ParseException if something went wrong.
	 */
	@Test
	void testParseFilesWorking() throws ParseException {
		
		// expected tickstamps after parsing
		parse(getWorkingFile("globals"));
		assertEquals(  960, instruments.get(0).getCurrentTicks() );
		assertEquals( 1440, instruments.get(1).getCurrentTicks() );
		
		parse(getWorkingFile("empty"));
		assertEquals( 0, SequenceCreator.getSequence().getTickLength() );
		
		parse(getWorkingFile("nestable-block-with-m"));
		assertEquals( 480, instruments.get(0).getCurrentTicks() );
		
		parse(getWorkingFile("functions-and-blocks"));
		assertEquals( 11040, instruments.get(0).getCurrentTicks() );
		
		parse(getWorkingFile("alternative-note-names"));
		assertEquals( 12960, instruments.get(0).getCurrentTicks() );
		ArrayList<SingleMessage> messages = getMessagesByStatus("90");
		{
			int i = 0;
			// plain
			assertEquals( "0/0/90/c / 64",        messages.get(i++).toString() ); // bbb/c##/dbb = a/d/c ==> c/d/a
			assertEquals( "0/0/90/d / 64",        messages.get(i++).toString() );
			assertEquals( "0/0/90/a / 64",        messages.get(i++).toString() );
			assertEquals( "480/0/90/c / 64",      messages.get(i++).toString() ); // bbb/c##/dbb = a/d/c ==> c/d/a
			assertEquals( "480/0/90/d / 64",      messages.get(i++).toString() );
			assertEquals( "480/0/90/a / 64",      messages.get(i++).toString() );
			assertEquals( "960/0/90/a+2 / 64",    messages.get(i++).toString() ); // bbb+2  = a+2
			assertEquals( "1440/0/90/d-2 / 64",   messages.get(i++).toString() ); // c##-2  = d-2
			assertEquals( "1920/0/90/c / 64",     messages.get(i++).toString() ); // dbb    = c
			assertEquals( "2400/0/90/c-5 / 64",   messages.get(i++).toString() ); // a###-6 = c-5
			assertEquals( "2880/0/90/g+5 / 64",   messages.get(i++).toString() ); // abb+5  = g+5
			assertEquals( "3360/0/90/a#+4 / 64",  messages.get(i++).toString() ); // cbb+5  = a#+4
			assertEquals( "3840/0/90/c#+4 / 64",  messages.get(i++).toString() ); // b##+3  = c#+4
			// function + block
			assertEquals( "4320/0/90/c / 64",     messages.get(i++).toString() ); // bbb/c##/dbb = a/d/c ==> c/d/a
			assertEquals( "4320/0/90/d / 64",     messages.get(i++).toString() );
			assertEquals( "4320/0/90/a / 64",     messages.get(i++).toString() );
			assertEquals( "4800/0/90/c / 64",     messages.get(i++).toString() ); // bbb/c##/dbb = a/d/c ==> c/d/a
			assertEquals( "4800/0/90/d / 64",     messages.get(i++).toString() );
			assertEquals( "4800/0/90/a / 64",     messages.get(i++).toString() );
			assertEquals( "5280/0/90/a+2 / 64",   messages.get(i++).toString() ); // bbb+2  = a+2
			assertEquals( "5760/0/90/d-2 / 64",   messages.get(i++).toString() ); // c##-2  = d-2
			assertEquals( "6240/0/90/c / 64",     messages.get(i++).toString() ); // dbb    = c
			assertEquals( "6720/0/90/c-5 / 64",   messages.get(i++).toString() ); // a###-6 = c-5
			assertEquals( "7200/0/90/g+5 / 64",   messages.get(i++).toString() ); // abb+5  = g+5
			assertEquals( "7680/0/90/a#+4 / 64",  messages.get(i++).toString() ); // cbb+5  = a#+4
			assertEquals( "8160/0/90/c#+4 / 64",  messages.get(i++).toString() ); // b##+3  = c#+4
			// function
			assertEquals( "8640/0/90/c / 64",     messages.get(i++).toString() ); // bbb/c##/dbb = a/d/c ==> c/d/a
			assertEquals( "8640/0/90/d / 64",     messages.get(i++).toString() );
			assertEquals( "8640/0/90/a / 64",     messages.get(i++).toString() );
			assertEquals( "9120/0/90/c / 64",     messages.get(i++).toString() ); // bbb/c##/dbb = a/d/c ==> c/d/a
			assertEquals( "9120/0/90/d / 64",     messages.get(i++).toString() );
			assertEquals( "9120/0/90/a / 64",     messages.get(i++).toString() );
			assertEquals( "9600/0/90/a+2 / 64",   messages.get(i++).toString() ); // bbb+2  = a+2
			assertEquals( "10080/0/90/d-2 / 64",  messages.get(i++).toString() ); // c##-2  = d-2
			assertEquals( "10560/0/90/c / 64",    messages.get(i++).toString() ); // dbb    = c
			assertEquals( "11040/0/90/c-5 / 64",  messages.get(i++).toString() ); // a###-6 = c-5
			assertEquals( "11520/0/90/g+5 / 64",  messages.get(i++).toString() ); // abb+5  = g+5
			assertEquals( "12000/0/90/a#+4 / 64", messages.get(i++).toString() ); // cbb+5  = a#+4
			assertEquals( "12480/0/90/c#+4 / 64", messages.get(i++).toString() ); // b##+3  = c#+4
			// no more messages
			assertTrue(i == messages.size());
		}
		
		parse(getWorkingFile("chords"));
		assertEquals( 13920, instruments.get(0).getCurrentTicks() );
		assertEquals(  2400, instruments.get(1).getCurrentTicks() );
		assertEquals(  1440, instruments.get(9).getCurrentTicks() );
		assertEquals(  1920, instruments.get(2).getCurrentTicks() );
		
		parse(getWorkingFile("define"));
		assertEquals( 3120, instruments.get(0).getCurrentTicks() );
		assertEquals( 2160, instruments.get(9).getCurrentTicks() );
		
		parse(getWorkingFile("using-unknown-drumkit"));
		assertEquals(       123, instruments.get(9).instrumentNumber );
		assertEquals( "testing", instruments.get(9).instrumentName   );
		
		parse(getWorkingFile("using-known-drumkit"));
		assertEquals(           8, instruments.get(9).instrumentNumber );
		assertEquals( "test room", instruments.get(9).instrumentName   );
		
		parse(getWorkingFile("instruments-with-banknumbers"));
		assertEquals(   2, instruments.get(1).instrumentNumber  );
		assertEquals(   0, instruments.get(1).getBankMSB()      );
		assertEquals(   0, instruments.get(1).getBankLSB()      );
		assertEquals(   2, instruments.get(2).instrumentNumber  );
		assertEquals(   0, instruments.get(2).getBankMSB()      );
		assertEquals(   0, instruments.get(2).getBankLSB()      );
		assertEquals(  24, instruments.get(10).instrumentNumber );
		assertEquals( 120, instruments.get(10).getBankMSB()     );
		assertEquals(   0, instruments.get(10).getBankLSB()     );
		assertEquals(  24, instruments.get(15).instrumentNumber );
		assertEquals( 120, instruments.get(15).getBankMSB()     );
		assertEquals(   1, instruments.get(15).getBankLSB()     );
		
		parse(getWorkingFile("instruments-with-banknumbers2"));
		assertEquals( 0, instruments.get(1).instrumentNumber  );
		assertEquals( 0, instruments.get(1).getBankMSB()      );
		assertEquals( 0, instruments.get(1).getBankLSB()      );
		assertEquals( 0, instruments.get(2).instrumentNumber  );
		assertEquals( 0, instruments.get(2).getBankMSB()      );
		assertEquals( 0, instruments.get(2).getBankLSB()      );
		assertEquals( 0, instruments.get(10).instrumentNumber );
		assertEquals( 0, instruments.get(10).getBankMSB()     );
		assertEquals( 0, instruments.get(10).getBankLSB()     );
		assertEquals( 0, instruments.get(15).instrumentNumber );
		assertEquals( 0, instruments.get(15).getBankMSB()     );
		assertEquals( 0, instruments.get(15).getBankLSB()     );
		
		parse(getWorkingFile("instruments-single-line"));
		assertEquals(  40, instruments.get(1).instrumentNumber  );
		assertEquals(   0, instruments.get(1).getBankMSB()      );
		assertEquals(   0, instruments.get(1).getBankLSB()      );
		assertEquals(  30, instruments.get(2).instrumentNumber  );
		assertEquals(   0, instruments.get(2).getBankMSB()      );
		assertEquals(   0, instruments.get(2).getBankLSB()      );
		assertEquals(  22, instruments.get(10).instrumentNumber );
		assertEquals( 120, instruments.get(10).getBankMSB()     );
		assertEquals(   0, instruments.get(10).getBankLSB()     );
		
		parse(getWorkingFile("meta"));
		assertEquals(
			"(c) test\r\n2nd line",
			getMetaMsgText(0, 0)  // copyright
		);
		assertEquals(
			  "{#title=Title with tab\\t!}"
			+ "{#composer=Wolfgang Amadeus Mozart\\r\\nHaydn}"
			+ "{#lyrics=Some\\\\One}"
			+ "{#artist=\\{Someone\\} \\[Else\\]}"
			+ "{#software=Midica " + Midica.VERSION + "}"
			+ "{#}",
			getMetaMsgText(0, 1)  // RP-026 tags
		);
		// soft karaoke fields (meta track)
		assertEquals( "@KMIDI KARAOKE FILE", getMetaMsgText(0, 2) );
		assertEquals( "@V0100",              getMetaMsgText(0, 3) );
		assertEquals( "@Iinfo 1",            getMetaMsgText(0, 4) );
		assertEquals( "@Iinfo 2",            getMetaMsgText(0, 5) );
		assertEquals( "@Iinfo 3",            getMetaMsgText(0, 6) );
		// soft karaoke fields (lyrics track)
		assertEquals( "@LENGL",           getMetaMsgText(1, 0) );
		assertEquals( "@Tsk-title",       getMetaMsgText(1, 1) );
		assertEquals( "@Tthe kar author", getMetaMsgText(1, 2) );
		assertEquals( "@Tsk-copyright",   getMetaMsgText(1, 3) );
		// soft karaoke syllables
		assertEquals( "\\test1", getMetaMsgText(1, 4) );
		assertEquals( "\\test2", getMetaMsgText(1, 5) );
		assertEquals( "/test3",  getMetaMsgText(1, 6) );
		assertEquals( " test4",  getMetaMsgText(1, 7) );
		assertEquals( " test5",  getMetaMsgText(1, 8) );
		// soft karaoke lyrics
		assertEquals( "test1\n\ntest2\ntest3 test4 test5", getLyrics() );
		
		// test normal lyrics
		parse(getWorkingFile("lyrics"));
		assertEquals( "happy birthday to you,\nhappy birthday to you,\n\nhappy", getLyrics() );
		
		parse(getWorkingFile("block-tuplets"));
		assertEquals( 3668,  instruments.get(0).getCurrentTicks() );
		assertEquals(  101,  instruments.get(0).getVelocity() );
		assertEquals(  0.8f, instruments.get(0).getDurationRatio() );
		assertEquals( 3520,  instruments.get(1).getCurrentTicks() );
		
		parse(getWorkingFile("drum-only-with-global"));
		assertEquals( 960, instruments.get(9).getCurrentTicks() );
		
		parse(getWorkingFile("drum-only-with-empty-multiple-block"));
		assertEquals( 0, instruments.get(9).getCurrentTicks() );
		
		parse(getWorkingFile("drum-only-with-empty-multiple-function"));
		assertEquals( 0, instruments.get(9).getCurrentTicks() );
		
		parse(getWorkingFile("drum-only-with-multiple"));
		assertEquals( 0, instruments.get(9).getCurrentTicks() );
		
		parse(getWorkingFile("drum-only-with-channel-options"));
		assertEquals( 1920, instruments.get(9).getCurrentTicks() );
		
		parse(getWorkingFile("tremolo"));
		assertEquals( 11520, instruments.get(0).getCurrentTicks() );
		String rootString = ((MidicaTreeModel)SequenceAnalyzer.getSequenceInfo().get("banks_total")).getRoot().toString();
		assertEquals( "Total (33)", rootString );
		
		parse(getWorkingFile("const"));
		assertEquals( constants.get("$forte"),            "120"                                                              );
		assertEquals( constants.get("$piano"),            "30"                                                               );
		assertEquals( constants.get("$mezzoforte"),       "75"                                                               );
		assertEquals( constants.get("$staccato"),         "duration=50%"                                                     );
		assertEquals( constants.get("$legato"),           "duration=100%"                                                    );
		assertEquals( constants.get("$legato_forte"),     "duration=100% , v = 120"                                          );
		assertEquals( constants.get("$several_columns"),  "c  /4  duration=50%"                                              );
		assertEquals( constants.get("$cmd_with_columns"), "0  c  /4"                                                         );
		assertEquals( constants.get("$whole_line"),       "0  c  /4  duration=50%"                                           );
		assertEquals( constants.get("$complex_const"),    "START duration=100% , v = 120 MIDDLE duration=100% , v = 120 END" );
		
		parse(getWorkingFile("var"));
		assertEquals( "cent", constants.get("$cent")            );
		assertEquals( "123",  variables.get("$forte")           );
		assertEquals( "30",   variables.get("$piano")           );
		assertEquals( "80",   variables.get("$mezzoforte")      );
		assertEquals( "50%",  variables.get("$staccato")        );
		assertEquals( "100",  variables.get("$legato")          );
		assertEquals( "75%",  variables.get("$medium_duration") );
		assertEquals( "1",    variables.get("$c")               );
		assertEquals( "c",    variables.get("$n")               );
		assertEquals( "d",    variables.get("$n2")              );
		assertEquals( "/2",   variables.get("$l")               );
		assertEquals( "3",    variables.get("$q")               );
		// channel 0:
		messages = getMessagesByChannel(0);
		{
			int i = 0;
			assertEquals( "0/0/90/c / 30",     messages.get(++i).toString() );  // c piano
			assertEquals( "240/0/80/c / 0",    messages.get(++i).toString() );  //   staccato=240 ticks
			assertEquals( "480/0/90/c / 30",   messages.get(++i).toString() );  // c
			assertEquals( "959/0/80/c / 0",    messages.get(++i).toString() );  //   legato=480-1 ticks    (-1: legato-correction)
			assertEquals( "960/0/90/c / 123",  messages.get(++i).toString() );  // c forte   (CALL test1)
			assertEquals( "1439/0/80/c / 0",   messages.get(++i).toString() );  //   legato=480-1 ticks    (-1: legato-correction)
			assertEquals( "1440/0/90/c / 123", messages.get(++i).toString() );  // c         (CALL test2)
			assertEquals( "1920/0/80/c / 0",   messages.get(++i).toString() );  //   legato=480 ticks
		}
		// channel 1:
		messages = getMessagesByChannel(1);
		{
			int i = 0;
			// function test1, plain
			assertEquals( "0/1/91/c / 123",     messages.get(++i).toString() );  // c forte
			assertEquals( "456/1/81/c / 0",     messages.get(++i).toString() );  //   default duration: 456 ticks
			assertEquals( "480/1/91/c / 123",   messages.get(++i).toString() );  // c forte
			assertEquals( "720/1/81/c / 0",     messages.get(++i).toString() );  //   staccato=240 ticks
			// function test1, block
			assertEquals( "960/1/91/c / 80",   messages.get(++i).toString()  );  // c mezzoforte
			assertEquals( "1200/1/81/c / 0",   messages.get(++i).toString()  );  //   staccato=240 ticks
			assertEquals( "1440/1/91/c / 80",  messages.get(++i).toString()  );  // c mezzoforte
			assertEquals( "1440/1/91/c+ / 80", messages.get(++i).toString()  );  // c+ mezzoforte
			assertEquals( "1800/1/81/c / 0",   messages.get(++i).toString()  );  //   medium_duration=360 ticks
			assertEquals( "1800/1/81/c+ / 0",  messages.get(++i).toString()  );  //   medium_duration=360 ticks
		}
		// INLUDE test2
		assertEquals( 21, messages.size() ); // 1x program change, 10x note-on, 10x note-off
		
		parse(getWorkingFile("shift"));
		messages = getMessagesByStatus("90");
		assertEquals( 19, messages.size() );
		{
			int i = 0;
			assertEquals( "0/0/90/d / 64",      messages.get(i++).toString() ); // before all functions
			assertEquals( "480/0/90/e / 64",    messages.get(i++).toString() );
			assertEquals( "960/0/90/c / 64",    messages.get(i++).toString() );
			assertEquals( "1440/0/90/a#- / 64", messages.get(i++).toString() );
			assertEquals( "1920/0/90/c / 64",   messages.get(i++).toString() );
			assertEquals( "2400/0/90/c / 64",   messages.get(i++).toString() ); // CALL test1
			assertEquals( "2880/0/90/c / 64",   messages.get(i++).toString() );
			assertEquals( "3360/0/90/c / 64",   messages.get(i++).toString() );     // c (chord)
			assertEquals( "3360/0/90/d / 64",   messages.get(i++).toString() );     // d (chord)
			assertEquals( "3840/0/90/a#- / 64", messages.get(i++).toString() );
			assertEquals( "4320/0/90/a#- / 64", messages.get(i++).toString() );     // a#- (chord)
			assertEquals( "4320/0/90/c / 64",   messages.get(i++).toString() );     // c (chord)
			assertEquals( "4800/0/90/c+ / 64",  messages.get(i++).toString() ); // CALL test1  s=12
			assertEquals( "5280/0/90/c+ / 64",  messages.get(i++).toString() );
			assertEquals( "5760/0/90/c+ / 64",  messages.get(i++).toString() );     // c+ (chord)
			assertEquals( "5760/0/90/d+ / 64",  messages.get(i++).toString() );     // d+ (chord)
			assertEquals( "6240/0/90/a# / 64",  messages.get(i++).toString() );
			assertEquals( "6720/0/90/a# / 64",  messages.get(i++).toString() );     // a# (chord)
			assertEquals( "6720/0/90/c+ / 64",  messages.get(i++).toString() );     // c+ (chord)
		}
		
		parse(getWorkingFile("functions"));
		assertEquals( 14400, instruments.get(0).getCurrentTicks() );  // channel 0
		assertEquals(  2880, instruments.get(1).getCurrentTicks() );  // channel 1
		assertEquals(                                                 // channel 0
			"abc... abc... abc... abc... abc... abc... "      // CALL test1(/2)
			+ "xyz3rd xyz3rd xyz3rd xyz3rd xyz3rd xyz3rd "    // CALL test2(dur=/4, l1=xyz, 3rd, 4th) q=3, m
			+ "xyz3rd xyz3rd xyz3rd xyz3rd xyz3rd xyz3rd "
			+ "xyz3rd xyz3rd xyz3rd xyz3rd xyz3rd xyz3rd "
			+ "abc... abc... abc... abc... abc... abc... "    // test3()
			+ "bbb?? bbb?? bbb?? bbb?? bbb?? bbb?? ",
			getLyrics()
		);
		messages = getMessagesByStatus("91");                         // channel 1
		assertEquals( 6, messages.size() );
		{
			int i = 0;
			assertEquals( "0/1/91/b+ / 60",       messages.get(i++).toString() );
			assertEquals( "480/1/91/c+2 / 63",    messages.get(i++).toString() );
			assertEquals( "960/1/91/c#+ / 70",    messages.get(i++).toString() );
			assertEquals( "1440/1/91/e+ / 80",    messages.get(i++).toString() );
			assertEquals( "1920/1/91/c-2 / 90",   messages.get(i++).toString() );
			assertEquals( "2400/1/91/c#-2 / 110", messages.get(i++).toString() );
		}
		
		parse(getWorkingFile("if-elsif-else-blocks"));
		assertEquals(
			"EQ: 1 2 3 4\n"
			+ "NEQ: 1 2 3 4\n"
			+ "LT: 1 2 3 4\n"
			+ "LE: 1 2 3 4\n"
			+ "GT: 1 2 3 4\n"
			+ "GE: 1 2 3 4\n"
			+ "IN: 1 2 3 4\n"
			+ "DEF: 1 2 3 4\n"
			+ "NDEF: 1 2 3 4\n"
			+ "I.A.E.: 1 3\n"
			+ "Nested: a a1 a1b a b3b b3c if\n"
			+ "CALL: 2 2 2 4 6",
			getLyrics()
		);
		
		parse(getWorkingFile("legato-correction"));
		// channel 0:
		messages = getNoteOnOffMessagesByChannel(0);
		assertEquals( 8, messages.size() );
		{
			int i = 0;
			assertEquals( "0/0/90/c / 78",    messages.get(i++).toString() ); // c ON
			assertEquals( "480/0/90/d / 78",  messages.get(i++).toString() ); // d ON
			assertEquals( "960/0/90/e / 78",  messages.get(i++).toString() ); // e ON
			assertEquals( "1439/0/80/e / 0",  messages.get(i++).toString() ); // e OFF (correction)
			assertEquals( "1440/0/90/e / 78", messages.get(i++).toString() ); // e ON
			assertEquals( "2400/0/80/c / 0",  messages.get(i++).toString() ); // c OFF
			assertEquals( "2880/0/80/d / 0",  messages.get(i++).toString() ); // d OFF
			assertEquals( "3840/0/80/e / 0",  messages.get(i++).toString() ); // e OFF
		}
		// channel 1:
		messages = getNoteOnOffMessagesByChannel(1);
		assertEquals( 14, messages.size() );
		{
			int i = 0;
			assertEquals( "0/1/91/c / 102",    messages.get(i++).toString() ); // c/d/e : c ON
			assertEquals( "0/1/91/d / 102",    messages.get(i++).toString() ); // c/d/e : d ON
			assertEquals( "0/1/91/e / 102",    messages.get(i++).toString() ); // c/d/e : e ON
			assertEquals( "480/1/91/f / 102",  messages.get(i++).toString() ); // f     : f ON
			assertEquals( "959/1/81/d / 0",    messages.get(i++).toString() ); // c/d/e : d OFF (correction)
			assertEquals( "960/1/91/d / 102",  messages.get(i++).toString() ); // g/d/a : d ON
			assertEquals( "960/1/91/g / 102",  messages.get(i++).toString() ); // g/d/a : g ON
			assertEquals( "960/1/91/a / 102",  messages.get(i++).toString() ); // g/d/a : a ON
			assertEquals( "2400/1/81/c / 0",   messages.get(i++).toString() ); // c/d/e : c OFF
			assertEquals( "2400/1/81/e / 0",   messages.get(i++).toString() ); // c/d/e : e OFF
			assertEquals( "2880/1/81/f / 0",   messages.get(i++).toString() ); // f     : f OFF
			assertEquals( "3360/1/81/d / 0",   messages.get(i++).toString() ); // g/d/a : d OFF
			assertEquals( "3360/1/81/g / 0",   messages.get(i++).toString() ); // g/d/a : g OFF
			assertEquals( "3360/1/81/a / 0",   messages.get(i++).toString() ); // g/d/a : a OFF
		}
		// channel 2:
		messages = getNoteOnOffMessagesByChannel(2);
		assertEquals( 4, messages.size() );
		{
			int i = 0;
			assertEquals( "0/2/92/c / 35",   messages.get(i++).toString() ); //            c ON
			assertEquals( "479/2/82/c / 0",  messages.get(i++).toString() ); //            c OFF (correction)
			assertEquals( "480/2/92/c / 35", messages.get(i++).toString() ); // d/s=-2 ==> c ON
			assertEquals( "1920/2/82/c / 0", messages.get(i++).toString() ); // d/s=-2 ==> c OFF
		}
		// channel 3:
		messages = getNoteOnOffMessagesByChannel(3);
		assertEquals( 4, messages.size() );
		{
			int i = 0;
			assertEquals( "0/3/93/c / 65",   messages.get(i++).toString() ); // c ON
			assertEquals( "479/3/83/c / 0",  messages.get(i++).toString() ); // c OFF (correction)
			assertEquals( "480/3/93/c / 65", messages.get(i++).toString() ); // c ON
			assertEquals( "960/3/83/c / 0",  messages.get(i++).toString() ); // c OFF
		}
		// different transpose levels
		setTransposeLevel((byte) 12);
		parse(getWorkingFile("legato-correction"));
		setTransposeLevel((byte) 0);
		messages = getNoteOnOffMessagesByChannel(0);
		assertEquals( 8, messages.size() );
		{
			int i = 0;
			assertEquals( "0/0/90/c+ / 78",    messages.get(i++).toString() ); // c+ ON
			assertEquals( "480/0/90/d+ / 78",  messages.get(i++).toString() ); // d+ ON
			assertEquals( "960/0/90/e+ / 78",  messages.get(i++).toString() ); // e+ ON
			assertEquals( "1439/0/80/e+ / 0",  messages.get(i++).toString() ); // e+ OFF (correction)
			assertEquals( "1440/0/90/e+ / 78", messages.get(i++).toString() ); // e+ ON
			assertEquals( "2400/0/80/c+ / 0",  messages.get(i++).toString() ); // c+ OFF
			assertEquals( "2880/0/80/d+ / 0",  messages.get(i++).toString() ); // d+ OFF
			assertEquals( "3840/0/80/e+ / 0",  messages.get(i++).toString() ); // e+ OFF
		}
		
		// key-signature with "sharp" and "flat" configuration
		boolean[] sharpOrFlat = new boolean[] {true, false};
		for (boolean isSharp : sharpOrFlat) {
			
			// adjust config
			if (isSharp)
				cbxs[3].setSelectedIndex(0);
			else
				cbxs[3].setSelectedIndex(1);
			
			parse(getWorkingFile("key-signature"));
			messages = getMessagesByStatusAndTickRangeAndSummary("FF", 1L, 999999L, true);
			assertEquals(40, messages.size());
			{
				int i = 0;
				assertEquals("c/maj, 0 ♯/♭", messages.get(i++).getOption(IMessageType.OPT_SUMMARY));
				assertEquals("c#/maj, 7 ♯",  messages.get(i++).getOption(IMessageType.OPT_SUMMARY));
				assertEquals("db/maj, 5 ♭",  messages.get(i++).getOption(IMessageType.OPT_SUMMARY));
				assertEquals("d/maj, 2 ♯",   messages.get(i++).getOption(IMessageType.OPT_SUMMARY));
				assertEquals("eb/maj, 3 ♭",  messages.get(i++).getOption(IMessageType.OPT_SUMMARY));
				assertEquals("eb/maj, 3 ♭",  messages.get(i++).getOption(IMessageType.OPT_SUMMARY));
				assertEquals("e/maj, 4 ♯",   messages.get(i++).getOption(IMessageType.OPT_SUMMARY));
				assertEquals("e/maj, 4 ♯",   messages.get(i++).getOption(IMessageType.OPT_SUMMARY));
				assertEquals("f/maj, 1 ♭",   messages.get(i++).getOption(IMessageType.OPT_SUMMARY));
				assertEquals("f/maj, 1 ♭",   messages.get(i++).getOption(IMessageType.OPT_SUMMARY));
				assertEquals("f#/maj, 6 ♯",  messages.get(i++).getOption(IMessageType.OPT_SUMMARY));
				assertEquals("gb/maj, 6 ♭",  messages.get(i++).getOption(IMessageType.OPT_SUMMARY));
				assertEquals("g/maj, 1 ♯",   messages.get(i++).getOption(IMessageType.OPT_SUMMARY));
				assertEquals("ab/maj, 4 ♭",  messages.get(i++).getOption(IMessageType.OPT_SUMMARY));
				assertEquals("ab/maj, 4 ♭",  messages.get(i++).getOption(IMessageType.OPT_SUMMARY));
				assertEquals("a/maj, 3 ♯",   messages.get(i++).getOption(IMessageType.OPT_SUMMARY));
				assertEquals("bb/maj, 2 ♭",  messages.get(i++).getOption(IMessageType.OPT_SUMMARY));
				assertEquals("bb/maj, 2 ♭",  messages.get(i++).getOption(IMessageType.OPT_SUMMARY));
				assertEquals("b/maj, 5 ♯",   messages.get(i++).getOption(IMessageType.OPT_SUMMARY));
				assertEquals("cb/maj, 7 ♭",  messages.get(i++).getOption(IMessageType.OPT_SUMMARY));
				assertEquals("c/min, 3 ♭",   messages.get(i++).getOption(IMessageType.OPT_SUMMARY));
				assertEquals("c#/min, 4 ♯",  messages.get(i++).getOption(IMessageType.OPT_SUMMARY));
				assertEquals("c#/min, 4 ♯",  messages.get(i++).getOption(IMessageType.OPT_SUMMARY));
				assertEquals("d/min, 1 ♭",   messages.get(i++).getOption(IMessageType.OPT_SUMMARY));
				assertEquals("d#/min, 6 ♯",  messages.get(i++).getOption(IMessageType.OPT_SUMMARY));
				assertEquals("eb/min, 6 ♭",  messages.get(i++).getOption(IMessageType.OPT_SUMMARY));
				assertEquals("e/min, 1 ♯",   messages.get(i++).getOption(IMessageType.OPT_SUMMARY));
				assertEquals("e/min, 1 ♯",   messages.get(i++).getOption(IMessageType.OPT_SUMMARY));
				assertEquals("f/min, 4 ♭",   messages.get(i++).getOption(IMessageType.OPT_SUMMARY));
				assertEquals("f/min, 4 ♭",   messages.get(i++).getOption(IMessageType.OPT_SUMMARY));
				assertEquals("f#/min, 3 ♯",  messages.get(i++).getOption(IMessageType.OPT_SUMMARY));
				assertEquals("f#/min, 3 ♯",  messages.get(i++).getOption(IMessageType.OPT_SUMMARY));
				assertEquals("g/min, 2 ♭",   messages.get(i++).getOption(IMessageType.OPT_SUMMARY));
				assertEquals("g#/min, 5 ♯",  messages.get(i++).getOption(IMessageType.OPT_SUMMARY));
				assertEquals("ab/min, 7 ♭",  messages.get(i++).getOption(IMessageType.OPT_SUMMARY));
				assertEquals("a/min, 0 ♯/♭", messages.get(i++).getOption(IMessageType.OPT_SUMMARY));
				assertEquals("a#/min, 7 ♯",  messages.get(i++).getOption(IMessageType.OPT_SUMMARY));
				assertEquals("bb/min, 5 ♭",  messages.get(i++).getOption(IMessageType.OPT_SUMMARY));
				assertEquals("b/min, 2 ♯",   messages.get(i++).getOption(IMessageType.OPT_SUMMARY));
				assertEquals("b/min, 2 ♯",   messages.get(i++).getOption(IMessageType.OPT_SUMMARY));
			}
		}
		cbxs[3].setSelectedIndex(0); // restore default config (sharp)
		
		parse(getWorkingFile("patterns"));
		// channel 0:
		messages = getNoteOnOffMessagesByChannel(0);
		assertEquals( 62, messages.size() );
		{
			int i = 0;
			// PART 1: q+m as outer options, q+v+d as inner options
			// pat_qvd, first loop run (outer q=2)
			assertEquals( "0/0/90/c / 90",     messages.get(i++).toString() );  // c ON    v=90    pattern line 1
			assertEquals( "0/0/90/c+3 / 90",   messages.get(i++).toString() );  // c+3 ON          channel cmd
			assertEquals( "192/0/80/c+3 / 0",  messages.get(i++).toString() );  // c+3 OFF d=80%
			assertEquals( "384/0/80/c / 0",    messages.get(i++).toString() );  // c OFF   d=80%
			assertEquals( "480/0/90/e / 90",   messages.get(i++).toString() );  // e ON    v=90    pattern line 2
			assertEquals( "672/0/80/e / 0",    messages.get(i++).toString() );  // e OFF   d=80%
			assertEquals( "720/0/90/g / 127",  messages.get(i++).toString() );  // g ON    v=127   pattern line 3  inner: q=2
			assertEquals( "732/0/80/g / 0",    messages.get(i++).toString() );  // g OFF   d=10%
			assertEquals( "840/0/90/g / 127",  messages.get(i++).toString() );  // g ON    v=127   pattern line 3  inner: q=2
			assertEquals( "852/0/80/g / 0",    messages.get(i++).toString() );  // g OFF   d=10%
			assertEquals( "960/0/90/c / 127",  messages.get(i++).toString() );  // c ON    v=127   pattern line 4
			assertEquals( "960/0/90/e / 127",  messages.get(i++).toString() );  // e ON    v=127   pattern line 4
			assertEquals( "960/0/90/g / 127",  messages.get(i++).toString() );  // g ON    v=127   pattern line 4
			assertEquals( "1008/0/80/c / 0",   messages.get(i++).toString() );  // c OFF   d=10%
			assertEquals( "1008/0/80/e / 0",   messages.get(i++).toString() );  // e OFF   d=10%
			assertEquals( "1008/0/80/g / 0",   messages.get(i++).toString() );  // g OFF   d=10%
			// pat_qvd, second loop run (outer q=2)
			assertEquals( "1440/0/90/c / 90",   messages.get(i++).toString() );  // c ON    v=90    pattern line 1
			assertEquals( "1824/0/80/c / 0",    messages.get(i++).toString() );  // c OFF   d=80%
			assertEquals( "1920/0/90/e / 90",   messages.get(i++).toString() );  // e ON    v=90    pattern line 2
			assertEquals( "2112/0/80/e / 0",    messages.get(i++).toString() );  // e OFF   d=80%
			assertEquals( "2160/0/90/g / 127",  messages.get(i++).toString() );  // g ON    v=127   pattern line 3  inner: q=2
			assertEquals( "2172/0/80/g / 0",    messages.get(i++).toString() );  // g OFF   d=10%
			assertEquals( "2280/0/90/g / 127",  messages.get(i++).toString() );  // g ON    v=127   pattern line 3  inner: q=2
			assertEquals( "2292/0/80/g / 0",    messages.get(i++).toString() );  // g OFF   d=10%
			assertEquals( "2400/0/90/c / 127",  messages.get(i++).toString() );  // c ON    v=127   pattern line 4
			assertEquals( "2400/0/90/e / 127",  messages.get(i++).toString() );  // e ON    v=127   pattern line 4
			assertEquals( "2400/0/90/g / 127",  messages.get(i++).toString() );  // g ON    v=127   pattern line 4
			assertEquals( "2448/0/80/c / 0",    messages.get(i++).toString() );  // c OFF   d=10%
			assertEquals( "2448/0/80/e / 0",    messages.get(i++).toString() );  // e OFF   d=10%
			assertEquals( "2448/0/80/g / 0",    messages.get(i++).toString() );  // g OFF   d=10%
			// PART 2: v+d+s as outer options, no inner options
			// pat_simple, first call (no outer options)
			assertEquals( "4080/0/90/a / 90",   messages.get(i++).toString() );  // a ON   v=90     channel cmd
			assertEquals( "4464/0/80/a / 0",    messages.get(i++).toString() );  // a OFF  d=80%
			assertEquals( "4560/0/90/a / 90",   messages.get(i++).toString() );  // a ON   v=90     pattern (pat_simple)
			assertEquals( "4944/0/80/a / 0",    messages.get(i++).toString() );  // a OFF  d=80%
			assertEquals( "5040/0/90/a / 90",   messages.get(i++).toString() );  // a ON   v=90     channel cmd
			assertEquals( "5424/0/80/a / 0",    messages.get(i++).toString() );  // a OFF  d=80%
			// pat_simple, second call (test outer v=50, d=60%, s=2)
			assertEquals( "5520/0/90/b / 50",   messages.get(i++).toString() );  // b ON   v=50     pattern (pat_simple)
			assertEquals( "5808/0/80/b / 0",    messages.get(i++).toString() );  // b OFF  d=60%
			assertEquals( "6000/0/90/a / 50",   messages.get(i++).toString() );  // a ON   v=50     channel cmd
			assertEquals( "6288/0/80/a / 0",    messages.get(i++).toString() );  // a OFF  d=60%
			// PART 3: no outer options, m+tr as inner options
			// pat_mtr
			assertEquals( "6480/0/90/c / 50",   messages.get(i++).toString() );  // c ON            pattern line 1
			assertEquals( "6480/0/90/d / 50",   messages.get(i++).toString() );  // d ON            pattern line 2
			assertEquals( "6768/0/80/d / 0",    messages.get(i++).toString() );  // d OFF
			assertEquals( "6960/0/90/d / 50",   messages.get(i++).toString() );  // d ON            pattern line 2
			assertEquals( "7056/0/80/c / 0",    messages.get(i++).toString() );  // c OFF
			assertEquals( "7248/0/80/d / 0",    messages.get(i++).toString() );  // d OFF
			assertEquals( "7440/0/90/d / 50",   messages.get(i++).toString() );  // d ON            pattern line 2
			assertEquals( "7728/0/80/d / 0",    messages.get(i++).toString() );  // d OFF
			assertEquals( "7920/0/90/d / 50",   messages.get(i++).toString() );  // d ON            pattern line 2
			assertEquals( "8208/0/80/d / 0",    messages.get(i++).toString() );  // d OFF
			// PART 4: l+t as outer options, no inner options
			// pat_simple, q=2, l=test_
			assertEquals( "test ", getLyrics() );
			assertEquals( "8400/0/90/a / 50",   messages.get(i++).toString() );  // c ON            pattern line 1
			assertEquals( "8688/0/80/a / 0",    messages.get(i++).toString() );  // c OFF
			assertEquals( "8880/0/90/a / 50",   messages.get(i++).toString() );  // c ON            q=2
			assertEquals( "9168/0/80/a / 0",    messages.get(i++).toString() );  // c OFF
			// pat_simple, called inside a block with t (triplet)
			assertEquals( "9360/0/90/a / 50",   messages.get(i++).toString() );  // c ON            pattern line 1 (from block)
			assertEquals( "9648/0/80/a / 0",    messages.get(i++).toString() );  // c OFF
			assertEquals( "9840/0/90/a / 50",   messages.get(i++).toString() );  // c ON            q=2
			assertEquals( "10128/0/80/a / 0",   messages.get(i++).toString() );  // c OFF
			// channel command with q=2 + t (triplet)
			assertEquals( "10320/0/90/a / 50",  messages.get(i++).toString() );  // c ON            channel cmd
			assertEquals( "10512/0/80/a / 0",   messages.get(i++).toString() );  // c OFF
			assertEquals( "10640/0/90/a / 50",  messages.get(i++).toString() );  // c ON            channel cmd
			assertEquals( "10832/0/80/a / 0",   messages.get(i++).toString() );  // c OFF
			assertEquals( 10960, instruments.get(0).getCurrentTicks() );
		}
		// channel 1:
		messages = getNoteOnOffMessagesByChannel(1);
		assertEquals( 24, messages.size() );
		{
			int i = 0;
			assertEquals( "0/1/91/c / 64",    messages.get(i).toString()    ); // c /4
			assertEquals( "480/1/91/d / 64",  messages.get(i+=2).toString() ); // d /8
			assertEquals( "720/1/91/d / 64",  messages.get(i+=2).toString() ); // d /8
			assertEquals( "960/1/91/c / 64",  messages.get(i+=2).toString() ); // c /4
			assertEquals( "1440/1/91/d / 64", messages.get(i+=2).toString() ); // d /8
			assertEquals( "1680/1/91/d / 64", messages.get(i+=2).toString() ); // d /8
			assertEquals( "1920/1/91/c / 64", messages.get(i+=2).toString() ); // c /4
			assertEquals( "2400/1/91/d / 64", messages.get(i+=2).toString() ); // d /8
			assertEquals( "2640/1/91/d / 64", messages.get(i+=2).toString() ); // d /8
			assertEquals( "2880/1/91/d / 64", messages.get(i+=2).toString() ); // d /4
			assertEquals( "3360/1/91/c / 64", messages.get(i+=2).toString() ); // c /8
			assertEquals( "3600/1/91/c / 64", messages.get(i+=2).toString() ); // c /8
			assertEquals( 3840, instruments.get(1).getCurrentTicks() );
		}
		// channel 9:
		messages = getNoteOnOffMessagesByChannel(9);
		assertEquals( 12, messages.size() );
		{
			int i = 0;
			assertEquals( "0/9/99/bass_drum_1 (bd1) / 64",     messages.get(i).toString()    ); // bd1 /4
			assertEquals( "480/9/99/hi_hat_closed (hhc) / 64", messages.get(i+=2).toString() ); // hhc /8
			assertEquals( "720/9/99/hi_hat_closed (hhc) / 64", messages.get(i+=2).toString() ); // hhc /8
			assertEquals( "960/9/99/hi_hat_closed (hhc) / 64", messages.get(i+=2).toString() ); // hhc /4
			assertEquals( "1440/9/99/bass_drum_1 (bd1) / 64",  messages.get(i+=2).toString() ); // bd1 /8
			assertEquals( "1680/9/99/bass_drum_1 (bd1) / 64",  messages.get(i+=2).toString() ); // bd1 /8
			assertEquals( 1920, instruments.get(9).getCurrentTicks() );
		}
		// channel 2:
		messages = getNoteOnOffMessagesByChannel(2);
		assertEquals( 12, messages.size() );
		{
			int i = 0;
			assertEquals( "0/2/92/e / 64",    messages.get(i).toString()    ); // e /4
			assertEquals( "480/2/92/c / 64",  messages.get(i+=2).toString() ); // c /8
			assertEquals( "720/2/92/c / 64",  messages.get(i+=2).toString() ); // c /8
			assertEquals( "960/2/92/e / 64",  messages.get(i+=2).toString() ); // e /4
			assertEquals( "1440/2/92/c / 64", messages.get(i+=2).toString() ); // c /8
			assertEquals( "1680/2/92/c / 64", messages.get(i+=2).toString() ); // c /8
			assertEquals( 0, instruments.get(2).getCurrentTicks() ); // q=2, m
		}
		// channel 3:
		messages = getNoteOnOffMessagesByChannel(3);
		assertEquals( 6, messages.size() );
		{
			int i = 0;
			assertEquals( "0/3/93/c / 120",   messages.get(i++).toString() ); // c ON /4t
			assertEquals( "0/3/93/e / 120",   messages.get(i++).toString() ); // e ON /8t
			assertEquals( "152/3/83/e / 0",   messages.get(i++).toString() ); // e OFF /8t d=95%
			assertEquals( "160/3/93/e / 120", messages.get(i++).toString() ); // e ON /8t
			assertEquals( "304/3/83/c / 0",   messages.get(i++).toString() ); // c OFF /4t d=95%
			assertEquals( "312/3/83/e / 0",   messages.get(i++).toString() ); // e OFF /8t d=95%
			assertEquals( 320, instruments.get(3).getCurrentTicks() ); // /4t
		}
		// channel 4: pattern recursion and REST
		messages = getNoteOnOffMessagesByChannel(4);
		assertEquals( 6, messages.size() );
		{
			int i = 0;
			assertEquals( "0/4/94/c / 64",   messages.get(i++).toString() ); // c ON
			assertEquals( "456/4/84/c / 0",  messages.get(i++).toString() ); // c OFF /4  d=95%
			// 1/8th REST
			assertEquals( "720/4/94/e / 64", messages.get(i++).toString() ); // e ON /8
			assertEquals( "720/4/94/f / 64", messages.get(i++).toString() ); // f ON /8
			assertEquals( "948/4/84/e / 0",  messages.get(i++).toString() ); // e OFF /8t d=95%
			assertEquals( "948/4/84/f / 0",  messages.get(i++).toString() ); // f OFF /8t d=95%
			assertEquals( 960, instruments.get(4).getCurrentTicks() );
		}
		// channel 5: pattern with parameters
		messages = getNoteOnOffMessagesByChannel(5);
		assertEquals( 48, messages.size() );
		{
			int i = 0;
			// downstroke[number], length: /1 (1920 ticks); distance: /32 (60 ticks)
			assertEquals( "0/5/95/f / 64",   messages.get(i++).toString() ); // f ON
			assertEquals( "60/5/95/e / 64",  messages.get(i++).toString() ); // e ON
			assertEquals( "120/5/95/d / 64", messages.get(i++).toString() ); // d ON
			assertEquals( "180/5/95/c / 64", messages.get(i++).toString() ); // c ON
			
			assertEquals( "1824/5/85/f / 0", messages.get(i++).toString() ); // f OFF /1  d=95%
			assertEquals( "1884/5/85/e / 0", messages.get(i++).toString() ); // e OFF /1  d=95%
			assertEquals( "1944/5/85/d / 0", messages.get(i++).toString() ); // d OFF /1  d=95%
			assertEquals( "2004/5/85/c / 0", messages.get(i++).toString() ); // c OFF /1  d=95%
			
			// /1 REST
			
			// downstroke[number], length: /1 (1920 ticks); distance: /32 (60 ticks)
			assertEquals( "3840/5/95/f / 64", messages.get(i++).toString() ); // f ON
			assertEquals( "3900/5/95/e / 64", messages.get(i++).toString() ); // e ON
			assertEquals( "3960/5/95/d / 64", messages.get(i++).toString() ); // d ON
			assertEquals( "4020/5/95/c / 64", messages.get(i++).toString() ); // c ON
			
			assertEquals( "5664/5/85/f / 0",  messages.get(i++).toString() ); // f OFF /1  d=95%
			assertEquals( "5724/5/85/e / 0",  messages.get(i++).toString() ); // e OFF /1  d=95%
			// ON/OFF overlapping with the second run (q=2)
			assertEquals( "5760/5/95/f / 64", messages.get(i++).toString() ); // f ON
			assertEquals( "5784/5/85/d / 0",  messages.get(i++).toString() ); // d OFF /1  d=95%
			assertEquals( "5820/5/95/e / 64", messages.get(i++).toString() ); // e ON
			assertEquals( "5844/5/85/c / 0",  messages.get(i++).toString() ); // c OFF /1  d=95%
			assertEquals( "5880/5/95/d / 64", messages.get(i++).toString() ); // d ON
			assertEquals( "5940/5/95/c / 64", messages.get(i++).toString() ); // c ON
			
			assertEquals( "7584/5/85/f / 0",  messages.get(i++).toString() ); // f OFF /1  d=95%
			assertEquals( "7644/5/85/e / 0",  messages.get(i++).toString() ); // e OFF /1  d=95%
			assertEquals( "7704/5/85/d / 0",  messages.get(i++).toString() ); // d OFF /1  d=95%
			assertEquals( "7764/5/85/c / 0",  messages.get(i++).toString() ); // c OFF /1  d=95%
			
			// /1 REST
			
			// downstroke{name}, length: /1 (1920 ticks); distance: /32 (60 ticks)
			assertEquals( "9600/5/95/f / 64", messages.get(i++).toString() ); // f ON
			assertEquals( "9660/5/95/e / 64", messages.get(i++).toString() ); // e ON
			assertEquals( "9720/5/95/d / 64", messages.get(i++).toString() ); // d ON
			assertEquals( "9780/5/95/c / 64", messages.get(i++).toString() ); // c ON
			
			assertEquals( "11424/5/85/f / 0",  messages.get(i++).toString() ); // f OFF /1  d=95%
			assertEquals( "11484/5/85/e / 0",  messages.get(i++).toString() ); // e OFF /1  d=95%
			// ON/OFF overlapping with the second run (q=2)
			assertEquals( "11520/5/95/f / 64", messages.get(i++).toString() ); // f ON
			assertEquals( "11544/5/85/d / 0",  messages.get(i++).toString() ); // d OFF /1  d=95%
			assertEquals( "11580/5/95/e / 64", messages.get(i++).toString() ); // e ON
			assertEquals( "11604/5/85/c / 0",  messages.get(i++).toString() ); // c OFF /1  d=95%
			assertEquals( "11640/5/95/d / 64", messages.get(i++).toString() ); // d ON
			assertEquals( "11700/5/95/c / 64", messages.get(i++).toString() ); // c ON
			
			assertEquals( "13344/5/85/f / 0",  messages.get(i++).toString() ); // f OFF /1  d=95%
			assertEquals( "13404/5/85/e / 0",  messages.get(i++).toString() ); // e OFF /1  d=95%
			assertEquals( "13464/5/85/d / 0",  messages.get(i++).toString() ); // d OFF /1  d=95%
			assertEquals( "13524/5/85/c / 0",  messages.get(i++).toString() ); // c OFF /1  d=95%
			
			// /1 REST
			
			// cond_pattern(foo): if ==> f
			assertEquals( "15360/5/95/f / 64", messages.get(i++).toString() ); // f ON
			assertEquals( "17184/5/85/f / 0",  messages.get(i++).toString() ); // f OFF /1  d=95%
			
			// cond_pattern(bar=123): elsif ==> e
			assertEquals( "17280/5/95/e / 64", messages.get(i++).toString() ); // e ON
			assertEquals( "19104/5/85/e / 0",  messages.get(i++).toString() ); // e OFF /1  d=95%
			
			// cond_pattern(bar): else ==> d
			assertEquals( "19200/5/95/d / 64", messages.get(i++).toString() ); // d ON
			assertEquals( "21024/5/85/d / 0",  messages.get(i++).toString() ); // d OFF /1  d=95%
			
			// try_cond(foo) ==> cond_pattern(foo): if ==> f
			assertEquals( "21120/5/95/f / 64", messages.get(i++).toString() ); // f ON
			assertEquals( "22944/5/85/f / 0",  messages.get(i++).toString() ); // f OFF /1  d=95%
			
			assertEquals( 23040, instruments.get(5).getCurrentTicks() );
		}
		
		parse(getWorkingFile("pattern-magic-vars"));
		// channel 0: pat_488
		messages = getMessagesByStatus("90");
		assertEquals( 36, messages.size() );
		{
			int i = 0;
			// 0: crd1:pat_488 (crd1 = c)
			assertEquals( "0/0/90/c / 64",      messages.get(i++).toString() );
			assertEquals( "480/0/90/c / 64",    messages.get(i++).toString() );
			assertEquals( "720/0/90/c / 64",    messages.get(i++).toString() );
			// 0: crd2:pat_488 (crd2 = c+/d+)
			assertEquals( "960/0/90/c+ / 64",   messages.get(i++).toString() );
			assertEquals( "960/0/90/d+ / 64",   messages.get(i++).toString() );
			assertEquals( "1440/0/90/c+ / 64",  messages.get(i++).toString() );
			assertEquals( "1440/0/90/d+ / 64",  messages.get(i++).toString() );
			assertEquals( "1680/0/90/c+ / 64",  messages.get(i++).toString() );
			assertEquals( "1680/0/90/d+ / 64",  messages.get(i++).toString() );
			// 0  crd3 pat_488 (crd3 = c+2/d+2/e+2)
			assertEquals( "1920/0/90/c+2 / 64", messages.get(i++).toString() );
			assertEquals( "1920/0/90/d+2 / 64", messages.get(i++).toString() );
			assertEquals( "1920/0/90/e+2 / 64", messages.get(i++).toString() );
			assertEquals( "2400/0/90/c+2 / 64", messages.get(i++).toString() );
			assertEquals( "2400/0/90/d+2 / 64", messages.get(i++).toString() );
			assertEquals( "2400/0/90/e+2 / 64", messages.get(i++).toString() );
			assertEquals( "2640/0/90/c+2 / 64", messages.get(i++).toString() );
			assertEquals( "2640/0/90/d+2 / 64", messages.get(i++).toString() );
			assertEquals( "2640/0/90/e+2 / 64", messages.get(i++).toString() );
			// 0  c pat_488
			assertEquals( "2880/0/90/c / 64",   messages.get(i++).toString() );
			assertEquals( "3360/0/90/c / 64",   messages.get(i++).toString() );
			assertEquals( "3600/0/90/c / 64",   messages.get(i++).toString() );
			// 0: c+/d+:pat_488
			assertEquals( "3840/0/90/c+ / 64",  messages.get(i++).toString() );
			assertEquals( "3840/0/90/d+ / 64",  messages.get(i++).toString() );
			assertEquals( "4320/0/90/c+ / 64",  messages.get(i++).toString() );
			assertEquals( "4320/0/90/d+ / 64",  messages.get(i++).toString() );
			assertEquals( "4560/0/90/c+ / 64",  messages.get(i++).toString() );
			assertEquals( "4560/0/90/d+ / 64",  messages.get(i++).toString() );
			// 0: c+2/d+2/e+2:pat_488
			assertEquals( "4800/0/90/c+2 / 64", messages.get(i++).toString() );
			assertEquals( "4800/0/90/d+2 / 64", messages.get(i++).toString() );
			assertEquals( "4800/0/90/e+2 / 64", messages.get(i++).toString() );
			assertEquals( "5280/0/90/c+2 / 64", messages.get(i++).toString() );
			assertEquals( "5280/0/90/d+2 / 64", messages.get(i++).toString() );
			assertEquals( "5280/0/90/e+2 / 64", messages.get(i++).toString() );
			assertEquals( "5520/0/90/c+2 / 64", messages.get(i++).toString() );
			assertEquals( "5520/0/90/d+2 / 64", messages.get(i++).toString() );
			assertEquals( "5520/0/90/e+2 / 64", messages.get(i++).toString() );
		}
		// channel 1: pat_4
		messages = getMessagesByStatus("91");
		assertEquals( 6, messages.size() );
		{
			int i = 0;
			assertEquals( "0/1/91/c / 64",      messages.get(i++).toString() );
			assertEquals( "480/1/91/c+ / 64",   messages.get(i++).toString() );
			assertEquals( "480/1/91/d+ / 64",   messages.get(i++).toString() );
			assertEquals( "960/1/91/c+2 / 64",  messages.get(i++).toString() );
			assertEquals( "960/1/91/d+2 / 64",  messages.get(i++).toString() );
			assertEquals( "960/1/91/e+2 / 64",  messages.get(i++).toString() );
		}
		// channel 2: pat_order_x2_up / pat_order_x2_down
		messages = getMessagesByStatus("92");
		assertEquals( 20, messages.size() );
		{
			int i = 0;
			// 2: c+/d+:pat_order_x2_up
			assertEquals( "0/2/92/c+ / 64",      messages.get(i++).toString() );
			assertEquals( "480/2/92/d+ / 64",   messages.get(i++).toString() );
			assertEquals( "960/2/92/c+ / 64",   messages.get(i++).toString() );
			assertEquals( "1440/2/92/d+ / 64",   messages.get(i++).toString() );
			// 2: c+/d+:pat_order_x2_down
			assertEquals( "1920/2/92/d+ / 64",   messages.get(i++).toString() );
			assertEquals( "2400/2/92/c+ / 64",   messages.get(i++).toString() );
			assertEquals( "2880/2/92/d+ / 64",   messages.get(i++).toString() );
			assertEquals( "3360/2/92/c+ / 64",   messages.get(i++).toString() );
			// 2  c+2/d+2/e+2  pat_order_x2_up
			assertEquals( "3840/2/92/c+2 / 64",   messages.get(i++).toString() );
			assertEquals( "4320/2/92/d+2 / 64",   messages.get(i++).toString() );
			assertEquals( "4800/2/92/e+2 / 64",   messages.get(i++).toString() );
			assertEquals( "5280/2/92/c+2 / 64",   messages.get(i++).toString() );
			assertEquals( "5760/2/92/d+2 / 64",   messages.get(i++).toString() );
			assertEquals( "6240/2/92/e+2 / 64",   messages.get(i++).toString() );
			// 2  c+2/d+2/e+2  pat_order_x2_down
			assertEquals( "6720/2/92/e+2 / 64",   messages.get(i++).toString() );
			assertEquals( "7200/2/92/d+2 / 64",   messages.get(i++).toString() );
			assertEquals( "7680/2/92/c+2 / 64",   messages.get(i++).toString() );
			assertEquals( "8160/2/92/e+2 / 64",   messages.get(i++).toString() );
			assertEquals( "8640/2/92/d+2 / 64",   messages.get(i++).toString() );
			assertEquals( "9120/2/92/c+2 / 64",   messages.get(i++).toString() );
		}
		// channel 3: pat_outer
		messages = getMessagesByStatus("93");
		assertEquals( 14, messages.size() );
		{
			int i = 0;
			// 3: c/d/e:pat_outer
			assertEquals( "0/3/93/e / 64",      messages.get(i++).toString() ); // outer
			assertEquals( "480/3/93/c / 64",    messages.get(i++).toString() ); // inner
			assertEquals( "480/3/93/e / 64",    messages.get(i++).toString() ); // inner
			assertEquals( "960/3/93/e / 64",    messages.get(i++).toString() ); // outer
			assertEquals( "1440/3/93/c / 64",   messages.get(i++).toString() ); // outer
			assertEquals( "1440/3/93/d / 64",   messages.get(i++).toString() ); // outer
			assertEquals( "1440/3/93/e / 64",   messages.get(i++).toString() ); // outer
			// 3  c+/d+/e+ pat_outer
			assertEquals( "1920/3/93/e+ / 64",   messages.get(i++).toString() ); // outer
			assertEquals( "2400/3/93/c+ / 64",   messages.get(i++).toString() ); // inner
			assertEquals( "2400/3/93/e+ / 64",   messages.get(i++).toString() ); // inner
			assertEquals( "2880/3/93/e+ / 64",   messages.get(i++).toString() ); // outer
			assertEquals( "3360/3/93/c+ / 64",   messages.get(i++).toString() ); // outer
			assertEquals( "3360/3/93/d+ / 64",   messages.get(i++).toString() ); // outer
			assertEquals( "3360/3/93/e+ / 64",   messages.get(i++).toString() ); // outer
		}
		// channel 4: pat_4b
		messages = getMessagesByStatus("94");
		assertEquals( 3, messages.size() );
		{
			int i = 0;
			assertEquals( "0/4/94/c / 64",      messages.get(i++).toString() );
			assertEquals( "480/4/94/d+ / 64",   messages.get(i++).toString() );
			assertEquals( "960/4/94/e+2 / 64",  messages.get(i++).toString() );
		}
		// channel 9: pat_488
		messages = getMessagesByStatus("99");
		assertEquals( 9, messages.size() );
		{
			int i = 0;
			// p: sl:pat_488
			assertEquals( "0/9/99/slap (sl) / 64",      messages.get(i++).toString() );
			assertEquals( "480/9/99/slap (sl) / 64",    messages.get(i++).toString() );
			assertEquals( "720/9/99/slap (sl) / 64",    messages.get(i++).toString() );
			// p  slap/cla pat_488
			assertEquals( "960/9/99/slap (sl) / 64",    messages.get(i++).toString() );
			assertEquals( "960/9/99/clave (cla) / 64",  messages.get(i++).toString() );
			assertEquals( "1440/9/99/slap (sl) / 64",   messages.get(i++).toString() );
			assertEquals( "1440/9/99/clave (cla) / 64", messages.get(i++).toString() );
			assertEquals( "1680/9/99/slap (sl) / 64",   messages.get(i++).toString() );
			assertEquals( "1680/9/99/clave (cla) / 64", messages.get(i++).toString() );
		}
		
		parse(getWorkingFile("condition"));
		messages = getMessagesByStatus("91");
		{
			int i = 0;
			
			// CALL try_1(/32, ...)
			assertEquals( "0/1/91/f+2 / 64",    messages.get(i++).toString() );
			assertEquals( "60/1/91/f+2 / 64",   messages.get(i++).toString() );
			assertEquals( "120/1/91/a+2 / 64",  messages.get(i++).toString() );
			
			// CALL try_2(/32, ...)
			assertEquals( "180/1/91/f+3 / 64",  messages.get(i++).toString() );
			assertEquals( "240/1/91/f+3 / 64",  messages.get(i++).toString() );
			assertEquals( "300/1/91/a+3 / 64",  messages.get(i++).toString() );
			
			// call pattern pat_try_1(/16, ...)
			assertEquals( "360/1/91/f+4 / 64",  messages.get(i++).toString() );
			assertEquals( "480/1/91/f+4 / 64",  messages.get(i++).toString() );
			assertEquals( "600/1/91/a+3 / 64",  messages.get(i++).toString() );
			
			// call pattern pat_try_2(/16, ...)
			assertEquals( "720/1/91/f+4 / 64",  messages.get(i++).toString() );
			assertEquals( "840/1/91/f+4 / 64",  messages.get(i++).toString() );
			assertEquals( "960/1/91/a+3 / 64",  messages.get(i++).toString() );
			
			// CALL try_3(/32, ...)
			assertEquals( "1080/1/91/f+2 / 64", messages.get(i++).toString() );
			assertEquals( "1140/1/91/f+2 / 64", messages.get(i++).toString() );
			assertEquals( "1200/1/91/a+2 / 64", messages.get(i++).toString() );
			
			// CALL try_4(/32, ...)
			assertEquals( "1260/1/91/f+3 / 64", messages.get(i++).toString() );
			assertEquals( "1320/1/91/f+3 / 64", messages.get(i++).toString() );
			assertEquals( "1380/1/91/a+3 / 64", messages.get(i++).toString() );
			
			// call pattern pat_try_3(/16, ...)
			assertEquals( "1440/1/91/f+4 / 64", messages.get(i++).toString() );
			assertEquals( "1560/1/91/f+4 / 64", messages.get(i++).toString() );
			assertEquals( "1680/1/91/a+3 / 64", messages.get(i++).toString() );
			
			// call pattern pat_try_4(/16, ...)
			assertEquals( "1800/1/91/f+4 / 64", messages.get(i++).toString() );
			assertEquals( "1920/1/91/f+4 / 64", messages.get(i++).toString() );
			assertEquals( "2040/1/91/a+3 / 64", messages.get(i++).toString() );
		}
		
		parse(getWorkingFile("condition-2"));
		assertEquals( "2:ELSE,4:ELSE,\n1:IF,3:IF,\n2:ELSE,4:ELSE,\n", getLyrics() );
		
		parse(getWorkingFile("zerolength"));
		messages = getMessagesByStatus("90");
		{
			int i = 0;
			
			assertEquals( "0/0/90/c / 10",     messages.get(i++).toString() ); // 0  c  /1
			assertEquals( "1920/0/90/c+ / 20", messages.get(i++).toString() ); // CALL func
			
			// 0  d/e/f  pat
			assertEquals( "3840/0/90/d / 20",  messages.get(i++).toString() );
			assertEquals( "4320/0/90/e / 30",  messages.get(i++).toString() );
			assertEquals( "4800/0/90/f / 40",  messages.get(i++).toString() );
			
			// 0  d/e/f  pat  v=110
			assertEquals( "5280/0/90/d / 110", messages.get(i++).toString() );
			assertEquals( "5760/0/90/e / 30",  messages.get(i++).toString() );
			assertEquals( "6240/0/90/f / 40",  messages.get(i++).toString() );
			
			// 0  c  /1
			assertEquals( "6720/0/90/c / 110", messages.get(i++).toString() );
			
			// 0  -  -  l=xyz
			MidiEvent   event = SequenceCreator.getSequence().getTracks()[1].get(0);
			MidiMessage msg   = event.getMessage();
			MetaMessage mMsg  = (MetaMessage) msg;
			byte[]      data  = mMsg.getData();
			assertEquals( 8640, event.getTick() );
			assertEquals( "xyz", CharsetUtils.getTextFromBytes(data, "UTF-8", null) );
			
			// first note-OFF (off-tick: 80% from 1920 = 1536)
			assertEquals( "1536/0/80/c / 0", getMessagesByStatus("80").get(0).toString() );
		}
		
		parse(getWorkingFile("soundbank-url"));
		assertEquals("https://midica.org/assets/sound/soundbank-emg.sf2", SoundbankParser.getFullPath());
		assertEquals(Dict.get(Dict.SOUND_FROM_URL) + "soundbank-emg.sf2", SoundbankParser.getShortName());
		assertEquals(SoundbankParser.SOUND_FORMAT_SF2, SoundbankParser.getSoundFormat());
		assertEquals(SoundbankParser.FROM_URL, SoundbankParser.getSource());
		
		parse(getWorkingFile("compact-drum-only"));
		assertEquals(1440, instruments.get(9).getCurrentTicks());
		
		parse(getWorkingFile("compact-syntax"));
		// channel 0
		messages = getMessagesByStatus("90");
		{
			int i = 0;
			
			// root level: 0: c
			assertEquals( "0/0/90/c / 64", messages.get(i++).toString() );
			
			// root level: 0: d e:/8 f g g:/4
			assertEquals( "480/0/90/d / 64", messages.get(i++).toString() );
			assertEquals( "960/0/90/e / 64", messages.get(i++).toString() );
			assertEquals( "1200/0/90/f / 64", messages.get(i++).toString() );
			assertEquals( "1440/0/90/g / 64", messages.get(i++).toString() );
			assertEquals( "1680/0/90/g / 64", messages.get(i++).toString() );
			
			// root level: 0: a b/c+/d+/e+:/1 - f+:/8 g+:4
			assertEquals( "2160/0/90/a / 64", messages.get(i++).toString() );
			assertEquals( "2640/0/90/b / 64", messages.get(i++).toString() );
			assertEquals( "2640/0/90/c+ / 64", messages.get(i++).toString() );
			assertEquals( "2640/0/90/d+ / 64", messages.get(i++).toString() );
			assertEquals( "2640/0/90/e+ / 64", messages.get(i++).toString() );
			assertEquals( "6480/0/90/f+ / 64", messages.get(i++).toString() );
			assertEquals( "6720/0/90/g+ / 64", messages.get(i++).toString() );
			
			// t=3:2, s=-12:
				// 0: c d:8
				assertEquals( "7200/0/90/c- / 64", messages.get(i++).toString() );
				assertEquals( "7520/0/90/d- / 64", messages.get(i++).toString() );
				
				// 0: c d/e - f
				assertEquals( "7680/0/90/c- / 64", messages.get(i++).toString() );
				assertEquals( "7840/0/90/d- / 64", messages.get(i++).toString() );
				assertEquals( "7840/0/90/e- / 64", messages.get(i++).toString() );
				assertEquals( "8160/0/90/f- / 64", messages.get(i++).toString() );
				
				// 0: c d e:/2
				assertEquals( "8320/0/90/c- / 64", messages.get(i++).toString() );
				assertEquals( "8480/0/90/d- / 64", messages.get(i++).toString() );
				assertEquals( "8640/0/90/e- / 64", messages.get(i++).toString() );
				
				// t=2:1, s=+12:
					// 0: c d:8
					assertEquals( "9280/0/90/c / 64", messages.get(i++).toString() );
					assertEquals( "9600/0/90/d / 64", messages.get(i++).toString() );
					
					// 0: c d/e - f
					assertEquals( "9680/0/90/c / 64", messages.get(i++).toString() );
					assertEquals( "9760/0/90/d / 64", messages.get(i++).toString() );
					assertEquals( "9760/0/90/e / 64", messages.get(i++).toString() );
					assertEquals( "9920/0/90/f / 64", messages.get(i++).toString() );
					
					// 0: c d e:/2
					assertEquals( "10000/0/90/c / 64", messages.get(i++).toString() );
					assertEquals( "10080/0/90/d / 64", messages.get(i++).toString() );
					assertEquals( "10160/0/90/e / 64", messages.get(i++).toString() );
					
				// 0: c d:8
				assertEquals( "10480/0/90/c- / 64", messages.get(i++).toString() );
				assertEquals( "11120/0/90/d- / 64", messages.get(i++).toString() );
				
			// 0: c d:8
			assertEquals( "11280/0/90/c / 64", messages.get(i++).toString() );
			assertEquals( "11520/0/90/d / 64", messages.get(i++).toString() );
		}
		// channel 1
		messages = getMessagesByStatus("91");
		{
			int i = 0;
			assertEquals( "0/1/91/c / 64", messages.get(i++).toString() );      // /4
			assertEquals( "480/1/91/c / 64", messages.get(i++).toString() );    // /2t
			assertEquals( "1120/1/91/c / 100", messages.get(i++).toString() );  // /4tt  (q=2)
			assertEquals( "1333/1/91/c / 100", messages.get(i++).toString() );  // /4tt
			assertEquals( "1546/1/91/c / 64", messages.get(i++).toString() );   // /4t
			assertEquals( "1866/1/91/c / 64", messages.get(i++).toString() );   // /4
		}
		// channel 2
		messages = getMessagesByStatus("92");
		{
			int i = 0;
			
			// normal variables
			assertEquals( "0/2/92/c / 64", messages.get(i++).toString() );
			assertEquals( "240/2/92/c / 64", messages.get(i++).toString() );
			
			// first function call
			assertEquals( "480/2/92/c / 64", messages.get(i++).toString() );
			assertEquals( "600/2/92/c / 64", messages.get(i++).toString() );
			assertEquals( "720/2/92/d / 20", messages.get(i++).toString() );
			assertEquals( "1200/2/92/c / 30", messages.get(i++).toString() );
			
			// second function call
			assertEquals( "1320/2/92/c / 30", messages.get(i++).toString() );
			assertEquals( "1440/2/92/c / 30", messages.get(i++).toString() );
			assertEquals( "1560/2/92/d / 20", messages.get(i++).toString() );
			assertEquals( "2040/2/92/c / 30", messages.get(i++).toString() );
		}
		// channel 3
		messages = getMessagesByStatus("93");
		{
			int i = 0;
			
			assertEquals( "0/3/93/c / 64", messages.get(i++).toString() );
			
			// first pattern call
			assertEquals( "480/3/93/c / 64", messages.get(i++).toString() );
			assertEquals( "960/3/93/d / 30", messages.get(i++).toString() );   // v=30
			assertEquals( "1200/3/93/e / 30", messages.get(i++).toString() );
			assertEquals( "1320/3/93/c / 30", messages.get(i++).toString() );  // /2
			
			assertEquals( "2280/3/93/c / 64", messages.get(i++).toString() );
			
			// second pattern call
			assertEquals( "2760/3/93/c+ / 64", messages.get(i++).toString() );
			assertEquals( "3240/3/93/d+ / 40", messages.get(i++).toString() ); // v=40
			assertEquals( "3480/3/93/e+ / 40", messages.get(i++).toString() );
			assertEquals( "3600/3/93/c+ / 40", messages.get(i++).toString() ); // /4
			
			assertEquals( "4080/3/93/c / 64", messages.get(i++).toString() );
		}
		// channel 4
		messages = getMessagesByStatus("94");
		{
			int i = 0;
			
			assertEquals( "0/4/94/c / 64", messages.get(i++).toString() );
			
			// first pattern call
			assertEquals( "480/4/94/c / 64", messages.get(i++).toString() );
			assertEquals( "960/4/94/d / 30", messages.get(i++).toString() );   // v=30
			assertEquals( "1200/4/94/e / 30", messages.get(i++).toString() );
			assertEquals( "1320/4/94/c / 30", messages.get(i++).toString() );  // /2
			
			assertEquals( "2280/4/94/c / 64", messages.get(i++).toString() );
			
			// second pattern call
			assertEquals( "2760/4/94/c+ / 64", messages.get(i++).toString() );
			assertEquals( "3240/4/94/d+ / 40", messages.get(i++).toString() ); // v=40
			assertEquals( "3480/4/94/e+ / 40", messages.get(i++).toString() );
			assertEquals( "3600/4/94/c+ / 40", messages.get(i++).toString() ); // /4
			
			assertEquals( "4080/4/94/c / 64", messages.get(i++).toString() );
		}
		// channel 5
		messages = getMessagesByStatus("95");
		{
			int i = 0;
			
			{
				// c/d/e:pat_outer(70)
				assertEquals( "0/5/95/c / 70", messages.get(i++).toString() );
				assertEquals( "480/5/95/c / 70", messages.get(i++).toString() );
				assertEquals( "960/5/95/d / 70", messages.get(i++).toString() );
				assertEquals( "1440/5/95/e / 70", messages.get(i++).toString() );
					// pat_inner
					assertEquals( "1920/5/95/e / 20", messages.get(i++).toString() );
					assertEquals( "2400/5/95/d / 20", messages.get(i++).toString() );
					assertEquals( "2880/5/95/c / 20", messages.get(i++).toString() ); // /2
					assertEquals( "3840/5/95/e / 30", messages.get(i++).toString() ); // /2
					assertEquals( "4800/5/95/d / 30", messages.get(i++).toString() ); // /2
					assertEquals( "5760/5/95/e / 30", messages.get(i++).toString() ); // /2
				assertEquals( "6720/5/95/c / 70", messages.get(i++).toString() ); // /4 + -:8
				assertEquals( "7440/5/95/c / 70", messages.get(i++).toString() ); // /8
			}
			assertEquals( "7680/5/95/c+ / 64", messages.get(i++).toString() );
			{
				// e/d/c:pat_outer(60)
				assertEquals( "8160/5/95/e / 60", messages.get(i++).toString() );
				assertEquals( "8640/5/95/e / 60", messages.get(i++).toString() );
				assertEquals( "9120/5/95/d / 60", messages.get(i++).toString() );
				assertEquals( "9600/5/95/c / 60", messages.get(i++).toString() );
					// pat_inner
					assertEquals( "10080/5/95/c / 20", messages.get(i++).toString() );
					assertEquals( "10560/5/95/d / 20", messages.get(i++).toString() );
					assertEquals( "11040/5/95/e / 20", messages.get(i++).toString() ); // /2
					assertEquals( "12000/5/95/c / 30", messages.get(i++).toString() ); // /2
					assertEquals( "12960/5/95/d / 30", messages.get(i++).toString() ); // /2
					assertEquals( "13920/5/95/c / 30", messages.get(i++).toString() ); // /2
				assertEquals( "14880/5/95/e / 60", messages.get(i++).toString() ); // /4 + -:8
				assertEquals( "15600/5/95/e / 60", messages.get(i++).toString() ); // /8
			}
			assertEquals( "15840/5/95/c+ / 64", messages.get(i++).toString() );
			{
				// cmaj:pat_outer(50)
				assertEquals( "16320/5/95/c / 50", messages.get(i++).toString() );
				assertEquals( "16800/5/95/c / 50", messages.get(i++).toString() );
				assertEquals( "17280/5/95/d / 50", messages.get(i++).toString() );
				assertEquals( "17760/5/95/e / 50", messages.get(i++).toString() );
					// pat_inner
					assertEquals( "18240/5/95/e / 20", messages.get(i++).toString() );
					assertEquals( "18720/5/95/d / 20", messages.get(i++).toString() );
					assertEquals( "19200/5/95/c / 20", messages.get(i++).toString() ); // /2
					assertEquals( "20160/5/95/e / 30", messages.get(i++).toString() ); // /2
					assertEquals( "21120/5/95/d / 30", messages.get(i++).toString() ); // /2
					assertEquals( "22080/5/95/e / 30", messages.get(i++).toString() ); // /2
				assertEquals( "23040/5/95/c / 50", messages.get(i++).toString() ); // /4 + -:8
				assertEquals( "23760/5/95/c / 50", messages.get(i++).toString() ); // /8
			}
			assertEquals( "24000/5/95/c+ / 64", messages.get(i++).toString() );
			{
				// cmaj_reverse:pat_outer40)
				assertEquals( "24480/5/95/c / 40", messages.get(i++).toString() );
				assertEquals( "24960/5/95/c / 40", messages.get(i++).toString() );
				assertEquals( "25440/5/95/d / 40", messages.get(i++).toString() );
				assertEquals( "25920/5/95/e / 40", messages.get(i++).toString() );
					// pat_inner
					assertEquals( "26400/5/95/e / 20", messages.get(i++).toString() );
					assertEquals( "26880/5/95/d / 20", messages.get(i++).toString() );
					assertEquals( "27360/5/95/c / 20", messages.get(i++).toString() ); // /2
					assertEquals( "28320/5/95/e / 30", messages.get(i++).toString() ); // /2
					assertEquals( "29280/5/95/d / 30", messages.get(i++).toString() ); // /2
					assertEquals( "30240/5/95/e / 30", messages.get(i++).toString() ); // /2
				assertEquals( "31200/5/95/c / 40", messages.get(i++).toString() ); // /4 + -:8
				assertEquals( "31920/5/95/c / 40", messages.get(i++).toString() ); // /8
			}
			assertEquals( "32160/5/95/c+ / 64", messages.get(i++).toString() );
		}
		// channel 6
		messages = getMessagesByStatus("96");
		{
			int i = 0;
			
			// first call: f6
			assertEquals( "0/6/96/a / 30", messages.get(i++).toString() );
			assertEquals( "480/6/96/b / 30", messages.get(i++).toString() );
			assertEquals( "960/6/96/c / 30", messages.get(i++).toString() );
			assertEquals( "1440/6/96/d / 40", messages.get(i++).toString() );
			assertEquals( "1920/6/96/e / 40", messages.get(i++).toString() );
			assertEquals( "2400/6/96/f / 40", messages.get(i++).toString() );
			assertEquals( "2880/6/96/a+ / 127", messages.get(i++).toString() );
			assertEquals( "3360/6/96/b+ / 127", messages.get(i++).toString() );
			assertEquals( "3840/6/96/c+ / 127", messages.get(i++).toString() );
			
			// second call: f6 s=-12
			assertEquals( "4320/6/96/a- / 30", messages.get(i++).toString() );
			assertEquals( "4800/6/96/b- / 30", messages.get(i++).toString() );
			assertEquals( "5280/6/96/c- / 30", messages.get(i++).toString() );
			assertEquals( "5760/6/96/d- / 40", messages.get(i++).toString() );
			assertEquals( "6240/6/96/e- / 40", messages.get(i++).toString() );
			assertEquals( "6720/6/96/f- / 40", messages.get(i++).toString() );
			assertEquals( "7200/6/96/a / 127", messages.get(i++).toString() );
			assertEquals( "7680/6/96/b / 127", messages.get(i++).toString() );
			assertEquals( "8160/6/96/c / 127", messages.get(i++).toString() );
		}
		// channel 7
		messages = getMessagesByStatus("97");
		{
			int i = 0;
			
			assertEquals( "0/7/97/c / 64", messages.get(i++).toString() );
			
			assertEquals( "240/7/97/c / 64", messages.get(i++).toString() );
			assertEquals( "360/7/97/d / 64", messages.get(i++).toString() );
			assertEquals( "480/7/97/c / 64", messages.get(i++).toString() );
			assertEquals( "600/7/97/d / 64", messages.get(i++).toString() );
			
			assertEquals( "720/7/97/e / 64", messages.get(i++).toString() );
			assertEquals( "840/7/97/f / 64", messages.get(i++).toString() );
			assertEquals( "960/7/97/e / 64", messages.get(i++).toString() );
			assertEquals( "1080/7/97/f / 64", messages.get(i++).toString() );
		}
		// channel 8
		messages = getMessagesByStatus("98");
		{
			int i = 0;
			
			assertEquals( "0/8/98/c / 25", messages.get(i++).toString() );         // top-level
			assertEquals( "1440/8/98/d / 25", messages.get(i++).toString() );
			assertEquals( "2880/8/98/e / 25", messages.get(i++).toString() );
			
			// function
			{
				assertEquals( "4320/8/98/f / 25", messages.get(i++).toString() );  // no block
				
				assertEquals( "4440/8/98/c / 25", messages.get(i++).toString() );  // q=2
				assertEquals( "4560/8/98/d / 25", messages.get(i++).toString() );
				assertEquals( "4680/8/98/c / 25", messages.get(i++).toString() );
				assertEquals( "4800/8/98/d / 25", messages.get(i++).toString() );
				
				assertEquals( "4920/8/98/c / 25", messages.get(i++).toString() );  // no block
				
				assertEquals( "5400/8/98/c+ / 25", messages.get(i++).toString() ); // t
				assertEquals( "5560/8/98/d+ / 25", messages.get(i++).toString() );
				assertEquals( "5720/8/98/e+ / 25", messages.get(i++).toString() );
				assertEquals( "5880/8/98/c / 25", messages.get(i++).toString() );
				assertEquals( "6040/8/98/d / 25", messages.get(i++).toString() );
				assertEquals( "6680/8/98/e / 25", messages.get(i++).toString() );
				assertEquals( "7320/8/98/f / 25", messages.get(i++).toString() );
				
				assertEquals( "7960/8/98/c / 25", messages.get(i++).toString() );  // no block
				assertEquals( "8920/8/98/d / 25", messages.get(i++).toString() );
				
				assertEquals( "9400/8/98/c / 25", messages.get(i++).toString() );  // t
				assertEquals( "9720/8/98/d / 25", messages.get(i++).toString() );
				assertEquals( "10360/8/98/e / 25", messages.get(i++).toString() );
				assertEquals( "11000/8/98/f / 25", messages.get(i++).toString() );
				
				assertEquals( "11640/8/98/c / 25", messages.get(i++).toString() );  // block
				assertEquals( "12600/8/98/d / 25", messages.get(i++).toString() );
				
				assertEquals( "13560/8/98/f / 25", messages.get(i++).toString() );  // t
				assertEquals( "14200/8/98/c / 25", messages.get(i++).toString() );  // t, pat7
				assertEquals( "14440/8/98/d / 25", messages.get(i++).toString() );  // t, pat7
				assertEquals( "14680/8/98/e / 25", messages.get(i++).toString() );  // t, pat7
				assertEquals( "14920/8/98/f / 25", messages.get(i++).toString() );  // t
				assertEquals( "15560/8/98/g / 25", messages.get(i++).toString() );  // t
				
				assertEquals( "16200/8/98/c / 25", messages.get(i++).toString() );  // no block
				assertEquals( "17160/8/98/d / 25", messages.get(i++).toString() );
			}
		}
		// channel 9
		messages = getMessagesByStatus("99");
		{
			int i = 0;
			
			// root level: p: bd1:4
			assertEquals( "0/9/99/bass_drum_1 (bd1) / 64", messages.get(i++).toString() );
			assertEquals( "480/9/99/bass_drum_1 (bd1) / 64", messages.get(i++).toString() );
			assertEquals( "960/9/99/bass_drum_1 (bd1) / 64", messages.get(i++).toString() );
			
			// t
			assertEquals( "1440/9/99/bass_drum_1 (bd1) / 64", messages.get(i++).toString() );
			assertEquals( "1760/9/99/bass_drum_1 (bd1) / 64", messages.get(i++).toString() );
			assertEquals( "2080/9/99/bass_drum_1 (bd1) / 64", messages.get(i++).toString() );
			
			// tt
			assertEquals( "2400/9/99/bass_drum_1 (bd1) / 64", messages.get(i++).toString() );
			assertEquals( "2613/9/99/bass_drum_1 (bd1) / 64", messages.get(i++).toString() );
			assertEquals( "2826/9/99/bass_drum_1 (bd1) / 64", messages.get(i++).toString() );
			
			// t
			assertEquals( "3039/9/99/bass_drum_1 (bd1) / 64", messages.get(i++).toString() );
			assertEquals( "3359/9/99/bass_drum_1 (bd1) / 64", messages.get(i++).toString() );
			assertEquals( "3679/9/99/bass_drum_1 (bd1) / 64", messages.get(i++).toString() );
			
			// root level
			assertEquals( "3999/9/99/bass_drum_1 (bd1) / 64", messages.get(i++).toString() );
			assertEquals( "4479/9/99/bass_drum_1 (bd1) / 64", messages.get(i++).toString() );
			assertEquals( "4959/9/99/bass_drum_1 (bd1) / 64", messages.get(i++).toString() );
		}
		
		parse(getWorkingFile("compact-oto"));
		// channel 0
		messages = getMessagesByStatus("90");
		{
			int i = 0;
			
			// (q=4) c
			assertEquals( "0/0/90/c / 64",    messages.get(i++).toString() );
			assertEquals( "480/0/90/c / 64",  messages.get(i++).toString() );
			assertEquals( "960/0/90/c / 64",  messages.get(i++).toString() );
			assertEquals( "1440/0/90/c / 64", messages.get(i++).toString() );
			// (m) d e
			assertEquals( "1920/0/90/d / 64", messages.get(i++).toString() );
			assertEquals( "1920/0/90/e / 64", messages.get(i++).toString() );
			// f
			assertEquals( "2400/0/90/f / 64", messages.get(i++).toString() );
			// (tr=/32,q=2,m) g:8 a:4 ==> same as (m) a:4 (q=8) g:32
			assertEquals( "2880/0/90/g / 64", messages.get(i++).toString() );
			assertEquals( "2880/0/90/a / 64", messages.get(i++).toString() ); // a:4
			assertEquals( "2940/0/90/g / 64", messages.get(i++).toString() );
			assertEquals( "3000/0/90/g / 64", messages.get(i++).toString() );
			assertEquals( "3060/0/90/g / 64", messages.get(i++).toString() );
			assertEquals( "3120/0/90/g / 64", messages.get(i++).toString() );
			assertEquals( "3180/0/90/g / 64", messages.get(i++).toString() );
			assertEquals( "3240/0/90/g / 64", messages.get(i++).toString() );
			assertEquals( "3300/0/90/g / 64", messages.get(i++).toString() );
			// (m) b (m) c+ -:8 d+
			assertEquals( "3360/0/90/b / 64", messages.get(i++).toString() );
			assertEquals( "3360/0/90/c+ / 64", messages.get(i++).toString() );
			assertEquals( "3600/0/90/d+ / 64", messages.get(i++).toString() );
		}
		// channel 1
		messages = getMessagesByStatus("91");
		{
			int i = 0;
			
			// (q=2) c
			assertEquals( "0/1/91/c / 64",    messages.get(i++).toString() );
			assertEquals( "480/1/91/c / 64",  messages.get(i++).toString() );
			// { q=2 (first run)
				// (q=2) d:4
				assertEquals( "960/1/91/d / 64",  messages.get(i++).toString() );
				assertEquals( "1440/1/91/d / 64", messages.get(i++).toString() );
				// { m
					// (m) e (m) f
					assertEquals( "1920/1/91/e / 64", messages.get(i++).toString() );
					assertEquals( "1920/1/91/f / 64", messages.get(i++).toString() );
				// }
				// g:1
				assertEquals( "1920/1/91/g / 64", messages.get(i++).toString() );
			// }
			// { q=2 (second run)
				// (q=2) d:4
				assertEquals( "3840/1/91/d / 64", messages.get(i++).toString() );
				assertEquals( "4320/1/91/d / 64", messages.get(i++).toString() );
				// { m
					// e (m) f
					assertEquals( "4800/1/91/e / 64", messages.get(i++).toString() );
					assertEquals( "4800/1/91/f / 64", messages.get(i++).toString() );
				// }
				// g:1
				assertEquals( "4800/1/91/g / 64", messages.get(i++).toString() );
			// }
			// a
			assertEquals( "6720/1/91/a / 64", messages.get(i++).toString() );
		}
		// channel 2
		messages = getMessagesByStatus("92");
		{
			int i = 0;
			
			// c
			assertEquals( "0/2/92/c / 64", messages.get(i++).toString() );
			// (m,q=2) d/e:pat (first run) f:*2
				// : (q=2) 0:4
				assertEquals( "480/2/92/d / 64",  messages.get(i++).toString() );
				assertEquals( "480/2/92/f / 64",  messages.get(i++).toString() );
				assertEquals( "960/2/92/d / 64",  messages.get(i++).toString() );
				// : (m) 0 1
				assertEquals( "1440/2/92/d / 64",  messages.get(i++).toString() );
				assertEquals( "1440/2/92/e / 64",  messages.get(i++).toString() );
				// : (tr=32) 0/1:16
				assertEquals( "1920/2/92/d / 64",  messages.get(i++).toString() );
				assertEquals( "1920/2/92/e / 64",  messages.get(i++).toString() );
				assertEquals( "1980/2/92/d / 64",  messages.get(i++).toString() );
				assertEquals( "1980/2/92/e / 64",  messages.get(i++).toString() );
			// (m,q=2) d/e:pat (second run)
				// : (q=2) 0:4
				assertEquals( "2040/2/92/d / 64",  messages.get(i++).toString() );
				assertEquals( "2520/2/92/d / 64",  messages.get(i++).toString() );
				// : (m) 0 1
				assertEquals( "3000/2/92/d / 64",  messages.get(i++).toString() );
				assertEquals( "3000/2/92/e / 64",  messages.get(i++).toString() );
				// : (tr=32) 0/1:16
				assertEquals( "3480/2/92/d / 64",  messages.get(i++).toString() );
				assertEquals( "3480/2/92/e / 64",  messages.get(i++).toString() );
				assertEquals( "3540/2/92/d / 64",  messages.get(i++).toString() );
				assertEquals( "3540/2/92/e / 64",  messages.get(i++).toString() );
			// [f:*2] (already over), g
			assertEquals( "4320/2/92/g / 64",  messages.get(i++).toString() );
		}
		
		parse(getWorkingFile("bar-lines"));
		
		parse(getWorkingFile("effects-1-set"));
		// channel 0
		{
			messages = getMessagesByStatus("B0");
			int i = 0;
			
			// vol => Expression == 0B
			assertEquals( "480/0/B0-0B/10",   messages.get(i++).toString() ); // vol.wait.set(10)
			assertEquals( "960/0/B0-0B/20",   messages.get(i++).toString() ); // .wait().set(20)
			assertEquals( "1200/0/B0-0B/30",  messages.get(i++).toString() ); // .wait(/8) .set(30)
			assertEquals( "2160/0/B0-0B/40",  messages.get(i++).toString() ); // .wait.wait.set(40)
			assertEquals( "3120/0/B0-0B/50",  messages.get(i++).toString() ); // .length(/2).wait.set(50)
			assertEquals( "4080/0/B0-0B/60",  messages.get(i++).toString() ); // .wait .set(60)
			assertEquals( "4110/0/B0-0B/0",   messages.get(i++).toString() ); // .length(64).wait.set(0%)
			assertEquals( "4140/0/B0-0B/127", messages.get(i++).toString() ); // .wait.set(100.000%)
			
			// .double.wait.set(100%)
			assertEquals( "4170/0/B0-0B/127", messages.get(i++).toString() ); // MSB
			assertEquals( "4170/0/B0-2B/127", messages.get(i++).toString() ); // LSB
			
			// .double.wait.set(0/0)
			assertEquals( "4200/0/B0-0B/0",   messages.get(i++).toString() ); // MSB
			assertEquals( "4200/0/B0-2B/0",   messages.get(i++).toString() ); // LSB
			
			// .double.wait.set(127/127)
			assertEquals( "4230/0/B0-0B/127", messages.get(i++).toString() ); // MSB
			assertEquals( "4230/0/B0-2B/127", messages.get(i++).toString() ); // LSB
			
			// .double.wait.set(127/0)
			assertEquals( "4260/0/B0-0B/127", messages.get(i++).toString() ); // MSB
			assertEquals( "4260/0/B0-2B/0",   messages.get(i++).toString() ); // LSB
			
			// no further messages
			assertEquals(messages.size(), i);
		}
		// channel 1
		{
			messages = getMessagesByStatus("B1");
			int i = 0;
			
			// Balance == 08
			assertEquals( "30/1/B1-08/0",    messages.get(i++).toString() ); // balance.length(64).wait.set(-64)
			assertEquals( "60/1/B1-08/127",  messages.get(i++).toString() ); // .wait.set(+63)
			assertEquals( "90/1/B1-08/64",   messages.get(i++).toString() ); // .wait.set(+0)
			assertEquals( "120/1/B1-08/0",   messages.get(i++).toString() ); // .wait.set(-100%)
			assertEquals( "150/1/B1-08/127", messages.get(i++).toString() ); // .wait.set(+100%)
			assertEquals( "180/1/B1-08/64",  messages.get(i++).toString() ); // .wait.set(-0%)
			
			// .double.wait.set(-100%)
			assertEquals( "210/1/B1-08/0", messages.get(i++).toString() ); // MSB
			assertEquals( "210/1/B1-28/0", messages.get(i++).toString() ); // LSB
			
			// .wait.set(+100%)
			assertEquals( "240/1/B1-08/127", messages.get(i++).toString() ); // MSB
			assertEquals( "240/1/B1-28/127", messages.get(i++).toString() ); // LSB
			
			// .wait.set(+0%)
			assertEquals( "270/1/B1-08/64", messages.get(i++).toString() ); // MSB
			assertEquals( "270/1/B1-28/0",  messages.get(i++).toString() ); // LSB
			
			// .wait.set(-8192)
			assertEquals( "300/1/B1-08/0", messages.get(i++).toString() ); // MSB
			assertEquals( "300/1/B1-28/0", messages.get(i++).toString() ); // LSB
			
			// .wait.set(+8191)
			assertEquals( "330/1/B1-08/127", messages.get(i++).toString() ); // MSB
			assertEquals( "330/1/B1-28/127", messages.get(i++).toString() ); // LSB
			
			// .wait.set(-0)
			assertEquals( "360/1/B1-08/64", messages.get(i++).toString() ); // MSB
			assertEquals( "360/1/B1-28/0",  messages.get(i++).toString() ); // LSB
			
			// .wait.set(64/0)
			assertEquals( "390/1/B1-08/64", messages.get(i++).toString() ); // MSB
			assertEquals( "390/1/B1-28/0",  messages.get(i++).toString() ); // LSB
			
			// no further messages
			assertEquals(messages.size(), i);
		}
		// channel 2
		{
			messages = getMessagesByStatus("B2");
			int i = 0;
			
			// Chorus == 5D
			assertEquals( "30/2/B2-5D/0",    messages.get(i++).toString() ); // chorus.length(64).wait.set(0)
			assertEquals( "60/2/B2-5D/127",  messages.get(i++).toString() ); // .wait.set(127)
			assertEquals( "90/2/B2-5D/0",    messages.get(i++).toString() ); // .wait.set(0)
			assertEquals( "120/2/B2-5D/127", messages.get(i++).toString() ); // .wait.set(100%)
			assertEquals( "150/2/B2-5D/0",   messages.get(i++).toString() ); // .wait.set(0%)
			assertEquals( "180/2/B2-5D/64",  messages.get(i++).toString() ); // .wait.set(0%)
			
			// no further messages
			assertEquals(messages.size(), i);
		}
		// channel 3
		{
			messages = getMessagesByStatus("B3");
			int i = 0;
			
			// hold pedal == 40
			assertEquals( "30/3/B3-40/ON",  messages.get(i++).toString() ); // hold.length(64).wait.on()
			assertEquals( "60/3/B3-40/OFF", messages.get(i++).toString() ); // .wait.off()
			
			// no further messages
			assertEquals(messages.size(), i);
		}
		// channel 4
		{
			messages = getMessagesByStatus("B4");
			int i = 0;
			
			// ctrl=80 == 0x50
			assertEquals( "30/4/B4-50/127",  messages.get(i++).toString() ); // length(64).wait.ctrl=80.on()
			assertEquals( "60/4/B4-50/0",    messages.get(i++).toString() ); // .wait.off()
			assertEquals( "90/4/B4-50/127",  messages.get(i++).toString() ); // .wait.set(100%)
			assertEquals( "120/4/B4-50/0",   messages.get(i++).toString() ); // .wait.set(0%)
			assertEquals( "150/4/B4-50/64",  messages.get(i++).toString() ); // .wait.set(50%)
			assertEquals( "180/4/B4-50/127", messages.get(i++).toString() ); // .wait.set(127)
			assertEquals( "210/4/B4-50/0",   messages.get(i++).toString() ); // .wait.set(0)
			assertEquals( "240/4/B4-50/0",   messages.get(i++).toString() ); // .wait.set(-100%)
			assertEquals( "270/4/B4-50/16",  messages.get(i++).toString() ); // .wait.set(-75%)
			assertEquals( "300/4/B4-50/32",  messages.get(i++).toString() ); // .wait.set(-50%)
			assertEquals( "330/4/B4-50/48",  messages.get(i++).toString() ); // .wait.set(-25%)
			assertEquals( "360/4/B4-50/64",  messages.get(i++).toString() ); // .wait.set(-0%)
			assertEquals( "390/4/B4-50/64",  messages.get(i++).toString() ); // .wait.set(+0%)
			assertEquals( "420/4/B4-50/79",  messages.get(i++).toString() ); // .wait.set(+25%)
			assertEquals( "450/4/B4-50/95",  messages.get(i++).toString() ); // .wait.set(+50%)
			assertEquals( "480/4/B4-50/111", messages.get(i++).toString() ); // .wait.set(+75%)
			assertEquals( "510/4/B4-50/127", messages.get(i++).toString() ); // .wait.set(+100%)
			assertEquals( "540/4/B4-50/64",  messages.get(i++).toString() ); // .wait.set(-0)
			assertEquals( "570/4/B4-50/0",   messages.get(i++).toString() ); // .wait.set(-64)
			assertEquals( "600/4/B4-50/127", messages.get(i++).toString() ); // .wait.set(+63)
			assertEquals( "630/4/B4-50/64",  messages.get(i++).toString() ); // .wait.set(+0)
			
			// no further messages
			assertEquals(messages.size(), i);
		}
		// channel 5
		{
			messages = getMessagesByStatus("B5");
			int i = 0;
			
			// ctrl=7B == 123
			assertEquals( "30/5/B5-7B/0",  messages.get(i++).toString() ); // ctrl=123.length(64).wait.on()
			
			// no further messages
			assertEquals(messages.size(), i);
		}
		// channel 6
		{
			messages = getMessagesByStatus("B6");
			int i = 0;
			
			// modulation depth range == RPN 00/05
			{
				// tick 60: mod_range.length(32).wait.set(0)
				assertEquals( "30/6/B6-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
				assertEquals( "40/6/B6-64/5",   messages.get(i++).toString() ); // RPN LSB: 5
				assertEquals( "50/6/B6-06/0",   messages.get(i++).toString() ); // data MSB: 0
				assertEquals( "70/6/B6-65/127", messages.get(i++).toString() ); // MSB reset
				assertEquals( "70/6/B6-64/127", messages.get(i++).toString() ); // LSB reset
			}
			{
				// tick 120: .wait.set(127)
				assertEquals( "90/6/B6-65/0",    messages.get(i++).toString() ); // RPN MSB: 0
				assertEquals( "100/6/B6-64/5",   messages.get(i++).toString() ); // RPN LSB: 5
				assertEquals( "110/6/B6-06/127", messages.get(i++).toString() ); // data MSB: 127
				assertEquals( "130/6/B6-65/127", messages.get(i++).toString() ); // MSB reset
				assertEquals( "130/6/B6-64/127", messages.get(i++).toString() ); // LSB reset
			}
			{
				// tick 180: .wait.set(0%)
				assertEquals( "150/6/B6-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
				assertEquals( "160/6/B6-64/5",   messages.get(i++).toString() ); // RPN LSB: 5
				assertEquals( "170/6/B6-06/0",   messages.get(i++).toString() ); // data MSB: 0
				assertEquals( "190/6/B6-65/127", messages.get(i++).toString() ); // MSB reset
				assertEquals( "190/6/B6-64/127", messages.get(i++).toString() ); // LSB reset
			}
			{
				// tick 240: .wait.set(100%)
				assertEquals( "210/6/B6-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
				assertEquals( "220/6/B6-64/5",   messages.get(i++).toString() ); // RPN LSB: 5
				assertEquals( "230/6/B6-06/127", messages.get(i++).toString() ); // data MSB: 127
				assertEquals( "250/6/B6-65/127", messages.get(i++).toString() ); // MSB reset
				assertEquals( "250/6/B6-64/127", messages.get(i++).toString() ); // LSB reset
			}
			{
				// tick 300: .double.wait.set(0)
				assertEquals( "270/6/B6-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
				assertEquals( "280/6/B6-64/5",   messages.get(i++).toString() ); // RPN LSB: 5
				assertEquals( "290/6/B6-06/0",   messages.get(i++).toString() ); // data MSB: 0
				assertEquals( "300/6/B6-26/0",   messages.get(i++).toString() ); // data LSB: 0
				assertEquals( "310/6/B6-65/127", messages.get(i++).toString() ); // MSB reset
				assertEquals( "310/6/B6-64/127", messages.get(i++).toString() ); // LSB reset
			}
			{
				// tick 360: .wait.set(16383)
				assertEquals( "330/6/B6-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
				assertEquals( "340/6/B6-64/5",   messages.get(i++).toString() ); // RPN LSB: 5
				assertEquals( "350/6/B6-06/127", messages.get(i++).toString() ); // data MSB: 127
				assertEquals( "360/6/B6-26/127", messages.get(i++).toString() ); // data LSB: 127
				assertEquals( "370/6/B6-65/127", messages.get(i++).toString() ); // MSB reset
				assertEquals( "370/6/B6-64/127", messages.get(i++).toString() ); // LSB reset
			}
			{
				// tick 420: .wait.set(0%)
				assertEquals( "390/6/B6-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
				assertEquals( "400/6/B6-64/5",   messages.get(i++).toString() ); // RPN LSB: 5
				assertEquals( "410/6/B6-06/0",   messages.get(i++).toString() ); // data MSB: 0
				assertEquals( "420/6/B6-26/0",   messages.get(i++).toString() ); // data LSB: 0
				assertEquals( "430/6/B6-65/127", messages.get(i++).toString() ); // MSB reset
				assertEquals( "430/6/B6-64/127", messages.get(i++).toString() ); // LSB reset
			}
			{
				// tick 480: .wait.set(50%)
				assertEquals( "450/6/B6-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
				assertEquals( "460/6/B6-64/5",   messages.get(i++).toString() ); // RPN LSB: 5
				assertEquals( "470/6/B6-06/64",  messages.get(i++).toString() ); // data MSB: 64
				assertEquals( "480/6/B6-26/0",   messages.get(i++).toString() ); // data LSB: 0
				assertEquals( "490/6/B6-65/127", messages.get(i++).toString() ); // MSB reset
				assertEquals( "490/6/B6-64/127", messages.get(i++).toString() ); // LSB reset
			}
			{
				// tick 540: .wait.set(100%)
				assertEquals( "510/6/B6-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
				assertEquals( "520/6/B6-64/5",   messages.get(i++).toString() ); // RPN LSB: 5
				assertEquals( "530/6/B6-06/127", messages.get(i++).toString() ); // data MSB: 127
				assertEquals( "540/6/B6-26/127", messages.get(i++).toString() ); // data LSB: 127
				assertEquals( "550/6/B6-65/127", messages.get(i++).toString() ); // MSB reset
				assertEquals( "550/6/B6-64/127", messages.get(i++).toString() ); // LSB reset
			}
			{
				// tick 960: rpn=0/0.set(50%)
				assertEquals( "930/6/B6-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
				assertEquals( "940/6/B6-64/5",   messages.get(i++).toString() ); // RPN LSB: 5
				assertEquals( "950/6/B6-06/64",  messages.get(i++).toString() ); // data MSB: 64
				assertEquals( "970/6/B6-65/127", messages.get(i++).toString() ); // MSB reset
				assertEquals( "970/6/B6-64/127", messages.get(i++).toString() ); // LSB reset
			}
			{
				// tick 1440: rpn=0.set(0%)
				assertEquals( "1410/6/B6-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
				assertEquals( "1420/6/B6-64/5",   messages.get(i++).toString() ); // RPN LSB: 5
				assertEquals( "1430/6/B6-06/0",   messages.get(i++).toString() ); // data MSB: 0
				assertEquals( "1450/6/B6-65/127", messages.get(i++).toString() ); // MSB reset
				assertEquals( "1450/6/B6-64/127", messages.get(i++).toString() ); // LSB reset
			}
			{
				// tick 1920: rpn=0/5.double.set(12/30)
				assertEquals( "1890/6/B6-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
				assertEquals( "1900/6/B6-64/5",   messages.get(i++).toString() ); // RPN LSB: 5
				assertEquals( "1910/6/B6-06/12",  messages.get(i++).toString() ); // data MSB: 12
				assertEquals( "1920/6/B6-26/30",  messages.get(i++).toString() ); // data LSB: 30
				assertEquals( "1930/6/B6-65/127", messages.get(i++).toString() ); // MSB reset
				assertEquals( "1930/6/B6-64/127", messages.get(i++).toString() ); // LSB reset
			}
			
			// no further messages
			assertEquals(messages.size(), i);
		}
		// channel 7
		{
			messages = getMessagesByStatus("B7");
			int i = 0;
			
			// channel coarse tuning == RPN 00/02
			{
				// tick 60: coarse_tune.length(32).wait.set(-64.0)
				assertEquals( "30/7/B7-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
				assertEquals( "40/7/B7-64/2",   messages.get(i++).toString() ); // RPN LSB: 2
				assertEquals( "50/7/B7-06/0",   messages.get(i++).toString() ); // data MSB: 0
				assertEquals( "70/7/B7-65/127", messages.get(i++).toString() ); // MSB reset
				assertEquals( "70/7/B7-64/127", messages.get(i++).toString() ); // LSB reset
			}
			{
				// tick 120: .wait.set(-12)
				assertEquals( "90/7/B7-65/0",    messages.get(i++).toString() ); // RPN MSB: 0
				assertEquals( "100/7/B7-64/2",   messages.get(i++).toString() ); // RPN LSB: 2
				assertEquals( "110/7/B7-06/52",  messages.get(i++).toString() ); // data MSB: 52
				assertEquals( "130/7/B7-65/127", messages.get(i++).toString() ); // MSB reset
				assertEquals( "130/7/B7-64/127", messages.get(i++).toString() ); // LSB reset
			}
			{
				// tick 180: .wait.set(-1.0)
				assertEquals( "150/7/B7-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
				assertEquals( "160/7/B7-64/2",   messages.get(i++).toString() ); // RPN LSB: 2
				assertEquals( "170/7/B7-06/63",  messages.get(i++).toString() ); // data MSB: 63
				assertEquals( "190/7/B7-65/127", messages.get(i++).toString() ); // MSB reset
				assertEquals( "190/7/B7-64/127", messages.get(i++).toString() ); // LSB reset
			}
			{
				// tick 240: .wait.set(+0)
				assertEquals( "210/7/B7-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
				assertEquals( "220/7/B7-64/2",   messages.get(i++).toString() ); // RPN LSB: 2
				assertEquals( "230/7/B7-06/64",  messages.get(i++).toString() ); // data MSB: 64
				assertEquals( "250/7/B7-65/127", messages.get(i++).toString() ); // MSB reset
				assertEquals( "250/7/B7-64/127", messages.get(i++).toString() ); // LSB reset
			}
			{
				// tick 300: .wait.set(+1)
				assertEquals( "270/7/B7-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
				assertEquals( "280/7/B7-64/2",   messages.get(i++).toString() ); // RPN LSB: 2
				assertEquals( "290/7/B7-06/65",  messages.get(i++).toString() ); // data MSB: 65
				assertEquals( "310/7/B7-65/127", messages.get(i++).toString() ); // MSB reset
				assertEquals( "310/7/B7-64/127", messages.get(i++).toString() ); // LSB reset
			}
			{
				// tick 360: .wait.set(+12.0)
				assertEquals( "330/7/B7-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
				assertEquals( "340/7/B7-64/2",   messages.get(i++).toString() ); // RPN LSB: 2
				assertEquals( "350/7/B7-06/76",  messages.get(i++).toString() ); // data MSB: 76
				assertEquals( "370/7/B7-65/127", messages.get(i++).toString() ); // MSB reset
				assertEquals( "370/7/B7-64/127", messages.get(i++).toString() ); // LSB reset
			}
			{
				// tick 420: .wait.set(+63)
				assertEquals( "390/7/B7-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
				assertEquals( "400/7/B7-64/2",   messages.get(i++).toString() ); // RPN LSB: 2
				assertEquals( "410/7/B7-06/127", messages.get(i++).toString() ); // data MSB: 127
				assertEquals( "430/7/B7-65/127", messages.get(i++).toString() ); // MSB reset
				assertEquals( "430/7/B7-64/127", messages.get(i++).toString() ); // LSB reset
			}
			{
				// c:2
				// tick 960: rpn=0/2.set(+12.0)
				assertEquals( "930/7/B7-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
				assertEquals( "940/7/B7-64/2",   messages.get(i++).toString() ); // RPN LSB: 2
				assertEquals( "950/7/B7-06/76",  messages.get(i++).toString() ); // data MSB: 76
				assertEquals( "970/7/B7-65/127", messages.get(i++).toString() ); // MSB reset
				assertEquals( "970/7/B7-64/127", messages.get(i++).toString() ); // LSB reset
			}
			{
				// c:4
				// tick 1440: rpn=2.set(-0.0)
				assertEquals( "1410/7/B7-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
				assertEquals( "1420/7/B7-64/2",   messages.get(i++).toString() ); // RPN LSB: 2
				assertEquals( "1430/7/B7-06/64",  messages.get(i++).toString() ); // data MSB: 64
				assertEquals( "1450/7/B7-65/127", messages.get(i++).toString() ); // MSB reset
				assertEquals( "1450/7/B7-64/127", messages.get(i++).toString() ); // LSB reset
			}
			
			// channel fine tuning == RPN 00/01
			{
				// c:4
				// tick 1980: fine_tune.length(32).wait.set(-100%)
				assertEquals( "1950/7/B7-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
				assertEquals( "1960/7/B7-64/1",   messages.get(i++).toString() ); // RPN LSB: 1
				assertEquals( "1970/7/B7-06/0",   messages.get(i++).toString() ); // data MSB: 0
				assertEquals( "1990/7/B7-65/127", messages.get(i++).toString() ); // MSB reset
				assertEquals( "1990/7/B7-64/127", messages.get(i++).toString() ); // LSB reset
			}
			{
				// tick 2040: .wait.set(-50.0%)
				assertEquals( "2010/7/B7-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
				assertEquals( "2020/7/B7-64/1",   messages.get(i++).toString() ); // RPN LSB: 1
				assertEquals( "2030/7/B7-06/32",  messages.get(i++).toString() ); // data MSB: 32
				assertEquals( "2050/7/B7-65/127", messages.get(i++).toString() ); // MSB reset
				assertEquals( "2050/7/B7-64/127", messages.get(i++).toString() ); // LSB reset
			}
			{
				// tick 2100: .wait.set(-0.0%)
				assertEquals( "2070/7/B7-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
				assertEquals( "2080/7/B7-64/1",   messages.get(i++).toString() ); // RPN LSB: 1
				assertEquals( "2090/7/B7-06/64",  messages.get(i++).toString() ); // data MSB: 64
				assertEquals( "2110/7/B7-65/127", messages.get(i++).toString() ); // MSB reset
				assertEquals( "2110/7/B7-64/127", messages.get(i++).toString() ); // LSB reset
			}
			{
				// tick 2160: .wait.set(+50%)
				assertEquals( "2130/7/B7-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
				assertEquals( "2140/7/B7-64/1",   messages.get(i++).toString() ); // RPN LSB: 1
				assertEquals( "2150/7/B7-06/96",  messages.get(i++).toString() ); // data MSB: 96
				assertEquals( "2170/7/B7-65/127", messages.get(i++).toString() ); // MSB reset
				assertEquals( "2170/7/B7-64/127", messages.get(i++).toString() ); // LSB reset
			}
			{
				// tick 2220: .wait.set(+100%)
				assertEquals( "2190/7/B7-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
				assertEquals( "2200/7/B7-64/1",   messages.get(i++).toString() ); // RPN LSB: 1
				assertEquals( "2210/7/B7-06/127", messages.get(i++).toString() ); // data MSB: 127
				assertEquals( "2230/7/B7-65/127", messages.get(i++).toString() ); // MSB reset
				assertEquals( "2230/7/B7-64/127", messages.get(i++).toString() ); // LSB reset
			}
			{
				// tick 2280: .wait.set(-1.0)
				assertEquals( "2250/7/B7-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
				assertEquals( "2260/7/B7-64/1",   messages.get(i++).toString() ); // RPN LSB: 1
				assertEquals( "2270/7/B7-06/0",   messages.get(i++).toString() ); // data MSB: 0
				assertEquals( "2290/7/B7-65/127", messages.get(i++).toString() ); // MSB reset
				assertEquals( "2290/7/B7-64/127", messages.get(i++).toString() ); // LSB reset
			}
			{
				// tick 2340: .wait.set(-0.5)
				assertEquals( "2310/7/B7-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
				assertEquals( "2320/7/B7-64/1",   messages.get(i++).toString() ); // RPN LSB: 1
				assertEquals( "2330/7/B7-06/32",  messages.get(i++).toString() ); // data MSB: 32
				assertEquals( "2350/7/B7-65/127", messages.get(i++).toString() ); // MSB reset
				assertEquals( "2350/7/B7-64/127", messages.get(i++).toString() ); // LSB reset
			}
			{
				// tick 2400: .wait.set(-0)
				assertEquals( "2370/7/B7-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
				assertEquals( "2380/7/B7-64/1",   messages.get(i++).toString() ); // RPN LSB: 1
				assertEquals( "2390/7/B7-06/64",   messages.get(i++).toString() ); // data MSB: 64
				assertEquals( "2410/7/B7-65/127", messages.get(i++).toString() ); // MSB reset
				assertEquals( "2410/7/B7-64/127", messages.get(i++).toString() ); // LSB reset
			}
			{
				// tick 2460: .wait.set(+0.5)
				assertEquals( "2430/7/B7-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
				assertEquals( "2440/7/B7-64/1",   messages.get(i++).toString() ); // RPN LSB: 1
				assertEquals( "2450/7/B7-06/96",  messages.get(i++).toString() ); // data MSB: 96
				assertEquals( "2470/7/B7-65/127", messages.get(i++).toString() ); // MSB reset
				assertEquals( "2470/7/B7-64/127", messages.get(i++).toString() ); // LSB reset
			}
			{
				// tick 2520: .wait.set(+1.0)
				assertEquals( "2490/7/B7-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
				assertEquals( "2500/7/B7-64/1",   messages.get(i++).toString() ); // RPN LSB: 1
				assertEquals( "2510/7/B7-06/127", messages.get(i++).toString() ); // data MSB: 127
				assertEquals( "2530/7/B7-65/127", messages.get(i++).toString() ); // MSB reset
				assertEquals( "2530/7/B7-64/127", messages.get(i++).toString() ); // LSB reset
			}
			{
				// tick 2580: .double.wait.set(-100%)
				assertEquals( "2550/7/B7-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
				assertEquals( "2560/7/B7-64/1",   messages.get(i++).toString() ); // RPN LSB: 1
				assertEquals( "2570/7/B7-06/0",   messages.get(i++).toString() ); // data MSB: 0
				assertEquals( "2580/7/B7-26/0",   messages.get(i++).toString() ); // data LSB: 0
				assertEquals( "2590/7/B7-65/127", messages.get(i++).toString() ); // MSB reset
				assertEquals( "2590/7/B7-64/127", messages.get(i++).toString() ); // LSB reset
			}
			{
				// tick 2640: .wait.set(-50%)
				assertEquals( "2610/7/B7-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
				assertEquals( "2620/7/B7-64/1",   messages.get(i++).toString() ); // RPN LSB: 1
				assertEquals( "2630/7/B7-06/32",  messages.get(i++).toString() ); // data MSB: 32
				assertEquals( "2640/7/B7-26/0",   messages.get(i++).toString() ); // data LSB: 0
				assertEquals( "2650/7/B7-65/127", messages.get(i++).toString() ); // MSB reset
				assertEquals( "2650/7/B7-64/127", messages.get(i++).toString() ); // LSB reset
			}
			{
				// tick 2700: .wait.set(+0.0%)
				assertEquals( "2670/7/B7-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
				assertEquals( "2680/7/B7-64/1",   messages.get(i++).toString() ); // RPN LSB: 1
				assertEquals( "2690/7/B7-06/64",  messages.get(i++).toString() ); // data MSB: 64
				assertEquals( "2700/7/B7-26/0",   messages.get(i++).toString() ); // data LSB: 0
				assertEquals( "2710/7/B7-65/127", messages.get(i++).toString() ); // MSB reset
				assertEquals( "2710/7/B7-64/127", messages.get(i++).toString() ); // LSB reset
			}
			{
				// tick 2760: .wait.set(+50%)
				assertEquals( "2730/7/B7-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
				assertEquals( "2740/7/B7-64/1",   messages.get(i++).toString() ); // RPN LSB: 1
				assertEquals( "2750/7/B7-06/96",  messages.get(i++).toString() ); // data MSB: 96
				assertEquals( "2760/7/B7-26/0",   messages.get(i++).toString() ); // data LSB: 0
				assertEquals( "2770/7/B7-65/127", messages.get(i++).toString() ); // MSB reset
				assertEquals( "2770/7/B7-64/127", messages.get(i++).toString() ); // LSB reset
			}
			{
				// tick 2820: .wait.set(+100%)
				assertEquals( "2790/7/B7-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
				assertEquals( "2800/7/B7-64/1",   messages.get(i++).toString() ); // RPN LSB: 1
				assertEquals( "2810/7/B7-06/127", messages.get(i++).toString() ); // data MSB: 127
				assertEquals( "2820/7/B7-26/127", messages.get(i++).toString() ); // data LSB: 127
				assertEquals( "2830/7/B7-65/127", messages.get(i++).toString() ); // MSB reset
				assertEquals( "2830/7/B7-64/127", messages.get(i++).toString() ); // LSB reset
			}
			{
				// tick 2880: .wait.set(-1.0)
				assertEquals( "2850/7/B7-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
				assertEquals( "2860/7/B7-64/1",   messages.get(i++).toString() ); // RPN LSB: 1
				assertEquals( "2870/7/B7-06/0",   messages.get(i++).toString() ); // data MSB: 0
				assertEquals( "2880/7/B7-26/0",   messages.get(i++).toString() ); // data LSB: 0
				assertEquals( "2890/7/B7-65/127", messages.get(i++).toString() ); // MSB reset
				assertEquals( "2890/7/B7-64/127", messages.get(i++).toString() ); // LSB reset
			}
			{
				// tick 2940: .wait.set(-0.5)
				assertEquals( "2910/7/B7-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
				assertEquals( "2920/7/B7-64/1",   messages.get(i++).toString() ); // RPN LSB: 1
				assertEquals( "2930/7/B7-06/32",  messages.get(i++).toString() ); // data MSB: 32
				assertEquals( "2940/7/B7-26/0",   messages.get(i++).toString() ); // data LSB: 0
				assertEquals( "2950/7/B7-65/127", messages.get(i++).toString() ); // MSB reset
				assertEquals( "2950/7/B7-64/127", messages.get(i++).toString() ); // LSB reset
			}
			{
				// tick 3000: .wait.set(-0)
				assertEquals( "2970/7/B7-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
				assertEquals( "2980/7/B7-64/1",   messages.get(i++).toString() ); // RPN LSB: 1
				assertEquals( "2990/7/B7-06/64",  messages.get(i++).toString() ); // data MSB: 64
				assertEquals( "3000/7/B7-26/0",   messages.get(i++).toString() ); // data LSB: 0
				assertEquals( "3010/7/B7-65/127", messages.get(i++).toString() ); // MSB reset
				assertEquals( "3010/7/B7-64/127", messages.get(i++).toString() ); // LSB reset
			}
			{
				// tick 3060: .wait.set(+0.5)
				assertEquals( "3030/7/B7-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
				assertEquals( "3040/7/B7-64/1",   messages.get(i++).toString() ); // RPN LSB: 1
				assertEquals( "3050/7/B7-06/96",  messages.get(i++).toString() ); // data MSB: 96
				assertEquals( "3060/7/B7-26/0",   messages.get(i++).toString() ); // data LSB: 0
				assertEquals( "3070/7/B7-65/127", messages.get(i++).toString() ); // MSB reset
				assertEquals( "3070/7/B7-64/127", messages.get(i++).toString() ); // LSB reset
			}
			{
				// tick 3120: .wait.set(+1.0)
				assertEquals( "3090/7/B7-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
				assertEquals( "3100/7/B7-64/1",   messages.get(i++).toString() ); // RPN LSB: 1
				assertEquals( "3110/7/B7-06/127", messages.get(i++).toString() ); // data MSB: 127
				assertEquals( "3120/7/B7-26/127", messages.get(i++).toString() ); // data LSB: 127
				assertEquals( "3130/7/B7-65/127", messages.get(i++).toString() ); // MSB reset
				assertEquals( "3130/7/B7-64/127", messages.get(i++).toString() ); // LSB reset
			}
			{
				// c:1
				// tick 3840: rpn=0/1.double.set(+0.5)
				assertEquals( "3810/7/B7-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
				assertEquals( "3820/7/B7-64/1",   messages.get(i++).toString() ); // RPN LSB: 1
				assertEquals( "3830/7/B7-06/96",  messages.get(i++).toString() ); // data MSB: 96
				assertEquals( "3840/7/B7-26/0",   messages.get(i++).toString() ); // data LSB: 0
				assertEquals( "3850/7/B7-65/127", messages.get(i++).toString() ); // MSB reset
				assertEquals( "3850/7/B7-64/127", messages.get(i++).toString() ); // LSB reset
			}
			{
				// c:4
				// tick 4320: rpn=1.double.set(+0.0)
				assertEquals( "4290/7/B7-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
				assertEquals( "4300/7/B7-64/1",   messages.get(i++).toString() ); // RPN LSB: 1
				assertEquals( "4310/7/B7-06/64",  messages.get(i++).toString() ); // data MSB: 64
				assertEquals( "4320/7/B7-26/0",   messages.get(i++).toString() ); // data LSB: 0
				assertEquals( "4330/7/B7-65/127", messages.get(i++).toString() ); // MSB reset
				assertEquals( "4330/7/B7-64/127", messages.get(i++).toString() ); // LSB reset
			}
			
			// no further messages
			assertEquals(messages.size(), i);
		}
		// channel 8
		{
			messages = getMessagesByStatus("B8");
			int i = 0;
			
			// NRPN 04/07 == NRPN 519
			{
				// tick 60: nrpn=4/7.length(32).wait.double.set(100%)
				assertEquals( "30/8/B8-63/4",   messages.get(i++).toString() ); // NRPN MSB: 4
				assertEquals( "40/8/B8-62/7",   messages.get(i++).toString() ); // NRPN LSB: 7
				assertEquals( "50/8/B8-06/127", messages.get(i++).toString() ); // data MSB: 127
				assertEquals( "60/8/B8-26/127", messages.get(i++).toString() ); // data LSB: 127
				assertEquals( "70/8/B8-63/127", messages.get(i++).toString() ); // MSB reset
				assertEquals( "70/8/B8-62/127", messages.get(i++).toString() ); // LSB reset
			}
			{
				// tick 540: nrpn=519.length(32).wait.double.set(50%)
				assertEquals( "510/8/B8-63/4",   messages.get(i++).toString() ); // NRPN MSB: 4
				assertEquals( "520/8/B8-62/7",   messages.get(i++).toString() ); // NRPN LSB: 7
				assertEquals( "530/8/B8-06/64",  messages.get(i++).toString() ); // data MSB: 64
				assertEquals( "540/8/B8-26/0",   messages.get(i++).toString() ); // data LSB: 0
				assertEquals( "550/8/B8-63/127", messages.get(i++).toString() ); // MSB reset
				assertEquals( "550/8/B8-62/127", messages.get(i++).toString() ); // LSB reset
			}
			{
				// tick 1020: nrpn=4/7.length(32).wait.double.set(127/64)
				assertEquals( "990/8/B8-63/4",    messages.get(i++).toString() ); // NRPN MSB: 4
				assertEquals( "1000/8/B8-62/7",   messages.get(i++).toString() ); // NRPN LSB: 7
				assertEquals( "1010/8/B8-06/127", messages.get(i++).toString() ); // data MSB: 127
				assertEquals( "1020/8/B8-26/64",  messages.get(i++).toString() ); // data LSB: 64
				assertEquals( "1030/8/B8-63/127", messages.get(i++).toString() ); // MSB reset
				assertEquals( "1030/8/B8-62/127", messages.get(i++).toString() ); // LSB reset
			}
			
			// no further messages
			assertEquals(messages.size(), i);
		}
		
		parse(getWorkingFile("effects-2-pitch"));
		// channel 0: pitch bend range
		{
			messages = getMessagesByStatus("B0");
			int i = 0;
			
			// tick 60: pitch_range.length(32).wait.set(0.0)
			assertEquals( "30/0/B0-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
			assertEquals( "40/0/B0-64/0",   messages.get(i++).toString() ); // RPN LSB: 0
			assertEquals( "50/0/B0-06/0",   messages.get(i++).toString() ); // data MSB: 0
			assertEquals( "70/0/B0-65/127", messages.get(i++).toString() ); // MSB reset
			assertEquals( "70/0/B0-64/127", messages.get(i++).toString() ); // LSB reset
			
			// tick 120: .wait.set(127.0)
			assertEquals( "90/0/B0-65/0",    messages.get(i++).toString() ); // RPN MSB: 0
			assertEquals( "100/0/B0-64/0",   messages.get(i++).toString() ); // RPN LSB: 0
			assertEquals( "110/0/B0-06/127", messages.get(i++).toString() ); // data MSB: 127
			assertEquals( "130/0/B0-65/127", messages.get(i++).toString() ); // MSB reset
			assertEquals( "130/0/B0-64/127", messages.get(i++).toString() ); // LSB reset
			
			// tick 180: .wait.set(12.7)
			assertEquals( "150/0/B0-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
			assertEquals( "160/0/B0-64/0",   messages.get(i++).toString() ); // RPN LSB: 0
			assertEquals( "170/0/B0-06/13",  messages.get(i++).toString() ); // data MSB: 13
			assertEquals( "190/0/B0-65/127", messages.get(i++).toString() ); // MSB reset
			assertEquals( "190/0/B0-64/127", messages.get(i++).toString() ); // LSB reset
			
			// tick 240: .wait.set(12.0)
			assertEquals( "210/0/B0-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
			assertEquals( "220/0/B0-64/0",   messages.get(i++).toString() ); // RPN LSB: 0
			assertEquals( "230/0/B0-06/12",  messages.get(i++).toString() ); // data MSB: 12
			assertEquals( "250/0/B0-65/127", messages.get(i++).toString() ); // MSB reset
			assertEquals( "250/0/B0-64/127", messages.get(i++).toString() ); // LSB reset
			
			// tick 300: .wait.set(127)
			assertEquals( "270/0/B0-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
			assertEquals( "280/0/B0-64/0",   messages.get(i++).toString() ); // RPN LSB: 0
			assertEquals( "290/0/B0-06/127", messages.get(i++).toString() ); // data MSB: 127
			assertEquals( "310/0/B0-65/127", messages.get(i++).toString() ); // MSB reset
			assertEquals( "310/0/B0-64/127", messages.get(i++).toString() ); // LSB reset
			
			// tick 360: .double.wait.set(0.0)
			assertEquals( "330/0/B0-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
			assertEquals( "340/0/B0-64/0",   messages.get(i++).toString() ); // RPN LSB: 0
			assertEquals( "350/0/B0-06/0",   messages.get(i++).toString() ); // data MSB: 0
			assertEquals( "360/0/B0-26/0",   messages.get(i++).toString() ); // data LSB: 0
			assertEquals( "370/0/B0-65/127", messages.get(i++).toString() ); // MSB reset
			assertEquals( "370/0/B0-64/127", messages.get(i++).toString() ); // LSB reset
			
			// tick 420: .wait.set(127/5)
			assertEquals( "390/0/B0-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
			assertEquals( "400/0/B0-64/0",   messages.get(i++).toString() ); // RPN LSB: 0
			assertEquals( "410/0/B0-06/127", messages.get(i++).toString() ); // data MSB: 127
			assertEquals( "420/0/B0-26/5",   messages.get(i++).toString() ); // data LSB: 5
			assertEquals( "430/0/B0-65/127", messages.get(i++).toString() ); // MSB reset
			assertEquals( "430/0/B0-64/127", messages.get(i++).toString() ); // LSB reset
			
			// tick 480: .wait.set(124.998)
			assertEquals( "450/0/B0-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
			assertEquals( "460/0/B0-64/0",   messages.get(i++).toString() ); // RPN LSB: 0
			assertEquals( "470/0/B0-06/125", messages.get(i++).toString() ); // data MSB: 125
			assertEquals( "480/0/B0-26/0",   messages.get(i++).toString() ); // data LSB: 0
			assertEquals( "490/0/B0-65/127", messages.get(i++).toString() ); // MSB reset
			assertEquals( "490/0/B0-64/127", messages.get(i++).toString() ); // LSB reset
			
			// tick 540: .wait.set(4.997)
			assertEquals( "510/0/B0-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
			assertEquals( "520/0/B0-64/0",   messages.get(i++).toString() ); // RPN LSB: 0
			assertEquals( "530/0/B0-06/5",   messages.get(i++).toString() ); // data MSB: 5
			assertEquals( "540/0/B0-26/0",   messages.get(i++).toString() ); // data LSB: 0
			assertEquals( "550/0/B0-65/127", messages.get(i++).toString() ); // MSB reset
			assertEquals( "550/0/B0-64/127", messages.get(i++).toString() ); // LSB reset
			
			// tick 600: .wait.set(127.990)
			assertEquals( "570/0/B0-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
			assertEquals( "580/0/B0-64/0",   messages.get(i++).toString() ); // RPN LSB: 0
			assertEquals( "590/0/B0-06/127", messages.get(i++).toString() ); // data MSB: 127
			assertEquals( "600/0/B0-26/99",  messages.get(i++).toString() ); // data LSB: 99
			assertEquals( "610/0/B0-65/127", messages.get(i++).toString() ); // MSB reset
			assertEquals( "610/0/B0-64/127", messages.get(i++).toString() ); // LSB reset
			
			// tick 660: .wait.set(0.000001)
			assertEquals( "630/0/B0-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
			assertEquals( "640/0/B0-64/0",   messages.get(i++).toString() ); // RPN LSB: 0
			assertEquals( "650/0/B0-06/0",   messages.get(i++).toString() ); // data MSB: 0
			assertEquals( "660/0/B0-26/0",   messages.get(i++).toString() ); // data LSB: 0
			assertEquals( "670/0/B0-65/127", messages.get(i++).toString() ); // MSB reset
			assertEquals( "670/0/B0-64/127", messages.get(i++).toString() ); // LSB reset
			
			// tick 720: .wait.set(127)
			assertEquals( "690/0/B0-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
			assertEquals( "700/0/B0-64/0",   messages.get(i++).toString() ); // RPN LSB: 0
			assertEquals( "710/0/B0-06/127", messages.get(i++).toString() ); // data MSB: 127
			assertEquals( "720/0/B0-26/0",   messages.get(i++).toString() ); // data LSB: 0
			assertEquals( "730/0/B0-65/127", messages.get(i++).toString() ); // MSB reset
			assertEquals( "730/0/B0-64/127", messages.get(i++).toString() ); // LSB reset
			
			// tick 960: rpn=0/0.set(96.7)
			assertEquals( "930/0/B0-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
			assertEquals( "940/0/B0-64/0",   messages.get(i++).toString() ); // RPN LSB: 0
			assertEquals( "950/0/B0-06/97",  messages.get(i++).toString() ); // data MSB: 97
			assertEquals( "970/0/B0-65/127", messages.get(i++).toString() ); // MSB reset
			assertEquals( "970/0/B0-64/127", messages.get(i++).toString() ); // LSB reset
			
			// tick 1440: rpn=0/0.double.set(96.7)
			assertEquals( "1410/0/B0-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
			assertEquals( "1420/0/B0-64/0",   messages.get(i++).toString() ); // RPN LSB: 0
			assertEquals( "1430/0/B0-06/96",  messages.get(i++).toString() ); // data MSB: 96
			assertEquals( "1440/0/B0-26/70",  messages.get(i++).toString() ); // data LSB: 70
			assertEquals( "1450/0/B0-65/127", messages.get(i++).toString() ); // MSB reset
			assertEquals( "1450/0/B0-64/127", messages.get(i++).toString() ); // LSB reset
			
			// tick 1920: rpn=0.set(2.0)
			assertEquals( "1890/0/B0-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
			assertEquals( "1900/0/B0-64/0",   messages.get(i++).toString() ); // RPN LSB: 0
			assertEquals( "1910/0/B0-06/2",   messages.get(i++).toString() ); // data MSB: 2
			assertEquals( "1930/0/B0-65/127", messages.get(i++).toString() ); // MSB reset
			assertEquals( "1930/0/B0-64/127", messages.get(i++).toString() ); // LSB reset
			
			// tick 2400: rpn=0.double.set(2.0)
			assertEquals( "2370/0/B0-65/0",   messages.get(i++).toString() ); // RPN MSB: 0
			assertEquals( "2380/0/B0-64/0",   messages.get(i++).toString() ); // RPN LSB: 0
			assertEquals( "2390/0/B0-06/2",   messages.get(i++).toString() ); // data MSB: 2
			assertEquals( "2400/0/B0-26/0",   messages.get(i++).toString() ); // data LSB: 0
			assertEquals( "2410/0/B0-65/127", messages.get(i++).toString() ); // MSB reset
			assertEquals( "2410/0/B0-64/127", messages.get(i++).toString() ); // LSB reset
			
			// no further messages
			assertEquals(messages.size(), i);
		}
		// channel 1
		{
			messages = getMessagesByStatus("E1");
			int i = 0;
			
			// percentage, range: 2.0
			{
				// single byte
				assertEquals("00/00/-2.00000", getPitchBendStr(messages.get(i++), 5)); // pitch.set(-100%)
				assertEquals("20/20/-1.0",     getPitchBendStr(messages.get(i++), 1)); // pitch.set(-50%)
				assertEquals("40/00/0.00000",  getPitchBendStr(messages.get(i++), 5)); // pitch.set(+0%)
				assertEquals("60/60/1.0",      getPitchBendStr(messages.get(i++), 1)); // pitch.set(+50%)
				assertEquals("7F/7F/2.00000",  getPitchBendStr(messages.get(i++), 5)); // pitch.set(+100%)
				
				// double
				assertEquals("00/00/-2.00000", getPitchBendStr(messages.get(i++), 5)); // pitch.double.set(-100%)
				assertEquals("20/00/-1.00000", getPitchBendStr(messages.get(i++), 5)); // pitch.double.set(-50%)
				assertEquals("40/00/0.00000",  getPitchBendStr(messages.get(i++), 5)); // pitch.double.set(-0%)
				assertEquals("60/00/1.000",    getPitchBendStr(messages.get(i++), 3)); // pitch.double.set(+50%)
				assertEquals("7F/7F/2.00000",  getPitchBendStr(messages.get(i++), 5)); // pitch.double.set(+100%)
			}
			
			// MSB/LSB, range: 2.0
			{
				assertEquals("00/00/-2.00000", getPitchBendStr(messages.get(i++), 5)); // pitch.set(0/0)
				assertEquals("40/00/0.00000",  getPitchBendStr(messages.get(i++), 5)); // pitch.set(64/0)
				assertEquals("7F/7F/2.00000",  getPitchBendStr(messages.get(i++), 5)); // pitch.set(127/127)
			}
			
			// half-tone, range: 2.7
			{
				// single
				assertEquals("15/15/-2.0",    getPitchBendStr(messages.get(i++), 1)); // pitch.set(-2.0)
				assertEquals("2B/2B/-1.0",    getPitchBendStr(messages.get(i++), 1)); // pitch.set(-1.0)
				assertEquals("40/00/0.00000", getPitchBendStr(messages.get(i++), 5)); // pitch.set(+0)
				assertEquals("55/55/1.0",     getPitchBendStr(messages.get(i++), 1)); // pitch.set(+1.0)
				assertEquals("6A/6A/2.0",     getPitchBendStr(messages.get(i++), 1)); // pitch.set(+2.0)
				
				// double
				assertEquals("15/2B/-2.000",  getPitchBendStr(messages.get(i++), 3)); // pitch.double.set(-2.0)
				assertEquals("2A/55/-1.000",  getPitchBendStr(messages.get(i++), 3)); // pitch.double.set(-1)
				assertEquals("40/00/0.00000", getPitchBendStr(messages.get(i++), 5)); // pitch.double.set(+0)
				assertEquals("55/2A/1.000",   getPitchBendStr(messages.get(i++), 3)); // pitch.double.set(+1.0)
				assertEquals("6A/55/2.000",   getPitchBendStr(messages.get(i++), 3)); // pitch.double.set(+2)
			}
			
			// half-tone, range: 4.0
			{
				// single
				assertEquals("00/00/-4.00000", getPitchBendStr(messages.get(i++), 5)); // pitch.set(-4.0)
				assertEquals("20/20/-2.0",     getPitchBendStr(messages.get(i++), 1)); // pitch.set(-2)
				assertEquals("30/30/-1.0",     getPitchBendStr(messages.get(i++), 1)); // pitch.set(-1.0)
				assertEquals("40/00/0.00000",  getPitchBendStr(messages.get(i++), 5)); // pitch.set(+0)
				assertEquals("50/50/1.0",      getPitchBendStr(messages.get(i++), 1)); // pitch.set(+1.0)
				assertEquals("60/60/2.0",      getPitchBendStr(messages.get(i++), 1)); // pitch.set(+2.0)
				assertEquals("7F/7F/4.00000",  getPitchBendStr(messages.get(i++), 5)); // pitch.set(+4)
				
				// double
				assertEquals("00/00/-4.00000", getPitchBendStr(messages.get(i++), 5)); // pitch.double.set(-4.0)
				assertEquals("20/00/-2.00000", getPitchBendStr(messages.get(i++), 5)); // pitch.double.set(-2.0)
				assertEquals("30/00/-1.00000", getPitchBendStr(messages.get(i++), 5)); // pitch.double.set(-1.0)
				assertEquals("40/00/0.00000",  getPitchBendStr(messages.get(i++), 5)); // pitch.double.set(+0)
				assertEquals("50/00/1.000",    getPitchBendStr(messages.get(i++), 3)); // pitch.double.set(+1.0)
				assertEquals("60/00/2.000",    getPitchBendStr(messages.get(i++), 3)); // pitch.double.set(+2.0)
				assertEquals("7F/7F/4.00000",  getPitchBendStr(messages.get(i++), 5)); // pitch.double.set(+4.0)
			}
			// half-tone, range: 8.0
			{
				// single
				assertEquals("00/00/-8.00000", getPitchBendStr(messages.get(i++), 5)); // pitch.set(-8.0)
				assertEquals("30/30/-2.0",     getPitchBendStr(messages.get(i++), 1)); // pitch.set(-2.0)
				assertEquals("38/38/-1"  ,     getPitchBendStr(messages.get(i++), 0)); // pitch.set(-1.0)
				assertEquals("40/00/0.00000",  getPitchBendStr(messages.get(i++), 5)); // pitch.set(+0)
				assertEquals("48/48/1",        getPitchBendStr(messages.get(i++), 0)); // pitch.set(+1.0)
				assertEquals("50/50/2",        getPitchBendStr(messages.get(i++), 0)); // pitch.set(+2.0)
				assertEquals("7F/7F/8.00000",  getPitchBendStr(messages.get(i++), 5)); // pitch.set(+8.0)
				
				// double
				assertEquals("00/00/-8.00000", getPitchBendStr(messages.get(i++), 5)); // pitch.double.set(-8.0)
				assertEquals("30/00/-2.00000", getPitchBendStr(messages.get(i++), 5)); // pitch.double.set(-2.0)
				assertEquals("38/00/-1.00000", getPitchBendStr(messages.get(i++), 5)); // pitch.double.set(-1.0)
				assertEquals("40/00/0.00000",  getPitchBendStr(messages.get(i++), 5)); // pitch.double.set(+0)
				assertEquals("48/00/1.000",    getPitchBendStr(messages.get(i++), 3)); // pitch.double.set(+1.0)
				assertEquals("50/00/2.000",    getPitchBendStr(messages.get(i++), 3)); // pitch.double.set(+2.0)
				assertEquals("7F/7F/8.00000",  getPitchBendStr(messages.get(i++), 5)); // pitch.double.set(+8.0)
			}
			// half-tone, range: 12.0
			{
				// single
				assertEquals("00/00/-12.00000", getPitchBendStr(messages.get(i++), 5)); // pitch.set(-12.0)
				assertEquals("40/00/0.00000",   getPitchBendStr(messages.get(i++), 5)); // pitch.set(+0)
				assertEquals("7F/7F/12.00000",  getPitchBendStr(messages.get(i++), 5)); // pitch.set(+12.0)
				
				// double
				assertEquals("00/00/-12.00000", getPitchBendStr(messages.get(i++), 5)); // pitch.double.set(-12.0)
				assertEquals("40/00/0.00000",   getPitchBendStr(messages.get(i++), 5)); // pitch.double.set(+0)
				assertEquals("7F/7F/12.00000",  getPitchBendStr(messages.get(i++), 5)); // pitch.double.set(+12.0)
			}
			// half-tone, range: 24.0
			{
				// single
				assertEquals("00/00/-24.00000", getPitchBendStr(messages.get(i++), 5)); // pitch.set(-24.0)
				assertEquals("20/20/-12",       getPitchBendStr(messages.get(i++), 0)); // pitch.set(-12.0)
				assertEquals("40/00/0.00000",   getPitchBendStr(messages.get(i++), 5)); // pitch.set(+0)
				assertEquals("60/60/12",        getPitchBendStr(messages.get(i++), 0)); // pitch.set(+12.0)
				assertEquals("7F/7F/24.00000",  getPitchBendStr(messages.get(i++), 5)); // pitch.set(+24.0)
				
				// double
				assertEquals("00/00/-24.00000", getPitchBendStr(messages.get(i++), 5)); // pitch.double.set(-24.0)
				assertEquals("20/00/-12.00000", getPitchBendStr(messages.get(i++), 5)); // pitch.double.set(-12.0)
				assertEquals("40/00/0.00000",   getPitchBendStr(messages.get(i++), 5)); // pitch.double.set(+0)
				assertEquals("60/00/12.00",     getPitchBendStr(messages.get(i++), 2)); // pitch.double.set(+12.0)
				assertEquals("7F/7F/24.00000",  getPitchBendStr(messages.get(i++), 5)); // pitch.double.set(+24.0)
			}
			// half-tone, range: 36.0
			{
				// single
				assertEquals("00/00/-36.00000", getPitchBendStr(messages.get(i++), 5)); // pitch.set(-36.0)
				assertEquals("15/15/-24",       getPitchBendStr(messages.get(i++), 0)); // pitch.set(-24.0)
				assertEquals("2B/2B/-12",       getPitchBendStr(messages.get(i++), 0)); // pitch.set(-12.0)
				assertEquals("40/00/0.00000",   getPitchBendStr(messages.get(i++), 5)); // pitch.set(+0)
				assertEquals("55/55/12",        getPitchBendStr(messages.get(i++), 0)); // pitch.set(+12.0)
				assertEquals("6A/6A/24",        getPitchBendStr(messages.get(i++), 0)); // pitch.set(+24.0)
				assertEquals("7F/7F/36.00000",  getPitchBendStr(messages.get(i++), 5)); // pitch.set(+36.0)
				
				// double
				assertEquals("00/00/-36.00000", getPitchBendStr(messages.get(i++), 5)); // pitch.double.set(-36.0)
				assertEquals("15/2B/-24.00",    getPitchBendStr(messages.get(i++), 2)); // pitch.double.set(-24.0)
				assertEquals("2A/55/-12.00",    getPitchBendStr(messages.get(i++), 2)); // pitch.double.set(-12.0)
				assertEquals("40/00/0.00000",   getPitchBendStr(messages.get(i++), 5)); // pitch.double.set(+0)
				assertEquals("55/2A/12.00",     getPitchBendStr(messages.get(i++), 2)); // pitch.double.set(+12.0)
				assertEquals("6A/55/24.00",     getPitchBendStr(messages.get(i++), 2)); // pitch.double.set(+24.0)
				assertEquals("7F/7F/36.00000",  getPitchBendStr(messages.get(i++), 5)); // pitch.double.set(+36.0)
			}
			// half-tone, range: 48.0
			{
				// single
				assertEquals("00/00/-48.00000", getPitchBendStr(messages.get(i++), 5)); // pitch.set(-48.0)
				assertEquals("10/10/-36",       getPitchBendStr(messages.get(i++), 0)); // pitch.set(-36.0)
				assertEquals("20/20/-24",       getPitchBendStr(messages.get(i++), 0)); // pitch.set(-24.0)
				assertEquals("30/30/-12",       getPitchBendStr(messages.get(i++), 0)); // pitch.set(-12.0)
				assertEquals("40/00/0.00000",   getPitchBendStr(messages.get(i++), 5)); // pitch.set(+0)
				assertEquals("50/50/12",        getPitchBendStr(messages.get(i++), 0)); // pitch.set(+12.0)
				assertEquals("60/60/25",        getPitchBendStr(messages.get(i++), 0)); // pitch.set(+24.0) - rounding error too big
				assertEquals("6F/6F/36",        getPitchBendStr(messages.get(i++), 0)); // pitch.set(+36.0)
				assertEquals("7F/7F/48.00000",  getPitchBendStr(messages.get(i++), 5)); // pitch.set(+48.0)
				
				// double
				assertEquals("00/00/-48.00000", getPitchBendStr(messages.get(i++), 5)); // pitch.double.set(-48.0)
				assertEquals("10/00/-36.00000", getPitchBendStr(messages.get(i++), 5)); // pitch.double.set(-36.0)
				assertEquals("20/00/-24.00000", getPitchBendStr(messages.get(i++), 5)); // pitch.double.set(-24.0)
				assertEquals("30/00/-12.00000", getPitchBendStr(messages.get(i++), 5)); // pitch.double.set(-12.0)
				assertEquals("40/00/0.00000",   getPitchBendStr(messages.get(i++), 5)); // pitch.double.set(+0)
				assertEquals("50/00/12.00",     getPitchBendStr(messages.get(i++), 2)); // pitch.double.set(+12.0)
				assertEquals("60/00/24.00",     getPitchBendStr(messages.get(i++), 2)); // pitch.double.set(+24.0)
				assertEquals("6F/7F/36.00",     getPitchBendStr(messages.get(i++), 2)); // pitch.double.set(+36.0)
				assertEquals("7F/7F/48.00000",  getPitchBendStr(messages.get(i++), 5)); // pitch.double.set(+48.0)
			}
			// half-tone, range: 60.0
			{
				// single
				assertEquals("00/00/-60.00000", getPitchBendStr(messages.get(i++), 5)); // pitch.set(-60.0)
				assertEquals("0D/0D/-48",       getPitchBendStr(messages.get(i++), 0)); // pitch.set(-48.0)
				assertEquals("1A/1A/-35",       getPitchBendStr(messages.get(i++), 0)); // pitch.set(-36.0) - rounding error too big
				assertEquals("26/26/-24",       getPitchBendStr(messages.get(i++), 0)); // pitch.set(-24.0)
				assertEquals("33/33/-12",       getPitchBendStr(messages.get(i++), 0)); // pitch.set(-12.0)
				assertEquals("40/00/0.00000",   getPitchBendStr(messages.get(i++), 5)); // pitch.set(+0)
				assertEquals("4D/4D/13",        getPitchBendStr(messages.get(i++), 0)); // pitch.set(+12.0) - rounding error too big
				assertEquals("59/59/24",        getPitchBendStr(messages.get(i++), 0)); // pitch.set(+24.0)
				assertEquals("66/66/36",        getPitchBendStr(messages.get(i++), 0)); // pitch.set(+36.0)
				assertEquals("72/72/48",        getPitchBendStr(messages.get(i++), 0)); // pitch.set(+48.0)
				assertEquals("7F/7F/60.00000",  getPitchBendStr(messages.get(i++), 5)); // pitch.set(+60.0)
				
				// double
				assertEquals("00/00/-60.00000", getPitchBendStr(messages.get(i++), 5)); // pitch.double.set(-60.0)
				assertEquals("0C/66/-48.00",    getPitchBendStr(messages.get(i++), 2)); // pitch.double.set(-48.0)
				assertEquals("19/4D/-36.00",    getPitchBendStr(messages.get(i++), 2)); // pitch.double.set(-36.0)
				assertEquals("26/33/-24.00",    getPitchBendStr(messages.get(i++), 2)); // pitch.double.set(-24.0)
				assertEquals("33/1A/-12.00",    getPitchBendStr(messages.get(i++), 2)); // pitch.double.set(-12.0)
				assertEquals("40/00/0.00000",   getPitchBendStr(messages.get(i++), 5)); // pitch.double.set(+0)
				assertEquals("4C/66/12.00",     getPitchBendStr(messages.get(i++), 2)); // pitch.double.set(+12.0)
				assertEquals("59/4C/24.00",     getPitchBendStr(messages.get(i++), 2)); // pitch.double.set(+24.0)
				assertEquals("66/33/36.00",     getPitchBendStr(messages.get(i++), 2)); // pitch.double.set(+36.0)
				assertEquals("73/19/48.00",     getPitchBendStr(messages.get(i++), 2)); // pitch.double.set(+48.0)
				assertEquals("7F/7F/60.00000",  getPitchBendStr(messages.get(i++), 5)); // pitch.double.set(+60.0)
			}
			// half-tone, range: 127.99
			{
				// single
				assertEquals("22/22/-59",     getPitchBendStr(messages.get(i++), 0)); // pitch.set(-60.0) - rounding error too big
				assertEquals("28/28/-47",     getPitchBendStr(messages.get(i++), 0)); // pitch.set(-48.0) - rounding error too big
				assertEquals("2E/2E/-35",     getPitchBendStr(messages.get(i++), 0)); // pitch.set(-36.0) - rounding error too big
				assertEquals("34/34/-23",     getPitchBendStr(messages.get(i++), 0)); // pitch.set(-24.0) - rounding error too big
				assertEquals("3A/3A/-11",     getPitchBendStr(messages.get(i++), 0)); // pitch.set(-12.0) - rounding error too big
				assertEquals("40/00/0.00000", getPitchBendStr(messages.get(i++), 5)); // pitch.set(+0)
				assertEquals("46/46/13",      getPitchBendStr(messages.get(i++), 0)); // pitch.set(+12.0)
				assertEquals("4C/4C/25",      getPitchBendStr(messages.get(i++), 0)); // pitch.set(+24.0) - rounding error too big
				assertEquals("52/52/37",      getPitchBendStr(messages.get(i++), 0)); // pitch.set(+36.0) - rounding error too big
				assertEquals("58/58/49",      getPitchBendStr(messages.get(i++), 0)); // pitch.set(+48.0) - rounding error too big
				assertEquals("5E/5E/61",      getPitchBendStr(messages.get(i++), 0)); // pitch.set(+60.0) - rounding error too big
				
				// double
				assertEquals("22/00/-60",     getPitchBendStr(messages.get(i++), 0)); // pitch.double.set(-60.0)
				assertEquals("28/00/-48",     getPitchBendStr(messages.get(i++), 0)); // pitch.double.set(-48.0)
				assertEquals("2E/00/-36",     getPitchBendStr(messages.get(i++), 0)); // pitch.double.set(-36.0)
				assertEquals("34/00/-24.0",   getPitchBendStr(messages.get(i++), 1)); // pitch.double.set(-24.0)
				assertEquals("3A/00/-12.0",   getPitchBendStr(messages.get(i++), 1)); // pitch.double.set(-12.0)
				assertEquals("40/00/0.00000", getPitchBendStr(messages.get(i++), 5)); // pitch.double.set(+0)
				assertEquals("46/00/12.0",    getPitchBendStr(messages.get(i++), 1)); // pitch.double.set(+12.0)
				assertEquals("4C/00/24.0",    getPitchBendStr(messages.get(i++), 1)); // pitch.double.set(+24.0)
				assertEquals("52/00/36",      getPitchBendStr(messages.get(i++), 0)); // pitch.double.set(+36.0)
				assertEquals("58/00/48",      getPitchBendStr(messages.get(i++), 0)); // pitch.double.set(+48.0)
				assertEquals("5E/00/60",      getPitchBendStr(messages.get(i++), 0)); // pitch.double.set(+60.0)
			}
			
			// no further messages
			assertEquals(messages.size(), i);
		}
		
		parse(getWorkingFile("effects-3-at-port"));
		// channel 0: mono_at
		{
			messages = getMessagesByStatus("D0");
			int i = 0;
			
			assertEquals("D0/00", getShortMsgBytesAsStr(messages.get(i++))); // mono_at.wait(4).set(0)
			assertEquals("D0/40", getShortMsgBytesAsStr(messages.get(i++))); // mono_at.wait(4).set(64)
			assertEquals("D0/7F", getShortMsgBytesAsStr(messages.get(i++))); // mono_at.wait(4).set(127)
			assertEquals("D0/00", getShortMsgBytesAsStr(messages.get(i++))); // mono_at.wait(4).set(0%)
			assertEquals("D0/40", getShortMsgBytesAsStr(messages.get(i++))); // mono_at.wait(4).set(50%)
			assertEquals("D0/7F", getShortMsgBytesAsStr(messages.get(i++))); // mono_at.wait(4).set(100%)
			
			// .line(0%,100%)
			for (int j = 0; j < 128; j++) {
				String value = String.format("%02X", j);
				assertEquals("D0/" + value, getShortMsgBytesAsStr(messages.get(i++)));
			}
			
			// line(100%,0%)
			for (int j = 127; j > -1; j--) {
				String value = String.format("%02X", j);
				assertEquals("D0/" + value, getShortMsgBytesAsStr(messages.get(i++)));
			}
			
			// no further messages
			assertEquals(messages.size(), i);
		}
		// channel 1: poly_at
		{
			messages = getMessagesByStatus("A1");
			int i = 0;
			
			// note c#-4 ==> 0D
			assertEquals("A1/0D/00", getShortMsgBytesAsStr(messages.get(i++))); // poly_at.note(c#-4).wait.set(0)
			assertEquals("A1/0D/40", getShortMsgBytesAsStr(messages.get(i++))); // .wait(4).set(64)
			assertEquals("A1/0D/7F", getShortMsgBytesAsStr(messages.get(i++))); // .wait(4).set(127)
			
			// note c ==> 3C
			assertEquals("A1/3C/00", getShortMsgBytesAsStr(messages.get(i++))); // poly_at.note(c).wait(4).set(0)
			assertEquals("A1/3C/40", getShortMsgBytesAsStr(messages.get(i++))); // poly_at.note(c).wait(4).set(64)
			assertEquals("A1/3C/7F", getShortMsgBytesAsStr(messages.get(i++))); // poly_at.note(c).wait(4).set(127)
			
			// note d ==> 3E
			assertEquals("A1/3E/00", getShortMsgBytesAsStr(messages.get(i++))); // poly_at.note(d).wait(4).set(0%)
			assertEquals("A1/3E/40", getShortMsgBytesAsStr(messages.get(i++))); // poly_at.note(d).wait(4).set(50%)
			assertEquals("A1/3E/7F", getShortMsgBytesAsStr(messages.get(i++))); // poly_at.note(d).wait(4).set(100%)
			
			// poly_at.note(c).length(1).line(0%,100%)
			for (int j = 0; j < 128; j++) {
				String value = String.format("%02X", j);
				assertEquals("A1/3C/" + value, getShortMsgBytesAsStr(messages.get(i++)));
			}
			
			// poly_at.note(c).length(1).line(100%,0%)
			for (int j = 127; j > -1; j--) {
				String value = String.format("%02X", j);
				assertEquals("A1/3C/" + value, getShortMsgBytesAsStr(messages.get(i++)));
			}
			
			// no further messages
			assertEquals(messages.size(), i);
		}
		// channel 2: poly_at with patterns (compact)
		{
			messages = getMessagesByStatus("A2");
			int i = 0;
			
			// c/d/e:pat1
			{
				// .note(1) == .note(d);   d == 62 == 0x3E
				// .note(1).line(120,121)  120 == 0x78
				assertEquals("A2/3E/78", getShortMsgBytesAsStr(messages.get(i++)));
				assertEquals("A2/3E/79", getShortMsgBytesAsStr(messages.get(i++)));
				
				// : 0/1/2:pat2
				assertEquals("A2/3C/0A", getShortMsgBytesAsStr(messages.get(i++))); // .note(0)
				assertEquals("A2/3C/0B", getShortMsgBytesAsStr(messages.get(i++)));
				assertEquals("A2/3E/0C", getShortMsgBytesAsStr(messages.get(i++))); // .note(1)
				assertEquals("A2/3E/0D", getShortMsgBytesAsStr(messages.get(i++)));
				assertEquals("A2/40/0E", getShortMsgBytesAsStr(messages.get(i++))); // .note(2)
				assertEquals("A2/40/0F", getShortMsgBytesAsStr(messages.get(i++)));
				
				// .note(2) == .note(e);   d == 64 == 0x40
				// .note(2).line(122,123)
				assertEquals("A2/40/7A", getShortMsgBytesAsStr(messages.get(i++)));
				assertEquals("A2/40/7B", getShortMsgBytesAsStr(messages.get(i++)));
				
				// : 0/1/2:pat2
				assertEquals("A2/3C/0A", getShortMsgBytesAsStr(messages.get(i++))); // .note(0)
				assertEquals("A2/3C/0B", getShortMsgBytesAsStr(messages.get(i++)));
				assertEquals("A2/3E/0C", getShortMsgBytesAsStr(messages.get(i++))); // .note(1)
				assertEquals("A2/3E/0D", getShortMsgBytesAsStr(messages.get(i++)));
				assertEquals("A2/40/0E", getShortMsgBytesAsStr(messages.get(i++))); // .note(2)
				assertEquals("A2/40/0F", getShortMsgBytesAsStr(messages.get(i++)));
				
				// .note(0) == .note(c);   c == 60 == 0x3C
				// .note(0)
				// .line(124,125)
				assertEquals("A2/3C/7C", getShortMsgBytesAsStr(messages.get(i++)));
				assertEquals("A2/3C/7D", getShortMsgBytesAsStr(messages.get(i++)));
				
				// : 2/1/0:pat2
				assertEquals("A2/40/0A", getShortMsgBytesAsStr(messages.get(i++))); // .note(0)
				assertEquals("A2/40/0B", getShortMsgBytesAsStr(messages.get(i++)));
				assertEquals("A2/3E/0C", getShortMsgBytesAsStr(messages.get(i++))); // .note(1)
				assertEquals("A2/3E/0D", getShortMsgBytesAsStr(messages.get(i++)));
				assertEquals("A2/3C/0E", getShortMsgBytesAsStr(messages.get(i++))); // .note(2)
				assertEquals("A2/3C/0F", getShortMsgBytesAsStr(messages.get(i++)));
			}
			
			// no further messages
			assertEquals(messages.size(), i);
		}
		// channel 3: poly_at with patterns (lowlevel)
		{
			messages = getMessagesByStatus("A3");
			int i = 0;
			
			// 2 c/d/e pat_ll_1
			{
				// .note(1) == .note(d);   d == 62 == 0x3E
				// .note(1).line(120,121)  120 == 0x78
				assertEquals("A3/3E/78", getShortMsgBytesAsStr(messages.get(i++)));
				assertEquals("A3/3E/79", getShortMsgBytesAsStr(messages.get(i++)));
				
				// 0/1/2  pat_ll_2
				assertEquals("A3/3C/0A", getShortMsgBytesAsStr(messages.get(i++))); // .note(0)
				assertEquals("A3/3C/0B", getShortMsgBytesAsStr(messages.get(i++)));
				assertEquals("A3/3E/0C", getShortMsgBytesAsStr(messages.get(i++))); // .note(1)
				assertEquals("A3/3E/0D", getShortMsgBytesAsStr(messages.get(i++)));
				assertEquals("A3/40/0E", getShortMsgBytesAsStr(messages.get(i++))); // .note(2)
				assertEquals("A3/40/0F", getShortMsgBytesAsStr(messages.get(i++)));
				
				// .note(2) == .note(e);   d == 64 == 0x40
				// .note(2).line(122,123)
				assertEquals("A3/40/7A", getShortMsgBytesAsStr(messages.get(i++)));
				assertEquals("A3/40/7B", getShortMsgBytesAsStr(messages.get(i++)));
				
				// 0/1/2  pat_ll_2
				assertEquals("A3/3C/0A", getShortMsgBytesAsStr(messages.get(i++))); // .note(0)
				assertEquals("A3/3C/0B", getShortMsgBytesAsStr(messages.get(i++)));
				assertEquals("A3/3E/0C", getShortMsgBytesAsStr(messages.get(i++))); // .note(1)
				assertEquals("A3/3E/0D", getShortMsgBytesAsStr(messages.get(i++)));
				assertEquals("A3/40/0E", getShortMsgBytesAsStr(messages.get(i++))); // .note(2)
				assertEquals("A3/40/0F", getShortMsgBytesAsStr(messages.get(i++)));
				
				// .note(0) == .note(c);   c == 60 == 0x3C
				// .note(0)
				// .line(124,125)
				assertEquals("A3/3C/7C", getShortMsgBytesAsStr(messages.get(i++)));
				assertEquals("A3/3C/7D", getShortMsgBytesAsStr(messages.get(i++)));
				
				// 2/1/0  pat_ll_2
				assertEquals("A3/40/0A", getShortMsgBytesAsStr(messages.get(i++))); // .note(0)
				assertEquals("A3/40/0B", getShortMsgBytesAsStr(messages.get(i++)));
				assertEquals("A3/3E/0C", getShortMsgBytesAsStr(messages.get(i++))); // .note(1)
				assertEquals("A3/3E/0D", getShortMsgBytesAsStr(messages.get(i++)));
				assertEquals("A3/3C/0E", getShortMsgBytesAsStr(messages.get(i++))); // .note(2)
				assertEquals("A3/3C/0F", getShortMsgBytesAsStr(messages.get(i++)));
			}
			
			// no further messages
			assertEquals(messages.size(), i);
		}
		// channel 4: port_ctrl
		{
			messages = getMessagesByStatus("B4");
			int i = 0;
			
			// mono_mode.on()
			assertEquals(0x7E, Integer.valueOf((byte) messages.get(i++).getOption(SingleMessage.OPT_CONTROLLER)));
			
			// port_time.set(50%) - only MSB
			assertEquals( 0x05, Integer.valueOf((byte) messages.get(i++).getOption(SingleMessage.OPT_CONTROLLER)));
			
			// note c#-4 ==> 0D
			assertEquals("4/c#-2", getPortCtrlStr(messages.get(i++))); // port_ctrl.note(c#-2).wait.on()
			assertEquals("4/c#+2", getPortCtrlStr(messages.get(i++))); // .wait(4).note(c#+2).on()
			assertEquals("4/c#+2", getPortCtrlStr(messages.get(i++))); // .wait(4).on()
			
			// port_time.set(40%)
			assertEquals( 0x05, Integer.valueOf((byte) messages.get(i++).getOption(SingleMessage.OPT_CONTROLLER)));
			
			// lower octave
			assertEquals("4/c-",  getPortCtrlStr(messages.get(i++))); // port_ctrl.note(c-).on()
			assertEquals("4/d-2", getPortCtrlStr(messages.get(i++))); // port_ctrl.note(d-2)on()
			assertEquals("4/e-",  getPortCtrlStr(messages.get(i++))); // port_ctrl.note(e-)on()
			
			// higher octave
			assertEquals("4/c+",  getPortCtrlStr(messages.get(i++))); // port_ctrl.note(c+)on()
			assertEquals("4/d+2", getPortCtrlStr(messages.get(i++))); // port_ctrl.note(d+2)on()
			assertEquals("4/e+",  getPortCtrlStr(messages.get(i++))); // port_ctrl.note(e+)on()
			
			// port_time.set(10%)
			assertEquals( 0x05, Integer.valueOf((byte) messages.get(i++).getOption(SingleMessage.OPT_CONTROLLER)));
			
			assertEquals("4/c-3", getPortCtrlStr(messages.get(i++))); // port_ctrl.note(c-3).on()
			assertEquals("4/c+3", getPortCtrlStr(messages.get(i++))); // port_ctrl.note(c+3).on()
			
			// no further messages
			assertEquals(messages.size(), i);
		}
		// channel 5: port_ctrl with patterns
		{
			messages = getMessagesByStatus("B5");
			int i = 0;
			
			// mono_mode.on()
			assertEquals(0x7E, Integer.valueOf((byte) messages.get(i++).getOption(SingleMessage.OPT_CONTROLLER)));
			
			// port_time.set(50%) - only MSB
			assertEquals( 0x05, Integer.valueOf((byte) messages.get(i++).getOption(SingleMessage.OPT_CONTROLLER)));
			
			// c/d/e:pat_p1
			{
				assertEquals("5/d", getPortCtrlStr(messages.get(i++))); // .note(1).on()
				
				// 0/1/2:pat_p2
				{
					assertEquals("5/c", getPortCtrlStr(messages.get(i++))); // .note(0).on()
					assertEquals("5/d", getPortCtrlStr(messages.get(i++))); // .note(1).on()
					assertEquals("5/e", getPortCtrlStr(messages.get(i++))); // .note(2).on()
				}
				assertEquals("5/e", getPortCtrlStr(messages.get(i++))); // .note(2).on()
				
				// 0/1/2:pat_p2
				{
					assertEquals("5/c", getPortCtrlStr(messages.get(i++))); // .note(0).on()
					assertEquals("5/d", getPortCtrlStr(messages.get(i++))); // .note(1).on()
					assertEquals("5/e", getPortCtrlStr(messages.get(i++))); // .note(2).on()
				}
				
				// .note(0)
				// .on()
				assertEquals("5/c", getPortCtrlStr(messages.get(i++))); // .note(2).on()
				
				// 2/1/0:pat_p2
				{
					assertEquals("5/e", getPortCtrlStr(messages.get(i++))); // .note(0).on()
					assertEquals("5/d", getPortCtrlStr(messages.get(i++))); // .note(1).on()
					assertEquals("5/c", getPortCtrlStr(messages.get(i++))); // .note(2).on()
				}
			}
			
			// no further messages
			assertEquals(messages.size(), i);
		}
		
		parse(getWorkingFile("effects-4-ctrl-dest"));
		{
			messages = getMessagesByStatus("F0");
			int i = 0;
			
			// channel 0
			
			// pitch: 0x00
			assertEquals("0/mono_at==>00:4C", getCtrlDestStr(messages.get(i++))); // mono_at ==> pitch: +12
			assertEquals("0/mono_at==>00:34", getCtrlDestStr(messages.get(i++))); // mono_at ==> pitch: -12.0
			assertEquals("0/poly_at==>00:28", getCtrlDestStr(messages.get(i++))); // poly_at ==> pitch: -24
			assertEquals("0/poly_at==>00:58", getCtrlDestStr(messages.get(i++))); // poly_at ==> pitch: +24
			
			// filter cutoff: 0x01
			assertEquals("0/mono_at==>01:7F", getCtrlDestStr(messages.get(i++))); // mono_at ==> filter_cutoff: +100%
			assertEquals("0/mono_at==>01:00", getCtrlDestStr(messages.get(i++))); // mono_at ==> filter_cutoff: -100%
			assertEquals("0/ctrl:01==>01:7F", getCtrlDestStr(messages.get(i++))); // mod ==> filter_cutoff: +63
			assertEquals("0/ctrl:01==>01:00", getCtrlDestStr(messages.get(i++))); // mod ==> filter_cutoff: -64
			
			// vol (amplitude): 0x02
			assertEquals("0/mono_at==>02:7F", getCtrlDestStr(messages.get(i++))); // mono_at ==> vol: +100%
			assertEquals("0/mono_at==>02:40", getCtrlDestStr(messages.get(i++))); // mono_at ==> vol: +0%
			assertEquals("0/ctrl:10==>02:20", getCtrlDestStr(messages.get(i++))); // ctrl=16 ==> vol: -50%
			assertEquals("0/ctrl:11==>02:00", getCtrlDestStr(messages.get(i++))); // ctrl=17 ==> vol: -64
			
			// lfo_pitch: 0x03
			assertEquals("0/mono_at==>03:00", getCtrlDestStr(messages.get(i++))); // mono_at ==> lfo_pitch: 0
			assertEquals("0/mono_at==>03:20", getCtrlDestStr(messages.get(i++))); // mono_at ==> lfo_pitch: 25%
			assertEquals("0/ctrl:46==>03:5F", getCtrlDestStr(messages.get(i++))); // var     ==> lfo_pitch: 75%
			assertEquals("0/ctrl:46==>03:7F", getCtrlDestStr(messages.get(i++))); // ctrl=70 ==> lfo_pitch: 127
			assertEquals("0/mono_at==>03:00", getCtrlDestStr(messages.get(i++))); // mono_at ==> lfo_pitch: -64
			assertEquals("0/mono_at==>03:20", getCtrlDestStr(messages.get(i++))); // mono_at ==> lfo_pitch: -50%
			assertEquals("0/ctrl:46==>03:5F", getCtrlDestStr(messages.get(i++))); // var     ==> lfo_pitch: +50%
			assertEquals("0/ctrl:46==>03:7F", getCtrlDestStr(messages.get(i++))); // ctrl=70 ==> lfo_pitch: +63
			
			// lfo_filter: 0x04
			assertEquals("0/mono_at==>04:00", getCtrlDestStr(messages.get(i++))); // mono_at ==> lfo_filter: 0%
			assertEquals("0/mono_at==>04:40", getCtrlDestStr(messages.get(i++))); // mono_at ==> lfo_filter: 64
			assertEquals("0/mono_at==>04:7F", getCtrlDestStr(messages.get(i++))); // mono_at ==> lfo_filter: 127
			
			// lfo_vol: 0x05
			assertEquals("0/poly_at==>05:00", getCtrlDestStr(messages.get(i++))); // poly_at ==> lfo_filter: 0%
			assertEquals("0/poly_at==>05:20", getCtrlDestStr(messages.get(i++))); // poly_at ==> lfo_filter: 25%
			assertEquals("0/poly_at==>05:7F", getCtrlDestStr(messages.get(i++))); // poly_at ==> lfo_filter: 127
			
			// channel 1
			// ctrl_dest.src(ctrl=95)
			//   .dest(pitch,+12).dest(filter_cutoff,-50%).dest(vol,-16)
			//   .dest(lfo_pitch,25%).dest(lfo_filter,0%).dest(lfo_vol,100%)
			//   .on()
			assertEquals("1/ctrl:5F==>00:4C,01:20,02:30,03:20,04:00,05:7F", getCtrlDestStr(messages.get(i++)));
			
			// no further messages
			assertEquals(messages.size(), i);
		}
	}
	
	/**
	 * Tests if the files in the examples directory can be parsed.
	 * 
	 * Needed in case of bugs that are not found by normal unit tests.
	 * 
	 * @throws ParseException if something went wrong.
	 */
	@Test
	void testExampleFiles() throws ParseException {
		String dirStr = System.getProperty("user.dir") + File.separator + "examples";
		File dir = new File(dirStr);
		for (File file : dir.listFiles()) {
			if (!file.isFile())
				continue;
			if (file.getName().endsWith(".midica") || file.getName().endsWith(".mpl")) {
				try {
					parse(file);
				}
				catch (Exception e) {
					System.err.println(file.getAbsolutePath() + " failed.");
					throw e;
				}
			}
		}
	}
	
	/**
	 * Tests for parsing full source files that are expected to throw a parsing exception.
	 * 
	 * The files are located in test/org/midica/testfiles/failing
	 */
	@Test
	void testParseFilesFailing() {
		ParseException e;
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("nestable-block-open-at-eof")) );
		assertEquals( 6, e.getLineNumber() );
		assertEquals( "0 d /4", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_NESTABLE_BLOCK_OPEN_AT_EOF)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("function-open-at-eof")) );
		assertEquals( 6, e.getLineNumber() );
		assertEquals( "0 d /4", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_NAMED_BLOCK_OPEN_AT_EOF)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("file-that-does-not-exist")) );
		assertTrue( e.getMessage().startsWith("java.io.FileNotFoundException:") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("include-failing-file")) );
		assertEquals( "instruments-with-nestable-block.midica", e.getFile().getName() );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "{", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_NOT_ALLOWED_IN_INSTR_BLK)) );
		e.getStackTraceElements();
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("include-not-existing-file")) );
		assertEquals( 1, e.getLineNumber() );
		assertEquals( "INCLUDE inc/not-existing-file.midica", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FILE_EXISTS)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("global-cmd-in-instruments")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "*", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_GLOBALS_IN_INSTR_DEF)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("instruments-in-block")) );
		assertEquals( 5, e.getLineNumber() );
		assertEquals( "INSTRUMENTS", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_BLOCK_UNMATCHED_OPEN)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("function-in-block")) );
		assertEquals( 5, e.getLineNumber() );
		assertEquals( "FUNCTION mac1", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_BLOCK_UNMATCHED_OPEN)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("using-channel-without-instr-def")) );
		assertEquals( 1, e.getLineNumber() );
		assertEquals( "2 c /4", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_CHANNEL_UNDEFINED).replaceFirst("%s", "2")) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("using-undefined-channel")) );
		assertEquals( 6, e.getLineNumber() );
		assertEquals( "2 c /4", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_CHANNEL_UNDEFINED).replaceFirst("%s", "2")) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("using-invalid-drumkit")) );
		assertEquals( 10, e.getLineNumber() );
		assertEquals( "p 128 testing", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_INSTR_BANK)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("instruments-with-duplicate-channel")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "1 0 test2", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_CHANNEL_REDEFINED), 1)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("instruments-with-param")) );
		assertEquals( 1, e.getLineNumber() );
		assertEquals( "INSTRUMENTS param", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_MODE_INSTR_NUM_OF_ARGS)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("end-with-param")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "END param", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_ARGS_NOT_ALLOWED)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("unmatched-end")) );
		assertEquals( 6, e.getLineNumber() );
		assertEquals( "END", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_CMD_END_WITHOUT_BEGIN)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("unmatched-close")) );
		assertEquals( 6, e.getLineNumber() );
		assertEquals( "}", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_BLOCK_UNMATCHED_CLOSE)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("function-nested")) );
		assertEquals( 5, e.getLineNumber() );
		assertEquals( "FUNCTION inner", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_NOT_ALLOWED_IN_BLK) + "FUNCTION") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("function-in-meta")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "FUNCTION test", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_NOT_ALLOWED_IN_BLK) + "FUNCTION") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("function-redefined")) );
		assertEquals( 8, e.getLineNumber() );
		assertEquals( "FUNCTION test", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNCTION_ALREADY_DEFINED)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("function-with-second-param")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "FUNCTION test1 test2", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNCTION_NUM_OF_ARGS)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("include-soundbank-twice")) );
		assertEquals( 6, e.getLineNumber() );
		assertEquals( "SOUNDBANK ../working/java-emergency-soundfont.sf2", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_SOUNDBANK_ALREADY_PARSED)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("include-soundbank-inside-block")) );
		assertEquals( 6, e.getLineNumber() );
		assertEquals( "SOUNDBANK ../working/java-emergency-soundfont.sf2", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_NOT_ALLOWED_IN_BLK) + "SOUNDBANK") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("include-soundbank-inside-function")) );
		assertEquals( 6, e.getLineNumber() );
		assertEquals( "SOUNDBANK ../working/java-emergency-soundfont.sf2", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_NOT_ALLOWED_IN_BLK) + "SOUNDBANK") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("unknown-cmd")) );
		assertEquals( 5, e.getLineNumber() );
		assertEquals( "UNKNOWN_CMD", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_UNKNOWN_CMD)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("block-reset-multiple")) );
		assertEquals( 5, e.getLineNumber() );
		assertEquals( "} m", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_BLOCK_ARG_ALREADY_SET) + "multiple") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("block-reset-quantity")) );
		assertEquals( 5, e.getLineNumber() );
		assertEquals( "} q=2", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_BLOCK_ARG_ALREADY_SET) + "quantity") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("block-reset-tuplet")) );
		assertEquals( 5, e.getLineNumber() );
		assertEquals( "} t", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_BLOCK_ARG_ALREADY_SET) + "tuplet") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("block-reset-shift")) );
		assertEquals( 5, e.getLineNumber() );
		assertEquals( "} s=3", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_BLOCK_ARG_ALREADY_SET) + "shift") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("block-param-invalid")) );
		assertEquals( 5, e.getLineNumber() );
		assertEquals( "} v=50", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_BLOCK_INVALID_OPT)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("chord-inside-block")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "CHORD testchord c d e", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_NOT_ALLOWED_IN_BLK)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("chord-inside-function")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "CHORD testchord c d e", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_NOT_ALLOWED_IN_BLK)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("chord-inside-instruments")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "CHORD testchord c d e", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_NOT_ALLOWED_IN_BLK)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("chord-without-param")) );
		assertEquals( 2, e.getLineNumber() );
		assertEquals( "CHORD", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_CHORD_NUM_OF_ARGS)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("chord-without-notes")) );
		assertEquals( 2, e.getLineNumber() );
		assertEquals( "CHORD test", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_CHORD_NUM_OF_ARGS)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("chord-redefined")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "CHORD test c/d/e", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_CHORD_ALREADY_DEFINED)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("chord-name-like-note")) );
		assertEquals( 2, e.getLineNumber() );
		assertEquals( "CHORD c# c/d/c", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_CHORD_EQUALS_NOTE)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("chord-name-like-percussion")) );
		assertEquals( 2, e.getLineNumber() );
		assertEquals( "CHORD hhc c/d/e", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_CHORD_EQUALS_PERCUSSION)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("chord-with-duplicate-note")) );
		assertEquals( 2, e.getLineNumber() );
		assertEquals( "CHORD test c/d/c", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_CHORD_CONTAINS_ALREADY)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("chord-separator-double")) );
		assertEquals( 9, e.getLineNumber() );
		assertEquals( "CHORD crd=c,d,,e", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_CHORD_REDUNDANT_SEP)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("chord-separator-leading")) );
		assertEquals( 5, e.getLineNumber() );
		assertEquals( "CHORD crd = /c/d/e", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_CHORD_REDUNDANT_SEP)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("chord-separator-trailing")) );
		assertEquals( 5, e.getLineNumber() );
		assertEquals( "CHORD crd = c/d/e/", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_CHORD_REDUNDANT_SEP)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("call-with-invalid-option")) );
		assertEquals( 6, e.getLineNumber() );
		assertEquals( "CALL test v=50", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_CALL_UNKNOWN_OPT)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("call-with-recursion")) );
		assertEquals( 5, e.getLineNumber() );
		assertEquals( "CALL test", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNCTION_RECURSION)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("call-with-recursion-depth")) );
		assertEquals( 3, e.getLineNumber() );
		assertTrue( "CALL test2".equals(e.getLineContent()) || "CALL test1".equals(e.getLineContent()) );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNCTION_RECURSION_DEPTH)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("call-undefined-function")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "CALL test", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNCTION_UNDEFINED)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("call-without-name")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "CALL", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_CALL_NUM_OF_ARGS)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("instruments-with-more-instr-sep")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "10	0,0,0 test", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_NOT_AN_INTEGER) + "0,0") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("instruments-with-more-bank-sep")) );
		assertEquals( 5, e.getLineNumber() );
		assertEquals( "12	0,0/0/0 test", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_NOT_AN_INTEGER) + "0/0") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("instruments-with-big-banknumber")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "10	0,9999999 test", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_INSTR_BANK)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("instruments-with-big-msb")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "10	0,128/0 test", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_INSTR_BANK)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("instruments-with-big-lsb")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "10	0,0/128 test", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_INSTR_BANK)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("instruments-with-missing-bank")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "10	0, test", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_NOT_AN_INTEGER)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("instruments-with-missing-lsb")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "10	0,0/ test", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_NOT_AN_INTEGER)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("meta-in-block")) );
		assertEquals( 5, e.getLineNumber() );
		assertEquals( "META", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_BLOCK_UNMATCHED_OPEN)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("meta-with-block")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "{", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_NOT_ALLOWED_IN_META_BLK)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("meta-in-function")) );
		assertEquals( 5, e.getLineNumber() );
		assertEquals( "META", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_NOT_ALLOWED_IN_BLK) + "META") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("meta-with-param")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "META test", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_META_NUM_OF_ARGS)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("sk-with-param")) );
		assertEquals( 6, e.getLineNumber() );
		assertEquals( "SOFT_KARAOKE test", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_SOFT_KARAOKE_NUM_OF_ARGS)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("sk-in-function")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "SOFT_KARAOKE", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_SOFT_KARAOKE_NOT_ALLOWED_HERE)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("sk-in-root-lvl")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "SOFT_KARAOKE", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_SOFT_KARAOKE_NOT_ALLOWED_HERE)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("sk-duplicate")) );
		assertEquals( 9, e.getLineNumber() );
		assertEquals( "SOFT_KARAOKE", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_SOFT_KARAOKE_ALREADY_SET)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("sk-duplicate-author")) );
		assertEquals( 9, e.getLineNumber() );
		assertEquals( "author     another author", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_SK_VALUE_ALREADY_SET) + "author") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("sk-unknown-sk-cmd")) );
		assertEquals( 8, e.getLineNumber() );
		assertEquals( "composer   Haydn", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_SOFT_KARAOKE_UNKNOWN_CMD) + "composer") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("sk-unknown-cmd")) );
		assertEquals( 8, e.getLineNumber() );
		assertEquals( "testcmd    Haydn", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_UNKNOWN_CMD) + "testcmd") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("sk-field-with-crlf")) );
		assertEquals( 15, e.getLineNumber() );
		assertEquals( "title      sk\\rtitle", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_SK_FIELD_CRLF_NOT_ALLOWED)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("sk-lyrics-with-crlf")) );
		assertEquals( 15, e.getLineNumber() );
		assertEquals( "0  c  /4  l=_te\\nst5", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_SK_SYLLABLE_CRLF_NOT_ALLOWED)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("chord-inside-meta")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "CHORD testchord c d e", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_META_UNKNOWN_CMD) + "CHORD") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("note-in-percussion-channel")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "p c /4", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_UNKNOWN_PERCUSSION) + "c") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("note-unknown")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "0 c+6 /4", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_UNKNOWN_NOTE) + "c+6") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("chord-with-unknown-note")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "CHORD test c d e c+6", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_UNKNOWN_CHORD_ELEMENT) + "c+6") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("chord-with-unknown-note-number")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "CHORD test c d 128", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_NOTE_TOO_BIG) + "128") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("chord-assigner-double")) );
		assertEquals( 5, e.getLineNumber() );
		assertEquals( "CHORD crd==c/d/e", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_UNKNOWN_CHORD_ELEMENT) + "=c") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("chord-with-note-percussion-mix")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "CHORD test c d to", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_CHORD_WITH_NOTES_AND_PERC) + "test") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("channel-cmd-missing-param")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "0 c", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_CH_CMD_NUM_OF_ARGS)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("channel-rest-missing-param")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "0 -", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_CH_CMD_NUM_OF_ARGS)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("channel-if")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "0 c /4 if=123", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_CHANNEL_INVALID_OPT) + "if") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("instrument-in-instruments")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "INSTRUMENT 1 0 test", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_SINGLE_INSTR_IN_INSTR_DEF)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("var-in-instruments")) );
		assertEquals( 6, e.getLineNumber() );
		assertEquals( "$ch 60 test", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_VAR_NOT_ALLOWED)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("var-undefined")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "0  c  l=$x", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_VAR_NOT_DEFINED) + "$x") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("param-i-outside-function")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "$[1]", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_PARAM_OUTSIDE_FUNCTION) + "$[1]") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("param-i-outside-function-nested")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "$[1]", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_PARAM_OUTSIDE_FUNCTION) + "$[1]") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("param-n-outside-function")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "${x}", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_PARAM_OUTSIDE_FUNCTION) + "${x}") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("param-n-outside-function-nested")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "${x}", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_PARAM_OUTSIDE_FUNCTION) + "${x}") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("param-i-with-name")) );
		assertEquals( 6, e.getLineNumber() );
		assertEquals( "$[x]", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_NOT_AN_INTEGER)) );
		assertTrue( e.getFullMessage().contains(Dict.get(Dict.EXCEPTION_CAUSED_BY_INVALID_VAR)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("block-if-not-alone")) );
		assertEquals( 5, e.getLineNumber() );
		assertEquals( "{ if=$x, elsif $x", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_BLOCK_IF_MUST_BE_ALONE))
			|| e.getMessage().startsWith(Dict.get(Dict.ERROR_BLOCK_ELSIF_MUST_BE_ALONE)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("block-if-not-alone-2")) );
		assertEquals( 7, e.getLineNumber() );
		assertEquals( "} if $x", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_BLOCK_IF_MUST_BE_ALONE)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("block-if-not-alone-nested")) );
		assertEquals( 8, e.getLineNumber() );
		assertEquals( "} if $x", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_BLOCK_IF_MUST_BE_ALONE)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("block-elsif-not-alone")) );
		assertEquals( 8, e.getLineNumber() );
		assertEquals( "{ elsif=$x, if $x", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_BLOCK_IF_MUST_BE_ALONE))
			|| e.getMessage().startsWith(Dict.get(Dict.ERROR_BLOCK_ELSIF_MUST_BE_ALONE)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("block-elsif-not-alone-2")) );
		assertEquals( 10, e.getLineNumber() );
		assertEquals( "} elsif $x", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_BLOCK_ELSIF_MUST_BE_ALONE)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("block-elsif-not-alone-nested")) );
		assertEquals( 11, e.getLineNumber() );
		assertEquals( "} elsif $x", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_BLOCK_ELSIF_MUST_BE_ALONE)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("block-else-not-alone")) );
		assertEquals( 8, e.getLineNumber() );
		assertEquals( "{ else, elsif=$x", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_BLOCK_ELSIF_MUST_BE_ALONE))
			|| e.getMessage().startsWith(Dict.get(Dict.ERROR_BLOCK_ELSE_MUST_BE_ALONE)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("block-else-not-alone-2")) );
		assertEquals( 10, e.getLineNumber() );
		assertEquals( "} else", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_BLOCK_ELSE_MUST_BE_ALONE)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("block-else-not-alone-nested")) );
		assertEquals( 11, e.getLineNumber() );
		assertEquals( "} else", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_BLOCK_ELSE_MUST_BE_ALONE)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("block-elsif-without-if")) );
		assertEquals( 11, e.getLineNumber() );
		assertTrue( e.getFullMessage().contains(Dict.get(Dict.EXCEPTION_CAUSED_BY_BLK_COND)) );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_BLOCK_NO_IF_FOUND)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("block-elsif-without-if-nested")) );
		assertEquals( 13, e.getLineNumber() );
		assertTrue( e.getFullMessage().contains(Dict.get(Dict.EXCEPTION_CAUSED_BY_BLK_COND)) );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_BLOCK_NO_IF_FOUND)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("block-else-without-if")) );
		assertEquals( 11, e.getLineNumber() );
		assertTrue( e.getFullMessage().contains(Dict.get(Dict.EXCEPTION_CAUSED_BY_BLK_COND)) );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_BLOCK_NO_IF_FOUND)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("block-else-without-if-2")) );
		assertEquals( 12, e.getLineNumber() );
		assertTrue( e.getFullMessage().contains(Dict.get(Dict.EXCEPTION_CAUSED_BY_BLK_COND)) );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_BLOCK_NO_IF_FOUND)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("block-else-without-if-nested")) );
		assertEquals( 13, e.getLineNumber() );
		assertTrue( e.getFullMessage().contains(Dict.get(Dict.EXCEPTION_CAUSED_BY_BLK_COND)) );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_BLOCK_NO_IF_FOUND)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("block-else-without-if-nested-2")) );
		assertEquals( 14, e.getLineNumber() );
		assertTrue( e.getFullMessage().contains(Dict.get(Dict.EXCEPTION_CAUSED_BY_BLK_COND)) );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_BLOCK_NO_IF_FOUND)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("call-elsif")) );
		assertEquals( 8, e.getLineNumber() );
		assertEquals( "CALL test elsif=$x", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_CALL_UNKNOWN_OPT)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("call-else")) );
		assertEquals( 8, e.getLineNumber() );
		assertEquals( "CALL test else", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_CALL_UNKNOWN_OPT)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("call-if-not-alone")) );
		assertEquals( 5, e.getLineNumber() );
		assertEquals( "CALL test if=$x, if $x", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_DUPLICATE_OPTION) + "if") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("cond-too-many-operators")) );
		assertEquals( 5, e.getLineNumber() );
		assertEquals( "{ if $x==$x!=$x", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_TOO_MANY_OPERATORS_IN_COND)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("cond-defined-with-whitespace")) );
		assertEquals( 5, e.getLineNumber() );
		assertEquals( "{ if $x $x", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_COND_DEFINED_HAS_WHITESPACE)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("cond-first-with-whitespace")) );
		assertEquals( 5, e.getLineNumber() );
		assertEquals( "{ if $x $x==5", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_COND_WHITESPACE_IN_FIRST_OP)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("cond-second-with-whitespace")) );
		assertEquals( 5, e.getLineNumber() );
		assertEquals( "{ if $x==5 5", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_COND_WHITESPACE_IN_SEC_OP)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("cond-undef-empty")) );
		assertEquals( 5, e.getLineNumber() );
		assertEquals( "{ if !", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_COND_UNDEF_EMPTY)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("cond-undef-not-at-start")) );
		assertEquals( 5, e.getLineNumber() );
		assertEquals( "{ if $x!$x", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_COND_UNDEF_IN_CENTER)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("cond-in-with-whitespace")) );
		assertEquals( 5, e.getLineNumber() );
		assertEquals( "{ if $x in 1;2;3 4;5", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_COND_WHITESPACE_IN_IN_ELEM)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("cond-empty-in-element")) );
		assertEquals( 5, e.getLineNumber() );
		assertEquals( "{ if $x in 1;2;;5", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_COND_EMPTY_ELEM_IN_IN_LIST)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("opt-assigner-double")) );
		assertEquals( 5, e.getLineNumber() );
		assertEquals( "0  c  /4  v==100", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_NOT_AN_INTEGER) + "=100") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("opt-without-value-velocity")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "0  c  /4  v", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_OPTION_NEEDS_VAL) + "v") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("opt-without-value-velocity-2")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "0  c  /4  v=", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_OPTION_NEEDS_VAL) + "v") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("opt-without-value-duration")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "0  c  /4  d", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_OPTION_NEEDS_VAL) + "d") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("opt-without-value-duration-2")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "0  c  /4  d=", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_OPTION_NEEDS_VAL) + "d") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("opt-without-value-quantity")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "0  c  /4  q", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_OPTION_NEEDS_VAL) + "q") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("opt-without-value-quantity-2")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "0  c  /4  q=", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_OPTION_NEEDS_VAL) + "q") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("opt-without-value-lyrics")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "0  c  /4  l", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_OPTION_NEEDS_VAL) + "l") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("opt-without-value-lyrics-2")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "0  c  /4  lyrics=", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_OPTION_NEEDS_VAL) + "lyrics") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("opt-without-value-tremolo")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "0  c  /4  tr", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_OPTION_NEEDS_VAL) + "tr") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("opt-without-value-tremolo-2")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "0  c  /4  tr=", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_OPTION_NEEDS_VAL) + "tr") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("opt-without-value-shift")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "0  c  /4  s", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_OPTION_NEEDS_VAL) + "s") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("opt-without-value-shift-2")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "0  c  /4  s=", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_OPTION_NEEDS_VAL) + "s") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("opt-without-value-if")) );
		assertEquals( 5, e.getLineNumber() );
		assertEquals( "{ if", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_OPTION_NEEDS_VAL) + "if") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("opt-without-value-if-2")) );
		assertEquals( 5, e.getLineNumber() );
		assertEquals( "{ if=", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_OPTION_NEEDS_VAL) + "if") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("opt-without-value-elsif")) );
		assertEquals( 8, e.getLineNumber() );
		assertEquals( "{ elsif", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_OPTION_NEEDS_VAL) + "elsif") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("opt-without-value-elsif-2")) );
		assertEquals( 8, e.getLineNumber() );
		assertEquals( "{ elsif=", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_OPTION_NEEDS_VAL) + "elsif") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("opt-with-value-multiple")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "0  c  /4  m=", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_OPTION_VAL_NOT_ALLOWED) + "m") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("opt-with-value-else")) );
		assertEquals( 8, e.getLineNumber() );
		assertEquals( "{ else=", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_OPTION_VAL_NOT_ALLOWED) + "else") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("opt-tuplet-invalid")) );
		assertEquals( 5, e.getLineNumber() );
		assertEquals( "{ t=5", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_TUPLET_INVALID) + "5") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("opt-tuplet-invalid-2")) );
		assertEquals( 5, e.getLineNumber() );
		assertEquals( "{ t=5:0", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_TUPLET_INVALID)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("opt-tuplet-invalid-3")) );
		assertEquals( 5, e.getLineNumber() );
		assertEquals( "{ t=5:", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_TUPLET_INVALID) + "5:") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("opt-tuplet-invalid-4")) );
		assertEquals( 5, e.getLineNumber() );
		assertEquals( "{ t=:3", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_TUPLET_INVALID) + ":3") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("opt-unknown")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "{ xyz=5", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_UNKNOWN_OPTION) + "xyz") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("opt-velocity-too-high")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "0  c  /4  v=128", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_VEL_NOT_MORE_THAN_127)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("opt-velocity-negative")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "0  c  /4  v=-2", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_NEGATIVE_NOT_ALLOWED)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("opt-velocity-zero")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "0  c  /4  v=0", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_VEL_NOT_LESS_THAN_1)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("opt-duration-zero")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "0  c  /4  d=0", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_DURATION_MORE_THAN_0)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("opt-duration-not-float")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "0  c  /4  d=0.1.2", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_NOT_A_FLOAT)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("opt-duration-negative")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "0  c  /4  d=-0.1", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_DURATION_MORE_THAN_0)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("channel-negative")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "-1  c  /4", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_UNKNOWN_CMD) + "-1") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("channel-negative-2")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "-1  c  /4", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_UNKNOWN_CMD) + "-1") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("channel-too-high")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "16  c  /4", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_INVALID_CHANNEL_NUMBER)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("call-param-empty")) );
		assertEquals( 6, e.getLineNumber() );
		assertEquals( "CALL test(a,)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_CALL_EMPTY_PARAM)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("call-param-name-empty")) );
		assertEquals( 6, e.getLineNumber() );
		assertEquals( "CALL test(=a)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_CALL_PARAM_NAME_EMPTY)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("call-param-name-invalid")) );
		assertEquals( 6, e.getLineNumber() );
		assertEquals( "CALL test(a/=b)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_CALL_PARAM_NAME_WITH_SPEC)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("call-param-name-doublet")) );
		assertEquals( 6, e.getLineNumber() );
		assertEquals( "CALL test(a=x,a=y)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_CALL_DUPLICATE_PARAM_NAME)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("call-param-value-empty")) );
		assertEquals( 6, e.getLineNumber() );
		assertEquals( "CALL test(a=,b=y)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_CALL_PARAM_VALUE_EMPTY)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("call-param-named-more-assigners")) );
		assertEquals( 6, e.getLineNumber() );
		assertEquals( "CALL test(a=b=c)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_CALL_PARAM_MORE_ASSIGNERS)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("call-param-named-more-assigners-2")) );
		assertEquals( 5, e.getLineNumber() );
		assertEquals( "CALL f(vel==102)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_CALL_PARAM_MORE_ASSIGNERS)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("include-directory")) );
		assertEquals( 1, e.getLineNumber() );
		assertEquals( "INCLUDE inc/", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FILE_NORMAL)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("include-without-args")) );
		assertEquals( 1, e.getLineNumber() );
		assertEquals( "INCLUDE", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FILE_NUM_OF_ARGS)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("const-assigner-double")) );
		assertEquals( 7, e.getLineNumber() );
		assertEquals( "0  c  /4  v==103", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_NOT_AN_INTEGER) + "=103") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("include-too-many-args")) );
		assertEquals( 1, e.getLineNumber() );
		assertEquals( "INCLUDE inc/instruments.midica  inc/instruments.midica", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FILE_NUM_OF_ARGS)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("soundbank-file-doesnt-exist")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "SOUNDBANK  soundfont.sf2", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FILE_EXISTS)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("soundbank-file-not-normal")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "SOUNDBANK  .", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FILE_NORMAL)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("soundbank-file-no-args")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "SOUNDBANK", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_SOUNDBANK_NUM_OF_ARGS)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("soundbank-file-too-many-args")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "SOUNDBANK  ../working/java-emergency-soundfont.sf2  ../working/java-emergency-soundfont.sf2", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_SOUNDBANK_NUM_OF_ARGS)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("soundbank-file-invalid")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "SOUNDBANK  inc/instruments.midica", e.getLineContent() );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("soundbank-url-invalid")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "SOUNDBANK https:/ /midica.org/assets/sound/invalid.sf2", e.getLineContent() );
		assertTrue( e.getMessage().contains(Dict.get(Dict.INVALID_RIFF)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("soundbank-url-404")) );
		assertEquals( 3, e.getLineNumber() );
		assertTrue( e.getMessage().contains(Dict.get(Dict.DOWNLOAD_PROBLEM)) );
		assertEquals( "SOUNDBANK https:/ /midica.org/assets/sound/404.sf2", e.getLineContent() );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("define-not-enough-args")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "DEFINE CHORD", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_DEFINE_NUM_OF_ARGS)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("define-too-many-args")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "DEFINE CHORD crd crd", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_DEFINE_NUM_OF_ARGS)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("define-unknown-id")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "DEFINE UNKNOWN_ID something", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_UNKNOWN_COMMAND_ID)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("define-twice")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "DEFINE CHORD b", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_ALREADY_REDEFINED) + "CHORD") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("define-assigner-double")) );
		assertEquals( 5, e.getLineNumber() );
		assertEquals( "DEFINE CHORD == CRD", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_DEFINE_NUM_OF_ARGS)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("define-assigner-double-2")) );
		assertEquals( 8, e.getLineNumber() );
		assertEquals( "CRD crd c/d/e", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_UNKNOWN_CMD) + "CRD") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("const-without-args")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "CONST", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_CONST_NUM_OF_ARGS)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("const-not-enough-args")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "CONST $crd", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_CONST_NUM_OF_ARGS)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("const-already-defined")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "CONST $crd c+/d+/e+", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_CONST_ALREADY_DEFINED)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("const-without-dollar")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "CONST xy = z", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_CONST_NAME_INVALID) + "xy") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("const-name-eq-value")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "CONST $a = $a", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_CONST_NAME_EQ_VALUE)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("const-recursion")) );
		assertEquals( 3, e.getLineNumber() );
		assertTrue( "CONST $a = !$b!".equals(e.getLineContent()) || "CONST $b = !$a!".equals(e.getLineContent()) );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_CONST_RECURSION)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("var-without-args")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "VAR", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_VAR_NUM_OF_ARGS)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("var-assigner-double")) );
		assertEquals( 7, e.getLineNumber() );
		assertEquals( "0  c  /4  v=$x", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_NOT_AN_INTEGER) + "=100") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("var-not-enough-args")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "VAR $x", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_VAR_NUM_OF_ARGS)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("var-without-dollar")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "VAR x = c", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_VAR_NAME_INVALID) + "x") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("var-name-eq-value")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "VAR $a = $a", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_VAR_NAME_EQ_VALUE)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("var-recursion")) );
		assertEquals( 5, e.getLineNumber() );
		assertEquals( "VAR $a = $d$d$d$d$d$d$d$d$d$d$d$d$d$d$d$d$d$d$d$d$d$d$d$d$d$d$d$d$d$d$d$d$d$d$d$d$d$d$d$d$d$d$d$d$d$d$d$d$d$d$d$a", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_VAR_RECURSION)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("var-with-whitespace")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "VAR x = c c", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_VAR_VAL_HAS_WHITESPACE)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("param-n-assign-unknown-name")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "VAR ${y} = b", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_PARAM_NAMED_UNKNOWN)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("param-i-assign-index-too-high")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "VAR $[0] = a", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_PARAM_INDEX_TOO_HIGH)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("instrument-not-enough-args")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "INSTRUMENT 0", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_INSTR_NUM_OF_ARGS_SINGLE)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("instruments-elem-not-enough-args")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0 5", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_INSTR_NUM_OF_ARGS)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("global-timesig-invalid")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "* time 3:4", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_INVALID_TIME_SIG) + "3:4") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("global-tonality-invalid")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "* key c/inval", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_INVALID_TONALITY) + "inval") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("global-keysig-invalid-note")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "* key d5/maj", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_UNKNOWN_NOTE) + "d5") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("global-keysig-invalid")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "* key d5:maj", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_INVALID_KEY_SIG) + "d5:maj") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("global-unknown-cmd")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "* cmd d5:maj", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_UNKNOWN_GLOBAL_CMD) + "cmd") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("global-partial-empty")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "* 0,1-2,,3", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_PARTIAL_RANGE_EMPTY)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("global-partial-order")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "* 0,2-2,3", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_PARTIAL_RANGE_ORDER) + "2-2") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("global-partial-invalid-range-elem")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "* 0,2-3-4,5", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_PARTIAL_RANGE) + "2-3-4") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("pattern-inside-function")) );
		assertEquals( 5, e.getLineNumber() );
		assertEquals( "PATTERN", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_NOT_ALLOWED_IN_BLK)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("pattern-magic-cond-idx-too-high")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "0 [2] /4", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_PATTERN_INDEX_TOO_HIGH_2)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("pattern-magic-cond-idx-too-high-2")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "0: [2]", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_PATTERN_INDEX_TOO_HIGH_2)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("pattern-magic-cond-idx-too-high-3")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "0: 60/62/[2]", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_PATTERN_INDEX_TOO_HIGH_2)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("pattern-magic-cond-idx-too-high-4")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "0 60/62/[2] /4", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_PATTERN_INDEX_TOO_HIGH_2)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("pattern-inside-block")) );
		assertEquals( 5, e.getLineNumber() );
		assertEquals( "PATTERN", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_BLOCK_UNMATCHED_OPEN)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("pattern-redefined")) );
		assertEquals( 7, e.getLineNumber() );
		assertEquals( "PATTERN p1", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_PATTERN_ALREADY_DEFINED) + "p1") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("pattern-def-with-second-arg")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "PATTERN p1 test", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_PATTERN_NUM_OF_ARGS)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("pattern-def-without-name")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "PATTERN", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_PATTERN_NUM_OF_ARGS)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("pattern-call-with-tremolo")) );
		assertEquals( 7, e.getLineNumber() );
		assertEquals( "0 c pat tr=/4", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_PATTERN_INVALID_OUTER_OPT) + "tremolo") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("pattern-with-shift")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0 /1  s=1", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_PATTERN_INVALID_INNER_OPT) + "shift") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("pattern-with-if")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "0 /1 if y == x", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_PATTERN_INVALID_INNER_OPT) + "if") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("pattern-with-if-2")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "0 pat_b(x, y, z) if y == x", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_PATTERN_INVALID_INNER_OPT) + "if") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("pattern-call-without-param-close")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "0 c/d/e  simple(foo, bar q=2, m", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_UNKNOWN_OPTION) + "(foo") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("pattern-undefined")) );
		assertEquals( 6, e.getLineNumber() );
		assertEquals( "0  not_existing_pattern( x, y, z )  v = 120 , d = 80%", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_PATTERN_UNDEFINED) + "not_existing_pattern") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("pattern-undefined-2")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "0 /42", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_NOTE_LENGTH_INVALID) + "/42") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("pattern-call-index-wrong")) );
		assertEquals( 5, e.getLineNumber() );
		assertEquals( "1.2 /4", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_PATTERN_INDEX_INVALID)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("pattern-call-index-too-high")) );
		assertEquals( 10, e.getLineNumber() );
		assertEquals( "1 /4", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_PATTERN_INDEX_TOO_HIGH)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("pattern-recursion")) );
		assertEquals( 13, e.getLineNumber() );
		assertTrue( "1/0 first".equals(e.getLineContent()) || "0/1 second".equals(e.getLineContent()) );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_PATTERN_RECURSION_DEPTH)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("pattern-before-instruments")) );
		assertEquals( 6, e.getLineNumber() );
		assertEquals( "0/1 /4", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_CHANNEL_UNDEFINED).replaceFirst("%s", "0")) );
		
		// stacktraces
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("stacktrace")) );
		assertEquals( 10, e.getLineNumber() );
		assertEquals( "st-incl-1.midica", e.getFile().getName() );
		assertEquals( "0 62 -2 shift=2", e.getLineContent() );
		assertEquals( true, e.getMessage().startsWith(Dict.get(Dict.ERROR_NOTE_LENGTH_INVALID)) );
		Deque<StackTraceElement> stackTrace = e.getStackTraceElements();
		assertEquals( 9, stackTrace.size() );
		assertEquals( "st-incl-2.midica/21",     stackTrace.pop().toString() ); // channel cmd
		assertEquals( "st-incl-2.midica/19-23",  stackTrace.pop().toString() ); // block
		assertEquals( "st-incl-2.midica/17-25",  stackTrace.pop().toString() ); // block
		assertEquals( "st-incl-2.midica/25",     stackTrace.pop().toString() ); // in test2(...)
		assertEquals( "st-incl-5.midica/12",     stackTrace.pop().toString() ); // CALL test2(...) from test5
		assertEquals( "stacktrace.midica/40",    stackTrace.pop().toString() ); // CALL test5(...) from test6
		assertEquals( "stacktrace.midica/38-42", stackTrace.pop().toString() ); // block
		assertEquals( "stacktrace.midica/42",    stackTrace.pop().toString() ); // in test6(...)
		assertEquals( "st-incl-1.midica/10",     stackTrace.pop().toString() ); // CALL test6(...) from root
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("pattern-stacktrace")) );
		assertEquals( 25, e.getLineNumber() );
		assertEquals( "{ t, l=test", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_BLOCK_INVALID_OPT)) );
		stackTrace = e.getStackTraceElements();
		assertEquals( 5, stackTrace.size() );
		assertEquals( "pattern-stacktrace.midica/5",     stackTrace.pop().toString() ); // block with invaid option
		assertEquals( "pattern-stacktrace.midica/19",    stackTrace.pop().toString() ); // pattern call
		assertEquals( "pattern-stacktrace.midica/24",    stackTrace.pop().toString() ); // CALL test()
		assertEquals( "pattern-stacktrace.midica/23-25", stackTrace.pop().toString() ); // block
		assertEquals( "pattern-stacktrace.midica/25",    stackTrace.pop().toString() ); // block execution
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("pattern-stacktrace-2")) );
		assertEquals( 6, e.getLineNumber() );
		assertEquals( "{ l=test", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_BLOCK_INVALID_OPT)) );
		stackTrace = e.getStackTraceElements();
		assertEquals( 8, stackTrace.size() );
		assertEquals( "pat-st-incl.midica/4",            stackTrace.pop().toString() ); // block with invaid option
		assertEquals( "pat-st-incl.midica/19",           stackTrace.pop().toString() ); // call inner pattern
		assertEquals( "pat-st-incl.midica/17-21",        stackTrace.pop().toString() ); // block with m, q=2
		assertEquals( "pat-st-incl.midica/15-24",        stackTrace.pop().toString() ); // block with t
		assertEquals( "pat-st-incl.midica/24",           stackTrace.pop().toString() ); // block execution
		assertEquals( "pattern-stacktrace-2.midica/5",   stackTrace.pop().toString() ); // call outer pattern
		assertEquals( "pattern-stacktrace-2.midica/4-6", stackTrace.pop().toString() ); // block
		assertEquals( "pattern-stacktrace-2.midica/6",   stackTrace.pop().toString() ); // block execution
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("stacktrace-function")) );
		assertEquals( 13, e.getLineNumber() );
		assertEquals( "0 f/e -", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_ZEROLENGTH_NOT_ALLOWED)) );
		stackTrace = e.getStackTraceElements();
		assertEquals( 11, stackTrace.size() );
		assertEquals( "stacktrace-function.midica/47",    stackTrace.pop().toString() ); // channel cmd with invalid note length
		assertEquals( "stacktrace-function.midica/41",    stackTrace.pop().toString() ); // CALL fun3(...,-) from fun2()
		assertEquals( "stacktrace-function.midica/40-42", stackTrace.pop().toString() ); // else-block
		assertEquals( "stacktrace-function.midica/35-43", stackTrace.pop().toString() ); // block
		assertEquals( "stacktrace-function.midica/43",    stackTrace.pop().toString() ); // block execution
		assertEquals( "stacktrace-function.midica/27",    stackTrace.pop().toString() ); // CALL fun2(...,f=bar)
		assertEquals( "stacktrace-function.midica/23-28", stackTrace.pop().toString() ); // block
		assertEquals( "stacktrace-function.midica/21-30", stackTrace.pop().toString() ); // block
		assertEquals( "stacktrace-function.midica/30",    stackTrace.pop().toString() ); // block execution
		assertEquals( "stacktrace-function.midica/16",    stackTrace.pop().toString() ); // CALL fun1(...) from func()
		assertEquals( "stacktrace-function.midica/13",    stackTrace.pop().toString() ); // CALL func(...)
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("stacktrace-pattern")) );
		assertEquals( 13, e.getLineNumber() );
		assertEquals( "0/1/2 -", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_ZEROLENGTH_NOT_ALLOWED)) );
		stackTrace = e.getStackTraceElements();
		assertEquals( 11, stackTrace.size() );
		assertEquals( "stacktrace-pattern.midica/47",    stackTrace.pop().toString() ); // channel cmd with invalid note length
		assertEquals( "stacktrace-pattern.midica/41",    stackTrace.pop().toString() ); // CALL pat3(-) from pat2()
		assertEquals( "stacktrace-pattern.midica/40-42", stackTrace.pop().toString() ); // else-block
		assertEquals( "stacktrace-pattern.midica/35-43", stackTrace.pop().toString() ); // block
		assertEquals( "stacktrace-pattern.midica/43",    stackTrace.pop().toString() ); // block execution
		assertEquals( "stacktrace-pattern.midica/27",    stackTrace.pop().toString() ); // CALL pat2(f=bar, ...)
		assertEquals( "stacktrace-pattern.midica/23-28", stackTrace.pop().toString() ); // block
		assertEquals( "stacktrace-pattern.midica/21-30", stackTrace.pop().toString() ); // block
		assertEquals( "stacktrace-pattern.midica/30",    stackTrace.pop().toString() ); // block execution
		assertEquals( "stacktrace-pattern.midica/16",    stackTrace.pop().toString() ); // CALL pat1(...) from func()
		assertEquals( "stacktrace-pattern.midica/13",    stackTrace.pop().toString() ); // CALL func(...)
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("stacktrace-compact-pattern")) );
		assertEquals( 13, e.getLineNumber() );
		assertEquals( "0: 65/64/62:-", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_ZEROLENGTH_NOT_ALLOWED)) );
		stackTrace = e.getStackTraceElements();
		assertEquals( 11, stackTrace.size() );
		assertEquals( "stacktrace-compact-pattern.midica/66",    stackTrace.pop().toString() ); // : f/e/d:-
		assertEquals( "stacktrace-compact-pattern.midica/58",    stackTrace.pop().toString() ); // CALL pat3(-) from pat2()
		assertEquals( "stacktrace-compact-pattern.midica/56-59", stackTrace.pop().toString() ); // else-block
		assertEquals( "stacktrace-compact-pattern.midica/48-60", stackTrace.pop().toString() ); // block
		assertEquals( "stacktrace-compact-pattern.midica/60",    stackTrace.pop().toString() ); // block execution
		assertEquals( "stacktrace-compact-pattern.midica/39",    stackTrace.pop().toString() ); // CALL pat2(f=baz, ...)
		assertEquals( "stacktrace-compact-pattern.midica/35-41", stackTrace.pop().toString() ); // block
		assertEquals( "stacktrace-compact-pattern.midica/33-43", stackTrace.pop().toString() ); // block
		assertEquals( "stacktrace-compact-pattern.midica/43",    stackTrace.pop().toString() ); // block execution
		assertEquals( "stacktrace-compact-pattern.midica/22",    stackTrace.pop().toString() ); // CALL pat1(...) from func()
		assertEquals( "stacktrace-compact-pattern.midica/13",    stackTrace.pop().toString() ); // CALL func(...)
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("zero-tempo")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "* tempo 0", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_0_NOT_ALLOWED)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("zero-quantity")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "1: (q=0) c", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_0_NOT_ALLOWED)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("zero-for-note")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "0 c -", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_ZEROLENGTH_NOT_ALLOWED)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("zero-in-compact-opt")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "0: (length=-)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_OPT_LENGTH_MORE_THAN_0) + "length") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("zero-for-note-2")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "0 -", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_ZEROLENGTH_NOT_ALLOWED)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("zero-for-chord")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "0 c/d -", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_ZEROLENGTH_NOT_ALLOWED)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("zero-in-summand")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "0 - /4+-+/8", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_ZEROLENGTH_IN_SUM)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("zero-with-m")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "0 - - m", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_ZEROLENGTH_INVALID_OPTION) + OPT_MULTIPLE) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("zero-with-m-2")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "- - m", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_ZEROLENGTH_INVALID_OPTION) + OPT_MULTIPLE) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("zero-with-m-3")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "- - m", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_ZEROLENGTH_INVALID_OPTION) + OPT_MULTIPLE) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("zero-with-q")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "0 - - q=5", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_ZEROLENGTH_INVALID_OPTION) + OPT_QUANTITY) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("zero-with-s")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "0 - - s=5", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_ZEROLENGTH_INVALID_OPTION) + OPT_SHIFT) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("zero-with-tr")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "0 - - tr=5", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_ZEROLENGTH_INVALID_OPTION) + OPT_TREMOLO) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("compact-channel-var-invalid")) );
		assertEquals( 5, e.getLineNumber() );
		assertEquals( "test: c:/4", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_UNKNOWN_CMD) + "test:") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("compact-cmd-without-instruments")) );
		assertEquals( 1, e.getLineNumber() );
		assertEquals( "0: c d e", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_CHANNEL_UNDEFINED).replaceFirst("%s", "0")) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("compact-channel-var-undef")) );
		assertEquals( 5, e.getLineNumber() );
		assertEquals( "$y: c:/4", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_VAR_NOT_DEFINED) + "$y") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("compact-unknown-pattern")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "0: c c c/d/e:pat_none c c", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_NOTE_LENGTH_INVALID) + "pat_none") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("compact-invalid-option")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "0: c c (v=127,l=text,d=50%) c (s=1) c", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_COMPACT_INVALID_OPTION), "shift", "(s=1)")));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("compact-unknown-option")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "0: c c (v=127,l=text,d=50%) c (unk=1) c", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_UNKNOWN_OPTION) + "unk") );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("compact-pattern-call-with-options")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "0: c d e:8 f/e/d/c:pat(foo,bar)q=2,m a b c", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_COMPACT_PAT_CALL_WITH_OPT), "q=2,m", "f/e/d/c:pat(foo,bar)q=2,m")));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("compact-pattern-call-with-whitespace")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "0: c d e:8 f/e/d/c:pat(foo, bar) a b c", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_COMPACT_PAT_CALL_WITH_OPT), "(foo,", "f/e/d/c:pat(foo,")));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("compact-pattern-with-wrong-index")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( ": 0 1 a", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_PATTERN_INDEX_INVALID)), "a");
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("bar-line-too-early-1")) );
		assertEquals( 9, e.getLineNumber() );
		assertEquals( "0: | c   | c:16 c c c  c c c c  c c c c  c c c c:32 |", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_BAR_LINE_INCORRECT)));
		assertTrue( e.getMessage().contains(String.format(Dict.get(Dict.ERROR_BAR_LINE_TOO_EARLY), 3, 60)));
		assertTrue( e.getMessage().contains(String.format(Dict.get(Dict.ERROR_BAR_LINE_EXACT_NOTE_LEN), "/32")));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("bar-line-too-early-2")) );
		assertEquals( 9, e.getLineNumber() );
		assertEquals( "0: | c   | c:16 c c c  c c c c  c c c c  c c c c:32. |", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_BAR_LINE_INCORRECT)));
		assertTrue( e.getMessage().contains(String.format(Dict.get(Dict.ERROR_BAR_LINE_TOO_EARLY), 3, 30)));
		assertTrue( e.getMessage().contains(String.format(Dict.get(Dict.ERROR_BAR_LINE_SMALL), "/32", 60)));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("bar-line-too-late-1")) );
		assertEquals( 9, e.getLineNumber() );
		assertEquals( "0: | c   | c:16 c c c  c c c c  c c c c  c c c c  c:32 |", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_BAR_LINE_INCORRECT)));
		assertTrue( e.getMessage().contains(String.format(Dict.get(Dict.ERROR_BAR_LINE_TOO_LATE), 3, 60)));
		assertTrue( e.getMessage().contains(String.format(Dict.get(Dict.ERROR_BAR_LINE_EXACT_NOTE_LEN), "/32")));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("bar-line-too-late-2")) );
		assertEquals( 9, e.getLineNumber() );
		assertEquals( "0: | c   | c:16 c c c  c c c c  c c c c  c c c c  c:32.. |", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_BAR_LINE_INCORRECT)));
		assertTrue( e.getMessage().contains(String.format(Dict.get(Dict.ERROR_BAR_LINE_TOO_LATE), 3, 105)));
		assertTrue( e.getMessage().contains(String.format(Dict.get(Dict.ERROR_BAR_LINE_BETWEEN), "/32.", 90, "/16", 120)));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("oto-before-block")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "{", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_OTO_BEFORE_BLOCK), "quantity", 1)));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("oto-before-block-2")) );
		assertEquals( 6, e.getLineNumber() );
		assertEquals( "{", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_OTO_BEFORE_BLOCK), "multiple", 1)));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("oto-at-end-of-block")) );
		assertEquals( 8, e.getLineNumber() );
		assertEquals( "}", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_OTO_AT_END_OF_BLOCK), "multiple", 1)));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("oto-at-end-of-block-2")) );
		assertEquals( 10, e.getLineNumber() );
		assertEquals( "}", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_OTO_AT_END_OF_BLOCK), "quantity", 1)));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("oto-before-function-call")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "CALL f", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_OTO_BEFORE_FUNCTION), "quantity", 1)));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("oto-before-function-call-2")) );
		assertEquals( 7, e.getLineNumber() );
		assertEquals( "CALL f", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_OTO_BEFORE_FUNCTION), "quantity", 1)));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("oto-before-function-call-3")) );
		assertEquals( 10, e.getLineNumber() );
		assertEquals( "CALL f", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_OTO_BEFORE_FUNCTION), "quantity", 1)));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("oto-at-end-of-function")) );
		assertEquals( 9, e.getLineNumber() );
		assertEquals( "END", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_OTO_AT_END_OF_FUNCTION), "quantity", 1)));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("oto-tremolo-with-pattern")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "1: c (tr=/32) d:pat", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_OTO_TREMOLO_PATTERN_CALL)));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("oto-duplicate-option")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "1: c (m,q=2,m) d", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_DUPLICATE_OPTION) + "multiple"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("oto-duplicate-option-m")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "1: c (m,q=2) (m) d", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_OTO_DUPLICATE_MULTIPLE)));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("oto-duplicate-option-q")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "1: (q=3) d", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_OTO_DUPLICATE_QUANTITY)));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("oto-duplicate-option-tr")) );
		assertEquals( 5, e.getLineNumber() );
		assertEquals( "1: (tr=/16) d", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_OTO_DUPLICATE_TREMOLO)));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-unknown-flow-elem-1")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: volll.keep.set(50)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_UNKNOWN_NOTE)));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-unknown-flow-elem-2")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: vol.keeeeeep.set(50)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FL_UNKNOWN_ELEMENT)));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-flow-broken-by-var")) );
		assertEquals( 6, e.getLineNumber() );
		assertEquals( "0: .wait.set(50)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_FL_NOT_OPEN), ".")));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-flow-broken-by-const")) );
		assertEquals( 6, e.getLineNumber() );
		assertEquals( "0: .wait.set(50)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_FL_NOT_OPEN), ".")));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-flow-broken-by-call")) );
		assertEquals( 6, e.getLineNumber() );
		assertEquals( "0: .wait.set(50)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_FL_NOT_OPEN), ".")));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-flow-broken-by-function")) );
		assertEquals( 7, e.getLineNumber() );
		assertEquals( "0: .wait.set(50)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_FL_NOT_OPEN), ".")));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-flow-broken-by-note")) );
		assertEquals( 6, e.getLineNumber() );
		assertEquals( "0: .wait.set(50)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_FL_NOT_OPEN), ".")));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-flow-broken-by-other-channel")) );
		assertEquals( 6, e.getLineNumber() );
		assertEquals( "0: .wait.set(50)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_FL_NOT_OPEN), ".")));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-flow-missing-dot-1")) );
		assertEquals( 5, e.getLineNumber() );
		assertEquals( "0: wait().set(50)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FL_EFF_NOT_SET) + "set"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-flow-missing-dot-2")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: vol.wait()set(50)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_FL_MISSING_DOT), ".")));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-flow-non-generic-with-num")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: vol=30.set(50)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FL_NUMBER_NOT_ALLOWED) + "vol"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-flow-rpn-without-num")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: rpn.set(50)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_FL_NUMBER_MISSING), "rpn")));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-flow-rpn-without-num")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: rpn.set(50)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_FL_NUMBER_MISSING), "rpn")));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-flow-nrpn-num-too-high")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: nrpn=999999999999999.set(50)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_FL_NUMBER_TOO_HIGH), "999999999999999", "nrpn", 16383)));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-flow-ctrl-num-too-high")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: ctrl=128.set(50)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_FL_NUMBER_TOO_HIGH), 128, "ctrl", 127)));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-flow-ctrl-with-lsb")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: ctrl=0/11.set(50)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FL_NUM_SEP_NOT_ALLOWED) + "ctrl"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-flow-double-with-params")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: vol.double().set(50)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_FL_PARAMS_NOT_ALLOWED), "double")));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-flow-double-for-boolean")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: hold.double.on()", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_FL_DOUBLE_NOT_SUPPORTED), "double")));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-flow-double-for-single")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: chorus.double.set(50)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_FL_DOUBLE_NOT_SUPPORTED), "double")));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-flow-double-for-coarse-tune")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: coarse_tune.double.set(+50)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_FL_DOUBLE_NOT_SUPPORTED), "double")));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-tune-coarse-invalid-1")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: coarse_tune.set(-65)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_FUNC_VAL_LOWER_MIN), -65, -64)));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-tune-coarse-invalid-2")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: coarse_tune.set(+64)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_FUNC_VAL_GREATER_MAX), "+64", "+63")));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-tune-coarse-invalid-3")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: coarse_tune.set(+0%)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_FUNC_PERCENT_FORBIDDEN), "+0%")));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-tune-coarse-invalid-4")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: coarse_tune.set(+3.5)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_FUNC_BROKEN_HALFTONE), "+3.5")));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-tune-fine-invalid-1")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: fine_tune.set(-1.0001)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_FUNC_VAL_LOWER_MIN), -1.0001f, -1.0f)));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-tune-fine-invalid-2")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: fine_tune.set(+1.0001)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_FUNC_VAL_GREATER_MAX), "+1.0001", "+1.0")));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-tune-fine-invalid-3")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: fine_tune.set(-101%)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_FUNC_VAL_LOWER_MIN), "-101%", "-100%")));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-tune-fine-invalid-4")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: fine_tune.set(+101%)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_FUNC_VAL_GREATER_MAX), "+101%", "+100%")));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-tune-fine-invalid-5")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: fine_tune.double.set(+2)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_FUNC_VAL_GREATER_MAX), "+2", "+1.0")));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-without-params")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: vol.set", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_FL_PARAMS_REQUIRED), "set")));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-wrong-param-count-1")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: vol.set(30,40)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_FL_WRONG_PARAM_NUM), "set", 1, 2, "30,40")));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-wrong-param-count-2")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: vol.wait(4,8)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_FL_WRONG_PARAM_NUM), "wait", 1, 2, "4,8")));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-wrong-param-count-3")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: vol.set()", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_FL_WRONG_PARAM_NUM), "set", 1, 0, "")));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-flow-remainder-1")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: vol.wait().wait().set(50).test", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FL_UNKNOWN_ELEMENT) + "test"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-flow-remainder-2")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: vol.wait().wait().set(50).wait-for-me", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FL_UNMATCHED_REMAINDER) + "-for-me"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-empty-param")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: vol.sin(0,,100%)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_FL_EMPTY_PARAM), "0,,100%")));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-poly-mode-off")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: poly_mode.off()", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNC_NOT_SUPPORTED_BY_EFF) + "off"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-poly-mode-set-1")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: poly_mode.set(0)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNC_NOT_SUPPORTED_BY_EFF) + "set"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-poly-mode-set-2")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: poly_mode.set(+0)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNC_NOT_SUPPORTED_BY_EFF) + "set"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-mono-mode-off")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: mono_mode.off()", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNC_NOT_SUPPORTED_BY_EFF) + "off"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-mono-mode-set-17")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: mono_mode.set(17)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_FUNC_VAL_GREATER_MAX), 17, 16)));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-mono-mode-set-percent")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: mono_mode.set(50%)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNC_PERCENT_FORBIDDEN) + "50%"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-mono-mode-line")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: mono_mode.line(0,16)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNC_NOT_SUPPORTED_BY_EFF) + "line"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-bool-with-numeric-1")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: vol.on()", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNC_NOT_SUPPORTED_BY_EFF) + "on"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-bool-with-numeric-2")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: chorus.off()", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNC_NOT_SUPPORTED_BY_EFF) + "off"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-numeric-for-bool-1")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: legato.set(0)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNC_NOT_SUPPORTED_BY_EFF) + "set"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-numeric-for-bool-2")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: legato.set(+0)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNC_NOT_SUPPORTED_BY_EFF) + "set"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-cont-rpn")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: pitch_range.line(1,12)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNC_NOT_SUPPORTED_BY_EFF) + "line"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-cont-nrpn")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: nrpn=123.line(1,12)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNC_NOT_SUPPORTED_BY_EFF) + "line"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-param-invalid-1")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: vol.line(1,9999999999999)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNC_NO_NUMBER) + "9999999999999"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-param-invalid-2")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: vol.line(1,0x7F)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNC_NO_NUMBER) + "0x7F"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-param-too-low-1")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: balance.line(+63,-65)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_FUNC_VAL_LOWER_MIN), -65, -64)));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-param-too-low-2")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: vol.line(1,-0.000001%)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNC_SIGNED_FORBIDDEN) + "-0.000001%"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-param-too-low-3")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: balance.line(+1,-101%)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_FUNC_VAL_LOWER_MIN), "-101%", "-100%")));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-param-too-high-1")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: vol.line(1,128)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_FUNC_VAL_GREATER_MAX), 128, 127)));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-param-too-high-2")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: balance.double.line(+1,+8192)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_FUNC_VAL_GREATER_MAX), "+8192", "+8191")));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-param-periods-nan-1")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: vol.sin(0,100%,1.2.3)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNC_PERIODS_NO_NUMBER) + "1.2.3"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-param-periods-nan-2")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: vol.sin(0,100%,999999999999999999999999999999999999999.0)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNC_PERIODS_NO_NUMBER) + "999999999999999999999999999999999999999.0"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-param-periods-signed-1")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: vol.sin(0,100%,-1.0)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNC_PERIODS_SIGNED) + "-1.0"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-param-periods-signed-2")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: vol.sin(0,100%,+1.0)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNC_PERIODS_SIGNED) + "+1.0"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-param-periods-signed-2")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: vol.sin(0,100%,+1.0)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNC_PERIODS_SIGNED) + "+1.0"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-param-periods-signed-3")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: vol.sin(0,100%,+10%)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNC_PERIODS_SIGNED) + "+10%"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-param-periods-zero")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: vol.sin(0,100%,0)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNC_PERIODS_NOT_POS) + "0"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-flow-eff-not-set-1")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: wait().set(50)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FL_EFF_NOT_SET) + "set"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-flow-eff-not-set-2")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: wait().double.vol.set(50)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FL_EFF_NOT_SET) + "double"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-flow-eff-already-set")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: vol.wait().vol.set(50)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FL_EFF_ALREADY_SET)));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-halftone-for-vol-1")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: vol.set(12.0)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_FUNC_HALFTONE_NOT_ALLOWED), "12.0")));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-halftone-for-vol-2")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: vol.set(+12.0)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_FUNC_SIGNED_FORBIDDEN), "+12.0")));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-pbr-halftone-gt-max-1")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: pitch_range.set(129.0)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_FUNC_VAL_GREATER_MAX), 129.0f, 127f)));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-pbr-halftone-gt-max-2")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: pitch_range.double.set(127.997)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_FUNC_VAL_GREATER_MAX), 127.997f, 127.99f)));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-pbr-halftone-gt-max-3")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: pitch_range.set(127.0001)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_FUNC_VAL_GREATER_MAX), 127.0001f, 127f)));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-pbr-halftone-gt-max-4")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: pitch_range.double.set(127.997)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_FUNC_VAL_GREATER_MAX), 127.997, 127.99f)));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-pbr-with-percent")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: pitch_range.set(12.0%)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNC_PERCENT_FORBIDDEN) + "12.0%"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-pitch-gt-range-1")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: pitch.wait.set(+2.3)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_FUNC_HALFTONE_GT_RANGE), "+2.3", "2.0")));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-pitch-gt-range-2")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: pitch.wait.set(-2.3)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_FUNC_HALFTONE_GT_RANGE), "-2.3", "2.0")));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-flow-note-invalid")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: poly_at.note(c+6).set(123)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_UNKNOWN_NOTE) + "c+6"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-flow-note-without-effect")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: note(c).vol.set(100%)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FL_EFF_NOT_SET) + "note"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-flow-note-not-allowed-1")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: vol.note(c).set(100%)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNC_NOT_SUPPORTED_BY_EFF) + "note"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-flow-note-not-allowed-2")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: mono_at.note(c).set(100%)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNC_NOT_SUPPORTED_BY_EFF) + "note"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-flow-note-not-allowed-3")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: pitch_range.note(c).set(2.0)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNC_NOT_SUPPORTED_BY_EFF) + "note"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-flow-note-not-set-1")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: poly_at.set(100%)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FL_NOTE_NOT_SET) + "set"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-flow-note-not-set-2")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: port_ctrl.on()", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FL_NOTE_NOT_SET) + "on"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-flow-numeric-for-none")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: ctrl=123.wait.set(12)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNC_NOT_SUPPORTED_BY_EFF) + "set"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-flow-off-for-none")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: ctrl=123.wait.off()", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNC_NOT_SUPPORTED_BY_EFF) + "off"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-msblsb-without-double")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: vol.set(12/30)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_FUNC_MSB_LSB_NEEDS_DOUBLE), "12/30", "double")));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-msb-too-high")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: vol.double.set(128/30)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_FUNC_MSB_TOO_HIGH), "128/30", "128")));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-lsb-too-high")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: vol.double.set(30/128)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_FUNC_LSB_TOO_HIGH), "30/128", "128")));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-pattern-index-invalid-1")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( ": poly_at.note(3)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_FL_NOTE_PAT_IDX_TOO_HIGH), "3", ".note(3)")));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-pattern-index-invalid-2")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( ": poly_at.note(c)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_FL_NOTE_PAT_IDX_NAN), "c", ".note(c)")));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-pattern-index-invalid-3")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( ": poly_at.note(3)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_FL_NOTE_PAT_IDX_TOO_HIGH), "3", ".note(3)")));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-pattern-index-invalid-4")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( ": poly_at.note(3)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_FL_NOTE_PAT_IDX_TOO_HIGH), "3", ".note(3)")));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-pattern-index-invalid-5")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( ": poly_at.note(3)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_FL_NOTE_PAT_IDX_TOO_HIGH), "3", ".note(3)")));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-cd-note")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: ctrl_dest.note(c)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNC_NOT_SUPPORTED_BY_EFF) + "note"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-cd-src-missing-1")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0:   .on()", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNC_CD_SRC_NOT_SET) + "on"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-cd-src-missing-2")) );
		assertEquals( 5, e.getLineNumber() );
		assertEquals( "0:   .on()", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNC_CD_SRC_NOT_SET) + "on"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-cd-src-unknown-1")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0:   .src(voll)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNC_CD_SRC_CTRL_UNKNOWN) + "voll"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-cd-src-unknown-2")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0:   .src(ctrl=128)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNC_CD_SRC_CTRL_NOT_SUPP) + "ctrl=128"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-cd-src-unknown-3")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0:   .src(ctrl=5=7)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNC_CD_SRC_CTRL_UNKNOWN) + "ctrl=5=7"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-cd-src-unknown-4")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0:   .src(ctrl=9999999999999999999999999999)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNC_CD_SRC_CTRL_UNKNOWN) + "ctrl=9999999999999999999999999999"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-cd-src-not-sup-1")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0:   .src(ctrl=0)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNC_CD_SRC_CTRL_NOT_SUPP) + "ctrl=0"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-cd-src-not-sup-2")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0:   .src(ctrl=32)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNC_CD_SRC_CTRL_NOT_SUPP) + "ctrl=32"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-cd-src-not-sup-3")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0:   .src(ctrl=57)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNC_CD_SRC_CTRL_NOT_SUPP) + "ctrl=57"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-cd-src-not-sup-4")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0:   .src(ctrl=60)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNC_CD_SRC_CTRL_NOT_SUPP) + "ctrl=60"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-cd-src-not-sup-5")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0:   .src(mono_mode)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNC_CD_SRC_CTRL_NOT_SUPP) + "mono_mode"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-cd-src-duplicate")) );
		assertEquals( 6, e.getLineNumber() );
		assertEquals( "0:   .src(poly_at)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNC_CD_SRC_ALREADY_SET) + "src"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-cd-dest-missing")) );
		assertEquals( 6, e.getLineNumber() );
		assertEquals( "0:   .on()", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNC_CD_DEST_NOT_SET) + "on"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-cd-dest-unknown-1")) );
		assertEquals( 6, e.getLineNumber() );
		assertEquals( "0:   .dest(voll,10%)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNC_CD_DEST_UNKNOWN) + "voll"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-cd-dest-unknown-2")) );
		assertEquals( 6, e.getLineNumber() );
		assertEquals( "0:   .dest(ctrl=11,10%)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNC_CD_DEST_UNKNOWN) + "ctrl=11"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-signed-vol")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: vol.set(+50)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNC_SIGNED_FORBIDDEN) + "+50"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-unsigned-balance")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: balance.set(50)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNC_SIGNED_REQUIRED) + "50"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-func-mono-mode-set-signed")) );
		assertEquals( 4, e.getLineNumber() );
		assertEquals( "0: mono_mode.set(+5)", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FUNC_SIGNED_FORBIDDEN) + "+5"));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-flow-pending-1")) );
		assertEquals( 5, e.getLineNumber() );
		assertEquals( "1: c", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FL_PENDING)));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-flow-pending-2")) );
		assertEquals( 5, e.getLineNumber() );
		assertEquals( "0: hold.wait", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FL_PENDING)));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-flow-pending-3")) );
		assertEquals( 6, e.getLineNumber() );
		assertEquals( "", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FL_PENDING)));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("eff-flow-pending-4")) );
		assertEquals( 23, e.getLineNumber() );
		assertEquals( "0: c", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_FL_PENDING)));
	}
	
	/**
	 * Returns a source file for testing the parse() method with a
	 * file that is supposed to be parsable.
	 * 
	 * @param name file name without the extension .midicapl
	 * @return file object of the file to be tested
	 */
	private static File getWorkingFile(String name) {
		String sourceDir = TestUtil.getTestfileDirectory() + "working" + File.separator;
		File file = new File(sourceDir + name + ".midica");
		
		return file;
	}
	
	/**
	 * Returns a source file for testing the parse() method with a
	 * file that is supposed to be parsable.
	 * 
	 * @param name file name without the extension .midicapl
	 * @return file object of the file to be tested
	 */
	private static File getFailingFile(String name) {
		String sourceDir = TestUtil.getTestfileDirectory() + "failing" + File.separator;
		File file = new File(sourceDir + name + ".midica");
		
		return file;
	}
	
	/**
	 * Returns the text of the requested message, assuming that it is a meta message.
	 * 
	 * @param track    Track index, beginning with 0.
	 * @param i        Message index inside of the track.
	 * @return         Text of the message.
	 */
	private static String getMetaMsgText(int track, int i) {
		Sequence  seq = SequenceCreator.getSequence();
		
		MidiMessage msg  = seq.getTracks()[track].get(i).getMessage();
		byte[]      data = ((MetaMessage) msg).getData();
		String      text = CharsetUtils.getTextFromBytes(data, "UTF-8", null);
		
		return text;
	}
	
	/**
	 * Returns the message list, filtered by the given channel.
	 * 
	 * @param channel  MIDI channel
	 * @return all messages with the given channel.
	 */
	private static ArrayList<SingleMessage> getMessagesByChannel(int channel) {
		ArrayList<SingleMessage> allMessages = (ArrayList<SingleMessage>) SequenceAnalyzer.getMessages();
		ArrayList<SingleMessage> messages    = new ArrayList<>();
		for (SingleMessage msg : allMessages) {
			Integer ch = (Integer) msg.getOption(IMessageType.OPT_CHANNEL);
			if (ch != null && ch == channel)
				messages.add(msg);
		}
		
		return messages;
	}
	
	/**
	 * Returns the message list, filtered by the given status byte.
	 * 
	 * @param statusByte  status byte (first byte of the message)
	 * @return the filtered messages.
	 */
	private static ArrayList<SingleMessage> getMessagesByStatus(String statusByte) {
		ArrayList<SingleMessage> allMessages = (ArrayList<SingleMessage>) SequenceAnalyzer.getMessages();
		ArrayList<SingleMessage> messages    = new ArrayList<>();
		for (SingleMessage msg : allMessages) {
			String status = (String) msg.getOption(IMessageType.OPT_STATUS_BYTE);
			if (status.equals(statusByte))
				messages.add(msg);
		}
		
		return messages;
	}
	
	/**
	 * Returns the message list, filtered by the given status byte, and tick range, and summary status.
	 * 
	 * @param statusByte  status byte (first byte of the message);
	 *                    **null** if the status doesn't matter
	 * @param minTick     minimum tick to be included;
	 *                    **null** if the minimum tick doesn't matter
	 * @param maxTick     maximum tick to be included;
	 *                    **null** if the maximum tick doesn't matter
	 * @param summary     **true** for messages **with** summary;
	 *                    **false** messages **without** summary;
	 *                    **null** if the summary doesn't matter.
	 * @return the filtered messages.
	 */
	private static ArrayList<SingleMessage> getMessagesByStatusAndTickRangeAndSummary(String statusByte, Long minTick, Long maxTick, Boolean summary) {
		ArrayList<SingleMessage> allMessages = (ArrayList<SingleMessage>) SequenceAnalyzer.getMessages();
		ArrayList<SingleMessage> messages    = new ArrayList<>();
		for (SingleMessage msg : allMessages) {
			
			// filter status
			if (statusByte != null) {
				String status = (String) msg.getOption(IMessageType.OPT_STATUS_BYTE);
				if (! status.equals(statusByte))
					continue;
			}
			
			// filter min tick
			if (minTick != null || maxTick != null) {
				Long tick = (Long) msg.getOption(IMessageType.OPT_TICK);
				if (minTick != null && tick < minTick)
					continue;
				if (maxTick != null && tick > maxTick)
					continue;
			}
			
			// filter summary
			if (summary != null) {
				String msgSummary = (String) msg.getOption(IMessageType.OPT_SUMMARY);
				if (summary && null == msgSummary)
					continue;
				if (! summary && msgSummary != null)
					continue;
			}
			
			// not yet filtered - add the message
			messages.add(msg);
		}
		
		return messages;
	}
	
	/**
	 * Returns only NOTE-ON and NOTE-OFF messages from the message list, filtered by channel.
	 * 
	 * @param channel  MIDI channel
	 * @return the filtered messages.
	 */
	private static ArrayList<SingleMessage> getNoteOnOffMessagesByChannel(int channel) {
		ArrayList<SingleMessage> allMessages = (ArrayList<SingleMessage>) SequenceAnalyzer.getMessages();
		ArrayList<SingleMessage> messages    = new ArrayList<>();
		
		// filter
		for (SingleMessage msg : allMessages) {
			Integer ch     = (Integer) msg.getOption(IMessageType.OPT_CHANNEL);
			String  status = (String)  msg.getOption(IMessageType.OPT_STATUS_BYTE);
			if (ch != null && ch == channel) {
				if (status.startsWith("8") || status.startsWith("9")) {
					messages.add(msg);
				}
			}
		}
		
		return messages;
	}
	
	/**
	 * Returns the full lyrics of the sequence.
	 * 
	 * @return full lyrics.
	 */
	private static String getLyrics() {
		return (String) KaraokeAnalyzer.getKaraokeInfo().get("lyrics_full");
	}
	
	/**
	 * Calculates and returns a summary of a short message without (without tickstamp).
	 * 
	 * The summary consists of:
	 * 
	 * - Status byte in hex
	 * - first data byte in hex (if available)
	 * - second data byte in hex (if available)
	 * 
	 * All bytes are separated by slash (/).
	 * 
	 * @param msg  the message
	 * @return the summary.
	 */
	private static String getShortMsgBytesAsStr(SingleMessage msg) {
		
		// get MSB and LSB
		byte[] bytes = msg.getMessageBytes();
		String result = String.format("%02X", bytes[0]);
		if (bytes.length > 1)
			result += "/" + String.format("%02X", bytes[1]);
		if (bytes.length > 2)
			result += "/" + String.format("%02X", bytes[2]);
		
		return result;
	}
	
	/**
	 * Calculates and returns a summary from a pitch bend message.
	 * 
	 * The summary consists of:
	 * 
	 * - MSB
	 * - LSB
	 * - resulting pitch bend in half tones (rounded)
	 * 
	 * @param msg            the message
	 * @param decimalPlaces  number of decimal places to round the half tones
	 * @return the summary.
	 */
	private static String getPitchBendStr(SingleMessage msg, int decimalPlaces) {
		
		// format half tones
		String summary   = msg.getDistinctOptions(IMessageType.OPT_SUMMARY);
		float  halfTones = Float.parseFloat(summary);
		String formatted = String.format("%." + decimalPlaces + "f", halfTones);
		
		// get MSB and LSB
		byte[] bytes = msg.getMessageBytes();
		String msb   = String.format("%02X", bytes[2]);
		String lsb   = String.format("%02X", bytes[1]);
		
		return msb + "/" + lsb + "/" + formatted;
	}
	
	/**
	 * Calculates and returns a summary from a portamento control message.
	 * 
	 * Throws an exception if the message is not a portamento control message.
	 * 
	 * The summary consists of:
	 * 
	 * - channel
	 * - note name
	 * 
	 * @param msg  the message
	 * @return the summary.
	 */
	private static String getPortCtrlStr(SingleMessage msg) {
		
		String note = msg.getDistinctOptions(IMessageType.OPT_SUMMARY);
		
		byte[] bytes   = msg.getMessageBytes();
		String status  = String.format("%02X", bytes[0]);
		int    channel = bytes[0] & 0x0F;
		String ctrl    = String.format("%02X", bytes[1]);
		
		// is it a portamento ctrl?
		if (!status.startsWith("B"))
			throw new RuntimeException("not a ctrl change. status byte: 0x" + status);
		if (!ctrl.startsWith("54"))
			throw new RuntimeException("not a portamento ctrl message. ctrl: 0x" + ctrl);
		
		return channel + "/" + note;
	}
	
	/**
	 * Calculates and returns a summary from a controller destination message.
	 * 
	 * Throws an exception is the message is not a controller destination message.
	 * 
	 * The summary consists of:
	 * 
	 * - channel
	 * - source
	 * - list of destinations and their range, separated by comma, ordered alphabetically
	 * 
	 * @param msg  the message
	 * @return the summary.
	 */
	private static String getCtrlDestStr(SingleMessage msg) {
		
		// handle first 5 bytes
		byte[] bytes = msg.getMessageBytes();
		if (bytes.length < 8)
			throw new RuntimeException("message length too short for controller destination: " + bytes.length);
		if ((bytes[0] & 0xFF) != 0xF0)
			throw new RuntimeException("Not a sysex message. Wrong status: " + bytes[0]);
		if (bytes[1] != 0x7F)
			throw new RuntimeException("Wrong manufacturer byte: " + bytes[1]);
		if (bytes[2] != 0x7F)
			throw new RuntimeException("Wrong device ID: " + bytes[2]);
		if (bytes[3] != 0x09)
			throw new RuntimeException("Sub ID 1: " + bytes[3]);
		byte srcType = bytes[4];
		byte channel = bytes[5];
		if (srcType < 1 || srcType > 3)
			throw new RuntimeException("Sub ID 2: " + srcType);
		
		// handle source (1 or 2 bytes)
		int i = 6;
		String src = null;
		if (0x01 == srcType)
			src = "mono_at";
		else if (0x02 == srcType)
			src = "poly_at";
		else if (0x03 == srcType)
			src = "ctrl:" + String.format("%02X", bytes[i++]);
		
		// handle destinations
		TreeSet<String> destinations = new TreeSet<>();
		while (i < bytes.length - 1) {
			String dest  = String.format("%02X", bytes[i++]);
			String range = String.format("%02X", bytes[i++]);
			destinations.add(dest + ":" + range);
		}
		
		// handle last byte
		if (i != bytes.length - 1)
			throw new RuntimeException("Odd number of controller destinations");
		int endByte = bytes[i++] & 0xFF;
		if (endByte != 0xF7)
			throw new RuntimeException("Wrong end byte for sysex message: " + bytes[0]);
		
		// put everything together
		return channel + "/" + src + "==>" + String.join(",", destinations);
	}
}
