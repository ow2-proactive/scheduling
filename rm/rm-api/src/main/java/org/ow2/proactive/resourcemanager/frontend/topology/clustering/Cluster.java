/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.frontend.topology.clustering;

import java.util.LinkedList;
import java.util.List;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Class represents the cluster of elements grouped by their proximity.
 * One element can belong only to one cluster, so it is one-to-many relationship
 * (one cluster - many nodes).
 *
 */
@PublicAPI
public class Cluster<Element> {

    private String id = "";
    private int hashCode = 0;
    private LinkedList<Element> elements = new LinkedList<Element>();

    public Cluster(String id, Element element) {
        elements.add(element);
        this.id = id;
        hashCode = id.hashCode();
    }

    public void add(List<Element> elements) {
        this.elements.addAll(elements);
    }

    public void remove(List<Element> elements) {
        this.elements.removeAll(elements);
    }

    public void removeLast(int number) {
        // removeLast will throw NoSuchElementException if number is bigger than list size
        for (int i = 0; i < number; i++) {
            this.elements.removeLast();
        }
    }

    public List<Element> getElements() {
        return elements;
    }

    public int size() {
        return elements.size();
    }

    public String toString() {
        return "Cluster \"" + id + "\"";
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Cluster<?>))
            return false;

        return this.id.equals(((Cluster<?>) obj).id);
    }

    public int hashCode() {
        return hashCode;
    }
}
