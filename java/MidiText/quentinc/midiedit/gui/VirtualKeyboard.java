package quentinc.midiedit.gui;
import quentinc.midi.*;
import javax.sound.midi.*;
import java.util.*;
import java.awt.event.*;
import static java.awt.event.KeyEvent.*;

public class VirtualKeyboard extends KeyAdapter {
private static Map<Integer,Integer> keymap = new HashMap<Integer,Integer>();
Collection<Integer> notesOn = new HashSet<Integer>();
Receiver recv1, recv2;
int vc;

static {
keymap.put( VK_LESS, 60);
keymap.put( VK_A, 61);
keymap.put( VK_Y, 62);
keymap.put( VK_S, 63);
keymap.put( VK_X, 64);
keymap.put( VK_C, 65);
keymap.put( VK_F, 66);
keymap.put( VK_V, 67);
keymap.put( VK_G, 68);
keymap.put( VK_B, 69);
keymap.put( VK_H, 70);
keymap.put( VK_N, 71);
keymap.put( VK_M, 72);
keymap.put( VK_K, 73);
keymap.put( VK_COMMA, 74 );
keymap.put( VK_L, 75 );
keymap.put( VK_PERIOD, 76 );
keymap.put( VK_UNDERSCORE, 77 );
keymap.put( VK_Q, 72);
keymap.put( VK_2, 73);
keymap.put( VK_W, 74);
keymap.put( VK_3, 75);
keymap.put( VK_E, 76);
keymap.put( VK_R, 77);
keymap.put( VK_5, 78);
keymap.put( VK_T, 79);
keymap.put( VK_6, 80);
keymap.put( VK_Z, 81 );
keymap.put( VK_7, 82);
keymap.put( VK_U, 83);
keymap.put( VK_I, 84 );
keymap.put( VK_9, 85 );
keymap.put( VK_O, 86 );
keymap.put( VK_0, 87 );
keymap.put( VK_P, 88 );
}
public void setVoice (int n) { vc=n; }
public void setReceivers (Receiver a, Receiver b) {
recv1 = a; 
recv2 = b;
}
public void noteOn (int note) {
if (notesOn.contains(note)) return;
try {
ShortMessage sm = new ShortMessage();
sm.setMessage( 0x90, vc, note, 127 );
if (recv1!=null) recv1.send(sm,-1);
if (recv2!=null) recv2.send(sm,-1);
notesOn.add(note);
} catch (InvalidMidiDataException e) {}
}
public void noteOff (int note) {
try {
ShortMessage sm = new ShortMessage();
sm.setMessage( 0x80, vc, note, 127 );
if (recv1!=null) recv1.send(sm,-1);
if (recv2!=null) recv2.send(sm,-1);
notesOn.remove(note);
} catch (InvalidMidiDataException e) {}
}
public void keyPressed (KeyEvent e) {
int c = e.getKeyCode(), m = e.getModifiers();
if (m==0 && keymap.containsKey(c)) noteOn(keymap.get(c));
}
public void keyReleased (KeyEvent e) {
int c = e.getKeyCode(), m = e.getModifiers();
if (m==0 && keymap.containsKey(c)) noteOff(keymap.get(c));
}
public void keyTyped  (KeyEvent e) {
e.consume();
}

}
