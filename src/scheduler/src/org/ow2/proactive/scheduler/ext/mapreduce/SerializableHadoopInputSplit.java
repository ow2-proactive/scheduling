package org.ow2.proactive.scheduler.ext.mapreduce;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.util.ReflectionUtils;


/**
 * The {@link SerializableHadoopInputSplit} class is used to make the Hadoop {@link InputSplit}
 * 	one serializable when using the Java serialization.
 * It is a wrapper for an Hadoop InputSplit and it stores the actual class of that InputSplit
 * 	to be able to serialize/deserialize it through Hadoop "readFields" and "write" method
 * 	invocations.
 *
 * @author The ProActive Team
 *
 */
public class SerializableHadoopInputSplit extends InputSplit implements Serializable {

    /**  */
    private static final long serialVersionUID = 31L;

    /**
     * The name of the method the Hadoop framework use to deserialize the InputSplit
     * 	instance
     */
    protected static String READ_OBJECT_HADOOP_METHOD_NAME = "readFields";

    /**
     * The name of the method the Hadoop framework use to serialize the InputSplit
     * 	instance
     */
    protected static String WRITE_OBJECT_HADOOP_METHOD_NAME = "write";

    /**
     * The actual class of the wrapped InputSplit instance (this means the name of a
     * 	class that extends InputSplit. E.g., FileSplit etc...)
     */
    protected Class<? extends InputSplit> inputSplitClass = null;

    /**
     * The {@link InputSplit} that represents the wrapped object
     */
    protected InputSplit inputSplit = null;

    public SerializableHadoopInputSplit() {

    }

    public SerializableHadoopInputSplit(InputSplit inputSplit, Class<? extends InputSplit> inputSplitClass) {
        this.inputSplitClass = inputSplitClass;
        this.inputSplit = inputSplit;
    }

    @Override
    public long getLength() throws IOException, InterruptedException {
        return inputSplit.getLength();
    }

    @Override
    public String[] getLocations() throws IOException, InterruptedException {
        return inputSplit.getLocations();
    }

    /**
     * The method of {@link Serializable} interface to implement to let the
     * 	{@link SerializableHadoopInputSplit} have special handling during the
     * 	deserialization process.
     * @param in {@link ObjectInputStream} to deserialize this object from
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        inputSplitClass = (Class<? extends InputSplit>) in.readObject();
        inputSplit = ReflectionUtils.newInstance(inputSplitClass, null);
        Method readObjectMethod = null;

        try {
            readObjectMethod = inputSplitClass.getMethod(READ_OBJECT_HADOOP_METHOD_NAME,
                    new Class[] { java.io.DataInput.class });
        } catch (SecurityException e) {
            // thrown by "readObjectMethod = inputSplitClass.getMethod(READ_OBJECT_HADOOP_METHOD_NAME, new Class[]{ java.io.ObjectInputStream.class });"
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // thrown by "readObjectMethod = inputSplitClass.getMethod(READ_OBJECT_HADOOP_METHOD_NAME, new Class[]{ java.io.ObjectInputStream.class });"
            e.printStackTrace();
        }

        if (readObjectMethod != null) {
            try {
                readObjectMethod.invoke(inputSplit, new Object[] { in });
            } catch (IllegalArgumentException e) {
                // thrown by "readObjectMethod.invoke(inputSplit, new Object[]{in});"
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // thrown by "readObjectMethod.invoke(inputSplit, new Object[]{in});"
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // thrown by "readObjectMethod.invoke(inputSplit, new Object[]{in});"
                e.printStackTrace();
            }
        }

        in.close();
    }

    /**
     * The method of {@link Serializable} interface to implement to let the
     * 	{@link SerializableHadoopInputSplit} have special handling during the
     * 	deserialization process.
     * @param out {@link ObjectInputStream} to deserialize this object from
     * @throws IOException
     */
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeObject(inputSplitClass);
        Method writeObjectMethod = null;

        try {
            writeObjectMethod = inputSplitClass.getMethod(WRITE_OBJECT_HADOOP_METHOD_NAME,
                    new Class[] { java.io.DataOutput.class });
        } catch (SecurityException e) {
            // thrown by "writeObjectMethod = inputSplitClass.getMethod(WRITE_OBJECT_HADOOP_METHOD_NAME, new Class[]{ java.io.ObjectOutputStream.class });"
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // thrown by "writeObjectMethod = inputSplitClass.getMethod(WRITE_OBJECT_HADOOP_METHOD_NAME, new Class[]{ java.io.ObjectOutputStream.class });"
            e.printStackTrace();
        }

        if (writeObjectMethod != null) {
            try {
                writeObjectMethod.invoke(inputSplit, new Object[] { out });
            } catch (IllegalArgumentException e) {
                // thrown by "writeObjectMethod.invoke(inputSplit, new Object[]{ out });"
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // thrown by "writeObjectMethod.invoke(inputSplit, new Object[]{ out });"
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // thrown by "writeObjectMethod.invoke(inputSplit, new Object[]{ out });"
                e.printStackTrace();
            }
        }

        out.close();
    }

    /**
     * Retrieve the wrapped Hadoop {@link InputSplit} instance
     * @return {@link InputSplit} the wrapped instance
     */
    public InputSplit getAdaptee() {
        return inputSplit;
    }

    @Override
    public String toString() {
        return inputSplit.toString();
    }

}
