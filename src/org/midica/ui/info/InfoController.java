/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.info;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import org.midica.config.Config;
import org.midica.config.Dict;
import org.midica.config.KeyBinding;
import org.midica.config.Laf;
import org.midica.midi.SequenceAnalyzer;
import org.midica.ui.model.MessageTableModel;
import org.midica.ui.ErrorMsgView;
import org.midica.ui.model.IMessageType;
import org.midica.ui.model.SingleMessage;
import org.midica.ui.tablesorter.MessageTableSorter;
import org.midica.ui.model.MessageTreeNode;
import org.midica.ui.model.MidicaTreeModel;
import org.midica.ui.model.MidicaTreeNode;
import org.midica.ui.widget.MidicaButton;
import org.midica.ui.widget.MidicaTable;
import org.midica.ui.widget.MidicaTree;

/**
 * Controller for the info window.
 * Acts as a listener to events that occur in the info window.
 * 
 * @author Jan Trukenmüller
 */
public class InfoController implements WindowListener, ActionListener, TreeSelectionListener,
	ListSelectionListener, ItemListener, DocumentListener, FocusListener, RowSorterListener {
	
	public static final String CMD_COLLAPSE                = "cmd_collapse";
	public static final String CMD_EXPAND                  = "cmd_expand";
	public static final String CMD_ADD_KEY_BINDING         = "cmd_add_key_binding";
	public static final String CMD_REMOVE_KEY_BINDING      = "cmd_remove_key_binding";
	public static final String CMD_RESET_KEY_BINDING_ID    = "cmd_reset_key_binding_id";
	public static final String CMD_RESET_KEY_BINDING_GLOB  = "cmd_reset_key_binding_glob";
	public static final String NAME_TREE_BANKS_TOTAL       = "name_tree_banks_total";
	public static final String NAME_TREE_BANKS_PER_CHANNEL = "name_tree_banks_per_channel";
	public static final String NAME_TREE_MESSAGES          = "name_tree_messages";
	public static final String NAME_TREE_KEYBINDINGS       = "name_tree_keybindings";
	public static final String NAME_KB_FILTER              = "name_kb_filter";
	
	private InfoView view = null;
	
	// tree models
	private MidicaTreeModel tmBanksTotal      = null;
	private MidicaTreeModel tmBanksPerChannel = null;
	private MidicaTreeModel tmMessages        = null;
	private MidicaTreeModel tmKeyBindings     = null;
	
	private String     selectedKeyBindingId = null;
	private KeyBinding pressedKeyBinding    = null;
	
	/**
	 * Creates a new instance of the controller for the given info view.
	 * This is called during the initialization of the info view.
	 * 
	 * @param view  info view to which the controller is connected.
	 */
	public InfoController( InfoView view ) {
		this.view = view;
	}
	
	/**
	 * Receives the specified tree model so that it's tree can be
	 * collapsed/expanded later.
	 * 
	 * @param treeModel The tree model to be set.
	 * @param name      The name associated with this tree.
	 */
	public void setTreeModel(MidicaTreeModel treeModel, String name) {
		if ( NAME_TREE_BANKS_TOTAL.equals(name) ) {
			tmBanksTotal = treeModel;
		}
		else if ( NAME_TREE_BANKS_PER_CHANNEL.equals(name) ) {
			tmBanksPerChannel = treeModel;
		}
		else if ( NAME_TREE_MESSAGES.equals(name) ) {
			tmMessages = treeModel;
		}
	}
	
	/**
	 * Creates the key binding tree model, if not yet done, and returns it.
	 * 
	 * @return the key binding tree model
	 */
	public MidicaTreeModel getKeyBindingTreeModel() {
		if (tmKeyBindings == null)
			tmKeyBindings = Config.getKeyBindingTreeModel();
		return tmKeyBindings;
	}
	
	/**
	 * Handles all button clicks from the info view window.
	 * 
	 * Those are:
	 * 
	 * - pushing a collapse-all button
	 * - pushing an expand-all button
	 * - pushing a show-in-tree button in the message filter
	 * - pushing one of the buttons in the key binding window:
	 *     - add key binding
	 *     - remove one key binding
	 *     - reset key bindings for one action
	 *     - reset all key bindings globally
	 * 
	 * @param e    The invoked action event.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		String       cmd  = e.getActionCommand();
		MidicaButton btn  = (MidicaButton) e.getSource();
		String       name = btn.getName();
		
		// collapse or expand all nodes of a tree
		if ( CMD_COLLAPSE.equals(cmd) || CMD_EXPAND.equals(cmd) ) {
			
			boolean mustExpand = CMD_EXPAND.equals( cmd );
			if ( NAME_TREE_BANKS_TOTAL.equals(name) )
				tmBanksTotal.expandOrCollapse( mustExpand );
			else if ( NAME_TREE_BANKS_PER_CHANNEL.equals(name) )
				tmBanksPerChannel.expandOrCollapse( mustExpand );
			else if ( NAME_TREE_MESSAGES.equals(name) )
				tmMessages.expandOrCollapse( mustExpand );
			else if ( NAME_TREE_KEYBINDINGS.equals(name) )
				tmKeyBindings.expandOrCollapse( mustExpand );
		}
		
		// show a message in the tree (after it has been clicked in the table)
		else if ( InfoView.FILTER_BTN_SHOW_TREE.equals(cmd) ) {
			showInTree();
		}
		
		// add a key binding
		else if ( CMD_ADD_KEY_BINDING.equals(cmd) ) {
			
			// no key binding entered?
			if (null == pressedKeyBinding) {
				ErrorMsgView msgView = new ErrorMsgView(view);
				msgView.init( Dict.get(Dict.KB_ERROR_NO_BINDING_PRESSED) );
			}
			
			// add key binding
			Config.addKeyBinding(selectedKeyBindingId, pressedKeyBinding);
			
			// refresh details
			displayKeyBindingDetailsIfPossible( tmKeyBindings.getTree() );
			
			// update tree
			updateKeyBindingTree();
		}
		
		// remove a key binding
		else if ( CMD_REMOVE_KEY_BINDING.equals(cmd) ) {
			
			// remove it
			Config.removeKeyBinding(selectedKeyBindingId, name);
			
			// refresh details
			displayKeyBindingDetailsIfPossible( tmKeyBindings.getTree() );
			
			// update tree
			updateKeyBindingTree();
		}
		
		// reset key bindings for one action
		else if ( CMD_RESET_KEY_BINDING_ID.equals(cmd) ) {
			
			// reset selected bindings
			Config.resetKeyBindingsToDefault(selectedKeyBindingId);
			
			// refresh details
			displayKeyBindingDetailsIfPossible( tmKeyBindings.getTree() );
			
			// update tree
			updateKeyBindingTree();
		}
		
		// reset all key bindings globally
		else if ( CMD_RESET_KEY_BINDING_GLOB.equals(cmd) ) {
			
			// reset all bindings
			Config.resetAllKeyBindingsToDefault();

			// refresh details
			displayKeyBindingDetailsIfPossible( tmKeyBindings.getTree() );
			
			// update tree
			updateKeyBindingTree();
		}
	}
	
	/**
	 * Handles selection changes in the message or key binding tree.
	 * 
	 * **Message tree**
	 * 
	 * - Filters the messages shown in the table. (Only if the filter checkbox
	 *   for selected nodes is selected.)
	 * - Changes the details area.
	 * 
	 * The details area is only filled if exactly one node is selected and
	 * this is a leaf node.
	 * In all other cases it will just be emptied.
	 * 
	 * **Key binding tree**
	 * 
	 * - Shows the currently selected key bindings for the chosen ID in the detail area.
	 *   (Only if exactly one leaf node is selected.)
	 * - Empties the detail area, if not exactly one leaf node is selected.
	 * 
	 * @param e    The event to be handled.
	 */
	@Override
	public void valueChanged(TreeSelectionEvent e) {
		MidicaTree tree = (MidicaTree) e.getSource();
		String     name = tree.getName();
		
		// message tree
		if ( name.equals(NAME_TREE_MESSAGES) ) {
			
			// prepare widgets
			HashMap<String, JComponent> widgets = getMsgFilterWidgetsIfReady();
			if ( null == widgets )
				return;
			JCheckBox cbxNodes = (JCheckBox) widgets.get( InfoView.FILTER_CBX_NODE );
			
			// apply filter changes to the message table
			boolean mustFilterNodes = cbxNodes.isSelected();
			if (mustFilterNodes) {
				filterMessages();
			}
			
			// make the details area empty so that it's ready for refilling
			view.cleanMsgDetails();
			
			// display details, if exactly one leaf node is selected now
			displayDetailsIfPossible( true );
		}
		
		// key bindings tree
		else if ( name.equals(NAME_TREE_KEYBINDINGS) ) {
			
			// show configured binding details
			displayKeyBindingDetailsIfPossible(tree);
		}
	}
	
	/**
	 * Handles selection changes in the message table.
	 * 
	 * If a row (message) is selected now:
	 * 
	 * - displays the message details in the detail area
	 * - shows the selected message in the tree (only if the auto-show checkbox
	 *   is selected)
	 * 
	 * Otherwise:
	 * 
	 * - removes content from the detail area
	 * 
	 * At the end: activates or deactivates the button to show the message
	 * type in the tree. This depends also on the auto-show checkbox.
	 * 
	 * @param event  Table row selection event.
	 */
	@Override
	public void valueChanged( ListSelectionEvent event ) {
		
		// prevent handle 2 events after a selection by mouse click
		if ( event.getValueIsAdjusting() )
			return;
		
		// not a table row selection?
		boolean isTableSelection = event.getSource() instanceof DefaultListSelectionModel;
		if ( ! isTableSelection )
			return;
		
		// empty details
		view.cleanMsgDetails();
		
		// display details, if a row is selected now
		displayDetailsIfPossible( false );
		
		// row selected?
		SingleMessage singleMessage = getSelectedMessage();
		if (singleMessage != null) {
			
			// show in the tree, if needed
			HashMap<String, JComponent> widgets = getMsgFilterWidgetsIfReady();
			JCheckBox cbxAutoShow = (JCheckBox) widgets.get( InfoView.FILTER_CBX_AUTO_SHOW );
			boolean   isAutoShow  = cbxAutoShow.isSelected();
			if (isAutoShow) {
				showInTree();
			}
		}
		
		// activate or deactivate the button
		updateShowInTreeButtonActivity();
	}
	
	/**
	 * Handles checkbox events from message filter checkboxes.
	 * 
	 * If the limit-ticks checkbox has been changed:
	 * 
	 * - activates/deactivates the from-ticks and to-ticks text fields
	 * - changes the color of the from/to text fields
	 * 
	 * If the limit-tracks checkbox has been changed:
	 * 
	 * - activates/deactivates the tracks text field
	 * - changes the color of the tracks text field
	 * 
	 * If the channel-dependent-messages checkbox has been changed:
	 * 
	 * - checks/unchecks all channel checkboxes
	 * 
	 * If one of the channel checkboxes has been changed:
	 * 
	 * - checks/unchecks the channel-dependent-messages checkbox
	 * 
	 * At the end: applies the filter according to the changes.
	 * 
	 * If the auto-show checkbox has been changed, no special action
	 * is necessary. The filter will be re-applied, and
	 * {@link #filterMessages()} will re-select the previously selected
	 * message, if possible. That will fire a {@link ListSelectionEvent}.
	 * This event is handled by {@link #valueChanged(ListSelectionEvent)},
	 * where the button activity is updated and {@link #showInTree()} is
	 * called if needed. So we don't need to do that here.
	 * 
	 * @param e    Event object.
	 */
	@Override
	public void itemStateChanged( ItemEvent e ) {
		
		// get name, component and checked/unchecked
		String    name      = ((Component) e.getSource()).getName();
		JCheckBox cbx       = (JCheckBox) e.getSource();
		boolean   isChecked = cbx.isSelected();
		
		if ( null == name )
			return;
		
		// make sure that everything is initialized
		HashMap<String, JComponent> widgets = getMsgFilterWidgetsIfReady();
		if ( null == widgets )
			return;
		
		// limit-ticks checkbox - activate or deactivate the tick text fields
		if ( InfoView.FILTER_CBX_LIMIT_TICKS.equals(name) ) {
			JTextField txtFrom = (JTextField) widgets.get( InfoView.FILTER_TXT_FROM_TICKS );
			JTextField txtTo   = (JTextField) widgets.get( InfoView.FILTER_TXT_TO_TICKS   );
			if (isChecked) {
				// activate text fields
				txtFrom.setEnabled(true);
				txtTo.setEnabled(true);
				
				// adjust text field colors
				checkTextFields( createFakeTxtFieldChange() );
			}
			else {
				// deactivate text fields
				txtFrom.setEnabled(false);
				txtTo.setEnabled(false);
				
				// neutralize text field colors
				txtFrom.setBackground( Laf.COLOR_NORMAL );
				txtTo.setBackground( Laf.COLOR_NORMAL );
			}
		}
		
		// limit-tracks checkbox - activate or deactivate the track text field
		else if ( InfoView.FILTER_CBX_LIMIT_TRACKS.equals(name) ) {
			JTextField txtTrack = (JTextField) widgets.get( InfoView.FILTER_TXT_TRACKS );
			if (isChecked) {
				// activate text field
				txtTrack.setEnabled( true );
				
				// adjust text field colors
				checkTextFields( createFakeTxtFieldChange() );
			}
			else {
				// deactivate text field
				txtTrack.setEnabled( false );
				
				// neutralize text field colors
				txtTrack.setBackground( Laf.COLOR_NORMAL );
			}
		}
		
		// channel-dependant checkbox - activate/deactivate all checkboxes
		else if ( InfoView.FILTER_CBX_CHAN_DEP.equals(name) ) {
			
			for ( int channel = 0; channel < 16; channel++ ) {
				JCheckBox cbxChannel = (JCheckBox) widgets.get( InfoView.FILTER_CBX_CHAN_PREFIX + channel );
				
				cbxChannel.removeItemListener( this );
				if (isChecked)
					cbxChannel.setSelected( true );
				else
					cbxChannel.setSelected( false );
				cbxChannel.addItemListener( this );
			}
		}
		
		// channel checkbox - adjust channel-dependent checkbox:
		// - activate it, if one or more channel checkboxes are checked
		// - deactivate it, if no channel checkbox is selected
		else if ( name.startsWith(InfoView.FILTER_CBX_CHAN_PREFIX) ) {
			
			// is at least one channel selected?
			boolean isChannelSelected = false;
			for ( int channel = 0; channel < 16; channel++ ) {
				JCheckBox cbxChannel = (JCheckBox) widgets.get( InfoView.FILTER_CBX_CHAN_PREFIX + channel );
				if ( cbxChannel.isSelected() ) {
					isChannelSelected = true;
					break;
				}
			}
			
			// select/deselect the channel-dependent checkbox
			JCheckBox cbxChDep = (JCheckBox) widgets.get( InfoView.FILTER_CBX_CHAN_DEP );
			cbxChDep.removeItemListener( this );
			if (isChannelSelected)
				cbxChDep.setSelected( true );
			else
				cbxChDep.setSelected( false );
			cbxChDep.addItemListener( this );
		}
		
		// apply filter
		filterMessages();
	}
	
	/**
	 * Handles text field changes.
	 * 
	 * @param e    Event object.
	 */
	@Override
	public void changedUpdate(DocumentEvent e) {
		handleTextFieldChange(e);
	}
	
	/**
	 * Handles text field changes.
	 * 
	 * @param e    Event object.
	 */
	@Override
	public void insertUpdate(DocumentEvent e) {
		handleTextFieldChange(e);
	}
	
	/**
	 * Handles text field changes.
	 * 
	 * @param e    Event object.
	 */
	@Override
	public void removeUpdate(DocumentEvent e) {
		handleTextFieldChange(e);
	}
	
	/**
	 * Handles focus events of the message table and the message tree.
	 * 
	 * If one of these items gets the focus, this method tries to fill the
	 * details area with the selected content of that item.
	 * 
	 * @param e  Event object.
	 */
	@Override
	public void focusGained( FocusEvent e ) {
		
		if ( e.getSource() instanceof MidicaTable ) {
			displayDetailsIfPossible( false );
		}
		else if ( e.getSource() instanceof MidicaTree ) {
			displayDetailsIfPossible( true );
		}
	}
	
	@Override
	public void focusLost( FocusEvent e ) {
		// nothing more to do
	}
	
	@Override
	public void sorterChanged(RowSorterEvent e) {
		filterMessages();
	}
	
	/**
	 * Creates and returns a key listener to handle the detection of a new key binding that's pressed
	 * while the key binding form text field is focused.
	 * 
	 * @param field  the text field that must be focused to receive a new key binding
	 * @return the key listener
	 */
	public KeyAdapter createKeyListener(JTextField field) {
		
		return new KeyAdapter() {
			private int     lastPressedKeyCode = 0;
			private int     lastPressedMods    = 0;
			private boolean canDetect          = true;
			
			@Override
			public void keyPressed(KeyEvent e) {
				int keyCode = e.getKeyCode();
				
				// ignore an already presssed key, if only a modifier has been released
				if ( ! canDetect && keyCode == lastPressedKeyCode ) {
					e.consume();
					return;
				}
				
				canDetect          = true;
				lastPressedKeyCode = e.getKeyCode();
				lastPressedMods    = 0x00;
				lastPressedMods   |= ( e.isShiftDown()    ? InputEvent.SHIFT_DOWN_MASK     : 0x00 );
				lastPressedMods   |= ( e.isControlDown()  ? InputEvent.CTRL_DOWN_MASK      : 0x00 );
				lastPressedMods   |= ( e.isAltDown()      ? InputEvent.ALT_DOWN_MASK       : 0x00 );
				lastPressedMods   |= ( e.isAltGraphDown() ? InputEvent.ALT_GRAPH_DOWN_MASK : 0x00 );
				lastPressedMods   |= ( e.isMetaDown()     ? InputEvent.META_DOWN_MASK      : 0x00 );
				e.consume();
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				
				// ignore the key binding used to focus the text field
				if (0 == lastPressedKeyCode) {
					e.consume();
					return;
				}
				
				// Only detect, if the first key is released.
				if (canDetect) {
					boolean lastIsModifier = lastPressedKeyCode == KeyEvent.VK_ALT
					                      || lastPressedKeyCode == KeyEvent.VK_ALT_GRAPH
					                      || lastPressedKeyCode == KeyEvent.VK_CONTROL
					                      || lastPressedKeyCode == KeyEvent.VK_SHIFT
					                      || lastPressedKeyCode == KeyEvent.VK_META;
					if ( ! lastIsModifier ) {
						
						// show and remember key binding
						KeyBinding binding = new KeyBinding(lastPressedKeyCode, lastPressedMods);
						field.setText( binding.getDescription() );
						pressedKeyBinding = binding;
					}
				}
				canDetect = false;
				e.consume();
			}
			
			@Override
			public void keyTyped(KeyEvent e) {
				e.consume();
			}
		};
	}
	
	/**
	 * Shows the message details from the selected tree node or table row in
	 * the details area.
	 * 
	 * In the following cases this method has no effect:
	 * 
	 * - if no MIDI sequence has been loaded yet
	 * - if the details from a tree node are to be shown but none or more
	 *   than one nodes are currently selected
	 * - if the details from a tree node are to be shown but the selected node
	 *   is not a leaf node
	 * - if the details from a table row are to be shown but no row is selected
	 * 
	 * @param fromTree  **true** to display the details from a tree node;
	 *                  **false** to display the details from a table row
	 */
	private void displayDetailsIfPossible( boolean fromTree ) {
		
		// display details from the tree, if possible
		if (fromTree) {
			
			// not exactly 1 node selected?
			MidicaTree tree  = view.getMsgTree();
			int        count = tree.getSelectionCount();
			if ( count != 1 )
				return;
			
			// selected node is not a leaf?
			MessageTreeNode node = (MessageTreeNode) tree.getLastSelectedPathComponent();
			if ( node.getChildCount() > 0 )
				return;
			
			// selected node is the root node (no sequence loaded)
			if ( node.isRoot() )
				return;
			
			// (re)fill the details area
			view.cleanMsgDetails();
			view.fillMsgDetails( node );
		}
		
		// display details from the table, if possible
		else {
			SingleMessage singleMessage = getSelectedMessage();
			if (singleMessage != null) {
				
				// (re)fill the details area
				view.cleanMsgDetails();
				view.fillMsgDetails(singleMessage);
			}
		}
	}
	
	/**
	 * Shows the key bindings, configured for the currently selected ID in the tree.
	 * This is done only if exactly one leaf node is currently selected.
	 * 
	 * @param tree  the key binding tree
	 */
	private void displayKeyBindingDetailsIfPossible(MidicaTree tree) {
		
		selectedKeyBindingId = null;
		pressedKeyBinding    = null;
		
		// empty the details area so that it can be (re)filled (again)
		view.cleanKeyBindingDetails();
		view.resetResetWidgetsForSelectedKeyBindingAction();
		
		int count = tree.getSelectionCount();
		
		// not exactly 1 node selected?
		if ( count != 1 )
			return;
		
		// selected node is not a leaf?
		MidicaTreeNode node = (MidicaTreeNode) tree.getLastSelectedPathComponent();
		if ( node.getChildCount() > 0 )
			return;
		
		// display configured key bindings
		String         id       = node.getId();
		MidicaTreeNode catNode  = (MidicaTreeNode) node.getParent();
		String         category = catNode.getName();
		selectedKeyBindingId    = id;
		view.fillKeyBindingDetails(id, category);
	}
	
	/**
	 * Handles text field change events.
	 * 
	 * The text field causing the event can be one of the following:
	 * 
	 * - the key binding tree filter
	 * - one of the MIDI message filters (from ticks, to ticks, tracks)
	 * 
	 * In the first case the tree is filtered. In the second case, the
	 * text field contents are checked (by calling
	 * {@link #checkTextFields(DocumentEvent)}).
	 * 
	 * @param e    Event object.
	 */
	private void handleTextFieldChange(DocumentEvent e) {
		
		String fieldName = e.getDocument().getProperty("name").toString();
		if (NAME_KB_FILTER.equals(fieldName)) {
			filterKeyBindingTree();
		}
		else {
			checkTextFields(e);
		}
	}
	
	/**
	 * Filters the tree according to the text from the tree filter field.
	 * Only filters leaf nodes but no categories.
	 * Categories or the root node are colored in gray, if they don't contain
	 * any matching leaf nodes.
	 * 
	 * The filter is checked against the action description and all
	 * associated key binding descriptions.
	 * 
	 * Example:
	 * To look for the action to close error message windows, you can
	 * filter for one of the following strings:
	 * 
	 * - "Close the message window"
	 * - "Enter"
	 * - "Escape"
	 * - "Space"
	 */
	private void filterKeyBindingTree() {
		JTextField filter    = view.getKeybindingTreeFilter();
		String     filterStr = filter.getText().toLowerCase();
		MidicaTree tree      = tmKeyBindings.getTree();
		
		// add filter functionality
		tree.setCellRenderer(new DefaultTreeCellRenderer() {
			
			private static final long serialVersionUID = 1L;
			
			private JLabel nullLabel = new JLabel();
			
			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {
				
				Component defaultComponent = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
				
				// don't filter, if the filter text is empty
				if ("".equals(filterStr))
					return defaultComponent;
				
				// category?
				// don't filter but colorize
				MidicaTreeNode node = (MidicaTreeNode) value;
				if ( ! node.isLeaf() ) {
					if ( ! hasMatchingDescendant(node) ) {
						defaultComponent.setForeground(Laf.COLOR_TREE_NODE_INACTIVE);
					}
					return defaultComponent;
				}
				
				// leaf - filter matches?
				if (filterMatches(node))
					return defaultComponent;
				
				// leaf - filter doesn't match
				return nullLabel;
			}
			
			/**
			 * Determins if the given node has a descending leaf matching the filter.
			 * 
			 * @param node  the node to be checked
			 * @return **true**, if a descending leaf is found, otherwise **false**
			 */
			private boolean hasMatchingDescendant(MidicaTreeNode node) {
				int count = node.getChildCount();
				for (int i = 0; i < count; i++) {
					MidicaTreeNode child = (MidicaTreeNode) node.getChildAt(i);
					if (child.isLeaf()) {
						if (filterMatches(child)) {
							return true;
						}
					}
					else if (hasMatchingDescendant(child)) {
						return true;
					}
				}
				
				return false;
			}
			
			/**
			 * Checks if the given leaf node matches the filter.
			 * The filter is matching, if either the action description matches or one of
			 * the associated key binding descriptions.
			 * 
			 * @param node  the leaf node to be checked
			 * @return **true**, if the filter matches, otherwise **false**.
			 */
			private boolean filterMatches(MidicaTreeNode node) {
				
				// check action name
				String action = node.getName();
				if (action.toLowerCase().contains(filterStr))
					return true;
				
				// check key binding description
				String id = node.getId();
				for (KeyBinding binding : Config.getKeyBindings(id)) {
					if (binding.getDescription().toLowerCase().contains(filterStr)) {
						return true;
					}
				}
				
				return false;
			}
		});
	}
	
	/**
	 * Recreates the key binding tree model, reconnects it with the
	 * tree and selects the previously selected nodes.
	 * 
	 * This is called when the key bindings have been changed.
	 */
	private void updateKeyBindingTree() {
		
		// remember selected nodes
		TreeSet<String> selectedIds = tmKeyBindings.getSelectedIds();
		
		// recreate the tree model and reconnect it with the tree
		MidicaTree tree = tmKeyBindings.getTree();
		tmKeyBindings   = Config.getKeyBindingTreeModel();
		tree.setModel(tmKeyBindings);
		tmKeyBindings.setTree(tree);
		
		// re-select previous selection
		tmKeyBindings.selectByIds(selectedIds);
	}
	
	/**
	 * Checks if the content of the text fields (from-ticks, to/ticks, tracks)
	 * is ok. Checks the tick fields only if the limit-ticks checkbox is
	 * checked. Checks the tracks text field only if the limit-tracks checkbox
	 * is checked.
	 * 
	 * The text of the from/to tick fields is checked against the expected
	 * number format and range.
	 * 
	 * The track text field is checked against the expected comma- and/or
	 * minus separated track number format and the ranges of all contained
	 * edge values.
	 * 
	 * If the check fails for a text field, the background of that field is
	 * colored in red, otherwise in white.
	 * 
	 * If the from-ticks and to-ticks numbers are both ok, but the "from"
	 * number is higher than the "to" number, both fields are colored in red.
	 * 
	 * If the event comes from one of the text fields and it's content is ok,
	 * the according field is colored in green.
	 * 
	 * @param e  Event object.
	 * @return **true**, if everything is ok. Otherwise: **false**.
	 */
	private boolean checkTextFields( DocumentEvent e ) {
		Document doc    = e.getDocument();
		String   name   = doc.getProperty( "name" ).toString();
		boolean  result = true;
		
		// prepare widgets
		HashMap<String, JComponent> widgets = getMsgFilterWidgetsIfReady();
		if ( null == widgets )
			return false;
		JTextField txtFrom     = (JTextField) widgets.get( InfoView.FILTER_TXT_FROM_TICKS );
		JTextField txtTo       = (JTextField) widgets.get( InfoView.FILTER_TXT_TO_TICKS   );
		JTextField txtTracks   = (JTextField) widgets.get( InfoView.FILTER_TXT_TRACKS     );
		Document   docFrom     = txtFrom.getDocument();
		Document   docTo       = txtTo.getDocument();
		Document   docTracks   = txtTracks.getDocument();
		JCheckBox  limitTicks  = (JCheckBox) widgets.get( InfoView.FILTER_CBX_LIMIT_TICKS  );
		JCheckBox  limitTracks = (JCheckBox) widgets.get( InfoView.FILTER_CBX_LIMIT_TRACKS );
		boolean    checkTicks  = limitTicks.isSelected();
		boolean    checkTracks = limitTracks.isSelected();
		
		// get max ticks
		long maxTicks = 0;
		HashMap<String, Object> sequenceInfo = SequenceAnalyzer.getSequenceInfo();
		Object ticksObj = sequenceInfo.get( "ticks" );
		if ( ticksObj != null )
			maxTicks = (long) ticksObj;
		
		// check "from"
		long fromTicks = 0;
		if (checkTicks) {
			fromTicks = checkTickTextField( docFrom, maxTicks );
			if ( fromTicks < 0 ) {
				txtFrom.setBackground( Laf.COLOR_ERROR );
				result = false;
			}
			else {
				txtFrom.setBackground( Laf.COLOR_NORMAL );
			}
		}
		
		// check "to"
		long toTicks = maxTicks;
		if (checkTicks) {
			toTicks = checkTickTextField( docTo, maxTicks );
			if ( toTicks < 0 ) {
				txtTo.setBackground( Laf.COLOR_ERROR );
				result = false;
			}
			else {
				txtTo.setBackground( Laf.COLOR_NORMAL );
			}
		}
		
		// check "tracks"
		HashSet<Integer> tracks = null;
		if (checkTracks) {
			tracks = getTracksFromTextField( docTracks );
			if ( null == tracks ) {
				txtTracks.setBackground( Laf.COLOR_ERROR );
				result = false;
			}
			else {
				txtTracks.setBackground( Laf.COLOR_NORMAL );
			}
		}
		
		// currently changed text field is ok? - Than make it green.
		boolean makeGreen = false;
		if ( InfoView.FILTER_TXT_FROM_TICKS.equals(name) && fromTicks > 0 )
			makeGreen = true;
		else if ( InfoView.FILTER_TXT_TO_TICKS.equals(name) && toTicks > 0 )
			makeGreen = true;
		else if ( InfoView.FILTER_TXT_TRACKS.equals(name) && tracks != null )
			makeGreen = true;
		if (makeGreen)
			widgets.get( name ).setBackground( Laf.COLOR_OK );
		
		// from > to?
		if ( checkTicks && fromTicks >= 0 && toTicks >= 0 ) {
			if ( fromTicks > toTicks ) {
				txtFrom.setBackground( Laf.COLOR_ERROR );
				txtTo.setBackground( Laf.COLOR_ERROR );
				result = false;
			}
		}
		
		// apply message filter only if the check succeeded
		if (result) {
			filterMessages();
		}
		
		return result;
	}
	
	/**
	 * Checks one of the tick text fields (to or from).
	 * 
	 * @param doc        Document of the text field to check.
	 * @param maxTicks   Number of ticks in the currently loaded MIDI sequence.
	 * @return the textfield's value, if the value is ok, otherwise: **-1**.
	 */
	private long checkTickTextField( Document doc, long maxTicks ) {
		
		// get the value from the text field
		long ticks = -1;
		try {
			String text = doc.getText( 0, doc.getLength() );
			ticks       = Long.parseLong( text );
		}
		catch( NumberFormatException | BadLocationException e ) {
		}
		
		// check range
		if ( ticks < 0 || ticks > maxTicks )
			return -1;
		
		return ticks;
	}
	
	/**
	 * Gets, checks and returns the tracks from the text field.
	 * 
	 * @param doc  Document of the text field to check.
	 * @return the track numbers, if the field contains no errors.
	 *         Returns **null** if there are errors.
	 */
	private HashSet<Integer> getTracksFromTextField( Document doc ) {
		
		HashSet<Integer> result = new HashSet<Integer>();
		
		// get track number
		HashMap<String, Object> sequenceInfo = SequenceAnalyzer.getSequenceInfo();
		Object tracksObj = sequenceInfo.get( "num_tracks" );
		int  maxTrack = 0;
		if ( tracksObj != null )
			maxTrack = (int) tracksObj - 1;
		
		try {
			String   text    = doc.getText( 0, doc.getLength() );
			String[] parts   = text.split( ",", -1 );
			Pattern  pSingle = Pattern.compile( "^(\\d+)$" );
			Pattern  pRange  = Pattern.compile( "^(\\d+)\\-(\\d+)$" );
			
			// one part is either a track number or a range
			PART:
			for ( String part : parts ) {
				
				// single track number?
				Matcher m = pSingle.matcher( part );
				if ( m.find() ) {
					int track = getTrackNum( part, maxTrack );
					result.add( track );
					
					continue PART;
				}
				
				// range?
				m = pRange.matcher( part );
				if ( m.find() ) {
					String firstStr = m.group( 1 );
					String lastStr  = m.group( 2 );
					int    smallNum = getTrackNum( firstStr, maxTrack );
					int    bigNum   = getTrackNum( lastStr,  maxTrack );
					if ( smallNum > bigNum )
						return null;
					
					// process each element of the range
					while ( smallNum <= bigNum ) {
						result.add( smallNum );
						smallNum++;
					}
					
					continue PART;
				}
				
				// invalid element (neither single number nor range)
				else {
					return null;
				}
			}
		}
		catch( NumberFormatException | BadLocationException e ) {
			return null;
		}
		
		// empty field?
		if ( result.isEmpty() )
			return null;
		
		// ok
		return result;
	}
	
	/**
	 * Converts the given string into a number and checks if it's a valid
	 * track number.
	 * 
	 * @param str       String to convert.
	 * @param maxTrack  Maximum track number.
	 * @return the track number, beginning with **0**, or **-1** if
	 *         the check fails.
	 * @throws NumberFormatException if the string cannot be converted
	 *         into an integer or the check fails.
	 */
	private int getTrackNum( String str, int maxTrack ) throws NumberFormatException {
		
		// convert string to int
		int track = Integer.parseInt( str );
		
		// check range
		if ( track < 0 )
			throw new NumberFormatException();
		if ( track > maxTrack )
			throw new NumberFormatException();
		
		// ok
		return track;
	}
	
	/**
	 * Returns the currently selected message from the message table or
	 * **null**, if no message is selected.
	 * 
	 * @return the selected message, if possible. Otherwise: **null**.
	 */
	private SingleMessage getSelectedMessage() {
		
		// get table
		MidicaTable table = view.getMsgTable();
		if ( null == table )
			return null;
		
		// get selected row index
		int row = table.getSelectedRow();
		
		// nothing selected or selection not visible?
		if (-1 == row)
			return null;
		
		// get row object
		MessageTableModel model         = (MessageTableModel) table.getModel();
		SingleMessage     singleMessage = model.getMsg( table.convertRowIndexToModel(row) );
		
		return singleMessage;
	}
	
	/**
	 * Activates or deactivates the show-in-tree button in the message filter.
	 * 
	 * The button is activated if:
	 * 
	 * - a message in the table is selected; and
	 * - the auto-show-in-tree checkbox is **not** checked.
	 * 
	 * Otherwise the button is deactivated
	 */
	private void updateShowInTreeButtonActivity() {
		
		// prepare widgets
		HashMap<String, JComponent> widgets = getMsgFilterWidgetsIfReady();
		if ( null == widgets )
			return;
		
		// is a message selected?
		SingleMessage singleMessage = getSelectedMessage();
		boolean       isMsgSelected = null == singleMessage ? false : true;
		
		// is the auto-show checkbox selected?
		JCheckBox cbxAutoShow = (JCheckBox) widgets.get( InfoView.FILTER_CBX_AUTO_SHOW );
		boolean   isAutoShow  = cbxAutoShow.isSelected();
		
		// activate or deactivate the button
		MidicaButton button = (MidicaButton) widgets.get( InfoView.FILTER_BTN_SHOW_TREE );
		if ( isMsgSelected && ! isAutoShow ) {
			button.setEnabled( true );
		}
		else {
			button.setEnabled( false );
		}
	}
	
	/**
	 * Updates the labels for visible and total messages.
	 */
	private void updateVisibleTotalLabels() {
		
		// get labels
		HashMap<String, JComponent> widgets = view.getMsgFilterWidgets();
		JLabel lblVisible = (JLabel) widgets.get( InfoView.FILTER_LBL_VISIBLE );
		JLabel lblTotal   = (JLabel) widgets.get( InfoView.FILTER_LBL_TOTAL   );
		
		// get content
		String countVisible = view.getMsgTable().getRowCount()  + "";
		String countTotal   = MessageTableModel.msgCount        + "";
		
		// update labels
		lblVisible.setText( countVisible );
		lblTotal.setText( countTotal );
	}
	
	/**
	 * Applies the message filter.
	 * 
	 * Creates data structures from the filter widgets. Uses these structures
	 * as parameters for the filter function of the table model.
	 * 
	 * If a message is currently selected and still visible after the selection:
	 * - re-selects that message
	 * - scrolls to that message
	 */
	private void filterMessages() {
		
		// prepare widgets
		HashMap<String, JComponent> widgets = getMsgFilterWidgetsIfReady();
		if ( null == widgets )
			return;
		
		// create filter for checkboxes
		HashMap<String, Boolean> filterBoolean = new HashMap<String, Boolean>();
		boolean chDep       = ( (JCheckBox) widgets.get(InfoView.FILTER_CBX_CHAN_DEP)     ).isSelected();
		boolean chIndep     = ( (JCheckBox) widgets.get(InfoView.FILTER_CBX_CHAN_INDEP)   ).isSelected();
		boolean useNodes    = ( (JCheckBox) widgets.get(InfoView.FILTER_CBX_NODE)         ).isSelected();
		boolean limitTicks  = ( (JCheckBox) widgets.get(InfoView.FILTER_CBX_LIMIT_TICKS)  ).isSelected();
		boolean limitTracks = ( (JCheckBox) widgets.get(InfoView.FILTER_CBX_LIMIT_TRACKS) ).isSelected();
		filterBoolean.put( InfoView.FILTER_CBX_CHAN_DEP,     chDep       );
		filterBoolean.put( InfoView.FILTER_CBX_CHAN_INDEP,   chIndep     );
		filterBoolean.put( InfoView.FILTER_CBX_NODE,         useNodes    );
		filterBoolean.put( InfoView.FILTER_CBX_LIMIT_TICKS,  limitTicks  );
		filterBoolean.put( InfoView.FILTER_CBX_LIMIT_TRACKS, limitTracks );
		for ( int channel = 0; channel < 16; channel++ ) {
			String  name = InfoView.FILTER_CBX_CHAN_PREFIX + channel;
			boolean chan = ((JCheckBox) widgets.get(name) ).isSelected();
			filterBoolean.put( name, chan );
		}
		
		// create filter for tree nodes
		ArrayList<MessageTreeNode> filterNode = new ArrayList<MessageTreeNode>();
		MidicaTree                 tree       = view.getMsgTree();
		TreePath[]                 paths      = tree.getSelectionPaths();
		if ( null != paths ) {
			for ( TreePath path : paths ) {
				MessageTreeNode node = (MessageTreeNode) path.getLastPathComponent();
				filterNode.add( node );
			}
		}
		
		// create filter for ticks
		String fromStr    = ( (JTextField) widgets.get(InfoView.FILTER_TXT_FROM_TICKS) ).getText();
		String toStr      = ( (JTextField) widgets.get(InfoView.FILTER_TXT_TO_TICKS)   ).getText();
		long   filterFrom = -1;
		long   filterTo   = -1;
		try {
			filterFrom = Long.parseLong( fromStr );
			filterTo   = Long.parseLong( toStr   );
		}
		catch ( NumberFormatException e ) {
			return;
		}
		
		// get filter for tracks
		JTextField       txtTracks    = (JTextField) widgets.get( InfoView.FILTER_TXT_TRACKS );
		HashSet<Integer> filterTracks = getTracksFromTextField( txtTracks.getDocument() );
		if ( null == filterTracks ) {
			return;
		}
		
		// apply filter
		MidicaTable           table = view.getMsgTable();
		MessageTableSorter<?> rowSorter = (MessageTableSorter<?>) table.getRowSorter();
		rowSorter.removeRowSorterListener(this);
		rowSorter.setMessageFilters(filterBoolean, filterNode, filterFrom, filterTo, filterTracks);
		rowSorter.filter();
		rowSorter.addRowSorterListener(this);
		
		// update UI
		updateVisibleTotalLabels();
		
		// scroll to the currently selected message, if possible
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				SingleMessage selectedMsg = getSelectedMessage();
				if (selectedMsg != null) {
					
					// get the new row of the message
					int modelRow = ((MessageTableModel) table.getModel()).getTableRow(selectedMsg);
					int viewRow  = table.convertRowIndexToView(modelRow);
					
					// message (still) visible?
					if ( viewRow != -1 ) {
						
						// scroll to the selected row
						Rectangle cell = table.getCellRect( viewRow, 0, true );
						table.scrollRectToVisible( cell );
					}
				}
			}
		});
	}
	
	/**
	 * Shows the selected message from the table in the tree.
	 * If no message is selected, this method has no effect.
	 */
	private void showInTree() {
		
		// get leaf node of the selected message
		SingleMessage singleMessage = getSelectedMessage();
		if (null == singleMessage)
			return;
		MessageTreeNode leaf = (MessageTreeNode) singleMessage.getOption( IMessageType.OPT_LEAF_NODE );
		
		// collapse the whole tree, than select a node and scroll to it
		MidicaTree tree = tmMessages.getTree();
		tree.removeTreeSelectionListener(this);
		tmMessages.reload();                                // collapse
		TreePath leafPath = new TreePath( leaf.getPath() );
		tree.setSelectionPath(leafPath);                    // select and expand
		tree.scrollPathToVisible(leafPath);                 // scroll
		tree.addTreeSelectionListener(this);
	}
	
	/**
	 * Checks and returns the message filter widgets.
	 * 
	 * Returns the filter widgets only if all widgets in the view are ready.
	 * Otherwise, **null** is returned.
	 * 
	 * This is useful for listeners in order to avoid null pointer exceptions.
	 * 
	 * The widgets are made ready in the same order they are added to the view.
	 * Therefore it's sufficient to check against the last widget.
	 * In the moment that's the message table, but that may change in the
	 * future.
	 * 
	 * @return the filter widgets, if everything is ready, or otherwise **null**.
	 */
	private HashMap<String, JComponent> getMsgFilterWidgetsIfReady() {
		
		MidicaTable table = view.getMsgTable();
		if ( null == table )
			return null;
		
		HashMap<String, JComponent> widgets = view.getMsgFilterWidgets();
		
		return widgets;
	}
	
	public void windowActivated( WindowEvent e ) {
	}
	
	public void windowClosed( WindowEvent e ) {
	}
	
	public void windowClosing( WindowEvent e ) {
	}
	
	public void windowDeactivated( WindowEvent e ) {
	}
	
	@Override
	public void windowDeiconified( WindowEvent e ) {
	}
	
	@Override
	public void windowIconified( WindowEvent e ) {
	}
	
	/**
	 * Fills the labels for visible and total messages.
	 */
	@Override
	public void windowOpened( WindowEvent e ) {
		updateVisibleTotalLabels();
	}
	
	/**
	 * Creates a faked document change event in order to simulate a text field
	 * change. That's necessary in order to call the check method so that the
	 * fields can be colored in red if their content is invalid.
	 * 
	 * @return a faked document change event.
	 */
	private DocumentEvent createFakeTxtFieldChange() {
		return new DocumentEvent() {
			@Override
			public int getOffset() {
				return 0;
			}
			@Override
			public int getLength() {
				return 0;
			}
			@Override
			public Document getDocument() {
				return new Document() {
					@Override
					public void render( Runnable paramRunnable ) {
					}
					@Override
					public void removeUndoableEditListener( UndoableEditListener paramUndoableEditListener ) {
					}
					@Override
					public void removeDocumentListener( DocumentListener paramDocumentListener ) {
					}
					@Override
					public void remove( int paramInt1, int paramInt2 ) throws BadLocationException {
					}
					@Override
					public void putProperty( Object paramObject1, Object paramObject2 ) {
					}
					@Override
					public void insertString( int paramInt, String paramString, AttributeSet paramAttributeSet ) throws BadLocationException {
					}
					@Override
					public void getText( int paramInt1, int paramInt2, Segment paramSegment ) throws BadLocationException {
					}
					@Override
					public String getText( int paramInt1, int paramInt2 ) throws BadLocationException {
						return null;
					}
					@Override
					public Position getStartPosition() {
						return null;
					}
					@Override
					public Element[] getRootElements() {
						return null;
					}
					@Override
					public Object getProperty( Object paramObject ) {
						return "";
					}
					@Override
					public int getLength() {
						return 0;
					}
					@Override
					public Position getEndPosition() {
						return null;
					}
					@Override
					public Element getDefaultRootElement() {
						return null;
					}
					@Override
					public Position createPosition( int paramInt ) throws BadLocationException {
						return null;
					}
					@Override
					public void addUndoableEditListener( UndoableEditListener paramUndoableEditListener ) {
					}
					@Override
					public void addDocumentListener( DocumentListener paramDocumentListener ) {
					}
				};
			}
			@Override
			public EventType getType() {
				return null;
			}
			@Override
			public ElementChange getChange( Element paramElement ) {
				return null;
			}
		};
	}
}
