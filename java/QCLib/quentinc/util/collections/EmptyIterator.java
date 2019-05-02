package quentinc.util.collections;
public class EmptyIterator<T> extends IterableIterator<T> {
public T next () { return null; }
public boolean hasNext () { return false; }
}