package quentinc.geom;
/**
Class for manipulating matrix of arbitrary size
*/
public class Matrix {
private double[][] m;
/**
Create a new matrix with given number of rows and columns 
*/
public Matrix (int w, int h) {
m = new double[w][h];
for (int i=0; i < w; i++) m[i]=new double[h];
}
/**
Create a new matrix with the 2D array given.
*/
public Matrix (double[][] d) { 
m=d; 
}
/**
Create a new 3x1 matrix representing the given vector.
*/
public Matrix (Vector v) {
m = new double[3][1];
double[] d = { v.getX(), v.getY(), v.getZ() };
for (int i=0; i < 3; i++) {
m[i] = new double[1];
m[i][0] = d[i];
}}
/**
Get the value at the given position
*/
public double get (int row, int col) { 
return m[row][col];
}
/**
Set the value at the given position
*/
public void set (int row, int col, double value) {
m[row][col] = value;
}
/**
Get the number of columns
*/
public int getColumnCount () { return m[0].length; }
/**
Get the number of rows
*/
public int getRowCount () { return m.length; }
/**
Convert this matrix to a vector, using the first column as coefficiants.
*/
public Vector toVector () {
return new Vector( (float)m[0][0], (float)m[1][0], (float)m[2][0] );
}
/**
Return a string representation of this matrix. This representation is like CSV.
*/
public String toString () {
StringBuilder sb = new StringBuilder();
for (int i=0; i < m.length; i++) {
for (int j=0; j < m[i].length; j++) {
sb.append(m[i][j]).append(',').append(' ');
}
sb.append("\r\n");
}
return sb.toString();
}
public boolean equals (Object o) {
if (o==this) return true;
if (!(o instanceof Matrix)) return false;
Matrix m = (Matrix)o;
if (this.m.length != m.m.length || this.m[0].length != m.m[0].length) return false;
for (int i=0; i < this.m.length; i++) {
for (int j=0; j < this.m[i].length; j++) {
if (this.m[i][j]!=m.m[i][j]) return false;
}}
return true;
}
public int hashCode () {
double d = 0;
for (int i=0; i < m.length; i++) {
for (int j=0; j<m[i].length; j++) {
d += m[i][j] + i*j;
}}
return (int)d;
}
/**
Add the given matrix to this, modifying this
*/
public Matrix add (Matrix m) {
for (int i=0; i < this.m.length; i++) {
for (int j=0; j < this.m[i].length; j++) {
this.m[i][j] += m.m[i][j];
}}
return this;
}
/**
Subtract the given matrix to this, modifying this
*/
public Matrix subtract (Matrix m) {
for (int i=0; i < this.m.length; i++) {
for (int j=0; j < this.m[i].length; j++) {
this.m[i][j] -= m.m[i][j];
}}
return this;
}
/**
Multiply this matrix by a scalar value, modifying this
*/
public Matrix multiply (double d) {
for (int i=0; i < m.length; i++) {
for (int j=0; j < m[i].length; j++) {
m[i][j] *= d;
}}
return this;
}
/**
Divide this matrix by a scalar value, modifying this
*/
public Matrix divide (double d) { return multiply(1/d); }
public Matrix clone () {
Matrix z = new Matrix(getRowCount(), getColumnCount());
for (int i=0; i < m.length; i++) {
for (int j=0; j < m[i].length; j++) {
z.m[i][j] = m[i][j];
}}
return z;
}
/**
Create a new matrix which is the transposition of this one.
*/
public Matrix transpose () {
Matrix z = new Matrix(getColumnCount(), getRowCount());
for (int i=0; i < m.length; i++) {
for (int j=0; j<m[i].length; j++) {
z.m[j][i] = m[i][j];
}}
return z;
}
/**
Multiply this matrix with the one given. Return a new matrix.
*/
public Matrix multiply (Matrix m) {
if (this.getColumnCount()!=m.getRowCount()) throw new ArithmeticException(String.format("Could not multiply a %d*%d matrix by a %d*%d one", this.getRowCount(), this.getColumnCount(), m.getRowCount(), m.getColumnCount() ));
Matrix n = new Matrix( this.getRowCount(), m.getColumnCount() );
for (int i=0, I=n.getRowCount(); i<I; i++) {
for (int j=0, J=n.getColumnCount(); j<J; j++) {
for (int k=0; k < this.m[0].length; k++) {
n.m[i][j] += this.m[i][k] * m.m[k][j];
}}}
return n;
}


private double[][] invline (double[][] f, int a, int b) {
for (int i=0; i < f[0].length; i++) {
double t = f[a][i];
f[a][i] = f[b][i];
f[b][i] = t;
}
return f;
}
private double[][] multline (double f[][], double x, int a, int b) {
for (int i=0; i < f[0].length; i++) {
f[b][i] = f[b][i] + (f[a][i] * (x));
}
return f;
}
/**
Transform the matrix so that it has the gauss stair form. Return a new matrix, the original is unchanged
*/
public Matrix toGaussForm () {
Matrix m = this.clone();
int h = m.m.length;
int pi = -1, pj = -1;

while (true) {
int curl = ++pi;
do {
pj++;
if (pj>=m.m[0].length) break;
while (curl<m.m.length && m.m[curl][pj]==0) curl++;
} while (curl==m.m.length && pj<m.m[0].length);
if (pj>=m.m[0].length) break;

if (pi!=curl) m.m = invline(m.m, pi, curl);
double coeff = m.m[pi][pj];
for (int pii=pi+1; pii<m.m.length; pii++) {
double c = m.m[pii][pj];
m.m = multline(m.m, c / (-coeff), pi, pii);
}
} // end
return m;
}

/**
Return the determinant value of the matrix
*/
public double determinant () {
Matrix r = toGaussForm();
double f = 1;
for (int i=0; i < r.m.length; i++) {
f *= (r.m[i][i]);
}
return f;
}


}