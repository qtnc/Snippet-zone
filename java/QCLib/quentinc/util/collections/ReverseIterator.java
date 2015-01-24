package quentinc.util.collections;
import java.util.*;
public final class ReverseIterator<T> extends IterableIterator<T> {
final ListIterator<T> l;
public ReverseIterator (ListIterator<T> l) { this.l=l; }
public ReverseIterator (List<T> l) { this(l.listIterator(l.size())); }
public boolean hasNext () { return l.hasPrevious(); }
public T next () { return l.previous(); }
public void remove () { l.remove(); }
}
