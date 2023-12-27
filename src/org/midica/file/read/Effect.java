/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file.read;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.midi.InvalidMidiDataException;

import org.midica.config.Dict;
import org.midica.midi.SequenceCreator;

/**
 * This class is used to parse/create sound effects.
 * It is used by the MidicaPL parser.
 * 
 * @author Jan Trukenmüller
 */
public class Effect {
	
	private static MidicaPLParser parser = null;
	
	private static EffectFlow flow = null;
	
	private static Set<String> functionNames;
	private static Set<String> effectNames;
	
	private static Map<String, Integer> functionToParamCount;
	private static Map<String, Integer> channelMsgNameToNumber;
	private static Map<String, Integer> ctrlNameToNumber;
	private static Map<String, Integer> rpnNameToNumber;
	private static Set<String>          flowElementNames;
	
	private static Pattern flowPattern       = null;
	private static Pattern noteOrRestPattern = null;
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
		
		functionToParamCount   = new HashMap<>();
		functionNames          = new HashSet<>();
		effectNames            = new HashSet<>();
		channelMsgNameToNumber = new HashMap<>();
		ctrlNameToNumber       = new HashMap<>();
		rpnNameToNumber        = new HashMap<>();
		flowElementNames       = new HashSet<>();
		
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
		for (String key : functionToParamCount.keySet()) {
			functionNames.add(key);
		}
		
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
		effectNames.add( MidicaPLParser.RPN_0_PITCH_BEND_R );
		effectNames.add( MidicaPLParser.RPN_1_FINE_TUNE    );
		effectNames.add( MidicaPLParser.RPN_2_COARSE_TUNE  );
		effectNames.add( MidicaPLParser.RPN_3_TUNING_PROG  );
		effectNames.add( MidicaPLParser.RPN_4_TUNING_BANK  );
		effectNames.add( MidicaPLParser.RPN_5_MOD_DEPTH_R  );
		
		effectNames.add( MidicaPLParser.FL_CTRL );
		effectNames.add( MidicaPLParser.FL_RPN  );
		effectNames.add( MidicaPLParser.FL_NRPN );
		
		channelMsgNameToNumber.put( MidicaPLParser.CH_A_POLY_AT,    0xA0 );
		channelMsgNameToNumber.put( MidicaPLParser.CH_D_MONO_AT,    0xD0 );
		channelMsgNameToNumber.put( MidicaPLParser.CH_E_PITCH_BEND, 0xE0 );
		
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
		
		rpnNameToNumber.put( MidicaPLParser.RPN_0_PITCH_BEND_R, 0x0000 );
		rpnNameToNumber.put( MidicaPLParser.RPN_1_FINE_TUNE,    0x0001 );
		rpnNameToNumber.put( MidicaPLParser.RPN_2_COARSE_TUNE,  0x0002 );
		rpnNameToNumber.put( MidicaPLParser.RPN_3_TUNING_PROG,  0x0003 );
		rpnNameToNumber.put( MidicaPLParser.RPN_4_TUNING_BANK,  0x0004 );
		rpnNameToNumber.put( MidicaPLParser.RPN_5_MOD_DEPTH_R,  0x0005 );
		
		// flow element names (effect names, function names, or other possible flow elements)
		for (String effectName : effectNames) {
			flowElementNames.add(effectName);
		}
		for (String functionName : functionNames) {
			flowElementNames.add(functionName);
		}
		flowElementNames.add(MidicaPLParser.FL_DOUBLE);
		
		// compile regex patterns
		String flowRegex
			= "\\G"                                                                // end of previous match
			+"(^|" + Pattern.quote(MidicaPLParser.FL_DOT) + ")"                    // begin or '.'
			+ "(\\w+)(?:" + Pattern.quote(MidicaPLParser.FL_ASSIGNER) + "(\\d+))?" // flow element without parameters
			+ "(?:"
			+ Pattern.quote(MidicaPLParser.PARAM_OPEN)                             // (
			+ "(\\S*?)"                                                            // function parameters
			+ Pattern.quote(MidicaPLParser.PARAM_CLOSE)                            // )
			+ ")?";                                                                // optional
		flowPattern       = Pattern.compile(flowRegex);
		noteOrRestPattern = Pattern.compile("^[0-9]|" + Pattern.quote(MidicaPLParser.REST) + "$");
		String intRegex = "^"
			+ "(\\-?\\d+)"                // direct int value (group 1)
			+ "|"
			+ "(?:"
				+ "(\\-?\\d+(\\.\\d+)?)"  // percentage value (group 2)
				+ Pattern.quote(MidicaPLParser.EFF_PERCENT)
			+ ")"
			+ "$";
		intPattern = Pattern.compile(intRegex);
		String periodsRegex = "^"
			+ "(\\-?\\d+(?:\\.\\d*))"     // int or float (group 1)
			+ "("                         // percent (group 2)
				+ Pattern.quote(MidicaPLParser.EFF_PERCENT)
			+ ")?"
			+ "$";
		periodsPattern = Pattern.compile(periodsRegex);
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
				String paramStr  = m.group(4);
				
				// flow or something else?
				if (null == elemName) {
					return false;
				}
				if (flowElementNames.contains(elemName))
					looksLikeFlow = true;
				else
					throw new ParseException(Dict.get(Dict.ERROR_FL_UNKNOWN_ELEMENT) + elemName);
				
				// starts with dot? - flow must be open
				if (dot.length() > 0) {
					if (null == flowStr)
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
					number = parseGenericNumber(numberStr, 0x7F, elemName);
				}
				else if (MidicaPLParser.FL_RPN.equals(elemName) || MidicaPLParser.FL_NRPN.equals(elemName)) {
					number = parseGenericNumber(numberStr, 0x3FFF, elemName);
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
	 * Parses a generic controller or (N)RPN number, assigned in a flow.
	 * 
	 * @param numberStr  The number to be parsed.
	 * @param maxNum     The maximum allowed number.
	 * @param elemName   Flow element name (for error messages).
	 * @return the parsed number
	 * @throws ParseException if the number cannot be parsed or is too high.
	 */
	private static int parseGenericNumber(String numberStr, int maxNum, String elemName) throws ParseException {
		
		// no number provided?
		if (null == numberStr)
			throw new ParseException(String.format(Dict.get(Dict.ERROR_FL_NUMBER_MISSING), elemName));
		
		try {
			// parse the number
			int number = Integer.parseInt(numberStr);
			
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
		
		// check number
		if (MidicaPLParser.FL_CTRL.equals(elemName)) {
			if (number < 0)
				throw new ParseException("Number missing for element '" + elemName + "'. This should not happen. Please report.");
			else if (number > 127)
				throw new ParseException("Number for element '" + elemName + "' is higher than 127. This should not happen. Please report.");
		}
		else if (MidicaPLParser.FL_RPN.equals(elemName) || MidicaPLParser.FL_RPN.equals(elemName)) {
			if (number < 0)
				throw new ParseException("Number missing for element '" + elemName + "'. This should not happen. Please report.");
			else if (number > 16383)
				throw new ParseException("Number for element '" + elemName + "' is higher than 16383. This should not happen. Please report.");
		}
		else if (number > -1) {
			throw new ParseException("Number missing for element '" + elemName + "' not allowed. This should not happen. Please report.");
		}
		
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
				else {
					throw new ParseException("Don't know what to do with effect '" + elemName + "'. This should not happen. Please report.");
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
			if ((null == paramStr || paramStr.isEmpty()) && MidicaPLParser.FUNC_WAIT.equals(elemName))
				params = new String[] {};
			else
				params = paramStr.split(Pattern.quote(MidicaPLParser.PARAM_SEPARATOR), -1);
			
			// check number of parameters
			Integer expectedCount = functionToParamCount.get(elemName);
			if (null == expectedCount) {
				throw new ParseException("Expected parameter count unknown for function '" + elemName + "'. This should not happen. Please report.");
			}
			if (0 == expectedCount && 1 == params.length && paramStr.isEmpty()) {
				// OK. Special case for functions without any parameter.
			}
			else if (0 == params.length && MidicaPLParser.FUNC_WAIT.equals(elemName)) {
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
		
		throw new ParseException("Don't know what to do with flow element '" + elemName + "'. This should not happen. Please report.");
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
		
		// note()
		if (MidicaPLParser.FUNC_NOTE.equals(funcName)) {
			int note = parser.parseNote(params[0]);
			flow.setNote(note);
			return;
		}
		
		// for all other functions we need the effect type
		int valueType = flow.getValueType(funcName);
		
		// on()/off() - boolean functions
		if (MidicaPLParser.FUNC_ON.equals(funcName) || MidicaPLParser.FUNC_OFF.equals(funcName)) {
			if (valueType != EffectFlow.TYPE_BOOLEAN && valueType != EffectFlow.TYPE_ANY) {
				throw new ParseException(String.format(Dict.get(Dict.ERROR_FUNC_TYPE_NOT_BOOL), funcName));
			}
			
			int value = MidicaPLParser.FUNC_ON.equals(funcName) ? 127 : 0;
			setValue(new int[] {value, value});
			
			return;
		}
		
		// non-boolean function for a boolean effect?
		if (EffectFlow.TYPE_BOOLEAN == valueType) {
			throw new ParseException(String.format(Dict.get(Dict.ERROR_FUNC_TYPE_BOOL), funcName));
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
			
			// don't allow (N)RPN
			int effectType = flow.getEffectType();
			if (EffectFlow.EFF_TYPE_RPN == effectType)
				throw new ParseException(String.format(Dict.get(Dict.ERROR_FL_CONT_RPN), funcName));
			if (EffectFlow.EFF_TYPE_NRPN == effectType)
				throw new ParseException(String.format(Dict.get(Dict.ERROR_FL_CONT_NRPN), funcName));
			
			applyContinousFunction(funcName, params);
			
			return;
		}
		
		throw new ParseException("Don't know what to do with function '" + funcName + "'. This should not happen. Please report.");
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
				if (0 == values.length) {
					SequenceCreator.addMessageChannelEffect(effectNum, channel, 0, 0, tick);
					return;
				}
				else if (2 == values.length) {
					
					// pitch bend?
					if (0xE0 == flow.getEffectNumber()) {
						// special case: set(0) for MSB only - set the LSB to 0 - to get no bend at all
						if (0x40 == values[1])
							SequenceCreator.addMessageChannelEffect(effectNum, channel, 0, values[1], tick);
						else
							// use the MSB in both positions - cover the whole range
							SequenceCreator.addMessageChannelEffect(effectNum, channel, values[1], values[1], tick);
					}
					else {
						// TODO: mono_at, poly_at
						// MSB first, LSB second
						SequenceCreator.addMessageChannelEffect(effectNum, channel, values[1], 0, tick);
					}
					return;
				}
				else if (3 == values.length) {
					// pitch bend: lsb first, msb second
					SequenceCreator.addMessageChannelEffect(effectNum, channel, values[2], values[1], tick);
					return;
				}
			}
			else if (EffectFlow.EFF_TYPE_CTRL == effectType) {
				if (0 == values.length) {
					SequenceCreator.addMessageCtrl(effectNum, channel, 0, tick);
					return;
				}
				else if (2 == values.length) {
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
				// TODO: implement
			}
			else if (EffectFlow.EFF_TYPE_NRPN == effectType) {
				// TODO: implement
			}
			else {
				throw new ParseException("Unknown effect type: " + effectType + ". This should not happen. Please report.");
			}
			throw new ParseException("Unknown effect type/number/byte-count combination: " + effectType + "/" + effectNum + "/" + values.length + ". This should not happen. Please report.");
		}
		catch (InvalidMidiDataException e) {
			throw new ParseException("Invalid MIDI data when trying to apply effect " + effectType + "/" + effectNum + ". This should not happen. Please report.");
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
		System.err.println("_____");
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
				System.err.println(setValues[0]); // TODO: delete
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
	 * Parses a numeric or percentage function parameter, checks it against the
	 * sound effect's min/max and returns the resulting value byte(s).
	 * 
	 * The returned array consists of the following bytes:
	 * 
	 * - first byte: the complete value
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
		
		// TODO: handle TYPE_NONE
		
		// get range of the sound effect
		int min = flow.getMin();
		int max = flow.getMax();
		
		// parse the parameter
		Integer value = null;
		try {
			Matcher m = intPattern.matcher(valueStr);
			if (m.matches()) {
				String intStr     = m.group(1);
				String percentStr = m.group(2);
				//String floatStr   = m.group(3); // TODO: something to make pitch bend easier to use
				if (intStr != null) {
					value = Integer.parseInt(intStr);
				}
				else if (percentStr != null) {
					float percent = Float.parseFloat(percentStr);
					
					// A negative percentage with a minimum of 0 should NOT evaluate to 0
					// but throw an exception instead.
					if (percent < 0 && 0 == min)
						throw new ParseException(String.format(Dict.get(Dict.ERROR_FUNC_VAL_LOWER_MIN), valueStr, min));
					
					// TODO: check
					if (percent < 0)
						// theoretically: value = percent * -min / 100
						value = (int) ((percent * -min * 10 - 100 * 5) / (100 * 10));
					else
						// theoretically: value = percent * max / 100
						value = (int) ((percent * max * 10 + 100 * 5) / (100 * 10));
				}
				else {
					throw new ParseException(Dict.get(Dict.ERROR_FUNC_NO_NUMBER) + valueStr);
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
		if (value > max)
			throw new ParseException(String.format(Dict.get(Dict.ERROR_FUNC_VAL_GREATER_MAX), valueStr, max));
		
		// adjust the actual MIDI value for signed types
		int valueType = flow.getValueType(valueStr);
		if (EffectFlow.TYPE_BYTE_SIGNED == valueType) {
			value += 64;
		}
		if (EffectFlow.TYPE_DOUBLE_SIGNED == valueType) {
			value += 8192;
		}
		if (EffectFlow.TYPE_MSB_SIGNED == valueType) {
			if (flow.isDouble())
				value += 8192;
			else
				value += 64;
		}
		
		// find out how many bytes are needed
		int byteCount = 1;
		if (EffectFlow.TYPE_DOUBLE == valueType || EffectFlow.TYPE_DOUBLE_SIGNED == valueType) {
			byteCount = 2;
		}
		else if (EffectFlow.TYPE_MSB == valueType || EffectFlow.TYPE_MSB_SIGNED == valueType) {
			if (flow.isDouble())
				byteCount = 2;
		}
		
		// handle 0 and 1 byte
		if (0 == byteCount) {
			return new int[] {};
		}
		else if (1 == byteCount) {
			return new int[] {value, value};
		}
		
		// handle 2 bytes
		int msb = value >> 7;
		int lsb = value & 0x7F;
		return new int[] {value, msb, lsb};
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
				String percentStr = m.group(2);
				float  periods;
				if (floatStr != null) {
					periods = Float.parseFloat(floatStr);
					if (periods < 0) {
						throw new ParseException(Dict.get(Dict.ERROR_FUNC_PERIODS_NEG) + valueStr);
					}
					if (percentStr != null) {
						periods /= 100;
					}
					return periods;
				}
			}
		}
		catch (NumberFormatException e) {
			throw new ParseException(Dict.get(Dict.ERROR_FUNC_PERIODS_NO_NUMBER) + valueStr);
		}
		
		throw new ParseException(Dict.get(Dict.ERROR_FUNC_PERIODS_NO_NUMBER) + valueStr);
	}
}
