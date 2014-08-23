package quentinc.audio;
/**
Low frequency oscillator DSP, which is used to vary a certain parameter between a minimum and a maximum over time. Thank to knobs, there are many usable parameters.
For example, to make a tremolo effect, use this LFO DSP + a volume knob. To make a vibrato effect, use this LFO DSP + a pitch knob. To make a wah-wah effect, use an audio filter DSP already set, this LFP DSV + a frequency knob coming from the audio filter.
*/
public class LFO extends DSP {
/**
Values for knobs
*/
public static final int FREQUENCY = 0x100;

private AudioManager mgr;
private Knob source;
private float step, center, ampl, value;

/**
Create a new LFO on default audio manager, not currently bound to a knob, and with default parameters : value variation between 0 and 1, frequency 1 Hz.
*/
public LFO () { this(AudioManager.getDefault()); }
/**
Create a new LFO not currently bound to a knob, and with default parameters : value variation between 0 and 1, frequency 1 Hz.
*/
public LFO (AudioManager mgr) {
this(mgr, null, 0, 1, 1);
}
/**
Create a new LFO by specifiing all parameters.
*/
public LFO (AudioManager mgr1, Knob src, float ctr, float a, float period) {
source = src;
mgr = mgr1;
if (mgr==null) mgr = AudioManager.getDefault();
value = 0;
setCenterValue(ctr);
setValueVariation(a);
setFrequency(period);
}
/**
Set the knob to bound to.
*/
public void setKnob (Knob k) { source=k; }
/**
Get the knob which this LFO is currently bound to.
*/
public Knob getKnob () { return source; }
/**
Get a knob bound to a parameter of this LFO
*/
public Knob getKnob (final int param) {
return new Knob(){
public void setValue (float f) {
switch (param) {
case FREQUENCY : setFrequency(f); break;
}}
public float getValue () {
switch (param) {
case FREQUENCY : return getFrequency();
default : return 0;
}}
};}
/**
Set the average value of this LFO
*/
public void setCenterValue (float f) {
center = f;
}
/**
Set the value variation around center.
*/
public void setValueVariation (float f) {
ampl = f;
}
/**
Set the minimum value
*/
public void setMinimumValue (float min) {
float max = center + ampl;
setCenterValue( (min+max)/2 );
setValueVariation( max-min );
}
/**
Set the maximum value
*/
public void setMaximumValue (float max) {
float min = center - ampl;
setCenterValue( (min+max)/2 );
setValueVariation( max-min );
}
/**
Set the frequency of this LFO expressed in Hz
*/
public void setFrequency (float f) {
step = (float)( Math.PI * f / mgr.sampleRate );
}
/**
Get the minimum value
*/
public float getMinimumValue () { return center - ampl; }
/**
Get the maximum value
*/
public float getMaximumValue () { return center + ampl; }
/**
Get the average value
*/
public float getCenterValue () { return center; }
/**
Get the variation between minimum and maximum value
*/
public float getValueVariation () { return ampl; }
/**
Get the frequency of this LFO
*/
public float getFrequency () { 
return (float)( step * mgr.sampleRate / Math.PI );
}

public int process (Channel src, short[] buf, int start, int end) {
float f = (float)( center + ampl * Math.cos( value += (end-start)*step ) );
source.setValue(f);
return end-start;
}

}
