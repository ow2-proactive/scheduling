package org.objectweb.proactive.ic2d.chronolog.editors.pages.details;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.objectweb.proactive.ic2d.chronolog.data.model.AbstractTypeModel;
import org.objectweb.proactive.ic2d.chronolog.data.model.GroupedNumberBasedTypeModel;
import org.objectweb.proactive.ic2d.chronolog.editors.ChronologDataEditorInput;


public class GroupedNumberBasedDetailsPage extends AbstractDetailsPage<GroupedNumberBasedTypeModel> {

    public static final String PAGE_NAME = "Grouped Data Providers and Charting";

    /**
     * The chart type combo box 
     */
    protected Combo chartChoiceCombo;

    /**
     * @param editorInput
     * @param tableViewer
     */
    public GroupedNumberBasedDetailsPage(final ChronologDataEditorInput editorInput,
            final TableViewer tableViewer) {
        super(editorInput, tableViewer);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.ic2d.chronolog.editors.pages.details.AbstractDetailsPage#createInternalContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public FormToolkit createInternalContents(final Composite parent) {
        final FormToolkit toolkit = super.createInternalContents(parent);

        // Add the standard selection listener to the selection button
        super.addSelectionButtonListener();

        // Add selection button to disable list
        super.editorInput.addControlToDisable(super.selectionButton);

        // Enable the selection button      
        if (!this.editorInput.getCollector().isRunning()) {
            super.selectionButton.setEnabled(true);
        }

        // Add chart related information
        Label label = toolkit.createLabel(parent, "Chart Type:");
        label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));

        // Add a chart type selection combo
        this.chartChoiceCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
        this.chartChoiceCombo.setItems(AbstractTypeModel
                .getAuthorizedChartTypeNames(GroupedNumberBasedTypeModel.authorizedChartTypes));
        super.editorInput.addControlToDisable(this.chartChoiceCombo);
        // Set the default selection
        this.chartChoiceCombo.select(0);
        // Add a selection listener
        this.chartChoiceCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                GroupedNumberBasedDetailsPage.super.type.setChartChoice(chartChoiceCombo.getSelectionIndex());
            }
        });
        return toolkit;
    }

    @Override
    public void run() {
        // Update the attribute description and value
        super.attributeDescriptionText.setText(super.type.getDescription());
        // Ask the model to update its cached value
        super.type.updateProvidedValue();
        final Double[] arr = super.type.getCachedProvidedValue();
        super.attributeValueText.setText("");
        for (final Double d : arr) {
            super.attributeValueText.append(d + "\n");
        }
        // Update the selection state
        super.selectionButton.setSelection(super.type.isUsed());
    }
}