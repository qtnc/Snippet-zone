package quentinc.util;
import java.math.BigInteger;

public class MathUtils {
private MathUtils () {}

public static int gcd (int a, int... t) {
BigInteger b = BigInteger.valueOf(a);
for (int n : t) b = b.gcd(BigInteger.valueOf(n));
return b.intValue();
}
public static int mod (int val, int mod) {
return (BigInteger.valueOf(val)).mod(BigInteger.valueOf(mod)).intValue();
}
public static int modInverse (int val, int mod) {
try {
return (BigInteger.valueOf(val)).modInverse(BigInteger.valueOf(mod)).intValue();
} catch (ArithmeticException e) { return -1; }
}
public static int max (int... t) {
int n = Integer.MIN_VALUE;
for (int i : t) n = Math.max(n,i);
return n;
}
public static int min (int... t) {
int n = Integer.MAX_VALUE;
for (int i : t) n = Math.min(n,i);
return n;
}

}
