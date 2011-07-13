package org.ow2.proactive.scheduler.ext.mapreduce;

import java.io.IOException;

import org.apache.hadoop.mapred.OutputCollector;
import org.ow2.proactive.scheduler.ext.mapreduce.IntermediateFile.Writer;

/**
 * The {@link CombineOutputCollector} is a simply copy and paste of the Hadoop
 * class org.apache.hadoop.mapred.Task.CombineOutputCollector. We do the copy and
 * paste because that was the only way to have a class whose behavior is equal to
 * the one of the mentioned Hadoop class since that class is not visible outside
 * the org.apache.hadoop.mapred.Task
 *
 * @author The ProActive Team
 *
 * @param <K> the class of the key
 * @param <V> the class of the value
 */
public class CombineOutputCollector<K, V> implements OutputCollector<K, V> {

	protected Writer<K, V> writer = null;

	public synchronized void setWriter(Writer<K, V> writer) {
		this.writer = writer;
	}

	@Override
	public synchronized void collect(K key, V value) throws IOException {
		writer.append(key, value);
	}

}
