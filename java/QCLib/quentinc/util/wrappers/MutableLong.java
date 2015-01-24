package quentinc.util.wrappers;
public final class MutableLong extends Number {
private long n;
public MutableLong  (long x) { set(x); }
public MutableLong () { this(0); }
public synchronized void set (long x) { n=x; }
public long get () { return n; }
public int intValue () { return (int)n; }
public long longValue () { return (long)n; }
public short shortValue () { return (short)n; }
public byte byteValue () { return (byte)n; }
public float floatValue () { return (float)n; }
public double doubleValue () { return (double)n; }
}