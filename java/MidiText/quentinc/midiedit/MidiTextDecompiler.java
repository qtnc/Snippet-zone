package quentinc.midiedit;
import java.util.*;
import javax.sound.midi.*;


public class MidiTextDecompiler {
int resolution = 480;
StringBuilder[] sbs;
int[] ticks, bols, vols;
LinkedList<MidiEvent>  curNotesOn;
public String decompile (Sequence s) {
sbs = new StringBuilder[16];
ticks = new int[16];
bols = new int[16];
vols = new int[16];
resolution = s.getResolution();
curNotesOn = new LinkedList<MidiEvent>();
for (int i=0; i < sbs.length; i++) {
sbs[i] = new StringBuilder();
ticks[i]=0;
bols[i]=0;
vols[i]= -1;
}

List<MidiEvent> evs = new ArrayList<MidiEvent>();
Track tracks[] = s.getTracks();
for (int k=0; k < tracks.length; k++) {
for (int i=0; i < tracks[k].size(); i++) {
evs.add(tracks[k].get(i));
}}

Collections.sort(evs, new MidiEventComparator());

for (MidiEvent evx : evs) {
parseMessage(evx);
}

StringBuilder sb = new StringBuilder();
sb.append("// Decompiled by MidiText\r\n")
.append("resolution:").append(s.getResolution())
.append("\r\n\r\n");

for (int i=0; i < sbs.length; i++) {
if (sbs[i].length()>0) {
sb.append("[v:").append(i).append("]\r\n").append(sbs[i].toString()).append("\r\n\r\n");
}}
return sb.toString();
}

static final int[] PRIMES = {2, 3, 5, 7};
public static final String NOTES[] = {"c", "c#", "d", "d#", "e", "f", "f#", "g", "g#", "a", "a#", "b"};

private String getTimeStr (int dur) {
if (dur==resolution) return "";
int num = dur, denom = resolution;
int gcd = quentinc.util.MathUtils.gcd(num, denom);
denom/=gcd; num/=gcd;

if (denom==1) return String.valueOf(num);
else if (num==1) return (new StringBuilder()).append('/').append(denom).toString();
else return (new StringBuilder()).append(num).append('/').append(denom).toString();
}
private void append (int ch, int ti, String cmd) {
if (ticks[ch]!=ti) {
String s1 = getTimeStr(Math.abs(ticks[ch]-ti));
sbs[ch].append(ti>ticks[ch]? 's':'r').append(s1).append(' ');
ticks[ch] = ti;
}

sbs[ch].append(cmd);

if (ticks[ch]-bols[ch] > resolution*32) {
bols[ch]=ticks[ch];
sbs[ch].append("\r\n");
}
else sbs[ch].append(' ');
}
private MidiEvent getCorrespondingNoteOn (MidiEvent ev) {

Iterator<MidiEvent> iterator = curNotesOn.descendingIterator();
ShortMessage msg = (ShortMessage)ev.getMessage();

//System.out.println("Search corresponding note off : channel=" + msg.getChannel() + ", note=" + msg.getData1());
//System.out.println("List size : " + curNotesOn.size());


while (iterator.hasNext()) {
MidiEvent ev2 = iterator.next();
ShortMessage m = (ShortMessage)ev2.getMessage();

if (m.getChannel()==msg.getChannel() && m.getData1()==msg.getData1()) {
iterator.remove();
return ev2;
}}

//System.out.println("Not found!");
return null;
}

private void parseMessage (MidiEvent ev) {
MidiMessage msg = ev.getMessage();
int ti = (int)ev.getTick();

if (msg instanceof ShortMessage) {
ShortMessage m = (ShortMessage)msg;
int ch = m.getChannel();
int status = m.getStatus();
if ((status&0xF0) == ShortMessage.NOTE_ON && m.getData2()==0) status = ShortMessage.NOTE_OFF; // MIDI convention : note on with velocity 0 = note off
switch (status&0xF0) {
case ShortMessage.NOTE_ON : curNotesOn.add(ev); break;
case ShortMessage.NOTE_OFF :
MidiEvent onev = getCorrespondingNoteOn(ev);
if (onev==null) break;
m = (ShortMessage)onev.getMessage();
int note = m.getData1(), vel = m.getData2();
if (vols[ch]!=vel) {
vols[ch]=vel;
append(ch, ti, "v" + vel);
}

StringBuilder sb1 = new StringBuilder();
int ti1 = (int)onev.getTick();
int oct = (note/12) -5;

if (oct!=0) sb1.append(oct);
sb1.append(NOTES[note%12]);
sb1.append(getTimeStr(ti-ti1));

append(ch, ti1, sb1.toString());
ticks[ch] = ti;
break;

case ShortMessage.CONTROL_CHANGE :
int ctrl = m.getData1(), cval = m.getData2();
append(ch, ti, (new StringBuilder()).append("ctrl:").append(ctrl).append(',').append(cval).toString());
break;

case ShortMessage.PROGRAM_CHANGE :
int prog = m.getData1();
append(ch, ti, "p" + prog);
break;


case ShortMessage.PITCH_BEND :
int value = (m.getData2()<<8) + m.getData1();
append(ch, ti, "h" + value);
break;


// other short messages
}}
else if (msg instanceof MetaMessage) {
MetaMessage m = (MetaMessage)msg;
int type = m.getType();
byte b[] = m.getData();
switch (type) {
case 81 : // tempo
int mpq = ((b[0]&0xFF)<<16) | ((b[1]&0xFF)<<8) | (b[2]&0xFF);
int bpm = 60000000 / mpq;
append(0, ti, "tem" + bpm);
break;

// other meta messages

default : break;
}}
else {
// Other types of messages
}}

}