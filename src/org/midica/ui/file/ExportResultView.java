/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.file;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;

import org.midica.config.Dict;
import org.midica.config.KeyBindingManager;
import org.midica.config.Laf;
import org.midica.ui.MessageView;
import org.midica.ui.model.ExportResultTableModel;
import org.midica.ui.renderer.MidicaTableCellRenderer;
import org.midica.ui.tablefilter.FilterIcon;
import org.midica.ui.tablesorter.ExportResultTableSorter;
import org.midica.ui.widget.MidicaTable;

/**
 * This class provides a window for showing the result of file exports.
 * 
 * @author Jan Trukenmüller
 */
public class ExportResultView extends MessageView implements ActionListener, RowSorterListener {

	private static final long serialVersionUID = 1L;
	
	// table sizes and color
	private static final int COL_WIDTH_TRACK   =  50;
	private static final int COL_WIDTH_TICK    =  80;
	private static final int COL_WIDTH_CHANNEL =  70;
	private static final int COL_WIDTH_MSG     = 200;
	private static final int COL_WIDTH_DETAIL  = 600;
	private static final int TABLE_HEIGHT      = 400;
	
	private Container   content  = null;
	private Dimension   tableDim = null;
	private MidicaTable table    = null;
	
	// filter widgets
	private JCheckBox  cbxShortMsg     = null;
	private JCheckBox  cbxMetaMsg      = null;
	private JCheckBox  cbxSysexMsg     = null;
	private JCheckBox  cbxRestSkipped  = null;
	private JCheckBox  cbxOffNotFound  = null;
	private JCheckBox  cbxCrdGrpFailed = null;
	private JCheckBox  cbxOther        = null;
	private FilterIcon filterIcon      = null;
	
	/**
	 * Creates a new export result window.
	 * The window is not visible by default and must be initialized and set visible via init().
	 * 
	 * @param owner    The parent window (Player).
	 */
	public ExportResultView(JDialog owner) {
		super(owner, Dict.get(Dict.TITLE_EXPORT_RESULT));
	}
	
	/**
	 * Creates a new export result window.
	 * The window is not visible by default and must be initialized and set visible via init().
	 * 
	 * @param owner    The parent window (main window).
	 */
	public ExportResultView(JFrame owner) {
		super(owner, Dict.get(Dict.TITLE_EXPORT_RESULT));
	}
	
	/**
	 * Initializes the export result window, writes the export result and shows the window.
	 * 
	 * @param result    Export result.
	 */
	public void init(ExportResult result) {
		// content
		content = getContentPane();
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		content.setLayout(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.NONE;
		constraints.anchor     = GridBagConstraints.WEST;
		constraints.insets     = Laf.INSETS_NWE;
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 1;
		constraints.weighty    = 0;
		
		// show success message
		JLabel successLbl = new JLabel(Dict.get(Dict.EXPORT_SUCCESS));
		content.add(successLbl, constraints);
		
		// show number of warnings
		constraints.gridy++;
		JLabel warningCountLbl = new JLabel(Dict.get(Dict.NUMBER_OF_WARNINGS) + " " +  result.countWarnings());
		content.add(warningCountLbl, constraints);
		
		// scrollable warning table (filter widgets and table)
		int tableWidth = COL_WIDTH_TRACK + COL_WIDTH_TICK + COL_WIDTH_CHANNEL + COL_WIDTH_MSG + COL_WIDTH_DETAIL;
		this.tableDim  = new Dimension(tableWidth, TABLE_HEIGHT);
		if (result.countWarnings() > 0) {
			
			// add filter widgets
			constraints.gridy++;
			content.add(createFilterWidgets(), constraints);
			
			// add the table
    		constraints.gridy++;
    		constraints.weighty = 1;
    		ExportResultTableModel tableModel = new ExportResultTableModel(result);
    		table = new MidicaTable(tableModel);
    		table.setDefaultRenderer(Object.class, new MidicaTableCellRenderer());
    		JScrollPane scroll = new JScrollPane(table);
    		scroll.setPreferredSize(tableDim);
    		content.add(scroll, constraints);
    		
    		// add row sorter/filter
    		ExportResultTableSorter<ExportResultTableModel> sorter = new ExportResultTableSorter<>();
    		sorter.setModel(tableModel);
    		table.setRowSorter(sorter);
    		
    		// set column sizes
    		table.getColumnModel().getColumn(0).setPreferredWidth(COL_WIDTH_TRACK);
    		table.getColumnModel().getColumn(1).setPreferredWidth(COL_WIDTH_TICK);
    		table.getColumnModel().getColumn(2).setPreferredWidth(COL_WIDTH_CHANNEL);
    		table.getColumnModel().getColumn(3).setPreferredWidth(COL_WIDTH_MSG);
    		table.getColumnModel().getColumn(4).setPreferredWidth(COL_WIDTH_DETAIL);
    		
    		// connect table string filter with the table
    		filterIcon.setTable(table);
    		
    		constraints.weighty = 0;
		}
		
		// close button
		constraints.gridy++;
		constraints.insets = Laf.INSETS_SWE;
		constraints.fill   = GridBagConstraints.BOTH;
		content.add(createCloseButton(), constraints);
		
		addKeyBindings();
		pack();
		setVisible(true);
	}
	
	/**
	 * Creates the area with the table filter widgets.
	 * 
	 * @return the created area.
	 */
	private Container createFilterWidgets() {
		JPanel area = new JPanel();
		
		// layout
		area.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.NONE;
		constraints.fill       = GridBagConstraints.HORIZONTAL;
		constraints.insets     = Laf.INSETS_IN;
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 0;
		constraints.weighty    = 0;
		constraints.anchor     = GridBagConstraints.NORTHWEST;
		
		// short messages
		cbxShortMsg = new JCheckBox(Dict.get(Dict.SHOW_IGN_SHORT_MSG));
		cbxShortMsg.setSelected(true);
		cbxShortMsg.addActionListener(this);
		area.add(cbxShortMsg, constraints);
		
		// meta messages
		constraints.gridy++;
		cbxMetaMsg = new JCheckBox(Dict.get(Dict.SHOW_IGN_META_MSG));
		cbxMetaMsg.setSelected(true);
		cbxMetaMsg.addActionListener(this);
		area.add(cbxMetaMsg, constraints);
		
		// sysex messages
		constraints.gridy++;
		cbxSysexMsg = new JCheckBox(Dict.get(Dict.SHOW_IGN_SYSEX_MSG));
		cbxSysexMsg.setSelected(true);
		cbxSysexMsg.addActionListener(this);
		area.add(cbxSysexMsg, constraints);
		
		// next column
		constraints.gridx++;
		constraints.gridy -= 2;
		JLabel spacer = new JLabel(" ");
		area.add(spacer, constraints);
		constraints.gridx++;
		constraints.gridy--;
		
		// skipped rests
		constraints.gridy++;
		cbxRestSkipped = new JCheckBox(Dict.get(Dict.SHOW_SKIPPED_RESTS));
		cbxRestSkipped.setSelected(true);
		cbxRestSkipped.addActionListener(this);
		area.add(cbxRestSkipped, constraints);
		
		// note-OFF not found
		constraints.gridy++;
		cbxOffNotFound = new JCheckBox(Dict.get(Dict.SHOW_OFF_NOT_FOUND));
		cbxOffNotFound.setSelected(true);
		cbxOffNotFound.addActionListener(this);
		area.add(cbxOffNotFound, constraints);
		
		// chord grouping failed
		constraints.gridy++;
		cbxCrdGrpFailed = new JCheckBox(Dict.get(Dict.SHOW_CRD_GRP_FAILED));
		cbxCrdGrpFailed.setSelected(true);
		cbxCrdGrpFailed.addActionListener(this);
		area.add(cbxCrdGrpFailed, constraints);
		
		// next column
		constraints.gridx++;
		constraints.gridy -= 2;
		JLabel spacer2 = new JLabel(" ");
		area.add(spacer2, constraints);
		constraints.gridx++;
		constraints.gridy--;
		
		// other
		constraints.gridy++;
		cbxOther = new JCheckBox(Dict.get(Dict.SHOW_OTHER_WARNINGS));
		cbxOther.setSelected(true);
		cbxOther.addActionListener(this);
		area.add(cbxOther, constraints);
		
		// next column
		constraints.gridx++;
		JLabel spacer3 = new JLabel(" ");
		area.add(spacer3, constraints);
		constraints.gridx++;
		constraints.gridy += 2;
		
		// table string filter
		filterIcon = new FilterIcon(this);
		area.add(filterIcon, constraints);
		
		return area;
	}
	
	/**
	 * Filters the warnings in the table.
	 * Called if a checkbox has been changed.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		filterWarnings();
	}
	
	@Override
	public void sorterChanged(RowSorterEvent e) {
		filterWarnings();
	}
	
	/**
	 * Filters the warnings in the table.
	 */
	private void filterWarnings() {
		ExportResultTableSorter<?> rowSorter = (ExportResultTableSorter<?>) table.getRowSorter();
		
		rowSorter.removeRowSorterListener(this);
		rowSorter.setWarningFilters(
			cbxShortMsg.isSelected(),
			cbxMetaMsg.isSelected(),
			cbxSysexMsg.isSelected(),
			cbxRestSkipped.isSelected(),
			cbxOffNotFound.isSelected(),
			cbxCrdGrpFailed.isSelected(),
			cbxOther.isSelected()
		);
		rowSorter.filter();
		rowSorter.addRowSorterListener(this);
	}
	
	/**
	 * Initializes the content of all the tabs inside the info view.
	 */
	public void addKeyBindings() {
		
		// reset everything
		KeyBindingManager keyBindingManager = new KeyBindingManager(this, this.getRootPane());
		
		// add key bindings comboboxes
		keyBindingManager.addBindingsForCheckbox(cbxShortMsg,     Dict.KEY_EXPORT_RESULT_SHORT);
		keyBindingManager.addBindingsForCheckbox(cbxMetaMsg,      Dict.KEY_EXPORT_RESULT_META);
		keyBindingManager.addBindingsForCheckbox(cbxSysexMsg,     Dict.KEY_EXPORT_RESULT_SYSEX);
		keyBindingManager.addBindingsForCheckbox(cbxRestSkipped,  Dict.KEY_EXPORT_RESULT_SKIPPED_RESTS);
		keyBindingManager.addBindingsForCheckbox(cbxOffNotFound,  Dict.KEY_EXPORT_RESULT_OFF_NOT_FOUND);
		keyBindingManager.addBindingsForCheckbox(cbxCrdGrpFailed, Dict.KEY_EXPORT_RESULT_CRD_GRP_FAILED);
		keyBindingManager.addBindingsForCheckbox(cbxOther,        Dict.KEY_EXPORT_RESULT_OTHER);
		
		// add key bindings to filters
		keyBindingManager.addBindingsForIconLabel(filterIcon, Dict.KEY_EXPORT_RESULT_FILTER);
		
		// add key bindings to buttons
		keyBindingManager.addBindingsForButton(this.closeButton, Dict.KEY_EXPORT_RESULT_CLOSE);
		
		// set input and action maps
		keyBindingManager.postprocess();
	}
}
