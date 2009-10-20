//============================================================================
// Name        : ProActive Files Split-Merge Framework
// Author      : Emil Salageanu, ActiveEon team
// Version     : 0.1
// Copyright   : Copyright ActiveEon 2008-2009, Tous Droits Réservés (All Rights Reserved)
// Description : Framework for building distribution layers for native applications
//================================================================================

package org.ow2.proactive.scheduler.ext.filessplitmerge;

/**
 * One should implement this interface in order to describe the configuration of his job. 
 * When a job is submitted, a JobConfiguration object will be attached to it (using the generic information mechanism).
 * 
 * Useful object for the disconnected mode and fault tolerance. 
 * When the result of job arrives, the JobConfiguration object can be obtain as well. This might be useful for merging the results of the job  
 * and provide fault tolerance (if the application crashed between the moment when the job has submitted and the moment when the result is available,
 * all the information needed to merge the results should be found in this object)
 *   
 *   IMPORTANT: how to implement this object
 *    
 *     The JobConfiguration concrete class should be implemented as a bean (for each property provide a getter and a setter). 
 *     
 *     The framework will use introspection in order to discover this properties 
 *     and automatically call your getters and setters. 
 *     
 *     
 *   CONSTRAINT: 
 *   
 *   As we use the generic information mechanism of the scheduler, only will be considered the properties of type java.lang.String 
 *     
 *       
 * @author esalagea
 *
 */
public interface JobConfiguration {

}
