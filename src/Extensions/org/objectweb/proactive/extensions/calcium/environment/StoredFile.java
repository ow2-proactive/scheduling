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
package org.objectweb.proactive.extensions.calcium.environment;

import java.io.File;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * A <code>StoredFile</code> is a reference on a file stored in a {@link FileServer}.
 *
 * @author The ProActive Team (mleyton)
 */
public class StoredFile implements java.io.Serializable {
    static Logger logger = ProActiveLogger.getLogger(Loggers.SKELETONS_SYSTEM);
    public long fileId;
    public File location;
    public long length;
    public String md5sum;

    public StoredFile(File location, long fileId, long length) {
        this.location = location;
        this.fileId = fileId;
        this.length = length;
        this.md5sum = null;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof StoredFile)) {
            return false;
        }

        return equals((StoredFile) o);
    }

    public boolean equals(StoredFile rf) {
        return (this.fileId == rf.fileId) && this.location.getPath().equals(rf.location.getPath()) &&
            (this.length == rf.length);
    }

    @Override
    public String toString() {
        return "id=" + fileId + " path=" + location + " length=" + length + " md5sum=" + md5sum;
    }
}
