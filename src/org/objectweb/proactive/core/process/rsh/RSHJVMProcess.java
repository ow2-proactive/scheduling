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
package org.objectweb.proactive.core.process.rsh;

import org.objectweb.proactive.core.util.MessageLogger;
import org.objectweb.proactive.core.process.JVMProcessImpl;
import org.objectweb.proactive.core.process.JVMProcess;

public class RSHJVMProcess extends RSHProcess implements JVMProcess {

  protected JVMProcessImpl jvmProcess;

  //
  // -- CONSTRUCTORS -----------------------------------------------
  //

  public RSHJVMProcess() {
    super();
  }
    
  public RSHJVMProcess(MessageLogger messageLogger) {
    this(messageLogger, messageLogger);
  }
    
  public RSHJVMProcess(MessageLogger inputMessageLogger, MessageLogger errorMessageLogger) {
    super(new JVMProcessImpl(inputMessageLogger, errorMessageLogger));
    jvmProcess = (JVMProcessImpl) targetProcess;
  }
    

  //
  // -- PUBLIC METHODS -----------------------------------------------
  //
    
  public static void main(String[] args) {
    try {
      RSHProcess rsh = new RSHJVMProcess(new StandardOutputMessageLogger());
      rsh.setHostname("solida");
      rsh.startProcess();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  //
  // -- implements JVMProcess -----------------------------------------------
  //

  public String getClasspath() {
    return jvmProcess.getClasspath();
  }
  
  public void setClasspath(String classpath) {
    checkStarted();
    jvmProcess.setClasspath(classpath);
  }
  

  public String getJavaPath() {
    return jvmProcess.getJavaPath();
  }
  
  public void setJavaPath(String javaPath) {
    checkStarted();
    jvmProcess.setJavaPath(javaPath);
  }


  public String getPolicyFile() {
    return jvmProcess.getPolicyFile();
  }
  
  public void setPolicyFile(String policyFile) {
    checkStarted();
    jvmProcess.setPolicyFile(policyFile);
  }
  

  public String getClassname() {
    return jvmProcess.getClassname();
  }
  
  public void setClassname(String classname) {
    checkStarted();
    jvmProcess.setClassname(classname);
  }
  
  
  public String getParameters() {
    return jvmProcess.getParameters();
  }
  
  public void setParameters(String parameters) {
    checkStarted();
    jvmProcess.setParameters(parameters);
  }


  //
  // -- PROTECTED METHODS -----------------------------------------------
  //

  
  //
  // -- PRIVATE METHODS -----------------------------------------------
  //

}
