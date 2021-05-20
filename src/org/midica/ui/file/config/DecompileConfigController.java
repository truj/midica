/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.file.config;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeSet;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.text.JTextComponent;

import org.midica.config.Config;
import org.midica.config.Dict;
import org.midica.config.Laf;
import org.midica.config.NamedInteger;
import org.midica.file.write.MidicaPLExporter;
import org.midica.ui.widget.ConfigIcon;

/**
 * Controller for the decompile config window.
 * 
 * @author Jan Trukenmüller
 */
public class DecompileConfigController extends FileConfigController {
	
	private static DecompileConfigController controller;
	
	private static TreeSet<Long> extraGlobalTicks = null;
	
	/**
	 * Creates a singleton instance of the controller.
	 * 
	 * @param view  the window to be controlled
	 * @param icon  the icon that is used to open the config window
	 * @return the created controller.
	 */
	public static DecompileConfigController getInstance(DecompileConfigView view, ConfigIcon icon) {

		if (null == controller)
			controller = new DecompileConfigController(view, icon);
		else
			controller.init(view, icon);
		
		return controller;
	}
	
	/**
	 * Creates a controller for the decompile config window.
	 * 
	 * @param view  the window to be controlled
	 * @param icon  the icon that is used to open the config window
	 */
	private DecompileConfigController(DecompileConfigView view, ConfigIcon icon) {
		super(view, icon);
	}
	
	@Override
	protected void createDefaultView() {
		new DecompileConfigView();
	}
	
	/**
	 * Returns the ticks for extra global commands.
	 * 
	 * @return ticks for extra global commands
	 */
	public static TreeSet<Long> getExtraGlobalTicks() {
		return extraGlobalTicks;
	}
	
	@Override
	protected void initSessionConfig(boolean fromConfig) {
		
		super.initSessionConfig(fromConfig);
		
		// update extra ticks in the gui
		if (fromConfig) {
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
	}
	
	@Override
	protected HashMap<String, String> getDefaultConfig() {
		return Config.getDefaultDecompileConfig();
	}
	
	@Override
	protected void initSessionConfigSpecific(boolean fromConfig) {
		DecompileConfigView view = (DecompileConfigView) this.view;
		initWidgetConfig( Config.DC_MUST_ADD_TICK_COMMENTS,   view.cbxAddTickComments,        Boolean.class, fromConfig );
		initWidgetConfig( Config.DC_MUST_ADD_CONFIG,          view.cbxAddConfig,              Boolean.class, fromConfig );
		initWidgetConfig( Config.DC_MUST_ADD_QUALITY_SCORE,   view.cbxAddScore,               Boolean.class, fromConfig );
		initWidgetConfig( Config.DC_MUST_ADD_STATISTICS,      view.cbxAddStatistics,          Boolean.class, fromConfig );
		initWidgetConfig( Config.DC_MUST_ADD_STRATEGY_STAT,   view.cbxAddStrategyStat,        Boolean.class, fromConfig );
		initWidgetConfig( Config.DC_LENGTH_STRATEGY,          view.cbxLengthStrategy,         Integer.class, fromConfig );
		initWidgetConfig( Config.DC_MIN_TARGET_TICKS_ON,      view.cbxMinTargetTicksOn,       Integer.class, fromConfig );
		initWidgetConfig( Config.DC_MAX_TARGET_TICKS_ON,      view.cbxMaxTargetTicksOn,       Integer.class, fromConfig );
		initWidgetConfig( Config.DC_MIN_DURATION_TO_KEEP,     view.fldMinDurToKeep,           Float.class,   fromConfig );
		initWidgetConfig( Config.DC_MAX_DURATION_TO_KEEP,     view.fldMaxDurToKeep,           Float.class,   fromConfig );
		initWidgetConfig( Config.DC_LENGTH_TICK_TOLERANCE,    view.fldLengthTickTolerance,    Integer.class, fromConfig );
		initWidgetConfig( Config.DC_DURATION_RATIO_TOLERANCE, view.fldDurationRatioTolerance, Float.class,   fromConfig );
		initWidgetConfig( Config.DC_USE_PRE_DEFINED_CHORDS,   view.cbxPredefinedChords,       Boolean.class, fromConfig );
		initWidgetConfig( Config.DC_CHORD_NOTE_ON_TOLERANCE,  view.fldChordNoteOnTolerance,   Integer.class, fromConfig );
		initWidgetConfig( Config.DC_CHORD_NOTE_OFF_TOLERANCE, view.fldChordNoteOffTolerance,  Integer.class, fromConfig );
		initWidgetConfig( Config.DC_CHORD_VELOCITY_TOLERANCE, view.fldChordVelocityTolerance, Integer.class, fromConfig );
		initWidgetConfig( Config.DC_USE_DOTTED_NOTES,         view.cbxUseDottedNote,          Boolean.class, fromConfig );
		initWidgetConfig( Config.DC_USE_DOTTED_RESTS,         view.cbxUseDottedRest,          Boolean.class, fromConfig );
		initWidgetConfig( Config.DC_USE_TRIPLETTED_NOTES,     view.cbxUseTriplettedNote,      Boolean.class, fromConfig );
		initWidgetConfig( Config.DC_USE_TRIPLETTED_RESTS,     view.cbxUseTriplettedRest,      Boolean.class, fromConfig );
		initWidgetConfig( Config.DC_USE_KARAOKE,              view.cbxUseKaraoke,             Boolean.class, fromConfig );
		initWidgetConfig( Config.DC_ALL_SYLLABLES_ORPHANED,   view.cbxAllSyllablesOrphaned,   Boolean.class, fromConfig );
		initWidgetConfig( Config.DC_ORPHANED_SYLLABLES,       view.cbxOrphanedSyllables,      Integer.class, fromConfig );
		initWidgetConfig( Config.DC_KARAOKE_ONE_CHANNEL,      view.cbxKarOneChannel,          Boolean.class, fromConfig );
		initWidgetConfig( Config.DC_CTRL_CHANGE_MODE,         view.cbxCtrlChangeMode,         Integer.class, fromConfig );
		initWidgetConfig( Config.DC_EXTRA_GLOBALS_STR,        view.areaGlobalsStr,            String.class,  fromConfig );
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		
		Object              widget = e.getSource();
		DecompileConfigView view   = (DecompileConfigView) this.view;
		
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
		
		// restore default settings
		else if (widget == view.btnRestoreDefaults) {
			extraGlobalTicks = new TreeSet<Long>();
		}
	}
	
	@Override
	public void focusGained(FocusEvent e) {
		Component           widget = e.getComponent();
		DecompileConfigView view   = (DecompileConfigView) this.view;
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
	protected boolean applyConfigById(String id) {
		boolean isOk = super.applyConfigById(id);
		
		// check extra global ticks
		JComponent widget = configWidgets.get(id);
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
	
	@Override
	protected void handleDocumentChange(DocumentEvent e) {
		Integer docId = (Integer) e.getDocument().getProperty(DecompileConfigView.DOC_ID);
		DecompileConfigView view = (DecompileConfigView) this.view;
		
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
	 * (Re)fills the text area with ticks for extra global commands.
	 * Makes the according config variable ready to be saved.
	 */
	private void refillExtraGlobalField() {
		DecompileConfigView view = (DecompileConfigView) this.view;
		
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
	 * Converts the comma-separated extra ticks from the text area into a data structure.
	 * 
	 * @return the ticks
	 * @throws NumberFormatException if there are errors in the text string.
	 */
	private TreeSet<Long> getExtraTicksFromTxtArea() throws NumberFormatException {
		DecompileConfigView view  = (DecompileConfigView) this.view;
		TreeSet<Long>       ticks = new TreeSet<>();
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
	
	/**
	 * Returns the model for a combobox.
	 * 
	 * @param id  config ID, identifying the config variable that the combobox controls
	 * @return the combobox model
	 */
	public static DefaultComboBoxModel<NamedInteger> getComboboxModel(String id) {
		DefaultComboBoxModel<NamedInteger> model = new DefaultComboBoxModel<>();
		if (Config.DC_LENGTH_STRATEGY.equals(id)) {
			model.addElement(new NamedInteger(Dict.get(Dict.DC_STRAT_NEXT_DURATION_PRESS), MidicaPLExporter.STRATEGY_NEXT_DURATION_PRESS, true));
			model.addElement(new NamedInteger(Dict.get(Dict.DC_STRAT_DURATION_NEXT_PRESS), MidicaPLExporter.STRATEGY_DURATION_NEXT_PRESS, true));
			model.addElement(new NamedInteger(Dict.get(Dict.DC_STRAT_NEXT_PRESS),          MidicaPLExporter.STRATEGY_NEXT_PRESS,          true));
			model.addElement(new NamedInteger(Dict.get(Dict.DC_STRAT_DURATION_PRESS),      MidicaPLExporter.STRATEGY_DURATION_PRESS,      true));
			model.addElement(new NamedInteger(Dict.get(Dict.DC_STRAT_PRESS),               MidicaPLExporter.STRATEGY_PRESS,               true));
			
			return model;
		}
		else if (Config.DC_ORPHANED_SYLLABLES.equals(id) || Config.DC_CTRL_CHANGE_MODE.equals(id)) {
			model.addElement(new NamedInteger(Dict.get(Dict.DC_INLINE_BLOCK),      MidicaPLExporter.INLINE_BLOCK,      true));
			model.addElement(new NamedInteger(Dict.get(Dict.DC_SLICE_BEGIN_BLOCK), MidicaPLExporter.SLICE_BEGIN_BLOCK, true));
			
			return model;
		}
		else if (Config.DC_MAX_TARGET_TICKS_ON.equals(id) || Config.DC_MIN_TARGET_TICKS_ON.equals(id)) {
			String noteLengths[] = {
				Dict.SYNTAX_32, Dict.SYNTAX_16, Dict.SYNTAX_8,  Dict.SYNTAX_4,   Dict.SYNTAX_2, Dict.SYNTAX_1,
				Dict.SYNTAX_M2, Dict.SYNTAX_M4, Dict.SYNTAX_M8, Dict.SYNTAX_M16, Dict.SYNTAX_M32,
			};
			Integer tickLengths[] = {
				60,   120,  240,   480,   960, 1920,
				3840, 7680, 15360, 30720, 61440,
			};
			for (int i = 0; i < noteLengths.length; i++) {
				model.addElement(getMaxNoteLengthCbxModelElement(tickLengths[i], Dict.getSyntax(noteLengths[i])));
			}
			Integer savedVal = null;
			try {
				savedVal = Integer.parseInt(Config.get(id));
			}
			catch (NumberFormatException e) {
			}
			if (savedVal != null && ! Arrays.asList(tickLengths).contains(savedVal)) {
				model.addElement(getMaxNoteLengthCbxModelElement(savedVal, Dict.get(Dict.CHANGED_IN_CONF_FILE)));
				
			}
			return model;
		}
		
		return null;
	}
	
	/**
	 * Creates a numbered integer that can be put into the combobox model for the max note length combobox.
	 * 
	 * @param ticks         note length in ticks of the target resolution (480 PPQ)
	 * @param lengthName    note length name or description
	 * @return the created element.
	 */
	private static NamedInteger getMaxNoteLengthCbxModelElement(int ticks, String lengthName) {
		return new NamedInteger(
			"<html>"
			+ "<b>" + lengthName + "</b> &nbsp;&mdash; "
			+ ticks + " " + Dict.get(Dict.TICKS_FOR_TARGET_PPQ),
			ticks,
			true
		);
	}
}
