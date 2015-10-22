/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.midi;

import java.io.IOException;
import java.util.ArrayList;

import javax.sound.midi.Instrument;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Transmitter;
import javax.swing.table.AbstractTableModel;

import org.midica.config.Dict;
import org.midica.file.MidiParser;
import org.midica.ui.player.PlayerController;

/**
 * This class encapsulates the Java MIDI functionality.
 * It is not meant to instantiate any objects and contains only static methods.
 * 
 * After parsing a file and creating a MIDI sequence, the parsing class sets the sequence
 * with setSequence().
 * 
 * After starting the player or reparsing and resetting the sequence, setupDevices() is called
 * in order to create all devices, connect them with each other and pass the sequene to the
 * sequencer.
 * 
 * The other methods of this class are mostly used by the player.
 * 
 * @author Jan Trukenmüller
 */
public final class MidiDevices {
	
	// constants
	public static final int  WAITING_TIME_BEFORE_REMEMBER =  2; // milliseconds
	public static final int  DEFAULT_VOLUME               = 64;
	public static final int  NUMBER_OF_CHANNELS           = 16;
	public static final byte NOTE_HISTORY_BUFFER_SIZE     =  5;
	
	private static PlayerController  playerControler   = null;
	//private static int              bpm             =  60; // beats per minute ???
	private static float            tempoFactor       =   1;
	private static byte             volume            = DEFAULT_VOLUME;
	private static Sequence         seq;
	private static Sequencer        sequencer;
	private static Synthesizer      synthesizer;
	private static Receiver         receiver;
	// number of ticks to skip on forward/rewind
	private static int              skipTicks         = 480 * 4;  //  4 quarter notes = 1 bar
	private static int              skipFastTicks     = 480 * 16; // 16 quarter notes = 4 bars
	private static String[]         instruments       = null;
	private static String[]         channelComments   = new String[ NUMBER_OF_CHANNELS ];
	private static Soundbank        selectedSoundbank = null;
	private static byte[]           channelActivity   = new byte[ NUMBER_OF_CHANNELS ];
	private static byte[]           channelVolume     = new byte[ NUMBER_OF_CHANNELS ];
	private static boolean[]        channelMute       = new boolean[ NUMBER_OF_CHANNELS ];
	private static boolean[]        channelSolo       = new boolean[ NUMBER_OF_CHANNELS ];
	
	// ring buffer for note history
	private static ArrayList<ArrayList<Long[]>>  noteHistoryBuffer    = null;
	private static ArrayList<AbstractTableModel> noteHistoryObservers = null;
	
	/**
	 * This class is only used statically so a public constructor is not needed.
	 */
	private MidiDevices() {
	}
	
	/**
	 * Sets the sequence.
	 * This is called by the file parser objects after creating the sequence via
	 * the {@link SequenceCreator} class.
	 * 
	 * @param sequence    The sequence to be set.
	 */
	public static void setSequence( Sequence sequence ) {
		seq = sequence;
	}
	
	/**
	 * Returns the sequence.
	 * This is called by the exporter classes.
	 * 
	 * @return sequence the MIDI sequence
	 */
	public static Sequence getSequence() {
		return seq;
	}
	
	/**
	 * Indicates if a sequence has been set.
	 * 
	 * @return   **true**, if a sequence has been set. Otherwise, returns **false**.
	 */
	public static boolean isSequenceSet() {
		if ( null == seq )
			return false;
		return true;
	}
	
	/**
	 * Initializes all needed MIDI devices and connects them with each other.
	 * 
	 * @param controller     The player controller object.
	 * @throws InvalidMidiDataException    if the sequence contains invalid data.
	 * @throws MidiUnavailableException    if a device is not reachable.
	 * @throws SequenceNotSetException     if no sequence has been set yet. That means,
	 *                                     no music file has been parsed successfully.
	 * @throws IOException                 on I/O problems.
	 */
	public static void setupDevices( PlayerController controller ) throws InvalidMidiDataException, MidiUnavailableException, SequenceNotSetException, IOException {
		
		playerControler = controller;
		
		// initialize sequencer and get transmitter
		Transmitter trans = setupSequencer();
		
		// initialize synthesizer and get receiver
		Receiver rec = setupSynthesizer();
		
		// connect sequencer with synthesizer
		trans.setReceiver( rec );
		
		// initialize or restore user changes in the player
		sequencer.setTempoFactor( tempoFactor );
		setVolume( volume );
		for( int i = 0; i < NUMBER_OF_CHANNELS; i++ ) {
			setMute( i, channelMute[i] );
			setSolo( i, channelSolo[i] );
		}
		
		// initialize channel activity state
		resetChannelActivity();
		
		// initialize note history
		noteHistoryBuffer = new ArrayList<ArrayList<Long[]>>();
		for ( byte channel = 0; channel < NUMBER_OF_CHANNELS; channel++ )
			noteHistoryBuffer.add( new ArrayList<Long[]>() );
	}
	
	/**
	 * Creates and initializes the sequencer.
	 * 
	 * @return    A transmitter object, connected with the newly created sequencer.
	 * @throws SequenceNotSetException     if no sequence has been set yet. That means,
	 *                                     no music file has been parsed successfully.
	 * @throws MidiUnavailableException    if a device is not reachable.
	 * @throws InvalidMidiDataException    if the sequence contains invalid data.
	 */
	private static Transmitter setupSequencer() throws SequenceNotSetException, MidiUnavailableException, InvalidMidiDataException {
		// initialize sequencer
		if ( null == seq )
			throw new SequenceNotSetException();
		sequencer = MidiSystem.getSequencer( false );
		
		// initialize listeners
		MidiListener listener = new MidiListener( playerControler );
		sequencer.addMetaEventListener( listener );
		
		sequencer.open();
		sequencer.setSequence( seq );
		
		return sequencer.getTransmitter();
	}
	
	/**
	 * Initializes a software or hardware synthesizer.
	 * 
	 * @return    A receiver object, connected to the hardware or software synthesizer.
	 * @throws MidiUnavailableException    if a device is not reachable.
	 */
	private static Receiver setupSynthesizer() throws MidiUnavailableException {
		
		// get synthesizer
		synthesizer = MidiSystem.getSynthesizer();
		
		// hardware or software?
		boolean software = ( null == synthesizer.getDefaultSoundbank() )
				         ? false
				         : true
				         ;
		
		// load chosen soundbank and initialize it's instruments
		if ( selectedSoundbank != null ) {
			if ( synthesizer.isSoundbankSupported(selectedSoundbank) ) {
				synthesizer.loadAllInstruments( selectedSoundbank );
				initInstrumentsIfNotYetDone( selectedSoundbank );
			}
		}
		
		Receiver rec = null;
		if (software) {
			Soundbank soundbank = synthesizer.getDefaultSoundbank();
			initInstrumentsIfNotYetDone( soundbank );
			
			synthesizer.open();
			rec = synthesizer.getReceiver();
		}
		else {
			// hardware
			rec = receiver = MidiSystem.getReceiver();
			initInstrumentsIfNotYetDone( null );
		}
		
		return rec;
	}
	
	/**
	 * Initializes the instruments of the right soundbank.
	 * 
	 * @param soundbank    Selected or default soundbank - or **null** if a
	 *                     hardware soundbank is used.
	 */
	private static void initInstrumentsIfNotYetDone( Soundbank soundbank ) {
		if ( null != instruments )
			return;
		
		if ( null == soundbank ) {
			int instrCnt = 128;
			instruments = new String[ instrCnt ];
			for ( int i=0; i<instrCnt; i++ ) {
				instruments[ i ] = Dict.get( Dict.DEFAULT_INSTRUMENT_NAME );
			}
		}
		else {
			int instrCnt = soundbank.getInstruments().length;
			instruments = new String[ instrCnt ];
			int i = 0;
			for ( Instrument instrument : soundbank.getInstruments() ) {
				instruments[ i ] = instrument.getName();
				i++;
			}
		}
	}
	
	/**
	 * Resets the channel activity of each channel to 0. The channel activity is used in
	 * the player in order to display, if there are notes being currently played in a channel.
	 */
	public static void resetChannelActivity() {
		for ( byte i = 0; i < 16; i++ )
			channelActivity[ i ] = 0;
	}
	
	/**
	 * Increments the channel activity by one. That means, one more instrument is playing in
	 * that channel.
	 * 
	 * @param channel    Channel number from 0 to 15.
	 */
	public static void incrementChannelActivity( byte channel ) {
		channelActivity[ channel ] ++;
		playerControler.channelActivityChanged( channel );
	}
	
	/**
	 * Decrements the channel activity by one. That means, one instrument has stopped to play
	 * in that channel.
	 * 
	 * @param channel    Channel number from 0 to 15.
	 */
	public static void decrementChannelActivity( byte channel ) {
		channelActivity[ channel ] --;
		if ( channelActivity[channel] < 0 )
			channelActivity[ channel ] = 0;
		playerControler.channelActivityChanged( channel );
	}
	
	/**
	 * Closes and destroys all MIDI devices.
	 */
	public static void destroyDevices() {
		
		// destroy sequencer
		if ( null != sequencer ) {
			if ( sequencer.isRunning() )
				sequencer.stop();
			if ( sequencer.isOpen() )
				sequencer.close();
		}
		sequencer = null;
		
		// destroy software synthesizer
		if ( null != synthesizer ) {
			if ( synthesizer.isOpen() )
				synthesizer.close();
		}
		synthesizer = null;
		
		// destroy receiver of hardware synthesizer
		receiver = null;
		
		// reset instruments
		instruments = null;
		
		// reset channel comments
		channelComments = new String[ NUMBER_OF_CHANNELS ];
	}
	
	/**
	 * Starts to play the current MIDI sequence - or continues to play a paused sequence.
	 * 
	 * @throws IllegalStateException
	 */
	public static void play() throws IllegalStateException {
		if ( null != sequencer ) {
			// find out if we have to refresh the volume settings in the synthesizer
			boolean rememberNeeded = false;
			if ( 0 == sequencer.getTickPosition() )
				rememberNeeded = true;
			
			// play
			sequencer.start();
			
			// refresh if necessary
			if ( rememberNeeded )
				rememberVolume();
		}
	}
	
	/**
	 * Pauses a MIDI sequence that is currently being played.
	 * 
	 * @throws InvalidMidiDataException
	 * @throws MidiUnavailableException
	 * @throws IllegalStateException       if the sequencer is closed.
	 */
	public static void pause() throws InvalidMidiDataException, MidiUnavailableException, IllegalStateException {
		if ( null != sequencer )
			sequencer.stop();
	}
	
	/**
	 * Stops a MIDI sequence that is currently being played and resets it's current position.
	 * 
	 * @throws MidiUnavailableException
	 * @throws IllegalStateException       if the sequencer is closed.
	 */
	public static void stop() throws MidiUnavailableException, IllegalStateException {
		if ( null != sequencer ) {
			sequencer.stop();
		}
		setTickPosition( 0 );
	}
	
	/**
	 * Increments the current position in the MIDI stream by 4 quarter notes.
	 */
	public static void forward() {
		if ( null != sequencer ) {
			long totalTicks   = sequencer.getTickLength();
			long currentTicks = sequencer.getTickPosition();
			currentTicks += skipTicks;
			if ( currentTicks >= totalTicks )
				currentTicks = totalTicks - 1;
			setTickPosition( currentTicks );
		}
	}
	
	/**
	 * Increments the current position in the MIDI stream by 16 quarter notes.
	 */
	public static void fastForward() {
		if ( null != sequencer ) {
			long totalTicks   = sequencer.getTickLength();
			long currentTicks = sequencer.getTickPosition();
			currentTicks += skipFastTicks;
			if ( currentTicks >= totalTicks )
				currentTicks = totalTicks - 1;
			setTickPosition( currentTicks );
		}
	}
	
	/**
	 * Decrements the current position in the MIDI stream by 4 quarter notes.
	 */
	public static void rewind() {
		if ( null != sequencer ) {
			long currentTicks = sequencer.getTickPosition();
			currentTicks -= skipTicks;
			if ( currentTicks < 0 )
				currentTicks = 0;
			setTickPosition( currentTicks );
		}
	}
	
	/**
	 * Decrements the current position in the MIDI stream by 16 quarter notes.
	 */
	public static void fastRewind() {
		if ( null != sequencer ) {
			long currentTicks = sequencer.getTickPosition();
			currentTicks -= skipFastTicks;
			if ( currentTicks < 0 )
				currentTicks = 0;
			setTickPosition( currentTicks );
		}
	}
	
	/**
	 * Returns the current tick position in the MIDI stream.
	 * 
	 * @return    Current tickstamp.
	 */
	public static long getTickPosition() {
		if ( null != sequencer ) {
			return sequencer.getTickPosition();
		}
		else
			return 0;
	}
	
	/**
	 * Returns the current time in the MIDI stream.
	 * 
	 * @return    Current time as **hh:mm:ss**.
	 */
	public static String getTimePosition() {
		if ( null != sequencer ) {
			long microseconds = sequencer.getMicrosecondPosition();
			return microsecondsToTimeString( microseconds );
		}
		else
			return Dict.get( Dict.TIME_INFO_UNAVAILABLE );
	}
	
	/**
	 * Returns the length of the current MIDI stream in ticks.
	 * 
	 * @return    tick length of the current MIDI stream.
	 */
	public static long getTickLength() {
		if ( null != sequencer ) {
			return sequencer.getTickLength();
		}
		else
			return 0;
	}
	
	/**
	 * Returns the length of the current MIDI stream in the time format **hh:mm:ss**.
	 * 
	 * @return    length of the current MIDI stream.
	 */
	public static String getTimeLength() {
		if ( null != sequencer ) {
			long microseconds = sequencer.getMicrosecondLength();
			return microsecondsToTimeString( microseconds );
		}
		else
			return Dict.get( Dict.TIME_INFO_UNAVAILABLE );
	}
	
	/**
	 * Transforms the given microseconds into a time string in the format **hh:mm:ss**.
	 * 
	 * @param microseconds    number of microseconds to be transformed.
	 * @return                time string in the format **hh:mm:ss**.
	 */
	private static String microsecondsToTimeString( long microseconds ) {
		// get number of full seconds ignoring the rest of an opened second
		int rest    = (int) microseconds / 1000000; // full seconds
		int seconds = rest % 60;
		rest        = rest / 60; // full minutes
		int minutes = rest % 60;
		int hours   = rest / 60;
		
		return String.format( "%02d:%02d:%02d", hours, minutes, seconds );
	}
	
	/**
	 * Sets the position of the current MIDI stream to the given value in ticks.
	 * 
	 * @param pos    Tickstamp to be set.
	 */
	public static void setTickPosition( long pos ) {
		if ( null != sequencer )
			sequencer.setTickPosition( pos );
		resetChannelActivity();
		
		rememberVolume();
	}
	
	/**
	 * Restores the volume settings in the sequencer.
	 *  
	 * For some mysterious reasons all volume settings
	 * are destroyed:
	 * 
	 * 1. after setting the tick position; or
	 * 2. after starting to play when the tick position was 0
	 * 
	 * So we have to restore them again in these cases.
	 * 
	 * In some cases we have to wait a little until the volume
	 * resetting is accepted by the MIDI system.
	 */
	private static void rememberVolume() {
		try {
			Thread.sleep( WAITING_TIME_BEFORE_REMEMBER );
		}
		catch ( InterruptedException e ) {
		}
		setVolume( volume );
	}
	
	/**
	 * Determines if the sequencer is currently playing.
	 * 
	 * @return    **true**, if the sequencer is playing; otherwise: **false**.
	 */
	public static boolean isPlaying() {
		if ( null == sequencer )
			return false;
		if ( sequencer.isRunning() )
			return true;
		return false;
	}
	
	/**
	 * Sets the tempo factor of the sequencer.
	 * 
	 * @param factor    Tempo factor.
	 */
	public static void setTempo( float factor ) {
		if ( null == sequencer )
			return;
		sequencer.setTempoFactor( factor );
		tempoFactor = factor;
	}
	
	/**
	 * Returns the current tempo factor that is responsible for the playing speed.
	 * 
	 * @return    Tempo factor.
	 */
	public static float getTempo() {
		return tempoFactor;
	}
	
	/**
	 * Returns the current global (channel-independent) volume.
	 * 
	 * @return    volume
	 */
	public static byte getVolume() {
		return volume;
	}
	
	/**
	 * Sets the global (channel-independent) volume.
	 * Calculates and sets the new volume for each channel.
	 * Each channel volume is calculated by the global volume and the channel volume.
	 * 
	 * @param vol    New global volume.
	 */
	public static void setVolume( byte vol ) {
		volume = vol;
		
		if ( null != synthesizer )
			for (  int i=0; i< synthesizer.getChannels().length; i++ )
				setChannelVolume( i, channelVolume[i] );
	}
	
	/**
	 * Sets the volume of a channel relative to the global volume.
	 * 
	 * @param channelNumber    Channel number from 0 to 15.
	 * @param channelVol       Number from -127 to +127. This is added to the global
	 *                         volume value for this channel.
	 */
	public static void setChannelVolume( int channelNumber, byte channelVol ) {
		// store the new value
		channelVolume[ channelNumber ] = channelVol;
		
		// get total (resulting) channel volume
		int resultingVolume = volume + channelVol;
		if ( resultingVolume < 0 )
			resultingVolume = 0;
		else if ( resultingVolume > 127 )
			resultingVolume = 127;
		
		setChannelVolumeAbsolute( channelNumber, resultingVolume );
	}
	
	/**
	 * Sets the absolute volume of a channel.
	 * 
	 * @param channelNumber    Channel number from 0 to 15.
	 * @param channelVol       Number from 0 to 127.
	 */
	private static void setChannelVolumeAbsolute( int channelNumber, int channelVol ) {
		if ( null != receiver ) {
			try {
				ShortMessage volMessage = new ShortMessage();
				volMessage.setMessage(
					ShortMessage.CONTROL_CHANGE,
					channelNumber,
					MidiParser.CTRL_CHANGE_VOLUME,
					channelVol
				);
				receiver.send( volMessage, -1 );
			}
			catch ( InvalidMidiDataException e ) {
				e.printStackTrace();
			}
		}
		else if ( null != synthesizer ) {
			MidiChannel channel = synthesizer.getChannels()[ channelNumber ];
			channel.controlChange( MidiParser.CTRL_CHANGE_VOLUME, channelVol );
		}
	}
	
	/**
	 * Returns the current relative volume of the given channel.
	 * 
	 * @param channel    Channel number from 0 to 15.
	 * @return           Relative channel number from -127 to +127.
	 */
	public static byte getChannelVolume( byte channel ) {
		return channelVolume[ channel ];
	}
	
	/**
	 * Sets the mute state of the given channel.
	 * A muted channel doesn't produce an audio signal while playing.
	 * 
	 * @param channel    Channel number from 0 to 15.
	 * @param mute       **true**: muted; **false**: not muted.
	 */
	public static void setMute( int channel, boolean mute ) {
		channelMute[ channel ] = mute;
		if ( null == synthesizer )
			return;
		synthesizer.getChannels()[ channel ].setMute( mute );
	}
	
	/**
	 * Determines if the given channel is muted.
	 * A muted channel doesn't produce an audio signal while playing.
	 * 
	 * @param channel    Channel number from 0 to 15.
	 * @return           **true**, if the channel is muted. Otherwise: **false**.
	 */
	public static boolean getMute( int channel ) {
		return channelMute[ channel ];
	}
	
	/**
	 * Determines if the given channel is soloed.
	 * If at least one channel is soloed at a time, than only soloed channels produce
	 * an audio signal while playing. All non-soloed channels stay quiet.
	 * 
	 * @param channel    Channel number from 0 to 15.
	 * @return           **true**, if the channel is soloed. Otherwise: **false**.
	 */
	public static boolean getSolo( int channel ) {
		return channelSolo[ channel ];
	}
	
	/**
	 * Sets the solo state of the given channel.
	 * If at least one channel is soloed at a time, than only soloed channels produce
	 * an audio signal while playing. All non-soloed channels stay quiet.
	 * 
	 * @param channel    Channel number from 0 to 15.
	 * @param solo       **true**: solo; **false**: not solo.
	 */
	public static void setSolo( int channel, boolean solo ) {
		channelSolo[ channel ] = solo;
		if ( null == synthesizer )
			return;
		synthesizer.getChannels()[ channel ].setSolo( solo );
	}
	
	/**
	 * Determines if the given channel is currently playing.
	 * 
	 * @param channel    Channel number from 0 to 15.
	 * @return           **true**, if the channel is currently playing. Otherwise; **false**.
	 */
	public static boolean getChannelActivity( int channel ) {
		if ( channelActivity[channel] > 0 )
			return true;
		else
			return false;
	}
	
	/**
	 * Returns the channel configuration. That consists of:
	 * 
	 * - program number
	 * - instrument name
	 * - channel comment
	 * 
	 * @param channelNumber    Channel number from 0 to 15.
	 * @return                 Channel configuration in the order described above.
	 */
	public static String[] getChannelInfo( int channelNumber ) {
		String[] info = new String[ 3 ];
		info[0] = Dict.get( Dict.DEFAULT_PROGRAM_NUMBER );
		info[1] = Dict.get( Dict.DEFAULT_INSTRUMENT_NAME );
		info[2] = Dict.get( Dict.DEFAULT_CHANNEL_COMMENT );;
		if ( null == synthesizer )
			return info;
		MidiChannel channel = synthesizer.getChannels()[ channelNumber ];
		if ( null == channel )
			return info;
		int program = channel.getProgram();
		info[0] = Integer.toString( program );
		info[1] = instruments[ program ];
		info[2] = channelComments[ channelNumber ];
		
		return info;
	}
	
	/**
	 * Sets the comment for the given channel.
	 * Also triggers the {@link PlayerController} to refresh the changed channel info.
	 * 
	 * @param channel    Channel number from 0 to 15.
	 * @param comment    Channel comment.
	 */
	public static void setChannelComment( int channel, String comment ) {
		channelComments[ channel ] = comment;
		playerControler.channelInfoChanged( channel );
	}
	
	/**
	 * Puts a new note or percussion activity element into the ring buffer for
	 * the channel activity.
	 * Informs the note history table model about the change.
	 * 
	 * @param channel    Channel number from 0 to 15.
	 * @param note       Note or percussion instrument number.
	 * @param volume     velocity
	 */
	public static void addNoteHistory( byte channel, byte note, byte volume ) {
		// get channel specific note history buffer
		ArrayList<Long[]> buffer = noteHistoryBuffer.get( channel );
		
		// create new note history buffer entry
		Long[] bufferEntry = new Long[ 3 ];
		bufferEntry[ 0 ] = (long) note;
		bufferEntry[ 1 ] = (long) volume;
		bufferEntry[ 2 ] = getTickPosition();
		
		// add the new entry
		buffer.add( bufferEntry );
		
		// cut the buffer to it's maximum size
		while ( buffer.size() > NOTE_HISTORY_BUFFER_SIZE )
			buffer.remove( 0 );
		
		// inform note history listener
		noteHistoryObservers.get( channel ).fireTableDataChanged();
	}
	
	/**
	 * Returns the note history of the given channel.
	 * The note history is a ring buffer consisting of the newest notes.
	 * Each entry is an array consisting of the following elements:
	 * 
	 * - note value
	 * - volume (velocity)
	 * - tick position
	 * 
	 * @param channel    Channel number from 0 to 15.
	 * @return           Note history ring buffer.
	 */
	public static ArrayList<Long[]> getNoteHistory( byte channel ) {
		return noteHistoryBuffer.get( channel );
	}
	
	/**
	 * Removes all note history observers from the according data structure.
	 * Initializes the data structure, if not yet done.
	 */
	public static void resetNoteHistoryObservers() {
		if ( null == noteHistoryObservers )
			noteHistoryObservers = new ArrayList<AbstractTableModel>();
		noteHistoryObservers.removeAll( noteHistoryObservers );
	}
	
	/**
	 * Adds the given observer to the data structure for note history observers.
	 * The observer will be triggered whenever a note in the given channel is played.
	 * Then it is responsible for refreshing the note history table for that channel.
	 * 
	 * @param observer    Note history observer.
	 * @param channel     Channel number from 0 to 15.
	 */
	public static void addNoteHistoryObserver( AbstractTableModel observer, byte channel ) {
		noteHistoryObservers.add( channel, observer );
	}
	
	/**
	 * Sets the given soundbank so that it can be used by the synthesizer.
	 * 
	 * @param soundbank    Custom soundbank.
	 */
	public static void setSoundbank( Soundbank soundbank ) {
		selectedSoundbank = soundbank;
	}
	
	/**
	 * Returns the currently selected soundbank if available,
	 * or otherwise the default soundbank if available, or **null**
	 * if neither is available.
	 * 
	 * @return the soundbank.
	 */
	public static Soundbank getSoundbank() {
		
		// selected soundbank available?
		if ( selectedSoundbank != null )
			return selectedSoundbank;
		
		// create a synthesizer, if not yet done
		if ( null == synthesizer ) {
			try {
				synthesizer = MidiSystem.getSynthesizer();
			}
			catch (MidiUnavailableException e) {
				return null;
			}
		}
		
		// return default soundbank or null if a default soundbank doesn't exist
		return synthesizer.getDefaultSoundbank();
	}
	
	/**
	 * Plays one note in the given channel.
	 * This method is called from the soundcheck module.
	 * After playing the note, the old instrument number (program number) and channel
	 * volume are restored.
	 * 
	 * @param instrNum    Instrument number - corresponds to the MIDI program number.
	 * @param channel     Channel number from 0 to 15.
	 * @param note        Note or percussion instrument number.
	 * @param volume      Used for the channel volume and also for the velocity of the note.
	 * @param duration    Note length in milliseconds.
	 */
	public static void doSoundcheck( int instrNum, int channel, int note, int volume, int duration ) {
		if ( null == synthesizer )
			return;
		
		// remember instrument
		MidiChannel midiChannel = synthesizer.getChannels()[ channel ];
		int oldInstrNum = midiChannel.getProgram();
		
		// set instrument and volume
		midiChannel.programChange( instrNum );
		setChannelVolumeAbsolute( channel, volume );
		
		// note on
		midiChannel.noteOn( note, volume );
		
		// wait
		try {
			Thread.sleep( duration );
		}
		catch ( InterruptedException e ) {
		}
		
		// note off
		midiChannel.noteOff( note );
		
		// restore instrument and volume
		midiChannel.programChange( oldInstrNum );
		setChannelVolume( channel, channelVolume[channel] );
	}
}
