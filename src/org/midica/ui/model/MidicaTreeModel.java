/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.model;

import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.midica.ui.widget.MidicaTree;

/**
 * This class defines a tree model for trees displaying MIDI sequence information.
 * 
 * It uses nodes of the type {@link MidicaTreeNode}.
 * 
 * In order to use this tree model, the following steps are necessary:
 * 
 * - Call the constructor with the text to be displayed by the root node.
 * - For each leaf: call {@link #add(ArrayList)} providing one string array
 *   per node on the way to the leaf.
 * - After adding each leaf: call {@link #postprocess()}.
 * - Create the tree.
 * - Call {@link #setTree(MidicaTree)}.
 * 
 * @author Jan Trukenmüller
 */
public class MidicaTreeModel extends DefaultTreeModel {
	
	private static final long serialVersionUID = 1L;
	
	private MidicaTreeNode rootNode = null;
	private MidicaTree     tree     = null;
	
	/**
	 * Creates a new tree model and initializes it with a new empty root node.
	 * The root node will be a {@link MidicaTreeNode} object.
	 * 
	 * @param name   Name of the root node.
	 */
	public MidicaTreeModel( String name ) {
		super( new MidicaTreeNode(name) );
		rootNode = (MidicaTreeNode) getRoot();
		rootNode.initChildren();
	}
	
	/**
	 * Creates a new tree model and initializes it with a new empty root node.
	 * The root node can be a derived class of {@link MidicaTreeNode}.
	 * 
	 * @param name       Name of the root node.
	 * @param nodeClass  Class of the nodes.
	 * @throws IllegalAccessException if the node class or its nullary constructor is not accessible.
	 * @throws InstantiationException if the root node creation fails.
	 */
	public MidicaTreeModel( String name, Class<?> nodeClass ) throws InstantiationException, IllegalAccessException {
		super( (MidicaTreeNode) nodeClass.newInstance() );
		rootNode = (MidicaTreeNode) getRoot();
		rootNode.setName( name );
		rootNode.initChildren();
	}
	
	/**
	 * Sets the tree to which this model belongs.
	 * 
	 * @param tree  The tree showing the data of this model.
	 */
	public void setTree( MidicaTree tree ) {
		this.tree = tree;
	}
	
	/**
	 * Returns the tree or **null**, if the tree is not yet set.
	 * 
	 * @return the tree.
	 */
	public MidicaTree getTree() {
		return tree;
	}
	
	/**
	 * Adds a new path to the tree, if it does not yet exist, using the
	 * root node as the entry point.
	 * 
	 * All nodes on the way to the leaf will be created if not yet done
	 * and incremented by 1. That includes all branches and the leaf.
	 * 
	 * @param params  Two-dimensional list.
	 *                1st dimension: node, 2nd dimension: id, name and number
	 * @return the leaf node of the added path.
	 * @throws ReflectiveOperationException if the new child node cannot be created.
	 */
	public MidicaTreeNode add( ArrayList<String[]> params ) throws ReflectiveOperationException {
		rootNode.increment();
		return add( rootNode, params );
	}
	
	/**
	 * Adds and/or increments new nodes (branch or leaf) recursively.
	 * 
	 * @param parent  Data structure where the new node is to be added.
	 * @param params  Two-dimensional list.
	 *                1st dimension: node, 2nd dimension: id, name and number.
	 * @return the leaf node of the added/incremented path.
	 * @throws ReflectiveOperationException if the new child node cannot be created.
	 */
	private MidicaTreeNode add( MidicaTreeNode parent, ArrayList<String[]> params ) throws ReflectiveOperationException {
		
		// get options of the first node to be added/incremented
		boolean isLeaf  = 1 == params.size();
		String[] opts   = params.get( 0 );
		String   id     = opts[ 0 ];
		String   name   = opts[ 1 ];
		String   number = opts[ 2 ];
		
		// add/increment the first node
		MidicaTreeNode incrementedChild = parent.addAndOrIncrement( id, name, number, isLeaf );
		
		// recursion or return
		if (isLeaf) {
			return incrementedChild;
		}
		else {
			// recursion: add/increment the other nodes
			params.remove( 0 );
			return add( incrementedChild, params );
		}
	}
	
	/**
	 * Connects all nodes with each other recursively.
	 */
	public void postprocess() {
		rootNode.addChildren();
	}
	
	/**
	 * Expands or collapses (selected) nodes of the tree.
	 * If no nodes are selected, all nodes are expanded or collapsed.
	 * 
	 * @param mustExpand  **true** for expanding, **false** for collapsing
	 */
	public void expandOrCollapse( boolean mustExpand ) {
		if ( null == tree )
			return;
		
		// no nodes selected?
		TreePath[] paths = tree.getSelectionPaths();
		if ( null == paths ) {
			
			// expand recursively, beginning with the root node
			if (mustExpand) {
				expandOrCollapse( new TreePath(rootNode), mustExpand );
			}
			
			// collapse only from the first level but not the root node itself
			else {
				reload();
			}
		}
		
		// at least one node selected
		else {
			// expand only the selected nodes recursively
			for ( TreePath path : paths ) {
				expandOrCollapse( path, mustExpand );
			}
		}
	}
	
	/**
	 * Expands or collapses all children of the given node recursively.
	 * 
	 * @param parentPath  Path of the node to be expended.
	 * @param mustExpand  **true** for expanding, **false** for collapsing
	 */
	private void expandOrCollapse( TreePath parentPath, boolean mustExpand ) {
		
		// expand/collapse the children recursively
		MidicaTreeNode node = (MidicaTreeNode) parentPath.getLastPathComponent();
		if ( node.getChildCount() >= 0 ) {
			for ( Enumeration<MidicaTreeNode> e = node.children(); e.hasMoreElements(); ) {
				MidicaTreeNode childNode = (MidicaTreeNode) e.nextElement();
				TreePath childPath = parentPath.pathByAddingChild( childNode );
				expandOrCollapse( childPath, mustExpand );
			}
		}
		
		// expand/collapse the parent
		if (mustExpand)
			tree.expandPath( parentPath );
		else
			tree.collapsePath( parentPath );
	}
}
