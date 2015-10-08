/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.player.soundcheck;

import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;

import org.midica.config.Dict;
import org.midica.config.InstrumentElement;

/**
 * This class provides the model for the instruments combobox in the soundcheck
 * window.
 * 
 * @author Jan Trukenmüller
 */
public class SoundcheckInstrumentModel extends DefaultComboBoxModel {
	
	private ArrayList<InstrumentElement> instrumentList = null;
	
	/**
	 * Creates a model for the instruments combobox.
	 */
	public SoundcheckInstrumentModel() {
		instrumentList = Dict.getInstrumentList();
		
		for ( InstrumentElement elem : instrumentList )
			addElement( elem );
	}
	
}
