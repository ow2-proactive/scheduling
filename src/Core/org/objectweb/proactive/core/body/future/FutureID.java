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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + (int) (ID ^ (ID >>> 32));
        result = (prime * result) +
            ((creatorID == null) ? 0 : creatorID.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FutureID other = (FutureID) obj;
        if (ID != other.ID) {
            return false;
        }
        if (creatorID == null) {
            if (other.creatorID != null) {
                return false;
            }
        } else if (!creatorID.equals(other.creatorID)) {
            return false;
        }
        return true;
    }
}
