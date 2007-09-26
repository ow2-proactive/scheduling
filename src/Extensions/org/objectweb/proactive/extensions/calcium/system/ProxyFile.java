/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed, Concurrent
 * computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis Contact:
 * proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Initial developer(s): The ProActive Team
 * http://www.inria.fr/oasis/ProActive/contacts.html Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extensions.calcium.system;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.calcium.environment.FileServer;
import org.objectweb.proactive.extensions.calcium.environment.RemoteFile;


/**
 * This class is a Proxy for the real File class.
 *
 * @author The ProActive Team (mleyton)
 */
public class ProxyFile extends File {
    static Logger logger = ProActiveLogger.getLogger(Loggers.SKELETONS_STRUCTURE);
    File wspace;
    File relative;
    File current; //current = wspace+"/"+relative
    RemoteFile remote;
    long lastmodified;
    public boolean marked;

    public ProxyFile(File wspace, File relative) {
        super(relative.getName());
        this.relative = relative;
        this.remote = null;
        setWSpace(wspace);
        this.lastmodified = 0;
    }

    public ProxyFile(File wspace, String name) {
        this(wspace, new File(name));
    }

    public void setWSpace(File wspace) {
        this.wspace = wspace;
        this.current = new File(wspace, relative.getPath());
    }

    public void store(FileServer fserver) throws IOException {
        this.remote = fserver.store(current);
        this.lastmodified = current.lastModified();
    }

    public void saveRemoteFileInWSpace() throws IOException {
        int i = 1;
        while (current.exists()) {
            current = new File(wspace, relative.getPath() + "-" + i);
        }
        remote.saveAs(current);
        lastmodified = current.lastModified();
    }

    public File getCurrent() {
        return current;
    }

    public boolean isRemotelyStored() {
        return remote != null;
    }

    public boolean hasBeenModified() {

        /* TODO improve by:
             *
             *  1. Also considering the hashcode.
             *  2. Keeping track of lastModified access.
             */
        return lastmodified != current.lastModified();
    }

    public void dereference(FileServer fserver) {
        //if(!isRemotelyStored()) return; //nothing to do
        remote.discountReference(fserver);

        //make this file as new
        remote = null;
        lastmodified = 0;
    }

    public void countReference(FileServer fserver) {
        remote.countReference(fserver);
    }

    /*
     *  BEGIN EXTENDED FILE METHODS
     *
     */
    public String getName() {
        return current.getName();
    }

    public String getParent() {
        return current.getParent();
    }

    public File getParentFile() {
        return current.getParentFile();
    }

    public String getPath() {
        return current.getPath();
    }

    public boolean isAbsolute() {
        return current.isAbsolute();
    }

    public String getAbsolutePath() {
        return current.getAbsolutePath();
    }

    public File getAbsoluteFile() {
        return current.getAbsoluteFile();
    }

    public String getCanonicalPath() throws IOException {
        return current.getCanonicalPath();
    }

    public File getCanonicalFile() throws IOException {
        return current.getCanonicalFile();
    }

    public URL toURL() throws MalformedURLException {
        return current.toURL();
    }

    public URI toURI() {
        return current.toURI();
    }

    public boolean canRead() {
        return current.canRead();
    }

    public boolean canWrite() {
        return current.canWrite();
    }

    public boolean exists() {
        return current.exists();
    }

    public boolean isDirectory() {
        return current.isDirectory();
    }

    public boolean isFile() {
        return current.isFile();
    }

    public boolean isHidden() {
        return current.isHidden();
    }

    public long lastModified() {
        return current.lastModified();
    }

    public long length() {
        return current.length();
    }

    public boolean createNewFile() throws IOException {
        return current.createNewFile();
    }

    public boolean delete() {
        return current.delete();
    }

    public void deleteOnExit() {
        current.deleteOnExit();
    }

    public String[] list() {
        return current.list();
    }

    public String[] list(FilenameFilter filter) {
        return current.list();
    }

    public File[] listFiles() {
        return current.listFiles();
    }

    public File[] listFiles(FilenameFilter filter) {
        return current.listFiles(filter);
    }

    public File[] listFiles(FileFilter filter) {
        return current.listFiles(filter);
    }

    public boolean mkdir() {
        return current.mkdir();
    }

    public boolean mkdirs() {
        return current.mkdirs();
    }

    public boolean renameTo(File dest) {
        boolean res = current.renameTo(new File(wspace, dest.getPath()));

        if (res) {
            relative = dest;
        }

        return res;
    }

    public boolean setLastModified(long time) {
        return current.setLastModified(time);
    }

    public boolean setReadOnly() {
        return current.setReadOnly();
    }

    public int compareTo(File pathname) {
        return current.compareTo(pathname);
    }

    public boolean equals(Object obj) {
        return current.equals(obj);
    }

    public int hashCode() {
        return current.hashCode();
    }

    public String toString() {
        return current.toString();
    }

    public static void main(String[] args) throws Exception {
        WSpaceImpl wspace = new WSpaceImpl(new File("/tmp/calcium"));
        File f = wspace.copyInto(new File("/home/mleyton/IMG00070.jpg"));
    }
}
