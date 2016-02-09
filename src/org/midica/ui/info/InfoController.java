/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.info;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;

import org.midica.ui.model.MidicaTreeModel;


/**
 * Controller for the info window.
 * 
 * - Adds and removes key bindings to/from the info view.
 * - Collapses or expands a tree after a button has been clicked.
 * 
 * @author Jan Trukenmüller
 */
public class InfoController implements WindowListener, ActionListener {
	
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
	}
	
	/**
	 * Adds info view specific key bindings.
	 */
	public void windowActivated( WindowEvent e ) {
		view.addKeyBindings();
	}
	
	/**
	 * Removes info view specific key bindings.
	 */
	public void windowClosed( WindowEvent e ) {
		view.removeKeyBindings();
	}
	
	/**
	 * Removes info view specific key bindings.
	 */
	public void windowClosing( WindowEvent e ) {
		view.removeKeyBindings();
	}
	
	/**
	 * Removes info view specific key bindings.
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
	
	@Override
	public void windowOpened( WindowEvent e ) {
	}
	
}
