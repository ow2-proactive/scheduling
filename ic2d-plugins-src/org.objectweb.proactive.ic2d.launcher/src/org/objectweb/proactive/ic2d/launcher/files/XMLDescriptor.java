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

import java.util.Observable;

import org.objectweb.proactive.core.descriptor.Launcher;
import org.objectweb.proactive.core.descriptor.data.MainDefinition;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptorInternal;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.ic2d.launcher.exceptions.TagMissingException;


public class XMLDescriptor extends Observable {
    private String jobID;
    private Launcher launcher;
    private String path;
    private String name;
    private String shortName;
    private FileState state = FileState.DEFAULT;

    //
    // -- CONSTRUCTORS ---------------------------------------------
    //
    public XMLDescriptor(String path) {
        this.path = path;
        initFileName(path);
    }

    //
    // -- PUBLIC METHODS ---------------------------------------------
    //

    /**
     * @return The name of the xml descriptor.
     * (For example : The name of Examples/Hello.xml is Hello.xml)
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return The short name of the xml descriptor.
     * (For example : The short name of Examples/Hello.xml is Hello)
     */
    public String getShortName() {
        return this.shortName;
    }

    /**
     * @return The associated launcher to this xml descriptor.
     */
    public Launcher getLauncher() {
        return launcher;
    }

    /**
     * Set the associated launcher to this xml descriptor.
     * @param launcher The launcher.
     * @throws TagMissingException
     */
    public void setLauncher(Launcher launcher) throws TagMissingException {
        this.launcher = launcher;
        if (launcher == null) {
            return;
        }
        ProActiveDescriptorInternal proActiveDesc = launcher.getProActiveDescriptor();
        MainDefinition[] mainDefinitions = proActiveDesc.getMainDefinitions();

        // If there is NO mainDesfinition tag.
        if (mainDefinitions.length == 0) {
            String message = "The descriptor must contain a mainDefinition tag!\n\nFile : " + this.path;
            setChanged();
            notifyObservers(message);
            throw new TagMissingException(this);
        }

        VirtualNode[] virtualNodes = mainDefinitions[0].getVirtualNodes();

        // If there is NO mapToVirtualNode tag.
        if (virtualNodes.length == 0) {
            String message = "The descriptor must contain a mapToVirtualNode tag!\n\nFile : " + this.path;
            setChanged();
            notifyObservers(message);
            throw new TagMissingException(this);
        }
        this.jobID = virtualNodes[0].getJobID();
    }

    /**
     * @return The jobID associated with this application.
     */
    public String getJobID() {
        return this.jobID;
    }

    /**
     * @return The path of this file.
     */
    public String getPath() {
        return path;
    }

    /**
     * @return The current state of this file.
     */
    public FileState getState() {
        return this.state;
    }

    /**
     * Set the current state of this file.
     * @param state The new state.
     */
    public void setState(FileState state) {
        this.state = state;
        setChanged();
        notifyObservers();
    }

    /**
     * @return The name of the image.
     */
    public String getImage() {
        switch (state) {
            case LAUNCHED:
                return /*ISharedImages.IMG_TOOL_CUT;*/"fileSucces.gif";
            case KILLED:
                return /*ISharedImages.IMG_OBJS_ERROR_TSK*/"fileError.gif";
            case TERMINATED:
                return /*ISharedImages.IMG_TOOL_BACK_DISABLED*/"fileDefault.gif";
            case ERROR:
                return /*ISharedImages.IMG_OBJS_ERROR_TSK*/"fileError.gif";
            default:
                return /*ISharedImages.IMG_OBJ_FILE*/"fileDefault.gif";
        }
    }

    //
    // -- PRIVATE METHODS ---------------------------------------------
    //

    /**
     * Init the name of the file from the file's path.
     * @param path
     */
    private void initFileName(String completePath) {
        String name = "";
        if (completePath.lastIndexOf("/") >= 0) {
            name = completePath.substring(completePath.lastIndexOf("/") + 1);
        } else {
            name = completePath.substring(completePath.lastIndexOf("\\") + 1);
        }
        this.name = name;
        if (name.endsWith(".xml")) {
            name = name.substring(0, name.lastIndexOf('.'));
        }
        this.shortName = name;
    }

    public enum FileState {
        DEFAULT, LAUNCHED, TERMINATED, KILLED, ERROR;
    }
}
