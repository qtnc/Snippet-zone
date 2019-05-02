package quentinc.io;
import java.io.*;
import java.util.*;
import java.net.URL;
import java.util.regex.*;
//import quentinc.util.*;

public class FileUtils {
private FileUtils () {}

public static String getFileContents (Reader in) throws IOException {
CharArrayWriter out = new CharArrayWriter();
char[] buf = new char[128];
int n = 0;
while ((n = in.read(buf,0,buf.length))>0) out.write(buf,0,n);
return out.toString();
}
public static String getFileContents (String fn) throws IOException {
Reader in = null;
if (fn.startsWith("http://")) in = new BufferedReader(new InputStreamReader( (new java.net.URL(fn)).openStream() ));
else in = new BufferedReader(new FileReader(fn));
String x = getFileContents(in);
in.close();
return x;
}
public static String getFileContents (URL u) throws IOException {
InputStream in = u.openStream();
String x = getFileContents(new InputStreamReader(in));
in.close();
return x;
}
public static String getFileContents (File f) throws IOException {
Reader r = new FileReader(f);
String x = getFileContents(r);
r.close();
return x;
}

public static void putFileContents (File fn, String data) throws IOException {
BufferedWriter bw = new BufferedWriter(new FileWriter(fn));
bw.write(data);
bw.flush();
bw.close();
}
public static void putFileContents (String fn, String data) throws IOException {
BufferedWriter bw = new BufferedWriter(new FileWriter(fn));
bw.write(data);
bw.flush();
bw.close();
}

public static void copy (InputStream in, OutputStream out) throws IOException {
byte[] buf = new byte[4096];
int n = 0;
while ((n = in.read(buf, 0, buf.length)) >0) out.write(buf, 0, n);
}
public static void copy (File a, File b) throws IOException {
InputStream in = new BufferedInputStream(new FileInputStream(a));
OutputStream out = new BufferedOutputStream(new FileOutputStream(b));
copy(in, out);
in.close();
out.flush();
out.close();
}
public static void copy (java.net.URL a, File b) throws IOException {
InputStream in = new BufferedInputStream( a.openStream() );
OutputStream out = new BufferedOutputStream(new FileOutputStream(b));
copy(in, out);
in.close();
out.flush();
out.close();
}

}