/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.swing.tree.TreeNode;

/**
 * This class represents a certain MIDI message.
 * 
 * Jan Trukenmüller
 */
public class MessageDetail implements IMessageDetailProvider, Comparable<MessageDetail> {
	
	/** can be used to store custom options */
	private HashMap<String, Object> options = new HashMap<String, Object>();
	
	/**
	 * Sets the custom option with the given name.
	 * 
	 * @param name   The option name.
	 * @param value  The option value.
	 */
	public void setOption( String name, Object value ) {
		options.put( name, value );
	}
	
	/**
	 * Returns the custom option with the given name.
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
	 * Returns the custom option with the given name.
	 * 
	 * Returns **null**, if no option of the given name exists.
	 * 
	 * Same as {@link #getDistinctOptions(String)}, but necessary in order to
	 * fulfill the interface requirements.
	 * 
	 * @param name   The option name.
	 * @return the option value or **null** if not available.
	 */
	@Override
	public String getRange( String name ) {
		return getDistinctOptions( name );
	}
	
	/**
	 * Returns the custom option with the given name as a string.
	 * 
	 * Returns **null**, if no option of the given name exists.
	 * 
	 * The only difference to {@link #getOption(String)} is the
	 * return type.
	 * 
	 * @param name   The option name.
	 * @return the option value or **null** if not available.
	 */
	@Override
	public String getDistinctOptions( String name ) {
		if ( ! options.containsKey(name) )
			return null;
		
		return options.get( name ) + "";
	}
	
	/**
	 * Returns the custom option with the given name.
	 * 
	 * Returns **null**, if no option of the given name exists.
	 * 
	 * Same as {@link #getOption(String)}, but necessary in order to
	 * fulfill the interface requirements.
	 * 
	 * @param name   The option name.
	 * @return the option value or **null** if not available.
	 */
	@Override
	public String getDistinctOptions( String name, String separator ) {
		return getDistinctOptions( name );
	}
	
	/**
	 * Compares 2 this message with the given one. This is needed for sorting.
	 * 
	 * Sorting criteria are:
	 * 
	 * # tickstamp
	 * # track
	 * # message number inside the track
	 * 
	 * - Returns **+1**, if this message is "greater" than the other message.
	 * - Returns **11**, if this message is "lesser" than the other message.
	 * - Returns **0**, if both messages are considered equal.
	 * 
	 * @param other  The other message to be compared with.
	 * @return the comparison result as described above.
	 */
	@Override
	public int compareTo( MessageDetail other ) {
		
		// sort by tickstamp first
		Long tick      = (Long) getOption( "tick" );
		Long otherTick = (Long) other.getOption( "tick" );
		int  result    = tick.compareTo( otherTick );
		if ( result != 0 ) {
			return result;
		}
		
		// sort by track number
		Integer track      = (Integer) getOption( "track" );
		Integer otherTrack = (Integer) other.getOption( "track" );
		result             = track.compareTo( otherTrack );
		if ( result != 0 ) {
			return result;
		}
		
		// sort by message number inside the track
		Integer msgNum      = (Integer) getOption( "msg_num" );
		Integer otherMsgNum = (Integer) other.getOption( "msg_num" );
		result              = msgNum.compareTo( otherMsgNum );
		if ( result != 0 ) {
			return result;
		}
		
		return 0;
	}
	
	/**
	 * Returns a description of the message type. This is used for the "type"
	 * column in the message table.
	 * 
	 * The description contains the names from the according tree nodes in
	 * reverse order. Only the root node is **not** included.
	 * 
	 * @return the message type description.
	 */
	public String getType() {
		
		// get reverse tree path
		MidicaTreeNode leaf  = (MidicaTreeNode) getOption( "leaf_node" );
		TreeNode[]     paths = leaf.getPath();
		
		// remove the first (root) element
		TreeNode[] reducedPaths = Arrays.copyOfRange( paths, 1, paths.length );
		
		// get it in reverse order
		List<TreeNode> nodes = Arrays.asList( reducedPaths );
		Collections.reverse( nodes );
		
		// construct type string
		StringBuilder text = new StringBuilder( "" );
		for ( TreeNode bareNode : nodes ) {
			MidicaTreeNode node = (MidicaTreeNode) bareNode;
			String         name = node.getName();
			
			// leaf node
			if ( 0 == text.length() ) {
				text.append( "<html>" + name );
			}
			
			// another node
			else {
				text.append( " <span style=\"color: #dd0000; font-size: 105%; font-weight: bold; \"> &larr; </span> " + name );
			}
		}
		
		return text.toString();
	}
	
	/**
	 * Returns the tooltip for the type column of the message table for this
	 * message.
	 * 
	 * The tooltip shows the hierarchy of the message type, according to it's
	 * leaf node's tree path.
	 * 
	 * @return the tooltip text.
	 */
	public String getTypeTooltip() {
		
		// get tree path
		MidicaTreeNode leaf  = (MidicaTreeNode) getOption( "leaf_node" );
		TreeNode[]     paths = leaf.getPath();
		
		// construct tooltip text
		StringBuilder text = new StringBuilder( "<html>" );
		int i = 0;
		for ( TreeNode path : paths ) {
			MidicaTreeNode node = (MidicaTreeNode) path;
			
			// add indentation
			text.append( "<tt>" );
			for ( int j = 0; j < i; j++ ) {
				text.append( "&nbsp; &nbsp;" );
			}
			i++;
			
			// add the node
			text.append( "</tt>" + node.getName() + "<br>" );
		}
		
		return text.toString();
	}
}
