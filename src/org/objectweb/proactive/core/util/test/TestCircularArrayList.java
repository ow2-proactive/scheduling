package org.objectweb.proactive.core.util.test;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.objectweb.proactive.core.body.request.RequestImpl;
import org.objectweb.proactive.core.body.request.RequestQueueImpl;
import org.objectweb.proactive.core.util.CircularArrayList;

public class TestCircularArrayList extends TestCase {

	private Object o, o2, o3;
	private CircularArrayList list;

	protected void setUp() {
		o = new Object();
		Object o2 = new Object();
		Object o3 = new Object();
		this.list = new CircularArrayList();
	}

	public void testInsertion() {
		int size = list.size();
		list.add(o);
		Assert.assertEquals(size + 1, list.size());
	}

	public void testEmpty() {
		this.list = new CircularArrayList();
		Assert.assertTrue(" List should be empty", list.isEmpty());
		this.list.add(o);
		Assert.assertTrue(" List should not be empty", !list.isEmpty());
	}

	public void testIndex() {

		this.list = new CircularArrayList();
		this.list.add(o);
		this.list.add(o2);
		this.list.add(o3);
		Assert.assertEquals(o, list.get(0));
		Assert.assertEquals(o2, list.get(1));
		Assert.assertEquals(o3, list.get(2));
	}

	public void testRemove() {
		this.list = new CircularArrayList();
		list.add(o);
		list.add(o2);
		list.add(o3);

		Assert.assertEquals(o, list.remove(0));
		Assert.assertEquals(2, list.size());
		Assert.assertEquals(o2, list.remove(0));
		Assert.assertEquals(1, list.size());
		Assert.assertEquals(o3, list.remove(0));
		Assert.assertEquals(0, list.size());

		try {
			list.remove(0);
			Assert.fail("Remove should have thrown exception");
		} catch (Exception e) {
		}
	}

	public TestCircularArrayList(String str) {
		super(str);
	}
}