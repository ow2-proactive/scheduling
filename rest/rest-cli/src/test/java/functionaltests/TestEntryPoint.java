package functionaltests;

import org.ow2.proactive_grid_cloud_portal.cli.CommonEntryPoint;
import org.ow2.proactive_grid_cloud_portal.cli.EntryPoint;

import static org.ow2.proactive_grid_cloud_portal.cli.RestConstants.RM_RESOURCE_TYPE;

/**
 * Created by Sandrine on 18/09/2015.
 */
public class TestEntryPoint extends EntryPoint {
    @Override
    protected String resourceType() {
        return RM_RESOURCE_TYPE;
    }


    public void runTest(String...args){
        this.run(args);
    }
}
