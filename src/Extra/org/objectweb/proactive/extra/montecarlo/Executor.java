package org.objectweb.proactive.extra.montecarlo;

import org.objectweb.proactive.extensions.masterworker.TaskException;

import java.util.List;
import java.io.Serializable;


/**
 * Created by IntelliJ IDEA.
 * User: fviale
 * Date: 17 juin 2008
 * Time: 16:51:13
 * To change this template use File | Settings | File Templates.
 */
public interface Executor {

    public List<Serializable> solve(List<EngineTask> engineTasks) throws TaskException;
}
