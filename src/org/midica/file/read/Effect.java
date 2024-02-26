/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file.read;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.SysexMessage;

import org.midica.config.Dict;
import org.midica.midi.SequenceCreator;

/**
 * This class is used to parse/create sound effects.
 * It is used by the MidicaPL parser.
 * 
 * @author Jan Trukenmüller
 */
public class Effect {
	
	private static final long RPN_DISTANCE = 10;
	
	private static MidicaPLParser parser = null;
	
	private static EffectFlow flow = null;
	
	private static List<TreeMap<Long, Float>> pitchBendRangeByChannel;
	
	private static Set<String> functionNames;
	private static Set<String> effectNames;
	
	private static Map<String, Integer> functionToParamCount;
	private static Map<String, Integer> channelMsgNameToNumber;
	private static Map<String, Integer> ctrlNameToNumber;
	private static Map<String, Integer> rpnNameToNumber;
	private static Map<String, Integer> sysexNameToNumber;
	private static Set<String>          flowElementNames;
	private static Map<String, Integer> ctrlDestToNumber;
	
	private static Pattern flowPattern       = null;
	private static Pattern genCtrlPattern    = null;
	private static Pattern noteOrRestPattern = null;
	private static Pattern noteIndexPattern  = null;
	private static Pattern intPattern        = null;
	private static Pattern periodsPattern    = null;
	
	/**
	 * Initializes data structures and regex patterns based on the current syntax.
	 * Called by {@link MidicaPLParser} if the DEFINE parsing run is finished.
	 * 
	 * @param rootParser  the root parser
	 */
	public static void init(MidicaPLParser rootParser) {
		parser = rootParser;
		
		// create and initialize special structures
		{
			// pitch bend range by channel/tick
			pitchBendRangeByChannel = new ArrayList<>();
			for (int channel = 0; channel < 16; channel++) {
				TreeMap<Long, Float> rangeMap = new TreeMap<>();
				rangeMap.put(0L, 2f);                // default: 2.0
				pitchBendRangeByChannel.add(rangeMap);
			}
		}
		
		// create structures
		functionToParamCount   = new HashMap<>();
		functionNames          = new HashSet<>();
		effectNames            = new HashSet<>();
		channelMsgNameToNumber = new HashMap<>();
		ctrlNameToNumber       = new HashMap<>();
		rpnNameToNumber        = new HashMap<>();
		sysexNameToNumber      = new HashMap<>();
		flowElementNames       = new HashSet<>();
		ctrlDestToNumber       = new HashMap<>();
		
		// init structures for functions
		functionToParamCount.put( MidicaPLParser.FUNC_SET,    1 );
		functionToParamCount.put( MidicaPLParser.FUNC_ON,     0 );
		functionToParamCount.put( MidicaPLParser.FUNC_OFF,    0 );
		functionToParamCount.put( MidicaPLParser.FUNC_LINE,   2 );
		functionToParamCount.put( MidicaPLParser.FUNC_SIN,    3 );
		functionToParamCount.put( MidicaPLParser.FUNC_COS,    3 );
		functionToParamCount.put( MidicaPLParser.FUNC_NSIN,   3 );
		functionToParamCount.put( MidicaPLParser.FUNC_NCOS,   3 );
		functionToParamCount.put( MidicaPLParser.FUNC_LENGTH, 1 );
		functionToParamCount.put( MidicaPLParser.FUNC_WAIT,   1 );
		functionToParamCount.put( MidicaPLParser.FUNC_NOTE,   1 );
		functionToParamCount.put( MidicaPLParser.FUNC_SRC,    1 );
		functionToParamCount.put( MidicaPLParser.FUNC_DEST,   2 );
		for (String key : functionToParamCount.keySet()) {
			functionNames.add(key);
		}
		
		// init structures for effects
		effectNames.add( MidicaPLParser.CH_A_POLY_AT       );
		effectNames.add( MidicaPLParser.CH_D_MONO_AT       );
		effectNames.add( MidicaPLParser.CH_E_PITCH_BEND    );
		effectNames.add( MidicaPLParser.CC_01_MOD          );
		effectNames.add( MidicaPLParser.CC_02_BREATH       );
		effectNames.add( MidicaPLParser.CC_04_FOOT         );
		effectNames.add( MidicaPLParser.CC_05_PORT_TIME    );
		effectNames.add( MidicaPLParser.CC_08_BALANCE      );
		effectNames.add( MidicaPLParser.CC_0A_PAN          );
		effectNames.add( MidicaPLParser.CC_0B_EXPRESSION   );
		effectNames.add( MidicaPLParser.CC_0C_EFFECT_1     );
		effectNames.add( MidicaPLParser.CC_0D_EFFECT_2     );
		effectNames.add( MidicaPLParser.CC_40_HOLD_1       );
		effectNames.add( MidicaPLParser.CC_41_PORTAMENTO   );
		effectNames.add( MidicaPLParser.CC_42_SOSTENUTO    );
		effectNames.add( MidicaPLParser.CC_43_SOFT         );
		effectNames.add( MidicaPLParser.CC_44_LEGATO       );
		effectNames.add( MidicaPLParser.CC_45_HOLD_2       );
		effectNames.add( MidicaPLParser.CC_46_SC1_VAR      );
		effectNames.add( MidicaPLParser.CC_47_SC2_TIMB     );
		effectNames.add( MidicaPLParser.CC_48_SC3_REL_T    );
		effectNames.add( MidicaPLParser.CC_49_SC4_ATT_T    );
		effectNames.add( MidicaPLParser.CC_4A_SC5_BRI      );
		effectNames.add( MidicaPLParser.CC_4B_SC6_DEC_T    );
		effectNames.add( MidicaPLParser.CC_4C_SC7_VIB_R    );
		effectNames.add( MidicaPLParser.CC_4D_SC8_VIB_DP   );
		effectNames.add( MidicaPLParser.CC_4E_SC9_VIB_DL   );
		effectNames.add( MidicaPLParser.CC_54_PORTAMENTO   );
		effectNames.add( MidicaPLParser.CC_5B_EFF1_DEP     );
		effectNames.add( MidicaPLParser.CC_5C_EFF2_DEP     );
		effectNames.add( MidicaPLParser.CC_5D_EFF3_DEP     );
		effectNames.add( MidicaPLParser.CC_5E_EFF4_DEP     );
		effectNames.add( MidicaPLParser.CC_5F_EFF4_DEP     );
		effectNames.add( MidicaPLParser.CC_7E_MONO_MODE    );
		effectNames.add( MidicaPLParser.CC_7F_POLY_MODE    );
		effectNames.add( MidicaPLParser.RPN_0_PITCH_BEND_R );
		effectNames.add( MidicaPLParser.RPN_1_FINE_TUNE    );
		effectNames.add( MidicaPLParser.RPN_2_COARSE_TUNE  );
		effectNames.add( MidicaPLParser.RPN_3_TUNING_PROG  );
		effectNames.add( MidicaPLParser.RPN_4_TUNING_BANK  );
		effectNames.add( MidicaPLParser.RPN_5_MOD_DEPTH_R  );
		effectNames.add( MidicaPLParser.SX_7F09_CTRL_DEST  );
		effectNames.add( MidicaPLParser.FL_CTRL );  // generic controller with number
		effectNames.add( MidicaPLParser.FL_RPN  );  // generic RPN  with number or MSB/LSB
		effectNames.add( MidicaPLParser.FL_NRPN );  // generic NRPN with number or MSB/LSB
		
		// init structures for channel-based effects
		channelMsgNameToNumber.put( MidicaPLParser.CH_A_POLY_AT,    0xA0 );
		channelMsgNameToNumber.put( MidicaPLParser.CH_D_MONO_AT,    0xD0 );
		channelMsgNameToNumber.put( MidicaPLParser.CH_E_PITCH_BEND, 0xE0 );
		
		// init structures for controller-based effects
		ctrlNameToNumber.put( MidicaPLParser.CC_01_MOD,        0x01 );
		ctrlNameToNumber.put( MidicaPLParser.CC_02_BREATH,     0x02 );
		ctrlNameToNumber.put( MidicaPLParser.CC_04_FOOT,       0x04 );
		ctrlNameToNumber.put( MidicaPLParser.CC_05_PORT_TIME,  0x05 );
		ctrlNameToNumber.put( MidicaPLParser.CC_08_BALANCE,    0x08 );
		ctrlNameToNumber.put( MidicaPLParser.CC_0A_PAN,        0x0A );
		ctrlNameToNumber.put( MidicaPLParser.CC_0B_EXPRESSION, 0x0B );
		ctrlNameToNumber.put( MidicaPLParser.CC_0C_EFFECT_1,   0x0C );
		ctrlNameToNumber.put( MidicaPLParser.CC_0D_EFFECT_2,   0x0D );
		ctrlNameToNumber.put( MidicaPLParser.CC_40_HOLD_1,     0x40 );
		ctrlNameToNumber.put( MidicaPLParser.CC_41_PORTAMENTO, 0x41 );
		ctrlNameToNumber.put( MidicaPLParser.CC_42_SOSTENUTO,  0x42 );
		ctrlNameToNumber.put( MidicaPLParser.CC_43_SOFT,       0x43 );
		ctrlNameToNumber.put( MidicaPLParser.CC_44_LEGATO,     0x44 );
		ctrlNameToNumber.put( MidicaPLParser.CC_45_HOLD_2,     0x45 );
		ctrlNameToNumber.put( MidicaPLParser.CC_46_SC1_VAR,    0x46 );
		ctrlNameToNumber.put( MidicaPLParser.CC_47_SC2_TIMB,   0x47 );
		ctrlNameToNumber.put( MidicaPLParser.CC_48_SC3_REL_T,  0x48 );
		ctrlNameToNumber.put( MidicaPLParser.CC_49_SC4_ATT_T,  0x49 );
		ctrlNameToNumber.put( MidicaPLParser.CC_4A_SC5_BRI,    0x4A );
		ctrlNameToNumber.put( MidicaPLParser.CC_4B_SC6_DEC_T,  0x4B );
		ctrlNameToNumber.put( MidicaPLParser.CC_4C_SC7_VIB_R,  0x4C );
		ctrlNameToNumber.put( MidicaPLParser.CC_4D_SC8_VIB_DP, 0x4D );
		ctrlNameToNumber.put( MidicaPLParser.CC_4E_SC9_VIB_DL, 0x4E );
		ctrlNameToNumber.put( MidicaPLParser.CC_54_PORTAMENTO, 0x54 );
		ctrlNameToNumber.put( MidicaPLParser.CC_5B_EFF1_DEP,   0x5B );
		ctrlNameToNumber.put( MidicaPLParser.CC_5C_EFF2_DEP,   0x5C );
		ctrlNameToNumber.put( MidicaPLParser.CC_5D_EFF3_DEP,   0x5D );
		ctrlNameToNumber.put( MidicaPLParser.CC_5E_EFF4_DEP,   0x5E );
		ctrlNameToNumber.put( MidicaPLParser.CC_5F_EFF4_DEP,   0x5F );
		ctrlNameToNumber.put( MidicaPLParser.CC_7E_MONO_MODE,  0x7E );
		ctrlNameToNumber.put( MidicaPLParser.CC_7F_POLY_MODE,  0x7F );
		
		// init structures for RPN-based effects
		rpnNameToNumber.put( MidicaPLParser.RPN_0_PITCH_BEND_R, 0x0000 );
		rpnNameToNumber.put( MidicaPLParser.RPN_1_FINE_TUNE,    0x0001 );
		rpnNameToNumber.put( MidicaPLParser.RPN_2_COARSE_TUNE,  0x0002 );
		rpnNameToNumber.put( MidicaPLParser.RPN_3_TUNING_PROG,  0x0003 );
		rpnNameToNumber.put( MidicaPLParser.RPN_4_TUNING_BANK,  0x0004 );
		rpnNameToNumber.put( MidicaPLParser.RPN_5_MOD_DEPTH_R,  0x0005 );
		
		// init structures for SysEx-based effects
		sysexNameToNumber.put( MidicaPLParser.SX_7F09_CTRL_DEST, 0x7F0900 );
		
		// init controler desinations
		ctrlDestToNumber.put( MidicaPLParser.CD_00_PITCH,         0x00 );
		ctrlDestToNumber.put( MidicaPLParser.CD_01_FILTER_CUTOFF, 0x01 );
		ctrlDestToNumber.put( MidicaPLParser.CD_02_AMPLITUDE,     0x02 );
		ctrlDestToNumber.put( MidicaPLParser.CD_03_LFO_PITCH_D,   0x03 );
		ctrlDestToNumber.put( MidicaPLParser.CD_04_LFO_FILTER_D,  0x04 );
		ctrlDestToNumber.put( MidicaPLParser.CD_05_LFO_AMPL_D,    0x05 );
		
		// init flow element names (effect names, function names, or other possible flow elements)
		for (String effectName : effectNames) {
			flowElementNames.add(effectName);
		}
		for (String functionName : functionNames) {
			flowElementNames.add(functionName);
		}
		flowElementNames.add(MidicaPLParser.FL_DOUBLE);
		
		// compile regex patterns
		{
			// flow pattern
			String flowRegex
				= "\\G"                                               // end of previous match
				+ "(^|" + Pattern.quote(MidicaPLParser.FL_DOT) + ")?" // begin or '.'
				+ "(\\w+)"                                            // name
				+ "(?:"                                               // generic number (optional)
				    + Pattern.quote(MidicaPLParser.FL_ASSIGNER)
				    + "(\\d+)"                                        // MSB or whole number
				    + "(?:"
				    + Pattern.quote(MidicaPLParser.FL_GEN_NUM_SEP)
				    + "(\\d+)"                                        // LSB of generic number
				    + ")?"
				+ ")?"
				+ "(?:"                                               // parameters (optional)
				+ Pattern.quote(MidicaPLParser.PARAM_OPEN)            // (
				+ "(\\S*?)"                                           // function parameters
				+ Pattern.quote(MidicaPLParser.PARAM_CLOSE)           // )
				+ ")?";                                               // optional
			flowPattern = Pattern.compile(flowRegex);
			
			// generic ctrl for controler destination .src(...)
			String genCtrlStr = "^"
				+ Pattern.quote(MidicaPLParser.FL_CTRL)
				+ Pattern.quote(MidicaPLParser.FL_ASSIGNER)
			    + "(\\d+)"                                   // CC number
			    + "$";
			genCtrlPattern = Pattern.compile(genCtrlStr);
			
			// note index pattern (to replace .note(idx) by .note(name))
			String noteIndexRegex
				= "\\G"                                       // end of previous match
				+ "(.*?)"                                     // anything (group 1)
				+ Pattern.quote(MidicaPLParser.FL_DOT)        // '.'
				+ Pattern.quote(MidicaPLParser.FUNC_NOTE)     // note
				+ Pattern.quote(MidicaPLParser.PARAM_OPEN)    // (
				+ "(\\S*?)"                                   // parameter (group 2)
				+ Pattern.quote(MidicaPLParser.PARAM_CLOSE);  // )
			noteIndexPattern = Pattern.compile(noteIndexRegex);
			
			// note/rest pattern
			noteOrRestPattern = Pattern.compile("^[0-9]|" + Pattern.quote(MidicaPLParser.REST) + "$");
			
			// int pattern (for function parameters)
			String intRegex = "^"
				+ "("                           // direct int value (group 1)
				    + "(\\-|\\+)?"              // +/- sign (group 2)
				    + "\\d+"
				+ ")"
				+ "|"
				+ "(?:"
				    + "("                       // percentage value (group 3)
				        + "(\\-|\\+)?"          // +/- sign (group 4)
				        + "\\d+(?:\\.\\d+)?"
				    + ")"
				    + Pattern.quote(MidicaPLParser.EFF_PERCENT)
				+ ")"
				+ "|"
				+ "("                           // half-tone-steps (group 5)
				    + "(\\-|\\+)?"              // +/- sign (group 6)
				    + "\\d+\\.\\d+"
				+ ")"
				+ "|"
				+ "(?:"
				    + "(\\d+)"                  // MSB (group 7)
				    + Pattern.quote(MidicaPLParser.FL_GEN_NUM_SEP)
				    + "(\\d+)"                  // LSB (group 8)
				+ ")"
				+ "$";
			intPattern = Pattern.compile(intRegex);
			
			// periods pattern (for function parameters)
			String periodsRegex = "^"
				+ "("                         // int or float (group 1)
				    + "(\\-|\\+)?"            // +/- sign (group 2)
				    + "\\d+"                  // integer part
				    + "(?:\\.\\d*)?"          // decial part (optional)
				+ ")"
				+ "("                         // percent (group 3)
				    + Pattern.quote(MidicaPLParser.EFF_PERCENT)
				+ ")?"
				+ "$";
			periodsPattern = Pattern.compile(periodsRegex);
		}
	}
	
	/**
	 * Determins if the given string looks like an effect flow or not.
	 * 
	 * @param flow  the string to check
	 * @return **true** if it looks like a flow, otherwise: **false**
	 */
	public static boolean isFlow(String flow) {
		
		// note or rest?
		if (noteOrRestPattern.matcher(flow).find()) {
			return false;
		}
		
		// try to match a flow
		Matcher m = flowPattern.matcher(flow);
		if (m.find()) {
			String elemName = m.group(2);
			
			if (flowElementNames.contains(elemName))
				return true;
			else
				return false;
		}
		
		return false;
	}
	
	/**
	 * Parses and applies the given flow string, if it really looks like a flow.
	 * 
	 * Usually it does **not** look like a flow, if it is a note, rest or pattern.
	 * 
	 * @param channel    MIDI channel
	 * @param flowStr    Flow string, note, rest or pattern.
	 * @param lengthStr  The note length string of the channel command.
	 * @return **true**, if the flow can be parsed, or **false** if it does not look like a flow.
	 * @throws ParseException if it looks like a flow but cannot be parsed.
	 */
	public static boolean applyFlowIfPossible(int channel, String flowStr, String lengthStr) throws ParseException {
		
		// flow active but for a different channel? - close flow
		if (flow != null && channel != flow.getChannel())
			closeFlowIfPossible();
		
		// looks like normal note or rest?
		if (noteOrRestPattern.matcher(flowStr).find()) {
			return false;
		}
		
		// try to match an effect flow
		boolean looksLikeFlow = false;
		int     elemCount     = 0;
		Matcher m = flowPattern.matcher(flowStr);
		int lastMatchOffset = 0;
		try {
			while (m.find()) {
				
				// unpack match
				elemCount++;
				lastMatchOffset  = m.end();
				String dot       = m.group(1);
				String elemName  = m.group(2);
				String numberStr = m.group(3);
				String numberLsb = m.group(4);
				String paramStr  = m.group(5);
				
				if (flowElementNames.contains(elemName))
					looksLikeFlow = true;
				else
					throw new ParseException(Dict.get(Dict.ERROR_FL_UNKNOWN_ELEMENT) + elemName);
				
				// starts with dot? - flow must be open (from the same channel)
				if (dot != null && dot.length() > 0) {
					if (null == flow)
						throw new ParseException(String.format(Dict.get(Dict.ERROR_FL_NOT_OPEN), MidicaPLParser.FL_DOT));
				}
				else {
					// open flow
					if (1 == elemCount)
						flow = new EffectFlow(channel, lengthStr);
					else
						throw new ParseException(String.format(Dict.get(Dict.ERROR_FL_MISSING_DOT), MidicaPLParser.FL_DOT));
				}
				
				// check number
				int number = -1;
				if (MidicaPLParser.FL_CTRL.equals(elemName)) {
					if (numberLsb != null)
						throw new ParseException(Dict.get(Dict.ERROR_FL_NUM_SEP_NOT_ALLOWED) + elemName);
					number = parseGenericNumber(numberStr, numberLsb, 0x7F, elemName);
				}
				else if (MidicaPLParser.FL_RPN.equals(elemName) || MidicaPLParser.FL_NRPN.equals(elemName)) {
					number = parseGenericNumber(numberStr, numberLsb, 0x3FFF, elemName);
				}
				else if (numberStr != null) {
					throw new ParseException(Dict.get(Dict.ERROR_FL_NUMBER_NOT_ALLOWED) + elemName);
				}
				
				// apply the flow element
				applyFlowElement(elemName, number, paramStr);
			}
		}
		catch (ParseException e) {
			if (!looksLikeFlow)
				return false;
			
			throw e;
		}
		
		// probably no match at all - not a flow
		if (!looksLikeFlow)
			return false;
		
		// unmatched characters left?
		if (lastMatchOffset != flowStr.length()) {
			String remainder = flowStr.substring(lastMatchOffset);
			throw new ParseException(Dict.get(Dict.ERROR_FL_UNMATCHED_REMAINDER) + remainder);
		}
		
		// flow applied successfully
		return true;
	}
	
	/**
	 * Closes the current flow, if there is an open flow.
	 */
	public static void closeFlowIfPossible() {
		flow = null;
	}
	
	/**
	 * Translates the pattern indices inside the given effect flow to notes.
	 * 
	 * Needed for pattern calls.
	 * 
	 * @param flowStr      the effect flow to be translated
	 * @param noteNumbers  the note numbers
	 * @return the translated flow
	 * @throws ParseException if an index is not a positive integer or too high for the given noteNumbers.
	 */
	public static String translatePatternIndices(String flowStr, Integer[] noteNumbers) throws ParseException {
		
		String translated = "";
		Matcher m = noteIndexPattern.matcher(flowStr);
		int lastMatchOffset = 0;
		while (m.find()) {
			lastMatchOffset  = m.end();
			String beforeStr = m.group(1);
			String indexStr  = m.group(2);
			try {
				int index = Integer.parseInt(indexStr);
				int note  = noteNumbers[index];
				translated += beforeStr + MidicaPLParser.FL_DOT + MidicaPLParser.FUNC_NOTE
					+ MidicaPLParser.PARAM_OPEN + note + MidicaPLParser.PARAM_CLOSE;
				
			}
			catch (NumberFormatException e) {
				String noteElem = MidicaPLParser.FL_DOT + MidicaPLParser.FUNC_NOTE
					+ MidicaPLParser.PARAM_OPEN + indexStr + MidicaPLParser.PARAM_CLOSE;
				throw new ParseException(
					String.format(Dict.get(Dict.ERROR_FL_NOTE_PAT_IDX_NAN), indexStr, noteElem)
				);
			}
			catch (IndexOutOfBoundsException e) {
				String noteElem = MidicaPLParser.FL_DOT + MidicaPLParser.FUNC_NOTE
					+ MidicaPLParser.PARAM_OPEN + indexStr + MidicaPLParser.PARAM_CLOSE;
				throw new ParseException(
					String.format(Dict.get(Dict.ERROR_FL_NOTE_PAT_IDX_TOO_HIGH), indexStr, noteElem)
				);
			}
		}
		
		// unmatched characters left?
		if (lastMatchOffset != flowStr.length()) {
			String remainder = flowStr.substring(lastMatchOffset);
			translated += remainder;
		}
		
		return translated;
	}
	
	/**
	 * Parses a generic controller or (N)RPN number, assigned in a flow.
	 * 
	 * Needed by one of the following elements:
	 * 
	 * - .ctrl=...
	 * - .rpn=...
	 * - .nrpn=...
	 * 
	 * @param numberStr  The MSB or whole number to be parsed.
	 * @param numberLsb  The LSB, if numberStr is an MSB, or **null** if numberStr is the whole 14-bit number.
	 * @param maxNum     The maximum allowed number.
	 * @param elemName   Flow element name (for error messages).
	 * @return the parsed number
	 * @throws ParseException if the number cannot be parsed or is too high.
	 */
	private static int parseGenericNumber(String numberStr, String numberLsb, int maxNum, String elemName) throws ParseException {
		
		// no number provided?
		if (null == numberStr)
			throw new ParseException(String.format(Dict.get(Dict.ERROR_FL_NUMBER_MISSING), elemName));
		
		try {
			// parse the number or MSB
			int number = Integer.parseInt(numberStr);
			
			// LSB available? - parse it
			if (numberLsb != null) {
				int lsb = Integer.parseInt(numberLsb);
				number = number * 128 + lsb;
			}
			
			// number higher then allowed by the controller or (n)rpn?
			if (number > maxNum)
				throw new ParseException(String.format(Dict.get(Dict.ERROR_FL_NUMBER_TOO_HIGH), numberStr, elemName, maxNum));
			
			// ok
			return number;
		}
		catch (NumberFormatException e) {
			
			// number exceeds integer limit
			throw new ParseException(String.format(Dict.get(Dict.ERROR_FL_NUMBER_TOO_HIGH), numberStr, elemName, maxNum));
		}
	}
	
	/**
	 * Applies an element of an effect flow.
	 * 
	 * @param elemName    flow element name
	 * @param number      controller/rpn/nrpn number or null
	 * @param paramStr    parameters or null
	 * @throws ParseException
	 */
	private static void applyFlowElement(String elemName, int number, String paramStr) throws ParseException {
		
		// check presence of params
		if (functionNames.contains(elemName)) {
			if (paramStr == null && ! MidicaPLParser.FUNC_WAIT.equals(elemName))
				throw new ParseException(String.format(Dict.get(Dict.ERROR_FL_PARAMS_REQUIRED), elemName));
		}
		else {
			if (null != paramStr)
				throw new ParseException(String.format(Dict.get(Dict.ERROR_FL_PARAMS_NOT_ALLOWED), elemName));
		}
		
		// effect type?
		if (effectNames.contains(elemName)) {
			
			int effectType;
			int effectNumber;
			
			// generic controller/rpn/nrpn?
			if (MidicaPLParser.FL_CTRL.equals(elemName)) {
				effectType   = EffectFlow.EFF_TYPE_CTRL;
				effectNumber = number;
			}
			else if (MidicaPLParser.FL_RPN.equals(elemName)) {
				effectType   = EffectFlow.EFF_TYPE_RPN;
				effectNumber = number;
			}
			else if (MidicaPLParser.FL_NRPN.equals(elemName)) {
				effectType   = EffectFlow.EFF_TYPE_NRPN;
				effectNumber = number;
			}
			else {
				
				// named channel-msg/controller/rpn?
				if (channelMsgNameToNumber.containsKey(elemName)) {
					effectType   = EffectFlow.EFF_TYPE_CHANNEL;
					effectNumber = channelMsgNameToNumber.get(elemName);
				}
				else if (ctrlNameToNumber.containsKey(elemName)) {
					effectType   = EffectFlow.EFF_TYPE_CTRL;
					effectNumber = ctrlNameToNumber.get(elemName);
				}
				else if (rpnNameToNumber.containsKey(elemName)) {
					effectType   = EffectFlow.EFF_TYPE_RPN;
					effectNumber = rpnNameToNumber.get(elemName);
				}
				else if (sysexNameToNumber.containsKey(elemName)) {
					effectType   = EffectFlow.EFF_TYPE_SYSEX;
					effectNumber = sysexNameToNumber.get(elemName);
				}
				else {
					throw new FatalParseException("Don't know what to do with effect '" + elemName + "'.");
				}
			}
			
			// apply effect
			flow.setEffect(effectType, effectNumber);
			
			return;
		}
		
		// function?
		if (functionNames.contains(elemName)) {
			
			// unpack parameters - special case: treat 'wait' like 'wait()'
			String[] params;
			if (paramStr != null && paramStr.isEmpty())
				params = new String[] {};  // special case: Functions without any parameter.
			else if ((null == paramStr || paramStr.isEmpty()) && MidicaPLParser.FUNC_WAIT.equals(elemName))
				params = new String[] {};  // special case: wait/wait() without parameter
			else
				params = paramStr.split(Pattern.quote(MidicaPLParser.PARAM_SEPARATOR), -1);
			
			// check number of parameters
			Integer expectedCount = functionToParamCount.get(elemName);
			if (null == expectedCount) {
				throw new FatalParseException("Expected parameter count unknown for function '" + elemName + "'.");
			}
			if (0 == params.length && MidicaPLParser.FUNC_WAIT.equals(elemName)) {
				// OK. Special case for wait() without parameter
			}
			else {
				if (params.length != expectedCount)
					throw new ParseException(
						String.format(Dict.get(Dict.ERROR_FL_WRONG_PARAM_NUM), elemName, expectedCount, params.length, paramStr)
					);
				
				// don't allow empty parameters
				for (String param : params) {
					if (param.isEmpty())
						throw new ParseException(Dict.get(Dict.ERROR_FL_EMPTY_PARAM) + paramStr);
				}
			}
			
			// call function
			applyFunction(elemName, params);
			
			return;
		}
		
		// other flow elements
		if (MidicaPLParser.FL_DOUBLE.equals(elemName)) {
			flow.setDouble();
			return;
		}
		
		throw new FatalParseException("Don't know what to do with flow element '" + elemName + "'.");
	}
	
	/**
	 * Applies a function call inside an effect flow.
	 * 
	 * @param funcName  function name
	 * @param params    parameters
	 * @throws ParseException
	 */
	private static void applyFunction(String funcName, String[] params) throws ParseException {
		
		// wait()
		if (MidicaPLParser.FUNC_WAIT.equals(funcName)) {
			if (params.length > 0)
				flow.applyWait(params[0]);
			else
				flow.applyWait();
			return;
		}
		
		// length()
		if (MidicaPLParser.FUNC_LENGTH.equals(funcName)) {
			flow.setLength(params[0]);
			return;
		}
		
		// All other functions are not supported by all effect types.
		// Check if the function is supported.
		Collection<Integer> supportedFunctions = flow.getSupportedFunctions(funcName);
		int funcType = getFunctionTypeBySyntax(funcName);
		if (!supportedFunctions.contains(funcType))
			throw new ParseException(Dict.get(Dict.ERROR_FUNC_NOT_SUPPORTED_BY_EFF) + funcName);
		
		// note()
		if (MidicaPLParser.FUNC_NOTE.equals(funcName)) {
			int note = parser.parseNote(params[0]);
			flow.setNote(note);
			return;
		}
		
		// src()
		if (MidicaPLParser.FUNC_SRC.equals(funcName)) {
			int[] values = parseSrc(params[0]);
			flow.setSource(values, funcName);
			return;
		}
		
		// dest()
		if (MidicaPLParser.FUNC_DEST.equals(funcName)) {
			int[] values = parseDest(params);
			flow.putDestination(values);
			return;
		}
		
		// for all other functions we need the effect type
		int valueType = flow.getValueType(funcName);
		
		// note required but not set?
		if (flow.needsNote() && flow.getNote() < 0)
			throw new ParseException(Dict.get(Dict.ERROR_FL_NOTE_NOT_SET) + funcName);
		
		// on()/off() - boolean functions
		if (MidicaPLParser.FUNC_ON.equals(funcName) || MidicaPLParser.FUNC_OFF.equals(funcName)) {
			int value = MidicaPLParser.FUNC_ON.equals(funcName) ? 127 : 0;
			
			// check type
			if (MidicaPLParser.FUNC_ON.equals(funcName)) {
				
				// special case: allow on() for ANY / NONE (but then use a predefined value)
				if (valueType == EffectFlow.TYPE_NONE || valueType == EffectFlow.TYPE_ANY) {
					value = flow.getDefaultValueForOn(valueType);
				}
			}
			
			// special case: SysEx
			if (EffectFlow.EFF_TYPE_SYSEX == flow.getEffectType()) {
				applySysex(funcName);
			}
			else {
				setValue(new int[] {value, value});
			}
			
			return;
		}
		
		// set()
		if (MidicaPLParser.FUNC_SET.equals(funcName)) {
			int[] values = parseIntParam(params[0]);
			setValue(values);
			return;
		}
		
		// continuous functions: line(), sin(), cos(), nsin(), ncos()
		if (MidicaPLParser.FUNC_LINE.equals(funcName)
			|| MidicaPLParser.FUNC_SIN.equals(funcName) || MidicaPLParser.FUNC_COS.equals(funcName)
			|| MidicaPLParser.FUNC_NSIN.equals(funcName) || MidicaPLParser.FUNC_NCOS.equals(funcName)) {
			
			applyContinousFunction(funcName, params);
			
			return;
		}
		
		throw new FatalParseException("Don't know what to do with function '" + funcName + "'.");
	}
	
	/**
	 * Translates a currently configured function name to a function type used by
	 * the {@link EffectFlow} class.
	 * 
	 * @param syntax  the configured function name
	 * @return the function type
	 */
	private static int getFunctionTypeBySyntax(String syntax) {
		
		if (MidicaPLParser.FUNC_ON.equals(syntax))
			return EffectFlow.FUNC_TYPE_ON;
		if (MidicaPLParser.FUNC_OFF.equals(syntax))
			return EffectFlow.FUNC_TYPE_OFF;
		if (MidicaPLParser.FUNC_SET.equals(syntax))
			return EffectFlow.FUNC_TYPE_SET;
		if (MidicaPLParser.FUNC_NOTE.equals(syntax))
			return EffectFlow.FUNC_TYPE_NOTE;
		if (MidicaPLParser.FUNC_SRC.equals(syntax))
			return EffectFlow.FUNC_TYPE_SRC;
		if (MidicaPLParser.FUNC_DEST.equals(syntax))
			return EffectFlow.FUNC_TYPE_DEST;
		
		// line, sin, cos, ...
		return EffectFlow.FUNC_TYPE_CONT;
	}
	
	/**
	 * Applies the given value array to the MIDI sequence.
	 * 
	 * The given **values** array has the same format as the one returned
	 * by {@link #parseIntParam(String)}. It contains 2 or 3 bytes.
	 * 
	 * The first byte is always the raw value.
	 * 
	 * The second byte is either the MSB (if there is a third byte) or the only value.
	 * 
	 * If the third value is available, it contains the LSB.
	 * 
	 * @param values  see above.
	 * @throws ParseException if there is an unexpected problem.
	 */
	private static void setValue(int[] values) throws ParseException {
		
		long tick       = flow.getCurrentTick();
		int  effectType = flow.getEffectType();
		int  effectNum  = flow.getEffectNumber();
		int  channel    = flow.getChannel();
		
		try {
			if (EffectFlow.EFF_TYPE_CHANNEL == effectType) {
				if (2 == values.length) {
					
					// pitch bend?
					if (0xE0 == effectNum) {
						// special case: set(0) for MSB only - set the LSB to 0 - to get no bend at all
						if (0x40 == values[1])
							SequenceCreator.addMessageChannelEffect(effectNum, channel, 0, values[1], tick);
						else
							// use the MSB in both positions - cover the whole range
							SequenceCreator.addMessageChannelEffect(effectNum, channel, values[1], values[1], tick);
						return;
					}
					
					// mono_at?
					if (0xD0 == effectNum) {
						SequenceCreator.addMessageChannelEffect(effectNum, channel, values[1], 0, tick);
						return;
					}
					
					// poly_at?
					if (0xA0 == effectNum) {
						int note = flow.getNote();
						SequenceCreator.addMessageChannelEffect(effectNum, channel, note, values[1], tick);
						return;
					}
				}
				else if (3 == values.length) {
					// pitch bend: lsb first, msb second
					SequenceCreator.addMessageChannelEffect(effectNum, channel, values[2], values[1], tick);
					return;
				}
			}
			else if (EffectFlow.EFF_TYPE_CTRL == effectType) {
				if (2 == values.length) {
					SequenceCreator.addMessageCtrl(effectNum, channel, values[1], tick);
					return;
				}
				else if (3 == values.length) {
					SequenceCreator.addMessageCtrl(effectNum,        channel, values[1], tick); // MSB
					SequenceCreator.addMessageCtrl(effectNum + 0x20, channel, values[2], tick); // LSB
					return;
				}
			}
			else if (EffectFlow.EFF_TYPE_RPN == effectType) {
				setRpnOrNrpn(true, effectNum, values);
				return;
			}
			else if (EffectFlow.EFF_TYPE_NRPN == effectType) {
				setRpnOrNrpn(false, effectNum, values);
				return;
			}
			else {
				throw new FatalParseException("Unknown effect type: " + effectType + ".");
			}
			throw new FatalParseException("Unknown effect type/number/byte-count combination: " + effectType + "/" + effectNum + "/" + values.length + ".");
		}
		catch (InvalidMidiDataException e) {
			throw new FatalParseException("Invalid MIDI data when trying to apply effect " + effectType + "/" + effectNum + ".");
		}
	}
	
	/**
	 * Writes an RPN or NRPN to the MIDI sequence.
	 * 
	 * The given **values** array has the same format as the one returned
	 * by {@link #parseIntParam(String)}. It contains 2 or 3 bytes.
	 * 
	 * The first byte is always the raw value.
	 * 
	 * The second byte is either the MSB (if there is a third byte) or the only value.
	 * 
	 * If the third value is available, it contains the LSB.
	 * 
	 * @param isRpn      **true** to create an RPN, **false** to create an NRPN.
	 * @param effectNum  14-bit number of the RPN or NRPN
	 * @param values     see above
	 * @throws ParseException if there is an unexpected problem.
	 */
	private static void setRpnOrNrpn(boolean isRpn, int effectNum, int[] values) throws ParseException {
		
		long tick    = flow.getCurrentTick();
		int  channel = flow.getChannel();
		
		int rpnMsb = isRpn ? 0x65 : 0x63;
		int rpnLsb = isRpn ? 0x64 : 0x62;
		
		int effectMsb = effectNum >> 7;
		int effectLsb = effectNum & 0x7F;
		
		long tick1 = tick - 3 * RPN_DISTANCE;
		long tick2 = tick - 2 * RPN_DISTANCE;
		long tick3 = tick - RPN_DISTANCE;
		long tick4 = tick;
		long tick5 = tick + RPN_DISTANCE;
		if (tick1 < 0) tick1 = 0;
		if (tick2 < 0) tick2 = 0;
		if (tick3 < 0) tick3 = 0;
		
		try {
			// (N)RPN MSB / LSB
			SequenceCreator.addMessageCtrl(rpnMsb, channel, effectMsb, tick1);
			SequenceCreator.addMessageCtrl(rpnLsb, channel, effectLsb, tick2);
			
			// data entry MSB / LSB
			SequenceCreator.addMessageCtrl(0x06, channel, values[1], tick3);
			if (values.length > 2)
				SequenceCreator.addMessageCtrl(0x26, channel, values[2], tick4);
			
			// (N)RPN reset MSB / LSB
			SequenceCreator.addMessageCtrl(rpnMsb, channel, 0x7F, tick5);
			SequenceCreator.addMessageCtrl(rpnLsb, channel, 0x7F, tick5);
		}
		catch (InvalidMidiDataException e) {
			throw new FatalParseException("Invalid MIDI data when trying to apply (N)RPN " + effectNum + ".");
		}
		
		// special case: pitch bend range
		if (isRpn && 0x0000 == flow.getEffectNumber()) {
			TreeMap<Long, Float> rangeMap = pitchBendRangeByChannel.get(channel);
			
			// get range in half tone steps
			float halfToneSteps = values[1];
			if (values.length > 2) {
				float cents = values[2] / 100f;
				halfToneSteps += cents;
			}
			
			// remember pitch bend range
			rangeMap.put(tick, halfToneSteps);
		}
	}
	
	/**
	 * Applies an effect that results in a SysEx message.
	 * 
	 * Currently this is only used for controller destination messages.
	 * 
	 * @param functionName  the name of the called function (only needed for error messages)
	 * @throws ParseException if the .src() or .dest() have not yet been called, or an unexpected error occurs.
	 */
	private static void applySysex(String functionName) throws ParseException {
		int effNum = flow.getEffectNumber();
		
		try {
			SysexMessage msg = null;
			
			// controller destination
			if (0x7F0900 == effNum) {
				byte[] content = flow.getControllerDestination(functionName);
				msg = new SysexMessage(content, content.length);
			}
			
			// check sysex message
			if (null == msg)
				throw new FatalParseException("Don't know how to create sysex msg for effect " + effNum);
			byte[] content = msg.getMessage();
			if (content.length < 3)
				throw new FatalParseException("SysEx message too short for effect " + effNum);
			if (content[0] != (byte) 0xF0)
				throw new FatalParseException("SysEx message doesn't start with 0xF0 for effect " + effNum);
			if (content[content.length - 1] != (byte) 0xF7)
				throw new FatalParseException("SysEx message doesn't end with 0xF7 for effect " + effNum);
			
			// add the message to the right tick
			SequenceCreator.addMessageGeneric(msg, flow.getCurrentTick());
		}
		catch (InvalidMidiDataException e) {
			throw new FatalParseException("Unable to create sysex message for effect number " + effNum);
		}
	}
	
	/**
	 * Applies a continuous function like line(), sin(), and so on.
	 * 
	 * @param function  function name
	 * @param params    function parameters
	 * @throws ParseException if the parameters are invalid.
	 */
	private static void applyContinousFunction(String function, String[] params) throws ParseException {
		
		// prepare common data
		long startTick = flow.getCurrentTick();
		long endTick   = flow.getFutureTick();
		long tickDiff  = endTick - startTick;
		int  byteCount = 0;
		int  lastMsb   = -1;
		int  lastLsb   = -1;
		
		// prepare data for line()
		boolean isLine    = false;
		long    valueDiff = 0;
		long    fromVal   = 0;
		
		// prepare data for sin(), cos(), nsin(), ncos()
		boolean isSin          = false;
		boolean isNeg          = false;
		int     middleVal      = 0;
		int     posDiff        = 0;
		int     negDiff        = 0;
		double  ticksPerPeriod = 0;
		
		// parse parameters
		if (MidicaPLParser.FUNC_LINE.equals(function)) {
			isLine            = true;
			int[] fromValues  = parseIntParam(params[0]);
			int[] untilValues = parseIntParam(params[1]);
			fromVal           = fromValues[0];
			long untilVal     = untilValues[0];
			valueDiff         = untilVal - fromVal;
			byteCount         = fromValues.length;
		}
		else {
			// sin, cos, nsin, ncos
			int[] minValues = parseIntParam(params[0]);
			int[] maxValues = parseIntParam(params[1]);
			int   minVal    = minValues[0];
			int   maxVal    = maxValues[0];
			middleVal       = (minVal + maxVal + 1) / 2;
			negDiff         = middleVal - minVal;
			posDiff         = maxVal - middleVal;
			float periods   = parsePeriodsParam(params[2]);
			ticksPerPeriod  = (tickDiff / periods);
			byteCount       = minValues.length;
			if (MidicaPLParser.FUNC_SIN.equals(function) || MidicaPLParser.FUNC_NSIN.equals(function)) {
				isSin = true;
			}
			if (MidicaPLParser.FUNC_NSIN.equals(function) || MidicaPLParser.FUNC_NCOS.equals(function)) {
				isNeg = true;
			}
		}
		
		// calculate the value for each tick
		for (long tick = 0; tick <= tickDiff; tick++) {
			int[] setValues = new int[byteCount];
			
			if (isLine) {
				// line(): f(tick) = (tick * valueDiff / tickDiff) + from
				long value = tick * valueDiff / tickDiff;
				setValues[0] = ((int) value) + ((int) fromVal);
			}
			else {
				// sin, cos, nsin, ncos
				double radian = Math.PI * 2 * (tick / ticksPerPeriod);
				double value  = isSin ? Math.sin(radian) : Math.cos(radian);
				if (isNeg)
					value = -value;
				value = value < 0 ? value * negDiff : value * posDiff;
				value = Math.round(value) + middleVal;
				setValues[0] = (int) value;
			}
			
			// calculate MSB / LSB
			int msb = setValues[0];
			int lsb = lastLsb;
			if (byteCount > 2) {
				msb = setValues[0] >> 7;
				lsb = setValues[0] & 0x7F;
			}
			
			// nothing to be changed?
			if (msb == lastMsb && lsb == lastLsb)
				continue;
			
			// calculate and set the real MIDI tick for the effect's event
			flow.setCurrentTick(tick + startTick);
			
			// apply the value similar to set()
			setValues[1] = msb;
			if (byteCount > 2)
				setValues[2] = lsb;
			setValue(setValues);
			lastMsb = msb;
			lastLsb = lsb;
		}
		
		// update flow tick
		flow.setCurrentTick(endTick);
	}
	
	/**
	 * Parses a numeric or percentage or float or MSB/LSB function parameter, checks it against the
	 * sound effect's min/max and returns the resulting value byte(s).
	 * 
	 * The returned array consists of the following bytes:
	 * 
	 * - first byte: the complete value (up to 14 bits)
	 * - second byte: first data byte (or MSB)
	 * - third byte: second data byte (or LSB)
	 * 
	 * Single-byte values only return 2 bytes.
	 * 
	 * @param valueStr  numeric or percentage parameter string
	 * @return resulting value and byte(s), as described above
	 * @throws ParseException if the parameter cannot be parsed or is out of range
	 */
	private static int[] parseIntParam(String valueStr) throws ParseException {
		
		// get range of the sound effect
		int valueType = flow.getValueType(valueStr);
		int min       = flow.getMin();
		int max       = flow.getMax();
		boolean requiresSign    = flow.requiresSign(valueType);
		boolean supportsPercent = flow.supportsPercentage();
		boolean canUseHalfTones = flow.supportsHalfToneSteps();
		boolean isMsbLsb = false;
		String  maxStr        = requiresSign ? "+" + max : max + "";
		String  maxPercentStr = requiresSign ? "+100" : "100";
		
		// parse the parameter
		Integer value = null;
		try {
			Matcher m = intPattern.matcher(valueStr);
			if (m.matches()) {
				String intStr      = m.group(1);
				String sign1       = m.group(2);
				String percentStr  = m.group(3);
				String sign2       = m.group(4);
				String halfToneStr = m.group(5);
				String sign3       = m.group(6);
				String msbStr      = m.group(7);
				String lsbStr      = m.group(8);
				
				// check sign (+/-)
				boolean isSigned     = (sign1 != null || sign2 != null || sign3 != null);
				boolean isFlexSigned = isSigned && (EffectFlow.TYPE_ANY == valueType || EffectFlow.TYPE_BYTE_FLEX == valueType);
				if (null == msbStr) {
					if (isSigned && !flow.supportsSign(valueType))
						throw new ParseException(Dict.get(Dict.ERROR_FUNC_SIGNED_FORBIDDEN) + valueStr);
					if (!isSigned && flow.requiresSign(valueType))
						throw new ParseException(Dict.get(Dict.ERROR_FUNC_SIGNED_REQUIRED) + valueStr);
				}
				
				if (intStr != null) {
					if (canUseHalfTones) {
						value = parseHalfToneSteps(intStr);
					}
					else {
						value = Integer.parseInt(intStr);
						if (isFlexSigned)
							value += 64;
					}
				}
				else if (percentStr != null) {
					float percent = Float.parseFloat(percentStr);
					if (isFlexSigned)
						percent = percent / 2 + 50;
					
					// check percentage input
					if (!supportsPercent)
						throw new ParseException(Dict.get(Dict.ERROR_FUNC_PERCENT_FORBIDDEN) + valueStr);
					if (percent > 100)
						throw new ParseException(String.format(Dict.get(Dict.ERROR_FUNC_VAL_GREATER_MAX), valueStr, maxPercentStr + MidicaPLParser.EFF_PERCENT));
					if (percent < -100 && min < 0)
						throw new ParseException(String.format(Dict.get(Dict.ERROR_FUNC_VAL_LOWER_MIN), valueStr, -100 + MidicaPLParser.EFF_PERCENT));
					
					// calculate value
					if (percent < 0)
						// theoretically: value = percent * -min / 100
						value = (int) ((percent * -min * 10 - 100 * 5) / (100 * 10));
					else
						// theoretically: value = percent * max / 100
						value = (int) ((percent * max * 10 + 100 * 5) / (100 * 10));
				}
				else if (halfToneStr != null) {
					if (!canUseHalfTones)
						throw new ParseException(String.format(Dict.get(Dict.ERROR_FUNC_HALFTONE_NOT_ALLOWED), valueStr));
					
					value = parseHalfToneSteps(halfToneStr);
				}
				else if (msbStr != null) {
					isMsbLsb = true;
					if (!flow.isDouble())
						throw new ParseException(String.format(
							Dict.get(Dict.ERROR_FUNC_MSB_LSB_NEEDS_DOUBLE), valueStr, MidicaPLParser.FL_DOUBLE));
					int msb = Integer.parseInt(msbStr);
					int lsb = Integer.parseInt(lsbStr);
					if (msb > 127)
						throw new ParseException(String.format(
							Dict.get(Dict.ERROR_FUNC_MSB_TOO_HIGH), valueStr, msbStr));
					if (lsb > 127)
						throw new ParseException(String.format(
							Dict.get(Dict.ERROR_FUNC_LSB_TOO_HIGH), valueStr, lsbStr));
					value = msb * 128 + lsb;
				}
				else {
					throw new FatalParseException(Dict.get(Dict.ERROR_FUNC_NO_NUMBER) + valueStr);
				}
			}
		}
		catch (NumberFormatException e) {
			throw new ParseException(Dict.get(Dict.ERROR_FUNC_NO_NUMBER) + valueStr);
		}
		if (null == value)
			throw new ParseException(Dict.get(Dict.ERROR_FUNC_NO_NUMBER) + valueStr);
		
		// check value against min / max
		if (value < min)
			throw new ParseException(String.format(Dict.get(Dict.ERROR_FUNC_VAL_LOWER_MIN), valueStr, min));
		if (value > max && !isMsbLsb)
			throw new ParseException(String.format(Dict.get(Dict.ERROR_FUNC_VAL_GREATER_MAX), valueStr, maxStr));
		
		// adjust the actual MIDI value for signed types
		if (EffectFlow.TYPE_MSB_SIGNED == valueType && !isMsbLsb) {
			if (flow.isDouble())
				value += 8192;
			else
				value += 64;
		}
		else if (EffectFlow.TYPE_BYTE_SIGNED == valueType) {
			value += 64;
		}
		
		// find out how many bytes are needed
		int byteCount = 1;
		if (EffectFlow.TYPE_MSB == valueType || EffectFlow.TYPE_MSB_SIGNED == valueType) {
			if (flow.isDouble())
				byteCount = 2;
		}
		
		// handle 1 byte
		if (1 == byteCount)
			return new int[] {value, value};
		
		// handle 2 bytes
		int msb = value >> 7;
		int lsb = value & 0x7F;
		int[] values = new int[] {value, msb, lsb};
		adjustRoundingEdgeCaseForPitchBendRange(values);
		
		return values;
	}
	
	/**
	 * Parses half-tone parameters (float or int).
	 * 
	 * This is used for one of the following effect types:
	 * 
	 * - pitch bend range
	 * - pitch bend
	 * - channel coarse tuning
	 * - channel fine tuning
	 * - controller destination: pitch control
	 * 
	 * @param halfToneStr  the half tone steps parameter
	 * @return the value that the effect type needs.
	 * @throws ParseException
	 */
	private static int parseHalfToneSteps(String halfToneStr) throws ParseException {
		
		int effNum = flow.getEffectNumber();
		
		float halfToneSteps = Float.parseFloat(halfToneStr);
		
		// pitch bend range
		if (0x0000 == effNum) {
			
			// check range
			float max = flow.isDouble() ? 127.99f : 127;
			if (halfToneSteps > max)
				throw new ParseException(String.format(Dict.get(Dict.ERROR_FUNC_VAL_GREATER_MAX), halfToneStr, max));
			
			// only one byte?
			if (!flow.isDouble())
				return Math.round(halfToneSteps);
			
			// 2 bytes
			int   msb       = (int) halfToneSteps;
			float remainder = halfToneSteps - msb;
			int   lsb       = Math.round(remainder * 100);
			return msb * 128 + lsb;
		}
		
		// channel coarse tune or
		// controller destination: pitch control
		if (0x0002 == effNum || 0x7F0900 == effNum) {
			
			// don't allow broken values
			if (halfToneSteps != Math.round(halfToneSteps))
				throw new ParseException(Dict.get(Dict.ERROR_FUNC_BROKEN_HALFTONE) + halfToneStr);
			
			// only one byte allowed
			return Math.round(halfToneSteps);
		}
		
		// From here on, we have either channel fine tune or pitch bend.
		// Both are signed MSBs that can have up to 2 bytes.
		// get max value
		int max;
		if (flow.isDouble())
			max = halfToneSteps < 0 ? 8192 : 8191;
		else
			max = halfToneSteps < 0 ? 64 : 63;
		
		// channel fine tune
		if (0x0001 == effNum) {
			
			// not between +/-1.0?
			if (halfToneSteps < -1.0)
				throw new ParseException(String.format(Dict.get(Dict.ERROR_FUNC_VAL_LOWER_MIN), halfToneStr, -1.0));
			if (halfToneSteps > 1.0)
				throw new ParseException(String.format(Dict.get(Dict.ERROR_FUNC_VAL_GREATER_MAX), halfToneStr, "+1.0"));
			
			return Math.round(halfToneSteps * max);
		}
		
		// pitch bend
		if (0xE0 == effNum) {
			
			// get current pitch bend range
			int  channel = flow.getChannel();
			long tick    = flow.getCurrentTick();
			Entry<Long, Float> entry = pitchBendRangeByChannel.get(channel).floorEntry(tick);
			float range = entry.getValue();
			
			// range exceeded?
			if (Math.abs(halfToneSteps) > range)
				throw new ParseException(String.format(Dict.get(Dict.ERROR_FUNC_HALFTONE_GT_RANGE), halfToneStr, range));
			
			return Math.round(max * (halfToneSteps / range));
		}
		
		// this should never be reached
		int elemType = flow.getEffectType();
		throw new FatalParseException("Don't know what to do with half tone steps of effect '" + elemType + "/" + effNum + "'.");
	}
	
	/**
	 * Checks and corrects rounding errors for half tone parameters with rounding errors.
	 * 
	 * Only applies for:
	 * 
	 * - pitch bend range
	 * 
	 * Does nothing for other effect types.
	 * 
	 * Fixes an edge case with MSB and LSB where the rounded LSB value is 100.
	 * Example: 4.997
	 * 
	 * Here the MSB must be set to 5 and the LSB must be set to 0.
	 * 
	 * @param values  the values as returned by {@link #parseIntParam(String)}
	 */
	private static void adjustRoundingEdgeCaseForPitchBendRange(int[] values) {
		
		// not pitch bend range?
		if (!flow.supportsHalfToneSteps())
			return;
		if (flow.getEffectNumber() != 0x0000)
			return;
		
		// the current flow is a pitch bend range RPN
		int msb = values[1];
		int lsb = values[2];
		
		// fix rounding issue.
		// Example: 4.997 ==> MSB = 4, LSB = 100
		// Fix:           ==> MSB = 5, LSB = 0
		if (lsb >= 100) {
			msb++;
			lsb = 0;
			values[1] = msb;
			values[2] = lsb;
		}
	}
	
	/**
	 * Parses the 'periods' argument of a trigonometric function call like sin() etc.
	 * 
	 * @param valueStr  numeric or percentage parameter string
	 * @return the parsed periods value
	 * @throws ParseException if the parameter cannot be parsed or is out of range
	 */
	private static float parsePeriodsParam(String valueStr) throws ParseException {
		
		try {
			Matcher m = periodsPattern.matcher(valueStr);
			if (m.matches()) {
				String floatStr   = m.group(1);
				String sign       = m.group(2);
				String percentStr = m.group(3);
				float  periods;
				if (sign != null)
					throw new ParseException(Dict.get(Dict.ERROR_FUNC_PERIODS_SIGNED) + valueStr);
				
				periods = Float.parseFloat(floatStr);
				if (periods <= 0)
					throw new ParseException(Dict.get(Dict.ERROR_FUNC_PERIODS_NOT_POS) + valueStr);
				if (Float.isInfinite(periods))
					throw new ParseException(Dict.get(Dict.ERROR_FUNC_PERIODS_NO_NUMBER) + valueStr);
				if (percentStr != null) {
					periods /= 100;
				}
				
				return periods;
			}
		}
		catch (NumberFormatException e) {
			throw new ParseException(Dict.get(Dict.ERROR_FUNC_PERIODS_NO_NUMBER) + valueStr);
		}
		
		throw new ParseException(Dict.get(Dict.ERROR_FUNC_PERIODS_NO_NUMBER) + valueStr);
	}
	
	/**
	 * Parses the parameter of a src() function call used in a controller destination effect.
	 * 
	 * @param param  the parameter
	 * @return the resulting byte for the SysEx message (2 for mono_at/poly_at, 3 for cc)
	 * @throws ParseException if the parameter cannot be parsed
	 */
	private static int[] parseSrc(String param) throws ParseException {
		
		// get channel
		int channel = flow.getChannel();
		
		// mono_at?
		if (MidicaPLParser.CH_D_MONO_AT.equals(param))
			return new int[] {0x01, channel};
		
		// poly_at?
		if (MidicaPLParser.CH_A_POLY_AT.equals(param))
			return new int[] {0x02, channel};
		
		// cc (continuous controller) by name?
		Integer cc = ctrlNameToNumber.get(param);
		
		// cc (continuous controller) by generic number? - e.g. ctrl=5
		if (null == cc) {
			Matcher m = genCtrlPattern.matcher(param);
			if (m.matches()) {
				String ccStr = m.group(1);
				try {
					cc = Integer.parseInt(ccStr);
				}
				catch (NumberFormatException e) {
					throw new ParseException(Dict.get(Dict.ERROR_FUNC_CD_SRC_CTRL_UNKNOWN) + param);
				}
			}
		}
		
		// cc cannot be parsed?
		if (null == cc)
			throw new ParseException(Dict.get(Dict.ERROR_FUNC_CD_SRC_CTRL_UNKNOWN) + param);
		
		// cc is allowed?
		boolean isOk = false;
		if (0x01 <= cc && cc <= 0x1F)
			isOk = true;
		if (0x40 <= cc && cc <= 0x5F)
			isOk = true;
		if (!isOk)
			throw new ParseException(Dict.get(Dict.ERROR_FUNC_CD_SRC_CTRL_NOT_SUPP) + param);
		
		// cc result
		return new int[] {0x03, channel, cc};
	}
	
	/**
	 * Parses the parameters of a dest() function call used in a controller destination effect.
	 * 
	 * @param params  the parameters
	 * @return the resulting bytes (2 bytes: destination and range)
	 * @throws ParseException if the parameters cannot be parsed
	 */
	private static int[] parseDest(String[] params) throws ParseException {
		
		Integer dest = ctrlDestToNumber.get(params[0]);
		if (null == dest)
			throw new ParseException(Dict.get(Dict.ERROR_FUNC_CD_DEST_UNKNOWN) + params[0]);
		
		// parse range
		flow.setCurrentCtrlDest(dest);
		int range = parseIntParam(params[1])[0];
		
		return new int[] {dest, range};
	}
}
