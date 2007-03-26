package org.objectweb.proactive.extra.infrastructuremanager.frontend;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extra.infrastructuremanager.core.IMCore;


public class IMUserImpl implements IMUser {
    // Attributes
    private IMCore imcore;

    //----------------------------------------------------------------------//
    // CONSTRUCTORS

    /** ProActive compulsory no-args constructor */
    public IMUserImpl() {
    }

    public IMUserImpl(IMCore imcore) {
        System.out.println("[IMUser] constructor");
        this.imcore = imcore;
    }

    //=======================================================//
    public String echo() {
        return "Je suis le IMUser";
    }

    //=======================================================//

    //----------------------------------------------------------------------//
    // METHODS
    public Node getNode() throws NodeException {
        System.out.println("[IMUser] getNode");
        return imcore.getNode();
    }

    public Node[] getAtLeastNNodes(int nb) throws NodeException {
        return imcore.getAtLeastNNodes(nb);
    }

    public void freeNode(Node node) throws NodeException {
        imcore.freeNode(node);
    }

    public void freeNodes(Node[] nodes) throws NodeException {
        imcore.freeNodes(nodes);
    }
}
