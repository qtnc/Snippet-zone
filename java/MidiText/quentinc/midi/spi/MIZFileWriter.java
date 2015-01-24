package quentinc.midi.spi;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import javax.sound.midi.*;
import javax.sound.midi.spi.*;
import com.sun.media.sound.*;

public class MIZFileWriter extends MidiFileWriter {
public static final int MIZ_TYPE = 126;

public boolean isFileTypeSupported (int n) { return n==MIZ_TYPE; }
public boolean isFileTypeSupported (int n, Sequence s) { return isFileTypeSupported(n); }
public int[] getMidiFileTypes () { return new int[]{MIZ_TYPE}; }
public int[] getMidiFileTypes (Sequence s) { return getMidiFileTypes(); }
public int write (Sequence s, int t, File f) throws IOException {
OutputStream out = new BufferedOutputStream(new FileOutputStream(f));
int n = write(s,t,out);
out.flush();
out.close();
return n;
}
public int write (Sequence s, int t, OutputStream out) throws IOException {
GZIPOutputStream g = new GZIPOutputStream(out);
int n = (new StandardMidiFileWriter()).write(s, 1, g);
g.finish();
g.flush();
g.close();
return n;
}

}