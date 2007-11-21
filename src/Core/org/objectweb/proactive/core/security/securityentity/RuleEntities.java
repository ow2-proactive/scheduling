/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 */
package org.objectweb.proactive.core.security.securityentity;

import java.util.ArrayList;

import org.objectweb.proactive.core.security.securityentity.RuleEntity.Match;


public class RuleEntities extends ArrayList<RuleEntity> {

    /**
     *
     */
    private static final long serialVersionUID = -5629440174390543367L;

    public RuleEntities() {
        super();
    }

    public RuleEntities(RuleEntities entities) {
        super(entities);
    }

    public Match match(Entities entities) {
        if (isEmpty()) {
            return Match.DEFAULT;
        }

        for (RuleEntity entity : this) {
            if (entity.match(entities) == Match.FAILED) {
                return Match.FAILED;
            }
        }
        return Match.OK;
    }

    /**
     * level represents the specificity of the target entities of a rule, higher
     * level is more specific
     *
     * @return the maximum level among the RuleEnties
     */
    public int getLevel() {
        int maxLevel = RuleEntity.UNDEFINED_LEVEL;
        for (RuleEntity rule : this) {
            if (maxLevel < rule.getLevel()) {
                maxLevel = rule.getLevel();
            }
        }
        return maxLevel;
    }

    public boolean contains(Entity entity) {
        for (RuleEntity rule : this) {
            if (rule.match(entity) == Match.OK) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        String result = new String();
        for (RuleEntity rule : this) {
            result += rule.toString();
            result += "\n";
        }
        return result;
    }
}
