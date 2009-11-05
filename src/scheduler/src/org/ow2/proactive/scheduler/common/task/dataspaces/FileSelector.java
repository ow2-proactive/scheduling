/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.task.dataspaces;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Type;
import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Specify a set of files to include and a set of files to exclude among the include set.
 * This allows accurate files targeting.
 *
 * The idea is simple. A given directory is recursively scanned for all files
 * and directories. Each file/directory is matched against a set of selectors,
 * including special support for matching against filenames with include and
 * and exclude patterns. Only files/directories which match at least one
 * pattern of the include pattern list or other file selector, and don't match
 * any pattern of the exclude pattern list or fail to match against a required
 * selector will be placed in the list of files/directories found.
 * <p>
 * When no list of include patterns is supplied, "**" will be used, which
 * means that everything will be matched. When no list of exclude patterns is
 * supplied, an empty list is used, such that nothing will be excluded. When
 * no selectors are supplied, none are applied.
 * <p>
 * The filename pattern matching is done as follows:
 * The name to be matched is split up in path segments. A path segment is the
 * name of a directory or file, which is bounded by
 * <code>File.separator</code> ('/' under UNIX, '\' under Windows).
 * For example, "abc/def/ghi/xyz.java" is split up in the segments "abc",
 * "def","ghi" and "xyz.java".
 * The same is done for the pattern against which should be matched.
 * <p>
 * The segments of the name and the pattern are then matched against each
 * other. When '**' is used for a path segment in the pattern, it matches
 * zero or more path segments of the name.
 * <p>
 * There is a special case regarding the use of <code>File.separator</code>s
 * at the beginning of the pattern and the string to match:<br>
 * When a pattern starts with a <code>File.separator</code>, the string
 * to match must also start with a <code>File.separator</code>.
 * When a pattern does not start with a <code>File.separator</code>, the
 * string to match may not start with a <code>File.separator</code>.
 * When one of these rules is not obeyed, the string will not
 * match.
 * <p>
 * When a name path segment is matched against a pattern path segment, the
 * following special characters can be used:<br>
 * '*' matches zero or more characters<br>
 * '?' matches one character.
 * <p>
 * Examples:
 * <p>
 * "**\*.class" matches all .class files/dirs in a directory tree.
 * <p>
 * "test\a??.java" matches all files/dirs which start with an 'a', then two
 * more characters and then ".java", in a directory called test.
 * <p>
 * "**" matches everything in a directory tree.
 * <p>
 * "**\test\**\XYZ*" matches all files/dirs which start with "XYZ" and where
 * there is a parent directory called test (e.g. "abc\test\def\ghi\XYZ123").
 * <p>
 * Case sensitivity may be turned off if necessary. By default, it is
 * turned on.
 * <p>
 * Example of usage:
 * <pre>
 *   String[] includes = {"**\\*.class"};
 *   String[] excludes = {"modules\\*\\**"};
 *   fs.setIncludes(includes);
 *   fs.setExcludes(excludes);
 *   fs.setCaseSensitive(true);
 *
 * </pre>
 * This example selects .class files, but excludes all
 * files in all proper subdirectories of a directory called "modules".
 * <p>
 * Finally, the selector must be passed to the task inputFiles or outputFiles.
 *
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
@PublicAPI
@Entity
@Table(name = "FILE_SELECTOR")
@AccessType("field")
@Proxy(lazy = false)
public class FileSelector implements Serializable {

    /**  */
    private static final long serialVersionUID = 20;

    @Id
    @GeneratedValue
    protected long hId;

    @Column(name = "INCLUDES", columnDefinition = "BLOB")
    @Type(type = "org.ow2.proactive.scheduler.core.db.schedulerType.CharacterLargeOBject")
    private String[] includes;
    @Column(name = "EXCLUDES", columnDefinition = "BLOB")
    @Type(type = "org.ow2.proactive.scheduler.core.db.schedulerType.CharacterLargeOBject")
    private String[] excludes;
    @Column(name = "CASE_SENSITIVE")
    private boolean caseSensitive = true;

    /**
     * Create a new instance of FileSelector
     */
    public FileSelector() {
    }

    /**
     * Create a new instance of FileSelector using include files pattern
     *
     * @param includes
     */
    public FileSelector(String... includes) {
        this.includes = includes;
    }

    /**
     * Create a new instance of FileSelector using exclude and include files pattern
     *
     * @param includes
     * @param excludes
     */
    public FileSelector(String[] includes, String[] excludes) {
        this.includes = includes;
        this.excludes = excludes;
    }

    /**
     * Get the includes
     *
     * @return the includes
     */
    public String[] getIncludes() {
        return includes;
    }

    /**
     * Set the includes value to the given includes value
     *
     * @param includes the includes to set
     */
    public void setIncludes(String... includes) {
        this.includes = includes;
    }

    /**
     * Get the excludes
     *
     * @return the excludes
     */
    public String[] getExcludes() {
        return excludes;
    }

    /**
     * Set the excludes value to the given excludes value
     *
     * @param excludes the excludes to set
     */
    public void setExcludes(String... excludes) {
        this.excludes = excludes;
    }

    /**
     * Get the caseSensitive
     *
     * @return the caseSensitive
     */
    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    /**
     * Set the caseSensitive value to the given caseSensitive value
     *
     * @param caseSensitive the caseSensitive to set
     */
    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

}
