package org.objectweb.proactive.ic2d.launcher.files;

import java.util.HashMap;
import java.util.Observable;


public class XMLDescriptorSet extends Observable {
    private HashMap<String, XMLDescriptor> files;
    private static XMLDescriptorSet instance;

    private XMLDescriptorSet() {
        files = new HashMap<String, XMLDescriptor>();
    }

    public static XMLDescriptorSet getInstance() {
        if (instance == null) {
            instance = new XMLDescriptorSet();
        }
        return instance;
    }

    public void addFile(XMLDescriptor file) {
        this.files.put(file.getPath(), file);
        setChanged();
        notifyObservers(file);
    }

    public void removeFile(XMLDescriptor file) {
        this.files.remove(file.getPath());
        setChanged();
        notifyObservers();
    }

    public void removeFile(String path) {
        this.files.remove(path);
        setChanged();
        notifyObservers();
    }

    public Object[] getFilePaths() {
        Object[] paths = files.keySet().toArray();
        return paths;
    }

    public XMLDescriptor getFile(String path) {
        return files.get(path);
    }

    /**
     * Returns the name and the state of the file to display.
     * For example : 'Hello.xml - activated'
     * @return The name to display concatenated with the file state.
     */
    public String getFileNameToDisplay(String path) {
        XMLDescriptor file = this.files.get(path);
        if (file != null) {
            String name = file.getName();
            switch (file.getState()) {
            case LAUNCHED:
                return name + " - activated";
            case ERROR:
                return name + " - error";
            case KILLED:
                return name + " - killed";
            case TERMINATED:
                return name + " - terminated";
            default:
                return name;
            }
        } else {
            return null;
        }
    }
}
