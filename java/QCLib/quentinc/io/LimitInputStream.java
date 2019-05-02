package quentinc.io;
import java.io.*;

public class LimitInputStream extends FilterInputStream {
private int limit;
private int count;
private int markCount;
public LimitInputStream (InputStream i, int l) {
super(i);
limit=l;
count=0;
markCount = 0;
}
@Override public int read () throws IOException {
if (++count>limit) return -1;
return super.read();
}
@Override public int read (byte[] b, int of, int len) throws IOException {
if (count>=limit) return -1;
int n = super.read(b, of, Math.min(len, limit-count));
if (n>0) count+=n;
return n;
}
@Override public long skip (long l) throws IOException {
l = super.skip(Math.min(l, limit-count));
count += (int)l;
return l;
}
@Override  public void mark (int n) {
markCount=count;
super.mark(n);
}
@Override public void reset () throws IOException {
super.reset();
count = markCount;
}


public int getLimit () { return limit; }
public void setLimit (int l) { limit=l; }
public int getCount () { return count; }
}
