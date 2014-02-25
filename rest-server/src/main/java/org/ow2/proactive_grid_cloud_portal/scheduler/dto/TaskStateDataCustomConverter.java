/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2014 INRIA/University of
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
package org.ow2.proactive_grid_cloud_portal.scheduler.dto;

import java.util.HashMap;
import java.util.Map;

import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.dozer.DozerConverter;
import org.dozer.Mapper;
import org.dozer.MapperAware;
import org.dozer.MappingException;


public class TaskStateDataCustomConverter extends DozerConverter<Map, Map> implements MapperAware {
    private Mapper mapper;

    public TaskStateDataCustomConverter() {
        super(Map.class, Map.class);
    }

    @Override
    public Map convertTo(Map source, Map destination) {
        return null;
    }

    @Override
    public Map convertFrom(Map source, Map destination) {
        if (source == null) {
            return null;
        }

        if (source instanceof Map) {
            Map<String, TaskStateData> dest = new HashMap<String, TaskStateData>();
            for (Map.Entry<TaskId, TaskState> entry : ((Map<TaskId, TaskState>) source).entrySet()) {
                dest.put(entry.getKey().value(), mapper.map(entry.getValue(), TaskStateData.class));
            }
            return dest;
        } else {
            throw new MappingException("Converter TaskStateDataCustomConverter "
              + "used incorrectly. Arguments passed in were:"
              + destination + " and " + source);
        }
    }

    @Override
    public void setMapper(Mapper mapper) {
        this.mapper = mapper;
    }
}
