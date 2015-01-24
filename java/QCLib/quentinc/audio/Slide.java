package quentinc.audio;
/**
Slide DSP, used to change the value of a parameter gradually from a starting to an ending value. Thank to knobs, many parameters can be used for sliding.
For example, to make a fade in or a fade out, use this slide DSP + a volume knob.
*/
public class Slide extends DSP {
AudioManager mgr;
Knob source;
float start, end, value, step;
/**
Create a new slide using the given knob, starting and ending values, and slide duration.
*/
public Slide (AudioManager mgr, Knob k, float start, float end, float dur) {
if (mgr==null) mgr = AudioManager.getDefault();
this.mgr = mgr;
source = k;
value = start;
this.start = start;
this.end = end;
setDuration(dur);
}
/**
Set the starting value
*/
public void setStartValue (float f) { 
start = f;
}
/**
Set the ending value
*/
public void setEndValue (float f) {
end = f;
}
/**
SEt the slide duration in seconds
*/
public void setDuration (float f) {
step = (end - start) / (2.0f * f * mgr.sampleRate);
}
/**
Get the starting value
*/
public float getStartValue () { return start; }
/**
Get the ending value
*/
public float getEndValue () { return end; }
/**
Get the slide duration
*/
public float getDuration () { 
return (end-start) / (step * 2.0f * mgr.sampleRate);
}

public int process (Channel src, short[] buf, int begin, int finish) {
if ((end-value) * (start-end) >0) return finish(src);
source.setValue( value += step*(finish-begin)  );
return finish-begin;
}

protected int finish (Channel src) {  return 0;  }
}
