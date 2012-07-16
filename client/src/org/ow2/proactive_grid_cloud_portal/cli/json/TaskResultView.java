package org.ow2.proactive_grid_cloud_portal.cli.json;

public class TaskResultView {

	private TaskIdView id;
	private byte[] serializedValue;

	public TaskIdView getId() {
		return id;
	}

	public void setId(TaskIdView id) {
		this.id = id;
	}

	public byte[] getSerializedValue() {
		return serializedValue;
	}

	public void setSerializedValue(byte[] serializedValue) {
		this.serializedValue = serializedValue;
	}
}
