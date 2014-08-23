package quentinc.util.wrappers;
public class MutableObject<T> {
private T ob;
public MutableObject (T t) { ob=t; }
public MutableObject () { this(null); }
public T get () { return ob; }
public synchronized void set (T t) { ob=t; }
}