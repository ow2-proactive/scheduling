//============================================================================
// Name        : ProActive Files Split-Merge Framework
// Author      : Emil Salageanu, ActiveEon team
// Version     : 0.1
// Copyright   : Copyright ActiveEon 2008-2009, Tous Droits Réservés (All Rights Reserved)
// Description : Framework for building distribution layers for native applications
//================================================================================

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
