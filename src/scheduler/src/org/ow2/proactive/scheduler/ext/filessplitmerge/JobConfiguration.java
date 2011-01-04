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
