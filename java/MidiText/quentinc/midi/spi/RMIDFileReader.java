package quentinc.midi.spi;
import java.net.URL;
import java.io.*;
import com.sun.media.sound.*;
import javax.sound.midi.*;
import javax.sound.midi.spi.*;
import java.util.*;

public class RMIDFileReader extends MidiFileReader {
public static final int RMID_TYPE = 127;

public Sequence  getSequence (InputStream in3) throws InvalidMidiDataException, IOException {
try {
if (in3.markSupported()) in3.mark(Integer.MAX_VALUE);
RIFFReader riff = ((in3 instanceof RIFFReader)? (RIFFReader)in3 : new RIFFReader(in3));
if (!riff.getType().equalsIgnoreCase("RMID") || !riff.getFormat().equalsIgnoreCase("RIFF"))  throw new InvalidMidiDataException("Not a valid RIFF-MIDI file");

RIFFReader r2 = null;
while (riff.hasNextChunk() && !(r2=riff.nextChunk()).getFormat().equalsIgnoreCase("data")) ;
if (r2==null)  throw new InvalidMidiDataException("not a valid RIFF-MIDI file");

int size = (int)r2.getSize(), pos=0, n=0;
byte buf[] = new byte[size];
while (pos<size) {
n = r2.read(buf, pos, size - pos);
if (n<=0) break;
pos+=n;
}

ByteArrayInputStream stream = new ByteArrayInputStream(buf);

return (new StandardMidiFileReader()).getSequence(stream);

} catch (IOException e) {
if (in3.markSupported()) in3.reset();
throw e;
} catch (InvalidMidiDataException e) {
if (in3.markSupported()) in3.reset();
throw e;
}
}

public Sequence getSequence (File f) throws InvalidMidiDataException, IOException {
InputStream stream = new BufferedInputStream(new FileInputStream(f));
Sequence s = getSequence(stream);
stream.close();
return s;
}
public Sequence getSequence (URL u) throws InvalidMidiDataException, IOException {
InputStream stream = u.openStream();
Sequence s = getSequence(stream);
stream.close();
return s;
}
public MidiFileFormat getMidiFileFormat (InputStream in) throws IOException, InvalidMidiDataException {
Sequence s = getSequence(in);
return new MidiFileFormat(RMID_TYPE, s.getDivisionType(), s.getResolution(), MidiFileFormat.UNKNOWN_LENGTH, s.getMicrosecondLength(), new HashMap<String,Object>());
}
public MidiFileFormat getMidiFileFormat (File in) throws IOException, InvalidMidiDataException {
Sequence s = getSequence(in);
return new MidiFileFormat(RMID_TYPE, s.getDivisionType(), s.getResolution(), MidiFileFormat.UNKNOWN_LENGTH, s.getMicrosecondLength(), new HashMap<String,Object>());
}
public MidiFileFormat getMidiFileFormat (URL in) throws IOException, InvalidMidiDataException {
Sequence s = getSequence(in);
return new MidiFileFormat(RMID_TYPE, s.getDivisionType(), s.getResolution(), MidiFileFormat.UNKNOWN_LENGTH, s.getMicrosecondLength(), new HashMap<String,Object>());
}

}