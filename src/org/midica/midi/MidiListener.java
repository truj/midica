/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.midi;

import java.util.HashMap;

import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiUnavailableException;

import org.midica.ui.player.PlayerController;

/**
 * This class is used as a meta event listener for a playing MIDI sequence. An object of this
 * class is provided to the sequencer. If a meta event occurs in the MIDI sequence, this
 * listener is triggered.
 * 
 * This is especially important for self-made meta events that are used to inform us
 * about note-on or note-off events. If those events occur, the widgets in the player are
 * updated accordingly.
 * 
 * @author Jan Trukenmüller
 */
public class MidiListener implements MetaEventListener {
	
	private PlayerController playerControler = null;
	
	// constants for meta message types
	public static final int META_SEQUENCE_NUMBER    =   0;
	public static final int META_TEXT               =   1;
	public static final int META_COPYRIGHT          =   2;
	public static final int META_TRACK_NAME         =   3;
	public static final int META_INSTRUMENT_NAME    =   4;
	public static final int META_LYRICS             =   5;
	public static final int META_MARKER             =   6;
	public static final int META_CUE_POINT          =   7;
	public static final int META_CHANNEL_PREFIX     =  32;
	public static final int META_MIDI_PORT          =  33;
	public static final int META_END_OF_SEQUENCE    =  47;
	public static final int META_SET_TEMPO          =  81;
	public static final int META_SMPTE_OFFSET       =  84;
	public static final int META_TIME_SIGNATURE     =  88;
	public static final int META_KEY_SIGNATURE      =  89;
	public static final int META_SEQUENCER_SPECIFIC = 127;
	
	// marker bitmasks
	public static final byte MARKER_BITMASK_LYRICS     = (byte) 0b1000_0000;
	public static final byte MARKER_BITMASK_ACTIVITY   = (byte) 0b0100_0000;
	public static final byte MARKER_BITMASK_HISTORY    = (byte) 0b0010_0000;
	public static final byte MARKER_BITMASK_INSTRUMENT = (byte) 0b0001_0000;
	public static final byte MARKER_BITMASK_CHANNEL    = (byte) 0b0000_1111;
	
	/**
	 * Creates a new meta event listener object.
	 * 
	 * @param controller    Player controller.
	 */
	public MidiListener( PlayerController controller ) {
		playerControler = controller;
	}
	
	/**
	 * This method is called if a meta event in the MIDI stream is detected.
	 * It determines the exact event type and informs the {@link PlayerController} and
	 * the {@link MidiDevices} about the changes.
	 */
	@Override
	public void meta( MetaMessage msg ) {
		
		int    type = msg.getType();
		byte[] data = msg.getData();
		
		if ( META_END_OF_SEQUENCE == type ) {
			try {
				playerControler.endOfSequence();
				MidiDevices.stop();
			}
			catch ( MidiUnavailableException e ) {
			}
		}
		
		else if ( META_MARKER == type ) {
			for ( byte bitmaskedChannel : data ) {
				byte    channel            = (byte) ( bitmaskedChannel & MARKER_BITMASK_CHANNEL    );
				boolean isLyricsChange     =   0 != ( bitmaskedChannel & MARKER_BITMASK_LYRICS     );
				boolean isActivityChange   =   0 != ( bitmaskedChannel & MARKER_BITMASK_ACTIVITY   );
				boolean isHistoryChange    =   0 != ( bitmaskedChannel & MARKER_BITMASK_HISTORY    );
				boolean isInstrumentChange =   0 != ( bitmaskedChannel & MARKER_BITMASK_INSTRUMENT );
				if (isActivityChange)
					MidiDevices.refreshChannelActivity( channel );
				if (isHistoryChange)
					MidiDevices.refreshNoteHistory( channel );
				if (isInstrumentChange)
					MidiDevices.refreshInstrument( channel );
				if (isLyricsChange)
					MidiDevices.refreshLyrics();
			}
		}
		
		else {
		}
	}
}
