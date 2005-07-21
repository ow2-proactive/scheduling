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
package org.objectweb.proactive.core.runtime.http;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.ft.checkpointing.Checkpoint;
import org.objectweb.proactive.core.body.http.util.exceptions.HTTPRemoteException;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.process.UniversalProcess;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.VMInformation;
import org.objectweb.proactive.core.runtime.http.messages.RuntimeRequest;
import org.objectweb.proactive.ext.security.PolicyServer;
import org.objectweb.proactive.ext.security.ProActiveSecurityManager;
import org.objectweb.proactive.ext.security.SecurityContext;
import org.objectweb.proactive.ext.security.exceptions.SecurityNotAvailableException;

 
/**
 *   An adapter for a ProActiveRuntime to be able to receive remote calls usinfg HTTP. This helps isolate HTTP-specific
 *   code into a small set of specific classes, thus enabling reuse if we one day decide to switch
 *   to anothe remote objects library.
 *          @see <a href="http://www.javaworld.com/javaworld/jw-05-1999/jw-05-networked_p.html">Adapter Pattern</a>
 */
public class HttpRemoteRuntimeAdapterImpl implements ProActiveRuntime {
    private static transient Logger logger = Logger.getLogger("XML_HTTP");
    private String url;
//    private int port;

    //this boolean is used when killing the runtime. Indeed in case of co-allocation, we avoid a second call to the runtime
    // which is already dead
    protected boolean alreadykilled = false;

    /**
     * 
     */
    public HttpRemoteRuntimeAdapterImpl(HttpRuntimeAdapter newruntimeadapter,
        String newurl) {
    //    System.out.println("------------------------ >>>>>>>>>>>>>>>>>> Adapter URL = " + newurl );
        logger.debug("Adapter URL = " + newurl);
        this.url = newurl;
       
//        this.port = UrlBuilder.getPortFromUrl(newurl);
        logger.debug("New Remote XML Adapter : " + url );
    }

    //
    // -- Implements ProActiveRuntime -----------------------------------------------
    //
    public String createLocalNode(String nodeName,
        boolean replacePreviousBinding, PolicyServer ps, String vname,
        String jobId) throws NodeException {
   
            ArrayList paramsList = new ArrayList();
            paramsList.add(nodeName);
            paramsList.add(new Boolean(replacePreviousBinding));
            paramsList.add(ps);
            paramsList.add(vname);
            paramsList.add(jobId);

            
            RuntimeRequest req = new RuntimeRequest("createLocalNode",
                    paramsList, this.url);

 

    
            try {
                req.send ();
                return (String) req.getReturnedObject();
                }  
            	catch (Exception e) {
                  throw new NodeException(e);
                }
            
    }

    public void killAllNodes() throws ProActiveException {

        try {
            new RuntimeRequest("killAllNodes", this.url).send ();
        } catch (HTTPRemoteException e) {            
            e.printStackTrace();
        }

    }

    public void killNode(String nodeName) throws ProActiveException{
        ArrayList params = new ArrayList();
        params.add(nodeName);
        try {
            new RuntimeRequest("killNode", params, this.url).send();
        } catch (HTTPRemoteException e) {        
            e.printStackTrace();
        }
    }

    public void createVM(UniversalProcess remoteProcess)
        throws IOException, ProActiveException {

        ArrayList params = new ArrayList();
        params.add(remoteProcess);

        new RuntimeRequest("createVM", params, this.url).send();
        
    }

    public String[] getLocalNodeNames() throws ProActiveException {
        	RuntimeRequest req = new RuntimeRequest(
            "getLocalNodeNames", this.url);
        	
          
                try {
                    req.send ();
                    return (String[]) req.getReturnedObject();
                } catch (Exception e) {
                    throw new ProActiveException(e);
                }
                
    }

    public VMInformation getVMInformation() {
        	RuntimeRequest req = new RuntimeRequest(
            "getVMInformation", this.url);
    
          
                try {
                	req.send ();
                    return (VMInformation) req.getReturnedObject();
                } catch (Exception e) {
                    e.printStackTrace();
                }
        return null;
    }

    public void register(ProActiveRuntime proActiveRuntimeDist,
        String proActiveRuntimeName, String creatorID, String creationProtocol,
        String vmName) {
        	
            ArrayList params = new ArrayList();
            ArrayList paramsTypes = new ArrayList();

            params.add(proActiveRuntimeDist);
            paramsTypes.add(ProActiveRuntime.class);
            params.add(proActiveRuntimeName);
            paramsTypes.add(String.class);
            params.add(creatorID);
            paramsTypes.add(String.class);
            params.add(creationProtocol);
            paramsTypes.add(String.class);
            params.add(vmName);
            paramsTypes.add(String.class);
       
             try {
                new RuntimeRequest("register", params,
                        paramsTypes, this.url).send();
            } catch (HTTPRemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#unregister(org.objectweb.proactive.core.runtime.ProActiveRuntime, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void unregister(ProActiveRuntime proActiveRuntimeDist,
        String proActiveRuntimeUrl, String creatorID, String creationProtocol,
        String vmName) {
      
            ArrayList params = new ArrayList();
            ArrayList paramsTypes = new ArrayList();

            params.add(proActiveRuntimeDist);
            paramsTypes.add(ProActiveRuntime.class);
            params.add(proActiveRuntimeUrl);
            paramsTypes.add(String.class);
            params.add(creatorID);
            paramsTypes.add(String.class);
            params.add(creationProtocol);
            paramsTypes.add(String.class);
            params.add(vmName);
            paramsTypes.add(String.class);
            try {
                new RuntimeRequest("unregister", params,
                        paramsTypes, this.url).send ();
            } catch (HTTPRemoteException e) {                
                e.printStackTrace();
            }
      
    }

    public ProActiveRuntime[] getProActiveRuntimes() throws ProActiveException {
        	RuntimeRequest req = new RuntimeRequest(
            "getProActiveRuntimes", this.url);
      
        	
                try {
                  	req.send ();
                    return (ProActiveRuntime[]) req.getReturnedObject();
                } catch (Exception e) {
                    throw new ProActiveException(e);
                }
            
          }

    public ProActiveRuntime getProActiveRuntime(String proActiveRuntimeName)
        throws ProActiveException {
        ArrayList params = new ArrayList();
        params.add(proActiveRuntimeName);

        RuntimeRequest req = new RuntimeRequest(
                "getProActiveRuntime", params, this.url);
    
        
                try {
                    req.send ();
                    return (ProActiveRuntime) req.getReturnedObject();
                } catch (Exception e) {
          throw new ProActiveException(e);
                }
        
        
    }

    public void killRT(boolean softly)  {
        if (!alreadykilled) {
            ArrayList params = new ArrayList();
            params.add(new Boolean(softly));

             try {
                new RuntimeRequest("killRT", params, this.url).send ();
            } catch (HTTPRemoteException e) {
                // TODO Auto-generated catch block
                //e.printStackTrace();
            } 
        }

        alreadykilled = true;
    }

    public String getURL() throws ProActiveException {
        //return runtimeadapter.getStrategyURL();
        throw new ProActiveException("This method should never be called," +
            " since the Adapter already does the job.");
    }

    public ArrayList getActiveObjects(String nodeName)
        throws ProActiveException {
        ArrayList params = new ArrayList();
        params.add(nodeName);

        RuntimeRequest req = new RuntimeRequest(
                "getActiveObjects", params, this.url);
     
      
                try {
                    req.send ();
                    return (ArrayList) req.getReturnedObject();
                } catch (Exception e) {
                    throw new ProActiveException(e);
                }
         
    }

    public ArrayList getActiveObjects(String nodeName, String objectName)
        throws ProActiveException {
        ArrayList params = new ArrayList();
        params.add(nodeName);
        params.add(objectName);

      RuntimeRequest req = new RuntimeRequest(
              "getActiveObjects", params, this.url);
   
          
            try {
                req.send ();
                return (ArrayList) req.getReturnedObject();
            } catch (Exception e) {
                throw  new ProActiveException(e);
            }
      
    }

    public VirtualNode getVirtualNode(String virtualNodeName)
        throws ProActiveException {
        ArrayList params = new ArrayList();
        params.add(virtualNodeName);
        RuntimeRequest req = new RuntimeRequest(
                "getVirtualNode", params, this.url);
  
            
                try {
                    req.send ();
                    return (VirtualNode) req.getReturnedObject();
                } catch (Exception e) {
                    throw  new ProActiveException(e);
                }
            
    }

    public void registerVirtualNode(String virtualNodeName,
        boolean replacePreviousBinding) throws ProActiveException {
        ArrayList params = new ArrayList();
        params.add(virtualNodeName);
        params.add(new Boolean(replacePreviousBinding));

      
      try {
        new RuntimeRequest("registerVirtualNode", params, this.url).send();
    } catch (HTTPRemoteException e) {

        e.printStackTrace();
    }
      
    }

    public void unregisterVirtualNode(String virtualNodeName)
        throws ProActiveException {
        ArrayList params = new ArrayList();
        params.add(virtualNodeName);

      
      try {
        new RuntimeRequest("unregisterVirtualNode", params, this.url).send();
    } catch (HTTPRemoteException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
      
    }

    public void unregisterAllVirtualNodes() throws ProActiveException {
            try {
                new RuntimeRequest("unregisterAllVirtualNodes", this.url).send();
            } catch (HTTPRemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }      
    }

    public UniversalBody createBody(String nodeName,
        ConstructorCall bodyConstructorCall, boolean isNodeLocal)
        throws ProActiveException, ConstructorCallExecutionFailedException, 
            InvocationTargetException {
        ArrayList params = new ArrayList();
        params.add(nodeName);
        params.add(bodyConstructorCall);
        params.add(new Boolean(isNodeLocal));
        
        RuntimeRequest req = new RuntimeRequest(
                "createBody", params, this.url);
  
    
            try {
                req.send ();    
                return  (UniversalBody) req.getReturnedObject();
            } catch (ConstructorCallExecutionFailedException e){
                throw e;
            } catch ( InvocationTargetException e) {
                throw e;
            } catch (Exception e) {
                throw  new ProActiveException(e);
            }
       
    }

    public UniversalBody receiveBody(String nodeName, Body body)
        throws ProActiveException {
        ArrayList params = new ArrayList();
        params.add(nodeName);
        params.add(body);

        	RuntimeRequest req = new RuntimeRequest(
                    "receiveBody", params, this.url);
       
       
                try {
                 	req.send ();
                    return (UniversalBody)req.getReturnedObject();
                } catch (Exception e) {
                    throw  new ProActiveException(e);
                }
           
    }

    // SECURITY 
    public PolicyServer getPolicyServer() throws ProActiveException {
    
        RuntimeRequest req = new RuntimeRequest(
        "getPolicyServer", this.url);
    
        
            try {
                req.send ();
                return (PolicyServer) req.getReturnedObject();
            } catch (Exception e) {
                throw  new ProActiveException(e);
            }
        
    }

    public void setProActiveSecurityManager(ProActiveSecurityManager ps)
        throws ProActiveException {
        ArrayList params = new ArrayList();
        params.add(ps);

        
        try {
            new RuntimeRequest("setProActiveSecurityManager", params, this.url).send ();
        } catch (HTTPRemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getCreatorCertificate()
     */
    public X509Certificate getCreatorCertificate() throws ProActiveException {
        RuntimeRequest req = new RuntimeRequest(
        "getCreatorCertificate", this.url);
   
          
                try {
                    req.send ();
                    return (X509Certificate) req.getReturnedObject();
                } catch (Exception e) {
                    throw  new ProActiveException(e);
                }
            
    }

    public String getVNName(String nodename) throws ProActiveException {
        ArrayList params = new ArrayList();
        params.add(nodename);

        	RuntimeRequest req = new RuntimeRequest("getVNName", params, this.url);
    
        	
            
                try {
                	req.send ();
                    return (String) req.getReturnedObject();
                } catch (Exception e) {
                    throw  new ProActiveException(e);
                }
           
        
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#setDefaultNodeVirtualNodeName(java.lang.String)
     */
    public void setDefaultNodeVirtualNodeName(String s)
        throws ProActiveException {
        ArrayList params = new ArrayList();
        params.add(s);
        try {
            new RuntimeRequest("setDefaultNodeVirtualNodeName",
                        params, this.url).send ();
        } catch (HTTPRemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
      
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getNodePolicyServer(java.lang.String)
     */
    public PolicyServer getNodePolicyServer(String nodeName)
        throws ProActiveException {
        ArrayList params = new ArrayList();
        params.add(nodeName);

        	RuntimeRequest req = new RuntimeRequest(
                    "getNodePolicyServer", params, this.url);

       
                try {
                	req.send ();
                    return (PolicyServer) req.getReturnedObject();
                } catch (Exception e) {
                    throw  new ProActiveException(e);
                }
    }
           

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#enableSecurityIfNeeded()
     */
    public void enableSecurityIfNeeded() throws ProActiveException {
        
        try {
            new RuntimeRequest("enableSecurityIfNeeded", this.url).send ();
        } catch (HTTPRemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getNodeCertificate(java.lang.String)
     */
    public X509Certificate getNodeCertificate(String nodeName)
        throws ProActiveException {
        ArrayList params = new ArrayList();
        params.add(nodeName);

        RuntimeRequest req = new RuntimeRequest(
                "getNodeCertificate", params, this.url);
        
          
            try {
                req.send ();	
                return (X509Certificate) req.getReturnedObject();
            } catch (Exception e) {
                throw  new ProActiveException(e);
            }
     
    
    }

    /**
     * @param nodeName
     * @return returns all entities associated to the node
     */
    public ArrayList getEntities(String nodeName) throws ProActiveException {
        ArrayList params = new ArrayList();
        params.add(nodeName);
        RuntimeRequest req = new RuntimeRequest("getEntities",
                params, this.url);
  
       
            try {
                req.send ();      
                return (ArrayList) req.getReturnedObject();
            } catch (Exception e) {
                throw  new ProActiveException(e);
            }
      
    }

    /**
     * @param uBody
     * @return returns all entities associated to the node
     */
    public ArrayList getEntities(UniversalBody uBody) throws ProActiveException {
        ArrayList params = new ArrayList();
        params.add(uBody);

      RuntimeRequest req = new RuntimeRequest("getEntities",
              params, this.url);
      
         
                try {
                    req.send ();
                    return (ArrayList) req.getReturnedObject();
                } catch (Exception e) {
                    throw  new ProActiveException(e);
                }
       
    }

    /**
     * @return returns all entities associated to this runtime
     */
    public ArrayList getEntities() throws ProActiveException {
        RuntimeRequest req = new RuntimeRequest("getEntities", this.url);
      
     
            try {
                req.send ();
                return (ArrayList) req.getReturnedObject();
            } catch (Exception e) {
                throw  new ProActiveException(e);
            }
     
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getJobID(java.lang.String)
     */
    public String getJobID(String nodeUrl) throws ProActiveException {
        ArrayList params = new ArrayList();
        params.add(nodeUrl);

        RuntimeRequest req = new RuntimeRequest("getJobID", params, this.url);
   
         
            try {
                req.send ();
                return (String) req.getReturnedObject();
            } catch (Exception e) {
                throw  new ProActiveException(e);
            }
    
    }

    public String[] getNodesNames() throws ProActiveException {
        
        RuntimeRequest req = new RuntimeRequest("getNodesNames", this.url);
    
      
            try {
                req.send ();
                return (String[]) req.getReturnedObject();
            } catch (Exception e) {
                throw  new ProActiveException(e);
            }
        
    }

    /**
     * @see org.objectweb.proactive.Job#getJobID()
     */
    public String getJobID() {
        
        RuntimeRequest req = new RuntimeRequest("getJobID", this.url);
    
        
        
            try {
                req.send ();
                return (String)req.getReturnedObject();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
     
       
    }

    
    public void addAcquaintance(String proActiveRuntimeName) {
        ArrayList params = new ArrayList();
        params.add(proActiveRuntimeName);

        
        try {
            new RuntimeRequest("addAcquaintance", params, this.url).send ();
        } catch (HTTPRemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    
    }

    public String[] getAcquaintances() {
        RuntimeRequest req = new RuntimeRequest("getAcquaintances", this.url);
      
        
       
                try {
                    req.send ();
                    return (String[]) req.getReturnedObject();
                } catch (Exception e) {
                   
                    e.printStackTrace();
                    return null;
                }
                
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#rmAcquaintance(java.lang.String)
     */
    public void rmAcquaintance(String proActiveRuntimeName) {
        ArrayList params = new ArrayList();
        params.add(proActiveRuntimeName);
        try {
            new RuntimeRequest("rmAcquaintance", params, this.url).send ();
        } catch (HTTPRemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public SecurityContext getPolicy(SecurityContext sc)
        throws ProActiveException, SecurityNotAvailableException {
        ArrayList params = new ArrayList();
        params.add(sc);

        RuntimeRequest req = new RuntimeRequest(
                "getPolicy", params, this.url);
   
        

    
                try {
                    req.send ();
                    return (SecurityContext) req.getReturnedObject();
                } catch ( SecurityNotAvailableException e) {
                    throw e;
                }
                catch (Exception e) {
                    throw new ProActiveException(e);
                }
           
    }

    public byte[] getClassDataFromParentRuntime(String className)
        throws ProActiveException {
        ArrayList params = new ArrayList();
        params.add(className);

        RuntimeRequest req = new RuntimeRequest(
                "getClassDataFromParentRuntime", params, this.url);
      
            
                try {
                    req.send ();
                    return (byte[]) req.getReturnedObject();
                } catch (Exception e) {
                    throw new ProActiveException(e);
                }
            
    }

    public byte[] getClassDataFromThisRuntime(String className) {
        ArrayList params = new ArrayList();
        params.add(className);

        RuntimeRequest req = new RuntimeRequest(
                "getClassDataFromThisRuntime", params, this.url);
       
          
                try {
                    req.send ();
                    return (byte[]) req.getReturnedObject();
                } catch (Exception e) {
            e.printStackTrace();
            return null;
                }
          

    }

    public void setParent(String parentRuntimeName) {
        ArrayList params = new ArrayList();
        params.add(parentRuntimeName);

        	try {
                new RuntimeRequest("setParent", params, this.url).send ();
            } catch (HTTPRemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

    }

    public void listVirtualNodes() throws ProActiveException {
        
            try {
                new RuntimeRequest("listVirtualNodes", this.url).send ();
            } catch (HTTPRemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#receiveCheckpoint(java.lang.String, org.objectweb.proactive.core.body.ft.checkpointing.Checkpoint, int)
     */
    public UniversalBody receiveCheckpoint(String nodeName, Checkpoint ckpt,
        int inc) throws ProActiveException {
        return null;
    }
}
