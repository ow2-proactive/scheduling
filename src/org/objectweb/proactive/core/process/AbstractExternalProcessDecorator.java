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
import org.objectweb.proactive.core.process.AbstractExternalProcess;
import org.objectweb.proactive.core.process.UniversalProcess;

public abstract class AbstractExternalProcessDecorator extends AbstractExternalProcess implements ExternalProcessDecorator {

  protected  ExternalProcess targetProcess;

  private int compositionType = APPEND_TO_COMMAND_COMPOSITION;

  //
  // -- CONSTRUCTORS -----------------------------------------------
  //

  public AbstractExternalProcessDecorator() {
    super();
  }

  public AbstractExternalProcessDecorator(ExternalProcess targetProcess) {
    this(targetProcess, APPEND_TO_COMMAND_COMPOSITION);
  }

  public AbstractExternalProcessDecorator(ExternalProcess targetProcess, int compositionType) {
    super();
    setTargetProcess(targetProcess);
    this.compositionType = compositionType;
  }


  //
  // -- PUBLIC METHODS -----------------------------------------------
  //

  public ExternalProcess getTargetProcess() {
    return targetProcess;
  }


  public void setTargetProcess(ExternalProcess targetProcess) {
    checkStarted();
    this.targetProcess = targetProcess;
    setInputMessageLogger(targetProcess.getInputMessageLogger());
    setErrorMessageLogger(targetProcess.getErrorMessageLogger());
    setOutputMessageSink(targetProcess.getOutputMessageSink());
  }


  public int getCompositionType() {
    return compositionType;
  }


  public void setCompositionType(int compositionType) {
    checkStarted();
    this.compositionType = compositionType;
  }


  //
  // -- PROTECTED METHODS -----------------------------------------------
  //

  protected void toString(StringBuffer sb) {
    super.toString(sb);
    sb.append(" ---- Target Process ----- \n");
    if (targetProcess == null) {
      sb.append(" NOT DEFINED \n");
    } else {
      sb.append(targetProcess.toString());
    }
    sb.append(" -------------- \n");
  }


  protected String buildCommand() {
    if (compositionType == SEND_TO_OUTPUT_STREAM_COMPOSITION || compositionType == GIVE_COMMAND_AS_PARAMETER) {
      return internalBuildCommand();
    } else {
      if (targetProcess != null) {
        return internalBuildCommand()+targetProcess.getCommand();
      } else {
        return internalBuildCommand();
      }
    }
  }


  protected abstract String internalBuildCommand();


  protected void internalStartProcess(String command) throws java.io.IOException {
  	//System.out.println("---------------Internal start process of AbstractExternalProcessDecorator "+command);
    super.internalStartProcess(command);
    if (compositionType == SEND_TO_OUTPUT_STREAM_COMPOSITION) {
      try {
        Thread.currentThread().sleep(3000);
        //System.out.println("---------------Internal start process of AbstractExternalProcessDecorator");
      } catch (InterruptedException e) {}
      // the masterProcess is started, now we feed the output with the slave command
      outputMessageSink.setMessage(targetProcess.getCommand());
    }
  }


  protected void handleOutput(java.io.BufferedWriter out) {
    if (compositionType == SEND_TO_OUTPUT_STREAM_COMPOSITION) {
      if (outputMessageSink == null) outputMessageSink = new SimpleMessageSink();
    }
    super.handleOutput(out);
  }


  //
  // -- PRIVATE METHODS -----------------------------------------------
  //



  //
  // -- INNER CLASSES -----------------------------------------------
  //

  /**
   * Implementation of a MessageLogger that feeds two MessageLoggers
   */
  public static class CompositeMessageLogger implements MessageLogger,java.io.Serializable {

    private MessageLogger messageLogger1;
    private MessageLogger messageLogger2;

    public CompositeMessageLogger(MessageLogger messageLogger1, MessageLogger messageLogger2) {
      this.messageLogger1 = messageLogger1;
      this.messageLogger2 = messageLogger2;
    }

    public void log(String message) {
      messageLogger1.log(message);
      messageLogger2.log(message);
    }

    public void log(Throwable t) {
      messageLogger1.log(t);
      messageLogger2.log(t);
    }

    public void log(String message, Throwable t) {
      messageLogger1.log(message, t);
      messageLogger2.log(message, t);
    }

  } // end inner class CompositeMessageLogger



  /**
   * Implementation of a MessageSink that can receive one message at a time
   */
  public static class CompositeMessageSink implements MessageSink {

    private String message;
    private boolean isActive = true;

    private MessageSink messageSink1;
    private MessageSink messageSink2;

    public CompositeMessageSink(MessageSink messageSink1, MessageSink messageSink2) {
      this.messageSink1 = messageSink1;
      this.messageSink2 = messageSink2;
    }

    public synchronized String getMessage() {
      while ((! hasMessage()) && isActive()) {
        try {
          wait(1000);
        } catch (InterruptedException e) {}
      }
      if (messageSink1.hasMessage()) {
        return messageSink1.getMessage();
      } else if (messageSink2.hasMessage()) {
        return messageSink1.getMessage();
      }
      return null;
    }


    public synchronized void setMessage(String messageToPost) {
      messageSink1.setMessage(messageToPost);
      notifyAll();
    }

    public synchronized boolean hasMessage() {
      return messageSink1.hasMessage() || messageSink2.hasMessage();
    }

    public synchronized boolean isActive() {
      return messageSink1.isActive() || messageSink2.isActive();
    }
  } // end inner class CompositeMessageSink

}
