package quentinc.audio;
import javax.sound.sampled.*;
import java.io.*;
import java.nio.*;

class S16AudioInputStream {
private AudioInputStream in;
private byte[] tbuf ;
private int sz;
private boolean be;


public S16AudioInputStream (AudioInputStream in2, int bsz) { 
in = in2;
tbuf = new byte[ bsz ];
sz = in.getFormat().getSampleSizeInBits();
be = in.getFormat().isBigEndian();
}
public void close () throws IOException { in.close(); }
public AudioFormat getFormat () { return in.getFormat(); }
public int getLength () { return (int)in.getFrameLength(); }
public int read (short[] buf, int of, int len) throws IOException {
int n = in.read(tbuf, 0, Math.min(len<<1, tbuf.length));
for (int i=0, j=of; i < n; i++, j++) {
if (sz==16) {
buf[j] = (short)(
((tbuf[i]&0xFF)<<(be? 8 : 0))
| ((tbuf[++i]&0xFF)<<(be? 0 : 8))
);//end
}
else if (sz==8) buf[j] = (short)(tbuf[i] <<8);
//others
}
return n * 8 / sz;
}

public long skip  (long n) throws IOException {
if (sz==16) return in.skip(n*2)/2;
else if (sz==8) return in.skip(n);
else throw new IOException("skip not supported by S16AudioInputStream");
}

public void mark (int n) { in.mark(n); }
public void reset () throws IOException { in.reset(); }
public boolean markSupported () { return in.markSupported(); }
}
