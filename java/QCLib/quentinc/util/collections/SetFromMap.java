package quentinc.util.collections;
import java.util.*;

public class SetFromMap <K,V> extends AbstractSet <Map.Entry<K,V>> {

private final Map<K,V> m;

public SetFromMap  (Map<K,V> m1) { m=m1; }

public Iterator<Map.Entry<K,V>> iterator () {  return m.entrySet().iterator();  }

public int size () { return m.size(); }

@SuppressWarnings("unchecked") public boolean contains (Object o) {
if (!(o instanceof Map.Entry)) return m.containsKey(o);
Map.Entry<K,V> e = (Map.Entry<K,V>)o;
return m.containsKey(e.getKey());
}

public boolean add (Map.Entry<K,V> e) {
return m.put(e.getKey(), e.getValue())==null;
}

@SuppressWarnings("unchecked") public boolean remove (Object o) {
if (!(o instanceof Map.Entry)) return m.remove(o)!=null;
Map.Entry<K,V> e = (Map.Entry<K,V>)o;
return m.remove(e.getKey())!=null;
}

public void clear () { m.clear(); }

}