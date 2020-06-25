/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.file;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import org.midica.config.Dict;
import org.midica.config.Laf;
import org.midica.ui.model.IMessageType;
import org.midica.ui.model.SingleMessage;

/**
 * This class is used to collect export warnings for a later display.
 * A MIDI sequence can not always be transformed into source code properly.
 * Warnings regarding such problems are collected and sorted here in order to be displayed later.
 * 
 * @author Jan Trukenmüller
 */
public class ExportResult {
	
	private TreeMap<Long, ArrayList<HashMap<String, Object>>> warningMap;
	private int                     numberOfWarnings;
	private boolean                 success     = true;
	private HashMap<String, Object> lastWarning = null;
	
	/**
	 * Creator of an empty warning object.
	 * Initializes a new empty data structure for warnings.
	 * 
	 * @param success  false, if the export failed. Otherwise: true.
	 */
	public ExportResult(boolean success) {
		this.success     = success;
		warningMap       = new TreeMap<Long, ArrayList<HashMap<String, Object>>>();
		numberOfWarnings = 0;
	}
	
	/**
	 * Adds a new warning message.
	 * 
	 * @param track    Track number of the event that caused the warning, or **null** if unknown.
	 * @param tick     Tickstamp of the event that caused the warning.
	 * @param channel  Channel where the warning occured -- or **null** if it wasn't a channel based event.
	 * @param msg      Warning message
	 */
	public void addWarning(Integer track, long tick, Byte channel, String msg) {
		
		numberOfWarnings++;
		
		// create new warning list for the given tick, if this is the first warning at that tick
		ArrayList<HashMap<String, Object>> warningsAtTick;
		if (warningMap.containsKey(tick)) {
			warningsAtTick = warningMap.get(tick);
		}
		else {
			warningsAtTick = new ArrayList<HashMap<String, Object>>();
			warningMap.put(tick, warningsAtTick);
		}
		
		// create new warning
		HashMap<String, Object> warning = new HashMap<String, Object>();
		warning.put( "track",   track   );
		warning.put( "tick",    tick    );
		warning.put( "channel", channel );
		warning.put( "msg",     msg     );
		
		// add the new warning
		warningsAtTick.add(warning);
		
		// remember the last warning, in case it's based on a MIDI message
		lastWarning = warning;
		
		return;
	}
	
	/**
	 * Adds MIDI message details to the last warning.
	 * 
	 * @param msg  the message details
	 */
	public void setDetailsOfLastWarning(SingleMessage msg) {
		if (null == msg || null == lastWarning) {
			return;
		}
		
		String summary = (String) msg.getOption(IMessageType.OPT_SUMMARY);
		String details = "<html><b>" + Dict.get(Dict.INFO_COL_MSG_TYPE) + ":</b> " + msg.getType();
		if (summary != null) {
			details += "<span style=\"color: #" + Laf.COLOR_MSG_ARROW_HTML + "; font-weight: bold; \"> / </span>"
			        +  "<b>" + Dict.get(Dict.INFO_COL_MSG_SUMMARY) + ":</b> " + summary;
		}
		setDetailsOfLastWarning(details);
	}
	
	/**
	 * Adds a custom details string to the last warning.
	 * 
	 * @param details  the custom details to be added.
	 */
	public void setDetailsOfLastWarning(String details) {
		if (lastWarning != null) {
			lastWarning.put("details", details);
		}
	}
	
	/**
	 * Returns a data structure containing all warnings, sorted by tick.
	 * Each tick where at least one warning occured has an entry.
	 * The {@link ArrayList} for each tick contains a {@link HashMap} for each warning.
	 * Each {@link HashMap} contains the details of the warning.
	 * 
	 * @return data structure containing all warnings.
	 */
	public ArrayList<HashMap<String, Object>> getWarnings() {
		
		// transform the warnings into a flat data structure
		ArrayList<HashMap<String, Object>> warnings = new ArrayList<HashMap<String,Object>>();
		for (long tick : warningMap.keySet()) {
			for (int i = 0; i < warningMap.get(tick).size(); i++) {
				warnings.add(warningMap.get(tick).get(i));
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
