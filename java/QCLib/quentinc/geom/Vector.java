package quentinc.geom;
/**
Class for manipulating 3D vectors.
*/
public class Vector implements Cloneable, java.io.Serializable {
protected double x, y, z;
/**
Default constructor. Create a new vector x=0, y=0, z=0
*/
public Vector () { this(0,0,0); }
/**
_Create a new vector with given x and y, and z=0
*/
public Vector (double x, double y) { this(x,y,0); }
/**
Create a new vector with given components
*/
public Vector (double x, double y, double z) { this.x=x; this.y=y; this.z=z; }
/** 
Get X component 
*/
public double getX () { return x; }
/**
Get Y component
*/
public double getY () { return y; }
/**
Get Z component
*/
public double getZ () { return z; }
/**
Set X component
*/
public void setX (double a) { x=a; }
/**
Set Y component
*/
public void setY (double a) { y=a; }
/**
set Z component
*/
public void setZ (double a) { z=a; }
/**
Get the vector's length, as known as norma. Formula : sqrt(x^2 + y^2 + z^2)
*/
public double getLength () { return (double)Math.sqrt(x*x + y*y + z*z); }
/**
Normalize the vector so that it has a length of 1
*/
public void normalize () {
double l = getLength();
if (l==0) return;
multiply(1/l);
}
/**
Divide the vector by a scalar value
*/
public Vector divide (double f) { return multiply(1/f); }
/**
Multiply the vector by a scalar value
*/
public Vector multiply (double f) { x *= f; y *= f; z *= f; return this; }
/**
Invert the vector (aka multipliing it by -1)
*/
public Vector invert () { return multiply(-1); }
/**
Set all the components in one call
*/
public void set (double x, double y, double z) { this.x=x; this.y=y; this.z=z; }
/** 
Add values to each component
*/
public Vector add (double x, double y, double z) { this.x+=x; this.y+=y; this.z+=z; return this; }
/**
Add the vector given to this, modifying this
*/
public Vector add (Vector v) { return add(v.x,v.y,v.z); }
/**
Subtract the given vector to this, modifying this
*/
public Vector subtract (double x, double y, double z) { this.x-=x; this.y-=y; this.z-=z; return this; }
/**
Subtract the given vector to this, modifying this
*/
public Vector subtract (Vector v) { return subtract(v.x,v.y,v.z); }
/**
Return the scalar product (as known as dot product) of this vector and the one given.
*/
public double scalarProduct (Vector v) { return x*v.x + y*v.y + z*v.z; }
/**
Return the cross product of this vector and the one given
*/
public Vector crossProduct (Vector v) {
return new Vector( y*v.z-v.y*z, z*v.x-v.z*x, x*v.y-v.x*y );
}
/**
Multiply this vector with the given 3x3 matrix. Operation as known as linear transformation.
*/
public Vector linearTransform (Matrix m) { return m.multiply(new Matrix(this)).toVector(); }
/**
Return a String representation of this vector (i.e. "1;2;3")
*/
public String toString () { return String.format("%.2f;%.2f;%.2f", x, y, z); }
public Vector clone () { return new Vector(x,y,z); }
public boolean equals (Object o) {
if (!(o instanceof Vector)) return false;
Vector v = (Vector)o;
return v.x==x && v.y==y && v.z==z;
}
public int hashCode () { return Float.floatToIntBits((float)(x*y*z+x+y+z)); }

}