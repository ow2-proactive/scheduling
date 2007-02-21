package org.objectweb.proactive.core.gc;


/**
 * When an AO makes a consensus, it notifies its referencers with this response
 * This is just an optimization
 */
public class GCTerminationResponse extends GCSimpleResponse {
    GCTerminationResponse(Activity lastActivity) {
        super(lastActivity);
    }

    boolean isTerminationResponse() {
        return true;
    }

    public boolean equals(Object o) {
        if (o instanceof GCTerminationResponse) {
            return this.consensusActivity.equals(((GCTerminationResponse) o).consensusActivity);
        }
        return false;
    }
}
