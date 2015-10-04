/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui;

import java.awt.event.KeyEvent;

import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.SliderUI;
import javax.swing.plaf.metal.MetalSliderUI;

/**
 * This class provides helper methods to fine-tune the behavior of {@link JSlider}s.
 * It enables them to be controlled by keystrokes and left clicks and not only by drag and drop.
 * 
 * @author Jan Trukenmüller
 */
public class SliderHelper {
	
	/**
	 * Checks if a slider has been adjusted by a keystroke.
	 * In this case the changeListener is invoked and valueIsAdjusting is set accordingly.
	 * In this project this is necessary because slider changes are only handled if
	 * getValueIsAdjusting() returns true.
	 * 
	 * @param e KeyEvent that has been triggered by the keystroke
	 */
	public static void handleSliderAdjustmentViaKey( KeyEvent e ) {
		if ( e.getSource() instanceof JSlider ) {
			if (   KeyEvent.VK_LEFT      == e.getKeyCode()
				|| KeyEvent.VK_RIGHT     == e.getKeyCode()
				|| KeyEvent.VK_UP        == e.getKeyCode()
				|| KeyEvent.VK_DOWN      == e.getKeyCode()
				|| KeyEvent.VK_PAGE_UP   == e.getKeyCode()
				|| KeyEvent.VK_PAGE_DOWN == e.getKeyCode()
				|| KeyEvent.VK_HOME      == e.getKeyCode()
				|| KeyEvent.VK_END       == e.getKeyCode()
			) {
				JSlider slider = (JSlider) e.getSource();
				for ( ChangeListener listener : slider.getChangeListeners() ) {
					slider.setValueIsAdjusting( true );
					listener.stateChanged( new ChangeEvent(slider) );
					slider.setValueIsAdjusting( false );
				}
				e.consume();
			}
		}
	}
	
	
	/**
	 * Creates and returns a Slider UI.
	 * This UI ensures that the slider can be set by left click and not
	 * only by drag and drop.
	 * 
	 * @return Slider UI.
	 */
	public static SliderUI createSliderUi() {
		
		return new MetalSliderUI() {
			
			protected void scrollDueToClickInTrack( int direction ) {
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
