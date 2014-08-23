package quentinc.audio;
import java.util.*;
import quentinc.geom.Vector;

class ChainChannel extends Channel implements ChannelListener {
protected int framesCumuled = 0;
protected float secondsCumuled = 0;
protected int position = -1;
protected Channel curch = null;
protected List<Sound> clips = new ArrayList<Sound>();

protected ChainChannel (AudioManager m, ChannelGroup g, Sound... cc) {
super(m,g);
for (Sound c : cc) clips.add(c);
nextItem();
}

public void channelEvent (ChannelEvent e) {
if (e.getType()==ChannelEvent.END_REACHED) nextItem();
}
@Override public ChainChannel setVolume (float f) {
super.setVolume(f);
if (curch!=null) curch.setVolume(f);
return this;
}
@Override public ChainChannel setVolumeInDB (float f) {
super.setVolumeInDB(f);
if (curch!=null) curch.setVolumeInDB(f);
return this;
}
@Override public ChainChannel setPan (float f) {
super.setPan(f);
if (curch!=null) curch.setPan(f);
return this;
}
@Override public ChainChannel setPitch (float f) {
super.setPitch(f);
if (curch!=null) curch.setPitch(f);
return this;
}
@Override public ChainChannel setPaused (boolean b) {
super.setPaused(b);
if (curch!=null) curch.setPaused(b);
return this;
}
@Override public ChainChannel set3DPosition (Vector v) {
super.set3DPosition(v);
if (curch!=null) curch.set3DPosition(v);
return this;
}
@Override public ChainChannel setReferenceDistance (float f) {
super.setReferenceDistance(f);
if (curch!=null) curch.setReferenceDistance(f);
return this;
}
@Override protected ChainChannel update3D () {
super.update3D();
if (curch!=null) curch.update3D();
return this;
}
@Override public int getPositionInFrames () {
return framesCumuled + curch.getPositionInFrames();
}
@Override public float getPosition () {
return secondsCumuled + curch.getPosition();
}
@Override public ChainChannel setPositionInFrames (int n) {
n -= framesCumuled;
if (n>curch.getDurationInFrames()) {
n -= curch.getDurationInFrames();
curch.stop();
while (++position<clips.size() && n>clips.get(position).getDurationInFrames()) n-=clips.get(position).getDurationInFrames();
if (position<clips.size()) playpos(position);
}
curch.setPositionInFrames(n);
return this;
}
@Override public ChainChannel setPosition (float n) {
n -= secondsCumuled;
if (n>curch.getDuration()) {
n -= curch.getDuration();
curch.stop();
while (++position<clips.size() && n>clips.get(position).getDuration()) n-=clips.get(position).getDuration();
if (position<clips.size()) playpos(position);
}
curch.setPosition(n);
return this;
}
@Override public ChainChannel addDSP (DSP d) {
if (curch!=null) curch.addDSP(d);
return this;
}
@Override public ChainChannel removeDSP (DSP d) {
if (curch!=null) curch.removeDSP(d);
return this;
}


protected void playpos (int p) {
position = p;
Channel c = parent.play(clips.get(position));
if (curch!=null && c!=null) {
c.setVolume(curch.getVolume());
c.setPan(curch.getPan());
c.setPitch(curch.getPitch());
c.setReferenceDistance(curch.getReferenceDistance());
c.set3DPosition(curch.get3DPosition());
c.setVelocity(curch.getVelocity());
c.setOrientation(curch.getOrientation());
c.dsps = curch.dsps;
c.update3D();
}
c.addChannelListener(this);
curch = c;
secondsCumuled = framesCumuled = 0;
for (int i=0; i < position; i++) {
framesCumuled += clips.get(i).getDurationInFrames();
secondsCumuled += clips.get(i).getDuration();
}}
protected void nextItem () {
position++;
if (position>=clips.size()) {
if (loop) position = 0;
else close();
}
if (position>=0 && position<clips.size()) playpos(position);
}
@Override public void close () {
if (curch!=null) curch.close();
curch=null;
super.close();
}



protected int mix (short[] s, int a, int b) { return b; }
}