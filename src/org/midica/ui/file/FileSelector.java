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

import javax.swing.JComboBox;
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
	 * If this file selector contains tabs, than the **type** parameter is used for
	 * the file selector in the **first** tab. It's changed later, if the tab is switched.
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
			if (type.equals(FILE_TYPE_SOUNDFONT)) {
    			directory     = Config.get(Config.DIRECTORY_SF2);
    			charSetSelect = false;
    		}
			else {
    			directory     = Config.get(Config.DIRECTORY_MPL);
    			charSetSelect = true;
    			tabs.put(type, Dict.TAB_MIDICAPL);
    		}
    	}
		else {
			directory     = Config.get(Config.DIRECTORY_EXPORT_MID);
			charSetSelect = true;
			tabs.put(type, Dict.TAB_MIDI);
		}
		
		// create the first file chooser
		fileChoosers.add(new MidicaFileChooser(
			type, filePurpose, directory, charSetSelect, null, this
		));
		
		// create more file choosers, if needed
		if (READ == this.filePurpose && ! type.equals(FILE_TYPE_SOUNDFONT)) {
			fileChoosers.add(new MidicaFileChooser(
				FILE_TYPE_MIDI,
				filePurpose,
				Config.get(Config.DIRECTORY_MID),
				true,
				null,
				this
			));
			tabs.put(FILE_TYPE_MIDI, Dict.TAB_MIDI);
			
			fileChoosers.add(new MidicaFileChooser(
				FILE_TYPE_ALDA,
				filePurpose,
				Config.get(Config.DIRECTORY_ALDA),
				false,
				Config.EXEC_PATH_IMP_ALDA,
				this
			));
			tabs.put(FILE_TYPE_ALDA, Dict.TAB_ALDA);
		}
		else if (WRITE == this.filePurpose) {
			fileChoosers.add(new MidicaFileChooser(
				FILE_TYPE_MPL,
				filePurpose,
				Config.get(Config.DIRECTORY_EXPORT_MPL),
				true,
				null,
				this
			));
			tabs.put(FILE_TYPE_MPL, Dict.TAB_MIDICAPL);
			
			fileChoosers.add(new MidicaFileChooser(
				FILE_TYPE_ALDA,
				filePurpose,
				Config.get(Config.DIRECTORY_EXPORT_ALDA),
				false,
				null,
				this
			));
			tabs.put(FILE_TYPE_ALDA, Dict.TAB_ALDA);
		}
		
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
		
		// only one file chooser? - add it directly
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
				if (FILE_TYPE_MPL.equals(fileType))
    				Config.set(Config.DIRECTORY_MPL, directory);
				else if (FILE_TYPE_MIDI.equals(fileType))
					Config.set(Config.DIRECTORY_MID, directory);
				else if (FILE_TYPE_ALDA.equals(fileType))
					Config.set(Config.DIRECTORY_ALDA, directory);
    			else if (FILE_TYPE_SOUNDFONT.equals(fileType))
    				Config.set(Config.DIRECTORY_SF2, directory);
    		}
			else {
				if (FILE_TYPE_MIDI.equals(fileType))
    				Config.set(Config.DIRECTORY_EXPORT_MID, directory);
				else if (FILE_TYPE_MPL.equals(fileType))
					Config.set(Config.DIRECTORY_EXPORT_MPL, directory);
				else if (FILE_TYPE_ALDA.equals(fileType))
					Config.set(Config.DIRECTORY_EXPORT_ALDA, directory);
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
		
		// open charset combobox / focus execution command / open decompile config
		ArrayList<JComboBox<?>> cbxs   = new ArrayList<>();
		ArrayList<JComponent>   fields = new ArrayList<>();
		ArrayList<JComponent>   icons  = new ArrayList<>();
		for (MidicaFileChooser chooser : fileChoosers) {
			JComboBox<?> cbx  = chooser.getCharsetSelectionCbx();
			JComponent   fld  = chooser.getForeignExecField();
			JComponent   icon = chooser.getDecompileConfigIcon();
			if (cbx != null)
				cbxs.add(cbx);
			if (fld != null)
				fields.add(fld);
			if (icon != null)
				icons.add(icon);
		}
		if (cbxs.size() > 0)
			keyBindingManager.addBindingsForComboboxOfVisibleElement(cbxs, Dict.KEY_FILE_SELECT_CHARSET_CBX);
		if (fields.size() > 0)
			keyBindingManager.addBindingsForFocusOfVisibleElement(fields, Dict.KEY_FILE_SELECT_FOREIGN_EXE);
		if (icons.size() > 0)
			keyBindingManager.addBindingsForIconLabelOfVisibleTab(icons, Dict.KEY_FILE_SELECT_DC_OPEN);
		
		// add key binding to choose a tab
		if (content != null) {
			if (READ == this.filePurpose) {
				keyBindingManager.addBindingsForTabLevel1( content, Dict.KEY_FILE_SELECTOR_IMP_MPL,  0 );
				keyBindingManager.addBindingsForTabLevel1( content, Dict.KEY_FILE_SELECTOR_IMP_MID,  1 );
				keyBindingManager.addBindingsForTabLevel1( content, Dict.KEY_FILE_SELECTOR_IMP_ALDA, 2 );
			}
			else {
				keyBindingManager.addBindingsForTabLevel1( content, Dict.KEY_FILE_SELECTOR_EXP_MID,  0 );
				keyBindingManager.addBindingsForTabLevel1( content, Dict.KEY_FILE_SELECTOR_EXP_MPL,  1 );
				keyBindingManager.addBindingsForTabLevel1( content, Dict.KEY_FILE_SELECTOR_EXP_ALDA, 2 );
			}
		}
		
		// set input and action maps
		keyBindingManager.postprocess();
	}
}
