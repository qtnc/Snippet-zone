package quentinc.util.collections;
import java.util.*;
import quentinc.util.*;
public class FilteredSet<T> extends AbstractSet<T> {
private final Set<T> parent;
private final Predicate<T> predicate;
public FilteredSet (Set<T> s, Predicate<T> p) {
predicate = p;
parent = s;
}

@Override public IterableIterator<T> iterator () { return new FilteredIterator<T>(parent, predicate); }
@Override public int size () {
int c=0;
for (T t: iterator()) c++;
return c;
}
@SuppressWarnings("unchecked") public boolean contains (Object o) { return parent.contains(o) && predicate.accept((T)o); }
@Override public boolean add (T t) {
if (predicate.accept(t)) return parent.add(t);
else return false;
}
@SuppressWarnings("unchecked") @Override public boolean remove (Object o) {
if (predicate.accept((T)o)) return parent.remove(o);
else return false;
}
@Override public boolean removeAll (Collection<?> c) {
boolean b = false;
for (Object o: c) b = remove(o) || b;
return b;
}
@Override public void clear () {
parent.removeAll(Sets.collect(this));
}


}
