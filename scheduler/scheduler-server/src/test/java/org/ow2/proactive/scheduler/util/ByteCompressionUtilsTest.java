package org.ow2.proactive.scheduler.util;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.io.IOException;
import java.util.zip.DataFormatException;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.Before;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.exception.UserException;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;

public class ByteCompressionUtilsTest {
	
	private TaskFlowJob job ;
	
	private byte[] jobByte;
	
	
	@Before
	public void setup() throws UserException {
		job = new TaskFlowJob();
		job.setName(this.getClass().getName());
		job.addTask(new JavaTask());
		
		jobByte = SerializationUtils.serialize(job);
	}

	@Test
	public void test() throws IOException, DataFormatException {
		byte[] compressed = ByteCompressionUtils.compress(jobByte);
		System.out.println("compressed lenght : " + jobByte.length);
		System.out.println("compressed lenght : " + compressed.length);
		assertThat(compressed.length < jobByte.length, is(true));
		byte[] decompressed = ByteCompressionUtils.decompress(compressed);
		System.out.println("compressed lenght : " + compressed.length);
		System.out.println("decompressed lenght : " + decompressed.length);
		assertThat(decompressed.length == jobByte.length, is(true));
	}

}
