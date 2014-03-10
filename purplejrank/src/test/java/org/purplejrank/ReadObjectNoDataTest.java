package org.purplejrank;

import java.io.Serializable;

import org.junit.Assert;
import org.junit.Test;

public class ReadObjectNoDataTest {
	public static class A implements Serializable {
		private static final long serialVersionUID = 0;
	}
	
	public static class AB extends A {
		private static final long serialVersionUID = 0;
	}
	
	public static class AC extends A {
		private static final long serialVersionUID = 0;
		public int foo = 0;
		
		@SuppressWarnings("unused")
		private void readObjectNoData() {
			foo = 1;
		}
	}
	
	public static class ACD extends AC {
		private static final long serialVersionUID = 0;
	}
	
	@Test
	public void testReadObjectNoData() throws Exception {
		byte[] buf = Util.serialize(new AB());
		
		MappingClassLoader mcl = new MappingClassLoader(ReadObjectNoDataTest.class.getClassLoader());
		mcl.map(AB.class, ACD.class);
		
		ACD acd = (ACD) Util.deserialize(buf, mcl);
		Assert.assertEquals(1, acd.foo);
	}
}
