package org.objectweb.proactive.extensions.scheduler.ext.scilab.util;

public class ScilabConfiguration {

    // the Home Dir of Scilab on this machine
    private String scilabHome = null;

    public ScilabConfiguration(String scilabHome) {
        this.scilabHome = scilabHome;
    }

    /**
     * The path where Scilab is installed
     * @return scilab path
     */
    public String getScilabHome() {
        return scilabHome;
    }

    public String toString() {
        return "Scilab Home : " + scilabHome;
    }

}
