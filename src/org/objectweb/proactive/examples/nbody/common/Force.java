package org.objectweb.proactive.examples.nbody.common;

import java.io.Serializable;

/**
 * Class implementing physical gravitation force between bodies. 
 */
public class Force implements Serializable{
    
    double x=0,y=0;    
    final double G=9.81, RMIN = 1;
    
    public Force () {}

    /**
     * From 2 interacting bodies 1 & 2, adds the force resulting from their interaction.
     * The force is the force that applies on 1, caused by 2  
     * @param p1 the information of the boody on which the force is applied.
     * @param p2 the information of the body which caused the generation of a force. 
     */
    public void add(Planet p1, Planet p2) {
        if (p2!=null) {  // indeed, P2 null means no force must be added 
            double a = p2.x - p1.x;
            double b = p2.y - p1.y;
            double length = Math.sqrt(a*a + b*b );
            if (length < p1.diameter + p2.diameter)
                length = p1.diameter + p2.diameter;
            double cube = length*length; // *length; 
            double coeff = this.G * p2.mass / cube ; // * p1.mass removed, because division removed as well
            // Watch out : no minus sign : we want to have force of 2 on 1!
            this.x += coeff * a;
            this.y += coeff * b;
        }
    }
    
    // FIXME : the code below is not used, and should be in order to make things more efficient.
    /**
     * Adds up the force of the parameter force to this. 
     * @param f, the force to be added to this
     */
    public void add(Force f) {
        this.x += f.x;
        this.y += f.y;
    }
    
    public String toString(){
        return "<" + (int) this.x + " " + (int) this.y + ">";
    }
}
