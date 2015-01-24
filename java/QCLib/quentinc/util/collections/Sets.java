package quentinc.util.collections;
import quentinc.util.*;
import java.util.*;
public class Sets {
private Sets () {}

public static <T> Set<T> set (T... t) {
Set<T> s = new HashSet<T>();
for (T elem : t) s.add(elem);
return s;
}
public static <T> NavigableSet<T> navigableSet (T... t) {
NavigableSet<T> s = new TreeSet<T>();
for (T elem : t) s.add(elem);
return s;
}
public static <T> SortedSet<T> sortedSet (T... t) { return navigableSet(t); }

public static <K,V> Set<Map.Entry<K,V>> set (Map<K,V> m) {
return new SetFromMap<K,V>(m);
}

public static <T> Set<T> filter (Set<T> base, Predicate<T> predicate) { return new FilteredSet<T>(base, predicate); }

public static <T> Set<T> collect (Iterable<T> it) { return collect(it.iterator()); }
public static <T> Set<T> collect (IterableIterator<T> it) { return collect(it.iterator()); }
public static <T> Set<T> collect (Iterator<T> it) { 
Set<T> s = new HashSet<T>();
while (it.hasNext()) s.add(it.next());
return s;
}

}