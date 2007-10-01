package org.objectweb.proactive.core.body;

import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.ft.internalmsg.FTMessage;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.component.request.Shortcut;
import org.objectweb.proactive.core.gc.GCMessage;
import org.objectweb.proactive.core.gc.GCResponse;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.remoteobject.adapter.Adapter;
import org.objectweb.proactive.core.remoteobject.exception.UnknownProtocolException;
import org.objectweb.proactive.core.security.Communication;
import org.objectweb.proactive.core.security.SecurityContext;
import org.objectweb.proactive.core.security.crypto.KeyExchangeException;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.core.security.exceptions.SecurityNotAvailableException;
import org.objectweb.proactive.core.security.securityentity.Entity;


public class UniversalBodyRemoteObjectAdapter extends Adapter<UniversalBody>
    implements UniversalBody {

    /**
     * Cache the ID of the Body locally for speed
     */
    protected UniqueID bodyID;

    /**
     * Cache the jobID locally for speed
     */
    protected String jobID;

    public UniversalBodyRemoteObjectAdapter() {
    }

    public UniversalBodyRemoteObjectAdapter(UniversalBody u) {
        super(u);
        if (bodyLogger.isDebugEnabled()) {
            bodyLogger.debug(target.getClass());
        }
    }

    @Override
    protected void construct() {
        this.bodyID = target.getID();
        this.jobID = target.getJobID();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof UniversalBodyRemoteObjectAdapter) {
            return ((StubObject) this.target).getProxy()
                    .equals(((StubObject) ((UniversalBodyRemoteObjectAdapter) o).target).getProxy());
        }

        return false;
    }

    /**
      * @see org.objectweb.proactive.core.body.UniversalBody#getID()
      */
    public UniqueID getID() {
        return bodyID;
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#createShortcut(org.objectweb.proactive.core.component.request.Shortcut)
     */
    public void createShortcut(Shortcut shortcut) throws IOException {
        //      TODO implement
        throw new ProActiveRuntimeException(
            "create shortcut method not implemented yet");
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getRemoteAdapter()
     */
    public UniversalBody getRemoteAdapter() {
        return this;
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getReifiedClassName()
     */
    public String getReifiedClassName() {
        return this.target.getReifiedClassName();
    }

    @Override
    public int hashCode() {
        return target.hashCode();
    }

    /**
     * @see org.objectweb.proactive.Job#getJobID()
     */
    public String getJobID() {
        return jobID;
    }

    public void disableAC() throws IOException {
        target.disableAC();
    }

    public void enableAC() throws IOException {
        target.enableAC();
    }

    public String getNodeURL() {
        return target.getNodeURL();
    }

    public Object receiveFTMessage(FTMessage ev) throws IOException {
        return target.receiveFTMessage(ev);
    }

    public GCResponse receiveGCMessage(GCMessage toSend)
        throws IOException {
        return target.receiveGCMessage(toSend);
    }

    public int receiveReply(Reply r) throws IOException {
        return target.receiveReply(r);
    }

    public int receiveRequest(Request request)
        throws IOException, RenegotiateSessionException {
        return target.receiveRequest(request);
    }

    public void register(String url)
        throws IOException, UnknownProtocolException {
        target.register(url);
    }

    public void setRegistered(boolean registered) throws IOException {
        target.setRegistered(registered);
    }

    public void updateLocation(UniqueID id, UniversalBody body)
        throws IOException {
        target.updateLocation(id, body);
    }

    public X509Certificate getCertificate()
        throws SecurityNotAvailableException, IOException {
        return target.getCertificate();
    }

    public byte[] getCertificateEncoded()
        throws SecurityNotAvailableException, IOException {
        return target.getCertificateEncoded();
    }

    public ArrayList<Entity> getEntities()
        throws SecurityNotAvailableException, IOException {
        return target.getEntities();
    }

    public SecurityContext getPolicy(SecurityContext securityContext)
        throws SecurityNotAvailableException, IOException {
        return target.getPolicy(securityContext);
    }

    public PublicKey getPublicKey()
        throws SecurityNotAvailableException, IOException {
        return target.getPublicKey();
    }

    public byte[][] publicKeyExchange(long sessionID, byte[] myPublicKey,
        byte[] myCertificate, byte[] signature)
        throws SecurityNotAvailableException, RenegotiateSessionException,
            KeyExchangeException, IOException {
        return target.publicKeyExchange(sessionID, myPublicKey, myCertificate,
            signature);
    }

    public byte[] randomValue(long sessionID, byte[] clientRandomValue)
        throws SecurityNotAvailableException, RenegotiateSessionException,
            IOException {
        return target.randomValue(sessionID, clientRandomValue);
    }

    public byte[][] secretKeyExchange(long sessionID, byte[] encodedAESKey,
        byte[] encodedIVParameters, byte[] encodedClientMacKey,
        byte[] encodedLockData, byte[] parametersSignature)
        throws SecurityNotAvailableException, RenegotiateSessionException,
            IOException {
        return target.secretKeyExchange(sessionID, encodedAESKey,
            encodedIVParameters, encodedClientMacKey, encodedLockData,
            parametersSignature);
    }

    public long startNewSession(Communication policy)
        throws SecurityNotAvailableException, RenegotiateSessionException,
            IOException {
        return target.startNewSession(policy);
    }

    public void terminateSession(long sessionID)
        throws SecurityNotAvailableException, IOException {
        target.terminateSession(sessionID);
    }
}
