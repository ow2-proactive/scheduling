/*
 *  *
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
 *  * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.task.context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.google.common.base.Strings;
import org.ow2.proactive.scheduler.task.utils.ForkerUtils;

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