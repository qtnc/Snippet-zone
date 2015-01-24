package quentinc.audio;
import java.util.concurrent.*;
import java.util.*;

/**
A channel group can contain one or more channels. It allows to manage them together more easily.
*/
public class ChannelGroup extends Channel {
protected Set<Channel> channels = Collections.newSetFromMap( new ConcurrentHashMap<Channel,Boolean>(3) );


protected ChannelGroup () { this(null,null); }
protected ChannelGroup (AudioManager mgr) { this(mgr,null); }
protected ChannelGroup (AudioManager mgr, ChannelGroup p) {
super(mgr,p);
}
/**
Set the volume of the group. The real volume of each individual channel is proportionally changed.
*/
public ChannelGroup setVolume (float f) {
int oldvol = vol;
super.setVolume(f);
for (Channel c : channels) c.vol = c.vol * vol / oldvol;
return this;
}
protected int mix (short[] buf, int start, int end) {
if (closed) return 0;
if (paused) return end-start;

short[] dstbuf = buf;
if (dsps!=null) {
if (altbuf==null) altbuf = new short[end];
Arrays.fill(altbuf,(short)0);
dstbuf = altbuf;
}

Iterator<Channel> it = channels.iterator();
while (it.hasNext()) {
Channel c = it.next();
if (c.mix(dstbuf, start, end) <end) {
it.remove();
c.close();
}}

if (dsps!=null) {
int re;
Iterator<DSP> dspi = dsps.iterator();
while (dspi.hasNext()) {
re = dspi.next().process(this, altbuf, start, end);
if (re<=0) dspi.remove();
}
for (int i=start; i<end; i++) buf[i] += altbuf[i];
}


return end-start;
}

/**
Close this group and all channels contained in it.
*/
public void close () {
if (closed) return;
for (Channel c : channels) c.close();
channels.clear();
if (parent!=null) parent.channels.remove(this);
super.close();
}
/**
Play an audio clip.
*/
public Channel play (Sound s) {
Channel ch = s.open(this);
if (ch!=null) channels.add(ch.setVolume(1));
return ch;
}
/**
Loop an audio clip. convenience method for play(c).setLoop(true);
*/
public Channel loop (Sound s) {
Channel ch = play(s);
if (ch!=null) ch.setLoop(true);
return ch;
}
/**
Play sequentially each audio clip given one after another.
*/
public Channel play (Sound... s) {
return new ChainChannel(mgr, this, s);
}
/**
Create a new ChannelGroup which is owned by this group.
*/
public ChannelGroup createGroup () {
ChannelGroup g = new ChannelGroup(mgr, this);
channels.add(g);
return g;
}
protected Channel update3D () {
for (Channel ch : channels) ch.update3D();
return this;
}
/**
Overriden because the playback position cannot be set for multiple channels at once.
*/
@Override public ChannelGroup setPosition (float f) {
throw new UnsupportedOperationException("A group channel is not seekable");
}
/**
Overriden because the playback position cannot be set for multiple channels at once.
*/
@Override public ChannelGroup setPositionInFrames (int n) {
throw new UnsupportedOperationException("A group channel is not seekable");
}
/**
Set panning for all channels owned by this group.
*/
@Override public ChannelGroup setPan (float f) {
for (Channel c : channels) c.setPan(f);
return this;
}
/**
Set pitch for all channels owned by this goup
*/
public ChannelGroup setPitch (float f) {
for (Channel c : channels) c.setPitch(f);
return this;
}

}
