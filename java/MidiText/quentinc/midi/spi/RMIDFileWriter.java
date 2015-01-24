package quentinc.midi.spi;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import javax.sound.midi.*;
import javax.sound.midi.spi.*;
import com.sun.media.sound.*;

public class RMIDFileWriter extends MidiFileWriter {
public static final int RMID_TYPE = 127;

public boolean isFileTypeSupported (int n) { return n==RMID_TYPE; }
public boolean isFileTypeSupported (int n, Sequence s) { return isFileTypeSupported(n); }
public int[] getMidiFileTypes () { return new int[]{RMID_TYPE}; }
public int[] getMidiFileTypes (Sequence s) { return getMidiFileTypes(); }
public int write (Sequence s, int t, File f) throws IOException {
OutputStream out = new BufferedOutputStream(new FileOutputStream(f));
int n = write(s,t,out);
out.flush();
out.close();
return n;
}
public int write (Sequence s, int t, OutputStream out3) throws IOException {
RIFFWriter riff = new RIFFWriter(out3, "RMID");
RIFFWriter r2 = riff.writeChunk("data");
int n = (new StandardMidiFileWriter()).write(s, 1, r2);
r2.flush();
r2.close();
riff.flush();
riff.close();
return n+20;
}

}