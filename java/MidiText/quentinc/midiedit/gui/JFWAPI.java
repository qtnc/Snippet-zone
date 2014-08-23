package quentinc.midiedit.gui;

import com.sun.jna.*;

public class JFWAPI implements FunctionMapper {
private static boolean init = false;
public JFWAPI () {
if (!init) init();
}
private void init () {
NativeLibrary lib = NativeLibrary.getInstance("jfwapi");
lib.getOptions().put(Library.OPTION_FUNCTION_MAPPER, this);
Native.register(lib);
init=true;
}
public String getFunctionName (NativeLibrary lib, java.lang.reflect.Method m) {
String name = m.getName();
if (name.equals("sayString")) return "JFWSayString";
else if (name.equals("stopSpeech")) return "JFWStopSpeech";
else if (name.equals("runScript")) return "JFWRunScript";
else return null;
}

public native boolean sayString (String str, boolean interrupt) ;
public native boolean stopSpeech () ;
public native boolean runScript (String scriptName) ;
}
