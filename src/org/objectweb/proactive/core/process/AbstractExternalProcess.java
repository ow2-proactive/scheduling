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

public abstract class AbstractExternalProcess extends AbstractUniversalProcess implements ExternalProcess {

  protected static final boolean IS_WINDOWS_SYSTEM = System.getProperty("os.name").toLowerCase().startsWith("win");
 
  protected Process externalProcess;
  
  private boolean shouldRun;
  
  protected MessageLogger inputMessageLogger;
  protected MessageLogger errorMessageLogger;
  protected MessageSink outputMessageSink;

  private ThreadActivityMonitor inThreadMonitor;
  private ThreadActivityMonitor errThreadMonitor;
    
  
  //
  // -- CONSTRUCTORS -----------------------------------------------
  //

  protected AbstractExternalProcess() {
  }

  
  public AbstractExternalProcess(MessageLogger messageLogger) {
    this(messageLogger, messageLogger, null);
  }
  

  public AbstractExternalProcess(MessageLogger inputMessageLogger, MessageLogger errorMessageLogger) {
    this(inputMessageLogger, errorMessageLogger, null);
  }
  
  public AbstractExternalProcess(MessageLogger inputMessageLogger, MessageLogger errorMessageLogger, MessageSink outputMessageSink) {
    this.inputMessageLogger = inputMessageLogger;
    this.errorMessageLogger = errorMessageLogger;
    this.outputMessageSink = outputMessageSink;
  }


  //
  // -- PUBLIC METHODS -----------------------------------------------
  //
  
  //
  // -- implements ExternalProcess -----------------------------------------------
  //

  public MessageLogger getInputMessageLogger() {
    return inputMessageLogger;
  }
  
  public MessageLogger getErrorMessageLogger() {
    return errorMessageLogger;
  }
  
  public MessageSink getOutputMessageSink() {
    return outputMessageSink;
  }
  
  public void setInputMessageLogger(MessageLogger inputMessageLogger) {
    checkStarted();
    this.inputMessageLogger = inputMessageLogger;
  }
  
  public void setErrorMessageLogger(MessageLogger errorMessageLogger) {
    checkStarted();
    this.errorMessageLogger = errorMessageLogger;
  }
  
  public void setOutputMessageSink(MessageSink outputMessageSink) {
    checkStarted();
    this.outputMessageSink = outputMessageSink;
  }
    

  //
  // -- PROTECTED METHODS -----------------------------------------------
  //

  protected abstract String buildCommand(); 
  

  protected String buildEnvironmentCommand() {
    if (environment == null) return "";
    if (IS_WINDOWS_SYSTEM) {
      return buildWindowsEnvironmentCommand();
    } else {
      return buildUnixEnvironmentCommand();
    }
  }
  
  
  protected String buildWindowsEnvironmentCommand() {
    StringBuffer sb = new StringBuffer();
    for (int i=0; i<environment.length; i++) {
      inputMessageLogger.log("      exporting variable "+environment[i]);
      sb.append("set ");
      sb.append(environment[i]);
      sb.append(" ; ");
    }
    return sb.toString();
  }


  protected String buildUnixEnvironmentCommand() {
    StringBuffer sb = new StringBuffer();
    for (int i=0; i<environment.length; i++) {
      inputMessageLogger.log("      exporting variable "+environment[i]);
      sb.append("export ");
      sb.append(environment[i]);
      sb.append(" ; ");
    }
    return sb.toString();
  }


  protected void internalStartProcess(String commandToExecute) throws java.io.IOException {
    try {
      if (inputMessageLogger != null) inputMessageLogger.log("Command is "+commandToExecute);
      externalProcess = Runtime.getRuntime().exec(commandToExecute);
      java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(externalProcess.getInputStream()));
      java.io.BufferedReader err = new java.io.BufferedReader(new java.io.InputStreamReader(externalProcess.getErrorStream()));
      java.io.BufferedWriter out = new java.io.BufferedWriter(new java.io.OutputStreamWriter(externalProcess.getOutputStream()));
      handleProcess(in, out, err);
    } catch (java.io.IOException e) {
      isFinished = true;
      throw e;
    }
  }
  
 
  protected void internalStopProcess() {
    //System.out.println("--> internalStopProcess");
    if (externalProcess != null) externalProcess.destroy();
    if (outputMessageSink != null) outputMessageSink.setMessage(null);
  }


  protected void handleProcess(java.io.BufferedReader in, java.io.BufferedWriter out, java.io.BufferedReader err) {
    handleInput(in);
    handleOutput(out);
    handleError(err);
  }
  
  protected void handleInput(java.io.BufferedReader in) {
    if (inputMessageLogger == null) return;
    inThreadMonitor = new ThreadActivityMonitor();
    Runnable r = new ProcessInputHandler(in, inputMessageLogger, inThreadMonitor);
    Thread t = new Thread(r, "IN -> "+getShortName(getCommand(), 20));
    t.start();
  }
  
  protected void handleError(java.io.BufferedReader err) {
    if (errorMessageLogger == null) return;
    errThreadMonitor = new ThreadActivityMonitor();
    Runnable r = new ProcessInputHandler(err, errorMessageLogger, errThreadMonitor);
    Thread t = new Thread(r,"ERR -> "+getShortName(getCommand(), 20));
    t.start();
  }

  protected void handleOutput(java.io.BufferedWriter out) {
    if (outputMessageSink == null) return;
    Runnable r = new ProcessOutputHandler(out, outputMessageSink);
    Thread t = new Thread(r,"OUT -> "+getShortName(getCommand(), 20));
    t.start();
  }

  
  //
  // -- PRIVATE METHODS -----------------------------------------------
  //
  
  private final String getShortName(String name, int length) {
    return name.substring(0,Math.min(name.length(), length));
  }
  
  
  private final void waitForMonitoredThread() {
    do {
      try {
        Thread.sleep(300);
      } catch (InterruptedException e) {
      }
    } while (errThreadMonitor.isActive() || inThreadMonitor.isActive());
  }

  //
  // -- INNER CLASSES -----------------------------------------------
  //
  
  
  private static class ThreadActivityMonitor {
    private boolean isActive;
    public boolean isActive() {
      return isActive;
    }
    public void setActive(boolean b) {
      isActive = b;
    }
  }


  /**
   * Implementation of a MessageLogger that output all messages to the standard output
   */  
  public static class StandardOutputMessageLogger implements MessageLogger {
  
    public StandardOutputMessageLogger() {
    }
    
    public void log(String message) {
      System.out.println(message);
    }
    
    public void log(Throwable t) {
      t.printStackTrace();
    }
    
    public void log(String message, Throwable t) {
      System.out.println(message);
      t.printStackTrace();
    }
  
  } // end inner class StandardOutputMessageLogger



  /**
   * Implementation of a MessageLogger that discard all output
   */  
  public static class NullMessageLogger implements MessageLogger {
    public NullMessageLogger() {}    
    public void log(String message) {}    
    public void log(Throwable t) {}    
    public void log(String message, Throwable t) {}  
  } // end inner class NullMessageLogger



  /**
   * Implementation of a MessageSink that can receive one message at a time
   */  
  public static class SimpleMessageSink implements MessageSink {

    private String message;
    private boolean isActive = true;
  
    public synchronized String getMessage() {
      if (! isActive) return null;
      while (message == null && isActive) {
        try {
          wait();
        } catch (InterruptedException e) {}
      }
      String messageToSend = message;
      message = null;
      notifyAll();
      return messageToSend;
    }

  
    public synchronized void setMessage(String messageToPost) {
      if (! isActive) return;
      while (message != null && isActive) {
        try {
          wait();
        } catch (InterruptedException e) {}
      }
      if (messageToPost == null) isActive = false;
      this.message = messageToPost;
      notifyAll();
    }

    public synchronized boolean hasMessage() {
      return message != null;
    }
    
    public synchronized boolean isActive() {
      return isActive;
    }    
    
  } // end inner class SimpleMessageSink
  
  


  /**
   * This class reads all messages from an input and log them using a
   * MessageLogger
   */  
  protected class ProcessInputHandler implements Runnable {
  
    private java.io.BufferedReader in;
    private MessageLogger logger;
    private ThreadActivityMonitor threadMonitor;
    
    public ProcessInputHandler(java.io.BufferedReader in, MessageLogger logger, ThreadActivityMonitor threadMonitor) {
      this.in = in;
      this.logger = logger;
      this.threadMonitor = threadMonitor;
    }
    
    public void run() {
      logger.log("Process started Thread="+Thread.currentThread().getName());
      try {
        while (true) {
          threadMonitor.setActive(false);
          //System.out.println("ProcessInputHandler before readLine()");
          String s = in.readLine();
          //System.out.println("ProcessInputHandler after readLine() s="+s);
          threadMonitor.setActive(true);
          if (s == null) {
            break;
          } else {
            logger.log(s);
          }
        }
      } catch (java.io.IOException e) {
        logger.log(e);
      } finally {         
        isFinished = true;
        threadMonitor.setActive(false);
        try {
          in.close();
        } catch (java.io.IOException e) {}
        logger.log("Process finished Thread="+Thread.currentThread().getName());
      }
    }
  } // end inner class ProcessInputHandler




  /**
   * This class uses a MessageSink to write all messages produced
   * in a given output
   */  
  protected class ProcessOutputHandler implements Runnable {

    private java.io.BufferedWriter out;
    private MessageSink messageSink;
  
    public ProcessOutputHandler(java.io.BufferedWriter out, MessageSink messageSink) {
      this.out = out;
      this.messageSink = messageSink;
    }
    
    public void run() {
      try {
        while (true) {
          waitForMonitoredThread();
          //System.out.println("ProcessOutputHandler before getMessage()");
          String message = messageSink.getMessage();
          if (message == null) break;
          try {
            out.write(message);
            out.newLine();
            out.flush();
            //System.out.println("ProcessOutputHandler writing "+message);
          } catch (java.io.IOException e) {
            break;
          }
          message = null;
        }
      } finally {
        isFinished = true;
        waitForMonitoredThread();
        try {
          out.close();
        } catch (java.io.IOException e) {
        }
      }
    }
    
  } // end inner class ProcessOutputHandler

}
