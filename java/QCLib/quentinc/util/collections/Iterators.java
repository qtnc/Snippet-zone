package quentinc.util.collections;
import quentinc.util.*;
import java.util.*;
import java.util.regex.*;
public class Iterators {
private Iterators () {}
public static <T> Iterator<T> iterator (List<T> l, int index) { return l.listIterator(index); }
public static <T> IterableIterator<T> iterator (T... t) { return new ArrayIterator<T>(t); }
public static <T> IterableIterator<T> iterator (Iterable<T> it, Predicate<T> p) {
return new FilteredIterator<T>(it,p);
}
public static <T> IterableIterator<T> iterator (Iterable<T>... its) { return new AggregateIterator<T>(new ArrayIterator<Iterable<T>>(its)); }
public static <T> IterableIterator<T> iterator (Predicate<T> p, Iterable<T>... cols) { return iterator(iterator(cols), p); }
public static <T> IterableIterator<T> iterator () { return new EmptyIterator<T>(); }

public static <T> IterableIterator<T> reverseIterator (List<T> l) { return new ReverseIterator<T>(l); }

public static IterableIterator<String> iterator (Pattern pat, String str, int group) { return new MatcherIterator(pat.matcher(str), group); }
public static IterableIterator<String> iterator (Pattern pat, String str) { return iterator(pat, str, 0); }
public static IterableIterator<String> iterator (String regex, int options, String str, int group) { return iterator(Pattern.compile(regex, options), str, group); }
public static IterableIterator<String> iterator (String regex, String str, int group) { return iterator(regex, 0, str, group); }
public static IterableIterator<String> iterator (String regex, int options, String str) { return iterator(regex, options, str, 0); }
public static IterableIterator<String> iterator (String regex, String str) { return iterator(regex, 0, str, 0); }

public static <T> IterableIterator<T> iterator (final Enumeration<T> e) {
return new IterableIterator<T>(){
public T next () { return e.nextElement(); }
public boolean hasNext () { return e.hasMoreElements(); }
};}

public static <T> Enumeration<T> enumeration (final Iterator<T> it) {
return new Enumeration<T>(){
public T nextElement () { return it.next(); }
public boolean hasMoreElements () { return it.hasNext(); }
};}
public static <T> Enumeration<T> enumeration (Iterable<T> itr) {
return enumeration(itr.iterator());
}
public static <T> Enumeration<T> enumeration (IterableIterator<T> itr) {
return enumeration(itr.iterator());
}

}
