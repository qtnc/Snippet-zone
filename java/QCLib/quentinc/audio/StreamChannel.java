package quentinc.audio;
class StreamChannel extends SampleChannel implements Runnable {
protected Thread thr;

protected StreamChannel (ChannelGroup g, Stream s) {
super(g,s);
thr = new Thread(this, "Stream channel thread of " + sample.resource);
thr.start();
}
protected boolean mixend () {
dispatchEvent(new ChannelEvent(this, ChannelEvent.END_REACHED));
if (loop) ((Stream)sample).reload();
return loop;
}
public void run () {
try {
while (thr!=null && !thr.isInterrupted() && (((Stream)sample).fillBuffer() || loop))  Thread.currentThread().sleep(10);
} catch (InterruptedException e) {
} catch (Exception e) { e.printStackTrace(); }
}

public void close () {
if (thr!=null)  thr.interrupt();
Thread thr2 = thr;
thr = null;
try { thr2.join(); } catch (InterruptedException e) {}
super.close();
((Stream)sample).channel = null;
}
@Override public StreamChannel setPositionInFrames (int n) {
throw new UnsupportedOperationException("A stream channel is not seekable");
}

}