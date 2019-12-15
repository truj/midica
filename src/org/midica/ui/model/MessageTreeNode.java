/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.model;

import java.util.HashMap;
import java.util.TreeSet;

/**
 * This class represents a node for a {@link org.midica.ui.widget.MidicaTree} containing
 * MIDI message types.
 * 
 * It implements {@link IMessageType} so that it's content
 * can be displayed in the message detail panel.
 * Therefore it contains methods to store and retrieve message detail
 * information.
 * 
 * @author Jan Trukenmüller
 */
public class MessageTreeNode extends MidicaTreeNode implements IMessageType {
	
	private static final long serialVersionUID = 1L;
	
	/** can be used to store custom node options */
	private HashMap<Integer, Object> options = new HashMap<>();
	
	/** stores the minimum custom node options */
	private HashMap<Integer, Comparable<?>> minOptions = new HashMap<>();
	
	/** stores the maximum custom node options */
	private HashMap<Integer, Comparable<?>> maxOptions = new HashMap<>();
	
	/** stores multiple values of the same ID */
	private HashMap<Integer, TreeSet<Comparable<?>>> distinctOptions = new HashMap<>();
	
	/**
	 * Creates a new node with the given text and number.
	 * 
	 * @param name    Main text to be displayed.
	 * @param number  MIDI number representing this node.
	 */
	public MessageTreeNode(String name, String number) {
		super(name, number);
	}
	
	/**
	 * Creates a new node with the given text but without a number.
	 * 
	 * @param name  Main text to be displayed.
	 */
	public MessageTreeNode(String name) {
		super(name);
	}
	
	/**
	 * Creates a new node with an empty name.
	 * 
	 * This constructor is used indirectly from the {@link MidicaTreeModel}
	 * and from {@link MidicaTreeNode} by calling newInstance() on the class
	 * object. So the node name or number must be set later with
	 * {@link #setName(String)} or {@link #setNumber(String)}.
	 */
	public MessageTreeNode() {
		super();
	}
	
	/**
	 * Sets custom node options.
	 * 
	 * If the option value is comparable, remembers the minimum and maximum
	 * of all calls to this method with the same **id**.
	 * 
	 * These values can be retrieved later with {@link #getOption(int)}.
	 * 
	 * @param id     The option ID.
	 * @param value  The option value.
	 */
	public void setOption(int id, Object value) {
		
		// add option
		options.put(id, value);
		
		// adjust min/max option
		if ( value instanceof Comparable<?> ) {	
			
			Comparable    cValue = (Comparable<?>) value;
			Comparable<?> min    = minOptions.get( id );
			Comparable<?> max    = maxOptions.get( id );
			if ( null == min || cValue.compareTo(min) < 0 ) {
				minOptions.put( id, cValue );
			}
			if ( null == min || cValue.compareTo(max)	 > 0 ) {
				maxOptions.put( id, cValue );
			}
		}
	}
	
	/**
	 * Returns the custom node option that has been set with the last call
	 * to {@link #setOption(int, Object)} with the given ID.
	 * 
	 * Returns **null**, if no option of the given ID exists.
	 * 
	 * @param id   The option ID.
	 * @return the option value or **null** if not available.
	 */
	@Override
	public Object getOption(int id) {
		return options.get(id);
	}
	
	/**
	 * Returns the range of values that has been set by calling
	 * {@link #setOption(int, Object)} with the given ID.
	 * 
	 * Returns **null**, if no values have been set or the values
	 * are not {@link Comparable}.
	 * 
	 * Returns only one value, if the minimum and maximum values
	 * are identical.
	 * 
	 * Otherwise: Returns the range in the form **min - max**.
	 * 
	 * @param id    The option ID.
	 * @return the option's range or value or **null**, like described above.
	 */
	@Override
	public String getRange(int id) {
		Object min = minOptions.get(id);
		Object max = maxOptions.get(id);
		
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
	 * If this method is called several times with the same ID, the according
	 * value is only stored if not yet done.
	 * 
	 * The values can be retrieved later with {@link #getDistinctOptions(int)}
	 * 
	 * The following data types are allowed for the value:
	 * 
	 * - {@link java.lang.String}
	 * - {@link java.lang.Byte}
	 * - {@link java.lang.Integer}
	 * - {@link java.lang.Long}
	 * 
	 * @param id     The option ID.
	 * @param value  The option value (must be a string or a number object).
	 */
	public void setDistinctOption(int id, Comparable<?> value) {
		
		// check data type
		if ( ! (value instanceof String)
		  && ! (value instanceof Byte)
		  && ! (value instanceof Integer)
		  && ! (value instanceof Long) ) {
			return;
		}
		
		// called for the first time with this ID?
		if ( ! distinctOptions.containsKey(id) )
			distinctOptions.put( id, new TreeSet<Comparable<?>>() );
		
		// add the value
		distinctOptions.get( id ).add( value );
	}
	
	/**
	 * Returns all distinct options that have been added with the given ID.
	 * 
	 * Returns **null**, if no distinct options with this ID have been added.
	 * 
	 * Same as {@link #getDistinctOptions(int, String)} using "**, **" as
	 * the separator.
	 * 
	 * @param id    The option ID.
	 * @return      the option values, if available. Otherwise: **null**.
	 */
	@Override
	public String getDistinctOptions(int id) {
		return getDistinctOptions(id, ", ");
	}
	
	/**
	 * Returns all distinct options that have been added with the given ID.
	 * 
	 * Returns **null**, if no distinct options with this ID have been added.
	 * 
	 * @param id         The option ID.
	 * @param separator  The string to be used in order to separate the values.
	 * @return the option values, if available. Otherwise: **null**.
	 */
	@Override
	public String getDistinctOptions(int id, String separator) {
		
		// not yet set
		if ( ! distinctOptions.containsKey(id) )
			return null;
		
		// create a string
		StringBuilder result   = new StringBuilder("");
		boolean       firstOpt = true;
		for ( Object obj : distinctOptions.get(id) ) {
			if ( ! firstOpt )
				result.append(separator);
			result.append( obj.toString() );
			firstOpt = false;
		}
		return result.toString();
	}
}
