/*
 * Created on Jan 18, 2005
 */
package org.objectweb.proactive.examples.nbody.common;

/**
 * @author irosenbe
 */
public class Point2D {
    public double x=0,y=0;
    public Point2D () {}

    public Point2D(double a, double b) {
        x=a;
        y=b;
    }
    
    public String toString() {
        return x+", " +y;    
    }
}
