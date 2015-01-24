package quentinc.util.collections;
import java.util.*;
public class ArrayIterator<E> extends IterableIterator<E> implements ListIterator<E> {
private E[] t;
private int min, max, pos;

public ArrayIterator (E[] e) { this(e,0); }
public ArrayIterator (E[] e, int n) { this(e,n,e.length); }
public ArrayIterator (E[] e, int m, int M) { this(e,m,M,-1); }
public ArrayIterator (E[] e, int min2, int max2, int pos2) {
t=e;
min=min2;
max=max2;
pos=pos2;
}
public boolean hasNext () { return ++pos < max; }
public boolean hasPrevious () { return --pos >= min; }
public E next () { return t[pos]; }
public E previous () { return t[pos]; }
public int nextIndex () { return pos; }
public int previousIndex () { return pos; }
public void set (E e) { t[pos]=e; }
public void add (E e) { throw new UnsupportedOperationException(); }
}
