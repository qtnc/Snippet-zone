
package quentinc.midi;
import java.io.*;
import java.util.*;
import javax.sound.midi.*;
import com.sun.media.sound.*;

public class SF2Utils {
private SF2Utils () {}
public static void copyInstrumentInto (SF2Soundbank sf, SF2Instrument ins) {
SF2Instrument copy = new SF2Instrument(sf);
copy.setName(ins.getName());
copy.setPatch(ins.getPatch());
copy.setGlobalZone(ins.getGlobalRegion());
copy.setGenre(ins.getGenre());
copy.setMorphology(ins.getMorphology());
copy.setLibrary(ins.getLibrary());
for (SF2InstrumentRegion ir : ins.getRegions()) {
SF2InstrumentRegion irc = new SF2InstrumentRegion();
copy.getRegions().add(irc);
for (SF2Modulator m : ir.getModulators()) irc.getModulators().add(m);
for (Map.Entry<Integer,Short> m : ir.getGenerators().entrySet()) irc.putShort(m.getKey(), m.getValue());
SF2Layer l = ir.getLayer();
SF2Layer lc = new SF2Layer(sf);
lc.setName(l.getName());
lc.setGlobalZone(l.getGlobalRegion());
for (SF2LayerRegion lr : l.getRegions()) {
SF2LayerRegion lrc = new SF2LayerRegion();
lc.getRegions().add(lrc);
for (SF2Modulator m : lr.getModulators()) lrc.getModulators().add(m);
for (Map.Entry<Integer,Short> m : lr.getGenerators().entrySet()) lrc.putShort(m.getKey(), m.getValue());
SF2Sample s = lr.getSample();
SF2Sample sc = new SF2Sample(sf);
sc.setName(s.getName());
sc.setStartLoop(s.getStartLoop());
sc.setEndLoop(s.getEndLoop());
sc.setOriginalPitch(s.getOriginalPitch());
sc.setPitchCorrection(s.getPitchCorrection());
sc.setSampleLink(s.getSampleLink());
sc.setSampleRate(s.getSampleRate());
sc.setSampleType(s.getSampleType());
try {
ModelByteBuffer mbuf = s.getDataBuffer();
mbuf.load();
byte[] mbuf2 = mbuf.array();
byte[] cbuf = new byte[mbuf2.length];
System.arraycopy(mbuf2, 0, cbuf, 0, mbuf2.length);
sc.setData(cbuf);
mbuf.unload();
} catch (Exception e) { e.printStackTrace(); }
if (sc.getDataBuffer()!=null) {
sf.addResource(sc);
lrc.setSample(sc);
}}
sf.addResource(lc);
irc.setLayer(lc);
} 
sf.addResource(copy);
}
public static SF2Soundbank createBank (Instrument[] ins) {
SF2Soundbank sb = new SF2Soundbank();
for (Instrument in : ins) {
if (!(in instanceof SF2Instrument)) continue;
copyInstrumentInto(sb,(SF2Instrument)in);
}
return sb;
}

}