package org.objectweb.proactive.examples.nbody.barneshut;

import org.objectweb.proactive.examples.nbody.common.Rectangle;

public class Planet extends Info{
    
    double vx,vy;
    double diameter;
    
    private final double dt = 0.002;
    
    public Planet(){}
    
    /**
     * Defines a random Planet in the given bounds.
     * @param limits the rectangular bounds within which the Planet must be found.
     * @param id : the unique id which serves as color code for the display.
     */
    public Planet(Rectangle limits, int id) {
        this.identification = id;
        this.x = limits.x + Math.random() * limits.width  ;
        this.y = limits.y + Math.random() * limits.height  ;
        this.mass = 1000 + Math.random()*100000 ; 
        //vx = 2000*(Math.random () -0.5 );  
        //vy = 2000*(Math.random () -0.5 );
        this.vx = 0 ; this.vy = 0;
        
        this.diameter = this.mass/2000+3; ;              
    }
    
    public Planet(double x, double y, double vx, double vy, double mass) {
        this.x = x;
        this.y = y;
        this.mass = mass; 
        
        this.vx = vx; 
        this.vy = vy;
        
        this.diameter = mass/2000+3; ;  
        this.radius = 1; // smaller than any possible diameter, from equations above
    }
    
    /**
     * Move the planet, and define a new speed vector to assign to this planet, depending on the total 
     * force which is applied to it.  
     *      * @param force The total Force which pushes this planet on the local iteration. 
     */
    public void moveWithForce(Force force) {
        // Using f(t+dt) ~= f(t) + dt * f'(t)
        this.x += this.vx * dt;
        this.y += this.vy * dt;
        
        // sum F  = mass * acc;
        // a = sum F / mass 
        this.vx += this.dt * force.x ;  //  /mass removed, because muliplication removed as well
        this.vy += this.dt * force.y ;
        
    }
    public String toString(){
        return "x= " + this.x + " y= "+ this.y +" vx= "+ this.vx +" vy=" + this.vy;
    }
}

