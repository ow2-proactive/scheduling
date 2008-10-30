/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
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

    /**
     * Set the generic information as a hash map.
     * 
     * @param genericInformations the generic information to set.
     */
    public void setGenericInformations(HashMap<String, String> genericInformations);
}
