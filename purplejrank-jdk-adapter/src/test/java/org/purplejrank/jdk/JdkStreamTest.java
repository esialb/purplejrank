package org.purplejrank.jdk;

import java.util.BitSet;
import java.util.Calendar;

import org.junit.Test;

public class JdkStreamTest {
	@Test
	public void testVerify() throws Exception {
		Util.stream(Calendar.getInstance()).verify();
		Util.stream(new BitSet()).verify();
	}
}
