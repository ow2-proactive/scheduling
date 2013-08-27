package org.ow2.proactive.scheduler.ext.mapreduce;

import java.io.IOException;


public interface MapperOutputCollector<K, V> {

    public void collect(K key, V value, int partition) throws IOException, InterruptedException;

    public void close() throws IOException, InterruptedException;

    public void flush() throws IOException, InterruptedException, ClassNotFoundException;

}
