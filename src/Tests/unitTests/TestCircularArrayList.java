package unitTests;

import static junit.framework.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.core.mop.Utils;
import org.objectweb.proactive.core.util.CircularArrayList;

public class TestCircularArrayList {
	
	private CircularArrayList cal;
	
	@Before
	public void setUp() {
		cal = new CircularArrayList();
	}
	
	
	/**
	 * Add and remove 50 elements and check that size() is ok
	 */
	@Test
	public void addAndRemove() {
		int nbElem = 50;
		
		for (int i=0; i<nbElem; i++)
			cal.add(i);
		assertTrue(cal.size() == nbElem);

		for (int i=0; i<nbElem; i++)
			cal.remove(0);
		assertTrue(cal.size() == 0);
	}
	
	/**
	 * Remove() on an empty list must thrown an {@link IndexOutOfBoundsException} exception
	 */
	@Test(expected=IndexOutOfBoundsException.class)
	public void removeTooManyElems() {
		cal.remove(0);
	}
	
	/**
	 * Serialization
	 * @throws IOException
	 */
	@Test
	public void serialization() throws IOException {
		int nbElem = 50;
		
		for (int i=0; i<nbElem; i++)
			cal.add(i);
	
		CircularArrayList r = (CircularArrayList) Utils.makeDeepCopy(cal);
		assertTrue(r.equals(cal));
	}
	
	@Test
	public void collectionAsParameter() {
		Collection<Integer> col = new ArrayList<Integer>();
		for (int i=0; i<50; i++)
			col.add(i);
		
		CircularArrayList o = new CircularArrayList(col);
		
		assertTrue(col.equals(o));
		
		assertTrue(o.size() == col.size());
	}
}
