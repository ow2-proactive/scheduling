package org.objectweb.proactive.ic2d.gui.components.model;

import org.objectweb.fractal.gui.model.BasicComponent;
import org.objectweb.fractal.gui.model.BasicConfiguration;
import org.objectweb.fractal.gui.model.Interface;

import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.adl.nodes.VirtualNode;
import org.objectweb.proactive.core.component.adl.vnexportation.ExportedVirtualNodesList;
import org.objectweb.proactive.core.component.adl.vnexportation.LinkedVirtualNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 * @author Matthieu Morel
 *
 */
public class ProActiveComponentImpl extends BasicComponent
    implements ProActiveComponent {
    // convention : ends with a * if cardinality is multiple
    //private String virtualNode;
    private boolean isParallel = false;
    private String currentlyEditedExportedVirtualNode;
    private String currentlyEditedComposingVirtualNodes;

    // convention : elements are separated with ';'
    //private String exportedVirtualNodes;
    //private Map exportedVirtualNodes;

    /**
     * Constructs a new component.
     *
     * @param owner the configuration to which the component will belong.
     */
    public ProActiveComponentImpl(final BasicConfiguration owner) {
        super(owner);
        //virtualNode = "";
    }

    /**
     *
     * In ProActive, a collective server interface can be seen as one group.
     */
    public Interface getServerInterface(final String name) {
        // TODO make things clearer ! how should we see a group itf?
        // TODO display an external itf connection point
        if (name == null) {
            throw new IllegalArgumentException();
        }

        for (int i = 0; i < serverInterfaces.size(); ++i) {
            Interface itf = (Interface) serverInterfaces.get(i);
            if (itf.isCollection()) {
                if (itf.getMasterCollectionInterface() != null) {
                    itf = itf.getMasterCollectionInterface();
                }
                if (name.startsWith(itf.getName())) {
                    return itf;
                }
            } else if (itf.getName().equals(name)) {
                return itf;
            }
        }
        return null;
    }

    public String getVirtualNode() {
        LinkedVirtualNode lvn = ExportedVirtualNodesList.instance()
                                                        .getLeafVirtualNode(getName());
        if (lvn == null) {
            return "";
        } else {
            return lvn.getVirtualNodeName();
        }
    }

    public void setVirtualNode(String virtualNode) {
        if (virtualNode == null) {
            throw new IllegalArgumentException();
        }
        String oldVirtualNode = getVirtualNode();
        if (!virtualNode.equals(oldVirtualNode)) {
            List vetoableListeners = getOwner().getVetoableListeners();
            for (int i = 0; i < vetoableListeners.size(); ++i) {
                Object l = vetoableListeners.get(i);
                if (l instanceof ProActiveVetoableConfigurationListener) {
                    ((ProActiveVetoableConfigurationListener) l).canChangeVirtualNode(this,
                        virtualNode);
                }
            }
            String cardinality = (virtualNode.endsWith("*")
                ? VirtualNode.MULTIPLE : VirtualNode.SINGLE);
            ExportedVirtualNodesList.instance().addLeafVirtualNode(getName(),
                virtualNode, cardinality);
            List listeners = getOwner().getListeners();
            for (int i = 0; i < listeners.size(); ++i) {
                Object l = listeners.get(i);
                if (l instanceof ProActiveConfigurationListener) {
                    ((ProActiveConfigurationListener) l).virtualNodeChanged(this,
                        oldVirtualNode);
                }
            }
        }

        //System.out.println("virtual node changed to : " + getVirtualNode());
    }

    /**
     *
     */
    public String getExportedVirtualNodesAfterComposition() {
        return ExportedVirtualNodesList.instance()
                                       .getExportedVirtualNodesAfterCompositionAsString(getName());
        //return exportedVirtualNodes;
    }

    public String getExportedVirtualNodesBeforeComposition() {
        return ExportedVirtualNodesList.instance()
                                       .getExportedVirtualNodesBeforeCompositionAsString(getName());
        //return exportedVirtualNodes;
    }

    /**
     *
     */
    public void addExportedVirtualNode(final String virtualNodeName,
        String composingVirtualNodes) {
        if (virtualNodeName == null) {
            throw new IllegalArgumentException();
        }

        LinkedVirtualNode lvn = ExportedVirtualNodesList.instance().getNode(getName(),
                virtualNodeName, false);
        if ((lvn == null) && (composingVirtualNodes == null)) {
            addExportedVirtualNode("", "");
        }
        if (lvn == null) {
            // create a lvn
            lvn = ExportedVirtualNodesList.instance().getNode(getName(),
                    virtualNodeName, true);
        }

        // going to compare strings with no spaces and final ';' removed
        composingVirtualNodes = composingVirtualNodes.replaceAll(" ", "");
        if (composingVirtualNodes.lastIndexOf(';') == composingVirtualNodes.length()) {
            composingVirtualNodes.substring(0,
                composingVirtualNodes.length() - 1);
        }
        String old_composing_virtual_nodes = lvn.getComposingVirtualNodesAsString();
        if ((old_composing_virtual_nodes.equals("") &&
                composingVirtualNodes.equals("")) ||
                !old_composing_virtual_nodes.equals(composingVirtualNodes)) {
            List vetoableListeners = getOwner().getVetoableListeners();
            for (int i = 0; i < vetoableListeners.size(); ++i) {
                ProActiveVetoableConfigurationListener l = (ProActiveVetoableConfigurationListener) vetoableListeners.get(i);
                l.canChangeExportedVirtualNode(this, virtualNodeName);
            }
            if (composingVirtualNodes == null) {
                ExportedVirtualNodesList.instance().removeExportedVirtualNode(getName(),
                    virtualNodeName);
            } else {
                ExportedVirtualNodesList.instance().addExportedVirtualNode(getName(),
                    virtualNodeName, composingVirtualNodes);
            }
            List listeners = getOwner().getListeners();
            for (int i = 0; i < listeners.size(); ++i) {
                ProActiveConfigurationListener l = (ProActiveConfigurationListener) listeners.get(i);
                l.exportedVirtualNodeChanged(this, getVirtualNode(),
                    old_composing_virtual_nodes);
            }
        }
    }

    public List getExportedVirtualNodesNames() {
        Iterator iter = ExportedVirtualNodesList.instance()
                                                .getExportedVirtualNodes(getName())
                                                .iterator();
        List names = new ArrayList();
        while (iter.hasNext()) {
            LinkedVirtualNode lvn = (LinkedVirtualNode) iter.next();
            names.add(lvn.getVirtualNodeName());
        }
        return Collections.unmodifiableList(names);
    }

    public String getExportedVirtualNodeNameAfterComposition(
        String exportedVNName) {
        LinkedVirtualNode lvn = ExportedVirtualNodesList.instance().getNode(getName(),
                exportedVNName, false);
        return lvn.getExportedVirtualNodeNameAfterComposition();
    }

    public List getComposingVirtualNodes(String exportedVirtualNodeName) {
        return ExportedVirtualNodesList.instance().getComposingVirtualNodes(getName(),
            exportedVirtualNodeName);
    }

    public void setComposingVirtualNodes(String virtualNodeName,
        String composingVirtualNodes) {
        ExportedVirtualNodesList.checkComposingVirtualNodesSyntax(composingVirtualNodes);
        ExportedVirtualNodesList.instance().setComposingVirtualNodes(getName(),
            virtualNodeName, composingVirtualNodes);
    }

    public void removeExportedVirtualNode(String exportedVirtualNodeName) {
        ExportedVirtualNodesList.instance().removeExportedVirtualNode(getName(),
            exportedVirtualNodeName);
    }

    public String getComposingVirtualNodesAsString(String virtualNodeName) {
        LinkedVirtualNode lvn = ExportedVirtualNodesList.instance().getNode(getName(),
                virtualNodeName, false);
        if (lvn == null) {
            return null;
        }
        return lvn.getComposingVirtualNodesAsString();
    }

    public String getComponentControllerDescriptor() {
        if (isParallel) {
            return Constants.PARALLEL;
        } else {
            return super.getComponentControllerDescriptor();
        }
    }

    /**
     *
     */
    public void setCurrentlyEditedComposingVirtualNodesNames(
        String composingVirtualNodesNames) {
        currentlyEditedComposingVirtualNodes = composingVirtualNodesNames;
    }

    /**
     *
     */
    public void setCurrentlyEditedExportedVirtualNodeName(
        String exportedVirtualNodeName) {
        currentlyEditedExportedVirtualNode = exportedVirtualNodeName;
    }

    /**
     *
     */
    public String getCurrentlyEditedComposingVirtualNodesNames() {
        return currentlyEditedComposingVirtualNodes;
    }

    /**
     *
     */
    public String getCurrentlyEditedExportedVirtualNodeName() {
        return currentlyEditedExportedVirtualNode;
    }

    public void setParallel() {
        isParallel = true;
    }

    public boolean isParallel() {
        return isParallel;
    }

    //   public void setExportedVirtualNodes(
    //           String exportedVirtualNodes) {
    //      if (exportedVirtualNodes == null) {
    //          throw new IllegalArgumentException();
    //      }
    //      
    //      if (!(exportedVirtualNodes.replaceAll("[^.;]+[.][^.;]+[;]?", "").length() == 0)) {
    //          throw new IllegalArgumentException("exported virtual nodes can only be made of one or several regular expressions like : [^.;]+[.][^.;]+[;]?");
    //      }
    //       
    //      String oldExportedVirtualNodes = this.exportedVirtualNodes;
    //      if (!exportedVirtualNodes.equals(oldExportedVirtualNodes)) {
    //          List vetoableListeners = getOwner().getVetoableListeners();
    //			for (int i = 0; i < vetoableListeners.size(); ++i) {
    //				Object l = vetoableListeners.get(i);
    //				if (l instanceof ProActiveVetoableConfigurationListener) {
    //				    ((ProActiveVetoableConfigurationListener)l).canChangeExportedVirtualNodes(this, exportedVirtualNodes);
    //				}
    //			}
    //			this.exportedVirtualNodes = exportedVirtualNodes;
    //			List listeners = getOwner().getListeners();
    //			for (int i = 0; i < listeners.size(); ++i) {
    //				Object l = listeners.get(i);
    //				if (l instanceof ProActiveConfigurationListener) {
    //				    ((ProActiveConfigurationListener)l).exportedVirtualNodeChanged(this, oldExportedVirtualNodes);
    //				}
    //			}
    //		}
    //		//System.out.println("exported virtual node changed");
    //      }
}
