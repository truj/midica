/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.model;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.TreeSet;

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
 * - For each leaf: call {@link #add(ArrayList, String)} providing one string array
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
	 * 
	 * - 1st dimension: node
	 * - 2nd dimension:
	 *     - id
	 *     - name
	 *     - number
	 *     - sort key (optional) -- if missing, **id** is used instead
	 * @param ttAttachment  added to the leaf node's tool tip text, if not **null**
	 * @return the leaf node of the added path.
	 * @throws ReflectiveOperationException if the new child node cannot be created.
	 */
	public MidicaTreeNode add(ArrayList<String[]> params, String ttAttachment) throws ReflectiveOperationException {
		rootNode.increment();
		return add(rootNode, params, ttAttachment, true);
	}
	
	/**
	 * Adds a new path to the tree, if it does not yet exist, using the
	 * root node as the entry point, without incrementing the nodes in the path.
	 * 
	 * All nodes on the way to the leaf will be created if not yet done, but not incremented.
	 * That includes all branches and the leaf.
	 * 
	 * @param params  params  Two-dimensional list.
	 * 
	 * - 1st dimension: node
	 * - 2nd dimension:
	 *     - id
	 *     - name
	 *     - number
	 *     - sort key (optional) -- if missing, **id** is used instead
	 * @return the leaf node of the added path.
	 * @throws ReflectiveOperationException
	 */
	public MidicaTreeNode addWithoutIncrementing(ArrayList<String[]> params) throws ReflectiveOperationException {
		return add(rootNode, params, null, false);
	}
	
	/**
	 * Adds and/or increments new nodes (branch or leaf) recursively.
	 * 
	 * @param parent  Data structure where the new node is to be added.
	 * @param params  Two-dimensional list.
	 * 
	 * - 1st dimension: node
	 * - 2nd dimension:
	 *     - id
	 *     - name
	 *     - number
	 *     - sort key (optional) -- if missing, **id** is used instead
	 * 
	 * @param ttAttachment   added to the leaf node's tool tip text, if not **null**
	 * @param mustIncrement  **true** if the affected nodes should be incremented, otherwise **false**.
	 * @return the leaf node of the added/incremented path.
	 * @throws ReflectiveOperationException if the new child node cannot be created.
	 */
	private MidicaTreeNode add(MidicaTreeNode parent, ArrayList<String[]> params, String ttAttachment, boolean mustIncrement) throws ReflectiveOperationException {
		
		// get options of the first node to be added/incremented
		boolean isLeaf  = 1 == params.size();
		String[] opts    = params.get( 0 );
		String   id      = opts[ 0 ];
		String   name    = opts[ 1 ];
		String   number  = opts[ 2 ];
		String   sortKey = null;
		if (opts.length > 3)
			sortKey = opts[ 3 ];
		
		// add/increment the first node
		MidicaTreeNode affectedChild;
		affectedChild = parent.addAndOrIncrement(id, name, number, isLeaf, sortKey, ttAttachment, mustIncrement);
		
		// recursion or return
		if (isLeaf) {
			return affectedChild;
		}
		else {
			// recursion: add/increment the other nodes
			params.remove(0);
			return add(affectedChild, params, ttAttachment, mustIncrement);
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
	private void expandOrCollapse(TreePath parentPath, boolean mustExpand) {
		
		// expand/collapse the children recursively
		MidicaTreeNode node = (MidicaTreeNode) parentPath.getLastPathComponent();
		if (node.getChildCount() >= 0) {
			for ( Enumeration<MidicaTreeNode> e = node.children(); e.hasMoreElements(); ) {
				MidicaTreeNode childNode = (MidicaTreeNode) e.nextElement();
				TreePath childPath = parentPath.pathByAddingChild( childNode );
				expandOrCollapse( childPath, mustExpand );
			}
		}
		
		// expand/collapse the parent
		if (mustExpand)
			tree.expandPath(parentPath);
		else
			tree.collapsePath(parentPath);
	}
	
	/**
	 * Collects and returns all IDs of currently selected tree nodes.
	 * Does not collect **null** IDs.
	 * 
	 * @return the collected IDs.
	 */
	public TreeSet<String> getSelectedIds() {
		TreePath[]      paths       = tree.getSelectionPaths();
		TreeSet<String> selectedIds = new TreeSet<>();
		if (paths != null) {
			for (TreePath path : paths) {
				MidicaTreeNode node = (MidicaTreeNode) path.getLastPathComponent();
				String id = node.getId();
				if (id != null)
					selectedIds.add(id);
			}
		}
		return selectedIds;
	}
	
	/**
	 * Selects all tree nodes that contain one of the given IDs.
	 * 
	 * @param ids  the IDs associated with the nodes to be selected.
	 */
	public void selectByIds(TreeSet<String> ids) {
		ArrayList<TreePath> selectionPaths = new ArrayList<>();
		fillPathsByIds(ids, rootNode, selectionPaths);
		TreePath[] paths = new TreePath[selectionPaths.size()];
		int i = 0;
		for (TreePath path : selectionPaths) {
			paths[i] = path;
			i++;
		}
		tree.setSelectionPaths(paths);
	}
	
	/**
	 * Fills the given **selectionPaths** structure with all nodes under the given **node** recursively,
	 * that contain one of the given **ids**.
	 * Works recursively.
	 * 
	 * @param ids             the IDs associated with the nodes to be collected.
	 * @param node            the node under which to be searched.
	 * @param selectionPaths  the structure to be filled.
	 */
	private void fillPathsByIds(TreeSet<String> ids, MidicaTreeNode node, ArrayList<TreePath> selectionPaths) {
		
		// add given node to the list, if it has one of the requested IDs
		String id = node.getId();
		if (id != null) {
			if (ids.contains(id)) {
				TreePath path = new TreePath(node.getPath());
				selectionPaths.add(path);
			}
		}
		
		// do the same with the children, recursively
		if (node.getChildCount() > 0) {
			for (int i = 0; i < node.getChildCount(); i++) {
				MidicaTreeNode child = (MidicaTreeNode) node.getChildAt(i);
				fillPathsByIds(ids, child, selectionPaths); // recursion
			}
		}
	}
}
