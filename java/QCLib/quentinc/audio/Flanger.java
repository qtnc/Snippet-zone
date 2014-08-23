package quentinc.audio;
import static quentinc.audio.Channel.*;
/**
Simple flanger effect DSP.
*/
public class Flanger extends DSP {
private AudioManager mgr;
private short[] buffer ;
private int pos;
private boolean down;
private int invol, vol, outvol, feedback, curDelay, curDelayF, delayRate, delayMin, delayMax;
private float freq, mdelay, mxdelay;

/**
Create a new flanger on default audio manager with default effect parameters.
*/
public Flanger () { this(AudioManager.getDefault()); }
/**
Create a new flanger with default effect parameters.
*/
public Flanger (AudioManager mgr) { this(mgr, 1.0f, 1.0f, 0.75f, 0.75f, 0.0008f, 0.0008f, 0.4f); }
/**
Create a new flanger with effect parameters given.
*/
public Flanger (AudioManager mgr, float invol2, float vol2, float fb2, float outvol2, float md, float dm, float rt) {
if (mgr==null) mgr = AudioManager.getDefault();
this.mgr = mgr;
invol = (int)(invol2 * SHIFTVAL);
feedback = (int)(fb2 * SHIFTVAL);
vol = (int)(vol2 * SHIFTVAL);
outvol = (int)(outvol2* SHIFTVAL);
freq = rt;
setDelay(md, md+dm);
}
/**
Set the input volume between 0 (none) to 1 (maximum)
*/
public void setInputVolume (float f) {
if (f>1) f=1;
else if (f<0) f=0;
invol = (int)(f * SHIFTVAL);
}
/**
SEt the effect intensity between 0 (none) to 0.9999 (maximum). Values greater or equals to 1 make unspecified effect.
*/
public Flanger setIntensity (float f) {
if (f>1) f=1;
else if (f<0) f=0;
vol = (int)(f * SHIFTVAL);
return this;
}
/**
Set the effect feedback intensity between 0 (none) to 0.9999  (maximum). Values equals or greater to 1 make unspecified effect.
*/
public Flanger setFeedback (float f) {
if (f>1) f=1;
else if (f<0) f=0;
feedback = (int)(f * SHIFTVAL);
return this;
}
/**
Set the mixing volume. Note that this value can be above 1, it has not to be between 0 and 1.
*/
public Flanger setMixingVolume (float f) {
if (f>1) f=1;
else if (f<0) f=0;
outvol = (int)(f * SHIFTVAL);
return this;
}
protected void setDelay (float min, float max) {
mdelay = min; mxdelay = max;
delayMin = (int)(1.0f * mgr.sampleRate * min);
delayMax = (int)(1.0f * mgr.sampleRate * max);
setFrequency(freq);
if (buffer==null || buffer.length < 2*delayMax +8) buffer = new short[ 2*delayMax +8];
if (curDelay<delayMin) { down=false; curDelay=delayMin; }
else if (curDelay>delayMax) { curDelay=delayMax; down = true; }
}
/**
SEt the variation frequency expressed in Hz.
*/
public Flanger setFrequency (float f) {
if (f<0) f*=-1;
freq = f;
delayRate = (int)(SHIFTVAL * (delayMax - delayMin) / (freq * mgr.sampleRate));
return this;
}
/**
Set the echo minimum delay expressed in seconds.
*/
public Flanger setMinimumDelay (float f) {
if (f<0) f=0;
if (mxdelay<f) mxdelay=f;
setDelay(f, mxdelay);
return this;
}
/**
SEt the echo maximum delay expressed in seconds.
*/
public Flanger setMaximumDelay (float f) {
if (f<mdelay) f=mdelay;
setDelay(mdelay, f);
return this;
}
/**
Set the difference between minimum and maximum delay.
*/
public Flanger setDelayVariation (float f) {
if (f<0) f=0;
setDelay(mdelay, mdelay+f);
return this;
}
/**
Set the average echo delay. Delay will vary between center-variation and center+variation.
*/
public Flanger setDelayCenter (float f) {
float dc = mxdelay -mdelay;
if (f<dc/2) f=dc/2;
setDelay(f-dc/2, f+dc/2);
return this;
}
/**
Get the input volume
*/
public float getInputVolume () { return 1.0f * invol / SHIFTVAL; }
/**
Get the mixing volume
*/
public float getMixingVolume () { return 1.0f * outvol / SHIFTVAL; }
/**
Get the feedback
*/
public float getFeedback () { return 1.0f * feedback / SHIFTVAL; }
/**
Get the effect intensity
*/
public float getIntensity () { return 1.0f * vol / SHIFTVAL; }
/**
Get the variation frequency.
*/
public float getFrequency () { return freq; }
/**
Get the minimum echo delay
*/
public float getMinimumDelay () { return mdelay; }
/**
Get the maximum echo delay
*/
public float getMaximumDelay () { return mxdelay; }
/**
Get the difference between minimum and maximum echo delay
*/
public float getDelayVariation () { return mxdelay-mdelay; }
/**
Get the average echo delay.
*/
public float getDelayCenter () { return (mxdelay+mdelay)/2; }

public int process (Channel ch, short[] buf, int start, int end) {
int m, n, o;
for (int i=start; i<end; i++) {
n = (
(invol * buf[i]) +
(vol * buffer[ (pos + buffer.length -2*curDelay) %buffer.length ] )
)>>SHIFT;
m = (
(n * feedback) +
(buf[i] * (SHIFTVAL -feedback))
)>>SHIFT;
o = (
(n * outvol) +
(buf[i] * (SHIFTVAL -outvol)) 
)>>SHIFT;
buf[i] = (short)o;
buffer[ (pos++) % buffer.length ] = (short)m;

if (down) {
curDelayF += delayRate;
curDelay -= (curDelayF>>SHIFT);
curDelayF &= SHIFTMASK;
if (curDelay<delayMin) down = false;
} else {
curDelayF += delayRate;
curDelay += (curDelayF>>SHIFT);
curDelayF &= SHIFTMASK;
if (curDelay >= delayMax) down = true;
}}
return end-start;
}


}