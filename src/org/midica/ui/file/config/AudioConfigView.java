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
import org.midica.config.KeyBindingManager;
import org.midica.config.Laf;
import org.midica.config.NamedInteger;
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
		
		// init widgets
		cbxEncoding       = new JComboBox<>();
		fldSampleSizeBits = new JTextField();
		fldSampleRate     = new JTextField();
		cbxChannels       = new JComboBox<>();
		cbxIsBigEndian    = new JCheckBox();
		cbxEncoding.setModel(AudioConfigController.getComboboxModelEncoding());
		cbxChannels.setModel(AudioConfigController.getComboboxModelChannels());
		
		// create controller
		controller = AudioConfigController.getInstance(this, icon);
		
		init();
		addKeyBindings();
		pack();
		addWindowListener(controller);
	}
	
	/**
	 * Initializes the content of the window.
	 */
	private void init() {
		
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
		constrCenter.gridwidth = 2;
		area.add(cbxIsBigEndian, constrCenter);
		
		// description
		constrRight.gridy += 2;
		JLabel descIsBigEndian = new JLabel(Dict.get(Dict.AUDIO_IS_BIG_ENDIAN_D));
		area.add(descIsBigEndian, constrRight);
		
		// TODO: add info about file type
		
		return area;
	}
	
	/**
	 * Adds key bindings to the info window.
	 */
	protected void addKeyBindings() {
		
		// reset everything
		keyBindingManager = new KeyBindingManager(this, this.getRootPane());
		
		// TODO: implement key bindings for:
		// - cbxEncoding
		// - fldSampleSizeBits
		// - fldSampleRate
		// - cbxChannels
		// - cbxIsBigEndian
		
		// restore/save buttons
		addGeneralKeyBindings();
		
		// set input and action maps
		keyBindingManager.postprocess();
	}
}
