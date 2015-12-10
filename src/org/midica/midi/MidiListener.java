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
	public static final int META_END_OF_SEQUENCE    =  47;
	public static final int META_SET_TEMPO          =  81;
	public static final int META_SMPTE_OFFSET       =  84;
	public static final int META_TIME_SIGNATURE     =  88;
	public static final int META_KEY_SIGNATURE      =  89;
	public static final int META_SEQUENCER_SPECIFIC = 127;
	
	// marker bitmasks
	public static final byte MARKER_BITMASK_ACTIVITY   =  0b01000000;
	public static final byte MARKER_BITMASK_HISTORY    =  0b00100000;
	public static final byte MARKER_BITMASK_INSTRUMENT =  0b00010000;
	public static final byte MARKER_BITMASK_CHANNEL    =  0b00001111;
	
	
	private static final HashMap<Integer, String> metaEventTypeName = new HashMap<Integer, String>();
	
	// constants for channel activity
	public static final byte ACTIVITY_ON            = 0x00;
	public static final byte ACTIVITY_OFF           = 0x01;
	
	/**
	 * Creates a new meta event listener object.
	 * 
	 * @param controller    Player controller.
	 */
	public MidiListener( PlayerController controller ) {
		playerControler = controller;
		
		metaEventTypeName.put( META_SEQUENCE_NUMBER,    "SEQUENCE NUMBER"    );
		metaEventTypeName.put( META_TEXT,               "TEXT"               );
		metaEventTypeName.put( META_COPYRIGHT,          "COPYRIGHT"          );
		metaEventTypeName.put( META_TRACK_NAME,         "TRACK NAME"         );
		metaEventTypeName.put( META_INSTRUMENT_NAME,    "INSTRUMENT NAME"    );
		metaEventTypeName.put( META_LYRICS,             "LYRICS"             );
		metaEventTypeName.put( META_MARKER,             "MARKER"             );
		metaEventTypeName.put( META_CUE_POINT,          "CUE POINT"          );
		metaEventTypeName.put( META_CHANNEL_PREFIX,     "CHANNEL PREFIX"     );
		metaEventTypeName.put( META_END_OF_SEQUENCE,    "END OF SEQUENCE"    );
		metaEventTypeName.put( META_SET_TEMPO,          "SET TEMPO"          );
		metaEventTypeName.put( META_SMPTE_OFFSET,       "SMPTE OFFSET"       );
		metaEventTypeName.put( META_TIME_SIGNATURE,     "TIME SIGNATURE"     );
		metaEventTypeName.put( META_KEY_SIGNATURE,      "KEY SIGNATURE"      );
		metaEventTypeName.put( META_SEQUENCER_SPECIFIC, "SEQUENCER SPECIFIC" );
	}
	
	/**
	 * This method is called if a meta event in the MIDI stream is detected.
	 * It determines the exact event type and informs the {@link PlayerController} and
	 * the {@link MidiDevices} about the changes.
	 */
	public void meta( MetaMessage msg ) {
		
		int    type   = msg.getType();
		int    status = msg.getStatus();
		int    length = msg.getLength();
		byte[] data   = msg.getData();
		
		if ( META_END_OF_SEQUENCE == type ) {
			try {
				playerControler.endOfSequence();
				MidiDevices.stop();
			}
			catch ( MidiUnavailableException e ) {
			}
		}
		
		// TODO: change???
		else if ( META_TRACK_NAME == type ) {
			// get track number (== channel) - works only for Midica-produced MIDI streams
			byte track = data[ 0 ];
			
			if ( 0 <= track && track <= MidiDevices.NUMBER_OF_CHANNELS ) {
				// it's probably a Midica-produced stream
				data = shift( data );
			}
			else {
				// it's probably from a third-party MIDI file
				return;
			}
			
			// set the channel comment
			String text = new String( data );
			MidiDevices.setChannelComment( track, text );
		}
		
		else if ( META_MARKER == type ) {
			for ( byte bitmaskedChannel : data ) {
				byte channel = (byte) (bitmaskedChannel & MARKER_BITMASK_CHANNEL);
				boolean isActivityChange   = 0 != ( bitmaskedChannel & MARKER_BITMASK_ACTIVITY );
				boolean isHistoryChange    = 0 != ( bitmaskedChannel & MARKER_BITMASK_HISTORY );
				boolean isInstrumentChange = 0 != ( bitmaskedChannel & MARKER_BITMASK_INSTRUMENT );
				if (isActivityChange)
					MidiDevices.refreshChannelActivity( channel );
				if (isHistoryChange)
					MidiDevices.refreshNoteHistory( channel );
				// TODO: refresh instrument
			}
		}
		
		else {
		}
	}
	
	/**
	 * Returns an array that is one element smaller than the given array.
	 * The first element is thrown away. All other elements are moved one index down.
	 * 
	 * Something more or less similar to Perl's shift function that is not available
	 * in Java. However the shifted element is not returned.
	 * 
	 * @param array    The original array.
	 * @return         The shifted array.
	 */
	private static byte[] shift( byte[] array ) {
		
		int newLength = array.length - 1;
		
		// create a new array which is on element smaller
		byte[] result = new byte[ newLength ];
		
		// fill the resulting array
		System.arraycopy( array, 1, result, 0, newLength );
		
		return result;
	}
}
