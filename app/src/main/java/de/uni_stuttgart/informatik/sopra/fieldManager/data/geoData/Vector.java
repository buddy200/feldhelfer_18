package de.uni_stuttgart.informatik.sopra.fieldManager.data.geoData;

/**
 * a two-dimensional vector
 *
 * @author Christian
 */
public class Vector {

    private double x;
    private double y;

    /**
     * initializes a two-dimensional vector
     * @param x the first coordinate
     * @param y the second coordinate
     */
    public Vector(double x , double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    /**
     * returns the length of this vector
     * @return the length of this vector
     */
    public double getLength() {
        return Math.sqrt(x*x + y*y);
    }

    /**
     * normalizes this vector so that length = 1
     * 
     * @return the normalized vector 
     */
    public Vector normalize() {
        double len = getLength();
        if (len != 0) {
            double x1 = x / len;
            double y1 = y / len;
            return new Vector(x1, y1);
        } else {
            return new Vector(0,0);
        }
    }

    public boolean equals(Vector v) {
        return Math.abs(v.x - this.x ) < 0.001 && Math.abs(v.y - this.y) < 0.001;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Vector)
            return this.equals((Vector)o);
        else
            return false;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " X: " + x + " Y: " + y;
    }
}
