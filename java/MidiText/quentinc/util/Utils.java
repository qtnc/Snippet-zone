package quentinc.util;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;

public class Utils {
private Utils () {}
public static boolean classExists (String name) { return getClass(name)!=null; }
public static Class getClass (String name) {
try {
System.out.print("Checking class " + name + " ... ");
Class c = Class.forName(name);
System.out.println("OK");
return c;
} catch (Exception e) {e.printStackTrace(); }
return null;
}

public static KeyStroke getKeyStroke (String str) {
String t[] = str.split("\\+");
int n = t.length;
int key = 0, modifiers = 0;
for (int i=0; i <= n -1; i++) {
if (t[i].equalsIgnoreCase("Shift")) modifiers |= InputEvent.SHIFT_MASK;
else if (t[i].equalsIgnoreCase("Ctrl")) modifiers |= InputEvent.CTRL_MASK;
else if (t[i].equalsIgnoreCase("Meta")) modifiers |= InputEvent.META_MASK;
else if (t[i].equalsIgnoreCase("Alt")) modifiers |= InputEvent.ALT_MASK;
}

String strk = t[n -1];

if (strk.length() == 1) {
char c = strk.toUpperCase().charAt(0);
if (c >= 'A' && c <= 'Z') key = (int)c;
if (c >= '0' && c <= '9') key = (int)c;
}
else if (strk.toUpperCase().matches("^F\\d+$")) {
int fn = Integer.parseInt(strk.substring(1)) -1;
key = fn + KeyEvent.VK_F1;
}
else if (strk.equalsIgnoreCase("insert")) key = KeyEvent.VK_INSERT;
else if (strk.equalsIgnoreCase("delete")) key = KeyEvent.VK_DELETE;
else if (strk.equalsIgnoreCase("home")) key = KeyEvent.VK_HOME;
else if (strk.equalsIgnoreCase("end")) key = KeyEvent.VK_END;
else if (strk.equalsIgnoreCase("Pageup")) key = KeyEvent.VK_PAGE_UP;
else if (strk.equalsIgnoreCase("Pagedown")) key = KeyEvent.VK_PAGE_DOWN;
else if (strk.equalsIgnoreCase("enter")) key = KeyEvent.VK_ENTER;
else if (strk.equalsIgnoreCase("backspace")) key = KeyEvent.VK_BACK_SPACE;
else if (strk.equalsIgnoreCase("space")) key = KeyEvent.VK_SPACE;
else if (strk.equalsIgnoreCase("tab")) key = KeyEvent.VK_TAB;
else if (strk.equalsIgnoreCase("escape")) key = KeyEvent.VK_ESCAPE;
else if (strk.equalsIgnoreCase("right")) key = KeyEvent.VK_RIGHT;
else if (strk.equalsIgnoreCase("left")) key = KeyEvent.VK_LEFT;
else if (strk.equalsIgnoreCase("up")) key = KeyEvent.VK_UP;
else if (strk.equalsIgnoreCase("down")) key = KeyEvent.VK_DOWN;

if (key >0) {
return KeyStroke.getKeyStroke(key, modifiers);
}
return null;
}



}
