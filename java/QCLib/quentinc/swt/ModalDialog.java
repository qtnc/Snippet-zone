package quentinc.swt;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;

public class ModalDialog extends Shell {
public ModalDialog (Shell parent) { this(parent,0); }
public ModalDialog (Shell parent, int style) { super(parent, style | SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM); }
protected void checkSubclass () {}
@Override public void open () {
super.open();
Display display = getDisplay();
while (!this.isDisposed()) if (!display.readAndDispatch()) display.sleep();
}

}
