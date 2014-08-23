package quentinc.audio.spi;
import javax.sound.sampled.*;
import javax.sound.sampled.spi.*;
import java.io.*;
import java.net.URL;
public abstract class AbstractAudioFileReader extends AudioFileReader {
public abstract AudioFileFormat.Type getType () ;
public abstract AudioInputStream getAudioInputStream (InputStream in) throws IOException, UnsupportedAudioFileException ;

public AudioInputStream getAudioInputStream (File f) throws IOException, UnsupportedAudioFileException {
InputStream bis = null;
try {
bis = new BufferedInputStream(new FileInputStream(f));
return getAudioInputStream(bis);
} 
catch (UnsupportedAudioFileException e) { if (bis!=null) bis.close(); throw e; }
catch (IOException e) { if (bis!=null) bis.close(); throw e; }
}
public AudioInputStream getAudioInputStream (URL u) throws IOException, UnsupportedAudioFileException {
InputStream bis = null;
try {
bis = new BufferedInputStream(u.openStream());
return getAudioInputStream(bis);
}
catch (UnsupportedAudioFileException e) { if (bis!=null) bis.close(); throw e; }
catch (IOException e) { if (bis!=null) bis.close(); throw e; }
}

public AudioFileFormat getAudioFileFormat (AudioInputStream in) {
return new AudioFileFormat(getType(), in.getFormat(), (int)in.getFrameLength(), in.getFormat().properties());
}
public AudioFileFormat getAudioFileFormat (InputStream in) throws IOException, UnsupportedAudioFileException {
AudioInputStream in3 = getAudioInputStream(in);
AudioFileFormat aff = getAudioFileFormat(in3);
in3.close();
return aff;
}
public AudioFileFormat getAudioFileFormat (File f) throws IOException, UnsupportedAudioFileException {
AudioInputStream in3 = getAudioInputStream(f);
AudioFileFormat aff = getAudioFileFormat(in3);
in3.close();
return aff;
}
public AudioFileFormat getAudioFileFormat (URL u) throws IOException, UnsupportedAudioFileException {
AudioInputStream in3 = getAudioInputStream(u);
AudioFileFormat aff = getAudioFileFormat(in3);
in3.close();
return aff;
}

}