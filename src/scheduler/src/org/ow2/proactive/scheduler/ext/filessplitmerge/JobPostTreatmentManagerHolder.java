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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.ext.filessplitmerge;

import org.ow2.proactive.scheduler.ext.filessplitmerge.exceptions.NotInitializedException;


/**
 * This class will create and store an instance of the {@link JobPostTreatmentManager}
 * 
 * the concrete implementation of the  {@link JobPostTreatmentManager} should be given in argument to the {@link #setPostTreatmentManager(Class)} method
 * @author esalagea
 *
 */
public class JobPostTreatmentManagerHolder {

    private static JobPostTreatmentManager postTreatmentManager;

    /**
     * Creates and stores an instance of the {@link JobPostTreatmentManager} 
     * @param clazz - the concrete implementation of {@link JobPostTreatmentManager}
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public static void setPostTreatmentManager(Class<? extends JobPostTreatmentManager> clazz)
            throws InstantiationException, IllegalAccessException {
        //		
        //		if (!JobPostTreatmentManager.class.isAssignableFrom(clazz))
        //		{
        //			  throw new IllegalArgumentException("The expected class in argument should be of type "+JobPostTreatmentManager.class.getName());
        //		}

        postTreatmentManager = (JobPostTreatmentManager) clazz.newInstance();
    }

    /**
     * 
     * @return the {@link JobPostTreatmentManager} for this application
     * @throws NotInitializedException - if the {@link #setPostTreatmentManager(Class)} have not yet been called this exeption is thrown
     */
    public static JobPostTreatmentManager getPostTreatmentManager() throws NotInitializedException {
        if (postTreatmentManager == null) {
            throw new NotInitializedException(
                "No post treatment manager has been defined. Use setPostTreatmentManager static method in class " +
                    JobPostTreatmentManagerHolder.class.getName() + "");
        }
        return postTreatmentManager;
    }

}
