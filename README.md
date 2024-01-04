<img src="img/logo.svg" title="Midica Logo" width="300">

[Get Started](#get-started)
|
[Features](#features-of-the-midica-application-itself)
|
[Screenshots](#screenshots)
|
[Programming](#programming-with-midica)
|
[CLI](#command-line-interface)
|
[Contribute](#contributing)
|
[License](#license)

Midica is an interpreter for a Music Programming Language.
It translates source code to MIDI.

But it can also be used as a MIDI Player, MIDI compiler or decompiler, Karaoke Player, ALDA Player,
ABC Player, LilyPond Player or a MIDI File Analyzer.

You write music with one of the supported languages (MidicaPL, ALDA or ABC).  

The built-in music programming language MidicaPL supports the same techniques as regular programming languages, like:

- Variables and Constants
- Functions
- Loops
- Conditions (if/elsif/else)
- Including Libraries
- Code Comments

You write your songs in plain text files using the text editor of your choice. Midica converts these files into MIDI or plays them directly.

# Get started
1. Install Java Runtume Environment (JRE) version 1.8 or higher.
2. Go to the [latest release](https://github.com/truj/midica/releases/latest) and download the file `midica.jar`.
3. Start Midica using the command: `java -jar midica.jar` (or just by double-click if your operating system supports that)
4. Download one of the [example files](examples/) or create your own file and save it with the file extension `.midica`.
5. In the Midica application, load this file by pressing the upper right button `select file`.
6. Switch to the MidicaPL tab (it's the default tab) and choose this file.
7. Press the button `Start Player` and play the sequence

If you prefer to write your music in ALDA or ABC, you need to:

- Install [ALDA](https://github.com/alda-lang/alda) or [abcMIDI](https://ifdo.ca/~seymour/runabc/top.html)
- In Step 4: Download an [ALDA example](https://github.com/alda-lang/alda-core/tree/master/examples) or [ABC example](https://abcnotation.com/search) or create your own file with the extension `.alda` or `.abc`.
- In Step 6: Switch to the ALDA or ABC tab (and maybe adjust the program path).

# Features of the Midica Application itself

- **Loading and playing** MIDI sequences from:
    - MidicaPL files
    - MIDI files
    - ALDA files (if [ALDA](https://github.com/alda-lang/alda) is installed)
    - ABC files (if [abcMIDI](https://ifdo.ca/~seymour/runabc/top.html) is installed)
    - LilyPond files (if [LilyPond](https://lilypond.org/) is installed)
    - MusicXML, MuseScore, Guitar Pro, Capella, Bagpipe Music Writer, Sonicscores (Overture / Score Writer), PowerTab (if [MuseScore](https://musescore.org/) is installed)
- **Exporting MIDI sequences**
    - As MIDI files
    - As MidicaPL files (experimental)
    - As ALDA files (experimental)
    - As Audio files (wav, au, snd, aiff, aifc)
    - As ABC files (if [midi2abc](https://ifdo.ca/~seymour/runabc/top.html) is installed)
    - As Lilypond files (if [midi2ly](https://lilypond.org/) is installed)
    - As PDF, PNG, SVG, FLAC, OGG, MP3, MXL, MusicXML or MSCX (if [MuseScore](https://musescore.org/) is installed)
- **Integrated MIDI player** featuring:
    - Regulation of volume, tempo and pitch transposition
    - Memorizing a position in the MIDI sequence and jumping back to that position
    - Channel Overview (showing current instrument and channel activity)
    - Channel Detail viewer (Note name and number, volume and tick, channel by channel)
    - Channel configuration (Volume, Mute and Solo configurable channel by channel)
    - Quick reloading and reparsing of a loaded file
- **Karaoke Player**
    - Integrated as a part of the MIDI player
    - displaying syllables in different colors for future and past
    - displaying syllables in italic, shortly before they must be sung
- **Converter**
    - converts various formats to MIDI (MidicaPL, ALDA, ABC, LilyPond, MusicXML, MuseScore, Guitar Pro, Capella, Bagpipe, Overture, Score Writer, PowerTab)
    - converts MIDI to various formats (MidicaPL, ALDA, Audio, ABC, Lilypond, MuseScore, MusicXML and others)
- **Soundbanks**
    - Loading Soundfonts (SF2) or Downloadable Sounds (DLS) and using them for playback
      - either from an SF2 or DLS file
      - or via download (with caching, both formats work)
    - Analyzing Soundbank contents
    - Test Soundbanks with the Soundcheck window
    - Using the loaded soundbank for Audio exports
- **Configuration** of
    - GUI language (currently English or German)
    - Note System - (6 different systems), e.g. International (C,D,E,F,G,A,B), German (C,D,E,F,G,A,H), Italian (Do,Re,Mi,Fa,Sol,La,Si)
    - Half Tone Symbols (3 different sets)
    - Octave Naming - 4 different systems
    - Syntax (3 different sets of key words for the programming language) - not _that_ important as the syntax can be redefined in MidicaPL anyway
    - Percussion IDs (English or German)
    - Instrument IDs (English or German)
    - Key bindings

# Screenshots

You can find a lot of screenshots here: https://www.midica.org/screenshots.html

I will not repeat them all in this Readme. But at least here are three screenshots.
The first one shows the main window.
The second one shows the player in default mode.
The third one shows the player in Karaoke mode.

<img src="img/main.png" title="Midica Player">
<img src="img/player.png" title="Midica Player">
<img src="img/karaoke.png" title="Karaoke Mode">

# Programming with Midica

Midica has its own Music Programming Language: MidicaPL. But alternatively you can also code in ALDA or ABC.

For learning, each language has its own resources:

- [MidicaPL tutorial](https://www.midica.org/tutorial.html)
- [ALDA documentation](https://github.com/alda-lang/alda/blob/master/doc/index.md)
- [ABC tutorials](https://abcnotation.com/learn)

Here we focus on MidicaPL. For a quick reference, here are the links to the main
chapters of the MidicaPL tutorial:

- [Preparation](https://www.midica.org/tutorial.html)
- [Chapter 1: Basics](https://www.midica.org/tutorial-1.html)
- [Chapter 2: Improving](https://www.midica.org/tutorial-2.html)
- [Chapter 3: Functions](https://www.midica.org/tutorial-3.html)
- [Chapter 4: Blocks](https://www.midica.org/tutorial-4.html)
- [Chapter 5: Tweaking](https://www.midica.org/tutorial-5.html)
- [Chapter 6: Patterns](https://www.midica.org/tutorial-6.html)

Examples of complete songs can be found in the [examples directory](examples/).
In this Readme we just show some short examples to get an impression of the language.

## Example 1

This example uses only one channel and lyrics:

	// use Piano in channel 0
	INSTRUMENTS
		0  ACOUSTIC_GRAND_PIANO  Piano
	END
	
	// Every line beginning with "0:" defines events in channel 0
	0: (v=95)                   // (...=...) set an option to a value; v = velocity; 95 = forte
	0: (l=Hap) c:8.             // (l=...) defines a syllable. "c:8." = dotted eighth middle C
	0: (l=py_) c:16             // _ = space; c:16 = sixteenth middle C
	
	// More special characters in syllables:  \c = comma, \r = new line, \n = new paragraph
	0: (l=birth) d:4 (l=day_) c (l=to_) f (l=you\c\r) e:2   (l=Hap) c:8. (l=py_)   c:16
	0: (l=birth) d:4 (l=day_) c (l=to_) g (l=you.\n)  f:2   (l=Hap) c:8. (l=py...) c:16

This results in a MIDI sequence like this:

<img src="img/example-birthday.svg" title="Example Score">

## Example 2

This example uses nestable blocks and global commands:

	// initialize channel 0 and 1
	INSTRUMENTS
	    0  ACOUSTIC_GRAND_PIANO  Piano (Right Hand)
	    1  ACOUSTIC_GRAND_PIANO  Piano (Left Hand)
	END
	
	*  key    d/min            // key signature
	*  time   3/4              // time signature
	*  tempo  170              // tempo in beats per minute
	
	{ q=2                      // outer block to be repeated twice
	    { q=3                  // inner block to be repeated 3 times
	        
	        // in channel 1: switch to mezzo piano (v=70) and play D3 as an 8th note
	        1: | (v=70)  d-:4   // the pipe "|" checks for measure borders (optional)
	        
	        // in channel 0: switch to mezzo piano and play some notes and rests ("-" = rest)
	        0: | (v=70)  d:8 - d eb d eb |
	        
	        // synchronize: bring all channels to the same time
	        *
	    }
	    
	    1:  d-:4  f-  c-
	    0:  d:8   -   d   eb  f  eb
	}

This results in a MIDI sequence like this:

<img src="img/example-score.svg" title="Example Score">

Instead of the nested blocks we could have written this equivalent code:

	0: (v=70) | d:8 - d eb d eb | d - d eb d eb | d - d eb d eb | d - d eb f eb |
	0:        | d:8 - d eb d eb | d - d eb d eb | d - d eb d eb | d - d eb f eb |
	1: (v=70) | d-:4 -:/2       | d-:4 -:/2     | d-:4 -:/2     | d-:4  f-  c-  |
	1:        | d-:4 -:/2       | d-:4 -:/2     | d-:4 -:/2     | d-:4  f-  c-  |

## Example 3

This example uses a guitar picking pattern with several chords. It produces the beginning of "Dust in the wind":

	// use the guitar in channel 0
	INSTRUMENTS
		0  STEEL_GUITAR Guitar
	END
	
	// define some chords
	CHORD cmaj   c-  e- g- c
	CHORD cmaj7  c-  e- g- b-
	CHORD cadd9  c-  e- g- d
	CHORD asus2  a-2 e- a- b-
	CHORD asus4  a-2 e- a- d
	CHORD amin   a-2 e- a- c
	
	// Define the picking pattern (Travis picking)
	// The numbers inside the pattern aren't channel numbers but note indices
	PATTERN travis
	    : 0/3:4  1:8 2 0 3 1 2
	END
	
	// play the chords using this pattern
	0: | cmaj:travis  | cmaj7:travis | cadd9:travis | cmaj:travis  |
	0: | asus2:travis | asus4:travis | amin:travis  | asus2:travis |

This results in the following sequence:

<img src="img/example-dust.svg" title="Example Score">

## Example 4

This example uses functions and percussion instruments.<br>
It produces the first beats of "Another one bites the Dust":

	// Use bass in channel 5
	// Drums are always in channel 9 (automatically)
	INSTRUMENTS
	    5  E_BASS_FINGER  Bass
	END
	
	// anacrusis
	* time 1/8
	5: (d=30%) a-2:16  g-2              // d=30% --> staccato
	
	// regular bars
	* time 4/4
	CALL drum-and-bass(bar=1)
	CALL drum-and-bass(bar=2) q=2       // q=quantity, so the function will be called twice
	
	FUNCTION drum-and-bass
	    CALL bassline(${bar})
	    CALL drums  q=4                 // "drums()" is called 4 times without parameters
	END
	
	FUNCTION bassline
	    5:        e-2:4 e-2 e-2 -:8. e-2:16     e-2:8 e-2 g-2 e-2:16 a-2
	    { if $[0] == 1
	        5:    -:4+8  a-2:16 g-2
	    }
	    { else
	        5:    -:2
	    }
	END
	
	// p = 9 = percussion channel
	// hhc = hi-hat-closed, bd1 = base-drum-1, sd1 = snare-drum-1
	FUNCTION drums
	    p:   (v=127) hhc/bd1:8    (v=80) hhc   (v=127) hhc/bd1/sd1   (v=80) hhc
	END

The resulting sequence looks like this:

<img src="img/example-another.svg" title="Example Score">

# Command Line Interface

By default (without arguments) Midica is started in GUI mode.
But you can provide command line arguments:

```
java -jar midica.jar [ARGUMENTS]
```

You can see all available arguments with `--help`:

```
java -jar midica.jar --help
```

This explains which arguments are available and how they work:

```
ARGUMENTS:
--help                : Print this message.
--cli                 : Run in CLI mode (command line interface) without GUI.
                        Exits after all CLI related work is done.
--keep-alive          : Don't exit, even if --cli has been used.
                        Mainly used for unit tests.
--ignore-local-config : Doesn't use local config file. Use default config.
                        Without this argument the config is read from and
                        written into the file '.midica.conf' in the current
                        user's home directory.
--soundbank=PATH      : Use the specified soundbank file (.sf2 or .dls).
--soundbank=URL       : Use the specified soundbank URL (SF2 or DLS).
--import=PATH         : Import from the specified MidicaPL file.
--import-midi=PATH    : Import from the specified MIDI file.
--import-alda=PATH    : Import from the specified ALDA file by calling the
                        alda program. (ALDA needs to be installed.)
--import-abc=PATH     : Import from the specified ABC file by calling
                        abc2midi. (abcMIDI needs to be installed.)
--import-ly=PATH      : Import from the specified LilyPond file by calling
                        lilypond. (LilyPond needs to be installed.)
--import-mscore=PATH  : Import from the specified file using MuseScore
                        (MuseScore needs to be installed.)
--export-midi=PATH    : Export to the specified MIDI file.
--export=PATH         : Export to the specified MidicaPL file. (*)
--export-alda=PATH    : Export to the specified ALDA file. (*)
--export-audio=PATH   : Export to the specified audio file.
                        (Supported file Extensions: .wav, .au, .aif, .aiff)
--export-abc=PATH     : Export to the specified ABC file by calling
                        midi2abc. (abcMIDI needs to be installed.)
--export-ly=PATH      : Export to the specified Lilypond file by calling
                        midi2ly. (Lilypond needs to be installed.)
--export-mscore=PATH  : Export to the specified file using MuseScore.
                        (MuseScore needs to be installed.)

(*) A file is exported to STDOUT if the export PATH is a dash (-).
    E.g. --export=-
```

# Contributing

If you want to contribute, please check the [Readme file for Developers](build_helper/README.md).

# License

The main part of Midica is published under the [MPL 2.0](LICENSE) (Mozilla Public License Version 2.0).

The following third-party software is also included:

- Gervill Synthesizer by Karl Helgason, published under [GPL 2](LICENSE-gervill) (General Public License 2.0)  
  This code is located under [src/com/sun/gervill](src/com/sun/gervill)
- [MidiToAudioRenderer](src/com/sun/kh/MidiToAudioRenderer.java) (Copyright notice can be found in the file header)  
  originally written by Karl Helgason as "Midi2WavRender.java"    
  This file is located under [src/com/sun/kh](src/com/sun/kh)
- [ReferenceCountingDevice](src/com/sun/gervill/ReferenceCountingDevice.java) by  Matthias Pfisterer, published under [GPL 2](LICENSE-gervill) (General Public License 2.0)  
  This file is located under [src/com/sun/gervill](src/com/sun/gervill)
