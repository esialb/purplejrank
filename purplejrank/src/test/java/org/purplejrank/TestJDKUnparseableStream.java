package org.purplejrank;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.io.StreamCorruptedException;

import org.junit.Assert;
import org.junit.Test;

public class TestJDKUnparseableStream {
	/**
	 * Class that demonstrates the ambiguity in JDK object streams
	 * @author robin
	 *
	 */
	public static class A implements Serializable {
		private static final long serialVersionUID = 0;

		/*
		 * This magic value is the encoding of the string "Fail" followed by a null,
		 * as it would be written by defaultWriteObject
		 */
		public long fail = 0x7400044661696c70L;

		/*
		 * A single field, a long, should be written to the stream
		 */
		private void writeObject(ObjectOutputStream out) throws IOException {
			out.defaultWriteObject(); // written as raw stream metadata, not contained in any block
		}

		/*
		 * Because of ambiguity, the JDK allows the long to be interpreted as 
		 * a string reference followed by a null reference
		 */
		private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
			/*
			 * The first call to readObject should throw a StreamCorruptedException.
			 * JDK streams don't throw it.  PurpleJrank does.
			 */
			String fail = (String) in.readObject();
			Object nil = in.readObject();
			/*
			 * Verify that we _did_ manage to deserialize a default-written long as a string and
			 * a null
			 */
			if(!"Fail".equals(fail) || nil != null)
				throw new StreamCorruptedException();
		}
	}

	public static class B extends A {
		private static final long serialVersionUID = 0;
	}

	@Test
	public void testJdkFailsToFail() throws Exception {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(bout);
		out.writeObject(new B());
		out.close();

		ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()));
		in.readObject(); // this would fail if JDK streams weren't ambiguous
		in.close();
	}

	@Test
	public void testJrankSucceedsToFail() throws Exception {
		try {
			Util.cycle(new B());
			Assert.fail();
		} catch(ObjectStreamException e) {
			// expected
		}
	}
}
