package org.objectweb.proactive.scheduler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;


public class Agent {
    public Agent() {
    }

    public Agent(HashMap systemProperties) {
        Set keySet = systemProperties.keySet();
        Iterator iterator = keySet.iterator();

        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            String value = (String) systemProperties.get(key);

            System.setProperty(key, value);
        }
    }

    public void ping() {
    }
}
