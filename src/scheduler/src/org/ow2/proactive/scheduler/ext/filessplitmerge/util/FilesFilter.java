//============================================================================
// Name        : ProActive Files Split-Merge Framework
// Author      : Emil Salageanu, ActiveEon team
// Version     : 0.1
// Copyright   : Copyright ActiveEon 2008-2009, Tous Droits Réservés (All Rights Reserved)
// Description : Framework for building distribution layers for native applications
//================================================================================

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
