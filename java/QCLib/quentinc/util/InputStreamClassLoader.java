package quentinc.util;
import java.io.*;

public class InputStreamClassLoader extends ClassLoader {
byte[] data;
public InputStreamClassLoader (InputStream in) throws IOException { 
load(in);
}
public InputStreamClassLoader (File file) throws IOException {
InputStream in = new BufferedInputStream(new FileInputStream(file));
load(in);
in.close();
}
public InputStreamClassLoader (java.net.URL u) throws IOException {
InputStream in = new BufferedInputStream(u.openStream());
load(in);
in.close();
}
protected void load (InputStream in) throws IOException {
ByteArrayOutputStream out = new ByteArrayOutputStream();
quentinc.io.FileUtils.copy(in, out);
data = out.toByteArray();
}
@Override public Class findClass (String name) throws ClassNotFoundException {
try {
Class c = defineClass(null, data, 0, data.length);
data=null;
resolveClass(c);
return c;
} catch (ClassFormatError cfe) { throw new ClassNotFoundException(name); }
}
}
