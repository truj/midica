/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import org.midica.config.Dict;
import org.midica.file.ExportResult;
import org.midica.ui.model.ExportResultTableModel;
import org.midica.ui.renderer.MidicaTableCellRenderer;
import org.midica.ui.widget.MidicaTable;


/**
 * This class provides a window for showing the result of file exports.
 * 
 * @author Jan Trukenmüller
 */
public class ExportResultView extends MessageView {

	private static final long serialVersionUID = 1L;
	
	// table sizes and color
	private static final int COL_WIDTH_TICK    =  80;
	private static final int COL_WIDTH_CHANNEL =  80;
	private static final int COL_WIDTH_NOTE    = 170;
	private static final int COL_WIDTH_MSG     = 500;
	private static final int TABLE_HEIGHT      = 400;
	
	private ActionListener controller = null;
	private Container      content    = null;
	private Dimension      tableDim   = null;
	
	/**
	 * Creates a new export result window.
	 * The window is not visible by default and must be initialized and set visible via init().
	 * 
	 * @param view          The parent window (owner).
	 * @param controller    The controller that acts as an {@link ActionListener} for this
	 *                      window.
	 */
	public ExportResultView( JDialog view, ActionListener controller ) {
		super( view, Dict.get(Dict.TITLE_EXPORT_RESULT) );
		this.controller = controller;
		int tableWidth  = COL_WIDTH_TICK + COL_WIDTH_CHANNEL + COL_WIDTH_NOTE + COL_WIDTH_MSG;
		this.tableDim   = new Dimension( tableWidth, TABLE_HEIGHT );
	}
	
	/**
	 * Initializes the export result window, writes the export result and shows the window.
	 * 
	 * @param result    Export result.
	 */
	public void init( ExportResult result ) {
		// content
		content = getContentPane();
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		content.setLayout( layout );
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill   = GridBagConstraints.BOTH;
		constraints.insets = new Insets( 2, 2, 2, 2 );
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 1;
		constraints.weighty    = 0;
		
		// show success message
		JLabel successLbl = new JLabel( Dict.get(Dict.EXPORT_SUCCESS) );
		content.add( successLbl, constraints );
		
		// show number of warnings
		constraints.gridy++;
		JLabel warningCountLbl = new JLabel( Dict.get(Dict.NUMBER_OF_WARNINGS) + " " +  result.countWarnings() );
		content.add( warningCountLbl, constraints );
		
		// scrollable warning table
		if ( result.countWarnings() > 0 ) {
    		constraints.gridy++;
    		constraints.weighty = 1;
    		MidicaTable table = new MidicaTable();
    		table.setModel( new ExportResultTableModel(result) );
    		table.setDefaultRenderer( Object.class, new MidicaTableCellRenderer() );
    		JScrollPane scroll = new JScrollPane( table );
    		scroll.setPreferredSize( tableDim );
    		content.add( scroll, constraints );
    		
    		// set column sizes
    		table.getColumnModel().getColumn( 0 ).setPreferredWidth( COL_WIDTH_TICK    );
    		table.getColumnModel().getColumn( 1 ).setPreferredWidth( COL_WIDTH_CHANNEL );
    		table.getColumnModel().getColumn( 2 ).setPreferredWidth( COL_WIDTH_NOTE    );
    		table.getColumnModel().getColumn( 3 ).setPreferredWidth( COL_WIDTH_MSG     );
    		
    		constraints.weighty = 0;
		}
		
		// close button
		constraints.gridy++;
		closeButton = new JButton( Dict.get(Dict.CLOSE) );
		closeButton.setActionCommand( MessageView.CMD_CLOSE );
		closeButton.addActionListener( controller );
		closeButton.requestFocusInWindow();
		content.add( closeButton, constraints );
		
		pack();
		setVisible( true );
	}
}
