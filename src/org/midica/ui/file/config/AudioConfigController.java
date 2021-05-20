/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.file.config;

import java.util.ArrayList;
import java.util.HashMap;

import javax.sound.sampled.AudioFormat.Encoding;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;

import org.midica.config.Config;
import org.midica.config.Dict;
import org.midica.config.Laf;
import org.midica.config.NamedInteger;
import org.midica.file.write.AudioExporter;
import org.midica.ui.widget.ConfigIcon;

/**
 * Controller for the audio config window.
 * 
 * @author Jan Trukenmüller
 */
public class AudioConfigController extends FileConfigController {
	
	private static AudioConfigController controller;
	
	/**
	 * Creates a singleton instance of the controller.
	 * 
	 * @param view  the window to be controlled
	 * @param icon  the icon that is used to open the config window
	 * @return the created controller.
	 */
	public static AudioConfigController getInstance(AudioConfigView view, ConfigIcon icon) {
		
		if (null == controller)
			controller = new AudioConfigController(view, icon);
		else
			controller.init(view, icon);
		
		return controller;
	}
	
	/**
	 * Creates a controller for the audio config window.
	 * 
	 * @param view  the window to be controlled
	 * @param icon  the icon that is used to open the config window
	 */
	private AudioConfigController(AudioConfigView view, ConfigIcon icon) {
		super(view, icon);
	}
	
	@Override
	protected void createDefaultView() {
		new AudioConfigView();
	}
	
	@Override
	protected HashMap<String, String> getDefaultConfig() {
		return Config.getDefaultAudioExportConfig();
	}
	
	@Override
	protected void initSessionConfigSpecific(boolean fromConfig) {
		AudioConfigView view = (AudioConfigView) this.view;
		initWidgetConfig( Config.AU_ENCODING,         view.cbxEncoding,       String.class,  fromConfig );
		initWidgetConfig( Config.AU_SAMPLE_SIZE_BITS, view.fldSampleSizeBits, Integer.class, fromConfig );
		initWidgetConfig( Config.AU_SAMPLE_RATE,      view.fldSampleRate,     Float.class,   fromConfig );
		initWidgetConfig( Config.AU_CHANNELS,         view.cbxChannels,       Integer.class, fromConfig );
		initWidgetConfig( Config.AU_IS_BIG_ENDIAN,    view.cbxIsBigEndian,    Boolean.class, fromConfig );
	}
	
	@Override
	protected boolean applyConfigById(String id) {
		
		// data type based checks
		boolean isOk = super.applyConfigById(id);
		
		// special check: encoding / sample rate
		if (isOk) {
			AudioConfigView view   = (AudioConfigView) this.view;
			JComponent      widget = configWidgets.get(id);
			
			if (view.fldSampleSizeBits == widget || view.cbxEncoding == widget) {
				String encoding   = sessionConfig.get(Config.AU_ENCODING);
				int    sampleSize = Integer.parseInt(sessionConfig.get(Config.AU_SAMPLE_SIZE_BITS));
				isOk = encodingMatchesSampleRate(encoding, sampleSize);
				if (isOk)
					view.fldSampleSizeBits.setBackground(Laf.COLOR_NORMAL);
				else
					view.fldSampleSizeBits.setBackground(Laf.COLOR_ERROR);
			}
		}
		
		return isOk;
	}
	
	/**
	 * Checks if the given combination of encoding and sample rate make sense.
	 * 
	 * @param encoding    audio encoding
	 * @param sampleSize  sample rate
	 * @return **true** if the check succeeds, otherwise **false**
	 */
	private boolean encodingMatchesSampleRate(String encoding, int sampleSize) {
		if (sampleSize <= 0)
			return false;
		
		if (encoding.startsWith("PCM_")) {
			
			if (sampleSize % 8 != 0)
				return false;
			
			if (encoding.equals("PCM_FLOAT"))
				if (sampleSize != 32 && sampleSize != 64)
					return false;
		}
		
		// TODO: add more checks regarding the other possible formats
		
		return true;
	}
	
	/**
	 * Returns the model for the encoding combobox.
	 * 
	 * @return the combobox model
	 */
	public static DefaultComboBoxModel<String> getComboboxModelEncoding() {
		DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
		ArrayList<Encoding> encodings = AudioExporter.getEncodings();
		for (Encoding enc : encodings) {
			model.addElement(enc.toString());
		}
		return model;
	}
	
	/**
	 * Returns the model for the channels combobox.
	 * 
	 * @return the combobox model
	 */
	public static DefaultComboBoxModel<NamedInteger> getComboboxModelChannels() {
		DefaultComboBoxModel<NamedInteger> model = new DefaultComboBoxModel<>();
		model.addElement(new NamedInteger(Dict.get(Dict.AU_MONO),   1, true));
		model.addElement(new NamedInteger(Dict.get(Dict.AU_STEREO), 2, true));
		
		return model;
	}
}
