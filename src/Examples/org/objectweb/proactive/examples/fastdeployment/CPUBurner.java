package org.objectweb.proactive.examples.fastdeployment;

import java.io.Serializable;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.annotation.Cache;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.core.util.wrapper.LongWrapper;


public class CPUBurner implements Serializable, InitActive {
    int id;
    Manager manager;

    public CPUBurner() {
        // No-args empty constructor
    }

    public CPUBurner(IntWrapper id, Manager manager) {
        this.id = id.intValue();
        this.manager = manager;
    }

    public void compute(LongWrapper l) {
        long val = l.longValue();
        for (long i = 0; i < val; i++) {
            // Does nothing but eats some CPU time
        }

        manager.resultAvailable(new Result(id, null));
    }

    public void initActivity(Body body) {
        ProActiveObject.setImmediateService("getId");
    }

    @Cache
    public IntWrapper getId() {
        return new IntWrapper(id);
    }
}
