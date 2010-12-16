package org.ow2.proactive.scheduler.ext.matsci.common;

import org.objectweb.proactive.core.node.Node;


/**
 * ProcessListener
 *
 * @author The ProActive Team
 */
public interface ProcessListener {

    void setNode(Node node);

    void setDeployID(Integer deployID);

    Integer getDeployID();
}
