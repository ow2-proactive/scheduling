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

import java.io.IOException;
import java.util.Vector;

import javax.management.ObjectName;

import org.objectweb.proactive.core.jmx.ProActiveConnection;
import org.objectweb.proactive.examples.jmx.remote.management.status.Status;


/**
 * Abstract class representing an entity that can be managed by our mechanism .
 * @author vlegrand
 *
 */
public abstract class ManageableEntity implements Transactionnable {
    //The name of the entities whose parent is this entity
    private Vector<String> names = new Vector<String>();

    /**
     * @return an array containing all the sub-manageable entities contained by this entity
     */
    public abstract Object[] getChildren();

    /**
     * @return the parent entity that contains this entity
     */
    public abstract ManageableEntity getParent();

    /**
     * @return true if this entity contains one or more manageable entities, false if this entity contains no entity
     */
    public abstract boolean hasChildren();

    /**
     * Removes this entity in the model
     */
    public abstract void remove();

    /**
     * adds a sub entity to this one
     * @param entity the entity to be added
     */
    public abstract void addEntity(ManageableEntity entity);

    /**
     * Removes a sub entity
     * @param entity the entity to be removed
     */
    public abstract void removeEntity(ManageableEntity entity);

    /**
     * @return the entity's name
     */
    public abstract String getName();

    /**
     * Removes an entity whose parent is this entity
     * @param entity the entity to be removed
     */
    public void remove(ManageableEntity entity) {
        removeName(entity.getName());
    }

    private void removeName(String name) {
        this.names.removeElement(name);
    }

    /**
     * Changes an entity's name
     * @param entity the entity whose name has to be changed
     * @param newName the entity's new name
     */
    public void changeName(ManageableEntity entity, String newName) {
        this.names.remove(entity.getName());
        this.names.add(newName);
    }

    /**
     * Adds an entity name in the list of entities names
     * @param name the name to be added
     */
    protected void addName(String name) {
        this.names.addElement(name);
    }

    /**
     * @param name the entity name to be tested
     * @return true if
     */
    protected boolean contains(String name) {
        return this.names.contains(name);
    }

    /**
     *
     * @return
     */
    public abstract String getUrl();

    /**
     *
     * @return
     */
    public abstract ObjectName getObjectName();

    /**
     *
     * @return
     */
    public abstract ProActiveConnection getConnection();

    /**
     *
     */
    public void connect() throws IOException {
    }

    /**
     *
     * @param command
     * @return
     */
    public Status executeCommand(String command) {
        return null;
    }

    /**
     *
     *
     */
    public void refresh() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.examples.jmx.remote.management.client.entities.Transactionnable#cancelTransaction()
     */
    public Status cancelTransaction() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.examples.jmx.remote.management.client.entities.Transactionnable#commitTransaction()
     */
    public Status commitTransaction() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.examples.jmx.remote.management.client.entities.Transactionnable#openTransaction()
     */
    public Status openTransaction() {
        return null;
    }
}
