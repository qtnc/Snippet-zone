package quentinc.midi;
import java.util.regex.*;
import java.io.*;
import java.util.*;
import com.sun.media.sound.*;
import javax.sound.midi.*;
import javax.sound.sampled.*;

public class MIDIManager implements Receiver, MetaEventListener {
private static class SoundbankItem  {
private Soundbank bank;
private List<Patch> patchs = null;
private boolean exclude = true;

public SoundbankItem (Soundbank s) { setSoundbank(s); }
public Soundbank getSoundbank () { return bank; }
public void setSoundbank (Soundbank b) { bank = b; }
public void setExcludedList (ArrayList<Patch> p) {
exclude = true;
patchs=p;
}
public void setIncludedList (ArrayList<Patch> p) {
exclude = false;
patchs=p;
}
public boolean isInList (Patch p) {
if (patchs==null) return exclude;
return exclude!=contains(p);
}
public Patch[] computeIntersection (Patch patchlist[]) {
if (patchs==null && !exclude) return new Patch[0];
ArrayList<Patch> l = new ArrayList<Patch>();
for (Patch p : patchlist) {
if (exclude && patchs!=null && contains(p)) continue;
if (!exclude  && patchs!=null && !contains(p)) continue;
if (bank.getInstrument(p)!=null) l.add(p);
}
return l.toArray(new Patch[0]);
}
public boolean contains (Patch p) {
for (Patch p1 : patchs) {
if ((p1 instanceof ModelPatch) && (p instanceof ModelPatch) && ((ModelPatch)p1).isPercussion()!=((ModelPatch)p).isPercussion()) continue;
if (p1.getBank()==p.getBank() && p1.getProgram()==p.getProgram()) return true;
}
return false;
}
}


Thread singleNoteOnThread = null;
List<SoundbankItem> banks = new ArrayList<SoundbankItem>();
SoftSynthesizer synth;
Receiver recv;
Sequencer seq;
SourceDataLine line;
FloatControl volctrl;
boolean newSequence = false;

public MIDIManager (AudioFormat format, int bufferSize, Map<String,Object> paramMap, InputStream banksInfo) throws Exception {
boolean open  = paramMap==null || paramMap.get("don't open")==null;
if (open) {
DataLine.Info info = new DataLine.Info(SourceDataLine.class, format, bufferSize);
line = (SourceDataLine)AudioSystem.getLine(info);
line.open();
volctrl = (FloatControl)line.getControl(FloatControl.Type.MASTER_GAIN);
synth = new SoftSynthesizer();
synth.open(line, paramMap);
recv = synth.getReceiver();
line.start();
seq = MidiSystem.getSequencer(false);
seq.open();
seq.addMetaEventListener(this);
seq.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
seq.getTransmitter().setReceiver(recv);
}
System.out.println("Open midi = " + open);
BufferedReader br = new BufferedReader(new InputStreamReader(banksInfo));
String li = "";
while ((li=br.readLine())!=null)  {
if (li.length()<=0 || li.startsWith("#")) continue;
processSoundbankItem(li);
}
br.close();
}


private void processSoundbankItem (String str) {
if (str.equals("bank default")) {
banks.add(new SoundbankItem(synth.getDefaultSoundbank()));
return;
}
else if (str.equalsIgnoreCase("bank emergency")) {
try {
banks.add(new SoundbankItem(EmergencySoundbank.createSoundbank()));
} catch (Exception e) {}
return;
}
else if (str.startsWith("bank ")) {
str = str.substring(5).trim();
Soundbank b = null;
try {
System.out.println("Loading soundbank " + str + "...");
b = MidiSystem.getSoundbank(new File(str));
} catch (Exception e) { e.printStackTrace(); }
if (b!=null) banks.add(new SoundbankItem(b));
return;
}


if (banks.size()<=0) return;
SoundbankItem item = banks.get(banks.size() -1);

if (str.equals("exclude all")) item.setIncludedList(null);
else if (str.equals("include all")) item.setExcludedList(null);
else if (str.startsWith("exclude") || str.startsWith("include")) {
ArrayList<Patch> list = new ArrayList<Patch>();
Matcher m = Pattern.compile("\"(.*?)\"").matcher(str);
StringBuffer sb = new StringBuffer();
while (m.find()) m.appendReplacement(sb, m.group(1).replace(' ', '\u00A0'));
sb=m.appendTail(sb);
str = sb.toString(); sb=null; m=null;

boolean first = true;
for (String s : str.split(" ")) {
if (first) { first=false; continue; }
s=s.replace('\u00A0', ' ');
Patch[] p = string2patch(item.getSoundbank(), s, null);
if (p!=null) {
for (Patch z : p) list.add(z);
}}
if (str.startsWith("include")) item.setIncludedList(list);
else item.setExcludedList(list);
}
else if (str.startsWith("remap")) {
Matcher m = Pattern.compile("\"(.*?)\"").matcher(str);
StringBuffer sb = new StringBuffer();
while (m.find()) m.appendReplacement(sb, m.group(1).replace(' ', '\u00A0'));
sb=m.appendTail(sb);
str = sb.toString(); sb=null; m=null;
String t[] = str.split(" ");
for (int k=1;k<t.length; k+=2) {
Patch[] p1 = string2patch(item.getSoundbank(), t[k], null), p2 = string2patch(null, t[k+1], p1);
if (p1!=null && p2!=null && p1.length==p2.length) {
for (int i=0; i < p1.length; i++) {
Instrument ins = item.getSoundbank().getInstrument(p1[i]);
if (ins==null) continue;
//System.out.println("remap " + p1[i].getBank() + "," + p1[i].getProgram() + " to " + p2[i].getBank() + "," + p2[i].getProgram() + " from soundbank " + item.getSoundbank().getName());
if (ins instanceof SF2Instrument) ((SF2Instrument)ins).setPatch(p2[i]);
else if (ins instanceof DLSInstrument) ((DLSInstrument)ins).setPatch(p2[i]);
else if (ins instanceof SimpleInstrument) ((SimpleInstrument)ins).setPatch(p2[i]);
}}}}
}



private Patch[] string2patch (Soundbank sbk, String s, Patch p[]) {
if (s.matches("^\\d+\\s*,\\s*\\d+$")) {
int k = s.indexOf(",");
int bank = Integer.parseInt(s.substring(0,k).trim()), program = Integer.parseInt(s.substring(k+1).trim());
return new Patch[]{new ModelPatch(bank&0x3FFF, program&0x7F, bank>=16384)};
}

String PAT = "\\d+|\\*|\\d+\\-\\d+|\\*[-\\+]\\d+";
Matcher m = Pattern.compile("^(" + PAT + ")\\s*,\\s*(" + PAT + ")$").matcher(s);
if (m.find()) {
String bs = m.group(1), ps = m.group(2);
int bank = -2, program = -2, bankAd=0, programAd=0, bankInterval = 1, programInterval = 1;
if (bs.matches("^\\d+$")) bank = Integer.parseInt(bs);
else if (bs.equals("*")) bank = -1;
else if (bs.matches("^\\d+\\-\\d+$")) {
int k = bs.indexOf("-");
bank = Integer.parseInt(bs.substring(0,k));
bankInterval = Integer.parseInt(bs.substring(k+1)) -bank +1;
}
else { bank = -1; bankAd = Integer.parseInt(bs.substring(2).trim()) * (bs.charAt(1)=='+'? 1: -1); }
if (ps.matches("^\\d+$")) program = Integer.parseInt(ps);
else if (ps.equals("*")) program = -1;
else if (ps.matches("^\\d+\\-\\d+$")) {
int k = ps.indexOf("-");
program = Integer.parseInt(ps.substring(0,k));
programInterval = Integer.parseInt(ps.substring(k+1)) -program +1;
}
else { program = -1; programAd = Integer.parseInt(ps.substring(2).trim()) * (ps.charAt(1)=='+'? 1: -1); }
if (bank<-1 || program<-1) return new Patch[0];
if (p==null && bankAd!=0) return new Patch[0];
if (p==null && programAd!=0) return new Patch[0];
if (p!=null && bankInterval>1) return new Patch[0];
if (p!=null && programInterval>1) return new Patch[0];

if (p!=null) {
Patch p2[] = new Patch[p.length];
for (int i=0; i < p.length; i++) {
Patch p1 = p[i];
int b1 = p1.getBank();
int pr1 = p1.getProgram();
if (bank == -1) b1 += bankAd;
else b1 = bank;
if (program == -1) pr1 += programAd;
else pr1 = program;
p1 = new ModelPatch(b1&0x3FFF, pr1, b1>=16384);
p2[i] = p1;
}
return p2;
}
else if (sbk!=null) {
ArrayList<Patch> l = new ArrayList<Patch>();
for (Instrument ins : sbk.getInstruments()) {
Patch p1 = ins.getPatch();
int b1 = p1.getBank(), pr1 = p1.getProgram();
if ((p1 instanceof ModelPatch) && (((ModelPatch)p1).isPercussion())) b1 += 16384;
if (
(bank == -1 || (b1>=bank && b1<bank+bankInterval))
&& (program == -1 || (pr1>=program && pr1<program+programInterval))
) l.add(p1);
}
return l.toArray(new Patch[0]);
}
return new Patch[0];
}
return null;
}

public float getVolume () { return volctrl.getValue(); }
public void setVolume (float f) { volctrl.setValue(Math.max(volctrl.getMinimum(), Math.min(f, volctrl.getMaximum()))); }
public Receiver getReceiver () { return recv; }
public void send (MidiMessage m, long t) { recv.send(m,t); }
public void send (MidiMessage m) { send(m,-1); }
public void send (int cmd, int ch, int d1, int d2) {
try {
ShortMessage s = new ShortMessage();
s.setMessage(cmd,ch,d1,d2);
send(s,-1);
} catch (InvalidMidiDataException e) { e.printStackTrace(); }
}
public void send (int cmd, int ch, int d1) { send(cmd,ch,d1,0); }
public SoftSynthesizer getSynth () { return synth; }
public SourceDataLine getLine () { return line; }
public Sequencer getSequencer () { return seq; }
public Sequence getSequence () { return seq.getSequence(); }
public void stop () { seq.stop(); }
public void setSequence (Sequence s) {
if (isRunning()) stop();
try {
seq.setSequence(s);
newSequence=true;
} catch (InvalidMidiDataException imde) { imde.printStackTrace(); }
}
public void start () {
if (newSequence) prepareStart();
seq.start();
}
public void setLoopStartPoint (long n) { seq.setLoopStartPoint(n); }
public void setLoopEndPoint (long n) { seq.setLoopEndPoint(n); }
public void setLoopCount (int n) { seq.setLoopCount(n); }
public boolean isRunning () { return seq.isRunning(); }
public long getTickPosition () { return seq.getTickPosition(); }
public void setTickPosition (long n) { seq.setTickPosition(n); }
public long getMicrosecondPosition () { return seq.getMicrosecondPosition(); }
public void setMicrosecondPosition (long l) { seq.setMicrosecondPosition(l); }
public float getTempoFactor () { return seq.getTempoFactor(); }
public void setTempoFactor (float f) { seq.setTempoFactor(f); }
public void setDefaultTempoFactor () { setTempoFactor(1.0f); }
public float getTempoInBPM () { return seq.getTempoInBPM(); }
public Instrument getInstrument (Patch p) { 
for (SoundbankItem sbi : banks) {
if (sbi.isInList(p)) {
Instrument i = sbi.getSoundbank().getInstrument(p);
if (i!=null) return i;
}}

int bk = p.getBank(), prg = p.getProgram(), bk1 = bk&0x7F, bk2 = (bk>>7)&0x7F, bk3 = (bk>>14)&0x7F;
boolean perc = ((p instanceof ModelPatch) && ((ModelPatch)p).isPercussion());

if (bk1!=0 && bk2!=0) return getInstrument(new ModelPatch(bk2<<7, prg, perc));
else if (bk1==0 && bk2!=0) return getInstrument(new ModelPatch(0, prg, perc));
else if (bk1==0 && bk2==0 && prg%8!=0) return getInstrument(new ModelPatch(0, prg&(~7), perc));
else if (bk1==0 && bk2==0 && prg!=0) return getInstrument(new ModelPatch(0, 0, perc));
else return null;
}
public Instrument[] getInstrument (Patch[] patchs) {
List<Instrument> l = new ArrayList<Instrument>();
for (Patch p : patchs) {
Instrument i = getInstrument(p);
if (i!=null) l.add(i);
}
return l.toArray(new Instrument[0]);
}
private void preparePatchs (Patch[] p) {
Instrument[] ins = getInstrument(p);
for (Instrument i : ins) {
if (i!=null) synth.loadInstrument(i);
}}
private void prepareStart () {
for (int i=0; i < 16; i++) send(0xB0, i, 121, 127);
Sequence sq = seq.getSequence();
Patch patchs[] = sq.getPatchList();
if (patchs==null || patchs.length==0) patchs = analysePatchList(sq);
preparePatchs(patchs);
newSequence = false;
}
public Instrument[] getInstrumentList () {
List<Instrument> inslist = new ArrayList<Instrument>();
for (SoundbankItem sbi : banks) {
for (Instrument i : sbi.getSoundbank().getInstruments()) {
Patch p = i.getPatch();
if (sbi.isInList(p)) inslist.add(i);
}}
Instrument _insts[] = inslist.toArray(new Instrument[0]);
Arrays.sort(_insts, new ModelInstrumentComparator());
return _insts;
}
public void singleNoteOn (final Patch p, final int note, final int duration) {
preparePatchs(new Patch[]{p});
if (singleNoteOnThread!=null) singleNoteOnThread.interrupt();
singleNoteOnThread = new Thread(new Runnable(){
public void run () {
Thread t = Thread.currentThread();
if (t.isInterrupted()) return;
try {
t.sleep(15);
} catch (InterruptedException e) { return; }
int bank = p.getBank(), program = p.getProgram();
if ((p instanceof ModelPatch) && ((ModelPatch)p).isPercussion()) { bank=16256; }
send(176, 15, 0, bank>>7);
send(176, 15, 32, bank&0x7F);
send(192, 15, program);
send(144, 15, note, 127);
try {
Thread.currentThread().sleep(duration);
} catch (InterruptedException e) {}
send(128, 15, note, 0);
}});
singleNoteOnThread.start();
}
public void close () {
try {
if (isRunning()) stop();
line.stop();
seq.close();
recv.close();
synth.close();
line.close();
} catch (Exception e) { e.printStackTrace(); }
}
public Map<String,Object> openSynthStream (Map<String,Object> map) throws Exception {
Sequence s = (Sequence)map.get("sequence");
AudioSynthesizer synth2 = new SoftSynthesizer();
AudioFormat format2 = (AudioFormat)map.get("format");
AudioInputStream stream2 = synth2.openStream(format2, map);

Patch[] patchs = s.getPatchList();
for (SoundbankItem sbi : banks)  {
for (Patch p : patchs) {
if (sbi.isInList(p)) {
Instrument i = sbi.getSoundbank().getInstrument(p);
if (i!=null) {
synth.unloadInstrument(i); // to save memory
synth2.loadInstrument(i);
}}}}

long len = BufferedSequencer.sendAll(synth2.getReceiver(), s);
len = (long)(stream2.getFormat().getFrameRate() * len /1000000);

map.put("stream", stream2);
map.put("streamlength", len);
return map;
}

public void meta (MetaMessage m) {
int type = m.getType();
byte[] b = m.getData();

switch (type) {
case 6 : // marker
String str = null;
try {
str = new String(b, 0, b.length, "iso-8859-1");
} catch (UnsupportedEncodingException uee) {}
if (str==null) break;
if (str.equalsIgnoreCase("loopstart") || str.equalsIgnoreCase("loop start")) setLoopStartPoint(getTickPosition());
else if (str.equalsIgnoreCase("loopend") || str.equalsIgnoreCase("loop end")) setLoopEndPoint(getTickPosition());
break;

default : break;
// other meta messages to be intercepted
}}

public Patch[] analysePatchList (Sequence s) {
Collection<Patch> l = new HashSet<Patch>();
int[] bk1 = new int[60], bk2 = new int[60];
for (int i=0; i < bk1.length; i++) bk1[i]=bk2[i]=0;
for (Track t : s.getTracks()) {
for (int i=0, n=t.size(); i<n; i++) {
MidiMessage m = t.get(i).getMessage();
if (!(m instanceof ShortMessage)) continue;
ShortMessage sm = (ShortMessage)m;
int cmd = sm.getCommand()&0xF0;
if (cmd==0xC0) {
int prg = sm.getData1();
int ch = sm.getChannel();
int bk = bk1[ch] + (bk2[ch]<<7);
boolean perc =  (ch==9 || bk==16256);
if (bk==16256) bk=0;
Patch p = new ModelPatch(bk, prg, perc);
if (!l.contains(p)) l.add(p);
}
else if (cmd==0xB0) {
int d1 = sm.getData1();
if (d1==0) bk2[sm.getChannel()] = sm.getData2();
else if (d1==32) bk1[sm.getChannel()]=sm.getData2();
}
}}
return l.toArray(new Patch[0]);
}

}