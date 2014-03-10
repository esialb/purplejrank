package org.purplejrank;

import org.junit.Assert;
import org.junit.Test;

public class EnumTest {
	public static enum A {
		FOO,
		BAR,
	}
	
	@Test
	public void testCycle() throws Exception {
		A[] fb = new A[] {A.FOO, A.BAR};
		fb = (A[]) Util.cycle(fb);
		Assert.assertSame(A.FOO, fb[0]);
		Assert.assertSame(A.BAR, fb[1]);
	}

}
