package quentinc.util;

public class Range<T extends Comparable<T>> {
private static final int DISJOIN = 0, LESS = 1, GREATER = 2, INCLUDED = 3, INCLUDING = 4;
private final T start, end;
private final boolean minInclusive, maxInclusive;

public Range (T start, T end) { this(start, end, true, false); }
public Range (T start, T end, boolean b) { this(start, end, b, b); }
public Range (T start, T end, boolean mi, boolean mxi) { 
if (start.compareTo(end)>0) { T x=start; start=end; end=x; }
this.start=start; 
this.end=end; 
this.minInclusive=mi;
this.maxInclusive=mxi;
}

public boolean contains (T value) {
int a = start.compareTo(value), b = end.compareTo(value);
if (minInclusive && a==0) return true;
else if (maxInclusive && b==0) return true;
else return a<0 && b>0;
}

private int compareTo (Range<T> r) {
int 
ss = start.compareTo(r.start),
se = start.compareTo(r.end),
es = end.compareTo(r.start),
ee = end.compareTo(r.end);
if (ss>=0 && ee<=0) return INCLUDED;
else if (ss<0 && ee>0) return INCLUDING;
else if (se<0 && ss>0) return GREATER;
else if (es>0 && ee<0) return LESS;
else return DISJOIN;
}

public boolean disjoin (Range<T> r) {
return compareTo(r)==DISJOIN;
}
public boolean intersects (Range<T> r) {
return compareTo(r)!=DISJOIN;
}
public boolean contains (Range<T> r) {
return compareTo(r)==INCLUDING;
}
public boolean isSuperrangeOf (Range<T> r) {
return compareTo(r)==INCLUDING;
}
public boolean isSubrangeOf (Range<T> r) {
return compareTo(r)==INCLUDED;
}

public Range<T> union (Range<T> r) {
switch(compareTo(r)) {
case DISJOIN: return null;
case LESS: return new Range<T>(start, r.end);
case GREATER: return new Range<T>(r.start, end);
case INCLUDED: return r;
case INCLUDING: return this;
default: return null;
}}
public Range<T> intersection (Range<T> r) {
switch(compareTo(r)) {
case DISJOIN: return null;
case GREATER: return new Range<T>(start, r.end);
case LESS: return new Range<T>(r.start, end);
case INCLUDED: return this;
case INCLUDING: return r;
default: return null;
}}

public T getStart () { return start; }
public T getMin () { return start; }
public T getFirst () { return start; }
public T getEnd () { return end; }
public T getMax () { return end; }
public T getLast () { return end; }

public String toString () { return "[" + start + ".." + end + "]"; }

}
