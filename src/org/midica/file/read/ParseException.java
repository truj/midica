/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file.read;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

import org.midica.config.Dict;

/**
 * Exceptions of this class can be thrown if an error occurs while parsing a file.
 * It can be used for different file types.
 * 
 * @author Jan Trukenmüller
 */
public class ParseException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	private int                      lineNumber         = 0;
	private File                     file               = null;
	private Deque<StackTraceElement> stackTrace         = null;
	private String                   lineContent        = null;
	private boolean                  isLastAdded        = false;
	private boolean                  causedByBlockCond  = false;
	private boolean                  causedByInvalidVar = false;
	
	/**
	 * Throws a generic parse exception without a detail message.
	 */
	public ParseException() {
	}
	
	/**
	 * Throws an exception including a detail message.
	 * 
	 * @param message  the error message
	 */
	public ParseException(String message) {
		super(message);
	}
	
	/**
	 * Returns the line number of the parsed file where the exception occured.
	 * 
	 * @return    Line number of the exception.
	 */
	public int getLineNumber() {
		return lineNumber;
	}
	
	/**
	 * Sets the line number of the parsed file where the exception occured.
	 * 
	 * @param lineNumber        Line number of the exception.
	 */
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}
	
	/**
	 * Sets the content of the line that was parsed when the exception has been thrown, if not yet done.
	 * If a line content is already set, this will **not** be overridden.
	 * This is the case if an exception occurs in an included file.
	 * 
	 * @param line  MidicaPL line content.
	 */
	public void setLineContentIfNotYetDone(String line) {
		if (null == lineContent) {
			lineContent = line;
		}
	}
	
	/**
	 * Returns the content of the line causing the exception.
	 * 
	 * This is only used for unit tests.
	 * 
	 * @return line content
	 */
	public String getLineContent() {
		return lineContent;
	}
	
	/**
	 * Marks this exeption as being caused by if/elsif/else conditions of a nestable block.
	 */
	public void setCausedByBlockConditions() {
		causedByBlockCond = true;
	}
	
	/**
	 * Marks this exeption as being caused by an invalid variable or parameter name.
	 * 
	 * @param varName  the variable or parameter name causing the problem
	 */
	public void setCausedByInvalidVar(String varName) {
		causedByInvalidVar = true;
		lineContent        = varName;
	}
	
	/**
	 * Returns the parsed file.
	 * 
	 * @return  file name
	 */
	public File getFile() {
		return file;
	}
	
	/**
	 * Sets the parsed file.
	 * 
	 * @param file    Parsed file.
	 */
	public void setFile(File file) {
		this.file = file;
	}
	
	/**
	 * Sets the given call stack.
	 * 
	 * @param stack the call stack to be set.
	 */
	public void setStackTrace(Deque<StackTraceElement> stack) {
		stackTrace = stack;
	}
	
	/**
	 * Returns the message including file name, line number, line content and/or stack trace,
	 * whatever is available.
	 * 
	 * @return error message including more information, if available.
	 */
	public String getFullMessage() {
		
		String msg = getMessage();
		StringBuilder fullMsg = new StringBuilder("<html>" + msg + "<br>");
		
		String lineStr = "<br>";
		if (causedByBlockCond)
			lineStr += Dict.get(Dict.EXCEPTION_CAUSED_BY_BLK_COND);
		else if (causedByInvalidVar)
			lineStr += Dict.get(Dict.EXCEPTION_CAUSED_BY_INVALID_VAR) + "<br>" + lineContent;
		else if (lineContent != null)
			lineStr += Dict.get(Dict.EXCEPTION_CAUSED_BY_LINE) + "<br>" + lineContent;
		if (causedByBlockCond || causedByInvalidVar || lineContent != null) {
			lineStr += "<br>";
			msg     += "<br>" + lineStr;
			fullMsg.append(lineStr);
		}
		
		// call stack available? - add stack info
		if (stackTrace != null && ! stackTrace.isEmpty()) {
			
			// add call stack header?
			fullMsg.append("<br>" + Dict.get(Dict.STACK_TRACE_HEADER) + "<br>");
			
			// add the last line to the call stack
			addLastElementIfNotYetDone();
			
			// clone stack trace
			Deque<StackTraceElement> stackTraceCopy = ((ArrayDeque<StackTraceElement>) stackTrace).clone();
			
			// add call stack to message
			do { 
				StackTraceElement elem = stackTraceCopy.pop();
				fullMsg.append(elem.getTrace() + "<br>");
			} while (! stackTraceCopy.isEmpty());
			msg = fullMsg.toString();
		}
		
		// file and line available?
		else if (lineNumber > 0 && null != file) {
			
			// get full path if possible (otherwise, only the file name)
			String filePath = file.getName();
			try {
				filePath = file.getCanonicalPath();
			}
			catch (IOException e) {
			}
			
			// compile error message
			msg = String.format(
				Dict.get(Dict.ERROR_IN_LINE),
				filePath,
				lineNumber
			) + msg;
		}
		
		return msg;
	}
	
	/**
	 * Returns a clone of the stack trace.
	 * 
	 * @return clone of the stack trace.
	 */
	public Deque<StackTraceElement> getStackTraceElements() {
		if (null == stackTrace)
			return null;
		
		// add the last line to the call stack
		addLastElementIfNotYetDone();
		
		return ((ArrayDeque<StackTraceElement>) stackTrace).clone();
	}
	
	/**
	 * Adds the final element to the stack trace, if not yet done.
	 */
	private void addLastElementIfNotYetDone() {
		if (isLastAdded)
			return;
		
		StackTraceElement finalElem = new StackTraceElement(file, lineNumber);
		stackTrace.add(finalElem);
		isLastAdded = true;
	}
}
