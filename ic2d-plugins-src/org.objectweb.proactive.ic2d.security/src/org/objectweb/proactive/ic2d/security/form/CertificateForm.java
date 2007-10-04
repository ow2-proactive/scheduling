package org.objectweb.proactive.ic2d.security.form;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;


public class CertificateForm extends FormPage {
    private FormToolkit toolkit;
    private ScrolledForm form;

    /**
     * The constructor.
     */
    public CertificateForm(FormEditor editor, String id, String title) {
        super(editor, "certificate", "Certificates"); //$NON-NLS-1$ //$NON-NLS-2$
                                                      // TODO Auto-generated constructor stub
    }

    @Override
    protected void createFormContent(IManagedForm managedForm) {
        ScrolledForm form = managedForm.getForm();
        FormToolkit toolkit = managedForm.getToolkit();
        form.setText("SecondPage.title"); //$NON-NLS-1$
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        form.getBody().setLayout(layout);
        createTableSection(form, toolkit, "SecondPage.firstSection"); //$NON-NLS-1$
        createTableSection(form, toolkit, "SecondPage.secondSection"); //$NON-NLS-1$
    }

    private void createTableSection(final ScrolledForm form,
        FormToolkit toolkit, String title) {
        Section section = toolkit.createSection(form.getBody(),
                Section.TWISTIE | Section.DESCRIPTION);
        section.setActiveToggleColor(toolkit.getHyperlinkGroup()
                                            .getActiveForeground());
        section.setToggleColor(toolkit.getColors().getColor(FormColors.SEPARATOR));
        toolkit.createCompositeSeparator(section);
        Composite client = toolkit.createComposite(section, SWT.WRAP);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;

        client.setLayout(layout);
        Table t = toolkit.createTable(client, SWT.NULL);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = 200;
        gd.widthHint = 100;
        t.setLayoutData(gd);
        toolkit.paintBordersFor(client);
        Button b = toolkit.createButton(client, "SecondPage.add", SWT.PUSH); //$NON-NLS-1$
        gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
        b.setLayoutData(gd);
        section.setText(title);
        section.setDescription("SecondPage.desc"); //$NON-NLS-1$
        section.setClient(client);
        section.setExpanded(true);
        section.addExpansionListener(new ExpansionAdapter() {
                @Override
                public void expansionStateChanged(ExpansionEvent e) {
                    form.reflow(false);
                }
            });
        gd = new GridData(GridData.FILL_BOTH);
        section.setLayoutData(gd);
    }
}
