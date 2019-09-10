<img align="right" src="img/logo.png" title="Midica Logo">

# Introduction
Midica is an interpreter for a Music Programming Language.
It translates source code to MIDI.

But it can also be used as a MIDI Player, a MIDI Karaoke player or a MIDI file analyzer.

You write music with the easy-to-learn programming language "MidicaPL".  
You can use the same techniques like in regular programming, like:

- Copy & Paste
- Macros
- Modules for code re-use
- Self-defined constants
- Code Comments

You write your songs in plain text files using the text editor of your choice. Midica transforms these files into MIDI.

# Install And Run
- Install Java Runtume Environment (JRE) version 1.7 or higher.
- Download the file `midica-VERSION.jar`.
- Start Midica using the command: `java -jar midica-VERSION.jar`

# Features of the Midica Application itself

- **Loading and playing** MIDI sequences from:
    - MidicaPL files
    - MIDI files
- **Integrated MIDI player** featuring:
    - Regulation of volume, tempo and pitch transposition
    - Memorizing a position in the MIDI sequence and jumping back to that position
    - Channel Overview (showing current instrument and channel activity)
    - Channel Detail viewer (Note name and number, volume and tick, channel by channel)
    - Channel configuration (Volume, Mute and Solo configurable channel by channel)
    - Quick reloading and reparsing of a loaded MidicaPL script
- **Karaoke Player**
    - Integrated as a part of the MIDI player
    - displaying syllables in different colors for future and past
    - displaying syllables in italic, shortly before they must be sung
- **Soundfonts**
    - Loading Soundfonts and using them for playback
    - Analyzing Soundfont contents
    - Test Soundfonts with the Soundcheck window
- **Exporting MIDI sequences**
    - As MIDI files
    - As MidicaPL files (not yet implemented)
- **Configuration** of
    - GUI language (currently English or German)
    - Note System - International (C,D,E,F,G,A,B), German (C,D,E,F,G,A,H), Italian (Do,Re,Mi,Fa,Sol,La,Si)
    - Half Tone Symbols (6 different systems)
    - Octave Naming - International (c0, c1, ...), German (C',C,c,c',c''...)
    - Syntax (3 different sets of key words for the programming language) - not _that_ important as the syntax can be redefined in MidicaPL anyway
    - Percussion IDs (English or German)
    - Instrument IDs (English or German)

# Screenshots

You can find a lot of screenshots here: http://www.midica.org/screenshots.html

I will not repeat them all in this Readme. But at least here are two screenshots of
the player. The first one shows the channel mode and the second one the Karaoke mode.

<img src="img/player.png" title="Midica Player"><img src="img/karaoke.png" title="Karaoke Mode">

# MidicaPL (Midica's Music programming language)

You can find a MidicaPL tutorial here:

- [Preparation](http://www.midica.org/tutorial.html)
- [Chapter 1](http://www.midica.org/tutorial-1.html)
- [Chapter 2](http://www.midica.org/tutorial-2.html)
- [Chapter 3](http://www.midica.org/tutorial-3.html)
- [Chapter 4](http://www.midica.org/tutorial-4.html)

An example of a complete song can be found in the
[examples directory](examples/). Maybe there will be more in the future.

In this Readme one short example shall be enough:

	// initialize channel 0 and 1
	INSTRUMENTS
	    0  ACOUSTIC_GRAND_PIANO  Piano (Right Hand)
	    1  ACOUSTIC_GRAND_PIANO  Piano (Left Hand)
	END
	
	*  key    d/min             // key signature
	*  time   3/4               // time signature
	*  tempo  170               // tempo in beats per minute
	
	( q=2                       // outer block to be repeated twice
	    ( q=3                   // inner block to be repeated 3 times
	        1  d-2  /8  v=70    // channel 1: play D2 as an 8th note, using mezzo piano
	        0  d    /8  v=70    // channel 0: play D4 and switch to mezzo piano
	        0  -    /8          // play a rest
	        0  d    /8
	        0  d#   /8
	        0  d    /8
	        0  d#   /8
	        *                   // synchronize: bring all channels to the same time
	    )
	    1  d-2  /4
	    1  f-2  /4
	    1  c-2  /4
	    0  d    /8
	    0  -    /8
	    0  d    /8
	    0  d#   /8
	    0  f    /8
	    0  d#   /8
	)

This results in a MIDI sequence like this:

<img src="img/example-score.svg" title="Example Score">

# Contributing

If you want to contribute, please check the [Readme file for Developers](https://github.com/truj/midica/blob/master/build_helper/README.md).