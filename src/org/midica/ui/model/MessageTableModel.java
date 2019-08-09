/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.model;

import java.util.ArrayList;

import org.midica.config.Dict;
import org.midica.ui.tablesorter.OptionalNumber;

/**
 * This class represents the data model of the messages table in the
 * MIDI sequence > MIDI messages tab of the info window.
 * 
 * Each row represents a single MIDI message that occurred at a certain tick.
 * (To be picky it represents a MIDI event, because an event is a message
 * occurring at a certain tick.)
 * 
 * @author Jan Trukenmüller
 */
public class MessageTableModel extends MidicaTableModel {
	
	public  static long msgCount = 0;
	
	private static final long serialVersionUID = 1L;
	
	private ArrayList<SingleMessage> messages = null;
    
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
		this.messages = messages;
		if ( null == this.messages ) {
			this.messages = new ArrayList<>();
		}
		msgCount = messages.size();
		
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
	 * messages.
	 * 
	 * @return    Number of rows (messages).
	 */
	@Override
	public int getRowCount() {
		if (null == messages)
			return 0;
		return messages.size();
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
		if ( null == messages )
			return "";
		if ( rowIndex < 0 || messages.size() < rowIndex + 1 )
			return "";
		
		SingleMessage singleMessage = messages.get(rowIndex);
		
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
		if ( row < 0 || row > messages.size() - 1 )
			return null;
		
		return messages.get(row);
	}
	
	/**
	 * Returns the row index (model) of the given message in the table.
	 * 
	 * @param  singleMessage  The message representing a table row.
	 * @return the row index, or **-1**, if the row is not visible.
	 */
	public int getTableRow(SingleMessage singleMessage) {
		return messages.indexOf(singleMessage);
	}
}
