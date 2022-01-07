/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.file;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.midica.config.Config;
import org.midica.config.Dict;
import org.midica.config.Laf;
import org.midica.file.read.SoundfontParser;
import org.midica.ui.UiController;
import org.midica.ui.model.ComboboxStringOption;
import org.midica.ui.model.ConfigComboboxModel;
import org.midica.ui.widget.MidicaButton;

/**
 * Helper class for URL-based choosers.
 * (Currently only used for the URL chooser in the sound parser)
 * 
 * @author Jan Trukenmüller
 */
public class SoundUrlHelper {
		
	public static final String CMD_URL_CHOSEN = "cmd_url_chosen";
	
	private static String url         = null;
	private static String soundFormat = null;
	
	private static JTextField fldUrl = null;
	private static JComboBox<ComboboxStringOption> cbxFormat = null;
	private static MidicaButton btnOpen = null;
	
	/**
	 * Returns the chosen URL, if available.
	 * 
	 * @return the URL or **null**.
	 */
	public static String getSoundUrl() {
		return url;
	}
	
	/**
	 * Creates a form where a URL can be typed in.
	 * 
	 * @param controller   listener for URL choosing events
	 * @param selector     file selector window
	 * @return the created form
	 */
	public static JComponent createUrlForm(UiController controller, FileSelector selector) {
		JPanel area = new JPanel();
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.HORIZONTAL;
		constraints.insets     = Laf.INSETS_NWE;
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 3;
		constraints.weightx    = 1;
		constraints.weighty    = 0;
		constraints.anchor     = GridBagConstraints.NORTHWEST;
		
		// URL description label
		JLabel lblDesc = new JLabel(Dict.get(Dict.SOUND_URL));
		area.add(lblDesc, constraints);
		
		// url field
		constraints.gridy++;
		constraints.insets = Laf.INSETS_NE;
		constraints.anchor = GridBagConstraints.WEST;
		fldUrl = new JTextField();
		area.add(fldUrl, constraints);
		
		// format label
		constraints.gridy++;
		JLabel lblFormat = new JLabel(Dict.get(Dict.SOUND_FORMAT));
		area.add(lblFormat, constraints);
		
		// format selectbox
		constraints.gridy++;
		cbxFormat = new JComboBox<ComboboxStringOption>();
		area.add(cbxFormat, constraints);
		
		// vertical spacer
		constraints.gridy++;
		constraints.weighty = 1;
		JLabel spacer = new JLabel("");
		area.add(spacer, constraints);
		
		// button area: horizontal spacer
		constraints.gridy++;
		constraints.weighty   = 0;
		constraints.weightx   = 1;
		constraints.gridwidth = 1;
		JLabel hSpacer = new JLabel("");
		area.add(hSpacer, constraints);
		
		// open button
		constraints.gridx++;
		constraints.weightx  = 0;
		constraints.insets   = Laf.INSETS_S;
		btnOpen = new MidicaButton(Dict.get(Dict.SOUND_DOWNLOAD), true);
		area.add(btnOpen, constraints);
		
		// close button
		constraints.gridx++;
		constraints.insets    = Laf.INSETS_SE;
		MidicaButton btnClose = new MidicaButton(Dict.get(Dict.CLOSE), false);
		area.add(btnClose, constraints);
		
		// create and add selectbox model
		ArrayList<ComboboxStringOption> cbxOptions = new ArrayList<>();
		cbxOptions.add(new ComboboxStringOption(SoundfontParser.SOUND_FORMAT_DLS, Dict.get(Dict.SOUND_TYPE_DLS)));
		cbxOptions.add(new ComboboxStringOption(SoundfontParser.SOUND_FORMAT_SF2, Dict.get(Dict.SOUND_TYPE_SF2)));
		ConfigComboboxModel.initModel(cbxOptions, Config.URL_SOUND_FORMAT);
		cbxFormat.setModel(ConfigComboboxModel.getModel(Config.URL_SOUND_FORMAT));
		
		// add listeners
		cbxFormat.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				ComboboxStringOption item = (ComboboxStringOption) cbxFormat.getSelectedItem();
				soundFormat = item.getIdentifier();
			}
		});
		fldUrl.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				url = fldUrl.getText();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				url = fldUrl.getText();
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				url = fldUrl.getText();
			}
		});
		fldUrl.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				btnOpen.doClick();
			}
		});
		btnClose.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selector.setVisible(false);
			}
		});
		btnOpen.setActionCommand(CMD_URL_CHOSEN);
		btnOpen.addActionListener(controller);
		
		return area;
	}
	
	/**
	 * Returns the selected sound format.
	 * 
	 * @return selected sound format
	 */
	public static String getSoundFormat() {
		return soundFormat;
	}
	
	/**
	 * Writes the url into the text field.
	 * Needed on startup, when remember_sound is set.
	 * 
	 * @param url  the url to be set.
	 */
	public static void setUrl(String url) {
		fldUrl.setText(url);
	}
	
	/**
	 * Sets the sound url format.
	 * Needed on startup, when remember_sound is set.
	 * 
	 * @param format
	 */
	public static void setSoundUrlFormat(String format) {
		soundFormat = format;
	}
	
	/**
	 * Returns the url text field.
	 * Needed for the initialization of key bindings.
	 * 
	 * @return url text field
	 */
	public static JTextField getUrlField() {
		return fldUrl;
	}
	
	/**
	 * Returns the format combobox.
	 * Needed for the initialization of key bindings.
	 * 
	 * @return
	 */
	public static JComboBox<ComboboxStringOption> getFormatCbx() {
		return cbxFormat;
	}
	
	/**
	 * Returns the download button.
	 * Needed for the initialization of key bindings.
	 * 
	 * @return download button
	 */
	public static MidicaButton getDownloadButton() {
		return btnOpen;
	}
}
