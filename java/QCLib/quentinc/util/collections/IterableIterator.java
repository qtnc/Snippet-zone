package quentinc.util.collections;
import java.util.*;
public abstract class IterableIterator<E> implements Iterable<E>, Iterator<E> {
@Override public void remove () { throw new UnsupportedOperationException(); }
@Override public final Iterator<E> iterator () { return this; }
}
