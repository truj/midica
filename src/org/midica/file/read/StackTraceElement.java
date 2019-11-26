/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file.read;

import java.io.File;

import org.midica.config.Dict;

/**
 * This class represents one element in a stack trace.
 * That can be a called function, a nestable block, an included
 * file or an executed line.
 * 
 * @author Jan Trukenmüller
 */
public class StackTraceElement {
	
	private String  functionName   = null;
	private String  paramStr       = "";
	private String  optionStr      = "";
	private File    file           = null;
	private int     lineOffset     = 0;
	private int     line           = 0;
	private int     lineBlockOpen  = 0;
	private int     lineBlockClose = 0;
	
	/**
	 * Creates a new stack trace element.
	 * 
	 * @param file        the file causing the new stack trace element.
	 * @param lineOffset  the line causing (or line offset containing) the new stack trace element.
	 */
	public StackTraceElement(File file, int lineOffset) {
		this.file       = file;
		this.lineOffset = lineOffset;
		line            = lineOffset;
	}
	
	/**
	 * Sets the function name.
	 * To be called only if the causing line is a function call.
	 * 
	 * @param name function name
	 */
	public void setFunctionName(String name) {
		functionName = name;
	}
	
	/**
	 * Sets the parameter string.
	 * Only needed for function calls.
	 * 
	 * @param paramString the parameter string of the function call.
	 */
	public void setParams(String paramString) {
		if (paramString != null) {
			paramStr = paramString;
		}
	}
	
	/**
	 * Sets the option string.
	 * Needed for function calls or executions of nestable blocks.
	 * 
	 * @param optionString call or block option string
	 */
	public void setOptions(String optionString) {
		if (optionString != null) {
			optionStr = optionString;
		}
	}
	
	/**
	 * Sets opening and closing line number of the nestable block.
	 * 
	 * @param lineOpen   line number of the block opening
	 * @param lineClose  line number of the block closing
	 */
	public void setNestableBlock(int lineOpen, int lineClose) {
		lineBlockOpen  = lineOpen;
		lineBlockClose = lineClose;
	}
	
	/**
	 * Sets the current line number to the offset line.
	 * This is called from the constructor, and for block executions
	 * and function calls before each run, as often as the quantity
	 * option is specified.
	 */
	public void resetLine() {
		line = lineOffset;
	}
	
	/**
	 * Increments the current line number by 1.
	 */
	public void incrementLine() {
		line++;
	}
	
	/**
	 * Increments the current line number by the given value.
	 * 
	 * @param num  value to be added to the current line number.
	 */
	public void addToLine(int num) {
		line += num;
	}
	
	/**
	 * Constructs and returns a trace element string that can be included into
	 * the stack trace displayed to the user.
	 * 
	 * @return a string describing the stack trace element.
	 */
	public String getTrace() {
		
		String action;
		String indentation  = Dict.get(Dict.STACK_TRACE_INDENTATION);
		String optsOrParams = "";
		String fileName     = file.getName();
		String callLine     = line + "";
		if (lineBlockClose != 0) {
			action = Dict.get(Dict.STACK_TRACE_BLOCK);
			if ( ! "".equals(optionStr) ) {
				optsOrParams += Dict.get(Dict.STACK_TRACE_OPTIONS) + optionStr + "<br>" + indentation;
			}
			callLine = lineBlockOpen + " - " + lineBlockClose;
		}
		else if (functionName != null) {
			action = Dict.get(Dict.STACK_TRACE_FUNCTION) + functionName;
			if ( ! "".equals(paramStr) ) {
				optsOrParams += Dict.get(Dict.STACK_TRACE_PARAMS) + MidicaPLParser.PARAM_OPEN + paramStr + MidicaPLParser.PARAM_CLOSE + "<br>" + indentation;
			}
			if ( ! "".equals(optionStr) ) {
				optsOrParams += Dict.get(Dict.STACK_TRACE_OPTIONS) + optionStr + "<br>" + indentation;
			}
		}
		else {
			// root level element
			action = Dict.get(Dict.STACK_TRACE_EXEC);
		}
		
		return action + "<br>"
			+ indentation + optsOrParams
			+ Dict.get(Dict.STACK_TRACE_IN)   + " " + fileName + ", "
			+ Dict.get(Dict.STACK_TRACE_LINE) + " " + callLine;
	}
	
	/**
	 * Returns a short description of the trace element.
	 * Used for unit tests.
	 * 
	 * @return short description of trace element.
	 */
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder("");
		str.append(file.getName());
		str.append("/");
		if (lineBlockClose != 0)
			str.append(lineBlockOpen + "-" + lineBlockClose);
		else
			str.append(line);
		return str.toString();
	}
}
