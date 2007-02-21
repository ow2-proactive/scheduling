package org.objectweb.proactive.core.gc;

public class Referencer {

    /**
     * The last activity we gave it in a GC response
     */
    private Activity givenActivity;

    /**
     * Whether the referenced agreed on the last activity it gave us.
     * Note that this last activity can be different than this.givenActivity
     * in which case, it will obviously disagree with the consensus.
     */
    private boolean consensus;

    /**
     * When did we receive the latest GC message from this referencer?
     */
    private long lastMessageTimestamp;

    Referencer() {
    }

    long getLastMessageTimestamp() {
        return this.lastMessageTimestamp;
    }

    void setLastGCMessage(GCSimpleMessage mesg) {
        this.consensus = mesg.getConsensus();
        this.lastMessageTimestamp = System.currentTimeMillis();
    }

    void setGivenActivity(Activity activity) {
        if (!activity.equals(this.givenActivity)) {
            this.givenActivity = activity;
            this.consensus = false;
        }
    }

    boolean getConsensus(Activity activity) {
        if (activity.equals(this.givenActivity)) {
            return this.consensus;
        }

        return false;
    }
}
