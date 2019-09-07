/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.config;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * This class represents a key binding, consisting of a key code and modifiers like SHIFT, CTRL and so on.
 * 
 * @author Jan Trukenmüller
 */
public class KeyBinding implements Comparable<KeyBinding> {
	
	private int keycode;
	private int modifiers;
	
	/**
	 * Creates a new key binding.
	 * 
	 * @param code  key code
	 * @param mods  modifiers like SHIFT, CTRL, and so on, connected with bitwise-OR
	 */
	public KeyBinding(int code, int mods) {
		keycode   = code;
		modifiers = mods;
	}
	
	/**
	 * Returns the key code.
	 * 
	 * @return key code
	 */
	public int getKeyCode() {
		return keycode;
	}
	
	/**
	 * Returns a value representing the pressed modifiers (SHIFT, CTRL, and so on), connected with bitwise-OR.
	 * 
	 * @return modifiers
	 */
	public int getModifiers() {
		return modifiers;
	}
	
	/**
	 * Returns a string containing the key code and the modifiers.
	 * Each key binding with the same combination of keycode and modifiers returns the same string.
	 */
	public String toString() {
		return keycode + "-" + modifiers;
	}
	
	/**
	 * Returns a human-readable description of the key binding.
	 * 
	 * @return key-binding description.
	 */
	public String getDescription() {
		String modStr = InputEvent.getModifiersExText(modifiers);
		String keyStr = KeyEvent.getKeyText(keycode);
		String description;
		if ( "".equals(modStr) ) {
			description = keyStr;
		}
		else {
			description = modStr + "+" + keyStr;
		}
		
		return description;
	}
	
	@Override
	public int compareTo(KeyBinding o) {
		
		// compare keycode first
		int cmp = Integer.compare(keycode, o.getKeyCode());
		if (cmp != 0)
			return cmp;
		
		// then compare modifiers
		cmp = Integer.compare(modifiers, o.getModifiers());
		return cmp;
	}
}
