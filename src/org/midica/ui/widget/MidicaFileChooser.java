/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.widget;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.SortedMap;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.Box.Filler;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.midica.config.Config;
import org.midica.config.Dict;
import org.midica.config.Laf;
import org.midica.ui.file.FileSelector;
import org.midica.ui.model.ComboboxStringOption;
import org.midica.ui.model.ConfigComboboxModel;

/**
 * This class provides a file chooser that allows to choose a file together with
 * a charset.
 * 
 * It can add a charset combobox and a description to the inherited
 * {@link JFileChooser}.
 * 
 * Moreover this class also adjusts the look and feel of buttons for nimbus.
 * 
 * @author Jan Trukenmüller
 */
public class MidicaFileChooser extends JFileChooser {
	
	private static final long serialVersionUID = 1L;
	
	// fields for the charset description / foreign executable description
	private static final int DESC_CHARS       = 60; // characters per line
	private static final int DESC_PREF_WIDTH  =  1; // fake value (ignored by the layout manager)
	
	// make the left elements of our own widgets so many pixels wider than the file name label
	private static final int WIDTH_CORRECTION = 3;
	
	private FileSelector parentWindow = null;
	
	private String  type              = null;
	private byte    purpose           = -1;
	private boolean needCharsetSel    = false;
	private boolean needForeignExe    = false;
	private String  confKeyForeignExe = null;
	private boolean needDCIcon        = false;
	
	// charset selection combobox
	private JComboBox<ComboboxStringOption> cbxCharset = null;
	
	// decompile config icon
	private DecompileConfigIcon decompileConfigIcon = null;
	
	// foreign executable text field
	private JTextField fldForeignExec = null;
	
	/**
	 * Creates a new file chooser defaulting to the given directory.
	 * 
	 * @param type                 File type.
	 * @param purpose              **1**: read; **2**: write
	 * @param directory            Default directory.
	 * @param charsetSel           **true**, if the the charset selection combobox shall
	 *                             be shown. Otherwise **false**.
	 * @param confKeyForeignExe    config key for the foreign executable command or path, if a
	 *                             foreign program is needed. Otherwise: **null**.
	 * @param parent               the parent window (only needed for the MidicaPL exporter)
	 */
	public MidicaFileChooser(String type, byte purpose, String directory, boolean charsetSel,
			String confKeyForeignExe, FileSelector parent) {
		super(directory);
		
		this.type              = type;
		this.purpose           = purpose;
		this.parentWindow      = parent;
		this.needCharsetSel    = charsetSel;
		this.confKeyForeignExe = confKeyForeignExe;
		this.needDCIcon        = FileSelector.WRITE == purpose && ! FileSelector.FILE_TYPE_MIDI.equals(type);
		this.needForeignExe    = confKeyForeignExe != null;
		
		if (Laf.isNimbus)
			changeButtonColors();
		
		// insert the charset combobox and/or the decompile config icon
		if (needCharsetSel || needDCIcon || needForeignExe) {
			insertExtraWidgets();
		}
	}
	
	/**
	 * Returns the file type.
	 * 
	 * @return file type.
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * Returns the charset selection combobox, if available, or otherwise **null**.
	 * 
	 * @return charset selection combobox or **null**
	 */
	public JComboBox<?> getCharsetSelectionCbx() {
		return cbxCharset;
	}
	
	/**
	 * Returns the decompile config icon, if available, or otherwise **null**.
	 * 
	 * @return the icon or **null**
	 */
	public DecompileConfigIcon getDecompileConfigIcon() {
		return decompileConfigIcon;
	}
	
	/**
	 * Returns the text field for the foreign program, if available, or otherwise **null**.
	 * 
	 * @return the field or **null**
	 */
	public JTextField getForeignExecField() {
		return fldForeignExec;
	}
	
	/**
	 * Creates the new widgets, needed for the charset selection. These are:
	 * 
	 * - The area with the charset description.
	 * - The area with the charset label and charset combobox.
	 * 
	 * @param leftElement     file name label (used to copy the dimension)
	 * @param rightElement    file type combobox (used to copy the dimension)
	 * @return the created areas:
	 * 
	 * - area containing the spacer and the charset description label
	 * - area containing the charset combobox and its label
	 */
	private JComponent[] createCharsetAreas(Component leftElement, Component rightElement) {
		
		// file type specific initializations
		String configKey     = null;
		String defaultCsName = null;
		String descLangKey   = null;
		if (FileSelector.READ == purpose) {
    		if (FileSelector.FILE_TYPE_MIDI.equals(type)) {
    			configKey     = Config.CHARSET_MID;
    			defaultCsName = Config.DEFAULT_CHARSET_MID;
    			descLangKey   = Dict.CHARSET_DESC_MID_READ;
    		}
    		else {
    			configKey     = Config.CHARSET_MPL;
    			defaultCsName = Config.DEFAULT_CHARSET_MPL;
    			descLangKey   = Dict.CHARSET_DESC_MPL_READ;
    		}
    	}
		else {
			if (FileSelector.FILE_TYPE_MIDI.equals(type)) {
				configKey     = Config.CHARSET_EXPORT_MID;
    			defaultCsName = Config.DEFAULT_CHARSET_EXPORT_MID;
				descLangKey   = Dict.CHARSET_DESC_MID_WRITE;
			}
    		else {
    			configKey     = Config.CHARSET_EXPORT_MPL;
    			defaultCsName = Config.DEFAULT_CHARSET_EXPORT_MPL;
    			descLangKey   = Dict.CHARSET_DESC_MPL_WRITE;
    		}
		}
		
		// prepare preselection
		String               configuredCsName = Config.get(configKey);
		ComboboxStringOption configuredCs     = null;
		ComboboxStringOption defaultCs        = null;
		
		// create combobox model of all relevant charsets
		ArrayList<ComboboxStringOption> cbxOptions  = new ArrayList<>();
		SortedMap<String, Charset>      allCharsets = Charset.availableCharsets();
		for (String name : allCharsets.keySet()) {
			Charset cs = allCharsets.get(name);
			
			// ignore less important charsets
			if ( ! Charset.isSupported(name) )
				continue;
			if ( ! cs.canEncode() )
				continue;
			if ( ! cs.isRegistered() )
				continue;
			
			// add option to model
			ComboboxStringOption option = new ComboboxStringOption(name, name);
			cbxOptions.add(option);
			
			// remember configured and default option for the preselection
			if (name.equals(configuredCsName)) {
				configuredCs = option;
			}
			if (name.equals(defaultCsName)) {
				defaultCs = option;
			}
		}
		ConfigComboboxModel model = ConfigComboboxModel.initModel(cbxOptions, configKey);
		if (null == configuredCs) {
			configuredCs = defaultCs;
		}
		
		// create the description panel
		JPanel charsetDescArea = new JPanel();
		charsetDescArea.setLayout(new BoxLayout(charsetDescArea, BoxLayout.X_AXIS));
		
		// add the spacer and description labels
		JLabel lblCharsetDescSpacer = new JLabel("");
		charsetDescArea.add(lblCharsetDescSpacer);
		FlowLabel lblCharsetDesc = new FlowLabel(Dict.get(descLangKey), DESC_CHARS, DESC_PREF_WIDTH);
		lblCharsetDesc.makeFontLookLikeLabel();
		charsetDescArea.add(lblCharsetDesc);
		
		// create the combobox panel
		JPanel charsetArea = new JPanel();
		charsetArea.setLayout(new BoxLayout(charsetArea, BoxLayout.X_AXIS));
		
		// create and add the label
		JLabel lblCharset = new JLabel(Dict.get(Dict.CHARSET) + ":");
		charsetArea.add(lblCharset);
		
		// create and add the combobox
		cbxCharset = new JComboBox<>();
		cbxCharset.setModel(model);
		cbxCharset.setSelectedItem(configuredCs);
		charsetArea.add(cbxCharset);
		
		// copy dimensions
		Dimension leftDimCorrected = leftElement.getPreferredSize();
		leftDimCorrected.width += WIDTH_CORRECTION;
		lblCharsetDescSpacer.setMinimumSize(leftElement.getMinimumSize());
		lblCharsetDescSpacer.setMaximumSize(leftElement.getMaximumSize());
		lblCharsetDescSpacer.setPreferredSize(leftDimCorrected);
		lblCharset.setMinimumSize(leftElement.getMinimumSize());
		lblCharset.setMaximumSize(leftElement.getMaximumSize());
		lblCharset.setPreferredSize(leftElement.getPreferredSize());
		lblCharsetDesc.setMinimumSize(rightElement.getMinimumSize());
		lblCharsetDesc.setMaximumSize(rightElement.getMaximumSize());
		lblCharsetDesc.setPreferredSize(rightElement.getPreferredSize());
		cbxCharset.setMinimumSize(rightElement.getMinimumSize());
		cbxCharset.setMaximumSize(rightElement.getMaximumSize());
		cbxCharset.setPreferredSize(rightElement.getPreferredSize());
		
		return new JComponent[] {charsetDescArea, charsetArea};
	}
	
	/**
	 * Creates the widgets, needed to configure the execution command or path of the foreign program.
	 * These are:
	 * 
	 * - The area with the program description.
	 * - The area with the program label and text field.
	 * 
	 * @param leftElement     file name label (used to copy the dimension)
	 * @param rightElement    file type combobox (used to copy the dimension)
	 * @return the created areas:
	 * 
	 * - area containing the spacer and the command/path description label
	 * - area containing the text field and its label
	 */
	private JComponent[] createForeignExeAreas(Component leftElement, Component rightElement) {
		
		// program specific config
		String progName = null;
		if (FileSelector.FILE_TYPE_ALDA.equals(type))
			progName = Dict.get(Dict.FOREIGN_PROG_ALDA);
		else if (FileSelector.FILE_TYPE_ABC.equals(type))
			progName = Dict.get(Dict.FOREIGN_PROG_ABCMIDI);
		else if (FileSelector.FILE_TYPE_LY.equals(type))
			progName = Dict.get(Dict.FOREIGN_PROG_LY);
		else
			progName = "[[TRANSLATION MISSING, PLEASE REPORT THIS BUG]]";
		String progDesc = String.format(Dict.get(Dict.FOREIGN_PROG_DESC), progName);
		
		// create the description panel
		JPanel exeDescArea = new JPanel();
		exeDescArea.setLayout(new BoxLayout(exeDescArea, BoxLayout.X_AXIS));
		
		// add the spacer and description labels
		JLabel lblExeDescSpacer = new JLabel("");
		exeDescArea.add(lblExeDescSpacer);
		FlowLabel lblExeDesc = new FlowLabel(progDesc, DESC_CHARS, DESC_PREF_WIDTH);
		lblExeDesc.makeFontLookLikeLabel();
		exeDescArea.add(lblExeDesc);
		
		// create the combobox panel
		JPanel execArea = new JPanel();
		execArea.setLayout(new BoxLayout(execArea, BoxLayout.X_AXIS));
		
		// create and add the label
		JLabel lblExec = new JLabel(Dict.get(Dict.FOREIGN_PROG) + ":");
		execArea.add(lblExec);
		
		// create and add the combobox
		fldForeignExec = new JTextField(Config.get(confKeyForeignExe));
		execArea.add(fldForeignExec);
		
		// set config on update
		fldForeignExec.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				execPathChanged();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				execPathChanged();
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				execPathChanged();
			}
		});
		
		// copy dimensions
		Dimension leftDimCorrected = leftElement.getPreferredSize();
		leftDimCorrected.width += WIDTH_CORRECTION;
		lblExeDescSpacer.setMinimumSize(leftElement.getMinimumSize());
		lblExeDescSpacer.setMaximumSize(leftElement.getMaximumSize());
		lblExeDescSpacer.setPreferredSize(leftDimCorrected);
		lblExec.setMinimumSize(leftElement.getMinimumSize());
		lblExec.setMaximumSize(leftElement.getMaximumSize());
		lblExec.setPreferredSize(leftElement.getPreferredSize());
		lblExeDesc.setMinimumSize(rightElement.getMinimumSize());
		lblExeDesc.setMaximumSize(rightElement.getMaximumSize());
		lblExeDesc.setPreferredSize(rightElement.getPreferredSize());
		fldForeignExec.setMinimumSize(rightElement.getMinimumSize());
		fldForeignExec.setMaximumSize(rightElement.getMaximumSize());
		fldForeignExec.setPreferredSize(rightElement.getPreferredSize());
		
		return new JComponent[] {exeDescArea, execArea};
	}
	
	/**
	 * Creates widgets for the URL of the foreign program.
	 * 
	 * These are:
	 * 
	 * - the URL label
	 * - the link
	 * 
	 * @param url            the URL to the foreign program
	 * @param leftElement    file name label (used to copy the dimension)
	 * @return the created classes:
	 * 
	 * - the container with the created widgets (URL label and link)
	 */
	private JComponent createForeignUrlArea(String url, Component leftElement) {
		
		// container
		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.X_AXIS));
		
		// label
		JLabel lblUrl = new JLabel(Dict.get(Dict.FOREIGN_URL) + ":");
		content.add(lblUrl);
		
		// url
		LinkLabel lblLink = new LinkLabel(url);
		content.add(lblLink);
		
		// copy dimensions
		Dimension leftDimCorrected = leftElement.getPreferredSize();
		leftDimCorrected.width += WIDTH_CORRECTION;
		lblUrl.setMinimumSize(leftElement.getMinimumSize());
		lblUrl.setMaximumSize(leftElement.getMaximumSize());
		lblUrl.setPreferredSize(leftDimCorrected);
		
		return content;
	}
	
	/**
	 * Creates the decompilation area.
	 * It consists of a spacer on the left side and the icon on the right side.
	 * 
	 * @param leftElement    file name label (used to copy the dimension)
	 * @return the created area
	 */
	private Container createDecompileArea(Component leftElement) {
		
		// copy the dimensions of the "file name" label
		Dimension leftDimCorrected = leftElement.getPreferredSize();
		leftDimCorrected.width += WIDTH_CORRECTION;
		JLabel spacer = new JLabel("");
		spacer.setPreferredSize(leftDimCorrected);
		
		// create the decompilation area
		JPanel decompileArea = new JPanel();
		decompileArea.setLayout(new BoxLayout(decompileArea, BoxLayout.X_AXIS));
		
		// create the decompile config icon
		decompileConfigIcon = new DecompileConfigIcon(parentWindow);
		
		// add elements (spacer, icon, spacer)
		decompileArea.add(spacer);
		decompileArea.add(decompileConfigIcon);
		decompileArea.add(Box.createHorizontalGlue());
		
		return decompileArea;
	}
	
	/**
	 * Creates extra widgets and inserts them into the right
	 * places inside the parent file chooser.
	 * 
	 * The extra widgets are:
	 * 
	 * - charset-related widgets
	 * - decompile configuration icon
	 * - widgets related to a foreign program (command or full path)
	 * - foreign program URL, if available
	 * 
	 * The code in this method is really ugly but necessary because
	 * {@link JFileChooser} doesn't provide any other possibility to
	 * add extra elements.
	 */
	private void insertExtraWidgets() {
		
		// Get the component containing:
		// - file name label and text field  (index 0)
		// - a javax.swing.Box.Filler        (index 1)
		// - file type label and combobox    (index 2)
		// - open and cancel buttons         (index 3)
		Container fileAttrArea = (Container) getComponent( 3 );
		
		// From this component:
		
		// Step 1: Take over the filler dimensions.
		Filler f       = (Filler) fileAttrArea.getComponent( 1 );
		Filler fillerA = new Filler( f.getMinimumSize(), f.getPreferredSize(), f.getMaximumSize() );
		Filler fillerB = new Filler( f.getMinimumSize(), f.getPreferredSize(), f.getMaximumSize() );
		Filler fillerC = new Filler( f.getMinimumSize(), f.getPreferredSize(), f.getMaximumSize() );
		Filler fillerD = new Filler( f.getMinimumSize(), f.getPreferredSize(), f.getMaximumSize() );
		Filler fillerE = new Filler( f.getMinimumSize(), f.getPreferredSize(), f.getMaximumSize() );
		Filler fillerF = new Filler( f.getMinimumSize(), f.getPreferredSize(), f.getMaximumSize() );
		
		// Step 2: Get the file type label (left) and combobox (right).
		//         So we can copy dimensions from them later.
		JPanel       fileTypeArea  = (JPanel)       fileAttrArea.getComponent( 2 );
		JLabel       leftElemOrig  = (JLabel)       fileTypeArea.getComponent( 0 );
		JComboBox<?> rightElemOrig = (JComboBox<?>) fileTypeArea.getComponent( 1 );
		
		// Step 3: Remember and remove all elements
		Component[]          components    = fileAttrArea.getComponents();
		ArrayList<Component> componentList = new ArrayList<>();
		for (Component c : components) {
			componentList.add(c);
			fileAttrArea.remove(c);
		}
		
		// Step 4: Add the elements again, but also add our own fillers and panels
		fileAttrArea.add( componentList.get(0) ); // file name label and text field
		fileAttrArea.add( componentList.get(1) ); // original filler
		fileAttrArea.add( componentList.get(2) ); // file type label and combobox
		if (needCharsetSel) {
			
			// add charset widgets, if needed (and copy the dimensions from the original elements)
			JComponent[] charsetAreas = createCharsetAreas(leftElemOrig, rightElemOrig);
			
			fileAttrArea.add( fillerA         ); // cloned filler
			fileAttrArea.add( charsetAreas[0] ); // charset description
			fileAttrArea.add( fillerB         ); // cloned filler
			fileAttrArea.add( charsetAreas[1] ); // charset label and combobox
		}
		if (needForeignExe) {
			
			// create widgets to change the foreign executable path (and copy the dimensions from the original elements)
			JComponent[] foreignExeAreas = createForeignExeAreas(leftElemOrig, rightElemOrig);
			
			fileAttrArea.add( fillerC            ); // cloned filler
			fileAttrArea.add( foreignExeAreas[0] ); // program description
			fileAttrArea.add( fillerD            ); // cloned filler
			fileAttrArea.add( foreignExeAreas[1] ); // program label and text field
			
			String url = Dict.getForeignProgramUrl(confKeyForeignExe);
			if (url != null) {
				
				// create widgets to change the foreign executable path (and copy the dimensions from the original elements)
				JComponent foreignUrlArea = createForeignUrlArea(url, leftElemOrig);
				
				fileAttrArea.add( fillerE        ); // cloned filler
				fileAttrArea.add( foreignUrlArea ); // program url and label
			}
		}
		if (needDCIcon) {
			Container decompileArea = createDecompileArea(leftElemOrig);
			fileAttrArea.add( fillerF       ); // cloned filler
			fileAttrArea.add( decompileArea ); // decompile config icon
		}
		fileAttrArea.add( componentList.get(3) ); // open and cancel buttons
	}
	
	/**
	 * Adjusts the colors of buttons for the nimbus look and feel.
	 * 
	 * The buttons to be adjusted are:
	 * 
	 * - the buttons and toggle buttons with icons in the top right corner
	 * - the open and close buttons
	 * 
	 * The buttons with icons are adjusted directly.
	 * 
	 * The open and close buttons are replaced by custom buttons with the same functionality.
	 * That's necessary because directly applying the changes is somehow not enough to
	 * make them look like other buttons in the application.
	 */
	private void changeButtonColors() {
		
		// Get the component containing the icon buttons
		Container firstArea      = (Container) getComponent( 0 );
		Container iconButtonArea = (Container) firstArea.getComponent( 0 );
		
		// from this container, change all buttons and toggle buttons
		for (Component c : iconButtonArea.getComponents()) {
			if (c instanceof AbstractButton) {
				AbstractButton button = (AbstractButton) c;
				Laf.applyLafToButton(button, false);
			}
		}
		
		// Get the component containing:
		// - file name label and text field  (index 0)
		// - a javax.swing.Box.Filler        (index 1)
		// - file type label and combobox    (index 2)
		// - open and cancel buttons         (index 3)
		Container fileAttrArea = (Container) getComponent( 3 );
		
		// From this component:
		// get the button container
		Container buttonArea = (Container) fileAttrArea.getComponent( 3 );
		
		// get the buttons
		JButton openButton  = (JButton) buttonArea.getComponent( 0 );
		JButton closeButton = (JButton) buttonArea.getComponent( 1 );
		openButton.setContentAreaFilled(true);
		
		// create new buttons
		MidicaButton newOpenButton = new MidicaButton(openButton.getText(), true);
		MidicaButton newCloseButton = new MidicaButton(closeButton.getText());
		transferButtonProperties(openButton, newOpenButton);
		transferButtonProperties(closeButton, newCloseButton);
		
		// remove old buttons
		buttonArea.remove(openButton);
		buttonArea.remove(closeButton);
		
		// add new buttons
		buttonArea.add(newOpenButton);
		buttonArea.add(newCloseButton);
	}
	
	/**
	 * Transfers properties from a default file chooser button to a self-created button with
	 * different look and feel.
	 * 
	 * @param source  the default button.
	 * @param target  the new button.
	 */
	private void transferButtonProperties(JButton source, MidicaButton target) {
		
		// tooltips and action commands
		target.setToolTipText(source.getToolTipText());
		target.setActionCommand(source.getActionCommand());
		
		// transfer action listeners
		ActionListener[] listeners = source.getActionListeners();
		for (ActionListener listener : listeners) {
			target.addActionListener(listener);
			source.removeActionListener(listener);
		}
	}
	
	/**
	 * Handles changes in the text field for the executable path.
	 * 
	 * Updates the config accordingly.
	 */
	private void execPathChanged() {
		String path = fldForeignExec.getText();
		if (FileSelector.FILE_TYPE_ALDA.equals(type)) {
			Config.set(Config.EXEC_PATH_IMP_ALDA, path);
		}
		else if (FileSelector.FILE_TYPE_ABC.equals(type)) {
			Config.set(Config.EXEC_PATH_IMP_ABC, path);
		}
		else if (FileSelector.FILE_TYPE_LY.equals(type)) {
			Config.set(Config.EXEC_PATH_IMP_LY, path);
		}
	}
}
