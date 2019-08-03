/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.model;

import java.util.ArrayList;
import java.util.HashMap;

import org.midica.config.Dict;
import org.midica.file.SoundfontParser;
import org.midica.ui.tablesorter.OptionalNumber;

/**
 * This class represents the data model of the soundfont instruments and drumkits table
 * in the configuration details view of the info window.
 * 
 * Each row represents either a category or a certain instrument or drum kit.
 * 
 * A category can be one of the following:
 * 
 * - Chromatic instruments
 * - Single channel drum kits
 * - Multi channel drum kits
 * - Unknown
 * 
 * The bank column is special. A bank can consist of 2 bytes. However in most cases
 * only the MSB is used. In these cases we show only the MSB.
 * If the LSB is not 0, we show both, MSB and LSB, separated by the currently
 * configured separator bank separator syntax element.
 * 
 * @author Jan Trukenmüller
 */
public class SoundfontInstrumentsTableModel extends MidicaTableModel {

	private static final long serialVersionUID = 1L;
    
	private ArrayList<HashMap<String, String>> instruments = null;
	
	/**
	 * Creates a new instance of the soundfont instruments table data model.
	 * Initializes the table header names according to the currently
	 * configured language.
	 */
	public SoundfontInstrumentsTableModel() {
		
		// table header
		columnNames = new String[ 5 ];
		columnNames[ 0 ] = Dict.get( Dict.INFO_COL_SF_INSTR_PROGRAM  );
		columnNames[ 1 ] = Dict.get( Dict.INFO_COL_SF_INSTR_BANK     );
		columnNames[ 2 ] = Dict.get( Dict.INFO_COL_SF_INSTR_NAME     );
		columnNames[ 3 ] = Dict.get( Dict.INFO_COL_SF_INSTR_CHANNELS );
		columnNames[ 4 ] = Dict.get( Dict.INFO_COL_SF_INSTR_KEYS     );
		
		// column classes, used for sorting
		columnClasses = new Class[ 5 ];
		columnClasses[ 0 ] = OptionalNumber.class;
		columnClasses[ 1 ] = OptionalNumber.class;
		columnClasses[ 2 ] = String.class;
		columnClasses[ 3 ] = String.class;
		columnClasses[ 4 ] = String.class;
		
		// get soundfont instruments
		instruments = SoundfontParser.getSoundfontInstruments();
	}
	
	/**
	 * Returns the number of rows in the table - same as the sum of chromatic
	 * instruments, drum kits, unknown instruments and categories.
	 * 
	 * @return    Number of rows.
	 */
	@Override
	public int getRowCount() {
		if ( null == instruments )
			return 0;
		return instruments.size();
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
		
		// program number
		if ( 0 == colIndex ) {
			Object value = instruments.get( rowIndex ).get( "program" );
			return new OptionalNumber(value);
		}
		
		// bank number
		else if ( 1 == colIndex ) {
			
			// Don't show the bank number if it's a category.
			boolean isCategory = instruments.get( rowIndex ).get("category") != null;
			if (isCategory)
				return new OptionalNumber("");
			
			// if the LSB is 0, only show the MSB
			String lsb = instruments.get(rowIndex).get("bank_lsb");
			if ( lsb.equals("0") )
				return new OptionalNumber( instruments.get(rowIndex).get("bank_msb") );
			
			// show MSB and LSB, separated according to the configured syntax
			String display = instruments.get( rowIndex ).get( "bank_msb" )
			     + Dict.getSyntax( Dict.SYNTAX_BANK_SEP )
			     + instruments.get( rowIndex ).get( "bank_lsb" );
			return new OptionalNumber(display);
		}
		
		// name
		else if ( 2 == colIndex ) {
			return instruments.get( rowIndex ).get( "name" );
		}
		
		// channels
		else if ( 3 == colIndex ) {
			return instruments.get( rowIndex ).get( "channels" );
		}
		
		// keys (notes)
		else if ( 4 == colIndex ) {
			return instruments.get( rowIndex ).get( "keys" );
		}
		
		// default
		return "";
	}
	
	@Override
	public ArrayList<HashMap<String, ?>> getCategorizedHashMapRows() {
		return new ArrayList<HashMap<String, ?>>(instruments);
	}
}


