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
package org.objectweb.proactive.core.process.filetransfer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.process.filetransfer.FileTransferDefinition.DirectoryDescription;
import org.objectweb.proactive.core.process.filetransfer.FileTransferDefinition.FileDescription;
import org.objectweb.proactive.core.process.filetransfer.FileTransferWorkShop.StructureInformation;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * AbstractCopyProtocol implements default behaviour of methods
 * in CopyProtocol
 *
 * @author  ProActive Team
 * @version 1.0,  2005/08/26
 * @since   ProActive 2.3
 */
public abstract class AbstractCopyProtocol implements CopyProtocol {
    final protected static Logger logger = ProActiveLogger.getLogger(Loggers.DEPLOYMENT_FILETRANSFER);
    protected boolean isDefaultProtocol = false;
    protected String name;
    protected FileTransferDefinition[] fileTransfer;
    protected StructureInformation srcInfoParams;
    protected StructureInformation dstInfoParams;
    protected boolean closeStream = false;

    public AbstractCopyProtocol(String name) {
        this.name = name;
    }

    public String getProtocolName() {
        return name;
    }

    public void setProtocolName(String name) {
        this.name = name;
    }

    public boolean isDefaultProtocol() {
        return isDefaultProtocol;
    }

    public void setDefaultProtocol(boolean isDefaultProtocol) {
        this.isDefaultProtocol = isDefaultProtocol;
    }

    public void setFileTransferDefinitions(
        FileTransferDefinition[] fileTransfer) {
        this.fileTransfer = fileTransfer;
    }

    public void setSrcInfo(
        FileTransferWorkShop.StructureInformation srcInfoParams) {
        this.srcInfoParams = srcInfoParams;
    }

    public void setDstInfo(
        FileTransferWorkShop.StructureInformation dstInfoParams) {
        this.dstInfoParams = dstInfoParams;
    }

    /**
     * This method should only be modified in the
     * dummy copy protocol class: DummyCopyProtocol
     */
    public boolean isDummyProtocol() {
        return false;
    }

    /**
     * Note that for this only the filename is
     * considered, and not the filepath.
     * @return an array with the reference to all
     * the Files that are homonymous. That
     * is to say, with the same source and
     * destination filename.
     */
    protected FileDescription[] getHomonymousFiles() {
        ArrayList<FileDescription> aList = new ArrayList<FileDescription>();

        for (int i = 0; i < fileTransfer.length; i++) {
            FileDescription[] fd = fileTransfer[i].getHomonymousFile();
            for (int j = 0; j < fd.length; j++)
                aList.add(fd[j]);
        }

        return aList.toArray(new FileDescription[0]);
    }

    /**
     * Note that for this only the filename is
     * considered, and not the filepath.
     * @return an array with the reference to all
     * the Files that are heteronymous. That
     * is to say, with different src and destination
     * filename.
     */
    protected FileDescription[] getHeteronymousFiles() {
        ArrayList<FileDescription> aList = new ArrayList<FileDescription>();

        for (int i = 0; i < fileTransfer.length; i++) {
            FileDescription[] fd = fileTransfer[i].getHeteronymousFile();
            for (int j = 0; j < fd.length; j++)
                aList.add(fd[j]);
        }

        return aList.toArray(new FileDescription[0]);
    }

    /**
     * Note that for this only the dirname is
     * considered, and not the dirpath.
     * @return an array with the reference to all
     * the Dirs that are homonymous. That
     * is to say, with the same source and
     * destination dirname.
     */
    protected DirectoryDescription[] getHomonymousDirs() {
        ArrayList<FileDescription> aList = new ArrayList<FileDescription>();

        for (int i = 0; i < fileTransfer.length; i++) {
            FileDescription[] fd = fileTransfer[i].getHomonymousDir();
            for (int j = 0; j < fd.length; j++)
                aList.add(fd[j]);
        }

        return aList.toArray(new DirectoryDescription[0]);
    }

    /**
     *
     * Note that for this only the dirname is
     * considered, and not the dirpath.
     * @return an array with the reference to all
     * the Directories that are heteronymous. That
     * is to say, with different src and destination
     * dirname.
     */
    protected DirectoryDescription[] getHeteronymousDirs() {
        ArrayList<DirectoryDescription> aList = new ArrayList<DirectoryDescription>();

        for (int i = 0; i < fileTransfer.length; i++) {
            DirectoryDescription[] fd = fileTransfer[i].getHeteronymousDir();
            for (int j = 0; j < fd.length; j++)
                aList.add(fd[j]);
        }

        return aList.toArray(new DirectoryDescription[0]);
    }

    protected FileDescription[] getHeteronymousAll() {
        FileDescription[] files = getHeteronymousFiles();
        FileDescription[] dirs = getHeteronymousDirs();

        FileDescription[] all = new FileDescription[files.length + dirs.length];

        for (int i = 0; i < files.length; i++) {
            all[i] = files[i];
        }

        for (int i = 0; i < dirs.length; i++) {
            all[files.length + i] = dirs[i];
        }

        return all;
    }

    protected FileDescription[] getHomonymousAll() {
        FileDescription[] files = getHomonymousFiles();
        FileDescription[] dirs = getHomonymousDirs();

        FileDescription[] all = new FileDescription[files.length + dirs.length];

        for (int i = 0; i < files.length; i++) {
            all[i] = files[i];
        }

        for (int i = 0; i < dirs.length; i++) {
            all[files.length + i] = dirs[i];
        }

        return all;
    }

    /**
     * Note: this method will block if the stream is not closed!!!
     * @param in An input stream
     * @return the content of the stream
     */
    protected String getErrorMessage(InputStream in) {
        StringBuilder sb = new StringBuilder();
        byte[] b = new byte[1000];
        int read;
        while (true) {
            try {
                read = in.read(b);
                if (read < 0) {
                    break;
                }

                sb.append(new String(b, 0, read));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        return sb.toString();
    }
}
