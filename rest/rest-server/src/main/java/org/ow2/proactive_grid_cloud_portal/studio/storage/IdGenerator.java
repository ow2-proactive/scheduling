package org.ow2.proactive_grid_cloud_portal.studio.storage;

import org.ow2.proactive_grid_cloud_portal.studio.Named;

import java.io.File;

public interface IdGenerator {
    String generateId(File root, Named t);

}