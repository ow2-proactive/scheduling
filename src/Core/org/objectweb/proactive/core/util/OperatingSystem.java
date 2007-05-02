package org.objectweb.proactive.core.util;

public enum OperatingSystem {windows(';'),
    unix(':');
    protected char pathSeparator;

    OperatingSystem(char pathSeparator) {
        this.pathSeparator = pathSeparator;
    }

    public char pathSeparator() {
        return pathSeparator;
    }

    static public OperatingSystem getOperatingSystem() {
        String val = System.getProperty("os.name");
        if (val.contains("Windows")) {
            return windows;
        }

        return unix;
    }
}
