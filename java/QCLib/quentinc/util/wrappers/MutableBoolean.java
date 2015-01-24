package quentinc.util.wrappers;
public class MutableBoolean {
private boolean b;
public MutableBoolean (boolean b) { this.b=b; }
public MutableBoolean () { this(false); }
public boolean get () { return b; }
public synchronized void set (boolean b) { this.b=b; }
public synchronized void invert () { b=!b; }
public synchronized void set () { b=true; }
public synchronized void clear () { b=false; }
}