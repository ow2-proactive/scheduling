package org.objectweb.proactive.examples.nbody.barneshut;

/**
 * Class implementing physical gravitation force between bodies.
 * In this Barnes-Hut package, building a force may yield an exception 
 * if the info has a too big diameter.  
 */
import java.io.Serializable;

public class Force implements Serializable{
    
    double x=0,y=0;    
    final double G=9.81, RMIN = 1;
    
    public Force () {}
    
    /**
     * From a planet and a set of planets, adds the force resulting from their interaction.
     * The force is the force that applies on planet p1, caused by the set of planets contained in p2.  
     * @param planet the information of the boody on which the force is applied.
     * @param info the information of the body which caused the generation of a force. 
     * @throws TooCloseBodiesException if the radius is bigger than the distance 
     */
    public Force (Planet planet , Info info) throws TooCloseBodiesException{
        double a = info.x - planet.x;
        double b = info.y - planet.y;
        double distance = Math.sqrt(a*a + b*b);
        if (distance < planet.diameter ) // avoids division by zero 
            distance = planet.diameter; 
        if (info.radius > distance)					
            throw new TooCloseBodiesException();    
        double cube = distance*distance;            
        double coeff = this.G * info.mass / cube ; // * p1.mass removed, because division removed as well
        // Watch out : no minus sign : we want to have force of 2 on 1!
        this.x = coeff * (info.x - planet.x);
        this.y = coeff * (info.y - planet.y);
    } 
    
    /**
     * From 2 interacting Planets 1 & 2, adds the force resulting from their interaction.
     * The force considered is the force that applies on 1, caused by 2.  
     * @param p1 the information of the boody on which the force is applied.
     * @param p2 the information of the body which caused the generation of a force. 
     */
    public Force (Planet p1 , Planet p2) {
        double a = p2.x - p1.x;
        double b = p2.y - p1.y;
        double distance = Math.sqrt(a*a + b*b);
        if (distance < p1.diameter ) // avoids division by zero 
            distance = p1.diameter; 
        double cube = distance*distance; 
        double coeff = this.G  * p2.mass / cube; // * p1.mass removed, because division removed as well
        // Watch out : no minus sign : we want to have force of 2 on 1!
        this.x = coeff * (p2.x - p1.x);
        this.y = coeff * (p2.y - p1.y);
    } 
    
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

