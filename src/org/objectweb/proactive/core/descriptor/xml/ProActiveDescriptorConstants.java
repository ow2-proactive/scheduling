/* 
* ################################################################
* 
* ProActive: The Java(TM) library for Parallel, Distributed, 
*            Concurrent computing with Security and Mobility
* 
* Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
* Contact: proactive-support@inria.fr
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
package org.objectweb.proactive.core.descriptor.xml;

/**
 * Defines many constants useful across ProActive
 *
 * @author  ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 *
 */
public interface ProActiveDescriptorConstants  {
  
  public static final String PROACTIVE_DESCRIPTOR_TAG = "ProActiveDescriptor"; 
  public static final String DEPLOYMENT_TAG = "deployment"; 
  public static final String INFRASTRUCTURE_TAG = "infrastructure"; 
  
  public static final String VIRTUAL_NODES_TAG = "virtualNodes"; 
  public static final String VIRTUAL_NODE_TAG = "virtualNode"; 
  public static final String MAPPING_TAG = "mapping"; 
  public static final String MAP_TAG = "map"; 
  public static final String JVMSET_TAG = "jvmSet";
  public static final String VMNAME_TAG = "vmName";
  public static final String JVMS_TAG = "jvms"; 
  public static final String JVM_TAG = "jvm"; 
  public static final String ACQUISITION_TAG = "acquisition"; 
  public static final String CREATION_PROCESS_TAG = "creation"; 
  public static final String PROCESS_TAG = "process"; 
  public static final String JVM_PROCESS_TAG = "jvmProcess"; 
  public static final String PROCESSES_TAG = "processes"; 
  
  public static final String ENVIRONMENT_TAG = "environment"; 
  public static final String HOST_LIST_TAG = "hostlist";
  public static final String BSUB_OPTIONS_TAG = "bsubOption";
  public static final String SCRIPT_PATH_TAG = "scriptPath";
  public static final String GLOBUS_OPTIONS_TAG = "globusOption";
  public static final String GLOBUS_HOST_TAG = "globusHost";
  public static final String GLOBUS_HOST_LIST_TAG = "globusHostList";
  public static final String GRAM_PORT_TAG = "GramPort";
  public static final String GIS_PORT_TAG = "GISPort";
  public static final String PROCESSOR_TAG = "processor";
  public static final String VARIABLE_TAG = "variable"; 
  public static final String CLASSPATH_TAG = "classpath"; 
  public static final String JAVA_PATH_TAG = "javaPath"; 
  public static final String POLICY_FILE_TAG = "policyFile"; 
  public static final String CLASSNAME_TAG = "classname"; 
  public static final String PARAMETERS_TAG = "parameters"; 
  
  public static final String PATH_TAG = "path"; 
  
  
}