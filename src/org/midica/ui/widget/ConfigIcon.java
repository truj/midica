/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.widget;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import org.midica.config.Cli;
import org.midica.config.Dict;
import org.midica.config.KeyBindingManager;
import org.midica.config.Laf;
import org.midica.ui.file.config.AudioConfigView;
import org.midica.ui.file.config.DecompileConfigView;
import org.midica.ui.file.config.FileConfigView;

/**
 * This class provides an icon for opening a file-based config window.
 * (E.g. for decompilation options, audio options, etc.)
 * When clicked, it opens the window where the options can be adjusted.
 * 
 * @author Jan Trukenmüller
 */
public class ConfigIcon extends JLabel implements IOpenIcon {
	
	public static final int TYPE_NONE      = 0; // pseudo type
	public static final int TYPE_DECOMPILE = 1;
	public static final int TYPE_AUDIO     = 2;
	
	private static final long serialVersionUID = 1L;
	
	private static final String pathIconWrong = "org/midica/resources/config-icon-wrong.png";
	private static final String pathIconOk    = "org/midica/resources/config-icon-ok.png";
	
	private int            type;
	private FileConfigView configView;
	
	private JDialog   winOwner  = null;
	private ImageIcon iconWrong = null;
	private ImageIcon iconOk    = null;
	
	private boolean isConfigOk = true;
	
	private static final Border borderWrong = new LineBorder(Laf.COLOR_CONFIG_ICON_BORDER_WRONG, 1);
	private static final Border borderOk    = new LineBorder(Laf.COLOR_CONFIG_ICON_BORDER_OK,    1);
	
	private String keyBindingId     = null;
	private String keyBindingTypeId = null;
	
	/**
	 * Creates the config icon.
	 * 
	 * @param owner  The window that contains the icon.
	 * @param type   The window type.
	 */
	public ConfigIcon(JDialog owner, int type) {
		winOwner  = owner;
		this.type = type;
		if (!Cli.isCliMode) {
			iconWrong = new ImageIcon( ClassLoader.getSystemResource(pathIconWrong) );
			iconOk    = new ImageIcon( ClassLoader.getSystemResource(pathIconOk)    );
		}
		setIcon(iconOk);
		setOpaque(true);
		setAppearance(true);
		setBackground(null);
		
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				open();
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				if (isConfigOk)
					setBackground(Laf.COLOR_CONFIG_ICON_HOVER_BG_OK);
				else
					setBackground(Laf.COLOR_CONFIG_ICON_HOVER_BG_WRONG);
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				setBackground(null);
			}
		});
	}
	
	/**
	 * Changes icon and tooltip according to the config state.
	 * 
	 * A config icon looks different for a correct or incorrect config.
	 * 
	 * @param ok    **true**, if the config is correct, otherwise **false**.
	 */
	public void setAppearance(boolean ok) {
		isConfigOk = ok;
		setBackground(null);
		
		// icon and tooltip
		String tooltip = "<html><b>" + Dict.get(Dict.CONFIG_ICON_TOOLTIP) + "</b><br>\n";
		if (ok) {
			setIcon(iconOk);
			tooltip += Dict.get(Dict.CONFIG_ICON_TOOLTIP_OK);
			setBorder(borderOk);
		}
		else {
			setIcon(iconWrong);
			tooltip += Dict.get(Dict.CONFIG_ICON_TOOLTIP_WRONG);
			setBorder(borderWrong);
		}
		setToolTipText(tooltip);
		
		// add the key binding related part of the tooltip, if available
		if (keyBindingId != null && keyBindingTypeId != null) {
			KeyBindingManager.addTooltip(this, keyBindingId, keyBindingTypeId);
		}
	}
	
	@Override
	public void rememberKeyBindingId(String keyBindingId, String ttType) {
		this.keyBindingId     = keyBindingId;
		this.keyBindingTypeId = ttType;
	}
	
	/**
	 * Opens the config window.
	 * Called when activated by key binding.
	 */
	@Override
	public void open() {
		if (ConfigIcon.TYPE_DECOMPILE == type)
			configView = new DecompileConfigView(winOwner, this);
		else if (ConfigIcon.TYPE_AUDIO == type)
			configView = new AudioConfigView(winOwner, this);
		else
			assert(false);
		
		configView.open();
	}
}
