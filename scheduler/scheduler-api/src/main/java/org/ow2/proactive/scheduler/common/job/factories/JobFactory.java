/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.job.factories;

import java.net.URI;
import java.util.Map;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.exception.JobCreationException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.apache.log4j.Logger;


/**
 * JobFactory is used to parse XML Job descriptor.
 * It can use different implementation of parsing.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 *
 * $Id$
 */
@PublicAPI
public abstract class JobFactory {

    public static Logger logger = Logger.getLogger(JobFactory.class);

    /** Temp dir to store temporary archive content */
    public static final String JOBFACTORY_TMPDIR_PROPERTY = "pas.jobfactory.tmpdir";

    /** List of job factory implementation */
    private static final String[] CURRENT_IMPL = new String[] { "org.ow2.proactive.scheduler.common.job.factories.JobFactory_stax" };

    /** Archive special manifest directory, file, property */
    public static final String ARCHIVE_DEFAULT_XMLFILE = "job.xml";
    public static final String ARCHIVE_MANIFEST_DIRECTORY = "JOB-INF";
    public static final String ARCHIVE_MANIFEST_FILE = "manifest.mf";
    public static final String ARCHIVE_MANIFEST_PROPERTY_XMLFILE = "job-xml";

    /**
     * Try to instantiate the known factories.
     * Return the created instance of the jobFactory.
     * As it may instantiate built'in factories, this method rarely fails but a RuntimeException is raised if
     * no factory can be found.
     *
     * @return the instance of the jobFactory.
     */
    public static JobFactory getFactory() {
        JobFactory factory = null;
        for (String factoryInstance : CURRENT_IMPL) {
            try {
                ClassLoader cl = JobFactory.class.getClassLoader();
                Class c = cl.loadClass(factoryInstance);
                factory = (JobFactory) c.newInstance();
                break;
            } catch (ClassNotFoundException e) {
                logger.warn("Cannot instanciate this factory : " + factoryInstance, e);
            } catch (Exception e) {
                logger.warn("Error while instanciating this factory : " + factoryInstance, e);
            }
        }
        if (factory == null) {
            throw new RuntimeException("Cannot instanciate any factory ! (see WARN logs to know why)");
        }
        return factory;
    }

    /**
     * Try to instantiate the given factory.
     * Return the created instance of the jobFactory.
     * If the factory is not found or not created, a runtime exception is raised.
     *
     * @return the instance of the required factory.
     */
    public static JobFactory getFactory(String impl) {
        if (impl == null) {
            return getFactory();
        }

        JobFactory factory;

        try {
            factory = (JobFactory) Class.forName(impl).newInstance();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Cannot instanciate this factory : " + impl, e);
        } catch (Exception e) {
            throw new RuntimeException("Error while instanciating this factory : " + impl, e);
        }
        return factory;
    }

    /**
     * Creates a job using the given job descriptor.
     *
     * @param filePath the path to an XML job descriptor.
     * @return a Job instance created with the given XML file.
     * @throws JobCreationException if an exception occurred during job creation.
     */
    public abstract Job createJob(String filePath) throws JobCreationException;

    public abstract Job createJob(String filePath, Map<String, String> variables) throws JobCreationException;

    /**
     * @see #createJob(String)
     */
    public abstract Job createJob(URI filePath) throws JobCreationException;

    public abstract Job createJob(URI filePath, Map<String, String> variables) throws JobCreationException;

}
