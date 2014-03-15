package org.purplejrank.jdk;

import java.util.Calendar;

import org.junit.Assert;
import org.junit.Test;

public class JdkJrankInputStreamTest {
	@Test
	public void testInput() throws Exception {
		Calendar c = Calendar.getInstance();
		Assert.assertEquals(c, Util.cycle(c));
	}
}
