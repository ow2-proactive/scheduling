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
package org.objectweb.proactive.extensions.calcium.system.files;

import java.io.IOException;
import java.util.IdentityHashMap;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.calcium.environment.FileServerClient;
import org.objectweb.proactive.extensions.calcium.stateness.Handler;
import org.objectweb.proactive.extensions.calcium.system.PrefetchFilesMatching;
import org.objectweb.proactive.extensions.calcium.system.ProxyFile;
import org.objectweb.proactive.extensions.calcium.system.WSpaceImpl;
import org.objectweb.proactive.extensions.calcium.task.Task;


class HandlerPreHybridProxyFile implements Handler<ProxyFile> {
    static Logger logger = ProActiveLogger.getLogger(Loggers.SKELETONS_SYSTEM);
    FileServerClient fserver;
    WSpaceImpl wspace;
    Pattern fetchEager;
    DataFetchMatcher fetchMatcher;
    IdentityHashMap<ProxyFile, ProxyFile> allFiles;

    public HandlerPreHybridProxyFile(Task<?> task, FileServerClient fserver, WSpaceImpl wspace,
            IdentityHashMap<ProxyFile, ProxyFile> allFiles) {
        PrefetchFilesMatching annotation = task.getStack().get(0).getPrefetchFilesAnnotation();
        fetchMatcher = null;

        if (annotation != null) {
            fetchMatcher = new DataFetchMatcher(annotation.name(), annotation.sizeBiggerThan(), annotation
                    .sizeSmallerThan());
        }

        this.allFiles = allFiles;
        this.fserver = fserver;
        this.wspace = wspace;
    }

    public ProxyFile transform(ProxyFile pfile) throws IOException {
        pfile.refCBefore++; //Count the reference

        allFiles.put(pfile, pfile); //Put the file in the global list

        pfile.setWSpace(fserver, wspace.getWSpaceDir()); //Set the current wspace

        if ((fetchMatcher != null) && fetchMatcher.matches(pfile)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Pre-fetching annotated file: " + pfile.getWSpaceFile() + "/" + pfile.getName());
            }

            pfile.saveRemoteDataInWSpace(); //Set current & download data
        }

        return pfile;
    }

    public boolean matches(Object o) {
        return ProxyFile.class.isAssignableFrom(o.getClass());
    }

    class DataFetchMatcher {
        long sizeBT;
        long sizeST;
        String nameRegEx;

        public DataFetchMatcher(String nameRegEx, long sizeBT, long sizeST) {
            super();
            this.sizeBT = sizeBT;
            this.sizeST = sizeST;
            this.nameRegEx = nameRegEx;
        }

        public boolean matches(ProxyFile f) {
            //logger.debug("$$$$ "+f.length() +">="+ this.sizeBT + " "+ f.length() +"<="+ this.sizeST + " "+ f.getName() +"==" +nameRegEx);
            boolean res = (f.length() >= this.sizeBT) || (f.length() <= this.sizeST) ||
                (f.getName().matches(nameRegEx));

            if (res && logger.isDebugEnabled()) {
                logger.debug("Annotation matched for file: " + f.getName());
            }

            return res;
        }
    }
}
