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
package org.objectweb.proactive.examples.readers;

public class Reader {

  private ReaderDisplay display;
  private ReaderWriter rw;
  private boolean done;
  private boolean autopilot;
  private boolean reading;
  private int id;


  /**
   * The no args constructor as commanded by PaPdc
   */
  public Reader() {
  }


  /**
   * The real constructor
   */
  public Reader(ReaderDisplay display, ReaderWriter rw, int id) {
    this.display = display;
    this.rw = rw;
    this.id = id;

    done = false;
    autopilot = true;
    reading = false;
  }


  public void stopIt() {
    done = true;
  }


  public void startRead() {
    reading = true;
    display.setWait(id, true);
    rw.startRead();
    display.setRead(id, true);
  }


  public void endRead() {
    reading = false;
    rw.endRead();
    display.setRead(id, false);
  }


  /**
   * The live method.
   * @param body the body of the Active object
   */
  public void live(org.objectweb.proactive.Body body) {
    org.objectweb.proactive.Service service = new org.objectweb.proactive.Service(body);
    while (!done) {
      service.serveOldest();
      // Autopilot mode
      if (reading)
        endRead();
      else startRead();
      try {
        Thread.sleep((long)700 + (long)(Math.random() * 1500));
      } catch (InterruptedException e) {
      }
    }
  }
}
