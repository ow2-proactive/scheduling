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

import org.objectweb.proactive.core.util.MessageLogger;

public class SimpleExternalProcess extends AbstractExternalProcess {
  
  private String targetCommand;
  
  //
  // -- CONSTRUCTORS -----------------------------------------------
  //

  public SimpleExternalProcess(String targetCommand) {
    this(new StandardOutputMessageLogger(), targetCommand);
  }
  
  public SimpleExternalProcess(MessageLogger messageLogger, String targetCommand) {
    this(messageLogger, messageLogger, targetCommand);
  }
  

  public SimpleExternalProcess(MessageLogger inputMessageLogger, MessageLogger errorMessageLogger, String targetCommand) {
    super(inputMessageLogger, errorMessageLogger);
    this.targetCommand = targetCommand;
  }
  

  //
  // -- PUBLIC METHODS -----------------------------------------------
  //
    
  public static void main(String[] args) {
    try {
      String targetCommand = null;
      if (args.length > 0)
        targetCommand = args[0];
      else targetCommand = "ls -las";  
      SimpleExternalProcess p = new SimpleExternalProcess(targetCommand);
      p.startProcess();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  //
  // -- PROTECTED METHODS -----------------------------------------------
  //

  protected String buildCommand() {
    return targetCommand;
  }
  
  
  //
  // -- PRIVATE METHODS -----------------------------------------------
  //


}
