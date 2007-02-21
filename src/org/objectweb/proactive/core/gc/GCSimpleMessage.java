package org.objectweb.proactive.core.gc;

import java.io.Serializable;

import org.objectweb.proactive.core.UniqueID;


public class GCSimpleMessage implements Serializable {
    private final transient Referenced referenced;
    private final UniqueID sender;
    private final boolean consensus;
    private final Activity lastActivity;

    GCSimpleMessage(Referenced referenced, UniqueID sender, boolean consensus,
        Activity lastActivity) {
        this.referenced = referenced;
        this.sender = sender;
        this.consensus = consensus;
        this.lastActivity = lastActivity;
    }

    public String toString() {
        String s = sender.shortString();
        return "GCMSG[" + s + "(" + lastActivity + "):" + this.consensus + "]";
    }

    UniqueID getSender() {
        return this.sender;
    }

    boolean getConsensus() {
        return this.consensus;
    }

    Activity getLastActivity() {
        return this.lastActivity;
    }

    Referenced getReferenced() {
        return this.referenced;
    }
}
