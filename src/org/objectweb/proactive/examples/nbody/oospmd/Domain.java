/*
 * Created on Jan 7, 2005
 */
package org.objectweb.proactive.examples.nbody.oospmd;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Vector;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveGroup;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.examples.nbody.common.Displayer;
import org.objectweb.proactive.examples.nbody.common.TooCloseBodiesException;


/**
 * @author irosenbe
 */

public class Domain implements Serializable{
    
    private boolean started = false;
    
    public int identification;
    
    private Displayer display = null;
    
    private QuadTree quadTree, treeNode;
    private Domain maestro;		// the only Domain to notify for synchro
    private Domain neighbours; // Domains that need to know my coordinates 
    private Domain domainGroup; // All the Domains in the simulation
    private Domain [] domainArray; // a copy of the above domainGroup, but as an array
    private int iter = 0; 
    
    Force [] totalForce;  
    private int nbNeighbours = 0, nbUpdates=0;
    
    private Vector listPositions ;
    
    private String hostName;
    
    public Domain (){}
    
    /**
     * A new Domain, which is *NOT* initialized (init must be called).
     * @param id the unique integer which unambiguously denotes this Domain.
     */
    public Domain (Integer id) {
        identification = id.intValue();
        try {
            neighbours = (Domain) ProActiveGroup.newGroup( Domain.class.getName() );
        }
        catch (ClassNotReifiableException e) { Start.abort(e); }
        catch (ClassNotFoundException e) { Start.abort(e); }
        try {
            this.hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Assigns neighbours, the displayer, and the treenode corresponding to this Domain.
     * @param domainG The group of domains with which to communicate positions
     * @param dp the displayer, to show the evolution of the bodies - may be set to null 
     * @param master The Master of every Domain, which synchronizes them 
     * @param tree The tree structure giving the relation between all the Domains  
     */
    public void init(Domain domainGroup, Domain [] domainArray, Displayer dp, Domain master, QuadTree tree) {
        init(domainGroup, domainArray, master,tree);
        display=dp;
    }
    
    /**
     * Assigns neighbours, and the treenode corresponding to this Domain.
     * @param domainGroup The Group of Domains with which to communicate
     * @param master the Maestro to notify when computation is finished
     * @param tree the QuadTree which allows to find more Infos when needed
     */
    public void init(Domain domainGroup, Domain [] domainArray, Domain master, QuadTree tree) {
        this.domainGroup = domainGroup;
        Group group = ProActiveGroup.getGroup(neighbours);
        group.remove(ProActive.getStubOnThis());

        maestro = master;
        quadTree = tree;
        treeNode = quadTree.getNode (identification); 
        this.domainArray = domainArray;
        
        listPositions = new Vector();
        
        if (treeNode.Q==null) { // If this is a leaf, the root will send info, and all the others will follow 
            System.out.println(identification + " adding self as neighbour to root.");
            //root.addNeighbour(this);
            domainArray[0].addNeighbour((Domain) ProActive.getStubOnThis());
            nbNeighbours++;
        }
        else { // If this is not a leaf, we want to be sent the information of the sons, to be able to compute their average.
            for (int i = 0 ; i < treeNode.Q.length  ; i++){
                QuadTree sibling = treeNode.Q[i]; 
                Domain remoteDomain = domainArray[sibling.label];
                System.out.println(identification + " adding self as neighbour to " + sibling.label +"." );
                remoteDomain.addNeighbour((Domain) ProActive.getStubOnThis());
                nbNeighbours++;
            }
        }
        
        int nbPlanets = treeNode.info.getPlanets().size();
        totalForce = new Force [nbPlanets] ;  
        for (int i = 0 ; i < nbPlanets ; i++) {
            totalForce[i] = new Force();
        }
        System.out.println("END OF INIT OF " + identification);
    }
    
    
    /**
     * @param domain The neighbour that wants to be included in the broadcast of information
     */
    public void addNeighbour(Domain domain) {
        System.out.println("Domain " + identification + " received a new neighbour");
        Group group = ProActiveGroup.getGroup(neighbours);
        group.add(domain);
        if (started){
            treeNode.info.setId(identification);
            domain.setValue(treeNode.info);
        }
    }
    
    /**
     * @param domain The neighbour that wants to be excluded from the broadcast of information
     */
    public void removeNeighbour(Domain domain) {
        System.out.println(identification + " removing neighbour.");
        Group group = ProActiveGroup.getGroup(neighbours);
        group.remove(domain);
    }
    
    
    /**
     * Computes the new position of the center of mass of this domain.
     * Should only be called once all neighbouring nodes have sent their new value. 
     *
     */
    public void computeNewValue() {
        //        System.out.print("[re]Compute movement " + identification + ".");
        if (treeNode.Q==null) 
            computeNewValueForLeaf();
        
        //        System.out.print(" ... ");
 
        if (nbNeighbours == nbUpdates) { // finished if the above call hasn't created new neighbours
            treeNode.recomputeCenterOfMass();
            //            System.out.println(" Computed movement! " + identification + ".");
            nbUpdates=0;
            for (int i = 0 ; i < totalForce.length ; i++) { 
                totalForce [i] = new Force();
            }
            maestro.notifyFinished();
        }
        
    }
    
    
    /**
     * For every Planet, work out the interactions created by *all* the other bodies in the universe.
     * It tries from the root of the QuadTree to do it. If unable because the body it too close to 
     * the center of the info related to this TreeNode, split this node into its subnodes, and 
     * iteratively do it for these.
     */
    private void computeNewValueForLeaf() {
        // System.out.println("Domain " + identification + " starting mvt computation");
        
        //Planet [] planets = (Planet []) treeNode.info.getPlanets().toArray(); 
        Vector dumb = treeNode.info.getPlanets(); 
        Planet [] planets = new Planet[dumb.size()];
        for (int i = 0 ; i < planets.length ; i++ )
            planets[i]=(Planet) dumb.get(i);
        
        while (!listPositions.isEmpty() ) {
            
            Info distantInfo = (Info) listPositions.remove(0);  
            int label = distantInfo.identification;
            try {
                Force [] localForces = new Force [planets.length]; 
                for (int i = 0 ; i < planets.length ; i++)  // try to create a force for each planet
                    localForces[i] = new Force (planets[i], distantInfo); 
                for (int i = 0 ; i < planets.length ; i++)  // if no exception, add this force to totalforce
                    totalForce[i].add(localForces[i]) ;	
            }
            catch (TooCloseBodiesException e) { // have to split the QuadTree into smaller elements
                QuadTree distantTree = quadTree.getNode(label); // the tree Node corresponding to the Info
                if (distantTree.Q == null) {   // there are no subTrees, compute with every planet  
                    
                    Vector distantPlanets = distantInfo.getPlanets();
                    while (! distantPlanets.isEmpty() ) { 
                        Planet pluto = ((Planet) distantPlanets.remove(0)) ; 
                        for (int i=0; i < planets.length ; i ++) {
                            Force tmpForce =new Force (planets[i], pluto);
                            totalForce[i].add(tmpForce);
                        }
                    }
                }
                else {// since there are subtrees, put them in the queue.
                    System.out.println(identification + " removing father (" + label + ") ");
                    nbNeighbours--;   nbUpdates--; // Hide totally existence of this Domain 
                    domainArray[distantTree.label].removeNeighbour((Domain)ProActive.getStubOnThis());
                    for (int i=0; i < distantTree.Q.length ; i ++) { 
                        int index = distantTree.Q[i].label;
                        if (index != identification) { // don't add self, no need! 
                            System.out.println(identification + " making new neighbour "   + index + " ");
                            nbNeighbours ++ ; 
                            domainArray[index].addNeighbour((Domain) ProActive.getStubOnThis());
                        }
                    }
                }
                
            } // end of catch 
            
        } // end of while : now check if _really_ finished
        if (nbNeighbours == nbUpdates) {
            // add attractions of forces in the same Domain
            for (int i = 0 ; i < planets.length ; i++) // try to create a force for each planet
                for (int j = 0 ; j < planets.length ; j++)  // try to create a force for each planet
                    if (i!=j)
                        totalForce[i].add(new Force (planets[i], planets[j])); 
                    // It would be good to have the bodies bounce away 
                    // if they are too close!
            for (int i = 0 ; i < planets.length; i++ )
                planets[i].moveWithForce(totalForce[i]);
            //maestro.notify();
        }
    }
    
    
    /**
     * Get the current Info associated with this Domain.
     * @return the current Info associated with this Domain
     */
    public Info getValue() {
        return treeNode.info;
    }
    
    /**
     * Update the value of this info (given by it's id) in the tree 
     */
    public void setValue(Info inf) {  // FIXME : this never gets called if nb Domains=1
        nbUpdates++;
        
        //        int id = inf.identification;
        //        int old  = nbUpdates;
        //        boolean reach = (nbNeighbours == nbUpdates);
        //        System.out.println(identification + " received value from " + id + ". Info is " + inf.x 
        //                + " old nbUpdates " + old + " current " + nbUpdates + " reached " + reach + " nbNeighbours " + nbNeighbours);
        //        System.out.println( id + " -> " + identification);
        
        listPositions.add (inf);
        if (nbNeighbours == nbUpdates) {
            computeNewValue();
        }
    }
    
    
    /**
     * Broadcast the local value of CenterOfMass to all its neighbours.
     */
    public void sendValueToNeighbours() {

        started = true;
        
        //		  int size = ProActiveGroup.getGroup(neighbours).size();
        //        System.out.println(identification + " sends value to " + size + " neighbours");
        
        
        Info info = treeNode.info ; 
        //System.out.println(identification + " sending to all nodes value " + info.x + " root value is " + quadTree.info.x);
        info.setId(identification);
        neighbours.setValue(info);
        if (display != null) {
            if ( treeNode.Q == null ) { // ie if Domain has no subdomain 
                Vector v = info.getPlanets();
                for (int i = 0 ; i < v.size() ; i ++) {
                    Planet p = (Planet) v.get(i);
                    display.drawBody((int)p.x, (int)p.y, (int)p.vx, (int)p.vy, 
                            (int)p.mass, (int)p.diameter, p.identification, hostName);
                }
            }
        }
        else // if no display, only the first Domain outputs message to say recompute is going on
            if (identification==0) {
                System.out.println("Movement.");
            }
    }
    
    public String toString(){
        int neighboursSize = ProActiveGroup.getGroup(neighbours).size();
        return "Id : " + identification + " nbPlanets " + 
                treeNode.info.getPlanets().size()  + 
                " nb values to send " + neighboursSize;
    }

    // variables and methods only used for synchro
    private int nbFinished = 0, maxIter;
    

    /**
     * Start the whole simulation, for a given number of iterations. 
     * @param max The number of iterations which should be computed before the end of the program. 
     */
    public void start(int max) {
        System.out.println("MAESTRO START ");
        maxIter = max; 
        domainGroup.sendValueToNeighbours();
        ((Domain)ProActive.getStubOnThis()).sendValueToNeighbours();
    }
    
    /**
     * Called by a Domain when computation is finished. 
     * This method counts the answers, and restarts all Domains when all have finished. 
     */
    public void notifyFinished() {
        nbFinished ++ ;
        if (nbFinished == domainArray.length) {
            iter ++;
            if (iter == maxIter)  Start.quit ();
            nbFinished = 0 ;
            //System.out.println("Synchro " + iter );
            domainGroup.sendValueToNeighbours();
            ((Domain)ProActive.getStubOnThis()).sendValueToNeighbours();
        }
    }
    
    
    
}


