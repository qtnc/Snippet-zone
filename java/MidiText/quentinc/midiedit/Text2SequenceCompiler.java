package quentinc.midiedit;
import javax.sound.midi.*;

public interface Text2SequenceCompiler {
public Sequence compile (String text, int... ptrs);
}