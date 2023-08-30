/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.model;

import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * This class represents a node for a {@link org.midica.ui.widget.MidicaTree}.
 * 
 * It contains a name, a descendant counter, an ID and a number
 * representing a MIDI byte, double byte or byte range.
 * However the number can be **null**, as well as the ID.
 * 
 * This class is designed to be used mainly from a {@link MidicaTreeModel}.
 * The following steps are necessary:
 * 
 * - Call one of the constructors.
 * - Call {@link #initChildren()}, if it's a branch. Not necessary for leafs.
 * - For each child:
 *   - Call {@link #addAndOrIncrement(String, String, String, boolean, String, String, boolean)}
 *   - Call {@link #increment()}
 * - After all children are added, connect them with each other in the right
 *   order by calling {@link #addChildren()}. This works recursively.
 * 
 * @author Jan Trukenmüller
 */
public class MidicaTreeNode extends DefaultMutableTreeNode {
	
	private static final long serialVersionUID = 1L;
	
	/** basic text to be displayed */
	private String name = null;
	
	/** number representation of the node (e.g. bank number, program, status byte, etc.) */
	private String number = null;
	
	/** number of leaves under the node's hierarchy */
	private int count = 0;
	
	/** id of the node */
	private String id = null;
	
	/** stores the child nodes in a sorted form */
	private TreeMap<String, MidicaTreeNode> sortedChildren = null;
	
	/** additions to the tool tip */
	private String toolTipAttachment = null;
	
	/**
	 * Creates a new node with the given text and number.
	 * 
	 * @param name    Main text to be displayed.
	 * @param number  MIDI number representing this node.
	 */
	public MidicaTreeNode(String name, String number) {
		this.number = number;
		this.name   = name;
	}
	
	/**
	 * Creates a new node with the given text but without a number.
	 * 
	 * @param name  Main text to be displayed.
	 */
	public MidicaTreeNode(String name) {
		this.name = name;
	}
	
	/**
	 * Creates a new node with an empty name.
	 * 
	 * This constructor is used indirectly from the {@link MidicaTreeModel}
	 * and from this class ({@link MidicaTreeNode}) by calling newInstance()
	 * on the class object. So the node name or number must be set later with
	 * {@link #setName(String)} or {@link #setNumber(String)}.
	 */
	public MidicaTreeNode() {
		super("");
	}
	
	/**
	 * Increments the count by 1.
	 * This is called if a descendant of this node is added to the tree.
	 */
	public void increment() {
		count++;
	}
	
	/**
	 * Sets the number representation of the node.
	 * 
	 * @param number MIDI number.
	 */
	public void setNumber(String number) {
		this.number = number;
	}
	
	/**
	 * Returns the number representation of the node.
	 * 
	 * @return MIDI number.
	 */
	public String getNumber() {
		return number;
	}
	
	/**
	 * Sets the base text of the node.
	 * 
	 * @param name base text of the node
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Returns the ID of the node.
	 * 
	 * @return ID of the node
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Sets the ID of the node.
	 * 
	 * @param id  ID of the node
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * Returns the base text of the node.
	 * 
	 * @return base text of the node
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Initializes the data structure for the sorted children.
	 */
	public void initChildren() {
		sortedChildren = new TreeMap<String, MidicaTreeNode>();
	}
	
	/**
	 * Creates and/or increments the specified child.
	 * 
	 * If a new child is created, it is also added to the sorted children
	 * data structure. However it is not yet added to the tree.
	 * 
	 * @param id             Child identifier string -- unique for the children of this node.
	 * @param name           Child name.
	 * @param number         MIDI number representation of the child.
	 * @param isLeaf         Specifies if the child is a leaf or a branch.
	 * @param sortKey        String used for sorting (if **null**, the **id** is used for sorting instead).
	 * @param ttAttachment   added to the leaf node's tool tip text, if not **null**
	 * @param mustIncrement  **true** if the affected nodes should be incremented, otherwise **false**.
	 * @return created or incremented child.
	 * @throws ReflectiveOperationException if the new child node cannot be created.
	 */
	public MidicaTreeNode addAndOrIncrement(String id, String name, String number, boolean isLeaf,
			String sortKey, String ttAttachment, boolean mustIncrement) throws ReflectiveOperationException {
		
		if (null == sortKey)
			sortKey = id;
		
		// create child, if not yet done
		if ( ! sortedChildren.containsKey(sortKey) ) {
			MidicaTreeNode child;
			try {
				child = getClass().newInstance();
				child.setNumber(number);
				child.setName(name);
				child.setId(id);
				sortedChildren.put(sortKey, child);
				if ( ! isLeaf )
					child.initChildren();
			}
			catch (ReflectiveOperationException e) {
				throw e;
			}
		}
		
		// increment child
		MidicaTreeNode child = sortedChildren.get(sortKey);
		if (mustIncrement)
			child.increment();
		
		// tool tip attachment
		if (isLeaf && ttAttachment != null) {
			child.attachToToolTip(ttAttachment);
		}
		
		return child;
	}
	
	/**
	 * Adds the given attachment to the node's tool tip.
	 * 
	 * @param ttAttachment  tool tip attachment
	 */
	public void attachToToolTip(String ttAttachment) {
		if (toolTipAttachment == null)
			toolTipAttachment = ttAttachment;
		else
			toolTipAttachment += ttAttachment;
	}
	
	/**
	 * Adds all children recursively.
	 */
	public void addChildren() {
		
		if (null == sortedChildren)
			return;
		
		for (Entry<String, MidicaTreeNode> childEntry : sortedChildren.entrySet()) {
			MidicaTreeNode child = childEntry.getValue();
			child.addChildren();
			add( child );
		}
	}
	
	/**
	 * Returns the same string that's also displayed as the node.
	 * 
	 * Can be overridden in order to return a more customized tooltip.
	 * 
	 * @return the tooltip to be displayed.
	 */
	public String getToolTip() {
		
		if (toolTipAttachment != null) {
			return "<html>"
				+ toString()
				.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;")
				+ toolTipAttachment;
		}
		
		return toString();
	}
	
	/**
	 * Returns the string to be displayed in the tree.
	 */
	@Override
	public String toString() {
		if (null == number)
			return name + " (" + count + ")";
		return "[" + number + "] " + name + " (" + count + ")";
	}
}
