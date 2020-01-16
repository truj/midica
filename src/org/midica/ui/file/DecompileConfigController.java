/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.file;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import org.midica.config.Config;
import org.midica.config.Dict;
import org.midica.config.Laf;
import org.midica.config.NamedInteger;
import org.midica.file.write.MidicaPLExporter;
import org.midica.ui.widget.DecompileConfigIcon;

/**
 * Controller for the decompile config window.
 * 
 * @author Jan Trukenmüller
 */
public class DecompileConfigController implements WindowListener, DocumentListener, ActionListener, FocusListener {
	
	DecompileConfigView view;
	DecompileConfigIcon icon;
	
	private static HashSet<String>             configKeys;
	private static HashMap<String, Class<?>>   configClasses;
	private static HashMap<String, String>     sessionConfig;
	private static HashMap<String, JComponent> configWidgets;
	
	private static TreeSet<Long> extraGlobalTicks = null;
	
	/**
	 * Creates a controller for the decompile config window.
	 * 
	 * @param view  the window to be controlled
	 */
	public DecompileConfigController(DecompileConfigView view, DecompileConfigIcon icon) {
		this.view = view;
		this.icon = icon;
		if (null == sessionConfig) {
			initSessionConfig(true);
		}
		else {
			initSessionConfig(false);
		}
	}
	
	/**
	 * Returns the session config.
	 * 
	 * @return the session config.
	 */
	public static HashMap<String, String> getSessionConfig() {
		
		// create session config, if not yet done
		if (null == sessionConfig)
			new DecompileConfigView(null, null);
		
		return sessionConfig;
	}
	
	/**
	 * Returns the ticks for extra global commands.
	 * 
	 * @return ticks for extra global commands
	 */
	public static TreeSet<Long> getExtraGlobalTicks() {
		return extraGlobalTicks;
	}
	
	/**
	 * Initializes the session config and the state of the widgets.
	 * 
	 * If the parameter **fromConfig** is **true**, the config is copied from {@link Config} to the
	 * session config.
	 * Otherwise the session config is left unchanged.
	 * 
	 * @param fromConfig **true** if the session config shall be copied from the {@link Config} class.
	 */
	private void initSessionConfig(boolean fromConfig) {
		
		// init structures
		configKeys    = new HashSet<>();
		configClasses = new HashMap<>();
		configWidgets = new HashMap<>();
		if (fromConfig) {
			sessionConfig = new HashMap<>();
		}
		initWidgetConfig( Config.DC_MUST_ADD_TICK_COMMENTS,   view.cbxAddTickComments,        Boolean.class, fromConfig );
		initWidgetConfig( Config.DC_MUST_ADD_CONFIG,          view.cbxAddConfig,              Boolean.class, fromConfig );
		initWidgetConfig( Config.DC_MUST_ADD_QUALITY_SCORE,   view.cbxAddScore,               Boolean.class, fromConfig );
		initWidgetConfig( Config.DC_MUST_ADD_STATISTICS,      view.cbxAddStatistics,          Boolean.class, fromConfig );
		initWidgetConfig( Config.DC_LENGTH_STRATEGY,          view.cbxLengthStrategy,         Integer.class, fromConfig );
		initWidgetConfig( Config.DC_DURATION_TICK_TOLERANCE,  view.fldDurationTickTolerance,  Integer.class, fromConfig );
		initWidgetConfig( Config.DC_DURATION_RATIO_TOLERANCE, view.fldDurationRatioTolerance, Float.class,   fromConfig );
		initWidgetConfig( Config.DC_MIN_DURATION_TO_KEEP,     view.fldMinDurToKeep,           Float.class,   fromConfig );
		initWidgetConfig( Config.DC_NEXT_NOTE_ON_TOLERANCE,   view.fldNextNoteOnTolerance,    Integer.class, fromConfig );
		initWidgetConfig( Config.DC_MAX_TARGET_TICKS_ON,      view.fldMaxTargetTicksOn,       Integer.class, fromConfig );
		initWidgetConfig( Config.DC_USE_PRE_DEFINED_CHORDS,   view.cbxPredefinedChords,       Boolean.class, fromConfig );
		initWidgetConfig( Config.DC_CHORD_NOTE_ON_TOLERANCE,  view.fldChordNoteOnTolerance,   Integer.class, fromConfig );
		initWidgetConfig( Config.DC_CHORD_NOTE_OFF_TOLERANCE, view.fldChordNoteOffTolerance,  Integer.class, fromConfig );
		initWidgetConfig( Config.DC_CHORD_VELOCITY_TOLERANCE, view.fldChordVelocityTolerance, Integer.class, fromConfig );
		initWidgetConfig( Config.DC_ORPHANED_SYLLABLES,       view.cbxOrphanedSyllables,      Integer.class, fromConfig );
		initWidgetConfig( Config.DC_KARAOKE_ONE_CHANNEL,      view.cbxKarOneChannel,          Boolean.class, fromConfig );
		initWidgetConfig( Config.DC_EXTRA_GLOBALS_STR,        view.areaGlobalsStr,            String.class,  fromConfig );
		
		// update gui
		if (fromConfig) {
			for (String id : sessionConfig.keySet()) {
				JComponent widget = configWidgets.get(id);
				setWidget(widget, Config.get(id));
			}
			
			// extra ticks
			String extraTicksStr = (String) sessionConfig.get(Config.DC_EXTRA_GLOBALS_STR);
			extraGlobalTicks     = new TreeSet<>();
			for (String tickStr : extraTicksStr.split(",")) {
				try {
					long tick = Long.parseLong(tickStr);
					extraGlobalTicks.add(tick);
				}
				catch(Exception e) {
				}
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
	 * Initializes data structures for one decompile config element.
	 * 
	 * @param id          the config ID
	 * @param widget      the according widget
	 * @param type        the class of the config variable
	 * @param fromConfig  **true**, if the session config shall be overwritten with the saved config value
	 */
	private void initWidgetConfig(String id, JComponent widget, Class<?> type, boolean fromConfig) {
		configKeys.add(id);
		configClasses.put(id, type);
		configWidgets.put(id, widget);
		if (fromConfig) {
			sessionConfig.put(id, Config.get(id));
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
	 * @param valueObj  the value
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
			int value = Integer.parseInt(valueStr);
			JComboBox<?>     cbx   = (JComboBox<?>) widget;
			ComboBoxModel<?> model = cbx.getModel();
			for (int i = 0; i < model.getSize(); i++) {
				NamedInteger item = (NamedInteger) model.getElementAt(i);
				if (item.value == value) {
					cbx.setSelectedIndex(i);
				}
			}
		}
		else if (widget instanceof JTextArea) {
			((JTextArea) widget).setText(valueStr);
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object widget = e.getSource();
		
		// ENTER inside of a text field
		if (view.fldAddGlobalAtTick == widget) {
			view.btnAddGlobalAtTick.doClick();
			return;
		}
		else if (view.fldAddGlobalsEachTick == widget || view.fldAddGlobalsStartTick == widget || view.fldAddGlobalsStopTick == widget) {
			view.btnAddGlobalTicks.doClick();
			return;
		}
		
		// checkbox toggled
		else if (widget instanceof JCheckBox) {
			applyConfig();
		}
		
		// combobox changed
		else if (widget instanceof JComboBox<?>) {
			applyConfig();
		}
		
		// add a single tick:
		// check field, color background, add tick
		else if (widget == view.btnAddGlobalAtTick) {
			Long tick = null;
			try {
				String txt = view.fldAddGlobalAtTick.getText();
				tick = Long.parseLong(txt);
				if (tick == 0) {
					tick = null;
					throw new Exception();
				}
				
				// add tick
				extraGlobalTicks.add(tick);
				
				// update gui
				view.fldAddGlobalAtTick.setText("");
				view.fldAddGlobalAtTick.setBackground(Laf.COLOR_NORMAL);
				refillExtraGlobalField();
			}
			catch (Exception ex) {
				// red background
				view.fldAddGlobalAtTick.setBackground(Laf.COLOR_ERROR);
			}
		}
		
		// add multiple ticks:
		// check fields, color background, add ticks
		else if (widget == view.btnAddGlobalTicks) {
			Long eachTick  = null;
			Long startTick = null;
			Long stopTick  = null;
			
			// check 'each' field
			try {
				String txt = view.fldAddGlobalsEachTick.getText();
				eachTick = Long.parseLong(txt);
				if (eachTick == 0) {
					eachTick = null;
					throw new Exception();
				}
			}
			catch (Exception ex) {
				// red background
				view.fldAddGlobalsEachTick.setBackground(Laf.COLOR_ERROR);
			}
			
			// check start tick
			try {
				String txt = view.fldAddGlobalsStartTick.getText();
				startTick = Long.parseLong(txt);
				if (startTick == 0) {
					startTick = null;
					throw new Exception();
				}
			}
			catch (Exception ex) {
				// red background
				view.fldAddGlobalsStartTick.setBackground(Laf.COLOR_ERROR);
			}
			
			// check start tick
			try {
				String txt = view.fldAddGlobalsStopTick.getText();
				stopTick = Long.parseLong(txt);
				if (stopTick == 0) {
					stopTick = null;
					throw new Exception();
				}
			}
			catch (Exception ex) {
				// red background
				view.fldAddGlobalsStopTick.setBackground(Laf.COLOR_ERROR);
			}
			
			// all fields ok?
			if (eachTick != null && startTick != null && stopTick != null) {
				
				// check start/stop order
				if (startTick >= stopTick) {
					view.fldAddGlobalsStartTick.setBackground(Laf.COLOR_ERROR);
					view.fldAddGlobalsStopTick.setBackground(Laf.COLOR_ERROR);
					
					return;
				}
				else {
					// add ticks
					long currentTick = startTick;
					while (currentTick <= stopTick) {
						extraGlobalTicks.add(currentTick);
						currentTick += eachTick;
					}
					
					// update gui
					view.fldAddGlobalsEachTick.setText("");
					view.fldAddGlobalsStartTick.setText("");
					view.fldAddGlobalsStopTick.setText("");
					view.fldAddGlobalsEachTick.setBackground(Laf.COLOR_NORMAL);
					view.fldAddGlobalsStartTick.setBackground(Laf.COLOR_NORMAL);
					view.fldAddGlobalsStopTick.setBackground(Laf.COLOR_NORMAL);
					refillExtraGlobalField();
				}
			}
			
			return;
		}
		
		// update all ticks
		else if (widget == view.btnAllTicks) {
			try {
				TreeSet<Long> ticks = getExtraTicksFromTxtArea();
				
				// replace ticks
				extraGlobalTicks = ticks;
				
				// update gui
				refillExtraGlobalField();
			}
			catch(NumberFormatException ex) {
			}
		}
		
		// restore saved settings
		else if (widget == view.btnRestore) {
			initSessionConfig(true);
		}
		
		// restore default settings
		else if (widget == view.btnRestoreDefaults) {
			HashMap<String, String> dcDefaults = Config.getDefaultDecompileConfig();
			for (String id : dcDefaults.keySet()) {
				JComponent curWidget = configWidgets.get(id);
				String     value     = dcDefaults.get(id);
				restoreDefaultConfig(id, value, curWidget);
			}
		}
		
		// save settings
		else if (widget == view.btnSave) {
			for (String id : sessionConfig.keySet()) {
				String value = sessionConfig.get(id);
				Config.set(id, value);
			}
			Config.writeConfigFile();
		}
	}
	
	@Override
	public void focusGained(FocusEvent e) {
		Component widget = e.getComponent();
		if ( view.fldAddGlobalAtTick     == widget
		  || view.fldAddGlobalsEachTick  == widget
		  || view.fldAddGlobalsStartTick == widget
		  || view.fldAddGlobalsStopTick  == widget
		  || view.areaGlobalsStr         == widget
		) {
			JTextComponent txtWidget = (JTextComponent) widget;
			if (0 == txtWidget.getText().length()) {
				txtWidget.setBackground(Laf.COLOR_NORMAL);
			}
			else {
				txtWidget.setBackground(Laf.COLOR_WARNING);
			}
		}
	}
	
	@Override
	public void focusLost(FocusEvent e) {
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
	 * (Re)fills the text area with ticks for extra global commands.
	 * Makes the according config variable ready to be saved.
	 */
	private void refillExtraGlobalField() {
		
		// gui
		ArrayList<String> tickStrings = new ArrayList<>();
		for (long tick : extraGlobalTicks) {
			tickStrings.add(tick + "");
		}
		String configStr = String.join(",", tickStrings);
		view.areaGlobalsStr.setText(configStr);
		view.areaGlobalsStr.setBackground(Laf.COLOR_NORMAL);
		
		// config
		sessionConfig.put(Config.DC_EXTRA_GLOBALS_STR, configStr);
	}
	
	/**
	 * Copies each correct config value to the session config.
	 * 
	 * Checks each config value first.
	 * Depending on the check value:
	 * Adjusts each incorrect widget accordingly (background of text fields etc.).
	 * 
	 * Adjusts the icon to open the config window accordingly:
	 * 
	 * - default appearance, if all config values are correct
	 * - otherwise: failure appearance
	 * 
	 * @return **true** if the session config is OK, otherwise **false**.
	 */
	private boolean applyConfig() {
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
	private boolean applyConfigById(String id) {
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
			JComboBox<?> cbx = (JComboBox<?>) widget;
			NamedInteger item = (NamedInteger) cbx.getSelectedItem();
			valStr = item.value + "";
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
		}
		catch (Exception e) {
			isOk    = false;
			bgColor = Laf.COLOR_ERROR;
		}
		
		// apply color
		if (widget instanceof JTextField) {
			configWidgets.get(id).setBackground(bgColor);
		}
		
		// check extra global ticks
		if (widget instanceof JTextArea) {
			try {
				getExtraTicksFromTxtArea();
				return true;
			}
			catch (NumberFormatException e) {
				return false;
			}
		}
		
		return isOk;
	}
	
	/**
	 * Converts the comma-separated extra ticks from the text area into a data structure.
	 * 
	 * @return the ticks
	 * @throws NumberFormatException if there are errors in the text string.
	 */
	private TreeSet<Long> getExtraTicksFromTxtArea() throws NumberFormatException {
		TreeSet<Long> ticks = new TreeSet<>();
		try {
			// get ticks
			String[] strTicks = view.areaGlobalsStr.getText().split("\\s*,\\s*");
			for (String str : strTicks) {
				if ("".equals(str)) {
					continue;
				}
				long tick = Long.parseLong(str);
				ticks.add(tick);
			}
			
			return ticks;
		}
		catch (NumberFormatException e) {
			view.areaGlobalsStr.setBackground(Laf.COLOR_ERROR);
			throw e;
		}
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
	
	/**
	 * Adjusts the background color of fields that are not directly related to a config variable.
	 * 
	 * @param e  document event
	 */
	private void handleDocumentChange(DocumentEvent e) {
		Integer docId = (Integer) e.getDocument().getProperty(DecompileConfigView.DOC_ID);
		
		// not directly config-based field?
		if (docId != null) {
			int textLength = e.getDocument().getLength();
			JTextComponent widget = view.fldAddGlobalAtTick;
			if (DecompileConfigView.DOC_ID_ADD_GLOBAL_AT_TICK == docId)
				widget = view.fldAddGlobalAtTick;
			else if (DecompileConfigView.DOC_ID_ADD_GLOBAL_EACH == docId)
				widget = view.fldAddGlobalsEachTick;
			else if (DecompileConfigView.DOC_ID_ADD_GLOBAL_START == docId)
				widget = view.fldAddGlobalsStartTick;
			else if (DecompileConfigView.DOC_ID_ADD_GLOBAL_STOP == docId)
				widget = view.fldAddGlobalsStopTick;
			else if (DecompileConfigView.DOC_ID_UPDATE_GLOBAL_ALL == docId)
				widget = view.areaGlobalsStr;
			
			// adjust background
			if (0 == textLength)
				widget.setBackground(Laf.COLOR_NORMAL);
			else
				widget.setBackground(Laf.COLOR_WARNING);
			
			return;
		}
		
		// directly config-based
		applyConfig();
	}
	
	/**
	 * Returns the model for a combobox.
	 * 
	 * @param id  config ID, identifying the config variable that the combobox controls
	 * @return the combobox model
	 */
	public static DefaultComboBoxModel<NamedInteger> getComboboxModel(String id) {
		if (Config.DC_LENGTH_STRATEGY.equals(id)) {
			DefaultComboBoxModel<NamedInteger> model = new DefaultComboBoxModel<>();
			model.addElement(new NamedInteger(Dict.get(Dict.DC_STRAT_NEXT_DURATION_PRESS), MidicaPLExporter.STRATEGY_NEXT_DURATION_PRESS, true));
			model.addElement(new NamedInteger(Dict.get(Dict.DC_STRAT_DURATION_NEXT_PRESS), MidicaPLExporter.STRATEGY_DURATION_NEXT_PRESS, true));
			model.addElement(new NamedInteger(Dict.get(Dict.DC_STRAT_NEXT_PRESS),          MidicaPLExporter.STRATEGY_NEXT_PRESS,          true));
			model.addElement(new NamedInteger(Dict.get(Dict.DC_STRAT_DURATION_PRESS),      MidicaPLExporter.STRATEGY_DURATION_PRESS,      true));
			model.addElement(new NamedInteger(Dict.get(Dict.DC_STRAT_PRESS),               MidicaPLExporter.STRATEGY_PRESS,               true));
			
			return model;
		}
		else if (Config.DC_ORPHANED_SYLLABLES.equals(id)) {
			DefaultComboBoxModel<NamedInteger> model = new DefaultComboBoxModel<>();
			model.addElement(new NamedInteger(Dict.get(Dict.DC_INLINE), MidicaPLExporter.INLINE, true));
			model.addElement(new NamedInteger(Dict.get(Dict.DC_BLOCK),  MidicaPLExporter.BLOCK,  true));
			
			return model;
		}
		
		return null;
	}
}
