package org.objectweb.proactive.core.descriptor.xml;

//import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.xml.XMLProperties;
import org.objectweb.proactive.core.xml.handler.BasicUnmarshaller;
import org.objectweb.proactive.core.xml.handler.PassiveCompositeUnmarshaller;
import org.objectweb.proactive.core.xml.io.Attributes;
 
public class VariablesHandler extends PassiveCompositeUnmarshaller implements
		ProActiveDescriptorConstants {
	
 
    public VariablesHandler() {
        super(false);
        this.addHandler(PROPERTY_TAG, new PropertiesHandler());
        this.addHandler(PROPERTIES_FILE_TAG, new PropertiesFileHandler());
    }

	public class PropertiesHandler extends BasicUnmarshaller {
	    PropertiesHandler() {
	    }

	    public void startContextElement(String tag, Attributes attributes)
	        throws org.xml.sax.SAXException {
	        // First control if it's a file tag
	        String file = attributes.getValue("file");
	        if (checkNonEmpty(file)) {
	            // Specific processing for loading file
	            XMLProperties.loadXML( file);
	            return;
	        }

	        // get datas
	        String name = attributes.getValue("name");
	        if (!checkNonEmpty(name)) {
	            throw new org.xml.sax.SAXException("Tag property have no name !");
	        }
	        String type = attributes.getValue("type");
	        if (!checkNonEmpty(type)) {
	            throw new org.xml.sax.SAXException("Tag property " + name +
	                " have no type !");
	        }
	        String value = attributes.getValue("value");
//	        if ((!checkNonEmpty(value)) && (!type.equalsIgnoreCase("setInProgram"))) {
//	            throw new org.xml.sax.SAXException("Tag property " + name +
//	                " have no value !");
//	        }
			if ( value==null) value = "";
			
	        // add property informations to list
	        try {
	            XMLProperties.setDescriptorVariable(name, value, type);
	        } catch (org.xml.sax.SAXException ex) {
	            throw ex;
	        }
	    }
	}

	private class PropertiesFileHandler extends BasicUnmarshaller {
	    PropertiesFileHandler() {
	    }

	    public void startContextElement(String tag, Attributes attributes)
	        throws org.xml.sax.SAXException {
	        // First control if it's a file tag
	        String file = attributes.getValue("location");
	        if (checkNonEmpty(file)) {
	            // Specific processing for loading file
	            XMLProperties.load(file);
	            return;
	        }

	    }
	}




}
