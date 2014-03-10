package org.purplejrank;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

public class WriteReplaceTest {
	public static class A implements Serializable {
		private static final long serialVersionUID = 0;
		private Object writeReplace() {
			return "A";
		}
	}
	
	public static class B implements Serializable {
		private static final long serialVersionUID = 0;
		public static AtomicInteger count = new AtomicInteger();
		
		public B thiz = this;
		
		private void writeObject(ObjectOutputStream out) throws IOException {
			out.defaultWriteObject();
			count.incrementAndGet();
		}
		
		private Object writeReplace() {
			return new B();
		}
	}
	
	@Test
	public void testWriteReplace() throws Exception {
		Assert.assertEquals("A", Util.cycle(new A()));
	}
	
	@Test
	public void testCyclic() throws Exception {
		B b1, b2;
		b1 = b2 = new B();
		Object[] bb = new Object[] {b1, b2};
		bb = (Object[]) Util.cycle(bb);
		b1 = (B) bb[0]; b2 = (B) bb[1];
		Assert.assertSame(b1, b2);
		Assert.assertEquals(1, B.count.get());
	}
}
