package quentinc.util.collections;
import java.util.*;
public class Lists {
private Lists () {}
public static <T> List<T> list (T... t) {
List<T> l = new ArrayList<T>();
for (T elem : t) l.add(elem);
return l;
}

public static <T> List<T> collect (Iterable<T> it) { return collect(it.iterator()); }
public static <T> List<T> collect (IterableIterator<T> it) { return collect(it.iterator()); }
public static <T> List<T> collect (Iterator<T> it) { 
List<T> s = new ArrayList<T>();
while (it.hasNext()) s.add(it.next());
return s;
}


}