package org.objectweb.proactive.ic2d.chronolog.data.provider.predefined;

import javax.management.MBeanServerConnection;

import org.objectweb.proactive.ic2d.chronolog.data.provider.IDataProvider;


public class FloatArrayDataProvider implements IDataProvider {

    public static final String NAME = "arrayOfFloat";
    public static final String DESCRIPTION = "Test purpose array of float of size 9.";
    public static final String TYPE = "[F";

    /**
     * The reference on the array
     */
    private final float[] arr;

    /**
     * Builds a new instance of FloatArrayDataProvider class.
     */
    public FloatArrayDataProvider() {
        this.arr = new float[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
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
     * Returns the reference on the remote MBean
     * 
     * @param mBeanServerConnection
     *            The connection to the remote MBean server
     * @return The reference on the remote MBean
     */
    public static FloatArrayDataProvider build(final MBeanServerConnection mBeanServerConnection) {
        return new FloatArrayDataProvider();
    }
}
