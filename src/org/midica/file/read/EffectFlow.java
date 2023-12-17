/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file.read;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a sound effect flow.
 * It is mainly responsible to track the state of the flow.
 * 
 * @author Jan Trukenmüller
 */
public class EffectFlow {
	
	//////////////////////////////
	// static fields
	//////////////////////////////
	
	// value types
	public static final int TYPE_BOOLEAN       = 1;  // 0=off, 127=on
	public static final int TYPE_MSB           = 3;  // default:  0 - 127, double:     0 - 16383
	public static final int TYPE_MSB_SIGNED    = 5;  // default: -64 - 63, double: -8192 - 8191
	public static final int TYPE_BYTE          = 11; // 0 - 127
	public static final int TYPE_DOUBLE        = 13; // 0 - 16383
	public static final int TYPE_ANY           = 15; // anything that fits in 7 bits
	public static final int TYPE_NONE          = 17; // no value allowed (using 0 internally)
	public static final int TYPE_BYTE_SIGNED   = 19; // -64 - 63
	public static final int TYPE_DOUBLE_SIGNED = 21; // -8192 - 8191
	
	// effect types
	public static final int EFF_TYPE_CHANNEL = 1;
	public static final int EFF_TYPE_CTRL    = 2;
	public static final int EFF_TYPE_RPN     = 3;
	public static final int EFF_TYPE_NRPN    = 4;
	
	// data structures
	private static final Map<Integer, Integer> channelMsgToType = new HashMap<>();
	private static final Map<Integer, Integer> channelMsgToMin  = new HashMap<>();
	private static final Map<Integer, Integer> channelMsgToMax  = new HashMap<>();
	private static final Map<Integer, Integer> ctrlToType       = new HashMap<>();
	private static final Map<Integer, Integer> ctrlToMin        = new HashMap<>();
	private static final Map<Integer, Integer> ctrlToMax        = new HashMap<>();
	private static final Map<Integer, Integer> rpnToType        = new HashMap<>();
	private static final Map<Integer, Integer> rpnToMin         = new HashMap<>();
	private static final Map<Integer, Integer> rpnToMax         = new HashMap<>();
	private static final Map<Integer, Integer> rpnToDefault     = new HashMap<>();
	private static final Map<Integer, Integer> nrpnToType       = new HashMap<>();
	private static final Map<Integer, Integer> nrpnToMin        = new HashMap<>();
	private static final Map<Integer, Integer> nrpnToMax        = new HashMap<>();
	private static final Map<Integer, Integer> nrpnToDefault    = new HashMap<>();
	
	//////////////////////////////
	// flow fields
	//////////////////////////////
	
	private final int channel;
	private long    tick;
	private long    ticksPerAction;
	private int     effectType   = 0;
	private int     effectNumber = -1;
	private boolean mustKeep     = false;
	private boolean isDouble     = false;
	private int     note         = -1;
	
	/**
	 * Creates a new sound effect flow.
	 * 
	 * @param channel    MIDI channel
	 * @param lengthStr  initial length string
	 * @throws ParseException
	 */
	public EffectFlow(int channel, String lengthStr) throws ParseException {
		this.channel        = channel;
		this.tick           = MidicaPLParser.instruments.get(channel).getCurrentTicks();
		this.ticksPerAction = MidicaPLParser.parseDuration(lengthStr);
	}
	
	/**
	 * Returns the MIDI channel of the flow.
	 * 
	 * @return MIDI channel (0-15)
	 */
	public int getChannel() {
		return channel;
	}
	
	/**
	 * Sets the effect type and number.
	 * 
	 * @param type         {@link #EFF_TYPE_CHANNEL}, {@link #EFF_TYPE_CTRL}, {@link #EFF_TYPE_RPN} or {@link #EFF_TYPE_NRPN}
	 * @param effectNum    effect number (e.g. controller number, RPN number etc.)
	 * @throws ParseException if an effect has already been set.
	 */
	public void setEffect(int type, int effectNum) throws ParseException {
		if (effectType > 0) {
			throw new ParseException("Dict.get(Dict.ERROR_FL_EFF_ALREADY_SET)"); // TODO: Dict
		}
		effectType   = type;
		effectNumber = effectNum;
	}
	
	/**
	 * Determins the effect type of the current sound effect.
	 * 
	 * Returns one of the following values:
	 * 
	 * - {@link #EFF_TYPE_CHANNEL}
	 * - {@link #EFF_TYPE_CTRL}
	 * - {@link #EFF_TYPE_RPN}
	 * - {@link #EFF_TYPE_NRPN}
	 * 
	 * @return see above
	 */
	public int getEffectType() {
		return effectType;
	}
	
	/**
	 * Returns the effect number.
	 * 
	 * @return effect number, e.g. controller number, RPN number etc. 
	 */
	public int getEffectNumber() {
		return effectNumber;
	}
	
	/**
	 * Applies a **length(...)** function call in the flow.
	 * 
	 * @param lengthStr  The (note length) parameter of the length() call.
	 * @throws ParseException if the length string is an invalid note lengh.
	 */
	public void setLength(String lengthStr) throws ParseException {
		ticksPerAction = MidicaPLParser.parseDuration(lengthStr);
	}
	
	/**
	 * Sets the note in the flow.
	 * 
	 * @param note  note number (0 - 127)
	 */
	public void setNote(int note) throws ParseException {
		this.note = note;
	}
	
	/**
	 * Applies a **keep** flow element.
	 * 
	 * Causes the parser to keep the changed value after the end of the flow.
	 */
	public void setKeep() {
		mustKeep = true;
	}
	
	/**
	 * Returns the keep value.
	 * 
	 * Determins if the changed value is kept after the end of the flow.
	 * 
	 * @return the keep value
	 */
	public boolean mustKeep() {
		return mustKeep;
	}
	
	/**
	 * Apppies a **double** flow element.
	 * 
	 * (Causes MSB/LSB effects to use both bytes.)
	 * @throws ParseException if no effect has been set yet or the effect doesn't support double.
	 */
	public void setDouble() throws ParseException {
		
		int valueType = getValueType(MidicaPLParser.FL_DOUBLE);
		if (TYPE_MSB == valueType || TYPE_MSB_SIGNED == valueType) {
			isDouble = true;
			return;
		}
		
		throw new ParseException("Dict.get(Dict.ERROR_FL_DOUBLE_NOT_SUPPORTED)"); // TODO: Dict
	}
	
	/**
	 * Determins if double precision must be used.
	 * 
	 * @return **false** to use only MSB, **false** to use MSB/LSB.
	 */
	public boolean isDouble() {
		return isDouble;
	}
	
	/**
	 * Applies a **wait()** function call in the flow.
	 */
	public void applyWait() {
		tick += ticksPerAction;
	}
	
	/**
	 * Returns the current flow tick.
	 * 
	 * @return current tick
	 */
	public long getCurrentTick() {
		return tick;
	}
	
	/**
	 * Returns the tick that the flow will have after the next wait().
	 * 
	 * @return future flow tick
	 */
	public long getFutureTick() {
		return tick + ticksPerAction;
	}
	
	/**
	 * Determins the value type of the current sound effect.
	 * 
	 * Returns one of the following values:
	 * 
	 * - {@link #TYPE_BOOLEAN}
	 * - {@link #TYPE_MSB}
	 * - {@link #TYPE_MSB_SIGNED}
	 * - {@link #TYPE_BYTE}
	 * - {@link #TYPE_DOUBLE}
	 * - {@link #TYPE_ANY}
	 * - {@link #TYPE_NONE}
	 * - {@link #TYPE_BYTE_SIGNED}
	 * - {@link #TYPE_DOUBLE_SIGNED}
	 * 
	 * @param elemName  element name that caused the call (only used for error messages)
	 * @return see above
	 * @throws ParseException if no effect has been set yet.
	 */
	public int getValueType(String elemName) throws ParseException {
		
		if (effectNumber < 0) {
			throw new ParseException("Dict.get(Dict.ERROR_FL_EFF_NOT_SET)" + elemName); // TODO: Dict
		}
		
		Integer valueType = 0;
		String  typeStr   = "none";
		if (EFF_TYPE_CHANNEL == effectType) {
			valueType = channelMsgToType.get(effectNumber);
			typeStr   = "channel";
		}
		else if (EFF_TYPE_CTRL == effectType) {
			valueType = ctrlToType.get(effectNumber);
			typeStr   = "ctrl";
		}
		else if (EFF_TYPE_RPN == effectType) {
			valueType = rpnToType.get(effectNumber);
			typeStr   = "rpn";
		}
		else if (EFF_TYPE_NRPN == effectType) {
			valueType = nrpnToType.get(effectNumber);
			typeStr   = "nrpn";
		}
		
		// not found?
		if (null == valueType) {
			throw new ParseException("Unknown effect number '" + effectNumber + "' for effect type '" + typeStr + "'. This should not happen. Please report.");
		}
		
		return valueType;
	}
	
	/**
	 * Calculates and returns the minimum value for the current effect.
	 * 
	 * @return minimum value
	 * @throws ParseException if the minimum value can not be found
	 */
	public int getMin() throws ParseException {
		
		Integer min = null;
		if (EFF_TYPE_CHANNEL == effectType) {
			min = channelMsgToMin.get(effectNumber);
		}
		else if (EFF_TYPE_CTRL == effectType) {
			min = ctrlToMin.get(effectNumber);
		}
		else if (EFF_TYPE_RPN == effectType) {
			min = rpnToMin.get(effectNumber);
		}
		else if (EFF_TYPE_NRPN == effectType) {
			min = nrpnToMin.get(effectNumber);
		}
		
		if (null == min) {
			throw new ParseException("Unknown min value for '" + effectType + "/" + effectNumber + ". This should not happen. Please report.");
		}
		
		return min;
	}
	
	/**
	 * Calculates and returns the maximum value for the current effect.
	 * 
	 * @return maximum value
	 * @throws ParseException if the maximum value can not be found
	 */
	public int getMax() throws ParseException {
		
		Integer max = null;
		if (EFF_TYPE_CHANNEL == effectType) {
			max = channelMsgToMax.get(effectNumber);
		}
		else if (EFF_TYPE_CTRL == effectType) {
			max = ctrlToMax.get(effectNumber);
		}
		else if (EFF_TYPE_RPN == effectType) {
			max = rpnToMax.get(effectNumber);
		}
		else if (EFF_TYPE_NRPN == effectType) {
			max = nrpnToMax.get(effectNumber);
		}
		
		if (null == max) {
			throw new ParseException("Unknown max value for '" + effectType + "/" + effectNumber + ". This should not happen. Please report.");
		}
		
		return max;
	}
	
	/////////////////////////////////////////////////////
	// Define which effect can have which value(s).
	// This is independent from the configured syntax.
	/////////////////////////////////////////////////////
	static {
		
		/////////////////////////////
		// channel message-based effects
		/////////////////////////////
		channelMsgToType.put(0xA0, TYPE_BYTE);          // polyphonic after touch
		channelMsgToType.put(0xD0, TYPE_BYTE);          // monophonic after touch
		channelMsgToType.put(0xE0, TYPE_DOUBLE_SIGNED); // pitch bend
		
		// min / max
		applyDefaultMinAndMax(channelMsgToType, channelMsgToMin, channelMsgToMax);
		
		/////////////////////////////
		// continuous controllers
		/////////////////////////////
		
		// coarse controllers (MSB)
		for (int i = 0x00; i < 0x20; i++) {
			ctrlToType.put(i, TYPE_MSB);
		}
		
		// fine controllers (LSB)
		for (int i = 0x20; i < 0x40; i++) {
			ctrlToType.put(i, TYPE_BYTE);
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
		ctrlToType.put(0x08, TYPE_MSB_SIGNED); // portamento ctrl
		ctrlToType.put(0x0A, TYPE_MSB_SIGNED); // portamento ctrl
		ctrlToType.put(0x54, TYPE_ANY);        // portamento ctrl
		ctrlToType.put(0x58, TYPE_ANY);        // high resolution velocity prefix
		ctrlToType.put(0x60, TYPE_NONE);       // data increment
		ctrlToType.put(0x61, TYPE_NONE);       // data decrement
		ctrlToType.put(0x7A, TYPE_BOOLEAN);    // local control on/off
		ctrlToType.put(0x7E, TYPE_BYTE);       // mono mode on
		
		// min / max
		applyDefaultMinAndMax(ctrlToType, ctrlToMin, ctrlToMax);
		
		/////////////////////////////
		// (N)RPNs
		/////////////////////////////
		
		for (int i = 0x0000; i < 0x4000; i++) {
			rpnToType.put(i, TYPE_DOUBLE);
			rpnToDefault.put(i, 0);
			nrpnToType.put(i, TYPE_DOUBLE);
			nrpnToDefault.put(i, 0);
		}
		
		// min / max
		applyDefaultMinAndMax(rpnToType, rpnToMin, rpnToMax);
		applyDefaultMinAndMax(nrpnToType, nrpnToMin, nrpnToMax);
		
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
	
	/**
	 * Applies default min and max values according to the effect type.
	 * 
	 * @param typeStructure  map containing the effect type
	 * @param minStructure   map to be filled with the default minimum
	 * @param maxStructure   map to be filled with the default maximum
	 */
	private static void applyDefaultMinAndMax(Map<Integer, Integer> typeStructure, Map<Integer, Integer> minStructure, Map<Integer, Integer> maxStructure) {
		
		for (int number : typeStructure.keySet()) {
			int type = typeStructure.get(number);
			if (TYPE_BYTE == type || TYPE_MSB == type || TYPE_ANY == type) {
				minStructure.put(number, 0);
				maxStructure.put(number, 127);
			}
			else if (TYPE_BYTE_SIGNED == type || TYPE_MSB_SIGNED == type) {
				minStructure.put(number, -64);
				maxStructure.put(number, 63);
			}
			else if (TYPE_DOUBLE == type) {
				minStructure.put(number, 0);
				maxStructure.put(number, 16383);
			}
			else if (TYPE_DOUBLE_SIGNED == type) {
				minStructure.put(number, -8192);
				maxStructure.put(number, 8191);
			}
		}
	}
}
