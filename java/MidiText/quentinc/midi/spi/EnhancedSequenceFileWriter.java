package quentinc.midi.spi;
import quentinc.midi.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import javax.sound.midi.*;
import javax.sound.midi.spi.*;
import com.sun.media.sound.*;

public class EnhancedSequenceFileWriter {
public void write (Sequence s, int t, OutputStream out) throws IOException {
RIFFWriter riff = new RIFFWriter(out, "EMID");
RIFFWriter riff2 ;

if (s instanceof EnhancedSequence) {
EnhancedSequence es = (EnhancedSequence)s;

Object o;
if ((o=es.getParam("title"))!=null) {
riff2 = riff.writeChunk("titl");
riff2.writeString(o.toString());
riff2.flush(); riff2.close();
}
if ((o=es.getParam("copyright"))!=null) {
riff2 = riff.writeChunk("copy");
riff2.writeString(o.toString());
riff2.flush(); riff2.close();
}

for (Soundbank b : es.getSoundbanks()) {
if (b instanceof SF2Soundbank) {
riff2 = riff.writeChunk("sf2b");
((SF2Soundbank)b).save(riff2);
riff2.flush();
riff2.close();
}
else if (b instanceof DLSSoundbank) {
riff2 = riff.writeChunk("dlsb");
((DLSSoundbank)b).save(riff2);
riff2.flush();
riff2.close();
}}

}

riff2 = riff.writeChunk("midi");
MidiSystem.write(s,t,riff2);
riff2.flush();
riff2.close();
riff.flush();
riff.close();
}

}