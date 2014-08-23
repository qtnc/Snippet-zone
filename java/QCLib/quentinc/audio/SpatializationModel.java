package quentinc.audio;
/**
An interface representing an algorythm to convert between angle in real life and panning position.
*/
public interface SpatializationModel {
public float getPan (double cosAngle, double distance) ;

/** Sinusoidal spatialization model, return the angle unchanged */
public static final SpatializationModel SINUSOIDAL = new SpatializationModel (){
public float getPan (double angle, double distance) { return (float)angle; }
};
/** Linear spatialization model, corresponding to the formula cos<sup>-1</sup>(angle) / &pi; */
public static final SpatializationModel LINEAR = new SpatializationModel (){
public float getPan (double angle, double distance) {
return (float)( Math.acos(angle) / Math.PI );
}};
/** Default spatialisation model is sinusoidal */
public static final SpatializationModel DEFAULT = SINUSOIDAL;
}
