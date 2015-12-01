/*
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.midica.ui.player;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.swing.JCheckBox;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.midica.config.Dict;
import org.midica.file.ParseException;
import org.midica.file.SequenceParser;
import org.midica.midi.MidiDevices;
import org.midica.midi.SequenceNotSetException;
import org.midica.ui.ErrorMsgView;
import org.midica.ui.info.InfoView;
import org.midica.ui.player.soundcheck.SoundcheckView;


/**
 * This class provides the controller of the player window.
 * 
 * It listens and reacts to events in the {@link PlayerView}.
 * 
 * @author Jan Trukenmüller
 */
public class PlayerController implements ActionListener, WindowListener, ChangeListener,
	DocumentListener, MouseWheelListener {
	
	private PlayerView      view        = null;
	private ErrorMsgView    errorMsg    = null;
	private RefresherThread refresher   = null;
	private SequenceParser  parser      = null;
	private File            currentFile = null;
	
	/**
	 * Creates a new listener object for the player and a {@link RefresherThread},
	 * refreshing the progress slider.
	 * 
	 * @param view           The player window.
	 * @param parser         The parser that has successfully parsed the current file.
	 * @param currentFile    The last loaded (successfully parsed) file.
	 */
	public PlayerController( PlayerView view, SequenceParser parser, File currentFile ) {
		this.view        = view;
		this.parser      = parser;
		this.currentFile = currentFile;
		this.refresher   = new RefresherThread( this );
	}
	
	/**
	 * Handles all action events that have happened in the player window.
	 * 
	 * Those are:
	 * 
	 * - pushing a button
	 * - pressing ENTER in a text field
	 * 
	 * @param e    The invoked action event.
	 */
	@Override
	public void actionPerformed( ActionEvent e ) {
		String cmd = e.getActionCommand();
		
		// player control command button pushed
		if (   PlayerView.CMD_PLAY.equals(cmd) || PlayerView.CMD_PAUSE.equals(cmd)
			|| PlayerView.CMD_STOP.equals(cmd) || PlayerView.CMD_REW.equals(cmd)
			|| PlayerView.CMD_FWD.equals(cmd)  || PlayerView.CMD_FAST_REW.equals(cmd)
			|| PlayerView.CMD_FAST_FWD.equals(cmd) ) {
				performControlCommand( cmd );
		}
		
		// button pushed for memorizing the current tickstamp
		else if ( PlayerView.CMD_MEMORIZE.equals(cmd) ) {
			long pos = MidiDevices.getTickPosition();
			view.setMemory( pos );
		}
		
		// button pushed for jumping to the memorized tickstamp
		else if ( PlayerView.CMD_JUMP.equals(cmd) ) {
			String memory = view.getMemory();
			try {
				long pos = Long.parseLong( memory );
				MidiDevices.setTickPosition( pos );
				view.setTextFieldColor( PlayerView.NAME_JUMP, PlayerView.COLOR_NORMAL );
				view.resetChannelActivity();
			}
			catch( NumberFormatException ex ) {
			}
		}
		
		// button pushed for reparsing the currently loaded file
		else if ( PlayerView.CMD_REPARSE.equals(cmd) ) {
			reparse();
			view.resetChannelActivity();
		}
		
		else if ( PlayerView.CMD_SOUNDCHECK.equals(cmd) ) {
			SoundcheckView.showSoundcheck( view );
		}
		
		// button pushed to open the info window
		else if ( PlayerView.CMD_INFO.equals(cmd) ) {
			InfoView.showInfoWindow( view );
		}
		
		// button pushed to close the error message window
		else if ( ErrorMsgView.CMD_CLOSE.equals(cmd) ) {
			errorMsg.close();
		}
		
		// butto pushed to show/hide the details of a channel
		else if ( cmd.startsWith(PlayerView.CMD_SHOW_HIDE) ) {
			cmd = cmd.replaceFirst( PlayerView.CMD_SHOW_HIDE, "" );
			int channel = Integer.parseInt( cmd );
			view.toggleChannelDetails( channel );
		}
		
		// enter pressed in a text field
		Component component = (Component) e.getSource();
		if ( component instanceof JTextField ) {
			String name = component.getName();
			
			try {
				// jump text field
				if ( PlayerView.NAME_JUMP.equals(name) ) {
					view.pressJumpButton();
				}
				
				// volume field
				else if ( PlayerView.NAME_VOL.equals(name) ) {
					byte volume = view.getVolumeFromField(); // throws NumberFormatException
					if ( volume < 0 || volume > 127 )
						throw new NumberFormatException();
					MidiDevices.setVolume( volume );
					view.setVolumeSlider( volume );
				}
				
				// tempo field
				else if ( PlayerView.NAME_TEMPO.equals(name) ) {
					float tempoFactor = view.getTempoFromField(); // throws NumberFormatException
					if ( tempoFactor < 0 )
						throw new NumberFormatException();
					MidiDevices.setTempo( tempoFactor );
					view.setTempoSlider( tempoFactor );
				}
				
				// transpose field
				else if ( PlayerView.NAME_TRANSPOSE.equals(name) ) {
					byte level = view.getTransposeFromField(); // throws NumberFormatException
					if ( level < PlayerView.TRANSPOSE_MIN || level > PlayerView.TRANSPOSE_MAX )
						throw new NumberFormatException();
					SequenceParser.setTransposeLevel( level );
					view.setTransposeSlider( level );
					reparse();
				}
				
				// channel volume field
				else if ( name.startsWith(PlayerView.NAME_CH_VOL) ) {
					String channelStr = name.replaceFirst( PlayerView.NAME_CH_VOL, "" );
					byte channel = Byte.parseByte( channelStr );
					byte volume = view.getChannelVolumeFromField( channel ); // throws NumberFormatException
					if ( volume < -127 || volume > 127 )
						throw new NumberFormatException();
					view.setChannelVolumeSlider( channel, volume );
					MidiDevices.setChannelVolume( channel, volume );
				}
				
				// no exception yet, so the field has been set successfully
				view.setTextFieldColor( name, PlayerView.COLOR_NORMAL );
			}
			catch ( NumberFormatException ex ) {
			}
		}
	}
	
	/**
	 * Adds the key bindings - called when the window is focused.
	 * 
	 * @param e    Window activation event.
	 */
	@Override
	public void windowActivated( WindowEvent e ) {
		view.addKeyBindings();
	}
	
	/**
	 * Removes key bindings and stops and closes player-related resources and
	 * windows - called, if the window is going to be closed.
	 * 
	 * - removes the key bindings
	 * - closes the soundcheck window
	 * - stops the {@link RefresherThread} to refresh the progress slider
	 * - stops and destroys the MIDI devices
	 * 
	 * @param e    Window activation event.
	 */
	@Override
	public void windowClosing( WindowEvent e ) {
		view.removeKeyBindings();
		try {
			SoundcheckView.close();
			refresher.die();
			MidiDevices.stop();
			MidiDevices.destroyDevices();
		}
		catch ( MidiUnavailableException ex ) {
			showErrorMessage( ex );
		}
	}
	
	/**
	 * Removes key bindings - called, if the window is closed.
	 * 
	 * @param e    Window closed event.
	 */
	@Override
	public void windowClosed( WindowEvent e ) {
		view.removeKeyBindings();
	}
	
	/**
	 * Removes key bindings - called, if the window is deactivated.
	 * 
	 * @param e    Window deactivated event.
	 */
	@Override
	public void windowDeactivated( WindowEvent e ) {
		view.removeKeyBindings();
	}
	
	@Override
	public void windowDeiconified( WindowEvent e ) {
	}
	
	/**
	 * Does nothing - called, if the window is iconified.
	 * 
	 * @param e    Window deactivated event.
	 */
	@Override
	public void windowIconified( WindowEvent e ) {
	}
	
	/**
	 * Initializes resources needed for the player - called, if the window is opened.
	 * 
	 * - creates and sets up MIDI devices
	 * - adjusts the progress slider to the length of the MIDI stream
	 * - restores the channel-based widgets
	 * - starts the {@link RefresherThread} to refresh the progress slider
	 * 
	 * @param e    Window deactivated event.
	 */
	@Override
	public void windowOpened( WindowEvent e ) {
		try {
			MidiDevices.setupDevices( this );
		}
		catch ( InvalidMidiDataException ex ) {
			showErrorMessage( ex );
		}
		catch ( MidiUnavailableException ex ) {
			showErrorMessage( ex );
		}
		catch ( SequenceNotSetException ex ) {
			showErrorMessage( ex );
		}
		catch ( IOException ex ) {
			showErrorMessage( ex );
		}
		// init tick labels and progress slider
		view.setTickAndTimeLength( MidiDevices.getTickLength(), MidiDevices.getTimeLength() );
		view.initProgressSlider();
		view.setGlobalSlidersAndFields( MidiDevices.getVolume(), MidiDevices.getTempo(), SequenceParser.getTransposeLevel() );
		
		// restore the channel based widgets in the view
		for ( byte channel = 0; channel < MidiDevices.NUMBER_OF_CHANNELS; channel++ ) {
			// channel volume
			byte channelVolume = MidiDevices.getChannelVolume( channel );
			view.setChannelVolumeField( channel, channelVolume );
			view.setChannelVolumeSlider( channel, channelVolume );
			// mute
			boolean mute = MidiDevices.getMute( channel );
			view.setMute( channel, mute );
			// solo
			boolean solo = MidiDevices.getSolo( channel );
			view.setSolo( channel, solo );
		}
		
		refresher.start();
	}
	
	/**
	 * Refreshes the progress slider state automatically according to the
	 * sequencer state - called by the {@link RefresherThread}.
	 */
	public void refreshProgressBar() {
		// don't do refresh if it's moved manually
		if ( view.isProgressSliderAdjusting() )
			return;
		
		long   ticks = MidiDevices.getTickPosition();
		String time  = MidiDevices.getTimePosition();
		view.refreshProgressBar( ticks, time );
	}
	
	/**
	 * Makes the play/pause button ready to begin playing again - called if the end of
	 * the MIDI sequence is reached.
	 */
	public void endOfSequence() {
		view.togglePlayPauseButton( PlayerView.CMD_PAUSE );
	}
	
	/**
	 * Turns the given channel's activity LED on or off.
	 * 
	 * @param channel MIDI channel number.
	 * @param active  **true** to turn the LED on, **false** to turn it off.
	 */
	public void setChannelActivity( int channel, boolean active ) {
		view.setActivityLED( channel, active );
	}
	
	/**
	 * Updates the channel information labels of the given channel.
	 * Called, if a MIDI {@link MetaMessage} of the type *TRACK NAME* occurs.
	 * The following information is updated:
	 * 
	 * - program number
	 * - instrument name
	 * - track comment
	 * 
	 * @param channel    Channel number (0-15).
	 */
	public void channelInfoChanged( int channel ) {
		// program number / instrument name / comment
		String[] info = MidiDevices.getChannelInfo( channel );
		view.setInstrumentInfo( channel, info );
	}
	
	/**
	 * Handles slider and checkbox events.
	 * 
	 * Channel-independent actions:
	 * 
	 * - Adjusts the sequencer state according to the mannually changed process slider state.
	 * - Adjusts the sequencer state according to the mannually changed tempo slider state.
	 * - Adjusts the synthesizer state according to the mannually changed volume slider state.
	 * - Adjusts the transpose level according to the manually changed transpose slider.
	 * 
	 * Channel-dependent actions:
	 * 
	 * - Adjusts the synthesizer state according to the mannually changed channel volume slider state.
	 * - Adjusts the channel mute state according to the manually changed checkbox.
	 * - Adjusts the channel solo state according to the manually changed checkbox.
	 * 
	 * @param e    Event object.
	 */
	@Override
	public void stateChanged( ChangeEvent e ) {
		String name = ((Component) e.getSource()).getName();
		
		// handle progress slider changes
		if ( PlayerView.NAME_PROGRESS.equals(name) ) {
			
			// only react if it's moved manually
			if ( ! view.isProgressSliderAdjusting() )
				return;
			MidiDevices.setTickPosition( ((JSlider)e.getSource()).getValue() );
			view.resetChannelActivity();
		}
		
		// handle volume slider changes
		else if ( PlayerView.NAME_VOL.equals(name) ) {
			
			// only react if it's moved manually
			if ( ! view.isVolumeSliderAdjusting() )
				return;
			byte volume  = (byte) ( (JSlider) e.getSource() ).getValue();
			view.setVolumeField( volume );
			MidiDevices.setVolume( volume );
		}
		
		// handle tempo slider changes
		else if ( PlayerView.NAME_TEMPO.equals(name) ) {
			
			// only react if it's moved manually
			if ( ! view.isTempoSliderAdjusting() )
				return;
			int value = ( (JSlider) e.getSource() ).getValue();
			float tempo = ( (float) value ) / PlayerView.TEMPO_DEFAULT;
			view.setTempoField( tempo );
			MidiDevices.setTempo( tempo );
		}
		
		// handle transpose slider changes
		else if ( PlayerView.NAME_TRANSPOSE.equals(name) ) {
			// only react if it's moved manually
			if ( ! view.isTransposeSliderAdjusting() )
				return;
			byte level = (byte) ( (JSlider) e.getSource() ).getValue();
			view.setTransposeField( level );
			SequenceParser.setTransposeLevel( level );
			reparse();
		}
		
		// handle channel volume slider changes
		else if ( name.startsWith(PlayerView.NAME_CH_VOL) ) {
			name = name.replaceFirst( PlayerView.NAME_CH_VOL, "" );
			byte channel = Byte.parseByte( name );
			// only react if it's moved manually
			if ( ! view.isChVolSliderAdjusting(channel) )
				return;
			byte volume = (byte) ( (JSlider) e.getSource() ).getValue();
			view.setChannelVolumeField( channel, volume );
			MidiDevices.setChannelVolume( channel, (byte) volume );
		}
		
		// mute a channel
		else if ( name.startsWith(PlayerView.NAME_MUTE) ) {
			JCheckBox cbx = (JCheckBox) e.getSource();
			name = name.replaceFirst( PlayerView.NAME_MUTE, "" );
			int channel = Integer.parseInt( name );
			boolean mute = cbx.isSelected();
			MidiDevices.setMute( channel, mute );
		}
		
		// solo a channel
		else if ( name.startsWith(PlayerView.NAME_SOLO) ) {
			JCheckBox cbx = (JCheckBox) e.getSource();
			name = name.replaceFirst( PlayerView.NAME_SOLO, "" );
			int channel = Integer.parseInt( name );
			boolean solo = cbx.isSelected();
			MidiDevices.setSolo( channel, solo );
		}
	}
	
	/**
	 * Handles mouse wheel scrolling over a {@link JSlider}.
	 * Sets the slider to the new position, if possible, or otherwise to the minimum
	 * or maximum of the slider's range.
	 * 
	 * The following sliders are supported by this method:
	 * 
	 * - process slider
	 * - volume slider
	 * - tempo slider
	 * - transpose slider
	 * - channel volume slider
	 * 
	 * @param e    Event object.
	 */
	@Override
	public void mouseWheelMoved( MouseWheelEvent e ) {
		String name     = ((Component) e.getSource()).getName();
		int amount      = e.getWheelRotation();
		int sliderTicks = ( (JSlider) e.getSource() ).getValue();
		
		// process slider scrolls
		if ( PlayerView.NAME_PROGRESS.equals(name) ) {
			// get and check new slider state
			sliderTicks -= ( amount * PlayerView.PROGRESS_SCROLL );
			int max = (int) MidiDevices.getTickLength();
			if ( sliderTicks < 0 )
				sliderTicks = 0;
			else if ( sliderTicks > max )
				sliderTicks = max;
			
			// set new slider state and apply the resulting actions
			MidiDevices.setTickPosition( sliderTicks );
			view.setProgressSlider( sliderTicks );
			view.resetChannelActivity();
		}
		
		// volume slider scrolls
		else if ( PlayerView.NAME_VOL.equals(name) ) {
			// get and check new slider state
			sliderTicks -= amount * PlayerView.VOL_SCROLL;
			if ( sliderTicks < PlayerView.VOL_MIN )
				sliderTicks = PlayerView.VOL_MIN;
			else if ( sliderTicks > PlayerView.VOL_MAX )
				sliderTicks = PlayerView.VOL_MAX;
			
			// set new slider state and apply the resulting actions
			view.setVolumeSlider( (byte) sliderTicks );
			view.setVolumeField( (byte) sliderTicks );
			MidiDevices.setVolume( (byte) sliderTicks );
		}
		
		// tempo slider scrolls
		else if ( PlayerView.NAME_TEMPO.equals(name) ) {
			// get and check new slider state
			sliderTicks -= ( amount * PlayerView.TEMPO_SCROLL );
			if ( sliderTicks < PlayerView.TEMPO_MIN )
				sliderTicks = PlayerView.TEMPO_MIN;
			else if ( sliderTicks > PlayerView.TEMPO_MAX )
				sliderTicks = PlayerView.TEMPO_MAX;
			
			// calculate tempo factor from tempo ticks
			float tempoFactor = (float) sliderTicks / PlayerView.TEMPO_DEFAULT;
			
			// set new slider state and apply the resulting actions
			view.setTempoSlider( tempoFactor );
			view.setTempoField( tempoFactor );
			MidiDevices.setTempo( tempoFactor );
		}
		
		// transpose slider scrolls
		else if ( PlayerView.NAME_TRANSPOSE.equals(name) ) {
			// get and check new slider state
			sliderTicks -= ( amount * PlayerView.TRANSPOSE_SCROLL );
			if ( sliderTicks < PlayerView.TRANSPOSE_MIN )
				sliderTicks = PlayerView.TRANSPOSE_MIN;
			else if ( sliderTicks > PlayerView.TRANSPOSE_MAX )
				sliderTicks = PlayerView.TRANSPOSE_MAX;
			
			// set new slider state and apply the resulting actions
			view.setTransposeSlider( (byte) sliderTicks );
			view.setTransposeField( (byte) sliderTicks );
			SequenceParser.setTransposeLevel( (byte) sliderTicks );
			reparse();
		}
		
		// channel volume slider scrolls
		else if ( name.startsWith(PlayerView.NAME_CH_VOL) ) {
			// get channel
			name = name.replaceFirst( PlayerView.NAME_CH_VOL, "" );
			byte channel = Byte.parseByte( name );
			
			// get and check new slider state
			sliderTicks -= amount * PlayerView.CH_VOL_SCROLL;
			if ( sliderTicks < PlayerView.CH_VOL_MIN_VAL )
				sliderTicks = PlayerView.CH_VOL_MIN_VAL;
			else if ( sliderTicks > PlayerView.CH_VOL_MAX_VAL )
				sliderTicks = PlayerView.CH_VOL_MAX_VAL;
			
			// set new slider state and apply the resulting actions
			view.setChannelVolumeSlider( channel, (byte) sliderTicks );
			view.setChannelVolumeField( channel, (byte) sliderTicks );
			MidiDevices.setChannelVolume( channel, (byte) sliderTicks );
		}
	}
	
	/**
	 * Handles change events in text fields by checking the changed content.
	 * 
	 * The text is checked against the expected number format and range.
	 * The text field's background will be changed to red, if the check fails, 
	 * or to green, if it succeeds.
	 * 
	 * The following text fields are handled by this method:
	 * 
	 * - The jump field (must be between 0 and the total number of ticks).
	 * - The volume field (must be between 0 and 127)
	 * - The tempo field (must be a float greater or equal than 0)
	 * - The transpose field (must be between -30 and 30)
	 * - The channel volume fields (must be between -127 and 127)
	 * 
	 * @param e    Event object.
	 */
	private void handleTextFieldChanges( DocumentEvent e ) {
		Document doc  = e.getDocument();
		String   name = doc.getProperty( "name" ).toString();
		try {
			String text = doc.getText( 0, doc.getLength() );
			
			// jump field changed
			if ( PlayerView.NAME_JUMP.equals(name) ) {
				long max   = MidiDevices.getTickLength();
				int  ticks = Integer.parseInt( text );
				if ( ticks < 0 || ticks > max )
					throw new NumberFormatException();
			}
			
			// volume field changed
			else if ( PlayerView.NAME_VOL.equals(name) ) {
				byte volume = Byte.parseByte( text );
				if ( volume < 0 || volume > 127 )
					throw new NumberFormatException();
			}
			
			// tempo field changed
			else if ( PlayerView.NAME_TEMPO.equals(name) ) {
				float tempoFactor = Float.parseFloat( text );
				if ( tempoFactor < 0 )
					throw new NumberFormatException();
			}
			
			// transpose field changed
			else if ( PlayerView.NAME_TRANSPOSE.equals(name) ) {
				byte level = Byte.parseByte( text );
				if ( level < PlayerView.TRANSPOSE_MIN || level > PlayerView.TRANSPOSE_MAX )
					throw new NumberFormatException();
			}
			
			// channel volume field changed
			else if ( name.startsWith(PlayerView.NAME_CH_VOL) ) {
				byte volume = Byte.parseByte( text );
				if ( volume < -127 || volume > 127 )
					throw new NumberFormatException();
			}
			
			// no exception yet, so the field input is ok
			view.setTextFieldColor( name, PlayerView.COLOR_OK );
		}
		catch ( NumberFormatException ex ) {
			view.setTextFieldColor( name, PlayerView.COLOR_ERROR );
		}
		catch ( BadLocationException ex ) {
		}
	}
	
	/**
	 * Checks text field contents after a change (by calling
	 * {@link #handleTextFieldChanges(DocumentEvent)}).
	 * 
	 * @param e    Event object.
	 */
	@Override
	public void changedUpdate( DocumentEvent e ) {
		handleTextFieldChanges(e);
	}
	
	/**
	 * Checks text field contents after a change (by calling
	 * {@link #handleTextFieldChanges(DocumentEvent)}).
	 * 
	 * @param e    Event object.
	 */
	@Override
	public void insertUpdate( DocumentEvent e ) {
		handleTextFieldChanges(e);
	}
	
	/**
	 * Checks text field contents after a change (by calling
	 * {@link #handleTextFieldChanges(DocumentEvent)}).
	 * 
	 * @param e    Event object.
	 */
	@Override
	public void removeUpdate( DocumentEvent e ) {
		handleTextFieldChanges(e);
	}
	
	/**
	 * Handles commands after a player control button has been pushed.
	 * 
	 * Those buttons are:
	 * 
	 * - play
	 * - pause
	 * - stop
	 * - forward
	 * - fast forward
	 * - rewind
	 * - fast rewind
	 * 
	 * @param cmd    The pushed button's command string.
	 */
	private void performControlCommand( String cmd ) {
		try {
			if ( PlayerView.CMD_PLAY.equals(cmd) ) {
				MidiDevices.play();
				view.togglePlayPauseButton(cmd);
			}
			else if ( PlayerView.CMD_PAUSE.equals(cmd) ) {
				MidiDevices.pause();
				view.togglePlayPauseButton(cmd);
			}
			else if ( PlayerView.CMD_FWD.equals(cmd) ) {
				MidiDevices.forward();
				view.resetChannelActivity();
			}
			else if ( PlayerView.CMD_REW.equals(cmd) ) {
				MidiDevices.rewind();
				view.resetChannelActivity();
			}
			else if ( PlayerView.CMD_FAST_FWD.equals(cmd) ) {
				MidiDevices.fastForward();
				view.resetChannelActivity();
			}
			else if ( PlayerView.CMD_FAST_REW.equals(cmd) ) {
				MidiDevices.fastRewind();
				view.resetChannelActivity();
			}
			else if ( PlayerView.CMD_STOP.equals(cmd) ) {
				MidiDevices.stop();
				view.togglePlayPauseButton( PlayerView.CMD_PAUSE );
				view.resetChannelActivity();
			}
		}
		catch ( InvalidMidiDataException e ) {
			showErrorMessage(e);
		}
		catch ( MidiUnavailableException e ) {
			showErrorMessage(e);
		}
		catch ( IllegalStateException e ) {
			showErrorMessage(e);
		}
	}
	
	/**
	 * Re-parses the last successfully parsed file again.
	 * Then sets up the MIDI devices again and connects them with the new MIDI stream.
	 * Re-builds the progress slider because the MIDI stream length could have changed.
	 * 
	 * Shows an error message if the parsing fails.
	 */
	private void reparse() {
		try {
			long    currentTicks = MidiDevices.getTickPosition();
			boolean isPlaying    = MidiDevices.isPlaying();
			MidiDevices.destroyDevices();
			parser.parse( currentFile );
			MidiDevices.setupDevices( this );
			MidiDevices.setTickPosition( currentTicks );
			view.setTickAndTimeLength( MidiDevices.getTickLength(), MidiDevices.getTimeLength() );
			view.initProgressSlider();
			if (isPlaying) {
				view.togglePlayPauseButton( PlayerView.CMD_PLAY );
				MidiDevices.play();
			}
			else
				view.togglePlayPauseButton( PlayerView.CMD_PAUSE );
		}
		catch ( ParseException ex ) {
			int    lineNumber = ex.getLineNumber();
			String fileName   = ex.getFileName();
			String msg = ex.getMessage();
			if ( lineNumber > 0 && null != fileName ) {
				msg = String.format(
					Dict.get(Dict.ERROR_IN_LINE),
					fileName,
					lineNumber
				) + msg;
			}
			showErrorMessage(msg);
		}
		catch ( InvalidMidiDataException ex ) {
			showErrorMessage(ex);
		}
		catch ( MidiUnavailableException ex ) {
			showErrorMessage(ex);
		}
		catch ( SequenceNotSetException ex ) {
			showErrorMessage(ex);
		}
		catch ( IOException ex ) {
			showErrorMessage(ex);
		}
	}
	
	/**
	 * Shows an error message based on an exception.
	 * 
	 * @param e    Exception containing the error message to be shown.
	 */
	private void showErrorMessage ( Exception e ) {
		errorMsg = new ErrorMsgView( view, this );
		errorMsg.init( e.getMessage() );
	}
	
	/**
	 * Shows an error message based on a message string.
	 * 
	 * @param message    Error message.
	 */
	private void showErrorMessage( String message ) {
		errorMsg = new ErrorMsgView( view, this );
		errorMsg.init( message );
	}
}
