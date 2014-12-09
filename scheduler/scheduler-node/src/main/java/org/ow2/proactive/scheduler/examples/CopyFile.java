/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 *  * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.examples;

import java.io.Serializable;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.Selectors;
import org.objectweb.proactive.extensions.dataspaces.api.FileSelector;
import org.objectweb.proactive.extensions.dataspaces.vfs.adapter.VFSFileObjectAdapter;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;


/**
 * CopyFile, copy a single input File to an output File
 *
 * A wildcard can be used if parts of the names of the input file is not known
 *
 * @author The ProActive Team
 */
public class CopyFile extends JavaExecutable {

    protected String inputFile;
    protected String outputFile;

    @Override
    public Serializable execute(TaskResult... results) throws Throwable {
        if (inputFile.contains("*")) {
            inputFile = inputFile.replace("*", ".*").replace("?", ".");
            FileObject space = ((VFSFileObjectAdapter) getLocalSpace()).getAdaptee();

            FileObject[] lfo = space.findFiles(new org.apache.commons.vfs2.FileSelector() {
                @Override
                public boolean includeFile(FileSelectInfo fileSelectInfo) throws Exception {
                    String name = fileSelectInfo.getFile().getName().getBaseName();
                    boolean answer = name.matches(inputFile);
                    return answer;
                }

                @Override
                public boolean traverseDescendents(FileSelectInfo fileSelectInfo) throws Exception {
                    return true;
                }
            });
            if (lfo.length == 0) {
                throw new IllegalStateException("No input file match the pattern : " + inputFile);
            }
            if (lfo.length > 1) {
                getOut().println("Warning more than one file matched the pattern : " + inputFile);
            }
            ((VFSFileObjectAdapter) super.getLocalFile(outputFile)).getAdaptee().copyFrom(lfo[0],
                    Selectors.SELECT_SELF);
            getOut().println("Copied " + lfo[0].getURL() + " to  " +
                super.getLocalFile(outputFile).getRealURI());
        } else {
            super.getLocalFile(outputFile).copyFrom(super.getLocalFile(inputFile), FileSelector.SELECT_SELF);
            getOut().println("Copied " + super.getLocalFile(inputFile).getRealURI() + " to  " +
                super.getLocalFile(outputFile).getRealURI());
        }

        return true;
    }
}
