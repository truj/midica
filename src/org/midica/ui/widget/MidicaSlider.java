/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.widget;

import javax.swing.JSlider;

import org.midica.config.Laf;
import org.midica.ui.SliderHelper;

/**
 * This is the base class for all sliders.
 * 
 * It applies slider-specific look and feel to the slider, as well as special behaviour.
 * It supports setting the slider by mouse clicks.
 * 
 * @author Jan Trukenmüller
 */
public class MidicaSlider extends JSlider {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Creates a slider.
	 * Applies slider-specific look and feel.
	 * 
	 * @param orientation  Same as the orentation for the parent {@link JSlider} class.
	 */
	public MidicaSlider(int orientation) {
		super(orientation);
		
		// enable setting the slider directly by clicking on it
		setUI( SliderHelper.createSliderUi(this) );
		
		// show everything
		setPaintTicks( true );
		setPaintLabels( true );
		setPaintTrack( true );
		
		// look and feel
		Laf.applyLaf(this);
	}
}
