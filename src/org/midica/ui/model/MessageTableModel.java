/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.midica.config.Dict;
import org.midica.ui.info.InfoView;

/**
 * This class represents the data model of the messages table in the
 * MIDI sequence > MIDI messages tab of the info window.
 * 
 * Each row represents a MIDI event. (MIDI events are MIDI messages occurring
 * at a certain tick.)
 * 
 * @author Jan Trukenmüller
 */
public class MessageTableModel extends MidicaTableModel {
	
	public static long msgCountTotal   = 0;
	public static long msgCountVisible = 0;
	
	private static final long serialVersionUID = 1L;
	
	private ArrayList<MessageDetail> allMessages     = null;
	private ArrayList<MessageDetail> visibleMessages = new ArrayList<MessageDetail>();
    
	/**
	 * Creates a new instance of the message table data model.
	 * 
	 * Initializes internal data structures.
	 * 
	 * Initializes the table header names and tooltips according to the
	 * currently configured language.
	 * 
	 * @param messages  All messages found in the MIDI sequence -- or **null**,
	 *                  if no sequence has been loaded yet.
	 */
	public MessageTableModel( ArrayList<MessageDetail> messages ) {
		
		// store messages
		allMessages = messages;
		if ( null == allMessages ) {
			allMessages = new ArrayList<MessageDetail>();
		}
		visibleMessages = (ArrayList<MessageDetail>) allMessages.clone();
		
		// save counts
		msgCountTotal   = allMessages.size();
		msgCountVisible = visibleMessages.size();
		
		// table header
		columnNames = new String[ 6 ];
		columnNames[ 0 ] = Dict.get( Dict.INFO_COL_MSG_TICK        );
		columnNames[ 1 ] = Dict.get( Dict.INFO_COL_MSG_STATUS_BYTE );
		columnNames[ 2 ] = Dict.get( Dict.INFO_COL_MSG_TRACK       );
		columnNames[ 3 ] = Dict.get( Dict.INFO_COL_MSG_CHANNEL     );
		columnNames[ 4 ] = Dict.get( Dict.INFO_COL_MSG_LENGTH      );
		columnNames[ 5 ] = Dict.get( Dict.INFO_COL_MSG_TYPE        );
		
		// tooltips for the table header
		setHeaderToolTip( 1, Dict.get(Dict.INFO_COL_MSG_TT_STATUS)  );
		setHeaderToolTip( 2, Dict.get(Dict.INFO_COL_MSG_TT_TRACK)   );
		setHeaderToolTip( 3, Dict.get(Dict.INFO_COL_MSG_TT_CHANNEL) );
		setHeaderToolTip( 4, Dict.get(Dict.INFO_COL_MSG_TT_LENGTH)  );
	}
	
	/**
	 * Returns the number of rows in the table - same as the sum of all
	 * messages that have not currently been filtered out.
	 * 
	 * @return    Number of rows (notes).
	 */
	@Override
	public int getRowCount() {
		
		if ( null == visibleMessages )
			return 0;
		
		return (int) msgCountVisible;
	}
	
	/**
	 * Returns the String value to be written into the specified table cell.
	 * 
	 * @param rowIndex    Queried table row index.
	 * @param colIndex    Queried table column index.
	 * @return    Table cell text.
	 */
	@Override
	public Object getValueAt( int rowIndex, int colIndex ) {
		
		// avoid exceptions
		if ( null == visibleMessages )
			return "";
		if ( rowIndex < 0 || visibleMessages.size() < rowIndex + 1 )
			return "";
		
		MessageDetail msgDetail = visibleMessages.get( rowIndex );
		
		// tick
		if ( 0 == colIndex ) {
			return msgDetail.getOption( "tick" );
		}
		
		// status byte
		else if ( 1 == colIndex ) {
			return "0x" + msgDetail.getOption( "status_byte" );
		}
		
		// track
		else if ( 2 == colIndex ) {
			return msgDetail.getOption( "track" );
		}
		
		// channel
		else if ( 3 == colIndex ) {
			Object channelObj = msgDetail.getOption( "channel" );
			if ( null == channelObj ) {
				return "-";
			}
			
			return msgDetail.getOption( "channel" );
		}
		
		// length
		else if ( 4 == colIndex ) {
			return msgDetail.getOption( "length" );
		}
		
		// type
		else if ( 5 == colIndex ) {
			return msgDetail.getType();
		}
		
		return "";
	}
	
	/**
	 * Returns the message details from the specified visible row.
	 * 
	 * @param row  Table row (beginning with 0)
	 * @return  message details or **null** if the given row is invalid.
	 */
	public MessageDetail getMsg( int row ) {
		
		// invalid row?
		if ( row < 0 || row > visibleMessages.size() - 1 )
			return null;
		
		return visibleMessages.get( row );
	}
	
	/**
	 * Applies the given message filters to the messages shown in the table.
	 * 
	 * @param filterBoolean  Contains checkbox filters.
	 * @param filterNodes    The selected nodes from the message tree.
	 * @param filterFrom     Minimum tick number.
	 * @param filterTo       Maximum tick number.
	 * @param filterTracks   The tracks to be shown.
	 */
	public void filterMessages( HashMap<String, Boolean>   filterBoolean,
	                            ArrayList<MessageTreeNode> filterNodes,
	                            long filterFrom, long filterTo, HashSet<Integer> filterTracks ) {
		
		// unpack filter elements
		boolean          limitTicks      = filterBoolean.get( InfoView.FILTER_CBX_LIMIT_TICKS  );
		boolean          limitTracks     = filterBoolean.get( InfoView.FILTER_CBX_LIMIT_TRACKS );
		boolean          mustFilterNodes = filterBoolean.get( InfoView.FILTER_CBX_NODE         );
		HashSet<Integer> fltrChannel     = new HashSet<Integer>();
		for ( int channel = 0; channel < 16; channel++ ) {
			String name             = InfoView.FILTER_CBX_CHAN_PREFIX + channel;
			boolean mustShowChannel = filterBoolean.get( name );
			if (mustShowChannel) {
				fltrChannel.add( channel );
			}
		}
		boolean isFltrIndep = filterBoolean.get( InfoView.FILTER_CBX_CHAN_INDEP );
		if (isFltrIndep) {
			fltrChannel.add( -1 );
		}
		
		// reset visible messages
		visibleMessages.clear();
		
		// refill visible messages
		MESSAGE:
		for ( MessageDetail msg : allMessages ) {
			
			// get channel (-1 = channel independent)
			Object channelObj = msg.getOption( "channel" );
			int    channel    = -1;
			if ( channelObj instanceof Integer ) {
				channel = (int) channelObj;
			}
			
			// apply channel filters (channel independent / channel number)
			if ( ! fltrChannel.contains(channel) ) {
				continue MESSAGE;
			}
			
			// apply limit-ticks filter
			if (limitTicks) {
				Object tickObj = msg.getOption( "tick" );
				if ( tickObj instanceof Long ) {
					long tick = (long) tickObj;
					if ( tick < filterFrom || tick > filterTo ) {
						continue MESSAGE;
					}
				}
			}
			
			// apply limit-tracks filter
			if (limitTracks) {
				Object trackObj = msg.getOption( "track" );
				if ( trackObj instanceof Integer ) {
					int track = (int) trackObj;
					if ( ! filterTracks.contains(track) ) {
						continue MESSAGE;
					}
				}
			}
			
			// apply node filter
			if (mustFilterNodes) {
				
				// get the leaf node of the message
				MessageTreeNode leaf = (MessageTreeNode) msg.getOption( "leaf_node" );
				
				// check if the leaf node is a descendant of one of the selected nodes
				boolean matches = false;
				SELECTED_NODE:
				for ( MessageTreeNode node : filterNodes ) {
					matches = leaf.isNodeAncestor( node );
					if (matches) {
						break SELECTED_NODE;
					}
				}
				if ( ! matches ) {
					continue MESSAGE;
				}
			}
			
			// the message has passed the filter successfully
			visibleMessages.add( msg );
		}
		
		// update GUI
		msgCountVisible = visibleMessages.size();
		this.fireTableDataChanged();
	}
	
	/**
	 * Returns the row index of the given message in the table.
	 * 
	 * @param msgDetail  The message detail representing a table row.
	 * @return  the row index, or **-1**, if the row is not visible.
	 */
	public int getTableRow( MessageDetail msgDetail ) {
		return visibleMessages.indexOf( msgDetail );
	}
}


