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

import org.midica.midi.SequenceAnalyzer;

/**
 * This class represents one single MIDI message.
 * It's used for the messages in the messages table, produced by the {@link SequenceAnalyzer}.
 * 
 * Jan Trukenmüller
 */
public class SingleMessage implements IMessageType, Comparable<SingleMessage> {
	
	/** can be used to store custom options */
	private HashMap<Integer, Object> options = new HashMap<>();
	
	/**
	 * Sets the custom option with the given ID.
	 * 
	 * @param id     The option ID.
	 * @param value  The option value.
	 */
	public void setOption(int id, Object value) {
		options.put(id, value);
	}
	
	/**
	 * Returns the custom option with the given ID.
	 * 
	 * Returns **null**, if no option of the given ID exists.
	 * 
	 * @param id    The option ID.
	 * @return the option value or **null** if not available.
	 */
	@Override
	public Object getOption(int id) {
		return options.get(id);
	}
	
	/**
	 * Returns the custom option with the given ID.
	 * 
	 * Returns **null**, if no option of the given ID exists.
	 * 
	 * Same as {@link #getDistinctOptions(String)}, but necessary in order to
	 * fulfill the interface requirements.
	 * 
	 * @param id  The option ID.
	 * @return the option value or **null** if not available.
	 */
	@Override
	public String getRange(int id) {
		return getDistinctOptions(id);
	}
	
	/**
	 * Returns the custom option with the given ID as a string.
	 * 
	 * Returns **null**, if no option of the given ID exists.
	 * 
	 * The only difference to {@link #getOption(String)} is the
	 * return type.
	 * 
	 * @param id  The option ID.
	 * @return the option value or **null** if not available.
	 */
	@Override
	public String getDistinctOptions(int id) {
		if ( ! options.containsKey(id) )
			return null;
		
		return options.get(id) + "";
	}
	
	/**
	 * Returns the custom option with the given ID.
	 * 
	 * Returns **null**, if no option of the given ID exists.
	 * 
	 * Same as {@link #getOption(String)}, but necessary in order to
	 * fulfill the interface requirements.
	 * 
	 * @param id  The option ID.
	 * @return the option value or **null** if not available.
	 */
	@Override
	public String getDistinctOptions(int id, String separator) {
		return getDistinctOptions(id);
	}
	
	/**
	 * Compares this message with the given one. This is needed for sorting.
	 * 
	 * Sorting criteria are:
	 * 
	 * # tickstamp
	 * # track
	 * # note (only for NOTE-ON or NOTE-OFF messages)
	 * # message number inside the track
	 * 
	 * - Returns **+1**, if this message is "greater" than the other message.
	 * - Returns **-1**, if this message is "lesser" than the other message.
	 * - Returns **0**, if both messages are considered equal.
	 * 
	 * @param other  The other message to be compared with.
	 * @return the comparison result as described above.
	 */
	@Override
	public int compareTo(SingleMessage other) {
		
		// sort by tickstamp first
		Long tick      = (Long) getOption( IMessageType.OPT_TICK );
		Long otherTick = (Long) other.getOption( IMessageType.OPT_TICK );
		int  result    = tick.compareTo( otherTick );
		if (result != 0) {
			return result;
		}
		
		// sort by track number
		Integer track      = (Integer) getOption( IMessageType.OPT_TRACK );
		Integer otherTrack = (Integer) other.getOption( IMessageType.OPT_TRACK );
		result             = track.compareTo( otherTrack );
		if (result != 0) {
			return result;
		}
		
		// both NOTE-ON or both NOTE-OFF? - sort by note number
		String status         = (String) getOption( IMessageType.OPT_STATUS_BYTE );
		String otherStatus    = (String) other.getOption( IMessageType.OPT_STATUS_BYTE );
		char   cmdNibble      = status.charAt(0);
		char   otherCmdNibble = otherStatus.charAt(0);
		if (cmdNibble == otherCmdNibble) {
			if ('8' == cmdNibble || '9' == cmdNibble) {
				byte[] msgBytes      = (byte[]) getOption( IMessageType.OPT_MESSAGE );
				byte[] otherMsgBytes = (byte[]) other.getOption( IMessageType.OPT_MESSAGE );
				if (msgBytes[1] < otherMsgBytes[1])
					return -1;
				else if (msgBytes[1] > otherMsgBytes[1])
					return 1;
			}
		}
		
		// sort by message number inside the track
		Integer msgNum      = (Integer) getOption( IMessageType.OPT_MSG_NUM );
		Integer otherMsgNum = (Integer) other.getOption( IMessageType.OPT_MSG_NUM );
		result              = msgNum.compareTo( otherMsgNum );
		if (result != 0) {
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
		MidicaTreeNode leaf  = (MidicaTreeNode) getOption( IMessageType.OPT_LEAF_NODE );
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
		MidicaTreeNode leaf  = (MidicaTreeNode) getOption( IMessageType.OPT_LEAF_NODE );
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
	
	/**
	 * Used in unit tests.
	 */
	@Override
	public String toString() {
		return (Long) getOption(IMessageType.OPT_TICK)
			+ "/" + (Integer) getOption(IMessageType.OPT_CHANNEL)
			+ "/" + (String) getOption(IMessageType.OPT_STATUS_BYTE)
			+ "/" + (String) getOption(IMessageType.OPT_SUMMARY)
			;
	}
}
