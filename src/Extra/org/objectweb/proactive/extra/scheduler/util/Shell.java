package org.objectweb.proactive.extra.scheduler.util;

public enum Shell {Sh("/bin/sh"),
    Bash("/bin/bash"),
    Zsh("/bin/zsh"),
    Csh("/bin/csh");
    private String command;

    Shell(String command) {
        this.command = command;
    }

    String command() {
        return command;
    }
}
