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
package org.objectweb.proactive.core.process.globus;

import org.objectweb.proactive.core.ProActiveException;
import org.apache.log4j.Logger;
import org.globus.gram.Gram;
import org.globus.gram.GramJob;

public class Globus {
	
	static Logger logger = Logger.getLogger(Globus.class.getName());
  public static final int DEFAULT_PORT = 754;
  public static final String GLOBUS_HOST_PROP_NAME = "globus_host";
  public static final String GLOBUS_PORT_PROP_NAME = "globus_port";

  private String host = "localhost";
  private int port = DEFAULT_PORT; // Default Globus !
  private String classpath;
  private String javaPath;
  private static boolean debug = false;

  public void setProperties(java.util.HashMap p) {
    host = (String) p.get(GLOBUS_HOST_PROP_NAME);
    Integer portInteger = (Integer) p.get(GLOBUS_PORT_PROP_NAME);
    if (portInteger == null)
      port = DEFAULT_PORT;
    else port = portInteger.intValue();  
  }

  public void startRMIRegistry() throws ProActiveException {
    try {
      String classpath=System.getProperty("java.class.path", ".");
      String rsl;
      rsl="&(executable=/usr/local/java1.2/bin/rmiregistry)(directory=/u/satura/0/oasis/abergel)(environment=(CLASSPATH "+classpath+"))";
      GramJob job=new GramJob(rsl);
      GramJobSignaler sign=new GramJobSignaler();
      job.addListener(sign);
      Gram.request(host+":"+port, job);
      while(sign.statusChanged()) 
        Thread.sleep(1000);
    }
    catch (Exception e) {
      e.printStackTrace();
      throw new ProActiveException("Error submitting gram job: ", e);
    }
  }

  public void setClasspath(String cp) {
    this.classpath = cp;
  }


  public void setJavaPath(String jp) {
    this.javaPath = jp;
  }


  public boolean startCommand(String commandName, String args) throws ProActiveException {
    try {
      String cp;
      if (classpath == null)
        cp = System.getProperty("java.class.path", ".");
      else
        cp = classpath;

      String rsl;
      logger.info("-=+-=+-=+-=+-=+-=+-=+-=+-=+-=+-=+-=+-=+\n" + commandName + " " + args + "<=\n-=+-=+-=+-=+-=+-=+-=+-=+-=+-=+-=+-=+-=+-=+");
      if (args == null || args.equals(""))
        rsl = "&(executable=" + commandName + ")(directory=/u/satura/0/oasis/abergel)(environment=(CLASSPATH " + cp + "))";
      else
        rsl = "&(executable=" + commandName + ")(arguments=" + args + ")(directory=/u/satura/0/oasis/abergel)(environment=(CLASSPATH " + cp + "))";

      if (Globus.debug)
        logger.info("-=+-=+-=+-=+-=+-=+\nRSL: " + rsl + "\n-=+-=+-=+-=+-=+-=+");

      GramJob job = new GramJob(rsl);

      GramJobSignaler sign = new GramJobSignaler();
      job.addListener(sign);

      Gram.request(host + ":" + port, job);
      while (sign.statusChanged())
        Thread.sleep(1000);
    } catch (InterruptedException e) {
    } catch (Exception e) {
      if (Globus.debug)
        e.printStackTrace();
      throw new ProActiveException("Error submitting gram job: " + e.getMessage());
    }
    return true;
  }


  public boolean startClass(String nameCls, String args) throws ProActiveException {
    try {
      String cp;
      if (classpath == null)
        cp = System.getProperty("java.class.path", ".");
      else
        cp = classpath;

      String jp;
      if (javaPath == null)
        jp = "/usr/local/java1.2/bin/";
      else
        jp = javaPath;

      String rsl;

      if (Globus.debug) {
        if (args == null)
          rsl = "&(executable=" + jp + "java)(arguments=" + nameCls + ")(directory=/u/satura/0/oasis/abergel)(environment=(CLASSPATH " + cp + "))(stdout=/tmp/stdoutglobus)(stderr=/tmp/stderrglobus)";
        else
          rsl = "&(executable=" + jp + "java)(directory=/u/satura/0/oasis/abergel)" + "(environment=(CLASSPATH " + cp + "))(arguments=" + nameCls + " " + args + ")(stdout=/tmp/stdoutglobus)(stderr=/tmp/stderrglobus)";
      } else {
        if (args == null)
          rsl = "&(executable=" + jp + "java)(arguments=" + nameCls + ")(directory=/u/satura/0/oasis/abergel)(environment=(CLASSPATH " + cp + "))";
        else
          rsl = "&(executable=" + jp + "java)(directory=/u/satura/0/oasis/abergel)" + "(environment=(CLASSPATH " + cp + "))(arguments=" + nameCls + " " + args + ")";
      }
      if (Globus.debug)
        logger.info("-=+-=+-=+-=+-=+-=+\nRSL: " + rsl + "\n-=+-=+-=+-=+-=+-=+");

      GramJob job = new GramJob(rsl);

      GramJobSignaler sign = new GramJobSignaler();
      job.addListener(sign);

      Gram.request(host + ":" + port, job);
      while (!sign.statusChanged())
        Thread.sleep(1000);
    } catch (InterruptedException e) {
    } catch (Exception e) {
      if (Globus.debug)
        e.printStackTrace();
      throw new ProActiveException("Error submitting gram job: " + e.getMessage());
    }
    return true;
  }
}
