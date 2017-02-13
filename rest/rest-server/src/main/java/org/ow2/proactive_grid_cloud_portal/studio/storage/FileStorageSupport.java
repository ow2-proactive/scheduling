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
package org.ow2.proactive_grid_cloud_portal.studio.storage;

import java.io.File;

import org.ow2.proactive_grid_cloud_portal.studio.Script;
import org.ow2.proactive_grid_cloud_portal.studio.Workflow;
import org.ow2.proactive_grid_cloud_portal.studio.storage.generators.NameAsIdGenerator;
import org.ow2.proactive_grid_cloud_portal.studio.storage.generators.SmallestAvailableIdGenerator;
import org.ow2.proactive_grid_cloud_portal.studio.storage.serializers.ScriptSerializer;
import org.ow2.proactive_grid_cloud_portal.studio.storage.serializers.WorkflowSerializer;


public class FileStorageSupport {

    private final File userWorkflowsDir;

    private final File templateWorkflowsDir;

    public FileStorageSupport(File userWorkflowsDir, File templateWorkflowsDir) {
        this.userWorkflowsDir = userWorkflowsDir;
        this.templateWorkflowsDir = templateWorkflowsDir;
    }

    public File getWorkflowsDir(String userName) {
        return new File(userWorkflowsDir, userName);
    }

    public FileStorage<Workflow> getTemplateStorage() {
        templateWorkflowsDir.mkdirs();
        return new FileStorage<>(templateWorkflowsDir, new WorkflowSerializer(), new SmallestAvailableIdGenerator());
    }

    public FileStorage<Workflow> getWorkflowStorage(String userName) {
        File workflowsDir = new File(getWorkflowsDir(userName), "workflows");
        workflowsDir.mkdirs();
        return new FileStorage<>(workflowsDir, new WorkflowSerializer(), new SmallestAvailableIdGenerator());
    }

    public FileStorage<Script> getScriptStorage(String userName) {
        File scriptsDir = new File(getWorkflowsDir(userName), "scripts");
        scriptsDir.mkdirs();
        return new FileStorage<>(scriptsDir, new ScriptSerializer(), new NameAsIdGenerator());

    }
}
