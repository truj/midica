/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file;

import org.midica.config.Dict;

/**
 * This class is used for exceptions that can occur during a file export process.
 * 
 * @author Jan Trukenmüller
 */
public class ExportException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	/** false: simple string messge; true: complex message containing string, tick, channel, note */
	private boolean composed = false;
	private Long    tick     = -1L;
	private int     channel  = -1;
	private int     note     = -1;
	private String  msg      = null;
	
	/**
	 * Creator of a simple export exception, containing only a simple string.
	 * This is used for IO errors regarding the export file or MIDI exports.
	 * 
	 * @param msg Error message
	 */
	public ExportException( String msg ) {
		super( msg );
	}
	
	/**
	 * Creator of a composed error message containing details like tickstamp, channel, note number etc.
	 * This is used in the {@link MidicaPLExporter} class when an event has occured that cannot be
	 * handled properly.
	 * 
	 * @param tick     Tickstamp of the event that caused the exception.
	 * @param channel  Channel where the event occured -- or -1 if it wasn't a channel based event.
	 * @param note     Note number of the event -- or -1 if no note was involved.
	 * @param msg      Error message
	 */
	public ExportException( Long tick, int channel, int note, String msg ) {
		this.composed = true;
		this.tick     = tick;
		this.channel  = channel;
		this.note     = note;
		this.msg      = msg;
	}
	
	/**
	 * Returns the final error message.
	 * In case of a simple message the error message provided to the constructor is left
	 * unchanged.
	 * In the case of a composed message it contains more details.
	 * 
	 * @return Error message
	 */
	public String getErrorMessage() {
		
		// simple exception?
		if ( ! composed )
			return this.msg;
		
		// more complicated exception
		StringBuffer composedMsg = new StringBuffer( "<html>" + msg + "<br>" );
		
		// channel or global event?
		String channelStr = -1 == channel ? "global event"     : Integer.toString( channel );
		String noteStr    = -1 == note    ? "Not a note event" : Integer.toString( note );
		
		// add details
		composedMsg.append( "<table>" );
		composedMsg.append( "<tr><td>" + Dict.get(Dict.ERROR_TICK)    + "</td><td>" + tick       + "</td></tr>" );
		composedMsg.append( "<tr><td>" + Dict.get(Dict.ERROR_CHANNEL) + "</td><td>" + channelStr + "</td></tr>" );
		composedMsg.append( "<tr><td>" + Dict.get(Dict.ERROR_NOTE)    + "</td><td>" + noteStr    + "</td></tr>" );
		composedMsg.append( "</table>" );
		
		return composedMsg.toString();
	}
}
