package quentinc.util;
public class NotPredicate<T> implements Predicate<T> {
private final Predicate<T> p;
public NotPredicate (Predicate<T> x) { p=x; }
public boolean accept (T ob) { return !p.accept(ob); }
}