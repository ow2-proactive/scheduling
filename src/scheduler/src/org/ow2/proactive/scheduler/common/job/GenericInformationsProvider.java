/**
 * 
 */
package org.ow2.proactive.scheduler.common.job;

import java.util.HashMap;


/**
 * GenericInformationsProvider define the methods to use to define the generic informations
 * in a class.
 *
 * @author The ProActive Team
 * @date 12 mars 08
 *
 */
public interface GenericInformationsProvider {

    /**
     * Returns the genericInformations.<br>
     * These informations are transmitted to the policy that can use it for the scheduling.
     * 
     * @return the genericInformations.
     */
    public HashMap<String, String> getGenericInformations();

    /**
     * Add an information to the generic field informations field.
     * This information will be given to the scheduling policy.
     *
     * @param key the key in which to store the informations.
     * @param genericInformation the information to store.
     */
    public void addGenericInformation(String key, String genericInformation);
}
