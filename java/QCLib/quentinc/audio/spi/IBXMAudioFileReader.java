package quentinc.audio.spi;
import java.net.URL;
import java.io.*;
import ibxm.*;
import javax.sound.sampled.*;
import javax.sound.sampled.spi.*;

public class IBXMAudioFileReader extends AudioFileReader {
private static class AudioStream extends InputStream {
IBXM ibxm;
int length, position, mark, limit;
public AudioStream (Module mod) { 
ibxm = new IBXM(44100);
ibxm.set_module(mod);
length = ibxm.calculate_song_duration();
position=0;
mark = 0;
limit = Integer.MAX_VALUE;
}
public int read () { throw new UnsupportedOperationException("You must read at least 4 bytes."); }
public int read (byte buf[], int of, int len) {
int frames = Math.min(length-position, len/4);
ibxm.get_audio(buf, of, frames);
position += frames;
return frames*4;
}
public int read (byte buf[]) {
return read(buf,0,buf.length);
}
public int available () { return (length-position)*4; }
public int getFrameLength () { return length; }
public boolean markSupported () { return true; }
public void mark (int n) { mark=position; limit=n/4; }
public void reset () throws IOException {
if (position > mark+limit) throw new IOException("Reset : mark+limit has already been reached");
position = mark;
ibxm.seek(position);
}
}


private static final AudioFormat format = new AudioFormat(44100,16,2,true,false);

public AudioInputStream getAudioInputStream (InputStream stream) throws IOException, UnsupportedAudioFileException {
try {
stream.mark(2048);
Module mod = Player.load_module(stream);
AudioStream modstream = new AudioStream(mod);
return new AudioInputStream(modstream, format, modstream.getFrameLength());
} catch (IllegalArgumentException e) {  stream.reset(); throw new UnsupportedAudioFileException(e.getMessage());  }
}

public AudioInputStream getAudioInputStream (URL u) throws IOException, UnsupportedAudioFileException {
InputStream b = null;
try {
b = new BufferedInputStream(u.openStream());
AudioInputStream a = getAudioInputStream(b);
return a;
} finally { b.close(); }
}

public AudioInputStream getAudioInputStream  (File f) throws IOException, UnsupportedAudioFileException {
InputStream s = null;
try {
s = new BufferedInputStream(new FileInputStream(f));
AudioInputStream a = getAudioInputStream (s);
return a;
} finally { if (s!=null) s.close(); }
}

public AudioFileFormat getAudioFileFormat (InputStream i) { throw new UnsupportedOperationException("getAudioFileFormat : opperation not supported by this AudioFileReader"); }
public AudioFileFormat getAudioFileFormat (File f) { throw new UnsupportedOperationException("getAudioFileFormat : opperation not supported by this AudioFileReader"); }
public AudioFileFormat getAudioFileFormat (URL u) { throw new UnsupportedOperationException("getAudioFileFormat : opperation not supported by this AudioFileReader"); }
}