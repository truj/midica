/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.file.config;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.ComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.midica.config.Config;
import org.midica.config.Laf;
import org.midica.config.NamedInteger;
import org.midica.ui.widget.ConfigIcon;

/**
 * This is the base class of controllers for file-based config windows.
 * 
 * @author Jan Trukenmüller
 */
public abstract class FileConfigController implements WindowListener, DocumentListener, ActionListener, FocusListener {
	
	protected ConfigIcon     icon;
	protected FileConfigView view;
	
	protected HashSet<String>             configKeys;
	protected HashMap<String, Class<?>>   configClasses;
	protected HashMap<String, String>     sessionConfig;
	protected HashMap<String, JComponent> configWidgets;
	
	/**
	 * Creates a file config controller.
	 * 
	 * @param view  the window to be controlled
	 * @param icon  the icon that is used to open the config window
	 */
	protected FileConfigController(FileConfigView view, ConfigIcon icon) {
		init(view, icon);
	}
	
	/**
	 * Returns the session config.
	 * 
	 * @return the session config.
	 */
	public HashMap<String, String> getSessionConfig() {
		
		// create session config, if not yet done
		if (null == sessionConfig)
			createDefaultView();
		
		return sessionConfig;
	}
	
	/**
	 * Creates a default view that is not intended to be used as a real window.
	 * This is used to initialize data structures, if the user does not want
	 * to config window is.
	 */
	protected abstract void createDefaultView();
	
	/**
	 * Initializes the controller.
	 * 
	 * Connects view and icon with the controller, if not yet done.
	 * Initializes the session config, if not yet done.
	 * Connects the session config to the widgets.
	 * 
	 * @param view  the window to be controlled
	 * @param icon  the icon that is used to open the config window
	 */
	protected void init(FileConfigView view, ConfigIcon icon) {
		if (view != null)
			this.view = view;
		if (icon != null)
			this.icon = icon;
		
		// init session config if not yet done
		if (null == sessionConfig) {
			initSessionConfig(true);
		}
		else {
			initSessionConfig(false);
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object widget = e.getSource();
		
		// checkbox toggled
		if (widget instanceof JCheckBox) {
			applyConfig();
		}
		
		// combobox changed
		else if (widget instanceof JComboBox<?>) {
			applyConfig();
		}
		
		// restore saved settings
		else if (widget == view.btnRestore) {
			initSessionConfig(true);
		}
		
		// restore default settings
		else if (widget == view.btnRestoreDefaults) {
			restoreDefaultSettings();
		}
		
		// save settings
		else if (widget == view.btnSave) {
			saveSettings();
		}
	}
	
	@Override
	public void focusGained(FocusEvent e) {
	}
	
	@Override
	public void focusLost(FocusEvent e) {
	}
	
	@Override
	public void changedUpdate(DocumentEvent e) {
		handleDocumentChange(e);
	}
	
	@Override
	public void insertUpdate(DocumentEvent e) {
		handleDocumentChange(e);
	}
	
	@Override
	public void removeUpdate(DocumentEvent e) {
		handleDocumentChange(e);
	}
	
	@Override
	public void windowActivated(WindowEvent e) {
	}
	
	@Override
	public void windowClosed(WindowEvent e) {
	}
	
	@Override
	public void windowClosing(WindowEvent e) {
		applyConfig();
	}
	
	@Override
	public void windowDeactivated(WindowEvent e) {
	}
	
	@Override
	public void windowDeiconified(WindowEvent e) {
	}
	
	@Override
	public void windowIconified(WindowEvent e) {
	}
	
	@Override
	public void windowOpened(WindowEvent e) {
	}
	
	/**
	 * Initializes the session config and the state of the widgets.
	 * 
	 * If the parameter **fromConfig** is **true**, the config is copied from {@link Config}
	 * to the session config.
	 * Otherwise the session config is left unchanged.
	 * 
	 * @param fromConfig **true** if the session config shall be copied from the {@link Config} class.
	 */
	protected void initSessionConfig(boolean fromConfig) {
		
		// init structures
		configKeys    = new HashSet<>();
		configClasses = new HashMap<>();
		configWidgets = new HashMap<>();
		if (fromConfig) {
			sessionConfig = new HashMap<>();
		}
		
		initSessionConfigSpecific(fromConfig);
		
		// update gui
		if (fromConfig) {
			for (String id : sessionConfig.keySet()) {
				JComponent widget = configWidgets.get(id);
				setWidget(widget, getSavedConfigValue(id));
			}
		}
		else {
			for (String id : sessionConfig.keySet()) {
				JComponent widget = configWidgets.get(id);
				String     value  = sessionConfig.get(id);
				setWidget(widget, value);
			}
		}
	}
	
	/**
	 * Initializes the specific part of the session config.
	 * 
	 * @param fromConfig **true** if the session config shall be copied from the {@link Config} class.
	 */
	protected abstract void initSessionConfigSpecific(boolean fromConfig);
	
	/**
	 * Initializes data structures for one config element.
	 * 
	 * @param id          the config ID
	 * @param widget      the according widget
	 * @param type        the class of the config variable
	 * @param fromConfig  **true**, if the session config shall be overwritten with the saved config value
	 */
	protected void initWidgetConfig(String id, JComponent widget, Class<?> type, boolean fromConfig) {
		configKeys.add(id);
		configClasses.put(id, type);
		configWidgets.put(id, widget);
		
		if (fromConfig)
			sessionConfig.put(id, getSavedConfigValue(id));
	}
	
	/**
	 * Returns the hard-coded default config.
	 * 
	 * @return the default config.
	 */
	protected abstract HashMap<String, String> getDefaultConfig();
	
	/**
	 * Copies each correct config value to the session config.
	 * 
	 * Checks each config value first.
	 * Depending on the check result:
	 * Adjusts each incorrect widget accordingly (background of text fields etc.).
	 * 
	 * Adjusts the icon to open the config window accordingly:
	 * 
	 * - default appearance, if all config values are correct
	 * - otherwise: failure appearance
	 * 
	 * @return **true** if the session config is OK, otherwise **false**.
	 */
	protected boolean applyConfig() {
		boolean isOk = true;
		
		// not yet initialized? - regard as ok
		if (null == configKeys)
			return true;
		
		for (String key : configKeys) {
			if (applyConfigById(key)) {
				// nothing more to do
			}
			else {
				isOk = false;
			}
		}
		icon.setAppearance(isOk);
		
		return isOk;
	}
	
	/**
	 * Copies the given config key's value to the session config, if the value is correct.
	 * 
	 * Adjusts the widget according to the correctness (background of text fields etc.).
	 * 
	 * @param id  config ID
	 * @return **true** if the value is correct, otherwise **false**
	 */
	protected boolean applyConfigById(String id) {
		boolean isOk    = true;
		Color   bgColor = Laf.COLOR_NORMAL;
		
		// get value
		String     valStr  = null;
		JComponent widget  = configWidgets.get(id);
		if (widget instanceof JTextField) {
			JTextField fld = (JTextField) widget;
			valStr = fld.getText();
		}
		else if (widget instanceof JCheckBox) {
			JCheckBox cbx = (JCheckBox) widget;
			valStr = cbx.isSelected() + "";
		}
		else if (widget instanceof JComboBox) {
			JComboBox<?>     cbx   = (JComboBox<?>) widget;
			ComboBoxModel<?> model = cbx.getModel();
			Object           first = model.getElementAt(0);
			if (first instanceof NamedInteger) {
				NamedInteger item = (NamedInteger) cbx.getSelectedItem();
				valStr = item.value + "";
			}
			else if (first instanceof String) {
				String item = (String) cbx.getSelectedItem();
				valStr = item;
			}
		}
		
		// check and apply value
		Class<?> type = configClasses.get(id);
		try {
			if (type == Boolean.class) {
				sessionConfig.put(id, valStr);
			}
			else if (type == Integer.class) {
				int val = Integer.parseInt(valStr);
				if (val < 0)
					isOk = false;
				if (isOk)
					sessionConfig.put(id, valStr);
				else
					bgColor = Laf.COLOR_ERROR;
			}
			else if (type == Float.class) {
				float val = Float.parseFloat(valStr);
				if (val < 0)
					isOk = false;
				if (isOk)
					sessionConfig.put(id, valStr);
				else
					bgColor = Laf.COLOR_ERROR;
			}
			else if (type == String.class) {
				sessionConfig.put(id, valStr);
			}
		}
		catch (Exception e) {
			isOk    = false;
			bgColor = Laf.COLOR_ERROR;
		}
		
		// apply color
		if (widget instanceof JTextField) {
			configWidgets.get(id).setBackground(bgColor);
		}
		
		return isOk;
	}
	
	/**
	 * Adjusts the background color of fields that are not directly related to a
	 * config variable, if overridden accordingly.
	 * 
	 * Does nothing by default. Per default, this is called
	 * by all kinds of document change event.
	 * 
	 * @param e  document event
	 */
	protected void handleDocumentChange(DocumentEvent e) {
		applyConfig();
	}
	
	/**
	 * Saves the session config to the config file.
	 */
	private void saveSettings() {
		for (String id : sessionConfig.keySet()) {
			String value = sessionConfig.get(id);
			Config.set(id, value);
		}
		Config.writeConfigFile();
	}
	
	/**
	 * Applies the (hard-coded) default settings to
	 * the session config and the widgets.
	 */
	private void restoreDefaultSettings() {
		HashMap<String, String> defaultSettings = getDefaultConfig();
		for (String id : defaultSettings.keySet()) {
			JComponent curWidget = configWidgets.get(id);
			String     value     = defaultSettings.get(id);
			restoreDefaultConfig(id, value, curWidget);
		}
	}
	
	/**
	 * Restores the session config with the according default value.
	 * 
	 * @param id      config ID
	 * @param value   config value
	 * @param widget  the according widget
	 */
	private void restoreDefaultConfig(String id, String value, JComponent widget) {
		sessionConfig.put(id, value);
		setWidget(widget, value);
	}
	
	/**
	 * Sets the given widget to the given value.
	 * 
	 * @param widget    the widget to be adjusted
	 * @param valueStr  the value
	 */
	private void setWidget(JComponent widget, String valueStr) {
		if (widget instanceof JTextField) {
			((JTextField) widget).setText(valueStr);
		}
		else if (widget instanceof JCheckBox) {
			boolean isTrue = Boolean.parseBoolean(valueStr);
			((JCheckBox) widget).setSelected(isTrue);
		}
		else if (widget instanceof JComboBox<?>) {
			JComboBox<?>     cbx   = (JComboBox<?>) widget;
			ComboBoxModel<?> model = cbx.getModel();
			Object           first = model.getElementAt(0);
			if (first instanceof NamedInteger) {
				int value = Integer.parseInt(valueStr);
				for (int i = 0; i < model.getSize(); i++) {
					NamedInteger item = (NamedInteger) model.getElementAt(i);
					if (item.value == value)
						cbx.setSelectedIndex(i);
				}
			}
			else if (first instanceof String) {
				for (int i = 0; i < model.getSize(); i++) {
					String item = (String) model.getElementAt(i);
					if (item.equals(valueStr))
						cbx.setSelectedIndex(i);
				}
			}
		}
		else if (widget instanceof JTextArea) {
			((JTextArea) widget).setText(valueStr);
		}
	}
	
	/**
	 * Returns the saved config value of the given config ID.
	 * If the saved config value is not valid, the default config value is used instead.
	 * 
	 * @param id    the config ID
	 * @return the configured value, if the check succeeds, or otherwise the default value
	 */
	private String getSavedConfigValue(String id) {
		
		String valueStr = Config.get(id);
		
		Class<?> type = configClasses.get(id);
		try {
			if (type == Integer.class)
				Integer.parseInt(valueStr);
			else if (type == Float.class)
				Float.parseFloat(valueStr);
		}
		catch (NullPointerException | NumberFormatException e) {
			// use default instead
			valueStr = getDefaultConfig().get(id);
		}
		
		return valueStr;
	}
}
