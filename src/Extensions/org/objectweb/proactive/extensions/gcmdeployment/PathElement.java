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
package org.objectweb.proactive.extensions.gcmdeployment;

import java.io.Serializable;
import static org.objectweb.proactive.extensions.gcmdeployment.GCMDeploymentLoggers.GCMD_LOGGER;

import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.commandbuilder.CommandBuilder;
import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.hostinfo.HostInfo;
import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.hostinfo.Tool;
import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.hostinfo.Tools;


public class PathElement implements Cloneable, Serializable {
    protected String relPath;

    public enum PathBase {
        PROACTIVE, HOME, ROOT;
    };

    protected PathBase base;

    public PathElement() {
        this.relPath = null;
        this.base = PathBase.ROOT;
    }

    public PathElement(String relPath) {
        this.relPath = relPath;
        this.base = PathBase.ROOT;
    }

    public PathElement(String relPath, String base) {
        this.relPath = relPath;
        setBase(base);
    }

    public PathElement(String relPath, PathBase base) {
        this.relPath = relPath;
        this.base = base;
    }

    public String getRelPath() {
        return relPath;
    }

    public void setRelPath(String relPath) {
        this.relPath = relPath;
    }

    public PathBase getBase() {
        return base;
    }

    public void setBase(PathBase base) {
        this.base = base;
    }

    public void setBase(String baseString) {
        if (baseString == null) {
            this.base = PathBase.ROOT; // TODO - what should be the default ?
        } else {
            String baseStringCanonical = baseString.trim();
            if (baseStringCanonical.equalsIgnoreCase("proactive")) {
                this.base = PathBase.PROACTIVE;
            } else if (baseStringCanonical.equalsIgnoreCase("home")) {
                this.base = PathBase.HOME;
            } else if (baseStringCanonical.equalsIgnoreCase("root")) {
                this.base = PathBase.ROOT;
            }
        }
    }

    public String getFullPath(HostInfo hostInfo, CommandBuilder commandBuilder) {
        switch (base) {
            case ROOT:
                return relPath;
            case HOME:
                return appendPath(hostInfo.getHomeDirectory(), relPath, hostInfo);
            case PROACTIVE:
                Tool tool = hostInfo.getTool(Tools.PROACTIVE.id);
                if (tool != null) {
                    String ret = appendPath(hostInfo.getHomeDirectory(), tool.getPath(), hostInfo);
                    return appendPath(ret, relPath, hostInfo);
                } else {
                    String bp = commandBuilder.getPath(hostInfo);
                    if (bp != null) {
                        return appendPath(bp, relPath, hostInfo);
                    } else {
                        GCMD_LOGGER
                                .warn(
                                        "Full Path cannot be returned since nor the ProActive tool nor the CommandBuilder base path have been specified",
                                        new IllegalStateException());
                        return null;
                    }
                }
        }

        GCMD_LOGGER.warn("Reached unreachable code", new Exception("Unreachable"));
        return null;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        super.clone();
        PathElement res = new PathElement(this.relPath);
        res.setBase(this.base);
        return res;
    }

    /**
     * Concatenates two path using the file separator given by the hostInfo parameter
     *
     * @param s1 the first path
     * @param s2 the second path
     * @param hostInfo Indicates which file separator to use
     * @return The concatenation of s1 and s2
     */
    static public String appendPath(final String s1, final String s2, final HostInfo hostInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append(s1);

        // If s1 ends with fp remove it
        if (sb.length() > 0) {
            if (sb.charAt(sb.length() - 1) == hostInfo.getOS().fileSeparator()) {
                sb.deleteCharAt(sb.length() - 1);
            }
        }

        // Adds fp
        sb.append(hostInfo.getOS().fileSeparator());

        // If s2 begins with fp remove it
        if (s2.charAt(0) == hostInfo.getOS().fileSeparator()) {
            sb.append(s2.substring(1));
        } else {
            sb.append(s2);
        }

        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((base == null) ? 0 : base.hashCode());
        result = (prime * result) + ((relPath == null) ? 0 : relPath.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PathElement other = (PathElement) obj;
        if (base == null) {
            if (other.base != null) {
                return false;
            }
        } else if (!base.equals(other.base)) {
            return false;
        }
        if (relPath == null) {
            if (other.relPath != null) {
                return false;
            }
        } else if (!relPath.equals(other.relPath)) {
            return false;
        }
        return true;
    }
}
