package quentinc.swt;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;

public class Button3State extends Button implements SelectionListener {
public Button3State (Composite s) { this(s,0); }
public Button3State (Composite s, int c) { 
super(s, c | SWT.CHECK);
addSelectionListener(this); 
}
protected void checkSubclass () {}

public void widgetDefaultSelected (SelectionEvent e) {}
public void widgetSelected (SelectionEvent e) {
if (getSelection()) {
if (!getGrayed()) setGrayed(true);
}
else if (getGrayed()) {
setGrayed(false);
setSelection(true);
}
}

}
