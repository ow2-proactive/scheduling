/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.body.ft.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.ft.checkpointing.Checkpoint;
import org.objectweb.proactive.core.body.ft.checkpointing.CheckpointInfo;
import org.objectweb.proactive.core.body.ft.internalmsg.FTMessage;
import org.objectweb.proactive.core.body.ft.internalmsg.GlobalStateCompletion;
import org.objectweb.proactive.core.body.ft.internalmsg.Heartbeat;
import org.objectweb.proactive.core.body.ft.protocols.cic.CheckpointInfoCIC;
import org.objectweb.proactive.core.body.ft.util.faultdetection.FaultDetector;
import org.objectweb.proactive.core.body.ft.util.location.LocationServer;
import org.objectweb.proactive.core.body.ft.util.recovery.RecoveryProcess;
import org.objectweb.proactive.core.body.ft.util.resource.ResourceServer;
import org.objectweb.proactive.core.body.ft.util.storage.CheckpointServer;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.rmi.ClassServerHelper;


/**
 * A Generic fault-tolerance Server. This class implements all ft.util.* interfaces.
 * This server is an RMI object.
 * @author cdelbe
 * @since ProActive 2.2
 */

public class GlobalFTServer extends UnicastRemoteObject implements 
	CheckpointServer, RecoveryProcess, LocationServer, FaultDetector, ResourceServer {
    
    /** Default server port */
    public static final int DEFAULT_PORT = 1100;
    
    /** Period of the failure detector sanning (ms) */
    public static final int DEFAULT_FDETECT_SCAN_PERIOD = 10000;
    
    /** Period of the checkpoints garbage collection (ms) */
    public static final int DEFAULT_GC_PERIOD = 40000;
    
    /** Default name of this server */
    public static final String DEFAULT_SERVER_NAME = "FTServer";
    
    
    // max number of global states :(
    private static final int NB_CKPT_MAX = 500;
    
    
    // ClassFileServer and the assiociated codebase
    private static ClassServerHelper classServerHelper = new ClassServerHelper(); 
    private String codebase;
    
    // logger
    protected static Logger logger = Logger.getLogger(GlobalFTServer.class.getName());
    
    /**
     * Create a new FTServer.
     * @param fdPeriod the period of fault detection
     * @throws RemoteException
     */
	public GlobalFTServer(int fdPeriod) throws RemoteException{
		
		//classloader
		try {
            GlobalFTServer.classServerHelper.setShouldCreateClassServer(true);
            GlobalFTServer.classServerHelper.setClasspath(System.getProperty ("java.class.path"));
            this.codebase = GlobalFTServer.classServerHelper.initializeClassServer();
            System.setProperty("java.rmi.server.codebase", this.codebase);
            logger.info("ClassServer is bound on " + this.codebase);
        } catch (IOException e) {
            System.err.println("** ERROR ** Unable to launch FT server : ");
            e.printStackTrace();
        }
	    	    
	    // CheckpointServer	    
	    this.storage = new Hashtable();
		this.stateMonitor = new int[NB_CKPT_MAX];
		this.lastGlobalState = 0;
		this.recoveryLineMonitor = new int[NB_CKPT_MAX];
		this.recoveryLine = 1;
		this.lastRegisteredCkpt = 0;
		this.lastUpdatedCkpt = 0;
		this.globalIncarnation = 1;
		new GarbageCollector(this, GlobalFTServer.DEFAULT_GC_PERIOD).start();
		this.displayCkptSize = false; // debugging stuff
		
		// LocationServer
		this.freeNodes = new ArrayList();
		this.locations = new Hashtable();
		
		// RecoveryProcess
		this.bodies = new Hashtable();
		
		// Fault Detection
		if (fdPeriod!=0){
		    this.faultDetectionPeriod = fdPeriod*1000;
		}else{
		    this.faultDetectionPeriod = GlobalFTServer.DEFAULT_FDETECT_SCAN_PERIOD;
		}		    
		this.startFailureDetector(this,this);
			

		
	}
    
    
    
    //////////////////////////////////////////////////////////////////
    //////////////////////// CheckpointServer ////////////////////////
    //////////////////////////////////////////////////////////////////
    
	// simulate the stable storage (idCheckpointer --> list of checkpoints)
	private Hashtable storage;	

	//monitoring latest global state
	private int[] stateMonitor;
	private int lastGlobalState;
	private int[] recoveryLineMonitor;
	private int recoveryLine;
	private int lastRegisteredCkpt;
	private int lastUpdatedCkpt;
	private int globalIncarnation;
	private boolean displayCkptSize;
	
	
	



	public synchronized int storeCheckpoint(Checkpoint c, int inc){
	    
	    if (inc<this.globalIncarnation){
	        logger.warn("Object with old incarnation " + inc + " is trying to store checkpoint");
	        return 0;
	    }
	    
		ArrayList ckptList = (ArrayList)storage.get(c.getBodyID());	    
	    // the first checkpoint ...
	    if (ckptList == null){
			// new storage slot
			ArrayList checkpoints = new ArrayList();
			//dummy first checkpoint
			checkpoints.add(new Object());
			UniqueID id = c.getBodyID();
			storage.put(id, checkpoints);
			checkpoints.add(c);
	    } else {
			ckptList.add(c);
		}
	    
		// updating monitoring
		int index = c.getIndex();
		if (index > this.lastRegisteredCkpt){this.lastRegisteredCkpt = index;}
		this.stateMonitor[index]++;
		//this.checkLastGlobalState();
		logger.info("[CKPT] Receive checkpoint indexed "+index+" from body " + c.getBodyID());// + "[" + System.currentTimeMillis() + "]");
		if (displayCkptSize){
		    logger.info("[CKPT] Size of ckpt " + index +" before addInfo : " + this.getSize(c) + " bytes");
		}
		
		// broadcast history closure if a new globalState is built
		if (this.checkLastGlobalState()){
		    try {
		        this.broadcastFTEvent(new GlobalStateCompletion(this.lastGlobalState));
		    } catch (RemoteException e) {
		        // an active object seems to be failed ...
		        this.forceDetection();
		    }
		}		
		return this.lastGlobalState;	   
	}



	public Checkpoint getCheckpoint(UniqueID id, int index){
		ArrayList checkpoints = (java.util.ArrayList)(storage.get(id));
		Iterator it = checkpoints.iterator();
		while (it.hasNext()){
			Checkpoint ckpt = (Checkpoint)(it.next());
			if ( ckpt.getIndex()==index ){
				return ckpt;
			}
		}
		return null;
	}
	
	

	public Checkpoint getLastCheckpoint(UniqueID id){
		ArrayList checkpoints = (java.util.ArrayList)(storage.get(id));
		int size=checkpoints.size();
		return (Checkpoint)(checkpoints.get(size-1));
	}
	
	

	public CheckpointInfo getInfoFromCheckpoint(UniqueID id, int sequenceNumber) {
		ArrayList checkpoints = (ArrayList)(storage.get(id));
		// WARNING : list(1)=ckpt 1 
		Checkpoint c = (Checkpoint)(checkpoints.get(sequenceNumber)); 
		return c.getCheckpointInfo();
	}


	// set infos for the id-th checkpoints of id
	public synchronized void addInfoToCheckpoint(CheckpointInfo ci, UniqueID id, int sequenceNumber, int inc) throws RemoteException {	    
	    if (inc < this.globalIncarnation){
	        logger.warn("Object with old incarnation " + inc + " is trying to store checkpoint infos");
	        return;
	    }
	    
	    ArrayList checkpoints = (ArrayList)(storage.get(id));
		// WARNING : list(1)=ckpt 1
		Checkpoint c = (Checkpoint)(checkpoints.get(sequenceNumber)); 
		if (c.getCheckpointInfo() != null){
			logger.error("[CKPT] **ERROR** INFOS already set for checkpoint " + sequenceNumber);
		}
		c.setCheckpointInfo(ci);
		logger.info("[CKPT] INFOS have been added to checkpoint " + sequenceNumber);
		if (displayCkptSize){
		    logger.info("[CKPT] Size of infos for ckpt " + sequenceNumber + " : " + this.getSize(ci) + " bytes");
		    logger.info("[CKPT] Total size of ckpt " + sequenceNumber + " : " + this.getSize(c) + " bytes" );
		}
		// updating monitoring
		int index = c.getIndex();
		if (index > this.lastUpdatedCkpt){this.lastUpdatedCkpt = index;}
		this.recoveryLineMonitor[index]++;
		int currentLine = this.checkRecoveryLine();
	}
	
	

	public String getServerCodebase(){
	    return this.codebase;
	}
	
	
	// return true if a new globalState is found
	private boolean checkLastGlobalState(){
		logger.info("[CKPT] Checking last global state...");
		int systemSize = this.bodies.size();
		int lastGB = this.lastGlobalState;
		int lastCkpt = this.lastRegisteredCkpt;
		for (int i=lastCkpt ; i>lastGB ; i--){
			int numRegistered = this.stateMonitor[i];
			if (numRegistered == systemSize){
				this.lastGlobalState = i;				
				return true;
			}
		}
		return false;
	}
	

	//return the index of the current recovery line
	private int checkRecoveryLine(){
	    logger.info("[CKPT] Checking Recovery Line...");
	    int systemSize = this.bodies.size();
	    int lastRecoveryLine = this.recoveryLine;
	    int lastUpdate = this.lastUpdatedCkpt;
	    for (int i=lastUpdate ; i>lastRecoveryLine ; i--){
	        int numUpdated = this.recoveryLineMonitor[i];
	        if (numUpdated == systemSize){
	            this.recoveryLine = i;
	            System.out.println("[CKPT] Recovery line is " + i);
	            return i;
	        }
	    }
	    return this.recoveryLine;
	    
	}

	
	// Delete unsable checkpoints, i.e. index < currentRecoveryLine
	private synchronized void garbageCollection(){
	    int recLine = this.recoveryLine;
	    int lastGS  = this.lastGlobalState;	    
	    Iterator it = this.storage.values().iterator();
	    while (it.hasNext()){
	        ArrayList ckpts = ((ArrayList)(it.next()));
	        for (int i=0;i<recLine;i++){
		        if (ckpts.get(i)!=null){
		            ckpts.remove(i);
		            ckpts.add(i,null);
		        }
		    }
	    }
	}
	
	/*
	 * This class defines the gargage collector thread.
	 * @author cdelbe
	 */
	private class GarbageCollector extends Thread{
	    
	    private GlobalFTServer server;
	    private int period;
	    
	    public GarbageCollector (GlobalFTServer server, int period){
	        this.server = server;
	        this.period = period;
	    }
	    
	    public void run () {
	        while (true){
	            try {
                    Thread.sleep(period);
                    GlobalFTServer.logger.info("[CKPT] Performing Garbage Collection...");
                    server.garbageCollection();
                    GlobalFTServer.logger.info("[CKPT] Garbage Collection done.");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
	        }
	    }
	}
	
	
	
	
	
	
	//////////////////////////////////////////////////////////////////
    //////////////////////// LocationServer //////////////////////////
    //////////////////////////////////////////////////////////////////
	
	private Hashtable locations; // id <-> adapter
	private ArrayList freeNodes; // ProActiveRuntimes
	
	

	public synchronized UniversalBody searchObject(UniqueID id, UniversalBody oldLocation, UniqueID caller) throws RemoteException {        
	    UniversalBody currentLocation = (UniversalBody)(this.locations.get(id));
	    if (currentLocation == null){
	        logger.error("[LOCATION] **ERROR** " + id + " is not registered !");
	        return null;
	    } else if (currentLocation.equals(oldLocation)){
	        this.forceDetection();
	        return null;	        
	    } else {
	        // send the new location of id
	        logger.info("[LOCATION] Return the new location of " + id);
	        return currentLocation;
	    }
	}


	public synchronized ArrayList getAllLocations(){
	    return new ArrayList(locations.values());
	}
	


    public synchronized void updateLocation(UniqueID id, UniversalBody newLocation) throws RemoteException {
        UniversalBody currentLocation = (UniversalBody)(this.locations.get(id));
        if (currentLocation == null){
            // this body is not registred in the system
            // register in the recoveryProcess
            this.register(id);
            // register the location
            this.locations.put(id,newLocation);
        } else {
            if (currentLocation.equals(newLocation)){
                logger.info("[LOCATION] location of " + id +" is already " + newLocation.getNodeURL());
            } else {
                logger.info("[LOCATION] " + id +" is updating its location : " + newLocation.getNodeURL());
                this.locations.put(id,newLocation);
            }
        }
    }



    public void addFreeNode(Node n){   
        logger.info("[RESSOURCE] A node is added : " + n.getNodeInformation().getURL());
        this.freeNodes.add(n);       
    }



    private int nodeCounter = 0;
    
    public Node getFreeNode(){
        this.nodeCounter++;
        if (this.freeNodes.isEmpty()){
            logger.error("[RESSOURCE] **ERROR** There is no free node !");
            return null;
        } else {
            Node n = (Node)(this.freeNodes.get(nodeCounter%(this.freeNodes.size())));
            logger.info("[RESSOURCE] Return a free node : " + n.getNodeInformation().getURL());
            return n;
        }
        
    }

	
    
    
	//////////////////////////////////////////////////////////////////
    //////////////////////// RecoveryProcess /////////////////////////
    //////////////////////////////////////////////////////////////////	
	
	// association (bodyID --> state)
	private Hashtable bodies;
	

	
	// launch recovery process
	private void recoverFrom(int globalState, UniqueID failed){
		try {
			if (globalState==0){
			    // recovery line is not given
			    // use last recovery line
			    globalState=this.recoveryLine;
			}		    
			
			logger.info("[RECOVERY] Recovering system from " + globalState);
			this.globalIncarnation++;
		    Enumeration itBodies = this.storage.keys();
			this.lastGlobalState = globalState;
			this.lastRegisteredCkpt = globalState;
			this.recoveryLine = globalState;
			this.stateMonitor = new int[NB_CKPT_MAX];
			this.recoveryLineMonitor = new int[NB_CKPT_MAX];
			
			
			// delete unusable checkpoints
			Iterator it = this.storage.values().iterator();
		    while (it.hasNext()){
		        ArrayList ckpts = ((ArrayList)(it.next()));
		        while(ckpts.size()>globalState+1){
		            ckpts.remove(globalState+1);
		        }
		    }
		    
		    // set all the system in recovery state
		    while(itBodies.hasMoreElements()){
		        UniqueID current = (UniqueID)(itBodies.nextElement());
		        this.bodies.put(current, new Integer(RECOVERING));
		    }
		    
		    //reinit the iterator
		    itBodies = this.storage.keys();
		    
		    // for waiting the end of the recovery
		    ArrayList recoveryThreads = new ArrayList();
		    
		    // send checkpoints
		    while(itBodies.hasMoreElements()){
		        UniqueID current = (UniqueID)(itBodies.nextElement());
		        Checkpoint toSend = (Checkpoint)((ArrayList)(storage.get(current))).get(globalState);
		        if (current.equals(failed)){
		            //look for a new Runtime for this oa
		            Node node = this.getFreeNode();
		            Thread t = new RecoveryThread(toSend, this.globalIncarnation, node);
		            recoveryThreads.add(t);
		            t.start();
		        } else {		            
		            UniversalBody toRecover = (UniversalBody)(this.locations.get(current));
		            // test current OA so as to handle mutliple failures
		            boolean isDead=false;
		            try{isDead = this.isUnreachable(toRecover);}catch(Exception e){}
		            if (isDead){
			            Node node = this.getFreeNode();			            
			            Thread t = new RecoveryThread(toSend, this.globalIncarnation, node);
			            recoveryThreads.add(t);
			            t.start();
		            }else{		                
		                String nodeURL = toRecover.getNodeURL();
		                Node node = NodeFactory.getNode(nodeURL);	                
		                Thread t = new RecoveryThread(toSend, this.globalIncarnation, node);
			            recoveryThreads.add(t);
			            t.start();
		            }
		        }
		    }

		    // MUST WAIT THE TERMINAISON OF THE RECOVERY !
		    Iterator itrt = recoveryThreads.iterator();
		    while (it.hasNext()){
		        try {
                    ((Thread)(itrt.next())).join();
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
		    }
	    
		} catch (NodeException e) {
			logger.error("[RECOVERY] **ERROR** Unable to send checkpoint for recovery");
			e.printStackTrace();
		}
		
	}
	
	
	

	/*
	 * Thread for parallel recovery. One thread per actove object to recover.
	 * @author cdelbe
	 */
	private class RecoveryThread extends Thread{
		private Checkpoint toSend;
		private int incarnation;
		private Node receiver;
		
		public RecoveryThread(Checkpoint toSend, int inc, Node receiver){
			this.toSend = toSend;
			this.receiver = receiver;
			this.incarnation = inc;
			this.setName("RecoveryThread for " + this.receiver.getNodeInformation().getURL());
		}
		
		public void run () {
			try {
				logger.info("[RECOVERY] " + this.toSend.getBodyID() + " is recovering from " + toSend.getIndex() +  " ...");
			    receiver.getProActiveRuntime().receiveCheckpoint(receiver.getNodeInformation().getName(), toSend, incarnation);
			    logger.info("[RECOVERY] " + this.toSend.getBodyID() + " recovered.");
			} catch (ProActiveException e) {
				logger.error("[RECOVERY] **ERROR** Unable to recover !!!!");
				e.printStackTrace();
			}
				
		}
	}
	


	public void register(UniqueID id){
		//register with RUNNING default state
		bodies.put(id, new Integer(RUNNING));
		logger.info("[RECOVERY] Body " + id + " has registered");
	}
	
	

    public synchronized void failureDetected(UniqueID id) throws RemoteException {
     
        // id is recovering ??
        int currentState = ((Integer)this.bodies.get(id)).intValue();
        
        if (currentState == RUNNING){
            // we can suppose that id is failed
            logger.info("[RECOVERY] Failure is detected for " + id);
            this.bodies.put(id, new Integer(RECOVERING));
            this.recoverFrom(0,id); //0 for the last recovery line ...
        } else if (currentState == RECOVERING) {
            // id is recovering ...  do nothing  
        }
    }


    public synchronized void updateState(UniqueID id, int state) throws RemoteException {
        logger.info("[RECOVERY]  " + id + " is updating its state : " + state);
        this.bodies.put(id, new Integer(state));
    }



    public void broadcastFTEvent(FTMessage fte) throws RemoteException {
        new BroadcastThread(this.getAllLocations(), fte).start();
    }
    
    
	/*
	 * For broacasting a message. One thread per active object
	 * @author cdelbe
	 */
	private class BroadcastThread extends Thread{
	    private ArrayList destinations;
	    private FTMessage toSend;
		
		public BroadcastThread(ArrayList dest, FTMessage fte){
			this.destinations = dest;
			this. toSend = fte;
			this.setName("BroadcastThread");
		}
		
		public void run () {
		    Iterator itAll = destinations.iterator();
		    while (itAll.hasNext()){
		        try {
                    UniversalBody current = (UniversalBody)(itAll.next());    
                    current.receiveFTMessage(toSend);
                } catch (IOException e) {
                    GlobalFTServer.this.forceDetection();
                }		            
		    }
		}
	}
	


	/////////////////////////////////////////////////
    ///////////// FAULT DETECTOR ////////////////////
	/////////////////////////////////////////////////


    private FaultDetectorThread fdt;
    private Heartbeat hbe = new Heartbeat();
    private long faultDetectionPeriod;
    
    

    public boolean isUnreachable(UniversalBody body) throws RemoteException {        
        try {
            int res = body.receiveFTMessage(this.hbe);
        } catch (IOException e) {
            return true;
        }
        return false;
    }



    public void startFailureDetector(LocationServer ls, RecoveryProcess rp) throws RemoteException {
        this.fdt = new FaultDetectorThread(ls,rp,this);
        this.fdt.start();
    }


    public void suspendFailureDetector() throws RemoteException {
    }


    public void stopFailureDetector() throws RemoteException {
    }


    public void forceDetection(){
        this.fdt.wakeUp();
    }
    
	/*
	 * Thread for fault detection. One unique thread scans all active objects
	 * @author cdelbe
	 */
	private class FaultDetectorThread extends Thread{

		private LocationServer loc;
	    private RecoveryProcess rec;
	    private FaultDetector fdet;
		
	    
		public FaultDetectorThread(LocationServer ls, RecoveryProcess rp, FaultDetector fd){
		    this.loc = ls;
		    this.rec = rp;
		    this.fdet = fd;
			this.setName("FaultDetectorThread");
		}
		
		
		public synchronized void wakeUp(){
		    notifyAll();
		}
		
		public synchronized void pause(){
		    try {
		        this.wait(GlobalFTServer.this.faultDetectionPeriod);
		    } catch (InterruptedException e) {
		        e.printStackTrace();
		    }
		}
		
		public void run () {			
		    while (true){
		        try {
		            ArrayList al = loc.getAllLocations();
		            Iterator it = al.iterator();
		            GlobalFTServer.logger.info("[FAULT DETECTOR] Scanning " + al.size() + " objects ...");
		            while (it.hasNext()){
		                UniversalBody current = (UniversalBody)(it.next());
		                if (fdet.isUnreachable(current)){
		                    rec.failureDetected(current.getID());
		                }
		            }
		            GlobalFTServer.logger.info("[FAULT DETECTOR] End scanning.");
		            this.pause();
		        } catch (RemoteException e) {
		            e.printStackTrace();
		        }   
		    }
		}
	}
	
	

	//////////////////////////////////////////////////////////////////
    /////////////////////////// MISC /////////////////////////////////
    //////////////////////////////////////////////////////////////////
    
    
	// DEBUGGING
	private void printDebug(String location){
		System.out.println("> Current server state ("+ location +") :");
		System.out.println("   * number of registred bodies : " + this.bodies.size());
		System.out.println("   * latest registred checkpoint index : " + this.lastRegisteredCkpt);
		System.out.println("   * latest global state : " + this.lastGlobalState);
	}

	private long getSize(Serializable c){
	    try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            // serialize the body
            oos.writeObject(c);
            // store the serialized form
            return baos.toByteArray().length;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
	}
	
    public UniqueID getOneID(){
        return (UniqueID)(bodies.keys().nextElement());
    }
    
}
