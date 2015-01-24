package quentinc.midiedit;

import javax.sound.midi.*;

public interface SoundbankEditor {
public Soundbank getSoundbank ();
public void setSoundbank (Soundbank s);
public void addInstrument (Instrument ins);
public void removeInstrument (Instrument ins);
public Instrument getCurrentlyEditedInstrument ();
}