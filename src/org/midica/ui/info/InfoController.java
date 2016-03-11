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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.tree.TreePath;

import org.midica.config.Config;
import org.midica.midi.SequenceAnalyzer;
import org.midica.ui.model.MessageTableModel;
import org.midica.ui.model.MessageDetail;
import org.midica.ui.model.MidicaTreeModel;
import org.midica.ui.model.MidicaTreeNode;
import org.midica.ui.widget.MidicaTable;
import org.midica.ui.widget.MidicaTree;

/**
 * Controller for the info window.
 * 
 * - Adds and removes key bindings to/from the info view.
 * - Collapses or expands a tree after a button has been clicked.
 * 
 * @author Jan Trukenmüller
 */
public class InfoController implements WindowListener, ActionListener, TreeSelectionListener,
	ListSelectionListener, ItemListener, DocumentListener, FocusListener {
	
	public static final String CMD_COLLAPSE                = "cmd_collapse";
	public static final String CMD_EXPAND                  = "cmd_expand";
	public static final String NAME_TREE_BANKS_TOTAL       = "name_tree_banks_total";
	public static final String NAME_TREE_BANKS_PER_CHANNEL = "name_tree_banks_per_channel";
	public static final String NAME_TREE_MESSAGES          = "name_tree_messages";
	
	private InfoView view = null;
	
	// tree models
	private MidicaTreeModel tmBanksTotal      = null;
	private MidicaTreeModel tmBanksPerChannel = null;
	private MidicaTreeModel tmMessages        = null;
	
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
	public void setTreeModel( MidicaTreeModel treeModel, String name ) {
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
	 * Handles all button clicks from in the info view window.
	 * 
	 * Those are:
	 * 
	 * - pushing a collapse-all button
	 * - pushing an expand-all button
	 * - pushing a show-in-tree button in the message filter
	 * 
	 * @param e    The invoked action event.
	 */
	@Override
	public void actionPerformed( ActionEvent e ) {
		String  cmd  = e.getActionCommand();
		JButton btn  = (JButton) e.getSource();
		String  name = btn.getName();
		
		// collapse or expand all nodes of a tree
		if ( CMD_COLLAPSE.equals(cmd) || CMD_EXPAND.equals(cmd) ) {
			
			boolean mustExpand = CMD_EXPAND.equals( cmd );
			if ( NAME_TREE_BANKS_TOTAL.equals(name) )
				tmBanksTotal.expandOrCollapse( mustExpand );
			else if ( NAME_TREE_BANKS_PER_CHANNEL.equals(name) )
				tmBanksPerChannel.expandOrCollapse( mustExpand );
			else if ( NAME_TREE_MESSAGES.equals(name) )
				tmMessages.expandOrCollapse( mustExpand );
		}
		
		// show a message in the tree (after it has been clicked in the table)
		else if ( InfoView.FILTER_BTN_SHOW_TREE.equals(cmd) ) {
			showInTree();
		}
	}
	
	/**
	 * Handles selection changes in the message tree.
	 * 
	 * - Filters the messages shown in the table. (Only if the filter checkbox
	 *   for selected nodes is selected.)
	 * - Changes the details area.
	 * 
	 * The details area is only filled if exactly one node is selected and
	 * this is a leaf node.
	 * In all other cases it will just be emptied.
	 * 
	 * @param e    The event to be handled.
	 */
	@Override
	public void valueChanged( TreeSelectionEvent e ) {
		MidicaTree tree = (MidicaTree) e.getSource();
		String     name = tree.getName();
		
		// only handle the message tree
		if ( ! name.equals(NAME_TREE_MESSAGES) )
			return;
		
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
		MessageDetail msgDetail = getSelectedMessage();
		if ( msgDetail != null ) {
			
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
	 * - changes the color of the text fields
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
				txtFrom.setEnabled( true );
				txtTo.setEnabled( true );
				
				// adjust text field colors
				checkTextFields( createFakeTxtFieldChange() );
			}
			else {
				// deactivate text fields
				txtFrom.setEnabled( false );
				txtTo.setEnabled( false );
				
				// neutralize text field colors
				txtFrom.setBackground( Config.COLOR_NORMAL );
				txtTo.setBackground( Config.COLOR_NORMAL );
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
	 * Checks text field contents after a change (by calling
	 * {@link #checkTextFields(DocumentEvent)}).
	 * 
	 * @param e    Event object.
	 */
	@Override
	public void changedUpdate( DocumentEvent e ) {
		checkTextFields( e );
	}
	
	/**
	 * Checks text field contents after a change (by calling
	 * {@link #checkTextFields(DocumentEvent)}).
	 * 
	 * @param e    Event object.
	 */
	@Override
	public void insertUpdate( DocumentEvent e ) {
		checkTextFields( e );
	}
	
	/**
	 * Checks text field contents after a change (by calling
	 * {@link #checkTextFields(DocumentEvent)}).
	 * 
	 * @param e    Event object.
	 */
	@Override
	public void removeUpdate( DocumentEvent e ) {
		checkTextFields( e );
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
	
	/**
	 * Shows the message details from the selected tree node or table row in
	 * the details area.
	 * 
	 * In the following cases this method has no effect:
	 * 
	 * - if the details from a tree node are to be shown but none or more
	 *   than one nodes are currently selected
	 * - if the detials from a tree node are to be shown but the selected node
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
			MidicaTreeNode node = (MidicaTreeNode) tree.getLastSelectedPathComponent();
			if ( node.getChildCount() > 0 )
				return;
			
			// (re)fill the details area
			view.cleanMsgDetails();
			view.fillMsgDetails( node );
		}
		
		// display details from the table, if possible
		else {
			MessageDetail msgDetail = getSelectedMessage();
			if ( msgDetail != null ) {
				
				// (re)fill the details area
				view.cleanMsgDetails();
				view.fillMsgDetails( msgDetail );
			}
		}
	}
	
	/**
	 * Checks if the content of the from-ticks and to/ticks text fields is ok.
	 * 
	 * The text of both text fields is checked against the expected number format
	 * and range. If the check fails, the background is colored red, otherwise
	 * white.
	 * 
	 * If both numbers are ok, but the "from" number is higher than the "to"
	 * number, both fields are colored in red.
	 * 
	 * 
	 * If the event comes from one of the text fields and it's content is ok,
	 * the according field is colored in green.
	 * 
	 * @param e  Event object.
	 * @return **true**, if everything is ok. Otherwise: **false**.
	 */
	private boolean checkTextFields( DocumentEvent e ) {
		Document doc  = e.getDocument();
		String   name = doc.getProperty( "name" ).toString();
		
		// prepare widgets
		HashMap<String, JComponent> widgets = getMsgFilterWidgetsIfReady();
		if ( null == widgets )
			return false;
		JTextField txtFrom = (JTextField) widgets.get( InfoView.FILTER_TXT_FROM_TICKS );
		JTextField txtTo   = (JTextField) widgets.get( InfoView.FILTER_TXT_TO_TICKS   );
		Document docFrom   = txtFrom.getDocument();
		Document docTo     = txtTo.getDocument();
		
		// get max ticks
		long maxTicks = 0;
		HashMap<String, Object> sequenceInfo = SequenceAnalyzer.getSequenceInfo();
		Object ticksObj = sequenceInfo.get( "ticks" );
		if ( ticksObj != null )
			maxTicks = (long) ticksObj;
		
		// check "from"
		long fromTicks = checkTextField( docFrom, maxTicks );
		if ( fromTicks < 0 )
			widgets.get( InfoView.FILTER_TXT_FROM_TICKS ).setBackground( Config.COLOR_ERROR );
		else
			widgets.get( InfoView.FILTER_TXT_FROM_TICKS ).setBackground( Config.COLOR_NORMAL );
		
		// check "to"
		long toTicks = checkTextField( docTo, maxTicks );
		if ( toTicks < 0 )
			widgets.get( InfoView.FILTER_TXT_TO_TICKS ).setBackground( Config.COLOR_ERROR );
		else
			widgets.get( InfoView.FILTER_TXT_TO_TICKS ).setBackground( Config.COLOR_NORMAL );
		
		// currently changed text field is ok?
		long changedTicks = checkTextField( doc, maxTicks );
		if ( changedTicks > 0 )
			widgets.get( name ).setBackground( Config.COLOR_OK );
		
		// from > to?
		if ( fromTicks >= 0 && toTicks >= 0 ) {
			if ( fromTicks > toTicks ) {
				widgets.get( InfoView.FILTER_TXT_FROM_TICKS ).setBackground( Config.COLOR_ERROR );
				widgets.get( InfoView.FILTER_TXT_TO_TICKS   ).setBackground( Config.COLOR_ERROR );
			}
		}
		
		// everything is ok?
		if ( fromTicks >= 0 && toTicks >= 0 && fromTicks <= toTicks ) {
			
			// apply filter to the changes
			filterMessages();
			
			return true;
		}
		
		// something is invalid
		return false;
	}
	
	/**
	 * Checks one of the tick text fields (to or from).
	 * 
	 * @param doc        Document of the text field to check.
	 * @param maxTicks   Number of ticks in the currently loaded MIDI sequence.
	 * @return the textfield's value, if the value is ok, otherwise: **-1**.
	 */
	private long checkTextField( Document doc, long maxTicks ) {
		
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
	 * Returns the currently selected message from the message table ocheckedr
	 * **null**, if no message is selected.
	 * 
	 * @return the selected message, if possible. Otherwise: **null**.
	 */
	private MessageDetail getSelectedMessage() {
		
		// get table
		MidicaTable table = view.getMsgTable();
		if ( null == table )
			return null;
		
		// get selected row index
		int row = table.getSelectedRow();
		if ( -1 == row )
			return null;
		
		// get row object
		MessageTableModel model     = (MessageTableModel) table.getModel();
		MessageDetail     msgDetail = model.getMsg( row );
		
		return msgDetail;
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
		MessageDetail msgDetail     = getSelectedMessage();
		boolean       isMsgSelected = null == msgDetail ? false : true;
		
		// is the auto-show checkbox selected?
		JCheckBox cbxAutoShow = (JCheckBox) widgets.get( InfoView.FILTER_CBX_AUTO_SHOW );
		boolean   isAutoShow  = cbxAutoShow.isSelected();
		
		// activate or deactivate the button
		JButton button = (JButton) widgets.get( InfoView.FILTER_BTN_SHOW_TREE );
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
		String countVisible = MessageTableModel.msgCountVisible + "";
		String countTotal   = MessageTableModel.msgCountTotal   + "";
		
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
		boolean chDep      = ( (JCheckBox) widgets.get(InfoView.FILTER_CBX_CHAN_DEP)    ).isSelected();
		boolean chIndep    = ( (JCheckBox) widgets.get(InfoView.FILTER_CBX_CHAN_INDEP)  ).isSelected();
		boolean useNodes   = ( (JCheckBox) widgets.get(InfoView.FILTER_CBX_NODE)        ).isSelected();
		boolean limitTicks = ( (JCheckBox) widgets.get(InfoView.FILTER_CBX_LIMIT_TICKS) ).isSelected();
		filterBoolean.put( InfoView.FILTER_CBX_CHAN_DEP,    chDep      );
		filterBoolean.put( InfoView.FILTER_CBX_CHAN_INDEP,  chIndep    );
		filterBoolean.put( InfoView.FILTER_CBX_NODE,        useNodes   );
		filterBoolean.put( InfoView.FILTER_CBX_LIMIT_TICKS, limitTicks );
		for ( int channel = 0; channel < 16; channel++ ) {
			String  name = InfoView.FILTER_CBX_CHAN_PREFIX + channel;
			boolean chan = ((JCheckBox) widgets.get(name) ).isSelected();
			filterBoolean.put( name, chan );
		}
		
		// create filter for tree nodes
		ArrayList<MidicaTreeNode> filterNode = new ArrayList<MidicaTreeNode>();
		MidicaTree                tree       = view.getMsgTree();
		TreePath[]                paths      = tree.getSelectionPaths();
		if ( null != paths ) {
			for ( TreePath path : paths ) {
				MidicaTreeNode node = (MidicaTreeNode) path.getLastPathComponent();
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
		
		// remember the currently selected message
		MessageDetail selectedMsg = getSelectedMessage();
		
		// apply filter
		MidicaTable       table = view.getMsgTable();
		MessageTableModel model = (MessageTableModel) table.getModel();
		model.filterMessages( filterBoolean, filterNode, filterFrom, filterTo );
		
		// update UI
		updateVisibleTotalLabels();
		
		// was a message selected before?
		if ( selectedMsg != null ) {
			
			// get the new row of the message
			int row = model.getTableRow( selectedMsg );
			
			// message still visible?
			if ( row != -1 ) {
				
				// restore the selection (re-select the row)
				table.getSelectionModel().setSelectionInterval( row, row );
				
				// scroll to the selected row
				Rectangle cell = table.getCellRect( row, 0, true );
				table.scrollRectToVisible( cell );
			}
		}
	}
	
	/**
	 * Shows the selected message from the table in the tree.
	 * If no message is selected, this method has no effect.
	 */
	private void showInTree() {
		
		// get leaf node of the selected message
		MessageDetail msgDetail = getSelectedMessage();
		if ( null == msgDetail )
			return;
		MidicaTreeNode leaf = (MidicaTreeNode) msgDetail.getOption( "leaf_node" );
		
		// collapse the whole tree, than select a node and scroll to it
		MidicaTree tree = tmMessages.getTree();
		tree.removeTreeSelectionListener( this );
		tmMessages.reload();                                // collapse
		TreePath leafPath = new TreePath( leaf.getPath() );
		tree.setSelectionPath( leafPath );                  // select and expand
		tree.scrollPathToVisible( leafPath );               // scroll
		tree.addTreeSelectionListener( this );
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
	
	/**
	 * Adds info view specific key bindings.
	 * 
	 * @param e    The event to be handled.
	 */
	public void windowActivated( WindowEvent e ) {
		view.addKeyBindings();
	}
	
	/**
	 * Removes info view specific key bindings.
	 * 
	 * @param e    The event to be handled.
	 */
	public void windowClosed( WindowEvent e ) {
		view.removeKeyBindings();
	}
	
	/**
	 * Removes info view specific key bindings.
	 * 
	 * @param e    The event to be handled.
	 */
	public void windowClosing( WindowEvent e ) {
		view.removeKeyBindings();
	}
	
	/**
	 * Removes info view specific key bindings.
	 * 
	 * @param e    The event to be handled.
	 */
	public void windowDeactivated( WindowEvent e ) {
		view.removeKeyBindings();
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
