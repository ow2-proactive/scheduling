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

/**
 * <p>
 * The SSHProcess class is able to start any class, of the ProActive library, 
 * using ssh protocol.
 * </p><p>
 * For instance:
 * </p><pre>
 * .......
 * SSHProcess ssh = new SSHProcess(new SimpleExternalProcess("ls -lsa"));
 * ssh.setHostname("hostname.domain.fr");
 * ssh.startProcess();
 * .....
 * </pre>
 * @author  ProActive Team
 * @version 1.0,  2002/09/20
 * @since   ProActive 0.9.4
 */

public class SSHProcess extends AbstractExternalProcessDecorator {
  
    
    public final static String DEFAULT_SSHPATH="ssh";
  //
  // -- CONSTRUCTORS -----------------------------------------------
  //
  
  /**
   * Creates a new SSHProcess
   * Used with XML Descriptor
   */
  public SSHProcess() {
    super();
    this.command_path = DEFAULT_SSHPATH;
  
  }
  
  /**
   * Creates a new SSHProcess
   * @param targetProcess The target process associated to this process. The target process 
   * represents the process that will be launched after logging remote host with ssh protocol
   */
  public SSHProcess(ExternalProcess targetProcess) {
    super(targetProcess);
    this.command_path = DEFAULT_SSHPATH;
  }
    

  //
  // -- PUBLIC METHODS -----------------------------------------------
  //
    
  public static void main(String[] args) {
    try {

      SSHProcess ssh = new SSHProcess(new SimpleExternalProcess("ls -lsa"));
      ssh.setHostname("galere1.inria.fr");
      ssh.startProcess();

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
    if(logger.isDebugEnabled()){
    logger.debug(command.toString());  
    }
    return command.toString();
  }
  
  
  protected String buildWindowsSSHCommand() {
    StringBuffer command = new StringBuffer();
    command.append(command_path);
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
