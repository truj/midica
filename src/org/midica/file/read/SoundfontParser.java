/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file.read;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.midi.Instrument;
import javax.sound.midi.Patch;
import javax.sound.midi.Soundbank;
import javax.sound.midi.SoundbankResource;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import org.midica.config.Dict;
import org.midica.midi.MidiDevices;

import com.sun.gervill.SF2Soundbank;

/**
 * This class is used in order to load a user-defined soundfont file.
 * 
 * It's also used for retrieving information from the currently loaded (or standard) soundfont.
 * 
 * @author Jan Trukenmüller
 */
public class SoundfontParser implements IParser {
	
	/** The currently loaded user-defined soundfont. */
	private static Soundbank soundfont = null;
	
	/** The successfully loaded soundfont file. */
	private static File soundfontFile = null;
	
	/** Data structure for general information of the currently loaded soundfont. */
	private static HashMap<String, String> generalInfo = null;
	
	/** Data structure for instruments and drum kits of the currently loaded soundfont. */
	private static ArrayList<HashMap<String, String>> soundfontInstruments = null;
	
	/** Data structure for resources of the currently loaded soundfont. */
	private static ArrayList<HashMap<String, Object>> soundfontResources = null;
	
	/**
	 * Parses a soundfont file.
	 * 
	 * @param file             Soundfont file chosen by the user.
	 * @throws ParseException  If the file can not be loaded correctly.
	 */
	public void parse(File file) throws ParseException {
		
		// reset file - in case the parsing fails
		soundfontFile = null;
		
		try {
			// load the soundfont
			soundfont = new SF2Soundbank(file);
			MidiDevices.setSoundfont(soundfont);
			
			// read it and build up data structures
			parseSoundfontInstruments();
			parseSoundfontResources();
			parseSoundfontInfo();
			
			// parsing successful - save the file info
			soundfontFile = file;
		}
		catch (IOException e) {
			throw new ParseException(e.getMessage());
		}
	}
	
	/**
	 * Returns the base name of the successfully loaded custom soundfont
	 * file.
	 * If no custom soundfont is loaded successfully, **null** is returned.
	 * 
	 * @return the soundfont file name or **null**.
	 */
	public static String getFileName() {
		if (null == soundfontFile) {
			return null;
		}
		return soundfontFile.getName();
	}
	
	/**
	 * Returns the absolute path of the successfully loaded custom soundfont
	 * file.
	 * If no custom soundfont is loaded successfully, **null** is returned.
	 * 
	 * @return the soundfont file path or **null**.
	 */
	public static String getFilePath() {
		if (null == soundfontFile) {
			return null;
		}
		try {
			return soundfontFile.getCanonicalPath();
		}
		catch (IOException e) {
			return null;
		}
	}
	
	/**
	 * Returns general information from the currently loaded soundfont.
	 * 
	 * @return general soundfont information.
	 */
	public static HashMap<String, String> getSoundfontInfo() {
		
		// parse, if not yet done
		if (null == generalInfo) {
			
			// The soundfont info parsing relies on the other data structures.
			// so we have to parse them first, before building up the info
			// structure itself.
			if (null == soundfontInstruments)
				parseSoundfontInstruments();
			if (null == soundfontResources)
				parseSoundfontResources();
			
			// Now we can parse the info.
			parseSoundfontInfo();
		}
		
		return generalInfo;
	}
	
	/**
	 * Returns instruments and drum kits from the currently loaded soundfont.
	 * 
	 * @return Instruments from the soundfont.
	 */
	public static ArrayList<HashMap<String, String>> getSoundfontInstruments() {
		
		// parse, if not yet done
		if (null == soundfontInstruments)
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
		if (null == soundfontResources)
			parseSoundfontResources();
		
		return soundfontResources;
	}
	
	/**
	 * Retrieves general information from the currently loaded soundfont.
	 */
	private static void parseSoundfontInfo() {
		
		generalInfo = new HashMap<String, String>();
		Soundbank soundfont = MidiDevices.getSoundfont();
		
		// no soundfont loaded?
		if (null == soundfont) {
			generalInfo.put( "name",                     "-" );
			generalInfo.put( "version",                  "-" );
			generalInfo.put( "vendor",                   "-" );
			generalInfo.put( "description",              "-" );
			generalInfo.put( "creation_date",            "-" );
			generalInfo.put( "tools",                    "-" );
			generalInfo.put( "product",                  "-" );
			generalInfo.put( "target_engine",            "-" );
			generalInfo.put( "chromatic_count",          "-" );
			generalInfo.put( "drumkit_single_count",     "-" );
			generalInfo.put( "drumkit_multi_count",      "-" );
			generalInfo.put( "unknown_instrument_count", "-" );
			generalInfo.put( "layer_count",              "-" );
			generalInfo.put( "sample_count",             "-" );
			generalInfo.put( "unknown_resource_count",   "-" );
			generalInfo.put( "frames_count",             "-" );
			generalInfo.put( "seconds_count",            "-" );
			generalInfo.put( "bytes_count",              "-" );
			generalInfo.put( "frames_avg",               "-" );
			generalInfo.put( "seconds_avg",              "-" );
			generalInfo.put( "bytes_avg",                "-" );
			
			return;
		}
		
		// get general information
		String unknown     = Dict.get(Dict.UNKNOWN);
		String name        = soundfont.getName();
		String version     = soundfont.getVersion();
		String vendor      = soundfont.getVendor();
		String description = soundfont.getDescription();
		generalInfo.put( "name",        name        != null ? name        : unknown );
		generalInfo.put( "version",     version     != null ? version     : unknown );
		generalInfo.put( "vendor",      vendor      != null ? vendor      : unknown );
		generalInfo.put( "description", description != null ? description : unknown );
		
		// get sf2 specific information
		String creationDate    = unknown;
		String tools           = unknown;
		String product         = unknown;
		String targetEngine    = unknown;
		try {
			Method getCreationDate = soundfont.getClass().getMethod("getCreationDate");
			Method getTools        = soundfont.getClass().getMethod("getTools");
			Method getProduct      = soundfont.getClass().getMethod("getProduct");
			Method getTargetEngine = soundfont.getClass().getMethod("getTargetEngine");
			creationDate = (String) getCreationDate.invoke(soundfont, (Object[]) null);
			tools        = (String) getTools.invoke(soundfont, (Object[]) null);
			product      = (String) getProduct.invoke(soundfont, (Object[]) null);
			targetEngine = (String) getTargetEngine.invoke(soundfont, (Object[]) null);
		}
		catch(Exception e) {
		}
		creationDate = null == creationDate ? unknown : creationDate;
		tools        = null == tools        ? unknown : tools;
		product      = null == product      ? unknown : product;
		targetEngine = null == targetEngine ? unknown : targetEngine;
		generalInfo.put( "creation_date", creationDate );
		generalInfo.put( "tools",         tools        );
		generalInfo.put( "product",       product      );
		generalInfo.put( "target_engine", targetEngine );
		
		// count instruments and drumkits
		int chromaticCount    = 0;
		int drumSingleCount   = 0;
		int drumMultiCount    = 0;
		int unknownInstrCount = 0;
		if (soundfontInstruments != null) {
			for (HashMap<String, String> instrument : soundfontInstruments) {
				String type = instrument.get("type");
				if ("chromatic".equals(type))
					chromaticCount++;
				else if ("drumkit_single".equals(type))
					drumSingleCount++;
				else if ("drumkit_multi".equals(type))
					drumMultiCount++;
				else if ("-".equals(type))
					unknownInstrCount++;
			}
		}
		generalInfo.put( "chromatic_count",          Integer.toString(chromaticCount)    );
		generalInfo.put( "drumkit_single_count",     Integer.toString(drumSingleCount)   );
		generalInfo.put( "drumkit_multi_count",      Integer.toString(drumMultiCount)    );
		generalInfo.put( "unknown_instrument_count", Integer.toString(unknownInstrCount) );
		
		// count resources
		int    layerCount      = 0;
		int    sampleCount     = 0;
		int    unknownResCount = 0;
		long   framesCount     = 0;
		double secondsCount    = 0;
		long   bytesCount      = 0;
		if (soundfontResources != null) {
			for (HashMap<String, Object> resource : soundfontResources) {
				String type = (String) resource.get("type");
				if ("Layer".equals(type))
					layerCount++;
				else if ("Sample".equals(type))
					sampleCount++;
				else if ("-".equals(type))
					unknownResCount++;
				
				// count frames, seconds and bytes
				if ("Sample".equals(type)) {
					
					Object frames = resource.get("frame_length");
					if (frames != null)
						framesCount += (Long) frames;
					
					Object seconds = resource.get("seconds");
					if (seconds != null)
						secondsCount += (Double) seconds;
					
					Object bytes = resource.get("bytes");
					if (bytes != null)
						bytesCount += (Long) bytes;
				}
			}
		}
		generalInfo.put( "layer_count",            Integer.toString(layerCount)        );
		generalInfo.put( "sample_count",           Integer.toString(sampleCount)       );
		generalInfo.put( "unknown_resource_count", Integer.toString(unknownResCount)   );
		generalInfo.put( "frames_count",           Long.toString(framesCount)          );
		generalInfo.put( "seconds_count",          String.format("%.2f", secondsCount) );
		generalInfo.put( "bytes_count",            Long.toString(bytesCount)           );
		
		// calculate average values
		float  avgFrames  = 0;
		double avgSeconds = 0;
		float  avgBytes   = 0;
		try {
			avgFrames = (float) framesCount / sampleCount;
		}
		catch (ArithmeticException e) {
		}
		try {
			avgSeconds = secondsCount / sampleCount;
		}
		catch (ArithmeticException e) {
		}
		try {
			avgBytes = (float) framesCount / sampleCount;
		}
		catch (ArithmeticException e) {
		}
		generalInfo.put( "frames_avg",  String.format("%.2f", avgFrames)  );
		generalInfo.put( "seconds_avg", String.format("%.2f", avgSeconds) );
		generalInfo.put( "bytes_avg",   String.format("%.2f", avgBytes)   );
		
		return;
	}
	
	/**
	 * Retrieves instruments and drum kits from the currently loaded soundfont.
	 */
	private static void parseSoundfontInstruments() {
		
		// initialize
		soundfontInstruments = new ArrayList<HashMap<String, String>>();
		Soundbank soundfont  = MidiDevices.getSoundfont();
		boolean needCategoryDrumkitSingle = false;
		boolean needCategoryDrumkitMulti  = false;
		boolean needCategoryChromatic     = false;
		boolean needCategoryUnknown       = false;
		
		// no soundfont loaded?
		if (null == soundfont)
			return;
		
		// collect instruments
		Instrument[] instruments = soundfont.getInstruments();
		for (int i=0; i < instruments.length; i++) {
			
			// add general instrument data
			HashMap<String, String> instrument = new HashMap<String, String>();
			soundfontInstruments.add(instrument);
			Instrument midiInstr = instruments[ i ];
			Patch      patch     = midiInstr.getPatch();
			int        bank      = patch.getBank();
			int        bankMsb   = bank >> 7;
			int        bankLsb   = bank & 0b00000000_01111111;
			int        program   = patch.getProgram();
			instrument.put( "name",     midiInstr.getName()       );
			instrument.put( "program",  Integer.toString(program) );
			instrument.put( "bank",     Integer.toString(bank)    );
			instrument.put( "bank_msb", Integer.toString(bankMsb) );
			instrument.put( "bank_lsb", Integer.toString(bankLsb) );
			
			// prepare syntax
			String syntaxDrum = Dict.getDrumkit(program);
			if (Dict.get(Dict.UNKNOWN_INSTRUMENT).equals(syntaxDrum))
				syntaxDrum = instrument.get("program");
			String syntaxChrom = Dict.getInstrument(program);
			if (Dict.get(Dict.UNKNOWN_DRUMKIT_NAME).equals(syntaxChrom))
				syntaxChrom = instrument.get("program");
			String postfix = "";
			if (bankLsb != 0)
				// TODO: test with a soundfont that uses the LSB
				postfix = Dict.getSyntax(Dict.SYNTAX_PROG_BANK_SEP) + bankMsb + Dict.getSyntax(Dict.SYNTAX_BANK_SEP) + bankLsb;
			else if (bankMsb != 0)
				postfix = Dict.getSyntax(Dict.SYNTAX_PROG_BANK_SEP) + bankMsb;
			syntaxDrum  += postfix;
			syntaxChrom += postfix;
			
			// get channels and keys
			boolean[] sf2Channels = null;
			String[]  sf2keys     = null;
			try {
				Method getChannels = midiInstr.getClass().getMethod("getChannels");
				Method getKeys     = midiInstr.getClass().getMethod("getKeys");
				sf2Channels = (boolean[]) getChannels.invoke(midiInstr, (Object[]) null);
				sf2keys     = (String[]) getKeys.invoke(midiInstr, (Object[]) null);
			}
			catch(Exception e) {
				needCategoryUnknown = true;
				instrument.put( "channels",      "-"  );
				instrument.put( "channels_long", "-1" );
				instrument.put( "keys",          "-"  );
				instrument.put( "type",          "-"  );
				instrument.put( "syntax", syntaxChrom + " / " + syntaxDrum );
				
				continue;
			}
			
			// process channels
			boolean            hasChromaticChannel = false;
			boolean            hasDrumChannel      = false;
			ArrayList<Integer> channels            = new ArrayList<Integer>();
			for (int channel = 0; channel < sf2Channels.length; channel++) {
				
				// channel not supported?
				if ( ! sf2Channels[channel] )
					continue;
				
				// remember the channel
				channels.add(channel);
				
				// remember if channel 9 or another one is supported
				if (9 == channel)
					hasDrumChannel = true;
				else
					hasChromaticChannel = true;
			}
			instrument.put("channels", makeNumberRangeString(channels)); // e.g. "0-9,10-15"
			StringBuilder channelsStr = new StringBuilder();
			for (int channel : channels) {
				if (0 == channelsStr.length())
					channelsStr.append(channel);
				else
					channelsStr.append("," + channel);
			}
			instrument.put("channels_long", channelsStr.toString()); // e.g. "0,1,2,3,4,5,6,7,8,10,11,12,13,14,15"
			
			// add syntax name
			String syntax = hasDrumChannel ? syntaxDrum : syntaxChrom;
			instrument.put("syntax", syntax);
			
			// process keys
			ArrayList<Integer> keys = new ArrayList<Integer>();
			for (int key = 0; key < sf2keys.length; key++) {
				
				// key not available?
				if (null == sf2keys[key])
					continue;
				
				// remember the key
				keys.add(key);
			}
			instrument.put("keys", makeNumberRangeString(keys));
			
			// Get the type: chromatic, single channel drum kit
			// or multi channel drum kit.
			if (hasDrumChannel && hasChromaticChannel) {
				needCategoryDrumkitMulti = true;
				instrument.put("type", "drumkit_multi");
				continue;
			}
			else if (hasDrumChannel && ! hasChromaticChannel) {
				needCategoryDrumkitSingle = true;
				instrument.put("type", "drumkit_single");
				continue;
			}
			else if (hasChromaticChannel && ! hasDrumChannel) {
				needCategoryChromatic = true;
				instrument.put("type", "chromatic");
				continue;
			}
			
			// no channels at all
			needCategoryUnknown = true;
			instrument.put("type", "-");
		}
		
		// add categories
		if (needCategoryChromatic) {
			HashMap<String, String> category = new HashMap<String, String>();
			category.put( "category", "category" );
			category.put( "type",     "category_chromatic" );
			category.put( "name",     Dict.get(Dict.SF_INSTR_CAT_CHROMATIC) );
			soundfontInstruments.add(category);
		}
		if (needCategoryDrumkitSingle) {
			HashMap<String, String> category = new HashMap<String, String>();
			category.put( "category", "category" );
			category.put( "type",     "category_drumkit_single" );
			category.put( "name",     Dict.get(Dict.SF_INSTR_CAT_DRUMKIT_SINGLE) );
			soundfontInstruments.add(category);
		}
		if (needCategoryDrumkitMulti) {
			HashMap<String, String> category = new HashMap<String, String>();
			category.put( "category", "category" );
			category.put( "type",     "category_drumkit_multi" );
			category.put( "name",     Dict.get(Dict.SF_INSTR_CAT_DRUMKIT_MULTI) );
			soundfontInstruments.add(category);
		}
		if (needCategoryUnknown) {
			HashMap<String, String> category = new HashMap<String, String>();
			category.put( "category", "category" );
			category.put( "type",     "category_unknown" );
			category.put( "name",     Dict.get(Dict.SF_INSTR_CAT_UNKNOWN) );
			soundfontInstruments.add(category);
		}
		
		// sort instruments
		// drumkits first, then chromatic instruments, then unknown types
		// inside a type category: order by program number, then by bank number
		Comparator<HashMap<String, String>> instrumentComparator = new Comparator<HashMap<String, String>>() {
			
			/** Sorting priority for the type. */
			private HashMap<String, Integer> typePriority = new HashMap<String, Integer>();
			
			// initialize type sorting priorities
			{
				typePriority.put( "category_drumkit_single", 8 );
				typePriority.put( "drumkit_single",          7 );
				typePriority.put( "category_drumkit_multi",  6 );
				typePriority.put( "drumkit_multi",           5 );
				typePriority.put( "category_chromatic",      4 );
				typePriority.put( "chromatic",               3 );
				typePriority.put( "category_unknown",        2 );
				typePriority.put( "-",                       1 );
			}
			
			@Override
			public int compare(HashMap<String, String> instrA, HashMap<String, String> instrB) {
				
				// first priority: type
				int priorityA = typePriority.get(instrA.get("type"));
				int priorityB = typePriority.get(instrB.get("type"));
				if (priorityA > priorityB)
					return -1;
				else if (priorityA < priorityB)
					return 1;
				
				// second priority: program number
				int programA = Integer.parseInt(instrA.get("program"));
				int programB = Integer.parseInt(instrB.get("program"));
				if (programA < programB)
					return -1;
				else if (programA > programB)
					return 1;
				
				// third priority: bank number
				int bankA = Integer.parseInt(instrA.get("bank"));
				int bankB = Integer.parseInt(instrB.get("bank"));
				if (bankA < bankB)
					return -1;
				else if (bankA > bankB)
					return 1;
				
				// default compare result
				return 0;
			}
		};
		Collections.sort(soundfontInstruments, instrumentComparator);
		
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
		if (null == soundfont)
			return;
		
		// collect resources
		SoundbankResource[] resources = soundfont.getResources();
		for (int i=0; i < resources.length; i++) {
			
			HashMap<String, Object> resource = new HashMap<String, Object>();
			soundfontResources.add(resource);
			
			// apply general information and defaults
			resource.put( "index",        i );
			resource.put( "name",         resources[i].getName() );
			resource.put( "class",        "-" );
			resource.put( "class_detail", "-" );
			resource.put( "type",         "-" );
			resource.put( "format",       "-" );
			resource.put( "frame_length", 0   );
			String  classDesc      = resources[i].toString();
			Pattern pattern        = Pattern.compile("^(.+?):.*");
			Matcher matcher        = pattern.matcher(classDesc);
			String  identifiedType = null;
			if (matcher.matches()) {
				String type = matcher.group(1);
				if ("Layer".equals(type)) {
					resource.put("type", "Layer");
					needCategoryLayer = true;
					identifiedType    = type;
				}
			}
			
			// apply null-class information
			Class<?> dataClass = resources[ i ].getDataClass();
			if (null == dataClass) {
				resource.put( "class",        "null" );
				resource.put( "class_detail", "null" );
			}
			
			// apply class name information
			else {
				String fullClassName = dataClass.getCanonicalName();
				Pattern classPattern = Pattern.compile(".+\\.([^.]+)$");
				Matcher classMatcher = classPattern.matcher(fullClassName);
				if (classMatcher.matches()) {
					resource.put("class", classMatcher.group(1));
				}
				else {
					resource.put("class", fullClassName);
				}
				resource.put("class_detail", fullClassName);
			}
			
			// apply class-dependant information
			if (dataClass == AudioInputStream.class) {
				
				resource.put("type", "Sample");
				needCategorySample = true;
				identifiedType     = "Sample";
				
				// get stream and format information
				AudioInputStream stream       = (AudioInputStream) resources[i].getData();
				long             frameCount   = stream.getFrameLength();
				AudioFormat      format       = stream.getFormat();
				String           formatDetail = format.toString();
				String           encoding     = format.getEncoding().toString().toLowerCase();
				float            frameRate    = format.getFrameRate();
				int              frameSize    = format.getFrameSize();
				int              bitRate      = format.getSampleSizeInBits();
				String           frameKHz     = String.format("%.1f", frameRate / 1000);
				double           seconds      = (frameCount + 0.0) / frameRate;
				int              channels     = format.getChannels();
				long             bytes        = frameCount * frameSize * channels;
				
				// construct a frame length details field (for the frames tooltip text)
				String lengthDetail = frameCount + " " + Dict.get(Dict.FRAMES) + ", "
				                    + String.format("%.2f", seconds) + " "
				                    + Dict.get(Dict.SEC) + ", "
				                    + bytes + " " + Dict.get(Dict.BYTES);
				
				// construct a format summary field (for the format tooltip text)
				String monoStereo = 2 == channels ? "s" : "m"; // m=mono, s=stereo
				String formatStr  = encoding   + " "  + frameKHz  + " kHz, " + bitRate + " bit "
				                  + monoStereo + ", " + frameSize + " B/frm";
				
				// store everything into the data structure
				resource.put( "frame_length",  frameCount   ); // content of the frames column
				resource.put( "length_detail", lengthDetail ); // tooltip for the frames column
				resource.put( "format",        formatStr    ); // content of the f column
				resource.put( "format_detail", formatDetail ); // tooltip for the format column
				resource.put( "seconds",       seconds      );
				resource.put( "bytes",         bytes        );
				
				// close the stream to avoid an exception caused by "too many open files"
				try {
	                stream.close();
                }
                catch (IOException e) {
                }
			}
			
			// We failed to guess the type?
			if (null == identifiedType)
				needCategoryUnknown = true;
		}
		
		// add categories
		if (needCategorySample) {
			HashMap<String, Object> category = new HashMap<String, Object>();
			category.put( "category", "category" );
			category.put( "type",     "category_sample" );
			category.put( "name",     Dict.get(Dict.SF_RESOURCE_CAT_SAMPLE) );
			soundfontResources.add(category);
		}
		if (needCategoryLayer) {
			HashMap<String, Object> category = new HashMap<String, Object>();
			category.put( "category", "category" );
			category.put( "type",     "category_layer" );
			category.put( "name",     Dict.get(Dict.SF_RESOURCE_CAT_LAYER) );
			soundfontResources.add(category);
		}
		if (needCategoryUnknown) {
			HashMap<String, Object> category = new HashMap<String, Object>();
			category.put( "category", "category" );
			category.put( "type",     "category_unknown" );
			category.put( "name",     Dict.get(Dict.SF_RESOURCE_CAT_UNKNOWN) );
			soundfontResources.add(category);
		}
		
		// sort resources
		// samples first, then layers, then unknown types
		// inside a type category: keep the order of the soundfont (order by index)
		Comparator<HashMap<String, Object>> instrumentComparator = new Comparator<HashMap<String, Object>>() {
			
			/** Sorting priority for the type. */
			private HashMap<String, Integer> typePriority = new HashMap<String, Integer>();
			
			// initialize type sorting priorities
			{
				typePriority.put( "category_sample",  6 );
				typePriority.put( "Sample",           5 );
				typePriority.put( "category_layer",   4 );
				typePriority.put( "Layer",            3 );
				typePriority.put( "category_unknown", 2 );
				typePriority.put( "-",                1 );
			}
			
			@Override
			public int compare(HashMap<String, Object> resourceA, HashMap<String, Object> resourceB) {
				
				// first priority: type
				int priorityA = typePriority.get(resourceA.get("type"));
				int priorityB = typePriority.get(resourceB.get("type"));
				if (priorityA > priorityB)
					return -1;
				else if (priorityA < priorityB)
					return 1;
				
				// second priority: index
				int programA = (Integer) resourceA.get("index");
				int programB = (Integer) resourceB.get("index");
				if (programA < programB)
					return -1;
				else if (programA > programB)
					return 1;
				
				// default compare result
				return 0;
			}
		};
		Collections.sort(soundfontResources, instrumentComparator);
		
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
	private static String makeNumberRangeString(ArrayList<Integer> list) {
		
		String result = "";
		
		// init
		int lastWritten   = -1; // last number we wrote into the result string
		int lastSeen      = -1; // last number we saw in the list
		
		// walk through the list
		for (int i : list) {
			
			// very first number?
			if (lastWritten < 0) {
				result = Integer.toString(i);
				lastWritten = i;
				lastSeen    = i;
				
				continue;
			}
			
			// normal incrementation?
			if (i == lastSeen + 1) {
				
				// more numbers can follow. Do not yet write anything.
				lastSeen = i;
				
				continue;
			}
			
			// There was a gap.
			
			// Does the last range have only one number?
			if (lastSeen == lastWritten) {
				result      = result + ", " + Integer.toString(i);
				lastWritten = i;
				lastSeen    = i;
				
				continue;
			}
			
			// The last range had more than one number.
			result      = result + "-" + Integer.toString(lastSeen) + ", " + Integer.toString(i);
			lastWritten = i;
			lastSeen    = i;
		}
		
		// list was empty?
		if (lastWritten < 0)
			return result;
		
		// close the list
		
		// Does the last range have only one number?
		if (lastSeen == lastWritten)
			return result;
		
		// The last range had more than one number.
		result = result + "-" + Integer.toString(lastSeen);
		
		return result;
	}
}
