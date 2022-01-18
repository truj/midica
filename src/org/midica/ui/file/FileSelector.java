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
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.midica.config.Config;
import org.midica.config.Dict;
import org.midica.config.KeyBindingManager;
import org.midica.ui.UiController;
import org.midica.ui.UiView;
import org.midica.ui.widget.MidicaButton;
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
	public static final String FILE_TYPE_MPL        = "mpl";
	public static final String FILE_TYPE_ALDA       = "alda";
	public static final String FILE_TYPE_AUDIO      = "audio";
	public static final String FILE_TYPE_ABC        = "abc";
	public static final String FILE_TYPE_MIDI       = "midi";
	public static final String FILE_TYPE_LY         = "ly";
	public static final String FILE_TYPE_MSCORE_IMP = "mscore_import";
	public static final String FILE_TYPE_MSCORE_EXP = "mscore_export";
	public static final String FILE_TYPE_SOUND_FILE = "sound_file";
	public static final String FILE_TYPE_SOUND_URL  = "sound_url";
	public static final byte   READ                 = 1;
	public static final byte   WRITE                = 2;
	
	private String  fileType;
	private byte    filePurpose;
	private boolean isSound;
	
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
		this.isSound      = false;
		this.fileChoosers = new ArrayList<>();
		this.tabs         = new HashMap<>();
		
		String  directory     = "";
		boolean charSetSelect = false;
		if (READ == this.filePurpose) {
			if (type.equals(FILE_TYPE_SOUND_FILE)) {
				this.isSound  = true;
    			directory     = Config.get(Config.DIRECTORY_SB);
    			charSetSelect = false;
    			tabs.put(type, Dict.TAB_SOUND_FILE);
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
		if (isSound) {
			fileChoosers.add(null); // placeholder for the URL form
		}
		else if (READ == this.filePurpose) {
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
			
			fileChoosers.add(new MidicaFileChooser(
				FILE_TYPE_ABC,
				filePurpose,
				Config.get(Config.DIRECTORY_ABC),
				false,
				Config.EXEC_PATH_IMP_ABC,
				this
			));
			tabs.put(FILE_TYPE_ABC, Dict.TAB_ABC);
			
			fileChoosers.add(new MidicaFileChooser(
				FILE_TYPE_LY,
				filePurpose,
				Config.get(Config.DIRECTORY_LY),
				false,
				Config.EXEC_PATH_IMP_LY,
				this
			));
			tabs.put(FILE_TYPE_LY, Dict.TAB_LY);
			
			fileChoosers.add(new MidicaFileChooser(
				FILE_TYPE_MSCORE_IMP,
				filePurpose,
				Config.get(Config.DIRECTORY_MSCORE),
				false,
				Config.EXEC_PATH_IMP_MSCORE,
				this
			));
			tabs.put(FILE_TYPE_MSCORE_IMP, Dict.TAB_MSCORE);
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
			
			fileChoosers.add(new MidicaFileChooser(
					FILE_TYPE_AUDIO,
					filePurpose,
					Config.get(Config.DIRECTORY_EXPORT_AUDIO),
					false,
					null,
					this
				));
				tabs.put(FILE_TYPE_AUDIO, Dict.TAB_AUDIO);
				
			fileChoosers.add(new MidicaFileChooser(
				FILE_TYPE_ABC,
				filePurpose,
				Config.get(Config.DIRECTORY_EXPORT_ABC),
				false,
				Config.EXEC_PATH_EXP_ABC,
				this
			));
			tabs.put(FILE_TYPE_ABC, Dict.TAB_ABC);
			
			fileChoosers.add(new MidicaFileChooser(
				FILE_TYPE_LY,
				filePurpose,
				Config.get(Config.DIRECTORY_EXPORT_LY),
				false,
				Config.EXEC_PATH_EXP_LY,
				this
			));
			tabs.put(FILE_TYPE_LY, Dict.TAB_LY);
			
			fileChoosers.add(new MidicaFileChooser(
				FILE_TYPE_MSCORE_EXP,
				filePurpose,
				Config.get(Config.DIRECTORY_EXPORT_MSCORE),
				false,
				Config.EXEC_PATH_EXP_MSCORE,
				this
			));
			tabs.put(FILE_TYPE_MSCORE_EXP, Dict.TAB_MSCORE);
		}
		
		// complete file choosers
		for (MidicaFileChooser chooser : fileChoosers) {
			if (null == chooser)
				continue;
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
					if (null == chooser)
						continue;
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
	 * Returns **true** if the currently selected file chooser has a checked 'direct import' checkbox.
	 * Returns **false** if no such checkbox is available in the file chooser, of if the checkbox is
	 * not checked.
	 * 
	 * @return **true** if the exported file should be imported directly, otherwise **false**.
	 */
	public boolean mustDirectlyImport() {
		int index = content.getSelectedIndex();
		return fileChoosers.get(index).mustDirectlyImport();
	}
	
	/**
	 * Initializes the user interface.
	 */
	private void initUI() {
		
		content = new JTabbedPane(JTabbedPane.LEFT);
		add(content);
		
		// add file choosers (and url chooser)
		for (MidicaFileChooser chooser : fileChoosers) {
			
			// URL chooser
			if (null == chooser) {
				String tabString = Dict.get(Dict.TAB_SOUND_URL);
				JComponent urlForm = SoundUrlHelper.createUrlForm(controller, this);
				content.addTab(tabString, urlForm);
			}
			
			// file chooser
			else {
				String tabString = Dict.get(tabs.get(chooser.getType()));
				content.addTab(tabString, chooser);
			}
		}
		
		// add listener to change the file type according to the chosen tab
		content.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int index = content.getSelectedIndex();
				if (fileChoosers.get(index) != null) {
					fileType = fileChoosers.get(index).getType();
				}
			}
		});
		
		// get configured index
		int index;
		try {
			if (READ == this.filePurpose)
				if (isSound)
					index = Integer.parseInt(Config.get(Config.TAB_FILE_SOUND));
				else
					index = Integer.parseInt(Config.get(Config.TAB_FILE_IMPORT));
			else
				index = Integer.parseInt(Config.get(Config.TAB_FILE_EXPORT));
		}
		catch (NumberFormatException e) {
			index = 0;
		}
		
		// set configured index
		try {
			content.setSelectedIndex(index);
		}
		catch (IndexOutOfBoundsException e) {
		}
		
		// remember tab changes
		content.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int index = content.getSelectedIndex();
				if (READ == filePurpose)
					if (isSound)
						Config.set(Config.TAB_FILE_SOUND, index + "");
					else
						Config.set(Config.TAB_FILE_IMPORT, index + "");
				else
					Config.set(Config.TAB_FILE_EXPORT, index + "");
			}
		});
		
		// set configured sound url
		if (isSound) {
			SoundUrlHelper.setUrl(Config.get(Config.SOUND_URL));
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
			if (null == chooser)
				continue;
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
				else if (FILE_TYPE_ABC.equals(fileType))
					Config.set(Config.DIRECTORY_ABC, directory);
				else if (FILE_TYPE_LY.equals(fileType))
					Config.set(Config.DIRECTORY_LY, directory);
				else if (FILE_TYPE_MSCORE_IMP.equals(fileType))
					Config.set(Config.DIRECTORY_MSCORE, directory);
    			else if (FILE_TYPE_SOUND_FILE.equals(fileType))
    				Config.set(Config.DIRECTORY_SB, directory);
    		}
			else {
				if (FILE_TYPE_MIDI.equals(fileType))
    				Config.set(Config.DIRECTORY_EXPORT_MID, directory);
				else if (FILE_TYPE_MPL.equals(fileType))
					Config.set(Config.DIRECTORY_EXPORT_MPL, directory);
				else if (FILE_TYPE_ALDA.equals(fileType))
					Config.set(Config.DIRECTORY_EXPORT_ALDA, directory);
				else if (FILE_TYPE_AUDIO.equals(fileType))
					Config.set(Config.DIRECTORY_EXPORT_AUDIO, directory);
				else if (FILE_TYPE_ABC.equals(fileType))
					Config.set(Config.DIRECTORY_EXPORT_ABC, directory);
				else if (FILE_TYPE_LY.equals(fileType))
					Config.set(Config.DIRECTORY_EXPORT_LY, directory);
				else if (FILE_TYPE_MSCORE_EXP.equals(fileType))
					Config.set(Config.DIRECTORY_EXPORT_MSCORE, directory);
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
			if (null == chooser)
				continue;
			JComboBox<?> cbx  = chooser.getCharsetSelectionCbx();
			JComponent   fld  = chooser.getForeignExecField();
			JComponent   icon = chooser.getConfigIcon();
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
			keyBindingManager.addBindingsForIconLabelOfVisibleTab(icons, Dict.KEY_FILE_SELECT_CONFIG_OPEN);
		
		// sound url specific bindings
		if (isSound) {
			ArrayList<JTextField> urlFields = new ArrayList<>();
			urlFields.add(SoundUrlHelper.getUrlField());
			keyBindingManager.addBindingsForFocusOfVisibleElement(urlFields, Dict.KEY_FILE_SELECTOR_SND_URL_FLD);
			ArrayList<MidicaButton> buttons = new ArrayList<>();
			buttons.add(SoundUrlHelper.getDownloadButton());
			keyBindingManager.addBindingsForButtonOfVisibleElement(buttons, Dict.KEY_FILE_SELECTOR_SND_DOWNLOAD);
		}
		
		// add key binding to choose a tab
		if (content != null) {
			if (READ == this.filePurpose) {
				if (isSound) {
					keyBindingManager.addBindingsForTabLevel1( content, Dict.KEY_FILE_SELECTOR_SND_FILE, 0 );
					keyBindingManager.addBindingsForTabLevel1( content, Dict.KEY_FILE_SELECTOR_SND_URL,  1 );
				}
				else {
					keyBindingManager.addBindingsForTabLevel1( content, Dict.KEY_FILE_SELECTOR_IMP_MPL,    0 );
					keyBindingManager.addBindingsForTabLevel1( content, Dict.KEY_FILE_SELECTOR_IMP_MID,    1 );
					keyBindingManager.addBindingsForTabLevel1( content, Dict.KEY_FILE_SELECTOR_IMP_ALDA,   2 );
					keyBindingManager.addBindingsForTabLevel1( content, Dict.KEY_FILE_SELECTOR_IMP_ABC,    3 );
					keyBindingManager.addBindingsForTabLevel1( content, Dict.KEY_FILE_SELECTOR_IMP_LY,     4 );
					keyBindingManager.addBindingsForTabLevel1( content, Dict.KEY_FILE_SELECTOR_IMP_MSCORE, 5 );
				}
			}
			else {
				keyBindingManager.addBindingsForTabLevel1( content, Dict.KEY_FILE_SELECTOR_EXP_MID,    0 );
				keyBindingManager.addBindingsForTabLevel1( content, Dict.KEY_FILE_SELECTOR_EXP_MPL,    1 );
				keyBindingManager.addBindingsForTabLevel1( content, Dict.KEY_FILE_SELECTOR_EXP_ALDA,   2 );
				keyBindingManager.addBindingsForTabLevel1( content, Dict.KEY_FILE_SELECTOR_EXP_AUDIO,  3 );
				keyBindingManager.addBindingsForTabLevel1( content, Dict.KEY_FILE_SELECTOR_EXP_ABC,    4 );
				keyBindingManager.addBindingsForTabLevel1( content, Dict.KEY_FILE_SELECTOR_EXP_LY,     5 );
				keyBindingManager.addBindingsForTabLevel1( content, Dict.KEY_FILE_SELECTOR_EXP_MSCORE, 6 );
			}
		}
		
		// set input and action maps
		keyBindingManager.postprocess();
	}
}
