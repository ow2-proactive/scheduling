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
package org.objectweb.proactive.ic2d.timit.util;

import java.io.FileInputStream;
import java.net.URL;

import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.PDFRenderOption;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.objectweb.proactive.ic2d.timit.Activator;


/**
 * This class contains the static method runReport that is used to generate a report (thanks to BIRT)
 * from an xml source file.
 * @author The ProActive Team
 *
 */
public class ExecuteReport {
    public static final String PDF_FORMAT = "Pdf";
    public static final String HTML_FORMAT = "Html";
    /**
     * The default path of the rptdesign file used to generate the report	 
     */
    public static final String DEFAULT_RPTDESIGN_FILE_PATH = "reports/TimIt_Report.rptdesign";

    /**
     * Be careful with absolute/relative paths when use this method.
     * @param absolutePathOfXmlSourceFile Absolute path of the xml source file
     * @param outputFilename Absolute path of the output file
     * @param rptDesignFilename Relative path of the rptdesign file
     */
    @SuppressWarnings("unchecked")
    public static final void runReport(final String absolutePathOfXmlSourceFile, final String outputFilename,
            final String rptDesignFilename, final IProgressMonitor monitor, final String format) {
        IRunAndRenderTask task = null;
        IReportEngine engine = null;
        try {
            // Begin task on monitor
            final SubMonitor subMonitor = SubMonitor.convert(monitor, 100); // passing name as parameter has no effect See eclipse Bug#:174040
            subMonitor.setTaskName("Generating Report ...");

            // Set up the engine configuration
            final EngineConfig config = new EngineConfig();
            System.setProperty("RUN_UNDER_ECLIPSE", "true");

            final IReportEngineFactory factory = (IReportEngineFactory) Platform
                    .createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
            engine = factory.createReportEngine(config);

            // Locate the report file
            final URL reportURL = Activator.getDefault().getBundle().getEntry(DEFAULT_RPTDESIGN_FILE_PATH);
            if (reportURL == null) {
                throw new RuntimeException("Cannot locate the rptdesign file !");
            }

            // Open the report design
            final IReportRunnable design = engine
                    .openReportDesign(FileLocator.toFileURL(reportURL).getPath());
            subMonitor.worked(30);

            // Create task to run and render the report,		
            task = engine.createRunAndRenderTask(design);
            task.getAppContext().put("org.eclipse.birt.report.data.oda.xml.inputStream",
                    new FileInputStream(absolutePathOfXmlSourceFile));
            task.getAppContext().put("org.eclipse.birt.report.data.oda.xml.closeInputStream",
                    new Boolean(true));

            // Set the report parameter value for xml source file
            task.setParameterValue("xml_file_name", absolutePathOfXmlSourceFile);

            // Render in pdf
            if (format == ExecuteReport.PDF_FORMAT) {
                final PDFRenderOption pdfOptions = new PDFRenderOption();
                pdfOptions.setOutputFileName(outputFilename);
                pdfOptions.setOutputFormat("pdf");
                task.setRenderOption(pdfOptions);
            } else { // or in HTML format
                final HTMLRenderOption htmlOptions = new HTMLRenderOption();
                htmlOptions.setOutputFileName(outputFilename);
                htmlOptions.setOutputFormat("html");
                htmlOptions.setHtmlRtLFlag(false);
                htmlOptions.setEmbeddable(false);
                htmlOptions.setImageDirectory(outputFilename + "_images");
                task.setRenderOption(htmlOptions);
            }
            subMonitor.worked(20);

            // Run the task
            task.run();

            monitor.worked(50);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (task != null)
                task.close();
            if (engine != null)
                engine.destroy();
        }
    }
}