package org.ow2.proactive.scheduler.ext.mapreduce;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.RawComparator;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.RawKeyValueIterator;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.util.ReflectionUtils;

/**
 * The {@link CombinerRunner} class is a copy and paste of the code of the
 * Hadoop classes org.apache.hadoop.mapred.Task.CombinerRunner and
 * org.apache.hadoop.mapred.Task.NewCombineRunner. We need to do the copy and
 * paste because the Hadoop Task class and so its inner classes are not visible
 * outside the org.apache.hadoop.mapred package
 *
 * @author The ProActive Team
 *
 */
public class CombinerRunner<K, V> {

	protected final Class<? extends org.apache.hadoop.mapreduce.Reducer<K, V, K, V>> reducerClass;
	protected final TaskAttemptID taskAttemptId;
	protected final RawComparator<K> comparator;
	protected final Class<K> keyClass;
	protected final Class<V> valueClass;

	protected final Configuration configuration;

	/**
	 * Create a new instance of the CombinerRunner
	 *
	 * @param taskAttemptContext
	 * @throws ClassNotFoundException
	 *             if the class to use as the combiner is not found
	 */
	public CombinerRunner(TaskAttemptContext taskAttemptContext)
			throws ClassNotFoundException {
		this.configuration = taskAttemptContext.getConfiguration();

		/*
		 * TODO assure that the class is loaded properly as some errors related
		 * to which class loader is used to load the class could occur
		 */
		this.reducerClass = (Class<? extends org.apache.hadoop.mapreduce.Reducer<K, V, K, V>>) ((PAHadoopJobConfiguration) configuration)
				.getCombinerClass();
		if (reducerClass == null) {
			/*
			 * If the class to use for the combiner is not found we cannot
			 * create an instance of the CombinerRunner so we throw an exception
			 */
			throw new ClassNotFoundException(
					"The class to use for the combiner is null!");
		}

		/*
		 * As we can notice in the code of the class
		 * org.apache.hadoop.mapred.Task.NewCombinerRunner to initialize the
		 * comparator the method JobContext.getSortComparator is called That
		 * method invokes the method JobConf.getOutputKeyComparator. So if we
		 * invoke the method PAHadoopConfiguration.getOutputKeyComparator() (
		 * that has the same semantics of the method
		 * JobConf.getOutputKeyComparator() ) we get the right comparator.
		 */
		comparator = ((PAHadoopJobConfiguration) configuration)
				.getOutputKeyComparator();

		keyClass = (Class<K>) ((PAHadoopJobConfiguration) configuration)
				.getMapperOutputKeyClass();
		valueClass = (Class<V>) ((PAHadoopJobConfiguration) configuration)
				.getMapperOutputValueClass();

		taskAttemptId = taskAttemptContext.getTaskAttemptID();
	}

	public void combine(RawKeyValueIterator rawKeyValueIterator,
			OutputCollector<K, V> collector) throws IOException,
			InterruptedException {
		org.apache.hadoop.mapreduce.Reducer<K, V, K, V> reducer = (org.apache.hadoop.mapreduce.Reducer<K, V, K, V>) ReflectionUtils
				.newInstance(reducerClass, configuration);

		/*
		 * We must notice that the counters for the Reducer.Context cannot be
		 * null because otherwise the class
		 * "org.apache.hadoop.mapreduce.ReduceContext.nextKeyValue" will throw a
		 * NullPointerException when it invokes the method
		 * "inputValueCounter.increment(1);" Hence we must build two fake
		 * counters.
		 */
		Counter fakeHadoopInputKeyCounter = new FakeHadoopCounter(
				PAMapReduceFrameworkProperties.getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_INPUT_KEY_COUNTER_NAME
						.getKey()),
				PAMapReduceFrameworkProperties
						.getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_INPUT_KEY_COUNTER_DISPLAY_NAME
								.getKey()));
		Counter fakeHadoopInputValueCounter = new FakeHadoopCounter(
				PAMapReduceFrameworkProperties.getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_INPUT_VALUE_COUNTER_NAME
						.getKey()),
				PAMapReduceFrameworkProperties
						.getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_INPUT_VALUE_COUNTER_DISPLAY_NAME
								.getKey()));

		Reducer.Context fakeHadoopReducerContext = new FakeHadoopReducerContext(
				reducer, configuration, taskAttemptId, rawKeyValueIterator,
				fakeHadoopInputKeyCounter, fakeHadoopInputValueCounter,
				new CombineOutputConverter<K, V>(collector), null,
				new FakeHadoopStatusReporter(), comparator, keyClass,
				valueClass);
		reducer.run(fakeHadoopReducerContext);
	}
}
