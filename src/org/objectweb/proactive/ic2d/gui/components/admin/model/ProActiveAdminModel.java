package org.objectweb.proactive.ic2d.gui.components.admin.model;

import javax.swing.JOptionPane;

import org.objectweb.fractal.gui.admin.model.BasicAdminModel;
import org.objectweb.fractal.gui.model.Component;
import org.objectweb.fractal.gui.model.IllegalOperationException;
import org.objectweb.proactive.ic2d.gui.components.model.ProActiveComponent;
import org.objectweb.proactive.ic2d.gui.components.model.ProActiveConfigurationListener;
import org.objectweb.proactive.ic2d.gui.components.model.ProActiveVetoableConfigurationListener;

/**
 * @author Matthieu Morel
 *
 */
public class ProActiveAdminModel extends BasicAdminModel implements ProActiveConfigurationListener, ProActiveVetoableConfigurationListener {
    
	public void virtualNodeChanged(ProActiveComponent component, String oldValue) {
		// nothing to do (should never be called, because of veto)
	}

	public void exportedVirtualNodeChanged(ProActiveComponent component, String virtualNodeName, String oldValue) {
		// nothing to do (should never be called, because of veto)
	}

    // --------------------------
    public void canChangeVirtualNode(Component component, String virtualNode) {
    	org.objectweb.fractal.api.Component ci = getInstance(component);
    	if (ci != null) {
    		//throw new IllegalOperationException(
    		avert("Cannot change the virtual node of an instantiated component");
    	}
    
    }

    public void canChangeExportedVirtualNode(ProActiveComponent component, String exportedVirtualNodes) {
        //avert("Sorry, in the current version, you have to change the export setting manually by editing the ADL files.");
        org.objectweb.fractal.api.Component ci = getInstance(component);
    	
    	if (ci != null) {
    		//throw new IllegalOperationException(
    		avert("Cannot change and exported virtual node of an instantiated component");
    	}
    
    }

	private void avert(String motif) {
		JOptionPane.showMessageDialog(null, motif, "Error", JOptionPane.ERROR_MESSAGE);
		throw new IllegalOperationException(motif);
	}
	
	


}
