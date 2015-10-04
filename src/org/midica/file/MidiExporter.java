/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;

import org.midica.midi.MidiDevices;
import org.midica.midi.MidiListener;

/**
 * This class is used to export the currently loaded MIDI sequence as a MIDI file.
 * 
 * @author Jan Trukenmüller
 */
public class MidiExporter extends Exporter {
	
	/**
	 * Creates a new MIDI exporter.
	 */
	public MidiExporter() {
	}
	
	/**
	 * Exports a MIDI file.
	 * 
	 * @param   file             MIDI file.
	 * @return                   Empty data structure (warnings are not used for MIDI exports).
	 * @throws  ExportException  If the file can not be exported correctly.
	 */
	public ExportResult export( File file ) throws ExportException {
		
		try {
			
			// create file writer and store it in this.writer
			if ( ! createFile(file) )
				return new ExportResult( false );
			
			// export the MIDI file
			Sequence seq = cloneSequence();
			int[] supportedFileTypes = MidiSystem.getMidiFileTypes( seq );
			MidiSystem.write( seq, supportedFileTypes[0], file );
			
		}
		catch ( FileNotFoundException e ) {
			e.printStackTrace();
		}
		catch ( IOException e ) {
			e.printStackTrace();
		}
        catch ( InvalidMidiDataException e ) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
		
		return new ExportResult( true );
	}
	
	/**
	 * Creates a modified copy of the loaded sequence.
	 * Removes meta events for key presses and key releases.
	 * Adds a Midica signature as a meta event.
	 * 
	 * @return copied and modified MIDI Sequence.
	 * @throws InvalidMidiDataException if the new MIDI (copied) sequence cannot be created.
	 */
	private Sequence cloneSequence() throws InvalidMidiDataException {
		
		Sequence oldSeq = MidiDevices.getSequence();
		Sequence newSeq = new Sequence( oldSeq.getDivisionType(), oldSeq.getResolution() );
		
		TRACK:
		for ( Track oldTrack : oldSeq.getTracks() ) {
			
			Track newTrack = newSeq.createTrack();
			
			EVENT:
			for ( int i=0; i < oldTrack.size(); i++ ) {
				MidiEvent   event = oldTrack.get( i );
				MidiMessage msg   = event.getMessage();
				
				// ignore some meta messages created by the 
				if ( msg instanceof MetaMessage ) {
					int    type = ((MetaMessage) msg).getType();
					byte[] data = ((MetaMessage) msg).getData();
					
					if ( MidiListener.META_TRACK_NAME == type) {
						continue EVENT;
					}
					if ( MidiListener.META_SEQUENCER_SPECIFIC == type && 4 == data.length ) {
						byte channel = data[ 0 ];
						byte cmd     = data[ 1 ];
						
						if ( channel >= 0 && channel <= 15 )
							if ( MidiListener.ACTIVITY_ON == cmd || MidiListener.ACTIVITY_OFF == cmd )
								continue EVENT;
					}
				}
				
				// copy the event
				newTrack.add( event );
			}
		}
		
		// TODO: add signature containing Midica Version and creation date
		
		return newSeq;
	}
}
