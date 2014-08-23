package quentinc.midi;
import com.sun.media.sound.*;
import javax.sound.midi.*;

public class BufferedSequencer {
private BufferedSequencer () {}
public static long sendAll (Receiver recv, Sequence seq) {
			float divtype = seq.getDivisionType();			
			Track[] tracks = seq.getTracks();
			int[] trackspos = new int[tracks.length];
			int mpq = 60000000 / 120;
System.out.printf("New MPQ value %d corresponding to %d BPM\r\n", mpq, 60000000 / mpq);
			int seqres = seq.getResolution();
			long lasttick = 0;
			long curtime = 0;
			while (true) {
				MidiEvent selevent = null;
				int seltrack = -1;
				for (int i = 0; i < tracks.length; i++) {
					int trackpos = trackspos[i];
					Track track = tracks[i];
					if (trackpos < track.size()) {
						MidiEvent event = track.get(trackpos);
						if (selevent == null
								|| event.getTick() < selevent.getTick()) {
							selevent = event;
							seltrack = i;
						}
					}
				}
				if (seltrack == -1)
					break;
				trackspos[seltrack]++;
				long tick = selevent.getTick();
				if(divtype == Sequence.PPQ)
					curtime += ((tick - lasttick) * mpq) / seqres;
				else
					curtime = (long)((tick * 1000000.0 * divtype) / seqres);
				lasttick = tick;
				MidiMessage msg = selevent.getMessage();
				if (msg instanceof MetaMessage) {
					if(divtype == Sequence.PPQ)
					if (((MetaMessage) msg).getType() == 0x51) {
						byte[] data = ((MetaMessage) msg).getData();
						mpq = ((data[0] & 0xff) << 16)
								| ((data[1] & 0xff) << 8) | (data[2] & 0xff);

System.out.printf("New MPQ value %d corresponding to %d BPM\r\n", mpq, 60000000 / mpq);
					}
				} else {
					recv.send(msg, curtime);
				}
			}

			long totallen = curtime ;
return totallen;
}}