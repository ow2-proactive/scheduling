package org.objectweb.proactive.core.group;

import org.objectweb.proactive.core.component.group.ProxyForComponentInterfaceGroup;
import org.objectweb.proactive.core.component.group.TaskFactoryCollectiveItfs;


/**
 * A factory for task factories. 
 * 
 * Indeed, groups dispatch parameters in configurable non-broadcast modes if and only if: 
 * they are instances of groups, and group parameters are tagged as "scatter".
 * 
 * On the contrary, the component framework simply interprets annotations on the signatures of
 * classes / methods / arguments.
 * 
 * Hence the two distinct factories.
 * 
 * @author The ProActive Team
 *
 */
public class TaskFactoryFactory {

    public static TaskFactory getTaskFactory(ProxyForGroup groupProxy) {
        if (groupProxy instanceof ProxyForComponentInterfaceGroup) {
            return new TaskFactoryCollectiveItfs(groupProxy);
        } else {
            return new BasicTaskFactory(groupProxy);
        }
    }

}
