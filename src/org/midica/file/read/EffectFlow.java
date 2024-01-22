/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file.read;

import java.util.HashMap;
import java.util.Map;

import org.midica.config.Dict;

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
	public static final int TYPE_MSB_HALFTONES = 7;  // default:  0 - 127, double:     0 - 127.99
	public static final int TYPE_BYTE          = 11; // 0 - 127
	public static final int TYPE_ANY           = 15; // anything that fits in 7 bits
	public static final int TYPE_NONE          = 17; // no value allowed (using 0 internally)
	
	// effect types
	public static final int EFF_TYPE_CHANNEL = 1;
	public static final int EFF_TYPE_CTRL    = 2;
	public static final int EFF_TYPE_RPN     = 3;
	public static final int EFF_TYPE_NRPN    = 4;
	
	// data structures
	private static final Map<Integer, Integer> channelMsgToType    = new HashMap<>();
	private static final Map<Integer, Integer> channelMsgToMin     = new HashMap<>();
	private static final Map<Integer, Integer> channelMsgToMax     = new HashMap<>();
	private static final Map<Integer, Integer> channelMsgToDefault = new HashMap<>(); // TODO: delete?
	private static final Map<Integer, Integer> ctrlToType          = new HashMap<>();
	private static final Map<Integer, Integer> ctrlToMin           = new HashMap<>();
	private static final Map<Integer, Integer> ctrlToMax           = new HashMap<>();
	private static final Map<Integer, Integer> ctrlToDefault       = new HashMap<>(); // TODO: delete?
	private static final Map<Integer, Integer> rpnToType           = new HashMap<>();
	private static final Map<Integer, Integer> rpnToMin            = new HashMap<>();
	private static final Map<Integer, Integer> rpnToMax            = new HashMap<>();
	private static final Map<Integer, Integer> rpnToDefault        = new HashMap<>(); // TODO: delete?
	private static final Map<Integer, Integer> nrpnToType          = new HashMap<>();
	private static final Map<Integer, Integer> nrpnToMin           = new HashMap<>();
	private static final Map<Integer, Integer> nrpnToMax           = new HashMap<>();
	private static final Map<Integer, Integer> nrpnToDefault       = new HashMap<>(); // TODO: delete?
	
	//////////////////////////////
	// flow fields
	//////////////////////////////
	
	private final int channel;
	private long    tick;
	private long    ticksPerAction;
	private int     effectType   = 0;
	private int     effectNumber = -1;
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
			throw new ParseException(Dict.get(Dict.ERROR_FL_EFF_ALREADY_SET));
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
	 * Determines if the current effect type supports half-tone-steps as parameters.
	 * 
	 * Examples:
	 * 
	 * - pitch bend
	 * - pitch bend range
	 * 
	 * @return **true** if half tone steps are supported, otherwise **false**.
	 */
	public boolean supportsHalfToneSteps() {
		
		if (EFF_TYPE_CHANNEL == effectType) {
			
			// pitch bend
			if (0xE0 == effectNumber)
				return true;
		}
		else if (EFF_TYPE_RPN == effectType) {
			
			// pitch bend range
			if (0x0000 == effectNumber)
				return true;
		}
		
		// TODO: how do we treat channel coarse/fine tuning?
		
		return false;
	}
	
	/**
	 * Determines if the current effect type supports ONLY half-tone-steps as parameters.
	 * That means, normal values are **not** accepted.
	 * 
	 * This is the case for the **pitch bend range**.
	 * 
	 * @return **true** if only half-tone-steps are accepted as parameters.
	 */
	public boolean mustUseHalfToneSteps() {
		
		// pitch bend range
		if (EFF_TYPE_RPN == effectType && 0x0000 == effectNumber) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Determines if the current effect needs a note to be set.
	 * 
	 * @return **true** if a note is needed, otherwise **false**.
	 */
	public boolean needsNote() {
		
		// poly_at?
		if (EFF_TYPE_CHANNEL == effectType && 0xA0 == effectNumber)
			return true;
		
		// port_ctrl?
		if (EFF_TYPE_CTRL == effectType && 0x54 == effectNumber)
			return true;
		
		return false;
	}
	
	/**
	 * Returns the pre-defined value for the current effect (with type=NONE or type=ANY).
	 * 
	 * For most effects with type NONE the predefined value is 0.
	 * 
	 * For most effects with type ANY the predefined value is 127.
	 * 
	 * Exceptions are:
	 * 
	 * - **port_ctrl** - Here the default is the configured note()
	 * - **mono_mode** - Here the default is 1
	 * 
	 * @param valueType  the effect's value type
	 * @return the default value to be set for **on()**.
	 * @throws FatalParseException
	 */
	public int getDefaultValueForOn(int valueType) throws FatalParseException {
		
		// NONE
		if (TYPE_NONE == valueType) {
			
			// port_ctrl?
			if (EFF_TYPE_CTRL == effectType && 0x54 == effectNumber)
				return note;
			
			return 0;
		}
		
		// ANY
		if (TYPE_ANY == valueType) {
			
			// mono_mode
			if (EFF_TYPE_CTRL == effectType && 0x7E == effectNumber)
				return 1;
			
			return 127;
		}
		
		throw new FatalParseException("Invalid value type '" + valueType + "' for getDefaultValueForOn().");
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
	 * @param note      note number (0 - 127)
	 * @param elemName  element name that caused the call (only used for error messages)
	 * @throws ParseException if the effect is not set or doesn't support a note.
	 */
	public void setNote(int note, String elemName) throws ParseException {
		
		// effect not yet set?
		if (effectNumber < 0)
			throw new ParseException(Dict.get(Dict.ERROR_FL_EFF_NOT_SET) + elemName);
		
		// check if effect type supports a note
		boolean isNoteSupported = false;
		if (EFF_TYPE_CHANNEL == effectType && 0xA0 == effectNumber) // poly_at
			isNoteSupported = true;
		else if (EFF_TYPE_CTRL == effectType && 0x54 == effectNumber) // portamento ctrl
			isNoteSupported = true;
		
		// note not supported?
		if (!isNoteSupported)
			throw new ParseException(Dict.get(Dict.ERROR_FL_NOTE_NOT_SUPP) + elemName);
		
		this.note = note;
	}
	
	/**
	 * Returns the MIDI note number of the currently set note().
	 * 
	 * Returns **-1** if no note has been set.
	 * 
	 * @return note number or **-1** if no note has been set.
	 */
	public int getNote() {
		return note;
	}
	
	/**
	 * Apppies a **double** flow element.
	 * 
	 * (Causes MSB/LSB effects to use both bytes.)
	 * @throws ParseException if no effect has been set yet or the effect doesn't support double.
	 */
	public void setDouble() throws ParseException {
		
		int valueType = getValueType(MidicaPLParser.FL_DOUBLE);
		if (TYPE_MSB == valueType || TYPE_MSB_SIGNED == valueType || TYPE_MSB_HALFTONES == valueType) {
			isDouble = true;
			return;
		}
		
		throw new ParseException(Dict.get(Dict.ERROR_FL_DOUBLE_NOT_SUPPORTED) + MidicaPLParser.FL_DOUBLE);
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
	 * Applies a **wait()** function call in the flow - without parameter.
	 */
	public void applyWait() {
		tick += ticksPerAction;
	}
	
	/**
	 * Applies a **wait(...)** function call in the flow - with length parameter.
	 * 
	 * @param lengthStr  note length string
	 * @throws ParseException if the length string is an invalid note lengh.
	 */
	public void applyWait(String lengthStr) throws ParseException {
		int length = MidicaPLParser.parseDuration(lengthStr);
		tick += length;
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
	 * Updates the current flow tick.
	 * 
	 * @param tick  the new MIDI tick for the flow
	 */
	public void setCurrentTick(long tick) {
		this.tick = tick;
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
	 * - {@link #TYPE_MSB_HALFTONES}
	 * - {@link #TYPE_BYTE}
	 * - {@link #TYPE_ANY}
	 * - {@link #TYPE_NONE}
	 * 
	 * @param elemName  element name that caused the call (only used for error messages)
	 * @return see above
	 * @throws ParseException if no effect has been set yet.
	 */
	public int getValueType(String elemName) throws ParseException {
		
		if (effectNumber < 0)
			throw new ParseException(Dict.get(Dict.ERROR_FL_EFF_NOT_SET) + elemName);
		
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
		if (null == valueType)
			throw new FatalParseException("Unknown effect number '" + effectNumber + "' for effect type '" + typeStr + "'.");
		
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
		
		// handle MSB / LSB
		if (isDouble && -64 == min) {
			return -8192;
		}
		
		if (null == min)
			throw new FatalParseException("Unknown min value for '" + effectType + "/" + effectNumber + ".");
		
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
		
		// handle MSB / LSB
		if (isDouble) {
			if (127 == max)
				max = 16383;
			if (63 == max)
				max = 8191;
			
			// special case: pitch bend range
			if (TYPE_MSB_HALFTONES == rpnToType.get(effectNumber))
				max = 128;
		}
		
		if (null == max)
			throw new FatalParseException("Unknown max value for '" + effectType + "/" + effectNumber + ".");
		
		return max;
	}
	
	/**
	 * Calculates and returns the default value for the current effect.
	 * 
	 * @return default value
	 * @throws ParseException if the default value can not be found
	 */
	public int getDefault() throws ParseException {
		
		Integer def = null;
		if (EFF_TYPE_CHANNEL == effectType) {
			def = channelMsgToDefault.get(effectNumber);
		}
		else if (EFF_TYPE_CTRL == effectType) {
			def = ctrlToDefault.get(effectNumber);
		}
		else if (EFF_TYPE_RPN == effectType) {
			def = rpnToDefault.get(effectNumber);
		}
		else if (EFF_TYPE_NRPN == effectType) {
			def = nrpnToDefault.get(effectNumber);
		}
		
		if (null == def) {
			throw new FatalParseException("Unknown default value for '" + effectType + "/" + effectNumber + ".");
		}
		
		return def;
	}
	
	/////////////////////////////////////////////////////
	// Define which effect can have which value(s).
	// This is independent from the configured syntax.
	/////////////////////////////////////////////////////
	static {
		
		/////////////////////////////
		// channel message-based effects
		/////////////////////////////
		channelMsgToType.put(0xA0, TYPE_BYTE);       // polyphonic after touch
		channelMsgToType.put(0xD0, TYPE_BYTE);       // monophonic after touch
		channelMsgToType.put(0xE0, TYPE_MSB_SIGNED); // pitch bend
		
		// min / max
		applyDefaultMinAndMax(channelMsgToType, channelMsgToDefault, channelMsgToMin, channelMsgToMax);
		
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
		ctrlToType.put(0x08, TYPE_MSB_SIGNED); // balance
		ctrlToType.put(0x0A, TYPE_MSB_SIGNED); // panorama
		ctrlToType.put(0x54, TYPE_NONE);       // portamento ctrl
		ctrlToType.put(0x58, TYPE_ANY);        // high resolution velocity prefix
		ctrlToType.put(0x60, TYPE_NONE);       // data increment
		ctrlToType.put(0x61, TYPE_NONE);       // data decrement
		ctrlToType.put(0x7A, TYPE_BOOLEAN);    // local control on/off
		ctrlToType.put(0x7E, TYPE_ANY);        // mono mode on
		
		// min / max
		applyDefaultMinAndMax(ctrlToType, ctrlToDefault, ctrlToMin, ctrlToMax);
		
		// exceptions for max
		ctrlToMax.put(0x7E, 16); // mono mode on - range: 0-16
		
		/////////////////////////////
		// (N)RPNs
		/////////////////////////////
		
		for (int i = 0x0000; i < 0x4000; i++) {
			rpnToType.put(i, TYPE_MSB);
			rpnToDefault.put(i, 0);
			nrpnToType.put(i, TYPE_MSB);
			nrpnToDefault.put(i, 0);
		}
		
		// exceptions (type)
		rpnToType.put(0x0000, TYPE_MSB_HALFTONES); // pitch bend range
		rpnToType.put(0x0001, TYPE_MSB_SIGNED);    // channel fine tuning
		rpnToType.put(0x0002, TYPE_MSB_SIGNED);    // channel coarse tuning
		rpnToType.put(0x7F7F, TYPE_NONE);          // reset RPN
		
		// min / max
		applyDefaultMinAndMax(rpnToType,  rpnToDefault,  rpnToMin,  rpnToMax);
		applyDefaultMinAndMax(nrpnToType, nrpnToDefault, nrpnToMin, nrpnToMax);
		
		// exceptions (default)
		rpnToDefault.put(0x0000, 0x0200); // pitch bend sensitivity
		rpnToDefault.put(0x0001, 0x4000); // channel fine tuning
		rpnToDefault.put(0x0002, 0x4000); // channel coarse tuning
		rpnToDefault.put(0x0005, 0x0040); // modulation depth range
	}
	
	/**
	 * Applies values for 'default', 'min' and 'max' values according to the effect type.
	 * 
	 * @param typeStructure     map containing the effect type
	 * @param defaultStructure  map to be filled with the default value
	 * @param minStructure      map to be filled with the default value
	 * @param maxStructure      map to be filled with the default value
	 */
	private static void applyDefaultMinAndMax(Map<Integer, Integer> typeStructure, Map<Integer, Integer> defaultStructure, Map<Integer, Integer> minStructure, Map<Integer, Integer> maxStructure) {
		
		for (int number : typeStructure.keySet()) {
			int type = typeStructure.get(number);
			defaultStructure.put(number, 0);
			if (TYPE_BYTE == type || TYPE_MSB == type || TYPE_MSB_HALFTONES == type || TYPE_ANY == type) {
				minStructure.put(number, 0);
				maxStructure.put(number, 127);
			}
			else if (TYPE_MSB_SIGNED == type) {
				minStructure.put(number, -64);
				maxStructure.put(number, 63);
			}
		}
	}
}
