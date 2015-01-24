package quentinc.audio;
/**
A slide which close automatically the channel when finished
*/
public class SlideAndClose extends Slide {
public SlideAndClose (AudioManager mgr, Knob k, float a, float b, float c) { super(mgr,k,a,b,c); }
protected int finish (Channel src) {
src.close();
return 0;
}

}
