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
 * This class represents a nestable block, used by the MidicaPL parser.
 * 
 * @author Jan Trukenmüller
 */
public class NestableBlock {
	
	private MidicaPLParser    parser   = null;
	private boolean           multiple = false;
	private int               quantity = 1;
	private String            tuplet   = null;
	private ArrayList<Object> elements = null;
	
	private boolean isMultipleSet = false;
	private boolean isQuantitySet = false;
	private boolean isTupletSet   = false;
	
	/**
	 * Creates a new nestable block.
	 * 
	 * @param parser    The parser object that was responsible to create the block.
	 */
	public NestableBlock(MidicaPLParser parser) {
		this.parser   = parser;
		this.elements = new ArrayList<Object>();
	}
	
	/**
	 * Sets the multiple option.
	 * This option indicates if the channel tickstamps are reverted at the end of the block.
	 * 
	 * @param optId           option ID (only needed for the exception description)
	 * @throws ParseException if this option has already been set for this block.
	 */
	public void setMultiple(String optId) throws ParseException {
		
		// already set?
		if (isMultipleSet) {
			throw new ParseException( Dict.get(Dict.ERROR_BLOCK_ARG_ALREADY_SET) + optId );
		}
		
		// set
		multiple      = true;
		isMultipleSet = true;
	}
	
	/**
	 * Sets the quantity option.
	 * This option indicates how often the block is executed.
	 * 
	 * @param quantity        the quantity value
	 * @param optId           option ID (only needed for the exception description)
	 * @throws ParseException if this option has already been set for this block.
	 */
	public void setQuantity(int quantity, String optId) throws ParseException {
		
		// already set?
		if (isQuantitySet) {
			throw new ParseException( Dict.get(Dict.ERROR_BLOCK_ARG_ALREADY_SET) + optId );
		}
		
		// set
		this.quantity = quantity;
		isQuantitySet = true;
	}
	
	/**
	 * Sets the tuplet modifier.
	 * This modifier shortens all notes of this block according to the tuplet value.
	 * This applies for notes and rests as well as for child blocks.
	 * 
	 * @param tuplet
	 * @param optId           option ID (only needed for the exception description)
	 * @throws ParseException if this option has already been set for this block.
	 */
	public void setTuplet(String tuplet, String optId) throws ParseException {
		
		// already set?
		if (isTupletSet) {
			throw new ParseException( Dict.get(Dict.ERROR_BLOCK_ARG_ALREADY_SET) + optId );
		}
		
		this.tuplet = tuplet;
		isTupletSet = true;
	}
	
	/**
	 * Applies tuplets to all child elements.
	 * 
	 * @param parentTuplets    The tuplet modifiers from all (grand)parents to be applied as well.
	 */
	public void applyTuplets(String parentTuplets) {
		String resultingTuplet = tuplet;
		if (resultingTuplet == null) {
			resultingTuplet = parentTuplets;
		}
		else if (parentTuplets != null) {
			resultingTuplet += parentTuplets;
		}
		
		// apply to children
		for (Object element : elements) {
			if (element instanceof NestableBlock) {
				((NestableBlock) element).applyTuplets(resultingTuplet);
			}
			else if (resultingTuplet != null && element instanceof String[]) {
				String[] tokens = (String[]) element;
				if (tokens.length >= 3) {
					try {
						// channel command?
						parser.toChannel(tokens[0]);
						
						// no exception - channel command
						tokens[2] += resultingTuplet;
					}
					catch (ParseException e) {
						// not a channel command - probably an include
					}
				}
			}
		}
	}
	
	/**
	 * Adds a new content element to this block.
	 * The content to be added can be one of the following objects:
	 * 
	 * - an array of strings belonging to one source code line
	 * - another (nested) block
	 * 
	 * @param element the content to be added
	 */
	public void add(Object element) {
		elements.add(element);
	}
	
	/**
	 * Executes the content of the block.
	 * 
	 * @throws ParseException if one of the content lines cannot be parsed.
	 */
	public void play() throws ParseException {
		
		// remember current tickstamps if needed
		ArrayList<Long> tickstamps = null;
		if (multiple) {
			tickstamps = parser.rememberTickstamps();
		}
		
		// apply the block content
		for (int i = 0; i < quantity; i++) {
			for (Object element : elements) {
				if (element instanceof NestableBlock) {
					((NestableBlock) element).play();
				}
				else if (element instanceof String[]) {
					parser.parseLine( String.join(" ", (String[]) element) );
				}
				else {
					throw new ParseException("invalid block element class: " + element);
				}
			}
		}
		
		// restore tickstamps, if needed
		if (multiple) {
			parser.restoreTickstamps(tickstamps);
		}
	}
	
	/**
	 * Returns a string representation of the block contents.
	 * This is mainly for debugging.
	 */
	public String toString() {
		if (null == elements) {
			return "null";
		}
		StringBuilder str = new StringBuilder("( ");
		if (isMultipleSet)
			str.append("m ");
		if (isQuantitySet)
			str.append("q=" + quantity + " ");
		if (isTupletSet)
			str.append("t=" + tuplet);
		str.append("\n");
		for (Object element : elements) {
			if (element instanceof NestableBlock) {
				str.append(element);
			}
			else {
				str.append( String.join(" ", (String[]) element) );
				str.append("\n");
			}
		}
		str.append(")\n");
		return str.toString();
	}
}
