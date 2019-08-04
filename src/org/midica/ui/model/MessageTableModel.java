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
import org.midica.ui.tablesorter.OptionalNumber;

/**
 * This class represents the data model of the messages table in the
 * MIDI sequence > MIDI messages tab of the info window.
 * 
 * Each row represents a single MIDI message that occurred at a certain tick.
 * (So to be picky it represents a MIDI event, because an event is a message
 * occurring at a certain tick.)
 * 
 * @author Jan Trukenmüller
 */
public class MessageTableModel extends MidicaTableModel {
	
	public  static long msgCountTotal   = 0;
	private static long msgCountVisible = 0; // remaining count after the message filter - but still ignoring the table string filter
	
	private static final long serialVersionUID = 1L;
	
	private ArrayList<SingleMessage> allMessages     = null;
	private ArrayList<SingleMessage> visibleMessages = new ArrayList<>();
    
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
	public MessageTableModel(ArrayList<SingleMessage> messages) {
		
		// store messages
		allMessages = messages;
		if ( null == allMessages ) {
			allMessages = new ArrayList<>();
		}
		visibleMessages = (ArrayList<SingleMessage>) allMessages.clone();
		
		// save counts
		msgCountTotal   = allMessages.size();
		msgCountVisible = visibleMessages.size();
		
		// table header
		columnNames = new String[ 7 ];
		columnNames[ 0 ] = Dict.get( Dict.INFO_COL_MSG_TICK        );
		columnNames[ 1 ] = Dict.get( Dict.INFO_COL_MSG_STATUS_BYTE );
		columnNames[ 2 ] = Dict.get( Dict.INFO_COL_MSG_TRACK       );
		columnNames[ 3 ] = Dict.get( Dict.INFO_COL_MSG_CHANNEL     );
		columnNames[ 4 ] = Dict.get( Dict.INFO_COL_MSG_LENGTH      );
		columnNames[ 5 ] = Dict.get( Dict.INFO_COL_MSG_SUMMARY     );
		columnNames[ 6 ] = Dict.get( Dict.INFO_COL_MSG_TYPE        );
		
		// tooltips for the table header
		setHeaderToolTip( 1, Dict.get(Dict.INFO_COL_MSG_TT_STATUS)  );
		setHeaderToolTip( 2, Dict.get(Dict.INFO_COL_MSG_TT_TRACK)   );
		setHeaderToolTip( 3, Dict.get(Dict.INFO_COL_MSG_TT_CHANNEL) );
		setHeaderToolTip( 4, Dict.get(Dict.INFO_COL_MSG_TT_LENGTH)  );
		
		// column classes, used for sorting
		columnClasses = new Class[ 7 ];
		columnClasses[ 0 ] = Long.class;
		columnClasses[ 1 ] = String.class;
		columnClasses[ 2 ] = Integer.class;
		columnClasses[ 3 ] = OptionalNumber.class;
		columnClasses[ 4 ] = Integer.class;
		columnClasses[ 5 ] = String.class;
		columnClasses[ 6 ] = String.class;
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
		
		SingleMessage singleMessage = visibleMessages.get(rowIndex);
		
		// tick
		if ( 0 == colIndex ) {
			return singleMessage.getOption( IMessageType.OPT_TICK );
		}
		
		// status byte
		else if ( 1 == colIndex ) {
			return "0x" + singleMessage.getOption( IMessageType.OPT_STATUS_BYTE );
		}
		
		// track
		else if ( 2 == colIndex ) {
			return singleMessage.getOption( IMessageType.OPT_TRACK );
		}
		
		// channel
		else if ( 3 == colIndex ) {
			Object channelObj = singleMessage.getOption( IMessageType.OPT_CHANNEL );
			if ( null == channelObj ) {
				channelObj = "-";
			}
			channelObj = new OptionalNumber(channelObj);
			
			return channelObj;
		}
		
		// length
		else if ( 4 == colIndex ) {
			return singleMessage.getOption( IMessageType.OPT_LENGTH );
		}
		
		// summary
		else if ( 5 == colIndex ) {
			return singleMessage.getOption( IMessageType.OPT_SUMMARY );
		}
		
		// type
		else if ( 6 == colIndex ) {
			return singleMessage.getType();
		}
		
		return "";
	}
	
	/**
	 * Returns the message details from the specified visible row.
	 * 
	 * @param row  Table row (beginning with 0)
	 * @return  message details or **null** if the given row is invalid.
	 */
	public SingleMessage getMsg(int row) {
		
		// invalid row?
		if ( row < 0 || row > visibleMessages.size() - 1 )
			return null;
		
		return visibleMessages.get(row);
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
		HashSet<Integer> fltrChannel     = new HashSet<>();
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
		for (SingleMessage msg : allMessages) {
			
			// get channel (-1 = channel independent)
			Object channelObj = msg.getOption( IMessageType.OPT_CHANNEL );
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
				Object tickObj = msg.getOption( IMessageType.OPT_TICK );
				if ( tickObj instanceof Long ) {
					long tick = (long) tickObj;
					if ( tick < filterFrom || tick > filterTo ) {
						continue MESSAGE;
					}
				}
			}
			
			// apply limit-tracks filter
			if (limitTracks) {
				Object trackObj = msg.getOption( IMessageType.OPT_TRACK );
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
				MessageTreeNode leaf = (MessageTreeNode) msg.getOption( IMessageType.OPT_LEAF_NODE );
				
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
		msgCountVisible = visibleMessages.size();
		
		// update GUI
		this.fireTableDataChanged();
	}
	
	/**
	 * Returns the row index of the given message in the table.
	 * 
	 * @param  singleMessage  The message representing a table row.
	 * @return the row index, or **-1**, if the row is not visible.
	 */
	public int getTableRow(SingleMessage singleMessage) {
		return visibleMessages.indexOf(singleMessage);
	}
}


