package quentinc.util.collections;
import java.util.regex.*;
public class MatcherIterator extends IterableIterator<String> {
private final Matcher m;
private final int g;
public MatcherIterator (Matcher m) { this(m,0); }
public MatcherIterator (Matcher m, int g) { this.m=m; this.g=g; }
public boolean hasNext () { return m.find(); }
public String next () { return m.group(g); }
}
