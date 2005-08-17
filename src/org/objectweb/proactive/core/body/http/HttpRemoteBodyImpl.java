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
package org.objectweb.proactive.core.body.http;

import java.io.IOException;
import java.io.Serializable;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.ft.internalmsg.FTMessage;
import org.objectweb.proactive.core.body.ft.protocols.FTManager;
import org.objectweb.proactive.core.body.http.util.exceptions.HTTPRemoteException;
import org.objectweb.proactive.core.body.http.util.exceptions.HTTPUnexpectedException;
import org.objectweb.proactive.core.body.http.util.messages.BodyRequest;
import org.objectweb.proactive.core.body.http.util.messages.HttpReply;
import org.objectweb.proactive.core.body.http.util.messages.HttpRequest;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.component.request.Shortcut;
import org.objectweb.proactive.core.exceptions.NonFunctionalException;
import org.objectweb.proactive.core.exceptions.manager.NFEListener;
import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.objectweb.proactive.ext.security.Communication;
import org.objectweb.proactive.ext.security.CommunicationForbiddenException;
import org.objectweb.proactive.ext.security.Policy;
import org.objectweb.proactive.ext.security.ProActiveSecurityManager;
import org.objectweb.proactive.ext.security.SecurityContext;
import org.objectweb.proactive.ext.security.crypto.AuthenticationException;
import org.objectweb.proactive.ext.security.crypto.ConfidentialityTicket;
import org.objectweb.proactive.ext.security.crypto.KeyExchangeException;
import org.objectweb.proactive.ext.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.ext.security.exceptions.SecurityNotAvailableException;

  
/**
 *   An adapter for a LocalBody to be able to receive remote calls using HTTP. This helps isolate HTTP-specific
 *   code into a small set of specific classes.
 * @author virginie
 */
/**
 * @author vlegrand
 *
 */
public class HttpRemoteBodyImpl implements UniversalBody, Serializable {
  
    /*   
      static initializations
     A runtime MUST be launched when using this class. Indeed, when one gets a reference on an active objet 
     (for instance  when performing a lookupActive ), it is necessary to launched the http runtime with its classserver that
     will receive the reply from the reference 
     */    
    static {
        try {
            RuntimeFactory.getDefaultRuntime();
        } catch (ProActiveException e) {
            e.printStackTrace();
        }
    }
    
    private static Logger logger = Logger.getLogger("XML_HTTP");
    UniqueID bodyID;
    String jobID;
    String url;
    //    int port; // port is no more necessary .. it is also included in the URL ...

    /**
     *  Constructs an HttpRemoteBodyImpl
     * @param bodyID the uniqueID of the body
     * @param url The runtime url where the remote body is accessible
     * @param jobID jobID
     */
    public HttpRemoteBodyImpl(UniqueID bodyID, String url, String jobID) {
        this.bodyID = bodyID;
        this.url = url;
        //       this.port = UrlBuilder.getPortFromUrl(url);
        this.jobID = jobID;
    }
    

    /* *
     * @see org.objectweb.proactive.core.body.UniversalBody#receiveRequest(org.objectweb.proactive.core.body.request.Request)
     */
    public int receiveRequest(Request request)  {              
            HttpRequest req = new HttpRequest(request, bodyID, this.url);            
            try {
                req.send ();
                return req.getReturnedObject();
            } catch (Exception e) {
                
                e.printStackTrace();
            }
            return 0;
    }

    /* *
     * @see org.objectweb.proactive.core.body.UniversalBody#receiveReply(org.objectweb.proactive.core.body.reply.Reply)
     */
    public int receiveReply(Reply reply)  {
        try {    
        HttpReply rep = new HttpReply(reply, bodyID,this.url);
            rep.send();
            
                return rep.getReturnedObject();
            } catch (Exception e) {
              e.printStackTrace();
            }
            return 0;
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#terminate()
     */
    public void terminate() throws java.io.IOException {        
            (new BodyRequest("terminate", new ArrayList(), bodyID, this.url)).send();
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getNodeURL()
     */
    public String getNodeURL (){        
            BodyRequest br = new BodyRequest("getNodeURL",
                    new ArrayList(), bodyID, this.url);
            try {
                br.send ();          
                return (String)br.getReturnedObject();
            } catch (Exception e) {
                //throw new HTTPRemoteException("Unexpected exception", e);
            } 
            return null;
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getID()
     */
    public UniqueID getID() {
        return bodyID;
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#updateLocation(org.objectweb.proactive.core.UniqueID, org.objectweb.proactive.core.body.UniversalBody)
     */
    public void updateLocation(UniqueID id, UniversalBody body)
        throws IOException {
            ArrayList paramsList = new ArrayList();
            paramsList.add(id);
            paramsList.add(body);
            (new BodyRequest("updateLocation", paramsList, bodyID,this.url)).send ();
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#enableAC()
     */
    public void enableAC() throws IOException {
        (new BodyRequest("enableAC", new ArrayList(), bodyID, this.url)).send ();        
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#disableAC()
     */
    public void disableAC() throws IOException {
        (new BodyRequest("disableAC", new ArrayList(), bodyID, this.url)).send ();        
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#setImmediateService(java.lang.String)
     */
    public void setImmediateService(String methodName)
        throws IOException {
        
            ArrayList paramsList = new ArrayList();
            paramsList.add(methodName);
            (new BodyRequest("setImmediateService", paramsList,
                    bodyID, this.url)).send ();
       
    }
    
    public void setImmediateService(String methodName, Class[] parametersTypes) throws IOException {
        try {
            ArrayList paramsList = new ArrayList();
            paramsList.add(methodName);
            paramsList.add(parametersTypes);
            new BodyRequest("setImmediateService", paramsList,
                    bodyID, this.url).send();
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new HTTPUnexpectedException("Unexpected exception", e);
        }
    }
    

    public void removeImmediateService(String methodName, Class[] parametersTypes) throws IOException {
        try {
            ArrayList paramsList = new ArrayList();
            paramsList.add(methodName);
            paramsList.add(parametersTypes);
            new BodyRequest("removeImmediateService", paramsList,
                    bodyID, this.url).send();
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new HTTPUnexpectedException("Unexpected exception", e);
        }
    }


    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#initiateSession(int, org.objectweb.proactive.core.body.UniversalBody)
     */
    public void initiateSession(int type, UniversalBody body)
        throws IOException, CommunicationForbiddenException, 
            AuthenticationException, RenegotiateSessionException, 
            SecurityNotAvailableException {
  
            ArrayList paramsList = new ArrayList();
            paramsList.add(new Integer(type));
            paramsList.add(body);
            new BodyRequest("initiateSession", paramsList, bodyID, this.url).send ();
      
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#terminateSession(long)
     */
    public void terminateSession(long sessionID)
        throws IOException, SecurityNotAvailableException {
     
            ArrayList paramsList = new ArrayList();
            paramsList.add(new Long(sessionID));
            new BodyRequest("terminateSession", paramsList, bodyID, this.url).send();
      
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getCertificate()
     */
    public X509Certificate getCertificate()
        throws SecurityNotAvailableException, IOException {
        
            BodyRequest req = new BodyRequest(
                    "getCertificate", new ArrayList(), bodyID, this.url);
            
            req.send ();

            try {
                return (X509Certificate) req.getReturnedObject();
            }catch (SecurityNotAvailableException e) {

                throw e;
            }catch (Exception e) {
                e.printStackTrace();
                throw new HTTPUnexpectedException("Unexpected exception", e);
            }
            
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getPolicyFrom(java.security.cert.X509Certificate)
     */
    public Policy getPolicyFrom(X509Certificate certificate)
        throws SecurityNotAvailableException, IOException {
        
            ArrayList paramsList = new ArrayList();
            paramsList.add(certificate);
            
            BodyRequest req = new BodyRequest("getPolicyFrom",
                    paramsList, bodyID,this.url);
            req.send ();
            
            
                try {
                    return (Policy) req.getReturnedObject();
                } catch (SecurityNotAvailableException e) {
                    throw e;
                }catch (Exception e) {
                    throw new HTTPUnexpectedException("Unexpected exception", e);
                    
                }
   
        
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#startNewSession(org.objectweb.proactive.ext.security.Communication)
     */
    public long startNewSession(Communication policy)
        throws SecurityNotAvailableException, IOException, 
            RenegotiateSessionException {
        ArrayList paramsList = new ArrayList();
        paramsList.add(policy);
        BodyRequest req = new BodyRequest("startNewSession",
                 paramsList, bodyID,this.url);
        req.send ();
                try {
                    return ((Long) req.getReturnedObject()).longValue();
                }catch (SecurityNotAvailableException e) {
                    throw e;
                }catch (RenegotiateSessionException e) {
                    throw e;
                } catch (Exception e) {
                    throw new HTTPUnexpectedException("Unexpected exception", e);
                }

    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#negociateKeyReceiverSide(org.objectweb.proactive.ext.security.crypto.ConfidentialityTicket, long)
     */
    public ConfidentialityTicket negociateKeyReceiverSide(
        ConfidentialityTicket confidentialityTicket, long sessionID)
        throws SecurityNotAvailableException, KeyExchangeException, IOException {
      
            ArrayList paramsList = new ArrayList();
            paramsList.add(confidentialityTicket);
            paramsList.add(new Long(sessionID));

            BodyRequest req = new BodyRequest(
                    "negociateKeyReceiverSide", paramsList, bodyID,this.url);
            req.send ();
           
                try {
                    return (ConfidentialityTicket) req.getReturnedObject();
                }catch (SecurityNotAvailableException e) {
                    throw e;
                }catch (KeyExchangeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new HTTPUnexpectedException("Unexpected exception", e);
                }
           
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getPublicKey()
     */
    public PublicKey getPublicKey()
        throws SecurityNotAvailableException, IOException {
     
        	BodyRequest req = new BodyRequest("getPublicKey",
                    new ArrayList(), bodyID, this.url);
        	req.send();
           
                try {
                    return (PublicKey) req.getReturnedObject();
                } catch (SecurityNotAvailableException e) {
                    throw e;
                }catch (Exception e) {
                    throw new HTTPUnexpectedException("Unexpected exception", e);
                }
            
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#randomValue(long, byte[])
     */
    public byte[] randomValue(long sessionID, byte[] cl_rand)
        throws SecurityNotAvailableException, Exception {
    
            ArrayList paramsList = new ArrayList();
            paramsList.add(new Long(sessionID));
            paramsList.add(cl_rand);
            
            BodyRequest req = new BodyRequest("randomValue",
                    paramsList, bodyID, this.url);
            req.send ();
            return (byte[]) req.getReturnedObject();
        
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#publicKeyExchange(long, org.objectweb.proactive.core.body.UniversalBody, byte[], byte[], byte[])
     */
    public byte[][] publicKeyExchange(long sessionID,
        UniversalBody distantBody, byte[] my_pub, byte[] my_cert,
        byte[] sig_code) throws SecurityNotAvailableException, Exception {
    
        ArrayList paramsList = new ArrayList();
        paramsList.add(new Long(sessionID));
        paramsList.add(distantBody);
        paramsList.add(my_pub);
        paramsList.add(my_cert);
        paramsList.add(sig_code);

        BodyRequest req = new BodyRequest("publicKeyExchange",
                paramsList, bodyID,this.url);
        req.send ();
        
        return (byte[][]) req.getReturnedObject();
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#secretKeyExchange(long, byte[], byte[], byte[], byte[], byte[])
     */
    public byte[][] secretKeyExchange(long sessionID, byte[] tmp, byte[] tmp1,
        byte[] tmp2, byte[] tmp3, byte[] tmp4)
        throws SecurityNotAvailableException, Exception {
        
        ArrayList paramsList = new ArrayList();
        paramsList.add(new Long(sessionID));
        paramsList.add(tmp);
        paramsList.add(tmp1);
        paramsList.add(tmp2);
        paramsList.add(tmp3);
        paramsList.add(tmp4);

        BodyRequest req = new BodyRequest("secretKeyExchange",
                paramsList, bodyID, this.url);
        req.send ();        
        return (byte[][]) req.getReturnedObject();
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getPolicyTo(java.lang.String, java.lang.String, java.lang.String)
     */
    public Communication getPolicyTo(String type, String from, String to)
        throws SecurityNotAvailableException, IOException {
        
            ArrayList paramsList = new ArrayList();
            paramsList.add(type);
            paramsList.add(from);
            paramsList.add(to);

            BodyRequest req = new BodyRequest("getPolicyTo",
                    paramsList, bodyID, this.url);
            req.send ();
           
                try {
                    return (Communication)req.getReturnedObject();
                } catch (SecurityNotAvailableException e) {
                    throw e;
                }catch (Exception e) {
                    throw new HTTPUnexpectedException("Unexpected exception", e);
                }
           
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getPolicy(org.objectweb.proactive.ext.security.SecurityContext)
     */
    public SecurityContext getPolicy(SecurityContext securityContext)
        throws SecurityNotAvailableException, IOException {

            ArrayList paramsList = new ArrayList();
            paramsList.add(securityContext);
            
            BodyRequest req = new BodyRequest("getPolicy",
                    paramsList, bodyID, this.url);
            req.send ();
        
                try {
                    return (SecurityContext) req.getReturnedObject();
                } catch (SecurityNotAvailableException e) {
                    throw e;
                }catch (Exception e) {
                    throw new HTTPUnexpectedException("Unexpected exception", e);
                }
         
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getVNName()
     */
    public String getVNName() throws SecurityNotAvailableException, IOException {
        
        	BodyRequest req = new BodyRequest("getVNName",
                    new ArrayList(), bodyID, this.url);
        	req.send ();
           
                try {
                    return (String) req.getReturnedObject();
                } catch (SecurityNotAvailableException e) {
                    throw e;
                }catch (Exception e) {
                    throw new HTTPUnexpectedException("Unexpected exception", e);
                }
           
            }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getCertificateEncoded()
     */
    public byte[] getCertificateEncoded()
        throws SecurityNotAvailableException, IOException {
        
        BodyRequest req = new BodyRequest(
                "getCertificateEncoded", new ArrayList(), bodyID, this.url);
        req.send ();
          
                try {
                    return (byte[]) req.getReturnedObject();
                } catch (SecurityNotAvailableException e) {
                    throw e;
                }catch (Exception e) {
                    throw new HTTPUnexpectedException("Unexpected exception", e);
                }
            
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getEntities()
     */
    public ArrayList getEntities()
        throws SecurityNotAvailableException, IOException {
        BodyRequest req = new BodyRequest("getEntities",
                new ArrayList(), bodyID, this.url);
        req.send ();
     
                try {
                    return (ArrayList) req.getReturnedObject();
                } catch (SecurityNotAvailableException e) {
                    throw e;
                }catch (Exception e) {
                    throw new HTTPUnexpectedException("Unexpected exception", e);
                }
           
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getProActiveSecurityManager()
     */
    public ProActiveSecurityManager getProActiveSecurityManager()
        throws SecurityNotAvailableException, IOException {
        	BodyRequest req = new BodyRequest(
                    "getProActiveSecurityManager", new ArrayList(), bodyID, this.url);
        	req.send ();
          
                try {
                    return (ProActiveSecurityManager) req.getReturnedObject();
                } catch (SecurityNotAvailableException e) {
                    throw e;
                }catch (Exception e) {
                    throw new HTTPUnexpectedException("Unexpected exception", e);
                }
       
    }

//    /**
//     * @see org.objectweb.proactive.core.exceptions.Handlerizable#getHandlersLevel()
//     */
//    public HashMap getHandlersLevel() throws java.io.IOException {
//        
//        BodyRequest req = new BodyRequest("getHandlersLevel",
//                new ArrayList(), bodyID, this.url);
//        req.send ();    
//       
//        
//                try {
//                    return (HashMap) req.getReturnedObject();
//                } catch (Exception e) {
//                    throw new HTTPUnexpectedException("Unexpected exception", e);
//                }
//           
//    }
//
// 
//    /** Set a new handler within the table of the Handlerizable Object
//     * @param handler A handler associated with a class of non functional exception.
//     * @param exception A class of non functional exception. It is a subclass of <code>NonFunctionalException</code>.
//     */
//    public void setExceptionHandler(Class handler, Class exception)
//        throws IOException {
//
//            ArrayList paramsList = new ArrayList();
//            paramsList.add(handler);
//            paramsList.add(exception);
//            new BodyRequest("setExceptionHandler", paramsList,
//                    bodyID, this.url
//                    ).send ();
//            
//            
//            
//    }
//
//    /**
//     * @see org.objectweb.proactive.core.exceptions.Handlerizable#unsetExceptionHandler(java.lang.Class)
//     */
//    public Handler unsetExceptionHandler(Class exception)
//        throws IOException {
//      
//            ArrayList paramsList = new ArrayList();
//            paramsList.add(exception);
//            
//            BodyRequest req = new BodyRequest(
//                    "unsetExceptionHandler", paramsList, bodyID, this.url);
//            req.send ();
//            
//            
//            try {
//                return (Handler) req.getReturnedObject();
//            } catch (Exception e) {               
//                e.printStackTrace();
//            }
//            return null;
//    }
//
//    /**
//     * @see org.objectweb.proactive.core.exceptions.Handlerizable#setExceptionHandler(org.objectweb.proactive.core.exceptions.handler.Handler, java.lang.Class)
//     */
//    public void setExceptionHandler(Handler handler, Class exception)
//        throws IOException {
//        
//            ArrayList paramsList = new ArrayList();
//            paramsList.add(handler);
//            paramsList.add(exception);
//
//             new BodyRequest("setExceptionHandler", paramsList,
//                    bodyID, this.url).send ();	
//                    
//    }

    /**
     * @see org.objectweb.proactive.Job#getJobID()
     */
    public String getJobID() {
        return this.jobID;
    }

//    /**
//     * Clear the local map of handlers
//     * @see org.objectweb.proactive.core.exceptions.Handlerizable#clearHandlersLevel()
//     */
//    public void clearHandlersLevel() throws java.io.IOException {
//        
//            new BodyRequest("clearHandlersLevel", new ArrayList(),
//                    bodyID, this.url).send ();
//        
//    }
//
//    /**
//     * Get information about the handlerizable object
//     * @see org.objectweb.proactive.core.exceptions.Handlerizable#getHandlerizableInfo()
//     */
//    public String getHandlerizableInfo() throws java.io.IOException {
//        BodyRequest req = new BodyRequest(
//                "getHandlerizableInfo", new ArrayList(), bodyID, this.url);
//        req.send ();    
//        try {
//            return (String) req.getReturnedObject();
//        } catch (Exception e) {
//            throw new HTTPUnexpectedException("Unexpected exception", e);
//        }
//    
//    }

    /*
       public boolean equals(Object o) {
               if (!(o instanceof HttpRemoteBodyImpl)) {
                       return false;
               }
               HttpRemoteBodyImpl rba = (HttpRemoteBodyImpl) o;
               return (remoteBodyAdapter.url.equals(rba.getURL()) && remoteBodyAdapter.bodyID.equals(rba.getBodyID())) &&
                   (remoteBodyAdapter.port == rba.getPort());
       }
    
    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getRemoteAdapter()
     */
    public UniversalBody getRemoteAdapter() {        
        return this;
    }

    /**
     * STILL NOT IMPLEMENTED
     * @see org.objectweb.proactive.core.body.UniversalBody#receiveFTEvent(org.objectweb.proactive.core.body.ft.events.FTEvent)
     */
    public int receiveFTMessage(FTMessage ev) throws IOException {
        return FTManager.NON_FT;
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#createShortcut(org.objectweb.proactive.core.component.request.Shortcut)
     */
    public void createShortcut(Shortcut shortcut) throws IOException {
        if (logger.isDebugEnabled()) { 
            logger.debug("shortcuts are currently not implemented for http communications");
        }
    }
    
//  NFEProducer implementation
    public void addNFEListener(NFEListener listener) {
    	ArrayList paramList = new ArrayList();
    	paramList.add(listener);
    	try {
    		new BodyRequest("addNFEListener", paramList, bodyID, url).send();
		} catch (HTTPRemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    public void removeNFEListener(NFEListener listener) {
    	ArrayList paramList = new ArrayList();
    	paramList.add(listener);
    	try {
			new BodyRequest("removeNFEListener", paramList, bodyID, url).send();
		} catch (HTTPRemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    public int fireNFE(NonFunctionalException e) {
    	ArrayList paramList = new ArrayList();
    	paramList.add(e);
    	try {
			BodyRequest br = new BodyRequest("fireNFE", paramList, bodyID, url);
			br.send();
			return ((Integer) br.getReturnedObject()).intValue();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return 0;
		}
    }
}
