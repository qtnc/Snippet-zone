package quentinc.misc;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import javax.sound.sampled.*;

public class AudioStreamProgress extends AudioInputStream {
ProgressMonitor monitor;
int byteLen = 0;
int count = 0;
int prc = 0;

public AudioStreamProgress (AudioInputStream parent, AudioFormat format, long length,
String msg, Component comp) 
{
super(parent, format, length);
byteLen = (int)(length * format.getFrameSize());
monitor = new ProgressMonitor(comp, msg, "0%", 0, byteLen);
}
public int read () throws IOException {
count++;
update();
return super.read();
}
public int read (byte[] buf, int of, int len) throws IOException {
int n = super.read(buf,of,len);
count += n;
update();
return n;
}
public int read (byte buf[]) throws IOException {
return read(buf,0,buf.length);
}

public void update () throws IOException {
if (monitor.isCanceled()) throw new IOException("Operation canceled by user");
int nprc = (int)(100.0f * count / byteLen);
if (nprc>prc) {
monitor.setProgress(count);
prc = nprc;
monitor.setNote(prc + "%");
}
}
}