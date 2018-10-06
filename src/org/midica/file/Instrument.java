/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file;

import java.util.ArrayList;

import org.midica.config.Dict;

/**
 * Objects of this class represent a specific MIDI channel.
 * They adjust their state according to the commands of the {@link MidicaPLParser}
 * while parsing source files - or according to the commands of the
 * {@link MidicaPLExporter} while exporting source files.
 * 
 * The following elements of a channel's state need further explication:
 * 
 * - **tickstamp**       - This is the current position of the parsed/exported file, counted in MIDI ticks.
 * - **velocity**        - Velocity of key strokes.
 * - **staccato value**  - That is the number of ticks that a key is released before
 *                         the theoretical end of the according note.
 * 
 * @author Jan Trukenmüller
 */
public class Instrument implements Comparable<Instrument> {
	
	/** First tickstamp where any action can occur. */
	private static final int MIN_CURRENT_TICKS = 0;
	/** Default velocity for this channel unless defined differently. */
	private static final int DEFAULT_VELOCITY  = 64;
	/** Default staccato value for this channel unless defined differently. */
	private static final int DEFAULT_STACCATO  = 10;
	
	/** Channel number as defined by the MIDI specification. Between 0 and 15. */
	public final int channel;
	/** Instrument number as defined by the MIDI specification. Between 0 and 127. */
	public int instrumentNumber;
	/** Instrument name like it is read from the MidicaPL source file. */
	public String instrumentName;
	/** Indicates if this channel has been automatically (true) or explicitly (false) defined. */
	public boolean autoChannel;
	/** MSB of the bank number (0-127) */
	private int bankMSB;
	/** LSB of the bank number (0-127) */
	private int bankLSB;
	
	/** Current velocity */
	private int velocity = DEFAULT_VELOCITY;
	/** Current tickstamp */
	private long currentTicks = MIN_CURRENT_TICKS;
	/** Each note ends that many ticks before it would end theoretically */
	private int staccato = DEFAULT_STACCATO;
	
	/**
	 * Creates a new Instrument object representing a channel during the parsing or export
	 * procedure of a MidicaPL source file.
	 * While parsing this can be done explicitly inside the first INSTRUMENTS block or (if
	 * not defined in this block) when the first INSTRUMENTS block is left.
	 * 
	 * @param channel           Channel number as defined by the MIDI specification (0-15)
	 * @param instrumentNumber  Instrument number as defined by the MIDI specification (0-127)
	 * @param instrumentName    Instrument name as defined in the INSTRUMENTS command or
	 *                          (if not defined explicitly) the default channel comment (in the
	 *                          parsing procedure) or the pre-configured instrument name (in the
	 *                          export procedure)
	 * @param automatic         **false**, as soon as the instrument has been defined explicitly
	 *                          inside an INSTRUMENTS block; otherwise (or until the first
	 *                          definition inside an INSTRUMENTS block): **true**
	 */
	public Instrument( int channel, int instrumentNumber, String instrumentName, boolean automatic ) {
		this.channel          = channel;
		this.bankMSB          = 0;
		this.bankLSB          = 0;
		this.instrumentNumber = instrumentNumber;
		this.instrumentName   = instrumentName;
		if (automatic)
			autoChannel = true;
		else
			autoChannel = false;
		
		// export?
		if ( null == instrumentName ) {
			if ( 9 == channel ) {
				this.instrumentName = Dict.getDrumkit( instrumentNumber );
			}
			else {
				this.instrumentName = Dict.getInstrument( instrumentNumber );
			}
		}
	}
	
	@Override
	public int compareTo( Instrument other ) {
		return channel - other.channel;
	}
	
	/**
	 * Returns the current velocity of the channel.
	 * 
	 * @return current velocity
	 */
	public int getVelocity() {
		return velocity;
	}
	
	/**
	 * Sets the velocity of the channel.
	 * 
	 * @param velocity  New velocity of the channel as defined in the MIDI specification.
	 *                  (0-127)
	 */
	public void setVelocity( int velocity ) {
		this.velocity = velocity;
	}
	
	/**
	 * Returns the current tickstamp. This is the amount of MIDI ticks for which this channel
	 * has already been parsed so far.
	 * 
	 * @return Current tickstamp
	 */
	public long getCurrentTicks() {
		return currentTicks;
	}
	
	/**
	 * Sets the current tickstamp. This is the amount of MIDI ticks for which this channel
	 * has already been parsed so far.
	 * 
	 * @param currentTicks  New tickstamp to be set.
	 */
	public void setCurrentTicks( long currentTicks ) {
		this.currentTicks = currentTicks;
	}
	
	/**
	 * Resets the current tickstamp.
	 * This is done during the initial instruments setup.
	 */
	public void resetCurrentTicks() {
		this.currentTicks = MIN_CURRENT_TICKS;
	}
	
	/**
	 * Returns the current staccato value of the channel.
	 * That is the number of ticks that the key is released before the theoretical end of
	 * the note.
	 * 
	 * @return Current staccato value
	 */
	public int getStaccato() {
		return staccato;
	}
	
	/**
	 * Sets a new staccato value.
	 * 
	 * @param staccato New staccato value.
	 */
	public void setStaccato(int staccato) {
		this.staccato = staccato;
	}
	
	/**
	 * Increments the tickstamp according to the note's duration.
	 * 
	 * @param duration Duration of the note in ticks.
	 * @return Tickstamp for the note end according to duration and staccato (key release tick).
	 */
	public long addNote( int duration ) {
		incrementTicks( duration );
		return currentTicks - staccato;
	}
	
	/**
	 * Increments the tickstamp according to the duration.
	 * 
	 * @param duration duration in ticks
	 */
	public void incrementTicks( int duration ) {
		currentTicks += duration;
	}
	
	@Override
	public String toString() {
		String inner =  ", channel: "          + channel
		             +  ", instrumentNumber: " + instrumentNumber
		             +  ", instrumentName: "   + instrumentName
		             +  ", velocity: "         + velocity
		             +  ", currentTicks: "     + currentTicks
		             +  ", staccato: "         + staccato
		             ;
		return "\n{" + inner + "}";
	}
	
	/**
	 * Returns the maximum tickstamp of all the channels.
	 * 
	 * @param instruments  List of all {@link Instrument} objects.
	 * @return             Maximum tickstamp
	 */
	public static long getMaxCurrentTicks( ArrayList<Instrument> instruments ) {
		long maxTicks = 0;
		for ( int i = 0; i < instruments.size(); i++ ) {
			long ticksOf_i = instruments.get( i ).getCurrentTicks();
			maxTicks = maxTicks > ticksOf_i
					 ? maxTicks
					 : ticksOf_i
					 ;
		}
		return maxTicks;
	}
	
	/**
	 * Sets the bank.
	 * 
	 * Returns the following two values.
	 * 
	 * - **true**, if the new MSB is different from the old MSB. **false**, if the MSB is unchanged.
	 * - **true**, if the new LSB is different from the old LSB. **false**, if the LSB is unchanged.
	 * 
	 * @param msb  bank MSB
	 * @param lsb  bank LSB
	 * @return  What has been changed (see description above).
	 */
	public boolean[] setBank( int msb, int lsb ) {
		boolean[] result = { false, false };
		
		// MSB changed?
		if ( this.bankMSB != msb )
			result[ 0 ] = true;
		
		// LSB changed?
		if ( this.bankLSB != lsb )
			result[ 1 ] = true;
		
		// set the values
		this.bankMSB = msb;
		this.bankLSB = lsb;
		
		return result;
	}
	
	/**
	 * Returns the currently configured bank MSB.
	 * 
	 * @return bank MSB
	 */
	public int getBankMSB() {
		return bankMSB;
	}
	
	/**
	 * Returns the currently configured bank LSB.
	 * 
	 * @return bank LSB
	 */
	public int getBankLSB() {
		return bankLSB;
	}
}
