/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.widget;

import java.awt.Component;
import java.awt.Container;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.SortedMap;

import javax.swing.Box.Filler;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.midica.config.Config;
import org.midica.config.Dict;
import org.midica.ui.FileSelector;
import org.midica.ui.model.ComboboxStringOption;
import org.midica.ui.model.ConfigComboboxModel;

/**
 * This class provides a file chooser that allows to choose the charset
 * with a combobox.
 * 
 * @author Jan Trukenmüller
 */
public class MidicaFileChooser extends JFileChooser {
	
	private static final long serialVersionUID = 1L;
	
	// fields for the charset description
	private static final int CHARSET_DESC_CHARS      = 53; // characters per line
	private static final int CHARSET_DESC_PREF_WIDTH =  1; // fake value (ignored by the layout manager)
	
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
	
	/**
	 * Creates a new file chooser defaulting to the given directory.
	 * 
	 * @param type        File type.
	 * @param purpose     **1**: read; **2**: write
	 * @param directory   Default directory.
	 * @param charsetSel  **true**, if the the charset selection combobox shall
	 *                    be shown. Otherwise **false**.
	 */
	public MidicaFileChooser( String type, int purpose, String directory, boolean charsetSel ) {
		super( directory );
		
		this.type    = type;
		this.purpose = purpose;
		
		// insert the charset combobox
		if (charsetSel) {
			insertCharsetSelectionWidgets();
		}
	}
	
	/**
	 * Creates the new widgets. These are:
	 * 
	 * - The area with the charset description.
	 * - The area with the charset label and charset combobox.
	 */
	private void createWidgets() {
		
		// file type specific initializations
		String configKey     = null;
		String defaultCsName = null;
		String descLangKey   = null;
		if ( FileSelector.READ == purpose ) {
    		if ( FileSelector.FILE_TYPE_MIDI.equals(type) ) {
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
			if ( FileSelector.FILE_TYPE_MIDI.equals(type) ) {
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
		String               configuredCsName = Config.get( configKey );
		ComboboxStringOption configuredCs     = null;
		ComboboxStringOption defaultCs        = null;
		
		// create combobox model of all relevant charsets
		ArrayList<ComboboxStringOption> cbxOptions  = new ArrayList<ComboboxStringOption>();
		SortedMap<String, Charset>      allCharsets = Charset.availableCharsets();
		for ( String name : allCharsets.keySet() ) {
			Charset cs = allCharsets.get( name );
			
			// ignore less important charsets
			if ( ! Charset.isSupported(name) )
				continue;
			if ( ! cs.canEncode() )
				continue;
			if ( ! cs.isRegistered() )
				continue;
			
			// add option to model
			ComboboxStringOption option = new ComboboxStringOption( name, name );
			cbxOptions.add( option );
			
			// remember configured and default option for the preselection
			if ( name.equals(configuredCsName) ) {
				configuredCs = option;
			}
			if ( name.equals(defaultCsName) ) {
				defaultCs = option;
			}
		}
		ConfigComboboxModel model = ConfigComboboxModel.initModel( cbxOptions, configKey );
		if ( null == configuredCs ) {
			configuredCs = defaultCs;
		}
		
		// create the description panel
		charsetDescArea = new JPanel();
		charsetDescArea.setLayout( new BoxLayout(charsetDescArea, BoxLayout.X_AXIS) );
		
		// add the spacer and description labels
		lblCharsetDescSpacer = new JLabel("");
		charsetDescArea.add( lblCharsetDescSpacer );
		lblCharsetDesc = new FlowLabel( Dict.get(descLangKey), CHARSET_DESC_CHARS, CHARSET_DESC_PREF_WIDTH );
		lblCharsetDesc.makeFontLookLikeLabel();
		charsetDescArea.add( lblCharsetDesc );
		
		// create the combobox panel
		charsetArea = new JPanel();
		charsetArea.setLayout( new BoxLayout(charsetArea, BoxLayout.X_AXIS) );
		
		// create and add the label
		lblCharset = new JLabel( Dict.get(Dict.CHARSET) + ":" );
		charsetArea.add( lblCharset );
		
		// create and add the combobox
		cbxCharset = new JComboBox<ComboboxStringOption>();
		cbxCharset.setModel( model );
		cbxCharset.setSelectedItem( configuredCs );
		charsetArea.add( cbxCharset );
	}
	
	/**
	 * Creates the charset-related widgets and inserts them into the right
	 * places inside the parent file chooser.
	 * 
	 * This code is really ugly but necessary because {@link JFileChooser}
	 * doesn't provide any other possibility to select a charset.
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
		
		// 2. Get the file type label and combobox.
		JPanel    fileTypeArea = (JPanel)    fileAttrArea.getComponent( 2 );
		JLabel    lblOrig      = (JLabel)    fileTypeArea.getComponent( 0 );
		JComboBox cbxOrig      = (JComboBox) fileTypeArea.getComponent( 1 );
		
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
		ArrayList<Component> componentList = new ArrayList<Component>();
		for ( Component c : components ) {
			componentList.add( c );
			fileAttrArea.remove( c );
		}
		
		// add the elements again, but also add our own fillers and panels
		fileAttrArea.add( componentList.get(0), 0 ); // file name label and text field
		fileAttrArea.add( componentList.get(1), 1 ); // original filler
		fileAttrArea.add( componentList.get(2), 2 ); // file type label and combobox
		fileAttrArea.add( fillerA,              3 ); // cloned filler
		fileAttrArea.add( charsetDescArea,      4 ); // charset description
		fileAttrArea.add( fillerB,              5 ); // cloned filler
		fileAttrArea.add( charsetArea,          6 ); // charset label and combobox
		fileAttrArea.add( componentList.get(3), 7 ); // open and cancel buttons
	}
}
