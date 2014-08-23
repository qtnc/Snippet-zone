package quentinc.util;
import quentinc.util.collections.*;
import java.util.*;
public class StringTokenizer extends IterableIterator<String> {
String s;
int pos = -1;
char ch;
public StringTokenizer (String s, char ch) {
this.s=s;
this.ch=ch;
}
public void setDelimiter (char ch) { this.ch=ch; }
public String rest () { 
if (pos<0) return "";
String re = s.substring(pos+1);
pos = s.length();
return re;
}
public boolean hasNext () {
return pos<s.length();
}
public String next () {
if (pos>=s.length()) return null;
int i = s.indexOf(ch, pos+1);
if (i<0) i = s.length();
String re = s.substring(pos+1, i);
pos = i;
return re;
}
public List<String> collect () { return collect(0); }
public List<String> collect (int n) { return collect(new ArrayList<String>(),n); }
public List<String> collect (List<String> l) { return collect(l,0); }
public List<String> collect (List<String> l, int max) {
while ((max<=0 || --max>=1) && hasNext()) l.add(next());
if (hasNext()) l.add(rest());
return l;
}

public static String[] split (String s, char ch) { return split(s,ch,0); }
public static String[] split (String s, char ch, int n) { return new StringTokenizer(s,ch).collect(n).toArray(new String[0]); }
}