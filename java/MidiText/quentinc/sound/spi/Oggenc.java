package quentinc.sound.spi;
import org.tritonus.sampled.file.jorbis.*;
import org.tritonus.sampled.convert.jorbis.*;
import javax.sound.sampled.*;
import javax.sound.sampled.spi.*;
import java.io.*;
import java.awt.*;

public class Oggenc extends AudioFileWriter {

private static class Proc  implements Runnable {
OutputStream oggout;
AudioInputStream in;
File out;
Process oggenc;
byte[] buf;
int pos, len;
Thread thr;

public Proc (AudioInputStream in, double quality, File out) throws IOException {
this.in = in;
this.out = out;
AudioFormat format = in.getFormat();
String cmdx = "oggenc.exe -o oggenc.tmp.ogg -Q -r -B " + format.getSampleSizeInBits() + " -C " + format.getChannels() + " -R " + (int)format.getSampleRate() + "";
if (quality > 0  && quality <= 10) cmdx += " -q " + quality;
cmdx += " -";
oggenc = Runtime.getRuntime().exec(cmdx);
oggout = oggenc.getOutputStream();
thr = new Thread(this, "OGGEncoderThread");
thr.start();
}
public void run () {
buf = new byte[128];
pos = 0;
len = (int)in.getFrameLength() * in.getFormat().getChannels() * in.getFormat().getSampleSizeInBits()/8;
try {
while (pos < len) {
int n = in.read(buf, 0, buf.length);
if (n <= 0) break;
pos += n;
oggout.write(buf, 0, n);
}
oggout.flush();
oggout.close();
Thread.currentThread().sleep(5000);
oggenc.destroy();
File f = new File("oggenc.tmp.ogg");
f.renameTo(out);
} catch (Exception e) { e.printStackTrace(); }
}

}


// End proc

public Oggenc () {}
public boolean isStreamSupported (AudioInputStream stream) {
AudioFormat f = stream.getFormat();
if (!f.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED)) return false;

int ss = f.getSampleSizeInBits();
if (ss!=8 && ss!=16 && ss!=24 && ss!=32) return false;

int ch = f.getChannels();
if (ch!=1 && ch!=2) return false;

if (f.isBigEndian()) return false;

return true;
}
public AudioFileFormat.Type[] getAudioFileTypes () {
if (!System.getProperty("os.name").startsWith("Windows")) return new AudioFileFormat.Type[0];
return new AudioFileFormat.Type[]{
org.tritonus.share.sampled.AudioFileTypes.getType("Vorbis", "ogg")
};}
public AudioFileFormat.Type[] getAudioFileTypes (AudioInputStream stream) {
return (isStreamSupported(stream)? getAudioFileTypes() : new AudioFileFormat.Type[0]);
}
public boolean isFileTypeSupported (AudioFileFormat.Type t) { return t.getExtension().equalsIgnoreCase("ogg"); }
public boolean isFileTypeSupported (AudioFileFormat.Type t, AudioInputStream stream) { return isFileTypeSupported(t)&&isStreamSupported(stream); }
public int write (AudioInputStream stream, AudioFileFormat.Type type, OutputStream out) throws IOException {
throw new UnsupportedOperationException("Writing OGG file only possible to a file");
}
public int write (AudioInputStream stream, AudioFileFormat.Type type, File file) throws IOException {
if (!isStreamSupported(stream) || !isFileTypeSupported(type)) throw new IOException("Unsupported stream and/or file type.");

double quality = 6;
String qualityStr = stream.getFormat().getProperty("quality").toString();
if (qualityStr!=null) quality = Double.parseDouble(qualityStr);
Proc proc = new Proc(stream, quality, file);
return Math.max(1, (int)file.length());
}

}