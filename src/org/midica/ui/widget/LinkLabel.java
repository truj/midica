/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.widget;

import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.Border;

import org.midica.config.Dict;
import org.midica.config.Laf;

/**
 * This class provides a label containing a clickable link.
 * 
 * Actually it's a {@link JTextArea} that looks and behaves like a label.
 * 
 * @author Jan Trukenmüller
 */
public class LinkLabel extends JTextArea {
	
	private static final long serialVersionUID = 1L;
	
	private static final String LINK_FONT_NAME      = "monospaced";
	private static       Font   LINK_FONT_NORMAL    = null;
	private static       Font   LINK_FONT_MOUSEOVER = null;
	private              URI    uri                 = null;
	
	private Map<TextAttribute, Integer> underlineAttr = new HashMap<TextAttribute, Integer>();
	
	/**
	 * Creates a new {@link LinkLabel}.
	 * 
	 * @param link  The link to be displayed.
	 */
	public LinkLabel(String link) {
		
		// set text and tooltip
		this.setText(link);
		this.setToolTipText( "<html>" + link + "<br>" + Dict.get(Dict.LINK_TOOLTIP) );
		
		// set colors
		this.setBackground( Laf.COLOR_PANEL );
		this.setForeground( Laf.COLOR_LINK  );
		
		// set uri
		try {
			this.uri = new URI(link);
		}
		catch (URISyntaxException e) {
		}
		
		// set font
		if (LINK_FONT_NORMAL == null) {
			underlineAttr.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
			Font labelFont      = UIManager.getFont("Label.font");
			int  size           = labelFont.getSize();
			LINK_FONT_NORMAL    = new Font(LINK_FONT_NAME, Font.BOLD, size);
			LINK_FONT_MOUSEOVER = LINK_FONT_NORMAL.deriveFont(underlineAttr);
		}
		this.setFont(LINK_FONT_NORMAL);
		
		// Make it look like a label.
		this.setOpaque( false );
		
		// Make it behave like a label.
		this.setEditable( false );
		
		// adds the mouse listener with the following functionality:
		// 
		// - underline the font on mouse over
		// - restore the font on mouse out
		// - open link in browser on click
		this.addMouseListener(new MouseAdapter() {
			
			/**
			 * Changes the font to be underlined.
			 * 
			 * @param e  the mouse event
			 */
			@Override
			public void mouseEntered(MouseEvent e) {
				setFont(LINK_FONT_MOUSEOVER);
			}
			
			/**
			 * Changes the font to normal (removes underlining).
			 * 
			 * @param e  the mouse event
			 */
			@Override
			public void mouseExited(MouseEvent e) {
				setFont(LINK_FONT_NORMAL);
			}
			
			/**
			 * Opens the link in the default browser.
			 * 
			 * @param e  the mouse event
			 */
			@Override
			public void mouseClicked(MouseEvent ev) {
				try {
					Desktop desktop   = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
					boolean canBrowse = desktop != null && desktop.isSupported( Desktop.Action.BROWSE );
					if (canBrowse) {
						Desktop.getDesktop().browse(uri);
					}
					else {
						String  os      = System.getProperty("os.name").toLowerCase();
						Runtime runtime = Runtime.getRuntime();
						if (os.contains("mac")) {
							runtime.exec("open " + uri);
						}
						else if (os.contains("nix") || os.contains("nux")) {
							runtime.exec("xdg-open " + uri);
						}
					}
				}
				catch (IOException e) {
				}
			}
		});
	}
	
	/**
	 * This method is overridden in order to show no border at all.
	 */
	@Override
	public void setBorder( Border border ) {
		// nothing to do
	}
}
