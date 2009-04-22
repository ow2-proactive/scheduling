/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.authentication;

/**
 * GroupHierarchy...
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9.1
 */
public class GroupHierarchy {
    /** hierarchy */
    private String[] hierarchy;

    /**
     * Create a new instance of GroupHierarchy
     * 
     * @param hierarchy a string array that represents the hierarchy
     */
    public GroupHierarchy(String[] hierarchy) {
        this.hierarchy = hierarchy;
    }

    /**
     * Is the given trueGroup above the given reqGroup
     * 
     * @param trueGroup real group
     * @param reqGroup required group
     * @return true if the given trueGroup above the given reqGroup
     * @throws GroupException
     */
    public boolean isAbove(String trueGroup, String reqGroup) throws GroupException {
        int trueGroupLevel = groupLevel(trueGroup);

        if (trueGroupLevel == -1) {
            throw new GroupException("group asked " + trueGroup + " is not in groups hierarchy");
        }

        int reqGroupLevel = groupLevel(reqGroup);

        if (reqGroupLevel == -1) {
            throw new GroupException("Required group " + reqGroup + " is not in groups hierarchy");
        }

        return trueGroupLevel >= reqGroupLevel;
    }

    /**
     * Return the group level of the given group
     * 
     * @param group the group name
     * @return the group level of the given group
     */
    private int groupLevel(String group) {
        for (int i = hierarchy.length - 1; i > -1; i--) {
            if (hierarchy[i].equals(group)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Is the group in hierarchy
     * 
     * @param group the group name
     * @return true if the given group is in hierarchy
     */
    public boolean isGroupInHierarchy(String group) {
        if (groupLevel(group) != -1) {
            return true;
        } else {
            return false;
        }
    }
}
