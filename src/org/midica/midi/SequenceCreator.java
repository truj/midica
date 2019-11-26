/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.midi;

import java.util.Map.Entry;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import org.midica.config.Config;
import org.midica.config.Dict;
import org.midica.file.CharsetUtils;
import org.midica.file.read.MidiParser;
import org.midica.file.read.MidicaPLParser;

import com.sun.media.sound.MidiUtils;

/**
 * This class is used to create a MIDI sequence. It is used by one of the parser methods while
 * parsing a MidicaPL or MIDI file.
 * 
 * @author Jan Trukenmüller
 */
public class SequenceCreator {
	
	public static final long NOW                =   0; // MIDI tick for channel initializations
	public static final int  DEFAULT_RESOLUTION = 480; // ticks per quarter note
	public static final int  NUM_META_TRACKS    =   3; // number of non-channel tracks
	public static final int  NUM_TRACKS         = NUM_META_TRACKS + 16; // total number of tracks
	
	private static String   fileType   = null; // last parsing attempt ("midica" or "mid")
	private static int      resolution = DEFAULT_RESOLUTION;
	private static String   charset    = null;
	private static Track[]  tracks     = null;
	private static Sequence seq;
	
	/**                    channel   --     note  -- event      */
	private static HashMap<Integer, HashMap<Integer, MidiEvent>> lastNoteOffEvent = null;
	
	/**
	 * This class is only used statically so a public constructor is not needed.
	 */
	private SequenceCreator() {
	}
	
	/**
	 * Creates a new sequence and sets it's resolution to the default value.
	 * Initiates all necessary data structures.
	 * 
	 * @param chosenCharset  Charset to be used for text-based messages.
	 * @throws InvalidMidiDataException    if {@link Sequence}.PPQ is not a valid division type.
	 *                                     This should never happen.
	 */
	public static void reset(String chosenCharset) throws InvalidMidiDataException {
		resolution = DEFAULT_RESOLUTION;
		
		if (null == chosenCharset) {
			chosenCharset = Config.get( Config.CHARSET_MPL );
		}
		
		reset(resolution, chosenCharset);
		fileType = "midica";
	}
	
	/**
	 * Creates a new sequence and sets it's resolution to the given value.
	 * Initiates all necessary data structures.
	 * This method is called by the {@link MidiParser}.
	 * 
	 * @param res                          Resolution of the new sequence.
	 * @param chosenCharset                Charset to be used for text-based messages.
	 * @throws InvalidMidiDataException    if {@link Sequence}.PPQ is not a valid division type.
	 *                                     This should never happen.
	 */
	public static void reset( int res, String chosenCharset ) throws InvalidMidiDataException {
		
		// create a new sequence
		resolution       = res;
		charset          = chosenCharset;
		seq              = new Sequence(Sequence.PPQ, resolution);
		tracks           = new Track[NUM_TRACKS];
		lastNoteOffEvent = new HashMap<>();
		for (int i = 0; i < NUM_TRACKS; i++) {
			tracks[i] = seq.createTrack();
			lastNoteOffEvent.put(i, new HashMap<Integer, MidiEvent>());
		}
		fileType = "mid";
		
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
	 * Determins which file type was attempted to parse last.
	 * 
	 * @return "midica" or "mid", depending on the file type.
	 */
	public static String getFileType() {
		return fileType;
	}
	
	/**
	 * Sets the bank MSB or LSB by sending an according control change message.
	 * 
	 * @param channel    Channel number from 0 to 15.
	 * @param tick       Tickstamp of the bank select or -1 if the method is called
	 *                   during initialization.
	 *                   TODO: test, how much the tick must be BEFORE the program change
	 * @param value      The value to set.
	 * @param isLSB      **false**: set the MSB; **true**: set the LSB
	 */
	public static void setBank( int channel, long tick, int value, boolean isLSB ) throws InvalidMidiDataException {
		
		// choose the right controller
		int controller = 0x00;
		if (isLSB)
			controller = 0x20;
		
		// make sure that the tick is not negative
		if ( tick < 0 )
			tick = 0;
		
		// set bank MSB or LSB
		ShortMessage msg = new ShortMessage();
		msg.setMessage( ShortMessage.CONTROL_CHANGE, channel, controller, value );
		tracks[ channel + NUM_META_TRACKS ].add( new MidiEvent(msg, tick) );
	}
	
	/**
	 * Initiates or changes the given channel's instrument, bank and channel comment.
	 * 
	 * The following steps are performed:
	 * 
	 * - adding a meta message (INSTRUMENT NAME) containing the channel number
	 *   and the channel comment
	 * - adding a program change message
	 * 
	 * @param channel     Channel number from 0 to 15.
	 * @param instrNum    Instrument number - corresponds to the MIDI program number.
	 * @param comment     Comment to be used as the track name.
	 * @param tick        Tickstamp of the instrument change or 0 if the method is called
	 *                    during initialization.
	 * @throws InvalidMidiDataException if invalid MIDI data is used to create a MIDI message.
	 */
	public static void initChannel( int channel, int instrNum, String comment, long tick ) throws InvalidMidiDataException {
		
		// meta message: instrument name
		MetaMessage metaMsg = new MetaMessage();
		byte[] data = CharsetUtils.getBytesFromText( comment, charset );
		metaMsg.setMessage( MidiListener.META_INSTRUMENT_NAME, data, data.length );
		tracks[ channel + NUM_META_TRACKS ].add( new MidiEvent(metaMsg, tick) );
		
		// program change
		ShortMessage msg = new ShortMessage();
		msg.setMessage( ShortMessage.PROGRAM_CHANGE, channel, instrNum, 0 );
		tracks[ channel + NUM_META_TRACKS ].add( new MidiEvent(msg, tick) );
	}
	
	/**
	 * Adds the note-ON and note-OFF messages for one note to be played.
	 * 
	 * @param channel      Channel number from 0 to 15.
	 * @param note         Note number.
	 * @param startTick    Tickstamp of the note-ON event.
	 * @param endTick      Tickstamp of the note-OFF event.
	 * @param velocity     Velocity of the key stroke.
	 * @throws InvalidMidiDataException if invalid MIDI data is used to create a MIDI message.
	 */
	public static void addMessageKeystroke( int channel, int note, long startTick, long endTick, int velocity ) throws InvalidMidiDataException {
		addMessageNoteON( channel, note, startTick, velocity );
		addMessageNoteOFF( channel, note, endTick );
	}
	
	/**
	 * Adds a note-ON event.
	 * 
	 * @param channel     Channel number from 0 to 15.
	 * @param note        Note number.
	 * @param tick        Tickstamp of the event.
	 * @param velocity    Velocity of the key stroke.
	 * @throws InvalidMidiDataException if invalid MIDI data is used to create a MIDI message.
	 */
	public static void addMessageNoteON(int channel, int note, long tick, int velocity) throws InvalidMidiDataException {
		ShortMessage msg = new ShortMessage();
		msg.setMessage(ShortMessage.NOTE_ON, channel, note, velocity);
		MidiEvent event = new MidiEvent(msg, tick);
		tracks[channel + NUM_META_TRACKS].add(event);
	}
	
	/**
	 * Adds a note-OFF event.
	 * 
	 * @param channel    Channel number from 0 to 15.
	 * @param note       Note number.
	 * @param tick       Tickstamp of the event.
	 * @throws InvalidMidiDataException if invalid MIDI data is used to create a MIDI message.
	 */
	public static void addMessageNoteOFF(int channel, int note, long tick) throws InvalidMidiDataException {
		ShortMessage msg = new ShortMessage();
		msg.setMessage(ShortMessage.NOTE_OFF, channel, note, 0);
		MidiEvent event = new MidiEvent(msg, tick);
		tracks[channel + NUM_META_TRACKS].add(event);
		
		// remember event, in case a correction is necessary later
		lastNoteOffEvent.get(channel).put(note, event);
	}
	
	/**
	 * Moves the last note-off event matching the given parameters to a new position.
	 * This is needed to correct legato-overlappings, otherwise resulting in a sequence like this:
	 * 
	 * # note-on
	 * # note-on
	 * # note-off
	 * # note-off
	 * 
	 * This can happen if a note is played twice in a row in the same channel, with a legato value of more than 100%.
	 * 
	 * @param channel     Channel number from 0 to 15.
	 * @param note        Note number.
	 * @param fromTick    Tick from where the event shall be moved away.
	 * @param toTick      Tick where the event shall be moved to.
	 * @throws Exception if the event to be moved was not found or has a different tick than expected.
	 */
	public static void moveNoteOffMessage(int channel, int note, long fromTick, long toTick) throws Exception {
		Track track = tracks[channel + NUM_META_TRACKS];
		
		// get the event to be corrected
		MidiEvent event = lastNoteOffEvent.get(channel).get(note);
		if (event == null) {
			throw new Exception("Cannot move note-off: event not found. This should not happen. Please report.");
		}
		
		// remove the event
		track.remove(event);
		
		// check
		if (event.getTick() != fromTick) {
			throw new Exception("cannot move note-off - wrong 'from' tick. This should not happen. Please report.");
		}
		
		// change tick
		event.setTick(toTick);
		
		// re-add the event
		track.add(event);
	}
	
	/**
	 * Sets the tempo in beats per minute by creating a tempo change message.
	 * 
	 * @param bpm     Tempo in beats per minute.
	 * @param tick    Tickstamp of the tempo change event.
	 * @throws InvalidMidiDataException if invalid MIDI data is used to create a MIDI message.
	 */
	public static void addMessageTempo(int bpm, long tick) throws InvalidMidiDataException {
		// bpm (beats per minute) --> mpq (microseconds per quarter)
		int mpq = (int) MidiUtils.convertTempo( bpm );
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
	 * Sets the time signature using a meta message.
	 * 
	 * @param numerator    Numerator of the time signature
	 * @param denominator  Denominator of the time signature
	 * @param tick         Tickstamp of the time signature event
	 * @throws InvalidMidiDataException if invalid MIDI data is used to create a MIDI message.
	 */
	public static void addMessageTimeSignature(int numerator, int denominator, long tick) throws InvalidMidiDataException {
		int cmd = MidiListener.META_TIME_SIGNATURE;
		
		// calculate valid denominators
		HashMap<Integer, Integer> validDemominators = new HashMap<Integer, Integer>();
		int validDenom = 1;
		for (int exponent = 0; exponent < 31; exponent++) {
			validDemominators.put(validDenom, exponent);
			validDenom *= 2;
		}
		
		// get and check exponent
		Integer exp = validDemominators.get(denominator);
		if (null == exp) {
			throw new InvalidMidiDataException( Dict.get(Dict.ERROR_INVALID_TIME_DENOM) + denominator);
		}
		
		MetaMessage msg = new MetaMessage();
		byte[] data = new byte[4];
		data[0] = (byte) numerator;
		data[1] = (byte) (int) exp;
		data[2] = (byte) 24;
		data[3] = (byte) 8;
		
		msg.setMessage(cmd, data, data.length);
		MidiEvent event = new MidiEvent(msg, tick);
		tracks[0].add(event);
	}
	
	/**
	 * Sets the key signature using a meta message.
	 * 
	 * @param note     any note number from 0 to 127
	 * @param isMajor  **true** for a major key signature, **false** for a minor one
	 * @param tick     Tickstamp of the time signature event
	 * @throws InvalidMidiDataException if invalid MIDI data is used to create a MIDI message.
	 */
	public static void addMessageKeySignature(int note, boolean isMajor, long tick) throws InvalidMidiDataException {
		int cmd = MidiListener.META_KEY_SIGNATURE;
		
		// calculate sharps or flats
		note %= 12; // ignore octaves
		byte sharpsOrFlats = 0;
		if (isMajor) {
			     if ( 0 == note) { sharpsOrFlats =  0; } // C  maj: 0 sharps or flats
			else if ( 1 == note) { sharpsOrFlats =  7; } // C# maj: 7 sharps (or Db maj: 5 flats)
			else if ( 2 == note) { sharpsOrFlats =  2; } // D  maj: 2 sharps
			else if ( 3 == note) { sharpsOrFlats = -3; } // Eb maj: 3 flats
			else if ( 4 == note) { sharpsOrFlats =  4; } // E  maj: 4 sharps
			else if ( 5 == note) { sharpsOrFlats = -1; } // F  maj: 1 flat
			else if ( 6 == note) { sharpsOrFlats =  6; } // F# maj: 6 sharps (or Gb maj: 6 flats)
			else if ( 7 == note) { sharpsOrFlats =  1; } // G  maj: 1 sharp
			else if ( 8 == note) { sharpsOrFlats = -4; } // Ab maj: 4 flats
			else if ( 9 == note) { sharpsOrFlats =  3; } // A  maj: 3 sharps
			else if (10 == note) { sharpsOrFlats = -2; } // Bb maj: 2 flats
			else if (11 == note) { sharpsOrFlats =  5; } // B  maj: 5 sharps (or Cb maj: 7 flats)
		}
		else {
			     if ( 0 == note) { sharpsOrFlats = -3; } // C  min: 3 flats
			else if ( 1 == note) { sharpsOrFlats =  4; } // C# min: 4 sharps
			else if ( 2 == note) { sharpsOrFlats = -1; } // D  min: 1 flat
			else if ( 3 == note) { sharpsOrFlats =  6; } // D# min: 6 sharps (or Eb min: 6 flats)
			else if ( 4 == note) { sharpsOrFlats =  1; } // E  min: 1 sharp
			else if ( 5 == note) { sharpsOrFlats = -4; } // F  min: 4 flats
			else if ( 6 == note) { sharpsOrFlats =  3; } // F# min: 3 sharps
			else if ( 7 == note) { sharpsOrFlats = -2; } // G  min: 2 flats
			else if ( 8 == note) { sharpsOrFlats =  5; } // G# min: 5 sharps (or Ab min: 7 flats)
			else if ( 9 == note) { sharpsOrFlats =  0; } // A  min: 0 sharps or flats
			else if (10 == note) { sharpsOrFlats =  7; } // A# min: 7 sharps (or Bb min: 5 flats)
			else if (11 == note) { sharpsOrFlats =  2; } // B  min: 2 sharps
		}
		
		// In some cases there are 2 possibilities. Then we must decide using the configured
		// half tone symbol
		if (Dict.isFlatConfigured()) {
			if (isMajor) {
				     if ( 1 == note) { sharpsOrFlats = -5; } // Db maj: 5 flats
				else if ( 6 == note) { sharpsOrFlats = -6; } // Gb maj: 6 flats
				else if (11 == note) { sharpsOrFlats = -7; } // Cb maj: 7 flats
			}
			else {
				     if ( 3 == note) { sharpsOrFlats = -6; } // Eb min: 6 flats
				else if ( 8 == note) { sharpsOrFlats = -7; } // Ab min: 7 flats
				else if (10 == note) { sharpsOrFlats = -5; } // Bb min: 5 flats
			}
		}
		
		MetaMessage msg = new MetaMessage();
		byte[] data = new byte[2];
		data[0] = sharpsOrFlats;
		data[1] = (byte) (isMajor ? 0x00 : 0x01);
		
		msg.setMessage(cmd, data, data.length);
		MidiEvent event = new MidiEvent(msg, tick);
		tracks[0].add(event);
	}
	
	/**
	 * Adds a copyright meta message to tick 0 of the sequence.
	 * This is called from the {@link MidicaPLParser} when postprocessing meta commands.
	 * 
	 * @param copyright The message to be added to the sequence.
	 * @throws InvalidMidiDataException if invalid MIDI data is used to create a MIDI message.
	 */
	public static void addMessageCopyright(String copyright) throws InvalidMidiDataException {
		MetaMessage metaMsg = new MetaMessage();
		byte[] data = CharsetUtils.getBytesFromText(copyright, charset);
		metaMsg.setMessage( MidiListener.META_COPYRIGHT, data, data.length );
		tracks[ 0 ].add( new MidiEvent(metaMsg, 0) );
	}
	
	/**
	 * Adds a lyrics meta message.
	 * The message may also be a meta message according to RP-026.
	 * 
	 * @param lyrics    The message to be added.
	 * @param tick      The tick where the lyrics event shall be added.
	 * @throws InvalidMidiDataException if invalid MIDI data is used to create a MIDI message.
	 */
	public static void addMessageLyrics(String lyrics, long tick) throws InvalidMidiDataException {
		MetaMessage metaMsg = new MetaMessage();
		byte[] data = CharsetUtils.getBytesFromText(lyrics, charset);
		metaMsg.setMessage( MidiListener.META_LYRICS, data, data.length );
		tracks[ 2 ].add( new MidiEvent(metaMsg, tick) );
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
		tracks[ channel + NUM_META_TRACKS ].add( event );
	}
	
	/**
	 * Adds a channel-independent generic message.
	 * This is called by the {@link MidiParser} to add messages that are not handled by another
	 * method.
	 * 
	 * Those messages are added to track 0.
	 * 
	 * @param msg     Generic MIDI message.
	 * @param tick    Tickstamp of the event.
	 */
	public static void addMessageGeneric( MidiMessage msg, long tick ) {
		MidiEvent event = new MidiEvent( msg, tick );
		tracks[ 0 ].add( event );
	}
	
	/**
	 * Adds a MIDI message to the given track.
	 * 
	 * This is needed for karaoke-related meta messages from foreign MIDI
	 * files. These messages must be put into the right track.
	 * That makes sure that they are later processed in the right order.
	 * (Sorted by tick, not by original track number).
	 * 
	 * @param msg    Meta message.
	 * @param track  Track number.
	 * @param tick   Tickstamp of the event.
	 */
	public static void addMessageToTrack( MidiMessage msg, int track, long tick ) {
		MidiEvent event = new MidiEvent( msg, tick );
		tracks[ track ].add( event );
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
	 * Adds meta events of the type **marker** to the sequence. These events
	 * show that the activity state of some channels change at this point.
	 * 
	 * This is called by the {@link SequenceAnalyzer} after all other analyzing
	 * is done.
	 * 
	 * The markers are always added to track 0.
	 * 
	 * @param markers  First dimension: **tick**; Second dimension: **bitmasked channels**
	 *                 that change their activity (and/or other properties) at this tick.
	 * @throws InvalidMidiDataException if one of the marker messages cannot be created.
	 */
	public static void addMarkers( TreeMap<Long, TreeSet<Byte>> markers ) throws InvalidMidiDataException {
		
		for ( Entry<Long, TreeSet<Byte>> eventData : markers.entrySet() ) {
			
			// get general parameters for the event to be created
			long          tick              = eventData.getKey();
			TreeSet<Byte> bitmaskedChannels = eventData.getValue();
			int           length            = bitmaskedChannels.size();
			
			// message part (all channels in the current tick's marker)
			byte[] content = new byte[ length ];
			int i = 0;
			for ( Object channelObj : bitmaskedChannels.toArray() ) {
				byte bitmaskedChannel = (byte) channelObj;
				byte channel          = (byte) (bitmaskedChannel & MidiListener.MARKER_BITMASK_CHANNEL);
				content[ i ]          = bitmaskedChannel;
				i++;
			}
			
			// create and add the event
			MetaMessage metaMsg = new MetaMessage();
			metaMsg.setMessage( MidiListener.META_MARKER, content, length );
			MidiEvent event = new MidiEvent( metaMsg, tick );
			tracks[ 0 ].add( event );
		}
	}
}
