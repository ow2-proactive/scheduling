package org.objectweb.proactive.examples.nbody.oospmd;

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
import org.objectweb.proactive.examples.nbody.common.TooCloseBodiesException;


public class Domain implements Serializable{
    
    private class Carrier {
        Info inf;
        int iter;
        
        Carrier (Info inf, int iter) {
            this.inf = inf;
            this.iter = iter;
        }
    }
    
    public int identification;
    
    private Displayer display = null;
    private String hostName = "unknown";
    
    private Domain [] domainArray; // a copy of the above domainGroup, but as an array
    private Domain neighbours; // Domains that need to know my coordinates 
    private QuadTree quadTree, treeNode;
    
    private Info info; 
    private Force [] totalForce;  
    private int nbNeighbours = 0, nbReceived = 0;
    
    private boolean isLeaf;
    
    private int iter = 0, maxIter;
    
    private Vector prematureValues;
    
    private Info [] savedInfo;
    
    private boolean [] listNeighId;
    
    private boolean [] canIReceiveInfoFrom;
    
    
    public Domain (){}
    
    /**
     * A new Domain, which is *NOT* initialized (init must be called).
     * @param info the Info linked to this Domain 
     * @param id the unique integer which unambiguously denotes this Domain.
     */
    public Domain (Integer id, Info info) {
        this.info = info;
        this.prematureValues = new Vector();
        this.identification = id.intValue();
        this.domainArray= null; // will be correctly initialized later on
        try {
            this.neighbours = (Domain) ProActiveGroup.newGroup( Domain.class.getName() );
        }
        catch (ClassNotReifiableException e) { org.objectweb.proactive.examples.nbody.common.Start.abort(e); }
        catch (ClassNotFoundException e) { org.objectweb.proactive.examples.nbody.common.Start.abort(e); }
        try {
            this.hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Assigns neighbours, the displayer, and the treenode corresponding to this Domain.
     * @param domainArray the array of Domains with which to communicate positions
     * @param dp the displayer, to show the evolution of the bodies  
     * @param tree The tree structure giving the relation between all the Domains
     * @param maxIter the number of iterations before stoppping  
     */
    public void init(Domain [] domainArray, Displayer dp, QuadTree tree, int maxIter) {
        init(domainArray,  tree, maxIter);
        this.display=dp;
    }
    
    /**
     * Assigns neighbours, and the treenode corresponding to this Domain.
     * @param domainArray an array of Domains with which to communicate
     * @param tree the QuadTree which allows to find more Infos when needed
     * @param maxIter the number of iterations before stoppping
     */
    public void init(Domain [] domainArray,  QuadTree tree, int maxIter) {
        this.maxIter = maxIter;
        this.savedInfo = new Info [maxIter];
        this.savedInfo[0] = this.info.copy();
        this.prematureValues = new Vector();
        this.maxIter = maxIter;
        this.quadTree = tree ;
        this.treeNode = tree.getNode(identification);
        this.domainArray = domainArray;
        this.listNeighId = new boolean[domainArray.length];
        for (int i =0 ; i <listNeighId.length ; i++)
            listNeighId[i]= false;
        this.canIReceiveInfoFrom= new boolean[domainArray.length];
        for (int i =0 ; i <listNeighId.length ; i++)
            canIReceiveInfoFrom[i] = false;
        this.isLeaf = (treeNode.Q==null);
        if (isLeaf) {
            totalForce = new Force [info.planets.length];
            for (int i = 0 ; i < totalForce.length ; i++)  
                this.totalForce [i] = new Force();
        }
        ProSPMD.barrier("all Domains are initialized");	// all Domains must be initialized before communication takes place.
        ((Domain) ProActive.getStubOnThis()).finishInit();
    }
    
    
    /**
     * Can only be called once all Domains are ready to receive addNeighbour.
     * That's why it is called after the synchro() hand-made barrier 
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
            throw new NullPointerException("XXXXXXXXXXXXXXXXXXXXX  Domain " +
                    domainIdent + "["+checkIter+"] asking for a future info to Domain"  + identification + "[" + iter + "] , not possible!");
        }
        Group gr = ProActiveGroup.getGroup(neighbours);
        
        gr.add(domainArray[domainIdent]);
        for (int i = checkIter ; i <= iter ; i++)   	// resend left out information
            domainArray[domainIdent].setValue(this.savedInfo[i], i);
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
            throw new NoSuchElementException("Domain " + domainIdent + " cannot be removed, because it's not in neighbour Group! ");
    }
    
    
    /**
     * Computes the new position of the center of mass of this domain.
     * Should only be called once all neighbouring nodes have sent their new value. 
     */
    public void computeMovement() {
        if (isLeaf) { // Domain contains bodies, let's add their mutual interactions 
            // add attractions of forces in the same Domain
            for (int i = 0 ; i < info.planets.length ; i++) // compute iteraction with every close planet 
                for (int j = 0 ; j < info.planets.length ; j++)     
                    if (i!=j)
                        totalForce[i].add(new Force (info.planets[i], info.planets[j])); 
                    // It would be good to have the bodies bounce away 
                    // if they are too close!
            for (int i = 0 ; i < info.planets.length; i++ )
                info.planets[i].moveWithForce(totalForce[i]);
        }
        
        info.recomputeCenterOfMass();
        sendValueToNeighbours();
    }
    
    
    
    /**
     * Update the value of a distant info (given by it's id), to allow movement update.
     * @param inf : the value which is being sent by a distant Domain
     * @param receivedIter the iteration of the distant Domain, to enable synchronization
     */
    public void setValue(Info inf , int receivedIter) {  // FIXME : this never gets called if nb Domains=1
        if (!canIReceiveInfoFrom[inf.identification]) {
            return;
        }
        if (iter == receivedIter) {
            nbReceived ++ ;
            if (nbReceived > nbNeighbours)  // This is a bad sign!
                throw new NullPointerException("Domain " + identification + " received too many answers");
            if (isLeaf)
                addToTotalForce(inf);
            else
                if (isSon(inf.identification))
                    info.addSon(inf);
                
            if (nbReceived == nbNeighbours) 
                computeMovement();
        }
        else { 
            if (iter > receivedIter)
                throw new NullPointerException("Value arrives too late!");
            this.prematureValues.add(new Carrier (inf, receivedIter));
        }
        
    }
    
    
    /**
     * Checks if this given identifier is a son of the current treenode.
     * @param ident the identifier of the info/Domain/treeNode to check.
     * @return true if the parameter is the identifier of one of the sons.
     */
    private boolean isSon(int ident) {   
        for (int i = 0 ; i < treeNode.Q.length  ; i++)
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
                localForces[i] = new Force (info.planets[i], distantInfo); 
            for (int i = 0 ; i < info.planets.length ; i++)  // if no exception, add this force to totalforce
                this.totalForce[i].add(localForces[i]) ;	
        }
        catch (TooCloseBodiesException e) { // have to split the QuadTree into smaller elements
            QuadTree distantTreeNode = quadTree.getNode(distantLabel); // the tree Node corresponding to the Info
            if (distantTreeNode.Q == null) {   // there are no subTrees, compute with every planet  
                Planet [] distantPlanets = distantInfo.planets;
                for (int j=0; j < distantPlanets.length ; j++) 
                    for (int i=0; i < info.planets.length ; i ++) 
                        this.totalForce[i].add(new Force (info.planets[i], distantPlanets[j]));
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
        iter++;
        if (iter < maxIter) {	  
            savedInfo[this.iter] = this.info.copy();
            neighbours.setValue(this.info, this.iter);
            treatPremature();
            if (display == null) {// if no display, only the first Domain outputs message to say recompute is going on
                if (identification==0 && iter % 50 == 0) 
                    System.out.println("Compute movement." + iter);
            }
            else 
                if (isLeaf) {  // only leaves contain enough information, concerning planets
                    for (int i = 0 ; i < info.planets.length ; i ++) {
                        Planet planet = info.planets[i];    
                        display.drawBody((int)planet.x, (int)planet.y, (int)planet.vx, (int)planet.vy, 
                                (int)planet.mass, (int)planet.diameter, planet.identification, hostName);
                    }
                }
        }
        else // finished all iterations.
            if (identification==0) // only need one quit signal man, and 0 always has smallest iteration!
                org.objectweb.proactive.examples.nbody.common.Start.quit();
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
        if (isLeaf) 
            for (int i = 0 ; i < totalForce.length ; i++)  
                this.totalForce [i] = new Force();
        else 
            this.info.emptySons(); 
    }
    
    /**
     * When Domain migrates or gets restarted by Fault Tolerance servers, re-initializes the hostname information.
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
    
    private void stopReceivingInfoFrom(int distantId) {
        canIReceiveInfoFrom[distantId] = false; 
        domainArray[distantId].removeNeighbour(this.identification);
    }
    
    private void askToReceiveInfoFrom(int distantId) {
        canIReceiveInfoFrom[distantId] = true; 
        domainArray[distantId].addNeighbour(this.identification, this.iter);
    }
    
}
