package org.ow2.proactive_grid_cloud_portal.studio.storage.generators;

import org.ow2.proactive_grid_cloud_portal.studio.Named;
import org.ow2.proactive_grid_cloud_portal.studio.storage.IdGenerator;

import java.io.File;

public class NameAsIdGenerator implements IdGenerator {
    @Override
    public String generateId(File root, Named entity) {
        return entity.getName();
    }
}
