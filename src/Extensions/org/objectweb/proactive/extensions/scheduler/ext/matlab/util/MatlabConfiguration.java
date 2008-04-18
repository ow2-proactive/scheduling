package org.objectweb.proactive.extensions.scheduler.ext.matlab.util;

public class MatlabConfiguration {

    /**
     * The home dir of Matlab on this machine
     */
    private String matlabHome = null;

    /**
     * The name of the arch dir to find native libraries (can be win32, glnx86, ...)
     */
    private String matlabLibDirName = null;

    /**
     * Version of Matlab 
     */
    private String matlabVersion;

    /**
     *  the name of the Matlab command on this machine
     */
    private String matlabCommandName = null;

    /**
     * Path to the ptolemy library dir.
     */
    private String ptolemyPath;

    private String nl = System.getProperty("line.separator");

    public MatlabConfiguration(String matlabHome, String matlabVersion, String matlabLibDirName,
            String matlabCommandName, String ptolemyPath) {
        this.matlabHome = matlabHome;
        this.matlabVersion = matlabVersion;
        this.matlabLibDirName = matlabLibDirName;
        this.matlabCommandName = matlabCommandName;
        this.ptolemyPath = ptolemyPath;
    }

    /**
     * returns the home dir of Matlab
     * @return home dir
     */
    public String getMatlabHome() {
        return matlabHome;
    }

    /**
     * returns the relative path of the lib directory on this matlab install
     * @return lib dir name
     */
    public String getMatlabLibDirName() {
        return matlabLibDirName;
    }

    /**
     * returns the path to the ptolemy library directory
     * @return ptolemy lib dir
     */
    public String getPtolemyPath() {
        return ptolemyPath;
    }

    /**
     * Returns the current matlab version
     * @return matlab version
     */
    public String getMatlabVersion() {
        return matlabVersion;
    }

    /**
     * Returns the name of the matlab command
     * @return
     */
    public String getMatlabCommandName() {
        return matlabCommandName;
    }

    public String toString() {
        return "Matlab home : " + matlabHome + nl + "Matlab version : " + matlabVersion + nl +
            "Matlab lib directory name : " + matlabLibDirName + nl + "Matlab command name:" +
            matlabCommandName + nl + "Ptolemy lib dir : " + ptolemyPath;
    }

}
