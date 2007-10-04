/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.objectweb.proactive.ic2d.security;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;


/**
 * @see IWorkbenchWindowActionDelegate
 */
public abstract class OpenFormEditorAction
    implements IWorkbenchWindowActionDelegate {
    private IWorkbenchWindow window;

    /*
     *
     */
    protected void openEditor(String inputName, String editorId) {
        openEditor(new FormEditorInput(inputName), editorId);
    }

    protected void openEditor(IEditorInput input, String editorId) {
        IWorkbenchPage page = this.window.getActivePage();
        try {
            page.openEditor(input, editorId);
        } catch (PartInitException e) {
            System.out.println(e);
        }
    }

    protected IWorkbenchWindow getWindow() {
        return this.window;
    }

    /**
     * @see IWorkbenchWindowActionDelegate#selectionChanged
     */
    public void selectionChanged(IAction action, ISelection selection) {
    }

    /**
     * @see IWorkbenchWindowActionDelegate#dispose
     */
    public void dispose() {
    }

    /**
     * @see IWorkbenchWindowActionDelegate#init
     */
    public void init(IWorkbenchWindow window) {
        this.window = window;
    }
}
