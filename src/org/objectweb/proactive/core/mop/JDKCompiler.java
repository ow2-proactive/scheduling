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
package org.objectweb.proactive.core.mop;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

class JDKCompiler extends Object implements Compiler {
  private static final String FILE_SEPARATOR = System.getProperty("file.separator");
  private static final String DEFAULT_JAVACPATH = findJavacCommand();
  
  private static final String QUOTE;
  static {
    if (System.getProperty("os.name").startsWith("Win"))
      QUOTE = "\"";
    else QUOTE = "";
  }

  private Vector knownclasses = new Vector();


  public JDKCompiler() {
  }
  
  private static String findJavacCommand() {
    String javahome = System.getProperty("java.home");
    if (javahome.endsWith("jre")) {
      javahome = javahome.substring(0,javahome.length()-4);
    }
    return javahome+FILE_SEPARATOR+"bin"+FILE_SEPARATOR+"javac";
  }


  public void compile(File f) throws java.io.IOException {
    String s = f.getAbsolutePath();
    compile(s);
  }


  public void compile(String filename) throws java.io.IOException {
    ProcessWrapper pw = new ProcessWrapper();
    String javac = MOPProperties.getCompilerCommandLine();
    // if the name if non fully qualify we do it
    if (javac.equals("javac")) javac = DEFAULT_JAVACPATH;
    String commandLine = javac + " " + QUOTE + filename + QUOTE;
    pw.go(commandLine);
  }


  protected boolean processHasFinished(Process p) {
    try {
      p.exitValue();
      return true;
    } catch (IllegalThreadStateException e) {
      return false;
    }
  }


  public synchronized boolean needToContinue(String filename) {
    if (this.knownclasses.contains(filename)) {
      return false;
    } else {
      this.knownclasses.addElement(filename);
      return true;
    }
  }
}

/**
 * Class used as a wrapper around an external process. The process'
 * stdout and stderr is redirected to a log display.
 *
 */
class ProcessWrapper {

  // identifies information and error messages
  protected final static int MSG_INFO = 0;
  protected final static int MSG_ERROR = 1;


  /**
   * Creates a new wrapper around an external process.
   *
   * @param cmdarray array containing the command to call and its
   * arguments.
   * @throws IOException if an error occurs spawning the process or
   * when retrieving its standard output and error streams.
   */

  public ProcessWrapper() {
    super();
  }


  public void go(String command) throws java.io.IOException {
    Process proc;
    // execute process
    System.out.println(command);
    if (MOPProperties.getCompilerInheritsClasspath()) {
      String[] envs = new String[1];
      envs[0] = "CLASSPATH=" + System.getProperty("java.class.path");
      proc = Runtime.getRuntime().exec(command);
      System.out.println("Environment settings inherited by the compiler: " + envs[0]);
	    System.out.println("Command is " + command);
      proc = Runtime.getRuntime().exec(command, envs);
    } else {
      proc = Runtime.getRuntime().exec(command);
    }
    // retrieve stdout and stderr
    InputStream stdout = proc.getInputStream();
    InputStream stderr = proc.getErrorStream();
    if (stdout != null) {
      new InputReader(stdout, MSG_INFO).start();
    }
    if (stderr != null) {
      new InputReader(stderr, MSG_ERROR).start();
    }
    // Here we should wait for completion
    while (!(this.processHasFinished(proc))) {
      try {
        proc.waitFor();
      } catch (InterruptedException e) {}
    }
  }


  protected boolean processHasFinished(Process p) {
    try {
      p.exitValue();
      return true;
    } catch (IllegalThreadStateException e) {
      return false;
    }
  }
}

/**
 * Private class InputReader reads lines from an InputStream and
 * writes them to a log display.
 *
 * @author Sindre Mehus
 */
class InputReader extends Thread {

  // MSG_INFO or MSG_ERROR
  private int m_type;

  // reader
  private BufferedReader m_reader;


  /**
   * Creates a new InputReader.
   *
   * @param is the input stream to read from.
   * @param type specifies where to put the messages. Is either
   * {@link MSG_INFO} or {@link MSG_ERROR}.
   */

  public InputReader(InputStream is, int type) {
    m_type = type;
    m_reader = new BufferedReader(new InputStreamReader(is));
  }


  /**
   * Overrides {@link Thread#run}.
   */

  public void run() {
    String s = null;
    // read full lines
    try {
      while ((s = m_reader.readLine()) != null) {
        switch (m_type) {
          case ProcessWrapper.MSG_INFO:
            {
              System.out.println(s);
              break;
            }
          case ProcessWrapper.MSG_ERROR:
            {
              System.err.println(s);
              break;
            }
          default:
            {
              break;
            }
        } // switch()
      } // while()
    } // try
    catch (IOException e) {
      return;
    }

  } // run()
}

// class InputReader
