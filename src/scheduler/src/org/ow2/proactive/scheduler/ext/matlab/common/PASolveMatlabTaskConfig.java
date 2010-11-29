package org.ow2.proactive.scheduler.ext.matlab.common;

import org.ow2.proactive.scheduler.ext.matsci.common.PASolveMatSciTaskConfig;


/**
 * PASolveMatlabTaskConfig
 *
 * @author The ProActive Team
 */
public class PASolveMatlabTaskConfig extends PASolveMatSciTaskConfig {

    private String[] checkLicenceScriptParams;

    public PASolveMatlabTaskConfig() {

    }

    public String[] getCheckLicenceScriptParams() {
        return checkLicenceScriptParams;
    }

    public void setCheckLicenceScriptParams(String[] checkLicenceScriptParams) {
        this.checkLicenceScriptParams = checkLicenceScriptParams;
    }
}
