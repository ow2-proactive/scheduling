/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive_grid_cloud_portal.studio.storage.serializers;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.ow2.proactive_grid_cloud_portal.studio.Workflow;
import org.ow2.proactive_grid_cloud_portal.studio.storage.Serializer;


public class WorkflowSerializer implements Serializer<Workflow> {
    @Override
    public Workflow serialize(File workflowDir, String id, Workflow workflow) throws IOException {
        FileUtils.forceMkdir(workflowDir);

        FileUtils.write(new File(workflowDir, "name"), workflow.getName());
        FileUtils.write(new File(workflowDir, "metadata"), workflow.getMetadata());
        FileUtils.write(new File(workflowDir, "job.xml"), workflow.getXml());

        workflow.setId(Long.parseLong(id));

        return workflow;
    }

    @Override
    public Workflow deserialize(File workflowDir, String id) throws IOException {
        String name = readFile(new File(workflowDir, "name"), id);
        String xml = readFile(new File(workflowDir, "job.xml"), id);
        String metadata = readFile(new File(workflowDir, "metadata"), id);
        return new Workflow(Long.parseLong(workflowDir.getName()), name, xml, metadata);
    }

    private String readFile(File file, String id) throws IOException {
        if (file.exists()) {
            return FileUtils.readFileToString(file);
        }
        throw new IOException(String.format("Could not find the file %s when de-serializing workflow %s",
                                            file.getAbsolutePath(),
                                            id));
    }

}
