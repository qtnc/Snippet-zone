package quentinc.util.collections;
public class Pair<A,B> implements java.util.Map.Entry<A,B> {
private A a;
private B b;
public Pair (A a, B b) { this.a=a; this.b=b; }
public A getFirst () { return a; }
public A getKey () { return a; }
public B getSecond () { return b; }
public B getValue () { return b; }
public void setFirst (A a) { this.a=a; }
public void setKey (A a) { this.a=a; }
public void setSecond (B b) { this.b=b; }
public B setValue (B b) { 
B old = this.b;
this.b=b; 
return old;
}
public boolean equals (Object o) {
if (!(o instanceof Pair)) return false;
Pair p = (Pair)o;
return a.equals(p.getFirst()) && b.equals(p.getSecond());
}
public int hashCode () { return a.hashCode() + b.hashCode(); }
}
