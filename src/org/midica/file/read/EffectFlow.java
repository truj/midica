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
import java.util.Set;

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
	public static final int TYPE_BOOLEAN     = 1;  // 0=off, 127=on
	public static final int TYPE_MSB         = 3;  // default:  0 - 127, double:     0 - 16383
	public static final int TYPE_MSB_SIGNED  = 5;  // default: -64 - 63, double: -8192 - 8191
	public static final int TYPE_BYTE        = 11; // 0 - 127
	public static final int TYPE_BYTE_SIGNED = 13; // -64 - 63
	public static final int TYPE_BYTE_FLEX   = 14; // signed (-64 - 63) or unsigned (0 - 127)
	public static final int TYPE_ANY         = 15; // anything that fits in 7 bits
	public static final int TYPE_NONE        = 17; // no value allowed (using 0 internally)
	
	// effect types
	public static final int EFF_TYPE_CHANNEL = 1;
	public static final int EFF_TYPE_CTRL    = 2;
	public static final int EFF_TYPE_RPN     = 3;
	public static final int EFF_TYPE_NRPN    = 4;
	public static final int EFF_TYPE_SYSEX   = 5;
	
	// supported functions
	public static final int FUNC_TYPE_ON     = 1;
	public static final int FUNC_TYPE_OFF    = 3;
	public static final int FUNC_TYPE_SET    = 5;
	public static final int FUNC_TYPE_CONT   = 7; // continuous (line, sin, cos, ...)
	public static final int FUNC_TYPE_NOTE   = 9;
	public static final int FUNC_TYPE_SRC    = 11;
	public static final int FUNC_TYPE_DEST   = 13;
	
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
	private static final Map<Integer, Integer> sxCtrlDestToType    = new HashMap<>();
	private static final Map<Integer, Integer> sxCtrlDestToMin     = new HashMap<>();
	private static final Map<Integer, Integer> sxCtrlDestToMax     = new HashMap<>();
	private static final Map<Integer, Integer> sxCtrlDestToDefault = new HashMap<>(); // TODO: delete?
	
	// controller destination
	private List<Integer> ctrlDestSrc  = new ArrayList<>();
	private List<Integer> ctrlDestDest = new ArrayList<>();
	private int currentCtrlDestination = -1;
	
	//////////////////////////////
	// flow fields
	//////////////////////////////
	
	private final int channel;
	private long    tick;
	private long    ticksPerAction;
	private int     effectType   = 0;
	private int     effectNumber = -1;
	private boolean isPending    = true;
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
	 * @param type         {@link #EFF_TYPE_CHANNEL}, {@link #EFF_TYPE_CTRL}, {@link #EFF_TYPE_RPN} or {@link #EFF_TYPE_NRPN} or {@link #EFF_TYPE_SYSEX}
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
	 * - {@link #EFF_TYPE_SYSEX}
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
	 * Marks the flow as pending or not pending.
	 * 
	 * "Pending" means that the last called function was something like length(), wait(), note(), src(), dest() etc.
	 * 
	 * "Not pending" means that the last called function was something like set(), on(), off(), line(), sin(), etc.
	 * 
	 * @param isPending  **true** for pending, **false** for not pending
	 */
	public void setPending(boolean isPending) {
		this.isPending = isPending;
	}
	
	/**
	 * Indicates if the flow is pending.
	 * 
	 * "Pending" means that the last called function was something like length(), wait(), note(), src(), dest() etc.
	 * 
	 * "Not pending" means that the last called function was something like set(), on(), off(), line(), sin(), etc.
	 * 
	 * @return **true** if the flow is pending, otherwise: **false**.
	 */
	public boolean isPending() {
		return isPending;
	}
	
	/**
	 * Return all functions that are supported by the current effect type.
	 * 
	 * Each returned element is one of the following:
	 * 
	 * - {@link #FUNC_TYPE_ON}
	 * - {@link #FUNC_TYPE_OFF}
	 * - {@link #FUNC_TYPE_SET}
	 * - {@link #FUNC_TYPE_CONT}
	 * - {@link #FUNC_TYPE_NOTE}
	 * - {@link #FUNC_TYPE_SRC}
	 * - {@link #FUNC_TYPE_DEST}
	 * 
	 * @param elemName  element name that caused the call (only used for error messages)
	 * @return the supported functions (see above)
	 * @throws ParseException
	 */
	public Collection<Integer> getSupportedFunctions(String elemName) throws ParseException {
		
		Set<Integer> functions = new HashSet<>();
		int valueType = getValueType(elemName);
		
		// special case: mono_mode - allow only on() and set()
		if (EFF_TYPE_CTRL == effectType && 0x7E == effectNumber) {
			functions.add(FUNC_TYPE_ON);
			functions.add(FUNC_TYPE_SET);
			return functions;
		}
		
		// special case for controller destination
		if (EFF_TYPE_SYSEX == effectType && 0x7F0900 == effectNumber) {
			functions.add(FUNC_TYPE_SRC);
			functions.add(FUNC_TYPE_DEST);
			functions.add(FUNC_TYPE_ON);
			return functions;
		}
		
		// normal cases
		if (TYPE_MSB == valueType || TYPE_MSB_SIGNED == valueType
				|| TYPE_BYTE == valueType || TYPE_BYTE_SIGNED == valueType
				|| TYPE_BYTE_FLEX == valueType || TYPE_ANY == valueType) {
			functions.add(FUNC_TYPE_SET);
			functions.add(FUNC_TYPE_CONT);
		}
		if (TYPE_BOOLEAN == valueType || TYPE_ANY == valueType) {
			functions.add(FUNC_TYPE_ON);
			functions.add(FUNC_TYPE_OFF);
		}
		if (TYPE_NONE == valueType) {
			functions.add(FUNC_TYPE_ON);
		}
		
		// special cases for note()
		if (EFF_TYPE_CHANNEL == effectType && 0xA0 == effectNumber) // poly_at
			functions.add(FUNC_TYPE_NOTE);
		else if (EFF_TYPE_CTRL == effectType && 0x54 == effectNumber) // portamento ctrl
			functions.add(FUNC_TYPE_NOTE);
		
		// special case for (N)RPNs: don't allow continuous functions
		if (EFF_TYPE_RPN == effectType || EFF_TYPE_NRPN == effectType)
			functions.remove(FUNC_TYPE_CONT);
		
		return functions;
	}
	
	/**
	 * Determines if the current effect type supports half-tone-steps as parameters.
	 * 
	 * Examples:
	 * 
	 * - pitch bend
	 * - pitch bend range
	 * - channel fine tuning
	 * - channel coarse tuning
	 * - controller destination: pitch control
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
			
			// channel fine tuning
			if (0x0001 == effectNumber)
				return true;
			
			// channel coarse tuning
			if (0x0002 == effectNumber)
				return true;
		}
		
		// controller destination
		else if (EFF_TYPE_SYSEX == effectType && 0x7F0900 == effectNumber) {
			
			// pitch control
			if (0x00 == currentCtrlDestination)
				return true;
		}
		
		return false;
	}
	
	/**
	 * Determines if the given value type supports signed (+/-) parameters.
	 * 
	 * @param valueType  the effect's value type
	 * @return **true** for types that support signed parameters, otherwise: **false**.
	 */
	public boolean supportsSign(int valueType) {
		
		// exception for mono_mode (type ANY)
		if (EFF_TYPE_CTRL == effectType && 0x7E == effectNumber)
			return false;
		
		if (requiresSign(valueType))
			return true;
		if (TYPE_BYTE_FLEX == valueType)
			return true;
		if (TYPE_ANY == valueType)
			return true;
		
		return false;
	}
	
	/**
	 * Determines if the given value type requires signed (+/-) parameters.
	 * 
	 * @param valueType  the effect's value type
	 * @return **true** for types that require signed parameters, otherwise: **false**.
	 */
	public boolean requiresSign(int valueType) {
		
		if (TYPE_MSB_SIGNED == valueType)
			return true;
		if (TYPE_BYTE_SIGNED == valueType)
			return true;
		
		return false;
	}
	
	/**
	 * Determines if the current effect type supports percentage values as parameters.
	 * 
	 * This is the case for most effects. Notable exceptions:
	 * 
	 * - pitch bend range
	 * - channel coarse tuning
	 * - mono mode
	 * - controller destination: pitch control
	 * 
	 * @return **true** if percentage values are allowed, otherwise: **false**.
	 */
	public boolean supportsPercentage() {
		
		// mono_mode
		if (EFF_TYPE_CTRL == effectType && 0x7E == effectNumber)
			return false;
		
		if (EFF_TYPE_RPN == effectType) {
			
			// pitch bend range
			if (0x0000 == effectNumber)
				return false;
			
			// channel coarse tuning
			if (0x0002 == effectNumber)
				return false;
		}
		
		// controller destination
		else if (EFF_TYPE_SYSEX == effectType && 0x7F0900 == effectNumber) {
			
			// pitch control
			if (0x00 == currentCtrlDestination)
				return false;
		}
		
		// regular cases
		return true;
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
	 * @param note  note number (0 - 127)
	 */
	public void setNote(int note) {
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
		
		// normal case: double allowed for all other MSB types
		if (TYPE_MSB == valueType || TYPE_MSB_SIGNED == valueType) {
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
	 * Sets the next controller destination in a controller destination effect.
	 * 
	 * (One controller destination effect can have only one source but several destinations.)
	 * 
	 * @param dest  the destination
	 */
	public void setCurrentCtrlDest(int dest) {
		currentCtrlDestination = dest;
	}
	
	/**
	 * Sets the source for a controller destination effect.
	 * 
	 * The source can have 2 or 3 bytes.
	 * 
	 * - first byte: 0x01, 0x02, 0x03 (mono_at, poly_at, cc)
	 * - second byte: channel
	 * - third byte: cc number (only available if the first byte is 0x03)
	 * 
	 * @param values    the values
	 * @param elemName
	 * @throws ParseException
	 */
	public void setSource(int[] values, String elemName) throws ParseException {
		
		if (!ctrlDestSrc.isEmpty())
			throw new ParseException(Dict.get(Dict.ERROR_FUNC_CD_SRC_ALREADY_SET) + elemName);
		
		for (int val : values)
			ctrlDestSrc.add(val);
	}
	
	/**
	 * Remembers the received bytes so that they can
	 * be put into a controller destination message later.
	 * 
	 * @param values  first byte: destination type (0x00-0x05); second byte: range (0x00-0x7F)
	 */
	public void putDestination(int[] values) {
		ctrlDestDest.add(values[0]);
		ctrlDestDest.add(values[1]);
	}
	
	/**
	 * Constructs and returns the content of a SysEx message with the meaning 'controller destination'.
	 * 
	 * @param elemName  the called function (only needed for error messages)
	 * @return the message content
	 * @throws ParseException if source or destination have not yet been set
	 */
	public byte[] getControllerDestination(String elemName) throws ParseException {
		
		// checks
		if (ctrlDestSrc.isEmpty())
			throw new ParseException(Dict.get(Dict.ERROR_FUNC_CD_SRC_NOT_SET) + elemName);
		if (ctrlDestDest.isEmpty())
			throw new ParseException(Dict.get(Dict.ERROR_FUNC_CD_DEST_NOT_SET) + elemName);
		
		// construct result
		int length = 5 + ctrlDestSrc.size() + ctrlDestDest.size();
		byte[] result = new byte[length];
		int i = 0;
		result[i++] = (byte) 0xF0; // sysex
		result[i++] =        0x7F; // universal real time
		result[i++] =        0x7F; // device ID: all devices
		result[i++] =        0x09; // sub ID 1: controller destination
		for (int val : ctrlDestSrc) {
			result[i++] = (byte) val; // sub ID 2 + channel + (optional) controller
		}
		for (int val : ctrlDestDest) {
			result[i++] = (byte) val; // pairs of destination + range
		}
		result[i++] = (byte) 0xF7;    // end of sysex
		
		return result;
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
	 * - {@link #TYPE_BYTE}
	 * - {@link #TYPE_BYTE_SIGNED}
	 * - {@link #TYPE_BYTE_FLEX}
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
		else if (EFF_TYPE_SYSEX == effectType && 0x7F0900 == effectNumber) {
			
			// source not yet set?
			if (currentCtrlDestination < 0)
				return EFF_TYPE_SYSEX;
			
			valueType = sxCtrlDestToType.get(currentCtrlDestination);
			typeStr   = "sysex (ctrl dest)";
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
		
		// special case: controller destination
		if (EFF_TYPE_SYSEX == effectType && 0x7F0900 == effectNumber) {
			min = sxCtrlDestToMin.get(currentCtrlDestination);
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
		}
		
		// special case: controller destination
		if (EFF_TYPE_SYSEX == effectType && 0x7F0900 == effectNumber) {
			max = sxCtrlDestToMax.get(currentCtrlDestination);
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
		
		// sound variation: 0 to 127
		ctrlToType.put(0x46, TYPE_BYTE);
		
		// signed simple controllers (values from -64 to 63)
		for (int i = 0x47; i < 0x4F; i++) {
			ctrlToType.put(i, TYPE_BYTE_SIGNED);
		}
		
		// simple controllers (values from 0 to 127)
		for (int i = 0x4F; i < 0x50; i++) {
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
		
		// exceptions for the above types
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
		
		// exceptions for default
		ctrlToDefault.put(0x07, 0x64); // volume: 100
		ctrlToDefault.put(0x0B, 0x7F); // vol (expression)
		ctrlToDefault.put(0x5B, 0x28); // reverb
		
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
		rpnToType.put(0x0001, TYPE_MSB_SIGNED);  // channel fine tuning
		rpnToType.put(0x0002, TYPE_BYTE_SIGNED); // channel coarse tuning
		rpnToType.put(0x7F7F, TYPE_NONE);        // reset RPN
		
		// min / max
		applyDefaultMinAndMax(rpnToType,  rpnToDefault,  rpnToMin,  rpnToMax);
		applyDefaultMinAndMax(nrpnToType, nrpnToDefault, nrpnToMin, nrpnToMax);
		
		// exceptions (default)
		rpnToDefault.put(0x0000, 0x0200); // pitch bend sensitivity
		rpnToDefault.put(0x0001, 0x4000); // channel fine tuning
		rpnToDefault.put(0x0002, 0x4000); // channel coarse tuning
		rpnToDefault.put(0x0005, 0x0040); // modulation depth range
		
		/////////////////////////////
		// SysEx
		/////////////////////////////
		
		// controller destination - type
		sxCtrlDestToType.put( 0x00, TYPE_BYTE_SIGNED ); // 00: pitch control
		sxCtrlDestToType.put( 0x01, TYPE_BYTE_SIGNED ); // 01: filter cutoff control
		sxCtrlDestToType.put( 0x02, TYPE_BYTE_SIGNED ); // 02: amplitude control
		sxCtrlDestToType.put( 0x03, TYPE_BYTE_FLEX   ); // 03: LFO pitch depth
		sxCtrlDestToType.put( 0x04, TYPE_BYTE        ); // 04: LFO filter depth
		sxCtrlDestToType.put( 0x05, TYPE_BYTE        ); // 05: LFO amplitude depth
		
		// controller destination - min / max
		applyDefaultMinAndMax(sxCtrlDestToType, sxCtrlDestToDefault, sxCtrlDestToMin, sxCtrlDestToMax);
		
		// controller destination - exceptions
		sxCtrlDestToMin.put( 0x00, -24 ); // 00: pitch control
		sxCtrlDestToMax.put( 0x00,  24 ); // 00: pitch control
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
			if (TYPE_BYTE == type || TYPE_MSB == type || TYPE_ANY == type || TYPE_BYTE_FLEX == type) {
				minStructure.put(number, 0);
				maxStructure.put(number, 127);
			}
			else if (TYPE_MSB_SIGNED == type || TYPE_BYTE_SIGNED == type) {
				minStructure.put(number, -64);
				maxStructure.put(number, 63);
			}
		}
	}
}
