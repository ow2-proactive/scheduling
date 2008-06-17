/**
 * 
 */
package org.objectweb.proactive.extensions.resourcemanager.core.properties;

import java.io.IOException;
import java.util.Properties;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.config.PAProperties.PAPropertiesType;


/**
 * PAResourceManagerProperties contains all ProActive Resource Manager properties.
 * 
 * You must use provide methods in order to get this RM properties.
 * 
 * @author The ProActiveTeam
 * @date 11 june 08
 * @version 4.0
 * @since ProActive 4.0
 *
 */
@PublicAPI
public enum PAResourceManagerProperties {

    /* ***************************************************************** */
    /* ********************** RMCORE PROPERTIES ********************* */
    /* ***************************************************************** */

    /** Default rm node name */
    RM_DEFAULT_NAME("pa.rm.core.defaultname", PAPropertiesType.STRING);

    //TODO germs ?
    //Define your properties here as shown in the example.
    //then go in your code and replace the property by the
    //PAResourceManagerProperties.PROPERTY_NAME.getValueAsInt|String|Boolean() method
    //don't forget to feel the PAResourceManagerProperties.ini file in the same package as shown
    //If needed, you have the same class and ini file in the scheduler.core.properties folder.
    //enjoy ;)

    /* ***************************************************************************** */
    /* ***************************************************************************** */
    /** Default properties file for the scheduler configuration */
    private static final String DEFAULT_PROPERTIES_FILE = "PASchedulerProperties.ini";
    /** memory entity of the properties file. */
    private static Properties prop = null;
    /** Key of the specific instance. */
    private String key;
    /** value of the specific instance. */
    private PAPropertiesType type;

    /**
     * Create a new instance of PAResourceManagerProperties
     *
     * @param str the key of the instance.
     * @param type the real java type of this instance.
     */
    PAResourceManagerProperties(String str, PAPropertiesType type) {
        this.key = str;
        this.type = type;
    }

    /**
     * Get the properties map or load it if needed.
     * 
     * @return the properties map corresponding to the default property file.
     */
    private static Properties getProperties() {
        if (prop == null) {
            prop = new Properties();
            try {
                prop.load(PAResourceManagerProperties.class.getResourceAsStream(DEFAULT_PROPERTIES_FILE));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return prop;
    }

    /**
     * Returns the value of this property as an integer.
     * If value is not an integer, an exception will be thrown.
     * 
     * @return the value of this property.
     */
    public int getValueAsInt() {
        String valueS = getValueAsString();
        try {
            int value = Integer.parseInt(valueS);
            return value;
        } catch (NumberFormatException e) {
            RuntimeException re = new IllegalArgumentException(key +
                " is not an integer property. getValueAsInt cannot be called on this property");
            throw re;
        }
    }

    /**
     * Returns the value of this property as a string.
     * 
     * @return the value of this property.
     */
    public String getValueAsString() {
        return getProperties().getProperty(key);
    }

    /**
     * Returns the value of this property as a boolean.
     * If value is not a boolean, an exception will be thrown.<br>
     * The behavior of this method is the same as the {@link java.lang.Boolean.parseBoolean(String s)}. 
     * 
     * @return the value of this property.
     */
    public boolean getValueAsBoolean() {
        return Boolean.parseBoolean(getValueAsString());
    }

    /**
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return getValueAsString();
    }

}
