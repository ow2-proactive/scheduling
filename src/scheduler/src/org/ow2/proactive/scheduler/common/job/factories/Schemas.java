/*
 *  *
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
 *  * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.job.factories;

import java.util.HashMap;
import java.util.Map;

public enum Schemas {
    SCHEMA_3_1("/org/ow2/proactive/scheduler/common/xml/schemas/jobdescriptor/3.1/schedulerjob.rng",
            "urn:proactive:jobdescriptor:3.1"),
    SCHEMA_3_2("/org/ow2/proactive/scheduler/common/xml/schemas/jobdescriptor/3.2/schedulerjob.rng",
            "urn:proactive:jobdescriptor:3.2"),
    SCHEMA_DEV("/org/ow2/proactive/scheduler/common/xml/schemas/jobdescriptor/dev/schedulerjob.rng",
            "urn:proactive:jobdescriptor:dev"),
    // should the last one declared, see #validate
    SCHEMA_LATEST(SCHEMA_3_2.location, SCHEMA_3_2.namespace);

    String location;
    String namespace;

    Schemas(String location, String namespace) {
        this.location = location;
        this.namespace = namespace;
    }

    static Map<String, Schemas> SCHEMAS_BY_NAMESPACE = new HashMap<String, Schemas>();
    static {
        for (Schemas schema : Schemas.values()) {
            SCHEMAS_BY_NAMESPACE.put(schema.namespace, schema);
        }
    }
}
