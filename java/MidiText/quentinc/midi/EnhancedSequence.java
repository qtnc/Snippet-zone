package quentinc.midi;
import javax.sound.midi.*;
import java.util.*;

public class EnhancedSequence extends Sequence {
protected List<Patch> patchs = new ArrayList<Patch>();
protected List<Soundbank> banks = new ArrayList<Soundbank>();
protected Map<String,Object> params = new HashMap<String,Object>();

public EnhancedSequence () throws InvalidMidiDataException { super(Sequence.PPQ,480); }
public EnhancedSequence (int r) throws InvalidMidiDataException { super(Sequence.PPQ,r); }
public EnhancedSequence (float f) throws InvalidMidiDataException { super(f,480); }
public EnhancedSequence (float f, int r) throws InvalidMidiDataException { super(f,r); }
public EnhancedSequence (Sequence s) throws InvalidMidiDataException {
super(s.getDivisionType(), s.getResolution());
tracks.clear();
for (Track t : s.getTracks()) tracks.add(t);
if (s instanceof EnhancedSequence) params = ((EnhancedSequence)s).params;
}
public void addSoundbank (Soundbank b) { banks.add(b); }
public void addPatch (Patch p) { patchs.add(p); }
public void addPatchs (Patch... p) { for (Patch p1 : p) addPatch(p1); }
public void addSoundbanks (Soundbank... s) { for (Soundbank s1 : s) addSoundbank(s1); }
public void addPatchs (Iterable<Patch> p) { for (Patch p1 : p) addPatch(p1); }
public void addSoundbanks (Iterable<Soundbank> s) { for (Soundbank s1 : s) addSoundbank(s1); }
public boolean removeSoundbank (Soundbank s) { return banks.remove(s); }
public boolean removePatch (Patch p) { return patchs.remove(p); }
public Track getTrack (int n) { return tracks.get(n); }
public void putParam (String s, Object o) { params.put(s,o); }
public Object getParam (String s) { return params.get(s); }
public Patch[] getPatchList () { return patchs.toArray(new Patch[0]); }
public Soundbank[] getSoundbanks () { return banks.toArray(new Soundbank[0]); }

}