package quentinc.audio;
/**
Base class for all audio clips. 
*/
public abstract class Sound {
protected AudioManager mgr;
protected Sound (AudioManager m) { mgr=m; }
protected abstract Channel open (ChannelGroup g) ;
/**
Get the duration of this clip in frames. -1 = not known.
*/
public abstract int getDurationInFrames () ;

/**
Get the duration of this clip in seconds. -1 = not known.
*/
public abstract float getDuration () ;
/**
Close the clip and release all associated resources.
*/
public void close ()  {}
}