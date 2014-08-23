package quentinc.swt;
import static quentinc.util.Strings.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import java.util.*;

public class SWTUtils {
private static Map<Integer,String> keys;
private static Map<String,Integer> rkeys;

private SWTUtils () {}

public static int messageBox (Shell shell, String title, String message, int options) {
MessageBox b = new MessageBox(shell, options);
b.setText(title);
b.setMessage(message);
return b.open();
}
public static void alert (Shell parent, String title, String message) {
messageBox(parent, title, message, SWT.OK | SWT.ICON_INFORMATION);
}
public static boolean confirm (Shell parent, String title, String message) {
return SWT.YES == messageBox(parent, title, message, SWT.YES | SWT.NO | SWT.ICON_QUESTION);
}
public static String prompt (Shell parent, String title, String message, String text) {
InputDialog i = new InputDialog(parent, title, message, text);
i.open();
return i.getValue();
}

private static String getSimpleKeyName (int code) {
if (keys==null) loadKeys();
String s = keys.get(code);
if (s!=null) return s;
else return ""+((char)code);
}
private static String getSimpleKeyName (int code, Locale l) {
if (l==null) l=Locale.getDefault();
ResourceBundle rb = ResourceBundle.getBundle("quentinc.swt.keys", l);
String s = getSimpleKeyName(code);
if (s.length()>1) try {
return capitalizeFirst(rb.getString(s));
} catch (MissingResourceException e) {}
return capitalizeFirst(s);
}
private static int getSimpleKeyCode (String name) {
if (rkeys==null) loadKeys();
Integer re = rkeys.get(name.toLowerCase());
if (re!=null) return re;
else return name.charAt(0);
}
public static String getKeyName (int code) {
if (code==SWT.CTRL || code==SWT.ALT || code==SWT.SHIFT || code==SWT.COMMAND) return getSimpleKeyName(code);
StringBuilder sb = new StringBuilder();
if ((code&SWT.CTRL)!=0) sb.append(getSimpleKeyName(SWT.CTRL)).append("+");
if ((code&SWT.ALT)!=0) sb.append(getSimpleKeyName(SWT.ALT)).append("+");
if ((code&SWT.COMMAND)!=0) sb.append(getSimpleKeyName(SWT.COMMAND)).append("+");
if ((code&SWT.SHIFT)!=0) sb.append(getSimpleKeyName(SWT.SHIFT)).append("+");
sb.append(getSimpleKeyName(code&SWT.KEY_MASK));
return sb.toString();
}
public static String getKeyDisplayName (int code) { return getKeyDisplayName(code,null); }
public static String getKeyDisplayName (int code, Locale l) {
if (code==SWT.CTRL || code==SWT.ALT || code==SWT.SHIFT || code==SWT.COMMAND) return getSimpleKeyName(code);
StringBuilder sb = new StringBuilder();
if ((code&SWT.CTRL)!=0) sb.append(getSimpleKeyName(SWT.CTRL, l)).append("+");
if ((code&SWT.ALT)!=0) sb.append(getSimpleKeyName(SWT.ALT, l)).append("+");
if ((code&SWT.COMMAND)!=0) sb.append(getSimpleKeyName(SWT.COMMAND, l)).append("+");
if ((code&SWT.SHIFT)!=0) sb.append(getSimpleKeyName(SWT.SHIFT, l)).append("+");
sb.append(getSimpleKeyName(code&SWT.KEY_MASK, l));
return sb.toString();
}
public static int getKeyCode (String name) {
if (name==null) return 0;
String[] t = split(name, '+');
int k = 0;
for (String s: t) k |= getSimpleKeyCode(s);
return k;
}

private static void loadKeys () {
keys = new HashMap<Integer,String>();
rkeys = new HashMap<String,Integer>();
addKey(SWT.CTRL, "ctrl");
addKey(SWT.SHIFT, "shift");
addKey(SWT.ALT, "alt");
addKey(SWT.COMMAND, "cmd");
addKey(SWT.ARROW_UP, "up");
addKey(SWT.ARROW_DOWN, "down");
addKey(SWT.ARROW_LEFT, "left");
addKey(SWT.ARROW_RIGHT, "right");
addKey(SWT.PAGE_UP, "pageup");
addKey(SWT.PAGE_DOWN, "pagedown");
addKey(SWT.HOME, "home");
addKey(SWT.END, "end");
addKey(SWT.INSERT, "insert");
addKey(127, "delete");
addKey(27, "escape");
addKey(32, "space");
addKey(8, "backspace");
addKey(9, "tab");
addKey(13, "enter");
addKey(SWT.PAUSE, "pause");
addKey(SWT.BREAK, "break");
addKey(SWT.KEYPAD_ADD, "numpad_plus");
addKey(SWT.KEYPAD_MULTIPLY, "numpad_star");
addKey(SWT.KEYPAD_SUBTRACT, "numpad_minus");
addKey(SWT.KEYPAD_DECIMAL, "numpad_dot");
addKey(SWT.KEYPAD_DIVIDE, "numpad_slash");
addKey(SWT.KEYPAD_CR, "numpad_enter");
addKey(SWT.KEYPAD_EQUAL, "numpad_equal");
addKey(SWT.HELP, "help");
addKey(SWT.CAPS_LOCK, "capslock");
addKey(SWT.NUM_LOCK, "numlock");
addKey(SWT.SCROLL_LOCK, "scrolllock");
addKey(SWT.PRINT_SCREEN, "printscreen");
for (int i=0; i<=9; i++) addKey(SWT.KEYPAD_0 +i, "numpad"+i);
for (int i=1; i<=20; i++) addKey(SWT.F1 -1 +i, "f"+i);
}
private static void addKey (int code, String name) {
Integer icode = Integer.valueOf(code);
keys.put(icode, name);
rkeys.put(name, icode);
}

}
