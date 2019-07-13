/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.widget;

import javax.swing.JButton;

import org.midica.config.Laf;

/**
 * This is the base class for all buttons.
 * It applies button-specific look and feel to the button.
 * 
 * A button can be a primary or secondary button.
 * Primary buttons have a different color.
 * 
 * @author Jan Trukenmüller
 */
public class MidicaButton extends JButton {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Creates a secondary button.
	 * Applies button-specific look and feel.
	 * 
	 * @param text  Same as the text for the parent {@link JButton} class
	 */
	public MidicaButton(String text) {
		super(text);
		Laf.applyLafToButton(this, false);
	}
	
	/**
	 * Creates a primary or secondary button.
	 * Applies button-specific look and feel.
	 * 
	 * @param text       Same as the text for the parent {@link JButton} class
	 * @param isPrimary  **true** for primary buttons, otherwise **false**
	 */
	public MidicaButton(String text, boolean isPrimary) {
		super(text);
		Laf.applyLafToButton(this, isPrimary);
	}
}
