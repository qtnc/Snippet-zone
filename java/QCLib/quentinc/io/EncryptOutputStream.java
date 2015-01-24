package quentinc.io;
import java.security.*;
import java.io.*;
import javax.crypto.*;
import javax.crypto.spec.*;

public class EncryptOutputStream extends FilterOutputStream {
public EncryptOutputStream (OutputStream out, byte[] key, String algo) {
super(new CipherOutputStream(out, initCipher(key, algo)));
}

private static Cipher initCipher (byte[] key, String algo) {
try {
Cipher c = Cipher.getInstance(algo);
c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, algo));
return c;
} 
catch (InvalidKeyException e) { }
catch (NoSuchAlgorithmException e) { }
catch (NoSuchPaddingException e) { }
return null;
}

}