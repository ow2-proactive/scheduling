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
package org.objectweb.proactive.core.process.ssh;

import org.objectweb.proactive.core.process.AbstractExternalProcessDecorator;
import org.objectweb.proactive.core.process.SimpleExternalProcess;
import org.objectweb.proactive.core.process.ExternalProcess;

public class SSHProcess extends AbstractExternalProcessDecorator {
  
  //
  // -- CONSTRUCTORS -----------------------------------------------
  //

  public SSHProcess() {
    super();
  }
  
  public SSHProcess(ExternalProcess targetProcess) {
    super(targetProcess);
  }
    

  //
  // -- PUBLIC METHODS -----------------------------------------------
  //
    
  public static void main(String[] args) {
    try {
      SSHProcess rsh = new SSHProcess(new SimpleExternalProcess("ls -lsa"));
      rsh.setHostname("solida");
      rsh.startProcess();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  //
  // -- PROTECTED METHODS -----------------------------------------------
  //

  protected String internalBuildCommand() {
    return buildSSHCommand()+buildEnvironmentCommand();
  }
  
  
  protected String buildSSHCommand() {
    if (IS_WINDOWS_SYSTEM) {
      return buildWindowsSSHCommand();
    } else {
      return buildUnixSSHCommand();
    }
  }
  
  
  protected String buildUnixSSHCommand() {
    StringBuffer command = new StringBuffer();
    command.append("ssh");
    // append username
    if (username != null) {
      command.append(" -l ");
      command.append(username);
    }
    // append host
    command.append(" ");
    command.append(hostname);
    command.append(" ");   
    return command.toString();
  }
  
  
  protected String buildWindowsSSHCommand() {
    StringBuffer command = new StringBuffer();
    command.append("ssh");
    command.append(" ");
    command.append(hostname);
    // append username
    if (username != null) {
      command.append(" -l ");
      command.append(username);
    }
    // append host
    command.append(" ");
    return command.toString();
  }
  
  
  //
  // -- PRIVATE METHODS -----------------------------------------------
  //

}
