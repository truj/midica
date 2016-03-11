/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.player;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyEventPostProcessor;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.midica.config.Config;
import org.midica.config.Dict;
import org.midica.file.SequenceParser;
import org.midica.ui.SliderHelper;
import org.midica.ui.UiView;
import org.midica.ui.widget.MidicaTable;


/**
 * This class provides the player window.
 * 
 * It uses the {@link PlayerController} as {@link EventListener} for all interactions.
 * 
 * @author Jan Trukenmüller
 */
public class PlayerView extends JDialog {
	
	private static final long serialVersionUID = 1L;
	
	// Constants for sliders
	// progress slider
	public static final String    NAME_PROGRESS     = "name_progress";
	public static final int       PROGRESS_SCROLL   =  100;
	public static final Dimension MIN_PROGRESS_DIM  = new Dimension( 700, 42 );
	// tempo slider
	public static final int    TEMPO_DEFAULT     =  300;
	public static final int    TEMPO_MIN         =    0;
	public static final int    TEMPO_MAX         = 1000;
	public static final int    TEMPO_LABEL       =  150;
	public static final int    TEMPO_MAJOR       =   75;
	public static final int    TEMPO_MINOR       =   15;
	public static final int    TEMPO_FRACT       =    2; // number of digits after the decimal point
	public static final int    TEMPO_SCROLL      =    2; // scroll factor
	// volume slider
	public static final int    VOL_MIN           =    0;
	public static final int    VOL_MAX           =  127;
	public static final int    VOL_LABEL         =   10;
	public static final int    VOL_MAJOR         =    5;
	public static final int    VOL_MINOR         =    1;
	public static final int    VOL_SCROLL        =    1; // scroll factor
	// transpose slider
	public static final int    TRANSPOSE_DEFAULT =    0;
	public static final int    TRANSPOSE_MIN     =  -30;
	public static final int    TRANSPOSE_MAX     =   30;
	public static final int    TRANSPOSE_LABEL   =    5;
	public static final int    TRANSPOSE_MAJOR   =    5;
	public static final int    TRANSPOSE_MINOR   =    1;
	public static final int    TRANSPOSE_SCROLL  =    1; // scroll factor
	// channel volume slider
	public static final int    CH_VOL_DEFAULT    =     0;
	public static final int    CH_VOL_MIN        =  -130; // for the display - has to be different from CH_VOL_MIN_VAL
	public static final int    CH_VOL_MAX        =   130; // in order to have a major tick at '0'
	public static final int    CH_VOL_MIN_VAL    =  -127;
	public static final int    CH_VOL_MAX_VAL    =   127;
	public static final int    CH_VOL_SKIP       =    50;
	public static final int    CH_VOL_MAJOR      =    10;
	public static final int    CH_VOL_MINOR      =     5;
	public static final int    CH_VOL_SCROLL     =     1; // scroll factor for the channel volume slider
	
	// Constants for text fields
	public static final String NAME_JUMP      = "name_jump";
	public static final String NAME_VOL       = "name_volume";
	public static final String NAME_TEMPO     = "name_tempo";
	public static final String NAME_TRANSPOSE = "name_transpose";
	public static final String NAME_CH_VOL    = "name_channel_volume_";
	public static final String NAME_MUTE      = "name_mute_";
	public static final String NAME_SOLO      = "name_solo_";
	
	// action commands
	public static final String CMD_REPARSE    = "cmd_reparse";
	public static final String CMD_SOUNDCHECK = "cmd_soundcheck";
	public static final String CMD_INFO       = "cmd_info";
	public static final String CMD_JUMP       = "cmd_jump";
	public static final String CMD_MEMORIZE   = "cmd_memorize";
	public static final String CMD_SHOW_HIDE  = "cmd_show_hide_";
	
	// control button commands
	public static final String CMD_STOP      = "cmd_stop";
	public static final String CMD_PLAY      = "cmd_play";
	public static final String CMD_PAUSE     = "cmd_pause";
	public static final String CMD_REW       = "cmd_rewind";
	public static final String CMD_FAST_REW  = "cmd_fast_rewind";
	public static final String CMD_FWD       = "cmd_forward";
	public static final String CMD_FAST_FWD  = "cmd_fast_forward";
	
	// text colors
	public static final Color COLOR_TICKS             = new Color(  50, 100, 255 );
	public static final Color COLOR_TIME              = new Color(   0,   0,   0 );
	public static final Color COLOR_CH_PROGRAM_NUMBER = new Color(   0,   0,   0 );
	public static final Color COLOR_CH_BANK_NUMBER    = new Color(  50, 100, 255 );
	public static final Color COLOR_CH_INSTRUMENT     = new Color(   0,   0,   0 );
	public static final Color COLOR_CH_COMMENT        = new Color(  50, 100, 255 );
	
	// for activity control
	public static final ImageIcon AC_ICON_INACTIVE  = new ImageIcon( ClassLoader.getSystemResource("org/midica/resources/inactive.png") );
	public static final ImageIcon AC_ICON_ACTIVE    = new ImageIcon( ClassLoader.getSystemResource("org/midica/resources/active.png") );
	
	// for channel details
	public static       int   NOTE_HISTORY_WIDTH            =   0; // will be set later
	public static final int   NOTE_HISTORY_HEIGHT           = 150;
	public static final int   NOTE_HISTORY_COL_WIDTH_NUMBER =  40;
	public static final int   NOTE_HISTORY_COL_WIDTH_NAME   = 100;
	public static final int   NOTE_HISTORY_COL_WIDTH_VOLUME =  40;
	public static final int   NOTE_HISTORY_COL_WIDTH_TICK   =  70;
	
	private Container             content      = null;
	private PlayerController      controller   = null;
	private KeyEventPostProcessor keyProcessor = null;
	
	// UI
	private JSlider         progressSlider  = null;
	private JSlider         volumeSlider    = null;
	private JSlider         tempoSlider     = null;
	private JSlider         transposeSlider = null;
	
	private JButton         btnPlayPause    = null;
	private JButton         btnReparse      = null;
	private JButton         btnSoundcheck   = null;
	private JButton         btnInfo         = null;
	private JButton         btnForw         = null;
	private JButton         btnFastForw     = null;
	private JButton         btnRew          = null;
	private JButton         btnFastRew      = null;
	private JButton         btnStop         = null;
	private JButton         btnMemorize     = null;
	private JButton         btnJump         = null;
	
	private JTextField      fldJump         = null;
	private JTextField      fldTempo        = null;
	private JTextField      fldTranspose    = null;
	private JTextField      fldVol          = null;
	private JLabel          lblCurrentTicks = null;
	private JLabel          lblCurrentTime  = null;
	private JLabel          lblTotalTicks   = null;
	private JLabel          lblTotalTime    = null;
	
	private ArrayList<JCheckBox>  muteCbx               = null;
	private ArrayList<JCheckBox>  soloCbx               = null;
	private ArrayList<Container>  channelDetails        = null;
	private ArrayList<JLabel>     channelActivityLEDs   = null;
	private ArrayList<JLabel>     channelInstruments    = null;
	private ArrayList<JLabel>     channelProgramNumbers = null;
	private ArrayList<JLabel>     channelBankNumbers    = null;
	private ArrayList<JLabel>     channelComments       = null;
	private ArrayList<JSlider>    channelVolumeSliders  = null;
	private ArrayList<JTextField> channelVolumeFields   = null;
	
	/**
	 * Creates the player window.
	 * Fills the content via {@link #init()}.
	 * 
	 * @param view         Main window object.
	 * @param parser       Parser object that can be used for reparsing the current file.
	 * @param currentFile  Currently loaded and parsed file.
	 */
	public PlayerView( UiView view, SequenceParser parser, File currentFile ) {
		super( view, Dict.get(Dict.TITLE_PLAYER), true );
		
		if ( null != currentFile ) {
			setTitle( Dict.get(Dict.TITLE_PLAYER) + " - " + currentFile.getName() );
		}
		
		controller = new PlayerController( this, parser, currentFile );
		
		// note history table width
		NOTE_HISTORY_WIDTH = NOTE_HISTORY_COL_WIDTH_NAME + NOTE_HISTORY_COL_WIDTH_NUMBER
		                   + NOTE_HISTORY_COL_WIDTH_TICK + NOTE_HISTORY_COL_WIDTH_VOLUME;
		
		// add listeners
		addWindowListener( controller );
		
		init();
		pack();
		setVisible( true );
	}
	
	/**
	 * Composes the content of the player window.
	 * Creates the different areas by calling the appropriate methods.
	 */
	private void init() {
		// content
		content = getContentPane();
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		content.setLayout( layout );
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill   = GridBagConstraints.BOTH;
		constraints.insets = new Insets( 2, 2, 2, 2 );
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 2;
		constraints.weightx    = 1;
		constraints.weighty    = 0;
		
		// jump area
		content.add( createJumpArea(), constraints );
		
		// slider
		constraints.gridy++;
		content.add( createProgressSlider(), constraints );
		
		// control area
		constraints.gridy++;
		constraints.gridwidth = 1;
		content.add( createControlArea(), constraints );
		
		// channels
		constraints.gridy++;
		constraints.gridx   = 0;
		constraints.weighty = 1;
		content.add( createChannelArea(), constraints );
		
		// volume, tempo and transpose sliders
		constraints.gridheight = 2;
		constraints.weightx    = 0;
		constraints.gridx++;
		constraints.gridy--;
		content.add( createGlobalSliderAndButtonsArea(), constraints );
	}
	
	/**
	 * Creates the top area containing the buttons for memorize and jump and the time display.
	 * 
	 * @return The created area.
	 */
	private Container createJumpArea() {
		Container area = new Container();
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout( layout );
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill   = GridBagConstraints.BOTH;
		constraints.insets = new Insets( 2, 2, 2, 2 );
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 0;
		
		// memorize button
		btnMemorize = new JButton( Dict.get(Dict.MEMORIZE) );
		btnMemorize.addActionListener( controller );
		btnMemorize.setActionCommand( CMD_MEMORIZE );
		area.add( btnMemorize, constraints );
		
		// text field
		constraints.gridx++;
		fldJump = new JTextField();
		fldJump.setName( NAME_JUMP );
		fldJump.getDocument().putProperty( "name", NAME_JUMP );
		fldJump.getDocument().addDocumentListener( controller );
		fldJump.addActionListener( controller );
		setTextFieldColor( fldJump.getName(), Config.COLOR_NORMAL );
		Dimension minField = new Dimension( 70, 26 );
		fldJump.setPreferredSize( minField );
		area.add( fldJump, constraints );
		
		// jump button
		constraints.gridx++;
		btnJump = new JButton( Dict.get(Dict.JUMP) );
		btnJump.addActionListener( controller );
		btnJump.setActionCommand( CMD_JUMP );
		area.add( btnJump, constraints );
		
		// spacer
		constraints.gridx++;
		constraints.weightx = 1;
		JLabel spacer = new JLabel( " " );
		area.add( spacer, constraints );
		
		// time and tick area
		constraints.gridx++;
		constraints.weightx = 0;
		area.add( createTimeArea(), constraints );
		
		return area;
	}
	
	/**
	 * Creates the top right area containing the current and total ticks and time.
	 * 
	 * @return The created area.
	 */
	private Container createTimeArea() {
		Container area = new Container();
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout( layout );
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.BOTH;
		constraints.insets     = new Insets( -1, 2, -1, 2 );
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 0;
		constraints.weighty    = 0;
		
		// current ticks
		lblCurrentTicks = new JLabel( "0" );
		lblCurrentTicks.setForeground( COLOR_TICKS );
		lblCurrentTicks.setHorizontalAlignment( SwingConstants.RIGHT );
		area.add( lblCurrentTicks, constraints );
		
		// /
		constraints.gridx++;
		JLabel slash = new JLabel("/");
		slash.setForeground( COLOR_TICKS );
		area.add( slash, constraints );
		
		// total ticks
		constraints.gridx++;
		lblTotalTicks = new JLabel( "0" );
		lblTotalTicks.setForeground( COLOR_TICKS );
		lblTotalTicks.setHorizontalAlignment( SwingConstants.RIGHT );
		area.add( lblTotalTicks, constraints );
		
		// current time
		constraints.gridy++;
		constraints.gridx = 0;
		lblCurrentTime = new JLabel( "00:00:00" );
		lblCurrentTime.setForeground( COLOR_TIME );
		lblCurrentTime.setHorizontalAlignment( SwingConstants.RIGHT );
		area.add( lblCurrentTime, constraints );
		
		// /
		constraints.gridx++;
		JLabel slash2 = new JLabel("/");
		slash2.setForeground( COLOR_TIME );
		area.add( slash2, constraints );
		
		// total time
		constraints.gridx++;
		lblTotalTime = new JLabel( "00:00:00" );
		lblTotalTime.setForeground( COLOR_TIME );
		lblTotalTime.setHorizontalAlignment( SwingConstants.RIGHT );
		area.add( lblTotalTime, constraints );
		
		return area;
	}
	
	/**
	 * Creates the right area containing the sliders for volume, tempo and transpose level
	 * and the buttons below the sliders.
	 * 
	 * @return The created area.
	 */
	private Container createGlobalSliderAndButtonsArea() {
		Container area = new Container();
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout( layout );
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill   = GridBagConstraints.BOTH;
		constraints.insets = new Insets( 2, 2, 2, 2 );
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 2;
		constraints.weightx    = 0;
		constraints.weighty    = 1;
		
		// global sliders
		area.add( createGlobalSliderArea(), constraints );
		
		// reparse button
		constraints.gridy ++;
		constraints.weighty   = 0;
		constraints.gridwidth = 1;
		btnReparse = new JButton( Dict.get(Dict.REPARSE) );
		btnReparse.setActionCommand( CMD_REPARSE );
		btnReparse.addActionListener( controller );
		area.add( btnReparse, constraints );
		
		// soundcheck button
		constraints.gridx ++;
		btnSoundcheck = new JButton( Dict.get(Dict.SOUNDCHECK) );
		btnSoundcheck.setActionCommand( CMD_SOUNDCHECK );
		btnSoundcheck.addActionListener( controller );
		area.add( btnSoundcheck, constraints );
		
		// info button
		constraints.gridx = 0;
		constraints.gridy++;
		constraints.gridwidth = 2;
		btnInfo = new JButton( Dict.get(Dict.SHOW_INFO_FROM_PLAYER) );
		btnInfo.setActionCommand( CMD_INFO );
		btnInfo.addActionListener( controller );
		area.add( btnInfo, constraints );
		
		return area;
	}
	
	/**
	 * Creates the area with the sliders for volume, tempo and the transpose level.
	 * 
	 * @return The created area.
	 */
	private Container createGlobalSliderArea() {
		JPanel area = new JPanel();
		area.setBorder( BorderFactory.createEtchedBorder() );
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout( layout );
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill   = GridBagConstraints.BOTH;
		constraints.insets = new Insets( 2, 2, 2, 2 );
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 0;
		constraints.weighty    = 0;
		
		// volume label
		JLabel lblVol = new JLabel( Dict.get(Dict.SLIDER_VOL) );
		area.add( lblVol, constraints );
		
		// tempo label
		constraints.gridx++;
		JLabel lblTempo = new JLabel( Dict.get(Dict.SLIDER_TEMPO) );
		area.add( lblTempo, constraints );
		
		// transpose label
		constraints.gridx++;
		JLabel lblTrans = new JLabel( Dict.get(Dict.SLIDER_TRANSPOSE) );
		area.add( lblTrans, constraints );
		
		// volume slider
		constraints.gridy++;
		constraints.gridx   = 0;
		constraints.weighty = 1;
		area.add( createVolumeSlider(), constraints );
		
		// tempo slider
		constraints.gridx++;
		area.add( createTempoSlider(), constraints );
		
		// tempo slider
		constraints.gridx++;
		area.add( createTransposeSlider(), constraints );
		
		// volume input field
		constraints.gridy++;
		constraints.gridx   = 0;
		constraints.weighty = 0;
		fldVol = new JTextField();
		fldVol.setName( NAME_VOL );
		fldVol.getDocument().putProperty( "name", NAME_VOL );
		fldVol.getDocument().addDocumentListener( controller );
		fldVol.addActionListener( controller );
		setTextFieldColor( fldVol.getName(), Config.COLOR_NORMAL );
		area.add( fldVol, constraints );
		
		// tempo input field
		constraints.gridx++;
		fldTempo = new JTextField();
		fldTempo.setName( NAME_TEMPO );
		fldTempo.getDocument().putProperty( "name", NAME_TEMPO );
		fldTempo.getDocument().addDocumentListener( controller );
		fldTempo.addActionListener( controller );
		setTextFieldColor( fldTempo.getName(), Config.COLOR_NORMAL );
		area.add( fldTempo, constraints );
		
		// transpose input field
		constraints.gridx++;
		fldTranspose = new JTextField();
		fldTranspose.setName( NAME_TRANSPOSE );
		fldTranspose.getDocument().putProperty( "name", NAME_TRANSPOSE );
		fldTranspose.getDocument().addDocumentListener( controller );
		fldTranspose.addActionListener( controller );
		setTextFieldColor( fldTranspose.getName(), Config.COLOR_NORMAL );
		area.add( fldTranspose, constraints );
		
		return area;
	}
	
	/**
	 * Creates the process slider for the current MIDI stream.
	 * 
	 * @return The created process slider.
	 */
	private JSlider createProgressSlider() {
		progressSlider = new JSlider( JSlider.HORIZONTAL );
		progressSlider.setName( NAME_PROGRESS );
		progressSlider.addChangeListener( controller );
		progressSlider.addMouseWheelListener( controller );
		progressSlider.setUI( SliderHelper.createSliderUi() );
		progressSlider.setMinimum( 0 );
		progressSlider.setValue( 0 );
		progressSlider.setPaintTicks( true );
		progressSlider.setPaintLabels( true );
		progressSlider.setPaintTrack( true );
		// set minimum size
		
		progressSlider.setPreferredSize( MIN_PROGRESS_DIM );
		return progressSlider;
	}
	
	/**
	 * Creates the global volume slider at the right side.
	 * 
	 * @return The created volume slider.
	 */
	private JSlider createVolumeSlider() {
		volumeSlider = new JSlider( JSlider.VERTICAL );
		volumeSlider.setName( NAME_VOL );
		volumeSlider.addChangeListener( controller );
		volumeSlider.addMouseWheelListener( controller );
		volumeSlider.setUI( SliderHelper.createSliderUi() );
		volumeSlider.setValue( 0 );
		volumeSlider.setPaintTicks( true );
		volumeSlider.setPaintLabels( true );
		volumeSlider.setPaintTrack( true );
		// labels
		volumeSlider.setMinimum( VOL_MIN );
		volumeSlider.setMaximum( VOL_MAX );
		volumeSlider.setMajorTickSpacing( VOL_MAJOR );
		volumeSlider.setMinorTickSpacing( VOL_MINOR );
		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		for ( int i = VOL_MIN; i <= VOL_MAX; i += VOL_LABEL ) {
			int display = i;
			labelTable.put( i, new JLabel(Integer.toString(display)) );
		}
		volumeSlider.setLabelTable( labelTable );
		return volumeSlider;
	}
	
	/**
	 * Creates a volume slider for the specified channel.
	 * 
	 * @param channel The MIDI channel.
	 * @return The created volume slider.
	 */
	private JSlider createChannelVolumeSlider( byte channel ) {
		JSlider volSlider = new JSlider( JSlider.HORIZONTAL ) {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void setValue( int value ) {
				if ( value < CH_VOL_MIN_VAL )
					super.setValue( CH_VOL_MIN_VAL );
				else if ( value > CH_VOL_MAX_VAL )
					super.setValue( CH_VOL_MAX_VAL );
				else
					super.setValue( value );
			}
		};
		volSlider.setName( NAME_CH_VOL + channel );
		volSlider.addChangeListener( controller );
		volSlider.addMouseWheelListener( controller );
		volSlider.setUI( SliderHelper.createSliderUi() );
		volSlider.setValue( 0 );
		volSlider.setPaintTicks( true );
		volSlider.setPaintLabels( true );
		volSlider.setPaintTrack( true );
		// labels
		volSlider.setMinimum( CH_VOL_MIN );
		volSlider.setMaximum( CH_VOL_MAX );
		volSlider.setMajorTickSpacing( CH_VOL_MAJOR );
		volSlider.setMinorTickSpacing( CH_VOL_MINOR );
		
		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		for ( int i = 0; i <= CH_VOL_MAX; i += CH_VOL_SKIP ) {
			int display = i;
			String text = Integer.toString( display );
			if ( 0 == i)
				labelTable.put( i, new JLabel(text) );
			else {
				labelTable.put( -i, new JLabel("-" + text) );
				labelTable.put(  i, new JLabel("+" + text) );
			}
		}
		volSlider.setLabelTable( labelTable );
		return volSlider;
	}
	
	/**
	 * Creates the tempo slider at the right side.
	 * 
	 * @return The created tempo slider.
	 */
	private JSlider createTempoSlider() {
		tempoSlider = new JSlider( JSlider.VERTICAL );
		tempoSlider.setName( NAME_TEMPO );
		tempoSlider.addChangeListener( controller );
		tempoSlider.addMouseWheelListener( controller );
		tempoSlider.setUI( SliderHelper.createSliderUi() );
		tempoSlider.setValue( 0 );
		tempoSlider.setPaintTicks( true );
		tempoSlider.setPaintLabels( true );
		tempoSlider.setPaintTrack( true );
		// labels
		tempoSlider.setMinimum( TEMPO_MIN );
		tempoSlider.setMaximum( TEMPO_MAX );
		tempoSlider.setMajorTickSpacing( TEMPO_MAJOR );
		tempoSlider.setMinorTickSpacing( TEMPO_MINOR );
		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		for ( int i = TEMPO_MIN; i <= TEMPO_MAX; i += TEMPO_LABEL ) {
			float display = (float) i / TEMPO_DEFAULT;
			labelTable.put( i, new JLabel(Float.toString(display)) );
		}
		tempoSlider.setLabelTable( labelTable );
		return tempoSlider;
	}
	
	/**
	 * Creates the transpose slider at the right side.
	 * 
	 * @return The created transpose slider.
	 */
	private JSlider createTransposeSlider() {
		transposeSlider = new JSlider( JSlider.VERTICAL );
		transposeSlider.setName( NAME_TRANSPOSE );
		transposeSlider.addChangeListener( controller );
		transposeSlider.addMouseWheelListener( controller );
		transposeSlider.setUI( SliderHelper.createSliderUi() );
		transposeSlider.setValue( 0 );
		transposeSlider.setPaintTicks( true );
		transposeSlider.setPaintLabels( true );
		transposeSlider.setPaintTrack( true );
		// labels
		transposeSlider.setMinimum( TRANSPOSE_MIN );
		transposeSlider.setMaximum( TRANSPOSE_MAX );
		transposeSlider.setMajorTickSpacing( TRANSPOSE_MAJOR );
		transposeSlider.setMinorTickSpacing( TRANSPOSE_MINOR );
		return transposeSlider;
	}
	
	/**
	 * Creates the area containing the control buttons like play/pause, stop, rewind,
	 * forward etc.
	 * 
	 * @return The created control area.
	 */
	private Container createControlArea() {
		Container area = new Container();
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout( layout );
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill   = GridBagConstraints.BOTH;
		constraints.insets = new Insets( 2, 2, 2, 2 );
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 0;
		
		// stop button
		btnStop = new JButton( Dict.get(Dict.CTRL_BTN_STOP) );
		btnStop.addActionListener( controller );
		btnStop.setActionCommand( CMD_STOP );
		area.add( btnStop, constraints );
		
		// <<
		constraints.gridx++;
		btnFastRew = new JButton( Dict.get(Dict.CTRL_BTN_FAST_REW) );
		btnFastRew.setActionCommand( CMD_FAST_REW );
		btnFastRew.addActionListener( controller );
		area.add( btnFastRew, constraints );
		
		// <
		constraints.gridx++;
		btnRew = new JButton( Dict.get(Dict.CTRL_BTN_REW) );
		btnRew.setActionCommand( CMD_REW );
		btnRew.addActionListener( controller );
		area.add( btnRew, constraints );
		
		// play / pause
		constraints.gridx++;
		constraints.weightx = 1;
		btnPlayPause = new JButton( Dict.get(Dict.CTRL_BTN_PLAY) );
		btnPlayPause.setActionCommand( CMD_PLAY );
		btnPlayPause.addActionListener( controller );
		area.add( btnPlayPause, constraints );
		
		// >
		constraints.gridx++;
		constraints.weightx = 0;
		btnForw = new JButton( Dict.get(Dict.CTRL_BTN_FWD) );
		btnForw.setActionCommand( CMD_FWD );
		btnForw.addActionListener( controller );
		area.add( btnForw, constraints );
		
		// >>
		constraints.gridx++;
		btnFastForw = new JButton( Dict.get(Dict.CTRL_BTN_FAST_FWD) );
		btnFastForw.setActionCommand( CMD_FAST_FWD );
		btnFastForw.addActionListener( controller );
		area.add( btnFastForw, constraints );
		
		return area;
	}
	
	/**
	 * Creates the channel area at the left side containing the channel widgets.
	 * 
	 * @return The created area.
	 */
	private Container createChannelArea() {
		muteCbx               = new ArrayList<JCheckBox>();
		soloCbx               = new ArrayList<JCheckBox>();
		channelActivityLEDs   = new ArrayList<JLabel>();
		channelInstruments    = new ArrayList<JLabel>();
		channelProgramNumbers = new ArrayList<JLabel>();
		channelBankNumbers    = new ArrayList<JLabel>();
		channelComments       = new ArrayList<JLabel>();
		channelDetails        = new ArrayList<Container>();
		channelVolumeSliders  = new ArrayList<JSlider>();
		channelVolumeFields   = new ArrayList<JTextField>();
		
		Container area = new Container();
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout( layout );
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.BOTH;
		constraints.insets     = new Insets( 2, 2, 2, 2 );
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 0;
		
		// headlines
		JLabel c = new JLabel( Dict.get(Dict.ABBR_CH_NUM) );
		c.setToolTipText( Dict.get(Dict.TIP_CH_NUM) );
		area.add( c, constraints );
		
		constraints.gridx++;
		JLabel m = new JLabel( Dict.get(Dict.ABBR_MUTE) );
		m.setToolTipText( Dict.get(Dict.TIP_MUTE) );
		area.add( m, constraints );
		
		constraints.gridx++;
		JLabel s = new JLabel( Dict.get(Dict.ABBR_SOLO) );
		s.setToolTipText( Dict.get(Dict.TIP_SOLO) );
		area.add( s, constraints );
		
		constraints.gridx++;
		JLabel a = new JLabel( Dict.get(Dict.ABBR_ACTIVITY) );
		a.setToolTipText( Dict.get(Dict.TIP_ACTIVITY) );
		area.add( a, constraints );
		
		constraints.gridx++;
		JLabel p = new JLabel( Dict.get(Dict.ABBR_PROG_NUM) );
		p.setToolTipText( Dict.get(Dict.TIP_PROG_NUM) );
		area.add( p, constraints );
		
		constraints.gridx++;
		JLabel b = new JLabel( Dict.get(Dict.ABBR_BANK_NUM) );
		b.setToolTipText( Dict.get(Dict.TIP_BANK_NUM) );
		area.add( b, constraints );
		
		constraints.gridx++;
		JLabel in = new JLabel( Dict.get(Dict.CH_HEAD_INSTRUMENT) );
		area.add( in, constraints );
		
		constraints.weightx = 1;
		constraints.gridx++;
		JLabel com = new JLabel( Dict.get(Dict.CH_HEAD_COMMENT) );
		area.add( com, constraints );
		
		constraints.weightx = 0;
		constraints.gridy++;
		
		// size for the channel activity labels
		Dimension activityDimension = new Dimension( 15, 15 );
		
		// size and insets for the button to show/hide channel details
		Insets showHideInsets = new Insets( 0, 0, 0, 0 );
		
		// size for the channel detail volume text field
		int defaultHeight = ( new JTextField() ).getHeight();
		Dimension dimVolTxtFld = new Dimension( 40, defaultHeight );
		
		for ( byte i=0; i<16; i++ ) {
			// number label
			constraints.gridx     = 0;
			constraints.gridwidth = 1;
			JButton showHideButton = new JButton( Integer.toString(i) );
			showHideButton.setActionCommand( CMD_SHOW_HIDE + i );
			showHideButton.addActionListener( controller );
			showHideButton.setMargin( showHideInsets );
			area.add( showHideButton, constraints );
			
			// mute checkbox
			constraints.gridx++;
			JCheckBox cbx = new JCheckBox();
			cbx.setName( NAME_MUTE + i );
			cbx.addChangeListener( controller );
			area.add( cbx, constraints );
			muteCbx.add( cbx );
			
			// solo checkbox
			constraints.gridx++;
			JCheckBox sCbx = new JCheckBox();
			sCbx.setName( NAME_SOLO + i );
			sCbx.addChangeListener( controller );
			area.add( sCbx, constraints );
			soloCbx.add( sCbx );
			
			// channel activity LED
			constraints.gridx++;
			JLabel lblAct = new JLabel();
			lblAct.setSize( activityDimension );
			lblAct.setPreferredSize( activityDimension );
			channelActivityLEDs.add( lblAct );
			setActivityLED( i, false );
			area.add( lblAct, constraints );
			
			// program number
			constraints.gridx++;
			JLabel lblProgNum = new JLabel();
			lblProgNum.setForeground( COLOR_CH_PROGRAM_NUMBER );
			area.add( lblProgNum, constraints );
			channelProgramNumbers.add( lblProgNum );
			
			// bank number
			constraints.gridx++;
			JLabel lblBankNum = new JLabel();
			lblBankNum.setForeground( COLOR_CH_BANK_NUMBER );
			area.add( lblBankNum, constraints );
			channelBankNumbers.add( lblBankNum );
			
			// instrument label
			constraints.weightx = 1;
			constraints.gridx++;
			JLabel lblInstr = new JLabel();
			lblInstr.setForeground( COLOR_CH_INSTRUMENT );
			area.add( lblInstr, constraints );
			channelInstruments.add( lblInstr );
			
			// comment label
			constraints.gridx++;
			JLabel lblDesc = new JLabel();
			lblDesc.setForeground( COLOR_CH_COMMENT );
			area.add( lblDesc, constraints );
			channelComments.add( lblDesc );
			
			// channel detail area
			constraints.gridy++;
			constraints.gridx     = 0;
			constraints.gridwidth = 8;
			constraints.weighty   = 1;
			Container details = createChannelDetailArea( i, dimVolTxtFld );
			area.add( details, constraints );
			details.setVisible( false );
			channelDetails.add( details );
			
			constraints.weightx = 0;
			constraints.weighty = 0;
			constraints.gridy++;
		}
		
		return area;
	}
	
	/**
	 * Creates the channel details area for the given channel that is hidden by default.
	 * It contains channel volume control widgets and the channel based note history table.
	 * 
	 * @param channel             MIDI channel number.
	 * @param textFieldDimension  Dimension object for the volume text field.
	 * @return The created area.
	 */
	private Container createChannelDetailArea( byte channel, Dimension textFieldDimension ) {
		JPanel area = new JPanel();
		area.setBorder( BorderFactory.createEtchedBorder() );
		
		// layout
		GridBagLayout layout = new GridBagLayout();
		area.setLayout( layout );
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill       = GridBagConstraints.BOTH;
		constraints.insets     = new Insets( 2, 2, 2, 2 );
		constraints.gridx      = 0;
		constraints.gridy      = 0;
		constraints.gridheight = 1;
		constraints.gridwidth  = 1;
		constraints.weightx    = 0;
		constraints.weighty    = 0;
		
		// volume label
		JLabel lblVol = new JLabel( Dict.get(Dict.CH_DETAILS_VOLUME) );
		area.add( lblVol, constraints );
		
		// volume text field
		constraints.gridx++;
		JTextField fldChVol = new JTextField( "0" );
		fldChVol.setPreferredSize( textFieldDimension );
		fldChVol.setName( NAME_CH_VOL + channel );
		fldChVol.getDocument().putProperty( "name", NAME_CH_VOL + channel );
		fldChVol.getDocument().addDocumentListener( controller );
		fldChVol.addActionListener( controller );
		area.add( fldChVol, constraints );
		channelVolumeFields.add( fldChVol );
		setTextFieldColor( fldChVol.getName(), Config.COLOR_NORMAL );
		
		// spacer
		constraints.gridx++;
		constraints.gridwidth = 2;
		constraints.weightx   = 1;
		area.add( new JLabel(""), constraints );
		
		// slider
		constraints.gridy++;
		constraints.gridx     = 1;
		constraints.gridwidth = 3;
		JSlider volSlider = createChannelVolumeSlider( channel );
		area.add( volSlider, constraints );
		channelVolumeSliders.add( volSlider );
		
		// note history label
		constraints.gridy++;
		constraints.gridx   = 0;
		constraints.weightx = 0;
		JLabel lblNotes = new JLabel( Dict.get(Dict.LBL_NOTE_HISTORY) );
		area.add( lblNotes, constraints );
		
		// note table
		constraints.gridx++;
		constraints.gridwidth = 3;
		constraints.weightx   = 1;
		constraints.weighty   = 1;
		Component table       = createNoteHistoryTable( channel );
		area.add( table, constraints );
		
		return area;
	}
	
	/**
	 * Creates the note history table for the given channel.
	 * 
	 * @param channel MIDI channel number.
	 * @return The created table.
	 */
	private Component createNoteHistoryTable( byte channel ) {
		
		// table model and renderer
		NoteHistoryTableModel        model    = new NoteHistoryTableModel( channel );
		NoteHistoryTableCellRenderer renderer = new NoteHistoryTableCellRenderer( model );
		
		// table
		MidicaTable table = new MidicaTable( model );
		table.setDefaultRenderer( Object.class, renderer );
		table.getColumnModel().getColumn( 0 ).setPreferredWidth( NOTE_HISTORY_COL_WIDTH_NUMBER );
		table.getColumnModel().getColumn( 1 ).setPreferredWidth( NOTE_HISTORY_COL_WIDTH_NAME   );
		table.getColumnModel().getColumn( 2 ).setPreferredWidth( NOTE_HISTORY_COL_WIDTH_VOLUME );
		table.getColumnModel().getColumn( 3 ).setPreferredWidth( NOTE_HISTORY_COL_WIDTH_TICK   );
		JScrollPane pane = new JScrollPane( table );
		
		// size
		Dimension dim = new Dimension( NOTE_HISTORY_WIDTH, NOTE_HISTORY_HEIGHT );
		pane.setPreferredSize( dim );
		
		return pane;
	}
	
	/**
	 * Calculates and sets the major and minor ticks to be displayed in the progress slider.
	 * This is based on the maximum ticks.
	 */
	public void initProgressSlider() {
		// set labels
		int max = progressSlider.getMaximum(); // total ticks
		
		// define where to use which distance between ticks
		String unit;
		int divisor;
		float factor = 1;
		if ( max < 500 ) {
			unit    = "";
			divisor = 10;
			factor  = 10;
		}
		else if ( max < 5000 ) {
			unit    = "";
			divisor = 100;
			factor  = 100;
		}
		else if ( max < 50000 ) {
			divisor = 1000;
			unit    = " k";
			factor  = 1;
		}
		else if ( max < 500000 ) {
			divisor = 10000;
			unit    = " k";
			factor  = 10;
		}
		else if ( max < 5000000 ) {
			divisor = 100000;
			unit    = " k";
			factor  = 100;
		}
		else if ( max < 50000000 ) {
			divisor = 1000000;
			unit    = " M";
			factor  = 1;
		}
		else if ( max < 500000000 ) {
			divisor = 10000000;
			unit    = " M";
			factor  = 10;
		}
		else {
			divisor = 100000000;
			unit    = " M";
			factor  = 100;
		}
		
		if ( max / divisor > 12 ) {
			divisor *= 5;
			factor  *= 5;
		}
		
		// tick distances
		progressSlider.setMajorTickSpacing( divisor / 2 );
		progressSlider.setMinorTickSpacing( divisor / 10 );
		
		// labels
		Hashtable<Integer, JLabel> dict = new Hashtable<Integer, JLabel>();
		for ( int i=0; i<=max; i += divisor ) {
			int display = Math.round( factor * i / divisor );
			dict.put( i, new JLabel(Integer.toString(display) + unit) );
		}
		progressSlider.setLabelTable( dict );
	}
	
	/**
	 * Toggles command and symbol of the play/pause button.
	 * 
	 * The given command is regarded as the command to be switched away from.
	 * 
	 * @param oldCmd    Old command which is to be left.
	 */
	public void togglePlayPauseButton( String oldCmd ) {
		if ( CMD_PLAY.equals(oldCmd) ) {
			btnPlayPause.setActionCommand( CMD_PAUSE );
			btnPlayPause.setText( Dict.get(Dict.CTRL_BTN_PAUSE) );
		}
		else if ( CMD_PAUSE.equals(oldCmd) ) {
			btnPlayPause.setActionCommand( CMD_PLAY );
			btnPlayPause.setText( Dict.get(Dict.CTRL_BTN_PLAY) );
		}
	}
	
//	public void pressReparseButton() {
//		btnReparse.doClick();
//	}
//	
//	public void pressSoundcheckButton() {
//		btnSoundcheck.doClick();
//	}
//	
//	public void pressInfoButton() {
//		btnInfo.doClick();
//	}
//	
//	public void pressForwardButton() {
//		btnForw.doClick();
//	}
//	
//	public void pressRewindButton() {
//		btnRew.doClick();
//	}
//	
//	public void pressStopButton() {
//		btnStop.doClick();
//	}
//	
//	public void pressMemorizeButton() {
//		btnMemorize.doClick();
//	}
//
	
	/**
	 * Clicks the jump button.
	 * This is called if ENTER is pressed while the jump text field is focused.
	 */
	public void pressJumpButton() {
		btnJump.doClick();
	}
	
//	public void focusJumpField() {
//		fldJump.requestFocus();
//	}
	
	/**
	 * Fills the memory text field with the given value (current tick).
	 * Also changes the background color of the text field to normal.
	 * 
	 * This is called if the current tickstamp in the MIDI stream is memorized.
	 * 
	 * @param pos Current tickstamp in the MIDI stream.
	 */
	public void setMemory( long pos ) {
		fldJump.setText( Long.toString(pos) );
		setTextFieldColor( fldJump.getName(), Config.COLOR_NORMAL );
	}
	
	/**
	 * Returns the content of the memory text field.
	 * 
	 * This is called if the jump button is pressed.
	 * 
	 * @return Content of the memory text field.
	 */
	public String getMemory() {
		return fldJump.getText();
	}
	
	/**
	 * Sets the volume text field under the global volume slider to the given value.
	 * 
	 * @param volume  Value to set.
	 */
	public void setVolumeField( byte volume ) {
		if ( null == fldVol )
			return;
		fldVol.getDocument().removeDocumentListener( controller );
		fldVol.setText( Byte.toString(volume) );
		fldVol.getDocument().addDocumentListener( controller );
		setTextFieldColor( fldVol.getName(), Config.COLOR_NORMAL );
	}
	
	/**
	 * Returns the content of the volume text field under the global volume slider.
	 * 
	 * @return Volume from the text field.
	 * @throws NumberFormatException if the text cannot be parsed to a byte value.
	 */
	public byte getVolumeFromField() throws NumberFormatException {
		return Byte.parseByte( fldVol.getText() );
	}
	
	/**
	 * Sets the tempo text field under the global tempo slider to the given value.
	 * 
	 * @param tempoFactor  Value to be set.
	 */
	public void setTempoField( float tempoFactor ) {
		if ( null == fldTempo )
			return;
		
		// limit the value to TEMPO_FRACT digits after the decimal point
		int accuracy = (int) Math.pow( 10, TEMPO_FRACT );
		tempoFactor = ( (float) Math.round(tempoFactor * accuracy) ) / accuracy;
		
		// set the text field
		fldTempo.getDocument().removeDocumentListener( controller );
		fldTempo.setText( Float.toString(tempoFactor) );
		fldTempo.getDocument().addDocumentListener( controller );
		setTextFieldColor( fldTempo.getName(), Config.COLOR_NORMAL );
	}
	
	/**
	 * Returns the content of the tempo text field under the global tempo slider.
	 * 
	 * @return Tempo factor from the text field.
	 * @throws NumberFormatException if the text cannot be parsed to a float value.
	 */
	public float getTempoFromField() throws NumberFormatException {
		return Float.parseFloat( fldTempo.getText() );
	}
	
	/**
	 * Sets the transpose text field under the global transpose slider to the given value.
	 * 
	 * @param level  Value to be set.
	 */
	public void setTransposeField( byte level ) {
		if ( null == fldTranspose )
			return;
		fldTranspose.getDocument().removeDocumentListener( controller );
		fldTranspose.setText( Byte.toString(level) );
		fldTranspose.getDocument().addDocumentListener( controller );
		setTextFieldColor( fldTranspose.getName(), Config.COLOR_NORMAL );
	}
	
	/**
	 * Returns the content of the transpose text field under the global transpose slider.
	 * 
	 * @return Transpose level from the text field.
	 * @throws NumberFormatException if the text cannot be parsed to a byte value.
	 */
	public byte getTransposeFromField() throws NumberFormatException {
		return Byte.parseByte( fldTranspose.getText() );
	}
	
	/**
	 * Returns the content of the channel volume text field in the channel details area.
	 * 
	 * @param channel  MIDI channel.
	 * @return Channel volume from the text field.
	 * @throws NumberFormatException if the text cannot be parsed to a byte value.
	 */
	public byte getChannelVolumeFromField( byte channel ) throws NumberFormatException {
		JTextField fld = channelVolumeFields.get( channel );
		return Byte.parseByte( fld.getText() );
	}
	
	/**
	 * Sets the channel volume text field in the channel details area.
	 * 
	 * @param channel  MIDI channel.
	 * @param volume   Value to be set.
	 */
	public void setChannelVolumeField( byte channel, byte volume ) {
		JTextField fldVolume = channelVolumeFields.get( channel );
		fldVolume.getDocument().removeDocumentListener( controller );
		fldVolume.setText( Integer.toString(volume) );
		fldVolume.getDocument().addDocumentListener( controller );
		setTextFieldColor( fldVolume.getName(), Config.COLOR_NORMAL );
	}
	
	/**
	 * Sets the tick and time maximum values in the top right corner and the maximum value of the progress slider.
	 * 
	 * @param tickLength  Length of the MIDI stream in ticks.
	 * @param timeLength  Length of the MIDI stream as a time string.
	 */
	public void setTickAndTimeLength( long tickLength, String timeLength ) {
		progressSlider.setMaximum( (int) tickLength );
		lblTotalTicks.setText( Long.toString(tickLength) );
		lblTotalTime.setText( timeLength );
	}
	
	/**
	 * Refreshes the progress slider state and the labels for the current tickstamp
	 * and current time - called indirectly by the {@link RefresherThread}.
	 * 
	 * @param midiTicks    Current tickstamp.
	 * @param time         Current timestamp.
	 */
	public void refreshProgressBar( long midiTicks, String time ) {
		progressSlider.setValue( (int) midiTicks );
		lblCurrentTicks.setText( Long.toString(midiTicks) );
		lblCurrentTime.setText( time );
	}
	
	/**
	 * Sets the progress slider to the given value.
	 * 
	 * This is called after scrolling with the mouse over the slider.
	 * 
	 * @param ticks  Value to be set.
	 */
	public void setProgressSlider( int ticks ) {
		progressSlider.setValue( ticks );
	}
	
	/**
	 * Sets the volume slider to the given value.
	 * 
	 * This is called:
	 * - after scrolling with the mouse over the slider;
	 * - after typing a value in the global volume text field; and
	 * - after opening the player window.
	 * 
	 * @param volume  Value to be set.
	 */
	public void setVolumeSlider( byte volume ) {
		volumeSlider.setValue( volume );
	}
	
	/**
	 * Sets the tempo slider to the given value.
	 * 
	 * This is called:
	 * - after scrolling with the mouse over the slider;
	 * - after typing a value in the tempo text field; and
	 * - after opening the player window.
	 * 
	 * @param tempoFactor  Value to be set.
	 */
	public void setTempoSlider( float tempoFactor ) {
		int tempoTicks = (int) ( tempoFactor * TEMPO_DEFAULT / 1f );
		tempoSlider.setValue( tempoTicks );
	}
	
	/**
	 * Sets the transpose slider to the given value.
	 * 
	 * This is called:
	 * - after scrolling with the mouse over the slider;
	 * - after typing a value in the transpose text field; and
	 * - after opening the player window.
	 * 
	 * @param level  Value to be set.
	 */
	public void setTransposeSlider( byte level ) {
		transposeSlider.setValue( level );
	}
	
	/**
	 * Sets the given channel volume slider to the given volume value.
	 * 
	 * This is called:
	 * - after scrolling with the mouse over the slider;
	 * - after typing a value in the channel volume text field; and
	 * - after opening the player window.
	 * 
	 * @param channel  MIDI channel.
	 * @param volume   Value to be set.
	 */
	public void setChannelVolumeSlider( byte channel, byte volume ) {
		channelVolumeSliders.get( channel ).setValue( volume );
	}
	
	/**
	 * Shows if the progress slider is being changed via mouse click in the moment.
	 * 
	 * Returns true, if a mouse click on the slider has been started (mouse down) but
	 * is not yet finished (mouse up).
	 * 
	 * @return **true**, if the slider is being changed. Otherwise: **false**.
	 */
	public boolean isProgressSliderAdjusting() {
		return progressSlider.getValueIsAdjusting();
	}
	
	/**
	 * Shows if the global volume slider is being changed via mouse click in the moment.
	 * 
	 * Returns true, if a mouse click on the slider has been started (mouse down) but
	 * is not yet finished (mouse up).
	 * 
	 * @return **true**, if the slider is being changed. Otherwise: **false**.
	 */
	public boolean isVolumeSliderAdjusting() {
		return volumeSlider.getValueIsAdjusting();
	}
	
	/**
	 * Shows if the tempo slider is being changed via mouse click in the moment.
	 * 
	 * Returns true, if a mouse click on the slider has been started (mouse down) but
	 * is not yet finished (mouse up).
	 * 
	 * @return **true**, if the slider is being changed. Otherwise: **false**.
	 */
	public boolean isTempoSliderAdjusting() {
		return tempoSlider.getValueIsAdjusting();
	}
	
	/**
	 * Shows if the transpose slider is being changed via mouse click in the moment.
	 * 
	 * Returns true, if a mouse click on the slider has been started (mouse down) but
	 * is not yet finished (mouse up).
	 * 
	 * @return **true**, if the slider is being changed. Otherwise: **false**.
	 */
	public boolean isTransposeSliderAdjusting() {
		return transposeSlider.getValueIsAdjusting();
	}
	
	/**
	 * Shows if the volume slider of the given channel is being changed via mouse click in the moment.
	 * 
	 * Returns true, if a mouse click on the slider has been started (mouse down) but
	 * is not yet finished (mouse up).
	 * 
	 * @param channel  MIDI channel
	 * @return **true**, if the slider is being changed. Otherwise: **false**.
	 */
	public boolean isChVolSliderAdjusting( byte channel ) {
		try {
			JSlider slider = channelVolumeSliders.get( channel );
			return slider.getValueIsAdjusting();
		}
		catch ( IndexOutOfBoundsException e ) {
			// the slider has not yet been put into the collection
			return false;
		}
	}
	
	/**
	 * Check or uncheck the given channel's **mute** checkbox according to the mute state
	 * in the MIDI device.
	 * 
	 * Called when the player window is opened.
	 * 
	 * @param channel  MIDI channel number.
	 * @param mute     **true** to check the checkbox, **false** to uncheck it.
	 */
	public void setMute( int channel, boolean mute ) {
		muteCbx.get( channel ).setSelected( mute );
	}
	
	/**
	 * Check or uncheck the given channel's **solo** checkbox according to the solo state
	 * in the MIDI device.
	 * 
	 * Called when the player window is opened.
	 * 
	 * @param channel  MIDI channel number.
	 * @param solo     **true** to check the checkbox, **false** to uncheck it.
	 */
	public void setSolo( int channel, boolean solo ) {
		soloCbx.get( channel ).setSelected( solo );
	}
	
	/**
	 * Sets the global sliders on the right side to the given values.
	 * 
	 * This is called when the player window is opened.
	 * 
	 * @param volume  Value for the volume slider.
	 * @param tempo   Value for the tempo slider.
	 * @param level   Value for the transpose slider.
	 */
	public void setGlobalSlidersAndFields( byte volume, float tempo, byte level ) {
		setVolumeSlider( volume );
		setTempoSlider( tempo );
		setTransposeSlider( level );
		setVolumeField( volume );
		setTempoField( tempo );
		setTransposeField( level );
	}
	
	/**
	 * Sets the Channel information.
	 * This consists of bank number, program number, instrument name and channel comment.
	 * 
	 * @param channel       MIDI channel number.
	 * @param bankNumShort  bank number syntax -- MSB, if LSB is null; otherwise: MSB and LSB,
	 *                      separated by the currently configured separator
	 * @param bankNumLong   full bank number, MSB and LSB in a human-readable form
	 * @param program       program (instrument) number
	 * @param instrName     instrument name
	 * @param comment       channel comment (META event INSTRUMENT NAME)
	 */
	public void setInstrumentInfo( int channel, String bankNumShort, String bankNumLong, String program, String instrName, String comment ) {
		
		// program number
		channelProgramNumbers.get( channel ).setText( program );
		
		// instrument name
		channelInstruments.get( channel ).setText( instrName );
		
		// comment
		channelComments.get( channel ).setText( comment );
		
		// bank number
		channelBankNumbers.get( channel ).setText( bankNumShort );
		channelBankNumbers.get( channel ).setToolTipText( bankNumLong );
	}
	
	/**
	 * Sets the given channel's activity icon and the according tooltip to show
	 * an active or inactive state.
	 * 
	 * @param channel  MIDI channel number.
	 * @param active   **true** to show an active state, **false** to show an inactive state.
	 */
	public void setActivityLED( int channel, boolean active ) {
		JLabel lbl = channelActivityLEDs.get( channel );
		if (active) {
			lbl.setToolTipText( Dict.get(Dict.ACTIVITY_ACTIVE) );
			lbl.setIcon( AC_ICON_ACTIVE );
		}
		else {
			lbl.setToolTipText( Dict.get(Dict.ACTIVITY_INACTIVE) );
			lbl.setIcon( AC_ICON_INACTIVE );
		}
	}
	
	/**
	 * Sets the text field with the given name to the given background color.
	 * 
	 * This shows if the content of the text field is valid.
	 * 
	 * @param name   The text field's name.
	 * @param color  The designated background color.
	 */
	public void setTextFieldColor( String name, Color color ) {
		if ( NAME_JUMP.equals(name) )
			fldJump.setBackground( color );
		else if ( NAME_VOL.equals(name) )
			fldVol.setBackground( color );
		else if ( NAME_TEMPO.equals(name) )
			fldTempo.setBackground( color );
		else if ( NAME_TRANSPOSE.equals(name) )
			fldTranspose.setBackground( color );
		else if ( name.startsWith(NAME_CH_VOL) ) {
			name = name.replaceFirst( NAME_CH_VOL, "" );
			byte channel = Byte.parseByte( name );
			channelVolumeFields.get( channel ).setBackground( color );
		}
	}
	
	/**
	 * Determines if one of the text fields is focused.
	 * 
	 * @return **true**, if one of the text fields is focused. Otherwise: returns **false**.
	 */
	public boolean isTextfieldFocussed() {
		if ( fldJump.hasFocus() || fldVol.hasFocus() || fldTempo.hasFocus() || fldTranspose.hasFocus() )
			return true;
		for ( JTextField fld : channelVolumeFields )
			if ( fld.hasFocus() )
				return true;
		
		return false;
	}
	
	/**
	 * Shows and/or hides a channel's detail area after a click on the channel number.
	 * 
	 * Hides the clicked channel if is visible.
	 * Otherwise shows it and hides all other channels' detail areas.
	 * 
	 * @param channel  number of the MIDI channel that has been clicked.
	 */
	public void toggleChannelDetails( int channel ) {
		Component detailArea = channelDetails.get( channel );
		if ( detailArea.isVisible() ) {
			// it's visible --> hide it
			detailArea.setVisible( false );
		}
		else {
			// it's hidden --> hide all other areas and show this one
			for ( Component area : channelDetails )
				area.setVisible( false );
			detailArea.setVisible( true );
		}
		pack();
	}
	
	/**
	 * Adds key bindings to the player window by creating a {@link KeyEventPostProcessor}
	 * (if not yet done) and adding it to the {@link KeyboardFocusManager}.
	 * 
	 * This is called whenever the player window is focused.
	 */
	public void addKeyBindings() {
		
		if ( null == keyProcessor ) {
			keyProcessor = new KeyEventPostProcessor() {
				public boolean postProcessKeyEvent( KeyEvent e ) {
					
					if ( KeyEvent.KEY_PRESSED == e.getID() ) {
						
						// handle slider adjustments via keyboard
						SliderHelper.handleSliderAdjustmentViaKey( e );
						
						// don't handle already consumed shortcuts any more
						if ( e.isConsumed() )
							return true;
						
						if ( KeyEvent.VK_ESCAPE == e.getKeyCode() ) {
							btnStop.doClick();
							return true;
						}
						
						// Unfortunately the postprocessing takes place before
						// any text field input. So we have to check if a textfield is focused.
						if ( isTextfieldFocussed() ) {
							return false;
						}
						
						switch ( e.getKeyCode() ) {
							case KeyEvent.VK_SPACE:
								btnPlayPause.doClick();
								break;
							case KeyEvent.VK_C:
								btnPlayPause.doClick();
								break;
							case KeyEvent.VK_P:
								btnPlayPause.doClick();
								break;
							case KeyEvent.VK_LEFT:
								btnRew.doClick();
								break;
							case KeyEvent.VK_RIGHT:
								btnForw.doClick();
								break;
							case KeyEvent.VK_DOWN:
								btnFastRew.doClick();
								break;
							case KeyEvent.VK_UP:
								btnFastForw.doClick();
								break;
							case KeyEvent.VK_ENTER:
								pressJumpButton();
								break;
							case KeyEvent.VK_M:
								btnMemorize.doClick();
								break;
							case KeyEvent.VK_G:
								btnJump.doClick();
								break;
							case KeyEvent.VK_F:
								fldJump.requestFocus();
								break;
							case KeyEvent.VK_F5:
								btnReparse.doClick();
								break;
							default:
								break;
						}
					}
					return e.isConsumed();
				}
			};
		}
		
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventPostProcessor( keyProcessor );
	}
	
	/**
	 * Removes the key bindings from the player window by removing the according
	 * {@link KeyEventPostProcessor} from the {@link KeyboardFocusManager}.
	 * 
	 * This is called if the player window is closed.
	 */
	public void removeKeyBindings() {
		KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventPostProcessor( keyProcessor );
	}
}
