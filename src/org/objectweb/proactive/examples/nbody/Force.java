/*
 * Created on Jan 22, 1005
 */
package org.objectweb.proactive.examples.nbody;

import java.io.Serializable;

/**
 * @author irosenbe
 */
public class Force implements Serializable {
    
    double x=0,y=0;    
    final double G=9.81, RMIN = 1;
    
    public Force () {}
    
    /**
     * From a planet and a set of planets, adds the force resulting from their interaction.
     * The force is the force that applies on planet p1, caused by the set of planets p2.  
     * @param p1, p2 the information of two interacting bodies
     * @throws TooCloseBodiesException if the radius is bigger than the distance 
     */
    public Force (Planet p1 , Info p2) throws TooCloseBodiesException{
        double a = p2.x - p1.x;
        double b = p2.y - p1.y;
        double distance = Math.sqrt(a*a + b*b);
        if (distance < p1.diameter ) // avoids division by zero 
            distance = p1.diameter; 
        if (p2.radius > distance)					// TODO Z9 add the theta parameter here?
            throw new TooCloseBodiesException();    
        double cube = distance*distance;            // TODO Z6 replace this square by a cube
        double coeff = G * p1.mass * p2.mass / cube ;
        // Watch out : no minus sign : we want to have force of 2 on 1!
        x = coeff * (p2.x - p1.x);
        y = coeff * (p2.y - p1.y);
    } 

    /**
     * From 2 interacting Planets 1 & 2, adds the force resulting from their interaction.
     * The force considered is the force that applies on 1, caused by 2.  
     * @param p1, p2 the information of two interacting planets
     */
    public Force (Planet p1 , Planet p2) {
        double a = p2.x - p1.x;
        double b = p2.y - p1.y;
        double distance = Math.sqrt(a*a + b*b);
        if (distance < p1.diameter ) // avoids division by zero 
            distance = p1.diameter; 
        double cube = distance*distance; //  TODO Z6  replace this square by a cube
        double coeff = G * p1.mass * p2.mass / cube ;
        // Watch out : no minus sign : we want to have force of 2 on 1!
        x = coeff * (p2.x - p1.x);
        y = coeff * (p2.y - p1.y);
    } 

    /**
     * Adds up the force of the parameter force to this. 
     * @param f, the force to be added to this
     */
    public void add(Force f) {
        x += f.x;
        y += f.y;
    }
    
}
