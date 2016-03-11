/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.widget;

import java.awt.event.MouseEvent;

import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.tree.TreePath;

import org.midica.ui.model.MidicaTreeModel;
import org.midica.ui.model.MidicaTreeNode;

/**
 * Class for Midica trees.
 * 
 * These trees support tooltips for their nodes.
 * 
 * @author Jan Trukenmüller
 */
public class MidicaTree extends JTree {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Creates a new tree supporting tooltips for the nodes.
	 */
	public MidicaTree() {
		super();
		prepareToolTips();
	}
	
	/**
	 * Creates a new tree supporting tooltips for the nodes.
	 * 
	 * @param model  The tree model.
	 */
	public MidicaTree( MidicaTreeModel model ) {
		super( model );
		prepareToolTips();
	}
	
	/**
	 * Sets the given model as tree model.
	 * 
	 * @param model
	 */
	public void setModel( MidicaTreeModel model ) {
		super.setModel( model );
	}
	
	/**
	 * Registers the tree at the {@link ToolTipManager}.
	 */
	private void prepareToolTips() {
		ToolTipManager.sharedInstance().registerComponent( this );
	}
	
	/**
	 * Returns the tooltip text according to the tree node hovered
	 * by the mouse pointer.
	 * 
	 * @param e  The mouseover event.
	 * @return   The tooltip text for the node hovered by the mouse pointer.
	 */
	@Override
	public String getToolTipText( MouseEvent e ) {
		
		// mouse hovers a valid tree row?
		int rowLocation = getRowForLocation( e.getX(), e.getY() );
		if ( rowLocation < 0 )
			return null;
		
		// get the node hovered by the mouse
		TreePath       curPath = getPathForLocation( e.getX(), e.getY() );
		MidicaTreeNode node    = (MidicaTreeNode) curPath.getLastPathComponent();
		
		// return the tooltip
		return node.getToolTip();
	}
}
