package org.objectweb.proactive.examples.nbody.common;

import java.io.Serializable;

public class Point2D implements Serializable{
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
