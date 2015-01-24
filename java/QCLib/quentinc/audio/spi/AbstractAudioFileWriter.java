package quentinc.audio.spi;
import javax.sound.sampled.*;
import javax.sound.sampled.spi.*;
import java.io.*;

public abstract class AbstractAudioFileWriter extends AudioFileWriter {
public abstract AudioFileFormat.Type getType () ;
public abstract boolean isSupported (AudioInputStream in) ;
public abstract int write (AudioInputStream in, OutputStream out) throws IOException ;

@Override public AudioFileFormat.Type[] getAudioFileTypes () { return new AudioFileFormat.Type[]{ getType() }; }
@Override public boolean isFileTypeSupported (AudioFileFormat.Type tp) { return getType().equals(tp); }
@Override public AudioFileFormat.Type[] getAudioFileTypes (AudioInputStream in) {
return (isSupported(in)? getAudioFileTypes() : new AudioFileFormat.Type[0]);
}
@Override public boolean isFileTypeSupported (AudioFileFormat.Type tp, AudioInputStream in) { return getType().equals(tp) && isSupported(in); }
@Override public int write (AudioInputStream in, AudioFileFormat.Type type, OutputStream out) throws IOException { 
if (!isSupported(in)) throw new IOException("Format of audio stream not supported");
return write(in,out); 
}
@Override public int write (AudioInputStream in, AudioFileFormat.Type type, File f) throws IOException {
OutputStream out = null;
try {
out = new BufferedOutputStream(new FileOutputStream(f));
return write(in,out);
} catch (IOException e) { throw e; }
finally { if (out!=null) out.close(); }
}

}
