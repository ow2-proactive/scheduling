/*
 * Created by IntelliJ IDEA.
 * User: fhuet
 * Date: Apr 17, 2002
 * Time: 7:09:56 PM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package org.objectweb.proactive.ext.mixedlocation;

import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.ext.security.Communication;
import org.objectweb.proactive.ext.security.CommunicationForbiddenException;
import org.objectweb.proactive.ext.security.Policy;
import org.objectweb.proactive.ext.security.ProActiveSecurityManager;
import org.objectweb.proactive.ext.security.RenegotiateSessionException;
import org.objectweb.proactive.ext.security.SecurityContext;
import org.objectweb.proactive.ext.security.SecurityNotAvailableException;
import org.objectweb.proactive.ext.security.crypto.AuthenticationException;
import org.objectweb.proactive.ext.security.crypto.ConfidentialityTicket;
import org.objectweb.proactive.ext.security.crypto.KeyExchangeException;


public class UniversalBodyWrapper implements UniversalBody, Runnable {
    protected UniversalBody wrappedBody;
    protected long time;
    protected UniqueID id;
    protected boolean stop;
    protected long creationTime;

    //protected  Thread t ;

    /**
     * Create a time-limited wrapper around a UniversalBody
     * @param body the wrapped UniversalBody
     * @param time the life expectancy of this wrapper in milliseconds
     */
    public UniversalBodyWrapper(UniversalBody body, long time) {
        this.wrappedBody = body;
        this.time = time;
        this.creationTime = System.currentTimeMillis();
        //     t =new Thread(this);
        this.id = this.wrappedBody.getID();

        //   t.start();
    }

    public void receiveRequest(Request request) throws IOException, RenegotiateSessionException {
        //       System.out.println("UniversalBodyWrapper.receiveRequest");
        if (this.wrappedBody == null) {
            throw new IOException();
        }

        //the forwarder should be dead
        if (System.currentTimeMillis() > (this.creationTime + this.time)) {
            //   this.updateServer();
            //   this.wrappedBody = null;
            //	t.start();
            //   System.gc();
            throw new IOException();
        } else {
            try {
                this.wrappedBody.receiveRequest(request);
            } catch (IOException e) {
                e.printStackTrace();
                throw e;
            }
        }

        //      this.stop();
    }

    public void receiveReply(Reply r) throws IOException {
        this.wrappedBody.receiveReply(r);
    }

    public String getNodeURL() {
        return this.wrappedBody.getNodeURL();
    }

    public UniqueID getID() {
        return this.id;
    }

    public void updateLocation(UniqueID id, UniversalBody body)
        throws IOException {
        this.wrappedBody.updateLocation(id, body);
    }

    public UniversalBody getRemoteAdapter() {
        return this.wrappedBody.getRemoteAdapter();
    }

    public void enableAC() throws java.io.IOException {
        this.wrappedBody.enableAC();
    }

    public void disableAC() throws java.io.IOException {
        this.wrappedBody.disableAC();
    }

    public void setImmediateService(String methodName)
        throws java.io.IOException {
        this.wrappedBody.setImmediateService(methodName);
    }

    protected void updateServer() {
        //        System.out.println("UniversalBodyWrapper.updateServer");
        //  LocationServer server = LocationServerFactory.getLocationServer();
        //        try {
        //            server.updateLocation(id, this.wrappedBody);
        //        } catch (Exception e) {
        //            System.out.println("XXXX Error XXXX");
        //           // e.printStackTrace();
        //        }
    }

    //protected synchronized void stop() {
    // this.stop=true;	
    // this.notifyAll();
    //}
    //
    //protected synchronized void waitForStop(long time) {
    //	if (!this.stop) {
    //	 try {
    //		 wait(time);	
    //	} catch (InterruptedException e) {
    //		e.printStackTrace();
    //	}
    //	}
    //	
    //}
    public void run() {
        //        System.out.println("UniversalBodyWrapper.run life expectancy " + time);
        try {
            // Thread.currentThread().sleep(time);
            //  this.waitForStop(time);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //        System.out.println("UniversalBodyWrapper.run end of life...");
        this.updateServer();
        this.wrappedBody = null;
        System.gc();
    }
    
    // SECURITY
	public void initiateSession(int type,UniversalBody body)
		throws java.io.IOException, CommunicationForbiddenException, 
			AuthenticationException, RenegotiateSessionException, 
			SecurityNotAvailableException {
		wrappedBody.initiateSession(type,body);
	}

	public void terminateSession(long sessionID)
		throws java.io.IOException, SecurityNotAvailableException {
		wrappedBody.terminateSession(sessionID);
	}

	public X509Certificate getCertificate()
		throws java.io.IOException, SecurityNotAvailableException {
		return wrappedBody.getCertificate();
	}

	public ProActiveSecurityManager getProActiveSecurityManager()
		throws java.io.IOException, SecurityNotAvailableException {
		return wrappedBody.getProActiveSecurityManager();
	}

	public Policy getPolicyFrom(X509Certificate certificate)
		throws java.io.IOException, SecurityNotAvailableException {
		return wrappedBody.getPolicyFrom(certificate);
	}

	public long startNewSession(Communication policy)
		throws java.io.IOException, RenegotiateSessionException, 
			SecurityNotAvailableException {
		return wrappedBody.startNewSession(policy);
	}

	public ConfidentialityTicket negociateKeyReceiverSide(
		ConfidentialityTicket confidentialityTicket, long sessionID)
		throws java.io.IOException, KeyExchangeException, 
			SecurityNotAvailableException {
		return wrappedBody.negociateKeyReceiverSide(confidentialityTicket,
			sessionID);
	}

	public PublicKey getPublicKey()
		throws java.io.IOException, SecurityNotAvailableException {
		return wrappedBody.getPublicKey();
	}

	public byte[] randomValue(long sessionID, byte[] cl_rand)
		throws Exception, SecurityNotAvailableException {
		return wrappedBody.randomValue(sessionID, cl_rand);
	}

	public byte[][] publicKeyExchange(long sessionID,
		UniversalBody distantBody, byte[] my_pub, byte[] my_cert,
		byte[] sig_code) throws Exception, SecurityNotAvailableException {
		return wrappedBody.publicKeyExchange(sessionID, distantBody, my_pub,
			my_cert, sig_code);
	}

	public byte[][] secretKeyExchange(long sessionID, byte[] tmp, byte[] tmp1,
		byte[] tmp2, byte[] tmp3, byte[] tmp4)
		throws Exception, SecurityNotAvailableException {
		return wrappedBody.secretKeyExchange(sessionID, tmp, tmp1, tmp2, tmp3,
			tmp4);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.proactive.core.body.UniversalBody#getVNName()
	 */
	public String getVNName() throws IOException, SecurityNotAvailableException {
		return wrappedBody.getVNName();
	}

	/* (non-Javadoc)
	 * @see org.objectweb.proactive.core.body.UniversalBody#getCertificateEncoded()
	 */
	public byte[] getCertificateEncoded()
		throws IOException, SecurityNotAvailableException {
		return wrappedBody.getCertificateEncoded();
	}

	/* (non-Javadoc)
	 * @see org.objectweb.proactive.core.body.UniversalBody#getPolicy(org.objectweb.proactive.ext.security.SecurityContext)
	 */
	public SecurityContext getPolicy(SecurityContext securityContext)
		throws SecurityNotAvailableException, IOException {
		return wrappedBody.getPolicy(securityContext);
	}
    
	public ArrayList getEntities() throws SecurityNotAvailableException, IOException {
			return wrappedBody.getEntities();
	}

	public Communication getPolicyTo(String vn, String from, String to)
		throws java.io.IOException, SecurityNotAvailableException {
		return this.wrappedBody.getPolicyTo(vn, from, to);
	}

	

}
