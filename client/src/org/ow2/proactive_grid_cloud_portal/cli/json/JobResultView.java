package org.ow2.proactive_grid_cloud_portal.cli.json;

import java.util.Map;

public class JobResultView {

	private JobIdView id;
	private Map<String, TaskResultView> allResults;

	public JobIdView getId() {
		return id;
	}

	public void setId(JobIdView id) {
		this.id = id;
	}

	public Map<String, TaskResultView> getAllResults() {
		return allResults;
	}

	public void setAllResults(Map<String, TaskResultView> allResults) {
		this.allResults = allResults;
	}
}
