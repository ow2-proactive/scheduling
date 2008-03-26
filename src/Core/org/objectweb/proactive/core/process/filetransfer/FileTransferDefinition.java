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

import java.io.Serializable;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * This class stores the FileTransfer Definition abstraction
 *
 * @author The ProActive Team
 * @version 1.0,  2005/08/26
 * @since   ProActive 2.3
 */
public class FileTransferDefinition implements Serializable {
    final protected static Logger logger = ProActiveLogger.getLogger(Loggers.DEPLOYMENT_FILETRANSFER);
    private ArrayList<FileDescription> all;
    private ArrayList<FileDescription> files;
    private ArrayList<FileDescription> homonymousFiles;
    private ArrayList<FileDescription> heteronymousFiles;
    private ArrayList<DirectoryDescription> directories;
    private ArrayList<DirectoryDescription> homonymousDirs;
    private ArrayList<DirectoryDescription> heteronymousDirs;
    String name;

    public FileTransferDefinition(String name) {
        all = new ArrayList<FileDescription>();
        files = new ArrayList<FileDescription>();
        directories = new ArrayList<DirectoryDescription>();
        homonymousDirs = new ArrayList<DirectoryDescription>();
        homonymousFiles = new ArrayList<FileDescription>();
        heteronymousDirs = new ArrayList<DirectoryDescription>();
        heteronymousFiles = new ArrayList<FileDescription>();

        this.name = name;
    }

    public FileTransferDefinition() {
        this("");
    }

    public void addFile(String srcname, String destname) {
        if ((srcname == null) || (srcname.length() < 0) || (destname == null) || (destname.length() < 0)) {
            logger.debug("Discarding empty or null filename");

            return;
        }

        FileDescription newFile = new FileDescription(srcname, destname);

        if (newFile.isHomonymous()) {
            homonymousFiles.add(newFile);
        } else {
            heteronymousFiles.add(newFile);
        }

        files.add(newFile);
        all.add(newFile);
    }

    public void addDir(String srcname, String destname, String includes, String excludes) {
        if ((srcname == null) || (srcname.length() < 0) || (destname == null) || (destname.length() < 0)) {
            logger.debug("Discarding empty or null directory name");

            return;
        }

        DirectoryDescription newDir = new DirectoryDescription(srcname, destname, includes, excludes);

        if (newDir.isHomonymous()) {
            homonymousDirs.add(newDir);
        } else {
            heteronymousDirs.add(newDir);
        }

        directories.add(newDir);
        all.add(newDir);
    }

    public void addDir(String srcname, String destname) {
        addDir(srcname, destname, "", "");
    }

    /**
     * @return All Files and Directories
     */
    public FileDescription[] getAll() {
        return all.toArray(new FileDescription[0]);
    }

    public FileDescription[] getAllFiles() {
        return files.toArray(new FileDescription[0]);
    }

    public DirectoryDescription[] getAllDirectories() {
        return directories.toArray(new DirectoryDescription[0]);
    }

    public FileDescription[] getHomonymousFile() {
        return homonymousFiles.toArray(new FileDescription[0]);
    }

    public DirectoryDescription[] getHomonymousDir() {
        return homonymousDirs.toArray(new DirectoryDescription[0]);
    }

    public FileDescription[] getHeteronymousFile() {
        return heteronymousFiles.toArray(new FileDescription[0]);
    }

    public DirectoryDescription[] getHeteronymousDir() {
        return heteronymousDirs.toArray(new DirectoryDescription[0]);
    }

    public String getId() {
        return name;
    }

    public void setId(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < files.size(); i++)
            sb.append(files.get(i)).append("\n");

        for (int i = 0; i < directories.size(); i++)
            sb.append(directories.get(i)).append("\n");

        while ((sb.length() > 0) && (sb.charAt(sb.length() - 1) == '\n'))
            sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }

    public boolean equals(FileTransferDefinition fileTransfer) {
        return (name.length() > 0) && (fileTransfer.getId().length() > 0) &&
            name.equalsIgnoreCase(fileTransfer.getId());
    }

    public boolean isEmpty() {
        return ((files.size() == 0) && (directories.size() == 0));
    }

    public class FileDescription implements Serializable {
        String srcName;
        String destName;

        public FileDescription(String srcname, String destname) {
            //Trim white spaces
            srcname = srcname.trim();
            destname = destname.trim();

            /* Delete ending slashes
             * Note: This can give trouble when copying to the root "/" !!!
             */
            if (srcname.endsWith("/") || srcname.endsWith("\\")) {
                this.srcName = srcname.substring(0, srcname.length() - 1);
            } else {
                this.srcName = srcname;
            }

            if (destname.endsWith("/") || destname.endsWith("\\")) {
                this.destName = destname.substring(0, destname.length() - 1);
            } else {
                this.destName = destname;
            }
        }

        @Override
        public String toString() {
            return "src:" + srcName + " dest:" + destName;
        }

        public boolean isHomonymous() {
            return srcName.equals(destName) || (destName.length() <= 0);
        }

        /**
         * @return Returns the destname.
         */
        public String getDestName() {
            return destName;
        }

        /**
         * @return Returns the srcname.
         */
        public String getSrcName() {
            return srcName;
        }

        public boolean isDir() {
            return false;
        }
    }

    public class DirectoryDescription extends FileDescription {
        String includes;
        String excludes;

        public DirectoryDescription(String srcname, String destname, String includes, String excludes) {
            super(srcname, destname);

            this.includes = includes;
            this.excludes = excludes;
        }

        @Override
        public String toString() {
            return super.toString() + " inc:" + includes + " exc:" + excludes;
        }

        @Override
        public boolean isDir() {
            return true;
        }
    }
}
