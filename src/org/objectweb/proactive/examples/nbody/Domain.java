/*
 * Created on Jan 7, 2005
 */
package org.objectweb.proactive.examples.nbody;

import java.io.Serializable;
import java.util.Vector;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveGroup;


/**
 * @author irosenbe
 */
public class Domain implements Serializable{
    
    private int identification;
    private int nbNeighbours, nbValuesAwaited;
    
    private Maestro maestro;
    private Displayer display = null;
    
    private QuadTree quadTree, treeNode;
    private Domain neighbours;
    
    
    public Domain (){}
    
    /**
     * A new Domain, which is *NOT* initialized (init must be called).
     * @param id the unique integer which unambiguously denotes this Domain.
     */
    public Domain (Integer id) {
        identification = id.intValue();
    }
    
    /**
     * Assigns neighbours, the displayer, and the treenode correpsonding to this Domain.
     * @param domainG The group of domains with which to communicate positions
     * @param dp the displayer, to show the evolution of the bodies - may be set to null 
     * @param master The Master of every Domain, which synchronizes them 
     * @param tree The tree structure giving the relation between all the Domains  
     */
    public void init(Domain domainG, Displayer dp, Maestro master, QuadTree tree) {
        init(domainG,master,tree);
        display=dp;
    }
    
    /**
     * Assigns neighbours, and the treenode correpsonding to this Domain.
     * @param domainGroup The Group of Domains with which to communicate
     * @param master the Maestro to notify when computation is finished
     * @param tree the QuadTree which allows to find more Infos when needed
     */
    public void init(Domain domainGroup, Maestro master, QuadTree tree) {
        maestro = master;
        neighbours = domainGroup;
        Group gr = ProActiveGroup.getGroup(neighbours);
        gr.remove(ProActive.getStubOnThis ()); // domainGroup now contains all domains, excluding self
        // TODO Z5 code above should also remove all useless nodes of tree   
        
        nbNeighbours = gr.size() ;
        nbValuesAwaited = nbNeighbours ;  
        quadTree = tree;
        
        treeNode = quadTree.getNode (identification); 
        //quadTree.setAllLeavesToNullExcept(treeNode);
    }
    
    
    /**
     * Computes the new position of the center of mass of this domain.
     * Should only be called once all neighbouring nodes have sent their new value. 
     *
     */
    public void computeNewValue() {
        if (treeNode.Q==null) 
            computeNewValueForLeaf();
        treeNode.recomputeCenterOfMass();
        nbValuesAwaited = nbNeighbours;  
        maestro.notifyFinished();
    }
    
    
    /**
     * For every Planet, work out the interactions created by *all* the other bodies in the universe.
     * It tries from the root of the QuadTree to do it. If unable because the body it too close to the center
     * of the info related to this TreeNode, split this node into its subnodes, and iteratively do it for these.
     */
    private void computeNewValueForLeaf() {
        // System.out.println("Domain " + identification + " starting mvt computation");
        
        //Planet [] planets = (Planet []) treeNode.info.getPlanets().toArray(); 
        Vector dummy = treeNode.info.getPlanets(); // TODO Z9 : why doesn't toArray() work?
        Planet [] planets = new Planet[dummy.size()];
        for (int i = 0 ; i < planets.length ; i++ )
            planets[i]=(Planet) dummy.get(i);
        
        
        
        Force [] totalForce = new Force [planets.length] ;  
        for (int i = 0 ; i < planets.length ; i++) 
            totalForce [i] = new Force();
        
        Vector listNodes = new Vector(); // TODO Z7 this should not be done each time : hack is make once, if not enough add others.
        listNodes.add(quadTree);
        while (!listNodes.isEmpty()) {
            QuadTree distantTree = (QuadTree) listNodes.remove(0);
            if (distantTree == treeNode) 
                continue;
            try {
                Force [] localForces = new Force [planets.length]; 
                for (int i = 0 ; i < planets.length ; i++)  // try to create a force for each planet
                    localForces[i] = new Force (planets[i], distantTree.info); 
                for (int i = 0 ; i < planets.length ; i++) // if no exception, add this force to totalforce
                    totalForce[i].add(localForces[i]);
            }
            catch (TooCloseBodiesException e) { // have to split the QuadTree into smaller elements
                if (distantTree.Q == null) {   // there are no subTrees, compute with every planet  
                    Vector distantPlanets = distantTree.info.getPlanets();
                    while (! distantPlanets.isEmpty() ) { 
                        Planet pluto = ((Planet) distantPlanets.remove(0)) ; 
                        for (int i=0; i < planets.length ; i ++) 
                            totalForce[i].add(new Force (planets[i], pluto));
                    }
                }
                else // since there are subtrees, put them in the queue.
                    for (int i = 0 ; i < distantTree.Q.length ; i++ )
                        listNodes.add (distantTree.Q[i]);
            }
        }
        for (int i = 0 ; i < planets.length; i++ )
            planets[i].moveWithForce(totalForce[i]);
    }
    
    
    
    /**
     * Update the value of this info (given by it's id) in the tree 
     */
    public void setValue(Info inf, int id) {
        //System.out.println(identification + " received value from " + id + " Info is " + inf.x);
        quadTree.setNodeInfo(inf,id) ;
        //values[id] = inf;
        nbValuesAwaited--;
        
        if (nbValuesAwaited == 0)
            computeNewValue();
    }
    
    /**
     * Broadcast the local value of CenterOfMass to all its neighbours.
     */
    public void sendValueToNeighbours() {
        // TODO Z4 send value to self, to solve case of only one domain deployed...
        // System.out.println(identification + " sends value to its neighbours");
        Info info = treeNode.info ; 
        //System.out.println(identification + " sending to all nodes value " + info.x + " root value is " + quadTree.info.x);
        
        neighbours.setValue(info, identification);
        if (display != null && treeNode.Q == null ) { // ie if Domain has no subdomain 
            Vector v = info.getPlanets();
            for (int i = 0 ; i < v.size() ; i ++) {
                Planet p = (Planet) v.get(i);
                display.drawBody((int)p.x, (int)p.y, (int)p.vx, (int)p.vy, 
                        (int)p.mass, (int)p.diameter, p.identification);
            }
        }
    }
    
}
