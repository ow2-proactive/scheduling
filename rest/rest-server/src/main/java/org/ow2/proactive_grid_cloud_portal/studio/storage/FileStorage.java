package org.ow2.proactive_grid_cloud_portal.studio.storage;

import org.apache.commons.io.FileUtils;
import org.ow2.proactive_grid_cloud_portal.studio.Named;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
