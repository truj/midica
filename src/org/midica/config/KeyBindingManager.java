/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.config;

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.midica.ui.ErrorMsgView;
import org.midica.ui.tablefilter.FilterIcon;
import org.midica.ui.tablefilter.FilterIconWithLabel;
import org.midica.ui.widget.MidicaButton;
import org.midica.ui.widget.MidicaTable;
import org.midica.ui.widget.MidicaTree;

/**
 * This class handles key bindings.
 * 
 * @author Jan Trukenmüller
 */
public class KeyBindingManager {
	
	private Window              window;
	private JComponent          component;
	private InputMap            inputMap;
	private ActionMap           actionMap;
	private TreeSet<KeyBinding> usedBindings;
	private TreeSet<KeyBinding> conflicts;
	private boolean isSpaceUsed = false;
	private boolean isEnterUsed = false;
	
	// special field for player and soundcheck
	private boolean isSpaceGlobal = false;
	
	// fields for nested tabs
	private boolean                           hasNestedTabs = false;
	private HashMap<String, Integer>          tabToLevel;
	private HashMap<String, int[]>            tabToIndices;
	private HashMap<String, HashSet<String>>  bindingToIds;
	private JTabbedPane                       tabbedPaneLvl1;
	private TreeMap<Integer, HashSet<String>> indexToLvl2Tabs;
	private TreeMap<Integer, JTabbedPane>     indexToLvl2Pane;
	private TreeMap<String, JComponent>       lvl3Components;
	
	// fields for channel selection in the combobox in the soundcheck window
	private JComboBox<?>                                 comboboxToSetChannel;
	private static TreeMap<Integer, TreeSet<KeyBinding>> bindingsToCbxChannel;
	
	/**
	 * Creates a new key binding manager used for one main container and window.
	 * The container will be used to host the input map and action map of the key bindings.
	 * It will react to the key bindings if one of it's decendents has the focus.
	 * 
	 * @param w  the window hosting the container and the key bindings
	 * @param c  the container hosting input map and action map
	 */
	public KeyBindingManager(Window w, JComponent c) {
		window    = w;
		component = c;
		init(c);
	}
	
	/**
	 * Initializes and resets all fields.
	 * 
	 * @param c  the container hosting input map and action map
	 */
	private void init(JComponent c) {
		inputMap  = c.getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
		actionMap = c.getActionMap();
		inputMap.clear();
		actionMap.clear();
		
		usedBindings    = new TreeSet<>();
		conflicts       = new TreeSet<>();
		tabToLevel      = new HashMap<>();
		tabToIndices    = new HashMap<>();
		bindingToIds    = new HashMap<>();
		indexToLvl2Pane = new TreeMap<>();
		indexToLvl2Tabs = new TreeMap<>();
		lvl3Components  = new TreeMap<>();
		
		bindingsToCbxChannel = new TreeMap<>();
		comboboxToSetChannel = null;
	}
	
	/**
	 * Enables the SPACE key to be a window-wide global key binding.
	 * Used for the player and soundcheck view.
	 */
	public void globalizeSpace() {
		isSpaceGlobal = true;
	}
	
	/**
	 * Adds a key binding for a button to be clicked.
	 * 
	 * @param btn  the button
	 * @param id   the key binding ID
	 */
	public void addBindingsForButton(MidicaButton btn, String id) {
		
		// fill input map
		addInputs(id);
		
		// tooltips
		addTooltip(btn, id, Dict.TT_KEY_BUTTON_PRESS);
		
		// fill action map
		actionMap.put(id, new AbstractAction() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				
				if (mustIgnore(e))
					return;
				
				btn.doClick();
			}
		});
	}
	
	/**
	 * Adds a key binding for a component to be focused.
	 * 
	 * @param comp  the component
	 * @param id    the key binding ID
	 */
	public void addBindingsForFocus(JComponent comp, String id) {
		
		// fill input map
		addInputs(id);
		
		// tooltips
		if (comp instanceof JTextField)
			addTooltip(comp, id, Dict.TT_KEY_TXT_FLD_FOCUS);
		else if (comp instanceof JSlider)
			addTooltip(comp, id, Dict.TT_KEY_SLD_FOCUS);
		else if (comp instanceof MidicaTable)
			addTooltip(comp, id, Dict.TT_KEY_TABLE_FOCUS);
		else if (comp instanceof MidicaTree)
			addTooltip(comp, id, Dict.TT_KEY_TREE_FOCUS);
		else if (comp instanceof JList)
			addTooltip(comp, id, Dict.TT_KEY_LIST_FOCUS);
		
		// fill action map
		actionMap.put(id, new AbstractAction() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				
				if (mustIgnore(e))
					return;
				
				comp.requestFocus();
			}
		});
	}
	
	/**
	 * Adds a key binding for list of components to be focused when possible.
	 * All elements use the same key binding ID but at most one of them is visible.
	 * This is used for channel-based widgets in the player window that are only visible
	 * if the according channel has been opened.
	 * The indices of the given components must match the channel number.
	 * 
	 * @param components  the list of components
	 * @param id          the key binding ID
	 */
	public void addBindingsForFocusOfVisibleChannel(ArrayList<?> components, String id) {
		
		// fill input map
		addInputs(id);
		
		// tooltips
		for (Object c : components) {
			JComponent comp = (JComponent) c;
			if (comp instanceof JTextField)
				addTooltip(comp, id, Dict.TT_KEY_TXT_FLD_FOCUS);
			else if (comp instanceof JSlider)
				addTooltip(comp, id, Dict.TT_KEY_SLD_FOCUS);
			else if (comp instanceof MidicaTable)
				addTooltip(comp, id, Dict.TT_KEY_TABLE_FOCUS);
		}
		
		// fill action map
		actionMap.put(id, new AbstractAction() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				
				if (mustIgnore(e))
					return;
				
				for (Object c : components) {
					JComponent comp = (JComponent) c;
					if (comp.isShowing()) {
						comp.requestFocus();
						return;
					}
				}
			}
		});
	}
	
	/**
	 * Adds a key binding for a combobox to be opened.
	 * This is used for opening the config comboboxes in the main window.
	 * 
	 * @param cbx  the combobox
	 * @param id   the key binding ID
	 */
	public void addBindingsForComboboxOpen(JComboBox<?> cbx, String id) {
		
		// fill input map
		addInputs(id);
		
		// tooltips
		addTooltip(cbx, id, Dict.TT_KEY_CBX_OPEN);
		
		// fill action map
		actionMap.put(id, new AbstractAction() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				
				if (mustIgnore(e))
					return;
				
				// get focus and open
				cbx.requestFocus();
				cbx.showPopup();
			}
		});
	}
	
	/**
	 * Adds a key binding for a combobox element to be selected.
	 * This is used for channel selection in the soundcheck window.
	 * 
	 * @param cbx    the combobox
	 * @param id     the key binding ID
	 * @param index  the index to be selected
	 */
	public void addBindingsForComboboxSelect(JComboBox<?> cbx, String id, int index) {
		
		// fill input map
		addInputs(id);
		
		// tooltips
		TreeSet<KeyBinding> keyBindings = Config.getKeyBindings(id);
		bindingsToCbxChannel.put(index, keyBindings);
		comboboxToSetChannel = cbx;
		
		// fill action map
		actionMap.put(id, new AbstractAction() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				
				if (mustIgnore(e))
					return;
				
				// select index
				cbx.setSelectedIndex(index);
			}
		});
	}
	
	/**
	 * Adds a key binding for a checkbox to be toggled.
	 * 
	 * @param cbx  the checkbox
	 * @param id   the key binding ID
	 */
	public void addBindingsForCheckbox(JCheckBox cbx, String id) {
		
		// fill input map
		addInputs(id);
		
		// tooltips
		addTooltip(cbx, id, Dict.TT_KEY_CBX_TOGGLE);
		
		// fill action map
		actionMap.put(id, new AbstractAction() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				
				if (mustIgnore(e))
					return;
				
				// focus checkbox
				cbx.requestFocus();
				
				// toggle checkbox
				if (cbx.isSelected())
					cbx.setSelected(false);
				else
					cbx.setSelected(true);
			}
		});
	}
	
	/**
	 * Adds a key binding for a table string filter to be opened.
	 * The given filter icon can be an element of one of the following classes:
	 * 
	 * - {@link FilterIconWithLabel}
	 * - {@link FilterIcon}
	 * 
	 * @param icon  the icon belonging to the string filter to be opened
	 * @param id    the key binding ID
	 */
	public void addBindingsForTableFilter(JComponent icon, String id) {
		
		// fill input map
		addInputs(id);
		
		// tooltips
		if (icon instanceof FilterIconWithLabel) {
			((FilterIconWithLabel) icon).rememberKeyBindingId(id, Dict.TT_KEY_FILTER_OPEN);
			addTooltip(icon, id, Dict.TT_KEY_FILTER_OPEN);
		}
		else if (icon instanceof FilterIcon) {
			((FilterIcon) icon).rememberKeyBindingId(id, Dict.TT_KEY_FILTER_OPEN);
			addTooltip(icon, id, Dict.TT_KEY_FILTER_OPEN);
		}
		
		// fill action map
		actionMap.put(id, new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				
				if (mustIgnore(e))
					return;
				
				// get the focus
				if (icon instanceof FilterIconWithLabel)
					((FilterIconWithLabel) icon).open();
				else if (icon instanceof FilterIcon)
					((FilterIcon) icon).open();
			}
		});
	}
	
	/**
	 * Adds a key binding for a slider to be set.
	 * 
	 * @param slider  the slider
	 * @param id      the key binding ID
	 * @param value   the value to be set
	 */
	public void addBindingsForSliderSet(JSlider slider, String id, int value) {
		
		// fill input map
		addInputs(id);
		
		// fill action map
		actionMap.put(id, new AbstractAction() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				
				if (mustIgnore(e))
					return;
				
				// set the slider
				setSliderAndInformListeners(slider, value);
			}
		});
	}
	
	/**
	 * Adds a key binding for closing the window.
	 * 
	 * @param id  the key binding ID
	 */
	public void addBindingsForClose(String id) {
		
		// fill input map
		addInputs(id);
		
		// fill action map
		actionMap.put(id, new AbstractAction() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				
				if (mustIgnore(e))
					return;
				
				WindowEvent closeEvent = new WindowEvent( window, WindowEvent.WINDOW_CLOSING );
				window.dispatchEvent(closeEvent);
			}
		});
	}
	
	/**
	 * Adds a key binding for selecting a tab in the first level of a nested {link {@link JTabbedPane}}.
	 * The same key binding can also be used for a different element.
	 * If the other element is visible, it has always priority over level-1 tabs.
	 * This is used for the Info View.
	 * 
	 * @param tabbedPane  the level-1 tabbed pane
	 * @param id          the key binding ID
	 * @param index       the index of the tab in the level-1 tabbed pane
	 */
	public void addBindingsForTabLevel1(JTabbedPane tabbedPane, String id, int index) {
		tabbedPaneLvl1 = tabbedPane;
		int[] indices  = { index };
		tabToIndices.put(id, indices);
		tabToLevel.put(id, 1);
		
		// tooltips
		String tt = getBindingSpecificToolTip(id, Dict.TT_KEY_TAB_SELECT);
		tabbedPane.setToolTipTextAt(index, "<html>" + tt);
		
		// remember bindings and fill input map
		rememberTabBindings(id);
	}
	
	/**
	 * Adds a key binding for selecting a tab in the second level of a nested {link {@link JTabbedPane}}.
	 * The same key binding can also be used for a different element.
	 * If the other element is visible and inside of the currently selected level-2 tab, it has priority over
	 * the level-2 tab to be selected.
	 * If the other element is a level-1 tab, the level-2 tab has priority.
	 * This is used for the Info View.
	 * 
	 * @param lvl2Pane   the level-2 tabbed pane
	 * @param id         the key binding ID
	 * @param indexLvl1  the index of the tab in the level-1 tabbed pane
	 * @param indexLvl2  the index of the tab in the level-2 tabbed pane
	 */
	public void addBindingsForTabLevel2(JTabbedPane lvl2Pane, String id, int indexLvl1, int indexLvl2) {
		tabToLevel.put(id, 2);
		int[] indices = { indexLvl1, indexLvl2 };
		tabToIndices.put(id, indices);
		indexToLvl2Pane.put(indexLvl1, lvl2Pane);
		
		// tooltips
		String tt = getBindingSpecificToolTip(id, Dict.TT_KEY_TAB_SELECT);
		lvl2Pane.setToolTipTextAt(indexLvl2, "<html>" + tt);
		
		// remember parent index (level-1 index)
		HashSet<String> ids = indexToLvl2Tabs.get(indexLvl1);
		if (null == ids) {
			ids = new HashSet<String>();
			indexToLvl2Tabs.put(indexLvl1, ids);
		}
		ids.add(id);
		
		// remember bindings and fill input map
		rememberTabBindings(id);
	}
	
	/**
	 * Adds a key binding for an element inside of a nested tab.
	 * 
	 * The same key binding can also be used for selecting a level-1 or level-2 tab.
	 * In this case, the level-3 element has always priority if it's currently visible.
	 * 
	 * This is used for the Info View.
	 * 
	 * @param c   the component to be controlled by the key binding
	 * @param id  the key binding ID
	 */
	public void addBindingsForTabLevel3(JComponent c, String id) {
		tabToLevel.put(id, 3);
		lvl3Components.put(id, c);
		
		// add tooltip
		if (c instanceof FilterIconWithLabel) {
			((FilterIconWithLabel) c).rememberKeyBindingId(id, Dict.TT_KEY_FILTER_OPEN);
			addTooltip(c, id, Dict.TT_KEY_FILTER_OPEN);
		}
		else if (c instanceof FilterIcon) {
			((FilterIcon) c).rememberKeyBindingId(id, Dict.TT_KEY_FILTER_OPEN);
			addTooltip(c, id, Dict.TT_KEY_FILTER_OPEN);
		}
		else if (c instanceof MidicaButton)
			addTooltip(c, id, Dict.TT_KEY_BUTTON_PRESS);
		else if (c instanceof MidicaTree) {
			addTooltip(c, id, Dict.TT_KEY_TREE_FOCUS);
		}
		else if (c instanceof MidicaTable) {
			JViewport   viewPort   = (JViewport) c.getParent();
			JScrollPane scrollPane = (JScrollPane) viewPort.getParent();
			addTooltip(scrollPane, id, Dict.TT_KEY_TABLE_FOCUS);
		}
		else if (c instanceof JCheckBox)
			addTooltip(c, id, Dict.TT_KEY_CBX_TOGGLE);
		else if (c instanceof JTextField)
			addTooltip(c, id, Dict.TT_KEY_TXT_FLD_FOCUS);
		
		// remember bindings and fill input map
		rememberTabBindings(id);
	}
	
	/**
	 * Fills the input map and stores the association between key bindings and components
	 * of the given ID to be used later.
	 * Only used for nested tabs. Needed for the info window.
	 * 
	 * @param id  the key binding ID
	 */
	private void rememberTabBindings(String id) {
		hasNestedTabs = true;
		
		TreeSet<KeyBinding> keyBindings = Config.getKeyBindings(id);
		for (KeyBinding binding : keyBindings) {
			HashSet<String> ids = bindingToIds.get(binding.toString());
			if (null == ids) {
				ids = new HashSet<>();
				bindingToIds.put(binding.toString(), ids);
			}
			ids.add(id);
		}
		
		addInputsForNestedTabs(id);
	}
	
	/**
	 * Checks if a key binding action must be ignored.
	 * This method is called if a bound key has been pressed.
	 * 
	 * A key must be ignored, if:
	 * 
	 * - a text field is currently focused; and
	 * - the pressed key is printable; and
	 * - there are no modifiers or only SHIFT; and
	 * - the key is NOT the SPACE bar
	 * 
	 * The SPACE bar exception is needed to enable the space bar to be bound
	 * to the PLAY/PAUSE button even if another button has the focus.
	 * 
	 * @param e  the action event that was caused by a key press
	 * @return **true**, if the action must be ignored, otherwise: **false**
	 */
	private boolean mustIgnore(ActionEvent e) {
		
		// no text field is focused? - don't ignore
		Component focusOwner = window.getFocusOwner();
		if ( ! (focusOwner instanceof JTextField) )
			return false;
		
		// special key pressed (e.g. F5)? - don't ignore
		String cmd = e.getActionCommand();
		if (null == cmd)
			return false;
		
		// check for other special keys
		if (1 == cmd.length()) {
			char c = cmd.charAt(0);
			
			// control character - don't ignore
			if (Character.isISOControl(c))
				return false;
			
			// undefined key? - no need to ignore
			if (KeyEvent.CHAR_UNDEFINED == c)
				return false;
			
			// another kind of special character
			Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
			if (null == block)
				return false;
			if (Character.UnicodeBlock.SPECIALS == block)
				return false;
			
			// global space binding? - don't ignore SPACE (used for player and soundcheck view):
			// PLAY/PAUSE should always work there, even if a text field is focused
			if (isSpaceGlobal) {
				
				// SPACE: don't ignore
				if (KeyEvent.VK_SPACE == c)
					return false;
			}
		}
		
		// printable key pressed! - check if and how it's modified (e.g. by SHIFT, CTRL and so on)
		int modifiers = e.getModifiers();
		
		// unmodified key pressed - ignore
		if (0 == modifiers)
			return true;
		
		// only SHIFT + printable key - ignore
		if (InputEvent.SHIFT_DOWN_MASK == modifiers)
			return true;
		
		// printable key while a text field is focused
		// but with other (or more) modifiers than (only) SHIFT
		// --> don't ignore
		return false;
	}
	
	/**
	 * Fills the input maps for the key bindings of the given ID.
	 * 
	 * @param id  the key binding ID
	 */
	private void addInputs(String id) {
		TreeSet<KeyBinding> keyBindings = Config.getKeyBindings(id);
		for (KeyBinding binding : keyBindings) {
			int keyCode   = binding.getKeyCode();
			int modifiers = binding.getModifiers();
			KeyStroke keyStroke = KeyStroke.getKeyStroke(keyCode, modifiers, false);
			inputMap.put(keyStroke, id);
			
			// conflicts?
			if (usedBindings.contains(binding)) {
				conflicts.add(binding);
			}
			else {
				// no conflicts: remember
				usedBindings.add(binding);
			}
			
			// remember if SPACE and/or ENTER are used
			if (KeyEvent.VK_SPACE == keyCode)
				isSpaceUsed = true;
			if (KeyEvent.VK_ENTER == keyCode)
				isEnterUsed = true;
		}
	}
	
	/**
	 * Fills the input maps for the key bindings of the given ID.
	 * Used instead of {@link addInputMap()}, when nested tabs are used.
	 * Used for the info window.
	 * 
	 * Other than addInputMap(), the key of the added input maps also contains the key binding.
	 * 
	 * @param id  the key binding ID
	 */
	private void addInputsForNestedTabs(String id) {
		TreeSet<KeyBinding> keyBindings = Config.getKeyBindings(id);
		for (KeyBinding binding : keyBindings) {
			int keyCode   = binding.getKeyCode();
			int modifiers = binding.getModifiers();
			KeyStroke keyStroke = KeyStroke.getKeyStroke(keyCode, modifiers, false);
			inputMap.put(keyStroke, "nested_tabs:" + id + ":" + binding);
			
			// remember the binding
			usedBindings.add(binding);
			
			// remember if SPACE and/or ENTER are used
			if (KeyEvent.VK_SPACE == keyCode)
				isSpaceUsed = true;
			if (KeyEvent.VK_ENTER == keyCode)
				isEnterUsed = true;
		}
	}
	
	/**
	 * This method must be called after all bindings have been added.
	 * 
	 * If some special bindings (SPACE or ENTER) are in use, it prevents the focused
	 * element to react to these bindings directly.
	 * 
	 * It also checks if the root component contains sliders. If this is the case, it adds
	 * some slider-specific bindings to them.
	 * 
	 * This is needed for some special cases, if a slider-specific binding is used for something else.
	 * In this case we still want to use the binding for the slider, if the slider is focused.
	 * 
	 * If nested tabs are used, the action map is filled here as well.
	 * 
	 * Also sets the tooltip of the channel selection combobox in the soundcheck window.
	 */
	public void postprocess() {
		
		// don't interpret SPACE and/or ENTER when used for something special
		TreeSet<KeyBinding> specialBindingsInUse = new TreeSet<>();
		if (isSpaceUsed)
			specialBindingsInUse.add(new KeyBinding(KeyEvent.VK_SPACE, 0));
		if (isEnterUsed)
			specialBindingsInUse.add(new KeyBinding(KeyEvent.VK_ENTER, 0));
		
		// something to ignore at all?
		if (specialBindingsInUse.size() > 0)
			ignoreKeysInChildren(component, specialBindingsInUse);
		
		// handle slider-specific keys when sliders are focused
		TreeSet<KeyBinding> sliderBindings = new TreeSet<>();
		sliderBindings.add( new KeyBinding(KeyEvent.VK_LEFT,      0) );
		sliderBindings.add( new KeyBinding(KeyEvent.VK_RIGHT,     0) );
		sliderBindings.add( new KeyBinding(KeyEvent.VK_UP,        0) );
		sliderBindings.add( new KeyBinding(KeyEvent.VK_DOWN,      0) );
		sliderBindings.add( new KeyBinding(KeyEvent.VK_PAGE_UP,   0) );
		sliderBindings.add( new KeyBinding(KeyEvent.VK_PAGE_DOWN, 0) );
		sliderBindings.add( new KeyBinding(KeyEvent.VK_HOME,      0) );
		sliderBindings.add( new KeyBinding(KeyEvent.VK_END,       0) );
		handleSlidersInChildren(component, sliderBindings);
		
		// handle nested tabs, if necessary
		if (hasNestedTabs)
			postprocessNestedTabs();
		
		// show warninig, if there are conflicts
		warnAboutConflicts();
		
		// add tooltip to set a combobox
		if (comboboxToSetChannel != null) {
			addTooltip(comboboxToSetChannel, Dict.TT_KEY_CBX_CHANNEL_SELECT, Dict.TT_KEY_CBX_CHANNEL_SELECT);
		}
	}
	
	/**
	 * Checks if conflicting key bindings exist in the same instance and warns if conflicts are found.
	 */
	private void warnAboutConflicts() {
		if (conflicts.size() > 0) {
			
			// create message window
			ErrorMsgView msgView;
			if (window instanceof JDialog)
				msgView = new ErrorMsgView((JDialog) window);
			else
				msgView = new ErrorMsgView((JFrame) window);
			
			// build warning message
			StringBuilder msgStr = new StringBuilder("<html>" + Dict.get(Dict.WARNING_KEY_BINDING_CONFLICT) + ":");
			msgStr.append("<ul>");
			for (KeyBinding binding : conflicts) {
				msgStr.append( "<li>" + binding.getDescription() + "</li>" );
			}
			msgStr.append("</ul>");
			msgView.init( msgStr.toString() );
		}
	}
	
	/**
	 * Adds a key binding related part to the tooltip of the component or sets a new tooltip, if no tooltip is set.
	 * 
	 * @param c       the component
	 * @param id      the key binding ID (or **null** in case of the channel combobox in the soundcheck window)
	 * @param ttType  the translation ID of the tooltip type
	 */
	public static void addTooltip(JComponent c, String id, String ttType) {
		StringBuilder tt = new StringBuilder("<html>");
		
		// already a tooltip used? - add empty line and append it
		String old = c.getToolTipText();
		if (old != null || "".equals(old)) {
			
			// old tooltext uses html?
			if (old.startsWith("<html>")) {
				tt = new StringBuilder("");
				
				// don't destroy normal/bold styles if the old tooltext uses <b>
				if (old.contains("<b>")) {
					tt = new StringBuilder(old + "<br><br>");
				}
				else {
					// no bold used before - old part bold, new part normal
					tt = new StringBuilder("<html><b>" + old + "</b><br><br>");
				}
			}
			else {
				// no html used before - old part bold, new part normal
				tt.append("<b>" + old + "</b><br><br>");
			}
		}
		
		// append the key binding specific part
		String bindingSpecific = getBindingSpecificToolTip(id, ttType);
		tt.append(bindingSpecific);
		
		// add or replace tooltip
		c.setToolTipText( tt.toString() );
	}
	
	/**
	 * Create and return the key binding specific part of a tooltip text.
	 * 
	 * @param id      the key binding ID (or **null** in case of the channel combobox in the soundcheck window)
	 * @param ttType  the translation ID of the tooltip type
	 * @return the key binding specific part of a tool tip.
	 */
	private static String getBindingSpecificToolTip(String id, String ttType) {
		
		// key binding type description
		StringBuilder tt = new StringBuilder( Dict.get(ttType) );
		
		// special case: channel combobox in the soundcheck window?
		if (Dict.TT_KEY_CBX_CHANNEL_SELECT.equals(id)) {
			tt.append("<br><br>");
			for (int channel : bindingsToCbxChannel.keySet()) {
				tt.append("&nbsp;&nbsp;&nbsp;- " + Dict.get(Dict.TT_KEY_CBX_SELECT_CHANNEL_N) + channel + ":<br>");
				for (KeyBinding binding : bindingsToCbxChannel.get(channel)) {
					tt.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;- " + binding.getDescription() + "<br>");
				}
				tt.append("</ul></li>");
			}
		}
		else {
			// regular case: add one line for each key binding
			tt.append("<ul>");
			TreeSet<KeyBinding> keyBindings = Config.getKeyBindings(id);
			for (KeyBinding binding : keyBindings) {
				tt.append("<li>" + binding.getDescription() + "</li>");
			}
			tt.append("</ul>");
		}
		
		return tt.toString();
	}
	
	/**
	 * Ignores special bindings in the children of the given container, recursively.
	 * This is needed if ENTER or SPACE is used for something else. Then we want to prevent
	 * the child elements to consume the binding first.
	 * 
	 * Needed e.g. for the player to enable SPACE for PLAY/PAUSE even if a button is focused.
	 * 
	 * @param c            the container
	 * @param keyBindings  the key binding to be ignored by the children of the container
	 */
	private void ignoreKeysInChildren(Container c, TreeSet<KeyBinding> keyBindings) {
		Component[] children = c.getComponents();
		for (Component child : children) {
			
			// ignore some keys for this child (but not for the root component itself)
			if (c != component && child instanceof JComponent) {
				ignoreKeysInComponent((JComponent) child, keyBindings);
			}
			
			// recursion: ignore for children
			if (child instanceof Container)
				ignoreKeysInChildren((Container) child, keyBindings);
		}
	}
	
	/**
	 * Ignores special bindings in the component.
	 * This is needed if ENTER or SPACE is used for something else.
	 * 
	 * @param c            the component
	 * @param keyBindings  the bindings to be ignored by that component
	 */
	private void ignoreKeysInComponent(JComponent c, TreeSet<KeyBinding> keyBindings) {
		InputMap inputMap = c.getInputMap( JComponent.WHEN_FOCUSED );
		
		for (KeyBinding binding : keyBindings) {
			int keyCode   = binding.getKeyCode();
			int modifiers = binding.getModifiers();
			KeyStroke keyStroke = KeyStroke.getKeyStroke(keyCode, modifiers, false);
			inputMap.put(keyStroke, "none");
		}
	}
	
	/**
	 * Adds some slider-specific bindings recursively to sliders in the decendents of the given container.
	 * 
	 * This is needed for some special cases, if a slider-specific binding is used for something else.
	 * In this case we still want to use the binding for the slider, if the slider is focused.
	 * 
	 * @param c               the container to be checked for sliders recursively
	 * @param sliderBindings  the slider-specific bindings (arrow keys, HOME and END)
	 */
	private void handleSlidersInChildren(Container c, TreeSet<KeyBinding> sliderBindings) {
		Component[] children = c.getComponents();
		for (Component child : children) {
			
			// slider? - handle slider keys
			if (child instanceof JSlider) {
				JSlider slider = (JSlider) child;
				
				// key bindings for sliders
				InputMap  inputMap  = slider.getInputMap( JComponent.WHEN_FOCUSED );
				ActionMap actionMap = slider.getActionMap();
				for (KeyBinding binding : sliderBindings) {
					int keyCode   = binding.getKeyCode();
					int modifiers = binding.getModifiers();
					KeyStroke keyStroke = KeyStroke.getKeyStroke(keyCode, modifiers, false);
					if (KeyEvent.VK_LEFT == keyCode || KeyEvent.VK_DOWN == keyCode)
						inputMap.put(keyStroke, "slider_minus");
					else if (KeyEvent.VK_RIGHT == keyCode || KeyEvent.VK_UP == keyCode)
						inputMap.put(keyStroke, "slider_plus");
					else if (KeyEvent.VK_HOME == keyCode)
						inputMap.put(keyStroke, "slider_min");
					else if (KeyEvent.VK_END == keyCode)
						inputMap.put(keyStroke, "slider_max");
				}
				
				// actions for sliders
				actionMap.put( "slider_plus",  getSliderAction(slider, "slider_plus")  );
				actionMap.put( "slider_minus", getSliderAction(slider, "slider_minus") );
				actionMap.put( "slider_min",   getSliderAction(slider, "slider_min")   );
				actionMap.put( "slider_max",   getSliderAction(slider, "slider_max")   );
			}
			
			// recursion: handle sliders in children
			if (child instanceof Container)
				handleSlidersInChildren((Container) child, sliderBindings);
		}
	}
	
	/**
	 * Returns an action for a slider-specific key-binding that can be put into the slider's action map.
	 * 
	 * @param slider      the slider
	 * @param actionName  an identifier that describes how the slider value must be changed
	 * @return
	 */
	private Action getSliderAction(JSlider slider, String actionName) {
		
		return new AbstractAction() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				
				if (mustIgnore(e))
					return;
				
				// calculate the new slider value
				int min      = slider.getMinimum();
				int max      = slider.getMaximum();
				int oldVal   = slider.getValue();
				int stepSize = slider.getMinorTickSpacing();
				int newVal;
				if ("slider_plus".equals(actionName))
					newVal = oldVal + stepSize;
				else if ("slider_minus".equals(actionName))
					newVal = oldVal - stepSize;
				else if ("slider_min".equals(actionName))
					newVal = min;
				else if ("slider_max".equals(actionName))
					newVal = max;
				else
					newVal = oldVal;
				
				// check limits
				if (newVal > max)
					newVal = max;
				else if (newVal < min)
					newVal = min;
				
				// nothing changed? - finished
				if (newVal == oldVal)
					return;
				
				// set the slider
				setSliderAndInformListeners(slider, newVal);
			}
		};
	}
	
	/**
	 * Sets a slider after a key has been pressed.
	 * Invokes the change listener and sets valueIsAdjusting accordingly.
	 * In this project this is necessary because slider changes are only handled if
	 * getValueIsAdjusting() returns true.
	 * 
	 * @param slider  the slider to be adjusted.
	 * @param value   the value to be set.
	 */
	private void setSliderAndInformListeners(JSlider slider, int value) {
		slider.setValueIsAdjusting( true );
		slider.setValue(value);
		for ( ChangeListener listener : slider.getChangeListeners() ) {
			listener.stateChanged( new ChangeEvent(slider) );
		}
		slider.setValueIsAdjusting( false );
	}
	
	/**
	 * Fills the action maps for key bindings in windows with (nested) tabs.
	 * This method is used for the info window.
	 * It's called after all bindings have been added and the input maps are filled.
	 * 
	 * The same key binding could possibly exist for different actions inside of the same container.
	 * In this case we need to check which (nested) tabs are open.
	 * Then we need to process the binding according to it's priority:
	 * 
	 * - visible level-3 elements first
	 * - visible level-2 tabs second
	 * - level-1 tabs last
	 * 
	 * The action event doesn't hold the information which key binding has been pressed or for
	 * which component it has been added.
	 * So here we add one action for each binding and binding ID. So we are still able to get
	 * that information.
	 */
	private void postprocessNestedTabs() {
		
		// fill all needed action maps
		for (String bindingStr : bindingToIds.keySet()) {
			for (String id : tabToLevel.keySet()) {
				
				actionMap.put("nested_tabs:" + id + ":" + bindingStr, new AbstractAction() {
					
					private static final long serialVersionUID = 1L;
					
					@Override
					public void actionPerformed(ActionEvent e) {
						
						if (mustIgnore(e))
							return;
						
						// level-3 component (non-tab element inside of a sub-tab)?
						HashSet<String> candidates = bindingToIds.get(bindingStr);
						for (String candidateId : candidates) {
							int level = tabToLevel.get(candidateId);
							if (3 == level) {
								
								// level-3 element visible? - apply key binding
								JComponent comp = lvl3Components.get(candidateId);
								if (comp != null && comp.isShowing()) {
									
									// add element-specific key binding
									if (comp instanceof FilterIconWithLabel)
										((FilterIconWithLabel) comp).open();
									else if (comp instanceof FilterIcon)
										((FilterIcon) comp).open();
									else if (comp instanceof MidicaButton)
										((MidicaButton) comp).doClick();
									else if (comp instanceof MidicaTree)
										((MidicaTree) comp).requestFocus();
									else if (comp instanceof MidicaTable)
										((MidicaTable) comp).requestFocus();
									else if (comp instanceof JCheckBox) {
										JCheckBox cbx = (JCheckBox) comp;
										if (cbx.isSelected())
											cbx.setSelected(false);
										else
											cbx.setSelected(true);
									}
									else if (comp instanceof JTextField) {
										JTextField fld = (JTextField) comp;
										if (fld.isFocusable()) {
											fld.requestFocus();
										}
									}
									
									
									return;
								}
							}
						}
						
						// get visible level-1 tab
						int visibleIndexLvl1 = tabbedPaneLvl1.getSelectedIndex();
						
						// get possible target tabs
						for (String candidateId : candidates) {
							
							// get our candidate's level
							int level = tabToLevel.get(candidateId);
							
							// is our tab a visible level-2 tab? - select it
							if (2 == level) {
								HashSet<String> visibleLvl2Tabs = indexToLvl2Tabs.get(visibleIndexLvl1);
								if (visibleLvl2Tabs != null && visibleLvl2Tabs.contains(candidateId)) {
									int[] indices   = tabToIndices.get(candidateId);
									int   indexLvl1 = indices[0];
									int   indexLvl2 = indices[1];
									JTabbedPane pane = indexToLvl2Pane.get(indexLvl1);
									pane.setSelectedIndex(indexLvl2);
									return;
								}
							}
						}
						
						// no matching level-2 tab found.
						// check for level-1
						for (String candidateId : candidates) {
							int level = tabToLevel.get(candidateId);
							if (1 == level) {
								int[] indices   = tabToIndices.get(candidateId);
								int   indexLvl1 = indices[0];
								tabbedPaneLvl1.setSelectedIndex(indexLvl1);
							}
						}
					}
				});
			}
		}
	}
}
