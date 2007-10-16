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
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extensions.calcium;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.extensions.calcium.Stream;
import org.objectweb.proactive.extensions.calcium.environment.EnvironmentFactory;
import org.objectweb.proactive.extensions.calcium.environment.FileServerClient;
import org.objectweb.proactive.extensions.calcium.skeletons.Skeleton;
import org.objectweb.proactive.extensions.calcium.statistics.StatsGlobal;
import org.objectweb.proactive.extensions.calcium.task.TaskPool;


/**
 * This class corresponds to the entry point of the skeleton framework.
 *
 * In order to instantiate this class, an Environment Factory must be provided.
 * The Enviroment Factory must implement the EnvironmentFactory class. The skeleton
 * kernel can be used with different Environment Factories, for example: MultihreadedEnvironment (Parallel)
 * or ProActiveEnvironment (Distributed).
 *
 * @author The ProActive Team (mleyton)
 */
@PublicAPI
public class Calcium {
    private Facade facade;
    private TaskPool taskpool;
    private FileServerClient fserver;
    EnvironmentFactory environment;

    public Calcium(EnvironmentFactory environment) {
        this.taskpool = environment.getTaskPool();
        this.environment = environment;
        this.facade = new Facade(taskpool);
        this.fserver = environment.getFileServer();
    }

    /**
     * This method is used to instantiate a new stream from the framework.
     * The stream is then used to input T into the framework, and
     * then retrieve the results (T) from the framework.
     *
     * All the T inputed into this stream will be computed using the
     * skeleton strucutre specified as parameter.
     *
     * @param <T> The type of the T this stream will work with.
     * @param root This skeleton represents the structured code that will
     * be executed for each T inputted into the stream.
     * @param <T> Th
     * @return A Stream that can input and output T from the framework.
     */
    public <T extends java.io.Serializable, R extends java.io.Serializable> Stream<T, R> newStream(
        Skeleton<T, R> root) {
        return new Stream<T, R>(facade, fserver, root);
    }

    public void boot() {
        facade.boot();
        environment.start();
    }

    public void shutdown() {
        facade.shutdown();
        environment.shutdown();
    }

    /**
     * @return The current status of the global statistics.
     */
    public StatsGlobal getStatsGlobal() {
        return taskpool.getStatsGlobal();
    }
}
