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
package org.objectweb.proactive.examples.penguin;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.util.CircularArrayList;
import org.objectweb.proactive.ext.migration.MigrationStrategyManagerImpl;


public class PenguinControler implements org.objectweb.proactive.RunActive, PenguinMessageReceiver, java.io.Serializable {

  //The image panel
  private transient PenguinApplet display;
  protected CircularArrayList penguinList;
  String[] args;
  private MigrationStrategyManagerImpl myStrategyManager;


  public PenguinControler() {
  }


  public PenguinControler(String[] args) {
    this.penguinList = new CircularArrayList(20);
    this.args = args;
    try {
      ProActive.turnActive(this);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  public void rebuild() {
    this.display = new PenguinApplet((PenguinControler) ProActive.getStubOnThis(), penguinList);
  }


  public void clean() {
    if (display != null) {
      display.dispose();
      display = null;
    }
  }


  public void receiveMessage(String s) {
    display.receiveMessage(s);
  }

  public Penguin createPenguin(int n) {
    try {
      Penguin newPenguin = (Penguin) org.objectweb.proactive.ProActive.newActive(Penguin.class.getName(), new Object[] { new Integer(n) });
      newPenguin.initialize(args);
      newPenguin.setControler((PenguinControler) ProActive.getStubOnThis());
      return newPenguin;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }


  public void runActivity(Body b) {
    org.objectweb.proactive.Service service = new org.objectweb.proactive.Service(b);
    myStrategyManager = new MigrationStrategyManagerImpl((org.objectweb.proactive.core.body.migration.Migratable) b);
    myStrategyManager.onDeparture("clean");
    rebuild();
    service.fifoServing();
    clean();
  }




  public static void main(String args[]) {
    try {
      // ProActive.newActive(AdvancedPenguinControler.class.getName(),null,(Node) null);
      new PenguinControler(args);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
