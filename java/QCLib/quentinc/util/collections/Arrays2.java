package quentinc.util.collections;
import java.util.*;
public class Arrays2 {
private Arrays2 () {}
public static <T> T[] array (T... t) { return t; }
public static <T> T random (Random r, T... t) {
return t[r.nextInt(t.length)];
}

@SuppressWarnings("unchecked") public static <T> T[] newArray (Class c, int l) {
return (T[])( java.lang.reflect.Array.newInstance(c, l) );
}
@SuppressWarnings("unchecked") public static <T> T[] newArray (T ob, int l) {
return (T[])( java.lang.reflect.Array.newInstance(ob.getClass(), l) );
}
public static <T> T[] merge (T[]... tabs) {
int l = 0;
for (T[] x : tabs) l+=x.length;
T[] nt = newArray(tabs[0][0], l);
int idx=0;
for (T[] x : tabs) {
for (T t : x) {
nt[idx++] = t;
}}
return nt;
}
public static <T> int indexOf (T[] tab, T ob) { return indexOf(tab,ob,0); }
public static <T> int indexOf (T[] tab, T ob, int start) {
for (int i=start; i<tab.length; i++) {
if (ob.equals(tab[i])) return i;
}
return -1;
}
public static <T> int lastIndexOf (T[] tab, T ob) { return indexOf(tab,ob,tab.length -1); }
public static <T> int lastIndexOf (T[] tab, T ob, int start) {
for (int i=start; i>=0; i--) {
if (ob.equals(tab[i])) return i;
}
return -1;
}

}