/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.player;

import java.util.ArrayList;

import org.midica.config.Dict;
import org.midica.midi.MidiDevices;
import org.midica.midi.SequenceAnalyzer;
import org.midica.ui.model.MidicaTableModel;


/**
 * This class represents the data model of the note history in the channel details.
 * 
 * Each row represents a played note.
 * 
 * @author Jan Trukenmüller
 */
public class NoteHistoryTableModel extends MidicaTableModel {

	private static final long serialVersionUID = 1L;
	
	private byte channel;
	
	/** note number -- velocity -- tick -- 0=past,1=future */
	private ArrayList<Long[]> tableData = null;
	
	/**
	 * Creates a new instance of a note history table data model.
	 * 
	 * Announces itself as a channel observer to the {@link MidiDevices} class.
	 * 
	 * @param channel    MIDI channel associated with this model.
	 */
	public NoteHistoryTableModel( byte channel ) {
		this.channel = channel;
		
		// avoid null pointer exceptions
		tableData = new ArrayList<Long[]>();
		
		// table header
		columnNames = new String[ 4 ];
		columnNames[ 0 ] = Dict.get( Dict.COLUMN_NUMBER   );
		columnNames[ 1 ] = Dict.get( Dict.COLUMN_NAME     );
		columnNames[ 2 ] = Dict.get( Dict.COLUMN_VELOCITY );
		columnNames[ 3 ] = Dict.get( Dict.COLUMN_TICK     );
		
		// column classes, used for sorting
		columnClasses = new Class[ 4 ];
		columnClasses[ 0 ] = Integer.class;
		columnClasses[ 1 ] = String.class;
		columnClasses[ 2 ] = Integer.class;
		columnClasses[ 3 ] = Long.class;
		
		// allow sorting only for the tick column
		sortableColumns = new Boolean[ 4 ];
		sortableColumns[ 0 ] = false;
		sortableColumns[ 1 ] = false;
		sortableColumns[ 2 ] = false;
		sortableColumns[ 3 ] = true;
		
		// observe the midi channel
		if ( 0 == channel )
			MidiDevices.resetNoteHistoryObservers();
		MidiDevices.addNoteHistoryObserver( this, channel );
	}
	
	/**
	 * Returns the column name according to the given index.
	 * 
	 * @param index    Table column index.
	 * @return    Column name.
	 */
	@Override
	public String getColumnName( int index ) {
		return columnNames[ index ];
	}
	
	/**
	 * Returns the number of columns in the table.
	 * 
	 * @return    Number of columns.
	 */
	@Override
	public int getColumnCount() {
		return columnNames.length;
	}
	
	/**
	 * Returns the number of rows in the table.
	 * That is the same as the number of entries in the note history ring buffer
	 * of the model's channel.
	 * 
	 * @return    Number of rows.
	 */
	@Override
	public int getRowCount() {
		if ( null == tableData )
			return 0;
		
		return tableData.size();
	}
	
	/**
	 * Returns the value to be written into the specified table cell.
	 * 
	 * @param rowIndex    Queried table row index.
	 * @param colIndex    Queried table column index.
	 * @return    Table cell value.
	 */
	@Override
	public Object getValueAt( int rowIndex, int colIndex ) {
		Long[] row = tableData.get( rowIndex );
		
		switch (colIndex) {
			case 0:
				return row[ 0 ];
			case 1:
				String name;
				long   number = row[ 0 ];
				if ( 9 == channel )
					// percussion channel
					name = Dict.getPercussionLongId( (int) number );
				else
					name = Dict.getNote( (int) number );
				return name;
			case 2:
				return row[ 1 ];
			case 3:
				return row[ 2 ];
			default:
				return null;
		}
	}
	
	/**
	 * Refreshes the model's data with the note history obtained by the {@link SequenceAnalyzer}.
	 * 
	 * Then: calls the overridden method to inform the parent class about the data change.
	 * 
	 * This method is called whenever a note in the model's channel is played.
	 */
	@Override
	public void fireTableDataChanged() {
		// refresh table data
		tableData = SequenceAnalyzer.getNoteHistory( channel, MidiDevices.getTickPosition() );
		super.fireTableDataChanged();
	}
	
	/**
	 * Determines if the given table row consists a note from the future or from the past.
	 * 
	 * This is called by the cell renderer for choosing the right background color.
	 * 
	 * @param rowIndex  Table row index.
	 * @return **true**, if it's a future note. Otherwise: **false**.
	 */
	public boolean isFuture( int rowIndex ) {
		
		long futureFlag = tableData.get( rowIndex )[ 3 ];
		if ( 1L == futureFlag )
			return true;
		
		return false;
	}
	
	/**
	 * Returns the channel number.
	 * 
	 * This is called by the cell renderer for choosing the right tooltip for percussion IDs.
	 * 
	 * @return channel number
	 */
	public byte getChannel() {
		return this.channel;
	}
}
