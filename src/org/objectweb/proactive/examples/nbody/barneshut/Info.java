package org.objectweb.proactive.examples.nbody.barneshut;

import java.io.Serializable;
import java.util.Vector;

import org.objectweb.proactive.examples.nbody.common.Rectangle;


public class Info implements Serializable{
    
    public int identification;  
    double mass=0; 
    double radius=0;
    
    double x,y;
    
    public Planet [] planets;
    
    private Vector sons;
    private int nbExpectedSons;
    private boolean isLeaf=true;
    
    
    public Info(){};
    
    public Info(Vector planetVector, Rectangle bounds){
        radius=Math.sqrt(bounds.width*bounds.width + bounds.height*bounds.height) / 2 ; 
        
        //FIXME : why don't this work? planets = ((Planet [])planetVector.toArray());
        planets = new Planet[planetVector.size()];		
        for (int i = 0 ; i < planets.length ; i++ )
            planets[i]=(Planet) planetVector.get(i);
        recomputeCenterOfMass();
        
        mass = totalMass();
        this.nbExpectedSons = planets.length;
    }
    
    private Info(Info info) {
        this.identification=info.identification;  
        this.mass=info.mass; 
        this.radius=info.radius;
        
        this.x = info.x;
        this.y = info.y;
        
        this.planets=info.planets;
        
        this.sons=info.sons;
    }
    
    
    /**
     * Compute the total mass contained in this "region".
     * @param bodyList List of Bodies, of type Info Vector, which all have a mass
     * @param numPlanets = bodyList.size()
     * @return the sum of all the masses of the planets within this region 
     */
    private double totalMass() {
        double mass = 0;
        for (int i = 0 ; i < planets.length ; i++) 
            mass += planets[i].mass;
        return mass;
    }
    
    
    /**
     * Sets the center of the current Info as the center of mass of the Planets contained.
     * @param planets The set of infos which are to be summed up
     */
    public void recomputeCenterOfMass() {
        x = 0;
        y = 0;
        mass = 0;
        if (!isLeaf) { // then this.sons contains the update, hopefully
            if ( sons.size() != nbExpectedSons ) 
                throw new NullPointerException("Info has not received all of its sons!");
            
            for (int i = 0 ; i < sons.size() ; i ++) {
                Info sibling = (Info) sons.get(i);
                mass += ( sibling ).mass; 
                x += sibling.x * sibling.mass;
                y += sibling.y * sibling.mass;
            }
        }
        else {
            //assert planets.length !=0 : "Trying to find the center of an empty set.";
            if ( planets.length == 0 ) 
                throw new NullPointerException("Trying to find the center of an empty set.");
            
            for (int i = 0 ; i < planets.length ; i ++) {
                mass += planets[i].mass; 
                x += planets[i].x * planets[i].mass;
                y += planets[i].y * planets[i].mass;
            }
        }
        
        x /= mass;
        y /= mass;
    }
    
    public void clean (boolean isLeaf){
        this.isLeaf = isLeaf;
        if (!isLeaf)
            planets = null;
        emptySons();
    }
    
    
    public void emptySons(){
        if (!isLeaf)
            sons = new Vector();
    }
    
    public void addSon(Info sibling){
        sons.add(sibling);
        if (sons.size() > nbExpectedSons)
            throw new NullPointerException("Adding a son too many!!");
    }
    
    public String toString(){
        return identification + (isLeaf ? " leaf " : " not leaf " ) + mass; 
    }
    
    public Info copy() {
        return new Info(this);
    } 
    /**
     * Method needed to reset the number of sons, because it initially corresponds to the underlying nb of Planets.
     * @param nb
     */
    public void setNbSons(int nb) {
        nbExpectedSons = nb;
    }
    
}
