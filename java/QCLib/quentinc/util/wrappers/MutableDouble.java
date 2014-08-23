package quentinc.util.wrappers;
public final class MutableDouble extends Number {
private double n;
public MutableDouble (double x) { set(x); }
public MutableDouble () { this(0); }
public synchronized void set (double x) { n=x; }
public double get () { return n; }
public int intValue () { return (int)n; }
public long longValue () { return (long)n; }
public short shortValue () { return (short)n; }
public byte byteValue () { return (byte)n; }
public float floatValue () { return (float)n; }
public double doubleValue () { return (double)n; }
}