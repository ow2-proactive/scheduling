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
package org.objectweb.proactive.examples.philosophers;


public class AppletPhil extends org.objectweb.proactive.examples.AppletWrapper {

//  private javax.swing.JButton bStart;
  private String url;
  private DinnerLayout theLayout;
  private javax.swing.JPanel theLayoutPanel;

  public AppletPhil(String name, int width, int height) {
    /* 
     * Everything is done in AppletWrapper
     */
    super(name, width, height);
  }


  public static void main(String args[]) {
	/* 
	 * Builds the root Frame, including "createRootPanel"
	 *
	 * this includes:
		this.init()			(builds graphic objects)
		AppletWrapper.createParentFrame (creates main Frame)
		  calls this.createRootPanel()
		this.start()
		this.repaint()
	 */
    AppletPhil phil = new AppletPhil("Philosophers", 450, 300);
    phil.displayMessage("Applet running...");
    if (args.length == 1) {
      phil.setURL(args[0]);
    }
    phil.go();
  }

    private void go () {
    try {
      /* le Layout est necessairement actif, puisqu'il est referencé par tous les autres objets.
       */
      theLayout = (DinnerLayout)org.objectweb.proactive.ProActive.turnActive(theLayout);
      if (url != null)
        theLayout.setNode(url);
	/*
	 * Builds the active Table and Philosophers:
	 */
      org.objectweb.proactive.ProActive.waitFor(theLayout.init());
      theLayout.activateButtons();
      displayMessage("Objects activated...");
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }


  public void setURL(String url) {
    this.url = url;
  }

  /* Called by AppletWrapper before creating the toplevel Frame: */

  public void init() {
    // Get the images
    javax.swing.Icon imgArray[] = new javax.swing.Icon[5];
    if (isApplet) {
      String imgThink = getParameter("IMGTHINK");
      String imgWait = getParameter("IMGWAIT");
      String imgEat = getParameter("IMGEAT");
      String imgFork0 = getParameter("IMGFORK0");
      String imgFork1 = getParameter("IMGFORK1");

      // Beware: AppletWrapper.displayMessage is not available at this point
      //						(during bootstrap)
      // displayMessage("Loading images");
      
      // Create the layout
  
      imgArray[0] = new javax.swing.ImageIcon(getImage(getDocumentBase(), imgThink));
      imgArray[1] = new javax.swing.ImageIcon(getImage(getDocumentBase(), imgWait));
      imgArray[2] = new javax.swing.ImageIcon(getImage(getDocumentBase(), imgEat));
      imgArray[3] = new javax.swing.ImageIcon(getImage(getDocumentBase(), imgFork0));
      imgArray[4] = new javax.swing.ImageIcon(getImage(getDocumentBase(), imgFork1));
    } else {
      // displayMessage("Loading alternate images");
      try {
        ClassLoader c = this.getClass().getClassLoader();
        imgArray[0] = new javax.swing.ImageIcon(c.getResource("org/objectweb/proactive/examples/philosophers/think.gif"));
        imgArray[1] = new javax.swing.ImageIcon(c.getResource("org/objectweb/proactive/examples/philosophers/wait.gif"));
        imgArray[2] = new javax.swing.ImageIcon(c.getResource("org/objectweb/proactive/examples/philosophers/eat.gif"));
        imgArray[3] = new javax.swing.ImageIcon(c.getResource("org/objectweb/proactive/examples/philosophers/fork0.gif"));
        imgArray[4] = new javax.swing.ImageIcon(c.getResource("org/objectweb/proactive/examples/philosophers/fork1.gif"));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    // the DinnerLayout constructor creates the graphical objects:
    theLayout = new DinnerLayout(imgArray);
    theLayoutPanel = theLayout.getDisplay();
  }

  //
  // -- PROTECTED METHODS -----------------------------------------------
  //
  /* createRootPanel:
   * abstract method of AppletWrapper.
   * result: the JPanel to be inserted in the upper part of the Main Frame.
   */

  protected javax.swing.JPanel createRootPanel() {
    javax.swing.JPanel rootPanel = theLayoutPanel;
    return rootPanel;
  }
}
