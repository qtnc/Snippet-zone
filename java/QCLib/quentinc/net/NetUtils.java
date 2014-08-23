package quentinc.net;
import java.io.*;
import java.net.*;
public class NetUtils {
private NetUtils () {}

public static String getCurrentIP () {
try {
URL u = new URL("http://www.whatismyip.com/automation/n09230945.asp");
InputStream in = u.openStream();
BufferedReader br = new BufferedReader(new InputStreamReader(in));
String ipx = br.readLine().trim();
br.close(); in.close();
return ipx;
} catch (IOException e) { e.printStackTrace(); }
return getCurrentLocalIP();
}

public static String getCurrentLocalIP () {
try {
return InetAddress.getLocalHost().getHostAddress().toString();
} catch (UnknownHostException e) { e.printStackTrace(); }
return "127.0.0.1";
}

}
