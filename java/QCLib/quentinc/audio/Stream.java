package quentinc.audio;
import java.io.*;
import java.util.*;
/**
A stream is an audio clip which is not totally loaded on memory but read just as needed. There is less memory overhead, but more CPU time. It is designed to handle large sounds which have to be decompressed or which last for a long time.
A stream cannot be played more than once in the same time.
*/
public class Stream extends Sample {
protected S16AudioInputStream stream = null;
protected StreamChannel channel;

protected Stream (AudioManager m, Object res, S16AudioInputStream strm, int bufsize) {
super(m, res, new short[bufsize * strm.getFormat().getChannels() ], bufsize, Integer.MAX_VALUE, 1, (int)strm.getFormat().getSampleRate(), strm.getFormat().getChannels()==2);
stream = strm;
}
protected void reload () {
Arrays.fill(data,(short)0);
try {
if (stream!=null) stream.reset();
limit = 1;
if (channel!=null) channel.pos=0;
return;
} catch (IOException e) { }
try {
if (stream!=null) stream.close();
stream = mgr.getAudioInputStream(resource, AudioManager.STREAMBUF);
limit = 1;
if (channel!=null) channel.pos=0;
} catch (Exception e) { 
e.printStackTrace(); 
close();
}
}
protected StreamChannel open (ChannelGroup g) {
if (channel!=null) {
channel.close();
reload();
}
channel = new StreamChannel(g, this);
if (channel!=null) channel.setReferenceDistance(referenceDistance);
return channel;
}
protected boolean fillBuffer () throws IOException {
if (channel==null) return true;
int n, p = (channel.pos%length)*(stereo?2:1), l = (limit%length)*(stereo?2:1);
if (p<l) {
n = stream.read(data, l, data.length-l);
if (n<=0) {
totalLength = limit;
return false;
}
limit += n/(stereo?2:1);
l = limit%length * (stereo?2:1);
}
if (l<p) {
n = stream.read(data, l, p-l);
if (n<=0) {
totalLength = limit;
return false;
}
limit += n/(stereo?2:1);
}
return true;
//suite
}
protected boolean _2_fillBuffer () throws IOException {
short[] buf = data;
int n=1, start;
if (channel.pos>origin) {
start =  (limit-origin) * (stereo? 2 : 1);
n = stream.read(buf, start, buf.length-start);
if (n<=0) {
totalLength = limit;
return false;
}
limit += (n / (stereo? 2 : 1));

}
if (limit-origin < length) return true;
start = (channel.pos -origin) * (stereo? 2 : 1);
origin += length;
if (start<=0) return true;
//System.out.printf("Read 2 : origin=%d, start=%d, pos=%d, length=%d%n", origin, start, channel.pos, length);
n = stream.read(buf, 0, start);
if (n<=0) {
totalLength = limit;
return false;
}
limit += (n / (stereo? 2 : 1));
return true;
}





public void close () {
super.close();
if (channel!=null) channel.close();
}

}