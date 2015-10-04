/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.model;

import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.DefaultComboBoxModel;

import org.midica.config.Config;
import org.midica.config.Dict;


/**
 * This class represents the data model of configuration comboboxes.
 * 
 * It also stores each instance of such a model and guarantees that there
 * will be only one instance of each configuration type.
 * 
 * @author Jan Trukenmüller
 */
public class ConfigComboboxModel extends DefaultComboBoxModel {
	
	private static HashMap<String, ConfigComboboxModel> models = new HashMap<String, ConfigComboboxModel>();
	
	/**
	 * The constructor is private to prevent the creation of more than one instance of
	 * the same configuration type.
	 * It can be called only indirectly via {@link #initModel(ArrayList, String)}.
	 */
	private ConfigComboboxModel() {
	}
	
	/**
	 * Adds each of the given options to the combobox model referred to by the given type.
	 * 
	 * @param options    List of all options to be added.
	 * @param type       Configuration type.
	 */
	private void fill( ArrayList<ComboboxStringOption> options, String type ) {
		for ( ComboboxStringOption option : options ) {
			super.addElement( option );
		}
		String configuredOption = Config.get( type );
		ComboboxStringOption option = ComboboxStringOption.getOptionById( configuredOption, options );
		super.setSelectedItem( option );
	}
	
	/**
	 * Returns the combobox model according to the given configuration type.
	 * 
	 * @param type    Configuration type.
	 * @return    Data model of the combobox.
	 */
	public static ConfigComboboxModel getModel( String type ) {
		return models.get( type );
	}
	
	/**
	 * Creates the {@link ConfigComboboxModel} associated with the given **type**, if not yet
	 * done, and returns it.
	 * 
	 * @param options    List of combobox options for the model to be filled with.
	 * @param type       Configuration type of the combobox.
	 * @return    Configuration combobox data model.
	 */
	public static synchronized ConfigComboboxModel initModel( ArrayList<ComboboxStringOption> options, String type ) {
		ConfigComboboxModel model = models.get( type );
		if ( null == model ) {
			model = new ConfigComboboxModel();
			model.fill( options, type );
			models.put( type, model );
		}
		return model;
	}
	
	/**
	 * Resets all language-dependent display texts of the {@link ConfigComboboxModel}
	 * with the given configuration type.
	 * 
	 * This is called if the language has been changed.
	 * 
	 * @param type    Configuration type of the combobox model to be refilled.
	 */
	public static synchronized void refill( String type ) {
		
		ConfigComboboxModel model = models.get( type );
		int count = model.getSize();
		
		for ( int i = 0; i < count; i++ ) {
			ComboboxStringOption option = (ComboboxStringOption) model.getElementAt( i );
			option.setText( Dict.get(option.getIdentifier()) );
		}
	}
}
