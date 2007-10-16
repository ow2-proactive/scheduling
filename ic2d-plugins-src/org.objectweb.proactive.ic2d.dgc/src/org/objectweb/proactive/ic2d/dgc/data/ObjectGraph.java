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
package org.objectweb.proactive.ic2d.dgc.data;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.AbstractData;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.ActiveObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.WorldObject;


public class ObjectGraph {
    private static Map<UniqueID, ActiveObject> ActiveObjectByID = new HashMap<UniqueID, ActiveObject>();

    public static void addObject(ActiveObject ao) {
        ActiveObjectByID.put(ao.getUniqueID(), ao);
    }

    private static Collection<AbstractData> getAllChildren(
        Collection<AbstractData> parents) {
        Collection<AbstractData> res = new Vector<AbstractData>();
        for (AbstractData o : parents) {
            res.addAll(o.getMonitoredChildrenAsList());
        }
        return res;
    }

    private static Collection<ActiveObject> bodyIDToActiveObject(
        Collection<UniqueID> ids) {
        Collection<ActiveObject> aos = new Vector<ActiveObject>();
        for (UniqueID id : ids) {
            ActiveObject ao = ActiveObjectByID.get(id);
            if (ao != null) {
                aos.add(ao);
            } else {
                System.out.println("Body " + id + " not found");
            }
        }
        return aos;
    }

    public static Map<ActiveObject, Collection<ActiveObject>> getObjectGraph(
        WorldObject world) {
        Collection<AbstractData> hosts = world.getMonitoredChildrenAsList();
        Collection<AbstractData> runtimes = getAllChildren(hosts);
        Collection<AbstractData> nodes = getAllChildren(runtimes);

        Map<ActiveObject, Collection<ActiveObject>> res = new HashMap<ActiveObject, Collection<ActiveObject>>();
        for (AbstractData o : nodes) {
            Collection<AbstractData> aos = o.getMonitoredChildrenAsList();
            for (AbstractData oo : aos) {
                ActiveObject ao = (ActiveObject) oo;
                UniqueID bodyID = ((ActiveObject) oo).getUniqueID();
                Collection<UniqueID> referencesID = null;
                try {
                    referencesID = (Collection<UniqueID>) ao.getAttribute(
                            "ReferenceList");
                    Collection<ActiveObject> referencesAO = bodyIDToActiveObject(referencesID);
                    res.put(ao, referencesAO);
                } catch (InstanceNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (MBeanException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (ReflectionException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (AttributeNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return res;
    }
}
