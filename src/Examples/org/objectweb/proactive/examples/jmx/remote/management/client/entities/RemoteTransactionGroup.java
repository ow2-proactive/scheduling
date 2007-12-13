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
package org.objectweb.proactive.examples.jmx.remote.management.client.entities;

import java.io.Serializable;

import javax.management.ObjectName;

import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.jmx.ProActiveConnection;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.examples.jmx.remote.management.events.EntitiesEventManager;


public class RemoteTransactionGroup extends ManageableEntity implements Serializable {

    /**
     *
     */
    private String name;
    private ManageableEntity entities;
    private Group<ManageableEntity> gEntities;

    public RemoteTransactionGroup(String name) {
        this.name = name;
        try {
            this.entities = (ManageableEntity) PAGroup.newGroup(ManageableEntity.class.getName());
            this.gEntities = PAGroup.getGroup(this.entities);
        } catch (ClassNotReifiableException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addEntity(ManageableEntity entity) {
        this.gEntities.add(entity);
        EntitiesEventManager.getInstance().newEvent(this, EntitiesEventManager.ENTITY_ADDED);
    }

    @Override
    public Object[] getChildren() {
        return this.gEntities.toArray();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public ManageableEntity getParent() {
        return null;
    }

    @Override
    public boolean hasChildren() {
        return gEntities.size() > 0;
    }

    @Override
    public void remove() {
        // TODO Auto-generated method stub
    }

    @Override
    public void removeEntity(ManageableEntity entity) {
        this.gEntities.remove(entity);
    }

    @Override
    public String toString() {
        return this.getName();
    }

    @Override
    public ProActiveConnection getConnection() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ObjectName getObjectName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getUrl() {
        // TODO Auto-generated method stub
        return null;
    }
}
