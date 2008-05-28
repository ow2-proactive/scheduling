package org.objectweb.proactive.ic2d.chartit.data.provider.predefined;

import org.objectweb.proactive.ic2d.chartit.data.provider.IDataProvider;


public class StringArrayDataProvider implements IDataProvider {

    public static final String NAME = "arrayOfString";
    public static final String DESCRIPTION = "Test purpose array of String.";
    public static final String TYPE = "[Ljava.lang.String;";

    /**
     * The reference on the array
     */
    private final String[] arr;

    /**
     * Builds a new instance of StringArrayDataProvider class.
     */
    public StringArrayDataProvider() {
        this.arr = new String[] { "val1", "val2", "val3", "val4", "val5", "val6", "val7", "val8", "val9" };
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