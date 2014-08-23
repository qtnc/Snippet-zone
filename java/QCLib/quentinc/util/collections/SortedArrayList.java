package quentinc.util.collections;
import java.util.*;
public class SortedArrayList<T> extends ArrayList<T> {
Comparator<T> comparator=null;
public SortedArrayList () { this(null); }
public SortedArrayList (Comparator<T> c) { comparator=c; }
public boolean add (T e) { 
super.add(Collections.binarySearch(this,e,comparator),e); 
return true;
}
public void add (int n, T t) { throw new UnsupportedOperationException(); }
public T set (int n, T t) { throw new UnsupportedOperationException(); }
@SuppressWarnings("unchecked")
public int indexOf (Object o) {
int n = Collections.binarySearch(this,(T)o,comparator);
return n>=0&&get(n).equals(o)? n : -1;
}
@SuppressWarnings("unchecked")
public int lastIndexOf (Object o) {
int n = Collections.binarySearch(this,(T)o,comparator);
if (n<0) return -1;
if (!get(n).equals(o)) return -1;
while (n<size()&&get(n).equals(o)) n++;
return n -1;
}
public boolean contains (Object o) { return indexOf(o)!=-1; }
public boolean remove (Object o) {
int n = indexOf(o);
if (n>=0) { remove(n); return true; }
else return false;
}
public boolean addAll (Collection<? extends T> c) {
boolean b = false;
for (T x : c) b=b||add(x);
return b;
}
public boolean addAll (int index, Collection<? extends T> c) { throw new UnsupportedOperationException(); }
public void setComparator (Comparator<T> c) { Collections.sort(this,comparator = c); }
}