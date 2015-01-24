package quentinc.midiedit.gui;
import com.sun.media.sound.*;
import quentinc.util.*;
import quentinc.midiedit.*;
import quentinc.midi.*;
import quentinc.midi.spi.*;
import quentinc.misc.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.regex.*;
import java.util.Properties;
import com.sun.media.sound.*;
import javax.sound.sampled.*;
import javax.sound.midi.*;
import javax.sound.midi.spi.*;

public class Main extends WindowAdapter implements Thread.UncaughtExceptionHandler {
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

JLabel statusbar = new JLabel("");
boolean lastFindWasRegex = false;
TextSequenceEditorPanel see = null;
MIDIManager midi = null;
MidiRecorder recorder = null;
VirtualKeyboard virtualKeyboard = null;
File curFile = null;
int curFileType = 0;

public static void main (String[] args) { new Main(args); }
public Main (String... args) {
Thread.setDefaultUncaughtExceptionHandler(this);

try {
PrintStream ps = new PrintStream(new BufferedOutputStream(new StderrDebugOutputStream(new FileOutputStream("stdout.txt"), 262144)), true);
System.setOut(ps);
System.setErr(ps);
} catch (Exception e) { e.printStackTrace(); }

InputStream sin = null;
try {
sin = new BufferedInputStream(new FileInputStream("midiedit-config.properties"));
settings.load(sin);
sin.close();
} catch (IOException ioe) {
try {
sin = new BufferedInputStream(getClass().getResourceAsStream("/midiedit-config.properties"));
settings.load(sin);
sin.close();
} catch (IOException e) {
System.out.println("No config file found, fallback to default config");
e.printStackTrace();
}}

String deflocale[] = param("language", "en-US").split("-",3);
Locale loc = new Locale(deflocale[0], (deflocale.length>1? deflocale[1] : ""), (deflocale.length>2? deflocale[2] : ""));

Locale.setDefault(loc);
JComponent.setDefaultLocale(loc);
strings = ResourceBundle.getBundle("midiedit-lang", loc);

BasicDialog.setOKButtonCaption(lng("ok"));
BasicDialog.setCancelButtonCaption(lng("cancel"));

BufferedReader in = null;
try { try {
in = new BufferedReader(new FileReader("midiedit-menubar.properties"));
} catch (FileNotFoundException fnfe) {
in = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/midiedit-menubar.properties")));
}
JMenu m;
while ((m=loadMenu(in,0))!=null) menubar.add(m);
in.close();
} catch (IOException e) { e.printStackTrace(); }

win = new JFrame();
win.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
win.addWindowListener(this);
win.getContentPane().setLayout(new BorderLayout());
win.setJMenuBar(menubar);

try {
see = new TextSequenceEditorPanel();
win.getContentPane().add(see, BorderLayout.CENTER);
win.getContentPane().add(statusbar, BorderLayout.SOUTH);

AudioFormat format = new AudioFormat(Integer.parseInt(param("synthSampleRate", "44100")), Integer.parseInt(param("synthSampleSize", "16")), Integer.parseInt(param("synthChannels", "2")), true, false);
int bufferSize = Integer.parseInt(param("synthBufferLength", "200")) * (int)format.getSampleRate() * format.getSampleSizeInBits() * format.getChannels() / 8000;

Map<String,Object> paramMap = new HashMap<String,Object>();
paramMap.put("format", format);
paramMap.put("latency", 1000L * Long.parseLong(param("synthBufferLength", "200")));
paramMap.put("interpolation", param("synthInterpolation", "linear"));
paramMap.put("max polyphony", Integer.parseInt(param("synthMaxPolyphony", "64")));
paramMap.put("control rate", (float)Double.parseDouble(param("synthControlRate", "147")));
paramMap.put("auto gain control", param("synthAutoGainControl", "false").equals("true"));

sin = null;
try {
sin = new FileInputStream("soundbanks.properties");
} catch (FileNotFoundException fnfe) {
sin = getClass().getResourceAsStream("/soundbanks.properties");
}
midi = new MIDIManager(format, bufferSize, paramMap, sin);
sin.close();
} catch (Exception e) { e.printStackTrace(); System.exit(0); }


if (args.length>0) {
String fn = args[0], fnx = fn.toLowerCase();
if (fnx.endsWith(".mid") || fnx.endsWith(".miz") || fnx.endsWith(".rmi")) openSequence(new File(fn), fnx.substring(fnx.length() -3, fnx.length()));
else openText(new File(fn), "txt");
}

win.pack();
win.setVisible(true);
updateWindowTitle();
}
public String param (String n) { return param(n,null); }
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
if (menu==null) {
menu = createJMenu(menuName = str.substring(5).trim());
}}
else if (str.toLowerCase().startsWith("sub" + menustr)) {
br.reset();
JMenu submenu = loadMenu(br, o+1);
menu.add(submenu);
}
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
public void windowClosing (WindowEvent exc) { 
try {
midi.close();
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
private void findReplace () {
TextSequenceEditorPanel tsep = (TextSequenceEditorPanel)see;
JTextField search = new JTextField(tsep.getFindString()), repl = new JTextField(tsep.getReplString());
JCheckBox cbRepl = new JCheckBox(lng("find.replace")), cbRegex = new JCheckBox(lng("find.regex"));
JPanel p = new JPanel();
p.setLayout(new GridLayout(0,2));
p.add(new JLabel(lng("find.search") + " : "));
p.add(search);
p.add(cbRepl);
p.add(repl);
p.add(cbRegex);
JPanel z = new JPanel();
z.setLayout(new FlowLayout());
z.add(p);
cbRegex.setSelected(lastFindWasRegex);
cbRepl.setSelected(tsep.getReplString().length()>0);

if (BasicDialog.showDialog(win, lng("find.dlgtitle"), z)) {
String find = search.getText(), repls = repl.getText();
if (find.length()<=0) return;
if (!(lastFindWasRegex=cbRegex.isSelected())) find = Pattern.quote(find);
if (cbRepl.isSelected()) tsep.replace(find, repls);
else tsep.find(find);
}

}
private void updateWindowTitle () {
if (curFile==null) win.setTitle(lng("windowtitle1"));
else win.setTitle(String.format(lng("windowtitle2"), curFile.getName()));
}
public void open () {
final Thread[] t1 = new Thread[1];
JFileChooser f = new JFileChooser();
f.setDialogTitle(lng("filedialog.opentitle"));
f.setCurrentDirectory(new File(param("defaultDirectory")));
f.setAcceptAllFileFilterUsed(false);
f.addChoosableFileFilter(new FileNameExtensionFilter(lng("filetype.mid"), "mid"));
if (Utils.classExists("quentinc.midi.spi.MIZFileReader")) f.addChoosableFileFilter(new FileNameExtensionFilter(lng("filetype.miz"), "miz"));
f.addChoosableFileFilter(new FileNameExtensionFilter(lng("filetype.txt"), "txt"));
f.setFileFilter(f.getChoosableFileFilters()[0]);
final JCheckBox autoplay = new JCheckBox(lng("filedialog.autoplayCb"));
autoplay.setSelected(true);
f.setAccessory(autoplay);
f.addPropertyChangeListener(new java.beans.PropertyChangeListener(){
public void propertyChange (java.beans.PropertyChangeEvent pce) {
if (pce.getPropertyName().equals("SelectedFileChangedProperty")) {
if (!autoplay.isSelected()) return;
midi.stop();
if (t1[0]!=null) t1[0].interrupt();
t1[0] = null;
final Object o = pce.getNewValue();
if (o==null) return;
t1[0] = new Thread(new Runnable(){public void run(){
try { Thread.currentThread().sleep( 2000 ); } catch (InterruptedException e) { return; }
File f;
if (o instanceof File) f = (File)o;
else f = new File(o.toString());
if (f.isDirectory() || !f.isFile() || !f.exists()) return;

String fn = f.getName().toLowerCase();
int fnidx = fn.lastIndexOf(".");
if (fnidx<0) fnidx=fn.length() -1;
String ext = fn.substring(fnidx);

Sequence s = null;
try {
if (ext.equals(".mid")) s = MidiSystem.getSequence(f);
else if (ext.equals(".miz")) s = (new MIZFileReader()).getSequence(f);
else if (ext.equals(".txt")) {
BufferedReader br = new BufferedReader(new FileReader(f));
StringBuffer sb = new StringBuffer();
String l;
while ((l=br.readLine())!=null) sb.append(l).append("\r\n");
br.close();
l = sb.toString();
s = (new MidiTextCompiler()).compile(l);
}
else return; // file extension not recognized

if (s!=null) {
midi.setSequence(s);
midi.setLoopEndPoint(s.getTickLength());
midi.setLoopStartPoint(0);
midi.start();
}
} catch (Exception e) { e.printStackTrace(); }
}}); t1[0].start();
}}
});

for (String acs : (new String[]{ "playRestart", "playForward", "playBackward", "playMoreForward", "playMoreBackward", "stop", "volumeUp", "volumeDown", "speedUp", "speedDown", "speedNormal"})) {
Action a = actions.get(acs); 
if (a==null) continue;
Object o1 = a.getValue(Action.ACCELERATOR_KEY);
if (o1==null  || !(o1 instanceof KeyStroke)) continue;
KeyStroke k = (KeyStroke)o1;

f.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(k, acs);
f.getActionMap().put(acs, a);
}


if (f.showOpenDialog(win)==JFileChooser.APPROVE_OPTION) {
String ext = ((FileNameExtensionFilter)f.getFileFilter()).getExtensions()[0];

if (ext.equals("txt")) openText(f.getSelectedFile(), ext);
else if (ext.equals("mid") || ext.equals("miz")) openSequence(f.getSelectedFile(), ext);
}
midi.stop();
((JComponent)see).requestFocus();
updateWindowTitle();
}
public void saveAs () {
final JFileChooser f = new JFileChooser();
f.setDialogTitle(lng("filedialog.savetitle"));
f.setCurrentDirectory(new File(param("defaultDirectory")));
f.setAcceptAllFileFilterUsed(false);
f.addChoosableFileFilter(new FileNameExtensionFilter(lng("filetype.mid"), "mid"));
if (Utils.classExists("quentinc.midi.spi.MIZFileWriter")) f.addChoosableFileFilter(new FileNameExtensionFilter(lng("filetype.miz"), "miz"));
f.addChoosableFileFilter(new FileNameExtensionFilter(lng("filetype.txt"), "txt"));
if (Utils.classExists("org.tritonus.sampled.convert.lame.Mp3LameFormatConversionProvider")) f.addChoosableFileFilter(new FileNameExtensionFilter(lng("filetype.mp3"), "mp3"));
if (Utils.classExists("org.tritonus.sampled.convert.jorbis.JorbisFormatConversionProvider")) f.addChoosableFileFilter(new FileNameExtensionFilter(lng("filetype.ogg"), "ogg"));
f.addChoosableFileFilter(new FileNameExtensionFilter(lng("filetype.wav"), "wav"));
f.addChoosableFileFilter(new FileNameExtensionFilter(lng("filetype.au"), "au"));
f.addChoosableFileFilter(new FileNameExtensionFilter(lng("filetype.aiff"), "aif", "aiff"));
f.setFileFilter(f.getChoosableFileFilters()[0]);

final Map<String,Object> map = new HashMap<String,Object>();
JButton optionsButton = new JButton(lng("filedialog.optionsBtn"));
f.setAccessory(optionsButton);
optionsButton.addActionListener(new ActionListener(){
public void actionPerformed (ActionEvent e) {
showFormatOptionsDialog(map, ((FileNameExtensionFilter)f.getFileFilter()).getExtensions()[0]);
}});

if (f.showSaveDialog(win)==JFileChooser.APPROVE_OPTION) {
new Thread(new Runnable(){
public void run(){
String ext = ((FileNameExtensionFilter)f.getFileFilter()).getExtensions()[0];
if (ext.equals("mid")) {
curFileType= 0; 
curFile = f.getSelectedFile();
save();
}
else if (ext.equals("miz")) {
curFileType= 126; 
curFile = f.getSelectedFile();
save();
}
else if (ext.equals("txt")) {
curFileType= -1; 
curFile = f.getSelectedFile();
save();
}
else {
saveAudio(f.getSelectedFile(), ext, map);
}
}}).start();
}
((JComponent)see).requestFocus();
updateWindowTitle();
}
public boolean openText (File f, String type) {
try {
String line = "", str = "";
BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
while ((line=br.readLine())!=null) str += line + "\n";
br.close();
((TextSequenceEditorPanel)see).setText(str);
curFile=f;
curFileType= -1;
return true;
} catch (Exception e) { e.printStackTrace(); }
return false;
}
public boolean openSequence (File f, String type) {
try {
Sequence s = null;
if (type.equals("miz")) s = (new MIZFileReader()).getSequence(f);
else s = MidiSystem.getSequence(f);
if (s==null) throw new NullPointerException();
see.setSequence(s);
curFile=f;
if (type.equals("miz")) curFileType = 126;
else curFileType = 0;
return true;
} catch (Exception e) { e.printStackTrace(); }
return false;
}
public boolean save () {
if (curFile==null) saveAs();
if (curFile==null) return true;

try {
switch (curFileType) {
case 0 : MidiSystem.write(see.getSequence(), 0, curFile); break;
case 126 : (new MIZFileWriter()).write(see.getSequence(), 126, curFile); break;
case -1 :
String str = ((TextSequenceEditorPanel)see).getText();
BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(curFile));
bos.write(str.getBytes("iso-8859-1"));
bos.flush();
bos.close();
break;
default : break;
}
return true;
} catch (Exception e) { e.printStackTrace(); }
return false;
}
public boolean saveAudio (File f, String type, Map<String,Object> map) {
try {
map.put("interpolation", param("synth2Interpolation", "linear"));
map.put("max polyphony", Integer.parseInt(param("synth2MaxPolyphony", "64")));
map.put("control rate", (float)Double.parseDouble(param("synth2ControlRate", "240")));
map.put("auto gain control", param("synth2AutoGainControl", "false").equals("true"));

if (map.get("format")==null) map.put("format", new AudioFormat(44100, 16, 2, true, false));
Sequence s = see.getSequence();
map.put("sequence", s);
map = midi.openSynthStream(map);
long len = (Long)map.get("streamlength");
AudioInputStream stream2 = (AudioInputStream)map.get("stream");
AudioInputStream stream = new AudioStreamProgress(stream2, stream2.getFormat(), len, lng("AudioStreamProgress"), this.win);
AudioFormat outf = (AudioFormat)(map.containsKey("output format")? map.get("output format") : null);
if (outf==null) {
if (type.equals("wav") || type.equals("au") || type.equals("aif")) outf = stream.getFormat();
else if (type.equals("mp3")) outf = new AudioFormat(org.tritonus.share.sampled.Encodings.getEncoding("MPEG1L3"), stream.getFormat().getSampleRate(), AudioSystem.NOT_SPECIFIED, stream.getFormat().getChannels(), AudioSystem.NOT_SPECIFIED, AudioSystem.NOT_SPECIFIED, false, map);
else if (type.equals("ogg")) {
if (System.getProperty("os.name").startsWith("Windows")) outf = stream.getFormat();
else outf = new AudioFormat(org.tritonus.share.sampled.Encodings.getEncoding("VORBIS"), stream.getFormat().getSampleRate(), AudioSystem.NOT_SPECIFIED, stream.getFormat().getChannels(), AudioSystem.NOT_SPECIFIED, AudioSystem.NOT_SPECIFIED, false, map);
}}
if (outf==null) return false;

if (!stream.getFormat().equals(outf)) stream = AudioSystem.getAudioInputStream(outf, stream);

AudioFileFormat.Type outt = null;
if (type.equals("wav")) outt = AudioFileFormat.Type.WAVE;
else if (type.equals("au")) outt = AudioFileFormat.Type.AU;
else if (type.equals("aif")) outt = AudioFileFormat.Type.AIFF;
else if (type.equals("mp3")) outt = org.tritonus.share.sampled.AudioFileTypes.getType("MP3", "mp3");
else if (type.equals("ogg")) outt = org.tritonus.share.sampled.AudioFileTypes.getType("Vorbis", "ogg");
if (outt==null) return false;

AudioSystem.write(stream, outt, f);
return true;
} catch (Exception e) { e.printStackTrace(); }
return false;
}
private void showFormatOptionsDialog (Map<String,Object> map, String type) {
if (type.equals("wav") || type.equals("au") || type.equals("aif")) {
JComboBox sampleRate, encoding, channels;
sampleRate = new JComboBox(new Integer[]{8000, 11025, 12000, 16000, 22050, 24000, 32000, 44100, 48000, 64000, 88200, 96000});
sampleRate.setSelectedItem(44100);
sampleRate.setEditable(true);
channels = new JComboBox(new String[]{"Mono", "Stereo"});
channels.setSelectedIndex(1);
AudioFormat def = new AudioFormat(44100, 16, 2, true, false);
encoding = new JComboBox();
encoding.addItem("PCM signed 8 bits");
encoding.addItem("PCM signed 16 bits");
if (AudioSystem.isConversionSupported(new AudioFormat(44100, 24, 2, true, false), new AudioFormat(44100, 16, 2, true, false))) encoding.addItem("PCM signed 24 bits");
if (AudioSystem.isConversionSupported(new AudioFormat(44100, 32, 2, true, false), new AudioFormat(44100, 16, 2, true, false)))  encoding.addItem("PCM signed 32 bits");
encoding.addItem("PCM unsigned 8 bits"); 
encoding.addItem("PCM unsigned 16 bits"); 
if (AudioSystem.isConversionSupported(new AudioFormat(44100, 24, 2, false, false), new AudioFormat(44100, 16, 2, false, false)))   encoding.addItem("PCM unsigned 24 bits"); 
if (AudioSystem.isConversionSupported(new AudioFormat(44100, 32, 2, false, false), new AudioFormat(44100, 16, 2, false, false)))    encoding.addItem("PCM unsigned 32 bits"); 
if (type.equals("wav")) {
if (AudioSystem.isConversionSupported(AudioFloatConverter.PCM_FLOAT, new AudioFormat(44100, 32, 2, true, false))) encoding.addItem("IEEE Float 32 bits");
}
if (AudioSystem.isConversionSupported(AudioFormat.Encoding.ALAW, def)) encoding.addItem("Alaw 8 bits");
if (AudioSystem.isConversionSupported(AudioFormat.Encoding.ULAW, def)) encoding.addItem("µlaw 8 bits");
encoding.setSelectedIndex(1);
JCheckBox cbEndianess = new JCheckBox("Big endian");

JPanel p = new JPanel(new GridLayout(0,2));
p.add(new JLabel(lng("format.sampleRate") + " : "));
p.add(sampleRate);
p.add(new JLabel(lng("format.encoding") + " : "));
p.add(encoding);
p.add(new JLabel(lng("format.channels") + " : "));
p.add(channels);
p.add(cbEndianess);
JPanel z = new JPanel(new FlowLayout());
z.add(p);

if (BasicDialog.showDialog(win, lng("format.dlgtitle"), z)) {
int sr = Integer.parseInt(sampleRate.getSelectedItem().toString());
int ch = channels.getSelectedIndex() +1;
String enc = encoding.getSelectedItem().toString();
AudioFormat synthFormat=null, outFormat=null;

if (enc.startsWith("PCM unsigned")) {
int k = enc.indexOf("bits");
k = Integer.parseInt(enc.substring(13, k -1).trim());
synthFormat = outFormat = new AudioFormat(sr, k, ch, false, cbEndianess.isSelected());
}
else if (enc.startsWith("PCM signed")) {
int k = enc.indexOf("bits");
k = Integer.parseInt(enc.substring(11, k -1).trim());
synthFormat = outFormat = new AudioFormat(sr, k, ch, true, cbEndianess.isSelected());
}
else if (enc.startsWith("IEE Float")) {
synthFormat = new AudioFormat(sr, 32, ch, true, false);
outFormat = new AudioFormat(AudioFloatConverter.PCM_FLOAT, sr, 32, ch, ch*4, ch*sr*4, false);
}
else if (enc.startsWith("Alaw")) {
synthFormat = new AudioFormat(sr, 16, ch, true, false);
outFormat = new AudioFormat(AudioFormat.Encoding.ALAW, sr, 8, ch, ch, ch*sr, false);
}
else if (enc.startsWith("µlaw")) {
synthFormat = new AudioFormat(sr, 16, ch, true, false);
outFormat = new AudioFormat(AudioFormat.Encoding.ULAW, sr, 8, ch, ch, ch*sr, false);
}
map.put("format", synthFormat);
map.put("output format", outFormat);
}}
else if (type.equals("mp3")) {
JComboBox sampleRate, bitrate, channels, quality;
sampleRate = new JComboBox(new Integer[]{8000, 11025, 12000, 16000, 22050, 24000, 32000, 44100, 48000, 64000, 88200, 96000});
sampleRate.setSelectedItem(44100);
channels = new JComboBox(new String[]{"Mono", "Stereo"});
channels.setSelectedIndex(1);
bitrate = new JComboBox(new Integer[]{64, 96, 128, 144, 160, 192, 256, 320});
bitrate.setSelectedItem(128);
quality = new JComboBox(lng("format.mp3qualities").split(","));
quality.setSelectedIndex(2);
JCheckBox vbr = new JCheckBox("VBR");

JPanel p = new JPanel(new GridLayout(0,2));
p.add(new JLabel(lng("format.sampleRate") + " : "));
p.add(sampleRate);
p.add(new JLabel(lng("format.bitrate") + " : "));
p.add(bitrate);
p.add(new JLabel(lng("format.channels") + " : "));
p.add(channels);
p.add(new JLabel(lng("format.quality") + " : "));
p.add(quality);
p.add(vbr);
JPanel z = new JPanel(new FlowLayout());
z.add(p);

if (BasicDialog.showDialog(win, lng("format.dlgtitle"), z)) {
int sr = Integer.parseInt(sampleRate.getSelectedItem().toString());
int ch = channels.getSelectedIndex() +1;
int br = Integer.parseInt(bitrate.getSelectedItem().toString());
int ql = quality.getSelectedIndex();
String qualt[] = {"lowest", "low", "middle", "high", "highest"};
						System.setProperty("tritonus.lame.bitrate", "" +br);
						System.setProperty("tritonus.lame.vbr", (vbr.isSelected()? "true":"false"));
						System.setProperty("tritonus.lame.quality", qualt[ql]);
map.put("bitrate", br);
map.put("mp3quality", qualt[ql]);
map.put("vbr", vbr.isSelected());
map.put("format", new AudioFormat(sr, 16, ch, true, false));
map.put("output format", new AudioFormat(org.tritonus.share.sampled.Encodings.getEncoding("MPEG1L3"), sr, AudioSystem.NOT_SPECIFIED, ch, AudioSystem.NOT_SPECIFIED, AudioSystem.NOT_SPECIFIED, false, map));
}}
else if (type.equals("ogg")) {
JComboBox sampleRate, channels, quality;
sampleRate = new JComboBox(new Integer[]{8000, 11025, 12000, 16000, 22050, 24000, 32000, 44100, 48000, 64000, 88200, 96000});
sampleRate.setSelectedItem(44100);
channels = new JComboBox(new String[]{"Mono", "Stereo"});
channels.setSelectedIndex(1);
quality = new JComboBox();
for (int i=0; i < 10; i++) quality.addItem(i);
quality.setSelectedIndex(6);
quality.setEditable(true);

JPanel p = new JPanel(new GridLayout(0,2));
p.add(new JLabel(lng("format.sampleRate") + " : "));
p.add(sampleRate);
p.add(new JLabel(lng("format.channels") + " : "));
p.add(channels);
p.add(new JLabel(lng("format.quality") + " : "));
p.add(quality);

JPanel z = new JPanel(new FlowLayout());
z.add(p);

if (BasicDialog.showDialog(win, lng("format.dlgtitle"), z)) {
int sr = Integer.parseInt(sampleRate.getSelectedItem().toString());
int ch = channels.getSelectedIndex() +1;
int ql = (int)Double.parseDouble(quality.getSelectedItem().toString());
map.put("quality", ql);
map.put("format", new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sr, 16, ch, 2*ch, 2*ch*sr, false, map));
if (System.getProperty("os.name").startsWith("Windows")) map.put("output format", map.get("format"));
else map.put("output format", new AudioFormat(org.tritonus.share.sampled.Encodings.getEncoding("VORBIS"), sr, AudioSystem.NOT_SPECIFIED, ch, AudioSystem.NOT_SPECIFIED, AudioSystem.NOT_SPECIFIED, false, map));
}}
else {
JOptionPane.showMessageDialog(win, lng("format.noDialog"), lng("format.noDialog.title"), JOptionPane.INFORMATION_MESSAGE);
}}
public void showPatchList () {
final Instrument[] insts = midi.getInstrumentList();
String data[] = new String[insts.length];
for (int i=0; i < insts.length; i++) {
Patch p = insts[i].getPatch();
data[i]= insts[i].getName() + " ("+p.getBank() + "," + p.getProgram() +")";
}

final JComboBox note = new JComboBox();
final JTextField dur = new JTextField("1000");
final JList list = new JList(data);
JButton listen = new JButton(lng("patchlist.listenBtn"));
String ntx[] = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
for (int i=0; i < 128; i++) {
int o = (i/12) -5;
int n = i%12;
note.addItem(o + ntx[n]);
}
note.setSelectedIndex(60);

final Action a = new AbstractAction(){
public void actionPerformed (ActionEvent e) {
midi.singleNoteOn(insts[list.getSelectedIndex()].getPatch(), note.getSelectedIndex(), Integer.parseInt(dur.getText()));
}};
list.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE,0), "singleNoteOn");
list.getActionMap().put("singleNoteOn", a);
listen.addActionListener(a);
note.addItemListener(new ItemListener(){
public void itemStateChanged (ItemEvent e) {
a.actionPerformed(null);
}});

JPanel p = new JPanel(new BorderLayout());
JPanel p1 = new JPanel(new FlowLayout());
p1.add(new JLabel(lng("patchlist.note") + " : "));
p1.add(note);
p1.add(new JLabel(lng("patchlist.duration") + " : "));
p1.add(dur);
p1.add(new JLabel(lng("patchlist.duration2")));
p1.add(listen);
p.add(p1, BorderLayout.SOUTH);
p.add(new JScrollPane(list), BorderLayout.CENTER);

if (BasicDialog.showDialog(win, lng("patchlist.dlgtitle"), p)) {
int n = list.getSelectedIndex();
Patch pa = insts[n].getPatch();
((TextSequenceEditorPanel)see).insertPatchChange(pa.getBank(), pa.getProgram());
}}
public void action (String s) {
if (s.equals("exit")) windowClosing(null);
else if (s.equals("stop")) {
if (midi.isRunning()) midi.stop(); else midi.start();
}
else if (s.equals("playRestart")) {
if (midi.isRunning()) midi.setTickPosition(0);
}
else if (s.equals("playForward")) midi.setTickPosition(midi.getTickPosition() + 4 * midi.getSequence().getResolution());
else if (s.equals("playMoreForward")) midi.setTickPosition(midi.getTickPosition() + 32 * midi.getSequence().getResolution());
else if (s.equals("playBackward")) midi.setTickPosition(midi.getTickPosition() -4 * midi.getSequence().getResolution());
else if (s.equals("playMoreBackward")) midi.setTickPosition(midi.getTickPosition() -32 * midi.getSequence().getResolution());
else if (s.equals("volumeUp")) midi.setVolume(midi.getVolume() +0.5f);
else if (s.equals("volumeDown")) midi.setVolume(midi.getVolume() -0.5f);
else if (s.equals("speedUp")) midi.setTempoFactor(midi.getTempoFactor() + 0.025f);
else if (s.equals("speedDown")) midi.setTempoFactor(Math.max(0.1f, midi.getTempoFactor() -0.025f));
else if (s.equals("speedNormal")) midi.setDefaultTempoFactor();
else if (s.equals("playRecord") && recorder!=null) {
if (midi.isRunning()) midi.stop();
recorder.stop();
Sequence sq = recorder.getSequence();
s = new MidiTextDecompiler().decompile(sq);
s = s.substring(1+s.indexOf(']')).trim();
see.insert(s);
recorder = null;
}	
else if (s.startsWith("play")) {
if (midi.isRunning()) midi.stop();
Sequence sq = null;
try {
sq = see.getSequence();
midi.setSequence(sq);
} catch (Exception e) { e.printStackTrace(); return; }
midi.setLoopStartPoint(0);
midi.setLoopEndPoint(sq.getTickLength());
if (s.equals("playFromStart")) midi.setTickPosition(0);
else if (s.equals("playFromCursor")) {
int pos = (Math.max(0, Math.min(see.getTickSelectionStart(), (int)sq.getTickLength())));
midi.setTickPosition(pos);
midi.setLoopStartPoint(pos);
}
else if (s.equals("playSelection")) {
int start = (Math.max(0, Math.min(see.getTickSelectionStart(), (int)sq.getTickLength()))),
end = (Math.max(0, Math.min(see.getTickSelectionEnd(), (int)sq.getTickLength())));
if (end-start <480) end = (int)sq.getTickLength();
midi.setLoopStartPoint(start);
midi.setLoopEndPoint(end);
midi.setTickPosition(start);
}
else if (s.equals("playRecord")) {
int pos = (Math.max(0, Math.min(see.getTickSelectionStart() -1920, (int)sq.getTickLength())));
int bpm = see.getSelectionTempo();
midi.setTickPosition(pos);
midi.setLoopStartPoint(pos);
recorder = new MidiRecorder( 8, 240000/bpm, bpm);
recorder.start();
if (virtualKeyboard!=null) virtualKeyboard.setReceivers(midi, recorder);
}
midi.setLoopCount(-1);
midi.start();
}
else if (s.equals("virtualKeyboard")) {
if (virtualKeyboard==null) {
Sequence sq = see.getSequence();
virtualKeyboard = new VirtualKeyboard();
virtualKeyboard.setVoice( see.getSelectionVoice() );
virtualKeyboard.setReceivers( midi, null );
see.getTextArea().addKeyListener( virtualKeyboard );
}
else {
see.getTextArea().removeKeyListener( virtualKeyboard );
virtualKeyboard = null;
}}
else if (s.equals("copy")) see.copy();
else if (s.equals("cut")) see.cut();
else if (s.equals("paste")) see.paste();
else if (s.equals("delete")) see.deleteSelection();
else if (s.equals("goToNextVoice")) see.nextVoice();
else if (s.equals("goToPrevVoice")) see.prevVoice();
else if (s.equals("findNext")) ((TextSequenceEditorPanel)see).findNext();
else if (s.equals("findPrev")) ((TextSequenceEditorPanel)see).findPrev();
else if (s.equals("find")) findReplace();
else if (s.equals("transposeUp")) ((TextSequenceEditorPanel)see).transposeSelection(1);
else if (s.equals("transposeDn")) ((TextSequenceEditorPanel)see).transposeSelection(-1);
else if (s.equals("save")) save();
else if (s.equals("saveAs")) saveAs();
else if (s.equals("open")) open();
else if (s.equals("new")) {
see.setSequence(null);
curFile=null;
}
else if (s.equals("insertProgramChange")) showPatchList();
else if (s.equals("about")) {
String version = "1.0 bêta 1";
JOptionPane.showMessageDialog(win, 
"MidiText\nVersion "+version+"\nCopyright © 2008, Cosendey Quentin\nSee http://quentinc.net/ for more details\n\n"
+ "This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation. \n"
+ "This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.",
lng("file.about"), 
JOptionPane.PLAIN_MESSAGE);
}

else if (s.equals("test123")) {
try {
Sequence sq = see.getSequence();
Patch[] p1 = sq.getPatchList();
List<SF2Instrument> l2 = new ArrayList<SF2Instrument>();
for (Patch p : p1) {
Instrument ins = midi.getInstrument(p);
if (!(ins instanceof SF2Instrument)) continue;
l2.add((SF2Instrument)ins);
}
SF2Instrument[] ll = l2.toArray(new SF2Instrument[0]);
l2=null;
SF2Soundbank bk = SF2Utils.createBank(ll);
BufferedOutputStream bs = new BufferedOutputStream( new FileOutputStream( "testautobank.sf2" ));
bk.save( bs );
bs.flush();
bs.close();
} catch (Exception e) { e.printStackTrace(); }}
// other actions
}
}


class StderrDebugOutputStream extends FilterOutputStream {
int count = 0, max = 15000;
public StderrDebugOutputStream (OutputStream os, int maxn) { super(os); max=maxn; }
public void write (int n) throws IOException { 
out.write(n); 
count++;
if (count>max) System.exit(0);
}
public void write (byte buf[], int of, int len) throws IOException {
out.write(buf,of,len);
count+=len;
if (count>max) System.exit(0);
}
public void write (byte b[]) throws IOException { write(b,0,b.length); }
}

/* notes/remembers
showMessageDialog(parent, message, title, messageType)
showConfirmDialog(parent, message, title, optionType, messageType)
*/