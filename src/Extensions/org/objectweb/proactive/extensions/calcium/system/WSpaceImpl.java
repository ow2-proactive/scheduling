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
package org.objectweb.proactive.extensions.calcium.system;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;


/**
 * This class is an implementation of the {@link WSpace} interface.
 *
 * This class is not serializable on purpose, since it is environment dependent.
 *
 * @author The ProActive Team
 */
public class WSpaceImpl implements WSpace, java.io.Serializable {
    File wspace;

    public WSpaceImpl(File wspace) throws IOException {
        this.wspace = wspace;

        if (wspace.exists()) {
            throw new IOException("Working space already exists: " + wspace.getPath());
        } else if (!wspace.mkdirs()) {
            throw new IOException("Unable to create working space: " + wspace.getPath());
        }

        if (!wspace.canWrite()) {
            throw new IOException("Cannot write to working space: " + wspace.getPath());
        }
    }

    /**
     * This method deletes the working space associated to this thread.
     * It is not intended to be directly used by programmers.
     *
     * If no working space has been created for this thread,
     * then this method does nothing.
     */
    public boolean delete() {
        return SkeletonSystemImpl.deleteDirectory(wspace);
    }

    public File getWSpaceDir() {
        return wspace;
    }

    /* ************************************
     *  PUBLIC INTERFACE WSPACE METHODS
     ***************************************/
    public File copyInto(File src) throws IOException {
        ProxyFile dst = new ProxyFile(wspace, src.getName());
        SkeletonSystemImpl.copyFile(src, dst);

        return dst;
    }

    public File copyInto(URL src) throws IOException {
        ProxyFile dst = new ProxyFile(wspace, new File(src.getPath()).getName());
        SkeletonSystemImpl.download(src, dst);

        return dst;
    }

    public File newFile(String path) {
        return new ProxyFile(wspace, new File(path));
    }

    public File newFile(File path) {
        return new ProxyFile(wspace, path);
    }

    public File[] listFiles() {
        File[] list = wspace.listFiles();

        return toProxyFile(list);
    }

    public File[] listFiles(FileFilter filter) {
        File[] list = wspace.listFiles(filter);

        return toProxyFile(list);
    }

    private ProxyFile[] toProxyFile(File[] list) {
        ProxyFile[] prox = new ProxyFile[list.length];

        for (int i = 0; i < list.length; i++) {
            prox[i] = new ProxyFile(wspace, list[i].getName());
        }

        return prox;
    }

    public boolean exists(File path) {
        return (new File(wspace, path.getPath())).exists();
    }
}
