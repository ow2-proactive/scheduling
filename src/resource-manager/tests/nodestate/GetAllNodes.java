package nodestate;

import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;
import org.ow2.proactive.resourcemanager.frontend.RMUser;


public class GetAllNodes {
    public static void main(String[] args) {
        RMAuthentication auth;
        try {
            auth = RMConnection.join(null);
            RMUser admin = auth.logAsUser("demo", "demo");
            admin.getAtMostNodes(admin.getFreeNodesNumber(), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(1);
    }
}
