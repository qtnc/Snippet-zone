package quentinc.swt;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;

public class InputDialog extends ModalDialog implements SelectionListener {
Text input;
Button ok, cancel;
Label label;
String value = null;

public InputDialog (Shell parent) { this(parent, "", "", ""); }
public InputDialog (Shell parent, String title, String message, String text) {
super(parent);
setText(title);
setLayout(new FillLayout(SWT.VERTICAL));
label = new Label(this, SWT.LEFT | SWT.WRAP); 
label.setText(message);
input = new Text(this, SWT.SINGLE);
input.setText(text);
Composite c = new Composite(this,SWT.NONE);
RowLayout r = new RowLayout(SWT.HORIZONTAL);
r.justify = true; 
r.center = true;
c.setLayout(r);
ok = new Button(c, SWT.PUSH);
cancel = new Button(c, SWT.PUSH);
ok.setText("&" + SWT.getMessage("SWT_OK"));
cancel.setText("&" + SWT.getMessage("SWT_Cancel"));
ok.addSelectionListener(this);
cancel.addSelectionListener(this);
setDefaultButton(ok);
pack();
}

public void widgetSelected (SelectionEvent e) {
if (e.widget==ok) value = input.getText();
close();
}
public void widgetDefaultSelected (SelectionEvent e) {
widgetSelected(e);
}

public String getValue () { return value; }
public void setValue (String value) {
if (!input.isDisposed()) input.setText(value);
}

public String getMessage () {
if (label.isDisposed()) return null;
return label.getText();
}

public void setMessage (String str) {
if (!label.isDisposed()) label.setText(str);
}

}