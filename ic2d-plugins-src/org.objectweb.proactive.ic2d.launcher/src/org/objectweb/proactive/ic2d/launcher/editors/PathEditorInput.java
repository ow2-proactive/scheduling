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
package org.objectweb.proactive.ic2d.launcher.editors;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.PlatformUI;


/**
 * EditorInput that stores a path.
 */
public class PathEditorInput implements IPathEditorInput {
    private IPath fPath;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //

    /**
     * Creates an editor input based of the given file resource.
     * @param path the file
     */
    public PathEditorInput(IPath path) {
        if (path == null) {
            throw new IllegalArgumentException();
        }
        this.fPath = path;
    }

    //
    // -- PUBLIC METHODS ---------------------------------------------
    //

    /*
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return fPath.hashCode();
    }

    /*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PathEditorInput)) {
            return false;
        }
        PathEditorInput other = (PathEditorInput) obj;
        return fPath.equals(other.fPath);
    }

    /*
     * @see org.eclipse.ui.IEditorInput#exists()
     */
    public boolean exists() {
        return fPath.toFile().exists();
    }

    /*
     * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
     */
    public ImageDescriptor getImageDescriptor() {
        return PlatformUI.getWorkbench().getEditorRegistry().getImageDescriptor(fPath.toString());
    }

    /*
     * @see org.eclipse.ui.IEditorInput#getName()
     */
    public String getName() {
        return fPath.toString();
    }

    /*
     * @see org.eclipse.ui.IEditorInput#getToolTipText()
     */
    public String getToolTipText() {
        return fPath.makeRelative().toOSString();
    }

    /*
     * @see org.eclipse.ui.IPathEditorInput#getPath()
     */
    public IPath getPath() {
        return fPath;
    }

    /*
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        return null;
    }

    /*
     * @see org.eclipse.ui.IEditorInput#getPersistable()
     */
    public IPersistableElement getPersistable() {
        // no persistence
        return null;
    }
}
