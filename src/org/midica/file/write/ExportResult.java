/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file.write;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * This class is used to collect export warnings for a later display.
 * A MIDI stream can not always be transformed into a MidicaPL source file properly.
 * Warnings regarding such problems are collected and sorted here in order to be displayed later.
 * 
 * @author Jan Trukenmüller
 */
public class ExportResult {
	
	private TreeMap<Long, ArrayList<HashMap<String, String>>> warningMap;
	private int     numberOfWarnings;
	private boolean success = true;
	
	/**
	 * Creator of an empty warning object.
	 * Initializes a new empty data structure for warnings.
	 * 
	 * @param success  false, if the export failed. Otherwise: true.
	 */
	public ExportResult( boolean success ) {
		this.success     = success;
		warningMap       = new TreeMap<Long, ArrayList<HashMap<String, String>>>();
		numberOfWarnings = 0;
	}
	
	/**
	 * Adds a new warning message.
	 * 
	 * @param track    Track number of the event that caused the warning.
	 * @param tick     Tickstamp of the event that caused the warning.
	 * @param channel  Channel where the warning occured -- or -1 if it wasn't a channel based event.
	 * @param note     Note number of the event -- or -1 if no note was involved.
	 * @param msg      Warning message
	 */
	public void addWarning( int track, long tick, int channel, int note, String msg ) {
		
		numberOfWarnings++;
		
		// create new warning list for the given tick, if this is the first warning at that tick
		ArrayList<HashMap<String, String>> warningsAtTick;
		if ( warningMap.containsKey(tick) ) {
			warningsAtTick = warningMap.get( tick );
		}
		else {
			warningsAtTick = new ArrayList<HashMap<String, String>>();
			warningMap.put( tick, warningsAtTick );
		}
		
		// create new warning
		HashMap<String, String> warning = new HashMap<String, String>();
		warning.put( "track",   track   + "" );
		warning.put( "tick",    tick    + "" );
		warning.put( "channel", channel + "" );
		warning.put( "note",    note    + "" );
		warning.put( "msg",     msg          );
		
		// add the new warning
		warningsAtTick.add( warning );
		
		return;
	}
	
	/**
	 * Returns a data structure containing all warnings, sorted by tick.
	 * Each tick where at least one warning occured has an entry.
	 * The {@link ArrayList} for each tick contains a {@link HashMap} for each warning.
	 * Each {@link HashMap} contains the details of the warning.
	 * 
	 * @return data structure containing all warnings.
	 */
	public ArrayList<HashMap<String, String>> getWarnings() {
		
		// transform the warnings into a flat data structure
		ArrayList<HashMap<String, String>> warnings = new ArrayList<HashMap<String,String>>();
		for ( long tick : warningMap.keySet() ) {
			for ( int i = 0; i < warningMap.get(tick).size(); i++ ) {
				warnings.add( warningMap.get(tick).get(i) );
			}
		}
		
		return warnings;
	}
	
	/**
	 * Returns the number of warnings.
	 * 
	 * @return number of warnings.
	 */
	public int countWarnings() {
		return numberOfWarnings;
	}
	
	/**
	 * Returns true, if the export was successful, otherwise false.
	 * 
	 * @return true, if the export was successful, otherwise false.
	 */
	public boolean isSuccessful() {
		return success;
	}
	
	
}
