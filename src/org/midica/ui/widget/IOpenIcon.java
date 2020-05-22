/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.widget;

/**
 * This interface is used by icons that open a layer or window.
 * 
 * Examples:
 * 
 * - Table string filter icons
 * - Decompile config icons
 * 
 * @author Jan Trukenmüller
 */
public interface IOpenIcon {
	
	/**
	 * Stores key binding related IDs.
	 * These IDs are needed to be able to restore the key binding specific part of
	 * the tool tip after the basic tool tip has been changed.
	 * 
	 * @param keyBindingId  the key binding id
	 * @param ttType        the key binding type id
	 */
	public void rememberKeyBindingId(String keyBindingId, String ttType);
	
	/**
	 * Opens the associated layer or window.
	 */
	public void open();
	
	/**
	 * Indicates if the icon is currently showing on screen.
	 * Implemented by {@link java.awt.Component}.
	 * 
	 * @return **true** if the icon is showing. Otherwise: **false**.
	 */
	public boolean isShowing();
}
