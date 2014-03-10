package org.purplejrank;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.junit.Assert;
import org.junit.Test;

public class WriteReplaceTest {
	public static class A implements Serializable {
		private Object writeReplace() {
			return "A";
		}
	}
	
	@Test
	public void testWriteReplace() throws Exception {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ObjectOutputStream out = new PurpleJrankOutput(bout);
		out.writeObject(new A());
		out.close();
		
		ObjectInputStream in = new PurpleJrankInput(new ByteArrayInputStream(bout.toByteArray()));
		Assert.assertEquals("A", in.readObject());
		in.close();
	}
}
