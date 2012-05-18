/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.ext.matsci.common.data;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;


/**
 * PASolveMatSciTaskConfig
 *
 * @author The ProActive Team
 */
public class PASolveMatSciTaskConfig implements Serializable {

    private static final long serialVersionUID = 32L;

    /**
     * names of source files
     */
    private ArrayList<String> sourceFileNames = new ArrayList<String>();

    /**
     * Name of source zipped file
     */
    private String sourceZipFileName = null;

    /**
     * URI of source Zipped file
     */
    private URI sourceZipFileURI = null;

    /**
     * URI of source files (non zipped)
     */
    private URI[] sourcesFilesURIs = null;

    /**
     * name of input Variables file
     */
    private String inputVariablesFileName = null;

    /**
     * name of input Variables file for composed task
     */
    private String composedInputVariablesFileName = null;

    /**
     * URI of input Variables file
     */
    private URI inputVariablesFileURI = null;

    /**
     * URI of input Variables file for composed task
     */
    private URI composedInputVariablesFileURI = null;

    /**
     * name of output Variables file
     */
    private String outputVariablesFileName = null;

    /**
     * url of custom selection Script
     */
    private String customScriptUrl = null;

    /**
     * Is the custom selection script static ?
     */
    private boolean staticScript = false;

    /**
     * Parameters of the custom script
     */
    private String customScriptParams = null;

    /**
     * task description
     */
    private String description = null;

    /**
     * output files (non zipped)
     */
    private String[] outputFiles;

    /**
     * input files (non zipped)
     */
    private String[] inputFiles;

    private DSSource inputSource = DSSource.INPUT;

    private DSSource outputSource = DSSource.OUTPUT;

    /**
     * URI of environment zipped file
     */
    private URI envZipFileURI = null;

    /**
     * URI of environment file (non zipped)
     */
    private URI envMatFileURI = null;

    /**
     * presence of input Files
     */
    private boolean inputFilesThere = false;

    /**
     * presence of output files
     */
    private boolean outputFilesThere = false;

    /**
     * Matlab/Scilab input code
     */
    private String inputScript = null;

    /**
     * Matlab/Scilab main code
     */
    private String mainScript = null;

    /**
     * topology in use
     */
    private MatSciTopology topology = null;

    /**
     * number of nodes needed
     */
    private int nbNodes = 1;

    /**
     * threshold proximity for topology
     */
    private long thresholdProximity = 0;

    public PASolveMatSciTaskConfig() {

    }

    public URI getSourceZipFileURI() {
        return sourceZipFileURI;
    }

    public void setSourceZipFileURI(URI sourceZipFileURI) {
        this.sourceZipFileURI = sourceZipFileURI;
    }

    public URI getEnvZipFileURI() {
        return envZipFileURI;
    }

    public DSSource getInputSource() {
        return inputSource;
    }

    public void setInputSource(DSSource inputSource) {
        this.inputSource = inputSource;
    }

    public DSSource getOutputSource() {
        return outputSource;
    }

    public void setOutputSource(DSSource outputSource) {
        this.outputSource = outputSource;
    }

    public String getCustomScriptUrl() {
        return customScriptUrl;
    }

    public void setCustomScriptUrl(String customScriptUrl) {
        this.customScriptUrl = customScriptUrl;
    }

    public boolean isStaticScript() {
        return staticScript;
    }

    public void setStaticScript(boolean staticScript) {
        this.staticScript = staticScript;
    }

    public String getCustomScriptParams() {
        return customScriptParams;
    }

    public void setCustomScriptParams(String customScriptParams) {
        this.customScriptParams = customScriptParams;
    }

    public String getInputVariablesFileName() {
        return inputVariablesFileName;
    }

    public void setInputVariablesFileName(String inputVariablesFileName) {
        this.inputVariablesFileName = inputVariablesFileName;
    }

    public String getOutputVariablesFileName() {
        return outputVariablesFileName;
    }

    public void setOutputVariablesFileName(String outputVariablesFileName) {
        this.outputVariablesFileName = outputVariablesFileName;
    }

    public String getComposedInputVariablesFileName() {
        return composedInputVariablesFileName;
    }

    public void setComposedInputVariablesFileName(String composedInputVariablesFileName) {
        this.composedInputVariablesFileName = composedInputVariablesFileName;
    }

    public URI getComposedInputVariablesFileURI() {
        return composedInputVariablesFileURI;
    }

    public void setComposedInputVariablesFileURI(URI composedInputVariablesFileURI) {
        this.composedInputVariablesFileURI = composedInputVariablesFileURI;
    }

    public URI getInputVariablesFileURI() {
        return inputVariablesFileURI;
    }

    public void setInputVariablesFileURI(URI inputVariablesFileURI) {
        this.inputVariablesFileURI = inputVariablesFileURI;
    }

    public URI[] getSourcesFilesURIs() {
        return sourcesFilesURIs;
    }

    public void setSourcesFilesURIs(URI[] sourcesFilesURIs) {
        this.sourcesFilesURIs = sourcesFilesURIs;
    }

    public String[] getOutputFiles() {
        return outputFiles;
    }

    public void setOutputFiles(String[] outputFiles) {
        this.outputFiles = outputFiles;
    }

    public void setEnvZipFileURI(URI envZipFileURI) {
        this.envZipFileURI = envZipFileURI;
    }

    public String[] getInputFiles() {
        return inputFiles;
    }

    public void setInputFiles(String[] inputFiles) {
        this.inputFiles = inputFiles;
    }

    public boolean isInputFilesThere() {
        return inputFilesThere;
    }

    public void setInputFilesThere(boolean inputFilesThere) {
        this.inputFilesThere = inputFilesThere;
    }

    public boolean isOutputFilesThere() {
        return outputFilesThere;
    }

    public void setOutputFilesThere(boolean outputFilesThere) {
        this.outputFilesThere = outputFilesThere;
    }

    public URI getEnvMatFileURI() {
        return envMatFileURI;
    }

    public void setEnvMatFileURI(URI envMatFileURI) {
        this.envMatFileURI = envMatFileURI;
    }

    public String getInputScript() {
        return inputScript;
    }

    public void setInputScript(String inputScript) {
        this.inputScript = inputScript;
    }

    public String getMainScript() {
        return mainScript;
    }

    public void setMainScript(String mainScript) {
        this.mainScript = mainScript;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSourceZipFileName() {
        return sourceZipFileName;
    }

    public void setSourceZipFileName(String sourceZipFileName) {
        this.sourceZipFileName = sourceZipFileName;
    }

    public ArrayList<String> getSourceFileNames() {
        return sourceFileNames;
    }

    public void addSourceFile(String src) {
        sourceFileNames.add(src);
    }

    public MatSciTopology getTopology() {
        return topology;
    }

    public void setTopology(MatSciTopology topology) {
        this.topology = topology;
    }

    public void setTopology(String topology) {
        this.topology = MatSciTopology.getTopology(topology);
    }

    public int getNbNodes() {
        return nbNodes;
    }

    public void setNbNodes(double nbNodes) {
        this.nbNodes = (int) Math.round(nbNodes);
    }

    public long getThresholdProximity() {
        return thresholdProximity;
    }

    public void setThresholdProximity(double thresholdProximity) {
        this.thresholdProximity = Math.round(thresholdProximity);
    }
}
