/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.player;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import org.midica.config.Dict;
import org.midica.midi.MidiDevices;


/**
 * This class represents the data model of the note history in the channel details.
 * 
 * Each row represents a played note.
 * 
 * @author Jan Trukenmüller
 */
public class NoteHistoryTableModel extends AbstractTableModel {
	
	private byte              channel;
	private ArrayList<Long[]> tableData = null;
	
	// table header
	private String[] columnNames = {
		Dict.COLUMN_NUMBER,
		Dict.COLUMN_NAME,
		Dict.COLUMN_VOLUME,
		Dict.COLUMN_TICK,
	};
	
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
		return Dict.get( columnNames[index] );
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
					name = Dict.getPercussion( (int) number );
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
	 * Refreshes the model's data with the note history ring buffer of it's channel.
	 * 
	 * Then: calls the overridden method to inform the parent class about the data change.
	 * 
	 * This method is called whenever a note in the model's channel is played.
	 */
	@Override
	public void fireTableDataChanged() {
		// refresh table data
		tableData = MidiDevices.getNoteHistory( channel );
		super.fireTableDataChanged();
	}
}


