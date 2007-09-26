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
package org.objectweb.proactive.extensions.calcium.environment.multithreaded;

import java.io.File;
import java.io.IOException;

import org.objectweb.proactive.extensions.calcium.environment.FileServer;
import org.objectweb.proactive.extensions.calcium.environment.RemoteFile;
import org.objectweb.proactive.extensions.calcium.system.SkeletonSystemImpl;


public class LocalFile implements RemoteFile {
    File location;
    long fileId;
    long length;
    String md5sum;

    public LocalFile(File location, long fileId, long length, String md5sum) {
        this.location = location;
        this.fileId = fileId;
        this.length = length;
        this.md5sum = md5sum;
    }

    public void countReference(FileServer fserver) {
        fserver.increaseReference(fileId);
    }

    public void discountReference(FileServer fserver) {
        fserver.discountReference(fileId);
    }

    public String getMD5Hash() {
        return md5sum;
    }

    public long length() {
        return length;
    }

    public boolean saveAs(File f) throws IOException {
        return SkeletonSystemImpl.copyFile(location, f);
    }
}
