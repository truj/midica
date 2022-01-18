/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.midi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Transmitter;
import javax.swing.table.AbstractTableModel;

import org.midica.config.Dict;
import org.midica.file.read.SoundfontParser;
import org.midica.ui.info.InstrumentElement;
import org.midica.ui.player.PlayerController;

import com.sun.gervill.SoftSynthesizer;

/**
 * This class encapsulates the Java MIDI functionality.
 * It is not meant to instantiate any objects and contains only static methods.
 * 
 * After parsing a file and creating a MIDI sequence, the parsing class sets
 * the sequence with setSequence().
 * 
 * After starting the player or reparsing and resetting the sequence, setupDevices()
 * is called in order to create all devices, connect them with each other and pass
 * the sequene to the sequencer.
 * 
 * The other methods of this class are mostly used by the player.
 * 
 * @author Jan Trukenmüller
 */
public final class MidiDevices {
	
	// constants
	public static final int  WAITING_TIME_BEFORE_REMEMBER =        2; // milliseconds
	public static final byte DEFAULT_MASTER_VOL_MSB       =      100;
	public static final byte DEFAULT_MASTER_VOL_LSB       =      100;
	public static final int  DEFAULT_CHANNEL_VOL_MSB      =      100; // default according to https://midi.org/articles/about-midi-part-4-midi-files
	public static final int  DEFAULT_CHANNEL_VOL_LSB      =      100; // real default is probably 0 but we keep MSB and LSB synchronized
	public static final int  DEFAULT_TEMPO_BPM            =      120; // beats per minute
	public static final int  DEFAULT_TEMPO_MPQ            = 60000000; // microseconds per quarter note
	public static final int  NUMBER_OF_CHANNELS           =       16;
	
	private static PlayerController playerController = null;
	private static float       tempoFactor           = 1;
	private static byte        masterVolumeMsb       = DEFAULT_MASTER_VOL_MSB;
	private static byte        masterVolumeLsb       = DEFAULT_MASTER_VOL_LSB;
	private static Sequence    seq;
	private static Sequencer   sequencer;
	private static Synthesizer synthesizer;
	private static Receiver    receiver;
	// number of bars to skip on forward/rewind
	private static int         skipQuarters      = 4;  //  4 quarter notes = 1 bar
	private static int         skipFastQuarters  = 16; // 16 quarter notes = 4 bars
	private static Soundbank   selectedSoundbank = null;
	private static boolean[]   channelMute       = new boolean[NUMBER_OF_CHANNELS];
	private static boolean[]   channelSolo       = new boolean[NUMBER_OF_CHANNELS];
	private static byte[]      channelVolumeMsb  = {
		DEFAULT_CHANNEL_VOL_MSB, DEFAULT_CHANNEL_VOL_MSB, DEFAULT_CHANNEL_VOL_MSB, DEFAULT_CHANNEL_VOL_MSB,
		DEFAULT_CHANNEL_VOL_MSB, DEFAULT_CHANNEL_VOL_MSB, DEFAULT_CHANNEL_VOL_MSB, DEFAULT_CHANNEL_VOL_MSB,
		DEFAULT_CHANNEL_VOL_MSB, DEFAULT_CHANNEL_VOL_MSB, DEFAULT_CHANNEL_VOL_MSB, DEFAULT_CHANNEL_VOL_MSB,
		DEFAULT_CHANNEL_VOL_MSB, DEFAULT_CHANNEL_VOL_MSB, DEFAULT_CHANNEL_VOL_MSB, DEFAULT_CHANNEL_VOL_MSB,
	};
	private static byte[]      channelVolumeLsb  = {
		DEFAULT_CHANNEL_VOL_LSB, DEFAULT_CHANNEL_VOL_LSB, DEFAULT_CHANNEL_VOL_LSB, DEFAULT_CHANNEL_VOL_LSB,
		DEFAULT_CHANNEL_VOL_LSB, DEFAULT_CHANNEL_VOL_LSB, DEFAULT_CHANNEL_VOL_LSB, DEFAULT_CHANNEL_VOL_LSB,
		DEFAULT_CHANNEL_VOL_LSB, DEFAULT_CHANNEL_VOL_LSB, DEFAULT_CHANNEL_VOL_LSB, DEFAULT_CHANNEL_VOL_LSB,
		DEFAULT_CHANNEL_VOL_LSB, DEFAULT_CHANNEL_VOL_LSB, DEFAULT_CHANNEL_VOL_LSB, DEFAULT_CHANNEL_VOL_LSB,
	};
	
	/**   channel  --  program * 2^14 + bankMSB * 2^7 + bankLSB  --  instrument name */
	private static TreeMap<Byte, TreeMap<Integer, String>> instruments = null;
	
	// ring buffer for note history
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
	public static void setSequence(Sequence sequence) {
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
		if (null == seq)
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
	public static void setupDevices(PlayerController controller) throws InvalidMidiDataException, MidiUnavailableException, SequenceNotSetException, IOException {
		
		playerController = controller;
		
		// initialize sequencer and get transmitter
		Transmitter trans = setupSequencer();
		
		// initialize synthesizer and get receiver
		Receiver rec = setupSynthesizer();
		
		// connect sequencer with synthesizer
		trans.setReceiver(rec);
		
		// initialize or restore user changes in the player
		sequencer.setTempoFactor(tempoFactor);
		setMasterVolume();
		setAllChannelVolumes();
		for (int i = 0; i < NUMBER_OF_CHANNELS; i++) {
			setMute(i, channelMute[i]);
			setSolo(i, channelSolo[i]);
		}
		
		// initialize channel activity state
		for (byte channel = 0; channel < NUMBER_OF_CHANNELS; channel++)
			playerController.setChannelActivity(channel, false);
		
		// initialize note history
		for (byte channel = 0; channel < NUMBER_OF_CHANNELS; channel++)
			refreshNoteHistory(channel);
		
		// initialize channel and instrument info
		for (byte channel = 0; channel < NUMBER_OF_CHANNELS; channel++)
			refreshInstrument(channel);
		
		// initialize lyrics
		refreshLyrics();
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
		if (null == seq)
			throw new SequenceNotSetException();
		sequencer = MidiSystem.getSequencer(false);
		
		// initialize listeners
		MidiListener listener = new MidiListener(playerController);
		sequencer.addMetaEventListener(listener);
		
		sequencer.open();
		sequencer.setSequence(seq);
		
		return sequencer.getTransmitter();
	}
	
	/**
	 * Initializes a software or hardware synthesizer.
	 * If a soundbank file has been selected, loads this file into the synthesizer.
	 * 
	 * @return    A receiver object, connected to the hardware or software synthesizer.
	 * @throws MidiUnavailableException    if a device is not reachable.
	 */
	private static Receiver setupSynthesizer() throws MidiUnavailableException {
		
		// get synthesizer
		synthesizer = new SoftSynthesizer();
		
		// hardware or software?
		boolean isSoftware = (null == synthesizer.getDefaultSoundbank()) ? false : true;
		
		Receiver rec = null;
		if (isSoftware) {
			synthesizer.open();
			
			// load chosen soundbank and initialize it's instruments
			boolean isCustomSoundbankLoaded = false;
			if (selectedSoundbank != null) {
				
				// soundbank supported?
				if (synthesizer.isSoundbankSupported(selectedSoundbank)) {
					isCustomSoundbankLoaded = synthesizer.loadAllInstruments(selectedSoundbank);
					
					// load instruments from custom soundbank
					if (isCustomSoundbankLoaded)
						initInstrumentsIfNotYetDone(isSoftware);
					
					// soundbank not loaded
					else
						playerController.showErrorMessage(Dict.get(Dict.ERROR_SOUNDBANK_LOADING_FAILED));
				}
				else {
					// soundbank not supported
					playerController.showErrorMessage(Dict.get(Dict.ERROR_SOUNDBANK_NOT_SUPPORTED));
				}
			}
			
			// load instruments from default soundbank
			if (! isCustomSoundbankLoaded)
				initInstrumentsIfNotYetDone(isSoftware);
			
			rec = synthesizer.getReceiver();
		}
		else {
			// hardware
			rec = receiver = MidiSystem.getReceiver();
			initInstrumentsIfNotYetDone(isSoftware);
		}
		
		return rec;
	}
	
	/**
	 * Initializes the instruments of the right soundbank, if not yet done.
	 * 
	 * @param soundbankAvailable  **true** if a software soundbank is available;
	 *                            **false** if a hardware synthesizer is used.
	 */
	private static void initInstrumentsIfNotYetDone(boolean soundbankAvailable) {
		if (null != instruments)
			return;
		
		// initialize channels
		instruments = new TreeMap<Byte, TreeMap<Integer, String>>();
		for (byte channel = 0; channel < NUMBER_OF_CHANNELS; channel++)
			instruments.put(channel, new TreeMap<Integer, String>());
		
		// software synthesizer
		if (soundbankAvailable) {
			ArrayList<HashMap<String, String>> soundbankInstruments = SoundfontParser.getSoundbankInstruments();
			for (byte channel = 0; channel < NUMBER_OF_CHANNELS; channel++)
				initSoftwareInstruments(channel, soundbankInstruments);
		}
		
		// hardware synthesizer
		else {
			
			// assume: 9: percussion, everything else: chromatic
			for (byte channel = 0; channel < NUMBER_OF_CHANNELS; channel++) {
				TreeMap<Integer, String> channelInstruments = instruments.get(channel);
				
				// percussion
				if (9 == channel) {
					// assume: MSB=0, LSB=0, program: 27-87
					ArrayList<InstrumentElement> predefinedDrumkits = Dict.getDrumkitList();
					for (InstrumentElement drumkit : predefinedDrumkits) {
						int key = drumkit.instrNum << 14;         // program * 2^14 + 0 + 0
						channelInstruments.put(key, drumkit.name);
					}
				}
				
				// chromatic
				else {
					// assume: MSB=0, LSB=0, program: 0-127
					ArrayList<InstrumentElement> predefinedInstruments = Dict.getInstrumentList();
					for (InstrumentElement instr : predefinedInstruments) {
						int key = instr.instrNum << 14;           // program * 2^14 + 0 + 0
						channelInstruments.put(key, instr.name);
					}
				}
			}
		}
	}
	
	/**
	 * Initializes the instruments of a software soundbank, that are
	 * supported for the given channel.
	 * 
	 * @param channel                 MIDI channel
	 * @param soundbankInstruments    pre-analyzed soundbank instruments
	 */
	private static void initSoftwareInstruments(byte channel, ArrayList<HashMap<String, String>> soundbankInstruments) {
		
		INSTRUMENT:
		for (HashMap<String, String> instr : soundbankInstruments) {
			
			// ignore categories
			String category = instr.get("category");
			if (category != null)
				continue INSTRUMENT;
			
			// ignore un-supported channels
			boolean isChannelSupported = false;
			String[] allowedChannels = instr.get("channels_long").split(",");
			for (String channelStr : allowedChannels)
				if (String.valueOf(channel).equals(channelStr))
					isChannelSupported = true;
			if (! isChannelSupported)
				continue INSTRUMENT;
			
			// get instrument data
			int    bankMSB   = Integer.parseInt( instr.get("bank_msb") );
			int    bankLSB   = Integer.parseInt( instr.get("bank_lsb") );
			int    program   = Integer.parseInt( instr.get("program")  );
			String instrName = instr.get("name");
			
			// construct key for the data structure: program * 2^14 + bankMSB * 2^7 + bankLSB
			int key = (program << 14) | (bankMSB << 7) | bankLSB;
			
			// remember instrument name
			instruments.get(channel).put(key, instrName);
		}
	}
	
	/**
	 * Is called if the activity of a channel has changed from
	 * active to inactive or the other way round.
	 * Gets the new channel activity from the {@link SequenceAnalyzer}.
	 * 
	 * @param channel    Channel number from 0 to 15.
	 */
	public static void refreshChannelActivity(byte channel) {
		boolean active = SequenceAnalyzer.getChannelActivity(channel, getTickPosition());
		playerController.setChannelActivity(channel, active);
	}
	
	/**
	 * Is queried if the displayed lyrics must change.
	 * Gets the new lyrics from the {@link SequenceAnalyzer} and
	 * changes the UI accordingly.
	 */
	public static void refreshLyrics() {
		String lyrics = KaraokeAnalyzer.getLyricsForPlayer(getTickPosition());
		playerController.setLyrics(lyrics);
	}
	
	/**
	 * Closes and destroys all MIDI devices.
	 */
	public static void destroyDevices() {
		
		// destroy sequencer
		if (null != sequencer) {
			if (sequencer.isRunning())
				sequencer.stop();
			if (sequencer.isOpen())
				sequencer.close();
		}
		sequencer = null;
		
		// destroy software synthesizer
		if (null != synthesizer) {
			if (synthesizer.isOpen())
				synthesizer.close();
		}
		synthesizer = null;
		
		// destroy receiver of hardware synthesizer
		receiver = null;
		
		// reset instruments
		instruments = null;
	}
	
	/**
	 * Starts to play the current MIDI sequence - or continues to play a paused sequence.
	 * 
	 * @throws IllegalStateException
	 */
	public static void play() throws IllegalStateException {
		if (null != sequencer) {
			// find out if we have to refresh the volume settings in the synthesizer
			boolean rememberNeeded = false;
			if (0 == sequencer.getTickPosition())
				rememberNeeded = true;
			
			// play
			sequencer.start();
			
			// refresh if necessary
			if (rememberNeeded)
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
		if (null != sequencer)
			sequencer.stop();
	}
	
	/**
	 * Stops a MIDI sequence that is currently being played and resets it's current position.
	 * 
	 * @throws MidiUnavailableException
	 * @throws IllegalStateException       if the sequencer is closed.
	 */
	public static void stop() throws MidiUnavailableException, IllegalStateException {
		if (null != sequencer) {
			sequencer.stop();
		}
		setTickPosition(0);
	}
	
	/**
	 * Increments the current position in the MIDI stream by 4 quarter notes.
	 */
	public static void forward() {
		if (null != sequencer) {
			long totalTicks   = sequencer.getTickLength();
			long currentTicks = sequencer.getTickPosition();
			currentTicks += skipQuarters * SequenceCreator.getResolution();
			if (currentTicks >= totalTicks)
				currentTicks = totalTicks - 1;
			setTickPosition(currentTicks);
		}
	}
	
	/**
	 * Increments the current position in the MIDI stream by 16 quarter notes.
	 */
	public static void fastForward() {
		if (null != sequencer) {
			long totalTicks   = sequencer.getTickLength();
			long currentTicks = sequencer.getTickPosition();
			currentTicks += skipFastQuarters * SequenceCreator.getResolution();
			if (currentTicks >= totalTicks)
				currentTicks = totalTicks - 1;
			setTickPosition(currentTicks);
		}
	}
	
	/**
	 * Decrements the current position in the MIDI stream by 4 quarter notes.
	 */
	public static void rewind() {
		if (null != sequencer) {
			long currentTicks = sequencer.getTickPosition();
			currentTicks -= skipQuarters * SequenceCreator.getResolution();
			if (currentTicks < 0)
				currentTicks = 0;
			setTickPosition(currentTicks);
		}
	}
	
	/**
	 * Decrements the current position in the MIDI stream by 16 quarter notes.
	 */
	public static void fastRewind() {
		if (null != sequencer) {
			long currentTicks = sequencer.getTickPosition();
			currentTicks -= skipFastQuarters * SequenceCreator.getResolution();
			if (currentTicks < 0)
				currentTicks = 0;
			setTickPosition(currentTicks);
		}
	}
	
	/**
	 * Returns the current tick position in the MIDI stream.
	 * 
	 * @return    Current tickstamp.
	 */
	public static long getTickPosition() {
		if (null != sequencer) {
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
		if (null != sequencer) {
			long microseconds = sequencer.getMicrosecondPosition();
			return microsecondsToTimeString(microseconds);
		}
		else
			return Dict.get(Dict.TIME_INFO_UNAVAILABLE);
	}
	
	/**
	 * Returns the length of the current MIDI stream in ticks.
	 * 
	 * @return    tick length of the current MIDI stream.
	 */
	public static long getTickLength() {
		if (null != sequencer) {
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
		if (null != sequencer) {
			long microseconds = sequencer.getMicrosecondLength();
			return microsecondsToTimeString(microseconds);
		}
		else
			return Dict.get(Dict.TIME_INFO_UNAVAILABLE);
	}
	
	/**
	 * Transforms the given microseconds into a time string in the format **hh:mm:ss**.
	 * 
	 * @param microseconds    number of microseconds to be transformed.
	 * @return                time string in the format **hh:mm:ss**.
	 */
	public static String microsecondsToTimeString(long microseconds) {
		// get number of full seconds ignoring the rest of an opened second
		int rest    = (int) microseconds / 1000000; // full seconds
		int seconds = rest % 60;
		rest        = rest / 60; // full minutes
		int minutes = rest % 60;
		int hours   = rest / 60;
		
		return String.format("%02d:%02d:%02d", hours, minutes, seconds);
	}
	
	/**
	 * Sets the position of the current MIDI stream to the given value in ticks.
	 * 
	 * Also refreshes the channel activity and note history.
	 * 
	 * @param pos    Tickstamp to be set.
	 */
	public static void setTickPosition(long pos) {
		if (null != sequencer)
			sequencer.setTickPosition(pos);
		
		// reload channel activity
		for (byte channel = 0; channel < NUMBER_OF_CHANNELS; channel++)
			refreshChannelActivity(channel);
		
		// reload note history
		for (byte channel = 0; channel < NUMBER_OF_CHANNELS; channel++)
			refreshNoteHistory(channel);
		
		// refresh instrument name, bank number and channel comment
		for (byte channel = 0; channel < NUMBER_OF_CHANNELS; channel++)
			refreshInstrument(channel);
		
		// refresh the lyrics
		refreshLyrics();
		
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
			Thread.sleep(WAITING_TIME_BEFORE_REMEMBER);
		}
		catch (InterruptedException e) {
		}
		setMasterVolume();
		setAllChannelVolumes();
	}
	
	/**
	 * Determines if the sequencer is currently playing.
	 * 
	 * @return    **true**, if the sequencer is playing; otherwise: **false**.
	 */
	public static boolean isPlaying() {
		if (null == sequencer)
			return false;
		if (sequencer.isRunning())
			return true;
		return false;
	}
	
	/**
	 * Sets the tempo factor of the sequencer.
	 * 
	 * @param factor    Tempo factor.
	 */
	public static void setTempo(float factor) {
		if (null == sequencer)
			return;
		sequencer.setTempoFactor(factor);
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
	 * Returns the current master volume MSB.
	 * 
	 * @return    master volume MSB.
	 */
	public static byte getMasterVolume() {
		return masterVolumeMsb;
	}
	
	/**
	 * Sets the channel volume of every channel to the currently configured value.
	 */
	private static void setAllChannelVolumes() {
		if (null != synthesizer)
			for (int i=0; i< synthesizer.getChannels().length; i++)
				sendChangeChannelVolumeMsg(i, channelVolumeMsb[i], channelVolumeLsb[i]);
	}
	
	/**
	 * Sets the master volume configuration to the given values.
	 * Changes the master volume accordingly.
	 * 
	 * @param volMsb    Most significant byte of the master volume.
	 * @param volLsb    Most significant byte of the master volume.
	 */
	public static void setMasterVolume(byte volMsb, byte volLsb) {
		
		masterVolumeMsb = volMsb;
		masterVolumeLsb = volLsb;
		
		setMasterVolume();
	}
	
	/**
	 * Sets the master volume to the currently configured value by sending an according SysEx message.
	 */
	private static void setMasterVolume() {
		try {
			// create message
			SysexMessage msg = new SysexMessage();
			byte[] data = {
				(byte) 0xF0, // status byte for sysex
				0x7F, // universal realtime
				0x7F, // sysex channel (0x7F = all devices)
				0x04, // sub ID: device control
				0x01, // sub ID 2: master volume
				masterVolumeLsb,
				masterVolumeMsb
			};
			msg.setMessage(data, data.length);
			
			// send message
			sendMessage(msg);
		}
		catch(InvalidMidiDataException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Sends the given MIDI message immediately to the receiver, if possible.
	 * 
	 * @param msg  MIDI message.
	 */
	private static void sendMessage(MidiMessage msg) {
		
		// find the right receiver
		Receiver rec = receiver;
		if (rec == null && synthesizer != null) {
			try {
				rec = synthesizer.getReceiver();
			}
			catch (MidiUnavailableException e) {
			}
		}
		
		// send the message
		if (rec != null) {
			rec.send(msg, -1);
		}
	}
	
	/**
	 * Sets the volume of a channel.
	 * Sets the configuration and sends according message for MSB and LSB.
	 * 
	 * @param channelNumber    Channel number from 0 to 15.
	 * @param volMsb           Most significant byte of the channel volume (0-127).
	 * @param volLsb           Most significant byte of the channel volume (0-127).
	 */
	public static void setChannelVolume(int channelNumber, byte volMsb, byte volLsb) {
		
		// store the new value
		channelVolumeMsb[channelNumber] = volMsb;
		channelVolumeLsb[channelNumber] = volLsb;
		
		sendChangeChannelVolumeMsg(channelNumber, volMsb, volLsb);
	}
	
	/**
	 * Changes the volume of a channel by sending according MIDI messages.
	 * 
	 * @param channelNumber    Channel number from 0 to 15.
	 * @param volMsb           Most significant byte (number from 0 to 127).
	 * @param volLsb           Least significant byte (number from 0 to 127).
	 */
	private static void sendChangeChannelVolumeMsg(int channelNumber, byte volMsb, byte volLsb) {
		if (null != receiver) {
			try {
				ShortMessage msbMessage = new ShortMessage();
				msbMessage.setMessage(
					ShortMessage.CONTROL_CHANGE,
					channelNumber,
					0x07,
					volMsb
				);
				receiver.send(msbMessage, -1);
				ShortMessage lsbMessage = new ShortMessage();
				lsbMessage.setMessage(
					ShortMessage.CONTROL_CHANGE,
					channelNumber,
					0x27,
					volLsb
				);
				receiver.send(lsbMessage, -1);
			}
			catch (InvalidMidiDataException e) {
				e.printStackTrace();
			}
		}
		else if (null != synthesizer) {
			MidiChannel channel = synthesizer.getChannels()[channelNumber];
			channel.controlChange(0x07, volMsb);
			channel.controlChange(0x27, volLsb);
		}
	}
	
	/**
	 * Returns the current volume MSB of the given channel.
	 * 
	 * @param channel    Channel number from 0 to 15.
	 * @return           the MSB of the channel volume (0 to +127).
	 */
	public static byte getChannelVolume(byte channel) {
		return channelVolumeMsb[channel];
	}
	
	/**
	 * Sets the mute state of the given channel.
	 * A muted channel doesn't produce an audio signal while playing.
	 * 
	 * @param channel    Channel number from 0 to 15.
	 * @param mute       **true**: muted; **false**: not muted.
	 */
	public static void setMute(int channel, boolean mute) {
		channelMute[channel] = mute;
		if (null == synthesizer)
			return;
		synthesizer.getChannels()[channel].setMute(mute);
	}
	
	/**
	 * Determines if the given channel is muted.
	 * A muted channel doesn't produce an audio signal while playing.
	 * 
	 * @param channel    Channel number from 0 to 15.
	 * @return           **true**, if the channel is muted. Otherwise: **false**.
	 */
	public static boolean getMute(int channel) {
		return channelMute[channel];
	}
	
	/**
	 * Determines if the given channel is soloed.
	 * If at least one channel is soloed at a time, than only soloed channels produce
	 * an audio signal while playing. All non-soloed channels stay quiet.
	 * 
	 * @param channel    Channel number from 0 to 15.
	 * @return           **true**, if the channel is soloed. Otherwise: **false**.
	 */
	public static boolean getSolo(int channel) {
		return channelSolo[channel];
	}
	
	/**
	 * Sets the solo state of the given channel.
	 * If at least one channel is soloed at a time, than only soloed channels produce
	 * an audio signal while playing. All non-soloed channels stay quiet.
	 * 
	 * @param channel    Channel number from 0 to 15.
	 * @param solo       **true**: solo; **false**: not solo.
	 */
	public static void setSolo(int channel, boolean solo) {
		channelSolo[channel] = solo;
		if (null == synthesizer)
			return;
		synthesizer.getChannels()[channel].setSolo(solo);
	}
	
	/**
	 * Is called if at least one NOTE-ON event an a channel has occurred.
	 * Informs the according table model about the change.
	 * 
	 * @param channel  Channel number from 0 to 15.
	 */
	public static void refreshNoteHistory(byte channel) {
		noteHistoryObservers.get(channel).fireTableDataChanged();
	}
	
	/**
	 * Queries the current bank and instrument information and channel comment
	 * from the {@link SequenceAnalyzer} and displays it on the player UI.
	 * 
	 * @param channel  Channel number from 0 to 15.
	 */
	public static void refreshInstrument(byte channel) {
		
		// query information
		long   tick           = getTickPosition();
		Byte[] instrumentInfo = SequenceAnalyzer.getInstrument(channel, tick);
		String comment        = SequenceAnalyzer.getChannelComment(channel, tick);
		byte   bankMSB        = instrumentInfo[0];
		byte   bankLSB        = instrumentInfo[1];
		byte   program        = instrumentInfo[2];
		
		// channel not used at all?
		if (-1 == bankMSB) {
			// return default config
			playerController.setChannelInfo(channel, "", "", "", "", "");
			
			return;
		}
		
		// bank number like it's used in the syntax
		// if LSB is set: MSB, separator, LSB
		// otherwise: MSB
		String bankNum = Byte.toString(bankMSB);
		if (bankLSB > 0)
			bankNum += Dict.getSyntax(Dict.SYNTAX_BANK_SEP) + Byte.toString(bankLSB);
		
		// full bank number = MSB * 128 + LSB
		int fullBankNum = (bankMSB << 7) + bankLSB;
		String fullBankNumStr = Integer.toString(fullBankNum);
		
		// bank number tooltip
		String bankTooltip = fullBankNumStr + " (MSB: " + bankMSB + ", LSB: " + bankLSB + ")";
		
		// program number
		String progNumStr = Byte.toString(program);
		
		// instrument name
		String instrName = Dict.get(Dict.UNKNOWN_INSTRUMENT);
		try {
			// construct key for the data structure: program * 2^14 + bankMSB * 2^7 + bankLSB
			int key     = (((int) program) << 14) | (((int) bankMSB) << 7) | ((int) bankLSB);
			String name = instruments.get(channel).get(key);
			if (null != name)
				instrName = name;
		}
		catch (NullPointerException e) {
			// nothing more to do
		}
		
		playerController.setChannelInfo(channel, bankNum, bankTooltip, progNumStr, instrName, comment);
	}
	
	/**
	 * Removes all note history observers from the according data structure.
	 * Initializes the data structure, if not yet done.
	 */
	public static void resetNoteHistoryObservers() {
		if (null == noteHistoryObservers)
			noteHistoryObservers = new ArrayList<AbstractTableModel>();
		noteHistoryObservers.removeAll(noteHistoryObservers);
	}
	
	/**
	 * Adds the given observer to the data structure for note history observers.
	 * The observer will be triggered whenever a note in the given channel is played.
	 * Then it is responsible for refreshing the note history table for that channel.
	 * 
	 * @param observer    Note history observer.
	 * @param channel     Channel number from 0 to 15.
	 */
	public static void addNoteHistoryObserver(AbstractTableModel observer, byte channel) {
		noteHistoryObservers.add(channel, observer);
	}
	
	/**
	 * Sets the given soundbank so that it can be used by the synthesizer.
	 * 
	 * @param soundbank    Custom soundbank.
	 */
	public static void setSoundbank(Soundbank soundbank) {
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
		if (selectedSoundbank != null)
			return selectedSoundbank;
		
		// create a synthesizer, if not yet done
		if (null == synthesizer) {
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
	 * @param channel     Channel number from 0 to 15.
	 * @param instr       Instrument specification (program number, bank MSB, bank LSB)
	 * @param note        Note or percussion instrument number.
	 * @param volume      Channel volume for the note to be played.
	 * @param velocity    Velocity (note volume) for the note to be played.
	 * @param duration    Note length in milliseconds.
	 * @param keep        **true** to keep the settings after playing the note, **false**
	 *                    to restore the channel's state.
	 */
	public static void doSoundcheck(int channel, int[] instr, int note, byte volume, int velocity, int duration, boolean keep) {
		if (null == synthesizer)
			return;
		
		// unpack instrument parts
		int program = instr[0];
		int bankMSB = instr[1];
		int bankLSB = instr[2];
		
		// set bank instrument and volume
		MidiChannel midiChannel = synthesizer.getChannels()[channel];
		midiChannel.controlChange(0x00, bankMSB);
		midiChannel.controlChange(0x20, bankLSB);
		midiChannel.programChange(program);
		sendChangeChannelVolumeMsg(channel, volume, volume);
		
		// note on
		midiChannel.noteOn(note, velocity);
		
		// wait
		try {
			Thread.sleep(duration);
		}
		catch (InterruptedException e) {
		}
		
		// note off
		midiChannel.noteOff(note);
		
		// keep or restore bank, instrument and volume
		if (keep)
			return;
		restoreChannelAfterSoundcheck(channel);
	}
	
	/**
	 * Restores the given channel's state after the channel has been changed
	 * for a soundcheck.
	 * 
	 * This is called from the soundcheck method directly (if the keep
	 * checkbox is unchecked) or after a soundcheck when the keep checkbox
	 * is un-checked later.
	 * 
	 * @param channel Channel number.
	 */
	public static void restoreChannelAfterSoundcheck(int channel) {
		
		// restore bank
		MidiChannel midiChannel = synthesizer.getChannels()[channel];
		Byte[] instrumentInfo = SequenceAnalyzer.getInstrument((byte) channel, getTickPosition());
		byte oldBankMSB = instrumentInfo[0];
		byte oldBankLSB = instrumentInfo[1];
		byte oldProgram = instrumentInfo[2];
		midiChannel.controlChange(0x00, oldBankMSB);
		midiChannel.controlChange(0x20, oldBankLSB);
		
		// restore program (instrument)
		midiChannel.programChange(oldProgram);
		
		// restore volume
		sendChangeChannelVolumeMsg(channel, channelVolumeMsb[channel], channelVolumeLsb[channel]);
	}
}
