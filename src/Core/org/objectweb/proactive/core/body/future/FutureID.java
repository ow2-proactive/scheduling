package org.objectweb.proactive.core.body.future;

import java.io.Serializable;

import org.objectweb.proactive.core.UniqueID;


public class FutureID implements Serializable {

    /**
    * The ID of the "evaluator" of the future.
    */
    private UniqueID creatorID;

    /**
     * ID of the future
     * In fact, the sequence number of the request that generated this future
     */
    private long ID;

    public UniqueID getCreatorID() {
        return creatorID;
    }

    public void setCreatorID(UniqueID creatorID) {
        this.creatorID = creatorID;
    }

    public long getID() {
        return ID;
    }

    public void setID(long id) {
        ID = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FutureID other = (FutureID) obj;
        if (ID != other.ID) {
            return false;
        }
        return creatorID.equals(other.creatorID);
    }
}
