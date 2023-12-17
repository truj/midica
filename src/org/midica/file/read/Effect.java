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
		functionToParamCount.put( MidicaPLParser.FUNC_WAIT,   0 );
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
		flowElementNames.add( MidicaPLParser.FL_KEEP   );
		flowElementNames.add( MidicaPLParser.FL_DOUBLE );
		
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
					throw new ParseException("Dict.get(Dict.ERROR_FL_UNKNOWN_ELEMENT)" + elemName); // TODO: Dict
				
				// starts with dot? - flow must be open
				if (dot.length() > 0) {
					if (null == flowStr)
						throw new ParseException("Dict.get(Dict.ERROR_FL_NOT_OPEN)"); // TODO: Dict
				}
				else {
					// open flow
					if (1 == elemCount)
						flow = new EffectFlow(channel, lengthStr);
					else
						throw new ParseException("Dict.get(Dict.ERROR_FL_MISSING_DOT)"); // TODO: Dict
				}
				
				// check number
				int number = -1;
				if (MidicaPLParser.FL_CTRL.equals(elemName)) {
					number = parseGenericNumber(numberStr, 0x7F);
				}
				else if (MidicaPLParser.FL_RPN.equals(elemName) || MidicaPLParser.FL_NRPN.equals(elemName)) {
					number = parseGenericNumber(numberStr, 0x3FFF);
				}
				else if (numberStr != null) {
					throw new ParseException("Dict.get(Dict.ERROR_FL_NUMBER_NOT_ALLOWED)" + elemName); // TODO: Dict
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
			String rest = flowStr.substring(lastMatchOffset);
			throw new ParseException("Dict.get(Dict.ERROR_FL_UNMATCHED_REMAINDER)" + rest); // TODO: Dict
		}
		
		// flow applied successfully
		return true;
	}
	
	/**
	 * Parses a generic controller or (N)RPN number, assigned in a flow.
	 * 
	 * @param numberStr  The number to be parsed.
	 * @param maxNum     The maximum allowed number.
	 * @return the parsed number
	 * @throws ParseException if the number cannot be parsed or is too high.
	 */
	private static int parseGenericNumber(String numberStr, int maxNum) throws ParseException {
		
		// no number provided?
		if (null == numberStr)
			throw new ParseException("Dict.get(Dict.ERROR_FL_NUMBER_MISSING)"); // TODO: Dict
		
		try {
			// parse the number
			int number = Integer.parseInt(numberStr);
			
			// number higher then allowed by the controller or (n)rpn?
			if (number > maxNum)
				throw new ParseException("Dict.get(Dict.ERROR_FL_NUMBER_TOO_HIGH)" + numberStr); // TODO: Dict
			
			// ok
			return number;
		}
		catch (NumberFormatException e) {
			
			// number exceeds integer limit
			throw new ParseException("Dict.get(Dict.ERROR_FL_NUMBER_TOO_HIGH)" + numberStr); // TODO: Dict
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
			if (number < 0) {
				throw new ParseException("Dict.get(Dict.ERROR_FL_NUMBER_REQUIRED)" + elemName); // TODO: Dict
			}
			else if (number > 127) {
				throw new ParseException("Dict.get(Dict.ERROR_FL_NUMBER_GT_127)"); // TODO: Dict
			}
		}
		else if (MidicaPLParser.FL_RPN.equals(elemName) || MidicaPLParser.FL_RPN.equals(elemName)) {
			if (number < 0) {
				throw new ParseException("Dict.get(Dict.ERROR_FL_NUMBER_REQUIRED)" + elemName); // TODO: Dict
			}
			else if (number > 16383) {
				throw new ParseException("Dict.get(Dict.ERROR_FL_NUMBER_GT_16383)"); // TODO: Dict
			}
		}
		else if (number > -1) {
			throw new ParseException("Dict.get(Dict.ERROR_FL_NUMBER_NOT_ALLOWED)" + elemName); // TODO: Dict
		}
		
		// check presence of params
		if (functionNames.contains(elemName)) {
			if (paramStr == null) {
				throw new ParseException("Dict.get(Dict.ERROR_FL_PARAMS_REQUIRED)" + elemName); // TODO: Dict
			}
		}
		else {
			if (null != paramStr) {
				throw new ParseException("Dict.get(Dict.ERROR_FL_PARAMS_NOT_ALLOWED)" + elemName); // TODO: Dict
			}
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
			String[] params = paramStr.split(Pattern.quote(MidicaPLParser.PARAM_SEPARATOR), -1);
			
			// check number of parameters
			Integer expectedCount = functionToParamCount.get(elemName);
			if (null == expectedCount) {
				throw new ParseException("Expected parameter count unknown for function '" + elemName + "'. This should not happen. Please report.");
			}
			if (0 == expectedCount && 1 == params.length && paramStr.isEmpty()) {
				// OK. Special case for functions without any parameter.
			}
			else {
				if (params.length != expectedCount) {
					throw new ParseException(
						String.format("Dict.get(Dict.ERROR_FL_WRONG_PARAM_NUM)", elemName, expectedCount, params.length)
					); // TODO: Dict
				}
				
				// don't allow empty parameters
				for (String param : params) {
					if (param.isEmpty())
						throw new ParseException("Dict.get(Dict.ERROR_FL_EMPTY_PARAM)" + paramStr); // TODO: Dict
				}
			}
			
			// call function
			applyFunction(elemName, params);
			
			return;
		}
		
		// other flow elements
		if (MidicaPLParser.FL_KEEP.equals(elemName)) {
			flow.setKeep();
			return;
		}
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
				throw new ParseException("Dict.get(Dict.ERROR_FUNC_TYPE_NOT_BOOL)" + funcName); // TODO: Dict
			}
			
			int value = MidicaPLParser.FUNC_ON.equals(funcName) ? 127 : 0;
			setValue(new int[] {value, value});
			
			return;
		}
		
		// non-boolean function for a boolean effect?
		if (EffectFlow.TYPE_BOOLEAN == valueType) {
			throw new ParseException("Dict.get(Dict.ERROR_FUNC_TYPE_BOOL)" + funcName); // TODO: Dict
		}
		
		// set()
		if (MidicaPLParser.FUNC_SET.equals(funcName)) {
			int[] values = parseIntParam(params[0]);
			setValue(values);
			return;
		}
		
		// continuous functions
		if (MidicaPLParser.FUNC_LINE.equals(funcName)
			|| MidicaPLParser.FUNC_SIN.equals(funcName) || MidicaPLParser.FUNC_COS.equals(funcName)
			|| MidicaPLParser.FUNC_NSIN.equals(funcName) || MidicaPLParser.FUNC_NCOS.equals(funcName)) {
			
			// TODO: implement
			
			return;
		}
		
		throw new ParseException("Don't know what to do with function '" + funcName + "'. This should not happen. Please report.");
	}
	
	/**
	 * 
	 * 
	 * @param values
	 * @throws ParseException
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
					SequenceCreator.addMessageChannelEffect(effectNum, channel, values[1], 0, tick);
					return;
				}
				else if (3 == values.length) {
					SequenceCreator.addMessageChannelEffect(effectNum, channel, values[1], values[2], tick);
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
	
	private static void setByte() {
		
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
						throw new ParseException("Dict.get(Dict.ERROR_FUNC_VAL_LOWER_MIN)" + min); // TODO: Dict
					
					// TODO: check
					if (percent < 0)
						// theoretically: value = percent * min / 100
						value = (int) ((percent * -min * 10 + 100 * 5) / (100 * 10));
					else
						// theoretically: value = percent * max / 100
						value = (int) ((percent * max * 10 + 100 * 5) / (100 * 10));
				}
				else {
					throw new ParseException("Dict.get(Dict.ERROR_FUNC_NO_INT)" + valueStr); // TODO: Dict
				}
			}
		}
		catch (NumberFormatException e) {
			throw new ParseException("Dict.get(Dict.ERROR_FUNC_NO_NUMBER)" + valueStr); // TODO: Dict
		}
		if (null == value)
			throw new ParseException("Dict.get(Dict.ERROR_FUNC_NO_NUMBER)" + valueStr); // TODO: Dict
		
		// check value against min / max
		if (value < min)
			throw new ParseException("Dict.get(Dict.ERROR_FUNC_VAL_LOWER_MIN)" + min); // TODO: Dict
		if (value > max)
			throw new ParseException("Dict.get(Dict.ERROR_FUNC_VAL_GREATER_MAX)" + max); // TODO: Dict
		
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
}
