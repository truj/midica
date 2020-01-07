/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.tablefilter;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.RowSorterListener;

import org.midica.config.Dict;
import org.midica.config.KeyBindingManager;
import org.midica.config.Laf;
import org.midica.ui.tablesorter.MidicaSorter;
import org.midica.ui.widget.MidicaButton;
import org.midica.ui.widget.MidicaTable;

/**
 * This class provides a layer where a string filter for a table can be entered.
 * This layer is opened when a filter icon has been clicked.
 * 
 * @author Jan Trukenmüller
 */
public class StringFilterLayer extends JDialog implements WindowListener, WindowFocusListener {
	
	private static final long serialVersionUID = 1L;
	
	private static final int LAYER_WIDTH       = 170;
	private static final int LAYER_HEIGHT      = 100;
	private static final int BORDER_WIDTH      =   3;
	private static final int TEXT_FIELD_WIDTH  = 150;
	private static final int TEXT_FIELD_HEIGHT =  30;
	private static final int ARC_WIDTH         =  10;
	private static final int ARC_HEIGHT        =  10;
	
	private MidicaTable       table;
	private FilterIcon        icon;
	private JTextField        textfield;
	private MidicaButton      clearBtn;
	private KeyBindingManager keyBindingManager;
	
	/**
	 * Creates a filter layer for filtering a string-based table filter.
	 * 
	 * @param icon     The filter icon that's clicked to open this layer.
	 * @param owner    The window containing icon and table.
	 */
	public StringFilterLayer(FilterIcon icon, Window owner) {
		super(owner);
		this.icon = icon;
		textfield = new JTextField();
		
		// window look and shape
		setUndecorated(true);
		setSize(LAYER_WIDTH, LAYER_HEIGHT);
		setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), ARC_WIDTH, ARC_HEIGHT));
		
		// fill the content
		init();
		addKeyBindings();
		
		// make it draggable
		DragListener drag = new DragListener();
		addMouseListener( drag );
		addMouseMotionListener( drag );
		
		// add window listener and key bindings
		this.addWindowListener(this);
		
		// close if focus is lost
		this.addWindowFocusListener(this);
		
		// process filter changes
		textfield.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				filterUpdated();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				filterUpdated();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				filterUpdated();
			}
		});
		
		// add functionality for the clear button
		clearBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				textfield.setText("");
				filterUpdated();
			}
		});
	}
	
	/**
	 * Sets the table that's filtered by the filter.
	 * 
	 * @param table  Table to be filtered.
	 */
	public void setTable(MidicaTable table) {
		this.table = table;
	}
	
	/**
	 * Initializes the content of the filter layer.
	 */
	private void init() {
		
		// background
		JRootPane rootPane = getRootPane();
		rootPane.setBackground(Laf.COLOR_TBL_FILTER_LAYER_BACKGROUND);
		rootPane.setBorder(new RoundedBorder(ARC_WIDTH, ARC_HEIGHT, BORDER_WIDTH, Laf.COLOR_BORDER));
		rootPane.setOpaque(true);
		
		// layout
		Container content = getRootPane();
		content.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.NONE;
		constraints.insets     = Laf.INSETS_IN;
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 0;
		constraints.weighty    = 0;
		
		// label
		JLabel label = new JLabel( Dict.get(Dict.FILTER_LAYER_LABEL) );
		Laf.makeBold(label);
		content.add(label, constraints);
		
		// text field
		textfield.setPreferredSize( new Dimension(TEXT_FIELD_WIDTH, TEXT_FIELD_HEIGHT) );
		textfield.setText("");
		constraints.gridy++;
		content.add(textfield, constraints);
		
		// clear button
		constraints.gridy++;
		clearBtn = new MidicaButton( Dict.get(Dict.FILTER_LAYER_CLEAR) );
		content.add(clearBtn, constraints);
	}
	
	/**
	 * Opens the filter layer.
	 */
	public void open() {
		setLocationRelativeTo(icon);
		setVisible(true);
	}
	
	/**
	 * Closes the filter layer.
	 */
	public void close() {
		setVisible(false);
		dispose();
	}
	
	/**
	 * Indicates, if the string filter layer is currently open or not.
	 * 
	 * @return  **true**, if the layer is open, otherwise **false**;
	 */
	public boolean isFilterLayerOpen() {
		return isVisible();
	}
	
	/**
	 * Handles changes to the filter text field.
	 */
	private void filterUpdated() {
		String text = textfield.getText();
		if (null == text || "".equals(text)) {
			icon.setActive(false);
		}
		else {
			icon.setActive(true);
		}
		((MidicaSorter) table.getRowSorter()).setStringFilter(text);
	}
	
	/**
	 * Adds a {@link RowSorterListener} to the row sorter.
	 * 
	 * @param listener  The listener to be added.
	 */
	public void addRowSorterListener(RowSorterListener listener) {
		table.getRowSorter().addRowSorterListener(listener);
	}
	
	@Override
	public void windowActivated(WindowEvent e) {
		this.textfield.requestFocus();
	}
	
	@Override
	public void windowOpened(WindowEvent e) {
	}
	
	@Override
	public void windowClosing(WindowEvent e) {
	}
	
	@Override
	public void windowClosed(WindowEvent e) {
	}
	
	@Override
	public void windowIconified(WindowEvent e) {
	}
	
	@Override
	public void windowDeiconified(WindowEvent e) {
	}
	
	@Override
	public void windowDeactivated(WindowEvent e) {
	}
	
	@Override
	public void windowGainedFocus(WindowEvent e) {
	}
	
	@Override
	public void windowLostFocus(WindowEvent e) {
		filterUpdated();
		close();
	}
	
	/**
	 * Adds filter layer specific key bindings.
	 */
	private void addKeyBindings() {
		
		// reset everything
		keyBindingManager = new KeyBindingManager(this, this.getRootPane());
		
		// add close bindings
		keyBindingManager.addBindingsForClose( Dict.KEY_STRING_FILTER_CLOSE );
		
		// add key bindings for the clear button
		keyBindingManager.addBindingsForButton( this.clearBtn, Dict.KEY_STRING_FILTER_CLEAR );
		
		// postprocess
		keyBindingManager.postprocess();
	}
}
