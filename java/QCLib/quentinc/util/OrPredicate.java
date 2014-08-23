package quentinc.util;
public class OrPredicate<T>  implements Predicate<T> {
private final Predicate<T> ps[];
public OrPredicate (Predicate<T>... x) { ps=x; }
public boolean accept (T ob)  {
for (Predicate<T> p: ps) if (p.accept(ob)) return true;
return false;
}

}