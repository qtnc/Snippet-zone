package quentinc.sf2compiler;
import com.sun.media.sound.*;
import javax.sound.sampled.*;
import javax.sound.midi.*;
import java.util.*;
import java.util.regex.*;
import java.io.*;

public class SF2Compiler {
private static Map<String,Integer> map = new HashMap<String,Integer>();
static {
map.put("overriding root key", 58);
map.put("root key", 58);
map.put("exclusive class", 57);
map.put("scale tuning", 56);
map.put("sample mode", 54);
map.put("fine tune", 52);
map.put("finetune", 52);
map.put("coarse tune", 51);
map.put("initial attenuation", 48);
map.put("attenuation", 48);
map.put("force velocity", 47);
map.put("force key number", 46);
map.put("velocity range", 44);
map.put("velrange", 44);
map.put("key range", 43);
map.put("keyrange", 43);
map.put("release", 38);
map.put("volenv release", 38);
map.put("release time", 38);
map.put("volenv release time", 3);
map.put("decay", 36);
map.put("decay time", 36);
map.put("volenv decay", 36);
map.put("volenv decay time", 36);
map.put("hold", 35);
map.put("hold time", 35);
map.put("volenv hold", 35);
map.put("volenv hold time", 35);
map.put("attack", 34);
map.put("attack time", 34);
map.put("volenv attack", 34);
map.put("volenv attack time", 34);
map.put("delay", 33);
map.put("delay time", 33);
map.put("volenv delay", 33);
map.put("volenv delay time", 33);
map.put("modenv release", 30);
map.put("modenv release time", 30);
map.put("modenv decay", 28);
map.put("modenv decay time", 28);
map.put("modenv hold", 27);
map.put("modenv hold time", 27);
map.put("modenv attack", 26);
map.put("modenv attack time", 26);
map.put("modenv delay", 25);
map.put("modenv delay time", 25);
map.put("modenv sustain", 29);
map.put("sustain", 37);
map.put("volenv sustain", 37);
map.put("vibrato rate", 24);
map.put("vibrato delay", 23);
map.put("vibrato depth", 6);
map.put("modlfo rate", 22);
map.put("modlfo delay", 21);
map.put("panning", 17);
map.put("pan", 17);
map.put("reverb", 16);
map.put("chorus", 15);
map.put("initial cutoff", 8);
map.put("cutoff", 8);
map.put("initial resonnance", 9);
map.put("resonance", 9);
map.put("modlfo volume", 13);
map.put("modlfo cutoff", 10);
map.put("modlfo pitch", 5);
map.put("modenv cutoff", 11);
map.put("modenv pitch", 7);
map.put("end coarse offset", 12);
map.put("start loop coarse offset", 45);
map.put("end loop coarse offset", 50);
}

private static void createSample (SF2Soundbank bank, File file, int loopStart, int loopEnd, int originalPitch, int pitchCorrection, String name, SF2LayerRegion curRegion, SF2Layer curLayer) throws Exception {
System.out.print("Adding " + file + " ... ");
SF2Sample s = new SF2Sample(bank);
if (name==null || name.length()<=0) name = file.getName();
s.setName(name);
s.setOriginalPitch(originalPitch);
s.setPitchCorrection((byte)pitchCorrection);
s.setSampleType(0);

AudioInputStream stream = AudioSystem.getAudioInputStream(file);
AudioFormat format = stream.getFormat();
if (!format.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED) || format.getSampleSizeInBits()>16) {
format = new AudioFormat(format.getSampleRate(), 16, format.getChannels(), true, format.isBigEndian());
stream = AudioSystem.getAudioInputStream(format, stream);
}

ByteArrayOutputStream bos = new ByteArrayOutputStream();
byte[] buf = new byte[1024];
int read = 0;
while ((read=stream.read(buf,0,buf.length))!= -1) bos.write(buf,0,read);
buf = bos.toByteArray();
bos=null;

if (format.getSampleSizeInBits()<16) {
byte[] buf2 = new byte[buf.length*2];
for (int i=0; i < buf.length; i++) buf2[i*2] = buf2[i*2 +1] = buf[i];
buf=buf2;
}
if (format.isBigEndian()) {
for (int i=0; i < buf.length; i+=2) {
byte tmp = buf[i+1];
buf[i+1] = buf[i];
buf[i] = tmp;
}}
if (format.getChannels()>1) {
int ch = format.getChannels();
byte buf2[] = new byte[buf.length / ch];
for (int i=0; i < buf2.length; i+=2) {
int val = 0;
for (int c=0; c < ch; c++) {
int k = (buf[i*ch + 2*c]&0xFF) + 256*(buf[i*ch + 2*c +1]);
val+=k;
}
val /= ch;
buf2[i] = (byte)(val&0xFF);
buf2[i+1] = (byte)(val>>8);
}
buf=buf2;
}

s.setSampleRate((int)format.getSampleRate());
s.setData(buf);
if (loopEnd < 0) loopEnd += buf.length/2;
if (loopStart < 0) loopStart += buf.length/2;
s.setStartLoop(loopStart);
s.setEndLoop(loopEnd);
bank.addResource(s);
curRegion.setSample(s);
System.out.println("OK, " + (buf.length/1024) + "K saved");
}

private static <T extends SoundbankResource> T findResource (T[] list, String str) {
for (T t : list) {
if (t.getName().equalsIgnoreCase(str)) return t;
}
return null;
}
public static void main (String args[]) {
if (args.length<2) System.exit(0);
try {
SF2Soundbank bank = new SF2Soundbank();
SF2Instrument curInst = null;
SF2Layer curLayer = null;
SF2LayerRegion curRegion = null;
SF2GlobalRegion curGlobalRegion = null;
SF2GlobalRegion curInstrumentGlobalRegion = null;
SF2Sample curSample = null;
File curSampleFile = null;
int loopStart = 0, loopEnd = -1, pitchCorrection = 0, originalPitch = 0;
String sampleName = "";
boolean newLayer = true;

BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(args[1]))));
String line = "";
int lineCount = 0;
while ((line=br.readLine())!=null) {
lineCount++;
line = line.trim();
if (line.length()<=0 || line.startsWith("#")) continue;
//System.out.println(line);

Matcher m = Pattern.compile(
"^instrument\\s*\"(.*?)\"\\s*(\\d+)\\s*,\\s*(\\d+)\\s*$"
).matcher(line);
if (m.find()) {
if (curSampleFile!=null || curSample!=null) {
if (curRegion==null) curRegion = new SF2LayerRegion();
if (curSampleFile!=null) createSample(bank, curSampleFile, loopStart, loopEnd, originalPitch, pitchCorrection, sampleName, curRegion, curLayer);
else if (curSample!=null) curRegion.setSample(curSample);
curLayer.getRegions().add(curRegion);
}
if (curLayer!=null) {
SF2InstrumentRegion region = new SF2InstrumentRegion();
if (newLayer) bank.addResource(curLayer);
curInst.getRegions().add(region);
region.setLayer(curLayer);
}
if (curInst!=null) bank.addInstrument(curInst);
curInst = new SF2Instrument(bank);
curInst.setName(m.group(1));
int _bank = Integer.parseInt(m.group(2)), program = Integer.parseInt(m.group(3));
curInst.setPatch(new ModelPatch(_bank<<7, program, _bank>=128));
//System.out.println(curInst);
curSampleFile = null;
curSample = null;
newLayer = true;
curLayer = null;
curRegion = null;
curGlobalRegion = null;
curInstrumentGlobalRegion = null;
continue;
}

m = Pattern.compile("^layer\\s*\"(.*?)\"\\s*$").matcher(line);
if (m.find()) {
if (curSampleFile!=null || curSample!=null) {
if (curRegion==null) curRegion = new SF2LayerRegion();
if (curSampleFile!=null) createSample(bank, curSampleFile, loopStart, loopEnd, originalPitch, pitchCorrection, sampleName, curRegion, curLayer);
else if (curSample!=null) curRegion.setSample(curSample);
curLayer.getRegions().add(curRegion);
}
if (curLayer!=null) {
SF2InstrumentRegion region = new SF2InstrumentRegion();
if (newLayer) bank.addResource(curLayer);
curInst.getRegions().add(region);
region.setLayer(curLayer);
}
curLayer = findResource(bank.getLayers(), m.group(1).trim());
if (curLayer==null) {
curLayer = new SF2Layer(bank);
curLayer.setName(m.group(1));
newLayer = true;
}
else newLayer = false;

curSampleFile = null;
curSample = null;
curRegion = null;
curGlobalRegion = null;
//System.out.println(curLayer);
continue;
}

m = Pattern.compile("^sample\\s+\"(.*?)\"\\s*(\".*?\")?\\s*$").matcher(line);
if (m.find()) {
if (curSampleFile!=null || curSample!=null) {
if (curRegion==null) curRegion = new SF2LayerRegion();
if (curSampleFile!=null) createSample(bank, curSampleFile, loopStart, loopEnd, originalPitch, pitchCorrection, sampleName, curRegion, curLayer);
else if (curSample!=null) curRegion.setSample(curSample);
curLayer.getRegions().add(curRegion);
}
curSampleFile = null;
curSample = null;
if (m.group(2)!=null && m.group(2).length()>0) {
File f = new File(m.group(2).replace("\"","").trim());
if (!f.exists()) continue;
curSampleFile = f;
}
else {
curSample = findResource(bank.getSamples(), m.group(1));
if (curSample==null) continue;
}
sampleName = m.group(1);
originalPitch = 60;
loopStart = 0;
loopEnd = -1;
pitchCorrection = 0;
curRegion = null;
}

m = Pattern.compile("^original pitch (\\d+)$").matcher(line);
if (m.find()) {
originalPitch = Integer.parseInt(m.group(1));
continue;
}
m = Pattern.compile("^pitch correction (-?\\d+)$").matcher(line);
if (m.find()) {
pitchCorrection = Integer.parseInt(m.group(1));
continue;
}
m = Pattern.compile("^loop from (-?\\d+) to (-?\\d+)$").matcher(line);
if (m.find()) {
loopStart = Integer.parseInt(m.group(1));
loopEnd = Integer.parseInt(m.group(2));
continue;
}

m = Pattern.compile("^(.*?)\\s*(\\d.*?)$").matcher(line);
if (m.find()) {
String key = m.group(1).trim(), value = m.group(2).trim();
SF2Region region = null;

if (curLayer==null) {
if (curInstrumentGlobalRegion==null) curInst.setGlobalZone(curInstrumentGlobalRegion = new SF2GlobalRegion());
region = curInstrumentGlobalRegion;
}
else if (curSampleFile==null && curSample==null) {
if (curGlobalRegion==null) curLayer.setGlobalZone(curGlobalRegion = new SF2GlobalRegion());
region = curGlobalRegion;
}
else {
if (curRegion==null) curRegion = new SF2LayerRegion();
region = curRegion;
}

if (region==null) continue;
int genNum = -1;
if (map.get(key)!=null) genNum = map.get(key);
if (genNum<0) continue;

if (value.matches("^-?\\d+\\s*,\\s*-?\\d+$")) {
int k = value.indexOf(","), a = Integer.parseInt(value.substring(0,k).trim()), b = Integer.parseInt(value.substring(k+1).trim());
region.putBytes(genNum, new byte[]{(byte)a,(byte)b});
}
else {
m = Pattern.compile("^(-?\\d+|-?\\d+\\.\\d+)([a-z]*)$").matcher(value);
if (m.find()) {
double d = Double.parseDouble(m.group(1));
String units = m.group(2).toLowerCase().trim();
if (units.equals("") || units.equals("cb"))  {}
else if (units.equals("%")) {
if (genNum==56 || genNum==52) {}
else d *= 10.0;
}
else if (units.equals("ms") || units.equals("msec")) d = 1200.0 * Math.log(d/1000.0) / Math.log(2);
else if (units.equals("s") || units.equals("sec")) d = 1200.0 * Math.log(d) / Math.log(2);
else if (units.equals("db")) d *= 10.0;
else if (units.equals("hz")) d = 1200.0 * Math.log(d / 8.176) / Math.log(2);
else if (units.equals("khz")) d = 1200.0 * Math.log((d * 1000.0) / 8.176) / Math.log(2);

System.out.println("Put generator " + genNum + " value " + (short)d);
region.putShort(genNum, (short)Math.round(d));
}}
continue;
}

// Other commands
System.out.println("Line " + lineCount + " : unkown command : " + line); //##
}
System.out.println("End of parsing");

if (curSampleFile!=null || curSample!=null) {
if (curRegion==null) curRegion = new SF2LayerRegion();
if (curSampleFile!=null) createSample(bank, curSampleFile, loopStart, loopEnd, originalPitch, pitchCorrection, sampleName, curRegion, curLayer);
else if (curSample!=null) curRegion.setSample(curSample);
curLayer.getRegions().add(curRegion);
}
if (curLayer!=null) {
SF2InstrumentRegion region = new SF2InstrumentRegion();
region.setLayer(curLayer);
curInst.getRegions().add(region);
if (newLayer) bank.addResource(curLayer);
}
if (curInst!=null) bank.addInstrument(curInst);

System.out.printf("%d instruments, %d layers, %d samples saved\r\n", bank.getInstruments().length, bank.getLayers().length, bank.getSamples().length);

bank.setName("Test");
bank.setVendor("QuentinC");
bank.setDescription("Test");
bank.save(new File(args[0]));

} catch (Exception e) { e.printStackTrace(); }
System.exit(0);
}}
