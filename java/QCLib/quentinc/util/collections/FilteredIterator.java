package quentinc.util.collections;
import quentinc.util.*;
import java.util.*;
public class FilteredIterator<T> extends IterableIterator<T>  {
private Iterator<T> it;
private Predicate<T> predicate;
private T current;

public FilteredIterator (IterableIterator<T> it, Predicate<T> p) { this.it=it; predicate=p; }
public FilteredIterator (Iterator<T> it, Predicate<T> p) { this.it=it; predicate=p; }
public FilteredIterator (Iterable<T> it, Predicate<T> p) { this(it.iterator(),p); }
public boolean hasNext () {
while (it.hasNext()) {
current = it.next();
if (predicate.accept(current)) return true;
}
return false;
}
public T next () { return current; }
public void remove () { it.remove(); }
}
