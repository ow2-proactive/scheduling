package org.objectweb.proactive.ic2d.timit.actions;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.objectweb.proactive.ic2d.console.Console;
import org.objectweb.proactive.ic2d.timit.Activator;
import org.objectweb.proactive.ic2d.timit.data.XMLExporter;
import org.objectweb.proactive.ic2d.timit.editparts.SafeSaveDialog;
import org.objectweb.proactive.ic2d.timit.util.ExecuteReport;
import org.objectweb.proactive.ic2d.timit.views.TimItView;


/**
 * This action is used when the user wants to generate an html report.
 * @author The ProActive Team
 * 
 */
public class GenerateReportAndSaveAction extends Action implements IRunnableWithProgress {
    public static final String GENERATE_REPORT_AND_SAVE = "Generate Report and Save to";
    public static final String DEFAULT_XML_OUTPUT_DIRECTORY = "reports/sources";
    public static final String DEFAULT_XML_OUTPUT_FILE_PATH = "timitXmlOutput.xml";

    private final TimItView timItView;
    private final String format;

    public GenerateReportAndSaveAction(final TimItView timItView, final String format) {
        this.timItView = timItView;
        this.format = format;
        super.setId(GENERATE_REPORT_AND_SAVE + " " + format);
        super.setImageDescriptor(ImageDescriptor.createFromURL(FileLocator.find(Activator.getDefault()
                .getBundle(), new Path("icons/generate_report.gif"), null)));
        super.setToolTipText(GENERATE_REPORT_AND_SAVE + " " + format);
        super.setText(format + " report");
        super.setEnabled(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        ProgressMonitorDialog pmd = new ProgressMonitorDialog(this.timItView.getSite().getShell());
        try {
            pmd.run(false, false, this);
            pmd.setBlockOnOpen(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * (non-Javadoc)
     * 
     * @see  org.eclipse.jface.operation.IRunnableWithProgress#run()
     */
    public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        // First use XMLExporter to write data to an xml file
        if (this.timItView.getChartContainer().getChildrenList().size() == 0) {
            Console.getInstance(Activator.CONSOLE_NAME).log(
                    "Cannot generate report. Gather some stats first.");
            return;
        }

        try {
            final SubMonitor progress = SubMonitor.convert(monitor, 100);
            progress.setTaskName("Preparing Report Generation");

            // Resolve the relative path of the xml source file
            URL xmlSourceURL = Activator.getDefault().getBundle().getEntry(
                    DEFAULT_XML_OUTPUT_DIRECTORY + "/" + DEFAULT_XML_OUTPUT_FILE_PATH);
            if (xmlSourceURL == null) {
                xmlSourceURL = (new File(FileLocator.toFileURL(
                        Activator.getDefault().getBundle().getEntry(DEFAULT_XML_OUTPUT_DIRECTORY)).getPath() +
                    "/" + DEFAULT_XML_OUTPUT_FILE_PATH)).toURI().toURL();
            }
            // The path will be used by the report executer
            final String absoluteXmlSourcePath = FileLocator.toFileURL(xmlSourceURL).getPath();
            progress.worked(10); // First part done

            // Export data to xml using absolute path
            final XMLExporter xmlExporter = new XMLExporter(this.timItView.getChartContainer()
                    .getChildrenList());
            xmlExporter.exportTo(absoluteXmlSourcePath);

            // Once the data has been dumped to an xml file ask user for report
            // output path
            final SafeSaveDialog safeSaveDialog = new SafeSaveDialog(this.timItView.getSite().getShell());
            safeSaveDialog.setText("Select Report Filename");

            final String absoluteOutputPath = safeSaveDialog.open();

            // Bad output path
            if ((absoluteOutputPath == null) || "".equals(absoluteOutputPath)) {
                Console.getInstance(Activator.CONSOLE_NAME).log(
                        "Cannot generate report file. Please provide a correct output file path.");
                return;
            }
            progress.worked(20); // Second part done

            long start = System.currentTimeMillis();
            // Launch report execution
            ExecuteReport.runReport(absoluteXmlSourcePath, absoluteOutputPath,
                    ExecuteReport.DEFAULT_RPTDESIGN_FILE_PATH, progress.newChild(70), this.format); // Third part is subtask
            // Log a message to the user console
            Console.getInstance(Activator.CONSOLE_NAME).log(
                    "Report generated successfully [" + (System.currentTimeMillis() - start) +
                        " ms] ! See : " + absoluteOutputPath);
        } catch (Exception e) {
            Console.getInstance(Activator.CONSOLE_NAME).log(
                    "An error occured during report generation ! See product logs.");
            e.printStackTrace();
        }
    }
}