package quentinc.audio.spi;
import java.net.URL;
import java.io.*;
import javax.sound.sampled.*;
import javax.sound.sampled.spi.*;

public class IFFAudioFileReader extends AbstractAudioFileReader {
public static final AudioFileFormat.Type IFF = new AudioFileFormat.Type("IFF/SVX", "iff");
public AudioFileFormat.Type getType () { return IFF; }
public AudioInputStream getAudioInputStream (InputStream in2) throws IOException, UnsupportedAudioFileException {
try {
if (in2.markSupported()) in2.mark(2048);
DataInputStream in = new DataInputStream(in2);
byte[] buf = new byte[4];
if (in.read(buf, 0, buf.length)<buf.length) throw new EOFException();
if (!"FORM".equals(new String(buf,0,buf.length,"iso-8859-1"))) throw new UnsupportedAudioFileException("Not an IFF audio file");
int fileSize = in.readInt();
if (in.read(buf,0,buf.length)<buf.length) throw new EOFException();
String str = new String(buf,0,buf.length,"iso-8859-1");
int sampleSize = 0;
if (str.equals("8SVX")) sampleSize = 8;
else if (str.equals("16SV")) sampleSize = 16;
if (sampleSize<=0) throw new UnsupportedAudioFileException("Not an IFF audio file");
int sampleRate = 0, dataLength = 0;

while (true) {
if (in.read(buf,0,4)<4) throw new EOFException();
String chunkName = new String(buf,0,4,"iso-8859-1");
int chunkSize = in.readInt();
if ("BODY".equals(chunkName)) {
if (dataLength<0) dataLength = chunkSize;
break;
}
else if (!"VHDR".equals(chunkName)) { in.skip(chunkSize); continue; }

int 
shotPart = in.readInt(), 
repeatPart = in.readInt(), 
cycle = in.readInt(), 
sr = in.readUnsignedShort(), 
nOctaves = in.readUnsignedByte(),
compression = in.readUnsignedByte(),
volume = in.readInt();

sampleRate = sr;
dataLength = shotPart + repeatPart;
if (compression!=0) throw new UnsupportedAudioFileException("Only uncompressed IFF are supported");
}
if (dataLength<=0 || sampleRate<=0) throw new UnsupportedAudioFileException("Not a valid IFF audio file");

return new AudioInputStream(in, new AudioFormat(sampleRate, sampleSize, 1, true, true), dataLength);
} 
catch (IOException e) { if (in2.markSupported()) in2.reset(); throw e; }
catch (UnsupportedAudioFileException e) { if (in2.markSupported()) in2.reset(); throw e; }
}

}