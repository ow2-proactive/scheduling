/*
 * Created on Jan 22, 1005
 */
package org.objectweb.proactive.examples.nbody.groupcom;

import java.io.Serializable;

/**
 * @author irosenbe
 */
public class Force implements Serializable {

    double x=0,y=0;    
    final double G=9.81, RMIN = 1;

    public Force () {}
   
    /**
     * From 2 interacting bodies 1 & 2, adds the force resulting from their interaction.
     * The force is the force that applies on 1, caused by 2  
     * @param p1, p2 the information of two interacting bodies 
     */
    public void add(Planet p1, Planet p2) {
       if (p2!=null) {  // indeed, P2 null means no force must be added 
        double a = p2.x - p1.x;
        double b = p2.y - p1.y;
        double length = Math.sqrt(a*a + b*b );
        if (length < p1.diameter + p2.diameter)
            length = p1.diameter + p2.diameter;
        double cube = length*length; // *length; 
        double coeff = G * p2.mass / cube ; // * p1.mass removed, because division removed as well
        // Watch out : no minus sign : we want to have force of 2 on 1!
        x += coeff * (p2.x - p1.x);
        y += coeff * (p2.y - p1.y);
      }
    }
   
}
