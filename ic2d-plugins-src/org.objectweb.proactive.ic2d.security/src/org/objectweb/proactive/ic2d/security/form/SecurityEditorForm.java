package org.objectweb.proactive.ic2d.security.form;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.FormToolkit;


public class SecurityEditorForm extends FormEditor {
    public SecurityEditorForm() {
    }

    @Override
    protected FormToolkit createToolkit(Display display) {
        // Create a toolkit that shares colors between editors.
        return new FormToolkit(display);
    }

    @Override
    protected void addPages() {
        try {
            addPage(new CertificateForm(this, null, null));
        } catch (PartInitException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
    }

    @Override
    public void doSaveAs() {
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }
}
