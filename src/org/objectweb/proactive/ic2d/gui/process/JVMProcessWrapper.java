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
package org.objectweb.proactive.ic2d.gui.process;

import org.objectweb.proactive.core.process.UniversalProcess;
import org.objectweb.proactive.ic2d.gui.util.MessagePanel;

/**
* MonitoredRSHProcess
*/
public class JVMProcessWrapper {

  private UniversalProcess process;
  private MessagePanel messagePanel;
  
  private String name;
  private String username;
  private String hostname;
  private String javaPath;
  private String policyFile;
  private String classpath;
  private String classname;
  private String parameters;
  
  //
  // -- CONSTRUCTORS -----------------------------------------------
  //
  
  public JVMProcessWrapper(UniversalProcess process, MessagePanel messagePanel, String javaPath, String policyFile, String classpath, String classname, String parameters) {
    this(process, messagePanel, javaPath, policyFile, classpath, classname, parameters, null, null);
  }

  public JVMProcessWrapper(UniversalProcess process, MessagePanel messagePanel, String javaPath, String policyFile, String classpath, 
                                               String classname, String parameters, String hostname, String username) {
    this.process = process;
    this.messagePanel = messagePanel;
    this.javaPath = javaPath;
    this.policyFile = policyFile;
    this.classpath = classpath;
    this.classname = classname;
    this.parameters = parameters;
    this.username = username;
    this.hostname = hostname;
    if (hostname != null) {
      this.name = hostname+" | "+classpath;
    } else {
      this.name = "local | "+classpath;
    }
  }

  //
  // -- PUBLIC METHODS -----------------------------------------------
  //

  public String getName() {
    return name;
  }

  public String getClasspath() {
    return classpath;
  }
  
  public String getJavaPath() {
    return javaPath;
  }
  
  public String getPolicyFile() {
    return policyFile;
  }
  
  public String getClassname() {
    return classname;
  }
  
  public String getParameters() {
    return parameters;
  }

  public String getHostname() {
    return hostname;
  }

  public String getUsername() {
    return username;
  }

  public String toString() {
    return name;
  }
  
  public javax.swing.JPanel getPanel() {
    return messagePanel;
  }
  
  public UniversalProcess getProcess() {
    return process;
  }
  
  
  public void startProcess() {
    try {
      process.startProcess();
    } catch (java.io.IOException e) {
      messagePanel.getMessageLogger().log("Problem when starting the process", e);
    }
  }

  public void stopProcess() {
    process.stopProcess();
  }

  //
  // -- PROTECTED METHODS -----------------------------------------------
  //
  
  //
  // -- PRIVATE METHODS -----------------------------------------------
  //
  
}
