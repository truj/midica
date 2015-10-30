/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.model;

import java.util.ArrayList;

import org.midica.config.Dict;
import org.midica.ui.info.SyntaxElement;


/**
 * This class represents the data model of the syntax table in the
 * configuration details view of the info window.
 * 
 * Each row represents either a command category or a syntax command.
 * 
 * @author Jan Trukenmüller
 */
public class SyntaxTableModel extends MidicaTableModel {

	private static final long serialVersionUID = 1L;
    
	private ArrayList<SyntaxElement> syntaxList = null;
	
	/**
	 * Creates a new instance of the syntax table data model.
	 * Initializes the table header names according to the currently configured syntax
	 * and language.
	 */
	public SyntaxTableModel() {
		
		// table header
		columnNames = new String[ 3 ];
		columnNames[ 0 ] = Dict.get( Dict.CONF_COL_SYNTAX_NAME     );
		columnNames[ 1 ] = Dict.get( Dict.CONF_COL_SYNTAX_SHORTCUT );
		columnNames[ 2 ] = Dict.get( Dict.CONF_COL_SYNTAX_DESC     );
		
		syntaxList = Dict.getSyntaxList();
	}
	
	/**
	 * Returns the number of rows in the table - same as the sum of syntax commands
	 * and categories.
	 * 
	 * @return    Number of rows (commands + categories).
	 */
	@Override
	public int getRowCount() {
		if ( null == syntaxList )
			return 0;
		return syntaxList.size();
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
		
		// name
		if ( 0 == colIndex )
			return syntaxList.get( rowIndex ).name;
		
		// keyword
		else if ( 1 == colIndex ) {
			return syntaxList.get( rowIndex ).keyword;
		}
		
		// description
		else if ( 2 == colIndex ) {
			return syntaxList.get( rowIndex ).description;
		}
		
		// default
		return Dict.get( Dict.UNKNOWN_SYNTAX );
	}
}


