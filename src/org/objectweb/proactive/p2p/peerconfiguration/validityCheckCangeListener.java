package org.objectweb.proactive.p2p.peerconfiguration;
import javax.swing.*;
import javax.swing.event.*;


public class validityCheckCangeListener implements ChangeListener {

       protected JSpinner[] sp = null;
       protected boolean selected = false;

       public validityCheckCangeListener(JSpinner[] sp) {
		      this.sp = sp;
	   }

       public void stateChanged(ChangeEvent e) {
		      if (sp!=null) {
				  if (!selected) {
				     for (int i=0; i<sp.length; i++) {
						 sp[i].setEnabled(false);
				     }
				     selected=true;
			      } else {
				     for (int i=0; i<sp.length; i++) {
						 sp[i].setEnabled(true);
				     }
					 selected=false;
				  }
			  }
       }

}