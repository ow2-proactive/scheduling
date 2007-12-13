/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extensions.calcium.instructions;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.calcium.muscle.Conquer;
import org.objectweb.proactive.extensions.calcium.stateness.Stateness;
import org.objectweb.proactive.extensions.calcium.statistics.Timer;
import org.objectweb.proactive.extensions.calcium.system.PrefetchFilesMatching;
import org.objectweb.proactive.extensions.calcium.system.SkeletonSystemImpl;
import org.objectweb.proactive.extensions.calcium.task.Task;


class ConquerInst<Y, R> implements Instruction<Y, R> {
    static Logger logger = ProActiveLogger.getLogger(Loggers.SKELETONS_STRUCTURE);
    private Conquer<Y, R> conq;

    protected ConquerInst(Conquer<Y, R> conq) {
        this.conq = conq;
    }

    public Task<R> compute(SkeletonSystemImpl system, Task<Y> parent) throws Exception {

        /*
         * We get the result objects from the child and then we execute the
         * conquer. Finally, we create a rebirth task of the parent with the
         * result of the conquer.
         */
        Timer timer = new Timer();
        R resultObject = conq.conquer(system, parent.family.getFinishedChildParams());
        timer.stop();
        Task<R> resultTask = parent.reBirth(resultObject);

        resultTask.getStats().getWorkout().track(conq, timer);
        return resultTask;
    }

    public boolean isStateFul() {
        return Stateness.isStateFul(conq);
    }

    @SuppressWarnings("unchecked")
    public PrefetchFilesMatching getPrefetchFilesAnnotation() {
        Class cls = conq.getClass();

        return (PrefetchFilesMatching) cls.getAnnotation(PrefetchFilesMatching.class);
    }
}
