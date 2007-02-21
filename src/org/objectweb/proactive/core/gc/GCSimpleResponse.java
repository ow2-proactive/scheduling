package org.objectweb.proactive.core.gc;

import java.io.Serializable;

public class GCSimpleResponse implements Serializable {
    protected final Activity consensusActivity;

    GCSimpleResponse(Activity consensusActivity) {
        this.consensusActivity = consensusActivity;
    }

    public String toString() {
        return "GCREP[" + this.consensusActivity + "]";
    }

    Activity getConsensusActivity() {
        return this.consensusActivity;
    }

    boolean isTerminationResponse() {
        return false;
    }

    public boolean equals(Object o) {
        if (o instanceof GCSimpleResponse) {
            return this.consensusActivity.equals(((GCSimpleResponse) o).consensusActivity);
        }
        return false;
    }
}
