package quentinc.util;
public class AndPredicate<T>  implements Predicate<T> {
private final Predicate<T> ps[];
public AndPredicate (Predicate<T>... x) { ps=x; }
public boolean accept (T ob)  {
for (Predicate<T> p: ps) if (!p.accept(ob)) return false;
return true;
}

}