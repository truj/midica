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
	private static final int DESC_CHARS      = 53; // characters per line
	private static final int DESC_PREF_WIDTH =  1; // fake value (ignored by the layout manager)
	
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
	 * @return the created areas:
	 * 
	 * - spacer left from the charset description label
	 * - charset label left from the combobox
	 * - area containing the spacer and the charset description label
	 * - area containing the charset combobox and its label
	 */
	private JComponent[] createCharsetWidgets() {
		
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
		
		return new JComponent[] {lblCharsetDescSpacer, lblCharset, charsetDescArea, charsetArea};
	}
	
	/**
	 * Creates the widgets, needed to configure the execution command or path of the foreign program.
	 * These are:
	 * 
	 * - The area with the program description.
	 * - The area with the program label and text field.
	 * 
	 *@return the created areas:
	 * 
	 * - spacer left from the command/path description label
	 * - program label left from the text field
	 * - area containing the spacer and the command/path description label
	 * - area containing the text field and its label
	 */
	private JComponent[] createForeignExeWidgets() {
		
		// program specific config
		String progName = null;
		if (FileSelector.FILE_TYPE_ALDA.equals(type)) {
			progName = Dict.get(Dict.FOREIGN_PROG_ALDA);
		}
		else if (FileSelector.FILE_TYPE_ALDA.equals(type)) {
			progName = Dict.get(Dict.FOREIGN_PROG_MSCORE);
		}
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
		
		return new JComponent[] {lblExeDescSpacer, lblExec, exeDescArea, execArea};
	}
	
	/**
	 * Creates the decompilation area.
	 * It consists of a spacer on the left side and the icon on the right side.
	 * 
	 * @param fileArea    container containing file label and text field
	 * @return the created area
	 */
	private Container createDecompileArea(Container fileArea) {
		
		// copy the dimensions of the "file name" label
		Component lbl      = (Component) fileArea.getComponent(0);  // file name label
		Dimension lblDim   = lbl.getPreferredSize();
		lblDim.width += 3;
		JLabel spacer = new JLabel("");
		spacer.setPreferredSize(lblDim);
		
		// create the decompilation area
		JPanel decompileArea = new JPanel();
		decompileArea.setLayout(new BoxLayout(decompileArea, BoxLayout.X_AXIS));
		
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
	 * 
	 * The code in this method is really ugly but necessary because
	 * {@link JFileChooser} doesn't provide any other possibility to
	 * select a charset.
	 */
	private void insertExtraWidgets() {
		
		// create decompile icon, if needed
		if (needDCIcon) {
			decompileConfigIcon = new DecompileConfigIcon(parentWindow);
		}
		
		// Get the component containing:
		// - file name label and text field  (index 0)
		// - a javax.swing.Box.Filler        (index 1)
		// - file type label and combobox    (index 2)
		// - open and cancel buttons         (index 3)
		Container fileAttrArea = (Container) getComponent( 3 );
		
		// From this component:
		
		// - 1. Take over the filler dimensions.
		Filler f       = (Filler) fileAttrArea.getComponent( 1 );
		Filler fillerA = new Filler( f.getMinimumSize(), f.getPreferredSize(), f.getMaximumSize() );
		Filler fillerB = new Filler( f.getMinimumSize(), f.getPreferredSize(), f.getMaximumSize() );
		Filler fillerC = new Filler( f.getMinimumSize(), f.getPreferredSize(), f.getMaximumSize() );
		Filler fillerD = new Filler( f.getMinimumSize(), f.getPreferredSize(), f.getMaximumSize() );
		Filler fillerE = new Filler( f.getMinimumSize(), f.getPreferredSize(), f.getMaximumSize() );
		
		// 2. Get the file type label and combobox.
		JPanel       fileTypeArea = (JPanel)       fileAttrArea.getComponent( 2 );
		JLabel       lblOrig      = (JLabel)       fileTypeArea.getComponent( 0 );
		JComboBox<?> cbxOrig      = (JComboBox<?>) fileTypeArea.getComponent( 1 );
		
		// add charset widgets, if needed
		JComponent[] charsetWidgets = null;
		if (needCharsetSel) {
			
			// create charset widgets
			charsetWidgets = createCharsetWidgets();
			
			// 3. Take over the label dimensions.
			charsetWidgets[0].setMinimumSize( lblOrig.getMinimumSize() ); // spacer left from the charset description
			charsetWidgets[0].setMaximumSize( lblOrig.getMaximumSize() );
			charsetWidgets[0].setPreferredSize( lblOrig.getPreferredSize() );
			charsetWidgets[1].setMinimumSize( lblOrig.getMinimumSize() ); // charset label
			charsetWidgets[1].setMaximumSize( lblOrig.getMaximumSize() );
			charsetWidgets[1].setPreferredSize( lblOrig.getPreferredSize() );
			
			// 4. Take over the combobox dimensions.
			cbxCharset.setMinimumSize( cbxOrig.getMinimumSize() );
			cbxCharset.setMaximumSize( cbxOrig.getMaximumSize() );
			cbxCharset.setPreferredSize( cbxOrig.getPreferredSize() );
		}
		
		// create widgets to change the foreign executable path
		JComponent[] foreignExeWidgets = null;
		if (needForeignExe) {
			
			// create foreign executable widgets
			foreignExeWidgets = createForeignExeWidgets();
			
			// 3. Take over the label dimensions.
			foreignExeWidgets[0].setMinimumSize( lblOrig.getMinimumSize() ); // spacer left from the exec description
			foreignExeWidgets[0].setMaximumSize( lblOrig.getMaximumSize() );
			foreignExeWidgets[0].setPreferredSize( lblOrig.getPreferredSize() );
			foreignExeWidgets[1].setMinimumSize( lblOrig.getMinimumSize() ); // exec label
			foreignExeWidgets[1].setMaximumSize( lblOrig.getMaximumSize() );
			foreignExeWidgets[1].setPreferredSize( lblOrig.getPreferredSize() );
			
			// 4. Take over the textfield dimensions.
			fldForeignExec.setMinimumSize( cbxOrig.getMinimumSize() );
			fldForeignExec.setMaximumSize( cbxOrig.getMaximumSize() );
			fldForeignExec.setPreferredSize( cbxOrig.getPreferredSize() );
		}
		
		// remember and remove all elements
		Component[]          components    = fileAttrArea.getComponents();
		ArrayList<Component> componentList = new ArrayList<>();
		for (Component c : components) {
			componentList.add(c);
			fileAttrArea.remove(c);
		}
		
		// decompile config icon - add it to the button area
		Container decompileArea = null;
		if (needDCIcon) {
			// componentList.get(0) == file name label + text field
			decompileArea = createDecompileArea((Container) componentList.get(0));
		}
		
		// add the elements again, but also add our own fillers and panels
		fileAttrArea.add( componentList.get(0) ); // file name label and text field
		fileAttrArea.add( componentList.get(1) ); // original filler
		fileAttrArea.add( componentList.get(2) ); // file type label and combobox
		if (needCharsetSel) {
			fileAttrArea.add( fillerA           ); // cloned filler
			fileAttrArea.add( charsetWidgets[2] ); // charset description
			fileAttrArea.add( fillerB           ); // cloned filler
			fileAttrArea.add( charsetWidgets[3] ); // charset label and combobox
		}
		if (needForeignExe) {
			fileAttrArea.add( fillerC              ); // cloned filler
			fileAttrArea.add( foreignExeWidgets[2] ); // charset description
			fileAttrArea.add( fillerD              ); // cloned filler
			fileAttrArea.add( foreignExeWidgets[3] ); // charset label and combobox
		}
		if (needDCIcon) {
			fileAttrArea.add( fillerE       ); // cloned filler
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
		Config.set(Config.EXEC_PATH_IMP_ALDA, path);
	}
}
