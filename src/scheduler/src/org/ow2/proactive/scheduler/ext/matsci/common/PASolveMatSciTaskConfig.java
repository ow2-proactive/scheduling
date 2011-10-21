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
package org.ow2.proactive.scheduler.ext.matsci.common;

import org.ow2.proactive.topology.descriptor.ThresholdProximityDescriptor;
import org.ow2.proactive.topology.descriptor.TopologyDescriptor;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;


/**
 * PASolveMatSciTaskConfig
 *
 * @author The ProActive Team
 */
public class PASolveMatSciTaskConfig implements Serializable {

    /**  */
    private static final long serialVersionUID = 31L;

    private URI[] inputZipFilesURI = null;

    private URI outputZipFileURI = null;

    private String[] inputFilesZipNames;

    private String[] outputFilesZipNames;

    private ArrayList<String> sourceFileNames = new ArrayList<String>();

    /**
     * Name of source zip file
     */
    private String sourceZipFileName = null;

    private String inputVariablesFileName = null;

    private String composedInputVariablesFileName = null;

    private URI inputVariablesFileURI = null;

    private URI composedInputVariablesFileURI = null;

    private String outputVariablesFileName = null;

    private String customScriptUrl = null;

    private String description = null;

    private String[] outputFilesZipList;

    private String[] outputFiles;

    private String[] inputFiles;

    private URI sourceZipFileURI = null;

    private URI[] sourcesFilesURIs = null;

    private URI envZipFileURI = null;

    private URI envMatFileURI = null;

    private boolean inputFilesThere = false;

    private boolean outputFilesThere = false;

    private String inputScript = null;

    private String mainScript = null;

    private TopologyDescriptor topology = null;

    private int nbNodes = 1;

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

    public String getCustomScriptUrl() {
        return customScriptUrl;
    }

    public void setCustomScriptUrl(String customScriptUrl) {
        this.customScriptUrl = customScriptUrl;
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

    public URI[] getInputZipFilesURI() {
        return inputZipFilesURI;
    }

    public void setInputZipFilesURI(URI[] inputZipFilesURI) {
        this.inputZipFilesURI = inputZipFilesURI;
    }

    public URI getOutputZipFileURI() {
        return outputZipFileURI;
    }

    public void setOutputZipFileURI(URI outputZipFileURI) {
        this.outputZipFileURI = outputZipFileURI;
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

    public String[] getInputFilesZipNames() {
        return inputFilesZipNames;
    }

    public void setInputFilesZipNames(String[] inputFilesZipNames) {
        this.inputFilesZipNames = inputFilesZipNames;
    }

    public String[] getOutputFilesZipNames() {
        return outputFilesZipNames;
    }

    public void setOutputFilesZipNames(String[] outputFilesZipNames) {
        this.outputFilesZipNames = outputFilesZipNames;
    }

    public String[] getOutputFilesZipList() {
        return outputFilesZipList;
    }

    public void setOutputFilesZipList(String[] outputFilesZipList) {
        this.outputFilesZipList = outputFilesZipList;
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

    public TopologyDescriptor getTopology() {
        return topology;
    }

    public void setTopology(TopologyDescriptor topology) {
        this.topology = topology;
    }

    public void setTopology(String topology) {
        if (topology.equalsIgnoreCase("arbitrary")) {
            this.topology = TopologyDescriptor.ARBITRARY;
        } else if (topology.equalsIgnoreCase("bestProximity")) {
            this.topology = TopologyDescriptor.BEST_PROXIMITY;
        } else if (topology.equalsIgnoreCase("singleHost")) {
            this.topology = TopologyDescriptor.SINGLE_HOST;
        } else if (topology.equalsIgnoreCase("singleHostExclusive")) {
            this.topology = TopologyDescriptor.SINGLE_HOST_EXCLUSIVE;
        } else if (topology.equalsIgnoreCase("multipleHostsExclusive")) {
            this.topology = TopologyDescriptor.MULTIPLE_HOSTS_EXCLUSIVE;
        } else if (topology.equalsIgnoreCase("differentHostsExclusive")) {
            this.topology = TopologyDescriptor.DIFFERENT_HOSTS_EXCLUSIVE;
        } else if (topology.equalsIgnoreCase("thresholdProximity")) {
            this.topology = new ThresholdProximityDescriptor(this.thresholdProximity);
        }
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
        if (this.topology instanceof ThresholdProximityDescriptor) {
            this.topology = new ThresholdProximityDescriptor(this.thresholdProximity);
        }
    }
}
