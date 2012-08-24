package org.ow2.proactive.scheduler.common.util.dsclient;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.vfs.FileSelectInfo;
import org.apache.commons.vfs.FileSelector;
import org.apache.log4j.Logger;

import org.objectweb.proactive.extensions.dataspaces.vfs.selector.fast.FastFileSelector;
import org.objectweb.proactive.extensions.dataspaces.vfs.selector.fast.SelectorUtils;


/**
 * A VFS file selector meant to behave like {@link FastFileSelector}: given a
 * list of include patterns and a list of excludes patterns, this selector will
 * respond true if the file matches one of the includes patterns and does not
 * match any of the excludes patterns. In order to perform the match operation,
 * the base folder uri is extracted from the file uri and the remaining is
 * matched against the patterns. The matching operation is performed by
 * {@link SelectorUtils#matchPath(String, String)}
 *
 * @author esalagea
 *
 */
public class DSFileSelector implements FileSelector {

    public static final Logger logger_util = Logger.getLogger(DSFileSelector.class);

    Set<String> includes = new HashSet<String>();
    Set<String> excludes = new HashSet<String>();

    public DSFileSelector() {

    }

    @Override
    public boolean includeFile(FileSelectInfo fileInfo) throws Exception {

        String buri = fileInfo.getBaseFolder().getURL().toString();
        String furi = fileInfo.getFile().getURL().toString();
        String name = furi.replaceFirst(buri + "/?", "");

        // logger_util.debug("Checking file " + name + "("+ furi+")");

        if (isIncluded(name)) {
            if (!isExcluded(name)) {
                logger_util.debug("File " + furi + " selected for copy.");
                return true;
            }
        }
        return false;
    }

    protected boolean isExcluded(String name) {
        if (excludes.contains(name))
            return true;

        for (String pattern : excludes) {
            if (SelectorUtils.matchPath(pattern, name)) {
                logger_util.debug("File " + name + " matches an exclude pattern");
                return true;
            }
        }
        return false;
    }

    protected boolean isIncluded(String name) {
        if (includes.contains(name))
            return true;

        for (String pattern : includes) {
            if (SelectorUtils.matchPath(pattern, name)) {
                return true;
            }
        }

        logger_util.debug("File " + name + " does not match any of the include patterns");

        return false;
    }

    @Override
    public boolean traverseDescendents(FileSelectInfo fileInfo) throws Exception {
        //
        return true;
    }

    public void addIncludes(Collection<String> files) {
        if (files != null)
            includes.addAll(files);
    }

    public void addExcludes(Collection<String> files) {
        if (files != null)
            excludes.addAll(files);
    }

    public Set<String> getIncludes() {
        return new HashSet<String>(includes);
    }

    public Set<String> getExcludes() {
        return new HashSet<String>(excludes);
    }

    public void setExcludes(Set<String> excludes) {
        this.excludes = excludes;
    }

}
