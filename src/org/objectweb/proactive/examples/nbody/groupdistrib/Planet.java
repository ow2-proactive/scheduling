package org.objectweb.proactive.examples.nbody.groupdistrib;

import java.io.Serializable;

import org.objectweb.proactive.examples.nbody.common.Rectangle;

public class Planet implements Serializable{

    final double dt = 0.002;
    double mass;
    double x,y;
    double vx,vy;
    double diameter;
    
    public Planet(){};

    /**
     * Builds one Planet within the given frame.
     * @param limits the bounds which contain the Planet
     */
    public Planet(Rectangle limits) {
        x = limits.x + Math.random() * limits.width  ;
        y = limits.y + Math.random() * limits.height  ;
        mass = 1000 + Math.random()*100000 ; 
        
        //vx = 2000*(Math.random () -0.5 );  
        //vy = 2000*(Math.random () -0.5 );
        vx = 0 ; vy = 0;
        
        diameter = mass/2000+3; ;              
    }

    /**
     * 	Move the given Planet with the Force given as parameter. 
     *  @param force
     */
    public void moveWithForce(Force force) {
        // Using f(t+dt) ~= f(t) + dt * f'(t)
        x += dt * vx ;
        y += dt * vy ;
        // sum F  = mass * acc;
        // v' = a = sum F / mass:
        vx += dt * force.x ;  // removed /mass because * p1.mass removed as well  
        vy += dt * force.y ; 

        //        x += 0.5;
    }
}
