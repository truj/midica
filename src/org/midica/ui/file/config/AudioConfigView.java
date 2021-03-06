/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.file.config;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.midica.config.Dict;
import org.midica.config.Laf;
import org.midica.config.NamedInteger;
import org.midica.file.write.AudioExporter;
import org.midica.ui.widget.ConfigIcon;

/**
 * This class provides the configuration window for audio options.
 * 
 * @author Jan Trukenmüller
 */
public class AudioConfigView extends FileConfigView {
	
	private static final long serialVersionUID = 1L;
	
	// identifier property name for text documents
	public static final String DOC_ID = "doc_id";
	
	// sizes
	private static final int TEXT_FIELD_WIDTH  = 150;
	private static final int TEXT_FIELD_HEIGHT =  30;
	
	// widgets that change audio config values
	JComboBox<String>       cbxEncoding;
	JTextField              fldSampleSizeBits;
	JTextField              fldSampleRate;
	JComboBox<NamedInteger> cbxChannels;
	JCheckBox               cbxIsBigEndian;
	
	/**
	 * Creates the window for the decompile configuration.
	 * 
	 * @param owner  the file selection window
	 * @param icon   the icon to open this window
	 */
	public AudioConfigView(JDialog owner, ConfigIcon icon) {
		super(owner, icon, Dict.get(Dict.TITLE_AU_CONFIG));
	}
	
	/**
	 * Creates a default window that is not intended to be used as a real window.
	 * 
	 * See {@link FileConfigView#FileConfigView()}.
	 */
	public AudioConfigView() {
		super();
	}
	
	@Override
	protected FileConfigController initStructures() {
		
		// init widgets
		cbxEncoding       = new JComboBox<>();
		fldSampleSizeBits = new JTextField();
		fldSampleRate     = new JTextField();
		cbxChannels       = new JComboBox<>();
		cbxIsBigEndian    = new JCheckBox();
		cbxEncoding.setModel(AudioConfigController.getComboboxModelEncoding());
		cbxChannels.setModel(AudioConfigController.getComboboxModelChannels());
		
		// create controller
		return AudioConfigController.getInstance(this, icon);
	}
	
	@Override
	protected void initUi() {
		
		// create top-level container
		JPanel content = new JPanel();
		GridBagConstraints constraints = new GridBagConstraints();
		content.setLayout(new GridBagLayout());
		constraints.fill       = GridBagConstraints.BOTH;
		constraints.insets     = Laf.INSETS_NWE;
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 1;
		constraints.weighty    = 1;
		
		// create and add content
		content.add(createContent(), constraints);
		
		// separator
		constraints.gridy++;
		constraints.weighty = 0;
		constraints.insets  = Laf.INSETS_ZERO;
		content.add(Laf.createSeparator(), constraints);
		
		// create and add buttons
		constraints.gridy++;
		constraints.weightx = 0;
		constraints.insets  = Laf.INSETS_SWE;
		Container buttonArea = createButtonArea();
		content.add(buttonArea, constraints);
		
		add(content);
	}
	
	/**
	 * Creates the main content area.
	 * 
	 * @return the created area
	 */
	private Container createContent() {
		
		// layout
		JPanel area = new JPanel();
		area.setLayout(new GridBagLayout());
		GridBagConstraints[] constaints = createConstraintsForArea();
		GridBagConstraints constrFull   = constaints[0];
		GridBagConstraints constrLeft   = constaints[1];
		GridBagConstraints constrCenter = constaints[2];
		GridBagConstraints constrRight  = constaints[3];
		
		// encoding
		// label
		JLabel lblEncoding = new JLabel(Dict.get(Dict.AUDIO_ENCODING));
		Laf.makeBold(lblEncoding);
		area.add(lblEncoding, constrLeft);
		
		// combobox
		cbxEncoding.addActionListener(controller);
		area.add(cbxEncoding, constrCenter);
		
		// sample size in bits
		// label
		constrLeft.gridy++;
		JLabel lblSampleSizeBits = new JLabel(Dict.get(Dict.AUDIO_SAMPLE_SIZE_BITS));
		Laf.makeBold(lblSampleSizeBits);
		area.add(lblSampleSizeBits, constrLeft);
		
		// text field
		constrCenter.gridy++;
		fldSampleSizeBits.getDocument().addDocumentListener(controller);
		fldSampleSizeBits.setPreferredSize(new Dimension(TEXT_FIELD_WIDTH, TEXT_FIELD_HEIGHT));
		area.add(fldSampleSizeBits, constrCenter);
		
		// description
		constrRight.gridy++;
		constrFull.gridy = constrRight.gridy;
		JLabel descSampleSizeBits = new JLabel(Dict.get(Dict.AUDIO_SAMPLE_SIZE_BITS_D));
		area.add(descSampleSizeBits, constrRight);
		
		// sample rate
		// label
		constrLeft.gridy++;
		JLabel lblSampleRate = new JLabel(Dict.get(Dict.AUDIO_SAMPLE_RATE));
		Laf.makeBold(lblSampleRate);
		area.add(lblSampleRate, constrLeft);
		
		// text field
		constrCenter.gridy++;
		fldSampleRate.getDocument().addDocumentListener(controller);
		fldSampleRate.setPreferredSize(new Dimension(TEXT_FIELD_WIDTH, TEXT_FIELD_HEIGHT));
		area.add(fldSampleRate, constrCenter);
		
		// description
		constrRight.gridy++;
		constrFull.gridy = constrRight.gridy;
		JLabel descSampleRate = new JLabel(Dict.get(Dict.AUDIO_SAMPLE_RATE_D));
		area.add(descSampleRate, constrRight);
		
		// channels
		// label
		constrLeft.gridy++;
		JLabel lblChannels = new JLabel(Dict.get(Dict.AUDIO_CHANNELS));
		Laf.makeBold(lblChannels);
		area.add(lblChannels, constrLeft);
		
		// combobox
		constrCenter.gridy++;
		cbxChannels.addActionListener(controller);
		constrCenter.gridwidth = 2;
		area.add(cbxChannels, constrCenter);
		
		// is big endian
		// label
		constrLeft.gridy++;
		JLabel lblEndian = new JLabel(Dict.get(Dict.AUDIO_IS_BIG_ENDIAN));
		Laf.makeBold(lblEndian);
		area.add(lblEndian, constrLeft);
		
		// checkbox
		constrCenter.gridy++;
		cbxIsBigEndian.addActionListener(controller);
		area.add(cbxIsBigEndian, constrCenter);
		
		// description
		constrRight.gridy += 2;
		JLabel descIsBigEndian = new JLabel(Dict.get(Dict.AUDIO_IS_BIG_ENDIAN_D));
		area.add(descIsBigEndian, constrRight);
		
		// separator
		constrLeft.gridy++;
		constrFull.gridy = constrLeft.gridy;
		area.add(Laf.createSeparator(), constrFull);
		
		// file type
		// label
		constrLeft.gridy++;
		constrLeft.anchor  = GridBagConstraints.NORTHWEST;
		JLabel lblFileType = new JLabel(Dict.get(Dict.AUDIO_FILE_TYPE));
		Laf.makeBold(lblFileType);
		area.add(lblFileType, constrLeft);
		
		// create description string
		String fileTypeDescStr = "<html>" + Dict.get(Dict.AUDIO_FILE_TYPE_D);
		fileTypeDescStr += "<br><ul>";
		for (String type : AudioExporter.getSupportedFileTypes()) {
			fileTypeDescStr += "<li>" + type + "</li>";
		}
		fileTypeDescStr += "</ul>";
		
		// description
		constrCenter.gridy = constrLeft.gridy;
		constrCenter.gridwidth = 2;
		JLabel descFileTypeDesc = new JLabel(fileTypeDescStr);
		area.add(descFileTypeDesc, constrCenter);
		
		return area;
	}
	
	@Override
	protected void addSpecificKeyBindings() {
		keyBindingManager.addBindingsForComboboxOpen( cbxEncoding,       Dict.KEY_AU_CONF_ENCODING             );
		keyBindingManager.addBindingsForFocus(        fldSampleSizeBits, Dict.KEY_AU_CONF_FLD_SAMPLE_SIZE_BITS );
		keyBindingManager.addBindingsForFocus(        fldSampleRate,     Dict.KEY_AU_CONF_FLD_SAMPLE_RATE      );
		keyBindingManager.addBindingsForComboboxOpen( cbxChannels,       Dict.KEY_AU_CONF_CHANNELS             );
		keyBindingManager.addBindingsForCheckbox(     cbxIsBigEndian,    Dict.KEY_AU_CONF_IS_BIG_ENDIAN        );
	}
}
