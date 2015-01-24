package quentinc.util.collections;
import quentinc.util.*;
import java.util.*;
public class Collections2 {
private Collections2 () {}
public static <T> T random (Random r, List<T> l) {
return l.get(r.nextInt(l.size()));
}
public static <T> T random (Random r, Collection<T> c) {
int n = r.nextInt(c.size());
Iterator<T> it = c.iterator();
for (int i=0; i<n; i++) it.next();
return it.next();
}

public static <T> boolean every (Iterable<T> it, Predicate<T> p) {
for (T t : it) {
if (!p.accept(t)) return false;
}
return true;
}
public static <T> boolean some (Iterable<T> it, Predicate<T> p) {
for (T t : it) {
if (p.accept(t)) return true;
}
return false;
}
@SuppressWarnings("unchecked") public static <T, C extends Collection<T>> C filter (C col, Predicate<T> p) {
C newCol = null;
try {
newCol = (C)( col.getClass().newInstance() );
} catch (Exception e) { return null; }
for (T t : col) {
if (p.accept(t)) newCol.add(t);
}
return newCol;
}

public static <T, C extends Collection<T>> C addAll (C c, IterableIterator<T> its) { return addAll(c, its.iterator()); }
public static <T, C extends Collection<T>> C addAll (C c, Iterable<T> its) { return addAll(c, its.iterator()); }
public static <T, C extends Collection<T>> C addAll (C c, Iterator<T> it) {
while (it.hasNext()) c.add(it.next());
return c;
}

public static <T, C extends Collection<T>> C removeAll (C c, IterableIterator<T> its) { return removeAll(c, its.iterator()); }
public static <T, C extends Collection<T>> C removeAll (C c, Iterable<T> its) { return removeAll(c, its.iterator()); }
public static <T, C extends Collection<T>> C removeAll (C c, Iterator<T> it) {
while (it.hasNext()) c.remove(it.next());
return c;
}
public static <T, C extends Collection<T>> C removeAll (C c, T... ts) {
for (T t: ts) c.remove(t);
return c;
}

public static <T, C extends Collection<T>> C removeAll (C c, Predicate<T> p) {
Iterator<T> it = c.iterator();
while (it.hasNext()) {
if (p.accept(it.next())) it.remove();
}
return c;
}

public static <T, C extends Collection<T>> C retainAll (C c, Predicate<T> p) {
Iterator<T> it = c.iterator();
while (it.hasNext()) {
if (!p.accept(it.next())) it.remove();
}
return c;
}



}