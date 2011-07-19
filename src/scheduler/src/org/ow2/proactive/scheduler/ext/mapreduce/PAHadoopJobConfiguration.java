package org.ow2.proactive.scheduler.ext.mapreduce;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.RawComparator;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.ReflectionUtils;
import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;


/**
 * The {@link PAHadoopJobConfiguration} represents the {@link Serializable}
 * version of the Hadoop {@link Configuration} class. An instance of the
 * FakeHadoopConfiguration class is an instance of the Hadoop Configuration
 * built from an Hadoop Configuration instance (look at the constructor
 * {@link PAHadoopJobConfiguration#FakeHadoopConfiguration(Configuration)} to
 * get more details). This means that the actual class of the configuration
 * object used by the ProActive MapReduce framework is FakeHadoopConfiguration.
 *
 * We cannot use directly the Hadoop Configuration class because it is not
 * serializable. But we can transform the non-Serializable Hadoop
 * {@link Configuration} class into a Serializable one because the Hadoop
 * {@link Configuration} class has a no-arg constructor and because the
 * {@link DataInput} and {@link DataOutput} instances used, respectively, in the
 * methods {@link Configuration#readFields(DataInput)} and
 * {@link Configuration#write(DataOutput)} represent an interface implemented by
 * the {@link ObjectInputStream} and {@link ObjectOutputStream} instances used,
 * respectively, in the
 * {@link PAHadoopJobConfiguration#readObject(ObjectInputStream)} and
 * {@link PAHadoopJobConfiguration#writeObject(ObjectOutputStream)} methods we
 * must implement to specialize the handling of the {@link Configuration} class
 * during the serialization and deserialization processes.
 *
 * We choose to not support the Hadoop {@link JobConf} class because it is
 * deprecated.
 *
 * The {@link PAHadoopJobConfiguration} class is an instance of
 * {@link Configuration} but since Configuration has not methods like
 * getMapOutputKeyClass(), getOutputKeyClass() etc... (which are present in a
 * sub-class of the Configuration class whose name is {@link JobConf}) we must
 * re-define them to be able to have the same behavior of the methods of the
 * JobConf class. The drawback is that each time we want to invoke a method that
 * is not in the interface of the Configuration class but is in the interface of
 * the JobConf class we have to down-cast the Configuration instance to our
 * defined {@link PAHadoopJobConfiguration} class (e.g., to invoke the
 * {@link JobConf#getMapOutputKeyClass()} method that is not in the
 * Configuration interface we must do: ((FakeHadoopConfiguration)
 * configuration).{@link #getMapperOutputKeyClass()})
 *
 * We must notice that do to the structure this class has (it extends the Hadoop
 * {@link Configuration} class but one instance is created from an Hadoop
 * Configuration one (cloning all the properties of the Hadoop Configuration
 * instance)) we can access the value of the properties it stores through the
 * "generic" methods of the Hadoop Configuration interface or, after we did a
 * cast to "PAHadoopJobConfiguration", through the methods of the
 * {@link PAHadoopJobConfiguration} interface.
 *
 * @author The ProActive Team
 *
 */
public class PAHadoopJobConfiguration extends Configuration implements Serializable {

    /**  */
    private static final long serialVersionUID = 31L;
    /**
     * Further configuration parameter that can be used to store the
     * DataSpacesFileObject a {@link JavaExecutable} or a {@link FileSystem}
     * must use to read data from or to write data to. We must notice that the
     * value of this attribute is used to instantiate a FileSystem object. Each
     * instance of a FileSystem has its own {@link DataSpacesFileObject}. This
     * means that if a {@link JavaExecutable} must read data from a
     * {@link DataSpacesFileObject} and write the output to a different
     * {@link DataSpacesFileObject} then that JavaExecutable will have to
     * different instances of the FileSystem: one used to read data and the
     * other one used to write them.
     */
    protected DataSpacesFileObject dataSpacesFileObject = null;

    /**
     * Create a {@link PAHadoopJobConfiguration} instance from an Hadoop
     * {@link Configuration} instance
     *
     * @param configuration
     *            the Hadoop {@link Configuration} instance from which the
     *            {@link PAHadoopJobConfiguration} instance must be created
     * @see Configuration#Configuration(Configuration other)
     */
    public PAHadoopJobConfiguration(Configuration configuration) {
        super(configuration);
    }

    /**
     * Set the DataSpacesFileObject
     *
     * @param dataSpacesFileObject
     *            the {@link DataSpacesFileObject} to set
     */
    public void setDataSpacesFileObject(DataSpacesFileObject dataSpacesFileObject) {
        this.dataSpacesFileObject = dataSpacesFileObject;
    }

    /**
     * Retrieve the DataSpacesFileObject
     *
     * @return dataSpacesFileObject the {@link DataSpacesFileObject}
     */
    public DataSpacesFileObject getDataSpacesFileObject() {
        return dataSpacesFileObject;
    }

    /**
     * The method of {@link Serializable} interface to implement to let the
     * {@link PAHadoopJobConfiguration} have special handling during the
     * deserialization process.
     *
     * @param in
     *            {@link ObjectInputStream} to deserialize this object from
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        readFields(in);
    }

    /**
     * The method of the {@link Serializable} interface to implement to let the
     * {@link PAHadoopJobConfiguration} have special handling during the
     * serialization process
     *
     * @param out
     *            {@link ObjectOutputStream} to serialize this object into
     * @throws IOException
     */
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        write(out);
    }

    /**
     * Retrieve the class of the key of the key-value pairs the Mapper of the
     * MapReduce job produces as output data. If it is not set, this method
     * return the class of the output key of the MapReduce job (i.e., it not
     * return the class of the output key of the Mapper). In Hadoop to retrieve
     * the class of the key used by the Mapper the method
     * {@link JobConf#getMapOutputKeyClass()} must be called. That method in
     * turn invokes the {@link Configuration#getClass(String, Class, Class)}
     * method. Since in the ProActive MapReduce framework we cannot have the
     * Hadoop JobConf instance but we have only the Hadoop Configuration
     * instance then in the body of this method we must call the
     * {@link Configuration#getClass(String, Class, Class)} method (to obtain
     * the same behavior of the Hadoop {@link JobConf#getMapOutputKeyClass()}
     * method).
     *
     * Apparently the method {@link JobConf#getMapOutputKeyClass()} (and so this
     * method) exists to allow the Mapper output key class to be different from
     * the class of the MapReduce job output key. In fact, otherwise, the
     * existence of the two methods {@link JobConf#getMapOutputKeyClass()} and
     * {@link JobConf#getOutputKeyClass()} will be a non sense.
     *
     * @return the class of the key of the key-value pairs the Mapper of the
     *         MapReduce job produces as its output data
     */
    public Class<?> getMapperOutputKeyClass() {
        Class<?> retv = getClass(
                PAMapReduceFrameworkProperties
                        .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_MAPPER_OUTPUT_KEY_CLASS_PROPERTY_NAME
                                .getKey()), null, Object.class);
        if (retv == null) {
            retv = getOutputKeyClass();
        }
        return retv;
    }

    /**
     * Retrieve the class of the value of the key-value pairs the Mapper of the
     * MapReduce job emits as output data. If it is not set, this method use the
     * (final) output value class. This allows the Mapper output value class to
     * be different than the final output value class.
     *
     * @return the class of the value of the key-value pairs the Mapper of the
     *         MapReduce job emits as output data
     */
    public Class<?> getMapperOutputValueClass() {
        Class<?> retv = getClass(
                PAMapReduceFrameworkProperties
                        .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_MAPPER_OUTPUT_VALUE_CLASS_PROPERTY_NAME
                                .getKey()), null, Object.class);
        if (retv == null) {
            retv = getOutputValueClass();
        }
        return retv;
    }

    /**
     * Retrieve the class of the key of the key-value pairs the MapReduce job
     * emits as output data. We must notice the code of this method is very
     * close to the one of the method {@link JobConf#getOutputKeyClass()} but we
     * need it in the {@link PAHadoopJobConfiguration} because in the ProActive
     * MapReduce framework we do not have a JobConf instance. We had only a
     * Configuration instance which has not a getOutputKeyClass so that to be
     * able to invoke that method we must re-define it
     *
     * @return the class of the key of the key-value pairs the MapReduce job
     *         emits as output data
     */
    public Class<?> getOutputKeyClass() {
        return getClass(PAMapReduceFrameworkProperties
                .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_OUTPUT_KEY_CLASS_PROPERTY_NAME
                        .getKey()), LongWritable.class, Object.class);
    }

    /**
     * Retrieve the class of the value of the key-value pairs the Mapper of the
     * MapReduce job emits as output data. If it is not set, this method use the
     * (final) output value class. This allows the Mapper output value class to
     * be different than the final output value class.
     *
     * @return the class of the value of the key-value pairs the Mapper of the
     *         MapReduce job emits as output data
     */
    public Class<?> getOutputValueClass() {
        return getClass(PAMapReduceFrameworkProperties
                .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_OUTPUT_VALUE_CLASS_PROPERTY_NAME
                        .getKey()), Text.class, Object.class);
    }

    /**
     * Retrieve the comparator to use when comparing the key the job produces in
     * output. The code in the body of the method is a simply copy and paste
     * (following the chain of method invocations in the Hadoop {@link JobConf}
     * class when the Hadoop framework try to retrieve the class of the key for
     * which a comparator must be instantiated) from the class Hadoop class
     * JobConf, but it represents the only way we can retrieve a comparator. We
     * could invoke directly the methods of the class {@link JobConf} but we
     * have not the Hadoop JobConf instance. We have only the Hadoop
     * {@link Configuration} instance.
     *
     * @return the comparator for the key the job produces in output
     */
    public RawComparator getOutputKeyComparator() {
        RawComparator rawComparator = null;
        Class<? extends RawComparator> rawComparatorClass = getClass(
                PAMapReduceFrameworkProperties
                        .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_JOB_OUTPUT_VALUE_GROUPING_COMPARATOR
                                .getKey()), null, RawComparator.class);
        if (rawComparatorClass == null) {
            rawComparatorClass = getClass(
                    PAMapReduceFrameworkProperties
                            .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_JOB_OUTPUT_KEY_COMPARATOR_CLASS
                                    .getKey()), null, RawComparator.class);
            if (rawComparatorClass == null) {
                Class<?> objectClass = null;
                objectClass = getMapperOutputKeyClass();
                Class<? extends WritableComparable> writableComparableClass = objectClass
                        .asSubclass(WritableComparable.class);
                rawComparator = WritableComparator.get(writableComparableClass);
            } else {
                rawComparator = ReflectionUtils.newInstance(rawComparatorClass, this);
            }
        } else {
            rawComparator = ReflectionUtils.newInstance(rawComparatorClass, this);
        }
        return rawComparator;
    }

    /**
     * Retrieve the {@link InputFormat} class for the Hadoop job
     *
     * @return the {@link InputFormat} class
     */
    public String getInputFormatClassName() {
        return getClass(
                PAMapReduceFrameworkProperties
                        .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_INPUT_FORMAT_CLASS_PROPERTY_NAME
                                .getKey()), TextInputFormat.class).getName();
    }

    /**
     * Retrieve the {@link Mapper} class for the Hadoop job
     *
     * @return the {@link Mapper} class
     */
    public String getMapperClassName() {
        return getClass(
                PAMapReduceFrameworkProperties
                        .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_MAPPER_CLASS_PROPERTY_NAME
                                .getKey()), Mapper.class).getName();
    }

    /**
     * Retrieve the {@link Reducer} class for the Hadoop job
     *
     * @return the {@link Reducer} class
     */
    public String getReducerClassName() {
        return getClass(
                PAMapReduceFrameworkProperties
                        .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_REDUCER_CLASS_PROPERTY_NAME
                                .getKey()), Reducer.class).getName();
    }

    /**
     * Retrieve the name of the class to use as the Combiner
     *
     * @return the name of the class to use as the Combiner, null if no such
     *         class exists
     */
    public String getCombinerClassName() {
        Class<?> combinerClass = getClass(PAMapReduceFrameworkProperties
                .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_COMBINER_CLASS_PROPERTY_NAME
                        .getKey()), null);
        if (combinerClass != null) {
            return combinerClass.getName();
        }
        return null;
    }

    /**
     * Retrieve the class to use as the combiner
     *
     * @return the class to use as the combiner, or null if no class to use as
     *         the combiner was defined by the user.
     */
    public Class<?> getCombinerClass() {
        String combinerClassName = getCombinerClassName();
        if (combinerClassName != null) {
            try {
                return getClassByName(combinerClassName);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Retrieve the {@link OutputFormat} class for the Hadoop job
     *
     * @return the {@link OutputFormat} class
     */
    public String getOutputFormatClassName() {
        return getClass(
                PAMapReduceFrameworkProperties
                        .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_OUTPUT_FORMAT_CLASS_PROPERTY_NAME
                                .getKey()), TextOutputFormat.class).getName();
    }
}
