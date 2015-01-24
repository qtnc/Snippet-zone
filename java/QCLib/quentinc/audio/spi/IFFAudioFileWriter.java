package quentinc.audio.spi;
import javax.sound.sampled.*;
import java.io.*;

public class IFFAudioFileWriter extends AbstractAudioFileWriter {
public AudioFileFormat.Type getType () { return IFFAudioFileReader.IFF; }
public boolean isSupported (AudioInputStream in) {
AudioFormat fmt = in.getFormat();
return
in.getFrameLength()>0
&& fmt.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED)
&& (fmt.getSampleSizeInBits()==8 || fmt.getSampleSizeInBits()==16)
&& fmt.getSampleRate() <65536
&& fmt.getChannels() == 1;
}
public int write (AudioInputStream in, OutputStream out3) throws IOException {
int len = (int)in.getFrameLength();
int sz = in.getFormat().getSampleSizeInBits();
int sr = (int)in.getFormat().getSampleRate();
boolean le = !in.getFormat().isBigEndian();
DataOutputStream out = new DataOutputStream(out3);

out.writeBytes("FORM");
out.writeInt(len *8/sz +36);
out.writeBytes(sz==16? "16SV" : "8SVX");
out.writeBytes("VHDR");
out.writeInt(20);
out.writeInt(len);
out.writeInt(0);
out.writeInt(0);
out.writeShort(sr);
out.writeByte(1);
out.writeByte(0);
out.writeInt(65536);
out.writeBytes("BODY");
out.writeInt(len * 8 / sz);
byte[] buf = new byte[ 4096 ];
int n;
while ((n = in.read(buf, 0, buf.length))>0) {
if (sz==16 && le) {
for (int i=0; i < n; i+=2) {
byte b = buf[i];
buf[i] = buf[i+1];
buf[i+1] = b;
}}
out.write(buf, 0, n);
}
return len*8/sz +44;
}

}