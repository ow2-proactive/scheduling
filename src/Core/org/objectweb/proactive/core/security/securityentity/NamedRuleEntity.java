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

import java.security.KeyStore;
import java.security.KeyStoreException;

import org.objectweb.proactive.core.security.KeyStoreTools;
import org.objectweb.proactive.core.security.SecurityConstants.EntityType;


public class NamedRuleEntity extends RuleEntity {

    /**
     *
     */
    private final String name;

    public NamedRuleEntity(EntityType type, KeyStore keystore, String name)
        throws KeyStoreException {
        super(type, KeyStoreTools.getApplicationLevel(keystore));
        this.name = name;
    }

    @Override
    protected Match match(Entities e) {
        for (Entity entity : e) {
            if (match(entity) == Match.OK) {
                return Match.OK;
            }
        }
        return Match.FAILED;
    }

    @Override
    protected Match match(Entity e) {
        if ((e.getType() == this.getType()) && e.getName().equals(this.name)) {
            return Match.OK;
        }

        return Match.FAILED;
    }

    @Override
    public String toString() {
        return super.toString() + "\n\tName : " + this.name;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
