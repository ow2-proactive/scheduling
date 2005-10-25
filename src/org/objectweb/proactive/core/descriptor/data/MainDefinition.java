package org.objectweb.proactive.core.descriptor.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.core.descriptor.data.VirtualNode;


public class MainDefinition implements Serializable {
    //
    //  ----- PRIVATE MEMBERS -----------------------------------------------------------------------------------
    //

    /** all virtualNodes are put in a List of VirtualNode */
    private List virtualNodeList;

    /** fully qualified name of the main Class */
    private String mainClass;

    /** List containing all parameters of the main method, as String */
    private List parameters;

    //
    //  ----- CONSTRUCTORS -----------------------------------------------------------------------------------
    //
    public MainDefinition(String mainClass, List parameters,
        List virtualNodeList) {
        this.virtualNodeList = virtualNodeList;
        this.mainClass = mainClass;
        this.parameters = parameters;
    }

    public MainDefinition(String mainClass) {
        this(mainClass, new ArrayList(), new ArrayList());
    }

    public MainDefinition() {
        this(null);
    }

    //
    //  ----- PUBLIC METHODS -----------------------------------------------------------------------------------
    //
    //

    /**
     * activates all nodes of the list virtualNodeList
     */
    public void activateMain() {
        // activate all nodes
        for (int i = 0; i < virtualNodeList.size(); i++) {
            getVirtualNode(i).activate();
        }
    }

    /**
     * set the list of virtual nodes
     * @param virtualNodeList new list
     */
    public void setVirtualNodeList(List virtualNodeList) {
        this.virtualNodeList = virtualNodeList;
    }

    /**
     * set the main class attribute
     * @param mainClass fully qualified name of the class containing a main method
     */
    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    /**
     * set the list of parameters
     * @param parameters list of String
     */
    public void setParameters(List parameters) {
        this.parameters = parameters;
    }

    /**
     * return the list of virtual nodes
     * @return list of virtual nodes
     */
    public List getVirtualNodeList() {
        return virtualNodeList;
    }

    /**
     * return a table of virtual nodes
     * @return a table of virtual nodes
     */
    public VirtualNode[] getVirtualNodes() {
        VirtualNode[] result = new VirtualNode[virtualNodeList.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = (VirtualNode) virtualNodeList.get(i);
        }
        return result;
    }

    /**
     * add a virtual node to the list of virtal nodes
     * @param virtualNode virtual node to add
     */
    public void addVirtualNode(VirtualNode virtualNode) {
        virtualNodeList.add(virtualNode);
    }

    /**
     * return the i-th virtual node of the list
     * @param i index of the virtual node to get
     * @return the i-th virtual node of the list
     */
    public VirtualNode getVirtualNode(int i) {
        return (VirtualNode) virtualNodeList.get(i);
    }

    /**
     * return the fully qualified name of the class containing the main method
     * @return fully qualified name of the class containing the main method
     */
    public String getMainClass() {
        return mainClass;
    }

    /**
     * return a table of String containing all the parameters to apply to the main method
     * @return a table of String containing all the parameters to apply to the main method
     */
    public String[] getParameters() {
        String[] result = new String[parameters.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = (String) parameters.get(i);
        }
        return result;
    }

    /**
     * add a parameter to the list of parameters, at the last position
     * @param parameter parameter to add to the list of parameters
     */
    public void addParameter(String parameter) {
        parameters.add(parameter);
    }
}
