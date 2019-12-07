/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file.write;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Map.Entry;

/**
 * This class is used by the MidicaPlExporter to store a sequence slice.
 * 
 * A slice is a part of a sequence, beginning either in tick 0 or with a global command.
 * 
 * It ends either with the next global command or at the end of the sequence.
 * 
 * @author Jan Trukenmüller
 */
public class Slice {
	
	private long beginTick = 0L;
	private long endTick   = -1L;
	private TreeMap<String, String> globalCommands = new TreeMap<>();
	
	private TreeMap<Byte, TreeMap<Long, TreeMap<Byte, Byte>>>    noteHistory = null;
	private TreeMap<Byte, TreeMap<Byte, TreeMap<Long, Boolean>>> noteOnOff   = null;
	
	/**
	 * The timelines have the following structure:
	 * 
	 * channel -- tick -- type -- name -- property -- value
	 * 
	 * For instrument changes:
	 * 
	 * - channel
	 * - tick
	 * - type = ET_INSTR
	 * - name = null
	 * 
	 * For notes or chords:
	 * 
	 * - channel
	 * - tick
	 * - type:     ET_INSTR or ET_NOTES
	 * - name:     note or chord name
	 * - property: VELOCITY, OFF_TICK, LENGTH, ...
	 * - value
	 */
	private ArrayList<TreeMap<Long, TreeMap<Byte, TreeMap<String, TreeMap<Byte, String>>>>> timelines = null;
	
	/**
	 * Timeline for rests that are only there for syllables.
	 * 
	 * - tick
	 * - value: syllable
	 */
	private TreeMap<Long, String> syllableRestTimeline = null;
	
	/**
	 * Returns the slice belonging to the given tick.
	 * 
	 * @param slices  all slices
	 * @param tick    MIDI tick
	 * @return the matching slice.
	 */
	public static Slice getSliceByTick(ArrayList<Slice> slices, long tick) {
		
		for (Slice slice : slices) {
			if (tick >= slice.getBeginTick() && tick < slice.getEndTick())
				return slice;
		}
		
		// TODO: throw exception
		// fallback: last slice
		System.err.println("could not find the correct slice. returning a default slice");
		return slices.get(slices.size() - 1);
	}
	
	/**
	 * Creates a new sequence slice.
	 * 
	 * @param beginTick  MIDI tick where the slice begins.
	 */
	public Slice(long beginTick) {
		this.beginTick = beginTick;
		
		// initialize timelines
		timelines = new ArrayList<>();
		for (int i = 0; i < 16; i++) {
			TreeMap<Long, TreeMap<Byte,TreeMap<String, TreeMap<Byte, String>>>> timeline = new TreeMap<>();
			timelines.add(timeline);
		}
		syllableRestTimeline = new TreeMap<>();
	}
	
	/**
	 * Returns the begin tick of the slice.
	 * 
	 * @return the begin tick of the slice.
	 */
	public long getBeginTick() {
		return beginTick;
	}
	
	/**
	 * Sets the end tick of the slice.
	 * 
	 * @param endTick  the end tick of the slice.
	 */
	public void setEndTick(long endTick) {
		this.endTick = endTick;
	}
	
	/**
	 * Returns the end tick of the slice, or **-1**, if an end tick has not yet been set.
	 * 
	 * @return the end tick of the slice or **-1**.
	 */
	public long getEndTick() {
		return endTick;
	}
	
	/**
	 * Determins if the slice contains rests with syllables.
	 * 
	 * @return **true**, if the slice contains rests with syllables, otherwise: **false**.
	 */
	public boolean hasSyllableRests() {
		return ! syllableRestTimeline.isEmpty();
	}
	
	/**
	 * Adds a global command to be put at the begin tick of the slice.
	 * 
	 * @param key    ID of the global command.
	 * @param value  value of the global command.
	 */
	public void addGlobalCmd(String key, String value) {
		globalCommands.put(key, value);
	}
	
	/**
	 * Returns the global commands from the beginning of the slice.
	 * 
	 * @return the global commands.
	 */
	public TreeMap<String, String> getGlobalCommands() {
		return globalCommands;
	}
	
	/**
	 * Adds an instrument change to the given channel's timeline.
	 * 
	 * @param tick     MIDI tick
	 * @param channel  MIDI channel
	 */
	public void addInstrChange(long tick, int channel) {
		addToTimeline(tick, channel, MidicaPLExporter.ET_INSTR, null);
	}
	
	/**
	 * Adds the given notes and/or chords to the channel's timeline.
	 * 
	 * @param tick           MIDI tick
	 * @param channel        MIDI channel
	 * @param notesOrChords  all notes and chords that begin at this channel and tick
	 */
	public void addNotesToTimeline(long tick, byte channel, TreeMap<String, TreeMap<Byte, String>> notesOrChords) {
		addToTimeline(tick, channel, MidicaPLExporter.ET_NOTES, notesOrChords);
	}
	
	/**
	 * Adds a lyrics syllable to a special timeline as an option to a rest command.
	 * This is needed if there is no note/chord being played at the tick when the syllable appears.
	 * 
	 * @param tick      MIDI tick
	 * @param syllable  Lyrics syllable for Karaoke
	 */
	public void addSyllableRest(long tick, String syllable) {
		syllableRestTimeline.put(tick, syllable);
	}
	
	/**
	 * Returns the timeline for the given channel.
	 * 
	 * @param channel  MIDI channel
	 * @return the timeline
	 */
	public TreeMap<Long, TreeMap<Byte, TreeMap<String, TreeMap<Byte, String>>>> getTimeline(int channel) {
		return timelines.get(channel);
	}
	
	/**
	 * Returns the special timeline only for rests with syllables.
	 * @return
	 */
	public TreeMap<Long, String> getSyllableRestTimeline() {
		return syllableRestTimeline;
	}
	
	/**
	 * Copies the given structure to the slice, omitting all entries that are not related to the slice's tick scope.
	 * Returns the copied (filtered) structure.
	 * 
	 * An entry is copied, if:
	 * 
	 * - tick >= scope's beginTick; and:
	 * - tick <  scope's endTick
	 * 
	 * @param noteHistory  structure containing channel, tick, note and velocity
	 * @return the filtered structure.
	 */
	public TreeMap<Byte, TreeMap<Long, TreeMap<Byte, Byte>>> filterNotes(TreeMap<Byte, TreeMap<Long, TreeMap<Byte, Byte>>> noteHistory) {
		this.noteHistory = new TreeMap<>();
		
		// filter and copy noteHistory
		CHANNEL:
		for (Entry<Byte, TreeMap<Long, TreeMap<Byte, Byte>>> channelSet : noteHistory.entrySet()) {
			byte channel = channelSet.getKey();
			
			TreeMap<Long, TreeMap<Byte, Byte>> allChannelNotes  = channelSet.getValue();
			TreeMap<Long, TreeMap<Byte, Byte>> slideNoteHistory = new TreeMap<>();
			
			TICK:
			for (Entry<Long, TreeMap<Byte, Byte>> chHistorySet : allChannelNotes.entrySet()) {
				long tick = chHistorySet.getKey();
				
				// decide if the notes belong to the slice
				if (tick >= endTick) {
					this.noteHistory.put(channel, slideNoteHistory);
					continue CHANNEL;
				}
				if (tick < beginTick) {
					continue TICK;
				}
				
				// add notes
				slideNoteHistory.put(tick, chHistorySet.getValue());
			}
			
			// add structure
			this.noteHistory.put(channel, slideNoteHistory);
		}
		
		return this.noteHistory;
	}
	
	/**
	 * Copies the given structure to the slice, omitting all entries that are not related to the slice's tick scope.
	 * Returns the copied (filtered) structure.
	 * 
	 * An entry is copied, if:
	 * 
	 * - it's within the slice's scope (>= beginTick or < endTick); **or**:
	 * - it's a **Note-OFF** in a later slice **but** the note has been pressed in the slice's scope and has not yet been released.
	 * 
	 * @param noteOnOff    structure containing channel, tick, note and on/off
	 * @return the filtered structure.
	 */
	public TreeMap<Byte, TreeMap<Byte, TreeMap<Long, Boolean>>> filterOnOff(TreeMap<Byte, TreeMap<Byte, TreeMap<Long, Boolean>>> noteOnOff) {
		this.noteOnOff = new TreeMap<>();
		
		// filter and copy noteOnOff
		// CHANNEL:
		for (Entry<Byte, TreeMap<Byte, TreeMap<Long, Boolean>>> channelSet : noteOnOff.entrySet()) {
			byte channel = channelSet.getKey();
			
			TreeMap<Byte, TreeMap<Long, Boolean>> allChannelOnOff   = channelSet.getValue();
			TreeMap<Byte, TreeMap<Long, Boolean>> slideChannelOnOff = new TreeMap<>();
			
			NOTE:
			for (Entry<Byte, TreeMap<Long, Boolean>> chOnOffSet : allChannelOnOff.entrySet()) {
				byte note = chOnOffSet.getKey();
				
				TreeMap<Long, Boolean> channelNoteOnOff = chOnOffSet.getValue();
				TreeMap<Long, Boolean> slideNoteOnOff   = new TreeMap<>();
				
				TICK:
				for (Entry<Long, Boolean> chNoteEntry : channelNoteOnOff.entrySet()) {
					long    tick = chNoteEntry.getKey();
					boolean isOn = chNoteEntry.getValue();
					
					// earlier slice - ignore
					if (tick < beginTick)
						continue TICK;
					
					// slice scope matches? - add on/off
					if (tick < endTick) {
						slideNoteOnOff.put(tick, isOn);
						continue TICK;
					}
					
					// later slice - only add one last entry, if the last note is still pressed
					
					// was there any note pressed in this slice at all?
					Entry<Long, Boolean> lastOnOff = slideNoteOnOff.lastEntry();
					if (lastOnOff != null) {
						
						// is the last pressed note still pressed?
						boolean isStillPressed = lastOnOff.getValue();
						if (isStillPressed) {
							slideNoteOnOff.put(tick, isOn);
						}
					}
					
					// TODO: test
					
					// the rest of the Note-ONs are out of the slice's scope
					slideChannelOnOff.put(note, slideNoteOnOff);
					continue NOTE;
				}
				
				// add note structure
				slideChannelOnOff.put(note, slideNoteOnOff);
			}
			
			// add channel structure
			this.noteOnOff.put(channel, slideChannelOnOff);
		}
		
		return this.noteOnOff;
	}
	
	/**
	 * Adds something to a channel's timeline.
	 * This 'something' can be one of:
	 * 
	 * - an instrument change marker (meaning: one or more instrument changes)
	 * - a collection of notes and/or chords beginning at the same tick and channel
	 * 
	 * @param tick             MIDI tick
	 * @param channel          MIDI channel
	 * @param key              ID of the event
	 * @param notesOrChords    all notes and chords that begin at this channel and tick
	 *                         (or **null** in case of an instrument change marker)
	 */
	private void addToTimeline(long tick, int channel, byte key, TreeMap<String, TreeMap<Byte, String>> notesOrChords) {
		TreeMap<Byte, TreeMap<String, TreeMap<Byte, String>>> events = timelines.get(channel).get(tick);
		
		// no events yet for this channel and tick?
		if (null == events) {
			events = new TreeMap<Byte, TreeMap<String, TreeMap<Byte, String>>>();
			timelines.get(channel).put(tick, events);
		}
		
		// add event
		events.put(key, notesOrChords);
	}
}
