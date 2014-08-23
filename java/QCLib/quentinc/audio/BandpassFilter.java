package quentinc.audio;
/**
Simple band pass filter. Formulas taken from the Internet, and thus perhaps not exact.
*/
public class BandpassFilter extends AbstractDSPFilter {
public BandpassFilter (AudioManager mgr) { super(mgr); }
protected void updateCoefficiants () {
double theta = 2 * Math.PI * f;
double beta = (0.5 * (1.0 - Math.tan(theta / (2.0 * q))) / (1.0 + Math.tan(theta / (2.0 * q))));
double alpha = ((0.5 - beta) / 2);
double gamma = ((0.5 + beta) * Math.cos(theta));
b0 = (float)(2 * alpha);
b1 = 0;
b2 = -b0;
a1 = (float)(-2 * gamma);
a2 = (float)( 2 * beta );
}
public void setQuality (float q) { super.setQuality(q); }
public float getQuality () { return q; }
}
