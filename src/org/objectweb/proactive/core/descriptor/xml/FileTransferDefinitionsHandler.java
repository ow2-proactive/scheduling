/*
 * Created on Jul 26, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.objectweb.proactive.core.descriptor.xml;

import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.process.filetransfer.FileTransfer;
import org.objectweb.proactive.core.xml.handler.BasicUnmarshaller;
import org.objectweb.proactive.core.xml.handler.PassiveCompositeUnmarshaller;
import org.objectweb.proactive.core.xml.handler.UnmarshallerHandler;
import org.objectweb.proactive.core.xml.io.Attributes;
import org.xml.sax.SAXException;



/**
 * @author mleyton
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class FileTransferDefinitionsHandler extends
		PassiveCompositeUnmarshaller implements ProActiveDescriptorConstants {

	protected ProActiveDescriptor proActiveDescriptor;

	public FileTransferDefinitionsHandler(
			ProActiveDescriptor proActiveDescriptor) {
		super(false);
		this.proActiveDescriptor = proActiveDescriptor;
		addHandler(FILE_TRANSFER_TAG, new FileTransferHandler());
	}

	protected void notifyEndActiveHandler(String name,
			UnmarshallerHandler activeHandler) throws SAXException {
		
	}


	public class FileTransferHandler extends PassiveCompositeUnmarshaller
			implements ProActiveDescriptorConstants {

		protected FileTransfer fileTransfer;

		public FileTransferHandler() {
			super(false);
			addHandler(FILE_TRANSFER_FILE_TAG, new FileHandler());
			addHandler(FILE_TRANSFER_DIR_TAG, new DirHandler());
			
			//This will be initalized once we have the ID in in the startContextElement(...)
			fileTransfer=null;  
		}

		public void startContextElement(String name, Attributes attributes)
				throws SAXException {

			String fileTransferId = attributes.getValue("id");

			if (!checkNonEmpty(fileTransferId)) {
				throw new org.xml.sax.SAXException(
						"FileTransfer defined without id");
			}
			
			if(fileTransferId.equalsIgnoreCase(FILE_TRANSFER_IMPLICT_KEYWORD))
				throw new org.xml.sax.SAXException("FileTransferDefinition id attribute is using illegal keyword: "+
						FILE_TRANSFER_IMPLICT_KEYWORD);
						
			/* We get a reference on the FileTransfer object with this ID.
			 * If this object doesn't exist the createFileTransfer will create
			 * one. All future calls on this method will then return this same 
			 * instance for this ID.
			 */
			fileTransfer = proActiveDescriptor.getFileTransfer(fileTransferId);
		}

		public Object getResultObject() throws SAXException {
			return fileTransfer;  //not really used for now
		}
		
		public class FileHandler extends BasicUnmarshaller implements
				ProActiveDescriptorConstants {

			public void startContextElement(String name, Attributes attributes)
					throws SAXException {

				String source = attributes.getValue("src");
				String dest = attributes.getValue("dest");

				if (!checkNonEmpty(source)) {
					throw new org.xml.sax.SAXException(
							"Source filename not specified for file tag");
				}

				if (!checkNonEmpty(dest))
					dest = source;

				//fileTransfer variable is in the parent class
				fileTransfer.addFile(source, dest);
			}
		}

		public class DirHandler extends BasicUnmarshaller implements
				ProActiveDescriptorConstants {

			public void startContextElement(String name, Attributes attributes)
					throws SAXException {

				String source = attributes.getValue("src");
				String dest = attributes.getValue("dest");
				String include = attributes.getValue("include");
				String exclude = attributes.getValue("exclude");

				if (!checkNonEmpty(source)) {
					throw new org.xml.sax.SAXException(
							"Source filename not specified for file tag");
				}

				if (!checkNonEmpty(dest))
					dest = source;
				if (!checkNonEmpty(include))
					include = "*";
				if (!checkNonEmpty(exclude))
					exclude = "";
				
				//fileTransfer variable is in the parent class
				fileTransfer.addDir(source, dest, include, exclude);
			}
		}

	}
}
