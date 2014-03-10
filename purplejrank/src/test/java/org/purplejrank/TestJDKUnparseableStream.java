package org.purplejrank;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class TestJDKUnparseableStream {
	public static class A implements Serializable {
		private static final long serialVersionUID = 0;
		
		public Object fail = Arrays.asList("this", "will", "fail");
		
		private void writeObject(ObjectOutputStream out) throws IOException {
			
		}
	}
	
	@Test
	public void testJdkFails() throws Exception {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(bout);
		out.writeObject(new A());
		out.close();
		
		ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()));
		try {
			in.readObject();
			Assert.fail();
		} catch(StreamCorruptedException e) {
			// expected
		} finally {
			in.close();
		}
	}
	
	@Test
	public void testJrankSucceeds() throws Exception {
		Util.cycle(new A());
	}
}
