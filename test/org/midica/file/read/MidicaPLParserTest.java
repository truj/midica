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
			assertEquals( "0/0/90/c / 64",        messages.get(i++).toString() ); // bbb,c##,dbb = a,d,c ==> c,d,a
			assertEquals( "0/0/90/d / 64",        messages.get(i++).toString() );
			assertEquals( "0/0/90/a / 64",        messages.get(i++).toString() );
			assertEquals( "480/0/90/c / 64",      messages.get(i++).toString() ); // bbb,c##,dbb = a,d,c ==> c,d,a
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
			assertEquals( "4320/0/90/c / 64",     messages.get(i++).toString() ); // bbb,c##,dbb = a,d,c ==> c,d,a
			assertEquals( "4320/0/90/d / 64",     messages.get(i++).toString() );
			assertEquals( "4320/0/90/a / 64",     messages.get(i++).toString() );
			assertEquals( "4800/0/90/c / 64",     messages.get(i++).toString() ); // bbb,c##,dbb = a,d,c ==> c,d,a
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
			assertEquals( "8640/0/90/c / 64",     messages.get(i++).toString() ); // bbb,c##,dbb = a,d,c ==> c,d,a
			assertEquals( "8640/0/90/d / 64",     messages.get(i++).toString() );
			assertEquals( "8640/0/90/a / 64",     messages.get(i++).toString() );
			assertEquals( "9120/0/90/c / 64",     messages.get(i++).toString() ); // bbb,c##,dbb = a,d,c ==> c,d,a
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
			"abc... abc... abc... abc... abc... abc... xyz3rd xyz3rd xyz3rd xyz3rd xyz3rd xyz3rd xyz3rd xyz3rd xyz3rd xyz3rd xyz3rd xyz3rd xyz3rd xyz3rd xyz3rd xyz3rd xyz3rd xyz3rd abc... abc... abc... abc... abc... abc... bbb?? bbb?? bbb?? bbb?? bbb?? bbb?? ",
			getLyrics()
		);
		messages = getMessagesByStatus("91");                           // channel 1
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
			assertEquals( "0/1/91/c / 102",    messages.get(i++).toString() ); // c,d,e : c ON
			assertEquals( "0/1/91/d / 102",    messages.get(i++).toString() ); // c,d,e : d ON
			assertEquals( "0/1/91/e / 102",    messages.get(i++).toString() ); // c,d,e : e ON
			assertEquals( "480/1/91/f / 102",  messages.get(i++).toString() ); // f     : f ON
			assertEquals( "959/1/81/d / 0",    messages.get(i++).toString() ); // c,d,e : d OFF (correction)
			assertEquals( "960/1/91/d / 102",  messages.get(i++).toString() ); // g,d,a : d ON
			assertEquals( "960/1/91/g / 102",  messages.get(i++).toString() ); // g,d,a : g ON
			assertEquals( "960/1/91/a / 102",  messages.get(i++).toString() ); // g,d,a : a ON
			assertEquals( "2400/1/81/c / 0",   messages.get(i++).toString() ); // c,d,e : c OFF
			assertEquals( "2400/1/81/e / 0",   messages.get(i++).toString() ); // c,d,e : e OFF
			assertEquals( "2880/1/81/f / 0",   messages.get(i++).toString() ); // f     : f OFF
			assertEquals( "3360/1/81/d / 0",   messages.get(i++).toString() ); // g,d,a : d OFF
			assertEquals( "3360/1/81/g / 0",   messages.get(i++).toString() ); // g,d,a : g OFF
			assertEquals( "3360/1/81/a / 0",   messages.get(i++).toString() ); // g,d,a : a OFF
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
			// 0: crd2:pat_488 (crd2 = c+,d+)
			assertEquals( "960/0/90/c+ / 64",   messages.get(i++).toString() );
			assertEquals( "960/0/90/d+ / 64",   messages.get(i++).toString() );
			assertEquals( "1440/0/90/c+ / 64",  messages.get(i++).toString() );
			assertEquals( "1440/0/90/d+ / 64",  messages.get(i++).toString() );
			assertEquals( "1680/0/90/c+ / 64",  messages.get(i++).toString() );
			assertEquals( "1680/0/90/d+ / 64",  messages.get(i++).toString() );
			// 0  crd3 pat_488 (crd3 = c+2,d+2,e+2)
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
			// 0: c+,d+:pat_488
			assertEquals( "3840/0/90/c+ / 64",  messages.get(i++).toString() );
			assertEquals( "3840/0/90/d+ / 64",  messages.get(i++).toString() );
			assertEquals( "4320/0/90/c+ / 64",  messages.get(i++).toString() );
			assertEquals( "4320/0/90/d+ / 64",  messages.get(i++).toString() );
			assertEquals( "4560/0/90/c+ / 64",  messages.get(i++).toString() );
			assertEquals( "4560/0/90/d+ / 64",  messages.get(i++).toString() );
			// 0: c+2,d+2,e+2:pat_488
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
			// 2: c+,d+:pat_order_x2_up
			assertEquals( "0/2/92/c+ / 64",      messages.get(i++).toString() );
			assertEquals( "480/2/92/d+ / 64",   messages.get(i++).toString() );
			assertEquals( "960/2/92/c+ / 64",   messages.get(i++).toString() );
			assertEquals( "1440/2/92/d+ / 64",   messages.get(i++).toString() );
			// 2: c+,d+:pat_order_x2_down
			assertEquals( "1920/2/92/d+ / 64",   messages.get(i++).toString() );
			assertEquals( "2400/2/92/c+ / 64",   messages.get(i++).toString() );
			assertEquals( "2880/2/92/d+ / 64",   messages.get(i++).toString() );
			assertEquals( "3360/2/92/c+ / 64",   messages.get(i++).toString() );
			// 2  c+2,d+2,e+2  pat_order_x2_up
			assertEquals( "3840/2/92/c+2 / 64",   messages.get(i++).toString() );
			assertEquals( "4320/2/92/d+2 / 64",   messages.get(i++).toString() );
			assertEquals( "4800/2/92/e+2 / 64",   messages.get(i++).toString() );
			assertEquals( "5280/2/92/c+2 / 64",   messages.get(i++).toString() );
			assertEquals( "5760/2/92/d+2 / 64",   messages.get(i++).toString() );
			assertEquals( "6240/2/92/e+2 / 64",   messages.get(i++).toString() );
			// 2  c+2,d+2,e+2  pat_order_x2_down
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
			// 3: c,d,e:pat_outer
			assertEquals( "0/3/93/e / 64",      messages.get(i++).toString() ); // outer
			assertEquals( "480/3/93/c / 64",    messages.get(i++).toString() ); // inner
			assertEquals( "480/3/93/e / 64",    messages.get(i++).toString() ); // inner
			assertEquals( "960/3/93/e / 64",    messages.get(i++).toString() ); // outer
			assertEquals( "1440/3/93/c / 64",   messages.get(i++).toString() ); // outer
			assertEquals( "1440/3/93/d / 64",   messages.get(i++).toString() ); // outer
			assertEquals( "1440/3/93/e / 64",   messages.get(i++).toString() ); // outer
			// 3  c+,d+,e+ pat_outer
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
			// p  slap,cla pat_488
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
			
			// 0  d,e,f  pat
			assertEquals( "3840/0/90/d / 20",  messages.get(i++).toString() );
			assertEquals( "4320/0/90/e / 30",  messages.get(i++).toString() );
			assertEquals( "4800/0/90/f / 40",  messages.get(i++).toString() );
			
			// 0  d,e,f  pat  v=110
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
		assertEquals("http://midica.org/assets/sound/soundbank-emg.sf2", SoundbankParser.getFullPath());
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
			
			// root level: 0: a b,c+,d+,e+:/1 - f+:/8 g+:4
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
				
				// 0: c d,e - f
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
					
					// 0: c d,e - f
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
				// c,d,e:pat_outer(70)
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
				// e,d,c:pat_outer(60)
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
				assertEquals( "3840/1/91/d / 64",  messages.get(i++).toString() );
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
			// (m,q=2) d,e:pat (first run) f:*2
				// : (q=2) 0:4
				assertEquals( "480/2/92/d / 64",  messages.get(i++).toString() );
				assertEquals( "480/2/92/f / 64",  messages.get(i++).toString() );
				assertEquals( "960/2/92/d / 64",  messages.get(i++).toString() );
				// : (m) 0 1
				assertEquals( "1440/2/92/d / 64",  messages.get(i++).toString() );
				assertEquals( "1440/2/92/e / 64",  messages.get(i++).toString() );
				// : (tr=32) 0,1:16
				assertEquals( "1920/2/92/d / 64",  messages.get(i++).toString() );
				assertEquals( "1920/2/92/e / 64",  messages.get(i++).toString() );
				assertEquals( "1980/2/92/d / 64",  messages.get(i++).toString() );
				assertEquals( "1980/2/92/e / 64",  messages.get(i++).toString() );
			// (m,q=2) d,e:pat (second run)
				// : (q=2) 0:4
				assertEquals( "2040/2/92/d / 64",  messages.get(i++).toString() );
				assertEquals( "2520/2/92/d / 64",  messages.get(i++).toString() );
				// : (m) 0 1
				assertEquals( "3000/2/92/d / 64",  messages.get(i++).toString() );
				assertEquals( "3000/2/92/e / 64",  messages.get(i++).toString() );
				// : (tr=32) 0,1:16
				assertEquals( "3480/2/92/d / 64",  messages.get(i++).toString() );
				assertEquals( "3480/2/92/e / 64",  messages.get(i++).toString() );
				assertEquals( "3540/2/92/d / 64",  messages.get(i++).toString() );
				assertEquals( "3540/2/92/e / 64",  messages.get(i++).toString() );
			// [f:*2] (already over), g
			assertEquals( "4320/2/92/g / 64",  messages.get(i++).toString() );
		}
		
		parse(getWorkingFile("bar-lines"));
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
			if (file.getName().endsWith(".midica") || file.getName().endsWith(".mpl"))
				parse(file);
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
		assertEquals( "CHORD test c,d,e", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_CHORD_ALREADY_DEFINED)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("chord-name-like-note")) );
		assertEquals( 2, e.getLineNumber() );
		assertEquals( "CHORD c# c,d,c", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_CHORD_EQUALS_NOTE)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("chord-name-like-percussion")) );
		assertEquals( 2, e.getLineNumber() );
		assertEquals( "CHORD hhc c,d,e", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_CHORD_EQUALS_PERCUSSION)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("chord-with-duplicate-note")) );
		assertEquals( 2, e.getLineNumber() );
		assertEquals( "CHORD test c,d,c", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_CHORD_CONTAINS_ALREADY)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("chord-separator-double")) );
		assertEquals( 5, e.getLineNumber() );
		assertEquals( "CHORD crd=c,d,,e", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_CHORD_REDUNDANT_SEP)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("chord-separator-leading")) );
		assertEquals( 5, e.getLineNumber() );
		assertEquals( "CHORD crd = ,c,d,e", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_CHORD_REDUNDANT_SEP)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("chord-separator-trailing")) );
		assertEquals( 5, e.getLineNumber() );
		assertEquals( "CHORD crd = c,d,e,", e.getLineContent() );
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
		assertEquals( "CHORD crd==c,d,e", e.getLineContent() );
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
		assertEquals( "SOUNDBANK http:/ /midica.org/assets/sound/invalid.sf2", e.getLineContent() );
		assertTrue( e.getMessage().contains(Dict.get(Dict.INVALID_RIFF)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("soundbank-url-404")) );
		assertEquals( 3, e.getLineNumber() );
		assertTrue( e.getMessage().contains(Dict.get(Dict.DOWNLOAD_PROBLEM)) );
		assertEquals( "SOUNDBANK http:/ /midica.org/assets/sound/404.sf2", e.getLineContent() );
		
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
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("define-assigner-double")) );
		assertEquals( 5, e.getLineNumber() );
		assertEquals( "DEFINE CHORD == CRD", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_DEFINE_NUM_OF_ARGS)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("define-assigner-double-2")) );
		assertEquals( 8, e.getLineNumber() );
		assertEquals( "CRD crd c,d,e", e.getLineContent() );
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
		assertEquals( "CONST $crd c+,d+,e+", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_CONST_ALREADY_DEFINED)) );
		
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
		assertEquals( "0: 60,62,[2]", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_PATTERN_INDEX_TOO_HIGH_2)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("pattern-magic-cond-idx-too-high-4")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "0 60,62,[2] /4", e.getLineContent() );
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
		assertEquals( "0 c,d,e  simple(foo, bar q=2, m", e.getLineContent() );
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
		assertTrue( "1,0 first".equals(e.getLineContent()) || "0,1 second".equals(e.getLineContent()) );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_PATTERN_RECURSION_DEPTH)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("pattern-before-instruments")) );
		assertEquals( 6, e.getLineNumber() );
		assertEquals( "0,1 /4", e.getLineContent() );
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
		assertEquals( "0 f,e -", e.getLineContent() );
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
		assertEquals( "0,1,2 -", e.getLineContent() );
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
		assertEquals( "0: 65,64,62:-", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_ZEROLENGTH_NOT_ALLOWED)) );
		stackTrace = e.getStackTraceElements();
		assertEquals( 11, stackTrace.size() );
		assertEquals( "stacktrace-compact-pattern.midica/66",    stackTrace.pop().toString() ); // : f,e,d:-
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
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("zero-for-note")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "0 c -", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_ZEROLENGTH_NOT_ALLOWED)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("zero-for-note-2")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "0 -", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_ZEROLENGTH_NOT_ALLOWED)) );
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("zero-for-chord")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "0 c,d -", e.getLineContent() );
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
		assertEquals( "0: c c c,d,e:pat_none c c", e.getLineContent() );
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
		assertEquals( "0: c d e:8 f,e,d,c:pat(foo,bar)q=2,m a b c", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_COMPACT_PAT_CALL_WITH_OPT), "q=2,m", "f,e,d,c:pat(foo,bar)q=2,m")));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("compact-pattern-call-with-whitespace")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( "0: c d e:8 f,e,d,c:pat(foo, bar) a b c", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(String.format(Dict.get(Dict.ERROR_COMPACT_PAT_CALL_WITH_OPT), "(foo,", "f,e,d,c:pat(foo,")));
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("compact-pattern-with-wrong-index")) );
		assertEquals( 3, e.getLineNumber() );
		assertEquals( ": 0 1 a", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_PATTERN_INDEX_INVALID)), "a");
		
		e = assertThrows( ParseException.class, () -> parse(getFailingFile("bar-line-too-early")) );
		assertEquals( 9, e.getLineNumber() );
		assertEquals( "0: | c   | c:16 c c c  c c c c  c c c c  c c c c:32 |", e.getLineContent() );
		assertTrue( e.getMessage().startsWith(Dict.get(Dict.ERROR_BAR_LINE_INCORRECT)));
		
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
	}
	
	/**
	 * Returns a source file for testing the parse() method with a
	 * file that is supposed to be parseble.
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
	 * file that is supposed to be parseble.
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
}
