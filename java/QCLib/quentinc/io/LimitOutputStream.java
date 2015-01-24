package quentinc.io;
import java.io.*;

public class LimitOutputStream extends FilterOutputStream {
private int limit;
private int count;
public LimitOutputStream (OutputStream o, int l) {
super(o);
limit=l;
count=0;
}
@Override public void write (int n) throws IOException {
if (++count>limit) return;
super.write(n);
}
@Override public void write (byte[] b, int of, int len) throws IOException {
if (count>=limit) return;
int n = Math.min(len, limit-count);
out.write(b, of, n);
count += n;
}


public int getLimit () { return limit; }
public void setLimit (int l) { limit=l; }
public int getCount () { return count; }
}
