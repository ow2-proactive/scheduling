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
package org.objectweb.proactive.core.body.jini;

import java.io.IOException;
import java.rmi.RemoteException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.ft.internalmsg.FTMessage;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.rmi.RandomPortSocketFactory;
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
 *   An adapter for a LocalBody to be able to receive jini calls. This helps isolate JINI-specific
 *   code into a small set of specific classes, thus enabling reuse if we one day decide to switch
 *   to anothe jini objects library.
 */
public class JiniBodyImpl extends java.rmi.server.UnicastRemoteObject
    implements JiniBody, java.rmi.server.Unreferenced {
    static Logger logger = Logger.getLogger(JiniBodyImpl.class.getName());

    /**
     * A custom socket Factory
     */
    protected static RandomPortSocketFactory factory = new RandomPortSocketFactory(37002,
            5000);

    /**
     * The encapsulated local body
     * transient to deal with custom serialization of requests.
     */
    protected transient UniversalBody body;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public JiniBodyImpl() throws RemoteException {
    }

    public JiniBodyImpl(UniversalBody body) throws RemoteException {
        // super(0, factory, factory);
        this.body = body;
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    //
    // -- implements JiniBody -----------------------------------------------
    //
    public String getJobID() {
    	return body.getJobID();
    }
    
    public int receiveRequest(Request r) throws java.io.IOException, RenegotiateSessionException {
        //System.out.println("JiniBodyImpl: receiveRequest() for " + this.localBody);
        //System.out.println("JiniBodyImpl: receiveRequest() request is " + r.getMethodName());
        return body.receiveRequest(r);
    }
    
    

    /**
     * @see org.objectweb.proactive.core.body.jini.JiniBody#terminate()
     */
    public void terminate() throws IOException {
        body.terminate();

    }
    public int receiveReply(Reply r) throws java.io.IOException {
        return body.receiveReply(r);
    }

    public String getNodeURL() {
        return body.getNodeURL();
    }

    public UniqueID getID() {
        return body.getID();
    }

    public void updateLocation(UniqueID id, UniversalBody jiniBody)
        throws java.io.IOException {
        body.updateLocation(id, jiniBody);
    }

    public void enableAC() throws java.io.IOException {
        body.enableAC();
    }

    public void disableAC() throws java.io.IOException {
        body.disableAC();
    }

    public void setImmediateService(String methodName)
        throws java.io.IOException {
        body.setImmediateService(methodName);
    }

    public void unreferenced() {
        logger.info("JiniBodyImpl: unreferenced()");
       // System.gc();
    }

	// SECURITY
	public void initiateSession(int type,UniversalBody body)
		throws IOException, CommunicationForbiddenException, 
			AuthenticationException, RenegotiateSessionException, 
			SecurityNotAvailableException {
		body.initiateSession(type,body);
	}

	public void terminateSession(long sessionID)
		throws IOException, SecurityNotAvailableException {
		body.terminateSession(sessionID);
	}

	public X509Certificate getCertificate()
		throws SecurityNotAvailableException, IOException {
		return body.getCertificate();
	}

	public ProActiveSecurityManager getProActiveSecurityManager()
		throws SecurityNotAvailableException, IOException {
		return body.getProActiveSecurityManager();
	}

	public Policy getPolicyFrom(X509Certificate certificate)
		throws SecurityNotAvailableException, IOException {
		return body.getPolicyFrom(certificate);
	}

	public long startNewSession(Communication policy)
		throws SecurityNotAvailableException, IOException, 
			RenegotiateSessionException {
		return body.startNewSession(policy);
	}

	public ConfidentialityTicket negociateKeyReceiverSide(
		ConfidentialityTicket confidentialityTicket, long sessionID)
		throws SecurityNotAvailableException, KeyExchangeException, IOException {
		return body.negociateKeyReceiverSide(confidentialityTicket, sessionID);
	}

	public PublicKey getPublicKey()
		throws SecurityNotAvailableException, IOException {
		return body.getPublicKey();
	}

	public byte[] randomValue(long sessionID, byte[] cl_rand)
		throws Exception {
		return body.randomValue(sessionID, cl_rand);
	}

	public byte[][] publicKeyExchange(long sessionID,
		UniversalBody distantBody, byte[] my_pub, byte[] my_cert,
		byte[] sig_code) throws Exception {
		return body.publicKeyExchange(sessionID, distantBody, my_pub, my_cert,
			sig_code);
	}

	public byte[][] secretKeyExchange(long sessionID, byte[] tmp, byte[] tmp1,
		byte[] tmp2, byte[] tmp3, byte[] tmp4) throws Exception {
		return body.secretKeyExchange(sessionID, tmp, tmp1, tmp2, tmp3, tmp4);
	}

	public Communication getPolicyTo(String type, String from, String to)
		throws SecurityNotAvailableException, IOException {
		return body.getPolicyTo(type, from, to);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.proactive.core.body.jini.JiniBody#getVNName()
	 */
	public String getVNName() throws IOException, SecurityNotAvailableException {
		return body.getVNName();
	}

	/* (non-Javadoc)
	 * @see org.objectweb.proactive.core.body.jini.JiniBody#getCertificateEncoded()
	 */
	public byte[] getCertificateEncoded()
		throws IOException, SecurityNotAvailableException {
		return body.getCertificateEncoded();
	}

	public SecurityContext getPolicy(SecurityContext securityContext)
		throws IOException, SecurityNotAvailableException {
		return body.getPolicy(securityContext);
	}
    
	public ArrayList getEntities() throws SecurityNotAvailableException, IOException {
			return body.getEntities();
		}

    public void changeProxiedBody(Body newBody) {
	        this.body = newBody;
	}

    /**
     * @see org.objectweb.proactive.core.body.jini.JiniBody#receiveFTMessage(org.objectweb.proactive.core.body.ft.internalmsg.FTMessage)
     */
    public int receiveFTMessage(FTMessage fte) throws IOException {
        return this.body.receiveFTMessage(fte);
    }
	
	
    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
    //
    // -- SERIALIZATION -----------------------------------------------
    //

    /*
          private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
          long startTime=System.currentTimeMillis();
          out.defaultWriteObject();
          long endTime=System.currentTimeMillis();
          System.out.println(" SERIALIZATION OF JINIBODYIMPL lasted " + (endTime - startTime));

          }


          private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
          in.defaultReadObject();
          }
    */
}
