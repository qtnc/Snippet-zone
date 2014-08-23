package quentinc.util;
import java.util.*;

public class Strings {
private Strings () {}

public static String capitalizeFirst (String str) {
if (str.length()<=0) return str;
return str.substring(0, 1).toUpperCase() + str.substring(1);
}

public static String[] split (String str, char ch) { return StringTokenizer.split(str,ch); }
public static String[] split (String str, String separator) { return split(str, separator, -1); }
public static String[] split (String str, String separator, int max) {
List<String> l = new ArrayList<String>();
int idx = -separator.length(), last = 0, count = 0;
while ((idx = str.indexOf(separator, idx+separator.length()))>0 && (max<=0 || ++count<max))  {
String s = str.substring(last, idx);
if (s.length()>0 || max==0) l.add(s);
last=idx + separator.length();
}
String s = str.substring(last);
if (s.length()>0 || max==0) l.add(s);
return l.toArray(new String[0]);
}

public static String join (String sep, Collection<?> c) {
StringBuilder sb = new StringBuilder();
for (Object o: c) {
if (sb.length()>0) sb.append(sep);
sb.append(o);
}
return sb.toString();
}
public static String join (String sep, Object... c) {
StringBuilder sb = new StringBuilder();
for (Object o: c) {
if (sb.length()>0) sb.append(sep);
sb.append(o);
}
return sb.toString();
}
public static String join (String sep, String sep2, Collection<?> c) {
StringBuilder sb = new StringBuilder();
int n=0, s=c.size();
for (Object o: c) {
if (sb.length()>0) {
if (++n>=s-1) sb.append(sep2);
else sb.append(sep);
}
sb.append(o);
}
return sb.toString();
}

public static String toAscii (String str) {
final String 
z1 = "éèêëàáâäîïìíôöòóùüûúçÊËÉÈÂÄÀÁÎÏÌÍÔÖÒÓÙÛÜÚÇãñõÃÑÕÿıİ ¡¢£¤¥¦§¨©ª«»¬­®¯°±²³´µ¶·¸¹º¼½¾¿Şß÷øĞ×Ø"
,z2 = "eeeeaaaaiiiioooouuuucEEEEAAAAIIIIOOOOUUUUCanoANOyyY !cLoY|S\"Ca\"\"--RMo+23`uP*,1o123?TB/oE*O"
;//f
StringBuilder sb = new StringBuilder(str);
for (int i=0; i < sb.length(); i++) {
char c = sb.charAt(i);
if (c>127) {
int k = z1.indexOf(c);
sb.setCharAt(i, (k>=0? z2.charAt(k) : '?'));
}}
return sb.toString();
}

public static String removeBackspaces (String s) {
int i = s.indexOf('\b');
if (i<0) return s;
StringBuilder sb = new StringBuilder(s);
i = -1;
while ((i = sb.indexOf("\b", i+1)) >=0) {
sb.delete(Math.max(0, i -1), i+1 );
i = Math.max(0, i -2);
}
return sb.toString();
}

public static String wrapLines (String str, int ll) {
int l = str.length();
if (l<=ll) return str;
StringBuilder sb = new StringBuilder(l+16);
int lastSpace = 0;
int lastWrap = 0;
int count = 0;
char c;
for (int i=0; i<l; i++, count++) {
c = str.charAt(i);
if (c==' ') lastSpace=i;
if (c=='\r' || c=='\n') { lastSpace=i; count=0; }
if (count>=ll) {
if (i-lastSpace>ll) {
sb.append(str, lastWrap, i).append("\r\n");
lastWrap=i;
count = 0;
}
else {
sb.append(str, lastWrap, lastSpace).append("\r\n");
i = lastWrap = lastSpace +1;
count = 0;
}}
}
sb.append(str, lastWrap, l);
return sb.toString();
}

}
