package de.uni_stuttgart.informatik.sopra.sopraapp.data.geoData;

/**
 * Created by Christian on 15.11.2017.
 */

public class Vector {

    private double x;
    private double y;

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

    public double getLength() {
        return Math.sqrt(x*x + y*y);
    }

    public Vector normalize() {
        double len = getLength();
        double x1 = x/len;
        double y1 = y/len;
        return new Vector(x1,y1);
    }

    public boolean equalDirection(Vector v) {
        return v.normalize().equals(this.normalize());
    }

    public boolean equals(Vector v) {
        return Math.abs(v.x - this.x ) < 0.001 && Math.abs(v.y - this.y) < 0.001;
    }

    /**
     * rotate this vector counterclockwise
     * @param angle
     * @return
     */
    public Vector rotate (double angle) {
        double x1 = x * Math.cos(angle) - y * Math.sin(angle);
        double y1 = x * Math.sin(angle) + y * Math.cos(angle);
        return new Vector(x1,y1);
    }
}