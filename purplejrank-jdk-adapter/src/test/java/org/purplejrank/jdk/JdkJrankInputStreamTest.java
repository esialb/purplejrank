package org.purplejrank.jdk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class JdkJrankInputStreamTest {
	
	@Parameters
	public static Iterable<Object[]> params() {
		List<Object> test = Arrays.<Object>asList(
				Calendar.getInstance(),
				1,
				new Vector<Object>(Arrays.<Object>asList(1,"two", 3.)),
				new Date()
				);
		List<Object[]> params = new ArrayList<Object[]>();
		for(Object t : test) 
			params.add(new Object[] {t});
		return params;
	}
	
	protected Object obj;
	
	public JdkJrankInputStreamTest(Object obj) {
		this.obj = obj;
	}
	
	@Test
	public void testInput() throws Exception {
		Assert.assertEquals(obj, Util.cycle(obj));
	}
}
