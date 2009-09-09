package functionaltests.nodestate;

import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;
import org.ow2.proactive.resourcemanager.frontend.RMUser;


public class GetAllNodes {
    public static void main(String[] args) {
        RMAuthentication auth;
        try {
            auth = RMConnection.join(null);
            Credentials cred = Credentials.createCredentials("demo", "demo", auth.getPublicKey());
            RMUser admin = auth.logAsUser(cred);
            admin.getAtMostNodes(admin.getFreeNodesNumber().intValue(), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(1);
    }
}
