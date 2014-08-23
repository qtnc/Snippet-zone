package quentinc.audio;
/**
A sample is enttirely loaded and stay in memory. It can be played more than once in the same time, and it is fast started. It is designed for short sounds which have to be played often.
*/
public class Sample extends Sound {
protected Object resource;
protected short[] data;
protected int totalLength, length, origin, limit, sampleRate;
protected boolean stereo;
protected float referenceDistance = 1;
protected float referenceVolume = 1;

public int getDurationInFrames () { return totalLength; }
public float getDuration () { return 1.0f * totalLength / sampleRate; }
/**
GEt the reference distance for the sample.
*/
public float getReferenceDistance () { return referenceDistance; }
/**
Set the reference distance for the sample. This method allow to set it once for all channels playing this sample.
*/
public Sample setReferenceDistance (float f) { referenceDistance=f; return this; }
/**
Get the reference volume of the sample
*/
public float getReferenceVolume () { return referenceVolume; }
/**
Set the reference volume of this sample. When this sample is played, the channel volume is automatically multiplied by this value.
*/
public Sample setReferenceVolume (float f) { referenceVolume = f; return this; }


protected Sample (AudioManager m, Object res, short[] dt, int len, int tlen, int lim, int sr, boolean st) {
super(m);
resource = res;
data = dt;
length = len;
totalLength = tlen;
limit = lim;
sampleRate = sr;
stereo = st;
}
public void close () {
data = null;
length=limit=0;
}


/**
Get the sample rate of this sample
*/
public int getSampleRate () { return sampleRate; }
/**
Tell if this sample is stereo or not
*/
public boolean isStereo () { return stereo; }
/**
Get the sample data
*/
public short[] getData () { return data; }
/**
Apply a DSP to the sample. Do not use with streams.
*/
public Sample applyDSP (DSP dsp) {
return applyDSP(dsp, 0, data.length);
}
/** 
Apply a DSP to a particular region of the sample. Do not use with streams.
*/
public Sample applyDSP (DSP dsp, int from, int to) {
dsp.process(null, data, from, to);
return this;
}


protected Channel open (ChannelGroup g) {
return new SampleChannel(g, this).setReferenceDistance(referenceDistance);
}


}