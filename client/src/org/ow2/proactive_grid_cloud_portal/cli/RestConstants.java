package org.ow2.proactive_grid_cloud_portal.cli;

import java.io.File;

public class RestConstants {
	public static final String DFLT_REST_SCHEDULER_URL = "http://localhost:8080/SchedulingRest/rest";

	public static final String DFLT_SESSION_DIR = System
			.getProperty("user.home") + File.separator + ".proactive";

	public static final String DFLT_SESSION_FILE_EXT = "-session-id";

	private RestConstants() {
	}
}
