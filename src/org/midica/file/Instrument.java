/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file;

import java.util.ArrayList;
import java.util.HashMap;

import org.midica.config.Dict;
import org.midica.file.read.MidicaPLParser;
import org.midica.file.read.ParseException;
import org.midica.file.write.MidicaPLExporter;

/**
 * Objects of this class represent a specific MIDI channel.
 * They adjust their state according to the commands of the {@link MidicaPLParser}
 * while parsing source files - or according to the commands of the
 * {@link MidicaPLExporter} while exporting source files.
 * 
 * The following elements of a channel's state need further explication:
 * 
 * - **tickstamp**      - This is the current position of the parsed/exported file, counted in MIDI ticks.
 * - **velocity**       - Velocity of key strokes.
 * - **noteLength**     - Note length string, only used for compact syntax and for ALDA
 * - **naturalLength**  - Note length without any block tuplets (only used for compact syntax)
 * - **durationRatio**  - Ratio between key press duration and note length duration.  
 *                        Low values result in staccato, high values in legato.  
 *                        Minimum: >0.0 (>0%)  
 *                        Maximum: infinite (max possible float value)
 * 
 * @author Jan Trukenmüller
 */
public class Instrument implements Comparable<Instrument> {
	
	/** First tickstamp where any action can occur. */
	private static final int MIN_CURRENT_TICKS = 0;
	/** Default velocity for this channel unless defined differently. */
	private static final int DEFAULT_VELOCITY  = 64;
	/** Default duration ratio for this channel unless defined differently. */
	private static final float DEFAULT_DURATION_RATIO = 0.95f; // 95 percent
	/** Minimum remaining note length after applying the duration ratio */
	private static final long MIN_NOTE_LENGTH = 1;
	/** Default tolerance for bar line checks in ticks. */
	private static final int DEFAULT_BAR_LINE_TOLERANCE = 0;
	
	// constants for foreign languages (e.g. alda)
	private static final byte   DEFAULT_OCTAVE      = 4;
	private static final String DEFAULT_NOTE_LENGTH = "4";
	
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
	/** key = note; value = tickstamp of the last NOTE-ON */
	private HashMap<Integer, Long> lastNoteOn = new HashMap<>();
	/** key = note; value = tickstamp of the last NOTE-OFF */
	private HashMap<Integer, Long> lastNoteOff = new HashMap<>();
	/**
	 * If legato overlappings are detected while adding a note, this is the stop tick
	 * of the detected note to be corrected.
	 * If no legato overlappings are detected, this is **null**.
	 */
	private Long stopTickForLegatoCorrection = null;
	
	/** Current velocity */
	private int velocity = DEFAULT_VELOCITY;
	/** Current tickstamp */
	private long currentTicks = MIN_CURRENT_TICKS;
	/** Ratio between key press duration and note length duration */
	private float durationRatio = DEFAULT_DURATION_RATIO;
	/** current note length for compact syntax or ALDA */
	private String noteLength = DEFAULT_NOTE_LENGTH;
	/** current note length for compact syntax WITHOUT block-tuplets */
	private String naturalLength = noteLength;
	/** current bar line tolerance */
	private int barLineTolerance = DEFAULT_BAR_LINE_TOLERANCE;
	/** one-time option for compact syntax: quantity */
	private Integer otoQuantity = null;
	/** one-time option for compact syntax: tremolo */
	private String  otoTremolo  = null;
	/** one-time option for compact syntax: multiple */
	private boolean otoMultiple = false;
	
	// fields for foreign languages (e.g. alda)
	private byte octave = DEFAULT_OCTAVE;
	
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
	 * Resets the current tickstamp and the data structures storing the last pressed notes.
	 * 
	 * This is done during the initial instruments setup.
	 */
	public void reset() {
		currentTicks = MIN_CURRENT_TICKS;
		lastNoteOn   = new HashMap<>();
		lastNoteOff  = new HashMap<>();
	}
	
	/**
	 * Returns the current duration ratio of the channel.
	 * This is the ratio between key press duration and note length duration.
	 * 0.1 means 10% (strong staccato)  
	 * 0.5 means 50% (staccato)  
	 * 1.0 means 100% (legato)  
	 * more than 1.0 is overlapping legato
	 * 
	 * @return Current duration ratio
	 */
	public float getDurationRatio() {
		return durationRatio;
	}
	
	/**
	 * Sets a new key-press/note-length duration ratio.
	 * 
	 * @param durationRatio  New duration ratio.
	 */
	public void setDurationRatio(float durationRatio) {
		this.durationRatio = durationRatio;
	}
	
	/**
	 * Increments the tickstamp according to the note's duration in ticks.
	 * 
	 * @param note        Note number.
	 * @param duration    Note length duration in ticks.
	 * @return Tickstamp for the key release according to note length duration and key press duration ratio.
	 */
	public long addNote(int note, int duration) {
		
		// get last ticks of that note - maybe we need corrections because of legato overlappings
		Long lastOnTick             = lastNoteOn.get(note);
		Long lastOffTick            = lastNoteOff.get(note);
		long noteOnTick             = currentTicks;
		stopTickForLegatoCorrection = null;
		
		// calculate key release tickstamp
		long pressTicks = Math.round(duration * this.durationRatio);
		if (pressTicks < MIN_NOTE_LENGTH) {
			pressTicks = MIN_NOTE_LENGTH;
		}
		long endTick = currentTicks + pressTicks;
		
		// increment channel tickstamp
		incrementTicks(duration);
		
		// correction needed?
		if (lastOnTick != null && lastOffTick != null) {
			if (lastOnTick < noteOnTick && lastOffTick >= noteOnTick) {
				stopTickForLegatoCorrection = lastOffTick;
			}
		}
		
		// store note for the next legato check
		lastNoteOn.put(note, noteOnTick);
		lastNoteOff.put(note, endTick);
		
		return endTick;
	}
	
	/**
	 * Returns the stop tick of the note to be corrected due to legato overlappings, or **null** if
	 * no overlappings have been detected.
	 * 
	 * @return the stop tick to be corrected or **null**.
	 */
	public Long getStopTickToCorrect() {
		return stopTickForLegatoCorrection;
	}
	
	/**
	 * Increments the tickstamp according to the note length duration.
	 * 
	 * @param duration duration in ticks
	 */
	public void incrementTicks(int duration) {
		currentTicks += duration;
	}
	
	@Override
	public String toString() {
		String inner =  ", channel: "          + channel
		             +  ", instrumentNumber: " + instrumentNumber
		             +  ", instrumentName: "   + instrumentName
		             +  ", velocity: "         + velocity
		             +  ", currentTicks: "     + currentTicks
		             +  ", durationRatio: "    + durationRatio
		             +  ", noteLength: "       + noteLength
		             +  ", naturalLength: "    + naturalLength
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
	
	/**
	 * Returns the currently configured octave.
	 * 
	 * @return octave
	 */
	public byte getOctave() {
		return octave;
	}
	
	/**
	 * Sets the octave of the channel.
	 * 
	 * @param octave  New octave of the channel.
	 */
	public void setOctave(byte octave) {
		this.octave = octave;
	}
	
	/**
	 * Returns the currently configured note length.
	 * 
	 * @return note length
	 */
	public String getNoteLength() {
		return noteLength;
	}
	
	/**
	 * Sets the note length of the channel.
	 * 
	 * @param noteLength  New note length of the channel.
	 */
	public void setNoteLength(String noteLength) {
		this.noteLength = noteLength;
	}
	
	/**
	 * Returns the currently configured natural length.
	 * 
	 * @return note length without block tuplets
	 */
	public String getNaturalLength() {
		return naturalLength;
	}
	
	/**
	 * Sets the natural length of the channel (note length without block tuplets).
	 * 
	 * @param naturalLength    note length without block tuplets
	 */
	public void setNaturalLength(String naturalLength) {
		this.naturalLength = naturalLength;
	}
	
	/**
	 * Returns the currently configured bar line tolerance.
	 * 
	 * @return bar line tolerance
	 */
	public int getBarLineTolerance() {
		return barLineTolerance;
	}
	
	/**
	 * Sets the bar line tolerance.
	 * 
	 * @param barLineTolerance  bar line tolerance to be set.
	 */
	public void setBarLineTolerance(int barLineTolerance) {
		this.barLineTolerance = barLineTolerance;
	}
	
	/**
	 * Returns the one-time quantity for compact syntax.
	 * The default is **1**.
	 * 
	 * @return one-time quantity
	 */
	public int getOtoQuantity() {
		if (null == otoQuantity)
			return 1;
		return otoQuantity;
	}
	
	/**
	 * Sets the one-time quantity for compact syntax.
	 * 
	 * @param quantity  number to set
	 * @throws ParseException if the option is already set
	 */
	public void setOtoQuantity(int quantity) throws ParseException {
		if (otoQuantity != null)
			throw new ParseException(Dict.get(Dict.ERROR_OTO_DUPLICATE_QUANTITY));
		otoQuantity = quantity;
	}
	
	/**
	 * Returns the one-time tremolo value for compact syntax, if set.
	 * Otherwise, returns null.
	 */
	public String getOtoTremolo() {
		return otoTremolo;
	}
	
	/**
	 * Sets or resets the one-time tremolo value for compact syntax.
	 * 
	 * @param tremolo  tremolo value to set, or null to reset
	 * @throws ParseException if the option is already set
	 */
	public void setOtoTremolo(String tremolo) throws ParseException {
		if (otoTremolo != null)
			throw new ParseException(Dict.get(Dict.ERROR_OTO_DUPLICATE_TREMOLO));
		otoTremolo = tremolo;
	}
	
	/**
	 * Determins if a one-time multiple option is set for compact syntax.
	 * 
	 * @return true or false
	 */
	public boolean isOtoMultiple() {
		return otoMultiple;
	}
	
	/**
	 * Sets or resets the one-time multiple option for compact syntax.
	 * 
	 * @param multiple  true to set the multiple option, false to reset it.
	 * @throws ParseException if the option is already set
	 */
	public void setOtoMultiple(boolean multiple) throws ParseException {
		if (otoMultiple)
			throw new ParseException(Dict.get(Dict.ERROR_OTO_DUPLICATE_MULTIPLE));
		otoMultiple = multiple;
	}
	
	/**
	 * Resets all one-time option for compact syntax.
	 */
	public void resetOto() {
		otoQuantity = null;
		otoTremolo  = null;
		otoMultiple = false;
	}
}
