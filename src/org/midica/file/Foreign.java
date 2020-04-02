/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.midica.config.Dict;

/**
 * This class provides methods to interact with foreign command line tools.
 * 
 * This is used for importing and exporting files using an intermediate, temporary MIDI file.
 * 
 * @author Jan Trukenmüller
 */
public class Foreign {
	
	/**
	 * Creates a temporary directory.
	 * 
	 * @return the temporary directory
	 * @throws ForeignException if something goes wrong
	 */
	public static Path createTempDirectory() throws ForeignException {
		
		Path path = null;
		try {
			path = Files.createTempDirectory(null);
		}
		catch (IOException e) {
			throw new ForeignException(Dict.get(Dict.FOREIGN_CREATE_TMPDIR));
		}
		
		File dir = null;
		try {
			dir = path.toFile();
			dir.deleteOnExit();
		}
		catch (UnsupportedOperationException e) {
			throw new ForeignException(Dict.get(Dict.FOREIGN_CREATE_TMPDIR));
		}
		
		return path;
	}
	
	/**
	 * Returns all files inside the given temp directory.
	 * 
	 * @param dirPath  temp directory to be read
	 * @return all files in the given directory.
	 * @throws ForeignException if something goes wrong
	 */
	public static File[] getFiles(Path dirPath) throws ForeignException {
		File[] files = null;
		try {
			File dir = dirPath.toFile();
			files = dir.listFiles();
		}
		catch (Exception e) {
			throw new ForeignException(Dict.get(Dict.FOREIGN_READ_TMPDIR));
		}
		
		return files;
	}
	
	/**
	 * Removes the given temp directory.
	 * 
	 * @param dir    the directory to remove
	 */
	public static void deleteTempDir(Path dir) {
		try {
			File directory = dir.toFile();
			
			// TODO: add recursive deletion, as soon as we need this
			
			// delete the directory
			directory.delete();
		}
		catch (Exception e) {
			// ignore exceptions
		}
	}
	
	/**
	 * Creates a temporary file to be used for MIDI import or export.
	 * 
	 * @return the temporary file
	 * @throws ForeignException if something goes wrong
	 */
	public static File createTempMidiFile() throws ForeignException {
		return createTempFile("mid", null);
	}
	
	/**
	 * Creates a temporary file.
	 * 
	 * @param extension    file extension (without the dot) - may be null
	 * @param parentDir    parent directory or **null**, if the directory doesn't matter
	 * @return the temporary file
	 * @throws ForeignException if something goes wrong
	 */
	public static File createTempFile(String extension, Path parentDir) throws ForeignException {
		
		Path path = null;
		try {
			if (parentDir != null) {
				if (null == extension)
					path = Files.createTempFile(parentDir, null, null);
				else if ("".equals(extension))
					path = Files.createTempFile(parentDir, null, "");
				else
					path = Files.createTempFile(parentDir, null, "." + extension);
			}
			else {
				if (null == extension)
					path = Files.createTempFile(null, null);
				else if ("".equals(extension))
					path = Files.createTempFile(null, "");
				else
					path = Files.createTempFile(null, "." + extension);
			}
		}
		catch (IOException e) {
			throw new ForeignException(Dict.get(Dict.FOREIGN_CREATE_TMPFILE));
		}
		
		File file = null;
		try {
			file = path.toFile();
			file.deleteOnExit();
		}
		catch (UnsupportedOperationException e) {
			throw new ForeignException(Dict.get(Dict.FOREIGN_CREATE_TMPFILE));
		}
		
		return file;
	}
	
	/**
	 * Deletes the given temporary file.
	 * Suppresses any exception, if the deletion fails.
	 * 
	 * @param file    the file to be deleted
	 */
	public static void deleteTempFile(File file) {
		try {
			file.delete();
		}
		catch(Exception e) {
		}
	}
	
	/**
	 * Executes a command.
	 * 
	 * Transforms the given **cmd** array to a list and calls {@link #execute(List, String, boolean)}.
	 * 
	 * @param cmd                   Command and Options
	 * @param programName           Name of the foreign Program to be displayed in error messages
	 * @param acceptAllExitCodes    **true** to silently ignore exit codes other than 0.
	 *                              **false** to throw an exception in this case.
	 * @throws ForeignException if an error occurs.
	 */
	public static void execute(String[] cmd, String programName, boolean acceptAllExitCodes) throws ForeignException {
		List<String> cmdList = new ArrayList<>();
		for (String token : cmd)
			cmdList.add(token);
		execute(cmdList, programName, acceptAllExitCodes);
	}
	
	/**
	 * Executes a command.
	 * 
	 * @param cmd                   Command and Options
	 * @param programName           Name of the foreign Program to be displayed in error messages
	 * @param acceptAllExitCodes    **true** to silently ignore exit codes other than 0.
	 *                              **false** to throw an exception in this case.
	 * @throws ForeignException if an error occurs. Sets the path-checked marker inside of
	 *                          the exception, if the command was found.
	 */
	public static void execute(List<String> cmd, String programName, boolean acceptAllExitCodes) throws ForeignException {
		ProcessBuilder pb = new ProcessBuilder(cmd);
		try {
			Process process = pb.start();
			
			// get STDERR and STDOUT
			String stdErr = "";
			String stdOut = "";
			BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			BufferedReader outReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = errReader.readLine()) != null) {
				stdErr += line + "<br>";
			}
			while ((line = outReader.readLine()) != null) {
				stdOut += line + "<br>";
			}
			
			try {
				int exitCode = process.waitFor();
				
				if (acceptAllExitCodes)
					return;
				
				if (exitCode != 0) {
					
					// get the command string for the error message
					StringBuilder composedCmd = new StringBuilder(cmd.get(0));
					for (int i = 1; i < cmd.size(); i++) {
						composedCmd.append(" '" + cmd.get(i) + "'");
					}
					
					// format and show the error message
					String msg = String.format(
						Dict.get(Dict.FOREIGN_EX_CODE),
						composedCmd.toString(),
						exitCode,
						stdErr,
						stdOut
					);
					ForeignException fe = new ForeignException(msg);
					fe.setPathChecked();
					throw fe;
				}
			}
			catch (InterruptedException e) {
				String msg = String.format(Dict.get(Dict.FOREIGN_EX_INTERRUPTED), programName);
				ForeignException fe = new ForeignException(msg);
				fe.setPathChecked();
				throw fe;
			}
		}
		catch (IOException e) {
			String exe = cmd.get(0);
			String msg;
			if ("".equals(exe)) {
				msg = String.format(Dict.get(Dict.FOREIGN_EX_NO_EXE), programName);
			}
			else {
				msg = String.format(
					Dict.get(Dict.FOREIGN_EX_EXECUTE),
					programName,
					programName,
					"'" + cmd.get(0) + "'"
				);
			}
			throw new ForeignException(msg);
		}
	}
}
