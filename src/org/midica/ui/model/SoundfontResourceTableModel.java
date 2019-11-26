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
import org.midica.file.read.SoundfontParser;
import org.midica.ui.tablesorter.OptionalNumber;


/**
 * This class represents the data model of the soundfont resources table
 * in the configuration details view of the info window.
 * 
 * Each row represents either a category or a certain resource.
 * 
 * A category can be one of the following:
 * 
 * - Sample
 * - Layer
 * - Unknown
 * 
 * @author Jan Trukenmüller
 */
public class SoundfontResourceTableModel extends MidicaTableModel {

	private static final long serialVersionUID = 1L;
    
	private ArrayList<HashMap<String, Object>> resources = null;
	
	/**
	 * Creates a new instance of the soundfont resource table data model.
	 * Initializes the table header names according to the currently
	 * configured language.
	 */
	public SoundfontResourceTableModel() {
		
		// table header
		columnNames = new String[ 6 ];
		columnNames[ 0 ] = Dict.get( Dict.INFO_COL_SF_RES_INDEX       );
		columnNames[ 1 ] = Dict.get( Dict.INFO_COL_SF_RES_TYPE        );
		columnNames[ 2 ] = Dict.get( Dict.INFO_COL_SF_RES_NAME        );
		columnNames[ 3 ] = Dict.get( Dict.INFO_COL_SF_RES_FRAMELENGTH );
		columnNames[ 4 ] = Dict.get( Dict.INFO_COL_SF_RES_FORMAT      );
		columnNames[ 5 ] = Dict.get( Dict.INFO_COL_SF_RES_CLASS       );
		
		// column classes, used for sorting
		columnClasses = new Class[ 6 ];
		columnClasses[ 0 ] = OptionalNumber.class;
		columnClasses[ 1 ] = String.class;
		columnClasses[ 2 ] = String.class;
		columnClasses[ 3 ] = OptionalNumber.class;
		columnClasses[ 4 ] = String.class;
		columnClasses[ 5 ] = String.class;
		
		// get soundfont instruments
		resources = SoundfontParser.getSoundfontResources();
	}
	
	/**
	 * Returns the number of rows in the table - same as the sum of resources
	 * and categories.
	 * 
	 * @return    Number of rows.
	 */
	@Override
	public int getRowCount() {
		if ( null == resources )
			return 0;
		return resources.size();
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
		
		// index
		if ( 0 == colIndex ) {
			return new OptionalNumber( resources.get(rowIndex).get("index") );
		}
		
		// type
		else if ( 1 == colIndex ) {
			String type = (String) resources.get( rowIndex ).get( "type" );
			
			// Don't show the type, if it's a category.
			String category = (String) resources.get( rowIndex ).get("category");
			if (category != null)
				return "";
			
			return type;
		}
		
		// name
		else if ( 2 == colIndex ) {
			return resources.get( rowIndex ).get( "name" );
		}
		
		// frames
		else if ( 3 == colIndex ) {
			return new OptionalNumber( resources.get(rowIndex).get("frame_length") );
		}
		
		// format
		else if ( 4 == colIndex ) {
			return resources.get( rowIndex ).get( "format" );
		}
		
		// class
		else if ( 5 == colIndex ) {
			return resources.get( rowIndex ).get( "class" );
		}
		
		// default
		return "";
	}
	
	@Override
	public ArrayList<HashMap<String, ?>> getCategorizedHashMapRows() {
		return new ArrayList<HashMap<String, ?>>(resources);
	}
}
