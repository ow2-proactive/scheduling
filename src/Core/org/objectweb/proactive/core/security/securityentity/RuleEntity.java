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

import java.io.Serializable;

import org.objectweb.proactive.core.security.SecurityConstants.EntityType;


public abstract class RuleEntity implements Serializable {
    public enum Match {
        OK, DEFAULT, FAILED;
    }

    public static final int UNDEFINED_LEVEL = 0;

    /**
     * Level of the entity, equals the depth of its certificate in the
     * certificate tree (UNDEFINED_LEVEL is the root, above the self signed
     * certificates)
     */
    protected final int level;
    protected final EntityType type;

    protected RuleEntity(EntityType type, int level) {
        this.type = type;
        this.level = level + levelIncrement();
    }

    protected int getLevel() {
        return this.level;
    }

    public EntityType getType() {
        return this.type;
    }

    protected Match match(Entities e) {
        for (Entity entity : e) {
            if (match(entity) == Match.FAILED) {
                return Match.FAILED;
            }
        }
        return Match.OK;
    }

    // returns the number of levels of the entity above the application
    // level
    private int levelIncrement() {
        switch (this.type) {
            case RUNTIME:
            case ENTITY:
                return 1;
            case NODE:
                return 2;
            case OBJECT:
                return 3;
            default:
                return 0;
        }
    }

    abstract protected Match match(Entity e);

    abstract public String getName();

    @Override
    public String toString() {
        return "RuleEntity :\n\tLevel : " + this.level;
    }
}
