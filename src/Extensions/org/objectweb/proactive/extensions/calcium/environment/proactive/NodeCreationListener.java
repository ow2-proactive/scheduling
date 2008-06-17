package org.objectweb.proactive.extensions.calcium.environment.proactive;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;


public class NodeCreationListener {

    int times;

    AOTaskPool taskpool;
    FileServerClientImpl fserver;
    AOInterpreterPool interpool;

    public NodeCreationListener(AOTaskPool taskpool, FileServerClientImpl fserver,
            AOInterpreterPool interpool, int times) {
        super();

        this.taskpool = taskpool;
        this.fserver = fserver;
        this.interpool = interpool;

        this.times = times;
    }

    public void listener(Node node, String virtualNodeName) throws ActiveObjectCreationException,
            NodeException {

        AOInterpreter interp = (AOInterpreter) PAActiveObject.newActive(AOInterpreter.class.getName(),
                new Object[] { taskpool, fserver }, node);

        AOStageIn stageIn = interp.getStageIn(interpool);

        interpool.putInRandomPosition(stageIn, times);
    }
}