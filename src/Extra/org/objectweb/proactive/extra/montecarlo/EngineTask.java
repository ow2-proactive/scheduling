package org.objectweb.proactive.extra.montecarlo;

import java.io.Serializable;


/**
 * Created by IntelliJ IDEA.
 * User: fviale
 * Date: 17 juin 2008
 * Time: 16:06:08
 * To change this template use File | Settings | File Templates.
 */
public interface EngineTask {

    public Serializable run(Simulator simulator, Executor executor);

}
