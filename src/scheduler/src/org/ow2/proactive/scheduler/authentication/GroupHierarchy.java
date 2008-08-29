package org.ow2.proactive.scheduler.authentication;

import javax.security.auth.login.FailedLoginException;


public class GroupHierarchy {
    private String[] hierarchy;

    public GroupHierarchy(String[] hierarchy) {
        this.hierarchy = hierarchy;
    }

    public boolean isAbove(String trueGroup, String reqGroup) throws GroupException {
        int trueGroupLevel = groupLevel(trueGroup);

        if (trueGroupLevel == -1) {
            throw new GroupException("group asked " + trueGroup + " is not in groups hierarchy");
        }

        int reqGroupLevel = groupLevel(reqGroup);

        if (reqGroupLevel == -1) {
            throw new GroupException("Required group " + reqGroup + " is not in groups hierarchy");
        }

        return trueGroupLevel >= reqGroupLevel;
    }

    private int groupLevel(String group) {
        for (int i = hierarchy.length - 1; i > -1; i--) {
            if (hierarchy[i].equals(group)) {
                return i;
            }
        }

        return -1;
    }

    public boolean isGroupInHierarchy(String group) {
        if (groupLevel(group) != -1) {
            return true;
        } else {
            return false;
        }
    }
}
