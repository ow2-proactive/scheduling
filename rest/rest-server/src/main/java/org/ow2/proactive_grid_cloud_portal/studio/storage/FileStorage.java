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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.ow2.proactive_grid_cloud_portal.studio.Named;


public class FileStorage<T extends Named> {

    protected final File rootDir;

    protected final IdGenerator idGenerator;

    protected Serializer<T> serializer;

    public FileStorage(File rootDir, Serializer<T> serializer, IdGenerator generator) {
        this.rootDir = rootDir;
        this.serializer = serializer;
        this.idGenerator = generator;
    }

    public T store(T entity) throws IOException {
        String id = idGenerator.generateId(rootDir, entity);
        File entityPath = getPathForId(id);
        return serializer.serialize(entityPath, id, entity);
    }

    public T update(String id, T entity) throws IOException {
        File entityPath = getPathForId(id);
        return serializer.serialize(entityPath, id, entity);
    }

    public List<T> readAll() throws IOException {
        File[] files = rootDir.listFiles();
        ArrayList<T> entities = new ArrayList<>(files.length);
        for (File f : files) {
            entities.add(read(f.getName()));
        }
        return entities;
    }

    public T read(String id) throws IOException {
        File entityPath = getPathForId(id);
        return serializer.deserialize(entityPath, id);
    }

    public T delete(String workflowId) throws IOException {
        File entityPath = getPathForId(workflowId);
        T result = read(entityPath.getName());
        FileUtils.forceDelete(entityPath);
        return result;
    }

    private File getPathForId(String id) {
        return new File(rootDir.getAbsolutePath(), id);
    }
}
