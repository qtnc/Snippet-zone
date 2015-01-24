package quentinc.audio;
/**
Abstract base for all audio filters.
*/
public abstract class AbstractDSPFilter extends DSP {
/**
Values for knobs
*/
public static final int FREQUENCY = 0x100, QUALITY = 0x101;

protected float x0l, x0r, x1l, x1r, x2l, x2r, y0l, y0r, y1l, y1r, y2l, y2r, a0, a1, a2, b0, b1, b2, q, f, g, in, mix, out;
protected AudioManager mgr;
protected AbstractDSPFilter (AudioManager m) { 
mgr=m; 
q = 1;
g = 0;
f = 0.25f;
in = 1.0f;
mix = 1.0f;
out = 1.0f;
updateCoefficiants();
}
public int process (Channel src, short[] buf, int start, int end) {
float l, r;
for (int i=start; i<end; i+=2) {
x2l = x1l; x2r = x1r;
x1l = x0l; x1r = x0r;
x0l = buf[i] / 32768.0f;
x0r = buf[i+1] / 32768.0f;
y2l = y1l; y2r = y1r;
y1l = y0l; y1r = y0r;
y0l = b0 * x0l + b1 * x1l + b2 * x2l - a1 * y1l - a2 * y2l;
y0r = b0 * x0r + b1 * x1r + b2 * x2r - a1 * y1r - a2 * y2r;
l = mix * (x0l * in + y0l * out);
r = mix * (x0r * in + y0r * out);
buf[i] = (short)(l * 32768.0f);
buf[i+1] = (short)(r * 32768.0f);
}
return end-start;
}

/**
Get the input volume
*/
public float getInputVolume () { return in; }
/**
Get the output volume
*/
public float getOutputVolume () { return out; }
/**
Get the mixing volume
*/
public float getMixingVolume () { return mix; }
/**
Set the input volume between 0 and 1.
*/
public void setInputVolume (float f) { in=f; }
/**
Set the output volume between 0 and 1
*/
public void setOutputVolume (float f) { out=f; }
/**
SEt the mixing volume. Note that this value can be above 1, it has not to be between 0 and 1.
*/
public void setMixingVolume (float f) { mix=f; }
/**
SEt the frequency of the filter (aka cut-off)
*/
public void setFrequency (float f) {
this.f = f / 44100.0f;
updateCoefficiants();
}
/**
Get the frequency of the filter
*/
public float getFrequency () {
return this.f * 44100.0f;
}
public void setGain (float f) {
g = f;
updateCoefficiants();
}
public void setQuality (float f) {
q = f;
updateCoefficiants();
}
/**
Get a knob bound to a parameter of this filter.
*/
public Knob getKnob (final int param) {
return new Knob () {
public void setValue (float f) {
switch (param) {
case FREQUENCY : setFrequency(f); break;
case QUALITY : setQuality(f); break;
}}
public float getValue () {
switch (param) {
case FREQUENCY : return getFrequency(); 
case QUALITY : return q;
default : return 0;
}}
};}



protected abstract void updateCoefficiants () ;
}
