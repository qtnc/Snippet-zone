package quentinc.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.util.*;

public class BasicDialog extends JDialog {
JPanel mainPanel = new JPanel(), buPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
private static String OK = "OK", CANCEL = "Cancel";
private int state = 0;

public static String getOKButtonCaption () { return OK; }
public static String getCancelButtonCaption () { return CANCEL; }
public static void setOKButtonCaption (String s) { OK=s; }
public static void setCancelButtonCaption (String s) { CANCEL=s; }

public BasicDialog (JFrame f, String title) { 
 super(f,title,true);  
initialize();
}
public BasicDialog (JDialog d, String title) { 
super(d,title,true); 
initialize();
}
public BasicDialog (String title) { 
super((JFrame)null, title); 
initialize();
}
public BasicDialog () { this("Untitled dialog"); }

public JPanel getPanel () { return mainPanel; }

private void initialize () {
setLayout(new BorderLayout());
add(mainPanel, BorderLayout.CENTER);
add(buPanel, BorderLayout.CENTER);

JButton ok, cancel;

ok = new JButton(OK);
cancel = new JButton(CANCEL);
ok.setActionCommand(OK);
cancel.setActionCommand(CANCEL);
ok.addActionListener(new ActionListener(){
public void actionPerformed(ActionEvent e) {
doClickOK();
}});
Action cancelAction = null;
cancel.addActionListener(cancelAction = new AbstractAction(){
public void actionPerformed(ActionEvent e) {
doClickCancel();
}});
addWindowListener(new WindowAdapter(){
public void windowClosing (WindowEvent e) {
doClickCancel();
}});

buPanel.add(ok);
buPanel.add(cancel);

setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);


getRootPane().setDefaultButton(ok);

cancel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0), "cancel");
cancel.getActionMap().put("cancel", cancelAction);
}

public void doClickOK () {
state=1;
dispose();
}
public void doClickCancel () {
state = 2;
dispose();
}
public boolean okWasPressed () { return state==1; }
public boolean cancelWasPressed () { return state==2; }
public boolean showDialog () {
state = 0;
pack();
setVisible(true);
while (state==0) { try { Thread.currentThread().sleep(1000); } catch (Exception e) {}}
return state==1;
}

public static boolean showDialog (JFrame f, String title, JPanel panel) {
BasicDialog b = new BasicDialog(f,title);
return showDialog2(b,panel);
}
public static boolean showDialog (JDialog f, String title, JPanel panel) {
BasicDialog b = new BasicDialog(f,title);
return showDialog2(b,panel);
}
public static boolean showDialog (String title, JPanel panel) {
BasicDialog b = new BasicDialog(title);
return showDialog2(b,panel);
}
public static boolean showDialog (JPanel panel) {
BasicDialog b = new BasicDialog();
return showDialog2(b,panel);
}
private static boolean showDialog2 (BasicDialog b, JPanel panel) {
JPanel p = b.getPanel();
p.setLayout(new BorderLayout());
p.add(panel);
return b.showDialog();
}
}