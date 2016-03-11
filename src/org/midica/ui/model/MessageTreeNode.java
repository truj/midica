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
 * This class represents a node for a {@link MidicaTree} containing
 * MIDI message types.
 * 
 * It implements {@link IMessageDetailProvider} so that it's content
 * can be displayed in the message detail panel.
 * Therefore it contains methods to store and retrieve message detail
 * information.
 * 
 * @author Jan Trukenmüller
 */
public class MessageTreeNode extends MidicaTreeNode implements IMessageDetailProvider {
	
	private static final long serialVersionUID = 1L;
	
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
	public MessageTreeNode( String name, String number ) {
		super( name, number );
	}
	
	/**
	 * Creates a new node with the given text but without a number.
	 * 
	 * @param name  Main text to be displayed.
	 */
	public MessageTreeNode( String name ) {
		super( name );
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
