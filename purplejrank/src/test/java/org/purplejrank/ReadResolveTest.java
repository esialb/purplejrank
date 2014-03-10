package org.purplejrank;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
	
	@Test
	public void testReadResolve() throws Exception {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ObjectOutputStream out = new PurpleJrankOutput(bout);
		out.writeObject(new A());
		out.close();
		
		ObjectInputStream in = new PurpleJrankInput(new ByteArrayInputStream(bout.toByteArray()));
		Assert.assertEquals("A", in.readObject());
		in.close();
	}
}
