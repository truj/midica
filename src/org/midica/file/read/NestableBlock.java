/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file.read;

import java.io.File;
import java.util.ArrayList;
import java.util.Deque;
import java.util.regex.Pattern;

import org.midica.config.Dict;

/**
 * This class represents a nestable block, used by the MidicaPL parser.
 * 
 * @author Jan Trukenmüller
 */
public class NestableBlock {
	
	private static Pattern plus        = null;
	private static Pattern whitespaces = Pattern.compile("\\s+");
	
	private MidicaPLParser    parser    = null;
	private boolean           multiple  = false;
	private int               quantity  = 1;
	private String            tuplet    = null;
	private int               shift     = 0;
	private ArrayList<Object> elements  = null;
	private String            condition = null;
	
	private boolean condChainOpened = false;
	private boolean condChainHit    = false;
	
	private boolean isMultipleSet = false;
	private boolean isQuantitySet = false;
	private boolean isTupletSet   = false;
	private boolean isShiftSet    = false;
	private boolean isIf          = false;
	private boolean isElsif       = false;
	private boolean isElse        = false;
	
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
	 * Sets the **if** option as well as the according condition.
	 * 
	 * @param condition  the IF condition.
	 * @throws ParseException  if this option is combined with other if-elsif-else options.
	 */
	public void setIf(String condition) throws ParseException {
		if (isIf || isElsif || isElse) {
			throw new ParseException( Dict.get(Dict.ERROR_BLOCK_IF_MUST_BE_ALONE) );
		}
		
		isIf = true;
		this.condition = condition;
	}
	
	/**
	 * Sets the **elsif** option as well as the according condition.
	 * 
	 * @param condition  the ELSIF condition.
	 * @throws ParseException  if this option is combined with other if-elsif-else options.
	 */
	public void setElsif(String condition) throws ParseException {
		if (isIf || isElsif || isElse) {
			throw new ParseException( Dict.get(Dict.ERROR_BLOCK_ELSIF_MUST_BE_ALONE) );
		}
		
		isElsif = true;
		this.condition = condition;
	}
	
	/**
	 * Sets the **else** option.
	 * 
	 * @throws ParseException  if this option is combined with other if-elsif-else options.
	 */
	public void setElse() throws ParseException {
		if (isIf || isElsif || isElse) {
			throw new ParseException( Dict.get(Dict.ERROR_BLOCK_ELSE_MUST_BE_ALONE) );
		}
		
		isElse = true;
	}
	
	/**
	 * Returns the condition type of the block, representing **if**, **elsif**, **else** or **none**.
	 * 
	 * @return a number representing the condition type.
	 */
	public int getConditionType() {
		if (isIf)
			return MidicaPLParser.COND_TYPE_IF;
		else if (isElsif)
			return MidicaPLParser.COND_TYPE_ELSIF;
		else if (isElse)
			return MidicaPLParser.COND_TYPE_ELSE;
		return MidicaPLParser.COND_TYPE_NONE;
	}
	
	/**
	 * Returns the condition, if this block contains an **if** or **elsif**.
	 * Otherwise: returns **null**.
	 * 
	 * @return the block condition.
	 */
	public String getCondition() {
		return condition;
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
				plus = Pattern.compile( Pattern.quote(MidicaPLParser.LENGTH_PLUS) );
			
			// add tuplet to all summands inside of the duration column
			// e.g. *1+/8 --> *1t4:3+/8t4:3
			if (tuplet != null) {
				// separate duration from options
				String[] durationAndOptions = whitespaces.split(tokens[2], 2);
				
				if (MidicaPLParser.LENGTH_ZERO.equals(durationAndOptions[0])) {
					// zero-length - ignore tuplets
					return tokens;
				}
				else if (MidicaPLParser.patterns.containsKey(durationAndOptions[0])) {
					// pattern - ignore tuplets
				}
				else {
					// normal channel command - apply tuplet
					String[] atoms = plus.split(durationAndOptions[0], -1);
					for (int j=0; j < atoms.length; j++) {
						atoms[j] += tuplet;
					}
					tokens[2] = String.join(MidicaPLParser.LENGTH_PLUS, atoms);
					
					// add options again, if necessary
					if (durationAndOptions.length > 1)
						tokens[2] += " " + durationAndOptions[1];
				}
			}
		}
		catch (ParseException e) {
			// not a channel command - probably a call
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
	 * Adds all resulting shifts and tuplets to channel or call commands before execution.
	 * Also tracks the current line in the call stack.
	 * 
	 * For root blocks, the provided lineNumber is the line where the block is closed.
	 * For other blocks it's the line where the block is opened.
	 * 
	 * @param callStack    the call stack of the parser.
	 * @param isRootBlock  **true**, if this is the root block, otherwise **false**.
	 * @param file         the file where the block is defined
	 * @param lineNumber   first or last line number of the block
	 * @throws ParseException if one of the content lines cannot be parsed.
	 */
	public void play(Deque<StackTraceElement> callStack, boolean isRootBlock, File file, int lineNumber) throws ParseException {
		
		// add call stack element
		int lineNumberOpen  = lineNumber;
		int lineNumberClose = lineNumber;
		if (isRootBlock) {
			lineNumberOpen = lineNumberOpen - getNumberOfLines();
			lineNumberOpen++;
		}
		else {
			lineNumberClose = lineNumberClose - 1 + getNumberOfLines();
		}
		lineNumber = lineNumberOpen;
		StackTraceElement traceElem = new StackTraceElement(file, lineNumberOpen);
		traceElem.setOptions(getOptionsForStackTrace());
		traceElem.setNestableBlock(lineNumberOpen, lineNumberClose);
		callStack.push(traceElem);
		
		// remember current tickstamps if needed
		ArrayList<Long> tickstamps = null;
		if (multiple) {
			tickstamps = parser.rememberTickstamps();
		}
		
		// apply the block content
		for (int i = 0; i < quantity; i++) {
			
			// reset line
			lineNumber = lineNumberOpen;
			traceElem.resetLine();
			
			// reset conditions
			condChainOpened = false;
			condChainHit    = false;
			
			for (Object element : elements) {
				
				// increment line
				lineNumber++;
				traceElem.incrementLine();
				
				if (element instanceof NestableBlock) {
					NestableBlock childBlock = (NestableBlock) element;
					
					// track line
					int offsetLine = lineNumber;
					int increment  = childBlock.getNumberOfLines() - 1;
					traceElem.addToLine(increment);
					lineNumber += increment;
					
					// track temp line (only in case of an exception before the child block's play() is called)
					StackTraceElement childTraceElem = new StackTraceElement(file, childBlock.getNumberOfLines());
					childTraceElem.setOptions(childBlock.getOptionsForStackTrace());
					childTraceElem.setNestableBlock(offsetLine, lineNumber);
					callStack.push(childTraceElem);
					
					// check if/elsif/else conditions
					String  childCondition     = childBlock.getCondition();
					int     childConditionType = childBlock.getConditionType();
					boolean mustPlay = true;
					try {
						if (childCondition != null) {
							childCondition = parser.replaceVariables(childCondition);
						}
						if (MidicaPLParser.COND_TYPE_NONE == childConditionType) {
							condChainOpened = false;
							condChainHit    = false;
						}
						else {
							if (MidicaPLParser.COND_TYPE_IF == childConditionType) {
								condChainHit = false;
							}
							if (MidicaPLParser.COND_TYPE_ELSIF == childConditionType || MidicaPLParser.COND_TYPE_ELSE == childConditionType) {
								if (! condChainOpened)
									throw new ParseException( Dict.get(Dict.ERROR_BLOCK_NO_IF_FOUND) );
							}
							if (MidicaPLParser.COND_TYPE_ELSE == childConditionType)
								mustPlay = ! condChainHit;
							else if (condChainHit)
								mustPlay = false;
							else
								mustPlay = parser.evalCondition(childCondition);
						}
					}
					catch (ParseException e) {
						e.setCausedByBlockConditions();
						throw e;
					}
					
					// remove temporary stack element
					callStack.pop();
					
					// execute child block
					if (mustPlay) {
						childBlock.play(callStack, false, file, offsetLine);
					}
					
					// postprocess conditions
					if (MidicaPLParser.COND_TYPE_ELSE == childConditionType) {
						condChainOpened = false;
						condChainHit    = false;
					}
					else if (childConditionType != MidicaPLParser.COND_TYPE_NONE) {
						condChainOpened = true;
						condChainHit    = condChainHit || mustPlay;
					}
				}
				else if (element instanceof String[]) {
					
					// add line to call stack
					StackTraceElement lineTraceElem = new StackTraceElement(file, lineNumber);
					callStack.push(lineTraceElem);
					
					// reset conditions
					String[] tokens = (String[]) element;
					if (! "".equals(tokens[0])) {
						condChainOpened = false;
						condChainHit    = false;
					}
					
					// parse line
					String line;
					if (shift != 0 || tuplet != null) {
						if (! MidicaPLParser.VAR.equals(tokens[0])) {
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
					parser.parseLine(String.join(" ", tokens));
					
					// remove line from call stack
					callStack.pop();
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
		
		// remove call stack element
		callStack.pop();
	}
	
	/**
	 * Returns the resulting options string for the current block so that it can
	 * be used in a stack trace.
	 * 
	 * @return  options string
	 */
	private String getOptionsForStackTrace() {
		ArrayList<String> options = new ArrayList<>();
		if (quantity != 1) {
			options.add(MidicaPLParser.Q + MidicaPLParser.OPT_ASSIGNER + quantity);
		}
		if (multiple) {
			options.add(MidicaPLParser.M);
		}
		if (tuplet != null) {
			options.add(MidicaPLParser.T + MidicaPLParser.OPT_ASSIGNER + tuplet);
		}
		if (shift != 0) {
			options.add(MidicaPLParser.S + MidicaPLParser.OPT_ASSIGNER + shift);
		}
		if (isIf) {
			options.add(MidicaPLParser.OPT_IF + " " + condition);
		}
		if (isElsif) {
			options.add(MidicaPLParser.OPT_ELSIF + " " + condition);
		}
		if (isElse) {
			options.add(MidicaPLParser.OPT_ELSE);
		}
		
		return String.join(MidicaPLParser.OPT_SEPARATOR + " ", options);
	}
	
	/**
	 * Recursively calculates the number of lines of this block.
	 * 
	 * @return the number of lines, including all sub blocks.
	 */
	public int getNumberOfLines() {
		int count = 2; // opening and closing block
		for (Object element : elements) {
			
			if (element instanceof NestableBlock) {
				count += ((NestableBlock) element).getNumberOfLines();
			}
			else if (element instanceof String[]) {
				count++;
			}
		}
		
		return count;
	}
}
