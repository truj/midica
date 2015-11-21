/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.midi;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import org.midica.file.MidiParser;
import org.midica.file.MidicaPLParser;

import com.sun.media.sound.MidiUtils;

/**
 * This class is used to create a MIDI sequence. It is used by one of the parser methods while
 * parsing a MidicaPL or MIDI file.
 * 
 * @author Jan Trukenmüller
 */
public class SequenceCreator {
	
	public  static final long NOW                =  -1; // MIDI tick for channel initializations
	public  static final int  DEFAULT_RESOLUTION = 480; // ticks per quarter note
	
	private static int      resolution = DEFAULT_RESOLUTION;
	private static Track[]  tracks     = null;
	private static Sequence seq;
	
	
	/**
	 * This class is only used statically so a public constructor is not needed.
	 */
	private SequenceCreator() {
	}
	
	/**
	 * Creates a new sequence and sets it's resolution to the default value.
	 * Initiates all necessary data structures.
	 * This method is called by the {@link MidicaPLParser}.
	 * 
	 * @throws InvalidMidiDataException    if {@link Sequence}.PPQ is not a valid division type.
	 *                                     This should never happen.
	 */
	public static void reset() throws InvalidMidiDataException {
		resolution = DEFAULT_RESOLUTION;
		reset( resolution );
	}
	
	/**
	 * Creates a new sequence and sets it's resolution to the given value.
	 * Initiates all necessary data structures.
	 * This method is called by the {@link MidiParser}.
	 * 
	 * @param res                          Resolution of the new sequence.
	 * @throws InvalidMidiDataException    if {@link Sequence}.PPQ is not a valid division type.
	 *                                     This should never happen.
	 */
	public static void reset( int res ) throws InvalidMidiDataException {
		
		// create a new stream
		resolution = res;
		seq        = new Sequence( Sequence.PPQ, resolution );
		tracks     = new Track[ 16 ];
		for ( int i=0; i<16; i++ ) {
			tracks[ i ] = seq.createTrack();
		}
		
		return;
	}
	
	/**
	 * Returns the MIDI sequence.
	 * 
	 * @return    MIDI sequence.
	 */
	public static Sequence getSequence() {
		return seq;
	}
	
	/**
	 * Initiates the given channel by providing a meta event with the comment as the track name
	 * and a program change event to set the channel to the default instrument.
	 * This is also done if the instrument (program number) of a channel is changed.
	 * 
	 * @param channel     Channel number from 0 to 15.
	 * @param instrNum    Instrument number - corresponds to the MIDI program number.
	 * @param comment     Comment to be used as the track name.
	 * @param tick        Tickstamp of the instrument change or -1 if the method is called
	 *                    during initialization.
	 * @throws InvalidMidiDataException if invalid MIDI data is used to create a MIDI message.
	 */
	public static void initChannel( int channel, int instrNum, String comment, long tick ) throws InvalidMidiDataException {
		
		// meta message: track name
		MetaMessage metaMsg = new MetaMessage();
		byte[] data = comment.getBytes();
		data = unshift( data, (byte)channel );  // the first byte marks the channel number
		metaMsg.setMessage( MidiListener.META_TRACK_NAME, data, data.length );
		tracks[ channel ].add( new MidiEvent(metaMsg, tick) );
		
		// program change
		ShortMessage msg = new ShortMessage();
		msg.setMessage( ShortMessage.PROGRAM_CHANGE, channel, instrNum, 0 );
		tracks[ channel ].add( new MidiEvent(msg, tick) );
	}
	
	/**
	 * Adds the note-ON and note-OFF messages for one note to be played.
	 * Also adds the according meta messages.
	 * 
	 * @param channel      Channel number from 0 to 15.
	 * @param note         Note number.
	 * @param startTick    Tickstamp of the note-ON event.
	 * @param endTick      Tickstamp of the note-OFF event.
	 * @param volume       Velocity of the key stroke.
	 * @throws InvalidMidiDataException if invalid MIDI data is used to create a MIDI message.
	 */
	public static void addMessageKeystroke( int channel, int note, long startTick, long endTick, int volume ) throws InvalidMidiDataException {
		addMessageNoteON( channel, note, startTick, volume );
		addMessageNoteOFF( channel, note, endTick );
	}
	
	/**
	 * Adds a note-ON event and the according meta event.
	 * 
	 * @param channel    Channel number from 0 to 15.
	 * @param note       Note number.
	 * @param tick       Tickstamp of the event.
	 * @param volume     Velocity of the key stroke.
	 * @throws InvalidMidiDataException if invalid MIDI data is used to create a MIDI message.
	 */
	public static void addMessageNoteON( int channel, int note, long tick, int volume ) throws InvalidMidiDataException {
		
		// meta message for note ON
		MetaMessage metaMsg = new MetaMessage();
		byte[] dataOn = {
			(byte) channel,
			MidiListener.ACTIVITY_ON,
			(byte) note,
			(byte) volume,
		};
		metaMsg.setMessage( MidiListener.META_SEQUENCER_SPECIFIC, dataOn, dataOn.length );
		MidiEvent event = new MidiEvent( metaMsg, tick );
		tracks[ channel ].add( event );
		
		// note ON
		ShortMessage msg = new ShortMessage();
		msg.setMessage( ShortMessage.NOTE_ON, channel, note, volume );
		event = new MidiEvent( msg, tick );
		tracks[ channel ].add( event );
	}
	
	/**
	 * Adds a note-OFF event and the according meta event.
	 * 
	 * @param channel    Channel number from 0 to 15.
	 * @param note       Note number.
	 * @param tick       Tickstamp of the event.
	 * @throws InvalidMidiDataException if invalid MIDI data is used to create a MIDI message.
	 */
	public static void addMessageNoteOFF( int channel, int note, long tick ) throws InvalidMidiDataException {
		
		// meta message for note OFF
		MetaMessage metaMsg = new MetaMessage();
		byte[] dataOff = {
			(byte) channel,
			MidiListener.ACTIVITY_OFF,
		};
		metaMsg.setMessage( MidiListener.META_SEQUENCER_SPECIFIC, dataOff, dataOff.length );
		MidiEvent event = new MidiEvent( metaMsg, tick );
		tracks[ channel ].add( event );
		
		// note OFF
		ShortMessage msg = new ShortMessage();
		msg.setMessage( ShortMessage.NOTE_OFF, channel, note, 0 );
		event = new MidiEvent( msg, tick );
		tracks[ channel ].add( event );
	}
	
	/**
	 * Sets the tempo in beats per minute by creating a tempo change message.
	 * 
	 * @param newBpm    Tempo in beats per minute.
	 * @param tick      Tickstamp of the tempo change event.
	 * @throws InvalidMidiDataException if invalid MIDI data is used to create a MIDI message.
	 */
	public static void addMessageBpm( int newBpm, long tick ) throws InvalidMidiDataException {
		// bpm (beats per minute) --> mpq (microseconds per quarter)
		int mpq = (int) MidiUtils.convertTempo( newBpm );
		int cmd = MidiListener.META_SET_TEMPO;
		
		MetaMessage msg = new MetaMessage();
		byte[] data = new byte[ 3 ];
		data[ 0 ] = (byte) ( (mpq >> 16) & 0xFF );
		data[ 1 ] = (byte) ( (mpq >>  8) & 0xFF );
		data[ 2 ] = (byte) (  mpq        & 0xFF );
		
		msg.setMessage( cmd, data, data.length );
		MidiEvent event = new MidiEvent( msg, tick );
		tracks[ 0 ].add( event );
	}
	
	/**
	 * Adds a channel-dependent generic message.
	 * This is called by the {@link MidiParser} to add messages that are not handled by another
	 * method.
	 * 
	 * @param msg        Generic MIDI message.
	 * @param channel    Channel number from 0 to 15.
	 * @param tick       Tickstamp of the event.
	 */
	public static void addMessageGeneric( MidiMessage msg, int channel, long tick ) {
		MidiEvent event = new MidiEvent( msg, tick );
		tracks[ channel ].add( event );
	}
	
	/**
	 * Adds a channel-independent generic message.
	 * This is called by the {@link MidiParser} to add messages that are not handled by another
	 * method.
	 * Internally those messages are added to channel 0.
	 * 
	 * @param msg     Generic MIDI message.
	 * @param tick    Tickstamp of the event.
	 */
	public static void addMessageGeneric( MidiMessage msg, long tick ) {
		addMessageGeneric( msg, 0, tick );
	}
	
	/**
	 * Returns the resolution of the MIDI sequence in ticks per quarter note.
	 * 
	 * @return Resolution in ticks per quarter note.
	 */
	public static int getResolution() {
		return resolution;
	}
	
	/**
	 * Returns an array that is one element bigger than the given array.
	 * 
	 * The given element will be placed to the first position (index 0).
	 * All other elements are moved one index up.
	 * 
	 * Something more or less similar to Perl's unshift function that is not
	 * available in Java.
	 * 
	 * @param array    The array to be extended.
	 * @param elem     The element to be added to beginning of the array.
	 * @return         The resulting (extended) array.
	 */
	private static byte[] unshift( byte[] array, byte elem ) {
		
		int newLength = array.length + 1;
		
		// create a new array which is on element bigger
		byte[] result = new byte[ newLength ];
		
		// fill the resulting array
		result[ 0 ] = elem;
		System.arraycopy( array, 0, result, 1, newLength - 1 );
		
		return result;
	}
}
