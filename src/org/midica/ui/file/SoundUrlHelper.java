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
import java.io.File;
import java.security.NoSuchAlgorithmException;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.midica.config.Dict;
import org.midica.config.Laf;
import org.midica.file.read.SoundbankParser;
import org.midica.ui.UiController;
import org.midica.ui.widget.MidicaButton;

/**
 * Helper class for URL-based choosers.
 * (Currently only used for the URL chooser in the sound parser)
 * 
 * @author Jan Trukenmüller
 */
public class SoundUrlHelper {
		
	public static final String CMD_URL_CHOSEN = "cmd_url_chosen";
	
	private static final String cacheFileRegex = "^[0-9a-f]+$";
	
	private static String url = null;
	
	private static JTextField   fldUrl         = null;
	private static JCheckBox    cbxDelCacheOne = null;
	private static JCheckBox    cbxDelCacheAll = null;
	private static MidicaButton btnDelCacheOne = null;
	private static MidicaButton btnDelCacheAll = null;
	private static JLabel       lblCachedOne   = null;
	private static JLabel       lblCachedAll   = null;
	private static MidicaButton btnDownload    = null;
	private static JPanel[]     cacheAreas     = new JPanel[2];
	
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
		
		// URL description label
		JLabel lblDesc = new JLabel(Dict.get(Dict.SND_URL));
		area.add(lblDesc, constraints);
		
		// url field
		constraints.gridy++;
		constraints.insets = Laf.INSETS_WE;
		fldUrl = new JTextField();
		area.add(fldUrl, constraints);
		
		// separator
		constraints.gridy++;
		constraints.insets = Laf.INSETS_NWE;
		area.add(Laf.createSeparator(), constraints);
		
		// delete cache ONE area
		lblCachedOne   = new JLabel();
		cbxDelCacheOne = new JCheckBox(Dict.get(Dict.SND_CACHE_DEL_ONE));
		btnDelCacheOne = new MidicaButton(Dict.get(Dict.SND_CACHE_DELETE));
		constraints.gridy++;
		constraints.insets  = Laf.INSETS_WE;
		area.add(createCacheArea(false), constraints);
		
		// separator
		constraints.gridy++;
		area.add(Laf.createSeparator(), constraints);
		
		// delete cache ALL area
		lblCachedAll   = new JLabel();
		cbxDelCacheAll = new JCheckBox(Dict.get(Dict.SND_CACHE_DEL_ALL));
		btnDelCacheAll = new MidicaButton(Dict.get(Dict.SND_CACHE_DELETE));
		constraints.gridy++;
		constraints.insets  = Laf.INSETS_WE;
		area.add(createCacheArea(true), constraints);
		
		// separator
		constraints.gridy++;
		area.add(Laf.createSeparator(), constraints);
		
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
		btnDownload = new MidicaButton(Dict.get(Dict.SND_DOWNLOAD_URL), true);
		area.add(btnDownload, constraints);
		
		// close button
		constraints.gridx++;
		constraints.insets    = Laf.INSETS_SE;
		MidicaButton btnClose = new MidicaButton(Dict.get(Dict.CLOSE), false);
		area.add(btnClose, constraints);
		
		// add listeners
		fldUrl.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				displayCacheInfo();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				displayCacheInfo();
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				displayCacheInfo();
			}
		});
		fldUrl.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				btnDownload.doClick();
			}
		});
		btnClose.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selector.setVisible(false);
			}
		});
		btnDownload.setActionCommand(CMD_URL_CHOSEN);
		btnDownload.addActionListener(controller);
		
		return area;
	}
	
	/**
	 * Creates the area for controlling one cache type (chosen url or whole cache).
	 * Contains: info label and sub area (with checkbox and button).
	 * 
	 * @param allUrls  **true** for the whole cache, **false** for the chosen URL
	 * @return the created cache controlling area
	 */
	public static JComponent createCacheArea(boolean allUrls) {
		
		JLabel lbl = allUrls ? lblCachedAll : lblCachedOne;
		JPanel area = new JPanel();
		cacheAreas[allUrls ? 1 : 0] = area;
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill    = GridBagConstraints.HORIZONTAL;
		constraints.insets  = Laf.INSETS_NWE;
		constraints.gridx   = 0;
		constraints.gridy   = 0;
		constraints.weightx = 1;
		constraints.weighty = 0;
		
		// label
		area.add(lbl, constraints);
		
		// checkbox and button
		constraints.gridy++;
		constraints.insets = Laf.INSETS_SWE;
		area.add(createDelCacheArea(allUrls), constraints);
		
		return area;
	}
	
	/**
	 * Creates the DELETE part sub area for one cache type (chosen url or whole cache).
	 * Contains: checkbox and button.
	 * 
	 * @param allUrls  **true** for the whole cache, **false** for the chosen URL
	 * @return the created area
	 */
	public static JComponent createDelCacheArea(boolean allUrls) {
		
		JCheckBox    cbx = allUrls ? cbxDelCacheAll : cbxDelCacheOne;
		MidicaButton btn = allUrls ? btnDelCacheAll : btnDelCacheOne;
		
		JPanel area = new JPanel();
		btn.setEnabled(false);
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill    = GridBagConstraints.NONE;
		constraints.insets  = Laf.INSETS_ZERO;
		constraints.gridx   = 0;
		constraints.gridy   = 0;
		constraints.weightx = 0;
		constraints.weighty = 0;
		
		// checkbox
		area.add(cbx, constraints);
		
		// horizontal spacer
		constraints.fill    = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1;
		constraints.gridx++;
		JLabel spacer = new JLabel("");
		area.add(spacer, constraints);
		
		// button
		constraints.fill    = GridBagConstraints.NONE;
		constraints.weightx = 0;
		constraints.gridx++;
		area.add(btn, constraints);
		
		// checkbox listener
		cbx.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (cbx.isSelected())
					btn.setEnabled(true);
				else
					btn.setEnabled(false);
			}
		});
		
		// button listener
		btn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				try {
					if (allUrls) {
						File cacheDir = SoundbankParser.getUrlCacheDir();
						for (File file : cacheDir.listFiles()) {
							String name = file.getName();
							if (name.matches(cacheFileRegex)) {
								file.delete();
							}
						}
					}
					else {
						getCachedUrl().delete();
					}
				}
				catch (NoSuchAlgorithmException ex) {
					ex.printStackTrace();
				}
				displayCacheInfo();
			}
		});
		
		return area;
		
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
	 * Returns the url text field.
	 * Needed for the initialization of key bindings.
	 * 
	 * @return url text field
	 */
	public static JTextField getUrlField() {
		return fldUrl;
	}
	
	/**
	 * Returns the download button.
	 * Needed for the initialization of key bindings.
	 * 
	 * @return download button
	 */
	public static MidicaButton getDownloadButton() {
		return btnDownload;
	}
	
	/**
	 * Returns one of the delete-cache checkboxes.
	 * 
	 * @param allUrls  **true** to get the checkbox to delete ALL urls, **false** for ONE url.
	 * @return the selected checkbox
	 */
	public static JCheckBox getCheckBox(boolean allUrls) {
		if (allUrls)
			return cbxDelCacheAll;
		else
			return cbxDelCacheOne;
	}
	
	/**
	 * Returns one of the delete-cache buttons.
	 * 
	 * @param allUrls  **true** to get the button to delete ALL urls, **false** for ONE url.
	 * @return the requested button
	 */
	public static MidicaButton getDeleteCacheBtn(boolean allUrls) {
		if (allUrls)
			return btnDelCacheAll;
		else
			return btnDelCacheOne;
	}
	
	/**
	 * (Re)calculates and the cache status and updates the according info labels.
	 * Unselects the checkboxes.
	 * 
	 * The info to be displayed:
	 * 
	 * - is the chosen URL cached or not (if yes, how many MB)
	 * - how many cached URLs (and the total cache size)
	 * - Download button text
	 */
	public static void displayCacheInfo() {
		url = fldUrl.getText();
		try {
			// single url
			File   cachedUrl = getCachedUrl();
			String textOne   = Dict.get(Dict.SND_CACHE_LBL_NO_ONE);
			String textBtn   = Dict.get(Dict.SND_DOWNLOAD_URL);
			if (cachedUrl.exists()) {
				double mb = ((double) cachedUrl.length()) / (1024 * 1024);
				textOne   = Dict.get(Dict.SND_CACHE_LBL_YES_ONE);
				textOne   = String.format(textOne, mb);
				textBtn   = Dict.get(Dict.SND_DOWNLOAD_CACHE);
			}
			lblCachedOne.setText(textOne);
			btnDownload.setText(textBtn);
			
			// whole cache
			File   cacheDir  = SoundbankParser.getUrlCacheDir();
			int    fileCount = 0;
			double byteCount = 0;
			for (File file : cacheDir.listFiles()) {
				String name = file.getName();
				if (name.matches(cacheFileRegex)) {
					fileCount++;
					byteCount += file.length();
				}
			}
			String textAll = Dict.get(Dict.SND_CACHE_LBL_NO_ALL);
			if (fileCount > 0) {
				byteCount /= (1024 * 1024);
				textAll = Dict.get(Dict.SND_CACHE_LBL_YES_ALL);
				textAll = String.format(textAll, fileCount, byteCount);
			}
			lblCachedAll.setText(textAll);
		}
		catch (NoSuchAlgorithmException e) {
		}
		
		// display the change immediately
		for (JPanel area : cacheAreas) {
			area.repaint();
		}
		
		// un-select the checkboxes
		cbxDelCacheOne.setSelected(false);
		cbxDelCacheAll.setSelected(false);
	}
	
	/**
	 * Calculates the cache file path of the url in the text field.
	 * 
	 * @return the file
	 * @throws NoSuchAlgorithmException
	 */
	private static File getCachedUrl() throws NoSuchAlgorithmException {
		File   cacheDir  = SoundbankParser.getUrlCacheDir();
		String hash      = SoundbankParser.getUrlHash(url);
		File   cachedUrl = new File(cacheDir.getAbsoluteFile() + File.separator + hash);
		return cachedUrl;
	}
}
