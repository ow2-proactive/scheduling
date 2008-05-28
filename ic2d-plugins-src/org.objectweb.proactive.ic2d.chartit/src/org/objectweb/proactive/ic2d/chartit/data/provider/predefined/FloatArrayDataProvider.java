package org.objectweb.proactive.ic2d.chartit.data.provider.predefined;

import org.objectweb.proactive.ic2d.chartit.data.provider.IDataProvider;


public class FloatArrayDataProvider implements IDataProvider {

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
    public FloatArrayDataProvider() {
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
}