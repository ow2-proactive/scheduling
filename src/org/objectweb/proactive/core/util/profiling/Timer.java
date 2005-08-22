package org.objectweb.proactive.core.util.profiling;


/**
 * @author fabrice
 *
 */
public interface Timer {
    public void start();

    public void resume();

    public void pause();

    public void stop();

    public long getCumulatedTime();

    public int getNumberOfValues();

    public double getAverage();

    public String getName();

    public void setName(String name);

    public void dump();

    public void reset();
}
