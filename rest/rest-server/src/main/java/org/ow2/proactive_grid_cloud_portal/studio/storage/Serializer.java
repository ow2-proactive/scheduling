package org.ow2.proactive_grid_cloud_portal.studio.storage;

import org.ow2.proactive_grid_cloud_portal.studio.Workflow;

import java.io.File;
import java.io.IOException;

public interface Serializer<T> {

    T serialize(File f, String id, T t) throws IOException;

    T deserialize(File f, String id) throws IOException;

}
