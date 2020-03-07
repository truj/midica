/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.file;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.midica.config.Config;
import org.midica.config.Dict;
import org.midica.config.KeyBindingManager;
import org.midica.ui.UiController;
import org.midica.ui.UiView;
import org.midica.ui.widget.MidicaFileChooser;

/**
 * Provides a file selector window for choosing files with a certain
 * set of extensions.
 * 
 * @author Jan Trukenmüller
 */
public class FileSelector extends JDialog {

	private static final long serialVersionUID = 1L;
	
	// file types (NOT the same as file extensions)
	public static final String FILE_TYPE_MPL       = "mpl";
	public static final String FILE_TYPE_ALDA      = "alda";
	public static final String FILE_TYPE_MIDI      = "midi";
	public static final String FILE_TYPE_SOUNDFONT = "sf2";
	public static final byte   READ                = 1;
	public static final byte   WRITE               = 2;
	
	private String fileType;
	private byte   filePurpose;
	
	ArrayList<MidicaFileChooser>    fileChoosers = null;
	UiController                    controller   = null;
	private HashMap<String, String> tabs         = null;
	private JTabbedPane             content      = null;
	
	/**
	 * Creates a new file selector window.
	 * 
	 * @param v    Parent view.
	 * @param c    Listener for file choosing events.
	 */
	public FileSelector(UiView v, UiController c) {
		super(v, Dict.get(Dict.TITLE_FILE_SELECTOR), true);
		controller = c;
	}
	
	/**
	 * Initializes and shows the file selector window including the {@link JFileChooser}.
	 * Sets a {@link FileExtensionFilter} for selecting the files by type.
	 * 
	 * @param type           File type by which the files are filtered.
	 * @param filePurpose    Purpose for which the file is going to be opened.
	 *                       Possible values are **READ** or **WRITE**.
	 */
	public void init(String type, byte filePurpose) {
		this.fileType     = type;
		this.filePurpose  = filePurpose;
		this.fileChoosers = new ArrayList<>();
		this.tabs         = new HashMap<>();
		
		String  directory     = "";
		boolean charSetSelect = false;
		if (READ == this.filePurpose) {
    		if (type.equals(FILE_TYPE_MIDI)) {
    			directory     = Config.get(Config.DIRECTORY_MID);
    			charSetSelect = true;
    		}
    		else if (type.equals(FILE_TYPE_SOUNDFONT)) {
    			directory     = Config.get(Config.DIRECTORY_SF2);
    			charSetSelect = false;
    		}
    		else {
    			directory     = Config.get(Config.DIRECTORY_MPL);
    			charSetSelect = true;
    		}
    	}
		else {
			if (type.equals(FILE_TYPE_MIDI)) {
    			directory     = Config.get(Config.DIRECTORY_EXPORT_MID);
    			charSetSelect = true;
			}
    		else {
    			directory     = Config.get(Config.DIRECTORY_EXPORT_MPL);
    			charSetSelect = true;
    			tabs.put(type, Dict.TAB_MIDICAPL);
    		}
		}
		
		// create the first file chooser
		fileChoosers.add( new MidicaFileChooser(type, filePurpose, directory, charSetSelect, this) );
		
		// TODO: enable, if the ALDA decompiler is ready
		// create more file choosers, if needed
//		if (WRITE == this.filePurpose && FILE_TYPE_MPL.equals(type)) {
//			type = FILE_TYPE_ALDA;
//			fileChoosers.add(new MidicaFileChooser(
//				FILE_TYPE_ALDA,
//				filePurpose,
//				Config.get(Config.DIRECTORY_EXPORT_ALDA),
//				false,
//				this
//			));
//			tabs.put(FILE_TYPE_ALDA, Dict.TAB_ALDA);
//		}
		
		// complete file choosers
		for (MidicaFileChooser chooser : fileChoosers) {
			chooser.setFileFilter(new FileExtensionFilter(chooser.getType()));
			chooser.setAcceptAllFileFilterUsed(false);
			chooser.addActionListener(controller);
		}
		
		// UI
		initUI();
		
		// refresh directory when (re)activated
		this.addWindowListener(new WindowListener() {
			
			@Override
			public void windowActivated(WindowEvent e) {
				for (MidicaFileChooser chooser : fileChoosers) {
					chooser.rescanCurrentDirectory();
				}
			}
			
			@Override
			public void windowOpened(WindowEvent e) {
			}
			@Override
			public void windowIconified(WindowEvent e) {
			}
			@Override
			public void windowDeiconified(WindowEvent e) {
			}
			@Override
			public void windowDeactivated(WindowEvent e) {
			}
			@Override
			public void windowClosing(WindowEvent e) {
			}
			@Override
			public void windowClosed(WindowEvent e) {
			}
		});
	}
	
	/**
	 * Initializes the user interface.
	 */
	private void initUI() {
		
		// only one file chooser
		if (1 == fileChoosers.size()) {
			add(fileChoosers.get(0));
		}
		else {
			// more than one file chooser - tabbed pane
			content = new JTabbedPane(JTabbedPane.LEFT);
			add(content);
			
			for (MidicaFileChooser chooser : fileChoosers) {
				String tabString = Dict.get(tabs.get(chooser.getType()));
				content.addTab(tabString, chooser);
			}
			
			// add listener to change the file type according to the chosen tab
			content.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					int index = content.getSelectedIndex();
					fileType  = fileChoosers.get(index).getType();
				}
			});
		}
		
		addKeyBindings();
		pack();
		setVisible(false);
	}
	
	/**
	 * Returns the file type.
	 * 
	 * @return file type.
	 */
	public String getFileType() {
		return this.fileType;
	}
	
	/**
	 * Sets the directory of the chosen file in the config.
	 * 
	 * Remembers the directory of that file so that next time the selector defaults to the same
	 * directory if the same file type for the same purpose is going to be opened.
	 */
	public void rememberDirectory() {
		
		// get the selected file from the right file chooser
		File file = null;
		for (MidicaFileChooser chooser : fileChoosers) {
			if (fileType.equals(chooser.getType())) {
				file = chooser.getSelectedFile();
				break;
			}
		}
		
		// no file selected? - probably this is during startup after parsing an automatically
		// parsed, remembered file
		if (null == file) {
			return;
		}
		
		// get directory and store it in the config
		try {
			String directory = file.getParentFile().getCanonicalPath();
			if (READ == this.filePurpose) {
				if (FILE_TYPE_MIDI.equals(fileType))
					Config.set(Config.DIRECTORY_MID, directory);
    			else if (FILE_TYPE_SOUNDFONT.equals(fileType))
    				Config.set(Config.DIRECTORY_SF2, directory);
    			else
    				Config.set(Config.DIRECTORY_MPL, directory);
    		}
			else {
				if (FILE_TYPE_MIDI.equals(fileType))
					Config.set(Config.DIRECTORY_EXPORT_MID, directory);
				if (FILE_TYPE_ALDA.equals(fileType))
					Config.set(Config.DIRECTORY_EXPORT_ALDA, directory);
				else
    				Config.set(Config.DIRECTORY_EXPORT_MPL, directory);
			}
		}
		catch (IOException e) {
		}
	}
	
	/**
	 * Adds key bindings to the info window.
	 */
	private void addKeyBindings() {
		
		// reset everything
		KeyBindingManager keyBindingManager = new KeyBindingManager(this, this.getRootPane());
		
		// close bindings
		keyBindingManager.addBindingsForClose(Dict.KEY_FILE_SELECT_CLOSE);
		
		// open decompile config
		ArrayList<JComponent> icons = new ArrayList<>();
		for (MidicaFileChooser chooser : fileChoosers) {
			JComponent icon = chooser.getDecompileConfigIcon();
			if (icon != null)
				icons.add(icon);
		}
		if (icons.size() > 0)
			keyBindingManager.addBindingsForIconLabelOfVisibleTab(icons, Dict.KEY_FILE_SELECT_DC_OPEN);
		
		// add key binding to choose a tab
		if (content != null) {
			if (WRITE == this.filePurpose) {
				keyBindingManager.addBindingsForTabLevel1( content, Dict.KEY_FILE_SELECTOR_MPL,  0 );
				keyBindingManager.addBindingsForTabLevel1( content, Dict.KEY_FILE_SELECTOR_ALDA, 1 );
			}
		}
		
		// set input and action maps
		keyBindingManager.postprocess();
	}
}
