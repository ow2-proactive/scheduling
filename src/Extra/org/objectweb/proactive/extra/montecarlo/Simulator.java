package org.objectweb.proactive.extra.montecarlo;

import org.objectweb.proactive.extensions.masterworker.TaskException;

import java.util.List;
import java.util.ArrayList;


/**
 * Created by IntelliJ IDEA.
 * User: fviale
 * Date: 17 juin 2008
 * Time: 16:08:02
 * To change this template use File | Settings | File Templates.
 */
public interface Simulator {

    public ArrayList<Double> solve(List<Experience> experiences) throws TaskException;
}
