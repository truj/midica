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
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import org.midica.Midica;
import org.midica.config.Dict;
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
	
	/** Data structure for general information of the currently loaded soundbank. */
	private static HashMap<String, String> generalInfo = null;
	
	/** Data structure for instruments and drum kits of the currently loaded soundbank. */
	private static ArrayList<HashMap<String, String>> soundfontInstruments = null;
	
	/** Data structure for resources of the currently loaded soundbank. */
	private static ArrayList<HashMap<String, Object>> soundfontResources = null;
	
	
	/**
	 * Parses a soundfont file.
	 * 
	 * @param file             Soundfont file chosen by the user.
	 * @throws ParseException  If the file can not be loaded correctly.
	 */
	public void parse( File file ) throws ParseException{
		try {
			// load the soundfont
			soundfont = MidiSystem.getSoundbank( file );
			MidiDevices.setSoundfont( soundfont );
			
			// read it and build up data structures
			parseSoundfontInstruments();
			parseSoundfontResources();
			parseSoundfontInfo();
		}
		catch ( InvalidMidiDataException e ) {
			throw new ParseException( e.getMessage() );
		}
		catch ( IOException e ) {
			throw new ParseException( e.getMessage() );
		}
	}
	
	/**
	 * Returns general information from the currently loaded soundfont.
	 * 
	 * @return general soundfont information.
	 */
	public static HashMap<String, String> getSoundfontInfo() {
		
		// parse, if not yet done
		if ( null == generalInfo )
			parseSoundfontInfo();
		
		return generalInfo;
	}
	
	/**
	 * Returns instruments and drum kits from the currently loaded soundfont.
	 * 
	 * @return Instruments from the soundfont.
	 */
	public static ArrayList<HashMap<String, String>> getSoundfontInstruments() {
		
		// parse, if not yet done
		if ( null == soundfontInstruments )
			parseSoundfontInstruments();
		
		return soundfontInstruments;
	}
	
	/**
	 * Returns resources from the currently loaded soundfont.
	 * 
	 * @return Resources from the soundfont.
	 */
	public static ArrayList<HashMap<String, Object>> getSoundfontResources() {
		// parse, if not yet done
		if ( null == soundfontResources )
			parseSoundfontResources();
		
		return soundfontResources;
	}
	
	/**
	 * Retrieves general information from the currently loaded soundfont.
	 */
	private static void parseSoundfontInfo() {
		
		generalInfo = new HashMap<String, String>();
		Soundbank soundfont = MidiDevices.getSoundfont();
		
		// get file name from the UI
		generalInfo.put( "file", Midica.uiController.getView().getChosenSoundfontFileLbl().getText() );
		
		// no soundfont loaded?
		if ( null == soundfont ) {
			generalInfo.put( "name",        "-" );
			generalInfo.put( "version",     "-" );
			generalInfo.put( "vendor",      "-" );
			generalInfo.put( "description", "-" );
			
			return;
		}
		
		// get general information
		generalInfo.put( "name",        soundfont.getName()        );
		generalInfo.put( "version",     soundfont.getVersion()     );
		generalInfo.put( "vendor",      soundfont.getVendor()      );
		generalInfo.put( "description", soundfont.getDescription() );
		
		return;
	}
	
	/**
	 * Retrieves instruments and drum kits from the currently loaded soundfont.
	 */
	private static void parseSoundfontInstruments() {
		
		// initialize
		soundfontInstruments = new ArrayList<HashMap<String, String>>();
		Soundbank soundfont  = MidiDevices.getSoundfont();
		boolean needCategoryDrumkit   = false;
		boolean needCategoryChromatic = false;
		boolean needCategoryUnknown   = false;
		
		// no soundfont loaded?
		if ( null == soundfont )
			return;
		
		// collect instruments
		Instrument[] instruments = soundfont.getInstruments();
		for ( int i=0; i < instruments.length; i++ ) {
			
			// add general instrument data
			HashMap<String, String> instrument = new HashMap<String, String>();
			soundfontInstruments.add( instrument );
			Instrument midiInstr = instruments[ i ];
			Patch      patch     = midiInstr.getPatch();
			int        bank      = patch.getBank();
			instrument.put( "name",    midiInstr.getName()                  );
			instrument.put( "program", Integer.toString(patch.getProgram()) );
			instrument.put( "bank",    Integer.toString(bank)               );
			instrument.put( "bankMSB", Integer.toString(bank >> 7)          );
			instrument.put( "bankLSB", Integer.toString(bank % 128)         );
			
			// add class specific instrument data
			if ( ! (midiInstr instanceof SF2Instrument) ) {
				
				// unknown class
				needCategoryUnknown = true;
				instrument.put( "channels", "-" );
				instrument.put( "keys",     "-" );
				instrument.put( "type",     "-" );
				
				continue;
			}
			
			// get channels
			boolean            hasChannel0 = false;
			boolean            hasChannel9 = false;
			SF2Instrument      sf2Instr    = (SF2Instrument) midiInstr;
			boolean[]          sf2Channels = sf2Instr.getChannels();
			ArrayList<Integer> channels    = new ArrayList<Integer>();
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
			
			// Try to guess the type: chromatic or drum kit.
			Pattern pattern = Pattern.compile( "^Drumkit:.*" );
			Matcher matcher = pattern.matcher( sf2Instr.toString() );
			if ( matcher.matches() ) {
				needCategoryDrumkit = true;
				instrument.put( "type", "drumkit" );
				continue;
			}
			if ( hasChannel9 && ! hasChannel0 ) {
				needCategoryDrumkit = true;
				instrument.put( "type", "drumkit" );
				continue;
			}
			if ( hasChannel0 && ! hasChannel9 ) {
				needCategoryChromatic = true;
				instrument.put( "type", "chromatic" );
				continue;
			}
			
			// Still in the loop, so we failed to guess.
			needCategoryUnknown = true;
			instrument.put( "type", "-" );
		}
		
		// add categories
		if (needCategoryChromatic) {
			HashMap<String, String> category = new HashMap<String, String>();
			category.put( "category", "category" );
			category.put( "type",     "categoryChromatic" );
			category.put( "name",     Dict.get(Dict.SF_INSTR_CAT_CHROMATIC) );
			soundfontInstruments.add( category );
		}
		if (needCategoryDrumkit) {
			HashMap<String, String> category = new HashMap<String, String>();
			category.put( "category", "category" );
			category.put( "type",     "categoryDrumkit" );
			category.put( "name",     Dict.get(Dict.SF_INSTR_CAT_DRUMKIT) );
			soundfontInstruments.add( category );
		}
		if (needCategoryUnknown) {
			HashMap<String, String> category = new HashMap<String, String>();
			category.put( "category", "category" );
			category.put( "type",     "categoryUnknown" );
			category.put( "name",     Dict.get(Dict.SF_INSTR_CAT_UNKNOWN) );
			soundfontInstruments.add( category );
		}
		
		// sort instruments
		// drumkits first, then chromatic instruments, then unknown types
		// inside a type category: order by program number, then by bank number
		Comparator<HashMap<String, String>> instrumentComparator = new Comparator<HashMap<String, String>>() {
			
			/** Sorting priority for the type. */
			private HashMap<String, Integer> typePriority = new HashMap<String, Integer>();
			
			// initialize type sorting priorities
			{
				typePriority.put( "categoryDrumkit",   6 );
				typePriority.put( "drumkit",           5 );
				typePriority.put( "categoryChromatic", 4 );
				typePriority.put( "chromatic",         3 );
				typePriority.put( "categoryUnknown",   2 );
				typePriority.put( "-",                 1 );
			}
			
			@Override
			public int compare( HashMap<String, String> instrA, HashMap<String, String> instrB ) {
				
				// first priority: type
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
		Collections.sort( soundfontInstruments, instrumentComparator );
		
		return;
	}
	
	/**
	 * Retrieves resources from the soundfont.
	 */
	public static void parseSoundfontResources() {
		
		// initialize
		soundfontResources  = new ArrayList<HashMap<String, Object>>();
		Soundbank soundfont = MidiDevices.getSoundfont();
		boolean needCategorySample  = false;
		boolean needCategoryLayer   = false;
		boolean needCategoryUnknown = false;
		
		// no soundfont loaded?
		if ( null == soundfont )
			return;
		
		// collect resources
		SoundbankResource[] resources = soundfont.getResources();
		for ( int i=0; i < resources.length; i++ ) {
			
			HashMap<String, Object> resource = new HashMap<String, Object>();
			soundfontResources.add( resource );
			
			// apply general information and defaults
			resource.put( "index",       i );
			resource.put( "name",        resources[i].getName() );
			resource.put( "class",       "-" );
			resource.put( "classDetail", "-" );
			resource.put( "type",        "-" );
			resource.put( "format",      "-" );
			resource.put( "frameLength", "-" );
			String classDesc      = resources[i].toString();
			Pattern pattern       = Pattern.compile( "^(.+):.*" );
			Matcher matcher       = pattern.matcher( classDesc );
			String identifiedType = null;
			if ( matcher.matches() ) {
				String type = matcher.group( 1 );
				resource.put( "type", type );
				if ( "Layer".equals(type) ) {
					needCategoryLayer = true;
					identifiedType    = type;
				}
			}
			
			// apply null-class information
			Class<?> dataClass = resources[ i ].getDataClass();
			if ( null == dataClass ) {
				resource.put( "class",       "null" );
				resource.put( "classDetail", "null" );
			}
			
			// apply class name information
			else {
				String fullClassName = dataClass.getCanonicalName();
				Pattern classPattern = Pattern.compile( ".+\\.([^.]+)$" );
				Matcher classMatcher = classPattern.matcher( fullClassName );
				if ( classMatcher.matches() ) {
					resource.put( "class", classMatcher.group(1) );
				}
				else {
					resource.put( "class", fullClassName );
				}
				resource.put( "classDetail", fullClassName );
			}
			
			// apply class-dependant information
			if ( dataClass == AudioInputStream.class ) {
				
				needCategorySample      = true;
				identifiedType          = "Sample";
				AudioInputStream stream = (AudioInputStream) resources[i].getData();
				resource.put( "frameLength",  String.valueOf(stream.getFrameLength()) );
				resource.put( "formatDetail", String.valueOf(stream.getFormat())      );
				
				// construct and add a format summary field
				AudioFormat format    = stream.getFormat();
				String      encoding  = format.getEncoding().toString().toLowerCase();
				float       frameRate = format.getFrameRate();
				int         frameSize = format.getFrameSize();
				int         bitRate   = format.getSampleSizeInBits();
				String      frameKHz  = String.format( "%.1f", frameRate / 1000 );
				String      channels  = "m"; // m=mono, s=stereo
				if ( 2 == format.getChannels() ) {
					channels = "s";
				}
				String formatStr = encoding + " " + frameKHz + " kHz, " + bitRate + " bit "
				                 + channels + ", " + frameSize + " B/frm";
				resource.put( "format", formatStr );
				
				// close the stream to avoid an exception caused by "too many open files"
				try {
	                stream.close();
                }
                catch ( IOException e ) {
                }
			}
			
			// We failed to guess the type?
			if ( null == identifiedType )
				needCategoryUnknown = true;
		}
		
		// add categories
		if (needCategorySample) {
			HashMap<String, Object> category = new HashMap<String, Object>();
			category.put( "category", "category" );
			category.put( "type",     "categorySample" );
			category.put( "name",     Dict.get(Dict.SF_RESOURCE_CAT_SAMPLE) );
			soundfontResources.add( category );
		}
		if (needCategoryLayer) {
			HashMap<String, Object> category = new HashMap<String, Object>();
			category.put( "category", "category" );
			category.put( "type",     "categoryLayer" );
			category.put( "name",     Dict.get(Dict.SF_RESOURCE_CAT_LAYER) );
			soundfontResources.add( category );
		}
		if (needCategoryUnknown) {
			HashMap<String, Object> category = new HashMap<String, Object>();
			category.put( "category", "category" );
			category.put( "type",     "categoryUnknown" );
			category.put( "name",     Dict.get(Dict.SF_RESOURCE_CAT_UNKNOWN) );
			soundfontResources.add( category );
		}
		
		// sort resources
		// samples first, then layers, then unknown types
		// inside a type category: keep the order of the soundfont (order by index)
		Comparator<HashMap<String, Object>> instrumentComparator = new Comparator<HashMap<String, Object>>() {
			
			/** Sorting priority for the type. */
			private HashMap<String, Integer> typePriority = new HashMap<String, Integer>();
			
			// initialize type sorting priorities
			{
				typePriority.put( "categorySample",  6 );
				typePriority.put( "Sample",          5 );
				typePriority.put( "categoryLayer",   4 );
				typePriority.put( "Layer",           3 );
				typePriority.put( "categoryUnknown", 2 );
				typePriority.put( "-",               1 );
			}
			
			@Override
			public int compare( HashMap<String, Object> resourceA, HashMap<String, Object> resourceB ) {
				
				// first priority: type
				int priorityA = typePriority.get( resourceA.get("type") );
				int priorityB = typePriority.get( resourceB.get("type") );
				if ( priorityA > priorityB )
					return -1;
				else if ( priorityA < priorityB )
					return 1;
				
				// second priority: index
				int programA = (Integer) resourceA.get("index");
				int programB = (Integer) resourceB.get("index");
				if ( programA < programB )
					return -1;
				else if ( programA > programB )
					return 1;
				
				// default compare result
				return 0;
			}
		};
		Collections.sort( soundfontResources, instrumentComparator );
		
		return;
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
