/*
 * ################################################################
 *
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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.ext.filessplitmerge.util;

import java.io.File;
import java.io.FileFilter;
import java.util.Iterator;
import java.util.List;


public class FilesFilter implements FileFilter {

    private List<String> extenstionsToExclude;
    private List<String> fileNamesToExclude;
    private List<String> fileNamesToInclude;

    public FilesFilter(List<String> _excludeExt, List<String> _fileNamestoExclude,
            List<String> _fileNamesToInclude) {
        this.extenstionsToExclude = _excludeExt;
        this.fileNamesToExclude = _fileNamestoExclude;
        this.fileNamesToInclude = _fileNamesToInclude;
    }

    public boolean accept(File pathname) {

        //Files to include: 
        if (fileNamesToInclude != null) {
            Iterator<String> it = fileNamesToInclude.iterator();
            while (it.hasNext()) {
                String fileName = it.next();
                if (pathname.getName().equals(fileName)) {
                    return true;
                }
            }//while

        } //if extentions!=null

        //Extensions to exclude

        if (extenstionsToExclude != null) {
            Iterator<String> it = extenstionsToExclude.iterator();
            while (it.hasNext()) {
                String ext = it.next();
                if (pathname.getName().endsWith("." + ext)) {
                    return false;
                }
            }
        } //if extentions!=null

        //files to exclude

        if (fileNamesToExclude != null) {
            Iterator<String> it = fileNamesToExclude.iterator();
            while (it.hasNext()) {
                String fileName = it.next();
                if (pathname.getName().equals(fileName)) {
                    return false;
                }
            }
        } //if fileNames!=null

        return true;

    }

}
