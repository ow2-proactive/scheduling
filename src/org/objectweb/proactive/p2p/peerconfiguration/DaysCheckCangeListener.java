package org.objectweb.proactive.p2p.peerconfiguration;
import javax.swing.*;
import javax.swing.event.*;


public class DaysCheckCangeListener implements ChangeListener {

       protected JCheckBox[] cbx = null;
       protected boolean selected = false;

       public DaysCheckCangeListener(JCheckBox[] cbx) {
		      this.cbx = cbx;
	   }

       public void stateChanged(ChangeEvent e) {
		      if (cbx!=null) {
				  if (!selected) {
				     for (int i=0; i<cbx.length; i++) {
						 if (!cbx[i].isSelected()) cbx[i].setSelected(true);
				     }
				     selected=true;
			      } else {
					 selected=false;
				  }
			  }
       }

}