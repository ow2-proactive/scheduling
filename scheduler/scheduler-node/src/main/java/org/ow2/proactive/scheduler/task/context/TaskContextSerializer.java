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
package org.ow2.proactive.scheduler.task.context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.ow2.proactive.scheduler.task.utils.ForkerUtils;

import com.google.common.base.Strings;


public class TaskContextSerializer implements Serializable {

    /**
     * Serializes a task context to disk.
     * @param context The context object to serialize.
     * @param directory The directory where to save the context object.
     * @return A file pointing/holding the serialized context object.
     * @throws IOException
     */
    public File serializeContext(TaskContext context, File directory) throws IOException {
        // prefix must be at least 3 characters long
        String tmpFilePrefix = Strings.padStart(context.getTaskId().value(), 3, '0');

        File file = File.createTempFile(tmpFilePrefix, null, directory);
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(file))) {
            objectOutputStream.writeObject(context);
        }
        if (context.isRunAsUser()) {
            ForkerUtils.setSharedPermissions(file);
        }
        return file;
    }
}
