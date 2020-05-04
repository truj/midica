/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.config;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.InvocationTargetException;

import javax.swing.JComboBox;

import org.junit.Test;
import org.midica.Midica;
import org.midica.TestUtil;
import org.midica.ui.UiView;

/**
 * This is the test class for {@link Dict}.
 * 
 * @author Jan Trukenmüller
 */
public class DictTest {
	
	private static UiView         uiView;
	private static JComboBox<?>[] cbxs;
	
	/**
	 * Initializes midica in test mode, if not yet done.
	 * 
	 * @throws InvocationTargetException  on interruptions while initializing Midica.
	 * @throws InterruptedException       on exceptions while initializing Midica.
	 */
	public DictTest() throws InvocationTargetException, InterruptedException {
		TestUtil.initMidica();
		
		uiView = Midica.uiController.getView();
		cbxs   = uiView.getConfigComboboxes();
	}
	
	/**
	 * Tests octave configurations.
	 * 
	 * Using the following config:
	 * 
	 * - note system: international (lower-case)
	 * - half tone: sharp
	 * - octave naming: all possibilities:
	 *     - +n/-n
	 *     - +/-
	 *     - international
	 *     - german
	 */
	@Test
	public void testOctaveSystemsIntLcSharp() {
		
		// test default (+n/-n)
		assertEquals( "c-5",  Dict.getNote(0)   );
		assertEquals( "c#-5", Dict.getNote(1)   );
		assertEquals( "d-5",  Dict.getNote(2)   );
		assertEquals( "d#-5", Dict.getNote(3)   );
		assertEquals( "e-5",  Dict.getNote(4)   );
		assertEquals( "f-5",  Dict.getNote(5)   );
		assertEquals( "f#-5", Dict.getNote(6)   );
		assertEquals( "g-5",  Dict.getNote(7)   );
		assertEquals( "g#-5", Dict.getNote(8)   );
		assertEquals( "a-5",  Dict.getNote(9)   );
		assertEquals( "a#-5", Dict.getNote(10)  );
		assertEquals( "b-5",  Dict.getNote(11)  );
		assertEquals( "c-4",  Dict.getNote(12)  );
		assertEquals( "c#-4", Dict.getNote(13)  );
		assertEquals( "d-4",  Dict.getNote(14)  );
		assertEquals( "d#-4", Dict.getNote(15)  );
		assertEquals( "e-4",  Dict.getNote(16)  );
		assertEquals( "f-4",  Dict.getNote(17)  );
		assertEquals( "f#-4", Dict.getNote(18)  );
		assertEquals( "g-4",  Dict.getNote(19)  );
		assertEquals( "g#-4", Dict.getNote(20)  );
		assertEquals( "a-4",  Dict.getNote(21)  );
		assertEquals( "a#-4", Dict.getNote(22)  );
		assertEquals( "b-4",  Dict.getNote(23)  );
		assertEquals( "c-3",  Dict.getNote(24)  );
		assertEquals( "c#-3", Dict.getNote(25)  );
		assertEquals( "d-3",  Dict.getNote(26)  );
		assertEquals( "d#-3", Dict.getNote(27)  );
		assertEquals( "e-3",  Dict.getNote(28)  );
		assertEquals( "f-3",  Dict.getNote(29)  );
		assertEquals( "f#-3", Dict.getNote(30)  );
		assertEquals( "g-3",  Dict.getNote(31)  );
		assertEquals( "g#-3", Dict.getNote(32)  );
		assertEquals( "a-3",  Dict.getNote(33)  );
		assertEquals( "a#-3", Dict.getNote(34)  );
		assertEquals( "b-3",  Dict.getNote(35)  );
		assertEquals( "c-2",  Dict.getNote(36)  );
		assertEquals( "c#-2", Dict.getNote(37)  );
		assertEquals( "d-2",  Dict.getNote(38)  );
		assertEquals( "d#-2", Dict.getNote(39)  );
		assertEquals( "e-2",  Dict.getNote(40)  );
		assertEquals( "f-2",  Dict.getNote(41)  );
		assertEquals( "f#-2", Dict.getNote(42)  );
		assertEquals( "g-2",  Dict.getNote(43)  );
		assertEquals( "g#-2", Dict.getNote(44)  );
		assertEquals( "a-2",  Dict.getNote(45)  );
		assertEquals( "a#-2", Dict.getNote(46)  );
		assertEquals( "b-2",  Dict.getNote(47)  );
		assertEquals( "c-",   Dict.getNote(48)  );
		assertEquals( "c#-",  Dict.getNote(49)  );
		assertEquals( "d-",   Dict.getNote(50)  );
		assertEquals( "d#-",  Dict.getNote(51)  );
		assertEquals( "e-",   Dict.getNote(52)  );
		assertEquals( "f-",   Dict.getNote(53)  );
		assertEquals( "f#-",  Dict.getNote(54)  );
		assertEquals( "g-",   Dict.getNote(55)  );
		assertEquals( "g#-",  Dict.getNote(56)  );
		assertEquals( "a-",   Dict.getNote(57)  );
		assertEquals( "a#-",  Dict.getNote(58)  );
		assertEquals( "b-",   Dict.getNote(59)  );
		assertEquals( "c",    Dict.getNote(60)  );
		assertEquals( "c#",   Dict.getNote(61)  );
		assertEquals( "d",    Dict.getNote(62)  );
		assertEquals( "d#",   Dict.getNote(63)  );
		assertEquals( "e",    Dict.getNote(64)  );
		assertEquals( "f",    Dict.getNote(65)  );
		assertEquals( "f#",   Dict.getNote(66)  );
		assertEquals( "g",    Dict.getNote(67)  );
		assertEquals( "g#",   Dict.getNote(68)  );
		assertEquals( "a",    Dict.getNote(69)  );
		assertEquals( "a#",   Dict.getNote(70)  );
		assertEquals( "b",    Dict.getNote(71)  );
		assertEquals( "c+",   Dict.getNote(72)  );
		assertEquals( "c#+",  Dict.getNote(73)  );
		assertEquals( "d+",   Dict.getNote(74)  );
		assertEquals( "d#+",  Dict.getNote(75)  );
		assertEquals( "e+",   Dict.getNote(76)  );
		assertEquals( "f+",   Dict.getNote(77)  );
		assertEquals( "f#+",  Dict.getNote(78)  );
		assertEquals( "g+",   Dict.getNote(79)  );
		assertEquals( "g#+",  Dict.getNote(80)  );
		assertEquals( "a+",   Dict.getNote(81)  );
		assertEquals( "a#+",  Dict.getNote(82)  );
		assertEquals( "b+",   Dict.getNote(83)  );
		assertEquals( "c+2",  Dict.getNote(84)  );
		assertEquals( "c#+2", Dict.getNote(85)  );
		assertEquals( "d+2",  Dict.getNote(86)  );
		assertEquals( "d#+2", Dict.getNote(87)  );
		assertEquals( "e+2",  Dict.getNote(88)  );
		assertEquals( "f+2",  Dict.getNote(89)  );
		assertEquals( "f#+2", Dict.getNote(90)  );
		assertEquals( "g+2",  Dict.getNote(91)  );
		assertEquals( "g#+2", Dict.getNote(92)  );
		assertEquals( "a+2",  Dict.getNote(93)  );
		assertEquals( "a#+2", Dict.getNote(94)  );
		assertEquals( "b+2",  Dict.getNote(95)  );
		assertEquals( "c+3",  Dict.getNote(96)  );
		assertEquals( "c#+3", Dict.getNote(97)  );
		assertEquals( "d+3",  Dict.getNote(98)  );
		assertEquals( "d#+3", Dict.getNote(99)  );
		assertEquals( "e+3",  Dict.getNote(100) );
		assertEquals( "f+3",  Dict.getNote(101) );
		assertEquals( "f#+3", Dict.getNote(102) );
		assertEquals( "g+3",  Dict.getNote(103) );
		assertEquals( "g#+3", Dict.getNote(104) );
		assertEquals( "a+3",  Dict.getNote(105) );
		assertEquals( "a#+3", Dict.getNote(106) );
		assertEquals( "b+3",  Dict.getNote(107) );
		assertEquals( "c+4",  Dict.getNote(108) );
		assertEquals( "c#+4", Dict.getNote(109) );
		assertEquals( "d+4",  Dict.getNote(110) );
		assertEquals( "d#+4", Dict.getNote(111) );
		assertEquals( "e+4",  Dict.getNote(112) );
		assertEquals( "f+4",  Dict.getNote(113) );
		assertEquals( "f#+4", Dict.getNote(114) );
		assertEquals( "g+4",  Dict.getNote(115) );
		assertEquals( "g#+4", Dict.getNote(116) );
		assertEquals( "a+4",  Dict.getNote(117) );
		assertEquals( "a#+4", Dict.getNote(118) );
		assertEquals( "b+4",  Dict.getNote(119) );
		assertEquals( "c+5",  Dict.getNote(120) );
		assertEquals( "c#+5", Dict.getNote(121) );
		assertEquals( "d+5",  Dict.getNote(122) );
		assertEquals( "d#+5", Dict.getNote(123) );
		assertEquals( "e+5",  Dict.getNote(124) );
		assertEquals( "f+5",  Dict.getNote(125) );
		assertEquals( "f#+5", Dict.getNote(126) );
		assertEquals( "g+5",  Dict.getNote(127) );
		
		// test +/-
		cbxs[3].setSelectedIndex(1);
		assertEquals( "c-----",  Dict.getNote(0)   );
		assertEquals( "c#-----", Dict.getNote(1)   );
		assertEquals( "d-----",  Dict.getNote(2)   );
		assertEquals( "d#-----", Dict.getNote(3)   );
		assertEquals( "e-----",  Dict.getNote(4)   );
		assertEquals( "f-----",  Dict.getNote(5)   );
		assertEquals( "f#-----", Dict.getNote(6)   );
		assertEquals( "g-----",  Dict.getNote(7)   );
		assertEquals( "g#-----", Dict.getNote(8)   );
		assertEquals( "a-----",  Dict.getNote(9)   );
		assertEquals( "a#-----", Dict.getNote(10)  );
		assertEquals( "b-----",  Dict.getNote(11)  );
		assertEquals( "c----",   Dict.getNote(12)  );
		assertEquals( "c#----",  Dict.getNote(13)  );
		assertEquals( "d----",   Dict.getNote(14)  );
		assertEquals( "d#----",  Dict.getNote(15)  );
		assertEquals( "e----",   Dict.getNote(16)  );
		assertEquals( "f----",   Dict.getNote(17)  );
		assertEquals( "f#----",  Dict.getNote(18)  );
		assertEquals( "g----",   Dict.getNote(19)  );
		assertEquals( "g#----",  Dict.getNote(20)  );
		assertEquals( "a----",   Dict.getNote(21)  );
		assertEquals( "a#----",  Dict.getNote(22)  );
		assertEquals( "b----",   Dict.getNote(23)  );
		assertEquals( "c---",    Dict.getNote(24)  );
		assertEquals( "c#---",   Dict.getNote(25)  );
		assertEquals( "d---",    Dict.getNote(26)  );
		assertEquals( "d#---",   Dict.getNote(27)  );
		assertEquals( "e---",    Dict.getNote(28)  );
		assertEquals( "f---",    Dict.getNote(29)  );
		assertEquals( "f#---",   Dict.getNote(30)  );
		assertEquals( "g---",    Dict.getNote(31)  );
		assertEquals( "g#---",   Dict.getNote(32)  );
		assertEquals( "a---",    Dict.getNote(33)  );
		assertEquals( "a#---",   Dict.getNote(34)  );
		assertEquals( "b---",    Dict.getNote(35)  );
		assertEquals( "c--",     Dict.getNote(36)  );
		assertEquals( "c#--",    Dict.getNote(37)  );
		assertEquals( "d--",     Dict.getNote(38)  );
		assertEquals( "d#--",    Dict.getNote(39)  );
		assertEquals( "e--",     Dict.getNote(40)  );
		assertEquals( "f--",     Dict.getNote(41)  );
		assertEquals( "f#--",    Dict.getNote(42)  );
		assertEquals( "g--",     Dict.getNote(43)  );
		assertEquals( "g#--",    Dict.getNote(44)  );
		assertEquals( "a--",     Dict.getNote(45)  );
		assertEquals( "a#--",    Dict.getNote(46)  );
		assertEquals( "b--",     Dict.getNote(47)  );
		assertEquals( "c-",      Dict.getNote(48)  );
		assertEquals( "c#-",     Dict.getNote(49)  );
		assertEquals( "d-",      Dict.getNote(50)  );
		assertEquals( "d#-",     Dict.getNote(51)  );
		assertEquals( "e-",      Dict.getNote(52)  );
		assertEquals( "f-",      Dict.getNote(53)  );
		assertEquals( "f#-",     Dict.getNote(54)  );
		assertEquals( "g-",      Dict.getNote(55)  );
		assertEquals( "g#-",     Dict.getNote(56)  );
		assertEquals( "a-",      Dict.getNote(57)  );
		assertEquals( "a#-",     Dict.getNote(58)  );
		assertEquals( "b-",      Dict.getNote(59)  );
		assertEquals( "c",       Dict.getNote(60)  );
		assertEquals( "c#",      Dict.getNote(61)  );
		assertEquals( "d",       Dict.getNote(62)  );
		assertEquals( "d#",      Dict.getNote(63)  );
		assertEquals( "e",       Dict.getNote(64)  );
		assertEquals( "f",       Dict.getNote(65)  );
		assertEquals( "f#",      Dict.getNote(66)  );
		assertEquals( "g",       Dict.getNote(67)  );
		assertEquals( "g#",      Dict.getNote(68)  );
		assertEquals( "a",       Dict.getNote(69)  );
		assertEquals( "a#",      Dict.getNote(70)  );
		assertEquals( "b",       Dict.getNote(71)  );
		assertEquals( "c+",      Dict.getNote(72)  );
		assertEquals( "c#+",     Dict.getNote(73)  );
		assertEquals( "d+",      Dict.getNote(74)  );
		assertEquals( "d#+",     Dict.getNote(75)  );
		assertEquals( "e+",      Dict.getNote(76)  );
		assertEquals( "f+",      Dict.getNote(77)  );
		assertEquals( "f#+",     Dict.getNote(78)  );
		assertEquals( "g+",      Dict.getNote(79)  );
		assertEquals( "g#+",     Dict.getNote(80)  );
		assertEquals( "a+",      Dict.getNote(81)  );
		assertEquals( "a#+",     Dict.getNote(82)  );
		assertEquals( "b+",      Dict.getNote(83)  );
		assertEquals( "c++",     Dict.getNote(84)  );
		assertEquals( "c#++",    Dict.getNote(85)  );
		assertEquals( "d++",     Dict.getNote(86)  );
		assertEquals( "d#++",    Dict.getNote(87)  );
		assertEquals( "e++",     Dict.getNote(88)  );
		assertEquals( "f++",     Dict.getNote(89)  );
		assertEquals( "f#++",    Dict.getNote(90)  );
		assertEquals( "g++",     Dict.getNote(91)  );
		assertEquals( "g#++",    Dict.getNote(92)  );
		assertEquals( "a++",     Dict.getNote(93)  );
		assertEquals( "a#++",    Dict.getNote(94)  );
		assertEquals( "b++",     Dict.getNote(95)  );
		assertEquals( "c+++",    Dict.getNote(96)  );
		assertEquals( "c#+++",   Dict.getNote(97)  );
		assertEquals( "d+++",    Dict.getNote(98)  );
		assertEquals( "d#+++",   Dict.getNote(99)  );
		assertEquals( "e+++",    Dict.getNote(100) );
		assertEquals( "f+++",    Dict.getNote(101) );
		assertEquals( "f#+++",   Dict.getNote(102) );
		assertEquals( "g+++",    Dict.getNote(103) );
		assertEquals( "g#+++",   Dict.getNote(104) );
		assertEquals( "a+++",    Dict.getNote(105) );
		assertEquals( "a#+++",   Dict.getNote(106) );
		assertEquals( "b+++",    Dict.getNote(107) );
		assertEquals( "c++++",   Dict.getNote(108) );
		assertEquals( "c#++++",  Dict.getNote(109) );
		assertEquals( "d++++",   Dict.getNote(110) );
		assertEquals( "d#++++",  Dict.getNote(111) );
		assertEquals( "e++++",   Dict.getNote(112) );
		assertEquals( "f++++",   Dict.getNote(113) );
		assertEquals( "f#++++",  Dict.getNote(114) );
		assertEquals( "g++++",   Dict.getNote(115) );
		assertEquals( "g#++++",  Dict.getNote(116) );
		assertEquals( "a++++",   Dict.getNote(117) );
		assertEquals( "a#++++",  Dict.getNote(118) );
		assertEquals( "b++++",   Dict.getNote(119) );
		assertEquals( "c+++++",  Dict.getNote(120) );
		assertEquals( "c#+++++", Dict.getNote(121) );
		assertEquals( "d+++++",  Dict.getNote(122) );
		assertEquals( "d#+++++", Dict.getNote(123) );
		assertEquals( "e+++++",  Dict.getNote(124) );
		assertEquals( "f+++++",  Dict.getNote(125) );
		assertEquals( "f#+++++", Dict.getNote(126) );
		assertEquals( "g+++++",  Dict.getNote(127) );
		
		// test international
		cbxs[3].setSelectedIndex(2);
		assertEquals( "c-1",  Dict.getNote(0)   );
		assertEquals( "c#-1", Dict.getNote(1)   );
		assertEquals( "d-1",  Dict.getNote(2)   );
		assertEquals( "d#-1", Dict.getNote(3)   );
		assertEquals( "e-1",  Dict.getNote(4)   );
		assertEquals( "f-1",  Dict.getNote(5)   );
		assertEquals( "f#-1", Dict.getNote(6)   );
		assertEquals( "g-1",  Dict.getNote(7)   );
		assertEquals( "g#-1", Dict.getNote(8)   );
		assertEquals( "a-1",  Dict.getNote(9)   );
		assertEquals( "a#-1", Dict.getNote(10)  );
		assertEquals( "b-1",  Dict.getNote(11)  );
		assertEquals( "c0",   Dict.getNote(12)  );
		assertEquals( "c#0",  Dict.getNote(13)  );
		assertEquals( "d0",   Dict.getNote(14)  );
		assertEquals( "d#0",  Dict.getNote(15)  );
		assertEquals( "e0",   Dict.getNote(16)  );
		assertEquals( "f0",   Dict.getNote(17)  );
		assertEquals( "f#0",  Dict.getNote(18)  );
		assertEquals( "g0",   Dict.getNote(19)  );
		assertEquals( "g#0",  Dict.getNote(20)  );
		assertEquals( "a0",   Dict.getNote(21)  );
		assertEquals( "a#0",  Dict.getNote(22)  );
		assertEquals( "b0",   Dict.getNote(23)  );
		assertEquals( "c1",   Dict.getNote(24)  );
		assertEquals( "c#1",  Dict.getNote(25)  );
		assertEquals( "d1",   Dict.getNote(26)  );
		assertEquals( "d#1",  Dict.getNote(27)  );
		assertEquals( "e1",   Dict.getNote(28)  );
		assertEquals( "f1",   Dict.getNote(29)  );
		assertEquals( "f#1",  Dict.getNote(30)  );
		assertEquals( "g1",   Dict.getNote(31)  );
		assertEquals( "g#1",  Dict.getNote(32)  );
		assertEquals( "a1",   Dict.getNote(33)  );
		assertEquals( "a#1",  Dict.getNote(34)  );
		assertEquals( "b1",   Dict.getNote(35)  );
		assertEquals( "c2",   Dict.getNote(36)  );
		assertEquals( "c#2",  Dict.getNote(37)  );
		assertEquals( "d2",   Dict.getNote(38)  );
		assertEquals( "d#2",  Dict.getNote(39)  );
		assertEquals( "e2",   Dict.getNote(40)  );
		assertEquals( "f2",   Dict.getNote(41)  );
		assertEquals( "f#2",  Dict.getNote(42)  );
		assertEquals( "g2",   Dict.getNote(43)  );
		assertEquals( "g#2",  Dict.getNote(44)  );
		assertEquals( "a2",   Dict.getNote(45)  );
		assertEquals( "a#2",  Dict.getNote(46)  );
		assertEquals( "b2",   Dict.getNote(47)  );
		assertEquals( "c3",   Dict.getNote(48)  );
		assertEquals( "c#3",  Dict.getNote(49)  );
		assertEquals( "d3",   Dict.getNote(50)  );
		assertEquals( "d#3",  Dict.getNote(51)  );
		assertEquals( "e3",   Dict.getNote(52)  );
		assertEquals( "f3",   Dict.getNote(53)  );
		assertEquals( "f#3",  Dict.getNote(54)  );
		assertEquals( "g3",   Dict.getNote(55)  );
		assertEquals( "g#3",  Dict.getNote(56)  );
		assertEquals( "a3",   Dict.getNote(57)  );
		assertEquals( "a#3",  Dict.getNote(58)  );
		assertEquals( "b3",   Dict.getNote(59)  );
		assertEquals( "c4",   Dict.getNote(60)  );
		assertEquals( "c#4",  Dict.getNote(61)  );
		assertEquals( "d4",   Dict.getNote(62)  );
		assertEquals( "d#4",  Dict.getNote(63)  );
		assertEquals( "e4",   Dict.getNote(64)  );
		assertEquals( "f4",   Dict.getNote(65)  );
		assertEquals( "f#4",  Dict.getNote(66)  );
		assertEquals( "g4",   Dict.getNote(67)  );
		assertEquals( "g#4",  Dict.getNote(68)  );
		assertEquals( "a4",   Dict.getNote(69)  );
		assertEquals( "a#4",  Dict.getNote(70)  );
		assertEquals( "b4",   Dict.getNote(71)  );
		assertEquals( "c5",   Dict.getNote(72)  );
		assertEquals( "c#5",  Dict.getNote(73)  );
		assertEquals( "d5",   Dict.getNote(74)  );
		assertEquals( "d#5",  Dict.getNote(75)  );
		assertEquals( "e5",   Dict.getNote(76)  );
		assertEquals( "f5",   Dict.getNote(77)  );
		assertEquals( "f#5",  Dict.getNote(78)  );
		assertEquals( "g5",   Dict.getNote(79)  );
		assertEquals( "g#5",  Dict.getNote(80)  );
		assertEquals( "a5",   Dict.getNote(81)  );
		assertEquals( "a#5",  Dict.getNote(82)  );
		assertEquals( "b5",   Dict.getNote(83)  );
		assertEquals( "c6",   Dict.getNote(84)  );
		assertEquals( "c#6",  Dict.getNote(85)  );
		assertEquals( "d6",   Dict.getNote(86)  );
		assertEquals( "d#6",  Dict.getNote(87)  );
		assertEquals( "e6",   Dict.getNote(88)  );
		assertEquals( "f6",   Dict.getNote(89)  );
		assertEquals( "f#6",  Dict.getNote(90)  );
		assertEquals( "g6",   Dict.getNote(91)  );
		assertEquals( "g#6",  Dict.getNote(92)  );
		assertEquals( "a6",   Dict.getNote(93)  );
		assertEquals( "a#6",  Dict.getNote(94)  );
		assertEquals( "b6",   Dict.getNote(95)  );
		assertEquals( "c7",   Dict.getNote(96)  );
		assertEquals( "c#7",  Dict.getNote(97)  );
		assertEquals( "d7",   Dict.getNote(98)  );
		assertEquals( "d#7",  Dict.getNote(99)  );
		assertEquals( "e7",   Dict.getNote(100) );
		assertEquals( "f7",   Dict.getNote(101) );
		assertEquals( "f#7",  Dict.getNote(102) );
		assertEquals( "g7",   Dict.getNote(103) );
		assertEquals( "g#7",  Dict.getNote(104) );
		assertEquals( "a7",   Dict.getNote(105) );
		assertEquals( "a#7",  Dict.getNote(106) );
		assertEquals( "b7",   Dict.getNote(107) );
		assertEquals( "c8",   Dict.getNote(108) );
		assertEquals( "c#8",  Dict.getNote(109) );
		assertEquals( "d8",   Dict.getNote(110) );
		assertEquals( "d#8",  Dict.getNote(111) );
		assertEquals( "e8",   Dict.getNote(112) );
		assertEquals( "f8",   Dict.getNote(113) );
		assertEquals( "f#8",  Dict.getNote(114) );
		assertEquals( "g8",   Dict.getNote(115) );
		assertEquals( "g#8",  Dict.getNote(116) );
		assertEquals( "a8",   Dict.getNote(117) );
		assertEquals( "a#8",  Dict.getNote(118) );
		assertEquals( "b8",   Dict.getNote(119) );
		assertEquals( "c9",   Dict.getNote(120) );
		assertEquals( "c#9",  Dict.getNote(121) );
		assertEquals( "d9",   Dict.getNote(122) );
		assertEquals( "d#9",  Dict.getNote(123) );
		assertEquals( "e9",   Dict.getNote(124) );
		assertEquals( "f9",   Dict.getNote(125) );
		assertEquals( "f#9",  Dict.getNote(126) );
		assertEquals( "g9",   Dict.getNote(127) );
		
		// test german
		cbxs[3].setSelectedIndex(3);
		assertEquals( "C'''",     Dict.getNote(0)   );
		assertEquals( "C#'''",    Dict.getNote(1)   );
		assertEquals( "D'''",     Dict.getNote(2)   );
		assertEquals( "D#'''",    Dict.getNote(3)   );
		assertEquals( "E'''",     Dict.getNote(4)   );
		assertEquals( "F'''",     Dict.getNote(5)   );
		assertEquals( "F#'''",    Dict.getNote(6)   );
		assertEquals( "G'''",     Dict.getNote(7)   );
		assertEquals( "G#'''",    Dict.getNote(8)   );
		assertEquals( "A'''",     Dict.getNote(9)   );
		assertEquals( "A#'''",    Dict.getNote(10)  );
		assertEquals( "B'''",     Dict.getNote(11)  );
		assertEquals( "C''",      Dict.getNote(12)  );
		assertEquals( "C#''",     Dict.getNote(13)  );
		assertEquals( "D''",      Dict.getNote(14)  );
		assertEquals( "D#''",     Dict.getNote(15)  );
		assertEquals( "E''",      Dict.getNote(16)  );
		assertEquals( "F''",      Dict.getNote(17)  );
		assertEquals( "F#''",     Dict.getNote(18)  );
		assertEquals( "G''",      Dict.getNote(19)  );
		assertEquals( "G#''",     Dict.getNote(20)  );
		assertEquals( "A''",      Dict.getNote(21)  );
		assertEquals( "A#''",     Dict.getNote(22)  );
		assertEquals( "B''",      Dict.getNote(23)  );
		assertEquals( "C'",       Dict.getNote(24)  );
		assertEquals( "C#'",      Dict.getNote(25)  );
		assertEquals( "D'",       Dict.getNote(26)  );
		assertEquals( "D#'",      Dict.getNote(27)  );
		assertEquals( "E'",       Dict.getNote(28)  );
		assertEquals( "F'",       Dict.getNote(29)  );
		assertEquals( "F#'",      Dict.getNote(30)  );
		assertEquals( "G'",       Dict.getNote(31)  );
		assertEquals( "G#'",      Dict.getNote(32)  );
		assertEquals( "A'",       Dict.getNote(33)  );
		assertEquals( "A#'",      Dict.getNote(34)  );
		assertEquals( "B'",       Dict.getNote(35)  );
		assertEquals( "C",        Dict.getNote(36)  );
		assertEquals( "C#",       Dict.getNote(37)  );
		assertEquals( "D",        Dict.getNote(38)  );
		assertEquals( "D#",       Dict.getNote(39)  );
		assertEquals( "E",        Dict.getNote(40)  );
		assertEquals( "F",        Dict.getNote(41)  );
		assertEquals( "F#",       Dict.getNote(42)  );
		assertEquals( "G",        Dict.getNote(43)  );
		assertEquals( "G#",       Dict.getNote(44)  );
		assertEquals( "A",        Dict.getNote(45)  );
		assertEquals( "A#",       Dict.getNote(46)  );
		assertEquals( "B",        Dict.getNote(47)  );
		assertEquals( "c",        Dict.getNote(48)  );
		assertEquals( "c#",       Dict.getNote(49)  );
		assertEquals( "d",        Dict.getNote(50)  );
		assertEquals( "d#",       Dict.getNote(51)  );
		assertEquals( "e",        Dict.getNote(52)  );
		assertEquals( "f",        Dict.getNote(53)  );
		assertEquals( "f#",       Dict.getNote(54)  );
		assertEquals( "g",        Dict.getNote(55)  );
		assertEquals( "g#",       Dict.getNote(56)  );
		assertEquals( "a",        Dict.getNote(57)  );
		assertEquals( "a#",       Dict.getNote(58)  );
		assertEquals( "b",        Dict.getNote(59)  );
		assertEquals( "c'",       Dict.getNote(60)  );
		assertEquals( "c#'",      Dict.getNote(61)  );
		assertEquals( "d'",       Dict.getNote(62)  );
		assertEquals( "d#'",      Dict.getNote(63)  );
		assertEquals( "e'",       Dict.getNote(64)  );
		assertEquals( "f'",       Dict.getNote(65)  );
		assertEquals( "f#'",      Dict.getNote(66)  );
		assertEquals( "g'",       Dict.getNote(67)  );
		assertEquals( "g#'",      Dict.getNote(68)  );
		assertEquals( "a'",       Dict.getNote(69)  );
		assertEquals( "a#'",      Dict.getNote(70)  );
		assertEquals( "b'",       Dict.getNote(71)  );
		assertEquals( "c''",      Dict.getNote(72)  );
		assertEquals( "c#''",     Dict.getNote(73)  );
		assertEquals( "d''",      Dict.getNote(74)  );
		assertEquals( "d#''",     Dict.getNote(75)  );
		assertEquals( "e''",      Dict.getNote(76)  );
		assertEquals( "f''",      Dict.getNote(77)  );
		assertEquals( "f#''",     Dict.getNote(78)  );
		assertEquals( "g''",      Dict.getNote(79)  );
		assertEquals( "g#''",     Dict.getNote(80)  );
		assertEquals( "a''",      Dict.getNote(81)  );
		assertEquals( "a#''",     Dict.getNote(82)  );
		assertEquals( "b''",      Dict.getNote(83)  );
		assertEquals( "c'''",     Dict.getNote(84)  );
		assertEquals( "c#'''",    Dict.getNote(85)  );
		assertEquals( "d'''",     Dict.getNote(86)  );
		assertEquals( "d#'''",    Dict.getNote(87)  );
		assertEquals( "e'''",     Dict.getNote(88)  );
		assertEquals( "f'''",     Dict.getNote(89)  );
		assertEquals( "f#'''",    Dict.getNote(90)  );
		assertEquals( "g'''",     Dict.getNote(91)  );
		assertEquals( "g#'''",    Dict.getNote(92)  );
		assertEquals( "a'''",     Dict.getNote(93)  );
		assertEquals( "a#'''",    Dict.getNote(94)  );
		assertEquals( "b'''",     Dict.getNote(95)  );
		assertEquals( "c''''",    Dict.getNote(96)  );
		assertEquals( "c#''''",   Dict.getNote(97)  );
		assertEquals( "d''''",    Dict.getNote(98)  );
		assertEquals( "d#''''",   Dict.getNote(99)  );
		assertEquals( "e''''",    Dict.getNote(100) );
		assertEquals( "f''''",    Dict.getNote(101) );
		assertEquals( "f#''''",   Dict.getNote(102) );
		assertEquals( "g''''",    Dict.getNote(103) );
		assertEquals( "g#''''",   Dict.getNote(104) );
		assertEquals( "a''''",    Dict.getNote(105) );
		assertEquals( "a#''''",   Dict.getNote(106) );
		assertEquals( "b''''",    Dict.getNote(107) );
		assertEquals( "c'''''",   Dict.getNote(108) );
		assertEquals( "c#'''''",  Dict.getNote(109) );
		assertEquals( "d'''''",   Dict.getNote(110) );
		assertEquals( "d#'''''",  Dict.getNote(111) );
		assertEquals( "e'''''",   Dict.getNote(112) );
		assertEquals( "f'''''",   Dict.getNote(113) );
		assertEquals( "f#'''''",  Dict.getNote(114) );
		assertEquals( "g'''''",   Dict.getNote(115) );
		assertEquals( "g#'''''",  Dict.getNote(116) );
		assertEquals( "a'''''",   Dict.getNote(117) );
		assertEquals( "a#'''''",  Dict.getNote(118) );
		assertEquals( "b'''''",   Dict.getNote(119) );
		assertEquals( "c''''''",  Dict.getNote(120) );
		assertEquals( "c#''''''", Dict.getNote(121) );
		assertEquals( "d''''''",  Dict.getNote(122) );
		assertEquals( "d#''''''", Dict.getNote(123) );
		assertEquals( "e''''''",  Dict.getNote(124) );
		assertEquals( "f''''''",  Dict.getNote(125) );
		assertEquals( "f#''''''", Dict.getNote(126) );
		assertEquals( "g''''''",  Dict.getNote(127) );
		
		// switch back to default
		cbxs[3].setSelectedIndex(0);
	}
	
	/**
	 * Tests octave configurations.
	 * 
	 * Using the following config:
	 * 
	 * - note system: international (upper-case)
	 * - half tone: flat
	 * - octave naming: all possibilities:
	 *     - +n/-n
	 *     - +/-
	 *     - international
	 *     - german
	 */
	@Test
	public void testOctaveSystemsIntUcFlat() {
		
		// switch config
		cbxs[1].setSelectedIndex(1); // international (upper-case)
		cbxs[2].setSelectedIndex(1); // -es
		
		// test default (+n/-n)
		assertEquals( "C-5",  Dict.getNote(0)   );
		assertEquals( "Db-5", Dict.getNote(1)   );
		assertEquals( "D-5",  Dict.getNote(2)   );
		assertEquals( "Eb-5", Dict.getNote(3)   );
		assertEquals( "E-5",  Dict.getNote(4)   );
		assertEquals( "F-5",  Dict.getNote(5)   );
		assertEquals( "Gb-5", Dict.getNote(6)   );
		assertEquals( "G-5",  Dict.getNote(7)   );
		assertEquals( "Ab-5", Dict.getNote(8)   );
		assertEquals( "A-5",  Dict.getNote(9)   );
		assertEquals( "Bb-5", Dict.getNote(10)  );
		assertEquals( "B-5",  Dict.getNote(11)  );
		assertEquals( "C-4",  Dict.getNote(12)  );
		assertEquals( "Db-4", Dict.getNote(13)  );
		assertEquals( "D-4",  Dict.getNote(14)  );
		assertEquals( "Eb-4", Dict.getNote(15)  );
		assertEquals( "E-4",  Dict.getNote(16)  );
		assertEquals( "F-4",  Dict.getNote(17)  );
		assertEquals( "Gb-4", Dict.getNote(18)  );
		assertEquals( "G-4",  Dict.getNote(19)  );
		assertEquals( "Ab-4", Dict.getNote(20)  );
		assertEquals( "A-4",  Dict.getNote(21)  );
		assertEquals( "Bb-4", Dict.getNote(22)  );
		assertEquals( "B-4",  Dict.getNote(23)  );
		assertEquals( "C-3",  Dict.getNote(24)  );
		assertEquals( "Db-3", Dict.getNote(25)  );
		assertEquals( "D-3",  Dict.getNote(26)  );
		assertEquals( "Eb-3", Dict.getNote(27)  );
		assertEquals( "E-3",  Dict.getNote(28)  );
		assertEquals( "F-3",  Dict.getNote(29)  );
		assertEquals( "Gb-3", Dict.getNote(30)  );
		assertEquals( "G-3",  Dict.getNote(31)  );
		assertEquals( "Ab-3", Dict.getNote(32)  );
		assertEquals( "A-3",  Dict.getNote(33)  );
		assertEquals( "Bb-3", Dict.getNote(34)  );
		assertEquals( "B-3",  Dict.getNote(35)  );
		assertEquals( "C-2",  Dict.getNote(36)  );
		assertEquals( "Db-2", Dict.getNote(37)  );
		assertEquals( "D-2",  Dict.getNote(38)  );
		assertEquals( "Eb-2", Dict.getNote(39)  );
		assertEquals( "E-2",  Dict.getNote(40)  );
		assertEquals( "F-2",  Dict.getNote(41)  );
		assertEquals( "Gb-2", Dict.getNote(42)  );
		assertEquals( "G-2",  Dict.getNote(43)  );
		assertEquals( "Ab-2", Dict.getNote(44)  );
		assertEquals( "A-2",  Dict.getNote(45)  );
		assertEquals( "Bb-2", Dict.getNote(46)  );
		assertEquals( "B-2",  Dict.getNote(47)  );
		assertEquals( "C-",   Dict.getNote(48)  );
		assertEquals( "Db-",  Dict.getNote(49)  );
		assertEquals( "D-",   Dict.getNote(50)  );
		assertEquals( "Eb-",  Dict.getNote(51)  );
		assertEquals( "E-",   Dict.getNote(52)  );
		assertEquals( "F-",   Dict.getNote(53)  );
		assertEquals( "Gb-",  Dict.getNote(54)  );
		assertEquals( "G-",   Dict.getNote(55)  );
		assertEquals( "Ab-",  Dict.getNote(56)  );
		assertEquals( "A-",   Dict.getNote(57)  );
		assertEquals( "Bb-",  Dict.getNote(58)  );
		assertEquals( "B-",   Dict.getNote(59)  );
		assertEquals( "C",    Dict.getNote(60)  );
		assertEquals( "Db",   Dict.getNote(61)  );
		assertEquals( "D",    Dict.getNote(62)  );
		assertEquals( "Eb",   Dict.getNote(63)  );
		assertEquals( "E",    Dict.getNote(64)  );
		assertEquals( "F",    Dict.getNote(65)  );
		assertEquals( "Gb",   Dict.getNote(66)  );
		assertEquals( "G",    Dict.getNote(67)  );
		assertEquals( "Ab",   Dict.getNote(68)  );
		assertEquals( "A",    Dict.getNote(69)  );
		assertEquals( "Bb",   Dict.getNote(70)  );
		assertEquals( "B",    Dict.getNote(71)  );
		assertEquals( "C+",   Dict.getNote(72)  );
		assertEquals( "Db+",  Dict.getNote(73)  );
		assertEquals( "D+",   Dict.getNote(74)  );
		assertEquals( "Eb+",  Dict.getNote(75)  );
		assertEquals( "E+",   Dict.getNote(76)  );
		assertEquals( "F+",   Dict.getNote(77)  );
		assertEquals( "Gb+",  Dict.getNote(78)  );
		assertEquals( "G+",   Dict.getNote(79)  );
		assertEquals( "Ab+",  Dict.getNote(80)  );
		assertEquals( "A+",   Dict.getNote(81)  );
		assertEquals( "Bb+",  Dict.getNote(82)  );
		assertEquals( "B+",   Dict.getNote(83)  );
		assertEquals( "C+2",  Dict.getNote(84)  );
		assertEquals( "Db+2", Dict.getNote(85)  );
		assertEquals( "D+2",  Dict.getNote(86)  );
		assertEquals( "Eb+2", Dict.getNote(87)  );
		assertEquals( "E+2",  Dict.getNote(88)  );
		assertEquals( "F+2",  Dict.getNote(89)  );
		assertEquals( "Gb+2", Dict.getNote(90)  );
		assertEquals( "G+2",  Dict.getNote(91)  );
		assertEquals( "Ab+2", Dict.getNote(92)  );
		assertEquals( "A+2",  Dict.getNote(93)  );
		assertEquals( "Bb+2", Dict.getNote(94)  );
		assertEquals( "B+2",  Dict.getNote(95)  );
		assertEquals( "C+3",  Dict.getNote(96)  );
		assertEquals( "Db+3", Dict.getNote(97)  );
		assertEquals( "D+3",  Dict.getNote(98)  );
		assertEquals( "Eb+3", Dict.getNote(99)  );
		assertEquals( "E+3",  Dict.getNote(100) );
		assertEquals( "F+3",  Dict.getNote(101) );
		assertEquals( "Gb+3", Dict.getNote(102) );
		assertEquals( "G+3",  Dict.getNote(103) );
		assertEquals( "Ab+3", Dict.getNote(104) );
		assertEquals( "A+3",  Dict.getNote(105) );
		assertEquals( "Bb+3", Dict.getNote(106) );
		assertEquals( "B+3",  Dict.getNote(107) );
		assertEquals( "C+4",  Dict.getNote(108) );
		assertEquals( "Db+4", Dict.getNote(109) );
		assertEquals( "D+4",  Dict.getNote(110) );
		assertEquals( "Eb+4", Dict.getNote(111) );
		assertEquals( "E+4",  Dict.getNote(112) );
		assertEquals( "F+4",  Dict.getNote(113) );
		assertEquals( "Gb+4", Dict.getNote(114) );
		assertEquals( "G+4",  Dict.getNote(115) );
		assertEquals( "Ab+4", Dict.getNote(116) );
		assertEquals( "A+4",  Dict.getNote(117) );
		assertEquals( "Bb+4", Dict.getNote(118) );
		assertEquals( "B+4",  Dict.getNote(119) );
		assertEquals( "C+5",  Dict.getNote(120) );
		assertEquals( "Db+5", Dict.getNote(121) );
		assertEquals( "D+5",  Dict.getNote(122) );
		assertEquals( "Eb+5", Dict.getNote(123) );
		assertEquals( "E+5",  Dict.getNote(124) );
		assertEquals( "F+5",  Dict.getNote(125) );
		assertEquals( "Gb+5", Dict.getNote(126) );
		assertEquals( "G+5",  Dict.getNote(127) );
		
		// test +/-
		cbxs[3].setSelectedIndex(1);
		assertEquals( "C-----",  Dict.getNote(0)   );
		assertEquals( "Db-----", Dict.getNote(1)   );
		assertEquals( "D-----",  Dict.getNote(2)   );
		assertEquals( "Eb-----", Dict.getNote(3)   );
		assertEquals( "E-----",  Dict.getNote(4)   );
		assertEquals( "F-----",  Dict.getNote(5)   );
		assertEquals( "Gb-----", Dict.getNote(6)   );
		assertEquals( "G-----",  Dict.getNote(7)   );
		assertEquals( "Ab-----", Dict.getNote(8)   );
		assertEquals( "A-----",  Dict.getNote(9)   );
		assertEquals( "Bb-----", Dict.getNote(10)  );
		assertEquals( "B-----",  Dict.getNote(11)  );
		assertEquals( "C----",   Dict.getNote(12)  );
		assertEquals( "Db----",  Dict.getNote(13)  );
		assertEquals( "D----",   Dict.getNote(14)  );
		assertEquals( "Eb----",  Dict.getNote(15)  );
		assertEquals( "E----",   Dict.getNote(16)  );
		assertEquals( "F----",   Dict.getNote(17)  );
		assertEquals( "Gb----",  Dict.getNote(18)  );
		assertEquals( "G----",   Dict.getNote(19)  );
		assertEquals( "Ab----",  Dict.getNote(20)  );
		assertEquals( "A----",   Dict.getNote(21)  );
		assertEquals( "Bb----",  Dict.getNote(22)  );
		assertEquals( "B----",   Dict.getNote(23)  );
		assertEquals( "C---",    Dict.getNote(24)  );
		assertEquals( "Db---",   Dict.getNote(25)  );
		assertEquals( "D---",    Dict.getNote(26)  );
		assertEquals( "Eb---",   Dict.getNote(27)  );
		assertEquals( "E---",    Dict.getNote(28)  );
		assertEquals( "F---",    Dict.getNote(29)  );
		assertEquals( "Gb---",   Dict.getNote(30)  );
		assertEquals( "G---",    Dict.getNote(31)  );
		assertEquals( "Ab---",   Dict.getNote(32)  );
		assertEquals( "A---",    Dict.getNote(33)  );
		assertEquals( "Bb---",   Dict.getNote(34)  );
		assertEquals( "B---",    Dict.getNote(35)  );
		assertEquals( "C--",     Dict.getNote(36)  );
		assertEquals( "Db--",    Dict.getNote(37)  );
		assertEquals( "D--",     Dict.getNote(38)  );
		assertEquals( "Eb--",    Dict.getNote(39)  );
		assertEquals( "E--",     Dict.getNote(40)  );
		assertEquals( "F--",     Dict.getNote(41)  );
		assertEquals( "Gb--",    Dict.getNote(42)  );
		assertEquals( "G--",     Dict.getNote(43)  );
		assertEquals( "Ab--",    Dict.getNote(44)  );
		assertEquals( "A--",     Dict.getNote(45)  );
		assertEquals( "Bb--",    Dict.getNote(46)  );
		assertEquals( "B--",     Dict.getNote(47)  );
		assertEquals( "C-",      Dict.getNote(48)  );
		assertEquals( "Db-",     Dict.getNote(49)  );
		assertEquals( "D-",      Dict.getNote(50)  );
		assertEquals( "Eb-",     Dict.getNote(51)  );
		assertEquals( "E-",      Dict.getNote(52)  );
		assertEquals( "F-",      Dict.getNote(53)  );
		assertEquals( "Gb-",     Dict.getNote(54)  );
		assertEquals( "G-",      Dict.getNote(55)  );
		assertEquals( "Ab-",     Dict.getNote(56)  );
		assertEquals( "A-",      Dict.getNote(57)  );
		assertEquals( "Bb-",     Dict.getNote(58)  );
		assertEquals( "B-",      Dict.getNote(59)  );
		assertEquals( "C",       Dict.getNote(60)  );
		assertEquals( "Db",      Dict.getNote(61)  );
		assertEquals( "D",       Dict.getNote(62)  );
		assertEquals( "Eb",      Dict.getNote(63)  );
		assertEquals( "E",       Dict.getNote(64)  );
		assertEquals( "F",       Dict.getNote(65)  );
		assertEquals( "Gb",      Dict.getNote(66)  );
		assertEquals( "G",       Dict.getNote(67)  );
		assertEquals( "Ab",      Dict.getNote(68)  );
		assertEquals( "A",       Dict.getNote(69)  );
		assertEquals( "Bb",      Dict.getNote(70)  );
		assertEquals( "B",       Dict.getNote(71)  );
		assertEquals( "C+",      Dict.getNote(72)  );
		assertEquals( "Db+",     Dict.getNote(73)  );
		assertEquals( "D+",      Dict.getNote(74)  );
		assertEquals( "Eb+",     Dict.getNote(75)  );
		assertEquals( "E+",      Dict.getNote(76)  );
		assertEquals( "F+",      Dict.getNote(77)  );
		assertEquals( "Gb+",     Dict.getNote(78)  );
		assertEquals( "G+",      Dict.getNote(79)  );
		assertEquals( "Ab+",     Dict.getNote(80)  );
		assertEquals( "A+",      Dict.getNote(81)  );
		assertEquals( "Bb+",     Dict.getNote(82)  );
		assertEquals( "B+",      Dict.getNote(83)  );
		assertEquals( "C++",     Dict.getNote(84)  );
		assertEquals( "Db++",    Dict.getNote(85)  );
		assertEquals( "D++",     Dict.getNote(86)  );
		assertEquals( "Eb++",    Dict.getNote(87)  );
		assertEquals( "E++",     Dict.getNote(88)  );
		assertEquals( "F++",     Dict.getNote(89)  );
		assertEquals( "Gb++",    Dict.getNote(90)  );
		assertEquals( "G++",     Dict.getNote(91)  );
		assertEquals( "Ab++",    Dict.getNote(92)  );
		assertEquals( "A++",     Dict.getNote(93)  );
		assertEquals( "Bb++",    Dict.getNote(94)  );
		assertEquals( "B++",     Dict.getNote(95)  );
		assertEquals( "C+++",    Dict.getNote(96)  );
		assertEquals( "Db+++",   Dict.getNote(97)  );
		assertEquals( "D+++",    Dict.getNote(98)  );
		assertEquals( "Eb+++",   Dict.getNote(99)  );
		assertEquals( "E+++",    Dict.getNote(100) );
		assertEquals( "F+++",    Dict.getNote(101) );
		assertEquals( "Gb+++",   Dict.getNote(102) );
		assertEquals( "G+++",    Dict.getNote(103) );
		assertEquals( "Ab+++",   Dict.getNote(104) );
		assertEquals( "A+++",    Dict.getNote(105) );
		assertEquals( "Bb+++",   Dict.getNote(106) );
		assertEquals( "B+++",    Dict.getNote(107) );
		assertEquals( "C++++",   Dict.getNote(108) );
		assertEquals( "Db++++",  Dict.getNote(109) );
		assertEquals( "D++++",   Dict.getNote(110) );
		assertEquals( "Eb++++",  Dict.getNote(111) );
		assertEquals( "E++++",   Dict.getNote(112) );
		assertEquals( "F++++",   Dict.getNote(113) );
		assertEquals( "Gb++++",  Dict.getNote(114) );
		assertEquals( "G++++",   Dict.getNote(115) );
		assertEquals( "Ab++++",  Dict.getNote(116) );
		assertEquals( "A++++",   Dict.getNote(117) );
		assertEquals( "Bb++++",  Dict.getNote(118) );
		assertEquals( "B++++",   Dict.getNote(119) );
		assertEquals( "C+++++",  Dict.getNote(120) );
		assertEquals( "Db+++++", Dict.getNote(121) );
		assertEquals( "D+++++",  Dict.getNote(122) );
		assertEquals( "Eb+++++", Dict.getNote(123) );
		assertEquals( "E+++++",  Dict.getNote(124) );
		assertEquals( "F+++++",  Dict.getNote(125) );
		assertEquals( "Gb+++++", Dict.getNote(126) );
		assertEquals( "G+++++",  Dict.getNote(127) );
		
		// test international
		cbxs[3].setSelectedIndex(2);
		assertEquals( "C-1",  Dict.getNote(0)   );
		assertEquals( "Db-1", Dict.getNote(1)   );
		assertEquals( "D-1",  Dict.getNote(2)   );
		assertEquals( "Eb-1", Dict.getNote(3)   );
		assertEquals( "E-1",  Dict.getNote(4)   );
		assertEquals( "F-1",  Dict.getNote(5)   );
		assertEquals( "Gb-1", Dict.getNote(6)   );
		assertEquals( "G-1",  Dict.getNote(7)   );
		assertEquals( "Ab-1", Dict.getNote(8)   );
		assertEquals( "A-1",  Dict.getNote(9)   );
		assertEquals( "Bb-1", Dict.getNote(10)  );
		assertEquals( "B-1",  Dict.getNote(11)  );
		assertEquals( "C0",   Dict.getNote(12)  );
		assertEquals( "Db0",  Dict.getNote(13)  );
		assertEquals( "D0",   Dict.getNote(14)  );
		assertEquals( "Eb0",  Dict.getNote(15)  );
		assertEquals( "E0",   Dict.getNote(16)  );
		assertEquals( "F0",   Dict.getNote(17)  );
		assertEquals( "Gb0",  Dict.getNote(18)  );
		assertEquals( "G0",   Dict.getNote(19)  );
		assertEquals( "Ab0",  Dict.getNote(20)  );
		assertEquals( "A0",   Dict.getNote(21)  );
		assertEquals( "Bb0",  Dict.getNote(22)  );
		assertEquals( "B0",   Dict.getNote(23)  );
		assertEquals( "C1",   Dict.getNote(24)  );
		assertEquals( "Db1",  Dict.getNote(25)  );
		assertEquals( "D1",   Dict.getNote(26)  );
		assertEquals( "Eb1",  Dict.getNote(27)  );
		assertEquals( "E1",   Dict.getNote(28)  );
		assertEquals( "F1",   Dict.getNote(29)  );
		assertEquals( "Gb1",  Dict.getNote(30)  );
		assertEquals( "G1",   Dict.getNote(31)  );
		assertEquals( "Ab1",  Dict.getNote(32)  );
		assertEquals( "A1",   Dict.getNote(33)  );
		assertEquals( "Bb1",  Dict.getNote(34)  );
		assertEquals( "B1",   Dict.getNote(35)  );
		assertEquals( "C2",   Dict.getNote(36)  );
		assertEquals( "Db2",  Dict.getNote(37)  );
		assertEquals( "D2",   Dict.getNote(38)  );
		assertEquals( "Eb2",  Dict.getNote(39)  );
		assertEquals( "E2",   Dict.getNote(40)  );
		assertEquals( "F2",   Dict.getNote(41)  );
		assertEquals( "Gb2",  Dict.getNote(42)  );
		assertEquals( "G2",   Dict.getNote(43)  );
		assertEquals( "Ab2",  Dict.getNote(44)  );
		assertEquals( "A2",   Dict.getNote(45)  );
		assertEquals( "Bb2",  Dict.getNote(46)  );
		assertEquals( "B2",   Dict.getNote(47)  );
		assertEquals( "C3",   Dict.getNote(48)  );
		assertEquals( "Db3",  Dict.getNote(49)  );
		assertEquals( "D3",   Dict.getNote(50)  );
		assertEquals( "Eb3",  Dict.getNote(51)  );
		assertEquals( "E3",   Dict.getNote(52)  );
		assertEquals( "F3",   Dict.getNote(53)  );
		assertEquals( "Gb3",  Dict.getNote(54)  );
		assertEquals( "G3",   Dict.getNote(55)  );
		assertEquals( "Ab3",  Dict.getNote(56)  );
		assertEquals( "A3",   Dict.getNote(57)  );
		assertEquals( "Bb3",  Dict.getNote(58)  );
		assertEquals( "B3",   Dict.getNote(59)  );
		assertEquals( "C4",   Dict.getNote(60)  );
		assertEquals( "Db4",  Dict.getNote(61)  );
		assertEquals( "D4",   Dict.getNote(62)  );
		assertEquals( "Eb4",  Dict.getNote(63)  );
		assertEquals( "E4",   Dict.getNote(64)  );
		assertEquals( "F4",   Dict.getNote(65)  );
		assertEquals( "Gb4",  Dict.getNote(66)  );
		assertEquals( "G4",   Dict.getNote(67)  );
		assertEquals( "Ab4",  Dict.getNote(68)  );
		assertEquals( "A4",   Dict.getNote(69)  );
		assertEquals( "Bb4",  Dict.getNote(70)  );
		assertEquals( "B4",   Dict.getNote(71)  );
		assertEquals( "C5",   Dict.getNote(72)  );
		assertEquals( "Db5",  Dict.getNote(73)  );
		assertEquals( "D5",   Dict.getNote(74)  );
		assertEquals( "Eb5",  Dict.getNote(75)  );
		assertEquals( "E5",   Dict.getNote(76)  );
		assertEquals( "F5",   Dict.getNote(77)  );
		assertEquals( "Gb5",  Dict.getNote(78)  );
		assertEquals( "G5",   Dict.getNote(79)  );
		assertEquals( "Ab5",  Dict.getNote(80)  );
		assertEquals( "A5",   Dict.getNote(81)  );
		assertEquals( "Bb5",  Dict.getNote(82)  );
		assertEquals( "B5",   Dict.getNote(83)  );
		assertEquals( "C6",   Dict.getNote(84)  );
		assertEquals( "Db6",  Dict.getNote(85)  );
		assertEquals( "D6",   Dict.getNote(86)  );
		assertEquals( "Eb6",  Dict.getNote(87)  );
		assertEquals( "E6",   Dict.getNote(88)  );
		assertEquals( "F6",   Dict.getNote(89)  );
		assertEquals( "Gb6",  Dict.getNote(90)  );
		assertEquals( "G6",   Dict.getNote(91)  );
		assertEquals( "Ab6",  Dict.getNote(92)  );
		assertEquals( "A6",   Dict.getNote(93)  );
		assertEquals( "Bb6",  Dict.getNote(94)  );
		assertEquals( "B6",   Dict.getNote(95)  );
		assertEquals( "C7",   Dict.getNote(96)  );
		assertEquals( "Db7",  Dict.getNote(97)  );
		assertEquals( "D7",   Dict.getNote(98)  );
		assertEquals( "Eb7",  Dict.getNote(99)  );
		assertEquals( "E7",   Dict.getNote(100) );
		assertEquals( "F7",   Dict.getNote(101) );
		assertEquals( "Gb7",  Dict.getNote(102) );
		assertEquals( "G7",   Dict.getNote(103) );
		assertEquals( "Ab7",  Dict.getNote(104) );
		assertEquals( "A7",   Dict.getNote(105) );
		assertEquals( "Bb7",  Dict.getNote(106) );
		assertEquals( "B7",   Dict.getNote(107) );
		assertEquals( "C8",   Dict.getNote(108) );
		assertEquals( "Db8",  Dict.getNote(109) );
		assertEquals( "D8",   Dict.getNote(110) );
		assertEquals( "Eb8",  Dict.getNote(111) );
		assertEquals( "E8",   Dict.getNote(112) );
		assertEquals( "F8",   Dict.getNote(113) );
		assertEquals( "Gb8",  Dict.getNote(114) );
		assertEquals( "G8",   Dict.getNote(115) );
		assertEquals( "Ab8",  Dict.getNote(116) );
		assertEquals( "A8",   Dict.getNote(117) );
		assertEquals( "Bb8",  Dict.getNote(118) );
		assertEquals( "B8",   Dict.getNote(119) );
		assertEquals( "C9",   Dict.getNote(120) );
		assertEquals( "Db9",  Dict.getNote(121) );
		assertEquals( "D9",   Dict.getNote(122) );
		assertEquals( "Eb9",  Dict.getNote(123) );
		assertEquals( "E9",   Dict.getNote(124) );
		assertEquals( "F9",   Dict.getNote(125) );
		assertEquals( "Gb9",  Dict.getNote(126) );
		assertEquals( "G9",   Dict.getNote(127) );
		
		// test german
		cbxs[3].setSelectedIndex(3);
		assertEquals( "C'''",     Dict.getNote(0)   );
		assertEquals( "Db'''",    Dict.getNote(1)   );
		assertEquals( "D'''",     Dict.getNote(2)   );
		assertEquals( "Eb'''",    Dict.getNote(3)   );
		assertEquals( "E'''",     Dict.getNote(4)   );
		assertEquals( "F'''",     Dict.getNote(5)   );
		assertEquals( "Gb'''",    Dict.getNote(6)   );
		assertEquals( "G'''",     Dict.getNote(7)   );
		assertEquals( "Ab'''",    Dict.getNote(8)   );
		assertEquals( "A'''",     Dict.getNote(9)   );
		assertEquals( "Bb'''",    Dict.getNote(10)  );
		assertEquals( "B'''",     Dict.getNote(11)  );
		assertEquals( "C''",      Dict.getNote(12)  );
		assertEquals( "Db''",     Dict.getNote(13)  );
		assertEquals( "D''",      Dict.getNote(14)  );
		assertEquals( "Eb''",     Dict.getNote(15)  );
		assertEquals( "E''",      Dict.getNote(16)  );
		assertEquals( "F''",      Dict.getNote(17)  );
		assertEquals( "Gb''",     Dict.getNote(18)  );
		assertEquals( "G''",      Dict.getNote(19)  );
		assertEquals( "Ab''",     Dict.getNote(20)  );
		assertEquals( "A''",      Dict.getNote(21)  );
		assertEquals( "Bb''",     Dict.getNote(22)  );
		assertEquals( "B''",      Dict.getNote(23)  );
		assertEquals( "C'",       Dict.getNote(24)  );
		assertEquals( "Db'",      Dict.getNote(25)  );
		assertEquals( "D'",       Dict.getNote(26)  );
		assertEquals( "Eb'",      Dict.getNote(27)  );
		assertEquals( "E'",       Dict.getNote(28)  );
		assertEquals( "F'",       Dict.getNote(29)  );
		assertEquals( "Gb'",      Dict.getNote(30)  );
		assertEquals( "G'",       Dict.getNote(31)  );
		assertEquals( "Ab'",      Dict.getNote(32)  );
		assertEquals( "A'",       Dict.getNote(33)  );
		assertEquals( "Bb'",      Dict.getNote(34)  );
		assertEquals( "B'",       Dict.getNote(35)  );
		assertEquals( "C",        Dict.getNote(36)  );
		assertEquals( "Db",       Dict.getNote(37)  );
		assertEquals( "D",        Dict.getNote(38)  );
		assertEquals( "Eb",       Dict.getNote(39)  );
		assertEquals( "E",        Dict.getNote(40)  );
		assertEquals( "F",        Dict.getNote(41)  );
		assertEquals( "Gb",       Dict.getNote(42)  );
		assertEquals( "G",        Dict.getNote(43)  );
		assertEquals( "Ab",       Dict.getNote(44)  );
		assertEquals( "A",        Dict.getNote(45)  );
		assertEquals( "Bb",       Dict.getNote(46)  );
		assertEquals( "B",        Dict.getNote(47)  );
		assertEquals( "c",        Dict.getNote(48)  );
		assertEquals( "db",       Dict.getNote(49)  );
		assertEquals( "d",        Dict.getNote(50)  );
		assertEquals( "eb",       Dict.getNote(51)  );
		assertEquals( "e",        Dict.getNote(52)  );
		assertEquals( "f",        Dict.getNote(53)  );
		assertEquals( "gb",       Dict.getNote(54)  );
		assertEquals( "g",        Dict.getNote(55)  );
		assertEquals( "ab",       Dict.getNote(56)  );
		assertEquals( "a",        Dict.getNote(57)  );
		assertEquals( "bb",       Dict.getNote(58)  );
		assertEquals( "b",        Dict.getNote(59)  );
		assertEquals( "c'",       Dict.getNote(60)  );
		assertEquals( "db'",      Dict.getNote(61)  );
		assertEquals( "d'",       Dict.getNote(62)  );
		assertEquals( "eb'",      Dict.getNote(63)  );
		assertEquals( "e'",       Dict.getNote(64)  );
		assertEquals( "f'",       Dict.getNote(65)  );
		assertEquals( "gb'",      Dict.getNote(66)  );
		assertEquals( "g'",       Dict.getNote(67)  );
		assertEquals( "ab'",      Dict.getNote(68)  );
		assertEquals( "a'",       Dict.getNote(69)  );
		assertEquals( "bb'",      Dict.getNote(70)  );
		assertEquals( "b'",       Dict.getNote(71)  );
		assertEquals( "c''",      Dict.getNote(72)  );
		assertEquals( "db''",     Dict.getNote(73)  );
		assertEquals( "d''",      Dict.getNote(74)  );
		assertEquals( "eb''",     Dict.getNote(75)  );
		assertEquals( "e''",      Dict.getNote(76)  );
		assertEquals( "f''",      Dict.getNote(77)  );
		assertEquals( "gb''",     Dict.getNote(78)  );
		assertEquals( "g''",      Dict.getNote(79)  );
		assertEquals( "ab''",     Dict.getNote(80)  );
		assertEquals( "a''",      Dict.getNote(81)  );
		assertEquals( "bb''",     Dict.getNote(82)  );
		assertEquals( "b''",      Dict.getNote(83)  );
		assertEquals( "c'''",     Dict.getNote(84)  );
		assertEquals( "db'''",    Dict.getNote(85)  );
		assertEquals( "d'''",     Dict.getNote(86)  );
		assertEquals( "eb'''",    Dict.getNote(87)  );
		assertEquals( "e'''",     Dict.getNote(88)  );
		assertEquals( "f'''",     Dict.getNote(89)  );
		assertEquals( "gb'''",    Dict.getNote(90)  );
		assertEquals( "g'''",     Dict.getNote(91)  );
		assertEquals( "ab'''",    Dict.getNote(92)  );
		assertEquals( "a'''",     Dict.getNote(93)  );
		assertEquals( "bb'''",    Dict.getNote(94)  );
		assertEquals( "b'''",     Dict.getNote(95)  );
		assertEquals( "c''''",    Dict.getNote(96)  );
		assertEquals( "db''''",   Dict.getNote(97)  );
		assertEquals( "d''''",    Dict.getNote(98)  );
		assertEquals( "eb''''",   Dict.getNote(99)  );
		assertEquals( "e''''",    Dict.getNote(100) );
		assertEquals( "f''''",    Dict.getNote(101) );
		assertEquals( "gb''''",   Dict.getNote(102) );
		assertEquals( "g''''",    Dict.getNote(103) );
		assertEquals( "ab''''",   Dict.getNote(104) );
		assertEquals( "a''''",    Dict.getNote(105) );
		assertEquals( "bb''''",   Dict.getNote(106) );
		assertEquals( "b''''",    Dict.getNote(107) );
		assertEquals( "c'''''",   Dict.getNote(108) );
		assertEquals( "db'''''",  Dict.getNote(109) );
		assertEquals( "d'''''",   Dict.getNote(110) );
		assertEquals( "eb'''''",  Dict.getNote(111) );
		assertEquals( "e'''''",   Dict.getNote(112) );
		assertEquals( "f'''''",   Dict.getNote(113) );
		assertEquals( "gb'''''",  Dict.getNote(114) );
		assertEquals( "g'''''",   Dict.getNote(115) );
		assertEquals( "ab'''''",  Dict.getNote(116) );
		assertEquals( "a'''''",   Dict.getNote(117) );
		assertEquals( "bb'''''",  Dict.getNote(118) );
		assertEquals( "b'''''",   Dict.getNote(119) );
		assertEquals( "c''''''",  Dict.getNote(120) );
		assertEquals( "db''''''", Dict.getNote(121) );
		assertEquals( "d''''''",  Dict.getNote(122) );
		assertEquals( "eb''''''", Dict.getNote(123) );
		assertEquals( "e''''''",  Dict.getNote(124) );
		assertEquals( "f''''''",  Dict.getNote(125) );
		assertEquals( "gb''''''", Dict.getNote(126) );
		assertEquals( "g''''''",  Dict.getNote(127) );
		
		// switch back to default
		cbxs[1].setSelectedIndex(0); 
		cbxs[2].setSelectedIndex(0);
		cbxs[3].setSelectedIndex(0);
	}
	
	/**
	 * Tests octave configurations.
	 * 
	 * Using the following config:
	 * 
	 * - note system: italian (upper-case)
	 * - half tone: diesis
	 * - octave naming: all possibilities:
	 *     - +n/-n
	 *     - +/-
	 *     - international
	 *     - german
	 */
	@Test
	public void testOctaveSystemsItUcDiesis() {
		
		// switch config
		cbxs[1].setSelectedIndex(3); // italian (upper-case) 
		cbxs[2].setSelectedIndex(2); // diesis
		
		// test default (+n/-n)
		assertEquals( "Do-5",         Dict.getNote(0)   );
		assertEquals( "Do-diesis-5",  Dict.getNote(1)   );
		assertEquals( "Re-5",         Dict.getNote(2)   );
		assertEquals( "Re-diesis-5",  Dict.getNote(3)   );
		assertEquals( "Mi-5",         Dict.getNote(4)   );
		assertEquals( "Fa-5",         Dict.getNote(5)   );
		assertEquals( "Fa-diesis-5",  Dict.getNote(6)   );
		assertEquals( "Sol-5",        Dict.getNote(7)   );
		assertEquals( "Sol-diesis-5", Dict.getNote(8)   );
		assertEquals( "La-5",         Dict.getNote(9)   );
		assertEquals( "La-diesis-5",  Dict.getNote(10)  );
		assertEquals( "Si-5",         Dict.getNote(11)  );
		assertEquals( "Do-4",         Dict.getNote(12)  );
		assertEquals( "Do-diesis-4",  Dict.getNote(13)  );
		assertEquals( "Re-4",         Dict.getNote(14)  );
		assertEquals( "Re-diesis-4",  Dict.getNote(15)  );
		assertEquals( "Mi-4",         Dict.getNote(16)  );
		assertEquals( "Fa-4",         Dict.getNote(17)  );
		assertEquals( "Fa-diesis-4",  Dict.getNote(18)  );
		assertEquals( "Sol-4",        Dict.getNote(19)  );
		assertEquals( "Sol-diesis-4", Dict.getNote(20)  );
		assertEquals( "La-4",         Dict.getNote(21)  );
		assertEquals( "La-diesis-4",  Dict.getNote(22)  );
		assertEquals( "Si-4",         Dict.getNote(23)  );
		assertEquals( "Do-3",         Dict.getNote(24)  );
		assertEquals( "Do-diesis-3",  Dict.getNote(25)  );
		assertEquals( "Re-3",         Dict.getNote(26)  );
		assertEquals( "Re-diesis-3",  Dict.getNote(27)  );
		assertEquals( "Mi-3",         Dict.getNote(28)  );
		assertEquals( "Fa-3",         Dict.getNote(29)  );
		assertEquals( "Fa-diesis-3",  Dict.getNote(30)  );
		assertEquals( "Sol-3",        Dict.getNote(31)  );
		assertEquals( "Sol-diesis-3", Dict.getNote(32)  );
		assertEquals( "La-3",         Dict.getNote(33)  );
		assertEquals( "La-diesis-3",  Dict.getNote(34)  );
		assertEquals( "Si-3",         Dict.getNote(35)  );
		assertEquals( "Do-2",         Dict.getNote(36)  );
		assertEquals( "Do-diesis-2",  Dict.getNote(37)  );
		assertEquals( "Re-2",         Dict.getNote(38)  );
		assertEquals( "Re-diesis-2",  Dict.getNote(39)  );
		assertEquals( "Mi-2",         Dict.getNote(40)  );
		assertEquals( "Fa-2",         Dict.getNote(41)  );
		assertEquals( "Fa-diesis-2",  Dict.getNote(42)  );
		assertEquals( "Sol-2",        Dict.getNote(43)  );
		assertEquals( "Sol-diesis-2", Dict.getNote(44)  );
		assertEquals( "La-2",         Dict.getNote(45)  );
		assertEquals( "La-diesis-2",  Dict.getNote(46)  );
		assertEquals( "Si-2",         Dict.getNote(47)  );
		assertEquals( "Do-",          Dict.getNote(48)  );
		assertEquals( "Do-diesis-",   Dict.getNote(49)  );
		assertEquals( "Re-",          Dict.getNote(50)  );
		assertEquals( "Re-diesis-",   Dict.getNote(51)  );
		assertEquals( "Mi-",          Dict.getNote(52)  );
		assertEquals( "Fa-",          Dict.getNote(53)  );
		assertEquals( "Fa-diesis-",   Dict.getNote(54)  );
		assertEquals( "Sol-",         Dict.getNote(55)  );
		assertEquals( "Sol-diesis-",  Dict.getNote(56)  );
		assertEquals( "La-",          Dict.getNote(57)  );
		assertEquals( "La-diesis-",   Dict.getNote(58)  );
		assertEquals( "Si-",          Dict.getNote(59)  );
		assertEquals( "Do",           Dict.getNote(60)  );
		assertEquals( "Do-diesis",    Dict.getNote(61)  );
		assertEquals( "Re",           Dict.getNote(62)  );
		assertEquals( "Re-diesis",    Dict.getNote(63)  );
		assertEquals( "Mi",           Dict.getNote(64)  );
		assertEquals( "Fa",           Dict.getNote(65)  );
		assertEquals( "Fa-diesis",    Dict.getNote(66)  );
		assertEquals( "Sol",          Dict.getNote(67)  );
		assertEquals( "Sol-diesis",   Dict.getNote(68)  );
		assertEquals( "La",           Dict.getNote(69)  );
		assertEquals( "La-diesis",    Dict.getNote(70)  );
		assertEquals( "Si",           Dict.getNote(71)  );
		assertEquals( "Do+",          Dict.getNote(72)  );
		assertEquals( "Do-diesis+",   Dict.getNote(73)  );
		assertEquals( "Re+",          Dict.getNote(74)  );
		assertEquals( "Re-diesis+",   Dict.getNote(75)  );
		assertEquals( "Mi+",          Dict.getNote(76)  );
		assertEquals( "Fa+",          Dict.getNote(77)  );
		assertEquals( "Fa-diesis+",   Dict.getNote(78)  );
		assertEquals( "Sol+",         Dict.getNote(79)  );
		assertEquals( "Sol-diesis+",  Dict.getNote(80)  );
		assertEquals( "La+",          Dict.getNote(81)  );
		assertEquals( "La-diesis+",   Dict.getNote(82)  );
		assertEquals( "Si+",          Dict.getNote(83)  );
		assertEquals( "Do+2",         Dict.getNote(84)  );
		assertEquals( "Do-diesis+2",  Dict.getNote(85)  );
		assertEquals( "Re+2",         Dict.getNote(86)  );
		assertEquals( "Re-diesis+2",  Dict.getNote(87)  );
		assertEquals( "Mi+2",         Dict.getNote(88)  );
		assertEquals( "Fa+2",         Dict.getNote(89)  );
		assertEquals( "Fa-diesis+2",  Dict.getNote(90)  );
		assertEquals( "Sol+2",        Dict.getNote(91)  );
		assertEquals( "Sol-diesis+2", Dict.getNote(92)  );
		assertEquals( "La+2",         Dict.getNote(93)  );
		assertEquals( "La-diesis+2",  Dict.getNote(94)  );
		assertEquals( "Si+2",         Dict.getNote(95)  );
		assertEquals( "Do+3",         Dict.getNote(96)  );
		assertEquals( "Do-diesis+3",  Dict.getNote(97)  );
		assertEquals( "Re+3",         Dict.getNote(98)  );
		assertEquals( "Re-diesis+3",  Dict.getNote(99)  );
		assertEquals( "Mi+3",         Dict.getNote(100) );
		assertEquals( "Fa+3",         Dict.getNote(101) );
		assertEquals( "Fa-diesis+3",  Dict.getNote(102) );
		assertEquals( "Sol+3",        Dict.getNote(103) );
		assertEquals( "Sol-diesis+3", Dict.getNote(104) );
		assertEquals( "La+3",         Dict.getNote(105) );
		assertEquals( "La-diesis+3",  Dict.getNote(106) );
		assertEquals( "Si+3",         Dict.getNote(107) );
		assertEquals( "Do+4",         Dict.getNote(108) );
		assertEquals( "Do-diesis+4",  Dict.getNote(109) );
		assertEquals( "Re+4",         Dict.getNote(110) );
		assertEquals( "Re-diesis+4",  Dict.getNote(111) );
		assertEquals( "Mi+4",         Dict.getNote(112) );
		assertEquals( "Fa+4",         Dict.getNote(113) );
		assertEquals( "Fa-diesis+4",  Dict.getNote(114) );
		assertEquals( "Sol+4",        Dict.getNote(115) );
		assertEquals( "Sol-diesis+4", Dict.getNote(116) );
		assertEquals( "La+4",         Dict.getNote(117) );
		assertEquals( "La-diesis+4",  Dict.getNote(118) );
		assertEquals( "Si+4",         Dict.getNote(119) );
		assertEquals( "Do+5",         Dict.getNote(120) );
		assertEquals( "Do-diesis+5",  Dict.getNote(121) );
		assertEquals( "Re+5",         Dict.getNote(122) );
		assertEquals( "Re-diesis+5",  Dict.getNote(123) );
		assertEquals( "Mi+5",         Dict.getNote(124) );
		assertEquals( "Fa+5",         Dict.getNote(125) );
		assertEquals( "Fa-diesis+5",  Dict.getNote(126) );
		assertEquals( "Sol+5",        Dict.getNote(127) );
		
		// test +/-
		cbxs[3].setSelectedIndex(1);
		assertEquals( "Do-----",         Dict.getNote(0)   );
		assertEquals( "Do-diesis-----",  Dict.getNote(1)   );
		assertEquals( "Re-----",         Dict.getNote(2)   );
		assertEquals( "Re-diesis-----",  Dict.getNote(3)   );
		assertEquals( "Mi-----",         Dict.getNote(4)   );
		assertEquals( "Fa-----",         Dict.getNote(5)   );
		assertEquals( "Fa-diesis-----",  Dict.getNote(6)   );
		assertEquals( "Sol-----",        Dict.getNote(7)   );
		assertEquals( "Sol-diesis-----", Dict.getNote(8)   );
		assertEquals( "La-----",         Dict.getNote(9)   );
		assertEquals( "La-diesis-----",  Dict.getNote(10)  );
		assertEquals( "Si-----",         Dict.getNote(11)  );
		assertEquals( "Do----",          Dict.getNote(12)  );
		assertEquals( "Do-diesis----",   Dict.getNote(13)  );
		assertEquals( "Re----",          Dict.getNote(14)  );
		assertEquals( "Re-diesis----",   Dict.getNote(15)  );
		assertEquals( "Mi----",          Dict.getNote(16)  );
		assertEquals( "Fa----",          Dict.getNote(17)  );
		assertEquals( "Fa-diesis----",   Dict.getNote(18)  );
		assertEquals( "Sol----",         Dict.getNote(19)  );
		assertEquals( "Sol-diesis----",  Dict.getNote(20)  );
		assertEquals( "La----",          Dict.getNote(21)  );
		assertEquals( "La-diesis----",   Dict.getNote(22)  );
		assertEquals( "Si----",          Dict.getNote(23)  );
		assertEquals( "Do---",           Dict.getNote(24)  );
		assertEquals( "Do-diesis---",    Dict.getNote(25)  );
		assertEquals( "Re---",           Dict.getNote(26)  );
		assertEquals( "Re-diesis---",    Dict.getNote(27)  );
		assertEquals( "Mi---",           Dict.getNote(28)  );
		assertEquals( "Fa---",           Dict.getNote(29)  );
		assertEquals( "Fa-diesis---",    Dict.getNote(30)  );
		assertEquals( "Sol---",          Dict.getNote(31)  );
		assertEquals( "Sol-diesis---",   Dict.getNote(32)  );
		assertEquals( "La---",           Dict.getNote(33)  );
		assertEquals( "La-diesis---",    Dict.getNote(34)  );
		assertEquals( "Si---",           Dict.getNote(35)  );
		assertEquals( "Do--",            Dict.getNote(36)  );
		assertEquals( "Do-diesis--",     Dict.getNote(37)  );
		assertEquals( "Re--",            Dict.getNote(38)  );
		assertEquals( "Re-diesis--",     Dict.getNote(39)  );
		assertEquals( "Mi--",            Dict.getNote(40)  );
		assertEquals( "Fa--",            Dict.getNote(41)  );
		assertEquals( "Fa-diesis--",     Dict.getNote(42)  );
		assertEquals( "Sol--",           Dict.getNote(43)  );
		assertEquals( "Sol-diesis--",    Dict.getNote(44)  );
		assertEquals( "La--",            Dict.getNote(45)  );
		assertEquals( "La-diesis--",     Dict.getNote(46)  );
		assertEquals( "Si--",            Dict.getNote(47)  );
		assertEquals( "Do-",             Dict.getNote(48)  );
		assertEquals( "Do-diesis-",      Dict.getNote(49)  );
		assertEquals( "Re-",             Dict.getNote(50)  );
		assertEquals( "Re-diesis-",      Dict.getNote(51)  );
		assertEquals( "Mi-",             Dict.getNote(52)  );
		assertEquals( "Fa-",             Dict.getNote(53)  );
		assertEquals( "Fa-diesis-",      Dict.getNote(54)  );
		assertEquals( "Sol-",            Dict.getNote(55)  );
		assertEquals( "Sol-diesis-",     Dict.getNote(56)  );
		assertEquals( "La-",             Dict.getNote(57)  );
		assertEquals( "La-diesis-",      Dict.getNote(58)  );
		assertEquals( "Si-",             Dict.getNote(59)  );
		assertEquals( "Do",              Dict.getNote(60)  );
		assertEquals( "Do-diesis",       Dict.getNote(61)  );
		assertEquals( "Re",              Dict.getNote(62)  );
		assertEquals( "Re-diesis",       Dict.getNote(63)  );
		assertEquals( "Mi",              Dict.getNote(64)  );
		assertEquals( "Fa",              Dict.getNote(65)  );
		assertEquals( "Fa-diesis",       Dict.getNote(66)  );
		assertEquals( "Sol",             Dict.getNote(67)  );
		assertEquals( "Sol-diesis",      Dict.getNote(68)  );
		assertEquals( "La",              Dict.getNote(69)  );
		assertEquals( "La-diesis",       Dict.getNote(70)  );
		assertEquals( "Si",              Dict.getNote(71)  );
		assertEquals( "Do+",             Dict.getNote(72)  );
		assertEquals( "Do-diesis+",      Dict.getNote(73)  );
		assertEquals( "Re+",             Dict.getNote(74)  );
		assertEquals( "Re-diesis+",      Dict.getNote(75)  );
		assertEquals( "Mi+",             Dict.getNote(76)  );
		assertEquals( "Fa+",             Dict.getNote(77)  );
		assertEquals( "Fa-diesis+",      Dict.getNote(78)  );
		assertEquals( "Sol+",            Dict.getNote(79)  );
		assertEquals( "Sol-diesis+",     Dict.getNote(80)  );
		assertEquals( "La+",             Dict.getNote(81)  );
		assertEquals( "La-diesis+",      Dict.getNote(82)  );
		assertEquals( "Si+",             Dict.getNote(83)  );
		assertEquals( "Do++",            Dict.getNote(84)  );
		assertEquals( "Do-diesis++",     Dict.getNote(85)  );
		assertEquals( "Re++",            Dict.getNote(86)  );
		assertEquals( "Re-diesis++",     Dict.getNote(87)  );
		assertEquals( "Mi++",            Dict.getNote(88)  );
		assertEquals( "Fa++",            Dict.getNote(89)  );
		assertEquals( "Fa-diesis++",     Dict.getNote(90)  );
		assertEquals( "Sol++",           Dict.getNote(91)  );
		assertEquals( "Sol-diesis++",    Dict.getNote(92)  );
		assertEquals( "La++",            Dict.getNote(93)  );
		assertEquals( "La-diesis++",     Dict.getNote(94)  );
		assertEquals( "Si++",            Dict.getNote(95)  );
		assertEquals( "Do+++",           Dict.getNote(96)  );
		assertEquals( "Do-diesis+++",    Dict.getNote(97)  );
		assertEquals( "Re+++",           Dict.getNote(98)  );
		assertEquals( "Re-diesis+++",    Dict.getNote(99)  );
		assertEquals( "Mi+++",           Dict.getNote(100) );
		assertEquals( "Fa+++",           Dict.getNote(101) );
		assertEquals( "Fa-diesis+++",    Dict.getNote(102) );
		assertEquals( "Sol+++",          Dict.getNote(103) );
		assertEquals( "Sol-diesis+++",   Dict.getNote(104) );
		assertEquals( "La+++",           Dict.getNote(105) );
		assertEquals( "La-diesis+++",    Dict.getNote(106) );
		assertEquals( "Si+++",           Dict.getNote(107) );
		assertEquals( "Do++++",          Dict.getNote(108) );
		assertEquals( "Do-diesis++++",   Dict.getNote(109) );
		assertEquals( "Re++++",          Dict.getNote(110) );
		assertEquals( "Re-diesis++++",   Dict.getNote(111) );
		assertEquals( "Mi++++",          Dict.getNote(112) );
		assertEquals( "Fa++++",          Dict.getNote(113) );
		assertEquals( "Fa-diesis++++",   Dict.getNote(114) );
		assertEquals( "Sol++++",         Dict.getNote(115) );
		assertEquals( "Sol-diesis++++",  Dict.getNote(116) );
		assertEquals( "La++++",          Dict.getNote(117) );
		assertEquals( "La-diesis++++",   Dict.getNote(118) );
		assertEquals( "Si++++",          Dict.getNote(119) );
		assertEquals( "Do+++++",         Dict.getNote(120) );
		assertEquals( "Do-diesis+++++",  Dict.getNote(121) );
		assertEquals( "Re+++++",         Dict.getNote(122) );
		assertEquals( "Re-diesis+++++",  Dict.getNote(123) );
		assertEquals( "Mi+++++",         Dict.getNote(124) );
		assertEquals( "Fa+++++",         Dict.getNote(125) );
		assertEquals( "Fa-diesis+++++",  Dict.getNote(126) );
		assertEquals( "Sol+++++",        Dict.getNote(127) );
		
		// test international
		cbxs[3].setSelectedIndex(2);
		assertEquals( "Do-1",         Dict.getNote(0)   );
		assertEquals( "Do-diesis-1",  Dict.getNote(1)   );
		assertEquals( "Re-1",         Dict.getNote(2)   );
		assertEquals( "Re-diesis-1",  Dict.getNote(3)   );
		assertEquals( "Mi-1",         Dict.getNote(4)   );
		assertEquals( "Fa-1",         Dict.getNote(5)   );
		assertEquals( "Fa-diesis-1",  Dict.getNote(6)   );
		assertEquals( "Sol-1",        Dict.getNote(7)   );
		assertEquals( "Sol-diesis-1", Dict.getNote(8)   );
		assertEquals( "La-1",         Dict.getNote(9)   );
		assertEquals( "La-diesis-1",  Dict.getNote(10)  );
		assertEquals( "Si-1",         Dict.getNote(11)  );
		assertEquals( "Do0",          Dict.getNote(12)  );
		assertEquals( "Do-diesis0",   Dict.getNote(13)  );
		assertEquals( "Re0",          Dict.getNote(14)  );
		assertEquals( "Re-diesis0",   Dict.getNote(15)  );
		assertEquals( "Mi0",          Dict.getNote(16)  );
		assertEquals( "Fa0",          Dict.getNote(17)  );
		assertEquals( "Fa-diesis0",   Dict.getNote(18)  );
		assertEquals( "Sol0",         Dict.getNote(19)  );
		assertEquals( "Sol-diesis0",  Dict.getNote(20)  );
		assertEquals( "La0",          Dict.getNote(21)  );
		assertEquals( "La-diesis0",   Dict.getNote(22)  );
		assertEquals( "Si0",          Dict.getNote(23)  );
		assertEquals( "Do1",          Dict.getNote(24)  );
		assertEquals( "Do-diesis1",   Dict.getNote(25)  );
		assertEquals( "Re1",          Dict.getNote(26)  );
		assertEquals( "Re-diesis1",   Dict.getNote(27)  );
		assertEquals( "Mi1",          Dict.getNote(28)  );
		assertEquals( "Fa1",          Dict.getNote(29)  );
		assertEquals( "Fa-diesis1",   Dict.getNote(30)  );
		assertEquals( "Sol1",         Dict.getNote(31)  );
		assertEquals( "Sol-diesis1",  Dict.getNote(32)  );
		assertEquals( "La1",          Dict.getNote(33)  );
		assertEquals( "La-diesis1",   Dict.getNote(34)  );
		assertEquals( "Si1",          Dict.getNote(35)  );
		assertEquals( "Do2",          Dict.getNote(36)  );
		assertEquals( "Do-diesis2",   Dict.getNote(37)  );
		assertEquals( "Re2",          Dict.getNote(38)  );
		assertEquals( "Re-diesis2",   Dict.getNote(39)  );
		assertEquals( "Mi2",          Dict.getNote(40)  );
		assertEquals( "Fa2",          Dict.getNote(41)  );
		assertEquals( "Fa-diesis2",   Dict.getNote(42)  );
		assertEquals( "Sol2",         Dict.getNote(43)  );
		assertEquals( "Sol-diesis2",  Dict.getNote(44)  );
		assertEquals( "La2",          Dict.getNote(45)  );
		assertEquals( "La-diesis2",   Dict.getNote(46)  );
		assertEquals( "Si2",          Dict.getNote(47)  );
		assertEquals( "Do3",          Dict.getNote(48)  );
		assertEquals( "Do-diesis3",   Dict.getNote(49)  );
		assertEquals( "Re3",          Dict.getNote(50)  );
		assertEquals( "Re-diesis3",   Dict.getNote(51)  );
		assertEquals( "Mi3",          Dict.getNote(52)  );
		assertEquals( "Fa3",          Dict.getNote(53)  );
		assertEquals( "Fa-diesis3",   Dict.getNote(54)  );
		assertEquals( "Sol3",         Dict.getNote(55)  );
		assertEquals( "Sol-diesis3",  Dict.getNote(56)  );
		assertEquals( "La3",          Dict.getNote(57)  );
		assertEquals( "La-diesis3",   Dict.getNote(58)  );
		assertEquals( "Si3",          Dict.getNote(59)  );
		assertEquals( "Do4",          Dict.getNote(60)  );
		assertEquals( "Do-diesis4",   Dict.getNote(61)  );
		assertEquals( "Re4",          Dict.getNote(62)  );
		assertEquals( "Re-diesis4",   Dict.getNote(63)  );
		assertEquals( "Mi4",          Dict.getNote(64)  );
		assertEquals( "Fa4",          Dict.getNote(65)  );
		assertEquals( "Fa-diesis4",   Dict.getNote(66)  );
		assertEquals( "Sol4",         Dict.getNote(67)  );
		assertEquals( "Sol-diesis4",  Dict.getNote(68)  );
		assertEquals( "La4",          Dict.getNote(69)  );
		assertEquals( "La-diesis4",   Dict.getNote(70)  );
		assertEquals( "Si4",          Dict.getNote(71)  );
		assertEquals( "Do5",          Dict.getNote(72)  );
		assertEquals( "Do-diesis5",   Dict.getNote(73)  );
		assertEquals( "Re5",          Dict.getNote(74)  );
		assertEquals( "Re-diesis5",   Dict.getNote(75)  );
		assertEquals( "Mi5",          Dict.getNote(76)  );
		assertEquals( "Fa5",          Dict.getNote(77)  );
		assertEquals( "Fa-diesis5",   Dict.getNote(78)  );
		assertEquals( "Sol5",         Dict.getNote(79)  );
		assertEquals( "Sol-diesis5",  Dict.getNote(80)  );
		assertEquals( "La5",          Dict.getNote(81)  );
		assertEquals( "La-diesis5",   Dict.getNote(82)  );
		assertEquals( "Si5",          Dict.getNote(83)  );
		assertEquals( "Do6",          Dict.getNote(84)  );
		assertEquals( "Do-diesis6",   Dict.getNote(85)  );
		assertEquals( "Re6",          Dict.getNote(86)  );
		assertEquals( "Re-diesis6",   Dict.getNote(87)  );
		assertEquals( "Mi6",          Dict.getNote(88)  );
		assertEquals( "Fa6",          Dict.getNote(89)  );
		assertEquals( "Fa-diesis6",   Dict.getNote(90)  );
		assertEquals( "Sol6",         Dict.getNote(91)  );
		assertEquals( "Sol-diesis6",  Dict.getNote(92)  );
		assertEquals( "La6",          Dict.getNote(93)  );
		assertEquals( "La-diesis6",   Dict.getNote(94)  );
		assertEquals( "Si6",          Dict.getNote(95)  );
		assertEquals( "Do7",          Dict.getNote(96)  );
		assertEquals( "Do-diesis7",   Dict.getNote(97)  );
		assertEquals( "Re7",          Dict.getNote(98)  );
		assertEquals( "Re-diesis7",   Dict.getNote(99)  );
		assertEquals( "Mi7",          Dict.getNote(100) );
		assertEquals( "Fa7",          Dict.getNote(101) );
		assertEquals( "Fa-diesis7",   Dict.getNote(102) );
		assertEquals( "Sol7",         Dict.getNote(103) );
		assertEquals( "Sol-diesis7",  Dict.getNote(104) );
		assertEquals( "La7",          Dict.getNote(105) );
		assertEquals( "La-diesis7",   Dict.getNote(106) );
		assertEquals( "Si7",          Dict.getNote(107) );
		assertEquals( "Do8",          Dict.getNote(108) );
		assertEquals( "Do-diesis8",   Dict.getNote(109) );
		assertEquals( "Re8",          Dict.getNote(110) );
		assertEquals( "Re-diesis8",   Dict.getNote(111) );
		assertEquals( "Mi8",          Dict.getNote(112) );
		assertEquals( "Fa8",          Dict.getNote(113) );
		assertEquals( "Fa-diesis8",   Dict.getNote(114) );
		assertEquals( "Sol8",         Dict.getNote(115) );
		assertEquals( "Sol-diesis8",  Dict.getNote(116) );
		assertEquals( "La8",          Dict.getNote(117) );
		assertEquals( "La-diesis8",   Dict.getNote(118) );
		assertEquals( "Si8",          Dict.getNote(119) );
		assertEquals( "Do9",          Dict.getNote(120) );
		assertEquals( "Do-diesis9",   Dict.getNote(121) );
		assertEquals( "Re9",          Dict.getNote(122) );
		assertEquals( "Re-diesis9",   Dict.getNote(123) );
		assertEquals( "Mi9",          Dict.getNote(124) );
		assertEquals( "Fa9",          Dict.getNote(125) );
		assertEquals( "Fa-diesis9",   Dict.getNote(126) );
		assertEquals( "Sol9",         Dict.getNote(127) );
		
		// test german
		cbxs[3].setSelectedIndex(3);
		assertEquals( "Do'''",           Dict.getNote(0)   );
		assertEquals( "Do-diesis'''",    Dict.getNote(1)   );
		assertEquals( "Re'''",           Dict.getNote(2)   );
		assertEquals( "Re-diesis'''",    Dict.getNote(3)   );
		assertEquals( "Mi'''",           Dict.getNote(4)   );
		assertEquals( "Fa'''",           Dict.getNote(5)   );
		assertEquals( "Fa-diesis'''",    Dict.getNote(6)   );
		assertEquals( "Sol'''",          Dict.getNote(7)   );
		assertEquals( "Sol-diesis'''",   Dict.getNote(8)   );
		assertEquals( "La'''",           Dict.getNote(9)   );
		assertEquals( "La-diesis'''",    Dict.getNote(10)  );
		assertEquals( "Si'''",           Dict.getNote(11)  );
		assertEquals( "Do''",            Dict.getNote(12)  );
		assertEquals( "Do-diesis''",     Dict.getNote(13)  );
		assertEquals( "Re''",            Dict.getNote(14)  );
		assertEquals( "Re-diesis''",     Dict.getNote(15)  );
		assertEquals( "Mi''",            Dict.getNote(16)  );
		assertEquals( "Fa''",            Dict.getNote(17)  );
		assertEquals( "Fa-diesis''",     Dict.getNote(18)  );
		assertEquals( "Sol''",           Dict.getNote(19)  );
		assertEquals( "Sol-diesis''",    Dict.getNote(20)  );
		assertEquals( "La''",            Dict.getNote(21)  );
		assertEquals( "La-diesis''",     Dict.getNote(22)  );
		assertEquals( "Si''",            Dict.getNote(23)  );
		assertEquals( "Do'",             Dict.getNote(24)  );
		assertEquals( "Do-diesis'",      Dict.getNote(25)  );
		assertEquals( "Re'",             Dict.getNote(26)  );
		assertEquals( "Re-diesis'",      Dict.getNote(27)  );
		assertEquals( "Mi'",             Dict.getNote(28)  );
		assertEquals( "Fa'",             Dict.getNote(29)  );
		assertEquals( "Fa-diesis'",      Dict.getNote(30)  );
		assertEquals( "Sol'",            Dict.getNote(31)  );
		assertEquals( "Sol-diesis'",     Dict.getNote(32)  );
		assertEquals( "La'",             Dict.getNote(33)  );
		assertEquals( "La-diesis'",      Dict.getNote(34)  );
		assertEquals( "Si'",             Dict.getNote(35)  );
		assertEquals( "Do",              Dict.getNote(36)  );
		assertEquals( "Do-diesis",       Dict.getNote(37)  );
		assertEquals( "Re",              Dict.getNote(38)  );
		assertEquals( "Re-diesis",       Dict.getNote(39)  );
		assertEquals( "Mi",              Dict.getNote(40)  );
		assertEquals( "Fa",              Dict.getNote(41)  );
		assertEquals( "Fa-diesis",       Dict.getNote(42)  );
		assertEquals( "Sol",             Dict.getNote(43)  );
		assertEquals( "Sol-diesis",      Dict.getNote(44)  );
		assertEquals( "La",              Dict.getNote(45)  );
		assertEquals( "La-diesis",       Dict.getNote(46)  );
		assertEquals( "Si",              Dict.getNote(47)  );
		assertEquals( "do",              Dict.getNote(48)  );
		assertEquals( "do-diesis",       Dict.getNote(49)  );
		assertEquals( "re",              Dict.getNote(50)  );
		assertEquals( "re-diesis",       Dict.getNote(51)  );
		assertEquals( "mi",              Dict.getNote(52)  );
		assertEquals( "fa",              Dict.getNote(53)  );
		assertEquals( "fa-diesis",       Dict.getNote(54)  );
		assertEquals( "sol",             Dict.getNote(55)  );
		assertEquals( "sol-diesis",      Dict.getNote(56)  );
		assertEquals( "la",              Dict.getNote(57)  );
		assertEquals( "la-diesis",       Dict.getNote(58)  );
		assertEquals( "si",              Dict.getNote(59)  );
		assertEquals( "do'",             Dict.getNote(60)  );
		assertEquals( "do-diesis'",      Dict.getNote(61)  );
		assertEquals( "re'",             Dict.getNote(62)  );
		assertEquals( "re-diesis'",      Dict.getNote(63)  );
		assertEquals( "mi'",             Dict.getNote(64)  );
		assertEquals( "fa'",             Dict.getNote(65)  );
		assertEquals( "fa-diesis'",      Dict.getNote(66)  );
		assertEquals( "sol'",            Dict.getNote(67)  );
		assertEquals( "sol-diesis'",     Dict.getNote(68)  );
		assertEquals( "la'",             Dict.getNote(69)  );
		assertEquals( "la-diesis'",      Dict.getNote(70)  );
		assertEquals( "si'",             Dict.getNote(71)  );
		assertEquals( "do''",            Dict.getNote(72)  );
		assertEquals( "do-diesis''",     Dict.getNote(73)  );
		assertEquals( "re''",            Dict.getNote(74)  );
		assertEquals( "re-diesis''",     Dict.getNote(75)  );
		assertEquals( "mi''",            Dict.getNote(76)  );
		assertEquals( "fa''",            Dict.getNote(77)  );
		assertEquals( "fa-diesis''",     Dict.getNote(78)  );
		assertEquals( "sol''",           Dict.getNote(79)  );
		assertEquals( "sol-diesis''",    Dict.getNote(80)  );
		assertEquals( "la''",            Dict.getNote(81)  );
		assertEquals( "la-diesis''",     Dict.getNote(82)  );
		assertEquals( "si''",            Dict.getNote(83)  );
		assertEquals( "do'''",           Dict.getNote(84)  );
		assertEquals( "do-diesis'''",    Dict.getNote(85)  );
		assertEquals( "re'''",           Dict.getNote(86)  );
		assertEquals( "re-diesis'''",    Dict.getNote(87)  );
		assertEquals( "mi'''",           Dict.getNote(88)  );
		assertEquals( "fa'''",           Dict.getNote(89)  );
		assertEquals( "fa-diesis'''",    Dict.getNote(90)  );
		assertEquals( "sol'''",          Dict.getNote(91)  );
		assertEquals( "sol-diesis'''",   Dict.getNote(92)  );
		assertEquals( "la'''",           Dict.getNote(93)  );
		assertEquals( "la-diesis'''",    Dict.getNote(94)  );
		assertEquals( "si'''",           Dict.getNote(95)  );
		assertEquals( "do''''",          Dict.getNote(96)  );
		assertEquals( "do-diesis''''",   Dict.getNote(97)  );
		assertEquals( "re''''",          Dict.getNote(98)  );
		assertEquals( "re-diesis''''",   Dict.getNote(99)  );
		assertEquals( "mi''''",          Dict.getNote(100) );
		assertEquals( "fa''''",          Dict.getNote(101) );
		assertEquals( "fa-diesis''''",   Dict.getNote(102) );
		assertEquals( "sol''''",         Dict.getNote(103) );
		assertEquals( "sol-diesis''''",  Dict.getNote(104) );
		assertEquals( "la''''",          Dict.getNote(105) );
		assertEquals( "la-diesis''''",   Dict.getNote(106) );
		assertEquals( "si''''",          Dict.getNote(107) );
		assertEquals( "do'''''",         Dict.getNote(108) );
		assertEquals( "do-diesis'''''",  Dict.getNote(109) );
		assertEquals( "re'''''",         Dict.getNote(110) );
		assertEquals( "re-diesis'''''",  Dict.getNote(111) );
		assertEquals( "mi'''''",         Dict.getNote(112) );
		assertEquals( "fa'''''",         Dict.getNote(113) );
		assertEquals( "fa-diesis'''''",  Dict.getNote(114) );
		assertEquals( "sol'''''",        Dict.getNote(115) );
		assertEquals( "sol-diesis'''''", Dict.getNote(116) );
		assertEquals( "la'''''",         Dict.getNote(117) );
		assertEquals( "la-diesis'''''",  Dict.getNote(118) );
		assertEquals( "si'''''",         Dict.getNote(119) );
		assertEquals( "do''''''",        Dict.getNote(120) );
		assertEquals( "do-diesis''''''", Dict.getNote(121) );
		assertEquals( "re''''''",        Dict.getNote(122) );
		assertEquals( "re-diesis''''''", Dict.getNote(123) );
		assertEquals( "mi''''''",        Dict.getNote(124) );
		assertEquals( "fa''''''",        Dict.getNote(125) );
		assertEquals( "fa-diesis''''''", Dict.getNote(126) );
		assertEquals( "sol''''''",       Dict.getNote(127) );
		
		// switch back to default
		cbxs[1].setSelectedIndex(0); 
		cbxs[2].setSelectedIndex(0);
		cbxs[3].setSelectedIndex(0);
	}
	
	/**
	 * Tests octave configurations.
	 * 
	 * Using the following config:
	 * 
	 * - note system: italian (lower-case)
	 * - half tone: bemolle
	 * - octave naming: all possibilities:
	 *     - +n/-n
	 *     - +/-
	 *     - international
	 *     - german
	 */
	@Test
	public void testOctaveSystemsItLcBemolle() {
		
		// switch config
		cbxs[1].setSelectedIndex(2); // italian (lower-case) 
		cbxs[2].setSelectedIndex(3); // bemolle
		
		// test default (+n/-n)
		assertEquals( "do-5",          Dict.getNote(0)   );
		assertEquals( "re-bemolle-5",  Dict.getNote(1)   );
		assertEquals( "re-5",          Dict.getNote(2)   );
		assertEquals( "mi-bemolle-5",  Dict.getNote(3)   );
		assertEquals( "mi-5",          Dict.getNote(4)   );
		assertEquals( "fa-5",          Dict.getNote(5)   );
		assertEquals( "sol-bemolle-5", Dict.getNote(6)   );
		assertEquals( "sol-5",         Dict.getNote(7)   );
		assertEquals( "la-bemolle-5",  Dict.getNote(8)   );
		assertEquals( "la-5",          Dict.getNote(9)   );
		assertEquals( "si-bemolle-5",  Dict.getNote(10)  );
		assertEquals( "si-5",          Dict.getNote(11)  );
		assertEquals( "do-4",          Dict.getNote(12)  );
		assertEquals( "re-bemolle-4",  Dict.getNote(13)  );
		assertEquals( "re-4",          Dict.getNote(14)  );
		assertEquals( "mi-bemolle-4",  Dict.getNote(15)  );
		assertEquals( "mi-4",          Dict.getNote(16)  );
		assertEquals( "fa-4",          Dict.getNote(17)  );
		assertEquals( "sol-bemolle-4", Dict.getNote(18)  );
		assertEquals( "sol-4",         Dict.getNote(19)  );
		assertEquals( "la-bemolle-4",  Dict.getNote(20)  );
		assertEquals( "la-4",          Dict.getNote(21)  );
		assertEquals( "si-bemolle-4",  Dict.getNote(22)  );
		assertEquals( "si-4",          Dict.getNote(23)  );
		assertEquals( "do-3",          Dict.getNote(24)  );
		assertEquals( "re-bemolle-3",  Dict.getNote(25)  );
		assertEquals( "re-3",          Dict.getNote(26)  );
		assertEquals( "mi-bemolle-3",  Dict.getNote(27)  );
		assertEquals( "mi-3",          Dict.getNote(28)  );
		assertEquals( "fa-3",          Dict.getNote(29)  );
		assertEquals( "sol-bemolle-3", Dict.getNote(30)  );
		assertEquals( "sol-3",         Dict.getNote(31)  );
		assertEquals( "la-bemolle-3",  Dict.getNote(32)  );
		assertEquals( "la-3",          Dict.getNote(33)  );
		assertEquals( "si-bemolle-3",  Dict.getNote(34)  );
		assertEquals( "si-3",          Dict.getNote(35)  );
		assertEquals( "do-2",          Dict.getNote(36)  );
		assertEquals( "re-bemolle-2",  Dict.getNote(37)  );
		assertEquals( "re-2",          Dict.getNote(38)  );
		assertEquals( "mi-bemolle-2",  Dict.getNote(39)  );
		assertEquals( "mi-2",          Dict.getNote(40)  );
		assertEquals( "fa-2",          Dict.getNote(41)  );
		assertEquals( "sol-bemolle-2", Dict.getNote(42)  );
		assertEquals( "sol-2",         Dict.getNote(43)  );
		assertEquals( "la-bemolle-2",  Dict.getNote(44)  );
		assertEquals( "la-2",          Dict.getNote(45)  );
		assertEquals( "si-bemolle-2",  Dict.getNote(46)  );
		assertEquals( "si-2",          Dict.getNote(47)  );
		assertEquals( "do-",           Dict.getNote(48)  );
		assertEquals( "re-bemolle-",   Dict.getNote(49)  );
		assertEquals( "re-",           Dict.getNote(50)  );
		assertEquals( "mi-bemolle-",   Dict.getNote(51)  );
		assertEquals( "mi-",           Dict.getNote(52)  );
		assertEquals( "fa-",           Dict.getNote(53)  );
		assertEquals( "sol-bemolle-",  Dict.getNote(54)  );
		assertEquals( "sol-",          Dict.getNote(55)  );
		assertEquals( "la-bemolle-",   Dict.getNote(56)  );
		assertEquals( "la-",           Dict.getNote(57)  );
		assertEquals( "si-bemolle-",   Dict.getNote(58)  );
		assertEquals( "si-",           Dict.getNote(59)  );
		assertEquals( "do",            Dict.getNote(60)  );
		assertEquals( "re-bemolle",    Dict.getNote(61)  );
		assertEquals( "re",            Dict.getNote(62)  );
		assertEquals( "mi-bemolle",    Dict.getNote(63)  );
		assertEquals( "mi",            Dict.getNote(64)  );
		assertEquals( "fa",            Dict.getNote(65)  );
		assertEquals( "sol-bemolle",   Dict.getNote(66)  );
		assertEquals( "sol",           Dict.getNote(67)  );
		assertEquals( "la-bemolle",    Dict.getNote(68)  );
		assertEquals( "la",            Dict.getNote(69)  );
		assertEquals( "si-bemolle",    Dict.getNote(70)  );
		assertEquals( "si",            Dict.getNote(71)  );
		assertEquals( "do+",           Dict.getNote(72)  );
		assertEquals( "re-bemolle+",   Dict.getNote(73)  );
		assertEquals( "re+",           Dict.getNote(74)  );
		assertEquals( "mi-bemolle+",   Dict.getNote(75)  );
		assertEquals( "mi+",           Dict.getNote(76)  );
		assertEquals( "fa+",           Dict.getNote(77)  );
		assertEquals( "sol-bemolle+",  Dict.getNote(78)  );
		assertEquals( "sol+",          Dict.getNote(79)  );
		assertEquals( "la-bemolle+",   Dict.getNote(80)  );
		assertEquals( "la+",           Dict.getNote(81)  );
		assertEquals( "si-bemolle+",   Dict.getNote(82)  );
		assertEquals( "si+",           Dict.getNote(83)  );
		assertEquals( "do+2",          Dict.getNote(84)  );
		assertEquals( "re-bemolle+2",  Dict.getNote(85)  );
		assertEquals( "re+2",          Dict.getNote(86)  );
		assertEquals( "mi-bemolle+2",  Dict.getNote(87)  );
		assertEquals( "mi+2",          Dict.getNote(88)  );
		assertEquals( "fa+2",          Dict.getNote(89)  );
		assertEquals( "sol-bemolle+2", Dict.getNote(90)  );
		assertEquals( "sol+2",         Dict.getNote(91)  );
		assertEquals( "la-bemolle+2",  Dict.getNote(92)  );
		assertEquals( "la+2",          Dict.getNote(93)  );
		assertEquals( "si-bemolle+2",  Dict.getNote(94)  );
		assertEquals( "si+2",          Dict.getNote(95)  );
		assertEquals( "do+3",          Dict.getNote(96)  );
		assertEquals( "re-bemolle+3",  Dict.getNote(97)  );
		assertEquals( "re+3",          Dict.getNote(98)  );
		assertEquals( "mi-bemolle+3",  Dict.getNote(99)  );
		assertEquals( "mi+3",          Dict.getNote(100) );
		assertEquals( "fa+3",          Dict.getNote(101) );
		assertEquals( "sol-bemolle+3", Dict.getNote(102) );
		assertEquals( "sol+3",         Dict.getNote(103) );
		assertEquals( "la-bemolle+3",  Dict.getNote(104) );
		assertEquals( "la+3",          Dict.getNote(105) );
		assertEquals( "si-bemolle+3",  Dict.getNote(106) );
		assertEquals( "si+3",          Dict.getNote(107) );
		assertEquals( "do+4",          Dict.getNote(108) );
		assertEquals( "re-bemolle+4",  Dict.getNote(109) );
		assertEquals( "re+4",          Dict.getNote(110) );
		assertEquals( "mi-bemolle+4",  Dict.getNote(111) );
		assertEquals( "mi+4",          Dict.getNote(112) );
		assertEquals( "fa+4",          Dict.getNote(113) );
		assertEquals( "sol-bemolle+4", Dict.getNote(114) );
		assertEquals( "sol+4",         Dict.getNote(115) );
		assertEquals( "la-bemolle+4",  Dict.getNote(116) );
		assertEquals( "la+4",          Dict.getNote(117) );
		assertEquals( "si-bemolle+4",  Dict.getNote(118) );
		assertEquals( "si+4",          Dict.getNote(119) );
		assertEquals( "do+5",          Dict.getNote(120) );
		assertEquals( "re-bemolle+5",  Dict.getNote(121) );
		assertEquals( "re+5",          Dict.getNote(122) );
		assertEquals( "mi-bemolle+5",  Dict.getNote(123) );
		assertEquals( "mi+5",          Dict.getNote(124) );
		assertEquals( "fa+5",          Dict.getNote(125) );
		assertEquals( "sol-bemolle+5", Dict.getNote(126) );
		assertEquals( "sol+5",         Dict.getNote(127) );
		
		// test +/-
		cbxs[3].setSelectedIndex(1);
		assertEquals( "do-----",          Dict.getNote(0)   );
		assertEquals( "re-bemolle-----",  Dict.getNote(1)   );
		assertEquals( "re-----",          Dict.getNote(2)   );
		assertEquals( "mi-bemolle-----",  Dict.getNote(3)   );
		assertEquals( "mi-----",          Dict.getNote(4)   );
		assertEquals( "fa-----",          Dict.getNote(5)   );
		assertEquals( "sol-bemolle-----", Dict.getNote(6)   );
		assertEquals( "sol-----",         Dict.getNote(7)   );
		assertEquals( "la-bemolle-----",  Dict.getNote(8)   );
		assertEquals( "la-----",          Dict.getNote(9)   );
		assertEquals( "si-bemolle-----",  Dict.getNote(10)  );
		assertEquals( "si-----",          Dict.getNote(11)  );
		assertEquals( "do----",           Dict.getNote(12)  );
		assertEquals( "re-bemolle----",   Dict.getNote(13)  );
		assertEquals( "re----",           Dict.getNote(14)  );
		assertEquals( "mi-bemolle----",   Dict.getNote(15)  );
		assertEquals( "mi----",           Dict.getNote(16)  );
		assertEquals( "fa----",           Dict.getNote(17)  );
		assertEquals( "sol-bemolle----",  Dict.getNote(18)  );
		assertEquals( "sol----",          Dict.getNote(19)  );
		assertEquals( "la-bemolle----",   Dict.getNote(20)  );
		assertEquals( "la----",           Dict.getNote(21)  );
		assertEquals( "si-bemolle----",   Dict.getNote(22)  );
		assertEquals( "si----",           Dict.getNote(23)  );
		assertEquals( "do---",            Dict.getNote(24)  );
		assertEquals( "re-bemolle---",    Dict.getNote(25)  );
		assertEquals( "re---",            Dict.getNote(26)  );
		assertEquals( "mi-bemolle---",    Dict.getNote(27)  );
		assertEquals( "mi---",            Dict.getNote(28)  );
		assertEquals( "fa---",            Dict.getNote(29)  );
		assertEquals( "sol-bemolle---",   Dict.getNote(30)  );
		assertEquals( "sol---",           Dict.getNote(31)  );
		assertEquals( "la-bemolle---",    Dict.getNote(32)  );
		assertEquals( "la---",            Dict.getNote(33)  );
		assertEquals( "si-bemolle---",    Dict.getNote(34)  );
		assertEquals( "si---",            Dict.getNote(35)  );
		assertEquals( "do--",             Dict.getNote(36)  );
		assertEquals( "re-bemolle--",     Dict.getNote(37)  );
		assertEquals( "re--",             Dict.getNote(38)  );
		assertEquals( "mi-bemolle--",     Dict.getNote(39)  );
		assertEquals( "mi--",             Dict.getNote(40)  );
		assertEquals( "fa--",             Dict.getNote(41)  );
		assertEquals( "sol-bemolle--",    Dict.getNote(42)  );
		assertEquals( "sol--",            Dict.getNote(43)  );
		assertEquals( "la-bemolle--",     Dict.getNote(44)  );
		assertEquals( "la--",             Dict.getNote(45)  );
		assertEquals( "si-bemolle--",     Dict.getNote(46)  );
		assertEquals( "si--",             Dict.getNote(47)  );
		assertEquals( "do-",              Dict.getNote(48)  );
		assertEquals( "re-bemolle-",      Dict.getNote(49)  );
		assertEquals( "re-",              Dict.getNote(50)  );
		assertEquals( "mi-bemolle-",      Dict.getNote(51)  );
		assertEquals( "mi-",              Dict.getNote(52)  );
		assertEquals( "fa-",              Dict.getNote(53)  );
		assertEquals( "sol-bemolle-",     Dict.getNote(54)  );
		assertEquals( "sol-",             Dict.getNote(55)  );
		assertEquals( "la-bemolle-",      Dict.getNote(56)  );
		assertEquals( "la-",              Dict.getNote(57)  );
		assertEquals( "si-bemolle-",      Dict.getNote(58)  );
		assertEquals( "si-",              Dict.getNote(59)  );
		assertEquals( "do",               Dict.getNote(60)  );
		assertEquals( "re-bemolle",       Dict.getNote(61)  );
		assertEquals( "re",               Dict.getNote(62)  );
		assertEquals( "mi-bemolle",       Dict.getNote(63)  );
		assertEquals( "mi",               Dict.getNote(64)  );
		assertEquals( "fa",               Dict.getNote(65)  );
		assertEquals( "sol-bemolle",      Dict.getNote(66)  );
		assertEquals( "sol",              Dict.getNote(67)  );
		assertEquals( "la-bemolle",       Dict.getNote(68)  );
		assertEquals( "la",               Dict.getNote(69)  );
		assertEquals( "si-bemolle",       Dict.getNote(70)  );
		assertEquals( "si",               Dict.getNote(71)  );
		assertEquals( "do+",              Dict.getNote(72)  );
		assertEquals( "re-bemolle+",      Dict.getNote(73)  );
		assertEquals( "re+",              Dict.getNote(74)  );
		assertEquals( "mi-bemolle+",      Dict.getNote(75)  );
		assertEquals( "mi+",              Dict.getNote(76)  );
		assertEquals( "fa+",              Dict.getNote(77)  );
		assertEquals( "sol-bemolle+",     Dict.getNote(78)  );
		assertEquals( "sol+",             Dict.getNote(79)  );
		assertEquals( "la-bemolle+",      Dict.getNote(80)  );
		assertEquals( "la+",              Dict.getNote(81)  );
		assertEquals( "si-bemolle+",      Dict.getNote(82)  );
		assertEquals( "si+",              Dict.getNote(83)  );
		assertEquals( "do++",             Dict.getNote(84)  );
		assertEquals( "re-bemolle++",     Dict.getNote(85)  );
		assertEquals( "re++",             Dict.getNote(86)  );
		assertEquals( "mi-bemolle++",     Dict.getNote(87)  );
		assertEquals( "mi++",             Dict.getNote(88)  );
		assertEquals( "fa++",             Dict.getNote(89)  );
		assertEquals( "sol-bemolle++",    Dict.getNote(90)  );
		assertEquals( "sol++",            Dict.getNote(91)  );
		assertEquals( "la-bemolle++",     Dict.getNote(92)  );
		assertEquals( "la++",             Dict.getNote(93)  );
		assertEquals( "si-bemolle++",     Dict.getNote(94)  );
		assertEquals( "si++",             Dict.getNote(95)  );
		assertEquals( "do+++",            Dict.getNote(96)  );
		assertEquals( "re-bemolle+++",    Dict.getNote(97)  );
		assertEquals( "re+++",            Dict.getNote(98)  );
		assertEquals( "mi-bemolle+++",    Dict.getNote(99)  );
		assertEquals( "mi+++",            Dict.getNote(100) );
		assertEquals( "fa+++",            Dict.getNote(101) );
		assertEquals( "sol-bemolle+++",   Dict.getNote(102) );
		assertEquals( "sol+++",           Dict.getNote(103) );
		assertEquals( "la-bemolle+++",    Dict.getNote(104) );
		assertEquals( "la+++",            Dict.getNote(105) );
		assertEquals( "si-bemolle+++",    Dict.getNote(106) );
		assertEquals( "si+++",            Dict.getNote(107) );
		assertEquals( "do++++",           Dict.getNote(108) );
		assertEquals( "re-bemolle++++",   Dict.getNote(109) );
		assertEquals( "re++++",           Dict.getNote(110) );
		assertEquals( "mi-bemolle++++",   Dict.getNote(111) );
		assertEquals( "mi++++",           Dict.getNote(112) );
		assertEquals( "fa++++",           Dict.getNote(113) );
		assertEquals( "sol-bemolle++++",  Dict.getNote(114) );
		assertEquals( "sol++++",          Dict.getNote(115) );
		assertEquals( "la-bemolle++++",   Dict.getNote(116) );
		assertEquals( "la++++",           Dict.getNote(117) );
		assertEquals( "si-bemolle++++",   Dict.getNote(118) );
		assertEquals( "si++++",           Dict.getNote(119) );
		assertEquals( "do+++++",          Dict.getNote(120) );
		assertEquals( "re-bemolle+++++",  Dict.getNote(121) );
		assertEquals( "re+++++",          Dict.getNote(122) );
		assertEquals( "mi-bemolle+++++",  Dict.getNote(123) );
		assertEquals( "mi+++++",          Dict.getNote(124) );
		assertEquals( "fa+++++",          Dict.getNote(125) );
		assertEquals( "sol-bemolle+++++", Dict.getNote(126) );
		assertEquals( "sol+++++",         Dict.getNote(127) );
		
		// test international
		cbxs[3].setSelectedIndex(2);
		assertEquals( "do-1",          Dict.getNote(0)   );
		assertEquals( "re-bemolle-1",  Dict.getNote(1)   );
		assertEquals( "re-1",          Dict.getNote(2)   );
		assertEquals( "mi-bemolle-1",  Dict.getNote(3)   );
		assertEquals( "mi-1",          Dict.getNote(4)   );
		assertEquals( "fa-1",          Dict.getNote(5)   );
		assertEquals( "sol-bemolle-1", Dict.getNote(6)   );
		assertEquals( "sol-1",         Dict.getNote(7)   );
		assertEquals( "la-bemolle-1",  Dict.getNote(8)   );
		assertEquals( "la-1",          Dict.getNote(9)   );
		assertEquals( "si-bemolle-1",  Dict.getNote(10)  );
		assertEquals( "si-1",          Dict.getNote(11)  );
		assertEquals( "do0",           Dict.getNote(12)  );
		assertEquals( "re-bemolle0",   Dict.getNote(13)  );
		assertEquals( "re0",           Dict.getNote(14)  );
		assertEquals( "mi-bemolle0",   Dict.getNote(15)  );
		assertEquals( "mi0",           Dict.getNote(16)  );
		assertEquals( "fa0",           Dict.getNote(17)  );
		assertEquals( "sol-bemolle0",  Dict.getNote(18)  );
		assertEquals( "sol0",          Dict.getNote(19)  );
		assertEquals( "la-bemolle0",   Dict.getNote(20)  );
		assertEquals( "la0",           Dict.getNote(21)  );
		assertEquals( "si-bemolle0",   Dict.getNote(22)  );
		assertEquals( "si0",           Dict.getNote(23)  );
		assertEquals( "do1",           Dict.getNote(24)  );
		assertEquals( "re-bemolle1",   Dict.getNote(25)  );
		assertEquals( "re1",           Dict.getNote(26)  );
		assertEquals( "mi-bemolle1",   Dict.getNote(27)  );
		assertEquals( "mi1",           Dict.getNote(28)  );
		assertEquals( "fa1",           Dict.getNote(29)  );
		assertEquals( "sol-bemolle1",  Dict.getNote(30)  );
		assertEquals( "sol1",          Dict.getNote(31)  );
		assertEquals( "la-bemolle1",   Dict.getNote(32)  );
		assertEquals( "la1",           Dict.getNote(33)  );
		assertEquals( "si-bemolle1",   Dict.getNote(34)  );
		assertEquals( "si1",           Dict.getNote(35)  );
		assertEquals( "do2",           Dict.getNote(36)  );
		assertEquals( "re-bemolle2",   Dict.getNote(37)  );
		assertEquals( "re2",           Dict.getNote(38)  );
		assertEquals( "mi-bemolle2",   Dict.getNote(39)  );
		assertEquals( "mi2",           Dict.getNote(40)  );
		assertEquals( "fa2",           Dict.getNote(41)  );
		assertEquals( "sol-bemolle2",  Dict.getNote(42)  );
		assertEquals( "sol2",          Dict.getNote(43)  );
		assertEquals( "la-bemolle2",   Dict.getNote(44)  );
		assertEquals( "la2",           Dict.getNote(45)  );
		assertEquals( "si-bemolle2",   Dict.getNote(46)  );
		assertEquals( "si2",           Dict.getNote(47)  );
		assertEquals( "do3",           Dict.getNote(48)  );
		assertEquals( "re-bemolle3",   Dict.getNote(49)  );
		assertEquals( "re3",           Dict.getNote(50)  );
		assertEquals( "mi-bemolle3",   Dict.getNote(51)  );
		assertEquals( "mi3",           Dict.getNote(52)  );
		assertEquals( "fa3",           Dict.getNote(53)  );
		assertEquals( "sol-bemolle3",  Dict.getNote(54)  );
		assertEquals( "sol3",          Dict.getNote(55)  );
		assertEquals( "la-bemolle3",   Dict.getNote(56)  );
		assertEquals( "la3",           Dict.getNote(57)  );
		assertEquals( "si-bemolle3",   Dict.getNote(58)  );
		assertEquals( "si3",           Dict.getNote(59)  );
		assertEquals( "do4",           Dict.getNote(60)  );
		assertEquals( "re-bemolle4",   Dict.getNote(61)  );
		assertEquals( "re4",           Dict.getNote(62)  );
		assertEquals( "mi-bemolle4",   Dict.getNote(63)  );
		assertEquals( "mi4",           Dict.getNote(64)  );
		assertEquals( "fa4",           Dict.getNote(65)  );
		assertEquals( "sol-bemolle4",  Dict.getNote(66)  );
		assertEquals( "sol4",          Dict.getNote(67)  );
		assertEquals( "la-bemolle4",   Dict.getNote(68)  );
		assertEquals( "la4",           Dict.getNote(69)  );
		assertEquals( "si-bemolle4",   Dict.getNote(70)  );
		assertEquals( "si4",           Dict.getNote(71)  );
		assertEquals( "do5",           Dict.getNote(72)  );
		assertEquals( "re-bemolle5",   Dict.getNote(73)  );
		assertEquals( "re5",           Dict.getNote(74)  );
		assertEquals( "mi-bemolle5",   Dict.getNote(75)  );
		assertEquals( "mi5",           Dict.getNote(76)  );
		assertEquals( "fa5",           Dict.getNote(77)  );
		assertEquals( "sol-bemolle5",  Dict.getNote(78)  );
		assertEquals( "sol5",          Dict.getNote(79)  );
		assertEquals( "la-bemolle5",   Dict.getNote(80)  );
		assertEquals( "la5",           Dict.getNote(81)  );
		assertEquals( "si-bemolle5",   Dict.getNote(82)  );
		assertEquals( "si5",           Dict.getNote(83)  );
		assertEquals( "do6",           Dict.getNote(84)  );
		assertEquals( "re-bemolle6",   Dict.getNote(85)  );
		assertEquals( "re6",           Dict.getNote(86)  );
		assertEquals( "mi-bemolle6",   Dict.getNote(87)  );
		assertEquals( "mi6",           Dict.getNote(88)  );
		assertEquals( "fa6",           Dict.getNote(89)  );
		assertEquals( "sol-bemolle6",  Dict.getNote(90)  );
		assertEquals( "sol6",          Dict.getNote(91)  );
		assertEquals( "la-bemolle6",   Dict.getNote(92)  );
		assertEquals( "la6",           Dict.getNote(93)  );
		assertEquals( "si-bemolle6",   Dict.getNote(94)  );
		assertEquals( "si6",           Dict.getNote(95)  );
		assertEquals( "do7",           Dict.getNote(96)  );
		assertEquals( "re-bemolle7",   Dict.getNote(97)  );
		assertEquals( "re7",           Dict.getNote(98)  );
		assertEquals( "mi-bemolle7",   Dict.getNote(99)  );
		assertEquals( "mi7",           Dict.getNote(100) );
		assertEquals( "fa7",           Dict.getNote(101) );
		assertEquals( "sol-bemolle7",  Dict.getNote(102) );
		assertEquals( "sol7",          Dict.getNote(103) );
		assertEquals( "la-bemolle7",   Dict.getNote(104) );
		assertEquals( "la7",           Dict.getNote(105) );
		assertEquals( "si-bemolle7",   Dict.getNote(106) );
		assertEquals( "si7",           Dict.getNote(107) );
		assertEquals( "do8",           Dict.getNote(108) );
		assertEquals( "re-bemolle8",   Dict.getNote(109) );
		assertEquals( "re8",           Dict.getNote(110) );
		assertEquals( "mi-bemolle8",   Dict.getNote(111) );
		assertEquals( "mi8",           Dict.getNote(112) );
		assertEquals( "fa8",           Dict.getNote(113) );
		assertEquals( "sol-bemolle8",  Dict.getNote(114) );
		assertEquals( "sol8",          Dict.getNote(115) );
		assertEquals( "la-bemolle8",   Dict.getNote(116) );
		assertEquals( "la8",           Dict.getNote(117) );
		assertEquals( "si-bemolle8",   Dict.getNote(118) );
		assertEquals( "si8",           Dict.getNote(119) );
		assertEquals( "do9",           Dict.getNote(120) );
		assertEquals( "re-bemolle9",   Dict.getNote(121) );
		assertEquals( "re9",           Dict.getNote(122) );
		assertEquals( "mi-bemolle9",   Dict.getNote(123) );
		assertEquals( "mi9",           Dict.getNote(124) );
		assertEquals( "fa9",           Dict.getNote(125) );
		assertEquals( "sol-bemolle9",  Dict.getNote(126) );
		assertEquals( "sol9",          Dict.getNote(127) );
		
		// test german
		cbxs[3].setSelectedIndex(3);
		assertEquals( "Do'''",             Dict.getNote(0)   );
		assertEquals( "Re-bemolle'''",     Dict.getNote(1)   );
		assertEquals( "Re'''",             Dict.getNote(2)   );
		assertEquals( "Mi-bemolle'''",     Dict.getNote(3)   );
		assertEquals( "Mi'''",             Dict.getNote(4)   );
		assertEquals( "Fa'''",             Dict.getNote(5)   );
		assertEquals( "Sol-bemolle'''",    Dict.getNote(6)   );
		assertEquals( "Sol'''",            Dict.getNote(7)   );
		assertEquals( "La-bemolle'''",     Dict.getNote(8)   );
		assertEquals( "La'''",             Dict.getNote(9)   );
		assertEquals( "Si-bemolle'''",     Dict.getNote(10)  );
		assertEquals( "Si'''",             Dict.getNote(11)  );
		assertEquals( "Do''",              Dict.getNote(12)  );
		assertEquals( "Re-bemolle''",      Dict.getNote(13)  );
		assertEquals( "Re''",              Dict.getNote(14)  );
		assertEquals( "Mi-bemolle''",      Dict.getNote(15)  );
		assertEquals( "Mi''",              Dict.getNote(16)  );
		assertEquals( "Fa''",              Dict.getNote(17)  );
		assertEquals( "Sol-bemolle''",     Dict.getNote(18)  );
		assertEquals( "Sol''",             Dict.getNote(19)  );
		assertEquals( "La-bemolle''",      Dict.getNote(20)  );
		assertEquals( "La''",              Dict.getNote(21)  );
		assertEquals( "Si-bemolle''",      Dict.getNote(22)  );
		assertEquals( "Si''",              Dict.getNote(23)  );
		assertEquals( "Do'",               Dict.getNote(24)  );
		assertEquals( "Re-bemolle'",       Dict.getNote(25)  );
		assertEquals( "Re'",               Dict.getNote(26)  );
		assertEquals( "Mi-bemolle'",       Dict.getNote(27)  );
		assertEquals( "Mi'",               Dict.getNote(28)  );
		assertEquals( "Fa'",               Dict.getNote(29)  );
		assertEquals( "Sol-bemolle'",      Dict.getNote(30)  );
		assertEquals( "Sol'",              Dict.getNote(31)  );
		assertEquals( "La-bemolle'",       Dict.getNote(32)  );
		assertEquals( "La'",               Dict.getNote(33)  );
		assertEquals( "Si-bemolle'",       Dict.getNote(34)  );
		assertEquals( "Si'",               Dict.getNote(35)  );
		assertEquals( "Do",                Dict.getNote(36)  );
		assertEquals( "Re-bemolle",        Dict.getNote(37)  );
		assertEquals( "Re",                Dict.getNote(38)  );
		assertEquals( "Mi-bemolle",        Dict.getNote(39)  );
		assertEquals( "Mi",                Dict.getNote(40)  );
		assertEquals( "Fa",                Dict.getNote(41)  );
		assertEquals( "Sol-bemolle",       Dict.getNote(42)  );
		assertEquals( "Sol",               Dict.getNote(43)  );
		assertEquals( "La-bemolle",        Dict.getNote(44)  );
		assertEquals( "La",                Dict.getNote(45)  );
		assertEquals( "Si-bemolle",        Dict.getNote(46)  );
		assertEquals( "Si",                Dict.getNote(47)  );
		assertEquals( "do",                Dict.getNote(48)  );
		assertEquals( "re-bemolle",        Dict.getNote(49)  );
		assertEquals( "re",                Dict.getNote(50)  );
		assertEquals( "mi-bemolle",        Dict.getNote(51)  );
		assertEquals( "mi",                Dict.getNote(52)  );
		assertEquals( "fa",                Dict.getNote(53)  );
		assertEquals( "sol-bemolle",       Dict.getNote(54)  );
		assertEquals( "sol",               Dict.getNote(55)  );
		assertEquals( "la-bemolle",        Dict.getNote(56)  );
		assertEquals( "la",                Dict.getNote(57)  );
		assertEquals( "si-bemolle",        Dict.getNote(58)  );
		assertEquals( "si",                Dict.getNote(59)  );
		assertEquals( "do'",               Dict.getNote(60)  );
		assertEquals( "re-bemolle'",       Dict.getNote(61)  );
		assertEquals( "re'",               Dict.getNote(62)  );
		assertEquals( "mi-bemolle'",       Dict.getNote(63)  );
		assertEquals( "mi'",               Dict.getNote(64)  );
		assertEquals( "fa'",               Dict.getNote(65)  );
		assertEquals( "sol-bemolle'",      Dict.getNote(66)  );
		assertEquals( "sol'",              Dict.getNote(67)  );
		assertEquals( "la-bemolle'",       Dict.getNote(68)  );
		assertEquals( "la'",               Dict.getNote(69)  );
		assertEquals( "si-bemolle'",       Dict.getNote(70)  );
		assertEquals( "si'",               Dict.getNote(71)  );
		assertEquals( "do''",              Dict.getNote(72)  );
		assertEquals( "re-bemolle''",      Dict.getNote(73)  );
		assertEquals( "re''",              Dict.getNote(74)  );
		assertEquals( "mi-bemolle''",      Dict.getNote(75)  );
		assertEquals( "mi''",              Dict.getNote(76)  );
		assertEquals( "fa''",              Dict.getNote(77)  );
		assertEquals( "sol-bemolle''",     Dict.getNote(78)  );
		assertEquals( "sol''",             Dict.getNote(79)  );
		assertEquals( "la-bemolle''",      Dict.getNote(80)  );
		assertEquals( "la''",              Dict.getNote(81)  );
		assertEquals( "si-bemolle''",      Dict.getNote(82)  );
		assertEquals( "si''",              Dict.getNote(83)  );
		assertEquals( "do'''",             Dict.getNote(84)  );
		assertEquals( "re-bemolle'''",     Dict.getNote(85)  );
		assertEquals( "re'''",             Dict.getNote(86)  );
		assertEquals( "mi-bemolle'''",     Dict.getNote(87)  );
		assertEquals( "mi'''",             Dict.getNote(88)  );
		assertEquals( "fa'''",             Dict.getNote(89)  );
		assertEquals( "sol-bemolle'''",    Dict.getNote(90)  );
		assertEquals( "sol'''",            Dict.getNote(91)  );
		assertEquals( "la-bemolle'''",     Dict.getNote(92)  );
		assertEquals( "la'''",             Dict.getNote(93)  );
		assertEquals( "si-bemolle'''",     Dict.getNote(94)  );
		assertEquals( "si'''",             Dict.getNote(95)  );
		assertEquals( "do''''",            Dict.getNote(96)  );
		assertEquals( "re-bemolle''''",    Dict.getNote(97)  );
		assertEquals( "re''''",            Dict.getNote(98)  );
		assertEquals( "mi-bemolle''''",    Dict.getNote(99)  );
		assertEquals( "mi''''",            Dict.getNote(100) );
		assertEquals( "fa''''",            Dict.getNote(101) );
		assertEquals( "sol-bemolle''''",   Dict.getNote(102) );
		assertEquals( "sol''''",           Dict.getNote(103) );
		assertEquals( "la-bemolle''''",    Dict.getNote(104) );
		assertEquals( "la''''",            Dict.getNote(105) );
		assertEquals( "si-bemolle''''",    Dict.getNote(106) );
		assertEquals( "si''''",            Dict.getNote(107) );
		assertEquals( "do'''''",           Dict.getNote(108) );
		assertEquals( "re-bemolle'''''",   Dict.getNote(109) );
		assertEquals( "re'''''",           Dict.getNote(110) );
		assertEquals( "mi-bemolle'''''",   Dict.getNote(111) );
		assertEquals( "mi'''''",           Dict.getNote(112) );
		assertEquals( "fa'''''",           Dict.getNote(113) );
		assertEquals( "sol-bemolle'''''",  Dict.getNote(114) );
		assertEquals( "sol'''''",          Dict.getNote(115) );
		assertEquals( "la-bemolle'''''",   Dict.getNote(116) );
		assertEquals( "la'''''",           Dict.getNote(117) );
		assertEquals( "si-bemolle'''''",   Dict.getNote(118) );
		assertEquals( "si'''''",           Dict.getNote(119) );
		assertEquals( "do''''''",          Dict.getNote(120) );
		assertEquals( "re-bemolle''''''",  Dict.getNote(121) );
		assertEquals( "re''''''",          Dict.getNote(122) );
		assertEquals( "mi-bemolle''''''",  Dict.getNote(123) );
		assertEquals( "mi''''''",          Dict.getNote(124) );
		assertEquals( "fa''''''",          Dict.getNote(125) );
		assertEquals( "sol-bemolle''''''", Dict.getNote(126) );
		assertEquals( "sol''''''",         Dict.getNote(127) );
		
		// switch back to default
		cbxs[1].setSelectedIndex(0); 
		cbxs[2].setSelectedIndex(0);
		cbxs[3].setSelectedIndex(0);
	}
	
	/**
	 * Tests octave configurations.
	 * 
	 * Using the following config:
	 * 
	 * - note system: german (lower-case)
	 * - half tone: -is
	 * - octave naming: all possibilities:
	 *     - +n/-n
	 *     - +/-
	 *     - international
	 *     - german
	 */
	@Test
	public void testOctaveSystemsGerLcCis() {
		
		// switch config
		cbxs[1].setSelectedIndex(4); // german (lower-case)
		cbxs[2].setSelectedIndex(4); // -is
		
		// test default (+n/-n)
		assertEquals( "c-5",   Dict.getNote(0)   );
		assertEquals( "cis-5", Dict.getNote(1)   );
		assertEquals( "d-5",   Dict.getNote(2)   );
		assertEquals( "dis-5", Dict.getNote(3)   );
		assertEquals( "e-5",   Dict.getNote(4)   );
		assertEquals( "f-5",   Dict.getNote(5)   );
		assertEquals( "fis-5", Dict.getNote(6)   );
		assertEquals( "g-5",   Dict.getNote(7)   );
		assertEquals( "gis-5", Dict.getNote(8)   );
		assertEquals( "a-5",   Dict.getNote(9)   );
		assertEquals( "b-5",   Dict.getNote(10)  );
		assertEquals( "h-5",   Dict.getNote(11)  );
		assertEquals( "c-4",   Dict.getNote(12)  );
		assertEquals( "cis-4", Dict.getNote(13)  );
		assertEquals( "d-4",   Dict.getNote(14)  );
		assertEquals( "dis-4", Dict.getNote(15)  );
		assertEquals( "e-4",   Dict.getNote(16)  );
		assertEquals( "f-4",   Dict.getNote(17)  );
		assertEquals( "fis-4", Dict.getNote(18)  );
		assertEquals( "g-4",   Dict.getNote(19)  );
		assertEquals( "gis-4", Dict.getNote(20)  );
		assertEquals( "a-4",   Dict.getNote(21)  );
		assertEquals( "b-4",   Dict.getNote(22)  );
		assertEquals( "h-4",   Dict.getNote(23)  );
		assertEquals( "c-3",   Dict.getNote(24)  );
		assertEquals( "cis-3", Dict.getNote(25)  );
		assertEquals( "d-3",   Dict.getNote(26)  );
		assertEquals( "dis-3", Dict.getNote(27)  );
		assertEquals( "e-3",   Dict.getNote(28)  );
		assertEquals( "f-3",   Dict.getNote(29)  );
		assertEquals( "fis-3", Dict.getNote(30)  );
		assertEquals( "g-3",   Dict.getNote(31)  );
		assertEquals( "gis-3", Dict.getNote(32)  );
		assertEquals( "a-3",   Dict.getNote(33)  );
		assertEquals( "b-3",   Dict.getNote(34)  );
		assertEquals( "h-3",   Dict.getNote(35)  );
		assertEquals( "c-2",   Dict.getNote(36)  );
		assertEquals( "cis-2", Dict.getNote(37)  );
		assertEquals( "d-2",   Dict.getNote(38)  );
		assertEquals( "dis-2", Dict.getNote(39)  );
		assertEquals( "e-2",   Dict.getNote(40)  );
		assertEquals( "f-2",   Dict.getNote(41)  );
		assertEquals( "fis-2", Dict.getNote(42)  );
		assertEquals( "g-2",   Dict.getNote(43)  );
		assertEquals( "gis-2", Dict.getNote(44)  );
		assertEquals( "a-2",   Dict.getNote(45)  );
		assertEquals( "b-2",   Dict.getNote(46)  );
		assertEquals( "h-2",   Dict.getNote(47)  );
		assertEquals( "c-",    Dict.getNote(48)  );
		assertEquals( "cis-",  Dict.getNote(49)  );
		assertEquals( "d-",    Dict.getNote(50)  );
		assertEquals( "dis-",  Dict.getNote(51)  );
		assertEquals( "e-",    Dict.getNote(52)  );
		assertEquals( "f-",    Dict.getNote(53)  );
		assertEquals( "fis-",  Dict.getNote(54)  );
		assertEquals( "g-",    Dict.getNote(55)  );
		assertEquals( "gis-",  Dict.getNote(56)  );
		assertEquals( "a-",    Dict.getNote(57)  );
		assertEquals( "b-",    Dict.getNote(58)  );
		assertEquals( "h-",    Dict.getNote(59)  );
		assertEquals( "c",     Dict.getNote(60)  );
		assertEquals( "cis",   Dict.getNote(61)  );
		assertEquals( "d",     Dict.getNote(62)  );
		assertEquals( "dis",   Dict.getNote(63)  );
		assertEquals( "e",     Dict.getNote(64)  );
		assertEquals( "f",     Dict.getNote(65)  );
		assertEquals( "fis",   Dict.getNote(66)  );
		assertEquals( "g",     Dict.getNote(67)  );
		assertEquals( "gis",   Dict.getNote(68)  );
		assertEquals( "a",     Dict.getNote(69)  );
		assertEquals( "b",     Dict.getNote(70)  );
		assertEquals( "h",     Dict.getNote(71)  );
		assertEquals( "c+",    Dict.getNote(72)  );
		assertEquals( "cis+",  Dict.getNote(73)  );
		assertEquals( "d+",    Dict.getNote(74)  );
		assertEquals( "dis+",  Dict.getNote(75)  );
		assertEquals( "e+",    Dict.getNote(76)  );
		assertEquals( "f+",    Dict.getNote(77)  );
		assertEquals( "fis+",  Dict.getNote(78)  );
		assertEquals( "g+",    Dict.getNote(79)  );
		assertEquals( "gis+",  Dict.getNote(80)  );
		assertEquals( "a+",    Dict.getNote(81)  );
		assertEquals( "b+",    Dict.getNote(82)  );
		assertEquals( "h+",    Dict.getNote(83)  );
		assertEquals( "c+2",   Dict.getNote(84)  );
		assertEquals( "cis+2", Dict.getNote(85)  );
		assertEquals( "d+2",   Dict.getNote(86)  );
		assertEquals( "dis+2", Dict.getNote(87)  );
		assertEquals( "e+2",   Dict.getNote(88)  );
		assertEquals( "f+2",   Dict.getNote(89)  );
		assertEquals( "fis+2", Dict.getNote(90)  );
		assertEquals( "g+2",   Dict.getNote(91)  );
		assertEquals( "gis+2", Dict.getNote(92)  );
		assertEquals( "a+2",   Dict.getNote(93)  );
		assertEquals( "b+2",   Dict.getNote(94)  );
		assertEquals( "h+2",   Dict.getNote(95)  );
		assertEquals( "c+3",   Dict.getNote(96)  );
		assertEquals( "cis+3", Dict.getNote(97)  );
		assertEquals( "d+3",   Dict.getNote(98)  );
		assertEquals( "dis+3", Dict.getNote(99)  );
		assertEquals( "e+3",   Dict.getNote(100) );
		assertEquals( "f+3",   Dict.getNote(101) );
		assertEquals( "fis+3", Dict.getNote(102) );
		assertEquals( "g+3",   Dict.getNote(103) );
		assertEquals( "gis+3", Dict.getNote(104) );
		assertEquals( "a+3",   Dict.getNote(105) );
		assertEquals( "b+3",   Dict.getNote(106) );
		assertEquals( "h+3",   Dict.getNote(107) );
		assertEquals( "c+4",   Dict.getNote(108) );
		assertEquals( "cis+4", Dict.getNote(109) );
		assertEquals( "d+4",   Dict.getNote(110) );
		assertEquals( "dis+4", Dict.getNote(111) );
		assertEquals( "e+4",   Dict.getNote(112) );
		assertEquals( "f+4",   Dict.getNote(113) );
		assertEquals( "fis+4", Dict.getNote(114) );
		assertEquals( "g+4",   Dict.getNote(115) );
		assertEquals( "gis+4", Dict.getNote(116) );
		assertEquals( "a+4",   Dict.getNote(117) );
		assertEquals( "b+4",   Dict.getNote(118) );
		assertEquals( "h+4",   Dict.getNote(119) );
		assertEquals( "c+5",   Dict.getNote(120) );
		assertEquals( "cis+5", Dict.getNote(121) );
		assertEquals( "d+5",   Dict.getNote(122) );
		assertEquals( "dis+5", Dict.getNote(123) );
		assertEquals( "e+5",   Dict.getNote(124) );
		assertEquals( "f+5",   Dict.getNote(125) );
		assertEquals( "fis+5", Dict.getNote(126) );
		assertEquals( "g+5",   Dict.getNote(127) );
		
		// test +/-
		cbxs[3].setSelectedIndex(1);
		assertEquals( "c-----",   Dict.getNote(0)   );
		assertEquals( "cis-----", Dict.getNote(1)   );
		assertEquals( "d-----",   Dict.getNote(2)   );
		assertEquals( "dis-----", Dict.getNote(3)   );
		assertEquals( "e-----",   Dict.getNote(4)   );
		assertEquals( "f-----",   Dict.getNote(5)   );
		assertEquals( "fis-----", Dict.getNote(6)   );
		assertEquals( "g-----",   Dict.getNote(7)   );
		assertEquals( "gis-----", Dict.getNote(8)   );
		assertEquals( "a-----",   Dict.getNote(9)   );
		assertEquals( "b-----",   Dict.getNote(10)  );
		assertEquals( "h-----",   Dict.getNote(11)  );
		assertEquals( "c----",    Dict.getNote(12)  );
		assertEquals( "cis----",  Dict.getNote(13)  );
		assertEquals( "d----",    Dict.getNote(14)  );
		assertEquals( "dis----",  Dict.getNote(15)  );
		assertEquals( "e----",    Dict.getNote(16)  );
		assertEquals( "f----",    Dict.getNote(17)  );
		assertEquals( "fis----",  Dict.getNote(18)  );
		assertEquals( "g----",    Dict.getNote(19)  );
		assertEquals( "gis----",  Dict.getNote(20)  );
		assertEquals( "a----",    Dict.getNote(21)  );
		assertEquals( "b----",    Dict.getNote(22)  );
		assertEquals( "h----",    Dict.getNote(23)  );
		assertEquals( "c---",     Dict.getNote(24)  );
		assertEquals( "cis---",   Dict.getNote(25)  );
		assertEquals( "d---",     Dict.getNote(26)  );
		assertEquals( "dis---",   Dict.getNote(27)  );
		assertEquals( "e---",     Dict.getNote(28)  );
		assertEquals( "f---",     Dict.getNote(29)  );
		assertEquals( "fis---",   Dict.getNote(30)  );
		assertEquals( "g---",     Dict.getNote(31)  );
		assertEquals( "gis---",   Dict.getNote(32)  );
		assertEquals( "a---",     Dict.getNote(33)  );
		assertEquals( "b---",     Dict.getNote(34)  );
		assertEquals( "h---",     Dict.getNote(35)  );
		assertEquals( "c--",      Dict.getNote(36)  );
		assertEquals( "cis--",    Dict.getNote(37)  );
		assertEquals( "d--",      Dict.getNote(38)  );
		assertEquals( "dis--",    Dict.getNote(39)  );
		assertEquals( "e--",      Dict.getNote(40)  );
		assertEquals( "f--",      Dict.getNote(41)  );
		assertEquals( "fis--",    Dict.getNote(42)  );
		assertEquals( "g--",      Dict.getNote(43)  );
		assertEquals( "gis--",    Dict.getNote(44)  );
		assertEquals( "a--",      Dict.getNote(45)  );
		assertEquals( "b--",      Dict.getNote(46)  );
		assertEquals( "h--",      Dict.getNote(47)  );
		assertEquals( "c-",       Dict.getNote(48)  );
		assertEquals( "cis-",     Dict.getNote(49)  );
		assertEquals( "d-",       Dict.getNote(50)  );
		assertEquals( "dis-",     Dict.getNote(51)  );
		assertEquals( "e-",       Dict.getNote(52)  );
		assertEquals( "f-",       Dict.getNote(53)  );
		assertEquals( "fis-",     Dict.getNote(54)  );
		assertEquals( "g-",       Dict.getNote(55)  );
		assertEquals( "gis-",     Dict.getNote(56)  );
		assertEquals( "a-",       Dict.getNote(57)  );
		assertEquals( "b-",       Dict.getNote(58)  );
		assertEquals( "h-",       Dict.getNote(59)  );
		assertEquals( "c",        Dict.getNote(60)  );
		assertEquals( "cis",      Dict.getNote(61)  );
		assertEquals( "d",        Dict.getNote(62)  );
		assertEquals( "dis",      Dict.getNote(63)  );
		assertEquals( "e",        Dict.getNote(64)  );
		assertEquals( "f",        Dict.getNote(65)  );
		assertEquals( "fis",      Dict.getNote(66)  );
		assertEquals( "g",        Dict.getNote(67)  );
		assertEquals( "gis",      Dict.getNote(68)  );
		assertEquals( "a",        Dict.getNote(69)  );
		assertEquals( "b",        Dict.getNote(70)  );
		assertEquals( "h",        Dict.getNote(71)  );
		assertEquals( "c+",       Dict.getNote(72)  );
		assertEquals( "cis+",     Dict.getNote(73)  );
		assertEquals( "d+",       Dict.getNote(74)  );
		assertEquals( "dis+",     Dict.getNote(75)  );
		assertEquals( "e+",       Dict.getNote(76)  );
		assertEquals( "f+",       Dict.getNote(77)  );
		assertEquals( "fis+",     Dict.getNote(78)  );
		assertEquals( "g+",       Dict.getNote(79)  );
		assertEquals( "gis+",     Dict.getNote(80)  );
		assertEquals( "a+",       Dict.getNote(81)  );
		assertEquals( "b+",       Dict.getNote(82)  );
		assertEquals( "h+",       Dict.getNote(83)  );
		assertEquals( "c++",      Dict.getNote(84)  );
		assertEquals( "cis++",    Dict.getNote(85)  );
		assertEquals( "d++",      Dict.getNote(86)  );
		assertEquals( "dis++",    Dict.getNote(87)  );
		assertEquals( "e++",      Dict.getNote(88)  );
		assertEquals( "f++",      Dict.getNote(89)  );
		assertEquals( "fis++",    Dict.getNote(90)  );
		assertEquals( "g++",      Dict.getNote(91)  );
		assertEquals( "gis++",    Dict.getNote(92)  );
		assertEquals( "a++",      Dict.getNote(93)  );
		assertEquals( "b++",      Dict.getNote(94)  );
		assertEquals( "h++",      Dict.getNote(95)  );
		assertEquals( "c+++",     Dict.getNote(96)  );
		assertEquals( "cis+++",   Dict.getNote(97)  );
		assertEquals( "d+++",     Dict.getNote(98)  );
		assertEquals( "dis+++",   Dict.getNote(99)  );
		assertEquals( "e+++",     Dict.getNote(100) );
		assertEquals( "f+++",     Dict.getNote(101) );
		assertEquals( "fis+++",   Dict.getNote(102) );
		assertEquals( "g+++",     Dict.getNote(103) );
		assertEquals( "gis+++",   Dict.getNote(104) );
		assertEquals( "a+++",     Dict.getNote(105) );
		assertEquals( "b+++",     Dict.getNote(106) );
		assertEquals( "h+++",     Dict.getNote(107) );
		assertEquals( "c++++",    Dict.getNote(108) );
		assertEquals( "cis++++",  Dict.getNote(109) );
		assertEquals( "d++++",    Dict.getNote(110) );
		assertEquals( "dis++++",  Dict.getNote(111) );
		assertEquals( "e++++",    Dict.getNote(112) );
		assertEquals( "f++++",    Dict.getNote(113) );
		assertEquals( "fis++++",  Dict.getNote(114) );
		assertEquals( "g++++",    Dict.getNote(115) );
		assertEquals( "gis++++",  Dict.getNote(116) );
		assertEquals( "a++++",    Dict.getNote(117) );
		assertEquals( "b++++",    Dict.getNote(118) );
		assertEquals( "h++++",    Dict.getNote(119) );
		assertEquals( "c+++++",   Dict.getNote(120) );
		assertEquals( "cis+++++", Dict.getNote(121) );
		assertEquals( "d+++++",   Dict.getNote(122) );
		assertEquals( "dis+++++", Dict.getNote(123) );
		assertEquals( "e+++++",   Dict.getNote(124) );
		assertEquals( "f+++++",   Dict.getNote(125) );
		assertEquals( "fis+++++", Dict.getNote(126) );
		assertEquals( "g+++++",   Dict.getNote(127) );
		
		// test international
		cbxs[3].setSelectedIndex(2);
		assertEquals( "c-1",   Dict.getNote(0)   );
		assertEquals( "cis-1", Dict.getNote(1)   );
		assertEquals( "d-1",   Dict.getNote(2)   );
		assertEquals( "dis-1", Dict.getNote(3)   );
		assertEquals( "e-1",   Dict.getNote(4)   );
		assertEquals( "f-1",   Dict.getNote(5)   );
		assertEquals( "fis-1", Dict.getNote(6)   );
		assertEquals( "g-1",   Dict.getNote(7)   );
		assertEquals( "gis-1", Dict.getNote(8)   );
		assertEquals( "a-1",   Dict.getNote(9)   );
		assertEquals( "b-1",   Dict.getNote(10)  );
		assertEquals( "h-1",   Dict.getNote(11)  );
		assertEquals( "c0",    Dict.getNote(12)  );
		assertEquals( "cis0",  Dict.getNote(13)  );
		assertEquals( "d0",    Dict.getNote(14)  );
		assertEquals( "dis0",  Dict.getNote(15)  );
		assertEquals( "e0",    Dict.getNote(16)  );
		assertEquals( "f0",    Dict.getNote(17)  );
		assertEquals( "fis0",  Dict.getNote(18)  );
		assertEquals( "g0",    Dict.getNote(19)  );
		assertEquals( "gis0",  Dict.getNote(20)  );
		assertEquals( "a0",    Dict.getNote(21)  );
		assertEquals( "b0",    Dict.getNote(22)  );
		assertEquals( "h0",    Dict.getNote(23)  );
		assertEquals( "c1",    Dict.getNote(24)  );
		assertEquals( "cis1",  Dict.getNote(25)  );
		assertEquals( "d1",    Dict.getNote(26)  );
		assertEquals( "dis1",  Dict.getNote(27)  );
		assertEquals( "e1",    Dict.getNote(28)  );
		assertEquals( "f1",    Dict.getNote(29)  );
		assertEquals( "fis1",  Dict.getNote(30)  );
		assertEquals( "g1",    Dict.getNote(31)  );
		assertEquals( "gis1",  Dict.getNote(32)  );
		assertEquals( "a1",    Dict.getNote(33)  );
		assertEquals( "b1",    Dict.getNote(34)  );
		assertEquals( "h1",    Dict.getNote(35)  );
		assertEquals( "c2",    Dict.getNote(36)  );
		assertEquals( "cis2",  Dict.getNote(37)  );
		assertEquals( "d2",    Dict.getNote(38)  );
		assertEquals( "dis2",  Dict.getNote(39)  );
		assertEquals( "e2",    Dict.getNote(40)  );
		assertEquals( "f2",    Dict.getNote(41)  );
		assertEquals( "fis2",  Dict.getNote(42)  );
		assertEquals( "g2",    Dict.getNote(43)  );
		assertEquals( "gis2",  Dict.getNote(44)  );
		assertEquals( "a2",    Dict.getNote(45)  );
		assertEquals( "b2",    Dict.getNote(46)  );
		assertEquals( "h2",    Dict.getNote(47)  );
		assertEquals( "c3",    Dict.getNote(48)  );
		assertEquals( "cis3",  Dict.getNote(49)  );
		assertEquals( "d3",    Dict.getNote(50)  );
		assertEquals( "dis3",  Dict.getNote(51)  );
		assertEquals( "e3",    Dict.getNote(52)  );
		assertEquals( "f3",    Dict.getNote(53)  );
		assertEquals( "fis3",  Dict.getNote(54)  );
		assertEquals( "g3",    Dict.getNote(55)  );
		assertEquals( "gis3",  Dict.getNote(56)  );
		assertEquals( "a3",    Dict.getNote(57)  );
		assertEquals( "b3",    Dict.getNote(58)  );
		assertEquals( "h3",    Dict.getNote(59)  );
		assertEquals( "c4",    Dict.getNote(60)  );
		assertEquals( "cis4",  Dict.getNote(61)  );
		assertEquals( "d4",    Dict.getNote(62)  );
		assertEquals( "dis4",  Dict.getNote(63)  );
		assertEquals( "e4",    Dict.getNote(64)  );
		assertEquals( "f4",    Dict.getNote(65)  );
		assertEquals( "fis4",  Dict.getNote(66)  );
		assertEquals( "g4",    Dict.getNote(67)  );
		assertEquals( "gis4",  Dict.getNote(68)  );
		assertEquals( "a4",    Dict.getNote(69)  );
		assertEquals( "b4",    Dict.getNote(70)  );
		assertEquals( "h4",    Dict.getNote(71)  );
		assertEquals( "c5",    Dict.getNote(72)  );
		assertEquals( "cis5",  Dict.getNote(73)  );
		assertEquals( "d5",    Dict.getNote(74)  );
		assertEquals( "dis5",  Dict.getNote(75)  );
		assertEquals( "e5",    Dict.getNote(76)  );
		assertEquals( "f5",    Dict.getNote(77)  );
		assertEquals( "fis5",  Dict.getNote(78)  );
		assertEquals( "g5",    Dict.getNote(79)  );
		assertEquals( "gis5",  Dict.getNote(80)  );
		assertEquals( "a5",    Dict.getNote(81)  );
		assertEquals( "b5",    Dict.getNote(82)  );
		assertEquals( "h5",    Dict.getNote(83)  );
		assertEquals( "c6",    Dict.getNote(84)  );
		assertEquals( "cis6",  Dict.getNote(85)  );
		assertEquals( "d6",    Dict.getNote(86)  );
		assertEquals( "dis6",  Dict.getNote(87)  );
		assertEquals( "e6",    Dict.getNote(88)  );
		assertEquals( "f6",    Dict.getNote(89)  );
		assertEquals( "fis6",  Dict.getNote(90)  );
		assertEquals( "g6",    Dict.getNote(91)  );
		assertEquals( "gis6",  Dict.getNote(92)  );
		assertEquals( "a6",    Dict.getNote(93)  );
		assertEquals( "b6",    Dict.getNote(94)  );
		assertEquals( "h6",    Dict.getNote(95)  );
		assertEquals( "c7",    Dict.getNote(96)  );
		assertEquals( "cis7",  Dict.getNote(97)  );
		assertEquals( "d7",    Dict.getNote(98)  );
		assertEquals( "dis7",  Dict.getNote(99)  );
		assertEquals( "e7",    Dict.getNote(100) );
		assertEquals( "f7",    Dict.getNote(101) );
		assertEquals( "fis7",  Dict.getNote(102) );
		assertEquals( "g7",    Dict.getNote(103) );
		assertEquals( "gis7",  Dict.getNote(104) );
		assertEquals( "a7",    Dict.getNote(105) );
		assertEquals( "b7",    Dict.getNote(106) );
		assertEquals( "h7",    Dict.getNote(107) );
		assertEquals( "c8",    Dict.getNote(108) );
		assertEquals( "cis8",  Dict.getNote(109) );
		assertEquals( "d8",    Dict.getNote(110) );
		assertEquals( "dis8",  Dict.getNote(111) );
		assertEquals( "e8",    Dict.getNote(112) );
		assertEquals( "f8",    Dict.getNote(113) );
		assertEquals( "fis8",  Dict.getNote(114) );
		assertEquals( "g8",    Dict.getNote(115) );
		assertEquals( "gis8",  Dict.getNote(116) );
		assertEquals( "a8",    Dict.getNote(117) );
		assertEquals( "b8",    Dict.getNote(118) );
		assertEquals( "h8",    Dict.getNote(119) );
		assertEquals( "c9",    Dict.getNote(120) );
		assertEquals( "cis9",  Dict.getNote(121) );
		assertEquals( "d9",    Dict.getNote(122) );
		assertEquals( "dis9",  Dict.getNote(123) );
		assertEquals( "e9",    Dict.getNote(124) );
		assertEquals( "f9",    Dict.getNote(125) );
		assertEquals( "fis9",  Dict.getNote(126) );
		assertEquals( "g9",    Dict.getNote(127) );
		
		// test german
		cbxs[3].setSelectedIndex(3);
		assertEquals( "C'''",      Dict.getNote(0)   );
		assertEquals( "Cis'''",    Dict.getNote(1)   );
		assertEquals( "D'''",      Dict.getNote(2)   );
		assertEquals( "Dis'''",    Dict.getNote(3)   );
		assertEquals( "E'''",      Dict.getNote(4)   );
		assertEquals( "F'''",      Dict.getNote(5)   );
		assertEquals( "Fis'''",    Dict.getNote(6)   );
		assertEquals( "G'''",      Dict.getNote(7)   );
		assertEquals( "Gis'''",    Dict.getNote(8)   );
		assertEquals( "A'''",      Dict.getNote(9)   );
		assertEquals( "B'''",      Dict.getNote(10)  );
		assertEquals( "H'''",      Dict.getNote(11)  );
		assertEquals( "C''",       Dict.getNote(12)  );
		assertEquals( "Cis''",     Dict.getNote(13)  );
		assertEquals( "D''",       Dict.getNote(14)  );
		assertEquals( "Dis''",     Dict.getNote(15)  );
		assertEquals( "E''",       Dict.getNote(16)  );
		assertEquals( "F''",       Dict.getNote(17)  );
		assertEquals( "Fis''",     Dict.getNote(18)  );
		assertEquals( "G''",       Dict.getNote(19)  );
		assertEquals( "Gis''",     Dict.getNote(20)  );
		assertEquals( "A''",       Dict.getNote(21)  );
		assertEquals( "B''",       Dict.getNote(22)  );
		assertEquals( "H''",       Dict.getNote(23)  );
		assertEquals( "C'",        Dict.getNote(24)  );
		assertEquals( "Cis'",      Dict.getNote(25)  );
		assertEquals( "D'",        Dict.getNote(26)  );
		assertEquals( "Dis'",      Dict.getNote(27)  );
		assertEquals( "E'",        Dict.getNote(28)  );
		assertEquals( "F'",        Dict.getNote(29)  );
		assertEquals( "Fis'",      Dict.getNote(30)  );
		assertEquals( "G'",        Dict.getNote(31)  );
		assertEquals( "Gis'",      Dict.getNote(32)  );
		assertEquals( "A'",        Dict.getNote(33)  );
		assertEquals( "B'",        Dict.getNote(34)  );
		assertEquals( "H'",        Dict.getNote(35)  );
		assertEquals( "C",         Dict.getNote(36)  );
		assertEquals( "Cis",       Dict.getNote(37)  );
		assertEquals( "D",         Dict.getNote(38)  );
		assertEquals( "Dis",       Dict.getNote(39)  );
		assertEquals( "E",         Dict.getNote(40)  );
		assertEquals( "F",         Dict.getNote(41)  );
		assertEquals( "Fis",       Dict.getNote(42)  );
		assertEquals( "G",         Dict.getNote(43)  );
		assertEquals( "Gis",       Dict.getNote(44)  );
		assertEquals( "A",         Dict.getNote(45)  );
		assertEquals( "B",         Dict.getNote(46)  );
		assertEquals( "H",         Dict.getNote(47)  );
		assertEquals( "c",         Dict.getNote(48)  );
		assertEquals( "cis",       Dict.getNote(49)  );
		assertEquals( "d",         Dict.getNote(50)  );
		assertEquals( "dis",       Dict.getNote(51)  );
		assertEquals( "e",         Dict.getNote(52)  );
		assertEquals( "f",         Dict.getNote(53)  );
		assertEquals( "fis",       Dict.getNote(54)  );
		assertEquals( "g",         Dict.getNote(55)  );
		assertEquals( "gis",       Dict.getNote(56)  );
		assertEquals( "a",         Dict.getNote(57)  );
		assertEquals( "b",         Dict.getNote(58)  );
		assertEquals( "h",         Dict.getNote(59)  );
		assertEquals( "c'",        Dict.getNote(60)  );
		assertEquals( "cis'",      Dict.getNote(61)  );
		assertEquals( "d'",        Dict.getNote(62)  );
		assertEquals( "dis'",      Dict.getNote(63)  );
		assertEquals( "e'",        Dict.getNote(64)  );
		assertEquals( "f'",        Dict.getNote(65)  );
		assertEquals( "fis'",      Dict.getNote(66)  );
		assertEquals( "g'",        Dict.getNote(67)  );
		assertEquals( "gis'",      Dict.getNote(68)  );
		assertEquals( "a'",        Dict.getNote(69)  );
		assertEquals( "b'",        Dict.getNote(70)  );
		assertEquals( "h'",        Dict.getNote(71)  );
		assertEquals( "c''",       Dict.getNote(72)  );
		assertEquals( "cis''",     Dict.getNote(73)  );
		assertEquals( "d''",       Dict.getNote(74)  );
		assertEquals( "dis''",     Dict.getNote(75)  );
		assertEquals( "e''",       Dict.getNote(76)  );
		assertEquals( "f''",       Dict.getNote(77)  );
		assertEquals( "fis''",     Dict.getNote(78)  );
		assertEquals( "g''",       Dict.getNote(79)  );
		assertEquals( "gis''",     Dict.getNote(80)  );
		assertEquals( "a''",       Dict.getNote(81)  );
		assertEquals( "b''",       Dict.getNote(82)  );
		assertEquals( "h''",       Dict.getNote(83)  );
		assertEquals( "c'''",      Dict.getNote(84)  );
		assertEquals( "cis'''",    Dict.getNote(85)  );
		assertEquals( "d'''",      Dict.getNote(86)  );
		assertEquals( "dis'''",    Dict.getNote(87)  );
		assertEquals( "e'''",      Dict.getNote(88)  );
		assertEquals( "f'''",      Dict.getNote(89)  );
		assertEquals( "fis'''",    Dict.getNote(90)  );
		assertEquals( "g'''",      Dict.getNote(91)  );
		assertEquals( "gis'''",    Dict.getNote(92)  );
		assertEquals( "a'''",      Dict.getNote(93)  );
		assertEquals( "b'''",      Dict.getNote(94)  );
		assertEquals( "h'''",      Dict.getNote(95)  );
		assertEquals( "c''''",     Dict.getNote(96)  );
		assertEquals( "cis''''",   Dict.getNote(97)  );
		assertEquals( "d''''",     Dict.getNote(98)  );
		assertEquals( "dis''''",   Dict.getNote(99)  );
		assertEquals( "e''''",     Dict.getNote(100) );
		assertEquals( "f''''",     Dict.getNote(101) );
		assertEquals( "fis''''",   Dict.getNote(102) );
		assertEquals( "g''''",     Dict.getNote(103) );
		assertEquals( "gis''''",   Dict.getNote(104) );
		assertEquals( "a''''",     Dict.getNote(105) );
		assertEquals( "b''''",     Dict.getNote(106) );
		assertEquals( "h''''",     Dict.getNote(107) );
		assertEquals( "c'''''",    Dict.getNote(108) );
		assertEquals( "cis'''''",  Dict.getNote(109) );
		assertEquals( "d'''''",    Dict.getNote(110) );
		assertEquals( "dis'''''",  Dict.getNote(111) );
		assertEquals( "e'''''",    Dict.getNote(112) );
		assertEquals( "f'''''",    Dict.getNote(113) );
		assertEquals( "fis'''''",  Dict.getNote(114) );
		assertEquals( "g'''''",    Dict.getNote(115) );
		assertEquals( "gis'''''",  Dict.getNote(116) );
		assertEquals( "a'''''",    Dict.getNote(117) );
		assertEquals( "b'''''",    Dict.getNote(118) );
		assertEquals( "h'''''",    Dict.getNote(119) );
		assertEquals( "c''''''",   Dict.getNote(120) );
		assertEquals( "cis''''''", Dict.getNote(121) );
		assertEquals( "d''''''",   Dict.getNote(122) );
		assertEquals( "dis''''''", Dict.getNote(123) );
		assertEquals( "e''''''",   Dict.getNote(124) );
		assertEquals( "f''''''",   Dict.getNote(125) );
		assertEquals( "fis''''''", Dict.getNote(126) );
		assertEquals( "g''''''",   Dict.getNote(127) );
		
		// switch back to default
		cbxs[1].setSelectedIndex(0); 
		cbxs[2].setSelectedIndex(0);
		cbxs[3].setSelectedIndex(0);
	}
	
	/**
	 * Tests octave configurations.
	 * 
	 * Using the following config:
	 * 
	 * - note system: german (upper-case)
	 * - half tone: -es
	 * - octave naming: all possibilities:
	 *     - +n/-n
	 *     - +/-
	 *     - international
	 *     - german
	 */
	@Test
	public void testOctaveSystemsGerUcCes() {
		
		// switch config
		cbxs[1].setSelectedIndex(5); // german (upper-case)
		cbxs[2].setSelectedIndex(5); // -es
		
		// test default (+n/-n)
		assertEquals( "C-5",   Dict.getNote(0)   );
		assertEquals( "Des-5", Dict.getNote(1)   );
		assertEquals( "D-5",   Dict.getNote(2)   );
		assertEquals( "Es-5",  Dict.getNote(3)   );
		assertEquals( "E-5",   Dict.getNote(4)   );
		assertEquals( "F-5",   Dict.getNote(5)   );
		assertEquals( "Ges-5", Dict.getNote(6)   );
		assertEquals( "G-5",   Dict.getNote(7)   );
		assertEquals( "As-5",  Dict.getNote(8)   );
		assertEquals( "A-5",   Dict.getNote(9)   );
		assertEquals( "B-5",   Dict.getNote(10)  );
		assertEquals( "H-5",   Dict.getNote(11)  );
		assertEquals( "C-4",   Dict.getNote(12)  );
		assertEquals( "Des-4", Dict.getNote(13)  );
		assertEquals( "D-4",   Dict.getNote(14)  );
		assertEquals( "Es-4",  Dict.getNote(15)  );
		assertEquals( "E-4",   Dict.getNote(16)  );
		assertEquals( "F-4",   Dict.getNote(17)  );
		assertEquals( "Ges-4", Dict.getNote(18)  );
		assertEquals( "G-4",   Dict.getNote(19)  );
		assertEquals( "As-4",  Dict.getNote(20)  );
		assertEquals( "A-4",   Dict.getNote(21)  );
		assertEquals( "B-4",   Dict.getNote(22)  );
		assertEquals( "H-4",   Dict.getNote(23)  );
		assertEquals( "C-3",   Dict.getNote(24)  );
		assertEquals( "Des-3", Dict.getNote(25)  );
		assertEquals( "D-3",   Dict.getNote(26)  );
		assertEquals( "Es-3",  Dict.getNote(27)  );
		assertEquals( "E-3",   Dict.getNote(28)  );
		assertEquals( "F-3",   Dict.getNote(29)  );
		assertEquals( "Ges-3", Dict.getNote(30)  );
		assertEquals( "G-3",   Dict.getNote(31)  );
		assertEquals( "As-3",  Dict.getNote(32)  );
		assertEquals( "A-3",   Dict.getNote(33)  );
		assertEquals( "B-3",   Dict.getNote(34)  );
		assertEquals( "H-3",   Dict.getNote(35)  );
		assertEquals( "C-2",   Dict.getNote(36)  );
		assertEquals( "Des-2", Dict.getNote(37)  );
		assertEquals( "D-2",   Dict.getNote(38)  );
		assertEquals( "Es-2",  Dict.getNote(39)  );
		assertEquals( "E-2",   Dict.getNote(40)  );
		assertEquals( "F-2",   Dict.getNote(41)  );
		assertEquals( "Ges-2", Dict.getNote(42)  );
		assertEquals( "G-2",   Dict.getNote(43)  );
		assertEquals( "As-2",  Dict.getNote(44)  );
		assertEquals( "A-2",   Dict.getNote(45)  );
		assertEquals( "B-2",   Dict.getNote(46)  );
		assertEquals( "H-2",   Dict.getNote(47)  );
		assertEquals( "C-",    Dict.getNote(48)  );
		assertEquals( "Des-",  Dict.getNote(49)  );
		assertEquals( "D-",    Dict.getNote(50)  );
		assertEquals( "Es-",   Dict.getNote(51)  );
		assertEquals( "E-",    Dict.getNote(52)  );
		assertEquals( "F-",    Dict.getNote(53)  );
		assertEquals( "Ges-",  Dict.getNote(54)  );
		assertEquals( "G-",    Dict.getNote(55)  );
		assertEquals( "As-",   Dict.getNote(56)  );
		assertEquals( "A-",    Dict.getNote(57)  );
		assertEquals( "B-",    Dict.getNote(58)  );
		assertEquals( "H-",    Dict.getNote(59)  );
		assertEquals( "C",     Dict.getNote(60)  );
		assertEquals( "Des",   Dict.getNote(61)  );
		assertEquals( "D",     Dict.getNote(62)  );
		assertEquals( "Es",    Dict.getNote(63)  );
		assertEquals( "E",     Dict.getNote(64)  );
		assertEquals( "F",     Dict.getNote(65)  );
		assertEquals( "Ges",   Dict.getNote(66)  );
		assertEquals( "G",     Dict.getNote(67)  );
		assertEquals( "As",    Dict.getNote(68)  );
		assertEquals( "A",     Dict.getNote(69)  );
		assertEquals( "B",     Dict.getNote(70)  );
		assertEquals( "H",     Dict.getNote(71)  );
		assertEquals( "C+",    Dict.getNote(72)  );
		assertEquals( "Des+",  Dict.getNote(73)  );
		assertEquals( "D+",    Dict.getNote(74)  );
		assertEquals( "Es+",   Dict.getNote(75)  );
		assertEquals( "E+",    Dict.getNote(76)  );
		assertEquals( "F+",    Dict.getNote(77)  );
		assertEquals( "Ges+",  Dict.getNote(78)  );
		assertEquals( "G+",    Dict.getNote(79)  );
		assertEquals( "As+",   Dict.getNote(80)  );
		assertEquals( "A+",    Dict.getNote(81)  );
		assertEquals( "B+",    Dict.getNote(82)  );
		assertEquals( "H+",    Dict.getNote(83)  );
		assertEquals( "C+2",   Dict.getNote(84)  );
		assertEquals( "Des+2", Dict.getNote(85)  );
		assertEquals( "D+2",   Dict.getNote(86)  );
		assertEquals( "Es+2",  Dict.getNote(87)  );
		assertEquals( "E+2",   Dict.getNote(88)  );
		assertEquals( "F+2",   Dict.getNote(89)  );
		assertEquals( "Ges+2", Dict.getNote(90)  );
		assertEquals( "G+2",   Dict.getNote(91)  );
		assertEquals( "As+2",  Dict.getNote(92)  );
		assertEquals( "A+2",   Dict.getNote(93)  );
		assertEquals( "B+2",   Dict.getNote(94)  );
		assertEquals( "H+2",   Dict.getNote(95)  );
		assertEquals( "C+3",   Dict.getNote(96)  );
		assertEquals( "Des+3", Dict.getNote(97)  );
		assertEquals( "D+3",   Dict.getNote(98)  );
		assertEquals( "Es+3",  Dict.getNote(99)  );
		assertEquals( "E+3",   Dict.getNote(100) );
		assertEquals( "F+3",   Dict.getNote(101) );
		assertEquals( "Ges+3", Dict.getNote(102) );
		assertEquals( "G+3",   Dict.getNote(103) );
		assertEquals( "As+3",  Dict.getNote(104) );
		assertEquals( "A+3",   Dict.getNote(105) );
		assertEquals( "B+3",   Dict.getNote(106) );
		assertEquals( "H+3",   Dict.getNote(107) );
		assertEquals( "C+4",   Dict.getNote(108) );
		assertEquals( "Des+4", Dict.getNote(109) );
		assertEquals( "D+4",   Dict.getNote(110) );
		assertEquals( "Es+4",  Dict.getNote(111) );
		assertEquals( "E+4",   Dict.getNote(112) );
		assertEquals( "F+4",   Dict.getNote(113) );
		assertEquals( "Ges+4", Dict.getNote(114) );
		assertEquals( "G+4",   Dict.getNote(115) );
		assertEquals( "As+4",  Dict.getNote(116) );
		assertEquals( "A+4",   Dict.getNote(117) );
		assertEquals( "B+4",   Dict.getNote(118) );
		assertEquals( "H+4",   Dict.getNote(119) );
		assertEquals( "C+5",   Dict.getNote(120) );
		assertEquals( "Des+5", Dict.getNote(121) );
		assertEquals( "D+5",   Dict.getNote(122) );
		assertEquals( "Es+5",  Dict.getNote(123) );
		assertEquals( "E+5",   Dict.getNote(124) );
		assertEquals( "F+5",   Dict.getNote(125) );
		assertEquals( "Ges+5", Dict.getNote(126) );
		assertEquals( "G+5",   Dict.getNote(127) );
		
		// test +/-
		cbxs[3].setSelectedIndex(1);
		assertEquals( "C-----",   Dict.getNote(0)   );
		assertEquals( "Des-----", Dict.getNote(1)   );
		assertEquals( "D-----",   Dict.getNote(2)   );
		assertEquals( "Es-----",  Dict.getNote(3)   );
		assertEquals( "E-----",   Dict.getNote(4)   );
		assertEquals( "F-----",   Dict.getNote(5)   );
		assertEquals( "Ges-----", Dict.getNote(6)   );
		assertEquals( "G-----",   Dict.getNote(7)   );
		assertEquals( "As-----",  Dict.getNote(8)   );
		assertEquals( "A-----",   Dict.getNote(9)   );
		assertEquals( "B-----",   Dict.getNote(10)  );
		assertEquals( "H-----",   Dict.getNote(11)  );
		assertEquals( "C----",    Dict.getNote(12)  );
		assertEquals( "Des----",  Dict.getNote(13)  );
		assertEquals( "D----",    Dict.getNote(14)  );
		assertEquals( "Es----",   Dict.getNote(15)  );
		assertEquals( "E----",    Dict.getNote(16)  );
		assertEquals( "F----",    Dict.getNote(17)  );
		assertEquals( "Ges----",  Dict.getNote(18)  );
		assertEquals( "G----",    Dict.getNote(19)  );
		assertEquals( "As----",   Dict.getNote(20)  );
		assertEquals( "A----",    Dict.getNote(21)  );
		assertEquals( "B----",    Dict.getNote(22)  );
		assertEquals( "H----",    Dict.getNote(23)  );
		assertEquals( "C---",     Dict.getNote(24)  );
		assertEquals( "Des---",   Dict.getNote(25)  );
		assertEquals( "D---",     Dict.getNote(26)  );
		assertEquals( "Es---",    Dict.getNote(27)  );
		assertEquals( "E---",     Dict.getNote(28)  );
		assertEquals( "F---",     Dict.getNote(29)  );
		assertEquals( "Ges---",   Dict.getNote(30)  );
		assertEquals( "G---",     Dict.getNote(31)  );
		assertEquals( "As---",    Dict.getNote(32)  );
		assertEquals( "A---",     Dict.getNote(33)  );
		assertEquals( "B---",     Dict.getNote(34)  );
		assertEquals( "H---",     Dict.getNote(35)  );
		assertEquals( "C--",      Dict.getNote(36)  );
		assertEquals( "Des--",    Dict.getNote(37)  );
		assertEquals( "D--",      Dict.getNote(38)  );
		assertEquals( "Es--",     Dict.getNote(39)  );
		assertEquals( "E--",      Dict.getNote(40)  );
		assertEquals( "F--",      Dict.getNote(41)  );
		assertEquals( "Ges--",    Dict.getNote(42)  );
		assertEquals( "G--",      Dict.getNote(43)  );
		assertEquals( "As--",     Dict.getNote(44)  );
		assertEquals( "A--",      Dict.getNote(45)  );
		assertEquals( "B--",      Dict.getNote(46)  );
		assertEquals( "H--",      Dict.getNote(47)  );
		assertEquals( "C-",       Dict.getNote(48)  );
		assertEquals( "Des-",     Dict.getNote(49)  );
		assertEquals( "D-",       Dict.getNote(50)  );
		assertEquals( "Es-",      Dict.getNote(51)  );
		assertEquals( "E-",       Dict.getNote(52)  );
		assertEquals( "F-",       Dict.getNote(53)  );
		assertEquals( "Ges-",     Dict.getNote(54)  );
		assertEquals( "G-",       Dict.getNote(55)  );
		assertEquals( "As-",      Dict.getNote(56)  );
		assertEquals( "A-",       Dict.getNote(57)  );
		assertEquals( "B-",       Dict.getNote(58)  );
		assertEquals( "H-",       Dict.getNote(59)  );
		assertEquals( "C",        Dict.getNote(60)  );
		assertEquals( "Des",      Dict.getNote(61)  );
		assertEquals( "D",        Dict.getNote(62)  );
		assertEquals( "Es",       Dict.getNote(63)  );
		assertEquals( "E",        Dict.getNote(64)  );
		assertEquals( "F",        Dict.getNote(65)  );
		assertEquals( "Ges",      Dict.getNote(66)  );
		assertEquals( "G",        Dict.getNote(67)  );
		assertEquals( "As",       Dict.getNote(68)  );
		assertEquals( "A",        Dict.getNote(69)  );
		assertEquals( "B",        Dict.getNote(70)  );
		assertEquals( "H",        Dict.getNote(71)  );
		assertEquals( "C+",       Dict.getNote(72)  );
		assertEquals( "Des+",     Dict.getNote(73)  );
		assertEquals( "D+",       Dict.getNote(74)  );
		assertEquals( "Es+",      Dict.getNote(75)  );
		assertEquals( "E+",       Dict.getNote(76)  );
		assertEquals( "F+",       Dict.getNote(77)  );
		assertEquals( "Ges+",     Dict.getNote(78)  );
		assertEquals( "G+",       Dict.getNote(79)  );
		assertEquals( "As+",      Dict.getNote(80)  );
		assertEquals( "A+",       Dict.getNote(81)  );
		assertEquals( "B+",       Dict.getNote(82)  );
		assertEquals( "H+",       Dict.getNote(83)  );
		assertEquals( "C++",      Dict.getNote(84)  );
		assertEquals( "Des++",    Dict.getNote(85)  );
		assertEquals( "D++",      Dict.getNote(86)  );
		assertEquals( "Es++",     Dict.getNote(87)  );
		assertEquals( "E++",      Dict.getNote(88)  );
		assertEquals( "F++",      Dict.getNote(89)  );
		assertEquals( "Ges++",    Dict.getNote(90)  );
		assertEquals( "G++",      Dict.getNote(91)  );
		assertEquals( "As++",     Dict.getNote(92)  );
		assertEquals( "A++",      Dict.getNote(93)  );
		assertEquals( "B++",      Dict.getNote(94)  );
		assertEquals( "H++",      Dict.getNote(95)  );
		assertEquals( "C+++",     Dict.getNote(96)  );
		assertEquals( "Des+++",   Dict.getNote(97)  );
		assertEquals( "D+++",     Dict.getNote(98)  );
		assertEquals( "Es+++",    Dict.getNote(99)  );
		assertEquals( "E+++",     Dict.getNote(100) );
		assertEquals( "F+++",     Dict.getNote(101) );
		assertEquals( "Ges+++",   Dict.getNote(102) );
		assertEquals( "G+++",     Dict.getNote(103) );
		assertEquals( "As+++",    Dict.getNote(104) );
		assertEquals( "A+++",     Dict.getNote(105) );
		assertEquals( "B+++",     Dict.getNote(106) );
		assertEquals( "H+++",     Dict.getNote(107) );
		assertEquals( "C++++",    Dict.getNote(108) );
		assertEquals( "Des++++",  Dict.getNote(109) );
		assertEquals( "D++++",    Dict.getNote(110) );
		assertEquals( "Es++++",   Dict.getNote(111) );
		assertEquals( "E++++",    Dict.getNote(112) );
		assertEquals( "F++++",    Dict.getNote(113) );
		assertEquals( "Ges++++",  Dict.getNote(114) );
		assertEquals( "G++++",    Dict.getNote(115) );
		assertEquals( "As++++",   Dict.getNote(116) );
		assertEquals( "A++++",    Dict.getNote(117) );
		assertEquals( "B++++",    Dict.getNote(118) );
		assertEquals( "H++++",    Dict.getNote(119) );
		assertEquals( "C+++++",   Dict.getNote(120) );
		assertEquals( "Des+++++", Dict.getNote(121) );
		assertEquals( "D+++++",   Dict.getNote(122) );
		assertEquals( "Es+++++",  Dict.getNote(123) );
		assertEquals( "E+++++",   Dict.getNote(124) );
		assertEquals( "F+++++",   Dict.getNote(125) );
		assertEquals( "Ges+++++", Dict.getNote(126) );
		assertEquals( "G+++++",   Dict.getNote(127) );
		
		// test international
		cbxs[3].setSelectedIndex(2);
		assertEquals( "C-1",   Dict.getNote(0)   );
		assertEquals( "Des-1", Dict.getNote(1)   );
		assertEquals( "D-1",   Dict.getNote(2)   );
		assertEquals( "Es-1",  Dict.getNote(3)   );
		assertEquals( "E-1",   Dict.getNote(4)   );
		assertEquals( "F-1",   Dict.getNote(5)   );
		assertEquals( "Ges-1", Dict.getNote(6)   );
		assertEquals( "G-1",   Dict.getNote(7)   );
		assertEquals( "As-1",  Dict.getNote(8)   );
		assertEquals( "A-1",   Dict.getNote(9)   );
		assertEquals( "B-1",   Dict.getNote(10)  );
		assertEquals( "H-1",   Dict.getNote(11)  );
		assertEquals( "C0",    Dict.getNote(12)  );
		assertEquals( "Des0",  Dict.getNote(13)  );
		assertEquals( "D0",    Dict.getNote(14)  );
		assertEquals( "Es0",   Dict.getNote(15)  );
		assertEquals( "E0",    Dict.getNote(16)  );
		assertEquals( "F0",    Dict.getNote(17)  );
		assertEquals( "Ges0",  Dict.getNote(18)  );
		assertEquals( "G0",    Dict.getNote(19)  );
		assertEquals( "As0",   Dict.getNote(20)  );
		assertEquals( "A0",    Dict.getNote(21)  );
		assertEquals( "B0",    Dict.getNote(22)  );
		assertEquals( "H0",    Dict.getNote(23)  );
		assertEquals( "C1",    Dict.getNote(24)  );
		assertEquals( "Des1",  Dict.getNote(25)  );
		assertEquals( "D1",    Dict.getNote(26)  );
		assertEquals( "Es1",   Dict.getNote(27)  );
		assertEquals( "E1",    Dict.getNote(28)  );
		assertEquals( "F1",    Dict.getNote(29)  );
		assertEquals( "Ges1",  Dict.getNote(30)  );
		assertEquals( "G1",    Dict.getNote(31)  );
		assertEquals( "As1",   Dict.getNote(32)  );
		assertEquals( "A1",    Dict.getNote(33)  );
		assertEquals( "B1",    Dict.getNote(34)  );
		assertEquals( "H1",    Dict.getNote(35)  );
		assertEquals( "C2",    Dict.getNote(36)  );
		assertEquals( "Des2",  Dict.getNote(37)  );
		assertEquals( "D2",    Dict.getNote(38)  );
		assertEquals( "Es2",   Dict.getNote(39)  );
		assertEquals( "E2",    Dict.getNote(40)  );
		assertEquals( "F2",    Dict.getNote(41)  );
		assertEquals( "Ges2",  Dict.getNote(42)  );
		assertEquals( "G2",    Dict.getNote(43)  );
		assertEquals( "As2",   Dict.getNote(44)  );
		assertEquals( "A2",    Dict.getNote(45)  );
		assertEquals( "B2",    Dict.getNote(46)  );
		assertEquals( "H2",    Dict.getNote(47)  );
		assertEquals( "C3",    Dict.getNote(48)  );
		assertEquals( "Des3",  Dict.getNote(49)  );
		assertEquals( "D3",    Dict.getNote(50)  );
		assertEquals( "Es3",   Dict.getNote(51)  );
		assertEquals( "E3",    Dict.getNote(52)  );
		assertEquals( "F3",    Dict.getNote(53)  );
		assertEquals( "Ges3",  Dict.getNote(54)  );
		assertEquals( "G3",    Dict.getNote(55)  );
		assertEquals( "As3",   Dict.getNote(56)  );
		assertEquals( "A3",    Dict.getNote(57)  );
		assertEquals( "B3",    Dict.getNote(58)  );
		assertEquals( "H3",    Dict.getNote(59)  );
		assertEquals( "C4",    Dict.getNote(60)  );
		assertEquals( "Des4",  Dict.getNote(61)  );
		assertEquals( "D4",    Dict.getNote(62)  );
		assertEquals( "Es4",   Dict.getNote(63)  );
		assertEquals( "E4",    Dict.getNote(64)  );
		assertEquals( "F4",    Dict.getNote(65)  );
		assertEquals( "Ges4",  Dict.getNote(66)  );
		assertEquals( "G4",    Dict.getNote(67)  );
		assertEquals( "As4",   Dict.getNote(68)  );
		assertEquals( "A4",    Dict.getNote(69)  );
		assertEquals( "B4",    Dict.getNote(70)  );
		assertEquals( "H4",    Dict.getNote(71)  );
		assertEquals( "C5",    Dict.getNote(72)  );
		assertEquals( "Des5",  Dict.getNote(73)  );
		assertEquals( "D5",    Dict.getNote(74)  );
		assertEquals( "Es5",   Dict.getNote(75)  );
		assertEquals( "E5",    Dict.getNote(76)  );
		assertEquals( "F5",    Dict.getNote(77)  );
		assertEquals( "Ges5",  Dict.getNote(78)  );
		assertEquals( "G5",    Dict.getNote(79)  );
		assertEquals( "As5",   Dict.getNote(80)  );
		assertEquals( "A5",    Dict.getNote(81)  );
		assertEquals( "B5",    Dict.getNote(82)  );
		assertEquals( "H5",    Dict.getNote(83)  );
		assertEquals( "C6",    Dict.getNote(84)  );
		assertEquals( "Des6",  Dict.getNote(85)  );
		assertEquals( "D6",    Dict.getNote(86)  );
		assertEquals( "Es6",   Dict.getNote(87)  );
		assertEquals( "E6",    Dict.getNote(88)  );
		assertEquals( "F6",    Dict.getNote(89)  );
		assertEquals( "Ges6",  Dict.getNote(90)  );
		assertEquals( "G6",    Dict.getNote(91)  );
		assertEquals( "As6",   Dict.getNote(92)  );
		assertEquals( "A6",    Dict.getNote(93)  );
		assertEquals( "B6",    Dict.getNote(94)  );
		assertEquals( "H6",    Dict.getNote(95)  );
		assertEquals( "C7",    Dict.getNote(96)  );
		assertEquals( "Des7",  Dict.getNote(97)  );
		assertEquals( "D7",    Dict.getNote(98)  );
		assertEquals( "Es7",   Dict.getNote(99)  );
		assertEquals( "E7",    Dict.getNote(100) );
		assertEquals( "F7",    Dict.getNote(101) );
		assertEquals( "Ges7",  Dict.getNote(102) );
		assertEquals( "G7",    Dict.getNote(103) );
		assertEquals( "As7",   Dict.getNote(104) );
		assertEquals( "A7",    Dict.getNote(105) );
		assertEquals( "B7",    Dict.getNote(106) );
		assertEquals( "H7",    Dict.getNote(107) );
		assertEquals( "C8",    Dict.getNote(108) );
		assertEquals( "Des8",  Dict.getNote(109) );
		assertEquals( "D8",    Dict.getNote(110) );
		assertEquals( "Es8",   Dict.getNote(111) );
		assertEquals( "E8",    Dict.getNote(112) );
		assertEquals( "F8",    Dict.getNote(113) );
		assertEquals( "Ges8",  Dict.getNote(114) );
		assertEquals( "G8",    Dict.getNote(115) );
		assertEquals( "As8",   Dict.getNote(116) );
		assertEquals( "A8",    Dict.getNote(117) );
		assertEquals( "B8",    Dict.getNote(118) );
		assertEquals( "H8",    Dict.getNote(119) );
		assertEquals( "C9",    Dict.getNote(120) );
		assertEquals( "Des9",  Dict.getNote(121) );
		assertEquals( "D9",    Dict.getNote(122) );
		assertEquals( "Es9",   Dict.getNote(123) );
		assertEquals( "E9",    Dict.getNote(124) );
		assertEquals( "F9",    Dict.getNote(125) );
		assertEquals( "Ges9",  Dict.getNote(126) );
		assertEquals( "G9",    Dict.getNote(127) );
		
		// test german
		cbxs[3].setSelectedIndex(3);
		assertEquals( "C'''",      Dict.getNote(0)   );
		assertEquals( "Des'''",    Dict.getNote(1)   );
		assertEquals( "D'''",      Dict.getNote(2)   );
		assertEquals( "Es'''",     Dict.getNote(3)   );
		assertEquals( "E'''",      Dict.getNote(4)   );
		assertEquals( "F'''",      Dict.getNote(5)   );
		assertEquals( "Ges'''",    Dict.getNote(6)   );
		assertEquals( "G'''",      Dict.getNote(7)   );
		assertEquals( "As'''",     Dict.getNote(8)   );
		assertEquals( "A'''",      Dict.getNote(9)   );
		assertEquals( "B'''",      Dict.getNote(10)  );
		assertEquals( "H'''",      Dict.getNote(11)  );
		assertEquals( "C''",       Dict.getNote(12)  );
		assertEquals( "Des''",     Dict.getNote(13)  );
		assertEquals( "D''",       Dict.getNote(14)  );
		assertEquals( "Es''",      Dict.getNote(15)  );
		assertEquals( "E''",       Dict.getNote(16)  );
		assertEquals( "F''",       Dict.getNote(17)  );
		assertEquals( "Ges''",     Dict.getNote(18)  );
		assertEquals( "G''",       Dict.getNote(19)  );
		assertEquals( "As''",      Dict.getNote(20)  );
		assertEquals( "A''",       Dict.getNote(21)  );
		assertEquals( "B''",       Dict.getNote(22)  );
		assertEquals( "H''",       Dict.getNote(23)  );
		assertEquals( "C'",        Dict.getNote(24)  );
		assertEquals( "Des'",      Dict.getNote(25)  );
		assertEquals( "D'",        Dict.getNote(26)  );
		assertEquals( "Es'",       Dict.getNote(27)  );
		assertEquals( "E'",        Dict.getNote(28)  );
		assertEquals( "F'",        Dict.getNote(29)  );
		assertEquals( "Ges'",      Dict.getNote(30)  );
		assertEquals( "G'",        Dict.getNote(31)  );
		assertEquals( "As'",       Dict.getNote(32)  );
		assertEquals( "A'",        Dict.getNote(33)  );
		assertEquals( "B'",        Dict.getNote(34)  );
		assertEquals( "H'",        Dict.getNote(35)  );
		assertEquals( "C",         Dict.getNote(36)  );
		assertEquals( "Des",       Dict.getNote(37)  );
		assertEquals( "D",         Dict.getNote(38)  );
		assertEquals( "Es",        Dict.getNote(39)  );
		assertEquals( "E",         Dict.getNote(40)  );
		assertEquals( "F",         Dict.getNote(41)  );
		assertEquals( "Ges",       Dict.getNote(42)  );
		assertEquals( "G",         Dict.getNote(43)  );
		assertEquals( "As",        Dict.getNote(44)  );
		assertEquals( "A",         Dict.getNote(45)  );
		assertEquals( "B",         Dict.getNote(46)  );
		assertEquals( "H",         Dict.getNote(47)  );
		assertEquals( "c",         Dict.getNote(48)  );
		assertEquals( "des",       Dict.getNote(49)  );
		assertEquals( "d",         Dict.getNote(50)  );
		assertEquals( "es",        Dict.getNote(51)  );
		assertEquals( "e",         Dict.getNote(52)  );
		assertEquals( "f",         Dict.getNote(53)  );
		assertEquals( "ges",       Dict.getNote(54)  );
		assertEquals( "g",         Dict.getNote(55)  );
		assertEquals( "as",        Dict.getNote(56)  );
		assertEquals( "a",         Dict.getNote(57)  );
		assertEquals( "b",         Dict.getNote(58)  );
		assertEquals( "h",         Dict.getNote(59)  );
		assertEquals( "c'",        Dict.getNote(60)  );
		assertEquals( "des'",      Dict.getNote(61)  );
		assertEquals( "d'",        Dict.getNote(62)  );
		assertEquals( "es'",       Dict.getNote(63)  );
		assertEquals( "e'",        Dict.getNote(64)  );
		assertEquals( "f'",        Dict.getNote(65)  );
		assertEquals( "ges'",      Dict.getNote(66)  );
		assertEquals( "g'",        Dict.getNote(67)  );
		assertEquals( "as'",       Dict.getNote(68)  );
		assertEquals( "a'",        Dict.getNote(69)  );
		assertEquals( "b'",        Dict.getNote(70)  );
		assertEquals( "h'",        Dict.getNote(71)  );
		assertEquals( "c''",       Dict.getNote(72)  );
		assertEquals( "des''",     Dict.getNote(73)  );
		assertEquals( "d''",       Dict.getNote(74)  );
		assertEquals( "es''",      Dict.getNote(75)  );
		assertEquals( "e''",       Dict.getNote(76)  );
		assertEquals( "f''",       Dict.getNote(77)  );
		assertEquals( "ges''",     Dict.getNote(78)  );
		assertEquals( "g''",       Dict.getNote(79)  );
		assertEquals( "as''",      Dict.getNote(80)  );
		assertEquals( "a''",       Dict.getNote(81)  );
		assertEquals( "b''",       Dict.getNote(82)  );
		assertEquals( "h''",       Dict.getNote(83)  );
		assertEquals( "c'''",      Dict.getNote(84)  );
		assertEquals( "des'''",    Dict.getNote(85)  );
		assertEquals( "d'''",      Dict.getNote(86)  );
		assertEquals( "es'''",     Dict.getNote(87)  );
		assertEquals( "e'''",      Dict.getNote(88)  );
		assertEquals( "f'''",      Dict.getNote(89)  );
		assertEquals( "ges'''",    Dict.getNote(90)  );
		assertEquals( "g'''",      Dict.getNote(91)  );
		assertEquals( "as'''",     Dict.getNote(92)  );
		assertEquals( "a'''",      Dict.getNote(93)  );
		assertEquals( "b'''",      Dict.getNote(94)  );
		assertEquals( "h'''",      Dict.getNote(95)  );
		assertEquals( "c''''",     Dict.getNote(96)  );
		assertEquals( "des''''",   Dict.getNote(97)  );
		assertEquals( "d''''",     Dict.getNote(98)  );
		assertEquals( "es''''",    Dict.getNote(99)  );
		assertEquals( "e''''",     Dict.getNote(100) );
		assertEquals( "f''''",     Dict.getNote(101) );
		assertEquals( "ges''''",   Dict.getNote(102) );
		assertEquals( "g''''",     Dict.getNote(103) );
		assertEquals( "as''''",    Dict.getNote(104) );
		assertEquals( "a''''",     Dict.getNote(105) );
		assertEquals( "b''''",     Dict.getNote(106) );
		assertEquals( "h''''",     Dict.getNote(107) );
		assertEquals( "c'''''",    Dict.getNote(108) );
		assertEquals( "des'''''",  Dict.getNote(109) );
		assertEquals( "d'''''",    Dict.getNote(110) );
		assertEquals( "es'''''",   Dict.getNote(111) );
		assertEquals( "e'''''",    Dict.getNote(112) );
		assertEquals( "f'''''",    Dict.getNote(113) );
		assertEquals( "ges'''''",  Dict.getNote(114) );
		assertEquals( "g'''''",    Dict.getNote(115) );
		assertEquals( "as'''''",   Dict.getNote(116) );
		assertEquals( "a'''''",    Dict.getNote(117) );
		assertEquals( "b'''''",    Dict.getNote(118) );
		assertEquals( "h'''''",    Dict.getNote(119) );
		assertEquals( "c''''''",   Dict.getNote(120) );
		assertEquals( "des''''''", Dict.getNote(121) );
		assertEquals( "d''''''",   Dict.getNote(122) );
		assertEquals( "es''''''",  Dict.getNote(123) );
		assertEquals( "e''''''",   Dict.getNote(124) );
		assertEquals( "f''''''",   Dict.getNote(125) );
		assertEquals( "ges''''''", Dict.getNote(126) );
		assertEquals( "g''''''",   Dict.getNote(127) );
		
		// switch back to default
		cbxs[1].setSelectedIndex(0); 
		cbxs[2].setSelectedIndex(0);
		cbxs[3].setSelectedIndex(0);
	}
	
	/**
	 * Tests all octave configurations.
	 * 
	 * Using all possible combinations of the following configs:
	 * 
	 * - note system:
	 *     - international lc
	 *     - international uc
	 *     - italian lc
	 *     - italian uc
	 *     - german lc
	 *     - german uc
	 * - half tone:
	 *     - sharp
	 *     - flat
	 *     - diesis
	 *     - bemolle
	 *     - -is
	 *     - -es
	 * - octave naming:
	 *     - +n/-n
	 *     - +/-
	 *     - international
	 *     - german
	 */
	@Test
	public void testOctaveSystemsAutomated() {
		
		StringBuilder str = new StringBuilder("");
		
		// note system
		for (int i = 0; i < 6; i++) {
			cbxs[1].setSelectedIndex(i);
			
			// half tone
			for (int j = 0; j < 6; j++) {
				cbxs[2].setSelectedIndex(j);
				
				// octave naming
				for (int k = 0; k < 4; k++) {
					cbxs[3].setSelectedIndex(k);
					
					str.append(
						Config.get(Config.NOTE) + "/"
						+ Config.get(Config.HALF_TONE) + "/"
						+ Config.get(Config.HALF_TONE) + ":\n"
					);
					
					for (int num = 0; num < 128; num++) {
						if (num > 0)
							str.append(",");
						str.append(Dict.getNote(num));
					}
					
					str.append("\n");
				}
			}
		}
		
		assertEquals(
			"cbx_note_id_international_lc/cbx_halftone_id_sharp/cbx_halftone_id_sharp:\n"
			+ "c-5,c#-5,d-5,d#-5,e-5,f-5,f#-5,g-5,g#-5,a-5,a#-5,b-5,c-4,c#-4,d-4,d#-4,e-4,f-4,f#-4,g-4,g#-4,a-4,a#-4,b-4,c-3,c#-3,d-3,d#-3,e-3,f-3,f#-3,g-3,g#-3,a-3,a#-3,b-3,c-2,c#-2,d-2,d#-2,e-2,f-2,f#-2,g-2,g#-2,a-2,a#-2,b-2,c-,c#-,d-,d#-,e-,f-,f#-,g-,g#-,a-,a#-,b-,c,c#,d,d#,e,f,f#,g,g#,a,a#,b,c+,c#+,d+,d#+,e+,f+,f#+,g+,g#+,a+,a#+,b+,c+2,c#+2,d+2,d#+2,e+2,f+2,f#+2,g+2,g#+2,a+2,a#+2,b+2,c+3,c#+3,d+3,d#+3,e+3,f+3,f#+3,g+3,g#+3,a+3,a#+3,b+3,c+4,c#+4,d+4,d#+4,e+4,f+4,f#+4,g+4,g#+4,a+4,a#+4,b+4,c+5,c#+5,d+5,d#+5,e+5,f+5,f#+5,g+5\n"
			+ "cbx_note_id_international_lc/cbx_halftone_id_sharp/cbx_halftone_id_sharp:\n"
			+ "c-----,c#-----,d-----,d#-----,e-----,f-----,f#-----,g-----,g#-----,a-----,a#-----,b-----,c----,c#----,d----,d#----,e----,f----,f#----,g----,g#----,a----,a#----,b----,c---,c#---,d---,d#---,e---,f---,f#---,g---,g#---,a---,a#---,b---,c--,c#--,d--,d#--,e--,f--,f#--,g--,g#--,a--,a#--,b--,c-,c#-,d-,d#-,e-,f-,f#-,g-,g#-,a-,a#-,b-,c,c#,d,d#,e,f,f#,g,g#,a,a#,b,c+,c#+,d+,d#+,e+,f+,f#+,g+,g#+,a+,a#+,b+,c++,c#++,d++,d#++,e++,f++,f#++,g++,g#++,a++,a#++,b++,c+++,c#+++,d+++,d#+++,e+++,f+++,f#+++,g+++,g#+++,a+++,a#+++,b+++,c++++,c#++++,d++++,d#++++,e++++,f++++,f#++++,g++++,g#++++,a++++,a#++++,b++++,c+++++,c#+++++,d+++++,d#+++++,e+++++,f+++++,f#+++++,g+++++\n"
			+ "cbx_note_id_international_lc/cbx_halftone_id_sharp/cbx_halftone_id_sharp:\n"
			+ "c-1,c#-1,d-1,d#-1,e-1,f-1,f#-1,g-1,g#-1,a-1,a#-1,b-1,c0,c#0,d0,d#0,e0,f0,f#0,g0,g#0,a0,a#0,b0,c1,c#1,d1,d#1,e1,f1,f#1,g1,g#1,a1,a#1,b1,c2,c#2,d2,d#2,e2,f2,f#2,g2,g#2,a2,a#2,b2,c3,c#3,d3,d#3,e3,f3,f#3,g3,g#3,a3,a#3,b3,c4,c#4,d4,d#4,e4,f4,f#4,g4,g#4,a4,a#4,b4,c5,c#5,d5,d#5,e5,f5,f#5,g5,g#5,a5,a#5,b5,c6,c#6,d6,d#6,e6,f6,f#6,g6,g#6,a6,a#6,b6,c7,c#7,d7,d#7,e7,f7,f#7,g7,g#7,a7,a#7,b7,c8,c#8,d8,d#8,e8,f8,f#8,g8,g#8,a8,a#8,b8,c9,c#9,d9,d#9,e9,f9,f#9,g9\n"
			+ "cbx_note_id_international_lc/cbx_halftone_id_sharp/cbx_halftone_id_sharp:\n"
			+ "C''',C#''',D''',D#''',E''',F''',F#''',G''',G#''',A''',A#''',B''',C'',C#'',D'',D#'',E'',F'',F#'',G'',G#'',A'',A#'',B'',C',C#',D',D#',E',F',F#',G',G#',A',A#',B',C,C#,D,D#,E,F,F#,G,G#,A,A#,B,c,c#,d,d#,e,f,f#,g,g#,a,a#,b,c',c#',d',d#',e',f',f#',g',g#',a',a#',b',c'',c#'',d'',d#'',e'',f'',f#'',g'',g#'',a'',a#'',b'',c''',c#''',d''',d#''',e''',f''',f#''',g''',g#''',a''',a#''',b''',c'''',c#'''',d'''',d#'''',e'''',f'''',f#'''',g'''',g#'''',a'''',a#'''',b'''',c''''',c#''''',d''''',d#''''',e''''',f''''',f#''''',g''''',g#''''',a''''',a#''''',b''''',c'''''',c#'''''',d'''''',d#'''''',e'''''',f'''''',f#'''''',g''''''\n"
			+ "cbx_note_id_international_lc/cbx_halftone_id_flat/cbx_halftone_id_flat:\n"
			+ "c-5,db-5,d-5,eb-5,e-5,f-5,gb-5,g-5,ab-5,a-5,bb-5,b-5,c-4,db-4,d-4,eb-4,e-4,f-4,gb-4,g-4,ab-4,a-4,bb-4,b-4,c-3,db-3,d-3,eb-3,e-3,f-3,gb-3,g-3,ab-3,a-3,bb-3,b-3,c-2,db-2,d-2,eb-2,e-2,f-2,gb-2,g-2,ab-2,a-2,bb-2,b-2,c-,db-,d-,eb-,e-,f-,gb-,g-,ab-,a-,bb-,b-,c,db,d,eb,e,f,gb,g,ab,a,bb,b,c+,db+,d+,eb+,e+,f+,gb+,g+,ab+,a+,bb+,b+,c+2,db+2,d+2,eb+2,e+2,f+2,gb+2,g+2,ab+2,a+2,bb+2,b+2,c+3,db+3,d+3,eb+3,e+3,f+3,gb+3,g+3,ab+3,a+3,bb+3,b+3,c+4,db+4,d+4,eb+4,e+4,f+4,gb+4,g+4,ab+4,a+4,bb+4,b+4,c+5,db+5,d+5,eb+5,e+5,f+5,gb+5,g+5\n"
			+ "cbx_note_id_international_lc/cbx_halftone_id_flat/cbx_halftone_id_flat:\n"
			+ "c-----,db-----,d-----,eb-----,e-----,f-----,gb-----,g-----,ab-----,a-----,bb-----,b-----,c----,db----,d----,eb----,e----,f----,gb----,g----,ab----,a----,bb----,b----,c---,db---,d---,eb---,e---,f---,gb---,g---,ab---,a---,bb---,b---,c--,db--,d--,eb--,e--,f--,gb--,g--,ab--,a--,bb--,b--,c-,db-,d-,eb-,e-,f-,gb-,g-,ab-,a-,bb-,b-,c,db,d,eb,e,f,gb,g,ab,a,bb,b,c+,db+,d+,eb+,e+,f+,gb+,g+,ab+,a+,bb+,b+,c++,db++,d++,eb++,e++,f++,gb++,g++,ab++,a++,bb++,b++,c+++,db+++,d+++,eb+++,e+++,f+++,gb+++,g+++,ab+++,a+++,bb+++,b+++,c++++,db++++,d++++,eb++++,e++++,f++++,gb++++,g++++,ab++++,a++++,bb++++,b++++,c+++++,db+++++,d+++++,eb+++++,e+++++,f+++++,gb+++++,g+++++\n"
			+ "cbx_note_id_international_lc/cbx_halftone_id_flat/cbx_halftone_id_flat:\n"
			+ "c-1,db-1,d-1,eb-1,e-1,f-1,gb-1,g-1,ab-1,a-1,bb-1,b-1,c0,db0,d0,eb0,e0,f0,gb0,g0,ab0,a0,bb0,b0,c1,db1,d1,eb1,e1,f1,gb1,g1,ab1,a1,bb1,b1,c2,db2,d2,eb2,e2,f2,gb2,g2,ab2,a2,bb2,b2,c3,db3,d3,eb3,e3,f3,gb3,g3,ab3,a3,bb3,b3,c4,db4,d4,eb4,e4,f4,gb4,g4,ab4,a4,bb4,b4,c5,db5,d5,eb5,e5,f5,gb5,g5,ab5,a5,bb5,b5,c6,db6,d6,eb6,e6,f6,gb6,g6,ab6,a6,bb6,b6,c7,db7,d7,eb7,e7,f7,gb7,g7,ab7,a7,bb7,b7,c8,db8,d8,eb8,e8,f8,gb8,g8,ab8,a8,bb8,b8,c9,db9,d9,eb9,e9,f9,gb9,g9\n"
			+ "cbx_note_id_international_lc/cbx_halftone_id_flat/cbx_halftone_id_flat:\n"
			+ "C''',Db''',D''',Eb''',E''',F''',Gb''',G''',Ab''',A''',Bb''',B''',C'',Db'',D'',Eb'',E'',F'',Gb'',G'',Ab'',A'',Bb'',B'',C',Db',D',Eb',E',F',Gb',G',Ab',A',Bb',B',C,Db,D,Eb,E,F,Gb,G,Ab,A,Bb,B,c,db,d,eb,e,f,gb,g,ab,a,bb,b,c',db',d',eb',e',f',gb',g',ab',a',bb',b',c'',db'',d'',eb'',e'',f'',gb'',g'',ab'',a'',bb'',b'',c''',db''',d''',eb''',e''',f''',gb''',g''',ab''',a''',bb''',b''',c'''',db'''',d'''',eb'''',e'''',f'''',gb'''',g'''',ab'''',a'''',bb'''',b'''',c''''',db''''',d''''',eb''''',e''''',f''''',gb''''',g''''',ab''''',a''''',bb''''',b''''',c'''''',db'''''',d'''''',eb'''''',e'''''',f'''''',gb'''''',g''''''\n"
			+ "cbx_note_id_international_lc/cbx_halftone_id_diesis/cbx_halftone_id_diesis:\n"
			+ "c-5,c-diesis-5,d-5,d-diesis-5,e-5,f-5,f-diesis-5,g-5,g-diesis-5,a-5,a-diesis-5,b-5,c-4,c-diesis-4,d-4,d-diesis-4,e-4,f-4,f-diesis-4,g-4,g-diesis-4,a-4,a-diesis-4,b-4,c-3,c-diesis-3,d-3,d-diesis-3,e-3,f-3,f-diesis-3,g-3,g-diesis-3,a-3,a-diesis-3,b-3,c-2,c-diesis-2,d-2,d-diesis-2,e-2,f-2,f-diesis-2,g-2,g-diesis-2,a-2,a-diesis-2,b-2,c-,c-diesis-,d-,d-diesis-,e-,f-,f-diesis-,g-,g-diesis-,a-,a-diesis-,b-,c,c-diesis,d,d-diesis,e,f,f-diesis,g,g-diesis,a,a-diesis,b,c+,c-diesis+,d+,d-diesis+,e+,f+,f-diesis+,g+,g-diesis+,a+,a-diesis+,b+,c+2,c-diesis+2,d+2,d-diesis+2,e+2,f+2,f-diesis+2,g+2,g-diesis+2,a+2,a-diesis+2,b+2,c+3,c-diesis+3,d+3,d-diesis+3,e+3,f+3,f-diesis+3,g+3,g-diesis+3,a+3,a-diesis+3,b+3,c+4,c-diesis+4,d+4,d-diesis+4,e+4,f+4,f-diesis+4,g+4,g-diesis+4,a+4,a-diesis+4,b+4,c+5,c-diesis+5,d+5,d-diesis+5,e+5,f+5,f-diesis+5,g+5\n"
			+ "cbx_note_id_international_lc/cbx_halftone_id_diesis/cbx_halftone_id_diesis:\n"
			+ "c-----,c-diesis-----,d-----,d-diesis-----,e-----,f-----,f-diesis-----,g-----,g-diesis-----,a-----,a-diesis-----,b-----,c----,c-diesis----,d----,d-diesis----,e----,f----,f-diesis----,g----,g-diesis----,a----,a-diesis----,b----,c---,c-diesis---,d---,d-diesis---,e---,f---,f-diesis---,g---,g-diesis---,a---,a-diesis---,b---,c--,c-diesis--,d--,d-diesis--,e--,f--,f-diesis--,g--,g-diesis--,a--,a-diesis--,b--,c-,c-diesis-,d-,d-diesis-,e-,f-,f-diesis-,g-,g-diesis-,a-,a-diesis-,b-,c,c-diesis,d,d-diesis,e,f,f-diesis,g,g-diesis,a,a-diesis,b,c+,c-diesis+,d+,d-diesis+,e+,f+,f-diesis+,g+,g-diesis+,a+,a-diesis+,b+,c++,c-diesis++,d++,d-diesis++,e++,f++,f-diesis++,g++,g-diesis++,a++,a-diesis++,b++,c+++,c-diesis+++,d+++,d-diesis+++,e+++,f+++,f-diesis+++,g+++,g-diesis+++,a+++,a-diesis+++,b+++,c++++,c-diesis++++,d++++,d-diesis++++,e++++,f++++,f-diesis++++,g++++,g-diesis++++,a++++,a-diesis++++,b++++,c+++++,c-diesis+++++,d+++++,d-diesis+++++,e+++++,f+++++,f-diesis+++++,g+++++\n"
			+ "cbx_note_id_international_lc/cbx_halftone_id_diesis/cbx_halftone_id_diesis:\n"
			+ "c-1,c-diesis-1,d-1,d-diesis-1,e-1,f-1,f-diesis-1,g-1,g-diesis-1,a-1,a-diesis-1,b-1,c0,c-diesis0,d0,d-diesis0,e0,f0,f-diesis0,g0,g-diesis0,a0,a-diesis0,b0,c1,c-diesis1,d1,d-diesis1,e1,f1,f-diesis1,g1,g-diesis1,a1,a-diesis1,b1,c2,c-diesis2,d2,d-diesis2,e2,f2,f-diesis2,g2,g-diesis2,a2,a-diesis2,b2,c3,c-diesis3,d3,d-diesis3,e3,f3,f-diesis3,g3,g-diesis3,a3,a-diesis3,b3,c4,c-diesis4,d4,d-diesis4,e4,f4,f-diesis4,g4,g-diesis4,a4,a-diesis4,b4,c5,c-diesis5,d5,d-diesis5,e5,f5,f-diesis5,g5,g-diesis5,a5,a-diesis5,b5,c6,c-diesis6,d6,d-diesis6,e6,f6,f-diesis6,g6,g-diesis6,a6,a-diesis6,b6,c7,c-diesis7,d7,d-diesis7,e7,f7,f-diesis7,g7,g-diesis7,a7,a-diesis7,b7,c8,c-diesis8,d8,d-diesis8,e8,f8,f-diesis8,g8,g-diesis8,a8,a-diesis8,b8,c9,c-diesis9,d9,d-diesis9,e9,f9,f-diesis9,g9\n"
			+ "cbx_note_id_international_lc/cbx_halftone_id_diesis/cbx_halftone_id_diesis:\n"
			+ "C''',C-diesis''',D''',D-diesis''',E''',F''',F-diesis''',G''',G-diesis''',A''',A-diesis''',B''',C'',C-diesis'',D'',D-diesis'',E'',F'',F-diesis'',G'',G-diesis'',A'',A-diesis'',B'',C',C-diesis',D',D-diesis',E',F',F-diesis',G',G-diesis',A',A-diesis',B',C,C-diesis,D,D-diesis,E,F,F-diesis,G,G-diesis,A,A-diesis,B,c,c-diesis,d,d-diesis,e,f,f-diesis,g,g-diesis,a,a-diesis,b,c',c-diesis',d',d-diesis',e',f',f-diesis',g',g-diesis',a',a-diesis',b',c'',c-diesis'',d'',d-diesis'',e'',f'',f-diesis'',g'',g-diesis'',a'',a-diesis'',b'',c''',c-diesis''',d''',d-diesis''',e''',f''',f-diesis''',g''',g-diesis''',a''',a-diesis''',b''',c'''',c-diesis'''',d'''',d-diesis'''',e'''',f'''',f-diesis'''',g'''',g-diesis'''',a'''',a-diesis'''',b'''',c''''',c-diesis''''',d''''',d-diesis''''',e''''',f''''',f-diesis''''',g''''',g-diesis''''',a''''',a-diesis''''',b''''',c'''''',c-diesis'''''',d'''''',d-diesis'''''',e'''''',f'''''',f-diesis'''''',g''''''\n"
			+ "cbx_note_id_international_lc/cbx_halftone_id_bemolle/cbx_halftone_id_bemolle:\n"
			+ "c-5,d-bemolle-5,d-5,e-bemolle-5,e-5,f-5,g-bemolle-5,g-5,a-bemolle-5,a-5,b-bemolle-5,b-5,c-4,d-bemolle-4,d-4,e-bemolle-4,e-4,f-4,g-bemolle-4,g-4,a-bemolle-4,a-4,b-bemolle-4,b-4,c-3,d-bemolle-3,d-3,e-bemolle-3,e-3,f-3,g-bemolle-3,g-3,a-bemolle-3,a-3,b-bemolle-3,b-3,c-2,d-bemolle-2,d-2,e-bemolle-2,e-2,f-2,g-bemolle-2,g-2,a-bemolle-2,a-2,b-bemolle-2,b-2,c-,d-bemolle-,d-,e-bemolle-,e-,f-,g-bemolle-,g-,a-bemolle-,a-,b-bemolle-,b-,c,d-bemolle,d,e-bemolle,e,f,g-bemolle,g,a-bemolle,a,b-bemolle,b,c+,d-bemolle+,d+,e-bemolle+,e+,f+,g-bemolle+,g+,a-bemolle+,a+,b-bemolle+,b+,c+2,d-bemolle+2,d+2,e-bemolle+2,e+2,f+2,g-bemolle+2,g+2,a-bemolle+2,a+2,b-bemolle+2,b+2,c+3,d-bemolle+3,d+3,e-bemolle+3,e+3,f+3,g-bemolle+3,g+3,a-bemolle+3,a+3,b-bemolle+3,b+3,c+4,d-bemolle+4,d+4,e-bemolle+4,e+4,f+4,g-bemolle+4,g+4,a-bemolle+4,a+4,b-bemolle+4,b+4,c+5,d-bemolle+5,d+5,e-bemolle+5,e+5,f+5,g-bemolle+5,g+5\n"
			+ "cbx_note_id_international_lc/cbx_halftone_id_bemolle/cbx_halftone_id_bemolle:\n"
			+ "c-----,d-bemolle-----,d-----,e-bemolle-----,e-----,f-----,g-bemolle-----,g-----,a-bemolle-----,a-----,b-bemolle-----,b-----,c----,d-bemolle----,d----,e-bemolle----,e----,f----,g-bemolle----,g----,a-bemolle----,a----,b-bemolle----,b----,c---,d-bemolle---,d---,e-bemolle---,e---,f---,g-bemolle---,g---,a-bemolle---,a---,b-bemolle---,b---,c--,d-bemolle--,d--,e-bemolle--,e--,f--,g-bemolle--,g--,a-bemolle--,a--,b-bemolle--,b--,c-,d-bemolle-,d-,e-bemolle-,e-,f-,g-bemolle-,g-,a-bemolle-,a-,b-bemolle-,b-,c,d-bemolle,d,e-bemolle,e,f,g-bemolle,g,a-bemolle,a,b-bemolle,b,c+,d-bemolle+,d+,e-bemolle+,e+,f+,g-bemolle+,g+,a-bemolle+,a+,b-bemolle+,b+,c++,d-bemolle++,d++,e-bemolle++,e++,f++,g-bemolle++,g++,a-bemolle++,a++,b-bemolle++,b++,c+++,d-bemolle+++,d+++,e-bemolle+++,e+++,f+++,g-bemolle+++,g+++,a-bemolle+++,a+++,b-bemolle+++,b+++,c++++,d-bemolle++++,d++++,e-bemolle++++,e++++,f++++,g-bemolle++++,g++++,a-bemolle++++,a++++,b-bemolle++++,b++++,c+++++,d-bemolle+++++,d+++++,e-bemolle+++++,e+++++,f+++++,g-bemolle+++++,g+++++\n"
			+ "cbx_note_id_international_lc/cbx_halftone_id_bemolle/cbx_halftone_id_bemolle:\n"
			+ "c-1,d-bemolle-1,d-1,e-bemolle-1,e-1,f-1,g-bemolle-1,g-1,a-bemolle-1,a-1,b-bemolle-1,b-1,c0,d-bemolle0,d0,e-bemolle0,e0,f0,g-bemolle0,g0,a-bemolle0,a0,b-bemolle0,b0,c1,d-bemolle1,d1,e-bemolle1,e1,f1,g-bemolle1,g1,a-bemolle1,a1,b-bemolle1,b1,c2,d-bemolle2,d2,e-bemolle2,e2,f2,g-bemolle2,g2,a-bemolle2,a2,b-bemolle2,b2,c3,d-bemolle3,d3,e-bemolle3,e3,f3,g-bemolle3,g3,a-bemolle3,a3,b-bemolle3,b3,c4,d-bemolle4,d4,e-bemolle4,e4,f4,g-bemolle4,g4,a-bemolle4,a4,b-bemolle4,b4,c5,d-bemolle5,d5,e-bemolle5,e5,f5,g-bemolle5,g5,a-bemolle5,a5,b-bemolle5,b5,c6,d-bemolle6,d6,e-bemolle6,e6,f6,g-bemolle6,g6,a-bemolle6,a6,b-bemolle6,b6,c7,d-bemolle7,d7,e-bemolle7,e7,f7,g-bemolle7,g7,a-bemolle7,a7,b-bemolle7,b7,c8,d-bemolle8,d8,e-bemolle8,e8,f8,g-bemolle8,g8,a-bemolle8,a8,b-bemolle8,b8,c9,d-bemolle9,d9,e-bemolle9,e9,f9,g-bemolle9,g9\n"
			+ "cbx_note_id_international_lc/cbx_halftone_id_bemolle/cbx_halftone_id_bemolle:\n"
			+ "C''',D-bemolle''',D''',E-bemolle''',E''',F''',G-bemolle''',G''',A-bemolle''',A''',B-bemolle''',B''',C'',D-bemolle'',D'',E-bemolle'',E'',F'',G-bemolle'',G'',A-bemolle'',A'',B-bemolle'',B'',C',D-bemolle',D',E-bemolle',E',F',G-bemolle',G',A-bemolle',A',B-bemolle',B',C,D-bemolle,D,E-bemolle,E,F,G-bemolle,G,A-bemolle,A,B-bemolle,B,c,d-bemolle,d,e-bemolle,e,f,g-bemolle,g,a-bemolle,a,b-bemolle,b,c',d-bemolle',d',e-bemolle',e',f',g-bemolle',g',a-bemolle',a',b-bemolle',b',c'',d-bemolle'',d'',e-bemolle'',e'',f'',g-bemolle'',g'',a-bemolle'',a'',b-bemolle'',b'',c''',d-bemolle''',d''',e-bemolle''',e''',f''',g-bemolle''',g''',a-bemolle''',a''',b-bemolle''',b''',c'''',d-bemolle'''',d'''',e-bemolle'''',e'''',f'''',g-bemolle'''',g'''',a-bemolle'''',a'''',b-bemolle'''',b'''',c''''',d-bemolle''''',d''''',e-bemolle''''',e''''',f''''',g-bemolle''''',g''''',a-bemolle''''',a''''',b-bemolle''''',b''''',c'''''',d-bemolle'''''',d'''''',e-bemolle'''''',e'''''',f'''''',g-bemolle'''''',g''''''\n"
			+ "cbx_note_id_international_lc/cbx_halftone_id_cis/cbx_halftone_id_cis:\n"
			+ "c-5,cis-5,d-5,dis-5,e-5,f-5,fis-5,g-5,gis-5,a-5,ais-5,b-5,c-4,cis-4,d-4,dis-4,e-4,f-4,fis-4,g-4,gis-4,a-4,ais-4,b-4,c-3,cis-3,d-3,dis-3,e-3,f-3,fis-3,g-3,gis-3,a-3,ais-3,b-3,c-2,cis-2,d-2,dis-2,e-2,f-2,fis-2,g-2,gis-2,a-2,ais-2,b-2,c-,cis-,d-,dis-,e-,f-,fis-,g-,gis-,a-,ais-,b-,c,cis,d,dis,e,f,fis,g,gis,a,ais,b,c+,cis+,d+,dis+,e+,f+,fis+,g+,gis+,a+,ais+,b+,c+2,cis+2,d+2,dis+2,e+2,f+2,fis+2,g+2,gis+2,a+2,ais+2,b+2,c+3,cis+3,d+3,dis+3,e+3,f+3,fis+3,g+3,gis+3,a+3,ais+3,b+3,c+4,cis+4,d+4,dis+4,e+4,f+4,fis+4,g+4,gis+4,a+4,ais+4,b+4,c+5,cis+5,d+5,dis+5,e+5,f+5,fis+5,g+5\n"
			+ "cbx_note_id_international_lc/cbx_halftone_id_cis/cbx_halftone_id_cis:\n"
			+ "c-----,cis-----,d-----,dis-----,e-----,f-----,fis-----,g-----,gis-----,a-----,ais-----,b-----,c----,cis----,d----,dis----,e----,f----,fis----,g----,gis----,a----,ais----,b----,c---,cis---,d---,dis---,e---,f---,fis---,g---,gis---,a---,ais---,b---,c--,cis--,d--,dis--,e--,f--,fis--,g--,gis--,a--,ais--,b--,c-,cis-,d-,dis-,e-,f-,fis-,g-,gis-,a-,ais-,b-,c,cis,d,dis,e,f,fis,g,gis,a,ais,b,c+,cis+,d+,dis+,e+,f+,fis+,g+,gis+,a+,ais+,b+,c++,cis++,d++,dis++,e++,f++,fis++,g++,gis++,a++,ais++,b++,c+++,cis+++,d+++,dis+++,e+++,f+++,fis+++,g+++,gis+++,a+++,ais+++,b+++,c++++,cis++++,d++++,dis++++,e++++,f++++,fis++++,g++++,gis++++,a++++,ais++++,b++++,c+++++,cis+++++,d+++++,dis+++++,e+++++,f+++++,fis+++++,g+++++\n"
			+ "cbx_note_id_international_lc/cbx_halftone_id_cis/cbx_halftone_id_cis:\n"
			+ "c-1,cis-1,d-1,dis-1,e-1,f-1,fis-1,g-1,gis-1,a-1,ais-1,b-1,c0,cis0,d0,dis0,e0,f0,fis0,g0,gis0,a0,ais0,b0,c1,cis1,d1,dis1,e1,f1,fis1,g1,gis1,a1,ais1,b1,c2,cis2,d2,dis2,e2,f2,fis2,g2,gis2,a2,ais2,b2,c3,cis3,d3,dis3,e3,f3,fis3,g3,gis3,a3,ais3,b3,c4,cis4,d4,dis4,e4,f4,fis4,g4,gis4,a4,ais4,b4,c5,cis5,d5,dis5,e5,f5,fis5,g5,gis5,a5,ais5,b5,c6,cis6,d6,dis6,e6,f6,fis6,g6,gis6,a6,ais6,b6,c7,cis7,d7,dis7,e7,f7,fis7,g7,gis7,a7,ais7,b7,c8,cis8,d8,dis8,e8,f8,fis8,g8,gis8,a8,ais8,b8,c9,cis9,d9,dis9,e9,f9,fis9,g9\n"
			+ "cbx_note_id_international_lc/cbx_halftone_id_cis/cbx_halftone_id_cis:\n"
			+ "C''',Cis''',D''',Dis''',E''',F''',Fis''',G''',Gis''',A''',Ais''',B''',C'',Cis'',D'',Dis'',E'',F'',Fis'',G'',Gis'',A'',Ais'',B'',C',Cis',D',Dis',E',F',Fis',G',Gis',A',Ais',B',C,Cis,D,Dis,E,F,Fis,G,Gis,A,Ais,B,c,cis,d,dis,e,f,fis,g,gis,a,ais,b,c',cis',d',dis',e',f',fis',g',gis',a',ais',b',c'',cis'',d'',dis'',e'',f'',fis'',g'',gis'',a'',ais'',b'',c''',cis''',d''',dis''',e''',f''',fis''',g''',gis''',a''',ais''',b''',c'''',cis'''',d'''',dis'''',e'''',f'''',fis'''',g'''',gis'''',a'''',ais'''',b'''',c''''',cis''''',d''''',dis''''',e''''',f''''',fis''''',g''''',gis''''',a''''',ais''''',b''''',c'''''',cis'''''',d'''''',dis'''''',e'''''',f'''''',fis'''''',g''''''\n"
			+ "cbx_note_id_international_lc/cbx_halftone_id_des/cbx_halftone_id_des:\n"
			+ "c-5,des-5,d-5,es-5,e-5,f-5,ges-5,g-5,as-5,a-5,bes-5,b-5,c-4,des-4,d-4,es-4,e-4,f-4,ges-4,g-4,as-4,a-4,bes-4,b-4,c-3,des-3,d-3,es-3,e-3,f-3,ges-3,g-3,as-3,a-3,bes-3,b-3,c-2,des-2,d-2,es-2,e-2,f-2,ges-2,g-2,as-2,a-2,bes-2,b-2,c-,des-,d-,es-,e-,f-,ges-,g-,as-,a-,bes-,b-,c,des,d,es,e,f,ges,g,as,a,bes,b,c+,des+,d+,es+,e+,f+,ges+,g+,as+,a+,bes+,b+,c+2,des+2,d+2,es+2,e+2,f+2,ges+2,g+2,as+2,a+2,bes+2,b+2,c+3,des+3,d+3,es+3,e+3,f+3,ges+3,g+3,as+3,a+3,bes+3,b+3,c+4,des+4,d+4,es+4,e+4,f+4,ges+4,g+4,as+4,a+4,bes+4,b+4,c+5,des+5,d+5,es+5,e+5,f+5,ges+5,g+5\n"
			+ "cbx_note_id_international_lc/cbx_halftone_id_des/cbx_halftone_id_des:\n"
			+ "c-----,des-----,d-----,es-----,e-----,f-----,ges-----,g-----,as-----,a-----,bes-----,b-----,c----,des----,d----,es----,e----,f----,ges----,g----,as----,a----,bes----,b----,c---,des---,d---,es---,e---,f---,ges---,g---,as---,a---,bes---,b---,c--,des--,d--,es--,e--,f--,ges--,g--,as--,a--,bes--,b--,c-,des-,d-,es-,e-,f-,ges-,g-,as-,a-,bes-,b-,c,des,d,es,e,f,ges,g,as,a,bes,b,c+,des+,d+,es+,e+,f+,ges+,g+,as+,a+,bes+,b+,c++,des++,d++,es++,e++,f++,ges++,g++,as++,a++,bes++,b++,c+++,des+++,d+++,es+++,e+++,f+++,ges+++,g+++,as+++,a+++,bes+++,b+++,c++++,des++++,d++++,es++++,e++++,f++++,ges++++,g++++,as++++,a++++,bes++++,b++++,c+++++,des+++++,d+++++,es+++++,e+++++,f+++++,ges+++++,g+++++\n"
			+ "cbx_note_id_international_lc/cbx_halftone_id_des/cbx_halftone_id_des:\n"
			+ "c-1,des-1,d-1,es-1,e-1,f-1,ges-1,g-1,as-1,a-1,bes-1,b-1,c0,des0,d0,es0,e0,f0,ges0,g0,as0,a0,bes0,b0,c1,des1,d1,es1,e1,f1,ges1,g1,as1,a1,bes1,b1,c2,des2,d2,es2,e2,f2,ges2,g2,as2,a2,bes2,b2,c3,des3,d3,es3,e3,f3,ges3,g3,as3,a3,bes3,b3,c4,des4,d4,es4,e4,f4,ges4,g4,as4,a4,bes4,b4,c5,des5,d5,es5,e5,f5,ges5,g5,as5,a5,bes5,b5,c6,des6,d6,es6,e6,f6,ges6,g6,as6,a6,bes6,b6,c7,des7,d7,es7,e7,f7,ges7,g7,as7,a7,bes7,b7,c8,des8,d8,es8,e8,f8,ges8,g8,as8,a8,bes8,b8,c9,des9,d9,es9,e9,f9,ges9,g9\n"
			+ "cbx_note_id_international_lc/cbx_halftone_id_des/cbx_halftone_id_des:\n"
			+ "C''',Des''',D''',Es''',E''',F''',Ges''',G''',As''',A''',Bes''',B''',C'',Des'',D'',Es'',E'',F'',Ges'',G'',As'',A'',Bes'',B'',C',Des',D',Es',E',F',Ges',G',As',A',Bes',B',C,Des,D,Es,E,F,Ges,G,As,A,Bes,B,c,des,d,es,e,f,ges,g,as,a,bes,b,c',des',d',es',e',f',ges',g',as',a',bes',b',c'',des'',d'',es'',e'',f'',ges'',g'',as'',a'',bes'',b'',c''',des''',d''',es''',e''',f''',ges''',g''',as''',a''',bes''',b''',c'''',des'''',d'''',es'''',e'''',f'''',ges'''',g'''',as'''',a'''',bes'''',b'''',c''''',des''''',d''''',es''''',e''''',f''''',ges''''',g''''',as''''',a''''',bes''''',b''''',c'''''',des'''''',d'''''',es'''''',e'''''',f'''''',ges'''''',g''''''\n"
			+ "cbx_note_id_international_uc/cbx_halftone_id_sharp/cbx_halftone_id_sharp:\n"
			+ "C-5,C#-5,D-5,D#-5,E-5,F-5,F#-5,G-5,G#-5,A-5,A#-5,B-5,C-4,C#-4,D-4,D#-4,E-4,F-4,F#-4,G-4,G#-4,A-4,A#-4,B-4,C-3,C#-3,D-3,D#-3,E-3,F-3,F#-3,G-3,G#-3,A-3,A#-3,B-3,C-2,C#-2,D-2,D#-2,E-2,F-2,F#-2,G-2,G#-2,A-2,A#-2,B-2,C-,C#-,D-,D#-,E-,F-,F#-,G-,G#-,A-,A#-,B-,C,C#,D,D#,E,F,F#,G,G#,A,A#,B,C+,C#+,D+,D#+,E+,F+,F#+,G+,G#+,A+,A#+,B+,C+2,C#+2,D+2,D#+2,E+2,F+2,F#+2,G+2,G#+2,A+2,A#+2,B+2,C+3,C#+3,D+3,D#+3,E+3,F+3,F#+3,G+3,G#+3,A+3,A#+3,B+3,C+4,C#+4,D+4,D#+4,E+4,F+4,F#+4,G+4,G#+4,A+4,A#+4,B+4,C+5,C#+5,D+5,D#+5,E+5,F+5,F#+5,G+5\n"
			+ "cbx_note_id_international_uc/cbx_halftone_id_sharp/cbx_halftone_id_sharp:\n"
			+ "C-----,C#-----,D-----,D#-----,E-----,F-----,F#-----,G-----,G#-----,A-----,A#-----,B-----,C----,C#----,D----,D#----,E----,F----,F#----,G----,G#----,A----,A#----,B----,C---,C#---,D---,D#---,E---,F---,F#---,G---,G#---,A---,A#---,B---,C--,C#--,D--,D#--,E--,F--,F#--,G--,G#--,A--,A#--,B--,C-,C#-,D-,D#-,E-,F-,F#-,G-,G#-,A-,A#-,B-,C,C#,D,D#,E,F,F#,G,G#,A,A#,B,C+,C#+,D+,D#+,E+,F+,F#+,G+,G#+,A+,A#+,B+,C++,C#++,D++,D#++,E++,F++,F#++,G++,G#++,A++,A#++,B++,C+++,C#+++,D+++,D#+++,E+++,F+++,F#+++,G+++,G#+++,A+++,A#+++,B+++,C++++,C#++++,D++++,D#++++,E++++,F++++,F#++++,G++++,G#++++,A++++,A#++++,B++++,C+++++,C#+++++,D+++++,D#+++++,E+++++,F+++++,F#+++++,G+++++\n"
			+ "cbx_note_id_international_uc/cbx_halftone_id_sharp/cbx_halftone_id_sharp:\n"
			+ "C-1,C#-1,D-1,D#-1,E-1,F-1,F#-1,G-1,G#-1,A-1,A#-1,B-1,C0,C#0,D0,D#0,E0,F0,F#0,G0,G#0,A0,A#0,B0,C1,C#1,D1,D#1,E1,F1,F#1,G1,G#1,A1,A#1,B1,C2,C#2,D2,D#2,E2,F2,F#2,G2,G#2,A2,A#2,B2,C3,C#3,D3,D#3,E3,F3,F#3,G3,G#3,A3,A#3,B3,C4,C#4,D4,D#4,E4,F4,F#4,G4,G#4,A4,A#4,B4,C5,C#5,D5,D#5,E5,F5,F#5,G5,G#5,A5,A#5,B5,C6,C#6,D6,D#6,E6,F6,F#6,G6,G#6,A6,A#6,B6,C7,C#7,D7,D#7,E7,F7,F#7,G7,G#7,A7,A#7,B7,C8,C#8,D8,D#8,E8,F8,F#8,G8,G#8,A8,A#8,B8,C9,C#9,D9,D#9,E9,F9,F#9,G9\n"
			+ "cbx_note_id_international_uc/cbx_halftone_id_sharp/cbx_halftone_id_sharp:\n"
			+ "C''',C#''',D''',D#''',E''',F''',F#''',G''',G#''',A''',A#''',B''',C'',C#'',D'',D#'',E'',F'',F#'',G'',G#'',A'',A#'',B'',C',C#',D',D#',E',F',F#',G',G#',A',A#',B',C,C#,D,D#,E,F,F#,G,G#,A,A#,B,c,c#,d,d#,e,f,f#,g,g#,a,a#,b,c',c#',d',d#',e',f',f#',g',g#',a',a#',b',c'',c#'',d'',d#'',e'',f'',f#'',g'',g#'',a'',a#'',b'',c''',c#''',d''',d#''',e''',f''',f#''',g''',g#''',a''',a#''',b''',c'''',c#'''',d'''',d#'''',e'''',f'''',f#'''',g'''',g#'''',a'''',a#'''',b'''',c''''',c#''''',d''''',d#''''',e''''',f''''',f#''''',g''''',g#''''',a''''',a#''''',b''''',c'''''',c#'''''',d'''''',d#'''''',e'''''',f'''''',f#'''''',g''''''\n"
			+ "cbx_note_id_international_uc/cbx_halftone_id_flat/cbx_halftone_id_flat:\n"
			+ "C-5,Db-5,D-5,Eb-5,E-5,F-5,Gb-5,G-5,Ab-5,A-5,Bb-5,B-5,C-4,Db-4,D-4,Eb-4,E-4,F-4,Gb-4,G-4,Ab-4,A-4,Bb-4,B-4,C-3,Db-3,D-3,Eb-3,E-3,F-3,Gb-3,G-3,Ab-3,A-3,Bb-3,B-3,C-2,Db-2,D-2,Eb-2,E-2,F-2,Gb-2,G-2,Ab-2,A-2,Bb-2,B-2,C-,Db-,D-,Eb-,E-,F-,Gb-,G-,Ab-,A-,Bb-,B-,C,Db,D,Eb,E,F,Gb,G,Ab,A,Bb,B,C+,Db+,D+,Eb+,E+,F+,Gb+,G+,Ab+,A+,Bb+,B+,C+2,Db+2,D+2,Eb+2,E+2,F+2,Gb+2,G+2,Ab+2,A+2,Bb+2,B+2,C+3,Db+3,D+3,Eb+3,E+3,F+3,Gb+3,G+3,Ab+3,A+3,Bb+3,B+3,C+4,Db+4,D+4,Eb+4,E+4,F+4,Gb+4,G+4,Ab+4,A+4,Bb+4,B+4,C+5,Db+5,D+5,Eb+5,E+5,F+5,Gb+5,G+5\n"
			+ "cbx_note_id_international_uc/cbx_halftone_id_flat/cbx_halftone_id_flat:\n"
			+ "C-----,Db-----,D-----,Eb-----,E-----,F-----,Gb-----,G-----,Ab-----,A-----,Bb-----,B-----,C----,Db----,D----,Eb----,E----,F----,Gb----,G----,Ab----,A----,Bb----,B----,C---,Db---,D---,Eb---,E---,F---,Gb---,G---,Ab---,A---,Bb---,B---,C--,Db--,D--,Eb--,E--,F--,Gb--,G--,Ab--,A--,Bb--,B--,C-,Db-,D-,Eb-,E-,F-,Gb-,G-,Ab-,A-,Bb-,B-,C,Db,D,Eb,E,F,Gb,G,Ab,A,Bb,B,C+,Db+,D+,Eb+,E+,F+,Gb+,G+,Ab+,A+,Bb+,B+,C++,Db++,D++,Eb++,E++,F++,Gb++,G++,Ab++,A++,Bb++,B++,C+++,Db+++,D+++,Eb+++,E+++,F+++,Gb+++,G+++,Ab+++,A+++,Bb+++,B+++,C++++,Db++++,D++++,Eb++++,E++++,F++++,Gb++++,G++++,Ab++++,A++++,Bb++++,B++++,C+++++,Db+++++,D+++++,Eb+++++,E+++++,F+++++,Gb+++++,G+++++\n"
			+ "cbx_note_id_international_uc/cbx_halftone_id_flat/cbx_halftone_id_flat:\n"
			+ "C-1,Db-1,D-1,Eb-1,E-1,F-1,Gb-1,G-1,Ab-1,A-1,Bb-1,B-1,C0,Db0,D0,Eb0,E0,F0,Gb0,G0,Ab0,A0,Bb0,B0,C1,Db1,D1,Eb1,E1,F1,Gb1,G1,Ab1,A1,Bb1,B1,C2,Db2,D2,Eb2,E2,F2,Gb2,G2,Ab2,A2,Bb2,B2,C3,Db3,D3,Eb3,E3,F3,Gb3,G3,Ab3,A3,Bb3,B3,C4,Db4,D4,Eb4,E4,F4,Gb4,G4,Ab4,A4,Bb4,B4,C5,Db5,D5,Eb5,E5,F5,Gb5,G5,Ab5,A5,Bb5,B5,C6,Db6,D6,Eb6,E6,F6,Gb6,G6,Ab6,A6,Bb6,B6,C7,Db7,D7,Eb7,E7,F7,Gb7,G7,Ab7,A7,Bb7,B7,C8,Db8,D8,Eb8,E8,F8,Gb8,G8,Ab8,A8,Bb8,B8,C9,Db9,D9,Eb9,E9,F9,Gb9,G9\n"
			+ "cbx_note_id_international_uc/cbx_halftone_id_flat/cbx_halftone_id_flat:\n"
			+ "C''',Db''',D''',Eb''',E''',F''',Gb''',G''',Ab''',A''',Bb''',B''',C'',Db'',D'',Eb'',E'',F'',Gb'',G'',Ab'',A'',Bb'',B'',C',Db',D',Eb',E',F',Gb',G',Ab',A',Bb',B',C,Db,D,Eb,E,F,Gb,G,Ab,A,Bb,B,c,db,d,eb,e,f,gb,g,ab,a,bb,b,c',db',d',eb',e',f',gb',g',ab',a',bb',b',c'',db'',d'',eb'',e'',f'',gb'',g'',ab'',a'',bb'',b'',c''',db''',d''',eb''',e''',f''',gb''',g''',ab''',a''',bb''',b''',c'''',db'''',d'''',eb'''',e'''',f'''',gb'''',g'''',ab'''',a'''',bb'''',b'''',c''''',db''''',d''''',eb''''',e''''',f''''',gb''''',g''''',ab''''',a''''',bb''''',b''''',c'''''',db'''''',d'''''',eb'''''',e'''''',f'''''',gb'''''',g''''''\n"
			+ "cbx_note_id_international_uc/cbx_halftone_id_diesis/cbx_halftone_id_diesis:\n"
			+ "C-5,C-diesis-5,D-5,D-diesis-5,E-5,F-5,F-diesis-5,G-5,G-diesis-5,A-5,A-diesis-5,B-5,C-4,C-diesis-4,D-4,D-diesis-4,E-4,F-4,F-diesis-4,G-4,G-diesis-4,A-4,A-diesis-4,B-4,C-3,C-diesis-3,D-3,D-diesis-3,E-3,F-3,F-diesis-3,G-3,G-diesis-3,A-3,A-diesis-3,B-3,C-2,C-diesis-2,D-2,D-diesis-2,E-2,F-2,F-diesis-2,G-2,G-diesis-2,A-2,A-diesis-2,B-2,C-,C-diesis-,D-,D-diesis-,E-,F-,F-diesis-,G-,G-diesis-,A-,A-diesis-,B-,C,C-diesis,D,D-diesis,E,F,F-diesis,G,G-diesis,A,A-diesis,B,C+,C-diesis+,D+,D-diesis+,E+,F+,F-diesis+,G+,G-diesis+,A+,A-diesis+,B+,C+2,C-diesis+2,D+2,D-diesis+2,E+2,F+2,F-diesis+2,G+2,G-diesis+2,A+2,A-diesis+2,B+2,C+3,C-diesis+3,D+3,D-diesis+3,E+3,F+3,F-diesis+3,G+3,G-diesis+3,A+3,A-diesis+3,B+3,C+4,C-diesis+4,D+4,D-diesis+4,E+4,F+4,F-diesis+4,G+4,G-diesis+4,A+4,A-diesis+4,B+4,C+5,C-diesis+5,D+5,D-diesis+5,E+5,F+5,F-diesis+5,G+5\n"
			+ "cbx_note_id_international_uc/cbx_halftone_id_diesis/cbx_halftone_id_diesis:\n"
			+ "C-----,C-diesis-----,D-----,D-diesis-----,E-----,F-----,F-diesis-----,G-----,G-diesis-----,A-----,A-diesis-----,B-----,C----,C-diesis----,D----,D-diesis----,E----,F----,F-diesis----,G----,G-diesis----,A----,A-diesis----,B----,C---,C-diesis---,D---,D-diesis---,E---,F---,F-diesis---,G---,G-diesis---,A---,A-diesis---,B---,C--,C-diesis--,D--,D-diesis--,E--,F--,F-diesis--,G--,G-diesis--,A--,A-diesis--,B--,C-,C-diesis-,D-,D-diesis-,E-,F-,F-diesis-,G-,G-diesis-,A-,A-diesis-,B-,C,C-diesis,D,D-diesis,E,F,F-diesis,G,G-diesis,A,A-diesis,B,C+,C-diesis+,D+,D-diesis+,E+,F+,F-diesis+,G+,G-diesis+,A+,A-diesis+,B+,C++,C-diesis++,D++,D-diesis++,E++,F++,F-diesis++,G++,G-diesis++,A++,A-diesis++,B++,C+++,C-diesis+++,D+++,D-diesis+++,E+++,F+++,F-diesis+++,G+++,G-diesis+++,A+++,A-diesis+++,B+++,C++++,C-diesis++++,D++++,D-diesis++++,E++++,F++++,F-diesis++++,G++++,G-diesis++++,A++++,A-diesis++++,B++++,C+++++,C-diesis+++++,D+++++,D-diesis+++++,E+++++,F+++++,F-diesis+++++,G+++++\n"
			+ "cbx_note_id_international_uc/cbx_halftone_id_diesis/cbx_halftone_id_diesis:\n"
			+ "C-1,C-diesis-1,D-1,D-diesis-1,E-1,F-1,F-diesis-1,G-1,G-diesis-1,A-1,A-diesis-1,B-1,C0,C-diesis0,D0,D-diesis0,E0,F0,F-diesis0,G0,G-diesis0,A0,A-diesis0,B0,C1,C-diesis1,D1,D-diesis1,E1,F1,F-diesis1,G1,G-diesis1,A1,A-diesis1,B1,C2,C-diesis2,D2,D-diesis2,E2,F2,F-diesis2,G2,G-diesis2,A2,A-diesis2,B2,C3,C-diesis3,D3,D-diesis3,E3,F3,F-diesis3,G3,G-diesis3,A3,A-diesis3,B3,C4,C-diesis4,D4,D-diesis4,E4,F4,F-diesis4,G4,G-diesis4,A4,A-diesis4,B4,C5,C-diesis5,D5,D-diesis5,E5,F5,F-diesis5,G5,G-diesis5,A5,A-diesis5,B5,C6,C-diesis6,D6,D-diesis6,E6,F6,F-diesis6,G6,G-diesis6,A6,A-diesis6,B6,C7,C-diesis7,D7,D-diesis7,E7,F7,F-diesis7,G7,G-diesis7,A7,A-diesis7,B7,C8,C-diesis8,D8,D-diesis8,E8,F8,F-diesis8,G8,G-diesis8,A8,A-diesis8,B8,C9,C-diesis9,D9,D-diesis9,E9,F9,F-diesis9,G9\n"
			+ "cbx_note_id_international_uc/cbx_halftone_id_diesis/cbx_halftone_id_diesis:\n"
			+ "C''',C-diesis''',D''',D-diesis''',E''',F''',F-diesis''',G''',G-diesis''',A''',A-diesis''',B''',C'',C-diesis'',D'',D-diesis'',E'',F'',F-diesis'',G'',G-diesis'',A'',A-diesis'',B'',C',C-diesis',D',D-diesis',E',F',F-diesis',G',G-diesis',A',A-diesis',B',C,C-diesis,D,D-diesis,E,F,F-diesis,G,G-diesis,A,A-diesis,B,c,c-diesis,d,d-diesis,e,f,f-diesis,g,g-diesis,a,a-diesis,b,c',c-diesis',d',d-diesis',e',f',f-diesis',g',g-diesis',a',a-diesis',b',c'',c-diesis'',d'',d-diesis'',e'',f'',f-diesis'',g'',g-diesis'',a'',a-diesis'',b'',c''',c-diesis''',d''',d-diesis''',e''',f''',f-diesis''',g''',g-diesis''',a''',a-diesis''',b''',c'''',c-diesis'''',d'''',d-diesis'''',e'''',f'''',f-diesis'''',g'''',g-diesis'''',a'''',a-diesis'''',b'''',c''''',c-diesis''''',d''''',d-diesis''''',e''''',f''''',f-diesis''''',g''''',g-diesis''''',a''''',a-diesis''''',b''''',c'''''',c-diesis'''''',d'''''',d-diesis'''''',e'''''',f'''''',f-diesis'''''',g''''''\n"
			+ "cbx_note_id_international_uc/cbx_halftone_id_bemolle/cbx_halftone_id_bemolle:\n"
			+ "C-5,D-bemolle-5,D-5,E-bemolle-5,E-5,F-5,G-bemolle-5,G-5,A-bemolle-5,A-5,B-bemolle-5,B-5,C-4,D-bemolle-4,D-4,E-bemolle-4,E-4,F-4,G-bemolle-4,G-4,A-bemolle-4,A-4,B-bemolle-4,B-4,C-3,D-bemolle-3,D-3,E-bemolle-3,E-3,F-3,G-bemolle-3,G-3,A-bemolle-3,A-3,B-bemolle-3,B-3,C-2,D-bemolle-2,D-2,E-bemolle-2,E-2,F-2,G-bemolle-2,G-2,A-bemolle-2,A-2,B-bemolle-2,B-2,C-,D-bemolle-,D-,E-bemolle-,E-,F-,G-bemolle-,G-,A-bemolle-,A-,B-bemolle-,B-,C,D-bemolle,D,E-bemolle,E,F,G-bemolle,G,A-bemolle,A,B-bemolle,B,C+,D-bemolle+,D+,E-bemolle+,E+,F+,G-bemolle+,G+,A-bemolle+,A+,B-bemolle+,B+,C+2,D-bemolle+2,D+2,E-bemolle+2,E+2,F+2,G-bemolle+2,G+2,A-bemolle+2,A+2,B-bemolle+2,B+2,C+3,D-bemolle+3,D+3,E-bemolle+3,E+3,F+3,G-bemolle+3,G+3,A-bemolle+3,A+3,B-bemolle+3,B+3,C+4,D-bemolle+4,D+4,E-bemolle+4,E+4,F+4,G-bemolle+4,G+4,A-bemolle+4,A+4,B-bemolle+4,B+4,C+5,D-bemolle+5,D+5,E-bemolle+5,E+5,F+5,G-bemolle+5,G+5\n"
			+ "cbx_note_id_international_uc/cbx_halftone_id_bemolle/cbx_halftone_id_bemolle:\n"
			+ "C-----,D-bemolle-----,D-----,E-bemolle-----,E-----,F-----,G-bemolle-----,G-----,A-bemolle-----,A-----,B-bemolle-----,B-----,C----,D-bemolle----,D----,E-bemolle----,E----,F----,G-bemolle----,G----,A-bemolle----,A----,B-bemolle----,B----,C---,D-bemolle---,D---,E-bemolle---,E---,F---,G-bemolle---,G---,A-bemolle---,A---,B-bemolle---,B---,C--,D-bemolle--,D--,E-bemolle--,E--,F--,G-bemolle--,G--,A-bemolle--,A--,B-bemolle--,B--,C-,D-bemolle-,D-,E-bemolle-,E-,F-,G-bemolle-,G-,A-bemolle-,A-,B-bemolle-,B-,C,D-bemolle,D,E-bemolle,E,F,G-bemolle,G,A-bemolle,A,B-bemolle,B,C+,D-bemolle+,D+,E-bemolle+,E+,F+,G-bemolle+,G+,A-bemolle+,A+,B-bemolle+,B+,C++,D-bemolle++,D++,E-bemolle++,E++,F++,G-bemolle++,G++,A-bemolle++,A++,B-bemolle++,B++,C+++,D-bemolle+++,D+++,E-bemolle+++,E+++,F+++,G-bemolle+++,G+++,A-bemolle+++,A+++,B-bemolle+++,B+++,C++++,D-bemolle++++,D++++,E-bemolle++++,E++++,F++++,G-bemolle++++,G++++,A-bemolle++++,A++++,B-bemolle++++,B++++,C+++++,D-bemolle+++++,D+++++,E-bemolle+++++,E+++++,F+++++,G-bemolle+++++,G+++++\n"
			+ "cbx_note_id_international_uc/cbx_halftone_id_bemolle/cbx_halftone_id_bemolle:\n"
			+ "C-1,D-bemolle-1,D-1,E-bemolle-1,E-1,F-1,G-bemolle-1,G-1,A-bemolle-1,A-1,B-bemolle-1,B-1,C0,D-bemolle0,D0,E-bemolle0,E0,F0,G-bemolle0,G0,A-bemolle0,A0,B-bemolle0,B0,C1,D-bemolle1,D1,E-bemolle1,E1,F1,G-bemolle1,G1,A-bemolle1,A1,B-bemolle1,B1,C2,D-bemolle2,D2,E-bemolle2,E2,F2,G-bemolle2,G2,A-bemolle2,A2,B-bemolle2,B2,C3,D-bemolle3,D3,E-bemolle3,E3,F3,G-bemolle3,G3,A-bemolle3,A3,B-bemolle3,B3,C4,D-bemolle4,D4,E-bemolle4,E4,F4,G-bemolle4,G4,A-bemolle4,A4,B-bemolle4,B4,C5,D-bemolle5,D5,E-bemolle5,E5,F5,G-bemolle5,G5,A-bemolle5,A5,B-bemolle5,B5,C6,D-bemolle6,D6,E-bemolle6,E6,F6,G-bemolle6,G6,A-bemolle6,A6,B-bemolle6,B6,C7,D-bemolle7,D7,E-bemolle7,E7,F7,G-bemolle7,G7,A-bemolle7,A7,B-bemolle7,B7,C8,D-bemolle8,D8,E-bemolle8,E8,F8,G-bemolle8,G8,A-bemolle8,A8,B-bemolle8,B8,C9,D-bemolle9,D9,E-bemolle9,E9,F9,G-bemolle9,G9\n"
			+ "cbx_note_id_international_uc/cbx_halftone_id_bemolle/cbx_halftone_id_bemolle:\n"
			+ "C''',D-bemolle''',D''',E-bemolle''',E''',F''',G-bemolle''',G''',A-bemolle''',A''',B-bemolle''',B''',C'',D-bemolle'',D'',E-bemolle'',E'',F'',G-bemolle'',G'',A-bemolle'',A'',B-bemolle'',B'',C',D-bemolle',D',E-bemolle',E',F',G-bemolle',G',A-bemolle',A',B-bemolle',B',C,D-bemolle,D,E-bemolle,E,F,G-bemolle,G,A-bemolle,A,B-bemolle,B,c,d-bemolle,d,e-bemolle,e,f,g-bemolle,g,a-bemolle,a,b-bemolle,b,c',d-bemolle',d',e-bemolle',e',f',g-bemolle',g',a-bemolle',a',b-bemolle',b',c'',d-bemolle'',d'',e-bemolle'',e'',f'',g-bemolle'',g'',a-bemolle'',a'',b-bemolle'',b'',c''',d-bemolle''',d''',e-bemolle''',e''',f''',g-bemolle''',g''',a-bemolle''',a''',b-bemolle''',b''',c'''',d-bemolle'''',d'''',e-bemolle'''',e'''',f'''',g-bemolle'''',g'''',a-bemolle'''',a'''',b-bemolle'''',b'''',c''''',d-bemolle''''',d''''',e-bemolle''''',e''''',f''''',g-bemolle''''',g''''',a-bemolle''''',a''''',b-bemolle''''',b''''',c'''''',d-bemolle'''''',d'''''',e-bemolle'''''',e'''''',f'''''',g-bemolle'''''',g''''''\n"
			+ "cbx_note_id_international_uc/cbx_halftone_id_cis/cbx_halftone_id_cis:\n"
			+ "C-5,Cis-5,D-5,Dis-5,E-5,F-5,Fis-5,G-5,Gis-5,A-5,Ais-5,B-5,C-4,Cis-4,D-4,Dis-4,E-4,F-4,Fis-4,G-4,Gis-4,A-4,Ais-4,B-4,C-3,Cis-3,D-3,Dis-3,E-3,F-3,Fis-3,G-3,Gis-3,A-3,Ais-3,B-3,C-2,Cis-2,D-2,Dis-2,E-2,F-2,Fis-2,G-2,Gis-2,A-2,Ais-2,B-2,C-,Cis-,D-,Dis-,E-,F-,Fis-,G-,Gis-,A-,Ais-,B-,C,Cis,D,Dis,E,F,Fis,G,Gis,A,Ais,B,C+,Cis+,D+,Dis+,E+,F+,Fis+,G+,Gis+,A+,Ais+,B+,C+2,Cis+2,D+2,Dis+2,E+2,F+2,Fis+2,G+2,Gis+2,A+2,Ais+2,B+2,C+3,Cis+3,D+3,Dis+3,E+3,F+3,Fis+3,G+3,Gis+3,A+3,Ais+3,B+3,C+4,Cis+4,D+4,Dis+4,E+4,F+4,Fis+4,G+4,Gis+4,A+4,Ais+4,B+4,C+5,Cis+5,D+5,Dis+5,E+5,F+5,Fis+5,G+5\n"
			+ "cbx_note_id_international_uc/cbx_halftone_id_cis/cbx_halftone_id_cis:\n"
			+ "C-----,Cis-----,D-----,Dis-----,E-----,F-----,Fis-----,G-----,Gis-----,A-----,Ais-----,B-----,C----,Cis----,D----,Dis----,E----,F----,Fis----,G----,Gis----,A----,Ais----,B----,C---,Cis---,D---,Dis---,E---,F---,Fis---,G---,Gis---,A---,Ais---,B---,C--,Cis--,D--,Dis--,E--,F--,Fis--,G--,Gis--,A--,Ais--,B--,C-,Cis-,D-,Dis-,E-,F-,Fis-,G-,Gis-,A-,Ais-,B-,C,Cis,D,Dis,E,F,Fis,G,Gis,A,Ais,B,C+,Cis+,D+,Dis+,E+,F+,Fis+,G+,Gis+,A+,Ais+,B+,C++,Cis++,D++,Dis++,E++,F++,Fis++,G++,Gis++,A++,Ais++,B++,C+++,Cis+++,D+++,Dis+++,E+++,F+++,Fis+++,G+++,Gis+++,A+++,Ais+++,B+++,C++++,Cis++++,D++++,Dis++++,E++++,F++++,Fis++++,G++++,Gis++++,A++++,Ais++++,B++++,C+++++,Cis+++++,D+++++,Dis+++++,E+++++,F+++++,Fis+++++,G+++++\n"
			+ "cbx_note_id_international_uc/cbx_halftone_id_cis/cbx_halftone_id_cis:\n"
			+ "C-1,Cis-1,D-1,Dis-1,E-1,F-1,Fis-1,G-1,Gis-1,A-1,Ais-1,B-1,C0,Cis0,D0,Dis0,E0,F0,Fis0,G0,Gis0,A0,Ais0,B0,C1,Cis1,D1,Dis1,E1,F1,Fis1,G1,Gis1,A1,Ais1,B1,C2,Cis2,D2,Dis2,E2,F2,Fis2,G2,Gis2,A2,Ais2,B2,C3,Cis3,D3,Dis3,E3,F3,Fis3,G3,Gis3,A3,Ais3,B3,C4,Cis4,D4,Dis4,E4,F4,Fis4,G4,Gis4,A4,Ais4,B4,C5,Cis5,D5,Dis5,E5,F5,Fis5,G5,Gis5,A5,Ais5,B5,C6,Cis6,D6,Dis6,E6,F6,Fis6,G6,Gis6,A6,Ais6,B6,C7,Cis7,D7,Dis7,E7,F7,Fis7,G7,Gis7,A7,Ais7,B7,C8,Cis8,D8,Dis8,E8,F8,Fis8,G8,Gis8,A8,Ais8,B8,C9,Cis9,D9,Dis9,E9,F9,Fis9,G9\n"
			+ "cbx_note_id_international_uc/cbx_halftone_id_cis/cbx_halftone_id_cis:\n"
			+ "C''',Cis''',D''',Dis''',E''',F''',Fis''',G''',Gis''',A''',Ais''',B''',C'',Cis'',D'',Dis'',E'',F'',Fis'',G'',Gis'',A'',Ais'',B'',C',Cis',D',Dis',E',F',Fis',G',Gis',A',Ais',B',C,Cis,D,Dis,E,F,Fis,G,Gis,A,Ais,B,c,cis,d,dis,e,f,fis,g,gis,a,ais,b,c',cis',d',dis',e',f',fis',g',gis',a',ais',b',c'',cis'',d'',dis'',e'',f'',fis'',g'',gis'',a'',ais'',b'',c''',cis''',d''',dis''',e''',f''',fis''',g''',gis''',a''',ais''',b''',c'''',cis'''',d'''',dis'''',e'''',f'''',fis'''',g'''',gis'''',a'''',ais'''',b'''',c''''',cis''''',d''''',dis''''',e''''',f''''',fis''''',g''''',gis''''',a''''',ais''''',b''''',c'''''',cis'''''',d'''''',dis'''''',e'''''',f'''''',fis'''''',g''''''\n"
			+ "cbx_note_id_international_uc/cbx_halftone_id_des/cbx_halftone_id_des:\n"
			+ "C-5,Des-5,D-5,Es-5,E-5,F-5,Ges-5,G-5,As-5,A-5,Bes-5,B-5,C-4,Des-4,D-4,Es-4,E-4,F-4,Ges-4,G-4,As-4,A-4,Bes-4,B-4,C-3,Des-3,D-3,Es-3,E-3,F-3,Ges-3,G-3,As-3,A-3,Bes-3,B-3,C-2,Des-2,D-2,Es-2,E-2,F-2,Ges-2,G-2,As-2,A-2,Bes-2,B-2,C-,Des-,D-,Es-,E-,F-,Ges-,G-,As-,A-,Bes-,B-,C,Des,D,Es,E,F,Ges,G,As,A,Bes,B,C+,Des+,D+,Es+,E+,F+,Ges+,G+,As+,A+,Bes+,B+,C+2,Des+2,D+2,Es+2,E+2,F+2,Ges+2,G+2,As+2,A+2,Bes+2,B+2,C+3,Des+3,D+3,Es+3,E+3,F+3,Ges+3,G+3,As+3,A+3,Bes+3,B+3,C+4,Des+4,D+4,Es+4,E+4,F+4,Ges+4,G+4,As+4,A+4,Bes+4,B+4,C+5,Des+5,D+5,Es+5,E+5,F+5,Ges+5,G+5\n"
			+ "cbx_note_id_international_uc/cbx_halftone_id_des/cbx_halftone_id_des:\n"
			+ "C-----,Des-----,D-----,Es-----,E-----,F-----,Ges-----,G-----,As-----,A-----,Bes-----,B-----,C----,Des----,D----,Es----,E----,F----,Ges----,G----,As----,A----,Bes----,B----,C---,Des---,D---,Es---,E---,F---,Ges---,G---,As---,A---,Bes---,B---,C--,Des--,D--,Es--,E--,F--,Ges--,G--,As--,A--,Bes--,B--,C-,Des-,D-,Es-,E-,F-,Ges-,G-,As-,A-,Bes-,B-,C,Des,D,Es,E,F,Ges,G,As,A,Bes,B,C+,Des+,D+,Es+,E+,F+,Ges+,G+,As+,A+,Bes+,B+,C++,Des++,D++,Es++,E++,F++,Ges++,G++,As++,A++,Bes++,B++,C+++,Des+++,D+++,Es+++,E+++,F+++,Ges+++,G+++,As+++,A+++,Bes+++,B+++,C++++,Des++++,D++++,Es++++,E++++,F++++,Ges++++,G++++,As++++,A++++,Bes++++,B++++,C+++++,Des+++++,D+++++,Es+++++,E+++++,F+++++,Ges+++++,G+++++\n"
			+ "cbx_note_id_international_uc/cbx_halftone_id_des/cbx_halftone_id_des:\n"
			+ "C-1,Des-1,D-1,Es-1,E-1,F-1,Ges-1,G-1,As-1,A-1,Bes-1,B-1,C0,Des0,D0,Es0,E0,F0,Ges0,G0,As0,A0,Bes0,B0,C1,Des1,D1,Es1,E1,F1,Ges1,G1,As1,A1,Bes1,B1,C2,Des2,D2,Es2,E2,F2,Ges2,G2,As2,A2,Bes2,B2,C3,Des3,D3,Es3,E3,F3,Ges3,G3,As3,A3,Bes3,B3,C4,Des4,D4,Es4,E4,F4,Ges4,G4,As4,A4,Bes4,B4,C5,Des5,D5,Es5,E5,F5,Ges5,G5,As5,A5,Bes5,B5,C6,Des6,D6,Es6,E6,F6,Ges6,G6,As6,A6,Bes6,B6,C7,Des7,D7,Es7,E7,F7,Ges7,G7,As7,A7,Bes7,B7,C8,Des8,D8,Es8,E8,F8,Ges8,G8,As8,A8,Bes8,B8,C9,Des9,D9,Es9,E9,F9,Ges9,G9\n"
			+ "cbx_note_id_international_uc/cbx_halftone_id_des/cbx_halftone_id_des:\n"
			+ "C''',Des''',D''',Es''',E''',F''',Ges''',G''',As''',A''',Bes''',B''',C'',Des'',D'',Es'',E'',F'',Ges'',G'',As'',A'',Bes'',B'',C',Des',D',Es',E',F',Ges',G',As',A',Bes',B',C,Des,D,Es,E,F,Ges,G,As,A,Bes,B,c,des,d,es,e,f,ges,g,as,a,bes,b,c',des',d',es',e',f',ges',g',as',a',bes',b',c'',des'',d'',es'',e'',f'',ges'',g'',as'',a'',bes'',b'',c''',des''',d''',es''',e''',f''',ges''',g''',as''',a''',bes''',b''',c'''',des'''',d'''',es'''',e'''',f'''',ges'''',g'''',as'''',a'''',bes'''',b'''',c''''',des''''',d''''',es''''',e''''',f''''',ges''''',g''''',as''''',a''''',bes''''',b''''',c'''''',des'''''',d'''''',es'''''',e'''''',f'''''',ges'''''',g''''''\n"
			+ "cbx_note_id_italian_lc/cbx_halftone_id_sharp/cbx_halftone_id_sharp:\n"
			+ "do-5,do#-5,re-5,re#-5,mi-5,fa-5,fa#-5,sol-5,sol#-5,la-5,la#-5,si-5,do-4,do#-4,re-4,re#-4,mi-4,fa-4,fa#-4,sol-4,sol#-4,la-4,la#-4,si-4,do-3,do#-3,re-3,re#-3,mi-3,fa-3,fa#-3,sol-3,sol#-3,la-3,la#-3,si-3,do-2,do#-2,re-2,re#-2,mi-2,fa-2,fa#-2,sol-2,sol#-2,la-2,la#-2,si-2,do-,do#-,re-,re#-,mi-,fa-,fa#-,sol-,sol#-,la-,la#-,si-,do,do#,re,re#,mi,fa,fa#,sol,sol#,la,la#,si,do+,do#+,re+,re#+,mi+,fa+,fa#+,sol+,sol#+,la+,la#+,si+,do+2,do#+2,re+2,re#+2,mi+2,fa+2,fa#+2,sol+2,sol#+2,la+2,la#+2,si+2,do+3,do#+3,re+3,re#+3,mi+3,fa+3,fa#+3,sol+3,sol#+3,la+3,la#+3,si+3,do+4,do#+4,re+4,re#+4,mi+4,fa+4,fa#+4,sol+4,sol#+4,la+4,la#+4,si+4,do+5,do#+5,re+5,re#+5,mi+5,fa+5,fa#+5,sol+5\n"
			+ "cbx_note_id_italian_lc/cbx_halftone_id_sharp/cbx_halftone_id_sharp:\n"
			+ "do-----,do#-----,re-----,re#-----,mi-----,fa-----,fa#-----,sol-----,sol#-----,la-----,la#-----,si-----,do----,do#----,re----,re#----,mi----,fa----,fa#----,sol----,sol#----,la----,la#----,si----,do---,do#---,re---,re#---,mi---,fa---,fa#---,sol---,sol#---,la---,la#---,si---,do--,do#--,re--,re#--,mi--,fa--,fa#--,sol--,sol#--,la--,la#--,si--,do-,do#-,re-,re#-,mi-,fa-,fa#-,sol-,sol#-,la-,la#-,si-,do,do#,re,re#,mi,fa,fa#,sol,sol#,la,la#,si,do+,do#+,re+,re#+,mi+,fa+,fa#+,sol+,sol#+,la+,la#+,si+,do++,do#++,re++,re#++,mi++,fa++,fa#++,sol++,sol#++,la++,la#++,si++,do+++,do#+++,re+++,re#+++,mi+++,fa+++,fa#+++,sol+++,sol#+++,la+++,la#+++,si+++,do++++,do#++++,re++++,re#++++,mi++++,fa++++,fa#++++,sol++++,sol#++++,la++++,la#++++,si++++,do+++++,do#+++++,re+++++,re#+++++,mi+++++,fa+++++,fa#+++++,sol+++++\n"
			+ "cbx_note_id_italian_lc/cbx_halftone_id_sharp/cbx_halftone_id_sharp:\n"
			+ "do-1,do#-1,re-1,re#-1,mi-1,fa-1,fa#-1,sol-1,sol#-1,la-1,la#-1,si-1,do0,do#0,re0,re#0,mi0,fa0,fa#0,sol0,sol#0,la0,la#0,si0,do1,do#1,re1,re#1,mi1,fa1,fa#1,sol1,sol#1,la1,la#1,si1,do2,do#2,re2,re#2,mi2,fa2,fa#2,sol2,sol#2,la2,la#2,si2,do3,do#3,re3,re#3,mi3,fa3,fa#3,sol3,sol#3,la3,la#3,si3,do4,do#4,re4,re#4,mi4,fa4,fa#4,sol4,sol#4,la4,la#4,si4,do5,do#5,re5,re#5,mi5,fa5,fa#5,sol5,sol#5,la5,la#5,si5,do6,do#6,re6,re#6,mi6,fa6,fa#6,sol6,sol#6,la6,la#6,si6,do7,do#7,re7,re#7,mi7,fa7,fa#7,sol7,sol#7,la7,la#7,si7,do8,do#8,re8,re#8,mi8,fa8,fa#8,sol8,sol#8,la8,la#8,si8,do9,do#9,re9,re#9,mi9,fa9,fa#9,sol9\n"
			+ "cbx_note_id_italian_lc/cbx_halftone_id_sharp/cbx_halftone_id_sharp:\n"
			+ "Do''',Do#''',Re''',Re#''',Mi''',Fa''',Fa#''',Sol''',Sol#''',La''',La#''',Si''',Do'',Do#'',Re'',Re#'',Mi'',Fa'',Fa#'',Sol'',Sol#'',La'',La#'',Si'',Do',Do#',Re',Re#',Mi',Fa',Fa#',Sol',Sol#',La',La#',Si',Do,Do#,Re,Re#,Mi,Fa,Fa#,Sol,Sol#,La,La#,Si,do,do#,re,re#,mi,fa,fa#,sol,sol#,la,la#,si,do',do#',re',re#',mi',fa',fa#',sol',sol#',la',la#',si',do'',do#'',re'',re#'',mi'',fa'',fa#'',sol'',sol#'',la'',la#'',si'',do''',do#''',re''',re#''',mi''',fa''',fa#''',sol''',sol#''',la''',la#''',si''',do'''',do#'''',re'''',re#'''',mi'''',fa'''',fa#'''',sol'''',sol#'''',la'''',la#'''',si'''',do''''',do#''''',re''''',re#''''',mi''''',fa''''',fa#''''',sol''''',sol#''''',la''''',la#''''',si''''',do'''''',do#'''''',re'''''',re#'''''',mi'''''',fa'''''',fa#'''''',sol''''''\n"
			+ "cbx_note_id_italian_lc/cbx_halftone_id_flat/cbx_halftone_id_flat:\n"
			+ "do-5,reb-5,re-5,mib-5,mi-5,fa-5,solb-5,sol-5,lab-5,la-5,sib-5,si-5,do-4,reb-4,re-4,mib-4,mi-4,fa-4,solb-4,sol-4,lab-4,la-4,sib-4,si-4,do-3,reb-3,re-3,mib-3,mi-3,fa-3,solb-3,sol-3,lab-3,la-3,sib-3,si-3,do-2,reb-2,re-2,mib-2,mi-2,fa-2,solb-2,sol-2,lab-2,la-2,sib-2,si-2,do-,reb-,re-,mib-,mi-,fa-,solb-,sol-,lab-,la-,sib-,si-,do,reb,re,mib,mi,fa,solb,sol,lab,la,sib,si,do+,reb+,re+,mib+,mi+,fa+,solb+,sol+,lab+,la+,sib+,si+,do+2,reb+2,re+2,mib+2,mi+2,fa+2,solb+2,sol+2,lab+2,la+2,sib+2,si+2,do+3,reb+3,re+3,mib+3,mi+3,fa+3,solb+3,sol+3,lab+3,la+3,sib+3,si+3,do+4,reb+4,re+4,mib+4,mi+4,fa+4,solb+4,sol+4,lab+4,la+4,sib+4,si+4,do+5,reb+5,re+5,mib+5,mi+5,fa+5,solb+5,sol+5\n"
			+ "cbx_note_id_italian_lc/cbx_halftone_id_flat/cbx_halftone_id_flat:\n"
			+ "do-----,reb-----,re-----,mib-----,mi-----,fa-----,solb-----,sol-----,lab-----,la-----,sib-----,si-----,do----,reb----,re----,mib----,mi----,fa----,solb----,sol----,lab----,la----,sib----,si----,do---,reb---,re---,mib---,mi---,fa---,solb---,sol---,lab---,la---,sib---,si---,do--,reb--,re--,mib--,mi--,fa--,solb--,sol--,lab--,la--,sib--,si--,do-,reb-,re-,mib-,mi-,fa-,solb-,sol-,lab-,la-,sib-,si-,do,reb,re,mib,mi,fa,solb,sol,lab,la,sib,si,do+,reb+,re+,mib+,mi+,fa+,solb+,sol+,lab+,la+,sib+,si+,do++,reb++,re++,mib++,mi++,fa++,solb++,sol++,lab++,la++,sib++,si++,do+++,reb+++,re+++,mib+++,mi+++,fa+++,solb+++,sol+++,lab+++,la+++,sib+++,si+++,do++++,reb++++,re++++,mib++++,mi++++,fa++++,solb++++,sol++++,lab++++,la++++,sib++++,si++++,do+++++,reb+++++,re+++++,mib+++++,mi+++++,fa+++++,solb+++++,sol+++++\n"
			+ "cbx_note_id_italian_lc/cbx_halftone_id_flat/cbx_halftone_id_flat:\n"
			+ "do-1,reb-1,re-1,mib-1,mi-1,fa-1,solb-1,sol-1,lab-1,la-1,sib-1,si-1,do0,reb0,re0,mib0,mi0,fa0,solb0,sol0,lab0,la0,sib0,si0,do1,reb1,re1,mib1,mi1,fa1,solb1,sol1,lab1,la1,sib1,si1,do2,reb2,re2,mib2,mi2,fa2,solb2,sol2,lab2,la2,sib2,si2,do3,reb3,re3,mib3,mi3,fa3,solb3,sol3,lab3,la3,sib3,si3,do4,reb4,re4,mib4,mi4,fa4,solb4,sol4,lab4,la4,sib4,si4,do5,reb5,re5,mib5,mi5,fa5,solb5,sol5,lab5,la5,sib5,si5,do6,reb6,re6,mib6,mi6,fa6,solb6,sol6,lab6,la6,sib6,si6,do7,reb7,re7,mib7,mi7,fa7,solb7,sol7,lab7,la7,sib7,si7,do8,reb8,re8,mib8,mi8,fa8,solb8,sol8,lab8,la8,sib8,si8,do9,reb9,re9,mib9,mi9,fa9,solb9,sol9\n"
			+ "cbx_note_id_italian_lc/cbx_halftone_id_flat/cbx_halftone_id_flat:\n"
			+ "Do''',Reb''',Re''',Mib''',Mi''',Fa''',Solb''',Sol''',Lab''',La''',Sib''',Si''',Do'',Reb'',Re'',Mib'',Mi'',Fa'',Solb'',Sol'',Lab'',La'',Sib'',Si'',Do',Reb',Re',Mib',Mi',Fa',Solb',Sol',Lab',La',Sib',Si',Do,Reb,Re,Mib,Mi,Fa,Solb,Sol,Lab,La,Sib,Si,do,reb,re,mib,mi,fa,solb,sol,lab,la,sib,si,do',reb',re',mib',mi',fa',solb',sol',lab',la',sib',si',do'',reb'',re'',mib'',mi'',fa'',solb'',sol'',lab'',la'',sib'',si'',do''',reb''',re''',mib''',mi''',fa''',solb''',sol''',lab''',la''',sib''',si''',do'''',reb'''',re'''',mib'''',mi'''',fa'''',solb'''',sol'''',lab'''',la'''',sib'''',si'''',do''''',reb''''',re''''',mib''''',mi''''',fa''''',solb''''',sol''''',lab''''',la''''',sib''''',si''''',do'''''',reb'''''',re'''''',mib'''''',mi'''''',fa'''''',solb'''''',sol''''''\n"
			+ "cbx_note_id_italian_lc/cbx_halftone_id_diesis/cbx_halftone_id_diesis:\n"
			+ "do-5,do-diesis-5,re-5,re-diesis-5,mi-5,fa-5,fa-diesis-5,sol-5,sol-diesis-5,la-5,la-diesis-5,si-5,do-4,do-diesis-4,re-4,re-diesis-4,mi-4,fa-4,fa-diesis-4,sol-4,sol-diesis-4,la-4,la-diesis-4,si-4,do-3,do-diesis-3,re-3,re-diesis-3,mi-3,fa-3,fa-diesis-3,sol-3,sol-diesis-3,la-3,la-diesis-3,si-3,do-2,do-diesis-2,re-2,re-diesis-2,mi-2,fa-2,fa-diesis-2,sol-2,sol-diesis-2,la-2,la-diesis-2,si-2,do-,do-diesis-,re-,re-diesis-,mi-,fa-,fa-diesis-,sol-,sol-diesis-,la-,la-diesis-,si-,do,do-diesis,re,re-diesis,mi,fa,fa-diesis,sol,sol-diesis,la,la-diesis,si,do+,do-diesis+,re+,re-diesis+,mi+,fa+,fa-diesis+,sol+,sol-diesis+,la+,la-diesis+,si+,do+2,do-diesis+2,re+2,re-diesis+2,mi+2,fa+2,fa-diesis+2,sol+2,sol-diesis+2,la+2,la-diesis+2,si+2,do+3,do-diesis+3,re+3,re-diesis+3,mi+3,fa+3,fa-diesis+3,sol+3,sol-diesis+3,la+3,la-diesis+3,si+3,do+4,do-diesis+4,re+4,re-diesis+4,mi+4,fa+4,fa-diesis+4,sol+4,sol-diesis+4,la+4,la-diesis+4,si+4,do+5,do-diesis+5,re+5,re-diesis+5,mi+5,fa+5,fa-diesis+5,sol+5\n"
			+ "cbx_note_id_italian_lc/cbx_halftone_id_diesis/cbx_halftone_id_diesis:\n"
			+ "do-----,do-diesis-----,re-----,re-diesis-----,mi-----,fa-----,fa-diesis-----,sol-----,sol-diesis-----,la-----,la-diesis-----,si-----,do----,do-diesis----,re----,re-diesis----,mi----,fa----,fa-diesis----,sol----,sol-diesis----,la----,la-diesis----,si----,do---,do-diesis---,re---,re-diesis---,mi---,fa---,fa-diesis---,sol---,sol-diesis---,la---,la-diesis---,si---,do--,do-diesis--,re--,re-diesis--,mi--,fa--,fa-diesis--,sol--,sol-diesis--,la--,la-diesis--,si--,do-,do-diesis-,re-,re-diesis-,mi-,fa-,fa-diesis-,sol-,sol-diesis-,la-,la-diesis-,si-,do,do-diesis,re,re-diesis,mi,fa,fa-diesis,sol,sol-diesis,la,la-diesis,si,do+,do-diesis+,re+,re-diesis+,mi+,fa+,fa-diesis+,sol+,sol-diesis+,la+,la-diesis+,si+,do++,do-diesis++,re++,re-diesis++,mi++,fa++,fa-diesis++,sol++,sol-diesis++,la++,la-diesis++,si++,do+++,do-diesis+++,re+++,re-diesis+++,mi+++,fa+++,fa-diesis+++,sol+++,sol-diesis+++,la+++,la-diesis+++,si+++,do++++,do-diesis++++,re++++,re-diesis++++,mi++++,fa++++,fa-diesis++++,sol++++,sol-diesis++++,la++++,la-diesis++++,si++++,do+++++,do-diesis+++++,re+++++,re-diesis+++++,mi+++++,fa+++++,fa-diesis+++++,sol+++++\n"
			+ "cbx_note_id_italian_lc/cbx_halftone_id_diesis/cbx_halftone_id_diesis:\n"
			+ "do-1,do-diesis-1,re-1,re-diesis-1,mi-1,fa-1,fa-diesis-1,sol-1,sol-diesis-1,la-1,la-diesis-1,si-1,do0,do-diesis0,re0,re-diesis0,mi0,fa0,fa-diesis0,sol0,sol-diesis0,la0,la-diesis0,si0,do1,do-diesis1,re1,re-diesis1,mi1,fa1,fa-diesis1,sol1,sol-diesis1,la1,la-diesis1,si1,do2,do-diesis2,re2,re-diesis2,mi2,fa2,fa-diesis2,sol2,sol-diesis2,la2,la-diesis2,si2,do3,do-diesis3,re3,re-diesis3,mi3,fa3,fa-diesis3,sol3,sol-diesis3,la3,la-diesis3,si3,do4,do-diesis4,re4,re-diesis4,mi4,fa4,fa-diesis4,sol4,sol-diesis4,la4,la-diesis4,si4,do5,do-diesis5,re5,re-diesis5,mi5,fa5,fa-diesis5,sol5,sol-diesis5,la5,la-diesis5,si5,do6,do-diesis6,re6,re-diesis6,mi6,fa6,fa-diesis6,sol6,sol-diesis6,la6,la-diesis6,si6,do7,do-diesis7,re7,re-diesis7,mi7,fa7,fa-diesis7,sol7,sol-diesis7,la7,la-diesis7,si7,do8,do-diesis8,re8,re-diesis8,mi8,fa8,fa-diesis8,sol8,sol-diesis8,la8,la-diesis8,si8,do9,do-diesis9,re9,re-diesis9,mi9,fa9,fa-diesis9,sol9\n"
			+ "cbx_note_id_italian_lc/cbx_halftone_id_diesis/cbx_halftone_id_diesis:\n"
			+ "Do''',Do-diesis''',Re''',Re-diesis''',Mi''',Fa''',Fa-diesis''',Sol''',Sol-diesis''',La''',La-diesis''',Si''',Do'',Do-diesis'',Re'',Re-diesis'',Mi'',Fa'',Fa-diesis'',Sol'',Sol-diesis'',La'',La-diesis'',Si'',Do',Do-diesis',Re',Re-diesis',Mi',Fa',Fa-diesis',Sol',Sol-diesis',La',La-diesis',Si',Do,Do-diesis,Re,Re-diesis,Mi,Fa,Fa-diesis,Sol,Sol-diesis,La,La-diesis,Si,do,do-diesis,re,re-diesis,mi,fa,fa-diesis,sol,sol-diesis,la,la-diesis,si,do',do-diesis',re',re-diesis',mi',fa',fa-diesis',sol',sol-diesis',la',la-diesis',si',do'',do-diesis'',re'',re-diesis'',mi'',fa'',fa-diesis'',sol'',sol-diesis'',la'',la-diesis'',si'',do''',do-diesis''',re''',re-diesis''',mi''',fa''',fa-diesis''',sol''',sol-diesis''',la''',la-diesis''',si''',do'''',do-diesis'''',re'''',re-diesis'''',mi'''',fa'''',fa-diesis'''',sol'''',sol-diesis'''',la'''',la-diesis'''',si'''',do''''',do-diesis''''',re''''',re-diesis''''',mi''''',fa''''',fa-diesis''''',sol''''',sol-diesis''''',la''''',la-diesis''''',si''''',do'''''',do-diesis'''''',re'''''',re-diesis'''''',mi'''''',fa'''''',fa-diesis'''''',sol''''''\n"
			+ "cbx_note_id_italian_lc/cbx_halftone_id_bemolle/cbx_halftone_id_bemolle:\n"
			+ "do-5,re-bemolle-5,re-5,mi-bemolle-5,mi-5,fa-5,sol-bemolle-5,sol-5,la-bemolle-5,la-5,si-bemolle-5,si-5,do-4,re-bemolle-4,re-4,mi-bemolle-4,mi-4,fa-4,sol-bemolle-4,sol-4,la-bemolle-4,la-4,si-bemolle-4,si-4,do-3,re-bemolle-3,re-3,mi-bemolle-3,mi-3,fa-3,sol-bemolle-3,sol-3,la-bemolle-3,la-3,si-bemolle-3,si-3,do-2,re-bemolle-2,re-2,mi-bemolle-2,mi-2,fa-2,sol-bemolle-2,sol-2,la-bemolle-2,la-2,si-bemolle-2,si-2,do-,re-bemolle-,re-,mi-bemolle-,mi-,fa-,sol-bemolle-,sol-,la-bemolle-,la-,si-bemolle-,si-,do,re-bemolle,re,mi-bemolle,mi,fa,sol-bemolle,sol,la-bemolle,la,si-bemolle,si,do+,re-bemolle+,re+,mi-bemolle+,mi+,fa+,sol-bemolle+,sol+,la-bemolle+,la+,si-bemolle+,si+,do+2,re-bemolle+2,re+2,mi-bemolle+2,mi+2,fa+2,sol-bemolle+2,sol+2,la-bemolle+2,la+2,si-bemolle+2,si+2,do+3,re-bemolle+3,re+3,mi-bemolle+3,mi+3,fa+3,sol-bemolle+3,sol+3,la-bemolle+3,la+3,si-bemolle+3,si+3,do+4,re-bemolle+4,re+4,mi-bemolle+4,mi+4,fa+4,sol-bemolle+4,sol+4,la-bemolle+4,la+4,si-bemolle+4,si+4,do+5,re-bemolle+5,re+5,mi-bemolle+5,mi+5,fa+5,sol-bemolle+5,sol+5\n"
			+ "cbx_note_id_italian_lc/cbx_halftone_id_bemolle/cbx_halftone_id_bemolle:\n"
			+ "do-----,re-bemolle-----,re-----,mi-bemolle-----,mi-----,fa-----,sol-bemolle-----,sol-----,la-bemolle-----,la-----,si-bemolle-----,si-----,do----,re-bemolle----,re----,mi-bemolle----,mi----,fa----,sol-bemolle----,sol----,la-bemolle----,la----,si-bemolle----,si----,do---,re-bemolle---,re---,mi-bemolle---,mi---,fa---,sol-bemolle---,sol---,la-bemolle---,la---,si-bemolle---,si---,do--,re-bemolle--,re--,mi-bemolle--,mi--,fa--,sol-bemolle--,sol--,la-bemolle--,la--,si-bemolle--,si--,do-,re-bemolle-,re-,mi-bemolle-,mi-,fa-,sol-bemolle-,sol-,la-bemolle-,la-,si-bemolle-,si-,do,re-bemolle,re,mi-bemolle,mi,fa,sol-bemolle,sol,la-bemolle,la,si-bemolle,si,do+,re-bemolle+,re+,mi-bemolle+,mi+,fa+,sol-bemolle+,sol+,la-bemolle+,la+,si-bemolle+,si+,do++,re-bemolle++,re++,mi-bemolle++,mi++,fa++,sol-bemolle++,sol++,la-bemolle++,la++,si-bemolle++,si++,do+++,re-bemolle+++,re+++,mi-bemolle+++,mi+++,fa+++,sol-bemolle+++,sol+++,la-bemolle+++,la+++,si-bemolle+++,si+++,do++++,re-bemolle++++,re++++,mi-bemolle++++,mi++++,fa++++,sol-bemolle++++,sol++++,la-bemolle++++,la++++,si-bemolle++++,si++++,do+++++,re-bemolle+++++,re+++++,mi-bemolle+++++,mi+++++,fa+++++,sol-bemolle+++++,sol+++++\n"
			+ "cbx_note_id_italian_lc/cbx_halftone_id_bemolle/cbx_halftone_id_bemolle:\n"
			+ "do-1,re-bemolle-1,re-1,mi-bemolle-1,mi-1,fa-1,sol-bemolle-1,sol-1,la-bemolle-1,la-1,si-bemolle-1,si-1,do0,re-bemolle0,re0,mi-bemolle0,mi0,fa0,sol-bemolle0,sol0,la-bemolle0,la0,si-bemolle0,si0,do1,re-bemolle1,re1,mi-bemolle1,mi1,fa1,sol-bemolle1,sol1,la-bemolle1,la1,si-bemolle1,si1,do2,re-bemolle2,re2,mi-bemolle2,mi2,fa2,sol-bemolle2,sol2,la-bemolle2,la2,si-bemolle2,si2,do3,re-bemolle3,re3,mi-bemolle3,mi3,fa3,sol-bemolle3,sol3,la-bemolle3,la3,si-bemolle3,si3,do4,re-bemolle4,re4,mi-bemolle4,mi4,fa4,sol-bemolle4,sol4,la-bemolle4,la4,si-bemolle4,si4,do5,re-bemolle5,re5,mi-bemolle5,mi5,fa5,sol-bemolle5,sol5,la-bemolle5,la5,si-bemolle5,si5,do6,re-bemolle6,re6,mi-bemolle6,mi6,fa6,sol-bemolle6,sol6,la-bemolle6,la6,si-bemolle6,si6,do7,re-bemolle7,re7,mi-bemolle7,mi7,fa7,sol-bemolle7,sol7,la-bemolle7,la7,si-bemolle7,si7,do8,re-bemolle8,re8,mi-bemolle8,mi8,fa8,sol-bemolle8,sol8,la-bemolle8,la8,si-bemolle8,si8,do9,re-bemolle9,re9,mi-bemolle9,mi9,fa9,sol-bemolle9,sol9\n"
			+ "cbx_note_id_italian_lc/cbx_halftone_id_bemolle/cbx_halftone_id_bemolle:\n"
			+ "Do''',Re-bemolle''',Re''',Mi-bemolle''',Mi''',Fa''',Sol-bemolle''',Sol''',La-bemolle''',La''',Si-bemolle''',Si''',Do'',Re-bemolle'',Re'',Mi-bemolle'',Mi'',Fa'',Sol-bemolle'',Sol'',La-bemolle'',La'',Si-bemolle'',Si'',Do',Re-bemolle',Re',Mi-bemolle',Mi',Fa',Sol-bemolle',Sol',La-bemolle',La',Si-bemolle',Si',Do,Re-bemolle,Re,Mi-bemolle,Mi,Fa,Sol-bemolle,Sol,La-bemolle,La,Si-bemolle,Si,do,re-bemolle,re,mi-bemolle,mi,fa,sol-bemolle,sol,la-bemolle,la,si-bemolle,si,do',re-bemolle',re',mi-bemolle',mi',fa',sol-bemolle',sol',la-bemolle',la',si-bemolle',si',do'',re-bemolle'',re'',mi-bemolle'',mi'',fa'',sol-bemolle'',sol'',la-bemolle'',la'',si-bemolle'',si'',do''',re-bemolle''',re''',mi-bemolle''',mi''',fa''',sol-bemolle''',sol''',la-bemolle''',la''',si-bemolle''',si''',do'''',re-bemolle'''',re'''',mi-bemolle'''',mi'''',fa'''',sol-bemolle'''',sol'''',la-bemolle'''',la'''',si-bemolle'''',si'''',do''''',re-bemolle''''',re''''',mi-bemolle''''',mi''''',fa''''',sol-bemolle''''',sol''''',la-bemolle''''',la''''',si-bemolle''''',si''''',do'''''',re-bemolle'''''',re'''''',mi-bemolle'''''',mi'''''',fa'''''',sol-bemolle'''''',sol''''''\n"
			+ "cbx_note_id_italian_lc/cbx_halftone_id_cis/cbx_halftone_id_cis:\n"
			+ "do-5,dois-5,re-5,reis-5,mi-5,fa-5,fais-5,sol-5,solis-5,la-5,lais-5,si-5,do-4,dois-4,re-4,reis-4,mi-4,fa-4,fais-4,sol-4,solis-4,la-4,lais-4,si-4,do-3,dois-3,re-3,reis-3,mi-3,fa-3,fais-3,sol-3,solis-3,la-3,lais-3,si-3,do-2,dois-2,re-2,reis-2,mi-2,fa-2,fais-2,sol-2,solis-2,la-2,lais-2,si-2,do-,dois-,re-,reis-,mi-,fa-,fais-,sol-,solis-,la-,lais-,si-,do,dois,re,reis,mi,fa,fais,sol,solis,la,lais,si,do+,dois+,re+,reis+,mi+,fa+,fais+,sol+,solis+,la+,lais+,si+,do+2,dois+2,re+2,reis+2,mi+2,fa+2,fais+2,sol+2,solis+2,la+2,lais+2,si+2,do+3,dois+3,re+3,reis+3,mi+3,fa+3,fais+3,sol+3,solis+3,la+3,lais+3,si+3,do+4,dois+4,re+4,reis+4,mi+4,fa+4,fais+4,sol+4,solis+4,la+4,lais+4,si+4,do+5,dois+5,re+5,reis+5,mi+5,fa+5,fais+5,sol+5\n"
			+ "cbx_note_id_italian_lc/cbx_halftone_id_cis/cbx_halftone_id_cis:\n"
			+ "do-----,dois-----,re-----,reis-----,mi-----,fa-----,fais-----,sol-----,solis-----,la-----,lais-----,si-----,do----,dois----,re----,reis----,mi----,fa----,fais----,sol----,solis----,la----,lais----,si----,do---,dois---,re---,reis---,mi---,fa---,fais---,sol---,solis---,la---,lais---,si---,do--,dois--,re--,reis--,mi--,fa--,fais--,sol--,solis--,la--,lais--,si--,do-,dois-,re-,reis-,mi-,fa-,fais-,sol-,solis-,la-,lais-,si-,do,dois,re,reis,mi,fa,fais,sol,solis,la,lais,si,do+,dois+,re+,reis+,mi+,fa+,fais+,sol+,solis+,la+,lais+,si+,do++,dois++,re++,reis++,mi++,fa++,fais++,sol++,solis++,la++,lais++,si++,do+++,dois+++,re+++,reis+++,mi+++,fa+++,fais+++,sol+++,solis+++,la+++,lais+++,si+++,do++++,dois++++,re++++,reis++++,mi++++,fa++++,fais++++,sol++++,solis++++,la++++,lais++++,si++++,do+++++,dois+++++,re+++++,reis+++++,mi+++++,fa+++++,fais+++++,sol+++++\n"
			+ "cbx_note_id_italian_lc/cbx_halftone_id_cis/cbx_halftone_id_cis:\n"
			+ "do-1,dois-1,re-1,reis-1,mi-1,fa-1,fais-1,sol-1,solis-1,la-1,lais-1,si-1,do0,dois0,re0,reis0,mi0,fa0,fais0,sol0,solis0,la0,lais0,si0,do1,dois1,re1,reis1,mi1,fa1,fais1,sol1,solis1,la1,lais1,si1,do2,dois2,re2,reis2,mi2,fa2,fais2,sol2,solis2,la2,lais2,si2,do3,dois3,re3,reis3,mi3,fa3,fais3,sol3,solis3,la3,lais3,si3,do4,dois4,re4,reis4,mi4,fa4,fais4,sol4,solis4,la4,lais4,si4,do5,dois5,re5,reis5,mi5,fa5,fais5,sol5,solis5,la5,lais5,si5,do6,dois6,re6,reis6,mi6,fa6,fais6,sol6,solis6,la6,lais6,si6,do7,dois7,re7,reis7,mi7,fa7,fais7,sol7,solis7,la7,lais7,si7,do8,dois8,re8,reis8,mi8,fa8,fais8,sol8,solis8,la8,lais8,si8,do9,dois9,re9,reis9,mi9,fa9,fais9,sol9\n"
			+ "cbx_note_id_italian_lc/cbx_halftone_id_cis/cbx_halftone_id_cis:\n"
			+ "Do''',Dois''',Re''',Reis''',Mi''',Fa''',Fais''',Sol''',Solis''',La''',Lais''',Si''',Do'',Dois'',Re'',Reis'',Mi'',Fa'',Fais'',Sol'',Solis'',La'',Lais'',Si'',Do',Dois',Re',Reis',Mi',Fa',Fais',Sol',Solis',La',Lais',Si',Do,Dois,Re,Reis,Mi,Fa,Fais,Sol,Solis,La,Lais,Si,do,dois,re,reis,mi,fa,fais,sol,solis,la,lais,si,do',dois',re',reis',mi',fa',fais',sol',solis',la',lais',si',do'',dois'',re'',reis'',mi'',fa'',fais'',sol'',solis'',la'',lais'',si'',do''',dois''',re''',reis''',mi''',fa''',fais''',sol''',solis''',la''',lais''',si''',do'''',dois'''',re'''',reis'''',mi'''',fa'''',fais'''',sol'''',solis'''',la'''',lais'''',si'''',do''''',dois''''',re''''',reis''''',mi''''',fa''''',fais''''',sol''''',solis''''',la''''',lais''''',si''''',do'''''',dois'''''',re'''''',reis'''''',mi'''''',fa'''''',fais'''''',sol''''''\n"
			+ "cbx_note_id_italian_lc/cbx_halftone_id_des/cbx_halftone_id_des:\n"
			+ "do-5,rees-5,re-5,mies-5,mi-5,fa-5,soles-5,sol-5,laes-5,la-5,sies-5,si-5,do-4,rees-4,re-4,mies-4,mi-4,fa-4,soles-4,sol-4,laes-4,la-4,sies-4,si-4,do-3,rees-3,re-3,mies-3,mi-3,fa-3,soles-3,sol-3,laes-3,la-3,sies-3,si-3,do-2,rees-2,re-2,mies-2,mi-2,fa-2,soles-2,sol-2,laes-2,la-2,sies-2,si-2,do-,rees-,re-,mies-,mi-,fa-,soles-,sol-,laes-,la-,sies-,si-,do,rees,re,mies,mi,fa,soles,sol,laes,la,sies,si,do+,rees+,re+,mies+,mi+,fa+,soles+,sol+,laes+,la+,sies+,si+,do+2,rees+2,re+2,mies+2,mi+2,fa+2,soles+2,sol+2,laes+2,la+2,sies+2,si+2,do+3,rees+3,re+3,mies+3,mi+3,fa+3,soles+3,sol+3,laes+3,la+3,sies+3,si+3,do+4,rees+4,re+4,mies+4,mi+4,fa+4,soles+4,sol+4,laes+4,la+4,sies+4,si+4,do+5,rees+5,re+5,mies+5,mi+5,fa+5,soles+5,sol+5\n"
			+ "cbx_note_id_italian_lc/cbx_halftone_id_des/cbx_halftone_id_des:\n"
			+ "do-----,rees-----,re-----,mies-----,mi-----,fa-----,soles-----,sol-----,laes-----,la-----,sies-----,si-----,do----,rees----,re----,mies----,mi----,fa----,soles----,sol----,laes----,la----,sies----,si----,do---,rees---,re---,mies---,mi---,fa---,soles---,sol---,laes---,la---,sies---,si---,do--,rees--,re--,mies--,mi--,fa--,soles--,sol--,laes--,la--,sies--,si--,do-,rees-,re-,mies-,mi-,fa-,soles-,sol-,laes-,la-,sies-,si-,do,rees,re,mies,mi,fa,soles,sol,laes,la,sies,si,do+,rees+,re+,mies+,mi+,fa+,soles+,sol+,laes+,la+,sies+,si+,do++,rees++,re++,mies++,mi++,fa++,soles++,sol++,laes++,la++,sies++,si++,do+++,rees+++,re+++,mies+++,mi+++,fa+++,soles+++,sol+++,laes+++,la+++,sies+++,si+++,do++++,rees++++,re++++,mies++++,mi++++,fa++++,soles++++,sol++++,laes++++,la++++,sies++++,si++++,do+++++,rees+++++,re+++++,mies+++++,mi+++++,fa+++++,soles+++++,sol+++++\n"
			+ "cbx_note_id_italian_lc/cbx_halftone_id_des/cbx_halftone_id_des:\n"
			+ "do-1,rees-1,re-1,mies-1,mi-1,fa-1,soles-1,sol-1,laes-1,la-1,sies-1,si-1,do0,rees0,re0,mies0,mi0,fa0,soles0,sol0,laes0,la0,sies0,si0,do1,rees1,re1,mies1,mi1,fa1,soles1,sol1,laes1,la1,sies1,si1,do2,rees2,re2,mies2,mi2,fa2,soles2,sol2,laes2,la2,sies2,si2,do3,rees3,re3,mies3,mi3,fa3,soles3,sol3,laes3,la3,sies3,si3,do4,rees4,re4,mies4,mi4,fa4,soles4,sol4,laes4,la4,sies4,si4,do5,rees5,re5,mies5,mi5,fa5,soles5,sol5,laes5,la5,sies5,si5,do6,rees6,re6,mies6,mi6,fa6,soles6,sol6,laes6,la6,sies6,si6,do7,rees7,re7,mies7,mi7,fa7,soles7,sol7,laes7,la7,sies7,si7,do8,rees8,re8,mies8,mi8,fa8,soles8,sol8,laes8,la8,sies8,si8,do9,rees9,re9,mies9,mi9,fa9,soles9,sol9\n"
			+ "cbx_note_id_italian_lc/cbx_halftone_id_des/cbx_halftone_id_des:\n"
			+ "Do''',Rees''',Re''',Mies''',Mi''',Fa''',Soles''',Sol''',Laes''',La''',Sies''',Si''',Do'',Rees'',Re'',Mies'',Mi'',Fa'',Soles'',Sol'',Laes'',La'',Sies'',Si'',Do',Rees',Re',Mies',Mi',Fa',Soles',Sol',Laes',La',Sies',Si',Do,Rees,Re,Mies,Mi,Fa,Soles,Sol,Laes,La,Sies,Si,do,rees,re,mies,mi,fa,soles,sol,laes,la,sies,si,do',rees',re',mies',mi',fa',soles',sol',laes',la',sies',si',do'',rees'',re'',mies'',mi'',fa'',soles'',sol'',laes'',la'',sies'',si'',do''',rees''',re''',mies''',mi''',fa''',soles''',sol''',laes''',la''',sies''',si''',do'''',rees'''',re'''',mies'''',mi'''',fa'''',soles'''',sol'''',laes'''',la'''',sies'''',si'''',do''''',rees''''',re''''',mies''''',mi''''',fa''''',soles''''',sol''''',laes''''',la''''',sies''''',si''''',do'''''',rees'''''',re'''''',mies'''''',mi'''''',fa'''''',soles'''''',sol''''''\n"
			+ "cbx_note_id_italian_uc/cbx_halftone_id_sharp/cbx_halftone_id_sharp:\n"
			+ "Do-5,Do#-5,Re-5,Re#-5,Mi-5,Fa-5,Fa#-5,Sol-5,Sol#-5,La-5,La#-5,Si-5,Do-4,Do#-4,Re-4,Re#-4,Mi-4,Fa-4,Fa#-4,Sol-4,Sol#-4,La-4,La#-4,Si-4,Do-3,Do#-3,Re-3,Re#-3,Mi-3,Fa-3,Fa#-3,Sol-3,Sol#-3,La-3,La#-3,Si-3,Do-2,Do#-2,Re-2,Re#-2,Mi-2,Fa-2,Fa#-2,Sol-2,Sol#-2,La-2,La#-2,Si-2,Do-,Do#-,Re-,Re#-,Mi-,Fa-,Fa#-,Sol-,Sol#-,La-,La#-,Si-,Do,Do#,Re,Re#,Mi,Fa,Fa#,Sol,Sol#,La,La#,Si,Do+,Do#+,Re+,Re#+,Mi+,Fa+,Fa#+,Sol+,Sol#+,La+,La#+,Si+,Do+2,Do#+2,Re+2,Re#+2,Mi+2,Fa+2,Fa#+2,Sol+2,Sol#+2,La+2,La#+2,Si+2,Do+3,Do#+3,Re+3,Re#+3,Mi+3,Fa+3,Fa#+3,Sol+3,Sol#+3,La+3,La#+3,Si+3,Do+4,Do#+4,Re+4,Re#+4,Mi+4,Fa+4,Fa#+4,Sol+4,Sol#+4,La+4,La#+4,Si+4,Do+5,Do#+5,Re+5,Re#+5,Mi+5,Fa+5,Fa#+5,Sol+5\n"
			+ "cbx_note_id_italian_uc/cbx_halftone_id_sharp/cbx_halftone_id_sharp:\n"
			+ "Do-----,Do#-----,Re-----,Re#-----,Mi-----,Fa-----,Fa#-----,Sol-----,Sol#-----,La-----,La#-----,Si-----,Do----,Do#----,Re----,Re#----,Mi----,Fa----,Fa#----,Sol----,Sol#----,La----,La#----,Si----,Do---,Do#---,Re---,Re#---,Mi---,Fa---,Fa#---,Sol---,Sol#---,La---,La#---,Si---,Do--,Do#--,Re--,Re#--,Mi--,Fa--,Fa#--,Sol--,Sol#--,La--,La#--,Si--,Do-,Do#-,Re-,Re#-,Mi-,Fa-,Fa#-,Sol-,Sol#-,La-,La#-,Si-,Do,Do#,Re,Re#,Mi,Fa,Fa#,Sol,Sol#,La,La#,Si,Do+,Do#+,Re+,Re#+,Mi+,Fa+,Fa#+,Sol+,Sol#+,La+,La#+,Si+,Do++,Do#++,Re++,Re#++,Mi++,Fa++,Fa#++,Sol++,Sol#++,La++,La#++,Si++,Do+++,Do#+++,Re+++,Re#+++,Mi+++,Fa+++,Fa#+++,Sol+++,Sol#+++,La+++,La#+++,Si+++,Do++++,Do#++++,Re++++,Re#++++,Mi++++,Fa++++,Fa#++++,Sol++++,Sol#++++,La++++,La#++++,Si++++,Do+++++,Do#+++++,Re+++++,Re#+++++,Mi+++++,Fa+++++,Fa#+++++,Sol+++++\n"
			+ "cbx_note_id_italian_uc/cbx_halftone_id_sharp/cbx_halftone_id_sharp:\n"
			+ "Do-1,Do#-1,Re-1,Re#-1,Mi-1,Fa-1,Fa#-1,Sol-1,Sol#-1,La-1,La#-1,Si-1,Do0,Do#0,Re0,Re#0,Mi0,Fa0,Fa#0,Sol0,Sol#0,La0,La#0,Si0,Do1,Do#1,Re1,Re#1,Mi1,Fa1,Fa#1,Sol1,Sol#1,La1,La#1,Si1,Do2,Do#2,Re2,Re#2,Mi2,Fa2,Fa#2,Sol2,Sol#2,La2,La#2,Si2,Do3,Do#3,Re3,Re#3,Mi3,Fa3,Fa#3,Sol3,Sol#3,La3,La#3,Si3,Do4,Do#4,Re4,Re#4,Mi4,Fa4,Fa#4,Sol4,Sol#4,La4,La#4,Si4,Do5,Do#5,Re5,Re#5,Mi5,Fa5,Fa#5,Sol5,Sol#5,La5,La#5,Si5,Do6,Do#6,Re6,Re#6,Mi6,Fa6,Fa#6,Sol6,Sol#6,La6,La#6,Si6,Do7,Do#7,Re7,Re#7,Mi7,Fa7,Fa#7,Sol7,Sol#7,La7,La#7,Si7,Do8,Do#8,Re8,Re#8,Mi8,Fa8,Fa#8,Sol8,Sol#8,La8,La#8,Si8,Do9,Do#9,Re9,Re#9,Mi9,Fa9,Fa#9,Sol9\n"
			+ "cbx_note_id_italian_uc/cbx_halftone_id_sharp/cbx_halftone_id_sharp:\n"
			+ "Do''',Do#''',Re''',Re#''',Mi''',Fa''',Fa#''',Sol''',Sol#''',La''',La#''',Si''',Do'',Do#'',Re'',Re#'',Mi'',Fa'',Fa#'',Sol'',Sol#'',La'',La#'',Si'',Do',Do#',Re',Re#',Mi',Fa',Fa#',Sol',Sol#',La',La#',Si',Do,Do#,Re,Re#,Mi,Fa,Fa#,Sol,Sol#,La,La#,Si,do,do#,re,re#,mi,fa,fa#,sol,sol#,la,la#,si,do',do#',re',re#',mi',fa',fa#',sol',sol#',la',la#',si',do'',do#'',re'',re#'',mi'',fa'',fa#'',sol'',sol#'',la'',la#'',si'',do''',do#''',re''',re#''',mi''',fa''',fa#''',sol''',sol#''',la''',la#''',si''',do'''',do#'''',re'''',re#'''',mi'''',fa'''',fa#'''',sol'''',sol#'''',la'''',la#'''',si'''',do''''',do#''''',re''''',re#''''',mi''''',fa''''',fa#''''',sol''''',sol#''''',la''''',la#''''',si''''',do'''''',do#'''''',re'''''',re#'''''',mi'''''',fa'''''',fa#'''''',sol''''''\n"
			+ "cbx_note_id_italian_uc/cbx_halftone_id_flat/cbx_halftone_id_flat:\n"
			+ "Do-5,Reb-5,Re-5,Mib-5,Mi-5,Fa-5,Solb-5,Sol-5,Lab-5,La-5,Sib-5,Si-5,Do-4,Reb-4,Re-4,Mib-4,Mi-4,Fa-4,Solb-4,Sol-4,Lab-4,La-4,Sib-4,Si-4,Do-3,Reb-3,Re-3,Mib-3,Mi-3,Fa-3,Solb-3,Sol-3,Lab-3,La-3,Sib-3,Si-3,Do-2,Reb-2,Re-2,Mib-2,Mi-2,Fa-2,Solb-2,Sol-2,Lab-2,La-2,Sib-2,Si-2,Do-,Reb-,Re-,Mib-,Mi-,Fa-,Solb-,Sol-,Lab-,La-,Sib-,Si-,Do,Reb,Re,Mib,Mi,Fa,Solb,Sol,Lab,La,Sib,Si,Do+,Reb+,Re+,Mib+,Mi+,Fa+,Solb+,Sol+,Lab+,La+,Sib+,Si+,Do+2,Reb+2,Re+2,Mib+2,Mi+2,Fa+2,Solb+2,Sol+2,Lab+2,La+2,Sib+2,Si+2,Do+3,Reb+3,Re+3,Mib+3,Mi+3,Fa+3,Solb+3,Sol+3,Lab+3,La+3,Sib+3,Si+3,Do+4,Reb+4,Re+4,Mib+4,Mi+4,Fa+4,Solb+4,Sol+4,Lab+4,La+4,Sib+4,Si+4,Do+5,Reb+5,Re+5,Mib+5,Mi+5,Fa+5,Solb+5,Sol+5\n"
			+ "cbx_note_id_italian_uc/cbx_halftone_id_flat/cbx_halftone_id_flat:\n"
			+ "Do-----,Reb-----,Re-----,Mib-----,Mi-----,Fa-----,Solb-----,Sol-----,Lab-----,La-----,Sib-----,Si-----,Do----,Reb----,Re----,Mib----,Mi----,Fa----,Solb----,Sol----,Lab----,La----,Sib----,Si----,Do---,Reb---,Re---,Mib---,Mi---,Fa---,Solb---,Sol---,Lab---,La---,Sib---,Si---,Do--,Reb--,Re--,Mib--,Mi--,Fa--,Solb--,Sol--,Lab--,La--,Sib--,Si--,Do-,Reb-,Re-,Mib-,Mi-,Fa-,Solb-,Sol-,Lab-,La-,Sib-,Si-,Do,Reb,Re,Mib,Mi,Fa,Solb,Sol,Lab,La,Sib,Si,Do+,Reb+,Re+,Mib+,Mi+,Fa+,Solb+,Sol+,Lab+,La+,Sib+,Si+,Do++,Reb++,Re++,Mib++,Mi++,Fa++,Solb++,Sol++,Lab++,La++,Sib++,Si++,Do+++,Reb+++,Re+++,Mib+++,Mi+++,Fa+++,Solb+++,Sol+++,Lab+++,La+++,Sib+++,Si+++,Do++++,Reb++++,Re++++,Mib++++,Mi++++,Fa++++,Solb++++,Sol++++,Lab++++,La++++,Sib++++,Si++++,Do+++++,Reb+++++,Re+++++,Mib+++++,Mi+++++,Fa+++++,Solb+++++,Sol+++++\n"
			+ "cbx_note_id_italian_uc/cbx_halftone_id_flat/cbx_halftone_id_flat:\n"
			+ "Do-1,Reb-1,Re-1,Mib-1,Mi-1,Fa-1,Solb-1,Sol-1,Lab-1,La-1,Sib-1,Si-1,Do0,Reb0,Re0,Mib0,Mi0,Fa0,Solb0,Sol0,Lab0,La0,Sib0,Si0,Do1,Reb1,Re1,Mib1,Mi1,Fa1,Solb1,Sol1,Lab1,La1,Sib1,Si1,Do2,Reb2,Re2,Mib2,Mi2,Fa2,Solb2,Sol2,Lab2,La2,Sib2,Si2,Do3,Reb3,Re3,Mib3,Mi3,Fa3,Solb3,Sol3,Lab3,La3,Sib3,Si3,Do4,Reb4,Re4,Mib4,Mi4,Fa4,Solb4,Sol4,Lab4,La4,Sib4,Si4,Do5,Reb5,Re5,Mib5,Mi5,Fa5,Solb5,Sol5,Lab5,La5,Sib5,Si5,Do6,Reb6,Re6,Mib6,Mi6,Fa6,Solb6,Sol6,Lab6,La6,Sib6,Si6,Do7,Reb7,Re7,Mib7,Mi7,Fa7,Solb7,Sol7,Lab7,La7,Sib7,Si7,Do8,Reb8,Re8,Mib8,Mi8,Fa8,Solb8,Sol8,Lab8,La8,Sib8,Si8,Do9,Reb9,Re9,Mib9,Mi9,Fa9,Solb9,Sol9\n"
			+ "cbx_note_id_italian_uc/cbx_halftone_id_flat/cbx_halftone_id_flat:\n"
			+ "Do''',Reb''',Re''',Mib''',Mi''',Fa''',Solb''',Sol''',Lab''',La''',Sib''',Si''',Do'',Reb'',Re'',Mib'',Mi'',Fa'',Solb'',Sol'',Lab'',La'',Sib'',Si'',Do',Reb',Re',Mib',Mi',Fa',Solb',Sol',Lab',La',Sib',Si',Do,Reb,Re,Mib,Mi,Fa,Solb,Sol,Lab,La,Sib,Si,do,reb,re,mib,mi,fa,solb,sol,lab,la,sib,si,do',reb',re',mib',mi',fa',solb',sol',lab',la',sib',si',do'',reb'',re'',mib'',mi'',fa'',solb'',sol'',lab'',la'',sib'',si'',do''',reb''',re''',mib''',mi''',fa''',solb''',sol''',lab''',la''',sib''',si''',do'''',reb'''',re'''',mib'''',mi'''',fa'''',solb'''',sol'''',lab'''',la'''',sib'''',si'''',do''''',reb''''',re''''',mib''''',mi''''',fa''''',solb''''',sol''''',lab''''',la''''',sib''''',si''''',do'''''',reb'''''',re'''''',mib'''''',mi'''''',fa'''''',solb'''''',sol''''''\n"
			+ "cbx_note_id_italian_uc/cbx_halftone_id_diesis/cbx_halftone_id_diesis:\n"
			+ "Do-5,Do-diesis-5,Re-5,Re-diesis-5,Mi-5,Fa-5,Fa-diesis-5,Sol-5,Sol-diesis-5,La-5,La-diesis-5,Si-5,Do-4,Do-diesis-4,Re-4,Re-diesis-4,Mi-4,Fa-4,Fa-diesis-4,Sol-4,Sol-diesis-4,La-4,La-diesis-4,Si-4,Do-3,Do-diesis-3,Re-3,Re-diesis-3,Mi-3,Fa-3,Fa-diesis-3,Sol-3,Sol-diesis-3,La-3,La-diesis-3,Si-3,Do-2,Do-diesis-2,Re-2,Re-diesis-2,Mi-2,Fa-2,Fa-diesis-2,Sol-2,Sol-diesis-2,La-2,La-diesis-2,Si-2,Do-,Do-diesis-,Re-,Re-diesis-,Mi-,Fa-,Fa-diesis-,Sol-,Sol-diesis-,La-,La-diesis-,Si-,Do,Do-diesis,Re,Re-diesis,Mi,Fa,Fa-diesis,Sol,Sol-diesis,La,La-diesis,Si,Do+,Do-diesis+,Re+,Re-diesis+,Mi+,Fa+,Fa-diesis+,Sol+,Sol-diesis+,La+,La-diesis+,Si+,Do+2,Do-diesis+2,Re+2,Re-diesis+2,Mi+2,Fa+2,Fa-diesis+2,Sol+2,Sol-diesis+2,La+2,La-diesis+2,Si+2,Do+3,Do-diesis+3,Re+3,Re-diesis+3,Mi+3,Fa+3,Fa-diesis+3,Sol+3,Sol-diesis+3,La+3,La-diesis+3,Si+3,Do+4,Do-diesis+4,Re+4,Re-diesis+4,Mi+4,Fa+4,Fa-diesis+4,Sol+4,Sol-diesis+4,La+4,La-diesis+4,Si+4,Do+5,Do-diesis+5,Re+5,Re-diesis+5,Mi+5,Fa+5,Fa-diesis+5,Sol+5\n"
			+ "cbx_note_id_italian_uc/cbx_halftone_id_diesis/cbx_halftone_id_diesis:\n"
			+ "Do-----,Do-diesis-----,Re-----,Re-diesis-----,Mi-----,Fa-----,Fa-diesis-----,Sol-----,Sol-diesis-----,La-----,La-diesis-----,Si-----,Do----,Do-diesis----,Re----,Re-diesis----,Mi----,Fa----,Fa-diesis----,Sol----,Sol-diesis----,La----,La-diesis----,Si----,Do---,Do-diesis---,Re---,Re-diesis---,Mi---,Fa---,Fa-diesis---,Sol---,Sol-diesis---,La---,La-diesis---,Si---,Do--,Do-diesis--,Re--,Re-diesis--,Mi--,Fa--,Fa-diesis--,Sol--,Sol-diesis--,La--,La-diesis--,Si--,Do-,Do-diesis-,Re-,Re-diesis-,Mi-,Fa-,Fa-diesis-,Sol-,Sol-diesis-,La-,La-diesis-,Si-,Do,Do-diesis,Re,Re-diesis,Mi,Fa,Fa-diesis,Sol,Sol-diesis,La,La-diesis,Si,Do+,Do-diesis+,Re+,Re-diesis+,Mi+,Fa+,Fa-diesis+,Sol+,Sol-diesis+,La+,La-diesis+,Si+,Do++,Do-diesis++,Re++,Re-diesis++,Mi++,Fa++,Fa-diesis++,Sol++,Sol-diesis++,La++,La-diesis++,Si++,Do+++,Do-diesis+++,Re+++,Re-diesis+++,Mi+++,Fa+++,Fa-diesis+++,Sol+++,Sol-diesis+++,La+++,La-diesis+++,Si+++,Do++++,Do-diesis++++,Re++++,Re-diesis++++,Mi++++,Fa++++,Fa-diesis++++,Sol++++,Sol-diesis++++,La++++,La-diesis++++,Si++++,Do+++++,Do-diesis+++++,Re+++++,Re-diesis+++++,Mi+++++,Fa+++++,Fa-diesis+++++,Sol+++++\n"
			+ "cbx_note_id_italian_uc/cbx_halftone_id_diesis/cbx_halftone_id_diesis:\n"
			+ "Do-1,Do-diesis-1,Re-1,Re-diesis-1,Mi-1,Fa-1,Fa-diesis-1,Sol-1,Sol-diesis-1,La-1,La-diesis-1,Si-1,Do0,Do-diesis0,Re0,Re-diesis0,Mi0,Fa0,Fa-diesis0,Sol0,Sol-diesis0,La0,La-diesis0,Si0,Do1,Do-diesis1,Re1,Re-diesis1,Mi1,Fa1,Fa-diesis1,Sol1,Sol-diesis1,La1,La-diesis1,Si1,Do2,Do-diesis2,Re2,Re-diesis2,Mi2,Fa2,Fa-diesis2,Sol2,Sol-diesis2,La2,La-diesis2,Si2,Do3,Do-diesis3,Re3,Re-diesis3,Mi3,Fa3,Fa-diesis3,Sol3,Sol-diesis3,La3,La-diesis3,Si3,Do4,Do-diesis4,Re4,Re-diesis4,Mi4,Fa4,Fa-diesis4,Sol4,Sol-diesis4,La4,La-diesis4,Si4,Do5,Do-diesis5,Re5,Re-diesis5,Mi5,Fa5,Fa-diesis5,Sol5,Sol-diesis5,La5,La-diesis5,Si5,Do6,Do-diesis6,Re6,Re-diesis6,Mi6,Fa6,Fa-diesis6,Sol6,Sol-diesis6,La6,La-diesis6,Si6,Do7,Do-diesis7,Re7,Re-diesis7,Mi7,Fa7,Fa-diesis7,Sol7,Sol-diesis7,La7,La-diesis7,Si7,Do8,Do-diesis8,Re8,Re-diesis8,Mi8,Fa8,Fa-diesis8,Sol8,Sol-diesis8,La8,La-diesis8,Si8,Do9,Do-diesis9,Re9,Re-diesis9,Mi9,Fa9,Fa-diesis9,Sol9\n"
			+ "cbx_note_id_italian_uc/cbx_halftone_id_diesis/cbx_halftone_id_diesis:\n"
			+ "Do''',Do-diesis''',Re''',Re-diesis''',Mi''',Fa''',Fa-diesis''',Sol''',Sol-diesis''',La''',La-diesis''',Si''',Do'',Do-diesis'',Re'',Re-diesis'',Mi'',Fa'',Fa-diesis'',Sol'',Sol-diesis'',La'',La-diesis'',Si'',Do',Do-diesis',Re',Re-diesis',Mi',Fa',Fa-diesis',Sol',Sol-diesis',La',La-diesis',Si',Do,Do-diesis,Re,Re-diesis,Mi,Fa,Fa-diesis,Sol,Sol-diesis,La,La-diesis,Si,do,do-diesis,re,re-diesis,mi,fa,fa-diesis,sol,sol-diesis,la,la-diesis,si,do',do-diesis',re',re-diesis',mi',fa',fa-diesis',sol',sol-diesis',la',la-diesis',si',do'',do-diesis'',re'',re-diesis'',mi'',fa'',fa-diesis'',sol'',sol-diesis'',la'',la-diesis'',si'',do''',do-diesis''',re''',re-diesis''',mi''',fa''',fa-diesis''',sol''',sol-diesis''',la''',la-diesis''',si''',do'''',do-diesis'''',re'''',re-diesis'''',mi'''',fa'''',fa-diesis'''',sol'''',sol-diesis'''',la'''',la-diesis'''',si'''',do''''',do-diesis''''',re''''',re-diesis''''',mi''''',fa''''',fa-diesis''''',sol''''',sol-diesis''''',la''''',la-diesis''''',si''''',do'''''',do-diesis'''''',re'''''',re-diesis'''''',mi'''''',fa'''''',fa-diesis'''''',sol''''''\n"
			+ "cbx_note_id_italian_uc/cbx_halftone_id_bemolle/cbx_halftone_id_bemolle:\n"
			+ "Do-5,Re-bemolle-5,Re-5,Mi-bemolle-5,Mi-5,Fa-5,Sol-bemolle-5,Sol-5,La-bemolle-5,La-5,Si-bemolle-5,Si-5,Do-4,Re-bemolle-4,Re-4,Mi-bemolle-4,Mi-4,Fa-4,Sol-bemolle-4,Sol-4,La-bemolle-4,La-4,Si-bemolle-4,Si-4,Do-3,Re-bemolle-3,Re-3,Mi-bemolle-3,Mi-3,Fa-3,Sol-bemolle-3,Sol-3,La-bemolle-3,La-3,Si-bemolle-3,Si-3,Do-2,Re-bemolle-2,Re-2,Mi-bemolle-2,Mi-2,Fa-2,Sol-bemolle-2,Sol-2,La-bemolle-2,La-2,Si-bemolle-2,Si-2,Do-,Re-bemolle-,Re-,Mi-bemolle-,Mi-,Fa-,Sol-bemolle-,Sol-,La-bemolle-,La-,Si-bemolle-,Si-,Do,Re-bemolle,Re,Mi-bemolle,Mi,Fa,Sol-bemolle,Sol,La-bemolle,La,Si-bemolle,Si,Do+,Re-bemolle+,Re+,Mi-bemolle+,Mi+,Fa+,Sol-bemolle+,Sol+,La-bemolle+,La+,Si-bemolle+,Si+,Do+2,Re-bemolle+2,Re+2,Mi-bemolle+2,Mi+2,Fa+2,Sol-bemolle+2,Sol+2,La-bemolle+2,La+2,Si-bemolle+2,Si+2,Do+3,Re-bemolle+3,Re+3,Mi-bemolle+3,Mi+3,Fa+3,Sol-bemolle+3,Sol+3,La-bemolle+3,La+3,Si-bemolle+3,Si+3,Do+4,Re-bemolle+4,Re+4,Mi-bemolle+4,Mi+4,Fa+4,Sol-bemolle+4,Sol+4,La-bemolle+4,La+4,Si-bemolle+4,Si+4,Do+5,Re-bemolle+5,Re+5,Mi-bemolle+5,Mi+5,Fa+5,Sol-bemolle+5,Sol+5\n"
			+ "cbx_note_id_italian_uc/cbx_halftone_id_bemolle/cbx_halftone_id_bemolle:\n"
			+ "Do-----,Re-bemolle-----,Re-----,Mi-bemolle-----,Mi-----,Fa-----,Sol-bemolle-----,Sol-----,La-bemolle-----,La-----,Si-bemolle-----,Si-----,Do----,Re-bemolle----,Re----,Mi-bemolle----,Mi----,Fa----,Sol-bemolle----,Sol----,La-bemolle----,La----,Si-bemolle----,Si----,Do---,Re-bemolle---,Re---,Mi-bemolle---,Mi---,Fa---,Sol-bemolle---,Sol---,La-bemolle---,La---,Si-bemolle---,Si---,Do--,Re-bemolle--,Re--,Mi-bemolle--,Mi--,Fa--,Sol-bemolle--,Sol--,La-bemolle--,La--,Si-bemolle--,Si--,Do-,Re-bemolle-,Re-,Mi-bemolle-,Mi-,Fa-,Sol-bemolle-,Sol-,La-bemolle-,La-,Si-bemolle-,Si-,Do,Re-bemolle,Re,Mi-bemolle,Mi,Fa,Sol-bemolle,Sol,La-bemolle,La,Si-bemolle,Si,Do+,Re-bemolle+,Re+,Mi-bemolle+,Mi+,Fa+,Sol-bemolle+,Sol+,La-bemolle+,La+,Si-bemolle+,Si+,Do++,Re-bemolle++,Re++,Mi-bemolle++,Mi++,Fa++,Sol-bemolle++,Sol++,La-bemolle++,La++,Si-bemolle++,Si++,Do+++,Re-bemolle+++,Re+++,Mi-bemolle+++,Mi+++,Fa+++,Sol-bemolle+++,Sol+++,La-bemolle+++,La+++,Si-bemolle+++,Si+++,Do++++,Re-bemolle++++,Re++++,Mi-bemolle++++,Mi++++,Fa++++,Sol-bemolle++++,Sol++++,La-bemolle++++,La++++,Si-bemolle++++,Si++++,Do+++++,Re-bemolle+++++,Re+++++,Mi-bemolle+++++,Mi+++++,Fa+++++,Sol-bemolle+++++,Sol+++++\n"
			+ "cbx_note_id_italian_uc/cbx_halftone_id_bemolle/cbx_halftone_id_bemolle:\n"
			+ "Do-1,Re-bemolle-1,Re-1,Mi-bemolle-1,Mi-1,Fa-1,Sol-bemolle-1,Sol-1,La-bemolle-1,La-1,Si-bemolle-1,Si-1,Do0,Re-bemolle0,Re0,Mi-bemolle0,Mi0,Fa0,Sol-bemolle0,Sol0,La-bemolle0,La0,Si-bemolle0,Si0,Do1,Re-bemolle1,Re1,Mi-bemolle1,Mi1,Fa1,Sol-bemolle1,Sol1,La-bemolle1,La1,Si-bemolle1,Si1,Do2,Re-bemolle2,Re2,Mi-bemolle2,Mi2,Fa2,Sol-bemolle2,Sol2,La-bemolle2,La2,Si-bemolle2,Si2,Do3,Re-bemolle3,Re3,Mi-bemolle3,Mi3,Fa3,Sol-bemolle3,Sol3,La-bemolle3,La3,Si-bemolle3,Si3,Do4,Re-bemolle4,Re4,Mi-bemolle4,Mi4,Fa4,Sol-bemolle4,Sol4,La-bemolle4,La4,Si-bemolle4,Si4,Do5,Re-bemolle5,Re5,Mi-bemolle5,Mi5,Fa5,Sol-bemolle5,Sol5,La-bemolle5,La5,Si-bemolle5,Si5,Do6,Re-bemolle6,Re6,Mi-bemolle6,Mi6,Fa6,Sol-bemolle6,Sol6,La-bemolle6,La6,Si-bemolle6,Si6,Do7,Re-bemolle7,Re7,Mi-bemolle7,Mi7,Fa7,Sol-bemolle7,Sol7,La-bemolle7,La7,Si-bemolle7,Si7,Do8,Re-bemolle8,Re8,Mi-bemolle8,Mi8,Fa8,Sol-bemolle8,Sol8,La-bemolle8,La8,Si-bemolle8,Si8,Do9,Re-bemolle9,Re9,Mi-bemolle9,Mi9,Fa9,Sol-bemolle9,Sol9\n"
			+ "cbx_note_id_italian_uc/cbx_halftone_id_bemolle/cbx_halftone_id_bemolle:\n"
			+ "Do''',Re-bemolle''',Re''',Mi-bemolle''',Mi''',Fa''',Sol-bemolle''',Sol''',La-bemolle''',La''',Si-bemolle''',Si''',Do'',Re-bemolle'',Re'',Mi-bemolle'',Mi'',Fa'',Sol-bemolle'',Sol'',La-bemolle'',La'',Si-bemolle'',Si'',Do',Re-bemolle',Re',Mi-bemolle',Mi',Fa',Sol-bemolle',Sol',La-bemolle',La',Si-bemolle',Si',Do,Re-bemolle,Re,Mi-bemolle,Mi,Fa,Sol-bemolle,Sol,La-bemolle,La,Si-bemolle,Si,do,re-bemolle,re,mi-bemolle,mi,fa,sol-bemolle,sol,la-bemolle,la,si-bemolle,si,do',re-bemolle',re',mi-bemolle',mi',fa',sol-bemolle',sol',la-bemolle',la',si-bemolle',si',do'',re-bemolle'',re'',mi-bemolle'',mi'',fa'',sol-bemolle'',sol'',la-bemolle'',la'',si-bemolle'',si'',do''',re-bemolle''',re''',mi-bemolle''',mi''',fa''',sol-bemolle''',sol''',la-bemolle''',la''',si-bemolle''',si''',do'''',re-bemolle'''',re'''',mi-bemolle'''',mi'''',fa'''',sol-bemolle'''',sol'''',la-bemolle'''',la'''',si-bemolle'''',si'''',do''''',re-bemolle''''',re''''',mi-bemolle''''',mi''''',fa''''',sol-bemolle''''',sol''''',la-bemolle''''',la''''',si-bemolle''''',si''''',do'''''',re-bemolle'''''',re'''''',mi-bemolle'''''',mi'''''',fa'''''',sol-bemolle'''''',sol''''''\n"
			+ "cbx_note_id_italian_uc/cbx_halftone_id_cis/cbx_halftone_id_cis:\n"
			+ "Do-5,Dois-5,Re-5,Reis-5,Mi-5,Fa-5,Fais-5,Sol-5,Solis-5,La-5,Lais-5,Si-5,Do-4,Dois-4,Re-4,Reis-4,Mi-4,Fa-4,Fais-4,Sol-4,Solis-4,La-4,Lais-4,Si-4,Do-3,Dois-3,Re-3,Reis-3,Mi-3,Fa-3,Fais-3,Sol-3,Solis-3,La-3,Lais-3,Si-3,Do-2,Dois-2,Re-2,Reis-2,Mi-2,Fa-2,Fais-2,Sol-2,Solis-2,La-2,Lais-2,Si-2,Do-,Dois-,Re-,Reis-,Mi-,Fa-,Fais-,Sol-,Solis-,La-,Lais-,Si-,Do,Dois,Re,Reis,Mi,Fa,Fais,Sol,Solis,La,Lais,Si,Do+,Dois+,Re+,Reis+,Mi+,Fa+,Fais+,Sol+,Solis+,La+,Lais+,Si+,Do+2,Dois+2,Re+2,Reis+2,Mi+2,Fa+2,Fais+2,Sol+2,Solis+2,La+2,Lais+2,Si+2,Do+3,Dois+3,Re+3,Reis+3,Mi+3,Fa+3,Fais+3,Sol+3,Solis+3,La+3,Lais+3,Si+3,Do+4,Dois+4,Re+4,Reis+4,Mi+4,Fa+4,Fais+4,Sol+4,Solis+4,La+4,Lais+4,Si+4,Do+5,Dois+5,Re+5,Reis+5,Mi+5,Fa+5,Fais+5,Sol+5\n"
			+ "cbx_note_id_italian_uc/cbx_halftone_id_cis/cbx_halftone_id_cis:\n"
			+ "Do-----,Dois-----,Re-----,Reis-----,Mi-----,Fa-----,Fais-----,Sol-----,Solis-----,La-----,Lais-----,Si-----,Do----,Dois----,Re----,Reis----,Mi----,Fa----,Fais----,Sol----,Solis----,La----,Lais----,Si----,Do---,Dois---,Re---,Reis---,Mi---,Fa---,Fais---,Sol---,Solis---,La---,Lais---,Si---,Do--,Dois--,Re--,Reis--,Mi--,Fa--,Fais--,Sol--,Solis--,La--,Lais--,Si--,Do-,Dois-,Re-,Reis-,Mi-,Fa-,Fais-,Sol-,Solis-,La-,Lais-,Si-,Do,Dois,Re,Reis,Mi,Fa,Fais,Sol,Solis,La,Lais,Si,Do+,Dois+,Re+,Reis+,Mi+,Fa+,Fais+,Sol+,Solis+,La+,Lais+,Si+,Do++,Dois++,Re++,Reis++,Mi++,Fa++,Fais++,Sol++,Solis++,La++,Lais++,Si++,Do+++,Dois+++,Re+++,Reis+++,Mi+++,Fa+++,Fais+++,Sol+++,Solis+++,La+++,Lais+++,Si+++,Do++++,Dois++++,Re++++,Reis++++,Mi++++,Fa++++,Fais++++,Sol++++,Solis++++,La++++,Lais++++,Si++++,Do+++++,Dois+++++,Re+++++,Reis+++++,Mi+++++,Fa+++++,Fais+++++,Sol+++++\n"
			+ "cbx_note_id_italian_uc/cbx_halftone_id_cis/cbx_halftone_id_cis:\n"
			+ "Do-1,Dois-1,Re-1,Reis-1,Mi-1,Fa-1,Fais-1,Sol-1,Solis-1,La-1,Lais-1,Si-1,Do0,Dois0,Re0,Reis0,Mi0,Fa0,Fais0,Sol0,Solis0,La0,Lais0,Si0,Do1,Dois1,Re1,Reis1,Mi1,Fa1,Fais1,Sol1,Solis1,La1,Lais1,Si1,Do2,Dois2,Re2,Reis2,Mi2,Fa2,Fais2,Sol2,Solis2,La2,Lais2,Si2,Do3,Dois3,Re3,Reis3,Mi3,Fa3,Fais3,Sol3,Solis3,La3,Lais3,Si3,Do4,Dois4,Re4,Reis4,Mi4,Fa4,Fais4,Sol4,Solis4,La4,Lais4,Si4,Do5,Dois5,Re5,Reis5,Mi5,Fa5,Fais5,Sol5,Solis5,La5,Lais5,Si5,Do6,Dois6,Re6,Reis6,Mi6,Fa6,Fais6,Sol6,Solis6,La6,Lais6,Si6,Do7,Dois7,Re7,Reis7,Mi7,Fa7,Fais7,Sol7,Solis7,La7,Lais7,Si7,Do8,Dois8,Re8,Reis8,Mi8,Fa8,Fais8,Sol8,Solis8,La8,Lais8,Si8,Do9,Dois9,Re9,Reis9,Mi9,Fa9,Fais9,Sol9\n"
			+ "cbx_note_id_italian_uc/cbx_halftone_id_cis/cbx_halftone_id_cis:\n"
			+ "Do''',Dois''',Re''',Reis''',Mi''',Fa''',Fais''',Sol''',Solis''',La''',Lais''',Si''',Do'',Dois'',Re'',Reis'',Mi'',Fa'',Fais'',Sol'',Solis'',La'',Lais'',Si'',Do',Dois',Re',Reis',Mi',Fa',Fais',Sol',Solis',La',Lais',Si',Do,Dois,Re,Reis,Mi,Fa,Fais,Sol,Solis,La,Lais,Si,do,dois,re,reis,mi,fa,fais,sol,solis,la,lais,si,do',dois',re',reis',mi',fa',fais',sol',solis',la',lais',si',do'',dois'',re'',reis'',mi'',fa'',fais'',sol'',solis'',la'',lais'',si'',do''',dois''',re''',reis''',mi''',fa''',fais''',sol''',solis''',la''',lais''',si''',do'''',dois'''',re'''',reis'''',mi'''',fa'''',fais'''',sol'''',solis'''',la'''',lais'''',si'''',do''''',dois''''',re''''',reis''''',mi''''',fa''''',fais''''',sol''''',solis''''',la''''',lais''''',si''''',do'''''',dois'''''',re'''''',reis'''''',mi'''''',fa'''''',fais'''''',sol''''''\n"
			+ "cbx_note_id_italian_uc/cbx_halftone_id_des/cbx_halftone_id_des:\n"
			+ "Do-5,Rees-5,Re-5,Mies-5,Mi-5,Fa-5,Soles-5,Sol-5,Laes-5,La-5,Sies-5,Si-5,Do-4,Rees-4,Re-4,Mies-4,Mi-4,Fa-4,Soles-4,Sol-4,Laes-4,La-4,Sies-4,Si-4,Do-3,Rees-3,Re-3,Mies-3,Mi-3,Fa-3,Soles-3,Sol-3,Laes-3,La-3,Sies-3,Si-3,Do-2,Rees-2,Re-2,Mies-2,Mi-2,Fa-2,Soles-2,Sol-2,Laes-2,La-2,Sies-2,Si-2,Do-,Rees-,Re-,Mies-,Mi-,Fa-,Soles-,Sol-,Laes-,La-,Sies-,Si-,Do,Rees,Re,Mies,Mi,Fa,Soles,Sol,Laes,La,Sies,Si,Do+,Rees+,Re+,Mies+,Mi+,Fa+,Soles+,Sol+,Laes+,La+,Sies+,Si+,Do+2,Rees+2,Re+2,Mies+2,Mi+2,Fa+2,Soles+2,Sol+2,Laes+2,La+2,Sies+2,Si+2,Do+3,Rees+3,Re+3,Mies+3,Mi+3,Fa+3,Soles+3,Sol+3,Laes+3,La+3,Sies+3,Si+3,Do+4,Rees+4,Re+4,Mies+4,Mi+4,Fa+4,Soles+4,Sol+4,Laes+4,La+4,Sies+4,Si+4,Do+5,Rees+5,Re+5,Mies+5,Mi+5,Fa+5,Soles+5,Sol+5\n"
			+ "cbx_note_id_italian_uc/cbx_halftone_id_des/cbx_halftone_id_des:\n"
			+ "Do-----,Rees-----,Re-----,Mies-----,Mi-----,Fa-----,Soles-----,Sol-----,Laes-----,La-----,Sies-----,Si-----,Do----,Rees----,Re----,Mies----,Mi----,Fa----,Soles----,Sol----,Laes----,La----,Sies----,Si----,Do---,Rees---,Re---,Mies---,Mi---,Fa---,Soles---,Sol---,Laes---,La---,Sies---,Si---,Do--,Rees--,Re--,Mies--,Mi--,Fa--,Soles--,Sol--,Laes--,La--,Sies--,Si--,Do-,Rees-,Re-,Mies-,Mi-,Fa-,Soles-,Sol-,Laes-,La-,Sies-,Si-,Do,Rees,Re,Mies,Mi,Fa,Soles,Sol,Laes,La,Sies,Si,Do+,Rees+,Re+,Mies+,Mi+,Fa+,Soles+,Sol+,Laes+,La+,Sies+,Si+,Do++,Rees++,Re++,Mies++,Mi++,Fa++,Soles++,Sol++,Laes++,La++,Sies++,Si++,Do+++,Rees+++,Re+++,Mies+++,Mi+++,Fa+++,Soles+++,Sol+++,Laes+++,La+++,Sies+++,Si+++,Do++++,Rees++++,Re++++,Mies++++,Mi++++,Fa++++,Soles++++,Sol++++,Laes++++,La++++,Sies++++,Si++++,Do+++++,Rees+++++,Re+++++,Mies+++++,Mi+++++,Fa+++++,Soles+++++,Sol+++++\n"
			+ "cbx_note_id_italian_uc/cbx_halftone_id_des/cbx_halftone_id_des:\n"
			+ "Do-1,Rees-1,Re-1,Mies-1,Mi-1,Fa-1,Soles-1,Sol-1,Laes-1,La-1,Sies-1,Si-1,Do0,Rees0,Re0,Mies0,Mi0,Fa0,Soles0,Sol0,Laes0,La0,Sies0,Si0,Do1,Rees1,Re1,Mies1,Mi1,Fa1,Soles1,Sol1,Laes1,La1,Sies1,Si1,Do2,Rees2,Re2,Mies2,Mi2,Fa2,Soles2,Sol2,Laes2,La2,Sies2,Si2,Do3,Rees3,Re3,Mies3,Mi3,Fa3,Soles3,Sol3,Laes3,La3,Sies3,Si3,Do4,Rees4,Re4,Mies4,Mi4,Fa4,Soles4,Sol4,Laes4,La4,Sies4,Si4,Do5,Rees5,Re5,Mies5,Mi5,Fa5,Soles5,Sol5,Laes5,La5,Sies5,Si5,Do6,Rees6,Re6,Mies6,Mi6,Fa6,Soles6,Sol6,Laes6,La6,Sies6,Si6,Do7,Rees7,Re7,Mies7,Mi7,Fa7,Soles7,Sol7,Laes7,La7,Sies7,Si7,Do8,Rees8,Re8,Mies8,Mi8,Fa8,Soles8,Sol8,Laes8,La8,Sies8,Si8,Do9,Rees9,Re9,Mies9,Mi9,Fa9,Soles9,Sol9\n"
			+ "cbx_note_id_italian_uc/cbx_halftone_id_des/cbx_halftone_id_des:\n"
			+ "Do''',Rees''',Re''',Mies''',Mi''',Fa''',Soles''',Sol''',Laes''',La''',Sies''',Si''',Do'',Rees'',Re'',Mies'',Mi'',Fa'',Soles'',Sol'',Laes'',La'',Sies'',Si'',Do',Rees',Re',Mies',Mi',Fa',Soles',Sol',Laes',La',Sies',Si',Do,Rees,Re,Mies,Mi,Fa,Soles,Sol,Laes,La,Sies,Si,do,rees,re,mies,mi,fa,soles,sol,laes,la,sies,si,do',rees',re',mies',mi',fa',soles',sol',laes',la',sies',si',do'',rees'',re'',mies'',mi'',fa'',soles'',sol'',laes'',la'',sies'',si'',do''',rees''',re''',mies''',mi''',fa''',soles''',sol''',laes''',la''',sies''',si''',do'''',rees'''',re'''',mies'''',mi'''',fa'''',soles'''',sol'''',laes'''',la'''',sies'''',si'''',do''''',rees''''',re''''',mies''''',mi''''',fa''''',soles''''',sol''''',laes''''',la''''',sies''''',si''''',do'''''',rees'''''',re'''''',mies'''''',mi'''''',fa'''''',soles'''''',sol''''''\n"
			+ "cbx_note_id_german_lc/cbx_halftone_id_sharp/cbx_halftone_id_sharp:\n"
			+ "c-5,c#-5,d-5,d#-5,e-5,f-5,f#-5,g-5,g#-5,a-5,b-5,h-5,c-4,c#-4,d-4,d#-4,e-4,f-4,f#-4,g-4,g#-4,a-4,b-4,h-4,c-3,c#-3,d-3,d#-3,e-3,f-3,f#-3,g-3,g#-3,a-3,b-3,h-3,c-2,c#-2,d-2,d#-2,e-2,f-2,f#-2,g-2,g#-2,a-2,b-2,h-2,c-,c#-,d-,d#-,e-,f-,f#-,g-,g#-,a-,b-,h-,c,c#,d,d#,e,f,f#,g,g#,a,b,h,c+,c#+,d+,d#+,e+,f+,f#+,g+,g#+,a+,b+,h+,c+2,c#+2,d+2,d#+2,e+2,f+2,f#+2,g+2,g#+2,a+2,b+2,h+2,c+3,c#+3,d+3,d#+3,e+3,f+3,f#+3,g+3,g#+3,a+3,b+3,h+3,c+4,c#+4,d+4,d#+4,e+4,f+4,f#+4,g+4,g#+4,a+4,b+4,h+4,c+5,c#+5,d+5,d#+5,e+5,f+5,f#+5,g+5\n"
			+ "cbx_note_id_german_lc/cbx_halftone_id_sharp/cbx_halftone_id_sharp:\n"
			+ "c-----,c#-----,d-----,d#-----,e-----,f-----,f#-----,g-----,g#-----,a-----,b-----,h-----,c----,c#----,d----,d#----,e----,f----,f#----,g----,g#----,a----,b----,h----,c---,c#---,d---,d#---,e---,f---,f#---,g---,g#---,a---,b---,h---,c--,c#--,d--,d#--,e--,f--,f#--,g--,g#--,a--,b--,h--,c-,c#-,d-,d#-,e-,f-,f#-,g-,g#-,a-,b-,h-,c,c#,d,d#,e,f,f#,g,g#,a,b,h,c+,c#+,d+,d#+,e+,f+,f#+,g+,g#+,a+,b+,h+,c++,c#++,d++,d#++,e++,f++,f#++,g++,g#++,a++,b++,h++,c+++,c#+++,d+++,d#+++,e+++,f+++,f#+++,g+++,g#+++,a+++,b+++,h+++,c++++,c#++++,d++++,d#++++,e++++,f++++,f#++++,g++++,g#++++,a++++,b++++,h++++,c+++++,c#+++++,d+++++,d#+++++,e+++++,f+++++,f#+++++,g+++++\n"
			+ "cbx_note_id_german_lc/cbx_halftone_id_sharp/cbx_halftone_id_sharp:\n"
			+ "c-1,c#-1,d-1,d#-1,e-1,f-1,f#-1,g-1,g#-1,a-1,b-1,h-1,c0,c#0,d0,d#0,e0,f0,f#0,g0,g#0,a0,b0,h0,c1,c#1,d1,d#1,e1,f1,f#1,g1,g#1,a1,b1,h1,c2,c#2,d2,d#2,e2,f2,f#2,g2,g#2,a2,b2,h2,c3,c#3,d3,d#3,e3,f3,f#3,g3,g#3,a3,b3,h3,c4,c#4,d4,d#4,e4,f4,f#4,g4,g#4,a4,b4,h4,c5,c#5,d5,d#5,e5,f5,f#5,g5,g#5,a5,b5,h5,c6,c#6,d6,d#6,e6,f6,f#6,g6,g#6,a6,b6,h6,c7,c#7,d7,d#7,e7,f7,f#7,g7,g#7,a7,b7,h7,c8,c#8,d8,d#8,e8,f8,f#8,g8,g#8,a8,b8,h8,c9,c#9,d9,d#9,e9,f9,f#9,g9\n"
			+ "cbx_note_id_german_lc/cbx_halftone_id_sharp/cbx_halftone_id_sharp:\n"
			+ "C''',C#''',D''',D#''',E''',F''',F#''',G''',G#''',A''',B''',H''',C'',C#'',D'',D#'',E'',F'',F#'',G'',G#'',A'',B'',H'',C',C#',D',D#',E',F',F#',G',G#',A',B',H',C,C#,D,D#,E,F,F#,G,G#,A,B,H,c,c#,d,d#,e,f,f#,g,g#,a,b,h,c',c#',d',d#',e',f',f#',g',g#',a',b',h',c'',c#'',d'',d#'',e'',f'',f#'',g'',g#'',a'',b'',h'',c''',c#''',d''',d#''',e''',f''',f#''',g''',g#''',a''',b''',h''',c'''',c#'''',d'''',d#'''',e'''',f'''',f#'''',g'''',g#'''',a'''',b'''',h'''',c''''',c#''''',d''''',d#''''',e''''',f''''',f#''''',g''''',g#''''',a''''',b''''',h''''',c'''''',c#'''''',d'''''',d#'''''',e'''''',f'''''',f#'''''',g''''''\n"
			+ "cbx_note_id_german_lc/cbx_halftone_id_flat/cbx_halftone_id_flat:\n"
			+ "c-5,db-5,d-5,eb-5,e-5,f-5,gb-5,g-5,ab-5,a-5,b-5,h-5,c-4,db-4,d-4,eb-4,e-4,f-4,gb-4,g-4,ab-4,a-4,b-4,h-4,c-3,db-3,d-3,eb-3,e-3,f-3,gb-3,g-3,ab-3,a-3,b-3,h-3,c-2,db-2,d-2,eb-2,e-2,f-2,gb-2,g-2,ab-2,a-2,b-2,h-2,c-,db-,d-,eb-,e-,f-,gb-,g-,ab-,a-,b-,h-,c,db,d,eb,e,f,gb,g,ab,a,b,h,c+,db+,d+,eb+,e+,f+,gb+,g+,ab+,a+,b+,h+,c+2,db+2,d+2,eb+2,e+2,f+2,gb+2,g+2,ab+2,a+2,b+2,h+2,c+3,db+3,d+3,eb+3,e+3,f+3,gb+3,g+3,ab+3,a+3,b+3,h+3,c+4,db+4,d+4,eb+4,e+4,f+4,gb+4,g+4,ab+4,a+4,b+4,h+4,c+5,db+5,d+5,eb+5,e+5,f+5,gb+5,g+5\n"
			+ "cbx_note_id_german_lc/cbx_halftone_id_flat/cbx_halftone_id_flat:\n"
			+ "c-----,db-----,d-----,eb-----,e-----,f-----,gb-----,g-----,ab-----,a-----,b-----,h-----,c----,db----,d----,eb----,e----,f----,gb----,g----,ab----,a----,b----,h----,c---,db---,d---,eb---,e---,f---,gb---,g---,ab---,a---,b---,h---,c--,db--,d--,eb--,e--,f--,gb--,g--,ab--,a--,b--,h--,c-,db-,d-,eb-,e-,f-,gb-,g-,ab-,a-,b-,h-,c,db,d,eb,e,f,gb,g,ab,a,b,h,c+,db+,d+,eb+,e+,f+,gb+,g+,ab+,a+,b+,h+,c++,db++,d++,eb++,e++,f++,gb++,g++,ab++,a++,b++,h++,c+++,db+++,d+++,eb+++,e+++,f+++,gb+++,g+++,ab+++,a+++,b+++,h+++,c++++,db++++,d++++,eb++++,e++++,f++++,gb++++,g++++,ab++++,a++++,b++++,h++++,c+++++,db+++++,d+++++,eb+++++,e+++++,f+++++,gb+++++,g+++++\n"
			+ "cbx_note_id_german_lc/cbx_halftone_id_flat/cbx_halftone_id_flat:\n"
			+ "c-1,db-1,d-1,eb-1,e-1,f-1,gb-1,g-1,ab-1,a-1,b-1,h-1,c0,db0,d0,eb0,e0,f0,gb0,g0,ab0,a0,b0,h0,c1,db1,d1,eb1,e1,f1,gb1,g1,ab1,a1,b1,h1,c2,db2,d2,eb2,e2,f2,gb2,g2,ab2,a2,b2,h2,c3,db3,d3,eb3,e3,f3,gb3,g3,ab3,a3,b3,h3,c4,db4,d4,eb4,e4,f4,gb4,g4,ab4,a4,b4,h4,c5,db5,d5,eb5,e5,f5,gb5,g5,ab5,a5,b5,h5,c6,db6,d6,eb6,e6,f6,gb6,g6,ab6,a6,b6,h6,c7,db7,d7,eb7,e7,f7,gb7,g7,ab7,a7,b7,h7,c8,db8,d8,eb8,e8,f8,gb8,g8,ab8,a8,b8,h8,c9,db9,d9,eb9,e9,f9,gb9,g9\n"
			+ "cbx_note_id_german_lc/cbx_halftone_id_flat/cbx_halftone_id_flat:\n"
			+ "C''',Db''',D''',Eb''',E''',F''',Gb''',G''',Ab''',A''',B''',H''',C'',Db'',D'',Eb'',E'',F'',Gb'',G'',Ab'',A'',B'',H'',C',Db',D',Eb',E',F',Gb',G',Ab',A',B',H',C,Db,D,Eb,E,F,Gb,G,Ab,A,B,H,c,db,d,eb,e,f,gb,g,ab,a,b,h,c',db',d',eb',e',f',gb',g',ab',a',b',h',c'',db'',d'',eb'',e'',f'',gb'',g'',ab'',a'',b'',h'',c''',db''',d''',eb''',e''',f''',gb''',g''',ab''',a''',b''',h''',c'''',db'''',d'''',eb'''',e'''',f'''',gb'''',g'''',ab'''',a'''',b'''',h'''',c''''',db''''',d''''',eb''''',e''''',f''''',gb''''',g''''',ab''''',a''''',b''''',h''''',c'''''',db'''''',d'''''',eb'''''',e'''''',f'''''',gb'''''',g''''''\n"
			+ "cbx_note_id_german_lc/cbx_halftone_id_diesis/cbx_halftone_id_diesis:\n"
			+ "c-5,c-diesis-5,d-5,d-diesis-5,e-5,f-5,f-diesis-5,g-5,g-diesis-5,a-5,b-5,h-5,c-4,c-diesis-4,d-4,d-diesis-4,e-4,f-4,f-diesis-4,g-4,g-diesis-4,a-4,b-4,h-4,c-3,c-diesis-3,d-3,d-diesis-3,e-3,f-3,f-diesis-3,g-3,g-diesis-3,a-3,b-3,h-3,c-2,c-diesis-2,d-2,d-diesis-2,e-2,f-2,f-diesis-2,g-2,g-diesis-2,a-2,b-2,h-2,c-,c-diesis-,d-,d-diesis-,e-,f-,f-diesis-,g-,g-diesis-,a-,b-,h-,c,c-diesis,d,d-diesis,e,f,f-diesis,g,g-diesis,a,b,h,c+,c-diesis+,d+,d-diesis+,e+,f+,f-diesis+,g+,g-diesis+,a+,b+,h+,c+2,c-diesis+2,d+2,d-diesis+2,e+2,f+2,f-diesis+2,g+2,g-diesis+2,a+2,b+2,h+2,c+3,c-diesis+3,d+3,d-diesis+3,e+3,f+3,f-diesis+3,g+3,g-diesis+3,a+3,b+3,h+3,c+4,c-diesis+4,d+4,d-diesis+4,e+4,f+4,f-diesis+4,g+4,g-diesis+4,a+4,b+4,h+4,c+5,c-diesis+5,d+5,d-diesis+5,e+5,f+5,f-diesis+5,g+5\n"
			+ "cbx_note_id_german_lc/cbx_halftone_id_diesis/cbx_halftone_id_diesis:\n"
			+ "c-----,c-diesis-----,d-----,d-diesis-----,e-----,f-----,f-diesis-----,g-----,g-diesis-----,a-----,b-----,h-----,c----,c-diesis----,d----,d-diesis----,e----,f----,f-diesis----,g----,g-diesis----,a----,b----,h----,c---,c-diesis---,d---,d-diesis---,e---,f---,f-diesis---,g---,g-diesis---,a---,b---,h---,c--,c-diesis--,d--,d-diesis--,e--,f--,f-diesis--,g--,g-diesis--,a--,b--,h--,c-,c-diesis-,d-,d-diesis-,e-,f-,f-diesis-,g-,g-diesis-,a-,b-,h-,c,c-diesis,d,d-diesis,e,f,f-diesis,g,g-diesis,a,b,h,c+,c-diesis+,d+,d-diesis+,e+,f+,f-diesis+,g+,g-diesis+,a+,b+,h+,c++,c-diesis++,d++,d-diesis++,e++,f++,f-diesis++,g++,g-diesis++,a++,b++,h++,c+++,c-diesis+++,d+++,d-diesis+++,e+++,f+++,f-diesis+++,g+++,g-diesis+++,a+++,b+++,h+++,c++++,c-diesis++++,d++++,d-diesis++++,e++++,f++++,f-diesis++++,g++++,g-diesis++++,a++++,b++++,h++++,c+++++,c-diesis+++++,d+++++,d-diesis+++++,e+++++,f+++++,f-diesis+++++,g+++++\n"
			+ "cbx_note_id_german_lc/cbx_halftone_id_diesis/cbx_halftone_id_diesis:\n"
			+ "c-1,c-diesis-1,d-1,d-diesis-1,e-1,f-1,f-diesis-1,g-1,g-diesis-1,a-1,b-1,h-1,c0,c-diesis0,d0,d-diesis0,e0,f0,f-diesis0,g0,g-diesis0,a0,b0,h0,c1,c-diesis1,d1,d-diesis1,e1,f1,f-diesis1,g1,g-diesis1,a1,b1,h1,c2,c-diesis2,d2,d-diesis2,e2,f2,f-diesis2,g2,g-diesis2,a2,b2,h2,c3,c-diesis3,d3,d-diesis3,e3,f3,f-diesis3,g3,g-diesis3,a3,b3,h3,c4,c-diesis4,d4,d-diesis4,e4,f4,f-diesis4,g4,g-diesis4,a4,b4,h4,c5,c-diesis5,d5,d-diesis5,e5,f5,f-diesis5,g5,g-diesis5,a5,b5,h5,c6,c-diesis6,d6,d-diesis6,e6,f6,f-diesis6,g6,g-diesis6,a6,b6,h6,c7,c-diesis7,d7,d-diesis7,e7,f7,f-diesis7,g7,g-diesis7,a7,b7,h7,c8,c-diesis8,d8,d-diesis8,e8,f8,f-diesis8,g8,g-diesis8,a8,b8,h8,c9,c-diesis9,d9,d-diesis9,e9,f9,f-diesis9,g9\n"
			+ "cbx_note_id_german_lc/cbx_halftone_id_diesis/cbx_halftone_id_diesis:\n"
			+ "C''',C-diesis''',D''',D-diesis''',E''',F''',F-diesis''',G''',G-diesis''',A''',B''',H''',C'',C-diesis'',D'',D-diesis'',E'',F'',F-diesis'',G'',G-diesis'',A'',B'',H'',C',C-diesis',D',D-diesis',E',F',F-diesis',G',G-diesis',A',B',H',C,C-diesis,D,D-diesis,E,F,F-diesis,G,G-diesis,A,B,H,c,c-diesis,d,d-diesis,e,f,f-diesis,g,g-diesis,a,b,h,c',c-diesis',d',d-diesis',e',f',f-diesis',g',g-diesis',a',b',h',c'',c-diesis'',d'',d-diesis'',e'',f'',f-diesis'',g'',g-diesis'',a'',b'',h'',c''',c-diesis''',d''',d-diesis''',e''',f''',f-diesis''',g''',g-diesis''',a''',b''',h''',c'''',c-diesis'''',d'''',d-diesis'''',e'''',f'''',f-diesis'''',g'''',g-diesis'''',a'''',b'''',h'''',c''''',c-diesis''''',d''''',d-diesis''''',e''''',f''''',f-diesis''''',g''''',g-diesis''''',a''''',b''''',h''''',c'''''',c-diesis'''''',d'''''',d-diesis'''''',e'''''',f'''''',f-diesis'''''',g''''''\n"
			+ "cbx_note_id_german_lc/cbx_halftone_id_bemolle/cbx_halftone_id_bemolle:\n"
			+ "c-5,d-bemolle-5,d-5,e-bemolle-5,e-5,f-5,g-bemolle-5,g-5,a-bemolle-5,a-5,b-5,h-5,c-4,d-bemolle-4,d-4,e-bemolle-4,e-4,f-4,g-bemolle-4,g-4,a-bemolle-4,a-4,b-4,h-4,c-3,d-bemolle-3,d-3,e-bemolle-3,e-3,f-3,g-bemolle-3,g-3,a-bemolle-3,a-3,b-3,h-3,c-2,d-bemolle-2,d-2,e-bemolle-2,e-2,f-2,g-bemolle-2,g-2,a-bemolle-2,a-2,b-2,h-2,c-,d-bemolle-,d-,e-bemolle-,e-,f-,g-bemolle-,g-,a-bemolle-,a-,b-,h-,c,d-bemolle,d,e-bemolle,e,f,g-bemolle,g,a-bemolle,a,b,h,c+,d-bemolle+,d+,e-bemolle+,e+,f+,g-bemolle+,g+,a-bemolle+,a+,b+,h+,c+2,d-bemolle+2,d+2,e-bemolle+2,e+2,f+2,g-bemolle+2,g+2,a-bemolle+2,a+2,b+2,h+2,c+3,d-bemolle+3,d+3,e-bemolle+3,e+3,f+3,g-bemolle+3,g+3,a-bemolle+3,a+3,b+3,h+3,c+4,d-bemolle+4,d+4,e-bemolle+4,e+4,f+4,g-bemolle+4,g+4,a-bemolle+4,a+4,b+4,h+4,c+5,d-bemolle+5,d+5,e-bemolle+5,e+5,f+5,g-bemolle+5,g+5\n"
			+ "cbx_note_id_german_lc/cbx_halftone_id_bemolle/cbx_halftone_id_bemolle:\n"
			+ "c-----,d-bemolle-----,d-----,e-bemolle-----,e-----,f-----,g-bemolle-----,g-----,a-bemolle-----,a-----,b-----,h-----,c----,d-bemolle----,d----,e-bemolle----,e----,f----,g-bemolle----,g----,a-bemolle----,a----,b----,h----,c---,d-bemolle---,d---,e-bemolle---,e---,f---,g-bemolle---,g---,a-bemolle---,a---,b---,h---,c--,d-bemolle--,d--,e-bemolle--,e--,f--,g-bemolle--,g--,a-bemolle--,a--,b--,h--,c-,d-bemolle-,d-,e-bemolle-,e-,f-,g-bemolle-,g-,a-bemolle-,a-,b-,h-,c,d-bemolle,d,e-bemolle,e,f,g-bemolle,g,a-bemolle,a,b,h,c+,d-bemolle+,d+,e-bemolle+,e+,f+,g-bemolle+,g+,a-bemolle+,a+,b+,h+,c++,d-bemolle++,d++,e-bemolle++,e++,f++,g-bemolle++,g++,a-bemolle++,a++,b++,h++,c+++,d-bemolle+++,d+++,e-bemolle+++,e+++,f+++,g-bemolle+++,g+++,a-bemolle+++,a+++,b+++,h+++,c++++,d-bemolle++++,d++++,e-bemolle++++,e++++,f++++,g-bemolle++++,g++++,a-bemolle++++,a++++,b++++,h++++,c+++++,d-bemolle+++++,d+++++,e-bemolle+++++,e+++++,f+++++,g-bemolle+++++,g+++++\n"
			+ "cbx_note_id_german_lc/cbx_halftone_id_bemolle/cbx_halftone_id_bemolle:\n"
			+ "c-1,d-bemolle-1,d-1,e-bemolle-1,e-1,f-1,g-bemolle-1,g-1,a-bemolle-1,a-1,b-1,h-1,c0,d-bemolle0,d0,e-bemolle0,e0,f0,g-bemolle0,g0,a-bemolle0,a0,b0,h0,c1,d-bemolle1,d1,e-bemolle1,e1,f1,g-bemolle1,g1,a-bemolle1,a1,b1,h1,c2,d-bemolle2,d2,e-bemolle2,e2,f2,g-bemolle2,g2,a-bemolle2,a2,b2,h2,c3,d-bemolle3,d3,e-bemolle3,e3,f3,g-bemolle3,g3,a-bemolle3,a3,b3,h3,c4,d-bemolle4,d4,e-bemolle4,e4,f4,g-bemolle4,g4,a-bemolle4,a4,b4,h4,c5,d-bemolle5,d5,e-bemolle5,e5,f5,g-bemolle5,g5,a-bemolle5,a5,b5,h5,c6,d-bemolle6,d6,e-bemolle6,e6,f6,g-bemolle6,g6,a-bemolle6,a6,b6,h6,c7,d-bemolle7,d7,e-bemolle7,e7,f7,g-bemolle7,g7,a-bemolle7,a7,b7,h7,c8,d-bemolle8,d8,e-bemolle8,e8,f8,g-bemolle8,g8,a-bemolle8,a8,b8,h8,c9,d-bemolle9,d9,e-bemolle9,e9,f9,g-bemolle9,g9\n"
			+ "cbx_note_id_german_lc/cbx_halftone_id_bemolle/cbx_halftone_id_bemolle:\n"
			+ "C''',D-bemolle''',D''',E-bemolle''',E''',F''',G-bemolle''',G''',A-bemolle''',A''',B''',H''',C'',D-bemolle'',D'',E-bemolle'',E'',F'',G-bemolle'',G'',A-bemolle'',A'',B'',H'',C',D-bemolle',D',E-bemolle',E',F',G-bemolle',G',A-bemolle',A',B',H',C,D-bemolle,D,E-bemolle,E,F,G-bemolle,G,A-bemolle,A,B,H,c,d-bemolle,d,e-bemolle,e,f,g-bemolle,g,a-bemolle,a,b,h,c',d-bemolle',d',e-bemolle',e',f',g-bemolle',g',a-bemolle',a',b',h',c'',d-bemolle'',d'',e-bemolle'',e'',f'',g-bemolle'',g'',a-bemolle'',a'',b'',h'',c''',d-bemolle''',d''',e-bemolle''',e''',f''',g-bemolle''',g''',a-bemolle''',a''',b''',h''',c'''',d-bemolle'''',d'''',e-bemolle'''',e'''',f'''',g-bemolle'''',g'''',a-bemolle'''',a'''',b'''',h'''',c''''',d-bemolle''''',d''''',e-bemolle''''',e''''',f''''',g-bemolle''''',g''''',a-bemolle''''',a''''',b''''',h''''',c'''''',d-bemolle'''''',d'''''',e-bemolle'''''',e'''''',f'''''',g-bemolle'''''',g''''''\n"
			+ "cbx_note_id_german_lc/cbx_halftone_id_cis/cbx_halftone_id_cis:\n"
			+ "c-5,cis-5,d-5,dis-5,e-5,f-5,fis-5,g-5,gis-5,a-5,b-5,h-5,c-4,cis-4,d-4,dis-4,e-4,f-4,fis-4,g-4,gis-4,a-4,b-4,h-4,c-3,cis-3,d-3,dis-3,e-3,f-3,fis-3,g-3,gis-3,a-3,b-3,h-3,c-2,cis-2,d-2,dis-2,e-2,f-2,fis-2,g-2,gis-2,a-2,b-2,h-2,c-,cis-,d-,dis-,e-,f-,fis-,g-,gis-,a-,b-,h-,c,cis,d,dis,e,f,fis,g,gis,a,b,h,c+,cis+,d+,dis+,e+,f+,fis+,g+,gis+,a+,b+,h+,c+2,cis+2,d+2,dis+2,e+2,f+2,fis+2,g+2,gis+2,a+2,b+2,h+2,c+3,cis+3,d+3,dis+3,e+3,f+3,fis+3,g+3,gis+3,a+3,b+3,h+3,c+4,cis+4,d+4,dis+4,e+4,f+4,fis+4,g+4,gis+4,a+4,b+4,h+4,c+5,cis+5,d+5,dis+5,e+5,f+5,fis+5,g+5\n"
			+ "cbx_note_id_german_lc/cbx_halftone_id_cis/cbx_halftone_id_cis:\n"
			+ "c-----,cis-----,d-----,dis-----,e-----,f-----,fis-----,g-----,gis-----,a-----,b-----,h-----,c----,cis----,d----,dis----,e----,f----,fis----,g----,gis----,a----,b----,h----,c---,cis---,d---,dis---,e---,f---,fis---,g---,gis---,a---,b---,h---,c--,cis--,d--,dis--,e--,f--,fis--,g--,gis--,a--,b--,h--,c-,cis-,d-,dis-,e-,f-,fis-,g-,gis-,a-,b-,h-,c,cis,d,dis,e,f,fis,g,gis,a,b,h,c+,cis+,d+,dis+,e+,f+,fis+,g+,gis+,a+,b+,h+,c++,cis++,d++,dis++,e++,f++,fis++,g++,gis++,a++,b++,h++,c+++,cis+++,d+++,dis+++,e+++,f+++,fis+++,g+++,gis+++,a+++,b+++,h+++,c++++,cis++++,d++++,dis++++,e++++,f++++,fis++++,g++++,gis++++,a++++,b++++,h++++,c+++++,cis+++++,d+++++,dis+++++,e+++++,f+++++,fis+++++,g+++++\n"
			+ "cbx_note_id_german_lc/cbx_halftone_id_cis/cbx_halftone_id_cis:\n"
			+ "c-1,cis-1,d-1,dis-1,e-1,f-1,fis-1,g-1,gis-1,a-1,b-1,h-1,c0,cis0,d0,dis0,e0,f0,fis0,g0,gis0,a0,b0,h0,c1,cis1,d1,dis1,e1,f1,fis1,g1,gis1,a1,b1,h1,c2,cis2,d2,dis2,e2,f2,fis2,g2,gis2,a2,b2,h2,c3,cis3,d3,dis3,e3,f3,fis3,g3,gis3,a3,b3,h3,c4,cis4,d4,dis4,e4,f4,fis4,g4,gis4,a4,b4,h4,c5,cis5,d5,dis5,e5,f5,fis5,g5,gis5,a5,b5,h5,c6,cis6,d6,dis6,e6,f6,fis6,g6,gis6,a6,b6,h6,c7,cis7,d7,dis7,e7,f7,fis7,g7,gis7,a7,b7,h7,c8,cis8,d8,dis8,e8,f8,fis8,g8,gis8,a8,b8,h8,c9,cis9,d9,dis9,e9,f9,fis9,g9\n"
			+ "cbx_note_id_german_lc/cbx_halftone_id_cis/cbx_halftone_id_cis:\n"
			+ "C''',Cis''',D''',Dis''',E''',F''',Fis''',G''',Gis''',A''',B''',H''',C'',Cis'',D'',Dis'',E'',F'',Fis'',G'',Gis'',A'',B'',H'',C',Cis',D',Dis',E',F',Fis',G',Gis',A',B',H',C,Cis,D,Dis,E,F,Fis,G,Gis,A,B,H,c,cis,d,dis,e,f,fis,g,gis,a,b,h,c',cis',d',dis',e',f',fis',g',gis',a',b',h',c'',cis'',d'',dis'',e'',f'',fis'',g'',gis'',a'',b'',h'',c''',cis''',d''',dis''',e''',f''',fis''',g''',gis''',a''',b''',h''',c'''',cis'''',d'''',dis'''',e'''',f'''',fis'''',g'''',gis'''',a'''',b'''',h'''',c''''',cis''''',d''''',dis''''',e''''',f''''',fis''''',g''''',gis''''',a''''',b''''',h''''',c'''''',cis'''''',d'''''',dis'''''',e'''''',f'''''',fis'''''',g''''''\n"
			+ "cbx_note_id_german_lc/cbx_halftone_id_des/cbx_halftone_id_des:\n"
			+ "c-5,des-5,d-5,es-5,e-5,f-5,ges-5,g-5,as-5,a-5,b-5,h-5,c-4,des-4,d-4,es-4,e-4,f-4,ges-4,g-4,as-4,a-4,b-4,h-4,c-3,des-3,d-3,es-3,e-3,f-3,ges-3,g-3,as-3,a-3,b-3,h-3,c-2,des-2,d-2,es-2,e-2,f-2,ges-2,g-2,as-2,a-2,b-2,h-2,c-,des-,d-,es-,e-,f-,ges-,g-,as-,a-,b-,h-,c,des,d,es,e,f,ges,g,as,a,b,h,c+,des+,d+,es+,e+,f+,ges+,g+,as+,a+,b+,h+,c+2,des+2,d+2,es+2,e+2,f+2,ges+2,g+2,as+2,a+2,b+2,h+2,c+3,des+3,d+3,es+3,e+3,f+3,ges+3,g+3,as+3,a+3,b+3,h+3,c+4,des+4,d+4,es+4,e+4,f+4,ges+4,g+4,as+4,a+4,b+4,h+4,c+5,des+5,d+5,es+5,e+5,f+5,ges+5,g+5\n"
			+ "cbx_note_id_german_lc/cbx_halftone_id_des/cbx_halftone_id_des:\n"
			+ "c-----,des-----,d-----,es-----,e-----,f-----,ges-----,g-----,as-----,a-----,b-----,h-----,c----,des----,d----,es----,e----,f----,ges----,g----,as----,a----,b----,h----,c---,des---,d---,es---,e---,f---,ges---,g---,as---,a---,b---,h---,c--,des--,d--,es--,e--,f--,ges--,g--,as--,a--,b--,h--,c-,des-,d-,es-,e-,f-,ges-,g-,as-,a-,b-,h-,c,des,d,es,e,f,ges,g,as,a,b,h,c+,des+,d+,es+,e+,f+,ges+,g+,as+,a+,b+,h+,c++,des++,d++,es++,e++,f++,ges++,g++,as++,a++,b++,h++,c+++,des+++,d+++,es+++,e+++,f+++,ges+++,g+++,as+++,a+++,b+++,h+++,c++++,des++++,d++++,es++++,e++++,f++++,ges++++,g++++,as++++,a++++,b++++,h++++,c+++++,des+++++,d+++++,es+++++,e+++++,f+++++,ges+++++,g+++++\n"
			+ "cbx_note_id_german_lc/cbx_halftone_id_des/cbx_halftone_id_des:\n"
			+ "c-1,des-1,d-1,es-1,e-1,f-1,ges-1,g-1,as-1,a-1,b-1,h-1,c0,des0,d0,es0,e0,f0,ges0,g0,as0,a0,b0,h0,c1,des1,d1,es1,e1,f1,ges1,g1,as1,a1,b1,h1,c2,des2,d2,es2,e2,f2,ges2,g2,as2,a2,b2,h2,c3,des3,d3,es3,e3,f3,ges3,g3,as3,a3,b3,h3,c4,des4,d4,es4,e4,f4,ges4,g4,as4,a4,b4,h4,c5,des5,d5,es5,e5,f5,ges5,g5,as5,a5,b5,h5,c6,des6,d6,es6,e6,f6,ges6,g6,as6,a6,b6,h6,c7,des7,d7,es7,e7,f7,ges7,g7,as7,a7,b7,h7,c8,des8,d8,es8,e8,f8,ges8,g8,as8,a8,b8,h8,c9,des9,d9,es9,e9,f9,ges9,g9\n"
			+ "cbx_note_id_german_lc/cbx_halftone_id_des/cbx_halftone_id_des:\n"
			+ "C''',Des''',D''',Es''',E''',F''',Ges''',G''',As''',A''',B''',H''',C'',Des'',D'',Es'',E'',F'',Ges'',G'',As'',A'',B'',H'',C',Des',D',Es',E',F',Ges',G',As',A',B',H',C,Des,D,Es,E,F,Ges,G,As,A,B,H,c,des,d,es,e,f,ges,g,as,a,b,h,c',des',d',es',e',f',ges',g',as',a',b',h',c'',des'',d'',es'',e'',f'',ges'',g'',as'',a'',b'',h'',c''',des''',d''',es''',e''',f''',ges''',g''',as''',a''',b''',h''',c'''',des'''',d'''',es'''',e'''',f'''',ges'''',g'''',as'''',a'''',b'''',h'''',c''''',des''''',d''''',es''''',e''''',f''''',ges''''',g''''',as''''',a''''',b''''',h''''',c'''''',des'''''',d'''''',es'''''',e'''''',f'''''',ges'''''',g''''''\n"
			+ "cbx_note_id_german_uc/cbx_halftone_id_sharp/cbx_halftone_id_sharp:\n"
			+ "C-5,C#-5,D-5,D#-5,E-5,F-5,F#-5,G-5,G#-5,A-5,B-5,H-5,C-4,C#-4,D-4,D#-4,E-4,F-4,F#-4,G-4,G#-4,A-4,B-4,H-4,C-3,C#-3,D-3,D#-3,E-3,F-3,F#-3,G-3,G#-3,A-3,B-3,H-3,C-2,C#-2,D-2,D#-2,E-2,F-2,F#-2,G-2,G#-2,A-2,B-2,H-2,C-,C#-,D-,D#-,E-,F-,F#-,G-,G#-,A-,B-,H-,C,C#,D,D#,E,F,F#,G,G#,A,B,H,C+,C#+,D+,D#+,E+,F+,F#+,G+,G#+,A+,B+,H+,C+2,C#+2,D+2,D#+2,E+2,F+2,F#+2,G+2,G#+2,A+2,B+2,H+2,C+3,C#+3,D+3,D#+3,E+3,F+3,F#+3,G+3,G#+3,A+3,B+3,H+3,C+4,C#+4,D+4,D#+4,E+4,F+4,F#+4,G+4,G#+4,A+4,B+4,H+4,C+5,C#+5,D+5,D#+5,E+5,F+5,F#+5,G+5\n"
			+ "cbx_note_id_german_uc/cbx_halftone_id_sharp/cbx_halftone_id_sharp:\n"
			+ "C-----,C#-----,D-----,D#-----,E-----,F-----,F#-----,G-----,G#-----,A-----,B-----,H-----,C----,C#----,D----,D#----,E----,F----,F#----,G----,G#----,A----,B----,H----,C---,C#---,D---,D#---,E---,F---,F#---,G---,G#---,A---,B---,H---,C--,C#--,D--,D#--,E--,F--,F#--,G--,G#--,A--,B--,H--,C-,C#-,D-,D#-,E-,F-,F#-,G-,G#-,A-,B-,H-,C,C#,D,D#,E,F,F#,G,G#,A,B,H,C+,C#+,D+,D#+,E+,F+,F#+,G+,G#+,A+,B+,H+,C++,C#++,D++,D#++,E++,F++,F#++,G++,G#++,A++,B++,H++,C+++,C#+++,D+++,D#+++,E+++,F+++,F#+++,G+++,G#+++,A+++,B+++,H+++,C++++,C#++++,D++++,D#++++,E++++,F++++,F#++++,G++++,G#++++,A++++,B++++,H++++,C+++++,C#+++++,D+++++,D#+++++,E+++++,F+++++,F#+++++,G+++++\n"
			+ "cbx_note_id_german_uc/cbx_halftone_id_sharp/cbx_halftone_id_sharp:\n"
			+ "C-1,C#-1,D-1,D#-1,E-1,F-1,F#-1,G-1,G#-1,A-1,B-1,H-1,C0,C#0,D0,D#0,E0,F0,F#0,G0,G#0,A0,B0,H0,C1,C#1,D1,D#1,E1,F1,F#1,G1,G#1,A1,B1,H1,C2,C#2,D2,D#2,E2,F2,F#2,G2,G#2,A2,B2,H2,C3,C#3,D3,D#3,E3,F3,F#3,G3,G#3,A3,B3,H3,C4,C#4,D4,D#4,E4,F4,F#4,G4,G#4,A4,B4,H4,C5,C#5,D5,D#5,E5,F5,F#5,G5,G#5,A5,B5,H5,C6,C#6,D6,D#6,E6,F6,F#6,G6,G#6,A6,B6,H6,C7,C#7,D7,D#7,E7,F7,F#7,G7,G#7,A7,B7,H7,C8,C#8,D8,D#8,E8,F8,F#8,G8,G#8,A8,B8,H8,C9,C#9,D9,D#9,E9,F9,F#9,G9\n"
			+ "cbx_note_id_german_uc/cbx_halftone_id_sharp/cbx_halftone_id_sharp:\n"
			+ "C''',C#''',D''',D#''',E''',F''',F#''',G''',G#''',A''',B''',H''',C'',C#'',D'',D#'',E'',F'',F#'',G'',G#'',A'',B'',H'',C',C#',D',D#',E',F',F#',G',G#',A',B',H',C,C#,D,D#,E,F,F#,G,G#,A,B,H,c,c#,d,d#,e,f,f#,g,g#,a,b,h,c',c#',d',d#',e',f',f#',g',g#',a',b',h',c'',c#'',d'',d#'',e'',f'',f#'',g'',g#'',a'',b'',h'',c''',c#''',d''',d#''',e''',f''',f#''',g''',g#''',a''',b''',h''',c'''',c#'''',d'''',d#'''',e'''',f'''',f#'''',g'''',g#'''',a'''',b'''',h'''',c''''',c#''''',d''''',d#''''',e''''',f''''',f#''''',g''''',g#''''',a''''',b''''',h''''',c'''''',c#'''''',d'''''',d#'''''',e'''''',f'''''',f#'''''',g''''''\n"
			+ "cbx_note_id_german_uc/cbx_halftone_id_flat/cbx_halftone_id_flat:\n"
			+ "C-5,Db-5,D-5,Eb-5,E-5,F-5,Gb-5,G-5,Ab-5,A-5,B-5,H-5,C-4,Db-4,D-4,Eb-4,E-4,F-4,Gb-4,G-4,Ab-4,A-4,B-4,H-4,C-3,Db-3,D-3,Eb-3,E-3,F-3,Gb-3,G-3,Ab-3,A-3,B-3,H-3,C-2,Db-2,D-2,Eb-2,E-2,F-2,Gb-2,G-2,Ab-2,A-2,B-2,H-2,C-,Db-,D-,Eb-,E-,F-,Gb-,G-,Ab-,A-,B-,H-,C,Db,D,Eb,E,F,Gb,G,Ab,A,B,H,C+,Db+,D+,Eb+,E+,F+,Gb+,G+,Ab+,A+,B+,H+,C+2,Db+2,D+2,Eb+2,E+2,F+2,Gb+2,G+2,Ab+2,A+2,B+2,H+2,C+3,Db+3,D+3,Eb+3,E+3,F+3,Gb+3,G+3,Ab+3,A+3,B+3,H+3,C+4,Db+4,D+4,Eb+4,E+4,F+4,Gb+4,G+4,Ab+4,A+4,B+4,H+4,C+5,Db+5,D+5,Eb+5,E+5,F+5,Gb+5,G+5\n"
			+ "cbx_note_id_german_uc/cbx_halftone_id_flat/cbx_halftone_id_flat:\n"
			+ "C-----,Db-----,D-----,Eb-----,E-----,F-----,Gb-----,G-----,Ab-----,A-----,B-----,H-----,C----,Db----,D----,Eb----,E----,F----,Gb----,G----,Ab----,A----,B----,H----,C---,Db---,D---,Eb---,E---,F---,Gb---,G---,Ab---,A---,B---,H---,C--,Db--,D--,Eb--,E--,F--,Gb--,G--,Ab--,A--,B--,H--,C-,Db-,D-,Eb-,E-,F-,Gb-,G-,Ab-,A-,B-,H-,C,Db,D,Eb,E,F,Gb,G,Ab,A,B,H,C+,Db+,D+,Eb+,E+,F+,Gb+,G+,Ab+,A+,B+,H+,C++,Db++,D++,Eb++,E++,F++,Gb++,G++,Ab++,A++,B++,H++,C+++,Db+++,D+++,Eb+++,E+++,F+++,Gb+++,G+++,Ab+++,A+++,B+++,H+++,C++++,Db++++,D++++,Eb++++,E++++,F++++,Gb++++,G++++,Ab++++,A++++,B++++,H++++,C+++++,Db+++++,D+++++,Eb+++++,E+++++,F+++++,Gb+++++,G+++++\n"
			+ "cbx_note_id_german_uc/cbx_halftone_id_flat/cbx_halftone_id_flat:\n"
			+ "C-1,Db-1,D-1,Eb-1,E-1,F-1,Gb-1,G-1,Ab-1,A-1,B-1,H-1,C0,Db0,D0,Eb0,E0,F0,Gb0,G0,Ab0,A0,B0,H0,C1,Db1,D1,Eb1,E1,F1,Gb1,G1,Ab1,A1,B1,H1,C2,Db2,D2,Eb2,E2,F2,Gb2,G2,Ab2,A2,B2,H2,C3,Db3,D3,Eb3,E3,F3,Gb3,G3,Ab3,A3,B3,H3,C4,Db4,D4,Eb4,E4,F4,Gb4,G4,Ab4,A4,B4,H4,C5,Db5,D5,Eb5,E5,F5,Gb5,G5,Ab5,A5,B5,H5,C6,Db6,D6,Eb6,E6,F6,Gb6,G6,Ab6,A6,B6,H6,C7,Db7,D7,Eb7,E7,F7,Gb7,G7,Ab7,A7,B7,H7,C8,Db8,D8,Eb8,E8,F8,Gb8,G8,Ab8,A8,B8,H8,C9,Db9,D9,Eb9,E9,F9,Gb9,G9\n"
			+ "cbx_note_id_german_uc/cbx_halftone_id_flat/cbx_halftone_id_flat:\n"
			+ "C''',Db''',D''',Eb''',E''',F''',Gb''',G''',Ab''',A''',B''',H''',C'',Db'',D'',Eb'',E'',F'',Gb'',G'',Ab'',A'',B'',H'',C',Db',D',Eb',E',F',Gb',G',Ab',A',B',H',C,Db,D,Eb,E,F,Gb,G,Ab,A,B,H,c,db,d,eb,e,f,gb,g,ab,a,b,h,c',db',d',eb',e',f',gb',g',ab',a',b',h',c'',db'',d'',eb'',e'',f'',gb'',g'',ab'',a'',b'',h'',c''',db''',d''',eb''',e''',f''',gb''',g''',ab''',a''',b''',h''',c'''',db'''',d'''',eb'''',e'''',f'''',gb'''',g'''',ab'''',a'''',b'''',h'''',c''''',db''''',d''''',eb''''',e''''',f''''',gb''''',g''''',ab''''',a''''',b''''',h''''',c'''''',db'''''',d'''''',eb'''''',e'''''',f'''''',gb'''''',g''''''\n"
			+ "cbx_note_id_german_uc/cbx_halftone_id_diesis/cbx_halftone_id_diesis:\n"
			+ "C-5,C-diesis-5,D-5,D-diesis-5,E-5,F-5,F-diesis-5,G-5,G-diesis-5,A-5,B-5,H-5,C-4,C-diesis-4,D-4,D-diesis-4,E-4,F-4,F-diesis-4,G-4,G-diesis-4,A-4,B-4,H-4,C-3,C-diesis-3,D-3,D-diesis-3,E-3,F-3,F-diesis-3,G-3,G-diesis-3,A-3,B-3,H-3,C-2,C-diesis-2,D-2,D-diesis-2,E-2,F-2,F-diesis-2,G-2,G-diesis-2,A-2,B-2,H-2,C-,C-diesis-,D-,D-diesis-,E-,F-,F-diesis-,G-,G-diesis-,A-,B-,H-,C,C-diesis,D,D-diesis,E,F,F-diesis,G,G-diesis,A,B,H,C+,C-diesis+,D+,D-diesis+,E+,F+,F-diesis+,G+,G-diesis+,A+,B+,H+,C+2,C-diesis+2,D+2,D-diesis+2,E+2,F+2,F-diesis+2,G+2,G-diesis+2,A+2,B+2,H+2,C+3,C-diesis+3,D+3,D-diesis+3,E+3,F+3,F-diesis+3,G+3,G-diesis+3,A+3,B+3,H+3,C+4,C-diesis+4,D+4,D-diesis+4,E+4,F+4,F-diesis+4,G+4,G-diesis+4,A+4,B+4,H+4,C+5,C-diesis+5,D+5,D-diesis+5,E+5,F+5,F-diesis+5,G+5\n"
			+ "cbx_note_id_german_uc/cbx_halftone_id_diesis/cbx_halftone_id_diesis:\n"
			+ "C-----,C-diesis-----,D-----,D-diesis-----,E-----,F-----,F-diesis-----,G-----,G-diesis-----,A-----,B-----,H-----,C----,C-diesis----,D----,D-diesis----,E----,F----,F-diesis----,G----,G-diesis----,A----,B----,H----,C---,C-diesis---,D---,D-diesis---,E---,F---,F-diesis---,G---,G-diesis---,A---,B---,H---,C--,C-diesis--,D--,D-diesis--,E--,F--,F-diesis--,G--,G-diesis--,A--,B--,H--,C-,C-diesis-,D-,D-diesis-,E-,F-,F-diesis-,G-,G-diesis-,A-,B-,H-,C,C-diesis,D,D-diesis,E,F,F-diesis,G,G-diesis,A,B,H,C+,C-diesis+,D+,D-diesis+,E+,F+,F-diesis+,G+,G-diesis+,A+,B+,H+,C++,C-diesis++,D++,D-diesis++,E++,F++,F-diesis++,G++,G-diesis++,A++,B++,H++,C+++,C-diesis+++,D+++,D-diesis+++,E+++,F+++,F-diesis+++,G+++,G-diesis+++,A+++,B+++,H+++,C++++,C-diesis++++,D++++,D-diesis++++,E++++,F++++,F-diesis++++,G++++,G-diesis++++,A++++,B++++,H++++,C+++++,C-diesis+++++,D+++++,D-diesis+++++,E+++++,F+++++,F-diesis+++++,G+++++\n"
			+ "cbx_note_id_german_uc/cbx_halftone_id_diesis/cbx_halftone_id_diesis:\n"
			+ "C-1,C-diesis-1,D-1,D-diesis-1,E-1,F-1,F-diesis-1,G-1,G-diesis-1,A-1,B-1,H-1,C0,C-diesis0,D0,D-diesis0,E0,F0,F-diesis0,G0,G-diesis0,A0,B0,H0,C1,C-diesis1,D1,D-diesis1,E1,F1,F-diesis1,G1,G-diesis1,A1,B1,H1,C2,C-diesis2,D2,D-diesis2,E2,F2,F-diesis2,G2,G-diesis2,A2,B2,H2,C3,C-diesis3,D3,D-diesis3,E3,F3,F-diesis3,G3,G-diesis3,A3,B3,H3,C4,C-diesis4,D4,D-diesis4,E4,F4,F-diesis4,G4,G-diesis4,A4,B4,H4,C5,C-diesis5,D5,D-diesis5,E5,F5,F-diesis5,G5,G-diesis5,A5,B5,H5,C6,C-diesis6,D6,D-diesis6,E6,F6,F-diesis6,G6,G-diesis6,A6,B6,H6,C7,C-diesis7,D7,D-diesis7,E7,F7,F-diesis7,G7,G-diesis7,A7,B7,H7,C8,C-diesis8,D8,D-diesis8,E8,F8,F-diesis8,G8,G-diesis8,A8,B8,H8,C9,C-diesis9,D9,D-diesis9,E9,F9,F-diesis9,G9\n"
			+ "cbx_note_id_german_uc/cbx_halftone_id_diesis/cbx_halftone_id_diesis:\n"
			+ "C''',C-diesis''',D''',D-diesis''',E''',F''',F-diesis''',G''',G-diesis''',A''',B''',H''',C'',C-diesis'',D'',D-diesis'',E'',F'',F-diesis'',G'',G-diesis'',A'',B'',H'',C',C-diesis',D',D-diesis',E',F',F-diesis',G',G-diesis',A',B',H',C,C-diesis,D,D-diesis,E,F,F-diesis,G,G-diesis,A,B,H,c,c-diesis,d,d-diesis,e,f,f-diesis,g,g-diesis,a,b,h,c',c-diesis',d',d-diesis',e',f',f-diesis',g',g-diesis',a',b',h',c'',c-diesis'',d'',d-diesis'',e'',f'',f-diesis'',g'',g-diesis'',a'',b'',h'',c''',c-diesis''',d''',d-diesis''',e''',f''',f-diesis''',g''',g-diesis''',a''',b''',h''',c'''',c-diesis'''',d'''',d-diesis'''',e'''',f'''',f-diesis'''',g'''',g-diesis'''',a'''',b'''',h'''',c''''',c-diesis''''',d''''',d-diesis''''',e''''',f''''',f-diesis''''',g''''',g-diesis''''',a''''',b''''',h''''',c'''''',c-diesis'''''',d'''''',d-diesis'''''',e'''''',f'''''',f-diesis'''''',g''''''\n"
			+ "cbx_note_id_german_uc/cbx_halftone_id_bemolle/cbx_halftone_id_bemolle:\n"
			+ "C-5,D-bemolle-5,D-5,E-bemolle-5,E-5,F-5,G-bemolle-5,G-5,A-bemolle-5,A-5,B-5,H-5,C-4,D-bemolle-4,D-4,E-bemolle-4,E-4,F-4,G-bemolle-4,G-4,A-bemolle-4,A-4,B-4,H-4,C-3,D-bemolle-3,D-3,E-bemolle-3,E-3,F-3,G-bemolle-3,G-3,A-bemolle-3,A-3,B-3,H-3,C-2,D-bemolle-2,D-2,E-bemolle-2,E-2,F-2,G-bemolle-2,G-2,A-bemolle-2,A-2,B-2,H-2,C-,D-bemolle-,D-,E-bemolle-,E-,F-,G-bemolle-,G-,A-bemolle-,A-,B-,H-,C,D-bemolle,D,E-bemolle,E,F,G-bemolle,G,A-bemolle,A,B,H,C+,D-bemolle+,D+,E-bemolle+,E+,F+,G-bemolle+,G+,A-bemolle+,A+,B+,H+,C+2,D-bemolle+2,D+2,E-bemolle+2,E+2,F+2,G-bemolle+2,G+2,A-bemolle+2,A+2,B+2,H+2,C+3,D-bemolle+3,D+3,E-bemolle+3,E+3,F+3,G-bemolle+3,G+3,A-bemolle+3,A+3,B+3,H+3,C+4,D-bemolle+4,D+4,E-bemolle+4,E+4,F+4,G-bemolle+4,G+4,A-bemolle+4,A+4,B+4,H+4,C+5,D-bemolle+5,D+5,E-bemolle+5,E+5,F+5,G-bemolle+5,G+5\n"
			+ "cbx_note_id_german_uc/cbx_halftone_id_bemolle/cbx_halftone_id_bemolle:\n"
			+ "C-----,D-bemolle-----,D-----,E-bemolle-----,E-----,F-----,G-bemolle-----,G-----,A-bemolle-----,A-----,B-----,H-----,C----,D-bemolle----,D----,E-bemolle----,E----,F----,G-bemolle----,G----,A-bemolle----,A----,B----,H----,C---,D-bemolle---,D---,E-bemolle---,E---,F---,G-bemolle---,G---,A-bemolle---,A---,B---,H---,C--,D-bemolle--,D--,E-bemolle--,E--,F--,G-bemolle--,G--,A-bemolle--,A--,B--,H--,C-,D-bemolle-,D-,E-bemolle-,E-,F-,G-bemolle-,G-,A-bemolle-,A-,B-,H-,C,D-bemolle,D,E-bemolle,E,F,G-bemolle,G,A-bemolle,A,B,H,C+,D-bemolle+,D+,E-bemolle+,E+,F+,G-bemolle+,G+,A-bemolle+,A+,B+,H+,C++,D-bemolle++,D++,E-bemolle++,E++,F++,G-bemolle++,G++,A-bemolle++,A++,B++,H++,C+++,D-bemolle+++,D+++,E-bemolle+++,E+++,F+++,G-bemolle+++,G+++,A-bemolle+++,A+++,B+++,H+++,C++++,D-bemolle++++,D++++,E-bemolle++++,E++++,F++++,G-bemolle++++,G++++,A-bemolle++++,A++++,B++++,H++++,C+++++,D-bemolle+++++,D+++++,E-bemolle+++++,E+++++,F+++++,G-bemolle+++++,G+++++\n"
			+ "cbx_note_id_german_uc/cbx_halftone_id_bemolle/cbx_halftone_id_bemolle:\n"
			+ "C-1,D-bemolle-1,D-1,E-bemolle-1,E-1,F-1,G-bemolle-1,G-1,A-bemolle-1,A-1,B-1,H-1,C0,D-bemolle0,D0,E-bemolle0,E0,F0,G-bemolle0,G0,A-bemolle0,A0,B0,H0,C1,D-bemolle1,D1,E-bemolle1,E1,F1,G-bemolle1,G1,A-bemolle1,A1,B1,H1,C2,D-bemolle2,D2,E-bemolle2,E2,F2,G-bemolle2,G2,A-bemolle2,A2,B2,H2,C3,D-bemolle3,D3,E-bemolle3,E3,F3,G-bemolle3,G3,A-bemolle3,A3,B3,H3,C4,D-bemolle4,D4,E-bemolle4,E4,F4,G-bemolle4,G4,A-bemolle4,A4,B4,H4,C5,D-bemolle5,D5,E-bemolle5,E5,F5,G-bemolle5,G5,A-bemolle5,A5,B5,H5,C6,D-bemolle6,D6,E-bemolle6,E6,F6,G-bemolle6,G6,A-bemolle6,A6,B6,H6,C7,D-bemolle7,D7,E-bemolle7,E7,F7,G-bemolle7,G7,A-bemolle7,A7,B7,H7,C8,D-bemolle8,D8,E-bemolle8,E8,F8,G-bemolle8,G8,A-bemolle8,A8,B8,H8,C9,D-bemolle9,D9,E-bemolle9,E9,F9,G-bemolle9,G9\n"
			+ "cbx_note_id_german_uc/cbx_halftone_id_bemolle/cbx_halftone_id_bemolle:\n"
			+ "C''',D-bemolle''',D''',E-bemolle''',E''',F''',G-bemolle''',G''',A-bemolle''',A''',B''',H''',C'',D-bemolle'',D'',E-bemolle'',E'',F'',G-bemolle'',G'',A-bemolle'',A'',B'',H'',C',D-bemolle',D',E-bemolle',E',F',G-bemolle',G',A-bemolle',A',B',H',C,D-bemolle,D,E-bemolle,E,F,G-bemolle,G,A-bemolle,A,B,H,c,d-bemolle,d,e-bemolle,e,f,g-bemolle,g,a-bemolle,a,b,h,c',d-bemolle',d',e-bemolle',e',f',g-bemolle',g',a-bemolle',a',b',h',c'',d-bemolle'',d'',e-bemolle'',e'',f'',g-bemolle'',g'',a-bemolle'',a'',b'',h'',c''',d-bemolle''',d''',e-bemolle''',e''',f''',g-bemolle''',g''',a-bemolle''',a''',b''',h''',c'''',d-bemolle'''',d'''',e-bemolle'''',e'''',f'''',g-bemolle'''',g'''',a-bemolle'''',a'''',b'''',h'''',c''''',d-bemolle''''',d''''',e-bemolle''''',e''''',f''''',g-bemolle''''',g''''',a-bemolle''''',a''''',b''''',h''''',c'''''',d-bemolle'''''',d'''''',e-bemolle'''''',e'''''',f'''''',g-bemolle'''''',g''''''\n"
			+ "cbx_note_id_german_uc/cbx_halftone_id_cis/cbx_halftone_id_cis:\n"
			+ "C-5,Cis-5,D-5,Dis-5,E-5,F-5,Fis-5,G-5,Gis-5,A-5,B-5,H-5,C-4,Cis-4,D-4,Dis-4,E-4,F-4,Fis-4,G-4,Gis-4,A-4,B-4,H-4,C-3,Cis-3,D-3,Dis-3,E-3,F-3,Fis-3,G-3,Gis-3,A-3,B-3,H-3,C-2,Cis-2,D-2,Dis-2,E-2,F-2,Fis-2,G-2,Gis-2,A-2,B-2,H-2,C-,Cis-,D-,Dis-,E-,F-,Fis-,G-,Gis-,A-,B-,H-,C,Cis,D,Dis,E,F,Fis,G,Gis,A,B,H,C+,Cis+,D+,Dis+,E+,F+,Fis+,G+,Gis+,A+,B+,H+,C+2,Cis+2,D+2,Dis+2,E+2,F+2,Fis+2,G+2,Gis+2,A+2,B+2,H+2,C+3,Cis+3,D+3,Dis+3,E+3,F+3,Fis+3,G+3,Gis+3,A+3,B+3,H+3,C+4,Cis+4,D+4,Dis+4,E+4,F+4,Fis+4,G+4,Gis+4,A+4,B+4,H+4,C+5,Cis+5,D+5,Dis+5,E+5,F+5,Fis+5,G+5\n"
			+ "cbx_note_id_german_uc/cbx_halftone_id_cis/cbx_halftone_id_cis:\n"
			+ "C-----,Cis-----,D-----,Dis-----,E-----,F-----,Fis-----,G-----,Gis-----,A-----,B-----,H-----,C----,Cis----,D----,Dis----,E----,F----,Fis----,G----,Gis----,A----,B----,H----,C---,Cis---,D---,Dis---,E---,F---,Fis---,G---,Gis---,A---,B---,H---,C--,Cis--,D--,Dis--,E--,F--,Fis--,G--,Gis--,A--,B--,H--,C-,Cis-,D-,Dis-,E-,F-,Fis-,G-,Gis-,A-,B-,H-,C,Cis,D,Dis,E,F,Fis,G,Gis,A,B,H,C+,Cis+,D+,Dis+,E+,F+,Fis+,G+,Gis+,A+,B+,H+,C++,Cis++,D++,Dis++,E++,F++,Fis++,G++,Gis++,A++,B++,H++,C+++,Cis+++,D+++,Dis+++,E+++,F+++,Fis+++,G+++,Gis+++,A+++,B+++,H+++,C++++,Cis++++,D++++,Dis++++,E++++,F++++,Fis++++,G++++,Gis++++,A++++,B++++,H++++,C+++++,Cis+++++,D+++++,Dis+++++,E+++++,F+++++,Fis+++++,G+++++\n"
			+ "cbx_note_id_german_uc/cbx_halftone_id_cis/cbx_halftone_id_cis:\n"
			+ "C-1,Cis-1,D-1,Dis-1,E-1,F-1,Fis-1,G-1,Gis-1,A-1,B-1,H-1,C0,Cis0,D0,Dis0,E0,F0,Fis0,G0,Gis0,A0,B0,H0,C1,Cis1,D1,Dis1,E1,F1,Fis1,G1,Gis1,A1,B1,H1,C2,Cis2,D2,Dis2,E2,F2,Fis2,G2,Gis2,A2,B2,H2,C3,Cis3,D3,Dis3,E3,F3,Fis3,G3,Gis3,A3,B3,H3,C4,Cis4,D4,Dis4,E4,F4,Fis4,G4,Gis4,A4,B4,H4,C5,Cis5,D5,Dis5,E5,F5,Fis5,G5,Gis5,A5,B5,H5,C6,Cis6,D6,Dis6,E6,F6,Fis6,G6,Gis6,A6,B6,H6,C7,Cis7,D7,Dis7,E7,F7,Fis7,G7,Gis7,A7,B7,H7,C8,Cis8,D8,Dis8,E8,F8,Fis8,G8,Gis8,A8,B8,H8,C9,Cis9,D9,Dis9,E9,F9,Fis9,G9\n"
			+ "cbx_note_id_german_uc/cbx_halftone_id_cis/cbx_halftone_id_cis:\n"
			+ "C''',Cis''',D''',Dis''',E''',F''',Fis''',G''',Gis''',A''',B''',H''',C'',Cis'',D'',Dis'',E'',F'',Fis'',G'',Gis'',A'',B'',H'',C',Cis',D',Dis',E',F',Fis',G',Gis',A',B',H',C,Cis,D,Dis,E,F,Fis,G,Gis,A,B,H,c,cis,d,dis,e,f,fis,g,gis,a,b,h,c',cis',d',dis',e',f',fis',g',gis',a',b',h',c'',cis'',d'',dis'',e'',f'',fis'',g'',gis'',a'',b'',h'',c''',cis''',d''',dis''',e''',f''',fis''',g''',gis''',a''',b''',h''',c'''',cis'''',d'''',dis'''',e'''',f'''',fis'''',g'''',gis'''',a'''',b'''',h'''',c''''',cis''''',d''''',dis''''',e''''',f''''',fis''''',g''''',gis''''',a''''',b''''',h''''',c'''''',cis'''''',d'''''',dis'''''',e'''''',f'''''',fis'''''',g''''''\n"
			+ "cbx_note_id_german_uc/cbx_halftone_id_des/cbx_halftone_id_des:\n"
			+ "C-5,Des-5,D-5,Es-5,E-5,F-5,Ges-5,G-5,As-5,A-5,B-5,H-5,C-4,Des-4,D-4,Es-4,E-4,F-4,Ges-4,G-4,As-4,A-4,B-4,H-4,C-3,Des-3,D-3,Es-3,E-3,F-3,Ges-3,G-3,As-3,A-3,B-3,H-3,C-2,Des-2,D-2,Es-2,E-2,F-2,Ges-2,G-2,As-2,A-2,B-2,H-2,C-,Des-,D-,Es-,E-,F-,Ges-,G-,As-,A-,B-,H-,C,Des,D,Es,E,F,Ges,G,As,A,B,H,C+,Des+,D+,Es+,E+,F+,Ges+,G+,As+,A+,B+,H+,C+2,Des+2,D+2,Es+2,E+2,F+2,Ges+2,G+2,As+2,A+2,B+2,H+2,C+3,Des+3,D+3,Es+3,E+3,F+3,Ges+3,G+3,As+3,A+3,B+3,H+3,C+4,Des+4,D+4,Es+4,E+4,F+4,Ges+4,G+4,As+4,A+4,B+4,H+4,C+5,Des+5,D+5,Es+5,E+5,F+5,Ges+5,G+5\n"
			+ "cbx_note_id_german_uc/cbx_halftone_id_des/cbx_halftone_id_des:\n"
			+ "C-----,Des-----,D-----,Es-----,E-----,F-----,Ges-----,G-----,As-----,A-----,B-----,H-----,C----,Des----,D----,Es----,E----,F----,Ges----,G----,As----,A----,B----,H----,C---,Des---,D---,Es---,E---,F---,Ges---,G---,As---,A---,B---,H---,C--,Des--,D--,Es--,E--,F--,Ges--,G--,As--,A--,B--,H--,C-,Des-,D-,Es-,E-,F-,Ges-,G-,As-,A-,B-,H-,C,Des,D,Es,E,F,Ges,G,As,A,B,H,C+,Des+,D+,Es+,E+,F+,Ges+,G+,As+,A+,B+,H+,C++,Des++,D++,Es++,E++,F++,Ges++,G++,As++,A++,B++,H++,C+++,Des+++,D+++,Es+++,E+++,F+++,Ges+++,G+++,As+++,A+++,B+++,H+++,C++++,Des++++,D++++,Es++++,E++++,F++++,Ges++++,G++++,As++++,A++++,B++++,H++++,C+++++,Des+++++,D+++++,Es+++++,E+++++,F+++++,Ges+++++,G+++++\n"
			+ "cbx_note_id_german_uc/cbx_halftone_id_des/cbx_halftone_id_des:\n"
			+ "C-1,Des-1,D-1,Es-1,E-1,F-1,Ges-1,G-1,As-1,A-1,B-1,H-1,C0,Des0,D0,Es0,E0,F0,Ges0,G0,As0,A0,B0,H0,C1,Des1,D1,Es1,E1,F1,Ges1,G1,As1,A1,B1,H1,C2,Des2,D2,Es2,E2,F2,Ges2,G2,As2,A2,B2,H2,C3,Des3,D3,Es3,E3,F3,Ges3,G3,As3,A3,B3,H3,C4,Des4,D4,Es4,E4,F4,Ges4,G4,As4,A4,B4,H4,C5,Des5,D5,Es5,E5,F5,Ges5,G5,As5,A5,B5,H5,C6,Des6,D6,Es6,E6,F6,Ges6,G6,As6,A6,B6,H6,C7,Des7,D7,Es7,E7,F7,Ges7,G7,As7,A7,B7,H7,C8,Des8,D8,Es8,E8,F8,Ges8,G8,As8,A8,B8,H8,C9,Des9,D9,Es9,E9,F9,Ges9,G9\n"
			+ "cbx_note_id_german_uc/cbx_halftone_id_des/cbx_halftone_id_des:\n"
			+ "C''',Des''',D''',Es''',E''',F''',Ges''',G''',As''',A''',B''',H''',C'',Des'',D'',Es'',E'',F'',Ges'',G'',As'',A'',B'',H'',C',Des',D',Es',E',F',Ges',G',As',A',B',H',C,Des,D,Es,E,F,Ges,G,As,A,B,H,c,des,d,es,e,f,ges,g,as,a,b,h,c',des',d',es',e',f',ges',g',as',a',b',h',c'',des'',d'',es'',e'',f'',ges'',g'',as'',a'',b'',h'',c''',des''',d''',es''',e''',f''',ges''',g''',as''',a''',b''',h''',c'''',des'''',d'''',es'''',e'''',f'''',ges'''',g'''',as'''',a'''',b'''',h'''',c''''',des''''',d''''',es''''',e''''',f''''',ges''''',g''''',as''''',a''''',b''''',h''''',c'''''',des'''''',d'''''',es'''''',e'''''',f'''''',ges'''''',g''''''\n",
			str.toString()
		);
		
		// switch back to default
		cbxs[1].setSelectedIndex(0); 
		cbxs[2].setSelectedIndex(0);
		cbxs[3].setSelectedIndex(0);
	}
}
