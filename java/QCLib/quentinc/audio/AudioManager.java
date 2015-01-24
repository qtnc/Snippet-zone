package quentinc.audio;
import java.io.*;
import javax.sound.sampled.*;
import java.util.*;
import java.nio.*;
import quentinc.geom.Vector;
import quentinc.geom.Matrix;

/**
Main class of the system. Provide basic services related to the API.
*/
public class AudioManager extends ChannelGroup implements Runnable {
protected static final int STREAMBUF = 8192, SAMPLEBUF = 12288;
protected static AudioManager current = null;
protected int sampleRate = 44100;
protected float speedOfSound = 340; 
protected ShortBuffer sbuf;
protected ByteBuffer bbuf;
protected short[] gbuf;
protected Vector listenerUp = new Vector(0,0,1), normale;
protected Matrix projectionMatrix;
protected DistanceModel distanceModel = DistanceModel.DEFAULT;
protected SpatializationModel  spatModel = SpatializationModel .DEFAULT;
protected Thread thr;
protected SourceDataLine line;

/** Get the default audio manager. The default audio manager is the first created if none has been set explicitely. */
public static AudioManager getDefault () { return current; }
/** Set the default audio manager explicitely. */
public static void setDefault (AudioManager m) { current=m; }
/**
Default constructor. Initialize a new AudioManager on system's default sound card, with a default rendering sample rate of 44100Hz (Standard CD quality) and a default internal buffer size of 27ms.
*/
public AudioManager () throws LineUnavailableException {
this(44100, 0.027f);
}
/**
Create a new AudioManager on system's default sound card with specified rendering sample rate and internal buffer size expressed in miliseconds.
*/
public AudioManager (int sampleRate, float bufferMs) throws LineUnavailableException {
this((Mixer.Info)null, sampleRate, bufferMs);
}
/**
Create a new AudioManager on the specified mixer, with sample rate and buffer size given.
*/
public AudioManager (Mixer.Info mixerInfo, int sampleRate, float bufferMs) throws LineUnavailableException {
this(AudioSystem.getMixer(mixerInfo), sampleRate, bufferMs);
}
/**
Create a new AudioManager on the specified mixer, with sample rate and buffer size given.
*/
public AudioManager (Mixer mixer, int sampleRate, float bufferMs) throws LineUnavailableException {
if (current==null) current = this;
this.pos3d = new Vector(0,0,0);
this.fwd = new Vector(0,1,0);
this.sampleRate = sampleRate;
int bufsize = bufferMs>0? (int)(bufferMs * sampleRate * 0.004f) :AudioSystem.NOT_SPECIFIED;
AudioFormat fmt = new AudioFormat(sampleRate, 16, 2, true, false);
line = (SourceDataLine)mixer.getLine(new DataLine.Info(SourceDataLine.class, fmt, bufsize));
if (line==null) throw new LineUnavailableException();
line.open(fmt, bufsize);
sampleRate = (int)(line.getFormat().getSampleRate());
bufsize = line.getBufferSize();
double bufms = bufsize / (sampleRate * 0.004);
//System.out.println("Sample rate: " + sampleRate);
//System.out.println("Buffer size: " + bufsize + " (" + bufms + " ms)");
bufsize = 384;
gbuf = new short[ bufsize ];
bbuf = ByteBuffer.allocate( 2*bufsize ).order(ByteOrder.LITTLE_ENDIAN);
sbuf = bbuf.asShortBuffer();
start();
}
/**
Get the position of the listener in the world space
*/
public Vector getListenerPosition () { return pos3d; }
/**
Get the orientation of the listener.
*/
public Vector getListenerOrientation () { return fwd; }
/**
Get the listener velocity
*/
public Vector getListenerVelocity () { return vel; }
/**
Get the up vector.
*/
public Vector getListenerUpwards () { return listenerUp; }
/**
Set the listener position
*/
public AudioManager setListenerPosition (Vector v) { pos3d=v; return this; }
/**
Set the listener orientation
*/
public AudioManager setListenerOrientation (Vector v) { return setOrientation(v); }
/**
Set the listener orientation
*/
public AudioManager setOrientation (Vector v) { 
fwd=v; 
normale = null;
return this;
}
/**
Set the listener velocity
*/
public AudioManager setListenerVelocity (Vector v) { vel = v; return this; }
/**
Set the up vector
*/
public AudioManager setListenerUpwards (Vector v) { 
listenerUp = v; 
normale = null;
return this;
}
/**
Set the listener position, orientation and up in a single call.
*/
public AudioManager setListenerAttributes (Vector pos, Vector fw, Vector up) {
setListenerPosition(pos);
setListenerOrientation(fw);
setListenerUpwards(up);
return this;
}
/**
Set the distance model, the algorythm used to calculate the volume of a playing sound corresponding to a given distance to the listener.
*/
public AudioManager setDistanceModel (DistanceModel dm) { distanceModel=dm; return this; }
/**
Get the distance model
*/
public DistanceModel getDistanceModel () { return distanceModel; }
/**
Set the spatialisation model, the algorythm used to calculate the panning of a playing sound corresponding to a given angle.
*/
public AudioManager setSpatializationModel (SpatializationModel  m) { spatModel=m; return this; }
/**
Get the speed of sound expressed in units per second
*/
public float getSpeedOfSound () { return speedOfSound; }
/**
Set the speed of sound expressed in units per seconds. Default is 340 m/s, which is realistic in the air
*/
public AudioManager setSpeedOfSound (float f) { speedOfSound = f; return this; }
/**
Get the spatialisation model
*/
public SpatializationModel  getSpatializationModel  () { return spatModel; }
/**
Actually update volume and panning of the currently playing sounds corresponding to the current 3D state. Should be called once per frame.
*/
public AudioManager update3D () {
if (pos3d==null || fwd==null || listenerUp==null) return this;
if (normale==null) {
normale = fwd.crossProduct(listenerUp);
Matrix m1 = new Matrix(new double[][]{ { normale.getX(), fwd.getX() }, { normale.getY(), fwd.getY() }, { normale.getZ(), fwd.getZ() } });
projectionMatrix = m1.multiply(m1.transpose());
}
super.update3D();
return this;
}
/**
Run method inherited from Runnable interface. 
*/
public void run () {
line.start();
while (thr!=null) {
Arrays.fill(gbuf,(short)0);
if (mix(gbuf, 0, gbuf.length)<gbuf.length) thr=null;
sbuf.clear();
sbuf.put(gbuf);
line.write(bbuf.array(), 0, bbuf.capacity() );
}
line.stop();
}
/**
Close the audio manager and release all associated resources
*/
public void close () {
super.close();
Thread t = thr;
thr=null;
try { t.join(); } catch (InterruptedException e) {}
line.close();
line = null;
gbuf = null;
}
/**
Start or restart main audio thread and audio playback.
*/
public void start () {
if (thr!=null) return;
thr = new Thread(this, "Audio thread");
thr.start();
}
/**
Pause main audio thread and audio playback.
*/
public void stop () {
thr = null;
}
/**
Load a sample from a file
*/
public Sample loadSample (File f) throws IOException, UnsupportedAudioFileException {
return loadSampleImpl(f, getAudioInputStream(f, SAMPLEBUF));
}
/**
Load a sample from an URL
*/
public Sample loadSample (java.net.URL u) throws IOException, UnsupportedAudioFileException {
return loadSampleImpl(u, getAudioInputStream(u, SAMPLEBUF));
}
/**
Load a sample from an InputStream
*/
public Sample loadSample (InputStream in) throws IOException, UnsupportedAudioFileException {
return loadSampleImpl(null, getAudioInputStream(in, SAMPLEBUF));
}
/**
Load a sample from an InputStream, with format and length given
*/
public Sample loadSample (InputStream in, AudioFormat fmt, int length) throws IOException, UnsupportedAudioFileException {
return loadSampleImpl(null, getAudioInputStream(new AudioInputStream(in, fmt, length), SAMPLEBUF));
}
/**
Load a sample from raw data, with sample rate and mono/stereo mode given.
*/
public Sample loadSample (short[] data, int sampleRate, boolean stereo) {
int len = data.length >> (stereo? 1 : 0);
return new Sample(this, null, data, len, len, len, sampleRate, stereo);
}
/**
Load a stream from a file
*/
public Stream loadStream (File f) throws IOException, UnsupportedAudioFileException {
return loadStreamImpl(f, getAudioInputStream(f, STREAMBUF));
}
/**
Load a stream from an URL
*/
public Stream loadStream (java.net.URL f) throws IOException, UnsupportedAudioFileException {
return loadStreamImpl(f, getAudioInputStream(f, STREAMBUF));
}
/**
Load a stream from an InputStreamFactory
*/
public Stream loadStream (InputStreamFactory isf) throws IOException, UnsupportedAudioFileException {
return loadStreamImpl(isf, getAudioInputStream(isf.createInputStream(), STREAMBUF));
}
/**
Load a stream from an InputStream
*/
public Stream loadStream (InputStream in) throws IOException, UnsupportedAudioFileException {
return loadStreamImpl(null, getAudioInputStream(in, STREAMBUF));
}
/**
Load a stream from an InputStream, with format given.
*/
public Stream loadStream (InputStream in, AudioFormat fmt) throws IOException, UnsupportedAudioFileException {
return loadStream(in, fmt, AudioSystem.NOT_SPECIFIED);
}
/**
Load a stream from an InputStream, with format and length given.
*/
public Stream loadStream (InputStream in, AudioFormat fmt, int length) throws IOException, UnsupportedAudioFileException {
return loadStreamImpl(null, getAudioInputStream(new AudioInputStream(in, fmt, length), STREAMBUF));
}


protected Stream loadStreamImpl (Object res, S16AudioInputStream strm) throws IOException {
return new Stream(this, res, strm, STREAMBUF );
}
protected Sample loadSampleImpl (Object res, S16AudioInputStream in) throws IOException {
boolean st = in.getFormat().getChannels()==2;
int sr = (int)in.getFormat().getSampleRate();
int len = in.getLength() * in.getFormat().getChannels();
short[] data = null;

if (len<=0) {
int n=0, r=0;
data = new short[ 4096 ];
while ((r = in.read(data, n, data.length-n))>0) {
n+=r;
if (n>=data.length -16) {
short[] tmp = new short[ (2*(data.length *3/2 +2))/2 ];
System.arraycopy(data, 0, tmp, 0, data.length);
data = tmp;
}}
len = n;
}
else {
data = new short[len +4];
int n=0, r=0;
while (n<len && (r = in.read(data, n, len-n))>0) n+=r;
}
in.close();
if (st) len>>=1;
return new Sample(this, res, data, len, len, len, sr, st);
}
protected S16AudioInputStream getAudioInputStream (Object o, int bsz) throws IOException, UnsupportedAudioFileException {
if (o instanceof File) return getAudioInputStream((File)o, bsz);
else if (o instanceof java.net.URL) return getAudioInputStream((java.net.URL)o, bsz);
else if (o instanceof AudioInputStream) return getAudioInputStream((AudioInputStream)o, bsz);
else if (o instanceof InputStream) return getAudioInputStream((InputStream)o, bsz);
else if (o instanceof InputStreamFactory) return getAudioInputStream(((InputStreamFactory)o).createInputStream(), bsz);
else if (o instanceof String) {
String s = (String)o;
if (s.startsWith("file:") || s.startsWith("jar:") || s.startsWith("zip:") || s.startsWith("http://") || s.startsWith("ftp://") || s.startsWith("https://")) return getAudioInputStream(new java.net.URL(s), bsz);
else return getAudioInputStream (new File(s), bsz);
}
else throw new ClassCastException("Object couldn't be cast to a compatible type to retriev AudioInputStream from. Class="+o.getClass().getName()+", object="+o);
}
protected S16AudioInputStream getAudioInputStream (InputStream in, int bsz) throws IOException, UnsupportedAudioFileException {
return getAudioInputStream(AudioSystem.getAudioInputStream(in), bsz);
}
protected S16AudioInputStream getAudioInputStream (java.net.URL u, int bsz) throws IOException, UnsupportedAudioFileException {
return getAudioInputStream(AudioSystem.getAudioInputStream(u), bsz);
}
protected S16AudioInputStream getAudioInputStream (File f, int bsz) throws IOException, UnsupportedAudioFileException {
return getAudioInputStream(AudioSystem.getAudioInputStream(f), bsz);
}
protected S16AudioInputStream getAudioInputStream (AudioInputStream in, int bsz) throws UnsupportedAudioFileException  {
if (in==null) throw new UnsupportedAudioFileException("Null AudioInputStream given");
if (!in.getFormat().getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED)) {
AudioFormat f0 = in.getFormat(),
f1 = new AudioFormat(f0.getSampleRate(), 16, f0.getChannels(), true, f0.isBigEndian()),
f2 = new AudioFormat(f0.getSampleRate(), 8, f0.getChannels(), true, f0.isBigEndian());

if (AudioSystem.isConversionSupported(f1, f0)) in = AudioSystem.getAudioInputStream(f1, in);
else if (AudioSystem.isConversionSupported(f2,f0)) in = AudioSystem.getAudioInputStream(f2, in);
else throw new UnsupportedAudioFileException(f0.toString());
}
return new S16AudioInputStream(in, bsz);
}

}
