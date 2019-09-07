/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui;

import javax.swing.JSlider;
import javax.swing.plaf.SliderUI;
import javax.swing.plaf.metal.MetalSliderUI;
import javax.swing.plaf.synth.SynthSliderUI;

import org.midica.config.Laf;

/**
 * This class provides helper methods to fine-tune the behavior of {@link JSlider}s.
 * It enables them to be controlled by keystrokes and left clicks and not only by drag and drop.
 * 
 * @author Jan Trukenmüller
 */
public class SliderHelper {
	
	/**
	 * Creates and returns a Slider UI.
	 * This UI ensures that the slider can be set by left click and not
	 * only by drag and drop.
	 * 
	 * @param slider  The slider.
	 * @return Slider UI.
	 */
	public static SliderUI createSliderUi(JSlider slider) {
		
		// nimbus
		if (Laf.isNimbus) {
			return new SynthSliderUI(slider) {
				protected void scrollDueToClickInTrack(int direction) {
					
					// avoid null pointer exception when clicking e.g. the transpose
					// slider on the left side
					if ( null == slider.getMousePosition() )
						return;
					
					int value = slider.getValue();
					if ( slider.getOrientation() == JSlider.HORIZONTAL ) {
						value = this.valueForXPosition( slider.getMousePosition().x );
					}
					else if ( slider.getOrientation() == JSlider.VERTICAL ) {
						value = this.valueForYPosition( slider.getMousePosition().y );
					}
					slider.setValue( value );
				}
			};
		}
		
		// metal
		return new MetalSliderUI() {
			protected void scrollDueToClickInTrack(int direction) {
				
				// avoid null pointer exception when clicking e.g. the transpose
				// slider on the left side
				if ( null == slider.getMousePosition() )
					return;
				
				int value = slider.getValue();
				if ( slider.getOrientation() == JSlider.HORIZONTAL ) {
					value = this.valueForXPosition( slider.getMousePosition().x );
				}
				else if ( slider.getOrientation() == JSlider.VERTICAL ) {
					value = this.valueForYPosition( slider.getMousePosition().y );
				}
				slider.setValue( value );
			}
		};
	}
}
