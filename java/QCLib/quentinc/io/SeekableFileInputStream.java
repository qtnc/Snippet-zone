package quentinc.io;
import java.io.*;
import java.net.URL;

public class SeekableFileInputStream extends FileInputStream {
public SeekableFileInputStream (String s) throws FileNotFoundException { super(s); }
public SeekableFileInputStream (File f) throws FileNotFoundException { super(f); }

private int mark = 0,  limit = 0;
public int getPosition () throws IOException { return (int)getChannel().position(); }
public void setPosition (int n) throws IOException { getChannel().position(n); }
public boolean markSupported () { return true; }
public void mark (int n) { 
try {
mark=getPosition(); limit=n; 
} catch (IOException e) { throw new RuntimeException(e); }
}
public void reset () throws IOException {
int p = getPosition();
if (p > mark+limit) throw new IOException("Mark out of limit");
else setPosition(mark);
}

}