package quentinc.util.wrappers;
public final class MutableFloat extends Number {
private float n;
public MutableFloat (float x) { set(x); }
public MutableFloat  () { this(0); }
public synchronized void set (float x) { n=x; }
public float get () { return n; }
public int intValue () { return (int)n; }
public long longValue () { return (long)n; }
public short shortValue () { return (short)n; }
public byte byteValue () { return (byte)n; }
public float floatValue () { return (float)n; }
public double doubleValue () { return (double)n; }
}