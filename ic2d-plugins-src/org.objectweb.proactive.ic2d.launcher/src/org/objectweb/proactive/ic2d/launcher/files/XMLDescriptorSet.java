/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
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
