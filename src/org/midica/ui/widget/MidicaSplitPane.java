/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.widget;

import javax.swing.JSplitPane;

/**
 * Class for Midica split panes.
 * 
 * These split panes override {@link #setDividerLocation(double)} in order to
 * work correctly even if it's called before the split pane has been painted.
 * 
 * @author Jan Trukenmüller
 */
public class MidicaSplitPane extends JSplitPane {
	
	private static final long serialVersionUID = 1L;
	
	private double  dividerLocation   = 0.5;
	private boolean firstValidateDone = false;
	
	/**
	 * Creates a new split pane.
	 */
	public MidicaSplitPane( int orientation ) {
		super( orientation );
	}
	
	/**
	 * Sets the divider location (if possible) and remembers the value
	 * in order to try this again after the first validation.
	 * 
	 * @param dividerLocation  Divider location from **0.0** (top or left)
	 *                         to **1.0** (bottom of right)
	 */
	@Override
	public void setDividerLocation( double dividerLocation ) {
		this.dividerLocation = dividerLocation;
		super.setDividerLocation( dividerLocation );
	}
	
	/**
	 * Validates the container and retries setting the divider location,
	 * if this is the first call to {@link #validate()}.
	 */
	@Override
	public void validate() {
		super.validate();
		
		// Make sure that the divider location is only set on the first call
		// to validate(). Otherwise the user could never resize the divider
		// to any other value.
		if (firstValidateDone)
			return;
		
		// Set the divider location to the remembered value.
		super.setDividerLocation( dividerLocation );
		firstValidateDone = true;
	}
}
