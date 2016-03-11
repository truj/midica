/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.model;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * This class represents a node for a {@link MidicaTree}.
 * 
 * It contains a name, a descendant counter and a number
 * representing a MIDI byte, double byte or byte range.
 * However the number can be **null**.
 * 
 * This class is designed to be used mainly from a {@link MidicaTreeModel}.
 * The following steps are necessary:
 * 
 * - Call one of the constructors.
 * - Call {@link #initChildren()}, if it's a branch. Not necessary for leafs.
 * - For each child:
 *   - Call {@link #addAndOrIncrement(String, String, String, boolean)}
 *   - Call {@link #increment()}
 * - After all children are added, connect them with each other in the right
 *   order by calling {@link #addChildren()}. This works recursively.
 * 
 * @author Jan Trukenmüller
 */
public class MidicaTreeNode extends DefaultMutableTreeNode implements IMessageDetailProvider {
	
	private static final long serialVersionUID = 1L;
	
	/** basic text to be displayed */
	private String name = null;
	
	/** number representation of the node (e.g. bank number, program, status byte, etc.) */
	private String number = null;
	
	/** number of leaves under the node's hierarchy */
	private int count = 0;
	
	/** stores the child nodes in a sorted form */
	private TreeMap<String, MidicaTreeNode> sortedChildren = null;
	
	/** can be used to store custom node options */
	private HashMap<String, Object> options = new HashMap<String, Object>();
	
	/** stores the minimum custom node options */
	private HashMap<String, Comparable<?>> minOptions = new HashMap<String, Comparable<?>>();
	
	/** stores the maximum custom node options */
	private HashMap<String, Comparable<?>> maxOptions = new HashMap<String, Comparable<?>>();
	
	/** stores multiple values of the same name */
	private HashMap<String, TreeSet<Comparable<?>>> distinctOptions = new HashMap<String, TreeSet<Comparable<?>>>();
	
	/**
	 * Creates a new node with the given text and number.
	 * 
	 * @param name    Main text to be displayed.
	 * @param number  MIDI number representing this node.
	 */
	public MidicaTreeNode( String name, String number ) {
		this.number = number;
		this.name   = name;
	}
	
	/**
	 * Creates a new node with the given text but without a number.
	 * 
	 * @param name  Main text to be displayed.
	 */
	public MidicaTreeNode( String name ) {
		this.name = name;
	}
	
	/**
	 * Increments the count by 1.
	 * This is called if a descendant of this node is added to the tree.
	 */
	public void increment() {
		count++;
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
	 * Returns the text of the node.
	 * 
	 * @return text of the node
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
	 * @param id      Child identifier string -- unique for the children of this node.
	 * @param name    Child name.
	 * @param number  MIDI number representation of the child.
	 * @param isLeaf  Specifies if the child is a leaf or a branch.
	 * @return created or incremented child.
	 */
	public MidicaTreeNode addAndOrIncrement( String id, String name, String number, boolean isLeaf ) {
		
		// create child, if not yet done
		if ( ! sortedChildren.containsKey(id) ) {
			MidicaTreeNode child = new MidicaTreeNode( name, number );
			sortedChildren.put( id, child );
			if ( ! isLeaf )
				child.initChildren();
		}
		
		// increment child
		MidicaTreeNode child = sortedChildren.get( id );
		child.increment();
		
		return child;
	}
	
	/**
	 * Adds all children recursively.
	 */
	public void addChildren() {
		
		if ( null == sortedChildren )
			return;
		
		for ( Entry<String, MidicaTreeNode> childEntry : sortedChildren.entrySet() ) {
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
		return toString();
	}
	
	/**
	 * Returns the string to be displayed in the tree.
	 */
	@Override
	public String toString() {
		if ( null == number )
			return name + " (" + count + ")";
		return "[" + number + "] " + name + " (" + count + ")";
	}
	
	/**
	 * Sets custom node options.
	 * 
	 * If the option value is comparable, remembers the minimum and maximum
	 * of all calls to this method with the same **name**.
	 * 
	 * These values can be retrieved later with {@link #getMinOption(String)} and
	 * {@link #getMaxOption(String)}.
	 * 
	 * @param name   The option name.
	 * @param value  The option value.
	 */
	public void setOption( String name, Object value ) {
		
		// add option
		options.put( name, value );
		
		// adjust min/max option
		if ( value instanceof Comparable<?> ) {	
			
			Comparable    cValue = (Comparable<?>) value;
			Comparable<?> min    = minOptions.get( name );
			Comparable<?> max    = maxOptions.get( name );
			if ( null == min || cValue.compareTo(min) < 0 ) {
				minOptions.put( name, cValue );
			}
			if ( null == min || cValue.compareTo(max)	 > 0 ) {
				maxOptions.put( name, cValue );
			}
		}
	}
	
	/**
	 * Returns the custom node option that has been set with the last call
	 * to {@link #setOption(String, Object)} with the given name.
	 * 
	 * Returns **null**, if no option of the given name exists.
	 * 
	 * @param name   The option name.
	 * @return the option value or **null** if not available.
	 */
	@Override
	public Object getOption( String name ) {
		return options.get( name );
	}
	
	/**
	 * Returns the range of values that has been set by calling
	 * {@link #setOption(String, Object)} with the given name.
	 * 
	 * Returns **null**, if no values have been set or the values
	 * are not {@link Comparable}.
	 * 
	 * Returns only one value, if the minimum and maximum values
	 * are identical.
	 * 
	 * Otherwise: Returns the range in the form **min - max**.
	 * 
	 * @param name  The option name.
	 * @return the option's range or value or **null**, like described above.
	 */
	@Override
	public String getRange( String name ) {
		Object min = minOptions.get( name );
		Object max = maxOptions.get( name );
		
		// not set or not comparable?
		if ( null == minOptions || null == maxOptions )
			return null;
		
		// min and max are identical?
		String minStr = min.toString();
		String maxStr = max.toString();
		if ( minStr.equals(maxStr) )
			return minStr;
		
		// range
		return minStr + " - " + maxStr;
	}
	
	/**
	 * Sets options to be stored permanently.
	 * 
	 * If this method is called several times with the same name, the according
	 * value is only stored if not yet done.
	 * 
	 * The values can be retrieved later with {@link #getDistinctOption(String)}
	 * 
	 * The following data types are allowed for the value:
	 * 
	 * - {@link String}
	 * - {@link Byte}
	 * - {@link Integer}
	 * - {@link Long}
	 * 
	 * @param name   The option name.
	 * @param value  The option value (must be a string or a number object).
	 */
	public void setDistinctOption( String name, Comparable<?> value ) {
		
		// check data type
		if ( ! (value instanceof String)
		  && ! (value instanceof Byte)
		  && ! (value instanceof Integer)
		  && ! (value instanceof Long) ) {
			return;
		}
		
		// called for the first time with this name?
		if ( ! distinctOptions.containsKey(name) )
			distinctOptions.put( name, new TreeSet<Comparable<?>>() );
		
		// add the value
		distinctOptions.get( name ).add( value );
	}
	
	/**
	 * Returns all distinct options that have been added with the given name.
	 * 
	 * Returns **null**, if no distinct options with this name have been added.
	 * 
	 * Same as {@link #getDistinctOptions(String, String)} using "**, **" as
	 * the separator.
	 * 
	 * @param name   The option name.
	 * @return       the option values, if available. Otherwise: **null**.
	 */
	@Override
	public String getDistinctOptions( String name ) {
		return getDistinctOptions( name, ", " );
	}
	
	/**
	 * Returns all distinct options that have been added with the given name.
	 * 
	 * Returns **null**, if no distinct options with this name have been added.
	 * 
	 * @param name       The option name.
	 * @param separator  The string to be used in order to separate the values.
	 * @return the option values, if available. Otherwise: **null**.
	 */
	@Override
	public String getDistinctOptions( String name, String separator ) {
		
		// not yet set
		if ( ! distinctOptions.containsKey(name) )
			return null;
		
		// create a string
		StringBuffer result   = new StringBuffer("");
		boolean      firstOpt = true;
		for ( Object obj : distinctOptions.get(name) ) {
			if ( ! firstOpt )
				result.append( separator );
			result.append( obj.toString() );
			firstOpt = false;
		}
		return result.toString();
	}
}
