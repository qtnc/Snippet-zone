package quentinc.sf2edit.gui;
import quentinc.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.util.*;
import com.sun.media.sound.*;
import javax.sound.midi.*;
import javax.sound.sampled.*;
import javax.swing.tree.*;
import javax.swing.filechooser.*;

public class Main extends WindowAdapter implements TreeSelectionListener, Thread.UncaughtExceptionHandler {
private class SF2GUITree implements TreeNode {
private class LeafNode implements TreeNode {
private Object value;
private TreeNode parent;
public LeafNode (TreeNode p, Object v) {
parent=p; 
value=v;
}
public Object getValue () { return value; }
public String toString () { 
if (value instanceof Instrument) {
Instrument i = (Instrument)value;
Patch p = i.getPatch();
return i.getName() + " (" + p.getBank() + "," + p.getProgram() + ")";
}
else if (value instanceof SF2Layer) return ((SF2Layer)value).getName();
else if (value instanceof SF2Sample) return ((SF2Sample)value).getName();
else return value.toString(); 
}
public TreeNode getParent () { return parent; }
public TreeNode getChildAt (int n) { return null; }
public int getChildCount () { return 0; }
public int getIndex (TreeNode n) { return -1; }
public boolean isLeaf () { return true; }
public boolean getAllowsChildren () { return false; }
public Enumeration children () { return Collections.enumeration(Collections.EMPTY_LIST); }
public boolean equals (Object o) { return value.equals(o); }
}
private class ListNode implements TreeNode {
private Object[] list;
private String value;
private TreeNode parent;
public ListNode (TreeNode p, Object[] l, String v) { parent=p; list=l;  value=v; }
public boolean equals (Object o) { return value.equals(o); }
public String toString () { return value; }
public int getChildCount () { return list.length; }
public int getIndex (TreeNode n) { return -1; }
public TreeNode getChildAt (int n) { return new LeafNode(this, list[n]); }
public boolean isLeaf () { return false; }
public boolean getAllowsChildren () { return true; }
public TreeNode getParent () { return parent; }
public Enumeration children () {
ArrayList<TreeNode> l = new ArrayList<TreeNode>(list.length);
for (Object o : list) l.add(new LeafNode(this, o));
return Collections.enumeration(l);
}
}

TreeNode[] nodes = new TreeNode[3];
public int getChildCount () { return nodes.length; }
public TreeNode getChildAt (int n) {
if (nodes[n]==null) nodes[n] = implGetChildAt(n);
return nodes[n];
}
private TreeNode implGetChildAt (int n) {
switch (n) {
case 0 : return new ListNode(this, bank.getInstruments(), lng("tree.instruments"));
case 1 : return new ListNode(this, bank.getLayers(), lng("tree.layers"));
case 2 : return new ListNode(this, bank.getSamples() , lng("tree.samples"));
default : return null;
}}
public int getIndex (TreeNode n) { return -1; }
public boolean getAllowsChildren () { return true; }
public boolean isLeaf () { return false; }
public TreeNode getParent () { return null; }
public String toString () { return (curFile==null? lng("tree.defaultName") : curFile.getName()); }
public void clearNodes () {
for (int i=0; i < nodes.length; i++) nodes[i]=null;
}
public Enumeration children () {
ArrayList<TreeNode> l = new ArrayList<TreeNode>();
for (int i=0; i < 3; i++) l.add(getChildAt(i));
return Collections.enumeration(l);
}

}
private class ActionHandler extends AbstractAction {
public ActionHandler (String actionName, String menuName) {
String
name = menuName+'.'+actionName,
tooltip = name + ".tooltip",
longtext = name + ".longText";

name = lng(name, actionName);
tooltip = lng(tooltip, null);
longtext = lng(longtext, null);

int mnemonic = 0, k = name.indexOf("&");
if (k!= -1) {
mnemonic = name.toUpperCase().charAt(k+1) -'A' + KeyEvent.VK_A;
name = name.substring(0,k)+name.substring(k+1);
}

putValue(NAME, name);
putValue(SHORT_DESCRIPTION, tooltip);
putValue(LONG_DESCRIPTION, longtext);
putValue(MNEMONIC_KEY, mnemonic);
putValue(ACTION_COMMAND_KEY, actionName);
}
public String getText () { return (String)getValue(NAME); }
public String getLabel () { return getText(); }
public String getTooltip () { return (String)getValue(SHORT_DESCRIPTION); }
public String getLongText () { return (String)getValue(LONG_DESCRIPTION); }
public Icon getSmallIcon () { return (Icon)getValue(SMALL_ICON); }
public String getActionCommand () { return (String)getValue(ACTION_COMMAND_KEY); }
public KeyStroke getKeyStroke () { return (KeyStroke)getValue(ACCELERATOR_KEY); }
public int getMnemonic () { return (Integer)getValue(MNEMONIC_KEY); }
public void doAction () { actionPerformed(null); }
public void actionPerformed (ActionEvent e) {
action(e.getActionCommand());
}
}


JFrame win;
ActionMap actions = new ActionMap();
JMenuBar menubar = new JMenuBar();
ResourceBundle strings;
Properties settings = new Properties();
AudioSynthesizer synth;
Receiver recv;
SourceDataLine line;
FloatControl volume;
File curFile = null;
SF2Soundbank bank = new SF2Soundbank();
JTree tree;
SF2GUITree treestruct;
JComboBox midiMsgType;
JTextField midiData1, midiData2;
JButton midiSend;
CardLayout card;
JPanel panel;
Object curObject = null;

public static void main (String... args) { new Main(args); }
public Main (String... args) {
Thread.setDefaultUncaughtExceptionHandler(this);

try {
PrintStream ps = new PrintStream(new BufferedOutputStream(new FileOutputStream("stdout.txt")), true);
System.setOut(ps);
System.setErr(ps);
} catch (IOException e) { e.printStackTrace(); }
try {
InputStream in = new BufferedInputStream(new FileInputStream("sf2edit-config.properties"));
settings.load(in);
in.close();
} catch (IOException e) { e.printStackTrace(); }

String deflocale[] = param("language", "en-US").split("-",3);
Locale loc = new Locale(deflocale[0], (deflocale.length>1? deflocale[1] : ""), (deflocale.length>2? deflocale[2] : ""));

Locale.setDefault(loc);
JComponent.setDefaultLocale(loc);
strings = ResourceBundle.getBundle("sf2edit-lang", loc);

try {
BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("sf2edit-menubar.properties")));
JMenu m;
while ((m=loadMenu(in,0))!=null) menubar.add(m);
in.close();
} catch (IOException e) { e.printStackTrace(); }
try {
AudioFormat format = new AudioFormat(Integer.parseInt(param("synthSampleRate", "44100")), Integer.parseInt(param("synthSampleSize", "16")), Integer.parseInt(param("synthChannels", "2")), true, false);
int bufferSize = Integer.parseInt(param("synthBufferLength", "200")) * (int)format.getSampleRate() * format.getSampleSizeInBits() * format.getChannels() / 8000;
DataLine.Info info = new DataLine.Info(SourceDataLine.class, format, bufferSize);
line = (SourceDataLine)AudioSystem.getLine(info);
line.open();

volume = (FloatControl)line.getControl(FloatControl.Type.MASTER_GAIN);

Map<String,Object> paramMap = new HashMap<String,Object>();
paramMap.put("format", format);
paramMap.put("latency", 1000L * Long.parseLong(param("synthBufferLength", "200")));
paramMap.put("interpolation", param("synthInterpolation", "linear"));
paramMap.put("max polyphony", Integer.parseInt(param("synthMaxPolyphony", "64")));
paramMap.put("control rate", (float)Double.parseDouble(param("synthControlRate", "147")));

synth = new SoftSynthesizer();
synth.open(line, paramMap);
line.start();

recv = synth.getReceiver();


} catch (Exception e) { e.printStackTrace(); System.exit(0); }
try {
File file = new File("D:\\java\\jdk162\\jre\\lib\\audio\\ct2mgm.sf2");
InputStream stream = new BufferedInputStream(new FileInputStream(file));
bank = new SF2Soundbank(stream);
stream.close();
} catch (Exception e) { e.printStackTrace(); }

win = new JFrame();
treestruct = new SF2GUITree();
tree = new JTree(treestruct);
tree.addTreeSelectionListener(this);
tree.setEditable(false);
JPanel p1 = new JPanel(new FlowLayout());
midiSend = new JButton(createAction(("midiSend").split(" "), ""));
midiData1 = new JTextField(3); midiData2 = new JTextField(3);
midiMsgType = new JComboBox(new String[]{
"128 - Note off",
"144 - Note on",
"160 - Channel pressure",
"176 - Control change",
"192 - Program change",
"208 - Polyphonic key pressure",
"224 - Pitch bend"
});
p1.add(midiMsgType);
p1.add(midiData1);
p1.add(midiData2);
p1.add(midiSend);
win.setLayout(new BorderLayout());
win.add(new JScrollPane(tree), BorderLayout.WEST);
win.add(p1, BorderLayout.SOUTH);
panel = new JPanel(card = new CardLayout());
win.add(panel, BorderLayout.CENTER);

win.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
win.addWindowListener(this);
win.setJMenuBar(menubar);
win.pack();
win.setVisible(true);
fireTreeChanged();
}
public String param (String n) { return param(n,n); }
public String param (String n, String d) { return settings.getProperty(n,d); }
public String lng (String n) { return lng(n,n); }
public String lng (String n, String d) {
try { return strings.getString(n);
} catch (Exception e) { return d; }
}
private Action createAction (String args[], String menuName) {
String actionName = args[0], accelerator = null;
ImageIcon icon = null;

for (int i=1; i < args.length; i++) {
if (args[i].startsWith("+")) accelerator = args[i].substring(1).trim();
else if (args[i].matches("^\\[.*\\]$")) icon = new ImageIcon(args[i].substring(1,args[i].length()-1), lng(menuName+"."+actionName, actionName));
}

Action a = new ActionHandler(actionName, menuName);
if (accelerator!=null) a.putValue(Action.ACCELERATOR_KEY, Utils.getKeyStroke(accelerator));
if (icon!=null) a.putValue(Action.SMALL_ICON, icon);

actions.put(actionName, a);
return a;
}
private JMenu createJMenu (String menuName) {
String label = lng("menu."+menuName, menuName);
int mnemonic = 0;
int k = label.indexOf("&");
if (k != -1) {
mnemonic = label.toUpperCase().charAt(k+1) -'A' + KeyEvent.VK_A;
label = label.substring(0,k)+label.substring(k+1);
}

JMenu m = new JMenu(label);
m.setName(menuName);
m.setMnemonic(mnemonic);
return m;
}
private JMenuItem createJMenuItem (String args, String menuName) {
String[] tab = args.split(" ");
Action a = getAction(tab[0]);
if (a==null) a = createAction(tab, menuName);
JMenuItem item = new JMenuItem(a);
return item;
}

private JMenu loadMenu (BufferedReader br, int o) throws IOException {
String menustr = "menu";
for (int i=0; i < o; i++) menustr = "sub" + menustr;

JMenu menu = null;
String str = "", menuName = null;
//br.mark(Integer.MAX_VALUE);
while ((str=br.readLine())!=null) {
str = str.trim();
if (str.startsWith("#")) continue; // skip comments

if (str.length()<=0) {
if (menu!=null) return menu;
else continue;
}

if (str.toLowerCase().startsWith(menustr)) {
if (menu==null) menu = createJMenu(menuName = str.substring(5).trim());
else {
br.reset();
JMenu submenu = loadMenu(br, o+1);
menu.add(submenu);
}}
else menu.add(createJMenuItem(str, menuName));

//br.mark(Integer.MAX_VALUE);
}
return menu;
}
public ActionHandler getAction (String s) { return (ActionHandler)actions.get(s); }
public JMenu getMenu (String name) {
for (int i=0, n=menubar.getMenuCount(); i<n; i++) {
JMenu menu = menubar.getMenu(i);
if (menu.getName().equalsIgnoreCase(name)) return menu;
}
return null;
}
public void windowClosing (WindowEvent _e) { 
try {
line.stop();
recv.close();
synth.close();
line.close();
} catch (Exception e) { e.printStackTrace(); }
System.exit(0); 
}
public void uncaughtException (Thread thr, Throwable t) {
t.printStackTrace();
/*String msg =
"An unhandled error has happened. The program may not continue safely, what would you like to do ?"
+ "\r\n* Click Yes if you want to quit immediately, losing all unsaved work."
+ "\r\nClick No if you want to continue working, but the program might have unexpected behaviors."
+ "\r\nWe are very sorry for the inconvenience."
+ "\r\n\r\nException info :\r\n"
+ t.getClass().getName() + " : " + t.getLocalizedMessage()
+ "\r\nFurther information about this exception has been put into stdout.txt.";
int n = JOptionPane.showConfirmDialog(null, msg, "Uncaught Exception", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);*/

while (true) {
String msg = "An unhandled error has happened. What do you want to do ?\r\n\r\n"
+ "* Click «Abort» to immediately quit. All unsaved work will be lost.\r\n"
+ "* Click «Ignore» to return to your work. The program may not continue safely, and could have unexpected behaviors, possibly causing any damage.\r\n"
+ "\r\nIf this message keeps showing up, please contact the developpers. More informations about this problem has been put on stdout.txt such as the complete stack trace, which you can also view by clicking on «Technical details...»."
+ "\r\n\r\nWe are very sorry for the inconvenience.";
String options[] = {"Abort", "Ignore", "Technical details..."};
int n = JOptionPane.showOptionDialog(null, msg, "Uncaught Exception", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);

if (n == 0) System.exit(0);
else if (n==1) break;
}

}
public void fireTreeChanged () {
((DefaultTreeModel)tree.getModel()).nodeStructureChanged(treestruct);
}


public ArrayList<Object> findSample (SF2Sample s) {
ArrayList<Object> result = new ArrayList<Object>();
for (SF2Layer lx : bank.getLayers()) {
for (SF2LayerRegion l : lx.getRegions()) {
if (l.getSample().equals(s)) {
result.add(lx);
result.add(l);
}}}
return result;
}
public ArrayList<Object> findLayer (SF2Layer s) {
ArrayList<Object> result = new ArrayList<Object>();
for (SF2Instrument i : bank.getInstruments()) {
for (SF2InstrumentRegion l : i.getRegions()) {
if (l.getLayer().equals(s)) {
result.add(i);
result.add(l);
}}}
return result;
}

public void valueChanged (TreeSelectionEvent tse) {
}
public void action (String s) {
if (s.equals("exit")) windowClosing(null);
else if (s.equals("midiSend")) {
int type = Integer.parseInt(midiMsgType.getSelectedItem().toString().substring(0,3));
int data1 = Integer.parseInt(midiData1.getText());
int data2 = Integer.parseInt(midiData2.getText());

try {
ShortMessage m = new ShortMessage();
if (type==192) {
Instrument ins = bank.getInstrument(new Patch(data2, data1));
if (ins!=null) synth.loadInstrument(ins);
m.setMessage(176, 0, 0, data2);
recv.send(m, -1);
m.setMessage(176, 0, 32, 0);
recv.send(m, -1);
m.setMessage(192, 0, data1, 0);
recv.send(m, -1);
}
else {
m.setMessage(type, 0, data1, data2);
recv.send(m, -1);
}
} catch (Exception e) { e.printStackTrace(); }

}

// other commands
}
}


/* notes/remembers
showMessageDialog(parent, message, title, messageType)
showConfirmDialog(parent, message, title, optionType, messageType)
*/