/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
 * $$ACTIVEEON_INITIAL_DEV$$
 */

package org.ow2.proactive_grid_cloud_portal.studio;

public class Workflow implements Named {

    private long id;
    private String name;
    private String xml;
    private String metadata;

    public Workflow() {
    }

    public Workflow(long id, String name, String xml, String metadata) {
        this(name, xml, metadata);
        this.id = id;
    }

    public Workflow(String name, String xml, String metadata) {
        this.name = name;
        this.xml = xml;
        this.metadata = metadata;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getXml() {
        return xml;
    }

    public String getMetadata() {
        return metadata;
    }

    @Override
    public String toString() {
        return "Workflow{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", xml='" + xml + '\'' +
                ", metadata='" + metadata + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Workflow workflow = (Workflow) o;

        if (id != workflow.id) return false;
        if (metadata != null ? !metadata.equals(workflow.metadata) : workflow.metadata != null) return false;
        if (name != null ? !name.equals(workflow.name) : workflow.name != null) return false;
        if (xml != null ? !xml.equals(workflow.xml) : workflow.xml != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (xml != null ? xml.hashCode() : 0);
        result = 31 * result + (metadata != null ? metadata.hashCode() : 0);
        return result;
    }
}
