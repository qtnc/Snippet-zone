package quentinc.io;
import java.io.*;

public class Base64OutputStream extends OutputStream {
private static final byte[] chars = ("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=").getBytes();
private OutputStream out;
private int curInput, curPos;

public Base64OutputStream (OutputStream s) { 
out=s; 
curPos=0;
curInput=0;
}

public void write (byte buf[], int of, int len) throws IOException {
for (int i=0; i < len; i++) write(buf[of+i]);
}
public void write (int n) throws IOException {
curInput <<= 8;
curInput |= (n&0xFF);
curPos++;

if (curPos >= 3) {
curPos=0;
for (int i=18; i>=0; i-=6) out.write(chars[(curInput>>i)&0x3F]);
}}
public void flush () throws IOException {
if (curPos==1) {
curInput<<=8;
out.write(chars[(curInput>>10)&0x3F]);
out.write(chars[(curInput>>4)&0x3F]);
out.write(chars[64]); out.write(chars[64]);
}
else if (curPos==2) {
curInput<<=8;
out.write(chars[(curInput>>18)&0x3F]);
out.write(chars[(curInput>>12)&0x3F]);
out.write(chars[(curInput>>6)&0x3F]);
out.write(chars[64]);
}
out.flush();
}
public void close () throws IOException {
flush();
out.close();
}


public static byte[] encode (byte[] in) {
try {
ByteArrayOutputStream bos = new ByteArrayOutputStream();
Base64OutputStream b64 = new Base64OutputStream(bos);
b64.write(in, 0, in.length);
b64.flush();
return bos.toByteArray();
} catch (Exception e) {}
return new byte[0];
}

public static String encode (String in) {
try {
return new String(encode(in.getBytes("iso-8859-1")), "iso-8859-1");
} catch (Exception e) {}
return null;
}

}