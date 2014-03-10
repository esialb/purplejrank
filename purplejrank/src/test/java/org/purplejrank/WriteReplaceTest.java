package org.purplejrank;

import java.io.Serializable;

import org.junit.Assert;
import org.junit.Test;

public class WriteReplaceTest {
	public static class A implements Serializable {
		private static final long serialVersionUID = 0;
		private Object writeReplace() {
			return "A";
		}
	}
	
	@Test
	public void testWriteReplace() throws Exception {
		Assert.assertEquals("A", Util.cycle(new A()));
	}
}
