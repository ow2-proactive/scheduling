package org.objectweb.proactive.p2p.peerconfiguration;
import javax.swing.*;
import javax.swing.event.*;


public class SliderValueUpdate implements ChangeListener {

protected JSlider j=null;
protected JTextField t=null;

 public SliderValueUpdate(JSlider j, JTextField t) {
	 this.j=j;
     this.t=t;
 }

public void stateChanged(ChangeEvent e) {
     Integer CPULoad = new Integer(j.getValue());
     t.setText(CPULoad.toString() + "%");
 }

}