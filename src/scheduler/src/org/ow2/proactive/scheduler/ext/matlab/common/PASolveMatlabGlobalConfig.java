package org.ow2.proactive.scheduler.ext.matlab.common;

import org.ow2.proactive.scheduler.ext.matsci.common.PASolveMatSciGlobalConfig;


/**
 * PASolveMatlabGlobalConfig
 *
 * @author The ProActive Team
 */
public class PASolveMatlabGlobalConfig extends PASolveMatSciGlobalConfig {

    private String checkLicenceScriptUrl = null;

    private String matFileOptions = null;

    public PASolveMatlabGlobalConfig() {

    }

    public String getCheckLicenceScriptUrl() {
        return checkLicenceScriptUrl;
    }

    public void setCheckLicenceScriptUrl(String checkLicenceScript) {
        this.checkLicenceScriptUrl = checkLicenceScript;
    }

    public String getMatFileOptions() {
        return matFileOptions;
    }

    public void setMatFileOptions(String matFileOptions) {
        this.matFileOptions = matFileOptions;
    }

}
