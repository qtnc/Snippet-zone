package quentinc.util;
import java.io.*;
import java.util.*;

public class Misc {
private Misc () {}

public static File getApplicationPath () {
try {
String s = java.net.URLDecoder.decode(new Misc().getClass().getProtectionDomain().getCodeSource().getLocation().getFile(), "ISO-8859-1");
s = s.substring(0, 1+s.lastIndexOf('/'));
s.substring(1).replace('/', File.separatorChar);
return new File(s);
} catch (UnsupportedEncodingException e) { return null; }
}

public static File getExtensionsPath () {
String s = System.getProperty("java.ext.dirs");
int i = s.indexOf(File.pathSeparatorChar);
if (i>=0) s = s.substring(0,i);
return new File(s);
}

public static File getUserHomePath () {
return new File( System.getProperty("user.home") );
}

public static boolean isWindows () {
return System.getProperty("os.name").toLowerCase().contains("windows");
}

public static boolean isLinux () {
return System.getProperty("os.name").toLowerCase().contains("linux");
}

public static boolean is64bit () {
return System.getProperty("sun.arch.data.model").contains("64");
}

public static boolean is32bit () {
return System.getProperty("sun.arch.data.model").contains("32");
}

public static Locale getLocaleFromString (String str) {
String[] t = Strings.split(str, '-');
switch(t.length){
case 0: return Locale.getDefault();
case 1: return new Locale(t[0]);
case 2: return new Locale(t[0], t[1]);
default: return new Locale(t[0], t[1], t[2]);
}}

public static Locale getBestLocaleMatch (Collection<Locale> col) { return getBestLocaleMatch(col, Locale.getDefault(), Locale.ENGLISH); }
public static Locale getBestLocaleMatch (Collection<Locale> col, Locale l, Locale def) {
if (col.contains(l)) return l;
if (!l.getVariant().equals("")) return getBestLocaleMatch(col, new Locale(l.getLanguage(), l.getCountry()), def);
else if (!l.getCountry().equals("")) return getBestLocaleMatch(col, new Locale(l.getLanguage()), def);
else return def;
}


public static Class loadClass (InputStream in) {
try {
return new InputStreamClassLoader(in) .findClass(null);
} 
catch (ClassNotFoundException e) { return null; }
catch (IOException e) { return null; }
}
public static Class loadClass (File in) {
try {
return new InputStreamClassLoader(in) .findClass(null);
} 
catch (ClassNotFoundException e) { return null; }
catch (IOException e) { return null; }
}
public static Class loadClass (java.net.URL in) {
try {
return new InputStreamClassLoader(in) .findClass(null);
} 
catch (ClassNotFoundException e) { return null; }
catch (IOException e) { return null; }
}

@SuppressWarnings("unchecked") private static <T extends Throwable> T throw2 (Exception e) throws T {      throw (T) e;    } 
public static RuntimeException throwUncheckedException (Exception e) { return Misc.<RuntimeException>throw2(e);   } 

}