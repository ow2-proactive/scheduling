/*
 * Created on Jan 11, 2005
 */
package org.objectweb.proactive.examples.nbody.oospmd;

import java.io.Serializable;
import java.util.Vector;

import org.objectweb.proactive.examples.nbody.common.Rectangle;


/**
 * @author irosenbe
 */



public class Info implements Serializable{
    
    public int identification;  
    int numPlanets;
    double mass=0; 
    double radius=0;
    
    double x,y;
    
    private Vector planets;
    
    public Info(){};
    
    public Info(Vector planetVector, Rectangle bounds){
        planets = planetVector ; 
        numPlanets = planetVector.size();
        radius=Math.max(bounds.width, bounds.height) / 2 ; // there could be a better approximation, but this is enough
        
        //setCenterOfMass((Info [])planetVector.toArray());
        Planet [] planets = new Planet[planetVector.size()];		
        for (int i = 0 ; i < planets.length ; i++ )
            planets[i]=(Planet) planetVector.get(i);
        setCenterOfMass(planets);
        
        mass = totalMass(planetVector,numPlanets);
    };
    
    
    /**
     * @return a Vector of Planets, which represents all the planets inside this Info 
     */
    public Vector getPlanets() {
        return planets;
    }
    
    /**
     * @param bodyList List of Bodies, of type Info Vector, which all have a mass
     * @param numPlanets = bodyList.size()
     * @return the sum of all the masses of the planets within this region 
     */
    private double totalMass(Vector planetVector, int numPlanets) {
        double mass = 0;
        for (int i = 0 ; i < numPlanets ; i++) {
            Planet body = (Planet) planetVector.get(i);
            mass += body.mass;
        }
        return mass;
    }
    
    /**
     * Sets the center of the current Info as the center of mass of the bodies given in parameter
     * @param planets The set of infos which are to be summed up
     */
    public void setCenterOfMass(Info[] planets) {
        assert planets.length !=0 : "Trying to find the center of an empty set.";
        
        x = 0;
        y = 0;
        mass = 0;
        for (int i = 0 ; i < planets.length ; i ++) {
            mass += planets[i].mass; 
            x += planets[i].x * planets[i].mass;
            y += planets[i].y * planets[i].mass;
        }
        if (mass == 0 ) {
            System.out.println ("Divide by zero . Nb Planets : " + planets.length );
            System.exit (-2);
        }
        
        x /= mass;
        y /= mass;
        
    }

    /**
     * @param identification
     */
    public void setId(int id) {
        identification = id;        
    }
    
}
