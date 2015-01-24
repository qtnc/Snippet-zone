package quentinc.util;
import java.io.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.net.URL;


public class Hash {
private Hash () {}

public static byte[] hash (byte[] input, String algo) { 
try {
return MessageDigest.getInstance(algo).digest(input); 
} catch (NoSuchAlgorithmException e) {}
return null;
}

public static byte[] hash (InputStream in, String algo) throws IOException   { 
try {
MessageDigest md = MessageDigest.getInstance(algo);
byte[] buf = new byte[1024]; int n=0;
while ((n = in.read(buf,0,buf.length))>0) md.update(buf,0,n);
return md.digest();
} catch (NoSuchAlgorithmException e) {}
return null;
}

public static byte[] hash (File file, String algo) {
try {
InputStream in = new BufferedInputStream(new FileInputStream(file));
byte[] b = hash(in, algo);
in.close();
return b;
} catch (IOException e) {}
return null;
}

public static byte[] hash (URL url, String algo) {
try {
InputStream in = new BufferedInputStream(url.openStream());
byte[] b = hash(in, algo);
in.close();
return b;
} catch (IOException e) {}
return null;
}

public static byte[] mac (byte[] input, byte[] key, String algo) { 
try {
Mac m = Mac.getInstance(algo);
m.init(new SecretKeySpec(key, algo));
return m.doFinal(input);
} catch (NoSuchAlgorithmException e) {
} catch (InvalidKeyException e) {}
return null;
}

public static byte[] mac (InputStream in, byte[] key, String algo) throws IOException   { 
try {
Mac m = Mac.getInstance(algo);
m.init(new SecretKeySpec(key, algo));
byte[] buf = new byte[1024]; int n=0;
while ((n = in.read(buf,0,buf.length))>0) m.update(buf,0,n);
return m.doFinal();
} catch (NoSuchAlgorithmException e) {
} catch (InvalidKeyException e) {}
return null;
}


public static byte[] mac  (File file, byte[] key, String algo) {
try {
InputStream in = new BufferedInputStream(new FileInputStream(file));
byte[] b = mac(in, key, algo);
in.close();
return b;
} catch (IOException e) {}
return null;
}

public static byte[] mac (URL url, byte[] key, String algo) {
try {
InputStream in = new BufferedInputStream(url.openStream());
byte[] b = mac(in, key, algo);
in.close();
return b;
} catch (IOException e) {}
return null;
}


}
