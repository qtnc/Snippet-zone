package quentinc.swt;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;

public class KeystrokeField extends Text implements Listener {
private int keystroke = 0;
protected void checkSubclass () {}
public KeystrokeField (Composite parent, int style) { 
super(parent, style);
addListener(SWT.KeyDown,this); 
}
public KeystrokeField (Composite parent) { 
this(parent,0); 
}
public void handleEvent (Event e) {
if (e.keyCode==SWT.CTRL || e.keyCode==SWT.SHIFT || e.keyCode==SWT.ALT || e.keyCode==SWT.COMMAND || e.keyCode==9) return;
else if (e.keyCode==127 && e.stateMask==0) updateKey(0);
else updateKey(e.keyCode | e.stateMask);
e.doit=false;
}

private void updateKey (int code) {
String s = SWTUtils.getKeyDisplayName(code);
setText(s);
setSelection(0, 1+s.length());
keystroke = code;
}

public int getKey () { return keystroke; }
public void setKey (final int k) { 
keystroke=k;
getDisplay().asyncExec(new Runnable(){ public void run(){
updateKey(k);
}});
}

}