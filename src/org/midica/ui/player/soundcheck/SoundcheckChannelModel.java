/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.player.soundcheck;

import javax.swing.DefaultComboBoxModel;

import org.midica.config.Dict;
import org.midica.file.NamedInteger;

/**
 * This is the model for the channel combobox in the soundcheck window.
 * 
 * 
 * @author Jan Trukenmüller
 */
public class SoundcheckChannelModel extends DefaultComboBoxModel {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Writes the channel number to every entry. Marks the percussion channel as
	 * a special channel.
	 */
	public SoundcheckChannelModel() {
		for ( int i = 0; i < 16; i++ ) {
			NamedInteger entry = new NamedInteger();
			entry.value = i;
			if ( 9 == i ) {
				entry.name = i + " : " + Dict.get( Dict.PERCUSSION_CHANNEL );
			}
			else {
				entry.name = Integer.toString( i );
			}
			addElement( entry );
		}
	}
	
}
