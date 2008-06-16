package org.objectweb.proactive.ic2d.chartit.data.provider;

import java.util.Collection;

import org.objectweb.proactive.ic2d.chartit.util.Utils;


/**
 * This class provides a generic way to get the size of a collection. The
 * <code>type</code> of the provided value is <code>int</code>.
 * 
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
 */
public final class CollectionSizeDataProvider implements IDataProvider {

    /**
     * The name of this provider
     */
    private final String name;

    /**
     * The description of the provided value
     */
    private final String description;

    /**
     * The collection that will provide its size
     */
    private final Collection<?> collection;

    /**
     * Creates a new instance of <code>CollectionSizeDataProvider</code>
     * class.
     * 
     * @param name
     *            The name of this provider
     * @param description
     *            The description of the provided value
     * @param collection
     *            The collection that will provide its size
     */
    public CollectionSizeDataProvider(final String name, final String description,
            final Collection<?> collection) {
        this.name = name;
        this.description = description;
        this.collection = collection;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.ic2d.chronolog.data.provider.IDataProvider#getName()
     */
    public String getName() {
        return this.name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.ic2d.chronolog.data.provider.IDataProvider#getDescription()
     */
    public String getDescription() {
        return this.description;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.ic2d.chronolog.data.provider.IDataProvider#getType()
     */
    public String getType() {
        return Utils.PRIMITIVE_TYPE_INT;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.ic2d.chronolog.data.provider.IDataProvider#provideValue()
     */
    public Object provideValue() {
        return this.collection.size();
    }
}
