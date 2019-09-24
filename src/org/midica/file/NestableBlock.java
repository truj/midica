/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.midica.config.Dict;

/**
 * This class represents a nestable block, used by the MidicaPL parser.
 * 
 * @author Jan Trukenmüller
 */
public class NestableBlock {
	
	private static Pattern plus = null;
	
	private MidicaPLParser    parser   = null;
	private boolean           multiple = false;
	private int               quantity = 1;
	private String            tuplet   = null;
	private int               shift    = 0;
	private ArrayList<Object> elements = null;
	
	private boolean isMultipleSet = false;
	private boolean isQuantitySet = false;
	private boolean isTupletSet   = false;
	private boolean isShiftSet    = false;
	
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
	 * Resets the precompiled PLUS pattern because in the next compile run it could be redefined.
	 */
	public static void reset() {
		plus = null;
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
	 * @param tuplet          the tuplet value
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
	 * Sets the shift modifier.
	 * This modifier applies a transposition of the block by the shift value.
	 * This applies for notes and chords as well as for child blocks.
	 * 
	 * @param shift           the shift value (a positive or negative number of half tone steps)
	 * @param optId           option ID (only needed for the exception description)
	 * @throws ParseException if this option has already been set for this block.
	 */
	public void setShift(int shift, String optId) throws ParseException {
		
		// already set?
		if (isShiftSet) {
			throw new ParseException( Dict.get(Dict.ERROR_BLOCK_ARG_ALREADY_SET) + optId );
		}
		
		this.shift += shift;
		isShiftSet = true;
	}
	
	/**
	 * Applies tuplets and shifts to all child blocks.
	 * 
	 * @param parentTuplets  The tuplet modifiers from all (grand)parents to be applied as well.
	 * @param parentShifts   The sum of all shift modifiers from all (grand)parents to be applied as well.
	 */
	public void applyTupletsAndShifts(String parentTuplets, int parentShifts) {
		
		// apply to this block
		shift += parentShifts;
		if (tuplet == null) {
			tuplet = parentTuplets;
		}
		else if (parentTuplets != null) {
			tuplet += parentTuplets;
		}
		
		// apply to children
		for (Object element : elements) {
			if (element instanceof NestableBlock) {
				((NestableBlock) element).applyTupletsAndShifts(tuplet, shift);
			}
		}
	}
	
	/**
	 * Adds the tuplets to the duration column of a channel command.
	 * 
	 * @param tokens  command tokens
	 * @return resulting command tokens.
	 */
	private String[] addTuplets(String[] tokens) {
		
		if (tokens.length < 3)
			return tokens;
		
		try {
			// channel command?
			parser.toChannel(tokens[0]);
			
			// no exception - channel command
			
			if (null == plus)
				plus = Pattern.compile( Pattern.quote(MidicaPLParser.DURATION_PLUS) );
			
			// add tuplet to all summands inside of the duration column
			// e.g. *1+/8 --> *1t4:3+/8t4:3
			if (tuplet != null) {
				String[] atoms = plus.split(tokens[2], -1);
				for (int j=0; j < atoms.length; j++) {
					atoms[j] += tuplet;
				}
				tokens[2] = String.join(MidicaPLParser.DURATION_PLUS, atoms);
			}
		}
		catch (ParseException e) {
			// not a channel command - probably an include
		}
		
		return tokens;
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
	 * Adds all resulting shifts and tuplets to channel or include commands before execution.
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
					String[] tokens = (String[]) element;
					String line;
					if (shift != 0 || tuplet != null) {
						if ( ! MidicaPLParser.VAR.equals(tokens[0]) ) {
							line = String.join(" ", tokens);
							line = parser.replaceVariables(line);
							tokens = line.split("\\s+", 3);
						}
					}
					if (shift != 0) {
						tokens = parser.addShiftToOptions(tokens, shift);
					}
					if (tuplet != null) {
						tokens = addTuplets(tokens);
					}
					parser.parseLine( String.join(" ", tokens) );
				}
				else {
					throw new ParseException("invalid block element class: " + element.getClass());
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
