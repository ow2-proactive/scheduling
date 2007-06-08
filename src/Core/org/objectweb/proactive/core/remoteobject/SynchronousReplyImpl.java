package org.objectweb.proactive.core.remoteobject;

import java.io.IOException;
import java.io.Serializable;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.ft.message.MessageInfo;
import org.objectweb.proactive.core.body.ft.protocols.FTManager;
import org.objectweb.proactive.core.body.future.FutureResult;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.security.ProActiveSecurityManager;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;


public class SynchronousReplyImpl implements Reply, Serializable {
    protected Object reply;

    public SynchronousReplyImpl() {
    }

    public SynchronousReplyImpl(Object reply) {
        this.reply = reply;
    }

    public boolean decrypt(ProActiveSecurityManager psm)
        throws RenegotiateSessionException {
        // TODO Auto-generated method stub
        return false;
    }

    public Object getSynchResult() {
        return reply;
    }

    public FutureResult getResult() {
        return null;
    }

    public long getSessionId() {
        // TODO Auto-generated method stub
        return 0;
    }

    public boolean isAutomaticContinuation() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isCiphered() {
        // TODO Auto-generated method stub
        return false;
    }

    public int send(UniversalBody destinationBody) throws IOException {
        return 0;
    }

    public FTManager getFTManager() {
        // TODO Auto-generated method stub
        return null;
    }

    public MessageInfo getMessageInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getMethodName() {
        // TODO Auto-generated method stub
        return null;
    }

    public long getSequenceNumber() {
        // TODO Auto-generated method stub
        return 0;
    }

    public UniqueID getSourceBodyID() {
        // TODO Auto-generated method stub
        return null;
    }

    public long getTimeStamp() {
        // TODO Auto-generated method stub
        return 0;
    }

    public boolean ignoreIt() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isOneWay() {
        // TODO Auto-generated method stub
        return false;
    }

    public void setFTManager(FTManager ft) {
        // TODO Auto-generated method stub
    }

    public void setIgnoreIt(boolean ignore) {
        // TODO Auto-generated method stub
    }

    public void setMessageInfo(MessageInfo mi) {
        // TODO Auto-generated method stub
    }
}
