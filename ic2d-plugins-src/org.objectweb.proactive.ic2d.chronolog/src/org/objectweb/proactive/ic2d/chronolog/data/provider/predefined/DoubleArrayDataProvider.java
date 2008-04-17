package org.objectweb.proactive.ic2d.chronolog.data.provider.predefined;

import javax.management.MBeanServerConnection;

import org.objectweb.proactive.ic2d.chronolog.data.provider.IDataProvider;


public class DoubleArrayDataProvider implements IDataProvider {

    public static final String NAME = "arrayOfFloat";
    public static final String DESCRIPTION = "Test purpose array of float.";
    public static final String TYPE = "[D";

    /**
     * The reference on the array
     */
    private final double[] arr;

    /**
     * Builds a new instance of FloatArrayDataProvider class.
     */
    public DoubleArrayDataProvider() {
        this.arr = new double[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.ic2d.chronolog.data.provider.IDataProvider#provideValue()
     */
    public Object provideValue() {
        return this.arr;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.ic2d.chronolog.data.provider.IDataProvider#getName()
     */
    public String getName() {
        return NAME;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.ic2d.chronolog.data.provider.IDataProvider#getDescription()
     */
    public String getDescription() {
        return DESCRIPTION;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.ic2d.chronolog.data.provider.IDataProvider#getType()
     */
    public String getType() {
        return TYPE;
    }

    // /////////////////////////////////////////////
    // Static methods for local and remote creation
    // /////////////////////////////////////////////

    /**
     * Returns a new reference on the data provider
     * 
     * @param mBeanServerConnection
     *            The connection to the remote MBean server
     * @return The reference on the data provider
     */
    public static DoubleArrayDataProvider build(final MBeanServerConnection mBeanServerConnection) {
        return new DoubleArrayDataProvider();
    }
}