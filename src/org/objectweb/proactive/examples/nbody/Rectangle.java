/*
 * Created on Jan 14, 2005
 */
package org.objectweb.proactive.examples.nbody;

import java.io.Serializable;

/**
 * @author irosenbe
 */
public class Rectangle implements Serializable{

    double x, y, width, height;
    
    public Rectangle (){}

    public Rectangle (double x, double y , double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * A rectangle spanning between the two given points
     */
    public Rectangle (Point2D p , Point2D q) {
        this.x = p.x;
        this.y = p.y;
        this.width = q.x - p.x;
        this.height = q.y - p.y;
    }

    public String toString () {
        return "x="+x+" y=" +y+" width="+width+" height="+height;
    }
}
