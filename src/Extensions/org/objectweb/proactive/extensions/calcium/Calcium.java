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
package org.objectweb.proactive.extensions.calcium;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.extensions.calcium.Stream;
import org.objectweb.proactive.extensions.calcium.environment.EnvironmentFactory;
import org.objectweb.proactive.extensions.calcium.environment.FileServerClient;
import org.objectweb.proactive.extensions.calcium.skeletons.Skeleton;
import org.objectweb.proactive.extensions.calcium.statistics.StatsGlobal;
import org.objectweb.proactive.extensions.calcium.task.TaskPool;


/**
 * This class corresponds to the main entry point of the skeleton framework.
 *
 * To instantiate this class, an {@link org.objectweb.proactive.extensions.calcium.environment.EnvironmentFactory  EnvironmentFactory}  must be provided.
 * The skeleton framework can be used with different EnvironmentFactories, for example:
 * <ul>
 * <li>{@link org.objectweb.proactive.extensions.calcium.environment.multithreaded.MultiThreadedEnvironment MultiThreadedEnvironment} Executes the framework using threads on the local machine.</li>
 * <li>{@link org.objectweb.proactive.extensions.calcium.environment.proactive.ProActiveEnvironment ProActiveEnvironment} Executes the framework using ProActive.</li>
 * <li>{@link org.objectweb.proactive.extra.calcium.environment.proactivescheduler.ProActiveSchedulerEnvironment ProActiveSchedulerEnvironment} Executes the framework using ProActive Scheduler.</li>
 * </ul>
 *
 *
 * @author The ProActive Team (mleyton)
 */
@PublicAPI
public class Calcium {
    private Facade facade;
    private TaskPool taskpool;
    private FileServerClient fserver;
    EnvironmentFactory environment;

    /**
     * The main construction method
     *
     * @param environment An EnvironmentFactory that will be used to execute the framework
     */
    public Calcium(EnvironmentFactory environment) {
        this.taskpool = environment.getTaskPool();
        this.environment = environment;
        this.facade = new Facade(taskpool);
        this.fserver = environment.getFileServer();
    }

    /**
     * This method is used to instantiate a new stream from the framework.
     * The stream is then used to input a parameter T into the framework, and
     * then retrieve the results (R) from the framework.
     *
     * All T inputed into this stream will be computed using the
     * skeleton program specified as parameter.
     *
     * @param <T> The type of the T this stream will accept.
     * @param root The skeleton program that will be computed on each T inputed into the framework
     * @param <R> The result type of the skeleton program.
     * @return A {@link Stream Stream} that can input T and output  from the framework.
     */
    public <T extends java.io.Serializable, R extends java.io.Serializable> Stream<T, R> newStream(
            Skeleton<T, R> root) {
        return new Stream<T, R>(facade, fserver, root);
    }

    /**
     * Boots the framework by calling, among others,  start on the {@link org.objectweb.proactive.extensions.calcium.environment.EnvironmentFactory  EnvironmentFactory}.
     */
    public void boot() {
        facade.boot();
        environment.start();
    }

    /**
     * Shuts down the framework by calling shutdown, among others, shutdown on the {@link org.objectweb.proactive.extensions.calcium.environment.EnvironmentFactory  EnvironmentFactory}.
     */
    public void shutdown() {
        facade.shutdown();
        environment.shutdown();
    }

    /**
     * This method can be used to get a snapshot on the global statistics of the framework.
     * To get an updated version of the statistics, the method must be re-invoked.
     *
     * @return The current status of the global statistics.
     */
    public StatsGlobal getStatsGlobal() {
        return taskpool.getStatsGlobal();
    }
}
