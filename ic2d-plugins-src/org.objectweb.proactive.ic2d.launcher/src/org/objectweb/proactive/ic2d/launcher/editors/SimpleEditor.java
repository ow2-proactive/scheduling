/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.ic2d.launcher.editors;

import org.eclipse.ui.texteditor.AbstractTextEditor;


/**
 * A simple text editor.
 *
 * @see org.eclipse.ui.examples.rcp.texteditor.editors.SimpleDocumentProvider
 * @since 3.0
 */
public class SimpleEditor extends AbstractTextEditor {
    //
    // -- PUBLIC METHODS ---------------------------------------------
    //
    public SimpleEditor() {
        super();
        // make sure we inherit all the text editing commands (delete line etc).
        setKeyBindingScopes(new String[] { "org.eclipse.ui.textEditorScope" }); //$NON-NLS-1$
        internal_init();
    }

    //
    // -- PROTECTED METHODS ---------------------------------------------
    //

    /**
     * Initializes the document provider and source viewer configuration.
     * Called by the constructor. Subclasses may replace this method.
     */
    protected void internal_init() {
        configureInsertMode(SMART_INSERT, false);
        setDocumentProvider(new SimpleDocumentProvider());
    }
}
