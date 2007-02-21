package org.objectweb.proactive.core.gc;

import java.io.Serializable;

import org.objectweb.proactive.core.UniqueID;


/**
 * An Activity is an instance of the Lamport clock. Instances of this class
 * are immutable.
 */
public class Activity implements Serializable {

    /**
     * Who increased the counter?
     */
    private final UniqueID bodyID;

    /**
     * The increasing counter
     */
    private final long activityCounter;

    Activity(UniqueID bodyID, long activityCounter) {
        this.bodyID = bodyID;
        this.activityCounter = activityCounter;
    }

    /**
     * Compare the counter, then the ID
     */
    boolean strictlyMoreRecentThan(Activity a) {
        if (this.activityCounter != a.activityCounter) {
            return this.activityCounter > a.activityCounter;
        }

        return this.bodyID.getCanonString().compareTo(a.bodyID.getCanonString()) > 0;
    }

    public String toString() {
        return bodyID.shortString() + ":" + activityCounter;
    }

    /**
     * Same counter and ID
     */
    public boolean equals(Object o) {
        if (o instanceof Activity) {
            Activity a = (Activity) o;
            return (this.activityCounter == a.activityCounter) &&
            this.bodyID.equals(a.bodyID);
        }
        return false;
    }

    UniqueID getBodyID() {
        return this.bodyID;
    }

    long getCounter() {
        return this.activityCounter;
    }
}
