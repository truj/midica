/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file;

import java.io.File;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Track;

import org.midica.config.Dict;
import org.midica.midi.SequenceCreator;

/**
 * An object of this class can be used in order to parse a MIDI file.
 * 
 * It uses the {@link SequenceCreator} in order to create a MIDI sequence from the parsed input
 * and includes meta events whenever a key is pressed or released.
 * This is used later for the channel activity analyzer in the player while playing the
 * MIDI sequence.
 * 
 * @author Jan Trukenmüller
 */
public class MidiParser extends SequenceParser {
	
	// Midi control messages
	public static final int CTRL_CHANGE_BANK_SELECT      =  0;
	public static final int CTRL_CHANGE_MODULATION_WHEEL =  1;
	public static final int CTRL_CHANGE_FOOT_CONTROLLER  =  4;
	public static final int CTRL_CHANGE_VOLUME           =  7;
	public static final int CTRL_CHANGE_BALANCE          =  8;
	public static final int CTRL_CHANGE_PAN              = 10;
	
	/**
	 * Parses a MIDI file.
	 * 
	 * @param file  MIDI file to be parsed.
	 */
	public void parse( File file ) throws ParseException {
		try {
			Sequence sequence = MidiSystem.getSequence( file );
			createSequence( sequence );
			postprocessSequence( sequence, "mid" ); // we want to analyze the loaded sequence - not the created one
		}
		catch ( InvalidMidiDataException e ) {
			throw new ParseException( e.getMessage() );
		}
		catch ( IOException e ) {
			throw new ParseException( e.getMessage() );
		}
	}
	
	/**
	 * Transforms the parsed MIDI sequence. Passes all events to {@link SequenceCreator}
	 * in order to create a new sequence while adding meta events for each key-press
	 * and key-release event.
	 * 
	 * @param  sequence                  The original MIDI sequence.
	 * @throws ParseException            If the input file can not be parsed correctly.
	 * @throws InvalidMidiDataException  If the created sequence is invalid.
	 */
	private void createSequence( Sequence sequence ) throws ParseException, InvalidMidiDataException {
		
		// process global parameters and initialize the sequence to create
		float divisionType = sequence.getDivisionType();
		if ( Sequence.PPQ != divisionType )
			throw new ParseException( Dict.get(Dict.ERROR_ONLY_PPQ_SUPPORTED) );
		int resolution = sequence.getResolution();
		try {
			SequenceCreator.reset( resolution );
			// init percussion channel comment
			SequenceCreator.initChannel( 9, 0, Dict.get(Dict.PERCUSSION_CHANNEL), SequenceCreator.NOW );
		}
		catch ( InvalidMidiDataException e ) {
			throw new ParseException( e.getMessage() );
		}

		
		TRACK:
		for ( Track t : sequence.getTracks() ) {
			EVENT:
			for ( int i=0; i < t.size(); i++ ) {
				MidiEvent   event = t.get( i );
				long        tick  = event.getTick();
				MidiMessage msg   = event.getMessage();
				
				if ( msg instanceof MetaMessage ) {
					processMetaMessage( (MetaMessage) msg, tick );
				}
				else if ( msg instanceof ShortMessage ) {
					processShortMessage( (ShortMessage) msg, tick );
				}
				else if ( msg instanceof SysexMessage ) {
					processSysexMessage( (SysexMessage) msg, tick );
				}
				else {
				}
			}
		}
	}
	
	/**
	 * Processes a meta message from the input sequence. Meta messages are just
	 * forwarded into the target sequence without being changed.
	 * 
	 * @param msg   Meta message from the input sequence.
	 * @param tick  Tickstamp of the message's occurrence.
	 * @throws InvalidMidiDataException
	 */
	private void processMetaMessage( MetaMessage msg, long tick ) throws InvalidMidiDataException {
		SequenceCreator.addMessageGeneric( msg, tick );
	}
	
	/**
	 * Processes a SysEx message from the input sequence. SysEx messages are just
	 * forwarded into the target sequence without being changed.
	 * 
	 * @param msg   SysEx message from the input sequence.
	 * @param tick  Tickstamp of the message's occurrence.
	 */
	private void processSysexMessage(SysexMessage msg, long tick) {
		SequenceCreator.addMessageGeneric( msg, tick );
	}
	
	/**
	 * Processes a short message from the input sequence.
	 * 
	 * If it is a **note-on** or **note-off** message than the note will be transposed
	 * according to the configured transpose level.
	 * 
	 * If it is a **program change**, **note-on** or **note-off** message than an according
	 * meta message is added to the target sequence together with the message itself.
	 * 
	 * @param msg   The short message from the input sequence.
	 * @param tick  Tickstamp of the message's occurrence.
	 * @throws InvalidMidiDataException
	 * @throws ParseException
	 */
	private void processShortMessage( ShortMessage msg, long tick ) throws InvalidMidiDataException, ParseException {
		int cmd     = msg.getCommand();
		int channel = msg.getChannel();
		int note    = msg.getData1();
		int volume  = msg.getData2();
		if ( channel < 0 || channel > 15 ) {
			// not a channel command
			System.out.println( "NOT A CHANNEL COMMAND - command: " + cmd + ", channel: " + channel );
			SequenceCreator.addMessageGeneric( msg, tick );
			return;
		}
		
		if ( ShortMessage.PROGRAM_CHANGE == cmd ) {
			
			// PROGRAM CHANGE: initialize the channel
			int    instrNum = msg.getData1();
			String comment  = "";
			if ( 9 == channel )
				comment = Dict.get( Dict.PERCUSSION_CHANNEL );
			SequenceCreator.initChannel( channel, instrNum, comment, tick );
		}
		else if ( ShortMessage.NOTE_ON == cmd && volume > 0 ) {
			
			// note on
			note = transpose( note, channel );
			// TODO: delete (used to debug channel volume changes in "And then there was silence")
//			if (9==channel&&tick>99000) {
//				System.out.println("channel: "+channel+", note: "+note+", tick:"+tick+", vol: "+volume);
//			}
			SequenceCreator.addMessageNoteON( channel, note, tick, volume );
		}
		else if ( ShortMessage.NOTE_OFF == cmd || (ShortMessage.NOTE_ON == cmd && 0 == volume) ) {
			
			// note off
			note = transpose( note, channel );
			SequenceCreator.addMessageNoteOFF( channel, note, tick );
		}
		
		else {
			// another channel command
			SequenceCreator.addMessageGeneric( msg, channel, tick );
		}
	}
}
