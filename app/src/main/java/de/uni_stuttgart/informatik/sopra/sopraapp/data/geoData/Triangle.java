package de.uni_stuttgart.informatik.sopra.sopraapp.data.geoData;

import de.uni_stuttgart.informatik.sopra.sopraapp.data.CornerPoint;

/**
 * Created by Christian on 15.11.2017.
 */

public class Triangle {

    private CornerPoint a;
    private CornerPoint b;
    private CornerPoint c;

    public Triangle(CornerPoint a, CornerPoint b, CornerPoint c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public double getSize() {
        UTMCoordinate utmA = a.getUtm();
        UTMCoordinate utmB = b.getUtm();
        UTMCoordinate utmC = c.getUtm();
        if(utmA.getZone() == utmB.getZone() && utmB.getZone() == utmC.getZone()) {
            Vector v_a = new Vector((utmA.getEasting() - utmB.getEasting()), (utmC.getNorthing() - utmB.getNorthing()));
            Vector v_c = new Vector((utmA.getEasting() - utmB.getEasting()), (utmC.getNorthing() - utmB.getNorthing()));

            return 0.5* Math.abs( v_a.getX()*v_c.getY() - v_c.getX()*v_a.getY()); //test requiered, formula from wikipedia
        } else {
            //TODO
        }
        return 0;
    }
}
