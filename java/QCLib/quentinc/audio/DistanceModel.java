package quentinc.audio;
/**
Interface representing an algorythm for computing the actual sound volume corresponding to a given distance and reference distance.
*/
public interface DistanceModel {
public float getVolume (float distance, float reference) ;

/** Exponential distance model, corresponding to the formula 2^(distance/reference) */
public static final DistanceModel EXPONENTIAL = new DistanceModel(){
public float getVolume (float distance, float reference) {
return (float)Math.pow(2, -distance/reference);
}};
/** Linear distance model, corresponding to the formula 1-(distance/(reference*2)) */
public static final DistanceModel LINEAR = new DistanceModel(){
public float getVolume (float distance, float reference) {
if (distance > reference*2) return 0;
return 1 - (distance / (reference*2));
}};
/** Default distance model is exponential */
public static final DistanceModel DEFAULT = EXPONENTIAL;
}

