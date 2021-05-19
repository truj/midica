/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.file.config;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.midica.config.Dict;
import org.midica.config.KeyBindingManager;
import org.midica.config.Laf;
import org.midica.ui.widget.ConfigIcon;
import org.midica.ui.widget.MidicaButton;

/**
 * This is the base class for file-based config windows.
 * 
 * @author Jan Trukenmüller
 */
public abstract class FileConfigView extends JDialog {
	
	private static final long serialVersionUID = 1L;
	
	protected FileConfigController controller;
	protected ConfigIcon           icon;
	
	protected MidicaButton btnRestoreDefaults; // use hard-coded default
	protected MidicaButton btnRestore;         // use config from file
	protected MidicaButton btnSave;            // copy session config to config file
	
	protected KeyBindingManager keyBindingManager = null;
	
	/**
	 * Creates a window for the file-based configuration.
	 * 
	 * @param owner  the file selection window
	 * @param icon   the icon to open this window
	 * @param title  the window title
	 */
	protected FileConfigView(JDialog owner, ConfigIcon icon, String title) {
		super(owner, title, true);
		this.icon = icon;
		
		btnRestoreDefaults = new MidicaButton(Dict.get(Dict.DC_RESTORE_DEFAULTS));
		btnRestore         = new MidicaButton(Dict.get(Dict.DC_RESTORE));
		btnSave            = new MidicaButton(Dict.get(Dict.DC_SAVE));
	}
	
	/**
	 * Opens the window.
	 */
	public void open() {
		setLocationRelativeTo(icon);
		setVisible(true);
	}
	
	/**
	 * Wraps the given content of a tab inside another container.
	 * This is used to position the tab content correctly inside the tab.
	 * 
	 * @param area    the area to be wrapped
	 * @return the wrapped area.
	 */
	protected Container wrapTabContent(Container area) {
		
		// outer container and layout
		JPanel content = new JPanel();
		GridBagConstraints constraints = new GridBagConstraints();
		content.setLayout(new GridBagLayout());
		constraints.fill       = GridBagConstraints.NONE;
		constraints.insets     = Laf.INSETS_ALL;
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 1;
		constraints.weighty    = 1;
		constraints.anchor     = GridBagConstraints.NORTHWEST;
		
		// wrap it
		content.add(area, constraints);
		
		return content;
	}
	
	/**
	 * Creates {@link GridBagConstraints} that can be used for the sub areas of a tab etc.
	 * 
	 * Returns the following elements:
	 * 
	 * - left column constraints
	 * - center column constraints
	 * - right column constraints
	 * - full width constraints (for elements using all 3 columns)
	 * 
	 * @return the created constraints like described above.
	 */
	protected GridBagConstraints[] createConstraintsForArea() {
		
		GridBagConstraints constrLeft = new GridBagConstraints();
		constrLeft.fill       = GridBagConstraints.NONE;
		constrLeft.anchor     = GridBagConstraints.WEST;
		constrLeft.insets     = Laf.INSETS_W;
		constrLeft.gridx      = 0;
		constrLeft.gridy      = 1;
		constrLeft.gridheight = 1;
		constrLeft.gridwidth  = 1;
		constrLeft.weightx    = 0;
		constrLeft.weighty    = 0;
		GridBagConstraints constrCenter = (GridBagConstraints) constrLeft.clone();
		constrCenter.gridx = 1;
		constrLeft.insets  = Laf.INSETS_IN;
		GridBagConstraints constrRight = (GridBagConstraints) constrLeft.clone();
		constrRight.gridx   = 2;
		constrLeft.insets   = Laf.INSETS_E;
		constrRight.weightx = 1.0;
		constrRight.fill    = GridBagConstraints.HORIZONTAL;
		GridBagConstraints constrFull = (GridBagConstraints) constrCenter.clone();
		constrFull.gridx     = 0;
		constrFull.gridy     = 0;
		constrFull.gridwidth = 3;
		constrFull.weightx   = 1.0;
		constrFull.insets    = Laf.INSETS_ZERO;
		constrFull.fill      = GridBagConstraints.HORIZONTAL;
		constrFull.anchor    = GridBagConstraints.CENTER;
		
		return new GridBagConstraints[] {
			constrFull,
			constrLeft,
			constrCenter,
			constrRight,
		};
	}
	
	/**
	 * Creates the area for buttons.
	 * 
	 * @return the created area
	 */
	protected Container createButtonArea() {
		JPanel area = new JPanel();
		
		// layout
		area.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.NONE;
		constraints.insets     = Laf.INSETS_IN;
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 0;
		constraints.weighty    = 0;
		
		// restore button
		btnRestore.addActionListener(controller);
		area.add(btnRestore, constraints);
		
		// restore defaults button
		constraints.gridx++;
		btnRestoreDefaults.addActionListener(controller);
		area.add(btnRestoreDefaults, constraints);
		
		// save button
		constraints.gridx++;
		btnSave.addActionListener(controller);
		area.add(btnSave, constraints);
		
		return area;
	}
	
	/**
	 * Creates the info area for a tab.
	 * 
	 * @param tabKey   language key for the titled border (same as the tab name)
	 * @param infoKey  language key for the info text
	 * @return the component containing the tab info
	 */
	protected JComponent createTabInfo(String tabKey, String infoKey) {
		JPanel area = new JPanel();
		
		// border
		area.setBorder( Laf.createTitledBorder(Dict.get(tabKey)) );
		
		// layout
		area.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.BOTH;
		constraints.insets     = Laf.INSETS_IN;
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 1;
		constraints.weighty    = 1;
		constraints.anchor     = GridBagConstraints.NORTHWEST;
		
		// info text
		JLabel lbl = new JLabel(Dict.get(infoKey));
		area.add(lbl, constraints);
		
		return area;
	}
	
	/**
	 * Adds all key bindings to the info window (general and specific key bindings).
	 * 
	 * The specific key bindings should be implemented directly in this method.
	 * The general bindings should be added
	 * by calling {@link #addGeneralKeyBindings()}.
	 */
	protected abstract void addKeyBindings();
	
	/**
	 * Adds general key bindings that are used by all file config windows.
	 */
	protected void addGeneralKeyBindings() {
		
		// close bindings
		keyBindingManager.addBindingsForClose( Dict.KEY_FILE_CONF_CLOSE );
		
		// restore/save buttons
		keyBindingManager.addBindingsForButton( btnSave,            Dict.KEY_FILE_CONF_SAVE            );
		keyBindingManager.addBindingsForButton( btnRestore,         Dict.KEY_FILE_CONF_RESTORE_SAVED   );
		keyBindingManager.addBindingsForButton( btnRestoreDefaults, Dict.KEY_FILE_CONF_RESTORE_DEFAULT );
	}
}
