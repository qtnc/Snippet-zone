package quentinc.swt;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;

public class Checkbox extends Button {
public Checkbox (Composite s) { this(s,0); }
public Checkbox (Composite s, int c) { super(s, c | SWT.CHECK); }
protected void checkSubclass () {}
}
