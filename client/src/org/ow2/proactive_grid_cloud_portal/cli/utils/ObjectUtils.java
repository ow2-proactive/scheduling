package org.ow2.proactive_grid_cloud_portal.cli.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class ObjectUtils {

	public static Object object(byte[] bytes) {
		try {
			return new ObjectInputStream(new ByteArrayInputStream(bytes))
					.readObject();
		} catch (ClassNotFoundException cnfe) {
			return String.format("[De-serialization error : %s]",
					cnfe.getMessage());
		} catch (IOException ioe) {
			return String.format("[De-serialization error : %s]",
					ioe.getMessage());
		}
	}

}
