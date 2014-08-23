package quentinc.audio;
import java.util.*;
import static quentinc.audio.Channel.*;

class SampleChannel extends Channel {
protected int pos=0, posf=0;
protected Sample sample;

protected SampleChannel (ChannelGroup p, Sample s) {
super(s.mgr, p);
sample = s;
rate = defrate = SHIFTVAL * s.sampleRate / s.mgr.sampleRate;
}
public int getPositionInFrames () { return pos; }
public SampleChannel setPositionInFrames (int n) {
if (n>=0 && n<sample.limit) pos=n;
return this;
}
public float getPosition () {
return 1.0f * pos / sample.sampleRate;
}
public SampleChannel setPosition (float f) {
return setPositionInFrames((int)( f * sample.sampleRate ));
}
public int getDurationInFrames () { return sample.totalLength; }
public float getDuration () { return sample.getDuration(); }

public SampleChannel setVolume (float f) {
super.setVolume(f * sample.referenceVolume) ;
return this;
}
public float getVolume () {
return super.getVolume() / sample.referenceVolume;
}


protected boolean mixend () {
dispatchEvent(new ChannelEvent(this, ChannelEvent.END_REACHED));
if (loop) pos -= sample.length;
return loop;
}
protected int mix (short[] buf, int start, int end) {
if (closed) return 0;
if (paused) return end-start;
if (virtual) {
posf += (end-start) * rate;
pos += (posf>>SHIFT);
posf&=SHIFTMASK;
if (pos>=sample.totalLength) return (mixend()? end: 0);
else if (pos>=sample.limit) pos = Math.max(0, sample.limit -1 -(rate>>SHIFT));
return end-start;
}

short[] dstbuf = buf;
if (dsps!=null) {
if (altbuf==null) altbuf = new short[end];
Arrays.fill(altbuf,(short)0);
dstbuf = altbuf;
}

long l, r;
for (int i=start; i<end; i++) {
if (sample.stereo) {
l = 1L * vol * (SHIFTVAL -pan) * (
( (SHIFTVAL-posf) * sample.data[ (pos % sample.length)<<1 ] )
+ ( posf * sample.data[ ((pos+1) % sample.length)<<1 ] )
);//
r = 1L * vol * pan * (
((SHIFTVAL-posf) * sample.data[ ((pos % sample.length)<<1) +1 ])
+ (posf * sample.data[ (((pos+1) % sample.length)<<1) +1 ])
);//
}
else {
r = 1L * vol * (
((SHIFTVAL-posf) * sample.data[ pos % sample.length ])
+ (posf * sample.data[ (pos+1) % sample.length ])
);//
l = r * (SHIFTVAL -pan);
r *= pan;
}
dstbuf[i] += (short)(l >> TOTALSHIFT);
dstbuf[++i] += (short)(r >> TOTALSHIFT);
posf += rate;
pos += (posf>>SHIFT);
posf&=SHIFTMASK;
if (pos>=sample.totalLength) {
if (!mixend()) return i;
}
else if (pos>=sample.limit) {
//System.out.printf("Limit! pos=%d, limit=%d%n", pos, sample.limit);
pos = Math.max(0, sample.limit -1 -(rate>>SHIFT));
return end-start;
}
} //end main mixing

if (dsps!=null) {
Iterator<DSP> dspi = dsps.iterator();
int re;
while (dspi.hasNext()) {
re = dspi.next().process(this, altbuf, start, end);
if (re<=0) dspi.remove();
}
for (int i=start; i<end; i++) buf[i] += altbuf[i];
}
return end-start;
}

}