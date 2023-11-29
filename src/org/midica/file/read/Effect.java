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

import org.midica.config.Dict;

/**
 * This class is used to parse/create sound effects.
 * It is used by the MidicaPL parser.
 * 
 * @author Jan Trukenmüller
 */
public class Effect {
	
	private static final int TYPE_BOOLEAN       = 1;  // 0=off, 127=on
	private static final int TYPE_MSB           = 3;  // default: 0 - 127, double: 0 - 16383
	private static final int TYPE_LSB           = 5;  // default: 0 - 127, double: 0 - 16383
	private static final int TYPE_BYTE          = 7;  // 0 - 127
	private static final int TYPE_DOUBLE        = 9;  // 0 - 16383
	private static final int TYPE_ANY           = 11; // anything that fits in 7 bits
	private static final int TYPE_NONE          = 13; // no value allowed (using 0 internally)
	private static final int TYPE_BYTE_SIGNED   = 17; // -64 - 63
	private static final int TYPE_DOUBLE_SIGNED = 19; // -8192 - 8191
	
	private static final Map<Integer, Integer> channelMsgToType = new HashMap<>();
	private static final Map<Integer, Integer> ctrlToType       = new HashMap<>();
	private static final Map<Integer, Integer> rpnToType        = new HashMap<>();
	private static final Map<Integer, Integer> rpnToDefault     = new HashMap<>();
	private static final Map<Integer, Integer> nrpnToType       = new HashMap<>();
	private static final Map<Integer, Integer> nrpnToDefault    = new HashMap<>();
	
	private static EffectPipeline currentPipeline = null;
	
	private static Set<String> functionNames;
	private static Set<String> effectNames;
	
	private static Map<String, Integer> channelMsgNameToNumber;
	private static Map<String, Integer> ctrlNameToNumber;
	private static Map<String, Integer> rpnNameToNumber;
	private static Set<String>          pipelineElementNames;
	
	private static Pattern pipelinePattern   = null;
	private static Pattern anyCallPattern    = null;
	private static Pattern assignCallPattern = null;
	private static Pattern noteOrRestPattern = null;
	private static Pattern paramCallPattern  = null;
	
	/**
	 * Initializes data structures and regex patterns based on the current syntax.
	 * Called by {@link MidicaPLParser} if the DEFINE parsing run is finished.
	 */
	public static void init() {
		functionNames          = new HashSet<>();
		effectNames            = new HashSet<>();
		channelMsgNameToNumber = new HashMap<>();
		ctrlNameToNumber       = new HashMap<>();
		rpnNameToNumber        = new HashMap<>();
		pipelineElementNames   = new HashSet<>();
		
		functionNames.add( MidicaPLParser.FUNC_SET    );
		functionNames.add( MidicaPLParser.FUNC_ON     );
		functionNames.add( MidicaPLParser.FUNC_OFF    );
		functionNames.add( MidicaPLParser.FUNC_LINE   );
		functionNames.add( MidicaPLParser.FUNC_SIN    );
		functionNames.add( MidicaPLParser.FUNC_COS    );
		functionNames.add( MidicaPLParser.FUNC_NSIN   );
		functionNames.add( MidicaPLParser.FUNC_NCOS   );
		functionNames.add( MidicaPLParser.FUNC_LENGTH );
		functionNames.add( MidicaPLParser.FUNC_WAIT   );
		functionNames.add( MidicaPLParser.FUNC_NOTE   );
		
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
		
		// pipeline element names (effect names, function names, or other possible pipeline elements)
		for (String effectName : effectNames) {
			pipelineElementNames.add(effectName);
		}
		for (String functionName : functionNames) {
			pipelineElementNames.add(functionName);
		}
		pipelineElementNames.add( MidicaPLParser.PL_KEEP );
		pipelineElementNames.add( MidicaPLParser.PL_DOUBLE );
		pipelineElementNames.add( MidicaPLParser.PL_CTRL );
		pipelineElementNames.add( MidicaPLParser.PL_RPN );
		pipelineElementNames.add( MidicaPLParser.PL_NRPN );
		
		// compile regex patterns
		String pipelineRegex
			= "\\G"                                                                // end of previous match
			+"(^|" + Pattern.quote(MidicaPLParser.PL_DOT) + ")"                    // begin or '.'
			+ "(\\w+)(?:" + Pattern.quote(MidicaPLParser.PL_ASSIGNER) + "(\\d+))?" // pipeline element without parameters
			+ "(?:"
			+ Pattern.quote(MidicaPLParser.PARAM_OPEN)                             // (
			+ "(\\S+?)"                                                            // function parameters
			+ Pattern.quote(MidicaPLParser.PARAM_CLOSE)                            // )
			+ ")?";                                                                // optional
		pipelinePattern   = Pattern.compile(pipelineRegex);
		noteOrRestPattern = Pattern.compile("^[0-9]|" + Pattern.quote(MidicaPLParser.REST));
	}
	
	/**
	 * Parses and applies the given pipeline string, if it really looks like a pipeline.
	 * 
	 * Usually it does **not** look like a pipeline, if it is a note, rest or pattern.
	 * 
	 * @param channel    MIDI channel
	 * @param pipeline   Pipeline string, note, rest or pattern.
	 * @param lengthStr  The note length string of the channel command.
	 * @return **true**, if the pipeline can be parsed, or **false** if it does not look like a pipeline.
	 * @throws ParseException if it looks like a pipeline but cannot be parsed.
	 */
	public static boolean applyPipelineIfPossible(int channel, String pipeline, String lengthStr) throws ParseException {
		
		// looks like normal note or rest?
		if (noteOrRestPattern.matcher(pipeline).find()) {
			return false;
		}
		
		// try to match an effect pipeline
		boolean looksLikePipeline = false;
		int     elemCount         = 0;
		Matcher m = pipelinePattern.matcher(pipeline);
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
				
				// pipeline or something else?
				if (null == elemName) {
					return false;
				}
				if (pipelineElementNames.contains(elemName))
					looksLikePipeline = true;
				else
					throw new ParseException("Dict.get(Dict.ERROR_UNKNOWNN_PL_ELEMENT)" + elemName); // TODO: Dict
				
				// starts with dot? - pipeline must be open
				if (dot.length() > 0) {
					if (null == currentPipeline)
						throw new ParseException("Dict.get(Dict.ERROR_PL_NOT_OPEN)"); // TODO: Dict
				}
				else {
					// open pipeline
					if (1 == elemCount)
						currentPipeline = new EffectPipeline(channel, lengthStr);
					else
						throw new ParseException("Dict.get(Dict.ERROR_PL_MISSING_DOT)"); // TODO: Dict
				}
				
				// check number
				int number = -1;
				if (MidicaPLParser.PL_CTRL.equals(elemName)) {
					number = parseGenericNumber(numberStr, 0x7F);
				}
				else if (MidicaPLParser.PL_RPN.equals(elemName) || MidicaPLParser.PL_NRPN.equals(elemName)) {
					number = parseGenericNumber(numberStr, 0x3FFF);
				}
				else if (numberStr != null) {
					throw new ParseException("Dict.get(Dict.ERROR_PL_NUMBER_NOT_ALLOWED)" + elemName); // TODO: Dict
				}
				
				// apply the pipeline element
				applyPipelineElement(elemName, number, paramStr);
			}
		}
		catch (ParseException e) {
			if (!looksLikePipeline)
				return false;
			
			throw e;
		}
		
		// probably no match at all - not a pipeline
		if (!looksLikePipeline)
			return false;
		
		// unmatched characters left?
		if (lastMatchOffset != pipeline.length()) {
			String rest = pipeline.substring(lastMatchOffset);
			throw new ParseException("Dict.get(Dict.ERROR_PL_UNMATCHED_REMAINDER)" + rest); // TODO: Dict
		}
		
		// pipeline applied successfully
		return true;
	}
	
	/**
	 * Parses a generic controller or (N)RPN number, assigned in a pipeline.
	 * 
	 * @param numberStr  The number to be parsed.
	 * @param maxNum     The maximum allowed number.
	 * @return the parsed number
	 * @throws ParseException if the number cannot be parsed or is too high.
	 */
	private static int parseGenericNumber(String numberStr, int maxNum) throws ParseException {
		
		// no number provided?
		if (null == numberStr)
			throw new ParseException("Dict.get(Dict.ERROR_PL_NUMBER_MISSING)"); // TODO: Dict
		
		try {
			// parse the number
			int number = Integer.parseInt(numberStr);
			
			// number higher then allowed by the controller or (n)rpn?
			if (number > maxNum)
				throw new ParseException("Dict.get(Dict.ERROR_PL_NUMBER_TOO_HIGH)" + numberStr); // TODO: Dict
			
			// ok
			return number;
		}
		catch (NumberFormatException e) {
			
			// number exceeds integer limit
			throw new ParseException("Dict.get(Dict.ERROR_PL_NUMBER_TOO_HIGH)" + numberStr); // TODO: Dict
		}
	}
	
	// TODO: document
	private static void applyPipelineElement(String elemName, int number, String paramStr) throws ParseException {
		// TODO: implement
	}
	
	/////////////////////////////////////////////////////
	// Define which effect can have which value(s).
	// This is independent from the configured syntax.
	/////////////////////////////////////////////////////
	static {
		
		/////////////////////////////
		// channel message-based effects
		/////////////////////////////
		channelMsgToType.put(0xA0, TYPE_BYTE);
		channelMsgToType.put(0xD0, TYPE_BYTE);
		channelMsgToType.put(0xE0, TYPE_DOUBLE);
		
		/////////////////////////////
		// continuous controllers
		/////////////////////////////
		
		// coarse controllers (MSB)
		for (int i = 0x00; i < 0x20; i++) {
			ctrlToType.put(i, TYPE_MSB);
		}
		
		// fine controllers (LSB)
		for (int i = 0x20; i < 0x40; i++) {
			ctrlToType.put(i, TYPE_LSB);
		}
		
		// boolean (switch) controllers
		for (int i = 0x40; i < 0x46; i++) {
			ctrlToType.put(i, TYPE_BOOLEAN);
		}
		
		// simple controllers (values from 0 to 127)
		for (int i = 0x46; i < 0x50; i++) {
			ctrlToType.put(i, TYPE_BYTE);
		}
		
		// general purpose or undefined (1 byte)
		for (int i = 0x50; i < 0x5B; i++) {
			ctrlToType.put(i, TYPE_ANY);
		}
		
		// level controllers (1 byte)
		for (int i = 0x5B; i < 0x66; i++) {
			ctrlToType.put(i, TYPE_BYTE);
		}
		
		// undefined
		for (int i = 0x66; i < 0x78; i++) {
			ctrlToType.put(i, TYPE_ANY);
		}
		
		// channel mode messages
		for (int i = 0x78; i < 0x80; i++) {
			ctrlToType.put(i, TYPE_NONE);
		}
		
		// exceptions for the above ranges
		ctrlToType.put(0x54, TYPE_ANY);     // portamento ctrl
		ctrlToType.put(0x58, TYPE_ANY);     // high resolution velocity prefix
		ctrlToType.put(0x60, TYPE_NONE);    // data increment
		ctrlToType.put(0x61, TYPE_NONE);    // data decrement
		ctrlToType.put(0x7A, TYPE_BOOLEAN); // local control on/off
		ctrlToType.put(0x7E, TYPE_BYTE);    // mono mode on
		
		/////////////////////////////
		// (N)RPNs
		/////////////////////////////
		
		for (int i = 0x0000; i < 0x4000; i++) {
			rpnToType.put(i, TYPE_DOUBLE);
			rpnToDefault.put(i, 0);
			nrpnToType.put(i, TYPE_DOUBLE);
			nrpnToDefault.put(i, 0);
		}
		
		// exceptions (default)
		rpnToDefault.put(0x0000, 0x0200); // pitch bend sensitivity
		rpnToDefault.put(0x0001, 0x4000); // channel fine tuning
		rpnToDefault.put(0x0002, 0x4000); // channel coarse tuning
		rpnToDefault.put(0x0005, 0x0040); // modulation depth range
		
		// exceptions (type)
		rpnToType.put(0x0001, TYPE_DOUBLE_SIGNED); // channel fine tuning
		rpnToType.put(0x0002, TYPE_DOUBLE_SIGNED); // channel coarse tuning
		rpnToType.put(0x7F7F, TYPE_NONE);          // reset RPN
	}
}
