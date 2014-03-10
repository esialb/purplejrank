package org.purplejrank.mods;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.purplejrank.PurpleJrankOutput;
import org.purplejrank.mods.NullsJrankInput;

public class NullsJrankInputTest {
	public static class Missing implements Serializable {
		private static final long serialVersionUID = 0;
		
		public Counter counter = new Counter();
	}
	
	public static class Counter implements Externalizable {
		private static final long serialVersionUID = 0;
		public static AtomicInteger count = new AtomicInteger();
		public Counter() {
			count.incrementAndGet();
		}
		
		@Override
		public void writeExternal(ObjectOutput out) throws IOException {}
		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {}
	}
	
	private static class MissingMissingClassLoader extends ClassLoader {
		public MissingMissingClassLoader() {
			super(MissingMissingClassLoader.class.getClassLoader());
		}
		
		@Override
		public Class<?> loadClass(String name) throws ClassNotFoundException {
			Class<?> cls = super.loadClass(name);
			if(cls == Missing.class)
				throw new ClassNotFoundException();
			return cls;
		}
	}
	
	@Test
	public void testMissingAsNull() throws Exception {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		try {
			ObjectOutputStream out = new PurpleJrankOutput(bout);
			Missing m = new Missing();
			out.writeObject(m);
			out.writeObject(m.counter);
			out.close();
		} catch(Exception e) {
			Assume.assumeNoException(e);;
		}
		
		byte[] buf = bout.toByteArray();

		ByteArrayInputStream bin = new ByteArrayInputStream(buf);
		ObjectInputStream in = new NullsJrankInput(bin, new MissingMissingClassLoader());

		Assert.assertNull(in.readObject());;
		Assert.assertTrue(in.readUnshared() instanceof Counter);
		Assert.assertEquals(3, Counter.count.get());

		in.close();
	}
}
