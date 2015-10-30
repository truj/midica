/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.midi.Instrument;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Patch;
import javax.sound.midi.Soundbank;
import javax.sound.midi.SoundbankResource;
import javax.sound.sampled.AudioInputStream;

import org.midica.Midica;
import org.midica.debug.Dumper;
import org.midica.midi.MidiDevices;

import com.sun.media.sound.SF2Instrument;

/**
 * This class is used in order to load a user-defined soundfont file.
 * 
 * It's also used for retrieving information from the currently loaded (or standard) soundfont.
 * 
 * @author Jan Trukenmüller
 */
public class SoundfontParser extends Parser {
	
	/** The currently loaded user-defined soundfont. */
	private Soundbank soundfont = null;
	
	/**
	 * Parses a soundfont file.
	 * 
	 * @param file             Soundfont file chosen by the user.
	 * @throws ParseException  If the file can not be loaded correctly.
	 */
	public void parse( File file ) throws ParseException{
		try {
			soundfont = MidiSystem.getSoundbank( file );
			MidiDevices.setSoundfont( soundfont );
		}
		catch ( InvalidMidiDataException e ) {
			throw new ParseException( e.getMessage() );
		}
		catch ( IOException e ) {
			throw new ParseException( e.getMessage() );
		}
	}
	
	/**
	 * Retrieves and returns general information from the currently loaded soundfont.
	 * 
	 * @return general soundfont information.
	 */
	public static HashMap<String, String> getSoundfontInfo() {
		
		HashMap<String, String> info = new HashMap<String, String>();
		Soundbank soundfont = MidiDevices.getSoundfont();
		
		// get file name from the UI
		info.put( "file", Midica.uiController.getView().getChosenSoundfontFileLbl().getText() );
		
		// no soundfont loaded?
		if ( null == soundfont ) {
			info.put( "name",        "-" );
			info.put( "version",     "-" );
			info.put( "vendor",      "-" );
			info.put( "description", "-" );
			
			return info;
		}
		
		// get general information
		info.put( "name",        soundfont.getName()        );
		info.put( "version",     soundfont.getVersion()     );
		info.put( "vendor",      soundfont.getVendor()      );
		info.put( "description", soundfont.getDescription() );
		
		return info;
	}
	
	/**
	 * Retrieves and returns instruments and drum kits from the currently loaded soundfont.
	 * 
	 * @param soundfont  The currently loaded soundfont object.
	 * @param info       The data structure to be enriched with the instruments.
	 * @return Instruments from the soundfont.
	 */
	public static ArrayList<HashMap<String, String>> getSoundfontInstruments() {
		
		ArrayList<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
		Soundbank soundfont = MidiDevices.getSoundfont();
		
		// no soundfont loaded?
		if ( null == soundfont )
			return result;
		
		// collect instruments
		Instrument[] instruments = soundfont.getInstruments();
		for ( int i=0; i < instruments.length; i++ ) {
			
			// add general instrument data
			HashMap<String, String> instrument = new HashMap<String, String>();
			result.add( instrument );
			Instrument midiInstr = instruments[ i ];
			Patch      patch     = midiInstr.getPatch();
			instrument.put( "name",    midiInstr.getName()                  );
			instrument.put( "bank",    Integer.toString(patch.getBank())    );
			instrument.put( "program", Integer.toString(patch.getProgram()) );
			
			// add class specific instrument data
			instrument.put( "channels", "-" );
			instrument.put( "keys",     "-" );
			instrument.put( "type",     "-" ); // instrument or drum kit
			if ( midiInstr instanceof SF2Instrument ) {
				SF2Instrument sf2Instr = (SF2Instrument) midiInstr;
				
				// get channels
				boolean   hasChannel0 = false;
				boolean   hasChannel9 = false;
				boolean[] sf2Channels = sf2Instr.getChannels();
				ArrayList<Integer> channels = new ArrayList<Integer>();
				for ( int channel = 0; channel < sf2Channels.length; channel++ ) {
					
					// channel not supported?
					if ( ! sf2Channels[channel] )
						continue;
					
					// remember the channel
					channels.add( channel );
					
					// remember if channel 0 or 9 is supported (that's a hint if this is a drum kit or an instrument)
					if ( 0 == channel )
						hasChannel0 = true;
					else if ( 9 == channel )
						hasChannel9 = true;
				}
				instrument.put( "channels", makeNumberRangeString(channels) );
				
				// get keys
				ArrayList<Integer> keys = new ArrayList<Integer>();
				String[] sf2keys = sf2Instr.getKeys();
				for ( int key = 0; key < sf2keys.length; key++ ) {
					
					// key not available?
					if ( null == sf2keys[key] )
						continue;
					
					// remember the key
					keys.add( key );
				}
				instrument.put( "keys", makeNumberRangeString(keys) );
				
				// guess if this is a chromatic instrument or a drum kit
				Pattern pattern = Pattern.compile( "^Drumkit:.*" );
				Matcher matcher = pattern.matcher( sf2Instr.toString() );
				if ( matcher.matches() ) {
					instrument.put( "type", "drumkit" );
					continue;
				}
				if ( hasChannel9 && ! hasChannel0 ) {
					instrument.put( "type", "drumkit" );
					continue;
				}
				if ( hasChannel0 && ! hasChannel9 ) {
					instrument.put( "type", "chromatic" );
					continue;
				}
			}
		}
		
		// sort instruments
		// drumkits first, then chromatic instruments, then unknown types
		// inside a type category: order by program number, then by bank number
		Comparator<HashMap<String, String>> instrumentComparator = new Comparator<HashMap<String, String>>() {
			
			/** Sorting priority for the type. */
			private HashMap<String, Integer> typePriority = new HashMap<String, Integer>();
			
			// initialize type sorting priorities
			{
				typePriority.put( "drumkit",   3 );
				typePriority.put( "chromatic", 2 );
				typePriority.put( "-",         1 );
			}
			
			@Override
			public int compare( HashMap<String, String> instrA, HashMap<String, String> instrB ) {
				
				// first priority: type ( drumkit > chromatic > unknown )
				int priorityA = typePriority.get( instrA.get("type") );
				int priorityB = typePriority.get( instrB.get("type") );
				if ( priorityA > priorityB )
					return -1;
				else if ( priorityA < priorityB )
					return 1;
				
				// second priority: program number
				int programA = Integer.parseInt( instrA.get("program") );
				int programB = Integer.parseInt( instrB.get("program") );
				if ( programA < programB )
					return -1;
				else if ( programA > programB )
					return 1;
				
				// third priority: bank number
				int bankA = Integer.parseInt( instrA.get("bank") );
				int bankB = Integer.parseInt( instrB.get("bank") );
				if ( bankA < bankB )
					return -1;
				else if ( bankA > bankB )
					return 1;
				
				// default compare result
				return 0;
			}
		};
		Collections.sort( result, instrumentComparator );
		
		// TODO: delete
		for ( HashMap<String, String> instr : result ) {
			Dumper.printString( instr );
		}
		
		return result;
	}
	
	/**
	 * Retrieves resources from the soundfont and adds them to the info collection.
	 * 
	 * @param soundfont  The currently loaded soundfont object.
	 * @param info       The data structure to be enriched with the resources.
	 * @return Resources from the soundfont.
	 */
	public static ArrayList<HashMap<String, Object>> getSoundfontResources() {
		
		ArrayList<HashMap<String, Object>> result = new ArrayList<HashMap<String, Object>>();
		Soundbank soundfont = MidiDevices.getSoundfont();
		
		// no soundfont loaded?
		if ( null == soundfont )
			return result;
		
		// collect resources
		SoundbankResource[] resources = soundfont.getResources();
		for ( int i=0; i < resources.length; i++ ) {
			
			HashMap<String, Object> resource = new HashMap<String, Object>();
			result.add( resource );
			
			// apply general information and defaults
			resource.put( "name",        resources[i].getName() );
			resource.put( "class",       "-" );
			resource.put( "type",        "-" );
			resource.put( "format",      "-" );
			resource.put( "frameLength", "-" );
			String classDesc = resources[i].toString();
			Pattern pattern  = Pattern.compile( "^(.+):.*" );
			Matcher matcher  = pattern.matcher( classDesc );
			if ( matcher.matches() ) {
				resource.put( "type", matcher.group(1) );
			}
			
			// apply null-class information
			Class<?> dataClass = resources[ i ].getDataClass();
			if ( null == dataClass ) {
				resource.put( "class", "null" );
			}
			
			// apply general non-null-class information
			else {
				resource.put( "class", dataClass.getCanonicalName() );
			}
			
			// apply class-dependant information
			if ( dataClass == AudioInputStream.class ) {
				
				AudioInputStream stream = (AudioInputStream) resources[i].getData();
				resource.put( "format",      String.valueOf(stream.getFormat())      );
				resource.put( "frameLength", String.valueOf(stream.getFrameLength()) );
			}
		}
		
		// TODO: sort resources
		
		
		return result;
	}
	
	/**
	 * Transforms a list of numbers into a String describing these numbers in ranges.
	 * 
	 * E.g. the list **(2, 5, 6, 7, 8, 9, 11, 15, 16, 17, 20)** would result in the
	 * string **2, 5-9, 11, 15-17, 20**
	 * 
	 * @param list  Sorted list of numbers.
	 * @return Compressed but still human-readable range string.
	 */
	private static String makeNumberRangeString( ArrayList<Integer> list ) {
		
		String result = "";
		
		// init
		int lastWritten   = -1; // last number we wrote into the result string
		int lastSeen      = -1; // last number we saw in the list
		
		// walk through the list
		for ( int i : list ) {
			
			// very first number?
			if (lastWritten < 0) {
				result = Integer.toString( i );
				lastWritten = i;
				lastSeen    = i;
				
				continue;
			}
			
			// normal incrementation?
			if ( i == lastSeen + 1 ) {
				
				// more numbers can follow. Do not yet write anything.
				lastSeen = i;
				
				continue;
			}
			
			// There was a gap.
			
			// Does the last range have only one number?
			if ( lastSeen == lastWritten ) {
				result      = result + ", " + Integer.toString( i );
				lastWritten = i;
				lastSeen    = i;
				
				continue;
			}
			
			// The last range had more than one number.
			result      = result + "-" + Integer.toString( lastSeen ) + ", " + Integer.toString( i );
			lastWritten = i;
			lastSeen    = i;
		}
		
		// list was empty?
		if ( lastWritten < 0 )
			return result;
		
		// close the list
		
		// Does the last range have only one number?
		if ( lastSeen == lastWritten )
			return result;
		
		// The last range had more than one number.
		result      = result + "-" + Integer.toString( lastSeen );
		
		return result;
	}
}
