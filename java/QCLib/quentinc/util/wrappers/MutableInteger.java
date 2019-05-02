package quentinc.util.wrappers;
public final class MutableInteger extends Number {
private int n;
public MutableInteger (int x) { set(x); }
public MutableInteger () { this(0); }
public synchronized void set (int x) { n=x; }
public int get () { return n; }
public int intValue () { return (int)n; }
public long longValue () { return (long)n; }
public short shortValue () { return (short)n; }
public byte byteValue () { return (byte)n; }
public float floatValue () { return (float)n; }
public double doubleValue () { return (double)n; }
}