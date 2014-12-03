package org.ow2.proactive_grid_cloud_portal.studio.storage.generators;

import org.ow2.proactive_grid_cloud_portal.studio.Named;
import org.ow2.proactive_grid_cloud_portal.studio.storage.IdGenerator;

import java.io.File;


public class SmallestAvailableIdGenerator implements IdGenerator {
    public String generateId(File f, Named n) {
        long id = 1;
        while (new File(f, String.valueOf(id)).exists()) {
            id++;
        }
        return Long.toString(id);
    }

}
