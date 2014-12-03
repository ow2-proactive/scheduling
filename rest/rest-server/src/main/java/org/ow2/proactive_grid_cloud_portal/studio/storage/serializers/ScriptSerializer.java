package org.ow2.proactive_grid_cloud_portal.studio.storage.serializers;

import org.apache.commons.io.FileUtils;
import org.ow2.proactive_grid_cloud_portal.studio.Script;
import org.ow2.proactive_grid_cloud_portal.studio.storage.Serializer;

import java.io.File;
import java.io.IOException;

public class ScriptSerializer implements Serializer<Script> {
    @Override
    public Script serialize(File f, String id, Script script) throws IOException {
        String absolutePath = f.getAbsolutePath();
        FileUtils.write(new File(absolutePath), script.getContent());
        script.setAbsolutePath(absolutePath);
        return script;
    }

    @Override
    public Script deserialize(File f, String id) throws IOException {

        String name = f.getName();
        String absolutePath = f.getAbsolutePath();
        String content = FileUtils.readFileToString(new File(absolutePath));

        return new Script(name, content, absolutePath);
    }
}
