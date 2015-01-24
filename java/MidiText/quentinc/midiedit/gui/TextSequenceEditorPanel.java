package quentinc.midiedit.gui;
import quentinc.midiedit.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import quentinc.midiedit.*;
import javax.sound.midi.*;
import java.util.regex.*;

public class TextSequenceEditorPanel extends JPanel {
boolean includeSrc = true;
int selStart=0, selEnd=0, curVoice = 0, curProgram = 0, curTempo = 0;
String find = "", repl = "";
JTextArea ta = new JTextArea();
JFWAPI jfw = new JFWAPI();

public TextSequenceEditorPanel () {
setLayout(new BorderLayout());
add(new JScrollPane(ta), BorderLayout.CENTER);
ta.setText("");

ta.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_MASK), "putTimeDif");
ta.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK), "recompile");
ta.getActionMap().put("recompile", new AbstractAction(){
public void actionPerformed (ActionEvent e) {
getSequence();
}});
ta.getActionMap().put("putTimeDif", new AbstractAction(){
public void actionPerformed (ActionEvent e) {
putTimeDif();
}});
/*ta.addKeyListener(new KeyAdapter(){
public void keyPressed (KeyEvent e) {
if (e.getKeyCode()==KeyEvent.VK_BACK_SPACE) {
int pos = ta.getCaretPosition();
if (pos<=0) return;
char ch = ta.getText().charAt(pos -1);
//jfw.sayString(""+ch,true);
}
}});*/

}

public String getText () { return ta.getText(); }
public void setText (String s) { ta.setText(s); }
public void copy () { ta.copy(); }
public void cut () { ta.cut(); }
public void insert (String s) { ta.insert(s, ta.getCaretPosition()); }
public void paste () { ta.paste(); }
public void deleteSelection () { ta.replaceSelection(""); }
public void replace (String regex, String repl) {
find=regex; this.repl=repl;
ta.setText(ta.getText().replaceAll(regex, repl));
}
public void nextVoice () { findNext("\\[v"); }
public void prevVoice () { findPrev("\\[v"); }
public String getFindString () { return find; }
public String getReplString () { return repl; }
public boolean find (String regex) {
find = regex;
repl = "";
return findNext();
}
public boolean findNext () { return findNext(find); }
public boolean findPrev () { return findPrev(find); }
private boolean findNext (String find) {
try {
Matcher m = Pattern.compile(find, Pattern.DOTALL).matcher(ta.getText());
if (m.find(ta.getCaretPosition())) {
ta.select(m.start(), m.end());
return true;
}
} catch (Exception e) { e.printStackTrace(); }
return false;
}
private boolean findPrev (String find) {
try {
Matcher m = Pattern.compile(find, Pattern.DOTALL).matcher(ta.getText());
int limit = ta.getCaretPosition(), cur = limit, cure = cur;
while (m.find() && m.start()<limit -1) { cur=m.start(); cure = m.end(); }
if (cur!=limit && cure-cur>0) {
ta.select(cur, cure);
return true;
}
} catch (Exception e) { e.printStackTrace(); }
return false;
}
public void replaceSelection (String s) { 
int strt = ta.getSelectionStart(), end = ta.getSelectionEnd();
ta.replaceRange(s,strt,end);
ta.select(strt, strt+s.length());
}
public void transposeSelection (int n) { replaceSelection(transpose(ta.getSelectedText(), n)); }
public static String transpose (String s, int transposition) {
StringBuffer sb = new StringBuffer();
Matcher m = MidiTextCompiler.getRegex("\\S+").matcher(s);
while (m.find()) {
m.appendReplacement(sb, transposeUnique(m.group(), transposition));
}
return (sb = m.appendTail(sb)).toString();
}
public static String transposeUnique (String s, int transposition) {
Matcher m = MidiTextCompiler.getRegex("^(-?\\d*)([CDEFGABcdefgab][#b]?)(\\d*|/\\d*|\\d+/\\d*)(\\*?)$").matcher(s);
if (m.find()) {
String octS = m.group(1), noteS = m.group(2), durS = m.group(3), durStartS = m.group(4);
int note = MidiTextCompiler.parseNote(noteS);
int oct = (octS.equals("")? 0 : Integer.parseInt(octS));
note += transposition + oct*12;
noteS = MidiTextDecompiler.NOTES[note%12];
if (note<60) {
noteS = noteS.toUpperCase();
oct = (note -59)/12;
}
else {
noteS = noteS.toLowerCase();
oct = (note -60)/12;
}
octS = (oct==0? "" : String.valueOf(oct));
String repl = octS + noteS + durS + durStartS;
return repl;
}
return s;
}
public int getSelectionVoice () { return curVoice; }
public int getSelectionProgram () { return curProgram; }
public int getSelectionTempo () { return curTempo; }
public int getTickSelectionStart () { return selStart; }
public int getTickSelectionEnd () { return selEnd; }
public Sequence getSequence () {
int start = ta.getSelectionStart(), end = ta.getSelectionEnd();
int tab[] = { start, -1, -1, -1, end, -1, -1, -1  };
MidiTextCompiler mtc = new MidiTextCompiler();
mtc.setIncludeSource(includeSrc);
Sequence s = mtc.compile(ta.getText(), tab);
selStart = tab[0];
curVoice = tab[1];
curProgram = tab[2];
curTempo = tab[3];
selEnd = tab[4];
return s;
}
public void setSequence (Sequence s) {
if (s==null) { ta.setText(""); return; }
try {
Track[] tracks = s.getTracks();
for (int k=0; k < tracks.length; k++) {
for (int i=0; i < tracks[k].size(); i++) {
MidiMessage msg = tracks[k].get(i).getMessage();
if (msg instanceof MetaMessage) {
MetaMessage m = (MetaMessage)msg;
if (m.getType()==MidiTextCompiler.META_COMPILER_TEXT) {
byte buf[] = m.getData();
String data = new String(buf, 0, buf.length, "iso-8859-1");
ta.setText(data);
return;
}}}}

// Decompilation is needed
MidiTextDecompiler mtd = new MidiTextDecompiler();
String result = mtd.decompile(s);
ta.setText(result);

} catch (Exception e) { 
e.printStackTrace(); 
ta.setText("// Exception occured during process"); 
}
}

public void requestFocus () { ta.requestFocus(); }
public void insertPatchChange (int bank, int program) {
int value = (bank<<7)+(program&0x7F);
ta.insert("p" + value + " ", ta.getCaretPosition());
}
public JTextArea getTextArea () { return ta; }
public int getCaretPosition () {
return ta.getCaretPosition();
}

private void putTimeDif () {
int last = selStart;
Sequence s = getSequence();
int dif = last - selStart ;

int k = Math.abs(dif), l = s.getResolution();
int _t[]={2,3,5,7,11};
for (int t : _t) {
while (k%t==0 && l%t==0) { k/=t; l/=t; }
}

StringBuilder sb = new StringBuilder();
sb.append(dif>0? 's':'r');
if (l==1) sb.append(k);
else if (k==1) sb.append('/').append(l);
else sb.append(k).append('/').append(l);
sb.append(' ');
ta.insert(sb.toString(), ta.getCaretPosition());
}


public String getOption (String s) {
if (s.equals("includeSource")) return String.valueOf(includeSrc);
return null;
}
public void setOption (String n, String v) {
if (n.equals("includeSource")) includeSrc = Boolean.valueOf(v);
}

}