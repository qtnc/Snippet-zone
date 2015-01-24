package quentinc.audio;
import java.util.*;
import java.util.concurrent.*;
import quentinc.geom.Matrix;
import quentinc.geom.Vector;

/**
A channel is an handle to a currently playing sound. It allow you to change various parameters along the sound is playing : volume, panning, pitch...
*/
public abstract class Channel {
protected static final int 
SHIFT = 15,
SHIFTVAL = 1<<SHIFT,
SHIFTMASK = SHIFTVAL -1,
TOTALSHIFT = 3*SHIFT,
VIRTUAL_LIMIT = 327;
/**
Values for knobs
*/
public static final int VOLUME = 1, PAN = 2, PITCH = 3, VOLUME_IN_DB = 4, PITCH_IN_SEMITONES = 5;


protected int vol = SHIFTVAL, pan = SHIFTVAL>>1, rate = SHIFTVAL, defrate = SHIFTVAL;
protected float referenceDistance = 1;
protected boolean closed = false, loop = false, virtual = false, paused = false, hasToNotify = false;
protected ChannelGroup parent = null;
protected AudioManager mgr = null;
protected Vector pos3d = null, fwd = null, vel = null;
protected boolean pos3dChanged = false;
protected Set<DSP> dsps = null;
protected Set<ChannelListener> listeners = null;
protected short[] altbuf;

protected Channel (AudioManager m, ChannelGroup p) { 
mgr = m;
parent=p; 
vol = (parent==null? SHIFTVAL : parent.vol);
}
protected abstract int mix (short[] buf, int start, int end) ;
protected void dispatchEvent (ChannelEvent e) {
if (listeners==null) return;
for (ChannelListener l : listeners) l.channelEvent(e);
}


/**
Get the volume of the sound, between 0 (silence) to 1 (maximum)
*/
public float getVolume () { 
if (parent!=null) return 1.0f * vol / parent.vol;
else return 1.0f * vol / SHIFTVAL; 
}
/**
Get the volume in decibels, 0dB is maximum.
*/
public float getVolumeInDB () { return (float)(Math.log(getVolume())/Math.log(10.0) *10.0); }
/**
Get the panning of the sound, from -1 (left) to 1 (right). 0 is center.
*/
public float getPan () { 
return (2.0f * pan / SHIFTVAL) -1.0f; 
}
/**
Get the pitch of the sound. 1 is normal pitch, 2 is twice faster and twice higher as normal.
*/
public float getPitch () { 
return 1.0f * rate / defrate; 
}
/**
Get the pitch of the sound, expressed in semitones
*/
public float getPitchInSemitones () {
return (float)( 12 * Math.log( getPitch() ) / Math.log(2) );
}
/**
Set the volume of the sound, from 0 (silence) to 1 (maximum)
*/
public Channel setVolume (float f) {
if (f>1) f=1;
else if (f<0) f=0;
if (parent==null) vol = (int)(f * SHIFTVAL);
else vol = (int)(f * parent.vol);
if (vol<1) vol=1;
else if (vol>SHIFTVAL) vol=SHIFTVAL;
virtual = vol< VIRTUAL_LIMIT;
return this;
}
/**
Set the volume in decibels. 0dB is maximum.
*/
public Channel setVolumeInDB (float db) {
setVolume((float)Math.pow(10, db/10.0));
return this;
}
/**
Set the panning of the sound, from -1 (left) to 1 (right). 0 is center.
*/
public Channel setPan (float f) {
if (Float.isNaN(f)) f=0;
if (f<-1) f=-1;
else if (f>1) f=1;
pan = (int)(SHIFTVAL * ((f+1) * 0.5f));
return this;
}
/**
Set the pitch of the sound. 1 is normal pitch, 2 is twice faster and twice higher as normal.
*/
public Channel setPitch (float f) {
rate = (int)(Math.abs(f) * defrate);
return this;
}
/**
Set the pitch of the sound, expressed in semitones.
*/
public Channel setPitchInSemitones (float f) {
return setPitch((float)Math.pow(2, f/12.0));
}
/**
Tell either the sound is looping or not.
*/
public boolean isLoop () { return loop; }
/**
Set the looping state of the sound.
*/
public Channel setLoop (boolean b) {  loop=b;  return this;  }
/**
Tell either the channel is paused or not.
*/
public boolean isPaused () { return paused; }
/**
Set the paused state of the channel.
*/
public Channel setPaused (boolean b) { paused=b; return this; }
/**
Tell either the channel is virtual or not. A virtual channel is a channel whose sound is too low to be perceved by the listener in the 3D space. The sound of a virtual channel is partially calculated, but not mixed and not affected by DSPs, so that some CPU time is saved.
*/
public boolean isVirtual () { return virtual; }
/**
Get the parent channel, the channel who own this one.
*/
public ChannelGroup getParentChannel () { return parent; }
/**
Get the current playback position expressed in frames. -1 signifizes that it is not known.
*/
public int getPositionInFrames () { return -1; }
/**
Get the current playback position expressed in seconds. -1 signifizes that it is not known.
*/
public float getPosition () { return -1; }
/**
Set the playback position expressed in frames.
*/
public Channel setPositionInFrames (int n) { 
throw new UnsupportedOperationException("This channel is not seekable");
}
/**
Set the playback position expressed in seconds.
*/
public Channel setPosition (float f) { 
throw new UnsupportedOperationException("This channel is not seekable");
}
/**
Get the total duration of the sound expressed in seconds. -1 signifizes that it is not known.
*/
public float getDuration () { return -1; }
/**
Get the total duration of the sound expressed in frames. -1 signifize that it is not known.
*/
public int getDurationInFrames () { return -1; }
/**
Get the remaining duration til the sound finish playing, expressed in seconds.
*/
public float getRemainingDuration () {
float d = getDuration(), p = getPosition();
if (d<=0 || p<0) return -1;
else return d-p;
}
/**
Get the remaining duration til the sound finish playing, expressed in frames.
*/
public int getRemainingDurationInFrames () {
int d = getDurationInFrames(), p = getPositionInFrames();
if (p<0 || d<=0) return -1;
else return d-p;
}
/**
Tell either the channel is active, in other words if there is currently a sound playing on it.
*/
public boolean isActive () { return !closed; }
/**
Attach a DSP to this channel.
*/
public Channel addDSP (DSP dsp) { 
if (dsps==null) dsps = Collections.newSetFromMap( new ConcurrentHashMap<DSP,Boolean>(3) );
dsps.add(dsp); 
return this;
}
/**
Detach a DSP from this channel.
*/
public Channel removeDSP (DSP dsp) { 
if (dsps==null) return this;
dsps.remove(dsp); 
if (dsps.isEmpty()) {
dsps = null;
altbuf = null;
}
return this;
}
/**
Get a knob bound to a particular parameter of this channel
*/
public Knob getKnob (final int param) {
return new Knob(){
public void setValue (float f) {
switch (param) {
case VOLUME : setVolume(f); break;
case PAN : setPan(f); break;
case PITCH : setPitch(f); break;
case VOLUME_IN_DB : setVolumeInDB(f); break;
case PITCH_IN_SEMITONES : setPitchInSemitones(f); break;
default : break;
}}
public float getValue () {
switch (param) {
case VOLUME : return getVolume();
case PAN : return getPan();
case PITCH : return getPitch();
case VOLUME_IN_DB : return getVolumeInDB();
case PITCH_IN_SEMITONES : return getPitchInSemitones();
default : return 0;
}}
};}
/** Make a fade having a duration and a final volume specified */
public void fade (float duration, float finalVolume) {
addDSP(new Slide(mgr, getKnob(VOLUME), getVolume(), finalVolume, duration));
}
/** Make a fade-out effect which lasts the specified number of seconds */
public void fadeOut (float duration) {
addDSP(new SlideAndClose(mgr, getKnob(VOLUME), getVolume(), 0, duration));
}
/** Make a fade-in effect which lasts the specified number of seconds up to the specified volume */
public void fadeIn (float duration, float finalVolume) {
vol = VIRTUAL_LIMIT +1;
fade(duration, finalVolume);
}
/** Wait for the sound to complete before returning to the caller. */
public boolean waitUntilDone () {
addChannelListener(new ChannelListener(){
public void channelEvent (ChannelEvent e) {
synchronized(Channel.this) { Channel.this.notifyAll(); }
}});
synchronized(this){ try { this.wait(); } catch (InterruptedException e) { return false; }}
return true;
}
/**
Get the position of the sound in 3D world space
*/
public Vector get3DPosition () { return pos3d; }
/**
Get the sound orientation.
*/
public Vector getOrientation () { return fwd; }
/**
Get the velocity of the sound.
*/
public Vector getVelocity () { return vel; }
/**
Set the 3D position of the sound in world space
*/
public Channel set3DPosition (Vector v) { 
pos3d = v; 
pos3dChanged = true;
return this;
}
/**
Set the 3D position of the sound, relatively to the listener.
*/
public Channel setRelative3DPosition (Vector v) {
return set3DPosition(mgr.pos3d.clone().subtract(v));
}
/**
Set the 3D position of the sound, relatively to the parent channel. If the channel is a direct child of the manager, this method has the same effect as set3DRelativePosition.
*/
public Channel set3DPositionRelativeToParent (Vector v) {
return set3DPosition(parent.pos3d.clone().subtract(v));
}
/**
Set the sound orientation
*/
public Channel setOrientation (Vector v) { 
fwd=v; 
pos3dChanged = true;
return this; 
}
/**
Set the velocity
*/
public Channel setVelocity (Vector v) { 
vel=v; 
pos3dChanged = true;
return this; 
}
/**
Get the reference distance
*/
public float getReferenceDistance () { return referenceDistance; }
/**
Set the reference distance. The reference distance is the distance under which the sound's volume is halfed.
*/
public Channel setReferenceDistance (float f) { referenceDistance=f; return this; }
/**
Attach a channel listener to this channel
*/
public boolean addChannelListener (ChannelListener l) {
if (listeners==null) listeners = new HashSet<ChannelListener>();
return listeners.add(l);
}
/**
Detach a channel listener from this channel.
*/
public boolean removeChannelListener (ChannelListener l) {
if (listeners==null) return false;
return listeners.remove(l);
}
protected Channel update3D () {
if (pos3d==null || !pos3dChanged) return this;
Vector dv = pos3d.clone().subtract(mgr.pos3d), r = dv.linearTransform(mgr.projectionMatrix);
double 
distance = dv.getLength(),
angle = mgr.normale.scalarProduct(r) / (r.getLength() * mgr.normale.getLength()) ,
sVel = (vel==null?0: -dv.scalarProduct(vel) / dv.getLength() ),
lVel = (mgr.vel==null?0: -dv.scalarProduct(mgr.vel) / dv.getLength() ),
dopplerFactor = (1-(lVel/mgr.speedOfSound)) / (1-(sVel/mgr.speedOfSound));
setPan( mgr.spatModel.getPan(angle, distance ) );
setVolume( mgr.distanceModel.getVolume((float)distance, referenceDistance ));
setPitch((float)dopplerFactor);
pos3dChanged = false;
return this;
}
/**
Stop the sound being played by the channel. Stopping a channel implie closing it as well.
*/
public void stop () { close(); }
/**
Close the channel and associated resources
*/
public void close () {
if (closed) return;
if (parent!=null) parent.channels.remove(this);
parent = null;
closed = true;
}

}