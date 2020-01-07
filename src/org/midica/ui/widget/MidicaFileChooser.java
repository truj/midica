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
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

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
	
	// fields for the charset description
	private static final int CHARSET_DESC_CHARS      = 53; // characters per line
	private static final int CHARSET_DESC_PREF_WIDTH =  1; // fake value (ignored by the layout manager)
	
	private FileSelector parentWindow = null;
	
	private String type    = null;
	private int    purpose = -1;
	
	// widgets for the charset description line
	private JLabel    lblCharsetDescSpacer = null;
	private FlowLabel lblCharsetDesc       = null;
	private JPanel    charsetDescArea      = null;
	
	// widgets for the charset combobox line
	private JLabel                          lblCharset  = null;
	private JComboBox<ComboboxStringOption> cbxCharset  = null;
	private JPanel                          charsetArea = null;
	
	// decompile config icon
	DecompileConfigIcon decompileConfigIcon = null;
	
	/**
	 * Creates a new file chooser defaulting to the given directory.
	 * 
	 * @param type        File type.
	 * @param purpose     **1**: read; **2**: write
	 * @param directory   Default directory.
	 * @param charsetSel  **true**, if the the charset selection combobox shall
	 *                    be shown. Otherwise **false**.
	 * @param parent      the parent window (only needed for the MidicaPL exporter)
	 */
	public MidicaFileChooser(String type, int purpose, String directory, boolean charsetSel, FileSelector parent) {
		super(directory);
		
		this.type         = type;
		this.purpose      = purpose;
		this.parentWindow = parent;
		
		if (Laf.isNimbus)
			changeButtonColors();
		
		// insert the charset combobox
		if (charsetSel) {
			insertCharsetSelectionWidgets();
		}
	}
	
	/**
	 * Returns the requested component, if available, or otherwise **null**.
	 * 
	 * @param id  key binding ID
	 * @return the component or **null**
	 */
	public JComponent getWidgetByKeyBindingId(String id) {
		if (Dict.KEY_FILE_SELECT_DC_OPEN.equals(id))
			return decompileConfigIcon;
		
		// fallback
		return null;
	}
	
	/**
	 * Creates the new widgets. These are:
	 * 
	 * - The area with the charset description.
	 * - The area with the charset label and charset combobox.
	 * - The button to open the decompile config window (only for the MPL export file chooser)
	 */
	private void createWidgets() {
		
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
			cbxOptions.add( option );
			
			// remember configured and default option for the preselection
			if ( name.equals(configuredCsName) ) {
				configuredCs = option;
			}
			if ( name.equals(defaultCsName) ) {
				defaultCs = option;
			}
		}
		ConfigComboboxModel model = ConfigComboboxModel.initModel(cbxOptions, configKey);
		if (null == configuredCs) {
			configuredCs = defaultCs;
		}
		
		// create the description panel
		charsetDescArea = new JPanel();
		charsetDescArea.setLayout( new BoxLayout(charsetDescArea, BoxLayout.X_AXIS) );
		
		// add the spacer and description labels
		lblCharsetDescSpacer = new JLabel("");
		charsetDescArea.add(lblCharsetDescSpacer);
		lblCharsetDesc = new FlowLabel( Dict.get(descLangKey), CHARSET_DESC_CHARS, CHARSET_DESC_PREF_WIDTH );
		lblCharsetDesc.makeFontLookLikeLabel();
		charsetDescArea.add(lblCharsetDesc);
		
		// create the combobox panel
		charsetArea = new JPanel();
		charsetArea.setLayout( new BoxLayout(charsetArea, BoxLayout.X_AXIS) );
		
		// create and add the label
		lblCharset = new JLabel( Dict.get(Dict.CHARSET) + ":" );
		charsetArea.add(lblCharset);
		
		// create and add the combobox
		cbxCharset = new JComboBox<>();
		cbxCharset.setModel(model);
		cbxCharset.setSelectedItem(configuredCs);
		charsetArea.add(cbxCharset);
		
		// create the decompile config button
		if (FileSelector.WRITE == purpose && FileSelector.FILE_TYPE_MPL.equals(type)) {
			decompileConfigIcon = new DecompileConfigIcon(parentWindow);
		}
	}
	
	/**
	 * Creates the charset-related widgets and inserts them into the right
	 * places inside the parent file chooser.
	 * 
	 * The code in this method is really ugly but necessary because
	 * {@link JFileChooser} doesn't provide any other possibility to
	 * select a charset.
	 */
	private void insertCharsetSelectionWidgets() {
		
		// create label and combobox for the charset selection and put them
		// into the newly created charsetArea panel
		createWidgets();
		
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
		
		// 2. Get the file type label and combobox.
		JPanel       fileTypeArea = (JPanel)       fileAttrArea.getComponent( 2 );
		JLabel       lblOrig      = (JLabel)       fileTypeArea.getComponent( 0 );
		JComboBox<?> cbxOrig      = (JComboBox<?>) fileTypeArea.getComponent( 1 );
		
		// 3. Take over the label dimensions.
		lblCharsetDescSpacer.setMinimumSize( lblOrig.getMinimumSize() );
		lblCharsetDescSpacer.setMaximumSize( lblOrig.getMaximumSize() );
		lblCharsetDescSpacer.setPreferredSize( lblOrig.getPreferredSize() );
		lblCharset.setMinimumSize( lblOrig.getMinimumSize() );
		lblCharset.setMaximumSize( lblOrig.getMaximumSize() );
		lblCharset.setPreferredSize( lblOrig.getPreferredSize() );
		
		// 4. Take over the combobox dimensions.
		cbxCharset.setMinimumSize( cbxOrig.getMinimumSize() );
		cbxCharset.setMaximumSize( cbxOrig.getMaximumSize() );
		cbxCharset.setPreferredSize( cbxOrig.getPreferredSize() );
		
		// remember and remove all elements
		Component[]          components    = fileAttrArea.getComponents();
		ArrayList<Component> componentList = new ArrayList<>();
		for (Component c : components) {
			componentList.add(c);
			fileAttrArea.remove(c);
		}
		
		// decompile config icon - add it to the button area
		Container decompileArea = null;
		if (FileSelector.WRITE == purpose && FileSelector.FILE_TYPE_MPL.equals(type)) {
			
			// copy the dimensions of the "file name" label
			Container fileArea = (Container) componentList.get(0);      // file name label + text field
			Component lbl      = (Component) fileArea.getComponent(0);  // file name label
			Dimension lblDim   = lbl.getPreferredSize();
			lblDim.width += 3;
			JLabel spacer = new JLabel("");
			spacer.setPreferredSize(lblDim);
			
			// create the decompilation area
			decompileArea = new JPanel();
			decompileArea.setLayout( new BoxLayout(decompileArea, BoxLayout.X_AXIS) );
			
			// add elements (spacer, icon, spacer)
			decompileArea.add(spacer);
			decompileArea.add(decompileConfigIcon);
			decompileArea.add(Box.createHorizontalGlue());
		}
		
		// add the elements again, but also add our own fillers and panels
		fileAttrArea.add( componentList.get(0) ); // file name label and text field
		fileAttrArea.add( componentList.get(1) ); // original filler
		fileAttrArea.add( componentList.get(2) ); // file type label and combobox
		fileAttrArea.add( fillerA              ); // cloned filler
		fileAttrArea.add( charsetDescArea      ); // charset description
		fileAttrArea.add( fillerB              ); // cloned filler
		fileAttrArea.add( charsetArea          ); // charset label and combobox
		if (FileSelector.WRITE == purpose && FileSelector.FILE_TYPE_MPL.equals(type)) {
			fileAttrArea.add( fillerC       ); // cloned filler
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
		for ( Component c : iconButtonArea.getComponents() ) {
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
}
