package org.objectweb.proactive.examples.nbody.barneshut;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveGroup;
import org.objectweb.proactive.core.group.spmd.ProSPMD;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.examples.nbody.common.Displayer;

/**
 * Domains are set over a given region of space.
 * They contain either Bodies, or more Domains, which have smaller space coverage. 
 * Communication is achieved through group communication. Neighbours are chosen
 * so that they cover the biggest area possible, without creating too big errors in computations. 
 */
public class Domain implements Serializable{
    
    private class Carrier implements Serializable{
        Info inf;
        int iter;
        
        Carrier (Info inf, int iter) {
            this.inf = inf;
            this.iter = iter;
        }
    }
    /** unique domain identifier */
    private int identification;					
    /**  The Group containing all the other Domains */
    private Domain neighbours;					
    /**  list of Domains that can send info */
    private boolean [] canIReceiveInfoFrom;		
    /** to display on which host we're running */
    private String hostName = "unknown";		
    
    /** If we want some graphical interface */
    private Displayer display;				
    
    /** references to all the Domains */
    private Domain [] domainArray; 			
    /** hierarchy between domains */
    private QuadTree quadTree, treeNode;	  
    
    /** isLeaf = (treeNode.Q==null) */
    private boolean isLeaf;		
    /** The local information */
    private Info info; 				
    /** the forces applying to the Planets inside the Info */
    private Force [] totalForce; 
    /**  iteration related variables, counting the "pings" */
    private int nbNeighbours = 0, nbReceived = 0;  
    private int iter = 0, maxIter;
    
    /**  If demands come too early, or a bit late. */
    private Vector prematureValues;				 				
    private Info [] savedInfo;
    private org.objectweb.proactive.examples.nbody.common.Start killsupport;
    
    
    /**
     * Required by ProActive Active Objects
     */
    public Domain (){}
    
    /**
     * A new Domain, which is *NOT* initialized (init must be called).
     * @param info the Info linked to this Domain 
     * @param id the unique integer which unambiguously denotes this Domain.
     */
    public Domain (Integer id, Info info, org.objectweb.proactive.examples.nbody.common.Start killsupport) {
        this.killsupport = killsupport;
        this.info = info;
        this.prematureValues = new Vector();
        this.identification = id.intValue();
        this.domainArray= null; // will be correctly initialized by init(...)
        try {
            this.neighbours = (Domain) ProActiveGroup.newGroup( Domain.class.getName() );
        }
        catch (ClassNotReifiableException e) { this.killsupport.abort(e); }
        catch (ClassNotFoundException e) { this.killsupport.abort(e); }
        try {
            this.hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {e.printStackTrace();}
    }
    
    /**
     * Assigns neighbours, the displayer, and the treenode corresponding to this Domain.
     * @param domainArray the array of Domains with which to communicate positions
     * @param dp the displayer, to show the evolution of the bodies  
     * @param tree The tree structure giving the relation between all the Domains
     * @param maxIter the number of iterations before stoppping  
     */
    public void init(Domain [] domainArray, Displayer dp, QuadTree tree, int maxIter) {
        this.display=dp;
        this.maxIter = maxIter;
        this.savedInfo = new Info [maxIter];
        this.savedInfo[0] = this.info.copy();
        this.maxIter = maxIter;
        this.quadTree = tree ;
        this.treeNode = tree.getNode(identification);
        this.domainArray = domainArray;
        this.canIReceiveInfoFrom= new boolean[domainArray.length];
        for (int i =0 ; i <canIReceiveInfoFrom.length ; i++)
            canIReceiveInfoFrom[i] = false;
        this.isLeaf = (treeNode.Q==null);
        if (isLeaf) {
            totalForce = new Force [info.planets.length];
            for (int i = 0 ; i < totalForce.length ; i++)  
                this.totalForce [i] = new Force();
        }
        ProSPMD.barrier("INIT");	// all Domains must be initialized before communication takes place.
        ((Domain) ProActive.getStubOnThis()).finishInit();
    }
    
    
    /**
     * Can only be called once all Domains are ready to receive addNeighbour.
     * That's why it is called after barrier 
     */
    public void finishInit() {
        if (isLeaf) {
            this.nbNeighbours ++ ; 
            askToReceiveInfoFrom(0); // get Info from root, and this will then ask for the sons. 
        }
        else{
            for (int i=0 ; i < treeNode.Q.length; i++) {
                int sibling = treeNode.Q[i].label ;
                this.nbNeighbours ++ ; 
                askToReceiveInfoFrom(sibling); // Stay informaed of the underlying events
            }
        }
    }
    
    
    /**
     * This Domain will now diffuse it's information to the Domain specified by domainIdent.
     * @param domainIdent The identifier of the neighbour that wants to be included in the broadcast of information
     * @param checkIter the iteration of the neighbour, to make sure we're synchronized 
     */
    public void addNeighbour( int domainIdent, int checkIter ) {
        
        if (checkIter -1> iter) { // checkIter - iter = 1 is ok, means start sending on next iteration 
            killsupport.abort( new RuntimeException("Domain " +
                    domainIdent + "["+checkIter+"] asking for a future info to Domain"  + identification + "[" + iter + "] , not possible!") );
        }
        Group gr = ProActiveGroup.getGroup(neighbours);
        
        gr.add(domainArray[domainIdent]);
        for (int i = checkIter ; i <= iter ; i++)   	// resend left out information
            this.domainArray[domainIdent].setValue(this.savedInfo[i], i);
    }
    
    /**
     * A Domain wanting to stop receiving updates of Info will call this method.
     * For example, if the Domain sends too gross Infos, it will be removed and replaced by its sons.
     * @param domainIdent The identifier of the neighbour that wants to be excluded from the broadcast of information.
     */
    public void removeNeighbour(int domainIdent) {
        Group group = ProActiveGroup.getGroup(this.neighbours);
        
        boolean correct = group.remove (domainArray[domainIdent]);
        if (!correct)
            killsupport.abort(new NoSuchElementException("Domain " + domainIdent + " cannot be removed, because it's not in neighbour Group! "));
    }
    
    
    /**
     * Computes the new position of the center of mass of this domain.
     * Should only be called once all neighbouring nodes have sent their new value. 
     */
    public void computeMovement() {
        if (this.isLeaf) { // Domain contains bodies, let's add their mutual interactions 
            // add attractions of forces in the same Domain
            for (int i = 0 ; i < this.info.planets.length ; i++) // compute iteraction with every close planet 
                for (int j = 0 ; j < this.info.planets.length ; j++)     
                    if (i!=j)
                        this.totalForce[i].add(new Force (this.info.planets[i], this.info.planets[j])); 
                    // It would be good to have the bodies bounce away 
                    // if they are too close!
            for (int i = 0 ; i < this.info.planets.length; i++ )
                this.info.planets[i].moveWithForce(this.totalForce[i]);
        }
        
        this.info.recomputeCenterOfMass();
        sendValueToNeighbours();
    }
    
    
    
    /**
     * Update the value of a distant info (given by it's id), to allow movement update.
     * @param inf : the value which is being sent by a distant Domain
     * @param receivedIter the iteration of the distant Domain, to enable synchronization
     */
    public void setValue(Info inf , int receivedIter) {  // FIXME : this never gets called if nb Domains=1
        if (!this.canIReceiveInfoFrom[inf.identification]) {
            return;
        }
        if (this.iter == receivedIter) {
            this.nbReceived ++ ;
            if (this.nbReceived > this.nbNeighbours)  // This is a bad sign!
                killsupport.abort( new NullPointerException("Domain " + identification + " received too many answers"));
            if (this.isLeaf)
                addToTotalForce(inf);
            else
                if (isSon(inf.identification))
                    this.info.addSon(inf);
                else 
                    killsupport.abort( new NullPointerException("Domain " + identification + " received not son!!"));
            
            if (this.nbReceived == this.nbNeighbours) 
                computeMovement();
        }
        else { 
            if (this.iter > receivedIter)
                killsupport.abort(  new NullPointerException("Value arrives too late!") );
            this.prematureValues.add(new Carrier (inf, receivedIter));
        }
        
    }
    
    
    /**
     * Checks if this given identifier is a son of the current treenode.
     * @param ident the identifier of the info/Domain/treeNode to check.
     * @return true if the parameter is the identifier of one of the sons.
     */
    private boolean isSon(int ident) {   
        for (int i = 0 ; i < this.treeNode.Q.length  ; i++)
            if ( treeNode.Q[i].label == ident)
                return true; 
        return false;
    }
    
    /**
     * This adds the contribution of the given Info to the totalForces which will move the underlying bodies.  
     * @param inf the Info we have to add to the totalForces. 
     */
    private void addToTotalForce(Info distantInfo) {
        int distantLabel = distantInfo.identification;
        try {
            Force [] localForces = new Force [info.planets.length]; 
            for (int i = 0 ; i < info.planets.length ; i++)  // try to create a force for each planet
                localForces[i] = new Force (this.info.planets[i], distantInfo); 
            for (int i = 0 ; i < info.planets.length ; i++)  // if no exception, add this force to totalforce
                this.totalForce[i].add(localForces[i]) ;	
        }
        catch (TooCloseBodiesException e) { // have to split the QuadTree into smaller elements
            QuadTree distantTreeNode = quadTree.getNode(distantLabel); // the tree Node corresponding to the Info
            if (distantTreeNode.Q == null) {   // there are no subTrees, compute with every planet  
                Planet [] distantPlanets = distantInfo.planets;
                for (int j=0; j < distantPlanets.length ; j++) 
                    for (int i=0; i < this.info.planets.length ; i ++) 
                        this.totalForce[i].add(new Force (this.info.planets[i], distantPlanets[j]));
            }
            else {// since there are subtrees, put them in the queue.
                this.nbNeighbours--;   this.nbReceived--; // totally hide the existence of this Domain 
                stopReceivingInfoFrom(distantLabel);
                // What we do here is add all the siblings: since the parent is too wide-spread 
                // (TooCloseBodiesException thrown), look for a finer grain for subdivision.
                for (int i=0; i < distantTreeNode.Q.length ; i ++) { 
                    int index = distantTreeNode.Q[i].label;
                    if (index != identification) { // don't add self, no need! 
                        this.nbNeighbours ++ ; 
                        askToReceiveInfoFrom(index);
                    }
                }
            }
            
        } // end of catch 
    }
    
    /**
     * Broadcast the local value of CenterOfMass to all its neighbours.
     */
    public void sendValueToNeighbours() {
        reset () ; 
        this.iter++;
        if (this.iter < this.maxIter) {	  
            this.savedInfo[this.iter] = this.info.copy();
            this.neighbours.setValue(this.info, this.iter);
            if (this.display == null) {// if no display, only the first Domain outputs message to say recompute is going on
                if (this.identification==0 && this.iter % 50 == 0) 
                    System.out.println("Compute movement." + this.iter);
            }
            else 
                if (this.isLeaf) {  // only leaves contain enough information, concerning planets
                    for (int i = 0 ; i < this.info.planets.length ; i ++) {
                        Planet planet = this.info.planets[i];    
                        display.drawBody((int)planet.x, (int)planet.y, (int)planet.vx, (int)planet.vy, 
                                (int)planet.mass, (int)planet.diameter, planet.identification, hostName);
                    }
                }
            treatPremature();
        }
        else // finished all iterations.
            if (this.identification==0) // only need one quit signal man, and 0 always has smallest iteration!
                killsupport.quit();
    }
    
    /**
     * Resends the premature information, which is probably up-to-date now
     */
    private void treatPremature() {
        int size = this.prematureValues.size() ;
        for (int i = 0 ; i < size ; i++) {
            Carrier c = (Carrier) this.prematureValues.remove(0);
            setValue(c.inf , c.iter); // works even if c.iter > iter
        }
    }
    
    /**
     * Cleans up fields related to receiving information from distant Domains.
     */
    private void reset() {
        this.nbReceived = 0 ;
        if (this.isLeaf) 
            for (int i = 0 ; i < this.totalForce.length ; i++)  
                this.totalForce [i] = new Force();
        else 
            this.info.emptySons(); 
    }
    
    /**
     * When Domain migrates or gets restarted by Fault Tolerance servers, 
     * re-initializes the hostname information.
     */
    private void readObject(java.io.ObjectInputStream in) 
    throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        try {
            this.hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            hostName="unknown";
            e.printStackTrace();
        }
        
    }
    
    /**
     * Remove this Domain from the Domain that are allowed to give their information
     * @param distantId the identifier of the Distant Domain to remove.
     */ 
    private void stopReceivingInfoFrom(int distantId) {
        this.canIReceiveInfoFrom[distantId] = false; 
        this.domainArray[distantId].removeNeighbour(this.identification);
    }
    
    /**
     * Put tjhis Domain on the list of the Domains which send thjeir information to this.
     * @param distantId the identifier of the Domain to add to list.
     */
    private void askToReceiveInfoFrom(int distantId) {
        this.canIReceiveInfoFrom[distantId] = true; 
        this.domainArray[distantId].addNeighbour(this.identification, this.iter);
    }
    
}
