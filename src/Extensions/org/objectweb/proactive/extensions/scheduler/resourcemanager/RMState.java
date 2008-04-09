/**
 * 
 */
package org.objectweb.proactive.extensions.scheduler.resourcemanager;

import java.io.Serializable;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;


/**
 * RMState represents informations about RM activity.
 *
 * @author The ProActive Team
 * @date 12 mars 08
 * @version 3.9
 *
 */
public class RMState implements Serializable {

    private IntWrapper numberOfAllResources;
    private IntWrapper numberOfFreeResources;

    /**
     * Get the number of all resources.
     * 
     * @return the number of all resources.
     */
    public IntWrapper getNumberOfAllResources() {
        return numberOfAllResources;
    }

    /**
     * Get the number of free resources.
     * 
     * @return the number of free resources.
     */
    public IntWrapper getNumberOfFreeResources() {
        return numberOfFreeResources;
    }

    /**
     * Return true if the scheduler has free resources, false if not.
     * 
     * @return true if the scheduler has free resources, false if not.
     */
    public BooleanWrapper hasFreeResources() {
        return new BooleanWrapper(numberOfFreeResources.intValue() != 0);
    }

    /**
     * Sets the number Of All Resources to the given numberOfAllResources value.
     *
     * @param numberOfAllResources the number Of All Resources to set.
     */
    public void setNumberOfAllResources(IntWrapper numberOfAllResources) {
        this.numberOfAllResources = numberOfAllResources;
    }

    /**
     * Sets the number Of Free Resources to the given numberOfFreeResources value.
     *
     * @param numberOfFreeResources the number Of Free Resources to set.
     */
    public void setNumberOfFreeResources(IntWrapper numberOfFreeResources) {
        this.numberOfFreeResources = numberOfFreeResources;
    }

}
