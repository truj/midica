# Introduction
Midica is a MIDI composing assistant with a focus on music programming.  
It features it's own programming language for music: MidicaPL.

So you can use the same techniques that "real" programmers use, like:
- Copy & Paste
- Macros
- Modules for code re-use
- Code Comments

You write your songs in plain text files using the text editor of your choice. Midica transforms these files into MIDI.

# Usage
- Install Java Runtume Environment (JRE) version 1.7 or higher.
- Download the file `midica-VERSION.jar`.
- Start Midica using the command: `java -jar midica-VERSION.jar`

# Features of the Midica Application itself
- **Loading and playing** MIDI sequences from:
    - MidicaPL files
    - MIDI files
- **Integrated MIDI player** featuring:
    - Regulation of volume, tempo and pitch transposition
    - Memorizing a position in the MIDI stream and jumping back to that position
    - Channel Overview (showing current instrument and channel activity)
    - Channel Detail viewer (Note name and number, volume and tick, channel by channel)
    - Channel configuration (Volume, Mute and Solo configurable channel by channel)
    - Quick reloading and reparsing of a loaded MidicaPL script
- **Karaoke Player**
    - Integrated as a part of the MIDI player
    - displaying syllables in italic, shortly before they must be sung
    - displaying syllables in a different color, when they must be sung
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
    - Syntax (2 different sets of key words for the programming language) - not _that_ important as the syntax can be redefined anyway in the music source code itself
    - Percussion shortcuts (English or German)
    - Instrument shortcuts (English or German)

# Features of Midica's MIDI programming language

Not yet documented.

