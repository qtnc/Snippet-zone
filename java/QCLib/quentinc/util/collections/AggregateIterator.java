package quentinc.util.collections;
import java.util.*;
import quentinc.util.*;

public class AggregateIterator<T> extends IterableIterator<T> {
Iterator<T> it;
Iterator<Iterable<T>> superIterator;
public AggregateIterator (Iterable<Iterable<T>> a) { superIterator = a.iterator(); }
public AggregateIterator (Iterator<Iterable<T>> a) { superIterator = a; }
public AggregateIterator (IterableIterator<Iterable<T>> a) { superIterator = a; }
public boolean hasNext () {
if (it==null) {
if (superIterator.hasNext()) it = superIterator.next().iterator();
else return false;
}
if (!it.hasNext()) {
it = null;
return hasNext();
}
return true;
}
public T next () { return it.next(); }
public void remove () { it.remove(); }
}
