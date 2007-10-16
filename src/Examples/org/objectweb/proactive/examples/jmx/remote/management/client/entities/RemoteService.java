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

import org.objectweb.proactive.core.jmx.ProActiveConnection;


public class RemoteService extends ManageableEntity implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 6776043593851011896L;

    public RemoteService() {
    }

    @Override
    public Object[] getChildren() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ManageableEntity getParent() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean hasChildren() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void addEntity(ManageableEntity entity) {
        // TODO Auto-generated method stub
    }

    @Override
    public void remove() {
        // TODO Auto-generated method stub
    }

    @Override
    public void addName(String name) {
        // TODO Auto-generated method stub
    }

    @Override
    public void removeEntity(ManageableEntity entity) {
        // TODO Auto-generated method stub
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
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
