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
package org.objectweb.proactive.core.process;

/**
 * <p>
 * The JVMProcess class is able to start localy any class of the ProActive library by 
 * creating a Java Virtual Machine.
 * </p>
 * @author  ProActive Team
 * @version 1.0,  2002/09/20
 * @since   ProActive 0.9.4
 */
 

public interface JVMProcess extends ExternalProcess {

	/**
	 * Returns the classpath associated to this process
	 * @return String
	 */
  public String getClasspath();
  
  
  
	/**
	 * Sets the classpath for this process
	 * @param classpath The value of the classpath environment variable
	 */
  public void setClasspath(String classpath);

  
  
	/**
	 * Returns the java path associated to this process.
	 * @return String The path to the java command
	 */
  public String getJavaPath();
  
  
	/**
	 * Sets the java path for this process
	 * @param javaPath The value of the path to execute 'java' command
	 */
  public void setJavaPath(String javaPath);
  

	/**
	 * Returns the location (path) to the policy file
	 * @return String The path to the policy file
	 */
  public String getPolicyFile();
  
  
	/**
	 * Sets the location of the policy file
	 * @param policyFilePath The value of the path to the policy file
	 */
  public void setPolicyFile(String policyFilePath);
  
  
	/**
	 * Returns the location of the log4j property file.
	 * @return String the location of the log4j property file
	 */ 
  public String getLog4jFile();
  
  
	/**
	 * Sets the location of the log4j property file.
	 * @param The value of the path to the log4j property file
	 */
  public void setLog4jFile(String log4fFilePath);
  
	/**
	 * Returns the class name that this process is about to start
	 * @return String The value of the class that this process is going to start
	 */
  public String getClassname();
  
  
	/**
	 * Sets the value of the class to start for this process
	 * @param classname The name of the class to start
	 */
  public void setClassname(String classname);
  
  
	/**
	 * Returns parameters associated to the class that this process is going to start
	 * @return String The value of the parameters of the class
	 */
  public String getParameters();
  
  
	/**
	 * Sets the parameters of the class to start with the given value
	 * @param parameters Paramaters to be given in order to start the class 
	 */
  public void setParameters(String parameters);


  /**
   * Sets the options of the jvm to start
   * <p>
 	 * For instance:
 	 * </p>
 	 * <pre>
 	 * jvmProcess.set JvmOptions("-verbose -Xms300M -Xmx300m");
 	 * </pre>
   * @param options Options to be given in order to start the jvm 
   */
  public void setJvmOptions(String options);
}
