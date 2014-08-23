package quentinc.midi.spi;
import java.net.URL;
import java.io.*;
import com.sun.media.sound.*;
import javax.sound.midi.*;
import javax.sound.midi.spi.*;
import java.util.*;
import java.util.zip.*;

public class MIZFileReader extends MidiFileReader {
public static final int MIZ_TYPE = 126;

public Sequence  getSequence (InputStream in3) throws InvalidMidiDataException, IOException {
try {
if (in3.markSupported()) in3.mark(Integer.MAX_VALUE);
GZIPInputStream stream = new GZIPInputStream(in3);
return (new StandardMidiFileReader()).getSequence(stream);
} catch (IOException e) {
if (in3.markSupported()) in3.reset();
throw new InvalidMidiDataException(e.getMessage());
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
return new MidiFileFormat(MIZ_TYPE, s.getDivisionType(), s.getResolution(), MidiFileFormat.UNKNOWN_LENGTH, s.getMicrosecondLength(), new HashMap<String,Object>());
}
public MidiFileFormat getMidiFileFormat (File in) throws IOException, InvalidMidiDataException {
Sequence s = getSequence(in);
return new MidiFileFormat(MIZ_TYPE, s.getDivisionType(), s.getResolution(), MidiFileFormat.UNKNOWN_LENGTH, s.getMicrosecondLength(), new HashMap<String,Object>());
}
public MidiFileFormat getMidiFileFormat (URL in) throws IOException, InvalidMidiDataException {
Sequence s = getSequence(in);
return new MidiFileFormat(MIZ_TYPE, s.getDivisionType(), s.getResolution(), MidiFileFormat.UNKNOWN_LENGTH, s.getMicrosecondLength(), new HashMap<String,Object>());
}

}