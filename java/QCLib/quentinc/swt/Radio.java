package quentinc.swt;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;

public class Radio extends Button {
public Radio (Composite s) { this(s,0); }
public Radio (Composite s, int c) { super(s, c | SWT.RADIO); }
protected void checkSubclass () {}
}
