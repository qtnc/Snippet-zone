package quentinc.audio;
import static quentinc.audio.Channel.*;

/**
Simple echo DSP effect.
*/
public class Echo extends DSP {
private AudioManager mgr;
private short[] buffer ;
private int pos;
private int delay;
private int vol;

/**
Create a new echo DSP on default audio manager with the delay and volume given.
*/
public Echo (float delay2, float vol2) { this(AudioManager.getDefault(), delay2, vol2); }
/**
Create a new echo DSP with the delay and volume given.
*/
public Echo (AudioManager mgr, float delay2, float vol2) {
if (mgr==null) mgr = AudioManager.getDefault();
this.mgr = mgr;
setVolume(vol2);
setDelay(delay2);
}
/**
Create a new echo DSP with default effect parameters : 100ms delay, 0.5 volume.
*/
public Echo (AudioManager mgr) {
this(mgr, 0.1f, 0.5f);
}
/**
Create a new echo DSP on default audio manager with default effect parameters : 100ms delay, 0.5 volume.
*/
public Echo () { this(AudioManager.getDefault()); }
/**
Set the volume of the echo effect. 0 = no echo, 0.9999 = maximum. Values greater or equals to 1 make unspecified effects.
*/
public Echo setVolume (float f) {
vol = (int)(f * SHIFTVAL);
return this;
}
/**
Get effect volume.
*/
public float getVolume () {
return 1.0f * vol / SHIFTVAL;
}
/**
SEt echo delay expressed in seconds. Note that this method is slow because an internal buffer has to be resized.
*/
public Echo setDelay (float f) {
delay = (int)(f * 2 * mgr.sampleRate);
delay = (delay>>1)<<1;
if (buffer==null || buffer.length < delay +8) {
buffer = null;
buffer = new short[delay +8];
}
return this;
}
/**
Get the echo delay expressed in seconds
*/
public float getDelay () {
return 0.5f * delay / mgr.sampleRate;
}
public int process (Channel ch, short[] buf, int start, int end) {
for (int i=start; i<end; i++) {
buf[i] += (short)( buffer[ (pos + buffer.length -delay) %buffer.length ] * vol / SHIFTVAL);
buffer[ (pos++) % buffer.length ] = buf[i];
}
return end-start;
}


}