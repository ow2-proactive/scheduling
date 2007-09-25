package org.objectweb.proactive.examples.fastdeployment;

import java.io.Serializable;


public class Result implements Serializable {
    int slaveID;
    Object result;

    public Result() {
        // No-args empty constructor
    }

    public Result(int slaveID, Object result) {
        this.slaveID = slaveID;
        this.result = result;
    }

    public int getSlaveID() {
        return slaveID;
    }

    public Object getResult() {
        return result;
    }
}
