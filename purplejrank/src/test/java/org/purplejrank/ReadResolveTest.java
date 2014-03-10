package org.purplejrank;

import java.io.Serializable;

import org.junit.Assert;
import org.junit.Test;

public class ReadResolveTest {
	public static class A implements Serializable {
		private static final long serialVersionUID = 0;
		private Object readResolve() {
			return "A";
		}
	}
	
	public static class B implements Serializable {
		private static final long serialVersionUID = 0;
		public C c;
		
		private Object readResolve() {
			return new B();
		}
	}
	public static class C implements Serializable {
		private static final long serialVersionUID = 0;
		public B b;
	}
	
	@Test
	public void testReadResolve() throws Exception {
		Assert.assertEquals("A", Util.cycle(new A()));
	}
	
	@Test
	public void testCyclic() throws Exception {
		B b = new B();
		C c = new C();
		
		b.c = c; c.b = b;
		
		Object[] bc = new Object[] {b, c};
		
		bc = (Object[]) Util.cycle(bc);
		
		b = (B) bc[0];
		c = (C) bc[1];
		
		Assert.assertNull(b.c);
		Assert.assertNotNull(c.b.c);
	}
}
