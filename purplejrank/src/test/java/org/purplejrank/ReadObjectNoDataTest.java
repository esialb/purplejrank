package org.purplejrank;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.junit.Assert;
import org.junit.Test;
import org.purplejrank.io.StreamReadableByteChannel;
import org.purplejrank.io.StreamWritableByteChannel;

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
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ObjectOutputStream out = new PurpleJrankOutput(new StreamWritableByteChannel(bout));
		out.writeObject(new AB());
		out.close();
		
		MappingClassLoader mcl = new MappingClassLoader(ReadObjectNoDataTest.class.getClassLoader());
		mcl.map(AB.class, ACD.class);
		
		ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
		ObjectInputStream in = new PurpleJrankInput(new StreamReadableByteChannel(bin), mcl);
		ACD acd = (ACD) in.readObject();
		in.close();
		
		Assert.assertEquals(1, acd.foo);
	}
}
