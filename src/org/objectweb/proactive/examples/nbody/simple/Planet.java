package org.objectweb.proactive.examples.nbody.simple;

import java.io.Serializable;

import org.objectweb.proactive.examples.nbody.common.Rectangle;

/**
 * The implementation of a physical body
 */
public class Planet implements Serializable{
    
    final double dt = 0.002; 		// the time step. The smaller the more precise the movement
    double mass;
    double x,y,vx,vy; 				// position and velocity
    double diameter;  				// diameter of the body, used by the Displayer
    
    /**
     * Required by ProActive, because this Object will be send as a parameter of a method on 
     * a distant Active Object.
     */
    public Planet(){};
    
    /**
     * Builds one Planet within the given frame.
     * @param limits the bounds which contain the Planet
     */
    public Planet(Rectangle limits) {
        this.x = limits.x + Math.random() * limits.width  ;
        this.y = limits.y + Math.random() * limits.height  ;
        this.mass = 1000 + Math.random()*100000 ; 
        //vx = 2000*(Math.random () -0.5 );  
        //vy = 2000*(Math.random () -0.5 );
        this.vx = 0 ; this.vy = 0;
        this.diameter = this.mass/2000+3; ;              
    }
    
    /**
     * 	Move the given Planet with the Force given as parameter. 
     *  @param force the force that causes the movement of the Planet
     */
    public void moveWithForce(Force force) {
        // Using f(t+dt) ~= f(t) + dt * f'(t)
        this.x += this.dt * this.vx ;
        this.y += this.dt * this.vy ;
        // sum F  = mass * acc;
        // v' = a = sum F / mass:
        this.vx += this.dt * force.x ;  // removed /mass because * p1.mass removed as well  
        this.vy += this.dt * force.y ; 
    }
}
