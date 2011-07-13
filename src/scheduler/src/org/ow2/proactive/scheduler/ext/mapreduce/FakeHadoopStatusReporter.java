package org.ow2.proactive.scheduler.ext.mapreduce;

import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.StatusReporter;

public class FakeHadoopStatusReporter extends StatusReporter {

	@Override
	public Counter getCounter(Enum<?> name) {
		return null;
	}

	@Override
	public Counter getCounter(String group, String name) {
		return null;
	}

	@Override
	public void progress() {
		// do nothing
	}

	@Override
	public void setStatus(String status) {
		// do nothing
	}

}
