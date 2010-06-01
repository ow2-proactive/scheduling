package org.ow2.proactive.resourcemanager.examples.documentation;

import java.io.File;
import java.security.KeyException;
import java.security.PublicKey;
import java.util.Iterator;

import javax.security.auth.login.LoginException;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.utils.NodeSet;


public class UserGuide {

    public static ResourceManager connect(String rmAddress, String user, String password) {
        //@snippet-start UserGuide_Connect
        RMAuthentication auth = null;
        try {
            // rmAddress is a String of the form: "rmi://hostname:port/"
            // e.g. rmi://localhost:1099/
            auth = RMConnection.join(rmAddress);
        } catch (RMException e) {
            // The connection to the resource manager cannot be established.
            e.printStackTrace();
        }
        ResourceManager resourceManager = null;
        try {
            // (1) preferred authentication method (getting credentials from the disk)
            resourceManager = auth.login(Credentials.getCredentials());
        } catch (KeyException e) {
            try {
                // (2) valid authentication method (creating credentials on the fly)
                PublicKey pubKey = auth.getPublicKey();
                if (pubKey == null) {
                    pubKey = Credentials.getPublicKey(Credentials.getPubKeyPath());
                }

                if (pubKey != null) {
                    // user and password have to be valid user and password names for the
                    // resource manager
                    // e.g. by default, "user" and "pwd" are valid
                    // refer to ${rm.home}/config/authentification/login.cfg to see all valid
                    // pairs and to add some others.
                    Credentials cred = Credentials.createCredentials(user, password, pubKey);
                    resourceManager = auth.login(cred);
                }
            } catch (KeyException ex) {
                // cannot retrieve the public key
                ex.printStackTrace();
            } catch (LoginException ex) {
                // incorrect user name or password
                ex.printStackTrace();
            }
        } catch (LoginException e) {
            // incorrect user name or password
            e.printStackTrace();
        }
        //@snippet-end UserGuide_Connect
        return resourceManager;
    }

    public static NodeSet getNodes(ResourceManager resourceManager, int nbOfNodes,
            SelectionScript selectionScript) {
        //@snippet-start UserGuide_GetNodes
        // For running your computations, you have to get one or more nodes from the
        // resource manager. The resource manager will return you as many nodes verifying
        // the selection script as it can within the limits of the given number of nodes.
        NodeSet nodeSet = resourceManager.getAtMostNodes(nbOfNodes, selectionScript);
        //@snippet-end UserGuide_GetNodes
        return nodeSet;
    }

    public static BooleanWrapper releaseNodes(ResourceManager resourceManager, NodeSet nodeSet) {
        //@snippet-start UserGuide_ReleaseNodes
        // Once your computations done, you can release nodes.
        BooleanWrapper released = resourceManager.releaseNodes(nodeSet);
        //@snippet-end UserGuide_ReleaseNodes
        return released;
    }

    public static BooleanWrapper disconnect(ResourceManager resourceManager) {
        //@snippet-start UserGuide_Disconnect
        // Once the resource manager is not needed anymore, you can disconnect.
        BooleanWrapper disconnected = resourceManager.disconnect();
        //@snippet-end UserGuide_Disconnect
        return disconnected;
    }

    private static SelectionScript getLinuxScript() {
        //@snippet-start UserGuide_LinuxScript
        String script = "" + "/* Check if OS name is Linux and their is wireless connection available */"
            + "if (java.lang.System.getProperty('os.name').toLowerCase().equals('linux')){"
            + "        selected = true;" + "} else {" + "        selected = false;" + "}";

        SelectionScript selectionScript = null;
        try {
            selectionScript = new SelectionScript(script, "JavaScript");
        } catch (InvalidScriptException e) {
            e.printStackTrace();
        }
        //@snippet-end UserGuide_LinuxScript
        return selectionScript;
    }

    private static SelectionScript getWindowsScript() {
        //@snippet-start UserGuide_WindowsScript
        SelectionScript selectionScript = null;
        try {
            selectionScript = new SelectionScript(new File(PAResourceManagerProperties.RM_HOME +
                "samples/scripts/selection/checkWindows.js"), new String[] {}, false);
        } catch (InvalidScriptException e) {
            e.printStackTrace();
        }
        //@snippet-end UserGuide_WindowsScript
        return selectionScript;
    }

    public static void main(String[] args) {
        ResourceManager resourceManager = UserGuide.connect(args[0], args[1], args[2]);
        if (resourceManager == null) {
            System.out.println("Connection has failed");
        } else {
            System.out.println("Connection has been performed properly");
        }

        SelectionScript selectionScript = UserGuide.getWindowsScript();

        NodeSet nodeSet = UserGuide.getNodes(resourceManager, 1, selectionScript);

        if (nodeSet == null || nodeSet.size() == 0) {
            System.out.println("No Windows node has been got");
        } else {
            System.out.println(nodeSet.size() + "Windows nodes have been got");
            for (Iterator<Node> iterator = nodeSet.iterator(); iterator.hasNext();) {
                Node node = iterator.next();
                System.out.println(node.getNodeInformation().getURL());
            }
        }

        selectionScript = UserGuide.getLinuxScript();

        nodeSet.addAll(UserGuide.getNodes(resourceManager, 2, selectionScript));

        if (nodeSet == null || nodeSet.size() == 0) {
            System.out.println("No Linux node has been got");
        } else {
            System.out.println(nodeSet.size() + " Linux nodes have been got: ");
            for (Iterator<Node> iterator = nodeSet.iterator(); iterator.hasNext();) {
                Node node = iterator.next();
                System.out.println(node.getNodeInformation().getURL());
            }
        }

        BooleanWrapper released = UserGuide.releaseNodes(resourceManager, nodeSet);

        if (released.booleanValue()) {
            System.out.println("Nodes have been released properly");
        } else {
            System.out.println("Nodes have not been released");
        }

        BooleanWrapper disconnected = UserGuide.disconnect(resourceManager);

        if (disconnected.booleanValue()) {
            System.out.println("The resource manager has been disconnected");
        } else {
            System.out.println("The resource manager has not been disconnected");
        }

    }

}
