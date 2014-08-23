package quentinc.audio;
/**
An abstract base class for all DSPs.
*/
public abstract class DSP {
/** Process a chunk of audio */
public abstract int process (Channel src, short[] buf, int start, int end) ;

/** Get a knob for the specified parameter. */
public Knob getKnob (int param) { return null; }
}