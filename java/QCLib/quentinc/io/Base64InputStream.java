package quentinc.io;
import java.io.*;
public class Base64InputStream extends InputStream {
private static final byte[] chars = ("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=").getBytes();
private long curInput;
private int curPos, curLen;
private InputStream in;

public Base64InputStream (InputStream s) {
in=s;
curPos= -1;
curInput=0;
}
private int indexOfByte (byte b) throws IOException {
for (int i=0; i < chars.length; i++) {
if (chars[i]==b) return i;
}
throw new IOException("Invalid base64 input");
}
private void readBlock () throws IOException {
curInput = 0;
curLen = 3;
curPos=0;
for (int i=0; i < 4; i++) {
if (i>0) curInput<<=6;
int n = in.read();
if (n!=-1) n = indexOfByte((byte)n);
if (n==-1 || n==64) {
curInput<<=(6*(3-i));
curLen = Math.max(0,i -1);
return;
}
curInput |= n;
}
}
public int read () throws IOException {
if (curPos <= -1) readBlock();
if (curLen<=0) return -1;
int n = (int)((curInput>>(8*(2-curPos)))&0xFF);
curPos++;
if (curPos>=curLen) curPos = -1;
return n;
}

public static byte[] decode (byte input[]) throws IOException {
ByteArrayInputStream bis = new ByteArrayInputStream(input);
Base64InputStream b64 = new Base64InputStream(bis);
ByteArrayOutputStream bos = new ByteArrayOutputStream();
int n = 0;
while ((n=b64.read())>=0)  bos.write(n);
return bos.toByteArray();
}

public static String decode (String s) {
try {
return new String(decode(s.getBytes("iso-8859-1")), "iso-8859-1");
} catch (Exception e) {}
return null;
}

}