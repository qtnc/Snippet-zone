package quentinc.midi;
import javax.sound.midi.*;

public class MidiRecorder implements Receiver {
Sequence seq;
Track track;
long time;
double mpq;
int latency, adj;


public MidiRecorder (int adj, int l, int b) {
latency = l;
this.adj = adj;
setBPM(b);
try {
seq = new Sequence(Sequence.PPQ, adj);
} catch (InvalidMidiDataException imde) {}
track = seq.createTrack();
}
public int getBPM () { 
return (int)(60000 / mpq);
}
public void setBPM (int bpm) {
mpq = 60000.0 / bpm;
}
public Sequence getSequence () { return seq; }
public void clear () {
while (track.size()>0) track.remove(track.get(track.size() -1));
}
public void stop () { time = -1; }
public void start () {
time = System.currentTimeMillis() -latency;
}
public void close () {}
public void send (MidiMessage msg, long when) {
if (time<=0) return;
when = System.currentTimeMillis() -time;
double tick = when / mpq;
track.add(new MidiEvent(msg, (long)Math.round(tick*adj) ));
}

}
