package quentinc.audio;
/**
Simple high pass filter. Formulas taken from the Internet, and thus perhaps not exact.
*/
public class HighpassFilter extends AbstractDSPFilter {
public HighpassFilter (AudioManager mgr) { super(mgr); }
protected void updateCoefficiants () {
double omega = 2 * Math.PI * f;
double cs = Math.cos(omega);
double alpha = Math.sin(omega) /2;
a0 = (float)( 1 + alpha );
b0 = (float)( (1 + cs) / 2 / a0 );
b1 = (float)( -(1 + cs) / a0 );
b2 = b0;
a1 = (float)( -2 * cs / a0 );
a2 = (float)( (1 - alpha) / a0 );
}
}
