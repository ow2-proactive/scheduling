package org.ow2.proactive.scheduler.ext.mapreduce;

import java.io.IOException;

import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

public class CombineOutputConverter<K, V> extends RecordWriter<K, V> {

	public OutputCollector<K, V> outputCollector = null;

	public CombineOutputConverter(OutputCollector<K, V> outputCollector) {
		this.outputCollector = outputCollector;
	}

	@Override
	public void close(TaskAttemptContext arg0) throws IOException,
			InterruptedException {
	}

	@Override
	public void write(K key, V value) throws IOException,
			InterruptedException {
		outputCollector.collect(key, value);

	}

}
