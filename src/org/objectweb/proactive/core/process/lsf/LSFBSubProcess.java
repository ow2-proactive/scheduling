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
package org.objectweb.proactive.core.process.lsf;

import org.objectweb.proactive.core.process.SimpleExternalProcess;
import org.objectweb.proactive.core.process.AbstractExternalProcessDecorator;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.process.MessageSink;
import org.objectweb.proactive.core.util.MessageLogger;

public class LSFBSubProcess extends AbstractExternalProcessDecorator {
    
  public static final String DEFAULT_QUEUE_NAME = "normal";
  
  protected int jobID;
  
  protected String queueName = DEFAULT_QUEUE_NAME;
  
  //
  // -- CONSTRUCTORS -----------------------------------------------
  //

  public LSFBSubProcess() {
    super();
    this.hostname = null;
  }
  
  
  public LSFBSubProcess(ExternalProcess targetProcess) {
    super(targetProcess);
    this.hostname = null;
  }

    
  //
  // -- PUBLIC METHODS -----------------------------------------------
  //
  
  public void setInputMessageLogger(MessageLogger inputMessageLogger) {
    super.setInputMessageLogger(new CompositeMessageLogger(new ParserMessageLogger(), inputMessageLogger));
  }
    
  public void setOutputMessageSink(MessageSink outputMessageSink) {
    if (outputMessageSink == null) {
      super.setOutputMessageSink(new SimpleMessageSink());
    } else {
      super.setOutputMessageSink(outputMessageSink);
    }
  }

  public static ExternalProcess buildBKillProcess(int jobID) {
    return new SimpleExternalProcess("bkill "+jobID);
  }
  
    
  public static void main(String[] args) {
    try {
      LSFBSubProcess p = new LSFBSubProcess(new SimpleExternalProcess("ls -lsa"));
      p.startProcess();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  
  public int getJobID() {
    return jobID;
  }
  
  
  public String getQueueName() {
    return queueName;
  }
  
  public void setQueueName(String queueName) {
    checkStarted();
    if (queueName == null) throw new NullPointerException();
    this.queueName = queueName;
  }


  //
  // -- PROTECTED METHODS -----------------------------------------------
  //

  protected String internalBuildCommand() {
    return buildEnvironmentCommand()+buildBSubCommand();
  }
  
  
  protected String buildBSubCommand() {
    return "bsub -n 1 -q "+queueName+" ";
  }
  
  
  protected String buildBJobsCommand() {
    return "bjobs "+jobID;
  }
  
  /**
   * parses a message in order to find the job id of the 
   * launched job.
   * we assume here that the jobid is displayed following this 
   * convention :
   *    Job <...>
   */
  protected int parseJobID(String message) {
    //System.out.println("parseJobID analyzing "+message);
    String beginJobIDMarkup = "Job <";
    String endJobIDMarkup = ">";
    int n1 = message.indexOf(beginJobIDMarkup);
    if (n1 == -1) return 0;
    int n2 = message.indexOf(endJobIDMarkup, n1+beginJobIDMarkup.length());
    if (n2 == -1) return 0;
    String id = message.substring(n1+beginJobIDMarkup.length(), n2);
    //System.out.println("!!!!!!!!!!!!!! JOBID = "+id);
    try {
      return Integer.parseInt(id);
    } catch (NumberFormatException e) {
      return 0;
    }
  }
  
  
  /**
   * parses the hostname from a string. We assume that the line  
   * looks like that :
   *    191009 user  status  queue     fromHost     targetHost        *eep 10000 Jan 25 13:33
   * Where targetHost is the hostname we are looking for.
   * status could be at least 
   *     - PEND for pending (means targethost is undetermined
   *     - anything else (means targethost is known
   * @param message the string that may contains the hostname
   * @return null if the message did not contains any hostname,  
   * an empty string if the message did contains the target host but 
   * was undertermined because the job was still pending. Return the 
   * hostname if it is found.
   */
  protected String parseHostname(String message) {
    //System.out.println("parseHostname analyzing "+message);
    java.util.StringTokenizer st = new java.util.StringTokenizer(message);
    if (st.countTokens() < 6) return null; // we expect at least 6 tokens
    try {
      int currentJobID = Integer.parseInt(st.nextToken());
      if (currentJobID != jobID) return null; // not the same id
    } catch (NumberFormatException e) {
      return null;
    }
    st.nextToken(); // ignore user
    String status = st.nextToken();
    if (status.equals("PEND")) {
      return ""; // not running yet
    }
    st.nextToken(); // ignore queue
    st.nextToken(); // ignore fromHost
    String hostname = st.nextToken();
    //System.out.println("!!!!!!!!!!!!!! hostname = "+hostname);
    return hostname;
  }
  
  protected void sendJobDetailsCommand() {
    outputMessageSink.setMessage(buildBJobsCommand());
  }
  
  
  //
  // -- PRIVATE METHODS -----------------------------------------------
  //



  //
  // -- INNER CLASSES -----------------------------------------------
  //

  /**
   * Implementation of a MessageLogger that look for the jobID of the launched job
   */  
  public class ParserMessageLogger implements MessageLogger {
  
    private boolean foundJobID;
    private boolean foundHostname;
  
    public ParserMessageLogger() {
    }
    
    public void log(String message) {
      if (! foundJobID) {
        jobID = parseJobID(message);
        foundJobID = jobID != 0;
        if (foundJobID) sendJobDetailsCommand();
      } else if (! foundHostname) {
        hostname = parseHostname(message);
        if (hostname != null) {
          foundHostname = hostname.length() > 0;
          if (foundHostname) {
            // we are done
            outputMessageSink.setMessage(null);
          } else {
            // send another command to fetch the hostname
            try {
              Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
            sendJobDetailsCommand();
          }
        }        
      }
    }
    
    public void log(Throwable t) {
    }
    
    public void log(String message, Throwable t) {
    }
  
  } // end inner class CompositeMessageLogger
}
