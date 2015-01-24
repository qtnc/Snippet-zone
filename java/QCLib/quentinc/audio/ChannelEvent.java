package quentinc.audio;
/**
A class representing an event occured to a specific channel. There is currently only one event supported.
*/
public class ChannelEvent {
/**
Event type representing the fact that a channel has reached its end (i.e. a sound has just finished playing)
*/
public static final int END_REACHED = 1;

private final int type;
private final Channel chan;
/**
Construct a new ChannelEvent
*/
public ChannelEvent (Channel ch, int t) {
chan=ch;
type=t;
}
/**
Get the type of event which has occured
*/
public int getType () { return type; }
/**
Get the channel where the event has occured
*/
public Channel getChannel () { return chan; }
}